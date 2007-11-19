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

import magellan.library.Border;
import magellan.library.Building;
import magellan.library.CoordinateID;
import magellan.library.GameData;
import magellan.library.ID;
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
public class Regions {
	private static final Logger log = Logger.getInstance(Regions.class);

	/**
	 * Retrieve the regions within radius around region center.
	 *
	 * @param regions a map containing the existing regions.
	 * @param center the region the neighbours of which are retrieved.
	 * @param radius the maximum distance between center and any region to be regarded as a
	 * 		  neighbour within radius.
	 * @param excludedRegionTypes region types that disqualify regions as valid neighbours. This
	 * 		  also may be null
	 *
	 * @return a map with all neighbours that were found, including     region center. The keys are
	 * 		   instances of class ID,     values are objects of class Region.
	 *
	 * @throws IllegalArgumentException DOCUMENT-ME
	 */
	public static Map<CoordinateID,Region> getAllNeighbours(Map<CoordinateID,Region> regions, ID center, int radius, Map<ID,RegionType> excludedRegionTypes) {
		if(center instanceof CoordinateID) {
			return getAllNeighbours(regions, (CoordinateID) center, radius, excludedRegionTypes);
		} else {
			throw new IllegalArgumentException("center is not an eressea coordinate. Support for e2 incomplete!");
		}
	}

	/**
	 * Retrieve the regions within radius around region center.
	 *
	 * @param regions a map containing the existing regions.
	 * @param center the region the neighbours of which are retrieved.
	 * @param radius the maximum distance between center and any region to be regarded as a
	 * 		  neighbour within radius.
	 * @param excludedRegionTypes region types that disqualify regions as valid neighbours.
	 *
	 * @return a map with all neighbours that were found, including     region center. The keys are
	 * 		   instances of class Coordinate,     values are objects of class Region.
	 */
	private static Map<CoordinateID,Region> getAllNeighbours(Map<CoordinateID,Region> regions, CoordinateID center, int radius, Map<ID,RegionType> excludedRegionTypes) {
		Map<CoordinateID,Region> neighbours = new Hashtable<CoordinateID, Region>();
		CoordinateID c = new CoordinateID(0, 0, center.z);

		for(int dx = -radius; dx <= radius; dx++) {
			for(int dy = (-radius + Math.abs(dx)) - ((dx > 0) ? dx : 0);
					dy <= ((radius - Math.abs(dx)) - ((dx < 0) ? dx : 0)); dy++) {
				c.x = center.x + dx;
				c.y = center.y + dy;

				Region neighbour = regions.get(c);

				if(neighbour != null) {
					if((excludedRegionTypes == null) || !excludedRegionTypes.containsKey(neighbour.getType().getID())) {
						neighbours.put((CoordinateID)neighbour.getID(), neighbour);
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
	 *
	 * @return a map with all neighbours that were found, including     region center. The keys are
	 * 		   instances of class Coordinate,     values are objects of class Region.
	 */
	public static Map<CoordinateID,Region> getAllNeighbours(Map<CoordinateID,Region> regions, ID center, Map<ID,RegionType> excludedRegionTypes) {
		return getAllNeighbours(regions, center, 1, excludedRegionTypes);
	}

	/**
	 * Find a way from one region to another region and  get the directions in which to move to
	 * follow a sequence of regions. This is virtually the same as
	 * <pre>getDirections(getPath(regions, start, dest, excludedRegionTypes));</pre>
	 *
	 * 
	 * 
	 * 
	 * 
	 *
	 * @return a String telling the direction statements necessary     to follow the sequence of
	 * 		   regions contained in regions.
	 */
	public static String getDirections(Map<CoordinateID,Region> regions, ID start, ID dest, Map<ID,RegionType> excludedRegionTypes) {
		return getDirections(getPath(regions, start, dest, excludedRegionTypes));
	}

	/**
	 * Get the directions in which to move to follow a sequence of regions.
	 *
	 * @param regions an ordered consecutive sequence of regions.
	 *
	 * @return a String telling the direction statements necessary     to follow the sequence of
	 * 		   regions contained in regions.
	 */
	public static String getDirections(Collection<Region> regions) {
		if(regions == null) {
			return null;
		}

		List directions = getDirectionObjectsOfRegions(regions);

		if(directions == null) {
			return null;
		}

		StringBuffer dir = new StringBuffer();

		for(Iterator iter = directions.iterator(); iter.hasNext();) {
			Direction d = (Direction) iter.next();

			if(dir.length() > 0) {
				dir.append(" ");
			}

			dir.append(Direction.toString(d.getDir(), true));
		}

		return dir.toString();
	}

	/**
	 * DOCUMENT-ME
	 *
	 * 
	 *
	 * 
	 */
	public static List<Direction> getDirectionObjectsOfRegions(Collection<Region> regions) {
		if(regions == null) {
			return null;
		}

		List<CoordinateID> coordinates = new ArrayList<CoordinateID>(regions.size());

		for(Iterator<Region> iter = regions.iterator(); iter.hasNext();) {
			Region r = iter.next();
			coordinates.add(r.getCoordinate());
		}

		return getDirectionObjectsOfCoordinates(coordinates);
	}

	/**
	 * DOCUMENT-ME
	 *
	 * 
	 *
	 * 
	 */
	public static List<Direction> getDirectionObjectsOfCoordinates(Collection<CoordinateID> coordinates) {
		if(coordinates == null) {
			return null;
		}

		List<Direction> directions = new ArrayList<Direction>(coordinates.size());

		CoordinateID prev = null;
		CoordinateID cur = null;

		Iterator<CoordinateID> iter = coordinates.iterator();

		if(iter.hasNext()) {
			prev = iter.next();
		}

		while(iter.hasNext()) {
			cur = iter.next();

			CoordinateID diffCoord = new CoordinateID(cur.x - prev.x, cur.y - prev.y, 0);
			int intDir = Direction.toInt(diffCoord);

			if(intDir != -1) {
				directions.add(new Direction(intDir));
			} else {
				log.warn("Regions.getDirectionsOfCoordinates(): invalid direction encountered");

				return null;
			}

			prev = cur;
		}

		return directions;
	}

	/**
	 * Find a way from one region to another region.
	 *
	 * 
	 * 
	 * 
	 * 
	 *
	 * @return a Collection of regions that have to be trespassed in  order to get from the one to
	 * 		   the other specified region, including both of them.
	 *
	 * @throws IllegalArgumentException DOCUMENT-ME
	 */
	public static List<Region> getPath(Map<CoordinateID,Region> regions, ID start, ID dest, Map<ID,RegionType> excludedRegionTypes) {
		if(start instanceof CoordinateID && dest instanceof CoordinateID) {
			return getPath(regions, (CoordinateID) start, (CoordinateID) dest, excludedRegionTypes);
		} else {
			throw new IllegalArgumentException("start of dest is not an eressea coordinate. Support for e2 incomplete!");
		}
	}

	/**
	 * Find a way from one region to another region.
	 *
	 * 
	 * 
	 * 
	 * 
	 *
	 * @return a Collection of regions that have to be trespassed in  order to get from the one to
	 * 		   the other specified region, including both of them.
	 */
	private static List<Region> getPath(Map<CoordinateID,Region> regions, CoordinateID start, CoordinateID dest, Map<ID,RegionType> excludedRegionTypes) {
		if((regions == null) || (start == null) || (dest == null)) {
			log.warn("Regions.getPath(): invalid argument");

			return new LinkedList<Region>();
		}

		Map<CoordinateID,Double> distances = new Hashtable<CoordinateID, Double>();
		// distances.put(start, new Float(0.0f)); // contains the distances from the start region to all other regions as Float objects
		distances.put(start, new Double(0)); // contains the distances from the start region to all other regions as Float objects

		LinkedList<Region> path = new LinkedList<Region>();
		LinkedList<Region> backlogList = new LinkedList<Region>(); // contains regions with unknown distance to the start region
		Map<CoordinateID,Region> backlogMap = new HashMap<CoordinateID,Region>(); // contains the same entries as the backlog list. It's contents are unordered but allow a fast look-up by coordinate
		Region curRegion = null;
		CoordinateID curCoord = null;
		int consecutiveReenlistings = 0; // safe-guard against endless loops

		if(excludedRegionTypes == null) {
			excludedRegionTypes = new Hashtable<ID, RegionType>();
		}

		/* initialize the backlog list and map with the neighbours of
		   the start region */
		Map<CoordinateID,Region> initNeighbours = getAllNeighbours(regions, start, excludedRegionTypes);
		initNeighbours.remove(start);
		backlogList.addAll(initNeighbours.values());
		backlogMap.putAll(initNeighbours);

		/* first, determine the distance from the start region to all
		   other regions */
		while(!backlogList.isEmpty()) {
			/* in this loop the backlog list contains all regions with
			   unkown distance to start */

			/* take the first region from the backlog list */
			curRegion = (Region) backlogList.getFirst();
			curCoord = curRegion.getCoordinate();

			/* safety checks */
			if(excludedRegionTypes.containsKey(curRegion.getType().getID())) {
				log.warn("Regions.getPath(): Found an region of type " +
						 curRegion.getType().getName() +
						 " in region list! Removing and ignoring it.");
				backlogList.removeFirst();
				backlogMap.remove(curCoord);

				continue;
			}

			if(distances.containsKey(curCoord)) {
				log.warn("Regions.getPath(): Found a region with known distance in region list! Removing and ignoring it.");
				backlogList.removeFirst();
				backlogMap.remove(curCoord);

				continue;
			}

			/* determine all neighbours of the current region taken
			   from the backlog list */
			// float minDistance = Float.MAX_VALUE;
			double minDistance = Double.MAX_VALUE;
			Map<CoordinateID,Region> neighbours = getAllNeighbours(regions, curCoord, excludedRegionTypes);
			neighbours.remove(curCoord);

			/* now determine the distance from the start region to the
			   current region taken from the backlog list by checking
			   its neighbour's distances to the start region */
			for(Iterator<Region> iter = neighbours.values().iterator(); iter.hasNext();) {
				Region curNb = iter.next();
				CoordinateID curNbCoord = curNb.getCoordinate();
				// Float dist = (Float) distances.get(curNbCoord);
				Double dist = (Double) distances.get(curNbCoord);
				if(dist != null) {
					/* we know the distance from the start region to
					   this neighbour, so we can determine the
					   distance from the start region to the current
					   region taken from the backlog list */
					// float curDistance = getDistance(curNb, curRegion) + dist.floatValue();
					double curDistance = getDistance(curNb, curRegion,true) + dist.floatValue();

					if(curDistance < minDistance) {
						minDistance = curDistance;
					}
				} else {
					/* we do not know the distance from the start
					   region to this neighbour, so we store this
					   neighbour in the backlog list */
					if(!backlogMap.containsKey(curNbCoord)) {
						backlogList.add(curNb);
						backlogMap.put(curNbCoord, null);
					}
				}
			}

			/* If we could determine the distance from the start
			   region to the current region taken from the backlog
			   list, we can remove it from that list and record the
			   distance */
			if(minDistance < Double.MAX_VALUE) {
				consecutiveReenlistings = 0;
				backlogList.removeFirst();
				backlogMap.remove(curCoord);
				distances.put(curCoord, new Double(minDistance));
			} else {
				backlogList.removeFirst();

				if(!distances.containsKey(curCoord)) {
					backlogList.addLast(curRegion);
					consecutiveReenlistings++;

					if(consecutiveReenlistings > backlogList.size()) {
						log.warn("Regions.getPath(): looks like an endless loop. Exiting.");

						break;
					}
				} else {
					log.warn("Regions.getPath(): Found a region with known distance in backlog list: " +
							 curRegion);
				}
			}
		}

		// backtracking
		/* now we know the distance of each region to the start region
		   but we do not know a shortest path. We can find one simply
		   by starting at the destination region, looking at its
		   neighbours and choosing the one with the smallest distance
		   to the start region until we reach the start region. This
		   sequence of regions is the reverse shortest path. */
		curRegion = regions.get(dest);
		curCoord = dest;
		path.add(curRegion);

		while((curRegion != null) && (curCoord != null) && !curCoord.equals(start)) {
			Double dist = (Double) distances.get(curCoord);

			if(dist != null) {
        // double minDistance = dist.doubleValue();
        // now add the last mile at minimum dist
        double minDistance = dist.doubleValue() + 1;
        
				CoordinateID closestNbCoord = null;
				Map neighbours = getAllNeighbours(regions, curCoord, excludedRegionTypes);
				neighbours.remove(curCoord);

				for(Iterator iter = neighbours.values().iterator(); iter.hasNext();) {
					Region curNb = (Region) iter.next();
					CoordinateID curNbCoord = curNb.getCoordinate();
					Double nbDist = (Double) distances.get(curNbCoord);

					if(nbDist != null) {
						double curDistance = nbDist.doubleValue();
            
            // add the last mile, Fiete 20070531
            curDistance += getDistance(curRegion, curNb, true);

						if(curDistance < minDistance) {
							minDistance = curDistance;
							closestNbCoord = curNbCoord;
						}
					} else {
						log.warn("Regions.getPath(): Found neighbouring region without distance: " +
								 curNb + " neighbouring " + curRegion);
					}
				}

				if(closestNbCoord != null) {
					curCoord = closestNbCoord;
					curRegion = (Region) regions.get(curCoord);
					path.addFirst(curRegion);
				} else {
					log.warn("Regions.getPath(): Discovered region without any distanced neighbours while backtracking");
					path.clear();

					break;
				}
			} else {
				log.warn("Regions.getPath(): Discovered region without distance while backtracking");
				path.clear();

				break;
			}
		}

		return path;
	}

	/**
	 * DOCUMENT-ME
	 *
	 * 
	 * 
	 *
	 * 
	 */
	public static boolean containsHarbour(Region r, BuildingType harbour) {
		boolean harbourFound = false;

		if(harbour != null) {
			for(Iterator iter = r.buildings().iterator(); iter.hasNext();) {
				Building b = (Building) iter.next();

				if(b.getType().equals(harbour) && (b.getSize() == harbour.getMaxSize())) {
					harbourFound = true;

					break;
				}
			}
		}

		return harbourFound;
	}

	/**
	 * DOCUMENT-ME
	 *
	 * 
	 * 
	 * 
	 * 
	 * 
	 *
	 * 
	 */
	public static List<Region> planShipRoute(Ship ship, CoordinateID destination, Map<CoordinateID,Region> allregions, BuildingType harbour, int speedBonus) {
		if(destination != null) {
			Map<CoordinateID,Region> regions = new Hashtable<CoordinateID, Region>();
			Map<CoordinateID,Region> harbourRegions = new Hashtable<CoordinateID, Region>();

			// Fetch all ocean-regions and all regions, that contain a harbour.
			// These are the valid one in which a path shall be searched.
			//if(oceanType != null) {
			for(Iterator iter = allregions.values().iterator(); iter.hasNext();) {
				Region r = (Region) iter.next();

				if((r.getRegionType() != null) && r.getRegionType().isOcean()) {
					regions.put(r.getCoordinate(), r);
				} else if(containsHarbour(r, harbour)) {
					harbourRegions.put(r.getCoordinate(), r);
				}
			}

			//}
			// Add destination region:
			Region destRegion = allregions.get(destination);

			if(destRegion != null) {
				regions.put(destination, destRegion);
			} else {
				// FF 20061231
				// if dest is null...for sure no path can be found..
				return null;
			}

			// determine the possible ways off (ships casually can not leave
			// land to all directions)
			List<Region> startregions = new LinkedList<Region>();
			int shipRange = ship.getShipType().getRange();
			shipRange += speedBonus;

			Region curRegion = ship.getRegion();

			if((ship.getShoreId() == Direction.DIR_INVALID) || containsHarbour(curRegion, harbour)) {
				// Ship can leave in all directions
				startregions.add(curRegion);
				regions.put(curRegion.getCoordinate(), curRegion);
			} else {
				/*
				 * Ship can not leave in all directions.
				 * Idea: All possibilities to leave land are evaluated and
				 * for all of them a single path is calculated. The shortest of
				 * these paths will be taken and the way from the land to that
				 * oceanregion will be added.
				 */
				CoordinateID c = null;
				Region r = null;

				// central direction
				int shoreID = ship.getShoreId();
				c = Direction.toCoordinate(shoreID);
				c.x += curRegion.getCoordinate().x;
				c.y += curRegion.getCoordinate().y;
				r = (Region) allregions.get(c);

				if((r != null) && (r.getRegionType().isOcean() || containsHarbour(r, harbour))) {
					startregions.add(r);
					regions.put(r.getCoordinate(), r);
				}

				// left neighbour
				shoreID--;

				if(shoreID == -1) {
					shoreID = 5;
				}

				c = Direction.toCoordinate(shoreID);
				c.x += curRegion.getCoordinate().x;
				c.y += curRegion.getCoordinate().y;
				r = (Region) allregions.get(c);

				if((r != null) && (r.getRegionType().isOcean() || containsHarbour(r, harbour))) {
					startregions.add(r);
					regions.put(r.getCoordinate(), r);
				}

				// right neighbour
				shoreID = (shoreID + 2) % 6;
				c = Direction.toCoordinate(shoreID);
				c.x += curRegion.getCoordinate().x;
				c.y += curRegion.getCoordinate().y;
				r = (Region) allregions.get(c);

				if((r != null) && (r.getRegionType().isOcean() || containsHarbour(r, harbour))) {
					startregions.add(r);
					regions.put(r.getCoordinate(), r);
				}
			}

			/*
			 * Now determine the several paths for the (possible) multiple
			 * ways of leaving land. There's another hazard. Harbours can be used
			 * as canals but they can not be crossed in one game round. (Your reach them
			 * in one and leave them in the next instead. So first a way without using
			 * harbours is searched and then another considering them. Then they are compared...
			 */
			List<List<Region>> paths = new LinkedList<List<Region>>();
			harbourRegions.putAll(regions);

			for(Iterator<Region> iter = startregions.iterator(); iter.hasNext();) {
				Region startregion = iter.next();
				List<Region> path = Regions.getPath(regions, startregion.getCoordinate(), destination,
											new Hashtable<ID, RegionType>());

				if((path != null) && (path.size() > 0)) {
					paths.add(path);
				}

				List<Region> pathWithHarbours = Regions.getPath(harbourRegions,
														startregion.getCoordinate(), destination,
														new Hashtable<ID, RegionType>());

				if((pathWithHarbours != null) && (pathWithHarbours.size() > 0) &&
					   !pathWithHarbours.equals(path)) {
					paths.add(pathWithHarbours);
				}
			}

			// search for shortest path (only if more than one path found)
			int minpos = 0;

			if(paths.size() > 1) {
				int curpos = 0;
				int minweeks = Integer.MAX_VALUE;

				for(Iterator iter = paths.iterator(); iter.hasNext(); curpos++) {
					List path = (List) iter.next();

					// determine path size considering ship-range
					// don't count regions due to harbours!
					int weeks = -1;

					if(shipRange > 0) {
						int counter = shipRange;
						weeks = 1;

						for(Iterator i = path.iterator(); i.hasNext(); counter--) {
							if(counter == 0) {
								counter = shipRange;
								weeks++;
							} else if(containsHarbour((Region) i.next(), harbour)) {
								counter = shipRange;
								weeks++;
							}
						}
					}

					if(weeks < minweeks) {
						minweeks = weeks;
						minpos = curpos;
					} else if((weeks == minweeks) &&
								  ((paths.get(minpos)).size() > path.size())) {
						minpos = curpos;
					}
				}
			}

			if(paths.size() > 0) {
				List<Region> path = paths.get(minpos);

				if(!path.get(0).equals(curRegion)) {
					path.add(0, curRegion);
				}

				return path;
			}
		}

		return null;
	}

	private static double getDistance(Region r1, Region r2) {
		return 1;
	}
	
	/**
	 * delivers a distance between 2 regions
	 * we asume, that both regions are neighbours, so trivial distance is 1
	 * for oceans, we deliver for landnearregions a significant smaller value
	 * for landregions we calculate a new distance...as like moveoints
	 * with propper roads: 2, without: 3
	 * @param r1
	 * @param r2
	 * @param useExtendedVersion
	 * @return
	 */
	private static double getDistance(Region r1, Region r2,boolean useExtendedVersion){
		double erg = 1;
		if (!useExtendedVersion) {
			return getDistance(r1, r2);
		}
		// Fiete 20061123
		// for Ships...prefer Regions near coasts
		// for land units...prfer Regions with roads
		// Trick: if suitable situation, reduce the distance minimal 
		double suitErg = 0.999999999999999;
		
		// if we have 2 Ozean regions...
		if (r1.getRegionType().isOcean() && r2.getRegionType().isOcean()) {
			if (r2.getOzeanWithCoast()==1){
				return suitErg;
			} else {
				return erg;
			}
		}
		
		// if we have 2 non ozean regions....
		if (!r1.getRegionType().isOcean() && !r2.getRegionType().isOcean()) {
			if (isCompleteRoadConnection(r1, r2)){
				return 2;
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
	 * 
	 *
	 * @return a List of Coordinate objects of the path the unit used (although evaluated via
	 * 		   backtracking) from start to end.
	 */
	public static List<CoordinateID> getMovement(GameData data, Unit u) {
		List<CoordinateID> coordinates = new ArrayList<CoordinateID>(2);

		// first of all add current coordinate
		coordinates.add((CoordinateID)u.getRegion().getID());

		// we need a string which is useable for travelThru AND travelThruShip
		String ID = (u.getShip() == null) ? u.toString() : u.getShip().toString(false);

		// run over neighbours recursively
		CoordinateID c = getMovement(data, ID, u.getRegion().getCoordinate(), coordinates);

		while((c != null) && !coordinates.contains(c)) {
			coordinates.add(c);
			c = getMovement(data, ID, c, coordinates);
		}

		Collections.reverse(coordinates);

		return coordinates;
	}

	private static CoordinateID getMovement(GameData data, String ID, CoordinateID c,
										  List travelledRegions) {
		Map<CoordinateID,Region> neighbours = getAllNeighbours(data.regions(), c, new Hashtable<ID,RegionType>());

		for(Iterator<Region> iter = neighbours.values().iterator(); iter.hasNext();) {
			Region r = iter.next();
			CoordinateID neighbour = r.getCoordinate();

			if(neighbour.equals(c) || travelledRegions.contains(neighbour)) {
				// dont add our own or an already visited coordinate
				continue;
			}

			if(messagesContainsString(r.getTravelThru(), ID) ||
				   messagesContainsString(r.getTravelThruShips(), ID)) {
				return neighbour;
			}
		}

		return null;
	}

	private static boolean messagesContainsString(List messages, String ID) {
		if(messages == null) {
			return false;
		}

		for(Iterator iter = messages.iterator(); iter.hasNext();) {
			Message m = (Message) iter.next();

			if(m.getText().equals(ID)) {
				return true;
			}
		}

		return false;
	}

	/**
	 * Returns a map of all RegionTypes that are flagged as <tt>ocean</tt>.
	 *
	 * @param rules Rules of the game
	 *
	 * @return map of all ocean RegionTypes
	 */
	public static Map<ID,RegionType> getOceanRegionTypes(Rules rules) {
		Map<ID,RegionType> ret = new Hashtable<ID,RegionType>();

		for(Iterator<RegionType> iter = rules.getRegionTypeIterator(); iter.hasNext();) {
			RegionType rt = iter.next();

			if(rt.isOcean()) {
				ret.put(rt.getID(), rt);
			}
		}

		return ret;
	}
	
	/**
	 * Returns the RegionType that is named as <tt>Feuerwand</tt>.
	 * @author Fiete
	 * 
	 * @param rules Rules of the game
	 * @param data GameDate - needed to find Translation
	 *
	 * @return RegionType Feuerwand
	 */
	public static RegionType getFeuerwandRegionType(Rules rules, GameData data){
		String actFeuerwandName = "Feuerwand";
		// TODO:we do not need a translation here (FF)!
		if (data != null) {
			actFeuerwandName = data.getTranslation("Feuerwand");
		}
		return rules.getRegionType(StringID.create(actFeuerwandName));	
	}
	
	/**
	 * returns true, if a working road connection is established between r1 and r2
	 * we assume, both regions are neighbours
	 * @param r1 a region
	 * @param r2 another region
	 * @return
	 */
	public static boolean isCompleteRoadConnection(Region r1,Region r2){
		boolean erg = false;
		// Collection of Regions
		// r1 -> r2
		List<Region> regions = new ArrayList<Region>(2);
		regions.add(r1);
		regions.add(r2);
		// generate List of directions
		List<Direction> directions = getDirectionObjectsOfRegions(regions);
		Direction dir1 = directions.get(0);
		// border of r1 -> 
		boolean border1OK = false;
		for(Iterator<Border> iter = r1.borders().iterator(); iter.hasNext();) {
			Border b = iter.next();
			if(magellan.library.utils.Umlaut.normalize(b.getType()).equals("STRASSE") &&
           (b.getDirection() == dir1.getDir())
            && b.getBuildRatio()==100 ) {
				border1OK = true;
				break;
			}
		}
		
		if (!border1OK){return false;}
		
		// r2->r1
		regions.clear();
		regions.add(r2);
		regions.add(r1);
		directions = getDirectionObjectsOfRegions(regions);
		dir1 = (Direction)directions.get(0);
		// border of r1 -> 
		boolean border2OK = false;
		for(Iterator iter = r2.borders().iterator(); iter.hasNext();) {
			Border b = (Border) iter.next();
			if(magellan.library.utils.Umlaut.normalize(b.getType()).equals("STRASSE") &&
          (b.getDirection() == dir1.getDir())
          && b.getBuildRatio()==100 ) {
				border2OK = true;
				break;
			}
		}
		if (border1OK && border2OK){
			return true;
		}
		
		return erg;
	}
	
	
	
}
