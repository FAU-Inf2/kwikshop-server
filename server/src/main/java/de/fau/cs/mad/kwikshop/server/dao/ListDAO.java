package de.fau.cs.mad.kwikshop.server.dao;

import de.fau.cs.mad.kwikshop.common.Item;
import de.fau.cs.mad.kwikshop.common.User;
import de.fau.cs.mad.kwikshop.server.exceptions.ItemNotFoundException;
import de.fau.cs.mad.kwikshop.server.exceptions.ListNotFoundException;

import java.util.List;

public interface ListDAO<TList>  {

    /**
     * Creates a new list for the user from the supplied list data
     * @param user The user to create the list for
     * @param list The list to create on the server
     * @return Returns the list actually created on the server
     */
    TList createList(User user, TList list);

    /**
     * Creates the specified list on the server or updates it if a list with the same id already exists
     * @param user The owner of the list
     * @param list The list to update or create on the server
     * @param updateItems Specifies whether to also overwrite the items of the list on the server with the items from the
     *                    specified list
     * @return Returns the updated or created list
     */
    TList updateOrCreateList(User user, TList list, boolean updateItems);

    /**
     * Deletes the specified list on the server
     * @param user The owner of the list
     * @param listId The id of the list to delete
     * @return Returns true if the list has been deleted successfully, otherwise false
     */
    boolean deleteList(User user, int listId);

    /**
     * Gets all of the user's lists
     * @param user The user for which to get lists for
     * @return Returns all list owned by the specified user
     */
    List<TList> getLists(User user);

    /**
     * Gets the specified list for which must be owned by the specified user
     * @param user The user who's lists to search
     * @param listId The id of the list to retrieve
     * @return Retunrs the list as it's stored on the server
     * @throws ListNotFoundException Thrown if the list with the specified id could not be found
     */
    TList getListById(User user, int listId) throws ListNotFoundException;

    /**
     * Gets an item from one of the users lists
     * @param user The owner of the list/item
     * @param listId The id of the list to get the item from
     * @param itemId The id of the item to retrieve
     * @return Returns the item retrieved from the database
     * @throws ListNotFoundException Thrown if the specified list could not be found
     * @throws ItemNotFoundException Thrown if the list was found but does not contain the specified item
     */
    Item getListItem(User user, int listId, int itemId) throws ListNotFoundException, ItemNotFoundException;

    /**
     * Adds the specified item to a list
     * @param user The owner of the list
     * @param listId The id of the list to add the item to
     * @param item The item to add to the list
     * @return Returns the Item as it's stored in the database
     * @throws ListNotFoundException Thrown if the specified list could not be found
     */
    Item addListItem(User user, int listId, Item item) throws ListNotFoundException;

    /**
     * Creates the specified item on the server or updates it if a list with the same id already exists
     * @param user The owner of the list/item
     * @param listId The id of the list to create or update the item in
     * @param itemToUpdate The updated data of the item
     * @return Returns the updated or created item
     * @throws ListNotFoundException Thrown if the specified list could not be found
     */
    Item updateOrCreateListItem(User user, int listId, Item itemToUpdate) throws ListNotFoundException;

    /**
     * Deletes the specified Item from the specified list
     * @param user The owner of the list to delete an item from
     * @param listId The id of the list to delete the item from
     * @param itemId The id of the item to delete
     * @return Returns true if the item has been deleted successfully, otherwise false
     * @throws ListNotFoundException Thrown if the specified list could not be found
     */
    boolean deleteListItem(User user, int listId, int itemId) throws ListNotFoundException;



}
