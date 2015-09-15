package de.fau.cs.mad.kwikshop.server;

import com.codahale.metrics.MetricRegistry;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableList;

import org.hibernate.SessionFactory;
import org.junit.*;

import java.lang.reflect.*;
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

        @Override
        public Supermarket getSupermarketByPlaceID(String placeId) {
            return null;
        }

        @Override
        public List<SupermarketChain> getAllSupermarketChains() {
            return null;
        }

        @Override
        public void createSupermarket(Supermarket supermarket) {

        }

        @Override
        public List<Edge> getEdgesBySupermarket(Supermarket supermarket) {
            return null;
        }

        @Override
        public Edge getEdgeByFromTo(BoughtItem from, BoughtItem to, Supermarket supermarket) {
            return null;
        }

        @Override
        public List<Edge> getEdgesByTo(BoughtItem boughtItem, Supermarket supermarket) {
            return null;
        }

        @Override
        public Edge createEdge(Edge edge) {
            return null;
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