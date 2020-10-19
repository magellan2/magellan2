// class magellan.library.gamebinding.EresseaExecutionStateTest
// created on Oct 12, 2020
//
// Copyright 2003-2020 by magellan project team
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
package magellan.library.gamebinding;

import static org.junit.Assert.assertEquals;

import java.util.List;

import org.junit.Before;
import org.junit.Test;

import magellan.library.Faction;
import magellan.library.GameData;
import magellan.library.Region;
import magellan.library.Unit;
import magellan.library.gamebinding.EresseaRelationFactory.EresseaExecutionState;
import magellan.library.relation.ReserveRelation;
import magellan.library.relation.TransferRelation;
import magellan.library.relation.UnitRelation;
import magellan.library.rules.ItemType;
import magellan.test.GameDataBuilder;

public class EresseaExecutionStateTest {

  private GameDataBuilder builder;
  private GameData data;
  private Unit unit;
  private Region region0;
  private EresseaRelationFactory relationFactory;
  private Faction faction0;
  private ItemType silverType;

  /**
   * @throws Exception
   */
  @Before
  public void setUp() throws Exception {
    builder = new GameDataBuilder();
    data = builder.createSimpleGameData();
    unit = data.getUnits().iterator().next();
    faction0 = unit.getFaction();
    region0 = data.getRegions().iterator().next();
    relationFactory = ((EresseaRelationFactory) data.getGameSpecificStuff().getRelationFactory());
    relationFactory.stopUpdating();
    silverType = data.getRules().getItemType(EresseaConstants.I_USILVER);
  }

  @Test
  public void testReserve() {

    EresseaExecutionState state = new EresseaRelationFactory.EresseaExecutionState(data);
    // RESERVE
    builder.addItem(data, unit, "Silber", 2);

    List<UnitRelation> relations = state.acquireItem(unit, silverType, 1, false, true, false, 0, null);
    add(unit, relations);
    List<UnitRelation> relations2 = null;
    for (int i = 0; i < 10; ++i) {
      relations2 = state.acquireItem(unit, silverType, 1, false, true, false, 0, null);
    }
    List<UnitRelation> relations3 = state.acquireItem(unit, silverType, 2, false, true, false, 0, null);
    List<UnitRelation> relations4 = state.acquireItem(unit, silverType, 4, false, true, false, 0, null);

    assertEquals(1, relations.size());
    assertReserve(relations, 0, 1);
    assertEquals(1, relations2.size());
    assertReserve(relations2, 0, 1);
    assertEquals(1, relations3.size());
    assertReserve(relations3, 0, 2);
    assertEquals(1, relations4.size());
    assertReserve(relations4, 0, 2, false);
  }

  @Test
  public void testGive() {
    EresseaExecutionState state = new EresseaRelationFactory.EresseaExecutionState(data);
    // RESERVE
    builder.addItem(data, unit, "Silber", 2);

    List<UnitRelation> relations = state.acquireItem(unit, silverType, 1, false, true, false, 0, null);
    add(unit, relations);
    List<UnitRelation> relations2 = state.acquireItem(unit, silverType, 1, false, false, false, 0, null);

    List<UnitRelation> relations3 = state.acquireItem(unit, silverType, 2, false, false, false, 0, null);

    assertEquals(1, relations.size());
    assertReserve(relations, 0, 1);
    assertEquals(1, relations2.size());
    assertReserve(relations2, 0, 1);
    assertEquals(1, relations3.size());
    assertReserve(relations3, 0, 1, false);
  }

  private void add(Unit unit, List<UnitRelation> relations) {
    for (UnitRelation rel : relations) {
      unit.addRelation(rel);
    }
  }

  private void assertTransfer(Unit source, int pos1, Unit target, int pos2, int amount) {
    TransferRelation transfer = (TransferRelation) source.getRelations().get(pos1);
    assertEquals(amount, transfer.amount);
    assertEquals(source, transfer.source);
    assertEquals(target, transfer.target);
    transfer = (TransferRelation) target.getRelations().get(pos2);
    assertEquals(amount, transfer.amount);
    assertEquals(source, transfer.source);
    assertEquals(target, transfer.target);
  }

  private void assertReserve(List<UnitRelation> relations, int pos, int amount) {
    assertReserve(relations, pos, amount, true);
  }

  private void assertReserve(List<UnitRelation> relations, int pos, int amount, boolean okay) {
    ReserveRelation reserve = (ReserveRelation) relations.get(pos);
    assertEquals(amount, reserve.amount);
    assertEquals(okay, reserve.problem == null);
  }

}
