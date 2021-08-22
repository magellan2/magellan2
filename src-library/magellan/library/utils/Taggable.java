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

/*
 * Taggable.java
 *
 * Created on 6. Juni 2002, 18:44
 */
package magellan.library.utils;

import java.util.Map;

/**
 * A Taggable object works much like a Map&lt;String, String&gt;. Use it for for assigning additional tags
 * to report objects which are not official tags.
 * 
 * @author Andreas
 * @version 1.0
 */
public interface Taggable {

  /**
   * Returns <code>true</code> if there are any tags associated with this object.
   */
  public boolean hasTags();

  /**
   * Returns <code>true</code> if there is a tag with key <code>tag</code> associated with this
   * object.
   * 
   * @param tag
   */
  public boolean containsTag(String tag);

  /**
   * Add a tag with key <code>tag</code> and value <code>value</code> to this object.
   * 
   * @param tag
   * @param value
   */
  public String putTag(String tag, String value);

  /**
   * Return the value of the tag with key <code>tag</code> from this object.
   * 
   * @param tag
   * @return The value of the tag <code>tag</code> or <code>null</code> if no such tag exists.
   */
  public String getTag(String tag);

  /**
   * Removes the value for the key <code>tag</code> from this object.
   * 
   * @param tag
   */
  public String removeTag(String tag);

  /**
   * Remove all tags from this object.
   */
  public void deleteAllTags();

  /**
   * Returns a Map containing all tags and their values associated with this object.
   */
  public Map<String, String> getTagMap();
}
