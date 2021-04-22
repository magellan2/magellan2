// class magellan.library.utils.TrustLevelsTest
// created on Apr 21, 2021
//
// Copyright 2003-2021 by magellan project team
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
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;

import magellan.library.AllianceGroup;
import magellan.library.Faction;
import magellan.library.GameData;
import magellan.library.Unit;
import magellan.library.gamebinding.EresseaConstants;
import magellan.library.gamebinding.EresseaRelationFactory;
import magellan.test.GameDataBuilder;
import magellan.test.MagellanTestWithResources;

/**
 */
public class TrustLevelsTest extends MagellanTestWithResources {
  private GameDataBuilder builder;
  private GameData data;
  private Unit unit;
  private EresseaRelationFactory relationFactory;
  private Faction faction0;

  /**
   * @throws Exception
   */
  @Before
  public void setUp() throws Exception {
    builder = new GameDataBuilder();
    data = builder.createSimpleGameData();
    unit = data.getUnits().iterator().next();
    faction0 = unit.getFaction();
    relationFactory = ((EresseaRelationFactory) data.getGameSpecificStuff().getRelationFactory());
    relationFactory.stopUpdating();
  }

  @Test
  public void testRecalculateTrustLevels() {
    Faction unknown = builder.addUnknownFaction(data);
    Faction monster = builder.addMonsterFaction(data);
    Faction monster0 = builder.addFaction(data, "0", "Monster alt", "Mensch", 4);
    monster0.setPassword(null);

    Faction faction2 = builder.addFaction(data, "two", null, "Mensch", 10);
    Faction faction3 = builder.addFaction(data, "tres", null, "Mensch", 10);
    faction3.setTrustLevel(42);
    faction3.setTrustLevelSetByUser(true);

    Faction ally = builder.addFaction(data, "guar", null, "Mensch", 10);
    Faction ally2 = builder.addFaction(data, "alli", null, "Mensch", 10);

    builder.addAlliance(faction0, ally, EresseaConstants.A_GUARD);

    AllianceGroup alliance = builder.addAlliance(data, faction0);
    builder.addToAlliance(alliance, ally2);

    TrustLevels.recalculateTrustLevels(data);
    assertEquals(0, faction2.getTrustLevel());
    assertEquals(42, faction3.getTrustLevel());
    assertEquals(100, faction0.getTrustLevel());
    assertTrue(TrustLevels.isMonstrous(monster));
    assertEquals(-20, unknown.getTrustLevel());
    assertTrue(TrustLevels.isAlly(ally));
    assertTrue(TrustLevels.isAlly(ally2));
    assertTrue(TrustLevels.isMonstrous(monster0));
  }

  @Test
  public void testContainsTrustLevelsSetByUser() {
    assertFalse(TrustLevels.containsTrustLevelsSetByUser(data));
    faction0.setTrustLevelSetByUser(true);
    assertTrue(TrustLevels.containsTrustLevelsSetByUser(data));
  }

  @Test
  public void testIsPrivileged() {
    faction0.setTrustLevel(100);
    assertTrue(TrustLevels.isPrivileged(faction0));
    faction0.setTrustLevel(99);
    assertFalse(TrustLevels.isPrivileged(faction0));
  }

  @Test
  public void testIsHostile() {
    faction0.setTrustLevel(-1);
    assertTrue(TrustLevels.isHostile(faction0));
    faction0.setTrustLevel(0);
    assertFalse(TrustLevels.isHostile(faction0));
  }

  @Test
  public void testIsAlly() {
    faction0.setTrustLevel(1);
    assertTrue(TrustLevels.isAlly(faction0));
    faction0.setTrustLevel(0);
    assertFalse(TrustLevels.isAlly(faction0));
  }

  @Test
  public void testIsMonstrous() {
    faction0.setTrustLevel(Integer.MIN_VALUE);
    assertTrue(TrustLevels.isMonstrous(faction0));
    faction0.setTrustLevel(-999);
    assertFalse(TrustLevels.isMonstrous(faction0));
  }

}
