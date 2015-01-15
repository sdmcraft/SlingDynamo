package org.sdm.slingdynamo;

import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceWrapper;

import java.util.Iterator;


// TODO: Auto-generated Javadoc
/**
 * The Class DynamoDBResourceWrapper.
 */
public class DynamoDBResourceWrapper extends ResourceWrapper {
    /**
     * Instantiates a new dynamo db resource wrapper.
     *
     * @param resource the resource
     */
    public DynamoDBResourceWrapper(Resource resource) {
        super(resource);
    }

    /* (non-Javadoc)
     * @see org.apache.sling.api.resource.ResourceWrapper#listChildren()
     */
    @Override
    public Iterator<Resource> listChildren() {
        return super.listChildren();
    }
}
