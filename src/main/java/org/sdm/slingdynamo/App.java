/*
 * Copyright (c) 2002, Marco Hunsicker. All rights reserved.
 *
 * The contents of this file are subject to the Common Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://jalopy.sf.net/license-cpl.html
 *
 * Copyright (c) 2001-2002 Marco Hunsicker
 */
package org.sdm.slingdynamo;

import com.amazonaws.AmazonClientException;
import com.amazonaws.AmazonServiceException;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient;
import com.amazonaws.services.dynamodbv2.model.AttributeDefinition;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.amazonaws.services.dynamodbv2.model.ComparisonOperator;
import com.amazonaws.services.dynamodbv2.model.Condition;
import com.amazonaws.services.dynamodbv2.model.CreateTableRequest;
import com.amazonaws.services.dynamodbv2.model.DescribeTableRequest;
import com.amazonaws.services.dynamodbv2.model.GetItemRequest;
import com.amazonaws.services.dynamodbv2.model.GetItemResult;
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

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;


/**
 * Hello world!
 */
public class App
{
    static AmazonDynamoDBClient dynamoDB;
    static String accessKey;
    static String secretAccessKey;

    /**
     * DOCUMENT ME!
     */
    public static void init()
    {
        Scanner reader = new Scanner(System.in);
        System.out.println("Enter access key:");

        accessKey = reader.next();
        System.out.println("Enter secret access key:");

        secretAccessKey = reader.next();
        System.out.println("Access Key:" + accessKey);
        System.out.println("Secret access Key:" + secretAccessKey);

        reader.close();

        AWSCredentials awsCredentials = new BasicAWSCredentials(accessKey, secretAccessKey);
        dynamoDB = new AmazonDynamoDBClient(awsCredentials);

        Region usWest2 = Region.getRegion(Regions.US_WEST_2);
        dynamoDB.setRegion(usWest2);
    }

    /**
     * DOCUMENT ME!
     *
     * @param args DOCUMENT ME!
     *
     * @throws Exception DOCUMENT ME!
     */
    public static void main1(String[] args) throws Exception
    {
        init();

        try
        {
            String tableName = "my-favorite-movies-table";

            // Create a table with a primary hash key named 'name', which holds a string
            CreateTableRequest createTableRequest = new CreateTableRequest().withTableName(tableName).withKeySchema(new KeySchemaElement().withAttributeName("name").withKeyType(KeyType.HASH))
                                                                            .withAttributeDefinitions(new AttributeDefinition().withAttributeName("name").withAttributeType(ScalarAttributeType.S))
                                                                            .withProvisionedThroughput(new ProvisionedThroughput().withReadCapacityUnits(1L).withWriteCapacityUnits(1L));
            TableDescription createdTableDescription = dynamoDB.createTable(createTableRequest).getTableDescription();
            System.out.println("Created Table: " + createdTableDescription);

            // Wait for it to become active
            waitForTableToBecomeAvailable(tableName);

            // Describe our new table
            DescribeTableRequest describeTableRequest = new DescribeTableRequest().withTableName(tableName);
            TableDescription tableDescription = dynamoDB.describeTable(describeTableRequest).getTable();
            System.out.println("Table Description: " + tableDescription);

            // Add an item
            Map<String, AttributeValue> item = newItem("Bill & Ted's Excellent Adventure", 1989, "****", "James", "Sara");
            PutItemRequest putItemRequest = new PutItemRequest(tableName, item);
            PutItemResult putItemResult = dynamoDB.putItem(putItemRequest);
            System.out.println("Result: " + putItemResult);

            // Add another item
            item = newItem("Airplane", 1980, "*****", "James", "Billy Bob");
            putItemRequest = new PutItemRequest(tableName, item);
            putItemResult = dynamoDB.putItem(putItemRequest);
            System.out.println("Result: " + putItemResult);

            // Scan items for movies with a year attribute greater than 1985
            HashMap<String, Condition> scanFilter = new HashMap<String, Condition>();
            Condition condition = new Condition().withComparisonOperator(ComparisonOperator.GT.toString()).withAttributeValueList(new AttributeValue().withN("1985"));
            scanFilter.put("year", condition);

            ScanRequest scanRequest = new ScanRequest(tableName).withScanFilter(scanFilter);
            ScanResult scanResult = dynamoDB.scan(scanRequest);
            System.out.println("Result: " + scanResult);
        }
        catch (AmazonServiceException ase)
        {
            System.out.println("Caught an AmazonServiceException, which means your request made it " + "to AWS, but was rejected with an error response for some reason.");
            System.out.println("Error Message:    " + ase.getMessage());
            System.out.println("HTTP Status Code: " + ase.getStatusCode());
            System.out.println("AWS Error Code:   " + ase.getErrorCode());
            System.out.println("Error Type:       " + ase.getErrorType());
            System.out.println("Request ID:       " + ase.getRequestId());
        }
        catch (AmazonClientException ace)
        {
            System.out.println("Caught an AmazonClientException, which means the client encountered " + "a serious internal problem while trying to communicate with AWS, " +
                "such as not being able to access the network.");
            System.out.println("Error Message: " + ace.getMessage());
        }
    }

    private static Map<String, AttributeValue> newItem(String name, int year, String rating, String... fans)
    {
        Map<String, AttributeValue> item = new HashMap<String, AttributeValue>();
        item.put("name", new AttributeValue(name));
        item.put("year", new AttributeValue().withN(Integer.toString(year)));
        item.put("rating", new AttributeValue(rating));
        item.put("fans", new AttributeValue().withSS(fans));

        return item;
    }

    private static void waitForTableToBecomeAvailable(String tableName)
    {
        System.out.println("Waiting for " + tableName + " to become ACTIVE...");

        long startTime = System.currentTimeMillis();
        long endTime = startTime + (10 * 60 * 1000);

        while (System.currentTimeMillis() < endTime)
        {
            try
            {
                Thread.sleep(1000 * 20);
            }
            catch (Exception e)
            {
                ;
            }

            try
            {
                DescribeTableRequest request = new DescribeTableRequest().withTableName(tableName);
                TableDescription tableDescription = dynamoDB.describeTable(request).getTable();
                String tableStatus = tableDescription.getTableStatus();
                System.out.println("  - current state: " + tableStatus);

                if (tableStatus.equals(TableStatus.ACTIVE.toString()))
                {
                    return;
                }
            }
            catch (AmazonServiceException ase)
            {
                if (ase.getErrorCode().equalsIgnoreCase("ResourceNotFoundException") == false)
                {
                    throw ase;
                }
            }
        }

        throw new RuntimeException("Table " + tableName + " never went active");
    }

    public static void main(String[] args)
    {
        init();

        Map<String, AttributeValue> key = new HashMap<String, AttributeValue>();
        key.put("name", new AttributeValue().withS("Airplane"));

        GetItemRequest getItemRequest = new GetItemRequest().withTableName("my-favorite-movies-table").withKey(key).withAttributesToGet(Arrays.asList("name", "fans", "rating", "year"));

        GetItemResult result = dynamoDB.getItem(getItemRequest);

        // Check the response.
        System.out.println("Printing item after retrieving it....");
        printItem(result.getItem());
    }

    private static void printItem(Map<String, AttributeValue> attributeList)
    {
        for (Map.Entry<String, AttributeValue> item : attributeList.entrySet())
        {
            String attributeName = item.getKey();
            AttributeValue value = item.getValue();
            System.out.println(attributeName + " " + ((value.getS() == null) ? "" : ("S=[" + value.getS() + "]")) + ((value.getN() == null) ? "" : ("N=[" + value.getN() + "]")) +
                ((value.getB() == null) ? "" : ("B=[" + value.getB() + "]")) + ((value.getSS() == null) ? "" : ("SS=[" + value.getSS() + "]")) +
                ((value.getNS() == null) ? "" : ("NS=[" + value.getNS() + "]")) + ((value.getBS() == null) ? "" : ("BS=[" + value.getBS() + "] \n")));
        }
    }
}
