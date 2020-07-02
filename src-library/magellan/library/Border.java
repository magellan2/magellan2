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

package magellan.library;

import java.util.List;

/**
 * Container class for a region border based on its representation in a cr version &gt; 45.
 */
public interface Border extends Identifiable {
  /** @deprecated should use EresseaConstants */
  @Deprecated
  StringID STRASSE = StringID.create("STRASSE");

  /**
   * The direction in which the border lies. The value must be one of the DIR_XXX constants in class
   * Direction.
   */
  public int getDirection();

  /**
   * The direction in which the border lies. The value must be one of the DIR_XXX constants in class
   * Direction.
   */
  public void setDirection(int direction);

  /** The type of this border. */
  public String getType();

  /** The type of this border. */
  public void setType(String type);

  /**
   * Indicates, to what extend this border type is completed. Values may range from 0 to 100, or -1
   * standing for an uninitialized/invalid value.
   */
  public int getBuildRatio();

  /**
   * Indicates, to what extend this border type is completed. Values may range from 0 to 100, or -1
   * standing for an uninitialized/invalid value.
   */
  public void setBuildRatio(int buildratio);

  /**
   * A list containing <tt>String</tt> objects, specifying effects on this border.
   */
  public List<String> getEffects();

  /**
   * A list containing <tt>String</tt> objects, specifying effects on this border.
   */
  public void setEffects(List<String> effects);

  // /**
  // * Return a string representation of this <tt>Border</tt> object.
  // *
  // * @return Border object as string.
  // */
  // public String toString();

  /**
   * Returns the id uniquely identifying this object.
   * 
   * @see magellan.library.Identifiable#getID()
   */
  public IntegerID getID();

  // /** Returns the localized direction name */
  // public String getDirectionName();
  //
  // /** Sets the localized direction name */
  // public void setDirectionName(String name);

}
