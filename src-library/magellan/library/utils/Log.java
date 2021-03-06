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
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintStream;

import magellan.library.io.file.FileBackup;
import magellan.library.io.file.FileType;

/**
 * @author $Author: $
 * @version $Revision: 171 $
 */
public class Log {
  private File baseDir = null;

  private String encoding;

  private File logFile;

  /**
   * Creates a new Log object copying the output onto the exported print stream to a file in the
   * specified directory.
   *
   * @param baseDir name of the directory for logging output.
   * @throws IOException On I/O error
   */
  public Log(File baseDir) throws IOException {
    this.baseDir = baseDir;
    System.setErr(getPrintStream());
  }

  /**
   * Create a stream for writing errors to the log.
   *
   * @return output stream to the error log.
   * @throws IOException On I/O error
   */
  protected PrintStream getPrintStream() throws IOException {
    logFile = new File(baseDir, "errors.txt");

    if (!baseDir.canWrite())
      throw new IOException("Cannot write to directory " + baseDir);
    if (logFile.exists() && !logFile.canWrite())
      throw new IOException("Cannot write to file " + logFile);
    if (logFile.length() > 1000000) {
      File backup = FileBackup.create(logFile, 3);
      if (backup != null) {
        logFile.delete();
      }
    }

    OutputStreamWriter osw =
        FileType.createEncodingWriter(new FileOutputStream(logFile.getAbsolutePath(), true),
            FileType.DEFAULT_ENCODING.toString());
    encoding = osw.getEncoding();

    return new PrintStream(new StreamWrapper(new BufferedWriter(osw)));
  }

  /**
   * Wrapper for the logging stream for adding timestamp and linebreaks to output.
   */
  private static class StreamWrapper extends OutputStream {
    BufferedWriter out = null;

    /**
     * Creates a new StreamWrapper object.
     */
    public StreamWrapper(BufferedWriter out) {
      super();
      this.out = out;

      Thread timeStamper = new Thread("Log-TimeStamper") {
        @Override
        public void run() {
          while (true) {
            try {
              // 2002.05.05 pavkovic: Synchronization needed because of multithreading
              synchronized (StreamWrapper.this.out) {
                StreamWrapper.this.out.write((new java.util.Date(System.currentTimeMillis()))
                    .toString());
                StreamWrapper.this.out.newLine();
                StreamWrapper.this.out.flush();
              }
            } catch (IOException e) {
              e.printStackTrace();
            }

            try {
              Thread.sleep(1000 * 60 * 10);
            } catch (InterruptedException e) {
              // no problem
            }
          }
        }
      };

      timeStamper.start();

      Thread flusher = new Thread("Log-Flusher") {
        @Override
        public void run() {
          while (true) {
            try {
              StreamWrapper.this.out.flush();
            } catch (IOException e) {
              e.printStackTrace();
            }

            try {
              Thread.sleep(1000);
            } catch (InterruptedException e) {
              // no problem
            }
          }
        }
      };

      flusher.start();
    }

    /**
     * Write value to the stream and to the console.
     *
     * @throws IOException passed on from delegate streams
     */
    @Override
    public void write(int b) throws IOException {
      System.out.write(b);
      out.write(b);
    }
  }

  /**
   * Return the file used for logging or <code>null</code>.
   */
  public File getFile() {
    return logFile;
  }

  /**
   * Returns the value of encoding.
   *
   * @return Returns encoding.
   */
  public String getEncoding() {
    return encoding;
  }

}
