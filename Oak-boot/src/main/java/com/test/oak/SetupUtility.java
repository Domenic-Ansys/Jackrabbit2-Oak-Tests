package com.test.oak;

import java.io.File;
import java.io.IOException;
import java.net.UnknownHostException;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.UUID;

import javax.annotation.Nullable;
import javax.jcr.LoginException;
import javax.jcr.Node;
import javax.jcr.Repository;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.SimpleCredentials;

import org.apache.jackrabbit.core.data.DataIdentifier;
import org.apache.jackrabbit.core.data.FileDataStore;
import org.apache.jackrabbit.oak.Oak;
import org.apache.jackrabbit.oak.jcr.Jcr;
import org.apache.jackrabbit.oak.jcr.repository.RepositoryImpl;
import org.apache.jackrabbit.oak.plugins.blob.datastore.DataStoreBlobStore;
import org.apache.jackrabbit.oak.plugins.blob.datastore.OakFileDataStore;
import org.apache.jackrabbit.oak.plugins.document.DocumentMK;
import org.apache.jackrabbit.oak.plugins.document.DocumentNodeStore;
import org.apache.jackrabbit.oak.plugins.document.rdb.RDBDataSourceFactory;
import org.apache.jackrabbit.oak.plugins.document.rdb.RDBOptions;
import org.apache.jackrabbit.oak.plugins.index.lucene.LuceneIndexEditorProvider;
import org.apache.jackrabbit.oak.plugins.segment.file.FileStore;
import org.apache.jackrabbit.oak.query.QueryEngineSettings;
import org.apache.jackrabbit.oak.security.SecurityProviderImpl;
import org.apache.jackrabbit.oak.security.authorization.AuthorizationConfigurationImpl;
import org.apache.jackrabbit.oak.spi.security.ConfigurationParameters;
import org.apache.jackrabbit.oak.spi.security.SecurityProvider;
import org.apache.jackrabbit.oak.spi.security.authorization.AuthorizationConfiguration;
import org.apache.jackrabbit.oak.spi.security.authorization.permission.PermissionConstants;

import com.google.common.base.Function;
import com.google.common.collect.Iterators;
import com.google.common.collect.Sets;
import com.mongodb.DB;
import com.mongodb.Mongo;
import com.mongodb.ReadPreference;
import com.mongodb.ServerAddress;
import com.mongodb.WriteConcern;

public class SetupUtility {
	
    protected static final String URL = System.getProperty("rdb.jdbc-url", "jdbc:h2:file:./{fname}test;DB_CLOSE_ON_EXIT=FALSE");

    protected static final String USERNAME = System.getProperty("rdb.jdbc-user", "sa");

    protected static final String PASSWD = System.getProperty("rdb.jdbc-passwd", "");
    
	private static FileStore store=null;

	private static FileDataStore fds=null;
	
	static DocumentNodeStore storeD=null;
	
    public static DocumentNodeStore getStoreD() {
		return storeD;
	}

	public static void setStoreD(DocumentNodeStore storeDin) {
		storeD = storeDin;
	}

	protected static Repository createRepositoryMongo() throws UnknownHostException{
		
    
    	ServerAddress serverAddress = new ServerAddress(PropertyFileBundle.getValue("mongo.connection.url"), new Integer(PropertyFileBundle.getValue("mongo.connection.port")).intValue());
    	
    	 Mongo mongo = new Mongo(serverAddress);
         mongo.setWriteConcern(WriteConcern.FSYNCED);
         mongo.setReadPreference(ReadPreference.primary());
     //    mongo = new Mongo(serverAddress);
     //    mongo.setWriteConcern(WriteConcern.JOURNAL_SAFE);
    
     //    mongo = new Mongo(serverAddress);
    //     mongo.setWriteConcern(WriteConcern.NORMAL);
    
    //     mongo = new Mongo(serverAddress);
    //     mongo.setWriteConcern(WriteConcern.SAFE);
         
     	DB db = mongo.getDB(PropertyFileBundle.getValue("mongo.db.name"));

		initFileStore("Mongo");

		DocumentNodeStore storeD = new DocumentMK.Builder().setPersistentCache("Mongo,size=1024,binary=0").setMongoDB(db).setBlobStore(new DataStoreBlobStore(fds)).getNodeStore();
		//DocumentNodeStore storeD = new DocumentMK.Builder().setMongoDB(db).getNodeStore();
		

        Repository repository = new Jcr(new Oak(storeD)).with(new LuceneIndexEditorProvider()).with(configureSearch()).createRepository();
        setStoreD(storeD);
        return repository;
	
    }
	
	protected static Repository createRepositoryMySql(){
		
	/*
	 * Properties to ecternalize
	 * 	Datastore directory
	 * 	Which database and database info
	 */
        //Mysql
		String url = PropertyFileBundle.getValue("mysql.connection.url");
		String driver = "org.mysql.jdbc";
		String userName = PropertyFileBundle.getValue("mysql.username");
		String password = PropertyFileBundle.getValue("mysql.password");

		initFileStore("MySql");

		String prefix = "T" + UUID.randomUUID().toString().replace("-", "");
		RDBOptions options = new RDBOptions().tablePrefix(prefix).dropTablesOnClose(false);
        DocumentNodeStore storeD = new DocumentMK.Builder().setBlobStore(new DataStoreBlobStore(fds)).setClusterId(1).memoryCacheSize(64 * 1024 * 1024).
                setPersistentCache("MySql,size=1024,binary=0").setRDBConnection(RDBDataSourceFactory.forJdbcUrl(url, userName, password), options).getNodeStore();

        Repository repository = new Jcr(new Oak(storeD)).with(new LuceneIndexEditorProvider()).with(configureSearch()).createRepository();
        setStoreD(storeD);
        return repository;
	
    }
	
	protected static Repository createRepositoryPostGress(){
		
	/*
	 * Properties to ecternalize
	 * 	Datastore directory
	 * 	Which database and database info
	 */
        
        //Postgress
		String userName = PropertyFileBundle.getValue("postgres.username");
		String password = PropertyFileBundle.getValue("postgres.password");
		String driver = "org.postgresql.Driver";
        String url = PropertyFileBundle.getValue("postgres.connection.url");

		
		initFileStore("PostGress");

		String prefix = "T" + UUID.randomUUID().toString().replace("-", "");
		RDBOptions options = new RDBOptions().tablePrefix(prefix).dropTablesOnClose(false);
        DocumentNodeStore storeD = new DocumentMK.Builder().setAsyncDelay(0).setBlobStore(new DataStoreBlobStore(fds)).setClusterId(1).memoryCacheSize(64 * 1024 * 1024).
                setPersistentCache("postGress,size=1024,binary=0").setRDBConnection(RDBDataSourceFactory.forJdbcUrl(url, userName, password), options).getNodeStore();

        Repository repository = new Jcr(new Oak(storeD)).with(new LuceneIndexEditorProvider()).with(configureSearch()).createRepository();
        setStoreD(storeD);
        return repository;
	
    }

	
	public static QueryEngineSettings configureSearch(){
        QueryEngineSettings qs = new QueryEngineSettings();
        qs.setFullTextComparisonWithoutIndex(false);
        qs.setLimitInMemory(1000000);
        qs.setLimitReads(1000000);
        return qs;
	}
    
    public static void initFileStore(String datasourceType){
		File testDir = new File("oak-dstore", "oak-datastore"+"_"+datasourceType);

        FileDataStore _fds = new OakFileDataStore();
        _fds.setPath(testDir.getAbsolutePath());
        _fds.init(null);

        Iterator<DataIdentifier> dis = _fds.getAllIdentifiers();
        Set<String> fileNames = Sets.newHashSet(Iterators.transform(dis, new Function<DataIdentifier, String>() {
            //@Override
            public String apply(@Nullable DataIdentifier input) {
                return input.toString();
            }
        }));
        
        //fds.init(tempFolder.newFolder().getAbsolutePath());
        _fds.init(null);
        FileStore.Builder fileStoreBuilder = FileStore.newFileStore(testDir)
                                        .withBlobStore(new DataStoreBlobStore(_fds)).withMaxFileSize(256)
                                        .withCacheSize(64).withMemoryMapping(false);
        
        try {
			store = fileStoreBuilder.create();
			fds=_fds;
			fds.init("oak-fstore-repo");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	
    }
    
    public static SecurityProvider createSecurity(){
    	Set<String> adminNames = new HashSet<String>();
        adminNames.add("admin");
        ConfigurationParameters configurationParameters = ConfigurationParameters.of(PermissionConstants.PARAM_ADMINISTRATIVE_PRINCIPALS, adminNames);
        SecurityProvider securityProvider = new SecurityProviderImpl();
        AuthorizationConfigurationImpl authorizationConfiguration = AuthorizationConfigurationImpl.class.cast(securityProvider.getConfiguration(AuthorizationConfiguration.class));
        authorizationConfiguration.setParameters(configurationParameters);
        return securityProvider;
    }
    
	   public static void createLuceneIndex(Repository repo) throws RepositoryException {
	        Session session = loginOak(repo);
	      //  session.getNodeByIdentifier("/oak:index/counter").setProperty(IndexConstants.REINDEX_PROPERTY_NAME,	true);
	      //  session.save();   
	        
	        Node oakIndex = session.getRootNode().getNode("oak:index"); 
	        if(!oakIndex.hasNode("EKMAllProperties")){
		        Node lucene = oakIndex.addNode("EKMAllProperties");
		        lucene.setPrimaryType("oak:QueryIndexDefinition");
		        lucene.setProperty("compatVersion", 2);
		        lucene.setProperty("type", "lucene");
		        lucene.setProperty("fulltextEnabled", "false");
		        //How to restrict search paths
		        //lucene.setProperty("evaluatePathRestrictions", "true");
		        //lucene.setProperty("excludedPaths", "/test2");
		        lucene.setProperty("async", "async");
		        
		        Node rules = null;
		        rules = lucene.addNode("indexRules", "nt:unstructured");
	      	
		       	Node nTBase = null;
		       	nTBase= rules.addNode("nt:base");
		       	
		       	Node props = null;
		       	props = nTBase.addNode("properties", "nt:unstructured");
		       	props = nTBase.getNode("properties");
		       	
		       	Node allProps = props.addNode("allProps");
		       	allProps.setProperty("name", ".*");
		       	allProps.setProperty("isRegexp", true);
		       	//example on how to restrict properties for an index
		       	//allProps.setProperty("name", "^(?!restriction_test|restriction_test1)\\w*");
		       	allProps.setProperty("propertyIndex", true);
		
		       	session.save();
		        session.logout();
	        }
	        System.out.println("Lucene index created");
	    }
	   
	    public static javax.jcr.Session loginOak(Repository repo) throws LoginException, RepositoryException{
	    	SimpleCredentials credentials = new SimpleCredentials("admin", "admin".toCharArray());
	        credentials.setAttribute(RepositoryImpl.REFRESH_INTERVAL, new Long(Long.MAX_VALUE));
	    	Session newSession = repo.login(credentials);
	    	return newSession;
	    	
	    }
	    
		   public static void createLuceneIndexUuid(Repository repo) throws RepositoryException {
		        Session session = loginOak(repo);
		      //  session.getNodeByIdentifier("/oak:index/counter").setProperty(IndexConstants.REINDEX_PROPERTY_NAME,	true);
		      //  session.save();   
		        
		        Node oakIndex = session.getRootNode().getNode("oak:index"); 
		        Node lucene = oakIndex.getNode("uuid");
		        lucene.setPrimaryType("oak:QueryIndexDefinition");
		        lucene.setProperty("compatVersion", 2);
		        lucene.setProperty("type", "lucene");
		        lucene.setProperty("fulltextEnabled", "false");
		        //How to restrict search paths
		        //lucene.setProperty("evaluatePathRestrictions", "true");
		        //lucene.setProperty("excludedPaths", "/test2");
		        lucene.setProperty("async", "async");
		        
		        Node rules = null;
		        rules = lucene.addNode("indexRules", "nt:unstructured");
	      	
		       	Node nTBase = null;
		       	nTBase= rules.addNode("nt:base");
		       	
		       	Node props = null;
		       	props = nTBase.addNode("properties", "nt:unstructured");
		       	props = nTBase.getNode("properties");
		       	
		       	Node allProps = props.addNode("allProps");
		       	allProps.setProperty("name", ".*");
		       	allProps.setProperty("isRegexp", true);
		       	//example on how to restrict properties for an index
		       	//allProps.setProperty("name", "^(?!restriction_test|restriction_test1)\\w*");
		       	allProps.setProperty("propertyIndex", true);
		
		       	session.save();
		        session.logout();
		        System.out.println("Lucene index created");
		    }
		   public static void createLuceneIndexnodetype(Repository repo) throws RepositoryException {
		        Session session = loginOak(repo);
		      //  session.getNodeByIdentifier("/oak:index/counter").setProperty(IndexConstants.REINDEX_PROPERTY_NAME,	true);
		      //  session.save();   
		        
		        Node oakIndex = session.getRootNode().getNode("oak:index"); 
		        Node lucene = oakIndex.getNode("nodetype");
		        lucene.setPrimaryType("oak:QueryIndexDefinition");
		        lucene.setProperty("compatVersion", 2);
		        lucene.setProperty("type", "lucene");
		        lucene.setProperty("fulltextEnabled", "false");
		        //How to restrict search paths
		        //lucene.setProperty("evaluatePathRestrictions", "true");
		        //lucene.setProperty("excludedPaths", "/test2");
		        lucene.setProperty("async", "async");
		        
		        Node rules = null;
		        rules = lucene.addNode("indexRules", "nt:unstructured");
	      	
		       	Node nTBase = null;
		       	nTBase= rules.addNode("nt:base");
		       	
		       	Node props = null;
		       	props = nTBase.addNode("properties", "nt:unstructured");
		       	props = nTBase.getNode("properties");
		       	
		       	Node allProps = props.addNode("allProps");
		       	allProps.setProperty("name", ".*");
		       	allProps.setProperty("isRegexp", true);
		       	//example on how to restrict properties for an index
		       	//allProps.setProperty("name", "^(?!restriction_test|restriction_test1)\\w*");
		       	allProps.setProperty("propertyIndex", true);
		
		       	session.save();
		        session.logout();
		        System.out.println("Lucene index created");
		    }
		   public static void createLuceneIndexRef(Repository repo) throws RepositoryException {
		        Session session = loginOak(repo);
		      //  session.getNodeByIdentifier("/oak:index/counter").setProperty(IndexConstants.REINDEX_PROPERTY_NAME,	true);
		      //  session.save();   
		        
		        Node oakIndex = session.getRootNode().getNode("oak:index"); 
		        Node lucene = oakIndex.getNode("reference");
		        lucene.setPrimaryType("oak:QueryIndexDefinition");
		        lucene.setProperty("compatVersion", 2);
		        lucene.setProperty("type", "lucene");
		        lucene.setProperty("fulltextEnabled", "false");
		        //How to restrict search paths
		        //lucene.setProperty("evaluatePathRestrictions", "true");
		        //lucene.setProperty("excludedPaths", "/test2");
		        lucene.setProperty("async", "async");
		        
		        Node rules = null;
		        rules = lucene.addNode("indexRules", "nt:unstructured");
	      	
		       	Node nTBase = null;
		       	nTBase= rules.addNode("nt:base");
		       	
		       	Node props = null;
		       	props = nTBase.addNode("properties", "nt:unstructured");
		       	props = nTBase.getNode("properties");
		       	
		       	Node allProps = props.addNode("allProps");
		       	allProps.setProperty("name", ".*");
		       	allProps.setProperty("isRegexp", true);
		       	//example on how to restrict properties for an index
		       	//allProps.setProperty("name", "^(?!restriction_test|restriction_test1)\\w*");
		       	allProps.setProperty("propertyIndex", true);
		
		       	session.save();
		        session.logout();
		        System.out.println("Lucene index created");
		    }
		   public static void createLuceneIndexCounter(Repository repo) throws RepositoryException {
		        Session session = loginOak(repo);
		      //  session.getNodeByIdentifier("/oak:index/counter").setProperty(IndexConstants.REINDEX_PROPERTY_NAME,	true);
		      //  session.save();   
		        
		        Node oakIndex = session.getRootNode().getNode("oak:index"); 
		        Node lucene = oakIndex.getNode("counter");
		        lucene.setPrimaryType("oak:QueryIndexDefinition");
		        lucene.setProperty("compatVersion", 2);
		        lucene.setProperty("type", "lucene");
		        lucene.setProperty("fulltextEnabled", "false");
		        //How to restrict search paths
		        //lucene.setProperty("evaluatePathRestrictions", "true");
		        //lucene.setProperty("excludedPaths", "/test2");
		        lucene.setProperty("async", "async");
		        
		        Node rules = null;
		        rules = lucene.addNode("indexRules", "nt:unstructured");
	      	
		       	Node nTBase = null;
		       	nTBase= rules.addNode("nt:base");
		       	
		       	Node props = null;
		       	props = nTBase.addNode("properties", "nt:unstructured");
		       	props = nTBase.getNode("properties");
		       	
		       	Node allProps = props.addNode("allProps");
		       	allProps.setProperty("name", ".*");
		       	allProps.setProperty("isRegexp", true);
		       	//example on how to restrict properties for an index
		       	//allProps.setProperty("name", "^(?!restriction_test|restriction_test1)\\w*");
		       	allProps.setProperty("propertyIndex", true);
		
		       	session.save();
		        session.logout();
		        System.out.println("Lucene index created");
		    }
		   public static void createLuceneIndexacPrincipalName(Repository repo) throws RepositoryException {
		        Session session = loginOak(repo);
		      //  session.getNodeByIdentifier("/oak:index/counter").setProperty(IndexConstants.REINDEX_PROPERTY_NAME,	true);
		      //  session.save();   
		        
		        Node oakIndex = session.getRootNode().getNode("oak:index"); 
		        Node lucene = oakIndex.getNode("acPrincipalName");
		        lucene.setPrimaryType("oak:QueryIndexDefinition");
		        lucene.setProperty("compatVersion", 2);
		        lucene.setProperty("type", "lucene");
		        lucene.setProperty("fulltextEnabled", "false");
		        //How to restrict search paths
		        //lucene.setProperty("evaluatePathRestrictions", "true");
		        //lucene.setProperty("excludedPaths", "/test2");
		        lucene.setProperty("async", "async");
		        
		        Node rules = null;
		        rules = lucene.addNode("indexRules", "nt:unstructured");
	      	
		       	Node nTBase = null;
		       	nTBase= rules.addNode("nt:base");
		       	
		       	Node props = null;
		       	props = nTBase.addNode("properties", "nt:unstructured");
		       	props = nTBase.getNode("properties");
		       	
		       	Node allProps = props.addNode("allProps");
		       	allProps.setProperty("name", ".*");
		       	allProps.setProperty("isRegexp", true);
		       	//example on how to restrict properties for an index
		       	//allProps.setProperty("name", "^(?!restriction_test|restriction_test1)\\w*");
		       	allProps.setProperty("propertyIndex", true);
		
		       	session.save();
		        session.logout();
		        System.out.println("Lucene index created");
		    }
		   
		   public static void createLuceneIndexacAuthorizableId(Repository repo) throws RepositoryException {
		        Session session = loginOak(repo);
		      //  session.getNodeByIdentifier("/oak:index/counter").setProperty(IndexConstants.REINDEX_PROPERTY_NAME,	true);
		      //  session.save();   
		        
		        Node oakIndex = session.getRootNode().getNode("oak:index"); 
		        Node lucene = oakIndex.getNode("authorizableId");
		        lucene.setPrimaryType("oak:QueryIndexDefinition");
		        lucene.setProperty("compatVersion", 2);
		        lucene.setProperty("type", "lucene");
		        lucene.setProperty("fulltextEnabled", "false");
		        //How to restrict search paths
		        //lucene.setProperty("evaluatePathRestrictions", "true");
		        //lucene.setProperty("excludedPaths", "/test2");
		        lucene.setProperty("async", "async");
		        
		        Node rules = null;
		        rules = lucene.addNode("indexRules", "nt:unstructured");
	      	
		       	Node nTBase = null;
		       	nTBase= rules.addNode("nt:base");
		       	
		       	Node props = null;
		       	props = nTBase.addNode("properties", "nt:unstructured");
		       	props = nTBase.getNode("properties");
		       	
		       	Node allProps = props.addNode("allProps");
		       	allProps.setProperty("name", ".*");
		       	allProps.setProperty("isRegexp", true);
		       	//example on how to restrict properties for an index
		       	//allProps.setProperty("name", "^(?!restriction_test|restriction_test1)\\w*");
		       	allProps.setProperty("propertyIndex", true);
		
		       	session.save();
		        session.logout();
		        System.out.println("Lucene index created");
		    }
		   
		   public static void createLuceneIndexacprincipalNameNoA(Repository repo) throws RepositoryException {
		        Session session = loginOak(repo);
		      //  session.getNodeByIdentifier("/oak:index/counter").setProperty(IndexConstants.REINDEX_PROPERTY_NAME,	true);
		      //  session.save();   
		        
		        Node oakIndex = session.getRootNode().getNode("oak:index"); 
		        Node lucene = oakIndex.getNode("principalName");
		        lucene.setPrimaryType("oak:QueryIndexDefinition");
		        lucene.setProperty("compatVersion", 2);
		        lucene.setProperty("type", "lucene");
		        lucene.setProperty("fulltextEnabled", "false");
		        //How to restrict search paths
		        //lucene.setProperty("evaluatePathRestrictions", "true");
		        //lucene.setProperty("excludedPaths", "/test2");
		        lucene.setProperty("async", "async");
		        
		        Node rules = null;
		        rules = lucene.addNode("indexRules", "nt:unstructured");
	      	
		       	Node nTBase = null;
		       	nTBase= rules.addNode("nt:base");
		       	
		       	Node props = null;
		       	props = nTBase.addNode("properties", "nt:unstructured");
		       	props = nTBase.getNode("properties");
		       	
		       	Node allProps = props.addNode("allProps");
		       	allProps.setProperty("name", ".*");
		       	allProps.setProperty("isRegexp", true);
		       	//example on how to restrict properties for an index
		       	//allProps.setProperty("name", "^(?!restriction_test|restriction_test1)\\w*");
		       	allProps.setProperty("propertyIndex", true);
		
		       	session.save();
		        session.logout();
		        System.out.println("Lucene index created");
		    }
		   
		   public static void createOrderedIndex(Repository repo) throws RepositoryException {
		        Session session = loginOak(repo);
		      //  session.getNodeByIdentifier("/oak:index/counter").setProperty(IndexConstants.REINDEX_PROPERTY_NAME,	true);
		      //  session.save();   
		        
		        Node oakIndex = session.getRootNode().getNode("oak:index"); 
		        Node lucene = oakIndex.addNode("OrderedIndex");
		        lucene.setPrimaryType("oak:QueryIndexDefinition");
		        lucene.setProperty("type", "ordered");
		        lucene.setProperty("propertyNames",  ".*");
		        lucene.setProperty("async", "async");
		        lucene.setProperty("isRegexp", true);
		
		       	session.save();
		        session.logout();
		        System.out.println("Lucene index created");
		    }
		   public static void createProperty(Repository repo) throws RepositoryException {
		        Session session = loginOak(repo);
		      //  session.getNodeByIdentifier("/oak:index/counter").setProperty(IndexConstants.REINDEX_PROPERTY_NAME,	true);
		      //  session.save();   
		        
		        Node oakIndex = session.getRootNode().getNode("oak:index"); 
		        Node lucene = oakIndex.addNode("PropertyIndex");
		        lucene.setPrimaryType("oak:QueryIndexDefinition");
		        lucene.setProperty("type", "property");
		        lucene.setProperty("propertyNames",  ".*");
		        lucene.setProperty("async", "async");
		        lucene.setProperty("isRegexp", true);
		
		       	session.save();
		        session.logout();
		        System.out.println("Lucene index created");
		    }
		

}
