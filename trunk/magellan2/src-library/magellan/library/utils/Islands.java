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
import java.util.HashMap;
import java.util.Hashtable;
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
  @SuppressWarnings("unused")
  private static final Logger log = Logger.getInstance(Islands.class);

  /**
   * Retrieve all islands formed by a collection of regions.
   * 
   * @param rules the rules to retrieve the ocean region type from.
   * @param regions the regions the islands consist of.
   * @param oldIslands islands contained in this map are regarded as already existing. They can be
   *          expanded or merged, depending on the regions supplied. To indicate that no islands are
   *          known supply an empty map or null.
   * @param data the game data as required by the Island constructor.
   */
  public static Map<IntegerID, Island> getIslands(UserInterface ui, Rules rules,
      Map<CoordinateID, Region> regions, Map<IntegerID, Island> oldIslands, GameData data) {
    if ((regions == null) || (regions.size() == 0))
      return new Hashtable<IntegerID, Island>();

    Map<IntegerID, Island> islands;
    if (oldIslands == null) {
      islands = new HashMap<IntegerID, Island>();
    } else {
      islands = new HashMap<IntegerID, Island>(oldIslands);
    }

    if (ui == null) {
      ui = new NullUserInterface();
    }
    ui.setTitle(Resources.get("progressdialog.islands.title"));
    ui.setMaximum(islands.size());
    ui.show();
    int counter = 0;

    Map<CoordinateID, Region> unassignedPool = new Hashtable<CoordinateID, Region>();
    unassignedPool.putAll(regions);

    // completely update all known islands
    for (Island curIsland : islands.values()) {
      ui.setProgress(Resources.get("progressdialog.islands.step01"), ++counter);

      Collection<Region> oldRegions = curIsland.regions();

      if (oldRegions.size() > 0) {
        Map<CoordinateID, ? extends Region> islandRegions =
            Islands.getIsland(rules, unassignedPool, oldRegions.iterator().next());

        for (Region curRegion : islandRegions.values()) {
          curRegion.setIsland(curIsland);
          unassignedPool.remove(curRegion.getID());
        }
      }
    }

    ui.setProgress("", 0);
    ui.setMaximum(unassignedPool.size());
    counter = 0;

    // assign new islands to remaining regions
    IntegerID newID = IntegerID.create(0);

    while (unassignedPool.size() > 0) {
      ui.setProgress(Resources.get("progressdialog.islands.step02"), ++counter);

      Region curRegion = unassignedPool.remove(unassignedPool.keySet().iterator().next());
      Map<CoordinateID, ? extends Region> islandRegions =
          Islands.getIsland(rules, unassignedPool, curRegion);

      if (islandRegions.size() > 0) {
        while (islands.containsKey(newID)) {
          newID = IntegerID.create(newID.intValue() + 1);
        }

        Island curIsland = MagellanFactory.createIsland(newID, data);
        curIsland.setName(newID.toString());
        islands.put(newID, curIsland);

        for (Region ir : islandRegions.values()) {
          ir.setIsland(curIsland);
          unassignedPool.remove(ir.getID());
        }
      }
    }

    ui.ready();

    return islands;
  }

  /**
   * Get all regions belonging the same island as the region r. Uses all the "land regions" from the
   * rule.
   * 
   * @param regions All regions that could possibly belong to the island.
   * @param r A region forming an island with its neighbouring regions.
   * @return a map containing all regions that can be reached from region r via allowed land region
   *         types
   */
  public static Map<CoordinateID, Region> getIsland(Rules rules, Map<CoordinateID, Region> regions,
      Region r) {
    return getIsland(rules, regions, r, null, true);
  }

  /**
   * Get all regions belonging the same island as the region r.
   * 
   * @param regions All regions that could possibly belong to the island.
   * @param r A region forming an island with its neighbouring regions.
   * @param excludedRegionTypes Additional region types which should not be included (e.g.
   *          Feuerwand)
   * @return a map containing all regions that can be reached from region r via allowed region types
   * @deprecated Use {@link #getIsland(Rules, Map, Region, Map)} or
   *             {@link #getIsland(Rules, Map, Region, Map, boolean)}.
   */
  @Deprecated
  public static Map<CoordinateID, Region> getIsland(Rules rules, Map<CoordinateID, Region> regions,
      Region r, Map<ID, RegionType> excludedRegionTypes) {
    return getIsland(rules, regions, r, excludedRegionTypes, true);
  }

  /**
   * Get all regions belonging the same island as the region r.
   * 
   * @param regions all regions that could possibly belong to the island.
   * @param r a region forming an island with its neighbouring regions.
   * @param excludedRegionTypes additional regiontypes which should not be included (e.g. Feuerwand)
   * @param onlyLand if true, all region types from {@link Regions#getNonLandRegionTypes(Rules)}
   *          will be excluded.
   * @return a map containing all regions that can be reached from region r via allowed region types
   */
  public static Map<CoordinateID, Region> getIsland(Rules rules, Map<CoordinateID, Region> regions,
      Region r, Map<ID, RegionType> excludedRegionTypes, boolean onlyLand) {
    Map<CoordinateID, Region> checked = new Hashtable<CoordinateID, Region>();

    Map<ID, RegionType> allExcludedTypes =
        onlyLand ? new HashMap<ID, RegionType>(Regions.getNonLandRegionTypes(rules))
            : new HashMap<ID, RegionType>();
    if (excludedRegionTypes != null) {
      allExcludedTypes.putAll(excludedRegionTypes);
    }

    Map<CoordinateID, Region> unchecked = new Hashtable<CoordinateID, Region>();

    if (!allExcludedTypes.containsValue(r.getRegionType())) {
      unchecked.put(r.getID(), r);
    }

    while (unchecked.size() > 0) {
      Region currentRegion = unchecked.remove(unchecked.keySet().iterator().next());
      checked.put(currentRegion.getID(), currentRegion);

      for (Region neighbour : currentRegion.getNeighbors().values()) {
        if (!checked.containsKey(neighbour.getID())
            && !allExcludedTypes.containsKey(neighbour.getType().getID())) {
          unchecked.put(neighbour.getID(), neighbour);
        }
      }
    }

    return checked;
  }
}
