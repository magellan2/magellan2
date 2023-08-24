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

package magellan.client.utils;

import java.awt.Frame;
import java.awt.Image;
import java.awt.MediaTracker;
import java.awt.Toolkit;
import java.awt.image.ImageObserver;
import java.awt.image.MemoryImageSource;
import java.awt.image.PixelGrabber;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import javax.swing.ImageIcon;

import magellan.client.event.EventDispatcher;
import magellan.library.event.GameDataEvent;
import magellan.library.event.GameDataListener;
import magellan.library.utils.Locales;
import magellan.library.utils.MagellanImages;
import magellan.library.utils.Umlaut;
import magellan.library.utils.logging.Logger;

/**
 * DOCUMENT-ME
 * 
 * @author $Author: $
 * @version $Revision: 328 $
 */
public class ImageFactory implements GameDataListener {
  private static final Logger log = Logger.getInstance(ImageFactory.class);
  private String gamename = "eressea";

  public ImageFactory(EventDispatcher dispatcher) {
    dispatcher.addGameDataListener(this);
  }

  /**
   * Called if Gamedata changes.
   */
  public void gameDataChanged(GameDataEvent e) {
    if (e.getGameData() != null) {
      gamename = e.getGameData().getGameName().toLowerCase();

      reset();
    }
  }

  /**
   * Clears the cache.
   */
  public void reset() {
    images.clear();
  }

  private Map<String, ImageIcon> images = new HashMap<String, ImageIcon>();

  /**
   * Loads the given image. First it tests to load
   */
  public ImageIcon loadImage(String imageName) {
    return loadImage(imageName, true);
  }

  /**
   * Loads the given image. First it tests to load
   */
  public ImageIcon loadImage(String imageName, boolean errorIfNotFound) {
    // look into cache
    if (images.containsKey(imageName))
      return images.get(imageName);

    ImageIcon img = doLoadImage(imageName);
    String fName = Umlaut.normalize(imageName).toLowerCase();
    // we do not replace spaces by strings
    // fName = fName.trim().replaceAll(" ","");
    if (img == null) {
      // ImageFactory.log.debug("Loading image " + fName);
      img = doLoadImage(gamename + "/" + fName);
    }
    if (img == null) {
      img = doLoadImage(fName);
    }
    // store into cache
    if (img != null) {
      images.put(imageName, img);
    }

    if (img == null && errorIfNotFound) {
      ImageFactory.log.errorOnce("ImageFactory.loadImage(" + imageName + "): not found");
    }

    return img;
  }

  /**
   * basic class to load an Icon
   * 
   * @param imageName
   */
  private ImageIcon doLoadImage(String imageName) {
    ImageIcon icon = null;
    // if we have an OrderLocale != German try to
    // load localized icon
    if (Locales.getGUILocale() != null && !Locales.getGUILocale().equals(Locale.GERMAN)) {
      icon = doLoadImageLocales(imageName + "_" + Locales.getGUILocale().toString());
    }
    if (icon == null) {
      icon = doLoadImageLocales(imageName);
    }

    return icon;
  }

  /**
   * basic class to load an icon, imageName may be extended by locale_string
   * 
   * @param imageName
   */
  private ImageIcon doLoadImageLocales(String imageName) {
    // try to find a .png
    ImageIcon icon = MagellanImages.getImageIcon(imageName + ".png");
    ImageIcon alphaIcon = null;

    // try to find a .jpg
    if (icon == null) {
      icon = MagellanImages.getImageIcon(imageName + ".jpg");
    }

    // try to find a .gif
    if (icon == null) {
      icon = MagellanImages.getImageIcon(imageName + ".gif");

      if (icon != null) {
        String iName = imageName;
        if (iName.endsWith("_summer")) {
          iName = iName.substring(0, iName.length() - 7);
        }
        if (iName.endsWith("_winter")) {
          iName = iName.substring(0, iName.length() - 7);
        }
        if (iName.endsWith("_autumn")) {
          iName = iName.substring(0, iName.length() - 7);
        }
        if (iName.endsWith("_spring")) {
          iName = iName.substring(0, iName.length() - 7);
        }
        alphaIcon = MagellanImages.getImageIcon(iName + "-alpha.gif");
      }
    }

    // try to find without ending
    if (icon == null) {
      icon = MagellanImages.getImageIcon(imageName);
    }

    if (icon == null)
      return null;

    if (alphaIcon == null)
      return icon;
    else
      return new ImageIcon(merge(icon.getImage(), alphaIcon.getImage()));
  }

  /**
   * Combine two images of equal size to one, where the resulting image contains the RGB information
   * of the first image directly and the RGB information of the second one as alpha channel
   * information.
   * 
   * @param rgb the image to take rgb information from.
   * @param alpha the image to take the alpha channel information from.
   * @return the composite image, or null if rgb or alpha were null or they were not of equal size.
   */
  public Image merge(Image rgb, Image alpha) {
    if ((rgb == null) || (alpha == null))
      return null;

    // pavkovic 2002.06.05: change way to wait for image data. This should dramatically
    // reduce the number of calls to getWidth and getHeight
    waitForImage(rgb);

    int w = rgb.getWidth(null);
    int h = rgb.getHeight(null);

    // FIXME(stm) This needs very much memory (temporarily) if it's called too often and not re-used
    // -- find out why
    int pixelsRGB[] = new int[w * h];
    int pixelsAlpha[] = new int[pixelsRGB.length];
    PixelGrabber pgRGB = new PixelGrabber(rgb, 0, 0, w, h, pixelsRGB, 0, w);
    PixelGrabber pgAlpha = new PixelGrabber(alpha, 0, 0, w, h, pixelsAlpha, 0, w);

    try {
      pgRGB.grabPixels();
      pgAlpha.grabPixels();
    } catch (InterruptedException e) {
      ImageFactory.log.warn("interrupted waiting for pixels!");

      return null;
    }

    if (((pgRGB.getStatus() & ImageObserver.ABORT) != 0)
        || ((pgAlpha.getStatus() & ImageObserver.ABORT) != 0)) {
      ImageFactory.log.warn("image fetch aborted or errored");

      return null;
    }

    for (int i = 0; i < pixelsRGB.length; i++) {
      pixelsRGB[i] &= (((pixelsAlpha[i] & 0x000000FF) << 24) | 0x00FFFFFF);
    }

    return Toolkit.getDefaultToolkit().createImage(new MemoryImageSource(w, h, pixelsRGB, 0, w));
  }

  /**
   * Wait until an image is loaded.
   */
  public void waitForImage(Image img) {
    MediaTracker mt = new MediaTracker(new Frame());

    try {
      mt.addImage(img, 0);
      mt.waitForAll();
    } catch (InterruptedException e) {
      // return
    }
  }

  /**
   * DOCUMENT-ME
   */
  public ImageIcon loadImageIcon(String imageName) {
    return loadImage("etc/images/icons/" + imageName);
  }

  /**
   * Load an image by file name. This procedure tries different file formats in the following order:
   * .png, if not found then .gif. If a .gif file is available, an optional -alpha.gif file is used
   * for alpha-channel information. If no such -alpha.gif file can be found, the optional alpha
   * information in the .gif file is used. If no such file seems to exist, null is returned. All
   * file names are prepended with the path 'images/map/'+gamename enforcing that the files are
   * located in such a sub-directory of the resources root directory /res. If no such image is
   * found, the fallback to 'images/map/' is used to load the file
   * 
   * @param imageName a file name without extension.
   * @return the image loaded from fileName, or null if not file could be found.
   */
  public Image loadMapImage(String imageName) {
    return loadMapImage(imageName, true);
  }

  /**
   * Load an image by file name. This procedure tries different file formats in the following order:
   * .png, if not found then .gif. If a .gif file is available, an optional -alpha.gif file is used
   * for alpha-channel information. If no such -alpha.gif file can be found, the optional alpha
   * information in the .gif file is used. If no such file seems to exist, null is returned. All
   * file names are prepended with the path 'images/map/'+gamename enforcing that the files are
   * located in such a sub-directory of the resources root directory /res. If no such image is
   * found, the fallback to 'images/map/' is used to load the file
   * 
   * @param imageName a file name without extension.
   * @return the image loaded from fileName, or null if not file could be found.
   */
  public Image loadMapImage(String imageName, boolean errorIfNotFound) {
    ImageIcon icon = loadImage("etc/images/map/" + imageName, errorIfNotFound);
    if (icon != null)
      return icon.getImage();
    return null;
  }

  /**
   * Checks, if icon exists
   * 
   * @param imageName the name of the icon to check
   * @return true, if a loadImage order would be succesfull...
   */
  public boolean existImageIcon(String imageName) {
    ImageIcon img = loadImage("etc/images/icons/" + imageName, false);

    return (img == null) ? false : true;
  }

  /**
   * Checks, if icon exeeds given max sizes
   * 
   * @param imageName the name of the icon to check
   * @return true, if a loadImage order would be succesfull...
   */
  public boolean imageIconSizeCheck(String imageName, int maxHeight, int maxWidth) {
    ImageIcon icon = loadImage("etc/images/icons/" + imageName);
    if (icon != null) {
      Image img = icon.getImage();
      int w = img.getWidth(null);
      int h = img.getHeight(null);
      if (w > maxWidth || h > maxHeight)
        return false;
      else
        return true;
    } else
      return false;
  }
}
