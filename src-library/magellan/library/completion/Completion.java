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

package magellan.library.completion;

/**
 * A class representing a possible completion of an incomplete order.
 */
public class Completion {

  /**
   * Field <code>DEFAULT_PRIORITY</code> The default priority value, <em>lower</em> priority comes
   * first
   */
  public static final int DEFAULT_PRIORITY = 9;

  private String name = null;
  private String value = null;
  private String postfix = null;
  private int priority = Completion.DEFAULT_PRIORITY;
  private int cursorOffset = 0;

  private boolean replaceLine;

  /**
   * Creates a new Completion object.
   * 
   * @param text The name that is to be displayed to the user <i>and</i> inserted as value
   */
  public Completion(String text) {
    this(text, text, "", Completion.DEFAULT_PRIORITY, 0);
  }

  /**
   * Creates a new Completion object.
   * 
   * @param text The name that is to be displayed to the user <i>and</i> inserted as value
   * @param prio The sorting priority, <em>lower</em> priority comes first
   */
  public Completion(String text, int prio) {
    this(text, text, "", prio, 0);
  }

  /**
   * Creates a new Completion object.
   * 
   * @param name The name that is to be displayed to the user
   * @param value The value that is inserted if this completion is chosen
   * @param postfix This is what should be inserted after the value but should not influence, for
   *          example, sorting
   */
  public Completion(String name, String value, String postfix) {
    this(name, value, postfix, Completion.DEFAULT_PRIORITY, 0);
  }

  /**
   * Creates a new Completion object.
   * 
   * @param text The name that is to be displayed to the user <i>and</i> inserted as value
   * @param postfix This is what should be inserted after the value but should not influence, for
   *          example, sorting
   */
  public Completion(String text, String postfix) {
    this(text, text, postfix, Completion.DEFAULT_PRIORITY, 0);
  }

  /**
   * Creates a new Completion object.
   * 
   * @param text The name that is to be displayed to the user <i>and</i> inserted as value
   * @param postfix This is what should be inserted after the value but should not influence, for
   *          example, sorting
   * @param prio The sorting priority, <em>lower</em> priority comes first
   */
  public Completion(String text, String postfix, int prio) {
    this(text, text, postfix, prio, 0);
  }

  /**
   * Creates a new Completion object.
   * 
   * @param name The name that is to be displayed to the user
   * @param value The value that is inserted if this completion is chosen
   * @param postfix This is what should be inserted after the value but should not influence, for
   *          example, sorting
   * @param prio The sorting priority, <em>lower</em> priority comes first
   */
  public Completion(String name, String value, String postfix, int prio) {
    this(name, value, postfix, prio, 0);
  }

  /**
   * Creates a new Completion object.
   * 
   * @param text The name that is to be displayed to the user <i>and</i> inserted as value
   * @param postfix This is what should be inserted after the value but should not influence, for
   *          example, sorting
   * @param prio The sorting priority, <em>lower</em> priority comes first
   * @param cursorOffset Indicates that the cursor is set back this amount of characters
   */
  public Completion(String text, String postfix, int prio, int cursorOffset) {
    this(text, text, postfix, prio, cursorOffset);
  }

  /**
   * Creates a new Completion object.
   * 
   * @param name The name that is to be displayed to the user
   * @param value The value that is inserted if this completion is chosen
   * @param postfix This is what should be inserted after the value but should not influence, for
   *          example, sorting
   * @param prio The sorting priority, <em>lower</em> priority comes first
   * @param cursorOffset Indicates that the cursor is set back this amount of characters
   */
  public Completion(String name, String value, String postfix, int prio, int cursorOffset) {
    this.name = name;
    this.value = value;
    this.postfix = postfix;
    priority = prio;
    this.cursorOffset = cursorOffset;
  }

  /**
   * Creates a new Completion object as shallow copy of another one.
   */
  public Completion(Completion c) {
    name = c.getName();
    value = c.getValue();
    postfix = c.getPostfix();
    priority = c.getPriority();
    cursorOffset = c.getCursorOffset();
  }

  /**
   * Returns the text that should be displayed to the user.
   */
  public String getName() {
    return name;
  }

  /**
   * Returns the value that is inserted if this completion is chosen including postfix.
   */
  public String getValue() {
    return value;
  }

  /**
   * Returns the sorting priority (<em>lower</em> priority comes first).
   */
  public int getPriority() {
    return priority;
  }

  /**
   * Returns the text that should be inserted after the value.
   */
  public String getPostfix() {
    return postfix;
  }

  /**
   * Returns the value the cursor should be set back after insertion.
   */
  public int getCursorOffset() {
    return cursorOffset;
  }

  /**
   * @see java.lang.Object#equals(java.lang.Object)
   */
  @Override
  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    if (obj instanceof Completion) {
      Completion c = (Completion) obj;
      return c.getName().equals(getName()) && c.getValue().equals(getValue())
          && c.getPostfix().equals(getPostfix());
    }
    return false;
  }

  @Override
  public int hashCode() {
    return getName().hashCode() + getValue().hashCode() + getPostfix().hashCode();
  }

  public boolean isReplaceLine() {
    return replaceLine;
  }

  public void setReplaceLine(boolean replaceLine) {
    this.replaceLine = replaceLine;
  }

  /**
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString() {
    return name;
  }

  public String replace(String line, String stub) {
    int stubBeg = line.length() - stub.length();

    String newLine = replaceLine ? "" : line.substring(0, stubBeg);
    String replacement = getValue() + getPostfix();
    newLine = newLine.concat(replacement);
    return newLine;
  }
}
