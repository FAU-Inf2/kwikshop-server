package de.fau.cs.mad.kwikshop.server.dao;

import de.fau.cs.mad.kwikshop.common.Item;
import de.fau.cs.mad.kwikshop.common.RecipeServer;
import de.fau.cs.mad.kwikshop.common.User;
import de.fau.cs.mad.kwikshop.common.util.NamedQueryConstants;
import de.fau.cs.mad.kwikshop.server.exceptions.ListNotFoundException;
import org.hibernate.Query;
import org.hibernate.SessionFactory;

import java.util.List;


public class RecipeDAO extends AbstractListDAO<RecipeServer> {


    /**
     * Creates a new DAO with a given session provider.
     *
     * @param sessionFactory a session provider
     */
    public RecipeDAO(SessionFactory sessionFactory) {
        super(sessionFactory);
    }


    @Override
    public RecipeServer updateList(User user, RecipeServer recipe, boolean updateItems) throws ListNotFoundException {

        RecipeServer existingRecipe = getListById(user, recipe.getId());

        existingRecipe.setName(recipe.getName());
        existingRecipe.setScaleFactor(recipe.getScaleFactor());
        existingRecipe.setScaleName(recipe.getScaleName());
        existingRecipe.setLastModifiedDate(recipe.getLastModifiedDate());

        if(updateItems) {

            //remove all existing items
            for(Item i : existingRecipe.getItems()) {
                existingRecipe.removeItem(i.getServerId());
            }

            //add items from updated value
            for(Item i : recipe.getItems()) {
                existingRecipe.addItem(i);
            }
        }


        existingRecipe = persist(existingRecipe);
        return existingRecipe;
    }

    @Override
    public List<RecipeServer> getLists(User user) {
        Query query = namedQuery(NamedQueryConstants.RECIPE_GET_ALL_FOR_USER)
                .setParameter(NamedQueryConstants.USER_ID, user.getId());
        return list(query);
    }

    @Override
    public List<RecipeServer> getDeletedLists(User user) {
        Query query = namedQuery(NamedQueryConstants.RECIPE_GET_DELETED_LISTS)
                .setParameter(NamedQueryConstants.USER_ID, user.getId());
        return list(query);
    }

    @Override
    public RecipeServer getListById(User user, int listId) throws ListNotFoundException {

        Query query = namedQuery(NamedQueryConstants.RECIPE_GET_BY_LISTID)
                .setParameter(NamedQueryConstants.USER_ID, user.getId())
                .setParameter(NamedQueryConstants.LIST_ID, listId);

        List<RecipeServer> result = list(query);

        if(result.size() != 1) {
            throw new ListNotFoundException(String.format("Recipe with id %s for user %s not found", listId, user.getId()));
        }

        return result.get(0);
    }


}
