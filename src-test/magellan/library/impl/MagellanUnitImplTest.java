// class magellan.library.impl.MagellanUnitImplTest
// created on Mar 6, 2022
//
// Copyright 2003-2022 by magellan project team
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

import magellan.library.Faction;
import magellan.library.GameData;
import magellan.library.Region;
import magellan.library.Skill;
import magellan.library.Unit;
import magellan.library.gamebinding.EresseaRelationFactory;
import magellan.library.relation.PersonTransferRelation;
import magellan.test.GameDataBuilder;

public class MagellanUnitImplTest {

  private GameData data;
  private GameDataBuilder builder;
  private Faction faction;
  private Region region;

  @Before
  public void setUp() throws Exception {
    builder = new GameDataBuilder();
    data = builder.createSimplestGameData(1300);
    faction = data.getFactions().iterator().next();
    region = data.getRegions().iterator().next();

    EresseaRelationFactory relationFactory = ((EresseaRelationFactory) data.getGameSpecificStuff()
        .getRelationFactory());
    relationFactory.stopUpdating();
  }

  @Test
  public void testSkill0Persons() {
    Unit unit = builder.addUnit(data, "src", "Source", faction, region);
    Skill skill = builder.addSkill(unit, "Ausdauer", 5);
    PersonTransferRelation rel = new PersonTransferRelation(unit, null, unit.getPersons(), unit.getRace(), 1);
    // rel.setSkills(unit);
    assertEquals(5, unit.getModifiedSkill(skill.getSkillType()).getLevel());
    rel.add();
    assertEquals(null, unit.getModifiedSkill(skill.getSkillType()));
  }

  @Test
  public void testSkillTransfer() {
    // FIXME the current implementation doesn't scale well if there are many person transfer operations
    // because modified skills are calculated from scratch in every step.
    MagellanUnitImpl unit = (MagellanUnitImpl) builder.addUnit(data, "src", "Source", faction, region);

    unit.setPersons(2);
    MagellanUnitImpl target = (MagellanUnitImpl) builder.addUnit(data, "trg", "Target", faction, region);

    // 5 + 0 = 2x3
    Skill skill = builder.addSkill(unit, "Ausdauer", 5);
    assertEquals(0, skill.getModifier(unit));
    PersonTransferRelation rel = new PersonTransferRelation(unit, target, 1, unit.getRace(), 1);
    // rel.setSkills(unit);
    assertEquals(5, unit.getModifiedSkill(skill.getSkillType()).getLevel());
    rel.add();
    assertEquals(5, unit.getModifiedSkill(skill.getSkillType()).getLevel());
    assertEquals(3, target.getModifiedSkill(skill.getSkillType()).getLevel());

    // transfer second person: 2x3 + 10 = 3x6
    skill.setLevel(10);
    // invalidate to reset modifiedSkills
    unit.invalidateCache(false);
    target.invalidateCache(false);
    assertEquals(10, unit.getModifiedSkill(skill.getSkillType()).getLevel());
    PersonTransferRelation rel2 = new PersonTransferRelation(unit, target, 1, unit.getRace(), 1);
    // rel2.setSkills(unit);
    rel2.add();
    // unit.invalidateCache(false);
    // target.invalidateCache(false);
    assertEquals(null, unit.getModifiedSkill(skill.getSkillType()));
    assertEquals(6, target.getModifiedSkill(skill.getSkillType()).getLevel());
  }

  @Test
  public void testSkillTransferRace() {
    // FIXME the current implementation doesn't scale well if there are many person transfer operations
    // because modified skills are calculated from scratch in every step.
    MagellanUnitImpl unit = (MagellanUnitImpl) builder.addUnit(data, "src", "Source", faction, region);

    unit.setPersons(2);
    MagellanUnitImpl target = (MagellanUnitImpl) builder.addUnit(data, "trg", "Target", faction, region);

    // (2+3) + 0 = 2x(1+3)
    Skill skill = builder.addSkill(unit, "Segeln", 5);
    assertEquals(3, skill.getModifier(unit));
    PersonTransferRelation rel = new PersonTransferRelation(unit, target, 1, unit.getRace(), 1);
    // rel.setSkills(unit);
    assertEquals(5, unit.getModifiedSkill(skill.getSkillType()).getLevel());
    rel.add();
    assertEquals(5, unit.getModifiedSkill(skill.getSkillType()).getLevel());
    assertEquals(4, target.getModifiedSkill(skill.getSkillType()).getLevel());

    // transfer second person: 2x(1+3) + (7+3) = 3x(4+3)
    skill.setLevel(10);
    // invalidate to reset modifiedSkills
    unit.invalidateCache(false);
    target.invalidateCache(false);
    assertEquals(10, unit.getModifiedSkill(skill.getSkillType()).getLevel());
    PersonTransferRelation rel2 = new PersonTransferRelation(unit, target, 1, unit.getRace(), 1);
    // rel2.setSkills(unit);
    rel2.add();
    // unit.invalidateCache(false);
    // target.invalidateCache(false);
    assertEquals(null, unit.getModifiedSkill(skill.getSkillType()));
    assertEquals(7, target.getModifiedSkill(skill.getSkillType()).getLevel());
  }

  @Test
  public void testSkillTransfer2() {
    MagellanUnitImpl unit = (MagellanUnitImpl) builder.addUnit(data, "src", "Source", faction, region);

    // unit.setPersons(0);
    MagellanUnitImpl target = (MagellanUnitImpl) builder.addUnit(data, "trg", "Target", faction, region);
    target.setPersons(0);

    Skill skill = builder.addSkill(unit, "Ausdauer", 5);

    PersonTransferRelation rel = new PersonTransferRelation(unit, target, 0, unit.getRace(), 0);
    assertEquals(5, unit.getModifiedSkill(skill.getSkillType()).getLevel());
    rel.add();
    assertEquals(5, unit.getModifiedSkill(skill.getSkillType()).getLevel());
    assertEquals(null, target.getModifiedSkill(skill.getSkillType()));
  }

  @Test
  public void testSkillTransferZero2() {
    MagellanUnitImpl unit = (MagellanUnitImpl) builder.addUnit(data, "src", "Source", faction, region);
    unit.setPersons(0);

    MagellanUnitImpl target = (MagellanUnitImpl) builder.addUnit(data, "trg", "Target", faction, region);

    Skill skill = builder.addSkill(unit, "Ausdauer", 5);

    PersonTransferRelation rel = new PersonTransferRelation(unit, target, 0, unit.getRace(), 0);
    assertEquals(5, unit.getModifiedSkill(skill.getSkillType()).getLevel());
    rel.add();
    assertEquals(null, unit.getModifiedSkill(skill.getSkillType()));
    assertEquals(null, target.getModifiedSkill(skill.getSkillType()));
  }

}
