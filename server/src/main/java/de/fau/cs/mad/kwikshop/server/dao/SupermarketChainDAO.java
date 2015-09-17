package de.fau.cs.mad.kwikshop.server.dao;

import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.Restrictions;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.List;

import de.fau.cs.mad.kwikshop.server.sorting.Supermarket;
import de.fau.cs.mad.kwikshop.server.sorting.SupermarketChain;
import io.dropwizard.hibernate.AbstractDAO;

public class SupermarketChainDAO extends AbstractDAO<SupermarketChain> {

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

        return supermarketChainList;
    }

    public Supermarket getGlobalSupermarket(SupermarketChain supermarketChain) {
        final Session session = currentSession();

        Criteria criteria = session.createCriteria(Supermarket.class)
                .add(Restrictions.eq("placeId", supermarketChain.getName()));

        Supermarket globalSupermarket = (Supermarket) criteria.uniqueResult();

        return globalSupermarket;

    }

    public SupermarketChain getByName(String name) {
        final Session session = currentSession();

        Criteria criteria = session.createCriteria(SupermarketChain.class)
                .add(Restrictions.eq("name", name));

        SupermarketChain supermarketChain = (SupermarketChain) criteria.uniqueResult();

        return supermarketChain;

    }

    public void setUp() {
        final Session session = currentSession();

        try {
            File file = new File("./data/chains.txt");
            FileReader fileReader = new FileReader(file);
            BufferedReader bufferedReader = new BufferedReader(fileReader);
            String name;
            while ((name = bufferedReader.readLine()) != null) {
                if(getByName(name) != null)
                    continue;

                if(name.length() < 3)
                    continue;

                SupermarketChain chain = new SupermarketChain(name);
                persist(chain);

                /* Every chain has its own global graph - implemented as a simple supermarket */
                Supermarket globalSupermarket = new Supermarket();
                globalSupermarket.setSupermarketChain(chain);
                globalSupermarket.setPlaceId(name); /* Set the SupermarketChain's name as the Supermarket's PlaceId */
                supermarketDAO.createSupermarket(globalSupermarket);

                System.out.println("Created SupermarketChain " + name);
            }
            fileReader.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
