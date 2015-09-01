package de.fau.cs.mad.kwikshop.server.sorting;

import de.fau.cs.mad.kwikshop.common.ShoppingListServer;

public class MagicSort implements Algorithm<ShoppingListServer, ShoppingListServer> {

    private ItemGraph itemGraph;

    @Override
    public void setUp(ItemGraph itemGraph) {
        this.itemGraph = itemGraph;
    }

    @Override
    public ShoppingListServer execute(ShoppingListServer shoppingList) {
        return shoppingList;
    }

}
