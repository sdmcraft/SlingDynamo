package org.sdm.slingdynamo;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;


// TODO: Auto-generated Javadoc
/**
 * The Class AppTest.
 */
public class AppTest extends TestCase {
    /**
     * Instantiates a new app test.
     *
     * @param testName the test name
     */
    public AppTest(String testName) {
        super(testName);
    }

    /**
     * Suite.
     *
     * @return the test
     */
    public static Test suite() {
        return new TestSuite(AppTest.class);
    }

    /**
     * Test app.
     */
    public void testApp() {
        assertTrue(true);
    }
}
