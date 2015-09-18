package de.fau.cs.mad.kwikshop.server.sorting;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import de.fau.cs.mad.kwikshop.common.ArgumentNullException;
import de.fau.cs.mad.kwikshop.common.ShoppingList;
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

    private DAOHelper daoHelper;

    public ItemGraph(DAOHelper daoHelper) {
        this.daoHelper = daoHelper;
    }

    public DAOHelper getDaoHelper() {
        return daoHelper;
    }

    public Set<BoughtItem> getVertices() {
        if(vertices == null)
            vertices = new HashSet<BoughtItem>();
        return vertices;
    }

    public Set<Edge> getEdges() {
        if(edges == null)
            edges = new HashSet<Edge>();
        return edges;
    }

    public boolean setSupermarket(String placeId, String supermarketName) {

        boolean isNewSupermarket = false;

        this.supermarket = daoHelper.getSupermarketByPlaceID(placeId);

        /* Supermarket does not exist yet, create it and try to find a matching SupermarketChain */
        if(supermarket == null) {
            isNewSupermarket = true;
            supermarket = new Supermarket(placeId);

            for(SupermarketChain supermarketChain : daoHelper.getAllSupermarketChains()) {
                /* If the supermarket's name contains the name of a chain, it (most likely) belongs to that chain */
                if(supermarketName.toLowerCase().contains(supermarketChain.getName().toLowerCase())) {
                    supermarket.setSupermarketChain(supermarketChain);
                    break;
                }
            }
            daoHelper.createSupermarket(supermarket);
        }

        return isNewSupermarket;

    }

    public Supermarket getSupermarket() {
        return supermarket;
    }

    public void update() {

        /* Update() loads all Edges and Vertices of one specific supermarket -> supermarket may not be null */
        if(supermarket == null) {
            throw new ArgumentNullException("supermarket");
        }

        /* Load Edges */
        List<Edge> edgeList = daoHelper.getEdgesBySupermarket(supermarket);
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
    public Edge createOrUpdateEdge(BoughtItem i1, BoughtItem i2, Supermarket supermarket) {

        /* Supermarket must be included because different supermarkets have different edges */
        Edge edge = daoHelper.getEdgeByFromTo(i1, i2, supermarket);

        if(edge == null) {

            /* Check if there is an Edge in the opposite direction */
            edge = daoHelper.getEdgeByFromTo(i2, i1, supermarket);

            if(edge != null) {
                /* Edit existing edge - decrease weight */
                edge.setWeight(edge.getWeight()-1);

                /* Create edge in the opposite direction */
                if(edge.getWeight() < 0) {
                    daoHelper.deleteEdge(edge);
                    daoHelper.createEdge(new Edge(i1, i2, supermarket));
                    edge = daoHelper.getEdgeByFromTo(i1, i2, supermarket);
                }
            } else {
                /* Create new edge */
                daoHelper.createEdge(new Edge(i1, i2, supermarket));
                edge = daoHelper.getEdgeByFromTo(i1, i2, supermarket);
            }

        } else {
            /* Edit existing edge - increase weight */
            edge.setWeight(edge.getWeight()+1);
        }

        return edge;

    }

    /* Adds the start and end Items for each Supermarket */
    private List<BoughtItem> addStartEnd(List<BoughtItem> boughtItemList) {
        String lastPlaceId = boughtItemList.get(0).getSupermarketPlaceId();
        String lastSupermarketName = boughtItemList.get(0).getSupermarketName();

        /* Add the very first start item and the very last end item */
        BoughtItem first = new BoughtItem(DAOHelper.START_ITEM, lastPlaceId, lastSupermarketName);
        BoughtItem last  = new BoughtItem(DAOHelper.END_ITEM, boughtItemList.get(boughtItemList.size()-1).getSupermarketPlaceId(), boughtItemList.get(boughtItemList.size()-1).getSupermarketName());
        boughtItemList.add(0, first);
        boughtItemList.add(boughtItemList.size(), last);

        for(int i = 0; i < boughtItemList.size(); i++) {
            BoughtItem current = boughtItemList.get(i);

            if(current == daoHelper.getStartBoughtItem() || current == daoHelper.getEndBoughtItem())
                continue;

            if(!current.getSupermarketPlaceId().equals(lastPlaceId)) {
                BoughtItem startItem = new BoughtItem(DAOHelper.START_ITEM, current.getSupermarketPlaceId(), current.getSupermarketName());
                BoughtItem endItem   = new BoughtItem(DAOHelper.END_ITEM, lastPlaceId, lastSupermarketName);
                boughtItemList.add(i, startItem);
                boughtItemList.add(i, endItem);

                lastPlaceId = current.getSupermarketPlaceId();
                lastSupermarketName = current.getSupermarketName();
            }
        }

        /*for(BoughtItem item : boughtItemList) {
            System.out.println(item.getName() + " - (" + item.getSupermarketName() + ")");
        }*/

        return boughtItemList;

    }

    public void addBoughtItems(List<BoughtItem> newBoughtItems) {

        List<BoughtItem> boughtItems = new ArrayList<BoughtItem>(newBoughtItems);

        /* Add start and end Items for every Supermarket */
        boughtItems = addStartEnd(boughtItems);

        /* Save all new boughtItems (vertices) */
        for(BoughtItem boughtItem: boughtItems) {
            if(daoHelper.getBoughtItemByName(boughtItem.getName()) == null)
                daoHelper.createBoughtItem(boughtItem);
        }

        /* Save all new edges */
        for(int i = 0; i < boughtItems.size()-1; i++) {
            /* BoughtItems need to be loaded from the DB, otherwise Hibernate complains about unsaved objects */
            BoughtItem i1 = daoHelper.getBoughtItemByName(boughtItems.get(i).getName());
            BoughtItem i2 = daoHelper.getBoughtItemByName(boughtItems.get(i + 1).getName());

            /* Continue if the Items are not from the same Supermarket. Here we have to use the parameter boughtItems because the placeId is not stored in the DB */
            if(!boughtItems.get(i).getSupermarketPlaceId().equals(boughtItems.get(i + 1).getSupermarketPlaceId())) {
                continue;
            }

            /* Load / create the Supermarket */
            setSupermarket(boughtItems.get(i).getSupermarketPlaceId(), boughtItems.get(i).getSupermarketName());

            createOrUpdateEdge(i1, i2, supermarket);

            /* If this supermarkt belongs to a chain, apply the Edge to this chain's global graph */
            if(supermarket.getSupermarketChain() != null) {
                Supermarket globalSupermarket = daoHelper.getGlobalSupermarketBySupermarketChain(supermarket.getSupermarketChain());
                createOrUpdateEdge(i1, i2, globalSupermarket);
            }
        }

        update();

    }

    public ShoppingListServer sort(Algorithm algorithm, ShoppingListServer shoppingList, SortingRequest sortingRequest) {

        /* setSupermarket() returns true if this supermarket is new - in this case, try to use the SupermarketChain's ItemGraph */
        if(setSupermarket(sortingRequest.getPlaceId(), sortingRequest.getSupermarketName()) == true) {
            this.supermarket = daoHelper.getGlobalSupermarket(supermarket.getSupermarketChain());

            /* If the Supermarket is new and does not belong to a chain, we can't sort the ShoppingList - just return the ShoppingList in this case */
            if(supermarket == null)
                return shoppingList;

            System.out.println("Using global ItemGraph for SupermarketChain " + supermarket.getSupermarketChain().getName());
        }

        /* Load the ItemGraph and sort the ShoppingList */
        update();
        algorithm.setUp(this);
        return (ShoppingListServer) algorithm.execute(shoppingList);

    }

    public void executeAlgorithm(Algorithm algorithm, List<BoughtItem> boughtItemList) {
        update();
        algorithm.setUp(this);
        algorithm.execute(boughtItemList);
    }

    public List<BoughtItem> getParents(BoughtItem child) {
        List<BoughtItem> parents = new ArrayList<BoughtItem>();

        for(Edge edge: edges) {
            if(edge.getTo() == child)
                parents.add(edge.getFrom());
        }

        return parents;
    }

    public List<BoughtItem> getChildren(BoughtItem parent) {
        List<BoughtItem> children = new ArrayList<BoughtItem>();

        for(Edge edge: edges) {
            if(edge.getFrom() == parent)
                children.add(edge.getTo());
        }

        return children;
    }

    public List<BoughtItem> getSiblings(BoughtItem child) {
        List<BoughtItem> siblings = new ArrayList<BoughtItem>();

        for(BoughtItem parent: getParents(child)) {
            siblings.addAll(getChildren(parent));
        }

        /* Remove all occurrences of this child */
        while(siblings.remove(child)) {}

        return siblings;
    }

    public Set<Edge> getEdgesFrom(BoughtItem boughtItem) {
        Set<Edge> edges = new HashSet<Edge>();

        for(Edge edge: getEdges()) {
            if(edge.getFrom() == boughtItem)
                edges.add(edge);
        }

        return edges;
    }

    //checks if there is an edge from node from to node to
    public boolean edgeFromToExists(BoughtItem from, BoughtItem to){
        if(this.getEdgesFrom(from).contains(to)) return true;

        return false;
    }

}
