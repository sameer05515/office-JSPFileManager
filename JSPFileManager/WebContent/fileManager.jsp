<%
response.setHeader("Cache-Control","no-cache"); //HTTP 1.1
response.setHeader("Pragma","no-cache"); //HTTP 1.0
response.setDateHeader ("Expires", 0); //prevents caching at the proxy server
%>
<%@page import="java.util.*" %>
<%@page import="java.text.*" %>
<%@page import="java.io.*" %>
<%@page import="java.net.*" %>
<%@page import="javax.media.jai.operator.*" %>
<%@page import="org.apache.commons.fileupload.*" %>
<%@page import="org.apache.commons.io.*" %>
<%@page import="org.apache.commons.lang.*" %>
<%@page import="org.vaya.assetmanager.util.*" %>
<%!
// const
String BASE_NAME = "BASE_NAME";
String ACTION = "ACTION";
String MESSAGE = "MESSAGE";
String SUCCESS = "SUCCESS";
String ERROR = "ERROR";
String LIST_DIR = "LIST_DIR";
String LIST_FILES = "LIST_FILES";
String FILE_LIST = "FILE_LIST";
String FILE_CHECK = "FILE_CHECK";
String UPLOAD_FILE = "UPLOAD_FILE"; 
String DELETE_FILE = "DELETE_FILE";
String DELETE_DIRECTORY = "DELETE_DIRECTORY";
String DELETE_FILE_PROCESS = "DELETE_FILE_PROCESS";
String DELETE_DIRECTORY_PROCESS = "DELETE_DIRECTORY_PROCESS";
String RENAME_FILE = "RENAME_FILE";
String RENAME_FILE_PROCESS = "RENAME_FILE_PROCESS";
String RENAME_FILE_NAME = "FILE_RENAME_NAME";
String CREATE_DIRECTORY = "CREATE_DIRECTORY";
String CREATE_DIRECTORY_NAME = "CREATE_DIRECTORY_NAME";
String CREATE_FILE = "CREATE_FILE";
String CREATE_FILE_NAME = "CREATE_FILE_NAME";
String DOWNLOAD_FILE = "DOWNLOAD_FILE";
String EDIT_FILE = "EDIT_FILE";
String EDIT_FILE_SAVE = "EDIT_FILE_SAVE";
String EDIT_FILE_SAVE_NAME = "EDIT_FILE_SAVE_NAME";
String EDIT_FILE_TEXT = "EDIT_FILE_TEXT";
String EDIT_FILE_BAK = "EDIT_FILE_BAK";
String COPY_FILE = "COPY_FILE";
String COPY_FILE_PROCESS = "COPY_FILE_PROCESS";
String COPY_FILE_NAME = "COPY_FILE_NAME";
String COPY_DIRECTORY = "COPY_DIRECTORY";
String COPY_DIRECTORY_PROCESS = "COPY_DIRECTORY_PROCESS";
String COPY_DIRECTORY_NAME = "COPY_DIRECTORY_NAME";
String MOVE_FILE = "MOVE_FILE";
String MOVE_FILE_PROCESS = "MOVE_FILE_PROCESS";
String MOVE_FILE_NAME = "MOVE_FILE_NAME";
String MOVE_DIRECTORY = "MOVE_DIRECTORY";
String MOVE_DIRECTORY_PROCESS = "MOVE_DIRECTORY_PROCESS";
String MOVE_DIRECTORY_NAME = "MOVE_DIRECTORY_NAME";
String DISPLAY_CHARACTER = "DISPLAY_CHARACTER";
String DISPLAY_BINARY = "DISPLAY_BINARY";
String DISPLAY_COMPRESS_CONTENTS = "DISPLAY_COMPRESS_CONTENTS";
String UNCOMPRESS_FILE = "UNCOMPRESS_FILE";
String UNCOMPRESS_FILE_PROCESS = "UNCOMPRESS_FILE_PROCESS";
String IMAGE_EDIT = "IMAGE_EDIT";
String IMAGE_RESIZE = "IMAGE_RESIZE";
String IMAGE_RESIZE_PROCESS = "IMAGE_RESIZE_PROCESS";
String IMAGE_RESIZE_DIMENSION = "IMAGE_RESIZE_DIMENSION";
String IMAGE_RESIZE_BAK = "IMAGE_RESIZE_BAK";
String IMAGE_RESIZE_SAVE_NAME = "IMAGE_RESIZE_SAVE_NAME";
String IMAGE_ROTATE = "IMAGE_ROTATE";
String IMAGE_ROTATE_PROCESS = "IMAGE_ROTATE_PROCESS";
String IMAGE_ROTATE_AMOUNT = "IMAGE_ROTATE_AMOUNT";
String IMAGE_ROTATE_BAK = "IMAGE_ROTATE_BAK";
String IMAGE_ROTATE_SAVE_NAME = "IMAGE_ROTATE_SAVE_NAME";
String IMAGE_GALLERY_CREATE = "IMAGE_GALLERY_CREATE";
String HEIGHT = "HEIGHT";
String WIDTH = "WIDTH";
String SIZE = "SIZE";
String LIST_DIR_FILES = "LIST_DIR_FILES";
String SORT_FILTER = "SORT_FILTER";
String SORT_ALPHA = "ALPHA";
String SORT_DIR = "DIR";
String SORT_FILE = "FILE";
String DIR_ONLY = "DIR_ONLY";
String FILE_ONLY = "FILE_ONLY";
String DELETE_MULTI = "DELETE_MULTI";
String DELETE_MULTI_PROCESS = "DELETE_MULTI_PROCESS";
String CREATE_ZIP_MULTI = "CREATE_ZIP_MULTI";
String CREATE_ZIP_MULTI_NAME = "CREATE_ZIP_MULTI_NAME";
String CREATE_ZIP_MULTI_PROCESS = "CREATE_ZIP_MULTI_PROCESS";
String CREATE_GALLERY_MULTI = "CREATE_GALLERY_MULTI";
String CREATE_GALLERY_MULTI_PROCESS = "CREATE_GALLERY_MULTI_PROCESS";
String CREATE_GALLERY_NAME = "CREATE_GALLERY_NAME";
String CREATE_GALLERY_BACKGROUND = "CREATE_GALLERY_BACKGROUND";
String CREATE_GALLERY_FONT_COLOR = "CREATE_GALLERY_FONT_COLOR";
String CREATE_GALLERY_FONT_SIZE = "CREATE_GALLERY_FONT_SIZE";
String CREATE_GALLERY_FONT_WEIGHT = "CREATE_GALLERY_FONT_WEIGHT";
String CREATE_GALLERY_FONT_FAMILY = "CREATE_GALLERY_FONT_FAMILY";
String CREATE_GALLERY_BORDER_COLOR = "CREATE_GALLERY_BORDER_COLOR";
String CREATE_GALLERY_BORDER_STYLE = "CREATE_GALLERY_BORDER_STYLE";
String CREATE_GALLERY_BORDER_WIDTH = "CREATE_GALLERY_BORDER_WIDTH";
String CREATE_GALLERY_BORDER_IMAGES = "CREATE_GALLERY_GALLERY_BORDER_IMAGES";
String CREATE_GALLERY_TEXT = "CREATE_GALLERY_TEXT";
String CREATE_GALLERY_COPYRIGHT_TEXT = "CREATE_GALLERY_COPYRIGHT_TEXT";
String CREATE_GALLERY_THUMB_SIZE = "CREATE_GALLERY_THUMB_SIZE";
String CREATE_GALLERY_FULL_SIZE = "CREATE_GALLERY_FULL_SIZE";
%>
<%
if (FileHandlerUtil.getDebug()) System.out.println("\nfileManager.jsp BEGIN");

// global members
String CONTEXT_PATH = request.getContextPath();
String IMAGE_PATH = CONTEXT_PATH + "/images";
String BASE_DIR = FileHandlerUtil.getRootDir();
String baseName = BASE_DIR;
File baseFile = null;
String basePath = null;
String baseParent = null;
String action = LIST_DIR;
String fieldName = null;
String fileName = null;
String message = null;
boolean showImageGalLink = false;

// check request as multipart, establish baseName and action from multiplart AND process file
boolean isMultipart = FileUpload.isMultipartContent(request);
if (isMultipart)
{
    HashMap multiPartMap = FileHandlerUtil.processMultiPartRequest(request);    
    baseName = (String) multiPartMap.get(BASE_NAME);
    message = (String) multiPartMap.get(MESSAGE);    
}

// establish baseName action and message from standard POST
else
{
    if (request.getParameter(BASE_NAME) != null) baseName = (String) request.getParameter(BASE_NAME);
    if (request.getParameter(ACTION) != null) action = (String) request.getParameter(ACTION);
    if (request.getParameter(MESSAGE) != null) message = (String) request.getParameter(MESSAGE);
    
    // check for multi file list actions (based on buttons associated with fileList checkboxes)
    if (request.getParameter(DELETE_MULTI) != null) action = DELETE_MULTI;
    else if (request.getParameter(CREATE_ZIP_MULTI) != null) action = CREATE_ZIP_MULTI;
    else if (request.getParameter(CREATE_GALLERY_MULTI) != null) action = CREATE_GALLERY_MULTI;
}

// check baseName to ensure security access is established if pseudochroot enabled
if (FileHandlerUtil.getPseudoChroot())
{
    HashMap baseNameMap = FileHandlerUtil.validateBaseName(baseName, FileHandlerUtil.getRootDir());
    baseName = (String) baseNameMap.get(FileHandlerUtil.KEY_PATH);
    Boolean validBaseName = (Boolean) baseNameMap.get(FileHandlerUtil.KEY_VALID);
    if (!validBaseName.booleanValue())
    {
        System.out.println("\n   SECURITY ACCESS WARNING - attempt to access directory OUTSIDE ROOT_DIR with psuedochroot enabled.");
        System.out.println("   LOGGING remote ip address - " + request.getRemoteAddr());
        System.out.println("\n");    
    }    
} 

// debug
if (FileHandlerUtil.getDebug())
{
    System.out.println("  action = " + action);
    System.out.println("  baseName = " + baseName);
    System.out.println("  message = " + message);
}

// setup base file based on baseName
baseFile = new File(baseName);
basePath = baseFile.getAbsolutePath();
baseParent = baseFile.getParent();

// est presence of message
boolean messagePresent = false;
if (!StringUtils.isEmpty(message))
{
    messagePresent = true;
}
%>
<html>
<head>
<link rel="stylesheet" href="css/style.css" type="text/css" />
<script language="javascript" src="<%= CONTEXT_PATH %>/js/functions.js" type="text/javascript" /></script>
<script language="javascript" src="<%= CONTEXT_PATH %>/js/anchorPos.js"></script>
<script language="javascript"src="<%= CONTEXT_PATH %>/js/colorPicker.js"></script>
<script language="javascript" src="<%= CONTEXT_PATH %>/js/popupWindow.js"></script>
<script language="javascript" src="<%= CONTEXT_PATH %>/js/objectManip.js"></script>
<title>JSP File Manager - <%= basePath %></title>
</head>
<body>
<span class="heading">File Manager</span>
<% 
//
// LIST_DIR
//
if (action.equals(LIST_DIR))
{    
    // check for filter
    String sortFilter = SORT_DIR;
    if (request.getParameter(SORT_FILTER) != null)
    {
       sortFilter = request.getParameter(SORT_FILTER);
       System.out.println("  sortFilter = " + sortFilter); 
    }    
%>

<!-- OUTER TABLE -->
<table width="100%" align="center" border="0">
<tr>
    <td>    
        
        <!-- FILE TASKS TABLE -->      
        <table class="taskTable" name="dirList" align="left" width="50%" border="0">                               
            <tr> 
                <td>Sort/Filter By:</td>
                <td>    
                    <!-- SORT FILTER -->
                    <form name="sort_filter" method="post" action="fileManager.jsp">
                    <select name="SORT_FILTER">
                    <% if (sortFilter.equals(SORT_ALPHA)) { %>
                    <option value="<%= SORT_ALPHA %>" selected="true">Alphabetically</option>
                    <% } else { %>    
                    <option value="<%= SORT_ALPHA %>">Alphabetically</option>
                    <% } if (sortFilter.equals(SORT_DIR)) { %>
                    <option value="<%= SORT_DIR %>" selected="true">Directories First</option>      
                    <% } else { %>   
                    <option value="<%= SORT_DIR %>">Directories First</option>        
                    <% } if (sortFilter.equals(SORT_FILE)) { %>
                    <option value="<%= SORT_FILE %>" selected="true">Files First</option>      
                    % } else { %>   
                    <option value="<%= SORT_FILE %>">Files First</option>        
                    <% } if (sortFilter.equals(DIR_ONLY)) { %>
                    <option value="<%= DIR_ONLY %>" selected="true">Directories Only</option>        
                    <% } else { %>   
                    <option value="<%= DIR_ONLY %>">Directories Only</option>        
                    <% } if (sortFilter.equals(FILE_ONLY)) { %>
                    <option value="<%= FILE_ONLY %>" selected="true">Files Only</option>        
                    <% } else {  %>   
                    <option value="<%= FILE_ONLY %>">Files Only</option>        
                    <% } %>      
                    </select>
                    <input type="hidden" name="BASE_NAME" value="<%= baseName %>" />
                    <input class="cssbtn btn_secondary" type="submit" name="submit" value="go" />
                    </form>
                    <!-- END SORT FILTER -->
                </td>                
            </tr>        
            <tr>
                <td>Upload File:</td>
                <td>
                    <!-- FILE UPLOAD -->
                    <form name="upload" enctype="multipart/form-data" method="post" action="fileManager.jsp">    
                    <input type="hidden" name="BASE_NAME" value="<%= baseName %>" />
                    <input size="30" type="file" name="uploadFile" /> <input class="cssbtn btn_secondary" type="submit" name="submit" value="go" />    
                    </form>    
                    <!-- END FILE UPLOAD -->
                 </td>                 
             </tr>
             <tr>
                <td>Create New Directory:</td>
                <td>    
                    <!-- CREATE DIRECTORY -->
                    <form name="createDirecotory" method="post" action="fileManager.jsp">    
                    <input type="hidden" name="BASE_NAME" value="<%= baseName %>" />
                    <input type="hidden" name="<%= ACTION %>" value="<%= CREATE_DIRECTORY %>" />    
                    <input type="text" name="<%= CREATE_DIRECTORY_NAME %>" /> <input class="cssbtn btn_secondary" type="submit" name="submit" value="go" />    
                    </form>    
                    <!-- CREATE DIRECTORY -->                 
                 </td>
             </tr>
             <tr>
                <td>Create New File:</td>
                <td>    
                    <!-- CREATE FILE -->
                    <form name="createFile" method="post" action="fileManager.jsp">    
                    <input type="hidden" name="BASE_NAME" value="<%= baseName %>" />
                    <input type="hidden" name="<%= ACTION %>" value="<%= CREATE_FILE %>" />    
                    <input type="text" name="<%= CREATE_FILE_NAME %>" /> <input class="cssbtn btn_secondary" type="submit" name="submit" value="go" />   
                    </form>    
                    <!-- CREATE FILE -->    
                </td>                
            </tr>                                
        </table>    
        <!-- END FILE TASKS TABLE -->
        
        <!-- MESSAGE TABLE -->
        <table class="messageMajor" name="dirList" align="left" width="40%" border="0">
            <% if (messagePresent) { %>                    
            <tr>
                <td>                    
                    <% if (StringUtils.contains(message, SUCCESS)) { %>
                    <%= message %>    
                    <% } else { %>
                    <font color="red"><%= message %></font>    
                    <% } %>                    
                </td>
            </tr>
            <tr>                
                <td> 
                    <a class="cssbtn btn_secondary" href="fileManager.jsp?<%= BASE_NAME %>=<%= baseName %>">Clear Message</a>                                 
                </td>                
            </tr>
            <% } else { %>
            <tr>
                <td>&#160</td>
            </tr>    
            <% } %>    
        </table>             
        <!-- END MESSAGE TABLE -->
        
     
    </td>
</tr>
<tr>
    <td>   
    
        <!-- FILE ACTION TABLE -->    
        <form name="fileAction" method="post" action="fileManager.jsp">
        <table class="actionTable" name="dirList" cellpadding="2" cellspacing="0" align="left" width="90%" border="0">
    
            <tr>
                <td class="browsePathBar" colspan="11">                    
                    <b>Current Directory</b>: 
                    <%    
                        // display current starting directory
                        out.println(FileHandlerUtil.getDirectoryStackHTML(baseFile));              
                    %>                    
                </td>                
            </tr>    
            <% if (FileHandlerUtil.getPseudoChroot())
            { %>
            <tr>
                <td colspan="11" align="right">
                    <span class="messageMinor"><b>*pseudochroot enabled*</b></span>                    
                </td>
            </tr>    
            <% } %>            
            </tr>                     
            <tr bgcolor="#ffffff"><td colspan="11"><img src="<%= IMAGE_PATH %>/1.gif" height="3" width="1" border="0" alt="" /></td></tr>                   
          
            <!-- column labels -->
            <tr>
                <td>&#160;</td>
                <td>name</td>
                <td>type</td>
                <td>last modified</td>
                <td>size</td>
                <td colspan="6">actions</td>
            </tr>
            
            <tr bgcolor="#ffffff"><td colspan="11"><img src="<%= IMAGE_PATH %>/1.gif" height="3" width="1" border="0" alt="" /></td></tr>                   
            
            <%
            // get directory contents
            List allContents = FileHandlerUtil.getList(baseName);
            List dirContents = FileHandlerUtil.parseDirList(allContents);
            List fileContents = FileHandlerUtil.parseFileList(allContents);
            
            if (allContents != null) FileHandlerUtil.sortFilesByName(allContents);
            if (dirContents != null) FileHandlerUtil.sortFilesByName(dirContents);
            if (fileContents != null) FileHandlerUtil.sortFilesByName(fileContents);
               
            // apply sort
            if (sortFilter.equals(SORT_ALPHA))
            {        
                // nothing to see here
            }
            else if (sortFilter.equals(SORT_DIR))
            {        
                allContents = new ArrayList();
                if (dirContents != null) allContents.addAll(dirContents);
                if (fileContents != null) allContents.addAll(fileContents);
            }
            else if (sortFilter.equals(SORT_FILE))
            {        
                allContents = new ArrayList();
                if (fileContents != null) allContents.addAll(fileContents);
                if (dirContents != null) allContents.addAll(dirContents);        
            }
            else if (sortFilter.equals(DIR_ONLY))
            {   
            	allContents = dirContents;                
            }
            else if (sortFilter.equals(FILE_ONLY))
            {     
                allContents = fileContents;        
            }    
    
            // parse all files and display directory list
            int size = allContents.size();
            if (size > 0)
            {
                for (int i = 0; i < size; i++)
                {   
                %>
                <tr bgcolor="#ffffff" onMouseOver="this.bgColor='#ece6e6';" onMouseOut="this.bgColor='#ffffff';">
                <%
        
                // this file props
                File thisFile = (File) allContents.get(i);        
                String path = FileHandlerUtil.getCanonicalPath(thisFile);                
                String name = thisFile.getName();
                String fileSize = FileUtils.byteCountToDisplaySize(thisFile.length());
                Calendar lastModCal = Calendar.getInstance();
                lastModCal.setTime(new Date(thisFile.lastModified()));
                DateFormat df = new SimpleDateFormat("dd.MM.yyyy mm:ss");
                String lastModifiedDate = df.format(lastModCal.getTime());                
                String escapedPath = StringEscapeUtils.escapeJavaScript(path);
                String encodedPath = URLEncoder.encode(path);
                String encodedEscapedPath = URLEncoder.encode(escapedPath);                
                            
                // process dir
                if (thisFile.isDirectory())
                {
                %>    
                    <td align="center"><a href="fileManager.jsp?BASE_NAME=<%= URLEncoder.encode(path) %>"><img src="images/folder_closed_ico.jpg" border="0" alt="Select Directory" /></a></td>    
                    <td><a href="fileManager.jsp?BASE_NAME=<%= URLEncoder.encode(path) %>"><%= name %></a></td>
                    <td>DIR</td>
                    <td>&#160;</td>
                    <td>&#160;</td>
                    <td><a class="cssbtn" href="fileManager.jsp?BASE_NAME=<%= encodedPath %>&<%= ACTION %>=<%= COPY_DIRECTORY %>">Copy</a></td>
                    <td><a class="cssbtn" href="fileManager.jsp?BASE_NAME=<%= encodedPath %>&<%= ACTION %>=<%= MOVE_FILE %>&<%= MOVE_FILE_NAME %>=<%= encodedPath %>">Move</a></td>
                    <td><a class="cssbtn" href="fileManager.jsp?BASE_NAME=<%= encodedPath %>&<%= ACTION %>=<%= RENAME_FILE %>">Rename</a></td>                    
                    <td><a class="cssbtn" href="fileManager.jsp?BASE_NAME=<%= encodedPath %>&<%= ACTION %>=<%= DELETE_DIRECTORY %>">Delete</a></td>                    
                    <td>&#160;</td>                    
                    <td>&#160;</td>                    
                <%    
                }
                // process file
                else if (thisFile.isFile())
                {
                    //
                    // perform actions on file
                    //
                        
                    // get icon image
                    String iconImage = IMAGE_PATH + "/" + FileHandlerUtil.getIconImageForMimeType(thisFile);           
                        
                    // TEXT
                    if (FileHandlerUtil.isFileType(thisFile, FileHandlerUtil.TEXT))
                    {
                    %>
                    <td align="center"><a target="_blank" href="fileManager.jsp?BASE_NAME=<%= encodedPath %>&ACTION=<%= DISPLAY_CHARACTER %>"><img src="<%= iconImage %>" border="0" alt="View Text File" /></a></td>
                    <td><a target="_blank" href="fileManager.jsp?BASE_NAME=<%= encodedPath %>&ACTION=<%= DISPLAY_CHARACTER %>"><%= name %></a></td>                   
                    <td>TEXT</td>
                    <td><%= lastModifiedDate %></td>
                    <td><%= fileSize %></td>
                    <td><a class="cssbtn" href="fileManager.jsp?BASE_NAME=<%= encodedPath %>&<%= ACTION %>=<%= COPY_FILE %>">Copy</a></td>
                    <td><a class="cssbtn" href="fileManager.jsp?BASE_NAME=<%= encodedPath %>&<%= ACTION %>=<%= MOVE_FILE %>&<%= MOVE_FILE_NAME %>=<%= encodedPath %>">Move</a></td>
                    <td><a class="cssbtn" href="fileManager.jsp?BASE_NAME=<%= encodedPath %>&<%= ACTION %>=<%= RENAME_FILE %>">Rename</a></td>
                    <td><a class="cssbtn" href="fileManager.jsp?BASE_NAME=<%= encodedPath %>&<%= ACTION %>=<%= EDIT_FILE %>">Edit</a></td>                    
                    <td><a class="cssbtn" href="fileManager.jsp?BASE_NAME=<%= encodedPath %>&<%= ACTION %>=<%= DOWNLOAD_FILE %>">Download</a></td>                    
                    <td><input type="checkbox" name="<%= FILE_LIST %>" value="<%= encodedPath %>" /></td>
                    <% 
                    }
                    // HTML
                    else if (FileHandlerUtil.isFileType(thisFile, FileHandlerUtil.HTML_META))
                    { 
//                      this file web props (the web path calculated from file name and context, NOT THE CURRENT PATH of fileManager)
                        String contextPath = request.getContextPath();
                        String contextRootRealPath = pageContext.getServletContext().getRealPath("/");
                        ///boolean access = WebPathHandlerUtil.isFileWebAccessible(contextRootRealPath, path);
                        // (irrelevant in current app, must be web path)
                        String webPath = WebPathHandlerUtil.getWebPath(contextRootRealPath, contextPath, path);                        
                    %>
                    <td align="center"><img src="<%= iconImage %>" border="0" alt="View HTML File" /></a></td>
                    <td><a target="_blank" href="<%= webPath %>"><%= name %></a></td>                    
                    <td>HTML</td>
                    <td><%= lastModifiedDate %></td>
                    <td><%= fileSize %></td>
                    <td><a class="cssbtn" href="fileManager.jsp?BASE_NAME=<%= encodedPath %>&<%= ACTION %>=<%= COPY_FILE %>">Copy</a></td>
                    <td><a class="cssbtn" href="fileManager.jsp?BASE_NAME=<%= encodedPath %>&<%= ACTION %>=<%= MOVE_FILE %>&<%= MOVE_FILE_NAME %>=<%= encodedPath %>">Move</a></td>
                    <td><a class="cssbtn" href="fileManager.jsp?BASE_NAME=<%= encodedPath %>&<%= ACTION %>=<%= RENAME_FILE %>">Rename</a></td>
                    <td><a class="cssbtn" href="fileManager.jsp?BASE_NAME=<%= encodedPath %>&<%= ACTION %>=<%= EDIT_FILE %>">Edit</a></td>
                    <td><a class="cssbtn" href="fileManager.jsp?BASE_NAME=<%= encodedPath %>&<%= ACTION %>=<%= DOWNLOAD_FILE %>">Download</a></td>
                    <td><input type="checkbox" name="<%= FILE_LIST %>" value="<%= encodedPath %>" /></td>
                    <%                 
                    } 
                    // EDIT_IMAGE
                    else if (FileHandlerUtil.isFileType(thisFile, FileHandlerUtil.EDIT_IMAGE))
                    {
                        showImageGalLink = true;                           
                    %>
                    <td align="center"><a target="_blank" href="fileManager.jsp?BASE_NAME=<%= encodedPath %>&ACTION=<%= DISPLAY_BINARY %>"><img src="<%= iconImage %>" border="0" alt="View Image File" /></a></td>
                    <td><a target="_blank" href="fileManager.jsp?BASE_NAME=<%= encodedPath %>&ACTION=<%= DISPLAY_BINARY %>"><%= name %></a></td>
                    <td>IMAGE</td>
                    <td><%= lastModifiedDate %></td>
                    <td><%= fileSize %></td>
                    <td><a class="cssbtn" href="fileManager.jsp?BASE_NAME=<%= encodedPath %>&<%= ACTION %>=<%= COPY_FILE %>">Copy</a></td>
                    <td><a class="cssbtn" href="fileManager.jsp?BASE_NAME=<%= encodedPath %>&<%= ACTION %>=<%= MOVE_FILE %>&<%= MOVE_FILE_NAME %>=<%= encodedPath %>">Move</a></td>
                    <td><a class="cssbtn" href="fileManager.jsp?BASE_NAME=<%= encodedPath %>&<%= ACTION %>=<%= RENAME_FILE %>">Rename</a></td>
                    <td><a class="cssbtn" href="fileManager.jsp?BASE_NAME=<%= encodedPath %>&<%= ACTION %>=<%= IMAGE_EDIT %>">Edit</a></td>
                    <td><a class="cssbtn" href="fileManager.jsp?BASE_NAME=<%= encodedPath %>&<%= ACTION %>=<%= DOWNLOAD_FILE %>">Download</a></td>
                    <td><input type="checkbox" name="<%= FILE_LIST %>" value="<%= encodedPath %>" /></td>
                    <%                 
                    }
                    // NON_EDIT_IMAGE
                    else if (FileHandlerUtil.isFileType(thisFile, FileHandlerUtil.NON_EDIT_IMAGE))
                    {  
                    %>
                    <td align="center"><a target="_blank" href="fileManager.jsp?BASE_NAME=<%= encodedPath %>&ACTION=<%= DISPLAY_BINARY %>"><img src="<%= iconImage %>" border="0" alt="View Image File" /></a></td>
                    <td><a target="_blank" href="fileManager.jsp?BASE_NAME=<%= encodedPath %>&ACTION=<%= DISPLAY_BINARY %>"><%= name %></a></td>
                    <td>IMAGE</td>
                    <td><%= lastModifiedDate %></td>
                    <td><%= fileSize %></td>
                    <td><a class="cssbtn" href="fileManager.jsp?BASE_NAME=<%= encodedPath %>&<%= ACTION %>=<%= COPY_FILE %>">Copy</a></td>
                    <td><a class="cssbtn" href="fileManager.jsp?BASE_NAME=<%= encodedPath %>&<%= ACTION %>=<%= MOVE_FILE %>&<%= MOVE_FILE_NAME %>=<%= encodedPath %>">Move</a></td>
                    <td><a class="cssbtn" href="fileManager.jsp?BASE_NAME=<%= encodedPath %>&<%= ACTION %>=<%= RENAME_FILE %>">Rename</a></td>
                    <td><a class="cssbtn" href="fileManager.jsp?BASE_NAME=<%= encodedPath %>&<%= ACTION %>=<%= IMAGE_EDIT %>">Edit</a></td>
                    <td><a class="cssbtn" href="fileManager.jsp?BASE_NAME=<%= encodedPath %>&<%= ACTION %>=<%= DOWNLOAD_FILE %>">Download</a></td>
                    <td><input type="checkbox" name="<%= FILE_LIST %>" value="<%= encodedPath %>" /></td>
                    <%                 
                    }
                    // VIDEO
                    else if (FileHandlerUtil.isFileType(thisFile, FileHandlerUtil.VIDEO))
                    {  
                    %>
                    <td align="center"><a target="_blank" href="fileManager.jsp?BASE_NAME=<%= encodedPath %>&ACTION=<%= DISPLAY_BINARY %>"><img src="<%= iconImage %>" border="0" alt="Play Video File" /></a></td>
                    <td><a href="fileManager.jsp?BASE_NAME=<%= encodedPath %>&ACTION=<%= DISPLAY_BINARY %>"><%= name %></a></td>
                    <td>VIDEO</td>
                    <td><%= lastModifiedDate %></td>
                    <td><%= fileSize %></td>
                    <td><a class="cssbtn" href="fileManager.jsp?BASE_NAME=<%= encodedPath %>&<%= ACTION %>=<%= COPY_FILE %>">Copy</a></td>
                    <td><a class="cssbtn" href="fileManager.jsp?BASE_NAME=<%= encodedPath %>&<%= ACTION %>=<%= MOVE_FILE %>&<%= MOVE_FILE_NAME %>=<%= encodedPath %>">Move</a></td>
                    <td><a class="cssbtn" href="fileManager.jsp?BASE_NAME=<%= encodedPath %>&<%= ACTION %>=<%= RENAME_FILE %>">Rename</a></td>
                    <td>&#160;</td>                    
					<td><a class="cssbtn" href="fileManager.jsp?BASE_NAME=<%= encodedPath %>&<%= ACTION %>=<%= DOWNLOAD_FILE %>">Download</a></td>
					<td><input type="checkbox" name="<%= FILE_LIST %>" value="<%= encodedPath %>" /></td>
                    <%                 
                    }
                    // AUDIO
                    else if (FileHandlerUtil.isFileType(thisFile, FileHandlerUtil.AUDIO))
                    {  
                    %>
                    <td align="center"><a target="_blank" href="fileManager.jsp?BASE_NAME=<%= encodedPath %>&ACTION=<%= DISPLAY_BINARY %>"><img src="<%= iconImage %>" border="0" alt="Play Audio File" /></a></td>
                    <td><a href="fileManager.jsp?BASE_NAME=<%= encodedPath %>&ACTION=<%= DISPLAY_BINARY %>"><%= name %></a></td>
                    <td>AUDIO</td>
                    <td><%= lastModifiedDate %></td>
                    <td><%= fileSize %></td>
                    <td><a class="cssbtn" href="fileManager.jsp?BASE_NAME=<%= encodedPath %>&<%= ACTION %>=<%= COPY_FILE %>">Copy</a></td>
                    <td><a class="cssbtn" href="fileManager.jsp?BASE_NAME=<%= encodedPath %>&<%= ACTION %>=<%= MOVE_FILE %>&<%= MOVE_FILE_NAME %>=<%= encodedPath %>">Move</a></td>
                    <td><a class="cssbtn" href="fileManager.jsp?BASE_NAME=<%= encodedPath %>&<%= ACTION %>=<%= RENAME_FILE %>">Rename</a></td>
                    <td>&#160;</td>
                    <td><a class="cssbtn" href="fileManager.jsp?BASE_NAME=<%= encodedPath %>&<%= ACTION %>=<%= DOWNLOAD_FILE %>">Download</a></td>
                    <td><input type="checkbox" name="<%= FILE_LIST %>" value="<%= encodedPath %>" /></td>
                    <%                 
                    }
                    // APP_EXT
                    else if (FileHandlerUtil.isFileType(thisFile, FileHandlerUtil.APP_EXT))
                    {                
                    %>
                    <td align="center"><a href="fileManager.jsp?BASE_NAME=<%= encodedPath %>&ACTION=<%= DISPLAY_BINARY %>"><img src="<%= iconImage %>" border="0" alt="View Application File" /></a></td>
                    <td><a href="fileManager.jsp?BASE_NAME=<%= encodedPath %>&ACTION=<%= DISPLAY_BINARY %>"><%= name %></a></td>
                    <td>APP</td>
                    <td><%= lastModifiedDate %></td>
                    <td><%= fileSize %></td>                
                    <td><a class="cssbtn" href="fileManager.jsp?BASE_NAME=<%= encodedPath %>&<%= ACTION %>=<%= COPY_FILE %>">Copy</a></td>
                    <td><a class="cssbtn" href="fileManager.jsp?BASE_NAME=<%= encodedPath %>&<%= ACTION %>=<%= MOVE_FILE %>&<%= MOVE_FILE_NAME %>=<%= encodedPath %>">Move</a></td>
                    <td><a class="cssbtn" href="fileManager.jsp?BASE_NAME=<%= encodedPath %>&<%= ACTION %>=<%= RENAME_FILE %>">Rename</a></td>
                    <td>&#160;</td>
                    <td><a class="cssbtn" href="fileManager.jsp?BASE_NAME=<%= encodedPath %>&<%= ACTION %>=<%= DOWNLOAD_FILE %>">Download</a></td>
                    <td><input type="checkbox" name="<%= FILE_LIST %>" value="<%= encodedPath %>" /></td>
                    <%                
                    }
                    // APP_INT
                    else if (FileHandlerUtil.isFileType(thisFile, FileHandlerUtil.APP_INT))
                    {                
                    %>
                    <td align="center"><a href="fileManager.jsp?BASE_NAME=<%= encodedPath %>&<%= ACTION %>=<%= DISPLAY_BINARY %>"><img src="<%= iconImage %>" border="0" alt="View Application File" /></a></td>
                    <td><a target="_blank" href="fileManager.jsp?BASE_NAME=<%= encodedPath %>&<%= ACTION %>=<%= DISPLAY_BINARY %>"><%= name %></a></td>
                    <td>APP</td>
                    <td><%= lastModifiedDate %></td>
                    <td><%= fileSize %></td>                
	                <td><a class="cssbtn" href="fileManager.jsp?BASE_NAME=<%= encodedPath %>&<%= ACTION %>=<%= COPY_FILE %>">Copy</a></td>
                    <td><a class="cssbtn" href="fileManager.jsp?BASE_NAME=<%= encodedPath %>&<%= ACTION %>=<%= MOVE_FILE %>&<%= MOVE_FILE_NAME %>=<%= encodedPath %>">Move</a></td>
                    <td><a class="cssbtn" href="fileManager.jsp?BASE_NAME=<%= encodedPath %>&<%= ACTION %>=<%= RENAME_FILE %>">Rename</a></td>
                    <td>&#160;</td>
                    <td><a class="cssbtn" href="fileManager.jsp?BASE_NAME=<%= encodedPath %>&<%= ACTION %>=<%= DOWNLOAD_FILE %>">Download</a></td>
                    <td><input type="checkbox" name="<%= FILE_LIST %>" value="<%= encodedPath %>" /></td>
                    <%                
                    } 
                    // COMP
                    else if (FileHandlerUtil.isFileType(thisFile, FileHandlerUtil.COMP))
                    {
                        showImageGalLink = true; 
                    %>
                    <td align="center"><a target="_blank" href="fileManager.jsp?BASE_NAME=<%= encodedPath %>&<%= ACTION %>=<%= DISPLAY_COMPRESS_CONTENTS %>"><img src="<%= iconImage %>" border="0" alt="View Compressed File Contents" /></a></td>
                    <td><a target="_blank" href="fileManager.jsp?BASE_NAME=<%= encodedPath %>&<%= ACTION %>=<%= DISPLAY_COMPRESS_CONTENTS %>"><%= name %></a></td>
                    <td>COMP</td>
                    <td><%= lastModifiedDate %></td>
                    <td><%= fileSize %></td>
                    <td><a class="cssbtn" href="fileManager.jsp?BASE_NAME=<%= encodedPath %>&<%= ACTION %>=<%= COPY_FILE %>">Copy</a></td>
                    <td><a class="cssbtn" href="fileManager.jsp?BASE_NAME=<%= encodedPath %>&<%= ACTION %>=<%= MOVE_FILE %>&<%= MOVE_FILE_NAME %>=<%= encodedPath %>">Move</a></td>
                    <td><a class="cssbtn" href="fileManager.jsp?BASE_NAME=<%= encodedPath %>&<%= ACTION %>=<%= RENAME_FILE %>">Rename</a></td>
                    <td><a class="cssbtn" href="fileManager.jsp?BASE_NAME=<%= encodedPath %>&<%= ACTION %>=<%= UNCOMPRESS_FILE %>">Unzip</a></td>
                    <td><a class="cssbtn" href="fileManager.jsp?BASE_NAME=<%= encodedPath %>&<%= ACTION %>=<%= DOWNLOAD_FILE %>">Download</a></td>
                    <td><input type="checkbox" name="<%= FILE_LIST %>" value="<%= encodedPath %>" /></td>
                    <%                 
                    }
                    // UNKNOWN
                    else
                    {
                    %>
                    <td align="center"><a href="fileManager.jsp?BASE_NAME=<%= encodedPath %>&<%= ACTION %>=<%= DISPLAY_BINARY %>"><img src="<%= iconImage %>" border="0" alt="View File (as text)" /></a></td>
                    <td><a target="_blank" href="fileManager.jsp?BASE_NAME=<%= encodedPath %>&<%= ACTION %>=<%= DISPLAY_CHARACTER %>"><%= name %></a></td>
                    <td>UNK</td>
                    <td><%= lastModifiedDate %></td>
                    <td><%= fileSize %></td>
                    <td><a class="cssbtn" href="fileManager.jsp?BASE_NAME=<%= encodedPath %>&<%= ACTION %>=<%= COPY_FILE %>">Copy</a></td>
                    <td><a class="cssbtn" href="fileManager.jsp?BASE_NAME=<%= encodedPath %>&<%= ACTION %>=<%= MOVE_FILE %>&<%= MOVE_FILE_NAME %>=<%= encodedPath %>">Move</a></td>
                    <td><a class="cssbtn" href="fileManager.jsp?BASE_NAME=<%= encodedPath %>&<%= ACTION %>=<%= RENAME_FILE %>">Rename</a></td>
                    <td><a class="cssbtn" href="fileManager.jsp?BASE_NAME=<%= encodedPath %>&<%= ACTION %>=<%= EDIT_FILE %>">Edit</a></td>
                    <td><a class="cssbtn" href="fileManager.jsp?BASE_NAME=<%= encodedPath %>&<%= ACTION %>=<%= DOWNLOAD_FILE %>">Download</a></td>
                    <td><input type="checkbox" name="<%= FILE_LIST %>" value="<%= encodedPath %>" /></td>
                    <%
                    }               
                }                 
                %>
                </tr>
                <%        
            }
        // end if size of allContents > 0
        }
        else
        {
        	// check if baseName exists
        	if (FileHandlerUtil.dirExists(baseName))
        	{        	
        		%>
				<tr bgcolor="#ffffff" onMouseOver="this.bgColor='#ece6e6';" onMouseOut="this.bgColor='#ffffff';">
                	<td colspan="11">directory empty</td>
				</tr>
				<%    
        	}
        	else
        	{
        		%>
				<tr bgcolor="#ffffff" onMouseOver="this.bgColor='#ece6e6';" onMouseOut="this.bgColor='#ffffff';">
                	<td colspan="11">directory does not exist</td>
				</tr>
				<% 	
        	}
        }
        %>            
            <!-- FILE LIST ACTIONS (checkbox multiple file actions -->
            <tr>
				<td colspan="11" align="right">			
					<input type="submit" class="cssbtn btn_primary" name="<%= CREATE_ZIP_MULTI %>" Value="Create Zip From Selected" />
					<input type="submit" class="cssbtn btn_primary" name="<%= DELETE_MULTI %>" Value="Delete Selected" />
					<input type="hidden" name="<%= BASE_NAME %>" value="<%= baseName %>" />
				            
            
                    <% if (showImageGalLink) { %>
                    <!-- IMAGE GALLERY HOOK -->
                    <input type="submit" class="cssbtn btn_primary" name="<%= CREATE_GALLERY_MULTI %>" Value="Create Image Gallery From Selected" />
                    <!-- END IMAGE GALLERY HOOK -->
                    <% } %>
            
                </td>
            </tr>
                          
        </table>  
        </form>    
        <!-- END FILE ACTION TABLE -->
        
            
    
    </td>
</tr>
<tr><td>&#160;</td></tr>

<tr><td>&#160;</td></tr>
</table>
<!-- END OUTER TABLE -->      
          
<%
// END LIST DIR ACTION
}


//
// DISPLAY CHARACTER FILES
//
else if (action.equals(DISPLAY_CHARACTER))
{       
    FileHandlerUtil.viewTextFileFromJspWriter(response, out, baseFile);
}

//
// DISPLAY BINARY FILES
//
else if (action.equals(DISPLAY_BINARY))
{       
    FileHandlerUtil.viewBinaryFileFromJspWriter(response, out, baseFile);        
}

//
// DOWNLOAD FILE
//
else if (action.equals(DOWNLOAD_FILE))
{       
 FileHandlerUtil.downloadFile(response, out, baseFile);        
}

//
// DISPLAY COMPRESSED FILE CONTENTS
//
else if (action.equals(DISPLAY_COMPRESS_CONTENTS))
{       
    FileHandlerUtil.viewCompressedFileContentsFromJspWriter(out, baseFile);
}

//
// UNCOMPRESS_FILE
//
else if (action.equals(UNCOMPRESS_FILE))
{        
    %>
    <br />
    <div class="introText">
    File <b><%= baseFile.getName() %></b> will be uncompressed into current location - <b><%= baseFile.getParentFile().getName() %></b>.
    <br />
    (Note: if you wish to uncompress file elsewhere please MOVE file to desired location and then uncompress.)
    <br />
    (Note: any EXISTING directories or files that are part of archive to be uncompressed in current location will be overwritten.)
    <br /><br />
    <form name="uncompFileYes" method="post" action="fileManager.jsp">
    <input class="cssbtn btn_secondary" type="submit" name="submit" value="Go" />    
    <input type="hidden" name="<%= ACTION %>" value="<%= UNCOMPRESS_FILE_PROCESS %>" />
    <input type="hidden" name="<%= BASE_NAME %>" value="<%= baseName %>" />
    </form>
    <form name="uncompFileNo" method="post" action="fileManager.jsp">
    <input class="cssbtn btn_secondary" type="submit" name="submit" value="Cancel" />    
    <input type="hidden" name="<%= BASE_NAME %>" value="<%= baseParent %>" />
    </form>
    </div>
    <%
}

//
// UNCOMPRESS_FILE_PROCESS
//
else if (action.equals(UNCOMPRESS_FILE_PROCESS))
{    
    try
    {
        FileHandlerUtil.unCompressFile(baseFile);
        message = "*SUCCESS* uncompressed file - " + baseFile.getName();        
    }
    catch (GeneralException ge)
    {
        message = ge.getMessage();        
    }
    response.sendRedirect("fileManager.jsp?" + BASE_NAME + "=" + URLEncoder.encode(baseParent) + "&" + MESSAGE + "=" + URLEncoder.encode(message));    
}

//
// CREATE FILE
//
else if (action.equals(CREATE_FILE))
{
    String file = request.getParameter(CREATE_FILE_NAME);
    try
    {
        FileHandlerUtil.createFile(baseFile, file);
        message = "*SUCCESS* created file - " + file;        
    }
    catch (GeneralException ge)
    {
        message = ge.getMessage();        
    }      
    response.sendRedirect("fileManager.jsp?" + BASE_NAME + "=" + URLEncoder.encode(baseName) + "&" + MESSAGE + "=" + URLEncoder.encode(message));    
}

//
// COPY_FILE
//
else if (action.equals(COPY_FILE))
{        
    %>
    <br />
    <div class="introText">
    Please enter a new name for - <b><%= baseFile.getName() %></b>    
    <br />
    (Note: File will be copied to new name WITHIN current directory, if you wish to move file to another location please use the move task after copying.)
    <br /><br />
    <form name="copyFileYes" method="post" action="fileManager.jsp">
    <input type="text" size="50" name="<%= COPY_FILE_NAME %>" value="<%= baseFile.getName() %>" />
    <br />
    <input class="cssbtn btn_secondary" type="submit" name="submit" value="Go" />    
    <input type="hidden" name="<%= ACTION %>" value="<%= COPY_FILE_PROCESS %>" />
    <input type="hidden" name="<%= BASE_NAME %>" value="<%= baseName %>" />
    </form>
    <form name="copyFileNo" method="post" action="fileManager.jsp">
    <input class="cssbtn btn_secondary" type="submit" name="submit" value="Cancel" />    
    <input type="hidden" name="<%= BASE_NAME %>" value="<%= baseParent %>" />
    </form>
    </div>
    <%
}

//
// COPY_FILE_PROCESS
//
else if (action.equals(COPY_FILE_PROCESS))
{    
    String name = request.getParameter(COPY_FILE_NAME);
    try
    {          
       FileHandlerUtil.copyFile(baseFile, name);
       message = "*SUCCESS* copied file - " + name;        
    }
    catch (GeneralException ge)
    {
        message = ge.getMessage();        
    } 
    response.sendRedirect("fileManager.jsp?" + BASE_NAME + "=" + URLEncoder.encode(baseParent) + "&" + MESSAGE + "=" + URLEncoder.encode(message));    
}

//
// COPY_DIRECTORY
//
else if (action.equals(COPY_DIRECTORY))
{        
    %>
    <br />
    <div class="introText">
    Please enter a new name for - <b><%= baseFile.getName() %></b>    
    <br />
    (Note: Directory will be copied to new name WITHIN current directory, if you wish to move directory to another location please use the move task after copying.)
    <br /><br />
    <form name="copyDirYes" method="post" action="fileManager.jsp">
    <input type="text" size="50" name="<%= COPY_DIRECTORY_NAME %>" value="<%= baseFile.getName() %>" />
    <br />
    <input class="cssbtn btn_secondary" type="submit" name="submit" value="Go" />    
    <input type="hidden" name="<%= ACTION %>" value="<%= COPY_DIRECTORY_PROCESS %>" />
    <input type="hidden" name="<%= BASE_NAME %>" value="<%= baseName %>" />
    </form>
    <form name="copyDirNo" method="post" action="fileManager.jsp">
    <input class="cssbtn btn_secondary" type="submit" name="submit" value="Cancel" />    
    <input type="hidden" name="<%= BASE_NAME %>" value="<%= baseParent %>" />
    </form>
    </div>
    <%
}

//
// COPY_DIRECTORY_PROCESS
//
else if (action.equals(COPY_DIRECTORY_PROCESS))
{    
   String name = request.getParameter(COPY_DIRECTORY_NAME);
   try
    {    
    	File newDir = new File(baseFile.getParent() + FileHandlerUtil.FILE_SEP + name);       
        FileHandlerUtil.copyDirectory(baseFile, newDir);
        message = "*SUCCESS* copied directory - " + name;      
    }
    catch (GeneralException ge)
    {
        message = ge.getMessage();        
    }    
    response.sendRedirect("fileManager.jsp?" + BASE_NAME + "=" + URLEncoder.encode(baseParent) + "&" + MESSAGE + "=" + URLEncoder.encode(message));    
}

//
// MOVE_FILE
//
else if (action.equals(MOVE_FILE))
{
    String moveFileName = request.getParameter(MOVE_FILE_NAME);
    String moveFileDisplayName = moveFileName.substring(moveFileName.lastIndexOf(FileHandlerUtil.FILE_SEP) + 1, moveFileName.length());
    String locatorName = null;
    if (baseFile.isDirectory()) locatorName = FileHandlerUtil.getCanonicalPath(baseFile);
    else locatorName = FileHandlerUtil.getCanonicalPath(baseFile.getParentFile());
    
    %>
    <br />
    <div class="introText">
    <br /><br />
    Please select a location that <b><%= moveFileDisplayName %></b> should MOVE TO:
    <br />
    Make your selection by clicking on the desired "Move To" button.
    <br />
    To browse outside (up) the current directory hierarchy please select from the "Current Directory" location bar.
    <br />
    To browse inside (down) the current directory hierarchy please select the name of the directory <br />(and note that only directories that have subdirectories have names that are clickable).
    <br />
    <br />
    <table class="actionTable" name="dirList" cellpadding="3" cellspacing="0" align="left" width="90%" border="0">
	   <tr>
           <td class="browsePathBar">                    
                <b>Current Directory</b>: 
                <%    
            	// display current starting directory
            	String moveAction = MOVE_FILE + "&" + MOVE_FILE_NAME + "=" + URLEncoder.encode(moveFileName);
                out.println(FileHandlerUtil.getDirectoryStackHTML(baseFile, null, moveAction));
                %>
                
            </td>
            <td class="browsePathBar">
                <a class="cssbtn" href="fileManager.jsp?BASE_NAME=<%= locatorName %>&ACTION=<%= MOVE_FILE_PROCESS %>&<%= MOVE_FILE_NAME %>=<%= URLEncoder.encode(moveFileName) %>">Move To</a>
            </td>            
        </tr>        
        <%
        // display directory structure to choose where to move file/directory
        List dirContents = null;
        int dirContentsSize = 0;        
    
        // dirContents as baseFile IF it is a dir, or baseFileParent if it is a file
        dirContents = FileHandlerUtil.getDirList(locatorName);
        if (dirContents != null) dirContentsSize = dirContents.size();    

	   // parse all files and display directory list
       for (int i = 0; i < dirContentsSize; i++)
       {         
            File thisDir = (File) dirContents.get(i);        
            String path = FileHandlerUtil.getCanonicalPath(thisDir);
            String name = thisDir.getName();
            String encodedPath = URLEncoder.encode(path);
        
            // exclude itself from the list of choices to which to move it
            if (!path.equals(moveFileName))
            {   
	        %>
     
            <tr bgcolor="#ffffff" onMouseOver="this.bgColor='#ece6e6';" onMouseOut="this.bgColor='#ffffff';">
    	        <% // if this directory HAS subdirectories then make it a link for "drill down"
                List subDirContents = FileHandlerUtil.getDirList(FileHandlerUtil.getCanonicalPath(thisDir));
                if ((subDirContents != null) && (subDirContents.size() > 0))
                { 
                File firstSubDir = (File) subDirContents.get(0);
                String firstSubDirPath = FileHandlerUtil.getCanonicalPath(firstSubDir);
                // was firstSubDirPath
                %> 
                <td><a href="fileManager.jsp?BASE_NAME=<%= encodedPath %>&ACTION=<%= MOVE_FILE %>&<%= MOVE_FILE_NAME %>=<%= URLEncoder.encode(moveFileName) %>"><%= name %></a></td>
                <% } else { %>
                <td><%= name %></td>
                <% } %>                
                <td><a class="cssbtn" href="fileManager.jsp?BASE_NAME=<%= encodedPath %>&ACTION=<%= MOVE_FILE_PROCESS %>&<%= MOVE_FILE_NAME %>=<%= URLEncoder.encode(moveFileName) %>">Move To</a></td>
            </tr>            
            <% 
    	   }   
        }
        %>    
        <tr>
    	   <td colspan="2">
    	      <form name="copyDirNo" method="post" action="fileManager.jsp">
		      <input class="cssbtn btn_secondary" type="submit" name="submit" value="Cancel" />    
		      <input type="hidden" name="<%= BASE_NAME %>" value="<%= baseParent %>" />
		      </form> 
		  </td>
	   </tr>    
    </table>
    </div>
<br />

<%        
}

//
// MOVE_FILE_PROCESS
//
else if (action.equals(MOVE_FILE_PROCESS))
{    
    String moveFileName = request.getParameter(MOVE_FILE_NAME);
    String moveFileDisplayName = moveFileName.substring(moveFileName.lastIndexOf(FileHandlerUtil.FILE_SEP) + 1, moveFileName.length());
    try
    {        
        FileHandlerUtil.moveFile(baseFile, moveFileName);
        message = "*SUCCESS* moved directory/file - " + moveFileDisplayName;        
    }
    catch (GeneralException ge)
    {
        message = ge.getMessage();        
    }    
    response.sendRedirect("fileManager.jsp?" + BASE_NAME + "=" + URLEncoder.encode(baseParent) + "&" + MESSAGE + "=" + URLEncoder.encode(message));    
}


//
// RENAME_FILE
//
else if (action.equals(RENAME_FILE))
{       
    %>
    <br />
    <div class="introText">
    Please enter a new name for - <b><%= baseFile.getName() %></b>    
    <br />
    (Note: File will be renamed WITHIN current directory, if you wish to copy or move file to another location please use those tasks.)    
    <br /><br />
    <form name="renameFileYes" method="post" action="fileManager.jsp">
    <input type="text" size="50" name="<%= RENAME_FILE_NAME %>" value="<%= baseFile.getName() %>" />
    <br />
    <input class="cssbtn btn_secondary" type="submit" name="submit" value="Go" />    
    <input type="hidden" name="<%= ACTION %>" value="<%= RENAME_FILE_PROCESS %>" />
    <input type="hidden" name="<%= BASE_NAME %>" value="<%= baseName %>" />
    </form>
    <form name="renameFileNo" method="post" action="fileManager.jsp">
    <input class="cssbtn btn_secondary" type="submit" name="submit" value="Cancel" />    
    <input type="hidden" name="<%= BASE_NAME %>" value="<%= baseParent %>" />
    </form>
    </div>     
    <%
}

//
// RENAME_FILE_PROCESS
//
else if (action.equals(RENAME_FILE_PROCESS))
{    
    String name = request.getParameter(RENAME_FILE_NAME);
    try
    {        
        FileHandlerUtil.renameFile(baseFile, name);
        message = "*SUCCESS* renamed file - " + name;        
    }
    catch (GeneralException ge)
    {
        message = ge.getMessage();        
    }    
    response.sendRedirect("fileManager.jsp?" + BASE_NAME + "=" + URLEncoder.encode(baseParent) + "&" + MESSAGE + "=" + URLEncoder.encode(message));    
}

//
// DELETE FILE
//
else if (action.equals(DELETE_FILE))
{       
    %>
    <br />
    <div class="introText">
    Are you sure you wish to <b>DELETE <%= baseName %></b>?
    <br /><br />
    <form name="deleteFileYes" method="post" action="fileManager.jsp">
    <input class="cssbtn btn_secondary" type="submit" name="submit" value="Yes" />    
    <input type="hidden" name="<%= ACTION %>" value="<%= DELETE_FILE_PROCESS %>" />
    <input type="hidden" name="<%= BASE_NAME %>" value="<%= baseName %>" />
    </form>
    <form name="deleteFileNo" method="post" action="fileManager.jsp">
    <input class="cssbtn btn_secondary" type="submit" name="submit" value="No" />    
    <input type="hidden" name="<%= BASE_NAME %>" value="<%= baseParent %>" />
    </form>
    </div>
    <%
}

//
// DELETE_FILE_PROCESS
//
else if (action.equals(DELETE_FILE_PROCESS))
{
    try
    {
        FileHandlerUtil.deleteFile(baseFile);
        message = "*SUCCESS* deleted file - " + baseFile.getName();  
    }
    catch (GeneralException ge)
    {
        message = ge.getMessage();        
    }   
    response.sendRedirect("fileManager.jsp?" + BASE_NAME + "=" + URLEncoder.encode(baseParent) + "&" + MESSAGE + "=" + URLEncoder.encode(message));    
}

//
// CREATE DIRECTORY
//
else if (action.equals(CREATE_DIRECTORY))
{
    String directory = request.getParameter(CREATE_DIRECTORY_NAME);
    try
    {
       FileHandlerUtil.createDirectory(baseFile, directory);
       message = "SUCCESS, created directory - " + directory;        
    }
    catch (GeneralException ge)
    {
        message = ge.getMessage();    
    }    
    response.sendRedirect("fileManager.jsp?" + BASE_NAME + "=" + URLEncoder.encode(baseName) + "&" + MESSAGE + "=" + URLEncoder.encode(message));    
}

//
// DELETE DIRECTORY
//
else if (action.equals(DELETE_DIRECTORY))
{       
    %>
    <br />
    <div class="introText">
    Are you sure you wish to DELETE <b><%= baseFile.getName() %></b> <br />AND ALL CONTENTS (since this is a directory all contents will be deleted)?
    <br /><br />
    <form name="deleteDirYes" method="post" action="fileManager.jsp">
    <input class="cssbtn btn_secondary" type="submit" name="submit" value="Yes" />    
    <input type="hidden" name="<%= ACTION %>" value="<%= DELETE_DIRECTORY_PROCESS %>" />
    <input type="hidden" name="<%= BASE_NAME %>" value="<%= baseName %>" />
    </form>
    <form name="deleteDirNo" method="post" action="fileManager.jsp">
    <input class="cssbtn btn_secondary" type="submit" name="submit" value="No" />    
    <input type="hidden" name="<%= BASE_NAME %>" value="<%= baseParent %>" />
    </form>
    </div>     
    <%
}

//
// DELETE_DIRECTORY_PROCESS
//
else if (action.equals(DELETE_DIRECTORY_PROCESS))
{  
    try
    {
        FileHandlerUtil.deleteDirectory(baseFile);
        message = "*SUCCESS* deleted directory - " + baseFile.getName();
    }
    catch (GeneralException ge)
    {
        message = ge.getMessage();    
    }    
    response.sendRedirect("fileManager.jsp?" + BASE_NAME + "=" + URLEncoder.encode(baseParent) + "&" + MESSAGE + "=" + URLEncoder.encode(message));    
}

//
// EDIT FILE
//
else if (action.equals(EDIT_FILE))
{  
  %>
    <br />
    <div class="introText">
    Editing File: <b><%= baseFile.getName() %></b>
    <br /><br />
    <form name="editFileYes" method="post" action="fileManager.jsp">
    <textarea name="<%= EDIT_FILE_TEXT %>" wrap="off" rows="20" cols="75"><%= FileHandlerUtil.getTextFileContentsForEdit(baseName) %></textarea>
    <br />
    Save File As (leave alone to overwrite) : <input type="text" size="64" name="<%= EDIT_FILE_SAVE_NAME %>" value="<%= baseFile.getName() %>" />
    <br />
    Make backup file with .bak extension? <input type="checkbox" name="<%= EDIT_FILE_BAK %>" value="true">
    <br />
    <br />
    <input class="cssbtn btn_secondary" type="submit" name="submit" value="Save" />    
    <input type="hidden" name="<%= ACTION %>" value="<%= EDIT_FILE_SAVE %>" />
    <input type="hidden" name="<%= BASE_NAME %>" value="<%= baseName %>" />
    </form>
    <form name="editFileNo" method="post" action="fileManager.jsp">
    <input class="cssbtn btn_secondary" type="submit" name="submit" value="Cancel" />    
    <input type="hidden" name="<%= BASE_NAME %>" value="<%= baseParent %>" />
    </form>
    </div>     
    <%  
}

//
// EDIT FILE SAVE
//
else if (action.equals(EDIT_FILE_SAVE))
{  
    String editFileSaveName = request.getParameter(EDIT_FILE_SAVE_NAME);
    String editFileSaveNameComplete = baseParent + FileHandlerUtil.FILE_SEP + editFileSaveName;
    String editFileCreateBak = request.getParameter(EDIT_FILE_BAK);
    String editFileText = request.getParameter(EDIT_FILE_TEXT);
    try
    {
        FileHandlerUtil.createTextFile(baseName, editFileSaveNameComplete, editFileCreateBak, editFileText);
        message = "*SUCCESS* edited file - " + editFileSaveName;
    }
    catch (GeneralException ge)
    {
        message = ge.getMessage();    
    }   
    response.sendRedirect("fileManager.jsp?" + BASE_NAME + "=" + URLEncoder.encode(baseParent) + "&" + MESSAGE + "=" + URLEncoder.encode(message));        
}

//
// IMAGE_EDIT
//
else if (action.equals(IMAGE_EDIT))
{
%>
    <br />
    <div class="introText">
	Edit Image: <b><%= baseFile.getName() %></b>    
	<br />
	<br />
    <img src="<%= CONTEXT_PATH %>/fileManager.jsp?BASE_NAME=<%= baseName %>&ACTION=DISPLAY_BINARY" border="1" />	
    <br />
    <%
        HashMap imageProps = ImageHandlerUtil.getImageProperties(baseName);
        Integer width = (Integer) imageProps.get(WIDTH);
        Integer height = (Integer) imageProps.get(HEIGHT);         
    %>
    Current image width: <%= width %>
    <br />
    Current image height: <%= height %>            
    <br />
	<br />
	Please select either RESIZE or ROTATE to continue.
	<br />
	<br />
	<a class="cssbtn btn_secondary" href="fileManager.jsp?BASE_NAME=<%= URLEncoder.encode(basePath) %>&<%= ACTION %>=<%= IMAGE_RESIZE %>">RESIZE</a>
	<a class="cssbtn btn_secondary" href="fileManager.jsp?BASE_NAME=<%= URLEncoder.encode(basePath) %>&<%= ACTION %>=<%= IMAGE_ROTATE %>">ROTATE</a>	
	<br />
    <br />
	<form name="imageEditNo" method="post" action="fileManager.jsp">
    <input class="cssbtn btn_secondary" type="submit" name="submit" value="Cancel" />    
    <input type="hidden" name="<%= BASE_NAME %>" value="<%= baseParent %>" />
    </form>
    </div>
<%     
}

//
// IMAGE_RESIZE
//
else if (action.equals(IMAGE_RESIZE)){
    %>
    <br />
    <div class="introText">
    Resize Image: <b><%= baseFile.getName() %></b>    
    <br />
    <form name="resizeImageYes" method="post" action="fileManager.jsp">
    <br />
    <img src="<%= CONTEXT_PATH %>/fileManager.jsp?BASE_NAME=<%= baseName %>&ACTION=DISPLAY_BINARY" border="1" />
    <br />
    <%
        HashMap imageProps = ImageHandlerUtil.getImageProperties(baseName);
        Integer width = (Integer) imageProps.get(WIDTH);
        Integer height = (Integer) imageProps.get(HEIGHT);         
    %>
    Current image width: <b><%= width %></b>
    <br />
    Current image height: <b><%= height %></b>            
    <br />
    New WIDTH size in pixels (ratio width/height will be maintained): <input type="text" name="<%= IMAGE_RESIZE_DIMENSION %>" />
    <br />
    Save Image As (leave alone to overwrite) : <input type="text" size="50" name="<%= IMAGE_RESIZE_SAVE_NAME %>" value="<%= baseFile.getName() %>" />
    <br />
    Make backup file with .bak extension? <input type="checkbox" name="<%= IMAGE_RESIZE_BAK %>" value="true">
    <br />
    <br />
    <input class="cssbtn btn_secondary" type="submit" name="submit" value="Process" />    
    <input type="hidden" name="<%= ACTION %>" value="<%= IMAGE_RESIZE_PROCESS %>" />
    <input type="hidden" name="<%= BASE_NAME %>" value="<%= baseName %>" />
    </form>
    <form name="imageResizeNo" method="post" action="fileManager.jsp">
    <input class="cssbtn btn_secondary" type="submit" name="submit" value="Cancel" />    
    <input type="hidden" name="<%= BASE_NAME %>" value="<%= baseParent %>" />
    </form>
    </div>         
    <%    
}

//
// IMAGE RESIZE PROCESS
//
else if (action.equals(IMAGE_RESIZE_PROCESS))
{    
    String imageResizeDimension = request.getParameter(IMAGE_RESIZE_DIMENSION);
    String imageResizeSaveName = request.getParameter(IMAGE_RESIZE_SAVE_NAME);
    String imageResizeSaveNameComplete = baseParent + FileHandlerUtil.FILE_SEP + imageResizeSaveName;    
    String imageResizeBak = request.getParameter(IMAGE_RESIZE_BAK);
    try
    {
        FileHandlerUtil.resizeImage(baseName, imageResizeSaveNameComplete, imageResizeBak, imageResizeDimension);
        message = "*SUCCESS resized image - " + imageResizeSaveName;
    }
    catch (GeneralException ge)
    {
        message = ge.getMessage();    
    }    
    response.sendRedirect("fileManager.jsp?" + BASE_NAME + "=" + URLEncoder.encode(baseParent) + "&" + MESSAGE + "=" + URLEncoder.encode(message));        
}

//
// IMAGE_ROTATE
//
else if (action.equals(IMAGE_ROTATE))
{
    %>
    <br />
    <div class="introText">
    Rotate Image: <b><%= baseFile.getName() %></b>    
    <br />
    <form name="rotateImageYes" method="post" action="fileManager.jsp">
    <br />
    <img src="<%= CONTEXT_PATH %>/fileManager.jsp?BASE_NAME=<%= baseName %>&ACTION=DISPLAY_BINARY" border="1" />
    <br />
    <%
        HashMap imageProps = ImageHandlerUtil.getImageProperties(baseName);
        Integer width = (Integer) imageProps.get(WIDTH);
        Integer height = (Integer) imageProps.get(HEIGHT);         
    %>
    <span class="introText">
    Current image width = <b><%= width %></b>
    <br />
    Current image height = <b><%= height %></b>            
    <br />
    Select amount to rotate image by:    
    <select name="<%= IMAGE_ROTATE_AMOUNT %>" />
    <option name="90" value="<%= TransposeDescriptor.ROTATE_90.getName() %>">90&deg; clockwise</option>
    <option name="180" value="<%= TransposeDescriptor.ROTATE_180.getName() %>">180&deg; clockwise</option>
    <option name="270" value="<%= TransposeDescriptor.ROTATE_270.getName() %>">270&deg; clockwise</option>    
    </select>    
    <br />    
    Save Image As (leave alone to overwrite) : <input type="text" size="50" name="<%= IMAGE_ROTATE_SAVE_NAME %>" value="<%= baseFile.getName() %>" />
    <br />
    Make backup file with .bak extension? <input type="checkbox" name="<%= IMAGE_ROTATE_BAK %>" value="true">
    <br />
    <br />
    <input class="cssbtn btn_secondary" type="submit" name="submit" value="Process" />    
    <input type="hidden" name="<%= ACTION %>" value="<%= IMAGE_ROTATE_PROCESS %>" />
    <input type="hidden" name="<%= BASE_NAME %>" value="<%= baseName %>" />
    </form>
    <form name="imageRotateNo" method="post" action="fileManager.jsp">
    <input class="cssbtn btn_secondary" type="submit" name="submit" value="Cancel" />    
    <input type="hidden" name="<%= BASE_NAME %>" value="<%= baseParent %>" />
    </div>    
    <%    
}

//
// IMAGE ROTATE PROCESS
//
else if (action.equals(IMAGE_ROTATE_PROCESS))
{    
    String imageRotateAmount = request.getParameter(IMAGE_ROTATE_AMOUNT);
    String imageRotateSaveName = request.getParameter(IMAGE_ROTATE_SAVE_NAME);
    String imageRotateSaveNameComplete = baseParent + FileHandlerUtil.FILE_SEP + imageRotateSaveName;
    String imageRotateBak = request.getParameter(IMAGE_ROTATE_BAK);    
    try
    {
        FileHandlerUtil.rotateImage(baseName, imageRotateSaveNameComplete, imageRotateBak, imageRotateAmount);        
        message = "*SUCCESS rotated image - " + imageRotateSaveName;
    }
    catch (GeneralException ge)
    {
        message = ge.getMessage();    
    }    
    response.sendRedirect("fileManager.jsp?" + BASE_NAME + "=" + URLEncoder.encode(baseParent) + "&" + MESSAGE + "=" + URLEncoder.encode(message));        
}

// 
// IMAGE_GALLERY_CREATE
//
else if (action.equals(IMAGE_GALLERY_CREATE))
{
    response.sendRedirect("imageGallery.jsp?" + BASE_NAME + "=" + URLEncoder.encode(baseParent));        
}
 
//
// DELETE_MULTI
//
else if (action.equals(DELETE_MULTI))
{
    //  get the fileList
    String[] fileList = request.getParameterValues(FILE_LIST);
    if ((fileList == null) || (fileList.length < 1))
    {
        message = "*ERROR no files have been selected to be deleted.";
        response.sendRedirect("fileManager.jsp?" + BASE_NAME + "=" + URLEncoder.encode(baseName) + "&" + MESSAGE + "=" + URLEncoder.encode(message));        
    }
    else
    {    
        %>
        <br />
        <div class="introText">
        Are you sure you wish to <b>DELETE</b> the selected list of files?
        <br /><br />
        <form name="deleteFilesYes" method="post" action="fileManager.jsp">
        <input type="hidden" name="<%= ACTION %>" value="<%= DELETE_MULTI_PROCESS %>" />
        <input type="hidden" name="<%= BASE_NAME %>" value="<%= baseName %>" />
        <%
        int size = fileList.length;
        for (int i = 0; i < size; i++)
        {
            String thisFileName = URLDecoder.decode(fileList[i]);
            String thisFileDisplayName = thisFileName.substring(thisFileName.lastIndexOf(FileHandlerUtil.FILE_SEP) + 1, thisFileName.length());
            %>
            <b><%= thisFileDisplayName %></b><br />
            <input type="hidden" name="<%= FILE_LIST %>" value="<%= fileList[i] %>" />
            <%
        }
        %>
        <br />
        <input class="cssbtn btn_secondary" type="submit" name="submit" value="Yes" />    
        </form>
        <form name="deleteFileNo" method="post" action="fileManager.jsp">
        <input class="cssbtn btn_secondary" type="submit" name="submit" value="No" />    
        <input type="hidden" name="<%= BASE_NAME %>" value="<%= baseParent %>" />
        </form>
        </div>
        <%
    }
}

//
// DELETE_MULTI_PROCESS
//
else if (action.equals(DELETE_MULTI_PROCESS))
{  
  // get the fileList
  String[] fileList = request.getParameterValues(FILE_LIST);
  if ((fileList == null) || (fileList.length < 1))
  {
  	message = "*ERROR no files have been selected to be deleted.";
  	response.sendRedirect("fileManager.jsp?" + BASE_NAME + "=" + URLEncoder.encode(baseName) + "&" + MESSAGE + "=" + URLEncoder.encode(message));        
  }
  else
  {
  	try
    {
        FileHandlerUtil.deleteFileList(fileList);        
        message = "*SUCCESS deleted selected files.";
    }
    catch (GeneralException ge)
    {
        message = ge.getMessage();    
    }    
    response.sendRedirect("fileManager.jsp?" + BASE_NAME + "=" + URLEncoder.encode(baseParent) + "&" + MESSAGE + "=" + URLEncoder.encode(message));  	
  }  
}

//
// CREATE_ZIP_MULTI
//
else if (action.equals(CREATE_ZIP_MULTI))
{    
    //  get the fileList
    String[] fileList = request.getParameterValues(FILE_LIST);
    if ((fileList == null) || (fileList.length < 1))
    {
        message = "*ERROR no files have been selected to be used to create zip file.";
        response.sendRedirect("fileManager.jsp?" + BASE_NAME + "=" + URLEncoder.encode(baseName) + "&" + MESSAGE + "=" + URLEncoder.encode(message));        
    }
    else
    {    
    %>
        <br />
        <form name="createZipGo" method="post" action="fileManager.jsp">
        <div class="introText">
        Please enter name for NEW zip file to be created from selected files (WITHOUT any filename extension):
        <br />(Note that new zip file will be created in current directory and will not have path information associated with zip entry.)
        <br /><br />
        <%
        int size = fileList.length;
        for (int i = 0; i < size; i++)
        {
            String thisFileName = URLDecoder.decode(fileList[i]);
            String thisFileDisplayName = thisFileName.substring(thisFileName.lastIndexOf(FileHandlerUtil.FILE_SEP) + 1, thisFileName.length());
            %>
            <b><%= thisFileDisplayName %></b><br />
            <input type="hidden" name="<%= FILE_LIST %>" value="<%= fileList[i] %>" />
            <%
        }
        %>    
        <br />        
        <input type="text" name="<%= CREATE_ZIP_MULTI_NAME %>" size="50" maxlength="255" />
        <input type="hidden" name="<%= ACTION %>" value="<%= CREATE_ZIP_MULTI_PROCESS %>" />
        <input type="hidden" name="<%= BASE_NAME %>" value="<%= baseName %>" />        
        <br />
        <input class="cssbtn btn_secondary" type="submit" name="submit" value="Go" />    
        </form>    
        </div>
    <%
    }
}
//
//CREATE_ZIP_MULTI_PROCESS
//
else if (action.equals(CREATE_ZIP_MULTI_PROCESS))
{
    //  get the fileList
    String[] fileList = request.getParameterValues(FILE_LIST);
    if ((fileList == null) || (fileList.length < 1))
    {
        message = "*ERROR no files have been selected to be used to create zip file.";
        response.sendRedirect("fileManager.jsp?" + BASE_NAME + "=" + URLEncoder.encode(baseName) + "&" + MESSAGE + "=" + URLEncoder.encode(message));        
    }
    else
    {
        // get the new fileName
        String newFileName = request.getParameter(CREATE_ZIP_MULTI_NAME);
        if ((StringUtils.isEmpty(newFileName)) || (newFileName.indexOf(".") != -1))
        {
            message = "*ERROR name for zip file to be created must be supplied and must not have an extension.";
            response.sendRedirect("fileManager.jsp?" + BASE_NAME + "=" + URLEncoder.encode(baseName) + "&" + MESSAGE + "=" + URLEncoder.encode(message));        
        }
        else
        {
            try
            {
                newFileName = newFileName + "." + "zip";
                FileHandlerUtil.createCompressedFile(fileList, newFileName, baseName);        
                message = "*SUCCESS created zip file.";
            }
            catch (GeneralException ge)
            {
                message = ge.getMessage();    
            }    
            response.sendRedirect("fileManager.jsp?" + BASE_NAME + "=" + URLEncoder.encode(baseName) + "&" + MESSAGE + "=" + URLEncoder.encode(message));
        }
    }
}

//
//CREATE GALLERY MULTI
//
else if (action.equals(CREATE_GALLERY_MULTI))
{    
 //  get the fileList
 String[] fileList = request.getParameterValues(FILE_LIST);
 if ((fileList == null) || (fileList.length < 1))
 {        
     message = "*ERROR no files have been selected to be used to create image gallery.";
     response.sendRedirect("fileManager.jsp?" + BASE_NAME + "=" + URLEncoder.encode(baseName) + "&" + MESSAGE + "=" + URLEncoder.encode(message));       
 }
 // fileList present
 else
 {        
     // split the fileList array into separate ArrayLists of allowed image type files and compressed files (which have
     // allowed image type files within ROOT).
     // *JSPFileManager Does not support compressed files with path information and images further down in compressed
     // file than root.
     int size = fileList.length;    
     ArrayList imageFiles = new ArrayList();
     ArrayList compImageFiles = new ArrayList();
     for (int i = 0; i < size; i ++)
     {
         String thisFileName = URLDecoder.decode(fileList[i]);            
         File thisFile = new File(thisFileName);
         // if edit image add FILE to imageFiles
         if (FileHandlerUtil.isFileType(thisFile, FileHandlerUtil.EDIT_IMAGE))
         {
             imageFiles.add(thisFile);
         }
         // if comp then check for image files in root and if present add FILE to compImageFiles
         else if (FileHandlerUtil.isFileType(thisFile, FileHandlerUtil.COMP))
         {
             if (FileHandlerUtil.compressedFileHasImageContent(thisFile))
             {
                 compImageFiles.add(thisFile);
             }
         }            
     }
     
     // process the arrayLists to display create form to user
     int imageFilesSize = imageFiles.size();
     int compImageFilesSize = compImageFiles.size();
     
     // if there are image files or compressed files with images then display the form
     if ((imageFilesSize > 0) || (compImageFilesSize > 0))
     {
         %>   
         <!-- javascript for preview -->
            <script language="javascript">
            // colorPicker stuff
         var cp = new ColorPicker('window'); // Popup window
         var cp2 = new ColorPicker(); // DIV style
         
         function setGalleryPreview()
            {
                getObject("galPreview").style.color = document.forms['imageGalleryProcess'].<%= CREATE_GALLERY_FONT_COLOR %>.value;
                getObject("galPreview").style.background = document.forms['imageGalleryProcess'].<%= CREATE_GALLERY_BACKGROUND %>.value;
                getObject("galPreview").style.fontFamily = document.forms['imageGalleryProcess'].<%= CREATE_GALLERY_FONT_FAMILY %>.value;
                getObject("galPreview").style.fontWeight = document.forms['imageGalleryProcess'].<%= CREATE_GALLERY_FONT_WEIGHT %>.value;
                getObject("galPreview").style.fontSize = document.forms['imageGalleryProcess'].<%= CREATE_GALLERY_FONT_SIZE %>.value;
                getObject("galPreview").style.borderColor = document.forms['imageGalleryProcess'].<%= CREATE_GALLERY_BORDER_COLOR %>.value;
                getObject("galPreview").style.borderStyle = document.forms['imageGalleryProcess'].<%= CREATE_GALLERY_BORDER_STYLE %>.value;
                getObject("galPreview").style.borderWidth = document.forms['imageGalleryProcess'].<%= CREATE_GALLERY_BORDER_WIDTH %>.value;
             setGalleryName();                
            }            
         function setGalleryName()
         {
             var galNameValue = document.forms['imageGalleryProcess'].<%= CREATE_GALLERY_NAME %>.value;
             var galleryName = document.getElementById("galleryName");
             if (typeof(galleryName.innerHTML) != undefined)
             {            
                 galleryName.innerHTML = galNameValue;
             }
         }            
            </script>
                     
         <div class="introText">
         <br />
         <b>Create Image Gallery</b>
         </div>
         <div id="galPreview" name="galPreview" 
             style="width:80%; margin:10px; padding:10px; font-size:18; font-family:arial; font-weight:bold; border-width: medium; border-style:outset; border-color: #ffffee; color:#000000; background-color:#eeeeee;">            
         <%        
         // imageFiles
     
         if (imageFilesSize > 0) 
         { %>   
            <br />              
            <span id="galleryName">Image Gallery Name Here</span>                
             <br /><br />
             <%
             for (int i = 0; i < imageFilesSize; i ++)
             {
                 File thisFile = (File) imageFiles.get(i);
                 String thisFileName = thisFile.getName();
                 String thisFilePath = FileHandlerUtil.getCanonicalPath(thisFile);
                 String thisFileEncodedPath = URLEncoder.encode(thisFilePath);
                 %>
                 <img src="fileManager.jsp?BASE_NAME=<%= thisFileEncodedPath %>&ACTION=DISPLAY_BINARY" width="125" border="0" />                
                 <%
                 if ((i + 1) % 5 == 0)
                 {
                     out.println("<br />");   
                 }    
             }        
         }
         %>            
         </div>            
         <hr />
                     
         <div class="introText">
         <%
         // compImageFiles
         if (compImageFilesSize > 0) 
         { %>
             <br />
             Compressed files with images for the gallery:
             <br />
             <%
             for (int i = 0; i < compImageFilesSize; i ++)
             {
                 File thisFile = (File) compImageFiles.get(i);
                 String thisFileName = thisFile.getName();
                 String thisFilePath = FileHandlerUtil.getCanonicalPath(thisFile);                    
                 %>
                     <%= thisFileName %>
                     <br />
                 <%
                 // display names of images in zip file
                 ArrayList zipContents = ZipHandlerUtil.getZipFileContents(thisFile);
                 int zsize = 0;
                 if (zipContents != null)
                     zsize = zipContents.size();
                 for (int j = 0; j < zsize; j++)
                 {
                     String entry = (String) zipContents.get(j);
                     if (FileHandlerUtil.isFileType(entry, FileHandlerUtil.EDIT_IMAGE))
                     {
                         %>&#160;&#160;<%= j + 1 %>.&#160;<%= entry %><br /><%   
                     }
                     else
                     {
                         %>&#160;&#160;<%= j + 1 %>.&#160;<%= entry %> (not image type)<br /><%
                     }            
                 }
             }
         %>
         <hr />  
         <%
         }        
         %>
                   
         </div>                        
         
         <!-- imageGalleryProcess form -->           
         <form name="imageGalleryProcess" method="post" action="fileManager.jsp">
         <input type="hidden" name="<%= BASE_NAME %>" value="<%= baseName %>" />
         <table class="introText" width="90%" border="0">        
         <tr>
             <td colspan="2">&#160;</td>
         </tr>
         <tr>
             <td>
                 Enter Image Gallery name (*required* - unique name in current directory path):
             </td>
             <td>
                 <input onchange="setGalleryPreview()" type="text" name="<%= CREATE_GALLERY_NAME %>" /> 
             </td>
         </tr>             
         <tr>
             <td>
                 Select background color (defaults to pale moonlit avocado):
             </td>
             <td>                
                 <input onchange="setGalleryPreview()" type="text" size="7" maxlength="7" name="<%= CREATE_GALLERY_BACKGROUND %>" value="#EEEEEE" />
                 <a onblur="setGalleryPreview()" href="#" onClick="cp.select(document.forms['imageGalleryProcess'].<%= CREATE_GALLERY_BACKGROUND %>,'pickBg');return false;" NAME="pickBg" ID="pickBg">Color Selector</a>
                 <script language="JavaScript">cp.writeDiv()</script>
             </td>
         <tr>
             <td>
                 Select font color for title (defaults to black)):
             </td>
             <td>    
                 <input onchange="setGalleryPreview()" type="text" size="7" maxlength="7" name="<%= CREATE_GALLERY_FONT_COLOR %>" value="#000000" /> 
                 <a onblur="setGalleryPreview()" href="#" onClick="cp.select(document.forms['imageGalleryProcess'].<%= CREATE_GALLERY_FONT_COLOR %>,'pickFont');return false;" NAME="pickFont" ID="pickFont">Color Selector</a>
                 <script language="JavaScript">cp.writeDiv();</script>
             </td>
         </tr>
         <tr>
             <td>
                 Select font size for title (defaults to 18px):
             </td>
             <td>    
                 <select onchange="setGalleryPreview()" name="<%= CREATE_GALLERY_FONT_SIZE %>">
                 <option value="11">10px</option>
                 <option value="11">11px</option>
                 <option value="12">12px</option>
                 <option value="14">14px</option>
                 <option value="16">16px</option>
                 <option selected="selected" value="18">18px</option>
                 <option value="20">20px</option>
                 <option value="22">22px</option>
                    <option value="24">24px</option>
                    <option value="28">28px</option>
                    <option value="30">30px</option>
                 </select>                    
             </td>
         </tr>
         <tr>
             <td>
                 Select font weight for title (defaults to bold):
             </td>
             <td>    
                 <select onchange="setGalleryPreview()" name="<%= CREATE_GALLERY_FONT_WEIGHT %>">
                 <option value="normal">Normal</option>
                 <option value="bold" selected="selected">Bold</option>
                 </select>
             </td>
         </tr>
         <tr>
             <td>
                 Select font family for title (defaults to Arial):
             </td>
             <td>    
                 <select onchange="setGalleryPreview()" name="<%= CREATE_GALLERY_FONT_FAMILY %>">
                 <option value="arial, helvetica" selected="selected">Arial</font></option>
                 <option value="verdana">Verdana</option>
                 <option value="courier">Courier</option>
                 <option value="times new roman">Times</option>
                 <option value="modern">Modern</option>
                 </select>
             </td>
         </tr>
         <tr>        
             <td>
                 Select gallery border color (defaults to pale pistachio):
             </td>
             <td>    
                 <input onchange="setGalleryPreview()" type="text" size="7" maxlength="7" name="<%= CREATE_GALLERY_BORDER_COLOR %>" value="#FFFFCC" />
                 <a onblur="setGalleryPreview()" href="#" onClick="cp.select(document.forms['imageGalleryProcess'].<%= CREATE_GALLERY_BORDER_COLOR %>,'pickBorder');return false;" NAME="pickBorder" ID="pickBorder">Color Selector</a>
                 <script language="JavaScript">cp.writeDiv()</script>
             </td>
         </tr>
         <tr>        
             <td>
                 Select gallery border style (defaults outset):
             </td>
             <td>    
                 <select onchange="setGalleryPreview()" name="<%= CREATE_GALLERY_BORDER_STYLE %>">
                 <option value="solid">solid</option>
                 <option value="dashed">dashed</option>
                 <option value="dotted">dotted</option>
                 <option value="double">double</option>
                 <option value="groove">groove</option>
                 <option value="ridge">ridge</option>
                 <option value="inset">inset</option>
                 <option value="outset" selected="selected">outset</option>
                 </select>
             </td>
         </tr>
         <tr>        
             <td>
                 Select gallery border width (defaults to medium):
             </td>
             <td>    
                 <select onchange="setGalleryPreview()" name="<%= CREATE_GALLERY_BORDER_WIDTH %>">
                 <option value="thin">thin</option>
                 <option value="medium" selected="selected">medium</option>
                 <option value="thick">thick</option>
                 <option value="1px">1px</option>
                 <option value="2px">2px</option>
                 <option value="3px">3px</option>
                 <option value="4px">4px</option>
                 <option value="5px">5px</option>
                 <option value="6px">6px</option>
                 <option value="7px">7px</option>
                 <option value="8px">8px</option>
                 <option value="9px">9px</option>
                 <option value="10px">10px</option>
                 </select>
             </td>
         </tr>          
         <tr>
             <td>
                 Select if border ON IMAGES should be present:
             </td>
             <td>
                 <input type="checkbox" name="<%= CREATE_GALLERY_BORDER_IMAGES %>" />
             </td>
         </tr>
         <tr>        
             <td>
                 Enter thumbnail image size in width as pixels (default is 150):
             </td>
             <td>
                 <input type="text" size="4" maxlength="4" name="<%= CREATE_GALLERY_THUMB_SIZE %>" value="150" />
             </td>
         </tr>
         <tr>
             <td>    
                 Enter full image size in width as pixels (default is 800):
             </td>
             <td>
                <input type="text" size="4" maxlength="4" name="<%= CREATE_GALLERY_FULL_SIZE %>" value="800" />
            </td>
         </tr>
         <tr>
             <td>
                 Enter "Watermark/Copyright" text to be embossed on images:
             </td>
             <td>
                 <input type="text" name="<%= CREATE_GALLERY_COPYRIGHT_TEXT %>" /> 
             </td>
         </tr> 
         <tr>
             <td colspan="2">
                 (Note, if image is already smaller than what is entered it is left alone.)
             </td>
         </tr>    
         <input type="hidden" name="<%= ACTION %>" value="<%= CREATE_GALLERY_MULTI_PROCESS %>" />        
 
         <% // put all the fileList selection params back into the next request
         for (int i = 0; i < size; i++)
         {                
             String thisFile = fileList[i];
         %>
         <input type="hidden" name="<%= FILE_LIST %>" value="<%= thisFile %>" />
         <%
         }
         %>    
         <tr>
             <td colspan="2">&#160;</td>            
         </tr> 
         <tr>
             <td colspan="2">    
                 <input class="cssbtn btn_secondary" type="submit" name="submit" value="Process" onmouseover="this.className='cssbtn btn_secondary_hover'" onmouseout="this.className='cssbtn btn_secondary'"/>
             </td>            
         </tr>
         </table>
         </form>    
         <form name="processNo" method="post" action="fileManager.jsp">
         <input type="hidden" name="<%= BASE_NAME %>" value="<%= baseName %>" />
         <table class="introText" width="90%" border="0">       
         <tr>
             <td colspan="2">    
                 <input class="cssbtn btn_secondary" type="submit" name="submit" value="Cancel" onmouseover="this.className='cssbtn btn_secondary_hover'" onmouseout="this.className='cssbtn btn_secondary'"/>
             </td>           
         </tr>
         </table> 
         </form>
         <br /><br />
     <%
     } // end imageFiles and compImageFiles > 0 size
     else
     {
         message = "*ERROR no valid image type files -JPEG or PNG- OR compressed files with valid image type files within have been selected.";
         response.sendRedirect("fileManager.jsp?" + BASE_NAME + "=" + URLEncoder.encode(baseName) + "&" + MESSAGE + "=" + URLEncoder.encode(message));
     }
 } // end else fileList present
}

//
//CREATE GALLERY MULTI PROCESS
//
else if (action.equals(CREATE_GALLERY_MULTI_PROCESS))
{   
 // get params
 String fullSize = request.getParameter(CREATE_GALLERY_FULL_SIZE);
 String thumbSize = request.getParameter(CREATE_GALLERY_THUMB_SIZE);
 String galleryName = request.getParameter(CREATE_GALLERY_NAME);
 String borderColor = request.getParameter(CREATE_GALLERY_BORDER_COLOR);
 String borderStyle = request.getParameter(CREATE_GALLERY_BORDER_STYLE);
 String borderWidth = request.getParameter(CREATE_GALLERY_BORDER_WIDTH);
 String imagesBorder = request.getParameter(CREATE_GALLERY_BORDER_IMAGES);
 String fontColor = request.getParameter(CREATE_GALLERY_FONT_COLOR);
 String fontSize = request.getParameter(CREATE_GALLERY_FONT_SIZE);
 String fontWeight = request.getParameter(CREATE_GALLERY_FONT_WEIGHT);
 String fontFamily = request.getParameter(CREATE_GALLERY_FONT_FAMILY);
 String background = request.getParameter(CREATE_GALLERY_BACKGROUND);   
 String copyrightText = request.getParameter(CREATE_GALLERY_COPYRIGHT_TEXT);   
 
 //  get the fileList
 String[] fileList = request.getParameterValues(FILE_LIST);
 if ((fileList == null) || (fileList.length < 1))
 {        
     message = "*ERROR no files have been selected to be used to create image gallery.";
     response.sendRedirect("fileManager.jsp?" + BASE_NAME + "=" + URLEncoder.encode(baseName) + "&" + MESSAGE + "=" + URLEncoder.encode(message));       
 }
 // fileList present
 else
 {        
     // validate name is present and unique
     if (!ImageGalleryHandlerUtil.validateGalleryName(baseFile.getPath(), galleryName))
     {
         message = "ERROR, invalid gallery name, either path invalid, name not present or gallery (directory of same name) already exists - " + galleryName;
         response.sendRedirect("fileManager.jsp?" + BASE_NAME + "=" + URLEncoder.encode(baseName) + "&" + MESSAGE + "=" + URLEncoder.encode(message));
     }        
     // validate full size and thumb size are present
     else if ((fullSize == null) || (thumbSize == null))
     {
         message = "ERROR, gallery image sizes, full and thumbnail, must be present.";
         response.sendRedirect("fileManager.jsp?" + BASE_NAME + "=" + URLEncoder.encode(baseName) + "&" + MESSAGE + "=" + URLEncoder.encode(message));    
     }
     // validate image sizes can be parsed to integers
     else if ((!StringUtils.isNumeric(fullSize)) || (!StringUtils.isNumeric(thumbSize)))
     {
         message = "ERROR, gallery image sizes, full and thumbnail, must be numeric.";
         response.sendRedirect("fileManager.jsp?" + BASE_NAME + "=" + URLEncoder.encode(baseName) + "&" + MESSAGE + "=" + URLEncoder.encode(message));   
     }       
     else
     {       
         //  split the fileList array into separate Arrays of allowed image type files and compressed files (which have
         // allowed image type files within ROOT).
         // *JSPFileManager Does not support compressed files with path information and images further down in
         // compressed file than root.
         int size = fileList.length;    
         ArrayList imageFiles = new ArrayList();
         ArrayList compImageFiles = new ArrayList();
         for (int i = 0; i < size; i ++)
         {
             String thisFileName = URLDecoder.decode(fileList[i]);                
             File thisFile = new File(thisFileName);
             // if edit image add STRING to imageFiles
             if (FileHandlerUtil.isFileType(thisFile, FileHandlerUtil.EDIT_IMAGE))
             {
                 imageFiles.add(thisFileName);
             }
             // if comp then check for image files in root and if present add STRING to compImageFiles
             else if (FileHandlerUtil.isFileType(thisFile, FileHandlerUtil.COMP))
             {
                 if (FileHandlerUtil.compressedFileHasImageContent(thisFile))
                 {
                     compImageFiles.add(thisFileName);
                 }
             }                
         }
         
         // convert the arrayLists to arrays for use with ImageGalleryHandlerUtil
         String[] selectedImages = (String[]) imageFiles.toArray(new String[imageFiles.size()]);
         String[] selectedComps = (String[]) compImageFiles.toArray(new String[compImageFiles.size()]);            
         
         // create the gallery with the props
         try
         {
             ImageGalleryHandlerUtil.createGallery(baseFile.getPath(), 
                                                         galleryName, 
                                                         selectedImages, 
                                                         selectedComps, 
                                                         fullSize,
                                                         thumbSize, 
                                                         borderColor,
                                                         borderStyle, 
                                                         borderWidth, 
                                                         imagesBorder,
                                                         fontColor,
                                                         fontSize,
                                                         fontWeight,
                                                         fontFamily,
                                                         background,
                                                         copyrightText);
             message = "*SUCCESS* created gallery named " + galleryName + ".  <b> CLICK \"" + galleryName + ".html\" TO VIEW GALLERY</b>.";
         }
         catch (GeneralException ge)
         {
             message = ge.getMessage();   
         }                                                            
         response.sendRedirect("fileManager.jsp?" + BASE_NAME + "=" + URLEncoder.encode(basePath + FileHandlerUtil.FILE_SEP + galleryName) + "&" + MESSAGE + "=" + URLEncoder.encode(message));
     } // end validation else
 } // end fileList present
}



//
// UNKNOWN ACTION
//
else
{
%>
    <tr><td colspan="6"><br /><br />ERROR: Unknown Action, cannot continue.</td></tr>
<%
}
%>        

</body>
</html>