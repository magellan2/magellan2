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

import java.io.IOException;
import java.io.Reader;


/**
 * Splits a string into <tt>OrderToken</tt> objects. The tokenizer recognizes quoted strings and
 * comments and marks the generated tokens as such. Note that the tokenizer is not intended to
 * detect keywords, identifiers and numbers, since such a classification can only be made with
 * syntactical context. For proper handling of escaped new lines this class wraps the underlying
 * stream in a MergeLineReader. This implies that the start and end attributes of the produced
 * <tt>OrderToken</tt> objects reflect the actual position and length of the token on the
 * underlying stream, including escape line breaks.
 */
public class OrderTokenizer {
	private MergeLineReader in = null;
	private boolean isFirstToken = true;

	/**
	 * Creates a new <tt>OrderTokenizer</tt> object which will perform its read operations on the
	 * specified stream.
	 *
	 * @param r the stream this <tt>OrderTokenizer</tt> reads from.
	 */
	public OrderTokenizer(Reader r) {
		in = new MergeLineReader(r);
		isFirstToken = true;
	}

	/**
	 * Reads the next chunk of text from the underlying stream. The token types are only recognized
	 * and set partially by this tokenizer. Apart from comments and quoted strings this has to be
	 * done in a semantical context.
	 *
	 * 
	 */
	public OrderToken getNextToken() {
		OrderToken retVal = new OrderToken("", -1, -1, OrderToken.TT_EOC);
		int c = 0;

		try {
			eatWhiteSpace();

			if((c = in.read()) != -1) {
				if(isFirstToken && (c == '@')) {
					retVal = new OrderToken("@", in.getPos() - 1, in.getPos(),
											OrderToken.TT_PERSIST, false);
				} else if(c == '"' || c == '\'') {
					retVal = readQuote(c);
				} else if(c == ';') {
					retVal = readSCComment();
				} else if(c == '/') {
					retVal = readSSComment();
				} else if((c == '\r') || (c == '\n')) {
					retVal = new OrderToken("", -1, -1, OrderToken.TT_EOC, false);
				} else {
					in.unread(c);
					retVal = readWord();
				}
			}
		} catch(IOException e) {
		}

		isFirstToken = false;

		return retVal;
	}

	/**
	 * Reads from the underlying stream up to the next quotation mark or line break.
	 *
	 * @return a <tt>OrderToken</tt> object of type TT_STRING containing the quoted string.
	 *
	 * @throws IOException DOCUMENT-ME
	 */
	private OrderToken readQuote(int quote) throws IOException {
		StringBuffer sb = new StringBuffer(""+(char)quote);
		int c = 0;
		int start = in.getPos() - 1;

		while((c = in.read()) != -1) {
			if(c == quote) {
				sb.append((char) c);

				break;
			} else if((c == '\r') || (c == '\n')) {
				break;
			} else {
				sb.append((char) c);
			}
		}

		int end = in.getPos();
		OrderToken retVal;

		if(c != quote) {
			end--;
			retVal = new OrderToken(sb.toString(), start, end, OrderToken.TT_STRING, true);
		} else {
			c = in.read();

			if((c == ' ') || (c == '\t')) {
				retVal = new OrderToken(sb.toString(), start, end, OrderToken.TT_STRING, true);
			} else {
				retVal = new OrderToken(sb.toString(), start, end, OrderToken.TT_STRING, false);
			}

			if(c != -1) {
				in.unread(c);
			}
		}

		return retVal;
	}

	/**
	 * Reads a one line comment beginning with a semicolon up to the next line break.
	 *
	 * @return a <tt>OrderToken</tt> object of type TT_COMMENT containing the comment.
	 *
	 * @throws IOException DOCUMENT-ME
	 */
	private OrderToken readSCComment() throws IOException {
		StringBuffer sb = new StringBuffer(";");
		int c = 0;
		int start = in.getPos() - 1;

		while((c = in.read()) != -1) {
			if((c == '\r') || (c == '\n')) {
				break;
			} else {
				sb.append((char) c);
			}
		}

		return new OrderToken(sb.toString(), start, in.getPos() - 1, OrderToken.TT_COMMENT, true);
	}

	/**
	 * Reads a one line comment beginning with a double slash up to the next line break.
	 *
	 * @return a <tt>OrderToken</tt> object of type TT_COMMENT containing the comment.
	 *
	 * @throws IOException DOCUMENT-ME
	 */
	private OrderToken readSSComment() throws IOException {
		StringBuffer sb = new StringBuffer("/");
		OrderToken retVal = new OrderToken("", -1, -1, OrderToken.TT_EOC);
		int start = in.getPos() - 1;
		int c = in.read();

		if(c == '/') {
			sb.append((char) c);

			while((c = in.read()) != -1) {
				if((c == '\r') || (c == '\n')) {
					break;
				} else {
					sb.append((char) c);
				}
			}

			retVal = new OrderToken(sb.toString(), start, in.getPos() - 1, OrderToken.TT_COMMENT,
									true);
		} else {
			in.unread(c);
			in.unread('/');
			retVal = readWord();
		}

		return retVal;
	}

	/**
	 * Reads one word from the underlying stream.
	 *
	 * @return a <tt>OrderToken</tt> object of type TT_UNDEF containing the word.
	 *
	 * @throws IOException DOCUMENT-ME
	 */
	private OrderToken readWord() throws IOException {
		StringBuffer sb = new StringBuffer();
		OrderToken retVal = new OrderToken("", -1, -1, OrderToken.TT_EOC);
		int c = 0;
		int start = in.getPos();

		while((c = in.read()) != -1) {
		  // TODO (stm) check for  '\'' here, too?
			if((c == '\r') || (c == '\n') || (c == ' ') || (c == '\t') || (c == '"') || (c == ';') ||
				   (c == '/')) {
				in.unread(c);

				break;
			} else {
				sb.append((char) c);
			}
		}

		int end = in.getPos();

		if(c == -1) {
			end--;
			retVal = new OrderToken(sb.toString(), start, end, OrderToken.TT_UNDEF, false);
		} else if((c == '\r') || (c == '\n') || (c == '\t') || (c == ' ')) {
			retVal = new OrderToken(sb.toString(), start, end, OrderToken.TT_UNDEF, true);
		} else {
			c = in.read();

			if((c == '\r') || (c == '\n') || (c == '\t') || (c == ' ')) {
				retVal = new OrderToken(sb.toString(), start, end, OrderToken.TT_UNDEF, true);
			} else {
				retVal = new OrderToken(sb.toString(), start, end, OrderToken.TT_UNDEF, false);
			}

			in.unread(c);
		}

		return retVal;
	}

	/**
	 * Consumes the stream up the next non-whitespace character.
	 *
	 * @throws IOException DOCUMENT-ME
	 */
	private void eatWhiteSpace() throws IOException {
		int c = 0;

		while((c = in.read()) != -1) {
			if((c != ' ') && (c != '\t')) {
        in.unread(c);
				break;
			}
		}
	}
}
