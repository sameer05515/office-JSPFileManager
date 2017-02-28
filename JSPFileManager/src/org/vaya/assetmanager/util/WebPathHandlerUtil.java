package org.vaya.assetmanager.util;

import org.apache.commons.lang.StringUtils;

/**
 * Simple WebPathHandlerUtil.
 * 
 * @author charlie collins
 *
 **/ 
public class WebPathHandlerUtil
{
    // debug
    private static boolean debug = false;

    // vars
    private static String HTML = "html";
    private static String BACKSLASH = "\\";
    private static String FORWARDSLASH = "/";
    private static String C_COLON = "c:";

    /**
     * Use context real path to compare to fileName - "pageContext.getServletContext().getRealPath(request.getContextPath()));"
     * and if fileName is sub of contextRealPath than web accessible is true.
     * 
     * @param contextRealPath
     * @param fileName
     * @return
     *
     **/    
    public static boolean isFileWebAccessible(String contextRealPath, String fileName)
    {
        if (debug)
        {
            System.out.println("\n  isFileWebAccessible invoked");
            System.out.println("   contextRealPath = " + contextRealPath + " \n   fileName = " + fileName);
        } 
        boolean access = false;
        if ((contextRealPath != null) && (fileName != null))
        {
            if (fileName.toLowerCase().indexOf(contextRealPath.toLowerCase()) != -1) access = true;   
        }
        if (debug)
        {
            System.out.println("   access = " + access);
        }
        return access;
    }

    /**
     * Translate path into UNIX style path (for use on a real computer or the Internet)
     * 
     * @param fileName
     * @return
     *
     **/
    public static String getUnixPath(String fileName)
    {        
        String path = null;        
        if (fileName != null)
        {            
            path = fileName;
            if (path.indexOf(C_COLON) != -1)
            {
                path = StringUtils.replace(path, C_COLON, "");
            }
            if (path.indexOf(BACKSLASH) != -1)
            {
                path = StringUtils.replace(path, BACKSLASH, FORWARDSLASH);    
            }   
        }        
        return path;      
    }   
   
    /**
     *  Get web path for particular fileName using context.
     *
     * use contextRealPath against fileName to get the difference 
     * add the contextPath to the front of the difference and that should be the web path
     *
     * ie :  
     * contextRealPath = /opt/fileManager/webApplication
     * contextPath = /fileManager
     * fileName = /opt/fileManager/webApplication/test/test1.html
     * 
     * difference of contextRealPath against fileName = test/test1.html
     * contextPath with difference = /fileManager/test/test1.html
     *    
     * 
     * @param contextRealPath
     * @param contextPath
     * @param fileName
     * @return
     *
     **/    
    public static String getWebPath(String contextRealPath, String contextPath, String fileName)
    {
        if (debug)
        {
            System.out.println("\n  getWebPath invoked");
            System.out.println("  contextRealPath = " + contextRealPath);
            System.out.println("  contextPath = " + contextPath);
            System.out.println("  fileName = " + fileName); 
        }
        String path = null;
        
        if ((contextRealPath != null) && (contextPath != null) && (fileName != null))
        {
            String unixContextRealPath = getUnixPath(contextRealPath);
            String unixFileName = getUnixPath(fileName);
            String filePath = StringUtils.replace(unixFileName, unixContextRealPath, "");
            
            if (debug)
            {
                System.out.println("  unixContextRealPath = " + unixContextRealPath);
                System.out.println("  unixFileName = " + unixFileName);
                System.out.println("  filePath = " + filePath);
            }                        
            path = contextPath + FORWARDSLASH + filePath;            
        } 
        if (debug)
        {
            System.out.println("  path = " + path);
        }
        return path;    
    }
}
