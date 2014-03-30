/**
 * Copyright (C) Intersect 2010.
 * 
 * This module contains Proprietary Information of Intersect,
 * and should be treated as Confidential.
 *
 * $Id: TilePositionCalculator.java 2 2010-09-06 01:33:48Z intersect.engineering.team@gmail.com $
 */
package dp.slideviewer.ndpread;

import java.util.ArrayList;
import java.util.List;

/**
 * @version $Rev: 2 $
 * 
 */
public class TilePositionCalculator
{

    private static final String COMMA = ", ";
    private final double xPositionOfRightEdge;
    private final double yPositionOfBottomEdge;
    private final double xPositionOfCentreOfLeftMostTile;
    private final double yPositionOfCentreOfTopMostTile;
    private final double tileWidthNanometresAsDouble;
    private final double tileHeightNanometresAsDouble;
    private final double halfOfTileWidthInNanometres;
    private final double halfOfTileHeightInNanometres;
    private List<Long> tileXPositions;
    private List<Long> tileYPositions;
    private double xPositionOfLeftEdge;
    private double yPositionOfTopEdge;
    private final int tileWidthInPixels;
    private final int tileHeightInPixels;

    public TilePositionCalculator(ImageInformation map, int tileWidthInPixels, int tileHeightInPixels,
            float requestedMagnification)
    {
        this.tileWidthInPixels = tileWidthInPixels;
        this.tileHeightInPixels = tileHeightInPixels;

        this.xPositionOfLeftEdge = map.locateLeftEdge();
        this.xPositionOfRightEdge = map.locateRightEdge();
        this.yPositionOfTopEdge = map.locateTopEdge();
        this.yPositionOfBottomEdge = map.locateBottomEdge();

        double tileWidthPixelsAsDouble = (double) tileWidthInPixels;
        double tileHeightPixelsAsDouble = (double) tileHeightInPixels;
        double imageWidthNanometresAsDouble = (double) map.getImageWidthInNanometres();
        double imageWidthPixelsAsDouble = (double) map.getImageWidthInPixels();
        double imageHeightNanometresAsDouble = (double) map.getImageHeightInNanometres();
        double imageHeightPixelsAsDouble = (double) map.getImageHeightInPixels();

        double requestedMag = (double) requestedMagnification;
        double sourceMag = (double) map.getSourceLensMagnification();
        double ratio = sourceMag / requestedMag;

        // calculate tile width/height in NM by dividing by NM per pixel as calculated from total image dimensions
        // stored as double so we don't get rounding errors
        this.tileWidthNanometresAsDouble = tileWidthPixelsAsDouble * imageWidthNanometresAsDouble * ratio
                / imageWidthPixelsAsDouble;
        this.tileHeightNanometresAsDouble = tileHeightPixelsAsDouble * imageHeightNanometresAsDouble * ratio
                / imageHeightPixelsAsDouble;

        // calculate half a tile so we can get from top left to middle of first tile
        this.halfOfTileWidthInNanometres = tileWidthNanometresAsDouble / 2D;
        this.halfOfTileHeightInNanometres = tileHeightNanometresAsDouble / 2D;

        // round the position of the first tile before we use it (since it will be rounded before passing to the api
        // anyway)
        this.xPositionOfCentreOfLeftMostTile = round(this.xPositionOfLeftEdge + this.halfOfTileWidthInNanometres);
        this.yPositionOfCentreOfTopMostTile = round(this.yPositionOfTopEdge + this.halfOfTileHeightInNanometres);

        // do the actual calculations based on positions of the first tile
        this.tileXPositions = calculateTiles(xPositionOfCentreOfLeftMostTile, xPositionOfRightEdge, 
                tileWidthNanometresAsDouble);
        this.tileYPositions = calculateTiles(yPositionOfCentreOfTopMostTile, yPositionOfBottomEdge,
                tileHeightNanometresAsDouble);
    }

    public List<Long> getTileXPositions()
    {
        return tileXPositions;
    }

    public List<Long> getTileYPositions()
    {
        return tileYPositions;
    }

    public int getTileWidthInPixels()
    {
        return tileWidthInPixels;
    }

    public int getTileHeightInPixels()
    {
        return tileHeightInPixels;
    }

    public int getTotalNumberOfTiles()
    {
        return tileXPositions.size() * tileYPositions.size();
    }

    private List<Long> calculateTiles(double positionOfFirstTile, double positionOfEdge, double tileSizeInNanometres)
    {
        double halfATileInNanometres = tileSizeInNanometres / 2D;
        List<Long> tilePositions = new ArrayList<Long>();

        // make sure we always have at least one tile, no matter how small the image is
        tilePositions.add(Math.round(positionOfFirstTile));

        // now keep adding a tile's worth until we reach the edge
        double currentTilePosition = round(positionOfFirstTile + tileSizeInNanometres);
        while ((currentTilePosition - halfATileInNanometres) < positionOfEdge)
        {
            long longValue = Math.round(currentTilePosition);
            tilePositions.add(longValue);
            currentTilePosition = round(currentTilePosition + tileSizeInNanometres);
        }
        return tilePositions;
    }

    private double round(double doubleToRound)
    {
        long longValue = Math.round(doubleToRound);
        return (double) longValue;
    }

}