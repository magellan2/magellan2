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

package magellan.client.swing.tree;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;

import magellan.library.Island;
import magellan.library.Region;
import magellan.library.Unit;
import magellan.library.utils.TrustLevels;

/**
 * DOCUMENT-ME
 * 
 * @author $Author: $
 * @version $Revision: 171 $
 */
public class IslandNodeWrapper extends DefaultNodeWrapper implements SupportsClipboard {
  private Island island = null;

  // a static list (will never change) its value over all instances of IslandNodeWrapper
  private static List<String> iconNames = Collections.singletonList("insel");

  /**
   * Creates a new IslandNodeWrapper object.
   */
  public IslandNodeWrapper(Island island) {
    this.island = island;
  }

  /**
   * @return The corresponding island
   */
  public Island getIsland() {
    return island;
  }

  /**
   * @return the island's name
   */
  @Override
  public String toString() {
    return island.getName();
  }

  /**
   * @see magellan.client.swing.tree.CellObject#getIconNames()
   */
  public List<String> getIconNames() {
    return IslandNodeWrapper.iconNames;
  }

  /**
   * @see magellan.client.swing.tree.CellObject#emphasized()
   */
  @Override
  public boolean emphasized() {
    for (Region region : island.regions()) {
      Iterator<Unit> it = (region).units().iterator();

      if (it != null) {
        while (it.hasNext()) {
          Unit u = it.next();

          if (u.getFaction() != null && TrustLevels.isPrivileged(u.getFaction())) {
            if (!u.isOrdersConfirmed())
              return true;
          }
        }
      }
    }

    return false;
  }

  /**
   * @see magellan.client.swing.tree.CellObject#propertiesChanged()
   */
  public void propertiesChanged() {
    // no changeable properties
  }

  /**
   * @see magellan.client.swing.tree.SupportsClipboard#getClipboardValue()
   */
  public String getClipboardValue() {
    return (island != null) ? island.getName() : toString();
  }

  /**
   * @see magellan.client.swing.tree.CellObject#init(java.util.Properties,
   *      magellan.client.swing.tree.NodeWrapperDrawPolicy)
   */
  public NodeWrapperDrawPolicy init(Properties settings, NodeWrapperDrawPolicy adapter) {
    return null;
  }

  /**
   * @see magellan.client.swing.tree.CellObject#init(java.util.Properties, java.lang.String,
   *      magellan.client.swing.tree.NodeWrapperDrawPolicy)
   */
  public NodeWrapperDrawPolicy init(Properties settings, String prefix,
      NodeWrapperDrawPolicy adapter) {
    return null;
  }
}
