package com.test.jackrabbit2;

import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.concurrent.TimeUnit;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.Repository;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.SimpleCredentials;
import javax.jcr.query.Query;
import javax.jcr.query.QueryManager;
import javax.jcr.query.QueryResult;
import javax.jcr.query.Row;
import javax.jcr.query.RowIterator;

import org.apache.jackrabbit.commons.JcrUtils;
import org.apache.jackrabbit.oak.plugins.index.IndexConstants;
import org.apache.jackrabbit.oak.plugins.segment.SegmentNodeStore;
import org.apache.jackrabbit.oak.plugins.segment.file.FileStore;
import org.apache.jackrabbit.oak.spi.commit.Observer;
import org.apache.jackrabbit.oak.spi.query.QueryIndexProvider;
import org.apache.jackrabbit.oak.spi.state.NodeBuilder;
import org.apache.jackrabbit.oak.spi.state.NodeStore;
import org.apache.jackrabbit.oak.api.Type;
import org.apache.jackrabbit.oak.jcr.query.RowImpl;

public class SearchJCR {
	
	 private Repository repository;
	 

	public static String performQuery(Session session) throws RepositoryException, InterruptedException {
		QueryManager qm = session.getWorkspace().getQueryManager();
		final Query q = qm.createQuery("//*[(@searchProperty = 'blahblah')]", Query.XPATH);
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
