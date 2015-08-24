package de.fau.cs.mad.kwikshop.server.sorting;

import javax.persistence.*;

@Entity
public class Supermarket {

    @Id
    @GeneratedValue
    private int id;

    @ManyToOne
    @JoinColumn(name="supermarketChain")
    private SupermarketChain supermarketChain;

    @Column(name="placesId")
    private String placesId;

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

    public String getPlacesId() {
        return placesId;
    }

    public void setPlacesId(String placesId) {
        this.placesId = placesId;
    }
}
