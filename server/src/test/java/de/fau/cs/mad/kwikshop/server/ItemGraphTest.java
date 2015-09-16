package de.fau.cs.mad.kwikshop.server;

import com.codahale.metrics.MetricRegistry;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableList;

import org.hibernate.SessionFactory;
import org.junit.*;

import java.lang.reflect.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.validation.Validator;

import de.fau.cs.mad.kwikshop.common.rest.ShoppingListResource;
import de.fau.cs.mad.kwikshop.common.sorting.BoughtItem;
import de.fau.cs.mad.kwikshop.server.api.ShoppingListResourceImpl;
import de.fau.cs.mad.kwikshop.server.dao.BoughtItemDAO;
import de.fau.cs.mad.kwikshop.server.dao.EdgeDAO;
import de.fau.cs.mad.kwikshop.server.dao.SupermarketChainDAO;
import de.fau.cs.mad.kwikshop.server.dao.SupermarketDAO;
import de.fau.cs.mad.kwikshop.server.sorting.DAOHelper;
import de.fau.cs.mad.kwikshop.server.sorting.Edge;
import de.fau.cs.mad.kwikshop.server.sorting.ItemGraph;
import de.fau.cs.mad.kwikshop.server.sorting.Supermarket;
import de.fau.cs.mad.kwikshop.server.sorting.SupermarketChain;
import io.dropwizard.db.DataSourceFactory;
import io.dropwizard.hibernate.HibernateBundle;
import io.dropwizard.hibernate.SessionFactoryFactory;
import io.dropwizard.setup.Environment;

import static org.junit.Assert.*;

public class ItemGraphTest {

    private ItemGraph itemGraph;

    private ItemGraph createNewItemGraph() {
        return new ItemGraph(new DAODummyHelper());
    }

    @Test
    public void dummyTest() {
        assertTrue(true);
    }

    private class DAODummyHelper implements DAOHelper {

        private final String ONE = "ONE";
        private final String TWO = "TWO";
        private final String THREE = "THREE";
        private final String FOUR = "FOUR";
        private final String CHAIN_ONE = "CHAIN_ONE";
        private final String CHAIN_TWO = "CHAIN_TWO";

        private final Supermarket defaultSupermarketOne;
        private final Supermarket defaultSupermarketTwo;
        private final Supermarket defaultSupermarketThree;
        private final Supermarket defaultSupermarketFour;
        private final SupermarketChain defaultSupermarketChainOne;
        private final SupermarketChain defaultSupermarketChainTwo;

        private final HashMap<String, List<Edge>> edges;
        private HashMap<String, Supermarket> supermarkets;

        public DAODummyHelper() {
            defaultSupermarketChainOne = new SupermarketChain();
            defaultSupermarketChainOne.setId(1);
            defaultSupermarketChainOne.setName(CHAIN_ONE);

            defaultSupermarketChainTwo = new SupermarketChain();
            defaultSupermarketChainTwo.setId(2);
            defaultSupermarketChainTwo.setName(CHAIN_TWO);

            defaultSupermarketOne = new Supermarket();
            defaultSupermarketOne.setId(1);
            defaultSupermarketOne.setPlaceId(ONE);
            defaultSupermarketOne.setSupermarketChain(defaultSupermarketChainOne);

            defaultSupermarketTwo = new Supermarket();
            defaultSupermarketOne.setId(2);
            defaultSupermarketOne.setPlaceId(TWO);
            defaultSupermarketTwo.setSupermarketChain(defaultSupermarketChainTwo);

            defaultSupermarketThree = new Supermarket();
            defaultSupermarketOne.setId(3);
            defaultSupermarketOne.setPlaceId(THREE);
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
            List<Edge> edges = this.edges.get(edge.getSupermarket().getPlaceId());
            edges.add(edge);
            return edge;
        }

        @Override
        public void deleteEdge(Edge edge) {

        }

        @Override
        public BoughtItem getStartBoughtItem() {
            return null;
        }

        @Override
        public BoughtItem getEndBoughtItem() {
            return null;
        }

        @Override
        public BoughtItem getBoughtItemByName(String name) {
            return null;
        }

        @Override
        public void createBoughtItem(BoughtItem boughtItem) {

        }

        @Override
        public Supermarket getGlobalSupermarketBySupermarketChain(SupermarketChain supermarketChain) {
            return null;
        }

        @Override
        public Supermarket getGlobalSupermarket(SupermarketChain supermarketChain) {
            return null;
        }
    }
}