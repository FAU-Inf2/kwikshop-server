package de.fau.cs.mad.kwikshop.server.sorting;

import de.fau.cs.mad.kwikshop.common.ShoppingListServer;

public interface Algorithm {

    void setUp(ItemGraph itemGraph);
    ShoppingListServer sort(ShoppingListServer shoppingList);

}
