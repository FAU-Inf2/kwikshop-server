package de.fau.cs.mad.kwikshop.server.sorting;

import org.junit.*;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import de.fau.cs.mad.kwikshop.common.sorting.BoughtItem;
import de.fau.cs.mad.kwikshop.server.sorting.helperClasses.DAODummyHelper;
import de.fau.cs.mad.kwikshop.server.sorting.helperClasses.SortingTestSuperclass;
import de.fau.cs.mad.kwikshop.server.sorting.helperClasses.SupermarketHelper;

import static org.junit.Assert.*;

public class ItemGraphTest extends SortingTestSuperclass {

    /* Tests concerning whether the item graph behaves correctly */

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
        assertEquals("The created edge has a different start as the one handed over in the method call", i1, edge.getFrom());
        assertEquals("The created edge has a different end as the one handed over in the method call", i2, edge.getTo());
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

    // Helper method for a limited number of related tests
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
        assertFalse("item i0 is recognized as parent of i2 incorrectly", i2sParents.contains(i0));
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
    public void getSiblingsDoesNotReturnFalseSiblings() {
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
    public void indirectEdgesAreSetCorrectlyForThreeItems(){
        ItemGraph itemGraph = createNewItemGraphWithSupermarket(ONE);
        List<BoughtItem> items = createBoughtItems(3, ONE);
        BoughtItem i0 = items.get(0);
        BoughtItem i1 = items.get(1);
        BoughtItem i2 = items.get(2);
        itemGraph.addBoughtItems(items);

        assertTrue("There is no edge from i0 to i1", itemGraph.edgeFromToExists(i0, i1));
        assertTrue("There is no edge from i1 to i2", itemGraph.edgeFromToExists(i1, i2));
        assertTrue("There is no edge from start to i2", itemGraph.edgeFromToExists(itemGraph.getDaoHelper().getStartBoughtItem(), i2));
        assertTrue("There is no edge from start to i1", itemGraph.edgeFromToExists(itemGraph.getDaoHelper().getStartBoughtItem(), i1));
        assertTrue("There is no edge from i0 to i2", itemGraph.edgeFromToExists(i0, i2));
        assertTrue("There is no edge from i0 to end", itemGraph.edgeFromToExists(i0, itemGraph.getDaoHelper().getEndBoughtItem()));
        assertTrue("There is no edge from i1 to end", itemGraph.edgeFromToExists(i1, itemGraph.getDaoHelper().getEndBoughtItem()));
        assertTrue("There is no edge from i2 to end", itemGraph.edgeFromToExists(i2, itemGraph.getDaoHelper().getEndBoughtItem()));
        assertTrue("The indirect edge from i0 to i2 has the wrong distance", itemGraph.getDaoHelper().getEdgeByFromTo(i0, i2, itemGraph.getSupermarket()).getDistance() == 1);
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

    @Test
    public void cycleOfFourItemsShouldNotOccur() {
        BoughtItem i1, i2, i3, i4;
        i1 = createBoughtItemWithIdAndSupermarket(1, ONE);
        i2 = createBoughtItemWithIdAndSupermarket(2, ONE);
        i3 = createBoughtItemWithIdAndSupermarket(3, ONE);
        i4 = createBoughtItemWithIdAndSupermarket(4, ONE);

        ItemGraph itemGraph = createNewItemGraphWithSupermarket(ONE);
        addItemsToItemGraphThatWouldProduceACycle(itemGraph, i1, i2, i3, i4);

        /*Now items were "bought in a cycle", but it is crucial that no cycles are contained in the
        item graph -> if there are edges i1->i2, i2->i3 and i3->i4, i4->i1 must not exist; only
        three of these four edges may exist at one time*/
        boolean i1ToI2Exists, i2ToI3Exists, i3ToI4Exists, i4ToI1Exists;
        i1ToI2Exists = itemGraph.edgeFromToExists(i1, i2);
        i2ToI3Exists = itemGraph.edgeFromToExists(i2, i3);
        i3ToI4Exists = itemGraph.edgeFromToExists(i3, i4);
        i4ToI1Exists = itemGraph.edgeFromToExists(i4, i1);

        if (i1ToI2Exists) {
            if (i2ToI3Exists) {
                if (i3ToI4Exists) {
                    assertFalse("Cycle in item graph detected", i4ToI1Exists);
                } else {
                    assertTrue("Missing edge in item Graph", i4ToI1Exists);
                }
            } else {
                assertTrue("Missing edge in item Graph", i3ToI4Exists);
                assertTrue("Missing edge in item Graph", i4ToI1Exists);
            }
        } else {
            assertTrue("Missing edge in item Graph", i2ToI3Exists);
            assertTrue("Missing edge in item Graph", i3ToI4Exists);
            assertTrue("Missing edge in item Graph", i4ToI1Exists);
        }
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
    public void edgeShouldFlipInsteadOfGettingWeightZero() {
        ItemGraph itemGraph = createNewItemGraphWithSupermarket(ONE);
        List<BoughtItem> items = createBoughtItems(2, ONE);
        BoughtItem i0 = items.get(0);
        BoughtItem i1 = items.get(1);
        itemGraph.addBoughtItems(items);
        Collections.reverse(items);
        itemGraph.addBoughtItems(items);

        assertFalse("Old edge was not removed", itemGraph.edgeFromToExists(i0, i1));
        assertTrue("New edge was not inserted", itemGraph.edgeFromToExists(i1, i0));
        Set<Edge> edges = itemGraph.getEdgesFrom(i1);
        for (Edge edge : edges) {
            if (edge.getTo().equals(i0)) {
                assertEquals("Weight of the edge is set incorrectly", 1, edge.getWeight());
                return;
            }
        }
    }

    @Test
    public void weightOfANewlyAddedEdgeInASimpleGraphShouldBeOne() {
        ItemGraph itemGraph = createNewItemGraphWithSupermarket(ONE);
        List<BoughtItem> items = createBoughtItems(2, ONE);
        BoughtItem i0 = items.get(0);
        BoughtItem i1 = items.get(1);
        itemGraph.addBoughtItems(items);

        assertTrue("Edge has not been added", itemGraph.edgeFromToExists(i0, i1));
        Set<Edge> edges = itemGraph.getEdgesFrom(i0);
        for (Edge edge : edges) {
            if (edge.getTo().equals(i1)) {
                assertEquals("Weight of the edge is set incorrectly", 1, edge.getWeight());
                return;
            }
        }
    }

    @Test
    public void weightOfANewlyAddedEdgeInAComplicatedGraphShouldBeOne() {
        ItemGraph itemGraph = createCyclicFreeDataWithSixVertices();
        BoughtItem i0 = createBoughtItemWithIdAndSupermarket(0, ONE);
        BoughtItem i2 = createBoughtItemWithIdAndSupermarket(2, ONE);
        BoughtItem i3 = createBoughtItemWithIdAndSupermarket(3, ONE);
        List<BoughtItem> items = new ArrayList<>(3);
        items.add(i0);
        items.add(i2);
        items.add(i3);
        itemGraph.addBoughtItems(items);

        assertTrue("Edge has not been added", itemGraph.edgeFromToExists(i2, i3));
        Set<Edge> edges = itemGraph.getEdgesFrom(i2);
        for (Edge edge : edges) {
            if (edge.getTo().equals(i3)) {
                assertEquals("Weight of the edge is set incorrectly", 1, edge.getWeight());
                return;
            }
        }
    }

    @Test
    public void weightOfAnEdgeShouldBeIncrementedIfItemsAreBoughtInThatOrder() {
        ItemGraph itemGraph = createCyclicFreeDataWithSixVertices();
        BoughtItem i1 = createBoughtItemWithIdAndSupermarket(1, ONE);
        BoughtItem i3 = createBoughtItemWithIdAndSupermarket(3, ONE);
        Set<Edge> edges = itemGraph.getEdgesFrom(i1);
        Edge edge = null;
        for (Edge e : edges) {
            if (e.getTo().equals(i3)) {
                edge = e;
                break;
            }
        }
        assertNotNull("Edge i1->i3 not found, although it should be part of the item graph", edge);
        int weightBeforeUpdate = edge.getWeight();

        List<BoughtItem> items = new ArrayList<>(2);
        items.add(i1);
        items.add(i3);
        itemGraph.addBoughtItems(items);

        edges = itemGraph.getEdgesFrom(i1);
        edge = null;
        for (Edge e : edges) {
            if (e.getTo().equals(i3)) {
                edge = e;
                break;
            }
        }
        assertNotNull("Edge i1->i3 not found, although it should be part of the item graph and already was before the update", edge);
        int weightAfterUpdate = edge.getWeight();

        assertEquals("The weight of the edge was not updated correctly", weightBeforeUpdate + 1, weightAfterUpdate);
    }

    @Test
    public void getEdgesFromDoesReturnEdges() {
        ItemGraph itemGraph = createNewItemGraphWithSupermarket(ONE);
        List<BoughtItem> items = createBoughtItems(3, ONE);
        itemGraph.addBoughtItems(items);
        Set<Edge> edgesFromI0 = itemGraph.getEdgesFrom(items.get(0));
        boolean edgeFound = false;
        for (Edge edge : edgesFromI0) {
            if (edge.getTo().equals(items.get(1))) {
                edgeFound = true;
                break;
            }
        }
        assertTrue("Edge i0->i1 not contained", edgeFound);

        Set<Edge> edgesFromI1 = itemGraph.getEdgesFrom(items.get(1));
        edgeFound = false;
        for (Edge edge : edgesFromI1) {
            if (edge.getTo().equals(items.get(2))) {
                edgeFound = true;
                break;
            }
        }
        assertTrue("Edge i1->i2 not contained", edgeFound);
    }


    @Test
    public void theGlobalSupermarketDataShouldBeUpdatedIfASupermarketChainIsSet() {
        ItemGraph itemGraph = createCyclicFreeDataWithSixVertices();
        Supermarket supermarket = itemGraph.getSupermarket();
        assertEquals("The placeId of the item graph's supermarket is not set correctly", "ONE", supermarket.getPlaceId());
        SupermarketChain supermarketChain = supermarket.getSupermarketChain();
        assertEquals("The supermarket chain is not set correctly", CHAIN_ONE, supermarketChain.getName());

        SupermarketHelper supermarketHelper = new SupermarketHelper((DAODummyHelper) itemGraph.getDaoHelper());
        Supermarket globalSupermarket = supermarketHelper.getGlobalSupermarket(supermarketChain);

        ItemGraph globalSupermarketItemGraph = createNewItemGraphWithSupermarketAndDAOHelper(globalSupermarket.getPlaceId(), itemGraph.getDaoHelper());
        globalSupermarketItemGraph.update();

        makeSureAllEdgesWereAddedCorrectlyAccordingToCyclicFreeExampleItemGraphWithSixVertices(globalSupermarketItemGraph);
    }

    //helper Method
    private void makeSureAllEdgesWereAddedCorrectlyAccordingToCyclicFreeExampleItemGraphWithSixVertices(ItemGraph itemGraph) {
        Set<BoughtItem> items = itemGraph.getVertices();
        BoughtItem i0 = null, i1 = null, i2 = null, i3 = null, i4 = null, i5 = null;
        for (BoughtItem item : items) {
            switch (item.getId()) {
                case 0:
                    //Start and End nodes also have id 0
                    if(item.getName().equals("i0")){
                        i0 = item;
                    }
                    break;
                case 1: i1 = item; break;
                case 2: i2 = item; break;
                case 3: i3 = item; break;
                case 4: i4 = item; break;
                case 5: i5 = item; break;
            }
        }



        assertNotNull("Vertex not found in item graph", i0);
        assertNotNull("Vertex not found in item graph", i1);
        assertNotNull("Vertex not found in item graph", i2);
        assertNotNull("Vertex not found in item graph", i3);
        assertNotNull("Vertex not found in item graph", i4);
        assertNotNull("Vertex not found in item graph", i5);

        assertTrue("Missing edge found", itemGraph.edgeFromToExists(i0, i1));
        assertTrue("Missing edge found", itemGraph.edgeFromToExists(i0, i2));
        assertTrue("Missing edge found", itemGraph.edgeFromToExists(i1, i2));
        assertTrue("Missing edge found", itemGraph.edgeFromToExists(i1, i5));
        assertTrue("Missing edge found", itemGraph.edgeFromToExists(i1, i3));
        assertTrue("Missing edge found", itemGraph.edgeFromToExists(i5, i3));
        assertTrue("Missing edge found", itemGraph.edgeFromToExists(i3, i4));
    }

    @Test
    public void makeSureTheCyclicFreeItemGraphWithSixVerticesHasEdgesSetCorrectly() {
        ItemGraph itemGraph = createCyclicFreeDataWithSixVertices();
        makeSureAllEdgesWereAddedCorrectlyAccordingToCyclicFreeExampleItemGraphWithSixVertices(itemGraph);
    }

    @Test
    public void addItemsToTheExampleItemGraphThatWouldProduceACycleButShouldNotRemoveAnyEdges() {
        ItemGraph itemGraph = createCyclicFreeDataWithSixVertices();
        BoughtItem i3, i4, i5;
        i3 = createBoughtItemWithIdAndSupermarket(3, ONE);
        i4 = createBoughtItemWithIdAndSupermarket(4, ONE);
        i5 = createBoughtItemWithIdAndSupermarket(5, ONE);

        addBoughtItemsToItemGraph(itemGraph, i3, i4, i5);
        makeSureAllEdgesWereAddedCorrectlyAccordingToCyclicFreeExampleItemGraphWithSixVertices(itemGraph);
        assertFalse("Cycle found", itemGraph.edgeFromToExists(i4, i5));
    }

    @Test
    public void onlyRelevantEdgesAreModifiedWhenAnEdgeFlips() {
        ItemGraph itemGraph = createNewItemGraphWithSupermarket(ONE);
        BoughtItem i0, i1, i2;
        i0 = createBoughtItemWithIdAndSupermarket(0, ONE);
        i1 = createBoughtItemWithIdAndSupermarket(1, ONE);
        i2 = createBoughtItemWithIdAndSupermarket(2, ONE);

        addBoughtItemsToItemGraph(itemGraph, i0, i1, i2);
        addBoughtItemsToItemGraph(itemGraph, i0, i1);

        assertTrue("Missing edge detected before adding the items that cause the flip", itemGraph.edgeFromToExists(i0, i1));
        assertTrue("Missing edge detected before adding the items that cause the flip", itemGraph.edgeFromToExists(i0, i2));
        assertTrue("Missing edge detected before adding the items that cause the flip", itemGraph.edgeFromToExists(i1, i2));

        addBoughtItemsToItemGraph(itemGraph, i2, i1);

        Set<BoughtItem> vertices = itemGraph.getVertices();
        assertTrue("Missing Vertex found after adding the items that cause the flip", vertices.contains(i0));
        assertTrue("Missing Vertex found after adding the items that cause the flip", vertices.contains(i1));
        assertTrue("Missing Vertex found after adding the items that cause the flip", vertices.contains(i2));


        assertTrue("Missing edge detected after adding the items that cause the flip", itemGraph.edgeFromToExists(i0, i1));
        assertTrue("Missing edge detected after adding the items that cause the flip", itemGraph.edgeFromToExists(i0, i2));
        assertTrue("Inverse edge was not added when edge should have flipped", itemGraph.edgeFromToExists(i2, i1));
        assertFalse("Edge that should have flipped was not removed", itemGraph.edgeFromToExists(i1, i2));
    }

    @Test
    public void addDataThatWouldProduceTwoCyclesAtOnce() {
        ItemGraph itemGraph = createNewItemGraphWithSupermarket(ONE);
        List<BoughtItem> items = createBoughtItems(5, ONE);
        BoughtItem i0, i1, i2, i3, i4;
        i0 = items.get(0);
        i1 = items.get(1);
        i2 = items.get(2);
        i3 = items.get(3);
        i4 = items.get(4);
        itemGraph.addBoughtItems(items);

        assertTrue("Missing edge found, although no inconsistent data was added so far", itemGraph.edgeFromToExists(i0,i1));
        assertTrue("Missing edge found, although no inconsistent data was added so far", itemGraph.edgeFromToExists(i1,i2));
        assertTrue("Missing edge found, although no inconsistent data was added so far", itemGraph.edgeFromToExists(i2,i3));
        assertTrue("Missing edge found, although no inconsistent data was added so far", itemGraph.edgeFromToExists(i3,i4));

        addBoughtItemsToItemGraph(itemGraph, i4, i2, i0);
        // i4->i2 would close cycle i2->i3->i4->i2
        // i2->i0 would close cycle i0->i1->i2->i0

        // check if first cycle was closed
        if (itemGraph.edgeFromToExists(i0, i1)) {
            if (itemGraph.edgeFromToExists(i1, i2)) {
                assertFalse("cycle found: i0->i1->i2->i0", itemGraph.edgeFromToExists(i2, i0));
            } else {
                assertTrue("missing edge found", itemGraph.edgeFromToExists(i2, i0));
            }
        } else {
            assertTrue("missing edge found", itemGraph.edgeFromToExists(i1, i2));
            assertTrue("missing edge found", itemGraph.edgeFromToExists(i2, i0));
        }

        // check if second cycle was closed
        if (itemGraph.edgeFromToExists(i2, i3)) {
            if (itemGraph.edgeFromToExists(i3, i4)) {
                assertFalse("cycle found: i2->i3->i4->i2", itemGraph.edgeFromToExists(i4, i2));
            } else {
                assertTrue("missing edge found", itemGraph.edgeFromToExists(i4, i2));
            }
        } else {
            assertTrue("missing edge found", itemGraph.edgeFromToExists(i3, i4));
            assertTrue("missing edge found", itemGraph.edgeFromToExists(i4, i2));
        }
    }

    @Test
    public void addDataThatWouldProduceThreeCyclesAtOnce() {
        ItemGraph itemGraph = createNewItemGraphWithSupermarket(ONE);
        List<BoughtItem> items = createBoughtItems(5, ONE);
        BoughtItem i0, i1, i2, i3, i4;
        i0 = items.get(0);
        i1 = items.get(1);
        i2 = items.get(2);
        i3 = items.get(3);
        i4 = items.get(4);
        itemGraph.addBoughtItems(items);

        assertTrue("Missing edge found, although no inconsistent data was added so far", itemGraph.edgeFromToExists(i0,i1));
        assertTrue("Missing edge found, although no inconsistent data was added so far", itemGraph.edgeFromToExists(i1,i2));
        assertTrue("Missing edge found, although no inconsistent data was added so far", itemGraph.edgeFromToExists(i2,i3));
        assertTrue("Missing edge found, although no inconsistent data was added so far", itemGraph.edgeFromToExists(i3,i4));

        addBoughtItemsToItemGraph(itemGraph, i4, i0, i3, i1);
        // i4->i0 would close cycle i0->i1->i2->i3->i4->i0
        // i0->i3 would close cycle i3->i4->i0->i3
        // i3->i1 would close cycle i1->i2->i3->i1

        boolean i0ToI1Exists, i1ToI2Exists, i2ToI3Exists, i3ToI4Exists, i4ToI0Exists, i0ToI3Exists, i3ToI1Exists;
        i0ToI1Exists = itemGraph.edgeFromToExists(i0, i1);
        i1ToI2Exists = itemGraph.edgeFromToExists(i1, i2);
        i2ToI3Exists = itemGraph.edgeFromToExists(i2, i3);
        i3ToI4Exists = itemGraph.edgeFromToExists(i3, i4);
        i4ToI0Exists = itemGraph.edgeFromToExists(i4, i0);

        i0ToI3Exists = itemGraph.edgeFromToExists(i0, i3);
        i3ToI1Exists = itemGraph.edgeFromToExists(i3, i1);

        // check if big cycle was closed
        if (i0ToI1Exists) {
            if (i1ToI2Exists) {
                if (i2ToI3Exists) {
                    if (i3ToI4Exists) {
                        assertFalse("cycle found: i0->i1->i2->i3->i4->i0", i4ToI0Exists);
                    } else {
                        assertTrue("Missing edge found", i4ToI0Exists);
                    }
                } else {
                    assertTrue("Missing edge found", i3ToI4Exists);
                    assertTrue("Missing edge found", i4ToI0Exists);
                }
            } else {
                assertTrue("Missing edge found", i2ToI3Exists);
                assertTrue("Missing edge found", i3ToI4Exists);
                assertTrue("Missing edge found", i4ToI0Exists);
            }
        } else {
            assertTrue("Missing edge found", i1ToI2Exists);
            assertTrue("Missing edge found", i2ToI3Exists);
            assertTrue("Missing edge found", i3ToI4Exists);
            assertTrue("Missing edge found", i4ToI0Exists);
        }

        // check if first small cycle was closed
        if (i3ToI4Exists) {
            if (i4ToI0Exists) {
                assertFalse("cycle found: i3->i4->i0->i3", i0ToI3Exists);
            } else {
                assertTrue("missing edge found", i0ToI3Exists);
            }
        } else {
            assertTrue("missing edge found", i4ToI0Exists);
            assertTrue("missing edge found", i0ToI3Exists);
        }

        // check if second small cycle was closed
        if (i1ToI2Exists) {
            if (i2ToI3Exists) {
                assertFalse("cycle found: i1->i2->i3->i1", i3ToI1Exists);
            } else {
                assertTrue("missing edge found", i3ToI1Exists);
            }
        } else {
            assertTrue("missing edge found", i2ToI3Exists);
            assertTrue("missing edge found", i3ToI1Exists);
        }
    }

    @Test
    public void cycleDetectionDoesNotAddAnEdgeIfAllOtherEdgesHaveAHigherWeight() {
        ItemGraph itemGraph = createNewItemGraphWithSupermarket(ONE);
        List<BoughtItem> items = createBoughtItems(3, ONE);
        BoughtItem i0, i1, i2;
        i0 = items.get(0);
        i1 = items.get(1);
        i2 = items.get(2);
        addItemsToItemGraphTwiceAndThenCloseTheCycle(itemGraph, items, i0, i1, i2);
    }

    @Test
    public void cycleDetectionShouldAddAnEdgeIfItIsAddedMultipleTimes() {
        ItemGraph itemGraph = createNewItemGraphWithSupermarket(ONE);
        List<BoughtItem> items = createBoughtItems(3, ONE);
        BoughtItem i0, i1, i2;
        i0 = items.get(0);
        i1 = items.get(1);
        i2 = items.get(2);
        addItemsToItemGraphTwiceAndThenCloseTheCycle(itemGraph, items, i0, i1, i2);

        addBoughtItemsToItemGraph(itemGraph, i2, i0);

        // This is the moment where the edge i2->i0 should be added. If not, we might think about un-commenting the following line
        // addBoughtItemsToItemGraph(itemGraph, i2, i0);
        assertTrue("Missing edge found", itemGraph.edgeFromToExists(i2, i0));
        // And exactly one of the edges i0->i1 and i1->i2 should still be part of the itemGraph
        if (itemGraph.edgeFromToExists(i0, i1)) {
            assertFalse("cycle detected", itemGraph.edgeFromToExists(i1, i2));
        } else {
            assertTrue("Missing edge found", itemGraph.edgeFromToExists(i1, i2));
        }
    }

    // helper method
    private void addItemsToItemGraphTwiceAndThenCloseTheCycle(ItemGraph itemGraph, List<BoughtItem> items, BoughtItem i0, BoughtItem i1, BoughtItem i2) {
        itemGraph.addBoughtItems(items);
        itemGraph.addBoughtItems(items);

        // now the item graph should look like this:
        // i0-->i1-->i2
        // where --> is an edge with weight 2
        assertTrue("Missing edge found", itemGraph.edgeFromToExists(i0, i1));
        assertTrue("Missing edge found", itemGraph.edgeFromToExists(i1, i2));

        Set<Edge> edges = itemGraph.getEdgesFrom(i0);
        boolean edgeFound = false;
        for (Edge edge : edges) {
            if (edge.getTo().equals(i1)) {
                edgeFound = true;
                assertEquals("Edge does not have the expected weight", 2, edge.getWeight());
                break;
            }
        }
        assertTrue("Edge not contained in result of getEdgesFrom(), although it exists according to itemGraph.edgeFromToExists", edgeFound);

        edges = itemGraph.getEdgesFrom(i1);
        edgeFound = false;
        for (Edge edge : edges) {
            if (edge.getTo().equals(i2)) {
                edgeFound = true;
                assertEquals("Edge does not have the expected weight", 2, edge.getWeight());
                break;
            }
        }
        assertTrue("Edge not contained in result of getEdgesFrom(), although it exists according to itemGraph.edgeFromToExists", edgeFound);

        addBoughtItemsToItemGraph(itemGraph, i2, i0);
        // This would add an edge i2->i0
        // so the resulting item graph would be i0-->i1-->i2->i0
        // where --> is an edge with weight 2 and -> an edge with weight 1
        // the cycle should be broken by removing i2->i0

        assertTrue("Missing edge found", itemGraph.edgeFromToExists(i0, i1));
        assertTrue("Missing edge found", itemGraph.edgeFromToExists(i1, i2));
        assertFalse("Cycle detected", itemGraph.edgeFromToExists(i2, i0));
    }

    @Test
    public void cycleDetectionShouldRemoveAnOtherEdgeWithWeightOneInsteadOfNotAddingANewEdge() {
        ItemGraph itemGraph = createNewItemGraphWithSupermarket(ONE);
        List<BoughtItem> items = createBoughtItems(3, ONE);
        BoughtItem i0, i1, i2;
        i0 = items.get(0);
        i1 = items.get(1);
        i2 = items.get(2);
        addBoughtItemsToItemGraph(itemGraph, i0, i1, i2);

        addBoughtItemsToItemGraph(itemGraph, i0, i1);

        // now the item graph should look like this:
        // i0-->i1->i2

        addBoughtItemsToItemGraph(itemGraph, i2, i0);

        // This would close the cycle. As new data is preferred, the weight-1-edge i1->i2 should be removed

        assertTrue("Missing edge found", itemGraph.edgeFromToExists(i0, i1));
        assertTrue("Missing edge found", itemGraph.edgeFromToExists(i2, i0));
        assertFalse("Cycle detected", itemGraph.edgeFromToExists(i1, i2));
    }

    @Test
    public void addItemsToItemGraphWithTheSameNameAsTheStartAndEndItems() {
        ItemGraph itemGraph = createNewItemGraphWithSupermarket(ONE);
        String startItemName = DAOHelper.START_ITEM;
        String endItemName = DAOHelper.END_ITEM;
        /*
         * Never trust user input -- even if the user does not know our start and end item names
         * (which are not difficult to find out, as we are open source), a user might choose the
         * same name.
         */

        BoughtItem i0, iEnd, iStart, i3;
        i0 = createBoughtItemWithIdAndSupermarket(0, ONE);
        iEnd = new BoughtItem(endItemName, ONE, ONE);
        iEnd.setId(1);
        iStart = new BoughtItem(startItemName, ONE, ONE);
        iStart.setId(2);
        i3 = createBoughtItemWithIdAndSupermarket(3, ONE);

        addBoughtItemsToItemGraph(itemGraph, i0, iEnd, iStart, i3);

        // All four items should be contained in the itemGraph, as should be the corresponding edges
        Set<BoughtItem> vertices = itemGraph.getVertices();
        assertTrue("Item i0 not contained in vertices, although it has been added", vertices.contains(i0));
        assertTrue("Item iEnd not contained in vertices, although it has been added", vertices.contains(iEnd));
        assertTrue("Item iStart not contained in vertices, although it has been added", vertices.contains(iStart));
        assertTrue("Item i3 not contained in vertices, although it has been added", vertices.contains(i3));

        assertTrue("Missing edge detected", itemGraph.edgeFromToExists(i0, iEnd));
        assertTrue("Missing edge detected", itemGraph.edgeFromToExists(iEnd, iStart));
        assertTrue("Missing edge detected", itemGraph.edgeFromToExists(iStart, i3));
    }

    @Test
    public void privateMethodAddStartEndDoesWorkIfItemsAreCalledSTART_ITEM_or_END_ITEM() throws NoSuchMethodException, IllegalAccessException, InvocationTargetException {
        ItemGraph itemGraph = createNewItemGraphWithSupermarket(ONE);
        String startItemName = DAOHelper.START_ITEM;
        String endItemName = DAOHelper.END_ITEM;

        BoughtItem i0, iEnd, iStart, i3;
        i0 = createBoughtItemWithIdAndSupermarket(0, ONE);
        iEnd = new BoughtItem(endItemName, ONE, ONE);
        iEnd.setId(1);
        iStart = new BoughtItem(startItemName, ONE, ONE);
        iStart.setId(2);
        i3 = createBoughtItemWithIdAndSupermarket(3, ONE);

        List<BoughtItem> boughtItems = new ArrayList<>(4);
        boughtItems.add(i0);
        boughtItems.add(iEnd);
        boughtItems.add(iStart);
        boughtItems.add(i3);
        // __DON'T__ call itemGraph.addBoughtItems(boughtItems)!!!

        // call the private method itemGraph.addStartEnd using Java Reflections
        Method addStartEnd = ItemGraph.class.getDeclaredMethod("addStartEnd", List.class);
        addStartEnd.setAccessible(true);
        List<BoughtItem> returnedValue = (List<BoughtItem>) addStartEnd.invoke(itemGraph, boughtItems);

        assertEquals("Returned list has wrong size", 6, returnedValue.size());

        assertEquals("START_ITEM not added correctly", startItemName, returnedValue.get(0).getName());
        assertTrue("START_ITEM not added correctly", returnedValue.get(0).isServerInternalItem());
        assertEquals("i0 not added correctly", "i0", returnedValue.get(1).getName());
        assertEquals("iEnd not added correctly", endItemName, returnedValue.get(2).getName());
        assertFalse("iEnd not added correctly", returnedValue.get(2).isServerInternalItem());
        assertEquals("iStart not added correctly", startItemName, returnedValue.get(3).getName());
        assertFalse("iStart not added correctly", returnedValue.get(3).isServerInternalItem());
        assertEquals("i3 not added correctly", "i3", returnedValue.get(4).getName());
        assertEquals("END_ITEM not added correctly", endItemName, returnedValue.get(5).getName());
        assertTrue("END_ITEM not added correctly", returnedValue.get(5).isServerInternalItem());
    }

    @Test
    public void closeACycleWhereOnlyOneEdgeIsAllowedToBeDeleted() {
        List<BoughtItem> items = createBoughtItems(7, ONE);
        BoughtItem i0, i1, i2, i3, i4, i5, i6, start;
        i0 = items.get(0);
        i1 = items.get(1);
        i2 = items.get(2);
        i3 = items.get(3);
        i4 = items.get(4);
        i5 = items.get(5);
        i6 = items.get(6);

        ItemGraph itemGraph = createNewItemGraphWithSupermarket(ONE);
        addBoughtItemsToItemGraph(itemGraph, i0, i1, i5);
        addBoughtItemsToItemGraph(itemGraph, i0, i2, i4, i6);
        addBoughtItemsToItemGraph(itemGraph, i1, i3, i4);

        start = itemGraph.getDaoHelper().getStartBoughtItem();

        /*
         * This item graph looks something like this:
         *                    i0
         *                   /  \
         *                  /    \
         *                 /      \
         *                |       |
         *                v       v
         *                i1      i2
         *               /  \      \
         *              /   |      |
         *             /    v      v
         *            /     i3 --> i4
         *           |             |
         *           v             v
         *           i5            i6
         * All edges have weight 1
         */

        addBoughtItemsToItemGraph(itemGraph, i6, i1);

        // This would close the cycle i1->i3->i4->i6
        // Either only the edge i3 -> i4 or the edge i4->i6 should have been removed in order to
        // break the cycle; otherwise it would not be possible to reach all items any more

        assertTrue("Missing edge found", itemGraph.edgeFromToExists(i1, i3));
        if (itemGraph.edgeFromToExists(i3, i4)) {
            assertFalse("cycle detected", itemGraph.edgeFromToExists(i4, i6));
            assertTrue("Missing edge found", itemGraph.edgeFromToExists(start, i6));
        } else {
            assertTrue("Missing edge found", itemGraph.edgeFromToExists(i4, i6));
        }
    }

    @Test
    public void itemGraphGetsBrokenIfMultipleSupermarketsAreUsed() {
        ItemGraph itemGraphOne = createNewItemGraphWithSupermarket(ONE);
        ItemGraph itemGraphTwo = createNewItemGraphWithSupermarketAndDAOHelper(TWO, itemGraphOne.getDaoHelper());

        assertSame("The two ItemGraphs work with different DAOHelpers", itemGraphOne.getDaoHelper(), itemGraphTwo.getDaoHelper());

        // These two supermarkets don't belong to the same chain
        BoughtItem i0One, i0Two, i1One, i1Two, i2One, i2Two, i3One, i3Two;
        i0One = createBoughtItemWithIdAndSupermarket(0, ONE);
        i0Two = createBoughtItemWithIdAndSupermarket(0, TWO);
        i1One = createBoughtItemWithIdAndSupermarket(1, ONE);
        i1Two = createBoughtItemWithIdAndSupermarket(1, TWO);
        i2One = createBoughtItemWithIdAndSupermarket(2, ONE);
        i2Two = createBoughtItemWithIdAndSupermarket(2, TWO);
        i3One = createBoughtItemWithIdAndSupermarket(3, ONE);
        i3Two = createBoughtItemWithIdAndSupermarket(3, TWO);

        addBoughtItemsToItemGraph(itemGraphOne, i0One, i1One, i2One);
        addBoughtItemsToItemGraph(itemGraphTwo, i0Two, i1Two, i3Two);
        itemGraphOne.update();
        itemGraphTwo.update();

        assertFalse("Edge should only exist in other item graph", itemGraphOne.edgeFromToExists(i1One, i3One));
        assertFalse("Edge should only exist in other item graph", itemGraphTwo.edgeFromToExists(i1Two, i2Two));
    }

    @Test
    public void updateItemGraphThatDoesNotBelongToASupermarketChain() {
        ItemGraph itemGraph = createNewItemGraphWithSupermarket(FOUR);
        List<BoughtItem> items = createBoughtItems(2, FOUR);
        itemGraph.addBoughtItems(items);
        itemGraph.update();

        Set<BoughtItem> vertices = itemGraph.getVertices();
        assertTrue("Not all vertices contained", vertices.containsAll(items));
    }

}