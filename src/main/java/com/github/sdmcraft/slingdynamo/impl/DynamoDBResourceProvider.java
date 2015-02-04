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
package com.github.sdmcraft.slingdynamo.impl;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient;
import com.amazonaws.services.dynamodbv2.document.DynamoDB;
import com.amazonaws.services.dynamodbv2.document.Item;
import com.amazonaws.services.dynamodbv2.document.ItemCollection;
import com.amazonaws.services.dynamodbv2.document.ScanFilter;
import com.amazonaws.services.dynamodbv2.document.ScanOutcome;
import com.amazonaws.services.dynamodbv2.document.Table;
import com.amazonaws.services.dynamodbv2.model.DescribeTableRequest;
import com.amazonaws.services.dynamodbv2.model.DescribeTableResult;

import org.apache.sling.api.resource.ModifyingResourceProvider;
import org.apache.sling.api.resource.PersistenceException;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceMetadata;
import org.apache.sling.api.resource.ResourceProvider;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.ValueMap;
import org.apache.sling.api.wrappers.ModifiableValueMapDecorator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;


// TODO: Auto-generated Javadoc
/**
 * The Class DynamoDBResourceProvider.
 */
public class DynamoDBResourceProvider implements ResourceProvider,
    ModifyingResourceProvider {
    /** The Constant LOGGER. */
    private static final Logger LOGGER = LoggerFactory.getLogger(DynamoDBResourceProvider.class);

    /** The dynamo db client. */
    private AmazonDynamoDBClient dynamoDBClient;

    /** The dynamo db. */
    private DynamoDB dynamoDB;

    /** The resource type. */
    private String resourceType;

    /** The root. */
    private String root;

    /**
     * Instantiates a new dynamo db resource provider.
     *
     * @param root the root
     * @param dynamoDBClient the dynamo db client
     * @param dynamoDB the dynamo db
     * @param resourceType the resource type
     */
    public DynamoDBResourceProvider(String root,
        AmazonDynamoDBClient dynamoDBClient, DynamoDB dynamoDB,
        String resourceType) {
        super();
        this.root = root;
        this.dynamoDBClient = dynamoDBClient;
        this.dynamoDB = dynamoDB;
        this.resourceType = resourceType;
    }

    /* (non-Javadoc)
     * @see org.apache.sling.api.resource.ModifyingResourceProvider#commit(org.apache.sling.api.resource.ResourceResolver)
     */
    public void commit(ResourceResolver arg0) throws PersistenceException {
        LOGGER.info(">>>>>>>>Commit");
    }

    /* (non-Javadoc)
     * @see org.apache.sling.api.resource.ModifyingResourceProvider#create(org.apache.sling.api.resource.ResourceResolver, java.lang.String, java.util.Map)
     */
    public Resource create(ResourceResolver arg0, String arg1,
        Map<String, Object> arg2) throws PersistenceException {
        throw new UnsupportedOperationException();
    }

    /* (non-Javadoc)
     * @see org.apache.sling.api.resource.ModifyingResourceProvider#delete(org.apache.sling.api.resource.ResourceResolver, java.lang.String)
     */
    public void delete(ResourceResolver arg0, String arg1)
        throws PersistenceException {
        throw new UnsupportedOperationException();
    }

    /**
     * Returns a resource from this resource provider or null if the resource provider cannot find it.
     * The table-name, id and child-ids are parsed out from the path and queried against dynamodb to fetch the specified resource.
     *
     * @param resolver the ResourceResolver to which the returned Resource is attached.
     * @param req the HttpServletRequest made to get this resource
     * @param path the path of the resource. The path is of the format &lt;table-name&gt;/&lt;id&gt;/[&lt;child-id1&gt;/.../&lt;child-idn&gt;]
     * @return the resource at the specified path if it exists else returns null
     */
    public Resource getResource(ResourceResolver resolver,
        HttpServletRequest req, String path) {
        Resource resource = null;

        try {
            Map<String, Object> resourceProps = new HashMap<String, Object>();
            ResourceMetadata resourceMetadata = new ResourceMetadata();
            resourceMetadata.setResolutionPath(path);

            if (!path.contains(".")) {
                if (path.length() > root.length()) {
                    String subPath = path.substring(root.length() + 1);
                    String[] subPathSplits = subPath.split("/");
                    String table = subPathSplits[0];
                    resourceMetadata.put("table", table);

                    Table dbtable = dynamoDB.getTable(table);

                    if (subPathSplits.length == 1) {
                        DescribeTableRequest describeTableRequest = new DescribeTableRequest(table);
                        DescribeTableResult describeTableResult = null;

                        describeTableResult = dynamoDBClient.describeTable(describeTableRequest);

                        Date creationDate = describeTableResult.getTable()
                                                               .getCreationDateTime();
                        long itemCount = describeTableResult.getTable()
                                                            .getItemCount();

                        resourceProps.put("creation-date", creationDate);
                        resourceProps.put("record-count", itemCount);
                        resourceProps.put("table-name", table);
                    } else if (subPathSplits.length == 2) {
                        int id = Integer.parseInt(subPathSplits[1]);
                        ScanFilter idFilter = new ScanFilter("id").eq(id);
                        ItemCollection<ScanOutcome> items = dbtable.scan(idFilter);
                        Iterator<Item> itemItr = items.iterator();

                        Item item = itemItr.next();
                        resourceProps = itemToMap(item);
                    } else if (subPathSplits.length > 2) {
                        int parent = Integer.parseInt(subPathSplits[1]);
                        Item item = null;

                        for (int i = 2; i < subPathSplits.length; i++) {
                            int child = Integer.parseInt(subPathSplits[i]);
                            ScanFilter parentFilter = new ScanFilter("parent").eq(parent);
                            ScanFilter childFilter = new ScanFilter("child_id").eq(child);
                            ItemCollection<ScanOutcome> items = dbtable.scan(parentFilter,
                                    childFilter);
                            Iterator<Item> itemItr = items.iterator();
                            item = itemItr.next();
                            parent = item.getInt("id");
                        }

                        resourceProps = itemToMap(item);
                    }
                }

                resourceProps.put("hello", "world");

                ModifiableValueMapDecorator valueMap = new ModifiableValueMapDecorator(resourceProps);

                resource = new DynamoDBResource(resolver, resourceMetadata,
                        valueMap, resourceType);
            }
        } catch (Throwable ex) {
            LOGGER.error(ex.getMessage(), ex);
            throw new RuntimeException(ex);
        }

        return resource;
    }

    /* (non-Javadoc)
     * @see org.apache.sling.api.resource.ResourceProvider#getResource(org.apache.sling.api.resource.ResourceResolver, java.lang.String)
     */
    public Resource getResource(ResourceResolver resolver, String path) {
        return getResource(resolver, null, path);
    }

    /* (non-Javadoc)
     * @see org.apache.sling.api.resource.ModifyingResourceProvider#hasChanges(org.apache.sling.api.resource.ResourceResolver)
     */
    public boolean hasChanges(ResourceResolver arg0) {
        LOGGER.info(">>>>>>>>HasChanges");

        return false;
    }

    /**
     *  Fetches the child resources of the specified resource by querying up dynamodb with the child-ids for this resource
     *
     * @param resource the resource whose children are to be fetched
     * @return the iterator over the child resources
     * @see org.apache.sling.api.resource.ResourceProvider#listChildren(org.apache.sling.api.resource.Resource)
     */
    public Iterator<Resource> listChildren(Resource resource) {
        ValueMap parentValueMap = resource.adaptTo(ValueMap.class);

        if ((parentValueMap == null) ||
                !parentValueMap.containsKey("children") ||
                (parentValueMap.get("children") == null)) {
            return null;
        } else {
            Set<BigDecimal> children = parentValueMap.get("children", Set.class);
            Iterator<BigDecimal> childIdItr = children.iterator();

            Object[] childrenIdsArray = new Object[children.size()];
            int i = 0;

            while (childIdItr.hasNext()) {
                childrenIdsArray[i++] = childIdItr.next().intValue();
            }

            String tableS = (String) resource.getResourceMetadata().get("table");
            Table dbtable = dynamoDB.getTable(tableS);

            List<Resource> childrenResourceList = getChildren(dbtable,
                    parentValueMap.get("id", Integer.class), childrenIdsArray,
                    resource.getResourceMetadata().getResolutionPath(),
                    resource.getResourceResolver());

            return childrenResourceList.iterator();
        }
    }

    /* (non-Javadoc)
     * @see org.apache.sling.api.resource.ModifyingResourceProvider#revert(org.apache.sling.api.resource.ResourceResolver)
     */
    public void revert(ResourceResolver arg0) {
        LOGGER.info(">>>>>>>>Revert");
    }

    /**
     * Gets the children.
     *
     * @param dbtable the dbtable
     * @param parent the parent
     * @param childIds the child ids
     * @param path the path
     * @param resolver the resolver
     * @return the children
     */
    private List<Resource> getChildren(Table dbtable, int parent,
        Object[] childIds, String path, ResourceResolver resolver) {
        ScanFilter idFilter = new ScanFilter("parent").in(parent);
        ScanFilter childIdsFilter = new ScanFilter("child_id").in(childIds);
        ItemCollection<ScanOutcome> items = dbtable.scan(idFilter,
                childIdsFilter);
        Iterator<Item> itemItr = items.iterator();
        List<Resource> children = new ArrayList<Resource>();

        while (itemItr.hasNext()) {
            Item item = itemItr.next();
            Iterable<Entry<String, Object>> attributes = item.attributes();
            Iterator<Entry<String, Object>> attributesItr = attributes.iterator();
            Map<String, Object> resourceProps = new HashMap<String, Object>();

            while (attributesItr.hasNext()) {
                Entry<String, Object> attribute = attributesItr.next();
                resourceProps.put(attribute.getKey(), attribute.getValue());
            }

            ModifiableValueMapDecorator valueMap = new ModifiableValueMapDecorator(resourceProps);
            ResourceMetadata resourceMetadata = new ResourceMetadata();
            resourceMetadata.setResolutionPath(path + '/' +
                resourceProps.get("child_id"));
            resourceMetadata.put("table", dbtable.getTableName());

            children.add(new DynamoDBResource(resolver, resourceMetadata,
                    valueMap, resourceType));
        }

        return children;
    }

    /**
     * Item to map.
     *
     * @param item the item
     * @return the map
     */
    private Map<String, Object> itemToMap(Item item) {
        Map<String, Object> resourceProps = new HashMap<String, Object>();
        Iterable<Entry<String, Object>> attributes = item.attributes();
        Iterator<Entry<String, Object>> attributesItr = attributes.iterator();

        while (attributesItr.hasNext()) {
            Entry<String, Object> attribute = attributesItr.next();
            resourceProps.put(attribute.getKey(), attribute.getValue());
        }

        return resourceProps;
    }
}
