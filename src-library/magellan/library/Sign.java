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

/**
 * class for a region sign based on our own representation in the CR.
 * 
 * @author Fiete
 * @see magellan.library.Region#getSigns()
 */
public class Sign {

  /** The type of this border. */
  private String text = null;

  /**
   * Create a new <kbd>Sign</kbd> object
   */
  public Sign() {
  }

  /**
   * Create a new <kbd>Sign</kbd> object initialized to the specified values.
   * 
   * @param text the text of the sign object
   */
  public Sign(String text) {
    this.text = text;
  }

  /**
   * Return a string representation of this <kbd>Sign</kbd> object.
   * 
   * @return Sign object as string.
   */
  @Override
  public String toString() {
    if (text != null)
      return text;
    return "undef";
  }

  /**
   * @return the text
   */
  public String getText() {
    return text;
  }

  /**
   * @param text the text to set
   */
  public void setText(String text) {
    this.text = text;
  }
}
