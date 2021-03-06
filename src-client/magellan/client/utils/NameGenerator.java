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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;

import javax.swing.JFrame;
import javax.swing.JOptionPane;

import magellan.library.utils.Resources;
import magellan.library.utils.Utils;

/**
 * DOCUMENT ME!
 * 
 * @author Andreas
 * @version 1.0
 */
public class NameGenerator {
  boolean available = false;
  List<String> names;
  Properties settings;
  static NameGenerator gen;

  /**
   * Initializes the generator from settings
   * 
   * @throws NullPointerException if <code>settings==null</code>
   */
  public static void init(Properties settings) {
    gen = new NameGenerator(settings);
  }

  /**
   * Closes the generator.
   */
  public static void quit() {
    if (NameGenerator.gen != null) {
      NameGenerator.gen.close();
    }
  }

  /**
   * Returns the generator
   * 
   * @throws IllegalStateException if instance hasn't been called before.
   */
  public static NameGenerator getInstance() {
    if (NameGenerator.gen == null)
      throw new IllegalStateException("not initialized");

    return NameGenerator.gen;
  }

  private NameGenerator(Properties settings) {
    load(settings.getProperty("NameGenerator.Source"));
    available = settings.getProperty("NameGenerator.active", "false").equals("true");

    this.settings = settings;
  }

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
          String s = null;

          while ((s = in.readLine()) != null) {
            names.add(s.trim());
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

  protected void close() {
    String file = settings.getProperty("NameGenerator.Source");

    if (!Utils.isEmpty(file)) {
      try {
        File f = new File(file);

        if (f.exists()) {
          f.delete();
        }

        if (names != null) {
          PrintWriter out = new PrintWriter(new FileWriter(file));
          Iterator<String> it = names.iterator();

          while (it.hasNext()) {
            out.println(it.next());
          }

          out.close();
        }
      } catch (IOException exc) {
      }
    }
  }

  /**
   * DOCUMENT-ME
   */
  public boolean isActive() {
    return available;
  }

  /**
   * Returns true, if there is a name generator configured and names are available.
   */
  public boolean isAvailable() {
    return (available && (names != null));
  }

  /**
   * Enabled the name generator.
   */
  public void setEnabled(boolean available) {
    this.available = available;
  }

  /**
   * DOCUMENT-ME
   */
  public String getName() {
    if (names != null) {
      String name = names.remove(0);

      if (names.size() == 0) {
        names = null;
        showMessage();
      }

      return name;
    }

    return null;
  }

  protected void showMessage() {
    JOptionPane.showMessageDialog(new JFrame(), Resources.get("util.namegenerator.nomorenames"));
  }
}
