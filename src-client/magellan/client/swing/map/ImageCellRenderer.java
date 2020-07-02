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
import java.awt.Image;
import java.awt.MediaTracker;
import java.util.HashMap;
import java.util.Map;

import magellan.client.MagellanContext;
import magellan.library.utils.Umlaut;

/**
 * A template for a renderer that uses images for rendering objects. This class takes care of
 * dynamic loading and proper scaling of the images. All images are loaded from the images/map/
 * sub-directory of the current resource bundle.
 */
public abstract class ImageCellRenderer extends HexCellRenderer {
  private Map<String, ImageContainer> images = new HashMap<String, ImageContainer>();

  private int id = Integer.MAX_VALUE / 2;
  private static MediaTracker tracker = null;

  /**
   * Creates a new ImageCellRenderer.
   * 
   * @param geo The geometry object to use for rendering.
   * @param context Context to get settings from.
   */
  public ImageCellRenderer(CellGeometry geo, MagellanContext context) {
    super(geo, context);
  }

  /**
   * Scale all images this renderer uses to a certain scale factor.
   * 
   * @param scaleFactor the factor to scale the images with (a scaleFactor of 1.0 would scale all
   *          images to their original size). Must be &gt; 0.
   * @throws IllegalArgumentException if scaleFactor &le; 0.
   */
  @Override
  public void scale(float scaleFactor) {
    if (scaleFactor <= 0)
      throw new IllegalArgumentException("factor < 0: " + scaleFactor);
    super.scale(scaleFactor);

    for (ImageContainer c : images.values()) {
      if (c != null) {
        // try to identify a memory leak
        c.scaled = null;
        c.scaled = scale(c.unscaled);
      }
    }
  }

  /**
   * Return a scaled version of the supplied using the current scale factor. If there is no media
   * tracker, this function enforces synchronous scaling.
   * 
   * @param img the img to scale
   * @return a scaled instance of img or null, if img is null.
   */
  public Image scale(Image img) {
    if (img != null) {
      Dimension size = cellGeo.getImageSize();
      Image scaled = img.getScaledInstance(size.width, size.height, Image.SCALE_SMOOTH);

      if (ImageCellRenderer.tracker != null) {
        // TODO random ID??
        ImageCellRenderer.tracker.addImage(scaled, --id);
      } else {
        context.getImageFactory().waitForImage(img);
      }

      return scaled;
    } else
      return null;
  }

  /**
   * Make the renderer reload all of its cached images.
   */
  public void reloadImages() {
    images.clear();
  }

  /**
   * Set the cell geometry this renderer is based on and make it reload all of its cached images.
   */
  @Override
  public void setCellGeometry(CellGeometry geo) {
    super.setCellGeometry(geo);
    reloadImages();
  }

  /**
   * Set a media tracker that is used to track all images that are scaled. If no media tracker is
   * present scaling is synchronous.
   */
  public static void setTracker(MediaTracker t) {
    ImageCellRenderer.tracker = t;
  }

  protected Image loadFile(String fileName) {
    return loadFile(fileName, true);
  }

  protected Image loadFile(String fileName, boolean errorIfNotFound) {
    return context.getImageFactory().loadMapImage(fileName, errorIfNotFound);
  }

  /**
   * Returns an image that is associated with name. If name has never been supplied to this function
   * before, it attempts to load an image with a file name of name. If no such file exists, there
   * will be no further attempts to load the file when this function is called with the same value
   * for name.
   * 
   * @param name a name identifying the image to get. This name is also used as a file name without
   *          extension to load the image from a file.
   * @return the image associated with name or null, if there is no such image and it cannot be
   *         loaded.
   */
  protected Image getImage(String name) {
    return getImage(name, true);
  }

  /**
   * Returns an image that is associated with name. If name has never been supplied to this function
   * before, it attempts to load an image with a file name of name. If no such file exists, there
   * will be no further attempts to load the file when this function is called with the same value
   * for name.
   * 
   * @param name a name identifying the image to get. This name is also used as a file name without
   *          extension to load the image from a file.
   * @return the image associated with name or null, if there is no such image and it cannot be
   *         loaded.
   */
  protected Image getImage(String name, boolean errorIfNotFound) {
    Image img = null;

    if (name != null) {
      String normName = Umlaut.convertUmlauts(name);

      if (images.containsKey(normName)) {
        ImageContainer c = images.get(normName);

        if (c != null) {
          img = c.scaled;
        }
      } else {
        img = loadFile(normName, errorIfNotFound);

        if (img != null) {
          // add loaded image to map
          images.put(normName, new ImageContainer(img, scale(img)));
        } else {
          // add null to the map so we do not attempt to load the file again
          images.put(normName, null);
        }
      }
    }

    return img;
  }

  protected static class ImageContainer {
    /** DOCUMENT-ME */
    public Image unscaled = null;

    /** DOCUMENT-ME */
    public Image scaled = null;

    /**
     * Creates a new ImageContainer object.
     */
    public ImageContainer(Image unscaled, Image scaled) {
      this.unscaled = unscaled;
      this.scaled = scaled;
    }
  }
}
