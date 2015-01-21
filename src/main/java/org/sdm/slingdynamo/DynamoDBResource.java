/**
 *
 */
package org.sdm.slingdynamo;

import java.util.Map;

import org.apache.sling.api.resource.AbstractResource;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceMetadata;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.ValueMap;


// TODO: Auto-generated Javadoc
/**
 * The Class DynamoDBResource.
 */
public class DynamoDBResource extends AbstractResource implements Resource {
    /** The Constant RESOURCE_TYPE. */
    public static final String RESOURCE_TYPE = "sling/servlet/default";

    /** The metadata. */
    private final ResourceMetadata metadata;

    /** The value map. */
    private final ValueMap valueMap;

    /** The resolver. */
    private final ResourceResolver resolver;

    /**
     * Instantiates a new dynamo db resource.
     *
     * @param resolver the resolver
     * @param metadata the metadata
     * @param valueMap the value map
     * @param resourceType the resource type
     */
    DynamoDBResource(ResourceResolver resolver, ResourceMetadata metadata, ValueMap valueMap,
        String resourceType) {

        this.valueMap = valueMap;
        this.resolver = resolver;
        this.metadata = metadata;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return getClass().getSimpleName() + " " + metadata.getResolutionPath();
    }

    /* (non-Javadoc)
     * @see org.apache.sling.api.resource.Resource#getPath()
     */
    public String getPath() {
        return metadata.getResolutionPath();
    }

    /* (non-Javadoc)
     * @see org.apache.sling.api.resource.Resource#getResourceMetadata()
     */
    public ResourceMetadata getResourceMetadata() {
        return metadata;
    }

    /* (non-Javadoc)
     * @see org.apache.sling.api.resource.Resource#getResourceResolver()
     */
    public ResourceResolver getResourceResolver() {
        return resolver;
    }

    /* (non-Javadoc)
     * @see org.apache.sling.api.resource.Resource#getResourceSuperType()
     */
    public String getResourceSuperType() {
        return null;
    }

    /* (non-Javadoc)
     * @see org.apache.sling.api.resource.Resource#getResourceType()
     */
    public String getResourceType() {
        return RESOURCE_TYPE;
    }

    /* (non-Javadoc)
     * @see org.apache.sling.api.adapter.SlingAdaptable#adaptTo(java.lang.Class)
     */
    @Override
    @SuppressWarnings("unchecked")
    public <AdapterType> AdapterType adaptTo(Class<AdapterType> type) {
        if (type == ValueMap.class || type == Map.class) {
            return (AdapterType) valueMap;
        }

        return super.adaptTo(type);
    }
}
