package de.fau.cs.mad.kwikshop.server.api;

import de.fau.cs.mad.kwikshop.common.ArgumentNullException;
import de.fau.cs.mad.kwikshop.common.SynchronizationLease;
import de.fau.cs.mad.kwikshop.common.rest.Constants;
import de.fau.cs.mad.kwikshop.common.rest.LeaseResource;
import de.fau.cs.mad.kwikshop.server.dao.SynchronizationLeaseDAO;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.ext.Provider;
import java.io.IOException;


@Provider
public class RequiresLeaseFilter implements ContainerRequestFilter {


    private final SynchronizationLeaseDAO leaseDAO;
    private final LeaseValidator leaseValidator;


    public RequiresLeaseFilter(SynchronizationLeaseDAO leaseDAO, LeaseValidator leaseValidator) {

        if(leaseDAO == null) {
            throw new ArgumentNullException("leaseDAO");
        }

        if(leaseValidator == null) {
            throw new ArgumentNullException("leaseValidator");
        }

        this.leaseDAO = leaseDAO;
        this.leaseValidator = leaseValidator;
    }

    @Override
    public void filter(ContainerRequestContext requestContext) throws IOException {

        String clientId = requestContext.getHeaderString(Constants.KWIKSHOP_CLIENT_ID);

        LeaseHelpers.ensureClientIdIsValid(clientId);

        SynchronizationLease lease = leaseDAO.getLeaseByClientId(clientId);
        if(!leaseValidator.isLeaseStillValid(lease)) {
            throw new WebApplicationException(LeaseResource.LEASE_DENIED_STATUS_CODE);
        }

    }


}
