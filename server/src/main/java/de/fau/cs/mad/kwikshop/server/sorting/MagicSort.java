package de.fau.cs.mad.kwikshop.server.sorting;

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
        LinkedList<BoughtItem> totallyOrderedItems = itemGraph.getTotallyOrderedItems();
        LinkedList<Item> itemsToSort = new LinkedList<>(shoppingListToSort.getItems());

        LinkedList<Item> sortedItems = new LinkedList<>();
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
                // This item is not known for the current supermarket, so insert item at the end
                sortedItems.addLast(item);
                namesOfItemsThatAreAlreadySorted.add(item.getName());
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

        // sortedItems now contains every item that should be sorted
        int index = 0;
        for (Item item : sortedItems) {
            item.setOrder(index);
            index++;
        }

        return new ShoppingListServer(shoppingListToSort.getId(), sortedItems);
    }
}
