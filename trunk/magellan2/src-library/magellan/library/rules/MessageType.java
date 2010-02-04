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

package magellan.library.rules;

import java.util.Locale;

import magellan.library.GameData;
import magellan.library.IntegerID;
import magellan.library.Localized;
import magellan.library.impl.MagellanIdentifiableImpl;

/**
 * DOCUMENT-ME
 * 
 * @author $Author: $
 * @version $Revision: 203 $
 */
public class MessageType extends MagellanIdentifiableImpl implements Localized {
  private String pattern = null;
  private String section = null;
  private Locale locale = null;
  private GameData data = null;

  /**
   * Creates a new MessageType object.
   */
  public MessageType(IntegerID id) {
    this(id, null);
  }

  /**
   * Creates a new MessageType object.
   */
  public MessageType(IntegerID id, String pattern) {
    super(id);
    setPattern(pattern);
  }

  /**
   * DOCUMENT-ME
   */
  public void setPattern(String pattern) {
    this.pattern = pattern;
  }

  /**
   * DOCUMENT-ME
   */
  public String getPattern() {
    return pattern;
  }

  /**
   * Sets the name of the category of messages this message type belongs to.
   */
  public void setSection(String section) {
    this.section = section;
  }

  /**
   * Returns the name of the category of messages this message type belongs to.
   */
  public String getSection() {
    return section;
  }

  /**
   * Transfers all available information from the current message type to the new one.
   * 
   * @param curGD fully loaded game data
   * @param curMsgType a fully initialized and valid message type
   * @param newGD the game data to be updated
   * @param newMsgType an uninitialized message type to be updated with the date from curMsgType
   */
  public static void merge(GameData curGD, MessageType curMsgType, GameData newGD,
      MessageType newMsgType) {

    if (curMsgType.getPattern() != null) {
      // if we don't have a message type for a new locale we stay on the old one
      // however when merging other reports later we are not aware that some of the message types
      // doesn't match the local of the report
      // probably we should store the locale of a messagetype in the message type when diffent to
      // GameData
      // But as we overwrite message types with new information each time the report is newer or
      // same turn and same locale the wrong localized messagetypes should disappear.
      if ((newGD.getLocale().equals(curGD.getLocale())) || (newMsgType.getPattern() == null)) {
        newMsgType.setPattern(curMsgType.getPattern());
      }
    }

    if (curMsgType.getSection() != null) {
      newMsgType.setSection(curMsgType.getSection());
    }

    // set the GameData were this message type belongs to
    // this is required to render messages of this type
    newMsgType.setGameData(newGD);
  }

  /**
   * Probably we should set the GameData together with the pattern as we only need it if a pattern
   * is assigned but in this case it is most probably required!
   */

  /**
   * Set the GameData were this message type belongs to this is required to render messages of this
   * type
   * 
   * @param data The GameData
   */
  public void setGameData(GameData data) {
    this.data = data;
  }

  /**
   * Returns the GameData were this msgtype belongs to. this is required to render messages of this
   * type
   * 
   * @return the GameData
   */
  public GameData getGameData() {
    return data;
  }

  /**
   * Get the Locale of the MessageType.
   * 
   * @see magellan.library.Localized#getLocale()
   * @return the locale of the MessageType.
   */
  public Locale getLocale() {
    return locale;
  }

  /**
   * Sets the locale and invalidates the pattern of the MessageType if required.
   * 
   * @see magellan.library.Localized#setLocale(java.util.Locale)
   */
  public void setLocale(Locale locale) {
    if ((locale != null) && (!locale.equals(this.locale))) {
      this.locale = locale;
      setPattern(null);
    }
  }

  @Override
  public String toString() {
    return "MessageType:{id:" + getID() + ",section:" + section + ",pattern:" + pattern + "}";
  }

  /**
   * Returns the id uniquely identifying this object.
   */
  @Override
  public IntegerID getID() {
    return (IntegerID) super.getID();
  }
}
