package de.fau.cs.mad.kwikshop.server.api;

import de.fau.cs.mad.kwikshop.server.api.annotations.RequiresClientId;


import javax.ws.rs.container.ResourceInfo;
import javax.ws.rs.core.FeatureContext;
import javax.ws.rs.ext.Provider;

@Provider
public class RequiresClientIdFeature extends DynamicFeatureBase {


    @Override
    public void configure(ResourceInfo resourceInfo, FeatureContext context) {

        if (hasAnnotation(resourceInfo, RequiresClientId.class)) {

            context.register(RequiresClientIdFilter.class);
        }

    }


}
