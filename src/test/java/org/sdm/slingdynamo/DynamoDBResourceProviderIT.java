package org.sdm.slingdynamo;

import com.amazonaws.AmazonServiceException;

import com.amazonaws.auth.BasicAWSCredentials;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient;
import com.amazonaws.services.dynamodbv2.model.AttributeDefinition;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.amazonaws.services.dynamodbv2.model.CreateTableRequest;
import com.amazonaws.services.dynamodbv2.model.DescribeTableRequest;
import com.amazonaws.services.dynamodbv2.model.KeySchemaElement;
import com.amazonaws.services.dynamodbv2.model.KeyType;
import com.amazonaws.services.dynamodbv2.model.ProvisionedThroughput;
import com.amazonaws.services.dynamodbv2.model.PutItemRequest;
import com.amazonaws.services.dynamodbv2.model.PutItemResult;
import com.amazonaws.services.dynamodbv2.model.ScalarAttributeType;
import com.amazonaws.services.dynamodbv2.model.TableDescription;
import com.amazonaws.services.dynamodbv2.model.TableStatus;

import org.apache.sling.testing.tools.sling.SlingClient;
import org.apache.sling.testing.tools.sling.SlingTestBase;

import org.junit.BeforeClass;
import org.junit.Test;

import org.skyscreamer.jsonassert.JSONAssert;

import java.util.HashMap;
import java.util.Map;


public class DynamoDBResourceProviderIT extends SlingTestBase {
    private static final String PORT = System.getProperty("dynamodb.port");
    private static final String testDbTable = "data";
    private static AmazonDynamoDB dynamoDB;

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
    @BeforeClass
    public static void init() throws Exception {
        dynamoDB = new AmazonDynamoDBClient(new BasicAWSCredentials("", ""));
        dynamoDB.setEndpoint(String.format("http://localhost:%s",
                DynamoDBResourceProviderIT.PORT));

        AttributeDefinition id = new AttributeDefinition("id",
                ScalarAttributeType.N);

        // Create a table with a primary hash key named 'name', which holds a string
        CreateTableRequest createTableRequest = new CreateTableRequest().withTableName(testDbTable)
                                                                        .withKeySchema(new KeySchemaElement().withAttributeName(
                    "id").withKeyType(KeyType.HASH)).withAttributeDefinitions(id)
                                                                        .withProvisionedThroughput(new ProvisionedThroughput().withReadCapacityUnits(
                    1L).withWriteCapacityUnits(1L));
        TableDescription createdTableDescription = dynamoDB.createTable(createTableRequest)
                                                           .getTableDescription();
        // Wait for it to become active
        waitForTableToBecomeAvailable(testDbTable);

        // Describe our new table
        DescribeTableRequest describeTableRequest = new DescribeTableRequest().withTableName(testDbTable);
        TableDescription tableDescription = dynamoDB.describeTable(describeTableRequest)
                                                    .getTable();
        //System.out.println("Table Description: " + tableDescription);
        createItem(1, "North America", null, null, new String[] { "1" });
        createItem(2, "South America", null, null, new String[] { "1" });
        createItem(3, "USA", 1, 1, new String[] { "1", "2" });
        createItem(4, "Brazil", 2, 1, null);
        createItem(5, "California", 3, 1, new String[] { "1" });
        createItem(6, "San Francisco", 5, 1, null);
        createItem(7, "Texas", 3, 2, null);

        //        ScanRequest scanRequest = new ScanRequest(testDbTable);
        //        ScanResult scanResult = dynamoDB.scan(scanRequest);
        //        System.out.println(("Scan Result: " + scanResult));
    }

    /**
     * The actual test, will be executed once the Sling instance is started and
     * the setup is complete.
     *
     * @throws Exception
     */
    @Test
    public void testGetResource() throws Exception {
        //    	{"id":"1","name":"North America","children":"[1]"}
        //    	{"id":"2","name":"South America","children":"[1]"}
        //    	{"id":"3","child_id":"1","name":"USA","children":"[1, 2]","parent":"1"}
        //    	{"id":"4","child_id":"1","name":"Brazil","parent":"2"}
        //    	{"id":"5","child_id":"1","name":"California","children":"[1]","parent":"3"}
        //    	{"id":"6","child_id":"1","name":"San Francisco","parent":"5"}
        //    	{"id":"7","child_id":"2","name":"Texas","parent":"3"}
        String expected = "{\"id\":\"1\",\"name\":\"North America\",\"children\":\"[1]\"}";
        String actual = getRequestExecutor()
                            .execute(getRequestBuilder()
                                         .buildGetRequest("/content/dynamodb/data/1.json")
                                         .withCredentials("admin", "admin"))
                            .assertStatus(200).getContent();
        JSONAssert.assertEquals(expected, actual, false);

        expected = "{\"id\":\"3\",\"child_id\":\"1\",\"name\":\"USA\",\"children\":\"[1, 2]\",\"parent\":\"1\"}";
        actual = getRequestExecutor()
                     .execute(getRequestBuilder()
                                  .buildGetRequest("/content/dynamodb/data/3.json")
                                  .withCredentials("admin", "admin"))
                     .assertStatus(200).getContent();
        JSONAssert.assertEquals(expected, actual, false);
    }

    @Test
    public void testListChildren() throws Exception {
        String expected = "{\"id\":\"1\",\"name\":\"North America\",\"children\":\"[1]\",\"1\":{\"id\":\"3\",\"child_id\":\"1\",\"name\":\"USA\",\"children\":\"[1, 2]\",\"parent\":\"1\"}}";
        String actual = getRequestExecutor()
                            .execute(getRequestBuilder()
                                         .buildGetRequest("/content/dynamodb/data/1.1.json")
                                         .withCredentials("admin", "admin"))
                            .assertStatus(200).getContent();
        JSONAssert.assertEquals(expected, actual, false);
    }

    /**
     * Wait for table to become available.
     *
     * @param tableName the table name
     */
    private static void waitForTableToBecomeAvailable(String tableName) {
        System.out.println("Waiting for " + tableName + " to become ACTIVE...");

        long startTime = System.currentTimeMillis();
        long endTime = startTime + (10 * 60 * 1000);

        while (System.currentTimeMillis() < endTime) {
            try {
                Thread.sleep(1000 * 20);
            } catch (Exception e) {
                ;
            }

            try {
                DescribeTableRequest request = new DescribeTableRequest().withTableName(tableName);
                TableDescription tableDescription = dynamoDB.describeTable(request)
                                                            .getTable();
                String tableStatus = tableDescription.getTableStatus();
                System.out.println("  - current state: " + tableStatus);

                if (tableStatus.equals(TableStatus.ACTIVE.toString())) {
                    return;
                }
            } catch (AmazonServiceException ase) {
                if (ase.getErrorCode()
                           .equalsIgnoreCase("ResourceNotFoundException") == false) {
                    throw ase;
                }
            }
        }

        throw new RuntimeException("Table " + tableName + " never went active");
    }

    private static void createItem(Integer id, String name, Integer parent,
        Integer childId, String[] children) {
        Map<String, AttributeValue> item = new HashMap<String, AttributeValue>();
        item.put("id", new AttributeValue().withN(id.toString()));
        item.put("name", new AttributeValue(name));

        if (parent != null) {
            item.put("parent", new AttributeValue().withN(parent.toString()));
        }

        if (childId != null) {
            item.put("child_id", new AttributeValue().withN(childId.toString()));
        }

        if (children != null) {
            item.put("children", new AttributeValue().withNS(children));
        }

        PutItemRequest putItemRequest = new PutItemRequest(testDbTable, item);
        PutItemResult putItemResult = dynamoDB.putItem(putItemRequest);
    }
}
