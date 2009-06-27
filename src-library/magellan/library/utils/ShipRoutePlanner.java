/*
 * Copyright (C) 2000-2004 Roger Butenuth, Andreas Gampe, Stefan Goetz, Sebastian Pappert, Klaas
 * Prause, Enno Rehling, Sebastian Tusk, Ulrich Kuester, Ilja Pavkovic This file is part of the
 * Eressea Java Code Base, see the file LICENSING for the licensing information applying to this
 * file.
 */

package magellan.library.utils;

import java.awt.Component;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;

import javax.swing.JOptionPane;

import magellan.library.CoordinateID;
import magellan.library.GameData;
import magellan.library.Region;
import magellan.library.Ship;
import magellan.library.Unit;
import magellan.library.gamebinding.EresseaConstants;
import magellan.library.rules.BuildingType;
import magellan.library.utils.guiwrapper.RoutingDialogData;
import magellan.library.utils.guiwrapper.RoutingDialogDataPicker;

/**
 * Works together with com.eressea.swing.RoutingDialog to calculate the route for a ship.
 * 
 * @author Ulrich Küster
 * @author Andreas
 */
public class ShipRoutePlanner {
  /**
   * DOCUMENT-ME
   */
  public static boolean canPlan(Ship ship) {
    if (ship.getSize() < ship.getShipType().getMaxSize()) {
      return false;
    }

    return (ship.getOwnerUnit() != null) && ship.getOwnerUnit().getFaction().isPrivileged();
  }

  /**
   * Creates a route for a ship. It is configured by the given dialog. The orders are added to the
   * responsible unit that is then returned.
   * 
   * @param ship The ship for which a route is planned
   * @param data
   * @param ui The parent component for message panes
   * @param picker The dialog for selecting target region and other options
   * @return the unit whose orders may have change
   */
  public static Unit planShipRoute(Ship ship, GameData data, Component ui,
      RoutingDialogDataPicker picker) {
    // fetch all coast regions
    // Map oceans = Regions.getOceanRegionTypes(data.rules);
    Collection<Region> coast = new LinkedList<Region>();

    try {
      Map<CoordinateID, Region> regionMap = data.regions();
      Iterator cIt = regionMap.values().iterator();

      while (cIt.hasNext()) {
        try {
          Region region = (Region) cIt.next();
          Map<CoordinateID, Region> m =
              Regions.getAllNeighbours(regionMap, region.getCoordinate(), 1, null);
          Iterator<Region> cIt2 = m.values().iterator();

          while (cIt2.hasNext()) {
            Region r2 = cIt2.next();

            // if(oceans.values().contains(r2.getRegionType())) {
            if (r2.getRegionType().isOcean()) {
              coast.add(region);

              break;
            }
          }
        } catch (Exception exc) {
        }
      }
    } catch (Exception coastException) {
    }

    // get the data:
    if (coast.size() == 0) {
      picker.initialize(data, null, true);
    } else {
      picker.initialize(data, coast, true);
    }
    RoutingDialogData v = picker.showRoutingDialog();

    if (v != null) {
      Unit shipOwner = ship.getOwnerUnit();

      if (shipOwner != null) {
        if ((shipOwner.getFaction() != null) && shipOwner.getFaction().isPrivileged()) {
          BuildingType harbour = data.rules.getBuildingType(EresseaConstants.B_HARBOUR);

          int speed = data.getGameSpecificStuff().getGameSpecificRules().getShipRange(ship);
          List<Region> path =
              Regions
                  .planShipRoute(ship, v.getDestination(), data.regions(), harbour, speed);

          if (path != null) {
            // Now try to calculate the orders:

            if (!v.useRange()) {
              speed = Integer.MAX_VALUE;
            } else if (speed <= 0) {
              // couldn't determine ship range
              JOptionPane.showMessageDialog(ui, Resources
                  .get("util.shiprouteplanner.msg.shiprangeiszero.text"), Resources
                  .get("util.shiprouteplanner.msg.title"), JOptionPane.WARNING_MESSAGE);
              speed = Integer.MAX_VALUE;
            }

            List<String> orders = new LinkedList<String>();

            // TODO(pavkovic): move to EresseaOrderChanger
            String command = "NACH";
            if (v.makeRoute()) {
              command = "ROUTE";
            }
            addOrders(orders, path, command, v.useVorlage(), speed, v.makeSingle(), harbour);

            if (v.replaceOrders()) {
              shipOwner.setOrders(orders);
            } else {
              for (ListIterator iter = orders.listIterator(); iter.hasNext();) {
                shipOwner.addOrder((String) iter.next(), false, 0);
              }
            }

            return shipOwner;
          } else {
            // No path could be found from start to destination region.
            JOptionPane.showMessageDialog(ui, Resources
                .get("util.shiprouteplanner.msg.nopathfound.text"), Resources
                .get("util.shiprouteplanner.msg.title"), JOptionPane.WARNING_MESSAGE);
          }
        } else {
          // Captain of the ship does not belong to a privileged faction.
          // No orders can be given.
          JOptionPane.showMessageDialog(ui, Resources
              .get("util.shiprouteplanner.msg.captainnotprivileged.text"), Resources
              .get("util.shiprouteplanner.msg.title"), JOptionPane.WARNING_MESSAGE);
        }
      } else {
        // Ship has no captain. No orders will be given.
        JOptionPane.showMessageDialog(ui, Resources
            .get("util.shiprouteplanner.msg.captainnotfound.text"), Resources
            .get("util.shiprouteplanner.msg.title"), JOptionPane.WARNING_MESSAGE);
      }
    }

    return null;
  }

  protected static void addOrders(List<String> orders, List<Region> path, String command,
      boolean useVorlage, int shipRange, boolean single, BuildingType harbour) {

    String localCommand = Resources.getOrderTranslation(EresseaConstants.O_MOVE);
    if (command.equals("ROUTE")) {
      localCommand = Resources.getOrderTranslation(EresseaConstants.O_ROUTE);
    }

    StringBuffer order = new StringBuffer();
    order.append(localCommand).append(" ");
    for (int i = 0; i < 1 || (!single && i < 2); ++i) {

      int count = shipRange;
      int after = 0;
      String temp = ""; // saves whether a closing bracket must be added: "}"

      List<Region> curPath = new LinkedList<Region>();

      for (Iterator<Region> iter = path.iterator(); iter.hasNext();) {
        curPath.add(iter.next());

        if (curPath.size() > 1) {
          String dir = Regions.getDirections(curPath);

          if (dir != null) {
            if ((count == 0)
                || ((count != shipRange) && Regions.containsHarbour(curPath.get(0), harbour))) {
              after++;
              count = shipRange;

              if (useVorlage) {
                order.append(temp);
                orders.add(order.toString());
                order =
                    new StringBuffer("// #after ").append(after).append(" { ").append(command)
                        .append(" ");
                temp = "}";
              } else {
                if (command.equals("ROUTE")) {
                  order.append(Resources.getOrderTranslation(EresseaConstants.O_PAUSE)).append(" ");
                } else {
                  orders.add(order.toString());
                  order = new StringBuffer("// ").append(command).append(" ");
                }
              }
            }

            order.append(dir).append(" ");
            count--;
          }

          curPath.remove(0);
        }
      }

      if (command.equals("ROUTE")) {
        order.append(Resources.getOrderTranslation(EresseaConstants.O_PAUSE)).append(" ");
        order.append(temp);
        if (i > 0 || single)
          orders.add(order.toString());
      } else {
        order.append(temp);
        orders.add(order.toString());
        if (useVorlage) {
          order =
              new StringBuffer("// #after ").append(after).append(" { ").append(command)
                  .append(" ");
          temp = "}";
        } else {
          order = new StringBuffer("// ").append(command).append(" ");
        }
      }
      if (!single)
        Collections.reverse(path);
    }

  }
}
