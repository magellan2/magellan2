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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.io.Reader;
import java.text.ParseException;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;
import java.util.StringTokenizer;

import magellan.library.CoordinateID;
import magellan.library.GameData;
import magellan.library.ID;
import magellan.library.Region;
import magellan.library.Unit;
import magellan.library.UnitID;
import magellan.library.gamebinding.EresseaConstants;
import magellan.library.gamebinding.RulesException;
import magellan.library.io.BOMReader;
import magellan.library.io.file.FileType;
import magellan.library.utils.logging.Logger;

/**
 * A class for executing ECheck and reading the output returned.
 */
public class JECheck extends Reader {
  private static final Logger log = Logger.getInstance(JECheck.class);
  private static String FIELD_SEP = "|";
  private Reader outputReader = null;
  private File tempFile = null;

  /**
   * Creates a new JECheck object by executing ECheck with the specified parameters and making its
   * output accessible through the Reader interface.
   *
   * @param eCheckExe a file specifying the ECheck executable. It is assumed that the files
   *          items.txt and zauber.txt are located in same directory as the executable itself.
   * @param orders the file containing the orders to be parsed by ECheck. If orders is null ECheck
   *          is executed without a specified orders file.
   * @param options additional options to be passed to ECheck to control its operation and output.
   * @param settings the settings of the client, needed to find out about forced ISO encod
   * @throws IOException DOCUMENT-ME
   */
  public JECheck(File eCheckExe, File orders, String options, Properties settings)
      throws IOException {
    List<String> commandLine = new LinkedList<String>();
    Process p = null;
    long start = 0;

    /* check ECheck executable */
    if ((eCheckExe.exists() == false) || (eCheckExe.canRead() == false))
      throw new IOException("Specified ECheck executable " + eCheckExe.getAbsolutePath()
          + " is invalid");

    /*
     * put the exe into the command line, tell ECheck to use magellan mode output and where to find
     * items.txt and zauber.txt
     */
    commandLine.add(eCheckExe.getAbsolutePath());

    // create a temp file for ECheck's output
    try {
      tempFile = File.createTempFile("JECheck", null);
    } catch (Exception e) {
      JECheck.log.error("JECheck.JECheck(): unable to create temporary file for output", e);
      throw new IOException("Unable to create temporary file for output: " + e);
    }

    commandLine.add("-O" + tempFile.getAbsolutePath());

    commandLine.add("-P" + eCheckExe.getParentFile().getAbsolutePath());

    // // TODO: make optionizable
    // commandLine.add("-nolost");
    // commandLine.add("-noroute");

    StringTokenizer optiontokens = new StringTokenizer(options);

    while (optiontokens.hasMoreTokens()) {
      commandLine.add(optiontokens.nextToken());
    }

    if (orders != null) {
      /*
       * do this so the -m option is not necessarily specified in the arguments but prevent problems
       * with older versions
       */
      if (JECheck.getVersion(eCheckExe, settings).compareTo(new Version("4.0.6", ".")) >= 0) {
        commandLine.add("-m");
      }

      commandLine.add(orders.getAbsolutePath());
    }

    // run the beast
    JECheck.log.info("ECheck is executed with this command line:\n" + commandLine);
    start = System.currentTimeMillis();

    try {
      p = Runtime.getRuntime().exec(commandLine.toArray(new String[] {}));
    } catch (Exception e) {
      JECheck.log.error("JECheck.JECheck(): exception while executing echeck", e);
      throw new IOException("Cannot execute ECheck: " + e.toString());
    }

    while (true) {
      try {
        Thread.sleep(100);
      } catch (InterruptedException e) {
      }

      // bail out if ECheck runs for longer than 20 secs
      if ((System.currentTimeMillis() - start) > 20000) {
        p.destroy();
        JECheck.log.warn("JECheck.JECheck(): echeck was terminated since it ran for too long");

        break;
      }

      // the loop is left when the process has terminated
      // termination is indicated by the exitValue() method
      // throwing no IllegalStateException
      try {
        p.exitValue();

        break;
      } catch (IllegalThreadStateException e) {
      }
    }

    // get a reader on the temporary output file (which is deleted on close)
    try {
      // apexo (Fiete) 20061205: if in properties, force ISO encoding

      if (PropertiesHelper.getBoolean(settings, "TextEncoding.ISOopenOrders", false)) {
        // new: force our default = ISO
        outputReader =
            new InputStreamReader(new FileInputStream(tempFile), FileType.DEFAULT_ENCODING
                .toString());
      } else if (PropertiesHelper.getBoolean(settings, "TextEncoding.UTFopenOrders", false)) {
        // new: force our default = ISO
        outputReader =
            new InputStreamReader(new FileInputStream(tempFile), FileType.DEFAULT_ENCODING
                .toString());
      } else {
        // old = default = system dependent
        outputReader = new BOMReader(new FileInputStream(tempFile), null);
      }
    } catch (Exception e) {
      JECheck.log.error(
          "JECheck.JECheck(): cannot create a file reader on the temporary output file", e);
      throw new IOException("Cannot create a file reader on the temporary output file: " + e);
    }
  }

  /**
   * Reads from the output of ECheck.
   *
   * @throws IOException DOCUMENT-ME
   */
  @Override
  public int read(char cbuf[], int off, int len) throws IOException {
    if (outputReader != null)
      return outputReader.read(cbuf, off, len);
    else
      throw new IOException("No ECheck output available");
  }

  /**
   * Closes the reader on the output of ECheck.
   *
   * @throws IOException DOCUMENT-ME
   */
  @Override
  public void close() throws IOException {
    if (outputReader != null) {
      outputReader.close();
    }

    tempFile.delete();
  }

  /**
   * Mark the present position in the stream. Subsequent calls to reset() will attempt to reposition
   * the stream to this point. Not all character-input streams support the mark() operation.
   *
   * @param readAheadLimit Limit on the number of characters that may be read while still preserving
   *          the mark. After reading this many characters, attempting to reset the stream may fail.
   * @throws IOException If the stream does not support mark(), or if some other I/O error occurs.
   */
  @Override
  public void mark(int readAheadLimit) throws IOException {
    if (outputReader != null) {
      outputReader.mark(readAheadLimit);
    } else
      throw new IOException("No ECheck output available");
  }

  /**
   * Tell whether this stream supports the mark() operation.
   */
  @Override
  public boolean markSupported() {
    return outputReader.markSupported();
  }

  /**
   * Tell whether this stream is ready to be read.
   *
   * @return True if the next read() is guaranteed not to block for input, false otherwise. Note
   *         that returning false does not guarantee that the next read will block.
   * @throws IOException If an I/O error occurs
   */
  @Override
  public boolean ready() throws IOException {
    if (outputReader != null)
      return outputReader.ready();
    else
      throw new IOException("No ECheck output available");
  }

  /**
   * Reset the stream. If the stream has been marked, then attempt to reposition it at the mark. If
   * the stream has not been marked, then attempt to reset it in some way appropriate to the
   * particular stream, for example by repositioning it to its starting point. Not all
   * character-input streams support the reset() operation, and some support reset() without
   * supporting mark().
   *
   * @throws IOException If the stream has not been marked, or if the mark has been invalidated, or
   *           if the stream does not support reset(), or if some other I/O error occurs
   */
  @Override
  public void reset() throws IOException {
    if (outputReader != null) {
      outputReader.reset();
    } else
      throw new IOException("No ECheck output available");
  }

  /**
   * Skip characters. This method will block until some characters are available, an I/O error
   * occurs, or the end of the stream is reached.
   *
   * @param n The number of characters to skip
   * @return The number of characters actually skipped
   * @throws IOException If an I/O error occurs
   */
  @Override
  public long skip(long n) throws IOException {
    if (outputReader != null)
      return outputReader.skip(n);
    else
      throw new IOException("No ECheck output available");
  }

  /**
   * Runs ECheck with the specified parameters and returns the error and warning messages it
   * reported. For reference for the parameters see the constructor. This method requires that the
   * specified echeck executable not older than the version returned by getRequiredVersion().
   *
   * @return a collection containing JECheck.ECheckMessage objects.
   * @throws IOException DOCUMENT-ME
   * @throws java.text.ParseException DOCUMENT-ME
   */
  public static Collection<ECheckMessage> getMessages(File eCheckExe, File orders, String options,
      Properties settings) throws IOException, java.text.ParseException {
    return JECheck.getMessages(new JECheck(eCheckExe, orders, options, settings));
  }

  /**
   * Returns a collection containing JECheck.ECheckMessage objects by reading from the specified
   * reader. This method requires that the specified echeck executable not older than the version
   * returned by getRequiredVersion().
   *
   * @return a collection containing JECheck.ECheckMessage objects.
   * @throws IOException DOCUMENT-ME
   * @throws java.text.ParseException DOCUMENT-ME
   */
  public static Collection<ECheckMessage> getMessages(Reader echeckOutput) throws IOException,
  java.text.ParseException {
    Collection<ECheckMessage> msgs = new LinkedList<ECheckMessage>();
    BufferedReader in = new LineNumberReader(echeckOutput);
    String line = null;
    int errors = -1;
    int warnings = -1;

    boolean error = false;

    line = JECheck.getLine(in);

    if (line == null)
      throw new java.text.ParseException("ECheck output is empty", 0);

    //
    // 2002.05.05 pavkovic: some more documentation:
    // for echeck <= 4.1.4, the first line looked like
    // <filename>|<version>|<date><filename>:<faction>:<factionid>
    // That is ok for the code below.
    // In newer versions, the first two lines look like
    // <filename>|<version>|<date>
    // <filename>:<faction>:<factionid>
    // To fake the old behaviour we simulate the "first"-line of echeck <= 4.1.4
    //

    // 2006.08.30 fiete: echeck may produce error messages....
    // we will try to just skip these messages
    // actuel example with version 4.3.2-3:
    // Fehler in Datei meldungen.txt Zeile 13: `BEHIND'
    // Error in file meldungen.txt line 13: `BEHIND'
    // yes, both languages

    while (line.startsWith("Fehler") || line.startsWith("Error")) {
      line = JECheck.getLine(in);
    }

    if (line.indexOf(":faction:") == -1) {
      line += JECheck.getLine(in);
    }

    /* parse header <filename>:faction:<factionid> */
    StringTokenizer tokenizer = new StringTokenizer(line, JECheck.FIELD_SEP);

    if (tokenizer.countTokens() == 4) {
      tokenizer.nextToken();

      if (!tokenizer.nextToken().equalsIgnoreCase("version")) {
        error = true;
      }
    } else {
      error = true;
    }

    /* check for error in header */
    if (error)
      throw new java.text.ParseException("The header of the ECheck output seems to be corrupt: "
          + line, 0);

    /* parse messages */
    line = JECheck.getLine(in);

    while (line != null) {
      tokenizer = new StringTokenizer(line, JECheck.FIELD_SEP);

      if (tokenizer.countTokens() >= 4) {
        ECheckMessage msg = new ECheckMessage(line);
        msgs.add(msg);
      } else {
        break;
      }

      line = JECheck.getLine(in);
    }

    if (line == null)
      throw new java.text.ParseException("The ECheck output seems to miss a footer", 0);

    /* parse footer */
    if (tokenizer.countTokens() == 3) {
      tokenizer.nextToken();

      if (tokenizer.nextToken().equalsIgnoreCase("warnings")) {
        warnings = Integer.parseInt(tokenizer.nextToken());
      } else {
        error = true;
      }

      line = JECheck.getLine(in);

      if (line != null) {
        tokenizer = new StringTokenizer(line, JECheck.FIELD_SEP);

        if (tokenizer.countTokens() == 3) {
          tokenizer.nextToken();

          if (tokenizer.nextToken().equalsIgnoreCase("errors")) {
            errors = Integer.parseInt(tokenizer.nextToken());
          } else {
            error = true;
          }
        } else {
          error = true;
        }
      } else {
        error = true;
      }
    } else {
      error = true;
    }

    if (error)
      throw new java.text.ParseException("The footer in the ECheck output seems to be corrupt: "
          + line, 0);

    in.close();

    if ((errors == -1) || (warnings == -1))
      throw new java.text.ParseException(
          "Unable to determine number of ECheck errors and warnings in ECheck summary.", 0);

    int errorsFound = 0;
    int warningsFound = 0;

    for (ECheckMessage msg : msgs) {
      if (msg.type == ECheckMessage.ERROR) {
        errorsFound++;
      } else if (msg.type == ECheckMessage.WARNING) {
        warningsFound++;
      }
    }

    if (errorsFound != errors)
      throw new java.text.ParseException(
          "The number of errors found does not match the summary information from ECheck.", 0);

    if (warningsFound != warnings)
      throw new java.text.ParseException(
          "The number of warnings found does not match the summary information from ECheck.", 0);

    return msgs;
  }

  /**
   * Returns the next line from the buffered Reader or null iff the the end of the stream is
   * reached.
   *
   * @throws IOException DOCUMENT-ME
   */
  private static String getLine(BufferedReader in) throws IOException {
    String line = null;

    while (in.ready()) {
      line = in.readLine();

      if (line == null) {
        continue;
      } else {
        break;
      }
    }

    return line;
  }

  /**
   * Determines the Unit or Region referenced in an ECheck message by looking at the orders the
   * ECheck messages were generated from.
   *
   * @param data the game data the orders belong to and from which the Unit or Region can be
   *          retrieved.
   * @param orderFile a file containing the orders that ECheck was run on when it produced the
   *          specified message.
   * @param messages the messages to be processed.
   * @return the collection of input messages with their affected objects fields set if possible.
   * @throws IOException DOCUMENT-ME
   * @throws IllegalArgumentException DOCUMENT-ME
   */
  public static Collection<ECheckMessage> determineAffectedObjects(GameData data, File orderFile,
      List<ECheckMessage> messages) throws IOException {
    if (data == null)
      throw new IllegalArgumentException(
          "JECheck.getAffectedObject(): invalid data argument specified.");

    if (orderFile == null)
      throw new IllegalArgumentException(
          "JECheck.getAffectedObject(): invalid orderFile argument specified.");

    if (!orderFile.exists())
      throw new IllegalArgumentException(
          "JECheck.getAffectedObject(): the specified orderFile file does not exist.");

    if (messages == null)
      throw new IllegalArgumentException(
          "JECheck.getAffectedObject(): invalid msgs argument specified.");

    String line = null;
    LineNumberReader lnr = null;
    List<String> orders = new LinkedList<String>();

    String unitOrder = null, regionOrder = null;
    /* frequently used strings */
    try {
      unitOrder =
          data.getGameSpecificStuff().getOrderChanger().getOrder(EresseaConstants.OC_UNIT,
              Locales.getOrderLocale(), new Object[] { true }).getText();
      regionOrder =
          data.getGameSpecificStuff().getOrderChanger().getOrder(EresseaConstants.OC_REGION,
              Locales.getOrderLocale(), new Object[] { true }).getText();
    } catch (RulesException e) {
      // orrder is null
    }
    /*
     * first read in all the orders into a list to access them quickly later
     */
    lnr = new LineNumberReader(new FileReader(orderFile));

    for (line = lnr.readLine(); (line != null); line = lnr.readLine()) {
      orders.add(line);
    }

    lnr.close();

    /*
     * now walk all messages and determine the affected object by looking at the line referenced.
     */
    for (ECheckMessage msg : messages) {
      if (msg.getLineNr() > 0) {
        for (int i = msg.getLineNr() - 1; i > -1; i--) {
          String order = Umlaut.normalize(orders.get(i));
          StringTokenizer tokenizer = new StringTokenizer(order, " ;");

          if (tokenizer.countTokens() < 2) {
            continue;
          }

          String token = tokenizer.nextToken();

          if (unitOrder != null && unitOrder.startsWith(token)) {
            try {
              ID id = UnitID.createUnitID(tokenizer.nextToken(), data.base);
              Unit u = data.getUnit(id);

              if (u != null) {
                msg.setAffectedObject(u);

                break;
              }
            } catch (Exception e) {
              JECheck.log.error(e);
            }
          } else if (regionOrder != null && regionOrder.startsWith(token)) {
            try {
              CoordinateID id = CoordinateID.parse(tokenizer.nextToken(), ",");
              Region r = data.getRegion(id);

              if (r != null) {
                msg.setAffectedObject(r);

                break;
              }
            } catch (Exception e) {
              JECheck.log.error(e);
            }
          }
        }
      }
    }

    return messages;
  }

  /**
   * Returns what ECheck returns when called with the -h option.
   *
   * @param eCheckExe
   * @param settings
   * @throws IOException
   */
  public static ECheckMessage getHelp(File eCheckExe, Properties settings) throws IOException {
    BufferedReader br = null;
    StringBuffer buffer = new StringBuffer("HELP|-1|-1|");

    try {
      br = new BufferedReader(new JECheck(eCheckExe, null, "-h", settings));
      String line = br.readLine();
      while (line != null) {
        buffer.append(line + "\n");
        line = br.readLine();
      }
      br.close();

    } catch (Exception e) {
      JECheck.log.error(e);
      if (br != null) {
        br.close();
      }
      throw new IOException("Cannot retrieve help: " + e.toString());
    }

    try {
      return new ECheckMessage(buffer.toString());
    } catch (ParseException e) {
      // cannot happen...
      e.printStackTrace();
      return null;
    }

  }

  /**
   * Returns the version of the specified ECheck executable file.
   *
   * @throws IOException DOCUMENT-ME
   */
  public static Version getVersion(File eCheckExe, Properties settings) throws IOException {
    Version v = null;
    String version = null;
    String line = null;
    BufferedReader br = null;

    try {
      br = new BufferedReader(new JECheck(eCheckExe, null, "-h", settings));
      line = br.readLine().toLowerCase();
      br.close();

      int verStart = line.indexOf("version ") + "version ".length();
      int verEnd = line.indexOf(",", verStart);

      if ((verStart > 0) && (verEnd > verStart)) {
        version = line.substring(verStart, verEnd);
        // Fiete: problems wirth version 4.3.2-3, build 2-3 cannot convert to int
        // solution: ignore -3 asuming, only in build number a "-" will occure
        int verEnd2 = version.indexOf("-");
        if (verEnd2 > 1) {
          version = version.substring(0, verEnd2);
        }
        v = new Version(version, ".");
      }
    } catch (Exception e) {
      v = null;
      JECheck.log.error(e);
      throw new IOException("Cannot retrieve version information: " + e.toString());
    }

    if (v == null)
      throw new IOException("Cannot retrieve version information");

    return v;
  }

  /**
   * Checks whether the version of the specified ECheck executable is valid and its output can be
   * processed.
   *
   * @throws IOException DOCUMENT-ME
   */
  public static boolean checkVersion(File eCheckExe, Properties settings) throws IOException {
    Version version = JECheck.getVersion(eCheckExe, settings);

    return (version.compareTo(JECheck.getRequiredVersion()) >= 0);
  }

  /**
   * Returns the oldest version of ECheck that is required by this class to process the orders
   * correctly.
   */
  public static Version getRequiredVersion() {
    return new Version("4.1.2", ".");
  }

  /**
   * A class representing a message as it produced by ECheck.
   */
  public static class ECheckMessage {
    /** An error message */
    public static final int ERROR = 1;

    /** A warning message */
    public static final int WARNING = 2;

    /** Some other message */
    public static final int MESSAGE = -1;

    private String fileName;
    private int lineNr = 0;
    private int type = 0;
    private int warnLevel = 0;
    private String msg;
    private Object affectedObject; // the object affected by this message

    /**
     * Creates a new ECheckMessage object by parsing the specified String.
     *
     * @throws java.text.ParseException if the rawMessage String cannot be parsed.
     */
    public ECheckMessage(String rawMessage) throws java.text.ParseException {
      this(rawMessage, null);
    }

    public ECheckMessage(String rawMessage, String fileName) throws java.text.ParseException {
      this.fileName = fileName;
      String delim = JECheck.FIELD_SEP;
      StringTokenizer tokenizer = new StringTokenizer(rawMessage, delim);

      if (tokenizer.countTokens() >= 4) {
        try {
          tokenizer.nextToken(); // skip file name
          lineNr = Integer.parseInt(tokenizer.nextToken());
          warnLevel = Integer.parseInt(tokenizer.nextToken());
          type =
              (warnLevel == 0) ? ECheckMessage.ERROR : (warnLevel > 0) ? ECheckMessage.WARNING
                  : ECheckMessage.MESSAGE;
          msg = tokenizer.nextToken();

          while (tokenizer.hasMoreTokens()) {
            msg += (delim + tokenizer.nextToken());
          }
        } catch (Exception e) {
          throw new java.text.ParseException("Unable to parse ECheck message \"" + rawMessage
              + "\"", 0);
        }
      } else
        throw new java.text.ParseException("Unable to parse ECheck message \"" + rawMessage + "\"",
            0);
    }

    /**
     * DOCUMENT-ME
     */
    public String getFileName() {
      return fileName;
    }

    /**
     * Returns the line where the error was. Line numbers start with 1!
     */
    public int getLineNr() {
      return lineNr;
    }

    /**
     * DOCUMENT-ME
     */
    public int getType() {
      return type;
    }

    /**
     * DOCUMENT-ME
     */
    public int getWarningLevel() {
      return warnLevel;
    }

    /**
     * DOCUMENT-ME
     */
    public String getMessage() {
      return msg;
    }

    /**
     * DOCUMENT-ME
     */
    public Object getAffectedObject() {
      return affectedObject;
    }

    /**
     * DOCUMENT-ME
     */
    public void setAffectedObject(Object o) {
      affectedObject = o;
    }

    /**
     * DOCUMENT-ME
     */
    @Override
    public String toString() {
      StringBuffer sb = new StringBuffer();
      sb.append(fileName).append("(").append(lineNr).append("): ");

      if (type == ECheckMessage.ERROR) {
        sb.append("Fehler: ");
      } else if (type == ECheckMessage.WARNING) {
        sb.append("Warnung (").append(warnLevel).append("): ");
      }

      sb.append(msg);

      return sb.toString();
    }
  }
}
