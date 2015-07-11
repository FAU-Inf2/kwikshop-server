package de.fau.cs.mad.kwikshop.server.dao;

import de.fau.cs.mad.kwikshop.common.User;
import io.dropwizard.hibernate.AbstractDAO;

import org.hibernate.Criteria;
import org.hibernate.Query;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.MatchMode;
import org.hibernate.criterion.Restrictions;

import java.util.List;

/**
 * Created by Andreas Kumlehn on 3/27/15.
 */
public class UserDAO extends AbstractDAO<User> {

    private final SessionFactory sessionFactory;

    /**
     * Creates a new DAO with a given session provider.
     *
     * @param sessionFactory a session provider
     */
    public UserDAO(SessionFactory sessionFactory) {
        super(sessionFactory);
        this.sessionFactory = sessionFactory;
    }

    public User create(User user) {
        return persist(user);
    }

    public User updateOrCreate(String id, User modified) {
        User stored = this.findById(id);
        if(stored == null)
            stored = new User(id);

        stored.setSessionToken(modified.getSessionToken());

        this.persist(stored);
        return stored;
    }

    public boolean delete(User user) {
        if (this.findById(user.getId()) == null) {
            return false;
        }

//        Session session = sessionFactory.openSession();
//        ManagedSessionContext.bind(session);
//        Transaction trans = super.currentSession().getTransaction();
//        trans.begin();
        currentSession().delete(user);
//        session.delete(user);
//        trans.commit();
//        session.close();
//        ManagedSessionContext.unbind(this.sessionFactory);
        return true;
    }

    public User findById(String id) {
        return super.get(id);
    }

    public List<User> listAll() {
        Query query = super.currentSession().createQuery("SELECT u FROM User u");
        List<User> result = super.list(query);
        return result;
    }

    public List<User> findByName(String firstName, String lastName) {
        Criteria criteria = criteria();

        if (firstName != null) {
            criteria = criteria.add(Restrictions.like("firstName", firstName, MatchMode.START));
        }
        if (lastName != null) {
            criteria = criteria.add(Restrictions.like("lastName", lastName, MatchMode.START));
        }
        List<User> list = list(criteria);
        return list;
    }

    public List<User> findByLastName(String lastName) {
        Query query = namedQuery("User.findByLastName");
        query.setParameter("lastName", lastName);
        List<User> list = list(query);
        return list;
    }
}
