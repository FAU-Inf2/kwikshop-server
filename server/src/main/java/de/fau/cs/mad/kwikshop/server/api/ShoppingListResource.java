package de.fau.cs.mad.kwikshop.server.api;


import com.wordnik.swagger.annotations.Api;
import de.fau.cs.mad.kwikshop.common.ShoppingListServer;
import de.fau.cs.mad.kwikshop.common.User;
import io.dropwizard.auth.Auth;
import io.dropwizard.hibernate.UnitOfWork;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.util.List;


@Path("shoppinglist")
@Api(value = "shoppinglist", description = "Storage and retrieval of shopping lists")
public interface ShoppingListResource extends ListResource {



    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @UnitOfWork
    List<ShoppingListServer> getList(@Auth User user);

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("{listId}")
    @UnitOfWork
    ShoppingListServer getList(@Auth User user, @PathParam("listId") String listId);

    @PUT
    @UnitOfWork
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    ShoppingListServer createList(@Auth User user, ShoppingListServer list);

    @POST
    @UnitOfWork
    @Path("{listId}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    ShoppingListServer updateList(@Auth User user, @PathParam("listId") int listId, ShoppingListServer list, @QueryParam("updateItems") boolean updateItems);


}
