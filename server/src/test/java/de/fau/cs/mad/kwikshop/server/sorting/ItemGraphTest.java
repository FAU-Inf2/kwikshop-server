package de.fau.cs.mad.kwikshop.server.sorting;

import org.junit.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import de.fau.cs.mad.kwikshop.common.sorting.BoughtItem;
import de.fau.cs.mad.kwikshop.server.sorting.helperClasses.SortingTestSuperclass;

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
}