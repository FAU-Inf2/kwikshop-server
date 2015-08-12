package de.fau.cs.mad.kwikshop.server.dao;

import org.hibernate.Query;
import org.hibernate.SessionFactory;

import java.util.ArrayList;
import java.util.List;

import de.fau.cs.mad.kwikshop.common.ShoppingList;
import de.fau.cs.mad.kwikshop.common.ShoppingListServer;
import de.fau.cs.mad.kwikshop.common.User;
import de.fau.cs.mad.kwikshop.common.util.NamedQueryConstants;
import de.fau.cs.mad.kwikshop.server.exceptions.ListNotFoundException;

public class SharedShoppingListDAO extends AbstractListDAO<ShoppingListServer> {

    /**
     * Creates a new DAO with a given session provider.
     *
     * @param sessionFactory a session provider
     */
    public SharedShoppingListDAO(SessionFactory sessionFactory, UnitDAO unitDAO, GroupDAO groupDAO, LocationDAO locationDAO) {
        super(sessionFactory, unitDAO, groupDAO, locationDAO);
    }


    @Override
    public ShoppingListServer updateList(User user, ShoppingListServer shoppingList) throws ListNotFoundException {

        ShoppingListServer existingList = getListById(user, shoppingList.getId());

        if (!comparer.shoppingListEquals(existingList, shoppingList)) {

            existingList.setName(shoppingList.getName());
            existingList.setSortTypeInt(shoppingList.getSortTypeInt());
            existingList.setLocation(shoppingList.getLocation());
            existingList.setLastModifiedDate(shoppingList.getLastModifiedDate());

            existingList.setVersion(existingList.getVersion() + 1);

            existingList = persist(existingList);
        }
        return existingList;


    }

    @Override
    public List<ShoppingListServer> getLists(User user) {

        List<ShoppingListServer> lists = new ArrayList<>();

        for (ShoppingListServer sharedList : user.getSharedShoppingLists()) {
            if(!sharedList.getDeleted())
                lists.add(sharedList);
        }

        return lists;
    }

    @Override
    public List<ShoppingListServer> getDeletedLists(User user) {
        List<ShoppingListServer> lists = new ArrayList<>();

        for (ShoppingListServer sharedList : user.getSharedShoppingLists()) {
            if(sharedList.getDeleted())
                lists.add(sharedList);
        }

        return lists;
    }

    @Override
    public ShoppingListServer getListById(User user, int listId) throws ListNotFoundException {

        for(ShoppingListServer sharedList : getLists(user)) {
            if(sharedList.getId() == listId)
                return sharedList;
        }

        throw new ListNotFoundException(String.format("SharedShoppingList with id %s for user %s not found", listId, user.getId()));
    }

    @Override
    public boolean deleteList(User user, int listId) {
        ShoppingListServer sharedList;
        try {
            sharedList = getListById(user, listId);
        } catch (ListNotFoundException e) {
            return false;
        }

        // TODO: TEST THIS
        user.getSharedShoppingLists().remove(sharedList);
        //sharedList.getSharedWith().remove(user);
        currentSession().persist(sharedList);
        currentSession().persist(user);

        return true;
    }

    public ShoppingListServer getListBySharingCode(String sharingCode) throws ListNotFoundException {

        Query query = namedQuery(NamedQueryConstants.SHOPPINGLIST_GET_BY_SHARINGCODE)
                .setParameter(NamedQueryConstants.SHARING_CODE, sharingCode);

        List<ShoppingListServer> result = list(query);

        if (result.size() != 1) {
            throw new ListNotFoundException(String.format("ShoppingList with sharingCode %s not found", sharingCode));
        }

        return result.get(0);
    }



}
