package de.fau.cs.mad.kwikshop.server.sorting;

import java.lang.ref.SoftReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.locks.ReentrantLock;

import de.fau.cs.mad.kwikshop.common.ArgumentNullException;
import de.fau.cs.mad.kwikshop.common.sorting.BoughtItem;

public class NewItemGraph {

    private final DAOHelper daoHelper;
    private final Supermarket supermarket;

    private final Set<Vertex> vertices = new HashSet<>();

    private final static HashMap<String, SoftReference<NewItemGraph>> itemGraphCache = new HashMap<>();

    private final static ReentrantLock[] locks = new ReentrantLock[1000];

    static {
        for (int i = 0; i < locks.length; i++) {
            locks[i] = new ReentrantLock();
        }
    }

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

    public void addBoughtItems(List<BoughtItem> newBoughtItems) {

        List<BoughtItem> boughtItems = new ArrayList<>(newBoughtItems);

        Set<Edge> edgesAddedThisTrip = new HashSet<>();

        /* Add start and end Items for every Supermarket */
        boughtItems = addStartEnd(boughtItems);

        /* Save all new boughtItems (vertices) */
        for(BoughtItem boughtItem: boughtItems) {
            synchronized (daoHelper) {
                if (daoHelper.getBoughtItemByName(boughtItem.getName()) == null && !boughtItem.isServerInternalItem())
                    daoHelper.createBoughtItem(boughtItem);
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
            BoughtItem i2 = daoHelper.getBoughtItemByName(boughtItems.get(i + 1).getName());

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

            Edge currentEdge = createOrUpdateEdge(i1, i2, supermarket);
            edgesAddedThisTrip.add(currentEdge);

            /* If this supermarket belongs to a chain, apply the Edge to this chain's global graph */
            if(supermarket.getSupermarketChain() != null) {
                Supermarket globalSupermarket = daoHelper.getGlobalSupermarketBySupermarketChain(supermarket.getSupermarketChain());
                createOrUpdateEdge(i1, i2, globalSupermarket);
            }
        }

        update();

    }

    /* Adds the start and end Items for each Supermarket */
    private List<BoughtItem> addStartEnd(List<BoughtItem> boughtItemList) {
        String lastPlaceId = boughtItemList.get(0).getSupermarketPlaceId();
        String lastSupermarketName = boughtItemList.get(0).getSupermarketName();

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

            if(!current.getSupermarketPlaceId().equals(lastPlaceId)) {
                BoughtItem startItem = new BoughtItem(DAOHelper.START_ITEM, current.getSupermarketPlaceId(), current.getSupermarketName());
                startItem.setServerInternalItem(true);
                BoughtItem endItem   = new BoughtItem(DAOHelper.END_ITEM, lastPlaceId, lastSupermarketName);
                endItem.setServerInternalItem(true);
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

    private Edge createOrUpdateEdge(BoughtItem i1, BoughtItem i2, Supermarket supermarket, Set<Edge> edgesAddedThisTrip) {

        ReentrantLock lock1, lock2;
        int id1 = i1.getId();
        int id2 = i2.getId();
        int numberOfLocks = locks.length;
        if(id1 % numberOfLocks <= id2 % numberOfLocks) {
            /* the locks have a global order in which they have to be acquired, in order to prevent deadlocks
             * it doesn't matter if the two lock references hold the same object, as the lock can
             * be acquired multiple times by one thread without having to wait */
            lock1 = locks[id1 % numberOfLocks];
            lock2 = locks[id2 % numberOfLocks];
        } else {
            lock1 = locks[id2 % numberOfLocks];
            lock2 = locks[id1 % numberOfLocks];
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
                                    System.out.println("Starting to delete, conflict: " + edge.getFrom().getName() + "->" + edge.getTo().getName());

                                    Edge edge2;
                                    //delete all edges between the conflicting items
                                    for (BoughtItem betweenTheConflictingVertices : getVertices()) {
                                        if (daoHelper.getEdgeByFromTo(i2, betweenTheConflictingVertices, supermarket) != null
                                                && (edge2 = daoHelper.getEdgeByFromTo(betweenTheConflictingVertices, i1, supermarket)) != null) {
                                            if (betweenTheConflictingVertices.equals(i1) || betweenTheConflictingVertices.equals(i2))
                                                continue;
                                            System.out.println("Deleted: " + edge2.getFrom().getName() + "->" + edge2.getTo().getName());
                                            boolean contains = false;
                                            for(Edge edgeFromThisTrip : edgesAddedThisTrip){
                                                if(edgeFromThisTrip.equals(edge2)) contains = true;
                                            }
                                            if (!contains) {
                                                //only delete edges if they were not added on this shopping list
                                                daoHelper.deleteEdge(edge2);
                                            }
                                        }
                                    }
                                    daoHelper.deleteEdge(daoHelper.getEdgeByFromTo(i2, i1, supermarket));
                                    daoHelper.createEdge(new Edge(i1, i2, supermarket));
                                    edge = daoHelper.getEdgeByFromTo(i1, i2, supermarket);
                                    break;
                                }
                            }
                        }
                    }


                } else {
                /* Create new edge */
                    daoHelper.createEdge(new Edge(i1, i2, supermarket));
                    edge = daoHelper.getEdgeByFromTo(i1, i2, supermarket);
                }

            } else {
            /* Edit existing edge - increase weight */
                edge.setWeight(edge.getWeight() + 1);
            }

            System.out.println("Calling insertIndirectEdges for node: " + i2.getName());
            insertIndirectEdgesToAncestors(i2, i1, supermarket);
            insertIndirectEdgesToDescendantsForNode(i2, i1);
        } finally {
            lock2.unlock();
            lock1.unlock();
        }
        return edge;

    }

    /* Create or update an Edge for the given combination of BoughtItems and Supermarket */
    public Edge createOrUpdateEdge(BoughtItem i1, BoughtItem i2, Supermarket supermarket) {
        Set<Edge> edgesAddedThisTrip = new HashSet<>();
        return createOrUpdateEdge(i1, i2, supermarket, edgesAddedThisTrip);
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
