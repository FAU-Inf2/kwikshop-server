package de.fau.cs.mad.kwikshop.server.dao;

import de.fau.cs.mad.kwikshop.common.Item;
import de.fau.cs.mad.kwikshop.common.util.NamedQueryConstants;
import de.fau.cs.mad.kwikshop.common.ShoppingList;
import de.fau.cs.mad.kwikshop.common.User;
import de.fau.cs.mad.kwikshop.server.dao.ListDAO;
import io.dropwizard.hibernate.AbstractDAO;
import org.hibernate.Query;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;

import java.util.List;


public class ShoppingListDAO extends AbstractDAO<ShoppingList> implements ListDAO<ShoppingList> {


    private final SessionFactory sessionFactory;

    /**
     * Creates a new DAO with a given session provider.
     *
     * @param sessionFactory a session provider
     */
    public ShoppingListDAO(SessionFactory sessionFactory) {
        super(sessionFactory);
        this.sessionFactory = sessionFactory;
    }


    @Override
    public ShoppingList createList(User user, ShoppingList shoppingList) {

        shoppingList.setOwner(user);

        Transaction transaction = currentSession().beginTransaction();
        shoppingList = persist(shoppingList);
        transaction.commit();

        return shoppingList;
    }

    @Override
    public ShoppingList updateOrCreateList(User user, ShoppingList shoppingList, boolean updateItems) {

        ShoppingList existingList = getListById(user, shoppingList.getId());

        if(existingList == null) {

            return createList(user, shoppingList);

        } else {

            Transaction transaction = currentSession().beginTransaction();

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
            transaction.commit();

            return existingList;
        }


    }

    @Override
    public boolean deleteList(User user, int listId) {

        ShoppingList list = getListById(user, listId);

        if(list == null) {
            return false;
        } else {

            Transaction transaction = currentSession().beginTransaction();;
            currentSession().delete(list);
            transaction.commit();

            return false;
        }

    }

    @Override
    public List<ShoppingList> getLists(User user) {

        Query query = namedQuery(NamedQueryConstants.SHOPPINGLIST_GET_ALL_FOR_USER)
                .setParameter(NamedQueryConstants.USER_ID, user.getId());
        List<ShoppingList> lists = list(query);
        return lists;
    }

    @Override
    public ShoppingList getListById(User user, int listId) {

        Query query = namedQuery(NamedQueryConstants.SHOPPINGLIST_GET_BY_LISTID)
                .setParameter(NamedQueryConstants.USER_ID, user.getId())
                .setParameter(NamedQueryConstants.LIST_ID, listId);

        List<ShoppingList> result = list(query);

        return result.isEmpty()
                ? null
                : result.get(0);
    }

    @Override
    public Item getListItem(User user, int listId, int itemId) {

        ShoppingList list = getListById(user, listId);
        if(list == null) {
            return null;
        } else {
            return list.getItem(itemId);
        }
    }

    @Override
    public boolean deleteListItem(User user, int listId, int itemId) {

        ShoppingList list = getListById(user, listId);
        if(list == null) {

            return false;

        } else {

            boolean success = list.removeItem(itemId);

            Transaction transaction = currentSession().beginTransaction();
            persist(list);
            transaction.commit();

            return success;
        }

    }

    @Override
    public Item addListItem(User user, int listId, Item item) {

        ShoppingList list = getListById(user, listId);
        if(list == null) {

            return null;

        } else {

            Transaction transaction = currentSession().beginTransaction();

            currentSession().persist(item);
            list.addItem(item);
            list = persist(list);

            transaction.commit();

            return item;
        }


    }

    @Override
    public Item updateOrCreateListItem(User user, int listId, Item updatedItem) {

        Item existingItem = getListItem(user, listId, updatedItem.getId());

        if(existingItem == null) {
            return addListItem(user, listId, updatedItem);
        } else {

            Transaction transaction = currentSession().beginTransaction();

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
            transaction.commit();

            return existingItem;

        }



    }
}
