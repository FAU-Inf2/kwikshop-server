package de.fau.cs.mad.kwikshop.server.dao;

import de.fau.cs.mad.kwikshop.common.Group;
import de.fau.cs.mad.kwikshop.common.User;
import de.fau.cs.mad.kwikshop.common.util.NamedQueryConstants;
import io.dropwizard.hibernate.AbstractDAO;
import org.hibernate.Query;
import org.hibernate.SessionFactory;

import java.util.List;

public class GroupDAO extends AbstractDAO<Group> {


    /**
     * Creates a new DAO with a given session provider.
     *
     * @param sessionFactory a session provider
     */
    public GroupDAO(SessionFactory sessionFactory) {
        super(sessionFactory);
    }

    public Group getById(User user, int groupId) {

        Query query = namedQuery(NamedQueryConstants.GROUP_GET_BY_ID)
                .setParameter(NamedQueryConstants.USER_ID, user.getId())
                .setParameter(NamedQueryConstants.GROUP_ID, groupId);

        List<Group> groups = list(query);

        if(groups.size() == 0) {
            return null;
        } else if(groups.size() == 1) {
            return groups.get(0);
        } else {
            throw new UnsupportedOperationException("Query for Group by Id yielded more than one result");
        }
    }

    public Group createGroup(User user, Group group) {

        group.setServerId(0);
        group.setOwnerId(user.getId());

        return persist(group);
    }

}
