package de.fau.cs.mad.kwikshop.server.api;


import com.wordnik.swagger.annotations.Api;
import de.fau.cs.mad.kwikshop.common.ShoppingListServer;

import javax.ws.rs.Path;



@Path("shoppinglist")
@Api(value = "shoppinglist", description = "Storage and retrieval of shopping lists")
public interface ShoppingListResource extends ListResource<ShoppingListServer> {





}
