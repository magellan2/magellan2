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

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.Writer;

/**
 * A class for writing text in lines of a maximum specified length. If the characters written to
 * this writer exceed the specified line length, a platform dependent line break is inserted and
 * escaped by writing a backslash character in front of the line break.
 * <p>
 * Assuming that spaces after an escaped line break and the first non-whitespace character in the
 * new line are discarded, there is a special rule in order to prevent such whitespace losses. If a
 * line has to be wrapped and - without escaping - the last character on that line was a whitespace
 * character the escape character and line break are in the line is wrapped at the last
 * non-whitespace character pulling it into the next line and preserving the whitespace character
 * following it.
 * </p>
 * <p>
 * With a line width of 4 the String "lorem ipsum" is written to the underlying stream in a Unix
 * environment as "lor\\\\nem \\\nips\\\num", i.e.
 * </p>
 * lor\<br>
 * em \<br>
 * ips\<br>
 * um
 * <p>
 * With a line width of 6 the String "lorem ipsum" is written to the underlying stream in a Unix
 * environment as "lore\\\nm ips\\\num", i.e.
 * </p>
 * lore\<br>
 * m ips\<br>
 * um
 */
public class FixedWidthWriter extends Writer {
  /** The maximum line width this class supports */
  public static final int MAX_WIDTH = 1000;
  protected BufferedWriter out = null;
  protected int width = FixedWidthWriter.MAX_WIDTH;
  private StringBuffer lineBuffer = new StringBuffer();
  private int lastNonWhitespace = 0;
  private boolean forceUnixLineBreaks = false;

  /**
   * Creates a new FixedWidthWriter object with the underlying <kbd>Writer</kbd> object out stream and
   * a MAX_WIDTH line width.
   * 
   * @param out the stream this writer writes to through a <kbd>BufferedWriter</kbd>.
   */
  public FixedWidthWriter(Writer out) {
    this(new BufferedWriter(out));
  }

  /**
   * Creates a new FixedWidthWriter object with the underlying <kbd>BufferedWriter</kbd> object out
   * stream and a MAX_WIDTH line width.
   * 
   * @param out the stream this writer writes to.
   */
  public FixedWidthWriter(BufferedWriter out) {
    this.out = out;
  }

  /**
   * Creates a new FixedWidthWriter object with the underlying <kbd>Writer</kbd> object out stream and
   * the specified line width.
   * 
   * @param out the stream this writer writes to through a <kbd>BufferedWriter</kbd>.
   * @param width the maximum line width enforced by this writer.
   */
  public FixedWidthWriter(Writer out, int width) {
    this(new BufferedWriter(out), width);
  }

  /**
   * Creates a new FixedWidthWriter object with the underlying <kbd>Writer</kbd> object out stream and
   * the specified line width.
   * 
   * @param out the stream this writer writes to through a <kbd>BufferedWriter</kbd>.
   * @param width the maximum line width enforced by this writer.
   * @param bool forces unix line breaks.
   */
  public FixedWidthWriter(Writer out, int width, boolean bool) {
    this(new BufferedWriter(out), width);
    setForceUnixLineBreaks(bool);
  }

  /**
   * Creates a new FixedWidthWriter object with the underlying <kbd>BufferedWriter</kbd> object out
   * stream and the specified line width.
   * 
   * @param out the stream this writer writes to.
   * @param width the maximum line width enforced by this writer.
   */
  public FixedWidthWriter(BufferedWriter out, int width) {
    this.out = out;

    if (width < 3) {
      this.width = 3;
    } else if (width > FixedWidthWriter.MAX_WIDTH) {
      this.width = FixedWidthWriter.MAX_WIDTH;
    } else {
      this.width = width;
    }
  }

  /**
   * DOCUMENT-ME
   * 
   * @throws IOException DOCUMENT-ME
   */
  @Override
  public void close() throws IOException {
    if (lineBuffer.length() > 0) {
      out.write(lineBuffer.toString());
    }

    out.close();
  }

  /**
   * DOCUMENT-ME
   * 
   * @throws IOException DOCUMENT-ME
   */
  @Override
  public void flush() throws IOException {
    int curWidth = lineBuffer.length();

    if (curWidth > 0) {
      out.write(lineBuffer.toString());
      lineBuffer.delete(0, curWidth);
    }

    out.flush();
  }

  /**
   * DOCUMENT-ME
   * 
   * @throws IOException DOCUMENT-ME
   */
  @Override
  public void write(char cbuf[], int off, int len) throws IOException {
    for (int i = off; i < (off + len); i++) {
      write(cbuf[i]);
    }
  }

  /**
   * DOCUMENT-ME
   * 
   * @throws IOException DOCUMENT-ME
   */
  @Override
  public void write(int c) throws IOException {
    int curWidth = lineBuffer.length();

    if ((c == '\n') || (c == '\r')) {
      if (curWidth > 0) {
        out.write(lineBuffer.toString());
        lineBuffer.delete(0, curWidth);
      }

      out.write(c);
    } else {
      if (curWidth >= (width - 1)) {
        if (Character.isWhitespace((char) c)) {
          out.write(lineBuffer.substring(0, lastNonWhitespace));
          out.write('\\');
          lineBreak();
          lineBuffer.delete(0, lastNonWhitespace);
          lineBuffer.append((char) c);
          lastNonWhitespace = 0;
        } else {
          out.write(lineBuffer.toString());
          lineBuffer.delete(0, curWidth);
          out.write('\\');
          lineBreak();
          lineBuffer.append((char) c);
          lastNonWhitespace = 0;
        }
      } else {
        if (!Character.isWhitespace((char) c)) {
          lastNonWhitespace = curWidth;
        }

        lineBuffer.append((char) c);
      }
    }
  }

  /**
   * DOCUMENT-ME
   * 
   * @throws IOException DOCUMENT-ME
   */
  @Override
  public void write(String str) throws IOException {
    write(str.toCharArray());
  }

  /**
   * DOCUMENT-ME
   * 
   * @throws IOException DOCUMENT-ME
   */
  @Override
  public void write(String str, int off, int len) throws IOException {
    write(str.toCharArray(), off, len);
  }

  /**
   * DOCUMENT-ME
   * 
   * @throws IOException DOCUMENT-ME
   */
  public void newLine() throws IOException {
    int curWidth = lineBuffer.length();

    if (curWidth > 0) {
      out.write(lineBuffer.toString());
      lineBuffer.delete(0, curWidth);
    }

    lineBreak();
  }

  /**
   * Sets whether this writer always uses Unix style line breaks or system dependent line breaks.
   * This functionality has been added, since copying the content of a StringWriter to the clipboard
   * with Windows line breaks introduces additional empty lines.
   */
  public void setForceUnixLineBreaks(boolean bool) {
    forceUnixLineBreaks = bool;
  }

  private void lineBreak() throws IOException {
    if (forceUnixLineBreaks) {
      out.write('\n');
    } else {
      out.newLine();
    }
  }
}
