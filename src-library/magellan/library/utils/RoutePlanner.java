// class magellan.library.utils.RoutePlanner
// created on Feb 1, 2010
//
// Copyright 2003-2010 by magellan project team
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
package magellan.library.utils;

import java.util.LinkedList;
import java.util.List;

import magellan.library.Region;
import magellan.library.StringID;
import magellan.library.gamebinding.EresseaConstants;
import magellan.library.utils.guiwrapper.RoutingDialogData;

/**
 * Base class for route planners that show a dialog and add movement orders to units.
 * 
 * @author stm
 */
public class RoutePlanner {

  /** A cost function which never exhausts. */
  public final static Costs ZERO_COSTS = new ZeroCosts();
  /** mode for ROUTE routes */
  public static final int MODE_CONTINUOUS = 2;
  /** mode for there and back routes */
  public static final int MODE_RETURN = 4;
  /** mode for stop after route is finished */
  public static final int MODE_STOP = 8;

  /**
   * Creates unit movement orders using the cost function provided as argument.
   * 
   * @param orders New orders will be appended to this list.
   * @param path The path to convert to orders
   * @param mode a combination of {@link RoutePlanner#MODE_CONTINUOUS},
   *          {@link RoutePlanner#MODE_RETURN}, {@link RoutePlanner#MODE_STOP}
   * @param useVorlage <em>Vorlage</em> meta orders will be created if this is <code>true</code>.
   * @param costs A cost function specific to the kind of movement used.
   * @return Number of added order lines
   * @see RoutingDialogData
   */
  public static int addOrders(List<String> orders, List<Region> path, int mode, boolean useVorlage,
      Costs costs) {
    // the movement command
    String localCommand;
    if ((mode & MODE_CONTINUOUS) > 0) {
      localCommand = getOrderTranslation(EresseaConstants.O_ROUTE);
    } else {
      localCommand = getOrderTranslation(EresseaConstants.O_MOVE);
    }
    StringBuilder order = new StringBuilder();
    order.append(localCommand).append(" ");

    int size = orders.size();

    String closing = ""; // saves whether a closing bracket must be added: "}"

    List<Region> curPath = new LinkedList<Region>();

    // iterate through path
    for (Region region : path) {
      curPath.add(region);

      if (curPath.size() > 1) {
        // add one movement order
        String dir = Regions.getDirections(curPath);

        if (dir != null) {
          if (dir.length() == 0 || costs.isExhausted()) {
            // begin new order
            costs.reset();

            if (useVorlage) {
              // create Vorlage meta order
              order.append(closing);
              orders.add(order.toString());
              order =
                  new StringBuilder(EresseaConstants.OS_PCOMMENT).append(" #after ").append(
                      orders.size() - size).append(" { ").append(localCommand).append(" ");
              closing = "}";
            } else {
              if ((mode & MODE_CONTINUOUS) > 0) { // FIXME
                // insert PAUSE
                order.append(getOrderTranslation(EresseaConstants.O_PAUSE)).append(" ");
              } else {
                // add new NACH order as comment
                orders.add(order.toString());
                order =
                    new StringBuilder(EresseaConstants.OS_PCOMMENT).append(" ")
                        .append(localCommand).append(" ");
              }
            }
          } else {
            costs.increase(curPath.get(curPath.size() - 2), curPath.get(curPath.size() - 1));
          }

          order.append(dir).append(" ");
        }

        curPath.remove(0);
      }
    }

    // add last order
    if ((mode & MODE_CONTINUOUS) > 0) {
      // add PAUSE at end
      order.append(getOrderTranslation(EresseaConstants.O_PAUSE)).append(" ");
      if ((mode & MODE_STOP) > 0) {
        order.append(getOrderTranslation(EresseaConstants.O_PAUSE)).append(" ");
      }
      order.append(closing);
      orders.add(order.toString());
    } else {
      order.append(closing);
      orders.add(order.toString());
    }

    return orders.size() - size;
  }

  private static String getOrderTranslation(StringID orderId) {
    return Resources.getOrderTranslation(orderId.toString());
    // world.getRules().getGameSpecificStuff().getOrderChanger().getOrder(getLocale(), orderId);
  }

  /**
   * An abstract function that calculates route costs.
   */
  public interface Costs {
    /**
     * Set costs to zero, starting a new leg
     */
    public void reset();

    /**
     * Increase the costs after movement from region to region2.
     */
    public void increase(Region region, Region region2);

    /**
     * Return true if enough movement for one week has been added.
     */
    public boolean isExhausted();
  }

  /**
   * A cost function that never exhausts.
   */
  public static class ZeroCosts implements Costs {

    public void increase(Region region, Region region2) {
      // no costs
    }

    public boolean isExhausted() {
      return false;
    }

    public void reset() {
      // nothing to do
    }
  }

}