package de.fau.cs.mad.kwikshop.server.dao;

import de.fau.cs.mad.kwikshop.common.Item;
import de.fau.cs.mad.kwikshop.common.User;
import de.fau.cs.mad.kwikshop.common.interfaces.DomainListObjectServer;
import de.fau.cs.mad.kwikshop.server.exceptions.ItemNotFoundException;
import de.fau.cs.mad.kwikshop.server.exceptions.ListNotFoundException;
import io.dropwizard.hibernate.AbstractDAO;
import org.hibernate.SessionFactory;

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
        list.setOwnerId(user.getId());
        list = persist(list);

        return list;
    }

    @Override
    public abstract TList updateList(User user, TList tList, boolean updateItems) throws ListNotFoundException;

    @Override
    public boolean deleteList(User user, int listId) {

        TList list;
        try {
            list = getListById(user, listId);
        } catch (ListNotFoundException e) {
            return false;
        }

        currentSession().delete(list);
        return true;
    }

    @Override
    public abstract List<TList> getLists(User user);

    @Override
    public abstract TList getListById(User user, int listId) throws ListNotFoundException;

    @Override
    public Item getListItem(User user, int listId, int itemId) throws ListNotFoundException, ItemNotFoundException {

        TList list = getListById(user, listId);

        Item item = list.getItem(itemId);
        if(item == null) {
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

    @Override
    public boolean deleteListItem(User user, int listId, int itemId) throws ListNotFoundException {

        TList list = getListById(user, listId);

        boolean success = list.removeItem(itemId);
        persist(list);
        return success;
    }
}