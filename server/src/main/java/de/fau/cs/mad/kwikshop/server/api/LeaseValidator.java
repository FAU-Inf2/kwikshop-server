package de.fau.cs.mad.kwikshop.server.api;

import de.fau.cs.mad.kwikshop.common.ArgumentNullException;
import de.fau.cs.mad.kwikshop.common.SynchronizationLease;
import de.fau.cs.mad.kwikshop.server.dao.SynchronizationLeaseDAO;
import de.fau.cs.mad.kwikshop.server.exceptions.LeaseNotFoundException;

import java.util.Date;

public class LeaseValidator {

    SynchronizationLeaseDAO leaseDAO;



    public LeaseValidator(SynchronizationLeaseDAO leaseDAO) {
        if(leaseDAO == null) {
            throw new ArgumentNullException("leaseDAO");
        }

        this.leaseDAO = leaseDAO;
    }



    public boolean isLeaseStillValid(int leaseId) {

        // 0 is not a valid id of a lease
        if(leaseId == 0) {
            return false;
        }

        // look up the lease in the database
        SynchronizationLease lease;
        try {

            lease = leaseDAO.getLease(leaseId);

        } catch (LeaseNotFoundException e) {

            // unknown lease is not valid
            return false;
        }


        long expirationTime = lease.getExpirationTime().getTime();
        long now = new Date().getTime();

        boolean valid  = now < expirationTime;

        // if lease is invalid, delete it from the database
        if(!valid) {
            leaseDAO.deleteLease(lease);
        }


        return valid;
    }

}
