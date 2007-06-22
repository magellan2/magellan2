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

import java.util.Collection;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;

import magellan.library.CoordinateID;
import magellan.library.GameData;
import magellan.library.ID;
import magellan.library.IntegerID;
import magellan.library.Island;
import magellan.library.Region;
import magellan.library.Rules;
import magellan.library.rules.RegionType;
import magellan.library.utils.logging.Logger;


/**
 * A class offering common operations on islands and regions.
 */
public class Islands {
	private static final Logger log = Logger.getInstance(Islands.class);

	/**
	 * Retrieve all islands formed by a collection of regions.
	 *
	 * @param rules the rules to retrieve the ocean region type from.
	 * @param regions the regions the islands consist of.
	 * @param islands islands contained in this map are regarded as already existing. They can be
	 * 		  expanded or merged, depending on the regions supplied. To indicate that no islands
	 * 		  are known supply an empty map or null.
	 * @param data the game data as required by the Island constructor.
	 *
	 * 
	 */
	public static Map<ID,Island> getIslands(Rules rules, Map<CoordinateID,Region> regions, Map<ID,Island> islands, GameData data) {
		if((regions == null) || (regions.size() == 0)) {
			return new Hashtable<ID, Island>();
		}

		if(islands == null) {
			islands = new Hashtable<ID,Island>();
		}

		Map<CoordinateID,Region> unassignedPool = new Hashtable<CoordinateID,Region>();
		unassignedPool.putAll(regions);

		// completely update all known islands
		for(Iterator<Island> iter = islands.values().iterator(); iter.hasNext();) {
			Island curIsland = iter.next();
			Collection oldRegions = curIsland.regions();

			if(oldRegions.size() > 0) {
				Map<CoordinateID,Region> islandRegions = getIsland(rules, unassignedPool, (Region) oldRegions.iterator().next());

				for(Iterator<Region> it = islandRegions.values().iterator(); it.hasNext();) {
					Region curRegion = it.next();
					curRegion.setIsland(curIsland);
					unassignedPool.remove(curRegion.getID());
				}
			}
		}

		// assign new islands to remaining regions
		IntegerID newID = IntegerID.create(0);

		while(unassignedPool.size() > 0) {
			Region curRegion = unassignedPool.remove(unassignedPool.keySet().iterator().next());
			Map<CoordinateID,Region> islandRegions = getIsland(rules, unassignedPool, curRegion);

			if(islandRegions.size() > 0) {
				while(islands.containsKey(newID)) {
					newID = IntegerID.create(newID.intValue() + 1);
				}

				Island curIsland = MagellanFactory.createIsland(newID, data);
				curIsland.setName(newID.toString());
				islands.put(newID, curIsland);

				for(Iterator<Region> it = islandRegions.values().iterator(); it.hasNext();) {
					Region ir = it.next();
					ir.setIsland(curIsland);
					unassignedPool.remove(ir.getID());
				}
			}
		}

		return islands;
	}

	/**
	 * Get all regions belonging the same island as the region r.
	 *
	 * 
	 * @param regions all regions that could possibly belong to the island.
	 * @param r a region forming an island with its neighbouring regions.
	 *
	 * @return a map containing all regions that can be reached from region r via any number of
	 * 		   regions that are not of type ocean.
	 */
	public static Map<CoordinateID,Region> getIsland(Rules rules, Map<CoordinateID,Region> regions, Region r) {
		Map<CoordinateID,Region> checked = new Hashtable<CoordinateID, Region>();

		Map<ID, RegionType> excludedRegionTypes = Regions.getOceanRegionTypes(rules);

		// Feature wish: Feuerwände nicht dabei (Fiete)
		
		RegionType feuerwand = Regions.getFeuerwandRegionType(rules,r.getData());
		if (feuerwand != null) {
			excludedRegionTypes.put(feuerwand.getID(),feuerwand);
		}
		if(excludedRegionTypes.isEmpty()) {
			log.warn("Islands.getIsland(): unable to determine ocean region types!");

			return null;
		}

		Map<CoordinateID,Region> unchecked = new Hashtable<CoordinateID, Region>();

		if(!r.getRegionType().isOcean()) {
			unchecked.put((CoordinateID)r.getID(), r);
		}

		while(unchecked.size() > 0) {
			Region currentRegion = unchecked.remove(unchecked.keySet().iterator().next());
			checked.put((CoordinateID)currentRegion.getID(), currentRegion);

			Map<CoordinateID,Region> neighbours = Regions.getAllNeighbours(regions, currentRegion.getID(), 1, excludedRegionTypes);

			for(Iterator<Region> iter = neighbours.values().iterator(); iter.hasNext();) {
				Region neighbour = iter.next();

				if(!checked.containsKey(neighbour.getID())) {
					unchecked.put((CoordinateID)neighbour.getID(), neighbour);
				}
			}
		}

		return checked;
	}
}
