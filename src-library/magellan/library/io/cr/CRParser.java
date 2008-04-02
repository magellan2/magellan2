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

package magellan.library.io.cr;

import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import magellan.library.Alliance;
import magellan.library.Battle;
import magellan.library.Border;
import magellan.library.Building;
import magellan.library.CombatSpell;
import magellan.library.CoordinateID;
import magellan.library.EntityID;
import magellan.library.Faction;
import magellan.library.GameData;
import magellan.library.Group;
import magellan.library.HotSpot;
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
import magellan.library.Ship;
import magellan.library.Sign;
import magellan.library.Skill;
import magellan.library.Spell;
import magellan.library.StringID;
import magellan.library.Unit;
import magellan.library.UnitContainer;
import magellan.library.UnitID;
import magellan.library.io.GameDataIO;
import magellan.library.io.RulesIO;
import magellan.library.io.file.FileType;
import magellan.library.rules.AllianceCategory;
import magellan.library.rules.BuildingType;
import magellan.library.rules.CastleType;
import magellan.library.rules.Date;
import magellan.library.rules.EresseaDate;
import magellan.library.rules.GenericRules;
import magellan.library.rules.ItemCategory;
import magellan.library.rules.ItemType;
import magellan.library.rules.MessageType;
import magellan.library.rules.OptionCategory;
import magellan.library.rules.Options;
import magellan.library.rules.Race;
import magellan.library.rules.RegionType;
import magellan.library.rules.Resource;
import magellan.library.rules.ShipType;
import magellan.library.rules.SkillCategory;
import magellan.library.rules.SkillType;
import magellan.library.utils.MagellanFactory;
import magellan.library.utils.MemoryManagment;
import magellan.library.utils.NullUserInterface;
import magellan.library.utils.OrderedHashtable;
import magellan.library.utils.Resources;
import magellan.library.utils.TranslationType;
import magellan.library.utils.UserInterface;
import magellan.library.utils.logging.Logger;


/**
 * Parser for cr-files.
 */
public class CRParser implements RulesIO, GameDataIO {
  private static final Logger log = Logger.getInstance(CRParser.class);
  
  
  /** These special tags are used by TreeHelper and are therefore reserved. */
  public static final String TAGGABLE_STRING = "ejcTaggableComparator";
  public static final String TAGGABLE_STRING2 = "ejcTaggableComparator2";
  public static final String TAGGABLE_STRING3 = "ejcTaggableComparator3";
  public static final String TAGGABLE_STRING4 = "ejcTaggableComparator4";
  public static final String TAGGABLE_STRING5 = "ejcTaggableComparator5";

  Scanner sc;
  GameData world;
  String configuration;
  String coordinates;
  String game;
  boolean umlauts;
  int version = 0; // the version of the report
  
  CoordinateID newOrigin = new CoordinateID(0,0,0);
  
  private Collection<String> warnedLines = new HashSet<String>();
  
  private UserInterface ui = null;
  private Faction firstFaction;

  /**
   * Creates a new parser.
   * 
   * @param ui The UserInterface for the progress. Can be NULL. Then no operation is displayed.
   */
  public CRParser(UserInterface ui){
    if (ui == null) ui = new NullUserInterface();
    this.ui = ui;
  }
  
  /**
   * Creates a new parser. This new parser translates coordinates according to newOrigin.
   * 
   * All coordinates which are read from the report are translated by newOrigin. That is, if a coordinate read and its level
   * (the z coordinate) equals the new origins level, its x and y coordinates are decreased by origin.x and origin.y, respectively.
   * That means, that the reports origin is transferred to newOrigin.
   * 
   * @param newOrigin The coordinates (relative to the origin of the report) of the new origin. 
   *
   */
  // FIXME Other games might want to change coordinate systems on all levels at once!
    public CRParser(UserInterface ui, CoordinateID newOrigin){
      this.ui = ui;
      this.newOrigin = newOrigin;
    }
    
    /**
     * 
     */
    CoordinateID originTranslate(CoordinateID c){
      if (c.z == newOrigin.z){
        c.x-=newOrigin.x;
        c.y-=newOrigin.y;
      }
      return c;
    }
    
    /**
     * 
     */
    CoordinateID inverseOriginTranslate(CoordinateID c){
      if (c.z == newOrigin.z){
        c.x+=newOrigin.x;
        c.y+=newOrigin.y;
      }
      return c;
    }
    
  /**
   * Tries to replace coordinates in string by the translated version.
   * 
   * The string is searched for occurences of the form "(123,123)" or "(123,123,123)" or
   * "(123,123,Astralraum)", transforms them and replaces them. This is note completely fool-proof!
   * 
   * @param string Usually a message text which might contain coordinates
   * @deprecated We should rather use Message.render() for this purpose 
   */
  private String originTranslate(String string) {
//    String debugString="Vogt";
//    if (string.indexOf(debugString)>=0){
//      log.info(string);
//    }
    StringBuffer result = new StringBuffer();
    String number = "[\\+\\-]?\\d+";
    // FIXME Look up "Astralraum"
    Matcher matcher = Pattern.compile("\\(("+number+")\\,\\ ?("+number+")(\\,\\ ?(("+number+")|Astralraum))?\\)").matcher(string);
    while(matcher.find()){
      String candi = matcher.group();
        candi=candi.replaceAll("Astralraum", "1");
      CoordinateID coord = CoordinateID.parse(candi.substring(1,candi.length()-1), ",");
      if (coord!=null){
        originTranslate(coord);
        matcher.appendReplacement(result, "("+coord.toString()+")");  
      } else {
        matcher.appendReplacement(result, matcher.group());
      }
    }
    matcher.appendTail(result);
    return result.toString();
  }

  /**
   * special sub to translate coords in ";regions" tags of messages
   * expecting this form "x1 y1 z1, x2 y2 z2";regions
   * @param string
   * @return
   */
  private String originTranslateRegions(String string){
    String result = string;
    String helper = "";
    boolean didSomething=false;
    result = result.replace("\"","");
    if (result.length()<3){
      return string;
    }
    if (result.indexOf(",")>0){
      // Split it
      String[] s = result.split(",");
      for(int i=0;i<s.length;i++){
        String sC = s[i];
        CoordinateID c = CoordinateID.parse(sC," ");
        if (c!=null){
          // we got valid coordinates - translate it
          c = originTranslate(c);
          didSomething=true;
          sC=c.toString(" ", true);
        }
        // , between coords
        if (helper.length()>0){
          helper = helper.concat(", "); 
        }
        helper = helper.concat(sC);
      }
    } else {
      CoordinateID c = CoordinateID.parse(result," ");
      if (c!=null){
        c = originTranslate(c);
        didSomething=true;
        helper=c.toString(" ", true);
      }
    }
    
    if (!didSomething){
      return string;
    }
    
    return helper;
  }
  
  /**
   * Print an error message on the standard output channel.
   *
   * @param context The context (usually a block) within the error has been found.
   * @param fetch If this is true, read the next line and skip the line with the error. Otherwise
   *       the line stays still at the front of the input.
   */
  private void unknown(String context, boolean fetch) throws IOException {
    int i;

    StringBuffer msg = new StringBuffer();

    for(i = 0; i < sc.argc; i++) {
      if(sc.isString[i]) {
        msg.append("\"");
      }

      msg.append(sc.argv[i]);

      if(sc.isString[i]) {
        msg.append("\"");
      }

      if((i + 1) < sc.argc) {
        msg.append(";");
      }
    }

        if(!warnedLines.contains(context+"_"+msg)) {
            // only warn once for context and message combination
            log.warn("unknown in line " + sc.lnr + ": (" + context + ")");
            log.warn(msg);
            warnedLines.add(context+"_"+msg);
        }
            
    if(fetch) {
      sc.getNextToken();
    }
  }

  /**
   * Helper function: Find a faction in world. If not found, create
   * one and insert it.
   */
  private Faction getAddFaction(GameData world, EntityID id) {
    Faction faction = world.getFaction(id);

    if(faction == null) {
      faction = MagellanFactory.createFaction(id, world);
      world.addFaction(faction);
    }

    return faction;
  }

  /**
   * Helper function: Find a unit in world. If not found, create
   * one and insert it.
   */
  private Unit getAddUnit(GameData world, UnitID id) {
    Unit unit = world.getUnit(id);

    if(unit == null) {
      unit = MagellanFactory.createUnit(id);
      world.addUnit(unit);
    }

    return unit;
  }

  /**
   * Helper function: Find a building in world. If not found, create
   * one and insert it.
   */
  private Building getAddBuilding(GameData world, EntityID id) {
    Building building = world.getBuilding(id);

    if(building == null) {
      building = MagellanFactory.createBuilding(id, world);
      world.addBuilding(building);
    }

    return building;
  }

  /**
   * Helper function: Find a ship in world. If not found, create
   * one and insert it.
   */
  private Ship getAddShip(GameData world, EntityID id) {
    Ship ship = world.getShip(id);

    if(ship == null) {
      ship = MagellanFactory.createShip(id, world);
      world.addShip(ship);
    }

    return ship;
  }

  public String getConfiguration() {
    return configuration;
  }

  /**
   * @return The first faction encountered while parsing, <code>null</code> if not applicable
   */
  public Faction getFirstFaction() {
    return firstFaction;
  }

  /**
   * Read the MESSAGETYPES block. Note that message type stubs have already been created by
   * parsing the messages themselves.
   *
   * 
   *
   * @return the resulting list of <tt>MessageType</tt> objects.
   *
   * @throws IOException DOCUMENT-ME
   */
  private List<MessageType> parseMessageTypes(GameData data) throws IOException {
    List<MessageType> list = new LinkedList<MessageType>();
    sc.getNextToken(); // skip the block

    while(!sc.eof && !sc.isBlock) {
      if(sc.argc == 2) {
        try {
          MessageType mt = data.getMsgType(IntegerID.create(sc.argv[1]));

          if(mt == null) {
            mt = new MessageType(IntegerID.create(sc.argv[1]), sc.argv[0]);
            data.addMsgType(mt);
          } else {
            mt.setPattern(sc.argv[0]);
            // set the GameData were this message type belongs to
            // this is required to render messages of this type
            mt.setGameData(data);
          }
          
          list.add(mt);
        } catch(NumberFormatException e) {
          log.error(e);
        }
      }

      sc.getNextToken();
    }

    return list;
  }

  /**
   * Read a MESSAGETYPE block. Note that message type stubs have already been created by parsing
   * the messages themselves.
   *
   * 
   *
   * @throws IOException DOCUMENT-ME
   */
  private void parseMessageType(GameData data) throws IOException {
    ID id = IntegerID.create(sc.argv[0].substring(12).trim());
    MessageType mt = data.getMsgType(id);

    if(mt == null) {
      mt = new MessageType(id);
      data.addMsgType(mt);
    }

    sc.getNextToken(); // skip the block

    while(!sc.eof && !sc.isBlock) {
      if((sc.argc == 2) && sc.argv[1].equalsIgnoreCase("text")) {
        mt.setPattern(sc.argv[0]);
        // set the GameData were this message type belongs to
        // this is required to render messages of this type
        mt.setGameData(data);
      } else if((sc.argc == 2) && sc.argv[1].equalsIgnoreCase("section")) {
        mt.setSection(sc.argv[0]);
      } else {
        unknown("MESSAGETYPE", false);
      }

      sc.getNextToken();
    }
  }

  /**
   * Handle a sequence of quoted strings, interpreting them as messages.
   *
   * @param msgs a list to add the read messages to
   *
   * @return the resulting list of <tt>Message</tt> objects.
   */
  private List<Message> parseMessageSequence(List<Message> msgs) throws IOException {
    sc.getNextToken(); // skip the block

    while(!sc.eof && (sc.argc == 1) && sc.isString[0]) {
      if(msgs == null) {
        msgs = new ArrayList<Message>();
      }

      // 2002.04.24 pavkovic: remove duplicate entries
      Message msg = MagellanFactory.createMessage(sc.argv[0]);

      if(msgs.contains(msg)) {
        // log.warn("Duplicate message \"" + msg.getText() + "\" found, removing it.");
        if(log.isDebugEnabled()) {
          log.debug("List: " + msgs);
          log.debug("new entry:" + msg);
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
   * @param strings a list to add the read strings to.
   *
   * @return the resulting list of <tt>String</tt> objects.
   */
  private List<String> parseStringSequence(List<String> strings) throws IOException {
    sc.getNextToken(); // skip the block

    while(!sc.eof && (sc.argc == 1) && sc.isString[0]) {
      if(strings == null) {
        strings = new LinkedList<String>();
      }

      strings.add(sc.argv[0]);
      sc.getNextToken();
    }

    // avoid unnecessary list allocations
    if((strings != null) && (strings.size() == 0)) {
      strings = null;
    }

    return strings;
  }

  /**
   * Parse the SPRUECHE sub block of UNIT and add them as <tt>Spell</tt> objects.
   *
   * @param world the game data to get the spells from
   * @param map a map to add the read spells to
   *
   * @return the resulting map of <tt>Spell</tt> objects.
   */
  private Map<ID, Spell> parseUnitSpells(GameData world, Map<ID,Spell> map) throws IOException {
    sc.getNextToken(); // skip the block

    while(!sc.eof && !sc.isBlock) {
      ID id = StringID.create(sc.argv[0]);
      Spell s = world.getSpell(id);

      if(s == null) {
        s = MagellanFactory.createSpell(id,world);
        s.setName(sc.argv[0]);
        world.addSpell(s);
      }

      if(map == null) {
        map = new OrderedHashtable<ID, Spell>();
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
  private void parseUnitCombatSpells(GameData world, Unit unit) throws IOException {
    ID id = IntegerID.create(sc.argv[0].substring(12).trim());
    CombatSpell s = MagellanFactory.createCombatSpell(id);
    s.setUnit(unit);

    if(unit.getCombatSpells() == null) {
      unit.setCombatSpells(new Hashtable<ID, CombatSpell>());
    }

    unit.getCombatSpells().put(s.getID(), s);

    sc.getNextToken();

    while(!sc.eof && !sc.isBlock) {
      if((sc.argc == 2) && sc.argv[1].equalsIgnoreCase("name")) {
        ID spellID = StringID.create(sc.argv[0]);
        Spell spell = world.getSpell(spellID);

        if(spell == null) {
          log.warn("CRParser.parseUnitCombatSpells(): a combat spell refers to an unknown spell (line " +
               sc.lnr + ")");
          spell = MagellanFactory.createSpell(spellID,world);
          spell.setName(sc.argv[0]);
          world.addSpell(spell);
        }

        s.setSpell(spell);
      } else if((sc.argc == 2) && sc.argv[1].equalsIgnoreCase("level")) {
        s.setCastingLevel(Integer.parseInt(sc.argv[0]));
      } else {
        unknown("KAMPFZAUBER", false);
      }

      sc.getNextToken();
    }
  }

  /**
   * Parse message blocks as can be found in cr versions >= 41. This function evaluates only two
   * special message attributes. These are the ";type" and ";rendered" attributes, which are
   * directly accessible in the <tt>Message</tt> object as type and text. If there is no
   * MessageType object for this type of message, a stub MessageType object is created and added
   * to world.
   *
   * 
   * 
   *
   * @return a list containing <tt>Message</tt> objects for all messages read.
   *
   * @throws IOException DOCUMENT-ME
   */
  private List<Message> parseMessages(GameData world, List<Message> list) throws IOException {
    while(sc.isBlock && sc.argv[0].startsWith("MESSAGE ")) {
      ID id = IntegerID.create(sc.argv[0].substring(8));
      Message msg = MagellanFactory.createMessage(id);

      // read message attributes
      sc.getNextToken(); // skip MESSAGE xx

      while(!sc.eof && !sc.isBlock) {
        if((sc.argc == 2) && sc.argv[1].equalsIgnoreCase("type")) {
          ID typeID = IntegerID.create(sc.argv[0]);
          MessageType mt = world.getMsgType(typeID);

          if(mt == null) {
            mt = new MessageType(typeID);
            world.addMsgType(mt);
          }

          msg.setType(mt);
        } else if((sc.argc == 2) && sc.argv[1].equalsIgnoreCase("rendered")) {
          msg.setText(originTranslate(sc.argv[0]));
        } else if(sc.argc == 2) {
          if(msg.getAttributes() == null) {
            msg.setAttributes(new OrderedHashtable<String, String>());
          }
          
          
          CoordinateID coord = CoordinateID.parse(sc.argv[0], ",");

          if (coord !=null){
            CoordinateID newCoord = originTranslate(coord);
            msg.getAttributes().put(sc.argv[1], newCoord.toString(","));
          }else{
            coord = CoordinateID.parse(sc.argv[0], " ");
            if (coord !=null){
              CoordinateID newCoord = originTranslate(coord);
              msg.getAttributes().put(sc.argv[1], newCoord.toString(" ", true));
            }else{
              // check for ;regions
              if (sc.argv[1].equalsIgnoreCase("regions")){
                // special dealing
                msg.getAttributes().put(sc.argv[1], this.originTranslateRegions(sc.argv[0]));
              } else {
                msg.getAttributes().put(sc.argv[1], sc.argv[0]);
              }
            }
          }
        }

        sc.getNextToken();
      }

      if(list == null) {
        list = new LinkedList<Message>();
      }
      list.add(msg);
    }

    return list;
  }

  /**
   * Parse a battle block sequence. Currently this is a block of message blocks.
   *
   * 
   * 
   *
   * @return A List of instances of class Battle.
   *
   * @throws IOException DOCUMENT-ME
   */
  private List<Battle> parseBattles(GameData world, List<Battle> list) throws IOException {
    while(!sc.eof && sc.argv[0].startsWith("BATTLE ")) {
      CoordinateID c = CoordinateID.parse(sc.argv[0].substring(sc.argv[0].indexOf(" ", 0)), " ");
      originTranslate(c);
      
      if(c == null) {
        unknown("BATTLE", true);

        continue;
      }

      Battle battle = MagellanFactory.createBattle(c);

      if(list == null) {
        list = new LinkedList<Battle>();
      }

      list.add(battle);
      sc.getNextToken(); // skip BATTLE x y
      parseMessages(world, battle.messages());
    }

    return list;
  }

  /**
   * Parse a battlespec block sequence. Currently this is a block of message blocks.
   *
   * 
   * 
   *
   * @return A List of instances of class Battle.
   *
   * @throws IOException DOCUMENT-ME
   */
  private List<Battle> parseBattleSpecs(GameData world, List<Battle> list) throws IOException {
    while(!sc.eof && sc.argv[0].startsWith("BATTLESPEC ")) {
      CoordinateID c = CoordinateID.parse(sc.argv[0].substring(sc.argv[0].indexOf(" ", 0)), " ");
      originTranslate(c);
      
      if(c == null) {
        unknown("BATTLESPEC", true);

        continue;
      }

      Battle battle = MagellanFactory.createBattle(c, true);

      if(list == null) {
        list = new LinkedList<Battle>();
      }

      list.add(battle);
      sc.getNextToken(); // skip BATTLE x y
      parseMessages(world, battle.messages());
    }

    return list;
  }

  /**
   * Parse a sequence of spell blocks. Do not confuse this with the spells block of a unit!
   *
   * 
   *
   * @throws IOException DOCUMENT-ME
   */
  private void parseSpells(GameData world) throws IOException {
    while(!sc.eof && sc.isBlock && sc.argv[0].startsWith("ZAUBER ")) {
      ID id = IntegerID.create(sc.argv[0].substring(7).trim());

      // not adding spell immediately is required here, please do not change this, unless you really know, what you're doing!
      Spell spell = MagellanFactory.createSpell(id,world);
      spell.setBlockID(((IntegerID) id).intValue());
      sc.getNextToken(); // skip ZAUBER nr

      while(!sc.eof) {
        if((sc.argc == 2) && sc.argv[1].equalsIgnoreCase("name")) {
          spell.setName(sc.argv[0]);
          sc.getNextToken();
        } else if((sc.argc == 2) && sc.argv[1].equalsIgnoreCase("info")) {
          spell.setDescription(sc.argv[0]);
          sc.getNextToken();
        } else if((sc.argc == 2) && sc.argv[1].equalsIgnoreCase("level")) {
          spell.setLevel(Integer.parseInt(sc.argv[0]));
          sc.getNextToken();
        } else if((sc.argc == 2) && sc.argv[1].equalsIgnoreCase("rank")) {
          spell.setRank(Integer.parseInt(sc.argv[0]));
          sc.getNextToken();
        } else if((sc.argc == 2) && sc.argv[1].equalsIgnoreCase("class")) {
          spell.setType(sc.argv[0]);
          sc.getNextToken();
        } else if((sc.argc == 2) && sc.argv[1].equalsIgnoreCase("syntax")) {
          // FF 20070221 : new ;syntax
          spell.setSyntax(sc.argv[0]);
          sc.getNextToken();
        } else if((sc.argc == 2) && sc.argv[1].equalsIgnoreCase("ship")) {
          spell.setOnShip(Integer.parseInt(sc.argv[0]) != 0);
          sc.getNextToken();
        } else if((sc.argc == 2) && sc.argv[1].equalsIgnoreCase("ocean")) {
          spell.setOnOcean(Integer.parseInt(sc.argv[0]) != 0);
          sc.getNextToken();
        } else if((sc.argc == 2) && sc.argv[1].equalsIgnoreCase("familiar")) {
          spell.setIsFamiliar(Integer.parseInt(sc.argv[0]) != 0);
          sc.getNextToken();
        } else if((sc.argc == 2) && sc.argv[1].equalsIgnoreCase("far")) {
          spell.setIsFar(Integer.parseInt(sc.argv[0]) != 0);
          sc.getNextToken();
        } else if(sc.isBlock && sc.argv[0].equals("KOMPONENTEN")) {
          Map<String,String> map = new Hashtable<String, String>();
          sc.getNextToken(); // skip KOMPONENTEN

          while(!sc.eof && !sc.isBlock && (sc.argc == 2)) {
            map.put(sc.argv[1], sc.argv[0]);
            sc.getNextToken();
          }

          spell.setComponents(map);
        } else if(sc.isBlock) {
          break;
        } else {
          unknown("ZAUBER", true);
        }
      }

      if(spell.getName() != null) {
        // spell.setID(StringID.create(spell.getName()));
        world.addSpell(spell);
      }
    }
  }

  /**
   * Parse a sequence of potion (TRANK) blocks.
   *
   * 
   *
   * @throws IOException DOCUMENT-ME
   */
  private void parsePotions(GameData world) throws IOException {
    while(!sc.eof && sc.isBlock && sc.argv[0].startsWith("TRANK ")) {
      ID id = IntegerID.create(sc.argv[0].substring(6));
      Potion potion = world.getPotion(id);

      if(potion == null) {
        potion = MagellanFactory.createPotion(id);
        world.addPotion(potion);
      }

      sc.getNextToken(); // skip TRANK nr

      while(!sc.eof) {
        if((sc.argc == 2) && sc.argv[1].equalsIgnoreCase("Name")) {
          potion.setName(sc.argv[0]);
          sc.getNextToken();
        } else if((sc.argc == 2) && sc.argv[1].equalsIgnoreCase("Stufe")) {
          potion.setLevel(Integer.parseInt(sc.argv[0]));
          sc.getNextToken();
        } else if((sc.argc == 2) && sc.argv[1].equalsIgnoreCase("Beschr")) {
          potion.setDescription(sc.argv[0]);
          sc.getNextToken();
        } else if(sc.isBlock && sc.argv[0].equals("ZUTATEN")) {
          sc.getNextToken(); // skip ZUTATEN block

          while(!sc.eof && !sc.isBlock && (sc.argc == 1)) {
            ItemType it = world.rules.getItemType(StringID.create(sc.argv[0]), true);
            Item i = new Item(it, 1);
            potion.addIngredient(i);
            sc.getNextToken();
          }
        } else if(sc.isBlock) {
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
   * 
   *
   * @throws IOException DOCUMENT-ME
   */
  private void parseIslands(GameData world) throws IOException {
    while(!sc.eof && sc.isBlock && sc.argv[0].startsWith("ISLAND ")) {
      //ID id = StringID.create(sc.argv[0].substring(7));
      ID id = IntegerID.create(sc.argv[0].substring(7));
      Island island = world.getIsland(id);

      if(island == null) {
        island = MagellanFactory.createIsland(id, world);
        world.addIsland(island);
      }

      sc.getNextToken(); // skip ISLAND nr

      while(!sc.eof && !sc.isBlock) {
        if((sc.argc == 2) && sc.argv[1].equalsIgnoreCase("name")) {
          island.setName(sc.argv[0]);
        } else if((sc.argc == 2) && sc.argv[1].equalsIgnoreCase("Beschr")) {
          island.setDescription(sc.argv[0]);
        } else {
          unknown("ISLAND", false);
        }

        sc.getNextToken();
      }
    }
  }

  /**
   * DOCUMENT ME!
   *
   * @param in The reader, that will read the file for us.
   *
   * @return a map, that maps all found header tags to their values.
   *
   * @throws java.io.IOException DOCUMENT-ME
   */
  public synchronized Map<String,Object> readHeader(Reader in) throws java.io.IOException {
    Map<String,Object> map = new HashMap<String, Object>();
    sc = new Scanner(in);
    sc.getNextToken();

    if(!sc.argv[0].startsWith("VERSION ")) {
      log.warn("CRParser.readHeader(): CR doesn't start with VERSION block.");

      return map;
    } else {
      try {
        map.put("_version_",
            new Integer(sc.argv[0].substring(sc.argv[0].indexOf(' ')).trim()));
      } catch(Exception exc) {
        log.warn("CRParser.readHeader(): Failed to parse  VERSION number. (setting 0)");
        log.warn(exc.toString());
        map.put("_version_", new Integer(0));
      }
    }

    /* Now read as long as the file lasts, or until a new block is found,
     * which will terminate the header. */
    sc.getNextToken();

    while(!sc.eof && !sc.isBlock) {
      if(sc.argc == 2) {
        map.put(sc.argv[1], sc.argv[0]);
      } else {
        log.warn("CRParser.readHeader(): Malformed tag on line " + sc.lnr);
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
   * @throws IOException
   *           DOCUMENT-ME
   */
  private void parseHeader(GameData world) throws IOException {
    Region specialRegion = null;
    int factionSortIndex = 0;
    int regionSortIndex = 0;
    int blankPos = sc.argv[0].indexOf(' ');

    if(blankPos > 0) {
      version = Integer.parseInt(sc.argv[0].substring(blankPos).trim());
    } else {
      version = 0;
    }

    sc.getNextToken(); // skip "VERSION xx"

    while(!sc.eof) {
      if((sc.argc == 2) && sc.argv[1].equalsIgnoreCase("Spiel")) {
        game = sc.argv[0];
        sc.getNextToken();
      } else if((sc.argc == 2) && sc.argv[1].equalsIgnoreCase("Konfiguration")) {
        configuration = sc.argv[0];
        sc.getNextToken();
      } else if((sc.argc == 2) && sc.argv[1].equalsIgnoreCase("Koordinaten")) {
        coordinates = sc.argv[0];
        sc.getNextToken();
      } else if((sc.argc == 2) && sc.argv[1].equalsIgnoreCase("Basis")) {
        try {
          world.base = Integer.parseInt(sc.argv[0]);
        } catch(NumberFormatException e) {
          world.base = 0;
        }

        if((world.base <= 0) || (world.base > 36)) {
          world.base = 10;
        }
        /**
         * asuing we have already the gamename we can make an additional check
         * Buck Tracking wrong base...
         */
        if (world.getGameName()!=null){
          String actGameName = world.getGameName().toLowerCase();
          if ((actGameName.indexOf("eressea")>-1 || actGameName.indexOf("vinyambar")>-1) && (world.base!=36)){
            // this should not happen
            log.warn("BASE ERROR !! read report could have not base36 !! Changed to base36.");
            world.base = 36;
          }
        }
        
        
        
        //com.eressea.util.IDBaseConverter.setBase(world.base);
        sc.getNextToken();
      } else if((sc.argc == 2) && sc.argv[1].equalsIgnoreCase("Umlaute")) {
        umlauts = Integer.parseInt(sc.argv[0]) != 0;
        sc.getNextToken();
      } else if((sc.argc == 2) && sc.argv[1].equalsIgnoreCase("curTempID")) {
        try {
          world.setCurTempID(Integer.parseInt(sc.argv[0]));
        } catch(NumberFormatException nfe) {
          log.warn("Error: Illegal Number format in line " + sc.lnr + ": " + sc.argv[0]);
          log.warn("Setting the corresponding value GameData.curTempID to default value!");
          world.setCurTempID(-1);
        }

        sc.getNextToken();
      } else if((sc.argc == 2) && sc.argv[1].equalsIgnoreCase("Runde")) {
        Date d = world.getDate();

        if(d == null) {
          world.setDate(new EresseaDate(Integer.parseInt(sc.argv[0])));
        } else {
          d.setDate(Integer.parseInt(sc.argv[0]));
        }

        sc.getNextToken();
      } else if((sc.argc == 2) && sc.argv[1].equalsIgnoreCase("Zeitalter")) {
        EresseaDate d = (EresseaDate) world.getDate();

        if(d == null) {
          d = new EresseaDate(0);
          d.setEpoch(Integer.parseInt(sc.argv[0]));
          world.setDate(d);
        } else {
          d.setEpoch(Integer.parseInt(sc.argv[0]));
        }

        sc.getNextToken();
      } else if((sc.argc == 2) && sc.argv[1].equalsIgnoreCase("locale")) {
        world.setLocale(new Locale(sc.argv[0], ""));
        sc.getNextToken();
      } else if((sc.argc == 2) && sc.argv[1].equalsIgnoreCase("date")) {
        // ignore date tag
        sc.getNextToken();
      } else if((sc.argc == 2) && sc.argv[1].equalsIgnoreCase("noskillpoints")) {
        world.noSkillPoints = (Integer.parseInt(sc.argv[0]) != 0);
        sc.getNextToken();
      } else if((sc.argc == 2) && sc.argv[1].equalsIgnoreCase("mailcmd")) {
        world.mailSubject = sc.argv[0];
        sc.getNextToken();
      } else if((sc.argc == 2) && sc.argv[1].equalsIgnoreCase("mailto")) {
        world.mailTo = sc.argv[0];
        sc.getNextToken();
      } else if((sc.argc == 2) && sc.argv[1].equalsIgnoreCase("reportowner")) {
        if (world.getOwnerFaction()==null && !configuration.equals("Standard"))
          world.setOwnerFaction(EntityID.createEntityID(Integer.parseInt(sc.argv[0]), world.base));
        sc.getNextToken();
      } else if((sc.argc == 1) && sc.argv[0].startsWith("COORDTRANS")) {
        parseCoordinateTransformation(world);
      } else if((sc.argc == 1) && sc.argv[0].startsWith("RULES")) {
        parseRules(world.rules);
      } else if((sc.argc == 1) && sc.argv[0].startsWith("HOTSPOT ")) {
        parseHotSpot(world);
      } else if((sc.argc == 1) && sc.argv[0].startsWith("PARTEI ")) {
        parseFaction(world, factionSortIndex++);
      } else if((sc.argc == 1) && sc.argv[0].startsWith("ZAUBER ")) {
        parseSpells(world);
      } else if((sc.argc == 1) && sc.argv[0].startsWith("TRANK ")) {
        parsePotions(world);
      } else if((sc.argc == 1) && sc.argv[0].startsWith("ISLAND ")) {
        parseIslands(world);
      } else if((sc.argc == 1) && sc.argv[0].startsWith("REGION ")) {
        parseRegion(world, regionSortIndex++);
      } else if((sc.argc == 1) && sc.argv[0].startsWith("SPEZIALREGION ")) {
        specialRegion = parseSpecialRegion(world, specialRegion);
      } else if((sc.argc == 1) && sc.argv[0].equals("MESSAGETYPES")) {
        parseMessageTypes(world);
      } else if((sc.argc == 1) && sc.argv[0].startsWith("MESSAGETYPE ")) {
        parseMessageType(world);
      } else if((sc.argc == 1) && sc.argv[0].equals("TRANSLATION")) {
        parseTranslation(world);
      } else if((sc.argc == 2) && sc.argv[1].equalsIgnoreCase("CHARSET")) {
        // do nothing
        world.setEncoding(sc.argv[0]);
        sc.getNextToken();
      } else {
        unknown("VERSION", true);
      }
    }
  }

  private void parseMagellan(Rules rules) throws IOException {
    sc.getNextToken(); // skip MAGELLAN

    while(!sc.eof) {
      if((sc.argc == 2) && sc.argv[1].equalsIgnoreCase("class")) {
        rules.setGameSpecificStuffClassName(sc.argv[0]);
        sc.getNextToken();
      } else if(sc.isBlock) {
        break;
      } else {
        unknown("MAGELLAN", true);
      }
    }
  }

  private void parseRace(Rules rules) throws IOException {
    int f = sc.argv[0].indexOf("\"", 0);
    int t = sc.argv[0].indexOf("\"", f + 1);
    String id = sc.argv[0].substring(f + 1, t);
    Race race = rules.getRace(StringID.create(id), true);
    race.setName(id);
    sc.getNextToken(); // skip RACE xx

    while(!sc.eof) {
      if((sc.argc == 2) && sc.argv[1].equalsIgnoreCase("recruitmentcosts")) {
        race.setRecruitmentCosts(Integer.parseInt(sc.argv[0]));
        sc.getNextToken();
      } else if((sc.argc == 2) && sc.argv[1].equalsIgnoreCase("name")) {
        race.setName(sc.argv[0]);
        sc.getNextToken();
      } else if((sc.argc == 2) && sc.argv[1].equalsIgnoreCase("weight")) {
        race.setWeight(Float.parseFloat(sc.argv[0]));
        sc.getNextToken();
      } else if((sc.argc == 2) && sc.argv[1].equalsIgnoreCase("capacity")) {
        race.setCapacity(Float.parseFloat(sc.argv[0]));
        sc.getNextToken();
      } else if(sc.isBlock && sc.argv[0].equals("TALENTBONI")) {
        parseRaceSkillBonuses(race, rules);
      } else if(sc.isBlock && sc.argv[0].startsWith("TALENTBONI ")) {
        parseRaceTerrainSkillBonuses(race, rules);
      } else if(sc.isBlock && sc.argv[0].equals("SPECIALS")) {
        parseRaceSpecials(race, rules);
      } else if(sc.isBlock) {
        break;
      } else {
        unknown("RACE", true);
      }
    }
  }

  private void parseRaceSkillBonuses(Race race, Rules rules) throws IOException {
    sc.getNextToken(); // skip TALENTBONI

    while(!sc.eof && !sc.isBlock) {
      try {
        SkillType skillType = rules.getSkillType(StringID.create(sc.argv[1]), true);
        race.setSkillBonus(skillType, Integer.parseInt(sc.argv[0]));
      } catch(NumberFormatException e) {
        log.warn("CRParser.parseRaceSkillBonuses(): in line " + sc.lnr +
             ": unable to convert skill bonus " + sc.argv[0] +
             " to an integer. Ignoring bonus for skill " + sc.argv[1]);
      }

      sc.getNextToken();
    }
  }

  private void parseRaceTerrainSkillBonuses(Race race, Rules rules) throws IOException {
    int f = sc.argv[0].indexOf("\"", 0);
    int t = sc.argv[0].indexOf("\"", f + 1);
    String id = sc.argv[0].substring(f + 1, t);
    RegionType rType = rules.getRegionType(StringID.create(id), true);
    sc.getNextToken(); // skip TALENTBONI

    while(!sc.eof && !sc.isBlock) {
      try {
        SkillType skillType = rules.getSkillType(StringID.create(sc.argv[1]), true);
        race.setSkillBonus(skillType, rType, Integer.parseInt(sc.argv[0]));
      } catch(NumberFormatException e) {
        log.warn("CRParser.parseRaceTerrainSkillBonuses(): in line " + sc.lnr +
             ": unable to convert skill bonus " + sc.argv[0] +
             " to an integer. Ignoring bonus for skill " + sc.argv[1]);
      }

      sc.getNextToken();
    }
  }

  private void parseRaceSpecials(Race race, Rules rules) throws IOException {
    sc.getNextToken(); // skip SPECIALS

    while(!sc.eof) {
      if((sc.argc == 2) && sc.argv[1].equalsIgnoreCase("shiprange")) {
        race.setAdditiveShipBonus(Integer.parseInt(sc.argv[0]));
        sc.getNextToken();
      } else if(sc.isBlock) {
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
    int f = sc.argv[0].indexOf("\"", 0);
    int t = sc.argv[0].indexOf("\"", f + 1);
    String id = sc.argv[0].substring(f + 1, t);
    ItemType itemType = null;

    if(sc.argv[0].startsWith("ITEM ")) {
      itemType = rules.getItemType(StringID.create(id), true);
    } else if(sc.argv[0].startsWith("HERB ")) {
      itemType = rules.getItemType(StringID.create(id), true);
    }

    itemType.setName(id);

    Skill makeSkill = null;
    sc.getNextToken(); // skip ITEM xx

    while(!sc.eof) {
      if((sc.argc == 2) && sc.argv[1].equalsIgnoreCase("weight")) {
        itemType.setWeight(Float.parseFloat(sc.argv[0]));
        sc.getNextToken();
      } else if((sc.argc == 2) && sc.argv[1].equalsIgnoreCase("makeskill")) {
        makeSkill = new Skill(rules.getSkillType(StringID.create(sc.argv[0]), true), 0, 0,
                    0, false);
        itemType.setMakeSkill(makeSkill);
        sc.getNextToken();
      } else if((sc.argc == 2) && sc.argv[1].equalsIgnoreCase("makeskilllevel")) {
        makeSkill.setLevel(Integer.parseInt(sc.argv[0]));
        sc.getNextToken();
      } else if((sc.argc == 2) && sc.argv[1].equalsIgnoreCase("name")) {
        itemType.setName(sc.argv[0]);
        sc.getNextToken();
      } else if((sc.argc == 2) && sc.argv[1].equalsIgnoreCase("category")) {
        ID catID = StringID.create(sc.argv[0]);
        ItemCategory cat = rules.getItemCategory(catID, true);
        itemType.setCategory(cat);
        sc.getNextToken();
      } else if((sc.argc == 2) && sc.argv[1].equalsIgnoreCase("region")) {
        ID regionID = StringID.create(sc.argv[0]);
        rules.getRegionType(regionID, true);
        sc.getNextToken();
      } else if((sc.argc == 2) && sc.argv[1].equalsIgnoreCase("iconname")) {
        itemType.setIconName(sc.argv[0]);
        sc.getNextToken();
      } else if((sc.argc == 2) && sc.argv[1].equalsIgnoreCase("useskill")) {
        Skill useSkill = new Skill(rules.getSkillType(StringID.create(sc.argv[0]), true),
                       0, 1, 0, false);
        itemType.setUseSkill(useSkill);
        sc.getNextToken();
        // darcduck - 20.11.2007 added magic bag tag that indicates if an item can be stored in the magic bag
      } else if((sc.argc == 2) && sc.argv[1].equalsIgnoreCase("storeinbonw")) {
        itemType.setStoreableInBonw(Integer.parseInt(sc.argv[0]));
        sc.getNextToken();
      } else if(sc.isBlock && sc.argv[0].equals("RESOURCES")) {
        parseItemTypeResources(itemType, rules);
      } else if(sc.isBlock) {
        break;
      } else {
        unknown("ITEM", true);
      }
    }
  }

  private void parseItemTypeResources(ItemType itemType, Rules rules) throws IOException {
    sc.getNextToken(); // skips the block header

    while(!sc.eof && !sc.isBlock) {
      if(sc.argc == 2) {
        ItemType component = rules.getItemType(StringID.create(sc.argv[1]), true);
        Item i = new Item(component, Integer.parseInt(sc.argv[0]));
        itemType.addResource(i);
        sc.getNextToken();
      } else {
        unknown("RESOURCES", true);
      }
    }
  }

  private void parseSkillType(Rules rules) throws IOException {
    int f = sc.argv[0].indexOf("\"", 0);
    int t = sc.argv[0].indexOf("\"", f + 1);
    String id = sc.argv[0].substring(f + 1, t);
    SkillType skillType = rules.getSkillType(StringID.create(id), true);
    sc.getNextToken(); // skip SKILL xx

    while(!sc.eof && !sc.isBlock) {
      if((sc.argc == 2) && sc.argv[1].equalsIgnoreCase("Name")) {
        skillType.setName(sc.argv[0]);
        sc.getNextToken();
      } else if((sc.argc == 2) && sc.argv[1].equalsIgnoreCase("category")) {
        ID catID = StringID.create(sc.argv[0]);
        SkillCategory cat = rules.getSkillCategory(catID, true);
        skillType.setCategory(cat);
        sc.getNextToken();
      } else {
        unknown("SKILL", true);
      }
    }

    if(skillType.getName() == null) {
      skillType.setName(id);
    }
  }

  private void parseShipType(Rules rules) throws IOException {
    int f = sc.argv[0].indexOf("\"", 0);
    int t = sc.argv[0].indexOf("\"", f + 1);
    String id = sc.argv[0].substring(f + 1, t);
    ShipType shipType = rules.getShipType(StringID.create(id), true);
    sc.getNextToken(); // skip SHIPTYPE xx

    while(!sc.eof && !sc.isBlock) {
      if((sc.argc == 2) && sc.argv[1].equalsIgnoreCase("size")) {
        shipType.setMaxSize(Integer.parseInt(sc.argv[0]));
        sc.getNextToken();
      } else if((sc.argc == 2) && sc.argv[1].equalsIgnoreCase("name")) {
        shipType.setName(sc.argv[0]);
        sc.getNextToken();
      } else if((sc.argc == 2) && sc.argv[1].equalsIgnoreCase("level")) {
        shipType.setBuildLevel(Integer.parseInt(sc.argv[0]));
        sc.getNextToken();
      } else if((sc.argc == 2) && sc.argv[1].equalsIgnoreCase("range")) {
        shipType.setRange(Integer.parseInt(sc.argv[0]));
        sc.getNextToken();
      } else if((sc.argc == 2) && sc.argv[1].equalsIgnoreCase("capacity")) {
        shipType.setCapacity(Integer.parseInt(sc.argv[0]));
        sc.getNextToken();
      } else if((sc.argc == 2) && sc.argv[1].equalsIgnoreCase("captainlevel")) {
        shipType.setCaptainSkillLevel(Integer.parseInt(sc.argv[0]));
        sc.getNextToken();
      } else if((sc.argc == 2) && sc.argv[1].equalsIgnoreCase("sailorlevel")) {
        shipType.setSailorSkillLevel(Integer.parseInt(sc.argv[0]));
        sc.getNextToken();
      } else {
        unknown("SHIPTYPE", true);
      }
    }
  }

  private void parseBuildingType(Rules rules) throws IOException {
    BuildingType bType = null;
    int f = sc.argv[0].indexOf("\"", 0);
    int t = sc.argv[0].indexOf("\"", f + 1);
    String id = sc.argv[0].substring(f + 1, t);
    String blockName = sc.argv[0].substring(0, sc.argv[0].indexOf(" "));

    if(blockName.equals("BUILDINGTYPE")) {
      bType = rules.getBuildingType(StringID.create(id), true);
    } else if(blockName.equals("CASTLETYPE")) {
      bType = rules.getCastleType(StringID.create(id), true);
    }

    sc.getNextToken(); // skip GEBÄUDETYP xx

    while(!sc.eof) {
      if((sc.argc == 2) && sc.argv[1].equalsIgnoreCase("name")) {
        bType.setName(sc.argv[0]);
        sc.getNextToken();
      } else if((sc.argc == 2) && sc.argv[1].equalsIgnoreCase("level")) {
        bType.setMinSkillLevel(Integer.parseInt(sc.argv[0]));
        sc.getNextToken();
      } else if((sc.argc == 2) && sc.argv[1].equalsIgnoreCase("maxsize")) {
        bType.setMaxSize(Integer.parseInt(sc.argv[0]));
        sc.getNextToken();
      } else if((sc.argc == 2) && sc.argv[1].equalsIgnoreCase("minsize")) {
        if(bType instanceof CastleType) {
          ((CastleType) bType).setMinSize(Integer.parseInt(sc.argv[0]));
        }

        sc.getNextToken();
      } else if((sc.argc == 2) && sc.argv[1].equalsIgnoreCase("wage")) {
        if(bType instanceof CastleType) {
          ((CastleType) bType).setPeasantWage(Integer.parseInt(sc.argv[0]));
        }

        sc.getNextToken();
      } else if((sc.argc == 2) && sc.argv[1].equalsIgnoreCase("tradetax")) {
        if(bType instanceof CastleType) {
          ((CastleType) bType).setTradeTax(Integer.parseInt(sc.argv[0]));
        }

        sc.getNextToken();
      } else if(sc.isBlock && sc.argv[0].equals("TALENTBONI")) {
        parseBuildingSkillBonuses(bType, rules);
      } else if(sc.isBlock && sc.argv[0].equals("RAWMATERIALS")) {
        parseBuildingRawMaterials(bType, rules);
      } else if(sc.isBlock && sc.argv[0].equals("MAINTENANCE")) {
        parseBuildingMaintenance(bType, rules);
      } else if(sc.isBlock && sc.argv[0].equals("REGIONTYPES")) {
        parseBuildingTerrain(bType, rules);
      } else if(sc.isBlock) {
        break;
      } else {
        unknown(blockName, true);
      }
    }
  }

  private void parseBuildingSkillBonuses(BuildingType bType, Rules rules)
                  throws IOException
  {
    sc.getNextToken(); // skip TALENTBONI

    while(!sc.eof && !sc.isBlock) {
      try {
        SkillType skillType = rules.getSkillType(StringID.create(sc.argv[1]), true);
        bType.setSkillBonus(skillType, Integer.parseInt(sc.argv[0]));
      } catch(NumberFormatException e) {
        log.warn("CRParser.parseBuildingSkillBonuses(): in line " + sc.lnr +
             ": unable to convert skill bonus " + sc.argv[0] +
             " to an integer. Ignoring bonus for skill " + sc.argv[1]);
      }

      sc.getNextToken();
    }
  }

  private void parseBuildingRawMaterials(BuildingType bType, Rules rules)
                  throws IOException
  {
    sc.getNextToken(); // skip RAWMATERIALS

    while(!sc.eof && !sc.isBlock) {
      try {
        ItemType itemType = rules.getItemType(StringID.create(sc.argv[1]), true);
        Item i = new Item(itemType, Integer.parseInt(sc.argv[0]));
        bType.addRawMaterial(i);
      } catch(NumberFormatException e) {
        log.warn("CRParser.parseBuildingRawMaterials(): in line " + sc.lnr +
             ": unable to convert item amount " + sc.argv[0] +
             " to an integer. Ignoring amount for item " + sc.argv[1]);
      }

      sc.getNextToken();
    }
  }

  private void parseBuildingMaintenance(BuildingType bType, Rules rules)
                   throws IOException
  {
    sc.getNextToken(); // skip MAINTENANCE

    while(!sc.eof && !sc.isBlock) {
      try {
        ItemType itemType = rules.getItemType(StringID.create(sc.argv[1]), true);
        Item i = new Item(itemType, Integer.parseInt(sc.argv[0]));
        bType.addMaintenance(i);
      } catch(NumberFormatException e) {
        log.warn("CRParser.parseBuildingMaintenance(): in line " + sc.lnr +
             ": unable to convert item amount " + sc.argv[0] +
             " to an integer. Ignoring amount for item " + sc.argv[1]);
      }

      sc.getNextToken();
    }
  }

  private void parseBuildingTerrain(BuildingType bType, Rules rules) throws IOException {
    sc.getNextToken(); // skip MAINTENANCE

    while(!sc.eof && !sc.isBlock) {
      RegionType t = rules.getRegionType(StringID.create(sc.argv[0]), true);
      bType.addRegionType(t);
      sc.getNextToken();
    }
  }

  private void parseRegionType(Rules rules) throws IOException {
    int f = sc.argv[0].indexOf("\"", 0);
    int t = sc.argv[0].indexOf("\"", f + 1);
    ID id = StringID.create(sc.argv[0].substring(f + 1, t));
    RegionType regionType = rules.getRegionType(id, true);
    sc.getNextToken(); // skip REGIONSTYP xx

    while(!sc.eof && !sc.isBlock) {
      if((sc.argc == 2) && sc.argv[1].equalsIgnoreCase("maxworkers")) {
        regionType.setInhabitants(Integer.parseInt(sc.argv[0]));
        sc.getNextToken();
      } else if((sc.argc == 2) && sc.argv[1].equalsIgnoreCase("name")) {
        regionType.setName(sc.argv[0]);
        sc.getNextToken();
      } else if((sc.argc == 2) && sc.argv[1].equalsIgnoreCase("roadstones")) {
        Resource resource = new Resource(Integer.parseInt(sc.argv[0]));
        resource.setObjectType(rules.getItemType(StringID.create("Stein"), true));
        regionType.addRoadResource(resource);
        sc.getNextToken();
      } else if((sc.argc == 2) && sc.argv[1].equalsIgnoreCase("roadsupportbuilding")) {
        Resource resource = new Resource();
        resource.setObjectType(rules.getBuildingType(StringID.create(sc.argv[0]), true));
        regionType.addRoadResource(resource);
        sc.getNextToken();
      } else if((sc.argc == 2) && sc.argv[1].equalsIgnoreCase("isOcean")) {
        regionType.setIsOcean(sc.argv[0].equals("true"));
        sc.getNextToken();
      } else if((sc.argc == 2) && sc.argv[1].equalsIgnoreCase("isAstralVisible")) {
        regionType.setAstralVisible(sc.argv[0].equals("true"));
        sc.getNextToken();
      } else if(sc.argc == 2) {
        unknown("REGIONTYPE", true);
      } else {
        unknown("GEBÄUDETYP", true);
      }
    }
  }

  private void parseItemCategory(Rules rules) throws IOException {
    int f = sc.argv[0].indexOf("\"", 0);
    int t = sc.argv[0].indexOf("\"", f + 1);
    ID id = StringID.create(sc.argv[0].substring(f + 1, t));
    ItemCategory cat = rules.getItemCategory(id, true);

    sc.getNextToken(); // skip ITEMCATEGORY xx

    while(!sc.eof && !sc.isBlock) {
      if((sc.argc == 2) && sc.argv[1].equalsIgnoreCase("name")) {
        cat.setName(sc.argv[0]);
        sc.getNextToken();
      } else if((sc.argc == 2) && sc.argv[1].equalsIgnoreCase("naturalorder")) {
        cat.setSortIndex(Integer.parseInt(sc.argv[0]));
        sc.getNextToken();
      } else if((sc.argc == 2) && sc.argv[1].equalsIgnoreCase("parent")) {
        ItemCategory parent = rules.getItemCategory(StringID.create(sc.argv[0]), false);
        cat.setParent(parent);
        sc.getNextToken();
      } else if((sc.argc == 2) && sc.argv[1].equalsIgnoreCase("iconname")) {
        cat.setIconName(sc.argv[0]);
        sc.getNextToken();
      } else if(sc.argc == 2) {
        unknown("ITEMCATEGORY", false);
      } else {
        // FIXME (stm) really break on unkown tag?
        break;
      }
    }
  }

  private void parseSkillCategory(Rules rules) throws IOException {
    int f = sc.argv[0].indexOf("\"", 0);
    int t = sc.argv[0].indexOf("\"", f + 1);
    ID id = StringID.create(sc.argv[0].substring(f + 1, t));
    SkillCategory cat = rules.getSkillCategory(id, true);

    sc.getNextToken(); // skip SKILLCATEGORY xx

    while(!sc.eof) {
      if((sc.argc == 2) && sc.argv[1].equalsIgnoreCase("name")) {
        cat.setName(sc.argv[0]);
      } else if((sc.argc == 2) && sc.argv[1].equalsIgnoreCase("naturalorder")) {
        cat.setSortIndex(Integer.parseInt(sc.argv[0]));
      } else if((sc.argc == 2) && sc.argv[1].equalsIgnoreCase("parent")) {
        SkillCategory parent = rules.getSkillCategory(StringID.create(sc.argv[0]), false);
        cat.setParent(parent);
      } else if(sc.argc == 2) {
        unknown("SKILLCATEGORY", false);
      } else {
        // FIXME (stm) really break on unkown tag?
        break;
      }

      sc.getNextToken();
    }
  }

  /**
   * DOCUMENT-ME
   *
   * 
   *
   * 
   *
   * @throws IOException DOCUMENT-ME
   */
  public Rules readRules(FileType filetype) throws IOException {
    return readRules(filetype.createReader());
  }

  /**
   * DOCUMENT ME!
   *
   * @param in The reader that will read the file for us.
   *
   * @return a ruleset object, or null, if the file hasn't been a ruleset.
   *
   * @throws IOException DOCUMENT-ME
   */
  private synchronized Rules readRules(Reader in) throws IOException {
    Rules rules = new GenericRules();
    sc = new Scanner(in);
    sc.getNextToken();

    if(!sc.argv[0].startsWith("VERSION ") || (sc.argc != 1)) {
      log.warn("CRParser.readRules(): corrupt rule file missing VERSION on first line.");

      return null;
    }

    if(!sc.eof) {
      sc.getNextToken();

      if(!sc.argv[0].startsWith("RULES ") || (sc.argc != 1)) {
        log.warn("CRParser.readRules(): corrupt rule file missing RULE block.");

        return null;
      }
    }

    /* The desired header has been parsed. Continue parsing the sequent
     * rule blocks until the file ends. */
    parseRules(rules);

    return rules;
  }

  private void parseRules(Rules rules) throws IOException {
    sc.getNextToken(); // skip "RULES"

    while(!sc.eof) {
      if(sc.argv[0].startsWith("MAGELLAN")) {
        parseMagellan(rules);
      } else if(sc.argv[0].startsWith("RACE ")) {
        parseRace(rules);
      } else if(sc.argv[0].startsWith("ITEM ") || sc.argv[0].startsWith("HERB ")) {
        parseItemType(rules);
      } else if(sc.argv[0].startsWith("SHIPTYPE ")) {
        parseShipType(rules);
      } else if(sc.argv[0].startsWith("BUILDINGTYPE ") ||
              sc.argv[0].startsWith("CASTLETYPE ")) {
        parseBuildingType(rules);
      } else if(sc.argv[0].startsWith("REGIONTYPE ")) {
        parseRegionType(rules);
      } else if(sc.argv[0].startsWith("SKILL ")) {
        parseSkillType(rules);
      } else if(sc.argv[0].startsWith("ITEMCATEGORY ")) {
        parseItemCategory(rules);
      } else if(sc.argv[0].startsWith("SKILLCATEGORY ")) {
        parseSkillCategory(rules);
      } else if(sc.argv[0].startsWith("OPTIONCATEGORY ")) {
        parseOptionCategory(rules);
      } else if(sc.argv[0].startsWith("ALLIANCECATEGORY ")) {
        parseAllianceCategory(rules);
      } else {
        break;
      }
    }
  }

  private void parseCoordinateTransformation(GameData world) throws IOException {
    EntityID id = EntityID.createEntityID(Integer.parseInt(sc.argv[0].substring("COORDTRANS".length()+1)), world.base);
    sc.getNextToken();

    CoordinateID translation = null;

    while(!sc.eof && !sc.isBlock) {
      if((sc.argc == 2) && sc.argv[1].equalsIgnoreCase("translation")) {
        translation = CoordinateID.parse(sc.argv[0], " ");
        inverseOriginTranslate(translation);
        world.setCoordinateTranslation(id, translation);
        sc.getNextToken();
      } else {
        unknown("COORDTRANS", true); 
      }
    }
  }


  private void parseOptionCategory(Rules rules) throws IOException {
    int f = sc.argv[0].indexOf("\"", 0);
    int t = sc.argv[0].indexOf("\"", f + 1);
    ID id = StringID.create(sc.argv[0].substring(f + 1, t));
    OptionCategory opt = rules.getOptionCategory(id, true);
    sc.getNextToken(); // skip OPTIONCATEGORY xx

    while(!sc.eof) {
      if((sc.argc == 2) && sc.argv[1].equalsIgnoreCase("name")) {
        opt.setName(sc.argv[0]);
        // sc.getNextToken();
      } else if((sc.argc == 2) && sc.argv[1].equalsIgnoreCase("order")) {
        opt.setOrder(sc.argv[0].equals("true"));
        // sc.getNextToken();
      } else if((sc.argc == 2) && sc.argv[1].equalsIgnoreCase("bitmask")) {
        // alt (Fiete)
        // opt.setBitMask(1 << Integer.parseInt(sc.argv[0]));
        // neu (Fiete)
        opt.setBitMask(Integer.parseInt(sc.argv[0]));
        // sc.getNextToken();
      } else if(sc.argc == 2) {
        unknown("OPTIONCATEGORY", true);
      } else {
        // FIXME (stm) really break on unknown token?
        break;
      }

      sc.getNextToken();
    }
  }

  private void parseAllianceCategory(Rules rules) throws IOException {
    int f = sc.argv[0].indexOf("\"", 0);
    int t = sc.argv[0].indexOf("\"", f + 1);
    ID id = StringID.create(sc.argv[0].substring(f + 1, t));
    AllianceCategory cat = rules.getAllianceCategory(id, true);
    sc.getNextToken(); // skip ALLIANCECATEGORY xx

    while(!sc.eof) {
      if((sc.argc == 2) && sc.argv[1].equalsIgnoreCase("name")) {
        cat.setName(sc.argv[0]);
      } else if((sc.argc == 2) && sc.argv[1].equalsIgnoreCase("parent")) {
        AllianceCategory parent = rules.getAllianceCategory(StringID.create(sc.argv[0]), false);
        cat.setParent(parent);
      } else if((sc.argc == 2) && sc.argv[1].equalsIgnoreCase("bitmask")) {
        cat.setBitMask(Integer.parseInt(sc.argv[0]));
      } else if(sc.argc == 2) {
        unknown("ALLIANCECATEGORY", true);
      } else {
        // FIXME (stm) really break on unk
        break;
      }

      sc.getNextToken();      
    }
  }

  /*
   * This is the new version, the old is called "ALLIERTE"
   * Heuristic for end of block detection: There are no
   * subblocks in one ALLIANZ block.
   */
  private Map<ID,Alliance> parseAlliance(Map<ID,Alliance> allies) throws IOException {
    if(allies == null) {
      allies = new OrderedHashtable<ID, Alliance>();
    }

    EntityID id = EntityID.createEntityID(Integer.parseInt(sc.argv[0].substring(8)), world.base);
    sc.getNextToken();

    int state = -1;

    while(!sc.eof && !sc.isBlock) {
      if((sc.argc == 2) && sc.argv[1].equalsIgnoreCase("Parteiname")) {
        sc.getNextToken();
      } else if((sc.argc == 2) && sc.argv[1].equalsIgnoreCase("Status")) {
        state = Integer.parseInt(sc.argv[0]);
        sc.getNextToken();
      } else {
        unknown("ALLIANZ", true); // loop within one ALLIANZ
        // FIXME (stm) really break on unk
        break;
      }
    }

    if(state != -1) {
      Faction faction = getAddFaction(world, id);
      Alliance alliance = new Alliance(faction, state);
      allies.put(faction.getID(), alliance);
    }

    return allies;
  }

  /**
   * NOT IMPLEMENTED YET
   * 
   * This is the old "ALLIERTE" version.
   * Heuristic for block termination:
   *  - Terminate on any other block
   *** This method isn't implemented yet. It skips the entire
   *** "ALLIERTE" block.
   */
  private Map<ID,Alliance> parseAlliierte() throws IOException {
    Map<ID,Alliance> allies = new Hashtable<ID, Alliance>();
    sc.getNextToken(); //skip "ALLIERTE" tag

    while(!sc.eof && !sc.isBlock) {
      sc.getNextToken();
    }

    return allies;
  }

  /**
   * This is the old "ADRESSEN" version. Heuristic for block termination: - Terminate on any
   * other block This method isn't implemented yet. It skips the entire "ADRESSEN" block.
   *
   * @throws IOException DOCUMENT-ME
   */
  private void parseAdressen() throws IOException {
    sc.getNextToken(); //skip "ADRESSEN" tag

    while(!sc.eof && !sc.isBlock) {
      sc.getNextToken();
    }
  }

  /*
   * Parse the FACTION block with all its subblocks.
   * Heuristic for block termination:
   *  - Terminate on another PARTEI block (without warning)
   *  - Terminate on another id block (without warning)
   *  - Terminate on any other unknown block (with warning)
   */
  private Faction parseFaction(GameData world, int sortIndex) throws IOException {
    Race type = null;
    int raceRecruit = -1;
    int groupSortIndex = 0;
    EntityID id = EntityID.createEntityID(Integer.parseInt(sc.argv[0].substring(7)), world.base);
    sc.getNextToken(); // skip PARTEI nr

    Faction faction = getAddFaction(world, id);
    faction.setSortIndex(sortIndex);

    if (firstFaction==null)
      firstFaction = faction;
    
    while(!sc.eof && !sc.argv[0].startsWith("PARTEI ")) {
      if((sc.argc == 2) && sc.argv[1].equalsIgnoreCase("Runde")) {
        magellan.library.rules.Date d = world.getDate();

        if(d == null) {
          world.setDate(new EresseaDate(Integer.parseInt(sc.argv[0])));
        } else {
          d.setDate(Integer.parseInt(sc.argv[0]));
        }

        sc.getNextToken();
      } else if((sc.argc == 2) && sc.argv[1].equalsIgnoreCase("Passwort")) {
        //faction.password = sc.argv[0];
        sc.getNextToken();
      } else if((sc.argc == 2) && sc.argv[1].equalsIgnoreCase("Optionen")) {
        if(faction.getOptions() == null) {
          faction.setOptions(new Options(world.rules));
        }

        faction.getOptions().setValues(Integer.parseInt(sc.argv[0]));
        sc.getNextToken();
      } else if((sc.argc == 2) && sc.argv[1].equalsIgnoreCase("Punkte")) {
        faction.setScore(Integer.parseInt(sc.argv[0]));
        sc.getNextToken();
      } else if((sc.argc == 2) && sc.argv[1].equalsIgnoreCase("Punktedurchschnitt")) {
        faction.setAverageScore(Integer.parseInt(sc.argv[0]));
        sc.getNextToken();
      } else if((sc.argc == 2) && sc.argv[1].equalsIgnoreCase("email")) {
        faction.setEmail(sc.argv[0]);
        sc.getNextToken();
      } else if((sc.argc == 2) && sc.argv[1].equalsIgnoreCase("banner")) {
        faction.setDescription(sc.argv[0]);
        sc.getNextToken();
      } else if((sc.argc == 2) &&
              (sc.argv[1].equalsIgnoreCase("Typ") ||
              sc.argv[1].equalsIgnoreCase("Typus") ||
              sc.argv[1].equalsIgnoreCase("race"))) {
        type = world.rules.getRace(StringID.create(sc.argv[0]), true);
        faction.setType(type);
        sc.getNextToken();
      } else if((sc.argc == 2) && sc.argv[1].equalsIgnoreCase("Rekrutierungskosten")) {
        raceRecruit = Integer.parseInt(sc.argv[0]);
        sc.getNextToken();
      } else if((sc.argc == 2) && sc.argv[1].equalsIgnoreCase("Anzahl Personen")) {
        faction.setPersons(Integer.parseInt(sc.argv[0]));
        sc.getNextToken();
      } else if((sc.argc == 2) && sc.argv[1].equalsIgnoreCase("Anzahl Immigranten")) {
        faction.setMigrants(Integer.parseInt(sc.argv[0]));
        sc.getNextToken();
      } else if((sc.argc == 2) && sc.argv[1].equalsIgnoreCase("Max. Immigranten")) {
        faction.setMaxMigrants(Integer.parseInt(sc.argv[0]));
        sc.getNextToken();
      } else if((sc.argc == 2) && sc.argv[1].equalsIgnoreCase("heroes")) {
        faction.setHeroes(Integer.parseInt(sc.argv[0]));
        sc.getNextToken();
      } else if((sc.argc == 2) && sc.argv[1].equalsIgnoreCase("max_heroes")) {
        faction.setMaxHeroes(Integer.parseInt(sc.argv[0]));
        sc.getNextToken();
      } else if((sc.argc == 2) && sc.argv[1].equalsIgnoreCase("age")) {
        faction.setAge(Integer.parseInt(sc.argv[0]));
        sc.getNextToken();
      } else if((sc.argc == 2) && sc.argv[1].equalsIgnoreCase("Parteiname")) {
        faction.setName(sc.argv[0]);
        sc.getNextToken();
      } else if((sc.argc == 2) && sc.argv[1].equalsIgnoreCase("Magiegebiet")) {
        faction.setSpellSchool(sc.argv[0]);
        sc.getNextToken();
      } else if((sc.argc == 2) && sc.argv[1].equalsIgnoreCase("trustlevel")) {
        faction.setTrustLevel(Integer.parseInt(sc.argv[0]));
        faction.setTrustLevelSetByUser(true);
        sc.getNextToken();
      } else if((sc.argc == 2) && sc.argv[1].equalsIgnoreCase("locale")) {
        faction.setLocale(new Locale(sc.argv[0], ""));
        sc.getNextToken();
      } else if((sc.argc == 2) && sc.argv[1].equalsIgnoreCase("typprefix")) {
        faction.setRaceNamePrefix(sc.argv[0]);
        sc.getNextToken();
      } else if((sc.argc == 2) && sc.argv[1].equalsIgnoreCase("ZAT")) {
        /* Verdanon tag */
        sc.getNextToken();
      } else if((sc.argc == 1) &&
              (sc.argv[0].equals("EREIGNISSE") || sc.argv[0].equals("EINKOMMEN") ||
              sc.argv[0].equals("HANDEL") || sc.argv[0].equals("PRODUKTION") ||
              sc.argv[0].equals("BEWEGUNGEN") || sc.argv[0].equals("MELDUNGEN"))) {
        faction.setMessages(parseMessageSequence(faction.getMessages()));
      } else if((sc.argc == 1) && sc.argv[0].startsWith("MESSAGE ")) {
        faction.setMessages(parseMessages(world, faction.getMessages()));
      } else if((sc.argc == 1) && sc.argv[0].startsWith("BATTLE ")) {
        faction.setBattles(parseBattles(world, faction.getBattles()));
      } else if((sc.argc == 1) && sc.argv[0].startsWith("BATTLESPEC ")) {
        faction.setBattles(parseBattleSpecs(world, faction.getBattles()));
      } else if((sc.argc == 1) && sc.argv[0].startsWith("KAEMPFE")) {
        sc.getNextToken(); // skip KAEMPFE

        // Skip the whole block (old syntax, no longer supported)
        while(!sc.eof && !sc.isBlock) {
          sc.getNextToken();
        }
      } else if((sc.argc == 1) && sc.argv[0].equals("ZAUBER")) { // old syntax, ignore
        parseMessageSequence(null);
      } else if((sc.argc == 1) && sc.argv[0].startsWith("TRAENKE")) { // old syntax, ignore
        parseMessageSequence(null);
      } else if((sc.argc == 1) && sc.argv[0].startsWith("ALLIIERTE")) {
        faction.setAllies(parseAlliierte()); // old syntax
      } else if(sc.isBlock && sc.argv[0].startsWith("ALLIANZ ")) {
        faction.setAllies(parseAlliance(faction.getAllies())); // newer syntax
      } else if(sc.isBlock && sc.argv[0].equals("ADRESSEN")) {
        parseAdressen();
      } else if(sc.isBlock && sc.argv[0].equals("GEGENSTAENDE")) {
        // FIXME: This only prevents the bug but the faction item pool will be lost!
        parseItems(faction);
      } else if(sc.isBlock && sc.argv[0].equals("OPTIONEN")) {
        // ignore this block, if there are options, they are
        // encoded as a bit field whereas these string
        // representation is not fixed and eventually leads
        // to trouble
        parseOptions(null);
      } else if(sc.isBlock && sc.argv[0].startsWith("GRUPPE ")) {
        faction.setGroups(parseGroup(faction.getGroups(), faction, groupSortIndex++));
      } else if((sc.argc == 1) && sc.argv[0].equals("COMMENTS")) {
        faction.setComments(parseStringSequence(faction.getComments()));
      } else if((sc.argc == 1) && sc.argv[0].equals("FEHLER")) {
        faction.setErrors(parseStringSequence(faction.getErrors()));
      } else if((sc.argc == 1) && sc.argv[0].equals("WARNUNGEN")) {
        /* Verdanon messages */
        faction.setMessages(parseMessageSequence(faction.getMessages()));
      } else if(sc.isBlock) {
        if(!sc.isIdBlock) {
          unknown("PARTEI", false);
        }
        // FIXME (stm) really break?

        break;
      } else {
        unknown("PARTEI", true);
      }
    }

    if((type != null) && (raceRecruit != -1)) {
      type.setRecruitmentCosts(raceRecruit);
    }

    return faction;
  }

  private Options parseOptions(Options options) throws IOException {
    sc.getNextToken(); // skip OPTIONEN

    while(!sc.eof && !sc.isBlock) {
      if(sc.argc == 2) {
        //options.setActive(StringID.create(sc.argv[1]), Integer.parseInt(sc.argv[0]) != 0);
        sc.getNextToken();
      } else {
        unknown("OPTIONEN", true);
      }
    }

    return options;
  }

  private Map<ID,Group> parseGroup(Map<ID,Group> groups, Faction faction, int sortIndex) throws IOException {
    ID id = IntegerID.create(sc.argv[0].substring(7));
    Group g = null;

    if(groups == null) {
      groups = new OrderedHashtable<ID, Group>();
    }

    g = (Group) groups.get(id);

    if(g == null) {
      g = MagellanFactory.createGroup(id, world);
    }

    g.setFaction(faction);
    g.setSortIndex(sortIndex);
    groups.put(id, g);
    sc.getNextToken(); // skip GRUPPE nr

    while(!sc.eof) {
      if((sc.argc == 2) && sc.argv[1].equalsIgnoreCase("name")) {
        g.setName(sc.argv[0]);
        sc.getNextToken();
      } else if((sc.argc == 2) && sc.argv[1].equalsIgnoreCase("typprefix")) {
        g.setRaceNamePrefix(sc.argv[0]);
        sc.getNextToken();
      } else if(sc.isBlock && sc.argv[0].startsWith("ALLIANZ ")) {
        parseAlliance(g.allies());
      } else if(sc.isBlock) {
        break;
      } else {
        unknown("GRUPPE", true);
        // FIXME (stm) really break on unknown token?

        break;
      }
    }

    return groups;
  }

  /**
   * Accesses unit.persons which must be > 0 and just adds new skills, existing skills are not
   * deleted.
   *
   * 
   * 
   *
   * @throws IOException DOCUMENT-ME
   */
  private void parseSkills(Unit unit, GameData world) throws IOException {
    sc.getNextToken(); // skip TALENTE

    if(unit == null) {
      invalidParam("parseSkills", "unit is null");

      return;
    }

    if(unit.getPersons() <= 0) {
      invalidParam("parseSkills", "unit.persons <= 0");

      return;
    }

    if(world == null) {
      invalidParam("parseSkills", "world is null");

      return;
    }

    if(world.rules == null) {
      invalidParam("parseSkills", "rules is null");

      return;
    }

    while(!sc.eof && (sc.argc == 2)) {
      int points = 0;
      int level = 0;
      int change = 0;
      boolean changed = false;

      int s = sc.argv[0].indexOf(' ', 0);
      int s2 = sc.argv[0].indexOf(' ', s + 1);

      if(s > -1) {
        points = Integer.parseInt(sc.argv[0].substring(0, s));

        if(s2 > -1) {
          level = Integer.parseInt(sc.argv[0].substring(s + 1, s2));
          change = Integer.parseInt(sc.argv[0].substring(s2 + 1));
          changed = true;
        } else {
          level = Integer.parseInt(sc.argv[0].substring(s + 1));
        }
      } else {
        level = Integer.parseInt(sc.argv[0]);
      }

      Skill skill = new Skill(world.rules.getSkillType(StringID.create(sc.argv[1]), true),
                  points, level, unit.getPersons(), world.noSkillPoints);
      skill.setChangeLevel(change);
      skill.setLevelChanged(changed);
      unit.addSkill(skill);
      sc.getNextToken();
    }
  }

  /*
   * Syntax: "count;item"
   * Example: "1;Steine"
   * Does not delete existing items.
   */
  private void parseItems(Unit unit) throws IOException {
    /*
    if(unit == null) {
      invalidParam("parseItems", "unit is null");

      return;
      }
    */

    sc.getNextToken(); // skip GEGENSTAENDE

    while(!sc.eof && (sc.argc == 2)) {
      Item item = new Item(world.rules.getItemType(StringID.create(sc.argv[1]), true),
                 Integer.parseInt(sc.argv[0]));
      if(unit != null) { unit.addItem(item); }
      sc.getNextToken();
    }
  }

  /*
   * Syntax: "count;item"
   * Example: "1;Steine"
   * Does not delete existing items.
   */
  private void parseItems(UnitContainer unitcontainer) throws IOException {

    sc.getNextToken(); // skip GEGENSTAENDE

    while(!sc.eof && (sc.argc == 2)) {
      Item item = new Item(world.rules.getItemType(StringID.create(sc.argv[1]), true),
                 Integer.parseInt(sc.argv[0]));
      if(unitcontainer != null) { unitcontainer.addItem(item); }
      sc.getNextToken();
    }
  }



  private int parseUnit(GameData world, Region region, int sortIndex) throws IOException {
    Unit unit = getAddUnit(world, UnitID.createUnitID(Integer.parseInt(sc.argv[0].substring(8)), world.base));
    EntityID factionID = EntityID.createEntityID(-1,world.base);
    ID groupID = null;

    if(region != unit.getRegion()) {
      unit.setRegion(region);
    }

    // if there is a unit in the region, this means we have
    // infos about it:
    if(region != null) {
      region.setTrees(Math.max(region.getTrees(), 0));
      region.setPeasants(Math.max(region.getPeasants(), 0));
      region.setHorses(Math.max(region.getHorses(), 0));
      // Fiete 20061217 this was double...
      // region.trees = Math.max(region.trees, 0);
    }

    sc.getNextToken(); // skip "EINHEIT nr"

    while(!sc.eof) {
      if((sc.argc == 2) && sc.argv[1].equalsIgnoreCase("Name")) {
        unit.setName(sc.argv[0]);
        sc.getNextToken();
      } else if((sc.argc == 2) && sc.argv[1].equalsIgnoreCase("Beschr")) {
        unit.setDescription(sc.argv[0]);
        sc.getNextToken();
      } else if((sc.argc == 2) && sc.argv[1].equalsIgnoreCase("Typ")) {
        unit.setRace(world.rules.getRace(StringID.create(sc.argv[0]), true));
        sc.getNextToken();
      } else if((sc.argc == 2) && sc.argv[1].equalsIgnoreCase("wahrerTyp")) {
        unit.setRealRace(world.rules.getRace(StringID.create(sc.argv[0]), true));
        sc.getNextToken();
      } else if((sc.argc == 2) && sc.argv[1].equalsIgnoreCase("temp")) {
        unit.setTempID(UnitID.createUnitID(Integer.parseInt(sc.argv[0]), world.base));
        sc.getNextToken();
      } else if((sc.argc == 2) && sc.argv[1].equalsIgnoreCase("alias")) {
        unit.setAlias(UnitID.createUnitID(Integer.parseInt(sc.argv[0]), world.base));
        sc.getNextToken();
      } else if((sc.argc == 2) && sc.argv[1].equalsIgnoreCase("privat")) {
        unit.setPrivDesc(sc.argv[0]);
        sc.getNextToken();
      } else if((sc.argc == 2) && sc.argv[1].equalsIgnoreCase("Anzahl")) {
        unit.setPersons(Integer.parseInt(sc.argv[0]));
        sc.getNextToken();
      } else if((sc.argc == 2) && sc.argv[1].equalsIgnoreCase("Partei")) {
        factionID = EntityID.createEntityID(Integer.parseInt(sc.argv[0]), world.base);
        sc.getNextToken();
      } else if((sc.argc == 2) && sc.argv[1].equalsIgnoreCase("Parteiname")) {
        sc.getNextToken();
      } else if((sc.argc == 2) && sc.argv[1].equalsIgnoreCase("Parteitarnung")) {
        if(Integer.parseInt(sc.argv[0]) != 0) {
          unit.setHideFaction(true);
        } else {
          unit.setHideFaction(false);
        }

        sc.getNextToken();
      } else if((sc.argc == 2) && sc.argv[1].equalsIgnoreCase("bewacht")) {
        unit.setGuard(Integer.parseInt(sc.argv[0]));

        Region r = unit.getRegion();

        if(r != null) {
          r.addGuard(unit);
        }

        sc.getNextToken();
      } else if((sc.argc == 2) && sc.argv[1].equalsIgnoreCase("belagert")) {
        unit.setSiege(getAddBuilding(world, EntityID.createEntityID(Integer.parseInt(sc.argv[0]), world.base)));
        sc.getNextToken();
      } else if((sc.argc == 2) && sc.argv[1].equalsIgnoreCase("folgt")) {
        unit.setFollows(getAddUnit(world, UnitID.createUnitID(Integer.parseInt(sc.argv[0]), world.base)));
        sc.getNextToken();
      }  else if((sc.argc == 2) && sc.argv[1].equalsIgnoreCase("familiarmage")) {
        unit.setFamiliarmageID(UnitID.createUnitID(Integer.parseInt(sc.argv[0]), world.base));
        sc.getNextToken();
      } else if((sc.argc == 2) && sc.argv[1].equalsIgnoreCase("Silber")) {
        int money = Integer.parseInt(sc.argv[0]);
        Item item = new Item(world.rules.getItemType(StringID.create("Silber"), true), money);
        unit.addItem(item);
        sc.getNextToken();
      } else if((sc.argc == 2) && sc.argv[1].equalsIgnoreCase("Burg")) {
        Integer.parseInt(sc.argv[0]);

        Building b = getAddBuilding(world, EntityID.createEntityID(Integer.parseInt(sc.argv[0]), world.base));

        if(unit.getBuilding() != b) {
          unit.setBuilding(b);
        }

        sc.getNextToken();
      } else if((sc.argc == 2) && sc.argv[1].equalsIgnoreCase("Schiff")) {
        Ship s = getAddShip(world, EntityID.createEntityID(Integer.parseInt(sc.argv[0]), world.base));

        if(unit.getShip() != s) {
          unit.setShip(s);
        }

        sc.getNextToken();
      } else if((sc.argc == 2) && sc.argv[1].equalsIgnoreCase("Kampfstatus")) {
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
        if(version < 57) {
          unit.setCombatStatus(unit.getCombatStatus()+1);

          if(unit.getCombatStatus() > 2) {
            unit.setCombatStatus(unit.getCombatStatus()+1);
          }
        }

        sc.getNextToken();
      } else if((sc.argc == 2) && sc.argv[1].equalsIgnoreCase("unaided")) {
        unit.setUnaided((Integer.parseInt(sc.argv[0]) != 0));
        sc.getNextToken();
      } else if((sc.argc == 2) && sc.argv[1].equalsIgnoreCase("Tarnung")) {
        unit.setStealth(Integer.parseInt(sc.argv[0]));
        sc.getNextToken();
      } else if((sc.argc == 2) && sc.argv[1].equalsIgnoreCase("Aura")) {
        unit.setAura(Integer.parseInt(sc.argv[0]));
        ;
        sc.getNextToken();
      } else if((sc.argc == 2) && sc.argv[1].equalsIgnoreCase("Auramax")) {
        unit.setAuraMax(Integer.parseInt(sc.argv[0]));
        ;
        sc.getNextToken();
      } else if((sc.argc == 2) && sc.argv[1].equalsIgnoreCase("hp")) {
        unit.setHealth(sc.argv[0]);
        sc.getNextToken();
      } else if((sc.argc == 2) && sc.argv[1].equalsIgnoreCase("hunger")) {
        unit.setStarving((Integer.parseInt(sc.argv[0]) != 0));
        sc.getNextToken();
      } else if((sc.argc == 2) && sc.argv[1].equalsIgnoreCase("ejcOrdersConfirmed")) {
        unit.setOrdersConfirmed((Integer.parseInt(sc.argv[0]) != 0));
        sc.getNextToken();
      } else if((sc.argc == 2) && sc.argv[1].equalsIgnoreCase("gruppe")) {
        groupID = IntegerID.create(sc.argv[0]);
        sc.getNextToken();
      } else if((sc.argc == 2) && sc.argv[1].equalsIgnoreCase("verraeter")) {
        unit.setSpy(true);
        sc.getNextToken();

        /* currently, verkleidung was announced but it seems that
         anderepartei is used. Please remove one as soon as it
         is clear which one can be discarded */
      } else if((sc.argc == 2) &&
              (sc.argv[1].equalsIgnoreCase("verkleidung") ||
              sc.argv[1].equalsIgnoreCase("anderepartei"))) {
        ID fid = EntityID.createEntityID(Integer.parseInt(sc.argv[0]), world.base);

        /* currently (2004-02) the cr is inconsistent with nr. There may
         * be a situation where the corresponding faction of this tag
         * does not exist in the game data so add it automagically
         * (bugzilla bug 794). */
        Faction faction = world.getFaction(fid);

        if(faction == null) {
          faction = MagellanFactory.createFaction(fid, world);
        }

        unit.setGuiseFaction(faction);
        sc.getNextToken();
      } else if((sc.argc == 2) && sc.argv[1].equalsIgnoreCase("typprefix")) {
        unit.setRaceNamePrefix(sc.argv[0]);
        sc.getNextToken();
      } else if((sc.argc == 2) && sc.argv[1].equalsIgnoreCase("ladung")) {
        // Verdanon tag
        sc.getNextToken();
      } else if((sc.argc == 2) && sc.argv[1].equalsIgnoreCase("kapazitaet")) {
        // Verdanon tag
        sc.getNextToken();
       } else if((sc.argc == 2) && sc.argv[1].equalsIgnoreCase("hero")) {
         // new promotion level
        unit.setHero((Integer.parseInt(sc.argv[0]) != 0));
        sc.getNextToken();
       } else if((sc.argc == 2) && sc.argv[1].equalsIgnoreCase("weight")) {
         unit.setWeight(Integer.parseInt(sc.argv[0]));
         sc.getNextToken();
      } else if((sc.argc == 1) && sc.argv[0].equals("COMMANDS")) {
        // there can be only one order block for a unit, replace existing ones
        unit.setOrders(parseStringSequence(null), false);
      } else if((sc.argc == 1) && sc.argv[0].equals("TALENTE")) {
        // there can be only one skills block for a unit, replace existing ones
        unit.clearSkills();
        parseSkills(unit, world);
      } else if((sc.argc == 1) && sc.argv[0].equals("SPRUECHE")) {
        // there can be only one spells block for a unit, replace existing ones
        unit.setSpells(parseUnitSpells(world, null));
      } else if((sc.argc == 1) && sc.argv[0].equals("GEGENSTAENDE")) {
        /* in verdanon reports the silver can already be
         included in the items */
        parseItems(unit);
      } else if(sc.isBlock && sc.argv[0].equals("EINHEITSBOTSCHAFTEN")) {
        unit.setUnitMessages(parseMessageSequence(unit.getUnitMessages()));
      } else if((sc.argc == 1) && sc.argv[0].equals("COMMENTS")) {
        unit.setComments(parseStringSequence(unit.getComments()));
      } else if((sc.argc == 1) && sc.argv[0].equals("EFFECTS")) {
        unit.setEffects(parseStringSequence(unit.getEffects()));
      } else if((sc.argc == 1) && sc.argv[0].startsWith("KAMPFZAUBER ")) {
        parseUnitCombatSpells(world, unit);
      } else if(sc.isBlock) {
        break;
      } else {
        if(sc.argc == 2) {
          unit.putTag(sc.argv[1], sc.argv[0]);
        }
        // realy put unknown message out?
        // check for wellknown tags...ejcTaggable etc...
        boolean isUnknown = true;
        if(sc.argc == 2) {
          if (sc.argv[1].equalsIgnoreCase(TAGGABLE_STRING)){isUnknown=false;}
          if (sc.argv[1].equalsIgnoreCase(TAGGABLE_STRING2)){isUnknown=false;}
          if (sc.argv[1].equalsIgnoreCase(TAGGABLE_STRING3)){isUnknown=false;}
          if (sc.argv[1].equalsIgnoreCase(TAGGABLE_STRING4)){isUnknown=false;}
          if (sc.argv[1].equalsIgnoreCase(TAGGABLE_STRING5)){isUnknown=false;}
        }
        if (isUnknown){
          unknown("EINHEIT", true);
        } else {
          sc.getNextToken();
        }
      }
    }

    // set the sortIndex so the original ordering of the units
    // can be restored
    unit.setSortIndex(sortIndex++);

    Faction faction = getAddFaction(world, factionID);

    if(faction.getName() == null) {
      if(factionID.intValue() == -1) {
        faction.setName("Parteigetarnte");
      } else if(factionID.intValue() == 0) {
        faction.setName("Monster");
      } else {
        faction.setName("Partei " + factionID);
      }
    }

    if(unit.getFaction() != faction) {
      unit.setFaction(faction);
    }

    if(groupID != null) {
      Group g = null;

      if((faction.getGroups() != null) && ((g = faction.getGroups().get(groupID)) != null)) {
        unit.setGroup(g);
      } else {
        log.warn("CRParser.parseUnit(): Unable to assign group " + groupID + " to unit " +
             unit.getID());
      }
    }

    /* a missing combat status can have two meanings:
     1. this is a unit we know everything about and the combat
     status is AGGRESSIVE
     2. this is a unit we just see but does not belong to us so we
     do not know its combat status.
     */
    if(!unit.ordersAreNull() && (unit.getCombatStatus() < 0)) {
      unit.setCombatStatus(0);
    }

    return sortIndex;
  }

  /*
   * Syntax: value;item
   * Example: 24;Balsam
   * < 0: offered in this region
   * > 0: demanded in this region
   */
  private Map<ID,LuxuryPrice> parsePrices(Map<ID,LuxuryPrice> prices) throws IOException {
    sc.getNextToken(); // skip PREISE

    while(!sc.eof && (sc.argc == 2)) {
      ItemType itemType = world.rules.getItemType(StringID.create(sc.argv[1]), true);
      LuxuryPrice pr = new LuxuryPrice(itemType, Integer.parseInt(sc.argv[0]));

      if(prices == null) {
        prices = new OrderedHashtable<ID, LuxuryPrice>();
      }

      prices.put(itemType.getID(), pr);
      sc.getNextToken();
    }

    return prices;
  }

  private void parseShip(GameData world, Region region, int sortIndex) throws IOException {
    EntityID id = EntityID.createEntityID(Integer.parseInt(sc.argv[0].substring(7)), world.base);
    sc.getNextToken(); // skip "SCHIFF nr"

    Ship ship = getAddShip(world, id);

    if(ship.getRegion() != region) {
      ship.setRegion(region);
    }

    ship.setSortIndex(sortIndex);

    while(!sc.eof) {
      if((sc.argc == 2) && sc.argv[1].equalsIgnoreCase("Name")) {
        ship.setName(sc.argv[0]);
        sc.getNextToken();
      } else if((sc.argc == 2) && sc.argv[1].equalsIgnoreCase("Typ")) {
        ShipType type = world.rules.getShipType(StringID.create(sc.argv[0]), true);
        ship.setType(type);
        sc.getNextToken();
      } else if((sc.argc == 2) && sc.argv[1].equalsIgnoreCase("Beschr")) {
        ship.setDescription(sc.argv[0]);
        sc.getNextToken();
      } else if((sc.argc == 2) && sc.argv[1].equalsIgnoreCase("Partei")) {
        if((ship.getOwnerUnit() != null) && (ship.getOwnerUnit().getFaction() == null)) {
          Faction f = world.getFaction(EntityID.createEntityID(Integer.parseInt(sc.argv[0]), world.base));
          ship.getOwnerUnit().setFaction(f);
        }

        sc.getNextToken();
      } else if((sc.argc == 2) && sc.argv[1].equalsIgnoreCase("Kapitaen")) {
        ship.setOwnerUnit(getAddUnit(world, UnitID.createUnitID(Integer.parseInt(sc.argv[0]), world.base)));
        sc.getNextToken();
      } else if((sc.argc == 2) && sc.argv[1].equalsIgnoreCase("Kueste")) {
        ship.setShoreId(Integer.parseInt(sc.argv[0]));
        sc.getNextToken();
      } else if((sc.argc == 2) && sc.argv[1].equalsIgnoreCase("Groesse")) {
        ship.setSize(Integer.parseInt(sc.argv[0]));
        sc.getNextToken();
      } else if((sc.argc == 2) && sc.argv[1].equalsIgnoreCase("Prozent") &&
              (ship.getType() != null)) {
        ship.setSize((ship.getShipType().getMaxSize() * Integer.parseInt(sc.argv[0])) / 100);
        sc.getNextToken();
      } else if((sc.argc == 2) && sc.argv[1].equalsIgnoreCase("Schaden")) {
        ship.setDamageRatio(Integer.parseInt(sc.argv[0]));
        sc.getNextToken();
      } else if((sc.argc == 2) && sc.argv[1].equalsIgnoreCase("cargo")) {
        ship.setCargo(Integer.parseInt(sc.argv[0]));
        sc.getNextToken();
      } else if((sc.argc == 2) && sc.argv[1].equalsIgnoreCase("capacity")) {
        ship.setCapacity(Integer.parseInt(sc.argv[0]));
        sc.getNextToken();
      } else if((sc.argc == 2) && sc.argv[1].equalsIgnoreCase("Ladung")) {
        ship.setDeprecatedLoad(Integer.parseInt(sc.argv[0]));
        sc.getNextToken();
      } else if((sc.argc == 2) && sc.argv[1].equalsIgnoreCase("MaxLadung")) {
        ship.setDeprecatedCapacity(Integer.parseInt(sc.argv[0]));
        sc.getNextToken();
      } else if((sc.argc == 1) && sc.argv[0].equals("EFFECTS")) {
        ship.setEffects(parseStringSequence(ship.getEffects()));
      } else if((sc.argc == 1) && sc.argv[0].equals("COMMENTS")) {
        ship.setComments(parseStringSequence(ship.getComments()));
      } else if(sc.isBlock) {
        break;
      } else {
        unknown("SCHIFF", true);
      }
    }
  }

  /*
   *
   */
  private void parseBuilding(GameData world, Region region, int sortIndex)
            throws IOException
  {
    EntityID id = EntityID.createEntityID(Integer.parseInt(sc.argv[0].substring(5)), world.base);
    sc.getNextToken(); // skip "BURG nr"

    Building bld = getAddBuilding(world, id);

    if(bld.getRegion() != region) {
      bld.setRegion(region);
    }

    bld.setSortIndex(sortIndex);

    while(!sc.eof) {
      if((sc.argc == 2) && sc.argv[1].equalsIgnoreCase("Name")) {
        bld.setName(sc.argv[0]);
        sc.getNextToken();
      } else if((sc.argc == 2) && sc.argv[1].equalsIgnoreCase("wahrerTyp")) {
        bld.setTrueBuildingType(sc.argv[0]);
        sc.getNextToken();
      } else if((sc.argc == 2) && sc.argv[1].equalsIgnoreCase("Typ")) {
        BuildingType type = world.rules.getBuildingType(StringID.create(sc.argv[0]), true);
        bld.setType(type);
        sc.getNextToken();
      } else if((sc.argc == 2) && sc.argv[1].equalsIgnoreCase("Beschr")) {
        bld.setDescription(sc.argv[0]);
        sc.getNextToken();
      } else if((sc.argc == 2) && sc.argv[1].equalsIgnoreCase("Besitzer")) {
        UnitID unitID = UnitID.createUnitID(Integer.parseInt(sc.argv[0]), world.base);
        bld.setOwnerUnit(getAddUnit(world, unitID));
        sc.getNextToken();
      } else if((sc.argc == 2) && sc.argv[1].equalsIgnoreCase("Partei")) {
        if((bld.getOwnerUnit() != null) && (bld.getOwnerUnit().getFaction() == null)) {
          Faction f = world.getFaction(EntityID.createEntityID(Integer.parseInt(sc.argv[0]), world.base));
          bld.getOwnerUnit().setFaction(f);
        }

        sc.getNextToken();
      } else if((sc.argc == 2) && sc.argv[1].equalsIgnoreCase("Groesse")) {
        bld.setSize(Integer.parseInt(sc.argv[0]));
        sc.getNextToken();
      } else if((sc.argc == 2) && sc.argv[1].equalsIgnoreCase("Unterhalt")) {
        bld.setCost(Integer.parseInt(sc.argv[0]));
        sc.getNextToken();
      } else if((sc.argc == 1) && sc.argv[0].equals("EFFECTS")) {
        bld.setEffects(parseStringSequence(bld.getEffects()));
      } else if((sc.argc == 1) && sc.argv[0].equals("COMMENTS")) {
        bld.setComments(parseStringSequence(bld.getComments()));
      } else if (sc.isBlock){
        break;
      } else {
        unknown("BURG", true);
      }
    }
  }

  /**
   * Parse consecutive GRENZE sub blocks of the REGION block.
   *
   * @param r a list to add the read borders to
   *
   * @throws IOException DOCUMENT-ME
   */
  private void parseBorders(Region r) throws IOException {
    while(!sc.eof && sc.isBlock && sc.argv[0].startsWith("GRENZE ")) {
      Border b = parseBorder();
      r.addBorder(b);
    }
  }

  /**
   * Parse consecutive SIGN sub blocks of the REGION block.
   *
   * @param r the actual region
   *
   * @throws IOException DOCUMENT-ME
   */
  private void parseSigns(Region r) throws IOException {
    while(!sc.eof && sc.isBlock && sc.argv[0].startsWith("SIGN ")) {
      Sign s = parseSign();
      r.addSign(s);
    }
  }
  
  
  
  /**
   * Parse one GRENZE sub block of the REGION block.
   *
   * @return the resulting <tt>Border</tt> object.
   *
   * @throws IOException DOCUMENT-ME
   */
  private Border parseBorder() throws IOException {
    ID id = IntegerID.create(sc.argv[0].substring(7));
    Border b = MagellanFactory.createBorder(id);
    sc.getNextToken(); // skip the block

    while(!sc.eof && !sc.isBlock) {
      if((sc.argc == 2) && sc.argv[1].equalsIgnoreCase("richtung")) {
        b.setDirection(-1);

        try {
          b.setDirection(Integer.parseInt(sc.argv[0]));
        } catch(NumberFormatException e) {
          final String dirNames[] = {
                          "Nordwesten", "Nordosten", "Osten", "Südosten",
                          "Südwesten", "Westen"
                        };

          for(int i = 0; i < dirNames.length; i++) {
            if(sc.argv[0].equalsIgnoreCase(dirNames[i])) {
              b.setDirection(i);

              break;
            }
          }
        }

        sc.getNextToken();
      } else if((sc.argc == 2) && sc.argv[1].equalsIgnoreCase("typ")) {
        b.setType(sc.argv[0]);
        sc.getNextToken();
      } else if((sc.argc == 2) && sc.argv[1].equalsIgnoreCase("prozent")) {
        b.setBuildRatio(Integer.parseInt(sc.argv[0]));
        sc.getNextToken();
      } else if(sc.isBlock) {
        break;
      } else {
        unknown("GRENZE", false);
      }
    }

    return b;
  }

  
  /**
   * Parse one SIGN sub block of the REGION block.
   *
   * @return the resulting <tt>SIGN</tt> object.
   *
   * @throws IOException DOCUMENT-ME
   */
  private Sign parseSign() throws IOException {
    Sign s = new Sign();
    // Border b = new Border(id);
    // create new sign...
    sc.getNextToken(); // skip the block

    while(!sc.eof && !sc.isBlock) {
      if((sc.argc == 2) && sc.argv[1].equalsIgnoreCase("text")) {
        s.setText(sc.argv[0]);
        sc.getNextToken();
      } else if(sc.isBlock) {
        break;
      } else {
        unknown("SIGN", false);
      }
    }
    return s;
  }
  
  private void parseHotSpot(GameData data) throws IOException {
    ID id = IntegerID.create(sc.argv[0].substring(8));
    HotSpot h = MagellanFactory.createHotSpot(id);
    sc.getNextToken(); // skip the block

    while(!sc.eof && !sc.isBlock) {
      if((sc.argc == 2) && sc.argv[1].equalsIgnoreCase("coord")) {
        h.setCenter(originTranslate(CoordinateID.parse(sc.argv[0], " ")));
        sc.getNextToken();
      } else if((sc.argc == 2) && sc.argv[1].equalsIgnoreCase("name")) {
        h.setName(sc.argv[0]);
        sc.getNextToken();
      } else {
        unknown("HOTSPOT", true);
      }
    }

    data.setHotSpot(h);
  }

  /*
   * Parse everything within one region.
   * Heuristic for block termination:
   *  - Terminate on another REGION or SPEZIALREGION block (without warning)
   *  - Terminate on any other unknown block (with warning)
   */
  private void parseRegion(GameData world, int sortIndex) throws IOException {
    int iValidateFlags = 0; // 1 - terrain type
    int unitSortIndex = 0;
    int shipSortIndex = 0;
    int buildingSortIndex = 0;
    CoordinateID c = CoordinateID.parse(sc.argv[0].substring(sc.argv[0].indexOf(" ", 0)), " ");
  
    if(c == null) {
      unknown("REGION", true);
      // FIXME (stm) really break on unknown token?

      return;
    }
    
    // this we should make after checking c!=null
    originTranslate(c);
    
    sc.getNextToken(); // skip "REGION x y"

    Region region = world.getRegion(c);

    if(region == null) {
      region = MagellanFactory.createRegion(c, world);
    }

    region.setSortIndex(sortIndex);

    while(!sc.eof) {
      if((sc.argc == 2) && sc.argv[1].equalsIgnoreCase("Name")) {
        // regions doesn't have name if name == type; e.g. "Ozean"=="Ozean"
        if(region.getType() == null) {
          region.setName(sc.argv[0]);
        } else {
          // FIXME? this will bite us sooner or later
          //  if (!region.getType().getName().equals(sc.argv[0])) {
          region.setName(sc.argv[0]);

          //  }
        }
        ui.setProgress(Resources.get("progressdialog.loadcr.step03",new Object[]{region.getName()}), 2);

        sc.getNextToken();
      } else if((sc.argc == 2) && sc.argv[1].equalsIgnoreCase("Beschr")) {
        region.setDescription(sc.argv[0]);
        sc.getNextToken();
      } else if((sc.argc == 2) && sc.argv[1].equalsIgnoreCase("Strasse")) {
        sc.getNextToken();
      } else if((sc.argc == 2) && sc.argv[1].equalsIgnoreCase("ejcIsSelected")) {
        world.getSelectedRegionCoordinates().put(c,region);
        sc.getNextToken();
      } else if((sc.argc == 2) && sc.argv[1].equalsIgnoreCase("Insel")) {
        try {
          //ID islandID = StringID.create(sc.argv[0]);
          ID islandID = IntegerID.create(sc.argv[0]);
          Island island = world.getIsland(islandID);

          if(island == null) {
            log.warn("CRParser.parseRegion(): unknown island " + sc.argv[0] +
                 " with region " + region + " in line " + sc.lnr+ ", creating it dynamically.");
            // FIXME: ID MUST STAY INTEGERID
//             island = new Island(islandID, world);
//             island.setName(islandID);
//             world.addIsland(island);
          }
          region.setIsland(island);

        } catch(NumberFormatException nfe) {
          log.warn("CRParser.parseRegion(): unknown island " + sc.argv[0] +
               " with region " + region + " in line " + sc.lnr);
        }

        sc.getNextToken();
      } else if((sc.argc == 2) && sc.argv[1].equalsIgnoreCase("Lohn")) {
        region.setWage(Integer.parseInt(sc.argv[0]));
        sc.getNextToken();
      } else if((sc.argc == 2) && sc.argv[1].equalsIgnoreCase("letzterlohn")) {
        region.setOldWage(Integer.parseInt(sc.argv[0]));
        sc.getNextToken();
      } else if((sc.argc == 2) && sc.argv[1].equalsIgnoreCase("aktiveRegion")) {
        region.setActive(true);
        sc.getNextToken();
      } else if((sc.argc == 2) && sc.argv[1].equalsIgnoreCase("Terrain")) {
        try {
          RegionType type = world.rules.getRegionType(StringID.create(sc.argv[0]), true);
          region.setType(type);
        } catch(IllegalArgumentException e) {
          // can happen in StringID constructor if sc.argv[0] == ""
          log.warn("CRParser.parseRegion(): found region without a valid region type in line " +
               sc.lnr);
        }

        // regions doesn't have name if name == type; e.g. "Ozean"=="Ozean"
        if(region.getType() != null) {
          if(region.getType().getName() != null) {
            // FIXME? this will bite us sooner or later
            //      if (region.getType().getName().equals(region.getName())) {
            //        region.setName( null );
            //      }
          } else {
            log.warn("CRParser.parseRegion(): found region type without a valid name in line " +
                 sc.lnr);
          }
        } else {
          log.warn("CRParser.parseRegion(): found region without a valid region type in line " +
               sc.lnr);
        }

        iValidateFlags |= 1;
        sc.getNextToken();
      } else if((sc.argc == 2) && sc.argv[1].equalsIgnoreCase("Baeume")) {
        region.setTrees(Integer.parseInt(sc.argv[0]));
        sc.getNextToken();
      } else if((sc.argc == 2) && sc.argv[1].equalsIgnoreCase("letztebaeume")) {
        region.setOldTrees(Integer.parseInt(sc.argv[0]));
        sc.getNextToken();
      } else if((sc.argc == 2) && sc.argv[1].equalsIgnoreCase("Bauern")) {
        region.setPeasants(Integer.parseInt(sc.argv[0]));
        sc.getNextToken();
      } else if((sc.argc == 2) && sc.argv[1].equalsIgnoreCase("letztebauern")) {
        region.setOldPeasants(Integer.parseInt(sc.argv[0]));
        sc.getNextToken();
      } else if((sc.argc == 2) && sc.argv[1].equalsIgnoreCase("Silber")) {
        region.setSilver(Integer.parseInt(sc.argv[0]));
        sc.getNextToken();
      } else if((sc.argc == 2) && sc.argv[1].equalsIgnoreCase("letztessilber")) {
        region.setOldSilver(Integer.parseInt(sc.argv[0]));
        sc.getNextToken();
      } else if((sc.argc == 2) && sc.argv[1].equalsIgnoreCase("Eisen")) {
        region.setIron(Integer.parseInt(sc.argv[0]));
        sc.getNextToken();
      } else if((sc.argc == 2) && sc.argv[1].equalsIgnoreCase("letzteseisen")) {
        region.setOldIron(Integer.parseInt(sc.argv[0]));
        sc.getNextToken();
      } else if((sc.argc == 2) && sc.argv[1].equalsIgnoreCase("Laen")) {
        region.setLaen(Integer.parseInt(sc.argv[0]));
        sc.getNextToken();
      } else if((sc.argc == 2) && sc.argv[1].equalsIgnoreCase("letzteslaen")) {
        region.setOldLaen(Integer.parseInt(sc.argv[0]));
        sc.getNextToken();
      } else if((sc.argc == 2) && sc.argv[1].equalsIgnoreCase("Pferde")) {
        region.setHorses(Integer.parseInt(sc.argv[0]));
        sc.getNextToken();
      } else if((sc.argc == 2) && sc.argv[1].equalsIgnoreCase("letztepferde")) {
        region.setOldHorses(Integer.parseInt(sc.argv[0]));
        sc.getNextToken();
      } else if((sc.argc == 2) && sc.argv[1].equalsIgnoreCase("Unterh")) {
        // Has not to be stored.
        sc.getNextToken();

        // pavkovic 2002.05.10: recruits (and old recruits are used from cr)
      } else if((sc.argc == 2) && sc.argv[1].equalsIgnoreCase("Rekruten")) {
        region.setRecruits(Integer.parseInt(sc.argv[0]));
        sc.getNextToken();
      } else if((sc.argc == 2) && sc.argv[1].equalsIgnoreCase("letzterekruten")) {
        region.setOldRecruits(Integer.parseInt(sc.argv[0]));
        sc.getNextToken();
      } else if((sc.argc == 2) && sc.argv[1].equalsIgnoreCase("maxLuxus")) {
        // Has not to be stored.
        sc.getNextToken();
      } else if((sc.argc == 2) && sc.argv[1].equalsIgnoreCase("Mallorn")) {
        if(Integer.parseInt(sc.argv[0]) > 0) {
          region.setMallorn(true);
        } else {
          region.setMallorn(false);
        }

        sc.getNextToken();
      } else if((sc.argc == 2) && sc.argv[1].equalsIgnoreCase("herb")) {
        ItemType type = world.rules.getItemType(StringID.create(sc.argv[0]), true);
        region.setHerb(type);
        sc.getNextToken();
      } else if((sc.argc == 2) && sc.argv[1].equalsIgnoreCase("herbamount")) {
        region.setHerbAmount(sc.argv[0]);
        sc.getNextToken();
      } else if((sc.argc == 2) && (sc.argv[1].compareTo("Runde") == 0)) {
        // ignore this tag
        sc.getNextToken();
      } else if((sc.argc == 2) && sc.argv[1].equalsIgnoreCase("Verorkt")) {
        region.setOrcInfested(true);
        sc.getNextToken();
      } else if((sc.argc == 2) && sc.argv[1].equalsIgnoreCase("Schoesslinge")) {
        region.setSprouts(Integer.parseInt(sc.argv[0]));
        sc.getNextToken();
      } else if((sc.argc == 2) && sc.argv[1].equalsIgnoreCase("letzteSchoesslinge")) {
        region.setOldSprouts(Integer.parseInt(sc.argv[0]));
        sc.getNextToken();
      } else if((sc.argc == 2) && (sc.argv[1].equalsIgnoreCase("Steine")|| sc.argv[1].equalsIgnoreCase("Stein"))) {
        region.setStones(Integer.parseInt(sc.argv[0]));
        sc.getNextToken();
      } else if((sc.argc == 2) && sc.argv[1].equalsIgnoreCase("letztesteine")) {
        region.setOldStones(Integer.parseInt(sc.argv[0]));
        sc.getNextToken();
      } else if((sc.argc == 2) && sc.argv[1].equalsIgnoreCase("visibility")) {
        region.setVisibility(sc.argv[0]);
        sc.getNextToken();
      } else if(sc.isBlock && sc.argv[0].equals("PREISE")) {
        region.setPrices(parsePrices(region.getPrices()));
      } else if(sc.isBlock && sc.argv[0].equals("LETZTEPREISE")) {
        region.setOldPrices(parsePrices(region.getOldPrices()));
      } else if(sc.isBlock && sc.argv[0].startsWith("GRENZE ")) {
        parseBorders(region);
      } else if(sc.isBlock && sc.argv[0].startsWith("SIGN ")) {
        parseSigns(region);
      } else if(sc.isBlock && sc.argv[0].startsWith("EINHEIT ")) {
        unitSortIndex = parseUnit(world, region, ++unitSortIndex);
      } else if(sc.isBlock && sc.argv[0].startsWith("SCHIFF ")) {
        parseShip(world, region, ++shipSortIndex);
      } else if(sc.isBlock && sc.argv[0].startsWith("BURG ")) {
        parseBuilding(world, region, ++buildingSortIndex);
      } else if(sc.isBlock && sc.argv[0].startsWith("MESSAGE ")) {
        region.setMessages(parseMessages(world, region.getMessages()));
      } else if(sc.isBlock && sc.argv[0].equals("REGIONSEREIGNISSE")) {
        region.setEvents(parseMessageSequence(region.getEvents()));

        /*} else if(sc.argc == 1 &&
         sc.argv[0].equals("REGIONSKOMMENTAR")) {
         region.comments = parseMessageSequence(region.comments);*/
      } else if(sc.isBlock && sc.argv[0].equals("REGIONSBOTSCHAFTEN")) {
        region.setPlayerMessages(parseMessageSequence(region.getPlayerMessages()));
      } else if(sc.isBlock && sc.argv[0].equals("UMGEBUNG")) {
        region.setSurroundings(parseMessageSequence(region.getSurroundings()));
      } else if(sc.isBlock && sc.argv[0].equals("DURCHREISE")) {
        region.setTravelThru(parseMessageSequence(region.getTravelThru()));
      } else if(sc.isBlock && sc.argv[0].equals("DURCHSCHIFFUNG")) {
        region.setTravelThruShips(parseMessageSequence(region.getTravelThruShips()));
      } else if(sc.isBlock && sc.argv[0].equals("EFFECTS")) {
        region.setEffects(parseStringSequence(region.getEffects()));
      } else if(sc.isBlock && sc.argv[0].equals("COMMENTS")) {
        region.setComments(parseStringSequence(region.getComments()));
      } else if(sc.isBlock && sc.argv[0].startsWith("RESOURCE ")) {
        RegionResource res = parseRegionResource(world.rules, region);

        if(res != null) {
          region.addResource(res);
        }
      } else if(sc.isBlock && sc.argv[0].startsWith("SCHEMEN ")) {
        parseScheme(world, region);
      } else if(sc.isBlock && sc.argv[0].equals("MESSAGETYPES")) {
        break;
      } else if(sc.isBlock && sc.argv[0].startsWith("REGION ")) {
        break;
      } else if(sc.isBlock) {
        break;
      } else {
        if(sc.argc == 2) {
          region.putTag(sc.argv[1], sc.argv[0]);
        }

        unknown("REGION", true);
      }
    }

    //validate region before add to world data
    if((iValidateFlags & 1) == 0) {
      log.warn("Warning: No region type is given for region '" + region.toString() +
           "' - it is ignored.");
    } else {
      world.addRegion(region);
    }
  }

  
  
  private Region parseSpecialRegion(GameData world, Region specialRegion)
                 throws IOException
  {
    int unitSortIndex = 0;
    sc.getNextToken(); // skip "SPEZIALREGION x y"

    if(specialRegion == null) {
      CoordinateID c = new CoordinateID(0, 0, 1);
      originTranslate(c);

      while(world.getRegion(c) != null) {
        c = new CoordinateID(0, 0, (int) (Math.random() * (Integer.MAX_VALUE - 1)) + 1);
        originTranslate(c);
      }

      specialRegion = MagellanFactory.createRegion(c, world);
      specialRegion.setName("Astralregion");
      world.addRegion(specialRegion);
    }

    while(!sc.eof) {
      if((sc.argc == 2) && sc.argv[1].equalsIgnoreCase("terrain")) {
        specialRegion.setType(world.rules.getRegionType(StringID.create(sc.argv[0]), true));
        sc.getNextToken();
      } else if(sc.isBlock && sc.argv[0].startsWith("SCHEMEN ")) {
        parseScheme(world, specialRegion);
      } else if(sc.isBlock && sc.argv[0].startsWith("MESSAGE ")) {
        specialRegion.setMessages(parseMessages(world, specialRegion.getMessages()));
      } else if(sc.isBlock && sc.argv[0].equals("EFFECTS")) {
        specialRegion.setEffects(parseStringSequence(specialRegion.getEffects()));
      } else if(sc.isBlock && sc.argv[0].equals("COMMENTS")) {
        specialRegion.setComments(parseStringSequence(specialRegion.getComments()));
      } else if(sc.isBlock && sc.argv[0].startsWith("GRENZE ")) {
        parseBorders(specialRegion);
      } else if(sc.isBlock && sc.argv[0].equals("REGIONSEREIGNISSE")) {
        specialRegion.setEvents(parseMessageSequence(specialRegion.getEvents()));
      } else if(sc.isBlock && sc.argv[0].equals("REGIONSBOTSCHAFTEN")) {
        specialRegion.setPlayerMessages(parseMessageSequence(specialRegion.getPlayerMessages()));
      } else if(sc.isBlock && sc.argv[0].equals("UMGEBUNG")) {
        specialRegion.setSurroundings(parseMessageSequence(specialRegion.getSurroundings()));
      } else if(sc.isBlock && sc.argv[0].equals("DURCHREISE")) {
        specialRegion.setTravelThru(parseMessageSequence(specialRegion.getTravelThru()));
      } else if(sc.isBlock && sc.argv[0].equals("DURCHSCHIFFUNG")) {
        specialRegion.setTravelThruShips(parseMessageSequence(specialRegion.getTravelThruShips()));
      } else if(sc.isBlock && sc.argv[0].startsWith("EINHEIT ")) {
        unitSortIndex = parseUnit(world, specialRegion, ++unitSortIndex);
      } else if(sc.isBlock) {
        break;
      } else {
        unknown("SPEZIALREGION", true);
        // FIXME (stm) really break on unknown token?

        break;
      }
    }

    return specialRegion;
  }

  private RegionResource parseRegionResource(Rules rules, Region region)
                    throws IOException
  {
    RegionResource r = null;
    ID id = null;
    ItemType type = null;
    id = LongID.create(sc.argv[0].substring(sc.argv[0].indexOf(" ", 0) + 1));
    sc.getNextToken(); // skip "RESOURCE id"

    while(!sc.eof && !sc.isBlock) {
      if((sc.argc == 2) && sc.argv[1].equalsIgnoreCase("type")) {
        if(r == null) {
          type = rules.getItemType(StringID.create(sc.argv[0]), true);

          if(type != null) {
            r = new RegionResource(id, type);
          }
        }

        sc.getNextToken();
      } else if((sc.argc == 2) && sc.argv[1].equalsIgnoreCase("skill")) {
        if(r != null) {
          r.setSkillLevel(Integer.parseInt(sc.argv[0]));
        }

        sc.getNextToken();
      } else if((sc.argc == 2) && sc.argv[1].equalsIgnoreCase("number")) {
        if(r != null) {
          r.setAmount(Integer.parseInt(sc.argv[0]));
        }

        sc.getNextToken();
      } else {
        unknown("RESOURCE", true);
      }
    }

    return r;
  }

  private void parseScheme(GameData world, Region region) throws IOException {
    CoordinateID c = CoordinateID.parse(sc.argv[0].substring(sc.argv[0].indexOf(" ", 0)), " ");
    originTranslate(c);

    if(c == null) {
      unknown("SCHEMEN", true);
      // FIXME (stm) really break on unknown token?

      return;
    }

    sc.getNextToken(); // skip "SCHEMEN x y"

    Scheme scheme = MagellanFactory.createScheme(c);
    region.addScheme(scheme);

    while(!sc.eof) {
      if((sc.argc == 2) && sc.argv[1].equalsIgnoreCase("name")) {
        scheme.setName(sc.argv[0]);
        sc.getNextToken();
      } else if(sc.isBlock) {
        break;
      } else {
        unknown("SCHEMEN", true);
        // FIXME (stm) really break on unknown token?

        break;
      }
    }

    // add scheme region as a normal region with unknown region type
    if(world.getRegion(c) == null) {
      Region newRegion = MagellanFactory.createRegion(c, world);
      newRegion.setType(RegionType.unknown);
      newRegion.setName(scheme.getName());
      world.addRegion(newRegion);
    }
  }

  /**
   * This function parses the informations found in Reader in and creates a corresponding
   * GameData object tree.
   *
   * @param in Reader to cr file
   * @param world GameData to be filled with informations of given cr file This function is
   *       synchronized.
   */
  public synchronized GameData read(Reader in, GameData world) throws IOException {
    boolean bCorruptReportMsg = false;
    int regionSortIndex = 0;
    // Fiete 20061208
    // set finalizer prio to max
    MemoryManagment.setFinalizerPriority(Thread.MAX_PRIORITY);
    
    ui.setTitle(Resources.get("progressdialog.loadcr.title"));
    ui.setMaximum(10000);
    ui.setProgress(Resources.get("progressdialog.loadcr.step01"), 1);
    ui.show();
    
    this.world = world;
    sc = new Scanner(in);
    sc.getNextToken();
    boolean oome = false;

    while(!sc.eof) {
      try {
        if(sc.argv[0].startsWith("VERSION")) {
          ui.setProgress(Resources.get("progressdialog.loadcr.step02"), 2);
          parseHeader(world);
        } else if((sc.argc == 1) && sc.argv[0].startsWith("REGION ")) {
          if(!bCorruptReportMsg) {
            log.warn("Warning: This computer report is " +
                 "missing the header and is therefore invalid or " +
                 "corrupted. Please contact the originator of this " +
                 "report if you experience data loss.");
            bCorruptReportMsg = true;
          }
  
          parseRegion(world, ++regionSortIndex);
        } else {
          unknown("top level", true);
        }
      } catch (OutOfMemoryError ome) {
        log.error(ome);
        oome = true;
      }
      
      setOwner(world);
      
      // Fiete 20061208  check Memory
      if (!MemoryManagment.isFreeMemory() || oome){
        // we have a problem..
        // like in startup of client..we reset the data
        this.world = new MissingData();
        // marking the problem
        this.world.outOfMemory = true;
        
        ui.ready();
        // end exit
        return this.world;
      }
      
    }
    this.world.setMaxSortIndex(++regionSortIndex);
    ui.ready();
    log.info("Done.");
    return this.world;
  }

  private void setOwner(GameData newData) {
    if (newData.getOwnerFaction()==null){
      // in standard reports, the first faction of the report should always be the owner faction 
      Faction firstFaction = getFirstFaction();
      if (getConfiguration().equals("Standard") && firstFaction!=null){
        newData.setOwnerFaction((EntityID) firstFaction.getID());
        
        // set translation to (0,0,...) in all existing layers
        Set<Integer> layers = new HashSet<Integer>();
        for (CoordinateID coord : newData.regions().keySet()){
          if (!layers.contains(coord.z)){
            newData.setCoordinateTranslation((EntityID) firstFaction.getID(), new CoordinateID(0,0,coord.z));
            layers.add(coord.z);
          }
        }
      }    
    }
  }

  private void invalidParam(String method, String msg) {
    log.warn("CRParser." + method + "(): invalid parameter specified, " + msg +
         "! Unable to parse block in line " + sc.lnr);
  }

  private void parseTranslation(GameData data) throws IOException {
    sc.getNextToken();

    while(!sc.eof) {
      if(sc.argc == 2) {
        // data.addTranslation(sc.argv[1], sc.argv[0]);
        data.addTranslation(sc.argv[1], sc.argv[0],TranslationType.sourceCR);
        sc.getNextToken();
      } else if(sc.isBlock) {
        break;
      } else {
        unknown("TRANSLATION", true);
        // FIXME (stm) really break on unknown token?

        break;
      }
    }
  }

}
