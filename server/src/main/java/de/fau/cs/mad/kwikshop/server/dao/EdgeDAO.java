package de.fau.cs.mad.kwikshop.server.dao;

import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.Restrictions;

import java.util.List;

import de.fau.cs.mad.kwikshop.common.sorting.BoughtItem;
import de.fau.cs.mad.kwikshop.server.sorting.Edge;
import de.fau.cs.mad.kwikshop.server.sorting.Supermarket;
import io.dropwizard.hibernate.AbstractDAO;

public class EdgeDAO extends AbstractDAO<Edge> {

    private BoughtItemDAO boughtItemDAO;

    /**
     * Creates a new DAO with a given session provider.
     *
     * @param sessionFactory a session provider
     */
    public EdgeDAO(SessionFactory sessionFactory, BoughtItemDAO boughtItemDAO) {
        super(sessionFactory);
        this.boughtItemDAO = boughtItemDAO;
    }

    public Edge getByFromTo(BoughtItem from, BoughtItem to, Supermarket supermarket) {
        final Session session = currentSession();

        List<Edge> result = null;

        Criteria criteria = session.createCriteria(Edge.class)
                .add(Restrictions.eq("from.id", from.getId()))
                .add(Restrictions.eq("to.id", to.getId()))
                .add(Restrictions.eq("supermarket.id", supermarket.getId()));

        result = criteria.list();

        return result.size() > 0 ? result.get(0) : null;
    }

    public List<Edge> getBySupermarket(Supermarket supermarket) {
        final Session session = currentSession();

        List<Edge> result = null;

        Criteria criteria = session.createCriteria(Edge.class)
                .add(Restrictions.eq("supermarket.id", supermarket.getId()));

        result = criteria.list();
        return result;
    }

    public List<Edge> getByFrom(BoughtItem from, Supermarket supermarket) {
        final Session session = currentSession();

        Criteria criteria = session.createCriteria(Edge.class)
                .add(Restrictions.eq("from.id", from.getId()))
                .add(Restrictions.eq("supermarket.id", supermarket.getId()));

        return criteria.list();
    }

    public List<Edge> getByTo(BoughtItem to, Supermarket supermarket) {
        final Session session = currentSession();

        Criteria criteria = session.createCriteria(Edge.class)
                .add(Restrictions.eq("to.id", to.getId()))
                .add(Restrictions.eq("supermarket.id", supermarket.getId()));

        return criteria.list();
    }

    public Edge createEdge(Edge edge) {
        currentSession().merge(edge);
        return persist(edge);
    }

    public void deleteEdge(Edge edge) {
        currentSession().merge(edge);
        currentSession().delete(edge);
    }
}
