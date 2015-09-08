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

    public int getDistance() {
        return distance;
    }

    public void addDistance(int distance) {
        this.distance = this.distance + distance;
    }

}
