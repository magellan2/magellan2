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
import java.util.LinkedList;
import java.util.List;

import javax.swing.JOptionPane;

import magellan.library.CoordinateID;
import magellan.library.GameData;
import magellan.library.Region;
import magellan.library.Ship;
import magellan.library.Unit;
import magellan.library.gamebinding.EresseaConstants;
import magellan.library.gamebinding.OrderChanger;
import magellan.library.rules.BuildingType;
import magellan.library.utils.guiwrapper.RoutingDialogData;
import magellan.library.utils.guiwrapper.RoutingDialogDataPicker;
import magellan.library.utils.logging.Logger;

/**
 * Works together with com.eressea.swing.RoutingDialog to calculate the route for a ship.
 *
 * @author Ulrich Küster
 * @author Andreas
 * @author stm
 */
public class ShipRoutePlanner extends RoutePlanner {
  private static final Logger log = Logger.getInstance(ShipRoutePlanner.class);

  /**
   * Returns <code>true</code> if the ship is complete and ship's owner belongs to a privileged
   * faction.
   */
  public static boolean canPlan(Ship ship) {
    if (ship.getSize() < Units.getNominalSize(ship))
      return false;

    return (ship.getModifiedOwnerUnit() != null)
        && ship.getModifiedOwnerUnit().getFaction().isPrivileged();
  }

  /**
   * Creates a route for a ship. It is configured by the given dialog. The orders are added to the
   * responsible unit that is then returned.
   *
   * @param ship The ship for which a route is planned
   * @param data GameData containing the units
   * @param ui The parent component for message panes
   * @param picker The dialog for selecting target region and other options
   * @return the unit whose orders may have changed if orders were changed (i.e., the user didn't
   *         abort and a path existed)
   */
  public Unit planShipRoute(Ship ship, GameData data, Component ui,
      RoutingDialogDataPicker picker) {
    Unit shipOwner = ship.getModifiedOwnerUnit();

    if (shipOwner == null) {
      // Ship has no captain. No orders will be given.
      JOptionPane.showMessageDialog(ui, Resources
          .get("util.shiprouteplanner.msg.captainnotfound.text"), Resources
              .get("util.shiprouteplanner.msg.title"), JOptionPane.WARNING_MESSAGE);
      return null;
    } else {
      if ((shipOwner.getFaction() == null) || !shipOwner.getFaction().isPrivileged()) {
        // Captain of the ship does not belong to a privileged faction.
        // No orders can be given.
        JOptionPane.showMessageDialog(ui, Resources
            .get("util.shiprouteplanner.msg.captainnotprivileged.text"), Resources
                .get("util.shiprouteplanner.msg.title"), JOptionPane.WARNING_MESSAGE);
      }
    }

    // fetch all coast regions
    // Map oceans = Regions.getOceanRegionTypes(data.rules);
    Collection<Region> coast = new LinkedList<Region>();

    for (Region region : data.getRegions()) {
      try {
        for (Region r2 : region.getNeighbors().values()) {

          // if(oceans.values().contains(r2.getRegionType())) {
          if (r2.getRegionType().isOcean()) {
            coast.add(region);

            break;
          }
        }
      } catch (Exception exc) {
        log.error(exc);
      }
    }

    // get the data:
    if (coast.size() == 0) {
      picker.initialize(data, null, true);
    } else {
      picker.initialize(data, coast, true);
    }

    RoutingDialogData options = picker.showRoutingDialog();

    // find the route
    if (options != null) {
      List<String> orders =
          getOrders(ship, data, ship.getRegion().getID(), options.getDestination(), ui, options
              .useRange(), options.getMode(), options.useVorlage());
      if (orders.size() == 0)
        return null;
      getOrderChanger(shipOwner).setLongOrders(shipOwner, orders, options.replaceOrders());

      return shipOwner;
    }

    return null;
  }

  private OrderChanger getOrderChanger(Unit u) {
    return u.getData().getGameSpecificStuff().getOrderChanger();
  }

  /**
   * Creates movement orders for a ship.
   *
   * @param ship The ship for which a route is planned
   * @param data GameData containing the units
   * @param start The region where to start, not necessarily equal to the ship's region
   * @param destination The target region
   * @param ui The parent component for message panes
   * @param useRange If this is <code>true</code>, the orders are split into multiple orders, so
   *          that the ship's range is not exceeded.
   * @param mode a combination of {@link RoutePlanner#MODE_CONTINUOUS},
   *          {@link RoutePlanner#MODE_RETURN}, {@link RoutePlanner#MODE_STOP}
   * @param useVorlage If this is <code>true</code>, <em>Vorlage</em> meta commands are produced.
   * @return The list of new orders.
   */
  public List<String> getOrders(Ship ship, GameData data, CoordinateID start,
      CoordinateID destination, Component ui, boolean useRange, int mode, boolean useVorlage) {
    BuildingType harbour = data.getRules().getBuildingType(EresseaConstants.B_HARBOUR);
    int speed = Math.max(1, data.getGameSpecificRules().getShipRange(ship));
    List<Region> path =
        Regions.planShipRoute(data, ship.getRegion().getID(), data.getGameSpecificStuff()
            .getMapMetric().toDirection(ship.getShoreId()), destination, speed);
    if (path == null || path.size() <= 1) {
      if (ui != null) {
        // No path could be found from start to destination region.
        JOptionPane.showMessageDialog(ui, Resources
            .get("util.shiprouteplanner.msg.nopathfound.text"), Resources
                .get("util.shiprouteplanner.msg.title"), JOptionPane.WARNING_MESSAGE);
      }
      return Collections.emptyList();
    }

    // ...and optionally the return path
    List<Region> returnPath = null;
    if ((mode & RoutePlanner.MODE_RETURN) > 0) {
      Region lastRegion = path.get(path.size() - 1);
      Direction returnDirection = Direction.INVALID;
      if (!lastRegion.getRegionType().isOcean() || !Regions.containsBuilding(lastRegion, harbour)) {
        Region preLastRegion = path.get(path.size() - 2);
        returnDirection =
            data.getGameSpecificStuff().getMapMetric().getDirection(lastRegion, preLastRegion);
      }
      returnPath =
          Regions
              .planShipRoute(data, destination, returnDirection, ship.getRegion().getID(), speed);
      path.addAll(returnPath);
    }

    Costs shipCosts = new ShipCosts(ship, speed, harbour);

    // adjust cost function
    if (!useRange) {
      shipCosts = RoutePlanner.ZERO_COSTS;
    } else if (speed <= 0) {
      // couldn't determine ship range
      JOptionPane.showMessageDialog(ui, Resources
          .get("util.shiprouteplanner.msg.shiprangeiszero.text"), Resources
              .get("util.shiprouteplanner.msg.title"), JOptionPane.WARNING_MESSAGE);
      shipCosts = RoutePlanner.ZERO_COSTS;
    }

    // compute new orders
    List<String> orders = new LinkedList<String>();
    RoutePlanner.addOrders(orders, path, mode, useVorlage, shipCosts);
    return orders;
  }

  /**
   * A cost function that accounts ship movement, considering harbours and the ship given in the
   * constructor.
   *
   * @author stm
   */
  protected static class ShipCosts implements RoutePlanner.Costs {
    private BuildingType harbour;
    // private Ship ship;
    private int costs;
    private int speed;

    public ShipCosts(Ship ship, int speed, BuildingType harbour) {
      // this.ship = ship;
      this.harbour = harbour;
      this.speed = speed;
    }

    public void increase(Region region, Region region2) {
      if (region2.getRegionType().isOcean() || Regions.containsBuilding(region2, harbour)) {
        costs += 1;
      } else {
        costs = speed;
      }
    }

    public void reset() {
      costs = 0;
    }

    public boolean isExhausted() {
      return costs >= speed;
    }
  }

}
