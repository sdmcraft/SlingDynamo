/**
 * 
 */
package org.sdm.slingdynamo;

import java.util.HashMap;
import java.util.Map;

import org.apache.sling.api.resource.AbstractResource;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceMetadata;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.ValueMap;
import org.apache.sling.api.wrappers.ValueMapDecorator;

/**
 * @author satyadeep
 *
 */
public class DynamoDBResource extends AbstractResource implements Resource {

    private final String path;
    private final ResourceMetadata metadata;
    private final ValueMap valueMap;
    private final ResourceResolver resolver;

    public static final String RESOURCE_TYPE = "sling/servlet/default";
    
    DynamoDBResource(ResourceResolver resolver, String path, ValueMap valueMap, String resourceType) {
        this.path = path;
                
        this.valueMap = valueMap;
        this.resolver = resolver;
        
        metadata = new ResourceMetadata();
        metadata.setResolutionPath(path);
        metadata.setResolutionPathInfo(path.substring(path.indexOf('.')));
    }
    
    @Override
    public String toString() {
        return getClass().getSimpleName() + " " + path;
    }
    
    public String getPath() {
        return path;
    }

    public ResourceMetadata getResourceMetadata() {
        return metadata;
    }

    public ResourceResolver getResourceResolver() {
        return resolver;
    }

    public String getResourceSuperType() {
        return null;
    }

    public String getResourceType() {
        return RESOURCE_TYPE;
    }
 	
    @Override
    @SuppressWarnings("unchecked")
    public <AdapterType> AdapterType adaptTo(Class<AdapterType> type) {
        if(type == ValueMap.class) {   
            return (AdapterType)valueMap;
        }
        return super.adaptTo(type);
    }


}
