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

  static Properties settings;
  static FileNameGenerator gen;

  /**
   * DOCUMENT-ME
   */
  public static void init(Properties set) {
    FileNameGenerator.settings = set;
    new FileNameGenerator(FileNameGenerator.settings);
  }

  /**
   * DOCUMENT-ME
   */
  public static void quit() {
    if (FileNameGenerator.gen != null) {
      FileNameGenerator.gen.close();
    }
  }

  /**
   * DOCUMENT-ME
   */
  public static FileNameGenerator getInstance() {
    if (FileNameGenerator.gen == null) {
      new FileNameGenerator(FileNameGenerator.settings);
    }
    return FileNameGenerator.gen;
  }

  private FileNameGenerator(Properties settings) {
    ordersSaveFileNamePattern = settings.getProperty("FileNameGenerator.ordersSaveFileNamePattern");
    FileNameGenerator.gen = this;
  }

  protected void close() {

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

    res = FileNameGenerator.replaceAll(res, "{faction}", feed.getFaction());
    res = FileNameGenerator.replaceAll(res, "{factionnr}", feed.getFactionnr());
    int i = feed.getRound();
    if (i > -1) {
      res = FileNameGenerator.replaceAll(res, "{round}", Integer.toString(i));
    } else {
      res = FileNameGenerator.replaceAll(res, "{round}", null);
    }
    res = FileNameGenerator.replaceAll(res, "{group}", feed.getGroup());

    return res;
  }

  /**
   * @param originalString String to work with
   * @param searchString String to be replaced with "replaceString"
   * @param replaceString String to replace "searchString"
   * @return originalString with searchString replaced by replaceString we could use
   *         String.replaceAll for that...but, it's built in java 1.4 and just now we are compatible
   *         with 1.3. ... (Fiete 20061108)
   */
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
