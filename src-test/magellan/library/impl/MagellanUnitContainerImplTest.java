// class magellan.library.impl.MagellanIdentifiableImplTest
// created on Aug 7, 2010
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
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU General Public License for more details.
//
// You should have received a copy of the GNU General Public License
// along with this program (see doc/LICENCE.txt); if not, write to the
// Free Software Foundation, Inc.,
// 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
//
package magellan.library.impl;

import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;

import magellan.library.EntityID;
import magellan.library.GameData;
import magellan.library.UnitID;
import magellan.test.GameDataBuilder;
import magellan.test.MagellanTestWithResources;

/**
 * Test class for {@link MagellanIdentifiableImpl} (mainly compare/equals).
 * 
 * @author stm
 */
public class MagellanUnitContainerImplTest extends MagellanTestWithResources {

  private MagellanUnitImpl unit1;
  private MagellanUnitImpl unit2;
  private MagellanUnitImpl unit3;
  private MagellanUnitImpl unit4;
  private GameData data;

  /**
   * @throws java.lang.Exception
   */
  @Before
  public void setUp() throws Exception {
    data = new GameDataBuilder().createSimplestGameData();
    unit1 = new MagellanUnitImpl(UnitID.createUnitID(41, 36), data);
    unit1.setPersons(1);
    unit2 = new MagellanUnitImpl(UnitID.createUnitID(42, 36), data);
    unit2.setPersons(2);
    unit3 = new MagellanUnitImpl(UnitID.createUnitID(43, 36), data);
    unit3.setPersons(3);
    unit4 = new MagellanUnitImpl(UnitID.createUnitID(44, 36), data);
    unit4.setPersons(4);
  }

  /**
   * Test the unitCount() method.
   */
  @Test
  public final void testUnitCounts() {
    MagellanBuildingImpl building = new MagellanBuildingImpl(EntityID.createEntityID(99, 36), data);
    building.addUnit(unit1);
    building.addUnit(unit2);
    building.addUnit(unit3);
    building.addUnit(unit4);
    assertEquals(10, building.personCount());
  }

  /**
   * Test the modifiedUnitsCount() method.
   */
  @Test
  public final void testModifiedUnitCounts() {
    MagellanBuildingImpl building = new MagellanBuildingImpl(EntityID.createEntityID(99, 36), data);
    building.addUnit(unit1);
    building.addUnit(unit2);
    building.addUnit(unit3);
    building.addUnit(unit4);
    assertEquals(10, building.modifiedPersonCount());
    MagellanUnitImpl unit5 = new MagellanUnitImpl(UnitID.createUnitID(45, 36), data);
    unit5.setPersons(5);
    building.enter(unit5);
    assertEquals(15, building.modifiedPersonCount());
    assertEquals(10, building.personCount());
  }

}
