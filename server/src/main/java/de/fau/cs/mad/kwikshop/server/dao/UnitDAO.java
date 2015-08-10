package de.fau.cs.mad.kwikshop.server.dao;

import de.fau.cs.mad.kwikshop.common.Unit;
import de.fau.cs.mad.kwikshop.common.User;
import de.fau.cs.mad.kwikshop.common.util.NamedQueryConstants;
import io.dropwizard.hibernate.AbstractDAO;
import org.hibernate.Query;
import org.hibernate.SessionFactory;

import java.util.List;

public class UnitDAO extends AbstractDAO<Unit> {


    /**
     * Creates a new DAO with a given session provider.
     *
     * @param sessionFactory a session provider
     */
    public UnitDAO(SessionFactory sessionFactory) {
        super(sessionFactory);
    }

    public Unit getById(User user, int unitId) {

        Query query = namedQuery(NamedQueryConstants.UNIT_GET_BY_ID)
                .setParameter(NamedQueryConstants.USER_ID, user.getId())
                .setParameter(NamedQueryConstants.UNIT_ID, unitId);
        List<Unit> units = list(query);

        if(units.size() == 0) {
            return null;
        } else if(units.size() == 1) {
            return units.get(0);
        } else {
            throw new UnsupportedOperationException("Query for Unit by Id yielded more than one result");
        }
    }

    public Unit createUnit(User user, Unit unit) {
        unit.setServerId(0);
        unit.setOwnerId(user.getId());

        return persist(unit);
    }

}
