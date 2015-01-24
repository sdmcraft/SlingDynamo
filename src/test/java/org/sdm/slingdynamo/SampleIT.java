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
import com.amazonaws.services.dynamodbv2.model.ScanRequest;
import com.amazonaws.services.dynamodbv2.model.ScanResult;
import com.amazonaws.services.dynamodbv2.model.TableDescription;
import com.amazonaws.services.dynamodbv2.model.TableStatus;

import org.apache.sling.testing.tools.sling.SlingClient;
import org.apache.sling.testing.tools.sling.SlingTestBase;

import org.junit.Before;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;


public class SampleIT extends SlingTestBase {
    private static final String PORT = System.getProperty("dynamodb.port");

    /**
     * The SlingClient can be used to interact with the repository when it is
     * started. By retrieving the information for the Server URL, username and
     * password, the Sling instance will be automatically started.
     */
    private SlingClient slingClient = new SlingClient(this.getServerBaseUrl(),
            this.getServerUsername(), this.getServerPassword());
    private AmazonDynamoDB dynamoDB;

    /**
     * Execute before the actual test, this will be used to setup the test data
     *
     * @throws Exception
     */
    @Before
    public void init() throws Exception {
        dynamoDB = new AmazonDynamoDBClient(new BasicAWSCredentials("", ""));
        dynamoDB.setEndpoint(String.format("http://localhost:%s", SampleIT.PORT));

        String tableName = "data";
        AttributeDefinition id = new AttributeDefinition("id",
                ScalarAttributeType.N);

        // Create a table with a primary hash key named 'name', which holds a string
        CreateTableRequest createTableRequest = new CreateTableRequest().withTableName(tableName)
                                                                        .withKeySchema(new KeySchemaElement().withAttributeName(
                    "id").withKeyType(KeyType.HASH)).withAttributeDefinitions(id)
                                                                        .withProvisionedThroughput(new ProvisionedThroughput().withReadCapacityUnits(
                    1L).withWriteCapacityUnits(1L));
        TableDescription createdTableDescription = dynamoDB.createTable(createTableRequest)
                                                           .getTableDescription();
        // Wait for it to become active
        waitForTableToBecomeAvailable(tableName);

        // Describe our new table
        DescribeTableRequest describeTableRequest = new DescribeTableRequest().withTableName(tableName);
        TableDescription tableDescription = dynamoDB.describeTable(describeTableRequest)
                                                    .getTable();
        System.out.println("Table Description: " + tableDescription);

        Map<String, AttributeValue> item = newItem(1, "North America", null,
                null, new String[] { "1", "2" });
        PutItemRequest putItemRequest = new PutItemRequest(tableName, item);
        PutItemResult putItemResult = dynamoDB.putItem(putItemRequest);
        System.out.println("Result: " + putItemResult);

        ScanRequest scanRequest = new ScanRequest(tableName);
        ScanResult scanResult = dynamoDB.scan(scanRequest);
        System.out.println("Scan Result: " + scanResult);
    }

    /**
     * The actual test, will be executed once the Sling instance is started and
     * the setup is complete.
     *
     * @throws Exception
     */
    @Test
    public void testSample() throws Exception {
        System.out.println(getRequestExecutor()
                               .execute(getRequestBuilder()
                                            .buildGetRequest("/content/dynamodb/data/1.json")
                                            .withCredentials("admin", "admin"))
                               .assertStatus(200).getContent());
    }

    /**
     * Wait for table to become available.
     *
     * @param tableName the table name
     */
    private void waitForTableToBecomeAvailable(String tableName) {
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

    private static Map<String, AttributeValue> newItem(Integer id, String name,
        Integer parent, Integer childId, String[] children) {
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

        return item;
    }
}
