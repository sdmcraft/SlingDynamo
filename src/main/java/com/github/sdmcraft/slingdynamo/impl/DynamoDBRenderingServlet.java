package com.github.sdmcraft.slingdynamo.impl;

import java.io.IOException;
import java.util.Iterator;
import java.util.Map.Entry;

import javax.servlet.Servlet;

import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Properties;
import org.apache.felix.scr.annotations.Property;
import org.apache.felix.scr.annotations.Service;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.servlets.SlingAllMethodsServlet;

import com.amazonaws.util.json.JSONArray;
import com.amazonaws.util.json.JSONException;
import com.amazonaws.util.json.JSONObject;


// TODO: Auto-generated Javadoc
/**
 * The Class DynamoDBRenderingServlet.
 */
@Component(immediate = true, metatype = true)
@Service(value = Servlet.class)
@Properties({@Property(name = "sling.servlet.resourceTypes",value = "dynamodb/render"),
	@Property(name = "endpoint",value = "xxx")
})
public class DynamoDBRenderingServlet extends SlingAllMethodsServlet {
    /* (non-Javadoc)
     * @see org.apache.sling.api.servlets.SlingSafeMethodsServlet#doGet(org.apache.sling.api.SlingHttpServletRequest, org.apache.sling.api.SlingHttpServletResponse)
     */
    @Override
    protected void doGet(SlingHttpServletRequest request,
        SlingHttpServletResponse response) throws IOException {
        Resource resource = request.getResource();

        JSONObject resourceObj = new JSONObject();

        try {
            resourceObj.put("children", getChildren(resource));
            //resourceObj.put("metadata", getResourceMetaData(resource));
            response.getWriter().println(resourceObj.toString(2));
        } catch (JSONException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        response.getWriter().close();
    }

    /**
     * Gets the resource meta data.
     *
     * @param resource the resource
     * @return the resource meta data
     */
    private JSONObject getResourceMetaData(Resource resource) {
        JSONObject resourceMetaDataObj = new JSONObject();

        for (Entry<String, Object> entry : resource.getResourceMetadata()
                                                   .entrySet()) {
            try {
                resourceMetaDataObj.put(entry.getKey(), entry.getValue());
            } catch (JSONException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }

        return resourceMetaDataObj;
    }

    /**
     * Gets the children.
     *
     * @param resource the resource
     * @return the children
     */
    private JSONArray getChildren(Resource resource) {
        JSONArray children = new JSONArray();

        Iterator<Resource> childItr = resource.listChildren();

        while (childItr.hasNext()) {
            JSONObject childObj = new JSONObject();

            try {
                childObj.put("metadata", getResourceMetaData(childItr.next()));
            } catch (JSONException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }

            children.put(childObj);
        }

        return children;
    }
}
