package de.fau.cs.mad.kwikshop.server.sorting;

import java.util.List;
import java.util.Set;

import de.fau.cs.mad.kwikshop.common.sorting.BoughtItem;

public class ItemGraph {

    protected Set<BoughtItem> vertices;
    protected Set<Edge> edges;

    public ItemGraph(Set<BoughtItem> vertices, Set<Edge> edges) {
        this.vertices = vertices;
        this.edges = edges;
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

}
