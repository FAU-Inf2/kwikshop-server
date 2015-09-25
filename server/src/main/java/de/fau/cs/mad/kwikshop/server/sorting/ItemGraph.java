package de.fau.cs.mad.kwikshop.server.sorting;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.concurrent.locks.ReentrantLock;

import de.fau.cs.mad.kwikshop.common.ArgumentNullException;
import de.fau.cs.mad.kwikshop.common.ShoppingListServer;
import de.fau.cs.mad.kwikshop.common.sorting.BoughtItem;
import de.fau.cs.mad.kwikshop.common.sorting.SortingRequest;

public class ItemGraph {

    /*package visible*/ interface ItemGraphFactory {
        ItemGraph createItemGraph(DAOHelper daoHelper, Supermarket supermarket);
    }

    public static boolean printDebugOutput = true;

    private final DAOHelper daoHelper;
    private final Supermarket supermarket;

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

    protected ItemGraph(DAOHelper daoHelper, Supermarket supermarket) {
        this.daoHelper = daoHelper;
        this.supermarket = supermarket;
    }

    private static ItemGraph getItemGraph(DAOHelper daoHelper, Supermarket supermarket) {
        ItemGraphFactory itemGraphFactory = new ItemGraphFactory() {
            @Override
            public ItemGraph createItemGraph(DAOHelper daoHelper, Supermarket supermarket) {
                return new ItemGraph(daoHelper, supermarket);
            }
        };

        return daoHelper.getItemGraphForSupermarket(supermarket, itemGraphFactory);
    }

    public static ItemGraph getItemGraph(DAOHelper daoHelper, String supermarketPlaceId, String supermarketName) {
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
            for (Vertex vertex : vertices.values()) {
                items.add(vertex.getBoughtItem());
            }
        }
        return items;
    }

    /*package visible*/ Vertex getVertexForBoughtItem(BoughtItem item) {
        Vertex foundVertex;
        synchronized (vertices) {
            foundVertex = vertices.get(item);
            if (foundVertex == null) {
                synchronized (daoHelper) {
                    BoughtItem itemFromDatabase = daoHelper.getBoughtItemByName(item.getName());
                    if (itemFromDatabase == null && !item.isServerInternalItem()) {
                        daoHelper.createBoughtItem(item);
                        itemFromDatabase = daoHelper.getBoughtItemByName(item.getName());
                        foundVertex = new Vertex(itemFromDatabase, this);
                        vertices.put(itemFromDatabase, foundVertex);
                    } else if (itemFromDatabase == null && item.isServerInternalItem()) {
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
                    } else { // itemFromDatabase != null
                        foundVertex = new Vertex(itemFromDatabase, this);
                        vertices.put(itemFromDatabase, foundVertex);
                    }
                }
            }
        }
        return foundVertex;
    }

    public Set<Edge> getEdges() {
        Set<Edge> edges = new HashSet<>(); // size is not known at the moment
        synchronized (vertices) {
            for (Vertex vertex : vertices.values()) {
                edges.addAll(vertex.getEdges());
            }
        }
        return edges;
    }

    public List<BoughtItem> getParents(BoughtItem child) {
        Set<Vertex> parentVertices;
        synchronized (vertices) {
            if (!vertices.containsKey(child)) {
                return null; //child not contained in this item graph
            }
            Vertex vertex = getVertexForBoughtItem(child);
            parentVertices = vertex.getParents();
        }
        List<BoughtItem> parents = new ArrayList<>(parentVertices.size());
        for (Vertex vertex : parentVertices) {
            parents.add(vertex.getBoughtItem());
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
            if (edge.getDistance() == 0) {
                children.add(edge.getTo());
            }
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
                return new HashSet<>();
            }
            edges=vertex.getEdges();
        }
        return new HashSet<>(edges);
    }

    public Set<Edge> getDirectEdgesFrom(BoughtItem boughtItem) {
        Set<Edge> edges = getEdgesFrom(boughtItem);
        HashSet<Edge> foundEdges = new HashSet<>();
        for (Edge edge : edges) {
            if (edge.getDistance() == 0) {
                foundEdges.add(edge);
            }
        }
        return foundEdges;
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

    public void addBoughtItems(List<BoughtItem> newBoughtItems) {

        List<BoughtItem> boughtItems = new ArrayList<>(newBoughtItems);

        Set<Edge> edgesAddedThisTrip = new HashSet<>();
        Set<Edge> globalEdgesAddedThisTrip = new HashSet<>();

        /* Add start and end Items for every Supermarket */
        boughtItems = addStartEnd(boughtItems);

        /* Save all new boughtItems (vertices) */
        for(BoughtItem boughtItem: boughtItems) {
            synchronized (daoHelper) {
                BoughtItem itemFromDatabase = daoHelper.getBoughtItemByName(boughtItem.getName());
                if (itemFromDatabase == null && !boughtItem.isServerInternalItem()) {
                    daoHelper.createBoughtItem(boughtItem);
                    itemFromDatabase = daoHelper.getBoughtItemByName(boughtItem.getName());
                    if (!vertices.containsKey(itemFromDatabase)) {
                        Vertex vertex = new Vertex(itemFromDatabase, this);
                        vertices.put(itemFromDatabase, vertex); // if vertex is already contained, no changes are made
                    }
                }
            }
        }

        Supermarket supermarket = getSupermarket(); // retrieve supermarket thread-safely, as it cannot change anyways

        List<BoughtItem> foreignSupermarketItems = new ArrayList<>();
        List<BoughtItem> thisSupermarketItems = new ArrayList<>(boughtItems.size());

        for(BoughtItem boughtItem : boughtItems) {
            if (boughtItem.getSupermarketPlaceId().equals(this.supermarket.getPlaceId()) || boughtItem.isServerInternalItem()) {
                thisSupermarketItems.add(boughtItem);
            } else {
                foreignSupermarketItems.add(boughtItem);
            }
        }

        /* Save all new edges for the supermarket of this item graph */
        for(int i = 0; i < thisSupermarketItems.size()-1; i++) {
            /* BoughtItems need to be loaded from the DB, otherwise Hibernate complains about unsaved objects */
            BoughtItem i1 = daoHelper.getBoughtItemByName(boughtItems.get(i).getName());
            if(i1 == null) {
                i1 = daoHelper.getBoughtItemByNameIncludingStartAndEnd(boughtItems.get(i).getName());
            }

            BoughtItem i2 = daoHelper.getBoughtItemByName(boughtItems.get(i + 1).getName());
            if(i2 == null) {
                i2 = daoHelper.getBoughtItemByNameIncludingStartAndEnd(boughtItems.get(i + 1).getName());
            }

            if (i == 0) {
                i1 = daoHelper.getStartBoughtItem();
            } else if (i + 1 == boughtItems.size() - 1) {
                i2 = daoHelper.getEndBoughtItem();
            }

            /* Continue if the Items are not from the same Supermarket. Here we have to use the parameter boughtItems because the placeId is not stored in the DB */
            /* Items belong to the same supermarket, or they would not be in this list */
            /*if(!boughtItems.get(i).getSupermarketPlaceId().equals(boughtItems.get(i + 1).getSupermarketPlaceId())) {
                continue;
            }*/

            /* Load / create the Supermarket */
            // The supermarket is already set up, as item graphs can no longer change their supermarket
            //setSupermarket(boughtItems.get(i).getSupermarketPlaceId(), boughtItems.get(i).getSupermarketName());

            Edge currentEdge = createOrUpdateEdge(i1, i2, supermarket, edgesAddedThisTrip);
            edgesAddedThisTrip.add(currentEdge);

            /* If this supermarket belongs to a chain, apply the Edge to this chain's global graph */
            if(supermarket.getSupermarketChain() != null) {
                Supermarket globalSupermarket = daoHelper.getGlobalSupermarketBySupermarketChain(supermarket.getSupermarketChain());


                Edge currentGlobalEdge = createOrUpdateEdge(i1, i2, globalSupermarket, globalEdgesAddedThisTrip);
                globalEdgesAddedThisTrip.add(currentGlobalEdge);
            }
        }

        update();
    }

    public void update() {
        wrappedUpdate(false);
    }

    public void updateGlobalItemGraph() {
        wrappedUpdate(true);
    }

    private void wrappedUpdate(boolean isGlobal) {

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

    private void setVerticesAndEdges(Collection<BoughtItem> vertices, Collection<Edge> edges) {
        synchronized (vertices) {
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
    private void copyDataFromItemGraph(Supermarket supermarket) {
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

    /* Adds the start and end Items for each Supermarket */
    private List<BoughtItem> addStartEnd(List<BoughtItem> boughtItemList) {
        String lastPlaceId = boughtItemList.get(0).getSupermarketPlaceId();
        String lastSupermarketName = boughtItemList.get(0).getSupermarketName();
        long lastTime = 0;

        /* Add the very first start item and the very last end item */
        BoughtItem first = new BoughtItem(DAOHelper.START_ITEM, lastPlaceId, lastSupermarketName);
        first.setServerInternalItem(true);
        BoughtItem last  = new BoughtItem(DAOHelper.END_ITEM, boughtItemList.get(boughtItemList.size()-1).getSupermarketPlaceId(), boughtItemList.get(boughtItemList.size()-1).getSupermarketName());
        last.setServerInternalItem(true);
        boughtItemList.add(0, first);
        boughtItemList.add(boughtItemList.size(), last);

        for(int i = 0; i < boughtItemList.size(); i++) {
            BoughtItem current = boughtItemList.get(i);

            if(current.equals(daoHelper.getStartBoughtItem()) || current.equals(daoHelper.getEndBoughtItem()))
                continue;

            if(!current.getSupermarketPlaceId().equals(lastPlaceId) || current.getDate() != null) {
                if(current.getDate().getTime() - lastTime < 3 * 3600000) {
                     continue;
                }
                BoughtItem startItem = new BoughtItem(DAOHelper.START_ITEM, current.getSupermarketPlaceId(), current.getSupermarketName());
                startItem.setServerInternalItem(true);
                BoughtItem endItem   = new BoughtItem(DAOHelper.END_ITEM, lastPlaceId, lastSupermarketName);
                endItem.setServerInternalItem(true);
                boughtItemList.add(i, startItem);
                boughtItemList.add(i, endItem);

                lastPlaceId = current.getSupermarketPlaceId();
                lastSupermarketName = current.getSupermarketName();

                if(current.getDate() != null) {
                    lastTime = current.getDate().getTime();
                }
            }
        }

        if (printDebugOutput) {
            for (BoughtItem item : boughtItemList) {
                System.out.println(item.getName() + " - (" + item.getSupermarketName() + " at " + (item.getDate() != null ? item.getDate().toString() : "?") + ")");
            }
        }

        return boughtItemList;

    }

    /* Create or update an Edge for the given combination of BoughtItems and Supermarket */
    public Edge createOrUpdateEdge(BoughtItem i1, BoughtItem i2, Supermarket supermarket) {
        Set<Edge> edgesAddedThisTrip = new HashSet<>();
        return createOrUpdateEdge(i1, i2, supermarket, edgesAddedThisTrip);
    }

    private Edge createOrUpdateEdge(BoughtItem i1, BoughtItem i2, Supermarket supermarket, Set<Edge> edgesAddedThisTrip) {

        ReentrantLock lock1, lock2;
        int id1 = i1.getId();
        int id2 = i2.getId();
        int numberOfLocks = daoHelper.getNumberOfLocks();
        if(id1 % numberOfLocks <= id2 % numberOfLocks) {
            /* the locks have a global order in which they have to be acquired, in order to prevent deadlocks
             * it doesn't matter if the two lock references hold the same object, as the lock can
             * be acquired multiple times by one thread without having to wait */
            lock1 = daoHelper.getLockWithNumber(id1 % numberOfLocks);
            lock2 = daoHelper.getLockWithNumber(id2 % numberOfLocks);
        } else {
            lock1 = daoHelper.getLockWithNumber(id2 % numberOfLocks);
            lock2 = daoHelper.getLockWithNumber(id1 % numberOfLocks);
        }

        Edge edge;

        try {
            lock1.lock();
            lock2.lock();
            /* Supermarket must be included because different supermarkets have different edges */
            edge = daoHelper.getEdgeByFromTo(i1, i2, supermarket);

            if(edge == null) {

            /* Check if there is an Edge in the opposite direction */
                edge = daoHelper.getEdgeByFromTo(i2, i1, supermarket);

                if(edge != null) {
                /* Edit existing edge - decrease weight */
                    //edge.setWeight(edge.getWeight()-1);

                    //decrease weight of all edges to direct parent nodes (minimum distance) of the first item (which comes after the second one in the graph)
                    for(BoughtItem parent : getParents(i1)) {

                        //only decrement if the parent node is connected with the other item
                        if (edgeFromToExists(i2, parent) || parent.equals(i2)) {
                            Edge edgeToParentNode;
                            if ((edgeToParentNode = daoHelper.getEdgeByFromTo(parent, i1, supermarket)) != null) {
                                edgeToParentNode.setWeight(edgeToParentNode.getWeight() - 1);

                            /* Create edge in the opposite direction */
                                if (edgeToParentNode.getWeight() <= 0) {

                                    Edge edge2;
                                    //delete all edges between the conflicting items
                                    for (BoughtItem betweenTheConflictingVertices : getVertices()) {
                                        if (daoHelper.getEdgeByFromTo(i2, betweenTheConflictingVertices, supermarket) != null
                                                && (edge2 = daoHelper.getEdgeByFromTo(betweenTheConflictingVertices, i1, supermarket)) != null) {
                                            if (betweenTheConflictingVertices.equals(i1) || betweenTheConflictingVertices.equals(i2))
                                                continue;
                                            boolean contains = false;
                                            for(Edge edgeFromThisTrip : edgesAddedThisTrip){
                                                if(edgeFromThisTrip.equals(edge2)) contains = true;
                                            }
                                            if (!contains) {
                                                //only delete edges if they were not added on this shopping list
                                                if (printDebugOutput) {
                                                    System.out.println("Deleted: " + edge2.getFrom().getName() + "->" + edge2.getTo().getName());
                                                }
                                                daoHelper.deleteEdge(edge2);
                                            }
                                        }
                                    }
                                    daoHelper.deleteEdge(daoHelper.getEdgeByFromTo(i2, i1, supermarket));
                                    daoHelper.createEdge(new Edge(i1, i2, supermarket));

                                    edge = daoHelper.getEdgeByFromTo(i1, i2, supermarket);
                                    Vertex vertex = getVertexForBoughtItem(i1);
                                    vertex.addEdge(edge);
                                    break;
                                }
                            }
                        }
                    }


                } else {
                /* Create new edge */
                    daoHelper.createEdge(new Edge(i1, i2, supermarket));
                    edge = daoHelper.getEdgeByFromTo(i1, i2, supermarket);
                    Vertex vertex = getVertexForBoughtItem(i1);
                    vertex.addEdge(edge);
                }

            } else {
            /* Edit existing edge - increase weight */
                edge.setWeight(edge.getWeight() + 1);
            }

            if (printDebugOutput) {
                System.out.println("Calling insertIndirectEdges for node: " + i2.getName());
            }
            insertIndirectEdgesToAncestors(i2, i1, supermarket);
            insertIndirectEdgesToDescendantsForNode(i2, i1);
        } finally {
            lock2.unlock();
            lock1.unlock();
        }
        return edge;
    }

    private void insertIndirectEdgesToDescendantsForNode(BoughtItem currentNode, BoughtItem parentNode){

        assert daoHelper.getLockWithNumber(currentNode.getId() % daoHelper.getNumberOfLocks()).isHeldByCurrentThread();
        assert daoHelper.getLockWithNumber(parentNode.getId() % daoHelper.getNumberOfLocks()).isHeldByCurrentThread();

        for(Edge fromCurrentNode : getEdgesFrom(currentNode)){
            Edge currentEdge;
            if((currentEdge = daoHelper.getEdgeByFromTo(parentNode, fromCurrentNode.getTo(), supermarket)) != null){
                //edge to parent already exists
                if(currentEdge.getDistance() > fromCurrentNode.getDistance() +1){
                    //update distance if its shorter
                    currentEdge.setDistance(fromCurrentNode.getDistance() +1);
                }
            }else if((currentEdge = daoHelper.getEdgeByFromTo(fromCurrentNode.getTo(), parentNode, supermarket)) != null){
                //edge exists in the opposite direction
            }else{
                if(!parentNode.equals(fromCurrentNode.getTo())) {
                    Edge toBeAdded = new Edge(parentNode, fromCurrentNode.getTo(), supermarket);
                    toBeAdded.setDistance(fromCurrentNode.getDistance() + 1);
                    daoHelper.createEdge(toBeAdded);
                    Vertex vertex = getVertexForBoughtItem(parentNode);
                    vertex.addEdge(toBeAdded);
                }
            }

            for(Edge toParentNode : daoHelper.getEdgesByTo(currentNode, supermarket)){
                if((currentEdge = daoHelper.getEdgeByFromTo(toParentNode.getFrom(), fromCurrentNode.getTo(), supermarket)) != null) {
                    //edge already exists
                    if (currentEdge.getDistance() > toParentNode.getDistance() + fromCurrentNode.getDistance() + 1) {
                        //update distance if its shorter than the existing one
                        currentEdge.setDistance(toParentNode.getDistance() + fromCurrentNode.getDistance() + 1);
                    }
                }else if((currentEdge = daoHelper.getEdgeByFromTo(fromCurrentNode.getTo(), toParentNode.getFrom(), supermarket)) != null){
                    //edge exists in the opposite direction

                }else{
                    //edge does not exist already, so create it if from != to
                    if(!toParentNode.getFrom().equals(fromCurrentNode.getTo())) {
                        Edge toBeAdded = new Edge(toParentNode.getFrom(), fromCurrentNode.getTo(), supermarket);
                        toBeAdded.setDistance(toParentNode.getDistance() + fromCurrentNode.getDistance() + 1);
                        daoHelper.createEdge(toBeAdded);
                        Vertex vertex = getVertexForBoughtItem(parentNode);
                        vertex.addEdge(toBeAdded);
                    }
                }
            }
        }
    }

    /* Connects a BoughtItem currentNode with all ancestors of his parent and sets distance
     if it's lower than the existing distance for a given Supermarket*/
    private void insertIndirectEdgesToAncestors(BoughtItem currentNode, BoughtItem parent, Supermarket supermarket) {

        assert daoHelper.getLockWithNumber(currentNode.getId() % daoHelper.getNumberOfLocks()).isHeldByCurrentThread();
        assert daoHelper.getLockWithNumber(parent.getId() % daoHelper.getNumberOfLocks()).isHeldByCurrentThread();

        for (Edge edgeToParent : daoHelper.getEdgesByTo(parent, supermarket)) {
            BoughtItem ancestor = edgeToParent.getFrom();
            if (printDebugOutput) {
                System.out.println("Ancestor found: " + ancestor.getName());
            }
            Edge existingEdge;
            if ((existingEdge = daoHelper.getEdgeByFromTo(ancestor, currentNode, supermarket)) != null) {
                //update distance
                if (printDebugOutput) {
                    System.out.println("Existing edge found: " + ancestor.getName() + "->" + currentNode.getName());
                }
                existingEdge.setWeight(existingEdge.getWeight() + 1);
                if (existingEdge.getDistance() > edgeToParent.getDistance() + 1) existingEdge.setDistance(edgeToParent.getDistance() + 1);

            } else if(daoHelper.getEdgeByFromTo(currentNode, ancestor, supermarket) != null) {
                //edge already exists in the opposite direction
            }else {
                //new Edge
                if (ancestor != currentNode) {
                    if (printDebugOutput) {
                        System.out.println("Created new indirect edge: " + ancestor.getName() + " -> " + currentNode.getName());
                    }
                    existingEdge = new Edge(ancestor, currentNode, supermarket);
                    existingEdge.setDistance(edgeToParent.getDistance() + 1);
                    daoHelper.createEdge(existingEdge);
                    Vertex vertex = getVertexForBoughtItem(ancestor);
                    vertex.addEdge(existingEdge);
                }
            }
        }
    }

    public ShoppingListServer sort(Algorithm algorithm, ShoppingListServer shoppingList, SortingRequest sortingRequest) {

        // The global data should already be copied in the call to update()
        /* setSupermarket() returns true if this supermarket is new - in this case, try to use the SupermarketChain's ItemGraph */
//        if(setSupermarket(sortingRequest.getPlaceId(), sortingRequest.getSupermarketName()) == true) {
//            Supermarket globalSupermarket = daoHelper.getGlobalSupermarket(supermarket.getSupermarketChain());
//
//            /* If the Supermarket is new and does not belong to a chain, we can't sort the ShoppingList - just return the ShoppingList in this case */
//            if(globalSupermarket == null)
//                return shoppingList;
//            else {
//                /* Copy all Vertices and Edges from the global ItemGraph to this ItemGraph */
//                copyDataFromItemGraph(globalSupermarket);
//            }
//            if (printDebugOutput) {
//                System.out.println("Using global ItemGraph for SupermarketChain " + supermarket.getSupermarketChain().getName());
//            }
//        }

        /* Load the ItemGraph and sort the ShoppingList */
        update();
        algorithm.setUp(this);
        return (ShoppingListServer) algorithm.execute(shoppingList);

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
            stringBuilder.append(String.format("%s -> %s [label=\"%s\"]\n", edge.getFrom().getName(), edge.getTo().getName(), String.valueOf(Math.round(currentWeightDistanceRatio*1000.0)/1000.0)));
        }
        stringBuilder.append("}\n");
        return stringBuilder.toString();
    }
}
