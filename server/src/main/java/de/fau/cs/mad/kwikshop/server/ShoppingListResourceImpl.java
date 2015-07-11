package de.fau.cs.mad.kwikshop.server;

import de.fau.cs.mad.kwikshop.common.ShoppingList;
import de.fau.cs.mad.kwikshop.common.rest.ShoppingListResource;


import java.util.Arrays;
import java.util.List;

public class ShoppingListResourceImpl implements ShoppingListResource {


    private final static ShoppingList dummyList;

    static {
        dummyList = new ShoppingList();
        dummyList.setName("Dummy List");
    }


    @Override
    public List<ShoppingList> getShoppingList() {
        return Arrays.asList(dummyList);
    }

    @Override
    public ShoppingList getShoppingList(int id) {
        return dummyList;
    }


}
