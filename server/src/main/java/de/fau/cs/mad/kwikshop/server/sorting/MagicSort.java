package de.fau.cs.mad.kwikshop.server.sorting;

import java.util.Collections;
import java.util.Date;
import java.util.LinkedList;
import java.util.TreeSet;

import de.fau.cs.mad.kwikshop.common.Item;
import de.fau.cs.mad.kwikshop.common.ShoppingListServer;
import de.fau.cs.mad.kwikshop.common.sorting.BoughtItem;

public class MagicSort /*implements Algorithm<ShoppingListServer, ShoppingListServer>*/ {

    public static boolean printDebugOutput = true;

    private ItemGraph itemGraph;

    //@Override
    public void setUp(ItemGraph itemGraph) {
        this.itemGraph = itemGraph;
    }

    public ShoppingListServer sort(ShoppingListServer shoppingListToSort) {
        return sort(shoppingListToSort, false);
    }

    private ShoppingListServer sort(ShoppingListServer shoppingListToSort, boolean isGlobalItemGraph) {
        LinkedList<BoughtItem> totallyOrderedItems = itemGraph.getTotallyOrderedItems();
        LinkedList<Item> itemsToSort = new LinkedList<>(shoppingListToSort.getItems());

        LinkedList<Item> sortedItems = new LinkedList<>();
        LinkedList<Item> itemsNotContainedInItemGraph = new LinkedList<>();

        TreeSet<String> namesOfItemsThatAreAlreadySorted = new TreeSet<>();

        for (BoughtItem boughtItem : totallyOrderedItems) {
            Item foundItem = null;
            for (Item item : itemsToSort) {
                if (boughtItem.getName().equals(item.getName())) {
                    // a item with the same name is contained in the list, that is to be sorted
                    sortedItems.addLast(item);
                    namesOfItemsThatAreAlreadySorted.add(item.getName());
                    foundItem = item;
                    break; // break inner loop
                }
            }
            if (foundItem != null) {
                // remove this item from the "to sort" list, because it already is sorted now
                itemsToSort.remove(foundItem);
            }
        }

        // the sorted items now contain exactly these items, that were contained in both
        // totallyOrderedItems and itemsToSort; so far the list is already sorted
        // itemsToSort now only contains the items, that aren't sorted yet

        for (Item item : itemsToSort) {
            // this item is not sorted yet
            // Find a descendant of this item's vertex, that is already contained in sortedItems and
            // insert the item before that other item
            String name = item.getName();

            Vertex vertex = itemGraph.getVertexForNameOrNull(name);
            if (vertex == null) {
                // This item is not known for the current supermarket, add it to the list itemsNotContainedInItemGraph
                itemsNotContainedInItemGraph.addLast(item);
                continue;
            }

            // this item is known for the current supermarket, but not contained in the totallyOrderedItems list
            Vertex foundVertex = vertex.findNextItemWithName(namesOfItemsThatAreAlreadySorted);
            if (foundVertex == null) {
                // none of the already sorted items come after this one
                sortedItems.addLast(item);
                namesOfItemsThatAreAlreadySorted.add(item.getName());
                continue;
            }

            // this item comes before the item of foundVertex
            int index = 0;
            for (Item sortedItem : sortedItems) {
                if (sortedItem.getName().equals(foundVertex.getBoughtItem().getName())) {
                    // before this "sortedItem" the "item" has to be inserted
                    break;
                }
                index++;
            }

            // index now holds the index, where item has to be inserted
            sortedItems.add(index, item);
            namesOfItemsThatAreAlreadySorted.add(item.getName());
        }

        ItemGraph globalItemGraph = itemGraph.getGlobalSupermarketItemGraph();
        if (!itemsNotContainedInItemGraph.isEmpty() || globalItemGraph == null) {
            if (isGlobalItemGraph) {
                // this is already the global graph, so just add the items at the end (because there is no global graph to look anymore
                for (Item item : itemsNotContainedInItemGraph) {
                    sortedItems.addLast(item);
                }
            } else if(globalItemGraph != null) {
                // have a look in the global supermarket data, where the items should be sorted
                MagicSort globalMagicSort = new MagicSort();
                globalMagicSort.setUp(globalItemGraph);
                ShoppingListServer globallySortedShoppingList = globalMagicSort.sort(shoppingListToSort, true);
                LinkedList<Item> globallySortedItems = new LinkedList<>(globallySortedShoppingList.getItems());
                Collections.sort(globallySortedItems);
                for (Item notYetSortedItem : itemsNotContainedInItemGraph) {
                    int index = globallySortedItems.indexOf(notYetSortedItem);
                    // find the next item in globallySortedItems, that is also contained in the sortedItems list
                    boolean itemFound = false;
                    for (int i = index + 1; i < globallySortedItems.size(); i++) {
                        Item item = globallySortedItems.get(i);
                        if (!sortedItems.contains(item)) {
                            // this item is not sorted yet
                            continue;
                        }
                        // the notYetSortedItem is to be inserted before item
                        int itemIndex = sortedItems.indexOf(item);
                        sortedItems.add(itemIndex, notYetSortedItem);
                        itemFound = true;
                        break;
                    }
                    if (!itemFound) {
                        // there is no item after this one, that is alredy sorted
                        sortedItems.addLast(notYetSortedItem);
                    }
                }
            }
        }

        // sortedItems now contains every item that should be sorted
        int index = 0;
        for (Item item : sortedItems) {
            Item listItem = shoppingListToSort.getItem(item.getServerId());
            if(listItem != null) {
                if(listItem.getDeleted() == false && listItem.isBought() == false) {
                    listItem.setOrder(index);
                    listItem.setVersion(listItem.getVersion()+1);
                }
            }

            index++;
        }

        /* Update version and lastModifiedDate to make sure this list gets synchronized */
        shoppingListToSort.setVersion(shoppingListToSort.getVersion()+1);
        shoppingListToSort.setLastModifiedDate(new Date());

        return shoppingListToSort;
    }
}
