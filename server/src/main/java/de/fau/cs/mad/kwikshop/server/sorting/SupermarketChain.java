package de.fau.cs.mad.kwikshop.server.sorting;

import javax.persistence.*;

@Entity
public class SupermarketChain {

    @Id
    @GeneratedValue
    private int id;

    @Column(name="name")
    private String name;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
