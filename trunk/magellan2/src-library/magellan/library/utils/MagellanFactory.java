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
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU General Public License for more details.
// 
// You should have received a copy of the GNU General Public License
// along with this program (see doc/LICENCE.txt); if not, write to the
// Free Software Foundation, Inc., 
// 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
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
import magellan.library.Battle;
import magellan.library.Border;
import magellan.library.Building;
import magellan.library.CombatSpell;
import magellan.library.CoordinateID;
import magellan.library.Faction;
import magellan.library.GameData;
import magellan.library.Group;
import magellan.library.HotSpot;
import magellan.library.ID;
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
import magellan.library.TempUnit;
import magellan.library.Unit;
import magellan.library.UnitContainer;
import magellan.library.UnitID;
import magellan.library.ZeroUnit;
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
 * This factory returns all kind of Magellan objects.
 * ....
 *
 * @author Thoralf Rickert
 * @version 1.0, 01.05.2007
 */
public abstract class MagellanFactory {
  private static final Logger log = Logger.getInstance(MagellanFactory.class);
  
  
  public static Faction createFaction(ID id, GameData data) {
    return new MagellanFactionImpl(id,data);
  }
  
  public static Group createGroup(ID id, GameData data) {
    return new MagellanGroupImpl(id,data);
  }
  public static Group createGroup(ID id, GameData data, String name) {
    return new MagellanGroupImpl(id,data,name);
  }
  public static Group createGroup(ID id, GameData data, String name, Faction faction) {
    return new MagellanGroupImpl(id,data,name,faction);
  }
  
  public static Message createMessage(String text) {
    return new MagellanMessageImpl(text);
  }
  public static Message createMessage(ID id) {
    return new MagellanMessageImpl(id);
  }
  public static Message createMessage(ID id, String text) {
    return new MagellanMessageImpl(id,text);
  }
  public static Message createMessage(ID id, MessageType type, Map<String,String> attributes) {
    return new MagellanMessageImpl(id,type,attributes);
  }
  public static Message createMessage(Message message) {
    return new MagellanMessageImpl(message);
  }
  
  public static Region createRegion(CoordinateID id, GameData data) {
    return new MagellanRegionImpl(id,data);
  }
  
  public static Ship createShip(ID id, GameData data) {
    return new MagellanShipImpl(id,data);
  }

  public static Unit createUnit(ID id) {
    return new MagellanUnitImpl(id);
  }
  
  public static Spell createSpell(ID id, GameData data) {
    return new MagellanSpellImpl(id,data);
  }

  public static Battle createBattle(ID id) {
    return new MagellanBattleImpl(id);
  }
  public static Battle createBattle(ID id, boolean spec) {
    return new MagellanBattleImpl(id, spec);
  }
  public static Border createBorder(ID id) {
    return new MagellanBorderImpl(id);
  }
  public static Border createBorder(ID id, int direction, String type, int buildratio) {
    return new MagellanBorderImpl(id,direction,type,buildratio);
  }
  
  public static Building createBuilding(ID id, GameData data) {
    return new MagellanBuildingImpl(id,data);
  }
  
  public static CombatSpell createCombatSpell(ID id) {
    return new MagellanCombatSpellImpl(id);
  }
  public static HotSpot createHotSpot(ID id) {
    return new MagellanHotSpotImpl(id);
  }
  public static Island createIsland(ID id,  GameData data) {
    return new MagellanIslandImpl(id,data);
  }
  public static Potion createPotion(ID id) {
    return new MagellanPotionImpl(id);
  }
  public static Scheme createScheme(ID id) {
    return new MagellanSchemeImpl(id);
  }
  public static TempUnit createTempUnit(ID id, Unit parent) {
    return new MagellanTempUnitImpl(id,parent);
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
    if(curGroup.getName() != null) {
      newGroup.setName(curGroup.getName());
    }

    if((curGroup.allies() != null) && (curGroup.allies().size() > 0)) {
      if(newGroup.allies() == null) {
        newGroup.setAllies(new Hashtable<ID, Alliance>());
      } else {
        newGroup.allies().clear();
      }

      for(Iterator<Alliance> iter = curGroup.allies().values().iterator(); iter.hasNext();) {
        Alliance alliance = iter.next();
        Faction ally = newGD.getFaction(alliance.getFaction().getID());
        newGroup.allies().put(ally.getID(), new Alliance(ally, alliance.getState()));
      }
    }

    if(curGroup.getFaction() != null) {
      newGroup.setFaction(newGD.getFaction(curGroup.getFaction().getID()));
    }

    newGroup.setSortIndex(Math.max(newGroup.getSortIndex(), curGroup.getSortIndex()));

    if(curGroup.getRaceNamePrefix() != null) {
      newGroup.setRaceNamePrefix(curGroup.getRaceNamePrefix());
    }
  }
  

  /**
   * DOCUMENT-ME
   *
   * 
   * 
   * 
   * 
   */
  public static void mergeFaction(GameData curGD, Faction curFaction, GameData newGD, Faction newFaction) {
    mergeUnitContainer(curGD, curFaction, newGD, newFaction);

    if((curFaction.getAllies() != null) && (curFaction.getAllies().size() > 0)) {
      if(newFaction.getAllies() == null) {
        newFaction.setAllies(new OrderedHashtable<ID, Alliance>());
      } else {
        newFaction.getAllies().clear();
      }

      for(Iterator<Alliance> iter = curFaction.getAllies().values().iterator(); iter.hasNext();) {
        Alliance alliance = iter.next();
        Faction ally = newGD.getFaction(alliance.getFaction().getID());
        newFaction.getAllies().put(ally.getID(), new Alliance(ally, alliance.getState()));
      }
    }

    if(curFaction.getEmail() != null) {
      newFaction.setEmail(curFaction.getEmail());
    }

    if((curFaction.getGroups() != null) && (curFaction.getGroups().size() > 0)) {
      if(newFaction.getGroups() == null) {
        newFaction.setGroups(new Hashtable<ID, Group>());
      } else {
        newFaction.getGroups().clear();
      }

      for(Iterator<Group> iter = curFaction.getGroups().values().iterator(); iter.hasNext();) {
        Group curGroup = iter.next();
        Group newGroup = null;

        try {
          newGroup = MagellanFactory.createGroup((ID) curGroup.getID().clone(), newGD);
        } catch(CloneNotSupportedException e) {
        }

        mergeGroup(curGD, curGroup, newGD, newGroup);
        newFaction.getGroups().put(newGroup.getID(), newGroup);
      }
    }

    if(curFaction.getLocale() != null) {
      newFaction.setLocale(curFaction.getLocale());
    }

    if(curFaction.getMaxMigrants() != -1) {
      newFaction.setMaxMigrants(curFaction.getMaxMigrants());
    }

    if(curFaction.getOptions() != null) {
      newFaction.setOptions(new Options(curFaction.getOptions()));
    }

    if(curFaction.getPassword() != null) {
      newFaction.setPassword(curFaction.getPassword());
    }

    if(curFaction.getSpellSchool() != null) {
      newFaction.setSpellSchool(curFaction.getSpellSchool());
    }

    // one trustLevel is guaranteed to be TL_DEFAULT
    // ReportMerger.mergeReport() :

    /**
     * prepare faction trustlevel for merging: - to be added CR is older or of same age -> hold
     * existing trust levels - to be added CR is newer and contains trust level that were set
     * by the user explicitly (or read from CR what means the same) -> take the trust levels
     * out of the new CR otherwise -> hold existing trust levels This means: set those trust
     * levels, that will not be retained to default values
     */
    if((curFaction.getTrustLevel() != Faction.TL_DEFAULT) || curFaction.isTrustLevelSetByUser()) {
      newFaction.setTrustLevel(curFaction.getTrustLevel());
      newFaction.setTrustLevelSetByUser(curFaction.isTrustLevelSetByUser());
    }

    // see Region.merge() for the meaning of the following if
    if(curGD.getDate().equals(newGD.getDate())) {
      if(curFaction.getAverageScore() != -1) {
        newFaction.setAverageScore(curFaction.getAverageScore());
      }

      if((curFaction.getBattles() != null) && (curFaction.getBattles().size() > 0)) {
        newFaction.setBattles(new LinkedList<Battle>());

        for(Iterator iter = curFaction.getBattles().iterator(); iter.hasNext();) {
          Battle curBattle = (Battle) iter.next();

          try {
            Battle newBattle = MagellanFactory.createBattle((ID) curBattle.getID().clone(),curBattle.isBattleSpec());

            for(Iterator msgs = curBattle.messages().iterator(); msgs.hasNext();) {
              Message curMsg = (Message) msgs.next();
              Message newMsg = MagellanFactory.createMessage((ID) curMsg.getID().clone());
              mergeMessage(curGD, curMsg, newGD, newMsg);
              newBattle.messages().add(newMsg);
            }

            newFaction.getBattles().add(curBattle);
          } catch(CloneNotSupportedException e) {
            log.error("Faction.merge()", e);
          }
        }
      }

      if((curFaction.getErrors() != null) && (curFaction.getErrors().size() > 0)) {
        newFaction.setErrors(new LinkedList<String>(curFaction.getErrors()));
      }

      if((curFaction.getMessages() != null) && (curFaction.getMessages().size() > 0)) {
        if(newFaction.getMessages() == null) {
          newFaction.setMessages(new LinkedList<Message>());
        } else {
          newFaction.getMessages().clear();
        }

        for(Iterator<Message> iter = curFaction.getMessages().iterator(); iter.hasNext();) {
          Message curMsg = iter.next();
          Message newMsg = null;

          try {
            newMsg = MagellanFactory.createMessage((ID) curMsg.getID().clone());
          } catch(CloneNotSupportedException e) {
          }

          mergeMessage(curGD, curMsg, newGD, newMsg);
          newFaction.getMessages().add(newMsg);
        }
      }

      if(curFaction.getMigrants() != -1) {
        newFaction.setMigrants(curFaction.getMigrants());
      }

      if(curFaction.getPersons() != -1) {
        newFaction.setPersons(curFaction.getPersons());
      }

      if(curFaction.getRaceNamePrefix() != null) {
        newFaction.setRaceNamePrefix(curFaction.getRaceNamePrefix());
      }

      if(curFaction.getScore() != -1) {
        newFaction.setScore(curFaction.getScore());
      }
      
      /**
       * Fiete: merge also new (20061102) hereos values and age
       */
      if(curFaction.getHeroes() != -1) {
        newFaction.setHeroes(curFaction.getHeroes());
      }
      
      if(curFaction.getMaxHeroes() != -1) {
        newFaction.setMaxHeroes(curFaction.getMaxHeroes());
      }
      
      if(curFaction.getAge() != -1) {
        newFaction.setAge(curFaction.getAge());
      }
      
      
      if(curFaction.getItems()!=null){
        if(curFaction.getItems().size()>0){
          for (Iterator<Item> iter = curFaction.getItems().iterator();iter.hasNext();){
            Item currentItem = iter.next();
            newFaction.addItem(currentItem);
          }
        }
      }
      
      
    }
  }


  /**
   * Merges UnitContainers.
   */
  public static void mergeUnitContainer(GameData curGD, UnitContainer curUC, GameData newGD, UnitContainer newUC) {
    if(curUC.getName() != null) {
      newUC.setName(curUC.getName());
    }

    if(curUC.getDescription() != null) {
      newUC.setDescription(curUC.getDescription());
    }

    if((curUC.getComments() != null) && (curUC.getComments().size() > 0)) {
      if(newUC.getComments() == null) {
        newUC.setComments(new LinkedList<String>());
      } 
//      else {
//        newUC.comments.clear();
//      }

      newUC.getComments().addAll(curUC.getComments());
    }

    // see Region.merge() for the meaning of the following if
    if(curGD.getDate().equals(newGD.getDate())) {
      if((curUC.getEffects() != null) && (curUC.getEffects().size() > 0)) {
        if(newUC.getEffects() == null) {
          newUC.setEffects(new LinkedList<String>());
        } else {
          newUC.getEffects().clear();
        }

        newUC.getEffects().addAll(curUC.getEffects());
      }
    }

    if(curUC.getOwner() != null) {
      newUC.setOwner(newGD.getUnit(curUC.getOwner().getID()));
    } else {
      newUC.setOwner(null);
    }

    if(curUC.getType() != null) {
      if(curUC instanceof Building) {
        newUC.setType(newGD.rules.getBuildingType(curUC.getType().getID(), true));
      } else if(curUC instanceof Region) {
        // pavkovic 2004.01.03: (bugzilla bug 801): overwrite with curUC.getType if
        // known or newUC.getType is same as "unknown" (this is a miracle to me but
        // Ulrich has more experiences with "Astralraum" :-))
        // if (newUC.getType() == null || newUC.getType().equals(RegionType.unknown)) {
        if ((curUC.getType() != null && !curUC.getType().equals(RegionType.unknown)) ||
          newUC.getType() == null ||
          newUC.getType().equals(RegionType.unknown)) {
          newUC.setType(newGD.rules.getRegionType(curUC.getType().getID(), true));
        }
      } else if(curUC instanceof Ship) {
        newUC.setType(newGD.rules.getShipType(curUC.getType().getID(), true));
      } else if(curUC instanceof Faction) {
        newUC.setType(newGD.rules.getRace(curUC.getType().getID(), true));
      }
    }

    //copy tags
    if(curGD.getDate().equals(newGD.getDate()) && curUC.hasTags()) {
      Iterator it = curUC.getTagMap().keySet().iterator();

      while(it.hasNext()) {
        String str = (String) it.next();

        if(!newUC.containsTag(str)) {
          newUC.putTag(str, curUC.getTag(str));
        }
      }
    }

    newUC.setCache(null);

    newUC.setSortIndex(Math.max(newUC.getSortIndex(), curUC.getSortIndex()));

  }  


  /**
   * Transfers all available information from the current message to the new one.
   *
   * @param curGD fully loaded game data
   * @param curMsg a fully initialized and valid message
   * @param newGD the game data to be updated
   * @param newMsg a message to be updated with the data from curMsg
   */
  public static void mergeMessage(GameData curGD, Message curMsg, GameData newGD, Message newMsg) {
    if((curMsg.getAttributes() != null) && (curMsg.getAttributes().size() > 0)) {
      if(newMsg.getAttributes() == null) {
        newMsg.setAttributes(new OrderedHashtable<String, String>());
      } else {
        newMsg.getAttributes().clear();
      }

      newMsg.getAttributes().putAll(curMsg.getAttributes());
    }

    if(curMsg.getText() != null) {
      newMsg.setText(curMsg.getText());
    }

    if(curMsg.getMessageType() != null) {
      newMsg.setType(newGD.getMsgType(curMsg.getMessageType().getID()));
    }
  }

  /**
   * Merges potion.
   */
  public static void mergePotion(GameData curGD, Potion curPotion, GameData newGD, Potion newPotion) {
    if(curPotion.getName() != null) {
      newPotion.setName(curPotion.getName());
    }

    if(curPotion.getDescription() != null) {
      newPotion.setDescription(curPotion.getDescription());
    }

    if(curPotion.getLevel() != -1) {
      newPotion.setLevel(curPotion.getLevel());
    }

    if(!curPotion.ingredients().isEmpty()) {
      newPotion.clearIngredients();

      for(Iterator iter = curPotion.ingredients().iterator(); iter.hasNext();) {
        Item i = (Item) iter.next();
        magellan.library.rules.ItemType it = newGD.rules.getItemType(i.getItemType().getID(),
                                    true);
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
   *
   * @see UnitContainer#merge
   */
  public static void mergeBuilding(GameData curGD, Building curBuilding, GameData newGD, Building newBuilding) {
    mergeUnitContainer(curGD, curBuilding, newGD, newBuilding);

    if(curBuilding.getCost() != -1) {
      newBuilding.setCost(curBuilding.getCost());
    }

    if(curBuilding.getRegion() != null) {
      newBuilding.setRegion(newGD.getRegion((CoordinateID) curBuilding.getRegion().getID()));
    }

    if(curBuilding.getSize() != -1) {
      newBuilding.setSize(curBuilding.getSize());
    }
    
    // Fiete 20060910
    // added support for wahrerTyp
    if (curBuilding.getTrueBuildingType()!=null) {
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
  public static void mergeCombatSpell(GameData curGD, CombatSpell curCS, GameData newGD, CombatSpell newCS) {
    // transfer the level of the casted spell
    if(curCS.getCastingLevel() != -1) {
      newCS.setCastingLevel(curCS.getCastingLevel());
    }

    // transfer the spell
    if(curCS.getSpell() != null) {
      newCS.setSpell(newGD.getSpell(curCS.getSpell().getID()));
    }

    // transfer the casting unit
    if(curCS.getUnit() != null) {
      newCS.setUnit(newGD.getUnit(curCS.getUnit().getID()));
    }
  }

  /**
   * Merges two HotSpot objects.
   */
  public static void mergeHotSpot(GameData curGD, HotSpot curHS, GameData newGD, HotSpot newHS) {
    if(curHS.getName() != null) {
      newHS.setName(curHS.getName());
    }

    if(curHS.getCenter() != null) {
      try {
        newHS.setCenter((ID) curHS.getCenter().clone());
      } catch(CloneNotSupportedException e) {
        // impossible position, should throw a runtime exception here
      }
    }
  }

  /**
   * Merges island.
   */
  public static void mergeIsland(GameData curGD, Island curIsland, GameData newGD, Island newIsland) {
    if(curIsland.getName() != null) {
      newIsland.setName(curIsland.getName());
    }

    if(curIsland.getDescription() != null) {
      newIsland.setDescription(curIsland.getDescription());
    }

    newIsland.invalidateRegions();
  }

  /**
   * Merges regions.
   */
  // TODO should name this either sameTurn everywhere or sameRound everywhere
  // sameTurn == false actually indicates that this method is to be called again
  // with the same "newRegion" but a more recent "curRegion".
  public static void mergeRegion(GameData curGD, Region curRegion, GameData newGD, Region newRegion,boolean sameTurn) {
    mergeUnitContainer(curGD, curRegion, newGD, newRegion);
    
    if(sameTurn) {
      // if both regions are from the same turn, "old" information is always assumed to be accurate. 
      // this is true, if curRegion is always younger for successive calls of Region.merge(). 
      if(curRegion.getOldTrees() != -1) {
        newRegion.setOldTrees(curRegion.getOldTrees());
      }
    } else {
      if(!curGD.getDate().equals(newGD.getDate())) {
        // curRegion is actually from an older round, so its information is old!
        if(curRegion.getTrees() != -1) {
          newRegion.setOldTrees(curRegion.getTrees());
        }
      } else {
        // curRegion is from a more recent round. Therefore
        // TODO: (stm) thinks this can never happen!
        log.error("Warning: reached code in Region.merge, that (stm) thought could never be reached!");
        if(curRegion.getTrees() == -1) {
          newRegion.setOldTrees(-1);
        }
      }
    }

    // same as with the old trees
    if(sameTurn) {
      if(curRegion.getOldSprouts() != -1) {
        newRegion.setOldSprouts(curRegion.getOldSprouts());
      }
    } else {
      if(!curGD.getDate().equals(newGD.getDate())) {
        if(curRegion.getSprouts() != -1) {
          newRegion.setOldSprouts(curRegion.getSprouts());
        }
      } else {
        if(curRegion.getSprouts() == -1) {
          newRegion.setOldSprouts(-1);
        }
      }
    }

    // same as with the old trees
    if(sameTurn) {
      if(curRegion.getOldIron() != -1) {
        newRegion.setOldIron(curRegion.getOldIron());
      }
    } else {
      if(!curGD.getDate().equals(newGD.getDate())) {
        if(curRegion.getIron() != -1) {
          newRegion.setOldIron(curRegion.getIron());
        }
      } else {
        if(curRegion.getIron() == -1) {
          newRegion.setOldIron( -1);
        }
      }
    }

    // same as with the old trees
    if(sameTurn) {
      if(curRegion.getOldLaen() != -1) {
        newRegion.setOldLaen(curRegion.getOldLaen());
      }
    } else {
      if(!curGD.getDate().equals(newGD.getDate())) {
        if(curRegion.getLaen() != -1) {
          newRegion.setOldLaen(curRegion.getLaen());
        }
      } else {
        if(curRegion.getLaen() == -1) {
          newRegion.setOldLaen(-1);
        }
      }
    }

    if(sameTurn) {
      // region is considered orc infested if one of the two regions considers it orc infested.
      newRegion.setOrcInfested(newRegion.isOrcInfested() || curRegion.isOrcInfested());
    } else {
      newRegion.setOrcInfested(curRegion.isOrcInfested());
    }

    // same as with the old trees
    if(sameTurn) {
      if(curRegion.getOldPeasants() != -1) {
        newRegion.setOldPeasants(curRegion.getOldPeasants());
      }
    } else {
      if(!curGD.getDate().equals(newGD.getDate())) {
        if(curRegion.getPeasants() != -1) {
          newRegion.setOldPeasants(curRegion.getPeasants());
        }
      } else {
        if(curRegion.getPeasants() == -1) {
          newRegion.setOldPeasants(-1);
        }
      }
    }

    // same as with the old trees
    if(sameTurn) {
      if(curRegion.getOldSilver() != -1) {
        newRegion.setOldSilver(curRegion.getOldSilver());
      }
    } else {
      if(!curGD.getDate().equals(newGD.getDate())) {
        if(curRegion.getSilver() != -1) {
          newRegion.setOldSilver(curRegion.getSilver());
        }
      } else {
        if(curRegion.getSilver() == -1) {
          newRegion.setOldSilver(-1);
        }
      }
    }

    // same as with the old trees
    if(sameTurn) {
      if(curRegion.getOldStones() != -1) {
        newRegion.setOldStones(curRegion.getOldStones());
      }
    } else {
      if(!curGD.getDate().equals(newGD.getDate())) {
        if(curRegion.getStones() != -1) {
          newRegion.setOldStones(curRegion.getStones());
        }
      } else {
        if(curRegion.getStones() == -1) {
          newRegion.setOldStones(-1);
        }
      }
    }

    // same as with the old trees
    if(sameTurn) {
      if(curRegion.getOldHorses() != -1) {
        newRegion.setOldHorses(curRegion.getOldHorses());
      }
    } else {
      if(!curGD.getDate().equals(newGD.getDate())) {
        if(curRegion.getHorses() != -1) {
          newRegion.setOldHorses(curRegion.getHorses());
        }
      } else {
        if(curRegion.getHorses() == -1) {
          newRegion.setOldHorses(-1);
        }
      }
    }

    // same as with the old trees
    if(sameTurn) {
      if(curRegion.getOldWage() != -1) {
        newRegion.setOldWage(curRegion.getOldWage());
      }
    } else {
      if(!curGD.getDate().equals(newGD.getDate())) {
        if(curRegion.getWage() != -1) {
          newRegion.setOldWage(curRegion.getWage());
        }
      } else {
        if(curRegion.getWage() == -1) {
          newRegion.setOldWage(-1);
        }
      }
    }

    // same as with the old trees
    if(sameTurn) {
      if(curRegion.getOldRecruits() != -1) {
        newRegion.setOldRecruits(curRegion.getOldRecruits());
      }
    } else {
      if(!curGD.getDate().equals(newGD.getDate())) {
        if(curRegion.getRecruits() != -1) {
          newRegion.setOldRecruits(curRegion.getRecruits());
        }
      } else {
        if(curRegion.getRecruits() == -1) {
          newRegion.setOldRecruits(-1);
        }
      }
    }

    if((newRegion.getPrices() != null) && (curRegion.getPrices() != null) &&
         !curRegion.getPrices().equals(newRegion.getPrices())) {
      newRegion.setOldPrices(new Hashtable<ID, LuxuryPrice>());

      for(Iterator iter = newRegion.getPrices().values().iterator(); iter.hasNext();) {
        LuxuryPrice curPrice = (LuxuryPrice) iter.next();
        LuxuryPrice newPrice = new LuxuryPrice(newGD.rules.getItemType(curPrice.getItemType()
                                             .getID()),
                             curPrice.getPrice());
        newRegion.getOldPrices().put(newPrice.getItemType().getID(), newPrice);
      }
    } else if(curRegion.getOldPrices() != null) {
      newRegion.setOldPrices(new Hashtable<ID, LuxuryPrice>());

      for(Iterator iter = curRegion.getOldPrices().values().iterator(); iter.hasNext();) {
        LuxuryPrice curPrice = (LuxuryPrice) iter.next();
        LuxuryPrice newPrice = new LuxuryPrice(newGD.rules.getItemType(curPrice.getItemType()
                                             .getID()),
                             curPrice.getPrice());

        if(newPrice.getItemType() == null) {
          // this happens if there does exist an unknown tag in
          // the current block description
          log.warn("WARNING: Invalid tag \"" + curPrice.getItemType() +
               "\" found in Region " + curRegion + ", ignoring it.");
        } else {
          newRegion.getOldPrices().put(newPrice.getItemType().getID(), newPrice);
        }
      }
    }

    //  TODO
    if(!sameTurn) {
      /* as long as both reports are from different turns we
       can just overwrite the visibility status with the newer
       version */
      newRegion.setVisibility(curRegion.getVisibility());
    } else {
      /* this where trouble begins: reports from the same turn
       so we basically have 4 visibility status:
       1 contains units (implicit)
       2 travel (explicit)
       3 lighthouse (explicit)
       4 next to a unit containing region (implicit)
       now - how do we merge this?
       for a start, we just make sure that the visibility
       value is not lost */
      if(curRegion.getVisibility() != null) {
        newRegion.setVisibility(curRegion.getVisibility());
      }
    }
    
    
    /*
        // from Region.java.~1.19~
    // pavkovic 2002.04.12: This logic seems to be more reasonable:
    // prerequisites: there are borders in the current region
    // if there are no borders in the new region
    //   -> the borders of the current region are added to the new region
    // if there are borders in the new region *and* there is at least one
    //    person in the current region
    //   -> the borders of the current region are added to the new region
    //
    if(!curRegion.borders().isEmpty() &&
         (newRegion.borders().isEmpty() || !curRegion.units().isEmpty())) {
    */

    /*
    // pavkovic 2004.06.03: This logic seems to be more reasonable:
    // 
    // |new.units| == 0, |current.units| == 0: current
    // |new.units| == 0, |current.units| != 0: current
    // |new.units| != 0, |current.units| == 0: new
    // |new.units| != 0, |current.units| != 0: sameTurn ? (merge/current) : current
    //
    // FIXME(pavkovic) bug# 819
    //  the problem:
    // we have a region with one person and one road not in the same turn
    // and add a region with no person and no road: Who wins?
    // If I would know that in the elder game data did exist a person the elder information
    // wins.
    // 
    // nice try but still buggy:
    if(!curRegion.units().isEmpty() || (newRegion.units().isEmpty() && curRegion.borders != null &&!(curRegion.borders.isEmpty()))) {
    */
    
    // if we have units in the current region the current region wins.
    // if we are not in the same turn the current region wins.
    // if we dont have units in the new region the current region wins.
    // if(!curRegion.units().isEmpty() || !sameTurn || newRegion.units().isEmpty()) {
    
    // test: determine, if newregion is seen just bei neighbour
    // bug #37: such regions only contain borders to regions we have units in....
    // we will try to the currentRegion win in that case, because newRegionData is not complete
    boolean newregionSeenByNeighbour = false;
    if (newRegion.getVisibility()!=null && newRegion.getVisibility().equalsIgnoreCase("neighbour")){
      newregionSeenByNeighbour=true;
    }
    
    if((!curRegion.units().isEmpty() || !sameTurn || newRegion.units().isEmpty()) && !newregionSeenByNeighbour) {
      // currentRegion wins
      // take borders for new region from current
      newRegion.clearBorders();

      for(Iterator iter = curRegion.borders().iterator(); iter.hasNext();) {
        Border curBorder = (Border) iter.next();
        Border newBorder = null;

        try {
          newBorder = MagellanFactory.createBorder((ID) curBorder.getID().clone(), curBorder.getDirection(),curBorder.getType(), curBorder.getBuildRatio());
        } catch(CloneNotSupportedException e) {
        }

        newRegion.addBorder(newBorder);
      }
    }

    if(curRegion.getHerb() != null) {
      newRegion.setHerb(newGD.rules.getItemType(curRegion.getHerb().getID(), true));
    }

    if(curRegion.getHerbAmount() != null) {
      /* FIXME There was a bug around 2002.02.16 where numbers would be
       stored in this field - filter them out. This should only
       be here for one or two weeks. */
      if(curRegion.getHerbAmount().length() > 2) {
        newRegion.setHerbAmount(curRegion.getHerbAmount());
      }
    }

    if(curRegion.getHorses() != -1) {
      newRegion.setHorses(curRegion.getHorses());
    }

    if(curRegion.getIron() != -1) {
      newRegion.setIron(curRegion.getIron());
    }

    if(curRegion.getIsland() != null) {
      Island newIsland = newGD.getIsland(curRegion.getIsland().getID());

      if(newIsland != null) {
        newRegion.setIsland(newIsland);
      } else {
        log.warn("Region.merge(): island could not be found in the merged data: " +
             curRegion.getIsland());
      }
    }

    if(curRegion.getLaen() != -1) {
      newRegion.setLaen(curRegion.getLaen());
    }

    newRegion.setMallorn(newRegion.isMallorn() || curRegion.isMallorn());

    if(curRegion.getPeasants() != -1) {
      newRegion.setPeasants(curRegion.getPeasants());
    }

    if((curRegion.getPrices() != null) && (curRegion.getPrices().size() > 0)) {
      if(newRegion.getPrices() == null) {
        newRegion.setPrices(new OrderedHashtable<ID,LuxuryPrice>());
      } else {
        newRegion.getPrices().clear();
      }

      for(Iterator iter = curRegion.getPrices().values().iterator(); iter.hasNext();) {
        LuxuryPrice curPrice = (LuxuryPrice) iter.next();
        LuxuryPrice newPrice = new LuxuryPrice(newGD.rules.getItemType(curPrice.getItemType()
                                             .getID()),
                             curPrice.getPrice());

        if(newPrice.getItemType() == null) {
          // this happens if there does exist an unknown tag in
          // the current block description
          log.warn("Invalid tag \"" + curPrice.getItemType() + "\" found in Region " +
               curRegion + ", ignoring it.");
        } else {
          newRegion.getPrices().put(newPrice.getItemType().getID(), newPrice);
        }
      }
    }

    if(!curRegion.resources().isEmpty()) {
      for(Iterator iter = curRegion.resources().iterator(); iter.hasNext();) {
        RegionResource curRes = (RegionResource) iter.next();
        RegionResource newRes = newRegion.getResource(curRes.getType());

        try {
          /**
           * Remember: Merging of regions works like follows: A new set of regions is
           * created in the new GameData object. Then first the regions of the older
           * report are merged into that new object. Then the regions of the newer
           * report are merged into that new object. At this time sameTurn is guaranteed
           * to be true! The crucial point is when a resource is suddenly not seen any
           * longer, because its level has increased.
           * 
           * Fiete Special case Mallorn: it could be disappeard -> fully cutted. In above way
           * it is added from old region and not removed, if not in new region.
           * but we should only erase that mallorn info, if we have a unit in the region
           * for that we have to use curRegion (units not yet merged)
           * 
           * 
           */
          
          if(newRes == null) {
            // add Resource
            newRes = new RegionResource((ID) curRes.getID().clone(),
                          newGD.rules.getItemType(curRes.getType().getID(),
                                      true));
            newRegion.addResource(newRes);
          }
          
          RegionResource.merge(curGD, curRes, newGD, newRes, sameTurn);
          
        } catch(CloneNotSupportedException e) {
          log.error(e);
        }
      }
    }

    // Now look for those resources, that are in the new created game data,
    // but not in the current one. These are those, that are not seen in the
    // maybe newer report! This maybe because their level has changed.
    
    // Types for which no skill is needed to see
    ItemType treesType = newGD.rules.getItemType("Baeume");
    ItemType mallornType = newGD.rules.getItemType("Mallorn");
    ItemType schoesslingeType = newGD.rules.getItemType("Schoesslinge");
    ItemType mallornSchoesslingeType = newGD.rules.getItemType("Mallornschoesslinge");
    
    // ArrayList of above Types
    List<ItemType> skillIrrelavntTypes = new ArrayList<ItemType>();
    skillIrrelavntTypes.add(treesType);
    skillIrrelavntTypes.add(mallornType);
    skillIrrelavntTypes.add(schoesslingeType);
    skillIrrelavntTypes.add(mallornSchoesslingeType);
    
    if((newRegion.resources() != null) && !newRegion.resources().isEmpty()) {
      List<ItemType> deleteRegionRessources = null;
      for(Iterator<RegionResource> iter = newRegion.resources().iterator(); iter.hasNext();) {
        RegionResource newRes = iter.next();
        RegionResource curRes = curRegion.getResource(newRes.getType());

        if(curRes == null) {
          // check wheather talent is good enogh that it should be seen!
          // Keep in mind, that the units are not yet merged (Use those of curRegion)
          boolean found = false;

          for(Iterator<Unit> i = curRegion.units().iterator(); i.hasNext() && !found;) {
            Unit unit = i.next();

            if(unit.getSkills() != null) {
              for(Iterator<Skill> skillIterator = unit.getSkills().iterator();
                  skillIterator.hasNext() && !found;) {
                Skill skill = (Skill) skillIterator.next();
                Skill makeSkill = newRes.getType().getMakeSkill();

                if((makeSkill != null) &&
                     skill.getSkillType().equals(makeSkill.getSkillType())) {
                  // found a unit with right skill, level high enough?
                  if(skill.getLevel() >= newRes.getSkillLevel()) {
                    found = true;
                  }
                }
              }
            }
          }

          if(found) {
            // enforce this information to be taken!
            if(newRes.getSkillLevel() == -1 && newRes.getAmount() == -1) {
              // but only if we don't have other informations.
              newRes.setSkillLevel(newRes.getSkillLevel() + 1);
              newRes.setAmount(-1);
            }
          } 
          // Fiete: check here if we have skillIrrelevantResources
          // if curRes == null AND we have units in curReg -> these
          // resources are realy not there anymore: Baeume, Mallorn
          if (sameTurn){
            if (skillIrrelavntTypes.contains(newRes.getType())){
              // we have "our" Type
              // do we have units in newRegion
              // if (newRegion.units()!=null && newRegion.units().size()>0){
              if (curRegion.units()!=null && curRegion.units().size()>0){
                // we have...so we know now for sure, that these 
                // ressource disappeared..so lets delete it
                if (deleteRegionRessources==null){
                  deleteRegionRessources = new ArrayList<ItemType>();
                }
                deleteRegionRessources.add(newRes.getType());
              }
              
            }
          }
          
        }
      }
      if (deleteRegionRessources!=null){
        // so we have Ressources, which are not present any more
        for (Iterator iter = deleteRegionRessources.iterator();iter.hasNext();){
          ItemType regResID = (ItemType)iter.next();
          newRegion.resources().remove(regResID);
        }
      }
    }

    if(!curRegion.schemes().isEmpty()) {
      for(Iterator iter = curRegion.schemes().iterator(); iter.hasNext();) {
        Scheme curScheme = (Scheme) iter.next();
        Scheme newScheme = newRegion.getScheme(curScheme.getID());

        try {
          if(newScheme == null) {
            newScheme = MagellanFactory.createScheme((ID) curScheme.getID().clone());
            newRegion.addScheme(newScheme);
          }

          mergeScheme(curGD, curScheme, newGD, newScheme);
        } catch(CloneNotSupportedException e) {
          log.error(e);
        }
      }
    }

    if(curRegion.getSilver() != -1) {
      newRegion.setSilver(curRegion.getSilver());
    }

    if(curRegion.getSprouts() != -1) {
      newRegion.setSprouts(curRegion.getSprouts());
    }

    if(curRegion.getStones() != -1) {
      newRegion.setStones(curRegion.getStones());
    }

    if(curRegion.getTrees() != -1) {
      newRegion.setTrees(curRegion.getTrees());
    }

    

    if(curRegion.getWage() != -1) {
      newRegion.setWage(curRegion.getWage());
    }

    //  signs
    if (curRegion.getSigns()!=null && curRegion.getSigns().size()>0){
      // new overwriting old ones...
      newRegion.clearSigns();
      newRegion.addSigns(curRegion.getSigns());
    }
    
    
    // Messages are special because they can contain different
    // data for different factions in the same turn.
    // Take new messages and stuff only into the new game data
    // if the two source game data objects are not from the
    // same turn and curGD is the newer game data or if both
    // are from the same turn. Both conditions are tested by the
    // following if statement
    if(curGD.getDate().equals(newGD.getDate())) {
      if((curRegion.getEvents() != null) && (curRegion.getEvents().size() > 0)) {
        if(newRegion.getEvents() == null) {
          newRegion.setEvents(new LinkedList<Message>());
        }

        for(Iterator<Message> iter = curRegion.getEvents().iterator(); iter.hasNext();) {
          Message curMsg = iter.next();
          Message newMsg = null;

          try {
            newMsg = MagellanFactory.createMessage((ID) curMsg.getID().clone());
          } catch(CloneNotSupportedException e) {
          }

          mergeMessage(curGD, curMsg, newGD, newMsg);
          newRegion.getEvents().add(newMsg);
        }
      }

      if((curRegion.getMessages() != null) && (curRegion.getMessages().size() > 0)) {
        if(newRegion.getMessages() == null) {
          newRegion.setMessages(new LinkedList<Message>());
        }

        for(Iterator<Message> iter = curRegion.getMessages().iterator(); iter.hasNext();) {
          Message curMsg = iter.next();
          Message newMsg = null;

          try {
            newMsg = MagellanFactory.createMessage((ID) curMsg.getID().clone());
          } catch(CloneNotSupportedException e) {
          }

          mergeMessage(curGD, curMsg, newGD, newMsg);
          newRegion.getMessages().add(newMsg);
        }
      }

      if((curRegion.getPlayerMessages() != null) && (curRegion.getPlayerMessages().size() > 0)) {
        if(newRegion.getPlayerMessages() == null) {
          newRegion.setPlayerMessages(new LinkedList<Message>());
        }

        for(Iterator<Message> iter = curRegion.getPlayerMessages().iterator(); iter.hasNext();) {
          Message curMsg = iter.next();
          Message newMsg = null;

          try {
            newMsg = MagellanFactory.createMessage((ID) curMsg.getID().clone());
          } catch(CloneNotSupportedException e) {
          }

          mergeMessage(curGD, curMsg, newGD, newMsg);
          newRegion.getPlayerMessages().add(newMsg);
        }
      }

      if((curRegion.getSurroundings() != null) && (curRegion.getSurroundings().size() > 0)) {
        if(newRegion.getSurroundings() == null) {
          newRegion.setSurroundings(new LinkedList<Message>());
        }

        for(Iterator<Message> iter = curRegion.getSurroundings().iterator(); iter.hasNext();) {
          Message curMsg = iter.next();
          Message newMsg = null;

          try {
            newMsg = MagellanFactory.createMessage((ID) curMsg.getID().clone());
          } catch(CloneNotSupportedException e) {
          }

          mergeMessage(curGD, curMsg, newGD, newMsg);
          newRegion.getSurroundings().add(newMsg);
        }
      }

      if((curRegion.getTravelThru() != null) && (curRegion.getTravelThru().size() > 0)) {
        if(newRegion.getTravelThru() == null) {
          newRegion.setTravelThru(new LinkedList<Message>());
        }

        for(Iterator<Message> iter = curRegion.getTravelThru().iterator(); iter.hasNext();) {
          Message curMsg = iter.next();
          Message newMsg = null;

          try {
            newMsg = MagellanFactory.createMessage((ID) curMsg.getID().clone());
          } catch(CloneNotSupportedException e) {
          }

          mergeMessage(curGD, curMsg, newGD, newMsg);

          // 2002.02.21 pavkovic: prevent double entries
          if(!newRegion.getTravelThru().contains(newMsg)) {
            newRegion.getTravelThru().add(newMsg);
          } else {
            //log.warn("Region.merge(): Duplicate message \"" + newMsg.getText() +
            //     "\", removing it.");

            /*
            if(log.isDebugEnabled()) {
                log.debug("list: "+newRegion.travelThru);
                log.debug("entry:"+newMsg);
            }
            */
          }
        }
      }

      if((curRegion.getTravelThruShips() != null) && (curRegion.getTravelThruShips().size() > 0)) {
        if(newRegion.getTravelThruShips() == null) {
          newRegion.setTravelThruShips(new LinkedList<Message>());
        }

        for(Iterator<Message> iter = curRegion.getTravelThruShips().iterator(); iter.hasNext();) {
          Message curMsg = iter.next();
          Message newMsg = null;

          try {
            newMsg = MagellanFactory.createMessage((ID) curMsg.getID().clone());
          } catch(CloneNotSupportedException e) {
          }

          mergeMessage(curGD, curMsg, newGD, newMsg);

          // 2002.02.21 pavkovic: prevent duplicate entries
          if(!newRegion.getTravelThruShips().contains(newMsg)) {
            newRegion.getTravelThruShips().add(newMsg);
          } else {
            //log.warn("Region.merge(): Duplicate message \"" + newMsg.getText() +
            //     "\", removing it.");

            /*
            if(log.isDebugEnabled()) {
                log.debug("list: "+newRegion.travelThruShips);
                log.debug("entry:"+newMsg);
            }
            */
          }
        }
      }
    }
  }

  /**
   * Merges two Scheme objects.
   */
  public static void mergeScheme(GameData curGD, Scheme curScheme, GameData newGD, Scheme newScheme) {
    if(curScheme.getName() != null) {
      newScheme.setName(curScheme.getName());
    }
  }

  /**
   * Merges ships.
   */
  public static void mergeShip(GameData curGD, Ship curShip, GameData newGD, Ship newShip) {
    mergeUnitContainer(curGD, curShip, newGD, newShip);

    if(curShip.getCargo() != -1) {
      newShip.setCargo( curShip.getCargo());
    }

    if(curShip.getCapacity() != -1) {
      newShip.setCapacity(curShip.getCapacity());
    }

    if(curShip.getDeprecatedCapacity() != -1) {
      newShip.setDeprecatedCapacity(curShip.getDeprecatedCapacity());
    }

    if(curShip.getDamageRatio() != -1) {
      newShip.setDamageRatio(curShip.getDamageRatio());
    }

    if(curShip.getDeprecatedLoad() != -1) {
      newShip.setDeprecatedLoad(curShip.getDeprecatedLoad());
    }

    if(curShip.getRegion() != null) {
      newShip.setRegion(newGD.getRegion((CoordinateID) curShip.getRegion().getID()));
    }

    newShip.setShoreId(curShip.getShoreId());

    if(curShip.getSize() != -1) {
      newShip.setSize(curShip.getSize());
    }
  }
  
  /**
   * Merges spells.
   */
  public static void mergeSpell(GameData curGD, Spell curSpell, GameData newGD, Spell newSpell) {
    if(curSpell.getBlockID() != -1) {
      newSpell.setBlockID(curSpell.getBlockID());
    }

    if(curSpell.getName() != null) {
      newSpell.setName(curSpell.getName());
    }

    if(curSpell.getDescription() != null) {
      newSpell.setDescription(curSpell.getDescription());
    }

    if(curSpell.getLevel() != -1) {
      newSpell.setLevel(curSpell.getLevel());
    }

    if(curSpell.getRank() != -1) {
      newSpell.setRank(curSpell.getRank());
    }

    if(curSpell.getType() != null) {
      newSpell.setType(curSpell.getType());
    }

    // FF 20070221: syntax
    if (curSpell.getSyntax()!=null && curSpell.getSyntax().length()>0){
      newSpell.setSyntax(curSpell.getSyntax());
    }
    
    if(curSpell.getOnShip() != false) {
      newSpell.setOnShip(curSpell.getOnShip());
    }

    if(curSpell.getOnOcean() != false) {
      newSpell.setOnOcean(curSpell.getOnOcean());
    }

    if(curSpell.getIsFamiliar() != false) {
      newSpell.setIsFamiliar(curSpell.getIsFamiliar());
    }

    if(curSpell.getIsFar() != false) {
      newSpell.setIsFar(curSpell.getIsFar());
    }

    if((curSpell.getComponents() != null) && (curSpell.getComponents().size() > 0)) {
      newSpell.setComponents(new OrderedHashtable<String, String>());
      newSpell.getComponents().putAll(curSpell.getComponents());
    }
  }
  
  /**
   * Merges two temp units.
   * 
   * @param sameRound notifies if both game data objects have been from the same round
   */
  public static void merge(GameData curGD, TempUnit curTemp, GameData newGD, TempUnit newTemp,boolean sameRound) {
    mergeUnit(curGD, curTemp, newGD, newTemp, sameRound);

    if(curTemp.getParent() != null) {
      newTemp.setParent(newGD.getUnit(curTemp.getParent().getID()));
    }
  }


  /**
   * Merges only the comments of <code>curShip</code> to <code>newShip</code>. Use if you
   * don't want to do a full merge.
   * 
   * @param curShip
   * @param newShip
   */
  // TODO (stm 2007-02-19) this is a workaround, we need a nicer solution
  public static void mergeComments(UnitContainer curShip, UnitContainer newShip) {
    if (newShip != null && curShip.getComments() != null) {
      if (newShip.getComments() == null)
        newShip.setComments(new LinkedList<String>());
      newShip.getComments().addAll(curShip.getComments());
    }

  }

  /**
   * DOCUMENT-ME
   * 
   * @param sameRound notifies if both game data objects have been from the same round
   */
  public static void mergeUnit(GameData curGD, Unit curUnit, GameData newGD, Unit newUnit, boolean sameRound) {
    /*
     * True, when curUnit is seen by the faction it belongs to and
     * is therefore fully specified.
     */
    boolean curWellKnown = !curUnit.ordersAreNull() || (curUnit.getCombatStatus() != -1);

    /*
     * True, when newUnit is seen by the faction it belongs to and
     * is therefore fully specified. This is only meaningful in
     * the second pass.
     */
    boolean newWellKnown = !newUnit.ordersAreNull() || (newUnit.getCombatStatus() != -1);

    /*
     * True, when newUnit is completely uninitialized, i.e. this
     * invokation of merge is the first one of the two to be
     * expected.
     */

    //boolean firstPass = (newUnit.getPersons() == 0);
    boolean firstPass = newUnit.getRegion() == null;

    if(curUnit.getName() != null) {
      newUnit.setName(curUnit.getName());
    }

    if(curUnit.getDescription() != null) {
      newUnit.setDescription(curUnit.getDescription());
    }

    if(curUnit.getAlias() != null) {
      try {
        newUnit.setAlias((UnitID) curUnit.getAlias().clone());
      } catch(CloneNotSupportedException e) {
      }
    }

    if(curUnit.getAura() != -1) {
      newUnit.setAura(curUnit.getAura());
    }

    if(curUnit.getAuraMax() != -1) {
      newUnit.setAuraMax(curUnit.getAuraMax());
    }
    
    if(curUnit.getFamiliarmageID() != null) {
      newUnit.setFamiliarmageID(curUnit.getFamiliarmageID());
    }
    
    if(curUnit.getWeight() != -1) {
      newUnit.setWeight(curUnit.getWeight());
    }
    
    if(curUnit.getBuilding() != null) {
      newUnit.setBuilding(newGD.getBuilding(curUnit.getBuilding().getID()));
    }

    newUnit.setCache(null);

    if((curUnit.getCombatSpells() != null) && (curUnit.getCombatSpells().size() > 0)) {
      if(newUnit.getCombatSpells() == null) {
        newUnit.setCombatSpells(new Hashtable<ID, CombatSpell>());
      } else {
        newUnit.getCombatSpells().clear();
      }

      for(Iterator<CombatSpell> iter = curUnit.getCombatSpells().values().iterator(); iter.hasNext();) {
        CombatSpell curCS = iter.next();
        CombatSpell newCS = null;

        try {
          newCS = MagellanFactory.createCombatSpell((ID) curCS.getID().clone());
        } catch(CloneNotSupportedException e) {
        }

        mergeCombatSpell(curGD, curCS, newGD, newCS);
        newUnit.getCombatSpells().put(newCS.getID(), newCS);
      }
    }

    if(!curUnit.ordersAreNull() && (curUnit.getCompleteOrders().size() > 0)) {
      newUnit.setOrders(curUnit.getCompleteOrders(), false);
    }

    newUnit.setOrdersConfirmed(newUnit.isOrdersConfirmed() || curUnit.isOrdersConfirmed());

    if((curUnit.getEffects() != null) && (curUnit.getEffects().size() > 0)) {
      if(newUnit.getEffects() == null) {
        newUnit.setEffects(new LinkedList<String>());
      } else {
        newUnit.getEffects().clear();
      }

      newUnit.getEffects().addAll(curUnit.getEffects());
    }

    if(curUnit.getFaction() != null) {
      if((newUnit.getFaction() == null) || curWellKnown) {
        newUnit.setFaction(newGD.getFaction(curUnit.getFaction().getID()));
      }
    }

    if(curUnit.getFollows() != null) {
      newUnit.setFollows(newGD.getUnit(curUnit.getFollows().getID()));
    }

    if((curUnit.getGroup() != null) && (newUnit.getFaction() != null) && (newUnit.getFaction().getGroups() != null)) {
      newUnit.setGroup(newUnit.getFaction().getGroups().get(curUnit.getGroup().getID()));
    }

    if(curUnit.getGuard() != -1) {
      newUnit.setGuard(curUnit.getGuard());
    }

    /* There is a correlation between guise faction and isSpy.
     Since the guise faction can only be known by the 'owner
     faction' it should override the isSpy value */
    if(curUnit.getGuiseFaction() != null) {
      newUnit.setGuiseFaction(newGD.getFaction(curUnit.getGuiseFaction().getID()));
    }

    newUnit.setSpy((curUnit.isSpy() || newUnit.isSpy()) && (newUnit.getGuiseFaction() == null));

    if(curUnit.getHealth() != null) {
      newUnit.setHealth(curUnit.getHealth());
    }

    newUnit.setHideFaction(newUnit.isHideFaction() || curUnit.isHideFaction());
    newUnit.setStarving(newUnit.isStarving() || curUnit.isStarving());
    newUnit.setHero(newUnit.isHero() || curUnit.isHero());

    // do not overwrite the items in one special case:
    // if both source units are from the same turn, the first one
    // being well known and the second one not and this is the
    // second pass
    if(firstPass || !newWellKnown || curWellKnown) {
      if((curUnit.getItems() != null) && (curUnit.getItems().size() > 0)) {
        if(newUnit.getItemMap() == null) {
          newUnit.setItems(new Hashtable<ID, Item>());
        } else {
          newUnit.getItemMap().clear();
        }

        for(Iterator<Item> iter = curUnit.getItems().iterator(); iter.hasNext();) {
          Item curItem = iter.next();
          Item newItem = new Item(newGD.rules.getItemType(curItem.getItemType().getID(),
                                  true), curItem.getAmount());
          newUnit.getItemMap().put(newItem.getItemType().getID(), newItem);
        }
      }
    }

    if(curUnit.getPersons() != -1) {
      newUnit.setPersons(curUnit.getPersons());
    }

    if(curUnit.getPrivDesc() != null) {
      newUnit.setPrivDesc(curUnit.getPrivDesc());
    }

    if(curUnit.getRace() != null) {
      newUnit.setRace(newGD.rules.getRace(curUnit.getRace().getID(), true));
    }

    if(curUnit.getRaceNamePrefix() != null) {
      newUnit.setRaceNamePrefix(curUnit.getRaceNamePrefix());
    }

    if(curUnit.getRealRace() != null) {
      newUnit.setRealRace(newGD.rules.getRace(curUnit.getRealRace().getID(), true));
    }

    if(curUnit.getRegion() != null) {
      newUnit.setRegion(newGD.getRegion((CoordinateID) curUnit.getRegion().getID()));
    }

    if(curUnit.getCombatStatus() != -1) {
      newUnit.setCombatStatus(curUnit.getCombatStatus());
    }

    if(curUnit.getShip() != null) {
      newUnit.setShip(newGD.getShip(curUnit.getShip().getID()));
    }

    if(curUnit.getSiege() != null) {
      newUnit.setSiege(newGD.getBuilding(curUnit.getSiege().getID()));
    }

    // this block requires newUnit.person to be already set!
    Collection<Skill> oldSkills = new LinkedList<Skill>();

    if(newUnit.getSkillMap() == null) {
      newUnit.setSkills(new OrderedHashtable<ID, Skill>());
    } else {
      oldSkills.addAll(newUnit.getSkills());
    }

    if(log.isDebugEnabled()) {
      log.debug("Unit.merge: curUnit.skills: " + curUnit.getSkills());
      log.debug("Unit.merge: newUnit.skills: " + newUnit.getSkills());
    }

    if((curUnit.getSkills() != null) && (curUnit.getSkills().size() > 0)) {
      for(Iterator<Skill> iter = curUnit.getSkills().iterator(); iter.hasNext();) {
        Skill curSkill = iter.next();
        SkillType newSkillType = newGD.rules.getSkillType(curSkill.getSkillType().getID(),true);
        Skill newSkill = new Skill(newSkillType, curSkill.getPoints(), curSkill.getLevel(),newUnit.getPersons(), curSkill.noSkillPoints());

        if(curSkill.isLevelChanged()) {
          newSkill.setLevelChanged(true);
          newSkill.setChangeLevel(curSkill.getChangeLevel());
        }

        if(curSkill.isLostSkill()) {
          newSkill.setLevel(-1);
        }

        // NOTE: Maybe some decision about change-level computation in reports of
        //       same date here
        Skill oldSkill = newUnit.getSkillMap().put(newSkillType.getID(), newSkill);

        if(!sameRound) {
          // notify change as we are not in the same round.
          if(oldSkill != null) {
            int dec = oldSkill.getLevel();
            newSkill.setChangeLevel(newSkill.getLevel() - dec);
          } else {
            // the skill is new as we did not have it before
            newSkill.setLevelChanged(true);
            newSkill.setChangeLevel(newSkill.getLevel());
          }
        }

        if(oldSkill != null) {
          oldSkills.remove(oldSkill);
        }
      }
    }

    // pavkovic 2002.12.31: Remove oldSkills if the current unit is well known
    // if not, the old skill values stay where they are
    // pavkovic 2003.05.13: ...but never remove skills from the same round (as before with items)
    // andreasg 2003.10.05: ...but if old skills from earlier date!
    // pavkovic 2004.01.27: now we remove oldSkills only if the round changed.
    if(!sameRound) {
      // Now remove all skills that are lost
      for(Iterator iter = oldSkills.iterator(); iter.hasNext();) {
        Skill oldSkill = (Skill) iter.next();

        if(oldSkill.isLostSkill()) { // remove if it was lost
          newUnit.getSkillMap().remove(oldSkill.getSkillType().getID());
        } else { // dont remove it but mark it as a lostSkill 
          oldSkill.setChangeLevel(-oldSkill.getLevel());
          oldSkill.setLevel(-1);
        }
      }
    }

    newUnit.setSortIndex(Math.max(newUnit.getSortIndex(), curUnit.getSortIndex()));

    if((curUnit.getSpells() != null) && (curUnit.getSpells().size() > 0)) {
      if(newUnit.getSpells() == null) {
        newUnit.setSpells(new Hashtable<ID,Spell>());
      } else {
        newUnit.getSpells().clear();
      }

      for(Iterator<Spell> iter = curUnit.getSpells().values().iterator(); iter.hasNext();) {
        Spell curSpell = iter.next();
        Spell newSpell = newGD.getSpell(curSpell.getID());
        newUnit.getSpells().put(newSpell.getID(), newSpell);
      }
    }

    if(curUnit.getStealth() != -1) {
      newUnit.setStealth(curUnit.getStealth());
    }

    if(curUnit.getTempID() != null) {
      try {
        newUnit.setTempID((UnitID) curUnit.getTempID().clone());
      } catch(CloneNotSupportedException e) {
        log.error(e);
      }
    }

    // temp units are created and merged in the merge methode of
    // the GameData class
    // new true iff cur true, new false iff cur false and well known
    if(curUnit.isUnaided()) {
      newUnit.setUnaided(true);
    } else {
      if(curWellKnown) {
        newUnit.setUnaided(false);
      }
    }

    // Messages are special because they can contain different
    // data for different factions in the same turn.
    // Take new messages and stuff only into the new game data
    // if the two source game data objects are from the
    // same turn and curGD is the newer game data or if both
    // are from the same turn. Both conditions are tested by the
    // following if statement
    if(!sameRound) {
      newUnit.setUnitMessages(null);
    }

    if((curUnit.getUnitMessages() != null) && (curUnit.getUnitMessages().size() > 0)) {
      if(newUnit.getUnitMessages() == null) {
        newUnit.setUnitMessages(new LinkedList<Message>());
      }
      
      for(Iterator<Message> iter = curUnit.getUnitMessages().iterator(); iter.hasNext();) {
        Message curMsg = iter.next();
        Message newMsg = null;
        
        try {
          newMsg = MagellanFactory.createMessage((ID) curMsg.getID().clone());
        } catch(CloneNotSupportedException e) {
        }
        
        mergeMessage(curGD, curMsg, newGD, newMsg);
        newUnit.getUnitMessages().add(newMsg);
      }
    }

    
    if((curUnit.getComments() != null) && (curUnit.getComments().size() > 0)) {
      if(newUnit.getComments() == null) {
        newUnit.setComments(new LinkedList<String>());
      }
//       else {
//        newUnit.comments.clear();
//      }

      newUnit.getComments().addAll(curUnit.getComments());
    }
    
    
    // merge tags
    if(curUnit.hasTags()) {
      for(Iterator iter = curUnit.getTagMap().keySet().iterator(); iter.hasNext();) {
        String tag = (String) iter.next();
        newUnit.putTag(tag, curUnit.getTag(tag));
      }
    }
  }

  /**
   * Copies the skills of the given unit. Does not empty this unit's skills.
   */
  public static void copySkills(Unit u, Unit v) {
    copySkills(u, v, true);
  }

  /**
   * Copies the skills of the given unit. Does not empty this unit's skills.
   */
  public static void copySkills(Unit u, Unit v, boolean sortOut) {
    v.setSkillsCopied(true);

    if(u.getSkills() != null) {
      Iterator<Skill> it = u.getSkills().iterator();

      while(it.hasNext()) {
        Skill sk = it.next();

        // sort out if changed to non-existent
        if(sortOut && sk.isLostSkill()) {
          continue;
        }

        Skill newSkill = new Skill(sk.getSkillType(), sk.getPoints(), sk.getLevel(), v.getPersons(), sk.noSkillPoints());
        v.addSkill(newSkill);
      }
    }
  }


  /**
   * Returns a locale specific string representation of the specified unit combat status.
   */
  public static String combatStatusToString(Unit u) {
    String retVal = combatStatusToString(u.getCombatStatus());

    if(u.isUnaided()) {
      retVal += (", " + Resources.get("unit.combatstatus.unaided"));
    }

    return retVal;
  }
  
  /**
   * Returns a locale specific string representation of the specified unit combat status.
   */
  public static String combatStatusToString(int combatStatus) {
    String retVal = null;

    switch(combatStatus) {
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

      Object msgArgs[] = { new Integer(combatStatus) };
      retVal = (new java.text.MessageFormat(Resources.get("unit.combatstatus.unknown"))).format(msgArgs);
    }

    return retVal;
  }

  /**
   * see Unit.GUARDFLAG_  Converts guard flags into a readable string.
   */
  public static String guardFlagsToString(int iFlags) {
    String strFlags = "";

    if(iFlags != 0) {
      strFlags += Resources.get("unit.guard.region");
    }

    if((iFlags & Unit.GUARDFLAG_WOOD) != 0) {
      strFlags += (", " + Resources.get("unit.guard.wood"));
    }

    return strFlags;
  }

  /**
   * Postprocess of Island objects.
   *  
   * The Regions of the GameData are attached to their Island.
   * The Factions got their Race settings. 
   */
  public static void postProcess(GameData data) {
    // create a map of region maps for every Island
    Map<Island,Map<CoordinateID,Region>> islandMap = new Hashtable<Island, Map<CoordinateID,Region>>();

    for(Region r : data.regions().values()) {
      if(r.getIsland() != null) {
        Map<CoordinateID,Region> actRegionMap = islandMap.get(r.getIsland());

        if(actRegionMap == null) {
          actRegionMap = new Hashtable<CoordinateID, Region>();
          islandMap.put(r.getIsland(), actRegionMap);
        }

        actRegionMap.put((CoordinateID)r.getID(), r);
      }
    }

    // setRegions for every Island in the map of region maps.
    for(Island island : islandMap.keySet()) {
      Map<CoordinateID,Region> actRegionMap = islandMap.get(island);
      island.setRegions(actRegionMap);
    }
    
    
    
    // search for the races of the factions in the report.
    Map<ID,Faction> factions = data.factions();
    
    for (ID id : factions.keySet()) {
      Faction faction = factions.get(id);
      
      // if the race is already set in the report ignore this algorithm
      if (faction.getType() != null) continue;
      
      Map<Race,Integer> personsPerRace = new HashMap<Race, Integer>();
      
      // iterate thru all units and count the races of them
      Collection<Unit> units = faction.units();
      for (Unit unit : units) {
        Race race = unit.getRace();
        if (race == null) continue;
        if (personsPerRace.containsKey(race)) {
          int amount = personsPerRace.get(race) + unit.getPersons();
          personsPerRace.put(race, amount);
        } else {
          personsPerRace.put(race, unit.getPersons());
        }
      }
      
      // find the race with the most persons in it - this is the race of the faction.
      int maxPersons = 0;
      Race race = null;
      for (Race aRace : personsPerRace.keySet()) {
        int amount = personsPerRace.get(aRace);
        if (amount > maxPersons) {
          maxPersons = amount;
          race = aRace;
        }
      }
      
      if (race != null) faction.setType(race);
    }
  }
}
