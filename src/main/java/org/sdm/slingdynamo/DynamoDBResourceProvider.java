package org.sdm.slingdynamo;

import java.util.Iterator;

import javax.servlet.http.HttpServletRequest;

import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Properties;
import org.apache.felix.scr.annotations.Property;
import org.apache.felix.scr.annotations.Service;
import org.apache.sling.api.SlingConstants;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceProvider;
import org.apache.sling.api.resource.ResourceResolver;

@Component(name="DBResourceProvider",                                           // (1)
label="DBResourceProvider",
description="Sample DB Resource Provider",getConfigurationFactory=true)
@Service                                                                                                                   // (2)
@Properties({                                                                   // (3)
@Property(name="service.description", value="Sample DB Resource Provider"),
@Property(name="service.vendor", value="lucamasini.net"),
@Property(name=ResourceProvider.ROOTS, value="/content/mynamespace/products"),
@Property(name="jdbc.url", value="jdbc:h2:~/sling-test"),
@Property(name="jdbc.user", value="sa"),
@Property(name="jdbc.pass", value=""),
@Property(name=SlingConstants.PROPERTY_RESOURCE_TYPE, value="/apps/dbprovider/dbprovider.jsp")
})
public class DynamoDBResourceProvider implements ResourceProvider {

	public Resource getResource(ResourceResolver arg0, String arg1) {
		// TODO Auto-generated method stub
		return null;
	}

	@Deprecated
	public Resource getResource(ResourceResolver arg0, HttpServletRequest arg1,
			String arg2) {
		// TODO Auto-generated method stub
		return null;
	}

	public Iterator<Resource> listChildren(Resource arg0) {
		// TODO Auto-generated method stub
		return null;
	}
...
...
...
}