package de.fau.cs.mad.kwikshop.server.dao;

import de.fau.cs.mad.kwikshop.common.SynchronizationLease;
import de.fau.cs.mad.kwikshop.common.User;
import de.fau.cs.mad.kwikshop.common.util.NamedQueryConstants;
import de.fau.cs.mad.kwikshop.server.exceptions.LeaseNotFoundException;
import io.dropwizard.hibernate.AbstractDAO;
import org.hibernate.Query;
import org.hibernate.SessionFactory;


import java.util.Date;
import java.util.List;

import static de.fau.cs.mad.kwikshop.common.util.NamedQueryConstants.*;

public class SynchronizationLeaseDAO extends AbstractDAO<SynchronizationLease> {

    /**
     * Creates a new DAO with a given session provider.
     *
     * @param sessionFactory a session provider
     */
    public SynchronizationLeaseDAO(SessionFactory sessionFactory) {
        super(sessionFactory);
    }



    public SynchronizationLease getLeaseByUser(User user) {

        return queryLease(
                namedQuery(SYNCHRONIZATIONLEASE_GET_ALL_FOR_USER)
                    .setParameter(USER_ID, user.getId()));
    }

    public SynchronizationLease getLeaseByClientId(String clientId) {
        return queryLease(
                namedQuery(SYNCHRONIZATIONLEASE_GET_ALL_FOR_CLIENT)
                        .setParameter(CLIENT_ID, clientId)
        );
    }

    public SynchronizationLease getLease(int leaseId) throws LeaseNotFoundException {

        SynchronizationLease lease = queryLease(
                namedQuery(SYNCHRONIZATIONLEASE_GET_BY_ID)
                .setParameter(SYNCHRONIZATIONLEASE_ID, leaseId)
        );

        if(lease == null) {
            throw new LeaseNotFoundException(String.format("Lease (id %s) not found", lease));
        }

        return lease;
    }

    public SynchronizationLease getLease(int leaseId, User user) throws LeaseNotFoundException{

        SynchronizationLease lease = queryLease(
                namedQuery(NamedQueryConstants.SYNCHRONIZATIONLEASE_GET_BY_ID_AND_OWNER)
                        .setParameter(NamedQueryConstants.USER_ID, user.getId())
                        .setParameter(NamedQueryConstants.SYNCHRONIZATIONLEASE_ID, leaseId));

        if(lease == null) {
            throw new LeaseNotFoundException(String.format("Could not find synchronization lease for User %s with id %s", user.getId(), leaseId));
        }
        return lease;

    }

    public SynchronizationLease createLease(User user, String clientId, Date expirationTime) {

        SynchronizationLease lease = new SynchronizationLease();
        lease.setId(0);
        lease.setClientId(clientId);
        lease.setUserId(user.getId());
        lease.setExpirationTime(expirationTime);

        return persist(lease);
    }

    public SynchronizationLease updateLease(SynchronizationLease lease) {

        currentSession().update(lease);
        return lease;

    }

    public boolean deleteLease(User user, SynchronizationLease lease) {

        if(!lease.getUserId().equals(user.getId())) {
            throw new UnsupportedOperationException();
        }

        lease = getLeaseByUser(user);

        if(lease == null || lease.getId() != lease.getId()) {
            return false;
        } else {
            return deleteLease(lease);
        }

    }

    public boolean deleteLease(SynchronizationLease lease) {

        currentSession().delete(lease);
        return true;
    }



    private SynchronizationLease queryLease(Query query) {

        List<SynchronizationLease> leases = list(query);

        if(leases.size() == 0) {

            return null;

        } else if(leases.size() == 1) {

            return leases.get(0);

        } else {

            throw new UnsupportedOperationException("Query for SynchronizationLease by Id yielded more than one result");
        }
    }
}
