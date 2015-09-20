package de.fau.cs.mad.kwikshop.server.sorting;

import org.junit.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import de.fau.cs.mad.kwikshop.common.Item;
import de.fau.cs.mad.kwikshop.common.ShoppingListServer;
import de.fau.cs.mad.kwikshop.common.sorting.BoughtItem;
import de.fau.cs.mad.kwikshop.common.sorting.SortingRequest;
import de.fau.cs.mad.kwikshop.server.sorting.helperClasses.SortingTestSuperclass;

import static org.junit.Assert.*;

public class MagicSortTest extends SortingTestSuperclass {

    /* Tests concerning whether the magic sort algorithm sorts items correctly */

    @Test
    public void twoItemsAreSortedIdenticallyASecondTime() {
        nItemsAreSortedIdenticallyASecondTime(2, false);
    }

    @Test
    public void threeItemsAreSortedIdenticallyASecondTime() {
        nItemsAreSortedIdenticallyASecondTime(3, false);
    }
    @Test
    public void fiveItemsAreSortedIdenticallyASecondTime() {
        nItemsAreSortedIdenticallyASecondTime(5, false);
    }

    @Test
    public void tenItemsAreSortedIdenticallyASecondTime() {
        nItemsAreSortedIdenticallyASecondTime(10, false);
    }

    @Test
    public void twoItemsAreSortedIdenticallyASecondTimeWhenTheOrderIsDifferent() {
        nItemsAreSortedIdenticallyASecondTime(2, true);
    }

    @Test
    public void threeItemsAreSortedIdenticallyASecondTimeWhenTheOrderIsDifferent() {
        nItemsAreSortedIdenticallyASecondTime(3, true);
    }
    @Test
    public void fiveItemsAreSortedIdenticallyASecondTimeWhenTheOrderIsDifferent() {
        nItemsAreSortedIdenticallyASecondTime(5, true);
    }

    @Test
    public void tenItemsAreSortedIdenticallyASecondTimeWhenTheOrderIsDifferent() {
        nItemsAreSortedIdenticallyASecondTime(10, true);
    }

    // Helper method for a limited number of related tests
    private void nItemsAreSortedIdenticallyASecondTime(int n, boolean mixItemsBeforeSorting) {
        List<BoughtItem> items = createBoughtItems(n, ONE);
        ItemGraph itemGraph = createNewItemGraphWithSupermarket(ONE);
        itemGraph.addBoughtItems(items);

        ShoppingListServer shoppingListServer;

        if (mixItemsBeforeSorting) {
            shoppingListServer = createShoppingListServerWithNItemsMixedUp(n);
            /*shoppingListServer now has items with exactly the same name as the items in itemGraph
                but in a different order*/
        } else {
            shoppingListServer = createShoppingListServerWithNItems(n);
            /*shoppingListServer now has items with exactly the same name as the items in itemGraph
                and in the same order*/
        }

        List<Item> sortedItems = magicSortHelper.sort(itemGraph, shoppingListServer);

        for (int i = 0; i < n; i++) {
            assertEquals("A identical list was sorted different as before, although no different data is available. The lists first differ at element " + i, items.get(i).getName(), sortedItems.get(i).getName());
        }
    }

    @Test(timeout = 5000)
    public void sortWillReturnSomethingEvenIfTheDataIsInsufficient() {
        BoughtItem i1, i2, i0;
        i1 = createBoughtItemWithIdAndSupermarket(1, ONE);
        i2 = createBoughtItemWithIdAndSupermarket(2, ONE);
        i0 = createBoughtItemWithIdAndSupermarket(0, ONE);

        ItemGraph itemGraph = createNewItemGraphWithSupermarket(ONE);
        addItemsToItemGraphThatWouldProduceACycleOfThree(itemGraph, i1, i2, i0);

        ShoppingListServer shoppingList = createShoppingListServerWithNItems(2);

        List<Item> sortedList = magicSortHelper.sort(itemGraph, shoppingList);

        for (Item item : shoppingList.getItems()) {
            assertTrue("Item that was to be sorted is not contained in the sorted list", sortedList.contains(item));
        }
    }

    @Test
    public void shoppingListServerAddItemAndGetItemsWorkTogether() {
        Item item = createItemWithId(1);

        ShoppingListServer shoppingListServer = new ShoppingListServer(0);
        shoppingListServer.addItem(item);

        assertTrue("An item added via addItem is not contained in the list of item obtained via getItems", shoppingListServer.getItems().contains(item));
    }

    @Test
    public void simpleListIsSortedCorrectlyIfIsNoConflictingDataWasAdded__ItemsWereBoughtImmediatelyOneAfterTheOtherBefore() {
        ItemGraph itemGraph = createCyclicFreeDataWithSixVertices();

        Item item1 = createItemWithId(1);
        Item item3 = createItemWithId(3);
        List<Item> shoppingListItems = new ArrayList<>(2);
        shoppingListItems.add(item3);
        shoppingListItems.add(item1);

        ShoppingListServer shoppingListServer = new ShoppingListServer(0, shoppingListItems);

        List<Item> sortedList = magicSortHelper.sort(itemGraph, shoppingListServer);

        assertEquals("The sorted list has a different size than before", 2, sortedList.size());

        int iteration = 0;
        for (Item item : sortedList) {
            if (iteration == 0) {
                assertEquals("Item was not sorted correctly", item1.getName(), item.getName());
            } else {
                assertEquals("An extra item was added while sorting", 1, iteration);
                assertEquals("Item was not sorted correctly", item3.getName(), item.getName());
            }
            iteration++;
        }
    }

    @Test
    public void simpleListIsSortedCorrectlyIfIsNoConflictingDataWasAdded__ItemsWereOnlyBoughtWithAnOtherItemInBetween() {
        ItemGraph itemGraph = createCyclicFreeDataWithSixVertices();

        Item item4 = createItemWithId(4);
        Item item5 = createItemWithId(5);

        List<Item> shoppingListItems = new ArrayList<>(2);
        shoppingListItems.add(item5);
        shoppingListItems.add(item4);

        ShoppingListServer shoppingListServer = new ShoppingListServer(0, shoppingListItems);

        List<Item> sortedList = magicSortHelper.sort(itemGraph, shoppingListServer);

        assertEquals("The sorted list has a different size than before", 2, sortedList.size());
        int iteration = 0;
        for (Item item : sortedList) {
            if (iteration == 0) {
                assertEquals("Item was not sorted correctly", item5.getName(), item.getName());
            } else {
                assertEquals("An extra item was added while sorting", 1, iteration);
                assertEquals("Item was not sorted correctly", item4.getName(), item.getName());
            }
            iteration++;
        }
    }

    @Test
    public void ifNoDataIsAvailableTheOriginalListShouldNotBeAltered() {
        ShoppingListServer shoppingListServer = createShoppingListServerWithNItems(5);

        // copy the names before sorting, because the list might be altered
        String[] unSortedNames = new String[5];
        int j = 0;
        for (Item item : shoppingListServer.getItems()) {
            unSortedNames[j++] = item.getName();
        }

        ItemGraph itemGraph = createNewItemGraphWithSupermarket(ONE);
        List<Item> sortedList = magicSortHelper.sort(itemGraph, shoppingListServer);

        String[] sortedNames = new String[5];
        int i = 0;
        for (Item item : sortedList) {
            sortedNames[i++] = item.getName();
        }

        assertArrayEquals("The list has been re-ordered although no data was available", unSortedNames, sortedNames);
    }

    @Test
    public void ifInsufficientDataIsAvailableTheOriginalShoppingListShouldNotBeAltered() {
        ItemGraph itemGraph = createCyclicFreeDataWithSixVertices();

        Item item2 = createItemWithId(2);
        Item item3 = createItemWithId(3);

        List<Item> shoppingListItems = new ArrayList<>(2);
        shoppingListItems.add(item2);
        shoppingListItems.add(item3);

        ShoppingListServer shoppingListServer = new ShoppingListServer(0, shoppingListItems);
        List<Item> sortedList = magicSortHelper.sort(itemGraph, shoppingListServer);

        assertEquals("The sorted list has a different size than before", 2, sortedList.size());
        int iteration = 0;
        for (Item item : sortedList) {
            if (iteration == 0) {
                assertEquals("Item was not sorted correctly", item2.getName(), item.getName());
            } else {
                assertEquals("An extra item was added while sorting", 1, iteration);
                assertEquals("Item was not sorted correctly", item3.getName(), item.getName());
            }
            iteration++;
        }

        /*And the same test the other way round*/

        shoppingListItems = new ArrayList<>(2);
        shoppingListItems.add(item3);
        shoppingListItems.add(item2);

        shoppingListServer = new ShoppingListServer(0, shoppingListItems);
        sortedList = magicSortHelper.sort(itemGraph, shoppingListServer);

        assertEquals("The sorted list has a different size than before", 2, sortedList.size());
        iteration = 0;
        for (Item item : sortedList) {
            if (iteration == 0) {
                assertEquals("Item was not sorted correctly", item3.getName(), item.getName());
            } else {
                assertEquals("An extra item was added while sorting", 1, iteration);
                assertEquals("Item was not sorted correctly", item2.getName(), item.getName());
            }
            iteration++;
        }
    }

    @Test
    public void sortingWithMagicSortHelperDoesNotAlterTheOriginalListButWorksOnACopy() {
        ItemGraph itemGraph = createCyclicFreeDataWithSixVertices();
        int n = 6;
        ShoppingListServer shoppingList = createShoppingListServerWithNItemsMixedUp(n);
        ShoppingListServer shoppingListCopy = createShoppingListServerWithNItemsMixedUp(n);
        List<Item> sorted = magicSortHelper.sort(itemGraph, shoppingListCopy);

        String[] originalNames = new String[n];
        String[] copiedNames = new String[n];
        String[] sortedNames = new String[n];

        int index = 0;
        for (Item item : shoppingList.getItems()) {
            originalNames[index++] = item.getName();
        }
        index = 0;
        for (Item item : shoppingListCopy.getItems()) {
            copiedNames[index++] = item.getName();
        }
        index = 0;
        for (Item item : sorted) {
            sortedNames[index++] = item.getName();
        }

        assertArrayEquals("The shopping list was altered while sorting", originalNames, copiedNames);
        assertFalse("The list was not sorted at all. This might be because the sorting algorithm or the test case are broken.", Arrays.equals(originalNames, sortedNames));
    }

    @Test
    public void sortingDoesNotSortAnItemBeforeAnotherAlthoughItWasAlwaysBoughtTheOtherWayRound() {
        ItemGraph itemGraph = createCyclicFreeDataWithSixVertices();
        sortSixItemsAndMakeSureTheSortingFitsToTheDefaultCyclicFreeItemGraphWithSixEdges(itemGraph);
    }

    @Test
    public void createCyclicFreeItemGraph_AddSomeInconsistentDataAndSort() {
        ItemGraph itemGraph = createCyclicFreeDataWithSixVertices();
        for (int i = 0; i < 10; i++) {
            // make sure the consistent data was added often enough
            addCycleFreeDataWithSixVerticesToItemGraph(itemGraph);
        }

        BoughtItem i0, i1, i2, i3, i4, i5;
        i0 = createBoughtItemWithIdAndSupermarket(0, ONE);
        i1 = createBoughtItemWithIdAndSupermarket(1, ONE);
        i2 = createBoughtItemWithIdAndSupermarket(2, ONE);
        i3 = createBoughtItemWithIdAndSupermarket(3, ONE);
        i4 = createBoughtItemWithIdAndSupermarket(4, ONE);
        i5 = createBoughtItemWithIdAndSupermarket(5, ONE);

        List<BoughtItem> first, second, third;

        first = new ArrayList<>(3);
        first.add(i4);
        first.add(i3);
        first.add(i0);

        second = new ArrayList<>(3);
        second.add(i5);
        second.add(i1);
        second.add(i2);

        third = new ArrayList<>(6);
        third.add(i5);
        third.add(i4);
        third.add(i3);
        third.add(i2);
        third.add(i1);
        third.add(i0);

        itemGraph.addBoughtItems(first);
        itemGraph.addBoughtItems(second);
        itemGraph.addBoughtItems(third);

        // This data should not have changed much in the item graph, so sorting is still the same

        sortSixItemsAndMakeSureTheSortingFitsToTheDefaultCyclicFreeItemGraphWithSixEdges(itemGraph);
    }

    // Helper method for a limited number of related tests
    private void sortSixItemsAndMakeSureTheSortingFitsToTheDefaultCyclicFreeItemGraphWithSixEdges(ItemGraph itemGraph) {
        int n = 6;
        ShoppingListServer shoppingList = createShoppingListServerWithNItems(n);
        List<Item> sortedList = magicSortHelper.sort(itemGraph, shoppingList);

        ArrayList<String> orderedItemNames = new ArrayList<>(6);
        for (Item item : sortedList) {
            orderedItemNames.add(item.getName());
        }

        /*
        * There are several possibilities how the items can be ordered
        * 0-1-2-5-3-4 OR
        * 0-1-5-2-3-4 OR
        * 0-1-5-3-2-4 OR
        * 0-1-5-3-4-2
        */

        assertEquals("i0 is not the first item, although it should be", "i0", orderedItemNames.get(0));
        assertEquals("i1 is not the second item, although it should be", "i1", orderedItemNames.get(1));
        if (orderedItemNames.get(2).equals("i2")) {
            assertEquals("i5 is not the fourth item, although it should be, as i2 was the third item", "i5", orderedItemNames.get(3));
            assertEquals("i3 is not the fifth item, although it should be, as i2 was the third item", "i3", orderedItemNames.get(4));
            assertEquals("i4 is not the sixth item, although it should be, as i2 was the third item", "i4", orderedItemNames.get(5));
        } else {
            assertEquals("i5 is not the third item, although it should be, as i2 was not the third item", "i5", orderedItemNames.get(2));
            if (orderedItemNames.get(3).equals("i2")) {
                assertEquals("i3 is not the fifth item, although it should be, as i2 was the fourth item", "i3", orderedItemNames.get(4));
                assertEquals("i4 is not the sixth item, although it should be, as i2 was the fourth item", "i4", orderedItemNames.get(5));
            } else {
                assertEquals("i3 is not the fourth item, although it should be, as i2 was not the third or fourth item", "i3", orderedItemNames.get(3));
                if (orderedItemNames.get(4).equals("i2")) {
                    assertEquals("i4 is not the sixth item, although it should be, as i2 was the fifth item", "i4", orderedItemNames.get(5));
                } else {
                    assertEquals("i4 is not the fifth item, although it should be, as i2 was not the third, fourth or fifth item", "i4", orderedItemNames.get(4));
                    assertEquals("i2 is not the sixth item, although it should be, as it was not the third, fourth or fifth item either", "i2", orderedItemNames.get(5));
                }
            }
        }
    }

    @Test(timeout = 5000)
    public void addTheSameItemTwiceAndThenSortTwoItems() {
        ItemGraph itemGraph = createNewItemGraphWithSupermarket(ONE);
        List<BoughtItem> items = createBoughtItems(3, ONE);
        BoughtItem i0 = items.get(0);
        items.add(i0); // i0 is now contained twice - this is something that can definitely happen
        itemGraph.addBoughtItems(items);

        Item i1 = createItemWithId(1);
        Item i2 = createItemWithId(2);

        List<Item> listToSort = new ArrayList<>(2);
        listToSort.add(i1);
        listToSort.add(i2);

        ShoppingListServer shoppingListServer = new ShoppingListServer(0, listToSort);

        List<Item> sorted = magicSortHelper.sort(itemGraph, shoppingListServer);

        assertEquals("The size of the sorted shopping list has changed while sorting", 2, sorted.size());
        Item sortedItem1 = sorted.get(0);
        assertEquals("The name of the first item that is to be sorted has changed while sorting", "i1", sortedItem1.getName());
        Item sortedItem2 = sorted.get(1);
        assertEquals("The name of the second item that is to be sorted has changed while sorting", "i2", sortedItem2.getName());
    }

    @Test
    public void sortAListThatContainsTheSameItemTwice() {
        sortAListThatContainsTheSameItemTwice(false);
    }

    @Test
    public void sortAMixedUpListThatContainsTheSameItemTwice() {
        sortAListThatContainsTheSameItemTwice(true);
    }

    // Helper method for a limited number of related tests
    private void sortAListThatContainsTheSameItemTwice(boolean mixedUp) {
        ItemGraph itemGraph = createCyclicFreeDataWithSixVertices();
        Item i0 = createItemWithId(0);
        Item i1 = createItemWithId(1);
        Item i3 = createItemWithId(3);
        Item i1V2 = createItemWithId(1);
        i1V2.setServerId(4);
        i1V2.setID(4);

        List<Item> sorted;
        if (mixedUp) {
            sorted = magicSortHelper.sort(itemGraph, i1V2, i0, i3, i1);
        } else {
            sorted = magicSortHelper.sort(itemGraph, i0, i1, i3, i1V2);
        }

        assertEquals("Not all items that were to be sorted were returned", 4, sorted.size());
        assertTrue("i0 not contained in the sorted list", sorted.contains(i0));
        assertTrue("i1 not contained in the sorted list", sorted.contains(i1));
        assertTrue("i3 not contained in the sorted list", sorted.contains(i3));
        assertTrue("i1 not contained in the sorted list a second time", sorted.contains(i1V2));

        assertEquals("i0 not sorted at the right position", "i0", sorted.get(0).getName());
        assertEquals("i1 not sorted at the right position", "i1", sorted.get(1).getName());
        assertEquals("i1 not sorted at the right position a second time", "i1", sorted.get(2).getName());
        assertEquals("i3 not sorted at the right position", "i3", sorted.get(3).getName());
    }

    @Test
    public void ifNoDataIsAvailableForTheCurrentSupermarketTheGlobalSupermarketChainDataShouldBeUsed() {
        ItemGraph itemGraph = createCyclicFreeDataWithSixVertices();

        ShoppingListServer shoppingListServer = createShoppingListServerWithNItemsMixedUp(3);

        SortingRequest sortingRequest = new SortingRequest(THREE, THREE); // This supermarket belongs to the same chain as ONE
        List<Item> sorted = magicSortHelper.sort(itemGraph, shoppingListServer, sortingRequest);

        assertEquals("The first item was not sorted correctly according to the global data of this supermarket", "i0", sorted.get(0).getName());
        assertEquals("The second item was not sorted correctly according to the global data of this supermarket", "i1", sorted.get(1).getName());
        assertEquals("The third item was not sorted correctly according to the global data of this supermarket", "i2", sorted.get(2).getName());
    }

}