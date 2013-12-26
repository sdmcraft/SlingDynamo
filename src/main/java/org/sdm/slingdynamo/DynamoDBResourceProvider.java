package org.sdm.slingdynamo;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.amazonaws.services.dynamodbv2.model.ScanRequest;
import com.amazonaws.services.dynamodbv2.model.ScanResult;
import com.amazonaws.util.json.JSONException;
import com.amazonaws.util.json.JSONObject;

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

import java.util.Iterator;
import java.util.List;
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
    private String root = "";
    private String secretAccessKey = "";

    @Activate
    protected void activate(BundleContext context, Map<String, Object> config)
    {
        this.accessKey = PropertiesUtil.toString(config.get(PROP_ACCESS_KEY), "");
        this.secretAccessKey = PropertiesUtil.toString(config.get(PROP_SECRET_ACCESS_KEY), "");
        this.region = PropertiesUtil.toString(config.get(PROP_REGION), "");
        this.resourceType = PropertiesUtil.toString(config.get(SlingConstants.PROPERTY_RESOURCE_TYPE), "");
        this.root = PropertiesUtil.toString(config.get(ResourceProvider.ROOTS), "");

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
        Resource resource = null;

        if (path.startsWith(root) && (path.length() > root.length()))
        {
            String subPath = path.substring(root.length() + 1);
            String[] subPathSplits = subPath.split("/");
            String table = subPathSplits[0];
            String column = null;
            String value = null;

            if (subPathSplits.length > 1)
            {
                column = subPath.split("/")[1];
            }

            if (subPathSplits.length > 2)
            {
                value = subPath.split("/")[2];
            }

            ScanRequest scanRequest = null;
            ScanResult scanResult = null;
            ResourceMetadata resourceMetaData = new ResourceMetadata();

            if (column == null)
            {
                scanRequest = new ScanRequest(table);
                scanResult = dynamoDB.scan(scanRequest);
            }

            List<Map<String, AttributeValue>> resultList = scanResult.getItems();

            int rowNum = 0;

            for (Map<String, AttributeValue> result : resultList)
            {
                JSONObject row = new JSONObject();

                for (Map.Entry<String, AttributeValue> item : result.entrySet())
                {
                    String attributeName = item.getKey();
                    AttributeValue attributeValue = item.getValue();

                    try
                    {
                        row.put(attributeName, attributeValue.getS());
                    }
                    catch (JSONException e)
                    {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                }

                resourceMetaData.put(Integer.toString(rowNum++), row);
            }

            resource = new SyntheticResource(resolver, resourceMetaData, resourceType);
        }

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
