package de.fau.cs.mad.kwikshop.server.api;

import com.wordnik.swagger.annotations.ApiParam;
import de.fau.cs.mad.kwikshop.common.Item;
import de.fau.cs.mad.kwikshop.common.RecipeServer;
import de.fau.cs.mad.kwikshop.common.User;
import de.fau.cs.mad.kwikshop.common.rest.RecipeResource;
import de.fau.cs.mad.kwikshop.server.dao.ListDAO;
import de.fau.cs.mad.kwikshop.server.exceptions.ItemNotFoundException;
import de.fau.cs.mad.kwikshop.server.exceptions.ListNotFoundException;
import io.dropwizard.auth.Auth;
import io.dropwizard.hibernate.UnitOfWork;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.List;

@Path("recipe")
public class RecipeResourceImpl implements RecipeResource {

    private final ListDAO<RecipeServer> recipeDAO;

    public RecipeResourceImpl(ListDAO<RecipeServer> recipeDAO) {

        if(recipeDAO == null) {
            throw new IllegalArgumentException("'recipeDAO' must not be null");
        }

        this.recipeDAO = recipeDAO;
    }


    @Override
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @UnitOfWork
    public List<RecipeServer> getList(@Auth User user) {
        return recipeDAO.getLists(user);
    }

    @Override
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("{listId}")
    @UnitOfWork
    public RecipeServer getList(@Auth User user, @PathParam("listId") int listId) {

        try {

            return recipeDAO.getListById(user, listId);

        } catch (ListNotFoundException e) {

            throw new WebApplicationException(Response.Status.NOT_FOUND);
        }

    }

    @PUT
    @UnitOfWork
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public RecipeServer createList(@Auth User user, RecipeServer list) {

        return recipeDAO.createList(user, list);

    }

    @Override
    @POST
    @UnitOfWork
    @Path("{listId}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public RecipeServer updateList(@Auth User user, @PathParam("listId") int listId, RecipeServer recipe, @QueryParam("updateItems") boolean updateItems) {

        if(recipe.getId() != listId) {
            throw new WebApplicationException(Response.Status.BAD_REQUEST);
        }

        try {

            return recipeDAO.updateList(user, recipe, updateItems);

        } catch (ListNotFoundException e) {

            throw new WebApplicationException(Response.Status.NOT_FOUND);
        }

    }

    @Override
    @DELETE
    @UnitOfWork
    @Path("{listId}")
    public void deleteList(@Auth User user, @PathParam("listId") int listId) {
        boolean listFound = recipeDAO.deleteList(user, listId);
        if(!listFound) {
            throw new WebApplicationException(Response.Status.NOT_FOUND);
        }
    }

    @GET
    @UnitOfWork
    @Path("{listId}/{itemId}")
    @Produces(MediaType.APPLICATION_JSON)
    public Item getListItem(@Auth User user, @PathParam("listId") int listId, @PathParam("itemId") int itemId) {

        try {

            return recipeDAO.getListItem(user, listId, itemId);

        } catch (ListNotFoundException | ItemNotFoundException e) {

            throw new WebApplicationException(Response.Status.NOT_FOUND);
        }

    }

    @Override
    @PUT
    @UnitOfWork
    @Path("{listId}/newItem")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Item createItem(@Auth User user, @PathParam("listId") int listId, @ApiParam(value = "Item to create", required = true) Item newItem) {

        try {

            return recipeDAO.addListItem(user, listId, newItem);

        } catch (ListNotFoundException e) {

            throw new WebApplicationException(Response.Status.NOT_FOUND);
        }
    }

    @Override
    @POST
    @UnitOfWork
    @Path("{listId}/{itemId}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Item updateItem(@Auth User user,
                           @ApiParam(value = "id of the list the item belongs to", required = true) @PathParam("listId") int listId,
                           @ApiParam(value = "id of the Item to update", required = true) @PathParam("itemId") int itemId,
                           @ApiParam(value = "new details of the specified item", required = true) Item item) {

        if(item.getId() != itemId) {
            throw new WebApplicationException(Response.Status.BAD_REQUEST);
        }

        try {

            return recipeDAO.updateListItem(user, listId, item);

        } catch (ListNotFoundException | ItemNotFoundException e) {

            throw new WebApplicationException(Response.Status.NOT_FOUND);
        }
    }

    @Override
    @DELETE
    @UnitOfWork
    @Path("{listId}/{itemId}")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Produces(MediaType.APPLICATION_JSON)
    public void deleteListItem(@Auth User user,
                               @ApiParam(value = "id of the list the item belongs to", required = true) @PathParam("listId") int listId,
                               @ApiParam(value = "id of the Item to update", required = true) @PathParam("itemId") int itemId) {

        try {

            recipeDAO.deleteListItem(user, listId, itemId);

        } catch (ListNotFoundException e) {

            throw new WebApplicationException(Response.Status.NOT_FOUND);
        }

    }
}
