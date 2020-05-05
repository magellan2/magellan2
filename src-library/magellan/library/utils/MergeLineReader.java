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
import java.io.PushbackReader;
import java.io.Reader;

/**
 * A stream filtering line breaks escaped by a backslash character from the underlying stream.
 */
public class MergeLineReader extends PushbackReader {
  private int pos = 0;

  /**
   * Creates a new MergeLineReader object.
   */
  public MergeLineReader(Reader in) {
    super(in, 2);
  }

  /**
   * DOCUMENT-ME
   * 
   * @throws IOException DOCUMENT-ME
   */
  @Override
  public int read() throws IOException {
    int c = super.read();
    pos++;

    while (c == '\\') {
      c = super.read();
      pos++;

      if (c == '\n') {
        // consume
      } else if (c == '\r') {
        c = super.read();
        pos++;

        if (c == '\n') {
          // consume
        } else {
          unread(c);
          unread('\r');
          c = '\\';

          break;
        }
      } else {
        if (c != -1) {
          unread(c);
        }
        c = '\\';

        break;
      }

      c = super.read();
      pos++;
    }

    return c;
  }

  /**
   * DOCUMENT-ME
   * 
   * @throws IOException DOCUMENT-ME
   */
  @Override
  public int read(char cbuf[], int off, int len) throws IOException {
    int retVal = 0;
    int c = 0;

    for (int i = off; (i < (off + len)) && (i < cbuf.length); i++) {
      if ((c = read()) == -1) {
        if (i == off) {
          retVal = -1;
        }

        break;
      } else {
        cbuf[i] = (char) c;
        retVal++;
      }
    }

    return retVal;
  }

  /**
   * DOCUMENT-ME
   * 
   * @throws IOException DOCUMENT-ME
   */
  @Override
  public void reset() throws IOException {
    super.reset();
    pos = 0;
  }

  /**
   * DOCUMENT-ME
   * 
   * @throws IOException DOCUMENT-ME
   */
  @Override
  public void unread(char cbuf[]) throws IOException {
    super.unread(cbuf);
    pos -= cbuf.length;
  }

  /**
   * DOCUMENT-ME
   * 
   * @throws IOException DOCUMENT-ME
   */
  @Override
  public void unread(char cbuf[], int off, int len) throws IOException {
    super.unread(cbuf, off, len);
    pos -= len;
  }

  /**
   * @see java.io.PushbackReader#unread(int)
   */
  @Override
  public void unread(int c) throws IOException {
    super.unread(c);
    pos--;
  }

  /**
   * Returns the position of the next character to be read, is initially 0. Escaped line breaks are
   * counted as well. E.g. after having read the string "A\\\nB" getPos() would return 4.
   */
  public int getPos() {
    return pos;
  }
}
