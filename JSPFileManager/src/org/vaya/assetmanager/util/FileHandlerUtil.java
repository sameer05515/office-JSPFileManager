package org.vaya.assetmanager.util;
import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.StringReader;
import java.io.Writer;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import java.util.Stack;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.jsp.JspWriter;

import org.apache.commons.fileupload.DiskFileUpload;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileUpload;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.SystemUtils;

/**
 * @author ccollins
 * 
 * Class to provide utility functions for browsing, viewing and manipulating files from within web application.
 * 
 * TODO - remove all use of file as string and use File, as per http://jakarta.apache.org/commons/io/bestpractices.html.
 */
public class FileHandlerUtil
{
    // debug
    private static boolean debug = true;

    // logger (using J2SE 1.4 "Logger")
    private static final Logger logger = Logger.getLogger(FileHandlerUtil.class.getName());

    // general const
    private static final String BASE_NAME = "BASE_NAME";
    private static final String ACTION = "ACTION";
    private static final String MESSAGE = "MESSAGE";
    public static final String KEY_PATH = "KEY_PATH";
    public static final String KEY_VALID = "KEY_VALID";
    public static final String TRUE = "true";
    public static final String FALSE = "false";
    public static final String ICON = "icon";
    public static final String IMAGE = "IMAGE";    
   
    // file sys
    private static final String TMP_DIR = System.getProperty("java.io.tmpdir");
    private static final String OS_NAME = System.getProperty("os.name");
    public static final String FILE_SEP = File.separator;
    public static final String FILE_SEP_WINDERS = "\\";
    public static final String LINE_SEP = SystemUtils.LINE_SEPARATOR;

    // props
    public static Properties props = PropsUtil.getProperties("JSPFileManager.properties");
    
    // FILE_MANAGER
    private static String FILE_MANAGER = "fileManager.jsp";
    
    // PSEUDOCHROOT - defaults to TRUE (set in WEB-INF/classes/JSPFileManager.properties)
    private static boolean pseudochroot = true;
    private static String pseudochroot_s = (String) props.getProperty("PSEUDOCHROOT");
    static
    {
        if ((pseudochroot_s != null) && (pseudochroot_s.equalsIgnoreCase(FALSE)))
            pseudochroot = false;
    }
    
    // ROOT_DIR - starting point AND dir set to root when pseudochroot true (set in WEB-INF/classes/JSPFileManager.properties)
    private static final String ROOT_DIR = (String) props.getProperty("ROOT_DIR");
    
    // HOME_DIR
    private static final String HOME_DIR = SystemUtils.USER_HOME;
    
    // file upload max to store in memory - 5000000 Byte = 4.7683716 MB
    private static final int sizeThreshold = 5000000;
    
    // file upload max bytes - 100000000 Byte = 95.3674316 MB
    private static final int sizeMax = 100000000;
    
    // reused exceptions
    private static GeneralException rootDirException = new GeneralException(
            "*ERROR*, unable to create/save file due to SECURITY EXCEPTION, attempt to save file outside of allowed path, incident LOGGED.");
    private static GeneralException noParentException = new GeneralException(
            "ERROR cannot parent file/directory does not exist, operation not permitted on ROOT.");
    private static GeneralException nonUniqueNameException = new GeneralException(
            "*ERROR*, file/directory name already exists.");
    private static GeneralException invalidNameException = new GeneralException(
            "*ERROR* file/directory name contains invalid characters.");
    private static GeneralException entityExistsException = new GeneralException(
            "*ERROR* file/directory already exists.");
    private static GeneralException parameterException = new GeneralException("*ERROR*, input parameters invalid.");
    
    // general meta file types
    public static final String TEXT = "TEXT";
    public static final String EDIT_IMAGE = "EDIT_IMAGE";
    public static final String NON_EDIT_IMAGE = "NON_IMAGE_IMAGE";
    public static final String AUDIO = "AUDIO";
    public static final String COMP = "COMP";
    public static final String VIDEO = "VIDEO";
    public static final String APP_INT = "APP_INT";
    public static final String APP_EXT = "APP_EXT";
    public static final String HTML_META = "HTML_META";
    
    // specific file types
    private static final String JPEG = "jpeg";
    private static final String JPG = "jpg";
    private static final String GIF = "gif";
    private static final String PNG = "png";
    private static final String BMP = "bmp";
    private static final String AVI = "avi";
    private static final String MPEG = "mpeg";
    private static final String MPG = "mpg";
    private static final String MOV = "mov";
    private static final String MP3 = "mp3";
    private static final String TXT = "txt";
    private static final String LOG = "log";
    private static final String JAVA = "java";
    private static final String HTML = "html";
    private static final String HTM = "htm";
    private static final String CSS = "css";
    private static final String JSP = "jsp";
    private static final String JS = "js";
    private static final String XML = "xml";
    private static final String DOC = "doc";
    private static final String MDB = "mdb";
    private static final String PPT = "ppt";
    private static final String MPP = "mpp";
    private static final String XLS = "xls";
    private static final String PUB = "pub";    
    private static final String PDF = "pdf";
    private static final String RTF = "rtf";
    private static final String ZIP = "zip";
    private static final String TAR = "tar";
    private static final String JAR = "jar";
    private static final String GZ = "gz";
    private static final String TGZ = "tgz";
    
    // handled mime types
    public static final String UNKNOWN_MIME_TYPE = "unknown";
    public static final String IMAGE_JPEG = "image/jpeg";
    public static final String IMAGE_GIF = "image/gif";
    public static final String IMAGE_BMP = "image/bmp";
    public static final String IMAGE_PNG = "image/png";
    public static final String VIDEO_MPEG = "video/mpeg";
    public static final String VIDEO_AVI = "video/avi";
    public static final String VIDEO_QT = "video/quicktime";    
    public static final String AUDIO_MP3 = "audio/x-mpeg";
    public static final String TEXT_PLAIN = "text/plain";
    public static final String TEXT_HTML = "text/html";
    public static final String TEXT_XML = "text/xml";
    public static final String APP_PDF = "application/pdf";
    public static final String APP_RTF = "application/rtf";
    public static final String APP_XLS = "application/vnd.ms-excel";    
    public static final String APP_DOC = "application/msword";
    public static final String APP_PPT = "application/vnd.ms-powerpoint";
    public static final String APP_MPP = "application/vnd.ms-project";
    public static final String APP_MDB = "application/x-msaccess";
    public static final String APP_PUB = "application/x-mspublisher";    
    public static final String COMP_ZIP = "application/zip";
    public static final String COMP_TAR = "application/x-tar";
    public static final String COMP_JAR = "application/java-archive";
    public static final String COMP_GZ = "application/x-gzip";
       
    /**
     * Static init block to load HashMap of HashMaps for file type checks. 
     * (Need only single property so ArrayLists or such would suffice, but they are log(n) and HashMap log(1) so it is used for performance.)
     *  
     */
    private static HashMap fileTypes;
    private static HashMap editableImageTypes;
    private static HashMap nonEditableImageTypes;
    private static HashMap videoTypes;
    private static HashMap audioTypes;
    private static HashMap textTypes;
    private static HashMap htmlTypes;
    private static HashMap compressTypes;
    private static HashMap externalAppTypes;
    private static HashMap internalAppTypes;
    static
    {
        fileTypes = new HashMap();
        editableImageTypes = new HashMap();
        nonEditableImageTypes = new HashMap();
        videoTypes = new HashMap();
        audioTypes = new HashMap();
        textTypes = new HashMap();
        htmlTypes = new HashMap();
        compressTypes = new HashMap();
        externalAppTypes = new HashMap();
        internalAppTypes = new HashMap();        
        editableImageTypes.put(JPG, EDIT_IMAGE);
        editableImageTypes.put(PNG, EDIT_IMAGE);
        editableImageTypes.put(BMP, EDIT_IMAGE);
        editableImageTypes.put(JPEG, EDIT_IMAGE);
        nonEditableImageTypes.put(GIF, NON_EDIT_IMAGE);
        videoTypes.put(MPG, VIDEO);
        videoTypes.put(MPEG, VIDEO);
        videoTypes.put(MOV, VIDEO);
        videoTypes.put(AVI, VIDEO);
        audioTypes.put(MP3, AUDIO);
        textTypes.put(TXT, TEXT);
        textTypes.put(JSP, TEXT);
        textTypes.put(JAVA, TEXT);
        textTypes.put(JS, TEXT);
        textTypes.put(CSS, TEXT);
        textTypes.put(LOG, TEXT);
        textTypes.put(XML, TEXT);
        htmlTypes.put(HTML, HTML_META);
        htmlTypes.put(HTM, HTML_META);
        externalAppTypes.put(DOC, APP_EXT);
        externalAppTypes.put(MDB, APP_EXT);
        externalAppTypes.put(PPT, APP_EXT);        
        externalAppTypes.put(MPP, APP_EXT);
        externalAppTypes.put(PUB, APP_EXT);
        externalAppTypes.put(XLS, APP_EXT);
        externalAppTypes.put(RTF, APP_EXT);
        internalAppTypes.put(PDF, APP_INT);
        compressTypes.put(ZIP, COMP);
        compressTypes.put(TAR, COMP);
        compressTypes.put(JAR, COMP);
        compressTypes.put(GZ, COMP);
        compressTypes.put(TGZ, COMP);
        fileTypes.put(EDIT_IMAGE, editableImageTypes);
        fileTypes.put(NON_EDIT_IMAGE, nonEditableImageTypes);
        fileTypes.put(VIDEO, videoTypes);
        fileTypes.put(AUDIO, audioTypes);
        fileTypes.put(COMP, compressTypes);
        fileTypes.put(TEXT, textTypes);
        fileTypes.put(HTML_META, htmlTypes);
        fileTypes.put(APP_INT, internalAppTypes);
        fileTypes.put(APP_EXT, externalAppTypes);
    }

    /**
     * Const.
     * 
     * @param f
     * @return
     */
    public FileHandlerUtil()
    {
    }

    /**
     * Get debug status.
     * 
     * @param f
     * @return
     */
    public static boolean getDebug()
    {
        return debug;
    }

    /**
     * Get pseudochroot status.
     * 
     * @param f
     * @return
     */
    public static boolean getPseudoChroot()
    {
        return pseudochroot;
    }

    /**
     * Get ROOT_DIR.
     * 
     * @param f
     * @return
     */
    public static String getRootDir()
    {
        return ROOT_DIR;
    }

    /**
     * Get HOME_DIR.
     * 
     * @param f
     * @return
     */
    public static String getHomeDir()
    {
        return SystemUtils.USER_HOME;
    }

    /**
     * Util to determine if file is of specified meta type. Valid types are: TEXT EDIT_IMAGE NON_EDIT_IMAGE AUDIO COMP
     * VIDEO APP_INT APP_EXT
     * 
     * @param f
     * @return
     */
    public static boolean isFileType(File f, String type)
    {
        boolean found = false;
        if (f != null)
        {
            String ext = getExtension(f.getName().toLowerCase());
            if (ext != null)
            {
                HashMap typeMap = (HashMap) fileTypes.get(type);
                if (typeMap != null)
                {
                    if (typeMap.get(ext) != null)
                    {
                        found = true;
                    }
                }
            }
        }
        return found;
    }

    /**
     * Overload of isFileType with input String (fileName) instead of File.
     * 
     * @param f
     * @param type
     * @return
     */
    public static boolean isFileType(String f, String type)
    {
        File tempFile = new File(f);
        return isFileType(tempFile, type);
    }

    /**
     * Get mime type based on static member vars of this class.
     * 
     * @param f
     * @return
     */
    public static String getMimeType(File f)
    {
        String returnType = null;
        if (f != null)
        {            
            String ext = getExtension(f.getName().toLowerCase());
            if (ext != null)
            {
                if (ext.equals(JPG))
                    returnType = IMAGE_JPEG;
                else if (ext.equals(JPEG))
                    returnType = IMAGE_JPEG;
                else if (ext.equals(GIF))
                    returnType = IMAGE_GIF;
                else if (ext.equals(BMP))
                    returnType = IMAGE_BMP;
                else if (ext.equals(PNG))
                    returnType = IMAGE_PNG;
                else if (ext.equals(MPEG))
                    returnType = VIDEO_MPEG;
                else if (ext.equals(MPG))
                    returnType = VIDEO_MPEG;
                else if (ext.equals(MOV))
                    returnType = VIDEO_QT;
                else if (ext.equals(AVI))
                    returnType = VIDEO_AVI;
                else if (ext.equals(MP3))
                    returnType = AUDIO_MP3;
                else if (ext.equals(HTML))
                    returnType = TEXT_HTML;
                else if (ext.equals(HTM))
                    returnType = TEXT_HTML;
                else if (ext.equals(XML))
                    returnType = TEXT_XML;
                else if (ext.equals(PDF))
                    returnType = APP_PDF;
                else if (ext.equals(RTF))
                    returnType = APP_RTF;
                else if (ext.equals(DOC))
                    returnType = APP_DOC;
                else if (ext.equals(PPT))
                    returnType = APP_PPT;
                else if (ext.equals(MPP))
                    returnType = APP_MPP;
                else if (ext.equals(MDB))
                    returnType = APP_MDB;
                else if (ext.equals(PUB))
                    returnType = APP_PUB;
                else if (ext.equals(XLS))
                    returnType = APP_XLS;
                else if (ext.equals(ZIP))
                    returnType = COMP_ZIP;
                else if (ext.equals(TAR))
                    returnType = COMP_TAR;
                else if (ext.equals(JAR))
                    returnType = COMP_JAR;
                else if (ext.equals(GZ))
                    returnType = COMP_GZ;
                else
                    returnType = UNKNOWN_MIME_TYPE;
            }
            else
            {
                returnType = UNKNOWN_MIME_TYPE;
            }
        }
        return returnType;
    }

    /**
     * Based on mime type associate icon image with file.
     * Icon Images should be on file system in the form of iconTYPE.jpg.
     * (Where TYPE is upper case extension of file or special meta type.)
     * (Special meta type is type that encompasses multiple file extensions, such as avi, mov, mpg and so on for VIDEO.)
     * 
     * @param dirName
     * @param filter
     * @return
     */
    public static String getIconImageForMimeType(File f)
    {
        String iconImage = null;
        if (f != null)
        {
            String type = getMimeType(f);
            String ext = getExtension(f.getName().toLowerCase());
            // if type is a known type then use iconTYPE.jpg from image path
            // (this way new icon types can be added with no code change, just place iconTYPE.jpg into image dir)
            //
            // unknown types are assigned to be text so that edit/view and such functions 
            // attempt to work on textual content (as most files are textual)
            
            // special meta types
            if ((type.equals(IMAGE_JPEG)) || (type.equals(IMAGE_PNG)) || (type.equals(IMAGE_BMP)))
            {
                iconImage = ICON + IMAGE.toUpperCase() + "." + JPG;
            }
            else if (type.equals(IMAGE_GIF))
            {
                iconImage = ICON + IMAGE.toUpperCase() + "." + JPG;
            }
            else if ((type.equals(VIDEO_MPEG)) || (type.equals(VIDEO_AVI)) || (type.equals(VIDEO_QT)))
            {
                iconImage = ICON + VIDEO.toUpperCase() + "." + JPG;
            }
            else if (type.equals(TEXT_HTML))
            {
                iconImage = ICON + HTML.toUpperCase() + "." + JPG;
            }
            else if ((type.equals(COMP_ZIP)) || (type.equals(COMP_TAR)) || (type.equals(COMP_JAR)) || (type.equals(COMP_GZ)) )
            {
                iconImage = ICON + COMP.toUpperCase() + "." + JPG;
            }            
            else if (type.equals(AUDIO_MP3))
            {
                iconImage = ICON + AUDIO.toUpperCase() + "." + JPG;
            }
            else if (type.equals(TEXT_PLAIN))
            {
                iconImage = ICON + TEXT.toUpperCase() + "." + JPG;
            }            
            // unknown mime types
            else if (type.equals(UNKNOWN_MIME_TYPE))
            {
                iconImage = ICON + TEXT.toUpperCase() + "." + JPG;
            }
            // lastly if not special meta type OR unknown then use extension for icon file name 
            else
            {
                iconImage = ICON + ext.toUpperCase() + "." + JPG;
            }            
        }            
        return iconImage;
    }

    /**
     * Util to get files and dirs based on passed in dir.
     * 
     * @param dirName
     * @param filter
     * @return
     */
    public static List getFileList(String dirName, FileFilter filter)
    {
        if (debug)
            logger.log(Level.INFO, "  getFileList invoked for - " + dirName);
        List filesList = null;
        File dir = null;
        // setup File dir
        dir = new File(dirName);
        // filter dir list and convert to arrayList
        if ((dir != null) && (dir.isDirectory()) && (dir.exists()) && (dir.canRead()))
        {
            File[] files = null;
            if (filter != null)
            {
                files = dir.listFiles(filter);
            }
            else
            {
                files = dir.listFiles();
            }
            if (files != null)
            {
                filesList = Arrays.asList(files);
            }
        }
        return filesList;
    }

    /**
     * Overload of getFileList to return directories only.
     * 
     * @param dirName
     * @return
     */
    public static List getDirList(String dirName)
    {
        return getFileList(dirName, new FileFilter_DIR());
    }

    /**
     * Overload of getFileList to return files only.
     * 
     * @param dirName
     * @return
     */
    public static List getFileList(String dirName)
    {
        return getFileList(dirName, new FileFilter_FILE());
    }

    /**
     * Overload of getFileList to return directories and files.
     * 
     * @param dirName
     * @return
     */
    public static List getList(String dirName)
    {
        return getFileList(dirName, null);
    }

    /**
     * Overload of getFileList to return files of image types only.
     * 
     * @param dirName
     * @return
     */
    public static List getImageList(String dirName)
    {
        return getFileList(dirName, new FileFilter_IMAGE());
    }

    /**
     * Overload of getFileList to return files of compessed type that CONTAIN image types only.
     * 
     * @param dirName
     * @return
     */
    public static List getCompImageList(String dirName)
    {
        return getFileList(dirName, new FileFilter_COMP_IMAGE());
    }

    /**
     * Overload of getFileList to return files of comp types only.
     * 
     * @param dirName
     * @return
     */
    public static List getCompList(String dirName)
    {
        return getFileList(dirName, new FileFilter_COMP());
    }

    /**
     * Overload of getFileList to return files of html types only.
     * 
     * @param dirName
     * @return
     */
    public static List getHTMLList(String dirName)
    {
        return getFileList(dirName, new FileFilter_HTML());
    }

    /**
     * Util to parse files or directories from list of files. This is provided as an alternative to recalling specific
     * directory OR file lists once ALL list has been obtained.
     * 
     * @param all
     * @return
     */
    public static List parseList(List all, boolean getDirs)
    {
        List returnList = null;
        List dirList = new ArrayList();
        List fileList = new ArrayList();
        if (all != null)
        {
            for (int i = 0; i < all.size(); i++)
            {
                File thisFile = (File) all.get(i);
                if (thisFile.isDirectory())
                {
                    dirList.add(thisFile);
                }
                else if (thisFile.isFile())
                {
                    fileList.add(thisFile);
                }
            }
            if (getDirs)
                returnList = dirList;
            else
                returnList = fileList;
        }
        return returnList;
    }

    // parse dirs
    public static List parseDirList(List all)
    {
        return parseList(all, true);
    }

    // parse files
    public static List parseFileList(List all)
    {
        return parseList(all, false);
    }

    /**
     * Return roots as ArrayList.
     * 
     * @return
     */
    public static List getRoots()
    {
        return Arrays.asList(File.listRoots());
    }    

    /**
     * Check baseName against ROOT_DIR and REPLACE with ROOT_DIR if baseName is path outside of baseName. This is to
     * ensure security of "pseudochroot" and "ROOT_DIR" when pseudochroot is enabled.
     * 
     * This method should only be used WHEN pseudochroot is enabled, it does not ITSELF check the status of
     * pseudochroot.
     * 
     * Returns HashMap as such: key value ---- ---- String VALID Boolean true/false String PATH String path, same as
     * original if valid, or REPLACED with root if not
     * 
     * 
     * @param baseName
     * @param rootDir
     * @return
     */
    public static HashMap validateBaseName(String baseName, String rootDir)
    {
        HashMap baseNameMap = new HashMap();
        if ((baseName != null) && (rootDir != null))
        {
            // if baseName equals ROOT_DIR things are ok
            // if baseName not equal ROOT_DIR - AND - baseName not start with
            // ROOT_DIR, then its outside allowed path
            // REPLACE it with the ROOT_DIR
            if ((!baseName.equals(ROOT_DIR)) && (!baseName.startsWith(ROOT_DIR)))
            {                
                String requestedBaseName = baseName;
                baseName = ROOT_DIR;
                baseNameMap.put(KEY_VALID, new Boolean(false));
                baseNameMap.put(KEY_PATH, baseName);
                logger.log(Level.WARNING, "\n");
                logger.log(Level.WARNING,
                        "  *** SECURITY ACCESS WARNING *** attempt to access directory OUTSIDE ROOT_DIR.");
                logger.log(Level.WARNING, "  *** ROOT_DIR = " + ROOT_DIR);
                logger.log(Level.WARNING, "  *** REQUESTED FILE PATH = " + requestedBaseName);
                logger.log(Level.WARNING, "  *** REPLACED requested path WITH ROOT_DIR");
                logger.log(Level.WARNING, "\n");
            }
            else
            {
                baseNameMap.put(KEY_VALID, new Boolean(true));
                baseNameMap.put(KEY_PATH, baseName);
            }
        }
        else
        {
            baseName = ROOT_DIR;
            baseNameMap.put(KEY_VALID, new Boolean(false));
            baseNameMap.put(KEY_PATH, baseName);
        }
        return baseNameMap;
    }

    /**
     * Util to turn current DIRECTORY path into Stack so that directory order from TOP of path is present.
     * 
     * @param f
     * @return
     */
    public static Stack getDirectoryStructureStack(File f)
    {
        if (debug)
        {
            logger.log(Level.INFO, "  getDirectoryStructureStack invoked");
            logger.log(Level.INFO, "  fileName = " + f.getName());
            logger.log(Level.INFO, "  f.isDirectory() = " + f.isDirectory());
        }
        // security check to ensure that path outside of ROOT_DIR can never be
        // returned, if pseudochroot true
        if (pseudochroot)
        {
            String f_path = getCanonicalPath(f);
            HashMap baseNameMap = validateBaseName(f_path, ROOT_DIR);
            String validPath = (String) baseNameMap.get(KEY_PATH);
            if (!f_path.equals(validPath))
            {
                f = new File(ROOT_DIR);
            }
        }
        Stack returnStack = new Stack();
        File parentFile = f;
        // each subdir of current dir
        if (parentFile != null)
        {
            while (parentFile.getParentFile() != null)
            {
                // put parent on stack
                returnStack.push(parentFile);
                // return path only upto ROOT_DIR if pseudochroot true, else
                // return em all
                if (pseudochroot)
                {
                    if (getCanonicalPath(parentFile).equalsIgnoreCase(ROOT_DIR))
                    {
                        break;
                    }
                }
                // advance parent
                parentFile = parentFile.getParentFile();
            }
            // root dir AFTER all other dirs (if not pseudochroot)
            if (parentFile.getParentFile() == null)
            {
                returnStack.push(parentFile);
            }
        }
        return returnStack;
    }   

    /**
     * Util to provide HTML with links for current DIRECTORY Stack. LinkPage may be passed to define to which page the
     * anchor tag will link. Action parameter may be passed in an will be appended as HTTP GET to links.
     * 
     * @param f
     * @param linkPage
     * @param action
     * @return
     */
    public static String getDirectoryStackHTML(File f, String linkPage, String action)
    {
        Stack stack = getDirectoryStructureStack(f);
        StringBuffer temp = new StringBuffer();
        // determine page to use for result of anchor, default to FILE_MANAGER
        // if linkPage not present
        String anchorLink = null;
        if (linkPage == null)
            anchorLink = FILE_MANAGER;
        else
            anchorLink = linkPage;
        if (stack != null)
        {
            while (!stack.empty())
            {
                File thisFile = (File) stack.pop();
                String name = null;
                String path = getCanonicalPath(thisFile);
                // the ROOT directory, no parent, link and cleanup path to be
                // used as name (root name does not exist)
                if (thisFile.getParent() == null)
                {
                    name = getCanonicalPath(thisFile);
                    name = name.substring(0, name.length() - 1);
                    // starting from root, root should not be a link
                    if (thisFile == f)
                    {
                        temp.append(name);
                    }
                    // not starting from root, root should be a link
                    else
                    {
                        if (action == null)
                        {
                            temp.append("<a class=\"browsePathBar\" href=\"" + anchorLink + "?BASE_NAME="
                                    + URLEncoder.encode(path) + "\">" + name + "</a>");
                        }
                        else
                        {
                            temp.append("<a class=\"browsePathBar\" href=\"" + anchorLink + "?BASE_NAME="
                                    + URLEncoder.encode(path) + "&ACTION=" + action + "\">" + name + "</a>");
                        }
                        temp.append(" " + FILE_SEP + " ");
                    }
                }
                // non ROOT subdirectories
                else
                {
                    name = thisFile.getName();
                    // current directory (not a link and no sep after)
                    if (thisFile == f)
                    {
                        temp.append(name);
                    }
                    // subdirectory that is not current, link and file sep after
                    else
                    {
                        if (action == null)
                        {
                            temp.append("<a class=\"browsePathBar\" href=\"" + anchorLink + "?BASE_NAME="
                                    + URLEncoder.encode(path) + "\">" + name + "</a>");
                        }
                        else
                        {
                            temp.append("<a class=\"browsePathBar\" href=\"" + anchorLink + "?BASE_NAME="
                                    + URLEncoder.encode(path) + "&ACTION=" + action + "\">" + name + "</a>");
                        }
                        temp.append(" " + FILE_SEP + " ");
                    }
                }
            }
        }
        return temp.toString();
    }

    /**
     * Overload to perform default ACTION. This will NOT APPEND any action which defaults to LIST_DIR.
     * 
     * @param f
     * @return
     */
    public static String getDirectoryStackHTML(File f)
    {
        return getDirectoryStackHTML(f, null, null);
    }

    /**
     * Util to create directory.
     * 
     * @param dir
     * @param directoryName
     *  
     */
    public static void createDirectory(File dir, String directoryName) throws GeneralException
    {
        if (debug)
        {
            logger.log(Level.INFO, "  createDirectory invoked - " + directoryName);
            logger.log(Level.INFO, "   path to create dir in dir.getCanonicalPath - " + getCanonicalPath(dir));
        }
        if ((dir != null) && (!StringUtils.isEmpty(directoryName)))
        {
            if (validFileName(directoryName))
            {
                String newDirName = getCanonicalPath(dir) + FILE_SEP + directoryName;
                File newDir = new File(newDirName);
                if (newDir.exists())
                {
                    throw entityExistsException;
                }
                else
                {
                    newDir.mkdir();
                }
            }
            else
            {
                throw invalidNameException;
            }
        }
        else
        {
            throw parameterException;
        }
    }

    /**
     * Util to delete directory.
     * 
     * @param f
     * @return
     */
    public static void deleteDirectory(File f) throws GeneralException
    {
        if (debug)
            logger.log(Level.INFO, "  deleteDirectory invoked - " + f);
        // ensure is a directory
        if ((f != null) && (f.exists()) && (f.isDirectory()))
        {
            // ensure has parent (so that root can never be deleted)
            if (f.getParentFile() != null)
            {
                try
                {
                    FileUtils.deleteDirectory(f);
                }
                catch (IOException ioe)
                {
                    throw new GeneralException("ERROR - unable to delete directory - " + f.getName());
                }
            }
            else
            {
                throw noParentException;
            }
        }
        else
        {
            throw parameterException;
        }
    }

    /**
     * Util to create file.
     * 
     * @param dir
     * @param fileName
     * @return
     */
    public static void createFile(File dir, String fileName) throws GeneralException
    {
        if (debug)
            logger.log(Level.INFO, "  createFile invoked - " + fileName);
        if ((dir != null) && (fileName != null))
        {
            if (validFileName(fileName))
            {
                String newFileName = getCanonicalPath(dir) + FILE_SEP + fileName;
                File newFile = new File(newFileName);
                if (newFile.exists())
                {
                    throw entityExistsException;
                }
                else
                {
                    try
                    {
                        newFile.createNewFile();
                    }
                    catch (IOException ioe)
                    {
                        throw new GeneralException("ERROR, unable to create new file - " + fileName + " "
                                + ioe.getMessage());
                    }
                }
            }
            else
            {
                throw invalidNameException;
            }
        }
        else
        {
            throw parameterException;
        }
    }

    /**
     * Util to copy file. Copies file within CURRENT DIRECTORY only, using src File f parent as location and String name
     * as name of new File within location.
     * 
     * @param f
     * @param name
     * @return
     */
    public static void copyFile(File f, String fileName) throws GeneralException
    {
        if (debug)
            logger.log(Level.INFO, "  copyFile invoked - " + fileName);
        // make sure there is a parent so that root is not copied ;)
        if ((f != null) && (f.getParentFile() != null))
        {
            if (f.getName().equals(fileName))
            {
                throw entityExistsException;
            }
            else
            {
                if (validFileName(fileName))
                {
                    String newName = getCanonicalPath(f.getParentFile()) + FILE_SEP + fileName;
                    if (debug)
                    {
                        logger.log(Level.INFO, "  file current path = " + getCanonicalPath(f));
                        logger.log(Level.INFO, "  new path name to copy to = " + newName);
                    }
                    try
                    {
                        File newFile = new File(newName);
                        if (newFile.exists())
                        {
                            throw entityExistsException;
                        }
                        else
                        {
                            if (debug)
                                logger.log(Level.INFO, "  attempting copy for " + f.getName() + " to "
                                        + newFile.getName());
                            FileUtils.copyFile(f, newFile);
                        }
                    }
                    catch (IOException ioe)
                    {
                        throw new GeneralException("ERROR unable to create file - " + fileName + " " + ioe.getMessage());
                    }
                }
                else
                {
                    throw invalidNameException;
                }
            }
        }
        else
        {
            throw parameterException;
        }
    }

    /**
     * Util to copy directory. Requires file src and dest (unlike other FileHandlerUtil methods that take src File and
     * dest String).
     * 
     * @param srcFile
     * @param destFile
     * @return
     */
    public static void copyDirectory(File srcFile, File destFile) throws GeneralException
    {
        if (debug)
            logger.log(Level.INFO, "  copyDirectory invoked - SRC = " + getCanonicalPath(srcFile) + " - DEST = "
                    + getCanonicalPath(destFile));
        // make sure there is a parent so that root is not copied ;)
        if ((srcFile != null) && (srcFile.getParentFile() != null))
        {
            try
            {
                if (srcFile.isDirectory())
                {
                    if (validFileName(destFile.getName()))
                    {
                        // create dest dir
                        destFile.mkdirs();
                        // parse directory contents and recursively call for
                        // subdirectories
                        String files[] = srcFile.list();
                        for (int i = 0; i < files.length; i++)
                        {
                            File srcFile1 = new File(srcFile, files[i]);
                            File destFile1 = new File(destFile, files[i]);
                            copyDirectory(srcFile1, destFile1);
                        }
                    }
                    else
                    {
                        throw invalidNameException;
                    }
                }
                else
                {
                    FileUtils.copyFile(srcFile, destFile);
                }
            }
            catch (IOException ioe)
            {
                throw new GeneralException("ERROR unable to copy directory - " + destFile.getName() + " "
                        + ioe.getMessage());
            }
        }
        else
        {
            throw parameterException;
        }
    }

    /**
     * Util to rename file/directory.
     * 
     * @param f
     * @param name
     * @return
     */
    public static void renameFile(File f, String fileName) throws GeneralException
    {
        if (debug)
            logger.log(Level.INFO, "  renameFile invoked - " + fileName);
        // make sure there is a parent so that root is not renamed ;)
        if ((f != null) && (f.getParentFile() != null))
        {
            if (f.getName().equals(fileName))
            {
                throw entityExistsException;
            }
            else
            {
                if (validFileName(fileName))
                {
                    String newName = getCanonicalPath(f.getParentFile()) + FILE_SEP + fileName;
                    File newFile = new File(newName);
                    if (newFile.exists())
                    {
                        throw entityExistsException;
                    }
                    else
                    {
                        f.renameTo(newFile);
                    }
                }
                else
                {
                    throw invalidNameException;
                }
            }
        }
        else
        {
            throw parameterException;
        }
    }

    /**
     * Util to move file/directory.
     * 
     * @param f
     * @param name
     * @return
     */
    public static void moveFile(File destDir, String srcName) throws GeneralException
    {
        if (debug)
            logger.log(Level.INFO, "  moveFile invoked - " + srcName);
        // make sure there is a parent so that root is not moved ;)
        if ((destDir != null) && (destDir.getParentFile() != null))
        {
            if (debug)
            {
                logger.log(Level.INFO, " destDir = " + getCanonicalPath(destDir));
                logger.log(Level.INFO, " srcName = " + srcName);
            }
            // only allow move TO directories
            if (destDir.isDirectory())
            {
                // create file from name
                File srcFile = new File(srcName);
                if ((srcFile != null) && (srcFile.exists()))
                {
                    // move file
                    if (srcFile.isFile())
                    {
                        try
                        {
                            // copy file to directory
                            FileUtils.copyFileToDirectory(srcFile, destDir);
                            // delete source
                            FileUtils.forceDelete(srcFile);
                        }
                        catch (IOException ioe)
                        {
                            throw new GeneralException("ERROR unable to move file - " + srcFile.getName()
                                    + " to destination - " + destDir.getName());
                        }
                    }
                    // move directory
                    else if (srcFile.isDirectory())
                    {
                        try
                        {
                            // create new src within which to place destDir
                            String newDestDirPath = destDir.getCanonicalPath() + FILE_SEP + srcFile.getName();
                            File newDestDir = new File(newDestDirPath);
                            // copy directory to directory
                            copyDirectory(srcFile, newDestDir);
                            // delete src directory
                            FileUtils.deleteDirectory(srcFile);
                        }
                        catch (IOException ioe)
                        {
                            throw new GeneralException("ERROR unable to move directory - " + srcFile.getName()
                                    + " to destination - " + destDir.getName());
                        }
                    }
                }
                else
                {
                    throw new GeneralException("ERROR cannot esablish directory/file to be moved - " + srcName);
                }
            }
            else
            {
                throw new GeneralException("ERROR destination of a MOVE must be a directory");
            }
        }
        else
        {
            throw parameterException;
        }
    }

    /**
     * Util to delete file.
     * 
     * @param f
     * @return
     */
    public static void deleteFile(File f) throws GeneralException
    {
        if ((f == null) || (!f.delete()))
        {
            throw new GeneralException("ERROR unable to delete file - " + f.getName());
        }
    }

    /**
     * Util to delete file list.
     * 
     * @param f
     * @return
     */
    public static void deleteFileList(String[] fileList) throws GeneralException
    {
        if (debug)
            logger.log(Level.INFO, "  deleteFileList invoked");
        StringBuffer sb = new StringBuffer();
        boolean failure = false;
        if (fileList != null)
        {
            int size = fileList.length;
            for (int i = 0; i < size; i++)
            {
                String fileName = URLDecoder.decode(fileList[i]);
                System.out.println("  fileName = " + fileName);
                File f = new File(fileName);
                try
                {
                    deleteFile(f);
                }
                catch (GeneralException ge)
                {
                    System.out.println(ge.getMessage());
                }
            }
        }
    }

    /**
     * Util to process file upload.
     * 
     * @param request
     * @return
     */
    public static HashMap processMultiPartRequest(HttpServletRequest request)
    {
        if (debug)
            logger.log(Level.INFO, "  processMultiPartRequest invoked");
        HashMap map = new HashMap();
        String baseName = null;
        String message = null;
        String uploadFileCompletePath = null;
        try
        {
            boolean isMultipart = FileUpload.isMultipartContent(request);
            if (isMultipart)
            {
                // upload handler
                DiskFileUpload upload = new DiskFileUpload();
                upload.setSizeThreshold(sizeThreshold);
                upload.setSizeMax(sizeMax);
                // get request items as FileItem
                List items = null;
                items = upload.parseRequest(request);
                // process items to get baseName and action and files
                Iterator iter = items.iterator();
                while (iter.hasNext())
                {
                    FileItem item = (FileItem) iter.next();
                    if (item.isFormField())
                    {
                        String fieldName = item.getFieldName();
                        String fieldValue = item.getString();
                        // establish baseName and action from multipart form
                        if (fieldName.equals(BASE_NAME))
                            baseName = fieldValue;
                    }
                    else
                    {
                        String fieldName = item.getFieldName();
                        String fileName = item.getName();
                        if (debug)
                            logger.log(Level.INFO, "  trying to process upload: fileName - " + fileName);
                        // IE leaves the entire original path on the fileName
                        // (the drive name and path of where was uploaded from?, dont know why)
                        // strip the item name to be just the file itself
                        // check for FILE_SEP and FILE_SEP_WINDERS (if the
                        // server is UNIX and client is WINDERS, FILE_SEP alone wont catch it)
                        if (fileName.indexOf(FILE_SEP) != -1)
                        {
                            if (debug)
                                logger
                                        .log(Level.INFO,
                                                " FILE_SEP in fileName, are you using IE again, shame on you, stripping the name");
                            fileName = fileName.substring(fileName.lastIndexOf(FILE_SEP) + 1, fileName.length());
                        }
                        else if (fileName.indexOf(FILE_SEP_WINDERS) != -1)
                        {
                            if (debug)
                                logger
                                        .log(Level.INFO,
                                                " FILE_SEP_WINDERS in fileName, are you using IE again, shame on you, stripping the name");
                            fileName = fileName
                                    .substring(fileName.lastIndexOf(FILE_SEP_WINDERS) + 1, fileName.length());
                        }
                        if (debug)
                            logger.log(Level.INFO, "  upload: fileName AFTER possible windows cleanup - " + fileName);
                        // validate fileName
                        if (validFileName(fileName))
                        {
                            String contentType = item.getContentType();
                            boolean isInMemory = item.isInMemory();
                            long sizeInBytes = item.getSize();
                            if (debug)
                            {
                                logger.log(Level.INFO, "   fieldName = " + fieldName);
                                logger.log(Level.INFO, "   fileName = " + fileName);
                                logger.log(Level.INFO, "   contentType = " + contentType);
                                logger.log(Level.INFO, "   isInMemory = " + isInMemory);
                                logger.log(Level.INFO, "   sizeInBytes = " + sizeInBytes);
                            }
                            //write file
                            uploadFileCompletePath = baseName + FileHandlerUtil.FILE_SEP + fileName;
                            File uploadedFile = new File(uploadFileCompletePath);
                            try
                            {
                                item.write(uploadedFile);
                            }
                            catch (Exception e) // have to catch "Exception"
                            // here because thats what
                            // FileItem throws
                            {
                                message = "*ERROR* unable to upload file - " + fileName;
                            }
                            message = "*SUCCESS* Uploaded File: " + fileName;
                        }
                        else
                        {
                            if (debug)
                                logger.log(Level.INFO, "  UNABLE to upload file, invalid name");
                            message = invalidNameException.getMessage();
                        }
                    }
                }
            }
            else
            {
                message = "*ERROR* Unable to Upload File:  not multipart request.";
            }
        }
        catch (FileUploadException fue)
        {
            message = "*ERROR* unable to upload file - " + fue.getMessage();
        }
        map.put(BASE_NAME, baseName);
        map.put(MESSAGE, message);
        return map;
    }

    /**
     * Util to view character file in JspWriter.
     * 
     * @param response
     * @param out
     * @param f
     *  
     */
    public static void viewTextFileFromJspWriter(HttpServletResponse response, JspWriter out, File f)
    {
        if (debug)
            logger.log(Level.INFO, "  viewTextFileFromJspWriter invoked");
        // get and set mime type
        String mimeType = getMimeType(f);
        response.setContentType(mimeType);
        try
        {
            if ((f != null) && (f.canRead()) && (f.length() > 0))
            {
                // clear out, then read each line of the character file to out
                // using Reader, then flush and close out
                out.clearBuffer();
                FileReader fr = new FileReader(f);
                BufferedReader br = new BufferedReader(fr);
                while (br.ready())
                {
                    out.println(br.readLine());
                }
                out.flush();
                out.close();
            }
            else
            {
                out.clearBuffer();
                out.println("*ERROR* unable to read file or file has zero size.");
                out.flush();
                out.close();
            }
        }
        catch (IOException ioe)
        {
            ioe.printStackTrace();
        }
    }

    /**
     * Util to view binary file in JspWriter. Relies on <code>Writer2Stream</code>.
     * 
     * @param response
     * @param out
     * @param f
     *  
     */
    public static void viewBinaryFileFromJspWriter(HttpServletResponse response, JspWriter out, File f)
    {
        if (debug)
            logger.log(Level.INFO, "  viewBinaryFileFromJspWriter invoked");
        // get and set mime type
        String mimeType = getMimeType(f);
        response.setContentType(mimeType);
        try
        {
            // write binary data bytes from FileInputStream to OutputStream
            // using Writer2Stream
            byte buffer[] = new byte[8 * 1024];
            int b;
            FileInputStream fis = new FileInputStream(f);
            BufferedInputStream fileInput = new BufferedInputStream(fis);
            out.clearBuffer();
            OutputStream viewOut = new Writer2Stream(out);
            while ((b = fileInput.read(buffer)) != -1)
            {
                viewOut.write(buffer, 0, b);
            }
            fileInput.close();
            viewOut.flush();
        }
        catch (IOException ioe)
        {
            ioe.printStackTrace();
        }
    }

    /**
     * Util to get character file contents to edit.
     * 
     * @param fileName
     * @return
     */
    public static String getTextFileContentsForEdit(String fileName) throws GeneralException
    {
        if (debug)
            logger.log(Level.INFO, "  getTextFileContentsForEdit invoked");
        StringBuffer result = new StringBuffer();
        try
        {
            File f = new File(fileName);
            if ((f != null) && (f.canRead()))
            {
                FileReader fr = new FileReader(f);
                BufferedReader br = new BufferedReader(fr);
                while (br.ready())
                {
                    // TODO vist newlines for each system and what to escape
                    // (should likely not escape, for example when entities are
                    // IN text file to be edited by design?)
                    result.append(StringEscapeUtils.escapeHtml(br.readLine()));
                    result.append("\n"); // hard coded for unix newlines only
                }
            }
        }
        catch (IOException ioe)
        {
            throw new GeneralException("*ERROR* unable to get text file contents to edit - " + fileName);
        }
        return result.toString();
    }

    /**
     * Util to create text file (used to create new and or to edit existing).
     * 
     * @param origFileName
     * @param newFileName
     * @param createBak
     * @param fileContents
     * @return
     */
    public static void createTextFile(String origFileName, String newFileName, String createBak, String fileContents)
            throws GeneralException
    {
        if (debug)
        {
            logger.log(Level.INFO, "  createTextFile invoked");
            logger.log(Level.INFO, "  origFileName = " + origFileName);
            logger.log(Level.INFO, "  newFileName = " + newFileName);
            logger.log(Level.INFO, "  createBak = " + createBak);
        }
        File newFile = new File(newFileName);
        if (validFileName(newFile.getName()))
        {
            // security check that new name is inside valid path, only check if
            // pseudochroot enabled
            boolean validBaseName = true;
            if (pseudochroot)
            {
                HashMap baseNameMap = validateBaseName(newFileName, ROOT_DIR);
                Boolean baseMapValid = (Boolean) baseNameMap.get(KEY_VALID);
                if (baseMapValid != null)
                    validBaseName = baseMapValid.booleanValue();
            }
            // process through all checks
            if (validBaseName)
            {
                if (newFile != null)
                {
                    if ((!newFile.exists()) || (newFileName.equals(origFileName)))
                    {
                        try
                        {
                            // if createBak = true then make a .bak file first
                            if ((createBak != null) && (createBak.equals(TRUE)))
                            {
                                createBakFile(origFileName);
                            }
                            // save edited text as file
                            FileWriter fw = new FileWriter(newFile);
                            BufferedWriter bw = new BufferedWriter(fw);
                            String uesc_fileContents = StringEscapeUtils.unescapeHtml(fileContents);
                            StringReader fileText = new StringReader(uesc_fileContents);
                            int i;
                            while ((i = fileText.read()) >= 0)
                            {
                                bw.write(i);
                            }
                            bw.flush();
                            bw.close();
                        }
                        catch (IOException ioe)
                        {
                            throw new GeneralException("ERROR unable to create/save file - " + newFileName + " "
                                    + ioe.getMessage());
                        }
                    }
                    else
                    {
                        throw entityExistsException;
                    }
                }
                else
                {
                    throw parameterException;
                }
            }
            else
            {
                throw rootDirException;
            }
        }
        else
        {
            throw invalidNameException;
        }
    }

    /**
     * Util to resize image using ImageHandlerUtil (which in turn uses JAI).
     * 
     * @param origFileName
     * @param newFileName
     * @param createBak
     * @param dimension
     * @return
     */
    public static void resizeImage(String origFileName, String newFileName, String createBak, String dimension)
            throws GeneralException
    {
        if (debug)
        {
            logger.log(Level.INFO, "  resizeImage invoked");
            logger.log(Level.INFO, "  origFileName = " + origFileName);
            logger.log(Level.INFO, "  newFileName = " + newFileName);
            logger.log(Level.INFO, "  createBak = " + createBak);
            logger.log(Level.INFO, "  dimension = " + dimension);
        }
        File newFile = new File(newFileName);
        if (validFileName(newFile.getName()))
        {
            // security check that new name is inside valid path, only check if
            // pseudochroot enabled
            boolean validBaseName = true;
            if (pseudochroot)
            {
                HashMap baseNameMap = validateBaseName(newFileName, ROOT_DIR);
                Boolean baseMapValid = (Boolean) baseNameMap.get(KEY_VALID);
                if (baseMapValid != null)
                    validBaseName = baseMapValid.booleanValue();
            }
            // process through all checks
            if (validBaseName)
            {
                if (newFile != null)
                {
                    if ((!newFile.exists()) || (newFileName.equals(origFileName)))
                    {
                        try
                        {
                            // if createBak = true then make a .bak file first
                            if ((createBak != null) && (createBak.equals(TRUE)))
                            {
                                createBakFile(origFileName);
                            }
                            // scale
                            ImageHandlerUtil.processImage(origFileName, newFileName, ImageHandlerUtil.SCALE, dimension);
                        }
                        catch (IOException ioe)
                        {
                            throw new GeneralException("*ERROR* unable to resize image: " + origFileName + "<br />"
                                    + ioe.getMessage());
                        }
                    }
                    else
                    {
                        throw entityExistsException;
                    }
                }
                else
                {
                    throw new GeneralException("*ERROR* unable to save edited image due to unable to establish file - "
                            + newFileName);
                }
            }
            else
            {
                throw rootDirException;
            }
        }
        else
        {
            throw invalidNameException;
        }
    }

    /**
     * Util to rotate image using ImageHandlerUtil (which in turn uses JAI).
     * 
     * @param origFileName
     * @param newFileName
     * @param createBak
     * @param amount
     * @return
     */
    public static void rotateImage(String origFileName, String newFileName, String createBak, String amount)
            throws GeneralException
    {
        if (debug)
        {
            logger.log(Level.INFO, "  rotateImage invoked");
            logger.log(Level.INFO, "  origFileName = " + origFileName);
            logger.log(Level.INFO, "  newFileName = " + newFileName);
            logger.log(Level.INFO, "  createBak = " + createBak);
            logger.log(Level.INFO, "  amount = " + amount);
        }
        File newFile = new File(newFileName);
        if (validFileName(newFile.getName()))
        {
            // security check that new name is inside valid path, only check if
            // pseudochroot enabled
            boolean validBaseName = true;
            if (pseudochroot)
            {
                HashMap baseNameMap = validateBaseName(newFileName, ROOT_DIR);
                Boolean baseMapValid = (Boolean) baseNameMap.get(KEY_VALID);
                if (baseMapValid != null)
                    validBaseName = baseMapValid.booleanValue();
            }
            // process through all checks
            if (validBaseName)
            {
                if (newFile != null)
                {
                    if ((!newFile.exists()) || (newFileName.equals(origFileName)))
                    {
                        try
                        {
                            // if createBak = true then make a .bak file first
                            if ((createBak != null) && (createBak.equals(TRUE)))
                            {
                                createBakFile(origFileName);
                            }
                            // rotate
                            ImageHandlerUtil
                                    .processImage(origFileName, newFileName, ImageHandlerUtil.TRANSPOSE, amount);
                        }
                        catch (IOException ioe)
                        {
                            throw new GeneralException("*ERROR* unable to rotate image: " + origFileName + " "
                                    + ioe.getMessage());
                        }
                    }
                    else
                    {
                        throw entityExistsException;
                    }
                }
                else
                {
                    throw new GeneralException("*ERROR* unable to save edited image due to unable to establish file - "
                            + newFileName);
                }
            }
            else
            {
                throw rootDirException;
            }
        }
        else
        {
            throw invalidNameException;
        }
    }

    /**
     * 
     * @param fileName
     * @throws IOException
     *  
     */
    public static void createBakFile(String fileName) throws IOException
    {
        String bakFileName = fileName + ".bak";
        File origFile = new File(fileName);
        File bakFile = new File(bakFileName);
        if ((origFile != null) && (origFile.exists()) && (origFile.canRead()) && (origFile.canWrite()))
        {
            FileUtils.copyFile(origFile, bakFile);
        }
    }

    /**
     * Util to validate that a fileName is acceptable. Based on pseudo UNIX filesystem rules (with space allowed against
     * better judgement because its so bastardized/adopted by windows world).
     * 
     * must start alphanumeric rest allowed are alphanumeric, underscore, period and space
     * 
     * @param name
     * @return
     */
    public static boolean validFileName(String name)
    {
        boolean result = false;
        final String VALID_CHARS = "abcdefghijklmnopqrstuvwxyz1234567890-_. ";
        if (!StringUtils.isEmpty(name))
        {
            if ((name.indexOf(FILE_SEP) == -1) && (StringUtils.containsOnly(name.toLowerCase(), VALID_CHARS)))
            {
                result = true;
            }
        }
        return result;
    }

    /**
     * Util to check for image content WITHIN compressed files.
     * NOTE - only checks the ROOT of the directory structure within the compressed file
     * does NOT search down the compressed file path any farther than root.  
     * 
     * @param f
     * @return
     */
    public static boolean compressedFileHasImageContent(File f)
    {
        boolean returnValue = false;
        ArrayList zipContents = ZipHandlerUtil.getZipFileContents(f);
        int size = 0;
        if (zipContents != null)
            size = zipContents.size();
        for (int i = 0; i < size; i++)
        {
            String entry = (String) zipContents.get(i);
            if (isFileType(entry, EDIT_IMAGE))
            {
                returnValue = true;
                break;
            }
        }
        return returnValue;
    }

    /**
     * Util to uncompress file, relies on <code>ZipHandlerUtil</code>.
     * 
     * @param f
     * @return
     */
    public static void unCompressFile(File f) throws GeneralException
    {
        if ((f == null) || (!f.exists()) || (!isFileType(f, COMP)))
        {
            throw new GeneralException("ERROR unable to unzip file - " + f.getName());
        }
        else
        {
            ZipHandlerUtil.unZip(f);
        }
    }

    /**
     * Util to view compressed file contents.
     * 
     * @param response
     * @param out
     * @param f
     *  
     */
    public static void viewCompressedFileContentsFromJspWriter(JspWriter out, File f)
    {
        if (debug) logger.log(Level.INFO, "  viewCompressedFileContentsFromJspWriter invoked");
        
        try
        {
            out.clearBuffer();
            out.print(getHTMLHeader(f.getCanonicalPath()));
            out.println("<br /><font face=\"arial, helvetica\">");
            out.println("Displaying compressed file contents:");
            out.println("<table width=\"90%\" border=\"0\">");
            ArrayList zipEntries = ZipHandlerUtil.getZipFileContents(f);
            int number = 0;
            for (int i = 0; i < zipEntries.size(); i++)
            {
                number++;
                String entryName = (String) zipEntries.get(i);
                out.println("<tr><td width=\"15\" bgcolor=\"#cccccc\">" + number + "</td><td bgcolor=\"#cccccc\">"
                        + entryName + "</td></tr>");
            }
            out.println("</table>");
            out.println("</font>");
            out.print(getHTMLFooter());
            out.flush();
            out.close();
        }
        catch (IOException ioe)
        {
            ioe.printStackTrace();
        }
    }
    
    /**
     * Util to compress files, relies on <code>ZipHandlerUtil</code>.
     * 
     * @param fileList
     * @param newFileName
     * @param baseParent
     * @return
     */
    public static void createCompressedFile(String[] fileList, String newFileName, String baseName) throws GeneralException
    {
        if (debug) logger.log(Level.INFO, "  createCompressedFile invoked");
        
        if ((fileList == null) || (newFileName == null) || (baseName == null))
        {
            throw new GeneralException("ERROR unable to create zip file.");
        }
        else if (!validFileName(newFileName))
        {
            throw new GeneralException("ERROR unable to create zip file, invalid new file name specified - " + newFileName);
        }
        else
        {
            // add the newFileName to the specified baseParent to create entire path to new file name to create
            String newFileName_full = baseName + FILE_SEP + newFileName;
            
            if (debug)
            {
                logger.log(Level.INFO, "  baseName = " + baseName);
                logger.log(Level.INFO, "  newFileName = " + newFileName);
                logger.log(Level.INFO, "  newFileName_full = " + newFileName_full);                
            }
            
            // parse the fileList[] String array into an ArrayList of decoded names for ZipHandlerUtil
            ArrayList fileArrayList = new ArrayList();
            int size = fileList.length;
            for (int i = 0; i < size; i++)
            {
                String thisFile = URLDecoder.decode(fileList[i]);
                fileArrayList.add(thisFile);
            }
            
            // call ZipHandlerUtil to do the processing
            ZipHandlerUtil.zip(fileArrayList, newFileName_full);
        }
    }

    /**
     * 
     * @param response
     * @param out
     * @param f
     */
    public static void downloadFile(HttpServletResponse response, JspWriter out, File f)
    {
        if (debug) logger.log(Level.INFO, "  downloadFile invoked");
        
        String fileName = f.getName();
        // setup response
        response.setContentType("application/octet-stream");
        response.setHeader("Content-Disposition", "attachment;filename=\"" + fileName + "\"");
        response.setContentLength((int) f.length());
        // process io
        try
        {
            byte buffer[] = new byte[8 * 1024];
            int b;
            FileInputStream fis = new FileInputStream(f);
            BufferedInputStream fileInput = new BufferedInputStream(fis);
            out.clearBuffer();
            OutputStream viewOut = new Writer2Stream(out);
            while ((b = fileInput.read(buffer)) != -1)
            {
                viewOut.write(buffer, 0, b);
            }
            fileInput.close();
            viewOut.flush();
        }
        catch (IOException ioe)
        {
            ioe.printStackTrace();
        }
    }

    /**
     * Sort using contained Comparator class for file name.
     * 
     * @param files
     *  
     */
    public static void sortFilesByName(List files)
    {
        Collections.sort(files, new FileNameComparator());
    }

    /**
     * Sort using contained Comparator class for directories first in directories/files list.
     * 
     * @param files
     *  
     */
    public static void sortFilesDirFirst(List files)
    {
        Collections.sort(files, new FileDirFirstComparator());
    }

    /**
     * Sort using contained Comparator class for files first in directory/files list.
     * 
     * @param files
     *  
     */
    public static void sortFilesFileFirst(List files)
    {
        Collections.sort(files, new FileFileFirstComparator());
    }

    /**
     * HTMLHeader
     * 
     * @return
     */
    public static String getHTMLHeader(String baseName)
    {
        String baseFileDisplayName = baseName.substring(baseName.lastIndexOf(FILE_SEP) + 1, baseName.length());        
        StringBuffer HTMLHeader = new StringBuffer();
        // header
        HTMLHeader.append("\n<html>\n");
        HTMLHeader.append("<head>\n");
        HTMLHeader.append("<title> " + baseFileDisplayName + " </title>\n");
        HTMLHeader.append("</head>\n");
        HTMLHeader.append("<body>\n");
        HTMLHeader.append("<font face=\"arial, helvetica\">" + baseFileDisplayName + "</font>\n");
        HTMLHeader.append("<br />");
        return HTMLHeader.toString();
    }

    /**
     * HTMLFooter
     * 
     * @return
     */
    public static String getHTMLFooter()
    {
        StringBuffer HTMLFooter = new StringBuffer();
        // footer
        HTMLFooter.append("<br />");
        HTMLFooter.append("\n</body>\n");
        HTMLFooter.append("</html>\n");
        return HTMLFooter.toString();
    }

    /**
     * 
     * @param dirPath
     * @return
     */
    public static boolean dirExists(String dirPath)
    {
        boolean returnValue = false;
        if (dirPath != null)
        {
            File dir = new File(dirPath);
            if (dir.exists())
                returnValue = true;
        }
        return returnValue;
    }

    /**
     * 
     * @param s
     * @return
     */
    public static String getExtension(String s)
    {
        String returnValue = null;
        if (s != null)
        {
            int index = s.lastIndexOf('.');
            if (index != -1)
            {
                returnValue = s.substring(index + 1);
            }
        }
        return returnValue;
    }

    /**
     * 
     * @param s
     * @return
     */
    public static String removeExtension(String s)
    {
        String returnValue = null;
        if (s != null)
        {
            int index = s.lastIndexOf('.');
            if (index != -1)
            {
                returnValue = s.substring(0, index);
            }
        }
        return returnValue;
    }
    
    /**
     * Get file canonical path, util method to handle exceptions in single location
     * as File.getCanonicalPath throws IOE.
     * 
     * @param f
     * @return
     */
    public static String getCanonicalPath(File f)
    {
       String path = null;
       try
       {
           if (f != null)
           {
               path = f.getCanonicalPath();               
           }
       }
       catch (IOException ioe)
       {
           System.out.println("  ERROR - " + ioe.getMessage());
       }
       return path;       
    }    
}
//
// additional classes
//
// file comparators

class FileNameComparator implements Comparator
{
    public int compare(Object o1, Object o2)
    {
        File f1 = (File) o1;
        File f2 = (File) o2;
        return f1.getName().compareToIgnoreCase(f2.getName());
    }
}

class FileDirFirstComparator implements Comparator
{
    public int compare(Object o1, Object o2)
    {
        int result = 1;
        File f1 = (File) o1;
        File f2 = (File) o2;
        if ((f1.isDirectory()) && (!f2.isDirectory()))
            result = 0;
        return result;
    }
}

class FileFileFirstComparator implements Comparator
{
    public int compare(Object o1, Object o2)
    {
        int result = 0;
        File f1 = (File) o1;
        File f2 = (File) o2;
        if ((f1.isDirectory()) && (!f2.isDirectory()))
            result = 1;
        return result;
    }
}
// file filters

class FileFilter_DIR implements FileFilter
{
    public boolean accept(java.io.File f)
    {
        return f.isDirectory();
    }
}

class FileFilter_FILE implements FileFilter
{
    public boolean accept(java.io.File f)
    {
        return f.isFile();
    }
}

class FileFilter_IMAGE implements FileFilter
{
    public boolean accept(java.io.File f)
    {
        boolean returnValue = false;
        if ((FileHandlerUtil.isFileType(f, FileHandlerUtil.EDIT_IMAGE))
                || (FileHandlerUtil.isFileType(f, FileHandlerUtil.EDIT_IMAGE)))
        {
            returnValue = true;
        }
        return returnValue;
    }
}

class FileFilter_COMP implements FileFilter
{
    public boolean accept(java.io.File f)
    {
        boolean returnValue = false;
        if (FileHandlerUtil.isFileType(f, FileHandlerUtil.COMP))
        {
            returnValue = true;
        }
        return returnValue;
    }
}

class FileFilter_COMP_IMAGE implements FileFilter
{
    public boolean accept(java.io.File f)
    {
        boolean returnValue = false;
        if (FileHandlerUtil.isFileType(f, FileHandlerUtil.COMP))
        {
            if (FileHandlerUtil.compressedFileHasImageContent(f))
                returnValue = true;
        }
        return returnValue;
    }
}

class FileFilter_HTML implements FileFilter
{
    public boolean accept(java.io.File f)
    {
        boolean returnValue = false;
        if (FileHandlerUtil.getMimeType(f).equals(FileHandlerUtil.TEXT_HTML))
        {
            returnValue = true;
        }
        return returnValue;
    }
}
/**
 * Taken from: jsp File browser 1.0RC2 Copyright (C) 2003, Boris von Loesch GNU GPL
 * 
 * Wrapperclass to wrap an OutputStream around a Writer
 */

class Writer2Stream extends OutputStream
{
    Writer out;

    Writer2Stream(Writer w)
    {
        super();
        out = w;
    }

    public void write(int i) throws IOException
    {
        out.write(i);
    }

    public void write(byte[] b) throws IOException
    {
        for (int i = 0; i < b.length; i++)
        {
            int n = b[i];
            //Convert byte to ubyte
            n = ((n >>> 4) & 0xF) * 16 + (n & 0xF);
            out.write(n);
        }
    }

    public void write(byte[] b, int off, int len) throws IOException
    {
        for (int i = off; i < off + len; i++)
        {
            int n = b[i];
            n = ((n >>> 4) & 0xF) * 16 + (n & 0xF);
            out.write(n);
        }
    }
} //End of class Writer2Stream
