package de.fau.cs.mad.kwikshop.server.sorting;

import org.junit.*;
import java.util.ArrayList;
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
        assertTrue("Newly created ItemGraph already had edges",edges.isEmpty());
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
        itemGraph.setSupermarket(supermarket.getPlaceId(), "");
        assertEquals("The returned supermarket by getSupermarket should be the same as the supermarket that was set", supermarket.getPlaceId(), itemGraph.getSupermarket().getPlaceId());
    }

    @Test
    public void setSupermarketReturnsCorrectValue() {
        ItemGraph itemGraph = createNewItemGraph();
        Supermarket supermarket = itemGraph.getDaoHelper().getSupermarketByPlaceID(ONE);
        assertFalse("setSupermarket returned true although it is not a new supermarket", itemGraph.setSupermarket(supermarket.getPlaceId(), ""));
        assertFalse("setSupermarket returned true although it is not a new supermarket", itemGraph.setSupermarket(supermarket.getPlaceId(), ""));
        assertTrue("setSupermarket returned false although it is a new supermarket", itemGraph.setSupermarket("blah", ""));
        assertFalse("setSupermarket returned true although it is not a new supermarket", itemGraph.setSupermarket("blah", ""));
    }

    @Test
    public void createOrUpdateEdgeForEmptyGraphShouldReturnAEdge() {
        ItemGraph itemGraph = createNewItemGraph();
        BoughtItem i1 = new BoughtItem("i1", ONE, ONE);
        BoughtItem i2 = new BoughtItem("i2", ONE, ONE);
        Supermarket supermarket = itemGraph.getDaoHelper().getSupermarketByPlaceID(ONE);

        Edge edge = itemGraph.createOrUpdateEdge(i1, i2, supermarket);
        assertNotNull("createOrUpdateEdge returns null", edge);
    }

    @Test
    public void createdEdgeShouldBeContainedInResultOfGetEdges() {
        ItemGraph itemGraph = createNewItemGraph();
        BoughtItem i1 = new BoughtItem("i1", ONE, ONE);
        BoughtItem i2 = new BoughtItem("i2", ONE, ONE);
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
        assertEquals("getVertices does not have size " + n+2 + "although " + n + "item(s) were added (+start/end)", n+2, vertices.size());
        for (int i = 0; i < n; i++) {
            assertTrue("The " + i + "th item is not contained in getVertices", vertices.contains(items.get(i)));
        }
    }

    private List<BoughtItem> createBoughtItems(int numberOfItemsToCreate, String supermarketPlaceId) {
        List<BoughtItem> items = new ArrayList<>(numberOfItemsToCreate);
        for (int i = 0; i < numberOfItemsToCreate; i++) {
            BoughtItem item = new BoughtItem("i" + i, supermarketPlaceId, "");
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
        ArrayList<Item> sortedItems = new ArrayList<>();
        for(Item item: sortedList.getItems()) {
            sortedItems.add(item);
        }

        for (int i = 0; i < n; i++) {
            assertEquals("A identical list was sorted different as before, although no different data is available. The lists first differ at element " + i, items.get(i).getName(), sortedItems.get(i).getName());
        }
    }

    private ShoppingListServer createShoppingListServerWithNItems(int n) {
        ArrayList<Item> items = new ArrayList<Item>();
        for (int i = 0; i < n; i++) {
            Item item = new Item();
            item.setName("i" + i);
            items.add(item);
        }
        ShoppingListServer shoppingListServer = new ShoppingListServer(0, items);
        return shoppingListServer;
    }

    private ShoppingListServer createShoppingListServerWithNItemsMixedUp(int n) {
        List<Item> orderedItems = new ArrayList<>(n);
        for (int i = 0; i < n; i++) {
            Item item = new Item();
            item.setName("i" + i);
            orderedItems.add(item);
        }

        Random random = new Random(n*n); // random generator with some random seed

        ArrayList<Item> randomItems = new ArrayList<Item>();
        while (!orderedItems.isEmpty()) {
            int index = random.nextInt(orderedItems.size());
            Item item = orderedItems.remove(index);
            randomItems.add(item);
        }
        ShoppingListServer shoppingListServer = new ShoppingListServer(0, randomItems);

        return shoppingListServer;
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