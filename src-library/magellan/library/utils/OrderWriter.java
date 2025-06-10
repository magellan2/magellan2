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
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;

import magellan.library.Faction;
import magellan.library.GameData;
import magellan.library.Group;
import magellan.library.Item;
import magellan.library.Order;
import magellan.library.Region;
import magellan.library.StringID;
import magellan.library.TempUnit;
import magellan.library.Unit;
import magellan.library.gamebinding.EresseaConstants;
import magellan.library.gamebinding.GameSpecificOrderWriter;
import magellan.library.plugins.OrderWriterPlugIn;
import magellan.library.rules.ConstructibleType;
import magellan.library.utils.logging.Logger;

/**
 * A class for writing orders of all units of a certain faction to a stream.
 */
public abstract class OrderWriter implements GameSpecificOrderWriter {
  private static final Logger log = Logger.getInstance(OrderWriter.class);

  /** Marker for confirmed units */
  public static final String CONFIRMED = "bestaetigt";

  /** Marker for confirmed TEMP units */
  public static final String CONFIRMEDTEMP = OrderWriter.CONFIRMED + "_temp";

  private String syntaxCheckOptions = "";
  private GameData world;
  private Faction faction;
  private Group group;
  private boolean addECheckComments = true;
  private boolean removeSCComments = false;
  private boolean removeSSComments = false;
  private boolean confirmedOnly = false;
  private boolean forceUnixLineBreaks = false;
  private Collection<Region> regions;
  private boolean writeUnitTagsAsVorlageComment = false;
  /**
   * sometimes I don't want the timestamp..
   * 
   * @author Fiete
   */
  private boolean writeTimeStamp = true;

  protected String commentStart = EresseaConstants.O_COMMENT;

  protected List<OrderWriterPlugIn> plugins = new ArrayList<OrderWriterPlugIn>();

  private List<String> errors;

  /**
   * Creates a new OrderWriter object extracting the orders of faction f's units and writing them to
   * the stream w.
   * 
   * @param g GameData object ot get orders from.
   * @param f the faction the orders are written for.
   */
  public OrderWriter(GameData g, Faction f) {
    this(g, f, null);
  }

  /**
   * Creates a new OrderWriter object extracting the orders of faction f's units and writing them to
   * the stream w with the specified options for E-Check.
   * 
   * @param g GameData object ot get orders from.
   * @param f the faction the orders are written for.
   * @param echeckOpts options for E-Check, default is " -s -l -w4"
   */
  public OrderWriter(GameData g, Faction f, String echeckOpts) {
    world = g;
    faction = f;
    errors = new ArrayList<String>(2);

    setECheckOptions(echeckOpts);
  }

  /**
   * Creates a new OrderWriter.
   */
  public OrderWriter() {
    errors = new ArrayList<String>(2);

    setECheckOptions(null);
  }

  /**
   * As {@link #write(BufferedWriter)}, but using a writer.
   */
  @Override
  public int write(Writer write) throws IOException {
    return write(new BufferedWriter(write));
  }

  /**
   * Writes the faction's orders to the stream. World and faction must be set, possibly using
   * {@link #setGameData(GameData)} and {@link #setFaction(Faction)}.
   * 
   * @param stream
   * @return The number of written units
   * @throws IOException If an I/O error occurs
   */
  @Override
  public synchronized int write(BufferedWriter stream) throws IOException {
    errors.clear();
    if (world == null) {
      log.warn("no game data");
      return 0;
    }
    if (faction == null) {
      faction = world.getFaction(world.getOwnerFaction());
    }
    if (faction == null) {
      log.warn("no faction");
      return 0;
    }

    writeHeader(stream);

    int units =
        writeRegions(((regions != null) && (regions.size() > 0)) ? regions : world.getRegions(),
            stream);
    writeFooter(stream);

    // we flush on purpose to fill the underlying Writer
    // with the buffered content of the BufferedWriter
    stream.flush();

    return units;
  }

  @Override
  public void setWriteUnitTagsAsVorlageComment(boolean bool) {
    writeUnitTagsAsVorlageComment = bool;
  }

  /**
   * Write comments used by ECheck order checker.
   */
  @Override
  public void setAddECheckComments(boolean bool) {
    addECheckComments = bool;
  }

  /**
   * Remove transient (semicolon type) and permanent (// type) comments.
   */
  @Override
  public void setRemoveComments(boolean semicolon, boolean slashslash) {
    removeSCComments = semicolon;
    removeSSComments = slashslash;
  }

  /**
   * Enforce that only Unix-style linebreaks are used. This is necessary when writing to the
   * clipboard under Windows.
   */
  @Override
  public void setForceUnixLineBreaks(boolean bool) {
    forceUnixLineBreaks = bool;
  }

  /**
   * Set a group. Only orders of units in this group are written.
   */
  @Override
  public void setGroup(Group group) {
    this.group = group;
  }

  protected void writeHeader(BufferedWriter stream) throws IOException {
    writeOrderfileStartingString(stream);

    if (writeTimeStamp) {
      writeCommentLine(stream, "TIMESTAMP " + getTimeStamp());
    }
    writeCommentLine(stream, "Magellan Version " + VersionInfo.getVersion(null));

    if (useChecker()) {
      if (addECheckComments) {
        writeCommentLine(stream, " " + getCheckerName().toUpperCase() + " "
            + getSyntaxCheckOptions());
      }
    }

    writeLocale(stream);
  }

  protected void writeOrderfileStartingString(BufferedWriter stream) throws IOException {
    stream.write(world.getRules().getOrderfileStartingString());
    stream.write(" " + faction.getID());
    writeln(stream, " \"" + faction.getPassword() + "\"");
  }

  protected void writeLocale(BufferedWriter stream) throws IOException {
    if (getLocale() != null) {
      writeln(stream, "LOCALE " + getLocale().getLanguage());
    } else {
      addError("locale unknown");
    }
  }

  protected Locale getLocale() {
    return faction.getLocale();
  }

  protected String getOrderTranslation(StringID orderId) {
    return world.getGameSpecificStuff().getOrderChanger().getOrderO(getLocale(), orderId).getText();
  }

  protected int writeRegions(Collection<? extends Region> writtenRegions, BufferedWriter stream)
      throws IOException {
    int writtenUnits = 0;

    for (Region r : writtenRegions) {
      Collection<Unit> units = filterUnits(r.units());

      if (units.size() > 0) {
        writtenUnits += writeRegion(r, units, stream);
        units.clear(); // this should help the garbage collector
      }

      units = null;
    }

    return writtenUnits;
  }

  protected int writeRegion(Region r, Collection<Unit> units, BufferedWriter stream)
      throws IOException {
    if (addECheckComments) {
      writeRegionLine(stream, r);
    }

    int writtenUnits = 0;

    for (Unit u : units) {
      if (writeUnit(u, stream)) {
        writtenUnits++;
      }
    }

    return writtenUnits;
  }

  protected void writeRegionLine(BufferedWriter stream, Region r) throws IOException {
    stream.write(getOrderTranslation(EresseaConstants.OC_REGION));
    stream.write(" " + r.getID().toString(",") + " ");
    writeCommentLine(stream, " " + r.getName());
    writeCommentLine(stream, " " + getCheckerName() + " Lohn " + r.getWage());
  }

  protected boolean writeUnit(Unit unit, BufferedWriter stream) throws IOException {
    if (unit instanceof TempUnit)
      return false;

    for (OrderWriterPlugIn plugIn : plugins) {
      if (plugIn.ignoreUnit(unit))
        return false;
    }

    writeUnitLine(stream, unit);

    // confirmed?
    if (unit.isOrdersConfirmed() && !removeSCComments) {
      writeCommentLine(stream, OrderWriter.CONFIRMED);
    }

    writeOrders(unit.getCompleteOrders(writeUnitTagsAsVorlageComment), stream);

    return true;
  }

  protected void writeUnitLine(BufferedWriter stream, Unit unit) throws IOException {
    stream.write(getOrderTranslation(EresseaConstants.OC_UNIT) + " " + unit.getID().toString());

    if (addECheckComments) {
      int money = 0;
      // pavkovic 2004.06.28: now use modified item
      // unit.getRegion().refreshUnitRelations();
      // Item silver = unit.getModifiedItem(world.rules.getItemType(StringID.create("Silber"),
      // true));
      // pavkovic 2004.09.13: dont use modified items as it creates some bugs
      Item silver = unit.getItem(world.getRules().getItemType(EresseaConstants.I_USILVER));

      if (silver != null) {
        money = silver.getAmount();
      }

      stream.write(commentStart + "\t\t" + unit.getName() + " [" + unit.getPersons() + "," + money
          + "$");

      if (unit.getBuilding() != null) {
        if (unit.equals(unit.getBuilding().getOwnerUnit())) {
          ConstructibleType type = unit.getBuilding().getBuildingType();

          if (type != null) {
            Item i = type.getMaintenance(EresseaConstants.I_USILVER);

            if (i != null) {
              stream.write(",U" + i.getAmount());
            }
          }
        }
      }

      if (unit.getShip() != null) {
        if (unit.equals(unit.getShip().getOwnerUnit())) {
          stream.write(",S");
        } else {
          stream.write(",s");
        }

        stream.write(unit.getShip().getID().toString());
      }

      stream.write("]");
    }

    writeln(stream, null);
  }

  protected void writeOrders(Collection<Order> cmds, BufferedWriter stream) throws IOException {
    for (Order cmd : cmds) {
      if (!cmd.isEmpty()
          && ((removeSCComments && cmd.getToken(0).getText().startsWith(EresseaConstants.O_COMMENT))
              || (removeSSComments && cmd
                  .getToken(0).getText().startsWith(EresseaConstants.O_PCOMMENT)))) {
        // consume
      } else if (check(cmd)) {
        writeln(stream, cmd.getText());
      }
    }
  }

  protected boolean check(Order cmd) {
    return true;
  }

  protected void writeFooter(BufferedWriter stream) throws IOException {
    writeln(stream, getOrderTranslation(EresseaConstants.OC_NEXT));
  }

  protected Collection<Unit> filterUnits(Collection<Unit> units) {
    Collection<Unit> filteredUnits = new LinkedList<Unit>();

    for (Unit u : units) {
      if (filterUnit(u)) {
        filteredUnits.add(u);
      }
    }

    return filteredUnits;
  }

  protected boolean filterUnit(Unit u) {
    if (u.getFaction().equals(faction) && !u.isSpy()) {
      if (!confirmedOnly || u.isOrdersConfirmed()) {
        if ((group == null) || group.equals(u.getGroup()))
          return true;
      } else {
        /*
         * if this is a parent unit, it has to be added if one of it's children has unconfirmed
         * orders
         */
        if (confirmedOnly && !(u instanceof TempUnit) && !u.tempUnits().isEmpty()) {
          for (TempUnit tu : u.tempUnits()) {
            if (tu.isOrdersConfirmed())
              return true;
          }
        }
      }
    }

    return false;
  }

  protected String getTimeStamp() {
    long time = System.currentTimeMillis();
    int x = System.getProperties().getProperty("user.name").hashCode();
    int y = System.getProperties().getProperty("os.name").hashCode();
    int z = System.getProperties().getProperty("java.version").hashCode();
    long sum = x + y + z;
    String strSum = Long.toString(sum);
    String strTime = Long.toString(time);
    String strSumPart = strSum.substring(strSum.length() - 3);
    String strTimePart = strTime.substring(strTime.length() - 6, strTime.length() - 3);
    String rot = rotate(strSumPart, Integer.parseInt(strTimePart));
    StringBuffer mergeSB = new StringBuffer("");

    for (int i = 0; i < 3; i++) {
      mergeSB.append(((Integer.parseInt(rot.substring(i, i + 1)) + Integer.parseInt(strTimePart
          .substring(i, i + 1))) % 10));
    }

    int foo = Integer.parseInt(mergeSB.toString());
    String padded = ((foo < 100) ? "0" : "") + ((foo < 10) ? "0" : "") + foo;
    String res = strTime.substring(0, strTime.length() - 3) + padded;

    return res;
  }

  protected String rotate(String str, int amount) {
    char res[] = new char[str.length()];

    for (int i = 0; i < res.length; i++) {
      res[i] = str.charAt((i + amount) % res.length);
    }

    return new String(res);
  }

  protected void writeln(BufferedWriter stream, String text) throws IOException {
    if (text != null) {
      stream.write(text);
    }

    if (forceUnixLineBreaks) {
      stream.write('\n');
    } else {
      stream.newLine();
    }
  }

  protected void writeCommentLine(BufferedWriter stream, String line) throws IOException {
    writeln(stream, commentStart + line);
  }

  /**
   * Returns true if only confirmed units written.
   */
  public boolean getConfirmedOnly() {
    return confirmedOnly;
  }

  /**
   * If true, only confirmed units are written.
   */
  @Override
  public void setConfirmedOnly(boolean confirmedOnly) {
    this.confirmedOnly = confirmedOnly;
  }

  /**
   * Sets the set of regions to write.
   */
  @Override
  public void setRegions(Collection<Region> aRegions) {
    regions = aRegions;
  }

  /**
   * @return the writeTimeStamp
   */
  public boolean isWriteTimeStamp() {
    return writeTimeStamp;
  }

  /**
   * @param writeTimeStamp the writeTimeStamp to set
   */
  public void setWriteTimeStamp(boolean writeTimeStamp) {
    this.writeTimeStamp = writeTimeStamp;
  }

  @Override
  public void setGameData(GameData gameData) {
    world = gameData;
  }

  @Override
  public void setFaction(Faction selectedFaction) {
    faction = selectedFaction;
  }

  /**
   * @see magellan.library.gamebinding.GameSpecificOrderWriter#setECheckOptions(java.lang.String)
   */
  @Override
  public void setECheckOptions(String options) {
    if (options != null) {
      syntaxCheckOptions = options;
    } else {
      syntaxCheckOptions = getCheckerDefaultParameter();
    }
  }

  /**
   * Returns the value of syntaxCheckOptions.
   * 
   * @return Returns syntaxCheckOptions.
   */
  protected String getSyntaxCheckOptions() {
    if (faction != null && faction.getType() != null && faction.getRace().getRecruitmentCosts() > 0)
      return "-r" + faction.getRace().getRecruitmentCosts() + syntaxCheckOptions;
    else
      return syntaxCheckOptions;
  }

  /**
   * You can use this method to add a OrderWriterPlugIn to the internal list of known
   * OrderWriterPlugIns.
   */
  @Override
  public void addOrderWriterPlugin(OrderWriterPlugIn plugin) {
    if (!plugins.contains(plugin)) {
      plugins.add(plugin);
    }
  }

  /**
   * You can use this method to rmove a OrderWriterPlugIn from the internal list of known
   * OrderWriterPlugIns.
   */
  @Override
  public void removeOrderWriterPlugIn(OrderWriterPlugIn plugin) {
    if (plugins.contains(plugin)) {
      plugins.remove(plugin);
    }
  }

  public List<String> getErrors() {
    return errors;
  }

  protected void addError(String string) {
    errors.add(string);
  }

}
