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

import java.util.Arrays;



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

  /** A token that is a quote which starts a string */
  public static final int TT_OPENING_QUOTE = 8;
  
  /** A token that is a quote which matches an opening quote */
  public static final int TT_CLOSING_QUOTE = 9;
  
  private String text = null; // the string representing this order token
	private int start = 0; // the start position of the token in the stream the token was read from, -1 indicates that the position is invalid
	private int end = 0; // the end position of the token in the stream the token was read from, -1 indicates that the position is invalid

	/** the type of the token */
	public int ttype = 0;
	private boolean followedBySpace = false;

	/**
	 * Creates a new <tt>OrderToken</tt> object representing the specified string, but with invalid
	 * start and end positions and undefined type.
	 *
	 * @param text the text this order token represents.
	 */
	public OrderToken(String text) {
		this(text, -1, -1, OrderToken.TT_UNDEF, false);
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
		this(text, start, end, OrderToken.TT_UNDEF, false);
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
	 * Should return <code>true</code> if the token is followed by a whitespace character
	 */
	public boolean followedBySpace() {
		return followedBySpace;
	}

  /**
   * Sets the followedBySpace() value.
   * @deprecated better make this immutable...
   */
  public void setFollowedBySpace(boolean b) {
    followedBySpace = b;
  }

	/**
	 * Returns the text. 
	 */
	public String getText() {
		return text;
	}

  static long count = 0;
  
  /**
   * Same as getText() but removes enclosing quotes.
   */
  public String getStrippedText(char [] delimiters) {
    if (ttype!=OrderToken.TT_STRING) {
      return getText();
    }
    
    int begin = 0, end = text.length();
    if (text.length()==0) {
      return text;
    }
    if (Arrays.binarySearch(delimiters, text.charAt(0))>0) {
      begin = 1;
    }
    if (text.length()>1 && Arrays.binarySearch(delimiters, text.charAt(0))>0) {
      end = text.length()-1;
    }
    return text.substring(begin, end);
  }

  /**
	 * Sets the token text. 
	 * @deprecated better make this immutable...
	 */
	public void setText(String text) {
		this.text = text;
	}

	/**
   * Returns the position of the first character of the token text in the order.
	 */
	public int getStart() {
		return start;
	}

	/**
   * Sets the position of the start of the token text in the order.
   * @deprecated better make this immutable...
	 */
	public void setStart(int start) {
		this.start = start;
	}

	/**
	 * Returns the position of the first character <em>after</em> the token text in the order (i.e., 
	 * <code>getEnd()-getStart()</code> is the length of the token).
	 */
	public int getEnd() {
		return end;
	}

	/**
	 * Sets the position of the end of the token text in the order.
   * @deprecated better make this immutable...
	 */
	public void setEnd(int end) {
		this.end = end;
	}

	/**
	 * Returns a string representation of this order token.
	 */
	@Override
  public String toString() {
		StringBuffer retVal = new StringBuffer(text);
		retVal.append(": ");

		switch(ttype) {
		case TT_UNDEF:
			retVal.append("Undefined");

			break;

		case TT_EOC:
			retVal.append("End of order");

			break;

		case TT_KEYWORD:
			retVal.append("Keyword");

			break;

		case TT_STRING:
			retVal.append("String");

			break;

		case TT_NUMBER:
			retVal.append("Number");

			break;

		case TT_ID:
			retVal.append("ID");

			break;

		case TT_COMMENT:
			retVal.append("Comment");

			break;

		case TT_PERSIST:
			retVal.append("Persistance marker");

			break;
			
		case TT_OPENING_QUOTE:
		  retVal.append("Opening quote");
		  break;

    case TT_CLOSING_QUOTE:
      retVal.append("Closing quote");
      break;
		}

		if(start != -1) {
			retVal.append("(").append(start).append(", ").append(end).append(")");
		}

		if(followedBySpace) {
			retVal.append(", followed by Space");
		} else {
			retVal.append(", not followed by Space");
		}

		return retVal.toString();
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
