SlingDynamo
===========

SlingDynamo is an Amazon's dynamo DB based Sling resource provider implementation

Table Structure in DynamoDB
===========================
This is a specific table structure which is supported OOTB by this implementation. This table structure is generic enough to store any data in hierarchial structure. The only requirement for this table is to have 3 columns in addition to other application specific columns. 

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
6. Now access 
