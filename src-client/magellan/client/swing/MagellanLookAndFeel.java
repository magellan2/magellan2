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

package magellan.client.swing;

import java.awt.Color;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.swing.LookAndFeel;
import javax.swing.UIManager;
import javax.swing.UIManager.LookAndFeelInfo;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.plaf.ColorUIResource;
import javax.swing.plaf.metal.DefaultMetalTheme;
import javax.swing.plaf.metal.MetalLookAndFeel;
import javax.swing.plaf.metal.MetalTheme;

import magellan.client.desktop.DesktopEnvironment;
import magellan.client.utils.Colors;
import magellan.library.utils.logging.Logger;

/**
 * DOCUMENT ME!
 * 
 * @author Andreas Gampe
 * @author Ilja Pavkovic
 * @version 1.0
 */
public class MagellanLookAndFeel {
  private static final Logger log = Logger.getInstance(MagellanLookAndFeel.class);

  private static File magellanDirectory = null;

  /**
   * Read and apply background color.
   */
  public static void loadBackground(Properties settings) {
    String s = settings.getProperty("MagLookAndFeel.Background");

    if (s != null) {
      Color col = null;

      try {
        col = Colors.decode(s);
      } catch (RuntimeException exc) {
        // ignore bad input
      }

      if (col != null) {
        MagellanLookAndFeel.setBackground(col, settings);
      }
    }
  }

  /**
   * Changes the window background color (only for Metal Themes).
   */
  public static void setBackground(Color col, Properties settings) {
    if (col.equals(MetalLookAndFeel.getWindowBackground()))
      return;

    MetalTheme mt = new MagMetalTheme(col);
    MetalLookAndFeel.setCurrentTheme(mt);

    DesktopEnvironment.updateLaF();
    DesktopEnvironment.repaintAll();

    if (settings != null) {
      if (!col.equals(Color.white)) {
        settings.setProperty("MagLookAndFeel.Background", Colors.encode(col));
      } else {
        settings.remove("MagLookAndFeel.Background");
      }
    }
  }

  protected static class MagMetalTheme extends DefaultMetalTheme {
    protected ColorUIResource magDesktopColor;

    /**
     * Creates a new MagMetalTheme object.
     */
    public MagMetalTheme(Color col) {
      magDesktopColor = new ColorUIResource(col);
    }

    /**
     * DOCUMENT-ME
     */
    @Override
    public ColorUIResource getWindowBackground() {
      return magDesktopColor;
    }
  }

  /**
   * Function determines if name of current look and feel corresponds to given laf name.
   */
  public static boolean equals(String laf) {
    return UIManager.getLookAndFeel().getName().equals(laf);
  }

  /** a static variable to initialize look and feels only once */
  private static Map<String, LookAndFeel> lafCache;

  /**
   * Delivers a Map (String, MagLookAndFeelWrapper) of possibly usable look and feel
   * implementations.
   */
  public static Map<String, ? extends LookAndFeel> getLookAndFeels() {
    // if (MagellanLookAndFeel.lafCache == null )
    {
      Map<String, LookAndFeel> lookAndFeels =
          new Hashtable<String, LookAndFeel>();

      File plafFile = new File("etc/plaf.ini");

      // Trying to find plaf.ini
      if (!plafFile.exists()) {
        // use magDir
        plafFile = new File(MagellanLookAndFeel.magellanDirectory, "etc/plaf.ini");
        if (!plafFile.exists()) {
          // OK give up here
          MagellanLookAndFeel.log
              .error(
                  "MagellanLookAndfeel.getLookAndFeels(): Unable to read property file plaf.ini");
          plafFile = null;
        }
      }
      String systemLaf = UIManager.getSystemLookAndFeelClassName();
      String defaultLaf = UIManager.getCrossPlatformLookAndFeelClassName();

      LookAndFeelInfo[] installed = UIManager.getInstalledLookAndFeels();
      for (LookAndFeelInfo info : installed) {
        addLaf(lookAndFeels, info.getName(), info.getClassName());
      }

      if (plafFile != null) {
        try {
          FileInputStream ir = new FileInputStream(plafFile);

          Properties plaf_ini = new Properties();
          plaf_ini.load(ir);
          ir.close();

          String name = null;
          for (int i = 0; i < 10 || name != null; i++) {
            name = plaf_ini.getProperty("plaf.name." + i);
            String clazz = plaf_ini.getProperty("plaf.class." + i);

            addLaf(lookAndFeels, name, clazz);
          }
        } catch (IOException ioe) {
          MagellanLookAndFeel.log.error(
              "MagellanLookAndfeel.getLookAndFeels(): Unable to read property file plaf.ini", ioe);
        }
      }

      Collection<String> lafClasses = new HashSet<String>();
      for (LookAndFeel lafWrap : lookAndFeels.values()) {
        lafClasses.add(lafWrap.getClass().getName());
      }
      if (!lafClasses.contains(systemLaf)) {
        addLaf(lookAndFeels, "System", systemLaf);
      }
      if (!lafClasses.contains(defaultLaf)) {
        addLaf(lookAndFeels, "Default", defaultLaf);
      }

      synchronized (MagellanLookAndFeel.class) {
        MagellanLookAndFeel.lafCache = lookAndFeels;
      }
    }

    return Collections.unmodifiableMap(MagellanLookAndFeel.lafCache);
  }

  private static void addLaf(Map<String, LookAndFeel> lookAndFeels, String name,
      String clazz) {
    if ((name != null) && (clazz != null)) {
      try {
        Class<?> c = Class.forName(clazz);
        Constructor<?> constructor;
        constructor = c.getConstructor();

        try {
          if (!constructor.trySetAccessible())
            return;
        } catch (NoSuchMethodError e) {
          // must be pre java 9, this is fine
        }

        Object lafO = constructor.newInstance();

        if (lafO instanceof LookAndFeel) {
          LookAndFeel laf = (LookAndFeel) lafO;

          if (laf.isSupportedLookAndFeel()) {
            MagellanLookAndFeel.log.debug("MagellanLookAndfeel.getLookAndFeel(" + name
                + "," + clazz + "): " + laf.getID());
            lookAndFeels.put(name, laf);
          }
        } else {
          MagellanLookAndFeel.log.debug("MagellanLookAndfeel.getLookAndFeel(" + name + ","
              + clazz + "): illegal class.");
        }
      } catch (ClassNotFoundException | NoClassDefFoundError | InstantiationException
          | IllegalAccessException | NoSuchMethodException | SecurityException
          | IllegalArgumentException
          | InvocationTargetException e) {
        if (MagellanLookAndFeel.log.isDebugEnabled()) {
          MagellanLookAndFeel.log.debug("MagellanLookAndfeel.getLookAndFeel(" + name + ","
              + clazz + "): invalid class (" + e.toString() + ")");

        }
      }
    }
  }

  /**
   * Set global Look & Feel.
   *
   * @param laf LaF identifier
   * @return true if LaF could be changed
   */
  public static boolean setLookAndFeel(String laf) {
    if (laf == null)
      return false;

    LookAndFeel old = UIManager.getLookAndFeel();
    LookAndFeel olaf = MagellanLookAndFeel.getLookAndFeels().get(laf);

    if (olaf == null) {
      MagellanLookAndFeel.log.error("Could not switch look and feel to " + laf + " (" + olaf + ")");

      return false;
    }

    try {
      UIManager.setLookAndFeel(olaf);
    } catch (Exception e) {
      MagellanLookAndFeel.log.info("Could not switch look and feel to " + laf + " (" + olaf + ")");

      if (MagellanLookAndFeel.log.isDebugEnabled()) {
        MagellanLookAndFeel.log.debug(
            "Could not switch look and feel to " + laf + "(" + olaf + ")", e);
      }

      try {
        UIManager.setLookAndFeel(old);
      } catch (UnsupportedLookAndFeelException ue) {
        MagellanLookAndFeel.log.info("U oh, exception while switching back to old " + old.getID()
            + ")");
      }

      return false;
    }

    try {
      MagellanLookAndFeel.log.info("MagellanLookAndfeel.setLookAndFeel(" + laf + "): "
          + UIManager.getLookAndFeel().getClass() + ", " + UIManager.getLookAndFeel().getName()
          + ", " + UIManager.getLookAndFeel().getID());
    } catch (Exception e) {
      // ignore errors while logging
    }

    return true;
  }

  /**
   * Function delivers a sorted list of look and feel names
   */
  public static List<String> getLookAndFeelNames() {
    List<String> s = new ArrayList<String>();
    s.addAll(MagellanLookAndFeel.getLookAndFeels().keySet());
    Collections.sort(s);

    return s;
  }

  /**
   * Sets the value of magellanDirectory.
   *
   * @param magellanDirectory The value for magellanDirectory.
   */
  public static void setMagellanDirectory(File magellanDirectory) {
    MagellanLookAndFeel.magellanDirectory = magellanDirectory;
  }
}
