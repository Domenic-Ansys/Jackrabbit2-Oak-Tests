package com.test.jackrabbit2;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.InputStream;
import java.util.Hashtable;
import java.util.UUID;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.Property;
import javax.jcr.PropertyIterator;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.Workspace;
import javax.jcr.nodetype.NodeTypeManager;
import javax.jcr.query.QueryResult;

import org.apache.jackrabbit.commons.cnd.CndImporter;

public class JCRTests {
	
	public static long createNodes(String parentNodeName, int number, Session session) throws Exception{
    	Workspace ws = session.getWorkspace();
        NodeTypeManager manager = ws.getNodeTypeManager();
        Node root = session.getRootNode();
    	long start = System.currentTimeMillis();
        Node parent = root.addNode(parentNodeName);
        parent.setProperty("count0", 1);
        parent.setProperty("searchProperty", "blahblah");

        for (int i = 0; i < number; i++) {
           createChild(parent, i);
            }
        session.save();
    	long end = System.currentTimeMillis();
    	
    	return end-start;
	}
	
    private static void createChild(Node parent, int index)
            throws Exception {
        	Node child = parent.addNode("Test" + index);
            child.setProperty("searchProperty", "blahblah");
            child.setProperty("searchProperty2", "blahblah2");
            child.setProperty("searchProperty3", 1);
        	for (int p = 0; p < 10; p++) {
                child.setProperty("count" + p, p);
            }
            
        }
	
	public static long moveNodes(Session session, Node node, String newNodeName) throws Exception{
    	long start = System.currentTimeMillis();
		session.move(node.getPath(), "/"+newNodeName);
        session.save();
    	long end = System.currentTimeMillis();
    	return end-start;
	}
	
	public static long copyNodes(Session session, Node node, String newNodeName) throws Exception{
    	long start = System.currentTimeMillis();
		session.getWorkspace().copy(node.getPath(), "/"+newNodeName);
        session.save();
    	long end = System.currentTimeMillis();
    	return end-start;
	}
	
	public static long deleteNode(Session session, Node node) throws RepositoryException{
    	String nodeName = node.getName();
		long start = System.currentTimeMillis();
		Node root = session.getRootNode();
        Node foo = root.getNode(node.getName());
        foo.remove();
        session.save();
    	long end = System.currentTimeMillis();
    	return end-start;

	}
	
    public static long fileUpload(Session session, String filePath, int fileNumber)
            throws Exception {
    	 	Node root = session.getRootNode();
        	Node folder = root.addNode("FileUploadTest" + "folder"+UUID.randomUUID());
        	InputStream initialStream = new FileInputStream(new File(filePath));
        	long start = System.currentTimeMillis();
        	for(int i=0;i<fileNumber;i++){
	        	Node file = folder.addNode("File"+i, "nt:file");
	        	Node fileContent = file.addNode("jcr:content","nt:resource");
	        	fileContent.setProperty("jcr:data", initialStream);
        		initialStream = new FileInputStream(new File(filePath));
        	}
        	session.save();    
        	long end = System.currentTimeMillis();
        	return end-start;
        }
	public static long update(Session session, String parentName) throws RepositoryException{
		long start = System.currentTimeMillis();
		Node root = session.getRootNode();
		Node parentNodeName = root.getNode(parentName);
		NodeIterator nI = parentNodeName.getNodes();
		int i =0;
		while(nI.hasNext()){
			Node childNode = nI.nextNode();
			if(childNode.hasProperty("searchProperty")){
				++i;
				childNode.getProperty("searchProperty").setValue("blahblah-updated");
			}
		}
		System.out.println(i);
        session.save();
    	long end = System.currentTimeMillis();
    	return end-start;

	}
	
	public static long read(Session session, String parentName) throws RepositoryException{
		long start = System.currentTimeMillis();
		Node root = session.getRootNode();
		Node parentNodeName = root.getNode(parentName);
		NodeIterator nI = parentNodeName.getNodes();
		while(nI.hasNext()){
			Node childNode = nI.nextNode();
			if(childNode.hasProperty("searchProperty")){
				childNode.getProperty("searchProperty").getValue();
			}
		}
    	long end = System.currentTimeMillis();
    	return end-start;

	}
}
