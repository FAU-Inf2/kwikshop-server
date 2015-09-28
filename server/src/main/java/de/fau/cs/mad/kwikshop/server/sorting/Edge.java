package de.fau.cs.mad.kwikshop.server.sorting;

import javax.persistence.*;

import de.fau.cs.mad.kwikshop.common.sorting.BoughtItem;
import de.fau.cs.mad.kwikshop.common.util.NamedQueryConstants;

@Entity
@NamedQueries({
        @NamedQuery(
                name = NamedQueryConstants.EDGE_GET_BY_FROMTO,
                query = "SELECT e FROM Edge e WHERE e.from = :" + NamedQueryConstants.EDGE_FROM_NAME +
                        " AND e.to = :" + NamedQueryConstants.EDGE_TO_NAME
        )
})
public class Edge {

    @Id
    @GeneratedValue
    private int id;

    @ManyToOne(cascade = {CascadeType.PERSIST, CascadeType.REFRESH})
    @JoinColumn(name="from_item")
    private BoughtItem from;

    @ManyToOne(cascade = {CascadeType.PERSIST, CascadeType.REFRESH})
    @JoinColumn(name="to_item")
    private BoughtItem to;

    @Column(name="weight")
    private int weight = 1;

    @Column(name="distance")
    private int distance = 0;

    @ManyToOne(cascade = {CascadeType.PERSIST, CascadeType.REFRESH})
    private Supermarket supermarket;

    public Edge() {

    }

    public Edge(BoughtItem from, BoughtItem to, Supermarket supermarket) {
        this.from = from;
        this.to = to;
        this.supermarket = supermarket;
    }

    public BoughtItem getFrom() {
        return from;
    }

    public void setFrom(BoughtItem from) {
        this.from = from;
    }

    public BoughtItem getTo() {
        return to;
    }

    public void setTo(BoughtItem to) {
        this.to = to;
    }

    public int getWeight() {
        return weight;
    }

    public void setWeight(int weight) {
        this.weight = weight;
    }

    public int incrementWeight() {
        return ++weight;
    }

    public int decrementWeight() {
        return --weight;
    }

    public int getDistance() {
        return distance;
    }

    /*// item graph is needed in order to be able to update the parents list of the "to"-Vertex
    public void setDistance(int distance, ItemGraph itemGraph) {
        if (itemGraph == null) {
            throw new ArgumentNullException("itemGraph");
        }
        int oldDistance = this.distance;
        this.distance = distance;
        if (distance == 0 && oldDistance > 0) {
            // add edge to the vertex's parents list
            Vertex child = itemGraph.getVertexForBoughtItem(to);
            child.addBoughtItemToParents(from);
        } else if (distance > 0 && oldDistance == 0) {
            // remove edge from the vertex's parents list
            Vertex child = itemGraph.getVertexForBoughtItem(to);
            child.removeBoughtItemFromParents(from);
        }
    }

    public void addDistance(int distance) {
        this.distance = this.distance + distance;
    }*/

    public Supermarket getSupermarket() {
        return supermarket;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null) {
            return false;
        }
        if (this == o) {
            return true;
        }
        if (o.getClass() != this.getClass()) {
            return false;
        }

        Edge edge = (Edge) o;

        if(!this.from.equals(edge.getFrom()))
            return false;

        if(!this.to.equals(edge.getTo()))
            return false;

        if(! (this.weight == edge.getWeight()) )
            return false;

        if(! (this.distance == edge.getDistance()) )
            return false;

        if(!this.supermarket.equals(edge.getSupermarket()))
            return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = 1;

        int fromHash = 0;
        if(from != null)
            fromHash = from.hashCode();
        result = 37 * result + fromHash;

        int toHash = 0;
        if(to != null)
            toHash = to.hashCode();
        result = 37 * result + toHash;

        result = 37 * result + weight;
        result = 37 * result + distance;

        result = 37 * result + supermarket.hashCode();

        return result;
    }
}
