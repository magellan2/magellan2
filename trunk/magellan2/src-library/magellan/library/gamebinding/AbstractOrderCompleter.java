/*
 * Copyright (C) 2000-2004 Roger Butenuth, Andreas Gampe, Stefan Goetz, Sebastian Pappert, Klaas
 * Prause, Enno Rehling, Sebastian Tusk, Ulrich Kuester, Ilja Pavkovic This file is part of the
 * Eressea Java Code Base, see the file LICENSING for the licensing information applying to this
 * file.
 */

package magellan.library.gamebinding;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import magellan.library.Alliance;
import magellan.library.Border;
import magellan.library.Building;
import magellan.library.CoordinateID;
import magellan.library.Faction;
import magellan.library.GameData;
import magellan.library.ID;
import magellan.library.Item;
import magellan.library.LuxuryPrice;
import magellan.library.Named;
import magellan.library.Region;
import magellan.library.Ship;
import magellan.library.Skill;
import magellan.library.Spell;
import magellan.library.StringID;
import magellan.library.TempUnit;
import magellan.library.Unit;
import magellan.library.UnitContainer;
import magellan.library.completion.Completer;
import magellan.library.completion.CompleterSettingsProvider;
import magellan.library.completion.Completion;
import magellan.library.completion.OrderParser;
import magellan.library.rules.ItemCategory;
import magellan.library.rules.ItemType;
import magellan.library.rules.RegionType;
import magellan.library.rules.SkillType;
import magellan.library.utils.Locales;
import magellan.library.utils.OrderToken;
import magellan.library.utils.OrderTokenizer;
import magellan.library.utils.Regions;
import magellan.library.utils.Resources;
import magellan.library.utils.Umlaut;
import magellan.library.utils.Units;
import magellan.library.utils.logging.Logger;

/**
 * A class for offering possible completions on incomplete orders. This class relies on the
 * <tt>OrderParser</tt> for reading input which calls the cmpltX methods of this class when it
 * encounters an incomplete order and has a <tt>OrderCompleter</tt> object registered. A
 * <tt>OrderCompleter</tt> wraps itself around a <tt>OrderParser</tt> so you do not get involved
 * with any of the cmpltX methods. They are solely called by the internal <tt>OrderParser</tt>.
 */
public abstract class AbstractOrderCompleter implements Completer {
  private static final Logger log = Logger.getInstance(AbstractOrderCompleter.class);

  private static final Comparator<Completion> prioComp = new PrioComp();
  private OrderParser parser;
  protected List<Completion> completions;
  protected GameData data;
  protected Region region;
  protected Unit unit;
  protected CompleterSettingsProvider completerSettingsProvider;
  protected String oneQuote = "\"";
  protected String twoQuotes = "\"\"";
  protected String spaceQuotes = " \"\"";
  private List<OrderToken> parserTokens = null;

  /**
   * Returns the value of completerSettingsProvider.
   * 
   * @return Returns completerSettingsProvider.
   */
  protected CompleterSettingsProvider getCompleterSettingsProvider() {
    return completerSettingsProvider;
  }

  /**
   * Creates a new <tt>EresseaOrderCompleter</tt> taking context information from the specified
   * <tt>GameData</tt> object.
   * 
   * @param gd The <tt>GameData</tt> this completer uses as context.
   */
  public AbstractOrderCompleter(GameData gd, CompleterSettingsProvider ac) {
    completerSettingsProvider = ac;
    completions = new LinkedList<Completion>();
    data = gd;

    initParser();
  }

  protected abstract void initParser();

  /**
   * Parses the String cmd with Unit u as context and returns possible completions if the cmd is an
   * incomplete order.
   * 
   * @param u a <tt>Unit</tt> object taken as context information for the completion decisions.
   * @param cmd a <tt>String</tt> containing the (possibly incomplete) order to parse.
   * @return a <tt>List</tt> with possible completions of the given order. If there are no proposed
   *         completions this list is empty.
   */
  public List<Completion> getCompletions(Unit u, String cmd) {
    unit = u;
    region = unit.getRegion();
    if (region == null) {
      region = unit.getData().getNullRegion();
    }
    completions = new LinkedList<Completion>();
    // getParser().read(new StringReader(cmd));

    parserTokens = getParser().parse(cmd, u.getLocale()).getTokens();

    if ((parserTokens.size() > 1)
        && (parserTokens.get(parserTokens.size() - 2).ttype == OrderToken.TT_COMMENT))
      return Collections.emptyList();
    else
      return crop(completions, parserTokens);
  }

  /**
   * @see magellan.library.completion.Completer#getCompletions(magellan.library.Unit,
   *      java.lang.String, java.util.List)
   * @deprecated Use {@link #getCompletions(Unit, String)}
   */
  @Deprecated
  public List<Completion> getCompletions(Unit u, String line, List<Completion> old) {
    return this.getCompletions(u, line);
    // final List<OrderToken> tokens = getParser().parse(line, u.getLocale()).getTokens();
    // return crop(old, tokens);
  }

  protected OrderParser getParser() {
    return parser;
  }

  protected void setParser(OrderParser parser) {
    this.parser = parser;
  }

  protected void setQuote(char quote) {
    if (quote == '\'') {
      oneQuote = "'";
      twoQuotes = "''";
      spaceQuotes = " ''";
    } else if (quote == '"') {
      oneQuote = "\"";
      twoQuotes = "\"\"";
      spaceQuotes = " \"\"";
    } else {
      oneQuote = String.valueOf(quote);
      twoQuotes = "" + quote + quote;
      spaceQuotes = " " + twoQuotes;
    }
  }

  /**
   * Filters all Completion objects from list, that do not match the last word in txt, usually the
   * order entered so far.
   */
  public List<Completion> crop(List<Completion> list, List<OrderToken> tokens) {
    List<Completion> ret = new LinkedList<Completion>();
    int start = 0;
    final String stub = AbstractOrderCompleter.getStub(tokens);

    if (stub.length() > 0) {
      // filter list
      Collections.sort(list, new IgnrCsComp());
      start = Collections.binarySearch(list, stub, new IgnrCsComp());

      if (start == (-list.size() - 1))
        return ret;
      else {
        if (start < 0) {
          start = Math.abs(start) - 1;
        }

        final Iterator<Completion> it = list.listIterator(start);

        while (it.hasNext()) {
          final Completion elem = it.next();
          final String val = elem.getName();
          final int len = Math.min(stub.length(), val.length());

          if (val.substring(0, len).equalsIgnoreCase(stub)) {
            ret.add(elem);
          } else {
            break;
          }
        }
      }

    } else {
      // stub.length <= 0
      ret = list;
    }
    Collections.sort(ret, AbstractOrderCompleter.prioComp);

    Completion last = null;
    for (final Iterator<Completion> it = ret.iterator(); it.hasNext();) {
      final Completion current = it.next();
      if (current.equals(last)) {
        it.remove();
      }
      last = current;
    }

    return ret;
  }

  /**
   * Add a completion to the list of completions.
   */
  public void addCompletion(Completion completion) {
    completions.add(completion);
  }

  /**
   * Deletes all completions.
   */
  public void clear() {
    completions.clear();
  }

  /**
   * adds the given spells if combat, only adds combat-spells and so on
   * 
   * @param closing
   * @param opening
   */
  protected void addFilteredSpells(Unit u, boolean far, boolean ocean, boolean combat,
      String opening, String closing) {
    final Collection<Spell> spells = u.getSpells().values();
    for (Spell spell : spells) {
      if ((spell.getDescription() == null) // indicates that no information is available about this
          // spell
          || ((spell.getIsFar() || !far)
              && (spell.getOnOcean() || !ocean || u.getRace().equals(
                  data.rules.getRace(EresseaConstants.R_MEERMENSCHEN))) && (!combat ^ (spell
              .getType().toLowerCase().indexOf("combat") > -1)))) {
        final String spellName = data.getTranslation(spell);

        completions.add(new Completion(opening + spellName + closing));
      }
    }
  }

  protected void addFamilarSpells(Unit mage, Unit familar, String opening, String closing) {

    Skill magic = mage.getSkill(data.rules.getSkillType(EresseaConstants.S_MAGIE));
    if ((magic != null)
        && (Regions.getDist(mage.getRegion().getCoordinate(), familar.getRegion().getCoordinate()) <= magic
            .getLevel())) {
      // familar is in range
      int maxlevel = magic.getLevel() / 2;
      magic = familar.getSkill(data.rules.getSkillType(EresseaConstants.S_MAGIE));
      if (magic != null) {
        // maximum possible spelllevel:
        maxlevel = Math.min(maxlevel, magic.getLevel());

        for (final Spell spell : mage.getSpells().values()) {
          if ((spell.getDescription() == null) // indicates that no information is available about
              // this spell
              || (spell.getIsFamiliar() && (spell.getLevel() <= maxlevel))) {
            // seems to be a spell usable by a familar
            final String spellName = data.getTranslation(spell);

            completions.add(new Completion(spellName, opening + spellName + closing, " "));
          }
        }
      }
    } else {
      completions.add(new Completion("=== Magier nicht in Reichweite ===", "", ""));
    }
  }

  /** Add completions for command Zeige. */
  public void cmpltZeige() {
    addUnitItems("");
    completions.add(new Completion(getOrderTranslation(EresseaConstants.O_ALL), " ",
        Completion.DEFAULT_PRIORITY - 1));
  }

  /** Add completions for command ZeigeAlle. */
  public void cmpltZeigeAlle() {
    completions.add(new Completion(getOrderTranslation(EresseaConstants.O_POTIONS)));
    completions.add(new Completion(getOrderTranslation(EresseaConstants.O_SPELLS)));
  }

  /** Add completions for command Zerstoere. */
  public void cmpltZerstoere() {
    completions.add(new Completion(getOrderTranslation(EresseaConstants.O_ROAD), " "));
  }

  /** Add completions for command ZerstoereStrasse. */
  public void cmpltZerstoereStrasse() {
    if (region != null) {
      for (Border b : region.borders()) {
        if (Umlaut.convertUmlauts(b.getType()).equalsIgnoreCase(
            getOrderTranslation(EresseaConstants.O_ROAD))) {
          completions.add(new Completion(getOrderTranslation(directions[b.getDirection()]), ""));
        }
      }
    } else {
      addDirections("");
    }
  }

  /** Add completions for command Zuechte. */
  public void cmpltZuechte() {
    completions.add(new Completion(getOrderTranslation(EresseaConstants.O_HORSES)));
    completions.add(new Completion(getOrderTranslation(EresseaConstants.O_HERBS)));
  }

  /** Add completions for command Factions. */
  public void cmpltFactions(String postfix) {
    addFactions(postfix);
  }

  /**
   * adds all units in this region whose faction has a trustlevel not greater than zero (TL_DEFAULT)
   */
  protected void addEnemyUnits(String postfix) {
    if ((data != null) && (unit != null) && (region != null)) {
      for (Unit u : region.units()) {
        if ((u.getFaction() == null || u.getFaction().getTrustLevel() <= Faction.TL_DEFAULT)
            || u.isSpy()) {
          addUnit(u, postfix);
        }
      }
    }
  }

  /**
   * adds all units in this region, whose faction does not fit all of the alliances in the given
   * Alliance-Object. Example: Given Alliance contains help and give: units are added if they are
   * not allied both: help AND give. The reference-object is the faction of the current unit
   */
  protected void addNotAlliedUnits(Alliance alliance, String postfix) {
    for (Unit curUnit : region.units()) {
      final Faction f = curUnit.getFaction();

      // search for alliances
      if (f == null) {
        addUnit(curUnit, postfix);
      } else if (!f.equals(unit.getFaction())) {
        if (!Units.isAllied(f, curUnit.getFaction(), alliance.getState())) {
          addUnit(curUnit, postfix);
        }
      }
    }
  }

  /**
   * Adds all units in the region to the completions.
   */
  protected void addRegionUnits(String postfix, boolean omitTemp) {
    addRegionUnits(postfix, 0, omitTemp);
  }

  /**
   * Adds all units in the region to the completions.
   */
  protected void addRegionUnits(String postfix, int cursorOffset, boolean omitTemp) {
    if (region != null) {
      for (final Unit u : region.units()) {
        if (((unit == null) || !u.equals(unit)) && (!omitTemp || u instanceof TempUnit)) {
          addUnit(u, postfix, cursorOffset, omitTemp);
        }
      }
    }
  }

  /**
   * Add all the ships in the current region except <code>exclude</code> (which may be
   * <code>null</code>). Prefix them with <code>prefix</code>.
   */
  protected void addRegionShips(String prefix, String postfix, Ship exclude, boolean comment) {
    final Iterator<Ship> iter2 = region.ships().iterator();
    for (; iter2.hasNext();) {
      final UnitContainer uc = iter2.next();

      if (!uc.equals(exclude)) {
        completions.add(new Completion(uc.getName() + " (" + uc.getID() + ")", prefix + uc.getID()
            + (comment ? (" ;" + uc.getName()) : ""), postfix, Completion.DEFAULT_PRIORITY - 1));
        completions.add(new Completion(uc.getID() + " (" + uc.getName() + ")", prefix + uc.getID()
            + (comment ? (" ;" + uc.getName()) : ""), postfix, Completion.DEFAULT_PRIORITY));
      }
    }
  }

  /**
   * Add all the buildings in the current region except <code>exclude</code> (which may be
   * <code>null</code>). Prefix them with <code>prefix</code>.
   * 
   * @param postfix
   */
  void addRegionBuildings(String prefix, String postfix, Building exclude, boolean comment) {
    final Iterator<Building> iter1 = region.buildings().iterator();
    for (; iter1.hasNext();) {
      final UnitContainer uc = iter1.next();

      if (!uc.equals(exclude)) {
        completions.add(new Completion(uc.getName() + " (" + uc.getID() + ")", prefix + uc.getID()
            + (comment ? (" ;" + uc.getName()) : ""), postfix, Completion.DEFAULT_PRIORITY - 1));
        completions.add(new Completion(uc.getID() + " (" + uc.getName() + ")", prefix + uc.getID()
            + (comment ? (" ;" + uc.getName()) : ""), postfix, Completion.DEFAULT_PRIORITY));
      }
    }
  }

  protected void addRegionItemsFaction(String postfix, int minAmount) {
    if (region != null) {
      final Map<ItemType, Integer> items = new HashMap<ItemType, Integer>();
      for (final Unit actUnit : region.units()) {
        if (actUnit.getFaction() != null && actUnit.getFaction().equals(unit.getFaction())) {
          for (final Item actUnitItem : actUnit.getItems()) {
            final ItemType actItemType = actUnitItem.getItemType();
            if (items.containsKey(actItemType)) {
              // our List contains the ItemType already
              items.put(actItemType, Integer.valueOf((items.get(actItemType)).intValue()
                  + actUnitItem.getAmount()));
            } else {
              // new ItemType on our List
              items.put(actItemType, Integer.valueOf(actUnitItem.getAmount()));
            }
          }
        }
      }
      if (items.size() > 0) {
        for (final ItemType itemType : items.keySet()) {
          final int amount = items.get(itemType).intValue();
          if (amount >= minAmount) {
            completions.add(new Completion(itemType.getName() + " (" + amount + ")", itemType
                .getOrderName(), postfix));
          }
        }
      }
    }
  }

  protected void addRegionShipCommanders(String postfix) {
    addRegionShipCommanders(postfix, 0);
  }

  protected void addRegionShipCommanders(String postfix, int cursorOffset) {
    if (region != null) {
      final Iterator<Ship> ships = region.ships().iterator();
      while (ships.hasNext() == true) {
        final Ship s = ships.next();
        if (s != null) {
          final Unit u = s.getModifiedOwnerUnit();
          if (u != null) {
            if ((unit == null) || !u.equals(unit)) {
              addUnitContainerOwner(s, u, postfix, cursorOffset);
            }
          }
        }
      }
    }
  }

  protected void addUnitContainerOwner(UnitContainer s, Unit u, String postfix, int cursorOffset) {
    final String id = u.getID().toString();

    completions.add(new Completion(s.toString() + " (" + s.getID() + ")", id, postfix,
        Completion.DEFAULT_PRIORITY + 1, cursorOffset));
    completions.add(new Completion(s.getID() + " (" + s.toString() + ")", id, postfix,
        Completion.DEFAULT_PRIORITY + 2, cursorOffset));
  }

  protected void addRegionBuildingOwners(String postfix) {
    addRegionBuildingOwners(postfix, 0);
  }

  protected void addRegionBuildingOwners(String postfix, int cursorOffset) {
    if (region != null) {
      final Iterator<Building> buildings = region.buildings().iterator();
      while (buildings.hasNext() == true) {
        final Building b = buildings.next();
        if (b != null) {
          final Unit u = b.getModifiedOwnerUnit();
          if (u != null) {
            if ((unit == null) || !u.equals(unit)) {
              addUnitContainerOwner(b, u, postfix, cursorOffset);
            }
          }
        }
      }
    }
  }

  protected void addUnitItems(String postfix) {
    addUnitItems(0, postfix);
  }

  protected void addUnitItems(int amount, String postfix) {
    for (final Item i : unit.getItems()) {
      completions
          .add(new Completion(i.getOrderName(), i.getOrderName(), postfix,
              (i.getAmount() >= amount) ? Completion.DEFAULT_PRIORITY
                  : Completion.DEFAULT_PRIORITY + 1));
    }
  }

  protected void addFactionItems(String postfix) {
    addFactionItems(0, postfix);
  }

  protected void addFactionItems(int amount, String postfix) {
    for (final Item i : unit.getFaction().getItems()) {
      completions
          .add(new Completion(i.getOrderName(), i.getOrderName(), postfix,
              (i.getAmount() >= amount) ? Completion.DEFAULT_PRIORITY
                  : Completion.DEFAULT_PRIORITY + 1));
    }
  }

  /**
   * Adds all known factions to the completions with comment
   * 
   * @param postfix
   */
  protected void addFactions(String postfix) {
    if (data != null) {
      for (Faction f : data.getFactions()) {
        addNamed(f, postfix, 0, true);
      }
    }
  }

  /**
   * Adds all factions except the current unit's.
   * 
   * @param postfix appended to Completion
   * @param cursorOffset used for Completion
   * @param addComment if <code>true</code>, the faction name is appended as a comment. Does not mix
   *          well with <code>cursorOffset!=0</code>.
   */
  protected void addOtherFactions(String postfix, int cursorOffset, boolean addComment) {
    final Faction ownerFaction = unit.getFaction();
    for (Faction f : data.getFactions()) {

      if ((ownerFaction == null) || (f.equals(ownerFaction) == false)) {
        addNamed(f, postfix, cursorOffset, addComment);
      }
    }
  }

  protected void addSurroundingRegions(int radius, String postfix) {
    if (radius < 1) {
      radius = 1;
    }

    final Map<ID, RegionType> excludedRegionTypes = Regions.getNonLandRegionTypes(getData().rules);
    // no need to exclude oceans, oceans have no name anyway and it'll break getPath(...)

    final Map<CoordinateID, Region> neighbours =
        Regions.getAllNeighbours(data.regions(), region.getID(), radius, excludedRegionTypes);

    // do not include the region the unit stays in
    neighbours.remove(region.getID());

    for (Region r : neighbours.values()) {
      if (r.getName() != null && region != null && !region.equals(r)) {
        // get a path from the current region to neighbouring
        // translate the path of regions into a string of
        // directions to take
        final String directions =
            Regions.getDirections(data, region.getID(), r.getID(), excludedRegionTypes, radius);

        if (directions != null) {
          completions.add(new Completion(r.getName(), directions, postfix,
              Completion.DEFAULT_PRIORITY - 1));
        }
      }
    }
  }

  private static StringID directions[] = new StringID[] { EresseaConstants.O_NW,
      EresseaConstants.O_NE, EresseaConstants.O_E, EresseaConstants.O_SE, EresseaConstants.O_SW,
      EresseaConstants.O_W };

  protected void addDirections(String postfix) {
    ArrayList<List<String>> dirs = new ArrayList<List<String>>(6);
    int max = Integer.MIN_VALUE;
    for (StringID dir : directions) {
      dirs.add(getData().getRules().getOrder(dir).getNames(getLocale()));
      max = Math.max(max, dirs.get(dirs.size() - 1).size());
    }
    for (int i = 0; i < max; ++i) {
      for (int dir = 0; dir < dirs.size(); ++dir)
        if (i < dirs.get(dir).size()) {
          completions.add(new Completion(dirs.get(dir).get(i), dirs.get(dir).get(i), postfix));
        }
    }
  }

  protected void addUnitLuxuries(String postfix) {
    ItemCategory cat = null;

    if ((data != null) && (data.rules != null)) {
      cat = data.rules.getItemCategory(EresseaConstants.C_LUXURIES);
    }

    if ((cat != null) && (unit != null)) {
      for (final Item i : unit.getModifiedItems()) {

        if ((i.getItemType().getCategory() != null) && i.getItemType().getCategory().equals(cat)) {
          final LuxuryPrice lp = unit.getRegion().getPrices().get(i.getItemType().getID());
          if (lp != null && lp.getPrice() > 0) {
            completions.add(new Completion(i.getOrderName(), i.getOrderName(), postfix));
          }
        }
      }
    }
  }

  protected void addUnit(Unit u, String postfix) {
    addUnit(u, postfix, 0);
  }

  /**
   * Adds a unit to the completion in a standard manner without comment.
   */
  protected void addUnit(Unit u, String postfix, int cursorOffset) {
    addUnit(u, postfix, cursorOffset, false);
  }

  /**
   * Adds a unit to the completions in a standard manner without comments.
   */
  protected void addUnit(Unit u, String postfix, int cursorOffset, boolean omitTemp) {
    try {
      if (u instanceof TempUnit) {
        completions.add(new Completion(omitTemp ? u.getID().toString() : getGameSpecificStuff()
            .getOrderChanger().getTokenLocalized(getLocale(), u.getID()), postfix,
            Completion.DEFAULT_PRIORITY - 1, cursorOffset));
      } else {
        addNamed(u, postfix, cursorOffset, false);
      }
    } catch (RulesException e) {
      log.warn(e);
    }
  }

  protected abstract String getTemp();

  /**
   * Adds a thing to the completions, optionally with comments.
   * 
   * @param named This object's name and ID are displayed
   * @param postfix Completion postfix
   * @param offset Completion offset (probably doesn't make sense with comment)
   * @param addComment If this is <code>true</code>, the name is inserted as a comment after the id
   */
  protected void addNamed(Named named, String postfix, int offset, boolean addComment) {
    addNamed(named, postfix, Completion.DEFAULT_PRIORITY, offset, addComment);
  }

  /**
   * Adds a thing to the completions, optionally with comments.
   * 
   * @param named This object's name and ID are displayed
   * @param postfix Completion postfix
   * @param prio This prio and <code>prio-1</code> are used
   * @param offset Completion offset (probably doesn't make sense with comment)
   * @param addComment If this is <code>true</code>, the name is inserted as a comment after the id
   */
  protected void addNamed(Named named, String postfix, int prio, int offset, boolean addComment) {
    String name = named.getName();
    String id = named.getID().toString();
    if (name != null) {
      name = name.replaceAll(" ", "~");
      completions.add(new Completion(name + " (" + id + ")",
          id + (addComment ? ("; " + name) : ""), postfix, prio - 1, offset));
      completions.add(new Completion(id + " (" + name + ")",
          id + (addComment ? ("; " + name) : ""), postfix, prio, offset));
    } else {
      completions.add(new Completion(id, id, postfix, prio, offset));
    }
  }

  /**
   * Check for the necessary materials to produce an item considering all privileged factions in the
   * current region
   * 
   * @param iter An Iterator over the necessary materials (Items)
   * @return true, if the necessary materials are available, false otherwise
   */
  protected boolean checkForMaterials(Iterator<Item> iter) {
    return checkForMaterials(iter, 1);
  }

  /**
   * Check for the necessary materials to produce an item considering all privileged factions in the
   * current region
   * 
   * @param iter An Iterator over the necessary materials (Items)
   * @param amount A multiplicator
   * @return true, if the necessary materials are available, false otherwise
   */
  protected boolean checkForMaterials(Iterator<Item> iter, int amount) {
    boolean canMake = true;

    while (iter != null && iter.hasNext() && canMake) {
      final Item ingredient = iter.next();
      canMake &= checkForMaterial(ingredient, amount);
    }

    return canMake;
  }

  private boolean checkForMaterial(Item ingredient, int amount) {
    // be careful, units cannot own peasants although one is required for the potion "Bauernblut"
    if (ingredient.getItemType() != null) {
      int availableAmount = 0;

      if (ingredient.getItemType().equals(data.rules.getItemType(EresseaConstants.I_PEASANTS))) {
        availableAmount = region.getPeasants();
      } else {
        final Item available =
            Units.getContainerPrivilegedUnitItem(region, ingredient.getItemType());
        // region.getItem(ingredient.getItemType());

        if (available != null) {
          availableAmount = available.getAmount();
        }
      }

      if (availableAmount < (ingredient.getAmount() * amount))
        return false;
    }
    return true;
  }

  /**
   * Returns the last word in the list of tokens.
   */
  public static String getStub(List<OrderToken> tokens) {
    if (tokens.size() == 0)
      throw new IllegalArgumentException();
    if (tokens.size() == 1)
      return "";
    else {
      final OrderToken lastWord = tokens.get(tokens.size() - 2);
      if (lastWord.followedBySpace() || lastWord.ttype == OrderToken.TT_PERSIST)
        return "";
      else if (lastWord.ttype == OrderToken.TT_CLOSING_QUOTE)
        return tokens.get(tokens.size() - 4).getText() + tokens.get(tokens.size() - 3).getText()
            + lastWord.getText();
      else if (tokens.size() > 2
          && tokens.get(tokens.size() - 3).ttype == OrderToken.TT_OPENING_QUOTE)
        return tokens.get(tokens.size() - 3).getText() + lastWord.getText();
      else
        return lastWord.getText();
    }
  }

  /**
   * Returns the last word immediately at the end of the String txt. It is preferable to use
   * {@link #getStub(List)}. <br/>
   * FIXME (stm) this is identical to
   * <code>magellan.client.completion.AutoCompletion.getStub(String)</code> but we don't want to
   * reference src-client here...
   */
  public static String getStub(String txt) {
    final StringBuffer retVal = new StringBuffer();

    for (int i = txt.length() - 1; i >= 0; i--) {
      final char c = txt.charAt(i);

      // if((c == '-') || (c == '_') || (c == '~') || (c == '.') || (Character.isLetterOrDigit(c) ==
      // true)) {
      if ((!Character.isWhitespace(c) && c != '\'' && c != '"' && c != '@')) {
        // if ((!Character.isWhitespace(c))) {
        retVal.append(c);
      } else {
        break;
      }
    }
    return retVal.reverse().toString();
  }

  /**
   * Determines whether the unit has any skill of at least the given level Used i.e. for determining
   * if someone can teach
   */
  protected boolean hasSkills(Unit u, int level) {
    for (final Skill s : u.getModifiedSkills()) {
      if (s.getLevel() >= level)
        return true;
    }
    return false;
  }

  /**
   * Determines whether the specified unit has a skill.
   */
  protected boolean hasSkill(Unit u, StringID id) {
    return hasSkill(u, id, 1);
  }

  /**
   * Determines whether the specified unit has a skill at a minimum level. Returns also true, if the
   * specified skill is unknown. FF: changed to reflect modified skill
   */
  protected boolean hasSkill(Unit u, StringID id, int level) {
    boolean retVal = false;
    final SkillType skillType = data.rules.getSkillType(id);

    if (skillType != null) {
      // Skill e = u.getSkill(skillType);
      final Skill e = u.getModifiedSkill(skillType);

      if ((e != null) && (e.getLevel() >= level)) {
        retVal = true;
      }
    } else {
      retVal = true;
    }

    return retVal;
  }

  /**
   * Adds an item by type
   */
  protected void addItem(ItemType iType, String postfix) {
    completions.add(new Completion(iType.getOrderName(), iType.getOrderName(), postfix));
  }

  /**
   * Case-insensitive comparator for String and/or Completion objects
   */
  protected static class IgnrCsComp implements Comparator<Object> {
    /**
     * Compares Strings or completions case insensitively.
     */
    public int compare(Object o1, Object o2) {
      if (o1 instanceof String && o2 instanceof String)
        return ((String) o1).compareToIgnoreCase((String) o2);
      else if (o1 instanceof Completion && o2 instanceof Completion) {
        final Completion c1 = (Completion) o1;
        final Completion c2 = (Completion) o2;

        if (c1.getName() == null)
          return (c2.getName() == null) ? 0 : 1;
        else
          return (c2.getName() == null) ? (-1) : c1.getName().compareToIgnoreCase(c2.getName());
      } else if (o1 instanceof Completion && o2 instanceof String) {
        final String s1 = ((Completion) o1).getName();
        final String s2 = (String) o2;
        if (s1 == null)
          return 0;
        else
          return s1.compareToIgnoreCase(s2);
      } else if (o1 instanceof String && o2 instanceof Completion) {
        final String s1 = (String) o1;
        final String s2 = ((Completion) o2).getName();
        if (s2 == null)
          return 0;
        else
          return s2.compareToIgnoreCase(s1);
      }

      return 0;
    }

  }

  /**
   * Priority comparator for Completion objects
   */
  protected static class PrioComp implements Comparator<Completion> {
    /**
     * Compares to Completions by priority.
     */
    public int compare(Completion o1, Completion o2) {
      int retVal = 0;

      if (o1.getPriority() != o2.getPriority()) {
        retVal = o1.getPriority() - o2.getPriority();
      } else {
        retVal = o1.getName().compareToIgnoreCase(o2.getName());
      }

      return retVal;
    }

  }

  /**
   * Returns the value of data.
   * 
   * @return Returns data.
   */
  public GameData getData() {
    return data;
  }

  protected GameSpecificStuff getGameSpecificStuff() {
    return data.getGameSpecificStuff();
  }

  protected Locale getLocale() {
    Locale locale = getUnit().getLocale();
    if (locale == null)
      return Locales.getOrderLocale();
    else
      return locale;
  }

  protected String getOrderTranslation(StringID orderKey) {
    return getData().getRules().getGameSpecificStuff().getOrderChanger().getOrder(getLocale(),
        orderKey);
  }

  protected String getRuleItemTranslation(String orderKey) {
    return Resources.getRuleItemTranslation(orderKey, getLocale());
  }

  protected String getTranslation(String key) {
    return Resources.get(key);
  }

  /**
   * Returns the value of completions.
   * 
   * @return Returns completions.
   */
  public List<Completion> getCompletions() {
    return completions;
  }

  /**
   * Returns the value of region.
   * 
   * @return Returns region.
   */
  public Region getRegion() {
    return region;
  }

  /**
   * Returns the value of unit.
   * 
   * @return Returns unit.
   */
  public Unit getUnit() {
    return unit;
  }

  public List<OrderToken> getParserTokens() {
    return parserTokens;
  }

  protected abstract void cmplt();

  /** Add completions for command Description. */
  protected void cmpltDescription() {
    completions.add(new Completion(twoQuotes, twoQuotes, " ", Completion.DEFAULT_PRIORITY, 2));
  }

  /**
   * TODO DOCUMENT-ME
   * 
   * @param openingToken
   * @param contentToken
   * @param closingToken
   * @param preferQuotes
   * @param forceQuotes
   * @param doClose
   * @param preferredQuote
   */
  public void fixQuotes(OrderToken openingToken, OrderToken contentToken, OrderToken closingToken,
      boolean preferQuotes, boolean forceQuotes, boolean doClose, char preferredQuote) {
    final List<Completion> oldList = new ArrayList<Completion>(completions);
    completions.clear();
    for (final Completion c : oldList) {
      final OrderTokenizer nameTokenizer = new OrderTokenizer(new StringReader(c.getName()));
      String newName = c.getName();
      if (openingToken != null || forceQuotes) {
        newName =
            fixQuotes(nameTokenizer, openingToken, contentToken, closingToken, preferQuotes,
                forceQuotes, doClose, preferredQuote);
      }
      final OrderTokenizer valueTokenizer = new OrderTokenizer(new StringReader(c.getValue()));
      final String newValue =
          fixQuotes(valueTokenizer, openingToken, contentToken, closingToken, preferQuotes,
              forceQuotes, doClose, preferredQuote);
      completions.add(new Completion(newName, newValue, c.getPostfix(), c.getPriority(), c
          .getCursorOffset()));
    }
  }

  /**
   * TODO DOCUMENT-ME
   * 
   * @param innerTokenizer
   * @param openingToken
   * @param contentToken
   * @param closingToken
   * @param preferQuotes
   * @param forceQuotes
   * @param doClose
   * @param preferredQuote
   */
  protected String fixQuotes(OrderTokenizer innerTokenizer, OrderToken openingToken,
      OrderToken contentToken, OrderToken closingToken, boolean preferQuotes, boolean forceQuotes,
      boolean doClose, char preferredQuote) {

    final StringBuffer result = new StringBuffer();

    // see if first inner token is a quote, if not, add it to inner tokens
    final List<OrderToken> innerTokens = new LinkedList<OrderToken>();
    OrderToken innerQuote = innerTokenizer.getNextToken();
    if (innerQuote.ttype != OrderToken.TT_OPENING_QUOTE) {
      innerTokens.add(innerQuote);
      innerQuote = null;
    }

    // assign which quote is used
    String insertedQuote;
    if (openingToken != null) {
      insertedQuote = openingToken.getText();
    } else {
      insertedQuote = String.valueOf(preferredQuote);
    }

    // add rest of inner tokens
    for (OrderToken currentToken = innerTokenizer.getNextToken(); currentToken.ttype != OrderToken.TT_EOC
        && (currentToken.ttype != OrderToken.TT_EOC || (currentToken.ttype == OrderToken.TT_EOC && !currentToken
            .getText().equals(insertedQuote))); currentToken = innerTokenizer.getNextToken()) {
      if (currentToken.ttype != OrderToken.TT_CLOSING_QUOTE
          || (currentToken.ttype == OrderToken.TT_CLOSING_QUOTE && !currentToken.getText().equals(
              insertedQuote))) {
        innerTokens.add(currentToken);
      }
    }

    // append opening quote if needed
    int lastLength = 0;
    if (openingToken != null || innerQuote != null
        || ((innerTokens.size() > 1 && preferQuotes) || forceQuotes)) {
      result.append(insertedQuote);
      lastLength = insertedQuote.length();
    }

    // append content
    for (final OrderToken t : innerTokens) {
      if (t.ttype != OrderToken.TT_EOC) {
        for (int i =
            result.length() - lastLength + (innerQuote == null ? 0 : innerQuote.getText().length()); i < t
            .getStart(); ++i) {
          if (preferQuotes) {
            result.append(" ");
          } else {
            result.append('~');
          }
        }
        result.append(t.getText());
      }
    }

    // append closing quote if needed
    if (doClose
        && (openingToken != null || innerQuote != null || (innerTokens.size() > 2 && preferQuotes) || forceQuotes)) {
      result.append(openingToken != null ? openingToken.getText() : preferredQuote);
    }

    return result.toString();
  }

  // /** Add completions for command FinalQuote. */
  // public void cmpltFinalQuote(char quote) {
  // final List<Completion> oldList = new ArrayList<Completion>(completions);
  // completions.clear();
  // for (final Completion c : oldList) {
  // if (c.getValue().length() > 1 && c.getValue().charAt(c.getValue().length() - 1) == quote
  // && c.getValue().charAt(c.getValue().length() - 2) != '\\') {
  // completions.add(c);
  // } else {
  // completions.add(new Completion(c.getName() + quote, c.getValue().trim() + quote, c
  // .getPostfix(), c.getPriority(), c.getCursorOffset()));
  // }
  // }
  // }
  //
  // /** Add completions for command OpeningQuote. */
  // public void cmpltOpeningQuote(char quote, boolean cmplName) {
  // final List<Completion> oldList = new LinkedList<Completion>(completions);
  // completions.clear();
  // for (final Completion c : oldList) {
  // completions.add(new Completion(cmplName ? quote + c.getName() : c.getName(), quote
  // + c.getValue(), c.getPostfix(), c.getPriority(), c.getCursorOffset()));
  // }
  // }

}
