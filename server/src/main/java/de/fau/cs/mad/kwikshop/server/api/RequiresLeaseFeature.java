package de.fau.cs.mad.kwikshop.server.api;

import de.fau.cs.mad.kwikshop.server.api.annotations.RequiresClientId;
import de.fau.cs.mad.kwikshop.server.api.annotations.RequiresLease;

import javax.ws.rs.container.ResourceInfo;
import javax.ws.rs.core.FeatureContext;
import javax.ws.rs.ext.Provider;

@Provider
public class RequiresLeaseFeature extends DynamicFeatureBase {

    @Override
    public void configure(ResourceInfo resourceInfo, FeatureContext context) {

        if (hasAnnotation(resourceInfo, RequiresLease.class)) {

            if(!hasAnnotation(resourceInfo, RequiresClientId.class)) {
                throw new InvalidAnnotationException("@RequiresLease cannot we used without @RequiresClientId");
            }

            context.register(RequiresLeaseFilter.class);
        }

    }

}
