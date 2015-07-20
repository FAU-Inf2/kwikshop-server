package de.fau.cs.mad.kwikshop.server.dao;

import de.fau.cs.mad.kwikshop.common.Item;
import de.fau.cs.mad.kwikshop.common.ShoppingListServer;
import de.fau.cs.mad.kwikshop.common.util.NamedQueryConstants;
import de.fau.cs.mad.kwikshop.common.User;
import de.fau.cs.mad.kwikshop.server.exceptions.ItemNotFoundException;
import de.fau.cs.mad.kwikshop.server.exceptions.ListNotFoundException;
import io.dropwizard.hibernate.AbstractDAO;
import org.hibernate.Query;
import org.hibernate.SessionFactory;

import java.util.List;


public class ShoppingListDAO extends AbstractDAO<ShoppingListServer> implements ListDAO<ShoppingListServer> {


    /**
     * Creates a new DAO with a given session provider.
     *
     * @param sessionFactory a session provider
     */
    public ShoppingListDAO(SessionFactory sessionFactory) {
        super(sessionFactory);
    }


    @Override
    public ShoppingListServer createList(User user, ShoppingListServer shoppingList) {

        shoppingList.setOwner(user);
        shoppingList = persist(shoppingList);

        return shoppingList;
    }

    @Override
    public ShoppingListServer updateList(User user, ShoppingListServer shoppingList, boolean updateItems) throws ListNotFoundException{


        ShoppingListServer existingList = getListById(user, shoppingList.getId());

        existingList.setName(shoppingList.getName());
        existingList.setSortTypeInt(shoppingList.getSortTypeInt());
        existingList.setLocation(shoppingList.getLocation());
        existingList.setLastModifiedDate(shoppingList.getLastModifiedDate());

        if(updateItems) {

            //remove all existing items
            for(Item i : existingList.getItems()) {
                existingList.removeItem(i.getId());
            }

            //add items from updated value
            for(Item i : shoppingList.getItems()) {
                existingList.addItem(i);
            }
        }


        existingList = persist(existingList);
        return existingList;


    }

    @Override
    public boolean deleteList(User user, int listId) {

        ShoppingListServer list;
        try {
            list = getListById(user, listId);
        } catch (ListNotFoundException e) {
            return false;
        }

        currentSession().delete(list);
        return true;


    }

    @Override
    public List<ShoppingListServer> getLists(User user) {

        Query query = namedQuery(NamedQueryConstants.SHOPPINGLIST_GET_ALL_FOR_USER)
                .setParameter(NamedQueryConstants.USER_ID, user.getId());
        return list(query);
    }

    @Override
    public ShoppingListServer getListById(User user, int listId) throws ListNotFoundException {

        Query query = namedQuery(NamedQueryConstants.SHOPPINGLIST_GET_BY_LISTID)
                .setParameter(NamedQueryConstants.USER_ID, user.getId())
                .setParameter(NamedQueryConstants.LIST_ID, listId);

        List<ShoppingListServer> result = list(query);

        if(result.size() != 1) {
            throw new ListNotFoundException(String.format("ShoppingList with id %s for user %s not found", listId, user.getId()));
        }

        return result.get(0);
    }

    @Override
    public Item getListItem(User user, int listId, int itemId) throws ListNotFoundException, ItemNotFoundException {

        ShoppingListServer list = getListById(user, listId);

        Item item = list.getItem(itemId);
        if(item == null) {
            throw new ItemNotFoundException(String.format("Item %s not found in shopping list %s for user %s", itemId, listId, user.getId()));
        }

        return item;
    }

    @Override
    public boolean deleteListItem(User user, int listId, int itemId) throws ListNotFoundException {

        ShoppingListServer list = getListById(user, listId);

        boolean success = list.removeItem(itemId);
        persist(list);
        return success;
    }

    @Override
    public Item addListItem(User user, int listId, Item item) throws ListNotFoundException {

        ShoppingListServer list = getListById(user, listId);

        currentSession().persist(item);
        list.addItem(item);
        persist(list);

        return item;

    }

    @Override
    public Item updateListItem(User user, int listId, Item updatedItem) throws ListNotFoundException, ItemNotFoundException {


        Item existingItem = getListItem(user, listId, updatedItem.getId());

        existingItem.setOrder(updatedItem.getOrder());
        existingItem.setBought(updatedItem.isBought());
        existingItem.setName(updatedItem.getName());
        existingItem.setAmount(updatedItem.getAmount());
        existingItem.setHighlight(updatedItem.isHighlight());
        existingItem.setBrand(updatedItem.getBrand());
        existingItem.setComment(updatedItem.getComment());
        existingItem.setGroup(updatedItem.getGroup());
        existingItem.setUnit(updatedItem.getUnit());
        existingItem.setLastBought(updatedItem.getLastBought());
        existingItem.setRegularlyRepeatItem(updatedItem.isRegularlyRepeatItem());
        existingItem.setPeriodType(updatedItem.getPeriodType());
        existingItem.setSelectedRepeatTime(updatedItem.getSelectedRepeatTime());
        existingItem.setRemindFromNextPurchaseOn(updatedItem.isRemindFromNextPurchaseOn());
        existingItem.setRemindAtDate(updatedItem.getRemindAtDate());
        existingItem.setLocation(updatedItem.getLocation());

        currentSession().persist(existingItem);

        return existingItem;


    }

}
