package de.fau.cs.mad.kwikshop.server.sorting.helperClasses;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Random;
import java.util.Set;

import de.fau.cs.mad.kwikshop.common.Item;
import de.fau.cs.mad.kwikshop.common.ShoppingListServer;
import de.fau.cs.mad.kwikshop.common.sorting.BoughtItem;
import de.fau.cs.mad.kwikshop.server.sorting.DAOHelper;
import de.fau.cs.mad.kwikshop.server.sorting.ItemGraph;
import de.fau.cs.mad.kwikshop.server.sorting.MagicSort;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

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
        return itemGraphHelper.createNewItemGraphWithSupermarket(ONE);
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

    protected boolean checkForDoubleEdge(ItemGraph itemGraph, BoughtItem i1, BoughtItem i2) {
        return itemGraphHelper.checkForDoubleEdge(itemGraph, i1, i2);
    }

    private ItemGraph createItemGraphWithNItemsAndCheckIfEveryItemHasBeenCreated(int n, boolean addItemsMoreOften, boolean mixSomeOfTheItems) {
        boolean magicSortDebugOutput = MagicSort.printDebugOutput;
        boolean itemGraphDebugOutput = ItemGraph.printDebugOutput;
        try {
            MagicSort.printDebugOutput = false;
            ItemGraph.printDebugOutput = false;
            Random random = new Random(42);
            List<BoughtItem> items = createBoughtItems(n, ONE);
            ItemGraph itemGraph = createNewItemGraphWithSupermarket(ONE);
            boolean[] itemAlreadyAdded = new boolean[n];
            double sqrtN = Math.sqrt(n);
            int numberOfTurns = (int)(sqrtN * 2.5);
            if (addItemsMoreOften) { numberOfTurns *= 4; }
            for (int i = 0; i < numberOfTurns; i++) {
                int numberOfItemsToAdd = (int)(sqrtN + ((random.nextDouble() - 0.5) * sqrtN));
                System.out.println("Starting to add items for the " + (i+1) + "th time; " + numberOfItemsToAdd + " will be added");
                List<BoughtItem> boughtItems = new ArrayList<>(numberOfItemsToAdd);
                for (int j = 0; j < numberOfItemsToAdd; j++) {
                    int index = random.nextInt(n);
                    itemAlreadyAdded[index] = true;
                    boughtItems.add(items.get(index));
                }
                if (mixSomeOfTheItems) {
                    if (random.nextDouble() < 0.5) {
                        Collections.sort(boughtItems, new Comparator<BoughtItem>() {
                            @Override
                            public int compare(BoughtItem i1, BoughtItem i2) {
                                return i1.getName().compareTo(i2.getName());
                            }
                        });
                    } else {
                        if (random.nextDouble() < 0.5) {
                            Collections.sort(boughtItems, new Comparator<BoughtItem>() {
                                @Override
                                public int compare(BoughtItem i1, BoughtItem i2) {
                                    return i1.getName().compareTo(i2.getName());
                                }
                            });
                            int swap1 = random.nextInt(boughtItems.size());
                            int swap2 = random.nextInt(boughtItems.size());
                            Collections.swap(boughtItems, swap1, swap2);
                        }
                    }
                }
                itemGraph.addBoughtItems(boughtItems);
            }

            Set<BoughtItem> addedItems = itemGraph.getVertices();
            int numberOfItemsAdded = 0;
            for (int i = 0; i < n; i++) {
                if (itemAlreadyAdded[i]) {
                    numberOfItemsAdded++;
                    assertTrue("Item not contained in list of items, although it has been added", addedItems.contains(items.get(i)));
                } else {
                    assertFalse("Item contained in list of items, although it has not been added", addedItems.contains(items.get(i)));
                }
            }
            assertEquals("", numberOfItemsAdded, addedItems.size() - 2); // -2 because of START/END
            System.out.print("Number of items added: " + numberOfItemsAdded);
            System.out.println(" (=" + ((double) numberOfItemsAdded / (double) n) * 100 + "%)");

            return itemGraph;
        } finally {
            MagicSort.printDebugOutput = magicSortDebugOutput;
            ItemGraph.printDebugOutput = itemGraphDebugOutput;
        }
    }

    protected ItemGraph createItemGraphWithNItems_MixSomeOfThem_AndCheckIfEveryItemHasBeenCreated(int n) {
        return createItemGraphWithNItemsAndCheckIfEveryItemHasBeenCreated(n, false, true);
    }

    protected ItemGraph createItemGraphWithNItemsAndCheckIfEveryItemHasBeenCreated(int n, boolean addItemsMoreOften) {
        return createItemGraphWithNItemsAndCheckIfEveryItemHasBeenCreated(n, addItemsMoreOften, false);
    }
}
