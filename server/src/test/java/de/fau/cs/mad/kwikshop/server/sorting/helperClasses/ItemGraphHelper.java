package de.fau.cs.mad.kwikshop.server.sorting.helperClasses;

import java.util.ArrayList;
import java.util.List;

import de.fau.cs.mad.kwikshop.common.sorting.BoughtItem;
import de.fau.cs.mad.kwikshop.server.sorting.ItemGraph;

public class ItemGraphHelper {

    private final String ONE = "ONE";
    private final ItemCreationHelper itemCreationHelper = new ItemCreationHelper();

    public ItemGraph createNewItemGraph() {
        return new ItemGraph(new DAODummyHelper());
    }

    public ItemGraph createNewItemGraphWithSupermarket(String supermarketPlaceId) {
        ItemGraph itemGraph = new ItemGraph(new DAODummyHelper());
        itemGraph.setSupermarket(supermarketPlaceId, supermarketPlaceId);
        return itemGraph;
    }

    public void addItemsToItemGraphThatWouldProduceACycleOfThree(ItemGraph itemGraph, BoughtItem i1, BoughtItem i2, BoughtItem i3) {
        List<BoughtItem> first, second, third;
        first = new ArrayList<>(2);
        first.add(i1);
        first.add(i2);

        second = new ArrayList<>(2);
        second.add(i2);
        second.add(i3);

        third = new ArrayList<>(2);
        third.add(i3);
        third.add(i1);

        itemGraph.addBoughtItems(first);
        itemGraph.addBoughtItems(second);
        itemGraph.addBoughtItems(third);
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

        List<BoughtItem> first, second, third, fourth, fifth, sixth;
        first = new ArrayList<>(2);
        first.add(i0);
        first.add(i2);

        second = new ArrayList<>(3);
        second.add(i0);
        second.add(i1);
        second.add(i3);

        third = new ArrayList<>(3);
        third.add(i0);
        third.add(i1);
        third.add(i2);

        fourth = new ArrayList<>(2);
        fourth.add(i3);
        fourth.add(i4);

        fifth = new ArrayList<>(2);
        fifth.add(i5);
        fifth.add(i3);

        sixth = new ArrayList<>(4);
        sixth.add(i1);
        sixth.add(i5);
        sixth.add(i3);
        sixth.add(i4);


        itemGraph.addBoughtItems(first);
        itemGraph.addBoughtItems(second);
        itemGraph.addBoughtItems(third);
        itemGraph.addBoughtItems(fourth);
        itemGraph.addBoughtItems(fifth);
        itemGraph.addBoughtItems(sixth);
    }
}
