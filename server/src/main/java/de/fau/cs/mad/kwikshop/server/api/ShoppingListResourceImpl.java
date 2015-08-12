package de.fau.cs.mad.kwikshop.server.api;

import com.wordnik.swagger.annotations.ApiParam;
import de.fau.cs.mad.kwikshop.common.DeletionInfo;
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
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

@Path("shoppinglist")
public class ShoppingListResourceImpl implements ShoppingListResource {


    private final ListDAO<ShoppingListServer> shoppingListDAO;
    private final ListDAO<ShoppingListServer> sharedShoppingListDAO;

    public ShoppingListResourceImpl(ListDAO<ShoppingListServer> shoppingListDAO, ListDAO<ShoppingListServer> sharedShoppingListDAO) {

        if(shoppingListDAO == null) {
            throw new IllegalArgumentException("'shoppingListDAO' must not be null");
        }

        if(sharedShoppingListDAO == null) {
            throw new IllegalArgumentException("'sharedShoppingListDAO' must not be null");
        }

        this.shoppingListDAO = shoppingListDAO;
        this.sharedShoppingListDAO = sharedShoppingListDAO;
    }



    @Override
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @UnitOfWork
    public List<ShoppingListServer> getList(@Auth User user) {
        List<ShoppingListServer> lists = new ArrayList<>();
        lists.addAll(shoppingListDAO.getLists(user));
        lists.addAll(sharedShoppingListDAO.getLists(user)); /* Add all shared Lists */
        return lists;
        //return shoppingListDAO.getLists(user);
    }

    @Override
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("{listId}")
    @UnitOfWork
    public ShoppingListServer getList(@Auth User user, @PathParam("listId") int listId) {

        try {
            return shoppingListDAO.getListById(user, listId);
        } catch (ListNotFoundException e) {
            try {
                return sharedShoppingListDAO.getListById(user, listId);
            } catch (ListNotFoundException e2) {
                throw new WebApplicationException(Response.Status.NOT_FOUND);
            }
        }

    }

    @Override
    @PUT
    @UnitOfWork
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public ShoppingListServer createList(@Auth User user, ShoppingListServer shoppingList) {

        if(shoppingList.getId() != 0) {
            throw new WebApplicationException(Response.Status.BAD_REQUEST);
        }

        return shoppingListDAO.createList(user, shoppingList);
    }

    @Override
    @POST
    @UnitOfWork
    @Path("{listId}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public ShoppingListServer updateList(@Auth User user,
                                   @PathParam("listId") int listId, ShoppingListServer shoppingList) {

        if(shoppingList.getId() != listId) {
            throw new WebApplicationException(Response.Status.BAD_REQUEST);
        }

        try {
            return shoppingListDAO.updateList(user, shoppingList);
        } catch (ListNotFoundException e) {
            try {
                return sharedShoppingListDAO.updateList(user, shoppingList);
            } catch (ListNotFoundException e2) {
                throw new WebApplicationException(Response.Status.NOT_FOUND);
            }
        }

    }

    @Override
    @DELETE
    @UnitOfWork
    @Path("{listId}")
    public void deleteList(@Auth User user, @PathParam("listId") int listId) {
        boolean listFound = shoppingListDAO.deleteList(user, listId);
        if(!listFound) {
            listFound = sharedShoppingListDAO.deleteList(user, listId);

            if(!listFound)
                throw new WebApplicationException(Response.Status.NOT_FOUND);
        }
    }

    @GET
    @UnitOfWork
    @Path("/deleted")
    @Produces(MediaType.APPLICATION_JSON)
    public List<DeletionInfo> getDeletedLists(@Auth User user) {

        List<DeletionInfo> result = new ArrayList<>();

        for(ShoppingListServer s : shoppingListDAO.getDeletedLists(user)) {
            result.add(new DeletionInfo(s.getId(), s.getVersion()));
        }

        for(ShoppingListServer s : sharedShoppingListDAO.getDeletedLists(user)) {
            result.add(new DeletionInfo(s.getId(), s.getVersion()));
        }

        return result;
    }

    @Override
    @GET
    @UnitOfWork
    @Path("{listId}/{itemId}")
    @Produces(MediaType.APPLICATION_JSON)
    public Item getListItem(@Auth User user,
                            @PathParam("listId") int listId,
                            @PathParam("itemId") int itemId) {

        try {
            return shoppingListDAO.getListItem(user, listId, itemId);
        } catch (ListNotFoundException | ItemNotFoundException e) {
            try {
                return sharedShoppingListDAO.getListItem(user, listId, itemId);
            } catch (ListNotFoundException | ItemNotFoundException e2) {
                throw new WebApplicationException(Response.Status.NOT_FOUND);
            }
        }


    }

    @Override
    @GET
    @UnitOfWork
    @Path("{listId}/sharingCode")
    @Produces(MediaType.TEXT_PLAIN)
    public String getSharingCode(@Auth User user, @PathParam("listId") int listId) {
        ShoppingListServer list;

        try {
            list = shoppingListDAO.getListById(user, listId);
        } catch (ListNotFoundException e) {
            throw new WebApplicationException(Response.Status.NOT_FOUND);
        }

        return list.getSharingCode();
    }

    @Override
    @POST
    @UnitOfWork
    @Path("share/{sharingCode}")
    @Produces(MediaType.TEXT_PLAIN)
    public String share(@Auth User user, @PathParam("sharingCode") String sharingCode) {
        ShoppingListServer list;

        try {
            list = shoppingListDAO.getListBySharingCode(sharingCode);
        } catch (ListNotFoundException e) {
            System.out.println(e.getMessage());
            throw new WebApplicationException(Response.Status.NOT_FOUND);
        }

        list.shareWith(user);

        return list.getName();
    }

    @Override
    @GET
    @UnitOfWork
    @Path("/sharedLists")
    @Produces(MediaType.APPLICATION_JSON)
    public List<ShoppingListServer> getSharedLists(@Auth User user) {
        return sharedShoppingListDAO.getLists(user);
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

        if(newItem.getServerId() != 0) {
            throw new WebApplicationException(Response.Status.BAD_REQUEST);
        }

        try {
            return shoppingListDAO.addListItem(user, listId, newItem);
        } catch (ListNotFoundException e) {
            try {
                return sharedShoppingListDAO.addListItem(user, listId, newItem);
            } catch (ListNotFoundException e2) {
                throw new WebApplicationException(Response.Status.NOT_FOUND);
            }
        }


    }


    @Override
    @POST
    @UnitOfWork
    @Path("{listId}/{itemId}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Item updateItem(@Auth User user,
                           @PathParam("listId") @ApiParam(value = "id of the list the item belongs to", required = true) int listId,
                           @PathParam("itemId") @ApiParam(value = "id of the Item to update", required = true) int itemId,
                           @ApiParam(value = "new details of the specified item", required = true) Item item) {

        if(item.getServerId() != itemId) {
            throw new WebApplicationException(Response.Status.BAD_REQUEST);
        }

        try {
            return shoppingListDAO.updateListItem(user, listId, item);
        } catch (ListNotFoundException | ItemNotFoundException e) {
            try {
                return sharedShoppingListDAO.updateListItem(user, listId, item);
            } catch (ListNotFoundException | ItemNotFoundException e2) {
                throw new WebApplicationException(Response.Status.NOT_FOUND);
            }
        }

    }

    @Override
    @DELETE
    @UnitOfWork
    @Path("{listId}/{itemId}")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Produces(MediaType.APPLICATION_JSON)
    public void deleteListItem(@Auth User user,
                               @PathParam("listId") @ApiParam(value = "id of the list the item belongs to", required = true) int listId,
                               @PathParam("itemId") @ApiParam(value = "id of the Item to update", required = true) int itemId) {

        try {
            shoppingListDAO.deleteListItem(user, listId, itemId);
        } catch (ListNotFoundException e) {
            try {
                sharedShoppingListDAO.deleteListItem(user, listId, itemId);
            } catch (ListNotFoundException e2) {
                throw new WebApplicationException(Response.Status.NOT_FOUND);
            }
        }

    }

    @GET
    @UnitOfWork
    @Path("{listId}/items")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Produces(MediaType.APPLICATION_JSON)
    public List<Item> getListItems(@Auth User user, @PathParam("listId") int listId) {

        try {
            return shoppingListDAO.getListItems(user, listId);
        } catch (ListNotFoundException ex) {

            try {
                return sharedShoppingListDAO.getListItems(user, listId);
            } catch (ListNotFoundException e2) {
                throw new WebApplicationException(Response.Status.NOT_FOUND);
            }
        }
    }


    @GET
    @UnitOfWork
    @Path("{listId}/items/deleted")
    @Produces(MediaType.APPLICATION_JSON)
    public List<DeletionInfo> getDeletedListItems(@Auth User user, @PathParam("listId") int listId) {

        List<Item> deletedItems;
        try {
            deletedItems = shoppingListDAO.getDeletedListItems(user, listId);
        } catch (ListNotFoundException ex) {

            try {
                deletedItems = sharedShoppingListDAO.getDeletedListItems(user, listId);
            } catch (ListNotFoundException e2) {
                throw new WebApplicationException(Response.Status.NOT_FOUND);
            }
        }

        List<DeletionInfo> result = new LinkedList<>();
        for(Item i : deletedItems) {
            result.add(new DeletionInfo(i.getServerId(), i.getVersion()));
        }

        return result;

    }
}
