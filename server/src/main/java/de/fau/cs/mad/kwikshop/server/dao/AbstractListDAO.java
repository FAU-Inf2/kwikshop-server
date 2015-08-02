package de.fau.cs.mad.kwikshop.server.dao;

import de.fau.cs.mad.kwikshop.common.*;
import de.fau.cs.mad.kwikshop.common.interfaces.DomainListObjectServer;
import de.fau.cs.mad.kwikshop.server.exceptions.ItemNotFoundException;
import de.fau.cs.mad.kwikshop.server.exceptions.ListNotFoundException;
import io.dropwizard.hibernate.AbstractDAO;
import org.hibernate.SessionFactory;

import java.util.ArrayList;
import java.util.Date;
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
        list.setVersion(1);

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

        item.setVersion(1);

        currentSession().persist(item);
        list.addItem(item);
        list.setVersion(list.getVersion() + 1);

        persist(list);

        return item;
    }

    @Override
    public Item updateListItem(User user, int listId, Item updatedItem) throws ListNotFoundException, ItemNotFoundException {

        TList list = getListById(user, listId);

        Item existingItem = getListItem(user, listId, updatedItem.getServerId());

        if(!itemEquals(existingItem, updatedItem)) {

            existingItem.setVersion(existingItem.getVersion() + 1);
            list.setVersion(list.getVersion() + 1);

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
        }


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

        list.setVersion(list.getVersion() + 1);

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


    protected boolean itemEquals(Item item1, Item item2) {

         return item1.getOrder() == item2.getOrder() &&
                item1.isBought() == item2.isBought() &&
                stringEquals(item1.getName(), item2.getName()) &&
                item1.getAmount() == item2.getAmount() &&
                item1.isHighlight() == item2.isHighlight() &&
                stringEquals(item1.getBrand(), item2.getBrand()) &&
                stringEquals(item1.getComment(), item2.getComment()) &&
                groupEquals(item1.getGroup(), item2.getGroup()) &&
                unitEquals(item1.getUnit(), item2.getUnit()) &&
                dateEquals(item1.getLastBought(), item2.getLastBought()) &&
                item1.getRepeatType() == item2.getRepeatType() &&
                item1.getPeriodType() == item2.getPeriodType() &&
                item1.getSelectedRepeatTime() == item2.getSelectedRepeatTime() &&
                item1.isRemindFromNextPurchaseOn() == item2.isRemindFromNextPurchaseOn() &&
                dateEquals(item1.getRemindAtDate(), item2.getRemindAtDate()) &&
                locationEquals(item1.getLocation(), item2.getLocation());
    }

    /**
     * String comparer function that can handle null value
     */
    protected boolean stringEquals(String string1, String string2){

        if(string1 == null && string2 == null) {
            return true;
        }

        if(string1 == null || string2 == null) {
            return false;
        }

        return  string1.equals(string2);
    }

    protected boolean groupEquals(Group group1, Group group2) {

        if(group1 == group2) {
            return true;
        }

        if(group1 == null || group2 == null) {
            return false;
        }

        return group1.getServerId() == group2.getServerId();
    }

    protected boolean unitEquals(Unit unit1, Unit unit2) {

        if(unit1 == unit2) {
            return true;
        }

        if(unit1 == null || unit2 == null) {
            return false;
        }

        return unit1.getServerId() == unit2.getServerId();

    }

    protected boolean dateEquals(Date date1, Date date2) {

        if(date1 == date2) {
            return true;
        }

        if(date1 == null || date2 == null) {
            return false;
        }

        return date1.equals(date2);
    }


    protected boolean locationEquals(LastLocation location1, LastLocation location2) {

        if(location1 == location2) {
            return true;
        }

        if(location1 == null || location2 == null) {
            return false;
        }

        return  location1.getServerId() == location2.getServerId();
    }

}
