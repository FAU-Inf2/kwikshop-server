package de.fau.cs.mad.kwikshop.server.sorting.helperClasses;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import de.fau.cs.mad.kwikshop.common.Item;
import de.fau.cs.mad.kwikshop.common.ShoppingListServer;
import de.fau.cs.mad.kwikshop.common.sorting.BoughtItem;
import de.fau.cs.mad.kwikshop.common.sorting.SortingRequest;
import de.fau.cs.mad.kwikshop.server.sorting.ItemGraph;
import de.fau.cs.mad.kwikshop.server.sorting.MagicSort;

public class MagicSortHelper {
    private final String ONE = "ONE";
    private final MagicSort algorithm = new MagicSort();
    private final SortingRequest defaultSortingRequest = new SortingRequest(ONE, ONE);
    private final ItemCreationHelper itemCreationHelper = new ItemCreationHelper();

    public List<Item> sort(ItemGraph itemGraph, List<Item> items) {
        return sort(itemGraph, items, defaultSortingRequest);
    }

    public List<Item> sort(ItemGraph itemGraph, List<Item> items, SortingRequest sortingRequest) {
        ShoppingListServer itemsToSort = new ShoppingListServer(0, items);
        ShoppingListServer sortedItemShoppingList = itemGraph.sort(algorithm, itemsToSort, sortingRequest);

        List<Item> sortedItems = new ArrayList<>();
        for(Item item: sortedItemShoppingList.getItems()) {
            sortedItems.add(item);
        }

        /* Sort according to the order of each Item, which was set in sort */
        Collections.sort(sortedItems);
        return sortedItems;
    }

    public List<BoughtItem> sortBoughtItems(ItemGraph itemGraph, List<BoughtItem> boughtItems) {
        return sortBoughtItems(itemGraph, boughtItems, defaultSortingRequest);
    }

    public List<BoughtItem> sortBoughtItems(ItemGraph itemGraph, List<BoughtItem> boughtItems, SortingRequest sortingRequest) {
        List<Item> items = new ArrayList<>(boughtItems.size());
        for (BoughtItem boughtItem : boughtItems) {
            Item item = itemCreationHelper.createItemWithId(boughtItem.getId());
            items.add(item);
        }
        List<Item> sortedItems = sort(itemGraph, items, sortingRequest);
        List<BoughtItem> sortedBoughtItems = new ArrayList<>(sortedItems.size());
        String supermarketPlaceId = sortingRequest.getPlaceId();
        for (Item item : sortedItems) {
            BoughtItem boughtItem = itemCreationHelper.createBoughtItemWithIdAndSupermarket(item.getId(), supermarketPlaceId);
            sortedBoughtItems.add(boughtItem);
        }
        return sortedBoughtItems;
    }
}
