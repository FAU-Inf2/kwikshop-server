package de.fau.cs.mad.kwikshop.server.api;

import javax.ws.rs.container.DynamicFeature;
import javax.ws.rs.container.ResourceInfo;
import javax.ws.rs.core.FeatureContext;
import javax.ws.rs.ext.Provider;
import java.lang.reflect.AnnotatedElement;

@Provider
public class RequiresLeaseFeature implements DynamicFeature {

    @Override
    public void configure(ResourceInfo resourceInfo, FeatureContext context) {

        if (hasRequiresLeaseAnnotation(resourceInfo.getResourceClass())  ||
            hasRequiresLeaseAnnotation(resourceInfo.getResourceMethod())) {

            context.register(RequiresLeaseFilter.class);
        }

    }


    public boolean hasRequiresLeaseAnnotation(AnnotatedElement element) {

        return element.getAnnotation(RequiresLease.class)  != null;
    }
}
