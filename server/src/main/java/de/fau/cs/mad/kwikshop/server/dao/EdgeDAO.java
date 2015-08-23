package de.fau.cs.mad.kwikshop.server.dao;

import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.Restrictions;

import de.fau.cs.mad.kwikshop.common.sorting.BoughtItem;
import de.fau.cs.mad.kwikshop.server.sorting.Edge;
import io.dropwizard.hibernate.AbstractDAO;

public class EdgeDAO extends AbstractDAO<Edge> {

    /**
     * Creates a new DAO with a given session provider.
     *
     * @param sessionFactory a session provider
     */
    public EdgeDAO(SessionFactory sessionFactory) {
        super(sessionFactory);
    }

    public Edge getByFromTo(BoughtItem from, BoughtItem to) {

        final Session session = currentSession();
        Edge result = null;

        try {
            Criteria criteria = session.createCriteria(Edge.class)
                    .add(Restrictions.eq("from", from.getId()))
                    .add(Restrictions.eq("to", to.getId()));

            Object tmp = criteria.uniqueResult();
            if (tmp != null) {
                result = (Edge) tmp;
            }
        } finally {
            session.close();
        }
        return result;
    }

    public Edge createEdge(Edge edge) {
        return persist(edge);
    }

}
