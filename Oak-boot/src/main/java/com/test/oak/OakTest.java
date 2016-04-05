/**
 * Copyright (c) 2015 ANSYS, Inc.
 */
package com.test.oak;

import java.io.File;
import java.io.FileWriter;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.Repository;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.SimpleCredentials;

/**
 * OakTest TODO
 *
 */
public class OakTest {
	
	private static Session session=null;

    /**
     * TODO
     *
     * @param args
     * @throws RepositoryException 
     * @throws LoginException 
     */
    public static void executeTests() {
        
        try {
        	String nodeNumber=PropertyFileBundle.getValue("node.number");
        	String fileUploads=PropertyFileBundle.getValue("fileupload.number");
        	int runs=new Integer(PropertyFileBundle.getValue("run.number")).intValue();
        	
        	boolean isMySql=new Boolean(PropertyFileBundle.getValue("mysql.run")).booleanValue();
        	boolean isPostgres=new Boolean(PropertyFileBundle.getValue("postgres.run")).booleanValue();
        	boolean isMongo=new Boolean(PropertyFileBundle.getValue("mongo.run")).booleanValue();
        	
          	StringBuffer postGress = new StringBuffer();
        	StringBuffer mySql = new StringBuffer();
        	StringBuffer mongo = new StringBuffer();
        	StringBuffer results = new StringBuffer();
   
        	if(isPostgres){
       			Repository repoPostGress = SetupUtility.createRepositoryPostGress();
        		for(int i=0;i<runs;i++){
        			postGress = runTests(repoPostGress,"PostGress",nodeNumber,fileUploads,0);
        			results.append(postGress.toString());
        		}
				File postgresFile = new File("postgres-results.txt");
				FileWriter postgresFileWriter = new FileWriter(postgresFile);
				postgresFileWriter.write(results.toString());
				postgresFileWriter.flush();
				postgresFileWriter.close();
        	}
        	results.setLength(0);
        	if(isMySql){
        		Repository repoMySql = SetupUtility.createRepositoryMySql();
            		for(int i=0;i<runs;i++){
        			mySql = runTests(repoMySql,"MySql",nodeNumber,fileUploads,i);
        			results.append(mySql.toString());
        		}
    				File mySqlFile = new File("mySql-results.txt");
    				FileWriter mySqlFileWriter = new FileWriter(mySqlFile);
    				mySqlFileWriter.write(results.toString());
    				mySqlFileWriter.flush();
    				mySqlFileWriter.close();
        	}
        	results.setLength(0);
        	if(isMongo){
       			Repository repoMongo = SetupUtility.createRepositoryMongo();
        		for(int i=0;i<runs;i++){
        			mongo = runTests(repoMongo,"Mongo",nodeNumber,fileUploads,i);
        			results.append(mongo.toString());
        		}
				File mongoFile = new File("mongo-results.txt");
				FileWriter mongoFileWriter = new FileWriter(mongoFile);
				mongoFileWriter.write(results.toString());
				mongoFileWriter.flush();
				mongoFileWriter.close();
        	}
        
        }catch(Exception e){
        	e.printStackTrace();
        }
    }
    public static StringBuffer runTests(Repository repo, String dataSource,String nodeNumber,String fileUploads,int i){
    	
        try {
            StringBuffer summary = new StringBuffer();
            summary.append(dataSource+ "***********************************************" +System.getProperty("line.separator"));
            if(session==null)
            	session = repo.login(new SimpleCredentials("admin", "admin".toCharArray()));
            SetupUtility.createLuceneIndex(repo);
            String run = new Integer(i).toString();
            String parentName = "Test"+run;
        	if(nodeNumber==null){
        		nodeNumber="100";
        		fileUploads = "10";
        	}
        	if(fileUploads==null){
        		fileUploads = "10";
        	}
            
            long createValue = JCRTests.createNodes(parentName,new Integer(nodeNumber).intValue(), session);
    		summary.append("Created "+nodeNumber+" nodes in "+createValue+"ms" +System.getProperty("line.separator"));
    		summary.append(LuceneIndexJCR.performQuery(session)).append(System.getProperty("line.separator"));
    		////summary.append(LuceneIndexJCR.performQueryJcrContains(session)).append(System.getProperty("line.separator"));;
    		//summary.append(LuceneIndexJCR.performQueryAnd(session)).append(System.getProperty("line.separator"));;
    		//summary.append(LuceneIndexJCR.performQueryContainsInt(session)).append(System.getProperty("line.separator"));;
    		//summary.append(LuceneIndexJCR.performQueryGreaterThan0(session)).append(System.getProperty("line.separator"));;
    		//summary.append(LuceneIndexJCR.performQueryOr(session)).append(System.getProperty("line.separator"));;
    		//summary.append(LuceneIndexJCR.performQueryOrSub(session)).append(System.getProperty("line.separator"));;
      		long readTime = JCRTests.read(session, parentName);
    		summary.append("read "+nodeNumber+" nodes in "+readTime+"ms" +System.getProperty("line.separator"));
            long uploadTime = JCRTests.fileUpload(session,  "sample.txt", new Integer(fileUploads).intValue());
            summary.append("Uploaded "+fileUploads+" files in "+uploadTime+"ms" +System.getProperty("line.separator"));		

    		long updateTime = JCRTests.update(session, parentName);
    		summary.append("updated "+nodeNumber+" nodes in "+updateTime+"ms" +System.getProperty("line.separator"));
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
            nI = root.getNodes();
            while(nI.hasNext()){
            	Node node = nI.nextNode();
            	if(node.getPath().equals("/"+parentName)){
            		long deleteTime = JCRTests.deleteNode(session, node);
            		summary.append("Deleted "+nodeNumber+" nodes in "+deleteTime+"ms" +System.getProperty("line.separator"));
            		break;
            	}
            }

            session.refresh(true);
            session.logout();

            session=null;
            return summary;
        }catch(Exception e){
        	e.printStackTrace();
        	return new StringBuffer().append("NADA");
        }
    }
    	
}


