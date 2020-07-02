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
import java.util.Arrays;

import magellan.library.gamebinding.EresseaConstants;
import magellan.library.utils.logging.Logger;

/**
 * Splits a string into <tt>OrderToken</tt> objects. The tokenizer recognizes quoted strings and
 * comments and marks the generated tokens as such. Note that the tokenizer is not intended to
 * detect keywords, identifiers and numbers, since such a classification can only be made with
 * syntactical context. For proper handling of escaped new lines this class wraps the underlying
 * stream in a MergeLineReader. This implies that the start and end attributes of the produced
 * <tt>OrderToken</tt> objects reflect the actual position and length of the token on the underlying
 * stream, including escape line breaks.
 */
public class OrderTokenizer {
  // private static final Logger log = Logger.getInstance(OrderTokenizer.class);

  private MergeLineReader in = null;
  private OrderToken quotedString;
  private OrderToken closingQuote;
  private OrderToken openingQuote;
  private boolean eos;
  private char quotes[] = new char[] { '"', '\'' };

  enum TYPE {
    NONE, EXCLAM, AT, MEAT
  }

  private TYPE prefix;

  /**
   * Creates a new <tt>OrderTokenizer</tt> object which will perform its read operations on the
   * specified stream.
   *
   * @param r the stream this <tt>OrderTokenizer</tt> reads from.
   */
  public OrderTokenizer(Reader r) {
    in = new MergeLineReader(r);
    prefix = TYPE.NONE;
  }

  /**
   * Reads the next chunk of text from the underlying stream. The token types are only recognized and
   * set partially by this tokenizer. Apart from comments and quoted strings this has to be done in a
   * semantical context.
   */
  public OrderToken getNextToken() {
    OrderToken retVal = null;
    if (quotedString != null) {
      retVal = quotedString;
      quotedString = null;
      return retVal;
    } else if (closingQuote != null) {
      retVal = closingQuote;
      closingQuote = null;
      return retVal;
    }

    int c = 0;

    try {
      eatWhiteSpace();

      if ((c = in.read()) != -1) {
        if (prefix == TYPE.NONE && (c == '!')) {
          retVal = new OrderToken("!", in.getPos() - 1, in.getPos(), OrderToken.TT_EXCLAM, false);
          prefix = TYPE.EXCLAM;
        } else if ((prefix == TYPE.EXCLAM || prefix == TYPE.NONE) && (c == '@')) {
          retVal = new OrderToken("@", in.getPos() - 1, in.getPos(), OrderToken.TT_PERSIST, false);
          prefix = TYPE.AT;
        } else if (isQuote(c)) {
          retVal = readQuote(c);
          prefix = TYPE.MEAT;
        } else if (c == ';') { // FIXME doesn't use EresseaConstants.OC_COMMENT
          retVal = readSCComment();
          prefix = TYPE.MEAT;
        } else if ((c == '\r') || (c == '\n')) {
          retVal = new OrderToken(OrderToken.TT_EOC);
          prefix = TYPE.MEAT;
        } else {
          in.unread(c);
          retVal = readWord();
          if (prefix == TYPE.NONE && retVal.getText().equals(EresseaConstants.O_PCOMMENT)) {
            retVal = readSSComment(retVal);
          }
          prefix = TYPE.MEAT;
        }
      } else {
        eos = true;
      }
    } catch (IOException e) {
      // FIXME should throw this
      Logger.getInstance(this.getClass()).error("Unknonw I/O error", e);
    }

    if (retVal == null) {
      retVal = new OrderToken(OrderToken.TT_EOC);
    }
    return retVal;
  }

  /**
   * Reads from the underlying stream up to the next quotation mark or line break.
   *
   * @return a <tt>OrderToken</tt> object of type TT_STRING containing the quoted string.
   * @throws IOException DOCUMENT-ME
   */
  protected OrderToken readQuote(int quote) throws IOException {
    // setting followedBySpace to true here, is somewhat of a hack. It ensures that the
    // OrderCompleter will not insert completions at this point.
    openingQuote =
        new OrderToken("" + (char) quote, in.getPos() - 1, in.getPos(),
            OrderToken.TT_OPENING_QUOTE, false);

    int c = 0;
    int previousC = -1;
    int start = in.getPos();
    StringBuffer sb = new StringBuffer();

    while ((c = in.read()) != -1) {
      if (c == '\\' && previousC != '\\') {

      } else if (c == quote && previousC != '\\') {
        break;
      } else if ((c == '\r') || (c == '\n')) {
        break;
      } else {
        sb.append((char) c);
      }
      previousC = c;
    }

    int end = in.getPos();

    if (c != quote) {
      end--;
      if (sb.length() > 0) {
        if (c == '\r' || c == '\n') {
          quotedString = new OrderToken(sb.toString(), start, end, OrderToken.TT_STRING, true);
        } else {
          quotedString = new OrderToken(sb.toString(), start, end, OrderToken.TT_STRING, false);
        }
      }
    } else {
      if (c == -1) {
        eos = true;
      } else {
        c = in.read();
      }
      end--;
      quotedString = new OrderToken(sb.toString(), start, end, OrderToken.TT_STRING, false);

      if (isSpace(c)) {
        closingQuote =
            new OrderToken("" + (char) quote, end, end + 1, OrderToken.TT_CLOSING_QUOTE, true);
      } else {
        closingQuote =
            new OrderToken("" + (char) quote, end, end + 1, OrderToken.TT_CLOSING_QUOTE, false);
      }

      if (c != -1) {
        in.unread(c);
      }
    }

    return openingQuote;
  }

  /**
   * Reads a one line comment beginning with a semicolon up to the next line break.
   *
   * @return a <tt>OrderToken</tt> object of type TT_COMMENT containing the comment.
   * @throws IOException DOCUMENT-ME
   */
  protected OrderToken readSCComment() throws IOException {
    StringBuffer sb = new StringBuffer(EresseaConstants.O_COMMENT);
    int c = 0;
    int start = in.getPos() - 1;

    while ((c = in.read()) != -1) {
      if ((c == '\r') || (c == '\n')) {
        break;
      } else {
        sb.append((char) c);
      }
    }
    eos = c == -1;

    return new OrderToken(sb.toString(), start, in.getPos() - 1, OrderToken.TT_COMMENT, true);
  }

  /**
   * Reads a one line comment beginning with a double slash up to the next line break.
   *
   * @param retVal2
   * @return a <tt>OrderToken</tt> object of type TT_COMMENT containing the comment.
   * @throws IOException DOCUMENT-ME
   */
  protected OrderToken readSSComment(OrderToken retVal2) throws IOException {
    StringBuffer sb = new StringBuffer(EresseaConstants.O_PCOMMENT);
    int start = in.getPos() - (retVal2.followedBySpace() ? 2 : 3);
    int c;

    if (eos) {
      c = -1;
    } else {
      c = in.read();
      if (!eos && c != -1 && !isSpace(c)) {
        if (!eos && c != -1) {
          in.unread(c);
        }
        return retVal2;
      }
    }

    while (c != -1) {
      if ((c == '\r') || (c == '\n')) {
        break;
      } else {
        sb.append((char) c);
      }
      c = in.read();
    }

    int end = in.getPos();
    if (c == -1) {
      eos = true;
      end--;
    }
    // if (!retVal2.followedBySpace()) {
    // end--;
    // }
    OrderToken retVal = new OrderToken(sb.toString(), start, end, OrderToken.TT_COMMENT, true);

    return retVal;
  }

  /**
   * Reads one word from the underlying stream.
   *
   * @return a <tt>OrderToken</tt> object of type TT_UNDEF containing the word.
   * @throws IOException DOCUMENT-ME
   */
  protected OrderToken readWord() throws IOException {
    StringBuffer sb = new StringBuffer();
    OrderToken retVal = null;
    int c = 0;
    int start = in.getPos();

    while ((c = in.read()) != -1) {
      if (isSpace(c) || isQuote(c) || (c == ';')) {
        in.unread(c);
        break;
      } else {
        sb.append((char) c);
      }
    }

    int end = in.getPos();

    if (c == -1) {
      eos = true;
      retVal = new OrderToken(sb.toString(), start, end - 1, OrderToken.TT_UNDEF, false);
    } else if (isSpace(c)) {
      retVal = new OrderToken(sb.toString(), start, end, OrderToken.TT_UNDEF, true);
    } else {
      c = in.read();

      if (isSpace(c) || isQuote(c) || (c == ';')) {
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
  protected void eatWhiteSpace() throws IOException {
    int c = 0;

    while ((c = in.read()) != -1) {
      if ((c != ' ') && (c != '\t')) {
        if (c == '\\') {
          in.unread(c);
        } else {
          in.unread(c);
        }
        break;
      }
    }
    eos = c == -1;
  }

  protected boolean isQuote(int c) {
    for (char quote : quotes) {
      if (quote == c)
        return true;
    }
    return false;
  }

  public void setQuotes(char[] quotes) {
    this.quotes = Arrays.copyOf(quotes, quotes.length);
  }

  protected boolean isSpace(int c) {
    return (c == '\r') || (c == '\n') || (c == '\t') || (c == ' ');
  }

}
