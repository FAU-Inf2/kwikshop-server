package de.fau.cs.mad.kwikshop.server.sorting;

import javax.persistence.*;

import de.fau.cs.mad.kwikshop.common.sorting.BoughtItem;

@Entity
public class Edge {

    @Id
    @GeneratedValue
    private int id;

    @ManyToOne
    private BoughtItem start;

    @ManyToOne
    private BoughtItem end;

    @Column(name="weight")
    private int weight;

    public BoughtItem getStart() {
        return start;
    }

    public void setStart(BoughtItem start) {
        this.start = start;
    }

    public BoughtItem getEnd() {
        return end;
    }

    public void setEnd(BoughtItem end) {
        this.end = end;
    }

    public int getWeight() {
        return weight;
    }

    public void setWeight(int weight) {
        this.weight = weight;
    }

}
