package de.fau.cs.mad.kwikshop.server.sorting;

import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.PriorityQueue;
import java.util.SortedSet;
import java.util.TreeSet;

import de.fau.cs.mad.kwikshop.common.sorting.BoughtItem;

public class Vertex {
    private final BoughtItem boughtItem;
    private List<Edge> edges; // adjacency list of edges
    private final ItemGraph itemGraph; // the ItemGraph this vertex is contained in

    private int itemIsVisitedCount = 0;

    public Vertex(BoughtItem boughtItem, ItemGraph itemGraph) {
        this.boughtItem = boughtItem;
        this.edges = new LinkedList<>();
        this.itemGraph = itemGraph;
    }

    public BoughtItem getBoughtItem() {
        return boughtItem;
    }

    public synchronized void addEdgeOrIncreaseWeightTo(Vertex toVertex) {
        Edge edge = getEdgeToVertex(toVertex);
        if (edge != null) {
            // Edge already exists, so increase weight
            edge.incrementWeight();
        } else {
            // Edge does not exist yet
            // check whether edge exists in the other direction
            edge = toVertex.getEdgeToVertex(this);
            if (edge != null) {
                // edge already exists in the other direction
                // decrease weight and flip Edge, when necessary
                int newWeight = edge.decrementWeight();
                if (newWeight <= 0) {
                    // edge has to be flipped
                    toVertex.removeEdge(edge);
                    edge.setTo(toVertex.getBoughtItem());
                    edge.setFrom(this.getBoughtItem());
                    edge.setWeight(1);
                    this.addEdge(edge);
                }
            } else {
                // edge doesn't exist in either direction
                BoughtItem i1 = this.getBoughtItem(), i2 = toVertex.getBoughtItem();
                Supermarket supermarket = itemGraph.getSupermarket();
                DAOHelper daoHelper = itemGraph.getDaoHelper();
                daoHelper.createEdge(new Edge(i1, i2, supermarket));
                edge = daoHelper.getEdgeByFromTo(i1, i2, supermarket);
                this.addEdge(edge);
            }
        }
    }

    private Edge getEdgeToVertex(Vertex toVertex) {
        BoughtItem to = toVertex.getBoughtItem();
        for (Edge edge : edges) {
            if (edge.getTo().equals(to)) {
                return edge;
            }
        }
        return null;
    }

    // this method is also used in ItemGraph when copying all data from another graph
    /*package global*/ void addEdge(Edge edge) {
        assert !edges.contains(edge) : "Trying to add a edge that is already contained";

        this.edges.add(edge);
    }

    private boolean removeEdge(Edge edge) {
        return edges.remove(edge);
    }

    public List<Edge> getEdges() {
        return new LinkedList<>(edges);
    }

    public void traverseGraphForENDAndAddItemsToList(LinkedList<BoughtItem> totallyOrderedItems, String endName) {
        if (!totallyOrderedItems.contains(boughtItem)) {
            // mark item as traversed
            totallyOrderedItems.addLast(boughtItem);

            if (boughtItem.isServerInternalItem()) {
                if (boughtItem.getName().equals(endName)) {
                    // end found
                    itemIsVisitedCount = 0;
                    return;
                }
            }


        } //else {
            // this item is traversed a second time, because there was no better item reachable from the last item
            // do nothing
        //}


        PriorityQueue<Edge> edges = getSortedEdges();
        for (Edge edge : edges) {
            BoughtItem item = edge.getTo();
            if (totallyOrderedItems.contains(item)) {
                // this item was already visited
                continue;
            }
            // edge is the Edge with the highest weight, that might not lead to an already added item
            // this item is not part of the totallyOrderedItems list
            Vertex vertex = itemGraph.getVertexForBoughtItem(item);
            vertex.traverseGraphForENDAndAddItemsToList(totallyOrderedItems, endName);

            assert totallyOrderedItems.getLast().getName().equals(endName);

            itemIsVisitedCount = 0;
            return;
        }
        // no edge found, that leads to an not already added item
        int count = 0;
        for (Edge edge : edges) {
            if (count++ < itemIsVisitedCount) {
                // this edge has already been taken a second time in a previous run
                continue;
            }
            // this edge has not been taken a second time yet
            itemIsVisitedCount++;
            if (itemIsVisitedCount == edges.size()) {
                // it is possible that far more cycles "end" in this item, as this item has out-going edges
                itemIsVisitedCount = 0;
            }
            // take edge a second time
            Vertex vertex = itemGraph.getVertexForBoughtItem(edge.getTo());
            vertex.traverseGraphForENDAndAddItemsToList(totallyOrderedItems, endName);
            // now the list is complete
            itemIsVisitedCount = 0;
            return;
        }
    }

    private PriorityQueue<Edge> getSortedEdges() {
        PriorityQueue<Edge> edges = new PriorityQueue<>(this.edges.size()+1, new Comparator<Edge>() {
            // the PriorityQueue-constructor with the capacity has to be used, because the constructor without is not available for Java 7
            @Override
            public int compare(Edge e1, Edge e2) {
                //return -(e1.getWeight() - e2.getWeight());
                return e2.getWeight() - e1.getWeight();
            }
        });
        edges.addAll(this.edges);
        return edges;
    }

    public Vertex findNextItemWithName(TreeSet<String> names) {
        final HashMap<BoughtItem, Integer> distances = new HashMap<>();
        SortedSet<Vertex> placesToLook = new TreeSet<>(new Comparator<Vertex>() {
            @Override
            public int compare(Vertex v1, Vertex v2) {
                Integer distance1, distance2;
                distance1 = distances.get(v1.getBoughtItem());
                distance2 = distances.get(v2.getBoughtItem());
                assert distance1 != null;
                assert distance2 != null;
                return distance1 - distance2;
            }
        });
        distances.put(this.getBoughtItem(), 0);
        placesToLook.add(this);

        return findNextItemWithName(names, placesToLook, distances, 0);
    }

    private Vertex findNextItemWithName(TreeSet<String> names, SortedSet<Vertex> placesToLook, HashMap<BoughtItem, Integer> distances, int currentDistance) {
        if (names.contains(this.getBoughtItem().getName())) {
            return this;
        }
        for (Edge edge : getSortedEdges()) {
            if (!distances.containsKey(edge.getTo())) {
                // the bought item at the other end of this edge has not yet been added to placesToLook
                Vertex vertex = itemGraph.getVertexForBoughtItem(edge.getTo());
                distances.put(vertex.getBoughtItem(), currentDistance + 1);
                placesToLook.add(vertex);
            }
            // the case "item is already contained in placesToLook, but with the wrong distance, cannot
            // occur, as all items with distance x come before any item with distance x+1
        }
        // all edges that lead to relevant vertices were added

        placesToLook.remove(this); // this vertex is not relevant any longer
        if (placesToLook.isEmpty()) {
            return null;
        }
        return placesToLook.first().findNextItemWithName(names, placesToLook, distances, currentDistance + 1);
    }
}