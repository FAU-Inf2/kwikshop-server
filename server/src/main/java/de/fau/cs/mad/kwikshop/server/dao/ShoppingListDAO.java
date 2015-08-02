package de.fau.cs.mad.kwikshop.server.dao;

import de.fau.cs.mad.kwikshop.common.ShoppingListServer;
import de.fau.cs.mad.kwikshop.common.util.NamedQueryConstants;
import de.fau.cs.mad.kwikshop.common.User;
import de.fau.cs.mad.kwikshop.server.exceptions.ListNotFoundException;
import org.hibernate.Query;
import org.hibernate.SessionFactory;

import java.util.List;

public class ShoppingListDAO extends AbstractListDAO<ShoppingListServer> {


    /**
     * Creates a new DAO with a given session provider.
     *
     * @param sessionFactory a session provider
     */
    public ShoppingListDAO(SessionFactory sessionFactory) {
        super(sessionFactory);
    }


    @Override
    public ShoppingListServer updateList(User user, ShoppingListServer shoppingList) throws ListNotFoundException {


        ShoppingListServer existingList = getListById(user, shoppingList.getId());

        if (!shoppingListEquals(existingList, shoppingList)) {

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

        Query query = namedQuery(NamedQueryConstants.SHOPPINGLIST_GET_ALL_FOR_USER)
                .setParameter(NamedQueryConstants.USER_ID, user.getId());
        return list(query);
    }

    @Override
    public List<ShoppingListServer> getDeletedLists(User user) {
        Query query = namedQuery(NamedQueryConstants.SHOPPINGLIST_GET_DELETED_LISTS)
                .setParameter(NamedQueryConstants.USER_ID, user.getId());
        return list(query);
    }

    @Override
    public ShoppingListServer getListById(User user, int listId) throws ListNotFoundException {

        Query query = namedQuery(NamedQueryConstants.SHOPPINGLIST_GET_BY_LISTID)
                .setParameter(NamedQueryConstants.USER_ID, user.getId())
                .setParameter(NamedQueryConstants.LIST_ID, listId);

        List<ShoppingListServer> result = list(query);

        if (result.size() != 1) {
            throw new ListNotFoundException(String.format("ShoppingList with id %s for user %s not found", listId, user.getId()));
        }

        return result.get(0);
    }


    protected boolean shoppingListEquals(ShoppingListServer shoppingList1, ShoppingListServer shoppingList2) {

        return stringEquals(shoppingList1.getName(), shoppingList2.getName()) &&
                shoppingList1.getSortTypeInt() == shoppingList2.getSortTypeInt() &&
                locationEquals(shoppingList1.getLocation(), shoppingList2.getLocation()) &&
                dateEquals(shoppingList1.getLastModifiedDate(), shoppingList2.getLastModifiedDate());
    }


}
