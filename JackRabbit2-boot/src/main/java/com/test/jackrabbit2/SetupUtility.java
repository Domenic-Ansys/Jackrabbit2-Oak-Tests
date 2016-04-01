package com.test.jackrabbit2;

import java.util.Hashtable;

import javax.jcr.Repository;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.apache.jackrabbit.commons.JcrUtils;
import org.apache.jackrabbit.core.TransientRepository;
import org.apache.jackrabbit.core.jndi.RegistryHelper;


public class SetupUtility {
	
	private static final String EKM_NAMING_CONTEXT="ekm.naming.context";
	private static final String EKM_NAMING_CONTEXT_DEFAULT="java:jboss";
    private static final String DEFAULT_CONTEXT_FACTORY="org.apache.jackrabbit.core.jndi.provider.DummyInitialContextFactory";
    private static final String DEFAULT_PROVIDER_URL="localhost";

	
	protected static Repository createRepositoryMySql(){
		Context nameCtx=createDummyInitialContext();
		String repName = "Test";
	    String repoConfFilePath = "repository.xml"; 
	    String repoHomePath="ekm-oak\\jr2";
	    
		try {
			RegistryHelper.registerRepository(
					nameCtx,
					repName,
					repoConfFilePath,
					repoHomePath,
					true);
		} catch (NamingException e) {
			throw new IllegalStateException(e);
		} catch (RepositoryException e) {
			throw new IllegalStateException(e);
		}
				
		Repository repository = new TransientRepository(); 
        
        return repository;
	
    }
	
    public static Context createDummyInitialContext() {
        Context initialCtx=null;

        try {
            //create initial naming context                 
            Hashtable<String, String> env = new Hashtable<String, String>();
            env.put(Context.INITIAL_CONTEXT_FACTORY, DEFAULT_CONTEXT_FACTORY);
            env.put(Context.PROVIDER_URL, DEFAULT_PROVIDER_URL);
            initialCtx = new InitialContext(env);
        } catch (NamingException ne) {
            throw new IllegalStateException(ne);
        }
        return initialCtx;
    }
	
		

}
