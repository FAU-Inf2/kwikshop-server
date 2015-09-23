package de.fau.cs.mad.kwikshop.server.sorting;

import java.lang.ref.SoftReference;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

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

    public Set<Edge> getEdges() {
        Set<Edge> edges = new HashSet<>(); // size is not known at the moment
        synchronized (vertices) {
            for (Vertex vertex : vertices) {
                edges.addAll(vertex.getEdges());
            }
        }
        return edges;
    }
}
