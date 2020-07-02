/*
 * Copyright (C) 2000-2004 Roger Butenuth, Andreas Gampe, Stefan Goetz, Sebastian Pappert, Klaas
 * Prause, Enno Rehling, Sebastian Tusk, Ulrich Kuester, Ilja Pavkovic This file is part of the
 * Eressea Java Code Base, see the file LICENSING for the licensing information applying to this
 * file.
 */

package magellan.library.utils;

import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
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
import magellan.library.Sign;
import magellan.library.StringID;
import magellan.library.Unit;
import magellan.library.gamebinding.EresseaConstants;
import magellan.library.gamebinding.MapMetric;
import magellan.library.gamebinding.OrderChanger;
import magellan.library.rules.BuildingType;
import magellan.library.rules.RegionType;
import magellan.library.utils.logging.Logger;

/**
 * A class offering common operations on regions.
 */
public class Regions {

  private static final Logger log = Logger.getInstance(Regions.class);

  private static final double EPSILON = 1e-10;

  /**
   * will be ignored from getAllNeighbours
   */
  public static Collection<Region> excludedRegions;

  /**
   * sets the excluded Regions
   *
   * @param excludedRegions
   */
  public static void setExcludedRegions(Collection<Region> excludedRegions) {
    Regions.excludedRegions = excludedRegions;
  }

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
   *         instances of class Coordinate, values are objects of class Region. This map is created in
   *         this method.
   */
  public static Map<CoordinateID, Region> getAllNeighbours(Map<CoordinateID, Region> regions,
      CoordinateID center, int radius, Map<ID, RegionType> excludedRegionTypes) {

    Metric metric =
        new UnitMetric(regions, excludedRegionTypes != null ? excludedRegionTypes : Collections
            .<ID, RegionType> emptyMap(), center, null);
    Regions.getDistances(regions, center, null, radius + 1, metric);

    Map<CoordinateID, Region> neighbours = new HashMap<CoordinateID, Region>();
    for (Region r : regions.values()) {
      RegionInfo info = metric.getDistances().get(r.getID());
      if (info != null && info.getDistance() <= radius) {
        neighbours.put(r.getID(), r);
      }

    }

    return neighbours;
  }

  /**
   * Retrieve the regions directly connected with the center region (including itself).
   *
   * @param regions a map containing the existing regions.
   * @param center the region the neighbours of which are retrieved.
   * @param excludedRegionTypes region types that disqualify regions as valid neighbours.
   * @return a map with all neighbours that were found, including region center. The keys are
   *         instances of class Coordinate, values are objects of class Region. This map is created in
   *         this method.
   * @see Region#getNeighbors()
   */
  public static Map<CoordinateID, Region> getAllNeighbours(Map<CoordinateID, Region> regions,
      CoordinateID center, Map<ID, RegionType> excludedRegionTypes) {

    Map<Direction, Region> allNeighbors;
    if (regions.get(center) != null) {
      allNeighbors = regions.get(center).getNeighbors();
    } else {
      allNeighbors = getCoordinateNeighbours(regions, center);
    }

    Map<CoordinateID, Region> neighbours =
        new HashMap<CoordinateID, Region>((allNeighbors.size() + 1) * 10 / 7, .8f);

    for (Region n : allNeighbors.values()) {
      if ((excludedRegionTypes == null) || !excludedRegionTypes.containsKey(n.getType().getID())) {
        if (Regions.excludedRegions == null || !Regions.excludedRegions.contains(n)) {
          neighbours.put(n.getID(), n);
        }
      }
    }

    neighbours.put(center, regions.get(center));

    return neighbours;
  }

  /**
   * Calculates the neighbors of a region based on coordinates, i.e., it tests all neighboring
   * coordinates and adds them if the region exists in the data. It follows wrappers if necessary.
   *
   * @return A map with Direction/Region entries of existing neighbors. This map is created in this
   *         method.
   * @see Region#getNeighbors()
   */
  public static Map<Direction, Region> getCoordinateNeighbours(GameData data, CoordinateID center) {
    int radius = 1;

    Map<Direction, Region> neighbours = new HashMap<Direction, Region>(9, .9f);
    MapMetric metric = data.getGameSpecificStuff().getMapMetric();
    for (Direction dir : metric.getDirections()) {
      CoordinateID c = metric.translate(center, dir);

      Region neighbour = data.getRegion(c);
      if (neighbour == null) {
        Region wrapper = data.wrappers().get(c);
        if (wrapper != null) {
          neighbour = data.getOriginal(wrapper);
          if (neighbour.getCoordX() != wrapper.getCoordX()
              && neighbour.getCoordY() != neighbour.getCoordY()) {
            log.error("improbable wrapper " + wrapper + "->" + neighbour);
          }
        }
      }

      if (neighbour != null && !neighbour.getID().equals(center)) {
        // if (neighbour.getVisibility().equals(Visibility.WRAP)) {
        // // find real region
        // for (Region r : regions.values()) {
        // if (r.getUID() == neighbour.getUID() && r.getVisibility() != Visibility.WRAP) {
        // neighbour = r;
        // }
        // }
        // }
        neighbours.put(dir, neighbour);
      }
    }

    return neighbours;
  }

  /**
   * Calculates the neighbors of a region based on coordinates, i.e., it tests all neighboring
   * coordinates and adds them if the region exists in the data.
   *
   * @return A map with Direction/Region entries of existing neighbors. This map is created in this
   *         method.
   * @see Region#getNeighbors()
   */
  public static Map<Direction, Region> getCoordinateNeighbours(Map<CoordinateID, Region> regions,
      CoordinateID center) {
    int radius = 1;

    Map<Direction, Region> neighbours = new HashMap<Direction, Region>(9, .9f);

    for (int dx = -radius; dx <= radius; dx++) {
      for (int dy = (-radius + Math.abs(dx)) - ((dx > 0) ? dx : 0); dy <= ((radius - Math.abs(dx)) - ((dx < 0)
          ? dx : 0)); dy++) {
        CoordinateID c = CoordinateID.create(center.getX() + dx, center.getY() + dy, center.getZ());

        Region neighbour = regions.get(c);

        if (neighbour != null && !neighbour.getID().equals(center)) {
          // if (neighbour.getVisibility().equals(Visibility.WRAP)) {
          // // find real region
          // for (Region r : regions.values()) {
          // if (r.getUID() == neighbour.getUID() && r.getVisibility() != Visibility.WRAP) {
          // neighbour = r;
          // }
          // }
          // }
          neighbours.put(getMapMetric(neighbour).getDirection(center, c), neighbour);
        }
      }
    }

    return neighbours;
  }

  /**
   * Find a way from one region to another region and get the directions in which to move to follow a
   * sequence of regions. This is virtually the same as
   *
   * <pre>
   * getDirections(getPath(regions, start, dest, excludedRegionTypes, radius));
   * </pre>
   *
   * @see #getPath(Map, CoordinateID, CoordinateID, Map)
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
    if (regions == null || regions.size() < 2)
      return null;

    List<Direction> directions = Regions.getDirectionObjectsOfRegions(regions);

    if (directions == null)
      return null;

    StringBuffer dir = new StringBuffer();

    OrderChanger changer =
        regions.iterator().next().getData().getGameSpecificStuff().getOrderChanger();

    for (Direction d : directions) {
      if (dir.length() > 0) {
        dir.append(" ");
      }

      dir.append(changer.getOrderO(Locales.getOrderLocale(), d.getId()));
    }

    return dir.toString();
  }

  /**
   * Converts a list of coordinate into a list of Directions between them. If successive coordinates
   * are not direct neighbors, those directions will be omitted!
   *
   * @see #getDirectionObjectsOfCoordinates(Collection)
   */
  // FIXME convert to List, not Collection
  public static List<Direction> getDirectionObjectsOfRegions(Collection<Region> regions) {
    if (regions == null)
      return null;

    List<Direction> directions = new ArrayList<Direction>(regions.size() - 1);

    Region prev = null;
    Region cur = null;

    Iterator<Region> iter = regions.iterator();

    if (iter.hasNext()) {
      prev = iter.next();

      while (iter.hasNext()) {
        cur = iter.next();

        Direction dir = getDirection(prev, cur);

        if (dir != Direction.INVALID) {
          directions.add(dir);
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
   * Converts a list of coordinate into a list of Directions between them. If successive coordinates
   * are not direct neighbors, those directions will be omitted! Note also, that this cannot consider
   * wrapping paths!
   */
  public static List<Direction> getDirectionObjectsOfCoordinates(
      Collection<CoordinateID> coordinates) {
    return getDirectionObjectsOfCoordinates(null, coordinates);
  }

  /**
   * Converts a list of coordinate into a list of Directions between them. If successive coordinates
   * are not direct neighbors, those directions will be omitted! If <code>data!=null</code>, this
   * method will assume that the coordinates correspond to real regions (not wrapped ones) and try
   * finding the right directions between them.
   */
  public static List<Direction> getDirectionObjectsOfCoordinates(GameData data,
      Collection<CoordinateID> coordinates) {
    if (coordinates == null)
      return null;

    List<Direction> directions = new ArrayList<Direction>(coordinates.size() - 1);

    CoordinateID prevCoord = null;

    Iterator<CoordinateID> coordIt = coordinates.iterator();

    if (coordIt.hasNext()) {
      prevCoord = coordIt.next();

      while (coordIt.hasNext()) {
        CoordinateID curCoord = coordIt.next();

        Direction dir = getMapMetric(data).getDirection(prevCoord, curCoord);

        // find direction based on neighbors
        if (dir == Direction.INVALID && data.getRegion(prevCoord) != null
            && data.getRegion(curCoord) != null) {
          dir = Regions.getDirection(data.getRegion(prevCoord), data.getRegion(curCoord));
        }

        if (dir != Direction.INVALID) {
          directions.add(dir);
        } // else {
        // Regions.log.warn("Regions.getDirectionsOfCoordinates(): invalid direction encountered");
        //
        // return null;
        // }

        prevCoord = curCoord;
      }
    }

    return directions;
  }

  /**
   * Holds shortest-path relevant information for a region.
   *
   * @author stm
   */
  public interface RegionInfo {

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
    public RegionInfo createZero(CoordinateID node);

    /**
     * Creates an entry with infinite distance for the specified node.
     */
    public RegionInfo createInfinity(CoordinateID node);

    /**
     * Returns the info for the node. Creates an infinite one if none exists.
     */
    public RegionInfo get(CoordinateID node);

    /**
     * Returns all neighbors of the provided coordinate.
     */
    public Map<CoordinateID, Region> getNeighbours(CoordinateID id);

    /**
     * Changes the distance of <code>nextRecord</code> according to the distance of <code>current</code>
     * and the distance between the two regions.
     *
     * @return <code>true</code> if <code>nextRecord</code>'s distance was decreased
     */
    public boolean relax(RegionInfo current, RegionInfo nextRecord);

    /**
     * Returns a map of all known distance values.
     */
    public Map<CoordinateID, ? extends RegionInfo> getDistances();
  }

  /**
   * Returns distance information about all Coordinates in regions that are at most as far as dest or
   * as far as maxDist away from start. The result is stored in the metric information.
   *
   * @param regions Regions will be looked up in this map.
   * @param start The origin of the search.
   * @param dest The coordinate that must be reached, or <code>null</code>
   * @param maxDist The maximum distance to search. The search stops if all regions with at most this
   *          distance have been visited.
   * @param metric A metric for computing distances.
   */
  public static void getDistances(Map<CoordinateID, Region> regions, CoordinateID start,
      CoordinateID dest, int maxDist, Metric metric) {
    boolean DEBUG = false;

    if (!regions.containsKey(start) || (dest != null && !regions.containsKey(dest)))
      return;

    // this method applies Dijkstra's algorithm

    // initialize queue
    PriorityQueue<RegionInfo> queue = new PriorityQueue<RegionInfo>(8);
    queue.add(metric.createZero(start));

    int touched = 0;
    int maxQueue = 1;

    while (!queue.isEmpty() && queue.peek().getDistance() <= maxDist
        && !queue.peek().getID().equals(dest)) {
      RegionInfo current = queue.poll();
      if (DEBUG)
        if (regions.get(current.getID()) != null
            && (metric instanceof LandMetric || metric instanceof ShipMetric)) {
          regions.get(current.getID()).clearSigns();
          regions.get(current.getID()).addSign(new Sign(touched + " " + current.distString()));
        } else {
          touched += 0;
        }
      touched++;
      current.setVisited();
      Map<CoordinateID, Region> neighbors = metric.getNeighbours(current.getID());
      for (CoordinateID next : neighbors.keySet()) {
        RegionInfo nextRecord = metric.get(next);
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
    if (DEBUG) {
      while (!queue.isEmpty()) {
        RegionInfo current = queue.poll();
        if (regions.get(current.getID()) != null
            && (metric instanceof LandMetric || metric instanceof ShipMetric)) {
          regions.get(current.getID()).clearSigns();
          regions.get(current.getID()).addSign(new Sign(touched + "*" + current.distString()));
        } else {
          touched += 0;
        }
        touched++;
      }
    }
  }

  /**
   * Returns a path from start to dest based on the predecessor information in the map.
   */
  public static List<Region> getPath(Map<CoordinateID, Region> regions, CoordinateID start,
      CoordinateID dest, Map<CoordinateID, ? extends RegionInfo> map) {
    LinkedList<Region> path = new LinkedList<Region>();
    CoordinateID currentID = map.get(dest).getID();
    while (currentID != null && !currentID.equals(start)) {
      path.addFirst(regions.get(currentID));
      currentID = map.get(currentID).getPredecessor();
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
   * additional bonus. <code>realDist</code> is a tertiary value, usually the number of regions on the
   * shortest path. <code>pot</code> is an additional potential that can be used to speed up the
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
      return String.valueOf(dist) + "," + String.valueOf(pot) + "," + String.valueOf(plus) + ","
          + String.valueOf(realDist);
    }

    /**
     * Sets this distance to infinity.
     */
    public void setInfinity() {
      dist = Integer.MAX_VALUE;
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
  public static class MultiDimensionalInfo implements RegionInfo, Comparable<MultiDimensionalInfo> {
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
      if (o == this)
        return true;
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
     * Returns a new info with infinite distance for the region.
     */
    public static MultiDimensionalInfo createInfinity(Region region) {
      return new MultiDimensionalInfo(region, Integer.MAX_VALUE, 0, Integer.MAX_VALUE, 0);
    }

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
     * @param dest may be null
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
    public Map<CoordinateID, ? extends RegionInfo> getDistances() {
      return Collections.unmodifiableMap(records);
    }

    /**
     * @see magellan.library.utils.Regions.Metric#createZero(magellan.library.CoordinateID)
     */
    public MultiDimensionalInfo createZero(CoordinateID id) {
      MultiDimensionalInfo record = new MultiDimensionalInfo(regions.get(id));
      record.dist.pot = getPotential(regions.get(start), regions.get(start));
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
     * Should be called by
     * {@link Regions.MultidimensionalMetric#relax(Regions.RegionInfo, Regions.RegionInfo)} to exclude
     * some special cases.
     *
     * @return <code>true</code> if a special case occurred
     */
    public boolean checkDistArguments(Region r1, Region r2, RegionInfo d1, RegionInfo d2) {
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
     * {@link Regions.MultidimensionalMetric#getNewValue(Region, Region, Regions.MultiDimensionalInfo, Regions.MultiDimensionalInfo, int[])}
     * is smaller than the old one. Subclasses should usually not overwrite this method, but
     * {@link Regions.MultidimensionalMetric#getNewValue(Region, Region, Regions.MultiDimensionalInfo, Regions.MultiDimensionalInfo, int[])}
     * .
     *
     * @see magellan.library.utils.Regions.Metric#relax(magellan.library.utils.Regions.RegionInfo,
     *      magellan.library.utils.Regions.RegionInfo)
     */
    public boolean relax(RegionInfo current, RegionInfo next) {
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
        // debugging code
        if (records.get(next2.pre) != null
            && current2.dist.compareTo(newValues[0], newValues[1], newValues[2], newValues[3]) > 0) {
          log.finest(current2 + " -> " + next2 + " (" + newValues[0] + "," + newValues[1] + ","
              + newValues[2] + "," + newValues[3] + ")" + records.get(next2.pre));
        }
        if (current2.dist.compareTo(newValues[0], newValues[1], newValues[2], newValues[3]) > 0) {
          log.finest(current2 + " -> " + next2 + " (" + newValues[0] + "," + newValues[1] + ","
              + newValues[2] + "," + newValues[3] + ")" + records.get(next2.pre));
        }
        // debugging end

        next2.dist.set(newValues[0], newValues[1], newValues[2], newValues[3]);
        next2.pre = current.getID();
        return true;
      }
      return false;

    }

    /**
     * Returns a potential based on the distance from the destination that should heuristically speed-up
     * search.
     */
    protected int getPotential(Region r1, Region r2) {
      if (dest == null)
        return 0;
      if (r1.getData().wrappers().isEmpty())
        // this potential does not work for wrapped coordinates!
        return getDist(r2.getCoordinate(), dest);
      else
        return 0;
    }

    /**
     * Subclasses should implement this method and return their new distance value encoded in newValues.
     * <code>newValue[3]</code> is pre-initialized with the potential.
     *
     * @param newValues An array with four elements that should hold the result like {dist, plus,
     *          realDist, pot}.
     * @return <code>false</code> if the distance of next should not be decreased
     */
    protected abstract boolean getNewValue(Region r1, Region r2, MultiDimensionalInfo current,
        MultiDimensionalInfo next, int[] newValues);
  }

  /**
   * A metric for ship paths. Distances are path lengths, but a path that ends in a land region is
   * rounded up to the next multiple of speed.
   */
  public static class ShipMetric extends MultidimensionalMetric {
    private Set<BuildingType> harbourType;
    private int speed;
    private Direction returnDirection;

    /**
     * @param regions
     * @param start
     * @param dest
     * @param excludedRegionTypes
     * @param harbourTypes
     * @param speed
     * @param returnDirection Ship may only leave the start region in this direction or the two
     *          neighboring direction
     */
    public ShipMetric(Map<CoordinateID, Region> regions, CoordinateID start, CoordinateID dest,
        Map<ID, RegionType> excludedRegionTypes, Set<BuildingType> harbourTypes, int speed,
        Direction returnDirection) {
      super(regions, excludedRegionTypes, start, dest);
      harbourType = harbourTypes;
      this.speed = speed;
      this.returnDirection = returnDirection;
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
        if (!Regions.containsHarbour(r1, harbourType)
            && r1.getCoordinate() == start
            && returnDirection != Direction.INVALID
            && Math.abs(getMapMetric(r1).getDifference(getMapMetric(r1).getDirection(r1, r2),
                returnDirection)) > 1)
          return false;
        else {
          newDist = 1;
          newPlus = 1;
          newRealDist = 1;
        }
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
     * Returns the direct neighbors of a coordinate.
     *
     * @see magellan.library.utils.Regions.Metric#getNeighbours(magellan.library.CoordinateID)
     */
    public Map<CoordinateID, Region> getNeighbours(CoordinateID id) {
      Map<Direction, Region> allNeighbors = regions.get(id).getNeighbors();
      Map<CoordinateID, Region> neighbors = new HashMap<CoordinateID, Region>();

      for (Region n : allNeighbors.values()) {
        if (regions.containsKey(n.getID()) || n.getID().equals(dest)) {
          neighbors.put(n.getID(), n);
        }
      }
      return neighbors;
    }
  }

  /**
   * Returns the distance on a shortest path over land from start to dest, excluding certain region
   * types and considering streets.
   *
   * @param data
   * @param start
   * @param dest
   * @param excludedRegionTypes regions with these types will be ignored (not entered) in the search.
   * @param radius The number of regions that can be travelled per turn without streets
   * @param streetRadius The number of regions that can be travelled per turn on streets
   * @return The distance in turns from start to dest. Integer.MAX_VALUE if there is no connection
   */
  public static int getLandDistance(GameData data, final CoordinateID start,
      final CoordinateID dest, final Map<ID, RegionType> excludedRegionTypes, final int radius,
      final int streetRadius) {
    Metric metric;
    Regions.getDistances(data.regions(), start, dest, Integer.MAX_VALUE, metric =
        new LandMetric(data.regions(), excludedRegionTypes, start, dest, radius, streetRadius));
    return metric.get(dest).getDistance() / streetRadius / radius;
  }

  public static MapMetric getMapMetric(Region region) {
    return region.getData().getGameSpecificStuff().getMapMetric();
  }

  public static MapMetric getMapMetric(GameData data) {
    return data.getGameSpecificStuff().getMapMetric();
  }

  /**
   * Returns a shortest path over land from start to dest, excluding certain region types and
   * considering streets.
   *
   * @param data
   * @param start
   * @param dest
   * @param excludedRegionTypes regions with these types will be ignored (not entered) in the search.
   * @param radius The number of regions that can be travelled per turn without streets
   * @param streetRadius The number of regions that can be travelled per turn on streets
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

    /**
     * @param regions
     * @param excludedRegionTypes
     * @param start
     * @param dest may be <code>null</code>
     * @param radius
     * @param streetRadius
     */
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
      for (RegionInfo record : metric.getDistances().values()) {
        if (record.getDistance() <= streetRadius) {
          neighbors.put(record.getID(), regions.get(record.getID()));
        }
      }

      return neighbors;
    }
  }

  /**
   * A metric that accounts for movement cost using roads. Distances are in weeks, but multiplied by
   * (streetradius*radius) in order to work with ints, not floats.
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
        // in order to use ints, not floats, we do not use 1/streetradius, but
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

  /**
   * A natural metric where neighboring regions have distance 1.
   */
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
    protected boolean getNewValue(Region r1, Region r2, MultiDimensionalInfo current,
        MultiDimensionalInfo next, int[] newValues) {
      newValues[0] = current.dist.dist + (r1 != r2 ? 1 : 0);
      newValues[1] = 0;
      newValues[2] = current.dist.realDist + (r1 != r2 ? 1 : 0);
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
   * @deprecated {@link Regions#getPath(Map, CoordinateID, CoordinateID, Map)} is better...
   */
  @Deprecated
  public static List<Region> getPath1(Map<CoordinateID, Region> regions, CoordinateID start,
      CoordinateID dest, Map<ID, RegionType> excludedRegionTypes) {
    if ((regions == null) || (start == null) || (dest == null)) {
      Regions.log.warn("Regions.getPath(): invalid argument");

      return Collections.emptyList();
    }

    Map<CoordinateID, Double> distances = new HashMap<CoordinateID, Double>();
    // distances.put(start, Float.valueOf(0.0f)); // contains the distances from the start region to
    // all
    // other regions as Float objects
    distances.put(start, Double.valueOf(0)); // contains the distances from the start region to all
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
      excludedRegionTypes = new HashMap<ID, RegionType>();
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
       * now determine the distance from the start region to the current region taken from the backlog
       * list by checking its neighbour's distances to the start region
       */
      for (Region curNb : neighbours.values()) {
        CoordinateID curNbCoord = curNb.getCoordinate();
        // Float dist = (Float) distances.get(curNbCoord);
        Double dist = distances.get(curNbCoord);
        if (dist != null) {
          /*
           * we know the distance from the start region to this neighbour, so we can determine the distance
           * from the start region to the current region taken from the backlog list
           */
          // float curDistance = getDistance(curNb, curRegion) + dist.floatValue();
          // double curDistance = Regions.getDistance(curNb, curRegion,true) + dist.floatValue();
          double curDistance = Regions.getDistance(curNb, curRegion, true) + dist.doubleValue();

          if (curDistance < minDistance) {
            minDistance = curDistance;
          }
        } else {
          /*
           * we do not know the distance from the start region to this neighbour, so we store this neighbour
           * in the backlog list
           */
          if (!backlogMap.containsKey(curNbCoord)) {
            backlogList.add(curNb);
            backlogMap.put(curNbCoord, null);
          }
        }
      }

      /*
       * If we could determine the distance from the start region to the current region taken from the
       * backlog list, we can remove it from that list and record the distance
       */
      if (minDistance < Double.MAX_VALUE) {
        consecutiveReenlistings = 0;
        backlogList.removeFirst();
        backlogMap.remove(curCoord);
        distances.put(curCoord, Double.valueOf(minDistance));
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
     * now we know the distance of each region to the start region but we do not know a shortest path.
     * We can find one simply by starting at the destination region, looking at its neighbours and
     * choosing the one with the smallest distance to the start region until we reach the start region.
     * This sequence of regions is the reverse shortest path.
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

  /**
   * Returns the distance on a shortest path for a ship from start to dest.
   *
   * @param data
   * @param start
   * @param returnDirection The ship's shore or {@link Direction#INVALID}
   * @param destination
   * @return The distance in turns from start to dest. Integer.MAX_VALUE if there is no connection.
   */
  public static int getShipDistance(GameData data, CoordinateID start, Direction returnDirection,
      CoordinateID destination, int speed) {
    PathWithLength result =
        planShipRouteWithLength(data, start, returnDirection, destination, speed);
    return result.length;
  }

  /**
   * Returns a route for the ship from its current region to its destination.
   */
  public static List<Region> planShipRoute(Ship ship, GameData data, CoordinateID destination) {
    return Regions.planShipRoute(data, ship.getRegion().getCoordinate(), getMapMetric(data)
        .toDirection(ship.getShoreId()), destination, data.getGameSpecificRules()
            .getShipRange(ship));
  }

  static class PathWithLength {
    public PathWithLength(List<Region> p, int l) {
      path = p;
      length = l;
    }

    List<Region> path;
    int length;
  }

  /**
   * Finds a shortest path for a ship from start to destination.
   *
   * @param data
   * @param start
   * @param returnDirection The ship's shore or {@link Direction#INVALID}
   * @param destination
   * @param speed The number of regions per week
   * @return A list of region from start to destination, or <code>null</code> if no path exists.
   */
  public static List<Region> planShipRoute(GameData data, CoordinateID start,
      Direction returnDirection, CoordinateID destination, int speed) {
    PathWithLength result =
        planShipRouteWithLength(data, start, returnDirection, destination, speed);
    return result.path;
  }

  /**
   * Finds a shortest path for a ship from start to destination.
   *
   * @param data
   * @param start
   * @param returnDirection The ship's shore or {@link Direction#INVALID}
   * @param destination
   * @param speed The number of regions per week
   * @return A list of region from start to destination, or <code>null</code> if no path exists and
   *         the length (in turns) of this path.
   */
  protected static PathWithLength planShipRouteWithLength(GameData data, CoordinateID start,
      Direction returnDirection, CoordinateID destination, int speed) {
    BuildingType harbour = data.getRules().getBuildingType(EresseaConstants.B_HARBOUR);

    if (destination == null || data.getRegion(destination) == null || start == null
        || data.getRegion(start) == null)
      // no path
      return null;
    Region destRegion = data.getRegion(destination);
    Region startRegion = data.getRegion(start);

    Map<CoordinateID, Region> harbourRegions = new HashMap<CoordinateID, Region>();
    harbourRegions.put(startRegion.getID(), startRegion);
    harbourRegions.put(destination, destRegion);

    // Fetch all ocean-regions and all regions, that contain a harbor.
    // These are the valid one in which a path shall be searched.
    // if(oceanType != null) {
    for (Region r : data.getRegions()) {
      if ((r.getRegionType() != null && r.getRegionType().isOcean())
          || Regions.containsBuilding(r, harbour)) {
        harbourRegions.put(r.getCoordinate(), r);
      }
    }

    // if (returnDirection != Direction.DIR_INVALID && !Regions.containsBuilding(startRegion,
    // harbour)) {
    // // Ship cannot leave in all directions
    // // try to find a path from every allowed shore-off region to the destination
    //
    // List<Region> bestPath = null;
    // MultiDimensionalInfo bestValue = null;
    // for (Direction tryShore : startRegion.getNeighbors().keySet()) {
    // if (Math.abs(tryShore.getDifference(returnDirection)) > 1) {
    // continue;
    // }
    // Region newStart = startRegion.getNeighbors().get(tryShore);
    // if (newStart == null) {
    // continue;
    // }
    //
    // if (!harbourRegions.containsKey(newStart.getID())
    // || !harbourRegions.get(newStart.getID()).getRegionType().isOcean()) {
    // continue;
    // }
    //
    // ShipMetric metric =
    // new ShipMetric(harbourRegions, newStart.getID(), destination, Collections
    // .<ID, RegionType> emptyMap(), Collections.singleton(harbour), speed, 0);
    // Regions.getDistances(harbourRegions, newStart.getID(), destination, Integer.MAX_VALUE,
    // metric);
    // List<Region> newPath =
    // Regions.getPath(harbourRegions, newStart.getID(), destination, metric.getDistances());
    //
    // MultiDimensionalInfo value = metric.get(destination);
    // if (bestValue == null || value.compareTo(bestValue) < 0) {
    // bestPath = newPath;
    // bestValue = value;
    // }
    // }
    // LinkedList<Region> result = new LinkedList<Region>();
    // result.add(startRegion);
    // if (bestPath == null) {
    // log.fine("planShipRoute without best path from " + start.toString() + " to "
    // + destination.toString());
    // } else {
    // result.addAll(bestPath);
    // }
    // return result;
    // }
    //
    // ShipMetric metric =
    // new ShipMetric(harbourRegions, start, destination, Collections.<ID, RegionType> emptyMap(),
    // Collections.singleton(harbour), speed);
    // Regions.getDistances(harbourRegions, start, destination, Integer.MAX_VALUE, metric);
    // return getPath(harbourRegions, start, destination, metric.getDistances());

    ShipMetric metric =
        new ShipMetric(harbourRegions, start, destination, Collections.<ID, RegionType> emptyMap(),
            Collections.singleton(harbour), speed, returnDirection);
    Regions.getDistances(harbourRegions, start, destination, Integer.MAX_VALUE, metric);
    return new PathWithLength(getPath(harbourRegions, start, destination, metric.getDistances()),
        metric.get(destination).getDistance() / speed
            + (metric.get(destination).getDistance() % speed != 0 ? 1 : 0));

  }

  // /**
  // * Returns an alias region corresponding to the argument that is not a wrapping region. Returns
  // * <code>null</code> if no such region exists.
  // */
  // public static Region getRealRegion(Region r) {
  // for (Region alias : r.getAliases())
  // if (alias.getVisibility() != Visibility.WRAP)
  // return alias;
  // return null;
  // }
  //
  // /**
  // * Returns an alias region corresponding to the argument that is not a wrapping region. Returns
  // * <code>null</code> if no such region exists.
  // */
  // public static Region getRealRegion(GameData data, CoordinateID c) {
  // if (data.getRegion(c) == null)
  // return null;
  // return getRealRegion(data.getRegion(c));
  // }

  /**
   * Returns the (first) direction from prev to cur even if it is wrapped around.
   */
  public static Direction getDirection(Region from, Region to) {
    return getMapMetric(from).getDirection(from, to);
  }

  /**
   * delivers a distance between 2 regions. We assume that both regions are neighbours, so trivial
   * distance is 1 for oceans, we deliver for regions near land a significantly smaller value. For
   * landregions we calculate a new distance...as like moveoints with propper roads: 2, without: 3
   *
   * @param r1
   * @param r2
   * @param useExtendedVersion
   * @return
   * @deprecated Use {@link #getDistances(Map, CoordinateID, CoordinateID, int, Metric)}
   */
  @Deprecated
  private static double getDistance(Region r1, Region r2, boolean useExtendedVersion) {
    double erg = 1;
    if (!useExtendedVersion)
      return 1;
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
    String moverName = (u.getShip() == null) ? u.toString() : u.getShip().toString(false);

    // run over neighbours recursively
    CoordinateID c = Regions.getMovement(moverName, u.getRegion(), coordinates);

    while ((c != null) && !coordinates.contains(c)) {
      coordinates.add(c);
      c = Regions.getMovement(moverName, data.getRegion(c), coordinates);
    }

    Collections.reverse(coordinates);

    return coordinates;
  }

  /**
   * Tries to reconstruct the path from travel through messages. This does not always yield a correct
   * path...
   */
  private static CoordinateID getMovement(String id, Region end, List<CoordinateID> travelledRegions) {

    for (Region r : end.getNeighbors().values()) {
      CoordinateID neighbour = r.getCoordinate();

      if (r == end || travelledRegions.contains(neighbour)) {
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
   * Returns a map of all RegionTypes that are <em>not</em> flagged as <tt>ocean</tt>.
   *
   * @param rules Rules of the game
   * @return map of all non-ocean RegionTypes
   */
  public static Map<ID, RegionType> getNonOceanRegionTypes(Rules rules) {
    Map<ID, RegionType> ret = new HashMap<ID, RegionType>();

    // TODO map size?
    for (Iterator<RegionType> iter = rules.getRegionTypeIterator(); iter.hasNext();) {
      RegionType rt = iter.next();

      if (!rt.isOcean()) {
        ret.put(rt.getID(), rt);
      }
    }
    if (ret.isEmpty()) {
      log.warn("unable to determine land region types!");
    }

    return ret;
  }

  /**
   * Returns a map of all RegionTypes that are flagged as <tt>ocean</tt>.
   *
   * @param rules Rules of the game
   * @return map of all ocean RegionTypes
   */
  public static Map<ID, RegionType> getOceanRegionTypes(Rules rules) {
    Map<ID, RegionType> ret = new HashMap<ID, RegionType>();

    for (Iterator<RegionType> iter = rules.getRegionTypeIterator(); iter.hasNext();) {
      RegionType rt = iter.next();

      if (rt.isOcean()) {
        ret.put(rt.getID(), rt);
      }
    }
    if (ret.isEmpty()) {
      log.warn("unable to determine ocean region types!");
    }

    return ret;
  }

  /**
   * Returns a map of all RegionTypes that are flagged as <tt>land</tt>.
   *
   * @param rules Rules of the game
   * @return map of all land RegionTypes
   */
  public static Map<ID, RegionType> getLandRegionTypes(Rules rules) {
    Map<ID, RegionType> ret = new HashMap<ID, RegionType>();

    for (RegionType rt : rules.getRegionTypes()) {
      if (rt.isLand()) {
        ret.put(rt.getID(), rt);
      }
    }
    if (ret.isEmpty()) {
      log.warn("unable to determine land region types!");
    }

    return ret;
  }

  /**
   * Returns a map of all RegionTypes that are <em>not</em> flagged as <tt>land</tt>.
   *
   * @param rules Rules of the game
   * @return map of all non-land RegionTypes
   */
  public static Map<ID, RegionType> getNonLandRegionTypes(Rules rules) {
    Map<ID, RegionType> ret = new HashMap<ID, RegionType>();

    for (RegionType rt : rules.getRegionTypes()) {
      if (!rt.isLand()) {
        ret.put(rt.getID(), rt);
      }
    }

    if (ret.isEmpty()) {
      log.warn("unable to determine land region types!");
    }

    return ret;
  }

  /**
   * @deprecated Use {@link #getFeuerwandRegionType(GameData)} instead
   */
  @Deprecated
  public static RegionType getFeuerwandRegionType(Rules rules, GameData data) {
    return getFeuerwandRegionType(data);
  }

  /**
   * Returns the RegionType that is named as <tt>Feuerwand</tt>.
   *
   * @param data needed for correct rules
   * @return RegionType Feuerwand
   */
  public static RegionType getFeuerwandRegionType(GameData data) {
    String actFeuerwandName = "Feuerwand";
    actFeuerwandName = data.getTranslation("Feuerwand");
    return data.getRules().getRegionType(StringID.create(actFeuerwandName));
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

    // safety check to avoid IndexOutOfBoundsException
    if (directions.size() == 0) {
      log.info("isCompleteRoadCon - size=0 Error: from " + r1.getCoordinate().toString() + " to "
          + r2.getCoordinate().toString());
      return false;
    }

    Direction dir1 = directions.get(0);
    // border of r1 ->
    boolean border1OK = false;
    for (Border b : r1.borders()) {
      if (b.getType().equals(EresseaConstants.BT_STRASSE.toString())
          && b.getDirection() == dir1.getDirCode() && b.getBuildRatio() == 100) {
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
    // safety check to avoid IndexOutOfBoundsException
    if (directions.size() == 0) {
      log.error("isCompleteRoadCon - size=0 Error: from " + r2.getCoordinate().toString() + " to "
          + r1.getCoordinate().toString());
      return false;
    }
    dir1 = directions.get(0);
    // border of r1 ->
    boolean border2OK = false;
    for (Border b : r2.borders()) {
      if (magellan.library.utils.Umlaut.normalize(b.getType()).equals("STRASSE")
          && b.getDirection() == dir1.getDirCode() && b.getBuildRatio() == 100) {
        border2OK = true;
        break;
      }
    }
    if (border1OK && border2OK)
      return true;

    return erg;
  }

  /**
   * Returns all coordinates with a distance of at most <code>radius</code> to <code>center</code>.
   */
  public static Collection<CoordinateID> getAllNeighbours(CoordinateID center, int radius) {
    Collection<CoordinateID> result =
        new ArrayList<CoordinateID>((radius * 2 + 1) * (radius * 2 + 1));

    for (int dx = -radius; dx <= radius; dx++) {
      for (int dy = (-radius + Math.abs(dx)) - ((dx > 0) ? dx : 0); dy <= ((radius - Math.abs(dx)) - ((dx < 0)
          ? dx : 0)); dy++) {
        result.add(CoordinateID.create(center.getX() + dx, center.getY() + dy, center.getZ()));
      }
    }

    return result;
  }

  /**
   * @deprecated Use {@link #getDist(CoordinateID, CoordinateID)} instead
   */
  @Deprecated
  public static int getRegionDist(CoordinateID r1, CoordinateID r2) {
    return getDist(r1, r2);
  }

  /**
   * Compute distance between two coordinates. Contributed by Hubert Mackenberg. Thanks.
   */
  public static int getDist(CoordinateID r1, CoordinateID r2) {
    int dx = r1.getX() - r2.getX();
    int dy = r1.getY() - r2.getY();
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

    // % part of regions with no borders which will
    // be changed as well
    double specialBorderProbability = 0.3;

    Random r = new Random(System.currentTimeMillis());

    long cnt = 0;
    Regions.log.finer("starting calculation of coasts");
    for (Region currentRegion : data.getRegions()) {
      int coastBitmap = 0;
      if (currentRegion.getRegionType().isOcean()) {
        // we have an ocean in front
        // the result
        // checking all neighbours
        for (Region neighbor : currentRegion.getNeighbors().values()) {
          if (neighbor.getRegionType().isLand()) {
            // not ocean! we should set an 1
            // what is relative coordinate ?
            int intDir = Regions.getDirection(currentRegion, neighbor).getDirCode();
            int bitMask = 1 << intDir;
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
            coastBitmap = coastBitmap | 1 << 7;
            break;
          case 2:
            coastBitmap = coastBitmap | 1 << 6;
            break;
          case 3:
            coastBitmap = coastBitmap | 1 << 6;
            coastBitmap = coastBitmap | 1 << 7;
            break;
          }
        }
      }
      currentRegion.setCoastBitMap(coastBitmap);
    }
    Regions.log.finer("finished calculation of coasts, found " + cnt + " coasts.");
  }

  /**
   * A comparator the sorts coordinates with respect to the angle to the coordinate at the lower right
   * of a set of coordinates.
   *
   * @author stm
   */
  public static class AngleSorter implements Comparator<CoordinateID> {

    private CoordinateID minY;

    /**
     * Initializes the comparator. The sort order will be: The point at the lower right (w.r.t. the
     * geometric position on the Eressea map) lowest, then the other coordinates in increasing order of
     * their angle to the lower right point.
     *
     * @param result
     */
    public AngleSorter(List<CoordinateID> result) {
      for (CoordinateID c : result) {
        if (minY == null || getY(c) < getY(minY) || (getY(c) == getY(minY) && getX(c) > getX(minY))) {
          minY = c;
        }
      }
    }

    public int compare(CoordinateID o1, CoordinateID o2) {
      if (o1.equals(o2))
        return 0;
      if (o1.equals(minY)) // lowest point goes first
        return -1;
      if (o2.equals(minY))
        return 1;
      int dot1 = crossProduct(minY, o1, o2);
      if (dot1 == 0) // same angle
        return dist2(minY, o1) - dist2(minY, o2); // closer point first

      return -dot1; // lower angle first
    }

    private int dist2(CoordinateID c1, CoordinateID c2) {
      return (getX(c2) - getX(c1)) * (getX(c2) - getX(c1)) + (getY(c2) - getY(c1)) * (getY(c2) - getY(c1));
    }

  }

  private static int crossProduct(CoordinateID center, CoordinateID c1, CoordinateID c2) {
    int p1X = getX(c1) - getX(center);
    int p1Y = getY(c1) - getY(center);
    int p2X = getX(c2) - getX(center);
    int p2Y = getY(c2) - getY(center);

    return (p1X * p2Y) - (p2X * p1Y);
  }

  private static double crossProduct(double cx, double cy, double x1, double y1, double x2, double y2) {
    double p1X = x1 - cx;
    double p1Y = y1 - cy;
    double p2X = x2 - cx;
    double p2Y = y2 - cy;

    return (p1X * p2Y) - (p2X * p1Y);
  }

  /**
   * Computes the convex hull of a given set of coordinates.
   *
   * @param points
   * @return A list of vertices of the convex hull in counter-clockwise order.
   */
  public static List<CoordinateID> convexHull(Collection<CoordinateID> points) {
    if (points == null || points.isEmpty())
      throw new RuntimeException("no points");

    List<CoordinateID> result = new ArrayList<CoordinateID>(new HashSet<CoordinateID>(points));
    result.sort(new AngleSorter(result));

    int hullLength = 2;
    for (int i = 2; i < result.size(); ++i) {
      while (hullLength >= 2 && ccw(result.get(hullLength - 2), result.get(hullLength - 1), result.get(i)) <= 0) {
        hullLength--;
      }
      result.set(hullLength++, result.get(i));
    }
    for (int i = result.size() - 1; i >= hullLength; --i) {
      result.remove(i);
    }
    return result;
  }

  private static int ccw(CoordinateID c1, CoordinateID c2, CoordinateID c3) {
    return (getX(c2) - getX(c1)) * (getY(c3) - getY(c1)) - (getY(c2) - getY(c1)) * (getX(c3) - getX(c1));
  }

  /**
   * Tests if the given point is inside the given convex hull. The points of hull must be sorted
   * counterclockwise and in the same layer. Attention: The points that are inside and on the edges do
   * not necessarily form a connected region.
   *
   * @param point
   * @param hull
   * @return 1 if the point is inside, -1 if it is outside and 0 if it lies on the perimeter of the
   *         hull.
   */
  public static int insideConvex(CoordinateID point, List<CoordinateID> hull) {
    int size = hull.size();
    if (size == 1)
      return point.equals(hull.get(0)) ? 0 : -1;

    for (int i = 0; i < size; ++i) {
      CoordinateID currentPoint = hull.get(i), lastPoint = hull.get((i + size - 1) % size);

      int angle = ccw(lastPoint, point, currentPoint);
      if (angle == 0) {
        int sgnx1 = getX(point) - getX(lastPoint),
            sgnx2 = getX(point) - getX(currentPoint),
            sgny1 = getY(point) - getY(lastPoint),
            sgny2 = getY(point) - getY(currentPoint);
        if (((sgnx1 >= 0 && -sgnx2 >= 0) || (sgnx1 <= 0 && -sgnx2 <= 0) || (sgnx1 == 0 && sgnx2 == 0)) && //
            ((sgny1 >= 0 && -sgny2 >= 0) || (sgny1 <= 0 && -sgny2 <= 0) || (sgny1 == 0 && sgny2 == 0)))
          return 0;
        else
          return -1;
      }
      if (angle > 0)
        return -1;
    }
    return 1;

  }

  /**
   * Returns 1 if the point is inside the polygon described by the given vertices, 0 if it is on an
   * edge of the polygon, or -1 if it is outside. The vertices must be given in clockwise or
   * counter-clockwise order. Attention: The points that are inside and on the edges do not
   * necessarily form a connected region.
   *
   * @param point
   * @param vertices
   * @return 1, 0, or -1 if the point is inside, on an edge, or outside the polygon, respectively
   */
  public static int insidePolygon(CoordinateID point, List<CoordinateID> vertices) {
    double[] xx = new double[vertices.size()], yy = new double[vertices.size()];
    coords2Points(vertices, xx, yy);

    return insidePolygon(getX(point), getY(point), xx, yy);
  }

  /**
   * Converts the given coordinates to lists of map coordinates
   *
   * @param vertices A collection of coordinates
   * @param xx An array with at least the same size as the collection of coordinates. The
   *          x-coordinates of the corresponding points will be stored in this array.
   * @param yy An array with at least the same size as the collection of coordinates. The
   *          y-coordinates of the corresponding points will be stored in this array.
   */
  public static void coords2Points(List<CoordinateID> vertices, double[] xx, double[] yy) {
    int i = 0;
    for (CoordinateID c : vertices) {
      xx[i] = getX(c);
      yy[i++] = getY(c);
    }
  }

  /**
   * Return true if the hexagon with inner radius RADIUS intersects the borders of the polygon given
   * by the xx and yy coordinates.
   *
   * @param point
   * @param xx
   * @param yy
   * @return <code>true</code> if the hexagon intersects the polygon.
   */
  public static boolean hexagonIntersects(CoordinateID point, double[] xx, double[] yy) {
    double minx = Double.POSITIVE_INFINITY, miny = Double.POSITIVE_INFINITY, maxx = Double.NEGATIVE_INFINITY, maxy =
        Double.NEGATIVE_INFINITY;
    for (int i = 0; i < xx.length; ++i) {
      minx = Math.min(minx, xx[i]);
      miny = Math.min(miny, yy[i]);
      maxx = Math.max(minx, xx[i]);
      maxy = Math.max(miny, yy[i]);
    }

    if (getX(point) < minx - RADIUS && getX(point) > maxx + RADIUS
        && getY(point) < miny - RADIUS && getY(point) > maxy + RADIUS)
      return false;
    List<Point2D> corners = getVertices(point);
    Point2D lastCorner = corners.get(corners.size() - 1);
    for (Point2D p : corners) {
      for (int j = 0; j < xx.length; ++j) {
        if (almostIntersects(lastCorner.getX(), lastCorner.getY(), p.getX(), p.getY(), xx[j], yy[j], xx[(j + 1)
            % xx.length], yy[(j + 1) % yy.length]))
          return true;
      }

      lastCorner = p;
    }
    return false;
  }

  /**
   * Returns true if the line segment (x1,y1)&rarr;(x2,y2) "almost" intersects (x3,y3)&rarr;(x4,y4). This
   * method may report line segments as intersecting that are just very close (closer than EPSILON).
   *
   * @param x1
   * @param y1
   * @param x2
   * @param y2
   * @param x3
   * @param y3
   * @param x4
   * @param y4
   * @return <code>true</code> iff the line segments intersect.
   */
  public static boolean almostIntersects(double x1, double y1, double x2, double y2, double x3, double y3,
      double x4, double y4) {
    double det = (x1 - x2) * (y3 - y4) - (y1 - y2) * (x3 - x4);
    if (det < EPSILON && det > -EPSILON)
      return (Math.min(x1, x2) <= Math.max(x3, x4) && Math.max(x1, x2) >= Math.min(x3, x4)) &&
          (Math.min(y1, y2) <= Math.max(y3, y4) && Math.max(y1, y2) >= Math.min(y3, y4));

    double t = (x1 - x3) * (y3 - y4) - (y1 - y3) * (x3 - x4);
    t /= det;
    double u = (y1 - y2) * (x1 - x3) - (x1 - x2) * (y1 - y3);
    u /= det;
    return (-EPSILON < t && t < 1 + EPSILON) && (-EPSILON < u && u < 1 + EPSILON);
  }

  /**
   * Inner radius (or half distance) of hexagons.
   */
  public static final int RADIUS = 2;

  private static List<Point2D> getVertices(CoordinateID point) {
    List<Point2D> vertices = new ArrayList<Point2D>(6);
    int x = getX(point), y = getY(point);
    double bigR = 2 * RADIUS / Math.sqrt(3), r = RADIUS;
    vertices.add(new Point2D.Double(x + r, y + bigR / 2));
    vertices.add(new Point2D.Double(x, y + bigR));
    vertices.add(new Point2D.Double(x - r, y + bigR / 2));
    vertices.add(new Point2D.Double(x - r, y - bigR / 2));
    vertices.add(new Point2D.Double(x, y - bigR));
    vertices.add(new Point2D.Double(x + r, y - bigR / 2));
    return vertices;
  }

  /**
   * Returns 1 if the point px, py is inside the polygon described by the given x and y coordinates, 0
   * if it is on an edge of the polygon, or -1 if it is outside. The vertices must be given in
   * clockwise or counter-clockwise order. Attention: The set of points that are inside and on the
   * edges do not necessarily form a connected region.
   *
   * @param px
   * @param py
   * @param xx
   * @param yy
   * @return 1, 0, or -1 if the point is inside, on an edge, or outside the polygon, respectively
   */
  public static int insidePolygon(double px, double py, double[] xx, double[] yy) {
    if (xx.length == 1)
      return (px == xx[0] && py == yy[0]) ? 0 : -1;

    int intersection = 0;
    for (int i = 0; i < yy.length; ++i) {
      int i0 = (i + yy.length - 1) % yy.length;
      double angle = crossProduct(px, py, xx[i0], yy[i0], xx[i], yy[i]);
      double dy1 = py - yy[i0], dy2 = yy[i] - py;

      if (Math.abs(angle) < EPSILON) {
        double dx1 = px - xx[i0], dx2 = xx[i] - px;
        if (dx1 * dx2 >= 0 && dy1 * dy2 >= 0)
          return 0;
      }
      if (dy2 == 0 || dy1 == 0) {
        angle = crossProduct(px, py + 100 * EPSILON, xx[i0], yy[i0], xx[i], yy[i]);
        dy1 = py + 100 * EPSILON - yy[i0];
        dy2 = yy[i] - py - 100 * EPSILON;
      }

      if (dy1 * dy2 > 0) {
        if (crossProduct(px, py, xx[i0], yy[i0], xx[i], yy[i]) * dy1 >= -EPSILON) {
          intersection++;
          if (crossProduct(px, py, xx[i0], yy[i0], xx[i], yy[i]) * dy1 < EPSILON)
            return 0;
        }
      }
    }
    return (intersection % 2 == 0) ? -1 : 1;
  }

  private static int getX(CoordinateID point) {
    return 2 * RADIUS * point.getX() + RADIUS * point.getY();

  }

  private static int getY(CoordinateID point) {
    return 3 * RADIUS / 2 * point.getY(); // 1,5 <- 1; ,75 <- 1
  }

  /**
   * Returns a list that consists of the coordinates in points whose z-coordinates are in the given
   * layer.
   *
   * @param layer
   * @param points
   * @return New list of coordinates.
   */
  public static List<CoordinateID> filter(int layer, Collection<CoordinateID> points) {
    List<CoordinateID> filtered = new ArrayList<CoordinateID>();

    for (CoordinateID c : points) {
      if (c.getZ() == layer) {
        filtered.add(c);
      }
    }
    return filtered;
  }

  public static Collection<? extends Collection<Region>> getComponents(Map<CoordinateID, Region> regions) {
    ArrayList<ArrayList<Region>> components = new ArrayList<ArrayList<Region>>();
    Map<Region, Integer> visited = new HashMap<Region, Integer>();
    for (Region r : regions.values()) {
      if (!visited.containsKey(r)) {
        components.add(getComponent(r, regions, visited));
      }
    }

    return components;
  }

  private static ArrayList<Region> getComponent(Region start, Map<CoordinateID, Region> regions,
      Map<Region, Integer> visited) {
    ArrayList<Region> comp = new ArrayList<Region>();
    Set<Region> bag = new HashSet<Region>();
    bag.add(start);
    while (!bag.isEmpty()) {
      Region r = bag.iterator().next();
      bag.remove(r);
      visited.put(r, 1);
      comp.add(r);

      for (Region n : r.getNeighbors().values()) {
        if (!visited.containsKey(n) && regions.containsKey(n.getCoordinate())) {
          bag.add(n);
        }
      }
    }
    return comp;
  }

}
