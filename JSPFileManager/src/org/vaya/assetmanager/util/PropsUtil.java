package org.vaya.assetmanager.util;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * @author ccollins
 * 
 */
public class PropsUtil 
{
	public static Properties getProperties(String propFileName) 
	{
		Properties props = new Properties();
		InputStream is = PropsUtil.class.getResourceAsStream("/" + propFileName);
		if (is != null)
		{
		    try
		    {		
		        props.load(is);
		    }
		    catch (IOException ioe)
		    {		
		        System.err.println(" ERROR UNABLE TO LOAD PROPERTIES FILE - " + propFileName);
		        ioe.printStackTrace();
		    }
		}
		else
		{
		    System.err.println(" ERROR PROPERTIES FILE NULL OR INCORRECTLY DEFINED " + propFileName);		       
		}		
		return props;		
	}	
}
