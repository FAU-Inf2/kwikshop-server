package de.fau.cs.mad.kwikshop.server.api;

import de.fau.cs.mad.kwikshop.common.util.StringHelper;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;

public class LeaseHelpers {

    /**
     * Throws a WebApplicationException with status "Bad Request" if the specified value is not an valid client id
     *
     * @param value The value to check for validity
     */
    static void ensureClientIdIsValid(String value) {

        // currently the only condition for validity: String must not be empty
        if (StringHelper.isNullOrWhiteSpace(value)) {
            throw new WebApplicationException(Response.Status.BAD_REQUEST);
        }
    }

}
