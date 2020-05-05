/*
 * Copyright (C) 2000-2004 Roger Butenuth, Andreas Gampe, Stefan Goetz, Sebastian Pappert, Klaas
 * Prause, Enno Rehling, Sebastian Tusk, Ulrich Kuester, Ilja Pavkovic This file is part of the
 * Eressea Java Code Base, see the file LICENSING for the licensing information applying to this
 * file.
 */

package magellan.library.io.cr;

import static org.apache.commons.beanutils.PropertyUtils.setProperty;

import java.io.IOException;
import java.io.Reader;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import magellan.library.Addeable;
import magellan.library.Alliance;
import magellan.library.AllianceGroup;
import magellan.library.Battle;
import magellan.library.BookmarkBuilder;
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
import magellan.library.LongID;
import magellan.library.LuxuryPrice;
import magellan.library.Message;
import magellan.library.MissingData;
import magellan.library.Potion;
import magellan.library.Region;
import magellan.library.RegionResource;
import magellan.library.Rules;
import magellan.library.Scheme;
import magellan.library.Selectable;
import magellan.library.Ship;
import magellan.library.Sign;
import magellan.library.Skill;
import magellan.library.Spell;
import magellan.library.StringID;
import magellan.library.Unit;
import magellan.library.UnitContainer;
import magellan.library.UnitID;
import magellan.library.gamebinding.EresseaConstants;
import magellan.library.impl.MagellanIslandImpl;
import magellan.library.impl.SpellBuilder;
import magellan.library.io.AbstractReportParser;
import magellan.library.io.GameDataIO;
import magellan.library.io.RulesIO;
import magellan.library.io.file.FileType;
import magellan.library.rules.AllianceCategory;
import magellan.library.rules.BuildingType;
import magellan.library.rules.CastleType;
import magellan.library.rules.ConstructibleType;
import magellan.library.rules.Date;
import magellan.library.rules.EresseaDate;
import magellan.library.rules.GenericRules;
import magellan.library.rules.ItemCategory;
import magellan.library.rules.ItemType;
import magellan.library.rules.MessageType;
import magellan.library.rules.OptionCategory;
import magellan.library.rules.Options;
import magellan.library.rules.OrderType;
import magellan.library.rules.Race;
import magellan.library.rules.RegionType;
import magellan.library.rules.Resource;
import magellan.library.rules.ShipType;
import magellan.library.rules.SkillCategory;
import magellan.library.rules.SkillType;
import magellan.library.utils.CollectionFactory;
import magellan.library.utils.MagellanFactory;
import magellan.library.utils.MemoryManagment;
import magellan.library.utils.NullUserInterface;
import magellan.library.utils.Resources;
import magellan.library.utils.TranslationType;
import magellan.library.utils.UserInterface;
import magellan.library.utils.logging.Logger;
import magellan.library.utils.transformation.IdentityTransformer;
import magellan.library.utils.transformation.ReportTransformer;
import magellan.library.utils.transformation.TwoLevelTransformer;

/**
 * Parser for cr-files.
 */
public class CRParser extends AbstractReportParser implements RulesIO, GameDataIO {
  protected static final Logger log = Logger.getInstance(CRParser.class);

  /** These special tags are used by TreeHelper and are therefore reserved. */
  public static final String TAGGABLE_STRING = "ejcTaggableComparator";
  /** These special tags are used by TreeHelper and are therefore reserved. */
  public static final String TAGGABLE_STRING2 = "ejcTaggableComparator2";
  /** These special tags are used by TreeHelper and are therefore reserved. */
  public static final String TAGGABLE_STRING3 = "ejcTaggableComparator3";
  /** These special tags are used by TreeHelper and are therefore reserved. */
  public static final String TAGGABLE_STRING4 = "ejcTaggableComparator4";
  /** These special tags are used by TreeHelper and are therefore reserved. */
  public static final String TAGGABLE_STRING5 = "ejcTaggableComparator5";

  Scanner sc;
  String configuration;
  String coordinates;
  boolean umlauts;
  final Collection<String> warnedLines = new HashSet<String>();

  private BlockParser unitParser;

  private BlockParser regionParser;

  private static long tRParse, tUParse;

  /**
   * Creates a new parser.
   *
   * @param ui The UserInterface for the progress. Can be NULL. Then no operation is displayed.
   */
  public CRParser(UserInterface ui) {
    this(ui, new IdentityTransformer());
  }

  /**
   * @deprecated Use {@link #CRParser(UserInterface, ReportTransformer)}
   */
  @Deprecated
  public CRParser(UserInterface ui, CoordinateID newOrigin) {
    this(ui, new TwoLevelTransformer(newOrigin, CoordinateID.ZERO));
  }

  /**
   * Creates a new parser. This new parser translates coordinates according to newOrigin. All
   * coordinates which are read from the report are translated by newOrigin. That is, if a
   * coordinate read and its level (the z coordinate) equals the new origins level, its x and y
   * coordinates are decreased by origin.x and origin.y, respectively. That means, that the reports
   * origin is transferred to newOrigin.
   *
   * @param translator The coordinates (relative to the origin of the report) of the new origin.
   */
  public CRParser(UserInterface ui, ReportTransformer translator) {
    if (ui == null) {
      this.ui = new NullUserInterface();
    } else {
      this.ui = ui;
    }
    transformer = translator;
  }

  /**
   * Returns the value of the configuration tag ("Konfiguration") as it has been read from the CR.
   *
   * @return The configuration string or <code>null</code> if the tag hasn't been read (yet)
   */
  public String getConfiguration() {
    return configuration;
  }

  /**
   * Print an error message on the standard output channel. Does not produce duplicate messages.
   *
   * @param context The context (usually a block) within the error has been found.
   * @param fetch If this is true, read the next line and skip the line with the error. Otherwise
   *          the line stays still at the front of the input.
   */
  protected void unknown(String context, boolean fetch) throws IOException {
    unknown(context, fetch, Logger.WARN);
  }

  protected void unknown(String context, boolean fetch, int logLevel) throws IOException {
    int i;

    final StringBuilder msg = new StringBuilder();

    for (i = 0; i < sc.argc; i++) {
      if (sc.isString[i]) {
        msg.append("\"");
      }

      msg.append(sc.argv[i]);

      if (sc.isString[i]) {
        msg.append("\"");
      }

      if ((i + 1) < sc.argc) {
        msg.append(";");
      }
    }

    if (!warnedLines.contains(context + "_" + msg)) {
      // only warn once for context and message combination
      CRParser.log.log(logLevel, "unknown in line " + sc.lnr + ": (" + context + ")", null);
      CRParser.log.log(logLevel, msg, null);
      warnedLines.add(context + "_" + msg);
    }

    if (fetch) {
      sc.getNextToken();
    }
  }

  /**
   * Read the MESSAGETYPES block. Note that message type stubs have already been created by parsing
   * the messages themselves.
   *
   * @return the resulting list of <tt>MessageType</tt> objects.
   * @throws IOException if the scanner throws an IOException
   */
  private List<MessageType> parseMessageTypes(GameData data) throws IOException {
    final List<MessageType> list = new LinkedList<MessageType>();
    sc.getNextToken(); // skip the block

    while (!sc.eof && !sc.isBlock) {
      if (sc.argc == 2) {
        try {
          MessageType mt = data.getMsgType(IntegerID.create(sc.argv[1]));

          if (mt == null) {
            mt = new MessageType(IntegerID.create(sc.argv[1]), sc.argv[0]);
            data.addMsgType(mt);
          } else {
            mt.setPattern(sc.argv[0]);
            // set the GameData were this message type belongs to
            // this is required to render messages of this type
            mt.setGameData(data);
          }

          list.add(mt);
        } catch (final NumberFormatException e) {
          CRParser.log.error(e);
        }
      }

      sc.getNextToken();
    }

    return list;
  }

  /**
   * Read a MESSAGETYPE block. Note that message type stubs have already been created by parsing the
   * messages themselves.
   *
   * @throws IOException if the scanner throws an IOException
   */
  private void parseMessageType(GameData data) throws IOException {
    final IntegerID id = IntegerID.create(sc.argv[0].substring(12).trim());
    MessageType mt = data.getMsgType(id);

    if (mt == null) {
      mt = new MessageType(id);
      data.addMsgType(mt);
    }

    sc.getNextToken(); // skip the block

    while (!sc.eof) {
      if ((sc.argc == 2) && sc.argv[1].equalsIgnoreCase("text")) {
        mt.setPattern(sc.argv[0]);
        // set the GameData were this message type belongs to
        // this is required to render messages of this type
        mt.setGameData(data);
      } else if ((sc.argc == 2) && sc.argv[1].equalsIgnoreCase("section")) {
        mt.setSection(sc.argv[0]);
      } else if (sc.isBlock) {
        break;
      } else {
        unknown("MESSAGETYPE", false);
      }

      sc.getNextToken();
    }
  }

  /**
   * Handle a sequence of quoted strings, interpreting them as messages.
   *
   * @param msgs a list to add the read messages to. May be <code>null</code>.
   * @return the resulting list of <tt>Message</tt> objects.
   */
  private List<Message> parseMessageSequence(List<Message> msgs) throws IOException {
    sc.getNextToken(); // skip the block

    while (!sc.eof && (sc.argc == 1) && sc.isString[0]) {
      if (msgs == null) {
        msgs = new ArrayList<Message>();
      }

      // 2002.04.24 pavkovic: remove duplicate entries
      final Message msg = MagellanFactory.createMessage(sc.argv[0]);

      if (msgs.contains(msg)) {
        // log.warn("Duplicate message \"" + msg.getText() + "\" found, removing it.");
        if (CRParser.log.isDebugEnabled()) {
          CRParser.log.debug("List: " + msgs);
          CRParser.log.debug("new entry:" + msg);
        }
      } else {
        msgs.add(msg);
      }

      sc.getNextToken();
    }

    return msgs;
  }

  /**
   * Handle a sequence of quoted strings, storing them as <tt>String</tt> objects. String
   * interpretation starts with the next line.
   *
   * @param strings a list to add the read strings to. May be <code>null</code>.
   * @return the resulting list of <tt>String</tt> objects.
   */
  private List<String> parseStringSequence(List<String> strings) throws IOException {
    sc.getNextToken(); // skip the block

    while (!sc.eof && (sc.argc == 1) && sc.isString[0]) {
      if (strings == null) {
        strings = new ArrayList<String>();
      }

      strings.add(sc.argv[0]);
      sc.getNextToken();
    }

    // avoid unnecessary list allocations
    if ((strings != null) && (strings.size() == 0)) {
      strings = null;
    }

    return strings;
  }

  /**
   * Parse the SPRUECHE sub block of UNIT and add them as <tt>Spell</tt> objects.
   *
   * @param world the game data to get the spells from
   * @param map a map to add the read spells to
   * @return the resulting map of <tt>Spell</tt> objects.
   */
  private Map<ID, Spell> parseUnitSpells(Map<ID, Spell> map) throws IOException {
    sc.getNextToken(); // skip the block

    while (!sc.eof && !sc.isBlock) {
      final StringID id = StringID.create(sc.argv[0]);
      Spell s = world.getSpell(id);

      if (s == null) {
        s = MagellanFactory.createSpell(id, world);
        s.setName(sc.argv[0]);
        world.addSpell(s);
      }

      if (map == null) {
        map = CollectionFactory.<ID, Spell> createSyncOrderedMap();
      }

      map.put(s.getID(), s);
      sc.getNextToken();
    }

    return map;
  }

  /**
   * Parse a KAMPFZAUBER sub block of UNIT and add it as <tt>CombatSpell</tt> object.
   *
   * @param world the game data to get the spells from
   * @param unit the unit that should get the combat spells set
   */
  private void parseUnitCombatSpells(Unit unit) throws IOException {
    final IntegerID id = IntegerID.create(sc.argv[0].substring(12).trim());
    final CombatSpell s = MagellanFactory.createCombatSpell(id);
    s.setUnit(unit);

    if (unit.getCombatSpells() == null) {
      unit.setCombatSpells(new LinkedHashMap<ID, CombatSpell>());
    }

    unit.getCombatSpells().put(s.getID(), s);

    sc.getNextToken();

    while (!sc.eof) {
      if ((sc.argc == 2) && sc.argv[1].equalsIgnoreCase("name")) {
        final StringID spellID = StringID.create(sc.argv[0]);
        Spell spell = world.getSpell(spellID);

        if (spell == null) {
          CRParser.log
              .warn(
                  "CRParser.parseUnitCombatSpells(): a combat spell refers to an unknown spell (line "
                      + sc.lnr + ")");
          spell = MagellanFactory.createSpell(spellID, world);
          spell.setName(sc.argv[0]);
          world.addSpell(spell);
        }

        s.setSpell(spell);
      } else if ((sc.argc == 2) && sc.argv[1].equalsIgnoreCase("level")) {
        s.setCastingLevel(Integer.parseInt(sc.argv[0]));
      } else if (sc.isBlock) {
        break;
      } else {
        unknown("KAMPFZAUBER", false);
      }

      sc.getNextToken();
    }
  }

  /**
   * Parse message blocks as can be found in cr versions >= 41. This function evaluates only two
   * special message attributes. These are the ";type" and ";rendered" attributes, which are
   * directly accessible in the <tt>Message</tt> object as type and text. If there is no MessageType
   * object for this type of message, a stub MessageType object is created and added to world.
   *
   * @return a list containing <tt>Message</tt> objects for all messages read.
   * @throws IOException if the scanner throws an IOException
   */
  private List<Message> parseMessages(List<Message> list) throws IOException {
    while (sc.isBlock && sc.argv[0].startsWith("MESSAGE ")) {
      final IntegerID id = IntegerID.create(sc.argv[0].substring(8));
      final Message msg = MagellanFactory.createMessage(id);

      // read message attributes
      sc.getNextToken(); // skip MESSAGE xx

      while (!sc.eof && !sc.isBlock) {
        if ((sc.argc == 2) && sc.argv[1].equalsIgnoreCase("type")) {
          final IntegerID typeID = IntegerID.create(sc.argv[0]);
          MessageType mt = world.getMsgType(typeID);

          if (mt == null) {
            mt = new MessageType(typeID);
            world.addMsgType(mt);
          }

          msg.setType(mt);
        } else if ((sc.argc == 2) && sc.argv[1].equalsIgnoreCase("rendered")) {
          msg.setText(originTranslate(sc.argv[0]));
        } else if ((sc.argc == 2) && sc.argv[1].equalsIgnoreCase("toolacknowledged")) {
          msg.setAcknowledged(sc.argv[0].equals("1"));
        } else if (sc.argc == 2) {
          if (msg.getAttributes() == null) {
            msg.setAttributes(CollectionFactory.<String, String> createSyncOrderedMap(4));
          }

          CoordinateID coord = CoordinateID.parse(sc.argv[0], ",");

          if (coord != null) {
            final CoordinateID newCoord = originTranslate(coord);
            msg.getAttributes().put(sc.argv[1], newCoord.toString(","));
          } else {
            coord = CoordinateID.parse(sc.argv[0], " ");
            if (coord != null) {
              final CoordinateID newCoord = originTranslate(coord);
              msg.getAttributes().put(sc.argv[1], newCoord.toString(" ", true));
            } else {
              // check for ;regions
              if (sc.argv[1].equalsIgnoreCase("regions")) {
                // special dealing
                msg.getAttributes().put(sc.argv[1], originTranslateRegions(sc.argv[0]));
              } else {
                msg.getAttributes().put(sc.argv[1], sc.argv[0]);
              }
            }
          }
        }

        sc.getNextToken();
      }

      if (list == null) {
        list = new ArrayList<Message>();
      }
      list.add(msg);
    }

    return list;
  }

  /**
   * Parse a battle block sequence. Currently this is a block of message blocks.
   *
   * @return A List of instances of class Battle. May be <code>null</code>.
   * @throws IOException if the scanner throws an IOException
   */
  private List<Battle> parseBattles(List<Battle> list) throws IOException {
    while (!sc.eof && sc.argv[0].startsWith("BATTLE ")) {
      CoordinateID c = CoordinateID.parse(sc.argv[0].substring(sc.argv[0].indexOf(" ", 0)), " ");
      if (c == null) {
        unknown("BATTLE", true);
        break;
      }
      c = originTranslate(c);

      final Battle battle = MagellanFactory.createBattle(c);

      if (list == null) {
        list = new ArrayList<Battle>();
      }

      list.add(battle);
      sc.getNextToken(); // skip BATTLE x y
      parseMessages(battle.messages());
    }

    return list;
  }

  /**
   * Parse a battlespec block sequence. Currently this is a block of message blocks.
   *
   * @return A List of instances of class Battle.
   * @throws IOException if the scanner throws an IOException
   */
  private List<Battle> parseBattleSpecs(List<Battle> list) throws IOException {
    while (!sc.eof && sc.argv[0].startsWith("BATTLESPEC ")) {
      CoordinateID c = CoordinateID.parse(sc.argv[0].substring(sc.argv[0].indexOf(" ", 0)), " ");
      if (c == null) {
        unknown("BATTLESPEC", true);
        continue;
      }
      c = originTranslate(c);
      final Battle battle = MagellanFactory.createBattle(c, true);

      if (list == null) {
        list = new ArrayList<Battle>();
      }

      list.add(battle);
      sc.getNextToken(); // skip BATTLE x y
      parseMessages(battle.messages());
    }

    return list;
  }

  /**
   * Parse a sequence of spell blocks. Do not confuse this with the spells block of a unit!
   *
   * @throws IOException if the scanner throws an IOException
   */
  private void parseSpells() throws IOException {
    while (!sc.eof && sc.isBlock && sc.argv[0].startsWith("ZAUBER ")) {
      final IntegerID id = IntegerID.create(sc.argv[0].substring(7).trim());

      // not adding spell immediately is required here, please do not change this, unless you really
      // know, what you're doing!
      final SpellBuilder spell = new SpellBuilder(id, world); // MagellanFactory.createSpell(id,
      // world);
      spell.setBlockID((id).intValue());
      sc.getNextToken(); // skip ZAUBER nr

      BookmarkBuilder bm = null;
      while (!sc.eof) {
        if ((sc.argc == 2) && sc.argv[1].equalsIgnoreCase("name")) {
          spell.setName(sc.argv[0]);
          sc.getNextToken();
        } else if ((sc.argc == 2) && sc.argv[1].equalsIgnoreCase("info")) {
          spell.setDescription(sc.argv[0]);
          sc.getNextToken();
        } else if ((sc.argc == 2) && sc.argv[1].equalsIgnoreCase("level")) {
          spell.setLevel(Integer.parseInt(sc.argv[0]));
          sc.getNextToken();
        } else if ((sc.argc == 2) && sc.argv[1].equalsIgnoreCase("rank")) {
          spell.setRank(Integer.parseInt(sc.argv[0]));
          sc.getNextToken();
        } else if ((sc.argc == 2) && sc.argv[1].equalsIgnoreCase("class")) {
          spell.setType(sc.argv[0]);
          sc.getNextToken();
        } else if ((sc.argc == 2) && sc.argv[1].equalsIgnoreCase("syntax")) {
          // FF 20070221 : new ;syntax
          spell.setSyntax(sc.argv[0]);
          sc.getNextToken();
        } else if ((sc.argc == 2) && sc.argv[1].equalsIgnoreCase("ship")) {
          spell.setOnShip(Integer.parseInt(sc.argv[0]) != 0);
          sc.getNextToken();
        } else if ((sc.argc == 2) && sc.argv[1].equalsIgnoreCase("ocean")) {
          spell.setOnOcean(Integer.parseInt(sc.argv[0]) != 0);
          sc.getNextToken();
        } else if ((sc.argc == 2) && sc.argv[1].equalsIgnoreCase("familiar")) {
          spell.setIsFamiliar(Integer.parseInt(sc.argv[0]) != 0);
          sc.getNextToken();
        } else if ((sc.argc == 2) && sc.argv[1].equalsIgnoreCase("far")) {
          spell.setIsFar(Integer.parseInt(sc.argv[0]) != 0);
          sc.getNextToken();
        } else if (isBookmark()) {
          bm = parseBookmark(null);
        } else if (sc.isBlock && sc.argv[0].equals("KOMPONENTEN")) {
          final Map<String, String> map = new LinkedHashMap<String, String>();
          sc.getNextToken(); // skip KOMPONENTEN

          while (!sc.eof && !sc.isBlock && (sc.argc == 2)) {
            map.put(sc.argv[1], sc.argv[0]);
            sc.getNextToken();
          }

          spell.setComponents(map);
        } else if (sc.isBlock) {
          break;
        } else {
          unknown("ZAUBER", true);
        }
      }

      if (spell.getName() == null) {
        CRParser.log.warn("found spell without name:" + spell.toString());
      } else {
        Spell sp;
        world.addSpell(sp = spell.construct());
        if (bm != null) {
          bm.setObject(sp);
          world.addBookmark(bm.getBookmark());
        }
      }
    }
  }

  /**
   * Parse a sequence of potion (TRANK) blocks.
   *
   * @throws IOException if the scanner throws an IOException
   */
  private void parsePotions() throws IOException {
    while (!sc.eof && sc.isBlock && sc.argv[0].startsWith("TRANK ")) {
      final IntegerID id = IntegerID.create(sc.argv[0].substring(6));
      Potion potion = world.getPotion(id);

      if (potion == null) {
        potion = MagellanFactory.createPotion(id);
        world.addPotion(potion);
      }

      sc.getNextToken(); // skip TRANK nr

      while (!sc.eof) {
        if ((sc.argc == 2) && sc.argv[1].equalsIgnoreCase("Name")) {
          potion.setName(sc.argv[0]);
          sc.getNextToken();
        } else if ((sc.argc == 2) && sc.argv[1].equalsIgnoreCase("Stufe")) {
          potion.setLevel(Integer.parseInt(sc.argv[0]));
          sc.getNextToken();
        } else if ((sc.argc == 2) && sc.argv[1].equalsIgnoreCase("Beschr")) {
          potion.setDescription(sc.argv[0]);
          sc.getNextToken();
        } else if (sc.isBlock && sc.argv[0].equals("ZUTATEN")) {
          sc.getNextToken(); // skip ZUTATEN block

          while (!sc.eof && !sc.isBlock && (sc.argc == 1)) {
            final ItemType it = world.getRules().getItemType(StringID.create(sc.argv[0]), true);
            final Item i = new Item(it, 1);
            potion.addIngredient(i);
            sc.getNextToken();
          }
        } else if (sc.isBlock) {
          break;
        } else {
          unknown("TRANK", true);
        }
      }
    }
  }

  /**
   * Parse a sequence of island blocks.
   *
   * @throws IOException if the scanner throws an IOException
   */
  private void parseIslands() throws IOException {
    while (!sc.eof && sc.isBlock && sc.argv[0].startsWith("ISLAND ")) {
      // ID id = StringID.create(sc.argv[0].substring(7));
      final IntegerID id = IntegerID.create(sc.argv[0].substring(7));
      Island island = world.getIsland(id);

      if (island == null) {
        island = MagellanFactory.createIsland(id, world);
        world.addIsland(island);
      }

      sc.getNextToken(); // skip ISLAND nr

      while (!sc.eof) {
        if ((sc.argc == 2) && sc.argv[1].equalsIgnoreCase("name")) {
          island.setName(sc.argv[0]);
        } else if ((sc.argc == 2) && sc.argv[1].equalsIgnoreCase("Beschr")) {
          island.setDescription(sc.argv[0]);
        } else if ((sc.isBlock) && sc.argv[0].equals("ATTRIBUTES")) {
          parseAttributes(island);
        } else if (sc.isBlock) {
          break;
        } else {
          unknown("ISLAND", false);
        }

        sc.getNextToken();
      }
    }
  }

  /**
   * Reads the header and stores the tags in a map.
   *
   * @param in The reader, that will read the file for us.
   * @return a map, that maps all found header tags to their values.
   * @throws IOException If an I/O error occurs
   */
  public synchronized Map<String, Object> readHeader(Reader in) throws java.io.IOException {
    final Map<String, Object> map = new LinkedHashMap<String, Object>();
    sc = new Scanner(in);
    sc.getNextToken();

    if (!sc.argv[0].startsWith("VERSION ")) {
      CRParser.log.warn("CRParser.readHeader(): CR doesn't start with VERSION block.");

      return map;
    }
    try {
      map.put("_version_", Integer.parseInt(sc.argv[0].substring(sc.argv[0].indexOf(' ')).trim()));
    } catch (final Exception exc) {
      CRParser.log.warn("CRParser.readHeader(): Failed to parse  VERSION number. (setting 0)");
      CRParser.log.warn(exc.toString());
      map.put("_version_", Integer.valueOf(0));
    }

    /*
     * Now read as long as the file lasts, or until a new block is found, which will terminate the
     * header.
     */
    sc.getNextToken();

    while (!sc.eof && !sc.isBlock) {
      if (sc.argc == 2) {
        map.put(sc.argv[1], sc.argv[0]);
      } else {
        CRParser.log.warn("CRParser.readHeader(): Malformed tag on line " + sc.lnr);
      }

      sc.getNextToken();
    }

    return map;
  }

  /**
   * Handle the header, i.e.: <code>
   * VERSION 37
   * "Eressea";Spiel
   * "Standard";Konfiguration
   * "Hex";Koordinaten
   * 36;Basis
   * 1;Umlaute
   * </code>
   *
   * @throws IOException If an I/O error occurs
   */
  private void parseHeader() throws IOException {
    Region specialRegion = null;
    int factionSortIndex = 0;
    int regionSortIndex = 0;
    final int blankPos = sc.argv[0].indexOf(' ');

    if (blankPos > 0) {
      version = Integer.parseInt(sc.argv[0].substring(blankPos).trim());
      world.version = version;
    } else {
      version = 0;
    }

    sc.getNextToken(); // skip "VERSION xx"

    while (!sc.eof) {
      if ((sc.argc == 2) && sc.argv[1].equalsIgnoreCase("Spiel")) {
        game = sc.argv[0];
        sc.getNextToken();
      } else if ((sc.argc == 2) && sc.argv[1].equalsIgnoreCase("Konfiguration")) {
        configuration = sc.argv[0];
        sc.getNextToken();
      } else if ((sc.argc == 2) && sc.argv[1].equalsIgnoreCase("Koordinaten")) {
        coordinates = sc.argv[0];
        sc.getNextToken();
      } else if ((sc.argc == 2) && sc.argv[1].equalsIgnoreCase("Basis")) {
        try {
          world.base = Integer.parseInt(sc.argv[0]);
        } catch (final NumberFormatException e) {
          world.base = 0;
        }

        if ((world.base <= 0) || (world.base > 36)) {
          CRParser.log.warn("invalid base: " + world.base);
          world.base = 10;
        }
        /**
         * assuming we have already the gamename we can make an additional check Buck Tracking wrong
         * base...
         */
        if (world.getGameName() != null) {
          final String actGameName = world.getGameName().toLowerCase();
          if ((actGameName.indexOf("eressea") > -1 || actGameName.indexOf("vinyambar") > -1)
              && (world.base != 36)) {
            // this should not happen
            CRParser.log
                .error("BASE ERROR !! read report could have not base36 !! Changed to base36.");
            world.base = 36;
          }
        }

        // com.eressea.util.IDBaseConverter.setBase(world.base);
        sc.getNextToken();
      } else if ((sc.argc == 2) && sc.argv[1].equalsIgnoreCase("Umlaute")) {
        umlauts = Integer.parseInt(sc.argv[0]) != 0;
        sc.getNextToken();
      } else if ((sc.argc == 2) && sc.argv[1].equalsIgnoreCase("curTempID")) {
        try {
          world.setCurTempID(Integer.parseInt(sc.argv[0]));
        } catch (final NumberFormatException nfe) {
          CRParser.log.warn("Error: Illegal Number format in line " + sc.lnr + ": " + sc.argv[0]);
          CRParser.log.warn("Setting the corresponding value GameData.curTempID to default value!");
          world.setCurTempID(-1);
        }

        sc.getNextToken();
      } else if ((sc.argc == 2) && sc.argv[1].equalsIgnoreCase("Runde")) {
        final Date d = world.getDate();

        if (d == null) {
          world.setDate(new EresseaDate(Integer.parseInt(sc.argv[0])));
        } else {
          d.setDate(Integer.parseInt(sc.argv[0]));
        }

        sc.getNextToken();
      } else if ((sc.argc == 2) && sc.argv[1].equalsIgnoreCase("Zeitalter")) {
        EresseaDate d = (EresseaDate) world.getDate();

        if (d == null) {
          d = new EresseaDate(0);
          d.setEpoch(Integer.parseInt(sc.argv[0]));
          world.setDate(d);
        } else {
          d.setEpoch(Integer.parseInt(sc.argv[0]));
        }

        sc.getNextToken();
      } else if ((sc.argc == 2) && sc.argv[1].equalsIgnoreCase("locale")) {
        world.setLocale(new Locale(sc.argv[0], ""));
        sc.getNextToken();
      } else if ((sc.argc == 2) && sc.argv[1].equalsIgnoreCase("date")) {
        world.setTimestamp(Long.parseLong(sc.argv[0]));
        sc.getNextToken();
      } else if ((sc.argc == 2) && sc.argv[1].equalsIgnoreCase("noskillpoints")) {
        world.noSkillPoints = (Integer.parseInt(sc.argv[0]) != 0);
        sc.getNextToken();
      } else if ((sc.argc == 2) && sc.argv[1].equalsIgnoreCase("mailcmd")) {
        world.mailSubject = sc.argv[0];
        sc.getNextToken();
      } else if ((sc.argc == 2) && sc.argv[1].equalsIgnoreCase("mailto")) {
        world.mailTo = sc.argv[0];
        sc.getNextToken();
      } else if ((sc.argc == 2) && sc.argv[1].equalsIgnoreCase("Build")) {
        world.build = sc.argv[0];
        sc.getNextToken();
      } else if ((sc.argc == 2) && sc.argv[1].equalsIgnoreCase("max_units")) {
        world.maxUnits = Integer.parseInt(sc.argv[0]);
        sc.getNextToken();
      } else if ((sc.argc == 2) && sc.argv[1].equalsIgnoreCase("reportowner")) {
        if (world.getOwnerFaction() == null && !configuration.equals("Standard")) {
          world.setOwnerFaction(EntityID.createEntityID(Integer.parseInt(sc.argv[0]), world.base));
        }
        sc.getNextToken();
      } else if ((sc.argc == 1) && sc.argv[0].startsWith("COORDTRANS")) {
        parseCoordinateTransformation();
      } else if ((sc.argc == 1) && sc.argv[0].startsWith("RULES")) {
        parseRules(world.getRules());
      } else if ((sc.argc == 1) && sc.argv[0].startsWith("HOTSPOT ")) {
        parseHotSpot(world);
      } else if ((sc.argc == 1) && sc.argv[0].startsWith("ALLIANCE ")) {
        parseAlliance2();
      } else if ((sc.argc == 1) && sc.argv[0].startsWith("PARTEI ")) {
        parseFaction(factionSortIndex++);
      } else if ((sc.argc == 1) && sc.argv[0].startsWith("ZAUBER ")) {
        parseSpells();
      } else if ((sc.argc == 1) && sc.argv[0].startsWith("TRANK ")) {
        parsePotions();
      } else if ((sc.argc == 1) && sc.argv[0].startsWith("ISLAND ")) {
        parseIslands();
      } else if ((sc.argc == 1) && sc.argv[0].startsWith("REGION ")) {
        parseRegion(regionSortIndex++);
      } else if ((sc.argc == 1) && sc.argv[0].startsWith("SPEZIALREGION ")) {
        specialRegion = parseSpecialRegion(specialRegion);
      } else if ((sc.argc == 1) && sc.argv[0].equals("MESSAGETYPES")) {
        parseMessageTypes(world);
      } else if ((sc.argc == 1) && sc.argv[0].startsWith("MESSAGETYPE ")) {
        parseMessageType(world);
      } else if ((sc.argc == 1) && sc.argv[0].equals("TRANSLATION")) {
        parseTranslation(world);
      } else if ((sc.argc == 1) && sc.argv[0].equals("ATTRIBUTES")) {
        parseAttributes(world);
      } else if ((sc.argc == 2) && sc.argv[1].equalsIgnoreCase("CHARSET")) {
        // do nothing
        world.setEncoding(sc.argv[0]);
        sc.getNextToken();
      } else {
        if (sc.isBlock) {
          parseUnknownBlock();
        } else {
          unknown("VERSION", true);
        }
      }
    }
  }

  private void parseMagellan(Rules rules) throws IOException {
    sc.getNextToken(); // skip MAGELLAN

    while (!sc.eof) {
      if ((sc.argc == 2) && sc.argv[1].equalsIgnoreCase("class")) {
        rules.setGameSpecificStuffClassName(sc.argv[0]);
        sc.getNextToken();
      } else if ((sc.argc == 2) && sc.argv[1].equalsIgnoreCase("orderFileStartingString")) {
        rules.setOrderfileStartingString(sc.argv[0]);
        sc.getNextToken();
      } else if (sc.isBlock) {
        break;
      } else {
        unknown("MAGELLAN", true);
      }
    }
  }

  private void parseRace(Rules rules) throws IOException {
    final int f = sc.argv[0].indexOf("\"", 0);
    final int t = sc.argv[0].indexOf("\"", f + 1);
    final String id = sc.argv[0].substring(f + 1, t);
    final Race race = rules.getRace(StringID.create(id), true);
    race.setName(id);
    sc.getNextToken(); // skip RACE xx

    while (!sc.eof) {
      if ((sc.argc == 2) && sc.argv[1].equalsIgnoreCase("recruitmentcosts")) {
        race.setRecruitmentCosts(Integer.parseInt(sc.argv[0]));
        sc.getNextToken();
      } else if ((sc.argc == 2) && sc.argv[1].equalsIgnoreCase("recruitmentfactor")) {
        race.setRecruitmentFactor(Integer.parseInt(sc.argv[0]));
        sc.getNextToken();
      } else if ((sc.argc == 2) && sc.argv[1].equalsIgnoreCase("maintenance")) {
        race.setMaintenance(Integer.parseInt(sc.argv[0]));
        sc.getNextToken();
      } else if ((sc.argc == 2) && sc.argv[1].equalsIgnoreCase("name")) {
        race.setName(sc.argv[0]);
        sc.getNextToken();
      } else if ((sc.argc == 2) && sc.argv[1].equalsIgnoreCase("weight")) {
        race.setWeight(Float.parseFloat(sc.argv[0]));
        sc.getNextToken();
      } else if ((sc.argc == 2) && sc.argv[1].equalsIgnoreCase("capacity")) {
        race.setCapacity(Float.parseFloat(sc.argv[0]));
        sc.getNextToken();
      } else if (sc.isBlock && sc.argv[0].equals("TALENTBONI")) {
        parseRaceSkillBonuses(race, rules);
      } else if (sc.isBlock && sc.argv[0].startsWith("TALENTBONI ")) {
        parseRaceTerrainSkillBonuses(race, rules);
      } else if (sc.isBlock && sc.argv[0].equals("SPECIALS")) {
        parseRaceSpecials(race, rules);
      } else if (sc.isBlock) {
        break;
      } else {
        unknown("RACE", true);
      }
    }
  }

  private void parseRaceSkillBonuses(Race race, Rules rules) throws IOException {
    sc.getNextToken(); // skip TALENTBONI

    while (!sc.eof && !sc.isBlock) {
      try {
        final SkillType skillType = rules.getSkillType(StringID.create(sc.argv[1]), true);
        race.setSkillBonus(skillType, Integer.parseInt(sc.argv[0]));
      } catch (final NumberFormatException e) {
        CRParser.log.warn("CRParser.parseRaceSkillBonuses(): in line " + sc.lnr
            + ": unable to convert skill bonus " + sc.argv[0]
            + " to an integer. Ignoring bonus for skill " + sc.argv[1]);
      }

      sc.getNextToken();
    }
  }

  private void parseRaceTerrainSkillBonuses(Race race, Rules rules) throws IOException {
    final int f = sc.argv[0].indexOf("\"", 0);
    final int t = sc.argv[0].indexOf("\"", f + 1);
    final String id = sc.argv[0].substring(f + 1, t);
    final RegionType rType = rules.getRegionType(StringID.create(id), true);
    sc.getNextToken(); // skip TALENTBONI

    while (!sc.eof && !sc.isBlock) {
      try {
        final SkillType skillType = rules.getSkillType(StringID.create(sc.argv[1]), true);
        race.setSkillBonus(skillType, rType, Integer.parseInt(sc.argv[0]));
      } catch (final NumberFormatException e) {
        CRParser.log.warn("CRParser.parseRaceTerrainSkillBonuses(): in line " + sc.lnr
            + ": unable to convert skill bonus " + sc.argv[0]
            + " to an integer. Ignoring bonus for skill " + sc.argv[1]);
      }

      sc.getNextToken();
    }
  }

  private void parseRaceSpecials(Race race, Rules rules) throws IOException {
    sc.getNextToken(); // skip SPECIALS

    while (!sc.eof) {
      if ((sc.argc == 2) && sc.argv[1].equalsIgnoreCase("shiprange")) {
        race.setAdditiveShipBonus(Integer.parseInt(sc.argv[0]));
        sc.getNextToken();
      } else if (sc.isBlock) {
        break;
      } else {
        unknown("RACE/SPECIALS", true);
      }
    }

  }

  /**
   * Parses the ITEM and HERB blocks in the game specific rules CR file.
   *
   * @param rules
   * @throws IOException
   */

  private void parseItemType(Rules rules) throws IOException {
    final int f = sc.argv[0].indexOf("\"", 0);
    final int t = sc.argv[0].indexOf("\"", f + 1);
    final String id = sc.argv[0].substring(f + 1, t);
    ItemType itemType = null;

    if (sc.argv[0].startsWith("ITEM ")) {
      itemType = rules.getItemType(StringID.create(id), true);
    } else if (sc.argv[0].startsWith("HERB ")) {
      itemType = rules.getItemType(StringID.create(id), true);
    } else {
      unknown(sc.argv[0], true);
      return;
    }

    itemType.setName(id);

    Skill makeSkill = null;
    sc.getNextToken(); // skip ITEM xx

    while (!sc.eof) {
      if ((sc.argc == 2) && sc.argv[1].equalsIgnoreCase("weight")) {
        itemType.setWeight(Float.parseFloat(sc.argv[0]));
        sc.getNextToken();
      } else if ((sc.argc == 2) && sc.argv[1].equalsIgnoreCase("makeskill")) {
        makeSkill =
            new Skill(rules.getSkillType(StringID.create(sc.argv[0]), true), 0, 0, 0, false);
        itemType.setMakeSkill(makeSkill);
        sc.getNextToken();
      } else if ((sc.argc == 2) && sc.argv[1].equalsIgnoreCase("makeskilllevel")) {
        if (makeSkill == null) {
          unknown(sc.argv[0], true);
          return;
        }
        makeSkill.setLevel(Integer.parseInt(sc.argv[0]));
        sc.getNextToken();
      } else if ((sc.argc == 2) && sc.argv[1].equalsIgnoreCase("name")) {
        itemType.setName(sc.argv[0]);
        sc.getNextToken();
      } else if ((sc.argc == 2) && sc.argv[1].equalsIgnoreCase("category")) {
        final StringID catID = StringID.create(sc.argv[0]);
        final ItemCategory cat = rules.getItemCategory(catID, true);
        itemType.setCategory(cat);
        sc.getNextToken();
      } else if ((sc.argc == 2) && sc.argv[1].equalsIgnoreCase("region")) {
        final StringID regionID = StringID.create(sc.argv[0]);
        rules.getRegionType(regionID, true);
        sc.getNextToken();
      } else if ((sc.argc == 2) && sc.argv[1].equalsIgnoreCase("iconname")) {
        itemType.setIcon(sc.argv[0]);
        sc.getNextToken();
      } else if ((sc.argc == 2) && sc.argv[1].equalsIgnoreCase("useskill")) {
        final Skill useSkill =
            new Skill(rules.getSkillType(StringID.create(sc.argv[0]), true), 0, 1, 0, false);
        itemType.setUseSkill(useSkill);
        sc.getNextToken();
        // darcduck - 20.11.2007 added magic bag tag that indicates if an item can be stored in the
        // magic bag
      } else if ((sc.argc == 2) && sc.argv[1].equalsIgnoreCase("storeinbonw")) {
        itemType.setStoreableInBonw(Integer.parseInt(sc.argv[0]));
        sc.getNextToken();
      } else if ((sc.argc == 2) && sc.argv[1].equalsIgnoreCase("ishorse")) {
        itemType.setHorse(Short.parseShort(sc.argv[0]));
        sc.getNextToken();
      } else if (sc.isBlock && sc.argv[0].equals("RESOURCES")) {
        parseItemTypeResources(itemType, rules);
      } else if (sc.isBlock) {
        break;
      } else {
        unknown("ITEM", true);
      }
    }
  }

  private void parseItemTypeResources(ItemType itemType, Rules rules) throws IOException {
    sc.getNextToken(); // skips the block header

    while (!sc.eof) {
      if (sc.argc == 2) {
        final ItemType component = rules.getItemType(StringID.create(sc.argv[1]), true);
        final Item i = new Item(component, Integer.parseInt(sc.argv[0]));
        itemType.addResource(i);
        sc.getNextToken();
      } else if (sc.isBlock) {
        break;
      } else {
        unknown("RESOURCES", true);
      }
    }
  }

  private void parseSkillType(Rules rules) throws IOException {
    final int f = sc.argv[0].indexOf("\"", 0);
    final int t = sc.argv[0].indexOf("\"", f + 1);
    final String id = sc.argv[0].substring(f + 1, t);
    final SkillType skillType = rules.getSkillType(StringID.create(id), true);
    sc.getNextToken(); // skip SKILL xx

    while (!sc.eof) {
      if ((sc.argc == 2) && sc.argv[1].equalsIgnoreCase("Name")) {
        skillType.setName(sc.argv[0]);
        sc.getNextToken();
      } else if ((sc.argc == 2) && sc.argv[1].equalsIgnoreCase("category")) {
        final StringID catID = StringID.create(sc.argv[0]);
        final SkillCategory cat = rules.getSkillCategory(catID, true);
        skillType.setCategory(cat);
        sc.getNextToken();
      } else if ((sc.argc == 2) && sc.argv[1].equalsIgnoreCase("cost")) {
        try {
          skillType.setCost(Integer.parseInt(sc.argv[0]));
        } catch (final NumberFormatException e) {
          CRParser.log.error(e);
          unknown("SKILL", false);
        }
        sc.getNextToken();
      } else if (sc.isBlock && sc.argv[0].equals("COSTS")) {
        parseSkillCosts(skillType, rules);
      } else if ((sc.argc == 2) && sc.argv[1].equalsIgnoreCase("iconname")) {
        skillType.setIcon(sc.argv[0]);
        sc.getNextToken();
      } else if (sc.isBlock) {
        break;
      } else {
        unknown("SKILL", true);
      }
    }

    if (skillType.getName() == null) {
      skillType.setName(id);
    }
  }

  private void parseSkillCosts(SkillType skillType, Rules rules) throws IOException {
    sc.getNextToken(); // skip COSTS
    while (!sc.eof) {
      if (sc.argc == 2) {
        try {
          skillType.setCost(Integer.parseInt(sc.argv[1]), Integer.parseInt(sc.argv[0]));
        } catch (final NumberFormatException e) {
          CRParser.log.error(e);
          unknown("SKILL", false);
        }
        sc.getNextToken();
      } else if (sc.isBlock) {
        break;
      } else {
        unknown("SKILL", true);
      }
    }
  }

  private void parseShipType(Rules rules) throws IOException {
    final int f = sc.argv[0].indexOf("\"", 0);
    final int t = sc.argv[0].indexOf("\"", f + 1);
    final String id = sc.argv[0].substring(f + 1, t);
    final ShipType shipType = rules.getShipType(StringID.create(id), true);
    shipType.init(rules.getItemType(EresseaConstants.I_WOOD));
    sc.getNextToken(); // skip SHIPTYPE xx

    while (!sc.eof) {
      if ((sc.argc == 2) && sc.argv[1].equalsIgnoreCase("size")) {
        shipType.setMaxSize(Integer.parseInt(sc.argv[0]));
        sc.getNextToken();
      } else if ((sc.argc == 2) && sc.argv[1].equalsIgnoreCase("name")) {
        shipType.setName(sc.argv[0]);
        sc.getNextToken();
      } else if ((sc.argc == 2) && sc.argv[1].equalsIgnoreCase("level")) {
        shipType.setBuildSkillLevel(Integer.parseInt(sc.argv[0]));
        sc.getNextToken();
      } else if ((sc.argc == 2) && sc.argv[1].equalsIgnoreCase("range")) {
        shipType.setRange(Integer.parseInt(sc.argv[0]));
        sc.getNextToken();
      } else if ((sc.argc == 2) && sc.argv[1].equalsIgnoreCase("captainlevel")) {
        shipType.setCaptainSkillLevel(Integer.parseInt(sc.argv[0]));
        sc.getNextToken();
      } else if ((sc.argc == 2) && sc.argv[1].equalsIgnoreCase("sailorlevel")) {
        shipType.setSailorSkillLevel(Integer.parseInt(sc.argv[0]));
        sc.getNextToken();
      } else if ((sc.argc == 2) && sc.argv[1].equalsIgnoreCase("minSailorlevel")) {
        shipType.setMinSailorLevel(Integer.parseInt(sc.argv[0]));
        sc.getNextToken();
      } else if ((sc.argc == 2) && sc.argv[1].equalsIgnoreCase("capacity")) {
        shipType.setCapacity(Integer.parseInt(sc.argv[0]));
        sc.getNextToken();
      } else if ((sc.argc == 2) && sc.argv[1].equalsIgnoreCase("persons")) {
        shipType.setMaxPersons(Integer.parseInt(sc.argv[0]));
        sc.getNextToken();
      } else if ((sc.argc == 2) && sc.argv[1].equalsIgnoreCase("maxsize")) {
        shipType.setMaxSize(Integer.parseInt(sc.argv[0]));
        sc.getNextToken();
        // } else if(sc.isBlock && sc.argv[0].equals("TALENTBONI")) {
        // parseBuildingSkillBonuses(shipType, rules);
      } else if ((sc.argc == 2) && sc.argv[1].equalsIgnoreCase("iconname")) {
        shipType.setIcon(sc.argv[0]);
        sc.getNextToken();
      } else if (sc.isBlock && sc.argv[0].equals("RAWMATERIALS")) {
        parseBuildingRawMaterials(shipType, rules);
      } else if (sc.isBlock && sc.argv[0].equals("MAINTENANCE")) {
        parseBuildingMaintenance(shipType, rules);
      } else if (sc.isBlock) {
        break;
      } else if (sc.isBlock) {
        break;
      } else {
        unknown("SHIPTYPE", true);
      }
    }
  }

  private void parseBuildingType(Rules rules) throws IOException {
    BuildingType bType = null;
    final int f = sc.argv[0].indexOf("\"", 0);
    final int t = sc.argv[0].indexOf("\"", f + 1);
    final String id = sc.argv[0].substring(f + 1, t);
    final String blockName = sc.argv[0].substring(0, sc.argv[0].indexOf(" "));

    if (blockName.equals("BUILDINGTYPE")) {
      bType = rules.getBuildingType(StringID.create(id), true);
    } else if (blockName.equals("CASTLETYPE")) {
      bType = rules.getCastleType(StringID.create(id), true);
      ((CastleType) bType).init(rules.getItemType(EresseaConstants.I_USTONE));
    } else {
      unknown(blockName, false);
      return;
    }

    sc.getNextToken(); // skip GEBÄUDETYP xx

    while (!sc.eof) {
      if ((sc.argc == 2) && sc.argv[1].equalsIgnoreCase("name")) {
        bType.setName(sc.argv[0]);
        sc.getNextToken();
      } else if ((sc.argc == 2) && sc.argv[1].equalsIgnoreCase("level")) {
        bType.setBuildSkillLevel(Integer.parseInt(sc.argv[0]));
        sc.getNextToken();
      } else if ((sc.argc == 2) && sc.argv[1].equalsIgnoreCase("maxsize")) {
        bType.setMaxSize(Integer.parseInt(sc.argv[0]));
        sc.getNextToken();
      } else if ((sc.argc == 2) && sc.argv[1].equalsIgnoreCase("minsize")) {
        if (bType instanceof CastleType) {
          ((CastleType) bType).setMinSize(Integer.parseInt(sc.argv[0]));
        }

        sc.getNextToken();
      } else if ((sc.argc == 2) && sc.argv[1].equalsIgnoreCase("wage")) {
        if (bType instanceof CastleType) {
          ((CastleType) bType).setPeasantWage(Integer.parseInt(sc.argv[0]));
        }

        sc.getNextToken();
      } else if ((sc.argc == 2) && sc.argv[1].equalsIgnoreCase("tradetax")) {
        if (bType instanceof CastleType) {
          ((CastleType) bType).setTradeTax(Integer.parseInt(sc.argv[0]));
        }

        sc.getNextToken();
      } else if ((sc.argc == 2) && sc.argv[1].equalsIgnoreCase("iconname")) {
        bType.setIcon(sc.argv[0]);
        sc.getNextToken();
      } else if ((sc.argc == 2) && sc.argv[1].equalsIgnoreCase("maintainedbyregionowner")) {
        bType.setMaintendByRegionOwner(Integer.parseInt(sc.argv[0]) != 0);
        sc.getNextToken();
      } else if (sc.isBlock && sc.argv[0].equals("TALENTBONI")) {
        parseBuildingSkillBonuses(bType, rules);
      } else if (sc.isBlock && sc.argv[0].equals("RAWMATERIALS")) {
        parseBuildingRawMaterials(bType, rules);
      } else if (sc.isBlock && sc.argv[0].equals("MAINTENANCE")) {
        parseBuildingMaintenance(bType, rules);
      } else if (sc.isBlock && sc.argv[0].equals("REGIONTYPES")) {
        parseBuildingTerrain(bType, rules);
      } else if (sc.isBlock) {
        break;
      } else {
        unknown(blockName, true);
      }
    }
  }

  private void parseBuildingSkillBonuses(BuildingType bType, Rules rules) throws IOException {
    sc.getNextToken(); // skip TALENTBONI

    while (!sc.eof && !sc.isBlock) {
      try {
        final SkillType skillType = rules.getSkillType(StringID.create(sc.argv[1]), true);
        bType.setSkillBonus(skillType, Integer.parseInt(sc.argv[0]));
      } catch (final NumberFormatException e) {
        CRParser.log.warn("CRParser.parseBuildingSkillBonuses(): in line " + sc.lnr
            + ": unable to convert skill bonus " + sc.argv[0]
            + " to an integer. Ignoring bonus for skill " + sc.argv[1]);
      }

      sc.getNextToken();
    }
  }

  private void parseBuildingRawMaterials(ConstructibleType bType, Rules rules) throws IOException {
    sc.getNextToken(); // skip RAWMATERIALS

    while (!sc.eof && !sc.isBlock) {
      try {
        final ItemType itemType = rules.getItemType(StringID.create(sc.argv[1]), true);
        final Item i = new Item(itemType, Integer.parseInt(sc.argv[0]));
        bType.addRawMaterial(i);
      } catch (final NumberFormatException e) {
        CRParser.log.warn("CRParser.parseBuildingRawMaterials(): in line " + sc.lnr
            + ": unable to convert item amount " + sc.argv[0]
            + " to an integer. Ignoring amount for item " + sc.argv[1]);
      }

      sc.getNextToken();
    }
  }

  private void parseBuildingMaintenance(ConstructibleType bType, Rules rules) throws IOException {
    sc.getNextToken(); // skip MAINTENANCE

    while (!sc.eof && !sc.isBlock) {
      try {
        final ItemType itemType = rules.getItemType(StringID.create(sc.argv[1]), true);
        final Item i = new Item(itemType, Integer.parseInt(sc.argv[0]));
        bType.addMaintenance(i);
      } catch (final NumberFormatException e) {
        CRParser.log.warn("CRParser.parseBuildingMaintenance(): in line " + sc.lnr
            + ": unable to convert item amount " + sc.argv[0]
            + " to an integer. Ignoring amount for item " + sc.argv[1]);
      }

      sc.getNextToken();
    }
  }

  private void parseBuildingTerrain(BuildingType bType, Rules rules) throws IOException {
    sc.getNextToken(); // skip MAINTENANCE

    while (!sc.eof && !sc.isBlock) {
      final RegionType t = rules.getRegionType(StringID.create(sc.argv[0]), true);
      bType.addRegionType(t);
      sc.getNextToken();
    }
  }

  private void parseRegionType(Rules rules) throws IOException {
    final int f = sc.argv[0].indexOf("\"", 0);
    final int t = sc.argv[0].indexOf("\"", f + 1);
    final StringID id = StringID.create(sc.argv[0].substring(f + 1, t));
    final RegionType regionType = rules.getRegionType(id, true);
    sc.getNextToken(); // skip REGIONSTYP xx

    while (!sc.eof && !sc.isBlock) {
      if ((sc.argc == 2) && sc.argv[1].equalsIgnoreCase("maxworkers")) {
        regionType.setInhabitants(Integer.parseInt(sc.argv[0]));
        sc.getNextToken();
      } else if ((sc.argc == 2) && sc.argv[1].equalsIgnoreCase("name")) {
        regionType.setName(sc.argv[0]);
        sc.getNextToken();
      } else if ((sc.argc == 2) && sc.argv[1].equalsIgnoreCase("roadstones")) {
        final Resource resource = new Resource(Integer.parseInt(sc.argv[0]));
        resource.setObjectType(rules.getItemType(EresseaConstants.I_USTONE, true));
        regionType.addRoadResource(resource);
        sc.getNextToken();
      } else if ((sc.argc == 2) && sc.argv[1].equalsIgnoreCase("roadsupportbuilding")) {
        final Resource resource = new Resource();
        resource.setObjectType(rules.getBuildingType(StringID.create(sc.argv[0]), true));
        regionType.addRoadResource(resource);
        sc.getNextToken();
      } else if ((sc.argc == 2) && sc.argv[1].equalsIgnoreCase("isOcean")) {
        regionType.setIsOcean(sc.argv[0].equals("true"));
        sc.getNextToken();
      } else if ((sc.argc == 2) && sc.argv[1].equalsIgnoreCase("isLand")) {
        regionType.setLand(sc.argv[0].equals("true"));
        sc.getNextToken();
      } else if ((sc.argc == 2) && sc.argv[1].equalsIgnoreCase("isAstralVisible")) {
        regionType.setAstralVisible(sc.argv[0].equals("true"));
        sc.getNextToken();
      } else if ((sc.argc == 2) && sc.argv[1].equalsIgnoreCase("peasantWage")) {
        regionType.setPeasantWage(Integer.parseInt(sc.argv[0]));
        sc.getNextToken();
      } else if ((sc.argc == 2) && sc.argv[1].equalsIgnoreCase("iconname")) {
        regionType.setIcon(sc.argv[0]);
        sc.getNextToken();
      } else if (sc.isBlock) {
        break;
      } else {
        unknown("REGIONTYPE", true);
      }
    }
  }

  private void parseItemCategory(Rules rules) throws IOException {
    final int f = sc.argv[0].indexOf("\"", 0);
    final int t = sc.argv[0].indexOf("\"", f + 1);
    final StringID id = StringID.create(sc.argv[0].substring(f + 1, t));
    final ItemCategory cat = rules.getItemCategory(id, true);

    sc.getNextToken(); // skip ITEMCATEGORY xx

    while (!sc.eof) {
      if ((sc.argc == 2) && sc.argv[1].equalsIgnoreCase("name")) {
        cat.setName(sc.argv[0]);
        sc.getNextToken();
      } else if ((sc.argc == 2) && sc.argv[1].equalsIgnoreCase("naturalorder")) {
        cat.setSortIndex(Integer.parseInt(sc.argv[0]));
        sc.getNextToken();
      } else if ((sc.argc == 2) && sc.argv[1].equalsIgnoreCase("parent")) {
        final ItemCategory parent = rules.getItemCategory(StringID.create(sc.argv[0]), false);
        cat.setParent(parent);
        sc.getNextToken();
      } else if ((sc.argc == 2) && sc.argv[1].equalsIgnoreCase("iconname")) {
        cat.setIconName(sc.argv[0]);
        sc.getNextToken();
      } else if (sc.isBlock) {
        break;
      } else {
        unknown("ITEMCATEGORY", true);
      }
    }
  }

  private void parseSkillCategory(Rules rules) throws IOException {
    final int f = sc.argv[0].indexOf("\"", 0);
    final int t = sc.argv[0].indexOf("\"", f + 1);
    final StringID id = StringID.create(sc.argv[0].substring(f + 1, t));
    final SkillCategory cat = rules.getSkillCategory(id, true);

    sc.getNextToken(); // skip SKILLCATEGORY xx

    while (!sc.eof) {
      if ((sc.argc == 2) && sc.argv[1].equalsIgnoreCase("name")) {
        cat.setName(sc.argv[0]);
      } else if ((sc.argc == 2) && sc.argv[1].equalsIgnoreCase("naturalorder")) {
        cat.setSortIndex(Integer.parseInt(sc.argv[0]));
      } else if ((sc.argc == 2) && sc.argv[1].equalsIgnoreCase("parent")) {
        final SkillCategory parent = rules.getSkillCategory(StringID.create(sc.argv[0]), false);
        cat.setParent(parent);
      } else if (sc.isBlock) {
        break;
      } else {
        unknown("SKILLCATEGORY", false);
      }

      sc.getNextToken();
    }
  }

  /**
   * Read a ruleset from a specified file.
   *
   * @throws IOException If an I/O error occurs
   */
  public Rules readRules(FileType filetype) throws IOException {
    return readRules(filetype.createReader());
  }

  /**
   * Reads a Rules object from an input Reader.
   *
   * @param in The reader that will read the file for us.
   * @return a Rules object, or <code>null</code>, if the file hasn't been a ruleset.
   * @throws IOException If an I/O error occurs
   */
  private synchronized Rules readRules(Reader in) throws IOException {
    final Rules rules = new GenericRules();
    sc = new Scanner(in);
    sc.getNextToken();

    if (!sc.argv[0].startsWith("VERSION ") || (sc.argc != 1)) {
      CRParser.log.warn("CRParser.readRules(): corrupt rule file missing VERSION on first line.");

      return null;
    }

    if (!sc.eof) {
      sc.getNextToken();

      if (!sc.argv[0].startsWith("RULES ") || (sc.argc != 1)) {
        CRParser.log.warn("CRParser.readRules(): corrupt rule file missing RULE block.");

        return null;
      }
    }

    /*
     * The desired header has been parsed. Continue parsing the subsequent rule blocks until the
     * file ends.
     */
    parseRules(rules);

    return rules;
  }

  /**
   * Parses a rules file and stores the result in <code>rules</code>.
   *
   * @param rules An existing Rules object
   * @throws IOException If an I/O error occurs
   */
  private void parseRules(Rules rules) throws IOException {
    sc.getNextToken(); // skip "RULES"

    while (!sc.eof) {
      if (sc.argv[0].startsWith("MAGELLAN")) {
        parseMagellan(rules);
      } else if (sc.argv[0].startsWith("RACE ")) {
        parseRace(rules);
      } else if (sc.argv[0].startsWith("ITEM ") || sc.argv[0].startsWith("HERB ")) {
        parseItemType(rules);
      } else if (sc.argv[0].startsWith("SHIPTYPE ")) {
        parseShipType(rules);
      } else if (sc.argv[0].startsWith("BUILDINGTYPE ") || sc.argv[0].startsWith("CASTLETYPE ")) {
        parseBuildingType(rules);
      } else if (sc.argv[0].startsWith("REGIONTYPE ")) {
        parseRegionType(rules);
      } else if (sc.argv[0].startsWith("SKILL ")) {
        parseSkillType(rules);
      } else if (sc.argv[0].startsWith("ITEMCATEGORY ")) {
        parseItemCategory(rules);
      } else if (sc.argv[0].startsWith("SKILLCATEGORY ")) {
        parseSkillCategory(rules);
      } else if (sc.argv[0].startsWith("OPTIONCATEGORY ")) {
        parseOptionCategory(rules);
      } else if (sc.argv[0].startsWith("ALLIANCECATEGORY ")) {
        parseAllianceCategory(rules);
      } else if (sc.argv[0].startsWith("ORDER ")) {
        parseOrder(rules);
      } else {
        unknown("RULES", true);
      }
    }
  }

  private void parseCoordinateTransformation() throws IOException {
    final EntityID id =
        EntityID.createEntityID(Integer.parseInt(sc.argv[0].substring("COORDTRANS".length() + 1)),
            world.base);
    sc.getNextToken();

    while (!sc.eof) {
      if ((sc.argc == 2) && sc.argv[1].equalsIgnoreCase("translation")) {
        CoordinateID translation = CoordinateID.parse(sc.argv[0], " ");
        // first mirror the translation at the origin (of the correct layer!), then translate, then
        // mirror again:
        translation = transformTranslation(translation);
        world.setCoordinateTranslation(id, translation);
        sc.getNextToken();
      } else if (!sc.isBlock) {
        unknown("COORDTRANS", true);
      } else {
        break;
      }
    }
  }

  private void parseOptionCategory(Rules rules) throws IOException {
    final int f = sc.argv[0].indexOf("\"", 0);
    final int t = sc.argv[0].indexOf("\"", f + 1);
    final StringID id = StringID.create(sc.argv[0].substring(f + 1, t));
    final OptionCategory opt = rules.getOptionCategory(id, true);
    sc.getNextToken(); // skip OPTIONCATEGORY xx

    while (!sc.eof) {
      if ((sc.argc == 2) && sc.argv[1].equalsIgnoreCase("name")) {
        opt.setName(sc.argv[0]);
        // sc.getNextToken();
      } else if ((sc.argc == 2) && sc.argv[1].equalsIgnoreCase("order")) {
        opt.setOrder(sc.argv[0].equals("true"));
        // sc.getNextToken();
      } else if ((sc.argc == 2) && sc.argv[1].equalsIgnoreCase("bitmask")) {
        // alt (Fiete)
        // opt.setBitMask(1 << Integer.parseInt(sc.argv[0]));
        // neu (Fiete)
        opt.setBitMask(Integer.parseInt(sc.argv[0]));
        // sc.getNextToken();
      } else if (sc.isBlock) {
        break;
      } else {
        unknown("OPTIONCATEGORY", true);
      }

      sc.getNextToken();
    }
  }

  private void parseAllianceCategory(Rules rules) throws IOException {
    final int f = sc.argv[0].indexOf("\"", 0);
    final int t = sc.argv[0].indexOf("\"", f + 1);
    final StringID id = StringID.create(sc.argv[0].substring(f + 1, t));
    final AllianceCategory cat = rules.getAllianceCategory(id, true);
    sc.getNextToken(); // skip ALLIANCECATEGORY xx

    while (!sc.eof) {
      if ((sc.argc == 2) && sc.argv[1].equalsIgnoreCase("name")) {
        cat.setName(sc.argv[0]);
      } else if ((sc.argc == 2) && sc.argv[1].equalsIgnoreCase("parent")) {
        final AllianceCategory parent =
            rules.getAllianceCategory(StringID.create(sc.argv[0]), false);
        cat.setParent(parent);
      } else if ((sc.argc == 2) && sc.argv[1].equalsIgnoreCase("bitmask")) {
        cat.setBitMask(Integer.parseInt(sc.argv[0]));
      } else if (!sc.isBlock) {
        unknown("ALLIANCECATEGORY", true);
      } else {
        break;
      }

      sc.getNextToken();
    }
  }

  private void parseOrder(Rules rules) throws IOException {
    try {
      final int f = sc.argv[0].indexOf("\"", 0);
      final int t = sc.argv[0].indexOf("\"", f + 1);
      final StringID id = StringID.create(sc.argv[0].substring(f + 1, t));
      final OrderType ord = rules.getOrder(id, true);
      sc.getNextToken(); // skip ORDER xx

      while (!sc.eof) {
        if ((sc.argc == 2) && sc.argv[1].equalsIgnoreCase("syntax")) {
          ord.setSyntax(sc.argv[0]);
        } else if ((sc.argc == 2) && sc.argv[1].equalsIgnoreCase("internal")) {
          ord.setInternal(sc.argv[0].equals("1"));
        } else if ((sc.argc == 2) && sc.argv[1].equalsIgnoreCase("active")) {
          ord.setActive(sc.argv[0].equals("1"));
        } else if ((sc.argc == 2) && sc.argv[1].startsWith("locale_")) {
          ord.addName(new Locale(sc.argv[1].substring("locale_".length())), sc.argv[0]);
        } else if (!sc.isBlock) {
          unknown("ORDER", true);
        } else {
          break;
        }

        sc.getNextToken();
      }
    } catch (RuntimeException e) {
      throw e;
    }
  }

  /**
   * This is the new version, the old is called "ALLIERTE" Heuristic for end of block detection:
   * There are no subblocks in one ALLIANZ block.
   *
   * @param allies A map with existing alliances or <code>null</code>.
   * @return The (modified) <code>allies</code> map
   * @throws IOException If an I/O error occurs
   */
  private Map<EntityID, Alliance> parseAlliance(Map<EntityID, Alliance> allies) throws IOException {
    if (allies == null) {
      allies = CollectionFactory.<EntityID, Alliance> createSyncOrderedMap();
    }

    final EntityID id =
        EntityID.createEntityID(Integer.parseInt(sc.argv[0].substring(8)), world.base);
    sc.getNextToken();

    int state = -1;

    while (!sc.eof) {
      if ((sc.argc == 2) && sc.argv[1].equalsIgnoreCase("Parteiname")) {
        sc.getNextToken();
      } else if ((sc.argc == 2) && sc.argv[1].equalsIgnoreCase("Status")) {
        state = Integer.parseInt(sc.argv[0]);
        sc.getNextToken();
      } else if (!sc.isBlock) {
        unknown("ALLIANZ", true);
      } else {
        break;
      }
    }

    if (state != -1) {
      final Faction faction = getAddFaction(id);
      final Alliance alliance = new Alliance(faction, state);
      allies.put(faction.getID(), alliance);
    }

    return allies;
  }

  /*
   * This is the new version, the old is called "ALLIERTE" Heuristic for end of block detection:
   * There are no subblocks in one ALLIANZ block.
   */
  private AllianceGroup parseAlliance2() throws IOException {
    final EntityID id =
        EntityID.createEntityID(Integer.parseInt(sc.argv[0].substring(9)), world.base);
    final AllianceGroup alliance = new AllianceGroup(id);
    sc.getNextToken();

    while (!sc.eof) {
      if ((sc.argc == 2) && sc.argv[1].equalsIgnoreCase("name")) {
        alliance.setName(sc.argv[0]);
        sc.getNextToken();
      } else if ((sc.argc == 2) && sc.argv[1].equalsIgnoreCase("leader")) {
        alliance.setLeader(EntityID.createEntityID(Integer.parseInt(sc.argv[0]), world.base));
        sc.getNextToken();
      } else if (!sc.isBlock) {
        unknown("ALLIANZ", true);
      } else {
        break;
      }
    }

    world.addAllianceGroup(alliance);

    return alliance;
  }

  /**
   * NOT IMPLEMENTED YET This is the old "ALLIERTE" version. Heuristic for block termination: -
   * Terminate on any other block This method isn't implemented yet. It skips the entire "ALLIERTE"
   * block.
   */
  private Map<EntityID, Alliance> parseAlliierte() throws IOException {
    final Map<EntityID, Alliance> allies = new LinkedHashMap<EntityID, Alliance>();
    sc.getNextToken(); // skip "ALLIERTE" tag

    while (!sc.eof && !sc.isBlock) {
      sc.getNextToken();
    }

    return allies;
  }

  /**
   * This is the old "ADRESSEN" version. Heuristic for block termination: - Terminate on any other
   * block This method isn't implemented yet. It skips the entire "ADRESSEN" block.
   *
   * @throws IOException If an I/O error occurs
   */
  private void parseAdressen() throws IOException {
    sc.getNextToken(); // skip "ADRESSEN" tag

    while (!sc.eof && !sc.isBlock) {
      sc.getNextToken();
    }
  }

  /**
   * Parse the FACTION block with all its subblocks. Heuristic for block termination:<br />
   * - Terminate on another PARTEI block (without warning)<br />
   * - Terminate on another id block (without warning)<br />
   * - Terminate on any other unknown block (with warning)
   *
   * @param sortIndex
   * @return The resulting faction
   * @throws IOException If an I/O error occurs
   */
  private Faction parseFaction(int sortIndex) throws IOException {
    Race type = null;
    int raceRecruit = -1;
    int groupSortIndex = 0;
    final EntityID id =
        EntityID.createEntityID(Integer.parseInt(sc.argv[0].substring(7)), world.base);
    sc.getNextToken(); // skip PARTEI nr

    final Faction faction = getAddFaction(id);
    faction.setSortIndex(sortIndex);

    if (firstFaction == null) {
      firstFaction = faction;
    }

    while (!sc.eof && !sc.argv[0].startsWith("PARTEI ")) {
      if ((sc.argc == 2) && sc.argv[1].equalsIgnoreCase("Runde")) {
        final magellan.library.rules.Date d = world.getDate();

        if (d == null) {
          world.setDate(new EresseaDate(Integer.parseInt(sc.argv[0])));
        } else {
          d.setDate(Integer.parseInt(sc.argv[0]));
        }

        sc.getNextToken();
      } else if ((sc.argc == 2) && sc.argv[1].equalsIgnoreCase("Passwort")) {
        // faction.password = sc.argv[0];
        sc.getNextToken();
      } else if ((sc.argc == 2) && sc.argv[1].equalsIgnoreCase("Optionen")) {
        if (faction.getOptions() == null) {
          faction.setOptions(new Options(world.getRules()));
        }

        faction.getOptions().setValues(Integer.parseInt(sc.argv[0]));
        sc.getNextToken();
      } else if ((sc.argc == 2) && sc.argv[1].equalsIgnoreCase("Punkte")) {
        faction.setScore(Integer.parseInt(sc.argv[0]));
        sc.getNextToken();
      } else if ((sc.argc == 2) && sc.argv[1].equalsIgnoreCase("Punktedurchschnitt")) {
        faction.setAverageScore(Integer.parseInt(sc.argv[0]));
        sc.getNextToken();
      } else if ((sc.argc == 2) && sc.argv[1].equalsIgnoreCase("email")) {
        faction.setEmail(sc.argv[0]);
        sc.getNextToken();
      } else if ((sc.argc == 2) && sc.argv[1].equalsIgnoreCase("banner")) {
        faction.setDescription(sc.argv[0]);
        sc.getNextToken();
      } else if ((sc.argc == 2)
          && (sc.argv[1].equalsIgnoreCase("Typ") || sc.argv[1].equalsIgnoreCase("Typus")
              || sc.argv[1]
                  .equalsIgnoreCase("race"))) {
        type = world.getRules().getRace(StringID.create(sc.argv[0]), true);
        faction.setType(type);
        sc.getNextToken();
      } else if ((sc.argc == 2) && sc.argv[1].equalsIgnoreCase("Rekrutierungskosten")) {
        raceRecruit = Integer.parseInt(sc.argv[0]);
        sc.getNextToken();
      } else if ((sc.argc == 2) && sc.argv[1].equalsIgnoreCase("Schatz")) {
        faction.setTreasury(Integer.parseInt(sc.argv[0]));
        sc.getNextToken();
      } else if ((sc.argc == 2) && sc.argv[1].equalsIgnoreCase("Anzahl Personen")) {
        faction.setPersons(Integer.parseInt(sc.argv[0]));
        sc.getNextToken();
      } else if ((sc.argc == 2) && sc.argv[1].equalsIgnoreCase("Anzahl Immigranten")) {
        faction.setMigrants(Integer.parseInt(sc.argv[0]));
        sc.getNextToken();
      } else if ((sc.argc == 2) && sc.argv[1].equalsIgnoreCase("Max. Immigranten")) {
        faction.setMaxMigrants(Integer.parseInt(sc.argv[0]));
        sc.getNextToken();
      } else if ((sc.argc == 2) && sc.argv[1].equalsIgnoreCase("heroes")) {
        faction.setHeroes(Integer.parseInt(sc.argv[0]));
        sc.getNextToken();
      } else if ((sc.argc == 2) && sc.argv[1].equalsIgnoreCase("max_heroes")) {
        faction.setMaxHeroes(Integer.parseInt(sc.argv[0]));
        sc.getNextToken();
      } else if ((sc.argc == 2) && sc.argv[1].equalsIgnoreCase("age")) {
        faction.setAge(Integer.parseInt(sc.argv[0]));
        sc.getNextToken();
      } else if ((sc.argc == 2) && sc.argv[1].equalsIgnoreCase("Parteiname")) {
        faction.setName(sc.argv[0]);
        sc.getNextToken();
      } else if ((sc.argc == 2) && sc.argv[1].equalsIgnoreCase("Magiegebiet")) {
        faction.setSpellSchool(sc.argv[0]);
        sc.getNextToken();
      } else if ((sc.argc == 2) && sc.argv[1].equalsIgnoreCase("trustlevel")) {
        faction.setTrustLevel(Integer.parseInt(sc.argv[0]));
        faction.setTrustLevelSetByUser(true);
        sc.getNextToken();
      } else if ((sc.argc == 2) && sc.argv[1].equalsIgnoreCase("locale")) {
        faction.setLocale(new Locale(sc.argv[0], ""));
        sc.getNextToken();
      } else if ((sc.argc == 2) && sc.argv[1].equalsIgnoreCase("typprefix")) {
        faction.setRaceNamePrefix(sc.argv[0]);
        sc.getNextToken();
      } else if ((sc.argc == 2) && sc.argv[1].equalsIgnoreCase("ZAT")) {
        /* Verdanon tag */
        sc.getNextToken();
      } else if ((sc.argc == 2) && sc.argv[1].equalsIgnoreCase("nmr")) {
        /* we do not use this info */
        sc.getNextToken();
      } else if ((sc.argc == 1)
          && (sc.argv[0].equals("EREIGNISSE") || sc.argv[0].equals("EINKOMMEN")
              || sc.argv[0].equals("HANDEL") || sc.argv[0].equals("PRODUKTION")
              || sc.argv[0].equals("BEWEGUNGEN") || sc.argv[0].equals("MELDUNGEN"))) {
        faction.setMessages(parseMessageSequence(faction.getMessages()));
      } else if ((sc.argc == 1) && sc.argv[0].startsWith("MESSAGE ")) {
        faction.setMessages(parseMessages(faction.getMessages()));
      } else if ((sc.argc == 1) && sc.argv[0].startsWith("BATTLE ")) {
        faction.setBattles(parseBattles(faction.getBattles()));
      } else if ((sc.argc == 1) && sc.argv[0].startsWith("BATTLESPEC ")) {
        faction.setBattles(parseBattleSpecs(faction.getBattles()));
      } else if ((sc.argc == 1) && sc.argv[0].startsWith("KAEMPFE")) {
        sc.getNextToken(); // skip KAEMPFE

        // Skip the whole block (old syntax, no longer supported)
        while (!sc.eof && !sc.isBlock) {
          sc.getNextToken();
        }
      } else if ((sc.argc == 1) && sc.argv[0].equals("ZAUBER")) { // old syntax, ignore
        parseMessageSequence(null);
      } else if ((sc.argc == 1) && sc.argv[0].startsWith("TRAENKE")) { // old syntax, ignore
        parseMessageSequence(null);
      } else if ((sc.argc == 1) && sc.argv[0].startsWith("ALLIIERTE")) {
        faction.setAllies(parseAlliierte()); // old syntax
      } else if (sc.isBlock && sc.argv[0].startsWith("ALLIANZ ")) {
        faction.setAllies(parseAlliance(faction.getAllies())); // newer syntax
      } else if ((sc.argc == 2) && sc.argv[1].equalsIgnoreCase("alliance")) { // even newer syntax
        final EntityID alliance = EntityID.createEntityID(Integer.parseInt(sc.argv[0]), world.base);
        if (world.getAllianceGroup(alliance) == null) {
          CRParser.log.error("unknown alliance for faction " + faction);
        } else {
          world.getAllianceGroup(alliance).addFaction(faction);
          faction.setAlliance(world.getAllianceGroup(alliance));
        }
        sc.getNextToken();
      } else if ((sc.argc == 2) && sc.argv[1].equalsIgnoreCase("joined")) {
        CRParser.log.fine("ignoring joined tag " + sc.argv[0]);
        sc.getNextToken();
      } else if (sc.isBlock && sc.argv[0].equals("ADRESSEN")) {
        parseAdressen();
      } else if (sc.isBlock && sc.argv[0].equals("GEGENSTAENDE")) {
        parseItems(faction);
      } else if (sc.isBlock && sc.argv[0].equals("OPTIONEN")) {
        // ignore this block, if there are options, they are
        // encoded as a bit field whereas these string
        // representation is not fixed and eventually leads
        // to trouble
        parseOptions(null);
      } else if (sc.isBlock && sc.argv[0].startsWith("GRUPPE ")) {
        faction.setGroups(parseGroup(faction.getGroups(), faction, groupSortIndex++));
      } else if ((sc.argc == 1) && sc.argv[0].equals("COMMENTS")) {
        faction.setComments(parseStringSequence(faction.getComments()));
      } else if ((sc.argc == 1) && sc.argv[0].equals("FEHLER")) {
        faction.setErrors(parseStringSequence(faction.getErrors()));
      } else if ((sc.isBlock) && sc.argv[0].equals("ATTRIBUTES")) {
        parseAttributes(faction);
      } else if ((sc.argc == 1) && sc.argv[0].equals("WARNUNGEN")) {
        /* Verdanon messages */
        faction.setMessages(parseMessageSequence(faction.getMessages()));
      } else if (sc.isBlock) {
        if (!sc.isIdBlock) {
          unknown("PARTEI", false);
        }
        break;
      } else {
        unknown("PARTEI", true);
      }
    }

    if ((type != null) && (raceRecruit != -1)) {
      type.setRecruitmentCosts(raceRecruit);
    }

    return faction;
  }

  private Options parseOptions(Options options) throws IOException {
    sc.getNextToken(); // skip OPTIONEN

    while (!sc.eof) {
      if (sc.argc == 2) {
        // options.setActive(StringID.create(sc.argv[1]), Integer.parseInt(sc.argv[0]) != 0);
        sc.getNextToken();
      } else if (sc.isBlock) {
        break;
      } else {
        unknown("OPTIONEN", true);
      }
    }

    return options;
  }

  private Map<IntegerID, Group> parseGroup(Map<IntegerID, Group> groups, Faction faction,
      int sortIndex) throws IOException {
    final IntegerID id = IntegerID.create(sc.argv[0].substring(7));
    Group group = null;

    if (groups == null) {
      groups = CollectionFactory.<IntegerID, Group> createSyncOrderedMap();
    }

    group = groups.get(id);

    if (group == null) {
      group = MagellanFactory.createGroup(id, world);
    }

    group.setFaction(faction);
    group.setSortIndex(sortIndex);
    groups.put(id, group);
    sc.getNextToken(); // skip GRUPPE nr

    while (!sc.eof) {
      if ((sc.argc == 2) && sc.argv[1].equalsIgnoreCase("name")) {
        group.setName(sc.argv[0]);
        sc.getNextToken();
      } else if ((sc.argc == 2) && sc.argv[1].equalsIgnoreCase("typprefix")) {
        group.setRaceNamePrefix(sc.argv[0]);
        sc.getNextToken();
      } else if (sc.isBlock && sc.argv[0].startsWith("ALLIANZ ")) {
        parseAlliance(group.allies());
      } else if (sc.isBlock && sc.argv[0].startsWith("ATTRIBUTES")) {
        parseAttributes(group);
      } else if (sc.isBlock) {
        break;
      } else {
        unknown("GRUPPE", true);
      }
    }

    return groups;
  }

  /**
   * Accesses unit.persons which must be > 0 and just adds new skills, existing skills are not
   * deleted.
   *
   * @throws IOException if the scanner throws an IOException
   */
  private void parseSkills(Unit unit) throws IOException {
    sc.getNextToken(); // skip TALENTE

    if (unit == null) {
      invalidParam("parseSkills", "unit is null");

      return;
    }

    if (unit.getPersons() <= 0) {
      invalidParam("parseSkills", "unit.persons <= 0");

      return;
    }

    if (world == null) {
      invalidParam("parseSkills", "world is null");

      return;
    }

    if (world.getRules() == null) {
      invalidParam("parseSkills", "rules is null");

      return;
    }

    while (!sc.eof && (sc.argc == 2)) {
      int points = 0;
      int level = 0;
      int change = 0;
      // boolean changed = false;

      final int s = sc.argv[0].indexOf(' ', 0);
      final int s2 = sc.argv[0].indexOf(' ', s + 1);

      if (s > -1) {
        points = Integer.parseInt(sc.argv[0].substring(0, s));

        if (s2 > -1) {
          level = Integer.parseInt(sc.argv[0].substring(s + 1, s2));
          change = Integer.parseInt(sc.argv[0].substring(s2 + 1));
          // changed = true;
        } else {
          level = Integer.parseInt(sc.argv[0].substring(s + 1));
        }
      } else {
        level = Integer.parseInt(sc.argv[0]);
      }

      final Skill skill =
          new Skill(world.getRules().getSkillType(StringID.create(sc.argv[1]), true), points,
              level, unit.getPersons(), world.noSkillPoints);
      skill.setChangeLevel(change);
      // skill.setLevelChanged(changed);
      unit.addSkill(skill);
      sc.getNextToken();
    }
  }

  /*
   * Syntax: "count;item" Example: "1;Steine" Does not delete existing items.
   */
  private void parseItems(Unit unit) throws IOException {
    /*
     * if(unit == null) { invalidParam("parseItems", "unit is null"); return; }
     */

    sc.getNextToken(); // skip GEGENSTAENDE

    while (!sc.eof && (sc.argc == 2)) {
      final Item item =
          new Item(world.getRules().getItemType(StringID.create(sc.argv[1]), true), Integer
              .parseInt(sc.argv[0]));
      if (unit != null) {
        unit.addItem(item);
      }
      sc.getNextToken();
    }
  }

  /*
   * Syntax: "count;item" Example: "1;Steine" Does not delete existing items.
   */
  private void parseItems(UnitContainer unitcontainer) throws IOException {

    sc.getNextToken(); // skip GEGENSTAENDE

    while (!sc.eof && (sc.argc == 2)) {
      final Item item =
          new Item(world.getRules().getItemType(StringID.create(sc.argv[1]), true), Integer
              .parseInt(sc.argv[0]));
      if (unitcontainer != null) {
        unitcontainer.addItem(item);
      }
      sc.getNextToken();
    }
  }

  private int parseUnit(Region region, int sortIndex) throws IOException {
    return parseUnit(region, sortIndex, "EINHEIT");
  }

  private int parseUnit(Region region, int sortIndex, String blockName) throws IOException {
    boolean oldUnit;
    final Unit unit =
        getAddUnit(UnitID
            .createUnitID(sc.argv[0].substring(blockName.length() + 1), 10, world.base), oldUnit =
                blockName.equals("ALTEINHEIT"));
    EntityID factionID = EntityID.createEntityID(-1, world.base);
    ID groupID = null;

    if (region != unit.getRegion()) {
      if (unit.getRegion() != null) {
        log.warn(unit + " " + unit.getRegion() + " --> " + region);
      }
      unit.setRegion(region);
    }

    sc.getNextToken(); // skip "EINHEIT nr"

    long t = System.currentTimeMillis();
    while (!sc.eof) {
      if ((sc.argc == 2) && sc.argv[1].equalsIgnoreCase("Name")) {
        unit.setName(sc.argv[0]);
        sc.getNextToken();
      } else if ((sc.argc == 2) && sc.argv[1].equalsIgnoreCase("Beschr")) {
        unit.setDescription(sc.argv[0]);
        sc.getNextToken();
      } else if ((sc.argc == 2) && sc.argv[1].equalsIgnoreCase("Typ")) {
        unit.setRace(world.getRules().getRace(StringID.create(sc.argv[0]), true));
        sc.getNextToken();
      } else if ((sc.argc == 2) && sc.argv[1].equalsIgnoreCase("wahrerTyp")) {
        unit.setRealRace(world.getRules().getRace(StringID.create(sc.argv[0]), true));
        sc.getNextToken();
      } else if ((sc.argc == 2) && sc.argv[1].equalsIgnoreCase("temp")) {
        unit.setTempID(UnitID.createUnitID(Integer.parseInt(sc.argv[0]), world.base));
        sc.getNextToken();
      } else if ((sc.argc == 2) && sc.argv[1].equalsIgnoreCase("alias")) {
        unit.setAlias(UnitID.createUnitID(Integer.parseInt(sc.argv[0]), world.base));
        sc.getNextToken();
      } else if ((sc.argc == 2) && sc.argv[1].equalsIgnoreCase("privat")) {
        unit.setPrivDesc(sc.argv[0]);
        sc.getNextToken();
      } else if ((sc.argc == 2) && sc.argv[1].equalsIgnoreCase("Anzahl")) {
        unit.setPersons(Integer.parseInt(sc.argv[0]));
        sc.getNextToken();
      } else if ((sc.argc == 2) && sc.argv[1].equalsIgnoreCase("Partei")) {
        factionID = EntityID.createEntityID(Integer.parseInt(sc.argv[0]), world.base);
        sc.getNextToken();
      } else if ((sc.argc == 2) && sc.argv[1].equalsIgnoreCase("Parteiname")) {
        sc.getNextToken();
      } else if ((sc.argc == 2) && sc.argv[1].equalsIgnoreCase("Parteitarnung")) {
        if (Integer.parseInt(sc.argv[0]) != 0) {
          unit.setHideFaction(true);
        } else {
          unit.setHideFaction(false);
        }

        sc.getNextToken();
      } else if ((sc.argc == 2) && sc.argv[1].equalsIgnoreCase("bewacht")) {
        unit.setGuard(Integer.parseInt(sc.argv[0]));

        final Region r = unit.getRegion();

        if (r != null) {
          r.addGuard(unit);
        }

        sc.getNextToken();
      } else if ((sc.argc == 2) && sc.argv[1].equalsIgnoreCase("belagert")) {
        unit.setSiege(getAddBuilding(EntityID.createEntityID(Integer.parseInt(sc.argv[0]),
            world.base)));
        sc.getNextToken();
      } else if ((sc.argc == 2) && sc.argv[1].equalsIgnoreCase("folgt")) {
        unit.setFollows(getAddUnit(UnitID.createUnitID(Integer.parseInt(sc.argv[0]), world.base),
            oldUnit));
        sc.getNextToken();
      } else if ((sc.argc == 2) && sc.argv[1].equalsIgnoreCase("familiarmage")) {
        unit.setFamiliarmageID(UnitID.createUnitID(Integer.parseInt(sc.argv[0]), world.base));
        sc.getNextToken();
      } else if ((sc.argc == 2) && sc.argv[1].equalsIgnoreCase("Silber")) {
        final int money = Integer.parseInt(sc.argv[0]);
        final Item item =
            new Item(world.getRules().getItemType(EresseaConstants.I_USILVER, true), money);
        unit.addItem(item);
        sc.getNextToken();
      } else if ((sc.argc == 2) && sc.argv[1].equalsIgnoreCase("Burg")) {
        Integer.parseInt(sc.argv[0]);

        final Building b =
            getAddBuilding(EntityID.createEntityID(Integer.parseInt(sc.argv[0]), world.base));

        if (unit.getBuilding() != b) {
          unit.setBuilding(b);
        }

        sc.getNextToken();
      } else if ((sc.argc == 2) && sc.argv[1].equalsIgnoreCase("Schiff")) {
        final Ship s =
            getAddShip(EntityID.createEntityID(Integer.parseInt(sc.argv[0]), world.base));

        if (unit.getShip() != s) {
          unit.setShip(s);
        }

        sc.getNextToken();
      } else if ((sc.argc == 2) && sc.argv[1].equalsIgnoreCase("Kampfstatus")) {
        // pre 57:
        // 0: VORNE
        // 1: HINTEN
        // 2: NICHT
        // 3: FLIEHE
        //
        // 57 and later:
        // 0 AGGRESSIV: 1. Reihe, flieht nie.
        // 1 VORNE: 1. Reihe, kaempfen bis 20% HP
        // 2 HINTEN: 2. Reihe, kaempfen bis 20% HP
        // 3 DEFENSIV: 2. Reihe, kaempfen bis 90% HP
        // 4 NICHT: 3. Reihe, kaempfen bis 90% HP
        // 5 FLIEHE: 4. Reihe, flieht immer.
        unit.setCombatStatus(Integer.parseInt(sc.argv[0]));

        // convert status from old to new
        if (version < 57) {
          unit.setCombatStatus(unit.getCombatStatus() + 1);

          if (unit.getCombatStatus() > 2) {
            unit.setCombatStatus(unit.getCombatStatus() + 1);
          }
        }

        sc.getNextToken();
      } else if ((sc.argc == 2) && sc.argv[1].equalsIgnoreCase("unaided")) {
        unit.setUnaided((Integer.parseInt(sc.argv[0]) != 0));
        sc.getNextToken();
      } else if ((sc.argc == 2) && sc.argv[1].equalsIgnoreCase("Tarnung")) {
        unit.setStealth(Integer.parseInt(sc.argv[0]));
        sc.getNextToken();
      } else if ((sc.argc == 2) && sc.argv[1].equalsIgnoreCase("Aura")) {
        unit.setAura(Integer.parseInt(sc.argv[0]));
        sc.getNextToken();
      } else if ((sc.argc == 2) && sc.argv[1].equalsIgnoreCase("Auramax")) {
        unit.setAuraMax(Integer.parseInt(sc.argv[0]));
        sc.getNextToken();
      } else if ((sc.argc == 2) && sc.argv[1].equalsIgnoreCase("hp")) {
        unit.setHealth(sc.argv[0]);
        sc.getNextToken();
      } else if ((sc.argc == 2) && sc.argv[1].equalsIgnoreCase("hunger")) {
        unit.setStarving((Integer.parseInt(sc.argv[0]) != 0));
        sc.getNextToken();
      } else if ((sc.argc == 2) && sc.argv[1].equalsIgnoreCase("ejcOrdersConfirmed")) {
        unit.setOrdersConfirmed((Integer.parseInt(sc.argv[0]) != 0));
        sc.getNextToken();
      } else if ((sc.argc == 2) && sc.argv[1].equalsIgnoreCase("gruppe")) {
        groupID = IntegerID.create(sc.argv[0]);
        sc.getNextToken();
      } else if ((sc.argc == 2) && sc.argv[1].equalsIgnoreCase("verraeter")) {
        unit.setSpy(true);
        sc.getNextToken();

        /*
         * currently, verkleidung was announced but it seems that anderepartei is used. Please
         * remove one as soon as it is clear which one can be discarded
         */
      } else if ((sc.argc == 2)
          && (sc.argv[1].equalsIgnoreCase("verkleidung") || sc.argv[1]
              .equalsIgnoreCase("anderepartei"))) {
        final EntityID fid = EntityID.createEntityID(Integer.parseInt(sc.argv[0]), world.base);

        /*
         * currently (2004-02) the cr is inconsistent with nr. There may be a situation where the
         * corresponding faction of this tag does not exist in the game data so add it automagically
         * (bugzilla bug 794).
         */
        Faction faction = world.getFaction(fid);

        if (faction == null) {
          faction = MagellanFactory.createFaction(fid, world);
        }

        unit.setGuiseFaction(faction);
        sc.getNextToken();
      } else if ((sc.argc == 2) && sc.argv[1].equalsIgnoreCase("typprefix")) {
        unit.setRaceNamePrefix(sc.argv[0]);
        sc.getNextToken();
      } else if ((sc.argc == 2) && sc.argv[1].equalsIgnoreCase("ladung")) {
        // Verdanon tag
        sc.getNextToken();
      } else if ((sc.argc == 2) && sc.argv[1].equalsIgnoreCase("kapazitaet")) {
        // Verdanon tag
        sc.getNextToken();
      } else if ((sc.argc == 2) && sc.argv[1].equalsIgnoreCase("hero")) {
        // new promotion level
        unit.setHero((Integer.parseInt(sc.argv[0]) != 0));
        sc.getNextToken();
      } else if ((sc.argc == 2) && sc.argv[1].equalsIgnoreCase("weight")) {
        unit.setWeight(Integer.parseInt(sc.argv[0]));
        sc.getNextToken();
      } else if ((sc.argc == 1) && sc.argv[0].equals("COMMANDS")) {
        // there can be only one order block for a unit, replace existing ones
        unit.setOrders(parseStringSequence(null), false);
      } else if ((sc.argc == 1) && sc.argv[0].equals("TALENTE")) {
        // there can be only one skills block for a unit, replace existing ones
        unit.clearSkills();
        parseSkills(unit);
      } else if ((sc.argc == 1) && sc.argv[0].equals("SPRUECHE")) {
        // there can be only one spells block for a unit, replace existing ones
        unit.setSpells(parseUnitSpells(null));
      } else if ((sc.argc == 1) && sc.argv[0].equals("GEGENSTAENDE")) {
        /*
         * in verdanon reports the silver can already be included in the items
         */
        parseItems(unit);
      } else if (sc.isBlock && sc.argv[0].equals("EINHEITSBOTSCHAFTEN")) {
        unit.setUnitMessages(parseMessageSequence(unit.getUnitMessages()));
      } else if ((sc.argc == 1) && sc.argv[0].equals("COMMENTS")) {
        unit.setComments(parseStringSequence(unit.getComments()));
      } else if ((sc.argc == 1) && sc.argv[0].equals("EFFECTS")) {
        unit.setEffects(parseStringSequence(unit.getEffects()));
      } else if ((sc.argc == 1) && sc.argv[0].startsWith("KAMPFZAUBER ")) {
        parseUnitCombatSpells(unit);
      } else if ((sc.isBlock) && sc.argv[0].equals("ATTRIBUTES")) {
        parseAttributes(unit);
      } else if (sc.isBlock) {
        break;
      } else {
        if (sc.argc == 2) {
          unit.putTag(sc.argv[1], sc.argv[0]);
        }
        // check for wellknown tags...ejcTaggable etc...
        boolean isUnknown = true;
        if (sc.argc == 2) {
          if (sc.argv[1].equalsIgnoreCase(CRParser.TAGGABLE_STRING)) {
            isUnknown = false;
          }
          if (sc.argv[1].equalsIgnoreCase(CRParser.TAGGABLE_STRING2)) {
            isUnknown = false;
          }
          if (sc.argv[1].equalsIgnoreCase(CRParser.TAGGABLE_STRING3)) {
            isUnknown = false;
          }
          if (sc.argv[1].equalsIgnoreCase(CRParser.TAGGABLE_STRING4)) {
            isUnknown = false;
          }
          if (sc.argv[1].equalsIgnoreCase(CRParser.TAGGABLE_STRING5)) {
            isUnknown = false;
          }
        }
        if (isUnknown) {
          unknown(blockName, true);
        } else {
          sc.getNextToken();
        }
      }
    }
    tUParse += System.currentTimeMillis() - t;

    // set the sortIndex so the original ordering of the units
    // can be restored
    unit.setSortIndex(sortIndex);

    final Faction faction = getAddFaction(factionID);

    if (faction.getName() == null) {
      if (factionID.intValue() == -1) {
        faction.setName(Resources.get("crparser.nofaction"));
      } else {
        faction.setName(Resources.get("crparser.unknownfaction", factionID));
      }
    }

    if (unit.getFaction() != faction) {
      if (unit.getFaction() != null) {
        log.warn(unit + " " + unit.getFaction() + " --> " + faction);
      }
      unit.setFaction(faction);
    }

    if (groupID != null) {
      Group g = null;

      if ((faction.getGroups() != null) && ((g = faction.getGroups().get(groupID)) != null)) {
        unit.setGroup(g);
      } else {
        CRParser.log.warn("CRParser.parseUnit(): Unable to assign group " + groupID + " to unit "
            + unit.getID());
      }
    }

    /*
     * a missing combat status can have two meanings: 1. this is a unit we know everything about and
     * the combat status is AGGRESSIVE 2. this is a unit we just see but does not belong to us so we
     * do not know its combat status.
     */
    if (!unit.ordersAreNull() && (unit.getCombatStatus() < 0)) {
      unit.setCombatStatus(0);
    }

    if (oldUnit) {
      unit.detach();
    }

    return sortIndex;
  }

  /*
   * Syntax: value;item Example: 24;Balsam < 0: offered in this region > 0: demanded in this region
   */
  private Map<StringID, LuxuryPrice> parsePrices(Map<StringID, LuxuryPrice> prices)
      throws IOException {
    sc.getNextToken(); // skip PREISE

    while (!sc.eof && (sc.argc == 2)) {
      ItemType itemType = world.getRules().getItemType(StringID.create(sc.argv[1]));
      if (itemType == null) {
        CRParser.log.warn("unknown price added: " + sc.argv[1] + ",maybe wrong coding?(actual:"
            + world.getEncoding() + ")");
        itemType = world.getRules().getItemType(StringID.create(sc.argv[1]), true);
      }
      final LuxuryPrice pr = new LuxuryPrice(itemType, Integer.parseInt(sc.argv[0]));

      if (prices == null) {
        prices = CollectionFactory.<StringID, LuxuryPrice> createSyncOrderedMap(8, .9f);
      }

      prices.put(itemType.getID(), pr);
      sc.getNextToken();
    }

    return prices;
  }

  private void parseShip(Region region, int sortIndex) throws IOException {
    final EntityID id =
        EntityID.createEntityID(Integer.parseInt(sc.argv[0].substring(7)), world.base);
    sc.getNextToken(); // skip "SCHIFF nr"

    final Ship ship = getAddShip(id);

    if (ship.getRegion() != region) {
      if (ship.getRegion() != null) {
        log.warn(ship + " " + ship.getRegion() + " --> " + region);
      }
      ship.setRegion(region);
    }

    ship.setSortIndex(sortIndex);

    while (!sc.eof) {
      if ((sc.argc == 2) && sc.argv[1].equalsIgnoreCase("Name")) {
        ship.setName(sc.argv[0]);
        sc.getNextToken();
      } else if ((sc.argc == 2) && sc.argv[1].equalsIgnoreCase("Typ")) {
        final ShipType type = world.getRules().getShipType(StringID.create(sc.argv[0]), true);
        ship.setType(type);
        sc.getNextToken();
      } else if ((sc.argc == 2) && sc.argv[1].equalsIgnoreCase("Beschr")) {
        ship.setDescription(sc.argv[0]);
        sc.getNextToken();
      } else if ((sc.argc == 2) && sc.argv[1].equalsIgnoreCase("Partei")) {
        // this info is largely redundant (and destroys the faction's unit order)
        // if ((ship.getOwnerUnit() != null) && (ship.getOwnerUnit().getFaction() == null)) {
        // final Faction f =
        // world.getFaction(EntityID.createEntityID(Integer.parseInt(sc.argv[0]), world.base));
        // ship.getOwnerUnit().setFaction(f);
        // }

        sc.getNextToken();
      } else if ((sc.argc == 2) && sc.argv[1].equalsIgnoreCase("Kapitaen")) {
        ship.setOwner(getAddUnit(UnitID.createUnitID(sc.argv[0], 10, world.base), false));
        sc.getNextToken();
      } else if ((sc.argc == 2) && sc.argv[1].equalsIgnoreCase("Kueste")) {
        ship.setShoreId(Integer.parseInt(sc.argv[0]));
        sc.getNextToken();
      } else if ((sc.argc == 2) && sc.argv[1].equalsIgnoreCase("Groesse")) {
        ship.setSize(Integer.parseInt(sc.argv[0]));
        sc.getNextToken();
      } else if ((sc.argc == 2) && sc.argv[1].equalsIgnoreCase("Prozent")
          && (ship.getType() != null)) {
        // deprecated
        ship.setSize((ship.getShipType().getMaxSize() * Integer.parseInt(sc.argv[0])) / 100);
        sc.getNextToken();
      } else if ((sc.argc == 2) && sc.argv[1].equalsIgnoreCase("Schaden")) {
        ship.setDamageRatio(Integer.parseInt(sc.argv[0]));
        sc.getNextToken();
      } else if ((sc.argc == 2) && sc.argv[1].equalsIgnoreCase("cargo")) {
        ship.setCargo(Integer.parseInt(sc.argv[0]));
        sc.getNextToken();
      } else if ((sc.argc == 2) && sc.argv[1].equalsIgnoreCase("capacity")) {
        if (version == 65) {
          // in E3 this tag was overloaded with the max number of persons (see Eressea bug #1645)!
          ship.setMaxPersons(Integer.parseInt(sc.argv[0]));
        } else {
          // in version 65 everything was back to normal
          ship.setCapacity(Integer.parseInt(sc.argv[0]));
        }
        sc.getNextToken();
      } else if ((sc.argc == 2) && sc.argv[1].equalsIgnoreCase("Ladung")) {
        ship.setDeprecatedLoad(Integer.parseInt(sc.argv[0]));
        sc.getNextToken();
      } else if ((sc.argc == 2) && sc.argv[1].equalsIgnoreCase("MaxLadung")) {
        ship.setDeprecatedCapacity(Integer.parseInt(sc.argv[0]));
        sc.getNextToken();
      } else if ((sc.argc == 2) && sc.argv[1].equalsIgnoreCase("speed")) {
        ship.setSpeed(Integer.parseInt(sc.argv[0]));
        sc.getNextToken();
      } else if ((sc.argc == 2) && sc.argv[1].equalsIgnoreCase("Anzahl")) {
        ship.setAmount(Integer.parseInt(sc.argv[0]));
        sc.getNextToken();
      } else if (isBookmark()) {
        parseBookmark(ship);
      } else if ((sc.argc == 1) && sc.argv[0].equals("EFFECTS")) {
        ship.setEffects(parseStringSequence(ship.getEffects()));
      } else if ((sc.argc == 1) && sc.argv[0].equals("COMMENTS")) {
        ship.setComments(parseStringSequence(ship.getComments()));
      } else if ((sc.isBlock) && sc.argv[0].equals("ATTRIBUTES")) {
        parseAttributes(ship);
      } else if (sc.isBlock) {
        break;
      } else {
        unknown("SCHIFF", true);
      }
    }
  }

  /*
   *
   */
  private void parseBuilding(Region region, int sortIndex) throws IOException {
    final EntityID id =
        EntityID.createEntityID(Integer.parseInt(sc.argv[0].substring(5)), world.base);
    sc.getNextToken(); // skip "BURG nr"

    final Building bld = getAddBuilding(id);

    if (bld.getRegion() != region) {
      if (bld.getRegion() != null) {
        log.warn(bld + " " + bld.getRegion() + " --> " + region);
      }
      bld.setRegion(region);
    }

    bld.setSortIndex(sortIndex);

    while (!sc.eof) {
      if ((sc.argc == 2) && sc.argv[1].equalsIgnoreCase("Name")) {
        bld.setName(sc.argv[0]);
        sc.getNextToken();
      } else if ((sc.argc == 2) && sc.argv[1].equalsIgnoreCase("wahrerTyp")) {
        bld.setTrueBuildingType(sc.argv[0]);
        sc.getNextToken();
      } else if ((sc.argc == 2) && sc.argv[1].equalsIgnoreCase("Typ")) {
        final BuildingType bType =
            world.getRules().getBuildingType(StringID.create(sc.argv[0]), true);
        bld.setType(bType);
        sc.getNextToken();
      } else if ((sc.argc == 2) && sc.argv[1].equalsIgnoreCase("Beschr")) {
        bld.setDescription(sc.argv[0]);
        sc.getNextToken();
      } else if ((sc.argc == 2) && sc.argv[1].equalsIgnoreCase("Besitzer")) {
        final UnitID unitID = UnitID.createUnitID(sc.argv[0], 10, world.base);
        bld.setOwner(getAddUnit(unitID, false));
        sc.getNextToken();
      } else if ((sc.argc == 2) && sc.argv[1].equalsIgnoreCase("Partei")) {
        // this info is largely redundant (and destroys the faction's unit order)
        // if ((bld.getOwnerUnit() != null) && (bld.getOwnerUnit().getFaction() == null)) {
        // final Faction f = world.getFaction(EntityID.createEntityID(sc.argv[0], 10, world.base));
        // bld.getOwnerUnit().setFaction(f);
        // }
        sc.getNextToken();
      } else if ((sc.argc == 2) && sc.argv[1].equalsIgnoreCase("Groesse")) {
        bld.setSize(Integer.parseInt(sc.argv[0]));
        sc.getNextToken();
      } else if ((sc.argc == 2) && sc.argv[1].equalsIgnoreCase("Unterhalt")) {
        bld.setCost(Integer.parseInt(sc.argv[0]));
        sc.getNextToken();
      } else if ((sc.argc == 2) && sc.argv[1].equalsIgnoreCase("Belagerer")) {
        bld.setBesiegers(Integer.parseInt(sc.argv[0]));
        sc.getNextToken();
      } else if (isBookmark()) {
        parseBookmark(bld);
      } else if ((sc.argc == 1) && sc.argv[0].equals("EFFECTS")) {
        bld.setEffects(parseStringSequence(bld.getEffects()));
      } else if ((sc.argc == 1) && sc.argv[0].equals("COMMENTS")) {
        bld.setComments(parseStringSequence(bld.getComments()));
      } else if ((sc.isBlock) && sc.argv[0].equals("ATTRIBUTES")) {
        parseAttributes(bld);
      } else if (sc.isBlock) {
        break;
      } else {
        unknown("BURG", true);
      }
    }
  }

  protected BookmarkBuilder parseBookmark(Selectable object) throws IOException {
    BookmarkBuilder bm = MagellanFactory.createBookmark();
    sc.getNextToken();
    if (sc.argc == 2 && sc.argv[1].equalsIgnoreCase("bookmarkname")) {
      bm.setName(sc.argv[0]);
      sc.getNextToken();
    }
    if (object != null) {
      bm.setObject(object);
      world.addBookmark(bm.getBookmark());
    }
    return bm;
  }

  private boolean isBookmark() {
    return sc.argc == 2 && sc.argv[1].equalsIgnoreCase("bookmark");
  }

  /**
   * Parse consecutive GRENZE sub blocks of the REGION block.
   *
   * @param r a list to add the read borders to
   * @throws IOException if the scanner throws an IOException
   */
  private void parseBorders(Region r) throws IOException {
    while (!sc.eof && sc.isBlock && sc.argv[0].startsWith("GRENZE ")) {
      final Border b = parseBorder();
      r.addBorder(b);
    }
  }

  /**
   * Parse consecutive SIGN sub blocks of the REGION block.
   *
   * @param r the actual region
   * @throws IOException if the scanner throws an IOException
   */
  private void parseSigns(Region r) throws IOException {
    while (!sc.eof && sc.isBlock && sc.argv[0].startsWith("SIGN ")) {
      final Sign s = parseSign();
      r.addSign(s);
    }
  }

  /**
   * Parse one GRENZE sub block of the REGION block.
   *
   * @return the resulting <tt>Border</tt> object.
   * @throws IOException if the scanner throws an IOException
   */
  private Border parseBorder() throws IOException {
    final IntegerID id = IntegerID.create(sc.argv[0].substring(7));
    final Border b = MagellanFactory.createBorder(id);
    sc.getNextToken(); // skip the block

    while (!sc.eof) {
      if ((sc.argc == 2) && sc.argv[1].equalsIgnoreCase("richtung")) {
        b.setDirection(-1);
        // b.setDirectionName(Resources.get("util.direction.name.long.invalid"));

        try {
          b.setDirection(Integer.parseInt(sc.argv[0]));
          // b.setDirectionName(world.rules.getGameSpecificStuff().getOrderChanger().getOrder(
          // Locales.getGUILocale(),
          // world.getGameSpecificStuff().getMapMetric().toDirection(Integer.parseInt(sc.argv[0]))
          // .getId()));
        } catch (final NumberFormatException e) {
          final String dirNames[] =
              { "Nordwesten", "Nordosten", "Osten", "Südosten", "Südwesten", "Westen" };

          for (int i = 0; i < dirNames.length; i++) {
            if (sc.argv[0].equalsIgnoreCase(dirNames[i])) {
              b.setDirection(i);
              // b.setDirectionName(world.rules.getGameSpecificStuff().getOrderChanger().getOrder(
              // Locales.getGUILocale(),
              // world.getGameSpecificStuff().getMapMetric().toDirection(i).getId()));
              break;
            }
          }
        }

        sc.getNextToken();
      } else if ((sc.argc == 2) && sc.argv[1].equalsIgnoreCase("typ")) {
        b.setType(sc.argv[0]);
        sc.getNextToken();
      } else if ((sc.argc == 2) && sc.argv[1].equalsIgnoreCase("prozent")) {
        b.setBuildRatio(Integer.parseInt(sc.argv[0]));
        sc.getNextToken();
      } else if ((sc.argc == 2) && sc.argv[1].equalsIgnoreCase("opaque")) {
        // ignored
        sc.getNextToken();
      } else if (sc.isBlock) {
        break;
      } else {
        unknown("GRENZE", true);
      }
    }

    return b;
  }

  /**
   * Parse one SIGN sub block of the REGION block.
   *
   * @return the resulting <tt>SIGN</tt> object.
   * @throws IOException if the scanner throws an IOException
   */
  private Sign parseSign() throws IOException {
    final Sign s = new Sign();
    // Border b = new Border(id);
    // create new sign...
    sc.getNextToken(); // skip the block

    while (!sc.eof) {
      if ((sc.argc == 2) && sc.argv[1].equalsIgnoreCase("text")) {
        s.setText(sc.argv[0]);
        sc.getNextToken();
      } else if (sc.isBlock) {
        break;
      } else {
        unknown("SIGN", true);
      }
    }
    return s;
  }

  private void parseHotSpot(GameData data) throws IOException {
    final IntegerID id = IntegerID.create(sc.argv[0].substring(8));
    // HotSpots have been replaced by Bookmarks
    log.warn("replacing obsolete HOTSPOT by bookmark");
    BookmarkBuilder bookmark = MagellanFactory.createBookmark();
    sc.getNextToken(); // skip the block

    while (!sc.eof) {
      if ((sc.argc == 2) && sc.argv[1].equalsIgnoreCase("coord")) {
        bookmark.setObject(MagellanFactory.createRegion(originTranslate(CoordinateID.parse(
            sc.argv[0], " ")), data));
        sc.getNextToken();
      } else if ((sc.argc == 2) && sc.argv[1].equalsIgnoreCase("name")) {
        // ignore
        bookmark.setName(sc.argv[0]);
        sc.getNextToken();
      } else if (sc.isBlock) {
        break;
      } else {
        unknown("HOTSPOT", true);
      }
    }

    data.addBookmark(bookmark.getBookmark());
  }

  /*
   * Parse everything within one region. Heuristic for block termination: - Terminate on another
   * REGION or SPEZIALREGION block (without warning) - Terminate on any other unknown block (with
   * warning)
   */
  private void parseRegion(int sortIndex) throws IOException {
    int iValidateFlags = 0; // 1 - terrain type
    int unitSortIndex = 0;
    int shipSortIndex = 0;
    int buildingSortIndex = 0;
    CoordinateID c = CoordinateID.parse(sc.argv[0].substring(sc.argv[0].indexOf(" ", 0)), " ");

    if (c == null) {
      unknown("REGION", true);
      // without CoordinateID Region is not accessible
      return;
    }

    // this we should make after checking c!=null
    c = originTranslate(c);

    sc.getNextToken(); // skip "REGION x y"

    Region region = world.getRegion(c);

    if (region == null) {
      region = MagellanFactory.createRegion(c, world);
    }

    region.setSortIndex(sortIndex);

    long t = System.currentTimeMillis();
    while (!sc.eof) {
      if ((sc.argc == 2) && sc.argv[1].equalsIgnoreCase("Name")) {
        // regions doesn't have name if name == type; e.g. "Ozean"=="Ozean"
        region.setName(sc.argv[0]);
        ui.setProgress(Resources.get("progressdialog.loadcr.step03", new Object[] { region
            .getName() }), 2);

        sc.getNextToken();
      } else if ((sc.argc == 2) && sc.argv[1].equalsIgnoreCase("id")) {
        region.setUID(Long.parseLong(sc.argv[0]));
        sc.getNextToken();
      } else if ((sc.argc == 2) && sc.argv[1].equalsIgnoreCase("Beschr")) {
        region.setDescription(sc.argv[0]);
        sc.getNextToken();
      } else if ((sc.argc == 2) && sc.argv[1].equalsIgnoreCase("Strasse")) {
        sc.getNextToken();
      } else if ((sc.argc == 2) && sc.argv[1].equalsIgnoreCase("ejcIsSelected")) {
        world.addSelectedRegionCoordinate(region);
        sc.getNextToken();
      } else if ((sc.argc == 2) && sc.argv[1].equalsIgnoreCase("Insel")) {
        try {
          final IntegerID islandID = IntegerID.create(sc.argv[0]);
          Island island = world.getIsland(islandID);

          if (island == null) {
            CRParser.log.warn("CRParser.parseRegion(): unknown island " + sc.argv[0]
                + " with region " + region + " in line " + sc.lnr + ", creating it dynamically.");
            island = new MagellanIslandImpl(islandID, world);
            island.setName(islandID.toString());
            world.addIsland(island);
          }
          region.setIsland(island);

        } catch (final NumberFormatException nfe) {
          CRParser.log.warn("CRParser.parseRegion(): unknown island " + sc.argv[0]
              + " with region " + region + " in line " + sc.lnr);
        }

        sc.getNextToken();
      } else if ((sc.argc == 2) && sc.argv[1].equalsIgnoreCase("prepared_mapline")) {
        // FFTools2-Region-Tag
        region.putTag(sc.argv[1], sc.argv[0]);
        sc.getNextToken();
      } else if ((sc.argc == 2) && sc.argv[1].equalsIgnoreCase("mapline")) {
        // FFTools2-Region-Tag
        region.putTag(sc.argv[1], sc.argv[0]);
        sc.getNextToken();
      } else if ((sc.argc == 2) && sc.argv[1].equalsIgnoreCase("Lohn")) {
        region.setWage(Integer.parseInt(sc.argv[0]));
        sc.getNextToken();
      } else if ((sc.argc == 2) && sc.argv[1].equalsIgnoreCase("letzterlohn")) {
        region.setOldWage(Integer.parseInt(sc.argv[0]));
        sc.getNextToken();
      } else if ((sc.argc == 2) && sc.argv[1].equalsIgnoreCase("aktiveRegion")) {
        world.setActiveRegion(region);
        sc.getNextToken();
      } else if ((sc.argc == 2) && sc.argv[1].equalsIgnoreCase("Terrain")) {
        try {
          final RegionType type = world.getRules().getRegionType(StringID.create(sc.argv[0]), true);
          region.setType(type);
        } catch (final IllegalArgumentException e) {
          // can happen in StringID constructor if sc.argv[0] == ""
          CRParser.log
              .warn("CRParser.parseRegion(): found region without a valid region type in line "
                  + sc.lnr);
        }

        // regions doesn't have name if name == type; e.g. "Ozean"=="Ozean"
        if (region.getType() != null) {
          if (region.getType().getName() != null) {
            // could set region name here...
          } else {
            CRParser.log
                .warn("CRParser.parseRegion(): found region type without a valid name in line "
                    + sc.lnr);
          }
        } else {
          CRParser.log
              .warn("CRParser.parseRegion(): found region without a valid region type in line "
                  + sc.lnr);
        }

        iValidateFlags |= 1;
        sc.getNextToken();
      } else if ((sc.argc == 2) && sc.argv[1].equalsIgnoreCase("owner")) {
        final Faction f =
            world.getFaction(EntityID.createEntityID(Integer.parseInt(sc.argv[0]), world.base));
        region.setOwnerFaction(f);
        sc.getNextToken();
      } else if ((sc.argc == 2) && sc.argv[1].equalsIgnoreCase("morale")) {
        region.setMorale(Integer.parseInt(sc.argv[0]));
        sc.getNextToken();
      } else if ((sc.argc == 2) && sc.argv[1].equalsIgnoreCase("mourning")) {
        final int val = Integer.parseInt(sc.argv[0]);
        region.setMourning(val);
        sc.getNextToken();
      } else if ((sc.argc == 2) && sc.argv[1].equalsIgnoreCase("Baeume")) {
        region.setTrees(Integer.parseInt(sc.argv[0]));
        sc.getNextToken();
      } else if ((sc.argc == 2) && sc.argv[1].equalsIgnoreCase("letztebaeume")) {
        region.setOldTrees(Integer.parseInt(sc.argv[0]));
        sc.getNextToken();
      } else if ((sc.argc == 2) && sc.argv[1].equalsIgnoreCase("Bauern")) {
        region.setPeasants(Integer.parseInt(sc.argv[0]));
        sc.getNextToken();
      } else if ((sc.argc == 2) && sc.argv[1].equalsIgnoreCase("letztebauern")) {
        region.setOldPeasants(Integer.parseInt(sc.argv[0]));
        sc.getNextToken();
      } else if ((sc.argc == 2) && sc.argv[1].equalsIgnoreCase("Silber")) {
        region.setSilver(Integer.parseInt(sc.argv[0]));
        sc.getNextToken();
      } else if ((sc.argc == 2) && sc.argv[1].equalsIgnoreCase("letztessilber")) {
        region.setOldSilver(Integer.parseInt(sc.argv[0]));
        sc.getNextToken();
      } else if ((sc.argc == 2) && sc.argv[1].equalsIgnoreCase("Eisen")) {
        region.setIron(Integer.parseInt(sc.argv[0]));
        sc.getNextToken();
      } else if ((sc.argc == 2) && sc.argv[1].equalsIgnoreCase("letzteseisen")) {
        region.setOldIron(Integer.parseInt(sc.argv[0]));
        sc.getNextToken();
      } else if ((sc.argc == 2) && sc.argv[1].equalsIgnoreCase("Laen")) {
        region.setLaen(Integer.parseInt(sc.argv[0]));
        sc.getNextToken();
      } else if ((sc.argc == 2) && sc.argv[1].equalsIgnoreCase("letzteslaen")) {
        region.setOldLaen(Integer.parseInt(sc.argv[0]));
        sc.getNextToken();
      } else if ((sc.argc == 2) && sc.argv[1].equalsIgnoreCase("Adamantium")) {
        // we don't need to record it, it will also appear under RESOURCES
        region.putTag("Adamantium", sc.argv[0]);
        sc.getNextToken();
      } else if ((sc.argc == 2) && sc.argv[1].equalsIgnoreCase("Pferde")) {
        region.setHorses(Integer.parseInt(sc.argv[0]));
        sc.getNextToken();
      } else if ((sc.argc == 2) && sc.argv[1].equalsIgnoreCase("letztepferde")) {
        region.setOldHorses(Integer.parseInt(sc.argv[0]));
        sc.getNextToken();
      } else if ((sc.argc == 2) && sc.argv[1].equalsIgnoreCase("Unterh")) {
        // Has not to be stored.

        sc.getNextToken();

        // pavkovic 2002.05.10: recruits (and old recruits are used from cr)
      } else if ((sc.argc == 2) && sc.argv[1].equalsIgnoreCase("Rekruten")) {
        region.setRecruits(Integer.parseInt(sc.argv[0]));
        sc.getNextToken();
      } else if ((sc.argc == 2) && sc.argv[1].equalsIgnoreCase("letzterekruten")) {
        region.setOldRecruits(Integer.parseInt(sc.argv[0]));
        sc.getNextToken();
      } else if ((sc.argc == 2) && sc.argv[1].equalsIgnoreCase("maxLuxus")) {
        // Has not to be stored.
        sc.getNextToken();
      } else if ((sc.argc == 2) && sc.argv[1].equalsIgnoreCase("letzteluxus")) {
        region.setOldLuxuries(Integer.parseInt(sc.argv[0]));
        sc.getNextToken();
      } else if ((sc.argc == 2) && sc.argv[1].equalsIgnoreCase("Mallorn")) {
        if (Integer.parseInt(sc.argv[0]) > 0) {
          region.setMallorn(true);
        } else {
          region.setMallorn(false);
        }

        sc.getNextToken();
      } else if ((sc.argc == 2) && sc.argv[1].equalsIgnoreCase("herb")) {
        final ItemType type = world.getRules().getItemType(StringID.create(sc.argv[0]), true);
        region.setHerb(type);
        sc.getNextToken();
      } else if ((sc.argc == 2) && sc.argv[1].equalsIgnoreCase("herbamount")) {
        region.setHerbAmount(sc.argv[0]);
        sc.getNextToken();
      } else if ((sc.argc == 2) && (sc.argv[1].compareTo("Runde") == 0)) {
        // ignore this tag
        sc.getNextToken();
      } else if ((sc.argc == 2) && sc.argv[1].equalsIgnoreCase("Verorkt")) {
        region.setOrcInfested(true);
        sc.getNextToken();
      } else if ((sc.argc == 2) && sc.argv[1].equalsIgnoreCase("Schoesslinge")) {
        region.setSprouts(Integer.parseInt(sc.argv[0]));
        sc.getNextToken();
      } else if ((sc.argc == 2) && sc.argv[1].equalsIgnoreCase("letzteSchoesslinge")) {
        region.setOldSprouts(Integer.parseInt(sc.argv[0]));
        sc.getNextToken();
      } else if ((sc.argc == 2)
          && (sc.argv[1].equalsIgnoreCase("Steine") || sc.argv[1].equalsIgnoreCase("Stein"))) {
        region.setStones(Integer.parseInt(sc.argv[0]));
        sc.getNextToken();
      } else if ((sc.argc == 2) && sc.argv[1].equalsIgnoreCase("letztesteine")) {
        region.setOldStones(Integer.parseInt(sc.argv[0]));
        sc.getNextToken();
      } else if ((sc.argc == 2) && sc.argv[1].equalsIgnoreCase("regionicon")) {
        region.putTag(sc.argv[1], sc.argv[0]);
        sc.getNextToken();
      } else if ((sc.argc == 2) && sc.argv[1].equalsIgnoreCase("visibility")) {
        region.setVisibilityString(sc.argv[0]);
        sc.getNextToken();
      } else if (isBookmark()) {
        parseBookmark(region);
      } else if (sc.isBlock && sc.argv[0].equals("PREISE")) {
        region.setPrices(parsePrices(region.getPrices()));
      } else if (sc.isBlock && sc.argv[0].equals("LETZTEPREISE")) {
        region.setOldPrices(parsePrices(region.getOldPrices()));
      } else if (sc.isBlock && sc.argv[0].startsWith("GRENZE ")) {
        parseBorders(region);
      } else if (sc.isBlock && sc.argv[0].startsWith("SIGN ")) {
        parseSigns(region);
      } else if (sc.isBlock && sc.argv[0].startsWith("EINHEIT ")) {
        unitSortIndex = parseUnit(region, ++unitSortIndex);
      } else if (sc.isBlock && sc.argv[0].startsWith("ALTEINHEIT ")) {
        unitSortIndex = parseUnit(region, ++unitSortIndex, "ALTEINHEIT");
      } else if (sc.isBlock && sc.argv[0].startsWith("SCHIFF ")) {
        parseShip(region, ++shipSortIndex);
      } else if (sc.isBlock && sc.argv[0].startsWith("BURG ")) {
        parseBuilding(region, ++buildingSortIndex);
      } else if (sc.isBlock && sc.argv[0].startsWith("MESSAGE ")) {
        region.setMessages(parseMessages(region.getMessages()));
      } else if (sc.isBlock && sc.argv[0].equals("REGIONSEREIGNISSE")) {
        region.setEvents(parseMessageSequence(region.getEvents()));

        /*
         * } else if(sc.argc == 1 && sc.argv[0].equals("REGIONSKOMMENTAR")) { region.comments =
         * parseMessageSequence(region.comments);
         */
      } else if (sc.isBlock && sc.argv[0].equals("REGIONSBOTSCHAFTEN")) {
        region.setPlayerMessages(parseMessageSequence(region.getPlayerMessages()));
      } else if (sc.isBlock && sc.argv[0].equals("UMGEBUNG")) {
        region.setSurroundings(parseMessageSequence(region.getSurroundings()));
      } else if (sc.isBlock && sc.argv[0].equals("DURCHREISE")) {
        region.setTravelThru(parseMessageSequence(region.getTravelThru()));
      } else if (sc.isBlock && sc.argv[0].equals("DURCHSCHIFFUNG")) {
        region.setTravelThruShips(parseMessageSequence(region.getTravelThruShips()));
      } else if (sc.isBlock && sc.argv[0].equals("EFFECTS")) {
        region.setEffects(parseStringSequence(region.getEffects()));
      } else if ((sc.isBlock) && sc.argv[0].equals("ATTRIBUTES")) {
        parseAttributes(region);
      } else if (sc.isBlock && sc.argv[0].equals("COMMENTS")) {
        region.setComments(parseStringSequence(region.getComments()));
      } else if (sc.isBlock && sc.argv[0].startsWith("RESOURCE ")) {
        final RegionResource res = parseRegionResource(world.getRules(), region);

        if (res != null) {
          region.addResource(res);
        }
      } else if (sc.isBlock && sc.argv[0].startsWith("SCHEMEN ")) {
        parseScheme(region);
      } else if (sc.isBlock && sc.argv[0].equals("GEGENSTAENDE")) {
        /* not used in standard Eressea */
        parseItems(region);
      } else if (sc.isBlock && sc.argv[0].equals("MESSAGETYPES")) {
        break;
      } else if (sc.isBlock && sc.argv[0].startsWith("REGION ")) {
        break;
      } else if (sc.isBlock) {
        break;
      } else {
        if (sc.argc == 2) {
          region.putTag(sc.argv[1], sc.argv[0]);
        }

        unknown("REGION", true);
      }
    }
    tRParse += System.currentTimeMillis() - t;

    // validate region before add to world data
    if ((iValidateFlags & 1) == 0) {
      if (Region.VIS_STR_WRAP.equals(region.getVisibilityString())) {
        region.setType(RegionType.wrap);
      } else {
        CRParser.log.warn("Warning: No region type is given for region '" + region.toString()
            + "' - it is ignored.");
        region.setType(RegionType.unknown);
      }
      world.addRegion(region);
    } else {
      world.addRegion(region);
    }
  }

  /**
   * DOCUMENT-ME What is a "SPEZIALREGION"?
   */
  private Region parseSpecialRegion(Region specialRegion) throws IOException {
    int unitSortIndex = 0;
    sc.getNextToken(); // skip "SPEZIALREGION x y"

    if (specialRegion == null) {
      CoordinateID c = CoordinateID.create(0, 0, 1);
      c = originTranslate(c);

      while (world.getRegion(c) != null) {
        // FIXME whats's this??
        c = CoordinateID.create(0, 0, (int) (Math.random() * (Integer.MAX_VALUE - 1)) + 1);
        c = originTranslate(c);
      }

      specialRegion = MagellanFactory.createRegion(c, world);
      specialRegion.setName("Astralregion");
      world.addRegion(specialRegion);
    }

    while (!sc.eof) {
      if ((sc.argc == 2) && sc.argv[1].equalsIgnoreCase("terrain")) {
        specialRegion.setType(world.getRules().getRegionType(StringID.create(sc.argv[0]), true));
        sc.getNextToken();
      } else if (sc.isBlock && sc.argv[0].startsWith("SCHEMEN ")) {
        parseScheme(specialRegion);
      } else if (sc.isBlock && sc.argv[0].startsWith("MESSAGE ")) {
        specialRegion.setMessages(parseMessages(specialRegion.getMessages()));
      } else if (sc.isBlock && sc.argv[0].equals("EFFECTS")) {
        specialRegion.setEffects(parseStringSequence(specialRegion.getEffects()));
      } else if (sc.isBlock && sc.argv[0].equals("COMMENTS")) {
        specialRegion.setComments(parseStringSequence(specialRegion.getComments()));
      } else if (sc.isBlock && sc.argv[0].startsWith("GRENZE ")) {
        parseBorders(specialRegion);
      } else if (sc.isBlock && sc.argv[0].equals("REGIONSEREIGNISSE")) {
        specialRegion.setEvents(parseMessageSequence(specialRegion.getEvents()));
      } else if (sc.isBlock && sc.argv[0].equals("REGIONSBOTSCHAFTEN")) {
        specialRegion.setPlayerMessages(parseMessageSequence(specialRegion.getPlayerMessages()));
      } else if (sc.isBlock && sc.argv[0].equals("UMGEBUNG")) {
        specialRegion.setSurroundings(parseMessageSequence(specialRegion.getSurroundings()));
      } else if (sc.isBlock && sc.argv[0].equals("DURCHREISE")) {
        specialRegion.setTravelThru(parseMessageSequence(specialRegion.getTravelThru()));
      } else if (sc.isBlock && sc.argv[0].equals("DURCHSCHIFFUNG")) {
        specialRegion.setTravelThruShips(parseMessageSequence(specialRegion.getTravelThruShips()));
      } else if (sc.isBlock && sc.argv[0].startsWith("EINHEIT ")) {
        unitSortIndex = parseUnit(specialRegion, ++unitSortIndex);
      } else if (sc.isBlock) {
        break;
      } else {
        unknown("SPEZIALREGION", true);
      }
    }

    return specialRegion;
  }

  private RegionResource parseRegionResource(Rules rules, Region region) throws IOException {
    RegionResource r = null;
    ID id = null;
    ItemType type = null;
    id = LongID.create(sc.argv[0].substring(sc.argv[0].indexOf(" ", 0) + 1));
    sc.getNextToken(); // skip "RESOURCE id"

    while (!sc.eof && !sc.isBlock) {
      if ((sc.argc == 2) && sc.argv[1].equalsIgnoreCase("type")) {
        if (r == null) {
          type = rules.getItemType(StringID.create(sc.argv[0]), true);

          if (type != null) {
            r = new RegionResource(id, type);
            // if coming from server, just set the date to world-date
            // if later a ;runde tag is found, date is overwritten
            // added for testing. we onyl do this, if the report has NO
            // "Konfiguartion" tag...we assume than, its coming from the server...
            // if (this.configuration.equalsIgnoreCase("standard")){
            r.setDate(world.getDate().getDate());
            // }
          }
        }

        sc.getNextToken();
      } else if ((sc.argc == 2) && sc.argv[1].equalsIgnoreCase("skill")) {
        if (r != null) {
          r.setSkillLevel(Integer.parseInt(sc.argv[0]));
        }

        sc.getNextToken();
      } else if ((sc.argc == 2) && sc.argv[1].equalsIgnoreCase("Runde")) {
        if (r != null) {
          r.setDate(Integer.parseInt(sc.argv[0]));
        }
        sc.getNextToken();
      } else if ((sc.argc == 2) && sc.argv[1].equalsIgnoreCase("number")) {
        if (r != null) {
          r.setAmount(Integer.parseInt(sc.argv[0]));
        }

        sc.getNextToken();
      } else {
        unknown("RESOURCE", true);
      }
    }

    return r;
  }

  private void parseScheme(Region region) throws IOException {
    CoordinateID c = CoordinateID.parse(sc.argv[0].substring(sc.argv[0].indexOf(" ", 0)), " ");
    if (c == null) {
      unknown("SCHEMEN", true);
      return;
    }
    c = originTranslate(c);

    sc.getNextToken(); // skip "SCHEMEN x y"

    final Scheme scheme = MagellanFactory.createScheme(c);
    region.addScheme(scheme);

    while (!sc.eof) {
      if ((sc.argc == 2) && sc.argv[1].equalsIgnoreCase("name")) {
        scheme.setName(sc.argv[0]);
        sc.getNextToken();
      } else if (sc.isBlock) {
        break;
      } else {
        unknown("SCHEMEN", true);
      }
    }

    // add scheme region as a normal region with unknown region type
    if (world.getRegion(c) == null) {
      final Region newRegion = MagellanFactory.createRegion(c, world);
      newRegion.setName(scheme.getName());
      world.addRegion(newRegion);
    }
  }

  /**
   * This function parses the informations found in Reader in and creates a corresponding GameData
   * object tree.
   *
   * @param in Reader to cr file
   * @param data GameData to be filled with informations of given cr file This function is
   *          synchronized.
   * @throws IOException If an I/O error occurs
   * @see magellan.library.io.GameDataIO#read(java.io.Reader, magellan.library.GameData)
   */
  public synchronized GameData read(Reader in, GameData data) throws IOException {
    boolean bCorruptReportMsg = false;
    int regionSortIndex = 0;
    // Fiete 20061208
    // set finalizer prio to max
    MemoryManagment.setFinalizerPriority(Thread.MAX_PRIORITY);

    ui.setTitle(Resources.get("progressdialog.loadcr.title"));
    ui.setMaximum(10000);
    ui.setProgress(Resources.get("progressdialog.loadcr.step01"), 1);
    ui.show();

    CRParser.tUParse = CRParser.tRParse = 0;

    try {
      world = data;
      sc = new Scanner(in);
      sc.getNextToken();
      boolean oome = false;

      while (!sc.eof) {
        try {
          if (sc.argv[0].startsWith("VERSION")) {
            ui.setProgress(Resources.get("progressdialog.loadcr.step02"), 2);
            parseHeader();
          } else if ((sc.argc == 1) && sc.argv[0].startsWith("REGION ")) {
            if (!bCorruptReportMsg) {
              CRParser.log.warn("Warning: This computer report is "
                  + "missing the header and is therefore invalid or "
                  + "corrupted. Please contact the originator of this "
                  + "report if you experience data loss.");
              bCorruptReportMsg = true;
            }

            parseRegion(++regionSortIndex);
          } else {
            unknown("top level", true);
          }
        } catch (final OutOfMemoryError ome) {
          CRParser.log.error(ome);
          oome = true;
        }

        setOwner(data);

        // Fiete 20061208 check Memory
        if (!MemoryManagment.isFreeMemory() || oome) {
          // we have a problem..
          // like in startup of client..we reset the data
          world = new MissingData();
          // marking the problem
          world.setOutOfMemory(true);

          // ui.ready();
          // end exit
          return world;
        }

      }
      world.setMaxSortIndex(++regionSortIndex);
    } catch (final RuntimeException e) {
      // ui.ready();
      throw e;
    } finally {
      // FIXME(stm) this could be too soon if called via the load menu!
      ui.ready();
    }

    log.fine(CRParser.tRParse + " " + CRParser.tUParse);

    CRParser.log.fine("Done reading.");

    return world;
  }

  private void setOwner(GameData newData) {
    if (newData.getOwnerFaction() == null) {
      // in standard reports, the first faction of the report should always be the owner faction
      final Faction firstFaction2 = getFirstFaction();
      if (getConfiguration() != null && getConfiguration().equals("Standard")
          && firstFaction2 != null) {
        newData.setOwnerFaction(firstFaction2.getID());
        CRParser.log.info("setOwner of Report to: " + firstFaction2.toString());
        // set translation to (0,0,...) in all existing layers
        final Set<Integer> layers = new HashSet<Integer>();
        for (Region r : newData.getRegions()) {
          // for (final CoordinateID coord : newData.regions().keySet()) {
          CoordinateID coord = r.getCoordinate();
          if (!layers.contains(coord.getZ())) {
            newData.setCoordinateTranslation(firstFaction2.getID(), CoordinateID.create(0, 0, coord
                .getZ()));
            layers.add(coord.getZ());
          }
        }
        CRParser.log.info("Layers updated with translation 0,0 for " + firstFaction2.toString());
      }
    }
  }

  private void invalidParam(String method, String msg) {
    CRParser.log.warn("CRParser." + method + "(): invalid parameter specified, " + msg
        + "! Unable to parse block in line " + sc.lnr);
  }

  private void parseTranslation(GameData data) throws IOException {
    sc.getNextToken();

    while (!sc.eof) {
      if (sc.argc == 2) {
        // data.addTranslation(sc.argv[1], sc.argv[0]);
        data.addTranslation(sc.argv[1], sc.argv[0], TranslationType.SOURCE_CR);
        sc.getNextToken();
      } else if (sc.isBlock) {
        break;
      } else {
        unknown("TRANSLATION", true);
      }
    }
  }

  /**
   * This method reads all attributes from a CR "value":key
   */
  private void parseAttributes(Addeable addeable) throws IOException {
    sc.getNextToken();

    while (!sc.eof) {
      if (sc.argc == 2) {
        addeable.addAttribute(sc.argv[1], sc.argv[0]);
        sc.getNextToken();
      } else if (sc.isBlock) {
        break;
      }
    }
  }

  private void parseUnknownBlock() throws IOException {
    String block = sc.argv[0];
    unknown("start of unknown block ", true);

    while (!sc.eof) {
      if (!sc.isBlock) {
        unknown(block, true, Logger.INFO);
      } else
        return;
    }
  }

  // (stm) the following is an experiment to simplify the parser using a tag map and Java Beans. It
  // didn't bring the performance boost I had hoped for...

  /**
   * A state object that is maintained by the parser. Individual handlers should implement their own
   * state.
   *
   * @author stm
   */
  public interface ParseState {
    // marker interface
  }

  /**
   * A class that handles individual tags or blocks as they are read by the parser.
   *
   * @author stm
   */
  public static abstract class TagHandler {
    /**
     * The parser that uses this handler.
     */
    public BlockParser parserReference;
    /**
     * The scanner used by the parser.
     */
    public Scanner sc;

    /**
     * Initializes the handler.
     *
     * @param parser The parser that uses this handler
     */
    public TagHandler(BlockParser parser) {
      parserReference = parser;
      sc = parser.sc;
    }

    /**
     * Handles a tag or block, probably by accessing {@link #sc}.
     *
     * @param state The current state
     * @return true if the tag has been handled.
     * @throws IOException If the scanner throws an exception.
     */
    public abstract boolean handle(ParseState state) throws IOException;

  }

  /**
   * A Parse state that can be used by {@link BeanHandler}.
   *
   * @author stm
   */
  public static interface BeanState extends ParseState {

    /**
     * @return The report base {@link GameData#base}.
     */
    public int getBase();

    /**
     * The bean that is modified by the handler.
     *
     * @return the bean that is modified by the handler.
     */
    public Object getBean();
  }

  /**
   * A ParseState for parsing unit blocks ("EINHEIT").
   *
   * @author stm
   */
  public static class UnitParseState implements BeanState {

    /**
     * The new unit object.
     */
    public Unit unit;
    /**
     * The unit's factionID if it has been read.
     */
    public EntityID factionID;
    /**
     * The unit's groupID if it has been read.
     */
    public ID groupID;
    private int base;

    /**
     * Initializes the parser.
     *
     * @param unit The new unit
     * @param base The report base, see {@link GameData#base}.
     */
    public UnitParseState(Unit unit, int base) {
      this.unit = unit;
      this.base = base;
    }

    public int getBase() {
      return base;
    }

    public Object getBean() {
      return unit;
    }

  }

  /**
   * A handler that handles unit tags or blocks.
   *
   * @author stm
   */
  public static abstract class UnitTagHandler extends TagHandler {

    protected UnitParseState parseState;

    /**
     * Creates a new handler
     *
     * @param unitParser The parser calling this handler.
     */
    public UnitTagHandler(BlockParser unitParser) {
      super(unitParser);
    }

    /**
     * Handle a unit tag (acces via the Scanner)
     *
     * @param unit The new unit object
     * @throws IOException if the scanner throws an exception
     */
    public abstract void handle(Unit unit) throws IOException;

    @Override
    public boolean handle(ParseState state) throws IOException {
      parseState = (UnitParseState) state;
      handle(parseState.unit);
      return true;
    }
  }

  /**
   * The fallback handler for unit tags.
   *
   * @author stm
   */
  public static class UnitDefaultTagHandler extends TagHandler {

    private String blockName;

    /**
     * @param parser
     */
    public UnitDefaultTagHandler(BlockParser parser) {
      super(parser);
    }

    @Override
    public boolean handle(ParseState state) throws IOException {
      UnitParseState parseState = (UnitParseState) state;

      parseState.unit.putTag(parserReference.sc.argv[1], parserReference.sc.argv[0]);
      boolean handled = false;
      // check for wellknown tags...ejcTaggable etc...
      if (parserReference.sc.argv[1].equalsIgnoreCase(CRParser.TAGGABLE_STRING)) {
        handled = true;
      }
      if (parserReference.sc.argv[1].equalsIgnoreCase(CRParser.TAGGABLE_STRING2)) {
        handled = true;
      }
      if (parserReference.sc.argv[1].equalsIgnoreCase(CRParser.TAGGABLE_STRING3)) {
        handled = true;
      }
      if (parserReference.sc.argv[1].equalsIgnoreCase(CRParser.TAGGABLE_STRING4)) {
        handled = true;
      }
      if (parserReference.sc.argv[1].equalsIgnoreCase(CRParser.TAGGABLE_STRING5)) {
        handled = true;
      }
      if (!handled) {
        parserReference.crParser.unknown(blockName, false);
        return false;
      }
      return true;
    }

    public void setBlockName(String blockName) {
      this.blockName = blockName;
    }
  }

  /**
   * A ParseState for regions.
   *
   * @author stm
   */
  public static class RegionParseState implements BeanState {
    /**
     * The new region.
     */
    public Region region;
    private int base;
    protected int iValidateFlags;
    protected int unitSortIndex;
    protected int shipSortIndex;
    protected int buildingSortIndex;

    /**
     * @param region
     * @param base
     */
    public RegionParseState(Region region, int base) {
      this.region = region;
      this.base = base;
    }

    public int getBase() {
      return base;
    }

    public Object getBean() {
      return region;
    }

  }

  /**
   * A handler that handles tags or blocks in a region block ("REGION").
   *
   * @author stm
   */
  public static abstract class RegionTagHandler extends TagHandler {

    protected RegionParseState parseState;

    /**
     * @param unitParser
     */
    public RegionTagHandler(BlockParser unitParser) {
      super(unitParser);
    }

    /**
     * Handles a tag by accessing the scanner
     *
     * @param region The new region
     * @throws IOException if the scanner throws an exception.
     */
    public abstract void handle(Region region) throws IOException;

    @Override
    public boolean handle(ParseState state) throws IOException {
      parseState = (RegionParseState) state;
      handle(parseState.region);
      return true;
    }
  }

  /**
   * The fallback handler for regions.
   *
   * @author stm
   */
  public static abstract class RegionDefaultTagHandler extends TagHandler {

    /**
     * @param parser
     */
    public RegionDefaultTagHandler(BlockParser parser) {
      super(parser);
    }

    @Override
    public boolean handle(ParseState state) throws IOException {
      RegionParseState parseState = (RegionParseState) state;

      parseState.region.putTag(parserReference.sc.argv[1], parserReference.sc.argv[0]);

      parserReference.crParser.unknown("REGION", true);

      return false;
    }
  }

  /**
   * The bean type
   *
   * @author stm
   */
  public static enum Type {
    /**
     * Integer
     */
    INTEGER,
    /**
     * Long
     */
    LONG,
    /**
     * String
     */
    STRING,
    /**
     * 0 = false, true otherwise
     */
    ZERO_ONE,
    /**
     * UnitID
     */
    UNIT_ID
  }

  /**
   * A handler that accesses java bean properties.
   *
   * @author stm
   */
  public static class BeanHandler extends TagHandler {

    private Type type;
    private String propertyName;

    /**
     * Creates a bean handler that changes a property of a bean.
     *
     * @param parser The parser calling this handler.
     * @param propertyName The name of the property being modified
     * @param type The property type
     */
    public BeanHandler(BlockParser parser, String propertyName, Type type) {
      super(parser);
      this.type = type;
      this.propertyName = propertyName;
    }

    @Override
    public boolean handle(ParseState state) throws IOException {
      Object bean = ((BeanState) state).getBean();
      Object value = null;
      try {
        switch (type) {
        case INTEGER:
          setProperty(bean, propertyName, value = Integer.parseInt(parserReference.sc.argv[0]));
          break;
        case LONG:
          setProperty(bean, propertyName, value = Long.parseLong(parserReference.sc.argv[0]));
          break;
        case STRING:
          setProperty(bean, propertyName, value = parserReference.sc.argv[0]);
          break;
        case ZERO_ONE:
          setProperty(bean, propertyName, Integer.parseInt(parserReference.sc.argv[0]) != 0);
          break;
        case UNIT_ID:
          setProperty(bean, propertyName, UnitID.createUnitID(Integer
              .parseInt(parserReference.sc.argv[0]), ((BeanState) state).getBase()));
          break;
        }
      } catch (IllegalAccessException e) {
        throw new IllegalArgumentException("No access to setter method possible for property "
            + fullPropertyName(bean, propertyName) + " with value " + value);
      } catch (InvocationTargetException e) {
        throw new RuntimeException("Unexpected exception setting property "
            + fullPropertyName(bean, propertyName) + " with value " + value);
      } catch (NoSuchMethodException e) {
        throw new IllegalArgumentException("No setter method found for property "
            + fullPropertyName(bean, propertyName) + " with value " + value);
      }
      return true;
    }

    private static String fullPropertyName(Object bean, String propertyName) {
      return bean.getClass().getName() + '#' + propertyName;
    }

  }

  /**
   * A class to parse a block by reading properties and blocks using a scanner.
   *
   * @author stm
   */
  public static class BlockParser {

    private Map<String, TagHandler> tagHandlers;
    private Map<String, TagHandler> blockHandlers;

    /**
     * Creates a new parser.
     */
    public BlockParser() {
      tagHandlers = new HashMap<String, CRParser.TagHandler>();
      blockHandlers = new HashMap<String, CRParser.TagHandler>();
    }

    /**
     * Adds a handler that is called if the specified tag (i.e., a line like "<value>;tagName" is
     * encountered.
     *
     * @param tagName The name of the tag triggering this handler
     * @param handler The handler for the tag
     */
    public void addTagHandler(String tagName, TagHandler handler) {
      if (tagHandlers.put(tagName, handler) != null)
        throw new IllegalArgumentException("tag name already registered");

      if (!tagName.toLowerCase().equals(tagName))
        if (tagHandlers.put(tagName.toLowerCase(), handler) != null)
          throw new IllegalArgumentException("tag name already registered");

    }

    /**
     * Adds a handler that manipulates a bean.
     *
     * @param tagName The name of the tag triggering this handler
     * @param propertyName The property manipulated by the handler
     * @param type The type of the property
     */
    public void addBeanHandler(String tagName, String propertyName, Type type) {
      addTagHandler(tagName, new BeanHandler(this, propertyName, type));
    }

    /**
     * Adds a handler that does nothing (but doesn't cause an error) for the specified tag.
     *
     * @param tagName The tag triggering this handler
     */
    public void addNullHandler(String tagName) {
      addTagHandler(tagName, new TagHandler(this) {
        @Override
        public boolean handle(ParseState state) throws IOException {
          // never do anything
          return true;
        }
      });
    }

    /**
     * Add a handler for a block, called if a block line like "NAME ..." is read.
     *
     * @param name the block name, i.e. the first word of the BLOCK line.
     * @param tagHandler The handler that is called upon reading the block.
     */
    public void addBlockHandler(String name, TagHandler tagHandler) {
      if (name.contains(" "))
        throw new IllegalArgumentException("space not allowed in block name");
      if (blockHandlers.put(name, tagHandler) != null)
        throw new IllegalArgumentException("tag name already registered");
    }

    protected Scanner sc;
    protected CRParser crParser;
    private TagHandler defaultTagHandler;
    private TagHandler defaultBlockHandler;
    private String blockName;

    /**
     * Parses the block by calling {@link Scanner#getNextToken()} and calling appropriate handlers.
     * Terminates if a block is encountered that has no handler or on end of file.
     *
     * @param crParser the CRParser, mainly used for calls to
     *          {@link CRParser#unknown(String, boolean)}.
     * @param state The initial state which is passed to handlers.
     * @throws IOException If the scanner throws an exception
     */
    public void parse(CRParser crParser, ParseState state) throws IOException {
      this.crParser = crParser;
      sc = crParser.sc;
      // for (TagHandler handler: tagHandlers.values()) handler.init();
      // for (TagHandler handler: blockHandlers.values()) handler.init();

      sc.getNextToken(); // skip "EINHEIT nr"

      while (!sc.eof) {
        if (sc.argc == 2) {
          TagHandler handler = tagHandlers.get(sc.argv[1]);
          if (handler == null) {
            handler = tagHandlers.get(sc.argv[1].toLowerCase());
          }
          if (handler != null) {
            handler.handle(state);
          } else if (getDefaultTagHandler() != null) {
            getDefaultTagHandler().handle(state);
          } else {
            crParser.unknown("unknown context", false);
          }
          sc.getNextToken();
        } else if (sc.isBlock) {
          TagHandler handler;
          if (sc.argv[0].contains(" ")) {
            handler = blockHandlers.get(sc.argv[0].substring(0, sc.argv[0].indexOf(" ")));
          } else {
            handler = blockHandlers.get(sc.argv[0]);
          }
          if (handler != null) {
            handler.handle(state);
          } else if (getDefaultBlockHandler() != null) {
            getDefaultBlockHandler().handle(state);
          } else {
            break;
          }
        }
        // TODO add handler for sc.argc==1 and sc.isBlock==false
      }
    }

    /**
     * Returns the value of defaultTagHandler.
     *
     * @return Returns defaultTagHandler.
     */
    public TagHandler getDefaultTagHandler() {
      return defaultTagHandler;
    }

    /**
     * Sets the value of defaultTagHandler.
     *
     * @param defaultTagHandler The value for defaultTagHandler.
     */
    public void setDefaultTagHandler(TagHandler defaultTagHandler) {
      this.defaultTagHandler = defaultTagHandler;
    }

    /**
     * Returns the value of defaultBlockHandler.
     *
     * @return Returns defaultBlockHandler.
     */
    public TagHandler getDefaultBlockHandler() {
      return defaultBlockHandler;
    }

    /**
     * Sets the value of defaultBlockHandler.
     *
     * @param defaultBlockHandler The value for defaultBlockHandler.
     */
    public void setDefaultBlockHandler(TagHandler defaultBlockHandler) {
      this.defaultBlockHandler = defaultBlockHandler;
    }

    public void setBlockName(String blockName) {
      this.blockName = blockName;
    }

  }

  private int parseUnitFast(Region region, int sortIndex) throws IOException {
    return parseUnitFast(region, sortIndex, "EINHEIT");
  }

  private int parseUnitFast(Region region, int sortIndex, String blockName) throws IOException {
    final boolean oldUnit;
    final Unit newUnit =
        getAddUnit(UnitID
            .createUnitID(sc.argv[0].substring(blockName.length() + 1), 10, world.base), oldUnit =
                blockName.equals("ALTEINHEIT"));

    if (region != newUnit.getRegion()) {
      if (newUnit.getRegion() != null) {
        log.warn(newUnit + " " + newUnit.getRegion() + " --> " + region);
      }
      newUnit.setRegion(region);
    }

    UnitDefaultTagHandler defaultUnitParser = null;
    /**
     * this was created by regexp replacements: <code> if \(\(sc\.argc == 2\) &&
     * sc\.argv\[1\]\.equalsIgnoreCase\(("[^"]*")\)\) \{(\R[^#]*)sc\.getNextToken\(\);\R \}#else
     * parser.addTagHandler(\1, new TagHandler() { public void handle(Unit unit){\2}}); if
     * \(\(sc\.isBlock\) && sc\.argv\[0\]\.equals\(("[^"]*")\)\) \{(\R[^#]*)\R \}#else
     * parser.addBlockHandler(\1, new TagHandler() { public void handle(Unit unit) throws
     * IOException {\2}}); <code>
     */
    if (unitParser == null) {
      unitParser = new BlockParser();

      unitParser.addBeanHandler("Name", "name", Type.STRING);
      unitParser.addBeanHandler("Beschr", "description", Type.STRING);
      unitParser.addTagHandler("Typ", new UnitTagHandler(unitParser) {
        @Override
        public void handle(Unit unit) {
          unit.setRace(world.getRules().getRace(StringID.create(sc.argv[0]), true));
        }
      });
      unitParser.addTagHandler("wahrerTyp", new UnitTagHandler(unitParser) {
        @Override
        public void handle(Unit unit) {
          unit.setRealRace(world.getRules().getRace(StringID.create(sc.argv[0]), true));
        }
      });
      unitParser.addTagHandler("temp", new UnitTagHandler(unitParser) {
        @Override
        public void handle(Unit unit) {
          unit.setTempID(UnitID.createUnitID(Integer.parseInt(sc.argv[0]), world.base));
        }
      });
      unitParser.addTagHandler("alias", new UnitTagHandler(unitParser) {
        @Override
        public void handle(Unit unit) {
          unit.setAlias(UnitID.createUnitID(Integer.parseInt(sc.argv[0]), world.base));
        }
      });
      unitParser.addBeanHandler("privat", "privDesc", Type.STRING);
      unitParser.addBeanHandler("Anzahl", "persons", Type.INTEGER);
      unitParser.addTagHandler("Partei", new UnitTagHandler(unitParser) {
        @Override
        public void handle(Unit unit) {
          parseState.factionID =
              (EntityID.createEntityID(Integer.parseInt(sc.argv[0]), world.base));
        }
      });
      unitParser.addNullHandler("Parteiname");
      unitParser.addBeanHandler("Parteitarnung", "hideFaction", Type.ZERO_ONE);
      unitParser.addTagHandler("bewacht", new UnitTagHandler(unitParser) {
        @Override
        public void handle(Unit unit) {
          unit.setGuard(Integer.parseInt(sc.argv[0]));

          final Region r = unit.getRegion();

          if (r != null) {
            r.addGuard(unit);
          }

        }
      });
      unitParser.addTagHandler("belagert", new UnitTagHandler(unitParser) {
        @Override
        public void handle(Unit unit) {
          unit.setSiege(getAddBuilding(EntityID.createEntityID(Integer.parseInt(sc.argv[0]),
              world.base)));
        }
      });
      unitParser.addTagHandler("folgt", new UnitTagHandler(unitParser) {
        @Override
        public void handle(Unit unit) {
          unit.setFollows(getAddUnit(UnitID.createUnitID(Integer.parseInt(sc.argv[0]), world.base),
              oldUnit));
        }
      });
      unitParser.addBeanHandler("familiarmage", "familiarmageID", Type.UNIT_ID);
      unitParser.addTagHandler("Silber", new UnitTagHandler(unitParser) {
        @Override
        public void handle(Unit unit) {
          final int money = Integer.parseInt(sc.argv[0]);
          final Item item =
              new Item(world.getRules().getItemType(EresseaConstants.I_USILVER, true), money);
          unit.addItem(item);
        }
      });
      unitParser.addTagHandler("Burg", new UnitTagHandler(unitParser) {
        @Override
        public void handle(Unit unit) {
          Integer.parseInt(sc.argv[0]);

          final Building b =
              getAddBuilding(EntityID.createEntityID(Integer.parseInt(sc.argv[0]), world.base));

          if (unit.getBuilding() != b) {
            unit.setBuilding(b);
          }

        }
      });
      unitParser.addTagHandler("Schiff", new UnitTagHandler(unitParser) {
        @Override
        public void handle(Unit unit) {
          final Ship s =
              getAddShip(EntityID.createEntityID(Integer.parseInt(sc.argv[0]), world.base));

          if (unit.getShip() != s) {
            unit.setShip(s);
          }

        }
      });
      unitParser.addTagHandler("Kampfstatus", new UnitTagHandler(unitParser) {
        @Override
        public void handle(Unit unit) {
          // pre 57:
          // 0: VORNE
          // 1: HINTEN
          // 2: NICHT
          // 3: FLIEHE
          //
          // 57 and later:
          // 0 AGGRESSIV: 1. Reihe, flieht nie.
          // 1 VORNE: 1. Reihe, kaempfen bis 20% HP
          // 2 HINTEN: 2. Reihe, kaempfen bis 20% HP
          // 3 DEFENSIV: 2. Reihe, kaempfen bis 90% HP
          // 4 NICHT: 3. Reihe, kaempfen bis 90% HP
          // 5 FLIEHE: 4. Reihe, flieht immer.
          unit.setCombatStatus(Integer.parseInt(sc.argv[0]));

          // convert status from old to new
          if (version < 57) {
            unit.setCombatStatus(unit.getCombatStatus() + 1);

            if (unit.getCombatStatus() > 2) {
              unit.setCombatStatus(unit.getCombatStatus() + 1);
            }
          }

        }
      });
      unitParser.addBeanHandler("unaided", "unaided", Type.ZERO_ONE);
      unitParser.addBeanHandler("Tarnung", "stealth", Type.INTEGER);
      unitParser.addBeanHandler("Aura", "aura", Type.INTEGER);
      unitParser.addBeanHandler("Auramax", "auraMax", Type.INTEGER);
      unitParser.addBeanHandler("hp", "health", Type.STRING);
      unitParser.addBeanHandler("hunger", "starving", Type.ZERO_ONE);
      unitParser.addBeanHandler("ejcOrdersConfirmed", "ordersConfirmed", Type.ZERO_ONE);
      unitParser.addTagHandler("gruppe", new UnitTagHandler(unitParser) {
        @Override
        public void handle(Unit unit) {
          parseState.groupID = IntegerID.create(sc.argv[0]);
        }
      });
      unitParser.addBeanHandler("verraeter", "spy", Type.ZERO_ONE);

      /*
       * currently, verkleidung was announced but it seems that anderepartei is used. Please remove
       * one as soon as it is clear which one can be discarded
       */
      TagHandler verkleidungHandler;
      unitParser.addTagHandler("verkleidung", verkleidungHandler = new UnitTagHandler(unitParser) {
        @Override
        public void handle(Unit unit) {
          final EntityID fid = EntityID.createEntityID(Integer.parseInt(sc.argv[0]), world.base);

          /*
           * currently (2004-02) the cr is inconsistent with nr. There may be a situation where the
           * corresponding faction of this tag does not exist in the game data so add it
           * automagically (bugzilla bug 794).
           */
          Faction faction = world.getFaction(fid);

          if (faction == null) {
            faction = MagellanFactory.createFaction(fid, world);
          }

          unit.setGuiseFaction(faction);
        }
      });
      unitParser.addTagHandler("anderepartei", verkleidungHandler);

      unitParser.addBeanHandler("typprefix", "raceNamePrefix", Type.STRING);
      unitParser.addNullHandler("ladung");
      unitParser.addNullHandler("kapazitaet");
      unitParser.addBeanHandler("hero", "hero", Type.ZERO_ONE);
      unitParser.addBeanHandler("weight", "weight", Type.INTEGER);

      unitParser.addBlockHandler("COMMANDS", new UnitTagHandler(unitParser) {
        @Override
        public void handle(Unit unit) throws IOException {
          // there can be only one order block for a unit, replace existing ones
          unit.setOrders(parseStringSequence(null), false);
        }
      });
      unitParser.addBlockHandler("TALENTE", new UnitTagHandler(unitParser) {
        @Override
        public void handle(Unit unit) throws IOException {
          // there can be only one skills block for a unit, replace existing ones
          unit.clearSkills();
          parseSkills(unit);
        }
      });
      unitParser.addBlockHandler("SPRUECHE", new UnitTagHandler(unitParser) {
        @Override
        public void handle(Unit unit) throws IOException {
          // there can be only one spells block for a unit, replace existing ones
          unit.setSpells(parseUnitSpells(null));
        }
      });
      unitParser.addBlockHandler("GEGENSTAENDE", new UnitTagHandler(unitParser) {
        @Override
        public void handle(Unit unit) throws IOException {
          /*
           * in verdanon reports the silver can already be included in the items
           */
          parseItems(unit);
        }
      });
      unitParser.addBlockHandler("EINHEITSBOTSCHAFTEN", new UnitTagHandler(unitParser) {
        @Override
        public void handle(Unit unit) throws IOException {
          unit.setUnitMessages(parseMessageSequence(unit.getUnitMessages()));
        }
      });
      unitParser.addBlockHandler("COMMENTS", new UnitTagHandler(unitParser) {
        @Override
        public void handle(Unit unit) throws IOException {
          unit.setComments(parseStringSequence(unit.getComments()));
        }
      });
      unitParser.addBlockHandler("EFFECTS", new UnitTagHandler(unitParser) {
        @Override
        public void handle(Unit unit) throws IOException {
          unit.setEffects(parseStringSequence(unit.getEffects()));
        }
      });
      unitParser.addBlockHandler("KAMPFZAUBER", new UnitTagHandler(unitParser) {
        @Override
        public void handle(Unit unit) throws IOException {
          parseUnitCombatSpells(unit);
        }
      });
      unitParser.addBlockHandler("ATTRIBUTES", new UnitTagHandler(unitParser) {
        @Override
        public void handle(Unit unit) throws IOException {
          parseAttributes(unit);
        }
      });

      unitParser.setDefaultTagHandler(defaultUnitParser = new UnitDefaultTagHandler(unitParser));

    }

    unitParser.setBlockName(blockName);
    defaultUnitParser.setBlockName(blockName);

    long t = System.currentTimeMillis();
    UnitParseState parserState = new UnitParseState(newUnit, world.base);

    unitParser.parse(this, parserState);
    tUParse += System.currentTimeMillis() - t;

    // set the sortIndex so the original ordering of the units
    // can be restored
    newUnit.setSortIndex(sortIndex);

    EntityID factionID = parserState.factionID;
    if (factionID == null) {
      factionID = EntityID.createEntityID(-1, world.base);
    }
    ID groupID = parserState.groupID;

    final Faction faction = getAddFaction(factionID);

    if (faction.getName() == null) {
      if (factionID.intValue() == -1) {
        faction.setName(Resources.get("crparser.nofaction"));
      } else {
        faction.setName(Resources.get("crparser.unknownfaction", factionID));
      }
    }

    if (newUnit.getFaction() != faction) {
      if (newUnit.getFaction() != null) {
        log.warn(newUnit + " " + newUnit.getFaction() + " --> " + faction);
      }
      newUnit.setFaction(faction);
    }

    if (groupID != null) {
      Group g = null;

      if ((faction.getGroups() != null) && ((g = faction.getGroups().get(groupID)) != null)) {
        newUnit.setGroup(g);
      } else {
        CRParser.log.warn("CRParser.parseUnit(): Unable to assign group " + groupID + " to unit "
            + newUnit.getID());
      }
    }

    /*
     * a missing combat status can have two meanings: 1. this is a unit we know everything about and
     * the combat status is AGGRESSIVE 2. this is a unit we just see but does not belong to us so we
     * do not know its combat status.
     */
    if (!newUnit.ordersAreNull() && (newUnit.getCombatStatus() < 0)) {
      newUnit.setCombatStatus(0);
    }

    if (oldUnit) {
      newUnit.detach();
    }

    return sortIndex;
  }

  /**
   * Parse everything within one region.
   */
  @SuppressWarnings("unused")
  private void parseRegionFast(int sortIndex) throws IOException {

    CoordinateID c = CoordinateID.parse(sc.argv[0].substring(sc.argv[0].indexOf(" ", 0)), " ");

    if (c == null) {
      unknown("REGION", true);
      // without CoordinateID Region is not accessible
      return;
    }

    // this we should make after checking c!=null
    c = originTranslate(c);

    Region newregion = world.getRegion(c);

    if (newregion == null) {
      newregion = MagellanFactory.createRegion(c, world);
    }

    newregion.setSortIndex(sortIndex);

    if (regionParser == null) {
      regionParser = new BlockParser();

      regionParser.addTagHandler("Name", new RegionTagHandler(regionParser) {
        @Override
        public void handle(Region region) throws IOException {
          // regions doesn't have name if name == type; e.g. "Ozean"=="Ozean"
          region.setName(sc.argv[0]);
          ui.setProgress(Resources.get("progressdialog.loadcr.step03", new Object[] { region
              .getName() }), 2);
        }
      });
      // this does not work; I'm not sure why
      // regionParser.addBeanHandler("id", "uID", Type.LONG);
      regionParser.addTagHandler("id", new RegionTagHandler(regionParser) {
        @Override
        public void handle(Region region) throws IOException {
          region.setUID(Long.parseLong(sc.argv[0]));
        }
      });
      regionParser.addBeanHandler("Beschr", "description", Type.STRING);
      regionParser.addNullHandler("Strasse");
      regionParser.addTagHandler("ejcIsSelected", new RegionTagHandler(regionParser) {
        @Override
        public void handle(Region region) throws IOException {
          world.addSelectedRegionCoordinate(region);

        }
      });
      regionParser.addTagHandler("Insel", new RegionTagHandler(regionParser) {
        @Override
        public void handle(Region region) throws IOException {
          try {
            final IntegerID islandID = IntegerID.create(sc.argv[0]);
            Island island = world.getIsland(islandID);

            if (island == null) {
              CRParser.log.warn("CRParser.parseRegion(): unknown island " + sc.argv[0]
                  + " with region " + region + " in line " + sc.lnr + ", creating it dynamically.");
              island = new MagellanIslandImpl(islandID, world);
              island.setName(islandID.toString());
              world.addIsland(island);
            }
            region.setIsland(island);

          } catch (final NumberFormatException nfe) {
            CRParser.log.warn("CRParser.parseRegion(): unknown island " + sc.argv[0]
                + " with region " + region + " in line " + sc.lnr);
          }

        }
      });
      regionParser.addBeanHandler("Lohn", "wage", Type.INTEGER);

      regionParser.addBeanHandler("letzterlohn", "oldWage", Type.INTEGER);
      regionParser.addTagHandler("aktiveRegion", new RegionTagHandler(regionParser) {
        @Override
        public void handle(Region region) throws IOException {
          world.setActiveRegion(region);

        }
      });
      regionParser.addTagHandler("Terrain", new RegionTagHandler(regionParser) {
        @Override
        public void handle(Region region) throws IOException {
          try {
            final RegionType type =
                world.getRules().getRegionType(StringID.create(sc.argv[0]), true);
            region.setType(type);
          } catch (final IllegalArgumentException e) {
            // can happen in StringID constructor if sc.argv[0] == ""
            CRParser.log
                .warn("CRParser.parseRegion(): found region without a valid region type in line "
                    + sc.lnr);
          }

          // regions doesn't have name if name == type; e.g. "Ozean"=="Ozean"
          if (region.getType() != null) {
            if (region.getType().getName() != null) {
              // could set region name here...
            } else {
              CRParser.log
                  .warn("CRParser.parseRegion(): found region type without a valid name in line "
                      + sc.lnr);
            }
          } else {
            CRParser.log
                .warn("CRParser.parseRegion(): found region without a valid region type in line "
                    + sc.lnr);
          }

          parseState.iValidateFlags |= 1;

        }
      });
      regionParser.addTagHandler("owner", new RegionTagHandler(regionParser) {
        @Override
        public void handle(Region region) throws IOException {
          final Faction f =
              world.getFaction(EntityID.createEntityID(Integer.parseInt(sc.argv[0]), world.base));
          region.setOwnerFaction(f);

        }
      });
      regionParser.addBeanHandler("morale", "morale", Type.INTEGER);
      regionParser.addBeanHandler("mourning", "mourning", Type.INTEGER);

      regionParser.addBeanHandler("Baeume", "trees", Type.INTEGER);

      regionParser.addBeanHandler("letztebaeume", "oldTrees", Type.INTEGER);

      regionParser.addBeanHandler("Bauern", "peasants", Type.INTEGER);

      regionParser.addBeanHandler("letztebauern", "oldPeasants", Type.INTEGER);

      regionParser.addBeanHandler("Silber", "silver", Type.INTEGER);

      regionParser.addBeanHandler("letztessilber", "oldSilver", Type.INTEGER);

      regionParser.addBeanHandler("Eisen", "iron", Type.INTEGER);

      regionParser.addBeanHandler("letzteseisen", "oldIron", Type.INTEGER);

      regionParser.addBeanHandler("Laen", "laen", Type.INTEGER);

      regionParser.addBeanHandler("letzteslaen", "oldLaen", Type.INTEGER);
      regionParser.addTagHandler("Adamantium", new RegionTagHandler(regionParser) {
        @Override
        public void handle(Region region) throws IOException {
          // we don't need to record it, it will also appear under RESOURCES
          region.putTag("Adamantium", sc.argv[0]);

        }
      });
      regionParser.addBeanHandler("Pferde", "horses", Type.INTEGER);

      regionParser.addBeanHandler("letztepferde", "oldHorses", Type.INTEGER);
      regionParser.addNullHandler("Unterh");
      // pavkovic 2002.05.10: recruits (and old recruits are used from cr)
      regionParser.addBeanHandler("Rekruten", "recruits", Type.INTEGER);

      regionParser.addBeanHandler("letzterekruten", "oldRecruits", Type.INTEGER);
      regionParser.addNullHandler("maxLuxus");
      regionParser.addBeanHandler("letzteluxus", "oldLuxuries", Type.INTEGER);
      regionParser.addBeanHandler("Mallorn", "mallorn", Type.ZERO_ONE);
      regionParser.addTagHandler("herb", new RegionTagHandler(regionParser) {
        @Override
        public void handle(Region region) throws IOException {
          final ItemType type = world.getRules().getItemType(StringID.create(sc.argv[0]), true);
          region.setHerb(type);
        }
      });
      regionParser.addBeanHandler("herbamount", "herbAmount", Type.STRING);

      regionParser.addNullHandler("Runde");

      regionParser.addBeanHandler("Verorkt", "orcInfested", Type.ZERO_ONE);
      regionParser.addBeanHandler("Schoesslinge", "sprouts", Type.INTEGER);

      regionParser.addBeanHandler("letzteSchoesslinge", "oldSprouts", Type.INTEGER);
      regionParser.addBeanHandler("Steine", "stones", Type.INTEGER);
      regionParser.addBeanHandler("Stein", "stones", Type.INTEGER);
      regionParser.addBeanHandler("letztesteine", "oldStones", Type.INTEGER);
      regionParser.addBeanHandler("visibility", "visibilityString", Type.STRING);
      // TODO Bookmarks

      regionParser.addBlockHandler("PREISE", new RegionTagHandler(regionParser) {
        @Override
        public void handle(Region region) throws IOException {
          region.setPrices(parsePrices(region.getPrices()));
        }
      });
      regionParser.addBlockHandler("LETZTEPREISE", new RegionTagHandler(regionParser) {
        @Override
        public void handle(Region region) throws IOException {
          region.setOldPrices(parsePrices(region.getOldPrices()));
        }
      });
      regionParser.addBlockHandler("GRENZE", new RegionTagHandler(regionParser) {
        @Override
        public void handle(Region region) throws IOException {
          parseBorders(region);
        }
      });
      regionParser.addBlockHandler("SIGN", new RegionTagHandler(regionParser) {
        @Override
        public void handle(Region region) throws IOException {
          parseSigns(region);
        }
      });
      regionParser.addBlockHandler("EINHEIT", new RegionTagHandler(regionParser) {
        @Override
        public void handle(Region region) throws IOException {
          parseState.unitSortIndex = parseUnitFast(region, ++parseState.unitSortIndex);
        }
      });
      regionParser.addBlockHandler("ALTEINHEIT", new RegionTagHandler(regionParser) {
        @Override
        public void handle(Region region) throws IOException {
          parseState.unitSortIndex =
              parseUnitFast(region, ++parseState.unitSortIndex, "ALTEINHEIT");
        }
      });
      regionParser.addBlockHandler("SCHIFF", new RegionTagHandler(regionParser) {
        @Override
        public void handle(Region region) throws IOException {
          parseShip(region, ++parseState.shipSortIndex);
        }
      });
      regionParser.addBlockHandler("BURG", new RegionTagHandler(regionParser) {
        @Override
        public void handle(Region region) throws IOException {
          parseBuilding(region, ++parseState.buildingSortIndex);
        }
      });
      regionParser.addBlockHandler("MESSAGE", new RegionTagHandler(regionParser) {
        @Override
        public void handle(Region region) throws IOException {
          region.setMessages(parseMessages(region.getMessages()));
        }
      });
      regionParser.addBlockHandler("REGIONSEREIGNISSE", new RegionTagHandler(regionParser) {
        @Override
        public void handle(Region region) throws IOException {
          region.setEvents(parseMessageSequence(region.getEvents()));

          /*
           * } else if(sc.argc == 1 && sc.argv[0].equals("REGIONSKOMMENTAR")) { region.comments =
           * parseMessageSequence(region.comments);
           */
        }
      });
      regionParser.addBlockHandler("REGIONSBOTSCHAFTEN", new RegionTagHandler(regionParser) {
        @Override
        public void handle(Region region) throws IOException {
          region.setPlayerMessages(parseMessageSequence(region.getPlayerMessages()));
        }
      });
      regionParser.addBlockHandler("UMGEBUNG", new RegionTagHandler(regionParser) {
        @Override
        public void handle(Region region) throws IOException {
          region.setSurroundings(parseMessageSequence(region.getSurroundings()));
        }
      });
      regionParser.addBlockHandler("DURCHREISE", new RegionTagHandler(regionParser) {
        @Override
        public void handle(Region region) throws IOException {
          region.setTravelThru(parseMessageSequence(region.getTravelThru()));
        }
      });
      regionParser.addBlockHandler("DURCHSCHIFFUNG", new RegionTagHandler(regionParser) {
        @Override
        public void handle(Region region) throws IOException {
          region.setTravelThruShips(parseMessageSequence(region.getTravelThruShips()));
        }
      });
      regionParser.addBlockHandler("EFFECTS", new RegionTagHandler(regionParser) {
        @Override
        public void handle(Region region) throws IOException {
          region.setEffects(parseStringSequence(region.getEffects()));
        }
      });
      regionParser.addBlockHandler("ATTRIBUTES", new RegionTagHandler(regionParser) {
        @Override
        public void handle(Region region) throws IOException {
          parseAttributes(region);
        }
      });
      regionParser.addBlockHandler("COMMENTS", new RegionTagHandler(regionParser) {
        @Override
        public void handle(Region region) throws IOException {
          region.setComments(parseStringSequence(region.getComments()));
        }
      });
      regionParser.addBlockHandler("RESOURCE", new RegionTagHandler(regionParser) {
        @Override
        public void handle(Region region) throws IOException {
          final RegionResource res = parseRegionResource(world.getRules(), region);

          if (res != null) {
            region.addResource(res);
          }
        }
      });
      regionParser.addBlockHandler("SCHEMEN", new RegionTagHandler(regionParser) {
        @Override
        public void handle(Region region) throws IOException {
          parseScheme(region);
        }
      });
      regionParser.addBlockHandler("GEGENSTAENDE", new RegionTagHandler(regionParser) {
        @Override
        public void handle(Region region) throws IOException {
          /* not used in standard Eressea */
          parseItems(region);
        }
      });
      // if (sc.isBlock && sc.argv[0].equals("MESSAGETYPES")) {
      // break;
      // } else if (sc.isBlock && sc.argv[0].startsWith("REGION ")) {
      // break;
      // } else if (sc.isBlock) {
      // break;
      // } else {
      // if (sc.argc == 2) {
      // region.putTag(sc.argv[1], sc.argv[0]);
      // }
      //
      // unknown("REGION", true);
      // }
    }

    long t = System.currentTimeMillis();
    RegionParseState parserState = new RegionParseState(newregion, world.base);

    regionParser.parse(this, parserState);
    tRParse += System.currentTimeMillis() - t;

    // validate region before add to world data
    if ((parserState.iValidateFlags & 1) == 0) {
      if (Region.VIS_STR_WRAP.equals(newregion.getVisibilityString())) {
        newregion.setType(RegionType.wrap);
      } else {
        CRParser.log.warn("Warning: No region type is given for region '" + newregion.toString()
            + "' - it is ignored.");
        newregion.setType(RegionType.unknown);
      }
      world.addRegion(newregion);
    } else {
      world.addRegion(newregion);
    }
  }

}
