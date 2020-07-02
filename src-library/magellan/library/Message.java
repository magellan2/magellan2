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

import java.util.Map;

import magellan.library.rules.MessageType;

/**
 * A class for representing a message.
 * <p>
 * The new format of messages in Eressea CR versions &ge; 41 made it necessary to reconsider this
 * class. Mainly, messages can now have an id and attributes.
 * </p>
 * <p>
 * Two special attributes are available directly via the corresping get/set methods.
 * </p>
 * <p>
 * First, this is the type attribte (tag ;type in the cr) denoting the type of the message. It is
 * transformed into a <tt>MessageType</tt> object.
 * </p>
 * <p>
 * Second, there is the text attribute (tag ;rendered in the cr). By design this attribute should
 * actually be created by rendering the message according to the message type's pattern and the
 * other attributes. This class does contain a <tt>render()</tt> method, still, it is too primitive
 * to yield acceptable results, so it is preferrable to take the rendered message text directly from
 * the cr.
 * </p>
 * <p>
 * Note, that for historic reasons, a <tt>Message</tt> object might have no type or attributes and
 * an invalid id of -1.
 * </p>
 */
public interface Message extends Identifiable {
  /**
   * An placeholder ID for messages without valid ID.
   */
  static final IntegerID ambiguousID = IntegerID.create(-1);

  /**
   * The attributes of this message. The keys are the keys of the attribute, the values object pairs
   * of the attributes' keys and values.
   */
  public Map<String, String> getAttributes();

  /**
   * The attributes of this message. The keys are the keys of the attribute, the values object pairs
   * of the attributes' keys and values.
   */
  public void setAttributes(Map<String, String> attributes);

  /**
   * Gets the rendered message text.
   * 
   * @return The message text
   */
  public String getText();

  /**
   * Sets the text of this message to <code>text</code>.
   * 
   * @param text The new text
   */
  public void setText(String text);

  /**
   * Returns the <code>MessageType</code> of this message.
   * 
   * @return The message type
   */
  public MessageType getMessageType();

  /**
   * Sets the <code>MessageType</code> of this message.
   * 
   * @param type The new message type
   */
  public void setType(MessageType type);

  /**
   * Renderes a message text from the given <code>pattern</code> and <code>attributes</code>.
   * <p>
   * Expects the tokens of the form {name1 name2} and replaces them by the attribute values for
   * name1, name2 etc. If these values are unit names or region coordinates, their names are taken
   * from the provided GameData.
   * </p>
   * <p>
   * WARNING! This does not work for the current cr format (41) which expects tokens of the form
   * $unit($unit).
   * </p>
   * 
   * @param data The game for replacing unit IDs and region coordinates
   * @param pattern The pattern to render
   * @param attributes A map of (String,Value)-pairs for replacing tokens in the pattern
   * @return The rendered text as string
   */
  // public static String render(GameData data, String pattern, Map attributes);

  /**
   * Renders the message and updates the message text.
   * 
   * @param data The GameData for replacing unit IDs and region coordinates
   */
  public void render(GameData data);

  /**
   * @return A hash code for this message
   */
  public int hashCode();

  /**
   * Indicates whether this Message object is equal to another object. Returns true only if o is not
   * null and an instance of class Message and o's id is equal to the id of this Message object.
   * 2002.02.21 pavkovic: Also the message text has to be the same for Messages with ambiguous
   * IntegerID(-1)
   */
  public boolean equals(Object o);

  /**
   * Transfers all available information from the current message to the new one.
   * 
   * @param curGD fully loaded game data
   * @param curMsg a fully initialized and valid message
   * @param newGD the game data to be updated
   * @param newMsg a message to be updated with the data from curMsg
   */
  // public static void merge(GameData curGD, Message curMsg, GameData newGD, Message newMsg);

  /**
   * DOCUMENT-ME
   */
  public String toString();

  /**
   * Returns an ID for this message. NOTE: This is not always unique, there are messages with
   * {@link #ambiguousID}.
   * 
   * @see magellan.library.impl.MagellanIdentifiableImpl#getID()
   */
  public IntegerID getID();

  /**
   * @return <code>true</code> if the message has been acknowledged (by the user)
   */
  public boolean isAcknowledged();

  /**
   * Sets the new acknowledged status.
   */
  public void setAcknowledged(boolean ack);
}
