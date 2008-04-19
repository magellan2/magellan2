/*
 *  Copyright (C) 2000-2004 Roger Butenuth, Andreas Gampe,
 *                          Stefan Goetz, Sebastian Pappert,
 *                          Klaas Prause, Enno Rehling,
 *                          Sebastian Tusk, Ulrich Kuester,
 *                          Ilja Pavkovic
 *
 * This file is part of the Eressea Java Code Base, see the
 * file LICENSING for the licensing information applying to
 * this file.
 *
 */

package magellan.client.swing.map;

import java.awt.Dimension;
import java.awt.Point;
import java.awt.Polygon;
import java.awt.Rectangle;
import java.io.InputStream;
import java.net.URL;
import java.util.Properties;

import magellan.library.utils.MagellanImages;
import magellan.library.utils.logging.Logger;


/**
 * A class holding the information necessary to describe the  geometry of a map cell. The
 * information should be sufficient to calculate the size of a cell, an optional offset of the
 * opaque part of an image representing the cell and the position of a cell with arbitrary
 * coordinates on the map in pixels. Some effort has been put into the routines calculating the
 * pixel position of a cell and for retrieving the coordinate of a cell containing a certain pixel
 * position with regard to good accuracy.
 */
public class CellGeometry {
	private static final Logger log = Logger.getInstance(CellGeometry.class);
	private float scaleFactor = 1.0f;
	private Polygon cell = null;
	private Polygon scaledPoly = null;
	private Point imgOffset = new Point(0, 0);
	private Point scaledImgOffset = new Point(0, 0);
	private Dimension imgSize = new Dimension(0, 0);
	private Dimension scaledImgSize = new Dimension(0, 0);
	private Dimension unscaledCellSize = new Dimension(0, 0); // cell.getBounds().width/height + 1
	private Dimension scaledCellSize = new Dimension(0, 0); // unscaledCellSize.width/height * scaleFactor
	private float cellShiftXX = 0.0f; // unscaledCellSize.width
	private float cellShiftXY = 0.0f; // unscaledCellSize.width / 2.0f
	private float cellShiftYY = 0.0f; // -(cell.ypoints[2] + 1)

	/**
	 * Creates a new CellGeometry object with no geometry data set.
	 */
	public CellGeometry() {
	}

	/**
	 * Creates a new CellGeometry object initializing itself with the data available in the
	 * specified resource file. The file is supposed to have the following layout:
	 * 
	 * <p>
	 * <tt>x0=32<br>x1=63<br>
	 * x2=63<br>
	 * x3=32<br>
	 * x4=0<br>
	 * x5=0<br>
	 * y0=0<br>
	 * y1=16<br>
	 * y2=47<br>
	 * y3=63<br>
	 * y4=47<br>
	 * y5=16<br>
	 * imgOffsetx=8<br>
	 * imgOffsety=8<br>
	 * imgSizex=80<br>
	 * imgSizey=80 </tt>
	 * </p>
	 * These values are default values if they cannot be retrieved from the file. The coordinates
	 * are interpreted to describe a symmetric hexagon that sits at the coordinate axes with
	 * positive x values increasing to the right and positive y values increasing downwards. The
	 * x0 through x5 values describe the x values of the points that make up the hexagonal cell,
	 * y0 through y5 are the respective y values. x0, y0 represent the top node of the hexagon,
	 * the other points are numbered up clockwise. Therefore x4, x5 and y0 are expected to always
	 * be 0, else no guarantee can be made as to the correctness of the position calculations. The
	 * imgOffset and imgSize values give additional information about how the hexagon is located
	 * within the graphics files used together with this cellgeometry. This information is
	 * necessary to calculate the images' pixel positions for  painting. imgOffset is the offset
	 * of the opaque part of the image representing a cell within the graphics file, imgSize
	 * denotes the size of the graphics files in pixels. The created CellGeometry is fully
	 * initialized with this data, no further information has to be given.
	 *
	 * @param fileName the name of the file to read the geometry data from. fileName is retrieved
	 * 		  from the current resource bundle.
	 */
	public CellGeometry(String fileName) {
		Properties p = new Properties();

		try {
			URL url = MagellanImages.getResource("etc/images/map/" + fileName);
			InputStream is = url.openStream();
			p.load(is);
			is.close();
		} catch(Exception e) {
			log.error("CellGeometry(): unable to load file images/map/" + fileName +
					  " from resource path. Using default values", e);
		}

		int xpoints[] = new int[6];
		xpoints[0] = Integer.parseInt(p.getProperty("x0", "32"));
		xpoints[1] = Integer.parseInt(p.getProperty("x1", "63"));
		xpoints[2] = Integer.parseInt(p.getProperty("x2", "63"));
		xpoints[3] = Integer.parseInt(p.getProperty("x3", "32"));
		xpoints[4] = Integer.parseInt(p.getProperty("x4", "0"));
		xpoints[5] = Integer.parseInt(p.getProperty("x5", "0"));

		int ypoints[] = new int[6];
		ypoints[0] = Integer.parseInt(p.getProperty("y0", "0"));
		ypoints[1] = Integer.parseInt(p.getProperty("y1", "16"));
		ypoints[2] = Integer.parseInt(p.getProperty("y2", "47"));
		ypoints[3] = Integer.parseInt(p.getProperty("y3", "63"));
		ypoints[4] = Integer.parseInt(p.getProperty("y4", "47"));
		ypoints[5] = Integer.parseInt(p.getProperty("y5", "16"));
		setGeometry(xpoints, ypoints);
		setImageOffset(Integer.parseInt(p.getProperty("imgOffsetx", "8")),
					   Integer.parseInt(p.getProperty("imgOffsety", "8")));
		setImageSize(Integer.parseInt(p.getProperty("imgSizex", "80")),
					 Integer.parseInt(p.getProperty("imgSizey", "80")));
	}

	/**
	 * Sets the hexagon coordinates of the cell geometry, the supplied arrays are expected to be of
	 * size 6 and the values are expected to confirm with the restrictions given in the
	 * constructor description.
	 */
	public void setGeometry(int xpoints[], int ypoints[]) {
		if((xpoints.length != 6) || (ypoints.length != 6)) {
			log.warn("CellGraphicsSet.setGeometry(): Invalid number of polygon points!");
		} else {
			cell = new Polygon(xpoints, ypoints, 6);

			// pavkovic 2003.06.19: the scaled polygon should have a resonable initial value
			scaledPoly = scalePolygon(scaleFactor);
			unscaledCellSize = cell.getBounds().getSize();
			unscaledCellSize.width++;
			unscaledCellSize.height++;
			scaledCellSize = new Dimension(unscaledCellSize);
			cellShiftXX = (float) (unscaledCellSize.width);
			cellShiftXY = (float) (unscaledCellSize.width / 2.0f);
			cellShiftYY = (float) (-1.0f * (ypoints[2] + 1.0f));
		}
	}

	/**
	 * Sets the offset of the opaque part of an image representing a cell in the graphics file in
	 * pixels. The x and y values are expected to be non-negative and smaller than the size of the
	 * cell hexagon.
	 *
	 * 
	 * 
	 */
	public void setImageOffset(int x, int y) {
/*
    if((x < 0) || (y < 0)) {
			log.warn("CellGraphicsSet.setImageOffsets(): Invalid offset value");

			return;
		}

		if((cell != null) && ((x > cell.getBounds().width) || (y > cell.getBounds().height))) {
			log.warn("CellGraphicsSet.setImageOffsets(): Invalid offset value");

			return;
		}
*/
		imgOffset = new Point(x, y);
		scaledImgOffset = new Point((int)(x * scaleFactor), (int)(y * scaleFactor));
	}

  /**
   * Only use for retrieving all Cell Geo data to consturct a related CellGeometry 
   */
  public Point getImageOffset() {
    return imgOffset;
  }
  
	/**
	 * Sets the size of graphics files used together with this cell geometry.
	 *
	 * 
	 * 
	 */
	public void setImageSize(int width, int height) {
		imgSize = new Dimension(width, height);
		scaledImgSize = new Dimension((int)Math.ceil(width * scaleFactor), (int)Math.ceil(height * scaleFactor));
	}

	/**
	 * Set a scale factor to be used for all calculations of cell positions and sizes.
	 *
	 * 
	 */
	public void setScaleFactor(float scaleFactor) {
		this.scaleFactor = scaleFactor;

		scaledPoly = scalePolygon(scaleFactor);
		scaledCellSize.width = (int)(unscaledCellSize.width * scaleFactor);
		scaledCellSize.height = (int)(unscaledCellSize.height * scaleFactor);

		scaledImgOffset.x = (int)(imgOffset.x * scaleFactor);
		scaledImgOffset.y = (int)(imgOffset.y * scaleFactor);

		scaledImgSize.width = (int)Math.ceil(imgSize.width * scaleFactor);
		scaledImgSize.height = (int)Math.ceil(imgSize.height * scaleFactor);
	}

	/**
	 * Returns the currently set scale factor.
	 *
	 * 
	 */
	public float getScaleFactor() {
		return scaleFactor;
	}

	/**
	 * Returns an unscaled Polygon object describing the cell hexagon.
	 *
	 * 
	 */
	public Polygon getPolygon() {
		return cell;
	}

	/**
	 * Returns a scaled Polygon object describing the cell hexagon.
	 *
	 * 
	 */
	public Polygon getScaledPolygon() {
		return scaledPoly;
	}

	/**
	 * Returns the scaled size of cells.
	 *
	 * 
	 */
	public Dimension getCellSize() {
		return scaledCellSize;
	}

	/**
	 * Returns the scaled pixel position where to draw an image to exactly match the position of
	 * the cell with the map coordinates mapX and mapY.
	 */
	public Point getImagePosition(int mapX, int mapY) {
		Point p = new Point(getCellPosition(mapX, mapY));
		p.translate(-scaledImgOffset.x, -scaledImgOffset.y);

		return p;
	}

	/**
	 * Returns the scaled size of images used together with this cell geometry.
	 *
	 * 
	 */
	public Dimension getImageSize() {
		return scaledImgSize;
	}

	/**
	 * Returns the scaled pixel position and size of an image to exactly match the position of the
	 * cell with the map coordinate mapX and mapY.
	 */
	public Rectangle getImageRect(int mapX, int mapY) {
		return new Rectangle(getImagePosition(mapX, mapY), scaledImgSize);
	}

	/**
	 * Returns the unscaled pixel position on the x-axis of a cell with the map coordinates mapX
	 * and mapY.
	 */
	public float getUnscaledCellPositionX(int mapX, int mapY) {
		return (mapX * cellShiftXX) + (mapY * cellShiftXY);
	}

	/**
	 * Returns the unscaled pixel position on the y-axis of a cell with the map coordinates mapX
	 * and mapY.
	 */
	public float getUnscaledCellPositionY(int mapX, int mapY) {
		return mapY * cellShiftYY;
	}

	/**
	 * Returns the scaled pixel position on the x-axis of a cell with the map coordinates mapX and
	 * mapY.
	 *
	 */
	public int getCellPositionX(int mapX, int mapY) {
		return (int)(getUnscaledCellPositionX(mapX, mapY) * scaleFactor);
	}

	/**
	 * Returns the scaled pixel position on the y-axis of a cell with the map coordinates mapX and
	 * mapY.
	 */
	public int getCellPositionY(int mapX, int mapY) {
		return (int)(getUnscaledCellPositionY(mapX, mapY) * scaleFactor);
	}

	/**
	 * Returns the scaled pixel position of a cell with the map coordinates mapX and mapY.
	 */
	public Point getCellPosition(int mapX, int mapY) {
		return new Point(getCellPositionX(mapX, mapY), getCellPositionY(mapX, mapY));
	}

	/**
	 * Returns the scaled pixel position and size of a cell with the map coordinates mapX and mapY.
	 */
	public Rectangle getCellRect(int mapX, int mapY) {
		return new Rectangle(getCellPosition(mapX, mapY), getCellSize());
	}

	/**
	 * Returns the map coordinate of a cell containing the pixels sx and sy. The z-value of the
	 * returned coordinate is z.
	 */
	public magellan.library.CoordinateID getCoordinate(int sx, int sy, int z) {
		int mx = 0;
		float mfy = (float) sy / (cellShiftYY * scaleFactor);
		int my = roundPosUp(mfy);
		mx = roundNegDown((((float) sx / scaleFactor) - (my * cellShiftXY)) / cellShiftXX);

		float capHeight = scaleFactor * cell.ypoints[1];
		int yPixelInCell = sy - getCellPositionY(mx, my);

		if(yPixelInCell < ((int) (capHeight) - 1)) {
			float halfCellWidth = scaledCellSize.width * scaleFactor * 0.5f;
			int xMaxPixelOffCenter = (int) ((halfCellWidth / capHeight) * yPixelInCell * 1.1f);

			int xPixelInCell = sx - getCellPositionX(mx, my);
			int xPixelOffCenter = xPixelInCell - (int) halfCellWidth;

			if(xPixelOffCenter < -xMaxPixelOffCenter) {
				my += 1;
				mx -= 1;
			} else if(xPixelOffCenter > xMaxPixelOffCenter) {
				my += 1;
			}
		}

		return new magellan.library.CoordinateID(mx, my, z);
	}

	/**
	 * Returns a new Polygon object that is a scaled instance of the unscaled polygon.
	 *
	 * 
	 *
	 * 
	 */
	private Polygon scalePolygon(float scaleFactor) {
		Polygon scaledPoly = new Polygon(cell.xpoints, cell.ypoints, cell.npoints);

		for(int i = 0; i < scaledPoly.npoints; i++) {
			scaledPoly.xpoints[i] = (int)(scaledPoly.xpoints[i] * scaleFactor);
			scaledPoly.ypoints[i] = (int)(scaledPoly.ypoints[i] * scaleFactor);
		}

		return scaledPoly;
	}

	/**
	 * Returns f rounded down the next integer.
	 *
	 * 
	 *
	 * 
	 */
	private static int roundNegDown(float f) {
		if(f >= 0) {
			return (int) f;
		} else {
			return ((int) f - 1);
		}
	}

	/**
	 * Rounds f up the the next integer.
	 *
	 * 
	 *
	 * 
	 */
	private static int roundPosUp(float f) {
		if(f <= 0) {
			return (int) f;
		} else {
			return ((int) f + 1);
		}
	}
}
