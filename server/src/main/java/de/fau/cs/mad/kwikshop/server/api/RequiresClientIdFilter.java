package de.fau.cs.mad.kwikshop.server.api;

import de.fau.cs.mad.kwikshop.common.rest.Constants;
import de.fau.cs.mad.kwikshop.common.util.StringHelper;

import javax.annotation.Priority;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.Provider;
import java.io.IOException;

@Provider
@Priority(100)
public class RequiresClientIdFilter implements ContainerRequestFilter {

    @Override
    public void filter(ContainerRequestContext requestContext) throws IOException {

        String clientId = requestContext.getHeaderString(Constants.KWIKSHOP_CLIENT_ID);

        // currently the only condition for validity: String must not be empty
        if (StringHelper.isNullOrWhiteSpace(clientId)) {
            throw new WebApplicationException(Response.Status.BAD_REQUEST);
        }

    }
}
