package de.fau.cs.mad.kwikshop.server.sorting.helperClasses;

import java.util.List;

import de.fau.cs.mad.kwikshop.common.Item;
import de.fau.cs.mad.kwikshop.common.ShoppingListServer;
import de.fau.cs.mad.kwikshop.common.sorting.BoughtItem;
import de.fau.cs.mad.kwikshop.server.sorting.DAOHelper;
import de.fau.cs.mad.kwikshop.server.sorting.ItemGraph;

public class SortingTestSuperclass {

    protected final String ONE = "ONE";
    protected final String TWO = "TWO";
    protected final String THREE = "THREE";
    protected final String FOUR = "FOUR";
    protected final String CHAIN_ONE = "CHAIN_ONE";
    protected final String CHAIN_TWO = "CHAIN_TWO";

    protected final ItemCreationHelper itemCreationHelper = new ItemCreationHelper();
    protected final ItemGraphHelper itemGraphHelper = new ItemGraphHelper();
    protected final MagicSortHelper magicSortHelper = new MagicSortHelper();

    /* Helper methods that might be useful in all test classes concerning sorting */

    protected ItemGraph createNewItemGraph() {
        return itemGraphHelper.createNewItemGraph();
    }

    protected ItemGraph createNewItemGraphWithSupermarket(String supermarketPlaceId) {
        return itemGraphHelper.createNewItemGraphWithSupermarket(supermarketPlaceId);
    }

    protected ItemGraph createNewItemGraphWithSupermarketAndDAOHelper(String supermarketPlaceId, DAOHelper daoHelper) {
        return itemGraphHelper.createNewItemGraphWithSupermarketAndDaoHelper(supermarketPlaceId, daoHelper);
    }

    protected ItemGraph createNewItemGraphWithSupermarketAndDAOHelper(String supermarketPlaceId, String supermarketName, DAOHelper daoHelper) {
        return itemGraphHelper.createNewItemGraphWithSupermarketAndDaoHelper(supermarketPlaceId, supermarketName, daoHelper);
    }

    protected void addItemsToItemGraphThatWouldProduceACycleOfThree(ItemGraph itemGraph, BoughtItem i1, BoughtItem i2, BoughtItem i3) {
        itemGraphHelper.addItemsToItemGraphThatWouldProduceACycleOfThree(itemGraph, i1, i2, i3);
    }

    protected void addItemsToItemGraphThatWouldProduceACycle(ItemGraph itemGraph, BoughtItem... boughtItems) {
        itemGraphHelper.addItemsToItemGraphThatWouldProduceACycle(itemGraph, boughtItems);
    }

    protected ItemGraph createCyclicFreeDataWithSixVertices() {
        return itemGraphHelper.createCyclicFreeDataWithSixVertices();
    }

    protected void addCycleFreeDataWithSixVerticesToItemGraph(ItemGraph itemGraph) {
        itemGraphHelper.addCycleFreeDataWithSixVerticesToItemGraph(itemGraph);
    }

    protected List<BoughtItem> createBoughtItems(int numberOfItemsToCreate, String supermarketPlaceId) {
        return itemCreationHelper.createBoughtItems(numberOfItemsToCreate, supermarketPlaceId);
    }

    protected ShoppingListServer createShoppingListServerWithNItems(int n) {
        return itemCreationHelper.createShoppingListServerWithNItems(n);
    }

    protected ShoppingListServer createShoppingListServerWithNItemsMixedUp(int n) {
        return itemCreationHelper.createShoppingListServerWithNItemsMixedUp(n);
    }

    protected Item createItemWithId(int id) {
        return itemCreationHelper.createItemWithId(id);
    }

    protected BoughtItem createBoughtItemWithIdAndSupermarket(int id, String supermarketPlaceId) {
        return itemCreationHelper.createBoughtItemWithIdAndSupermarket(id, supermarketPlaceId);
    }

    protected void addBoughtItemsToItemGraph(ItemGraph itemGraph, BoughtItem... boughtItems) {
        itemGraphHelper.addBoughtItemsToItemGraph(itemGraph, boughtItems);
    }
}
