/*
 * Copyright (c) 2013-14, Satya Deep Maheshwari. All rights reserved.
 *
 * The contents of this file are subject to the MIT License
 * You may not use this file except in compliance with the License.
 * A copy of the License is available at
 * http://opensource.org/licenses/MIT
 *
 * Copyright (c) 2013-2014 Satya Deep Maheshwari
 */
package org.sdm.slingdynamo;

import java.util.Map;

import org.apache.felix.scr.annotations.Activate;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Deactivate;
import org.apache.felix.scr.annotations.Properties;
import org.apache.felix.scr.annotations.Property;
import org.apache.felix.scr.annotations.Service;
import org.apache.sling.api.SlingConstants;
import org.apache.sling.api.resource.LoginException;
import org.apache.sling.api.resource.ResourceProvider;
import org.apache.sling.api.resource.ResourceProviderFactory;
import org.apache.sling.commons.osgi.PropertiesUtil;
import org.osgi.framework.BundleContext;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient;
import com.amazonaws.services.dynamodbv2.document.DynamoDB;


// TODO: Auto-generated Javadoc
/**
 * A factory for creating DynamoDBResourceProvider objects.
 */
@Component(name = "DynamoDBResourceProviderFactory", label = "DynamoDBResourceProviderFactory", description = "Dynamo DB Resource Provider Factory", immediate = true, metatype = true)
@Service
@Properties({@Property(name = "service.description",value = "Dynamo DB Resource Provider Factory")
    , @Property(name = "service.vendor",value = "sdm.org")
})
public class DynamoDBResourceProviderFactory implements ResourceProviderFactory {
    /** The Constant PROP_ACCESS_KEY. */
    @Property
    private static final String PROP_ACCESS_KEY = "aws.access.key";

    /** The Constant PROP_SECRET_ACCESS_KEY. */
    @Property
    private static final String PROP_SECRET_ACCESS_KEY = "aws.secret.access.key";

    /** The Constant PROP_REGION. */
    @Property
    private static final String PROP_REGION = "aws.region";

    /** The Constant PROP_ROOTS. */
    @Property
    private static final String PROP_ROOTS = ResourceProvider.ROOTS;

    /** The Constant PROP_RESOURCE_TYPE. */
    @Property
    private static final String PROP_RESOURCE_TYPE = SlingConstants.PROPERTY_RESOURCE_TYPE;

    /** The dynamo db client. */
    private AmazonDynamoDBClient dynamoDBClient;

    /** The dynamo db. */
    private DynamoDB dynamoDB;

    /** The access key. */
    private String accessKey = "";

    /** The region. */
    private String region = "";

    /** The resource type. */
    private String resourceType = "";

    /** The root. */
    private String root = "";

    /** The secret access key. */
    private String secretAccessKey = "";

    /* (non-Javadoc)
     * @see org.apache.sling.api.resource.ResourceProviderFactory#getAdministrativeResourceProvider(java.util.Map)
     */
    public ResourceProvider getAdministrativeResourceProvider(
        Map<String, Object> arg0) throws LoginException {
        return new DynamoDBResourceProvider(root, dynamoDBClient, dynamoDB,
            resourceType);
    }

    /* (non-Javadoc)
     * @see org.apache.sling.api.resource.ResourceProviderFactory#getResourceProvider(java.util.Map)
     */
    public ResourceProvider getResourceProvider(Map<String, Object> arg0)
        throws LoginException {
        return new DynamoDBResourceProvider(root, dynamoDBClient, dynamoDB,
            resourceType);
    }

    /**
     * Activate.
     *
     * @param context the context
     * @param config the config
     */
    @Activate
    protected void activate(BundleContext context, Map<String, Object> config) {
        this.accessKey = PropertiesUtil.toString(config.get(PROP_ACCESS_KEY), "");
        this.secretAccessKey = PropertiesUtil.toString(config.get(
                    PROP_SECRET_ACCESS_KEY), "");
        this.region = PropertiesUtil.toString(config.get(PROP_REGION), "");

        this.root = PropertiesUtil.toString(config.get(ResourceProvider.ROOTS),
                "");
        this.resourceType = PropertiesUtil.toString(config.get(
                    SlingConstants.PROPERTY_RESOURCE_TYPE), "");

        AWSCredentials awsCredentials = new BasicAWSCredentials(accessKey,
                secretAccessKey);
        dynamoDBClient = new AmazonDynamoDBClient(awsCredentials);

        Region awsRegion = Region.getRegion(Regions.fromName(region));
        dynamoDBClient.setRegion(awsRegion);

        dynamoDB = new DynamoDB(dynamoDBClient);
    }

    /**
     * Dectivate.
     *
     * @param context the context
     * @param config the config
     */
    @Deactivate
    protected void dectivate(BundleContext context, Map<String, Object> config) {
        dynamoDBClient.shutdown();
    }
}
