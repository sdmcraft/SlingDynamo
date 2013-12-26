package org.sdm.slingdynamo;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.amazonaws.services.dynamodbv2.model.GetItemRequest;
import com.amazonaws.services.dynamodbv2.model.GetItemResult;

import org.apache.felix.scr.annotations.Activate;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Deactivate;
import org.apache.felix.scr.annotations.Properties;
import org.apache.felix.scr.annotations.Property;
import org.apache.felix.scr.annotations.Service;
import org.apache.sling.api.SlingConstants;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceMetadata;
import org.apache.sling.api.resource.ResourceProvider;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.SyntheticResource;
import org.apache.sling.commons.osgi.PropertiesUtil;
import org.osgi.framework.BundleContext;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;


@Component(name = "DynamoDBResourceProvider", label = "DynamoDBResourceProvider", description = "Dynamo DB Resource Provider", immediate = true, metatype = true)
@Service
@Properties({@Property(name = "service.description", value = "Dynamo DB Resource Provider")
    , @Property(name = "service.vendor", value = "sdm.org")
    , @Property(name = ResourceProvider.ROOTS, value = "/content/dynamodb")
    , @Property(name = SlingConstants.PROPERTY_RESOURCE_TYPE, value = "/apps/dynamodb/render.jsp")
})
public class DynamoDBResourceProvider implements ResourceProvider
{
    @Property
    private static final String PROP_ACCESS_KEY = "aws.access.key";
    @Property
    private static final String PROP_SECRET_ACCESS_KEY = "aws.secret.access.key";
    @Property
    private static final String PROP_REGION = "aws.region";
    private AmazonDynamoDBClient dynamoDB;
    private String accessKey = "";
    private String region = "";
    private String resourceType = "";
    private String secretAccessKey = "";

    @Activate
    protected void activate(BundleContext context, Map<String, Object> config)
    {
        this.accessKey = PropertiesUtil.toString(config.get(PROP_ACCESS_KEY), "");
        this.secretAccessKey = PropertiesUtil.toString(config.get(PROP_SECRET_ACCESS_KEY), "");
        this.region = PropertiesUtil.toString(config.get(PROP_REGION), "");
        this.resourceType = PropertiesUtil.toString(config.get(SlingConstants.PROPERTY_RESOURCE_TYPE), "");

        AWSCredentials awsCredentials = new BasicAWSCredentials(accessKey, secretAccessKey);
        dynamoDB = new AmazonDynamoDBClient(awsCredentials);

        Region awsRegion = Region.getRegion(Regions.fromName(region));
        dynamoDB.setRegion(awsRegion);
    }

    @Deactivate
    protected void dectivate(BundleContext context, Map<String, Object> config)
    {
        dynamoDB.shutdown();
    }

    public Resource getResource(ResourceResolver resolver, String path)
    {
        Map<String, AttributeValue> key = new HashMap<String, AttributeValue>();
        key.put("name", new AttributeValue().withS("Airplane"));

        GetItemRequest getItemRequest = new GetItemRequest().withTableName("my-favorite-movies-table").withKey(key).withAttributesToGet(Arrays.asList("name", "fans", "rating", "year"));

        GetItemResult result = dynamoDB.getItem(getItemRequest);
        ResourceMetadata resourceMetaData = new ResourceMetadata();

        for (Map.Entry<String, AttributeValue> item : result.getItem().entrySet())
        {
            String attributeName = item.getKey();
            AttributeValue value = item.getValue();
            resourceMetaData.put(attributeName, value);
        }

        Resource resource = new SyntheticResource(resolver, resourceMetaData, resourceType);

        return resource;
    }

    public Iterator<Resource> listChildren(Resource arg0)
    {
        // TODO Auto-generated method stub
        return null;
    }

    public Resource getResource(ResourceResolver arg0, HttpServletRequest arg1, String arg2)
    {
        // TODO Auto-generated method stub
        return null;
    }
}
