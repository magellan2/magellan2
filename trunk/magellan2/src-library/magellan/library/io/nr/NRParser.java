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
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import magellan.library.Alliance;
import magellan.library.Building;
import magellan.library.CoordinateID;
import magellan.library.EntityID;
import magellan.library.Faction;
import magellan.library.GameData;
import magellan.library.Item;
import magellan.library.Message;
import magellan.library.MissingData;
import magellan.library.Region;
import magellan.library.Rules;
import magellan.library.Ship;
import magellan.library.Skill;
import magellan.library.StringID;
import magellan.library.Unit;
import magellan.library.UnitID;
import magellan.library.gamebinding.AtlantisConstants;
import magellan.library.io.AbstractReportParser;
import magellan.library.io.GameDataIO;
import magellan.library.io.RulesIO;
import magellan.library.io.file.FileType;
import magellan.library.rules.ItemType;
import magellan.library.rules.MessageType;
import magellan.library.rules.RegionType;
import magellan.library.rules.ShipType;
import magellan.library.rules.SkillType;
import magellan.library.rules.UnitContainerType;
import magellan.library.utils.CollectionFactory;
import magellan.library.utils.Direction;
import magellan.library.utils.MagellanFactory;
import magellan.library.utils.MemoryManagment;
import magellan.library.utils.NullUserInterface;
import magellan.library.utils.Resources;
import magellan.library.utils.UserInterface;
import magellan.library.utils.logging.Logger;
import magellan.library.utils.transformation.IdentityTransformer;
import magellan.library.utils.transformation.ReportTransformer;
import magellan.rules.SimpleDate;

/**
 * Parser for nr-files.
 */
public class NRParser extends AbstractReportParser implements RulesIO, GameDataIO {

  public interface SectionReader {

    boolean matches(String line);

    void parse() throws IOException, ParseException;

  }

  protected static final Logger log = Logger.getInstance(NRParser.class);

  String coordinates;
  boolean umlauts;
  final Collection<String> warnedLines = new HashSet<String>();

  protected static String id = "([0-9]+)";
  protected static String id2 = "\\(" + id + "\\)";
  protected static String name = "([^()]*)";
  protected static String identifier = "([^(),;.]*)";
  protected static String word = "(\\w+)";
  protected static String num = "(" + number + ")";
  protected static String atlantisLine = "\\s*" + name + " Turn Report(.*)";
  protected static String factionLine = "\\s*" + name + " " + id2 + "\\s*";
  protected static String dateLine = "\\s*((In the Beginning)|(" + name + ", Year " + num
      + "))\\s*";
  protected static String mistakesLine = "\\s*Mistakes\\s*";
  protected static String messageLine = "^(.*)$";
  protected static String messagesLine = "\\s*Messages\\s*";
  protected static String object = name + "\\s+" + id2;
  protected static String object2 = "\\s" + name + "\\s+" + id2 + "\\s*";
  protected static String eventsLine = "\\s*Events During Turn\\s*";
  protected static String statusLine = "\\s*Current Status\\s*";
  protected static String alliedLine = "You are allied to\\s+" + object + "(," + object + ")*\\.";
  protected static String coord2 = "(\\((" + num + "),(" + num + ")\\))";
  protected static String coord3 = "(\\((" + num + "),(" + num + "),(" + num + ")\\))";
  protected static String ocean = "ocean";
  protected static String regionLine = "^" + name + "?\\s+" + coord2 + ",\\s+" + word
      + "(,\\s+exits:\\s+([^.]*))?.\\s*(peasants:\\s+(" + num + ")(,\\s+\\$(" + num + "))?.)?";
  protected static String peasantsLine = "^peasants:\\s+" + num + "(,\\s+\\$" + num + ")\\.";

  protected static String exitsPart = "exits:\\s+([^.]*)";
  protected static String exitPart = word;
  protected static String peasantsPart = "peasants:\\s+(" + num + ")";
  protected static String peasantPart = num;

  //
  protected static String unitLine = "  *([*+-])\\s+" + object + "(,\\s+faction\\s+" + object
      + ")?([^.]*)?\\.";

  protected static String defaultPart = "default:\\s+\"([^\"]*)\"";
  protected static String skillsPart = "skills:\\s+" + name + "\\s+" + num + "\\s+\\[" + num
      + "\\]";
  protected static String skillPart = name + "\\s+" + num + "\\s+\\[" + num + "\\]";
  protected static String itemsPart = "has:\\s+" + num + "\\s+" + name;
  protected static String itemPart = num + "\\s+" + name;
  protected static String numberPart = "number:\\s+" + num;
  protected static String combatStatusPart = "behind";
  protected static String silverPart = "\\$" + num;

  protected static String buildingLine = "   +" + object + "(,\\s+size\\s+" + num + ")(.*)\\.";
  protected static String shipLine = "   +" + object + "(,\\s+" + identifier + ")(.*)\\.";

  protected static Pattern idPattern = Pattern.compile(id);
  protected static Pattern id2Pattern = Pattern.compile(id2);
  protected static Pattern namePattern = Pattern.compile(name);
  protected static Pattern wordPattern = Pattern.compile(word);

  protected static Pattern eresseaPattern = Pattern.compile(" *Report f.r ([^,]+),(.*)");
  protected static Pattern atlantisLinePattern = Pattern.compile(atlantisLine);
  protected static Pattern factionLinePattern = Pattern.compile(factionLine);
  protected static Pattern dateLinePattern = Pattern.compile(dateLine);
  protected static Pattern mistakesLinePattern = Pattern.compile(mistakesLine);
  protected static Pattern messageLinePattern = Pattern.compile(messageLine);
  protected static Pattern messagesLinePattern = Pattern.compile(messagesLine);
  protected static Pattern objectPattern = Pattern.compile(object);
  protected static Pattern object2Pattern = Pattern.compile(object2);
  protected static Pattern eventsLinePattern = Pattern.compile(eventsLine);
  protected static Pattern statusLinePattern = Pattern.compile(statusLine);
  protected static Pattern alliedLinePattern = Pattern.compile(alliedLine);
  protected static Pattern numPattern = Pattern.compile(num);
  protected static Pattern coord2Pattern = Pattern.compile(coord2);
  protected static Pattern coord3Pattern = Pattern.compile(coord3);
  protected static Pattern regionLinePattern = Pattern.compile(regionLine);
  protected static Pattern peasantsLinePattern = Pattern.compile(peasantsLine);
  //
  protected static Pattern unitLinePattern = Pattern.compile(unitLine);
  protected static Pattern defaultPartPattern = Pattern.compile(defaultPart);
  protected static Pattern skillsPartPattern = Pattern.compile(skillsPart);
  protected static Pattern skillPartPattern = Pattern.compile(skillPart);
  protected static Pattern itemsPartPattern = Pattern.compile(itemsPart);
  protected static Pattern itemPartPattern = Pattern.compile(itemPart);
  protected static Pattern numberPartPattern = Pattern.compile(numberPart);
  protected static Pattern combatStatusPartPattern = Pattern.compile(combatStatusPart);
  protected static Pattern silverPartPattern = Pattern.compile(silverPart);

  protected static Pattern shipLinePattern = Pattern.compile(shipLine);
  protected static Pattern buildingLinePattern = Pattern.compile(buildingLine);

  protected static StringID oceanType = StringID.create(ocean);

  private List<SectionReader> sectionReaders = new ArrayList<NRParser.SectionReader>();

  private String line;

  private BufferedReader reader;

  public Faction ownerFaction;

  private List<Message> messages = new ArrayList<Message>();

  public int allianceState = 26;

  private String nextLine = null;

  private RegionReader regionReader;

  public int lnr = 0;

  public String directions[] = { "south", "ydd", "east", "north", "mir", "west" };

  public UnitContainerType castleType;

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
    sectionReaders.add(new MessageReader(mistakesLinePattern, messages));
    sectionReaders.add(new MessageReader(messagesLinePattern, messages));
    sectionReaders.add(new MessageReader(eventsLinePattern, messages));
    sectionReaders.add(new StatusReader());
    regionReader = new RegionReader();
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
    boolean oome = false;

    ui.setTitle(Resources.get("progressdialog.loadnr.title"));
    ui.setMaximum(10000);
    ui.setProgress(Resources.get("progressdialog.loadnr.step01"), 1);
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
              ui.setProgress(Resources.get("progressdialog.loadcr.step02"), 2);
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
            log.warn("unmatched line: " + line);
            nextLine();
          }
          while (line != null && line.length() == 0) {
            nextLine();
          }
        }
      } catch (final OutOfMemoryError ome) {
        NRParser.log.error(ome);
        oome = true;
      }
      if (messages != null && messages.size() > 0) {
        ownerFaction.setMessages(messages);
      }

      setOwner(data);

      // Fiete 20061208 check Memory
      if (!MemoryManagment.isFreeMemory() || oome) {
        // we have a problem..
        // like in startup of client..we reset the data
        world = new MissingData();
        // marking the problem
        world.setOutOfMemory(true);

        ui.ready();
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
      while (line != null && !line.endsWith(".") && newLine != null) {
        nextLine = null;
        newLine = reader.readLine();
        if (newLine != null) {
          if (newLine.length() > 0) {
            lnr++;
            if (line.endsWith("\\s") || newLine.startsWith("\\s")) {
              line += newLine;
            } else {
              line = line + " " + newLine;
            }
          } else {
            nextLine = "";
          }
        }
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

  public static class ParseException extends Exception {

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

    public boolean matches(String line) {
      return startPattern.matcher(line).matches();
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
      partPattern = val ? pattern : null;
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
      nextLine();
    }
  }

  public class MessageReader extends AbstractReader implements SectionReader {

    private List<Message> messages;

    MessageReader(Pattern header, List<Message> messages) {
      super(header);
      this.messages = messages;
    }

    public void parse() throws IOException, ParseException {
      expectLine(startPattern);
      nextLine(true, true);

      while (line != null && line.length() > 0) {
        Message message;
        messages.add(message = MagellanFactory.createMessage(line));
        message.setType(MessageType.NO_TYPE);
        nextLine(true, false);
      }
    }
  }

  public class StatusReader extends AbstractReader implements SectionReader {

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

    private void addAlly(Map<EntityID, Alliance> allies, String id, String name) {
      Faction ally = getAddFaction(EntityID.createEntityID(id, world.base));
      if (ally.getName() != null) {
        ally.setName(name);
      }
      allies.put(ally.getID(), new Alliance(ally, allianceState));
    }
  }

  public class RegionReader extends AbstractReader implements SectionReader {

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
            currentRegion = MagellanFactory.createRegion(c, world);
          }
          if (lineMatcher.group(1) != null) {
            currentRegion.setName(lineMatcher.group(1));
          }

          try {
            final RegionType type =
                world.rules.getRegionType(StringID.create(lineMatcher.group(7)), true);
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
              log.warn("unmatched line: " + line);
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
      String predesc;
      if (semi >= 0) {
        predesc = lineMatcher.group(5).substring(0, semi);
        currentShip.setDescription(lineMatcher.group(5).substring(semi + 2));
      } else {
        predesc = lineMatcher.group(5);
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
      String predesc;
      if (semi >= 0) {
        predesc = lineMatcher.group(5).substring(0, semi);
        currentBuilding.setDescription(lineMatcher.group(5).substring(semi + 2));
      } else {
        predesc = lineMatcher.group(5);
      }

      currentShip = null;
      nextLine(true, false);
    }

    private void parseUnit() throws ParseException, IOException {
      // unitLine = "  ([*-+]) " + object + "(, faction " + object + ")?([^.]*)?\\.";
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
          currentUnit.setDescription(lineMatcher.group(7).substring(semi + 2));
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
          if (matches(defaultPartPattern, part)) {
            currentUnit.addOrder(partMatcher.group(1));
            if (currentUnit.getCombatStatus() == -1) {
              currentUnit.setCombatStatus(AtlantisConstants.CS_FRONT);
            }
          } else if (matches(skillsPartPattern, part)) {
            addSkill(currentUnit, partMatcher.group(1), Integer.parseInt(partMatcher.group(2)),
                Integer.parseInt(partMatcher.group(3)));
          } else if (matches(skillPartPattern, part)) {
            if (partPattern == skillsPartPattern || partPattern == skillPartPattern) {
              addSkill(currentUnit, partMatcher.group(1), Integer.parseInt(partMatcher.group(2)),
                  Integer.parseInt(partMatcher.group(3)));
            }
          } else if (matches(itemsPartPattern, part)) {
            addItem(currentUnit, partMatcher.group(2), Integer.parseInt(partMatcher.group(1)));
          } else if (matches(itemPartPattern, part)) {
            if (partPattern == itemsPartPattern || partPattern == itemPartPattern) {
              addItem(currentUnit, partMatcher.group(2), Integer.parseInt(partMatcher.group(1)));
            }
          } else if (matches(numberPartPattern, part)) {
            currentUnit.setPersons(Integer.parseInt(partMatcher.group(1)));
          } else if (matches(combatStatusPartPattern, part)) {
            currentUnit.setCombatStatus(AtlantisConstants.CS_REAR);
          } else if (matches(silverPartPattern, part)) {
            addItem(currentUnit, "silver", Integer.parseInt(partMatcher.group(1)));
          } else {
            log.warn("unmatched part: " + part);
          }
        }

        partMatcher = null;
        nextLine(true, false);
      } finally {
        currentUnit = null;
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
        StringTokenizer tokenizer = new StringTokenizer(group, ",");
        while (tokenizer.hasMoreTokens()) {
          String part = tokenizer.nextToken().trim();
          CoordinateID c2 = null;
          for (int d = 0; d < directions.length; ++d) {
            if (directions[d].equals(part)) {
              c2 =
                  region.getCoordinate()
                      .translate(magellan.library.utils.Direction.toCoordinate(d));
              break;
            }
          }
          if (c2 == null)
            throw new ParseException("invalid coordinate in " + group);
          Region r2 = world.getRegion(c2);
          if (r2 == null) {
            r2 = MagellanFactory.createRegion(c2, world);
            r2.setType(RegionType.unknown);
            world.addRegion(r2);
          }
        }
        for (Direction dir : Direction.getDirections()) {
          CoordinateID c2 = region.getCoordinate().translate(dir.toCoordinate());
          if (world.getRegion(c2) == null) {
            Region r2 = MagellanFactory.createRegion(c2, world);
            r2.setType(RegionType.unknown);
            world.addRegion(r2);
            final RegionType type = world.rules.getRegionType(oceanType, true);
            r2.setType(type);
          }
        }
      }
    }
  }

}
