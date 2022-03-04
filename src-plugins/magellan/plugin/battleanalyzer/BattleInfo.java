// class magellan.plugin.battleanalyzer.BattleInfo
// created on Feb 5, 2012
//
// Copyright 2003-2012 by magellan project team
//
// Author : $Author: $
// $Id: $
//
// This program is free software; you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation; either version 2 of the License, or
// (at your option) any later version.
//
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU General Public License for more details.
//
// You should have received a copy of the GNU General Public License
// along with this program (see doc/LICENCE.txt); if not, write to the
// Free Software Foundation, Inc.,
// 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
//
package magellan.plugin.battleanalyzer;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Set;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.tree.DefaultMutableTreeNode;

import magellan.client.swing.tree.NodeWrapperFactory;
import magellan.library.Battle;
import magellan.library.CoordinateID;
import magellan.library.EntityID;
import magellan.library.Faction;
import magellan.library.GameData;
import magellan.library.Item;
import magellan.library.Message;
import magellan.library.Skill;
import magellan.library.StringID;
import magellan.library.UnitID;
import magellan.library.gamebinding.EresseaConstants;
import magellan.library.rules.ItemType;
import magellan.library.rules.SkillType;
import magellan.library.utils.Resources;
import magellan.library.utils.logging.Logger;

/**
 * Holds parsed info about a battle.
 *
 * @author stm
 */
public class BattleInfo {

  // protected static final Logger log = Logger.getInstance(BattleInfo.class);
  Logger log = Logger.getInstance(BattleInfo.class);
  protected static final SkillType unknownSkillType = new SkillType(StringID
      .create("unknown skill"));
  protected static final ItemType unknownItemType = new ItemType(StringID.create("unknown skill"));
  protected static final ItemType spellItemType = new ItemType(StringID.create("spells"));

  /** Number of rounds */
  public static int MAX_ROUND = 10;
  /** Number of rows */
  public static int MAX_ROWS = 5;

  /** Identifies a spell for our purposes */
  public static class SpellID {
    UnitID unit;
    int round;

    /** Obvious constructor */
    public SpellID(UnitID unit, int round) {
      this.unit = unit;
      this.round = round;
    }

    @Override
    public boolean equals(Object obj) {
      if (obj instanceof SpellID)
        return unit.equals(((SpellID) obj).unit) && round == ((SpellID) obj).round;
      return false;
    }

    @Override
    public int hashCode() {
      return unit.hashCode() * 15 + round;
    }
  }

  /** Info about spellcasting */
  public class SpellInfo {

    private String name;
    private UnitID unit;
    private int round;
    private boolean completed;
    private int dead = -1;
    private int amount = -1;

    /**
     * Returns the value of name.
     *
     * @return Returns name.
     */
    public String getName() {
      return name;
    }

    /**
     * Sets the value of name.
     *
     * @param name The value for name.
     */
    public void setName(String name) {
      this.name = name;
    }

    /**
     * Returns the value of unit.
     *
     * @return Returns unit.
     */
    public UnitID getUnit() {
      return unit;
    }

    /**
     * Sets the value of unit.
     *
     * @param unit The value for unit.
     */
    public void setUnit(UnitID unit) {
      if (unit == null)
        throw new NullPointerException();
      this.unit = unit;
    }

    /**
     * Returns the value of round.
     *
     * @return Returns round.
     */
    public int getRound() {
      return round;
    }

    /**
     * Sets the value of round.
     *
     * @param round The value for round.
     */
    public void setRound(int round) {
      this.round = round;
    }

    /**
     * Returns the value of completed.
     *
     * @return Returns completed.
     */
    public boolean isCompleted() {
      return completed;
    }

    /**
     * Sets the value of completed.
     *
     * @param completed The value for completed.
     */
    public void setCompleted(boolean completed) {
      this.completed = completed;
    }

    /**
     * Returns the value of dead.
     *
     * @return Returns dead.
     */
    public int getDead() {
      return dead;
    }

    /**
     * Sets the value of dead.
     *
     * @param dead The value for dead.
     */
    public void setDead(int dead) {
      this.dead = dead;
    }

    /**
     * Returns the value of amount.
     *
     * @return Returns amount.
     */
    public int getAmount() {
      return amount;
    }

    /**
     * Sets the value of amount.
     *
     * @param amount The value for amount.
     */
    public void setAmount(int amount) {
      this.amount = amount;
    }

  }

  /** Info about a host */
  public class HostInfo {
    private int index;
    private String name;
    private String abbrev;
    private EntityID id;
    private int ded;
    private int fled;
    private int survived;
    private Set<Integer> adversaries;
    private Set<Integer> attacked;
    private Set<Integer> helped;
    private int[][] rows;

    private Map<UnitID, UnitInfo> units;
    private boolean changed;

    /** Standard constructor */
    public HostInfo() {
      adversaries = new HashSet<Integer>();
      attacked = new HashSet<Integer>();
      helped = new HashSet<Integer>();
      rows = new int[MAX_ROUND + 3][MAX_ROWS]; // MAX_ROUND+precombat+tactic+after
    }

    /**
     * Returns the value of index.
     *
     * @return Returns index.
     */
    public int getIndex() {
      return index;
    }

    /**
     * Sets the value of index.
     *
     * @param index The value for index.
     */
    public void setIndex(int index) {
      this.index = index;
    }

    /**
     * Returns the value of name.
     *
     * @return Returns name.
     */
    public String getName() {
      return name;
    }

    /**
     * Sets the value of name.
     *
     * @param name The value for name.
     */
    public void setName(String name) {
      this.name = name;
    }

    /**
     * Returns the value of id.
     *
     * @return Returns id.
     */
    public EntityID getId() {
      return id;
    }

    /**
     * Sets the value of id.
     *
     * @param id The value for id.
     */
    public void setId(EntityID id) {
      this.id = id;
    }

    /**
     * Returns the value of dead.
     *
     * @return Returns dead.
     */
    public int getDead() {
      return ded;
    }

    /**
     * Sets the value of dead.
     *
     * @param dead The value for dead.
     */
    public void setDead(int dead) {
      ded = dead;
    }

    /**
     * Returns the value of fled.
     *
     * @return Returns fled.
     */
    public int getFled() {
      return fled;
    }

    /**
     * Sets the value of fled.
     *
     * @param fled The value for fled.
     */
    public void setFled(int fled) {
      this.fled = fled;
    }

    /**
     * Returns the value of survived.
     *
     * @return Returns survived.
     */
    public int getSurvived() {
      return survived;
    }

    /**
     * Sets the value of survived.
     *
     * @param survived The value for survived.
     */
    public void setSurvived(int survived) {
      this.survived = survived;
    }

    /** Add a foe */
    public void addAdversary(int host) {
      adversaries.add(host);
    }

    /** Add an ally */
    public void addHelped(int host) {
      helped.add(host);

    }

    /** Add a victim */
    public void addAttacked(int host) {
      attacked.add(host);
    }

    /**
     * Set row info. <code>setRow(0,row,x)</code> sets the number of people in the lineup.
     * <code>setRow(round,row,x)</code> is the number of people in the round-1.
     * <code>setRow(maxRound+2, row, x)</code> is the number of people after combat.
     */
    public void setRow(int round, int row, int number) {
      rows[round + 1][row] = number;
      changed = true;
    }

    /**
     * Returns the value of adversaries.
     *
     * @return Returns adversaries.
     */
    public Set<Integer> getAdversaries() {
      return adversaries;
    }

    /**
     * Returns the value of attacked.
     *
     * @return Returns attacked.
     */
    public Set<Integer> getAttacked() {
      return attacked;
    }

    /**
     * Returns the value of helped.
     *
     * @return Returns helped.
     */
    public Set<Integer> getHelped() {
      return helped;
    }

    /**
     * Returns the row infos. <code>getRows()[0][row]</code> is the number of people in the lineup.
     * <code>getRows()[round][row]</code> is the number of people in the round-1.
     * <code>getRows()[maxRound+2][row]</code> is the number of people after combat. Front row is row 0.
     */
    public int[][] getRows() {
      if (changed) {
        for (UnitInfo info : getUnits()) {
          rows[0][info.getRow() - 1] += info.getPersons();
        }
        for (int round = 0; round < rows.length; ++round) {
          for (int row = 0; row < rows[round].length - 1; ++row) {
            rows[round][rows[round].length - 1] += rows[round][row];
          }
        }
        changed = false;
      }
      return rows;
    }

    /**
     * Returns the value of abbrev.
     *
     * @return Returns abbrev.
     */
    public String getAbbrev() {
      return abbrev;
    }

    /**
     * Sets the value of abbrev.
     *
     * @param abbrev The value for abbrev.
     */
    public void setAbbrev(String abbrev) {
      this.abbrev = abbrev;
    }

    /** Add a unit */
    public void addUnit(UnitID id_, UnitInfo unitInfo) {
      if (units == null) {
        units = new LinkedHashMap<UnitID, BattleInfo.UnitInfo>();
      }
      units.put(id_, unitInfo);
      changed = true;
    }

    /** Return units */
    public Collection<UnitInfo> getUnits() {
      if (units == null) {
        units = new LinkedHashMap<UnitID, BattleInfo.UnitInfo>();
      }
      return units.values();
    }

  }

  /** Info about a unit */
  public class UnitInfo {
    private int host = -1;
    private UnitID unit;

    private String name;
    private int persons;
    private String race;
    boolean hero;
    private int row = -1;
    private String combatStatus;
    private String health;
    private boolean starving;
    private boolean guarding;

    private Map<SkillType, Skill> skills;
    private Map<ItemType, Item> items;

    private int hits = -1;
    private int kills = -1;
    private int fallen = 0;
    private int alive = -1;
    private int run = 0;
    private List<Item> loot;

    /**
     * Returns the value of unit.
     *
     * @return Returns unit.
     */
    public UnitID getUnit() {
      return unit;
    }

    /**
     * Sets the value of unit.
     *
     * @param unit The value for unit.
     */
    public void setUnit(UnitID unit) {
      this.unit = unit;
    }

    /**
     * Returns the value of fallen.
     *
     * @return Returns fallen.
     */
    public int getFallen() {
      return fallen;
    }

    /**
     * Sets the value of fallen.
     *
     * @param fallen The value for fallen.
     */
    public void setFallen(int fallen) {
      this.fallen = fallen;
    }

    /**
     * Returns the value of alive.
     *
     * @return Returns alive.
     */
    public int getAlive() {
      return alive;
    }

    /**
     * Sets the value of alive.
     *
     * @param alive The value for alive.
     */
    public void setAlive(int alive) {
      this.alive = alive;
    }

    /**
     * Returns the value of run.
     *
     * @return Returns run.
     */
    public int getRun() {
      return run;
    }

    /**
     * Sets the value of run.
     *
     * @param run The value for run.
     */
    public void setRun(int run) {
      this.run = run;
    }

    /**
     * Returns the value of host.
     *
     * @return Returns host.
     */
    public int getHost() {
      return host;
    }

    /**
     * Sets the value of host.
     *
     * @param host The value for host.
     */
    public void setHost(int host) {
      this.host = host;
    }

    /**
     * Returns the value of hits.
     *
     * @return Returns hits.
     */
    public int getHits() {
      return hits;
    }

    /**
     * Sets the value of hits.
     *
     * @param hits The value for hits.
     */
    public void setHits(int hits) {
      this.hits = hits;
    }

    /**
     * Returns the value of kills.
     *
     * @return Returns kills.
     */
    public int getKills() {
      return kills;
    }

    /**
     * Sets the value of kills.
     *
     * @param kills The value for kills.
     */
    public void setKills(int kills) {
      this.kills = kills;
    }

    /** Adds a loot item */
    public void addLoot(String itemType, int amount) {
      if (loot == null) {
        loot = new LinkedList<Item>();
      }
      loot.add(createItem(itemType, amount));
    }

    /**
     * Returns the value of name.
     *
     * @return Returns name.
     */
    public String getName() {
      return name;
    }

    /**
     * Sets the value of name.
     *
     * @param name The value for name.
     */
    public void setName(String name) {
      this.name = name;
    }

    /**
     * Returns the value of persons.
     *
     * @return Returns persons.
     */
    public int getPersons() {
      return persons;
    }

    /**
     * Sets the value of persons.
     *
     * @param persons The value for persons.
     */
    public void setPersons(int persons) {
      this.persons = persons;
    }

    /**
     * Returns the value of race.
     *
     * @return Returns race.
     */
    public String getRace() {
      return race;
    }

    /**
     * Sets the value of race.
     *
     * @param race The value for race.
     */
    public void setRace(String race) {
      this.race = race;
    }

    /**
     * Returns the value of hero.
     *
     * @return Returns hero.
     */
    public boolean isHero() {
      return hero;
    }

    /**
     * Sets the value of hero.
     *
     * @param hero The value for hero.
     */
    public void setHero(boolean hero) {
      this.hero = hero;
    }

    /**
     * Returns the value of combatStatus.
     *
     * @return Returns combatStatus.
     */
    public String getCombatStatus() {
      return combatStatus;
    }

    /**
     * Sets the value of combatStatus.
     *
     * @param combatStatus The value for combatStatus.
     */
    public void setCombatStatus(String combatStatus) {
      this.combatStatus = combatStatus;
    }

    /**
     * Returns the value of health.
     *
     * @return Returns health.
     */
    public String getHealth() {
      return health;
    }

    /**
     * Sets the value of health.
     *
     * @param health The value for health.
     */
    public void setHealth(String health) {
      this.health = health;
    }

    /**
     * Returns the value of starving.
     *
     * @return Returns starving.
     */
    public boolean isStarving() {
      return starving;
    }

    /**
     * Sets the value of starving.
     *
     * @param starving The value for starving.
     */
    public void setStarving(boolean starving) {
      this.starving = starving;
    }

    /**
     * Returns the value of guarding.
     *
     * @return Returns guarding.
     */
    public boolean isGuarding() {
      return guarding;
    }

    /**
     * Sets the value of guarding.
     *
     * @param guarding The value for guarding.
     */
    public void setGuarding(boolean guarding) {
      this.guarding = guarding;
    }

    /** Add a skill */
    public void addSkill(Skill skill) {
      if (skill.getSkillType() == null)
        throw new NullPointerException();
      if (skills == null) {
        skills = new LinkedHashMap<SkillType, Skill>();
      }
      skills.put(skill.getSkillType(), skill);
    }

    /** Add a skill */
    public void addSkill(String typeName, int level) {
      SkillType type = world.getRules().getSkillType(typeName);
      if (type == null) {
        for (SkillType someType : world.getRules().getSkillTypes()) {
          if (Resources.getRuleItemTranslation("skill." + someType.toString(), getLocale()).equals(
              typeName)) {
            type = someType;
            break;
          }
        }
      }
      if (type == null) {
        type = world.getRules().getSkillType(EresseaConstants.S_MAGIE);
        if (!typeName.startsWith(Resources.getRuleItemTranslation("skill." + type.toString(),
            getLocale()))) {
          log.warn("unknown skill type " + typeName);
          type = unknownSkillType;
        }
      }
      addSkill(new Skill(type, 0, level, 0, true));
    }

    /** Add an item */
    public void addItem(Item item) {
      if (items == null) {
        items = new LinkedHashMap<ItemType, Item>();
      }
      items.put(item.getItemType(), item);
    }

    /** Add an item if it's a weapon or armour */
    public void addItem(String typeName, int amount) {
      Item item = createItem(typeName, amount);

      ItemType type = item.getItemType();
      if (type == null) {
        log.warnOnce("unknown item type " + typeName);
      } else if (type.getCategory() != null
          && (type.getCategory().isDescendant(
              world.getRules().getItemCategory(EresseaConstants.C_WEAPONS)) || type.getCategory()
                  .isDescendant(world.getRules().getItemCategory(EresseaConstants.C_ARMOUR)))) {
        addItem(item);
      }
    }

    /**
     * Returns the value of row. Front row is row 1.
     *
     * @return Returns row.
     */
    public int getRow() {
      return row;
    }

    /**
     * Sets the value of row. Front row is row 1.
     *
     * @param row The value for row.
     */
    public void setRow(int row) {
      this.row = row;
    }

    /**
     * Returns the value of skills.
     *
     * @return Returns skills.
     */
    public Collection<Skill> getSkills() {
      if (skills == null)
        return Collections.emptyList();
      else
        return skills.values();
    }

    /**
     * Returns the value of items.
     *
     * @return Returns items.
     */
    public Collection<Item> getItems() {
      if (items == null)
        return Collections.emptyList();
      else
        return items.values();
    }

    /**
     * Returns the value of loot.
     *
     * @return Returns loot.
     */
    public List<Item> getLoot() {
      if (loot == null)
        return Collections.emptyList();
      else
        return loot;
    }
  }

  private GameData world;
  private CoordinateID coordinate;
  private Map<String, EntityID> factions;

  private Set<EntityID> started;
  private Map<SpellID, SpellInfo> spells; // unit, round
  private Map<Integer, HostInfo> hosts; // index
  private Map<UnitID, UnitInfo> allUnits; // ID
  private List<String> errors;

  private HostInfo currentHost;
  private int currentRow;
  private int currentRound;
  private Set<UnitID> tactics;
  private Locale locale;
  private List<Set<Integer>> sides;

  protected Pattern hostPattern1;
  protected Pattern hostPattern2;
  protected Pattern unitPattern;
  protected Pattern skillPattern;
  protected Pattern itemPattern;
  protected Pattern overviewPattern;
  protected Locale lastLocale;
  private Battle battle;
  private Map<String, ItemType> itemTypes;

  private int maxRound = 5;
  private int heroFactor = 5;

  /**
   * Initializes an empty battle info.
   *
   * @param id Coordinate of the battle
   * @param world
   */
  public BattleInfo(CoordinateID id, GameData world) {
    itemTypes = new HashMap<String, ItemType>();
    setCoordinate(id);
    this.world = world;
    factions = new HashMap<String, EntityID>();
    for (Faction f : world.getFactions()) {
      factions.put(f.getName(), f.getID());
    }

    started = new LinkedHashSet<EntityID>();
    spells = new LinkedHashMap<BattleInfo.SpellID, BattleInfo.SpellInfo>();
    hosts = new TreeMap<Integer, BattleInfo.HostInfo>();
    tactics = new LinkedHashSet<UnitID>();
    errors = new LinkedList<String>();
    allUnits = new HashMap<UnitID, UnitInfo>();

    unknownSkillType.setName(Resources.get("plugin.battle.skill.unknown"));
    unknownItemType.setName(Resources.get("plugin.battle.item.unknown"));
    spellItemType.setName(Resources.get("plugin.battle.item.spell"));
  }

  /** Constructs an item. Constructs new item type if necessary */
  public Item createItem(String typeName, int amount) {
    typeName = typeName.replace("ögen", "ogen");
    typeName = typeName.replace("brüste", "brust");
    typeName = typeName.replace("chwerter", "chwert");
    typeName = typeName.replace("äxte", "axt");
    typeName = typeName.replace("ellebarden", "ellebarde");
    typeName = typeName.replace("peere", "peer");
    typeName = typeName.replace("Rostige Kettenhemden", "Rostiges Kettenhemd");
    typeName = typeName.replace("hemden", "hemd");
    typeName = typeName.replace("childe", "child");
    typeName = typeName.replace("rösser", "ross");
    typeName = typeName.replace("atapulte", "atapult");

    typeName = typeName.replace("bows", "bow");
    typeName = typeName.replace("swords", "sword");
    typeName = typeName.replace("claymores", "claymore");
    typeName = typeName.replace("axes", "axe");
    typeName = typeName.replace("spears", "spear");
    typeName = typeName.replace("halberds", "halberd");
    typeName = typeName.replace("catapults", "catapult");
    typeName = typeName.replace("chainmails", "chainmail");
    typeName = typeName.replace("hemden", "hemd");
    typeName = typeName.replace("plates", "plate");
    typeName = typeName.replace("shields", "shield");
    typeName = typeName.replace("chargers", "charger");

    ItemType type = world.getRules().getItemType(typeName);
    if (type == null) {
      type = itemTypes.get(typeName);
      if (type == null) {
        type = new ItemType(StringID.create(typeName));
        type.setName(typeName);
      }
      itemTypes.put(typeName, type);
    }
    return new Item(type, amount);
  }

  /**
   * Adds information from a battle to this info.
   *
   * @param b A battle
   * @param locale_ locale of the battle report (which usually equals the faction's locale)
   */
  public synchronized void parse(Battle b, Locale locale_) {
    battle = b;
    setLocale(locale_);

    initPatterns();

    currentRound = -1;
    currentHost = null;
    currentRow = -1;
    SpellInfo currentSpellInfo = null;

    for (Message m : b.messages()) {
      try {
        switch (m.getMessageType().getID().intValue()) {
        case 1597436160:
        case 26679501: // Der Kampf wurde ausgelöst
          addStarter(m);
          currentSpellInfo = null;
          break;
        case 132674556:
        case 1670085741: // Zauber schlägt fehl
          addSpell(m, false);
          currentSpellInfo = null;
          break;
        case 1361741363: // Friedenslied
        case 886644306: // Schwere Glieder
        case 1702909634: // Gesang der Angst
        case 1896971593: // Blutrausch
        case 1548204856: // Heldengesang
        case 210897516: // Schlaf
        case 2045311619: // Furcht
        case 1619294438: // Basilisk
        case 1677670293: // Trugbilder
        case 1402218402: // zaubert
        case 138727251: // "Rosie Kattun (sppd) zaubert Beschleunigung: 385 Krieger wurden magisch
                        // beschleunigt.";rendered
        case 450463848: // "Die Verzehrenden von Dotetkul (Li8h) zaubert Eisiger Drachenodem: 1 Krieger wurde
                        // getötet.";rendered
        case 1878259751: // niemand in Reichweite
        case 1385719261: // Tod des Geistes
          currentSpellInfo = addSpell(m, true);
          break;
        case 672044929: // Wölfe
          currentSpellInfo =
              addSpell(m, Resources.get("plugin.battle.spell.wolves", getLocale(), false), true);
          break;
        case 588447428: // Tumult, legt sich
        case 130687857: // Tumult, legt sich
        case 46549538: // Tumult
        case 354013025: // Tumult
          currentSpellInfo =
              addSpell(m, Resources.get("plugin.battle.spell.tumult", getLocale(), false), true);
          break;
        case 855292180: // Sturm
          currentSpellInfo =
              addSpell(m, Resources.get("plugin.battle.spell.sturm", getLocale(), false), true);
          break;
        case 1694084197: // Windschild, nobody there
          currentSpellInfo =
              addSpell(m, Resources.get("plugin.battle.spell.sturm2", getLocale(), false), true);
          break;
        case 1697874555: // zaubert, tote
          currentSpellInfo = addSpell(m, true);
          break;
        case 2126279277: // zaubert, Opfer
          currentSpellInfo = addSpell(m, true);
          break;
        case 788716474: // zaubert Wiederbelebung
          currentSpellInfo =
              addSpell(m, Resources.get("plugin.battle.spell.wiederbelebung", getLocale(), false),
                  true);
          break;
        case 2046934159: // zaubert Heilung
          currentSpellInfo =
              addSpell(m, Resources.get("plugin.battle.spell.heilung", getLocale(), false), true);
          break;
        case 728970757: // Untote helden
        case 813109076: // keine Untoten
          currentSpellInfo =
              addSpell(m, Resources.get("plugin.battle.spell.untote_helden", getLocale(), false),
                  true);
          // 11 27 0;region ignored
          break;
        case 1281520191: // Flammenschwert //
                         // "4 Krieger von Meisterfechter (wxon) benutzen ihre Flammenschwerter.";rendered
        case 646112495: // "4 Krieger von Meisterfechter (wxon) benutzen ihre Flammenschwerter.";rendered
        case 881122508:
        case 1399004700: // Katapult
          currentSpellInfo = addCatapult(m);
          break;
        case 394859417:
        case 2001303749: // Katapulttote // "Meisterfechter (wxon) tötete 0 Krieger.";rendered
          if (currentSpellInfo == null) {
            log.warn("unexpected dead " + m);
          } else {
            currentSpellInfo.setDead(Integer.parseInt(m.getAttribute("dead")));
          }
          break;
        case 1612582557: // empty
          currentSpellInfo = null;
          break;
        case 1801908756: // neu in 1080
        case 1964885468: // "Army 0: Urgos wilder Haufen (isb9)";rendered
          currentSpellInfo = null;
          currentHost = addHost(m);
          hosts.put(currentHost.getIndex(), currentHost);
          currentRow = -1;
          break;
        case 1803906635: // string
          currentSpellInfo = null;
          addMisc(m);
          break;
        case 1617067165:
        case 1684814935: // Reihe
          currentSpellInfo = null;
          currentRow = startRow(m);
          break;
        case 472935109:
        case 603624501: // Falle
        case 1818018183: // Falle neu 1080
        case 101630319:
        case 583524665: // überrascht neu 1080
        case 606685727: // überrascht
          currentSpellInfo = null;
          addTactic(m);
          break;
        case 1558678477:
        case 564544796: // neu Runde 1080
        case 715582328: // Einheiten vor der x. Runde
          currentSpellInfo = null;
          currentRound = startRound(m);
          setMaxRound(currentRound - 1);
          break;
        case 164793159:
        case 22298165: // nach dem Kampf
        case 1264406109: // nach dem Kampf neu 1080
          currentSpellInfo = null;
          setMaxRound(currentRound);
          currentRound++;
          break;
        case 804883071: // "Galerrawyn (8610) verlor 1 Personen."
          currentSpellInfo = null;
          addLoss(m);
          break;
        case 445331753:
        case 1109807897: // "Army 0(isb9): 0 dead, 0 fled, 4 survivors."
          currentSpellInfo = null;
          addSummary(m);
          break;
        case 1436762363:
          currentSpellInfo = null;
          addHit(m);
          break;
        case 1421907893: // "Mannen (9eu) erbeutet 1 Drachenblut.";rendered
          currentSpellInfo = null;
          addLoot(m);
          break;
        case 131196913: // Heiltrank
          currentSpellInfo = null;
          // ignore
          break;
        case 1185902130: // old reports, deprecated messages
          if (m.getText().matches("( *)|(-+)")) {
            // ignore
          } else if (m.getText().matches(
              Resources.get("plugin.battle.pattern.started", getLocale(), false))) {
            // "Der Kampf wurde ausgelöst von Clan Donald (gvs6).";string
            Matcher matcher =
                Pattern.compile(Resources.get("plugin.battle.pattern.started", getLocale(), false))
                    .matcher(m.getText());
            matcher.matches();
            addStarter(m, matcher.group(1));
            currentSpellInfo = null;
          } else if (m.getText().matches(
              Resources.get("plugin.battle.pattern.army", getLocale(), false))) {
            // "Heer 0: Clan Donald (gvs6)";string
            Matcher matcher =
                Pattern.compile(Resources.get("plugin.battle.pattern.army", getLocale(), false))
                    .matcher(m.getText());
            matcher.matches();
            currentHost = addHost(m, matcher.group(1), matcher.group(2));
            hosts.put(currentHost.getIndex(), currentHost);
            currentRow = -1;
          } else if (m.getText().startsWith(
              Resources.get("plugin.battle.fights", getLocale(), false))) {
            // "Kämpft gegen: Heer 1(LdM)";string
            addMisc(m);
          } else if (m.getText().startsWith(
              Resources.get("plugin.battle.attacks", getLocale(), false))) {
            addMisc(m);
          } else if (m.getText().startsWith(
              Resources.get("plugin.battle.helps", getLocale(), false))) {
            addMisc(m);
          } else if (unitPattern.matcher(m.getText()).matches()) {
            // " + Ed MacDonald's Trupp (cw9v), 8 Menschen, vorne, hat: 8 Speere.";string
            addMisc(m);
          } else if (m.getText().matches(
              Resources.get("plugin.battle.pattern.loot", getLocale(), false))) {
            // "Seekriegsgarde (ypb2) erbeuten 92 Silber.";string
            addLoot(m, m.getText());
          } else {
            currentSpellInfo = null;
            addMisc(m);
          }
          break;
        default:
          currentSpellInfo = null;
          log.warn("unknown battle message type " + m.getMessageType().getID());
          addSpell(m, true);
        }

      } catch (RuntimeException e) {
        log.warn(e);
        addError(m, e);
      }
    }

    analyzeSides();
  }

  private void analyzeSides() {
    sides = new ArrayList<Set<Integer>>();
    Set<Integer> side = new LinkedHashSet<Integer>();
    for (HostInfo host : hosts.values()) {
      side.add(host.getIndex());
    }
    sides.add(side);

    boolean changed = true;
    for (int i = 0; i < sides.size(); ++i) {
      side = sides.get(i);
      Set<Integer> newSide = null;
      while (changed) {
        changed = false;
        for (int hostNum : side) {
          HostInfo host = hosts.get(hostNum);
          for (int adv : host.getAdversaries()) {
            if (side.contains(adv)) {
              side.remove(adv);
              if (newSide == null) {
                newSide = new LinkedHashSet<Integer>();
                sides.add(newSide);
              }
              newSide.add(adv);
              changed = true;
            }
          }
          if (changed) {
            break;
          }
        }
      }
    }
  }

  private void addLoot(Message m, String text) {
    // "Seekriegsgarde (ypb2) erbeuten 92 Silber.";string
    Matcher matcher =
        Pattern.compile(Resources.get("plugin.battle.pattern.loot", getLocale(), false)).matcher(
            text);
    if (matcher.matches()) {
      UnitInfo info = getUnit(UnitID.createUnitID(matcher.group(2), world.base, world.base));
      info.addLoot(matcher.group(4), Integer.parseInt(matcher.group(3)));
    }

  }

  private void addLoot(Message m) {
    UnitInfo info = getUnit(getUnit(m));
    info.addLoot(m.getAttribute("item"), Integer.parseInt(m.getAttribute("amount")));
  }

  private void addHit(Message m) {
    UnitInfo info = getUnit(getUnit(m));
    info.setKills(Integer.parseInt(m.getAttribute("kills")));
    info.setHits(Integer.parseInt(m.getAttribute("hits")));
  }

  private UnitID addTactic(Message m) {
    UnitID tactic = getUnit(m);
    if (!tactics.contains(tactic)) {
      tactics.add(tactic);
    }
    return tactic;
  }

  private void addSummary(Message m) {
    int hostIndex = Integer.parseInt(m.getAttribute("index"));
    HostInfo messageHost = hosts.get(hostIndex);
    messageHost.setDead(Integer.parseInt(m.getAttribute("dead")));
    if (m.getAttribute("fled") != null) {
      messageHost.setFled(Integer.parseInt(m.getAttribute("fled")));
    } else if (m.getAttribute("flown") != null) {
      messageHost.setFled(Integer.parseInt(m.getAttribute("flown")));
    }

    messageHost.setSurvived(Integer.parseInt(m.getAttribute("survived")));
    String abbrev = m.getAttribute("abbrev");
    if (messageHost.getAbbrev() == null || !abbrev.equals("-?-")) {
      messageHost.setAbbrev(abbrev);
    }
  }

  private void addLoss(Message m) {
    UnitInfo info = getUnit(getUnit(m));
    info.setFallen(Integer.parseInt(m.getAttribute("fallen")));
    info.setAlive(Integer.parseInt(m.getAttribute("alive")));
    info.setRun(Integer.parseInt(m.getAttribute("run")));
  }

  private int startRound(Message m) {
    return Integer.parseInt(m.getAttribute("turn"));
  }

  private int startRow(Message m) {
    return Integer.parseInt(m.getAttribute("row"));
  }

  protected void initPatterns() {
    if (lastLocale != getLocale()) {

      hostPattern1 = Pattern.compile(" *.* ([0-9]+) *\\((([^)]+)|(-\\?-))\\) *");
      hostPattern2 =
          Pattern.compile(" *" + Resources.get("plugin.battle.host", getLocale(), false)
              + " *[0-9]+.*");
      unitPattern =
          Pattern.compile(
              // 1: name, 2: ID
              " *[*+-] (.+) \\(([0-9a-zL]{1,4})\\)"
                  // 3: number 4:race
                  + ", ([0-9]+) ([^,;.()]+)"
                  // 5: hero?
                  + "(, "
                  + Resources.get("plugin.battle.hero", getLocale(), false)
                  + ")*"
                  // 6:combat status, 8: health?, 10: hunger?
                  + ", ([^,;.()]+)( \\(([^,;.)]+)(, ([^,;.)]+))*\\))*"
                  // 11:guarding?
                  + "(, "
                  + Resources.get("plugin.battle.guards", getLocale(), false)
                  + "[^,;.()]*)*"
                  // 12: skills?
                  + "(, " + Resources.get("plugin.battle.skills", getLocale(), false)
                  + " ([^.,;:0-9]* [0-9]+)(, [^.,;:0-9]* [0-9]+)*)*"
                  // 15: items?
                  + "(, " + Resources.get("plugin.battle.has", getLocale(), false)
                  + " (([0-9]+ )*[^;,.:0-9]+)(, ([0-9]+ )*[^;,.:0-9]+)*)*" + "([,;.].*)");
      skillPattern = Pattern.compile(" *(\\p{L}[^0-9]*[^ ]) ([0-9]+) *");
      itemPattern = Pattern.compile(" *(([0-9]+) )*(\\p{L}[^0-9]*[^ ]) *"); // \p{L}: letter
      overviewPattern =
          Pattern.compile(" *" + Resources.get("plugin.battle.host", getLocale(), false)
          // 1: number, 2: id
              + " *([0-9]+)\\(([^)]*)\\): "
              // 3: first row, 4: second row?
              + "([0-9]+)(\\+([0-9]+)(\\+([0-9]+)(\\+([0-9]+))*)*)*");
      lastLocale = getLocale();
    }

  }

  private void addMisc(Message m) {
    String string = m.getAttribute("string");
    if (string.startsWith(Resources.get("plugin.battle.fights", getLocale(), false))) {
      // "Kämpft gegen: Heer 1(orks)";string
      // "Kämpft gegen: Heer 1(orks), Heer 2(x), Heer 3(2j4e), Heer 4(das), Heer 5(przp), Heer 6(fust), Heer 7(Lord),
      // Heer 8(Luft), Heer 9(va2z), Heer 10(-?-), Heer 11(mu47), Heer 12(bart)";string
      for (String hostPart : string.substring(string.indexOf(":") + 1).split(",")) {
        Matcher matcher = hostPattern1.matcher(hostPart);
        boolean matched = matcher.matches();
        if (!matched) {
          log.warn("unexpected message; " + string);
        } else {
          int number = Integer.parseInt(matcher.group(1));
          currentHost.addAdversary(number);
        }
      }
    } else if (string.startsWith(Resources.get("plugin.battle.helps", getLocale(), false))) {
      // "Hilft: Heer 0(phos)";string
      // "Hilft: Heer 0(-?-), Heer 13(-?-), Heer 14(-?-), Heer 15(haLb), Heer 16(stds), Heer 17(-?-), Heer 18(-?-), Heer
      // 19(drac), Heer 20(ouLe), Heer 21(yjap), Heer 22(-?-), Heer 23(tata), Heer 24(-?-), Heer 25(cat), Heer 26(pt6f),
      // Heer 27(fLos), Heer 28(1akL), Heer 29(phos), Heer 30(isb9)";string
      for (String hostPart : string.substring(string.indexOf(":") + 1).split(",")) {
        Matcher matcher = hostPattern1.matcher(hostPart);
        boolean matched = matcher.matches();
        if (!matched) {
          log.warn("unexpected message; " + string);
        } else {
          int number = Integer.parseInt(matcher.group(1));
          currentHost.addHelped(number);
        }
      }
    } else if (string.startsWith(Resources.get("plugin.battle.attacks", getLocale(), false))) {
      // "Attacke gegen: Heer 1(orks)";string
      // "Attacke gegen: Heer 1(orks), Heer 2(x), Heer 3(2j4e), Heer 4(das), Heer 5(przp), Heer 6(fust), Heer 7(Lord),
      // Heer 8(Luft), Heer 9(va2z), Heer 10(-?-), Heer 11(mu47), Heer 12(bart)";string
      for (String hostPart : string.substring(string.indexOf(":") + 1).split(",")) {
        Matcher matcher = hostPattern1.matcher(hostPart);
        boolean matched = matcher.matches();
        if (!matched) {
          log.warn("unexpected message; " + string);
        } else {
          int number = Integer.parseInt(matcher.group(1));
          currentHost.addAttacked(number);
        }
      }
    } else if (unitPattern.matcher(string).matches()) {
      // " + Tatarische Fremdenlegion (t7z9), 17 Dämonen, vorne (erschöpft), hat: 17 Schilde, 24 Schwerter.";string
      // " - Wowukh (ru8y), 2 Trolle, flieht (schwer verwundet, hungert), hat: 2 Bihänder.";string
      // " + Tatarische Fremdenlegion (zyju), 30 Goblins, vorne, bewacht die Region, hat: 2 Bögen, Weihrauch, Myrrhe, 4
      // Speere, 32 Schwerter.";string
      // " - Hellebardiere der Flaumfußens (m01j), 1 Bluthalbling, flieht (verwundet, hungert), hat: Streitross,
      // Hellebarde, Schuppenpanzer.";string
      // " * Tatarische Fremdenlegion (t1f1), 44 Goblins, vorne, bewacht die Region, Talente: Hiebwaffen 9, Ausdauer 5,
      // Reiten 1, hat: 35 Kettenhemden, 2044 Silber, 44 Schilde, 46 Schwerter, Turmschild.";string
      // " - Rächer des Bösen (7qqu), 7 Skelette, defensiv (schwer verwundet)."
      // " + Tatarische Fremdenlegion (bdjd), 5 Orks, vorne, bewacht die Region, hat: 5 Kriegsäxte, 2 Kettenhemden, 5
      // Schilde, Schwert."
      // " - Delok (o1), 1 Ork, vorne, hat: Pferd, Speer, 2 Phiolen."
      // " - Mogotk (ut3t), 1 Ork, hinten (schwer verwundet), hat: Bogen."
      Matcher matcher = unitPattern.matcher(string);
      boolean matched = matcher.matches();
      if (!matched) {
        log.warn("unexpected message");
      }
      UnitInfo unitInfo = getUnit(UnitID.createUnitID(matcher.group(2), world.base));
      unitInfo.setName(matcher.group(1).trim());
      unitInfo.setPersons(Integer.parseInt(matcher.group(3)));
      unitInfo.setRace(matcher.group(4).trim());
      try {
        if (matcher.group(5) != null) {
          unitInfo.setHero(matcher.group(5).trim().length() > 0);
        }
        if (matcher.group(6) != null) {
          unitInfo.setCombatStatus(matcher.group(6).trim());
          unitInfo.setRow(currentRow);
        }
        if (matcher.group(8) != null) {
          unitInfo.setHealth(matcher.group(8).trim());
        }
        if (matcher.group(10) != null) {
          unitInfo.setStarving(matcher.group(10).trim().length() > 0);
        }
        if (matcher.group(11) != null) {
          unitInfo.setGuarding(matcher.group(11).trim().length() > 0);
        }
        if (matcher.group(12) != null) {
          for (String skill : matcher.group(12).substring(matcher.group(12).indexOf(":") + 1)
              .split(",")) {
            Matcher skillMatcher = skillPattern.matcher(skill);
            if (skillMatcher.matches()) {
              unitInfo.addSkill(skillMatcher.group(1), Integer.parseInt(skillMatcher.group(2)));
            }
          }
        }
        if (matcher.group(15) != null) {
          for (String item : matcher.group(15).substring(matcher.group(15).indexOf(":") + 1).split(
              ",")) {
            Matcher itemMatcher = itemPattern.matcher(item.trim());
            if (itemMatcher.matches()) {
              // itemPattern = Pattern.compile(" *(([0-9]+) )*(\\p{L}[^0-9]*[^ ]) *"); // \p{L}:
              // letter
              if (itemMatcher.group(2) != null) {
                unitInfo.addItem(itemMatcher.group(3), Integer.parseInt(itemMatcher.group(2)));
              } else {
                unitInfo.addItem(itemMatcher.group(3), 1);
              }
            }
          }
        }
      } finally {
        unitInfo.setHost(currentHost.getIndex());
        currentHost.addUnit(unitInfo.getUnit(), unitInfo);
      }
    } else if (hostPattern2.matcher(string).matches()) {
      // "Heer 0(phos): 17, Heer 1(orks): 0+0+0+2";string
      // "Heer 0(-?-): 2, Heer 1(orks): 335+233+10+10, Heer 2(x): 295+155+10, Heer 3(2j4e): 0+412+0+25, Heer 4(das):
      // 496+19+113+11, Heer 5(przp): 106+91, Heer 6(fust): 46+32, Heer 7(Lord): 202+100, Heer 8(Luft): 79+4+1, Heer
      // 9(va2z): 179+1, Heer 10(-?-): 0+0+0+1, Heer 11(mu47): 54+1, Heer 12(bart): 275+74+40, Heer 13(-?-): 1, Heer
      // 14(-?-): 1, Heer 15(haLb): 250+63, Heer 16(stds): 573+18+3+3, Heer 17(-?-): 219+53+0+1, Heer 18(-?-): 20+100,
      // Heer 19(drac): 157+249+1, Heer 20(ouLe): 268+348+0+5, Heer 21(yjap): 163+86+20+2, Heer 22(-?-): 34+0+0+2, Heer
      // 23(tata): 400+102, Heer 24(-?-): 703+83, Heer 25(cat): 0+1, Heer 26(pt6f): 44, Heer 27(fLos): 0+21, Heer
      // 28(1akL): 13, Heer 29(phos): 0+20, Heer 30(isb9): 33";string
      for (String hostPart : string.split(",")) {
        Matcher matcher = overviewPattern.matcher(hostPart);
        boolean matched = matcher.matches();
        if (!matched) {
          log.warn("unexpected message");
        }
        int number = Integer.parseInt(matcher.group(1));
        for (int i = 3; i < matcher.groupCount() + 1; i += 2) {
          if (matcher.group(i) != null && matcher.group(i).length() > 0) {
            hosts.get(number).setRow(currentRound, i / 2 - 1, Integer.parseInt(matcher.group(i)));
          }
        }
      }
    } else {
      log.warn("unknown pattern: " + string);
    }

  }

  private void addError(Message m, RuntimeException e) {
    errors.add(m.getText() + "\n" + e);
  }

  private HostInfo addHost(Message m) {
    String sIndex = m.getAttribute("index");
    // "Phosphoros Sancti (phos)";name
    String name = m.getAttribute("name");
    return addHost(m, sIndex, name);
  }

  private HostInfo addHost(Message m, String sIndex, String name) {
    int index = Integer.parseInt(sIndex);
    HostInfo messageHost = hosts.get(index);
    if (messageHost == null) {
      messageHost = new HostInfo();
    }
    messageHost.setIndex(index);

    if (name.matches(".*\\([a-zL0-9?-]+\\)")) {
      EntityID id =
          EntityID.createEntityID(name.substring(name.lastIndexOf('(') + 1, name.lastIndexOf(')')),
              world.base);
      messageHost.setId(id);
      messageHost.setName(name);
    } else {
      if (messageHost.getId() == null) {
        messageHost.setId(EntityID.createEntityID(-1, world.base));
      }
      if (messageHost.getName() == null) {
        messageHost.setName(name);
      }
    }

    return messageHost;
  }

  private void addStarter(Message m) {
    // "Phosphoros Sancti (phos)";factions
    String messageFactions = m.getAttribute("factions");
    addStarter(m, messageFactions);
  }

  private void addStarter(Message m, String messageFactions) {
    if (messageFactions != null) {
      for (String faction : messageFactions.split(Resources.get(
          "plugin.battle.pattern.factionundfaction", getLocale(), false))) {
        int und = faction.lastIndexOf("und");
        if (und > 0
            && (faction.substring(0, und).matches(".*\\(([^)]{1,4})\\) ") || faction.substring(0,
                und).matches(
                    Resources.get("plugin.battle.pattern.anunknownfaction", getLocale(), false)))) {
          addStarter(faction.substring(0, und));
          addStarter(faction.substring(und + 4));
        } else {
          addStarter(faction);
        }
      }
    }
  }

  private void addStarter(String faction) {
    Pattern pattern = Pattern.compile(" *(.+) *\\(([^)]{1,4})\\) *");
    Matcher matcher = pattern.matcher(faction);
    if (matcher.matches()) {
      EntityID fact = findFaction(matcher.group(1));
      if (fact != null) {
        started.add(fact);
      } else {
        started.add(EntityID.createEntityID(matcher.group(2), world.base));
      }
    } else {
      started.add(EntityID.createEntityID(-1, world.base));
    }
  }

  private SpellInfo addCatapult(Message m) {

    UnitID unit = getUnit(m);
    SpellID id = new SpellID(unit, currentRound);
    SpellInfo spell = spells.get(id);
    if (spell == null) {
      spell = new SpellInfo();
      spells.put(id, spell);
    }
    if (m.getMessageType().getID().intValue() == 1281520191) {
      spell.setName(Resources.get("plugin.battle.spell.sword"));
    } else if (m.getMessageType().getID().intValue() == 881122508) {
      spell.setName(Resources.get("plugin.battle.spell.catapult"));
    } else if (m.getMessageType().getID().intValue() == 1399004700) {
      spell.setName(Resources.get("plugin.battle.spell.catapult"));
    } else {
      spell.setName(Resources.get("plugin.battle.spell.specialweapon"));
    }

    getUnit(unit);

    spell.setUnit(unit);

    spell.setRound(currentRound);
    spell.setCompleted(true);

    if (m.getAttribute("dead") != null) {
      spell.setDead(Integer.parseInt(m.getAttribute("dead")));
    }

    return spell;
  }

  private SpellInfo addSpell(Message m, boolean success) {
    String name = m.getAttribute("spell");
    if (name == null) {
      name = Resources.get("plugin.battle.spell.unknown");
    }
    return addSpell(m, name, success);
  }

  private SpellInfo addSpell(Message m, String name, boolean success) {
    UnitID unit;
    if (m.getAttribute("unit") != null) {
      unit = getUnit(m);
    } else if (m.getAttribute("mage") != null) {
      unit = getUnit(m, "mage");
    } else {
      unit = UnitID.createUnitID(-1, world.base);
    }
    SpellID id = new SpellID(unit, currentRound);
    SpellInfo spell = spells.get(id);
    if (spell == null) {
      spell = new SpellInfo();
      spells.put(id, spell);
    }

    spell.setName(name);

    getUnit(unit);

    spell.setUnit(unit);

    spell.setRound(currentRound);
    spell.setCompleted(success);

    if (m.getAttribute("amount") != null) {
      spell.setAmount(Integer.parseInt(m.getAttribute("amount")));
    }
    if (m.getAttribute("dead") != null) {
      spell.setDead(Integer.parseInt(m.getAttribute("dead")));
    }

    return spell;
  }

  private UnitID getUnit(Message m) {
    return getUnit(m, "unit");
  }

  private UnitID getUnit(Message m, String key) {
    return UnitID.createUnitID(m.getAttribute(key), 10, world.base);
    // unit.substring(unit.lastIndexOf('(') + 1, unit .lastIndexOf(')'))
  }

  protected UnitInfo getUnit(UnitID id) {
    UnitInfo info = allUnits.get(id);
    if (info == null) {
      info = new UnitInfo();
      info.setUnit(id);
      allUnits.put(id, info);
    }
    return info;
  }

  private EntityID findFaction(String faction) {
    return factions.get(faction.trim());
  }

  /**
   * Returns the value of coordinate.
   *
   * @return Returns coordinate.
   */
  public CoordinateID getCoordinate() {
    return coordinate;
  }

  /**
   * Sets the value of coordinate.
   *
   * @param coordinate The value for coordinate.
   */
  public void setCoordinate(CoordinateID coordinate) {
    this.coordinate = coordinate;
  }

  /**
   * Returns the value of maxRound.
   *
   * @return Returns maxRound.
   */
  public int getMaxRound() {
    return maxRound;
  }

  /**
   * Sets the value of maxRound.
   *
   * @param maxRound The value for maxRound.
   */
  public void setMaxRound(int maxRound) {
    this.maxRound = maxRound;
  }

  /**
   * Number of attacks per round for heroes
   */
  public int getHeroFactor() {
    return heroFactor;
  }

  /**
   * Number of attacks per round for heroes
   */
  public void setHeroFactor(int heroFactor) {
    this.heroFactor = heroFactor;
  }

  /**
   * Returns the value of locale.
   *
   * @return Returns locale.
   */
  public Locale getLocale() {
    return locale;
  }

  /**
   * Sets the value of locale.
   *
   * @param locale The value for locale.
   */
  public void setLocale(Locale locale) {
    this.locale = locale;
  }

  static class HitInfo {
    int minHits;
    int maxHits;
    int avgHits;
    int minKills;
    int maxKills;
    int avgKills;
    int minStrikes;
    int maxStrikes;
    int avgStrikes;

    public void add(HitInfo hits) {
      minHits += hits.minHits;
      minKills += hits.minKills;
      minStrikes += hits.minStrikes;
      avgHits += hits.avgHits;
      avgKills += hits.avgKills;
      avgStrikes += hits.avgStrikes;
      maxHits += hits.maxHits;
      maxKills += hits.maxKills;
      maxStrikes += hits.maxStrikes;
    }
  }

  interface Builder {
    public Builder append(String text);

    public Builder append(Object object);

    public void setLevel(int newLevel);

    public int getLevel();

    public void setObject(Object object);
  }

  class MyStringBuilder implements Builder {
    StringBuilder builder;
    private int currentLevel;

    public MyStringBuilder() {
      builder = new StringBuilder();
      currentLevel = 0;
    }

    public Builder append(String text) {
      builder.append(text);
      return this;
    }

    public Builder append(Object object) {
      builder.append(object);
      return this;
    }

    @Override
    public String toString() {
      return builder.toString();
    }

    public void setLevel(int newLevel) {
      for (int l = 0; l < newLevel; ++l) {
        builder.append("*");
      }
      currentLevel = newLevel;
    }

    public int getLevel() {
      return currentLevel;
    }

    public void setObject(Object object) {
      // ignore
    }

  }

  abstract class IncrementalBuilder implements Builder {
    // private ArrayList<DefaultMutableTreeNode> roots;
    // private NodeWrapperFactory nodeWrapperFactory;
    private StringBuilder currentText = new StringBuilder();

    // private int currentLevel;
    // private Pattern linePattern;
    // private int explicitLevel;

    public IncrementalBuilder append(String text2) {
      currentText.append(text2);
      int start = 0;
      int end = currentText.indexOf("\n");
      while (end > -1) {
        String line = currentText.substring(start, end);
        start = end + 1;
        addLine(line);

        end = currentText.indexOf("\n", start);
      }
      currentText.delete(0, start);
      return this;
    }

    protected abstract void addLine(String line);

    public IncrementalBuilder append(Object o) {
      return append(o.toString());
    }

    public void setObject(Object object) {
      // ignore
    }
  }

  class TreeBuilder extends IncrementalBuilder {

    private ArrayList<DefaultMutableTreeNode> roots;
    private NodeWrapperFactory nodeWrapperFactory;
    private int currentLevel;
    private DefaultMutableTreeNode lastNode;
    private DefaultMutableTreeNode currentRoot;
    private Object object;

    public TreeBuilder(DefaultMutableTreeNode parent) {
      roots = new ArrayList<DefaultMutableTreeNode>(2);
      roots.add(parent);
      currentRoot = parent;
      lastNode = parent;
      currentLevel = 0;
      nodeWrapperFactory = new NodeWrapperFactory(new Properties());
    }

    /**
     * Returns a simple node
     */
    private DefaultMutableTreeNode createSimpleNode(Object obj, String text, String icons) {
      return new DefaultMutableTreeNode(nodeWrapperFactory
          .createSimpleNodeWrapper(obj, text, icons));
    }

    public void setLevel(int newLevel) {
      if (newLevel > currentLevel) {
        roots.add(lastNode);
        currentRoot = lastNode;
        for (int l = currentLevel + 1; l < newLevel; ++l) {
          roots.add(null);
        }
      } else if (newLevel < currentLevel) {
        for (int l = currentLevel; l > newLevel; --l) {
          roots.remove(l);
        }
        for (int l = newLevel; l >= 0; --l) {
          currentRoot = roots.get(l);
          if (roots.get(l) != null) {
            break;
          }
        }
      }
      currentLevel = newLevel;
    }

    @Override
    public void setObject(Object object) {
      this.object = object;
    }

    @Override
    protected void addLine(String line) {
      currentRoot.add(lastNode = createSimpleNode(object, line, null));
      object = null;
    }

    public int getLevel() {
      return currentLevel;
    }

  }

  class HtmlBuilder extends IncrementalBuilder {

    private StringBuilder htmlText;
    private int currentLevel;

    public HtmlBuilder() {
      htmlText = new StringBuilder();
      currentLevel = 0;
    }

    public void setLevel(int newLevel) {
      if (newLevel > currentLevel) {
        for (int l = currentLevel; l < newLevel; ++l) {
          for (int i = 0; i < currentLevel; ++i) {
            htmlText.append(" ");
          }
          htmlText.append("<ul>\n");
        }
      } else if (newLevel < currentLevel) {
        for (int l = currentLevel; l > newLevel; --l) {
          for (int i = 0; i < currentLevel - 1; ++i) {
            htmlText.append(" ");
          }
          htmlText.append("</ul>\n");
        }
      }
      currentLevel = newLevel;
    }

    @Override
    protected void addLine(String line) {
      for (int i = 0; i < currentLevel; ++i) {
        htmlText.append(" ");
      }
      htmlText.append("<li>").append(line).append("</li>\n");
    }

    @Override
    public String toString() {
      StringBuilder tmpBuilder = new StringBuilder();
      tmpBuilder.append(htmlText);
      // tmpBuilder.append(currentText.toString());
      for (int l = currentLevel; l > 0; --l) {
        for (int i = 0; i < l - 1; ++i) {
          tmpBuilder.append(" ");
        }
        tmpBuilder.append("</ul>\n");
      }
      return tmpBuilder.toString();
    }

    public int getLevel() {
      return currentLevel;
    }
  }

  /** Computes aggregate info about a battle */
  public class Evaluator {

    protected Map<String, Integer> statuses;

    protected int dead[][];
    protected int fled[][];
    protected int alive[][];

    /** side, round, row */
    protected int rows[][][];

    protected int catapultVictims[];
    protected int spellVictims[];
    protected int healed[];
    protected int revived[];
    protected int raised[];

    protected Set<UnitInfo> mages[];
    protected int cast[];
    protected int failed[];

    protected boolean tacticWon[];
    protected boolean tacticLost[];

    protected HashMap<ItemType, HitInfo> sideHits[];
    protected int[] hasAccuracyData;

    Map<ItemType, Item> loot[];

    private boolean[] killedAll;

    @SuppressWarnings("unchecked")
    void init() {
      statuses = new LinkedHashMap<String, Integer>();
      statuses.put(Resources.get("plugin.battle.tostring.aggressive", getLocale(), true), 0);
      statuses.put(Resources.get("plugin.battle.tostring.front", getLocale(), true), 0);
      statuses.put(Resources.get("plugin.battle.tostring.rear", getLocale(), true), 1);
      statuses.put(Resources.get("plugin.battle.tostring.defensive", getLocale(), true), 1);
      statuses.put(Resources.get("plugin.battle.tostring.notfighting", getLocale(), true), 2);
      statuses.put(Resources.get("plugin.battle.tostring.fleeing", getLocale(), true), 3);

      dead = new int[sides.size()][hosts.size() + 1];
      fled = new int[sides.size()][hosts.size() + 1];
      alive = new int[sides.size()][hosts.size() + 1];

      rows = new int[sides.size()][MAX_ROUND + 3][MAX_ROWS]; // MAX_ROUND+precombat+tactic+after

      killedAll = new boolean[sides.size()];

      catapultVictims = new int[sides.size()];
      spellVictims = new int[sides.size() * 2];
      healed = new int[sides.size()];
      revived = new int[sides.size()];
      raised = new int[sides.size()];
      mages = new HashSet[sides.size()];
      for (int side = 0; side < sides.size(); ++side) {
        mages[side] = new HashSet<BattleInfo.UnitInfo>();
      }
      cast = new int[sides.size()];
      failed = new int[sides.size()];
      tacticWon = new boolean[sides.size()];
      tacticLost = new boolean[sides.size()];
      sideHits = new HashMap[sides.size()];
      for (int side = 0; side < sides.size(); ++side) {
        sideHits[side] = new HashMap<ItemType, HitInfo>();
      }
      hasAccuracyData = new int[sides.size()];

      loot = new HashMap[sides.size()];
      for (int side = 0; side < sides.size(); ++side) {
        loot[side] = new HashMap<ItemType, Item>();
      }
    }

    /**
     * Computes the information about a battle.
     *
     * @return this object
     */
    public Evaluator evaluate() {
      init();

      int sideNum = 0;
      for (Set<Integer> side : sides) {
        for (SpellInfo info : spells.values()) {
          UnitInfo unitInfo = allUnits.get(info.getUnit());
          if (side.contains(unitInfo.getHost())) {
            // builder.append(info.getName() + " " + info.getUnit() + " " + info.getRound() + "\n");
            if (info.getName() == null) {
              log.warn("unnamed spell");
            }
            if (info.getName() != null
                && (info.getName().equals(Resources.get("plugin.battle.spell.catapult"))
                    || info.getName().equals(Resources.get("plugin.battle.spell.sword")) || info
                        .getName().equals(Resources.get("plugin.battle.spell.specialweapon")))) {
              catapultVictims[sideNum] += info.getDead();
            } else {
              mages[sideNum].add(unitInfo);
              cast[sideNum]++;
              if (!info.isCompleted()) {
                failed[sideNum]++;
              }
              if (info.getName() == null) {
                // ignore
              }
              if (info.getName().equals(Resources.get("plugin.battle.spell.heilung"))) {
                healed[sideNum] += info.getAmount();
              } else if (info.getName().equals(Resources.get("plugin.battle.spell.wiederbelebung"))) {
                revived[sideNum] += info.getAmount();
              } else if (info.getName().equals(Resources.get("plugin.battle.spell.untote_helden"))) {
                raised[sideNum] += info.getAmount();
              }
              if (info.getDead() > 0) {
                spellVictims[sideNum] += info.getDead();
              }
              if (info.getAmount() > 0) {
                spellVictims[sides.size() + sideNum] += info.getAmount();
              }
            }
          }
        }

        sideNum++;
      }

      sideNum = 0;
      for (Set<Integer> side : sides) {
        for (UnitID tacticID : tactics) {
          UnitInfo tacticInfo = getUnit(tacticID);
          if (side.contains(tacticInfo.getHost())) {
            tacticWon[sideNum] = true;
          } else {
            tacticLost[sideNum] = true;
          }
        }
        sideNum++;
      }

      sideNum = 0;
      for (Set<Integer> side : sides) {
        for (UnitInfo info : allUnits.values()) {
          if (side.contains(info.getHost())) {
            int row1 = getRow(info);
            rows[sideNum][0][MAX_ROWS - 1] += info.getPersons();
            rows[sideNum][0][row1] += info.getPersons();

            for (Item item : info.getLoot()) {
              if (!loot[sideNum].containsKey(item.getItemType())) {
                loot[sideNum].put(item.getItemType(), new Item(item.getItemType(), 0));
              }
              Item lootItem = loot[sideNum].get(item.getItemType());
              lootItem.setAmount(lootItem.getAmount() + item.getAmount());
            }
          }
        }
        sideNum++;
      }

      sideNum = 0;
      for (Set<Integer> side : sides) {
        for (int hostNum : side) {
          HostInfo hostInfo = hosts.get(hostNum);
          for (int round = 1; round < hostInfo.getRows().length; ++round) {
            int row = 0;
            for (int strength : hostInfo.getRows()[round]) {
              // rows[sideNum][round][MAX_ROWS] += strength;
              rows[sideNum][round][row++] += strength;
            }
          }
          dead[sideNum][0] += hostInfo.getDead();
          fled[sideNum][0] += hostInfo.getFled();
          alive[sideNum][0] += hostInfo.getSurvived();
        }
        killedAll[sideNum] = rows[sideNum][getMaxRound() + 2][MAX_ROWS - 1] == 0;

        sideNum++;
      }

      sideNum = 0;
      for (Set<Integer> side : sides) {
        for (UnitInfo info : allUnits.values()) {
          if (side.contains(info.getHost())) {
            dead[sideNum][info.getHost() + 1] += info.getFallen();
            fled[sideNum][info.getHost() + 1] += info.getRun();
            alive[sideNum][info.getHost() + 1] +=
                info.getAlive() >= 0 ? info.getAlive() : info.getPersons();
          }
        }
        sideNum++;
      }

      sideNum = 0;
      for (Set<Integer> side : sides) {
        for (UnitInfo info : allUnits.values()) {
          if (side.contains(info.getHost())) {
            if (info.getHits() >= 0) {
              hasAccuracyData[sideNum] += info.getPersons();
              addHits(info, sideHits[sideNum], mages[sideNum].contains(info), killedAll[sideNum],
                  tacticWon[sideNum]);
            }
          }
        }
        sideNum++;
      }

      return this;
    }

    private void addHits(UnitInfo info, HashMap<ItemType, HitInfo> hitsInOut, boolean isMage,
        boolean pKilledAll, boolean pTacticWon) {
      HashSet<Item> weapons = new HashSet<Item>();
      int totalWeapons = 0;

      for (Item item : info.getItems()) {
        if (item.getItemType().getCategory() != null
            && item.getItemType().getCategory().isDescendant(
                world.getRules().getItemCategory(EresseaConstants.C_WEAPONS))) {
          weapons.add(item);
          totalWeapons += item.getAmount();
        }
      }
      if (totalWeapons < info.getPersons()) {
        weapons.add(new Item(unknownItemType, info.getPersons() - totalWeapons));
        totalWeapons = info.getPersons();
      }
      if (isMage) {
        weapons.add(new Item(spellItemType, info.getPersons()));
        totalWeapons += info.getPersons();
      }
      for (Item item : weapons) {
        // FIXME use weapon frequencies (crossbows)
        if (!hitsInOut.containsKey(item.getItemType())) {
          hitsInOut.put(item.getItemType(), new HitInfo());
        }
        HitInfo hits = hitsInOut.get(item.getItemType());
        if (item.getAmount() == totalWeapons) {
          hits.minHits += info.hits;
          hits.avgHits += info.hits;
          hits.minKills += info.kills;
          hits.avgKills += info.kills;
          hits.minStrikes +=
              (info.getPersons() - info.getFallen() - info.getRun())
                  * (getMaxRound() - (pKilledAll ? 1 : 0)) * (info.isHero() ? heroFactor : 1);
          hits.avgStrikes +=
              (info.getPersons() - (info.getFallen() + info.getRun()) / 2)
                  * (info.isHero() ? heroFactor : 1)
                  * ((getMaxRound() + (pTacticWon ? 1 : 0)) * 2 - (pKilledAll ? 1 : 0)) / 2;
        } else {
          hits.avgHits += (int) Math.round(item.getAmount() / (double) totalWeapons * info.hits);
          hits.avgKills += (int) Math.round(item.getAmount() / (double) totalWeapons * info.kills);
          hits.avgStrikes +=
              (int) Math.round(item.getAmount() / (double) totalWeapons
                  * (info.getPersons() - (info.getFallen() + info.getRun()) / 2)
                  * (info.isHero() ? heroFactor : 1)
                  * ((getMaxRound() + (pTacticWon ? 1 : 0)) * 2 - (pKilledAll ? 1 : 0)) / 2);
        }
        hits.maxHits += info.hits;
        hits.maxStrikes +=
            Math.min(item.getAmount(), info.getPersons()) * (getMaxRound() + (pTacticWon ? 1 : 0))
                * (info.isHero() ? heroFactor : 1);
        hits.maxKills += info.kills;
      }
    }

    private int getRow(UnitInfo info) {
      int row1 = info.getRow() - 1;
      Integer row2 = statuses.get(info.getCombatStatus());

      if (row2 == null) {
        log.warn("unit " + info.getName() + " unknown combat status " + info.getCombatStatus());
      } else if (row2 < 0 || row2 > 3) {
        log.warn("invalid row " + row2 + " for unit " + info.getName() + "(" + info.getUnit() + ")");
      } else if (row2 != row1) {
        log.warn("unit not where it should be " + info.getName() + "(" + info.getUnit() + ")");
      }

      if (row1 < 0 || row1 >= 4) {
        log.warn("invalid row " + row1 + " for unit " + info.getName() + "(" + info.getUnit() + ")");
        row1 = 4;
      }
      return row1;
    }

    @Override
    public String toString() {
      return toString(false);
    }

    /** Creates a simple string for the aggregated data */
    public String toString(boolean appendMessages) {
      MyStringBuilder builder = new MyStringBuilder();
      build(builder, false);
      return builder.toString();
    }

    /** Creates a html string for the aggregated data */
    public String toHtml(boolean appendMessages) {
      HtmlBuilder builder = new HtmlBuilder();
      build(builder, true);
      return builder.toString();
    }

    /** Creates a JTree for the aggregated data */
    public void toTree(DefaultMutableTreeNode parent, boolean appendMessages) {
      TreeBuilder builder = new TreeBuilder(parent);

      build(builder, true);
    }

    protected void build(Builder builder, boolean appendMessages) {
      // BATTLE IN XYZ
      builder.setLevel(1);
      builder.setObject(world.getRegion(coordinate));
      builder.append(
          Resources.get("plugin.battle.tostring.battle", world.getRegion(coordinate) != null
              ? world.getRegion(coordinate).toString() : ("??? " + coordinate))).append("\n");

      if (appendMessages) {
        // // REPORT
        builder.setLevel(2);
        builder.append(Resources.get("plugin.battle.tostring.report")).append("\n");
        // // MESSAGE
        builder.setLevel(3);
        for (Message m : battle.messages()) {
          builder.append(m.getText()).append("\n");
        }
      }

      int sideNum = 0;
      for (Set<Integer> side : sides) {
        // // SIDE X: 0+1+2+3=4
        builder.setLevel(2);
        builder.append(Resources.get("plugin.battle.tostring.side", sideNum)).append(": ");

        show(builder, rows[sideNum][0]);
        builder.append("; ");
        builder.append(dead[sideNum][0]).append(" ").append(
            Resources.get("plugin.battle.tostring.dead"));
        builder.append(", ").append(fled[sideNum][0]).append(" ").append(
            Resources.get("plugin.battle.tostring.fled"));
        builder.append(", ").append(alive[sideNum][0]).append(" ").append(
            Resources.get("plugin.battle.tostring.survived"));

        // int i = 0;
        // for (int hostNum : side) {
        // if (i++ != 0) {
        // builder.append(", ");
        // }
        // builder.append(Resources
        // .get("plugin.battle.tostring.host", String.format("%2d", hostNum)));
        // HostInfo hostInfo = hosts.get(hostNum);
        // builder.append("(").append(hostInfo.getAbbrev()).append(",").append(hostInfo.getId())
        // .append(")");
        // }
        builder.append("\n");

        builder.setLevel(3);
        builder.append(Resources.get("plugin.battle.tostring.overview", side.size())).append(": ");
        boolean first = true;
        for (int hostNum : side) {
          if (!first) {
            builder.append(", ");
          }
          builder.append(hosts.get(hostNum).getName());
          first = false;
        }
        builder.append("\n");

        for (int hostNum : side) {
          // //// HOST X: 1+2+3+4=5
          builder.setLevel(4);
          builder.append(Resources
              .get("plugin.battle.tostring.host", String.format("%2d", hostNum)));
          HostInfo hostInfo = hosts.get(hostNum);
          builder.append(": ").append(hostInfo.getName()).append(" (").append(hostInfo.getAbbrev())
              .append("): ");
          show(builder, hostInfo.getRows()[0]);
          builder.append("; ");
          builder.append(dead[sideNum][hostNum + 1]).append(" ").append(
              Resources.get("plugin.battle.tostring.dead"));
          builder.append(", ").append(fled[sideNum][hostNum + 1]).append(" ").append(
              Resources.get("plugin.battle.tostring.fled"));
          builder.append(", ").append(alive[sideNum][hostNum + 1]).append(" ").append(
              Resources.get("plugin.battle.tostring.survived"));
          builder.append("\n");

          builder.setLevel(5);
          first = true;
          builder.append(Resources.get("plugin.battle.attacks"));
          for (Integer num : hostInfo.getAttacked()) {
            if (first) {
              builder.append(" ");
              first = false;
            } else {
              builder.append(", ");
            }
            builder.append(Resources.get("plugin.battle.tostring.host", String.format("%2d", num)));
          }
          builder.append("\n");
          first = true;
          builder.append(Resources.get("plugin.battle.fights"));
          for (Integer num : hostInfo.getAdversaries()) {
            if (first) {
              builder.append(" ");
              first = false;
            } else {
              builder.append(", ");
            }
            builder.append(Resources.get("plugin.battle.tostring.host", String.format("%2d", num)));
          }
          builder.append("\n");
          first = true;
          builder.append(Resources.get("plugin.battle.helps"));
          for (Integer num : hostInfo.getHelped()) {
            if (first) {
              builder.append(" ");
              first = false;
            } else {
              builder.append(", ");
            }
            builder.append(Resources.get("plugin.battle.tostring.host", String.format("%2d", num)));
          }
          builder.append("\n");

          // ////// 1+2+3+4=5
          for (int round = 1; round < Math.min(getMaxRound() + 3, hostInfo.getRows().length); ++round) {
            if (round != 1 || !tactics.isEmpty()) {
              if (round == 0) {
                builder.append(Resources.get("plugin.battle.tostring.preround"));
              } else if (round == getMaxRound() + 2) {
                builder.append(Resources.get("plugin.battle.tostring.aftercombat", round - 1));
              } else {
                builder.append(Resources.get("plugin.battle.tostring.round", round - 1));
              }
              builder.append(" ");
              show(builder, hostInfo.getRows()[round]);
              builder.append("\n");
            }
          }
        }

        builder.setLevel(3);
        for (int round = 0; round < Math.min(getMaxRound() + 3, rows[sideNum].length); ++round) {
          // //// TACTIC LOST/WON to/by xyz
          if (round != 1 || !tactics.isEmpty()) {
            if (round == 1 && !tactics.isEmpty()) {
              for (UnitID tacticID : tactics) {
                UnitInfo tacticInfo = getUnit(tacticID);
                builder.setObject(world.getUnit(tacticID));
                if (side.contains(tacticInfo.getHost())) {
                  builder.append(
                      Resources.get("plugin.battle.tostring.tacticwon", tacticInfo.getName(),
                          tacticID)).append("\n");
                } else {
                  builder.append(
                      Resources.get("plugin.battle.tostring.tacticlost", tacticInfo.getName(),
                          tacticID)).append("\n");
                }
              }
            }
            // //// LINUP/BEFORE ROUND x/AFTER COMBAT 1+2+3+4=5
            if (round == 0) {
              builder.append(Resources.get("plugin.battle.tostring.preround"));
            } else if (round == getMaxRound() + 2) {
              builder.append(Resources.get("plugin.battle.tostring.aftercombat", round - 1));
            } else {
              builder.append(Resources.get("plugin.battle.tostring.round", round - 1));
            }
            show(builder, rows[sideNum][round]);
            builder.append("\n");

            if (round == 0) {
              int[][] stats = new int[statuses.size()][3];
              int r = 0;
              for (String status : statuses.keySet()) {
                for (UnitInfo unitInfo : allUnits.values()) {
                  if (side.contains(unitInfo.getHost())
                      && unitInfo.getCombatStatus().equals(status)) {
                    stats[r][0] += unitInfo.getFallen();
                    stats[r][1] += unitInfo.getRun();
                    stats[r][2] +=
                        unitInfo.getAlive() >= 0 ? unitInfo.getAlive() : unitInfo.getPersons();
                  }
                }
                ++r;
              }
              r = 0;
              for (String status : statuses.keySet()) {
                // ////// COMBAT STATUS
                boolean hasStatusNode = false;
                for (UnitInfo unitInfo : allUnits.values()) {
                  if (side.contains(unitInfo.getHost())
                      && unitInfo.getCombatStatus().equals(status)) {
                    if (!hasStatusNode) {
                      builder.setLevel(4);
                      builder.append(status);

                      builder.append(", ").append(stats[r][0]).append(" ").append(
                          Resources.get("plugin.battle.tostring.dead"));
                      builder.append(", ").append(stats[r][1]).append(" ").append(
                          Resources.get("plugin.battle.tostring.fled"));
                      builder.append(", ").append(stats[r][2]).append(" ").append(
                          Resources.get("plugin.battle.tostring.survived"));

                      builder.append("\n");
                      hasStatusNode = true;
                    }
                    // ////// UNIT abc 1 persons 1 dead 2 fled 3 survived etc.
                    builder.setLevel(5);
                    show(builder, unitInfo, mages[sideNum].contains(unitInfo), killedAll[sideNum],
                        tacticWon[sideNum]);
                  }
                }
                ++r;
              }
            }

            // ////// HOST x: 1+2+3+4=5
            for (int hostNum : side) {
              builder.setLevel(4);
              builder.append(
                  Resources.get("plugin.battle.tostring.host", String.format("%2d", hostNum)))
                  .append(": ");
              HostInfo hostInfo = hosts.get(hostNum);
              show(builder, hostInfo.getRows()[round]);
              builder.append("\n");
              if (round == 0) {
                int[][] stats = new int[statuses.size()][3];
                int r = 0;
                for (String status : statuses.keySet()) {
                  for (UnitInfo unitInfo : hostInfo.getUnits()) {
                    if (unitInfo.getCombatStatus().equals(status)) {
                      stats[r][0] += unitInfo.getFallen();
                      stats[r][1] += unitInfo.getRun();
                      stats[r][2] +=
                          unitInfo.getAlive() >= 0 ? unitInfo.getAlive() : unitInfo.getPersons();
                    }
                  }
                  ++r;
                }
                r = 0;
                for (String status : statuses.keySet()) {
                  // ////// COMBAT STATUS
                  boolean hasStatusNode = false;
                  for (UnitInfo unitInfo : hostInfo.getUnits()) {
                    if (unitInfo.getCombatStatus().equals(status)) {
                      if (!hasStatusNode) {
                        builder.setLevel(5);
                        builder.append(status);

                        builder.append(", ").append(stats[r][0]).append(" ").append(
                            Resources.get("plugin.battle.tostring.dead"));
                        builder.append(", ").append(stats[r][1]).append(" ").append(
                            Resources.get("plugin.battle.tostring.fled"));
                        builder.append(", ").append(stats[r][2]).append(" ").append(
                            Resources.get("plugin.battle.tostring.survived"));

                        builder.append("\n");
                        hasStatusNode = true;
                      }
                      // ////// UNIT abc 1 persons 1 dead 2 fled 3 survived
                      builder.setLevel(6);
                      show(builder, unitInfo, mages[sideNum].contains(unitInfo),
                          killedAll[sideNum], tacticWon[sideNum]);
                    }
                  }
                  ++r;
                }
              }
            }
            builder.setLevel(3);
          }
        }
        // //// 1 dead 1 fled 1 survived
        builder.append(dead[sideNum][0]).append(" ").append(
            Resources.get("plugin.battle.tostring.dead"));
        builder.append(", ").append(fled[sideNum][0]).append(" ").append(
            Resources.get("plugin.battle.tostring.fled"));
        builder.append(", ").append(alive[sideNum][0]).append(" ").append(
            Resources.get("plugin.battle.tostring.survived"));
        builder.append("\n");

        // ////// host x: 1 dead 2 fled 3 survived
        builder.setLevel(4);
        for (int hostNum : side) {
          builder.append(
              Resources.get("plugin.battle.tostring.host", String.format("%2d", hostNum))).append(
                  ": ");
          builder.append(dead[sideNum][hostNum + 1]).append(" ").append(
              Resources.get("plugin.battle.tostring.dead"));
          builder.append(", ").append(fled[sideNum][hostNum + 1]).append(" ").append(
              Resources.get("plugin.battle.tostring.fled"));
          builder.append(", ").append(alive[sideNum][hostNum + 1]).append(" ").append(
              Resources.get("plugin.battle.tostring.survived"));
          builder.append("\n");
        }

        // //// 0 healed
        builder.setLevel(3);
        if (healed[sideNum] > 0) {
          builder.append(Resources.get("plugin.battle.tostring.healed", healed[sideNum]));
          builder.append("\n");
        }
        if (revived[sideNum] > 0) {
          builder.append(Resources.get("plugin.battle.tostring.revived", revived[sideNum]));
          builder.append("\n");
        }
        if (raised[sideNum] > 0) {
          builder.append(Resources.get("plugin.battle.tostring.raised", raised[sideNum]));
          builder.append("\n");
        }
        if (catapultVictims[sideNum] > 0) {
          builder.append(Resources.get("plugin.battle.tostring.catapultvictims",
              catapultVictims[sideNum]));
          builder.append("\n");
        }
        if (spellVictims[sideNum] > 0 || mages[sideNum].size() > 0 || failed[sideNum] > 0
            || cast[sideNum] > 0) {
          builder.append(Resources.get("plugin.battle.tostring.spellvictims",
              spellVictims[sideNum], mages[sideNum].size(), failed[sideNum], cast[sideNum],
              spellVictims[sides.size() + sideNum]));
          builder.append("\n");
          for (UnitInfo mage : mages[sideNum]) {
            builder.setLevel(4);
            show(builder, mage, true, killedAll[sideNum], tacticWon[sideNum]);
            for (SpellInfo spellInfo : spells.values()) {
              if (spellInfo.getUnit().equals(mage.getUnit())) {
                builder.setLevel(5);
                builder.append(Resources.get("plugin.battle.tostring.spellround", spellInfo
                    .getRound(), spellInfo.getName()));
                if (spellInfo.getAmount() > 0) {
                  builder.append(" ").append(
                      Resources.get("plugin.battle.tostring.spellamount", spellInfo.getAmount()));
                }
                if (spellInfo.getDead() > 0) {
                  builder.append(" ").append(
                      Resources.get("plugin.battle.tostring.spelldead", spellInfo.getDead()));
                }
                if (!spellInfo.isCompleted()) {
                  builder.append(" ").append(Resources.get("plugin.battle.tostring.fumble"));
                }
                builder.append("\n");
              }
            }
          }
        }

        builder.setLevel(3);
        if (sideHits[sideNum].size() == 0) {
          // //// no accuracy data
          builder.append(Resources.get("plugin.battle.tostring.nohits"));
          builder.append("\n");
        } else {
          // //// accuracy data (min/avg/max)
          builder.append(Resources.get("plugin.battle.tostring.hitdata", hasAccuracyData[sideNum],
              rows[sideNum][0][rows[sideNum][0].length - 1]));
          builder.append("\n");

          // ////// hits: 1/2/3, kills: 1/2/3, strikes 1/2/3, accuracy: 1%/2%/3%
          builder.setLevel(4);
          HitInfo total = new HitInfo();
          HitInfo front = new HitInfo();
          HitInfo rear = new HitInfo();
          Set<ItemType> frontWeapons = new HashSet<ItemType>();
          Set<ItemType> rangedWeapons = new HashSet<ItemType>();
          for (Entry<ItemType, HitInfo> entry : sideHits[sideNum].entrySet()) {
            HitInfo hits = entry.getValue();
            builder.append(entry.getKey()).append(":");
            if (entry.getKey().getCategory() != null) {
              if (entry.getKey().getCategory().isDescendant(
                  world.getRules().getItemCategory(EresseaConstants.C_RANGED_WEAPONS))) {
                rear.add(hits);
                rangedWeapons.add(entry.getKey());
              } else if (entry.getKey().getCategory().isDescendant(
                  world.getRules().getItemCategory(EresseaConstants.C_WEAPONS))) {
                front.add(hits);
                frontWeapons.add(entry.getKey());
              }
            }

            total.add(hits);
            show(builder, hits);
          }
          if (sideHits[sideNum].entrySet().size() > 1) {
            StringBuilder frontString = new StringBuilder(" (");
            for (ItemType type : frontWeapons) {
              if (frontString.length() > 2) {
                frontString.append(", ");
              }
              frontString.append(type.getName());
            }
            frontString.append(")");
            builder.append(Resources.get("plugin.battle.tostring.melee")).append(
                frontString.toString()).append(": ");
            show(builder, front);

            StringBuilder rangedString = new StringBuilder(" (");
            for (ItemType type : rangedWeapons) {
              if (rangedString.length() > 2) {
                rangedString.append(", ");
              }
              rangedString.append(type.getName());
            }
            rangedString.append(")");
            builder.append(Resources.get("plugin.battle.tostring.ranged")).append(
                rangedString.toString()).append(": ");
            show(builder, rear);

            builder.append(Resources.get("plugin.battle.tostring.total")).append(": ");
            show(builder, total);
          }
        }

        if (!loot[sideNum].isEmpty()) {
          // //// LOOT
          builder.setLevel(3);
          builder.append(Resources.get("plugin.battle.tostring.loot")).append("\n");
          // ////// ITEM: amount
          builder.setLevel(4);
          for (Item item : loot[sideNum].values()) {
            builder.append(item.getItemType()).append(": ").append(item.getAmount()).append("\n");
          }
        }

        sideNum++;
      }

      if (errors.size() > 0) {
        // // ERRORS
        builder.setLevel(2);
        builder.append("Errors:\n");
        // //// errors
        builder.setLevel(3);
        for (String error : errors) {
          builder.append(error);
          builder.append("\n");
        }
      }

    }

    private void show(Builder builder, int[] pRows) {
      int row = 0;
      for (int strength : pRows) {
        if (row + 1 == MAX_ROWS) {
          builder.append(" = ");
        } else if (row > 0) {
          builder.append(" + ");
        }
        builder.append(String.format("%5d", strength));
        row++;
      }
    }

    private void show(Builder builder, UnitInfo unitInfo, boolean isMage, boolean pKilledAll,
        boolean pTacticWon) {
      builder.setObject(world.getUnit(unitInfo.unit));
      builder.append(unitInfo.getName()).append("(").append(unitInfo.unit).append("), ");
      builder.append(unitInfo.getPersons()).append(" ").append(unitInfo.getRace());
      if (unitInfo.isHero()) {
        builder.append(", ").append(Resources.get("plugin.battle.hero"));
      }
      if (isMage) {
        builder.append(", ").append(Resources.get("plugin.battle.tostring.mage"));
      }
      builder.append(", ").append(unitInfo.getFallen()).append(" ").append(
          Resources.get("plugin.battle.tostring.dead"));
      builder.append(", ").append(unitInfo.getRun()).append(" ").append(
          Resources.get("plugin.battle.tostring.fled"));
      builder.append(", ").append(
          unitInfo.getAlive() >= 0 ? unitInfo.getAlive() : unitInfo.getPersons()).append(" ")
          .append(Resources.get("plugin.battle.tostring.survived"));
      if (unitInfo.getHits() >= 0) {
        builder.append(", ").append(unitInfo.getHits()).append(" ").append(
            Resources.get("plugin.battle.tostring.hits"));
        builder.append(", ").append(unitInfo.getKills()).append(" ").append(
            Resources.get("plugin.battle.tostring.kills"));
      }
      if (!unitInfo.getSkills().isEmpty()) {
        boolean first = true;
        builder.append(", ").append(Resources.get("plugin.battle.skills"));
        for (Skill skill : unitInfo.getSkills()) {
          if (first) {
            builder.append(" ");
            first = false;
          } else {
            builder.append(", ");
          }
          builder.append(skill);
        }
      }
      if (!unitInfo.getItems().isEmpty()) {
        boolean first = true;
        builder.append(", ").append(Resources.get("plugin.battle.has"));
        for (Item item : unitInfo.getItems()) {
          if (first) {
            builder.append(" ");
            first = false;
          } else {
            builder.append(", ");
          }
          builder.append(item.getAmount()).append(" ").append(item.getItemType());
        }
      }
      builder.append("\n");

      if (unitInfo.getHits() >= 0) {
        builder.setLevel(builder.getLevel() + 1);
        HashMap<ItemType, HitInfo> unitHits = new HashMap<ItemType, BattleInfo.HitInfo>();
        addHits(unitInfo, unitHits, isMage, pKilledAll, pTacticWon);

        HitInfo total = new HitInfo();
        for (Entry<ItemType, HitInfo> entry : unitHits.entrySet()) {
          HitInfo hits = entry.getValue();
          builder.append(entry.getKey()).append(":");
          total.minHits += hits.minHits;
          total.minKills += hits.minKills;
          total.minStrikes += hits.minStrikes;
          total.avgHits += hits.avgHits;
          total.avgKills += hits.avgKills;
          total.avgStrikes += hits.avgStrikes;
          total.maxHits += hits.maxHits;
          total.maxKills += hits.maxKills;
          total.maxStrikes += hits.maxStrikes;
          show(builder, hits);
        }
        if (unitHits.entrySet().size() > 1) {
          builder.append(Resources.get("plugin.battle.tostring.total")).append(": ");
          show(builder, total);
        }
        builder.setLevel(builder.getLevel() - 1);
      }
    }

    private void show(Builder builder, HitInfo hits) {
      builder.append(hits.minHits).append("/").append(hits.avgHits).append("/")
          .append(hits.maxHits).append(" ").append(Resources.get("plugin.battle.tostring.hits"));
      builder.append(", ").append(hits.minKills).append("/").append(hits.avgKills).append("/")
          .append(hits.maxKills).append(" ").append(Resources.get("plugin.battle.tostring.kills"));
      builder.append(", ").append(hits.minStrikes).append("/").append(hits.avgStrikes).append("/")
          .append(hits.maxStrikes).append(" ").append(
              Resources.get("plugin.battle.tostring.strikes"));
      builder.append("; ").append(
          String.format("%02.1f%%/%02.1f%%/%02.1f%%",
              100 * hits.minHits / (double) hits.maxStrikes, 100 * hits.avgHits
                  / (double) hits.avgStrikes, 100 * hits.maxHits / (double) hits.minStrikes));

      builder.append("\n");
    }

  }

  @Override
  public String toString() {
    return new Evaluator().evaluate().toString();
  }

  /**
   * Creates an HTML string with aggregated data of this battle.
   *
   * @return A string with HTML formatting (without surrounding <html> or <body> tags).
   */
  public String toHtml() {
    return new Evaluator().evaluate().toHtml(true);
  }

  /**
   * Creates a tree with aggregated data of thsi battle.
   *
   * @param parent all nodes are appended to this node
   */
  public void toTree(DefaultMutableTreeNode parent) {
    new Evaluator().evaluate().toTree(parent, true);
  }
}
