/**
 * Copyright (C) Intersect 2010.
 * 
 * This module contains Proprietary Information of Intersect,
 * and should be treated as Confidential.
 *
 * $Id: BaseImageCreator.java 2 2010-09-06 01:33:48Z intersect.engineering.team@gmail.com $
 */
package dp.slideviewer.ndpread;

import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.awt.image.Raster;
import java.io.File;
import java.io.IOException;

import java.io.OutputStream;
import javax.imageio.ImageIO;

/**
 * Base image creator which writes the image to the format specified by the fileType parameter to the constructor.
 * 
 * @version $Rev: 2 $
 */
public class BaseImageCreator
{
    private final String fileType;

    public BaseImageCreator(String fileType)
    {
        checkFormatIsSupported(fileType);
        this.fileType = fileType;
    }

    public void createImageFromNdpiBytes(byte[] imageBytes, int pixelWidth, int pixelHeight, String outputFileName,
            String outputDirectory) throws IOException
    {
        int pixelStride = 3;
        int scanlineStride = calculateScanlineStride(pixelWidth, pixelStride);
        int[] bands = {2, 1, 0};

        byte[] invertedImageBytes = flipBytes(imageBytes, scanlineStride, pixelHeight);

        BufferedImage image = new BufferedImage(pixelWidth, pixelHeight, BufferedImage.TYPE_3BYTE_BGR);
        DataBufferByte dataByteBuffer = new DataBufferByte(invertedImageBytes, invertedImageBytes.length);
        Raster raster = Raster.createInterleavedRaster(dataByteBuffer, pixelWidth, pixelHeight, scanlineStride,
                pixelStride, bands, null);
        image.setData(raster);

        String outputFilePath = decideWhereToStoreImage(raster, outputFileName, outputDirectory, pixelWidth,
                pixelHeight, scanlineStride);

        File file = new File(outputFilePath);
        writeImage(fileType, image, file);
    }

    public void createImageFromNdpiBytes(byte[] imageBytes, int pixelWidth, int pixelHeight, OutputStream os)
        throws IOException
    {
        int pixelStride = 3;
        int scanlineStride = calculateScanlineStride(pixelWidth, pixelStride);
        int[] bands = {2, 1, 0};

        byte[] invertedImageBytes = flipBytes(imageBytes, scanlineStride, pixelHeight);

        BufferedImage image = new BufferedImage(pixelWidth, pixelHeight, BufferedImage.TYPE_3BYTE_BGR);
        DataBufferByte dataByteBuffer = new DataBufferByte(invertedImageBytes, invertedImageBytes.length);
        Raster raster = Raster.createInterleavedRaster(dataByteBuffer, pixelWidth, pixelHeight, scanlineStride,
                pixelStride, bands, null);
        image.setData(raster);

        writeImage(fileType, image, os);
    }
    
    /**
     * This allows subclasses to do some further processing to decide where the image should be stored. This will be
     * used for the filtering empty tiles functionality. This default implementation just appends together the requested
     * output file path and file name
     * 
     * @param raster
     *            the image raster
     * @param outputFileName
     *            the requested output file name
     * @param outputDirectory
     *            the requested output directory
     * @param pixelWidth
     *            the pixel width of the image
     * @param scanlineStride
     * @return the fully qualified output file path
     */
    protected String decideWhereToStoreImage(Raster raster, String outputFileName, String outputDirectory,
            int pixelWidth, int pixelHeight, int scanlineStride)
    {
        return outputDirectory + File.separator + outputFileName;
    }

    protected void writeImage(String fileType, BufferedImage image, File file) throws IOException
    {
        boolean result = ImageIO.write(image, fileType, file);
        if (!result)
        {
            throw new RuntimeException("Image writing failed. Perhaps the image io plugin is missing.");
        }
    }

    protected void writeImage(String fileType, BufferedImage image, OutputStream os) throws IOException
    {
        //boolean result = ImageIO.write(image, fileType, file);
        //if (!result)
        //{
        //    throw new RuntimeException("Image writing failed. Perhaps the image io plugin is missing.");
        //}
    }

    /**
     * Calculate the number of bytes per "row" in the image. Each pixel is [pixelStride] bytes, but lines are padded to
     * 4 byte (DWORD) boundaries
     * 
     * @param pixelWidth
     *            of the image data
     * @param pixelStride
     *            the number of bytes per pixel
     * @return
     */
    protected int calculateScanlineStride(int pixelWidth, int pixelStride)
    {
        // byte array is padded to dword boundaries (4 bytes) so calculate what the row length (scanline stride) will be
        int pixelBytesPerRow = pixelWidth * pixelStride;
        int mod = pixelBytesPerRow % 4;
        int padding = mod > 0 ? 4 - mod : 0;
        int scanlineStride = (pixelWidth * pixelStride) + padding;
        return scanlineStride;
    }

    /**
     * This converts an array of image bytes from bottom-up to top-down.
     * 
     * @param original
     *            the bottom-up bytes
     * @param scanlineStride
     *            number of bytes per row
     * @param height
     *            number of rows
     * @return
     */
    protected byte[] flipBytes(byte[] original, int scanlineStride, int height)
    {
        byte[] newOne = new byte[original.length];
        int positionInNewArray = 0;
        for (int j = height - 1; j >= 0; j--)
        {
            for (int i = 0; i < scanlineStride; i++)
            {
                int arrayIndexToPull = j * scanlineStride + i;
                newOne[positionInNewArray] = original[arrayIndexToPull];
                positionInNewArray++;
            }
        }
        return newOne;
    }

    private void checkFormatIsSupported(String fileType)
    {
        String[] writerFormats = ImageIO.getWriterFormatNames();
        for (String format : writerFormats)
        {
            if (format.equals(fileType))
            {
                return;
            }
        }
        throw new RuntimeException("unsupported image format");
    }

    public String getFileType()
    {
        return this.fileType;
    }
}
