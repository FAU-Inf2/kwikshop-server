package de.fau.cs.mad.kwikshop.server.sorting;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import de.fau.cs.mad.kwikshop.common.sorting.BoughtItem;
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

    public ItemGraph(String supermarketPlaceId,
                     BoughtItemDAO boughtItemDAO, EdgeDAO edgeDAO,
                     SupermarketDAO supermarketDAO, SupermarketChainDAO supermarketChainDAO) {

        this.boughtItemDAO = boughtItemDAO;
        this.edgeDAO = edgeDAO;
        this.supermarketDAO = supermarketDAO;
        this.supermarketChainDAO = supermarketChainDAO;

        this.supermarket = supermarketDAO.getByPlaceId(supermarketPlaceId);
        if(supermarket == null) {
            supermarket = new Supermarket(supermarketPlaceId);
            supermarketDAO.createSupermarkt(supermarket);
        }

        /* Load Edges */
        List<Edge> edgeList = edgeDAO.getBySupermarket(supermarket);
        setEdges(new HashSet<>(edgeList));

        /* Load Vertices */
        vertices = new HashSet<>();
        for(Edge edge : edgeList) {
            if(!vertices.contains(edge.getFrom()))
                vertices.add(edge.getFrom());

            if(!vertices.contains(edge.getTo()))
                vertices.add(edge.getTo());
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

    public void addBoughtItems(List<BoughtItem> boughtItems) {
        /* Save all new boughtItems (vertices) */
        for(BoughtItem boughtItem: boughtItems) {
            if(boughtItemDAO.getByName(boughtItem.getName()) == null)
                boughtItemDAO.createBoughtItem(boughtItem);
        }

        /* Save all new edges */
        for(int i = 0; i < boughtItems.size()-1; i++) {
            BoughtItem i1 = boughtItemDAO.getByName(boughtItems.get(i).getName());
            BoughtItem i2 = boughtItemDAO.getByName(boughtItems.get(i+1).getName());

            Edge edge = edgeDAO.getByFromTo(i1, i2);

            if(edge == null) {
                edgeDAO.createEdge(new Edge(i1, i2, supermarket));
            } else {
                edge.setWeight(edge.getWeight()+1);
            }
        }
    }
}
