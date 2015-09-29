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

    //@Override
//    public ShoppingListServer execute(ShoppingListServer shoppingListArg) {
//        return null;
//        this.shoppingList = shoppingListArg;
//        knownItems = new ArrayList<BoughtItem>();
//        unknownItems = new ArrayList<BoughtItem>();
//        sortedList = new ArrayList<BoughtItem>();
//
//        //sortedList.add(daoHelper.getStartBoughtItem());
//
//        /* Split the ShoppingList's Items into known and unknown Items */
//        for(Item item: shoppingList.getItems()) {
//            if(item.getDeleted())
//                continue;
//
//            BoughtItem boughtItem = daoHelper.getBoughtItemByName(item.getName());
//
//            /* Item doesn't exist in the DB */
//            if(boughtItem == null) {
//                boughtItem = new BoughtItem(item.getName());
//                daoHelper.createBoughtItem(boughtItem);
//                unknownItems.add(boughtItem);
//                continue;
//            }
//
//            //boughtItem.setItemId(item.getServerId()); /* Set ItemId - used to map BoughtItems to ShoppingList Items */
//
//            if(itemGraph.getVertices().contains(boughtItem)) {
//                knownItems.add(boughtItem);
//            } else {
//                unknownItems.add(boughtItem);
//            }
//        }
//
//
//        /* Step 3-5 /
//        traverse(daoHelper.getStartBoughtItem());
//
//        if (printDebugOutput) {
//            System.out.println("-----TRAVERSE-----");
//            for(BoughtItem item: sortedList) {
//                System.out.println("---> " + item.getName());
//            }
//            System.out.println("----------");
//        }
//
//        /* Step 6 /
//        while(knownItems.size() > 0)
//            addMissingItems(knownItems.get(0));
//
//        /* Add unknown Items at the end */
//
//        //this replaces the original sorting algorithm (should be more precise)
//        sortBoughtItems(knownItems);
//        for(BoughtItem item : knownItems){
//            sortedList.add(item);
//        }
//
//        sortedList.addAll(unknownItems);
//
//        //sortedList.remove(daoHelper.getStartBoughtItem());
//
//        if (printDebugOutput) {
//            System.out.println("=====FINAL=====");
//            for (BoughtItem item : sortedList) {
//                System.out.println("===> " + item.getName());
//            }
//            System.out.println("==========");
//        }
//
//        /* Apply the order of BoughtItems to the ShoppingList */
//        applyOrderToShoppingList();
//
//        return shoppingList;
//    }

    /* Steps 3 - 5 (Sorting Algorithm.md) */
//    private void traverse(BoughtItem currentItem) {
//
//        Set<Edge> currentEdges = itemGraph.getEdgesFrom(currentItem);
//        double maxWeightDistanceRatio = 0;
//        BoughtItem nextItem = null;
//
//        for(Edge edge: currentEdges) {
//            //if(edge.getDistance() > 0)
//            //    continue;
//
//            double currentWeightDistanceRatio = ((double)edge.getWeight()+1) / ((double)edge.getDistance()+1);
//
//            if (printDebugOutput) {
//                System.out.println("Traverse: " + edge.getFrom().getName() + " -> " + edge.getTo().getName() + " (" + currentWeightDistanceRatio + ")");
//            }
//
//            if( (currentWeightDistanceRatio > maxWeightDistanceRatio) ) /*&&
//                    (knownItems.contains(edge.getTo())) ) */{
//                maxWeightDistanceRatio = currentWeightDistanceRatio;
//                nextItem = edge.getTo();
//            }
//        }
//
//        if(nextItem == null)
//            return;
//
//        knownItems.remove(nextItem);
//        sortedList.add(nextItem);
//
//        traverse(nextItem);
//    }
//
//    private void addMissingItems(BoughtItem item) {
//        if (printDebugOutput) {
//            System.out.println("MISSING ITEM: " + item.getName());
//        }
//
//        double maxWeightDistanceRatio = 0;
//
//        BoughtItem insertAfter = null;
//        List<Edge> parentEdges = daoHelper.getEdgesByTo(item, itemGraph.getSupermarket());
//
//        for(Edge edge: parentEdges) {
//            double currentWeightDistanceRatio = ((double)edge.getWeight()+1) / ((double)edge.getDistance()+1);
//
//            if (printDebugOutput) {
//                System.out.println("MISSING ITEM EDGE: " + edge.getFrom().getName() + " -> " + edge.getTo().getName() + " (" + currentWeightDistanceRatio + ")");
//            }
//
//            if( (currentWeightDistanceRatio > maxWeightDistanceRatio) && !edge.getFrom().equals(daoHelper.getStartBoughtItem()))
//                && (sortedList.contains(edge.getFrom())) ) {
//                maxWeightDistanceRatio = currentWeightDistanceRatio;
//                insertAfter = edge.getFrom();
//            }
//        }
//
//        knownItems.remove(item);
//
//        if(insertAfter != null) {
//
//            if(!sortedList.contains(insertAfter)) {
//                knownItems.remove(insertAfter);
//                knownItems.add(0, insertAfter);
//                addMissingItems(knownItems.get(0));
//            }
//
//             If insertAfter is the START_ITEM, do not insert at position 0, but at position 1
//            if(insertAfter.equals(daoHelper.getStartBoughtItem()))
//                insertAfter = sortedList.get(1);
//
//            if (printDebugOutput) {
//                System.out.println("INSERT AFTER: " + insertAfter.getName());
//            }
//            sortedList.add(sortedList.indexOf(insertAfter) + 1, item);
//        } else {
//            if(knownItems.size() == 0) {
//                unknownItems.add(item);
//                return;
//            }
//
//            knownItems.add(knownItems.size(), item);  Insert this Item at the end
//        }
//
//    }
//
//    private void applyOrderToShoppingList() {
//        int i = 0;
//        for(BoughtItem boughtItem: sortedList) {
//            if(boughtItem.equals(daoHelper.getEndBoughtItem()))
//                continue;
//
//            //Item item = shoppingList.getItem(boughtItem.getItemId());
//            List<Item> itemList = shoppingList.getAllItems(boughtItem.getName());
//            for(Item item : itemList) {
//                if (item == null)
//                    continue;
//                if (item.getDeleted())
//                    continue;
//
//                if (printDebugOutput) {
//                    System.out.println("Setting order for: " + item.getName());
//                }
//                item.setOrder(i);
//                item.setVersion(item.getVersion() + 1);
//                i++;
//            }
//        }
//        shoppingList.setVersion(shoppingList.getVersion()+1);
//        shoppingList.setLastModifiedDate(new Date());
//    }

    //sorts the List of boughtItems according to the direction of the existing edges
    //originally meant to only sort the missing items, but that might also work for the whole knownItems
//    public List<BoughtItem> sortBoughtItems(List<BoughtItem> items){
  //      return null;
/*
        for(int i = 0; i < items.size(); i++){
            for(int j = i + 1; j < items.size(); j++) {
                if (itemGraph.edgeFromToExists(items.get(i), items.get(j))) {
                    //edge exists: those two items have the correct order
                    continue;
                } else if (itemGraph.edgeFromToExists(items.get(j), items.get(i))) {
                    //edge is the other way around: put j before i
                    items.add(i, items.get(j));
                    items.remove(j + 1);

                    //repeat this step with a new from node
                    i--;
                    break;
                } else {
                    insertAfterAncestor(items, j);
                    continue;
                }
            }
        }
        return items;

        */

//        final HashMap<BoughtItem, Integer> amountOfEdgesFromThisPoint = new HashMap<>();
//
//        for(int i = 0; i < items.size(); i++){
//            int edgeCounter = 0;
//            for(int j = 0; j < items.size(); j++){
//                if(i != j){
//                    //increase edgeCounter for every item that has a edge from the current item at position i
//                    if(itemGraph.edgeFromToExists(items.get(i), items.get(j))) edgeCounter ++;
//                }
//                if(j == items.size() -1){
//                    //after iterating over every other item put the item with its counter in the hashmap
//                    amountOfEdgesFromThisPoint.put(items.get(i), edgeCounter);
//                }
//            }
//        }
//        Collections.sort(items, new Comparator<BoughtItem>(){
//            public int compare(BoughtItem i1, BoughtItem i2){
//                return amountOfEdgesFromThisPoint.get(i2) - amountOfEdgesFromThisPoint.get(i1);
//            }
//        });
//
//        return items;
//    }
//
//    //inserts the item at currentIndex after an ancestor if the item after the ancestor isn't also an ancestor of the item at currentIndex
//    private void insertAfterAncestor(List<BoughtItem> items, int currentIndex){
//        for (int k = 0; k < currentIndex; k++) {
//            if (itemGraph.edgeFromToExists(items.get(k), items.get(currentIndex)) && !itemGraph.edgeFromToExists(items.get(k+1), items.get(currentIndex))) {
//                items.add(k + 1, items.get(currentIndex));
//                items.remove(currentIndex + 1);
//                break;
//            }
//        }
//    }


}
