package com.test.oak;

import java.util.Properties;

import javax.jcr.GuestCredentials;
import javax.jcr.Node;
import javax.jcr.PathNotFoundException;
import javax.jcr.Repository;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.SimpleCredentials;
import javax.jcr.Value;
import javax.jcr.ValueFactory;
import javax.naming.InitialContext;
import javax.naming.NamingException;

public class JCRUtilsOak {
	
	private static  Session adminSession;
	public static Repository getRepository(){
    	return getRepositoryByJNDI("jackrabbit.repository");
    }
    
    
	public static void createNode(String nodeName) throws RepositoryException{
		
		Session session = getRepository().login(getAdminCredentials());
		ValueFactory valueFactory = session.getValueFactory();
        Node root = session.getRootNode();
        Node foo = root.addNode(nodeName);
        foo.setProperty("stringProp", "stringVal");
        foo.setProperty("intProp", 42);
        foo.setProperty("mvProp", new Value[]{
                valueFactory.createValue(1),
                valueFactory.createValue(2),
                valueFactory.createValue(3),
        });
        session.save();
        session.logout();
	}

	public static void deleteNode(String nodeName) throws RepositoryException{
		
		//NodeTypeManager ntMgr = getAdminSession().getWorkspace().getNodeTypeManager();
		Session session = getRepository().login(getAdminCredentials());
        Node root = session.getRootNode();
        Node foo = root.getNode(nodeName);
        foo.remove();
        session.save();
        session.logout();
	}
	
	public static void moveNode(String path, String destPath) throws RepositoryException{
		
		Session session = getRepository().login(getAdminCredentials());
        Node foo = session.getNode("/"+path);
		session.move("/"+path, "/"+destPath);
        session.save();
        session.logout();
	}

	public static void copyNode(String path, String destPath) throws RepositoryException{
		Session session = getRepository().login(getAdminCredentials());
        session.getWorkspace().copy("/"+path, "/"+destPath);
        session.save();
        session.logout();
	}

	
    protected Session createAnonymousSession() throws RepositoryException {
        Session admin = getAdminSession();
        //AccessControlUtils.addAccessControlEntry(admin, "/", EveryonePrincipal.getInstance(), new String[] {Privilege.PRIVILEGE_READ}, true);
        admin.save();
        return getRepository().login(new GuestCredentials());
    }
    

    private static Repository getRepositoryByJNDI(String repositoryName) {

        InitialContext ctx = getInitialContext();
        if (ctx == null) {
            return null;
        }
        try {
            Repository r = (Repository) ctx.lookup(repositoryName);
            return r;
        } catch (NamingException e) {
            e.printStackTrace();
            return null;
        }
    }
    
   
   private static InitialContext getInitialContext() {
	   InitialContext jndiContext = null;
	   Properties jndiEnv = new Properties();
	   jndiEnv.put("jndi.enabled","true");
	   jndiEnv.put("java.naming.provider.url","localhost");
	   jndiEnv.put("java.naming.factory.initial","org.apache.jackrabbit.core.jndi.provider.DummyInitialContextFactory");
	   
       try {
             jndiContext = new InitialContext(jndiEnv);
           } catch (NamingException e) {
        	 e.printStackTrace();
       }
       return jndiContext;
   }

    
    protected static Session getAdminSession() throws RepositoryException {
        //if (adminSession == null || !adminSession.isLive()) {
        adminSession = null; 
    	adminSession = createAdminSession();
        //}
        return adminSession;
    }
    
    protected static Session createAdminSession() throws RepositoryException {
        return getRepository().login(getAdminCredentials());
    }

    protected static SimpleCredentials getAdminCredentials() {
        return new SimpleCredentials("admin", "admin".toCharArray());
    }
    
    protected static Node getNode(String path) throws PathNotFoundException, RepositoryException{
    	return getAdminSession().getNode(path);
    }

}
