package de.fau.cs.mad.kwikshop.server.dao;

import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.Restrictions;

import de.fau.cs.mad.kwikshop.server.sorting.Supermarket;
import de.fau.cs.mad.kwikshop.server.sorting.SupermarketChain;
import io.dropwizard.hibernate.AbstractDAO;

public class SupermarketDAO extends AbstractDAO<Supermarket> {

    /**
     * Creates a new DAO with a given session provider.
     *
     * @param sessionFactory a session provider
     */
    public SupermarketDAO(SessionFactory sessionFactory) {
        super(sessionFactory);
    }

    public Supermarket getByPlaceId(String placeId) {
        final Session session = currentSession();
        Supermarket result = null;

        Criteria criteria = session.createCriteria(Supermarket.class)
                .add(Restrictions.eq("placeId", placeId));

        Object tmp = criteria.uniqueResult();
        if (tmp != null) {
            result = (Supermarket) tmp;
        }

        return result;
    }

    public Supermarket getGlobalBySupermarketChain(SupermarketChain supermarketChain) {
        /* This is a bit hacky, but it works */
        return getByPlaceId(supermarketChain.getName());
    }

    public Supermarket createSupermarkt(Supermarket supermarket) {
        return persist(supermarket);
    }

}
