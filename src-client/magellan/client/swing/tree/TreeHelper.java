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

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeNode;

import magellan.library.Alliance;
import magellan.library.Border;
import magellan.library.Building;
import magellan.library.EntityID;
import magellan.library.Faction;
import magellan.library.GameData;
import magellan.library.ID;
import magellan.library.Region;
import magellan.library.Ship;
import magellan.library.TempUnit;
import magellan.library.TrustLevel;
import magellan.library.Unit;
import magellan.library.io.cr.CRParser;
import magellan.library.utils.MagellanFactory;
import magellan.library.utils.Resources;
import magellan.library.utils.Taggable;
import magellan.library.utils.TrustLevels;
import magellan.library.utils.comparator.BuildingTypeComparator;
import magellan.library.utils.comparator.FactionTrustComparator;
import magellan.library.utils.comparator.IDComparator;
import magellan.library.utils.comparator.NameComparator;
import magellan.library.utils.comparator.ShipFactionComparator;
import magellan.library.utils.comparator.ShipTypeComparator;
import magellan.library.utils.comparator.TaggableComparator;
import magellan.library.utils.comparator.UnitCombatStatusComparator;
import magellan.library.utils.comparator.UnitFactionComparator;
import magellan.library.utils.comparator.UnitFactionDisguisedComparator;
import magellan.library.utils.comparator.UnitGroupComparator;
import magellan.library.utils.comparator.UnitGuiseFactionComparator;
import magellan.library.utils.comparator.UnitHealthComparator;
import magellan.library.utils.comparator.UnitTempUnitComparator;
import magellan.library.utils.comparator.UnitTrustComparator;
import magellan.library.utils.comparator.tree.GroupingComparator;

/**
 * To help constructing the tree structure.
 * 
 * @author Andreas, Ulrich Küster
 */
public class TreeHelper {
  /**
   * These are some constants used to encode the various criteria by which the units in the tree may
   * be organized.
   */
  public static final int FACTION = 0;

  public static final int GUISE_FACTION = 1;

  /** DOCUMENT-ME */
  public static final int GROUP = 2;

  /** DOCUMENT-ME */
  public static final int COMBAT_STATUS = 3;

  /** DOCUMENT-ME */
  public static final int HEALTH = 4;

  /** DOCUMENT-ME */
  public static final int FACTION_DISGUISE_STATUS = 5;

  /** DOCUMENT-ME */
  public static final int TRUSTLEVEL = 6;

  /** DOCUMENT-ME */
  public static final int TAGGABLE = 7;
  public static final int TAGGABLE2 = 8;
  public static final int TAGGABLE3 = 9;
  public static final int TAGGABLE4 = 10;
  public static final int TAGGABLE5 = 11;

  private static final Comparator<Building> buildingCmp = new BuildingTypeComparator(
      new NameComparator(IDComparator.DEFAULT));
  private static final Comparator<Ship> shipComparator = new ShipFactionComparator(
      new ShipTypeComparator(new NameComparator(IDComparator.DEFAULT)));
  private static final Comparator<Unit> healthCmp = new UnitHealthComparator(null);

  // pavkovic 2004.01.04: we dont want to sort groups by group id but name;
  // if they are sorted by id this would make tree hierarchy
  // (trustlevel, group) somehow uninteresting
  // Side effect: Groups are sorted by name
  private static final Comparator<Unit> groupCmp = new UnitGroupComparator(
      new NameComparator(null), null, null);

  private static final Comparator<Unit> factionDisguisedCmp = new UnitFactionDisguisedComparator(
      null);

  private static final Comparator<Unit> combatCmp = new UnitCombatStatusComparator(null);
  private static final Comparator<Unit> factionCmp = new UnitFactionComparator(
      new FactionTrustComparator(NameComparator.DEFAULT), null);
  private static final Comparator<Unit> guiseFactionCmp = new UnitFactionDisguisedComparator(
      new UnitGuiseFactionComparator(new FactionTrustComparator(NameComparator.DEFAULT), null));

  private static final Comparator<Unit> trustlevelCmp = UnitTrustComparator.DEFAULT_COMPARATOR;
  private static final Comparator<Taggable> taggableCmp = new TaggableComparator(
      CRParser.TAGGABLE_STRING, null);
  private static final Comparator<Taggable> taggableCmp2 = new TaggableComparator(
      CRParser.TAGGABLE_STRING2, null);
  private static final Comparator<Taggable> taggableCmp3 = new TaggableComparator(
      CRParser.TAGGABLE_STRING3, null);
  private static final Comparator<Taggable> taggableCmp4 = new TaggableComparator(
      CRParser.TAGGABLE_STRING4, null);
  private static final Comparator<Taggable> taggableCmp5 = new TaggableComparator(
      CRParser.TAGGABLE_STRING5, null);

  /**
   * Creates the subtree for one region with units (sorted by faction or other criteria), ships,
   * buildings, borders etc.
   */
  public TreeNode createRegionNode(Region r, NodeWrapperFactory factory,
      Map<EntityID, Alliance> activeAlliances, Map<ID, TreeNode> unitNodes,
      Map<ID, TreeNode> buildingNodes, Map<ID, TreeNode> shipNodes,
      Comparator<? super Unit> unitSorting, int treeStructure[], GameData data,
      boolean sortUnderUnitParent) {
    RegionNodeWrapper regionNodeWrapper = factory.createRegionNodeWrapper(r, 0);
    DefaultMutableTreeNode regionNode = new DefaultMutableTreeNode(regionNodeWrapper);

    List<Unit> units = new ArrayList<Unit>(r.units());

    if (units.size() > 0) {
      if (unitSorting != null) {
        Collections.sort(units, unitSorting);
      }

      addUnits(regionNode, treeStructure, 0, units, factory, activeAlliances, unitNodes, data,
          unitSorting);
    }

    // add ships
    List<Ship> sortedShips = new ArrayList<Ship>(r.ships());
    Collections.sort(sortedShips, TreeHelper.shipComparator);
    for (Ship s : sortedShips) {
      if (shipNodes == null || !shipNodes.containsKey(s.getID())) {
        DefaultMutableTreeNode shipNode = new DefaultMutableTreeNode(factory
            .createUnitContainerNodeWrapper(s));
        if (sortUnderUnitParent && s.getOwnerUnit() != null) {
          DefaultMutableTreeNode unitNode =
              (DefaultMutableTreeNode) unitNodes.get(s.getOwnerUnit().getID());
          if (unitNode != null) {
            DefaultMutableTreeNode parent = (DefaultMutableTreeNode) unitNode.getParent();
            parent.add(shipNode);
          }
        } else {
          regionNode.add(shipNode);
        }

        if (shipNodes != null) {
          shipNodes.put(s.getID(), shipNode);
        }
        for (Ship tempShip : s.getTempShips()) {
          DefaultMutableTreeNode tShipNode = new DefaultMutableTreeNode(factory
              .createUnitContainerNodeWrapper(tempShip));
          shipNode.add(tShipNode);
        }
      }
    }

    // add buildings
    List<Building> sortedBuildings = new ArrayList<Building>(r.buildings());
    Collections.sort(sortedBuildings, TreeHelper.buildingCmp);

    for (Building b : sortedBuildings) {
      DefaultMutableTreeNode node = new DefaultMutableTreeNode(factory
          .createUnitContainerNodeWrapper(b));
      regionNode.add(node);

      if (buildingNodes != null) {
        buildingNodes.put(b.getID(), node);
      }
    }

    // add borders
    for (Border b : r.borders()) {
      DefaultMutableTreeNode node = new DefaultMutableTreeNode(factory.createBorderNodeWrapper(b));
      regionNode.add(node);
    }

    return regionNode;
  }

  /**
   * This method assumes that the units are already sorted corresponding to treeStructure. (This is
   * done in createRegionNode(...).)
   * 
   * @return the number of persons (not units) that were added
   */
  private int addUnits(DefaultMutableTreeNode mother, int treeStructure[], int sortCriteria,
      List<Unit> units, NodeWrapperFactory factory, Map<EntityID, Alliance> activeAlliances,
      Map<ID, TreeNode> unitNodes, GameData data, Comparator<? super Unit> unitSorting) {
    SupportsEmphasizing se = null;

    if (mother.getUserObject() instanceof SupportsEmphasizing) {
      se = (SupportsEmphasizing) mother.getUserObject();
    }

    int retVal = 0;
    Unit curUnit = null;
    Unit prevUnit = null;
    List<Unit> helpList = new ArrayList<Unit>();
    // FIXME (stm) this recursive implementation seems very inefficient
    for (Unit unit : units) {
      // ignore temp units
      // they are added under their mother unit
      if (unit instanceof TempUnit) {
        continue;
      }

      prevUnit = curUnit;
      curUnit = unit;

      if (sortCriteria >= treeStructure.length) {
        // all structuring has been done
        // simply add the units
        UnitNodeWrapper nodeWrapper = factory.createUnitNodeWrapper(curUnit, curUnit.getPersons());
        DefaultMutableTreeNode node = new DefaultMutableTreeNode(nodeWrapper);

        if (unitNodes != null) {
          unitNodes.put(curUnit.getID(), node);
        }

        mother.add(node);

        if (se != null) {
          se.addSubordinatedElement(nodeWrapper);
        }

        retVal += curUnit.getPersons();

        // take care of temp units
        if (!curUnit.tempUnits().isEmpty()) {
          ArrayList<TempUnit> temps = new ArrayList<TempUnit>();
          temps.addAll(curUnit.tempUnits());
          Collections.sort(temps, unitSorting);
          for (TempUnit tempUnit2 : temps) {
            Unit tempUnit = tempUnit2;
            UnitNodeWrapper tempNodeWrapper =
                factory.createUnitNodeWrapper(tempUnit, tempUnit.getPersons());
            DefaultMutableTreeNode tempNode = new DefaultMutableTreeNode(tempNodeWrapper);
            node.add(tempNode);
            nodeWrapper.addSubordinatedElement(tempNodeWrapper);

            if (unitNodes != null) {
              unitNodes.put(tempUnit.getID(), tempNode);
            }
          }
        }
        /*
         * if(curUnit.getShip() != null){ Ship s = curUnit.getShip(); // also add ships under parent
         * of the current unit node = new
         * DefaultMutableTreeNode(factory.createUnitContainerNodeWrapper(s)); mother.add(node);
         * if(shipNodes != null) { shipNodes.put(s.getID(), node); } }
         */

      } else {
        if (change(treeStructure[sortCriteria], curUnit, prevUnit)) {
          Faction faction;
          // change in current sortCriteria?
          switch (treeStructure[sortCriteria]) {
          case FACTION:
            if (curUnit.getFaction() == null) {
              faction = data.getNullFaction();
            } else {
              faction = prevUnit.getFaction();
            }
            FactionNodeWrapper factionNodeWrapper = factory.createFactionNodeWrapper(
                faction, prevUnit.getRegion(), activeAlliances);
            DefaultMutableTreeNode factionNode = new DefaultMutableTreeNode(factionNodeWrapper);
            mother.add(factionNode);

            if (se != null) {
              se.addSubordinatedElement(factionNodeWrapper);
            }

            retVal +=
                addUnits(factionNode, treeStructure, sortCriteria + 1, helpList, factory,
                    activeAlliances, unitNodes, data, unitSorting);
            helpList.clear();

            break;

          case GUISE_FACTION:
            if (prevUnit.isHideFaction()) {
              SimpleNodeWrapper fdsNodeWrapper =
                  factory.createSimpleNodeWrapper(
                      Resources.get("tree.treehelper.factiondisguised"), "tarnung");
              DefaultMutableTreeNode fdsNode = new DefaultMutableTreeNode(fdsNodeWrapper);
              mother.add(fdsNode);

              if (se != null) {
                se.addSubordinatedElement(fdsNodeWrapper);
              }

              retVal +=
                  addUnits(fdsNode, treeStructure, sortCriteria + 1, helpList, factory,
                      activeAlliances, unitNodes, data, unitSorting);
            } else if (prevUnit.getGuiseFaction() != null) {
              FactionNodeWrapper guiseFactionNodeWrapper =
                  factory.createFactionNodeWrapper(prevUnit.getGuiseFaction(),
                      prevUnit.getRegion(), activeAlliances);
              DefaultMutableTreeNode guiseFactionNode =
                  new DefaultMutableTreeNode(guiseFactionNodeWrapper);
              mother.add(guiseFactionNode);

              if (se != null) {
                se.addSubordinatedElement(guiseFactionNodeWrapper);
              }

              retVal +=
                  addUnits(guiseFactionNode, treeStructure, sortCriteria + 1, helpList, factory,
                      activeAlliances, unitNodes, data, unitSorting);
            } else {
              retVal +=
                  addUnits(mother, treeStructure, sortCriteria + 1, helpList, factory,
                      activeAlliances, unitNodes, data, unitSorting);
            }

            helpList.clear();

            break;

          case FACTION_DISGUISE_STATUS:
            if (prevUnit.isHideFaction()) {
              SimpleNodeWrapper fdsNodeWrapper =
                  factory.createSimpleNodeWrapper(
                      Resources.get("tree.treehelper.factiondisguised"), "tarnung");
              DefaultMutableTreeNode fdsNode = new DefaultMutableTreeNode(fdsNodeWrapper);
              mother.add(fdsNode);

              if (se != null) {
                se.addSubordinatedElement(fdsNodeWrapper);
              }

              retVal +=
                  addUnits(fdsNode, treeStructure, sortCriteria + 1, helpList, factory,
                      activeAlliances, unitNodes, data, unitSorting);
            } else {
              retVal +=
                  addUnits(mother, treeStructure, sortCriteria + 1, helpList, factory,
                      activeAlliances, unitNodes, data, unitSorting);
            }

            helpList.clear();

            break;

          case GROUP:
            // Do the units belong to a group?
            if (prevUnit.getGroup() != null) {
              GroupNodeWrapper groupNodeWrapper =
                  factory.createGroupNodeWrapper(prevUnit.getGroup());
              DefaultMutableTreeNode groupNode = new DefaultMutableTreeNode(groupNodeWrapper);
              // FIXME (stm 2007-03-14) wrapping groupNodeWrapper into
              // simpleNodeWrapper has broken
              // display of group information in details window (see
              // SelectionEvent and EMapDetailsWrapper.appendGroupInfo)
              // SimpleNodeWrapper simpleGroupNodeWrapper =
              // factory.createSimpleNodeWrapper(groupNodeWrapper, "groups");
              // DefaultMutableTreeNode groupNode = new
              // DefaultMutableTreeNode(simpleGroupNodeWrapper);
              mother.add(groupNode);

              if (se != null) {
                se.addSubordinatedElement(groupNodeWrapper);
              }

              retVal +=
                  addUnits(groupNode, treeStructure, sortCriteria + 1, helpList, factory,
                      activeAlliances, unitNodes, data, unitSorting);
            } else {
              retVal +=
                  addUnits(mother, treeStructure, sortCriteria + 1, helpList, factory,
                      activeAlliances, unitNodes, data, unitSorting);
            }

            helpList.clear();

            break;

          case HEALTH:
            data.getTranslation("verwundet");
            data.getTranslation("schwer verwundet");
            data.getTranslation("erschöpft");
            String hicon = null;
            String text = prevUnit.getHealth();

            if (text == null) {
              text = Resources.get("tree.treehelper.healthy");
              hicon = "gesund";
            } else {
              hicon = text;
            }

            // parent.add(createSimpleNode(u.health,hicon));
            SimpleNodeWrapper healthNodeWrapper = factory.createSimpleNodeWrapper(text, hicon);
            DefaultMutableTreeNode healthNode = new DefaultMutableTreeNode(healthNodeWrapper);
            mother.add(healthNode);

            if (se != null) {
              se.addSubordinatedElement(healthNodeWrapper);
            }

            retVal +=
                addUnits(healthNode, treeStructure, sortCriteria + 1, helpList, factory,
                    activeAlliances, unitNodes, data, unitSorting);
            helpList.clear();

            break;

          case COMBAT_STATUS:
            SimpleNodeWrapper combatNodeWrapper =
                factory.createSimpleNodeWrapper(MagellanFactory.combatStatusToString(prevUnit),
                    "kampfstatus");
            DefaultMutableTreeNode combatNode = new DefaultMutableTreeNode(combatNodeWrapper);
            mother.add(combatNode);

            if (se != null) {
              se.addSubordinatedElement(combatNodeWrapper);
            }

            retVal +=
                addUnits(combatNode, treeStructure, sortCriteria + 1, helpList, factory,
                    activeAlliances, unitNodes, data, unitSorting);
            helpList.clear();

            break;

          case TRUSTLEVEL: {
            String label;
            if (prevUnit.getFaction() != null) {
              label = TrustLevels.getTrustLevelLabel(prevUnit.getFaction().getTrustLevel());
            } else {
              label = TrustLevels.getTrustLevelLabel(TrustLevel.TL_DEFAULT);
            }
            SimpleNodeWrapper trustlevelNodeWrapper = factory.createSimpleNodeWrapper(label, (String) null);
            DefaultMutableTreeNode trustlevelNode = new DefaultMutableTreeNode(trustlevelNodeWrapper);
            mother.add(trustlevelNode);

            if (se != null) {
              se.addSubordinatedElement(trustlevelNodeWrapper);
            }

            retVal +=
                addUnits(trustlevelNode, treeStructure, sortCriteria + 1, helpList, factory,
                    activeAlliances, unitNodes, data, unitSorting);
            helpList.clear();
          }
            break;

          case TAGGABLE:
          case TAGGABLE2:
          case TAGGABLE3:
          case TAGGABLE4:
          case TAGGABLE5: {
            String label = getTaggableLabel(prevUnit, treeStructure[sortCriteria]);
            if (label != null) {
              SimpleNodeWrapper simpleNodeWrapper = factory.createSimpleNodeWrapper(label, (String) null);
              DefaultMutableTreeNode taggableNode = new DefaultMutableTreeNode(simpleNodeWrapper);
              mother.add(taggableNode);

              if (se != null) {
                se.addSubordinatedElement(simpleNodeWrapper);
              }

              retVal +=
                  addUnits(taggableNode, treeStructure, sortCriteria + 1, helpList, factory,
                      activeAlliances, unitNodes, data, unitSorting);
            } else {
              retVal +=
                  addUnits(mother, treeStructure, sortCriteria + 1, helpList, factory,
                      activeAlliances, unitNodes, data, unitSorting);
            }
            helpList.clear();
          }
            break;
          } // end of switch
        }

        helpList.add(curUnit);
      }
    }

    // end of unit iterator
    // take care of all units that are left
    if (!helpList.isEmpty() && curUnit != null) {
      DefaultMutableTreeNode node = null;
      if (sortCriteria <= treeStructure.length) {
        Faction faction;
        switch (treeStructure[sortCriteria]) {
        case FACTION:
          if (curUnit.getFaction() == null) {
            faction = data.getNullFaction();
          } else {
            faction = curUnit.getFaction();
          }
          node =
              new DefaultMutableTreeNode(factory.createFactionNodeWrapper(faction,
                  curUnit.getRegion(), activeAlliances));
          break;

        case GUISE_FACTION:
          if (curUnit.isHideFaction()) {
            node =
                new DefaultMutableTreeNode(factory.createSimpleNodeWrapper(Resources
                    .get("tree.treehelper.factiondisguised"), "tarnung"));
          } else if (curUnit.getGuiseFaction() != null) {
            node =
                new DefaultMutableTreeNode(factory.createFactionNodeWrapper(curUnit
                    .getGuiseFaction(), curUnit.getRegion(), activeAlliances));
          }

          break;

        case FACTION_DISGUISE_STATUS:

          if (curUnit.isHideFaction()) {
            node =
                new DefaultMutableTreeNode(factory.createSimpleNodeWrapper(Resources
                    .get("tree.treehelper.factiondisguised"), "tarnung"));
          }

          break;

        case GROUP:

          if (curUnit.getGroup() != null) {
            node = new DefaultMutableTreeNode(factory.createGroupNodeWrapper(curUnit.getGroup()));
            // FIXME see above
            // GroupNodeWrapper groupNodeWrapper =
            // factory.createGroupNodeWrapper(curUnit.getGroup());
            // node = new
            // DefaultMutableTreeNode(factory.createSimpleNodeWrapper(groupNodeWrapper,
            // "groups"));
          }

          break;

        case HEALTH:

          data.getTranslation("verwundet");
          data.getTranslation("schwer verwundet");
          data.getTranslation("erschöpft");
          String hicon = null;
          String text = curUnit.getHealth();

          if (text == null) {
            text = Resources.get("tree.treehelper.healthy");
            hicon = "gesund";
          } else {
            hicon = text;
          }

          node = new DefaultMutableTreeNode(factory.createSimpleNodeWrapper(text, hicon));

          break;

        case COMBAT_STATUS:

          node =
              new DefaultMutableTreeNode(factory.createSimpleNodeWrapper(MagellanFactory
                  .combatStatusToString(curUnit), "kampfstatus"));

          break;

        case TRUSTLEVEL: {
          String label;
          if (curUnit.getFaction() != null) {
            label = TrustLevels.getTrustLevelLabel(curUnit.getFaction().getTrustLevel());
          } else {
            label = TrustLevels.getTrustLevelLabel(TrustLevel.TL_DEFAULT);
          }
          node = new DefaultMutableTreeNode(factory.createSimpleNodeWrapper(label, (String) null));
        }
          break;

        case TAGGABLE:
        case TAGGABLE2:
        case TAGGABLE3:
        case TAGGABLE4:
        case TAGGABLE5: {
          String label = getTaggableLabel(curUnit, treeStructure[sortCriteria]);
          if (label != null) {
            node =
                new DefaultMutableTreeNode(factory.createSimpleNodeWrapper(label, (String) null));
          }
        }
          break;

        } // end of switch
      }

      // end of if (sortCriteria <= treeStructure.length)
      // now add units
      if (node == null) {
        retVal +=
            addUnits(mother, treeStructure, sortCriteria + 1, helpList, factory, activeAlliances,
                unitNodes, data, unitSorting);
      } else {
        mother.add(node);

        if ((se != null) && node.getUserObject() instanceof SupportsEmphasizing) {
          se.addSubordinatedElement((SupportsEmphasizing) node.getUserObject());
        }

        retVal +=
            addUnits(node, treeStructure, sortCriteria + 1, helpList, factory, activeAlliances,
                unitNodes, data, unitSorting);
      }
    }

    // now add number of persons to node
    Object user = mother.getUserObject();

    if (user instanceof RegionNodeWrapper) {
      ((RegionNodeWrapper) user).setAmount(retVal);
    } else if (user instanceof FactionNodeWrapper) {
      ((FactionNodeWrapper) user).setAmount(retVal);
    } else if (user instanceof GroupNodeWrapper) {
      ((GroupNodeWrapper) user).setAmount(retVal);
    } else if (user instanceof SimpleNodeWrapper) {
      ((SimpleNodeWrapper) user).setAmount(retVal);
    } else {
      System.out.println("TreeHelper.addSortedUnits(): unknown user object.");
    }

    return retVal;
  }

  private String getTaggableLabel(Unit unit, int taggable) {
    String tagName = null;
    switch (taggable) {
    case TAGGABLE:
      tagName = CRParser.TAGGABLE_STRING;
      break;
    case TAGGABLE2:
      tagName = CRParser.TAGGABLE_STRING2;
      break;
    case TAGGABLE3:
      tagName = CRParser.TAGGABLE_STRING3;
      break;
    case TAGGABLE4:
      tagName = CRParser.TAGGABLE_STRING4;
      break;
    case TAGGABLE5:
      tagName = CRParser.TAGGABLE_STRING5;
      break;
    }
    return tagName == null || unit == null ? null : unit.getTag(tagName);
  }

  /**
   * Little helper function that determines, whether the two given units differ in regard to the
   * given flag. The flag should be given according to the constants defined in this class (FACTION,
   * GROUP, ...) If one of the unit arguments is null, false is returned.
   */
  private boolean change(int flag, Unit curUnit, Unit prevUnit) {
    if ((curUnit == null) || (prevUnit == null))
      return false;

    switch (flag) {
    case FACTION:
      return TreeHelper.factionCmp.compare(prevUnit, curUnit) != 0;

    case GUISE_FACTION:
      return TreeHelper.guiseFactionCmp.compare(prevUnit, curUnit) != 0;

    case GROUP:
      return TreeHelper.groupCmp.compare(prevUnit, curUnit) != 0;

    case HEALTH:
      return TreeHelper.healthCmp.compare(prevUnit, curUnit) != 0;

    case COMBAT_STATUS:
      return TreeHelper.combatCmp.compare(prevUnit, curUnit) != 0;

    case FACTION_DISGUISE_STATUS:
      return TreeHelper.factionDisguisedCmp.compare(prevUnit, curUnit) != 0;

    case TRUSTLEVEL:
      return TreeHelper.trustlevelCmp.compare(prevUnit, curUnit) != 0;

    case TAGGABLE:
      return TreeHelper.taggableCmp.compare(prevUnit, curUnit) != 0;
    case TAGGABLE2:
      return TreeHelper.taggableCmp2.compare(prevUnit, curUnit) != 0;
    case TAGGABLE3:
      return TreeHelper.taggableCmp3.compare(prevUnit, curUnit) != 0;
    case TAGGABLE4:
      return TreeHelper.taggableCmp4.compare(prevUnit, curUnit) != 0;
    case TAGGABLE5:
      return TreeHelper.taggableCmp5.compare(prevUnit, curUnit) != 0;
    }

    return false; // default
  }

  public static Comparator<? super Unit> buildComparator(Comparator<? super Unit> cmp,
      int[] treeStructure) {
    // now build the Comparator used for unit sorting
    GroupingComparator<Unit> comp = new GroupingComparator<Unit>(cmp, null);

    for (int i = treeStructure.length - 1; i >= 0; i--) {
      switch (treeStructure[i]) {
      case TreeHelper.FACTION:
        comp = new GroupingComparator<Unit>(TreeHelper.factionCmp, comp);
        break;

      case TreeHelper.GUISE_FACTION:
        comp = new GroupingComparator<Unit>(TreeHelper.guiseFactionCmp, comp);
        break;

      case TreeHelper.GROUP:

        // pavkovic 2004.01.04: we dont want to sort groups by group id but
        // name;
        // if they are sorted by id this would make tree hierarchy
        // (trustlevel, group) somehow uninteresting
        // Side effect: Groups are sorted by name
        comp = new GroupingComparator<Unit>(TreeHelper.groupCmp, comp);
        break;

      case TreeHelper.COMBAT_STATUS:
        comp = new GroupingComparator<Unit>(TreeHelper.combatCmp, comp);
        break;

      case TreeHelper.HEALTH:
        comp = new GroupingComparator<Unit>(TreeHelper.healthCmp, comp);
        break;

      case TreeHelper.FACTION_DISGUISE_STATUS:
        comp = new GroupingComparator<Unit>(TreeHelper.factionDisguisedCmp, comp);
        break;

      case TreeHelper.TRUSTLEVEL:
        comp = new GroupingComparator<Unit>(TreeHelper.trustlevelCmp, comp);
        break;

      case TreeHelper.TAGGABLE:
        comp = new GroupingComparator<Unit>(TreeHelper.taggableCmp, comp);
        break;
      case TreeHelper.TAGGABLE2:
        comp = new GroupingComparator<Unit>(TreeHelper.taggableCmp2, comp);
        break;
      case TreeHelper.TAGGABLE3:
        comp = new GroupingComparator<Unit>(TreeHelper.taggableCmp3, comp);
        break;
      case TreeHelper.TAGGABLE4:
        comp = new GroupingComparator<Unit>(TreeHelper.taggableCmp4, comp);
        break;
      case TreeHelper.TAGGABLE5:
        comp = new GroupingComparator<Unit>(TreeHelper.taggableCmp5, comp);
        break;
      }
    }

    // care for temp units
    return new UnitTempUnitComparator(IDComparator.DEFAULT, comp);
  }

}
