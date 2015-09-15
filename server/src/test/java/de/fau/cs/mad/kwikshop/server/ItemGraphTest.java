package de.fau.cs.mad.kwikshop.server;

import com.codahale.metrics.MetricRegistry;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableList;

import org.hibernate.SessionFactory;
import org.junit.*;

import java.lang.reflect.*;

import javax.validation.Validator;

import de.fau.cs.mad.kwikshop.common.rest.ShoppingListResource;
import de.fau.cs.mad.kwikshop.server.api.ShoppingListResourceImpl;
import de.fau.cs.mad.kwikshop.server.dao.BoughtItemDAO;
import de.fau.cs.mad.kwikshop.server.dao.EdgeDAO;
import de.fau.cs.mad.kwikshop.server.dao.SupermarketChainDAO;
import de.fau.cs.mad.kwikshop.server.dao.SupermarketDAO;
import de.fau.cs.mad.kwikshop.server.sorting.ItemGraph;
import io.dropwizard.db.DataSourceFactory;
import io.dropwizard.hibernate.HibernateBundle;
import io.dropwizard.hibernate.SessionFactoryFactory;
import io.dropwizard.setup.Environment;

import static org.junit.Assert.*;

public class ItemGraphTest {

    private ItemGraph itemGraph;

    /*@Before
    public void setup() throws Exception {
        ServerApplication serverApplication = new ServerApplication();
        Field hibernateField = ServerApplication.class.getDeclaredField("hibernate");
        hibernateField.setAccessible(true);
        HibernateBundle<ServerConfiguration> hibernate = (HibernateBundle<ServerConfiguration>) hibernateField.get(serverApplication);

        ServerConfiguration serverConfiguration = new ServerConfiguration();
        ObjectMapper objectMapper = new ObjectMapper();
        Validator validator = null;
        MetricRegistry metricRegistry = new MetricRegistry();
        ClassLoader classLoader = new ClassLoader() {
            @Override
            public Class<?> loadClass(String name) throws ClassNotFoundException {
                return super.loadClass(name);
            }
        };
        Environment environment = new Environment("Test", objectMapper, validator, metricRegistry, classLoader);

        Field sessionFactoryFactoryField = HibernateBundle.class.getDeclaredField("sessionFactoryFactory");
        sessionFactoryFactoryField.setAccessible(true);
        final SessionFactoryFactory sessionFactoryFactory = (SessionFactoryFactory) sessionFactoryFactoryField.get(hibernate);

        Field entitiesField = HibernateBundle.class.getDeclaredField("entities");
        entitiesField.setAccessible(true);
        final ImmutableList<Class<?>> entities = (ImmutableList<Class<?>>) entitiesField.get(hibernate);

        String defaultName = "hibernate";

        final DataSourceFactory dbConfig = hibernate.getDataSourceFactory(serverConfiguration);
        SessionFactory sessionFactory = sessionFactoryFactory.build(hibernate, environment, dbConfig, entities, defaultName);


        //String[] args = {};
        //serverApplication.run("server");
        //serverApplication.run();


        final BoughtItemDAO boughtItemDAO = new BoughtItemDAO(sessionFactory);
        final EdgeDAO edgeDAO = new EdgeDAO(hibernate.getSessionFactory(), boughtItemDAO);
        final SupermarketDAO supermarketDAO = new SupermarketDAO(hibernate.getSessionFactory());
        final SupermarketChainDAO supermarketChainDAO = new SupermarketChainDAO(hibernate.getSessionFactory(), supermarketDAO);

        itemGraph = new ItemGraph(boughtItemDAO, edgeDAO, supermarketDAO, supermarketChainDAO);
    }*/

    @Test
    public void dummyTest() {
        assertTrue(true);
    }

    @Ignore
    @Test
    public void failureTest() {
        assertTrue("It is all right that this test fails", false);
    }

    @Ignore
    @Test(expected = IllegalArgumentException.class)
    public void exceptionTest() {
        throw new IllegalArgumentException();
    }
}
