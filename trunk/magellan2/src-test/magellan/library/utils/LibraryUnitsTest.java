// class magellan.library.utils.LibraryUnitsTest
// created on Mar 1, 2011
//
// Copyright 2003-2011 by magellan project team
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

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import magellan.library.AllianceGroup;
import magellan.library.EntityID;
import magellan.library.Faction;
import magellan.library.GameData;
import magellan.library.Unit;
import magellan.library.event.GameDataEvent;
import magellan.library.gamebinding.EresseaConstants;
import magellan.test.GameDataBuilder;

import org.junit.Before;
import org.junit.Test;

/**
 * Tests for {@link magellan.library.utils.Units}
 * 
 * @author ...
 * @version 1.0, Mar 1, 2011
 */
public class LibraryUnitsTest {

  private GameDataBuilder builder;
  private GameData gd01;
  private Unit unit011;
  private Faction faction011;
  private Faction faction012;

  /**
   * @throws java.lang.Exception
   */
  @Before
  public void setUp() throws Exception {
    builder = new GameDataBuilder();
    gd01 = builder.createSimpleGameData("e3", 350, true);
    unit011 = gd01.getUnits().iterator().next();
    faction011 = unit011.getFaction();
    faction012 = builder.addFaction(gd01, "zwei", "Zwei", "Mensch", 2);

    Units.getGameDataListener().gameDataChanged(new GameDataEvent(this, gd01));
  }

  /**
   * Test method for
   * {@link magellan.library.utils.Units#isAllied(magellan.library.Faction, magellan.library.Faction, int, boolean)}
   * .
   */
  @Test
  public final void testIsAlliedFactionFactionIntBoolean() {
    int guState = EresseaConstants.A_GUARD;
    int gvState = EresseaConstants.A_GIVE;
    int cState = EresseaConstants.A_COMBAT;
    int ggState = guState | gvState;
    int cgState = cState | gvState;

    builder.addAlliance(faction011, faction012, guState);

    assertTrue(Units.isAllied(faction011, faction012, guState, false));
    assertTrue(Units.isAllied(faction011, faction012, guState, true));
    assertFalse(Units.isAllied(faction011, faction012, gvState, false));
    assertFalse(Units.isAllied(faction011, faction012, gvState, true));
    assertFalse(Units.isAllied(faction011, faction012, ggState));
    assertFalse(Units.isAllied(faction011, faction012, ggState, false));
    assertTrue(Units.isAllied(faction011, faction012, ggState, true));

    builder.addAlliance(faction011, faction012, ggState);

    assertTrue(Units.isAllied(faction011, faction012, guState, false));
    assertTrue(Units.isAllied(faction011, faction012, guState, true));
    assertTrue(Units.isAllied(faction011, faction012, gvState, false));
    assertTrue(Units.isAllied(faction011, faction012, gvState, true));
    assertTrue(Units.isAllied(faction011, faction012, ggState, false));
    assertTrue(Units.isAllied(faction011, faction012, ggState, true));

    AllianceGroup alliance = new AllianceGroup(EntityID.createEntityID("alli", 36), "Allianz 1");
    alliance.addFaction(faction011);
    alliance.addFaction(faction012);
    faction011.setAlliance(alliance);

    assertTrue(Units.isAllied(faction011, faction012, guState, false));
    assertTrue(Units.isAllied(faction011, faction012, guState, true));
    assertTrue(Units.isAllied(faction011, faction012, gvState, false));
    assertTrue(Units.isAllied(faction011, faction012, gvState, true));
    assertTrue(Units.isAllied(faction011, faction012, cState, true));
    assertTrue(Units.isAllied(faction011, faction012, cState, false));
    assertTrue(Units.isAllied(faction011, faction012, ggState, false));
    assertTrue(Units.isAllied(faction011, faction012, ggState, true));
    assertTrue(Units.isAllied(faction011, faction012, cgState, false));
    assertTrue(Units.isAllied(faction011, faction012, cgState, true));

  }

}
