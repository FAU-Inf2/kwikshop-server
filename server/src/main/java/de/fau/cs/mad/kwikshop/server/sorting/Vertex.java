package de.fau.cs.mad.kwikshop.server.sorting;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import de.fau.cs.mad.kwikshop.common.sorting.BoughtItem;

public class Vertex {
    private final BoughtItem boughtItem;
    private List<Edge> edges; // adjacency list of edges
    private Set<Vertex> parents;
            // Set of all parents (i.e. vertices with an edge with distance 0 to this edge).
            // As edges won't reduce their distance to 0, no edges have to be added after modifying their distance value

    private final ItemGraph itemGraph;

    public Vertex(BoughtItem boughtItem, ItemGraph itemGraph) {
        this.boughtItem = boughtItem;
        this.edges = new LinkedList<>();
        this.parents = new HashSet<>();
        this.itemGraph = itemGraph;
    }

    public BoughtItem getBoughtItem() {
        return boughtItem;
    }

    public synchronized void addEdge(Edge edge) {
        edges.add(edge);
        if (edge.getDistance() == 0) {
            Vertex child = itemGraph.getVertexForBoughtItem(edge.getTo());
            assert child != null : "This is " + boughtItem.getName() + "; Vertex not found: " + edge.getTo().getName();
            child.parents.add(this);
        }
    }

    public synchronized List<Edge> getEdges() {
        return new LinkedList<>(edges);
    }

    public synchronized Set<Vertex> getParents() {
        return new HashSet<>(parents);
    }

    public synchronized void addBoughtItemToParents(BoughtItem boughtItem) {
        Vertex vertex = itemGraph.getVertexForBoughtItem(boughtItem);
        if (!parents.contains(vertex)) {
            parents.add(vertex);
        }
    }

    public synchronized void removeBoughtItemFromParents(BoughtItem boughtItem) {
        Vertex vertex = itemGraph.getVertexForBoughtItem(boughtItem);
        if (parents.contains(vertex)) {
            parents.remove(vertex);
        }
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof Vertex)) {
            return false;
        }
        return boughtItem.getName().equals(((Vertex)obj).getBoughtItem().getName());
    }

    @Override
    public int hashCode() {
        return boughtItem.getName().hashCode();
    }
}
