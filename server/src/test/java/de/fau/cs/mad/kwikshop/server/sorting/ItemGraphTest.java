package de.fau.cs.mad.kwikshop.server.sorting;

import org.junit.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Random;
import java.util.Set;

import de.fau.cs.mad.kwikshop.common.Item;
import de.fau.cs.mad.kwikshop.common.ShoppingListServer;
import de.fau.cs.mad.kwikshop.common.sorting.BoughtItem;
import de.fau.cs.mad.kwikshop.common.sorting.SortingRequest;
import static org.junit.Assert.*;

public class ItemGraphTest {

    private final String ONE = "ONE";
    private final String TWO = "TWO";
    private final String THREE = "THREE";
    private final String FOUR = "FOUR";
    private final String CHAIN_ONE = "CHAIN_ONE";
    private final String CHAIN_TWO = "CHAIN_TWO";

    private ItemGraph createNewItemGraph() {
        return new ItemGraph(new DAODummyHelper());
    }

    private ItemGraph createNewItemGraphWithSupermarket(String supermarketPlaceId) {
        ItemGraph itemGraph = new ItemGraph(new DAODummyHelper());
        itemGraph.setSupermarket(supermarketPlaceId, supermarketPlaceId);
        return itemGraph;
    }

    @Test
    public void newItemGraphShouldNotHaveAnyEdges() {
        ItemGraph itemGraph = createNewItemGraph();
        Set<Edge> edges = itemGraph.getEdges();
        assertNotNull("getEdges returned null instead of an empty set", edges);
        assertTrue("Newly created ItemGraph already had edges", edges.isEmpty());
    }

    @Test
    public void newItemGraphShouldHaveNoVerticesOrStartAndEndVertices() {
        ItemGraph itemGraph = createNewItemGraph();
        Set<BoughtItem> vertices = itemGraph.getVertices();
        assertNotNull("getVertices returned null instead of an empty set or a set containing only start and end vertices", vertices);
        boolean isEmpty = vertices.isEmpty();
        if (!isEmpty) {
            assertEquals("getVertices should return either an empty set or a set containing only start and end vertices. But it is not empty and not containing 2 elements.", 2, vertices.size());
            assertTrue("\"getVertices should return either an empty set or a set containing only start and end vertices. But it is not containing the start vertex", vertices.contains(itemGraph.getDaoHelper().getStartBoughtItem()));
            assertTrue("\"getVertices should return either an empty set or a set containing only start and end vertices. But it is not containing the end vertex", vertices.contains(itemGraph.getDaoHelper().getEndBoughtItem()));
        }
    }

    @Test
    public void newItemGraphShouldNotHaveASupermarket() {
        ItemGraph itemGraph = createNewItemGraph();
        assertNull("Newly created ItemGraph already has supermarket set", itemGraph.getSupermarket());
    }

    @Test
    public void setAndGetSupermarketTest() {
        ItemGraph itemGraph = createNewItemGraph();
        Supermarket supermarket = itemGraph.getDaoHelper().getSupermarketByPlaceID(ONE);
        itemGraph.setSupermarket(supermarket.getPlaceId(), supermarket.getPlaceId());
        assertEquals("The returned supermarket by getSupermarket should be the same as the supermarket that was set", supermarket.getPlaceId(), itemGraph.getSupermarket().getPlaceId());
    }

    @Test
    public void setSupermarketReturnsCorrectValue() {
        ItemGraph itemGraph = createNewItemGraph();
        Supermarket supermarket = itemGraph.getDaoHelper().getSupermarketByPlaceID(ONE);
        assertFalse("setSupermarket returned true although it is not a new supermarket", itemGraph.setSupermarket(supermarket.getPlaceId(), supermarket.getPlaceId()));
        assertFalse("setSupermarket returned true although it is not a new supermarket", itemGraph.setSupermarket(supermarket.getPlaceId(), supermarket.getPlaceId()));
        assertTrue("setSupermarket returned false although it is a new supermarket", itemGraph.setSupermarket("blah", "blah"));
        assertFalse("setSupermarket returned true although it is not a new supermarket", itemGraph.setSupermarket("blah", "blah"));
    }

    @Test
    public void createOrUpdateEdgeForEmptyGraphShouldReturnAEdge() {
        ItemGraph itemGraph = createNewItemGraph();
        BoughtItem i1 = createBoughtItemWithIdAndSupermarket(1, ONE);
        BoughtItem i2 = createBoughtItemWithIdAndSupermarket(2, ONE);
        Supermarket supermarket = itemGraph.getDaoHelper().getSupermarketByPlaceID(ONE);

        Edge edge = itemGraph.createOrUpdateEdge(i1, i2, supermarket);
        assertNotNull("createOrUpdateEdge returns null", edge);
    }

    @Test
    public void createdEdgeShouldBeContainedInResultOfGetEdges() {
        ItemGraph itemGraph = createNewItemGraph();
        BoughtItem i1 = createBoughtItemWithIdAndSupermarket(1, ONE);
        BoughtItem i2 = createBoughtItemWithIdAndSupermarket(2, ONE);
        Supermarket supermarket = itemGraph.getDaoHelper().getSupermarketByPlaceID(ONE);

        Edge edge = itemGraph.createOrUpdateEdge(i1, i2, supermarket);
        itemGraph.setSupermarket(ONE, ONE);
        itemGraph.update();

        Set<Edge> edges = itemGraph.getEdges();
        assertNotNull("getEdges returns null although an edge was just added", edges);
        assertTrue("newly added edge is not contained in the item graph", edges.contains(edge));
        assertEquals("getEdges returns more than just one edge, although no other edges were added", 1, edges.size());
    }

    @Test
    public void getVerticesReturnsTheItemsThatWereAddedBeforeForOneItem() {
        getVerticesReturnsTheItemsThatWereAddedBeforeForNItems(1);
    }

    @Test
    public void getVerticesReturnsTheItemsThatWereAddedBeforeForTwoItems() {
        getVerticesReturnsTheItemsThatWereAddedBeforeForNItems(2);
    }

    @Test
    public void getVerticesReturnsTheItemsThatWereAddedBeforeForFiveItems() {
        getVerticesReturnsTheItemsThatWereAddedBeforeForNItems(5);
    }

    private void getVerticesReturnsTheItemsThatWereAddedBeforeForNItems(int n) {
        ItemGraph itemGraph = createNewItemGraph();
        itemGraph.setSupermarket(ONE, ONE);
        List<BoughtItem> items = createBoughtItems(n, ONE);
        itemGraph.addBoughtItems(items);

        Set<BoughtItem> vertices = itemGraph.getVertices();
        assertNotNull("getVertices returns null although items were added", vertices);
        assertEquals("getVertices does not have size " + n + 2 + "although " + n + "item(s) were added (+start/end)", n + 2, vertices.size());
        for (int i = 0; i < n; i++) {
            assertTrue("The " + i + "th item is not contained in getVertices", vertices.contains(items.get(i)));
        }
    }

    private List<BoughtItem> createBoughtItems(int numberOfItemsToCreate, String supermarketPlaceId) {
        List<BoughtItem> items = new ArrayList<>(numberOfItemsToCreate);
        for (int i = 0; i < numberOfItemsToCreate; i++) {
            BoughtItem item = createBoughtItemWithIdAndSupermarket(i, supermarketPlaceId);
            item.setId(i);
            items.add(item);
        }
        return items;
    }

    @Test
    public void childIsSetCorrectlyForAListOfTwoItems() {
        List<BoughtItem> items = createBoughtItems(2, ONE);
        ItemGraph itemGraph = createNewItemGraphWithSupermarket(ONE);
        itemGraph.addBoughtItems(items);
        BoughtItem i0 = items.get(0);
        BoughtItem i1 = items.get(1);
        List<BoughtItem> i0sChildren = itemGraph.getChildren(i0);
        assertTrue("item i1 is not recognized as i0's child", i0sChildren.contains(i1));
        List<BoughtItem> i1sChildren = itemGraph.getChildren(i1);
        assertFalse("item i0 is recognized as child of i1 incorrectly", i1sChildren.contains(i0));
    }

    @Test
    public void parentIsSetCorrectlyForAListOfTwoItems() {
        List<BoughtItem> items = createBoughtItems(2, ONE);
        ItemGraph itemGraph = createNewItemGraphWithSupermarket(ONE);
        itemGraph.addBoughtItems(items);
        BoughtItem i0 = items.get(0);
        BoughtItem i1 = items.get(1);
        List<BoughtItem> i1sParents = itemGraph.getParents(i1);
        assertTrue("item i0 is not recognized as i1's parent", i1sParents.contains(i0));
        List<BoughtItem> i0sParents = itemGraph.getParents(i0);
        assertFalse("item i1 is recognized as parent of i0 incorrectly", i0sParents.contains(i1));
    }

    @Test
    public void parentIsSetCorrectlyForAListOfThreeItems() {
        List<BoughtItem> items = createBoughtItems(3, ONE);
        ItemGraph itemGraph = createNewItemGraphWithSupermarket(ONE);
        itemGraph.addBoughtItems(items);
        BoughtItem i0 = items.get(0);
        BoughtItem i1 = items.get(1);
        BoughtItem i2 = items.get(2);
        List<BoughtItem> i1sParents = itemGraph.getParents(i1);
        assertTrue("item i0 is not recognized as i1's parent", i1sParents.contains(i0));
        assertFalse("item i2 is recognized as parent of i1 incorretclty", i1sParents.contains(i2));
        List<BoughtItem> i0sParents = itemGraph.getParents(i0);
        assertFalse("item i1 is recognized as parent of i0 incorrectly", i0sParents.contains(i1));
        assertFalse("item i2 is recognized as parent of i0 incorrectly", i0sParents.contains(i2));
        List<BoughtItem> i2sParents = itemGraph.getParents(i2);
        assertTrue("item i1 is not recognized as i2's parent", i2sParents.contains(i1));
        assertFalse("item i0 is recoginzed as parent of i2 incorrectly", i2sParents.contains(i0));
    }

    @Test
    public void getSiblingsWorksForTwoSimpleLists() {
        List<BoughtItem> items = createBoughtItems(3, ONE);
        ItemGraph itemGraph = createNewItemGraphWithSupermarket(ONE);
        BoughtItem i0, i1, i2;
        i0 = items.get(0);
        i1 = items.get(1);
        i2 = items.get(2);
        List<BoughtItem> firstPurchase, secondPurchase;
        firstPurchase = new ArrayList<>(2);
        secondPurchase = new ArrayList<>(2);

        firstPurchase.add(i0);
        firstPurchase.add(i1);

        secondPurchase.add(i0);
        secondPurchase.add(i2);

        itemGraph.addBoughtItems(firstPurchase);
        itemGraph.addBoughtItems(secondPurchase);

        assertTrue("i2 is not recognized as sibling for i1", itemGraph.getSiblings(i1).contains(i2));
        assertTrue("i1 is not recognized as sibling for i2", itemGraph.getSiblings(i2).contains(i1));
    }

    @Test
    public void getSiblingsDoesntReturnFalseSiblings() {
        List<BoughtItem> items = createBoughtItems(2, ONE);
        ItemGraph itemGraph = createNewItemGraphWithSupermarket(ONE);
        itemGraph.addBoughtItems(items);

        BoughtItem i0, i1;
        i0 = items.get(0);
        i1 = items.get(1);

        List<BoughtItem> i0sSiblings, i1sSiblings;
        i0sSiblings = itemGraph.getSiblings(i0);
        i1sSiblings = itemGraph.getSiblings(i1);

        assertFalse("i0 is contained in i0's siblings incorrectly", i0sSiblings.contains(i0));
        assertFalse("i0 is contained in i1's siblings incorrectly", i1sSiblings.contains(i0));
        assertFalse("i1 is contained in i0's siblings incorrectly", i0sSiblings.contains(i1));
        assertFalse("i1 is contained in i1's siblings incorrectly", i1sSiblings.contains(i1));
    }

    @Test
    public void edgeFromToExistsDoesDetectEdges() {
        ItemGraph itemGraph = createNewItemGraphWithSupermarket(ONE);
        List<BoughtItem> items = createBoughtItems(2, ONE);
        itemGraph.addBoughtItems(items);
        assertTrue("edge not detected", itemGraph.edgeFromToExists(items.get(0), items.get(1)));
    }

    @Test
    public void edgeFromToExistsDoesNotDetectNonExistingEdges() {
        ItemGraph itemGraph = createNewItemGraphWithSupermarket(ONE);
        List<BoughtItem> items = createBoughtItems(2, ONE);
        itemGraph.addBoughtItems(items);
        assertFalse("non existing edge detected", itemGraph.edgeFromToExists(items.get(1), items.get(0)));
    }

    @Test
    public void executeAlgorithmDoesNotCrash() {
        ItemGraph itemGraph = createNewItemGraphWithSupermarket(ONE);
        List<BoughtItem> items = createBoughtItems(2, ONE);
        itemGraph.executeAlgorithm(new IndirectEdgeInsertion(), items);
    }


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

    private void nItemsAreSortedIdenticallyASecondTime(int n, boolean mixItemsBeforeSorting) {
        List<BoughtItem> items = createBoughtItems(n, ONE);
        ItemGraph itemGraph = createNewItemGraphWithSupermarket(ONE);
        itemGraph.addBoughtItems(items);

        Algorithm magicSort = new MagicSort();

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

        SortingRequest sortingRequest = new SortingRequest(ONE, ONE);
        ShoppingListServer sortedList = itemGraph.sort(magicSort, shoppingListServer, sortingRequest);
        List<Item> sortedItems = new ArrayList<>();
        for(Item item: sortedList.getItems()) {
            sortedItems.add(item);
        }

        /* Sort according to the order of each Item */
        Collections.sort(sortedItems);

        for (int i = 0; i < n; i++) {
            assertEquals("A identical list was sorted different as before, although no different data is available. The lists first differ at element " + i, items.get(i).getName(), sortedItems.get(i).getName());
        }
    }

    private ShoppingListServer createShoppingListServerWithNItems(int n) {
        ArrayList<Item> items = new ArrayList<>();
        for (int i = 0; i < n; i++) {
            Item item = createItemWithId(i);
            items.add(item);
        }
        ShoppingListServer shoppingListServer = new ShoppingListServer(0, items);
        return shoppingListServer;
    }

    private ShoppingListServer createShoppingListServerWithNItemsMixedUp(int n) {
        List<Item> orderedItems = new ArrayList<>(n);
        for (int i = 0; i < n; i++) {
            Item item = createItemWithId(i);
            orderedItems.add(item);
        }

        Random random = new Random(n*n); // random generator with some random seed

        ArrayList<Item> randomItems = new ArrayList<>();
        while (!orderedItems.isEmpty()) {
            int index = random.nextInt(orderedItems.size());
            Item item = orderedItems.remove(index);
            randomItems.add(item);
        }
        ShoppingListServer shoppingListServer = new ShoppingListServer(0, randomItems);

        return shoppingListServer;
    }

    @Test
    public void cycleOfThreeItemsShouldNotOccur() {
        BoughtItem i1, i2, i3;
        i1 = createBoughtItemWithIdAndSupermarket(1, ONE);
        i2 = createBoughtItemWithIdAndSupermarket(2, ONE);
        i3 = createBoughtItemWithIdAndSupermarket(3, ONE);

        ItemGraph itemGraph = createNewItemGraphWithSupermarket(ONE);
        addItemsToItemGraphThatWouldProduceACycleOfThree(itemGraph, i1, i2, i3);

        /*Now items were "bought in a cycle", but it is crucial that no cycles are contained in the
        item graph -> if there are edges i1->i2 and i2->i3, i3->i1 must not exist; only two of these
        three edges may exist at one time*/
        boolean i1ToI2Exists, i2ToI3Exists, i3ToI1Exists;
        i1ToI2Exists = itemGraph.edgeFromToExists(i1, i2);
        i2ToI3Exists = itemGraph.edgeFromToExists(i2, i3);
        i3ToI1Exists = itemGraph.edgeFromToExists(i3, i1);

        if (i1ToI2Exists) {
            if (i2ToI3Exists) {
                assertFalse("Cycle in item graph detected", i3ToI1Exists);
            } else {
                assertTrue("Missing edge in item Graph", i3ToI1Exists);
            }
        } else {
            assertTrue("Missing edge in item Graph", i2ToI3Exists);
            assertTrue("Missing edge in item Graph", i3ToI1Exists);
        }
    }

    private void addItemsToItemGraphThatWouldProduceACycleOfThree(ItemGraph itemGraph, BoughtItem i1, BoughtItem i2, BoughtItem i3) {
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

    @Test(timeout = 5000)
    public void sortWillReturnSomethingEvenIfTheDataIsInsufficient() {
        BoughtItem i1, i2, i0;
        i1 = createBoughtItemWithIdAndSupermarket(1, ONE);
        i2 = createBoughtItemWithIdAndSupermarket(2, ONE);
        i0 = createBoughtItemWithIdAndSupermarket(0, ONE);

        ItemGraph itemGraph = createNewItemGraphWithSupermarket(ONE);
        addItemsToItemGraphThatWouldProduceACycleOfThree(itemGraph, i1, i2, i0);

        ShoppingListServer shoppingList = createShoppingListServerWithNItems(2);
        SortingRequest sortingRequest = new SortingRequest(ONE, ONE);
        Algorithm magicSort = new MagicSort();

        // create a new shopping list, because it might be overwritten in sort()
        ShoppingListServer sortedList = itemGraph.sort(magicSort, new ShoppingListServer(42, shoppingList.getItems()), sortingRequest);
        for (Item item : shoppingList.getItems()) {
            assertTrue("Item that was to be sorted is not contained in the sorted list", sortedList.getItems().contains(item));
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
    public void simpleListIsSortedCorrectlyIfIsNoConflictingDataWasAdded__ItemsWereBoughtImmedeatelyOneAfterTheOtherBefore() {
        ItemGraph itemGraph = createCyclicFreeDataWithSixVertices();

        SortingRequest sortingRequest = new SortingRequest(ONE, ONE);
        Algorithm magicSort = new MagicSort();
        Item item1 = createItemWithId(1);
        Item item3 = createItemWithId(3);
        List<Item> shoppingListItems = new ArrayList<>(2);
        shoppingListItems.add(item3);
        shoppingListItems.add(item1);

        ShoppingListServer shoppingListServer = new ShoppingListServer(0, shoppingListItems);

        ShoppingListServer sortedList = itemGraph.sort(magicSort, shoppingListServer, sortingRequest);

        assertEquals("The sorted list has a different size than before", 2, sortedList.size());
        List<Item> items = new ArrayList<Item>(sortedList.getItems());
        Collections.sort(items);
        int iteration = 0;
        for (Item item : items) {
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

        SortingRequest sortingRequest = new SortingRequest(ONE, ONE);
        Algorithm magicSort = new MagicSort();

        Item item4 = createItemWithId(4);
        Item item5 = createItemWithId(5);

        List<Item> shoppingListItems = new ArrayList<>(2);
        shoppingListItems.add(item5);
        shoppingListItems.add(item4);

        ShoppingListServer shoppingListServer = new ShoppingListServer(0, shoppingListItems);

        ShoppingListServer sortedList = itemGraph.sort(magicSort, shoppingListServer, sortingRequest);

        assertEquals("The sorted list has a different size than before", 2, sortedList.size());
        Collection<Item> items = sortedList.getItems();
        int iteration = 0;
        for (Item item : items) {
            if (iteration == 0) {
                assertEquals("Item was not sorted correctly", item5.getName(), item.getName());
            } else {
                assertEquals("An extra item was added while sorting", 1, iteration);
                assertEquals("Item was not sorted correctly", item4.getName(), item.getName());
            }
            iteration++;
        }
    }

    private ItemGraph createCyclicFreeDataWithSixVertices() {
        ItemGraph itemGraph = createNewItemGraphWithSupermarket(ONE);
        addCycleFreeDataWithSixVerticesToItemGraph(itemGraph);
        return itemGraph;
    }

    private void addCycleFreeDataWithSixVerticesToItemGraph(ItemGraph itemGraph) {
        BoughtItem i0, i1, i2, i3, i4, i5;
        i0 = createBoughtItemWithIdAndSupermarket(0, ONE);
        i1 = createBoughtItemWithIdAndSupermarket(1, ONE);
        i2 = createBoughtItemWithIdAndSupermarket(2, ONE);
        i3 = createBoughtItemWithIdAndSupermarket(3, ONE);
        i4 = createBoughtItemWithIdAndSupermarket(4, ONE);
        i5 = createBoughtItemWithIdAndSupermarket(5, ONE);

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

    @Test
    public void edgeShouldFlipIfItemsAreAddedTheOtherWayRoundMoreOften() {
        List<BoughtItem> items = createBoughtItems(2, ONE);
        BoughtItem i0 = items.get(0);
        BoughtItem i1 = items.get(1);

        List<BoughtItem> itemsOrderedTheOtherWayRound = new ArrayList<>(2);
        itemsOrderedTheOtherWayRound.add(i1);
        itemsOrderedTheOtherWayRound.add(i0);

        ItemGraph itemGraph = createNewItemGraphWithSupermarket(ONE);
        itemGraph.addBoughtItems(items);

        assertTrue("The edge was not added for the first two items", itemGraph.edgeFromToExists(i0, i1));

        itemGraph.addBoughtItems(itemsOrderedTheOtherWayRound);
        itemGraph.addBoughtItems(itemsOrderedTheOtherWayRound);

        assertTrue("The inverted edge has not been added after the data changed", itemGraph.edgeFromToExists(i1, i0));
        assertFalse("The edge, that was added for the first two items, didn't get removed after the data changed", itemGraph.edgeFromToExists(i0, i1));
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
        Algorithm magicSort = new MagicSort();
        SortingRequest sortingRequest = new SortingRequest(ONE, ONE);
        ShoppingListServer sortedList = itemGraph.sort(magicSort, shoppingListServer, sortingRequest);

        String[] sortedNames = new String[5];
        int i = 0;
        for (Item item : sortedList.getItems()) {
            sortedNames[i++] = item.getName();
        }

        assertArrayEquals("The list has been re-ordered although no data was available", unSortedNames, sortedNames);
    }

    @Test 
    public void ifInsufficientDataIsAvailableTheOriginalShoppingListShouldNotBeAltered() {
        ItemGraph itemGraph = createCyclicFreeDataWithSixVertices();

        SortingRequest sortingRequest = new SortingRequest(ONE, ONE);
        Algorithm magicSort = new MagicSort();

        Item item2 = createItemWithId(2);
        Item item3 = createItemWithId(3);

        List<Item> shoppingListItems = new ArrayList<>(2);
        shoppingListItems.add(item2);
        shoppingListItems.add(item3);

        ShoppingListServer shoppingListServer = new ShoppingListServer(0, shoppingListItems);
        ShoppingListServer sortedList = itemGraph.sort(magicSort, shoppingListServer, sortingRequest);

        assertEquals("The sorted list has a different size than before", 2, sortedList.size());
        Collection<Item> items = sortedList.getItems();
        int iteration = 0;
        for (Item item : items) {
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
        sortedList = itemGraph.sort(magicSort, shoppingListServer, sortingRequest);

        assertEquals("The sorted list has a different size than before", 2, sortedList.size());
        items = sortedList.getItems();
        iteration = 0;
        for (Item item : items) {
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
    @Ignore
    public void sortingDoesNotAlterTheOriginalListButWorksOnACopy() {
        ItemGraph itemGraph = createCyclicFreeDataWithSixVertices();
        int n = 6;
        ShoppingListServer shoppingList = createShoppingListServerWithNItems(n);
        ShoppingListServer sorted = itemGraph.sort(new MagicSort(), shoppingList, new SortingRequest(ONE, ONE));

        assertNotSame(shoppingList, sorted);
    }

    @Test
    public void sortingDoesNotSortAnItemBeforeAnotherAlthoughItWasAlwaysBoughtTheOtherWayRound() {
        ItemGraph itemGraph = createCyclicFreeDataWithSixVertices();
        sortSixItemsAndMakeSureTheSortingFitsToTheDefaultCyclicFreeItemGraphWithSixEdges(itemGraph);
    }

    private void sortSixItemsAndMakeSureTheSortingFitsToTheDefaultCyclicFreeItemGraphWithSixEdges(ItemGraph itemGraph) {
        int n = 6;
        ShoppingListServer shoppingList = createShoppingListServerWithNItems(n);
        ShoppingListServer sorted = itemGraph.sort(new MagicSort(), shoppingList, new SortingRequest(ONE, ONE));
        List<Item> sortedList = new ArrayList<Item>(sorted.getItems());
        Collections.sort(sortedList);

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

        Algorithm magicSort = new MagicSort();
        SortingRequest sortingRequest = new SortingRequest(ONE, ONE);
        ShoppingListServer shoppingListServer = new ShoppingListServer(0, listToSort);

        shoppingListServer = itemGraph.sort(magicSort, shoppingListServer, sortingRequest);
        assertEquals("The size of the sorted shopping list has changed while sorting", 2, shoppingListServer.getItems().size());
        Item sortedItem1 = (Item) shoppingListServer.getItems().toArray()[0];
        assertEquals("The name of the first item that is to be sorted has changed while sorting", "i1", sortedItem1.getName());
        Item sortedItem2 = (Item) shoppingListServer.getItems().toArray()[1];
        assertEquals("The name of the second item that is to be sorted has changed while sorting", "i2", sortedItem2.getName());
    }

    private Item createItemWithId(int id) {
        Item item = new Item();
        item.setName("i" + id);
        item.setID(id);
        item.setServerId(id);
        return item;
    }

    private BoughtItem createBoughtItemWithIdAndSupermarket(int id, String supermarketPlaceId) {
        BoughtItem item = new BoughtItem("i" + id, supermarketPlaceId, supermarketPlaceId);
        item.setId(id);
        return item;
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

    private class DAODummyHelper implements DAOHelper {

        private final Supermarket defaultSupermarketOne;
        private final Supermarket defaultSupermarketTwo;
        private final Supermarket defaultSupermarketThree;
        private final Supermarket defaultSupermarketFour;
        private final SupermarketChain defaultSupermarketChainOne;
        private final Supermarket defaultSupermarketChainOneGlobalSupermarket;
        private final SupermarketChain defaultSupermarketChainTwo;
        private final Supermarket defaultSupermarketChainTwoGlobalSupermarket;

        private final HashMap<String, List<Edge>> edges;
        private final HashMap<String, Supermarket> supermarkets;

        private final BoughtItem startBoughtItem;
        private final BoughtItem endBoughtItem;
        private final HashMap<String, BoughtItem> boughtItems;


        public DAODummyHelper() {
            defaultSupermarketChainOne = new SupermarketChain();
            defaultSupermarketChainOne.setId(1);
            defaultSupermarketChainOne.setName(CHAIN_ONE);
            defaultSupermarketChainOneGlobalSupermarket = new Supermarket();
            defaultSupermarketChainOneGlobalSupermarket.setId(-1);
            defaultSupermarketChainOneGlobalSupermarket.setPlaceId(CHAIN_ONE);

            defaultSupermarketChainTwo = new SupermarketChain();
            defaultSupermarketChainTwo.setId(2);
            defaultSupermarketChainTwo.setName(CHAIN_TWO);
            defaultSupermarketChainTwoGlobalSupermarket = new Supermarket();
            defaultSupermarketChainTwoGlobalSupermarket.setId(-2);
            defaultSupermarketChainTwoGlobalSupermarket.setPlaceId(CHAIN_TWO);

            defaultSupermarketOne = new Supermarket();
            defaultSupermarketOne.setId(1);
            defaultSupermarketOne.setPlaceId(ONE);
            defaultSupermarketOne.setSupermarketChain(defaultSupermarketChainOne);

            defaultSupermarketTwo = new Supermarket();
            defaultSupermarketTwo.setId(2);
            defaultSupermarketTwo.setPlaceId(TWO);
            defaultSupermarketTwo.setSupermarketChain(defaultSupermarketChainTwo);

            defaultSupermarketThree = new Supermarket();
            defaultSupermarketThree.setId(3);
            defaultSupermarketThree.setPlaceId(THREE);
            defaultSupermarketThree.setSupermarketChain(defaultSupermarketChainTwo);

            defaultSupermarketFour = new Supermarket();
            defaultSupermarketFour.setId(4);
            defaultSupermarketFour.setPlaceId(FOUR);

            supermarkets = new HashMap<>();
            supermarkets.put(ONE, defaultSupermarketOne);
            supermarkets.put(TWO, defaultSupermarketTwo);
            supermarkets.put(THREE, defaultSupermarketThree);
            supermarkets.put(FOUR, defaultSupermarketFour);

            edges = new HashMap<>();

            startBoughtItem = new BoughtItem(START_ITEM);
            endBoughtItem = new BoughtItem(END_ITEM);
            boughtItems = new HashMap<>();
            boughtItems.put(START_ITEM, startBoughtItem);
            boughtItems.put(END_ITEM, endBoughtItem);
        }

        @Override
        public Supermarket getSupermarketByPlaceID(String placeId) {
            return supermarkets.get(placeId);
        }

        @Override
        public List<SupermarketChain> getAllSupermarketChains() {
            List<SupermarketChain> supermarketChains = new ArrayList<>(2);
            supermarketChains.add(0, defaultSupermarketChainOne);
            supermarketChains.add(1, defaultSupermarketChainTwo);
            return supermarketChains;
        }

        @Override
        public void createSupermarket(Supermarket supermarket) {
            if (supermarkets.containsKey(supermarket.getPlaceId())) {
                throw new IllegalArgumentException("Supermarket already created");
            }
            supermarkets.put(supermarket.getPlaceId(), supermarket);
        }

        @Override
        public List<Edge> getEdgesBySupermarket(Supermarket supermarket) {
            if(supermarket == null) {
                return new ArrayList<Edge>();
            }

            List<Edge> edges = this.edges.get(supermarket.getPlaceId());
            if (edges == null) {
                return new ArrayList<Edge>();
            }
            return new ArrayList<>(edges);
        }

        @Override
        public Edge getEdgeByFromTo(BoughtItem from, BoughtItem to, Supermarket supermarket) {
            List<Edge> edges = getEdgesBySupermarket(supermarket);
            for (Edge edge : edges) {
                if (edge.getFrom().equals(from) && edge.getTo().equals(to)) {
                    return edge;
                }
            }
            return null;
        }

        @Override
        public List<Edge> getEdgesByTo(BoughtItem boughtItem, Supermarket supermarket) {
            List<Edge> allEdges = getEdgesBySupermarket(supermarket);
            List<Edge> foundEdges = new ArrayList<>();
            for (Edge edge : allEdges) {
                if (edge.getTo().equals(boughtItem)) {
                    foundEdges.add(edge);
                }
            }
            return foundEdges;
        }

        @Override
        public Edge createEdge(Edge edge) {
            String supermarketPlaceId = edge.getSupermarket().getPlaceId();
            List<Edge> edges = this.edges.get(supermarketPlaceId);
            if (edges == null) {
                // the specified supermarket doesn't have edges yet
                edges = new ArrayList<>();
                this.edges.put(supermarketPlaceId, edges);
            }
            edges.add(edge);
            return edge;
        }

        @Override
        public void deleteEdge(Edge edge) {
            List<Edge> edges = this.edges.get(edge.getSupermarket().getPlaceId());
            if (edges != null) {
                edges.remove(edge);
            }
        }

        @Override
        public BoughtItem getStartBoughtItem() {
            return startBoughtItem;
        }

        @Override
        public BoughtItem getEndBoughtItem() {
            return endBoughtItem;
        }

        @Override
        public BoughtItem getBoughtItemByName(String name) {
            return boughtItems.get(name);
        }

        @Override
        public void createBoughtItem(BoughtItem boughtItem) {
            if (!boughtItems.containsValue(boughtItem)) {
                boughtItems.put(boughtItem.getName(), boughtItem);
            }
        }

        @Override
        public Supermarket getGlobalSupermarketBySupermarketChain(SupermarketChain supermarketChain) {
            if (supermarketChain.getName().equals(CHAIN_ONE)) {
                return defaultSupermarketChainOneGlobalSupermarket;
            } else if (supermarketChain.getName().equals(CHAIN_TWO)) {
                return defaultSupermarketChainTwoGlobalSupermarket;
            } else {
                return null;
            }
        }

        @Override
        public Supermarket getGlobalSupermarket(SupermarketChain supermarketChain) {
            return getGlobalSupermarketBySupermarketChain(supermarketChain);
        }
    }
}