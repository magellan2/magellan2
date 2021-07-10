// class magellan.library.EmptyData
// created on Feb 16, 2010
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
package magellan.library;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import magellan.library.rules.MessageType;
import magellan.library.utils.Translations;
import magellan.library.utils.transformation.ReportTransformer;

/**
 * A GameData that contains nothing. Nothing can be added to this data.
 * 
 * @author stm
 */
public class EmptyData extends GameData {

  private Locale locale;
  private Translations translations = new Translations();

  /**
   * @param rules
   */
  public EmptyData(Rules rules) {
    super(rules);
    postProcess();
  }

  /**
   * @param rules
   * @param name
   */
  public EmptyData(Rules rules, String name) {
    super(rules, name);
    postProcess();
  }

  /**
   * Takes rules, game name, date, base, version, locale and "noSkillPoints" from data.
   */
  public EmptyData(GameData data) {
    super(data.getRules(), data.getGameName());
    setDate(data.getDate());
    base = data.base;
    version = data.version;
    noSkillPoints = data.noSkillPoints;
    locale = data.getLocale();
  }

  // /**
  // * @see magellan.library.GameData#addHotSpot(magellan.library.HotSpot)
  // */
  // @Override
  // public void addHotSpot(HotSpot h) {
  // throw new UnsupportedOperationException("cannot add to EmptyData");
  // }

  /**
   * @see magellan.library.GameData#addSelectedRegionCoordinate(magellan.library.Region)
   */
  @Override
  public void addSelectedRegionCoordinate(Region region) {
    throw new UnsupportedOperationException("cannot add to EmptyData");
  }

  /**
   * @see magellan.library.GameData#addTempUnit(magellan.library.TempUnit)
   */
  @Override
  public void addTempUnit(TempUnit t) {
    throw new UnsupportedOperationException("cannot add to EmptyData");
  }

  /**
   * @see magellan.library.GameData#buildingView()
   */
  @Override
  protected Map<EntityID, Building> buildingView() {
    return Collections.emptyMap();
  }

  /**
   * @see magellan.library.GameData#estimateSize()
   */
  @Override
  public long estimateSize() {
    return 1000;
  }

  /**
   * @see magellan.library.GameData#factionView()
   */
  @Override
  protected Map<EntityID, Faction> factionView() {
    return Collections.emptyMap();
  }

  /**
   * @see magellan.library.GameData#getLocale()
   */
  @Override
  public Locale getLocale() {
    return locale;
  }

  /**
   * @see magellan.library.GameData#getSelectedRegionCoordinates()
   */
  @Override
  public Map<CoordinateID, Region> getSelectedRegionCoordinates() {
    return Collections.emptyMap();
  }

  // /**
  // * @see magellan.library.GameData#hotSpotView()
  // */
  // @Override
  // protected Map<IntegerID, HotSpot> hotSpotView() {
  // return Collections.emptyMap();
  // }

  /**
   * @see magellan.library.GameData#islandView()
   */
  @Override
  public Map<IntegerID, Island> islandView() {
    return Collections.emptyMap();
  }

  /**
   * @see magellan.library.GameData#msgTypeView()
   */
  @Override
  protected Map<IntegerID, MessageType> msgTypeView() {
    return Collections.emptyMap();
  }

  /**
   * @see magellan.library.GameData#potionView()
   */
  @Override
  protected Map<IntegerID, Potion> potionView() {
    return Collections.emptyMap();
  }

  /**
   * @see magellan.library.GameData#regionView()
   */
  @Override
  protected Map<CoordinateID, Region> regionView() {
    return Collections.emptyMap();
  }

  /**
   * @see magellan.library.GameData#setIslands(java.util.Map)
   */
  @Override
  public void setIslands(Map<IntegerID, Island> islands) {
    throw new UnsupportedOperationException("cannot add to EmptyData");
  }

  /**
   * @see magellan.library.GameData#setLocale(java.util.Locale)
   */
  @Override
  public void setLocale(Locale l) {
    locale = l;
  }

  /**
   * @see magellan.library.GameData#setSelectedRegionCoordinates(java.util.Map)
   */
  @Override
  public void setSelectedRegionCoordinates(Map<CoordinateID, Region> regions) {
    throw new UnsupportedOperationException("cannot add to EmptyData");
  }

  /**
   * @see magellan.library.GameData#shipView()
   */
  @Override
  protected Map<EntityID, Ship> shipView() {
    return Collections.emptyMap();
  }

  /**
   * @see magellan.library.GameData#spellView()
   */
  @Override
  protected Map<StringID, Spell> spellView() {
    return Collections.emptyMap();
  }

  /**
   * @see magellan.library.GameData#tempUnitView()
   */
  @Override
  protected Map<UnitID, TempUnit> tempUnitView() {
    return Collections.emptyMap();
  }

  /**
   * @see magellan.library.GameData#translations()
   */
  @Override
  public Translations translations() {
    return translations;
  }

  /**
   * @see magellan.library.GameData#unitView()
   */
  @Override
  protected Map<UnitID, Unit> unitView() {
    return Collections.emptyMap();
  }

  /**
   * @see magellan.library.GameData#addAllianceGroup(magellan.library.AllianceGroup)
   */
  @Override
  public void addAllianceGroup(AllianceGroup alliance) {
    throw new UnsupportedOperationException("cannot add to EmptyData");
  }

  /**
   * @see magellan.library.GameData#addAttribute(java.lang.String, java.lang.String)
   */
  @Override
  public void addAttribute(String key, String value) {
    // FIXME (stm) make this depend on a ...View() method
    throw new UnsupportedOperationException("cannot add to EmptyData");
  }

  /**
   * @see magellan.library.GameData#clearTranslations(magellan.library.EntityID)
   */
  @Override
  public void clearTranslations(EntityID f) {
    throw new UnsupportedOperationException("cannot add to EmptyData");
  }

  /**
   * @see magellan.library.GameData#clone()
   */
  @Override
  public GameData clone() throws CloneNotSupportedException {
    return new EmptyData(getRules(), getGameName());
  }

  /**
   * @see magellan.library.GameData#clone(magellan.library.CoordinateID)
   */
  @Override
  public GameData clone(CoordinateID newOrigin) throws CloneNotSupportedException {
    return new EmptyData(getRules(), getGameName());
  }

  /**
   * @see magellan.library.GameData#clone(ReportTransformer)
   */
  @Override
  public GameData clone(ReportTransformer coordinateTranslator) throws CloneNotSupportedException {
    return new EmptyData(getRules(), getGameName());
  }

  /**
   * @see magellan.library.GameData#getActiveRegion()
   */
  @Override
  public Region getActiveRegion() {
    return null;
  }

  /**
   * @see magellan.library.GameData#getAllianceGroup(magellan.library.ID)
   */
  @Override
  public AllianceGroup getAllianceGroup(ID allianceID) {
    return null;
  }

  /**
   * @see magellan.library.GameData#getAllianceGroups()
   */
  @Override
  public Collection<AllianceGroup> getAllianceGroups() {
    return Collections.emptyList();
  }

  /**
   * @see magellan.library.GameData#getAttribute(java.lang.String)
   */
  @Override
  public String getAttribute(String key) {
    return null;
  }

  /**
   * @see magellan.library.GameData#getAttributeKeys()
   */
  @Override
  public List<String> getAttributeKeys() {
    return Collections.emptyList();
  }

  /**
   * @see magellan.library.GameData#getAttributeSize()
   */
  @Override
  public int getAttributeSize() {
    return 0;
  }

  /**
   * @see magellan.library.GameData#getCoordinateTranslation(magellan.library.EntityID, int)
   */
  @Override
  public CoordinateID getCoordinateTranslation(EntityID otherFaction, int layer) {
    return null;
  }

  /**
   * @see magellan.library.GameData#getCoordinateTranslationMap(magellan.library.EntityID)
   */
  @Override
  public Map<Integer, CoordinateID> getCoordinateTranslationMap(EntityID otherFaction) {
    return Collections.emptyMap();
  }

  /**
   * @see magellan.library.GameData#getCoordinateTranslations()
   */
  @Override
  protected Map<EntityID, Map<Integer, CoordinateID>> getCoordinateTranslations() {
    return Collections.emptyMap();
  }

  /**
   * @see magellan.library.GameData#getOwnerFaction()
   */
  @Override
  public EntityID getOwnerFaction() {
    return null;
  }

  /**
   * @see magellan.library.GameData#makeWrapper(Region, Region)
   */
  @Override
  public void makeWrapper(Region w, Region o) {
    throw new UnsupportedOperationException("cannot add to EmptyData");
  }

  /**
   * @see magellan.library.GameData#setActiveRegion(magellan.library.Region)
   */
  @Override
  public void setActiveRegion(Region region) {
    throw new UnsupportedOperationException("cannot add to EmptyData");
  }

  /**
   * @see magellan.library.GameData#setCoordinateTranslation(magellan.library.EntityID,
   *      magellan.library.CoordinateID)
   */
  @Override
  public void setCoordinateTranslation(EntityID otherFaction, CoordinateID usedTranslation) {
    throw new UnsupportedOperationException("cannot add to EmptyData");
  }

  /**
   * @see magellan.library.GameData#wrappers()
   */
  @Override
  public Map<CoordinateID, Region> wrappers() {
    return Collections.emptyMap();
  }

  @Override
  protected Map<UnitID, Unit> oldUnitsView() {
    return Collections.emptyMap();
  }

  @Override
  public void addBookmark(Bookmark bookmark) {
    // nothing to do
  }

  @Override
  public Bookmark getBookmark(Selectable selection) {
    return null;
  }

  @Override
  public Collection<Bookmark> getBookmarks() {
    return Collections.emptyList();
  }

  @Override
  public void removeBookmark(Selectable selection) {
    // NOP
  }

}
