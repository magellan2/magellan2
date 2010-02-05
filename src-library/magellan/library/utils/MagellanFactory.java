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

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import magellan.library.Alliance;
import magellan.library.AllianceGroup;
import magellan.library.Battle;
import magellan.library.Border;
import magellan.library.Building;
import magellan.library.CombatSpell;
import magellan.library.CoordinateID;
import magellan.library.EntityID;
import magellan.library.Faction;
import magellan.library.GameData;
import magellan.library.Group;
import magellan.library.HotSpot;
import magellan.library.ID;
import magellan.library.IntegerID;
import magellan.library.Island;
import magellan.library.Item;
import magellan.library.LuxuryPrice;
import magellan.library.Message;
import magellan.library.Potion;
import magellan.library.Region;
import magellan.library.RegionResource;
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
import magellan.library.gamebinding.EresseaConstants;
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
import magellan.library.rules.ItemType;
import magellan.library.rules.MessageType;
import magellan.library.rules.Options;
import magellan.library.rules.Race;
import magellan.library.rules.RegionType;
import magellan.library.rules.SkillType;
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
   */
  public static void mergeGroup(GameData curGD, Group curGroup, GameData newGD, Group newGroup) {
    if (curGroup.getName() != null) {
      newGroup.setName(curGroup.getName());
    }

    if ((curGroup.allies() != null) && (curGroup.allies().size() > 0)) {
      if (newGroup.allies() == null) {
        newGroup.setAllies(new Hashtable<EntityID, Alliance>());
      } else {
        newGroup.allies().clear();
      }

      for (Alliance alliance : curGroup.allies().values()) {
        final Faction ally = newGD.getFaction(alliance.getFaction().getID());
        newGroup.allies().put(ally.getID(), new Alliance(ally, alliance.getState()));
      }
    }

    if (curGroup.getFaction() != null) {
      newGroup.setFaction(newGD.getFaction(curGroup.getFaction().getID()));
    }

    newGroup.setSortIndex(Math.max(newGroup.getSortIndex(), curGroup.getSortIndex()));

    if (curGroup.getRaceNamePrefix() != null) {
      newGroup.setRaceNamePrefix(curGroup.getRaceNamePrefix());
    }

    if (curGroup.getAttributeSize() > 0) {
      for (final String key : curGroup.getAttributeKeys()) {
        if (!newGroup.containsAttribute(key)) {
          newGroup.addAttribute(key, curGroup.getAttribute(key));
        }
      }
    }

  }

  /**
   * DOCUMENT-ME
   */
  public static void mergeFaction(GameData curGD, Faction curFaction, GameData newGD,
      Faction newFaction) {
    MagellanFactory.mergeUnitContainer(curGD, curFaction, newGD, newFaction);

    // tricky: keep alliance only if from the same round and either curFaction is owner faction or
    // alliance is known
    if (curGD.getDate().equals(newGD.getDate())
        && (curFaction.getAlliance() != null || curFaction.getID().equals(curGD.getOwnerFaction()))) {
      newFaction.setAlliance(curFaction.getAlliance());
    }

    // keep allies information from last round if now new info is known
    if ((curFaction.getAllies() != null && curFaction.getAllies().size() > 0)
        || curFaction.getID().equals(curGD.getOwnerFaction())) {
      if (newFaction.getAllies() == null) {
        newFaction.setAllies(new OrderedHashtable<EntityID, Alliance>());
      } else {
        newFaction.getAllies().clear();
      }

      if (curFaction.getAllies() != null) {
        for (Alliance alliance : curFaction.getAllies().values()) {
          final Faction ally = newGD.getFaction(alliance.getFaction().getID());
          newFaction.getAllies().put(ally.getID(), new Alliance(ally, alliance.getState()));
        }
      }
    }

    if (curFaction.getEmail() != null) {
      newFaction.setEmail(curFaction.getEmail());
    }

    if ((curFaction.getGroups() != null) && (curFaction.getGroups().size() > 0)) {
      if (newFaction.getGroups() == null) {
        newFaction.setGroups(new Hashtable<IntegerID, Group>());
      } else {
        newFaction.getGroups().clear();
      }

      for (Group curGroup : curFaction.getGroups().values()) {
        Group newGroup = null;

        try {
          newGroup = MagellanFactory.createGroup(curGroup.getID().clone(), newGD);
        } catch (final CloneNotSupportedException e) {
          throw new NullPointerException("cannot happen");
        }

        MagellanFactory.mergeGroup(curGD, curGroup, newGD, newGroup);
        newFaction.getGroups().put(newGroup.getID(), newGroup);
      }
    }

    if (curFaction.getLocale() != null) {
      newFaction.setLocale(curFaction.getLocale());
    }

    if (curFaction.getMaxMigrants() != -1) {
      newFaction.setMaxMigrants(curFaction.getMaxMigrants());
    }

    if (curFaction.getOptions() != null) {
      newFaction.setOptions(new Options(curFaction.getOptions()));
    }

    if (curFaction.getPassword() != null) {
      newFaction.setPassword(curFaction.getPassword());
    }

    if (curFaction.getTreasury() != 0) {
      newFaction.setTreasury(curFaction.getTreasury());
    }

    if (curFaction.getSpellSchool() != null) {
      newFaction.setSpellSchool(curFaction.getSpellSchool());
    }

    // one trustLevel is guaranteed to be TL_DEFAULT
    // ReportMerger.mergeReport() :

    /**
     * prepare faction trustlevel for merging: - to be added CR is older or of same age -> hold
     * existing trust levels - to be added CR is newer and contains trust level that were set by the
     * user explicitly (or read from CR what means the same) -> take the trust levels out of the new
     * CR otherwise -> hold existing trust levels This means: set those trust levels, that will not
     * be retained to default values
     */
    if ((curFaction.getTrustLevel() != Faction.TL_DEFAULT) || curFaction.isTrustLevelSetByUser()) {
      newFaction.setTrustLevel(curFaction.getTrustLevel());
      newFaction.setTrustLevelSetByUser(curFaction.isTrustLevelSetByUser());
    }

    // see Region.merge() for the meaning of the following if
    if (curGD.getDate().equals(newGD.getDate())) {
      if (curFaction.getAverageScore() != -1) {
        newFaction.setAverageScore(curFaction.getAverageScore());
      }

      if ((curFaction.getBattles() != null) && (curFaction.getBattles().size() > 0)) {
        newFaction.setBattles(new LinkedList<Battle>());

        for (Battle curBattle : curFaction.getBattles()) {
          try {
            final Battle newBattle =
                MagellanFactory.createBattle(curBattle.getID().clone(), curBattle.isBattleSpec());

            for (Message curMsg : curBattle.messages()) {
              final Message newMsg = MagellanFactory.createMessage(curMsg.getID().clone());
              MagellanFactory.mergeMessage(curGD, curMsg, newGD, newMsg);
              newBattle.messages().add(newMsg);
            }

            newFaction.getBattles().add(curBattle);
          } catch (final CloneNotSupportedException e) {
            MagellanFactory.log.error("Faction.merge()", e);
          }
        }
      }

      if ((curFaction.getErrors() != null) && (curFaction.getErrors().size() > 0)) {
        newFaction.setErrors(new LinkedList<String>(curFaction.getErrors()));
      }

      if ((curFaction.getMessages() != null) && (curFaction.getMessages().size() > 0)) {
        if (newFaction.getMessages() == null) {
          newFaction.setMessages(new LinkedList<Message>());
        } else {
          newFaction.getMessages().clear();
        }

        for (Message curMsg : curFaction.getMessages()) {
          Message newMsg = null;

          try {
            newMsg = MagellanFactory.createMessage(curMsg.getID().clone());
          } catch (final CloneNotSupportedException e) {
          }

          MagellanFactory.mergeMessage(curGD, curMsg, newGD, newMsg);
          newFaction.getMessages().add(newMsg);
        }
      }

      if (curFaction.getMigrants() != -1) {
        newFaction.setMigrants(curFaction.getMigrants());
      }

      if (curFaction.getPersons() != -1) {
        newFaction.setPersons(curFaction.getPersons());
      }

      if (curFaction.getRaceNamePrefix() != null) {
        newFaction.setRaceNamePrefix(curFaction.getRaceNamePrefix());
      }

      if (curFaction.getScore() != -1) {
        newFaction.setScore(curFaction.getScore());
      }

      /**
       * Fiete: merge also new (20061102) hereos values and age
       */
      if (curFaction.getHeroes() != -1) {
        newFaction.setHeroes(curFaction.getHeroes());
      }

      if (curFaction.getMaxHeroes() != -1) {
        newFaction.setMaxHeroes(curFaction.getMaxHeroes());
      }

      if (curFaction.getAge() != -1) {
        newFaction.setAge(curFaction.getAge());
      }

      if (curFaction.getItems() != null) {
        if (curFaction.getItems().size() > 0) {
          for (Item currentItem : curFaction.getItems()) {
            newFaction.addItem(currentItem);
          }
        }
      }

    }
  }

  /**
   * Merges UnitContainers.
   */
  public static void mergeUnitContainer(GameData curGD, UnitContainer curUC, GameData newGD,
      UnitContainer newUC) {
    if (curUC.getName() != null) {
      newUC.setName(curUC.getName());
    }

    if (curUC.getDescription() != null) {
      newUC.setDescription(curUC.getDescription());
    }

    if ((curUC.getComments() != null) && (curUC.getComments().size() > 0)) {
      if (newUC.getComments() == null) {
        newUC.setComments(new LinkedList<String>());
      }
      // else {
      // newUC.comments.clear();
      // }

      newUC.getComments().addAll(curUC.getComments());
    }

    // see Region.merge() for the meaning of the following if
    if (curGD.getDate().equals(newGD.getDate())) {
      if ((curUC.getEffects() != null) && (curUC.getEffects().size() > 0)) {
        if (newUC.getEffects() == null) {
          newUC.setEffects(new LinkedList<String>());
        } else {
          newUC.getEffects().clear();
        }

        newUC.getEffects().addAll(curUC.getEffects());
      }
    }

    if (curUC.getOwner() != null) {
      newUC.setOwner(newGD.getUnit(curUC.getOwner().getID()));
    } else {
      newUC.setOwner(null);
    }

    if (curUC.getOwnerUnit() != null) {
      newUC.setOwnerUnit(newGD.getUnit(curUC.getOwnerUnit().getID()));
    } else {
      newUC.setOwnerUnit(null);
    }

    if (curUC.getType() != null) {
      if (curUC instanceof Building) {
        newUC.setType(newGD.rules.getBuildingType(curUC.getType().getID(), true));
      } else if (curUC instanceof Region) {
        // pavkovic 2004.01.03: (bugzilla bug 801): overwrite with curUC.getType
        // if
        // known or newUC.getType is same as "unknown" (this is a miracle to me
        // but
        // Ulrich has more experiences with "Astralraum" :-))
        // if (newUC.getType() == null ||
        // newUC.getType().equals(RegionType.unknown)) {
        if ((curUC.getType() != null && !curUC.getType().equals(RegionType.unknown))
            || newUC.getType() == null || newUC.getType().equals(RegionType.unknown)) {
          newUC.setType(newGD.rules.getRegionType(curUC.getType().getID(), true));
        }
      } else if (curUC instanceof Ship) {
        newUC.setType(newGD.rules.getShipType(curUC.getType().getID(), true));
      } else if (curUC instanceof Faction) {
        newUC.setType(newGD.rules.getRace(curUC.getType().getID(), true));
      }
    }

    // copy tags
    if (curGD.getDate().equals(newGD.getDate()) && curUC.hasTags()) {
      final Iterator<String> it = curUC.getTagMap().keySet().iterator();

      while (it.hasNext()) {
        final String str = it.next();

        if (!newUC.containsTag(str)) {
          newUC.putTag(str, curUC.getTag(str));
        }
      }
    }

    newUC.setCache(null);

    newUC.setSortIndex(Math.max(newUC.getSortIndex(), curUC.getSortIndex()));

    if (curUC.getAttributeSize() > 0) {
      for (final String key : curUC.getAttributeKeys()) {
        if (!newUC.containsAttribute(key)) {
          newUC.addAttribute(key, curUC.getAttribute(key));
        }
      }
    }

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
   */
  public static void mergeMessage(GameData curGD, Message curMsg, GameData newGD, Message newMsg) {
    if ((curMsg.getAttributes() != null) && (curMsg.getAttributes().size() > 0)) {
      if (newMsg.getAttributes() == null) {
        newMsg.setAttributes(new OrderedHashtable<String, String>());
      } else {
        newMsg.getAttributes().clear();
      }

      newMsg.getAttributes().putAll(curMsg.getAttributes());
    }

    // first update the message type - this was already localized
    if (curMsg.getMessageType() != null) {
      newMsg.setType(newGD.getMsgType(curMsg.getMessageType().getID()));
    }

    if (curMsg.getText() != null) {
      // TODO save the locale of the message. If msg.locale matches the
      // newGD.locale then we can use the text
      // currently we check the GameData as the msg locale is not implemented.
      if (curGD.getLocale().equals(newGD.getLocale())) {
        // we can only copy the text if it matches the locale
        newMsg.setText(curMsg.getText());
      } else {
        if (curMsg.getMessageType() == null) {
          // if the message has no message type (e.g. DURCHSCHIFFUNG,
          // DURCHREISE), the best thing we can do is to copy the text anyway...
          newMsg.setText(curMsg.getText());
        }
        // otherwise we can render the text from the probably localized
        // messagetype
        /*
         * we dont render it here as the new GameData is not fully initialized. as the text is null,
         * it will be rendered on the first usage. newMsg.render(newGD);
         */
      }
    }
  }

  /**
   * Merges potion.
   */
  public static void mergePotion(GameData curGD, Potion curPotion, GameData newGD, Potion newPotion) {
    if (curPotion.getName() != null) {
      newPotion.setName(curPotion.getName());
    }

    if ((curPotion.getDescription() != null) && (curGD.getLocale().equals(newGD.getLocale()))) {
      newPotion.setDescription(curPotion.getDescription());
    }

    if (curPotion.getLevel() != -1) {
      newPotion.setLevel(curPotion.getLevel());
    }

    if (!curPotion.ingredients().isEmpty()) {
      newPotion.clearIngredients();

      for (Item i : curPotion.ingredients()) {
        final magellan.library.rules.ItemType it =
            newGD.rules.getItemType(i.getItemType().getID(), true);
        newPotion.addIngredient(new Item(it, i.getAmount()));
      }
    }
  }

  /**
   * Merges buildings. The new one get the name, comments etc. from the current one, effects etc.
   * are added, not written over.
   * 
   * @param curGD current GameData
   * @param curBuilding the current Building
   * @param newGD new GameData
   * @param newBuilding the new Building
   */
  public static void mergeBuilding(GameData curGD, Building curBuilding, GameData newGD,
      Building newBuilding) {
    MagellanFactory.mergeUnitContainer(curGD, curBuilding, newGD, newBuilding);

    if (curBuilding.getCost() != -1) {
      newBuilding.setCost(curBuilding.getCost());
    }

    if (curBuilding.getRegion() != null) {
      newBuilding.setRegion(newGD.getRegion(curBuilding.getRegion().getID()));
    }

    if (curBuilding.getSize() != -1) {
      newBuilding.setSize(curBuilding.getSize());
    }

    // Fiete 20060910
    // added support for wahrerTyp
    if (curBuilding.getTrueBuildingType() != null) {
      newBuilding.setTrueBuildingType(curBuilding.getTrueBuildingType());
    }

  }

  /**
   * Merges two combat spells.
   * 
   * @param curGD the current GameData.
   * @param curCS the current CombatSpell.
   * @param newGD the new GameData.
   * @param newCS the new CombatSpell.
   */
  public static void mergeCombatSpell(GameData curGD, CombatSpell curCS, GameData newGD,
      CombatSpell newCS) {
    // transfer the level of the casted spell
    if (curCS.getCastingLevel() != -1) {
      newCS.setCastingLevel(curCS.getCastingLevel());
    }

    // transfer the spell
    if (curCS.getSpell() != null) {
      newCS.setSpell(newGD.getSpell(curCS.getSpell().getID()));
    }

    // transfer the casting unit
    if (curCS.getUnit() != null) {
      newCS.setUnit(newGD.getUnit(curCS.getUnit().getID()));
    }
  }

  /**
   * Merges two HotSpot objects.
   */
  public static void mergeHotSpot(GameData curGD, HotSpot curHS, GameData newGD, HotSpot newHS) {
    if (curHS.getName() != null) {
      newHS.setName(curHS.getName());
    }

    if (curHS.getCenter() != null) {
      newHS.setCenter(curHS.getCenter());
    }
  }

  /**
   * Merges island.
   */
  public static void mergeIsland(GameData curGD, Island curIsland, GameData newGD, Island newIsland) {
    if (curIsland.getName() != null) {
      newIsland.setName(curIsland.getName());
    }

    if (curIsland.getDescription() != null) {
      newIsland.setDescription(curIsland.getDescription());
    }

    newIsland.invalidateRegions();

    if (curIsland.getAttributeSize() > 0) {
      for (final String key : curIsland.getAttributeKeys()) {
        if (!newIsland.containsAttribute(key)) {
          newIsland.addAttribute(key, curIsland.getAttribute(key));
        }
      }
    }
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
   */
  public static void mergeRegion(GameData curGD, Region curRegion, GameData resultGD,
      Region resultRegion, boolean newTurn, boolean firstPass) {
    MagellanFactory.mergeUnitContainer(curGD, curRegion, resultGD, resultRegion);

    final boolean sameTurn = !newTurn || !firstPass;

    /******************** MORALE AND OWNER *************************************/
    if (curRegion.getMorale() >= 0
        || (resultRegion.getMorale() >= 0 && curRegion.getVisibility().equals(Visibility.UNIT))) {
      resultRegion.setMorale(curRegion.getMorale());
    }
    if (curRegion.getOwnerFaction() != null
        || (resultRegion.getOwnerFaction() != null && curRegion.getVisibility().equals(
            Visibility.UNIT))) {
      resultRegion.setOwnerFaction(curRegion.getOwnerFaction());
    }

    if (sameTurn) {
      resultRegion.setMourning(curRegion.getMourning());
    }

    /******************** OLD VALUES OF SIMPLE RESOURCES *************************************/
    // *** OldTrees ****
    if (newTurn && firstPass && curRegion.getTrees() != -1) {
      resultRegion.setOldTrees(curRegion.getTrees());
      // resultRegion.setOldSprouts(-1);
    } else if (!newTurn && curRegion.getOldTrees() != -1) {
      resultRegion.setOldTrees(curRegion.getOldTrees());
    }

    // *** OldSprouts ****
    // same as with the old trees
    if (newTurn && firstPass && curRegion.getSprouts() != -1) {
      resultRegion.setOldSprouts(curRegion.getSprouts());
    } else if (!newTurn && curRegion.getOldSprouts() != -1) {
      resultRegion.setOldSprouts(curRegion.getOldSprouts());
    }

    // *** OldIron ****
    // same as with the old trees
    if (newTurn && firstPass && curRegion.getIron() != -1) {
      resultRegion.setOldIron(curRegion.getIron());
    } else if (!newTurn && curRegion.getOldIron() != -1) {
      resultRegion.setOldIron(curRegion.getOldIron());
    }

    // *** OldLaen ****
    // same as with the old trees
    if (newTurn && firstPass && curRegion.getLaen() != -1) {
      resultRegion.setOldLaen(curRegion.getLaen());
    } else if (!newTurn && curRegion.getOldLaen() != -1) {
      resultRegion.setOldLaen(curRegion.getOldLaen());
    }

    // *** Orc infest ****
    if (!newTurn || !firstPass) {
      // region is considered orc infested if one of the two regions considered
      // it orc infested.
      resultRegion.setOrcInfested(resultRegion.isOrcInfested() || curRegion.isOrcInfested());
    } else {
      resultRegion.setOrcInfested(curRegion.isOrcInfested());
    }

    // *** OldPeasants ****
    // same as with the old trees
    if (newTurn && firstPass && curRegion.getPeasants() != -1) {
      resultRegion.setOldPeasants(curRegion.getPeasants());
    } else if (!newTurn && curRegion.getOldPeasants() != -1) {
      resultRegion.setOldPeasants(curRegion.getOldPeasants());
    }

    // *** OldSilver ****
    // same as with the old trees
    if (newTurn && firstPass && curRegion.getSilver() != -1) {
      resultRegion.setOldSilver(curRegion.getSilver());
    } else if (!newTurn && curRegion.getOldSilver() != -1) {
      resultRegion.setOldSilver(curRegion.getOldSilver());
    }

    // *** OldStones ****
    // same as with the old trees
    if (newTurn && firstPass && curRegion.getStones() != -1) {
      resultRegion.setOldStones(curRegion.getStones());
    } else if (!newTurn && curRegion.getOldStones() != -1) {
      resultRegion.setOldStones(curRegion.getOldStones());
    }

    // same as with the old trees
    if (newTurn && firstPass && curRegion.getHorses() != -1) {
      resultRegion.setOldHorses(curRegion.getHorses());
    } else if (!newTurn && curRegion.getOldHorses() != -1) {
      resultRegion.setOldHorses(curRegion.getOldHorses());
    }

    // same as with the old trees
    if (newTurn && firstPass && curRegion.getWage() != -1) {
      resultRegion.setOldWage(curRegion.getWage());
    } else if (!newTurn && curRegion.getOldWage() != -1) {
      resultRegion.setOldWage(curRegion.getOldWage());
    }

    // same as with the old trees
    if (newTurn && firstPass && curRegion.getRecruits() != -1) {
      resultRegion.setOldRecruits(curRegion.getRecruits());
    } else if (!newTurn && curRegion.getOldRecruits() != -1) {
      resultRegion.setOldRecruits(curRegion.getOldRecruits());
    }

    /******************* PRICES ******************************/
    if ((resultRegion.getPrices() != null) && (curRegion.getPrices() != null)
        && !curRegion.getPrices().equals(resultRegion.getPrices())) {
      resultRegion.setOldPrices(new Hashtable<StringID, LuxuryPrice>());

      for (LuxuryPrice curPrice : resultRegion.getPrices().values()) {
        final LuxuryPrice newPrice =
            new LuxuryPrice(resultGD.rules.getItemType(curPrice.getItemType().getID()), curPrice
                .getPrice());
        resultRegion.getOldPrices().put(newPrice.getItemType().getID(), newPrice);
      }
    } else if (curRegion.getOldPrices() != null) {
      resultRegion.setOldPrices(new Hashtable<StringID, LuxuryPrice>());

      for (LuxuryPrice curPrice : curRegion.getOldPrices().values()) {
        final LuxuryPrice newPrice =
            new LuxuryPrice(resultGD.rules.getItemType(curPrice.getItemType().getID()), curPrice
                .getPrice());

        if (newPrice.getItemType() == null) {
          // this happens if there does exist an unknown tag in
          // the current block description
          MagellanFactory.log.warn("WARNING: Invalid tag \"" + curPrice.getItemType()
              + "\" found in Region " + curRegion + ", ignoring it.");
          MagellanFactory.log.warn("curRegion Encoding: " + curRegion.getData().getEncoding()
              + ", newRegion Enc: " + resultRegion.getData().getEncoding());
        } else {
          resultRegion.getOldPrices().put(newPrice.getItemType().getID(), newPrice);
        }
      }
    }

    /******************** BORDERS *************************************/
    /**
     * <p>
     * If we have first-hand visibility (unit, travel; FIXME what about lighthouse?), we should
     * erase old borders. If visibility == 0, we should keep the border. It might be gone or it
     * might still be there. It does not matter if sameRound or not.
     * </p>
     * <p>
     * FIXME: If visibility==neighbor, we should remove borders to all visible regions without
     * borders.
     * </p>
     * <p>
     * Fiete 20080121: we have also full info about borders if we traveled (status==3) through the
     * region.
     * </p>
     */
    if (curRegion.getVisibility() == Visibility.UNIT
        || curRegion.getVisibility() == Visibility.TRAVEL) {

      resultRegion.clearBorders();

      for (Border curBorder : curRegion.borders()) {
        Border newBorder = null;

        try {
          newBorder =
              MagellanFactory.createBorder(curBorder.getID().clone(), curBorder.getDirection(),
                  curBorder.getType(), curBorder.getBuildRatio());
        } catch (final CloneNotSupportedException e) {
        }

        resultRegion.addBorder(newBorder);
      }

    } else {
      // just add new Borders
      for (Border curBorder : curRegion.borders()) {
        // do we already have this border?
        boolean curBorderPresent = false;
        for (Border actNewBorder : resultRegion.borders()) {
          if (actNewBorder.getType().equalsIgnoreCase(curBorder.getType())
              && actNewBorder.getDirection() == curBorder.getDirection()) {
            curBorderPresent = true;
            break;
          }
        }

        if (!curBorderPresent) {
          // add border to result region
          Border newBorder = null;
          final IntegerID newID = Regions.getNewBorderID(resultRegion, curBorder);

          newBorder =
              MagellanFactory.createBorder(newID, curBorder.getDirection(), curBorder.getType(),
                  curBorder.getBuildRatio());

          if (newBorder != null) {
            resultRegion.addBorder(newBorder);
          }
        }
      }
    }

    /******************** VISIBILITY *************************************/
    if (firstPass) {
      if (newTurn) {
        resultRegion.setVisibility(Visibility.NULL);
      } else {
        resultRegion.setVisibility(curRegion.getVisibility());
      }
    } else {
      final Visibility curRegionVis = curRegion.getVisibility();
      final Visibility actNewRegionVis = resultRegion.getVisibility();
      final Visibility result = Visibility.getMax(curRegionVis, actNewRegionVis);
      resultRegion.setVisibility(result);
    }

    /******************** REGION IDs *************************************/
    if (curRegion.getUID() != 0) {
      resultRegion.setUID(curRegion.getUID());
    }

    /******************** HERBS *************************************/
    if (curRegion.getHerb() != null) {
      resultRegion.setHerb(resultGD.rules.getItemType(curRegion.getHerb().getID(), true));
    }

    if (curRegion.getHerbAmount() != null) {
      /*
       * There was a bug around 2002.02.16 where numbers would be stored in this field - filter them
       * out. This should only be here for one or two weeks.
       */
      if (curRegion.getHerbAmount().length() > 2) {
        resultRegion.setHerbAmount(curRegion.getHerbAmount());
      }
    }

    /******************** SIMPLE RESOURCES *************************************/
    if (curRegion.getHorses() != -1) {
      resultRegion.setHorses(curRegion.getHorses());
    }

    if (curRegion.getIron() != -1) {
      resultRegion.setIron(curRegion.getIron());
    }

    if (curRegion.getIsland() != null) {
      final Island newIsland = resultGD.getIsland(curRegion.getIsland().getID());

      if (newIsland != null) {
        resultRegion.setIsland(newIsland);
      } else {
        MagellanFactory.log.warn("Region.merge(): island could not be found in the merged data: "
            + curRegion.getIsland());
      }
    }

    if (curRegion.getLaen() != -1) {
      resultRegion.setLaen(curRegion.getLaen());
    }

    resultRegion.setMallorn(resultRegion.isMallorn() || curRegion.isMallorn());

    if (curRegion.getPeasants() != -1) {
      resultRegion.setPeasants(curRegion.getPeasants());
    }

    if (curRegion.getSilver() != -1) {
      resultRegion.setSilver(curRegion.getSilver());
    }

    if (curRegion.getSprouts() != -1) {
      resultRegion.setSprouts(curRegion.getSprouts());
    }

    if (curRegion.getStones() != -1) {
      resultRegion.setStones(curRegion.getStones());
    }

    if (curRegion.getTrees() != -1) {
      resultRegion.setTrees(curRegion.getTrees());
    }

    if (curRegion.getWage() != -1) {
      resultRegion.setWage(curRegion.getWage());
    }

    /******************** PRICES *************************************/
    if ((curRegion.getPrices() != null) && (curRegion.getPrices().size() > 0)) {
      if (resultRegion.getPrices() == null) {
        resultRegion.setPrices(new OrderedHashtable<StringID, LuxuryPrice>());
      } else {
        resultRegion.getPrices().clear();
      }

      for (LuxuryPrice curPrice : curRegion.getPrices().values()) {
        final LuxuryPrice newPrice =
            new LuxuryPrice(resultGD.rules.getItemType(curPrice.getItemType().getID()), curPrice
                .getPrice());

        if (newPrice.getItemType() == null) {
          // this happens if there does exist an unknown tag in
          // the current block description
          MagellanFactory.log.warn("Invalid tag \"" + curPrice.getItemType()
              + "\" found in Region " + curRegion + ", ignoring it.");
        } else {
          resultRegion.getPrices().put(newPrice.getItemType().getID(), newPrice);
        }
      }
    }

    /******************** MERGE RESOURCES *************************************/
    if (!curRegion.resources().isEmpty()) {
      for (final RegionResource curRes : curRegion.resources()) {
        RegionResource newRes = resultRegion.getResource(curRes.getType());

        try {
          /**
           * Remember: Merging of regions works like follows: A new set of regions is created in the
           * new GameData object. Then first the regions of the older report are merged into that
           * new object. Then the regions of the newer report are merged into that new object. At
           * this time sameTurn is guaranteed to be true! The crucial point is when a resource is
           * suddenly not seen any longer, because its level has increased. Please note: -
           * resultRegion is always part of the resulting GameData - curRegion is always the data to
           * be "merged" into newRegion (if applicable) Fiete Special case Mallorn: it could have
           * disappeard (fully cut). In above way it is added from old region and not removed, if
           * not in new region. but we should only erase that mallorn info, if we have a unit in the
           * region for that we have to use curRegion (units not yet merged)
           */

          if (newRes == null) {
            // add Resource
            newRes =
                new RegionResource((ID) curRes.getID().clone(), resultGD.rules.getItemType(curRes
                    .getType().getID(), true));
            resultRegion.addResource(newRes);
          }

          RegionResource.merge(curGD, curRes, resultGD, newRes, sameTurn);

        } catch (final CloneNotSupportedException e) {
          MagellanFactory.log.error(e);
        }
      }
    }

    /******************** DELETE RESOURCES IF NECESSARY **********************************/
    // Now look for those resources, that are in the new created game data,
    // but not in the current one. These are those, that are not seen in the
    // maybe newer report! This maybe because their level has changed.
    // Types for which no skill is needed to see
    final ItemType horsesType = resultGD.rules.getItemType(EresseaConstants.I_RHORSES);
    final ItemType treesType = resultGD.rules.getItemType(EresseaConstants.I_TREES);
    final ItemType mallornType = resultGD.rules.getItemType(EresseaConstants.I_RMALLORN);
    final ItemType schoesslingeType = resultGD.rules.getItemType(EresseaConstants.I_SPROUTS);
    final ItemType mallornSchoesslingeType =
        resultGD.rules.getItemType(EresseaConstants.I_MALLORNSPROUTS);
    // FF 20080910...need new resources too
    final ItemType bauernType = resultGD.rules.getItemType(EresseaConstants.I_PEASANTS);
    final ItemType silberType = resultGD.rules.getItemType(EresseaConstants.I_RSILVER);

    // ArrayList of above Types
    final List<ItemType> skillIrrelevantTypes = new ArrayList<ItemType>();
    skillIrrelevantTypes.add(horsesType);
    skillIrrelevantTypes.add(treesType);
    skillIrrelevantTypes.add(mallornType);
    skillIrrelevantTypes.add(schoesslingeType);
    skillIrrelevantTypes.add(mallornSchoesslingeType);
    // FF 20080910...need new resources too
    if (bauernType != null) {
      skillIrrelevantTypes.add(bauernType);
    }
    if (silberType != null) {
      skillIrrelevantTypes.add(silberType);
    }

    if ((resultRegion.resources() != null) && !resultRegion.resources().isEmpty()) {
      List<ItemType> deleteRegionRessources = null;
      for (final RegionResource newRes : resultRegion.resources()) {
        final RegionResource curRes = curRegion.getResource(newRes.getType());

        if (curRes == null) {
          // check whether talent is good enough that it should be seen!
          // Keep in mind, that the units are not yet merged (Use those of
          // curRegion)
          boolean found = false;

          // new coding with same effect but better performance
          final Skill makeSkill = newRes.getType().getMakeSkill();
          if (makeSkill != null) {
            for (final Iterator<Unit> i = curRegion.units().iterator(); i.hasNext() && !found;) {
              final Unit unit = i.next();
              final Skill skill = unit.getSkill(makeSkill.getSkillType());
              if (skill != null) {
                if (skill.getLevel() >= newRes.getSkillLevel()) {
                  found = true;
                }
              }
            }
          }

          if (found) {
            // enforce this information to be taken!
            if (newRes.getSkillLevel() == -1 && newRes.getAmount() == -1) {
              // but only if we don't have other informations.

              // TODO: (darcduck) i don't understand the following line
              newRes.setSkillLevel(newRes.getSkillLevel() + 1);
              newRes.setAmount(-1);
            }
          }
          // Fiete: check here if we have skillIrrelevantResources
          // if curRes == null AND we have units in curReg -> these
          // resources are really not there anymore: Baeume, Mallorn

          // sameTurn must be true here, otherwise we would not reach that code
          // to be here newRes!=null and curRes==null
          // this cannot be true in the first merge pass
          // in the second merge pass sameTurn is always true
          // if (sameTurn){
          if (!sameTurn) {
            MagellanFactory.log.warn("Fiete thinks this cannot happen");
          }

          if (skillIrrelevantTypes.contains(newRes.getType())) {
            // we have "our" Type
            // better using the visibility: 3 or 4 should show resources
            if (curRegion.getVisibility().compareTo(Visibility.TRAVEL) >= 0) {
              // we have. So we know now for sure, that this
              // resource disappeared. So lets delete it.
              if (deleteRegionRessources == null) {
                deleteRegionRessources = new ArrayList<ItemType>();
              }
              deleteRegionRessources.add(newRes.getType());
            }

          }

        }
      }
      if (deleteRegionRessources != null) {
        // so we have Resources, that are not present any more
        for (final ItemType regResID : deleteRegionRessources) {
          // newRegion.resources().remove(regResID);
          // doesn't work, as it doesn't modify the
          // collection (only the hashset)
          resultRegion.removeResource(regResID);
        }
      }
    }

    /******************** SCHEMES *************************************/
    if (!curRegion.schemes().isEmpty()) {
      for (Scheme curScheme : curRegion.schemes()) {
        Scheme newScheme = resultRegion.getScheme(curScheme.getID());

        if (newScheme == null) {
          newScheme = MagellanFactory.createScheme(curScheme.getID());
          resultRegion.addScheme(newScheme);
        }

        MagellanFactory.mergeScheme(curGD, curScheme, resultGD, newScheme);
      }
    }

    /******************** SIGNS *************************************/
    if (curRegion.getSigns() != null && curRegion.getSigns().size() > 0) {
      // new overwriting old ones...
      resultRegion.clearSigns();
      resultRegion.addSigns(curRegion.getSigns());
    }

    /******************** MESSAGES *************************************/
    // Messages are special because they can contain different
    // data for different factions in the same turn.
    // Take new messages and stuff only into the new game data
    // if the two source game data objects are not from the
    // same turn and curGD is the newer game data or if both
    // are from the same turn. Both conditions are tested by the
    // following if statement
    if (curGD.getDate().equals(resultGD.getDate())) {
      if ((curRegion.getEvents() != null) && (curRegion.getEvents().size() > 0)) {
        if (resultRegion.getEvents() == null) {
          resultRegion.setEvents(new LinkedList<Message>());
        }

        for (Message curMsg : curRegion.getEvents()) {
          Message newMsg = null;

          try {
            newMsg = MagellanFactory.createMessage(curMsg.getID().clone());
          } catch (final CloneNotSupportedException e) {
          }

          MagellanFactory.mergeMessage(curGD, curMsg, resultGD, newMsg);
          resultRegion.getEvents().add(newMsg);
        }
      }

      if ((curRegion.getMessages() != null) && (curRegion.getMessages().size() > 0)) {
        if (resultRegion.getMessages() == null) {
          resultRegion.setMessages(new LinkedList<Message>());
        }

        for (Message curMsg : curRegion.getMessages()) {
          Message newMsg = null;

          try {
            newMsg = MagellanFactory.createMessage(curMsg.getID().clone());
          } catch (final CloneNotSupportedException e) {
          }

          MagellanFactory.mergeMessage(curGD, curMsg, resultGD, newMsg);
          resultRegion.getMessages().add(newMsg);
        }
      }

      if ((curRegion.getPlayerMessages() != null) && (curRegion.getPlayerMessages().size() > 0)) {
        if (resultRegion.getPlayerMessages() == null) {
          resultRegion.setPlayerMessages(new LinkedList<Message>());
        }

        for (Message curMsg : curRegion.getPlayerMessages()) {
          Message newMsg = null;

          try {
            newMsg = MagellanFactory.createMessage(curMsg.getID().clone());
          } catch (final CloneNotSupportedException e) {
          }

          MagellanFactory.mergeMessage(curGD, curMsg, resultGD, newMsg);
          resultRegion.getPlayerMessages().add(newMsg);
        }
      }

      if ((curRegion.getSurroundings() != null) && (curRegion.getSurroundings().size() > 0)) {
        if (resultRegion.getSurroundings() == null) {
          resultRegion.setSurroundings(new LinkedList<Message>());
        }

        for (Message curMsg : curRegion.getSurroundings()) {
          Message newMsg = null;

          try {
            newMsg = MagellanFactory.createMessage(curMsg.getID().clone());
          } catch (final CloneNotSupportedException e) {
          }

          MagellanFactory.mergeMessage(curGD, curMsg, resultGD, newMsg);
          resultRegion.getSurroundings().add(newMsg);
        }
      }

      if ((curRegion.getTravelThru() != null) && (curRegion.getTravelThru().size() > 0)) {
        if (resultRegion.getTravelThru() == null) {
          resultRegion.setTravelThru(new LinkedList<Message>());
        }

        for (Message curMsg : curRegion.getTravelThru()) {
          Message newMsg = null;

          try {
            newMsg = MagellanFactory.createMessage(curMsg.getID().clone());
          } catch (final CloneNotSupportedException e) {
          }

          MagellanFactory.mergeMessage(curGD, curMsg, resultGD, newMsg);

          // 2002.02.21 pavkovic: prevent double entries
          if (!resultRegion.getTravelThru().contains(newMsg)) {
            resultRegion.getTravelThru().add(newMsg);
          } else {
          }
        }
      }

      if ((curRegion.getTravelThruShips() != null) && (curRegion.getTravelThruShips().size() > 0)) {
        if (resultRegion.getTravelThruShips() == null) {
          resultRegion.setTravelThruShips(new LinkedList<Message>());
        }

        for (Message curMsg : curRegion.getTravelThruShips()) {
          Message newMsg = null;

          try {
            newMsg = MagellanFactory.createMessage(curMsg.getID().clone());
          } catch (final CloneNotSupportedException e) {
          }

          MagellanFactory.mergeMessage(curGD, curMsg, resultGD, newMsg);

          // 2002.02.21 pavkovic: prevent duplicate entries
          if (!resultRegion.getTravelThruShips().contains(newMsg)) {
            resultRegion.getTravelThruShips().add(newMsg);
          } else {
          }
        }
      }
    }
  }

  /**
   * Merges two Scheme objects.
   */
  public static void mergeScheme(GameData curGD, Scheme curScheme, GameData newGD, Scheme newScheme) {
    if (curScheme.getName() != null) {
      newScheme.setName(curScheme.getName());
    }
  }

  /**
   * Merges ships.
   */
  public static void mergeShip(GameData curGD, Ship curShip, GameData newGD, Ship newShip) {
    MagellanFactory.mergeUnitContainer(curGD, curShip, newGD, newShip);

    if (curShip.getCargo() != -1) {
      newShip.setCargo(curShip.getCargo());
    }

    if (curShip.getCapacity() != -1) {
      newShip.setCapacity(curShip.getCapacity());
    }

    if (curShip.getDeprecatedCapacity() != -1) {
      newShip.setDeprecatedCapacity(curShip.getDeprecatedCapacity());
    }

    if (curShip.getDamageRatio() != -1) {
      newShip.setDamageRatio(curShip.getDamageRatio());
    }

    if (curShip.getDeprecatedLoad() != -1) {
      newShip.setDeprecatedLoad(curShip.getDeprecatedLoad());
    }

    if (curShip.getRegion() != null) {
      newShip.setRegion(newGD.getRegion(curShip.getRegion().getID()));
    }

    newShip.setShoreId(curShip.getShoreId());

    if (curShip.getSize() != -1) {
      newShip.setSize(curShip.getSize());
    }

    if (curShip.getMaxPersons() != -1) {
      newShip.setMaxPersons(curShip.getMaxPersons());
    }
  }

  /**
   * Merges spells.
   */
  public static void mergeSpell(GameData curGD, Spell curSpell, GameData newGD, Spell newSpell) {
    if (curSpell.getBlockID() != -1) {
      newSpell.setBlockID(curSpell.getBlockID());
    }

    if (curSpell.getName() != null) {
      newSpell.setName(curSpell.getName());
    }

    if ((curSpell.getDescription() != null) && (curGD.getLocale().equals(newGD.getLocale()))) {
      newSpell.setDescription(curSpell.getDescription());
    }

    if (curSpell.getLevel() != -1) {
      newSpell.setLevel(curSpell.getLevel());
    }

    if (curSpell.getRank() != -1) {
      newSpell.setRank(curSpell.getRank());
    }

    if (curSpell.getType() != null) {
      newSpell.setType(curSpell.getType());
    }

    // FF 20070221: syntax
    if (curSpell.getSyntax() != null) {
      newSpell.setSyntax(curSpell.getSyntax());
    }

    if (curSpell.getOnShip() != false) {
      newSpell.setOnShip(curSpell.getOnShip());
    }

    if (curSpell.getOnOcean() != false) {
      newSpell.setOnOcean(curSpell.getOnOcean());
    }

    if (curSpell.getIsFamiliar() != false) {
      newSpell.setIsFamiliar(curSpell.getIsFamiliar());
    }

    if (curSpell.getIsFar() != false) {
      newSpell.setIsFar(curSpell.getIsFar());
    }

    if ((curSpell.getComponents() != null) && (curSpell.getComponents().size() > 0)) {
      newSpell.setComponents(new OrderedHashtable<String, String>());
      newSpell.getComponents().putAll(curSpell.getComponents());
    }
  }

  /**
   * Merges two temp units.
   * 
   * @param sameRound notifies if both game data objects have been from the same round
   */
  public static void merge(GameData curGD, TempUnit curTemp, GameData newGD, TempUnit newTemp,
      boolean sameRound, boolean firstPass) {
    MagellanFactory.mergeUnit(curGD, curTemp, newGD, newTemp, sameRound, firstPass);

    if (curTemp.getParent() != null) {
      newTemp.setParent(newGD.getUnit(curTemp.getParent().getID()));
    }
  }

  /**
   * Merges only the comments of <code>curShip</code> to <code>newShip</code>. Use if you don't want
   * to do a full merge.
   * 
   * @param curShip
   * @param newShip
   */
  // TODO (stm 2007-02-19) this is a workaround, we need a nicer solution
  public static void mergeComments(UnitContainer curShip, UnitContainer newShip) {
    if (newShip != null && curShip.getComments() != null) {
      if (newShip.getComments() == null) {
        newShip.setComments(new LinkedList<String>());
      }
      newShip.getComments().addAll(curShip.getComments());
    }

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
   */
  public static void mergeUnit(GameData curGD, Unit curUnit, GameData resultGD, Unit resultUnit,
      boolean sameRound, boolean firstPass) {
    /*
     * True, when curUnit is seen by the faction it belongs to and is therefore fully specified.
     */
    final boolean curWellKnown = !curUnit.ordersAreNull() || (curUnit.getCombatStatus() != -1);

    /*
     * True, when newUnit is seen by the faction it belongs to and is therefore fully specified.
     * This is only meaningful in the second pass.
     */
    final boolean newWellKnown =
        !resultUnit.ordersAreNull() || (resultUnit.getCombatStatus() != -1);

    if (curUnit.getName() != null) {
      resultUnit.setName(curUnit.getName());
    }

    if (curUnit.getDescription() != null) {
      resultUnit.setDescription(curUnit.getDescription());
    }

    if (curUnit.getAlias() != null) {
      try {
        resultUnit.setAlias(curUnit.getAlias().clone());
      } catch (final CloneNotSupportedException e) {
      }
    }

    if (curUnit.getAura() != -1) {
      resultUnit.setAura(curUnit.getAura());
    }

    if (curUnit.getAuraMax() != -1) {
      resultUnit.setAuraMax(curUnit.getAuraMax());
    }

    if (curUnit.getFamiliarmageID() != null) {
      resultUnit.setFamiliarmageID(curUnit.getFamiliarmageID());
    }

    if (curUnit.isWeightWellKnown()) {
      resultUnit.setWeight(curUnit.getSimpleWeight());
    }

    if (curUnit.getBuilding() != null) {
      resultUnit.setBuilding(resultGD.getBuilding(curUnit.getBuilding().getID()));
    }

    resultUnit.setCache(null);

    if ((curUnit.getCombatSpells() != null) && (curUnit.getCombatSpells().size() > 0)) {
      if (resultUnit.getCombatSpells() == null) {
        resultUnit.setCombatSpells(new Hashtable<ID, CombatSpell>());
      } else {
        resultUnit.getCombatSpells().clear();
      }

      for (CombatSpell curCS : curUnit.getCombatSpells().values()) {
        CombatSpell newCS = null;

        try {
          newCS = MagellanFactory.createCombatSpell(curCS.getID().clone());
        } catch (final CloneNotSupportedException e) {
          throw new RuntimeException("should never happen");
        }

        MagellanFactory.mergeCombatSpell(curGD, curCS, resultGD, newCS);
        resultUnit.getCombatSpells().put(newCS.getID(), newCS);
      }
    }

    if (!curUnit.ordersAreNull() && (curUnit.getCompleteOrders().size() > 0)) {
      resultUnit.setOrders(curUnit.getCompleteOrders(), false);
    }

    resultUnit.setOrdersConfirmed(resultUnit.isOrdersConfirmed() || curUnit.isOrdersConfirmed());

    if ((curUnit.getEffects() != null) && (curUnit.getEffects().size() > 0)) {
      if (resultUnit.getEffects() == null) {
        resultUnit.setEffects(new LinkedList<String>());
      } else {
        resultUnit.getEffects().clear();
      }

      resultUnit.getEffects().addAll(curUnit.getEffects());
    }

    if (curUnit.getFaction() != null) {
      if ((resultUnit.getFaction() == null) || curWellKnown) {
        resultUnit.setFaction(resultGD.getFaction(curUnit.getFaction().getID()));
      }
    }

    if (curUnit.getFollows() != null) {
      resultUnit.setFollows(resultGD.getUnit(curUnit.getFollows().getID()));
    }

    if ((curUnit.getGroup() != null) && (resultUnit.getFaction() != null)
        && (resultUnit.getFaction().getGroups() != null)) {
      resultUnit.setGroup(resultUnit.getFaction().getGroups().get(curUnit.getGroup().getID()));
    }

    if (curUnit.getGuard() != -1) {
      resultUnit.setGuard(curUnit.getGuard());
    }

    /*
     * There is a correlation between guise faction and isSpy. Since the guise faction can only be
     * known by the 'owner faction' it should override the isSpy value
     */
    if (curUnit.getGuiseFaction() != null) {
      resultUnit.setGuiseFaction(resultGD.getFaction(curUnit.getGuiseFaction().getID()));
    }

    resultUnit.setSpy((curUnit.isSpy() || resultUnit.isSpy())
        && (resultUnit.getGuiseFaction() == null));

    if (curUnit.getHealth() != null) {
      resultUnit.setHealth(curUnit.getHealth());
    }

    resultUnit.setHideFaction(resultUnit.isHideFaction() || curUnit.isHideFaction());
    resultUnit.setStarving(resultUnit.isStarving() || curUnit.isStarving());
    resultUnit.setHero(resultUnit.isHero() || curUnit.isHero());

    // do not overwrite the items in one special case:
    // if both source units are from the same turn, the first one
    // being well known and the second one not and this is the
    // second pass
    if (firstPass || !newWellKnown || curWellKnown) {
      if ((curUnit.getItems() != null) && (curUnit.getItems().size() > 0)) {
        if (resultUnit.getItemMap() == null) {
          resultUnit.setItems(new Hashtable<StringID, Item>());
        } else {
          resultUnit.getItemMap().clear();
        }

        for (Item curItem : curUnit.getItems()) {
          final Item newItem =
              new Item(resultGD.rules.getItemType(curItem.getItemType().getID(), true), curItem
                  .getAmount());
          resultUnit.getItemMap().put(newItem.getItemType().getID(), newItem);
        }
      }
    }

    if (curUnit.getPersons() != -1) {
      resultUnit.setPersons(curUnit.getPersons());
    }

    if (curUnit.getPrivDesc() != null) {
      resultUnit.setPrivDesc(curUnit.getPrivDesc());
    }

    if (curUnit.getDisguiseRace() != null) {
      resultUnit.setRealRace(resultGD.rules.getRace(curUnit.getRace().getID(), true));
      resultUnit.setRace(resultGD.rules.getRace(curUnit.getDisguiseRace().getID(), true));
    } else {
      resultUnit.setRace(resultGD.rules.getRace(curUnit.getRace().getID(), true));
    }

    if (curUnit.getRaceNamePrefix() != null) {
      resultUnit.setRaceNamePrefix(curUnit.getRaceNamePrefix());
    }

    if (curUnit.getRegion() != null) {
      resultUnit.setRegion(resultGD.getRegion(curUnit.getRegion().getID()));
    }

    if (curUnit.getCombatStatus() != -1) {
      resultUnit.setCombatStatus(curUnit.getCombatStatus());
    }

    if (curUnit.getShip() != null) {
      resultUnit.setShip(resultGD.getShip(curUnit.getShip().getID()));
    }

    if (curUnit.getSiege() != null) {
      resultUnit.setSiege(resultGD.getBuilding(curUnit.getSiege().getID()));
    }

    // this block requires newUnit.person to be already set!
    final Collection<Skill> oldSkills = new LinkedList<Skill>();

    if (resultUnit.getSkillMap() == null) {
      resultUnit.setSkills(new OrderedHashtable<StringID, Skill>());
    } else {
      oldSkills.addAll(resultUnit.getSkills());
    }

    if ((curUnit.getSkills() != null) && (curUnit.getSkills().size() > 0)) {
      for (final Skill curSkill : curUnit.getSkills()) {
        final SkillType newSkillType =
            resultGD.rules.getSkillType(curSkill.getSkillType().getID(), true);
        final Skill newSkill =
            new Skill(newSkillType, curSkill.getPoints(), curSkill.getLevel(), resultUnit
                .getPersons(), curSkill.noSkillPoints());

        if (curSkill.isLevelChanged()) {
          newSkill.setLevelChanged(true);
          newSkill.setChangeLevel(curSkill.getChangeLevel());
        }

        if (curSkill.isLostSkill()) {
          newSkill.setLevel(-1);
        }

        // NOTE: Maybe some decision about change-level computation in reports
        // of same date here
        final Skill oldSkill = resultUnit.getSkillMap().put(newSkillType.getID(), newSkill);

        if (!sameRound) {
          // notify change as we are not in the same round.
          if (oldSkill != null) {
            final int dec = oldSkill.getLevel();
            newSkill.setChangeLevel(newSkill.getLevel() - dec);
          } else {
            // the skill is new as we did not have it before
            newSkill.setLevelChanged(true);
            newSkill.setChangeLevel(newSkill.getLevel());
          }
        } else {
          // TR 2008-03-25
          // okay, this is a try to make it possible to show level changes
          // of multiple factions in one week. Problem: If you load a second
          // report from the same round, the level changes are not updates
          //
          // my solution now is to set the level if we found an old skill. The
          // old skill is actually the negative current level of the first pass
          //
          // there is a known problem if there are more known factions with
          // unknown skills (not imported factions). At the moment I don't
          // have an idea, how to handle this, because this happens in the
          // second pass...
          if (oldSkill != null) {
            if (oldSkill.getLevel() != newSkill.getLevel() && !newSkill.isLevelChanged()) {
              if (!firstPass) {
                newSkill.setChangeLevel(newSkill.getLevel() - oldSkill.getLevel());
                newSkill.setLevelChanged(true);
              } else {
                newSkill.setLevelChanged(false);
              }
            }
          } else {
            if (curSkill.getLevel() == 0 && newSkill.getLevel() == 0 && curSkill.isLevelChanged()
                && !curWellKnown) {
              newSkill.setLevel(curSkill.getLevel() + curSkill.getChangeLevel() * (-1));
            }
          }
        }

        if (oldSkill != null) {
          oldSkills.remove(oldSkill);
        }
      }
    }

    // pavkovic 2002.12.31: Remove oldSkills if the current unit is well known
    // if not, the old skill values stay where they are
    // pavkovic 2003.05.13: ...but never remove skills from the same round (as
    // before with items)
    // andreasg 2003.10.05: ...but if old skills from earlier date!
    // pavkovic 2004.01.27: now we remove oldSkills only if the round changed.
    if (!sameRound) {
      // Now remove all skills that are lost
      for (Skill oldSkill : oldSkills) {
        if (oldSkill.isLostSkill()) { // remove if it was lost
          resultUnit.getSkillMap().remove(oldSkill.getSkillType().getID());
        } else { // dont remove it but mark it as a lostSkill
          oldSkill.setChangeLevel(-oldSkill.getLevel());
          oldSkill.setLevel(-1);
        }
      }
    }

    resultUnit.setSortIndex(Math.max(resultUnit.getSortIndex(), curUnit.getSortIndex()));

    if ((curUnit.getSpells() != null) && (curUnit.getSpells().size() > 0)) {
      if (resultUnit.getSpells() == null) {
        resultUnit.setSpells(new Hashtable<ID, Spell>());
      } else {
        resultUnit.getSpells().clear();
      }

      for (Spell curSpell : curUnit.getSpells().values()) {
        final Spell newSpell = resultGD.getSpell(curSpell.getID());
        resultUnit.getSpells().put(newSpell.getID(), newSpell);
      }
    }

    if (curUnit.getStealth() != -1) {
      resultUnit.setStealth(curUnit.getStealth());
    }

    if (curUnit.getTempID() != null) {
      try {
        resultUnit.setTempID(curUnit.getTempID().clone());
      } catch (final CloneNotSupportedException e) {
        MagellanFactory.log.error(e);
      }
    }

    // temp units are created and merged in the merge methode of
    // the GameData class
    // new true iff cur true, new false iff cur false and well known
    if (curUnit.isUnaided()) {
      resultUnit.setUnaided(true);
    } else {
      if (curWellKnown) {
        resultUnit.setUnaided(false);
      }
    }

    // Messages are special because they can contain different
    // data for different factions in the same turn.
    // Take new messages and stuff only into the new game data
    // if the two source game data objects are from the
    // same turn and curGD is the newer game data or if both
    // are from the same turn. Both conditions are tested by the
    // following if statement
    if (!sameRound) {
      resultUnit.setUnitMessages(null);
    }

    if ((curUnit.getUnitMessages() != null) && (curUnit.getUnitMessages().size() > 0)) {
      if (resultUnit.getUnitMessages() == null) {
        resultUnit.setUnitMessages(new LinkedList<Message>());
      }

      for (Message curMsg : curUnit.getUnitMessages()) {
        Message newMsg = null;

        try {
          newMsg = MagellanFactory.createMessage(curMsg.getID().clone());
        } catch (final CloneNotSupportedException e) {
        }

        MagellanFactory.mergeMessage(curGD, curMsg, resultGD, newMsg);
        resultUnit.getUnitMessages().add(newMsg);
      }
    }

    if ((curUnit.getComments() != null) && (curUnit.getComments().size() > 0)) {
      if (resultUnit.getComments() == null) {
        resultUnit.setComments(new LinkedList<String>());
      }
      // else {
      // newUnit.comments.clear();
      // }

      resultUnit.getComments().addAll(curUnit.getComments());
    }

    // merge tags
    if (curUnit.hasTags()) {
      for (String tag : curUnit.getTagMap().keySet()) {
        resultUnit.putTag(tag, curUnit.getTag(tag));
      }
    }

    if (curUnit.getAttributeSize() > 0) {
      for (final String key : curUnit.getAttributeKeys()) {
        if (!resultUnit.containsAttribute(key)) {
          resultUnit.addAttribute(key, curUnit.getAttribute(key));
        }
      }
    }
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

      final Object msgArgs[] = { new Integer(combatStatus) };
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

  /**
   * Postprocess of Island objects. The Regions of the GameData are attached to their Island. The
   * Factions got their Race settings.
   */
  public static void postProcess(GameData data) {
    // create a map of region maps for every Island
    final Map<Island, Map<CoordinateID, Region>> islandMap =
        new Hashtable<Island, Map<CoordinateID, Region>>();

    for (final Region r : data.regions().values()) {
      if (r.getIsland() != null) {
        Map<CoordinateID, Region> actRegionMap = islandMap.get(r.getIsland());

        if (actRegionMap == null) {
          actRegionMap = new Hashtable<CoordinateID, Region>();
          islandMap.put(r.getIsland(), actRegionMap);
        }

        actRegionMap.put(r.getID(), r);
      }
    }

    // setRegions for every Island in the map of region maps.
    for (final Island island : islandMap.keySet()) {
      final Map<CoordinateID, Region> actRegionMap = islandMap.get(island);
      island.setRegions(actRegionMap);
    }

    // search for the races of the factions in the report.
    final Map<EntityID, Faction> factions = data.factions();

    for (final EntityID id : factions.keySet()) {
      final Faction faction = factions.get(id);

      // if the race is already set in the report ignore this algorithm
      if (faction.getType() != null) {
        continue;
      }

      final Map<Race, Integer> personsPerRace = new HashMap<Race, Integer>();

      // iterate thru all units and count the races of them
      final Collection<Unit> units = faction.units();
      for (final Unit unit : units) {
        final Race race = unit.getRace();
        if (race == null) {
          continue;
        }
        if (personsPerRace.containsKey(race)) {
          final int amount = personsPerRace.get(race) + unit.getPersons();
          personsPerRace.put(race, amount);
        } else {
          personsPerRace.put(race, unit.getPersons());
        }
      }

      // find the race with the most persons in it - this is the race of the
      // faction.
      int maxPersons = 0;
      Race race = null;
      for (final Race aRace : personsPerRace.keySet()) {
        final int amount = personsPerRace.get(aRace);
        if (amount > maxPersons) {
          maxPersons = amount;
          race = aRace;
        }
      }

      if (race != null) {
        faction.setType(race);
      }
    }
  }

  public static AllianceGroup createAlliance(EntityID id, GameData resultGD) {
    return new AllianceGroup(id);
  }

  public static void mergeAlliance(GameData curGD, AllianceGroup curAlliance, GameData newGD,
      AllianceGroup newAlliance) {
    newAlliance.setName(curAlliance.getName());
    newAlliance.setLeader(curAlliance.getLeader());

    for (final ID f : curAlliance.getFactions()) {
      newAlliance.addFaction(newGD.getFaction(f));
    }
  }
}
