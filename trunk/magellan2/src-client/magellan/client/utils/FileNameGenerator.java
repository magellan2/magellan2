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

import java.util.Properties;

import magellan.library.utils.FileNameGeneratorFeed;

/**
 * DOCUMENT ME!
 * 
 * @author Fiete
 * @version 1.0
 */
public class FileNameGenerator {

  String ordersSaveFileNamePattern = null;

  Properties settings;
  static FileNameGenerator gen;

  /**
   * Initializes the generator from settings
   * 
   * @throws NullPointerException if <code>settings==null</code>
   */
  public static void init(Properties settings) {
    gen = new FileNameGenerator(settings);
  }

  /**
   * Closes the generator.
   */
  public static void quit() {
  }

  /**
   * Returns the generator
   * 
   * @throws IllegalStateException if instance hasn't been called before.
   * @deprecated Who needs this?
   */
  @Deprecated
  public static FileNameGenerator getInstance() {
    if (FileNameGenerator.gen == null)
      throw new IllegalStateException("not initialized");
    return FileNameGenerator.gen;
  }

  private FileNameGenerator(Properties settings) {
    ordersSaveFileNamePattern = settings.getProperty("FileNameGenerator.ordersSaveFileNamePattern");
    this.settings = settings;
  }

  /**
   * @return the ordersSaveFileNamePattern
   */
  public String getOrdersSaveFileNamePattern() {
    return ordersSaveFileNamePattern;
  }

  /**
   * @param ordersSaveFileNamePattern the ordersSaveFileNamePattern to set
   */
  public void setOrdersSaveFileNamePattern(String ordersSaveFileNamePattern) {
    this.ordersSaveFileNamePattern = ordersSaveFileNamePattern;
  }

  /**
   * @param pattern String the user defined pattern for generating the FileName
   * @param feed FileNameGeneratorFeed with needed information
   * @return the new FileName or Null, if pattern is null
   */
  public static String getFileName(String pattern, FileNameGeneratorFeed feed) {
    if (pattern == null)
      return null;
    if (feed == null)
      return null;

    // Lets work in extra String
    String res = pattern.toString();

    res = res.replaceAll("\\{faction\\}", feed.getFaction());
    res = res.replaceAll("\\{factionnr\\}", feed.getFactionnr());
    int i = feed.getRound();
    if (i > -1) {
      res = res.replaceAll("\\{round\\}", String.valueOf(i));
      res = res.replaceAll("\\{round+1\\}", String.valueOf(i));
    } else {
      res = res.replaceAll("\\{round\\}", null);
      res = res.replaceAll("\\{round+1\\}", null);
    }
    res = res.replaceAll("\\{group\\}", feed.getGroup());

    return res;
  }

  /**
   * @param originalString String to work with
   * @param searchString String to be replaced with "replaceString"
   * @param replaceString String to replace "searchString"
   * @return originalString with searchString replaced by replaceString we could use
   *         String.replaceAll for that...but, it's built in java 1.4 and just now we are compatible
   *         with 1.3. ... (Fiete 20061108)
   * @deprecated We use Java 1.5 now
   */
  @Deprecated
  public static String replaceAll(String originalString, String searchString, String replaceString) {
    if (originalString == null)
      return null;
    if (searchString == null)
      return originalString;
    String myReplaceString = "";
    if (replaceString != null) {
      myReplaceString = replaceString;
    }

    String res = originalString.toString();
    int i = res.indexOf(searchString);

    while (i > -1) {

      res = res.substring(0, i) + myReplaceString + res.substring(i + searchString.length());
      i = res.indexOf(searchString);
    }

    return res;
  }

}
