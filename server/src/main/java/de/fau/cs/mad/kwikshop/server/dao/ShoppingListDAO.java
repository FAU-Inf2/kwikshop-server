package de.fau.cs.mad.kwikshop.server.dao;

import de.fau.cs.mad.kwikshop.common.Item;
import de.fau.cs.mad.kwikshop.common.ShoppingListServer;
import de.fau.cs.mad.kwikshop.common.util.NamedQueryConstants;
import de.fau.cs.mad.kwikshop.common.User;
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
    public ShoppingListServer updateOrCreateList(User user, ShoppingListServer shoppingList, boolean updateItems) {

        ShoppingListServer existingList = getListById(user, shoppingList.getId());

        if(existingList == null) {

            return createList(user, shoppingList);

        } else {


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


    }

    @Override
    public boolean deleteList(User user, int listId) {

        ShoppingListServer list = getListById(user, listId);

        if(list == null) {
            return false;
        } else {

            currentSession().delete(list);
            return true;
        }

    }

    @Override
    public List<ShoppingListServer> getLists(User user) {

        Query query = namedQuery(NamedQueryConstants.SHOPPINGLIST_GET_ALL_FOR_USER)
                .setParameter(NamedQueryConstants.USER_ID, user.getId());
        return list(query);
    }

    @Override
    public ShoppingListServer getListById(User user, int listId) {

        Query query = namedQuery(NamedQueryConstants.SHOPPINGLIST_GET_BY_LISTID)
                .setParameter(NamedQueryConstants.USER_ID, user.getId())
                .setParameter(NamedQueryConstants.LIST_ID, listId);

        List<ShoppingListServer> result = list(query);

        return result.isEmpty()
                ? null
                : result.get(0);
    }

    @Override
    public Item getListItem(User user, int listId, int itemId) {

        ShoppingListServer list = getListById(user, listId);
        if(list == null) {
            return null;
        } else {
            return list.getItem(itemId);
        }
    }

    @Override
    public boolean deleteListItem(User user, int listId, int itemId) {

        ShoppingListServer list = getListById(user, listId);
        if(list == null) {

            return false;

        } else {

            boolean success = list.removeItem(itemId);
            persist(list);
            return success;
        }

    }

    @Override
    public Item addListItem(User user, int listId, Item item) {

        ShoppingListServer list = getListById(user, listId);
        if(list == null) {

            return null;

        } else {

            currentSession().persist(item);
            list.addItem(item);
            persist(list);

            return item;
        }


    }

    @Override
    public Item updateOrCreateListItem(User user, int listId, Item updatedItem) {

        Item existingItem = getListItem(user, listId, updatedItem.getId());

        if(existingItem == null) {
            return addListItem(user, listId, updatedItem);
        } else {

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
}
