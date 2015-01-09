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

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.sling.api.resource.ModifyingResourceProvider;
import org.apache.sling.api.resource.PersistenceException;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceMetadata;
import org.apache.sling.api.resource.ResourceProvider;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.SyntheticResource;
import org.apache.sling.api.wrappers.ValueMapDecorator;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.amazonaws.services.dynamodbv2.model.ComparisonOperator;
import com.amazonaws.services.dynamodbv2.model.Condition;
import com.amazonaws.services.dynamodbv2.model.DescribeTableRequest;
import com.amazonaws.services.dynamodbv2.model.DescribeTableResult;
import com.amazonaws.services.dynamodbv2.model.ScanRequest;
import com.amazonaws.services.dynamodbv2.model.ScanResult;
import com.amazonaws.util.json.JSONException;
import com.amazonaws.util.json.JSONObject;


/**
 * DOCUMENT ME!
 *
 * @author $Satya Deep Maheshwari$
 * @version $Revision: 1.0 $
 */
public class DynamoDBResourceProvider implements ResourceProvider,
    ModifyingResourceProvider {
    private AmazonDynamoDBClient dynamoDB;
    private String resourceType;
    private String root;

    /**
         * Creates a new DynamoDBResourceProvider object.
         *
         * @param root DOCUMENT ME!
         * @param dynamoDB DOCUMENT ME!
         * @param resourceType DOCUMENT ME!
         */
    public DynamoDBResourceProvider(String root, AmazonDynamoDBClient dynamoDB,
        String resourceType) {
        super();
        this.root = root;
        this.dynamoDB = dynamoDB;
        this.resourceType = resourceType;
    }

    /**
     * DOCUMENT ME!
     *
     * @param arg0 DOCUMENT ME!
     *
     * @throws PersistenceException DOCUMENT ME!
     */
    public void commit(ResourceResolver arg0) throws PersistenceException {
        // TODO Auto-generated method stub
    }

    /**
     * DOCUMENT ME!
     *
     * @param arg0 DOCUMENT ME!
     * @param arg1 DOCUMENT ME!
     * @param arg2 DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     *
     * @throws PersistenceException DOCUMENT ME!
     */
    public Resource create(ResourceResolver arg0, String arg1,
        Map<String, Object> arg2) throws PersistenceException {
        // TODO Auto-generated method stub
        return null;
    }

    /**
     * DOCUMENT ME!
     *
     * @param arg0 DOCUMENT ME!
     * @param arg1 DOCUMENT ME!
     *
     * @throws PersistenceException DOCUMENT ME!
     */
    public void delete(ResourceResolver arg0, String arg1)
        throws PersistenceException {
        // TODO Auto-generated method stub
    }

    /**
     * DOCUMENT ME!
     *
     * @param resolver DOCUMENT ME!
     * @param path DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public Resource getResource(ResourceResolver resolver, String path) {
        Resource resource = null;

        if (false && path.startsWith(root) && (path.length() > root.length())) {
            String subPath = path.substring(root.length() + 1);
            String[] subPathSplits = subPath.split("/");
            String table = subPathSplits[0];

            ResourceMetadata resourceMetaData = new ResourceMetadata();
            DescribeTableRequest describeTableRequest = new DescribeTableRequest(table);
            DescribeTableResult describeTableResult = dynamoDB.describeTable(describeTableRequest);
            Date creationDate = describeTableResult.getTable()
                                                   .getCreationDateTime();
            long itemCount = describeTableResult.getTable().getItemCount();
            resourceMetaData.put("creation-date", creationDate);
            resourceMetaData.put("record-count", itemCount);
            resourceMetaData.put("table-name", table);
            resourceMetaData.setResolutionPath(path);

            if ((subPathSplits.length > 1) && subPathSplits[1].contains("=") &&
                    (subPathSplits[1].split("=").length == 2)) {
                if (subPathSplits[1].startsWith("criteria")) {
                    resourceMetaData.put("criteria",
                        subPathSplits[1].split("=")[1]);
                }
            }

            resource = new SyntheticResource(resolver, resourceMetaData, resourceType);
        }

        //return resource;
    	Map<String, Object> map = new HashMap<String, Object>();
    	map.put("hello", "world");
    	ValueMapDecorator valueMap = new ValueMapDecorator(map);

        return new DynamoDBResource(resolver, path,valueMap, resourceType);
    }

    /**
     * DOCUMENT ME!
     *
     * @param arg0 DOCUMENT ME!
     * @param arg1 DOCUMENT ME!
     * @param arg2 DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     *
     * @throws UnsupportedOperationException DOCUMENT ME!
     */
    public Resource getResource(ResourceResolver arg0, HttpServletRequest arg1,
        String arg2) {
        throw new UnsupportedOperationException();
    }

    /**
     * DOCUMENT ME!
     *
     * @param arg0 DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public boolean hasChanges(ResourceResolver arg0) {
        // TODO Auto-generated method stub
        return false;
    }

    /**
     * DOCUMENT ME!
     *
     * @param arg0 DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     *
     * @throws UnsupportedOperationException DOCUMENT ME!
     */
    public Iterator<Resource> listChildren(Resource resource) {
        List<Resource> children = new ArrayList<Resource>();
        ResourceMetadata metaData = resource.getResourceMetadata();
        String table = (String) metaData.get("table-name");
        String criteria = (String) metaData.get("criteria");
        ScanRequest scanRequest = new ScanRequest().withTableName(table);

        if (criteria != null) {
            JSONObject criteriaObj = null;

            try {
                criteriaObj = new JSONObject(criteria);
            } catch (JSONException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }

            Iterator<String> keyItr = criteriaObj.keys();

            while (keyItr.hasNext()) {
                String column = keyItr.next();
                String value = null;

                try {
                    value = criteriaObj.getString(column);
                } catch (JSONException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }

                Condition condition = new Condition().withComparisonOperator(ComparisonOperator.EQ.toString())
                                                     .withAttributeValueList(new AttributeValue().withS(
                            value));
                scanRequest = scanRequest.addScanFilterEntry(column, condition);
            }
        }

        ScanResult scanResult = dynamoDB.scan(scanRequest);

        List<Map<String, AttributeValue>> resultList = scanResult.getItems();

        int rowNum = 0;

        for (Map<String, AttributeValue> result : resultList) {
            JSONObject row = new JSONObject();

            for (Map.Entry<String, AttributeValue> item : result.entrySet()) {
                String attributeName = item.getKey();
                AttributeValue attributeValue = item.getValue();

                try {
                    row.put(attributeName, attributeValue);
                } catch (JSONException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }

            }
            ResourceMetadata resourceMetaData = new ResourceMetadata();
            resourceMetaData.put(Integer.toString(rowNum++), row);
            resourceMetaData.setResolutionPath(resource.getPath() + "/" + rowNum);
            children.add(new SyntheticResource(
                    resource.getResourceResolver(), resourceMetaData,
                    RESOURCE_TYPE_SYNTHETIC));

        }

        return children.iterator();
    }

    /**
     * DOCUMENT ME!
     *
     * @param arg0 DOCUMENT ME!
     */
    public void revert(ResourceResolver arg0) {
        // TODO Auto-generated method stub
    }
}
