// class magellan.library.gamebinding.AllanonGameSpecificRules
// created on 19.04.2009
//
// Copyright 2003-2009 by magellan project team
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
package magellan.library.gamebinding;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Set;

import magellan.library.Alliance;
import magellan.library.Building;
import magellan.library.EntityID;
import magellan.library.Faction;
import magellan.library.ID;
import magellan.library.Order;
import magellan.library.Region;
import magellan.library.Rules;
import magellan.library.Ship;
import magellan.library.Skill;
import magellan.library.StringID;
import magellan.library.Unit;
import magellan.library.gamebinding.e3a.MaintainOrder;
import magellan.library.impl.MagellanUnitImpl;
import magellan.library.relation.PersonTransferRelation;
import magellan.library.relation.RecruitmentRelation;
import magellan.library.relation.UnitRelation;
import magellan.library.rules.CastleType;
import magellan.library.rules.ItemType;
import magellan.library.rules.Race;
import magellan.library.rules.RegionType;
import magellan.library.rules.SkillType;
import magellan.library.rules.UnitContainerType;
import magellan.library.utils.comparator.LinearUnitTempUnitComparator;
import magellan.library.utils.comparator.SortIndexComparator;
import magellan.library.utils.logging.Logger;

/**
 * This class implements all Eressea specific rule informations.
 *
 * @author Thoralf Rickert
 * @version 1.0, 19.04.2009
 */
public class EresseaGameSpecificRules implements GameSpecificRules {

  private static Logger log = Logger.getInstance(EresseaGameSpecificRules.class);

  private Rules rules;

  protected EresseaGameSpecificRules(Rules rules) {
    this.rules = rules;
  }

  /**
   * Returns the value of rules.
   *
   * @return Returns rules.
   */
  public Rules getRules() {
    return rules;
  }

  /**
   * @see magellan.library.gamebinding.GameSpecificRules#getMaxWorkers(magellan.library.Region)
   */
  public Integer getMaxWorkers(Region region) {

    if ((region.getTrees() != -1) && (region.getSprouts() != -1) && (region.getType() != null)) {
      int inhabitants = ((RegionType) region.getType()).getInhabitants();
      int trees = Math.max(region.getTrees(), 0);
      int sprouts = Math.max(region.getSprouts(), 0);

      return Integer.valueOf(Math.max(0, inhabitants - (8 * trees) - (4 * sprouts)));
    }

    return null;
  }

  /**
   * @see magellan.library.gamebinding.GameSpecificRules#getMaxEntertain(magellan.library.Region)
   */
  public Integer getMaxEntertain(Region region) {
    if (region.getEntertain() >= 0)
      return region.getEntertain();
    return maxEntertain(region.getSilver());
  }

  /**
   * @see magellan.library.gamebinding.GameSpecificRules#getMaxOldEntertain(magellan.library.Region)
   */
  public Integer getMaxOldEntertain(Region region) {
    if (region.getOldEntertain() >= 0)
      return region.getOldEntertain();
    return maxEntertain(region.getOldSilver());
  }

  /**
   * Return the silver that can be earned through entertainment in a region with the given amount of
   * silver.
   */
  private int maxEntertain(int silver) {
    if (silver >= 0)
      return silver / 20;
    return -1;
  }

  /**
   * @see magellan.library.gamebinding.GameSpecificRules#isShip(magellan.library.Ship)
   */
  public boolean isShip(Ship ship) {
    // in Eressea is a ship a ship.
    return true;
  }

  /**
   * @see magellan.library.gamebinding.GameSpecificRules#canLandInRegion(magellan.library.Ship,
   *      magellan.library.Region)
   */
  public boolean canLandInRegion(Ship ship, Region region) {
    if (region.getType().getID().equals(EresseaConstants.RT_FIREWALL))
      return false;

    if (ship.getType().getID().equals(EresseaConstants.ST_BOAT))
      return true; // can land everywhere

    if (region.getType().getID().equals(EresseaConstants.RT_PLAIN))
      return true;
    if (region.getType().getID().equals(EresseaConstants.RT_FOREST))
      return true;
    if (region.getType().getID().equals(EresseaConstants.RT_OCEAN))
      return true;

    return false;
  }

  /**
   * Calculates the wage for the units of a certain faction in the specified region.
   */
  public int getWage(Region region, Race race) {
    int wage = region.getWage() - 1;
    if (getRules().getRace(EresseaConstants.R_ORKS) == null
        || race.getID().equals(getRules().getRace(EresseaConstants.R_ORKS).getID())) {
      switch (wage) {
      case 12:
        wage = 11;

        break;

      case 13:
        wage = 12;

        break;

      case 14:
        wage = 12;

        break;

      case 15:
        wage = 13;

        break;
      }
    }

    return wage;
  }

  public int getShipRange(Ship s) {
    if (s.getSpeed() != -1 && s.getModifiedOwnerUnit() == s.getOwnerUnit())
      return s.getSpeed();

    // Reichweite (bei Schaden aufrunden)
    int rad = s.getShipType().getRange();

    if (s.getShipType().getRangeFormula() != null) {
      if (s.getShipType().getRangeFormula().equals(
          "$range + max(0, log_3(($currentcaptainlevel / $captainlevel)))")) {
        if (s.getModifiedOwnerUnit() != null) {
          Skill sailing =
              s.getModifiedOwnerUnit().getSkill(getRules().getSkillType(EresseaConstants.S_SEGELN));
          if (sailing != null) {
            for (int sail = Math.max(0, (sailing.getLevel() / s.getShipType()
                .getCaptainSkillLevel())); sail >= 3; sail /= 3) {
              ++rad;
            }
          }
        }
      } else if (s.getShipType().getRangeFormula().equals(
          "$range + max(0, ($currentcaptainlevel - (1 + $captainlevel) / 2) / 6)")) {
        if (s.getModifiedOwnerUnit() != null) {
          // Kapitäne erhalten einen Bonus von +1 auf die Reichweite ihrer Schiffe für je 6 Stufen
          // Segeln-Talent über dem halben Mindest-Kapitänstalent (aufgerundet, siehe Tabelle).
          // add +1 to speed for every six levels over half (rounded up!) minimum captain speed
          Skill sailing =
              s.getModifiedOwnerUnit().getSkill(getRules().getSkillType(EresseaConstants.S_SEGELN));
          if (sailing != null) {
            rad +=
                Math.max(0, (sailing.getLevel() - (1 + s.getShipType().getCaptainSkillLevel()) / 2)
                    / 6);
          }
        }
      } else {
        log.fine("unknown ship type formula " + s.getShipType().getRangeFormula() + " in " + s
            .getShipType().getName());
      }
    }

    if ((s.getModifiedOwnerUnit() != null) && (s.getModifiedOwnerUnit().getRace() != null)
        && s.getModifiedOwnerUnit().getRace().getAdditiveShipBonus() != 0) {
      rad += s.getModifiedOwnerUnit().getRace().getAdditiveShipBonus();
    }

    // rad = rad*(100.0-damageRatio)/100.0
    rad =
        new BigDecimal(rad).multiply(new BigDecimal(100 - s.getDamageRatio())).divide(
            new BigDecimal(100), BigDecimal.ROUND_UP).intValue();

    return rad;
  }

  /**
   * @see magellan.library.gamebinding.GameSpecificRules#getPeasantMaintenance(magellan.library.Region)
   */
  public int getPeasantMaintenance(Region region) {
    return 10;
  }

  /**
   * @see magellan.library.gamebinding.GameSpecificRules#getTeachFactor()
   */
  public int getTeachFactor() {
    return 10;
  }

  public int getSilverPerWeightUnit() {
    return 100;
  }

  /**
   * Returns true if the type is a castle (Befestigung, Turm, ...)
   *
   * @see magellan.library.gamebinding.GameSpecificRules#isCastle(magellan.library.rules.UnitContainerType)
   */
  public boolean isCastle(UnitContainerType type) {
    return type instanceof CastleType;
  }

  public int getMaxHorsesWalking(Unit u) {
    int skillLevel = 0;
    Skill s = u.getModifiedSkill(getRules().getSkillType(EresseaConstants.S_REITEN, true));

    if (s != null) {
      skillLevel = s.getLevel();
    }
    return ((skillLevel * u.getModifiedPersons() * 4) + u.getModifiedPersons());
  }

  public int getMaxHorsesRiding(Unit u) {
    int skillLevel = 0;
    Skill s = u.getModifiedSkill(getRules().getSkillType(EresseaConstants.S_REITEN, true));

    if (s != null) {
      skillLevel = s.getLevel();
    }
    return (skillLevel * u.getModifiedPersons() * 2);
  }

  /**
   * @see magellan.library.gamebinding.GameSpecificRules#isPooled(magellan.library.Unit,
   *      magellan.library.StringID)
   */
  public boolean isPooled(Unit unit, StringID type) {
    if (unit.getData().getDate().getDate() > 558
        && getRules().getGameSpecificStuff().getName().equalsIgnoreCase("eressea"))
      // the pools were activated in Eressea starting from report no. 559
      return true;
    if (unit.getFaction() == null || unit.getFaction().getOptions() == null) {
      log.fine("Don't know if pool is active for " + unit);
      return false;
    }
    if (EresseaConstants.I_USILVER.equals(type))
      return unit.getFaction().getOptions() != null
          && unit.getFaction().getOptions().isActive(EresseaConstants.OC_SILVERPOOL);
    else
      return unit.getFaction().getOptions().isActive(EresseaConstants.OC_ITEMPOOL);
  }

  /**
   * @see magellan.library.gamebinding.GameSpecificRules#isPooled(magellan.library.Unit,
   *      magellan.library.rules.ItemType)
   */
  public boolean isPooled(Unit unit, ItemType type) {
    return isPooled(unit, type.getID());
  }

  /**
   * @see magellan.library.gamebinding.GameSpecificRules#getRecruitmentLimit(magellan.library.Unit,
   *      magellan.library.rules.Race)
   */
  public int getRecruitmentLimit(Unit u, Race race) {
    if (u.getRegion() == null)
      return 0;
    int recruited = 0;
    for (RecruitmentRelation rrel : u.getRegion().getZeroUnit().getRelations(
        RecruitmentRelation.class)) {
      recruited +=
          rrel.amount / rrel.race.getRecruitmentFactor()
              + (rrel.amount % rrel.race.getRecruitmentFactor() > 0 ? 1 : 0);
    }
    return (u.getRegion().maxRecruit() - recruited) * race.getRecruitmentFactor();
  }

  public boolean isAllied(Faction faction, Faction ally, int aState) {
    if (faction == null || ally == null)
      return false;
    if (faction.equals(ally))
      return true;
    Map<EntityID, Alliance> allies = faction.getAllies();
    if (allies == null)
      return false;
    Alliance alliance = allies.get(ally.getID());
    return alliance != null && alliance.getState(aState);
  }

  public int getMaxTrade(Region region) {
    if (region.getPeasants() > 0) {
      int volume = region.getPeasants() / 100;
      for (Building b : region.buildings())
        if (b.getBuildingType().getID().equals(EresseaConstants.B_CARAVANSEREI)) {
          volume *= 2;
        }
      return volume;
    }
    return -1;
  }

  public Unit getMaintainer(Building b) {
    Unit owner = b.getModifiedOwnerUnit();
    if (owner == null && b.getBuildingType().isMaintainedByRegionOwner()) {
      owner = b.getRegion().getOwnerUnit();
    }
    boolean maintain = true;
    if (owner != null) {
      for (Order order : owner.getOrders2()) {
        if (order instanceof MaintainOrder) {
          MaintainOrder mOrder = (MaintainOrder) order;
          if (mOrder.isValid() && mOrder.isNot()
              && (mOrder.getBuilding() == null || b.getID().equals(mOrder.getBuilding()))) {
            maintain = false;
            break;
          }
        }
      }
    }

    if (maintain)
      return owner;
    else
      return null;
  }

  /**
   * @see magellan.library.gamebinding.GameSpecificRules#getModifiedSkills(magellan.library.Unit)
   */
  public Map<StringID, Skill> getModifiedSkills(Unit targetUnitt) {
    if (targetUnitt.getData().getDate().getDate() >= 1256) // actually 1255
      return mergeSkillWeeeks(targetUnitt);
    else
      return mergeSkillPoints(targetUnitt);
  }

  /**
   * An older method that also tracks skill points.
   */
  private Map<StringID, Skill> mergeSkillPoints(Unit targetUnitt) {
    // get all related units (as set) and sort it in a list afterwards
    final Set<Unit> relatedUnits = new HashSet<Unit>();
    final Set<UnitRelation.ID> relationTypes = new HashSet<UnitRelation.ID>();
    relationTypes.add(UnitRelation.getClassID(PersonTransferRelation.class));
    relationTypes.add(UnitRelation.getClassID(RecruitmentRelation.class));
    targetUnitt.getRelatedUnits(relatedUnits, relationTypes);

    /* sort related units according to report order */
    final List<Unit> sortedUnits = new ArrayList<Unit>(relatedUnits);
    Collections.sort(sortedUnits, new LinearUnitTempUnitComparator(new SortIndexComparator<Unit>(
        null)));

    /* clone units with all aspects relevant for skills */
    final Map<ID, MagellanUnitImpl> clones = new Hashtable<ID, MagellanUnitImpl>();

    for (Unit unit : relatedUnits) {
      try {
        MagellanUnitImpl clone = (MagellanUnitImpl) unit.clone();
        clone.setSkills(null);

        for (Skill s : unit.getSkills()) {
          clone.addSkill(new Skill(s.getSkillType(), s.getPoints(), s.getLevel(), clone.getPersons(), s
              .noSkillPoints()));
        }
        clones.put(clone.getID(), clone);
      } catch (CloneNotSupportedException e) {
        // won't fail
        e.printStackTrace();
      }
    }

    // now modify the skills according to changes introduced by the relations
    /*
     * indicates that a skill is lost through person transfers or recruiting. May not be
     * Integer.MIN_VALUE to avoid wrap- around effects but should also be fairly negative so no
     * modifier can push it up to positive values.
     */
    final int lostSkillLevel = Skill.SPECIAL_LEVEL;

    for (Unit unit : sortedUnits) {
      final MagellanUnitImpl srcUnit = (MagellanUnitImpl) unit;

      for (UnitRelation unitRel : srcUnit.getRelations()) {
        if ((unitRel.source != null && !(unitRel.source.equals(srcUnit)))
            || !(unitRel instanceof PersonTransferRelation)) {
          continue;
        }

        final PersonTransferRelation rel = (PersonTransferRelation) unitRel;
        final Unit srcClone = clones.get(srcUnit.getID());
        final Unit targetUnit = rel.target;
        final Unit targetClone = clones.get(targetUnit.getID());
        /*
         * NOTE: maybe we respect rel.amount instead of reducing it here; we produce a warning message if transfer
         * amount is illegal
         */
        final int transferredPersons = Math.max(0, Math.min(srcClone.getPersons(), rel.amount));

        if (transferredPersons == 0) {
          continue;
        }

        /* modify the target clone */
        /*
         * first modify all skills that are available in the target clone
         */
        for (Skill targetSkill : targetClone.getSkills()) {
          Skill srcSkill = srcClone.getSkill(targetSkill.getSkillType());
          final int skillModifier = targetSkill.getModifier(targetClone);

          if (srcSkill == null) {
            /*
             * skill exists only in the target clone, this is equivalent to a target skill at 0.
             * Level is set to lostSkillLevel to avoid confusion about level modifiers in case of
             * noSkillPoints. If skill points are relevant this value is ignored anyway.
             */
            srcSkill =
                new Skill(targetSkill.getSkillType(), 0, lostSkillLevel, srcClone.getPersons(),
                    targetSkill.noSkillPoints());
          }

          if (targetSkill.noSkillPoints()) {
            /*
             * Math.max(0, ...) guarantees that the true skill level cannot drop below 0. This also
             * important to handle the Integer.MIN_VALUE case below
             */
            final int transferredSkillFactor =
                Math.max(0, srcSkill.getLevel() - skillModifier) * transferredPersons;
            final int targetSkillFactor =
                Math.max(0, targetSkill.getLevel() - skillModifier) * targetClone.getPersons();
            final int newSkillLevel =
                (int) (((float) (transferredSkillFactor + targetSkillFactor))
                    / (float) (transferredPersons + targetClone
                        .getPersons()));

            /*
             * newSkillLevel == 0 means that that the skill is lost by this transfer but we may not
             * set the skill level to 0 + skillModifier since this would indicate an existing skill
             * depending on the modifier. Thus lostSkillLevel is used to distinctly mark the
             * staleness of this skill.
             */
            targetSkill.setLevel((newSkillLevel > 0) ? (newSkillLevel + skillModifier)
                : lostSkillLevel);
          } else {
            targetSkill.setPoints(targetSkill.getPoints()
                + (int) ((srcSkill.getPoints() * transferredPersons) / (float) srcClone
                    .getPersons()));
          }
        }

        /*
         * now modify the skills that only exist in the source clone
         */
        for (Skill srcSkill : srcClone.getSkills()) {
          Skill targetSkill = targetClone.getSkill(srcSkill.getSkillType());

          if (targetSkill == null) {
            /*
             * skill exists only in the source clone, this is equivalent to a source skill at 0.
             * Level is set to lostSkillLevel to avoid confusion about level modifiers in case of
             * noSkillPoints. If skill points are relevant this value is ignored anyway.
             */
            targetSkill =
                new Skill(srcSkill.getSkillType(), 0, lostSkillLevel, targetClone.getPersons(),
                    srcSkill.noSkillPoints());
            targetClone.addSkill(targetSkill);

            if (srcSkill.noSkillPoints()) {
              /*
               * Math.max(0, ...) guarantees that the true skill level cannot drop below 0. This
               * also important to handle the lostSkillLevel case below
               */
              final int skillModifier = srcSkill.getModifier(srcClone);
              final int transferredSkillFactor =
                  Math.max(0, srcSkill.getLevel() - skillModifier) * transferredPersons;
              final int newSkillLevel =
                  (int) (((float) transferredSkillFactor) / (float) (transferredPersons
                      + targetClone
                          .getPersons()));

              /*
               * newSkillLevel == 0 means that that the skill is lost by this transfer but we may
               * not set the skill level to 0 + skillModifier since this would indicate an existing
               * skill depending on the modifier. Thus lostSkillLevel is used to distinctly mark the
               * staleness of this skill.
               */
              targetSkill.setLevel((newSkillLevel > 0) ? (newSkillLevel + skillModifier)
                  : lostSkillLevel);
            } else {
              final int newSkillPoints =
                  (int) (srcSkill.getPoints() * (transferredPersons / (float) srcClone
                      .getPersons()));
              targetSkill.setPoints(newSkillPoints);
            }
          }

          /*
           * modify the skills in the source clone (no extra loop for this)
           */
          if (!srcSkill.noSkillPoints()) {
            final int transferredSkillPoints =
                (int) ((srcSkill.getPoints() * transferredPersons) / (float) srcClone.getPersons());
            srcSkill.setPoints(srcSkill.getPoints() - transferredSkillPoints);
          }
        }

        srcClone.setPersons(srcClone.getPersons() - transferredPersons);
        targetClone.setPersons(targetClone.getPersons() + transferredPersons);
      }
    }

    /* modify the skills according to recruitment */
    final MagellanUnitImpl clone = clones.get(targetUnitt.getID());

    Hashtable<StringID, Skill> modifiedSkills = null;
    /* update the person and level information in all clone skills */
    if (clone.getSkills().size() > 0) {
      modifiedSkills = new Hashtable<StringID, Skill>();

      for (Skill skill : clone.getSkills()) {
        skill.setLevel(skill.getLevel() + 1);
        skill.setPersons(clone.getPersons());

        /*
         * When skill points are relevant, all we did up to now, was to keep track of these while
         * the skill level was ignored - update it now
         */
        if (!skill.noSkillPoints()) {
          skill.setLevel(skill.getLevel(clone, false));
        } else {
          /*
           * If skill points are not relevant we always take skill modifiers into account but we
           * marked 'lost' skills by Integer.MIN_VALUE which has to be fixed here
           */
          if (skill.getLevel() == lostSkillLevel) {
            skill.setLevel(0);
          }
        }

        /*
         * inject clone skills into real unit (no extra loop for this
         */
        if ((skill.getPoints() > 0) || (skill.getLevel() > 0)) {
          modifiedSkills.put(skill.getSkillType().getID(), skill);
        }
      }
    }
    return modifiedSkills;
  }

  /**
   * More recent version that maintains total skill weeks (used from week 1256 onwards.
   */
  private Map<StringID, Skill> mergeSkillWeeeks(Unit targetUnitt) {
    Hashtable<StringID, Skill> modifiedSkills = new Hashtable<StringID, Skill>();
    mergeSkills(modifiedSkills, targetUnitt.getSkillMap(), 0, targetUnitt.getPersons());
    int persons = targetUnitt.getPersons();

    for (UnitRelation urel : targetUnitt.getRelations()) {
      if (urel instanceof PersonTransferRelation) {
        PersonTransferRelation ptr = (PersonTransferRelation) urel;
        if (ptr.target == targetUnitt) {
          mergeSkills(modifiedSkills, ptr.getSkills(), persons, ptr.amount);
          persons += ptr.amount;
        } else {
          persons -= ptr.amount;
          if (persons == 0) {
            modifiedSkills.clear();
          }
        }
      } else if (urel instanceof RecruitmentRelation) {
        mergeSkills(modifiedSkills, null, persons, ((RecruitmentRelation) urel).amount);
        persons += ((RecruitmentRelation) urel).amount;
      }
    }
    return modifiedSkills;
  }

  private void mergeSkills(Map<StringID, Skill> modifiedSkills, Map<StringID, Skill> map, int persons, int transfer) {
    if (map != null) {
      for (Skill sk : map.values()) {
        SkillType type = sk.getSkillType();
        mergeSkill(modifiedSkills, type, map.get(type.getID()).getLevel(), persons, transfer);
      }
    }
    for (Skill sk : modifiedSkills.values()) {
      SkillType type = sk.getSkillType();
      if (map == null || !map.containsKey(type.getID())) {
        mergeSkill(modifiedSkills, type, 0, persons, transfer);
      }
    }
  }

  private void mergeSkill(Map<StringID, Skill> modifiedSkills, SkillType skt, int level, int persons, int transfer) {
    Skill skill = modifiedSkills.get(skt.getID());
    if (skill == null) {
      skill = new Skill(skt, 0, 0, persons, true);
      modifiedSkills.put(skt.getID(), skill);
    }
    mergeSkill(skill, level, persons, transfer);

  }

  @SuppressWarnings("deprecation")
  private void mergeSkill(Skill skill, int svl, int n, int add) {
    int snl = skill.getLevel();
    int total = add + n;
    int weeks = weeks_from_level(snl) * n + weeks_from_level(svl) * add;
    // level that combined unit should be at:
    int level = level_from_weeks(weeks, total);
    int newLevel = level;
    // how long it should take to the next level:
    int newWeeks = level + 1;

    // see if we have any remaining weeks:
    weeks -= weeks_from_level(level) * total;
    if (weeks / total != 0) {
      weeks = newWeeks - weeks / total;
      while (weeks < 0) {
        ++newLevel;
        weeks += newLevel;
      }
      newWeeks = weeks;
    }

    skill.setPersons(n + add);
    skill.setLevel(newLevel);
  }

  private int weeks_from_level(int level) {
    return (level + 1) * level / 2;
  }

  int level_from_weeks(int weeks, int n) {
    return (int) (Math.sqrt(1.0 + (weeks * 8.0 / n)) - 1) / 2;
  }

}
