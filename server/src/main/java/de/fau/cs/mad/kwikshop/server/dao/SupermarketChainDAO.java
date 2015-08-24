package de.fau.cs.mad.kwikshop.server.dao;

import org.hibernate.SessionFactory;

import de.fau.cs.mad.kwikshop.server.sorting.SupermarketChain;
import io.dropwizard.hibernate.AbstractDAO;

public class SupermarketChainDAO extends AbstractDAO<SupermarketChain> {

    /**
     * Creates a new DAO with a given session provider.
     *
     * @param sessionFactory a session provider
     */
    public SupermarketChainDAO(SessionFactory sessionFactory) {
        super(sessionFactory);
    }
}
