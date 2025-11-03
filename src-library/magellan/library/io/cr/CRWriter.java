/*
 * Copyright (C) 2000-2004 Roger Butenuth, Andreas Gampe, Stefan Goetz, Sebastian Pappert, Klaas
 * Prause, Enno Rehling, Sebastian Tusk, Ulrich Kuester, Ilja Pavkovic This file is part of the
 * Eressea Java Code Base, see the file LICENSING for the licensing information applying to this
 * file.
 */

package magellan.library.io.cr;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;

import magellan.library.Addeable;
import magellan.library.Alliance;
import magellan.library.AllianceGroup;
import magellan.library.Battle;
import magellan.library.Bookmark;
import magellan.library.Border;
import magellan.library.Building;
import magellan.library.CombatSpell;
import magellan.library.CoordinateID;
import magellan.library.EntityID;
import magellan.library.Faction;
import magellan.library.GameData;
import magellan.library.Group;
import magellan.library.ID;
import magellan.library.IntegerID;
import magellan.library.Island;
import magellan.library.Item;
import magellan.library.LuxuryPrice;
import magellan.library.Message;
import magellan.library.Order;
import magellan.library.Potion;
import magellan.library.Region;
import magellan.library.RegionResource;
import magellan.library.Scheme;
import magellan.library.Selectable;
import magellan.library.Ship;
import magellan.library.Sign;
import magellan.library.Skill;
import magellan.library.Spell;
import magellan.library.TempUnit;
import magellan.library.Unit;
import magellan.library.io.file.FileType;
import magellan.library.rules.EresseaDate;
import magellan.library.rules.MessageType;
import magellan.library.rules.OptionCategory;
import magellan.library.rules.Options;
import magellan.library.rules.Race;
import magellan.library.rules.RegionType;
import magellan.library.rules.UnitContainerType;
import magellan.library.utils.CollectionFactory;
import magellan.library.utils.NullUserInterface;
import magellan.library.utils.Resources;
import magellan.library.utils.TranslationType;
import magellan.library.utils.Translations;
import magellan.library.utils.Umlaut;
import magellan.library.utils.UserInterface;
import magellan.library.utils.comparator.IDComparator;
import magellan.library.utils.comparator.SortIndexComparator;
import magellan.library.utils.logging.Logger;

/**
 * A class for writing game data, or parts of it, to a stream in the computer report format.
 * <p>
 * The generated output has platform dependent line breaks.
 * </p>
 * <p>
 * Implementation notes:
 * </p>
 * <p>
 * The basic mechanism of this class is to overload the <code>write()</code> method for most of the
 * Eressea base classes. Since many blocks and tags come in bunches there are helper functions for
 * each such sequence handling the collections in which the data objects are stored in.
 * </p>
 */
public class CRWriter extends BufferedWriter {
  private static final Logger log = Logger.getInstance(CRWriter.class);
  private boolean useTildesForQuotes = false;

  // incremented whenever a unit is written, can then be compared
  // to the total number of units in the game data
  private int unitsWritten = 0;

  private String encoding = FileType.DEFAULT_ENCODING.toString();

  private UserInterface ui = null;
  private boolean savingInProgress = false;
  private GameData world;
  private Map<Region, List<Unit>> oldUnitMap;

  /**
   * Creates a CR writer which uses the specified writer.
   *
   * @param out the writer used for output to.
   * @throws NullPointerException if data is null.
   */
  public CRWriter(GameData data, UserInterface ui, Writer out) {
    super(out);
    if (data == null)
      throw new NullPointerException("CRWriter.write(GameData): argument world is null");
    world = data;
    this.ui = ui;
    if (this.ui == null) {
      this.ui = new NullUserInterface();
    }
  }

  /**
   * Creates a CR writer that writes to the specified file.
   *
   * @param ui Interface for feedback. May be <code>null</code>.
   * @param fileType the filetype to write to
   * @param encoding The name of a supported {@link java.nio.charset.Charset </code>charset<code>}
   * @throws IOException If the file cannot be opened for writing.
   */
  public CRWriter(GameData data, UserInterface ui, FileType fileType, String encoding)
      throws IOException {
    super(fileType.createWriter(encoding));
    world = data;
    this.ui = ui;
    this.encoding = encoding;
    if (this.ui == null) {
      this.ui = new NullUserInterface();
    }
  }

  /**
   * Creates a CR writer that writes to the specified file.
   *
   * @param ui Interface for feedback. May be <code>null</code>.
   * @param fileType the filetype to write to
   * @param encoding The name of a supported {@link java.nio.charset.Charset </code>charset<code>}
   * @param numberOfBackups
   * @throws IOException If the file cannot be opened for writing.
   */
  public CRWriter(GameData data, UserInterface ui, FileType fileType, String encoding,
      int numberOfBackups) throws IOException {
    super(fileType.createWriter(encoding, numberOfBackups));
    world = data;
    this.ui = ui;
    this.encoding = encoding;
    if (this.ui == null) {
      this.ui = new NullUserInterface();
    }
  }

  /**
   * Escape quotation marks in <kbd>text</kbd> with a backslash.
   *
   * @param text the string to be modified.
   * @return the resulting string with escaped quotation marks.
   */
  private String escapeQuotes(String text) {
    if (text == null) {
      CRWriter.log.warn("CRWriter.escapeQuotes(): argument 'text' is null");
      return null;
    }
    return text.replace("\\", "\\\\").replace("\"", "\\\"");
  }

  /**
   * Remove quotes from text and replace blanks whithin the quoted section with ~ characters. ("a
   * \"b c\"" &rarr; "a b~c")
   *
   * @param text the string to be modified.
   * @return the resulting string.
   */
  private String tildeQuotes(String text) {
    if (text == null) {
      CRWriter.log.warn("CRWriter.tildeQuotes(): argument 'text' is null");

      return null;
    }

    StringBuffer sb = new StringBuffer(text.length() + 2);
    boolean replace = false;

    for (int i = 0; i < text.length(); i++) {
      char c = text.charAt(i);

      if (c == '"') {
        replace = !replace;
      } else if ((c == ' ') && replace) {
        sb.append('~');
      } else {
        sb.append(c);
      }
    }

    return sb.toString();
  }

  /**
   * Write the String <kbd>str</kbd> quoted to the underlying stream. If a part of <kbd>str</kbd> is
   * quoted, its quotes are escaped according to the current quote escape setting.
   *
   * @throws IOException If an I/O error occurs.
   */
  private void writeQuotedString(String str) throws IOException {
    if (str == null) {
      CRWriter.log.warn("CRWriter.writeQuotedString(): argument str is null");

      return;
    }

    boolean repairString = false;

    if (str.indexOf('\n') != -1) {
      repairString = true;
      CRWriter.log
          .warn("CRWriter.writeQuotedString(): argument str contains \'\\n\'. Splitting line.");
    }

    if (str.indexOf('\r') != -1) {
      repairString = true;
      CRWriter.log
          .warn("CRWriter.writeQuotedString(): argument str contains \'\\r\'. Splitting line.");
    }

    if (repairString) {
      // 2002.04.05 pavkovic: It seems that where exist a string with "\r\n" inside
      // These will be written linewise
      StringTokenizer st = new StringTokenizer(str, "\n\r");

      while (st.hasMoreTokens()) {
        writeQuotedString(st.nextToken());
      }

      return;
    }

    if (useTildesForQuotes) {
      write("\"" + tildeQuotes(str) + "\"");
    } else {
      write("\"" + escapeQuotes(str) + "\"");
    }

    newLine();
  }

  /**
   * Write the String <kbd>str</kbd> quoted along with the specified tag to the underlying stream. If
   * a part of <kbd>str</kbd> is quoted, its quotes are escaped according to the current quote escape
   * setting. writeQuotedTag("a b", "tag") results in writing "\"a b\";tag\n" to the
   *
   * @param value the string that is to be put in quotes and written to the
   * @param key the tag to be written to the stream, separated from <kbd>str</kbd> by a semicolon.
   * @throws IOException If an I/O error occurs.
   */
  private void writeQuotedTag(String value, String key) throws IOException {
    if (value == null) {
      CRWriter.log.warn("CRWriter.writeQuotedTag(): argument str is null");

      return;
    }

    if (key == null) {
      CRWriter.log.warn("CRWriter.writeQuotedTag(): argument tag is null");

      return;
    }

    if (useTildesForQuotes) {
      write("\"" + tildeQuotes(value) + "\";" + key);
    } else {
      write("\"" + escapeQuotes(value) + "\";" + key);
    }

    newLine();
  }

  private void writeCoordinateTranslations(GameData data) throws IOException {
    for (Faction f : data.getFactions()) {
      EntityID fID = f.getID();
      Map<Integer, CoordinateID> map = data.getCoordinateTranslationMap(fID);
      if (map != null && !map.isEmpty()) {
        write("COORDTRANS " + (fID).intValue());
        newLine();
        for (CoordinateID t : map.values()) {
          write(t.toString(" ") + ";translation");
          newLine();
        }
      }
    }
  }

  /**
   * Write a sequence of message blocks to the underlying stream.
   *
   * @param list a list containing the <kbd>Message</kbd> objects to be written.
   * @throws IOException If an I/O error occurs.
   */
  public void writeMessages(List<Message> list) throws IOException {
    if (list == null)
      return;

    for (Message message : list) {
      writeMessage(message);
    }
  }

  /**
   * Write the cr representation of a <kbd>Message</kbd> object to the underlying stream.
   *
   * @throws IOException If an I/O error occurs.
   */
  public void writeMessage(Message msg) throws IOException {
    if (msg == null)
      return;

    write("MESSAGE " + msg.getID());
    newLine();

    if (msg.getMessageType() != null) {
      write(msg.getMessageType().getID() + ";type");
      newLine();
    }

    if (msg.getText() != null) {
      writeQuotedTag(msg.getText(), "rendered");
    }

    if (msg.isAcknowledged()) {
      write("1;toolacknowledged");
      newLine();
    }

    for (String key : msg.getAttributeKeys()) {
      String value = msg.getAttribute(key);

      try {
        Integer.parseInt(value);
        write(value + ";" + key);
        newLine();
      } catch (NumberFormatException e) {
        CoordinateID c = CoordinateID.parse(value, " ");

        if (c != null) {
          write(value + ";" + key);
          newLine();
        } else {
          writeQuotedTag(value, key);
        }
      }
    }
  }

  /**
   * Write the data as one block named <kbd>blockName</kbd> to the underlying stream. The data is
   * written as simple cr strings. The block name is only written if there is data to follow.
   *
   * @param blockName the name of the block to be written (can not be a block with an id).
   * @param data a collection containing <kbd>Message</kbd> objects.
   * @throws IOException If an I/O error occurs.
   */
  public void writeMessageBlock(String blockName, Collection<Message> data) throws IOException {
    if ((data == null) || data.isEmpty())
      return;

    write(blockName);
    newLine();
    writeMessageSequence(data);
  }

  /**
   * Write the data as one sequence of simple cr strings.
   *
   * @param data a collection containing <kbd>Message</kbd> objects.
   * @throws IOException If an I/O error occurs.
   */
  public void writeMessageSequence(Collection<Message> data) throws IOException {
    if ((data == null) || data.isEmpty())
      return;

    for (Message msg : data) {
      writeQuotedString(msg.getText());
    }
  }

  /**
   * Write a the data as one sequence of simple cr strings.
   *
   * @param data a collection containing <kbd>String</kbd> objects.
   * @throws IOException If an I/O error occurs.
   */
  public void writeStringSequence(Collection<String> data) throws IOException {
    if ((data == null) || data.isEmpty())
      return;

    for (String str : data) {
      writeQuotedString(str);
    }
  }

  /**
   * Write the data as one block named <kbd>blockName</kbd> to the underlying stream. The data is
   * written as simple cr strings. The block name is only written if there is data to follow.
   *
   * @param blockName the name of the block to be written (can not be a block with an id).
   * @param data a colleciton containing <kbd>String</kbd> objects.
   * @throws IOException If an I/O error occurs.
   */
  public void writeStringBlock(String blockName, Collection<String> data) throws IOException {
    if ((data == null) || data.isEmpty())
      return;

    write(blockName);
    newLine();
    writeStringSequence(data);
  }

  /**
   * Write the VERSION block for the specified game data to the underyling
   *
   * @param world
   * @throws IOException If an I/O error occurs.
   */
  public void writeVersion(GameData world) throws IOException {
    write("VERSION " + world.version);
    newLine();

    // The Echecker of German Atlantis has problems with the locale line
    // so we check the game name
    if (!world.getGameName().startsWith("GAV")) {
      writeQuotedTag(encoding, "charset");
      if (world.getLocale() != null) {
        writeQuotedTag(world.getLocale().toString(), "locale");
      }
    }
    if (world.noSkillPoints) {
      write("1;noskillpoints");
      newLine();
    }

    write((System.currentTimeMillis() / 1000) + ";date");
    newLine();

    // keep the game type, when writing a CR.
    writeQuotedTag(world.getGameName(), "Spiel");

    if (serverConformance) {
      writeQuotedTag("Standard", "Konfiguration");
    } else {
      writeQuotedTag("Java-Tools", "Konfiguration");
    }

    writeQuotedTag("Hex", "Koordinaten");

    // Tracking a bug
    String actGameName = world.getGameName().toLowerCase();
    if ((actGameName.indexOf("eressea") > -1 || actGameName.indexOf("vinyambar") > -1)
        && (world.base != 36)) {
      // this should not happen
      CRWriter.log
          .error("BASE ERROR !! report to write could have not base36 !! Changed to base36. (Was "
              + world.base + ")");
      world.base = 36;
    }
    write(world.base + ";Basis");
    newLine();
    write("1;Umlaute");
    newLine();

    if (!serverConformance && (world.getCurTempID() != -1)) {
      write(world.getCurTempID() + ";curTempID");

      /**
       * @see com.eressea.GameData#curTempID
       */
      newLine();
    }

    if (world.getDate() != null) {
      write(world.getDate().getDate() + ";Runde");
      newLine();
      if (world.getDate() instanceof EresseaDate) {
        write(((EresseaDate) world.getDate()).getEpoch() + ";Zeitalter");
        newLine();
      }
    }

    if (world.mailTo != null) {
      writeQuotedTag(world.mailTo, "mailto");
    }

    if (world.mailSubject != null) {
      writeQuotedTag(world.mailSubject, "mailcmd");
    }

    if (!serverConformance && world.getOwnerFaction() != null) {
      write(world.getOwnerFaction().intValue() + ";reportowner");
      newLine();
    }

    if (world.build != null) {
      writeQuotedTag(world.build, "Build");
    }

    if (world.maxUnits != -1) {
      write(world.maxUnits + ";max_units");
      newLine();
    }
  }

  /**
   * Write a spells (ZAUBER) block to the underlying stream.
   *
   * @param spells a collection containing the spells to write.
   * @throws IOException If an I/O error occurs.
   */
  public void writeSpells(Collection<Spell> spells) throws IOException {
    if (spells == null)
      return;

    for (Spell spell : spells) {
      write(spell);
    }
  }

  /**
   * Write the cr representation of a <kbd>Spell</kbd> object to the underlying stream.
   *
   * @throws IOException If an I/O error occurs.
   */
  public void write(Spell spell) throws IOException {
    if (spell.getBlockID() == -1)
      return;

    write("ZAUBER " + spell.getBlockID());
    newLine();

    if (spell.getName() != null) {
      writeQuotedTag(spell.getName(), "name");
    }

    write(spell.getLevel() + ";level");
    newLine();
    write(spell.getRank() + ";rank");
    newLine();

    if (spell.getDescription() != null) {
      writeQuotedTag(spell.getDescription(), "info");
    }

    if (spell.getType() != null) {
      writeQuotedTag(spell.getType(), "class");
    }

    if (spell.getOnOcean()) {
      write("1;ocean");
      newLine();
    }

    if (spell.getOnShip()) {
      write("1;ship");
      newLine();
    }

    if (spell.getIsFar()) {
      write("1;far");
      newLine();
    }

    if (spell.getIsFamiliar()) {
      write("1;familiar");
      newLine();
    }

    if (spell.getSyntax() != null) {
      writeQuotedTag(spell.getSyntax(), "syntax");
    }

    if (!serverConformance && exportHotspots) {
      writeBookmark(spell);
    }

    writeSpellComponents(spell.getComponents());
  }

  /**
   * Write a sequence of potion (TRANK) blocks to the underlying stream.
   *
   * @param potions a collections containing the potions to write.
   * @throws IOException If an I/O error occurs.
   */
  public void writePotions(Collection<Potion> potions) throws IOException {
    if (potions == null)
      return;

    for (Potion potion : potions) {
      write(potion);
    }
  }

  /**
   * Write the cr representation of a <kbd>Potion</kbd> object to the underlying stream.
   *
   * @throws IOException If an I/O error occurs.
   */
  public void write(Potion potion) throws IOException {
    write("TRANK " + potion.getID().toString());
    newLine();

    if (potion.getName() != null) {
      writeQuotedTag(potion.getName(), "Name");
    }

    write(potion.getLevel() + ";Stufe");
    newLine();
    writeQuotedTag(potion.getDescription(), "Beschr");
    writePotionIngredients(potion.ingredients());
  }

  /**
   * Writes the ingredients of a potion as a ZUTATEN block to the underlying stream.
   *
   * @param ingredients a collection containing Item objects.
   * @throws IOException If an I/O error occurs.
   */
  public void writePotionIngredients(Collection<Item> ingredients) throws IOException {
    if (!ingredients.isEmpty()) {
      write("ZUTATEN");
      newLine();

      for (Item i : ingredients) {
        writeQuotedString(i.getItemType().getID().toString());
      }
    }
  }

  /**
   * Write a spell components (KOMPONENTEN) block to the underyling. The block name is only written,
   * if there are components in <kbd>comps</kbd>.
   *
   * @param comps a map containing the components to be written. The map is expected to contain the
   *          names of the components as keys and the component data as values (both as
   *          <kbd>String</kbd> objects). Such a map can be found in the <kbd>Spell</kbd> class.
   * @throws IOException If an I/O error occurs.
   * @see magellan.library.Spell
   */
  public void writeSpellComponents(Map<String, String> comps) throws IOException {
    if (comps == null)
      return;

    Iterator<String> iter = comps.keySet().iterator();

    if (iter.hasNext()) {
      write("KOMPONENTEN");
      newLine();
    }

    while (iter.hasNext()) {
      String key = iter.next();
      String value = comps.get(key);

      try {
        Integer.parseInt(value);
        write(value + ";" + key);
        newLine();
      } catch (NumberFormatException e) {
        writeQuotedTag(value, key);
      }
    }
  }

  /**
   * Write the cr representation of a <kbd>Option</kbd> object to the underlying stream.
   *
   * @throws IOException If an I/O error occurs.
   */
  public void write(Options options) throws IOException {
    write("OPTIONEN");
    newLine();

    for (OptionCategory o : options.options()) {
      write((o.isActive() ? "1" : "0") + ";" + o.getID().toString());
      newLine();
    }
  }

  /**
   * Write a sequence of group (GRUPPE) blocks to the underlying stream.
   *
   * @param map a map containing the groups to write. The keys are expected to be <kbd>Integer</kbd>
   *          objects containing the ids of the alliances. The values are expected to be instances
   *          of class <kbd>Group</kbd>. May be <code>null</code>.
   * @throws IOException If an I/O error occurs.
   */
  public void writeGroups(Map<? extends ID, Group> map) throws IOException {
    if (map == null)
      return;

    for (Group group : map.values()) {
      writeGroup(group);
    }
  }

  /**
   * Write the cr representation of a <kbd>Group</kbd> object to the underlying stream.
   *
   * @throws IOException If an I/O error occurs.
   */
  public void writeGroup(Group group) throws IOException {
    if (group == null)
      return;

    write("GRUPPE " + group.getID());
    newLine();

    if (group.getName() != null) {
      writeQuotedTag(group.getName(), "name");
    }

    if (group.getRaceNamePrefix() != null) {
      writeQuotedTag(group.getRaceNamePrefix(), "typprefix");
    }

    writeAlliances(group.allies());

    if (!serverConformance) {
      writeAttributes(group);
    }

  }

  /**
   * Write a sequence of alliance (ALLIANZ) blocks to the underlying stream.
   *
   * @param map a map containing the alliances to write. The keys are expected to be
   *          <kbd>Integer</kbd> objects containing the ids of the alliances. The values are expected
   *          to be instances of class <kbd>Alliance</kbd>. May be <code>null</code>.
   * @throws IOException If an I/O error occurs.
   */
  public void writeAlliances(Map<EntityID, Alliance> map) throws IOException {
    if (map == null)
      return;

    for (Alliance alliance : map.values()) {
      write(alliance);
    }
  }

  /**
   * Write the cr representation of an <kbd>Alliance</kbd> object to the underlying stream.
   *
   * @throws IOException If an I/O error occurs.
   */
  public void write(Alliance alliance) throws IOException {
    if (alliance == null)
      return;

    Faction f = alliance.getFaction();
    write("ALLIANZ " + (f.getID()).intValue());
    newLine();

    if (f.getName() != null) {
      writeQuotedTag(f.getName(), "Parteiname");
    }

    write(alliance.getState() + ";Status");
    newLine();
  }

  /**
   * Write a sequence of battle (BATTLE) blocks to the underlying stream.
   *
   * @param list a list containing the <kbd>Battle</kbd> objects to be written. My be
   *          <code>null</code>.
   * @throws IOException If an I/O error occurs.
   */
  public void writeBattles(List<Battle> list) throws IOException {
    if (list == null)
      return;

    for (Battle battle : list) {
      write(battle);
    }
  }

  /**
   * Write the cr representation of a <kbd>Battle</kbd> object to the underlying stream.
   *
   * @throws IOException If an I/O error occurs.
   */
  public void write(Battle battle) throws IOException {
    if (battle == null)
      return;

    if (!battle.isBattleSpec()) {
      write("BATTLE " + battle.getID().toString(" "));
    } else {
      write("BATTLESPEC " + battle.getID().toString(" "));
    }

    newLine();
    writeMessages(battle.messages());
  }

  protected void writeBookmark(Selectable selection) throws IOException {
    Bookmark bookmark = world.getBookmark(selection);
    if (bookmark != null) {
      write("1;bookmark");
      newLine();
      if (bookmark.getName() != null) {
        writeQuotedTag(bookmark.getName(), "bookmarkname");
      }
    }
  }

  /**
   * Write ALLIANCE blocks to the underlying stream.
   *
   * @param allianceGroups
   * @throws IOException
   */
  private void writeAlliances(Collection<AllianceGroup> allianceGroups) throws IOException {
    for (AllianceGroup alliance : allianceGroups) {
      write("ALLIANCE " + alliance.getID().intValue());
      newLine();
      write(alliance.getLeader().intValue() + ";leader");
      newLine();
      if (alliance.getName() != null) {
        writeQuotedTag(alliance.getName(), "name");
      }
    }
  }

  /**
   * Write a sequence of faction (PARTEI) blocks to the underlying stream.
   *
   * @param factions The Collection of factions to be written.
   * @throws IOException If an I/O error occurs.
   */
  public void writeFactions(Collection<Faction> factions, boolean noDetails) throws IOException {
    if (factions == null)
      return;

    // write owner first
    Faction ownerFaction = getOwnerFaction(factions);
    if (ownerFaction != null) {
      writeFaction(ownerFaction, noDetails);
    }
    List<Faction> sorted = new ArrayList<Faction>(factions);
    Comparator<Faction> sortIndexComparator =
        new SortIndexComparator<Faction>(IDComparator.DEFAULT);
    Collections.sort(sorted, sortIndexComparator);

    // write other factions
    for (Faction f : sorted) {
      if (ownerFaction == null || !f.equals(ownerFaction)) {
        writeFaction(f, noDetails);
      }
    }
  }

  /**
   * Write the cr representation of a <kbd>Faction</kbd> object to the underlying stream.
   *
   * @throws IOException If an I/O error occurs.
   */
  public void writeFaction(Faction faction, boolean noDetails) throws IOException {
    if ((faction.getID()).intValue() == -1)
      return;

    write("PARTEI " + (faction.getID()).intValue());
    newLine();

    // if (faction.password != null) {
    // writeQuotedTag(faction.password, "Passwort");
    // }
    if (faction.getLocale() != null) {
      writeQuotedTag(faction.getLocale().toString(), "locale");
    }

    if (faction.getOptions() != null) {
      write(faction.getOptions().getBitMap() + ";Optionen");
      newLine();
    }

    if (!noDetails) {
      if (faction.getScore() != -1) {
        write(faction.getScore() + ";Punkte");
        newLine();
      }

      if (faction.getAverageScore() != -1) {
        write(faction.getAverageScore() + ";Punktedurchschnitt");
        newLine();
      }

      if (getIncludeUnitDetails() && faction.getAlliance() != null) {
        write(faction.getAlliance().getID().intValue() + ";alliance");
        newLine();
      }

      Race race = faction.getRace();

      if (race != null) {
        if (race.toString() != null) {
          writeQuotedTag(race.getID().toString(), "Typ");
        }

        if (race.getRecruitmentCosts() != -1) {
          write(race.getRecruitmentCosts() + ";Rekrutierungskosten");
          newLine();
        }
      }

      if (faction.getPersons() != -1) {
        write(faction.getPersons() + ";Anzahl Personen");
        newLine();
      }

      if (faction.getMigrants() != -1) {
        write(faction.getMigrants() + ";Anzahl Immigranten");
        newLine();
      }

      if (faction.getHeroes() != -1) {
        write(faction.getHeroes() + ";heroes");
        newLine();
      }

      if (faction.getMaxHeroes() != -1) {
        write(faction.getMaxHeroes() + ";max_Heroes");
        newLine();
      }

      if (faction.getAge() != -1) {
        write(faction.getAge() + ";age");
        newLine();
      }

      if (faction.getMaxMigrants() != -1) {
        write(faction.getMaxMigrants() + ";Max. Immigranten");
        newLine();
      }

      if (faction.getSpellSchool() != null) {
        writeQuotedTag(faction.getSpellSchool(), "Magiegebiet");
      }
    }

    if (faction.getName() != null) {
      writeQuotedTag(faction.getName(), "Parteiname");
    }

    if (faction.getEmail() != null) {
      writeQuotedTag(faction.getEmail(), "email");
    }

    if (faction.getDescription() != null) {
      writeQuotedTag(faction.getDescription(), "banner");
    }

    if (!noDetails) {
      if (faction.getRaceNamePrefix() != null) {
        writeQuotedTag(faction.getRaceNamePrefix(), "typprefix");
      }

      if (faction.getTreasury() != 0) {
        write(faction.getTreasury() + ";Schatz");
        newLine();
      }

      if (!serverConformance && faction.isTrustLevelSetByUser()) {
        write(faction.getTrustLevel() + ";trustlevel");
        newLine();
      }

      writeItems(faction.getItems().iterator());

      if (faction.getOptions() != null) {
        write(faction.getOptions());
      }

      writeAlliances(faction.getAllies());
      writeGroups(faction.getGroups());

      if (includeMessages) {
        writeStringBlock("FEHLER", faction.getErrors());
        writeMessages(faction.getMessages());
        writeBattles(faction.getBattles());

        if (!serverConformance) {
          writeStringBlock("COMMENTS", faction.getComments());
          writeAttributes(faction);
        }
      }
    }
  }

  /**
   * Write a sequence of ship (SCHIFF) blocks to the underlying stream.
   *
   * @param ships an iterator containing the<kbd>Ship</kbd> objects to be written.
   * @throws IOException If an I/O error occurs.
   */
  public void writeShips(Collection<Ship> ships) throws IOException {
    if (ships == null)
      return;

    List<Ship> sorted = new ArrayList<Ship>(ships);
    Comparator<Ship> sortIndexComparator = new SortIndexComparator<Ship>(IDComparator.DEFAULT);
    Collections.sort(sorted, sortIndexComparator);

    for (Ship ship : sorted) {
      writeShip(ship);
    }
  }

  /**
   * Write the cr representation of a <kbd>Ship</kbd> object to the underlying stream.
   *
   * @throws IOException If an I/O error occurs.
   */
  public void writeShip(Ship ship) throws IOException {
    write("SCHIFF " + (ship.getID()).intValue());
    newLine();

    if (ship.getName() != null) {
      writeQuotedTag(ship.getName(), "Name");
    }

    if (ship.getDescription() != null) {
      writeQuotedTag(ship.getDescription(), "Beschr");
    }

    UnitContainerType type = ship.getType();

    if (type != null) {
      writeQuotedTag(type.getID().toString(), "Typ");
    }

    if (ship.getAmount() != 1) {
      write(ship.getAmount() + ";Anzahl");
      newLine();
    }

    if (ship.getDamageRatio() > 0) {
      write(ship.getDamageRatio() + ";Schaden");
      newLine();
    }

    if (ship.getSize() != -1) {
      write(ship.getSize() + ";Groesse");
      newLine();
    }

    if (ship.getShoreId() != -1) {
      write(ship.getShoreId() + ";Kueste");
      newLine();
    }

    if (ship.getOwner() != null && shallExportUnit(ship.getOwner())) {
      write((ship.getOwner().getID()).intValue() + ";Kapitaen");
      newLine();

      if (ship.getOwner().getFaction() != null) {
        write((ship.getOwner().getFaction().getID()).intValue() + ";Partei");
        newLine();
      }
    }

    if (ship.getCargo() != -1) {
      write(ship.getCargo() + ";cargo");
      newLine();
    }

    if (ship.getCapacity() != -1) {
      write(ship.getCapacity() + ";capacity");
      newLine();
    }

    if (ship.getSpeed() != -1) {
      write(ship.getSpeed() + ";speed");
      newLine();
    }

    if (ship.getDeprecatedLoad() != -1) {
      write(ship.getDeprecatedLoad() + ";Ladung");
      newLine();
    }

    if (ship.getDeprecatedCapacity() != -1) {
      write(ship.getDeprecatedCapacity() + ";MaxLadung");
      newLine();
    }

    // ships cannot be besieged...
    // if (ship.getBesiegers() > 0) {
    // write(ship.getBesiegers() + ";Belagerer");
    // newLine();
    // }

    if (!serverConformance && exportHotspots) {
      writeBookmark(ship);
      writeAttributes(ship);
    }

    if (includeMessages) {
      writeStringBlock("EFFECTS", ship.getEffects());

      if (!serverConformance) {
        writeStringBlock("COMMENTS", ship.getComments());
      }
    }
  }

  /**
   * Write a sequence of building (BURG) blocks to the underlying stream.
   *
   * @param buildings an iterator containing the<kbd>Building</kbd> objects to be written.
   * @throws IOException
   */
  public void writeBuildings(Collection<Building> buildings) throws IOException {
    if (buildings == null)
      return;

    List<Building> sorted = new ArrayList<Building>(buildings);
    Comparator<Building> sortIndexComparator =
        new SortIndexComparator<Building>(IDComparator.DEFAULT);
    Collections.sort(sorted, sortIndexComparator);

    for (Building building : sorted) {
      writeBuilding(building);
    }
  }

  /**
   * Write the cr representation of a <kbd>Building</kbd> object to the underlying stream.
   *
   * @throws IOException If an I/O error occurs.
   */
  public void writeBuilding(Building building) throws IOException {
    if (building == null)
      return;

    UnitContainerType type = building.getType();
    write("BURG " + (building.getID()).intValue());
    newLine();

    if (type != null) {
      writeQuotedTag(type.getID().toString(), "Typ");
    }

    if (building.getName() != null) {
      writeQuotedTag(building.getName(), "Name");
    }

    if (building.getDescription() != null) {
      writeQuotedTag(building.getDescription(), "Beschr");
    }

    if (building.getSize() > 0) {
      write(building.getSize() + ";Groesse");
      newLine();
    }

    if (building.getOwner() != null && shallExportUnit(building.getOwner())) {
      write((building.getOwner().getID()).intValue() + ";Besitzer");
      newLine();

      if (building.getOwner().getFaction() != null) {
        write((building.getOwner().getFaction().getID()).intValue() + ";Partei");
        newLine();
      }
    }

    if (building.getCost() > 0) {
      write(building.getCost() + ";Unterhalt");
      newLine();
    }

    if (building.getBesiegers() > 0) {
      write(building.getBesiegers() + ";Belagerer");
      newLine();
    }

    if (!serverConformance && exportHotspots) {
      writeBookmark(building);
      writeAttributes(building);
    }

    if (includeMessages) {
      writeStringBlock("EFFECTS", building.getEffects());

      if (!serverConformance) {
        writeStringBlock("COMMENTS", building.getComments());
      }
    }
  }

  /**
   * Write a skills (TALENTE) block to the underlying stream. The block is only written, if
   * <kbd>skills</kbd> contains at least one <kbd>Skill</kbd> object.
   *
   * @param skills an iterator over the <kbd>Skill</kbd> objects to write.
   * @param persons the number of persons in the unit this skill belongs to.
   * @throws IOException If an I/O error occurs.
   */
  public void writeSkills(Iterator<Skill> skills, int persons) throws IOException {
    if (skills.hasNext()) {
      write("TALENTE");
      newLine();
    }

    while (skills.hasNext()) {
      writeSkill(skills.next(), persons);
    }
  }

  /**
   * Write the cr representation of a <kbd>Skill</kbd> object to the underlying stream.
   *
   * @throws IOException If an I/O error occurs.
   */
  public void writeSkill(Skill skill, int persons) throws IOException {
    write(skill.getPoints() + " " + skill.getRealLevel());

    if (!getServerConformance() && skill.isLevelChanged()) {
      write(" " + skill.getChangeLevel());
    }

    write(";" + skill.getSkillType().getID());

    newLine();
  }

  /**
   * Write a COMMANDS block to the underlying stream. The block is only written, if <kbd>list</kbd>
   * contains at least one <kbd>String</kbd> object representing an order.
   *
   * @param list a list with the <kbd>String</kbd> objects to be written.
   * @throws IOException If an I/O error occurs.
   */
  public void writeOrders(List<Order> list) throws IOException {
    if ((list == null) || list.isEmpty())
      return;

    write("COMMANDS");
    newLine();

    for (Order line : list) {
      writeQuotedString(line.getText());
    }
  }

  /**
   * Write a unit's spell (SPRUECHE) block to the underlying stream. The block is only written, if
   * <kbd>list</kbd> contains at least one <kbd>Spell</kbd> object.
   *
   * @param spells a list with the<kbd>Spell</kbd> object names to be written.
   * @throws IOException If an I/O error occurs.
   */
  public void writeUnitSpells(Map<? extends ID, Spell> spells) throws IOException {
    if (spells == null)
      return;

    Iterator<Spell> i = spells.values().iterator();

    if (i.hasNext()) {
      write("SPRUECHE");
      newLine();
    }

    while (i.hasNext()) {
      Spell s = i.next();
      writeQuotedString(s.getName());
    }
  }

  /**
   * Write a unit's combat spell (KAMPFZAUBER) blocks to the underlying stream.
   *
   * @param map a Map with the <kbd>CombatSpell</kbd> objects to be written.
   * @throws IOException If an I/O error occurs.
   */
  public void writeUnitCombatSpells(Map<? extends ID, CombatSpell> map) throws IOException {
    if (map == null)
      return;

    for (CombatSpell cs : map.values()) {
      write(cs);
    }
  }

  /**
   * Write the cr representation of a <kbd>CombatSpell</kbd> object to the underlying stream.
   *
   * @throws IOException If an I/O error occurs.
   */
  public void write(CombatSpell cs) throws IOException {
    if (cs != null) {
      if (cs.getID() != null) {
        write("KAMPFZAUBER " + cs.getID().toString());
        newLine();

        if (cs.getSpell() != null) {
          if (cs.getSpell().getName() != null) {
            writeQuotedTag(cs.getSpell().getName(), "name");
          } else {
            CRWriter.log.warn("CRWriter.write(CombatSpell): warning: spell name is null!");
          }
        } else {
          CRWriter.log.warn("CRWriter.write(CombatSpell): warning: spell is null!");
        }

        write(cs.getCastingLevel() + ";level");
        newLine();
      } else {
        CRWriter.log.warn("CRWriter.write(CombatSpell): warning: combat spell ID is null!");
      }
    } else {
      CRWriter.log.warn("CRWriter.write(CombatSpell): warning: combat spell is null!");
    }
  }

  /**
   * Write a unit's items (GEGENSTAENDE) block to the underlying stream. The block is only written,
   * if <kbd>items</kbd> contains at least one <kbd>Item</kbd> object.
   *
   * @param items an iterator over the <kbd>Item</kbd> objects to be written.
   * @throws IOException If an I/O error occurs.
   */
  public void writeItems(Iterator<Item> items) throws IOException {
    if (items.hasNext()) {
      write("GEGENSTAENDE");
      newLine();
    }

    while (items.hasNext()) {
      Item item = items.next();
      write(item);
    }
  }

  /**
   * Write the cr representation of a <kbd>Item</kbd> object to the underlying stream.
   *
   * @throws IOException If an I/O error occurs.
   */
  public void write(Item item) throws IOException {
    write(item.getAmount() + ";" + item.getItemType().getID());
    newLine();
  }

  /**
   * Write a sequence of unit (EINHEIT) blocks to the underlying stream.
   *
   * @param units an iterator for the<kbd>Unit</kbd> objects to be written.
   * @throws IOException If an I/O error occurs.
   */
  public void writeUnits(Collection<Unit> units) throws IOException {
    if (units == null)
      return;

    List<Unit> sorted = new ArrayList<Unit>(units);
    Comparator<Unit> sortIndexComparator = new SortIndexComparator<Unit>(IDComparator.DEFAULT);
    Collections.sort(sorted, sortIndexComparator);

    for (Unit u : sorted) {
      writeUnit(u);
    }
  }

  /**
   * Write a sequence of old unit (ALTEINHEIT) blocks for the given region to the underlying stream.
   *
   * @throws IOException If an I/O error occurs.
   */
  public void writeOldUnits(Map<Region, List<Unit>> oldUnitMap2, Region region) throws IOException {
    if (oldUnitMap2 != null) {
      if (oldUnitMap2.get(region) != null) {
        for (Unit u : oldUnitMap2.get(region)) {
          writeUnit(u, true);
        }
      }
    }
  }

  /**
   * @param u the unit to export
   * @return true iff units == null or empty or units contains u
   */
  private boolean shallExportUnit(Unit u) {
    return u != null && (units == null || units.isEmpty() || units.contains(u));
  }

  /**
   * Write the cr representation of a <kbd>Unit</kbd> object to the underyling
   *
   * @throws IOException If an I/O error occurs.
   */
  public void writeUnit(Unit unit) throws IOException {
    writeUnit(unit, false);
  }

  /**
   * Write the cr representation of a <kbd>Unit</kbd> object to the underyling
   *
   * @throws IOException If an I/O error occurs.
   */
  public void writeUnit(Unit unit, boolean old) throws IOException {
    if (unit instanceof TempUnit || !shallExportUnit(unit))
      return;

    unitsWritten++;
    if (old) {
      write("ALTEINHEIT " + (unit.getID()).intValue());
    } else {
      write("EINHEIT " + (unit.getID()).intValue());
    }
    newLine();

    if (unit.getName() != null) {
      writeQuotedTag(unit.getName(), "Name");
    }

    if (unit.getDescription() != null) {
      writeQuotedTag(unit.getDescription(), "Beschr");
    }

    if (getIncludeUnitDetails() && unit.getPrivDesc() != null) {
      writeQuotedTag(unit.getPrivDesc(), "privat");
    }

    if (unit.getFaction() != null) {
      int id = (unit.getFaction().getID()).intValue();

      if (id != -1) {
        write(id + ";Partei");
        newLine();
      }
    }

    write(unit.getPersons() + ";Anzahl");
    newLine();

    if (getIncludeUnitDetails() && unit.getDisguiseRace() != null) {
      write("\"" + unit.getDisguiseRace().getID().toString() + "\";Typ");
      newLine();
      write("\"" + unit.getRace().getID().toString() + "\";wahrerTyp");
      newLine();
    } else {
      write("\"" + unit.getRace().getID().toString() + "\";Typ");
      newLine();
    }

    if (unit.getTempID() != null) {
      write(unit.getTempID().intValue() + ";temp");
      newLine();
    }

    if (unit.getAlias() != null) {
      write(unit.getAlias().intValue() + ";alias");
      newLine();
    }

    if (getIncludeUnitDetails() && unit.getCombatStatus() != -1) {
      write(unit.getCombatStatus() + ";Kampfstatus");
      newLine();
    }

    if (getIncludeUnitDetails() && unit.isUnaided()) {
      write("1;unaided");
      newLine();
    }

    if (getIncludeUnitDetails() && unit.getStealth() != -1) {
      write(unit.getStealth() + ";Tarnung");
      newLine();
    }

    if (unit.getShip() != null && includeShips) {
      write((unit.getShip().getID()).intValue() + ";Schiff");
      newLine();
    }

    if (unit.getBuilding() != null && includeBuildings) {
      write((unit.getBuilding().getID()).intValue() + ";Burg");
      newLine();
    }

    // since CR Version 51 Silber is an normal item

    if (getIncludeUnitDetails() && unit.isHideFaction()) {
      write("1;Parteitarnung");
      newLine();
    }

    if (getIncludeUnitDetails() && shallExportUnit(unit.getFollows())) {
      write((unit.getFollows().getID()).intValue() + ";folgt");
      newLine();
    }

    if (unit.getGuard() != 0) {
      write(unit.getGuard() + ";bewacht");
      newLine();
    }

    if (getIncludeUnitDetails() && unit.getAura() != -1) {
      write(unit.getAura() + ";Aura");
      newLine();
    }

    if (getIncludeUnitDetails() && unit.getAuraMax() != -1) {
      write(unit.getAuraMax() + ";Auramax");
      newLine();
    }

    if (getIncludeUnitDetails() && unit.getHealth() != null) {
      writeQuotedTag(unit.getHealth(), "hp");
    }

    if (getIncludeUnitDetails() && unit.isHero()) {
      write("1;hero");
      newLine();
    }

    if (getIncludeUnitDetails() && unit.isStarving()) {
      write("1;hunger");
      newLine();
    }

    if (getIncludeUnitDetails() && !serverConformance && unit.isOrdersConfirmed()) {
      write("1;ejcOrdersConfirmed");
      newLine();
    }

    if (getIncludeUnitDetails() && unit.getGroup() != null) {
      write(unit.getGroup().getID() + ";gruppe");
      newLine();
    }

    if (unit.isSpy()) {
      write("1;Verraeter");
      newLine();
    }

    if (getIncludeUnitDetails() && unit.getGuiseFaction() != null) {
      // write(((IntegerID) unit.getGuiseFaction().getID()).intValue() + ";Verkleidung");
      // Anderepartei
      write(((IntegerID) unit.getGuiseFaction().getID()).intValue() + ";Anderepartei");
      newLine();
    }

    if (unit.isWeightWellKnown()) {
      write(unit.getSimpleWeight() + ";weight");
      newLine();
    }

    // fiete: familiarmage
    if (getIncludeUnitDetails() && unit.getFamiliarmageID() != null) {
      IntegerID iID = (IntegerID) unit.getFamiliarmageID();
      write(iID.intValue() + ";familiarmage");
      newLine();
    }

    if (unit.getRaceNamePrefix() != null) {
      writeQuotedTag(unit.getRaceNamePrefix(), "typprefix");
    }

    if (unit.getSiege() != null) {
      write(unit.getSiege().getID().intValue() + ";belagert");
      newLine();
    }

    if (!serverConformance && exportHotspots) {
      writeBookmark(unit);
    }

    if (getIncludeUnitDetails() && unit.hasTags()) {
      java.util.Map<String, String> map = unit.getTagMap();
      for (Object key : map.keySet()) {
        Object value = map.get(key);

        try {
          Integer.parseInt(value.toString());
          write(value + ";" + key);
          newLine();
        } catch (NumberFormatException e) {
          writeQuotedTag(value.toString(), key.toString());
        }
      }
    }

    if (getIncludeUnitDetails() && includeMessages) {
      writeStringBlock("EFFECTS", unit.getEffects());
      writeMessageBlock("EINHEITSBOTSCHAFTEN", unit.getUnitMessages());
      if (!serverConformance) {
        writeStringBlock("COMMENTS", unit.getComments());
      }
    }

    //
    // writeOrders(unit.orders);
    // writeStringSequence(unit.getTempOrders());
    if (getIncludeOrders()) {
      writeOrders(unit.getCompleteOrders());
    }
    if (getIncludeSkills()) {
      writeSkills(unit.getSkills().iterator(), unit.getPersons());
    }
    if (getIncludeUnitDetails()) {
      writeUnitSpells(unit.getSpells());
      writeUnitCombatSpells(unit.getCombatSpells());
    }
    if (getIncludeItems()) {
      writeItems(unit.getItems().iterator());
    }

    if (!serverConformance) {
      writeAttributes(unit);
    }

  }

  /**
   * Write a region prices (PREISE) block to the underlying stream.
   *
   * @param map list containing the<kbd>LuxuryPrice</kbd> objects to be written.
   * @throws IOException If an I/O error occurs.
   */
  public void writePrices(Map<? extends ID, LuxuryPrice> map) throws IOException {
    if (map == null)
      return;

    Iterator<LuxuryPrice> i = map.values().iterator();

    if (i.hasNext()) {
      write("PREISE");
      newLine();
    }

    while (i.hasNext()) {
      write(i.next());
    }
  }

  /**
   * Write region block containing the luxury prices of the last turn (LETZTEPREISE) to the
   * underlying stream.
   *
   * @param map a map containing the <kbd>LuxuryPrice</kbd> objects to be written.
   * @throws IOException If an I/O error occurs.
   */
  public void writeOldPrices(Map<? extends ID, LuxuryPrice> map) throws IOException {
    if (map == null)
      return;

    Iterator<LuxuryPrice> i = map.values().iterator();

    if (i.hasNext()) {
      write("LETZTEPREISE");
      newLine();
    }

    while (i.hasNext()) {
      write(i.next());
    }
  }

  /**
   * Write the cr representation of a <kbd>LuxuryPrice</kbd> object to the underlying stream.
   *
   * @throws IOException If an I/O error occurs.
   */
  public void write(LuxuryPrice price) throws IOException {
    write(price.getPrice() + ";" + price.getItemType().getID().toString());
    newLine();
  }

  /**
   * Write a sequence of region border (GRENZE) blocks to the underlying stream.
   *
   * @param c collection containing the <kbd>Border</kbd> objects to be written.
   * @throws IOException If an I/O error occurs.
   */
  public void writeBorders(Collection<Border> c) throws IOException {
    if (c == null)
      return;

    for (Border element : c) {
      writeBorder(element);
    }
  }

  /**
   * Write the cr representation of a <kbd>Border</kbd> object to the underlying stream.
   *
   * @throws IOException If an I/O error occurs.
   */
  public void writeBorder(Border border) throws IOException {
    write("GRENZE " + border.getID());
    newLine();
    writeQuotedTag(border.getType(), "typ");
    write(border.getDirection() + ";richtung");
    newLine();
    write(border.getBuildRatio() + ";prozent");
    newLine();
  }

  /**
   * Write a sequence of region (REGION) blocks to the underlying stream.
   *
   * @param regions a collection containing the regions to write.
   * @throws IOException If an I/O error occurs.
   */
  public void writeRegions(Collection<Region> regions) throws IOException {
    if (regions == null)
      return;

    ui.setMaximum(regions.size());
    int counter = 0;

    for (Region region : regions) {
      if (region.getName() != null) {
        ui.setProgress(Resources.get("crwriterdialog.progress.07a",
            new Object[] { region.getName() }), counter++);
      } else {
        ui.setProgress(Resources.get("crwriterdialog.progress.07"), counter++);
      }

      writeRegion(region);
    }
    ui.setMaximum(11);
    ui.setProgress(Resources.get("crwriterdialog.progress.07"), counter++);
  }

  /**
   * Write the cr representation of a <kbd>Region</kbd> object to the underlying stream.
   *
   * @throws IOException If an I/O error occurs.
   */
  public void writeRegion(Region region) throws IOException {

    // Fiete 20070117
    // Exception: Magellan-added Regions to show TheVoid
    // these regions should not be written
    if (region.getRegionType().equals(RegionType.theVoid))
      return;

    write("REGION " + region.getID().toString(" "));
    newLine();

    // Fiete: starting in round 570 we can have region.UID within
    // eressea, coming from the server.
    // if UID is known, write it now
    // UID=0 reserved for no UID.
    if (region.hasUID()) {
      // first example was quoted
      // writeQuotedTag(region.getUID() + "", "id");
      // finally we use not quoted IDs
      write(region.getUID() + ";id");
      newLine();
    }

    UnitContainerType type = region.getType();

    if ((region.getName() != null) && !region.getName().equals("")) {
      // write name only if it differs from type
      if (type != null) {
        String strRegion = Umlaut.normalize(region.getName());
        String strType = Umlaut.normalize(type.toString());

        if (!strRegion.equalsIgnoreCase(strType)) {
          writeQuotedTag(region.getName(), "Name");
        }
      }
    }

    if (type != null) {
      writeQuotedTag(type.getID().toString(), "Terrain");
    }

    if (region.getDescription() != null) {
      writeQuotedTag(region.getDescription(), "Beschr");
    }

    if (includeIslands && !serverConformance && (region.getIsland() != null)) {
      writeQuotedTag(region.getIsland().getID().toString(), "Insel");
    }

    if (!serverConformance && world.getSelectedRegionCoordinates().containsKey(region.getID())) {
      write("1;ejcIsSelected");
      newLine();
    }

    if (includeRegionDetails && region.getOwnerFaction() != null) {
      write(((IntegerID) region.getOwnerFaction().getID()).intValue() + ";owner");
      newLine();
    }

    if (region.hasTags()) {
      java.util.Map<String, String> map = region.getTagMap();
      for (Object key : map.keySet()) {
        Object value = map.get(key);

        try {
          Integer.parseInt(value.toString());
          write(value + ";" + key);
          newLine();
        } catch (NumberFormatException e) {
          writeQuotedTag(value.toString(), key.toString());
        }
      }
    }

    if (includeRegionDetails) {
      if (region.getTrees() >= 0) {
        write(region.getTrees() + ";Baeume");
        newLine();
      }

      if (region.isMallorn()) {
        write("1;Mallorn");
        newLine();
      }

      if (!serverConformance && (region.getOldTrees() > -1)) {
        write(region.getOldTrees() + ";letztebaeume");
        newLine();
      }

      if (region.getSprouts() >= 0) {
        write(region.getSprouts() + ";Schoesslinge");
        newLine();
      }

      if (!serverConformance && (region.getOldSprouts() > -1)) {
        write(region.getOldSprouts() + ";letzteSchoesslinge");
        newLine();
      }

      if (region.getPeasants() >= 0) {
        write(region.getPeasants() + ";Bauern");
        newLine();
      }

      if (!serverConformance && (region.getOldPeasants() > -1)) {
        write(region.getOldPeasants() + ";letztebauern");
        newLine();
      }

      if (region.getHorses() >= 0) {
        write(region.getHorses() + ";Pferde");
        newLine();
      }

      if (!serverConformance && (region.getOldHorses() > -1)) {
        write(region.getOldHorses() + ";letztepferde");
        newLine();
      }

      if (region.getSilver() >= 0) {
        write(region.getSilver() + ";Silber");
        newLine();
      }

      if (!serverConformance && (region.getOldSilver() > -1)) {
        write(region.getOldSilver() + ";letztessilber");
        newLine();
      }

      if (region.maxEntertain() > 0) {
        write(region.maxEntertain() + ";Unterh");
        newLine();
      }

      if (region.maxOldEntertain() > 0) {
        write(region.maxOldEntertain() + ";letzteunterh");
        newLine();
      }

      if (region.getRecruits() > 0) {
        write(region.getRecruits() + ";Rekruten");
        newLine();
      }

      // pavkovic 2002.05.10: recruits (and old recruits are used from cr)
      if (!serverConformance && (region.maxOldRecruit() > -1)) {
        write(region.maxOldRecruit() + ";letzterekruten");
        newLine();
      }

      if (!serverConformance && (region.maxOldLuxuries() > -1)) {
        write(region.maxOldLuxuries() + ";letzteluxus");
        newLine();
      }

      if (region.getWage() > 0) {
        if (includeBuildings) {
          write(region.getWage() + ";Lohn");
        } else {
          // FIXME(stm) game specific?
          write("10;Lohn");
        }

        newLine();
      }

      if (includeBuildings && !serverConformance && (region.getOldWage() > -1)) {
        write(region.getOldWage() + ";letzterlohn");
        newLine();
      }

      if (includeRegionDetails && region.getMorale() >= 0) {
        write(region.getMorale() + ";morale");
        newLine();
      }

      if (includeRegionDetails && region.getMourning() >= 0) {
        write(region.getMourning() + ";mourning");
        newLine();
      }

      if (region.getIron() > 0) {
        write(region.getIron() + ";Eisen");
        newLine();
      }

      if (!serverConformance && (region.getOldIron() > -1)) {
        write(region.getOldIron() + ";letzteseisen");
        newLine();
      }

      if (region.getLaen() > 0) {
        write(region.getLaen() + ";Laen");
        newLine();
      }

      if (!serverConformance && (region.getOldLaen() > -1)) {
        write(region.getOldLaen() + ";letzteslaen");
        newLine();
      }

      if (region.getStones() > 0) {
        write(region.getStones() + ";Steine");
        newLine();
      }

      if (!serverConformance && (region.getOldStones() > -1)) {
        write(region.getOldStones() + ";letztesteine");
        newLine();
      }

      if ((region.getHerb() != null) && !serverConformance) {
        writeQuotedTag(region.getHerb().getID().toString(), "herb");
      }

      if ((region.getHerbAmount() != null) && !serverConformance) {
        writeQuotedTag(region.getHerbAmount(), "herbamount");
      }

      if (region.isOrcInfested()) {
        write("1;Verorkt");
        newLine();
      }

      if (!serverConformance && world.getActiveRegion() == region) {
        write("1;aktiveRegion");
        newLine();
      }

      if (region.getVisibilityString() != null) {
        writeQuotedTag(region.getVisibilityString(), "visibility");
      }

      if (!serverConformance && exportHotspots) {
        writeBookmark(region);
      }

      writeRegionResources(region.resources());
      writePrices(region.getPrices());

      if (!serverConformance && (region.getOldPrices() != null)) {
        writeOldPrices(region.getOldPrices());
      }

      if (!serverConformance && (region.getSigns() != null)) {
        writeSigns(region.getSigns());
      }

      writeBorders(region.borders());

      if (!serverConformance) {
        writeAttributes(region);
      }

      if (includeMessages) {
        writeStringBlock("EFFECTS", region.getEffects());

        if (!serverConformance) {
          writeStringBlock("COMMENTS", region.getComments());
        }

        writeMessageBlock("REGIONSEREIGNISSE", region.getEvents());

        // writeMessageBlock("REGIONSKOMMENTAR", region.comments);
        writeMessageBlock("REGIONSBOTSCHAFTEN", region.getPlayerMessages());
        writeMessageBlock("UMGEBUNG", region.getSurroundings());
        writeMessageBlock("DURCHREISE", region.getTravelThru());
        writeMessageBlock("DURCHSCHIFFUNG", region.getTravelThruShips());
        writeMessages(region.getMessages());
      }
      writeItems(region.getItems().iterator());
    }

    writeSchemes(region.schemes());

    if (includeBuildings) {
      writeBuildings(region.buildings());
    }

    if (includeShips) {
      writeShips(region.ships());
    }

    if (includeUnits) {
      writeUnits(region.units());
      writeOldUnits(oldUnitMap, region);
    }
  }

  /**
   * Write a collection of signs to the underlying stream
   *
   * @param signs Collection of signs
   * @throws IOException passes a IOException from streamwriter
   */
  private void writeSigns(Collection<Sign> signs) throws IOException {
    if (signs == null || signs.isEmpty())
      return;
    int counter = 1;
    for (Sign sign : signs) {
      writeSign(sign, counter);
      counter++;
    }
  }

  /**
   * Write a presentation of a sign to the underlying stream
   *
   * @param s the sign
   * @param counter just a counter for IDing the sign
   * @throws IOException passes a IOException from streamwriter
   */
  private void writeSign(Sign s, int counter) throws IOException {
    write("SIGN " + counter);
    newLine();
    writeQuotedTag(s.getText(), "text");
  }

  /**
   * Write a collection of schemes to the underlying stream
   *
   * @throws IOException If an I/O error occurs.
   */
  public void writeSchemes(Collection<Scheme> schemes) throws IOException {
    if ((schemes == null) || schemes.isEmpty())
      return;

    for (Scheme scheme : schemes) {
      writeScheme(scheme);
    }
  }

  /**
   * Writes the cr representation of a Scheme object to the underlying stream.
   *
   * @throws IOException If an I/O error occurs.
   */
  public void writeScheme(Scheme scheme) throws IOException {
    write("SCHEMEN " + scheme.getID().toString(" "));
    newLine();

    if (scheme.getName() != null) {
      writeQuotedTag(scheme.getName(), "Name");
    }
  }

  /**
   * Write a collection of region resources to the underlying stream
   *
   * @throws IOException If an I/O error occurs.
   */
  public void writeRegionResources(Collection<RegionResource> resources) throws IOException {
    if ((resources == null) || resources.isEmpty())
      return;

    for (RegionResource regionResource : resources) {
      writeRegionResource(regionResource);
    }
  }

  /**
   * Writes the cr representation of a region resource object to the underlying stream.
   *
   * @throws IOException If an I/O error occurs.
   */
  public void writeRegionResource(RegionResource res) throws IOException {
    write("RESOURCE " + res.getID().toString());
    newLine();
    writeQuotedTag(res.getType().getID().toString(), "type");

    if (res.getAmount() > -1) {
      write(res.getAmount() + ";number");
      newLine();
    }

    if (res.getSkillLevel() > -1) {
      write(res.getSkillLevel() + ";skill");
      newLine();
    }

    if (res.getDate() != null && res.getDate().getDate() > -1 && !serverConformance) {
      write(res.getDate().getDate() + ";Runde");
      newLine();
    }

  }

  /**
   * Write message type blocks to the underlying stream.
   *
   * @param map a map containing the <kbd>MessageType</kbd> objects to be written as values.
   * @throws IOException If an I/O error occurs.
   */
  public void writeMsgTypes(Map<? extends ID, MessageType> map) throws IOException {
    if (map == null)
      return;

    for (MessageType messageType : map.values()) {
      writeMessageType(messageType);
    }
  }

  /**
   * Write the cr representation of a <kbd>MessageType</kbd> object to the underlying stream.
   *
   * @throws IOException If an I/O error occurs.
   */
  public void writeMessageType(MessageType msgType) throws IOException {
    if (msgType == null) {
      CRWriter.log.warn("CRWriter.writeMessageType(): argument msgType is null");

      return;
    }

    if ((msgType.getID() == null) || ((msgType.getID()).intValue() < 0)) {
      CRWriter.log.warn("CRWriter.writeMessageType(): invalid ID");

      return;
    }

    if (msgType.getPattern() == null) {
      CRWriter.log.warn("CRWriter.writeMessageType(): pattern of message type " + msgType.getID()
          + " is null");

      return;
    }

    write("MESSAGETYPE " + msgType.getID().toString());
    newLine();
    writeQuotedTag(msgType.getPattern(), "text");

    if (msgType.getSection() != null) {
      writeQuotedTag(msgType.getSection(), "section");
    }
  }

  /**
   * Writes the GameData in the current thread.
   *
   * @throws IOException If an I/O error occurs
   */
  public synchronized void writeSynchronously() throws IOException {
    savingInProgress = true;

    ui.show();

    try {
      // Bug #117: make sure that savingInProgress is true, until the
      // writer is closed or writer could remain open in multi-threaded
      // execution.
      doWrite();
    } catch (Throwable exception) {
      CRWriter.log.error(exception);
      ui.showException(Resources.get("crwriterdialog.exception"), null, exception);
    } finally {
      close(true);
      savingInProgress = false;
    }
  }

  /**
   * Write the complete game data from <kbd>world</kbd> in the cr format.
   */
  public synchronized Thread writeAsynchronously() throws IOException, NullPointerException {
    if (world == null)
      throw new NullPointerException("CRWriter.write(GameData): argument world is null");
    savingInProgress = true;

    Thread t = null;

    ui.show();

    t = new Thread(new Runnable() {
      @Override
      public void run() {
        try {
          // Bug #117: make sure that savingInProgress is true, until the
          // writer is closed or writer could remain open in multi-threaded
          // execution.
          doWrite();
        } catch (Throwable exception) {
          CRWriter.log.error(exception);
          ui.showException(Resources.get("crwriterdialog.exception"), null, exception);
        } finally {
          // FIXME (stm) we close here /and/ in the calling method. Should make up our mind's what
          // to do where
          try {
            close(true);
          } catch (IOException e) {
            CRWriter.log.error(e);
            ui.showException(Resources.get("crwriterdialog.exception"), null, e);
          }
          savingInProgress = false;
        }
      }
    });
    t.start();

    if (world == null) {
      // for debugging
      doWrite();
    }

    return t;
  }

  /**
   * Close, even if saving in progress.
   *
   * @param b
   * @throws IOException
   */
  private void close(boolean b) throws IOException {
    super.close();
  }

  /**
   * @see java.io.BufferedWriter#close()
   */
  @Override
  public void close() throws IOException {
    if (savingInProgress)
      return;
    super.close();
  }

  /**
   * Write the complete game data from <kbd>world</kbd> in the cr format. This method is called by the
   * public method write(). This method can be run in a thread.
   *
   * @throws IOException If an I/O error occurs.
   */
  protected synchronized void doWrite() throws IOException {
    try {
      CRWriter.log.info("Start saving report. Encoding: " + encoding);
      if (!encoding.equalsIgnoreCase(world.getEncoding())) {
        CRWriter.log.warn("Encodings differ while writing CR: writer users " + encoding
            + ", gamadata is set to " + world.getEncoding() + ", setting charset to:"
            + world.getEncoding());
        encoding = world.getEncoding();
      }

      ui.setMaximum(11);
      ui.setTitle(Resources.get("crwriterdialog.progress.title"));
      ui.setProgress(Resources.get("crwriterdialog.progress.01"), 1);

      writeVersion(world);

      if (!serverConformance) {
        writeCoordinateTranslations(world);
        writeAttributes(world);
      }

      // if (!serverConformance && exportHotspots) {
      // ui.setProgress(Resources.get("crwriterdialog.progress.02"), 2);
      // writeHotSpots(world.getHotSpots());
      // }

      // this assumes that if somebody doesn't write units
      // also factions aren't necessary; maybe this needs further
      // specification
      ui.setProgress(Resources.get("crwriterdialog.progress.03"), 3);
      if (includeUnits) {
        writeAlliances(world.getAllianceGroups());
        writeFactions(world.getFactions(), false);
      } else if (getOwnerFaction(world.getFactions()) != null) {
        writeFactions(Collections.singletonList(getOwnerFaction(world.getFactions())), true);
      }

      if (includeSpellsAndPotions) {
        ui.setProgress(Resources.get("crwriterdialog.progress.04"), 4);
        writeSpells(world.getSpells());

        ui.setProgress(Resources.get("crwriterdialog.progress.05"), 5);
        writePotions(world.getPotions());
      }

      if (!serverConformance && includeIslands) {
        ui.setProgress(Resources.get("crwriterdialog.progress.06"), 6);
        writeIslands(world.getIslands());
      }

      if (includeRegions) {
        if (!getServerConformance() && getIncludeRegions()) {
          oldUnitMap = CollectionFactory.createMap();
          for (Unit u : world.getOldUnits()) {
            List<Unit> oldUnits = oldUnitMap.get(u.getRegion());
            if (oldUnits == null) {
              oldUnitMap.put(u.getRegion(), oldUnits = new ArrayList<Unit>());
            }
            oldUnits.add(u);
          }
        }

        ui.setProgress(Resources.get("crwriterdialog.progress.07"), 7);
        if ((regions != null) && (regions.size() > 0)) {
          writeRegions(regions);
          Set<Region> rs = new HashSet<Region>(regions);
          Map<CoordinateID, Region> ws = new HashMap<CoordinateID, Region>();
          for (Region w : world.wrappers().values()) {
            if (rs.contains(world.getOriginal(w))) {
              ws.put(w.getCoordinate(), w);
            }
          }
          writeRegions(ws.values());
        } else {
          writeRegions(world.getRegions());
        }
        writeRegions(world.wrappers().values());
      }

      if (includeMessages) {
        ui.setProgress(Resources.get("crwriterdialog.progress.08"), 8);
        writeMsgTypes(world.msgTypes());
      }

      ui.setProgress(Resources.get("crwriterdialog.progress.09"), 9);
      writeTranslations(world.translations());

      if (includeRegions && includeUnits && ((regions == null) || (regions.size() == 0))) {
        ui.setProgress(Resources.get("crwriterdialog.progress.10"), 10);

        if (world.getUnits() != null) {
          if (world.getUnits().size() != unitsWritten) {
            int homelessUnitsCounter = 0;

            for (Unit u : world.getUnits()) {
              if (u.getRegion() == null || u.getRegion() == world.getNullRegion()) {
                homelessUnitsCounter++;
              }
            }

            if ((world.getUnits().size() + world.getOldUnits().size()) != unitsWritten
                + homelessUnitsCounter)
              throw new IOException("Although there are "
                  + (world.getUnits().size() + world.getOldUnits().size()) + " units, only "
                  + (unitsWritten + homelessUnitsCounter) + " were written!");
          }
        }
      }
    } finally {
      ui.setProgress(Resources.get("crwriterdialog.progress.11"), 11);
      ui.ready();
    }
    CRWriter.log.info("Done saving report");
  }

  private Faction getOwnerFaction(Collection<Faction> factions) {
    Faction ownerFaction = null;

    if (world.getOwnerFaction() != null) {
      ownerFaction = world.getFaction(world.getOwnerFaction());
    }
    if (ownerFaction == null) {
      if (factions.size() > 0) {
        ownerFaction = factions.iterator().next();
      }
    }
    return ownerFaction;
  }

  /**
   * Returns <code>true</code> if writing has begun, but not finished
   */
  public boolean savingInProgress() {
    return savingInProgress;
  }

  /**
   * Change the quote escape behaviour of this CRWriter. Tilde escapes look like: 'a "b c"' &rarr; 'a
   * b~c', whereas a backslash escape works like this: 'a "b c"' &rarr; 'a \"b c\"'
   *
   * @param bool if <kbd>true</kbd>, escape quoted parts of any string written to the underlying
   *          stream with tildes. If <kbd>false</kbd>, use backslash character to escape the quotation
   *          marks themselves.
   */
  public void setTildeEscapes(boolean bool) {
    useTildesForQuotes = true;
  }

  private boolean includeRegions = true;

  /**
   * Returns whether {@link #doWrite()} writes information about the regions in data to the
   * underlying stream.
   */
  public boolean getIncludeRegions() {
    return includeRegions;
  }

  /**
   * Toggles whether {@link #doWrite()} writes information about the regions in data to the
   * underlying stream.
   */
  public void setIncludeRegions(boolean includeRegions) {
    this.includeRegions = includeRegions;
  }

  private boolean includeBuildings = true;

  /**
   * Returns whether {@link #doWrite()} writes information about the buildings in data to the
   * underlying stream.
   */
  public boolean getIncludeBuildings() {
    return includeBuildings;
  }

  /**
   * Toggles whether {@link #doWrite()} writes information about the buildings in data to the
   * underlying stream.
   */
  public void setIncludeBuildings(boolean includeBuildings) {
    this.includeBuildings = includeBuildings;
  }

  private boolean includeShips = true;

  /**
   * Returns whether {@link #doWrite()} writes information about the ships in data to the underlying
   * stream.
   */
  public boolean getIncludeShips() {
    return includeShips;
  }

  /**
   * Toggles whether {@link #doWrite()} writes information about the ships in data to the underlying
   * stream.
   */
  public void setIncludeShips(boolean includeShips) {
    this.includeShips = includeShips;
  }

  private boolean includeUnits = true;

  /**
   * Returns whether {@link #doWrite()} writes information about the units in data to the underlying
   * stream.
   */
  public boolean getIncludeUnits() {
    return includeUnits;
  }

  /**
   * Toggles whether {@link #doWrite()} writes information about the units in data to the underlying
   * stream.
   */
  public void setIncludeUnits(boolean includeUnits) {
    this.includeUnits = includeUnits;
  }

  private boolean includeUnitDetails = true;

  /**
   * Toggles whether {@link #doWrite()} writes information about the unit skills in data to the
   * underlying stream.
   *
   * @param newValue
   */
  public void setIncludeUnitDetails(boolean newValue) {
    includeUnitDetails = newValue;
  }

  /**
   * Returns true if details of unit's will be included in the written report.
   */
  public boolean getIncludeUnitDetails() {
    return includeUnitDetails;
  }

  private boolean includeSkills = true;

  /**
   * Toggles whether {@link #doWrite()} writes information about the unit skills in data to the
   * underlying stream.
   *
   * @param newValue
   */
  public void setIncludeSkills(boolean newValue) {
    includeSkills = newValue;
  }

  /**
   * Returns <code>true</code> if units' skills will be written.
   */
  public boolean getIncludeSkills() {
    return includeSkills;
  }

  private boolean includeOrders = true;

  /**
   * Toggles whether {@link #doWrite()} writes the units' orders in data to the underlying stream.
   *
   * @param newValue
   */
  public void setIncludeOrders(boolean newValue) {
    includeOrders = newValue;
  }

  /**
   * Returns <code>true</code> if the unit's orders will be included in writing.
   */
  public boolean getIncludeOrders() {
    return includeOrders;
  }

  private boolean includeItems = true;

  /**
   * Toggles whether {@link #doWrite()} writes information about the unit skills in data to the
   * underlying stream.
   *
   * @param newValue
   */
  public void setIncludeItems(boolean newValue) {
    includeItems = newValue;
  }

  /**
   * Returns true if items will be written.
   */
  public boolean getIncludeItems() {
    return includeItems;
  }

  private boolean includeRegionDetails = true;

  /**
   * Returns whether {@link #doWrite()} writes detailed information about the regions in data to the
   * underlying stream.
   */
  public boolean getIncludeRegionDetails() {
    return includeRegionDetails;
  }

  /**
   * Toggles whether {@link #doWrite()} writes detailed information about the regions in data to the
   * underlying stream.
   */
  public void setIncludeRegionDetails(boolean includeRegionDetails) {
    this.includeRegionDetails = includeRegionDetails;
  }

  private boolean includeIslands = true;

  /**
   * Returns whether {@link #doWrite()} writes information about islands to the underlying stream.
   */
  public boolean getIncludeIslands() {
    return includeIslands;
  }

  /**
   * Toggles whether {@link #doWrite()} writes information about islands to the underlying stream.
   */
  public void setIncludeIslands(boolean includeIslands) {
    this.includeIslands = includeIslands;
  }

  private boolean includeMessages = true;

  /**
   * Returns whether {@link #doWrite()} writes messages contained in the game data to the underlying
   * stream.
   */
  public boolean getIncludeMessages() {
    return includeMessages;
  }

  /**
   * Toggles whether {@link #doWrite()} writes messages contained in the game data to the underlying
   * stream.
   */
  public void setIncludeMessages(boolean includeMessages) {
    this.includeMessages = includeMessages;
  }

  private boolean exportHotspots = true;

  /**
   * Returns whether {@link #doWrite()} writes Hotspots contained in the game data to the underlying
   * stream.
   */
  public boolean getExportHotspots() {
    return exportHotspots;
  }

  /**
   * Toggles whether {@link #doWrite()} writes Hotspots contained in the game data to the underlying
   * stream.
   */
  public void setExportHotspots(boolean exportHotspots) {
    this.exportHotspots = exportHotspots;
  }

  private boolean includeSpellsAndPotions = true;

  /**
   * Returns whether {@link #doWrite()} writes messages contained in the game data to the underlying
   * stream.
   */
  public boolean getIncludeSpellsAndPotions() {
    return includeSpellsAndPotions;
  }

  /**
   * Toggles whether {@link #doWrite()} writes messages contained in the game data to the underlying
   * stream.
   */
  public void setIncludeSpellsAndPotions(boolean includeSpellsAndPotions) {
    this.includeSpellsAndPotions = includeSpellsAndPotions;
  }

  private boolean serverConformance = false;

  /**
   * Returns whether {@link #doWrite()} writes a cr that is compatible with cr's generated by the
   * Eressea server, i.e. not including JavaClient specific data.
   */
  public boolean getServerConformance() {
    return serverConformance;
  }

  /**
   * Toggles whether {@link #doWrite()} writes a cr that is compatible with cr's generated by the
   * Eressea server, i.e. not including JavaClient specific data.
   */
  public void setServerConformance(boolean serverConformance) {
    this.serverConformance = serverConformance;
  }

  /**
   * Write a sequence of island blocks to the underlying stream.
   *
   * @param islands a collection containing the islands to write.
   * @throws IOException If an I/O error occurs.
   */
  public void writeIslands(Collection<Island> islands) throws IOException {
    if (islands == null)
      return;

    for (Island island : islands) {
      write(island);
    }
  }

  /**
   * Write the cr representation of an <kbd>Island</kbd> object to the underlying stream.
   *
   * @throws IOException If an I/O error occurs.
   */
  public void write(Island island) throws IOException {
    if (island == null)
      return;

    write("ISLAND " + island.getID());
    newLine();

    if (island.getName() != null) {
      writeQuotedTag(island.getName(), "name");
    }

    if (island.getDescription() != null) {
      writeQuotedTag(island.getDescription(), "Beschr");
    }

    if (!serverConformance && exportHotspots) {
      writeBookmark(island);
      writeAttributes(island);
    }
  }

  // /**
  // * Write a sequence of hot spot blocks to the underlying stream.
  // *
  // * @throws IOException If an I/O error occurs.
  // */
  // public void writeHotSpots(Collection<HotSpot> hotSpots) throws IOException {
  // if (hotSpots == null)
  // return;
  //
  // for (HotSpot hotSpot : hotSpots) {
  // write(hotSpot);
  // }
  // }
  //
  // /**
  // * Write the cr representation of a hot spot to the underlying stream.
  // *
  // * @throws IOException If an I/O error occurs.
  // */
  // public void write(HotSpot h) throws IOException {
  // if (h == null)
  // return;
  //
  // write("HOTSPOT " + h.getID().toString(" "));
  // newLine();
  // writeQuotedTag(h.getName(), "name");
  // writeQuotedTag(h.getCenter().toString(" "), "coord");
  // }

  private Collection<Region> regions = null;

  /**
   * Returns the regions this object writes to the underlying stream.
   */
  public Collection<Region> getRegions() {
    return regions;
  }

  /**
   * Supply the writer with a collection of regions it should write to the underlying stream instead
   * of all regions contained in the game data. If regions is null or if there is no element in the
   * supplied collection, the writer returns to writing all regions defined in the game data.
   */
  public void setRegions(Collection<Region> regions) {
    this.regions = regions;
  }

  private Collection<Unit> units = null;

  /**
   * Returns the units this object writes to the underlying stream.
   */
  public Collection<Unit> getUnits() {
    return units;
  }

  /**
   * Supply the writer with a collection of units it should write to the underlying stream instead
   * of all units contained in the game data. If units is null or if there is no element in the
   * supplied collection, the writer returns to writing all units defined in the game data.
   */
  public void setUnits(Collection<Unit> units) {
    this.units = units;
  }

  /**
   * Write the translation table to underlying stream.
   *
   * @throws IOException If an I/O error occurs.
   */
  public void writeTranslations(Translations m) throws IOException {
    if ((m == null) || (m.size() == 0))
      return;

    write("TRANSLATION");
    newLine();

    for (String key : m.getKeyTreeSet()) {
      String value = m.getTranslation(key, TranslationType.SOURCE_CR);
      if (value != null) {
        writeQuotedTag(value, key);
      }
    }
  }

  /**
   * Writes all attributes to the stream
   */
  public void writeAttributes(Addeable addeable) throws IOException {
    if (addeable == null || addeable.getAttributeSize() <= 0)
      return;

    write("ATTRIBUTES");
    newLine();

    List<String> keys = addeable.getAttributeKeys();
    for (String key : keys) {
      String value = addeable.getAttribute(key);
      writeQuotedTag(value, key);
    }
  }

}
