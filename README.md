# Jackrabbit2-Oak-Tests
The following application will run the following Jackrabbit tests:

• create nodes 
• move nodes 
• copy nodes 
• delete nodes 
• upload files 
• read 1 property for x number of nodes 
• update properties x number of nodes

This is a spring boot application which can be compiled using:

mvn package

To actually run the application you need to do some configuration in the repository.xml. If you install a mysql database locally - "//localhost:3307/test" you really do not have to do nothing.

To run the application:

java -jar number of nodes to create, number of files to upload, number of runs

example: java -jar 10000 100 1

FYI sample.txt is the file that gets uploaded.

A lot of todos but its not really a production type app, just something that allows us to gauge the performance of Jackrabbit2

I have also included results for creation of 1000 and 100000 nodes in results1000.txt and results100000.txt.

Oak directions to come....
