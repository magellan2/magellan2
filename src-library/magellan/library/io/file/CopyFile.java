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

package magellan.library.io.file;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Handles backup generation for files.
 */
public class CopyFile {
  /**
   * Disable instantiation of class
   */
  private CopyFile() {
    throw new IllegalStateException("should never be called");
  }

  /**
   * Copies the given source file to a temporary file.
   * 
   * @param source source file.
   * @return The temporary file.
   * @throws IOException if an I/O error occurs
   */
  public static synchronized File copy(File source) throws IOException {
    return CopyFile.copy(source, CopyFile.createTempFile());
  }

  /**
   * Copies the given source file to the given destination.
   * 
   * @param source source file.
   * @param target destination target file.
   * @return target
   * @throws IOException if an I/O error occurs
   */
  public static synchronized File copy(File source, File target) throws IOException {
    CopyFile.copyStreams(new FileInputStream(source), new FileOutputStream(target));
    target.setLastModified(source.lastModified());

    return target;
  }

  /**
   * Identical to <code>CopyFile.createTempFile("magellan", ".tmp.cr")</code>.
   * 
   * @return The temp file
   * @throws IOException if an I/O error occurs
   */
  public static synchronized File createCrTempFile() throws IOException {
    return CopyFile.createTempFile("magellan", ".tmp.cr");
  }

  /**
   * Identical to <code>createTempFile("magellan", null)</code>.
   * 
   * @return The temp file
   * @throws IOException if an I/O error occurs
   */
  public static synchronized File createTempFile() throws IOException {
    return CopyFile.createTempFile("magellan", null);
  }

  /**
   * Identical to <code>createTempFile(prefix, null)</code>.
   * 
   * @return The temp file
   * @throws IOException if an I/O error occurs
   */
  public static synchronized File createTempFile(String prefix) throws IOException {
    return CopyFile.createTempFile(prefix, null);
  }

  /**
   * Identical to {@link File#createTempFile(String, String)}, but additionally sets
   * {@link File#deleteOnExit()}.
   * 
   * @return The temp file
   * @throws IOException if an I/O error occurs
   */
  private static File createTempFile(String prefix, String suffix) throws IOException {
    File tempfile = File.createTempFile(prefix, suffix);
    tempfile.deleteOnExit();

    return tempfile;
  }

  private static final int BUFF_SIZE = 100000;

  /**
   * Reads everything from source and writes it to target and attempts to close them after reading.
   * 
   * @param source
   * @param target
   * @throws IOException if an I/O error occurs
   */
  public static synchronized void copyStreams(InputStream source, OutputStream target)
      throws IOException {
    CopyFile.copyStreams(source, target, true);
  }

  /**
   * Reads everything from source and writes it to target.
   * 
   * @param source
   * @param target
   * @param closeStreams if <code>true</code>, will attempt to close the streams after reading
   * @throws IOException if an I/O error occurs
   */
  public static synchronized void copyStreams(InputStream source, OutputStream target,
      boolean closeStreams) throws IOException {
    InputStream in = null;
    OutputStream out = null;
    byte buffer[] = new byte[CopyFile.BUFF_SIZE];

    try {
      // encapsulate into BufferedInputStream if necessary
      if (source instanceof BufferedInputStream) {
        in = source;
      } else {
        in = new BufferedInputStream(source);
      }

      // encapsulate into BufferedInputStream if necessary
      if (target instanceof BufferedOutputStream) {
        out = target;
      } else {
        out = new BufferedOutputStream(target);
      }

      int count = 0;

      do {
        out.write(buffer, 0, count);
        count = in.read(buffer, 0, buffer.length);
      } while (count != -1);

    } finally {
      if (closeStreams) {
        if (in != null) {
          try {
            in.close();
          } catch (IOException e) {
            // can't do much about it
          }
        }

        if (out != null) {
          try {
            out.close();
          } catch (IOException e) {
            // can't do much about it
          }
        }
      } else {
        if (out != null) {
          out.flush();
        }
      }
    }
  }
}
