package com.test.oak;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Locale;
import java.util.ResourceBundle;

public class PropertyFileBundle {
	
	private static Hashtable oakTestProperties = new Hashtable();
	
	private static void init(){
	
		Enumeration bundleKeys = getBundle().getKeys();
		
		while (bundleKeys.hasMoreElements()) {
		    String key = (String)bundleKeys.nextElement();
		    String value = getBundle().getString(key);
		    oakTestProperties.put(key, value);
		}
	}
	
	public static String getValue(String key){
		if(oakTestProperties.size()<1)
			init();
		return (String)oakTestProperties.get(key);
	}
	
	private static ResourceBundle getBundle(){
		File file = new File(System.getProperty("user.dir"));
		ResourceBundle bundle =null;
		try{
			URL[] urls = {file.toURI().toURL()};
			ClassLoader loader = new URLClassLoader(urls);
			bundle = ResourceBundle.getBundle("OakTest", Locale.getDefault(), loader);
		}catch(MalformedURLException mE){
			mE.printStackTrace();
		}

		return bundle;
	}
}
