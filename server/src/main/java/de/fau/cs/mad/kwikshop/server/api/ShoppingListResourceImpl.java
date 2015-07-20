package de.fau.cs.mad.kwikshop.server.api;

import com.wordnik.swagger.annotations.ApiParam;
import de.fau.cs.mad.kwikshop.common.Item;
import de.fau.cs.mad.kwikshop.common.ShoppingListServer;
import de.fau.cs.mad.kwikshop.common.User;
import de.fau.cs.mad.kwikshop.server.dao.ListDAO;
import de.fau.cs.mad.kwikshop.server.exceptions.ItemNotFoundException;
import de.fau.cs.mad.kwikshop.server.exceptions.ListNotFoundException;
import io.dropwizard.auth.Auth;


import javax.ws.rs.PathParam;
import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import java.util.List;

public class ShoppingListResourceImpl implements ShoppingListResource {


    private final ListDAO<ShoppingListServer> shoppingListDAO;


    public ShoppingListResourceImpl(ListDAO<ShoppingListServer> shoppingListDAO) {

        if(shoppingListDAO == null) {
            throw new IllegalArgumentException("'shoppingListDAO' must not be null");
        }

        this.shoppingListDAO = shoppingListDAO;
    }



    @Override
    public List<ShoppingListServer> getList(@Auth User user) {
        return shoppingListDAO.getLists(user);
    }

    @Override
    public ShoppingListServer getList(@Auth User user, @PathParam("listId") int listId) {

        try {
            return shoppingListDAO.getListById(user, listId);
        } catch (ListNotFoundException e) {
            throw new WebApplicationException(Response.Status.BAD_REQUEST);
        }

    }

    @Override
    public ShoppingListServer createList(@Auth User user, ShoppingListServer shoppingList) {
        return shoppingListDAO.createList(user, shoppingList);
    }

    @Override
    public ShoppingListServer updateList(@Auth User user,
                                   @PathParam("listId") int listId, ShoppingListServer shoppingList,
                                   @QueryParam("updateItems") boolean updateItems) {

        if(shoppingList.getId() != listId) {
            throw new WebApplicationException(Response.Status.BAD_REQUEST);
        }

        try {
            return shoppingListDAO.getListById(user, listId);
        } catch (ListNotFoundException e) {
            throw new WebApplicationException(Response.Status.NOT_FOUND);
        }

    }

    @Override
    public void deleteList(@Auth User user, @PathParam("listId") int listId) {
        boolean listFound = shoppingListDAO.deleteList(user, listId);
        if(!listFound) {
            throw new WebApplicationException(Response.Status.NOT_FOUND);
        }
    }

    @Override
    public Item getListItem(@Auth User user,
                            @PathParam("listId") int listId,
                            @PathParam("itemId") int itemId) {

        try {

            return shoppingListDAO.getListItem(user, listId, itemId);

        } catch (ListNotFoundException e) {

            throw new WebApplicationException(Response.Status.NOT_FOUND);

        } catch (ItemNotFoundException e) {

            throw new WebApplicationException(Response.Status.NOT_FOUND);
        }


    }

    @Override
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
    public Item updateItem(@Auth User user,
                           @ApiParam(value = "id of the list the item belongs to", required = true) @PathParam("listId") int listId,
                           @ApiParam(value = "id of the Item to update", required = true) @PathParam("itemId") int itemId,
                           @ApiParam(value = "new details of the specified item", required = true) Item item) {

        if(item.getId() != itemId) {
            throw new WebApplicationException(Response.Status.BAD_REQUEST);
        }

        try {

            return shoppingListDAO.updateOrCreateListItem(user, listId, item);

        } catch (ListNotFoundException e) {

            throw new WebApplicationException(Response.Status.NOT_FOUND);
        }

    }

    @Override
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
