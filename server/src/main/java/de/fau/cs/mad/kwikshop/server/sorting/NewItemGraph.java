package de.fau.cs.mad.kwikshop.server.sorting;

import java.lang.ref.SoftReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import de.fau.cs.mad.kwikshop.common.ArgumentNullException;
import de.fau.cs.mad.kwikshop.common.sorting.BoughtItem;

public class NewItemGraph {

    private final DAOHelper daoHelper;
    private final Supermarket supermarket;

    private final Set<Vertex> vertices = new HashSet<>();

    private final static HashMap<String, SoftReference<NewItemGraph>> itemGraphCache = new HashMap<>();

    private NewItemGraph(DAOHelper daoHelper, Supermarket supermarket) {
        this.daoHelper = daoHelper;
        this.supermarket = supermarket;
    }

    public static NewItemGraph getItemGraph(DAOHelper daoHelper, Supermarket supermarket) {
        if(supermarket == null) {
            throw new ArgumentNullException("supermarket");
        }
        NewItemGraph itemGraph = null;
        synchronized (supermarket) {
            // make sure only one item graph per supermarket is created
            SoftReference<NewItemGraph> reference = itemGraphCache.get(supermarket.getPlaceId());
            if (reference != null) {
                itemGraph = reference.get();
            }
            if (itemGraph == null) {
                itemGraph = new NewItemGraph(daoHelper, supermarket);
                itemGraphCache.put(supermarket.getPlaceId(), new SoftReference<>(itemGraph));
            }
        }
        return itemGraph;
    }

    public static NewItemGraph getItemGraph(DAOHelper daoHelper, String supermarketPlaceId, String supermarketName) {
        // TODO: daoHelper.getSupermarketByPlaceId and daoHelper.createSupermarket needs to be synchronized
        boolean isNewSupermarket = false;
        Supermarket supermarket = daoHelper.getSupermarketByPlaceID(supermarketPlaceId);

        /* Supermarket does not exist yet, create it and try to find a matching SupermarketChain */
        if (supermarket == null) {
            isNewSupermarket = true;
            supermarket = new Supermarket(supermarketPlaceId);

            for (SupermarketChain supermarketChain : daoHelper.getAllSupermarketChains()) {
                /* If the supermarket's name contains the name of a chain, it (most likely) belongs to that chain */
                if (supermarketName.toLowerCase().contains(supermarketChain.getName().toLowerCase())) {
                    supermarket.setSupermarketChain(supermarketChain);
                    break;
                }
            }
            daoHelper.createSupermarket(supermarket);
        }
        return getItemGraph(daoHelper, supermarket);
    }

    public Supermarket getSupermarket() {
        return supermarket;
    }

    public DAOHelper getDaoHelper() {
        return daoHelper;
    }

    public Set<BoughtItem> getVertices() {
        Set<BoughtItem> items;
        synchronized (vertices) {
            items = new HashSet<>(vertices.size());
            for (Vertex vertex : vertices) {
                items.add(vertex.getBoughtItem());
            }
        }
        return items;
    }

    private Vertex getVertexForBoughtItem(BoughtItem item) {
        synchronized (vertices) {
            for (Vertex v : vertices) {
                if (v.getBoughtItem().equals(item)) {
                    return v;
                }
            }
        }
        return null;
    }

    public Set<Edge> getEdges() {
        Set<Edge> edges = new HashSet<>(); // size is not known at the moment
        synchronized (vertices) {
            for (Vertex vertex : vertices) {
                edges.addAll(vertex.getEdges());
            }
        }
        return edges;
    }

    public List<BoughtItem> getParents(BoughtItem child) {
        List<BoughtItem> parents = new ArrayList<>();
        synchronized (vertices) {
            // this is probably not slower than iterating over all vertices and all descendants of all vertices
            for(Edge edge: daoHelper.getEdgesByTo(child, supermarket)) {
                if(edge.getTo().equals(child) && edge.getDistance() == 0)
                    parents.add(edge.getFrom());
            }
        }
        return parents;
    }

    public List<BoughtItem> getChildren(BoughtItem parent) {
        List<Edge> edges;
        synchronized (vertices) {
            Vertex vertex = getVertexForBoughtItem(parent);
            if (vertex == null) {
                return null; //parent not contained in this item graph
            }
            edges = vertex.getEdges();
        }
        List<BoughtItem> children = new ArrayList<>(edges.size());
        for (Edge edge : edges) {
            children.add(edge.getTo());
        }
        return children;

    }

    public List<BoughtItem> getSiblings(BoughtItem child) {
        List<BoughtItem> siblings = new ArrayList<>();
        synchronized (vertices) {
            for (BoughtItem parent : getParents(child)) {
                siblings.addAll(getChildren(parent));
            }
        }
        /* Remove all occurrences of this child */
        while(siblings.remove(child)) {}

        return siblings;
    }

    public Set<Edge> getEdgesFrom(BoughtItem boughtItem) {
        List<Edge> edges;
        synchronized (vertices) {
            Vertex vertex = getVertexForBoughtItem(boughtItem);
            if (vertex == null) {
                return null; // item not contained in this item graph
            }
            edges=vertex.getEdges();
        }
        return new HashSet<>(edges);
    }

    //checks if there is an edge from node from to node to
    public boolean edgeFromToExists(BoughtItem from, BoughtItem to){
        Set<Edge> edges = getEdgesFrom(from);
        for(Edge edge : edges) {
            if(edge.getTo().equals(to))
                return true;
        }
        return false;
    }

    @Override
    public String toString() {
        /* Debug output */
        StringBuilder stringBuilder = new StringBuilder();
        Set<BoughtItem> vertices;
        Set<Edge> edges;
        synchronized (this.vertices) {
            vertices = getVertices();
            edges = getEdges();
        }
        stringBuilder.append(String.format("ItemGraph refreshed, containing %s Edges and %s vertices.\n", edges.size(), vertices.size()));
        stringBuilder.append("Vertices:\n");
        for(BoughtItem boughtItem : vertices) {
            stringBuilder.append(boughtItem.getName());
            stringBuilder.append('\n');
        }
        stringBuilder.append("Edges:\n\ndigraph G {\n");
        for(Edge edge: edges) {
            double currentWeightDistanceRatio = ((double)edge.getWeight()+1) / ((double)edge.getDistance()+1);
            stringBuilder.append(String.format("%s -> %s [label=\"%s\"]", edge.getFrom().getName(), edge.getTo().getName(), String.valueOf(Math.round(currentWeightDistanceRatio*1000.0)/1000.0)));
        }
        stringBuilder.append("}\n\n");
        return stringBuilder.toString();
    }
}
