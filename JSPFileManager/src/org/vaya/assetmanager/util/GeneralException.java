package org.vaya.assetmanager.util;
/**
 * Minimal exception class to be used for error handling.
 * 
 * @author ccollins
 * 
 **/
public class GeneralException extends Exception
{

    /**
     * 
     **/
    public GeneralException()
    {
        super();        
    }

    /**
     * @param s
     **/
    public GeneralException(String s)
    {
        super(s);        
    }
}
