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

package magellan.library.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import magellan.library.Border;
import magellan.library.Building;
import magellan.library.CoordinateID;
import magellan.library.Faction;
import magellan.library.GameData;
import magellan.library.ID;
import magellan.library.Identifiable;
import magellan.library.Island;
import magellan.library.LuxuryPrice;
import magellan.library.Message;
import magellan.library.Region;
import magellan.library.RegionResource;
import magellan.library.Scheme;
import magellan.library.Ship;
import magellan.library.Sign;
import magellan.library.StringID;
import magellan.library.Unit;
import magellan.library.UnitID;
import magellan.library.ZeroUnit;
import magellan.library.gamebinding.EresseaConstants;
import magellan.library.rules.CastleType;
import magellan.library.rules.ItemType;
import magellan.library.rules.RegionType;
import magellan.library.rules.UnitContainerType;
import magellan.library.utils.CollectionFactory;
import magellan.library.utils.Direction;
import magellan.library.utils.MagellanFactory;
import magellan.library.utils.Regions;
import magellan.library.utils.logging.Logger;

// Fiete 20080806: prepare for loosing special info in CR
// pre eressearound 570+(?) we have ;silber as region tag
// in eressearound 570(?) we have ;silver as region tag AND "Silber"-resource
// we can expect that the region tag will not hold forever...
// all requests to resources (old tags) have to check, if
// the requested Resource is in the r.resources

/**
 * @author $Author: $
 * @version $Revision: 356 $
 */
public class MagellanRegionImpl extends MagellanUnitContainerImpl implements Region {
  private static final Logger log = Logger.getInstance(MagellanUnitContainerImpl.class);

  /** Number of trees */
  private int trees = -1;

  /** Number of trees last round */
  private int oldTrees = -1;

  /** Number of sprouts */
  private int sprouts = -1;

  /** Number of Sprouts last round */
  private int oldSprouts = -1;

  /** DOCUMENT-ME */
  private boolean mallorn = false;

  /** Number of iron */
  private int iron = -1;

  /** Number of Iron last round */
  private int oldIron = -1;

  /** Number of laen */
  private int laen = -1;

  /** Number of Laen last round */
  private int oldLaen = -1;

  /** Number of peasants */
  private int peasants = -1;

  /** Number of Peasants last round */
  private int oldPeasants = -1;

  /** Number of silver */
  private int silver = -1;

  /** Number of Silver last round */
  private int oldSilver = -1;

  /** Number of horses */
  private int horses = -1;

  /** Number of Horses last round */
  private int oldHorses = -1;

  /** Number of stones */
  private int stones = -1;

  /** Number of Stones last round */
  private int oldStones = -1;

  /** Trade volume last round */
  private int oldLuxuries = -1;

  /**
   * The wage persons can earn by working in this region. Unfortunately this is not the wage
   * peasants earn but the wage a player's persons earn and to make it worse, the eressea server
   * puts different values into CRs depending of the race of the 'owner' faction of the report. I.e.
   * an orc faction gets a different value than factions of other races. Therefore there is a
   * getPeasantWage() method returning how much a peasant earns in this region depending on the
   * biggest castle.
   */
  public int wage = -1;

  /** the wage persons have been able to earn in the past. */
  public int oldWage = -1;

  /** DOCUMENT-ME */
  public ItemType herb = null;

  /** DOCUMENT-ME */
  public String herbAmount = null;

  /** Indicates that there are too many orcs in this region. */
  public boolean orcInfested = false;

  // pavkovic 2002.05.13: for eressea CR-Version >= 64 we do interpret the
  // recruits tag

  /** DOCUMENT-ME */
  public int recruits = -1;

  /** DOCUMENT-ME */
  public int oldRecruits = -1;

  // fiete 2007.02.12: we add sign support - 2 lines allowed
  private List<Sign> signs = null;

  /**
   * a flag which indicates if this region is ocean with a neighboring not-ocean region used for
   * better pathfindung for ships -1 -> not computed yet 0 -> either no ozean or no neighboring land
   * 1 -> ozean and neighboring land
   */
  private int ozeanWithCoast = -1;

  /** Informs about the reason why this region is visible. */
  private String visibility = null;

  /**
   * The Integer is an BitMap representing the info, if neighboriing regions are ozean or not
   * BitMask 1: dir = 0 BitMask 2: dir = 1 BitMask 4: dir = 2 .... Bit = 1 -> there is land! Bit = 0
   * -> there is ozean!
   */
  private Integer coastBitMask = null;

  /**
   * the unique regionID generated and sent by the eressea server starting with turn 570
   */
  private long uID = INVALID_UID;

  /**
   * Constructs a new Region object uniquely identifiable by the specified id.
   */
  public MagellanRegionImpl(CoordinateID id, GameData data) {
    super(id, data);
  }

  // pavkovic 2003.09.10: moved from Cache to this object to remove
  // Cache objects for empty/ocean regions
  // used in swing.map.RegionImageCellRenderer per region
  private int fogOfWar = -1;

  /**
   * @see magellan.library.Region#fogOfWar()
   */
  public synchronized boolean fogOfWar() {
    if (fogOfWar == -1) {
      fogOfWar = 1;

      for (Unit unit : units()) {
        Faction f = unit.getFaction();

        if (f.isPrivileged()) {
          fogOfWar = 0;

          break;
        }
      }
    }

    return fogOfWar == 1;
  }

  /**
   * @see magellan.library.Region#setFogOfWar(int)
   */
  public synchronized void setFogOfWar(int fog) {
    fogOfWar = fog;
  }

  /** this unit indicated the "0" unit! */
  private ZeroUnit cachedZeroUnit;

  /**
   * @see magellan.library.Region#getZeroUnit()
   */
  public Unit getZeroUnit() {
    // only create if needed
    if (cachedZeroUnit == null) {
      // if there are no units in this region we assume that this
      // region is less interesting (there will be NO Relation nor
      // massive interactive view of this region.
      // So we create the ZeroUnit on the fly.
      if (units().isEmpty())
        return MagellanFactory.createZeroUnit(this);

      cachedZeroUnit = MagellanFactory.createZeroUnit(this);
    }

    return cachedZeroUnit;
  }

  /**
   * Returns the number of modified persons after "give 0", recruit.
   *
   * @return the number of modified persons after "give 0", recruit
   */
  public int getModifiedPeasants() {
    ZeroUnit zu = (ZeroUnit) getZeroUnit();

    // peasants == peasants - (maxRecruit() - recruited peasants ) +
    // givenPersons
    return (peasants == -1) ? (-1) : (peasants - zu.getPersons() + zu.getModifiedPersons() + zu
        .getGivenPersons());
  }

  /**
   * DOCUMENT-ME
   */
  public int modifiedRecruit() {
    return getZeroUnit().getModifiedPersons();
  }

  /** Indicates that refreshUnitRelations() has already been called once. */
  private boolean unitRelationsRefreshed = false;

  /** The island this region belongs to. */
  private Island island = null;

  private Visibility visibilityConstant;

  /**
   * Sets the island this region belongs to.
   */
  public void setIsland(Island i) {
    if (island != null) {
      island.invalidateRegions();
    }

    island = i;

    if (island != null) {
      island.invalidateRegions();
    }
  }

  /**
   * Returns the island this region belongs to.
   */
  public Island getIsland() {
    return island;
  }

  /**
   * A string constant indicating why this region is visible.
   *
   * @return the string object or null, if the visibility is unspecified.
   */
  public String getVisibilityString() {
    return visibility;
  }

  /**
   * @see magellan.library.Region#getVisibility()
   */
  public Visibility getVisibility() {
    if (visibilityConstant != null)
      return visibilityConstant;

    Visibility result = Visibility.NULL;
    if (visibility == null) {
      // we have 0 or 4
      // check for qualified units
      boolean qualifiedUnitInCurRegion = false;
      if (units() != null && units().size() > 0) {
        for (Unit actUnit : units()) {
          if (actUnit.isDetailsKnown()) {
            // -1 is default for this int and stays, if no info is available
            qualifiedUnitInCurRegion = true;
            break;
          }
        }
      }
      if (qualifiedUnitInCurRegion) {
        result = Visibility.UNIT;
      } else {
        result = Visibility.NULL;
      }
    } else {
      // we have a visibility ... choose right int
      if (visibility.equalsIgnoreCase(Region.VIS_STR_NEIGHBOUR)) {
        result = Visibility.NEIGHBOR;
      }
      if (visibility.equalsIgnoreCase(Region.VIS_STR_LIGHTHOUSE)) {
        result = Visibility.LIGHTHOUSE;
      }
      if (visibility.equalsIgnoreCase(Region.VIS_STR_TRAVEL)) {
        result = Visibility.TRAVEL;
      }
      if (visibility.equalsIgnoreCase(Region.VIS_STR_WRAP)) {
        result = Visibility.WRAP;
      }
      if (visibility.equalsIgnoreCase(Region.VIS_STR_UNIT)) {
        // standard eressea reports don't have this...
        result = Visibility.UNIT;
      }
      if (visibility.equalsIgnoreCase(Region.VIS_STR_FAR)) {
        // standard eressea reports don't have this...
        result = Visibility.FAR;
      }
      if (visibility.equalsIgnoreCase(Region.VIS_STR_BATTLE)) {
        // standard eressea reports don't have this...
        result = Visibility.BATTLE;
      }
    }
    visibilityConstant = result;
    return result;
  }

  /**
   * Sets a string constant indicating why this region is visible.
   *
   * @param vis a String object or null to indicate that the visibility cannot be determined.
   * @see magellan.library.Region#setVisibilityString(java.lang.String)
   */
  public void setVisibilityString(String vis) {
    visibility = vis;
    visibilityConstant = null;
  }

  /**
   * 0..very poor - no info (->visibility=null)<br />
   * 1..neighbour<br />
   * 2..lighthouse<br />
   * 3..travel<br />
   * 4..qualified unit in region (->visibility=null)
   *
   * @param vis
   */
  public void setVisibility(Visibility vis) {
    if (vis == Visibility.NULL || vis == Visibility.UNIT) {
      visibility = null;
    } else if (vis == Visibility.NEIGHBOR) {
      visibility = Region.VIS_STR_NEIGHBOUR;
    } else if (vis == Visibility.LIGHTHOUSE) {
      visibility = Region.VIS_STR_LIGHTHOUSE;
    } else if (vis == Visibility.TRAVEL) {
      visibility = Region.VIS_STR_TRAVEL;
    } else if (vis == Visibility.UNIT) {
      visibility = Region.VIS_STR_UNIT;
    } else if (vis == Visibility.BATTLE) {
      visibility = Region.VIS_STR_BATTLE;
    } else if (vis == Visibility.FAR) {
      visibility = Region.VIS_STR_FAR;
    } else if (vis == Visibility.WRAP) {
      visibility = Region.VIS_STR_WRAP;
    }
    visibilityConstant = vis;
  }

  /**
   * The prices for luxury goods in this region. The map contains the name of the luxury good as
   * instance of class <tt>StringID</tt> as key and instances of class <tt>LuxuryPrice</tt> as
   * values.
   */
  public Map<StringID, LuxuryPrice> prices = null;

  /** The prices of luxury goods of the last turn. */
  public Map<StringID, LuxuryPrice> oldPrices = null;

  /**
   * The messages for this region. The list consists of objects of class <tt>Message</tt>.
   */
  public List<Message> messages = null;

  /**
   * Special messages related to this region. The list contains instances of class <tt>Message</tt>
   * with type -1 and only the text set.
   */
  public List<Message> events = null;

  /**
   * Special messages related to this region. The list contains instances of class <tt>Message</tt>
   * with type -1 and only the text set.
   */
  public List<Message> playerMessages = null;

  /**
   * Special messages related to this region. The list contains instances of class <tt>Message</tt>
   * with type -1 and only the text set.
   */
  public List<Message> surroundings = null;

  /**
   * Special messages related to this region. The list contains instances of class <tt>Message</tt>
   * with type -1 and only the text set.
   */
  public List<Message> travelThru = null;

  /**
   * Special messages related to this region. The list contains instances of class <tt>Message</tt>
   * with type -1 and only the text set.
   */
  public List<Message> travelThruShips = null;

  /**
   * RegionResources in this region. The keys in this map are instances of class <tt>ID</tt>
   * identifying the item type of the resource, the values are instances of class
   * <tt>RegionResource</tt>.
   */
  private Map<Identifiable, RegionResource> resources = null;

  /** A collection view of the resources. */
  private Collection<RegionResource> resourceCollection = null;

  /**
   * Returns all resources of this region.
   */
  public Collection<RegionResource> resources() {
    if (resourceCollection == null) {
      /*
       * since resources appear twice in the map, once with the numerical ID and once with the item
       * type ID, we have to make sure that this collection lists only one of them. Since the
       * hashValue() Method of a RegionResource relates to its numerical ID a HashSet can do the job
       */

      // 2002.02.18 ip: this.resources can be null
      if (resources == null) {
        resourceCollection = Collections.emptySet();
      } else {
        resourceCollection =
            Collections
                .unmodifiableCollection(new LinkedHashSet<RegionResource>(resources.values()));
      }
    }

    return resourceCollection;
  }

  /**
   * Adds a resource to this region.
   *
   * @throws NullPointerException
   */
  public RegionResource addResource(RegionResource resource) {
    if (resource == null)
      throw new NullPointerException();

    if (resources == null) {
      resources = CollectionFactory.<Identifiable, RegionResource> createSyncOrderedMap();

    }

    // enforce the creation of a new collection view
    resourceCollection = null;

    // pavkovic 2002.05.21: If some resources have an amount zero, we ignore it
    if (resource.getAmount() != 0) {
      resources.put(resource.getType(), resource);
      // if (this.resourceCollection != null)
      // this.resourceCollection.add(resource);
    }

    // if(log.isDebugEnabled()) {
    // log.debug("Region.addResource:" + this);
    // log.debug("Region.addResource:" + resource);
    // log.debug("Region.addResource:" + resources);
    // }

    return resource;
  }

  /**
   * Removes the resource with the specified numerical id or the id of its item type from this
   * region.
   *
   * @return the removed resource or null if no resource with the specified id exists in this
   *         region.
   */
  public RegionResource removeResource(RegionResource r) {
    return this.removeResource(r.getType());
  }

  /**
   * @see magellan.library.Region#removeResource(magellan.library.rules.ItemType)
   */
  public RegionResource removeResource(ItemType type) {
    if (resources == null)
      return null;

    RegionResource ret = resources.remove(type);

    if (resources.isEmpty()) {
      resources = null;
    }

    resourceCollection = null;
    // if(log.isDebugEnabled()) {
    // log.debug("Region.removeResource:" + this);
    // log.debug("Region.removeResource:" + ret);
    // if(ret != null) {
    // log.debug("Region.removeResource:" + ret.getID());
    // log.debug("Region.removeResource:" + ret.getType().getID());
    // }
    // log.debug("Region.removeResource:" + resources);
    // }
    return ret;
  }

  /**
   * Removes all resources from this region.
   */
  public void clearRegionResources() {
    if (resources != null) {
      resources.clear();
      resources = null;
      resourceCollection = null;
    }
  }

  /**
   * @see magellan.library.Region#getResource(magellan.library.rules.ItemType)
   */
  public RegionResource getResource(ItemType type) {
    if (type == null)
      return null;
    return (resources != null) ? (RegionResource) resources.get(type) : null;
  }

  /**
   * Schemes in this region. The keys in this map are instances of class <tt>Coordinate</tt>
   * identifying the location of the scheme, the values are instances of class <tt>Scheme</tt>.
   */
  private Map<ID, Scheme> schemes = null;

  /** A collection view of the schemes. */
  private Collection<Scheme> schemeCollection = null;

  /**
   * Returns all schemes of this region.
   */
  public Collection<Scheme> schemes() {
    if (schemes == null)
      return Collections.emptyList();

    if (schemeCollection == null) {
      if (schemes != null && schemes.values() != null) {
        schemeCollection = Collections.unmodifiableCollection(schemes.values());
      } else {
        schemeCollection = Collections.emptyList();
      }
    }

    return schemeCollection;
  }

  /**
   * Adds a scheme to this region.
   *
   * @throws NullPointerException
   */
  public Scheme addScheme(Scheme scheme) {
    if (scheme == null)
      throw new NullPointerException();

    if (schemes == null) {
      schemes = CollectionFactory.<ID, Scheme> createSyncOrderedMap(4);

      // enforce the creation of a new collection view
      // AG: Since we just create if the scheme map is non-null not necessary
      // this.schemeCollection = null;
    }

    schemes.put(scheme.getID(), scheme);

    return scheme;
  }

  /**
   * Removes all schemes from this region.
   */
  public void clearSchemes() {
    if (schemes != null) {
      schemes.clear();
      schemes = null;
      schemeCollection = null;
    }
  }

  /**
   * Returns the scheme with the specified corodinate.
   *
   * @return the scheme object or null if no scheme with the specified ID exists in this region.
   */
  public Scheme getScheme(ID id) {
    return (schemes != null) ? (Scheme) schemes.get(id) : null;
  }

  /**
   * Border elements of this region. The list contains instances of class <tt>Border</tt>.
   */
  private Map<ID, Border> borders = null;

  /** A collection view of the borders. */
  private Collection<Border> borderCollection = null;

  /**
   * Returns all borders of this region.
   */
  public Collection<Border> borders() {
    if (borders == null)
      return Collections.emptyList();

    if (borderCollection == null) {
      if (borders != null && borders.values() != null) {
        borderCollection = Collections.unmodifiableCollection(borders.values());
      } else {
        borderCollection = Collections.emptyList();
      }
    }

    return borderCollection;
  }

  /**
   * Adds a border to this region.
   *
   * @throws NullPointerException if border is <code>null</code>
   */
  public Border addBorder(Border border) {
    if (border == null)
      throw new NullPointerException();

    if (borders == null) {
      borders = CollectionFactory.<ID, Border> createSyncOrderedMap(3);

      // enforce the creation of a new collection view
      // AG: Since we just create if the scheme map is non-null not necessary
      // this.borderCollection = null;
    }

    borders.put(border.getID(), border);

    return border;
  }

  /**
   * Removes all borders from this region.
   */
  public void clearBorders() {
    if (borders != null) {
      borders.clear();
      borders = null;
      borderCollection = null;
    }
  }

  /**
   * Returns the border with the specified id.
   *
   * @return the border object or null if no border with the specified id exists in this region.
   */
  public Border getBorder(ID key) {
    return (borders != null) ? (Border) borders.get(key) : null;
  }

  /** All ships that are in this container. */
  private Map<ID, Ship> ships = null;

  /** Provides a collection view of the ship map. */
  private Collection<Ship> shipCollection = null;

  /**
   * Returns an unmodifiable collection of all the ships in this container.
   */
  public Collection<Ship> ships() {
    if (ships == null)
      return Collections.emptyList();

    if (shipCollection == null) {
      if (ships != null && ships.values() != null) {
        shipCollection = Collections.unmodifiableCollection(ships.values());
      } else {
        shipCollection = Collections.emptyList();
      }
    }

    return shipCollection;
  }

  /**
   * Retrieve a ship in this container by id.
   */
  public Ship getShip(ID key) {
    return (ships != null) ? (Ship) ships.get(key) : null;
  }

  /**
   * Adds a ship to this container. This method should only be invoked by Ship.setXXX() methods.
   */
  public void addShip(Ship s) {
    if (ships == null) {
      ships = new LinkedHashMap<ID, Ship>();

      // enforce the creation of a new collection view
      // AG: Since we just create if the ship map is non-null not necessary
      // this.shipCollection = null;
    }

    ships.put(s.getID(), s);
  }

  /**
   * Removes a ship from this container. This method should only be invoked by Ship.setXXX()
   * methods.
   */
  public Ship removeShip(Ship s) {
    if (ships == null)
      return null;

    Ship ret = ships.remove(s.getID());

    if (ships.isEmpty()) {
      ships = null;
      shipCollection = null;
    }

    return ret;
  }

  /** All buildings that are in this container. */
  private Map<ID, Building> buildings = null;

  /** Provides a collection view of the building map. */
  private Collection<Building> buildingCollection = null;

  /**
   * Returns an unmodifiable collection of all the buildings in this container.
   */
  public Collection<Building> buildings() {
    if (buildings == null)
      return Collections.emptyList();

    if (buildingCollection == null) {
      if (buildings != null && buildings.values() != null)
        return Collections.unmodifiableCollection(buildings.values());
      else
        return Collections.emptyList();
    }

    return buildingCollection;
  }

  /**
   * Retrieve a building in this container by id.
   */
  public Building getBuilding(ID key) {
    return (buildings != null) ? (Building) buildings.get(key) : null;
  }

  /**
   * Adds a building to this container. This method should only be invoked by Building.setXXX()
   * methods.
   */
  public void addBuilding(Building u) {
    if (buildings == null) {
      buildings = new LinkedHashMap<ID, Building>();

      // enforce the creation of a new collection view
      // AG: Since we just create if the builing map is non-null not necessary
      // this.buildingCollection = null;
    }

    buildings.put(u.getID(), u);
  }

  /**
   * Removes a building from this container. This method should only be invoked by Building.setXXX()
   * methods.
   */
  public Building removeBuilding(Building b) {
    if (buildings == null)
      return null;

    Building ret = buildings.remove(b.getID());

    if (buildings.isEmpty()) {
      buildings = null;
      buildingCollection = null;
    }

    return ret;
  }

  /**
   * Returns the maximum number of persons that can be recruited in this region. If it was manually
   * set, this value is returned, otherwise the value is calculated from the number of peasants.
   *
   * @see #getRecruits()
   * @see #maxRecruit(int)
   */
  public int maxRecruit() {
    // pavkovic 2002.05.10: in case we dont have a recruit max set we evaluate
    // it
    return (recruits == -1) ? MagellanRegionImpl.maxRecruit(peasants) : recruits;
  }

  /**
   * Returns the maximum number of persons that can be recruited in this region.
   */
  public int maxOldRecruit() {
    // pavkovic 2002.05.10: in case we dont have a recruit max set we evaluate
    // it
    return (oldRecruits == -1) ? MagellanRegionImpl.maxRecruit(oldPeasants) : oldRecruits;
  }

  /**
   * Returns the maximum number of persons available for recruitment in a region with the specified
   * number of peasants.
   *
   * @see #maxRecruit()
   * @see #getRecruits()
   */
  private static int maxRecruit(int peasants) {
    if (peasants >= 0)
      return peasants / 40; // 2.5 %

    return -1;
  }

  /**
   * Returns the silver that can be earned through entertainment in this region.
   *
   * @see magellan.library.Region#maxEntertain()
   */
  public int maxEntertain() {
    return getData().getGameSpecificRules().getMaxEntertain(this);
  }

  /**
   * Returns the silver that could be earned through entertainment in this region in the last week.
   */
  public int maxOldEntertain() {
    return getData().getGameSpecificRules().getMaxOldEntertain(this);
  }

  /**
   * Returns the maximum number of luxury items that can be bought in this region without a price
   * penalty.
   */
  public int maxLuxuries() {
    return getData().getGameSpecificRules().getMaxTrade(this);
  }

  /**
   * Returns the maximum number of luxury items that could be bought in this region without a price
   * penalty.
   */
  public int maxOldLuxuries() {
    return oldLuxuries;
  }

  /**
   * @see magellan.library.Region#setOldLuxuries(int)
   */
  public void setOldLuxuries(int amount) {
    oldLuxuries = amount;
  }

  @Override
  public RegionType getType() {
    if (super.getType() == null || super.getType() instanceof RegionType)
      return (RegionType) super.getType();
    throw new RuntimeException("invalid region type");
  }

  @Override
  public void setType(UnitContainerType type) {
    if (type instanceof RegionType) {
      super.setType(type);
    } else
      throw new IllegalArgumentException("invalid region type");
  }

  /**
   * Calculates the wage a peasant earns according to the biggest castle in this region. While the
   * value of the wage field is directly taken from the report and may be biased by the race of the
   * owner faction of that report, this function tries to determine the real wage a peasant can earn
   * in this region. Wage for player persons can be derived from that value
   */
  public int getPeasantWage() {
    int realWage = getType().getPeasantWage();

    for (Building b : buildings()) {
      if (b.getType() instanceof CastleType) {
        CastleType ct = (CastleType) b.getType();
        realWage = Math.max(ct.getPeasantWage(), realWage);
      }
    }

    return realWage;
  }

  /**
   * Returns a String representation of this Region object. If region has no name the string
   * representation of the region type is used.
   */
  @Override
  public String toString() {
    StringBuffer sb = new StringBuffer();

    if (getName() == null) {
      if (getType() != null) {
        sb.append(getType().toString());
      }
    } else {
      sb.append(getName());
    }

    sb.append(" (").append(getID().toString()).append(")");

    return sb.toString();
  }

  /**
   * Returns the coordinate of this region. This method is only a type-safe short cut for retrieving
   * and converting the ID object of this region.
   */
  public CoordinateID getCoordinate() {
    return (CoordinateID) super.getID();
  }

  /**
   * A synomym for {@link #getCoordinate()}.
   *
   * @see magellan.library.impl.MagellanIdentifiableImpl#getID()
   */
  @Override
  public CoordinateID getID() {
    return (CoordinateID) super.getID();
  }

  /**
   * Returns the RegionType of this region. This method is only a type-safe short cut for retrieving
   * and converting the RegionType of this region.
   */
  public RegionType getRegionType() {
    return getType();
  }

  /**
   * Refreshes all the relations of all units in this region. It is preferable to call this method
   * instead of refreshing the unit relations 'manually'.
   */
  // FIXME should be called once after region creation, then never again
  public void refreshUnitRelations() {
    refreshUnitRelations(false);
  }

  /**
   * Refreshes all the relations of all units in this region. It is preferable to call this method
   * instead of refreshing the unit relations 'manually'.
   *
   * @param forceRefresh to enforce refreshment, false for one refreshment only
   * @deprecated should be triggered implicitly by UnitOrdersEvents
   */

  @Deprecated
  public synchronized void refreshUnitRelations(boolean forceRefresh) {

    if (unitRelationsRefreshed == false || forceRefresh) {
      unitRelationsRefreshed = true;

      getData().getGameSpecificStuff().getRelationFactory().createRelations(this);
    }
  }

  /**
   * Guarding units of this region. The list contains instances of class <tt>Unit</tt>.
   */
  private List<Unit> guards;

  /**
   * add guarding Unit to region
   */
  public void addGuard(Unit u) {
    if (guards == null) {
      guards = new ArrayList<Unit>();
    }

    if (!guards.contains(u)) {
      guards.add(u);
    }
  }

  /**
   * get The List of guarding Units
   */
  public List<Unit> getGuards() {
    return guards;
  }

  /**
   * @see magellan.library.impl.MagellanUnitContainerImpl#getUnit(magellan.library.ID)
   */
  @Override
  public Unit getUnit(ID key) {
    if (UnitID.createUnitID(0, getData().base).equals(key))
      return getZeroUnit();
    else
      return super.getUnit(key);
  }

  private Map<Direction, Region> neighbors;

  private Faction ownerFaction;

  private int morale = -1;

  private int mourning = -1;

  // private Map<Faction, Integer> maintenance = new HashMap<Faction, Integer>();

  private Set<Unit> maintained = new HashSet<Unit>();

  /**
   * @see magellan.library.Region#setNeighbours(java.util.Collection)
   * @throws IllegalArgumentException if one of the neighbours doesn't exist in the data.
   * @deprecated Use {@link #setNeighbors(Map)}
   */
  @Deprecated
  public void setNeighbours(Collection<CoordinateID> neighbours) {
    if (neighbors == null) {
      neighbors = null;
    } else {
      HashMap<Direction, Region> newNeighbors = new LinkedHashMap<Direction, Region>();
      for (CoordinateID id : neighbours) {
        Direction dir =
            getData().getGameSpecificStuff().getMapMetric().getDirection(getCoordinate(), id);
        Region r = data.getRegion(id);
        if (r == null)
          throw new IllegalArgumentException("neighbor region doesn't exist");
        newNeighbors.put(dir, r);
      }
      neighbors = newNeighbors;
    }
  }

  /**
   * @see magellan.library.Region#setNeighbors(java.util.Map)
   */
  public void setNeighbors(Map<Direction, Region> neighbors) {
    if (neighbors == null) {
      this.neighbors = null;
    } else {
      this.neighbors = new LinkedHashMap<Direction, Region>(neighbors);
    }
  }

  /**
   * Adds a neighbor in the specified direction.
   *
   * @return The previous neighbour at that direction
   */
  public Region addNeighbor(Direction dir, Region newNeighbor) {
    if (neighbors == null) {
      neighbors = evaluateNeighbours();
    }
    if (getData() != newNeighbor.getData()) {
      log.warn("neighbor not in same data!");
    }
    return neighbors.put(dir, newNeighbor);
  }

  /**
   * @see magellan.library.Region#addNeighbor(magellan.library.utils.Direction,
   *      magellan.library.Region)
   */
  public Region removeNeighbor(Direction dir) {
    if (neighbors == null) {
      neighbors = evaluateNeighbours();
    }
    return neighbors.remove(dir);
  }

  /**
   * Returns a collection of ids for reachable neighbours. This may be set by setNeighbours() if
   * neighbours is null it will be calculated from the game data). This function may be necessary
   * for new xml reports.
   *
   * @deprecated better use {@link #getNeighbors()}.
   */
  @Deprecated
  public Collection<CoordinateID> getNeighbours() {
    Collection<CoordinateID> result = new ArrayList<CoordinateID>();
    for (Region r : getNeighbors().values()) {
      result.add(r.getID());
    }
    return result;
  }

  /**
   * @see magellan.library.Region#getNeighbors()
   */
  public Map<Direction, Region> getNeighbors() {
    if (neighbors == null)
      return evaluateNeighbours();

    return Collections.unmodifiableMap(neighbors);
  }

  private Map<Direction, Region> evaluateNeighbours() {
    if ((getData() == null) || (getData().getRegions() == null))
      return null;

    Map<Direction, Region> newNeighbors = Regions.getCoordinateNeighbours(data, getID());

    return newNeighbors;
  }

  /**
   * returns 1 if coast is nearby returns 0 if there es no coast
   *
   * @return the ozeanWithCoast
   */
  public int getOceanWithCoast() {
    if (ozeanWithCoast == -1) {
      // value was not set until now
      ozeanWithCoast = calcOceanWithCoast();
    }
    return ozeanWithCoast;
  }

  /**
   * calculates the OzeanWithCoast-value
   *
   * @return 1 if this region is ocean and has neighboring non-ocean regions
   */
  private int calcOceanWithCoast() {
    // start only if we are a ozean region
    if (!getRegionType().isOcean())
      return 0;
    // run through the neighbors
    for (Region n : getNeighbors().values()) {
      if (n.getRegionType().isLand())
        return 1;
    }
    return 0;
  }

  /**
   * Used for replacers..showing coordinates of region
   */
  public int getCoordX() {
    CoordinateID myCID = getCoordinate();
    return myCID.getX();
  }

  /**
   * Used for replacers..showing coordinates of region
   */
  public int getCoordY() {
    CoordinateID myCID = getCoordinate();
    return myCID.getY();
  }

  /**
   * @return the signLines
   */
  public Collection<Sign> getSigns() {
    return signs;
  }

  /**
   * @see magellan.library.Region#addSign(magellan.library.Sign)
   */
  public void addSign(Sign s) {
    if (signs == null) {
      signs = new ArrayList<Sign>(1);
    }
    signs.add(s);
  }

  /**
   * @see magellan.library.Region#addSigns(java.util.Collection)
   */
  public void addSigns(Collection<Sign> c) {
    if (signs == null) {
      signs = new ArrayList<Sign>(1);
    }
    signs.addAll(c);
  }

  /**
   * @see magellan.library.Region#clearSigns()
   */
  public void clearSigns() {
    if (signs != null) {
      signs.clear();
    }
  }

  /**
   * Returns the value of events.
   *
   * @return Returns events.
   */
  public List<Message> getEvents() {
    return events;
  }

  /**
   * Sets the value of events.
   *
   * @param events The value for events.
   */
  public void setEvents(List<Message> events) {
    this.events = events;
  }

  /**
   * Returns the value of herb.
   *
   * @return Returns herb.
   */
  public ItemType getHerb() {
    return herb;
  }

  /**
   * Sets the value of herb.
   *
   * @param herb The value for herb.
   */
  public void setHerb(ItemType herb) {
    this.herb = herb;
  }

  /**
   * Returns the value of herbAmount.
   *
   * @return Returns herbAmount.
   */
  public String getHerbAmount() {
    return herbAmount;
  }

  /**
   * Sets the value of herbAmount.
   *
   * @param herbAmount The value for herbAmount.
   */
  public void setHerbAmount(String herbAmount) {
    this.herbAmount = herbAmount;
  }

  /**
   * Returns the value of horses.
   *
   * @return Returns horses.
   */
  public int getHorses() {
    ItemType horsesIT = data.getRules().getItemType(EresseaConstants.I_RHORSES);
    if (horsesIT != null) {
      RegionResource horseRR = getResource(horsesIT);
      if (horseRR != null)
        return horseRR.getAmount();
    }
    return horses;
  }

  /**
   * Sets the value of horses.
   *
   * @param horses The value for horses.
   */
  public void setHorses(int horses) {
    this.horses = horses;
  }

  /**
   * Returns the value of iron.
   *
   * @return Returns iron.
   */
  public int getIron() {
    ItemType ironIT = data.getRules().getItemType(EresseaConstants.I_RIRON);
    RegionResource ironRR = getResource(ironIT);
    if (ironRR != null)
      return ironRR.getAmount();
    return iron;
  }

  /**
   * Sets the value of iron.
   *
   * @param iron The value for iron.
   */
  public void setIron(int iron) {
    this.iron = iron;
  }

  /**
   * Returns the value of laen.
   *
   * @return Returns laen.
   */
  public int getLaen() {
    ItemType laenIT = data.getRules().getItemType(EresseaConstants.I_RLAEN);
    RegionResource laenRR = getResource(laenIT);
    if (laenRR != null)
      return laenRR.getAmount();
    return laen;
  }

  /**
   * Sets the value of laen.
   *
   * @param laen The value for laen.
   */
  public void setLaen(int laen) {
    this.laen = laen;
  }

  /**
   * Returns the value of mallorn.
   *
   * @return Returns mallorn.
   */
  public boolean isMallorn() {
    return mallorn;
  }

  /**
   * Sets the value of mallorn.
   *
   * @param mallorn The value for mallorn.
   */
  public void setMallorn(boolean mallorn) {
    this.mallorn = mallorn;
  }

  /**
   * Returns the value of messages.
   *
   * @return Returns messages.
   */
  public List<Message> getMessages() {
    return messages;
  }

  /**
   * Sets the value of messages.
   *
   * @param messages The value for messages.
   */
  public void setMessages(List<Message> messages) {
    this.messages = messages;
  }

  /**
   * Returns the value of oldHorses.
   *
   * @return Returns oldHorses.
   */
  public int getOldHorses() {
    return oldHorses;
  }

  /**
   * Sets the value of oldHorses.
   *
   * @param oldHorses The value for oldHorses.
   */
  public void setOldHorses(int oldHorses) {
    this.oldHorses = oldHorses;
  }

  /**
   * Returns the value of oldIron.
   *
   * @return Returns oldIron.
   */
  public int getOldIron() {
    return oldIron;
  }

  /**
   * Sets the value of oldIron.
   *
   * @param oldIron The value for oldIron.
   */
  public void setOldIron(int oldIron) {
    this.oldIron = oldIron;
  }

  /**
   * Returns the value of oldLaen.
   *
   * @return Returns oldLaen.
   */
  public int getOldLaen() {
    return oldLaen;
  }

  /**
   * Sets the value of oldLaen.
   *
   * @param oldLaen The value for oldLaen.
   */
  public void setOldLaen(int oldLaen) {
    this.oldLaen = oldLaen;
  }

  /**
   * Returns the value of oldPeasants.
   *
   * @return Returns oldPeasants.
   */
  public int getOldPeasants() {
    return oldPeasants;
  }

  /**
   * Sets the value of oldPeasants.
   *
   * @param oldPeasants The value for oldPeasants.
   */
  public void setOldPeasants(int oldPeasants) {
    this.oldPeasants = oldPeasants;
  }

  /**
   * Returns the value of oldPrices.
   *
   * @return Returns oldPrices.
   */
  public Map<StringID, LuxuryPrice> getOldPrices() {
    return oldPrices;
  }

  /**
   * Sets the value of oldPrices.
   *
   * @param oldPrices The value for oldPrices.
   */
  public void setOldPrices(Map<StringID, LuxuryPrice> oldPrices) {
    this.oldPrices = oldPrices;
  }

  /**
   * Returns the value of oldRecruits.
   *
   * @return Returns oldRecruits.
   */
  public int getOldRecruits() {
    return oldRecruits;
  }

  /**
   * Sets the value of oldRecruits.
   *
   * @param oldRecruits The value for oldRecruits.
   */
  public void setOldRecruits(int oldRecruits) {
    this.oldRecruits = oldRecruits;
  }

  /**
   * Returns the value of oldSilver.
   *
   * @return Returns oldSilver.
   */
  public int getOldSilver() {
    return oldSilver;
  }

  /**
   * Sets the value of oldSilver.
   *
   * @param oldSilver The value for oldSilver.
   */
  public void setOldSilver(int oldSilver) {
    this.oldSilver = oldSilver;
  }

  /**
   * Returns the value of oldSprouts.
   *
   * @return Returns oldSprouts.
   */
  public int getOldSprouts() {
    return oldSprouts;
  }

  /**
   * Sets the value of oldSprouts.
   *
   * @param oldSprouts The value for oldSprouts.
   */
  public void setOldSprouts(int oldSprouts) {
    this.oldSprouts = oldSprouts;
  }

  /**
   * Returns the value of oldStones.
   *
   * @return Returns oldStones.
   */
  public int getOldStones() {
    return oldStones;
  }

  /**
   * Sets the value of oldStones.
   *
   * @param oldStones The value for oldStones.
   */
  public void setOldStones(int oldStones) {
    this.oldStones = oldStones;
  }

  /**
   * Returns the value of oldTrees.
   *
   * @return Returns oldTrees.
   */
  public int getOldTrees() {
    return oldTrees;
  }

  /**
   * Sets the value of oldTrees.
   *
   * @param oldTrees The value for oldTrees.
   */
  public void setOldTrees(int oldTrees) {
    this.oldTrees = oldTrees;
  }

  /**
   * Returns the value of oldWage.
   *
   * @return Returns oldWage.
   */
  public int getOldWage() {
    return oldWage;
  }

  /**
   * Sets the value of oldWage.
   *
   * @param oldWage The value for oldWage.
   */
  public void setOldWage(int oldWage) {
    this.oldWage = oldWage;
  }

  /**
   * Returns the value of orcInfested.
   *
   * @return Returns orcInfested.
   */
  public boolean isOrcInfested() {
    return orcInfested;
  }

  /**
   * Sets the value of orcInfested.
   *
   * @param orcInfested The value for orcInfested.
   */
  public void setOrcInfested(boolean orcInfested) {
    this.orcInfested = orcInfested;
  }

  /**
   * Returns the value of peasants.
   *
   * @return Returns peasants.
   */
  public int getPeasants() {
    ItemType peasantType = data.getRules().getItemType(EresseaConstants.I_PEASANTS);
    RegionResource peasantResource = getResource(peasantType);
    if (peasantResource != null)
      return peasantResource.getAmount();
    return peasants;
  }

  /**
   * Sets the value of peasants.
   *
   * @param peasants The value for peasants.
   */
  public void setPeasants(int peasants) {
    this.peasants = peasants;
  }

  /**
   * Returns the value of playerMessages.
   *
   * @return Returns playerMessages.
   */
  public List<Message> getPlayerMessages() {
    return playerMessages;
  }

  /**
   * Sets the value of playerMessages.
   *
   * @param playerMessages The value for playerMessages.
   */
  public void setPlayerMessages(List<Message> playerMessages) {
    this.playerMessages = playerMessages;
  }

  /**
   * Returns the value of prices.
   *
   * @return Returns prices.
   */
  public Map<StringID, LuxuryPrice> getPrices() {
    return prices;
  }

  /**
   * Sets the value of prices.
   *
   * @param prices The value for prices.
   */
  public void setPrices(Map<StringID, LuxuryPrice> prices) {
    this.prices = prices;
  }

  /**
   * Returns the value of recruits if it is in the report, -1 otherwise.
   *
   * @return Returns recruits.
   * @see #maxRecruit()
   * @see #maxRecruit(int)
   */
  public int getRecruits() {
    return recruits;
  }

  /**
   * Sets the value of recruits.
   *
   * @param recruits The value for recruits.
   */
  public void setRecruits(int recruits) {
    this.recruits = recruits;
  }

  /**
   * Returns the value of silver.
   *
   * @return Returns silver.
   */
  public int getSilver() {
    RegionResource silverRR = getResource(data.getRules().getItemType(EresseaConstants.I_RSILVER));
    if (silverRR != null)
      return silverRR.getAmount();
    return silver;
  }

  /**
   * Sets the value of silver.
   *
   * @param silver The value for silver.
   */
  public void setSilver(int silver) {
    this.silver = silver;
  }

  /**
   * Returns the value of sprouts.
   *
   * @return Returns sprouts.
   */
  public int getSprouts() {
    ItemType sproutsIT = data.getRules().getItemType(EresseaConstants.I_SPROUTS);
    RegionResource sproutsRR = getResource(sproutsIT);
    if (sproutsRR != null)
      return sproutsRR.getAmount();
    return sprouts;
  }

  /**
   * Sets the value of sprouts.
   *
   * @param sprouts The value for sprouts.
   */
  public void setSprouts(int sprouts) {
    this.sprouts = sprouts;
  }

  /**
   * Returns the value of stones.
   *
   * @return Returns stones.
   */
  public int getStones() {
    ItemType stonesIT = data.getRules().getItemType(EresseaConstants.I_RSTONES);
    RegionResource stonesRR = getResource(stonesIT);
    if (stonesRR != null)
      return stonesRR.getAmount();
    return stones;
  }

  /**
   * Sets the value of stones.
   *
   * @param stones The value for stones.
   */
  public void setStones(int stones) {
    this.stones = stones;
  }

  /**
   * Returns the value of surroundings.
   *
   * @return Returns surroundings.
   */
  public List<Message> getSurroundings() {
    return surroundings;
  }

  /**
   * Sets the value of surroundings.
   *
   * @param surroundings The value for surroundings.
   */
  public void setSurroundings(List<Message> surroundings) {
    this.surroundings = surroundings;
  }

  /**
   * Returns the value of travelThru.
   *
   * @return returns list of DURCHREISE messages or <code>null</code>
   */
  public List<Message> getTravelThru() {
    return travelThru;
  }

  /**
   * Sets the value of travelThru.
   *
   * @param travelThru The value for travelThru.
   */
  public void setTravelThru(List<Message> travelThru) {
    this.travelThru = travelThru;
  }

  /**
   * Returns the value of travelThruShips.
   *
   * @return returns list of DURCHSCHIFFUNG messages or <code>null</code>
   */
  public List<Message> getTravelThruShips() {
    return travelThruShips;
  }

  /**
   * Sets the value of travelThruShips.
   *
   * @param travelThruShips The value for travelThruShips.
   */
  public void setTravelThruShips(List<Message> travelThruShips) {
    this.travelThruShips = travelThruShips;
  }

  /**
   * Returns the value of trees.
   *
   * @return Returns trees.
   */
  public int getTrees() {
    ItemType treesIT = data.getRules().getItemType(EresseaConstants.I_TREES);
    RegionResource treesRR = getResource(treesIT);
    if (treesRR != null)
      return treesRR.getAmount();
    return trees;
  }

  /**
   * Sets the value of trees.
   *
   * @param trees The value for trees.
   */
  public void setTrees(int trees) {
    this.trees = trees;
  }

  /**
   * Returns the value of wage.
   *
   * @return Returns wage.
   */
  public int getWage() {
    return wage;
  }

  /**
   * Sets the value of wage.
   *
   * @param wage The value for wage.
   */
  public void setWage(int wage) {
    this.wage = wage;
  }

  /**
   * Returns <code>true</code> if this region is the active region.
   *
   * @deprecated Use {@link GameData#getActiveRegion()} instead.
   */
  @Deprecated
  public boolean isActive() {
    return getData().getActiveRegion() == this;
  }

  /**
   * Marks the region as active
   *
   * @deprecated Use {@link GameData#setActiveRegion(Region)} instead.
   */
  @Deprecated
  public void setActive(boolean isActive) {
    getData().setActiveRegion(this);
  }

  /**
   * @see magellan.library.Region#getCoastBitMap()
   */
  public Integer getCoastBitMap() {
    return coastBitMask;
  }

  /**
   * @see magellan.library.Region#setCoastBitMap(java.lang.Integer)
   */
  public void setCoastBitMap(Integer bitMap) {
    coastBitMask = bitMap;
  }

  public boolean hasUID() {
    return uID != INVALID_UID;
  }

  /**
   * Returns the value of uID, the unique regionID generated and sent by the eressea server
   * (starting in turn 570)
   *
   * @return Returns uID.
   */
  public long getUID() {
    return uID;
  }

  /**
   * Sets the value of uID, the unique regionID generated and sent by the eressea server (starting
   * in turn 570)
   *
   * @param uid The value for uID.
   */
  public void setUID(long uid) {
    uID = uid;
  }

  /**
   * @see magellan.library.Region#getOwnerFaction()
   */
  public Faction getOwnerFaction() {
    return ownerFaction;
  }

  /**
   * @see magellan.library.Region#setOwnerFaction(magellan.library.Faction)
   */
  public void setOwnerFaction(Faction f) {
    ownerFaction = f;
  }

  /**
   * @see magellan.library.Region#getMorale()
   */
  public int getMorale() {
    return morale;
  }

  /**
   * @see magellan.library.Region#setMorale(int)
   */
  public void setMorale(int morale) {
    this.morale = morale;
  }

  /**
   * @see magellan.library.Region#getMourning()
   */
  public int getMourning() {
    return mourning;
  }

  /**
   * @see magellan.library.Region#setMourning(int)
   */
  public void setMourning(int newMourning) {
    mourning = newMourning;
  }

  /**
   * @see magellan.library.Region#removeMaintenance(magellan.library.Unit)
   */
  public void removeMaintenance(Unit u) {
    maintained.remove(u);
  }

  /**
   * @see magellan.library.Region#addMaintenance(magellan.library.Unit)
   */
  public void addMaintenance(Unit u) {
    maintained.add(u);
  }

  /**
   * @see magellan.library.Region#getMaintained()
   */
  public Set<Unit> getMaintained() {
    for (Iterator<Unit> it = maintained.iterator(); it.hasNext();) {
      Unit u = it.next();
      if (!getCoordinate().equals(u.getNewRegion())) {
        it.remove();
      }
    }
    return Collections.unmodifiableSet(maintained);
  }

}
