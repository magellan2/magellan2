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

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import magellan.library.Region;
import magellan.library.gamebinding.EresseaConstants;
import magellan.library.utils.guiwrapper.RoutingDialogData;

public class RoutePlanner {

  /** A cost function which never exhausts. */
  public final static Costs ZERO_COSTS = new ZeroCosts();

  /**
   * Creates unit movement orders using the cost function provided by {@link #getCosts()}.
   * 
   * @param orders New orders will be appended to this list.
   * @param path The path to convert to orders
   * @param makeRoute ROUTE commands will be created if this is <code>true</code>.
   * @param useVorlage Vorlage meta orders will be created if this is <code>true</code>.
   * @param vorlageOffset This value will be added to the value of Vorlage "#after" orders.
   * @param A cost function specific to the kind of movement used.
   * @return Number of added order lines
   * @see RoutingDialogData
   */
  public static int addOrders(List<String> orders, List<Region> path, boolean makeRoute,
      boolean useVorlage, Costs costs) {
    // the movement command
    String localCommand;
    if (makeRoute) {
      localCommand = Resources.getOrderTranslation(EresseaConstants.O_ROUTE);
    } else {
      localCommand = Resources.getOrderTranslation(EresseaConstants.O_MOVE);
    }
    StringBuilder order = new StringBuilder();
    order.append(localCommand).append(" ");

    int size = orders.size();
    
    String temp = ""; // saves whether a closing bracket must be added: "}"

    List<Region> curPath = new LinkedList<Region>();

    // iterate through path
    for (Iterator<Region> iter = path.iterator(); iter.hasNext();) {
      curPath.add(iter.next());

      if (curPath.size() > 1) {
        // add one movement order
        String dir = Regions.getDirections(curPath);

        if (dir != null) {
          if (dir.length()==0 || costs.isExhausted()) {
            // begin new order
            costs.reset();

            if (useVorlage) {
              // create Vorlage meta order
              order.append(temp);
              orders.add(order.toString());
              order =
                  new StringBuilder("// #after ").append(orders.size()-size).append(" { ").append(localCommand)
                      .append(" ");
              temp = "}";
            } else {
              if (makeRoute) {
                // insert PAUSE
                order.append(Resources.getOrderTranslation(EresseaConstants.O_PAUSE)).append(" ");
              } else {
                // add new NACH order as comment
                orders.add(order.toString());
                order = new StringBuilder("// ").append(localCommand).append(" ");
              }
            }
          } else
            costs.increase(curPath.get(curPath.size() - 2), curPath.get(curPath.size() - 1));

          order.append(dir).append(" ");
        }

        curPath.remove(0);
      }
    }

    // add last order
    if (makeRoute) {
      // add PAUSE at end
      order.append(Resources.getOrderTranslation(EresseaConstants.O_PAUSE)).append(" ");
      order.append(temp);
      orders.add(order.toString());
    } else {
      order.append(temp);
      orders.add(order.toString());
    }

    return orders.size()-size;
  }

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
    }

    public boolean isExhausted() {
      return false;
    }

    public void reset() {
    }
  }

}