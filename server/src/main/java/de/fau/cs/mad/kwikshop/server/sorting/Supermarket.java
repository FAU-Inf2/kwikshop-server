package de.fau.cs.mad.kwikshop.server.sorting;

import javax.persistence.*;

@Entity
public class Supermarket {

    @Id
    @GeneratedValue
    private int id;

    @ManyToOne(cascade = {CascadeType.PERSIST, CascadeType.REFRESH})
    private SupermarketChain supermarketChain;

    @Column(name="placeId")
    private String placeId;

    public Supermarket() {

    }

    public Supermarket(String placeId) {
        this.placeId = placeId;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public SupermarketChain getSupermarketChain() {
        return supermarketChain;
    }

    public void setSupermarketChain(SupermarketChain supermarketChain) {
        this.supermarketChain = supermarketChain;
    }

    public String getPlaceId() {
        return placeId;
    }

    public void setPlaceId(String placeId) {
        this.placeId = placeId;
    }
}
