/*
 * Copyright (C) 2000-2004 Roger Butenuth, Andreas Gampe, Stefan Goetz, Sebastian Pappert, Klaas
 * Prause, Enno Rehling, Sebastian Tusk, Ulrich Kuester, Ilja Pavkovic This file is part of the
 * Eressea Java Code Base, see the file LICENSING for the licensing information applying to this
 * file.
 */

package magellan.library.utils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Random;
import java.util.Set;

import magellan.library.Border;
import magellan.library.Building;
import magellan.library.CoordinateID;
import magellan.library.GameData;
import magellan.library.ID;
import magellan.library.IntegerID;
import magellan.library.Message;
import magellan.library.Region;
import magellan.library.Rules;
import magellan.library.Ship;
import magellan.library.StringID;
import magellan.library.Unit;
import magellan.library.gamebinding.EresseaConstants;
import magellan.library.rules.BuildingType;
import magellan.library.rules.RegionType;
import magellan.library.utils.logging.Logger;

/**
 * A class offering common operations on regions.
 */
public class Regions {
  private static final Logger log = Logger.getInstance(Regions.class);

  /**
   * Retrieve the regions within radius around region center.
   * 
   * @param regions a map containing the existing regions.
   * @param center the region the neighbours of which are retrieved.
   * @param radius the maximum distance between center and any region to be regarded as a neighbour
   *          within radius.
   * @param excludedRegionTypes region types that disqualify regions as valid neighbours. This also
   *          may be null
   * @return a map with all neighbours that were found, including region center. The keys are
   *         instances of class ID, values are objects of class Region.
   * @throws IllegalArgumentException If center is not a CoordinatID
   * @deprecated now obsolete
   */
  @Deprecated
  public static Map<CoordinateID, Region> getAllNeighbours(Map<CoordinateID, Region> regions,
      ID center, int radius, Map<ID, RegionType> excludedRegionTypes) {
    if (center instanceof CoordinateID)
      return Regions.getAllNeighbours(regions, (CoordinateID) center, radius, excludedRegionTypes);
    else
      throw new IllegalArgumentException(
          "center is not an eressea coordinate. Support for e2 incomplete!");
  }

  /**
   * Retrieve the regions within radius around region center.
   * 
   * @param regions a map containing the existing regions.
   * @param center the region the neighbours of which are retrieved.
   * @param radius the maximum distance between center and any region to be regarded as a neighbour
   *          within radius.
   * @param excludedRegionTypes region types that disqualify regions as valid neighbours.
   * @return a map with all neighbours that were found, including region center. The keys are
   *         instances of class Coordinate, values are objects of class Region.
   */
  public static Map<CoordinateID, Region> getAllNeighbours(Map<CoordinateID, Region> regions,
      CoordinateID center, int radius, Map<ID, RegionType> excludedRegionTypes) {
    Map<CoordinateID, Region> neighbours = new Hashtable<CoordinateID, Region>();
    CoordinateID c = new CoordinateID(0, 0, center.z);

    for (int dx = -radius; dx <= radius; dx++) {
      for (int dy = (-radius + Math.abs(dx)) - ((dx > 0) ? dx : 0); dy <= ((radius - Math.abs(dx)) - ((dx < 0)
          ? dx : 0)); dy++) {
        c.x = center.x + dx;
        c.y = center.y + dy;

        Region neighbour = regions.get(c);

        if (neighbour != null) {
          if ((excludedRegionTypes == null)
              || !excludedRegionTypes.containsKey(neighbour.getType().getID())) {
            neighbours.put(neighbour.getID(), neighbour);
          }
        }
      }
    }

    return neighbours;
  }

  /**
   * Retrieve the regions directly connected with the center region (including it).
   * 
   * @param regions a map containing the existing regions.
   * @param center the region the neighbours of which are retrieved.
   * @param excludedRegionTypes region types that disqualify regions as valid neighbours.
   * @return a map with all neighbours that were found, including region center. The keys are
   *         instances of class Coordinate, values are objects of class Region.
   */
  public static Map<CoordinateID, Region> getAllNeighbours(Map<CoordinateID, Region> regions,
      CoordinateID center, Map<ID, RegionType> excludedRegionTypes) {
    return Regions.getAllNeighbours(regions, center, 1, excludedRegionTypes);
  }

  /**
   * Find a way from one region to another region and get the directions in which to move to follow
   * a sequence of regions. This is virtually the same as
   * 
   * <pre>
   * getDirections(getPath(regions, start, dest, excludedRegionTypes, radius));
   * </pre>
   * 
   * @return a String telling the direction statements necessary to follow the sequence of regions
   *         contained in regions.
   */
  public static String getDirections(GameData data, CoordinateID start, CoordinateID dest,
      Map<ID, RegionType> excludedRegionTypes, int radius) {
    return Regions.getDirections(Regions.getLandPath(data, start, dest, excludedRegionTypes,
        radius, radius));
  }

  /**
   * Get the directions in which to move to follow a sequence of regions.
   * 
   * @param regions an ordered consecutive sequence of regions.
   * @return a String telling the direction statements necessary to follow the sequence of regions
   *         contained in regions.
   */
  public static String getDirections(Collection<Region> regions) {
    if (regions == null)
      return null;

    List<Direction> directions = Regions.getDirectionObjectsOfRegions(regions);

    if (directions == null)
      return null;

    StringBuffer dir = new StringBuffer();

    for (Direction d : directions) {
      if (dir.length() > 0) {
        dir.append(" ");
      }

      dir.append(Direction.toString(d.getDir(), true));
    }

    return dir.toString();
  }

  /**
   * DOCUMENT-ME
   */
  public static List<Direction> getDirectionObjectsOfRegions(Collection<Region> regions) {
    if (regions == null)
      return null;

    List<CoordinateID> coordinates = new ArrayList<CoordinateID>(regions.size());

    for (Region r : regions) {
      coordinates.add(r.getCoordinate());
    }

    return Regions.getDirectionObjectsOfCoordinates(coordinates);
  }

  /**
   * DOCUMENT-ME
   */
  public static List<Direction> getDirectionObjectsOfCoordinates(
      Collection<CoordinateID> coordinates) {
    if (coordinates == null)
      return null;

    List<Direction> directions = new ArrayList<Direction>(coordinates.size());

    CoordinateID prev = null;
    CoordinateID cur = null;

    Iterator<CoordinateID> iter = coordinates.iterator();

    if (iter.hasNext()) {
      prev = iter.next();
      while (iter.hasNext()) {
        cur = iter.next();

        CoordinateID diffCoord = new CoordinateID(cur.x - prev.x, cur.y - prev.y, 0);
        int intDir = Direction.toInt(diffCoord);

        if (intDir != -1) {
          directions.add(new Direction(intDir));
        } // else {
        // Regions.log.warn("Regions.getDirectionsOfCoordinates(): invalid direction encountered");
        //
        // return null;
        // }

        prev = cur;
      }
    }

    return directions;
  }

  /**
   * Holds shortest-path relevant information for a region.
   * 
   * @author stm
   */
  public interface RegionInfo<T extends RegionInfo<?>> extends Comparable<T> {
    /**
     * Returns a distance value that is used to limit the search horizon.
     */
    public int getDistance();

    /**
     * Returns the info's region.
     */
    public Region getRegion();

    /**
     * Returns the id this info belongs to.
     */
    public CoordinateID getID();

    /**
     * Marks region as visited.
     */
    public void setVisited();

    /**
     * Returns if region is visited.
     */
    public boolean isVisited();

    /**
     * Returns the region that caused the last distance decrease.
     */
    public CoordinateID getPredecessor();

    /**
     * Sets the predecessor.
     */
    public void setPredecessor(CoordinateID pred);

    /**
     * Returns true if the region's distance is infinity.
     */
    public boolean isInfinity();

    /**
     * Sets the distance to infinity.
     */
    public void setInfinity();

    /**
     * Returns a string representation of the distance.
     */
    public String distString();
  }

  /**
   * A metric on Coordinates.
   * 
   * @author stm
   */
  public interface Metric {
    /**
     * Creates an entry with zero distance for the specified node.
     */
    public RegionInfo<?> createZero(CoordinateID node);

    /**
     * Creates an entry with infinite distance for the specified node.
     */
    public RegionInfo<?> createInfinity(CoordinateID node);

    /**
     * Returns the info for the node. Creates an infinite one if none exists.
     */
    public RegionInfo<?> get(CoordinateID node);

    /**
     * Returns all neighbors of the provided coordinate.
     */
    public Map<CoordinateID, Region> getNeighbours(CoordinateID id);

    /**
     * Changes the distance of <code>nextRecord</code> according to the distance of
     * <code>current</code> and the distance between the two regions.
     * 
     * @return <code>true</code> if <code>nextRecord</code>'s distance was decreased
     */
    public boolean relax(RegionInfo<?> current, RegionInfo<?> nextRecord);

    /**
     * Returns a map of all known distance values.
     */
    public Map<CoordinateID, ? extends RegionInfo<?>> getDistances();
  }

  /**
   * Returns distance information about all Coordinates in regions that are at most as far as dest
   * or as far as maxDist away from start. The result is stored in the metric information.
   * 
   * @param regions Regions will be looked up in this map.
   * @param start The origin of the search.
   * @param dest The coordinate that must be reached, or <code>null</code>
   * @param maxDist The maximum distance to search. The search stops if all regions with at most
   *          this distance have been visited.
   * @param metric A metric for computing distances.
   */
  public static void getDistances(Map<CoordinateID, Region> regions, CoordinateID start,
      CoordinateID dest, int maxDist, Metric metric) {
    if (!regions.containsKey(start) || (dest != null && !regions.containsKey(dest)))
      throw new IllegalArgumentException();

    // this method applies Dijkstra's algorithm

    // initialize queue
    PriorityQueue<RegionInfo<?>> queue = new PriorityQueue<RegionInfo<?>>(8);
    queue.add(metric.createZero(start));

    int touched = 0;
    int maxQueue = 1;

    while (!queue.isEmpty() && queue.peek().getDistance() <= maxDist
        && !queue.peek().getID().equals(dest)) {
      RegionInfo<?> current = queue.poll();
      // for debugging
      if (regions.get(current.getID()) != null
          && (metric instanceof LandMetric || metric instanceof ShipMetric)) {
        // regions.get(current.id).clearSigns();
        // regions.get(current.getID()).addSign(new Sign(touched + " " + current.distString()));
      } else {
        touched += 0;
      }
      touched++;
      current.setVisited();
      Map<CoordinateID, Region> neighbors = metric.getNeighbours(current.getID());
      for (CoordinateID next : neighbors.keySet()) {
        RegionInfo<?> nextRecord = metric.get(next);
        if (!nextRecord.isVisited()) {
          // PriorityQueue doesn't have a decreasekey, so we remove, relax, and re-insert
          queue.remove(nextRecord);
          metric.relax(current, nextRecord);
          queue.add(nextRecord);
        }
      }
      if (queue.size() > maxQueue) {
        maxQueue = queue.size();
      }
    }

    // TODO remove
    // log.info(touched + " " + maxQueue + " " + FloatDistance.instanceCount + " "
    // + CoordinateID.instanceCount);
  }

  /**
   * Returns a path from start to dest based on the predecessor information in records.
   */
  public static List<Region> getPath(Map<CoordinateID, Region> regions, CoordinateID start,
      CoordinateID dest, Map<CoordinateID, ? extends RegionInfo<?>> records) {
    LinkedList<Region> path = new LinkedList<Region>();
    CoordinateID currentID = records.get(dest).getID();
    while (currentID != null && !currentID.equals(start)) {
      path.addFirst(regions.get(currentID));
      currentID = records.get(currentID).getPredecessor();
      if (currentID == null)
        return null;
    }
    path.addFirst(regions.get(currentID));

    return path;
  }

  /**
   * Returns a path from start to dest based on the predecessor information in records. Accounts for
   * skipped regions.
   * 
   * @param outerMetric The metric for predecessor information
   * @param radius This method applies a RoadMetric with this minimal...
   * @param streetRadius ...and this maximal speed
   * @return A list of regions from start to dest (both inclusively)
   */
  public static List<Region> getPath2(Map<CoordinateID, Region> regions, CoordinateID start,
      CoordinateID dest, final Map<ID, RegionType> excludedRegionTypes, Metric outerMetric,
      int radius, int streetRadius) {
    LinkedList<Region> path = new LinkedList<Region>();
    CoordinateID currentID = outerMetric.get(dest).getID();
    while (currentID != start && currentID != null) {
      CoordinateID currentStart = outerMetric.get(currentID).getPredecessor();
      if (currentStart == null)
        return null;

      RoadMetric innerMetric;
      Regions.getDistances(regions, currentStart, currentID, Integer.MAX_VALUE, innerMetric =
          new RoadMetric(regions, excludedRegionTypes, currentStart, currentID, radius,
              streetRadius));
      List<Region> interPath =
          Regions.getPath(regions, currentStart, currentID, innerMetric.getDistances());
      interPath.remove(0);

      path.addAll(0, interPath);
      currentID = currentStart;
    }
    path.addFirst(regions.get(currentID));
    return path;
  }

  /**
   * A distance value that uses a tuple of distance components. <code>dist</code> is the primary
   * distance value. <code>plus</code> is a bonus value for regions with the same distance but some
   * additional bonus. <code>realDist</code> is a tertiary value, usually the number of regions on
   * the shortest path. <code>pot</code> is an additional potential that can be used to speed up the
   * search ("goal-directed search").
   * 
   * @author stm
   */
  public static class MultidimensionalDistance implements Comparable<MultidimensionalDistance> {
    protected int dist;
    protected int plus;
    protected int realDist;
    protected int pot;

    public MultidimensionalDistance(int d, int p, int r, int pot) {
      dist = d;
      plus = p;
      realDist = r;
      this.pot = pot;
    }

    public int getDistance() {
      return dist;
    }

    public int compareTo(MultidimensionalDistance o) {
      return compareTo(o.dist, o.plus, o.realDist, o.pot);
    }

    /**
     * Compares the distance (plus potential) first, the number of "non-plussed" regions on the path
     * second, and the real distance third.
     */
    public int compareTo(int newdist, int newplus, int newrealDist, int newpot) {
      if (dist + pot > newdist + newpot)
        return 1;
      if (dist + pot < newdist + newpot)
        return -1;
      if (realDist - plus > newrealDist - newplus)
        return 1;
      if (realDist - plus < newrealDist - newplus)
        return -1;
      return realDist - newrealDist;
    }

    /**
     * Adds a distance to this distance. Adds all components and takes care of infinite distances.
     */
    public void add(MultidimensionalDistance add) {
      if (add.dist == Integer.MAX_VALUE || dist == Integer.MAX_VALUE) {
        dist = Integer.MAX_VALUE;
      } else {
        dist += add.dist;
      }
      plus += add.plus;
      realDist += add.realDist;
      pot += add.pot;
    }

    /**
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
      return String.valueOf(dist) + "," + String.valueOf(plus) + "," + String.valueOf(realDist);
    }

    /**
     * Sets this distance to infinity.
     */
    public void setInfinity() {
      dist = Integer.MAX_VALUE;
    }

    /**
     * Sets this distance to zero
     */
    public void setZero() {
      dist = 0;
      plus = 0;
      realDist = 0;
      pot = 0;
    }

    void set(int dist2, int plus2, int realDist2, int pot2) {
      dist = dist2;
      plus = plus2;
      realDist = realDist2;
      pot = pot2;
    }

    /**
     * Returns <code>true</code> if this distance is infinite.
     */
    public boolean isInfinity() {
      return dist == Integer.MAX_VALUE;
    }

  }

  /**
   * A distance implementation that uses {@link MultidimensionalDistance}. <code>dist</code> is the
   * distance in regions, that has been modified if harbours have been encountered on the path.
   * <code>plus</code> is the number of oceans near the coast or land regions on the path.
   * <code>realDist</code> is the actual length of the path.
   * 
   * @author stm
   */
  public static class MultiDimensionalInfo implements RegionInfo<MultiDimensionalInfo> {
    public static long instanceCount = 0;

    MultidimensionalDistance dist;

    /** This record's information is for this coordinate */
    protected Region region;
    /** A node that is on the shortest path from start to id */
    protected CoordinateID pre;

    /** <code>true</code> if this id's neighbors have been touched in the search */
    protected boolean visited;

    protected MultiDimensionalInfo(Region region, int d, int p, int r) {
      this(region, d, p, r, 0);
    }

    protected MultiDimensionalInfo(Region region, int d, int p, int r, int pot) {
      dist = new MultidimensionalDistance(d, p, r, pot);
      this.region = region;
      MultiDimensionalInfo.instanceCount++;
    }

    protected MultiDimensionalInfo(Region region) {
      this(region, 0, 0, 0, 0);
    }

    /**
     * @see magellan.library.utils.Regions.RegionInfo#getDistance()
     */
    public int getDistance() {
      return dist.getDistance();
    }

    /**
     * @see java.lang.Comparable#compareTo(java.lang.Object)
     */
    public int compareTo(MultiDimensionalInfo o) {
      return dist.compareTo(o.dist);
    }

    /**
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
      return region.toString() + ":" + dist.toString();
    }

    /**
     * @see magellan.library.utils.Regions.RegionInfo#distString()
     */
    public String distString() {
      return dist.toString();
    }

    @Override
    public boolean equals(Object o) {
      if (o instanceof MultiDimensionalInfo) {
        MultiDimensionalInfo other = (MultiDimensionalInfo) o;
        return region == other.region && dist.equals(other.dist);
      }
      return false;
    }

    /**
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
      int result = 76;
      return result * 31 + dist.hashCode();
    }

    /**
     * @see magellan.library.utils.Regions.RegionInfo#setInfinity()
     */
    public void setInfinity() {
      dist.setInfinity();
    }

    /**
     * Sets the distance to zero.
     */
    public void setZero() {
      dist.setZero();
    }

    /**
     * @see magellan.library.utils.Regions.RegionInfo#isInfinity()
     */
    public boolean isInfinity() {
      return dist.isInfinity();
    }

    /**
     * @see magellan.library.utils.Regions.RegionInfo#getID()
     */
    public CoordinateID getID() {
      return region.getID();
    }

    /**
     * @see magellan.library.utils.Regions.RegionInfo#getRegion()
     */
    public Region getRegion() {
      return region;
    }

    /**
     * @see magellan.library.utils.Regions.RegionInfo#getPredecessor()
     */
    public CoordinateID getPredecessor() {
      return pre;
    }

    /**
     * @see magellan.library.utils.Regions.RegionInfo#setPredecessor(magellan.library.CoordinateID)
     */
    public void setPredecessor(CoordinateID pred) {
      pre = pred;
    }

    /**
     * @see magellan.library.utils.Regions.RegionInfo#isVisited()
     */
    public boolean isVisited() {
      return visited;
    }

    /**
     * @see magellan.library.utils.Regions.RegionInfo#setVisited()
     */
    public void setVisited() {
      visited = true;
    }

    /**
     * Returns a new info with zero distance for the region.
     */
    public static MultiDimensionalInfo createZero(Region region) {
      return new MultiDimensionalInfo(region);
    }

    /**
     * Returns a new info with infinite distance for the region.
     */
    public static MultiDimensionalInfo createInfinity(Region region) {
      return new MultiDimensionalInfo(region, Integer.MAX_VALUE, 0, Integer.MAX_VALUE, 0);
    }

  }

  /**
   * Returns a shortest path for ships from start to dest.
   * 
   * @param regions DOCUMENT ME
   * @param start DOCUMENT ME
   * @param dest DOCUMENT ME
   * @param excludedRegionTypes DOCUMENT ME
   * @param harbourTypes DOCUMENT ME
   * @param speed DOCUMENT ME
   * @return DOCUMENT ME
   */
  public static List<Region> getShipPath(final Map<CoordinateID, Region> regions,
      final CoordinateID start, final CoordinateID dest,
      final Map<ID, RegionType> excludedRegionTypes, final Set<BuildingType> harbourTypes,
      final int speed) {

    Metric metric;
    Regions.getDistances(regions, start, dest, Integer.MAX_VALUE, metric =
        new ShipMetric(regions, start, dest, excludedRegionTypes, harbourTypes, speed));
    return Regions.getPath(regions, start, dest, metric.getDistances());
  }

  /**
   * A superclass for metrics that use {@link MultiDimensionalInfo}.
   * 
   * @author stm
   */
  public static abstract class MultidimensionalMetric implements Metric {
    protected Map<CoordinateID, Region> regions;
    protected Map<ID, RegionType> excludedRegionTypes;
    protected HashMap<CoordinateID, MultiDimensionalInfo> records;
    protected CoordinateID start;
    protected CoordinateID dest;

    /**
     * @dest may be null
     */
    public MultidimensionalMetric(Map<CoordinateID, Region> regions,
        Map<ID, RegionType> excludedRegionTypes, CoordinateID start, CoordinateID dest) {
      this.regions = regions;
      this.excludedRegionTypes = excludedRegionTypes;
      this.start = start;
      this.dest = dest;

      records = new HashMap<CoordinateID, MultiDimensionalInfo>();
      if (dest != null) {
        createInfinity(dest);
      }
      createZero(start);
    }

    /**
     * @see magellan.library.utils.Regions.Metric#get(magellan.library.CoordinateID)
     */
    public MultiDimensionalInfo get(CoordinateID id) {
      MultiDimensionalInfo result = records.get(id);
      if (result == null) {
        result = createInfinity(id);
        records.put(id, result);
      }
      return result;
    }

    /**
     * @see magellan.library.utils.Regions.Metric#getDistances()
     */
    public Map<CoordinateID, ? extends RegionInfo<?>> getDistances() {
      return Collections.unmodifiableMap(records);
    }

    /**
     * @see magellan.library.utils.Regions.Metric#createZero(magellan.library.CoordinateID)
     */
    public MultiDimensionalInfo createZero(CoordinateID id) {
      MultiDimensionalInfo record = MultiDimensionalInfo.createZero(regions.get(id));
      records.put(id, record);
      return record;
    }

    /**
     * @see magellan.library.utils.Regions.Metric#createInfinity(magellan.library.CoordinateID)
     */
    public MultiDimensionalInfo createInfinity(CoordinateID id) {
      MultiDimensionalInfo record = MultiDimensionalInfo.createInfinity(regions.get(id));
      records.put(id, record);
      return record;
    }

    /**
     * Should be called by {@link #relax(RegionInfo, RegionInfo)} to exclude some special cases.
     * 
     * @return <code>true</code> if a special case occurred
     */
    public boolean checkDistArguments(Region r1, Region r2, RegionInfo<?> d1, RegionInfo<?> d2) {
      if (r1 == null || r2 == null) {
        Regions.log.error("found invalid region");
        return true;
      }
      if (d1.isInfinity())
        return true;
      if (excludedRegionTypes.containsKey(r1.getType().getID())
          || excludedRegionTypes.containsKey(r2.getType().getID()))
        return true;
      else if (r1 == r2) {
        Regions.log.warn("invalid path");
        return true;
      }

      return false;
    }

    /**
     * Checks the argument and decreases next's distance if the new distance created by
     * {@link #getNewValue(Region, Region, MultiDimensionalInfo, MultiDimensionalInfo, int[])} is
     * smaller than the old one. Subclasses should usually not overwrite this method, but
     * {@link #getNewValue(Region, Region, MultiDimensionalInfo, MultiDimensionalInfo, int[])}.
     * 
     * @see magellan.library.utils.Regions.Metric#relax(magellan.library.utils.Regions.RegionInfo,
     *      magellan.library.utils.Regions.RegionInfo)
     */
    public boolean relax(RegionInfo<?> current, RegionInfo<?> next) {
      Region r1 = current.getRegion();
      Region r2 = next.getRegion();
      if (checkDistArguments(r1, r2, current, next))
        return false;

      MultiDimensionalInfo current2 = (MultiDimensionalInfo) current;
      MultiDimensionalInfo next2 = (MultiDimensionalInfo) next;

      int[] newValues = new int[4];

      newValues[3] = getPotential(r1, r2);

      if (!getNewValue(r1, r2, current2, next2, newValues))
        return false;

      // if d(next)=\infty OR d(current)+d(current,next) < d(next)
      // decrease key
      if (next2.dist.compareTo(newValues[0], newValues[1], newValues[2], newValues[3]) > 0) {
        next2.dist.set(newValues[0], newValues[1], newValues[2], newValues[3]);
        next2.pre = current.getID();
        return true;
      }
      return false;

    }

    /**
     * Returns a potential based on the distance from the destination that should heuristically
     * speed-up search.
     */
    protected int getPotential(Region r1, Region r2) {
      if (dest == null)
        return 0;
      // add potential for goal-directed search:
      int xdiff1 = Math.abs(r1.getID().x - dest.x);
      int ydiff1 = Math.abs(r1.getID().y - dest.y);
      int xdiff2 = Math.abs(r2.getID().x - dest.x);
      int ydiff2 = Math.abs(r2.getID().y - dest.y);
      return Math.max(xdiff2, ydiff2) - Math.max(xdiff1, ydiff1);
    }

    /**
     * Subclasses should implement this method and return their new distance value encoded in
     * newValues. <code>newValue[3]</code> is pre-initialized with the potential.
     * 
     * @param newValues An array with four elements that should hold the result like {dist, plus,
     *          realDist, pot}.
     * @return <code>false</code> if the distance of next should not be decreased
     */
    protected abstract boolean getNewValue(Region r1, Region r2, MultiDimensionalInfo current,
        MultiDimensionalInfo next, int[] newValues);
  }

  /**
   * A metric for ship paths
   */
  public static class ShipMetric extends MultidimensionalMetric {
    private Set<BuildingType> harbourType;
    private int speed;

    public ShipMetric(Map<CoordinateID, Region> regions, CoordinateID start, CoordinateID dest,
        Map<ID, RegionType> excludedRegionTypes, Set<BuildingType> harbourTypes, int speed) {
      super(regions, excludedRegionTypes, start, dest);
      harbourType = harbourTypes;
      this.speed = speed;
    }

    /**
     * Returns the distance between r1 an r2 plus <code>current</code>'s distance, usually as
     * component-wise sum. If, however, r2 is a land region, the result is rounded up to the next
     * multiple of {@link #speed}. Oceans with coast increase the plus value.
     */
    @Override
    protected boolean getNewValue(Region r1, Region r2, MultiDimensionalInfo current,
        MultiDimensionalInfo next, int[] newValues) {
      int newDist;
      int newPlus;
      int newRealDist;

      if (r1.getRegionType().isOcean()) {
        if (r2.getRegionType().isOcean()) {
          if (r2.getOceanWithCoast() == 1) {
            newDist = 1;
            newPlus = 1;
            newRealDist = 1;
          } else {
            newDist = 1;
            newPlus = 0;
            newRealDist = 1;
          }
        } else {
          if (Regions.containsHarbour(r2, harbourType) || r2.getID().equals(dest)) {
            // movement ends in land regions, so round up to next multiple of speed
            newDist = speed - current.dist.dist % speed;
            newPlus = 1;
            newRealDist = 1;
          } else
            return false;
        }
      } else {
        if (!r2.getRegionType().isOcean()) {
          // this should not happen
          Regions.log.warn("ship route to neighboring land region");
          return false;
        }
        newDist = 1;
        newPlus = 1;
        newRealDist = 1;
      }

      newDist += current.dist.dist;
      newPlus += current.dist.plus;
      newRealDist += current.dist.realDist;
      // potential is not additive

      newValues[0] = newDist;
      newValues[1] = newPlus;
      newValues[2] = newRealDist;
      return true;
    }

    /**
     * Returns the direct neighbors of id.
     * 
     * @see magellan.library.utils.Regions.Metric#getNeighbours(magellan.library.CoordinateID)
     */
    public Map<CoordinateID, Region> getNeighbours(CoordinateID id) {
      Map<CoordinateID, Region> neighbors = Regions.getAllNeighbours(regions, id, null);
      for (Iterator<CoordinateID> it = neighbors.keySet().iterator(); it.hasNext();) {
        Region current = neighbors.get(it.next());
        if (!current.getRegionType().isOcean() && !current.getType().equals(harbourType)) {
          if (!current.getID().equals(dest)) {
            it.remove();
          }
        }
      }
      return neighbors;
    }
  }

  /**
   * Returns a shortest path over land from start to dest, excluding certain region types and
   * considering streets.
   * 
   * @param regions
   * @param start
   * @param dest
   * @param excludedRegionTypes
   * @param radius
   * @param streetRadius
   * @return
   */
  public static List<Region> getLandPath(GameData data, final CoordinateID start,
      final CoordinateID dest, final Map<ID, RegionType> excludedRegionTypes, final int radius,
      final int streetRadius) {

    Metric metric;
    Regions.getDistances(data.regions(), start, dest, Integer.MAX_VALUE, metric =
        new LandMetric(data.regions(), excludedRegionTypes, start, dest, radius, streetRadius));
    return Regions.getPath2(data.regions(), start, dest, excludedRegionTypes, metric, radius,
        streetRadius);
  }

  /**
   * A metric for land regions. The secret lies in the neighbor function, which returns regions that
   * can be reached in one week.
   */
  public static class LandMetric extends MultidimensionalMetric {
    private int radius;
    private int streetRadius;

    public LandMetric(Map<CoordinateID, Region> regions, Map<ID, RegionType> excludedRegionTypes,
        CoordinateID start, CoordinateID dest, int radius, int streetRadius) {
      super(regions, excludedRegionTypes, start, dest);
      this.radius = radius;
      this.streetRadius = streetRadius;
    }

    /**
     * Adds the movement costs considering roads to current's distance.
     * 
     * @see magellan.library.utils.Regions.MultidimensionalMetric#getNewValue(magellan.library.Region,
     *      magellan.library.Region, magellan.library.utils.Regions.MultiDimensionalInfo,
     *      magellan.library.utils.Regions.MultiDimensionalInfo, int[])
     */
    @Override
    protected boolean getNewValue(Region r1, Region r2, MultiDimensionalInfo current,
        MultiDimensionalInfo next, int[] newValues) {
      RoadMetric metric;
      Regions.getDistances(regions, current.getID(), next.getID(), Integer.MAX_VALUE, metric =
          new RoadMetric(regions, excludedRegionTypes, start, dest, radius, streetRadius));

      int rawDist = current.dist.dist + (metric.get(next.getID())).dist.dist;
      // round up if the maximum radius has been used.
      int newDist = rawDist + rawDist % (radius * streetRadius);
      int newRealDist = current.dist.realDist + 1;

      newValues[0] = newDist;
      newValues[1] = 0;
      newValues[2] = newRealDist;
      return true;
    }

    /**
     * Returns all regions that can be reached in one week from id
     * 
     * @see magellan.library.utils.Regions.Metric#getNeighbours(magellan.library.CoordinateID)
     */
    public Map<CoordinateID, Region> getNeighbours(CoordinateID id) {
      RoadMetric metric;
      Regions.getDistances(regions, id, null, streetRadius, metric =
          new RoadMetric(regions, excludedRegionTypes, start, null, radius, streetRadius));
      Map<CoordinateID, Region> neighbors = new HashMap<CoordinateID, Region>();
      for (RegionInfo<?> record : metric.getDistances().values()) {
        if (record.getDistance() <= streetRadius) {
          neighbors.put(record.getID(), regions.get(record.getID()));
        }
      }

      return neighbors;
    }
  }

  /**
   * A metric that accounts for movement cost using roads.
   */
  public static class RoadMetric extends MultidimensionalMetric {

    private int radius;
    private int streetRadius;

    public RoadMetric(Map<CoordinateID, Region> regions, Map<ID, RegionType> excludedRegionTypes,
        CoordinateID start, CoordinateID dest, int radius, int streetRadius) {
      super(regions, excludedRegionTypes, start, dest);
      this.radius = radius;
      this.streetRadius = streetRadius;
    }

    @Override
    protected boolean getNewValue(Region r1, Region r2, MultiDimensionalInfo current2,
        MultiDimensionalInfo next2, int[] newValues) {
      int newDist;
      int newRealDist;

      if (Regions.isCompleteRoadConnection(r1, r2)) {
        // in order to use ints, not floats, we do not use 1/radius, but
        // streetradius*radius/streetradius
        newDist = radius;
      } else {
        // in order to use ints, not floats, we do not use 1/radius, but streetradius*radius/radius
        newDist = streetRadius;
      }

      newDist += current2.dist.dist;
      newRealDist = 1 + current2.dist.realDist;

      newValues[0] = newDist;
      newValues[1] = 0;
      newValues[2] = newRealDist;
      newValues[3] = 0;
      return true;
    }

    public Map<CoordinateID, Region> getNeighbours(CoordinateID id) {
      return Regions.getAllNeighbours(regions, id, excludedRegionTypes);
    }
  }

  public static class UnitMetric extends MultidimensionalMetric {

    public UnitMetric(Map<CoordinateID, Region> regions, Map<ID, RegionType> excludedRegionTypes,
        CoordinateID start, CoordinateID dest) {
      super(regions, excludedRegionTypes, start, dest);
    }

    /**
     * @see magellan.library.utils.Regions.MultidimensionalMetric#getNewValue(magellan.library.Region,
     *      magellan.library.Region, magellan.library.utils.Regions.MultiDimensionalInfo,
     *      magellan.library.utils.Regions.MultiDimensionalInfo, int[])
     */
    @Override
    protected boolean getNewValue(Region r1, Region r2, MultiDimensionalInfo current2,
        MultiDimensionalInfo next2, int[] newValues) {
      newValues[0] = 1;
      newValues[1] = 0;
      newValues[2] = 1;
      newValues[3] = 0;
      return true;
    }

    /**
     * @see magellan.library.utils.Regions.Metric#getNeighbours(magellan.library.CoordinateID)
     */
    public Map<CoordinateID, Region> getNeighbours(CoordinateID id) {
      return Regions.getAllNeighbours(regions, id, excludedRegionTypes);
    }
  }

  /**
   * Find a way from one region to another region.
   * 
   * @return a Collection of regions that have to be trespassed in order to get from the one to the
   *         other specified region, including both of them.
   */
  public static List<Region> getPath1(Map<CoordinateID, Region> regions, CoordinateID start,
      CoordinateID dest, Map<ID, RegionType> excludedRegionTypes) {
    if ((regions == null) || (start == null) || (dest == null)) {
      Regions.log.warn("Regions.getPath(): invalid argument");

      return Collections.emptyList();
    }

    Map<CoordinateID, Double> distances = new Hashtable<CoordinateID, Double>();
    // distances.put(start, new Float(0.0f)); // contains the distances from the start region to all
    // other regions as Float objects
    distances.put(start, new Double(0)); // contains the distances from the start region to all
    // other regions as Float objects

    LinkedList<Region> path = new LinkedList<Region>();
    LinkedList<Region> backlogList = new LinkedList<Region>(); // contains regions with unknown
    // distance to the start region
    Map<CoordinateID, Region> backlogMap = new HashMap<CoordinateID, Region>(); // contains the same
    // entries as the
    // backlog list.
    // It's contents are
    // unordered but
    // allow a fast
    // look-up by
    // coordinate
    Region curRegion = null;
    CoordinateID curCoord = null;
    int consecutiveReenlistings = 0; // safe-guard against endless loops

    if (excludedRegionTypes == null) {
      excludedRegionTypes = new Hashtable<ID, RegionType>();
    }

    /*
     * initialize the backlog list and map with the neighbours of the start region
     */
    Map<CoordinateID, Region> initNeighbours =
        Regions.getAllNeighbours(regions, start, excludedRegionTypes);
    initNeighbours.remove(start);
    backlogList.addAll(initNeighbours.values());
    backlogMap.putAll(initNeighbours);

    /*
     * first, determine the distance from the start region to all other regions
     */
    while (!backlogList.isEmpty()) {
      /*
       * in this loop the backlog list contains all regions with unkown distance to start
       */

      /* take the first region from the backlog list */
      curRegion = backlogList.getFirst();
      curCoord = curRegion.getCoordinate();

      /* safety checks */
      if (excludedRegionTypes.containsKey(curRegion.getType().getID())) {
        Regions.log.warn("Regions.getPath(): Found an region of type "
            + curRegion.getType().getName() + " in region list! Removing and ignoring it.");
        backlogList.removeFirst();
        backlogMap.remove(curCoord);

        continue;
      }

      if (distances.containsKey(curCoord)) {
        Regions.log
            .warn("Regions.getPath(): Found a region with known distance in region list! Removing and ignoring it.");
        backlogList.removeFirst();
        backlogMap.remove(curCoord);

        continue;
      }

      /*
       * determine all neighbours of the current region taken from the backlog list
       */
      // float minDistance = Float.MAX_VALUE;
      double minDistance = Double.MAX_VALUE;
      Map<CoordinateID, Region> neighbours =
          Regions.getAllNeighbours(regions, curCoord, excludedRegionTypes);
      neighbours.remove(curCoord);

      /*
       * now determine the distance from the start region to the current region taken from the
       * backlog list by checking its neighbour's distances to the start region
       */
      for (Region curNb : neighbours.values()) {
        CoordinateID curNbCoord = curNb.getCoordinate();
        // Float dist = (Float) distances.get(curNbCoord);
        Double dist = distances.get(curNbCoord);
        if (dist != null) {
          /*
           * we know the distance from the start region to this neighbour, so we can determine the
           * distance from the start region to the current region taken from the backlog list
           */
          // float curDistance = getDistance(curNb, curRegion) + dist.floatValue();
          // double curDistance = Regions.getDistance(curNb, curRegion,true) + dist.floatValue();
          double curDistance = Regions.getDistance(curNb, curRegion, true) + dist.doubleValue();

          if (curDistance < minDistance) {
            minDistance = curDistance;
          }
        } else {
          /*
           * we do not know the distance from the start region to this neighbour, so we store this
           * neighbour in the backlog list
           */
          if (!backlogMap.containsKey(curNbCoord)) {
            backlogList.add(curNb);
            backlogMap.put(curNbCoord, null);
          }
        }
      }

      /*
       * If we could determine the distance from the start region to the current region taken from
       * the backlog list, we can remove it from that list and record the distance
       */
      if (minDistance < Double.MAX_VALUE) {
        consecutiveReenlistings = 0;
        backlogList.removeFirst();
        backlogMap.remove(curCoord);
        distances.put(curCoord, new Double(minDistance));
      } else {
        backlogList.removeFirst();

        if (!distances.containsKey(curCoord)) {
          backlogList.addLast(curRegion);
          consecutiveReenlistings++;

          if (consecutiveReenlistings > backlogList.size()) {
            Regions.log.warn("Regions.getPath(): looks like an endless loop. Exiting.");

            break;
          }
        } else {
          Regions.log
              .warn("Regions.getPath(): Found a region with known distance in backlog list: "
                  + curRegion);
        }
      }
    }

    // backtracking
    /*
     * now we know the distance of each region to the start region but we do not know a shortest
     * path. We can find one simply by starting at the destination region, looking at its neighbours
     * and choosing the one with the smallest distance to the start region until we reach the start
     * region. This sequence of regions is the reverse shortest path.
     */
    curRegion = regions.get(dest);
    curCoord = dest;
    path.add(curRegion);

    while ((curRegion != null) && (curCoord != null) && !curCoord.equals(start)) {
      Double dist = distances.get(curCoord);
      if (dist != null) {
        // double minDistance = dist.doubleValue();
        // now add the last mile at minimum dist
        double minDistance = dist.doubleValue() + 1;

        CoordinateID closestNbCoord = null;
        Map<CoordinateID, Region> neighbours =
            Regions.getAllNeighbours(regions, curCoord, excludedRegionTypes);
        neighbours.remove(curCoord);

        for (Region curNb : neighbours.values()) {
          CoordinateID curNbCoord = curNb.getCoordinate();
          Double nbDist = distances.get(curNbCoord);

          if (nbDist != null) {
            double curDistance = nbDist.doubleValue();

            // add the last mile, Fiete 20070531
            curDistance += Regions.getDistance(curRegion, curNb, true);

            if (curDistance < minDistance) {
              minDistance = curDistance;
              closestNbCoord = curNbCoord;
            }
          } else {
            Regions.log.warn("Regions.getPath(): Found neighbouring region without distance: "
                + curNb + " neighbouring " + curRegion);
          }
        }

        if (closestNbCoord != null) {
          curCoord = closestNbCoord;
          curRegion = regions.get(curCoord);
          path.addFirst(curRegion);
        } else {
          Regions.log
              .warn("Regions.getPath(): Discovered region without any distanced neighbours while backtracking");
          path.clear();

          break;
        }
      } else {
        Regions.log
            .warn("Regions.getPath(): Discovered region without distance while backtracking: "
                + curRegion);
        path.clear();

        break;
      }
    }

    return path;
  }

  public static boolean containsHarbour(Region r, Collection<BuildingType> harbours) {
    for (BuildingType harbourType : harbours) {
      if (Regions.containsBuilding(r, harbourType))
        return true;
    }
    return false;
  }

  /**
   * Returns <code>true</code> if r contains a build with the specified type
   */
  public static boolean containsBuilding(Region r, BuildingType type) {
    boolean harbourFound = false;

    if (type != null) {
      for (Building b : r.buildings()) {
        if (b.getType().equals(type) && (b.getSize() == type.getMaxSize())) {
          harbourFound = true;

          break;
        }
      }
    }

    return harbourFound;
  }

  public static List<Region> planShipRoute(Ship ship, GameData data, CoordinateID destination) {
    return Regions.planShipRoute(data, ship.getRegion().getCoordinate(), ship.getShoreId(),
        destination, data.getGameSpecificStuff().getGameSpecificRules().getShipRange(ship));
  }

  /**
   * @param ship
   * @param destination
   * @param allregions
   * @param harbour
   * @param speed The speed of the ship
   * @return
   */
  public static List<Region> planShipRoute(GameData data, CoordinateID start, int shoreId,
      CoordinateID destination, int speed) {
    BuildingType harbour = data.rules.getBuildingType(EresseaConstants.B_HARBOUR);

    if (destination == null || data.regions().get(destination) == null || start == null
        || data.regions().get(start) == null)
      // no path
      return null;
    Region destRegion = data.regions().get(destination);
    Region startRegion = data.regions().get(start);

    Map<CoordinateID, Region> harbourRegions = new Hashtable<CoordinateID, Region>();
    harbourRegions.put(startRegion.getID(), startRegion);
    harbourRegions.put(destination, destRegion);

    // Fetch all ocean-regions and all regions, that contain a harbor.
    // These are the valid one in which a path shall be searched.
    // if(oceanType != null) {
    for (Region r : data.regions().values()) {
      if ((r.getRegionType() != null) && r.getRegionType().isOcean()
          || Regions.containsBuilding(r, harbour)) {
        harbourRegions.put(r.getCoordinate(), r);
      }
    }

    if (shoreId != Direction.DIR_INVALID && !Regions.containsBuilding(startRegion, harbour)) {
      // Ship cannot leave in all directions
      // try to find a path from every allowed shore-off region to the destination
      List<Region> bestPath = null;
      for (int legalShore = (shoreId + 5) % 6; legalShore != (shoreId + 2) % 6; legalShore =
          (legalShore + 1) % 6) {
        CoordinateID newStart =
            new CoordinateID(start).translate(Direction.toCoordinate(legalShore));
        if (!harbourRegions.containsKey(newStart)
            || !harbourRegions.get(newStart).getRegionType().isOcean()) {
          continue;
        }

        List<Region> newPath =
            Regions.getShipPath(harbourRegions, newStart, destination, Collections
                .<ID, RegionType> emptyMap(), Collections.singleton(harbour), speed);
        if (bestPath == null || bestPath.size() > newPath.size()) {
          bestPath = newPath;
        }
      }
      LinkedList<Region> result = new LinkedList<Region>();
      result.add(startRegion);
      result.addAll(bestPath);
      return result;
    }

    return Regions.getShipPath(harbourRegions, start, destination, Collections
        .<ID, RegionType> emptyMap(), Collections.singleton(harbour), speed);
  }

  private static double getDistance(Region r1, Region r2) {
    return 1;
  }

  /**
   * delivers a distance between 2 regions we asume, that both regions are neighbours, so trivial
   * distance is 1 for oceans, we deliver for landnearregions a significant smaller value for
   * landregions we calculate a new distance...as like moveoints with propper roads: 2, without: 3
   * 
   * @param r1
   * @param r2
   * @param useExtendedVersion
   * @return
   */
  private static double getDistance(Region r1, Region r2, boolean useExtendedVersion) {
    double erg = 1;
    if (!useExtendedVersion)
      return Regions.getDistance(r1, r2);
    // Fiete 20061123
    // for Ships...prefer Regions near coasts
    // for land units...prfer Regions with roads
    // Trick: if suitable situation, reduce the distance minimal
    double suitErg = 0.99;

    // if we have 2 Ozean regions...
    if (r1.getRegionType().isOcean() && r2.getRegionType().isOcean()) {
      if (r2.getOceanWithCoast() == 1)
        return suitErg;
      else
        return erg;
    }

    // if we have 2 non ozean regions....
    if (!r1.getRegionType().isOcean() && !r2.getRegionType().isOcean()) {
      if (Regions.isCompleteRoadConnection(r1, r2))
        // return 2;
        return (1 + suitErg);
      else
        return 3;
    }

    return erg;
  }

  /**
   * Retrieve the coordinates the unit passes from the messages of the regions.
   * 
   * @param data the unit
   * @return a List of Coordinate objects of the path the unit used (although evaluated via
   *         backtracking) from start to end.
   */
  public static List<CoordinateID> getMovement(GameData data, Unit u) {
    List<CoordinateID> coordinates = new ArrayList<CoordinateID>(2);

    // first of all add current coordinate
    coordinates.add(u.getRegion().getID());

    // we need a string which is useable for travelThru AND travelThruShip
    String ID = (u.getShip() == null) ? u.toString() : u.getShip().toString(false);

    // run over neighbours recursively
    CoordinateID c = Regions.getMovement(data, ID, u.getRegion().getCoordinate(), coordinates);

    while ((c != null) && !coordinates.contains(c)) {
      coordinates.add(c);
      c = Regions.getMovement(data, ID, c, coordinates);
    }

    Collections.reverse(coordinates);

    return coordinates;
  }

  private static CoordinateID getMovement(GameData data, String id, CoordinateID c,
      List<CoordinateID> travelledRegions) {
    Map<CoordinateID, Region> neighbours =
        Regions.getAllNeighbours(data.regions(), c, new Hashtable<ID, RegionType>());

    for (Region r : neighbours.values()) {
      CoordinateID neighbour = r.getCoordinate();

      if (neighbour.equals(c) || travelledRegions.contains(neighbour)) {
        // dont add our own or an already visited coordinate
        continue;
      }

      if (Regions.messagesContainsString(r.getTravelThru(), id)
          || Regions.messagesContainsString(r.getTravelThruShips(), id))
        return neighbour;
    }

    return null;
  }

  private static boolean messagesContainsString(List<Message> messages, String ID) {
    if (messages == null)
      return false;

    for (Message m : messages) {
      if (m.getText().equals(ID))
        return true;
    }

    return false;
  }

  /**
   * Returns a map of all RegionTypes that are flagged as <tt>ocean</tt>.
   * 
   * @param rules Rules of the game
   * @return map of all ocean RegionTypes
   */
  public static Map<ID, RegionType> getOceanRegionTypes(Rules rules) {
    Map<ID, RegionType> ret = new Hashtable<ID, RegionType>();

    for (Iterator<RegionType> iter = rules.getRegionTypeIterator(); iter.hasNext();) {
      RegionType rt = iter.next();

      if (rt.isOcean()) {
        ret.put(rt.getID(), rt);
      }
    }

    return ret;
  }

  public static Map<ID, RegionType> getLandRegionTypes(Rules rules) {
    Map<ID, RegionType> ret = new Hashtable<ID, RegionType>();

    for (RegionType rt : rules.getRegionTypes()) {
      if (rt.isLand()) {
        ret.put(rt.getID(), rt);
      }
    }

    return ret;
  }

  /**
   * Returns the RegionType that is named as <tt>Feuerwand</tt>.
   * 
   * @param rules Rules of the game
   * @param data GameDate - needed to find Translation
   * @return RegionType Feuerwand
   */
  public static RegionType getFeuerwandRegionType(Rules rules, GameData data) {
    String actFeuerwandName = "Feuerwand";
    // TODO:we do not need a translation here (FF)!
    if (data != null) {
      actFeuerwandName = data.getTranslation("Feuerwand");
    }
    return rules.getRegionType(StringID.create(actFeuerwandName));
  }

  /**
   * returns true, if a working road connection is established between r1 and r2 we assume, both
   * regions are neighbours
   * 
   * @param r1 a region
   * @param r2 another region
   */
  public static boolean isCompleteRoadConnection(Region r1, Region r2) {
    boolean erg = false;
    // Collection of Regions
    // r1 -> r2
    List<Region> regions = new ArrayList<Region>(2);
    regions.add(r1);
    regions.add(r2);
    // generate List of directions
    List<Direction> directions = Regions.getDirectionObjectsOfRegions(regions);
    Direction dir1 = directions.get(0);
    // border of r1 ->
    boolean border1OK = false;
    for (Border b : r1.borders()) {
      if (magellan.library.utils.Umlaut.normalize(b.getType()).equals("STRASSE")
          && (b.getDirection() == dir1.getDir()) && b.getBuildRatio() == 100) {
        border1OK = true;
        break;
      }
    }

    if (!border1OK)
      return false;

    // r2->r1
    regions.clear();
    regions.add(r2);
    regions.add(r1);
    directions = Regions.getDirectionObjectsOfRegions(regions);
    dir1 = directions.get(0);
    // border of r1 ->
    boolean border2OK = false;
    for (Border b : r2.borders()) {
      if (magellan.library.utils.Umlaut.normalize(b.getType()).equals("STRASSE")
          && (b.getDirection() == dir1.getDir()) && b.getBuildRatio() == 100) {
        border2OK = true;
        break;
      }
    }
    if (border1OK && border2OK)
      return true;

    return erg;
  }

  /**
   * Contributed by Hubert Mackenberg. Thanks. x und y Abstand zwischen x1 und x2 berechnen
   **/
  public static int getRegionDist(CoordinateID r1, CoordinateID r2) {
    int dx = r1.x - r2.x;
    int dy = r1.y - r2.y;
    /*
     * Bei negativem dy am Ursprung spiegeln, das veraendert den Abstand nicht
     */
    if (dy < 0) {
      dy = dy * -1;
      dx = dx * -1;
    }
    /*
     * dy ist jetzt >=0, fuer dx sind 3 Faelle zu untescheiden
     */
    if (dx >= 0)
      return dx + dy;
    else if (-dx >= dy)
      return -dx;
    else
      return dy;
  }

  /**
   * Returns an ID for an new Border to be add
   * 
   * @param r
   * @param border
   */
  public static IntegerID getNewBorderID(Region r, Border border) {
    IntegerID erg = border.getID();
    // checks, of border with this ID es already present
    if (r.getBorder(erg) != null) {
      // OK, we have a problem
      // try to find a new one
      boolean IDisFree = false;
      int i = 1;
      while (!IDisFree) {
        IntegerID newID = IntegerID.create(i);
        if (r.getBorder(newID) == null)
          return newID;
        i++;
      }
    }
    return erg;
  }

  /**
   * Checks all regions and recalculates the BitMap for the borders, where is ocean and where not
   * 
   * @param data GameData
   */
  public static void calculateCoastBorders(GameData data) {
    // Bit masks for dir 0 to 5
    int[] bitMaskArray = { 1, 2, 4, 8, 16, 32, 64, 128 };

    // % part of regions with no borders which will
    // be changed as well
    double specialBorderProbability = 0.3;

    Random r = new Random(System.currentTimeMillis());

    long cnt = 0;
    Regions.log.info("starting calculation of coasts");
    for (Region actRegion : data.regions().values()) {
      int coastBitmap = 0;
      if (actRegion.getRegionType().isOcean()) {
        // we have an ocean in front
        // the result
        CoordinateID cID = actRegion.getID();
        Map<CoordinateID, Region> n = Regions.getAllNeighbours(data.regions(), cID, null);
        n.remove(cID);
        // checking all neighbours
        for (CoordinateID checkID : n.keySet()) {
          Region checkR = n.get(checkID);
          if (checkR.getRegionType().isLand()) {
            // not ocean! we should set an 1
            // what is relative coordinate ?
            CoordinateID diffCoord = new CoordinateID(checkID.x - cID.x, checkID.y - cID.y, 0);
            int intDir = Direction.toInt(diffCoord);
            int bitMask = bitMaskArray[intDir];
            coastBitmap = coastBitmap | bitMask;
            cnt++;
          }
        }
        // lets see, if we want ice anyway
        double nextR = r.nextDouble();
        if (nextR < specialBorderProbability) {
          // ok, we want to add a random graphic 0..3
          // putting this info on bit 7 and 8
          int intR = r.nextInt(4);
          // this is poor but I have no better idea..sorry
          switch (intR) {
          case 1:
            coastBitmap = coastBitmap | bitMaskArray[7];
            break;
          case 2:
            coastBitmap = coastBitmap | bitMaskArray[6];
            break;
          case 3:
            coastBitmap = coastBitmap | bitMaskArray[6];
            coastBitmap = coastBitmap | bitMaskArray[7];
            break;
          }
        }
      }
      actRegion.setCoastBitMap(coastBitmap);
    }
    Regions.log.info("finished calculation of coasts, found " + cnt + " coasts.");
  }

}
