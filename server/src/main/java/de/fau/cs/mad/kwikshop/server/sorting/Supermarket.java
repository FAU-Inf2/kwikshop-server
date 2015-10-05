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

    @Override
    public int hashCode() {
        int result = 1;

        result = 37 * result + placeId.hashCode();

        int supermarketChainHash = 0;
        if(supermarketChain != null) {
            supermarketChainHash = supermarketChain.hashCode();
        }
        result = 37 * result + supermarketChainHash;

        return result;
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

        Supermarket supermarket = (Supermarket) o;

        if(this.getSupermarketChain() != null && supermarket.getSupermarketChain() != null) {
            if(!this.getSupermarketChain().equals(supermarket.getSupermarketChain()))
                return false;
        }

        if(!this.getPlaceId().equals(supermarket.getPlaceId()))
            return false;

        return true;
    }
}
