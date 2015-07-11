package de.fau.cs.mad.kwikshop.server.dao;

import de.fau.cs.mad.kwikshop.common.Item;
import de.fau.cs.mad.kwikshop.common.User;

import java.util.List;

public interface ListDAO<TList>  {

    TList createList(User user, TList list);

    TList updateOrCreateList(User user, TList list, boolean updateItems);

    boolean deleteList(User user, int listId);

    List<TList> getLists(User user);

    TList getListById(User user, int listId);

    Item getListItem(User user, int listId, int itemId);

    Item addListItem(User user, int listId, Item item);

    Item updateOrCreateListItem(User user, int listId, Item itemToUpdate);

    boolean deleteListItem(User user, int listId, int itemId);



}
