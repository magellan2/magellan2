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
import magellan.library.rules.BuildingType;
import magellan.library.rules.RegionType;
import magellan.library.utils.logging.Logger;

/**
 * A class offering common operations on regions.
 */
public class CopyOfRegions {
  private static final Logger log = Logger.getInstance(CopyOfRegions.class);

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
  public static Map<CoordinateID, Region> getAllNeighbours(Map<CoordinateID, Region> regions,
      ID center, int radius, Map<ID, RegionType> excludedRegionTypes) {
    if (center instanceof CoordinateID) {
      return CopyOfRegions.getAllNeighbours(regions, (CoordinateID) center, radius, excludedRegionTypes);
    } else {
      throw new IllegalArgumentException(
          "center is not an eressea coordinate. Support for e2 incomplete!");
    }
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
    return CopyOfRegions.getAllNeighbours(regions, center, 1, excludedRegionTypes);
  }

  /**
   * Find a way from one region to another region and get the directions in which to move to follow
   * a sequence of regions. This is virtually the same as
   * 
   * <pre>getDirections(getPath(regions, start, dest, excludedRegionTypes, radius));</pre>
   * 
   * @return a String telling the direction statements necessary to follow the sequence of regions
   *         contained in regions.
   */
  public static String getDirections(Map<CoordinateID, Region> regions, CoordinateID start,
      CoordinateID dest, Map<ID, RegionType> excludedRegionTypes, int radius) {
    return CopyOfRegions.getDirections(CopyOfRegions.getPath(regions, start, dest, excludedRegionTypes, radius,
        radius));
  }

  /**
   * Get the directions in which to move to follow a sequence of regions.
   * 
   * @param regions an ordered consecutive sequence of regions.
   * @return a String telling the direction statements necessary to follow the sequence of regions
   *         contained in regions.
   */
  public static String getDirections(Collection<Region> regions) {
    if (regions == null) {
      return null;
    }

    List directions = CopyOfRegions.getDirectionObjectsOfRegions(regions);

    if (directions == null) {
      return null;
    }

    StringBuffer dir = new StringBuffer();

    for (Iterator iter = directions.iterator(); iter.hasNext();) {
      Direction d = (Direction) iter.next();

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
    if (regions == null) {
      return null;
    }

    List<CoordinateID> coordinates = new ArrayList<CoordinateID>(regions.size());

    for (Iterator<Region> iter = regions.iterator(); iter.hasNext();) {
      Region r = iter.next();
      coordinates.add(r.getCoordinate());
    }

    return CopyOfRegions.getDirectionObjectsOfCoordinates(coordinates);
  }

  /**
   * DOCUMENT-ME
   */
  public static List<Direction> getDirectionObjectsOfCoordinates(
      Collection<CoordinateID> coordinates) {
    if (coordinates == null) {
      return null;
    }

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
   * Find a way from one region to another region.
   * 
   * @return a Collection of regions that have to be trespassed in order to get from the one to the
   *         other specified region, including both of them.
   * @throws IllegalArgumentException if start or dest are not CoordinateIDs
   * @deprecated This is now obsolete
   */
  public static List<Region> getPath(Map<CoordinateID, Region> regions, ID start, ID dest,
      Map<ID, RegionType> excludedRegionTypes) {
    if (start instanceof CoordinateID && dest instanceof CoordinateID) {
      return CopyOfRegions.getPath(regions, (CoordinateID) start, (CoordinateID) dest,
          excludedRegionTypes, 1, 1);
    } else {
      throw new IllegalArgumentException(
          "start of dest is not an eressea coordinate. Support for e2 incomplete!");
    }
  }
  
  /**
   * A generalization of a distance value.
   */
  interface Distance extends Comparable {
    public int getDistance();
  }

  /**
   * A metric on Coordinates.
   * 
   * @author stm
   */
  interface Metric {
    /**
     * Returns the sum of <code>dist</code> and the distance between two neighboring coordinates.
     * All Distance methods should return distances of the same type! This method must assure
     * <code>getDistance(x,x, dist).compareTo(dist) == 0</code>.
     * 
     * @param a
     * @param b
     * @return The distance of a and b plus dist.
     * @see {@link #getNeighbours(CoordinateID)}
     */
    public void getDistance(CoordinateID current, CoordinateID next, Distance dist, Distance old);

    public Distance getZero();

    /**
     * Returns an infinity distance value that is greater than all other distances.
     */
    public Distance getInfinity();

    /**
     * Returns all neighbors of the provided coordinate.
     * 
     * @param id
     * @return
     */
    public Map<CoordinateID, Region> getNeighbours(CoordinateID id);

  }

  /**
   * Stores distance about a region.
   */
  static class RegionRecord implements Comparable<RegionRecord> {
    public static long instanceCount = 0; 
    
    /** This record's information is for this coordinate */
    public CoordinateID id;
    /** The distance from the start */
    public Distance dist;
    /** A node that is on the shortest path from start to id */
    public CoordinateID pre;

    /** <code>true</code> if this id's neighbors have been touched in the search */
    boolean visited;

    public RegionRecord(CoordinateID id, Distance dist) {
      this.id = id;
      this.dist = dist;
      instanceCount++;
    }

    public int compareTo(RegionRecord o) {
      return dist.compareTo(o.dist);
    }
  }

  /**
   * Returns distance information about all Coordinates in regions that are at most as far as dest
   * or as far as maxDist away from start.
   * 
   * @param regions Regions will be looked up in this map.
   * @param start The origin of the search.
   * @param dest The coordinate that must be reached, or <code>null</code>
   * @param maxDist The maximum radius to search.
   * @param metric A metric for computing distances
   * @return A map that maps each coordinate met in the search to its distance information.
   */
  public static Map<CoordinateID, RegionRecord> getDistances(Map<CoordinateID, Region> regions,
      CoordinateID start, CoordinateID dest, int maxDist, Metric metric) {
    if (!regions.containsKey(start) || (dest != null && !regions.containsKey(dest)))
      throw new IllegalArgumentException();

    Map<CoordinateID, RegionRecord> records = new HashMap<CoordinateID, RegionRecord>();
    PriorityQueue<RegionRecord> queue = new PriorityQueue<RegionRecord>(8);

    queue.add(new RegionRecord(start, metric.getZero()));
    records.put(start, queue.peek());

    int touched = 0;
    int maxQueue = 1;
    
    Distance newDist = metric.getInfinity();
    
    while (!queue.isEmpty() && queue.peek().dist.getDistance() <= maxDist
        && !queue.peek().id.equals(dest)) {
      RegionRecord current = queue.poll();
      // for debugging
      if (regions.get(current.id) != null
          && (metric instanceof LandMetric || metric instanceof ShipMetric)) {
// regions.get(current.id).clearSigns();
//        regions.get(current.id).addSign(new Sign(touched + " " + current.dist.toString()));
      } else
        touched += 0;
      touched++;
      current.visited = true;
      Map<CoordinateID, Region> neighbors = metric.getNeighbours(current.id);
      for (CoordinateID next : neighbors.keySet()) {
        RegionRecord nextRecord = records.get(next);
        if (nextRecord == null) {
          nextRecord = new RegionRecord(next, metric.getInfinity());
          records.put(next, nextRecord);
        }
        if (!nextRecord.visited) {
          metric.getDistance(current.id, next, current.dist, newDist);
          // if d(next)=\infty OR d(current)+d(current,next) < d(next)
          // decrease key
          if (records.get(next) == null) {
            log.warn("---");
          } else if (nextRecord.dist.compareTo(newDist) > 0) {
            queue.remove(next);
            Distance tempDist = nextRecord.dist;
            nextRecord.dist = newDist;
            newDist = tempDist;
            nextRecord.pre = current.id;
          }
          if (!queue.contains(nextRecord))
            queue.add(nextRecord);
        }
      }
      if (queue.size() > maxQueue)
        maxQueue = queue.size();
    }
    if (queue.isEmpty()) {
      return Collections.emptyMap();
    }

    log.debug(touched+" "+maxQueue+" "+RegionRecord.instanceCount+" "+FloatDistance.instanceCount);
    
    return records;
  }

  /**
   * Returns a path from start to dest based on the distance information in records.
   */
  public static List<Region> getPath(Map<CoordinateID, Region> regions, CoordinateID start,
      CoordinateID dest, Map<CoordinateID, RegionRecord> records) {
    LinkedList<Region> path = new LinkedList<Region>();
    CoordinateID currentID = records.get(dest).id;
    while (currentID != start && currentID != null) {
      path.addFirst(regions.get(currentID));
      currentID = records.get(currentID).pre;
    }
    path.addFirst(regions.get(currentID));

    return path;
  }

  /**
   * Returns a path from start to dest based on the distance information in records. Accounts for
   * skipped regions.
   */
  public static List<Region> getPath2(Map<CoordinateID, Region> regions, CoordinateID start,
      CoordinateID dest, final Map<ID, RegionType> excludedRegionTypes,
      Map<CoordinateID, RegionRecord> records, Metric metric) {
    LinkedList<Region> path = new LinkedList<Region>();
    CoordinateID currentID = records.get(dest).id;
    while (currentID != start && currentID != null) {
      CoordinateID currentStart = records.get(currentID).pre;

      Map<CoordinateID, RegionRecord> distances =
          getDistances(regions, currentStart, currentID, Integer.MAX_VALUE, metric);
      List<Region> interPath = getPath(regions, currentStart, currentID, distances);
      interPath.remove(0);

      // Map<CoordinateID, RegionRecord> distances =
// getDistances(regions, currentStart, currentID, Integer.MAX_VALUE, metric);
// if (distances.get(currentID).dist.equals(getRoadMetric(regions, excludedRegionTypes)
// .getInfinity())) {
// distances =
// getDistances(regions, currentStart, currentID, Integer.MAX_VALUE, getUnitMetric(
// regions, excludedRegionTypes));
// }
// List<Region> interPath = getPath(regions, currentStart, currentID, distances);
// interPath.remove(0);

// Map<CoordinateID, RegionRecord> interDist2 =
// getDistances(regions, currentStart, currentID, Integer.MAX_VALUE, new RoadMetric2(
// regions, excludedRegionTypes));
// List<Region> interPath2 = getPath(regions, currentStart, currentID, interDist2);
// interPath2.remove(0);
//
// if (interPath.size() != interPath2.size())
// log.warn("error");
// else {
// Iterator<Region> it1 = interPath.iterator();
// Iterator<Region> it2 = interPath2.iterator();
// for (; it1.hasNext();) {
// if (it1.next() != it2.next())
// log.warn("error");
// }
// }

      path.addAll(0, interPath);
      currentID = currentStart;
    }
    path.addFirst(regions.get(currentID));
    return path;
  }

  /**
   * A distance implementation that uses three values. <code>dist</code> is the distance in regions,
   * that has been modified if harbours have been encountered on the path. <code>plus</code> is the
   * number of oceans near the coast or land regions on the path. <code>realDist</code> is the
   * actual length of the path.
   * 
   * @author stm
   */
  static class FloatDistance implements Distance {
    public static long instanceCount = 0;
    /** Distance value for a path between a coastal region an another region */
//    public static final FloatDistance ONE_PLUS = new FloatDistance(1, 1, 1);
//    public static FloatDistance ZERO = new FloatDistance(0, 0, 0);
//    public static FloatDistance ONE = new FloatDistance(1, 0, 1);
//    public static FloatDistance TWO = new FloatDistance(2, 0, 2);
//    public static FloatDistance INFINITY =
//        new FloatDistance(Integer.MAX_VALUE, 0, Integer.MAX_VALUE);
    public static final int ONE_PLUS = 2;
    public static final int ZERO = 0;
    public static final int ONE = 1;
    public static final int INFINITY = Integer.MAX_VALUE;

    int dist;
    int plus;
    int realDist;
    private int pot;

    public FloatDistance(int d, int p, int r) {
      this(d,p,r, 0);
    }

    public FloatDistance(int d, int p, int r, int pot) {
      dist = d;
      plus = p;
      realDist = r;
      this.pot = pot;
      instanceCount++;
    }

    public FloatDistance() {
      this(0,0,0,0);
    }

    public int getDistance() {
      return dist;
    }
    
    public void set(int d, int p, int r) {
      dist = d;
      plus = p;
      realDist = r;
    }

    public void set(int d, int p, int r, int pot) {
      dist = d;
      plus = p;
      realDist = r;
      this.pot = pot;
    }

    /**
     * Compares the dist values first, the number of non-coast regions second and the real path
     * length third.
     * 
     * @see java.lang.Comparable#compareTo(java.lang.Object)
     */
    public int compareTo(Object o) {
      if (o instanceof FloatDistance) {
        FloatDistance iDist = (FloatDistance) o;
        if (dist + pot > iDist.dist + iDist.pot)
          return 1;
        if (dist + pot < iDist.dist + iDist.pot)
          return -1;
        if (realDist - plus > iDist.realDist - iDist.plus)
          return 1;
        if (realDist - plus < iDist.realDist - iDist.plus)
          return -1;
        return realDist - iDist.realDist;
      }
      throw new IllegalArgumentException(
          "something very evil has happened that may or may not have been prevented by Java generics...");
    }

    public FloatDistance add(FloatDistance add) {
      if (add.dist==Integer.MAX_VALUE || dist == Integer.MAX_VALUE)
        return get(INFINITY);
      return new FloatDistance(dist + add.dist, plus + add.plus, realDist + add.realDist, pot
          + add.pot);
    }

    public Distance add1(FloatDistance dist2) {
      dist+=dist2.dist;
      plus+=dist2.plus;
      realDist+=dist2.realDist;
      pot+=dist2.pot;
      return this;
    }

    private static FloatDistance get(int size) {
      switch (size) {
      case ZERO:
        return new FloatDistance(0,0,0);
      case ONE:
        return new FloatDistance(1,0,1);
      case ONE_PLUS:
        return new FloatDistance(1,1,1);
      case INFINITY:
        return new FloatDistance(Integer.MAX_VALUE, 0, Integer.MAX_VALUE);
      default:
        throw new IllegalArgumentException();
      }
    }

    public String toString() {
      return String.valueOf(dist) + "," + String.valueOf(plus) + "," + String.valueOf(realDist);
    }

    @Override
    public boolean equals(Object o) {
      if (o instanceof FloatDistance) {
        FloatDistance other = (FloatDistance) o;
        return dist == other.dist && plus == other.plus && realDist == other.realDist;
      }
      return false;

    }

    public void setInfinity() {
      this.dist = Integer.MAX_VALUE;
    }

    public void setZero() {
      this.dist = 0;
      this.plus = 0;
      this.realDist = 0;
      this.pot = 0;
    }

    public void set(FloatDistance dist2) {
      set(dist2.dist, dist2.plus, dist2.realDist, dist2.pot);
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
  public static List<Region> getPath(final Map<CoordinateID, Region> regions,
      final CoordinateID start, final CoordinateID dest,
      final Map<ID, RegionType> excludedRegionTypes, final Set<BuildingType> harbourTypes,
      final int speed) {
    Map<CoordinateID, RegionRecord> distance =
        getDistances(regions, start, dest, Integer.MAX_VALUE, getShipMetric(regions, start, dest,
            excludedRegionTypes, harbourTypes, speed));
    return getPath(regions, start, dest, distance);
  }

  protected static Metric getShipMetric(final Map<CoordinateID, Region> regions,
      CoordinateID start, CoordinateID dest, final Map<ID, RegionType> excludedRegionTypes,
      final Set<BuildingType> harbourTypes, final int speed) {
    return new ShipMetric(regions, start, dest, excludedRegionTypes, harbourTypes, speed);
  }

  protected static abstract class FloatMetric implements Metric {
    protected Map<CoordinateID, Region> regions;
    protected Map<ID, RegionType> excludedRegionTypes;

    public FloatMetric(Map<CoordinateID, Region> regions, Map<ID, RegionType> excludedRegionTypes) {
      this.regions = regions;
      this.excludedRegionTypes = excludedRegionTypes;
    }

    public Distance getZero() {
      return FloatDistance.get(FloatDistance.ZERO);
    }

    /**
     * @see magellan.library.utils.CopyOfRegions.Metric#getInfinity()
     */
    public Distance getInfinity() {
      return FloatDistance.get(FloatDistance.INFINITY);
    }

    /**
     * Should be called by {@link #getDistance(CoordinateID, CoordinateID, Distance)} to exclude
     * some special cases.
     * 
     * @return <code>null</code> if no special case occureed
     */
    public boolean checkDistArguments(Region current, Region next, Distance dist, Distance old2) {
      if (old2 instanceof FloatDistance) {
        FloatDistance old = (FloatDistance) old2;
        if (current == null || next == null) {
          old.setInfinity();
          return true;
        }

        if (dist != null) {
          if (dist instanceof FloatDistance) {
            if (dist.equals(FloatDistance.get(FloatDistance.INFINITY))) {
              old.setInfinity();
              return true;
            }
          } else
            throw new IllegalArgumentException(
                "something very evil has happened that may or may not have been prevented by Java generics...");
        }          
        if (excludedRegionTypes.containsKey(current.getType().getID())
            || excludedRegionTypes.containsKey(next.getType().getID()))
          old.setInfinity();
        else if (next == current) {
          if (dist == null)
            old.setZero();
          else
            old.set((FloatDistance) dist);
          return true;
        }
      } else {
        throw new IllegalArgumentException(
        "something very evil has happened that may or may not have been prevented by Java generics...");
      }
      return false;
    }
  }

  /**
   * A metric for ship paths
   */
  protected static class ShipMetric extends FloatMetric {
    private Set<BuildingType> harbourType;
    private int speed;
    private CoordinateID start;
    private CoordinateID dest;

    public ShipMetric(Map<CoordinateID, Region> regions, CoordinateID start, CoordinateID dest,
        Map<ID, RegionType> excludedRegionTypes, Set<BuildingType> harbourTypes, int speed) {
      super(regions, excludedRegionTypes);
      this.harbourType = harbourTypes;
      this.speed = speed;
      this.start = start;
      this.dest = dest;
    }

    /**
     * Returns the distance between r1 an r2 plus dist. If dist is <code>null</code> it returns the
     * distance between r1 and r2. Otherwise, it usually returns the component-wise sum of the
     * distance between the regions and dist. If, however, r2 is a land region, the result is
     * rounded up to the next multiple of {@link #speed}.
     * 
     * @param r1
     * @param r2
     * @param dist
     * @return
     */
    public void getDistance(CoordinateID current, CoordinateID next, Distance dist2, Distance oldDist) {
      Region r1 = regions.get(current);
      Region r2 = regions.get(next);
      if (checkDistArguments(r1, r2, dist2, oldDist))
        return;
      
      FloatDistance result = (FloatDistance) oldDist;

      // TODO does not do what it should...
//      if (Direction.toInt(current.createDistanceCoordinate(next)) == Direction.DIR_INVALID){
//        result.setInfinity();
//        return;
//      }

      // Fiete 20061123
      // for Ships...prefer Regions near coasts
      // for land units...prefer Regions with roads
      // Trick: if suitable situation, reduce the distance minimal

      FloatDistance dist = (FloatDistance) dist2;

      // add potential for goal-directed search:
      int xdiff1 = Math.abs(current.x - dest.x);
      int ydiff1 = Math.abs(current.y - dest.y);
      int xdiff2 = Math.abs(next.x - dest.x);
      int ydiff2 = Math.abs(next.y - dest.y);
      int pot = Math.max(xdiff2, ydiff2) - Math.max(xdiff1, ydiff1);

      if (r1.getRegionType().isOcean()) {
        if (r2.getRegionType().isOcean()) {
          if (r2.getOceanWithCoast() == 1) {
            result.set(1,1,1, pot);
          } else {
            result.set(1, 0, 1, pot);
          }
        } else {
          // movement ends in land regions, so round up to next multiple of speed
          if (dist != null)
            result.set(speed - dist.dist % speed, 1, 1, pot);
          else
            result.set(1,1,1, pot);
        }
      } else {
        if (!r2.getRegionType().isOcean())
          // this should not happen
          log.warn("ship route to neighboring land region");
        result.set(1,1,1, pot);
      }

      if (dist != null)
        result.add1(dist);
    }

    /**
     * Returns the direct neighbors of id.
     * 
     * @see magellan.library.utils.CopyOfRegions.Metric#getNeighbours(magellan.library.CoordinateID)
     */
    public Map<CoordinateID, Region> getNeighbours(CoordinateID id) {
      return CopyOfRegions.getAllNeighbours(regions, id, null);
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
  public static List<Region> getPath(final Map<CoordinateID, Region> regions,
      final CoordinateID start, final CoordinateID dest,
      final Map<ID, RegionType> excludedRegionTypes, final int radius, final int streetRadius) {
    Map<CoordinateID, RegionRecord> distances =
        getDistances(regions, start, dest, Integer.MAX_VALUE, getLandMetric(regions,
            excludedRegionTypes, radius, streetRadius));
    return getPath2(regions, start, dest, excludedRegionTypes, distances, getRoadMetric(regions,
        excludedRegionTypes, radius, streetRadius));
  }

  public static Metric getLandMetric(final Map<CoordinateID, Region> regions,
      final Map<ID, RegionType> excludedRegionTypes, final int radius, int streetRadius) {
    return new LandMetric(regions, excludedRegionTypes, radius, streetRadius);
  }

  /**
   * A metric for land regions. The secret lies in the neighbor function, which returns regions that
   * can be reached in one week.
   */
  static class LandMetric extends FloatMetric {
    private int radius;
    private int streetRadius;

    public LandMetric(Map<CoordinateID, Region> regions, Map<ID, RegionType> excludedRegionTypes,
        int radius, int streetRadius) {
      super(regions, excludedRegionTypes);
      this.radius = radius;
      this.streetRadius = streetRadius;
    }

    /**
     * delivers a distance between 2 regions we asume, that both regions are neighbours, so trivial
     * distance is 1 for oceans, we deliver for landnearregions a significant smaller value for
     * landregions we calculate a new distance...as like moveoints with propper roads: 2, without: 3
     * 
     * @param r1
     * @param r2
     * @return
     */
    public void getDistance(CoordinateID current, CoordinateID next, Distance dist2, Distance oldDist) {
      Region r1 = regions.get(current);
      Region r2 = regions.get(next);
      if (checkDistArguments(r1, r2, dist2, oldDist))
        return;
      
      FloatDistance result = (FloatDistance) oldDist;
      FloatDistance dist = (FloatDistance) dist2;

      Map<CoordinateID, RegionRecord> distances =
          getDistances(regions, current, next, Integer.MAX_VALUE, getRoadMetric(regions,
              excludedRegionTypes, radius, streetRadius));

      if (dist == null) {
        result.set((FloatDistance) distances.get(next).dist);
      } else {
        int rawDist = dist.dist + ((FloatDistance) distances.get(next).dist).dist;
        result.set(rawDist + streetRadius * radius - rawDist
            % (radius * streetRadius), dist.plus, dist.plus + 1);
      }
    }

    public Map<CoordinateID, Region> getNeighbours(CoordinateID id) {
      Map<CoordinateID, RegionRecord> distances =
          getDistances(regions, id, null, streetRadius, getRoadMetric(regions, excludedRegionTypes,
              radius, streetRadius));
      Map<CoordinateID, Region> neighbors = new HashMap<CoordinateID, Region>();
      for (RegionRecord record : distances.values()) {
        if (record.dist.getDistance() <= streetRadius)
          neighbors.put(record.id, regions.get(record.id));
      }

      return neighbors;
    }
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
  protected static float getFloatDistance(Region r1, Region r2) {
    // Fiete 20061123
    // for Ships...prefer Regions near coasts
    // for land units...prefer Regions with roads
    // Trick: if suitable situation, reduce the distance minimal

    // if we have 2 Ozean regions...
    if (r1.getRegionType().isOcean() && r2.getRegionType().isOcean()) {
      if (r2.getOceanWithCoast() == 1) {
        return .999f;
      } else {
        return 1;
      }
    }

    // if we have 2 non-ocean regions....
    if (!r1.getRegionType().isOcean() && !r2.getRegionType().isOcean()) {
      if (CopyOfRegions.isCompleteRoadConnection(r1, r2)) {
        return 1.999f;
      } else {
        return 3f;
      }
    }

    return 1;
  }

  public static RoadMetric getRoadMetric(Map<CoordinateID, Region> regions,
      Map<ID, RegionType> excludedRegionTypes, int radius, int streetRadius) {
    return new RoadMetric(regions, excludedRegionTypes, radius, streetRadius);
  }

  static class RoadMetric extends FloatMetric {

    private int radius;
    private int streetRadius;

    public RoadMetric(Map<CoordinateID, Region> regions, Map<ID, RegionType> excludedRegionTypes,
        int radius, int streetRadius) {
      super(regions, excludedRegionTypes);
      this.radius = radius;
      this.streetRadius = streetRadius;
    }

    public void getDistance(CoordinateID current, CoordinateID next, Distance dist2, Distance oldDist) {
      Region r1 = regions.get(current);
      Region r2 = regions.get(next);
      if (checkDistArguments(r1, r2, dist2, oldDist))
        return;
      
      FloatDistance result = (FloatDistance) oldDist;
      FloatDistance dist = (FloatDistance) dist2;

      if (CopyOfRegions.isCompleteRoadConnection(regions.get(current), regions.get(next)))
        result.set(radius, 0, 1);
      else
        result.set(streetRadius, 0, 1);

      if (dist != null)
        result.add1(dist);
    }

    public Map<CoordinateID, Region> getNeighbours(CoordinateID id) {
      return CopyOfRegions.getAllNeighbours(regions, id, excludedRegionTypes);
    }
  }

  public static UnitMetric getUnitMetric(Map<CoordinateID, Region> regions,
      Map<ID, RegionType> excludedRegionTypes) {
    return new UnitMetric(regions, excludedRegionTypes);
  }

  static class UnitMetric extends FloatMetric {

    public UnitMetric(Map<CoordinateID, Region> regions, Map<ID, RegionType> excludedRegionTypes) {
      super(regions, excludedRegionTypes);
    }

    public void getDistance(CoordinateID current, CoordinateID next, Distance dist2, Distance oldDist) {
      Region r1 = regions.get(current);
      Region r2 = regions.get(next);
      if (checkDistArguments(r1, r2, dist2, oldDist))
        return;
      
      FloatDistance result = (FloatDistance) oldDist;
      FloatDistance dist = (FloatDistance) dist2;

      result.set(1,0,1);
      if (dist != null) {
        result.add1(dist);
      }
    }

    public Map<CoordinateID, Region> getNeighbours(CoordinateID id) {
      return CopyOfRegions.getAllNeighbours(regions, id, excludedRegionTypes);
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
      CopyOfRegions.log.warn("Regions.getPath(): invalid argument");

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
        CopyOfRegions.getAllNeighbours(regions, start, excludedRegionTypes);
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
        CopyOfRegions.log.warn("Regions.getPath(): Found an region of type "
            + curRegion.getType().getName() + " in region list! Removing and ignoring it.");
        backlogList.removeFirst();
        backlogMap.remove(curCoord);

        continue;
      }

      if (distances.containsKey(curCoord)) {
        CopyOfRegions.log
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
          CopyOfRegions.getAllNeighbours(regions, curCoord, excludedRegionTypes);
      neighbours.remove(curCoord);

      /*
       * now determine the distance from the start region to the current region taken from the
       * backlog list by checking its neighbour's distances to the start region
       */
      for (Iterator<Region> iter = neighbours.values().iterator(); iter.hasNext();) {
        Region curNb = iter.next();
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
          double curDistance = CopyOfRegions.getDistance(curNb, curRegion, true) + dist.doubleValue();

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
            CopyOfRegions.log.warn("Regions.getPath(): looks like an endless loop. Exiting.");

            break;
          }
        } else {
          CopyOfRegions.log
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
        Map neighbours = CopyOfRegions.getAllNeighbours(regions, curCoord, excludedRegionTypes);
        neighbours.remove(curCoord);

        for (Iterator iter = neighbours.values().iterator(); iter.hasNext();) {
          Region curNb = (Region) iter.next();
          CoordinateID curNbCoord = curNb.getCoordinate();
          Double nbDist = distances.get(curNbCoord);

          if (nbDist != null) {
            double curDistance = nbDist.doubleValue();

            // add the last mile, Fiete 20070531
            curDistance += CopyOfRegions.getDistance(curRegion, curNb, true);

            if (curDistance < minDistance) {
              minDistance = curDistance;
              closestNbCoord = curNbCoord;
            }
          } else {
            CopyOfRegions.log.warn("Regions.getPath(): Found neighbouring region without distance: "
                + curNb + " neighbouring " + curRegion);
          }
        }

        if (closestNbCoord != null) {
          curCoord = closestNbCoord;
          curRegion = regions.get(curCoord);
          path.addFirst(curRegion);
        } else {
          CopyOfRegions.log
              .warn("Regions.getPath(): Discovered region without any distanced neighbours while backtracking");
          path.clear();

          break;
        }
      } else {
        CopyOfRegions.log
            .warn("Regions.getPath(): Discovered region without distance while backtracking: "
                + curRegion);
        path.clear();

        break;
      }
    }

    return path;
  }

  /**
   * DOCUMENT-ME
   */
  public static boolean containsHarbour(Region r, BuildingType harbour) {
    boolean harbourFound = false;

    if (harbour != null) {
      for (Iterator iter = r.buildings().iterator(); iter.hasNext();) {
        Building b = (Building) iter.next();

        if (b.getType().equals(harbour) && (b.getSize() == harbour.getMaxSize())) {
          harbourFound = true;

          break;
        }
      }
    }

    return harbourFound;
  }

  /**
   * @param ship
   * @param destination
   * @param allregions
   * @param harbour
   * @param speed The speed of the ship
   * @return
   */
  public static List<Region> planShipRoute(Ship ship, CoordinateID destination,
      Map<CoordinateID, Region> allregions, BuildingType harbour, int speed) {
    if (destination == null || allregions.get(destination)==null)
      // no path
      return null;
    Region destRegion = allregions.get(destination);
    if (ship.getRegion() == null || !allregions.containsKey(ship.getRegion().getID())) 
      // strange
      return null;
    Region startRegion = ship.getRegion();

    Map<CoordinateID, Region> harbourRegions = new Hashtable<CoordinateID, Region>();
    harbourRegions.put(startRegion.getID(), startRegion);
    harbourRegions.put(destination, destRegion);

    // Fetch all ocean-regions and all regions, that contain a harbor.
    // These are the valid one in which a path shall be searched.
    // if(oceanType != null) {
    for (Iterator iter = allregions.values().iterator(); iter.hasNext();) {
      Region r = (Region) iter.next();

      if ((r.getRegionType() != null) && r.getRegionType().isOcean()
          || CopyOfRegions.containsHarbour(r, harbour)) {
        harbourRegions.put(r.getCoordinate(), r);
      }
    }

    if (ship.getShoreId() != Direction.DIR_INVALID
        && !CopyOfRegions.containsHarbour(ship.getRegion(), harbour)) {
      // Ship cannot leave in all directions
      // try to find a path from every allowed shore-off region to the destination
      List<Region> bestPath = null;
      for (int legalShore = (ship.getShoreId() + 5) % 6; legalShore != (ship.getShoreId() + 2) % 6; legalShore =
          (legalShore + 1) % 6) {
        CoordinateID newStart =
            new CoordinateID(ship.getRegion().getCoordinate()).translate(Direction
                .toCoordinate(legalShore));
        if (!harbourRegions.containsKey(newStart) || !harbourRegions.get(newStart).getRegionType().isOcean())
          continue;
        
        List<Region> newPath =
            CopyOfRegions.getPath(harbourRegions, newStart, destination, Collections
                .<ID, RegionType> emptyMap(), Collections.singleton(harbour), speed);
        if (bestPath == null || bestPath.size() > newPath.size())
          bestPath = newPath;
      }
      LinkedList<Region> result = new LinkedList<Region>();
      result.add(startRegion);
      result.addAll(bestPath);
      return result;
    }
    
    return CopyOfRegions.getPath(harbourRegions, ship.getRegion().getCoordinate(), destination,
        Collections.<ID, RegionType> emptyMap(), Collections.singleton(harbour), speed);
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
    if (!useExtendedVersion) {
      return CopyOfRegions.getDistance(r1, r2);
    }
    // Fiete 20061123
    // for Ships...prefer Regions near coasts
    // for land units...prfer Regions with roads
    // Trick: if suitable situation, reduce the distance minimal
    double suitErg = 0.99;

    // if we have 2 Ozean regions...
    if (r1.getRegionType().isOcean() && r2.getRegionType().isOcean()) {
      if (r2.getOceanWithCoast() == 1) {
        return suitErg;
      } else {
        return erg;
      }
    }

    // if we have 2 non ozean regions....
    if (!r1.getRegionType().isOcean() && !r2.getRegionType().isOcean()) {
      if (CopyOfRegions.isCompleteRoadConnection(r1, r2)) {
        // return 2;
        return (1 + suitErg);
      } else {
        return 3;
      }
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
    CoordinateID c = CopyOfRegions.getMovement(data, ID, u.getRegion().getCoordinate(), coordinates);

    while ((c != null) && !coordinates.contains(c)) {
      coordinates.add(c);
      c = CopyOfRegions.getMovement(data, ID, c, coordinates);
    }

    Collections.reverse(coordinates);

    return coordinates;
  }

  private static CoordinateID getMovement(GameData data, String ID, CoordinateID c,
      List travelledRegions) {
    Map<CoordinateID, Region> neighbours =
        CopyOfRegions.getAllNeighbours(data.regions(), c, new Hashtable<ID, RegionType>());

    for (Iterator<Region> iter = neighbours.values().iterator(); iter.hasNext();) {
      Region r = iter.next();
      CoordinateID neighbour = r.getCoordinate();

      if (neighbour.equals(c) || travelledRegions.contains(neighbour)) {
        // dont add our own or an already visited coordinate
        continue;
      }

      if (CopyOfRegions.messagesContainsString(r.getTravelThru(), ID)
          || CopyOfRegions.messagesContainsString(r.getTravelThruShips(), ID)) {
        return neighbour;
      }
    }

    return null;
  }

  private static boolean messagesContainsString(List messages, String ID) {
    if (messages == null) {
      return false;
    }

    for (Iterator iter = messages.iterator(); iter.hasNext();) {
      Message m = (Message) iter.next();

      if (m.getText().equals(ID)) {
        return true;
      }
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
    List<Direction> directions = CopyOfRegions.getDirectionObjectsOfRegions(regions);
    Direction dir1 = directions.get(0);
    // border of r1 ->
    boolean border1OK = false;
    for (Iterator<Border> iter = r1.borders().iterator(); iter.hasNext();) {
      Border b = iter.next();
      if (magellan.library.utils.Umlaut.normalize(b.getType()).equals("STRASSE")
          && (b.getDirection() == dir1.getDir()) && b.getBuildRatio() == 100) {
        border1OK = true;
        break;
      }
    }

    if (!border1OK) {
      return false;
    }

    // r2->r1
    regions.clear();
    regions.add(r2);
    regions.add(r1);
    directions = CopyOfRegions.getDirectionObjectsOfRegions(regions);
    dir1 = directions.get(0);
    // border of r1 ->
    boolean border2OK = false;
    for (Iterator iter = r2.borders().iterator(); iter.hasNext();) {
      Border b = (Border) iter.next();
      if (magellan.library.utils.Umlaut.normalize(b.getType()).equals("STRASSE")
          && (b.getDirection() == dir1.getDir()) && b.getBuildRatio() == 100) {
        border2OK = true;
        break;
      }
    }
    if (border1OK && border2OK) {
      return true;
    }

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
    if (dx >= 0) {
      return dx + dy;
    } else if (-dx >= dy) {
      return -dx;
    } else {
      return dy;
    }
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
        if (r.getBorder(newID) == null) {
          return newID;
        }
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
    CopyOfRegions.log.info("starting calculation of coasts");
    for (Iterator<Region> iter = data.regions().values().iterator(); iter.hasNext();) {
      Region actRegion = iter.next();
      int coastBitmap = 0;
      if (actRegion.getRegionType().isOcean()) {
        // we have an ocean in front
        // the result
        CoordinateID cID = actRegion.getID();
        Map<CoordinateID, Region> n = CopyOfRegions.getAllNeighbours(data.regions(), cID, null);
        n.remove(cID);
        // checking all neighbours
        for (Iterator iter2 = n.keySet().iterator(); iter2.hasNext();) {
          CoordinateID checkID = (CoordinateID) iter2.next();
          Region checkR = n.get(checkID);
          if (!checkR.getRegionType().isOcean()) {
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
    CopyOfRegions.log.info("finished calculation of coasts, found " + cnt + " coasts.");
  }

}
