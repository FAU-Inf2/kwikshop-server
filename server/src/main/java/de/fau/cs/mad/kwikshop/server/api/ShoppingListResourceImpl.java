package de.fau.cs.mad.kwikshop.server.api;

import com.wordnik.swagger.annotations.ApiParam;
import de.fau.cs.mad.kwikshop.common.Item;
import de.fau.cs.mad.kwikshop.common.ShoppingListServer;
import de.fau.cs.mad.kwikshop.common.User;
import de.fau.cs.mad.kwikshop.common.rest.ShoppingListResource;
import de.fau.cs.mad.kwikshop.server.dao.ListDAO;
import de.fau.cs.mad.kwikshop.server.exceptions.ItemNotFoundException;
import de.fau.cs.mad.kwikshop.server.exceptions.ListNotFoundException;
import io.dropwizard.auth.Auth;
import io.dropwizard.hibernate.UnitOfWork;


import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.List;

@Path("shoppinglist")
public class ShoppingListResourceImpl implements ShoppingListResource {


    private final ListDAO<ShoppingListServer> shoppingListDAO;


    public ShoppingListResourceImpl(ListDAO<ShoppingListServer> shoppingListDAO) {

        if(shoppingListDAO == null) {
            throw new IllegalArgumentException("'shoppingListDAO' must not be null");
        }

        this.shoppingListDAO = shoppingListDAO;
    }



    @Override
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @UnitOfWork
    public List<ShoppingListServer> getList(@Auth User user) {
        return shoppingListDAO.getLists(user);
    }

    @Override
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("{listId}")
    @UnitOfWork
    public ShoppingListServer getList(@Auth User user, @PathParam("listId") String listId) {

        try {
            return shoppingListDAO.getListById(user, Integer.parseInt(listId));
        } catch (ListNotFoundException e) {
            throw new WebApplicationException(Response.Status.NOT_FOUND);
        }

    }

    @Override
    @PUT
    @UnitOfWork
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public ShoppingListServer createList(@Auth User user, ShoppingListServer shoppingList) {
        return shoppingListDAO.createList(user, shoppingList);
    }

    @Override
    @POST
    @UnitOfWork
    @Path("{listId}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public ShoppingListServer updateList(@Auth User user,
                                   @PathParam("listId") int listId, ShoppingListServer shoppingList,
                                   @QueryParam("updateItems") boolean updateItems) {

        if(shoppingList.getId() != listId) {
            throw new WebApplicationException(Response.Status.BAD_REQUEST);
        }

        try {
            return shoppingListDAO.updateList(user, shoppingList, updateItems);
        } catch (ListNotFoundException e) {
            throw new WebApplicationException(Response.Status.NOT_FOUND);
        }

    }

    @Override
    @DELETE
    @UnitOfWork
    @Path("{listId}")
    public void deleteList(@Auth User user, @PathParam("listId") int listId) {
        boolean listFound = shoppingListDAO.deleteList(user, listId);
        if(!listFound) {
            throw new WebApplicationException(Response.Status.NOT_FOUND);
        }
    }

    @Override
    @GET
    @UnitOfWork
    @Path("{listId}/{itemId}")
    public Item getListItem(@Auth User user,
                            @PathParam("listId") int listId,
                            @PathParam("itemId") int itemId) {

        try {

            return shoppingListDAO.getListItem(user, listId, itemId);

        } catch (ListNotFoundException | ItemNotFoundException e) {

            throw new WebApplicationException(Response.Status.NOT_FOUND);
        }


    }

    @Override
    @PUT
    @UnitOfWork
    @Path("{listId}/newItem")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Item createItem(@Auth User user,
                           @PathParam("listId") int listId,
                           @ApiParam(value = "Item to create", required = true) Item newItem) {

        try {

            return shoppingListDAO.addListItem(user, listId, newItem);

        } catch (ListNotFoundException e) {

            throw new WebApplicationException(Response.Status.NOT_FOUND);
        }


    }


    @Override
    @POST
    @UnitOfWork
    @Path("{listId}/{itemId}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Item updateItem(@Auth User user,
                           @ApiParam(value = "id of the list the item belongs to", required = true) @PathParam("listId") int listId,
                           @ApiParam(value = "id of the Item to update", required = true) @PathParam("itemId") int itemId,
                           @ApiParam(value = "new details of the specified item", required = true) Item item) {

        if(item.getId() != itemId) {
            throw new WebApplicationException(Response.Status.BAD_REQUEST);
        }

        try {

            return shoppingListDAO.updateListItem(user, listId, item);

        } catch (ListNotFoundException | ItemNotFoundException e) {

            throw new WebApplicationException(Response.Status.NOT_FOUND);
        }

    }

    @Override
    @DELETE
    @UnitOfWork
    @Path("{listId}/{itemId}")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Produces(MediaType.APPLICATION_JSON)
    public void deleteListItem(@Auth User user,
                               @ApiParam(value = "id of the list the item belongs to", required = true) @PathParam("listId") int listId,
                               @ApiParam(value = "id of the Item to update", required = true) @PathParam("itemId") int itemId) {

        try {

            shoppingListDAO.deleteListItem(user, listId, itemId);

        } catch (ListNotFoundException e) {

            throw new WebApplicationException(Response.Status.NOT_FOUND);
        }

    }
}
