package de.fau.cs.mad.kwikshop.server.dao;

import de.fau.cs.mad.kwikshop.common.*;
import de.fau.cs.mad.kwikshop.common.interfaces.DomainListObjectServer;
import de.fau.cs.mad.kwikshop.common.util.EqualityComparer;
import de.fau.cs.mad.kwikshop.server.ServerEqualityComparer;
import de.fau.cs.mad.kwikshop.server.exceptions.ItemNotFoundException;
import de.fau.cs.mad.kwikshop.server.exceptions.ListNotFoundException;
import io.dropwizard.hibernate.AbstractDAO;
import org.hibernate.SessionFactory;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;


public abstract class AbstractListDAO<TList extends DomainListObjectServer> extends AbstractDAO<TList> implements ListDAO<TList> {

    protected final EqualityComparer comparer = new ServerEqualityComparer();

    private final UnitDAO unitDAO;
    private final GroupDAO groupDAO;
    private final LocationDAO locationDAO;

    /**
     * Creates a new DAO with a given session provider.
     *
     * @param sessionFactory a session provider
     */
    public AbstractListDAO(SessionFactory sessionFactory, UnitDAO unitDAO, GroupDAO groupDAO, LocationDAO locationDAO) {
        super(sessionFactory);

        if(unitDAO == null) {
            throw new IllegalArgumentException("'unitDAO' must not be null");
        }

        if(groupDAO == null) {
            throw new IllegalArgumentException("'groupDAO' must not be null");
        }

        if(locationDAO == null){
            throw new IllegalArgumentException("'locationDAO' must not be null");
        }

        this.unitDAO = unitDAO;
        this.groupDAO = groupDAO;
        this.locationDAO = locationDAO;
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

        item.setUnit(getServerUnit(user, item.getUnit()));
        item.setGroup(getServerGroup(user, item.getGroup()));
        item.setLocation(getServerLocation(user, item.getLocation()));


        currentSession().saveOrUpdate(item);
        list.addItem(item);
        list.setVersion(list.getVersion() + 1);

        currentSession().save(list);

        return item;
    }

    @Override
    public Item updateListItem(User user, int listId, Item updatedItem) throws ListNotFoundException, ItemNotFoundException {

        TList list = getListById(user, listId);

        Item existingItem = getListItem(user, listId, updatedItem.getServerId());

        if(!comparer.itemEquals(existingItem, updatedItem)) {

            existingItem.setVersion(existingItem.getVersion() + 1);
            list.setVersion(list.getVersion() + 1);

            existingItem.setOrder(updatedItem.getOrder());
            existingItem.setBought(updatedItem.isBought());
            existingItem.setName(updatedItem.getName());
            existingItem.setAmount(updatedItem.getAmount());
            existingItem.setHighlight(updatedItem.isHighlight());
            existingItem.setBrand(updatedItem.getBrand());
            existingItem.setComment(updatedItem.getComment());
            existingItem.setLastBought(updatedItem.getLastBought());
            existingItem.setRepeatType(updatedItem.getRepeatType());
            existingItem.setPeriodType(updatedItem.getPeriodType());
            existingItem.setSelectedRepeatTime(updatedItem.getSelectedRepeatTime());
            existingItem.setRemindFromNextPurchaseOn(updatedItem.isRemindFromNextPurchaseOn());
            existingItem.setRemindAtDate(updatedItem.getRemindAtDate());

            existingItem.setUnit(getServerUnit(user, updatedItem.getUnit()));
            existingItem.setGroup(getServerGroup(user, updatedItem.getGroup()));
            existingItem.setLocation(getServerLocation(user, updatedItem.getLocation()));



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


    private Unit getServerUnit(User user, Unit clientUnit) {
        if(clientUnit == null) {
            return null;
        }

        int id = clientUnit.getServerId();
        Unit unit = unitDAO.getById(user, id);

        if(unit == null) {
            return unitDAO.createUnit(user, clientUnit);
        } else {
            return unit;
        }
    }

    private Group getServerGroup(User user, Group clientGroup) {
        if(clientGroup == null) {
            return null;
        }

        int id = clientGroup.getServerId();
        Group group = groupDAO.getById(user, id);

        if(group == null) {
            return groupDAO.createGroup(user, clientGroup);
        } else {
            return group;
        }
    }


    private LastLocation getServerLocation(User user, LastLocation clientLocation) {
        if(clientLocation == null) {
            return null;
        }

        int id = clientLocation.getServerId();
        LastLocation location = locationDAO.getById(user, id);

        if(location == null) {
            return locationDAO.createLocation(user, clientLocation);
        } else {
            return location;
        }
    }

}
