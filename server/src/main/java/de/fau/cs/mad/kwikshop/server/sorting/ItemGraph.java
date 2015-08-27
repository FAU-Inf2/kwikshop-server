package de.fau.cs.mad.kwikshop.server.sorting;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import de.fau.cs.mad.kwikshop.common.ShoppingListServer;
import de.fau.cs.mad.kwikshop.common.sorting.BoughtItem;
import de.fau.cs.mad.kwikshop.common.sorting.ItemOrderWrapper;
import de.fau.cs.mad.kwikshop.common.sorting.SortingRequest;
import de.fau.cs.mad.kwikshop.server.dao.BoughtItemDAO;
import de.fau.cs.mad.kwikshop.server.dao.EdgeDAO;
import de.fau.cs.mad.kwikshop.server.dao.SupermarketChainDAO;
import de.fau.cs.mad.kwikshop.server.dao.SupermarketDAO;

public class ItemGraph {

    private Set<BoughtItem> vertices;
    private Set<Edge> edges;

    private Supermarket supermarket;

    private BoughtItemDAO boughtItemDAO;
    private EdgeDAO edgeDAO;
    private SupermarketDAO supermarketDAO;
    private SupermarketChainDAO supermarketChainDAO;

    public ItemGraph(BoughtItemDAO boughtItemDAO, EdgeDAO edgeDAO,
                     SupermarketDAO supermarketDAO, SupermarketChainDAO supermarketChainDAO) {

        this.boughtItemDAO = boughtItemDAO;
        this.edgeDAO = edgeDAO;
        this.supermarketDAO = supermarketDAO;
        this.supermarketChainDAO = supermarketChainDAO;

    }

    public Set<BoughtItem> getVertices() {
        return vertices;
    }

    public Set<Edge> getEdges() {
        return edges;
    }

    public boolean setSupermarket(String placeId, String supermarketName) {
        boolean isNewSupermarket = false;

        this.supermarket = supermarketDAO.getByPlaceId(placeId);

        /* Supermarket does not exist yet, create it and try to find a matching SupermarketChain */
        if(supermarket == null) {
            isNewSupermarket = true;
            supermarket = new Supermarket(placeId);

            for(SupermarketChain supermarketChain : supermarketChainDAO.getAll()) {
                /* If the supermarket's name contains the name of a chain, it (most likely) belongs to that chain */
                if(supermarketName.toLowerCase().contains(supermarketChain.getName().toLowerCase())) {
                    supermarket.setSupermarketChain(supermarketChain);
                    break;
                }
            }
            supermarketDAO.createSupermarkt(supermarket);
        }
        return isNewSupermarket;
    }


    public void addItemOrder(ItemOrderWrapper itemOrder) {
        setSupermarket(itemOrder.getSupermarketPlaceId(), itemOrder.getSupermarketName());

        update();

        addBoughtItems(itemOrder.getBoughtItemList());
    }

    private void update() {
        /* Load Edges */
        List<Edge> edgeList = edgeDAO.getBySupermarket(supermarket);
        if(edgeList != null)
            edges = new HashSet<Edge>(edgeList);
        else
            edges = new HashSet<Edge>();

        /* Load Vertices */
        vertices = new HashSet<BoughtItem>();
        for(Edge edge : edgeList) {
            if(!vertices.contains(edge.getFrom()))
                vertices.add(edge.getFrom());

            if(!vertices.contains(edge.getTo()))
                vertices.add(edge.getTo());
        }

        /* Debug output */
        System.out.println(String.format("ItemGraph refreshed, containing %s Edges and %s vertices.", edgeList.size(), vertices.size()));
        System.out.println("Vertices:");
        for(BoughtItem boughtItem : vertices) {
            System.out.println(boughtItem.getName());
        }
        System.out.println("Edges:");
        for(Edge edge: edgeList) {
            System.out.println(String.format("%s -> %s (%s)", edge.getFrom().getName(), edge.getTo().getName(), String.valueOf(edge.getWeight())));
        }

    }

    /* Create or update an Edge for the given combination of BoughtItems and Supermarket */
    public void createOrUpdateEdge(BoughtItem i1, BoughtItem i2, Supermarket supermarket) {

        /* Supermarket must be included because different supermarkets have different edges */
        Edge edge = edgeDAO.getByFromTo(i1, i2, supermarket);

        if(edge == null) {
            /* Create new edge */
            edgeDAO.createEdge(new Edge(i1, i2, supermarket));
        } else {
            /* Edit existing edge - increase weight */
            edge.setWeight(edge.getWeight()+1);
        }
    }

    private void addBoughtItems(List<BoughtItem> boughtItems) {

        /* Save all new boughtItems (vertices) */
        for(BoughtItem boughtItem: boughtItems) {
            if(boughtItemDAO.getByName(boughtItem.getName()) == null)
                boughtItemDAO.createBoughtItem(boughtItem);
        }

        /* Save all new edges */
        for(int i = 0; i < boughtItems.size()-1; i++) {
            /* BoughtItems need to be loaded from the DB, otherwise Hibernate complains about unsaved objects */
            BoughtItem i1 = boughtItemDAO.getByName(boughtItems.get(i).getName());
            BoughtItem i2 = boughtItemDAO.getByName(boughtItems.get(i+1).getName());

            /* Apply the Edge to this supermarket's graph */
            createOrUpdateEdge(i1, i2, supermarket);

            /* If this supermarkt belongs to a chain, apply the Edge to this chain's global graph */
            if(supermarket.getSupermarketChain() != null) {
                Supermarket globalSupermarket = supermarketDAO.getGlobalBySupermarketChain(supermarket.getSupermarketChain());
                createOrUpdateEdge(i1, i2, globalSupermarket);
            }
        }

        update();

    }

    public ShoppingListServer executeAlgorithm(Algorithm algorithm, ShoppingListServer shoppingList, SortingRequest sortingRequest) {
        if(setSupermarket(sortingRequest.getPlaceId(), sortingRequest.getSupermarketName()) == true) {
            this.supermarket = supermarketChainDAO.getGlobalSupermarket(supermarket.getSupermarketChain());
            System.out.println("Using global ItemGraph for SupermarketChain " + supermarket.getSupermarketChain().getName());
        }
        algorithm.setUp(this);
        return algorithm.sort(shoppingList);
    }

    private List<BoughtItem> getParents(BoughtItem child) {
        List<BoughtItem> parents = new ArrayList<BoughtItem>();

        for(Edge edge: edges) {
            if(edge.getTo() == child)
                parents.add(edge.getFrom());
        }

        return parents;
    }

    private List<BoughtItem> getChildren(BoughtItem parent) {
        List<BoughtItem> children = new ArrayList<BoughtItem>();

        for(Edge edge: edges) {
            if(edge.getFrom() == parent)
                children.add(edge.getTo());
        }

        return children;
    }

    private List<BoughtItem> getSiblings(BoughtItem child) {
        List<BoughtItem> siblings = new ArrayList<BoughtItem>();

        for(BoughtItem parent: getParents(child)) {
            siblings.addAll(getChildren(parent));
        }

        /* Remove all occurrences of this child */
        while(siblings.remove(child)) {}

        return siblings;
    }

}
