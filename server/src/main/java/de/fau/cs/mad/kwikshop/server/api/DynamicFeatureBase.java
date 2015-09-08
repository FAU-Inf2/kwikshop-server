package de.fau.cs.mad.kwikshop.server.api;


import javax.ws.rs.container.DynamicFeature;
import javax.ws.rs.container.ResourceInfo;
import javax.ws.rs.core.FeatureContext;


public abstract class DynamicFeatureBase implements DynamicFeature {

    @Override
    public abstract void configure(ResourceInfo resourceInfo, FeatureContext context);


    public boolean hasAnnotation(ResourceInfo resourceInfo, Class annotation) {

        return resourceInfo.getResourceClass().getAnnotation(annotation) != null ||
               resourceInfo.getResourceMethod().getAnnotation(annotation) != null;
    }

}
