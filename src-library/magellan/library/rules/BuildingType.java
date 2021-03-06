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

package magellan.library.rules;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

import magellan.library.StringID;

/**
 * Stores attributes for a type of building.
 *
 * @author $Author: $
 * @version $Revision: 203 $
 */
public class BuildingType extends ConstructibleType {
  private Map<StringID, Integer> skillBonuses = null;
  private Map<StringID, RegionType> regionTypes = null;
  private boolean maintenedByRegionOwner;

  /**
   * Creates a new BuildingType object.
   */
  public BuildingType(StringID id) {
    super(id);
  }

  /**
   * Returns the bonus this building provides to the given skill.
   */
  public int getSkillBonus(SkillType skillType) {
    int bonus = 0;

    if (skillBonuses != null) {
      Integer i = skillBonuses.get(skillType.getID());

      if (i != null) {
        bonus = i.intValue();
      }
    }

    return bonus;
  }

  /**
   * Sets the bonus this building provides to the given skill.
   */
  public void setSkillBonus(SkillType skillType, int bonus) {
    if (skillBonuses == null) {
      skillBonuses = new LinkedHashMap<StringID, Integer>();
    }

    skillBonuses.put(skillType.getID(), Integer.valueOf(bonus));
  }

  /**
   * Registers type as allowed region type for this building.
   *
   * @param type
   */
  public void addRegionType(RegionType type) {
    if (regionTypes == null) {
      regionTypes = new LinkedHashMap<StringID, RegionType>();
    }

    regionTypes.put(type.getID(), type);
  }

  /**
   * Returns <code>true</code> if either no RegionType has been registered by
   * {@link #addRegionType(RegionType)} or if at least one RegionType has been registered and type
   * is one of the allowed types.
   *
   * @param type
   */
  public boolean containsRegionType(RegionType type) {
    return (regionTypes == null) || regionTypes.containsKey(type.getID());
  }

  /**
   * Returns a list of allowed RegionTypes. An empty list indicates that any type is allowed!
   */
  public Collection<RegionType> regionTypes() {
    if (regionTypes != null && regionTypes.values() != null)
      return Collections.unmodifiableCollection(regionTypes.values());
    return Collections.emptyList();
  }

  /**
   * Returns the id uniquely identifying this object.
   */
  @Override
  public StringID getID() {
    return (StringID) id;
  }

  public boolean isMaintainedByRegionOwner() {
    return maintenedByRegionOwner;
  }

  public void setMaintendByRegionOwner(boolean isMaintained) {
    maintenedByRegionOwner = isMaintained;
  }
}
