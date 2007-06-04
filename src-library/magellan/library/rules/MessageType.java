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

import magellan.library.GameData;
import magellan.library.ID;
import magellan.library.impl.MagellanIdentifiableImpl;

/**
 * DOCUMENT-ME
 *
 * @author $Author: $
 * @version $Revision: 203 $
 */
public class MessageType extends MagellanIdentifiableImpl {
	private String pattern = null;
	private String section = null;

	/**
	 * Creates a new MessageType object.
	 *
	 * 
	 */
	public MessageType(ID id) {
		this(id, null);
	}

	/**
	 * Creates a new MessageType object.
	 *
	 * 
	 * 
	 */
	public MessageType(ID id, String pattern) {
		super(id);
		setPattern(pattern);
	}

	/**
	 * DOCUMENT-ME
	 *
	 * 
	 */
	public void setPattern(String pattern) {
		this.pattern = pattern;
	}

	/**
	 * DOCUMENT-ME
	 *
	 * 
	 */
	public String getPattern() {
		return pattern;
	}

	/**
	 * Sets the name of the category of messages this message type belongs to.
	 *
	 * 
	 */
	public void setSection(String section) {
		this.section = section;
	}

	/**
	 * Returns the name of the category of messages this message type belongs to.
	 *
	 * 
	 */
	public String getSection() {
		return this.section;
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
		if(curMsgType.getPattern() != null) {
			newMsgType.setPattern(curMsgType.getPattern());
		}

		if(curMsgType.getSection() != null) {
			newMsgType.setSection(curMsgType.getSection());
		}
	}
}
