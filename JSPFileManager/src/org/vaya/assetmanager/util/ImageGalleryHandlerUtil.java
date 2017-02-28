package org.vaya.assetmanager.util;

import java.io.File;
import java.io.IOException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.FileUtils;


/**
 * Simple ImageGalleryHandlerUtil.
 * 
 * @author charlie collins
 * 
 * TODO - exception handling
 *
 **/
public class ImageGalleryHandlerUtil
{    

    // debug
    private static boolean debug = true;

    // vars
    private static String HTML = "html";
    private static String SUCCESS = "SUCCESS";
    private static String ERROR = "ERROR";
    private static String TRUE = "TRUE";
    private static String FALSE = "FALSE"; 
    private static String IMAGES = "images";
    private static String THUMB = "thumb";
    private static String FULL = "full";
    
        

    /**
     * Creates directory of gallery name, after validating it is unique to path
     * and then creates a .html file with links to the images and the specified additional properties (background colors, etc)
     * and then creates images of thumb and large size in the directory.
     * 
     * @param path
     * @param galleryName
     * @param imageNames
     * @param compNames
     * @param fullSize
     * @param thumbSize
     * @param borderColor
     * @param borderStyle
     * @param borderWidth
     * @param borderImages
     * @param fontColor
     * @param fontSize
     * @param fontWeight
     * @param fontFamily
     * @param background
     * @param copyrightText
     * @return
     *
     **/
    public static void createGallery(
        String path,
        String galleryName,
        String[] imageNames,
        String[] compNames,
        String fullSize,
        String thumbSize,
        String borderColor,
        String borderStyle,
        String borderWidth,
        String borderImages,
        String fontColor,
        String fontSize,
        String fontWeight,
        String fontFamily,
        String background,
        String copyrightText)
        throws GeneralException
    {

        if (debug)
        {
            System.out.println("  createGallery invoked");
            System.out.println("  galleryName = " + galleryName);
        }

        // convert encoded arrays
        if (imageNames != null)
        {
            imageNames = decodeStringArray(imageNames);
        }
        if (compNames != null)
        {
            compNames = decodeStringArray(compNames);
        }

        // create gallery html file        
        try
        {                       
            // create gallery directory and establish reference
            File startDir = new File(path); 
            FileHandlerUtil.createDirectory(startDir, galleryName);
            String galleryFullName = path + FileHandlerUtil.FILE_SEP + galleryName;
            File galleryDir = new File(galleryFullName); 
                                    
            // create gallery images subdirectory and establish reference
            FileHandlerUtil.createDirectory(galleryDir, IMAGES);
            String galleryImagesFullName = galleryFullName + FileHandlerUtil.FILE_SEP + IMAGES;
            File galleryImagesDir = new File(galleryImagesFullName);
                        
            // create gallery images/full subdirectory and establish reference
            FileHandlerUtil.createDirectory(galleryImagesDir, FULL);
            String galleryImagesFullFullName = galleryImagesFullName + FileHandlerUtil.FILE_SEP + FULL;
            File galleryImagesFullDir = new File(galleryImagesFullFullName);
            
            // create gallery images/thumb subdirectory and establish reference
            FileHandlerUtil.createDirectory(galleryImagesDir, THUMB);
            String galleryImagesThumbFullName = galleryImagesFullName + FileHandlerUtil.FILE_SEP + THUMB;
            File galleryImagesThumbDir = new File(galleryImagesThumbFullName);
            
            // create html file in gallery dir
            createGalleryHTMLFile(
                galleryDir,
                galleryName,
                imageNames,
                compNames,
                borderImages,
                borderColor,
                borderStyle,
                borderWidth,
                fontColor,
                fontSize,
                fontWeight,
                fontFamily,
                background);

            // create full size images (copy the gallery images to the full size dir and resize)
            createGalleryImages(galleryImagesFullDir, imageNames, compNames);
            resizeGalleryImages(galleryImagesFullDir, fullSize, copyrightText, 25);

            // create thumb size images (copy the gallery images to the thumb size dir and resize)
            createGalleryImages(galleryImagesThumbDir, imageNames, compNames);
            resizeGalleryImages(galleryImagesThumbDir, thumbSize, copyrightText, 10);

        }
        catch (GeneralException ge)
        {
            throw new GeneralException("*ERROR* unable to create image gallery - " + ge.getMessage());
        }
    }

    /**
     * Create html file with links to images and such for gallery.
     * 
     * @param galleryDir
     * @param galleryName
     * @param imageNames
     * @param compNames
     * @param borderImages
     * @param borderGalleryColor
     * @param borderGallerySize
     * @param fontColor
     * @param fontSize
     * @param fontWeight
     * @param fontFamily
     * @param background
     * @return
     *
     **/
    public static void createGalleryHTMLFile ( 
        File galleryDir,
        String galleryName,
        String[] imageNames,
        String[] compNames,
        String borderImages,
        String borderColor,
        String borderStyle,
        String borderWidth,
        String fontColor,
        String fontSize,
        String fontWeight,
        String fontFamily,
        String background) throws GeneralException
    {
        // buffer for new file, gallery, contents
        StringBuffer out = new StringBuffer();

        // header
        out.append(
            createGalleryHeader(
                galleryName,
                borderColor,
                borderStyle,
                borderWidth,
                fontColor,
                fontSize,
                fontWeight,
                fontFamily,
                background));

        int imageSize = 0;
        int compSize = 0;
        if (imageNames != null)
        {
            imageSize = imageNames.length;
        }
        if (compNames != null)
        {
            compSize = compNames.length;
        }

        // count for break        
        int imageCount = 0;

        // image border
        String imageBorder = "0";
        if (borderImages != null)
            imageBorder = "1";

        // process images
        if (debug)
            System.out.println("  processing selected images ");
        for (int i = 0; i < imageSize; i++)
        {
            imageCount++;
            String imageFullName = imageNames[i];
            String imageName =
                imageFullName.substring(
                    imageFullName.lastIndexOf(FileHandlerUtil.FILE_SEP) + 1,
                    imageFullName.length());
            if (debug)
            {
                System.out.println("  imageFullName = " + imageFullName);
                System.out.println("  imageName = " + imageName);
            }

            out.append(
                    "<a href=\"images/full/"
                    + imageName
                    + "\"><img src=\"images/thumb/"
                    + imageName
                    + "\" border=\""
                    + imageBorder
                    + "\" /></a>  \n");

            if ((imageCount % 5) == 0)
            {
                out.append("<br /><br />\n");
            }
        }

        // process comps
        if (debug)
            System.out.println("  processing selected comp files ");
        for (int i = 0; i < compSize; i++)
        {
            String compName = compNames[i];

            // process this comp file, it HAS images, it must or it would not have gotten here
            // get the names out of the entries in the file in order to create the HTML 
            ArrayList zipContents = ZipHandlerUtil.getZipFileContents(new File(compName));
            int size = 0;
            if (zipContents != null)
                size = zipContents.size();
            for (int j = 0; j < size; j++)
            {
                String entry = (String) zipContents.get(j);
                if (FileHandlerUtil.isFileType(entry, FileHandlerUtil.EDIT_IMAGE))
                {
                    imageCount++;
                    String imageName = entry.substring(entry.lastIndexOf(FileHandlerUtil.FILE_SEP) + 1, entry.length());                    
                    if (debug)
                    {
                        System.out.println("  zip file entry = " + entry);
                        System.out.println("  imageName = " + imageName);
                    }

                    out.append(
                            "<a href=\"images/full/"                        
                            + imageName
                            + "\"><img src=\"images/thumb/"                            
                            + imageName
                            + "\" border=\""
                            + imageBorder
                            + "\" /></a>&#160;\n");

                    if ((imageCount % 5) == 0)
                    {
                        out.append("<br /><br />\n");
                    }
                }
            }
        }
        out.append("<br />\n");

        // footer
        out.append(createGalleryFooter());

        String galleryHTMLFileName = FileHandlerUtil.getCanonicalPath(galleryDir) + FileHandlerUtil.FILE_SEP + galleryName + ".html";

        if (debug)
            System.out.println("  galleryHTMLFileName = " + galleryHTMLFileName);

        // create file
        try
        {        
            FileHandlerUtil.createTextFile(
                galleryHTMLFileName,
                galleryHTMLFileName,
                FileHandlerUtil.FALSE,
                out.toString());
        }
        catch (GeneralException ge)
        {
            throw new GeneralException("*ERROR* unable to create gallery html file.");   
        }        
    }

    /**
     * Create gallery images by copying images and compressed files to gallery location.
     * Also unzip compressed files.
     * 
     * @param galleryDir
     * @param imageNames
     * @param compNames
     * @return
     **/
    public static String createGalleryImages(File galleryImagesDir, String[] imageNames, String[] compNames)
    {
        String message = null;        
        
        int imageSize = 0;
        int compSize = 0;
        if (imageNames != null)
            imageSize = imageNames.length;
        if (compNames != null)
            compSize = compNames.length;

        message = "<b>SUCCESS</b> created gallery images.";

        // process images
        for (int i = 0; i < imageSize; i++)
        {
            String imageFullName = imageNames[i];

            // copy file to directory 
            try
            {
                // get originals, copy into galleryImagesDir
                File origFile = new File(imageFullName);
                FileUtils.copyFileToDirectory(origFile, galleryImagesDir);                
            }
            catch (IOException ioe) 
            {
                message = "<b>ERROR</b> unable to create gallery images.";
                ioe.printStackTrace();
            }            
        }

        // process zip files        
        for (int i = 0; i < compSize; i++)
        {
            String compFullName = compNames[i];
            String compName =
                compFullName.substring(compFullName.lastIndexOf(FileHandlerUtil.FILE_SEP) + 1, compFullName.length());

            // copy file to directory 
            try
            {
                // copy originals into galleryImagesDir
                FileUtils.copyFileToDirectory(new File(compFullName), galleryImagesDir);
            }
            catch (IOException ioe)
            {
                message = "<b>ERROR</b> unable to create gallery images.";
                ioe.printStackTrace();
            }

            // unzip file
            try
            {
                File zipImageFile = new File(galleryImagesDir + FileHandlerUtil.FILE_SEP + compName);
                FileHandlerUtil.unCompressFile(zipImageFile);

                // delete zip file
                FileHandlerUtil.deleteFile(zipImageFile);
            }
            catch (GeneralException ge)
            {
                message = "<b>ERROR</b> unable to create gallery images from zip file.";
                ge.printStackTrace();
            }

        }
        return message;
    }

    /**
     * Resize images to selected full size.
     * 
     * @param galleryDir
     * @param galleryImagesFullDir
     * @param resizeTo
     * @return
     **/
    public static void resizeGalleryImages(File imagesDir, String resizeTo, String copyrightText, int copyrightFontSize) throws GeneralException
    {
    
        if (debug) System.out.println("  resizeFullGalleryImages invoked");

        // first get the image file names in the directory
        if (imagesDir != null)
        {
            List images = FileHandlerUtil.getImageList(FileHandlerUtil.getCanonicalPath(imagesDir));
            for (int i = 0; i < images.size(); i++)
            {
                File thisFile = (File) images.get(i);                
                FileHandlerUtil.resizeImage(FileHandlerUtil.getCanonicalPath(thisFile), FileHandlerUtil.getCanonicalPath(thisFile), "false", resizeTo);
                
                // if copyrightText != null then perform copyright emboss
                {
                    if (copyrightText != null)
                    {
                        try
                        {
                            ImageHandlerUtil.copyrightText(copyrightFontSize, copyrightText, thisFile);
                        }
                        catch (IOException ioe)
                        {
                            ioe.printStackTrace();
                            throw new GeneralException(ioe.getMessage());                            
                        }
                    }
                }
            }
        }        
    }    

    /**
     * Decode strings in an array.
     * 
     * @param array
     * @return
     **/
    public static String[] decodeStringArray(String[] array)
    {
        String[] resultArray = null;
        if (array != null)
        {
            resultArray = new String[array.length];
            for (int i = 0; i < array.length; i++)
            {
                String encodedString = array[i];
                resultArray[i] = URLDecoder.decode(encodedString);
            }
        }
        return resultArray;
    }

    /**
     * Create gallery header using DIV and styling it with attributes supplied.
     * 
     * @param galleryName
     * @param borderColor
     * @param borderSize
     * @param fontColor
     * @param fontSize
     * @param fontWeight
     * @param fontFamily
     * @param background
     * @return
     *
     **/
    public static String createGalleryHeader(
        String galleryName,
        String borderColor,
        String borderStyle,
        String borderWidth,
        String fontColor,
        String fontSize,
        String fontWeight,
        String fontFamily,
        String background)
    {

        StringBuffer HTMLHeader = new StringBuffer();
        HTMLHeader.append(
            "<div style=\"font-size:"
                + fontSize
                + "; font-family:"
                + fontFamily
                + "; font-weight:"
                + fontWeight
                + "; border-style:"
                + borderStyle
                + "; border-color: "
                + borderColor
                + "; border-width: "
                + borderWidth
                + "; color:"
                + fontColor
                + "; background-color:"
                + background
                + "; padding:10px; margin:10px "
                + "\">\n");
        HTMLHeader.append(galleryName + "\n");
        HTMLHeader.append("<br /><br />\n");
        return HTMLHeader.toString();
    }

    /**
     * Create footer.
     * 
     * @return
     **/
    public static String createGalleryFooter()
    {
        StringBuffer HTMLFooter = new StringBuffer();
        HTMLFooter.append("</div>\n");
        return HTMLFooter.toString();
    }

    /**
     * Validate gallery name.
     * 
     * @param baseDir
     * @param galleryName
     * @return
     **/
    public static boolean validateGalleryName(String baseDir, String galleryName)
    {
        boolean valid = false;
        if ((baseDir != null) && (galleryName != null))
        {
            // check if name valid characters
            if (FileHandlerUtil.validFileName(galleryName))
            {
                // check if gallery with same name already exists
                File galleryFile = new File(baseDir + FileHandlerUtil.FILE_SEP + galleryName);
                if (!galleryFile.exists())
                {
                    valid = true;
                }
            }
        }
        return valid;
    }

}
