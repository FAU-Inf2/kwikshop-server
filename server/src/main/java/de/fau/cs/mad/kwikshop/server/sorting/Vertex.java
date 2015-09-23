package de.fau.cs.mad.kwikshop.server.sorting;

import java.util.LinkedList;
import java.util.List;

import de.fau.cs.mad.kwikshop.common.sorting.BoughtItem;

public class Vertex {
    private final BoughtItem boughtItem;
    private List<Edge> edges; // adjacency list of edges

    public Vertex(BoughtItem boughtItem) {
        this.boughtItem = boughtItem;
        this.edges = new LinkedList<>();
    }

    public BoughtItem getBoughtItem() {
        return boughtItem;
    }

    public synchronized void addEdge(Edge edge) {
        edges.add(edge);
    }

    public synchronized List<Edge> getEdges() {
        return new LinkedList<>(edges);
    }
}
