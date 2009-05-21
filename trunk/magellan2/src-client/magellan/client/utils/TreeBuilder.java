// class magellan.client.utils.TreeBuilder
// created on 15.02.2008
//
// Copyright 2003-2008 by magellan project team
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
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU General Public License for more details.
// 
// You should have received a copy of the GNU General Public License
// along with this program (see doc/LICENCE.txt); if not, write to the
// Free Software Foundation, Inc., 
// 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
// 
package magellan.client.utils;

import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeNode;

import magellan.client.swing.tree.NodeWrapperFactory;
import magellan.client.swing.tree.TreeHelper;
import magellan.library.Alliance;
import magellan.library.GameData;
import magellan.library.ID;
import magellan.library.Island;
import magellan.library.Region;
import magellan.library.Unique;
import magellan.library.Unit;
import magellan.library.utils.Resources;
import magellan.library.utils.comparator.IDComparator;
import magellan.library.utils.comparator.NameComparator;
import magellan.library.utils.comparator.RegionIslandComparator;

public class TreeBuilder {
  private Properties settings;
  private NodeWrapperFactory nodeWrapperFactory;
  
  /** Units are interesting. */
  public static final int UNITS = 1;

  /** Buildings are interesting. */
  public static final int BUILDINGS = 2;

  /** Ships are interesting. */
  public static final int SHIPS = 4;

  /** Comments are interesting. */
  public static final int COMMENTS = 8;

  /** Islands should be displayed. */
  public static final int CREATE_ISLANDS = 16384;

  /** the mode controls which elements are displayed */
  private int displayMode = TreeBuilder.UNITS | TreeBuilder.BUILDINGS | TreeBuilder.SHIPS | TreeBuilder.COMMENTS;

  // TODO hides fields form EmapOverviewPanel! */
  private Map<ID,TreeNode> regionNodes;
  private Map<ID,TreeNode> unitNodes;
  private Map<ID,TreeNode> buildingNodes;
  private Map<ID,TreeNode> shipNodes;
  private Map<ID, Alliance> activeAlliances;
  private Comparator unitComparator;
  private int treeStructure[];
  private boolean sortShipUnderUnitParent = true;
  
  /**
   * Creates a new treebuilder.
   */
  public TreeBuilder(Properties settings, NodeWrapperFactory nodeWrapperFactory) {
    this.settings = settings;
    this.nodeWrapperFactory = nodeWrapperFactory;
  }

  /**
   * Sets the display mode, which controls what elements to display.
   * 
   * @param mode
   */
  public void setDisplayMode(int mode) {
    this.displayMode = mode;
  }

  /**
   * Controls if ships nodes should be sorted under their parents node.
   * 
   * @param b
   */
  public void setSortShipUnderUnitParent(boolean b) {
    sortShipUnderUnitParent = b;
  }

  /**
   * Return the display mode, which controls what elements to display.
   * 
   * @return The current display mode.
   */
  public int getDisplayMode() {
    return displayMode;
  }

  /**
   * DOCUMENT-ME
   */
  public void setRegionNodes(Map<ID,TreeNode> regions) {
    regionNodes = regions;
  }

  /**
   * DOCUMENT-ME
   */
  public void setUnitNodes(Map<ID,TreeNode> units) {
    unitNodes = units;
  }

  /**
   * DOCUMENT-ME
   */
  public void setBuildingNodes(Map<ID,TreeNode> buildings) {
    buildingNodes = buildings;
  }

  /**
   * DOCUMENT-ME
   */
  public void setShipNodes(Map<ID,TreeNode> ships) {
    shipNodes = ships;
  }

  /**
   * DOCUMENT-ME
   */
  public void setActiveAlliances(Map<ID,Alliance> alliances) {
    activeAlliances = alliances;
  }

  /**
   * DOCUMENT-ME
   */
  public void setUnitComparator(Comparator compare) {
    unitComparator = compare;
  }

  /**
   * DOCUMENT-ME
   */
  public void setTreeStructure(int structure[]) {
    treeStructure = structure;
  }

  /**
   * Sort a collection of regions in a specific order.
   */
  private Collection sortRegions(Collection<Region> regions) {
    if ((Boolean.valueOf(settings.getProperty("EMapOverviewPanel.sortRegions", "true"))).booleanValue()) {
      if (settings.getProperty("EMapOverviewPanel.sortRegionsCriteria", "coordinates").equals("coordinates")) {
        List<Region> sortedRegions = new LinkedList<Region>(regions);
        Collections.sort(sortedRegions, IDComparator.DEFAULT);

        return sortedRegions;
      } else if (settings.getProperty("EMapOverviewPanel.sortRegionsCriteria", "coordinates").equals("islands")) {
        List<Region> sortedRegions = new LinkedList<Region>(regions);
        Comparator<Unique> idCmp = IDComparator.DEFAULT;
        Collections.sort(sortedRegions, new RegionIslandComparator(new NameComparator(idCmp), idCmp, idCmp));

        return sortedRegions;
      } else {
        return regions;
      }
    } else {
      return regions;
    }
  }


  /**
   * DOCUMENT-ME
   */
  public void buildTree(DefaultMutableTreeNode rootNode, GameData data) {
    if (data == null) {
      return;
    }

    buildTree(rootNode, sortRegions(data.regions().values()), data.units().values(), regionNodes, unitNodes, buildingNodes, shipNodes, unitComparator, activeAlliances, treeStructure, data);
  }

  /**
   * DOCUMENT-ME
   */
  public void buildTree(DefaultMutableTreeNode rootNode, Collection regionCollection, Collection<Unit> units, Map<ID,TreeNode> regionNodes, Map<ID,TreeNode> unitNodes, Map<ID,TreeNode> buildingNodes, Map<ID,TreeNode> shipNodes, Comparator unitSorting, Map<ID, Alliance> activeAlliances, int treeStructure[], GameData data) {
    boolean unitInteresting = (getDisplayMode() & TreeBuilder.UNITS) != 0;
    boolean buildingInteresting = (getDisplayMode() & TreeBuilder.BUILDINGS) != 0;
    boolean shipInteresting = (getDisplayMode() & TreeBuilder.SHIPS) != 0;
    boolean commentInteresting = (getDisplayMode() & TreeBuilder.COMMENTS) != 0;
    boolean createIslandNodes = (getDisplayMode() & TreeBuilder.CREATE_ISLANDS) != 0;

    DefaultMutableTreeNode islandNode = null;
    DefaultMutableTreeNode regionNode = null;
    Island curIsland = null;

    TreeHelper treehelper = new TreeHelper();
    for (Iterator regions = regionCollection.iterator(); regions.hasNext();) {
      Region r = (Region) regions.next();

      // check preferences if we want to include this region
      if (!((unitInteresting && !r.units().isEmpty())
          || (buildingInteresting && !r.buildings().isEmpty())
          || (shipInteresting && !r.ships().isEmpty()) || (commentInteresting && !((r.getComments() == null) || (r
          .getComments().size() == 0))))) {
        continue;
      }

      // add region node to tree an node map
      regionNode = (DefaultMutableTreeNode) treehelper.createRegionNode(r, nodeWrapperFactory, activeAlliances, unitNodes, buildingNodes, shipNodes, unitSorting, treeStructure, data, sortShipUnderUnitParent);

      if (regionNode == null) {
        continue;
      }

      // update island node
      if (createIslandNodes) {
        if (r.getIsland() != null) {
          if (!r.getIsland().equals(curIsland)) {
            curIsland = r.getIsland();
            islandNode = new DefaultMutableTreeNode(nodeWrapperFactory.createIslandNodeWrapper(curIsland));
            rootNode.add(islandNode);
          }
        } else {
          islandNode = null;
        }
      }

      if (islandNode != null) {
        islandNode.add(regionNode);
      } else {
        rootNode.add(regionNode);
      }

      regionNodes.put(r.getID(), regionNode);
    }

    // add the homeless
    DefaultMutableTreeNode n = new DefaultMutableTreeNode(Resources.get("emapoverviewpanel.node.regionlessunits"));

    for (Iterator<Unit> iter = units.iterator(); iter.hasNext();) {
      Unit un = iter.next();

      if (un.getRegion() == null) {
        n.add(new DefaultMutableTreeNode(nodeWrapperFactory.createUnitNodeWrapper(un)));
      }
    }

    if (n.getChildCount() > 0) {
      rootNode.add(n);
    }
  }
}
