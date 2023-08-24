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
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import magellan.library.CoordinateID;
import magellan.library.GameData;
import magellan.library.ID;
import magellan.library.IntegerID;
import magellan.library.Island;
import magellan.library.Region;

/**
 * DOCUMENT-ME
 * 
 * @author $Author: $
 * @version $Revision: 305 $
 */
public class MagellanIslandImpl extends MagellanDescribedImpl implements Island {
  /** The game data required to construct the collection of regions belonging to this island. */
  private GameData data = null;
  /** Contains all attributes */
  private Map<String, String> attributes = new LinkedHashMap<String, String>();

  /**
   * Constructs a new Island object uniquely identifiable by the specified id.
   * 
   * @param id allows this island to return all region objects that belong to it via the
   *          <kbd>regions()</kbd> and <kbd>getRegion()</kbd> methods.
   */
  public MagellanIslandImpl(IntegerID id, GameData data) {
    super(id);
    this.data = data;
  }

  /**
   * Returns a String representation of this Island object.
   */
  @Override
  public String toString() {
    return getName() + " (ID: " + id + ")";
  }

  /**
   * Contains all regions that are in this region. This map is updated lazily, i.e. this container
   * is notified every time an object is added or removed and it refreshes that map only when it is
   * accessed from outside. This strategy has two major advantages over one drawback. On the one
   * hand, it reduces memory consumption, as the map is only allocated with data when acually
   * needed. Since the map is used like a cache, the computational overhead is acceptable.
   * Furthermore, the complexity of merging to game data objects is significantly reduced because
   * there are no more consistency issues between such maps and the contained objects referring to
   * this container. On the other hand, there is basically no code re-use, which could be realized
   * through inheritance (not applicable here) or by using a generic data structure, which itself
   * has again the disadvantage of lacking type safety. Also, without wrapping it there is again the
   * problem of either having to deal with null values outside this class or increased memory
   * consumption by always allocating this data structure.
   */
  private Map<CoordinateID, Region> regions = null;

  /** Indicates that the current container map has to be refreshed. */
  private boolean regionsInvalidated = true;

  /**
   * Returns an unmodifiable collection of all the regions in this container.
   */
  public Collection<Region> regions() {
    if (regionsInvalidated) {
      refreshRegions();
    }

    if (regions != null && regions.values() != null)
      return Collections.unmodifiableCollection(regions.values());
    return Collections.emptyList();
  }

  /**
   * Retrieve a region in this container by id.
   */
  public Region getRegion(ID key) {
    if (regionsInvalidated) {
      refreshRegions();
    }

    if (regions != null)
      return regions.get(key);
    else
      return null;
  }

  /**
   * Informs this container that a region was added or removed. It is mandatory that this function
   * is called every time a region is added or removed from this container for keeping the objects
   * returned by the getRegion() and regions() methods consistent.
   */
  public void invalidateRegions() {
    regionsInvalidated = true;
  }

  /**
   * Recreates the map of regions in this container. This function is called every time the
   * collection of regions is accessed and the regionsInvalidated variable is true.
   */
  private void refreshRegions() {
    if (regions == null) {
      regions = new LinkedHashMap<CoordinateID, Region>();
    } else {
      regions.clear();
    }

    if (data.getRegions() != null) {
      for (Region r : data.getRegions()) {
        if (equals(r.getIsland())) {
          regions.put(r.getID(), r);
        }
      }
    }

    regionsInvalidated = false;
  }

  /**
   * Sets the Map of regions. This shall solely called by GameData.postProcess.
   */
  public void setRegions(Map<CoordinateID, Region> r) {
    regions = r;
    regionsInvalidated = false;
  }

  /**
   * @see magellan.library.Addeable#addAttribute(java.lang.String, java.lang.String)
   */
  public void addAttribute(String key, String value) {
    attributes.put(key, value);
  }

  /**
   * @see magellan.library.Addeable#containsAttribute(java.lang.String)
   */
  public boolean containsAttribute(String key) {
    return attributes.containsKey(key);
  }

  /**
   * @see magellan.library.Addeable#getAttribute(java.lang.String)
   */
  public String getAttribute(String key) {
    return attributes.get(key);
  }

  /**
   * @see magellan.library.Addeable#getAttributeKeys()
   */
  public List<String> getAttributeKeys() {
    return new ArrayList<String>(attributes.keySet());
  }

  /**
   * @see magellan.library.Addeable#getAttributeSize()
   */
  public int getAttributeSize() {
    return attributes.size();
  }

  /**
   * @see magellan.library.Identifiable#getID()
   */
  @Override
  public IntegerID getID() {
    return (IntegerID) super.getID();
  }
}
