package org.vaya.assetmanager.util;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.awt.image.RenderedImage;
import java.awt.image.renderable.ParameterBlock;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;

import javax.imageio.ImageIO;
import javax.media.jai.Interpolation;
import javax.media.jai.JAI;
import javax.media.jai.ParameterBlockJAI;
import javax.media.jai.PlanarImage;
import javax.media.jai.RenderedOp;
import javax.media.jai.operator.TransposeDescriptor;
import javax.media.jai.operator.TransposeType;

/**
 * Simple ImageHandlerUtil wrapper to JAI.
 * Adapted from 
 * http://java.sun.com/products/java-media/jai/forDevelopers/jai1_0_1guide-unc/Introduction.doc.html
 * http://java.sun.com/products/java-media/jai/forDevelopers/jai1_0_1guide-unc/JAITOC.fm.html
 * 
 * @author charlie collins
 * 
 * TODO fix exception handling so that there is some ;)
 *
 **/ 
public class ImageHandlerUtil
{
    // debug
    private static boolean debug = false;

    // vars
    public static String SCALE = "SCALE";    
    public static String TRANSPOSE = "TRANSPOSE";
    public static String COMPOSITE = "COMPOSITE";

    // main
    public static void main(String[] args) throws Exception
    {
        if (args.length != 3)
        {
            System.err.println("Usage: java ImageHandlerUtil FILE ACTION VALUE");
            System.err.println("  FILE - file to operate on");
            System.err.println("  ACTION - what do do, values are SCALE, TRANSPOSE");
            System.err.println("  VALUE - passed to action, pixels width to scale, rotate or flip how to transpose");
            System.exit(1);
        }

        // process
        if (args[1].equals(SCALE))
        {
            processImage(args[0], null, SCALE, args[2]);
        }
        else if (args[1].equals(TRANSPOSE))
        {
            processImage(args[0], null, TRANSPOSE, args[2]);
        }        
        else
        {
            System.err.println(" ERROR: do no recognize ACTION (valid types are SCALE, TRANSPOSE)");
            System.exit(1);
        }
        System.exit(0);
    }

    /**
     * Process image using passed in value for ACTION and FACTOR.
     *   ACTION - SCALE  (factor is size)
     *   ACTION - TRANSPOSE (factor is how to transpose as taken from TransposeDescriptor static fields)
     *
     * @param inFile
     * @param action
     * @param value
     *
     **/
    public static void processImage(String origFileName, String newFileName, String action, String value) throws IOException 
    {
        if (debug)
        {
            System.out.println(" ImageHandlerUtil.processImage() invoked");
            System.out.println("  - origFileName = " + origFileName);
            System.out.println("  - newFileName = " + newFileName);
            System.out.println("  - action = " + action);
            System.out.println("  - value = " + value);
        }
        
        // if newFileName is null save over origFileName
        if (newFileName == null) newFileName = origFileName;        

        // load image to process
        RenderedOp image = loadImage(origFileName);
        if (image != null)
        {

            // setup outFile as newFileName
            String outFile = newFileName;
            if (debug)
            {                
                System.out.println("  outFile = " + outFile);
            }

            // setup newImage
            RenderedOp newImage = null;

            // process action SCALE
            if (action.equals(SCALE))
            {
                // get original image properties to determine how to scale						
                int width = image.getWidth();
                int height = image.getHeight();
                if (debug)
                    System.out.println(" original image width = " + width + " and height = " + height);
                              
                // scale WIDTH always
                newImage = scaleX(image, (float) Integer.parseInt(value));                
            }
            // process action TRANSPOSE
            else if (action.equals(TRANSPOSE))
            {
                TransposeType type = null;
                ;
                if (value != null)
                {
                    // get int for value passed in using TransposeDescriptor fields
                    if (value.equals(TransposeDescriptor.FLIP_ANTIDIAGONAL.getName()))
                    {
                        type = TransposeDescriptor.FLIP_ANTIDIAGONAL;
                    }
                    if (value.equals(TransposeDescriptor.FLIP_DIAGONAL.getName()))
                    {
                        type = TransposeDescriptor.FLIP_DIAGONAL;
                    }
                    if (value.equals(TransposeDescriptor.FLIP_HORIZONTAL.getName()))
                    {
                        type = TransposeDescriptor.FLIP_HORIZONTAL;
                    }
                    if (value.equals(TransposeDescriptor.FLIP_VERTICAL.getName()))
                    {
                        type = TransposeDescriptor.FLIP_VERTICAL;
                    }
                    if (value.equals(TransposeDescriptor.ROTATE_180.getName()))
                    {
                        type = TransposeDescriptor.ROTATE_180;
                    }
                    if (value.equals(TransposeDescriptor.ROTATE_270.getName()))
                    {
                        type = TransposeDescriptor.ROTATE_270;
                    }
                    if (value.equals(TransposeDescriptor.ROTATE_90.getName()))
                    {
                        type = TransposeDescriptor.ROTATE_90;
                    }

                    // transpose
                    newImage = transpose(image, type);
                }
                else
                {
                    System.err.println("ERROR: value to action TRANSPOSE is null");
                }
            }
            else
            {
                System.err.println("ERROR: do not regonize action type of - " + action);
            }

            // save
            saveImage(newImage, outFile);

        }
        else
        {
            System.err.println(" ERROR: not able to create image - " + newFileName);
        }
    }

    /**
     * 
     * @param image
     * @param amount
     * @return
     */
    public static RenderedOp scale(RenderedOp image, float amount)
    {
        ParameterBlockJAI pb = new ParameterBlockJAI("scale");
        pb.addSource(image);
        pb.setParameter("xScale", amount);
        pb.setParameter("yScale", amount);
        pb.setParameter("xTrans", 0.0F);
        pb.setParameter("yTrans", 0.0F);
        pb.setParameter("interpolation", Interpolation.getInstance(Interpolation.INTERP_BILINEAR));
        ///pb.setParameter("interpolation", Interpolation.getInstance(Interpolation.INTERP_BICUBIC));
        return JAI.create("scale", pb);
    }

    /**
     * 
     * @param image
     * @param type
     * @return
     */
    public static RenderedOp transpose(RenderedOp image, TransposeType type)
    {
        // use pb?
        return JAI.create("transpose", image, type);
    }

    /**
     * 
     * @param image
     * @param angle
     * @return
     */
    public static RenderedOp rotate(RenderedOp image, double angle)
    {
        ParameterBlockJAI pb = new ParameterBlockJAI("rotate");
        pb.addSource(image);
        pb.setParameter("xOrigin", 0.0F);
        pb.setParameter("yOrigin", 0.0F);
        pb.setParameter("angle", (float) angle);
        pb.setParameter("interpolation", Interpolation.getInstance(Interpolation.INTERP_BICUBIC));
        return JAI.create("rotate", pb);
    }

    /**
     * 
     * @param image
     * @param width
     * @return
     */
    public static RenderedOp scaleX(RenderedOp image, float width)
    {
        float scaleFactor = width / (float) image.getWidth();
        return scale(image, scaleFactor);
    }
    
    /**
     * 
     * @param image
     * @param height
     * @return
     */
    public static RenderedOp scaleY(RenderedOp image, float height)
    {
        float scaleFactor = height / (float) image.getHeight();
        return scale(image, scaleFactor);
    } 

    /**
     * 
     * @param filepath
     * @return
     */
    public static RenderedOp loadImage(String filepath)
    {
        // use pb?
        return (RenderedOp) JAI.create("fileload", filepath);
    }
    
    /**
     * 
     * @param filepath
     * @return
     */
    public static PlanarImage loadPlanarImage(String filepath)
    {
        // use pb?
        return (PlanarImage) JAI.create("fileload", filepath);
    }

    /**
     * 
     * @param image
     * @param filepath
     * @throws IOException
     */
    public static void saveImage(RenderedOp image, String filepath) throws IOException
    {
        final String JPEG = "JPEG";
        final String JPG = "JPG";
        final String BMP = "BMP";
        final String PNG = "PNG";
        
        String type = JPEG;
        
        // determine type for encoding
        String ext = filepath.substring(filepath.lastIndexOf(".") + 1, filepath.length());        
        if ((ext.equalsIgnoreCase(JPEG)) || (ext.equalsIgnoreCase(JPG))) type = JPEG;
        else if (ext.equalsIgnoreCase(BMP)) type = BMP;
        else if (ext.equalsIgnoreCase(PNG)) type = PNG;
        else throw new IOException("ERROR, cannot save image file of type - " + type + " only JPEG, JPG, BMP and PNG are supported.");
                      
        JAI.create("filestore", image, filepath, type, null);
    }
    
    /**
     * Overlay.
     * 
     * @param imagePath1
     * @param imagePath2
     * @param saveImagePath
     */
    public static void overLay(String imagePath1, String imagePath2, String saveImagePath)
    {        
        // get images
        RenderedImage _src1 = (RenderedImage) loadImage(imagePath1);
        RenderedImage _src2 = (RenderedImage) loadImage(imagePath2);
        
        // pb
        ParameterBlock pb = new ParameterBlock();
        pb.addSource(_src1);          // The first image
        pb.addSource(_src2);          // The second image

        // overlay images
        RenderedOp op = JAI.create("overlay", pb);
        
        // save
        try
        {
            saveImage(op, saveImagePath);
        }
        catch (IOException ioe)
        {
            ioe.printStackTrace();
        }
    }    
    
    /**
     * copyrightText.
     * 
     * @param text
     * @param imageFile
     */
    public static void copyrightText(int fontSize, String text, File imageFile) throws IOException
    {                
        BufferedImage image = null;
        image = ImageIO.read(imageFile);
                
        // setup values for text color and transparency level
        // (use this to set the copy text to black or white and allow user to choose that)
        int red = 0;
        int green = 0;
        int blue = 0;
        int transparency = 60;
        
        // get "Graphics" and do the highlighting
        Graphics graphics = image.getGraphics();
        
        // color
        // defaults are BLACK, BLUE, CYAN, DARK_GRAY, GRAY, GREEN, LIGHT_GRAY, MAGENTA, ORANGE, PINK, RED, WHITE, YELLOW
        Color color = new Color(red, green, blue, 255 * transparency / 100);
        graphics.setColor(color);
        
        // font
        // sansserif, serif, dialog, monospaced are the only JVM supplied safe ones
        int fontStyle = Font.PLAIN; // PLAIN, BOLD, ITALIC, BOLD, BOLDITALIC
        Font font = new Font("sansserif", fontStyle, fontSize);
        graphics.setFont(font);
        
        // draw
        graphics.drawString(text, 10, image.getHeight() - 10);
        
        // save modified image
        String format = FileHandlerUtil.getExtension(imageFile.getName());
        ImageIO.write(image, format, imageFile);        
    } 
    
    
    /**
     * 
     * @param filePath
     * @return
     */
    public static HashMap getImageProperties(String filePath)
    {
        HashMap result = null;
        try
        {
            RenderedOp image = loadImage(filePath);
            final String HEIGHT = "HEIGHT";
            final String WIDTH = "WIDTH";
            if (image != null)
            {
                int height = image.getHeight();
                int width = image.getWidth();
                result = new HashMap();
                result.put(HEIGHT, new Integer(height));
                result.put(WIDTH, new Integer(width));            
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }        
        return result;        
    }

}
