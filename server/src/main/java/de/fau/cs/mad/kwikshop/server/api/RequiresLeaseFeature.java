package de.fau.cs.mad.kwikshop.server.api;

import de.fau.cs.mad.kwikshop.common.ArgumentNullException;
import de.fau.cs.mad.kwikshop.common.rest.annotations.RequiresClientId;
import de.fau.cs.mad.kwikshop.common.rest.annotations.RequiresLease;

import javax.ws.rs.container.ResourceInfo;
import javax.ws.rs.core.FeatureContext;
import javax.ws.rs.ext.Provider;

@Provider
public class RequiresLeaseFeature extends DynamicFeatureBase {

    private final RequiresLeaseFilter filterInstance;


    public RequiresLeaseFeature(RequiresLeaseFilter filterInstance) {

        if(filterInstance == null) {
            throw new ArgumentNullException("filterInstance");
        }

        this.filterInstance = filterInstance;
    }


    @Override
    public void configure(ResourceInfo resourceInfo, FeatureContext context) {

        if (hasAnnotation(resourceInfo, RequiresLease.class)) {

            if(!hasAnnotation(resourceInfo, RequiresClientId.class)) {
                throw new InvalidAnnotationException("@RequiresLease cannot we used without @RequiresClientId");
            }

            context.register(filterInstance);
        }

    }

}
