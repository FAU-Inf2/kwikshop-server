package de.fau.cs.mad.kwikshop.server.api;

import de.fau.cs.mad.kwikshop.common.*;
import de.fau.cs.mad.kwikshop.common.interfaces.DomainListObjectServer;
import de.fau.cs.mad.kwikshop.common.rest.Constants;
import de.fau.cs.mad.kwikshop.common.rest.LeaseResource;
import de.fau.cs.mad.kwikshop.common.util.StringHelper;
import de.fau.cs.mad.kwikshop.server.dao.ListDAO;
import de.fau.cs.mad.kwikshop.server.dao.SynchronizationLeaseDAO;
import de.fau.cs.mad.kwikshop.server.exceptions.LeaseNotFoundException;
import de.fau.cs.mad.kwikshop.server.exceptions.ListNotFoundException;
import io.dropwizard.auth.Auth;
import io.dropwizard.hibernate.UnitOfWork;

import javax.validation.constraints.NotNull;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.TimeUnit;

public class LeaseResourceImpl implements LeaseResource {

    private static final Object leaseCreationLock = new Object();

    private static final long LEASE_LENGTH = TimeUnit.MILLISECONDS.convert(2, TimeUnit.MINUTES);


    SynchronizationLeaseDAO leaseDAO;
    ListDAO<RecipeServer> recipeDAO;
    ListDAO<ShoppingListServer> shoppingListDAO;
    ListDAO<ShoppingListServer> sharedShoppingListDAO;
    LeaseValidator leaseValidator;


    public LeaseResourceImpl(@NotNull SynchronizationLeaseDAO leaseDAO, @NotNull ListDAO<RecipeServer> recipeDAO,
                             @NotNull ListDAO<ShoppingListServer> shoppingListDAO,
                             @NotNull ListDAO<ShoppingListServer> sharedShoppingListDAO,
                             @NotNull LeaseValidator leaseValidator) {

        if (leaseDAO == null) {
            throw new ArgumentNullException("leaseDAO");
        }

        if (recipeDAO == null) {
            throw new ArgumentNullException("recipeDAO");
        }

        if (shoppingListDAO == null) {
            throw new ArgumentNullException("shoppingListDAO");
        }

        if (sharedShoppingListDAO == null) {
            throw new ArgumentNullException("sharedShoppingListDAO");
        }

        if (leaseValidator == null) {
            throw new ArgumentNullException("leaseValidator");
        }

        this.leaseDAO = leaseDAO;
        this.recipeDAO = recipeDAO;
        this.shoppingListDAO = shoppingListDAO;
        this.sharedShoppingListDAO = sharedShoppingListDAO;
        this.leaseValidator = leaseValidator;
    }


    @PUT
    @UnitOfWork
    @Produces(MediaType.APPLICATION_JSON)
    public SynchronizationLease getSynchronizationLease(@Auth User user,
                                                        @HeaderParam(Constants.KWIKSHOP_CLIENT_ID) String clientId) {

        LeaseHelpers.ensureClientIdIsValid(clientId);

        // check if a lease can currently be granted

        //  Only a single lease per user can be granted


        SynchronizationLease currentLease = leaseDAO.getLeaseByUser(user);
        if (currentLease != null && leaseValidator.isLeaseStillValid(currentLease.getId())) {
            throw new WebApplicationException(LEASE_DENIED_STATUS_CODE);
        }


        // Only a single lease per client can be granted
        currentLease = leaseDAO.getLeaseByClientId(clientId);
        if (currentLease != null && leaseValidator.isLeaseStillValid(currentLease.getId())) {
            throw new WebApplicationException(LEASE_DENIED_STATUS_CODE);
        }

        // there must not be a lease for any other user or any other client that locks any of the users
        // (shared) shopping lists or recipes

        synchronized (leaseCreationLock) {

            //get the current lease ids from all lists we need to lock

            Set<Integer> leaseIds = new HashSet<>();
            leaseIds.addAll(shoppingListDAO.getListLeases(user));
            leaseIds.addAll(recipeDAO.getListLeases(user));
            leaseIds.addAll(sharedShoppingListDAO.getListLeases(user));

            // if any of the lease is still valid, we cannot grant the lease
            for (int leaseId : leaseIds) {
                if (leaseValidator.isLeaseStillValid(leaseId)) {
                    throw new WebApplicationException(LEASE_DENIED_STATUS_CODE);
                }
            }

            // all lists seems to be non-locked => create a new lease
            SynchronizationLease lease = leaseDAO.createLease(user, clientId, getNewLeaseExpirationTime());

            setLeaseIds(shoppingListDAO, user, lease);
            setLeaseIds(recipeDAO, user, lease);
            setLeaseIds(sharedShoppingListDAO, user, lease);

            return lease;
        }

    }

    @POST
    @UnitOfWork
    @Path("{leaseId}")
    @Produces(MediaType.APPLICATION_JSON)
    public SynchronizationLease extendSynchronizationLease(@Auth User user,
                                                           @PathParam("leaseId") int leaseId,
                                                           @HeaderParam(Constants.KWIKSHOP_CLIENT_ID) String clientId) {
        // make sure the client provided a valid id
        LeaseHelpers.ensureClientIdIsValid(clientId);

        // try to find the specified lease in the database
        SynchronizationLease lease;
        try {

            lease = leaseDAO.getLease(leaseId, user);

        } catch (LeaseNotFoundException e) {

            //lease not found => return 404
            throw new WebApplicationException(Response.Status.NOT_FOUND);
        }

        synchronized (leaseCreationLock) {

            //return error if the lease is no longer valid
            if (!leaseValidator.isLeaseStillValid(leaseId)) {
                throw new WebApplicationException(LEASE_DENIED_STATUS_CODE);
            }

            // extend the lease
            lease.setExpirationTime(getNewLeaseExpirationTime());
            lease = leaseDAO.updateLease(lease);

            return lease;
        }

    }

    @DELETE
    @UnitOfWork
    @Path("{leaseId}")
    public void removeSynchronizationLease(@Auth User user,
                                           @PathParam("leaseId") int leaseId,
                                           @HeaderParam(Constants.KWIKSHOP_CLIENT_ID) String clientId) {

        // make sure the client provided a valid id
        LeaseHelpers.ensureClientIdIsValid(clientId);

        // try to find the specified lease in the database
        SynchronizationLease lease;
        try {

            lease = leaseDAO.getLease(leaseId, user);

        } catch (LeaseNotFoundException e) {

            //lease not found => return 404
            throw new WebApplicationException(Response.Status.NOT_FOUND);
        }


        // delte the lease
        synchronized (leaseCreationLock) {

            leaseDAO.deleteLease(user, lease);
        }

    }




    /**
     * Sets the leaseId for all of the Lists provided by the specified DAO and User to the specified value
     */
    private <T extends DomainListObjectServer> void setLeaseIds(ListDAO<T> listDAO, User user, SynchronizationLease lease) {
        for (T list : listDAO.getLists(user)) {
            list.setLeaseId(lease.getId());
            try {
                listDAO.updateList(user, list);
            } catch (ListNotFoundException e) {
                //this should not happen
                throw new UnsupportedOperationException();
            }
        }
    }

    private static Date getNewLeaseExpirationTime() {
        return new Date(new Date().getTime() + LEASE_LENGTH);
    }

}
