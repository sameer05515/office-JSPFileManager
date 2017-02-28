package org.vaya.assetmanager.util;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

/**
 * Util to zip and unzip files and such.
 *
 * @author charlie collins
 * 
 * TODO - fix exception handling so that there is some ;)
 * 
 **/
public class ZipHandlerUtil
{
    // vars
    private static final int BUFFER = 2048;
    public static final String FILE_SEP = File.separator;

    /**
     * Util to unzip file.
     * 
     * @param f
     *
     **/
    public static void unZip(File f)
    {
        try
        {
            // create file
            File sourceZipFile = f;

            // create zip file
            ZipFile zipFile = new ZipFile(sourceZipFile, ZipFile.OPEN_READ);

            // get enum of entries
            Enumeration zipFileEntries = zipFile.entries();

            // process each entry
            while (zipFileEntries.hasMoreElements())
            {
                // grab a zip file entry
                ZipEntry entry = (ZipEntry) zipFileEntries.nextElement();
                String currentEntry = entry.getName();
                System.out.println("Extracting: " + entry);

                // create dest dir
                File destFile = new File(sourceZipFile.getParent(), currentEntry);

                // grab parent directory structure
                File destinationParent = destFile.getParentFile();

                // create parent directory structure if needed
                destinationParent.mkdirs();

                // extract file if not a directory
                if (!entry.isDirectory())
                {
                    BufferedInputStream is = new BufferedInputStream(zipFile.getInputStream(entry));
                    int currentByte;
                    // buffer for writing file
                    byte data[] = new byte[BUFFER];

                    // write file to disk
                    FileOutputStream fos = new FileOutputStream(destFile);
                    BufferedOutputStream dest = new BufferedOutputStream(fos, BUFFER);

                    // read and write to last byte
                    while ((currentByte = is.read(data, 0, BUFFER)) != -1)
                    {
                        dest.write(data, 0, currentByte);
                    }

                    // close
                    dest.flush();
                    dest.close();
                    is.close();
                }
            }
            zipFile.close();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }
    
    /**
     * Util to create zip file from list of files.
     * 
     * @param fileList
     * @param newFileName
     */
    public static void zip(ArrayList fileList, String newFileName)
    {
        if ((fileList != null) && (newFileName != null))
        {
            // buffer
            byte[] buf = new byte[BUFFER];

            try 
            {
                // create the streams (dont cross the streams!)
                FileOutputStream fos = new FileOutputStream(newFileName);
                ZipOutputStream zos = new ZipOutputStream(fos);

                // compress each individual file
                int size = fileList.size();
                for (int i=0; i < size; i++) 
                {
                    String thisFile = (String) fileList.get(i);
                    FileInputStream fis = new FileInputStream(thisFile);

                    // zip entry name to be just the fileName, not the full path
                    String zipEntryName = thisFile.substring(thisFile.lastIndexOf(FILE_SEP), thisFile.length());
                    
                    zos.putNextEntry(new ZipEntry(zipEntryName));

                    // xfer bytes from file to zipFile
                    int len;
                    while ((len = fis.read(buf)) > 0) 
                    {
                        zos.write(buf, 0, len);
                    }

                    // close entry
                    zos.closeEntry();
                    fis.close();
                }

                // close zip file
                zos.close();
            } 
            catch (Exception e)
            {
                e.printStackTrace();   
            }
        }
    }
    
    /**
     * Util to return zip file contents as array list.
     * 
     * @param f
     * @return
     *
     **/
    public static ArrayList getZipFileContents(File f)
    {
        ArrayList returnList = new ArrayList();
        
        try
        {
            // create zip file
            ZipFile zipFile = new ZipFile(f, ZipFile.OPEN_READ);

            // get enum of entries
            Enumeration zipFileEntries = zipFile.entries();

            // process each entry
            while (zipFileEntries.hasMoreElements())
            {
                // grab a zip file entry
                ZipEntry entry = (ZipEntry) zipFileEntries.nextElement();
                String currentEntry = entry.getName();
                returnList.add(currentEntry);
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();   
        }
        
        return returnList;        
    }    
}
