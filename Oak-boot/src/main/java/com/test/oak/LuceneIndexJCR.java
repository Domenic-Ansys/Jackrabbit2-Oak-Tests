package com.test.oak;

import javax.jcr.Node;
import javax.jcr.Repository;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.query.Query;
import javax.jcr.query.QueryManager;
import javax.jcr.query.QueryResult;
import javax.jcr.query.Row;
import javax.jcr.query.RowIterator;

import org.apache.jackrabbit.oak.plugins.index.IndexConstants;

public class LuceneIndexJCR {
	
	 private Repository repository;



	    public static void removeTestData(String name) throws RepositoryException {
	        Session session = JCRUtilsOak.getAdminSession();
	        
	        Node node = session.getNode(name);
	        node.remove();
        	long start = System.currentTimeMillis();
	        session.save();
        	long end = System.currentTimeMillis();
        	System.out.println("Took : " + ((end - start)));
	        session.logout();
	
	        System.out.println("RemovedNode");
	    }
	    
		   public void createLuceneIndex() throws RepositoryException {
		        Session session = JCRUtilsOak.getAdminSession();
		        session.getNodeByIdentifier("/oak:index/counter").setProperty(IndexConstants.REINDEX_PROPERTY_NAME,
		        		true);
		        		session.save();   
		      //  session.getRootNode().getNode("oak:index").getNode("lucene").remove();
		        Node oakIndex = session.getRootNode().getNode("oak:index"); 

	/*	        		JcrUtils.getOrCreateByPath("/oak:index/lucene1", "oak:Unstructured",
		                "oak:QueryIndexDefinition"
		                + "", session, false);*/
		        
		        Node lucene = oakIndex.addNode("EKMAllProperties");
		        lucene.setPrimaryType("oak:QueryIndexDefinition");
		        lucene.setProperty("compatVersion", 2);
		        lucene.setProperty("type", "lucene");
		        lucene.setProperty("fulltextEnabled", "false");
		        lucene.setProperty("evaluatePathRestrictions", "false");
		        lucene.setProperty("excludedPaths", "/test2");
		        lucene.setProperty("async", "async");

		        
		        Node rules = null;
	        	rules = lucene.addNode("indexRules", "nt:unstructured");
	        	
	        	Node nTBase = null;
	        	nTBase= rules.addNode("nt:base");
	        	
	        	Node props = null;
	        	props = nTBase.addNode("properties", "nt:unstructured");
	        	props = nTBase.getNode("properties");
	        	
	        	Node allProps = props.addNode("allProps");
	        	//allProps.setProperty("name", ".*");
	        	allProps.setProperty("isRegexp", true);
	        	allProps.setProperty("name", "^(?!restriction_test|restriction_test1)\\w*");
	        	allProps.setProperty("propertyIndex", true);
	        	//allProps.setProperty("nodeScopeIndex", true);

	        	session.save();
		        session.logout();
		        System.out.println("Lucene index created");
		    }
		   public static void createLucenePropertyIndex(String indexName) throws RepositoryException {
		        Session session = JCRUtilsOak.getAdminSession();
		        session.getNodeByIdentifier("/oak:index/counter").setProperty(IndexConstants.REINDEX_PROPERTY_NAME,
		        		true);
		        		session.save();   
		      //  session.getRootNode().getNode("oak:index").getNode("lucene").remove();
		        Node oakIndex = session.getRootNode().getNode("oak:index"); 

		        
		        Node lucene = oakIndex.addNode(indexName);
		        lucene.setPrimaryType("oak:QueryIndexDefinition");
		        lucene.setProperty("compatVersion", 2);
		        lucene.setProperty("type", "lucene");
		        lucene.setProperty("includePropertyTypes" ,true);
		        lucene.setProperty("async", "async");
		        Node rules = null;
	        	rules = lucene.addNode("indexRules", "nt:unstructured");
	        	
	        	Node nTBase = null;
	        	nTBase= rules.addNode("nt:base");
	        	
	        	Node props = null;
	        	props = nTBase.addNode("properties", "nt:unstructured");
	        	
	        	Node allProps = props.addNode(indexName);
	        	allProps.setProperty("name", indexName);
	        	allProps.setProperty("propertyIndex", true);
	        	//allProps.setProperty("nodeScopeIndex", true);

	        	
	        	session.save();
		        session.logout();
		        System.out.println("Lucene index created");
		    }		   

	public static String performQuery(Session session) throws RepositoryException, InterruptedException {
		QueryManager qm = session.getWorkspace().getQueryManager();
		final Query q = qm.createQuery("//*[(@searchProperty = 'blahblah')]", Query.XPATH);
		StringBuffer sB = new StringBuffer();
		int counter = 0;
		try {
			long start = System.currentTimeMillis();
			QueryResult queryResult = q.execute();
			long end = System.currentTimeMillis();
			System.out.println("Took : " + ((end - start)));
			
			for (RowIterator iter = queryResult.getRows(); iter.hasNext();) {
				Row row = iter.nextRow();
				++counter;
			}
			return sB.append("Queryed properties: time: ").append(end-start).append("  nodes ").append(counter).toString();
		} catch (Exception e) {
			e.printStackTrace();
			return "";
		}
	}


	public static String performQueryAnd(Session session) throws RepositoryException, InterruptedException {
		QueryManager qm = session.getWorkspace().getQueryManager();
		final Query q = qm.createQuery("/jcr:root/Test//*[(@searchProperty = 'blahblah') and (@searchProperty2 = 'blahblah2')]" , Query.XPATH);
		StringBuffer sB = new StringBuffer();
		int counter = 0;
		try {
			long start = System.currentTimeMillis();
			QueryResult queryResult = q.execute();
			long end = System.currentTimeMillis();
			
			for (RowIterator iter = queryResult.getRows(); iter.hasNext();) {
				Row row = iter.nextRow();
				++counter;
			}
			return sB.append("Queryed (And) properties: time: ").append(end-start).append("  nodes ").append(counter).toString();
		} catch (Exception e) {
			e.printStackTrace();
			return "";
		}
	}

	public static String performQueryOrSub(Session session) throws RepositoryException, InterruptedException {
		QueryManager qm = session.getWorkspace().getQueryManager();
		final Query q = qm.createQuery("//*[(@searchProperty = 'blahblah')]", Query.XPATH);
		StringBuffer sB = new StringBuffer();
		int counter = 0;
		try {
			long start = System.currentTimeMillis();
			QueryResult queryResult = q.execute();
			long end = System.currentTimeMillis();
			System.out.println("Took : " + ((end - start)));
			
			for (RowIterator iter = queryResult.getRows(); iter.hasNext();) {
				Row row = iter.nextRow();
				++counter;
			}
			return sB.append("Queryed (OrSub) properties: time: ").append(end-start).append("  nodes ").append(counter).toString();
		} catch (Exception e) {
			e.printStackTrace();
			return "";
		}
	}

	public static String performQueryOr(Session session) throws RepositoryException, InterruptedException {
		QueryManager qm = session.getWorkspace().getQueryManager();
		final Query q = qm.createQuery("//*[(@searchProperty = 'blahblah') or (@searchProperty2 = 'blahblah2')]" , Query.XPATH);
		StringBuffer sB = new StringBuffer();
		int counter = 0;
		try {
			long start = System.currentTimeMillis();
			QueryResult queryResult = q.execute();
			long end = System.currentTimeMillis();
			System.out.println("Took : " + ((end - start)));
			
			for (RowIterator iter = queryResult.getRows(); iter.hasNext();) {
				Row row = iter.nextRow();
				++counter;
			}
			return sB.append("Query (Or) properties: time: ").append(end-start).append("  nodes ").append(counter).toString();
		} catch (Exception e) {
			e.printStackTrace();
			return "";
		}
	}


	public static String performQueryJcrContains(Session session) throws RepositoryException, InterruptedException {
		QueryManager qm = session.getWorkspace().getQueryManager();
		session.refresh(true);
		final Query q = qm.createQuery("//*[jcr:contains(., 'blahblah')]" , Query.XPATH);
		StringBuffer sB = new StringBuffer();
		int counter = 0;
		try {
			long start = System.currentTimeMillis();
			QueryResult queryResult = q.execute();
			long end = System.currentTimeMillis();
			
			for (RowIterator iter = queryResult.getRows(); iter.hasNext();) {
				Row row = iter.nextRow();
				++counter;
			}
			return sB.append("Queryed (Contains) properties: time: ").append(end-start).append("  nodes ").append(counter).toString();
		} catch (Exception e) {
			e.printStackTrace();
			return "";
		}
	}
	
    

	public static String performQueryContainsInt(Session session) throws RepositoryException, InterruptedException {
		QueryManager qm = session.getWorkspace().getQueryManager();
	    final Query q = qm.createQuery("//*[jcr:contains(., '1')]" , Query.XPATH);
		StringBuffer sB = new StringBuffer();
		int counter = 0;
		try {
			long start = System.currentTimeMillis();
			QueryResult queryResult = q.execute();
			long end = System.currentTimeMillis();
			
			for (RowIterator iter = queryResult.getRows(); iter.hasNext();) {
				Row row = iter.nextRow();
				++counter;
			}
			return sB.append("Queryed (ContainsInt) properties: time: ").append(end-start).append("  nodes ").append(counter).toString();
		} catch (Exception e) {
			e.printStackTrace();
			return "";
		}
	}

	public static String performQueryGreaterThan0(Session session) throws RepositoryException, InterruptedException {
		QueryManager qm = session.getWorkspace().getQueryManager();
		final Query q = qm.createQuery("//*[(@searchProperty3 > 0)]" , Query.XPATH);
		StringBuffer sB = new StringBuffer();
		int counter = 0;
		try {
			long start = System.currentTimeMillis();
			QueryResult queryResult = q.execute();
			long end = System.currentTimeMillis();
			
			for (RowIterator iter = queryResult.getRows(); iter.hasNext();) {
				Row row = iter.nextRow();
				++counter;
			}
			return sB.append("Queryed (performQueryGreaterThan0) properties: time: ").append(end-start).append("  nodes ").append(counter).toString();
		} catch (Exception e) {
			e.printStackTrace();
			return "";
		}
	}
	
}
