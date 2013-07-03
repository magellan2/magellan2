/*
 * Copyright (C) 2000-2004 Roger Butenuth, Andreas Gampe, Stefan Goetz, Sebastian Pappert, Klaas
 * Prause, Enno Rehling, Sebastian Tusk, Ulrich Kuester, Ilja Pavkovic This file is part of the
 * Eressea Java Code Base, see the file LICENSING for the licensing information applying to this
 * file.
 */

package magellan.library.io.nr;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import magellan.library.Alliance;
import magellan.library.Battle;
import magellan.library.Building;
import magellan.library.CombatSpell;
import magellan.library.CoordinateID;
import magellan.library.EntityID;
import magellan.library.Faction;
import magellan.library.GameData;
import magellan.library.ID;
import magellan.library.IntegerID;
import magellan.library.Item;
import magellan.library.Message;
import magellan.library.MissingData;
import magellan.library.Region;
import magellan.library.Rules;
import magellan.library.Ship;
import magellan.library.Skill;
import magellan.library.Spell;
import magellan.library.StringID;
import magellan.library.Unit;
import magellan.library.UnitID;
import magellan.library.gamebinding.atlantis.AtlantisConstants;
import magellan.library.io.AbstractReportParser;
import magellan.library.io.GameDataIO;
import magellan.library.io.RulesIO;
import magellan.library.io.file.FileType;
import magellan.library.rules.ItemType;
import magellan.library.rules.MessageType;
import magellan.library.rules.RegionType;
import magellan.library.rules.ShipType;
import magellan.library.rules.SimpleDate;
import magellan.library.rules.SkillType;
import magellan.library.rules.UnitContainerType;
import magellan.library.utils.CollectionFactory;
import magellan.library.utils.MagellanFactory;
import magellan.library.utils.MemoryManagment;
import magellan.library.utils.NullUserInterface;
import magellan.library.utils.Resources;
import magellan.library.utils.UserInterface;
import magellan.library.utils.logging.Logger;
import magellan.library.utils.transformation.IdentityTransformer;
import magellan.library.utils.transformation.ReportTransformer;

/**
 * Parser for nr-files.
 */
public class NRParser extends AbstractReportParser implements RulesIO, GameDataIO {
  @SuppressWarnings("hiding")
  protected static final Logger log = Logger.getInstance(NRParser.class);

  protected interface SectionReader {

    boolean matches(String line);

    void parse() throws IOException, ParseException;

  }

  String coordinates;
  boolean umlauts;
  final Collection<String> warnedLines = new HashSet<String>();

  private static final String LINE_END = "([!?.])";
  protected static final String ID = "([0-9]+)";
  protected static final String ID2 = "\\(" + ID + "\\)";
  protected static final String NAME = "([^()]*)";
  protected static final String IDENTIFIER = "([^(),;.]*)";
  protected static final String word = "(\\w+)";
  protected static final String NUM = "(" + number + ")";
  protected static final String ANTLANTIS_LINE = "\\s*" + NAME + " Turn Report(.*)";
  protected static final String FACTION_LINE = "\\s*" + NAME + " " + ID2 + "\\s*";
  protected static final String DATE_LINE = "\\s*((In the Beginning)|(" + NAME + ", Year " + NUM
      + "))\\s*";
  protected static final String OBJECT = NAME + "\\s+" + ID2;
  protected static final String OBJECT2 = "\\s" + NAME + "\\s+" + ID2 + "\\s*";
  protected static final String MISTAKES_LINE = "\\s*Mistakes\\s*";
  protected static final String MESSAGE_LINE = "^(.*)$";
  protected static final String UNIT_MESSAGE_LINE = "^" + OBJECT + "\\s*(.*)$";
  protected static final String MESSAGES_LINE = "\\s*Messages\\s*";
  protected static final String EVENTS_LINE = "\\s*Events During Turn\\s*";
  protected static final String SECTION_LINE = "          \\s*([^\\s].*)\\s*";
  protected static final String STATUS_LINE = "\\s*Current Status\\s*";
  protected static final String ALLIED_LINE = "You are allied to\\s+" + OBJECT + "(," + OBJECT
      + ")*\\.";
  protected static final String COORD2 = "(\\((" + NUM + "),(" + NUM + ")\\))";
  protected static final String COORD3 = "(\\((" + NUM + "),(" + NUM + "),(" + NUM + ")\\))";
  protected static final String OCEAN = "ocean";
  protected static final String REGION_LINE = "^" + NAME + "?\\s+" + COORD2 + ",\\s+" + word
      + "(,\\s+exits:\\s+([^.]*))?.\\s*(peasants:\\s+(" + NUM + ")(,\\s+\\$(" + NUM + "))?.)?";
  protected static final String PEASANTS_LINE = "^peasants:\\s+" + NUM + "(,\\s+\\$" + NUM + ")\\.";

  protected static final String EXITS_PART = "exits:\\s+([^.]*)";
  protected static final String EXIT_PART = word;
  protected static final String PEASANTS_PART = "peasants:\\s+(" + NUM + ")";
  protected static final String PEASANT_PART = NUM;

  //
  protected static final String UNIT_LINE = "  *([*+-])\\s+" + OBJECT + "(,\\s+faction\\s+"
      + OBJECT + ")?(.*)" + LINE_END;

  protected static final String DEFAULT_PART = "default:\\s+\"([^\"]*)\"";
  protected static final String SKILLS_PART = "skills:\\s+" + NAME + "\\s+" + NUM + "\\s+\\[" + NUM
      + "\\]";
  protected static final String SKILL_PART = NAME + "\\s+" + NUM + "\\s+\\[" + NUM + "\\]";
  protected static final String ITEMS_PART = "has:\\s+" + NUM + "\\s+" + NAME;
  protected static final String ITEM_PART = NUM + "\\s+" + NAME;
  protected static final String SPELLS_PART = "spells:\\s+" + NAME;
  protected static final String SPELL_PART = NAME;
  protected static final String CSPELLS_PART = "combat spell:\\s+" + NAME;
  protected static final String CSPELL_PART = NAME;
  protected static final String NUMBER_PART = "number:\\s+" + NUM;
  protected static final String COMBAT_STATUS_PART = "behind";
  protected static final String GUARD_STATUS_PART = "on guard";
  protected static final String SILVER_PART = "\\$" + NUM;

  protected static final String BUILDING_LINE = "   +" + OBJECT + "(,\\s+size\\s+" + NUM + ")(.*)"
      + LINE_END;
  protected static final String SHIP_LINE = "   +" + OBJECT + "(,\\s+" + IDENTIFIER + ")(.*)"
      + LINE_END;

  public static final MessageType ERROR_TYPE = new MessageType(IntegerID.create(10000042), "Error");
  public static final MessageType MESSAGES_TYPE = new MessageType(IntegerID.create(10000043),
      "Message");
  public static final MessageType EVENTS_TYPE =
      new MessageType(IntegerID.create(10000044), "Event");
  public static final MessageType MISC_TYPE = new MessageType(IntegerID.create(10000045), "Misc");

  protected static Pattern idPattern = Pattern.compile(ID);
  protected static Pattern id2Pattern = Pattern.compile(ID2);
  protected static Pattern namePattern = Pattern.compile(NAME);
  protected static Pattern wordPattern = Pattern.compile(word);

  protected static Pattern eresseaPattern = Pattern.compile(" *Report f.r ([^,]+),(.*)");
  protected static Pattern atlantisLinePattern = Pattern.compile(ANTLANTIS_LINE);
  protected static Pattern factionLinePattern = Pattern.compile(FACTION_LINE);
  protected static Pattern dateLinePattern = Pattern.compile(DATE_LINE);
  protected static Pattern mistakesLinePattern = Pattern.compile(MISTAKES_LINE);
  protected static Pattern messageLinePattern = Pattern.compile(MESSAGE_LINE);
  protected static Pattern unitMessageLinePattern = Pattern.compile(UNIT_MESSAGE_LINE);
  protected static Pattern messagesLinePattern = Pattern.compile(MESSAGES_LINE);
  protected static Pattern objectPattern = Pattern.compile(OBJECT);
  protected static Pattern object2Pattern = Pattern.compile(OBJECT2);
  protected static Pattern eventsLinePattern = Pattern.compile(EVENTS_LINE);
  protected static Pattern sectionLinePattern = Pattern.compile(SECTION_LINE);
  protected static Pattern statusLinePattern = Pattern.compile(STATUS_LINE);
  protected static Pattern alliedLinePattern = Pattern.compile(ALLIED_LINE);
  protected static Pattern numPattern = Pattern.compile(NUM);
  protected static Pattern coord2Pattern = Pattern.compile(COORD2);
  protected static Pattern coord3Pattern = Pattern.compile(COORD3);
  protected static Pattern regionLinePattern = Pattern.compile(REGION_LINE);
  protected static Pattern peasantsLinePattern = Pattern.compile(PEASANTS_LINE);
  //
  protected static Pattern unitLinePattern = Pattern.compile(UNIT_LINE);
  protected static Pattern defaultPartPattern = Pattern.compile(DEFAULT_PART);
  protected static Pattern skillsPartPattern = Pattern.compile(SKILLS_PART);
  protected static Pattern skillPartPattern = Pattern.compile(SKILL_PART);
  protected static Pattern itemsPartPattern = Pattern.compile(ITEMS_PART);
  protected static Pattern itemPartPattern = Pattern.compile(ITEM_PART);
  protected static Pattern spellsPartPattern = Pattern.compile(SPELLS_PART);
  protected static Pattern spellPartPattern = Pattern.compile(SPELL_PART);
  protected static Pattern cspellsPartPattern = Pattern.compile(CSPELLS_PART);
  protected static Pattern cspellPartPattern = Pattern.compile(CSPELL_PART);
  protected static Pattern numberPartPattern = Pattern.compile(NUMBER_PART);
  protected static Pattern combatStatusPartPattern = Pattern.compile(COMBAT_STATUS_PART);
  protected static Pattern guardStatusPartPattern = Pattern.compile(GUARD_STATUS_PART);
  protected static Pattern silverPartPattern = Pattern.compile(SILVER_PART);

  protected static Pattern shipLinePattern = Pattern.compile(SHIP_LINE);
  protected static Pattern buildingLinePattern = Pattern.compile(BUILDING_LINE);

  protected static StringID oceanType = StringID.create(OCEAN);

  private List<SectionReader> sectionReaders = new ArrayList<NRParser.SectionReader>();

  private String line = "";

  private BufferedReader reader;

  protected Faction ownerFaction;

  private List<Message> reportMessages = new ArrayList<Message>();
  private List<String> reportErrors;
  private List<Battle> reportBattles;

  private Map<magellan.library.ID, Spell> spellMap = CollectionFactory
      .<ID, Spell> createSyncOrderedMap();
  private Map<ID, CombatSpell> combatSpells = CollectionFactory.createSyncOrderedMap();

  protected final int allianceState = 26;

  private String nextLine = null;

  private RegionReader regionReader;

  private int lnr = 0;

  protected UnitContainerType castleType;

  protected int unmatchedcounter = 0;

  private Exception firstError;
  private Exception lastError;
  protected int cSId = 1;

  /**
   * Creates a new parser.
   * 
   * @param ui The UserInterface for the progress. Can be NULL. Then no operation is displayed.
   */
  public NRParser(UserInterface ui) {
    this(ui, new IdentityTransformer());
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
  public NRParser(UserInterface ui, ReportTransformer translator) {
    if (ui == null) {
      this.ui = new NullUserInterface();
    } else {
      this.ui = ui;
    }
    transformer = translator;

    initParsers();
  }

  private void initParsers() {
    sectionReaders.add(new HeaderReader());
    sectionReaders.add(new MessageReader(mistakesLinePattern, reportMessages));
    sectionReaders.add(new MessageReader(messagesLinePattern, reportMessages));
    sectionReaders.add(new MessageReader(eventsLinePattern, reportMessages));
    sectionReaders.add(new StatusReader());
    regionReader = new RegionReader();
    // fall back
    sectionReaders.add(new MessageReader(sectionLinePattern, reportMessages));
  }

  /**
   * This function parses the informations found in Reader in and creates a corresponding GameData
   * object tree.
   * 
   * @param in Reader to nr file
   * @param data GameData to be filled with informations of given report file.
   * @throws IOException If an I/O error occurs
   * @see magellan.library.io.GameDataIO#read(java.io.Reader, magellan.library.GameData)
   */
  public synchronized GameData read(Reader in, GameData data) throws IOException {
    int regionSortIndex = 0;
    MemoryManagment.setFinalizerPriority(Thread.MAX_PRIORITY);
    boolean error = false;
    unmatchedcounter = 0;

    ui.setTitle(Resources.get("progressdialog.loadnr.title"));
    ui.setMaximum(10000);
    ui.setProgress(Resources.get("progressdialog.loadnr.start"), 1);
    ui.show();

    try {
      world = data;
      reader = new BufferedReader(in);
      castleType = world.getRules().getBuildingType(AtlantisConstants.B_BUILDING);

      nextLine();
      while (line != null && line.length() == 0) {
        nextLine();
      }
      try {
        while (line != null) {
          boolean matched = false;
          for (SectionReader r : sectionReaders) {
            if (r.matches(line)) {
              matched = true;
              try {
                r.parse();
                break;
              } catch (ParseException e) {
                error(e);
                if (line.length() == 0) {
                  break;
                }
              }
            }
          }
          if (!matched) {
            if (unmatchedcounter++ < 100) {
              log.warn("unmatched line: " + line);
            } else if (unmatchedcounter == 101) {
              log.warn("...more unmatched lines");
            }
            nextLine();
          }
          while (line != null && line.length() == 0) {
            nextLine();
          }
        }
      } catch (final OutOfMemoryError ome) {
        NRParser.log.error(ome);
        error = true;
        world.setOutOfMemory(true);
      }
      if (reportMessages != null && reportMessages.size() > 0) {
        ownerFaction.setMessages(reportMessages);
      }
      if (reportErrors != null && reportErrors.size() > 0) {
        ownerFaction.setErrors(reportErrors);
      }
      if (reportBattles != null && reportBattles.size() > 0) {
        ownerFaction.setBattles(reportBattles);
      }
      ERROR_TYPE.setSection("errors");
      MESSAGES_TYPE.setSection("message");
      MISC_TYPE.setSection("others");
      EVENTS_TYPE.setSection("events");
      data.addMsgType(ERROR_TYPE);
      data.addMsgType(MESSAGES_TYPE);
      data.addMsgType(MISC_TYPE);
      data.addMsgType(EVENTS_TYPE);

      setOwner(data);

      // Fiete 20061208 check Memory
      if (!MemoryManagment.isFreeMemory() || error) {
        // we have a problem..
        // like in startup of client..we reset the data
        world = new MissingData();
        // marking the problem

        // end exit
        return world;
      }

      world.setMaxSortIndex(++regionSortIndex);
    } finally {
      ui.ready();
    }

    NRParser.log.fine("Done reading.");

    return world;
  }

  protected final void nextLine() throws IOException {
    nextLine(false, true);
  }

  protected void nextLine(boolean join, boolean skipEmpty) throws IOException {
    if (line == null)
      return;

    lnr++;
    if (nextLine != null) {
      line = nextLine;
    } else {
      line = reader.readLine();
    }
    while (line != null && skipEmpty && line.length() == 0) {
      lnr++;
      line = reader.readLine();
    }
    nextLine = null;
    if (join && (skipEmpty || (line != null && line.length() > 0))) {
      String newLine = "";
      while (line != null && !line.matches(".*" + LINE_END) && newLine != null) {
        nextLine = null;
        newLine = reader.readLine();
        if (newLine != null) {
          if (newLine.length() > 0) {
            lnr++;
            if (line.endsWith("\\s")) {
              line += newLine.trim();
            } else {
              line = line + " " + newLine.trim();
            }
          } else {
            nextLine = "";
          }
        }
      }
    }
  }

  protected void continueLine() throws IOException {
    if (line == null)
      return;

    if (!line.matches(".*" + LINE_END)) {
      String oldLine = new String(line);
      nextLine(true, false);
      if (line == null) {
        line = oldLine;
      } else if (line.equals("")) {
        nextLine = "";
        line = oldLine;
      } else {
        line = oldLine + line;
      }
    }
  }

  private void setOwner(GameData newData) {
    if (newData.getOwnerFaction() == null) {
      // in standard reports, the first faction of the report should always be the owner faction
      final Faction firstFaction2 = getFirstFaction();
      if (newData.getOwnerFaction() == null && firstFaction2 != null) {
        newData.setOwnerFaction(firstFaction2.getID());
        NRParser.log.info("setOwner of Report to: " + firstFaction2.toString());
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
        NRParser.log.info("Layers updated with translation 0,0 for " + firstFaction2.toString());
      }
    }
  }

  public Rules readRules(FileType filetype) throws IOException {
    log.error("something went wrong, there are no NR rules");
    return null;
  }

  protected void error(ParseException e) {
    errors++;
    log.warn("unexpected line: ", e);
    if (firstError != null) {
      firstError = e;
    }
    lastError = e;
  }

  protected static class ParseException extends Exception {

    private Pattern pattern;
    private String line;
    private String message;

    public ParseException(String message, String line) {
      this.message = message + "\n" + line;
    }

    public ParseException(Pattern pattern, String line) {
      this.pattern = pattern;
      this.line = line;
    }

    public ParseException(String message) {
      this.message = message;
    }

    @Override
    public String getMessage() {
      if (message != null)
        return message;
      else
        return "expected pattern " + pattern + " but found line: " + line;
    }

    public String getLine() {
      return line;
    }

  }

  protected class AbstractReader {
    protected Matcher lineMatcher, partMatcher;
    protected boolean matched;

    protected Pattern startPattern;
    protected Pattern partPattern;

    AbstractReader(Pattern pattern) {
      startPattern = pattern;
    }

    public boolean matches(String aline) {
      return startPattern.matcher(aline).matches();
    }

    protected boolean expectLine(Pattern pattern, boolean exception) throws ParseException {
      if (line == null)
        if (exception)
          throw new ParseException(pattern, line);
        else
          return false;
      lineMatcher = pattern.matcher(line);
      matched = lineMatcher.matches();
      if (!matched && exception)
        throw new ParseException(pattern, line);
      return matched;
    }

    protected boolean expectLine(Pattern pattern) throws ParseException {
      return expectLine(pattern, true);
    }

    protected boolean matches(Pattern pattern, String part) {
      if (part == null)
        return false;
      partMatcher = pattern.matcher(part);
      boolean val = partMatcher.matches();
      if (val) {
        partPattern = pattern;
      }
      return val;
    }
  }

  class HeaderReader extends AbstractReader implements SectionReader {

    HeaderReader() {
      super(atlantisLinePattern);
    }

    public void parse() throws IOException, ParseException {
      expectLine(atlantisLinePattern);
      world.setGameName(lineMatcher.group(1));
      world.base = 10;
      nextLine(false, false);

      expectLine(factionLinePattern);
      EntityID factionId;
      world.setOwnerFaction(factionId = EntityID.createEntityID(lineMatcher.group(2), world.base));
      ownerFaction = getAddFaction(factionId);
      ownerFaction.setName(lineMatcher.group(1));
      nextLine(false, false);

      expectLine(dateLinePattern);
      if (lineMatcher.group(2) != null && lineMatcher.group(2).length() > 0) {
        world.setDate(new SimpleDate(null, "0"));
      } else {
        world.setDate(new SimpleDate(lineMatcher.group(4), lineMatcher.group(5)));
      }
      ui.setProgress(Resources.get("progressdialog.loadnr.game", world.getDate()), 1);
      nextLine();
    }
  }

  protected class MessageReader extends AbstractReader implements SectionReader {

    private List<Message> messages;
    private MessageType fallBackType = MessageType.NO_TYPE;

    MessageReader(Pattern header, List<Message> messages) {
      super(header);
      this.messages = messages;
      if (header == mistakesLinePattern) {
        fallBackType = ERROR_TYPE;
      } else if (header == messagesLinePattern) {
        fallBackType = MESSAGES_TYPE;
      } else if (header == eventsLinePattern) {
        fallBackType = EVENTS_TYPE;
      } else if (header == sectionLinePattern) {
        fallBackType = MISC_TYPE;
      }
    }

    public void parse() throws IOException, ParseException {
      expectLine(startPattern);
      ui.setProgress(Resources.get("progressdialog.loadnr.messages"), 1);
      nextLine(true, true);

      do {
        while (line != null && line.length() > 0) {
          Message message;
          messages.add(message = MagellanFactory.createMessage(line));
          Matcher objectMatcher = unitMessageLinePattern.matcher(line);
          if (objectMatcher.matches()) {
            Map<String, String> attributes = new HashMap<String, String>();
            attributes.put("unit", objectMatcher.group(2));
            message.setAttributes(attributes);
          }
          message.setType(fallBackType);
          nextLine(true, false);
        }
        nextLine(false, true);
        if (line != null) {
          for (SectionReader r : sectionReaders) {
            if (r.matches(line))
              return;
          }
          continueLine();
        }
      } while (line != null);
    }
  }

  protected class StatusReader extends AbstractReader implements SectionReader {

    StatusReader() {
      super(statusLinePattern);
    }

    public void parse() throws IOException, ParseException {
      expectLine(startPattern);
      nextLine(true, true);

      if (expectLine(alliedLinePattern, false)) {
        Map<EntityID, Alliance> allies =
            CollectionFactory.<EntityID, Alliance> createSyncOrderedMap();
        addAlly(allies, lineMatcher.group(2), lineMatcher.group(1));

        if (lineMatcher.group(3) != null && lineMatcher.group(3).length() > 0) {
          StringTokenizer tokenizer = new StringTokenizer(lineMatcher.group(3), ",");
          while (tokenizer.hasMoreTokens()) {
            String part = tokenizer.nextToken();
            Matcher m = object2Pattern.matcher(part);
            if (m.matches()) {
              addAlly(allies, m.group(2), m.group(1));
            }
          }
        }
        ownerFaction.setAllies(allies);
        nextLine(true, true);
      }

      while (expectLine(regionLinePattern, false)) {
        regionReader.parse();
      }
    }

    private void addAlly(Map<EntityID, Alliance> allies, String aid, String name) {
      Faction ally = getAddFaction(EntityID.createEntityID(aid, world.base));
      if (ally.getName() != null) {
        ally.setName(name);
      }
      allies.put(ally.getID(), new Alliance(ally, allianceState));
    }
  }

  /**
   * Translates c by newOrigin if it's in the same z-level and returns it.
   */
  @Override
  protected CoordinateID originTranslate(CoordinateID c) {
    return transformer.transform(c);// .translate(CoordinateID.create(c.getY() * -1, 0));
  }

  protected class RegionReader extends AbstractReader implements SectionReader {

    private Region currentRegion;
    private Unit currentUnit;
    private Ship currentShip;
    private Building currentBuilding;

    RegionReader() {
      super(regionLinePattern);
    }

    public void parse() throws IOException, ParseException {
      try {
        if (expectLine(regionLinePattern)) {
          // "^" + name + "?\\s+" + coord2 + ", " + word +
          // "(, exits: ([^.]*))?.\\s*(peasants: ("+num+")(, $("+num+"))?.)?";
          CoordinateID c =
              CoordinateID.parse(lineMatcher.group(2).substring(1,
                  lineMatcher.group(2).length() - 1), ",");

          if (c == null)
            throw new ParseException("no region coordinate", line);

          c = originTranslate(c);

          currentRegion = world.getRegion(c);

          if (currentRegion == null) {
            currentRegion = createRegion(c, world);
          }
          if (lineMatcher.group(1) != null) {
            currentRegion.setName(lineMatcher.group(1));
          }
          ui.setProgress(Resources.get("progressdialog.loadnr.region", currentRegion), 1);

          try {
            final RegionType type =
                world.getRules().getRegionType(StringID.create(lineMatcher.group(7)), true);
            currentRegion.setType(type);
          } catch (final IllegalArgumentException e) {
            currentRegion.setType(RegionType.unknown);
            // can happen in StringID constructor if sc.argv[0] == ""
            log.warn("CRParser.parseRegion(): found region without a valid region type in line "
                + lnr);
          }

          parseExits(currentRegion, lineMatcher.group(9));
          if (lineMatcher.group(12) != null) {
            currentRegion.setPeasants(Integer.parseInt(lineMatcher.group(12)));
          }
          if (lineMatcher.group(14) != null) {
            currentRegion.setSilver(Integer.parseInt(lineMatcher.group(14)));
          }

          nextLine(true, false);
          if (expectLine(peasantsLinePattern, false)) {
            // "^peasants: " + num + "(, \\$" + num + ")\\.";
            if (lineMatcher.group(1) != null) {
              currentRegion.setPeasants(Integer.parseInt(lineMatcher.group(12)));
            }
            if (lineMatcher.group(2) != null) {
              currentRegion.setSilver(Integer.parseInt(lineMatcher.group(14)));
            }
            nextLine(true, false);
          }
          if (world.getRegion(currentRegion.getCoordinate()) == null) {
            world.addRegion(currentRegion);
          }

          while (line != null) {
            if (line.length() == 0) {
              currentBuilding = null;
              currentShip = null;
              nextLine(true, false);
              if (expectLine(regionLinePattern, false)) {
                break;
              }
            } else if (expectLine(unitLinePattern, false)) {
              parseUnit();
            } else if (expectLine(buildingLinePattern, false)) {
              parseBuilding();
            } else if (expectLine(shipLinePattern, false)) {
              parseShip();
            } else {
              if (unmatchedcounter++ < 100) {
                log.warn("unmatched line: " + line);
              }
              currentBuilding = null;
              currentShip = null;
              nextLine(true, false);
            }
          }
          if (line != null && line.length() == 0) {
            nextLine(true, true);
          }
        }
      } finally {
        currentRegion = null;
      }
    }

    protected void parseShip() throws IOException {
      // shipLine = "    " + object + "(,\\s+" + identifier + ")(.*)\\.";
      currentShip =
          MagellanFactory.createShip(EntityID.createEntityID(lineMatcher.group(2), world.base),
              world);
      world.addShip(currentShip);
      currentShip.setRegion(currentRegion);
      currentShip.setName(lineMatcher.group(1));
      if (lineMatcher.group(3) != null) {
        if (world.getRules().getShipType(lineMatcher.group(4)) == null) {
          log.warn("unknown ship type " + lineMatcher.group(4));
        }
        ShipType type = world.getRules().getShipType(lineMatcher.group(4), true);
        currentShip.setType(type);
      }

      int semi = lineMatcher.group(5).indexOf(";");
      if (semi >= 0) {
        currentShip.setDescription(lineMatcher.group(5).substring(semi + 2));
      }

      currentBuilding = null;
      nextLine(true, false);
    }

    protected void parseBuilding() throws IOException {
      // buildingLine = "   " + object + "(,\\s+size\\s+" + num + ")?(.*)\\.";
      currentBuilding =
          MagellanFactory.createBuilding(EntityID.createEntityID(lineMatcher.group(2), world.base),
              world);
      world.addBuilding(currentBuilding);
      currentBuilding.setRegion(currentRegion);
      currentBuilding.setName(lineMatcher.group(1));
      currentBuilding.setType(castleType);
      if (lineMatcher.group(3) != null) {
        currentBuilding.setSize(Integer.parseInt(lineMatcher.group(4)));
      }

      int semi = lineMatcher.group(5).indexOf(";");
      if (semi >= 0) {
        currentBuilding.setDescription(lineMatcher.group(5).substring(semi + 2));
      }

      currentShip = null;
      nextLine(true, false);
    }

    private void parseUnit() throws ParseException, IOException {
      // UNIT_LINE = "  *([*+-])\\s+" + OBJECT + "(,\\s+faction\\s+" + OBJECT + ")?([^.]*)([.!?])";

      try {
        currentUnit = getAddUnit(UnitID.createUnitID(lineMatcher.group(3), world.base), false);
        currentUnit.setName(lineMatcher.group(2));

        Faction faction = null;
        if (lineMatcher.group(4) != null) {
          faction = getAddFaction(EntityID.createEntityID(lineMatcher.group(6), world.base));
          if (faction.getName() == null) {
            faction.setName(lineMatcher.group(5));
          }
        } else {
          faction = getAddFaction(EntityID.createEntityID(-1, world.base));
          faction.setName(Resources.get("crparser.nofaction"));
          currentUnit.setHideFaction(true);
        }
        currentUnit.setFaction(faction);

        // if (humans=null)
        // humans=world.getRules().getRaces().iterator().next();
        // unit.setRace(humans);
        currentUnit.setRace(world.getRules().getRaces().iterator().next());
        currentUnit.setRegion(currentRegion);

        if (currentBuilding != null) {
          currentUnit.setBuilding(currentBuilding);
          if (currentBuilding.getOwnerUnit() == null) {
            currentBuilding.setOwner(currentUnit);
          }
        }
        if (currentShip != null) {
          currentUnit.setShip(currentShip);
          if (currentShip.getOwnerUnit() == null) {
            currentShip.setOwner(currentUnit);
          }
        }

        int semi = lineMatcher.group(7).indexOf(";");
        String predesc;
        if (semi >= 0) {
          predesc = lineMatcher.group(7).substring(0, semi);
          String desc = lineMatcher.group(7).substring(semi + 2);
          if (!desc.endsWith(".")) {
            desc += lineMatcher.group(8);
          }
          currentUnit.setDescription(desc);
        } else {
          predesc = lineMatcher.group(7);
        }
        StringTokenizer tokenizer = new StringTokenizer(predesc, ",");
        while (tokenizer.hasMoreTokens()) {
          // defaultPart = "default: \"([^\"]*)\"";
          // skillsPart = "skills: " + name + " " + num +"\\[" + num + "\\]";
          // skillPart = name + " " + num +"\\[" + num + "\\]";
          // itemsPart = "has: " + num + " " + name;
          // itemPart = num + " " + name;
          // numberPart = "number: " + num;
          String part = tokenizer.nextToken().trim();
          // multi starting parts:
          if (matches(defaultPartPattern, part)) {
            currentUnit.addOrder(partMatcher.group(1));
            if (currentUnit.getCombatStatus() < 0) {
              currentUnit.setCombatStatus(AtlantisConstants.CS_FRONT);
            }
          } else if (matches(skillsPartPattern, part)) {
            addSkill(currentUnit, partMatcher.group(1), Integer.parseInt(partMatcher.group(2)),
                Integer.parseInt(partMatcher.group(3)));
          } else if (matches(itemsPartPattern, part)) {
            addItem(currentUnit, partMatcher.group(2), Integer.parseInt(partMatcher.group(1)));
          } else if (matches(spellsPartPattern, part)) {
            addSpell(currentUnit, partMatcher.group(1), false);
          } else if (matches(cspellsPartPattern, part)) {
            addSpell(currentUnit, partMatcher.group(1), true);
            // single parts:
          } else if (matches(numberPartPattern, part)) {
            currentUnit.setPersons(Integer.parseInt(partMatcher.group(1)));
          } else if (matches(combatStatusPartPattern, part)) {
            currentUnit.setCombatStatus(AtlantisConstants.CS_REAR);
          } else if (matches(guardStatusPartPattern, part)) {
            currentUnit.setGuard(1);
            currentRegion.addGuard(currentUnit);
          } else if (matches(silverPartPattern, part)) {
            addItem(currentUnit, AtlantisConstants.I_USILVER.toString(), Integer
                .parseInt(partMatcher.group(1)));
            // sub parts:
          } else if ((partPattern == itemsPartPattern || partPattern == itemPartPattern)
              && matches(itemPartPattern, part)) {
            addItem(currentUnit, partMatcher.group(2), Integer.parseInt(partMatcher.group(1)));
          } else if ((partPattern == skillsPartPattern || partPattern == skillPartPattern)
              && matches(skillPartPattern, part)) {
            addSkill(currentUnit, partMatcher.group(1), Integer.parseInt(partMatcher.group(2)),
                Integer.parseInt(partMatcher.group(3)));
          } else if ((partPattern == spellsPartPattern || partPattern == spellPartPattern)
              && matches(spellPartPattern, part)) {
            addSpell(currentUnit, partMatcher.group(1), false);
          } else if ((partPattern == cspellsPartPattern || partPattern == cspellPartPattern)
              && matches(cspellPartPattern, part)) {
            addSpell(currentUnit, partMatcher.group(1), true);
          } else {
            if (unmatchedcounter++ < 100) {
              log.warn("unmatched part: " + part);
            }
          }
        }

        partMatcher = null;
        nextLine(true, false);
      } finally {
        if (!spellMap.isEmpty()) {
          if (currentUnit == null)
            throw new ParseException("spell without unit.");
          else {
            currentUnit.setSpells(spellMap);
          }
          if (!combatSpells.isEmpty()) {
            currentUnit.setCombatSpells(combatSpells);
          }
        }
        spellMap.clear();
        currentUnit = null;
      }
    }

    protected void addSpell(Unit unit, String name, boolean combat) {
      final StringID id = StringID.create(name);
      Spell spell = world.getSpell(id);

      if (spell == null) {
        spell = MagellanFactory.createSpell(id, world);
        spell.setName(name);
        world.addSpell(spell);
      }

      spellMap.put(spell.getID(), spell);
      if (combat) {

        CombatSpell cs = MagellanFactory.createCombatSpell(IntegerID.create(cSId++));
        cs.setSpell(spell);
        cs.setUnit(currentUnit);
        combatSpells.put(cs.getID(), cs);
      }
    }

    protected void addItem(Unit unit, String item, int amount) {
      if (world.getRules().getItemType(item) == null) {
        log.warn("unknown item " + item);
      }
      ItemType itemType = world.getRules().getItemType(item, true);
      unit.addItem(new Item(itemType, amount));
    }

    protected void addSkill(Unit unit, String skillName, int level, int days) {
      if (world.getRules().getSkillType(skillName) == null) {
        log.warn("unknown skill " + skillName);
      }
      SkillType skillType = world.getRules().getSkillType(skillName, true);
      Skill skill;
      unit.addSkill(skill = new Skill(skillType, days, level, unit.getPersons(), false));
      skill.setChangeLevel(0);
    }

    private void parseExits(Region region, String group) throws ParseException {
      if (group != null) {
        Set<String> trans = new HashSet<String>(translateMap.keySet());
        StringTokenizer tokenizer = new StringTokenizer(group, ",");
        while (tokenizer.hasMoreTokens()) {
          String part = tokenizer.nextToken().trim();
          trans.remove(part);
          CoordinateID c2 = translate(region.getCoordinate(), part);
          if (c2 == null)
            throw new ParseException("invalid coordinate in " + group);
          Region r2 = world.getRegion(c2);
          if (r2 == null) {
            r2 = createRegion(c2, world);
            r2.setType(RegionType.unknown);
            world.addRegion(r2);
          }
        }
        for (String dir : trans) {
          CoordinateID c2 = region.getCoordinate().translate(translateMap.get(dir));
          if (world.getRegion(c2) == null) {
            Region r2 = createRegion(c2, world);
            r2.setType(RegionType.unknown);
            world.addRegion(r2);
            final RegionType type = world.getRules().getRegionType(oceanType, true);
            r2.setType(type);
          }
        }
      }
    }
  }

  static Map<String, CoordinateID> translateMap = new HashMap<String, CoordinateID>();

  static {
    translateMap.put("south", CoordinateID.create(0, 1));
    translateMap.put("ydd", CoordinateID.create(1, 1));
    translateMap.put("east", CoordinateID.create(1, 0));
    translateMap.put("north", CoordinateID.create(0, -1));
    translateMap.put("mir", CoordinateID.create(-1, -1));
    translateMap.put("west", CoordinateID.create(-1, 0));
  }

  protected CoordinateID translate(CoordinateID coordinate, String string) {
    CoordinateID trans = translateMap.get(string);
    if (trans == null)
      return null;
    return coordinate.translate(trans);
  }

  public Region createRegion(CoordinateID location, GameData data) {
    Region region = MagellanFactory.createRegion(location, data);
    region.setUID(getUid(location));
    return region;
  }

  private long getUid(CoordinateID location) {
    long val = ((long) location.getX() + (2 << 15)) << 31 | ((long) location.getY() + (2 << 15));
    return val;
  }

}
