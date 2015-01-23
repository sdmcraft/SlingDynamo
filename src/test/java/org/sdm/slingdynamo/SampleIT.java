package org.sdm.slingdynamo;

import org.apache.sling.testing.tools.sling.SlingClient;
import org.apache.sling.testing.tools.sling.SlingTestBase;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class SampleIT extends SlingTestBase {
	 
    /**
     * The SlingClient can be used to interact with the repository when it is
     * started. By retrieving the information for the Server URL, username and
     * password, the Sling instance will be automatically started.
     */
    private SlingClient slingClient = new SlingClient(this.getServerBaseUrl(),
            this.getServerUsername(), this.getServerPassword());

    /**
     * Execute before the actual test, this will be used to setup the test data
     * 
     * @throws Exception
     */
    @Before
    public void init() throws Exception {
        //[..Initialize The Tests...]
    }

    /**
     * The actual test, will be executed once the Sling instance is started and
     * the setup is complete.
     * 
     * @throws Exception
     */
    @Test
    public void testSample() throws Exception {
    	Assert.fail();
        //[..Run The Tests, any method annotated with @Test will be executed...]
    }
} 