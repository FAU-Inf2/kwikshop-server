package de.fau.cs.mad.kwikshop.server.api;

import com.wordnik.swagger.annotations.ApiParam;
import de.fau.cs.mad.kwikshop.common.Item;
import de.fau.cs.mad.kwikshop.common.ShoppingList;
import de.fau.cs.mad.kwikshop.common.User;
import de.fau.cs.mad.kwikshop.common.rest.ShoppingListResource;
import de.fau.cs.mad.kwikshop.server.dao.ListDAO;
import io.dropwizard.auth.Auth;


import javax.ws.rs.PathParam;
import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import java.util.List;

public class ShoppingListResourceImpl implements ShoppingListResource {


    private final ListDAO<ShoppingList> shoppingListDAO;


    public ShoppingListResourceImpl(ListDAO<ShoppingList> shoppingListDAO) {

        if(shoppingListDAO == null) {
            throw new IllegalArgumentException("'shoppingListDAO' must not be null");
        }

        this.shoppingListDAO = shoppingListDAO;
    }



    @Override
    public List<ShoppingList> getList(@Auth User user) {
        return shoppingListDAO.getLists(user);
    }

    @Override
    public ShoppingList getList(@Auth User user, @PathParam("listId") int listId) {
        return shoppingListDAO.getListById(user, listId);
    }

    @Override
    public ShoppingList createList(@Auth User user, ShoppingList shoppingList) {
        return shoppingListDAO.createList(user, shoppingList);
    }

    @Override
    public ShoppingList updateList(@Auth User user,
                                   @PathParam("listId") int listId,
                                   ShoppingList shoppingList,
                                   @QueryParam("updateItems") boolean updateItems) {

        if(shoppingList.getId() != listId) {
            throw new WebApplicationException(Response.Status.BAD_REQUEST);
        }

        return shoppingListDAO.updateOrCreateList(user, shoppingList, updateItems);
    }

    @Override
    public void deleteList(@Auth User user, @PathParam("listId") int listId) {
        shoppingListDAO.deleteList(user, listId);
    }

    @Override
    public Item getListItem(@Auth User user,
                            @PathParam("listId") int listId,
                            @PathParam("itemId") int itemId) {

        return shoppingListDAO.getListItem(user, listId, itemId);
    }

    @Override
    public Item createItem(@Auth User user,
                           @PathParam("listId") int listId,
                           @ApiParam(value = "Item to create", required = true) Item newItem) {

        return shoppingListDAO.addListItem(user, listId, newItem);
    }

    @Override
    public Item updateItem(@Auth User user,
                           @ApiParam(value = "id of the list the item belongs to", required = true) @PathParam("listId") int listId,
                           @ApiParam(value = "id of the Item to update", required = true) @PathParam("itemId") int itemId,
                           @ApiParam(value = "new details of the specified item", required = true) Item item) {

        if(item.getId() != itemId) {
            throw new WebApplicationException(Response.Status.BAD_REQUEST);
        }

        return shoppingListDAO.updateOrCreateListItem(user, listId, item);

    }

    @Override
    public void deleteListItem(@Auth User user,
                               @ApiParam(value = "id of the list the item belongs to", required = true) @PathParam("listId") int listId,
                               @ApiParam(value = "id of the Item to update", required = true) @PathParam("itemId") int itemId) {

        shoppingListDAO.deleteListItem(user, listId, itemId);

    }
}
