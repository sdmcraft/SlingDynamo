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

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.BasicAWSCredentials;

import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient;

import org.osgi.framework.BundleContext;

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


/**
 * DOCUMENT ME!
 *
 * @author $author$
 * @version $Revision: 1.3 $
 */
@Component(name = "DynamoDBResourceProviderFactory", label = "DynamoDBResourceProviderFactory", description = "Dynamo DB Resource Provider Factory", immediate = true, metatype = true)
@Service
@Properties({@Property(name = "service.description",value = "Dynamo DB Resource Provider Factory")
    , @Property(name = "service.vendor",value = "sdm.org")
})
public class DynamoDBResourceProviderFactory
    implements ResourceProviderFactory
{
    //~ Static variables/initializers ----------------------------------------------------

    @Property
    private static final String PROP_ACCESS_KEY = "aws.access.key";
    @Property
    private static final String PROP_SECRET_ACCESS_KEY = "aws.secret.access.key";
    @Property
    private static final String PROP_REGION = "aws.region";
    @Property
    private static final String PROP_ROOTS = ResourceProvider.ROOTS;
    @Property
    private static final String PROP_RESOURCE_TYPE = SlingConstants.PROPERTY_RESOURCE_TYPE;
    

    //~ Instance variables ---------------------------------------------------------------

    private AmazonDynamoDBClient dynamoDB;
    private String accessKey = "";
    private String region = "";
    private String resourceType = "";
    private String root = "";
    private String secretAccessKey = "";

    //~ Methods --------------------------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @param arg0 DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     *
     * @throws LoginException DOCUMENT ME!
     */
    public ResourceProvider getAdministrativeResourceProvider(Map<String, Object> arg0)
      throws LoginException
    {
        return new DynamoDBResourceProvider(root, dynamoDB, resourceType);
    }


    /**
     * DOCUMENT ME!
     *
     * @param arg0 DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     *
     * @throws LoginException DOCUMENT ME!
     */
    public ResourceProvider getResourceProvider(Map<String, Object> arg0)
      throws LoginException
    {
        return new DynamoDBResourceProvider(root, dynamoDB, resourceType);
    }


    /**
     * DOCUMENT ME!
     *
     * @param context DOCUMENT ME!
     * @param config DOCUMENT ME!
     */
    @Activate
    protected void activate(
        BundleContext       context,
        Map<String, Object> config)
    {
        this.accessKey = PropertiesUtil.toString(config.get(PROP_ACCESS_KEY), "");
        this.secretAccessKey = PropertiesUtil.toString(
                config.get(PROP_SECRET_ACCESS_KEY), "");
        this.region = PropertiesUtil.toString(config.get(PROP_REGION), "");
        
        this.root = PropertiesUtil.toString(config.get(ResourceProvider.ROOTS), "");
        this.resourceType = PropertiesUtil.toString(config.get(SlingConstants.PROPERTY_RESOURCE_TYPE), "");
        
        AWSCredentials awsCredentials =
            new BasicAWSCredentials(accessKey, secretAccessKey);
        dynamoDB = new AmazonDynamoDBClient(awsCredentials);

        Region awsRegion = Region.getRegion(Regions.fromName(region));
        dynamoDB.setRegion(awsRegion);
    }


    /**
     * DOCUMENT ME!
     *
     * @param context DOCUMENT ME!
     * @param config DOCUMENT ME!
     */
    @Deactivate
    protected void dectivate(
        BundleContext       context,
        Map<String, Object> config)
    {
        dynamoDB.shutdown();
    }
}
