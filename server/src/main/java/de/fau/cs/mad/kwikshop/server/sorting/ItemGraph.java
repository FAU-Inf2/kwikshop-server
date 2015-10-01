package de.fau.cs.mad.kwikshop.server.sorting;


import java.util.Collection;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.TreeMap;

import de.fau.cs.mad.kwikshop.common.ArgumentNullException;
import de.fau.cs.mad.kwikshop.common.ShoppingListServer;
import de.fau.cs.mad.kwikshop.common.sorting.BoughtItem;
import de.fau.cs.mad.kwikshop.common.sorting.SortingRequest;

public class ItemGraph {

    public static boolean printDebugOutput = true; // debug output is printed when the item graph is used

    /*package visible*/ interface ItemGraphFactory {

        ItemGraph createItemGraph(DAOHelper daoHelper, Supermarket supermarket);

    }
    private final DAOHelper daoHelper;
    private final Supermarket supermarket;

    private final BoughtItem startBoughtItem;
    private final BoughtItem endBoughtItem;

    private LinkedList<BoughtItem> totallyOrderedItems = null;
    private boolean totallyOrderedItemsAreUpToDate;

    // the keys in this map should all be loaded from the DAOHelper before.
    // Otherwise Hibernate might complain about unsaved objects, if they are used
    private final TreeMap<BoughtItem, Vertex> vertices = new TreeMap<>(new Comparator<BoughtItem>() {
        @Override
        public int compare(BoughtItem i1, BoughtItem i2) {
            if (i1.isServerInternalItem() != i2.isServerInternalItem()) {
                return i1.isServerInternalItem() ? -1 : +1;
            }
            return i1.getName().compareTo(i2.getName());
        }
    });

    // private constructor, used when getItemGraph is called for a supermarket, where no item graph exists so far
    private ItemGraph(DAOHelper daoHelper, Supermarket supermarket) {
        this.daoHelper = daoHelper;
        this.supermarket = supermarket;
        this.startBoughtItem = daoHelper.getStartBoughtItem();
        this.endBoughtItem = daoHelper.getEndBoughtItem();
    }

    private static synchronized ItemGraph getItemGraph(DAOHelper daoHelper, Supermarket supermarket) {
        ItemGraphFactory itemGraphFactory = new ItemGraphFactory() {
            @Override
            public ItemGraph createItemGraph(DAOHelper daoHelper, Supermarket supermarket) {
                return new ItemGraph(daoHelper, supermarket);
            }
        };

        return daoHelper.getItemGraphForSupermarket(supermarket, itemGraphFactory);
    }

    public static synchronized ItemGraph getItemGraph(DAOHelper daoHelper, String supermarketPlaceId, String supermarketName) {
        Supermarket supermarket = daoHelper.getSupermarketByPlaceID(supermarketPlaceId);

        /* Supermarket does not exist yet, create it and try to find a matching SupermarketChain */
        if (supermarket == null) {
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

    // this getter is mainly for being able to generate ItemGraphs with the same DAOHelper
    public DAOHelper getDaoHelper() {
        return daoHelper;
    }

    public synchronized Set<BoughtItem> getVertices() {
        Set<BoughtItem> items;
        synchronized (vertices) {
            items = new HashSet<>(vertices.size());
            for (Vertex vertex : vertices.values()) {
                items.add(vertex.getBoughtItem());
            }
        }
        return items;
    }

    public synchronized Vertex getVertexForNameOrNull(String name) {
        if (name == null || name.isEmpty()) {
            return null;
        }
        BoughtItem boughtItem = new BoughtItem(name);
        synchronized (vertices) {
            return vertices.get(boughtItem);
        }

    }

    /* returns the stored vertex for the given bought item, or creates a new one, if no such vertex exists */
    /*package visible*/ synchronized Vertex getVertexForBoughtItem(BoughtItem item) {
        if (item == null) {
            throw new ArgumentNullException("item");
        }
        Vertex foundVertex;
        synchronized (vertices) {
            foundVertex = vertices.get(item);
            if (foundVertex != null) {
                return foundVertex;
            }

            synchronized (daoHelper) {
                BoughtItem itemFromDatabase = daoHelper.getBoughtItemByName(item.getName());
                if (itemFromDatabase == null) {
                    if (item.isServerInternalItem()) {
                        String itemName = item.getName();
                        BoughtItem start = daoHelper.getStartBoughtItem();
                        BoughtItem end = daoHelper.getEndBoughtItem();
                        if (itemName.equals(start.getName())) {
                            itemFromDatabase = start;
                        } else {
                            assert itemName.equals(end.getName());
                            itemFromDatabase = end;
                        }
                        foundVertex = new Vertex(itemFromDatabase, this);
                        vertices.put(itemFromDatabase, foundVertex);
                    } else {
                        daoHelper.createBoughtItem(item);
                        itemFromDatabase = daoHelper.getBoughtItemByName(item.getName());
                        foundVertex = new Vertex(itemFromDatabase, this);
                        vertices.put(itemFromDatabase, foundVertex);
                    }
                } else {
                    foundVertex = new Vertex(itemFromDatabase, this);
                    vertices.put(itemFromDatabase, foundVertex);
                }
            }
        }
        return foundVertex;
    }

    // slow!!
    // this method is currently used for copying data from an other item graph and for toString()
    public synchronized Set<Edge> getEdges() {
        Set<Edge> edges = new HashSet<>(); // size is not known at the moment
        synchronized (vertices) {
            for (Vertex vertex : vertices.values()) {
                edges.addAll(vertex.getEdges());
            }
        }
        return edges;
    }

    // quite slow!
    public void update() {
        wrappedUpdate(false);
    }

    // quite slow!
    public void updateGlobalItemGraph() {
        wrappedUpdate(true);
    }

    // quite slow!
    private synchronized void wrappedUpdate(boolean isGlobal) {

        /* Update() loads all Edges and Vertices of one specific supermarket -> supermarket may not be null */
        if(supermarket == null) {
            throw new ArgumentNullException("supermarket");
        }

        synchronized (vertices) {
            /* Cache Edges */
            List<Edge> edgeList = daoHelper.getEdgesBySupermarket(supermarket);
            Set<Edge> edges;
            if (edgeList != null)
                edges = new HashSet<>(edgeList);
            else
                edges = new HashSet<>();

            /* Load Vertices */
            Set<BoughtItem> vertices = new HashSet<>();
            for (Edge edge : edges) {
                if (!vertices.contains(edge.getFrom()))
                    vertices.add(edge.getFrom());

                if (!vertices.contains(edge.getTo()))
                    vertices.add(edge.getTo());
            }

            setVerticesAndEdges(vertices, edges);

            if (!isGlobal) {
            /* If there are no Vertices for this Supermarket but it does belong to a SupermarketChain, copy the data from this SupermarketChain */
                if (vertices.size() == 0 && supermarket.getSupermarketChain() != null) {
                    copyDataFromItemGraph(daoHelper.getGlobalSupermarket(supermarket.getSupermarketChain()));
                }
            }
        }
        /* Debug output */
        if (printDebugOutput) {
            System.out.println(this.toString());
        }
    }

    private synchronized void setVerticesAndEdges(Collection<BoughtItem> vertices, Collection<Edge> edges) {
        synchronized (this.vertices) {
            this.vertices.clear();
            for (BoughtItem item : vertices) {
                if (!this.vertices.containsKey(item)) {
                    this.vertices.put(item, new Vertex(item, this));
                }
            }

            for (Edge edge : edges) {
                BoughtItem from = edge.getFrom();
                Vertex vertex = getVertexForBoughtItem(from);
                vertex.addEdge(edge);
            }
        }
    }

    /* Create a new ItemGraph, load all Edges and Vertices from 'supermarket' and copy them to this ItemGraph */
    private synchronized void copyDataFromItemGraph(Supermarket supermarket) {
        if(supermarket == null)
            return;

        ItemGraph globalItemGraph = getItemGraph(this.daoHelper, supermarket);
        globalItemGraph.updateGlobalItemGraph();
        Set<Edge> edges = globalItemGraph.getEdges();
        setVerticesAndEdges(globalItemGraph.getVertices(), edges);
        for(Edge edge : edges) {
            daoHelper.createEdge(edge);
        }
        for(Vertex vertex : this.vertices.values()) {
            daoHelper.createBoughtItem(vertex.getBoughtItem());
        }
    }

    public synchronized void addBoughtItems(List<BoughtItem> newBoughtItems) {
        addBoughtItems(newBoughtItems, false);
    }

    private synchronized void addBoughtItems(List<BoughtItem> newBoughtItems, boolean isChainGlobalSupermarket) {
        assert !newBoughtItems.isEmpty() : "Trying to add an empty list";

        totallyOrderedItemsAreUpToDate = false;

        LinkedList<BoughtItem> boughtItems = new LinkedList<>(newBoughtItems); // copy the input
        boughtItems.addFirst(startBoughtItem);
        boughtItems.addLast(endBoughtItem);

        Vertex vertex1 = getVertexForBoughtItem(boughtItems.pollFirst());
        while (!vertex1.getBoughtItem().isServerInternalItem() && !itemNameIsAllowed(vertex1.getBoughtItem().getName())) {
            vertex1 = getVertexForBoughtItem(boughtItems.pollFirst());
        }
        BoughtItem item2 = boughtItems.pollFirst();


        while (item2 != null) {
            if (!item2.isServerInternalItem() && !itemNameIsAllowed(item2.getName())) {
                item2 = boughtItems.pollFirst();
                continue;
            }
            Vertex vertex2 = getVertexForBoughtItem(item2);
            vertex1.addEdgeOrIncreaseWeightTo(vertex2);

            vertex1 = vertex2;
            item2 = boughtItems.pollFirst();
        }
        updateTotallyOrderedItems();

        if (isChainGlobalSupermarket) {
            // this is already the call for the global graph, so no data has to be added any more
            return;
        }

        SupermarketChain supermarketChain = this.supermarket.getSupermarketChain();
        if (supermarketChain != null) {
            // this supermarket belongs to a chain
            ItemGraph globalItemGraph = getItemGraph(daoHelper, daoHelper.getGlobalSupermarket(supermarketChain));
            globalItemGraph.addBoughtItems(newBoughtItems, true);
        }
    }

    private synchronized boolean itemNameIsAllowed(String name) {
        // all "reserved keywords" should return false
        if (name.equals(startBoughtItem.getName())) {
            return false;
        }
        if (name.equals(endBoughtItem.getName())) {
            return false;
        }
        return true;
    }

    public synchronized boolean updateTotallyOrderedItems() {
        if (totallyOrderedItemsAreUpToDate) {
            // no updating has to be performed
            return false;
        }
        totallyOrderedItems = new LinkedList<>();
        //totallyOrderedItems.addFirst(startBoughtItem); // this is done in startVertex

        Vertex startVertex = getVertexForBoughtItem(startBoughtItem);
        startVertex.traverseGraphForENDAndAddItemsToList(totallyOrderedItems, endBoughtItem.getName());

        if (printDebugOutput) {
            System.out.println("Totally ordered items:");
            for (BoughtItem boughtItem : totallyOrderedItems) {
                System.out.println(boughtItem.getName());
            }
            System.out.println("----------------------");
        }

        totallyOrderedItemsAreUpToDate = true;
        return true;
    }

    public synchronized LinkedList<BoughtItem> getTotallyOrderedItems() {
        if (!totallyOrderedItemsAreUpToDate) {
            updateTotallyOrderedItems();
        }
        return new LinkedList<>(totallyOrderedItems);
    }

    public synchronized ShoppingListServer sort(MagicSort magicSort, ShoppingListServer shoppingList, SortingRequest sortingRequest) {
        magicSort.setUp(this);
        return magicSort.sort(shoppingList);
    }

    // This method is mainly for testing, as it is quite slow
    public synchronized boolean edgeFromToExists(BoughtItem from, BoughtItem to) {
        Vertex vertexFrom = vertices.get(from);
        if (vertexFrom == null) {
            return false;
        }
        List<Edge> edges = vertexFrom.getEdges();
        for (Edge edge : edges) {
            if (edge.getTo().equals(to)) {
                return true;
            }
        }
        return false;
    }

    // This method is mainly for testing
    public synchronized Set<Edge> getEdgesFrom(BoughtItem from) {
        Vertex vertexFrom = vertices.get(from);
        if (vertexFrom == null) {
            return null;
        }
        return new HashSet<>(vertexFrom.getEdges());
    }

    @Override
    public synchronized String toString() {
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
            stringBuilder.append(String.format("%s -> %s [label=\"%s\"]\n", edge.getFrom().getName(), edge.getTo().getName(), String.valueOf(Math.round(currentWeightDistanceRatio*1000.0)/1000.0)));
        }
        stringBuilder.append("}\n");
        return stringBuilder.toString();
    }
}
