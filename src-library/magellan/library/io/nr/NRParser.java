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
import magellan.library.CoordinateID;
import magellan.library.EntityID;
import magellan.library.Faction;
import magellan.library.GameData;
import magellan.library.Message;
import magellan.library.MissingData;
import magellan.library.Region;
import magellan.library.Rules;
import magellan.library.StringID;
import magellan.library.Unit;
import magellan.library.UnitID;
import magellan.library.io.AbstractReportParser;
import magellan.library.io.GameDataIO;
import magellan.library.io.RulesIO;
import magellan.library.io.file.FileType;
import magellan.library.rules.RegionType;
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
  protected static String word = "(\\w+)";
  protected static String num = "(" + number + ")";
  protected static String atlantisLine = "\\s*" + name + " Turn Report(.*)";
  protected static String factionLine = "\\s*" + name + " " + id2 + "\\s*";
  protected static String dateLine = "\\s*" + name + ", Year " + num + "\\s*";
  protected static String mistakesLine = "\\s*Mistakes\\s*";
  protected static String messageLine = "^(.*)$";
  protected static String messagesLine = "\\s*Messages\\s*";
  protected static String object = name + " " + id2;
  protected static String object2 = "\\s" + name + " " + id2 + "\\s*";
  protected static String eventsLine = "\\s*Events During Turn\\s*";
  protected static String statusLine = "\\s*Current Status\\s*";
  protected static String alliedLine = "You are allied to " + object + "(," + object + ")*\\.";
  protected static String coord2 = "(\\((" + num + "),(" + num + ")\\))";
  protected static String coord3 = "(\\((" + num + "),(" + num + "),(" + num + ")\\))";
  protected static String ocean = "ocean";
  protected static String regionLine = "^" + name + " " + coord2 + ", " + word
      + "(, exits: ([^.]*))?.\\s*(peasants: (" + num + ")(, \\$(" + num + "))?.)?";
  protected static String peasantsLine = "^peasants: " + num + ", \\$" + num + "\\.";
  //
  protected static String unitLine = "  ([*-+]) " + object + "(.*)";
  protected static String knownUnitLine = "  ([*-+]) " + object + ", faction " + object + "(.*)";

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
  protected static Pattern knownUnitLinePattern = Pattern.compile(knownUnitLine);

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
                log.warn("unexpected line: " + line, e);
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

  }

  protected class AbstractReader {
    protected Matcher matcher;
    protected boolean matched;

    protected Pattern startPattern;

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
      matcher = pattern.matcher(line);
      matched = matcher.matches();
      if (!matched && exception)
        throw new ParseException(pattern, line);
      return matched;
    }

    protected boolean expectLine(Pattern pattern) throws ParseException {
      return expectLine(pattern, true);
    }

  }

  class HeaderReader extends AbstractReader implements SectionReader {

    HeaderReader() {
      super(atlantisLinePattern);
    }

    public void parse() throws IOException, ParseException {
      expectLine(atlantisLinePattern);
      world.setGameName(matcher.group(1));
      world.base = 10;
      nextLine(false, false);

      expectLine(factionLinePattern);
      EntityID factionId;
      world.setOwnerFaction(factionId = EntityID.createEntityID(matcher.group(2), world.base));
      ownerFaction = getAddFaction(factionId);
      ownerFaction.setName(matcher.group(1));
      nextLine(false, false);

      expectLine(dateLinePattern);
      world.setDate(new SimpleDate(matcher.group(1), matcher.group(2)));
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
        messages.add(MagellanFactory.createMessage(line));
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
        addAlly(allies, matcher.group(2), matcher.group(1));

        if (matcher.group(3) != null && matcher.group(3).length() > 0) {
          StringTokenizer tokenizer = new StringTokenizer(matcher.group(3), ",");
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

    RegionReader() {
      super(regionLinePattern);
    }

    public void parse() throws IOException, ParseException {

      if (expectLine(regionLinePattern)) {
        // "^" + name + " " + coord2 + ", " + word +
        // "(, exits: ([^.]*))?.\\s*(peasants: ("+num+")(, $("+num+"))?.)?";
        CoordinateID c =
            CoordinateID.parse(matcher.group(2).substring(1, matcher.group(2).length() - 1), ",");

        if (c == null)
          throw new ParseException("no region coordinate", line);

        c = originTranslate(c);

        Region region = world.getRegion(c);

        if (region == null) {
          region = MagellanFactory.createRegion(c, world);
        }
        if (!ocean.equals(matcher.group(7))) {
          region.setName(matcher.group(1));
        }

        try {
          final RegionType type =
              world.rules.getRegionType(StringID.create(matcher.group(7)), true);
          region.setType(type);
        } catch (final IllegalArgumentException e) {
          region.setType(RegionType.unknown);
          // can happen in StringID constructor if sc.argv[0] == ""
          log.warn("CRParser.parseRegion(): found region without a valid region type in line "
              + lnr);
        }

        parseExits(region, matcher.group(9));
        nextLine(true, false);
        if (expectLine(peasantsLinePattern, false)) {
          // TODO
          nextLine(true, false);
        }
        if (world.getRegion(region.getCoordinate()) == null) {
          world.addRegion(region);
        }

        while (line != null && line.length() > 0) {
          Unit unit = null;
          if (expectLine(knownUnitLinePattern, false)) {
            unit = getAddUnit(UnitID.createUnitID(matcher.group(3), world.base), false);
            unit.setName(matcher.group(2));
            Faction faction = getAddFaction(EntityID.createEntityID(matcher.group(5), world.base));
            if (faction.getName() == null) {
              faction.setName(matcher.group(4));
            }
            unit.setFaction(faction);
          } else if (expectLine(unitLinePattern, false)) {
            unit = getAddUnit(UnitID.createUnitID(matcher.group(3), world.base), false);
            if (unit.getName() != null) {
              unit.setName(matcher.group(2));
            }
            unit.setHideFaction(true);
          }
          if (unit != null) {
            // if (humans=null)
            // humans=world.getRules().getRaces().iterator().next();
            // unit.setRace(humans);
            unit.setRace(world.getRules().getRaces().iterator().next());
            unit.setRegion(region);
          }
          nextLine(true, false);
        }
        if (line != null && line.length() == 0) {
          nextLine(true, true);
        }
      }

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
