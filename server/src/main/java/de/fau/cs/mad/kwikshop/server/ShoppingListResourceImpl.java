package de.fau.cs.mad.kwikshop.server;

import com.wordnik.swagger.annotations.ApiParam;
import de.fau.cs.mad.kwikshop.common.Item;
import de.fau.cs.mad.kwikshop.common.ShoppingList;
import de.fau.cs.mad.kwikshop.common.rest.ShoppingListResource;


import javax.ws.rs.PathParam;
import java.util.Arrays;
import java.util.List;

public class ShoppingListResourceImpl implements ShoppingListResource {


    private final static ShoppingList dummyList;

    static {
        dummyList = new ShoppingList();
        dummyList.setName("Dummy List");
    }


    @Override
    public List<ShoppingList> getList() {
        return null;
    }

    @Override
    public ShoppingList getList(@PathParam("listId") int id) {
        return null;
    }

    @Override
    public List<Item> getListItem(@PathParam("listId") int listId,
                                  @PathParam("itemId") int itemId) {
        return null;
    }

    @Override
    public Item createItem(@ApiParam(value = "Item to create", required = true) Item newItem) {
        return null;
    }

    @Override
    public Item updateItem(@ApiParam(value = "id of the list the item belongs to", required = true) @PathParam("listId") long listId,
                           @ApiParam(value = "id of the Item to update", required = true) @PathParam("itemId") long itemId,
                           @ApiParam(value = "new details of the specified item", required = true) Item item) {
        return null;
    }

    @Override
    public void deleteUser(@ApiParam(value = "id of the list the item belongs to", required = true) @PathParam("listId") long listId,
                           @ApiParam(value = "id of the Item to update", required = true) @PathParam("itemId") long itemId) {

    }
}
