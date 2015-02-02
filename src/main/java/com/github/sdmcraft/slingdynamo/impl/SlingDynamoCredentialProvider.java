package com.github.sdmcraft.slingdynamo.impl;

import java.util.Map;

import org.apache.felix.scr.annotations.Activate;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Property;
import org.apache.felix.scr.annotations.Service;
import org.apache.sling.commons.osgi.PropertiesUtil;
import org.osgi.framework.BundleContext;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;


@Component(immediate = true, metatype = true)
@Service
public class SlingDynamoCredentialProvider implements AWSCredentialsProvider {
    /** The Constant PROP_ACCESS_KEY. */
    @Property
    private static final String PROP_ACCESS_KEY = "aws.access.key";

    /** The Constant PROP_SECRET_ACCESS_KEY. */
    @Property
    private static final String PROP_SECRET_ACCESS_KEY = "aws.secret.access.key";
    private AWSCredentials awsCredentials;

    @Activate
    protected void activate(BundleContext context, Map<String, Object> config) {
        String accessKey = PropertiesUtil.toString(config.get(PROP_ACCESS_KEY),
                "");
        String secretAccessKey = PropertiesUtil.toString(config.get(
                    PROP_SECRET_ACCESS_KEY), "");
        awsCredentials = new BasicAWSCredentials(accessKey, secretAccessKey);
    }

    public AWSCredentials getCredentials() {
        return awsCredentials;
    }

    public void refresh() {
        throw new UnsupportedOperationException();
    }
}
