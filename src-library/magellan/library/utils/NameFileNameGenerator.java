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

package magellan.library.utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;

import magellan.library.utils.logging.Logger;

/**
 * A NameGenerator that takes names from a file.
 * 
 * @author Andreas
 * @version 1.0
 */
public class NameFileNameGenerator implements NameGenerator {
  boolean available = false;
  List<String> names;
  Properties settings;
  private File settingsDir;
  static NameFileNameGenerator gen;

  /**
   * Initializes the generator from settings
   * 
   * @throws NullPointerException if <code>settings==null</code>
   */
  public static void init(Properties settings, File dir) {
    gen = new NameFileNameGenerator(settings, dir);
  }

  /**
   * Closes the generator.
   */
  public static void quit() {
    if (NameFileNameGenerator.gen != null) {
      NameFileNameGenerator.gen.close();
    }
  }

  /**
   * Returns the generator
   * 
   * @throws IllegalStateException if instance hasn't been called before.
   */
  public static NameFileNameGenerator getInstance() {
    if (NameFileNameGenerator.gen == null)
      throw new IllegalStateException("not initialized");

    return NameFileNameGenerator.gen;
  }

  private NameFileNameGenerator(Properties settings, File dir) {
    this.settings = settings;
    settingsDir = dir;
    if (getNameFile() != null) {
      load(getNameFile().getAbsolutePath());
    }
    available = PropertiesHelper.getBoolean(settings, "NameGenerator.active", true);
  }

  /**
   * Loads names from a file.
   * 
   * @param fileName If this is a file with at least one name, names are replaced with the contents. Otherwise, names
   *          are cleared.
   */
  public void load(String fileName) {
    if (names == null) {
      names = new LinkedList<String>();
    } else {
      names.clear();
    }

    if (!Utils.isEmpty(fileName)) {
      File file = new File(fileName);

      // we read the file only if it exists.
      if (file.exists() && file.canRead()) {
        try {
          BufferedReader in = new BufferedReader(new FileReader(file));
          String name = null;

          while ((name = in.readLine()) != null) {
            name = name.trim();
            if (name.startsWith("#")) {
              // lines starting with # are comments,
              // unless they start with two ##, in which case the first # is deleted
              if (name.startsWith("##")) {
                name = name.substring(1);
              } else {
                name = "";
              }
            }
            if (!name.isEmpty()) {
              names.add(name.trim());
            }
          }

          in.close();
        } catch (IOException ioe) {
          System.out.println(ioe);
        }
      }
    }

    if (names.size() == 0) {
      names = null;
    }
  }

  /**
   * Writes the remaining names to {@link #getNameFile()}.
   */
  protected void close() {
    File file = getNameFile();

    if (file != null) {
      try {
        if (names != null) {
          PrintWriter out = new PrintWriter(new FileWriter(file));
          for (String name : names) {
            out.println(name);
          }
          out.close();
        } else {
          file.delete();
        }
      } catch (IOException exc) {
        Logger.getInstance(getClass()).warn(exc);
      }
    }
  }

  /**
   * Returns the file to hold the local copy of the remaining names or <code>null</code> if this is not defined.
   */
  protected File getNameFile() {
    if (settingsDir != null)
      return new File(settingsDir, "names.txt");
    return null;
  }

  /**
   * @see magellan.library.utils.NameGenerator#isActive()
   */
  @Override
  public boolean isActive() {
    return available;
  }

  /**
   * @see magellan.library.utils.NameGenerator#isAvailable()
   */
  @Override
  public boolean isAvailable() {
    return (available && (names != null));
  }

  /**
   * @see magellan.library.utils.NameGenerator#setEnabled(boolean)
   */
  @Override
  public void setEnabled(boolean available) {
    this.available = available;
  }

  /**
   * @see magellan.library.utils.NameGenerator#getName()
   */
  @Override
  public String getName() {
    if (names != null) {
      String name = names.remove(0);

      if (names.size() == 0) {
        names = null;
      }

      return name;
    }

    return null;
  }

  /**
   * @see magellan.library.utils.NameGenerator#getNamesCount()
   */
  @Override
  public int getNamesCount() {
    if (names == null)
      return 0;
    return names.size();
  }

  /**
   * Returns the file where the remaining names are stored locally, <code>null</code> if undefined.
   */
  public String getCache() {
    File file = getNameFile();
    if (file != null)
      return file.getAbsolutePath();
    return null;
  }
}
