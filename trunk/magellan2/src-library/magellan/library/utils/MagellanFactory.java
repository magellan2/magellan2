// class magellan.library.utils.MagellanFactory
// created on 01.05.2007
//
// Copyright 2003-2007 by magellan project team
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
import java.util.Map;

import magellan.library.AllianceGroup;
import magellan.library.Battle;
import magellan.library.Border;
import magellan.library.Building;
import magellan.library.CombatSpell;
import magellan.library.CoordinateID;
import magellan.library.EntityID;
import magellan.library.Faction;
import magellan.library.GameData;
import magellan.library.GameDataMerger;
import magellan.library.Group;
import magellan.library.HotSpot;
import magellan.library.IntegerID;
import magellan.library.Island;
import magellan.library.Message;
import magellan.library.Potion;
import magellan.library.Region;
import magellan.library.Scheme;
import magellan.library.Ship;
import magellan.library.Skill;
import magellan.library.Spell;
import magellan.library.StringID;
import magellan.library.TempUnit;
import magellan.library.Unit;
import magellan.library.UnitContainer;
import magellan.library.UnitID;
import magellan.library.ZeroUnit;
import magellan.library.Region.Visibility;
import magellan.library.impl.MagellanBattleImpl;
import magellan.library.impl.MagellanBorderImpl;
import magellan.library.impl.MagellanBuildingImpl;
import magellan.library.impl.MagellanCombatSpellImpl;
import magellan.library.impl.MagellanFactionImpl;
import magellan.library.impl.MagellanGroupImpl;
import magellan.library.impl.MagellanHotSpotImpl;
import magellan.library.impl.MagellanIslandImpl;
import magellan.library.impl.MagellanMessageImpl;
import magellan.library.impl.MagellanPotionImpl;
import magellan.library.impl.MagellanRegionImpl;
import magellan.library.impl.MagellanSchemeImpl;
import magellan.library.impl.MagellanShipImpl;
import magellan.library.impl.MagellanSpellImpl;
import magellan.library.impl.MagellanTempUnitImpl;
import magellan.library.impl.MagellanUnitImpl;
import magellan.library.impl.MagellanZeroUnitImpl;
import magellan.library.rules.MessageType;
import magellan.library.rules.RegionType;
import magellan.library.utils.logging.Logger;

/**
 * This factory returns all kind of Magellan objects. ....
 * 
 * @author Thoralf Rickert
 * @version 1.0, 01.05.2007
 */
public abstract class MagellanFactory {
  private static final Logger log = Logger.getInstance(MagellanFactory.class);

  public static Faction createFaction(EntityID id, GameData data) {
    return new MagellanFactionImpl(id, data);
  }

  public static Group createGroup(IntegerID id, GameData data) {
    return new MagellanGroupImpl(id, data);
  }

  public static Group createGroup(IntegerID id, GameData data, String name) {
    return new MagellanGroupImpl(id, data, name);
  }

  public static Group createGroup(IntegerID id, GameData data, String name, Faction faction) {
    return new MagellanGroupImpl(id, data, name, faction);
  }

  public static Message createMessage(String text) {
    return new MagellanMessageImpl(text);
  }

  public static Message createMessage(IntegerID id) {
    return new MagellanMessageImpl(id);
  }

  public static Message createMessage(IntegerID id, String text) {
    return new MagellanMessageImpl(id, text);
  }

  public static Message createMessage(IntegerID id, MessageType type, Map<String, String> attributes) {
    return new MagellanMessageImpl(id, type, attributes);
  }

  public static Message createMessage(Message message) {
    return new MagellanMessageImpl(message);
  }

  public static Region createRegion(CoordinateID id, GameData data) {
    return new MagellanRegionImpl(id, data);
  }

  public static Region createWrapper(CoordinateID wrapperID, long regionID, GameData resultGD) {
    Region resultRegion = MagellanFactory.createRegion(wrapperID, resultGD);
    resultRegion.setUID(regionID);
    resultRegion.setVisibility(Visibility.WRAP);
    resultRegion.setType(RegionType.wrap);
    return resultRegion;
  }

  public static Ship createShip(EntityID id, GameData data) {
    return new MagellanShipImpl(id, data);
  }

  public static Unit createUnit(UnitID id) {
    return new MagellanUnitImpl(id);
  }

  /**
   * @param id This should currently be a {@link StringID}
   * @param data
   * @return
   */
  public static Spell createSpell(StringID id, GameData data) {
    return new MagellanSpellImpl(id, data);
  }

  public static Battle createBattle(CoordinateID id) {
    return new MagellanBattleImpl(id);
  }

  public static Battle createBattle(CoordinateID id, boolean spec) {
    return new MagellanBattleImpl(id, spec);
  }

  public static Border createBorder(IntegerID id) {
    return new MagellanBorderImpl(id);
  }

  public static Border createBorder(IntegerID id, int direction, String type, int buildratio) {
    return new MagellanBorderImpl(id, direction, type, buildratio);
  }

  public static Building createBuilding(EntityID id, GameData data) {
    return new MagellanBuildingImpl(id, data);
  }

  public static CombatSpell createCombatSpell(IntegerID id) {
    return new MagellanCombatSpellImpl(id);
  }

  public static HotSpot createHotSpot(IntegerID id) {
    return new MagellanHotSpotImpl(id);
  }

  public static Island createIsland(IntegerID id, GameData data) {
    return new MagellanIslandImpl(id, data);
  }

  public static Potion createPotion(IntegerID id) {
    return new MagellanPotionImpl(id);
  }

  public static Scheme createScheme(CoordinateID id) {
    return new MagellanSchemeImpl(id);
  }

  public static TempUnit createTempUnit(UnitID id, Unit parent) {
    return new MagellanTempUnitImpl(id, parent);
  }

  public static ZeroUnit createZeroUnit(Region region) {
    return new MagellanZeroUnitImpl(region);
  }

  /**
   * Transfers all available information from the current group to the new one.
   * 
   * @param curGD fully loaded game data
   * @param curGroup a fully initialized and valid group
   * @param newGD the game data to be updated
   * @param newGroup a group to be updated with the data from curGroup
   * @deprecated Use {@link GameDataMerger#mergeGroup(GameData,Group,GameData,Group)} instead
   */
  @Deprecated
  public static void mergeGroup(GameData curGD, Group curGroup, GameData newGD, Group newGroup) {
    GameDataMerger.mergeGroup(curGD, curGroup, newGD, newGroup);
  }

  /**
   * Merges factions.
   * 
   * @deprecated Use {@link GameDataMerger#mergeFaction(GameData,Faction,GameData,Faction)} instead
   */
  @Deprecated
  public static void mergeFaction(GameData curGD, Faction curFaction, GameData newGD,
      Faction newFaction, boolean adjustTrustLevels) {
    GameDataMerger.mergeFaction(curGD, curFaction, newGD, newFaction, adjustTrustLevels, null);
  }

  /**
   * Merges UnitContainers.
   * 
   * @deprecated Use
   *             {@link GameDataMerger#mergeUnitContainer(GameData,UnitContainer,GameData,UnitContainer)}
   *             instead
   */
  @Deprecated
  public static void mergeUnitContainer(GameData curGD, UnitContainer curUC, GameData newGD,
      UnitContainer newUC) {
    GameDataMerger.mergeUnitContainer(curGD, curUC, newGD, newUC);
  }

  /**
   * Transfers all available information from the current message to the new one. This is generally
   * a localization problem: if newMsg.text == null then newMsg=curMsg (also in case the locale is
   * different) if curMsg.locale == newGD.local then newMsg=curMsg => if correct locale available
   * use it => otherwise take wrong locale msg, to have at least a half localized msg if the msgtype
   * is available in locale => you can notice this half localized msg because msg.locale=gm.locale,
   * also msg.rerender=true
   * 
   * @param curGD fully loaded game data
   * @param curMsg a fully initialized and valid message
   * @param newGD the game data to be updated
   * @param newMsg a message to be updated with the data from curMsg
   * @deprecated Use {@link GameDataMerger#mergeMessage(GameData,Message,GameData,Message)} instead
   */
  @Deprecated
  public static void mergeMessage(GameData curGD, Message curMsg, GameData newGD, Message newMsg) {
    GameDataMerger.mergeMessage(curGD, curMsg, newGD, newMsg, null);
  }

  /**
   * Merges potion.
   * 
   * @deprecated Use {@link GameDataMerger#mergePotion(GameData,Potion,GameData,Potion)} instead
   */
  @Deprecated
  public static void mergePotion(GameData curGD, Potion curPotion, GameData newGD, Potion newPotion) {
    GameDataMerger.mergePotion(curGD, curPotion, newGD, newPotion);
  }

  /**
   * Merges buildings. The new one get the name, comments etc. from the current one, effects etc.
   * are added, not written over.
   * 
   * @param curGD current GameData
   * @param curBuilding the current Building
   * @param newGD new GameData
   * @param newBuilding the new Building
   * @deprecated Use {@link GameDataMerger#mergeBuilding(GameData,Building,GameData,Building)}
   *             instead
   */
  @Deprecated
  public static void mergeBuilding(GameData curGD, Building curBuilding, GameData newGD,
      Building newBuilding) {
    GameDataMerger.mergeBuilding(curGD, curBuilding, newGD, newBuilding, null);
  }

  /**
   * Merges two combat spells.
   * 
   * @param curGD the current GameData.
   * @param curCS the current CombatSpell.
   * @param newGD the new GameData.
   * @param newCS the new CombatSpell.
   * @deprecated Use
   *             {@link GameDataMerger#mergeCombatSpell(GameData,CombatSpell,GameData,CombatSpell)}
   *             instead
   */
  @Deprecated
  public static void mergeCombatSpell(GameData curGD, CombatSpell curCS, GameData newGD,
      CombatSpell newCS) {
    GameDataMerger.mergeCombatSpell(curGD, curCS, newGD, newCS);
  }

  /**
   * Merges two HotSpot objects.
   * 
   * @deprecated Use {@link GameDataMerger#mergeHotSpot(GameData,HotSpot,GameData,HotSpot)} instead
   */
  @Deprecated
  public static void mergeHotSpot(GameData curGD, HotSpot curHS, GameData newGD, HotSpot newHS) {
    GameDataMerger.mergeHotSpot(curGD, curHS, newGD, newHS, null);
  }

  /**
   * Merges island.
   * 
   * @deprecated Use {@link GameDataMerger#mergeIsland(GameData,Island,GameData,Island)} instead
   */
  @Deprecated
  public static void mergeIsland(GameData curGD, Island curIsland, GameData newGD, Island newIsland) {
    GameDataMerger.mergeIsland(curGD, curIsland, newGD, newIsland);
  }

  /**
   * Merges all info from curRegion into newRegion. The result is influenced by the
   * <code>sameRound</code> parameter (indicating if the region infos are from the same round) and
   * the <code>firstPass</code> parameter. Merging is usually done in two passes. In the first pass,
   * the old info is copied into an intermediate object. In the second pass, the new object is
   * merged into this intermediate object.
   * 
   * @param curGD The GameData of curUnit
   * @param curRegion The region where the info is taken from
   * @param resultGD The GameData of resultRegion
   * @param resultRegion The info is merged into this region
   * @param newTurn notifies if both game data objects have been from the same round
   * @param firstPass notifies if this is the first of two passes
   * @deprecated Use
   *             {@link GameDataMerger#mergeRegion(GameData,Region,GameData,Region,boolean,boolean)}
   *             instead
   */
  @Deprecated
  public static void mergeRegion(GameData curGD, Region curRegion, GameData resultGD,
      Region resultRegion, boolean newTurn, boolean firstPass) {
    GameDataMerger.mergeRegion(curGD, curRegion, resultGD, resultRegion, newTurn, firstPass, null);
  }

  /**
   * Merges two Scheme objects.
   * 
   * @deprecated Use {@link GameDataMerger#mergeScheme(GameData,Scheme,GameData,Scheme)} instead
   */
  @Deprecated
  public static void mergeScheme(GameData curGD, Scheme curScheme, GameData newGD, Scheme newScheme) {
    GameDataMerger.mergeScheme(curGD, curScheme, newGD, newScheme);
  }

  /**
   * Merges ships.
   * 
   * @deprecated Use {@link GameDataMerger#mergeShip(GameData,Ship,GameData,Ship)} instead
   */
  @Deprecated
  public static void mergeShip(GameData curGD, Ship curShip, GameData newGD, Ship newShip) {
    GameDataMerger.mergeShip(curGD, curShip, newGD, newShip, null);
  }

  /**
   * Merges spells.
   * 
   * @deprecated Use {@link GameDataMerger#mergeSpell(GameData,Spell,GameData,Spell)} instead
   */
  @Deprecated
  public static void mergeSpell(GameData curGD, Spell curSpell, GameData newGD, Spell newSpell) {
    GameDataMerger.mergeSpell(curGD, curSpell, newGD, newSpell);
  }

  /**
   * Merges two temp units.
   * 
   * @param sameRound notifies if both game data objects have been from the same round
   * @deprecated Use
   *             {@link GameDataMerger#merge(GameData,TempUnit,GameData,TempUnit,boolean,boolean)}
   *             instead
   */
  @Deprecated
  public static void merge(GameData curGD, TempUnit curTemp, GameData newGD, TempUnit newTemp,
      boolean sameRound, boolean firstPass) {
    GameDataMerger.merge(curGD, curTemp, newGD, newTemp, sameRound, firstPass, null);
  }

  /**
   * Merges only the comments of <code>curShip</code> to <code>newShip</code>. Use if you don't want
   * to do a full merge.
   * 
   * @param curShip
   * @param newShip
   * @deprecated Use {@link GameDataMerger#mergeComments(UnitContainer,UnitContainer)} instead
   */
  @Deprecated
  public static void mergeComments(UnitContainer curShip, UnitContainer newShip) {
    GameDataMerger.mergeComments(curShip, newShip);
  }

  /**
   * Merges all info from curUnit into newUnit. The result is influenced by the
   * <code>sameRound</code> parameter (indicating if the unit infos are from the same round) and the
   * <code>firstPass</code> parameter. Merging is usually done in two passes. In the first pass, the
   * old info is copied into an intermediate object. In the second pass, the new object is merged
   * into this intermediate object.
   * 
   * @param curGD The GameData of curUnit
   * @param curUnit The unit where the info is taken from
   * @param resultGD The GameData of newUnit
   * @param resultUnit The info is merged into this unit
   * @param sameRound notifies if both game data objects have been from the same round
   * @param firstPass notifies if this is the first of two passes
   * @deprecated Use {@link GameDataMerger#mergeUnit(GameData,Unit,GameData,Unit,boolean,boolean)}
   *             instead
   */
  @Deprecated
  public static void mergeUnit(GameData curGD, Unit curUnit, GameData resultGD, Unit resultUnit,
      boolean sameRound, boolean firstPass) {
    GameDataMerger.mergeUnit(curGD, curUnit, resultGD, resultUnit, sameRound, firstPass, null);
  }

  /**
   * Copies the skills of the given unit. Does not empty this unit's skills.
   */
  public static void copySkills(Unit u, Unit v) {
    MagellanFactory.copySkills(u, v, true);
  }

  /**
   * Copies the skills of the given unit. Does not empty this unit's skills.
   */
  public static void copySkills(Unit u, Unit v, boolean sortOut) {
    v.setSkillsCopied(true);

    if (u.getSkills() != null) {
      final Iterator<Skill> it = u.getSkills().iterator();

      while (it.hasNext()) {
        final Skill sk = it.next();

        // sort out if changed to non-existent
        if (sortOut && sk.isLostSkill()) {
          continue;
        }

        final Skill newSkill =
            new Skill(sk.getSkillType(), sk.getPoints(), sk.getLevel(), v.getPersons(), sk
                .noSkillPoints());
        v.addSkill(newSkill);
      }
    }
  }

  /**
   * Returns a locale specific string representation of the specified unit combat status.
   */
  public static String combatStatusToString(Unit u) {
    String retVal = MagellanFactory.combatStatusToString(u.getCombatStatus());
    if (u.getModifiedCombatStatus() != u.getCombatStatus()) {
      retVal += " (" + MagellanFactory.combatStatusToString(u.getModifiedCombatStatus()) + ")";
    }

    if (u.isUnaided()) {
      retVal += (", " + Resources.get("unit.combatstatus.unaided"));
    }

    if (u.getModifiedUnaided() != u.isUnaided()) {
      if (u.getModifiedUnaided()) {
        retVal += " (" + Resources.get("unit.combatstatus.unaided") + ")";
      } else {
        retVal += " (" + Resources.get("unit.combatstatus.aided") + ")";
      }
    }

    return retVal;
  }

  /**
   * Returns a locale specific string representation of the specified unit combat status.
   */
  public static String combatStatusToString(int combatStatus) {
    String retVal = null;

    switch (combatStatus) {
    case 0:
      retVal = Resources.get("unit.combatstatus.aggressive");

      break;

    case 1:
      retVal = Resources.get("unit.combatstatus.front");

      break;

    case 2:
      retVal = Resources.get("unit.combatstatus.back");

      break;

    case 3:
      retVal = Resources.get("unit.combatstatus.defensive");

      break;

    case 4:
      retVal = Resources.get("unit.combatstatus.passive");

      break;

    case 5:
      retVal = Resources.get("unit.combatstatus.escape");

      break;

    default:

      final Object msgArgs[] = { Integer.valueOf(combatStatus) };
      retVal =
          (new java.text.MessageFormat(Resources.get("unit.combatstatus.unknown"))).format(msgArgs);
    }

    return retVal;
  }

  /**
   * see Unit.GUARDFLAG_ Converts guard flags into a readable string.
   */
  public static String guardFlagsToString(int iFlags) {
    String strFlags = "";

    if (iFlags != 0) {
      strFlags += Resources.get("unit.guard.region");
    }

    /**
     * es all standard guarding units seems to have 1 -> tax is alleways triggered. Deactivating it.
     * if((iFlags & Unit.GUARDFLAG_TAX) != 0) { strFlags += (", " +
     * Resources.get("unit.guard.tax")); }
     */
    if ((iFlags & Unit.GUARDFLAG_MINING) != 0) {
      strFlags += (", " + Resources.get("unit.guard.mining"));
    }
    if ((iFlags & Unit.GUARDFLAG_WOOD) != 0) {
      strFlags += (", " + Resources.get("unit.guard.wood"));
    }
    if ((iFlags & Unit.GUARDFLAG_TRAVELTHRU) != 0) {
      strFlags += (", " + Resources.get("unit.guard.travelthru"));
    }
    if ((iFlags & Unit.GUARDFLAG_LANDING) != 0) {
      strFlags += (", " + Resources.get("unit.guard.landing"));
    }
    if ((iFlags & Unit.GUARDFLAG_CREWS) != 0) {
      strFlags += (", " + Resources.get("unit.guard.crews"));
    }
    if ((iFlags & Unit.GUARDFLAG_RECRUIT) != 0) {
      strFlags += (", " + Resources.get("unit.guard.recruit"));
    }
    if ((iFlags & Unit.GUARDFLAG_PRODUCE) != 0) {
      strFlags += (", " + Resources.get("unit.guard.produce"));
    }

    return strFlags;
  }

  public static AllianceGroup createAlliance(EntityID id, GameData resultGD) {
    return new AllianceGroup(id);
  }

  /**
   * @deprecated Use
   *             {@link GameDataMerger#mergeAlliance(GameData,AllianceGroup,GameData,AllianceGroup)}
   *             instead
   */
  @Deprecated
  public static void mergeAlliance(GameData curGD, AllianceGroup curAlliance, GameData newGD,
      AllianceGroup newAlliance) {
    GameDataMerger.mergeAlliance(curGD, curAlliance, newGD, newAlliance);
  }

}
