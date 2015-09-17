package de.fau.cs.mad.kwikshop.server.sorting;

import org.junit.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import de.fau.cs.mad.kwikshop.common.sorting.BoughtItem;
import de.fau.cs.mad.kwikshop.server.sorting.DAOHelper;
import de.fau.cs.mad.kwikshop.server.sorting.Edge;
import de.fau.cs.mad.kwikshop.server.sorting.ItemGraph;
import de.fau.cs.mad.kwikshop.server.sorting.Supermarket;
import de.fau.cs.mad.kwikshop.server.sorting.SupermarketChain;
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
    public void newItemGraphShouldNotHaveASupermerket() {
        ItemGraph itemGraph = createNewItemGraph();
        assertNull("Newly created ItemGraph already has supermarket set", itemGraph.getSupermarket());
    }

    @Test
    public void setAndGetSupermarketTest() {
        ItemGraph itemGraph = createNewItemGraph();
        DAOHelper daoHelper = itemGraph.getDaoHelper();
        Supermarket supermarket = daoHelper.getSupermarketByPlaceID(ONE);
        itemGraph.setSupermarket(supermarket.getPlaceId(), "");
        assertEquals("The returned supermarket by getSupermarket should be the same as the supermarket that was set", supermarket.getPlaceId(), itemGraph.getSupermarket().getPlaceId());
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
            List<Edge> edges = this.edges.get(supermarket.getPlaceId());
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