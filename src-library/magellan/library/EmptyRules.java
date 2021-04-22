// class magellan.library.EmptyRules
// created on Sep 3, 2017
//
// Copyright 2003-2017 by magellan project team
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
package magellan.library;

import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.Map;

import magellan.library.completion.Completer;
import magellan.library.completion.CompleterSettingsProvider;
import magellan.library.completion.OrderParser;
import magellan.library.gamebinding.GameSpecificOrderReader;
import magellan.library.gamebinding.GameSpecificOrderWriter;
import magellan.library.gamebinding.GameSpecificRules;
import magellan.library.gamebinding.GameSpecificStuff;
import magellan.library.gamebinding.MapMergeEvaluator;
import magellan.library.gamebinding.MapMetric;
import magellan.library.gamebinding.MessageRenderer;
import magellan.library.gamebinding.MovementEvaluator;
import magellan.library.gamebinding.OrderChanger;
import magellan.library.gamebinding.RelationFactory;
import magellan.library.io.GameDataIO;
import magellan.library.io.ReportParser;
import magellan.library.io.file.FileType;
import magellan.library.rules.AllianceCategory;
import magellan.library.rules.BuildingType;
import magellan.library.rules.CastleType;
import magellan.library.rules.FactionType;
import magellan.library.rules.ItemCategory;
import magellan.library.rules.ItemType;
import magellan.library.rules.ObjectType;
import magellan.library.rules.OptionCategory;
import magellan.library.rules.OrderType;
import magellan.library.rules.Race;
import magellan.library.rules.RegionType;
import magellan.library.rules.ShipType;
import magellan.library.rules.SkillCategory;
import magellan.library.rules.SkillType;
import magellan.library.utils.UserInterface;
import magellan.library.utils.transformation.ReportTransformer;

public class EmptyRules implements Rules {

  public RegionType getRegionType(StringID id, boolean add) {
    return null;
  }

  public RegionType getRegionType(StringID id) {
    return null;
  }

  public Iterator<RegionType> getRegionTypeIterator() {
    return Collections.emptyListIterator();
  }

  public Collection<RegionType> getRegionTypes() {
    return Collections.emptyList();
  }

  public RegionType getRegionType(String id, boolean add) {
    return null;
  }

  public RegionType getRegionType(String id) {
    return null;
  }

  public Race getRace(StringID id) {
    return null;
  }

  public Race getRace(StringID id, boolean add) {
    return null;
  }

  public Iterator<Race> getRaceIterator() {
    return Collections.emptyListIterator();
  }

  public Collection<Race> getRaces() {
    return Collections.emptyList();
  }

  public Race getRace(String id, boolean add) {
    return null;
  }

  public Race getRace(String id) {
    return null;
  }

  public ShipType getShipType(StringID id) {
    return null;
  }

  public ShipType getShipType(StringID id, boolean add) {
    return null;
  }

  public Iterator<ShipType> getShipTypeIterator() {
    return Collections.emptyListIterator();
  }

  public Collection<ShipType> getShipTypes() {
    return Collections.emptyList();
  }

  public ShipType getShipType(String id, boolean add) {
    return null;
  }

  public ShipType getShipType(String id) {
    return null;
  }

  public BuildingType getBuildingType(StringID id) {
    return null;
  }

  public BuildingType getBuildingType(StringID id, boolean add) {
    return null;
  }

  public Iterator<BuildingType> getBuildingTypeIterator() {
    return Collections.emptyListIterator();
  }

  public Collection<BuildingType> getBuildingTypes() {
    return Collections.emptyList();
  }

  public BuildingType getBuildingType(String id, boolean add) {
    return null;
  }

  public BuildingType getBuildingType(String id) {
    return null;
  }

  public CastleType getCastleType(StringID id) {
    return null;
  }

  public CastleType getCastleType(StringID id, boolean add) {
    return null;
  }

  public Iterator<CastleType> getCastleTypeIterator() {
    return Collections.emptyListIterator();
  }

  public Collection<CastleType> getCastleTypes() {
    return Collections.emptyList();
  }

  public CastleType getCastleType(String id, boolean add) {
    return null;
  }

  public CastleType getCastleType(String id) {
    return null;
  }

  public ItemType getItemType(StringID id) {
    return null;
  }

  public ItemType getItemType(StringID id, boolean add) {
    return null;
  }

  public Iterator<ItemType> getItemTypeIterator() {
    return Collections.emptyListIterator();
  }

  public Collection<ItemType> getItemTypes() {
    return Collections.emptyList();
  }

  public ItemType getItemType(String id, boolean add) {
    return null;
  }

  public ItemType getItemType(String id) {
    return null;
  }

  public SkillType getSkillType(StringID id) {
    return null;
  }

  public SkillType getSkillType(StringID id, boolean add) {
    return null;
  }

  public Iterator<SkillType> getSkillTypeIterator() {
    return Collections.emptyListIterator();
  }

  public Collection<SkillType> getSkillTypes() {
    return Collections.emptyList();
  }

  public SkillType getSkillType(String id, boolean add) {
    return null;
  }

  public SkillType getSkillType(String id) {
    return null;
  }

  public ItemCategory getItemCategory(StringID id) {
    return null;
  }

  public ItemCategory getItemCategory(StringID id, boolean add) {
    return null;
  }

  public Iterator<ItemCategory> getItemCategoryIterator() {
    return Collections.emptyListIterator();
  }

  public Collection<ItemCategory> getItemCategories() {
    return Collections.emptyList();
  }

  public ItemCategory getItemCategory(String id, boolean add) {
    return null;
  }

  public ItemCategory getItemCategory(String id) {
    return null;
  }

  public SkillCategory getSkillCategory(StringID id) {
    return null;
  }

  public SkillCategory getSkillCategory(StringID id, boolean add) {
    return null;
  }

  public Iterator<SkillCategory> getSkillCategoryIterator() {
    return Collections.emptyListIterator();
  }

  public Collection<SkillCategory> getSkillCategories() {
    return Collections.emptyList();
  }

  public SkillCategory getSkillCategory(String id, boolean add) {
    return null;
  }

  public SkillCategory getSkillCategory(String id) {
    return null;
  }

  public OptionCategory getOptionCategory(StringID id) {
    return null;
  }

  public OptionCategory getOptionCategory(StringID id, boolean add) {
    return null;
  }

  public Iterator<OptionCategory> getOptionCategoryIterator() {
    return Collections.emptyListIterator();
  }

  public Collection<OptionCategory> getOptionCategories() {
    return Collections.emptyList();
  }

  public OptionCategory getOptionCategory(String id, boolean add) {
    return null;
  }

  public OptionCategory getOptionCategory(String id) {
    return null;
  }

  public AllianceCategory getAllianceCategory(StringID id) {
    return null;
  }

  public AllianceCategory getAllianceCategory(StringID id, boolean add) {
    return null;
  }

  public Iterator<AllianceCategory> getAllianceCategoryIterator() {
    return Collections.emptyListIterator();
  }

  public Collection<AllianceCategory> getAllianceCategories() {
    return Collections.emptyList();
  }

  public AllianceCategory getAllianceCategory(String id, boolean add) {
    return null;
  }

  public AllianceCategory getAllianceCategory(String id) {
    return null;
  }

  public ObjectType changeName(String from, String to) {
    return null;
  }

  public void setGameSpecificStuffClassName(String className) throws IOException {
    /* nop */
  }

  public GameSpecificStuff getGameSpecificStuff() {
    return new GameSpecificStuff() {

      public void postProcessAfterTrustlevelChange(GameData data) {
        /* nop */
      }

      public void postProcess(GameData data) {
        /* nop */
      }

      public ReportTransformer[] getTransformers(GameData globalData, GameData addedData, UserInterface ui,
          boolean interactive) {
        return null;
      }

      public RelationFactory getRelationFactory() {
        return null;
      }

      public ReportParser getParser(FileType aFileType) throws IOException {
        return null;
      }

      public GameSpecificOrderWriter getOrderWriter() {
        return null;
      }

      public GameSpecificOrderReader getOrderReader(GameData data) {
        return null;
      }

      public OrderParser getOrderParser(GameData data) {
        return null;
      }

      public OrderChanger getOrderChanger() {
        return null;
      }

      public String getName() {
        return null;
      }

      public MovementEvaluator getMovementEvaluator() {
        return null;
      }

      public MessageRenderer getMessageRenderer(GameData data) {
        return null;
      }

      public MapMetric getMapMetric() {
        return null;
      }

      public MapMergeEvaluator getMapMergeEvaluator() {
        return null;
      }

      public GameSpecificRules getGameSpecificRules() {
        return null;
      }

      public GameDataIO getGameDataIO() {
        return null;
      }

      public CoordMapper getCoordMapper() {
        return null;
      }

      public Completer getCompleter(GameData data, CompleterSettingsProvider csp) {
        return null;
      }

      public Map<Integer, String> getCombatStates() {
        return null;
      }

      public GameData createGameData(String name) {
        return null;
      }
    };
  }

  public String getOrderfileStartingString() {
    return null;
  }

  public void setOrderfileStartingString(String startingString) {
    /* nop */
  }

  public OrderType getOrder(String id) {
    return null;
  }

  public OrderType getOrder(StringID id) {
    return null;
  }

  public OrderType getOrder(StringID id, boolean add) {
    return null;
  }

  public Collection<OrderType> getOrders() {
    return null;
  }

  public void setGameName(String name) {
    // nop
  }

  public String getGameName() {
    return null;
  }

  public FactionType getFaction(StringID id, boolean add) {
    return null;
  }

  public FactionType getFaction(StringID id) {
    return null;
  }

  public Collection<FactionType> getFactions() {
    return null;
  }

}
