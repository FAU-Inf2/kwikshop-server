package de.fau.cs.mad.kwikshop.server.dao;

import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.Restrictions;

import java.util.List;

import de.fau.cs.mad.kwikshop.server.sorting.Supermarket;
import de.fau.cs.mad.kwikshop.server.sorting.SupermarketChain;
import io.dropwizard.hibernate.AbstractDAO;

public class SupermarketChainDAO extends AbstractDAO<SupermarketChain> {

    /* TODO: Use actual chains */
    private String[] supermarketChainNames = {"Kwik-E-Mart", "Foobar"};

    private SupermarketDAO supermarketDAO;

    /**
     * Creates a new DAO with a given session provider.
     *
     * @param sessionFactory a session provider
     */
    public SupermarketChainDAO(SessionFactory sessionFactory, SupermarketDAO supermarketDAO) {
        super(sessionFactory);
        this.supermarketDAO = supermarketDAO;
    }

    public List<SupermarketChain> getAll() {
        final Session session = currentSession();

        Criteria criteria = session.createCriteria(SupermarketChain.class);

        List<SupermarketChain> supermarketChainList = criteria.list();

        /* Create standard SupermarketChains */
        if(supermarketChainList.size() == 0) {
            for(String name : supermarketChainNames) {
                SupermarketChain chain = new SupermarketChain(name);
                session.save(chain);
                supermarketChainList.add(chain);

                /* Every chain has it's own global graph - implemented as a simple supermarket */
                Supermarket globalSupermarket = new Supermarket();
                globalSupermarket.setSupermarketChain(chain);
                globalSupermarket.setPlaceId(name); /* Set the SupermarketChain's name as the Supermarket's PlaceId */
                supermarketDAO.createSupermarkt(globalSupermarket);
            }
        }

        return supermarketChainList;
    }

    public Supermarket getGlobalSupermarket(SupermarketChain supermarketChain) {
        final Session session = currentSession();

        Criteria criteria = session.createCriteria(Supermarket.class)
                .add(Restrictions.eq("placeId", supermarketChain.getName()));

        Supermarket globalSupermarket = (Supermarket) criteria.uniqueResult();

        return globalSupermarket;

    }

}
