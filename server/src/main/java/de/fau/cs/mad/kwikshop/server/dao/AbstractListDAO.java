package de.fau.cs.mad.kwikshop.server.dao;

import de.fau.cs.mad.kwikshop.common.Item;
import de.fau.cs.mad.kwikshop.common.User;
import de.fau.cs.mad.kwikshop.common.interfaces.DomainListObjectServer;
import de.fau.cs.mad.kwikshop.server.exceptions.ItemNotFoundException;
import de.fau.cs.mad.kwikshop.server.exceptions.ListNotFoundException;
import io.dropwizard.hibernate.AbstractDAO;
import org.hibernate.SessionFactory;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


public abstract class AbstractListDAO<TList extends DomainListObjectServer> extends AbstractDAO<TList> implements ListDAO<TList> {


    /**
     * Creates a new DAO with a given session provider.
     *
     * @param sessionFactory a session provider
     */
    public AbstractListDAO(SessionFactory sessionFactory) {
        super(sessionFactory);
    }


    @Override
    public TList createList(User user, TList list) {

        //make sure owner is set and is actually the current user
        list.setOwnerId(user.getId());

        return persist(list);
    }

    @Override
    public abstract TList updateList(User user, TList tList) throws ListNotFoundException;

    @Override
    public boolean deleteList(User user, int listId) {

        TList list;
        try {
            list = getListById(user, listId);
        } catch (ListNotFoundException e) {
            return false;
        }

        list.setDeleted(true);
        persist(list);

        return true;
    }

    @Override
    public abstract List<TList> getLists(User user);

    @Override
    public List<Item> getListItems(User user, int listId) throws ListNotFoundException {

        TList list = getListById(user, listId);

        List<Item> result = new ArrayList<>();
        for(Item i : list.getItems()) {

            if( ! i.getDeleted()){
                result.add(i);
            }
        }

        return result;
    }

    @Override
    public abstract List<TList> getDeletedLists(User user);

    @Override
    public abstract TList getListById(User user, int listId) throws ListNotFoundException;

    @Override
    public Item getListItem(User user, int listId, int itemId) throws ListNotFoundException, ItemNotFoundException {

        TList list = getListById(user, listId);

        Item item = list.getItem(itemId);
        if(item == null || item.getDeleted()) {
            throw new ItemNotFoundException(String.format("Item %s not found in list %s of type %s for user %s", itemId, list.getClass().getSimpleName(), listId, user.getId()));
        }

        return item;
    }

    @Override
    public Item addListItem(User user, int listId, Item item) throws ListNotFoundException {

        TList list = getListById(user, listId);

        currentSession().persist(item);
        list.addItem(item);
        persist(list);

        return item;
    }

    @Override
    public Item updateListItem(User user, int listId, Item updatedItem) throws ListNotFoundException, ItemNotFoundException {

        Item existingItem = getListItem(user, listId, updatedItem.getServerId());

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
        existingItem.setRepeatType(updatedItem.getRepeatType());
        existingItem.setPeriodType(updatedItem.getPeriodType());
        existingItem.setSelectedRepeatTime(updatedItem.getSelectedRepeatTime());
        existingItem.setRemindFromNextPurchaseOn(updatedItem.isRemindFromNextPurchaseOn());
        existingItem.setRemindAtDate(updatedItem.getRemindAtDate());
        existingItem.setLocation(updatedItem.getLocation());

        currentSession().persist(existingItem);

        return existingItem;
    }

    @Override
    public boolean deleteListItem(User user, int listId, int itemId) throws ListNotFoundException {

        TList list = getListById(user, listId);

        Item item;
        try {
            item = getListItem(user, listId, itemId);
        } catch (ItemNotFoundException e) {
            return false;
        }

        item.setDeleted(true);
        currentSession().persist(item);

        return true;
    }

    @Override
    public List<Item> getDeletedListItems(User user, int listId) throws ListNotFoundException {

        TList list = getListById(user, listId);

        List<Item> result = new ArrayList<>();
        for(Item i : list.getItems()) {

            if(i.getDeleted()){
                result.add(i);
            }
        }

        return result;

    }
}
