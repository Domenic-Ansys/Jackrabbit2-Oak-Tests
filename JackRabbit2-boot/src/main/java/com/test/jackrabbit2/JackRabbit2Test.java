/**
 * Copyright (c) 2015 ANSYS, Inc.
 */
package com.test.jackrabbit2;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.Date;
import java.util.Hashtable;
import java.util.concurrent.TimeUnit;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.PropertyType;
import javax.jcr.Repository;
import javax.jcr.Session;
import javax.jcr.SimpleCredentials;
import javax.jcr.Workspace;
import javax.jcr.nodetype.NodeTypeManager;
import javax.jcr.nodetype.NodeTypeTemplate;
import javax.jcr.nodetype.PropertyDefinitionTemplate;

import org.apache.jackrabbit.commons.cnd.CndImporter;
import org.apache.jackrabbit.oak.Oak;
import org.apache.jackrabbit.oak.jcr.Jcr;
import org.apache.log4j.Level;


/**
 * OakTest TODO
 *
 */
public class JackRabbit2Test {

    static int totalNodes = 0;

    /**
     * TODO
     *
     * @param args
     * @throws RepositoryException 
     * @throws LoginException 
     */
/*    public static void main(String[] args) throws Exception {
        
        try {
        	
        	Repository repoMySql = SetupUtility.createRepositoryMySql();
        	for(int i=0;i<5;i++){
            	StringBuffer mySql = new StringBuffer();
        		mySql = runTests(repoMySql,"MySql","Test"+i);
        		System.out.println(mySql.toString());
        		mySql=null;
        	}
        
        }catch(Exception e){
        	e.printStackTrace();
        }
    }*/
    
    public static void runTests(String nodeNumber, String fileUploads, String runs){
        try {

        	Repository repoMySql = SetupUtility.createRepositoryMySql();
        	if(nodeNumber==null){
        		nodeNumber="100";
        		fileUploads = "10";
        	}
        	if(fileUploads==null){
        		fileUploads = "10";
        	}

        	if(runs==null){
        		runs ="10";
        	}

        	StringBuffer mySql = new StringBuffer();        		
        	StringBuffer results = new StringBuffer();
        	for(int i=0;i<new Integer(runs).intValue();i++){
        		mySql = runTests(repoMySql,"MySql","Test"+i, new Integer(nodeNumber).intValue(),new Integer(fileUploads).intValue());
        		results.append(mySql.toString());
        	}
			File file = new File("results.txt");
			FileWriter fileWriter = new FileWriter(file);
			fileWriter.write(results.toString());
			fileWriter.flush();
			fileWriter.close();
        
        }catch(Exception e){
        	e.printStackTrace();
        }
    }
    public static StringBuffer runTests(Repository repo, String dataSource,String parentName, int nodeNumber,int fileUploads){
    	
        try {
            StringBuffer summary = new StringBuffer();
            summary.append(dataSource+ "***********************************************" +System.getProperty("line.separator"));
            Session session = repo.login(new SimpleCredentials("admin", "admin".toCharArray()));
            
            //String parentName = "Test";
            
            long createValue = JCRTests.createNodes(parentName,nodeNumber, session);
    		summary.append("Created "+nodeNumber+" nodes in "+createValue+"ms" +System.getProperty("line.separator"));
    		summary.append(SearchJCR.performQuery(session)).append(System.getProperty("line.separator"));
    		summary.append(SearchJCR.performQueryJcrContains(session)).append(System.getProperty("line.separator"));;
    		summary.append(SearchJCR.performQueryAnd(session)).append(System.getProperty("line.separator"));;
    		summary.append(SearchJCR.performQueryContainsInt(session)).append(System.getProperty("line.separator"));;
    		summary.append(SearchJCR.performQueryGreaterThan0(session)).append(System.getProperty("line.separator"));;
    		summary.append(SearchJCR.performQueryOr(session)).append(System.getProperty("line.separator"));;
    		summary.append(SearchJCR.performQueryOrSub(session)).append(System.getProperty("line.separator"));;
    		
    		long updateTime = JCRTests.update(session, parentName);
    		summary.append("updated "+nodeNumber+" nodes in "+updateTime+"ms" +System.getProperty("line.separator"));

    		long readTime = JCRTests.read(session, parentName);
    		summary.append("read "+nodeNumber+" nodes in "+readTime+"ms" +System.getProperty("line.separator"));
    		
            long uploadTime = JCRTests.fileUpload(session,  "sample.txt", fileUploads);
            summary.append("Uploaded "+fileUploads+" files in "+uploadTime+"ms" +System.getProperty("line.separator"));		
    		
            long moveTime = JCRTests.moveNodes(session, session.getRootNode().getNode(parentName), "MovedNode");
            summary.append("Moved "+nodeNumber+" nodes in "+moveTime+"ms" +System.getProperty("line.separator"));
            
            long copyTime = JCRTests.copyNodes(session, session.getRootNode().getNode("MovedNode"), "CopiedNode");
            summary.append("Copied "+nodeNumber+" nodes in "+copyTime+"ms" +System.getProperty("line.separator"));
            
            
            Node root = session.getRootNode();
            NodeIterator nI = root.getNodes();
            while(nI.hasNext()){
            	Node node = nI.nextNode();
            	if(node.getPath().equals("/CopiedNode")){
            		long deleteTime = JCRTests.deleteNode(session, node);
            		summary.append("Deleted "+nodeNumber+" nodes in "+deleteTime+"ms" +System.getProperty("line.separator"));
            		break;
            	}
            }
            nI = root.getNodes();
            while(nI.hasNext()){
            	Node node = nI.nextNode();
            	if(node.getPath().equals("/MovedNode")){
            		long deleteTime = JCRTests.deleteNode(session, node);
            		summary.append("Deleted "+nodeNumber+" nodes in "+deleteTime+"ms" +System.getProperty("line.separator"));
            		break;
            	}
            }
            
            session.logout();
            return summary;
        }catch(Exception e){
        	e.printStackTrace();
        	return new StringBuffer().append("NADA");
        }
    }
    	
}


