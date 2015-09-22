package de.fau.cs.mad.kwikshop.server.sorting.helperClasses;

import java.util.Arrays;
import java.util.List;

import de.fau.cs.mad.kwikshop.common.sorting.BoughtItem;
import de.fau.cs.mad.kwikshop.server.sorting.DAOHelper;
import de.fau.cs.mad.kwikshop.server.sorting.ItemGraph;

public class ItemGraphHelper {

    private final String ONE = "ONE";
    private final ItemCreationHelper itemCreationHelper = new ItemCreationHelper();

    public ItemGraph createNewItemGraph() {
        return new ItemGraph(new DAODummyHelper());
    }

    public ItemGraph createNewItemGraphWithSupermarket(String supermarketPlaceId) {
        return createNewItemGraphWithSupermarketAndDaoHelper(supermarketPlaceId, new DAODummyHelper());
    }

    public ItemGraph createNewItemGraphWithSupermarketAndDaoHelper(String supermarketPlaceId, DAOHelper daoHelper) {
        ItemGraph itemGraph = new ItemGraph(daoHelper);
        itemGraph.setSupermarket(supermarketPlaceId, supermarketPlaceId);
        return itemGraph;
    }

    public void addItemsToItemGraphThatWouldProduceACycleOfThree(ItemGraph itemGraph, BoughtItem i1, BoughtItem i2, BoughtItem i3) {
        addItemsToItemGraphThatWouldProduceACycle(itemGraph, i1, i2, i3);
    }

    public void addItemsToItemGraphThatWouldProduceACycle(ItemGraph itemGraph, BoughtItem... boughtItems) {
        for (int i = 0; i < boughtItems.length; i++) {
            BoughtItem firstItem = boughtItems[i];
            BoughtItem secondItem = boughtItems[(i+1) % boughtItems.length];
            addBoughtItemsToItemGraph(itemGraph, firstItem, secondItem);
        }
    }

    public ItemGraph createCyclicFreeDataWithSixVertices() {
        ItemGraph itemGraph = createNewItemGraphWithSupermarket(ONE);
        addCycleFreeDataWithSixVerticesToItemGraph(itemGraph);
        return itemGraph;
    }

    public void addCycleFreeDataWithSixVerticesToItemGraph(ItemGraph itemGraph) {
        BoughtItem i0, i1, i2, i3, i4, i5;
        i0 = itemCreationHelper.createBoughtItemWithIdAndSupermarket(0, ONE);
        i1 = itemCreationHelper.createBoughtItemWithIdAndSupermarket(1, ONE);
        i2 = itemCreationHelper.createBoughtItemWithIdAndSupermarket(2, ONE);
        i3 = itemCreationHelper.createBoughtItemWithIdAndSupermarket(3, ONE);
        i4 = itemCreationHelper.createBoughtItemWithIdAndSupermarket(4, ONE);
        i5 = itemCreationHelper.createBoughtItemWithIdAndSupermarket(5, ONE);

        addBoughtItemsToItemGraph(itemGraph, i0, i2);
        addBoughtItemsToItemGraph(itemGraph, i0, i1, i3);
        addBoughtItemsToItemGraph(itemGraph, i0, i1, i2);
        addBoughtItemsToItemGraph(itemGraph, i3, i4);
        addBoughtItemsToItemGraph(itemGraph, i5, i3);
        addBoughtItemsToItemGraph(itemGraph, i1, i5, i3, i4);
    }

    public void addBoughtItemsToItemGraph(ItemGraph itemGraph, BoughtItem... boughtItems) {
        List<BoughtItem> items = Arrays.asList(boughtItems);
        itemGraph.addBoughtItems(items);
    }
}
