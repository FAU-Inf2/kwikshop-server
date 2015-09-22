package de.fau.cs.mad.kwikshop.server.sorting;

import java.util.concurrent.locks.ReentrantLock;

import javax.persistence.*;

@Entity
public class SupermarketChain {

    @Id
    @GeneratedValue
    private int id;

    @Column(name="name")
    private String name;

    private ReentrantLock lock = new ReentrantLock();

    public SupermarketChain() {

    }

    public SupermarketChain(String name) {
        this.name = name;
    }

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

    public ReentrantLock getLock() {
        return lock;
    }

    @Override
    public int hashCode() {
        return name.hashCode();
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

        SupermarketChain supermarketChain = (SupermarketChain) o;

        if(!this.name.equals(supermarketChain.getName()))
            return false;

        return true;
    }
}
