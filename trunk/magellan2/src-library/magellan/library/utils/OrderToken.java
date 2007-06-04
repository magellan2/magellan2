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

package magellan.library.utils;


/**
 * A class representing a token of an Eressea order.
 */
public class OrderToken {
	/** Undefined token type */
	public static final int TT_UNDEF = 0;

	/** End-of-order token type */
	public static final int TT_EOC = 1;

	/** Well known keyword token type */
	public static final int TT_KEYWORD = 2;

	/** A quoted or non-quoted string */
	public static final int TT_STRING = 3;

	/** A number specifying some amount in contrast to an ID */
	public static final int TT_NUMBER = 4;

	/** An ID of a unit, building etc. */
	public static final int TT_ID = 5;

	/** A comment (starting with ; or //) */
	public static final int TT_COMMENT = 6;

	/** A token making the order persistent */
	public static final int TT_PERSIST = 7;
	private String text = null; // the string representing this order token
	private int start = 0; // the start position of the token in the stream the token was read from, -1 indicates that the position is invalid
	private int end = 0; // the end position of the token in the stream the token was read from, -1 indicates that the position is invalid

	/** DOCUMENT-ME */
	public int ttype = 0; // the type of the token
	private boolean followedBySpace = false;

	/**
	 * Creates a new <tt>OrderToken</tt> object representing the specified string, but with invalid
	 * start and end positions and undefined type.
	 *
	 * @param text the text this order token represents.
	 */
	public OrderToken(String text) {
		this(text, -1, -1, TT_UNDEF, false);
	}

	/**
	 * Creates a new <tt>OrderToken</tt> object representing the specified string and the specified
	 * start and end positions.
	 *
	 * @param text the text this order token represents.
	 * @param start the start position of the token in the underlying stream.
	 * @param end the end position of the token in the underlying stream.
	 */
	public OrderToken(String text, int start, int end) {
		this(text, start, end, TT_UNDEF, false);
	}

	/**
	 * Creates a new <tt>OrderToken</tt> object representing the specified string and the specified
	 * start and end positions.
	 *
	 * @param text the text this order token represents.
	 * @param start the start position of the token in the underlying stream.
	 * @param end the end position of the token in the underlying stream.
	 * @param type the type of the token
	 */
	public OrderToken(String text, int start, int end, int type) {
		this(text, start, end, type, false);
	}

	/**
	 * Creates a new <tt>OrderToken</tt> object representing the specified string with specific
	 * start and end positions and type.
	 *
	 * @param text the text this order token represents.
	 * @param start the start position of the token in the underlying stream.
	 * @param end the end position of the token in the underlying stream.
	 * @param type the type of the token, the value must equal one the TT_XXX constants.
	 * @param followedBySpace defines wether the token was followed by either '\r' '\n' '\t' or ' '
	 */
	public OrderToken(String text, int start, int end, int type, boolean followedBySpace) {
		this.text = text;
		this.start = start;
		this.end = end;
		this.ttype = type;
		this.followedBySpace = followedBySpace;
	}

	/**
	 * DOCUMENT-ME
	 *
	 * 
	 */
	public boolean followedBySpace() {
		return followedBySpace;
	}

	/**
	 * DOCUMENT-ME
	 *
	 * 
	 */
	public String getText() {
		return text;
	}

	/**
	 * DOCUMENT-ME
	 *
	 * 
	 */
	public void setText(String text) {
		this.text = text;
	}

	/**
	 * DOCUMENT-ME
	 *
	 * 
	 */
	public int getStart() {
		return start;
	}

	/**
	 * DOCUMENT-ME
	 *
	 * 
	 */
	public void setStart(int start) {
		this.start = start;
	}

	/**
	 * DOCUMENT-ME
	 *
	 * 
	 */
	public int getEnd() {
		return end;
	}

	/**
	 * DOCUMENT-ME
	 *
	 * 
	 */
	public void setEnd(int end) {
		this.end = end;
	}

	/**
	 * Returns a string representation of this order token.
	 *
	 * 
	 */
	public String toString() {
		String retVal = text + ": ";

		switch(ttype) {
		case TT_UNDEF:
			retVal += "Undefined";

			break;

		case TT_EOC:
			retVal += "End of order";

			break;

		case TT_KEYWORD:
			retVal += "Keyword";

			break;

		case TT_STRING:
			retVal += "String";

			break;

		case TT_NUMBER:
			retVal += "Number";

			break;

		case TT_ID:
			retVal += "ID";

			break;

		case TT_COMMENT:
			retVal += "Comment";

			break;

		case TT_PERSIST:
			retVal += "Persistance marker";

			break;
		}

		if(start != -1) {
			retVal += (" (" + start + ", " + end + ")");
		}

		if(followedBySpace) {
			retVal += ", followed by Space";
		} else {
			retVal += ", not followed by Space";
		}

		return retVal;
	}

	/**
	 * Compares the token and the specified keyword with respect to abbreviations as used by the
	 * eressea game server.
	 *
	 * 
	 *
	 * 
	 */
	public boolean equalsToken(String strKeyword) {
		if(text.length() == 0) {
			return false;
		}

		String strText = Umlaut.convertUmlauts(text.toLowerCase());
		String strTest = Umlaut.convertUmlauts(strKeyword.toLowerCase());

		if(followedBySpace) {
			return strTest.startsWith(strText);
		} else {
			return strTest.equalsIgnoreCase(strText);
		}
	}
	
	public boolean equals(OrderToken token){
		return this.equalsToken(token.text) || token.equalsToken(this.text);
	}
}
