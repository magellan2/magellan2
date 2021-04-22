// class magellan.library.utils.ShipRoutePlannerTest
// created on Dec 25, 2017
//
// Copyright 2003-2017 by magellan project team
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
package magellan.library.utils;

import static org.junit.Assert.assertEquals;

import java.util.Collection;

import org.junit.Test;

import magellan.library.CoordinateID;
import magellan.library.GameData;
import magellan.library.Region;
import magellan.library.Ship;
import magellan.library.TrustLevel;
import magellan.library.Unit;
import magellan.library.utils.guiwrapper.RoutingDialogData;
import magellan.library.utils.guiwrapper.RoutingDialogDataPicker;
import magellan.test.GameDataBuilder;
import magellan.test.MagellanTestWithResources;

/**
 * @author stm
 */
public class ShipRoutePlannerTest extends MagellanTestWithResources {

  private GameDataBuilder builder;
  private GameData data;
  private Unit captain;
  private Ship ship;
  private ShipRoutePlanner planner;

  /**
   * @throws Exception
   */
  @Test
  public void testPlanShipRoute() throws Exception {
    createRoute();

    Unit u = planner.planShipRoute(ship, data, null, getPicker(CoordinateID.create(1, 0)));
    assertEquals(captain, u);
    assertEquals(1, u.getOrders2().size());
    assertEquals("ROUTE O PAUSE", u.getOrders2().get(0).toString().trim());
  }

  /**
   * @throws Exception
   */
  @Test
  public void testPlanShipRoute2() throws Exception {
    createRoute();

    captain.addOrder("ATTACKIERE 123");
    captain.addOrder("ROUTE NW");

    Unit u = planner.planShipRoute(ship, data, null, getPicker(CoordinateID.create(1, 0)));
    assertEquals(captain, u);
    assertEquals(3, u.getOrders2().size());
    assertEquals("ATTACKIERE 123", u.getOrders2().get(0).toString().trim());
    assertEquals("; ROUTE NW", u.getOrders2().get(1).toString().trim());
    assertEquals("ROUTE O PAUSE", u.getOrders2().get(2).toString().trim());
  }

  private void createRoute() throws Exception {
    planner = new ShipRoutePlanner();
    builder = new GameDataBuilder();

    data = builder.createSimplestGameData();
    captain = data.getUnits().iterator().next();
    Region r = captain.getRegion();

    builder.addRegion(data, "1,0", "Ozean", "Ozean", 1);
    ship = builder.addShip(data, r, "home", "Langboot", "Home", 5);
    captain.setShip(ship);
    ship.setOwner(captain);
    captain.getFaction().setTrustLevel(TrustLevel.TL_PRIVILEGED);
    captain.clearOrders();

  }

  private RoutingDialogDataPicker getPicker(final CoordinateID target) {
    return new RoutingDialogDataPicker() {

      public RoutingDialogData showRoutingDialog() {
        RoutingDialogData result = new RoutingDialogData() {

          public boolean useVorlage() {
            return false;
          }

          public boolean useRange() {
            return false;
          }

          public boolean replaceOrders() {
            return false;
          }

          public int getMode() {
            return RoutePlanner.MODE_CONTINUOUS;
          }

          public CoordinateID getDestination() {
            return target;
          }
        };
        return result;
      }

      public void initialize(GameData data, Collection<Region> coast, boolean excludeUnnamed) {
        // empty
      }
    };
  }

}
