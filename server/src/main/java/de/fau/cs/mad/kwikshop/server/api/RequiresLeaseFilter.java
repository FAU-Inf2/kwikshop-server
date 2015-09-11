package de.fau.cs.mad.kwikshop.server.api;

import de.fau.cs.mad.kwikshop.common.ArgumentNullException;
import de.fau.cs.mad.kwikshop.common.SynchronizationLease;
import de.fau.cs.mad.kwikshop.common.rest.Constants;
import de.fau.cs.mad.kwikshop.common.rest.LeaseResource;
import de.fau.cs.mad.kwikshop.server.dao.SynchronizationLeaseDAO;
import org.hibernate.CacheMode;
import org.hibernate.FlushMode;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.context.internal.ManagedSessionContext;

import javax.annotation.Priority;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.ext.Provider;
import java.io.IOException;


@Provider
@Priority(200)
public class RequiresLeaseFilter implements ContainerRequestFilter {


    private final SynchronizationLeaseDAO leaseDAO;
    private final LeaseValidator leaseValidator;
    private final SessionFactory sessionFactory;

    public RequiresLeaseFilter(SessionFactory sessionFactory, SynchronizationLeaseDAO leaseDAO, LeaseValidator leaseValidator) {


        if(sessionFactory == null) {
            throw new ArgumentNullException("sessionFactory");
        }

        if(leaseDAO == null) {
            throw new ArgumentNullException("leaseDAO");
        }

        if(leaseValidator == null) {
            throw new ArgumentNullException("leaseValidator");
        }

        this.sessionFactory = sessionFactory;
        this.leaseDAO = leaseDAO;
        this.leaseValidator = leaseValidator;
    }

    @Override
    public void filter(ContainerRequestContext requestContext) throws IOException {

        Session session = sessionFactory.openSession();
        session.setDefaultReadOnly(true);
        session.setCacheMode(CacheMode.NORMAL);
        session.setFlushMode(FlushMode.MANUAL);
        ManagedSessionContext.bind(session);
        // DropWizard magic enabled from this point.

        String clientId = requestContext.getHeaderString(Constants.KWIKSHOP_CLIENT_ID);

        SynchronizationLease lease = leaseDAO.getLeaseByClientId(clientId);
        if(!leaseValidator.isLeaseStillValid(lease)) {
            throw new WebApplicationException(LeaseResource.LEASE_DENIED_STATUS_CODE);
        }


        session.close();
        // DropWizard magic disabled from this point.
    }


}
