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

package magellan.library.impl;

import java.util.ArrayList;
import java.util.List;

import magellan.library.Border;
import magellan.library.IntegerID;
import magellan.library.utils.Direction;

/**
 * Container class for a region border based on its representation in a cr version &gt; 45.
 */
public class MagellanBorderImpl extends MagellanIdentifiableImpl implements Border {
  /**
   * The direction in which the border lies. The value must be one of the DIR_XXX constants in class
   * Direction.
   */
  private int direction;

  /** The type of this border. */
  private String type;

  /**
   * Indicates, to what extend this border type is completed. Values may range from 0 to 100, or -1
   * standing for an uninitialized/invalid value.
   */
  private int buildRatio;

  /** A list containing <tt>String</tt> objects, specifying effects on this border. */
  private List<String> effects;

  /**
   * Create a new <tt>Border</tt> object with the specified id.
   * 
   * @param id the id of the border
   */
  public MagellanBorderImpl(IntegerID id) {
    this(id, Direction.DIR_INVALID, null, -1);
  }

  /**
   * Create a new <tt>Border</tt> object initialized to the specified values.
   * 
   * @param id the id of the border
   * @param direction the direction of the border
   * @param type the type of the border
   * @param buildRatio indicates, to what extend this border type is completed (e.g. street)
   */
  public MagellanBorderImpl(IntegerID id, int direction, String type, int buildRatio) {
    super(id);
    this.direction = direction;
    this.type = type;
    this.buildRatio = buildRatio;
  }

  /**
   * Return a string representation of this <tt>Border</tt> object.
   * 
   * @return Border object as string.
   */
  @Override
  public String toString() {
    // FIXME should localize, but don't have rules here!
    StringBuilder name = new StringBuilder(type + ": ");
    switch (getDirection()) {
    case 0:
      name.append("NW");
      break;
    case 1:
      name.append("NE");
      break;
    case 2:
      name.append("E");
      break;
    case 3:
      name.append("SE");
      break;
    case 4:
      name.append("SW");
      break;
    case 5:
      name.append("W");
      break;

    default:
      name.append(getDirection());
    }

    if (getBuildRatio() >= 0 && getBuildRatio() != 100) {
      name.append(" (").append(getBuildRatio()).append("%)");
    }
    return name.toString();
  }

  public int getBuildRatio() {
    return buildRatio;
  }

  public int getDirection() {
    return direction;
  }

  public List<String> getEffects() {
    return effects;
  }

  public String getType() {
    return type;
  }

  public void setDirection(int direction) {
    this.direction = direction;
  }

  public void setType(String type) {
    // fix for reports turn 551 (english) and 552 (all)
    if (type.equalsIgnoreCase("road")) {
      this.type = "Straﬂe";
    } else {
      this.type = type;
    }
  }

  public void setBuildRatio(int buildratio) {
    buildRatio = buildratio;
  }

  public void setEffects(List<String> effects) {
    this.effects = effects;
  }

  /**
   * @see magellan.library.Addeable#addAttribute(java.lang.String, java.lang.String)
   */
  public void addAttribute(String key, String value) {
    throw new RuntimeException("this method is not implemented");
  }

  /**
   * @see magellan.library.Addeable#containsAttribute(java.lang.String)
   */
  public boolean containsAttribute(String key) {
    return false;
  }

  /**
   * @see magellan.library.Addeable#getAttribute(java.lang.String)
   */
  public String getAttribute(String key) {
    throw new RuntimeException("this method is not implemented");
  }

  /**
   * @see magellan.library.Addeable#getAttributeKeys()
   */
  public List<String> getAttributeKeys() {
    return new ArrayList<String>();
  }

  /**
   * @see magellan.library.Addeable#getAttributeSize()
   */
  public int getAttributeSize() {
    return 0;
  }

  /**
   * @see magellan.library.Identifiable#getID()
   */
  @Override
  public IntegerID getID() {
    return (IntegerID) super.getID();
  }
}
