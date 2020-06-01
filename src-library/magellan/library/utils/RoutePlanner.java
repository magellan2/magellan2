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
      localCommand = getOrderTranslation(EresseaConstants.OC_ROUTE);
    } else {
      localCommand = getOrderTranslation(EresseaConstants.OC_MOVE);
    }

    int size = orders.size();

    StringBuilder order = new StringBuilder();
    LinkedList<Region> curPath = new LinkedList<Region>();
    int line = 0;

    Region lastRegion = null;
    // iterate through path
    for (Region region : path) {
      if (curPath.size() < 1) {
        curPath.add(region);
      } else {
        curPath.add(region);
        if (costs.isExhausted(curPath) || region == lastRegion) {
          Region removed = null;
          if (curPath.size() > 2) {
            removed = curPath.removeLast();
          }
          getOrder(order, curPath, localCommand, mode, useVorlage, line, false);
          if ((mode & MODE_CONTINUOUS) == 0 || useVorlage) {
            orders.add(order.toString());
            order.setLength(0);
            ++line;
          }

          Region last = curPath.getLast();
          curPath.clear();
          curPath.add(last);
          if (removed != null && removed != last) {
            curPath.add(removed);
          }
        }
      }
      lastRegion = region;
    }
    if (curPath.size() > 1) {
      getOrder(order, curPath, localCommand, mode, useVorlage, line++, true);
      orders.add(order.toString());
    }

    return orders.size() - size;
  }

  private static void getOrder(StringBuilder order, LinkedList<Region> curPath, String localCommand, int mode,
      boolean useVorlage, int line, boolean last) {
    int step = 0;
    // for(Region r: curPath) {
    // if (step==0) {
    // start command
    if (line == 0 && order.length() == 0) {
      order.append(localCommand);
    } else if (useVorlage) {
      order.append(EresseaConstants.O_PCOMMENT).append(" #after ").append(line).append(" { ").append(localCommand);
    } else if ((mode & MODE_CONTINUOUS) > 0) {
      // order.append(" ").append(getOrderTranslation(EresseaConstants.OC_PAUSE));
    } else {
      // add new NACH order as comment
      order.append(EresseaConstants.O_PCOMMENT).append(" ").append(localCommand);
    }
    // } else {
    String dir = Regions.getDirections(curPath);
    order.append(" ").append(dir);
    // }
    if ((mode & MODE_CONTINUOUS) > 0) {
      // add PAUSE at end
      order.append(" ").append(getOrderTranslation(EresseaConstants.OC_PAUSE));
    }
    if (last && (mode & MODE_STOP) > 0) {
      order.append(" ").append(getOrderTranslation(EresseaConstants.OC_PAUSE));
    }
    if (useVorlage && line > 0) {
      order.append(" }");
    }
  }

  private static String getOrderTranslation(StringID orderId) {
    return Resources.getOrderTranslation(orderId.toString());
    // world.getGameSpecificStuff().getOrderChanger().getOrder(getLocale(), orderId);
  }

  /**
   * An abstract function that calculates route costs.
   */
  public interface Costs {
    public boolean isExhausted(LinkedList<Region> curPath);
  }

  /**
   * A cost function that never exhausts.
   */
  public static class ZeroCosts implements Costs {

    public boolean isExhausted(LinkedList<Region> curPath) {
      return false;
    }
  }

}