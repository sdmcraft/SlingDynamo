SlingDynamo
===========

SlingDynamo is an Amazon's dynamo DB based Sling resource provider implementation

Table Structure in DynamoDB
===========================
This is a specific table structure which is supported OOTB by this implementation. This table structure is generic enough to store any data in hierarchial structure. The only requirement for this table is to have 4 columns in addition to other application specific columns. 
1. id (Number) - The unique identifier for a row
2. child_id (Number) - Unique identifier of a child row in context of its parent row
3. parent (Number) - id of the parent of this child row
4. children (Number Set)- A number set if the child_ids of this parent row
Here's a sample table structure:

| id | name          | child_id (Unique id in context of parent row) | children (child_ids of children rows) | parent (id of parent row) |
|----|---------------|-----------------------------------------------|---------------------------------------|---------------------------|
| 1  | North America |                                               | {1}                                   |                           |
| 2  | South America |                                               | {1}                                   |                           |
| 3  | USA           | 1                                             | {1,2}                                 | 1                         |
| 4  | Brazil        | 1                                             | {1}                                   | 2                         |
| 5  | California    | 1                                             | {1}                                   | 3                         |
| 6  | San Francisco | 1                                             |                                       | 5                         |
| 7  | Texas         | 2                                             |                                       | 3                         |

If this table structure does not suit your requirements, you can always modify the DynamoDBResourceProvider to suit your requirements.

Usage Instructions
==================
1. Clone https://github.com/satyadeep1980/SlingDynamo.git
2. cd SlingDynamo
3. mvn clean install
4. Open up http://localhost:8080/system/console/configMgr
5. Configure DynamoDBResourceProviderFactory as follows
  1. Provide your aws access key in aws.access.key.name
  2. Provide your aws access secret in aws.secret.access.key.name
  3. Provide your aws region in aws.region.name. For e.g. 'us-west-2'
  4. Provide the root path under which you would want to  access your dynamo DB resources. For e.g. /content/dynamodb
6. Now access your dynamodb resource as follows: 
  http://localhost:8080/content/dynamodb/<table_name>/<id>.json
  http://localhost:8080/content/dynamodb/<table_name>/<id>.json
7. 
