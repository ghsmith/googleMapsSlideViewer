/**
 * Copyright (C) Intersect 2010.
 * 
 * This module contains Proprietary Information of Intersect,
 * and should be treated as Confidential.
 *
 * $Id: ImageInformation.java 2 2010-09-06 01:33:48Z intersect.engineering.team@gmail.com $
 */
package dp.slideviewer.ndpread;

/**
 * Wrapper class that holds various pieces of info about an NDPI file.
 * 
 * @version $Rev: 2 $
 */
public class ImageInformation
{

    private long imageWidthInNanometres;
    private long imageHeightInNanometres;
    private long imageWidthInPixels;
    private long imageHeightInPixels;
    private long physicalXPositionOfCentreInNanometres;
    private long physicalYPositionOfCentreInNanometres;
    private float sourceLensMagnification;

    public long getImageWidthInNanometres()
    {
        return imageWidthInNanometres;
    }

    public void setImageWidthInNanometres(long imageWidthInNanometres)
    {
        this.imageWidthInNanometres = imageWidthInNanometres;
    }

    public long getImageHeightInNanometres()
    {
        return imageHeightInNanometres;
    }

    public void setImageHeightInNanometres(long imageHeightInNanometres)
    {
        this.imageHeightInNanometres = imageHeightInNanometres;
    }

    public long getImageWidthInPixels()
    {
        return imageWidthInPixels;
    }

    public void setImageWidthInPixels(long imageWidthInPixels)
    {
        this.imageWidthInPixels = imageWidthInPixels;
    }

    public long getImageHeightInPixels()
    {
        return imageHeightInPixels;
    }

    public void setImageHeightInPixels(long imageHeightInPixels)
    {
        this.imageHeightInPixels = imageHeightInPixels;
    }

    public long getPhysicalXPositionOfCentreInNanometres()
    {
        return physicalXPositionOfCentreInNanometres;
    }

    public void setPhysicalXPositionOfCentreInNanometres(long physicalXPositionOfCentreInNanometres)
    {
        this.physicalXPositionOfCentreInNanometres = physicalXPositionOfCentreInNanometres;
    }

    public long getPhysicalYPositionOfCentreInNanometres()
    {
        return physicalYPositionOfCentreInNanometres;
    }

    public void setPhysicalYPositionOfCentreInNanometres(long physicalYPositionOfCentreInNanometres)
    {
        this.physicalYPositionOfCentreInNanometres = physicalYPositionOfCentreInNanometres;
    }

    public double locateBottomEdge()
    {
        return ((double) physicalYPositionOfCentreInNanometres) + getHalfOfHeightInNanometres();
    }

    public double locateTopEdge()
    {
        return ((double) physicalYPositionOfCentreInNanometres) - getHalfOfHeightInNanometres();
    }

    public double locateRightEdge()
    {
        return ((double) physicalXPositionOfCentreInNanometres) + getHalfOfWidthInNanometres();
    }

    public double locateLeftEdge()
    {
        return ((double) physicalXPositionOfCentreInNanometres) - getHalfOfWidthInNanometres();
    }

    private double getHalfOfWidthInNanometres()
    {
        return imageWidthInNanometres / 2D;
    }

    private double getHalfOfHeightInNanometres()
    {
        return imageHeightInNanometres / 2D;
    }

    public float getSourceLensMagnification()
    {
        return sourceLensMagnification;
    }

    public void setSourceLensMagnification(float sourceLensMagnification)
    {
        this.sourceLensMagnification = sourceLensMagnification;
    }

}