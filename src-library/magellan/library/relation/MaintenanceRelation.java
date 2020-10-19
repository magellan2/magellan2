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

package magellan.library.relation;

import magellan.library.Unit;
import magellan.library.UnitContainer;
import magellan.library.rules.ItemType;

/**
 * A relation indicating that a unit pays maintenance of a building or ship.
 */
public class MaintenanceRelation extends UnitContainerRelation {
  /** Maintenance costs */
  private int costs;

  /** The required item type */
  public ItemType itemType;

  /** indicates whether there is some kind of problem with this relation */
  public boolean warning;

  private String reason;

  private String icon;

  /**
   * Creates a new MaintenanceRelation object.
   *
   * @param unit The maintaining unit
   * @param container the maintained building
   * @param amount The costs in silver
   * @param itemType
   * @param line The line in the source's orders
   * @param warning
   */
  public MaintenanceRelation(Unit unit, UnitContainer container, int amount, ItemType itemType, String reason,
      String icon, int line, boolean warning) {
    super(unit, container, line);
    setCosts(amount);
    this.itemType = itemType;
    this.reason = reason;
    this.icon = icon;
    this.warning = warning;
  }

  /**
   * @see magellan.library.relation.UnitRelation#add()
   */
  @Override
  public void add() {
    super.add();
    target.addRelation(this);
  }

  /**
   * Returns the value of costs.
   *
   * @return Returns costs.
   */
  public int getCosts() {
    return costs;
  }

  /**
   * Sets the value of costs.
   *
   * @param costs The value for costs.
   */
  public void setCosts(int costs) {
    this.costs = costs;
  }

  /**
   * Returns a localized reason for this relation.
   */
  public String getReason() {
    return reason;
  }

  /**
   * @param reason
   */
  public void setReason(String reason) {
    this.reason = reason;
  }

  /**
   * Returns an icon name associated with this relation.
   */
  public String getIcon() {
    return icon;
  }

  /**
   * @param name
   */
  public void setIcon(String name) {
    icon = name;
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.eressea.relation.UnitRelation#toString()
   */
  @Override
  public String toString() {
    return super.toString() + "@ITEM=" + itemType + "@COSTS" + costs;
  }
}
