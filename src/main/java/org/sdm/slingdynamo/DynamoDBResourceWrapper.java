package org.sdm.slingdynamo;

import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceWrapper;

import java.util.Iterator;


public class DynamoDBResourceWrapper extends ResourceWrapper {
    public DynamoDBResourceWrapper(Resource resource) {
        super(resource);
    }

    @Override
    public Iterator<Resource> listChildren() {
        return super.listChildren();
    }
}
