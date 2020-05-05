/*
 * Copyright (C) 2000-2004 Roger Butenuth, Andreas Gampe, Stefan Goetz, Sebastian Pappert, Klaas
 * Prause, Enno Rehling, Sebastian Tusk, Ulrich Kuester, Ilja Pavkovic This file is part of the
 * Eressea Java Code Base, see the file LICENSING for the licensing information applying to this
 * file.
 */

package magellan.library;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import magellan.library.rules.ItemType;
import magellan.library.rules.RegionType;
import magellan.library.utils.Direction;

/**
 * Represents a region of a report.
 */
public interface Region extends UnitContainer, Selectable {

  /** The invalid region ID */
  static final long INVALID_UID = Integer.MIN_VALUE + 1;

  /** A visibility string for {@link Visibility#NEIGHBOR} */
  static final String VIS_STR_NEIGHBOUR = "neighbour";
  /** A visibility string for {@link Visibility#LIGHTHOUSE} */
  static final String VIS_STR_LIGHTHOUSE = "lighthouse";
  /** A visibility string for {@link Visibility#TRAVEL} */
  static final String VIS_STR_TRAVEL = "travel";
  /** A visibility string for {@link Visibility#UNIT} */
  static final String VIS_STR_UNIT = "unit";
  /** A visibility string for {@link Visibility#FAR} */
  static final String VIS_STR_FAR = "far";

  /** A visibility string for {@link Visibility#WRAP} */
  static final String VIS_STR_WRAP = "wrap";
  /** A visibility string for {@link Visibility#BATTLE} */
  static final String VIS_STR_BATTLE = "battle";

  /**
   * 0..very poor - no info (->visibility=null)<br />
   * 1..neighbour<br />
   * 2..lighthouse<br />
   * 3..travel<br />
   * 4..qualified unit in region (->visibility=null)
   */
  public static class Visibility {

    // as enum:
    // /**
    // * ..very poor - no info (->visibility=null)<br />
    // */
    // NULL,
    // // /**
    // // * ..for toroidal worlds
    // // */
    // // WRAP,
    // /**
    // * ..neighbour
    // */
    // NEIGHBOR,
    // /**
    // * ..lighthouse
    // */
    // LIGHTHOUSE,
    // /**
    // * ..travel
    // */
    // TRAVEL,
    // /**
    // * ..qualified unit in region (->visibility=null)
    // */
    // UNIT;

    // as class:
    private int vis;
    private String name;

    protected Visibility(int vis, String name) {
      this.vis = vis;
      this.name = name;
    }

    /**
     * 0..very poor - no info (->visibility=null)<br />
     */
    public static final Visibility NULL = new Visibility(0, "NULL");
    /**
     * 1..neighbour
     */
    public static final Visibility NEIGHBOR = new Visibility(1, "NEIGHBOR");
    /**
     * 2..lighthouse
     */
    public static final Visibility LIGHTHOUSE = new Visibility(2, "LIGHTHOUSE");
    /**
     * 3..travel
     */
    public static final Visibility TRAVEL = new Visibility(3, "TRAVEL");
    /**
     * 4..this also exists, but I'm not sure what it does
     */
    public static final Visibility FAR = new Visibility(4, "FAR");
    /**
     * 5..qualified unit in region (->visibility=null)
     */
    public static final Visibility UNIT = new Visibility(5, "UNIT");
    /**
     * 6..this also exists, but I'm not sure what it does
     */
    public static final Visibility BATTLE = new Visibility(6, "BATTLE");
    /**
     * 7..for toroidal worlds
     */
    public static final Visibility WRAP = new Visibility(7, "WRAP");

    protected int ordinal() {
      return vis;
    }

    /**
     * Returns the greater of the two visibilities. The order of visibilities is
     * <code>UNIT > TRAVEL > LIGHTHOUSE > NEIGHBOR > WRAP > NULL </code>.
     */
    public static Visibility getMax(Visibility vis1, Visibility vis2) {
      return vis1.ordinal() >= vis2.ordinal() ? vis1 : vis2;
    }

    /**
     * Returns <code>true</code> if <code>this</code> is at least <code>other</code>.
     */
    public boolean greaterEqual(Visibility other) {
      return Visibility.getMax(this, other) == this;
    }

    /**
     * Returns <code>true</code> if <code>other</code> is at least <code>this</code>.
     */
    public boolean lessEqual(Visibility other) {
      return Visibility.getMax(this, other) == other;
    }

    /**
     * Returns <code>true</code> if <code>other</code> is more than <code>this</code>.
     */
    public boolean lessThan(Visibility other) {
      return Visibility.getMax(this, other) != this;
    }

    /**
     * Returns <code>true</code> if <code>this</code> is at least <code>other</code>.
     */
    public boolean greaterThan(Visibility other) {
      return Visibility.getMax(this, other) != other;
    }

    /**
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
      return name;
    }
    // @Override
    // public int compareTo(Visibility other) {
    //
    // }
  }

  /**
   * Returns true if this region has fog of war.
   */
  public boolean fogOfWar();

  /**
   * Sets the fog of war property. 1 means fog of war "on", 0 means "off" and -1 will re-compute the
   * fog from privileged units in the region.
   */
  public void setFogOfWar(int fog);

  /**
   * Returns the zero unit ("the peasants") of this region.
   */
  public Unit getZeroUnit();

  /**
   * Returns the number of modified peasants after a GIVE 0 or recruiting.
   * 
   * @return the number of modified persons after "give 0", recruit
   */
  public int getModifiedPeasants();

  /**
   * Returns the number of possible recruits.
   */
  public int modifiedRecruit();

  /**
   * Sets the island this region belongs to.
   */
  public void setIsland(Island i);

  /**
   * Returns the island this region belongs to.
   */
  public Island getIsland();

  /**
   * A string constant indicating why this region is visible. This should only be used when
   * writing/reading a CR. Otherwise use {@link #getVisibility()}.
   * 
   * @return the string object or null, if the visibility is unspecified.
   */
  public String getVisibilityString();

  /**
   * Sets a string constant indicating why this region is visible. This should only be used when
   * writing/reading a CR. Otherwise use {@link #setVisibility(Visibility)}.
   * 
   * @param vis a String object or null to indicate that the visibility cannot be determined.
   */
  public void setVisibilityString(String vis);

  /**
   * Represents the quality of the visibility as an int value. 0..very poor - no info
   * (->visibility=null)<br />
   * 1..neighbour <br />
   * 2..lighthouse <br />
   * 3..travel <br />
   * 4..qualified unit in region (->visibility=null)
   */
  public Visibility getVisibility();

  /**
   * 0..very poor - no info (->visibility=null) <br />
   * 1..neighbour <br />
   * 2..lighthouse <br />
   * 3..travel <br />
   * 4..qualified unit in region (->visibility=null)
   * 
   * @param v
   */
  public void setVisibility(Visibility v);

  /**
   * Returns all resources of this region.
   */
  public Collection<RegionResource> resources();

  /**
   * Adds a resource to this region.
   * 
   * @throws NullPointerException
   */
  public RegionResource addResource(RegionResource resource);

  /**
   * Removes the resource with the specified numerical id or the id of its item type from this
   * region.
   * 
   * @return the removed resource or null if no resource with the specified id exists in this
   *         region.
   */
  public RegionResource removeResource(RegionResource r);

  /**
   * Removes any reference to the Resource with ItemType type.
   * 
   * @param type
   * @return The resource that was removed if any, <code>null</code> otherwise
   */
  public RegionResource removeResource(ItemType type);

  /**
   * Removes all resources from this region.
   */
  public void clearRegionResources();

  /**
   * Returns the resource with the ID of its item type.
   * 
   * @param type may be <code>null</code> in which case <code>null</code> will be returned
   * @return the resource object or null if no resource with the specified ID exists in this region.
   */
  public RegionResource getResource(ItemType type);

  /**
   * Returns all schemes of this region.
   */
  public Collection<Scheme> schemes();

  /**
   * Adds a scheme to this region.
   * 
   * @throws NullPointerException
   */
  public Scheme addScheme(Scheme scheme);

  /**
   * Removes all schemes from this region.
   */
  public void clearSchemes();

  /**
   * Returns the scheme with the specified corodinate.
   * 
   * @return the scheme object or null if no scheme with the specified ID exists in this region.
   */
  public Scheme getScheme(ID id);

  /**
   * Returns all borders of this region.
   */
  public Collection<Border> borders();

  /**
   * Adds a border to this region.
   * 
   * @throws NullPointerException if border is <code>null</code>
   */
  public Border addBorder(Border border);

  /**
   * Removes all borders from this region.
   */
  public void clearBorders();

  /**
   * Returns the border with the specified id.
   * 
   * @return the border object or null if no border with the specified id exists in this region.
   */
  public Border getBorder(ID key);

  /**
   * Returns an unmodifiable collection of all the ships in this container.
   */
  public Collection<Ship> ships();

  /**
   * Retrieve a ship in this container by id.
   */
  public Ship getShip(ID key);

  /**
   * Adds a ship to this container. This method should only be invoked by Ship.setXXX() methods.
   */
  public void addShip(Ship s);

  /**
   * Removes a ship from this container. This method should only be invoked by Ship.setXXX()
   * methods.
   */
  public Ship removeShip(Ship s);

  /**
   * Returns an unmodifiable collection of all the buildings in this container.
   */
  public Collection<Building> buildings();

  /**
   * Retrieve a building in this container by id.
   */
  public Building getBuilding(ID key);

  /**
   * Adds a building to this container. This method should only be invoked by Building.setXXX()
   * methods.
   */
  public void addBuilding(Building u);

  /**
   * Removes a building from this container. This method should only be invoked by Building.setXXX()
   * methods.
   */
  public Building removeBuilding(Building b);

  /**
   * Returns the maximum number of persons that can be recruited in this region.
   */
  public int maxRecruit();

  /**
   * Returns the maximum number of persons that can be recruited in this region.
   */
  public int maxOldRecruit();

  /**
   * Returns the silver that can be earned through entertainment in this region. Returns
   * Integer.MIN_VALUE if this is not applicable.
   */
  public int maxEntertain();

  /**
   * Returns the silver that could be earned through entertainment in this region.
   */
  public int maxOldEntertain();

  /**
   * Returns the maximum number of luxury items that can be bought in this region without a price
   * penalty.
   */
  public int maxLuxuries();

  /**
   * Returns the maximum number of luxury items that could be bought in this region without a price
   * penalty.
   */
  public int maxOldLuxuries();

  /**
   * Sets the number of luxury items that could be bought last round.
   * 
   * @param amount The new value
   */
  public void setOldLuxuries(int amount);

  /**
   * Calculates the wage a peasant earns according to the biggest castle in this region. While the
   * value of the wage field is directly taken from the report and may be biased by the race of the
   * owner faction of that report, this function tries to determine the real wage a peasant can earn
   * in this region. Wage for player persons can be derived from that value
   */
  public int getPeasantWage();

  /**
   * Returns a String representation of this Region object. If region has no name the string
   * representation of the region type is used.
   */
  public String toString();

  /**
   * Returns the coordinate of this region. This method is only a type-safe short cut for retrieving
   * and converting the ID object of this region.
   */
  public CoordinateID getCoordinate();

  /**
   * A synonym of {@link #getCoordinate()}. Regions are identified by their Coordinate, not by their
   * region ID. This has historic reasons, but there could now also be multiple regions with the
   * same region id.
   * 
   * @see magellan.library.Identifiable#getID()
   */
  public CoordinateID getID();

  /**
   * Returns the RegionType of this region. This method is only a type-safe short cut for retrieving
   * and converting the RegionType of this region.
   */
  public RegionType getRegionType();

  /**
   * Refreshes all the relations of all units in this region. It is preferrable to call this method
   * instead of refreshing the unit relations 'manually'.
   */
  public void refreshUnitRelations();

  /**
   * Refreshes all the relations of all units in this region. It is preferrable to call this method
   * instead of refreshing the unit relations 'manually'.
   * 
   * @param forceRefresh to enforce refreshment, false for one refreshment only
   * @deprecated should be triggered implicitly by UnitOrdersEvents
   */
  @Deprecated
  public void refreshUnitRelations(boolean forceRefresh);

  /**
   * Add guarding Unit to region.
   */
  public void addGuard(Unit u);

  /**
   * Get The List of guarding Units.
   */
  public List<Unit> getGuards();

  /**
   * Get the unit with the given ID if it's in the region, <code>null</code> otherwise.
   */
  public Unit getUnit(ID key);

  /**
   * Sets the collection of ids for reachable regions to <tt>neighbours</tt>. If <tt>neighbours</tt>
   * is null they will be evaluated.
   * 
   * @throws IllegalArgumentException if one of the neighbours doesn't exist in the data.
   * @deprecated Use {@link #setNeighbors(Map)}
   */
  @Deprecated
  public void setNeighbours(Collection<CoordinateID> neighbours);

  /**
   * Sets the collection of ids for reachable regions to <tt>neighbors</tt>. If <tt>neighbors</tt>
   * is null they will be evaluated from Coordinate neighbors.
   */
  public void setNeighbors(Map<Direction, Region> neighbors);

  /**
   * Adds a neighbor in the specified direction.
   * 
   * @return The old neighbor in this direction, if there was one, otherwise <code>null</code>.
   */
  public Region addNeighbor(Direction dir, Region newNeighbor);

  /**
   * Removes the neighbor in the specified direction.
   */
  public Region removeNeighbor(Direction d);

  /**
   * Returns a collection of ids for reachable neighbours. This may be set by setNeighbours() if
   * neighbours is null it will be calculated from the game data). This function may be necessary
   * for new xml reports.
   * 
   * @deprecated Use {@link #getNeighbors()}.
   */
  @Deprecated
  public Collection<CoordinateID> getNeighbours();

  /**
   * Returns a map for reachable neighbors. If the neighbors have not been set by setNeighbors(), it
   * will be calculated from the game data).
   */
  public Map<Direction, Region> getNeighbors();

  /**
   * @return the ozeanWithCoast
   */
  public int getOceanWithCoast();

  /**
   * Used for replacers..showing coordinates of region
   */
  public int getCoordX();

  /**
   * Used for replacers..showing coordinates of region
   */
  public int getCoordY();

  /**
   * @return the signLines
   */
  public Collection<Sign> getSigns();

  /**
   * Adds a sign to the region.
   */
  public void addSign(Sign s);

  /**
   * Add all signs in c to the region.
   */
  public void addSigns(Collection<Sign> c);

  /**
   * Remove all signs
   */
  public void clearSigns();

  /**
   * Returns the value of events.
   * 
   * @return Returns events.
   */
  public List<Message> getEvents();

  /**
   * Sets the value of events.
   * 
   * @param events The value for events.
   */
  public void setEvents(List<Message> events);

  /**
   * Returns the value of herb.
   * 
   * @return Returns herb.
   */
  public ItemType getHerb();

  /**
   * Sets the value of herb.
   * 
   * @param herb The value for herb.
   */
  public void setHerb(ItemType herb);

  /**
   * Returns the value of herbAmount.
   * 
   * @return Returns herbAmount.
   */
  public String getHerbAmount();

  /**
   * Sets the value of herbAmount.
   * 
   * @param herbAmount The value for herbAmount.
   */
  public void setHerbAmount(String herbAmount);

  /**
   * Returns the value of horses.
   * 
   * @return Returns horses.
   */
  public int getHorses();

  /**
   * Sets the value of horses.
   * 
   * @param horses The value for horses.
   */
  public void setHorses(int horses);

  /**
   * Returns the value of iron.
   * 
   * @return Returns iron.
   */
  public int getIron();

  /**
   * Sets the value of iron.
   * 
   * @param iron The value for iron.
   */
  public void setIron(int iron);

  /**
   * Returns the value of laen.
   * 
   * @return Returns laen.
   */
  public int getLaen();

  /**
   * Sets the value of laen.
   * 
   * @param laen The value for laen.
   */
  public void setLaen(int laen);

  /**
   * Returns the value of mallorn.
   * 
   * @return Returns mallorn.
   */
  public boolean isMallorn();

  /**
   * Sets the value of mallorn.
   * 
   * @param mallorn The value for mallorn.
   */
  public void setMallorn(boolean mallorn);

  /**
   * Returns the value of messages.
   * 
   * @return Returns messages.
   */
  public List<Message> getMessages();

  /**
   * Sets the value of messages.
   * 
   * @param messages The value for messages.
   */
  public void setMessages(List<Message> messages);

  /**
   * Returns the value of oldHorses.
   * 
   * @return Returns oldHorses.
   */
  public int getOldHorses();

  /**
   * Sets the value of oldHorses.
   * 
   * @param oldHorses The value for oldHorses.
   */
  public void setOldHorses(int oldHorses);

  /**
   * Returns the value of oldIron.
   * 
   * @return Returns oldIron.
   */
  public int getOldIron();

  /**
   * Sets the value of oldIron.
   * 
   * @param oldIron The value for oldIron.
   */
  public void setOldIron(int oldIron);

  /**
   * Returns the value of oldLaen.
   * 
   * @return Returns oldLaen.
   */
  public int getOldLaen();

  /**
   * Sets the value of oldLaen.
   * 
   * @param oldLaen The value for oldLaen.
   */
  public void setOldLaen(int oldLaen);

  /**
   * Returns the value of oldPeasants.
   * 
   * @return Returns oldPeasants.
   */
  public int getOldPeasants();

  /**
   * Sets the value of oldPeasants.
   * 
   * @param oldPeasants The value for oldPeasants.
   */
  public void setOldPeasants(int oldPeasants);

  /**
   * Returns the value of oldPrices.
   * 
   * @return Returns oldPrices.
   */
  public Map<StringID, LuxuryPrice> getOldPrices();

  /**
   * Sets the value of oldPrices.
   * 
   * @param oldPrices The value for oldPrices.
   */
  public void setOldPrices(Map<StringID, LuxuryPrice> oldPrices);

  /**
   * Returns the value of oldRecruits.
   * 
   * @return Returns oldRecruits.
   */
  public int getOldRecruits();

  /**
   * Sets the value of oldRecruits.
   * 
   * @param oldRecruits The value for oldRecruits.
   */
  public void setOldRecruits(int oldRecruits);

  /**
   * Returns the value of oldSilver.
   * 
   * @return Returns oldSilver.
   */
  public int getOldSilver();

  /**
   * Sets the value of oldSilver.
   * 
   * @param oldSilver The value for oldSilver.
   */
  public void setOldSilver(int oldSilver);

  /**
   * Returns the value of oldSprouts.
   * 
   * @return Returns oldSprouts.
   */
  public int getOldSprouts();

  /**
   * Sets the value of oldSprouts.
   * 
   * @param oldSprouts The value for oldSprouts.
   */
  public void setOldSprouts(int oldSprouts);

  /**
   * Returns the value of oldStones.
   * 
   * @return Returns oldStones.
   */
  public int getOldStones();

  /**
   * Sets the value of oldStones.
   * 
   * @param oldStones The value for oldStones.
   */
  public void setOldStones(int oldStones);

  /**
   * Returns the value of oldTrees.
   * 
   * @return Returns oldTrees.
   */
  public int getOldTrees();

  /**
   * Sets the value of oldTrees.
   * 
   * @param oldTrees The value for oldTrees.
   */
  public void setOldTrees(int oldTrees);

  /**
   * Returns the value of oldWage.
   * 
   * @return Returns oldWage.
   */
  public int getOldWage();

  /**
   * Sets the value of oldWage.
   * 
   * @param oldWage The value for oldWage.
   */
  public void setOldWage(int oldWage);

  /**
   * Returns the value of orcInfested.
   * 
   * @return Returns orcInfested.
   */
  public boolean isOrcInfested();

  /**
   * Sets the value of orcInfested.
   * 
   * @param orcInfested The value for orcInfested.
   */
  public void setOrcInfested(boolean orcInfested);

  /**
   * Returns the value of peasants.
   * 
   * @return Returns peasants.
   */
  public int getPeasants();

  /**
   * Sets the value of peasants.
   * 
   * @param peasants The value for peasants.
   */
  public void setPeasants(int peasants);

  /**
   * Returns the value of playerMessages.
   * 
   * @return Returns playerMessages.
   */
  public List<Message> getPlayerMessages();

  /**
   * Sets the value of playerMessages.
   * 
   * @param playerMessages The value for playerMessages.
   */
  public void setPlayerMessages(List<Message> playerMessages);

  /**
   * Returns the value of prices.
   * 
   * @return Returns prices.
   */
  public Map<StringID, LuxuryPrice> getPrices();

  /**
   * Sets the value of prices.
   * 
   * @param prices The value for prices.
   */
  public void setPrices(Map<StringID, LuxuryPrice> prices);

  /**
   * Returns the value of recruits.
   * 
   * @return Returns recruits.
   */
  public int getRecruits();

  /**
   * Sets the value of recruits.
   * 
   * @param recruits The value for recruits.
   */
  public void setRecruits(int recruits);

  /**
   * Returns the value of silver.
   * 
   * @return Returns silver.
   */
  public int getSilver();

  /**
   * Sets the value of silver.
   * 
   * @param silver The value for silver.
   */
  public void setSilver(int silver);

  /**
   * Returns the value of sprouts.
   * 
   * @return Returns sprouts.
   */
  public int getSprouts();

  /**
   * Sets the value of sprouts.
   * 
   * @param sprouts The value for sprouts.
   */
  public void setSprouts(int sprouts);

  /**
   * Returns the value of stones.
   * 
   * @return Returns stones.
   */
  public int getStones();

  /**
   * Sets the value of stones.
   * 
   * @param stones The value for stones.
   */
  public void setStones(int stones);

  /**
   * Returns the value of surroundings.
   * 
   * @return Returns surroundings.
   */
  public List<Message> getSurroundings();

  /**
   * Sets the value of surroundings.
   * 
   * @param surroundings The value for surroundings.
   */
  public void setSurroundings(List<Message> surroundings);

  /**
   * Returns the value of travelThru.
   * 
   * @return Returns travelThru.
   */
  public List<Message> getTravelThru();

  /**
   * Sets the value of travelThru.
   * 
   * @param travelThru The value for travelThru.
   */
  public void setTravelThru(List<Message> travelThru);

  /**
   * Returns the value of travelThruShips.
   * 
   * @return Returns travelThruShips.
   */
  public List<Message> getTravelThruShips();

  /**
   * Sets the value of travelThruShips.
   * 
   * @param travelThruShips The value for travelThruShips.
   */
  public void setTravelThruShips(List<Message> travelThruShips);

  /**
   * Returns the value of trees.
   * 
   * @return Returns trees.
   */
  public int getTrees();

  /**
   * Sets the value of trees.
   * 
   * @param trees The value for trees.
   */
  public void setTrees(int trees);

  /**
   * Returns the value of wage. Returns -1 if unknown.
   * 
   * @return Returns wage.
   */
  public int getWage();

  /**
   * Sets the value of wage.
   * 
   * @param wage The value for wage.
   */
  public void setWage(int wage);

  /**
   * Returns <code>true</code> if this region is the activ region.
   * 
   * @deprecated Use {@link GameData#getActiveRegion()} instead.
   */
  @Deprecated
  public boolean isActive();

  /**
   * Marks the region as active
   * 
   * @deprecated Use {@link GameData#setActiveRegion(Region)} instead.
   */
  @Deprecated
  public void setActive(boolean isActive);

  /**
   * The returned integer is an BitMap representing the info, if neighboring regions are ozean or
   * not-<br />
   * BitMask 1: dir = 0<br />
   * BitMask 2: dir = 1<br />
   * BitMask 4: dir = 2 ....<br />
   * BitMask 64: random bit 1 (for variable effects like ice)<br />
   * BitMask 128: random bit 2<br />
   * Bit = 1 -> there is land!<br />
   * Bit = 0 -> there is ozean!
   * 
   * @return an Integer as BitMap
   */
  public Integer getCoastBitMap();

  /**
   * The given integer is an BitMap representing the info, if neighboring regions are ozean or not.<br />
   * BitMask 1: dir = 0<br />
   * BitMask 2: dir = 1<br />
   * BitMask 4: dir = 2 ....<br />
   * BitMask 64: random bit 1 (for variable effects like ice)<br />
   * BitMask 128: random bit 2<br />
   * Bit = 1 -> there is land!<br />
   * Bit = 0 -> there is ozean!
   * 
   * @param bitMap an Integer as BitMap
   */
  public void setCoastBitMap(Integer bitMap);

  /**
   * Returns <code>true</code> if a UID has been set.
   */
  public boolean hasUID();

  /**
   * Returns the unique regionID generated by the eressea-server. Note that this is no longer
   * necessarily unique, due to wrap around effects! Real region IDs (from the server) should be
   * >=0. Invented region IDs can be <0.
   */
  public long getUID();

  /**
   * Sets the given long value as the unique regionID. Note that this is no longer necessarily
   * unique, due to wrap around effects!
   */
  public void setUID(long uID);

  /**
   * Sets the faction owning this region (E3A only, currently)
   */
  public void setOwnerFaction(Faction f);

  /**
   * Returns the faction owning this region (E3A only, currently) or <code>null</code> if not
   * applicable.
   */
  public Faction getOwnerFaction();

  /**
   * Sets the region's morale (from 0 to 5(?)).
   */
  public void setMorale(int morale);

  /**
   * Gets the region's morale (from 0 to 5(?)) or -1 if not defined or known.
   */
  public int getMorale();

  /**
   * Returns the current mourning state or -1 if unknown.
   */
  public int getMourning();

  /**
   * Sets the current mourning state.
   */
  public void setMourning(int newMourning);

  // public void changeMaintenance(Faction faction, int delta);
  //
  // public int getMaintenance(Faction f);

  /**
   * Remove a unit from the list of units that may be in this region in the next turn.
   * 
   * @param u
   */
  public void removeMaintenance(Unit u);

  /**
   * Add a unit that may be in this region in the next turn.
   * 
   * @param u
   */
  public void addMaintenance(Unit u);

  /**
   * Returns a set of units that <em>may</em> be new in this region and thus need to be maintained.
   * Does not include units from {@link #units()}, but may contain units that won't actually be in
   * this region. Check {@link Unit#getNewRegion()} to verify.
   * 
   * @return The set of additional units
   */
  public Set<Unit> getMaintained();

}
