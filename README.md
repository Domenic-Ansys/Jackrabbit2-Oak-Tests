# Jackrabbit2-Oak-Tests
The following application will run the following Jackrabbit 2 and Oak 1.2.7 tests:

• create nodes 
• move nodes 
• copy nodes 
• delete nodes 
• upload files 
• read 1 property for x number of nodes 
• update properties x number of nodes

I have also attached a spreadsheet with the results: results-oak-jackrabbit2.xlsx

The Jackrabbit2 and Oak application are can be packaged using:

mvn package

FYI - These are spring boot applications.
*************************************************************
Jackrabbit 2 directions:

You might need to do some configuration in the repository.xml. If you install a mysql database locally - "//localhost:3307/test" you really do not have to do nothing.

java -jar number of nodes to create, number of files to upload, number of runs

example: java -jar 10000 100 1

FYI sample.txt is the file that gets uploaded.

*************************************************************
Oak directions:

In order to run the tests in Oak there is some configuration that needs to be done.  In the oak.properties file:

Number of runs you want
run.number=10
numbe rof times you want the sample.txt to be uploaded
fileupload.number=100
number of nodes you want to create
node.number=10000
3 persistence manager options (mysql, mongo, or postgress).  All use a filestore relative to the directory you run in
mysql.run=true
mongo.run=true
postgres.run=true

Start it by the following:
java -Xms1024m -Xmx2048m -Doak.queryLimitInMemory=500000 -Doak.queryLimitReads=100000 -Dupdate.limit=250000 -Doak.fastQuerySize=true -jar Oakboot-0.0.1-SNAPSHOT.jar

A lot of todos but its not really a production type app that may or maynot evolve, just something that allows us to gauge the performance of Jackrabbit2 and Oak

