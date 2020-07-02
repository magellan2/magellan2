// class magellan.library.GameDataMerger
// created on Feb 12, 2010
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
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
// GNU General Public License for more details.
//
// You should have received a copy of the GNU General Public License
// along with this program (see doc/LICENCE.txt); if not, write to the
// Free Software Foundation, Inc.,
// 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
//
package magellan.library;

import static magellan.library.merge.PropertyMerger.mergeBeans;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import magellan.library.Region.Visibility;
import magellan.library.gamebinding.EresseaConstants;
import magellan.library.merge.IslandMerger;
import magellan.library.rules.ItemType;
import magellan.library.rules.MessageType;
import magellan.library.rules.Options;
import magellan.library.rules.RegionType;
import magellan.library.rules.SkillType;
import magellan.library.utils.CollectionFactory;
import magellan.library.utils.Encoding;
import magellan.library.utils.MagellanFactory;
import magellan.library.utils.Regions;
import magellan.library.utils.Resources;
import magellan.library.utils.TrustLevels;
import magellan.library.utils.logging.Logger;
import magellan.library.utils.transformation.IdentityTransformer;
import magellan.library.utils.transformation.ReportTransformer;

/**
 * Utility class to merge two reports.
 *
 * @author stm
 */
public class GameDataMerger {

  private static final Logger log = Logger.getInstance(GameDataMerger.class);

  /**
   * Merges the specified dataset with this dataset.
   *
   * @param gd1 the first game data object for merging
   * @param gd2 the second game data object for merging
   * @return the new merged game data object
   * @throws IllegalArgumentException if first and second game data object are from different game
   *           types.
   */
  public static GameData merge(GameData gd1, GameData gd2) {
    return merge(gd1, gd2, new IdentityTransformer(), new IdentityTransformer());
  }

  /**
   * Returns a new report that is the given report translated by the given transformer.
   */
  public static GameData merge(GameData gd, ReportTransformer transformer) {
    return merge(gd, new EmptyData(gd), transformer, new IdentityTransformer());
  }

  /**
   * Returns a new report that is the given report translated by the given transformers. The first
   * transformer is applied to gd1, the second one to gd2.
   */
  public static GameData merge(GameData gd1, GameData gd2, ReportTransformer transformer1,
      ReportTransformer transformer2) {
    // make sure, the game types are the same.
    if (!gd1.getGameName().equalsIgnoreCase(gd2.getGameName())) {
      log.warn("GameData.merge(): Shouldn't merge different game types. (" + gd1.getGameName()
          + " != " + gd2.getGameName() + ")");
    }

    GameData resultGD;
    if (gd1.getDate().compareTo(gd2.getDate()) > 0) {
      resultGD = mergeIt(gd2, gd1, transformer2, transformer1);
    } else {
      resultGD = mergeIt(gd1, gd2, transformer1, transformer2);
    }

    mergeBeans(gd1, resultGD).merge("fileType");

    return resultGD;
  }

  /**
   * Merges the two game data containers yielding a third one. By convention, olderGD must not be
   * newer than newerGD. The resulting game data container inherits the rules and name from
   * <b>newerGD</b>.
   *
   * @param olderGD A GameData object, must be the older one of the two
   * @param newerGD The newer GameData object.
   * @return the merged GameData
   */
  private static GameData mergeIt(GameData olderGD, GameData newerGD,
      ReportTransformer transformer1, ReportTransformer transformer2) {
    // 2002.02.20 pavkovic: the newer rules are in GameData gd2. So we take
    // them for the new GameData
    GameData resultGD = new CompleteData(newerGD.getRules(), newerGD.getGameName());

    IslandMerger islandMerger = new IslandMerger(olderGD, newerGD, resultGD);

    boolean sameRound = olderGD.isSameRound(newerGD);
    /**
     * - to be added CR is newer and contains trust level that were set by the user explicitly (or
     * read from CR what means the same) &rarr; take the trust levels out of the new CR<br />
     * - to be added CR is older or of same age -> keep existing trust levels<br />
     * otherwise -> keep existing trust levels<br />
     */
    boolean takeTrustFromNew = !sameRound && TrustLevels.containsTrustLevelsSetByUser(newerGD);

    /***********************************************************************/
    /**************************** ADDING PHASE *****************************/
    /***********************************************************************/

    /**************************** DATE ***************************/
    resultGD.setDate(newerGD.getDate().clone());

    /**************************** ENCODING ***************************/
    // verify the encodings of the two reports
    String oldEncoding = olderGD.getEncoding();
    String newEncoding = newerGD.getEncoding();

    if (oldEncoding != null && newEncoding != null) {
      if (oldEncoding.equalsIgnoreCase(newEncoding)) {
        // do nothing
        log.debug("Do nothing");
        resultGD.setEncoding(oldEncoding);
      } else if (oldEncoding.equalsIgnoreCase(Encoding.UTF8.toString())
          || newEncoding.equalsIgnoreCase(Encoding.UTF8.toString())) {
        // if one of the reports has UTF-8 Encoding, we use it always.
        log.info("Set UTF-8 because one report match");
        resultGD.setEncoding(Encoding.UTF8.toString());
      } else {
        // okay, we have differnt encodings, but none of them is UTF-8 - what
        // now?
        log.info("Encoding does not match (" + oldEncoding + " vs. " + newEncoding
            + "), using new encoding");
        resultGD.setEncoding(newEncoding);
      }
    } else {
      // okay, this should never happen (no encoding in the reports)
      // so, we set the default encoding
      log.info("Set UTF-8 as default");
      resultGD.setEncoding(Encoding.UTF8.toString());
    }

    log.info("Old Encoding: " + oldEncoding + ",New Encoding: " + newEncoding
        + ",Result-Encoding: " + resultGD.getEncoding());

    /**************************** MAIL TO, MAIL SUBJECT ***************************/
    if (newerGD.mailTo != null) {
      resultGD.mailTo = newerGD.mailTo;
    } else {
      resultGD.mailTo = olderGD.mailTo;
    }

    if (newerGD.mailSubject != null) {
      resultGD.mailSubject = newerGD.mailSubject;
    } else {
      resultGD.mailSubject = olderGD.mailSubject;
    }

    /**************************** BASE ***************************/
    if (newerGD.base != 0) {
      resultGD.base = newerGD.base;
    } else {
      resultGD.base = olderGD.base;
    }

    /**************************** SERVER BUILD ***************************/
    if (newerGD.build != null) {
      resultGD.build = newerGD.build;
    } else {
      resultGD.build = olderGD.build;
    }

    /**************************** UNIT LIMIT ***************************/
    if (newerGD.maxUnits >= 0) {
      resultGD.maxUnits = newerGD.maxUnits;
    } else {
      resultGD.maxUnits = olderGD.maxUnits;
    }

    /**
     * Tracking an Bug: warn if we do not have 36 with eressea or vinyambar and set it to 36
     */
    String actGameName = newerGD.getGameName().toLowerCase();
    if ((actGameName.indexOf("eressea") > -1 || actGameName.indexOf("vinyambar") > -1)
        && (newerGD.base != 36)) {
      // this should not happen
      log.warn("BASE ERROR !! merged report could have not base36 !! Changed to base36.");
      newerGD.base = 36;
    }

    // NOSKILLPOINTS: the newer report determines the skill point handling
    resultGD.noSkillPoints = newerGD.noSkillPoints;

    // overwrite TempID only if newerGD is newer and has a tempID defined
    if (!sameRound && newerGD.getCurTempID() != -1) {
      resultGD.setCurTempID(newerGD.getCurTempID());
    } else {
      resultGD.setCurTempID(olderGD.getCurTempID());
    }

    /**************************** Attributes ***************************/
    // add both old and new attributes here
    if (olderGD.getAttributeSize() > 0) {
      for (String key : olderGD.getAttributeKeys()) {
        if (!resultGD.containsAttribute(key)) {
          resultGD.addAttribute(key, olderGD.getAttribute(key));
        }
      }
    }

    if (newerGD.getAttributeSize() > 0) {
      for (String key : newerGD.getAttributeKeys()) {
        if (!resultGD.containsAttribute(key)) {
          resultGD.addAttribute(key, newerGD.getAttribute(key));
        }
      }
    }

    // version - we take the CR version from the newer report
    resultGD.version = newerGD.version;

    /**************************** LOCALE ***************************/
    if (sameRound) {
      // if the added report is from the same round it is the newer
      // one, but we would stay then with the current locale
      if (olderGD.getLocale() != null) {
        resultGD.setLocale(olderGD.getLocale());
      } else {
        resultGD.setLocale(newerGD.getLocale());
      }
    } else {
      // if we do not have the same round then we use the newer locale
      // as we must have a chance to change the locale by adding a report
      // but we don't want that if we add an older report.
      if (newerGD.getLocale() != null) {
        resultGD.setLocale(newerGD.getLocale());
      } else {
        resultGD.setLocale(olderGD.getLocale());
      }
    }

    /**************************** TRANSLATIONS ***************************/
    // simple objects, created and merged in one step
    // for safety we should merge the translation directly
    // after setting the local of the result report
    if (resultGD.translations() != null) {
      if (olderGD.getLocale().equals(resultGD.getLocale())) {
        resultGD.translations().addAll(olderGD.translations(), resultGD.getRules());
      } else {
        resultGD.translations().clear();
      }
      if (newerGD.getLocale().equals(resultGD.getLocale())) {
        resultGD.translations().addAll(newerGD.translations(), resultGD.getRules());
      }
    }

    /**************************** MESSAGETYPES ***************************/
    // simple objects, created and merged in one step
    // locale has to be considered.
    if (olderGD.msgTypeView() != null) {
      for (MessageType mt : olderGD.msgTypeView().values()) {
        MessageType newMT = null;

        newMT = new MessageType(mt.getID());

        MessageType.merge(olderGD, mt, resultGD, newMT);
        resultGD.addMsgType(newMT);
      }
    }

    if (newerGD.msgTypeView() != null) {
      for (MessageType mt : newerGD.msgTypeView().values()) {
        MessageType newMT = resultGD.getMsgType(mt.getID());

        if (newMT == null) {
          newMT = new MessageType(mt.getID());
        }

        MessageType.merge(newerGD, mt, resultGD, newMT);
        resultGD.addMsgType(newMT);
      }
    }

    /**************************** SPELLS ***************************/
    // simple objects, created and merged in one step
    if (olderGD.spellView() != null) {
      for (Spell spell : olderGD.spellView().values()) {
        Spell newSpell = null;

        newSpell = MagellanFactory.createSpell(spell.getID(), resultGD);

        GameDataMerger.mergeSpell(olderGD, spell, resultGD, newSpell);
        resultGD.addSpell(newSpell);
      }
    }

    if (newerGD.spellView() != null) {
      for (Spell spell : newerGD.spellView().values()) {
        Spell newSpell = resultGD.getSpell(spell.getID());

        if (newSpell == null) {
          newSpell = MagellanFactory.createSpell(spell.getID(), resultGD);
        }

        GameDataMerger.mergeSpell(newerGD, spell, resultGD, newSpell);
        resultGD.addSpell(newSpell);
      }
    }

    /**************************** POTIONS ***************************/
    // simple objects, created and merged in one step
    if (olderGD.potionView() != null) {
      for (Potion potion : olderGD.potionView().values()) {
        Potion newPotion = null;

        newPotion = MagellanFactory.createPotion(potion.getID());

        GameDataMerger.mergePotion(olderGD, potion, resultGD, newPotion);
        resultGD.addPotion(newPotion);
      }
    }

    if (newerGD.potionView() != null) {
      for (Potion potion : newerGD.potionView().values()) {
        Potion newPotion = resultGD.getPotion(potion.getID());

        if (newPotion == null) {
          newPotion = MagellanFactory.createPotion(potion.getID());
        }

        GameDataMerger.mergePotion(newerGD, potion, resultGD, newPotion);
        resultGD.addPotion(newPotion);
      }
    }

    /**************************** OWNER FACTION ***************************/
    if (olderGD.getOwnerFaction() != null) {
      resultGD.setOwnerFaction(olderGD.getOwnerFaction());
    }
    // never change owner faction
    // else
    // resultGD.setOwnerFaction(newerGD.getOwnerFaction());

    // FIXME(stm) test
    /**************************** COORDINATE TRANSLATIONS ***************************/
    for (EntityID factionID : olderGD.getCoordinateTranslations().keySet()) {
      for (Integer layer : olderGD.getCoordinateTranslations().get(factionID).keySet()) {
        CoordinateID oldTranslation = olderGD.getCoordinateTranslation(factionID, layer);
        resultGD.setCoordinateTranslation(factionID, transformTranslation(transformer1,
            oldTranslation));
      }
    }
    for (EntityID factionID : newerGD.getCoordinateTranslations().keySet()) {
      for (Integer layer : newerGD.getCoordinateTranslations().get(factionID).keySet()) {
        CoordinateID newerTranslation = newerGD.getCoordinateTranslation(factionID, layer);
        CoordinateID resultTranslation = transformTranslation(transformer2, newerTranslation);
        if (olderGD.getCoordinateTranslation(factionID, layer) != null) {
          CoordinateID oldTranslation =
              transformTranslation(transformer1, olderGD.getCoordinateTranslation(factionID,
                  layer));
          if (!oldTranslation.equals(resultTranslation)) {
            log.warn("coordinate translations do not match " + factionID + "," + layer + ":"
                + oldTranslation + "!=" + resultTranslation);
            // resultGD.setCoordinateTranslation(factionID, oldTranslation);
          }
        } else {
          resultGD.setCoordinateTranslation(factionID, resultTranslation);
        }
      }
    }

    /**************************** ALLIANCES ***************************/
    if (olderGD.getAllianceGroups() != null && sameRound) {
      for (AllianceGroup alliance : olderGD.getAllianceGroups()) {
        resultGD.addAllianceGroup(MagellanFactory.createAlliance(alliance.getID(), resultGD));
      }
    }

    if (newerGD.getAllianceGroups() != null) {
      for (AllianceGroup alliance : newerGD.getAllianceGroups()) {
        if (resultGD.getAllianceGroup(alliance.getID()) == null) {
          resultGD.addAllianceGroup(MagellanFactory.createAlliance(alliance.getID(), resultGD));
        }
      }
    }

    /**************************** FACTIONS ***************************/
    // complex object, just add faction without merging here
    if (olderGD.factionView() != null) {
      for (Faction f : olderGD.factionView().values()) {
        resultGD.addFaction(MagellanFactory.createFaction(f.getID(), resultGD));
      }
    }

    if (newerGD.factionView() != null) {
      for (Faction f : newerGD.factionView().values()) {
        if (resultGD.getFaction(f.getID()) == null) {
          resultGD.addFaction(MagellanFactory.createFaction(f.getID(), resultGD));
        }
      }
    }

    /**************************** REGIONS ***************************/
    // complex object, just add region without merging here
    // this just adds all the regions to newGD. No content yet.
    if (olderGD.regionView() != null) {
      for (Region r : olderGD.regionView().values()) {
        // void regions are not in regionView any more
        // if (!r.getRegionType().equals(RegionType.theVoid)) {
        CoordinateID resultID = transform(transformer1, r.getID());
        Region resultRegion = MagellanFactory.createRegion(resultID, resultGD);
        resultRegion.setType(r.getType());
        resultGD.addRegion(resultRegion);
        // set uid and name here, needed to create the wrapper region
        if (r.hasUID() && r.getUID() >= 0) {
          // uID exists and is not "invented" (see GameData.makeWrapper)
          // TODO invent IDs if the region doesn't have one
          resultRegion.setUID(r.getUID());
        }
        resultRegion.setName(r.getName());

        // add wrappers for regions "on the edge"
        for (Region wrapper : transformer1.getWrappers(resultRegion, resultGD)) {
          resultGD.addRegion(wrapper);
        }
      }
    }

    if (newerGD.regionView() != null) {
      for (Region r : newerGD.regionView().values()) {
        CoordinateID resultID = transform(transformer2, r.getID());
        Region resultRegion = resultGD.getRegion(resultID);
        if (resultRegion == null) {
          resultRegion = MagellanFactory.createRegion(resultID, resultGD);
          resultRegion.setType(r.getType());
          resultGD.addRegion(resultRegion);
        }
        if (r.hasUID() && r.getUID() >= 0) {
          // TODO invent IDs if the region doesn't have one
          resultRegion.setUID(r.getUID());
        }
        resultRegion.setName(r.getName());
        for (Region wrapper : transformer2.getWrappers(resultRegion, resultGD)) {
          if (resultGD.getRegion(wrapper.getCoordinate()) == null) {
            resultGD.addRegion(wrapper);
          }
        }
      }
    }

    // /**************************** HOTSPOTS ***************************/
    // // complex object, just add without merging here
    // if (olderGD.hotSpotView() != null) {
    // for (HotSpot h : olderGD.hotSpotView().values()) {
    // resultGD.setHotSpot(MagellanFactory.createHotSpot(h.getID()));
    // }
    // }
    //
    // if (newerGD.hotSpotView() != null) {
    // for (HotSpot h : newerGD.hotSpotView().values()) {
    // if (resultGD.getHotSpot(h.getID()) == null) {
    // resultGD.setHotSpot(MagellanFactory.createHotSpot(h.getID()));
    // }
    // }
    // }

    /**************************** BUILDINGS ***************************/
    // complex object, just add without merging here
    if (newerGD.buildingView() != null) {
      for (Building b : newerGD.buildingView().values()) {
        resultGD.addBuilding(MagellanFactory.createBuilding(b.getID(), resultGD));
      }
    }

    for (Region newRegion : newerGD.regionView().values()) {
      if (newRegion.getVisibility().equals(Visibility.UNIT)) {
        CoordinateID resultRegion = transform(transformer2, newRegion.getID());
        resultGD.getRegion(resultRegion).setVisibility(Visibility.UNIT);
      }
    }

    if (olderGD.buildingView() != null) {
      // buildings are persistent.
      // Accept old buildings not occurring in the new report
      // only if there are no units in that region
      for (Building oldBuilding : olderGD.buildingView().values()) {
        Building curBuilding = newerGD.getBuilding(oldBuilding.getID());

        if (curBuilding == null) {
          // check if the building disappeared because we do
          // not know the region anymore or if it was
          // destroyed
          Region curRegion = null;
          if (oldBuilding.getRegion() == null) {
            log.errorOnce("Building without Region!" + oldBuilding.toString());
          } else {
            if (oldBuilding.getRegion().getID() == null) {
              log.errorOnce("Region without ID!");
            } else {
              curRegion =
                  resultGD.getRegion(transform(transformer1, oldBuilding.getRegion().getID()));

              if ((curRegion == null) || !curRegion.getVisibility().greaterEqual(
                  Visibility.TRAVEL)) {
                resultGD.addBuilding(MagellanFactory.createBuilding(oldBuilding.getID(), resultGD));
              } else {
                // skip this building
              }
            }
          }
        } else {
          // the building occurs in gd2 so we already
          // included its current version in newGD
        }
      }
    }

    /**************************** SHIPS ***************************/
    // complex object, just add without merging here
    if (sameRound && (olderGD.shipView() != null)) {
      for (Ship s : olderGD.shipView().values()) {
        resultGD.addShip(MagellanFactory.createShip(s.getID(), resultGD));
      }
    }

    if (newerGD.shipView() != null) {
      for (Ship s : newerGD.shipView().values()) {
        if (resultGD.getShip(s.getID()) == null) {
          resultGD.addShip(MagellanFactory.createShip(s.getID(), resultGD));
        }
      }
    }

    /**************************** UNITS ***************************/
    // complex object, just add without merging here

    /*
     * Note: To gather the information needed for level changes, report one is always treated. But
     * in the case of unequal dates only units that are also in the second report are added to the
     * new one and temp units are ignored. IDs are used for comparison.
     */

    if (sameRound) {
      // adding units faction by faction and for owner factions first (units from the owner faction
      // should be ensured to have the correct order in the server report); this still doesn't
      // enforce correct unit sorting, but it enforces it within factions, which is the most
      // important for order evaluation...
      int sortIndex = 0;
      EntityID added1 = null, added2 = null;
      if (getOwnerFaction(olderGD) != null) {
        sortIndex = addUnits(getOwnerFaction(olderGD).units(), resultGD, sortIndex);
        added1 = olderGD.getOwnerFaction();
      }
      if (newerGD.getOwnerFaction() != null
          && !newerGD.getOwnerFaction().equals(olderGD.getOwnerFaction())
          && getOwnerFaction(newerGD) != null) {
        sortIndex = addUnits(getOwnerFaction(newerGD).units(), resultGD, sortIndex);
        added2 = newerGD.getOwnerFaction();
      }
      for (Faction f : olderGD.getFactions()) {
        if (!f.getID().equals(added1)) {
          sortIndex = addUnits(f.units(), resultGD, sortIndex);
        }
      }
      for (Faction f : newerGD.getFactions()) {
        if (!f.getID().equals(added2)) {
          sortIndex = addUnits(f.units(), resultGD, sortIndex);
        }
      }

      int sortIndex2 = Integer.MAX_VALUE / 2 + resultGD.getOldUnits().size();
      sortIndex2 = addOldUnits(olderGD.getOldUnits(), resultGD.units(), resultGD, sortIndex2);
      addOldUnits(newerGD.getOldUnits(), resultGD.units(), resultGD, sortIndex2);
    } else {
      if (newerGD.unitView() != null) {
        int sortIndex = 0;
        if (getOwnerFaction(newerGD) != null) {
          sortIndex = addUnits(getOwnerFaction(newerGD).units(), resultGD, sortIndex);
        }
        for (Faction f : newerGD.getFactions()) {
          if (!f.getID().equals(newerGD.getOwnerFaction())) {
            sortIndex = addUnits(f.units(), resultGD, sortIndex);
          }
        }
      }
      int sortIndex2 = Integer.MAX_VALUE / 2 + resultGD.getOldUnits().size();
      sortIndex2 = addOldUnits(olderGD.getUnits(), resultGD.units(), resultGD, sortIndex2);
      addOldUnits(newerGD.getOldUnits(), resultGD.units(), resultGD, sortIndex2);
    }

    /**************************** Bookmarks ***************************/
    for (Bookmark bm : olderGD.getBookmarks()) {
      resultGD.addBookmark(MagellanFactory.createBookmark(resultGD, bm.getType().toString(), bm
          .getObject().getID().toString(), bm.getName()));
    }
    for (Bookmark bm : newerGD.getBookmarks()) {
      resultGD.addBookmark(MagellanFactory.createBookmark(resultGD, bm.getType().toString(), bm
          .getObject().getID().toString(), bm.getName()));
    }

    /***********************************************************************/
    /********************** MERGING PHASE -- FIRST PASS ********************/
    /***********************************************************************/

    /**************************** ALLIANCES ***************************/
    if (olderGD.getAllianceGroups() != null && sameRound) {
      for (AllianceGroup curAlliance : olderGD.getAllianceGroups()) {
        AllianceGroup newAlliance = resultGD.getAllianceGroup(curAlliance.getID());

        GameDataMerger.mergeAlliance(olderGD, curAlliance, resultGD, newAlliance);
      }
    }

    /**************************** MERGE FACTIONS ***************************/
    // complex object FIRST PASS
    if (olderGD.factionView() != null) {
      for (Faction oldFaction : olderGD.factionView().values()) {
        Faction newFaction = resultGD.getFaction(oldFaction.getID());

        // first pass
        GameDataMerger.mergeFaction(olderGD, oldFaction, resultGD, newFaction, !takeTrustFromNew,
            transformer1);
      }
    }

    islandMerger.firstPass();

    /**************************** MERGE REGIONS ***************************/
    // complex object FIRST PASS
    if (olderGD.regionView() != null) {
      for (Region oldRegion : olderGD.regionView().values()) {
        Region resultRegion = resultGD.getRegion(transform(transformer1, oldRegion.getID()));

        // first pass
        GameDataMerger.mergeRegion(olderGD, oldRegion, resultGD, resultRegion, !sameRound, true,
            transformer1);
      }
    }

    // /**************************** MERGE HOTSPOTS ***************************/
    // // complex object FIRST PASS
    // if (olderGD.hotSpotView() != null) {
    // for (HotSpot curHotSpot : olderGD.hotSpotView().values()) {
    // HotSpot newHotSpot = resultGD.getHotSpot(curHotSpot.getID());
    // // first pass
    // GameDataMerger.mergeHotSpot(olderGD, curHotSpot, resultGD, newHotSpot, transformer1);
    // }
    // }

    /**************************** MERGE BUILDINGS ***************************/
    // complex object FIRST PASS
    if (olderGD.buildingView() != null) {
      for (Building curBuilding : olderGD.buildingView().values()) {
        Building newBuilding = resultGD.getBuilding(curBuilding.getID());

        if (newBuilding != null) {
          // first pass
          GameDataMerger.mergeBuilding(olderGD, curBuilding, resultGD, newBuilding, transformer1);
        }
      }
    }

    /**************************** MERGE SHIPS ***************************/
    // complex object FIRST PASS
    if ((olderGD.shipView() != null)) {
      for (Ship oldShip : olderGD.shipView().values()) {
        Ship resultShip = resultGD.getShip(oldShip.getID());

        // only merge ships from the "older" game data if they are from the same
        // round
        if (sameRound) {
          // first pass
          GameDataMerger.mergeShip(olderGD, oldShip, resultGD, resultShip, transformer1);
        } else {
          // TODO (stm 2007-02-19) this is a workaround, we need a nicer
          // solution.
          // Unlike other unit containers, ships are deleted if they are not
          // seen any more in a new round. But we want to keep their comments anyway.
          GameDataMerger.mergeComments(oldShip, resultShip);
        }
      }
    }

    /***********************************************************************/
    /********************** MERGING PHASE -- SECOND PASS *******************/
    /***********************************************************************/

    /**************************** ALLIANCES ***************************/
    if (newerGD.getAllianceGroups() != null) {
      for (AllianceGroup curAlliance : newerGD.getAllianceGroups()) {
        AllianceGroup newAlliance = resultGD.getAllianceGroup(curAlliance.getID());

        GameDataMerger.mergeAlliance(newerGD, curAlliance, resultGD, newAlliance);
      }
    }

    /**************************** MERGE FACTIONS, SECOND PASS ***************************/
    // must be done before merging units to keep group information
    if (newerGD.factionView() != null) {
      for (Faction curFaction : newerGD.factionView().values()) {
        Faction newFaction = resultGD.getFaction(curFaction.getID());

        // second pass
        GameDataMerger.mergeFaction(newerGD, curFaction, resultGD, newFaction, takeTrustFromNew,
            transformer2);
      }
    }

    /**************************** MERGE OLD UNITS ***************************/
    // merge before real units to get proper base
    for (Unit resultUnit : resultGD.oldUnitsView().values()) {
      Unit olderUnit;
      if (sameRound) {
        olderUnit = olderGD.getOldUnit(resultUnit.getID());
      } else {
        olderUnit = olderGD.getUnit(resultUnit.getID());
      }
      if (olderUnit != null) {
        mergeUnit(olderGD, olderUnit, resultGD, resultUnit, sameRound, true, transformer1);
        // mergeUnits calls resultUnit.setRegion, which calls region.addUnit
        resultUnit.detach();
        // MagellanFactory.copySkills(olderUnit, resultUnit);
      }

      Unit newerUnit = newerGD.getOldUnit(resultUnit.getID());
      if (newerUnit != null) {
        mergeUnit(newerGD, newerUnit, resultGD, resultUnit, sameRound, false, transformer2);
        resultUnit.detach();
        // MagellanFactory.copySkills(newerUnit, resultUnit);
      }
      if (newerUnit == null && olderUnit == null) {
        log.warn("Unknown old unit " + resultUnit);
      }
    }

    /**************************** MERGE UNITS ***************************/
    /*
     * Note: To gather level change informations all units are used. If the dates are equal, a fully
     * merge is done, if not, only the skills are retrieved.
     */
    for (Unit resultUnit : resultGD.unitView().values()) {
      // find the second first since we may need the temp id
      Unit newerUnit = newerGD.getUnit(resultUnit.getID());

      // find a temp ID to gather information out of the temp unit
      UnitID tempID = null;

      if ((newerUnit != null) && !sameRound) {
        // only use temp ID if reports have different date
        tempID = newerUnit.getTempID();

        if (tempID != null) {
          tempID = UnitID.createUnitID(-tempID.intValue(), newerGD.base);
        }
      }

      {
        // now get the unit of the first report
        Unit olderUnit;
        boolean isOld = false;
        if (tempID != null) {
          olderUnit = olderGD.getTempUnit(tempID);
        } else {
          olderUnit = olderGD.getUnit(resultUnit.getID());
          if (olderUnit == null) {
            isOld = true;
            olderUnit = newerGD.getOldUnit(resultUnit.getID());
            if (sameRound && olderUnit == null) {
              olderUnit = olderGD.getOldUnit(resultUnit.getID());
            }
          }
        }
        // first merge step
        if (olderUnit != null) {
          if (sameRound && !isOld) { // full merge
            GameDataMerger.mergeUnit(olderGD, olderUnit, resultGD, resultUnit, sameRound, true,
                transformer1);
          } else { // only copy the skills to get change-level base
            if ((newerUnit != null)
                && ((newerUnit.getSkills() != null) || (olderUnit.getFaction().isPrivileged()))) {
              MagellanFactory.copySkills(olderUnit, resultUnit);
            }
          }
        }
      }

      // second merge step
      if (newerUnit != null) {
        GameDataMerger.mergeUnit(newerGD, newerUnit, resultGD, resultUnit, sameRound, false,
            transformer2);
      }
    }

    islandMerger.secondPass();

    /**************************** MERGE REGIONS, SECOND PASS ***************************/
    if (newerGD.regionView() != null) {
      for (Region newerRegion : newerGD.regionView().values()) {
        Region resultRegion = resultGD.getRegion(transform(transformer2, newerRegion.getID()));
        GameDataMerger.mergeRegion(newerGD, newerRegion, resultGD, resultRegion, !sameRound, false,
            transformer2);
      }
    }

    // /**************************** MERGE HOTSPOTS, SECOND PASS ***************************/
    // if (newerGD.hotSpotView() != null) {
    // for (HotSpot curHotSpot : newerGD.hotSpotView().values()) {
    // HotSpot newHotSpot = resultGD.getHotSpot(curHotSpot.getID());
    // // second pass
    // GameDataMerger.mergeHotSpot(newerGD, curHotSpot, resultGD, newHotSpot, transformer2);
    // }
    // }

    /**************************** MERGE BUILDINGS, SECOND PASS ***************************/
    if (newerGD.buildingView() != null) {
      for (Building curBuilding : newerGD.buildingView().values()) {
        Building newBuilding = resultGD.getBuilding(curBuilding.getID());

        if (newBuilding != null) {
          // second pass
          GameDataMerger.mergeBuilding(newerGD, curBuilding, resultGD, newBuilding, transformer2);
        }
      }
    }

    /**************************** MERGE SHIPS, SECOND PASS ***************************/
    if (newerGD.shipView() != null) {
      for (Ship curShip : newerGD.shipView().values()) {
        Ship newShip = resultGD.getShip(curShip.getID());

        // second pass
        GameDataMerger.mergeShip(newerGD, curShip, resultGD, newShip, transformer2);
      }
    }

    resultGD.postProcess();

    return resultGD;
  }

  private static Faction getOwnerFaction(GameData olderGD) {
    if (olderGD.getOwnerFaction() != null)
      return olderGD.getFaction(olderGD.getOwnerFaction());
    return null;
  }

  private static int addUnits(Collection<Unit> units, GameData resultGD, int sortIndex) {
    for (Unit u : units) {
      // Attention: UnitContainer.units() returns temp units (GameData.units() doesn't)
      if (u instanceof TempUnit) {
        continue;
      }
      if (resultGD.getUnit(u.getID()) == null) {
        Unit newUnit = MagellanFactory.createUnit(u.getID(), resultGD);
        newUnit.setSortIndex(sortIndex++);
        resultGD.addUnit(newUnit);
      }
    }
    return sortIndex;
  }

  private static int addOldUnits(Collection<Unit> sourceUnits, Map<UnitID, Unit> subtractUnits,
      GameData targetGD, int sortIndex) {
    for (Unit u : sourceUnits) {
      // Attention: UnitContainer.units() returns temp units (GameData.units() doesn't)
      if (u instanceof TempUnit) {
        continue;
      }
      if (subtractUnits.get(u.getID()) == null) {
        Unit newUnit = MagellanFactory.createUnit(u.getID(), targetGD);
        newUnit.setSortIndex(sortIndex++);
        targetGD.addOldUnit(newUnit);
      }
    }
    return sortIndex;
  }

  /**
   * Add units from older and newer to resultGD; this doesn't preserve the correct unit order.
   */
  @SuppressWarnings("unused")
  private void joinUnits0(GameData olderGD, GameData newerGD, final GameData resultGD,
      boolean sameRound) {
    // try to merge units in the correct order; this doesn't work any more if we merge more
    // than two reports.
    // joinUnits(olderGD, newerGD, resultGD);

    // but this doesn't work at all!
    if (olderGD.unitView() != null) {
      for (Unit u : olderGD.unitView().values()) {
        if (sameRound || (newerGD.getUnit(u.getID()) != null)) {
          resultGD.addUnit(MagellanFactory.createUnit(u.getID(), resultGD));
        }
      }
    }
    for (Unit u : newerGD.unitView().values()) {
      if (resultGD.getUnit(u.getID()) == null) {
        resultGD.addUnit(MagellanFactory.createUnit(u.getID(), resultGD));
      }
    }

  }

  /**
   * Add units from older and newer to resultGD, trying to maintain unit order. unfortunately, this
   * also doesn't always preserve the correct unit order (especially, if more than two reports are
   * added).
   */
  @SuppressWarnings("unused")
  private static void joinUnits1(GameData olderGD, GameData newerGD, final GameData resultGD) {
    for (Region r : resultGD.regionView().values()) {
      GameDataMerger.joinSortedMaps(olderGD.getRegion(r.getCoordinate()).getUnits(), newerGD
          .getRegion(r.getCoordinate()).getUnits(), new Assigner<Unit>() {

            private int index = 0;

            public void add(Unit element) {
              Unit unit = MagellanFactory.createUnit(element.getID(), resultGD);
              resultGD.addUnit(unit);
              unit.setSortIndex(index++);
            }
          });
    }
  }

  /**
   * Little driver interface to make this applicable to more than just units, but also regions
   * and... anything else.
   */
  public static interface Assigner<T extends Identifiable> {
    /** Adds the argument to the result object. */
    public void add(T element);
  }

  private static <T extends Identifiable> void joinSortedMaps(Map<? extends ID, T> map1,
      Map<? extends ID, T> map2, Assigner<T> assigner) {

    Iterator<T> iterator1 = map1.values().iterator();
    Iterator<T> iterator2 = map2.values().iterator();
    while (iterator1.hasNext() || iterator2.hasNext()) {
      T element1 = null, element2 = null;
      while (iterator1.hasNext()) {
        element1 = iterator1.next();
        if (!map2.containsKey(element1.getID())) {
          assigner.add(element1);
        } else {
          break;
        }
      }
      while (iterator2.hasNext()) {
        element2 = iterator2.next();
        if (!map1.containsKey(element2.getID())) {
          assigner.add(element2);
        } else {
          break;
        }
      }
      if (element1 != null) {
        assigner.add(element1);
      }
      if (element2 != null && (element1 == null || !element2.getID().equals(element1.getID()))) {
        assigner.add(element2);
      }
    }
  }

  /**
   * Transform a coordinate translation: mirror at origin, translate, mirror again.
   */
  private static CoordinateID transformTranslation(ReportTransformer transformer,
      CoordinateID oldTranslation) {
    CoordinateID zero = CoordinateID.create(0, 0, oldTranslation.getZ());
    return zero.inverseTranslateInLayer(transform(transformer, zero
        .inverseTranslateInLayer(oldTranslation)));
  }

  private static CoordinateID transform(ReportTransformer transformer, CoordinateID id) {
    if (transformer == null)
      return id;
    else
      return transformer.transform(id);
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
        newGroup.setAllies(new LinkedHashMap<EntityID, Alliance>());
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
   * Copies the values of curFaction to newFaction.
   *
   * @param adjustTrustLevels Only if this is <code>true</code> will the trust levels of newFaction
   *          be copied.
   * @param transformer
   */
  public static void mergeFaction(GameData curGD, Faction curFaction, GameData newGD,
      Faction newFaction, boolean adjustTrustLevels, ReportTransformer transformer) {
    GameDataMerger.mergeUnitContainer(curGD, curFaction, newGD, newFaction);

    // tricky: keep alliance only if from the same round and either curFaction is owner faction or
    // alliance is known
    if (curGD.isSameRound(newGD)
        && (curFaction.getAlliance() != null || curFaction.getID().equals(curGD
            .getOwnerFaction()))) {
      newFaction.setAlliance(curFaction.getAlliance());
    }

    // keep allies information from last round if no new info is known
    if ((curFaction.getAllies() != null && curFaction.getAllies().size() > 0)
        || curFaction.getID().equals(curGD.getOwnerFaction())) {
      if (newFaction.getAllies() == null) {
        newFaction.setAllies(CollectionFactory.<EntityID, Alliance> createSyncOrderedMap());
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
        newFaction.setGroups(new LinkedHashMap<IntegerID, Group>());
      } else {
        newFaction.getGroups().clear();
      }

      for (Group curGroup : curFaction.getGroups().values()) {
        Group newGroup = null;

        newGroup = MagellanFactory.createGroup(curGroup.getID(), newGD);

        GameDataMerger.mergeGroup(curGD, curGroup, newGD, newGroup);
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

    if (adjustTrustLevels
        && ((curFaction.getTrustLevel() != Faction.TL_DEFAULT) || curFaction
            .isTrustLevelSetByUser())) {
      newFaction.setTrustLevel(curFaction.getTrustLevel());
      newFaction.setTrustLevelSetByUser(curFaction.isTrustLevelSetByUser());
    }

    // see Region.merge() for the meaning of the following if
    if (curGD.isSameRound(newGD)) {
      if (curFaction.getAverageScore() != -1) {
        newFaction.setAverageScore(curFaction.getAverageScore());
      }

      if ((curFaction.getBattles() != null) && (curFaction.getBattles().size() > 0)) {
        newFaction.setBattles(new LinkedList<Battle>());

        for (Battle curBattle : curFaction.getBattles()) {
          CoordinateID newID = transform(transformer, curBattle.getID());
          final Battle newBattle = MagellanFactory.createBattle(newID, curBattle.isBattleSpec());

          for (Message curMsg : curBattle.messages()) {
            final Message newMsg = MagellanFactory.createMessage(curMsg.getID());
            GameDataMerger.mergeMessage(curGD, curMsg, newGD, newMsg, transformer);
            newBattle.messages().add(newMsg);
          }

          newFaction.getBattles().add(newBattle);
        }
      }

      if ((curFaction.getErrors() != null) && (curFaction.getErrors().size() > 0)) {
        newFaction.setErrors(new LinkedList<String>(curFaction.getErrors()));
      }

      if ((curFaction.getMessages() != null) && (curFaction.getMessages().size() > 0)) {
        if (newFaction.getMessages() == null) {
          newFaction.setMessages(new ArrayList<Message>());
        } else {
          newFaction.getMessages().clear();
        }

        for (Message curMsg : curFaction.getMessages()) {
          Message newMsg = null;

          newMsg = MagellanFactory.createMessage(curMsg.getID());

          GameDataMerger.mergeMessage(curGD, curMsg, newGD, newMsg, transformer);
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
    if (curGD.isSameRound(newGD)) {
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
        newUC.setType(newGD.getRules().getBuildingType(curUC.getType().getID(), true));
      } else if (curUC instanceof Region) {
        // pavkovic 2004.01.03: (bugzilla bug 801): overwrite with curUC.getType
        // if
        // known or newUC.getType is same as "unknown" (this is a miracle to me
        // but
        // Ulrich has more experiences with "Astralraum" :-))
        // if (newUC.getType() == null ||
        // newUC.getType().equals(RegionType.unknown)) {
        // TODO(stm) also test "theVoid"?
        if ((curUC.getType() != null && !curUC.getType().equals(RegionType.unknown))
            || newUC.getType() == null || newUC.getType().equals(RegionType.unknown)) {
          newUC.setType(newGD.getRules().getRegionType(curUC.getType().getID(), true));
        }
      } else if (curUC instanceof Ship) {
        newUC.setType(newGD.getRules().getShipType(curUC.getType().getID(), true));
      } else if (curUC instanceof Faction) {
        newUC.setType(newGD.getRules().getRace(curUC.getType().getID(), true));
      }
    }

    // copy tags
    if (curGD.isSameRound(newGD) && curUC.hasTags()) {
      for (String key : curUC.getTagMap().keySet()) {
        if (!newUC.containsTag(key)) {
          newUC.putTag(key, curUC.getTag(key));
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
   * different) if curMsg.locale == newGD.local then newMsg=curMsg =&gt; if correct locale available
   * use it =&gt; otherwise take wrong locale msg, to have at least a half localized msg if the msgtype
   * is available in locale =&gt; you can notice this half localized msg because msg.locale=gm.locale,
   * also msg.rerender=true
   *
   * @param curGD fully loaded game data
   * @param curMsg a fully initialized and valid message
   * @param newGD the game data to be updated
   * @param newMsg a message to be updated with the data from curMsg
   * @param transformer
   */
  public static void mergeMessage(GameData curGD, Message curMsg, GameData newGD, Message newMsg,
      ReportTransformer transformer) {
    if ((curMsg.getAttributes() != null) && (curMsg.getAttributes().size() > 0)) {
      if (newMsg.getAttributes() == null) {
        newMsg.setAttributes(CollectionFactory.<String, String> createSyncOrderedMap(4));
      } else {
        newMsg.getAttributes().clear();
      }

      if (curMsg.getAttributes() != null) {
        for (Entry<String, String> entry : curMsg.getAttributes().entrySet()) {
          if (entry.getKey().equalsIgnoreCase("type")) {
            newMsg.getAttributes().put(entry.getKey(), entry.getValue());
          } else if (entry.getKey().equalsIgnoreCase("rendered")) {
            newMsg.getAttributes().put(entry.getKey(),
                originTranslate(transformer, entry.getValue()));
          } else {
            CoordinateID coord = CoordinateID.parse(entry.getValue(), ",");

            if (coord != null) {
              final CoordinateID newCoord = transformer.transform(coord);
              newMsg.getAttributes().put(entry.getKey(), newCoord.toString(","));
            } else {
              coord = CoordinateID.parse(entry.getValue(), " ");
              if (coord != null) {
                final CoordinateID newCoord = transformer.transform(coord);
                newMsg.getAttributes().put(entry.getKey(), newCoord.toString(" ", true));
              } else {
                // check for ;regions
                if (entry.getKey().equalsIgnoreCase("regions")) {
                  // special dealing
                  newMsg.getAttributes().put(entry.getKey(),
                      originTranslateRegions(transformer, entry.getValue()));
                } else {
                  newMsg.getAttributes().put(entry.getKey(), entry.getValue());
                }
              }
            }
          }
        }
      }
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
        newMsg.setText(originTranslate(transformer, curMsg.getText()));
      } else {
        if (curMsg.getMessageType() == null) {
          // if the message has no message type (e.g. DURCHSCHIFFUNG,
          // DURCHREISE), the best thing we can do is to copy the text anyway...
          newMsg.setText(originTranslate(transformer, curMsg.getText()));
        }
        // otherwise we can render the text from the probably localized
        // messagetype
        /*
         * we dont render it here as the new GameData is not fully initialized. as the text is null,
         * it will be rendered on the first usage. newMsg.render(newGD);
         */
      }
    }

    newMsg.setAcknowledged(curMsg.isAcknowledged());
  }

  protected static final String number = "[\\+\\-]?\\d+";
  protected static String astral = Resources.get("rules.astralspacecoordinate");
  protected static String pattern;
  protected static String regionsPattern1;
  protected static String regionsPattern2;
  static {
    buildPattern();
  }

  protected static void buildPattern() {
    // check if locale has changed
    if (pattern == null || !astral.equals(Resources.get("rules.astralspacecoordinate"))) {
      astral = Resources.get("rules.astralspacecoordinate");
      // \((number)\,\ ?(number)(\,\ ?((number)|Astralraum))?\)
      // \(([\+\-]?\d+)\,\ ?([\+\-]?\d+)(\,\ ?(([\+\-]?\d+)|Astralraum))?\)
      pattern =
          "\\((" + number + ")\\,\\ ?(" + number + ")(\\,\\ ?((" + number + ")|" + astral
              + "))?\\)";
      // \(([\+\-]?\d+) ([\+\-]?\d+)( (([\+\-]?\d+)|Astralraum))?\)
      regionsPattern1 =
          "\\((" + number + ") (" + number + ")( ((" + number + ")|" + astral + "))?\\)";
      // \(([\+\-]?\d+) ([\+\-]?\d+)( (([\+\-]?\d+)|Astralraum))?\)*
      regionsPattern2 =
          "\\((" + number + ") (" + number + ")( ((" + number + ")|" + astral + "))?\\)*";
    }
  }

  /**
   * special sub to translate coords in ";regions" tags of messages expecting this form
   * <code>"x1 y1 z1, x2 y2 z2";regions</code>.<br />
   * There is also an older variant: <code>"der Sumpf von Rudros (-7,23)";regions</code>
   *
   * @param value
   * @return
   */
  private static String originTranslateRegions(ReportTransformer transformer, String value) {
    final StringBuffer result = new StringBuffer();
    buildPattern();
    if (value.matches(regionsPattern2)) {
      final Matcher matcher = Pattern.compile(regionsPattern1).matcher(value);
      while (matcher.find()) {
        final String candi = matcher.group();
        // candi=candi.replaceAll(astral,
        // world.getGameSpecificRules().getAstralSpacePlane());
        CoordinateID coord = CoordinateID.parse(candi, " ");
        if (coord != null) {
          coord = transformer.transform(coord);
          matcher.appendReplacement(result, "(" + coord.toString(" ") + ")");
        } else {
          matcher.appendReplacement(result, matcher.group());
        }
      }
      matcher.appendTail(result);
      return result.toString();
    } else
      return originTranslate(transformer, value);
  }

  /**
   * Tries to replace coordinates in string by the translated version. The string is searched for
   * occurrences of the form "(123,123)" or "(123,123,123)" or "(123,123,Astralraum)", transforms
   * them and replaces them. This is not completely fool-proof!
   *
   * @param value Usually a message text which might contain coordinates
   * @see magellan.library.utils.transformation.ReportTransformer#transform(java.lang.String)
   */
  private static String originTranslate(ReportTransformer transformer, String value) {
    final StringBuffer result = new StringBuffer();
    buildPattern();
    final Matcher matcher = Pattern.compile(pattern).matcher(value);
    while (matcher.find()) {
      final String candi = matcher.group();
      // candi=candi.replaceAll(astral,
      // world.getGameSpecificRules().getAstralSpacePlane());
      CoordinateID coord = CoordinateID.parse(candi.substring(1, candi.length() - 1), ",");
      if (coord != null) {
        coord = transformer.transform(coord);
        matcher.appendReplacement(result, "(" + coord.toString() + ")");
      } else {
        matcher.appendReplacement(result, matcher.group());
      }
    }
    matcher.appendTail(result);
    return result.toString();
  }

  /**
   * Merges potion.
   */
  public static void
      mergePotion(GameData curGD, Potion curPotion, GameData newGD, Potion newPotion) {
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
            newGD.getRules().getItemType(i.getItemType().getID(), true);
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
   * @param transformer
   */
  public static void mergeBuilding(GameData curGD, Building curBuilding, GameData newGD,
      Building newBuilding, ReportTransformer transformer) {
    GameDataMerger.mergeUnitContainer(curGD, curBuilding, newGD, newBuilding);

    if (curBuilding.getCost() != -1) {
      newBuilding.setCost(curBuilding.getCost());
    }

    if (curBuilding.getRegion() != null) {
      newBuilding.setRegion(newGD
          .getRegion(transform(transformer, curBuilding.getRegion().getID())));
    }

    if (curBuilding.getSize() != -1) {
      newBuilding.setSize(curBuilding.getSize());
    }

    // Fiete 20060910
    // added support for wahrerTyp
    if (curBuilding.getTrueBuildingType() != null) {
      newBuilding.setTrueBuildingType(curBuilding.getTrueBuildingType());
    }

    if (curGD.isSameRound(newGD)) {
      // besiegers are visible to all
      newBuilding.setBesiegers(curBuilding.getBesiegers());
      if (curBuilding.getBesiegerUnits() != null) {
        for (UnitID b : curBuilding.getBesiegerUnits()) {
          newBuilding.addBesiegerUnit(b);
        }
      }
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

  // /**
  // * Merges two HotSpot objects.
  // *
  // * @param transformer
  // */
  // public static void mergeHotSpot(GameData curGD, HotSpot curHS, GameData newGD, HotSpot newHS,
  // ReportTransformer transformer) {
  // if (curHS.getName() != null) {
  // newHS.setName(curHS.getName());
  // }
  //
  // if (curHS.getCenter() != null) {
  // newHS.setCenter(transform(transformer, curHS.getCenter()));
  // }
  // }

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
   * @param transformer
   */
  public static void mergeRegion(GameData curGD, Region curRegion, GameData resultGD,
      Region resultRegion, boolean newTurn, boolean firstPass, ReportTransformer transformer) {
    GameDataMerger.mergeUnitContainer(curGD, curRegion, resultGD, resultRegion);

    final boolean sameTurn = !newTurn || !firstPass;

    /******************** NEIGHBOURS *******************************************/
    // do not merge neighbors, add them automatically instead; if the report wraps, wrapper regions
    // have been inserted before
    // Map<Direction, Region> neighbors = curRegion.getNeighbors();
    // for (Direction d : neighbors.keySet()) {
    // Region neighbor = resultGD.getRegion(transform(transformer, neighbors.get(d).getID()));
    // if (neighbor == null) {
    // log.error("neighbor not found " + neighbors.get(d) + " of " + curRegion);
    // } else {
    // resultRegion.addNeighbor(d, neighbor);
    // }
    // }

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

    // *** Old luxury volume ****
    if (newTurn && firstPass && curRegion.maxLuxuries() != -1) {
      // current luxuries is a computed value...
      resultRegion.setOldLuxuries(curRegion.maxLuxuries());
    } else if (!newTurn && curRegion.maxOldLuxuries() != -1) {
      resultRegion.setOldLuxuries(curRegion.maxOldLuxuries());
    }

    /******************* OLD PRICES ******************************/
    if (newTurn && !firstPass && curRegion.getPrices() != null && resultRegion.getPrices() != null
        && !curRegion.getPrices().equals(resultRegion.getPrices())) {
      // this means that the new report is from a newer round than the old report,
      // the old report's prices have been merged into the current report's prices and
      // the new prices are different
      resultRegion.setOldPrices(new LinkedHashMap<StringID, LuxuryPrice>());

      for (LuxuryPrice curPrice : resultRegion.getPrices().values()) {
        final LuxuryPrice newPrice =
            new LuxuryPrice(resultGD.getRules().getItemType(curPrice.getItemType().getID()),
                curPrice.getPrice());
        if (newPrice.getItemType() == null) {
          // this happens if there does exist an unknown tag in
          // the current block description
          log.warn("WARNING: Invalid tag \"" + curPrice.getItemType() + "\" found in Region "
              + curRegion + ", ignoring it.");
          log.warn("curRegion Encoding: " + curRegion.getData().getEncoding() + ", newRegion Enc: "
              + resultRegion.getData().getEncoding());
        } else {
          resultRegion.getOldPrices().put(newPrice.getItemType().getID(), newPrice);
        }
      }
    } else if (!newTurn && curRegion.getOldPrices() != null) {
      resultRegion.setOldPrices(new LinkedHashMap<StringID, LuxuryPrice>());

      for (LuxuryPrice curPrice : curRegion.getOldPrices().values()) {
        final LuxuryPrice newPrice =
            new LuxuryPrice(resultGD.getRules().getItemType(curPrice.getItemType().getID()),
                curPrice.getPrice());

        if (newPrice.getItemType() == null) {
          // this happens if there does exist an unknown tag in
          // the current block description
          log.warn("WARNING: Invalid tag \"" + curPrice.getItemType() + "\" found in Region "
              + curRegion + ", ignoring it.");
          log.warn("curRegion Encoding: " + curRegion.getData().getEncoding() + ", newRegion Enc: "
              + resultRegion.getData().getEncoding());
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

        newBorder =
            MagellanFactory.createBorder(curBorder.getID(), curBorder.getDirection(), curBorder
                .getType(), curBorder.getBuildRatio());

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
    if (curRegion.hasUID()) {
      if (curRegion.getUID() >= 0 || resultRegion.getUID() < 0) {
        resultRegion.setUID(curRegion.getUID());
      }
    }

    /******************** HERBS *************************************/
    if (curRegion.getHerb() != null) {
      resultRegion.setHerb(resultGD.getRules().getItemType(curRegion.getHerb().getID(), true));
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
        log.warn("Region.merge(): island could not be found in the merged data: "
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

    if (curRegion.getRecruits() != -1) {
      resultRegion.setRecruits(curRegion.getRecruits());
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
        resultRegion.setPrices(CollectionFactory.<StringID, LuxuryPrice> createSyncOrderedMap(3));
      } else {
        resultRegion.getPrices().clear();
      }

      for (LuxuryPrice curPrice : curRegion.getPrices().values()) {
        final LuxuryPrice newPrice =
            new LuxuryPrice(resultGD.getRules().getItemType(curPrice.getItemType().getID()),
                curPrice.getPrice());

        if (newPrice.getItemType() == null) {
          // this happens if there does exist an unknown tag in
          // the current block description
          log.warn("Invalid tag \"" + curPrice.getItemType() + "\" found in Region " + curRegion
              + ", ignoring it.");
        } else {
          resultRegion.getPrices().put(newPrice.getItemType().getID(), newPrice);
        }
      }
    }

    /******************** MERGE RESOURCES *************************************/
    if (!curRegion.resources().isEmpty()) {
      for (final RegionResource curRes : curRegion.resources()) {
        RegionResource newRes = resultRegion.getResource(curRes.getType());

        /**
         * Remember: Merging of regions works as follows: A new set of regions is created in the new
         * GameData object. Then first the regions of the older report are merged into that new
         * object. Then the regions of the newer report are merged into that new object. At this
         * time sameTurn is guaranteed to be true! The crucial point is when a resource is suddenly
         * not seen any longer, because its level has increased. Please note: - resultRegion is
         * always part of the resulting GameData - curRegion is always the data to be "merged" into
         * newRegion (if applicable) Fiete Special case Mallorn: it could have disappeard (fully
         * cut). In above way it is added from old region and not removed, if not in new region. but
         * we should only erase that mallorn info, if we have a unit in the region for that we have
         * to use curRegion (units not yet merged)
         */
        if (newRes == null) {
          // add Resource
          newRes =
              new RegionResource(curRes.getID(), resultGD.getRules().getItemType(
                  curRes.getType().getID(), true));
          resultRegion.addResource(newRes);
        }

        RegionResource.merge(curGD, curRes, resultGD, newRes, sameTurn);
      }
    }

    /******************** DELETE RESOURCES IF NECESSARY **********************************/
    // Now look for those resources, that are in the new created game data,
    // but not in the current one. These are those, that are not seen in the
    // maybe newer report! This maybe because their level has changed.
    // Types for which no skill is needed to see
    final ItemType horsesType = resultGD.getRules().getItemType(EresseaConstants.I_RHORSES);
    final ItemType treesType = resultGD.getRules().getItemType(EresseaConstants.I_TREES);
    final ItemType mallornType = resultGD.getRules().getItemType(EresseaConstants.I_RMALLORN);
    final ItemType schoesslingeType = resultGD.getRules().getItemType(EresseaConstants.I_SPROUTS);
    final ItemType mallornSchoesslingeType =
        resultGD.getRules().getItemType(EresseaConstants.I_MALLORNSPROUTS);
    // FF 20080910...need new resources too
    final ItemType bauernType = resultGD.getRules().getItemType(EresseaConstants.I_PEASANTS);
    final ItemType silberType = resultGD.getRules().getItemType(EresseaConstants.I_RSILVER);

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
            log.warn("Fiete thinks this cannot happen");
          }

          if (skillIrrelevantTypes.contains(newRes.getType())) {
            // we have "our" Type
            // better using the visibility: 3 or 4 should show resources
            if (curRegion.getVisibility().greaterEqual(Visibility.TRAVEL)) {
              // we have. So we know now for sure, that this
              // resource disappeared. So lets delete it.
              // if (deleteRegionRessources == null) {
              // deleteRegionRessources = new ArrayList<ItemType>();
              // }
              // deleteRegionRessources.add(newRes.getType());
              newRes.setAmount(0);
              newRes.setDate(curGD.getDate().getDate());
            }

          }

        }
      }
      // if (deleteRegionRessources != null) {
      // // so we have Resources, that are not present any more
      // for (final ItemType regResID : deleteRegionRessources) {
      // // newRegion.resources().remove(regResID);
      // // doesn't work, as it doesn't modify the
      // // collection (only the hashset)
      // resultRegion.removeResource(regResID);
      // }
      // }
    }

    /******************** SCHEMES *************************************/
    if (!curRegion.schemes().isEmpty()) {
      for (Scheme curScheme : curRegion.schemes()) {
        CoordinateID newID = transform(transformer, curScheme.getID());
        Scheme newScheme = resultRegion.getScheme(newID);

        if (newScheme == null) {
          newScheme = MagellanFactory.createScheme(newID);
          resultRegion.addScheme(newScheme);
        }

        GameDataMerger.mergeScheme(curGD, curScheme, resultGD, newScheme);
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
    if (curGD.isSameRound(resultGD)) {
      if ((curRegion.getEvents() != null) && (curRegion.getEvents().size() > 0)) {
        if (resultRegion.getEvents() == null) {
          resultRegion.setEvents(new LinkedList<Message>());
        }

        for (Message curMsg : curRegion.getEvents()) {
          Message newMsg = null;

          newMsg = MagellanFactory.createMessage(curMsg.getID());

          GameDataMerger.mergeMessage(curGD, curMsg, resultGD, newMsg, transformer);
          resultRegion.getEvents().add(newMsg);
        }
      }

      if ((curRegion.getMessages() != null) && (curRegion.getMessages().size() > 0)) {
        if (resultRegion.getMessages() == null) {
          resultRegion.setMessages(new ArrayList<Message>());
        }

        for (Message curMsg : curRegion.getMessages()) {
          Message newMsg = null;

          newMsg = MagellanFactory.createMessage(curMsg.getID());

          GameDataMerger.mergeMessage(curGD, curMsg, resultGD, newMsg, transformer);
          resultRegion.getMessages().add(newMsg);
        }
      }

      if ((curRegion.getPlayerMessages() != null) && (curRegion.getPlayerMessages().size() > 0)) {
        if (resultRegion.getPlayerMessages() == null) {
          resultRegion.setPlayerMessages(new ArrayList<Message>());
        }

        for (Message curMsg : curRegion.getPlayerMessages()) {
          Message newMsg = null;

          newMsg = MagellanFactory.createMessage(curMsg.getID());

          GameDataMerger.mergeMessage(curGD, curMsg, resultGD, newMsg, transformer);
          resultRegion.getPlayerMessages().add(newMsg);
        }
      }

      if ((curRegion.getSurroundings() != null) && (curRegion.getSurroundings().size() > 0)) {
        if (resultRegion.getSurroundings() == null) {
          resultRegion.setSurroundings(new LinkedList<Message>());
        }

        for (Message curMsg : curRegion.getSurroundings()) {
          Message newMsg = null;

          newMsg = MagellanFactory.createMessage(curMsg.getID());

          GameDataMerger.mergeMessage(curGD, curMsg, resultGD, newMsg, transformer);
          resultRegion.getSurroundings().add(newMsg);
        }
      }

      if ((curRegion.getTravelThru() != null) && (curRegion.getTravelThru().size() > 0)) {
        if (resultRegion.getTravelThru() == null) {
          resultRegion.setTravelThru(new LinkedList<Message>());
        }

        for (Message curMsg : curRegion.getTravelThru()) {
          Message newMsg = null;

          newMsg = MagellanFactory.createMessage(curMsg.getID());

          GameDataMerger.mergeMessage(curGD, curMsg, resultGD, newMsg, transformer);

          // 2002.02.21 pavkovic: prevent double entries
          if (!resultRegion.getTravelThru().contains(newMsg)) {
            resultRegion.getTravelThru().add(newMsg);
          }
        }
      }

      if ((curRegion.getTravelThruShips() != null) && (curRegion.getTravelThruShips().size() > 0)) {
        if (resultRegion.getTravelThruShips() == null) {
          resultRegion.setTravelThruShips(new LinkedList<Message>());
        }

        for (Message curMsg : curRegion.getTravelThruShips()) {
          Message newMsg = null;

          newMsg = MagellanFactory.createMessage(curMsg.getID());

          GameDataMerger.mergeMessage(curGD, curMsg, resultGD, newMsg, transformer);

          // 2002.02.21 pavkovic: prevent duplicate entries
          if (!resultRegion.getTravelThruShips().contains(newMsg)) {
            resultRegion.getTravelThruShips().add(newMsg);
          }
        }
      }

      if (curRegion.getItems() != null) {
        for (Item currentItem : curRegion.getItems()) {
          resultRegion.addItem(currentItem);
        }
      }

    }
  }

  /**
   * Merges two Scheme objects.
   */
  public static void
      mergeScheme(GameData curGD, Scheme curScheme, GameData newGD, Scheme newScheme) {
    if (curScheme.getName() != null) {
      newScheme.setName(curScheme.getName());
    }
  }

  /**
   * Merges ships.
   *
   * @param transformer
   */
  public static void mergeShip(GameData curGD, Ship curShip, GameData newGD, Ship newShip,
      ReportTransformer transformer) {
    GameDataMerger.mergeUnitContainer(curGD, curShip, newGD, newShip);

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
      newShip.setRegion(newGD.getRegion(transform(transformer, curShip.getRegion().getID())));
    }

    newShip.setShoreId(curShip.getShoreId());

    if (curShip.getSize() != -1) {
      newShip.setSize(curShip.getSize());
    }

    if (curShip.getMaxPersons() != -1) {
      newShip.setMaxPersons(curShip.getMaxPersons());
    }

    if (curShip.getSpeed() != -1) {
      newShip.setSpeed(curShip.getSpeed());
    }
    // fleet support
    if (curShip.getAmount() != 1) {
      newShip.setAmount(curShip.getAmount());
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
      newSpell.setComponents(CollectionFactory.<String, String> createSyncOrderedMap());
      newSpell.getComponents().putAll(curSpell.getComponents());
    }
  }

  /**
   * Merges two temp units.
   *
   * @param sameRound notifies if both game data objects have been from the same round
   * @param transformer
   */
  public static void merge(GameData curGD, TempUnit curTemp, GameData newGD, TempUnit newTemp,
      boolean sameRound, boolean firstPass, ReportTransformer transformer) {
    GameDataMerger.mergeUnit(curGD, curTemp, newGD, newTemp, sameRound, firstPass, transformer);

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
   * @param transformer
   */
  public static void mergeUnit(GameData curGD, Unit curUnit, GameData resultGD, Unit resultUnit,
      boolean sameRound, boolean firstPass, ReportTransformer transformer) {
    /*
     * True, when curUnit is seen by the faction it belongs to and is therefore fully specified.
     */
    final boolean curWellKnown = curUnit.isDetailsKnown();

    /*
     * True, when newUnit is seen by the faction it belongs to and is therefore fully specified.
     * This is only meaningful in the second pass.
     */
    final boolean newWellKnown = resultUnit.isDetailsKnown();

    // do not overwrite with dummy units
    if (!firstPass && sameRound && curUnit.getRegion().equals(curGD.getNullRegion()) && !resultUnit
        .getRegion().equals(resultGD.getNullRegion()))
      return;

    if (curUnit.getName() != null) {
      resultUnit.setName(curUnit.getName());
    }

    if (curUnit.getDescription() != null) {
      resultUnit.setDescription(curUnit.getDescription());
    }

    if (curUnit.getAlias() != null) {
      resultUnit.setAlias(curUnit.getAlias());
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
        resultUnit.setCombatSpells(new LinkedHashMap<ID, CombatSpell>());
      } else {
        resultUnit.getCombatSpells().clear();
      }

      for (CombatSpell curCS : curUnit.getCombatSpells().values()) {
        CombatSpell newCS = null;

        newCS = MagellanFactory.createCombatSpell(curCS.getID());

        GameDataMerger.mergeCombatSpell(curGD, curCS, resultGD, newCS);
        resultUnit.getCombatSpells().put(newCS.getID(), newCS);
      }
    }

    if (!curUnit.ordersAreNull() && (curUnit.getCompleteOrders().size() > 0)) {
      resultUnit.setOrders2(curUnit.getCompleteOrders(), false);
    }

    resultUnit.setOrdersConfirmed((sameRound && resultUnit.isOrdersConfirmed())
        || curUnit.isOrdersConfirmed());

    if ((curUnit.getEffects() != null) && (curUnit.getEffects().size() > 0)) {
      if (resultUnit.getEffects() == null) {
        resultUnit.setEffects(new LinkedList<String>());
      } else {
        resultUnit.getEffects().clear();
      }

      resultUnit.getEffects().addAll(curUnit.getEffects());
    }

    if (curUnit.getFaction() != null) {
      if ((resultUnit.getFaction() == null) || curWellKnown || !newWellKnown) {
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
        resultUnit.clearItems();

        for (Item curItem : curUnit.getItems()) {
          final Item newItem =
              new Item(resultGD.getRules().getItemType(curItem.getItemType().getID(), true),
                  curItem.getAmount());
          resultUnit.addItem(newItem);
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
      resultUnit.setRealRace(resultGD.getRules().getRace(curUnit.getRace().getID(), true));
      resultUnit.setRace(resultGD.getRules().getRace(curUnit.getDisguiseRace().getID(), true));
    } else if (curUnit.getRace() != null) {
      resultUnit.setRace(resultGD.getRules().getRace(curUnit.getRace().getID(), true));
    }

    if (curUnit.getRaceNamePrefix() != null) {
      resultUnit.setRaceNamePrefix(curUnit.getRaceNamePrefix());
    }

    if (curUnit.getRegion() != null) {
      resultUnit.setRegion(resultGD.getRegion(transform(transformer, curUnit.getRegion().getID())));
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
    final Collection<Skill> oldSkills = new ArrayList<Skill>();

    final boolean resultWellKnown = resultUnit.getSkillMap() != null;
    if (resultUnit.getSkillMap() != null) {
      oldSkills.addAll(resultUnit.getSkills());
    } else if (curWellKnown || curUnit.getSkillMap() != null) {
      resultUnit.setSkills(CollectionFactory.<StringID, Skill> createSyncOrderedMap(2));
    }

    if ((curUnit.getSkills() != null) && (curUnit.getSkills().size() > 0)) {
      for (final Skill curSkill : curUnit.getSkills()) {
        final SkillType newSkillType =
            resultGD.getRules().getSkillType(curSkill.getSkillType().getID(), true);
        final Skill newSkill =
            new Skill(newSkillType, curSkill.getPoints(), curSkill.getLevel(), resultUnit
                .getPersons(), curSkill.noSkillPoints());

        // NOTE: Maybe some decision about change-level computation in reports
        // of same date here
        final Skill oldSkill = resultUnit.getSkillMap().put(newSkillType.getID(), newSkill);

        // if (!sameRound) {
        // // notify change as we are not in the same round.
        // if (oldSkill != null) {
        // final int dec = oldSkill.getLevel();
        // newSkill.setChangeLevel(newSkill.getLevel() - dec);
        // } else {
        // // the skill is new as we did not have it before
        // newSkill.setChangeLevel(newSkill.getLevel());
        // }
        // } else {
        // // TR 2008-03-25
        // // okay, this is a try to make it possible to show level changes
        // // of multiple factions in one week. Problem: If you load a second
        // // report from the same round, the level changes are not updates
        // //
        // // my solution now is to set the level if we found an old skill. The
        // // old skill is actually the negative current level of the first pass
        // //
        // // there is a known problem if there are more known factions with
        // // unknown skills (not imported factions). At the moment I don't
        // // have an idea, how to handle this, because this happens in the
        // // second pass...
        // if (oldSkill != null) {
        // if (oldSkill.getLevel() != newSkill.getLevel() && !newSkill.isLevelChanged()) {
        // if (!firstPass) {
        // newSkill.setChangeLevel(newSkill.getLevel() - oldSkill.getLevel()); // FIXME
        // } else {
        // // newSkill.setLevelChanged(false);
        // }
        // }
        // } else {
        // if (curSkill.getLevel() == 0 && newSkill.getLevel() == 0 && curSkill.isLevelChanged()
        // && !curWellKnown) {
        // // newSkill.setLevel(curSkill.getLevel() + curSkill.getChangeLevel() * (-1));
        // }
        // }
        // }

        // (stm 2012-09) just keep old skills; set change level if old skill was well known
        if (!firstPass) {
          if (oldSkill == null) {
            if (resultWellKnown) {
              newSkill.setChangeLevel(newSkill.getLevel());
            }
          } else {
            newSkill.setChangeLevel(newSkill.getLevel() - oldSkill.getLevel());
          }
        }

        // put this at the end to overwrite automatic changes
        if (curSkill.isLevelChanged()) {
          newSkill.setChangeLevel(curSkill.getChangeLevel());
        }

        if (curSkill.isLostSkill()) {
          newSkill.setLostLevel(-curSkill.getChangeLevel());
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
    // stm 2012.09.26: mark skill as lost whenever the current skills should be well known
    if (!sameRound || curWellKnown) {
      // Now remove all skills that are lost
      for (Skill oldSkill : oldSkills) {
        if (oldSkill.isLostSkill()) { // remove if it was lost // TODO does this ever happen?
          resultUnit.getSkillMap().remove(oldSkill.getSkillType().getID());
        } else { // dont remove it but mark it as a lostSkill
          if (curWellKnown) {
            Skill newSkill = resultUnit.getSkill(oldSkill.getSkillType());
            newSkill.setLostLevel(oldSkill.getLevel());
          }
        }
      }
    }

    // (stm) this had effectively destroyed report unit sorting
    // resultUnit.setSortIndex(Math.max(resultUnit.getSortIndex(), curUnit.getSortIndex()));

    if ((curUnit.getSpells() != null) && (curUnit.getSpells().size() > 0)) {
      if (resultUnit.getSpells() == null) {
        resultUnit.setSpells(new LinkedHashMap<ID, Spell>());
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
      resultUnit.setTempID(curUnit.getTempID());
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
        resultUnit.setUnitMessages(new ArrayList<Message>());
      }

      for (Message curMsg : curUnit.getUnitMessages()) {
        Message newMsg = null;

        newMsg = MagellanFactory.createMessage(curMsg.getID());

        GameDataMerger.mergeMessage(curGD, curMsg, resultGD, newMsg, transformer);
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

  public static void mergeAlliance(GameData curGD, AllianceGroup curAlliance, GameData newGD,
      AllianceGroup newAlliance) {
    newAlliance.setName(curAlliance.getName());
    newAlliance.setLeader(curAlliance.getLeader());

    for (final ID f : curAlliance.getFactions()) {
      newAlliance.addFaction(newGD.getFaction(f));
    }
  }
}
