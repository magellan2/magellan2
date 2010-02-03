/*
 * Copyright (C) 2000-2004 Roger Butenuth, Andreas Gampe, Stefan Goetz, Sebastian Pappert, Klaas
 * Prause, Enno Rehling, Sebastian Tusk, Ulrich Kuester, Ilja Pavkovic This file is part of the
 * Eressea Java Code Base, see the file LICENSING for the licensing information applying to this
 * file.
 */

package magellan.library.rules;

import java.util.Collection;
import java.util.Collections;

import magellan.library.Item;
import magellan.library.StringID;
import magellan.library.utils.logging.Logger;

/**
 * Members of this class contain information about a type of ship.
 * 
 * @author $Author: $
 * @version $Revision: 203 $
 */
public class ShipType extends ConstructibleType {
  Logger log = Logger.getInstance(ShipType.class);
  
  private int range = -1;
  private int capacity = -1;
  private int captainLevel = -1;
  private int sailorLevel = -1;
  private int persons = -1;
  private ItemType wood;

  /**
   * Creates a new ShipType object.
   */
  public ShipType(StringID id) {
    super(id);
  }


  public void init(ItemType wood) {
    this.wood = wood;
  }

  /**
   * Sets the regular range of this ship type.
   */
  public void setRange(int r) {
    range = r;
  }

  /**
   * Returns the regular range of this ship type (before any modifiers).
   */
  public int getRange() {
    return range;
  }

  /**
   * Sets the maximum capacity (in GE).
   */
  public void setCapacity(int c) {
    capacity = c;
  }

  /**
   * Returns the maximum capacity (in GE).
   */
  public int getCapacity() {
    return capacity;
  }

  /**
   * Sets the number of persons that fit on the ship.
   */
  public void setMaxPersons(int p) {
    persons = p;
  }

  /**
   * Returns the number of persons that fit on the ship.
   * 
   */
  public int getMaxPersons() {
    return persons ;
  }

  /**
   * Set the skill level required to command the ship.
   */
  public void setCaptainSkillLevel(int l) {
    captainLevel = l;
  }

  /**
   * Returns the skill level required to command the ship.
   */
  public int getCaptainSkillLevel() {
    return captainLevel;
  }

  /**
   * Sets the number of skill levels to sail the ship.
   */
  public void setSailorSkillLevel(int l) {
    sailorLevel = l;
  }

  /**
   * Returns the number of skill levels to sail the ship.
   */
  public int getSailorSkillLevel() {
    return sailorLevel;
  }

  protected boolean fallBackMaterial = true;

  /**
   * @see magellan.library.rules.ConstructibleType#addRawMaterial(magellan.library.Item)
   */
  @Override
  public void addRawMaterial(Item i) {
    fallBackMaterial = false;
    super.addRawMaterial(i);
  }

  /**
   * @see magellan.library.rules.ConstructibleType#getRawMaterial(magellan.library.ID)
   */
  @Override
  public Item getRawMaterial(StringID id) {
    // if no raw materials have been added we fall back to old behavior: 1 wood per size point
    if (fallBackMaterial) {
      if (id.equals(wood.getID()))
        return new Item(new ItemType(id), 1);
      else
        return null;
    } else {
      return super.getRawMaterial(id);
    }
  }

  /**
   * @see magellan.library.rules.ConstructibleType#getRawMaterials()
   */
  @Override
  public Collection<Item> getRawMaterials() {
    if (fallBackMaterial)
      if (wood == null){
        log.warn("Hmm..., don't know what wood is...");
        return super.getRawMaterials();
      } else
        return Collections.singletonList(new Item(wood, 1));
    else
      return super.getRawMaterials();
  }

  /**
   * Returns the id uniquely identifying this object.
   */
  @Override
  public StringID getID() {
    return (StringID) id;
  }
}
