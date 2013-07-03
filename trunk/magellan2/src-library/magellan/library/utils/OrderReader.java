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
import java.io.LineNumberReader;
import java.io.Reader;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.StringTokenizer;

import magellan.library.CompleteData;
import magellan.library.EntityID;
import magellan.library.Faction;
import magellan.library.GameData;
import magellan.library.Region;
import magellan.library.StringID;
import magellan.library.TempUnit;
import magellan.library.Unit;
import magellan.library.UnitID;
import magellan.library.gamebinding.EresseaConstants;
import magellan.library.gamebinding.GameSpecificOrderReader;
import magellan.library.rules.GenericRules;
import magellan.library.utils.logging.Logger;

/**
 * A class for reading a orders file for unit orders.
 */
public abstract class OrderReader implements GameSpecificOrderReader {

  protected interface LineHandler {

    void handle(String line, List<LineHandler> matchingHandlers) throws IOException;

  }

  private static final Logger log = Logger.getInstance(OrderReader.class);
  private GameData data;
  private LineNumberReader stream;
  private boolean autoConfirm;
  private boolean ignoreSemicolonComments;
  private Status status = new Status();
  private boolean doNotOverwriteConfirmedOrders;

  protected String command;
  protected String comment;
  protected RadixTree<LineHandler> handlers = new RadixTreeImpl<LineHandler>();
  protected Faction currentFaction;
  protected Unit currentUnit;
  protected String currentRegion;
  private Locale locale;

  /**
   * Creates a new OrderReader object adding the read orders to the units it can find in the
   * specified game data object. This function clears the caches of all units.
   */
  public OrderReader(GameData g) {
    data = g;

    if (data == null) {
      OrderReader.log
          .info("OrderReader.OrderReader(): game data is null! Creating empty game data to proceed.");
      data = new CompleteData(new GenericRules());
    }

    // clear the caches in game data
    if (data.getUnits() != null) {
      for (Unit u : data.getUnits()) {
        u.clearCache();
      }
    }

    if (data.getRegions() != null) {
      for (Region uc : data.getRegions()) {
        uc.clearCache();
      }
    }

    setLocale(Locales.getOrderLocale());
  }

  /**
   * Sets the order locale.
   */
  public void setLocale(Locale locale) {
    this.locale = locale;

    initHandlers();
  }

  /**
   * Returns the order locale.
   */
  public Locale getLocale() {
    return locale;
  }

  /**
   * Reads the orders from the specified Reader. Orders for multiple factions can be read. Region
   * lines are ignored. Unit are not created. If there are orders for a unit that cannot be found in
   * the game data these orders are ignored. Lines containing ECHECK comments are always ignored.
   * Comments starting with a semicolon and containing the literal 'bestaetigt' (case and umlaut
   * insensitive) after an arbitrary number of whitespace characters are never added to a unit's
   * orders, instead they set the order confirmation status of the unit to true.
   * 
   * @throws IOException if an I/O error occurs
   */
  public void read(Reader in) throws IOException {
    stream = new LineNumberReader(new MergeLineReader(in));

    String line = stream.readLine();
    splitLine(line);
    firstHandle(line);

    line = stream.readLine();
    while (line != null) {
      splitLine(line);
      if (command.trim().length() == 0 && comment.length() > 0) {
        commentHandle(line);
      } else {
        applyHandler(line);
      }
      line = stream.readLine();
    }

    endHandler();

  }

  protected void splitLine(String line) {
    int commentPos = line.indexOf(EresseaConstants.O_COMMENT);
    if (commentPos < 0) {
      command = line;
      comment = "";
    } else {
      command = line.substring(0, commentPos);
      comment = line.substring(line.indexOf(EresseaConstants.O_COMMENT));
    }
  }

  protected void initHandlers() {
    handlers = new RadixTreeImpl<OrderReader.LineHandler>();
    addHandler(data.rules.getOrderfileStartingString(), new StartingHandler());
    addHandler(getOrderTranslation(EresseaConstants.OC_REGION), new RegionHandler());
    addHandler(getOrderTranslation(EresseaConstants.OC_UNIT), new UnitHandler());
  }

  protected void addHandler(String trigger, LineHandler handler) {
    handlers.insert(normalize(trigger), handler);
  }

  protected List<LineHandler> applyHandler(String line) throws IOException {
    StringTokenizer tokenizer = new StringTokenizer(command);

    List<LineHandler> matchingHandlers = null;
    if (tokenizer.hasMoreTokens()) {
      String token = tokenizer.nextToken();
      if (token.trim().length() == 0) {
        defaultHandle(line);
      }

      matchingHandlers = getHandlers(token);
      if (matchingHandlers.size() == 0) {
        defaultHandle(line);
      } else {
        for (LineHandler handler : matchingHandlers) {
          handler.handle(line, matchingHandlers);
        }
      }
    }
    return matchingHandlers;
  }

  protected List<LineHandler> getHandlers(String token) {
    return handlers.searchPrefix(normalize(token), Integer.MAX_VALUE);
  }

  protected String normalize(String token) {
    return Umlaut.normalize(token.trim()).toLowerCase();
  }

  protected void firstHandle(String line) throws IOException {
    List<LineHandler> matching = applyHandler(line);
    if (matching == null || matching.size() != 1 || !(matching.get(0) instanceof StartingHandler)) {
      log.warn("order file does not start with orde file starting string");
      status.errors++;
    }
  }

  protected void defaultHandle(String line) {
    if (currentUnit != null) {
      currentUnit.addOrder(line, false);
    } else if (!command.trim().isEmpty()) {
      log.warn("unknown command outside UNIT encountered:\n" + line);
      status.errors++;
    }
  }

  protected void commentHandle(String line) {
    /*
     * There was a problem using this StringTokenizer: If a unit had an order like
     * " ; Einheit hat Kommando" the tokenizer skipped the leading semicolon and tried to parse a
     * new order instead of parsing this line as a comment. So treat lines, that start with a
     * semicolon special!
     */
    if (currentUnit != null) {
      // mark orders as confirmed on a ";bestaetigt" comment
      String rest = Umlaut.normalize(line.substring(line.indexOf(';') + 1).trim());

      if (rest.equalsIgnoreCase(OrderWriter.CONFIRMED)) {
        currentUnit.setOrdersConfirmed(true);
      } else if ((ignoreSemicolonComments == false) && getCheckerName() != null
          && (rest.startsWith(getCheckerName().toUpperCase()) == false)) {
        // add all other comments except "; ECHECK ..." to the orders
        currentUnit.addOrder(line, false);
      }
    } else {
      defaultHandle(line);
    }

  }

  protected void endHandler() {
    if (currentFaction != null) {
      log.warn("missing NEXT");
      status.errors++;
    }

    // necessary to refresh unit relations
    data.postProcess();
  }

  protected void endFaction() {
    currentFaction = null;
    currentRegion = null;
    currentUnit = null;
    // currentUnit.extractTempUnits(data, 0, getLocale());
  }

  /**
   * Handles lines with order starting string
   */
  public class StartingHandler implements LineHandler {

    public void handle(String line, List<LineHandler> matchingHandlers) throws IOException {
      StringTokenizer tokenizer = new StringTokenizer(line);
      String token = tokenizer.nextToken();
      token = tokenizer.nextToken();
      try {
        EntityID fID = EntityID.createEntityID(token, data.base);
        currentFaction = data.getFaction(fID);
        currentUnit = null;
        currentRegion = null;

        if (currentFaction == null) {
          OrderReader.log.info("OrderReader.read(): The faction with id " + fID + " (" + token
              + ") is not present in the game data, skipping this faction.");
          status.errors++;
        } else {
          status.factions++;
        }
      } catch (NumberFormatException e) {
        OrderReader.log.error("OrderReader.read(): Unable to parse faction id: " + e.toString()
            + " at line " + stream.getLineNumber(), e);
        status.errors++;
      }
    }
  }

  /**
   * Handles NEXT command.
   */
  public class NextHandler implements LineHandler {

    public void handle(String line, List<LineHandler> matchingHandlers) throws IOException {
      endFaction();
    }
  }

  /**
   * Handles LOCALE lines.
   */
  public class LocaleHandler implements LineHandler {

    public void handle(String line, List<LineHandler> matchingHandlers) throws IOException {
      if (currentUnit != null || currentRegion != null) {
        log.warn("LOCALE encountered outside order header");
        status.errors++;
      }
      StringTokenizer tokenizer = new StringTokenizer(command);
      String token = tokenizer.nextToken();

      if (tokenizer.hasMoreTokens()) {
        token = tokenizer.nextToken().replace('"', ' ').trim();
        setLocale(new Locale(token, ""));
      } else {
        log.warn("locale command without locale");
        status.errors++;
      }
    }
  }

  /**
   * Handles REGION lines.
   */
  public class RegionHandler implements LineHandler {

    public void handle(String line, List<LineHandler> matchingHandlers) throws IOException {
      // ignore
      currentRegion =
          command.substring(getOrderTranslation(EresseaConstants.OC_REGION).length()).trim();
      currentUnit = null;
    }
  }

  /**
   * Handles UNIT lines.
   */
  public class UnitHandler implements LineHandler {

    public void handle(String line, List<LineHandler> matchingHandlers) throws IOException {
      StringTokenizer tokenizer = new StringTokenizer(command);
      String token = tokenizer.nextToken();
      token = tokenizer.nextToken();

      UnitID unitID = null;

      try {
        unitID = UnitID.createUnitID(token, data.base);
      } catch (NumberFormatException e) {
        OrderReader.log.error("could not parse unit ID at line " + stream.getLineNumber() + ":\n"
            + line);
        status.errors++;
      }

      if (unitID != null) {
        status.units++;

        /* turn orders into 'real' temp units */
        if (currentUnit != null) {
          currentUnit.extractTempUnits(data, 0, getLocale());
        }

        currentUnit = data.getUnit(unitID);

        if (currentUnit == null) {
          // do not add unknown units
          // currentUnit = MagellanFactory.createUnit(unitID);
          // currentUnit.setFaction(faction);
          //
          // data.addUnit(currentUnit);
          status.unknownUnits++;
        } else {
          if (currentUnit.isOrdersConfirmed() && doNotOverwriteConfirmedOrders) {
            // we have a unit with confirmed orders and no OK for
            // changing anything
            // feature request #296, Fiete
            currentUnit = null;
            status.confirmedUnitsNotOverwritten++;
          } else {
            /*
             * the unit already exists so delete all its temp units
             */
            Collection<UnitID> victimIDs = new LinkedList<UnitID>();

            for (TempUnit tempUnit : currentUnit.tempUnits()) {
              victimIDs.add((tempUnit).getID());
            }

            for (UnitID id2 : victimIDs) {
              currentUnit.deleteTemp(id2, data);
            }
          }
        }
        if (currentUnit != null) {
          currentUnit.clearOrders();
          currentUnit.setOrdersConfirmed(autoConfirm);
        }
      } else {
        currentUnit = null;
      }
    }
  }

  protected String getOrderTranslation(StringID orderId) {
    return data.getRules().getGameSpecificStuff().getOrderChanger().getOrder(getLocale(), orderId);
  }

  /**
   * Returns whether all read orders get automatically confirmed.
   */
  public boolean getAutoConfirm() {
    return autoConfirm;
  }

  /**
   * Sets whether all read orders get automatically confirmed.
   */
  public void setAutoConfirm(boolean autoConfirm) {
    this.autoConfirm = autoConfirm;
  }

  /**
   * Returns whether all comments in the orders starting with a semicolon (except confirmation
   * comments) are ignored.
   */
  public boolean isIgnoringSemicolonComments() {
    return ignoreSemicolonComments;
  }

  /**
   * Sets whether all comments in the orders starting with a semicolon (except confirmation
   * comments) are ignored.
   */
  public void setIgnoreSemicolonComments(boolean ignoreSemicolonComments) {
    this.ignoreSemicolonComments = ignoreSemicolonComments;
  }

  /**
   * Returns the number of factions and units that were read. This method should only be called
   * after reading the orders has finished.
   */
  public Status getStatus() {
    return status;
  }

  /**
   * Returns whether orders of confirmed units should be overwritten.
   * 
   * @return Returns doNotOverwriteConfirmedOrders.
   */
  public boolean isDoNotOverwriteConfirmedOrders() {
    return doNotOverwriteConfirmedOrders;
  }

  /**
   * Sets whether orders of confirmed units should be overwritten. If set to <code>true</code>,
   * orders of confirmed units will not be changed.
   * 
   * @param doNotOverwriteConfirmedOrders The value for doNotOverwriteConfirmedOrders.
   */
  public void setDoNotOverwriteConfirmedOrders(boolean doNotOverwriteConfirmedOrders) {
    this.doNotOverwriteConfirmedOrders = doNotOverwriteConfirmedOrders;
  }

}
