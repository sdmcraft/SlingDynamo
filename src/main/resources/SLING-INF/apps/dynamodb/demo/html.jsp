<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="sling" uri="http://sling.apache.org/taglibs/sling"%>
<sling:defineObjects />
<c:set var="content" value="${sling:getResource(resourceResolver,'/content/dynamodb/data')}" />
<c:out value="${content}"></c:out>

Hello World!!
