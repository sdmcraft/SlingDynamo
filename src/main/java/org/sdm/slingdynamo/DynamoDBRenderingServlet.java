package org.sdm.slingdynamo;

import java.io.IOException;

import javax.servlet.Servlet;

import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Properties;
import org.apache.felix.scr.annotations.Property;
import org.apache.felix.scr.annotations.Service;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;
import org.apache.sling.api.servlets.SlingAllMethodsServlet;

@Component(immediate = true, metatype = true)
@Service(value = Servlet.class)
@Properties({ @Property(name = "sling.servlet.resourceTypes", value = "/apps/dynamodb/render") })
public class DynamoDBRenderingServlet extends SlingAllMethodsServlet {

	@Override
	protected void doGet(SlingHttpServletRequest request, SlingHttpServletResponse response) throws IOException
	{
		response.getWriter().println("Hello from DynamoDBRenderingServlet");
		response.getWriter().close();
	}
}
