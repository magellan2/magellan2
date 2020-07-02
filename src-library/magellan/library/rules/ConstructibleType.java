// class magellan.library.rules.ConstructibleType
// created on Jun 27, 2009
//
// Copyright 2003-2009 by magellan project team
//
// Author : $Author: $
// $Id: $
//
// This program is free software; you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation; either version 2 of the License, or
// (at your option) any later version.
// 
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
// GNU General Public License for more details.
// 
// You should have received a copy of the GNU General Public License
// along with this program (see doc/LICENCE.txt); if not, write to the
// Free Software Foundation, Inc.,
// 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
// 
package magellan.library.rules;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

import magellan.library.ID;
import magellan.library.Item;
import magellan.library.StringID;

/**
 * A type abstracting a unit container with building and maintenance costs.
 * 
 * @author stm
 */
public abstract class ConstructibleType extends UnitContainerType {

  private int minSkillLevel = -1;
  private int maxSize = -1;
  private Map<StringID, Item> rawMaterials = null;
  private Map<StringID, Item> maintenance = null;

  public ConstructibleType(StringID id) {
    super(id);
  }

  /**
   * Add an item needed to construct one size point of this type.
   */
  public void addRawMaterial(Item i) {
    if (rawMaterials == null) {
      rawMaterials = new LinkedHashMap<StringID, Item>();
    }

    rawMaterials.put(i.getItemType().getID(), i);
  }

  /**
   * Returns a collection of all items needed to construct one size point of this type.
   */
  public Collection<Item> getRawMaterials() {
    if (rawMaterials != null && rawMaterials.values() != null)
      return Collections.unmodifiableCollection(rawMaterials.values());
    return Collections.emptyList();
  }

  /**
   * Returns an item which contains how much is needed to build this type <code>null</code> if this
   * type isn't needed
   * 
   * @param id An ItemType ID
   */
  public Item getRawMaterial(StringID id) {
    if (rawMaterials != null)
      return rawMaterials.get(id);
    else
      return null;
  }

  /**
   * Adds an item needed for upkeep.
   */
  public void addMaintenance(Item i) {
    if (maintenance == null) {
      maintenance = new LinkedHashMap<StringID, Item>();
    }

    maintenance.put(i.getItemType().getID(), i);
  }

  /**
   * Returns a collections of all items neede for upkeep.
   */
  public Collection<Item> getMaintenanceItems() {
    if (maintenance != null && maintenance.values() != null)
      return Collections.unmodifiableCollection(maintenance.values());
    return Collections.emptyList();
  }

  /**
   * Returns an item which contains how much is needed to upkeep this type or <code>null</code> if
   * this type isn't needed
   * 
   * @param id An ItemType ID
   */
  public Item getMaintenance(ID id) {
    if (maintenance != null)
      return maintenance.get(id);
    else
      return null;
  }

  /**
   * Sets the minimum level to construct one unit of this type.
   */
  public void setBuildSkillLevel(int l) {
    minSkillLevel = l;
  }

  /**
   * Returns the minimum level to construct one unit of this type.
   */
  public int getBuildSkillLevel() {
    return minSkillLevel;
  }

  /**
   * Sets the maximum size. A value &lt; 0 means that there is no maximum.
   */
  public void setMaxSize(int m) {
    maxSize = m;
  }

  /**
   * Returns the maximum size. A value &lt; 0 means that there is no maximum.
   */
  public int getMaxSize() {
    return maxSize;
  }

  /**
   * Returns the id uniquely identifying this object.
   */
  @Override
  public StringID getID() {
    return (StringID) id;
  }

}