package de.fau.cs.mad.kwikshop.server.dao;

import de.fau.cs.mad.kwikshop.common.LastLocation;
import de.fau.cs.mad.kwikshop.common.User;
import de.fau.cs.mad.kwikshop.common.util.NamedQueryConstants;
import io.dropwizard.hibernate.AbstractDAO;
import org.hibernate.Query;
import org.hibernate.SessionFactory;

import java.util.List;

public class LocationDAO extends AbstractDAO<LastLocation> {


    /**
     * Creates a new DAO with a given session provider.
     *
     * @param sessionFactory a session provider
     */
    public LocationDAO(SessionFactory sessionFactory) {
        super(sessionFactory);
    }

    public LastLocation getById(User user, int unitId) {

        Query query = namedQuery(NamedQueryConstants.LOCATION_GET_BY_ID)
                .setParameter(NamedQueryConstants.USER_ID, user.getId())
                .setParameter(NamedQueryConstants.LOCATION_ID, unitId);
        List<LastLocation> locations = list(query);

        if(locations.size() == 0) {
            return null;
        } else if(locations.size() == 1) {
            return locations.get(0);
        } else {
            throw new UnsupportedOperationException("Query for LastLocation by Id yielded more than one result");
        }
    }

    public LastLocation createLocation(User user, LastLocation location) {
        location.setServerId(0);
        location.setOwnerId(user.getId());

        return persist(location);
    }

}
