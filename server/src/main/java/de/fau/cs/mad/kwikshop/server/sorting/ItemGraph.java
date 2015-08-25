package de.fau.cs.mad.kwikshop.server.sorting;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import de.fau.cs.mad.kwikshop.common.sorting.BoughtItem;
import de.fau.cs.mad.kwikshop.common.sorting.ItemOrderWrapper;
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

    public ItemGraph(ItemOrderWrapper itemOrder,
                     BoughtItemDAO boughtItemDAO, EdgeDAO edgeDAO,
                     SupermarketDAO supermarketDAO, SupermarketChainDAO supermarketChainDAO) {

        this.boughtItemDAO = boughtItemDAO;
        this.edgeDAO = edgeDAO;
        this.supermarketDAO = supermarketDAO;
        this.supermarketChainDAO = supermarketChainDAO;

        this.supermarket = supermarketDAO.getByPlaceId(itemOrder.getSupermarketPlaceId());

        /* Supermarket does not exist yet, create it and try to find a matching SupermarketChain */
        if(supermarket == null) {
            supermarket = new Supermarket(itemOrder.getSupermarketPlaceId());

            for(SupermarketChain supermarketChain : supermarketChainDAO.getAll()) {
                /* If the supermarket's name contains the name of a chain, it (most likely) belongs to that chain */
                if(itemOrder.getSupermarketName().toLowerCase().contains(supermarketChain.getName().toLowerCase())) {
                    supermarket.setSupermarketChain(supermarketChain);
                    break;
                }
            }
            supermarketDAO.createSupermarkt(supermarket);
        }

        update();

    }

    private void update() {
        /* Load Edges */
        List<Edge> edgeList = edgeDAO.getBySupermarket(supermarket);
        if(edgeList != null)
            setEdges(new HashSet<Edge>(edgeList));
        else
            setEdges(new HashSet<Edge>());

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

    public Set<BoughtItem> getVertices() {
        return vertices;
    }

    public void setVertices(Set<BoughtItem> vertices) {
        this.vertices = vertices;
    }

    public Set<Edge> getEdges() {
        return edges;
    }

    public void setEdges(Set<Edge> edges) {
        this.edges = edges;
    }

    public void addVertice(BoughtItem vertice) {
        this.vertices.add(vertice);
    }

    public void addEdge(Edge edge) {
        this.edges.add(edge);
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

    public void addBoughtItems(List<BoughtItem> boughtItems) {

        /* Save all new boughtItems (vertices) */
        for(BoughtItem boughtItem: boughtItems) {
            if(boughtItemDAO.getByName(boughtItem.getName()) == null)
                boughtItemDAO.createBoughtItem(boughtItem);
        }

        /* Save all new edges */
        for(int i = 0; i < boughtItems.size()-1; i++) {
            /* BoughtItems need to be loaded from the db, otherwise Hibernate complains about unsaved objects */
            BoughtItem i1 = boughtItemDAO.getByName(boughtItems.get(i).getName());
            BoughtItem i2 = boughtItemDAO.getByName(boughtItems.get(i+1).getName());

            /* Specific supermarket */
            createOrUpdateEdge(i1, i2, supermarket);

            /* If this supermarkt belongs to a chain, apply the edges to this chains global list */
            if(supermarket.getSupermarketChain() != null) {
                Supermarket globalSupermarket = supermarketDAO.getGlobalBySupermarketChain(supermarket.getSupermarketChain());
                createOrUpdateEdge(i1, i2, globalSupermarket);
            }
        }

        update();

    }
}
