package de.fau.cs.mad.kwikshop.server.api;


import com.wordnik.swagger.annotations.Api;

import de.fau.cs.mad.kwikshop.common.RecipeServer;
import de.fau.cs.mad.kwikshop.common.User;

import io.dropwizard.auth.Auth;
import io.dropwizard.hibernate.UnitOfWork;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.util.List;


@Path("recipe")
@Api(value = "recipe", description = "Storage and retrieval of recipes")
public interface RecipeResource extends ListResource {



    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @UnitOfWork
    List<RecipeServer> getList(@Auth User user);

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("{listId}")
    @UnitOfWork
    RecipeServer getList(@Auth User user, @PathParam("listId") int listId);

    @PUT
    @UnitOfWork
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    RecipeServer createList(@Auth User user, RecipeServer list);

    @POST
    @UnitOfWork
    @Path("{listId}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    RecipeServer updateList(@Auth User user, @PathParam("listId") int listId, RecipeServer list, @QueryParam("updateItems") boolean updateItems);


}
