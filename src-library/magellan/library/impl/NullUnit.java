// class magellan.library.impl.NullUnit
// created on Jan 27, 2020
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
package magellan.library.impl;

import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import magellan.library.Building;
import magellan.library.CombatSpell;
import magellan.library.CoordinateID;
import magellan.library.Faction;
import magellan.library.GameData;
import magellan.library.Group;
import magellan.library.ID;
import magellan.library.Item;
import magellan.library.Message;
import magellan.library.Order;
import magellan.library.Orders;
import magellan.library.Region;
import magellan.library.Ship;
import magellan.library.Skill;
import magellan.library.Spell;
import magellan.library.StringID;
import magellan.library.TempUnit;
import magellan.library.Unit;
import magellan.library.UnitContainer;
import magellan.library.UnitID;
import magellan.library.gamebinding.GameSpecificStuff;
import magellan.library.relation.ItemTransferRelation;
import magellan.library.relation.PersonTransferRelation;
import magellan.library.relation.ReserveRelation;
import magellan.library.relation.UnitRelation;
import magellan.library.rules.ItemType;
import magellan.library.rules.Race;
import magellan.library.rules.SkillType;
import magellan.library.utils.Cache;
import magellan.library.utils.CacheHandler;
import magellan.library.utils.guiwrapper.CacheableOrderEditor;

/**
 * Null implementation
 *
 * @author stm
 * @version 1.0, Jan 27, 2020
 */
public class NullUnit implements Unit {

  public List<UnitRelation> getRelations() {
    return null;
  }

  public <T extends UnitRelation> List<T> getRelations(Class<T> relationClass) {
    return null;
  }

  public void setDescription(String description) {
    //
  }

  public String getDescription() {
    return null;
  }

  public String getName() {
    return null;
  }

  public void setName(String name) {
    //
  }

  public int compareTo(Object o) {
    return 0;
  }

  public void addAttribute(String key, String value) {
    //
  }

  public List<String> getAttributeKeys() {
    return null;
  }

  public String getAttribute(String key) {
    return null;
  }

  public boolean containsAttribute(String key) {
    return false;
  }

  public int getAttributeSize() {
    return 0;
  }

  public boolean hasTags() {
    return false;
  }

  public boolean containsTag(String tag) {
    return false;
  }

  public String putTag(String tag, String value) {
    return null;
  }

  public String getTag(String tag) {
    return null;
  }

  public String removeTag(String tag) {
    return null;
  }

  public void deleteAllTags() {
    //
  }

  public Map<String, String> getTagMap() {
    return null;
  }

  public boolean hasCache() {
    return false;
  }

  public Cache getCache() {
    return null;
  }

  public void setCache(Cache cache) {
    //
  }

  public void clearCache() {
    //
  }

  public void addCacheHandler(CacheHandler handler) {
    //
  }

  public boolean ordersAreNull() {
    return false;
  }

  public void clearOrders() {
    //
  }

  public void clearOrders(boolean refreshRelations) {
    //
  }

  public void removeOrderAt(int i) {
    //
  }

  public void removeOrderAt(int i, boolean refreshRelations) {
    //
  }

  public boolean removeOrder(String order, int length) {
    return false;
  }

  public boolean removeOrder(String order, int length, boolean refreshRelations) {
    return false;
  }

  public void replaceOrder(int pos, Order newOrder) {
    //
  }

  public void replaceOrder(int pos, Order newOrder, boolean refreshRelations) {
    //
  }

  public void addOrderAt(int i, String newOrder) {
    //
  }

  public void addOrderAt(int i, String newOrders, boolean refreshRelations) {
    //
  }

  public void addOrders(Collection<String> newOrders) {
    //
  }

  public void addOrders(Collection<String> newOrders, boolean refreshRelations) {
    //
  }

  public void addOrders2(Collection<Order> newOrders) {
    //
  }

  public void addOrders2(Collection<Order> newOrders, boolean refreshRelations) {
    //
  }

  public void setOrders(Collection<String> newOrders) {
    //
  }

  public void setOrders(Collection<String> newOrders, boolean refreshRelations) {
    //
  }

  public void setOrders2(Collection<Order> newOrders) {
    //
  }

  public void setOrders2(Collection<Order> newOrders, boolean refreshRelations) {
    //
  }

  public List<String> getOrders() {
    return null;
  }

  public Orders getOrders2() {
    return null;
  }

  public void setGroup(Group g) {
    //
  }

  public Group getGroup() {
    return null;
  }

  public void setAlias(UnitID id) {
    //
  }

  public UnitID getAlias() {
    return null;
  }

  public Item getItem(ItemType type) {
    return null;
  }

  public void setSpy(boolean bool) {
    //
  }

  public boolean isSpy() {
    return false;
  }

  public void setGuiseFaction(Faction f) {
    //
  }

  public Faction getGuiseFaction() {
    return null;
  }

  public Item addItem(Item i) {
    return null;
  }

  public void setTempID(UnitID id) {
    //
  }

  public UnitID getTempID() {
    return null;
  }

  public void setRegion(Region r) {
    //
  }

  public Region getRegion() {
    return null;
  }

  public void setFaction(Faction faction) {
    //
  }

  public Faction getFaction() {
    return null;
  }

  public Locale getLocale() {
    return null;
  }

  public void setBuilding(Building building) {
    //
  }

  public Building getBuilding() {
    return null;
  }

  public void setShip(Ship ship) {
    //
  }

  public Ship getShip() {
    return null;
  }

  public void setSortIndex(int index) {
    //
  }

  public int getSortIndex() {
    return 0;
  }

  public void setRaceNamePrefix(String prefix) {
    //
  }

  public String getRaceNamePrefix() {
    return null;
  }

  public String getRaceName(GameData data) {
    return null;
  }

  public String getSimpleRealRaceName() {
    return null;
  }

  public String getSimpleRaceName() {
    return null;
  }

  public Collection<TempUnit> tempUnits() {
    return null;
  }

  public Unit getTempUnit(ID key) {
    return null;
  }

  public void clearTemps() {
    //
  }

  public Orders getCompleteOrders() {
    return null;
  }

  public Orders getCompleteOrders(boolean writeUnitTagsAsVorlageComment) {
    return null;
  }

  public TempUnit createTemp(GameData data, UnitID key) {
    return null;
  }

  public void deleteTemp(UnitID key, GameData data) {
    //
  }

  public String getModifiedName() {
    return null;
  }

  public void addRelation(UnitRelation rel) {
    //
  }

  public UnitRelation removeRelation(UnitRelation rel) {
    return null;
  }

  public void getRelatedUnits(Collection<Unit> units) {
    //
  }

  public void getRelatedUnits(Set<Unit> units,
      Set<magellan.library.relation.UnitRelation.ID> relations) {
    //
  }

  public List<CoordinateID> getModifiedMovement() {
    return null;
  }

  public Ship getModifiedShip() {
    return null;
  }

  public Building getModifiedBuilding() {
    return null;
  }

  public Skill getModifiedSkill(SkillType type) {
    return null;
  }

  public Collection<Skill> getModifiedSkills() {
    return null;
  }

  public UnitContainer getUnitContainer() {
    return null;
  }

  public UnitContainer getModifiedUnitContainer() {
    return null;
  }

  public Skill getSkill(SkillType type) {
    return null;
  }

  public Skill getSkill(StringID type) {
    return null;
  }

  public Skill addSkill(Skill s) {
    return null;
  }

  public Collection<Skill> getSkills() {
    return null;
  }

  public Map<StringID, Skill> getSkillMap() {
    return null;
  }

  public void clearSkills() {
    //
  }

  public Collection<Item> getItems() {
    return null;
  }

  public void setItems(Map<StringID, Item> items) {
    //
  }

  public Map<StringID, Item> getItemMap() {
    return null;
  }

  public void clearItems() {
    //
  }

  public Item getModifiedItem(ItemType type) {
    return null;
  }

  public Collection<ReserveRelation> getItemReserveRelations(ItemType itemType) {
    return null;
  }

  public List<ItemTransferRelation> getItemTransferRelations(ItemType type) {
    return null;
  }

  public List<PersonTransferRelation> getPersonTransferRelations() {
    return null;
  }

  public Collection<Item> getModifiedItems() {
    return null;
  }

  public int getPersons() {
    return 0;
  }

  public int getModifiedPersons() {
    return 0;
  }

  public int getModifiedCombatStatus() {
    return 0;
  }

  public int getModifiedGuard() {
    return 0;
  }

  public boolean getModifiedUnaided() {
    return false;
  }

  public boolean isWeightWellKnown() {
    return false;
  }

  public int getWeight() {
    return 0;
  }

  public int getSimpleWeight() {
    return 0;
  }

  public int getPayloadOnHorse() {
    return 0;
  }

  public int getPayloadOnFoot() {
    return 0;
  }

  public int getLoad() {
    return 0;
  }

  public int getModifiedLoad() {
    return 0;
  }

  public int getRadius() {
    return 0;
  }

  public int getModifiedWeight() {
    return 0;
  }

  public Collection<Unit> getPassengers() {
    return null;
  }

  public Collection<Unit> getCarriers() {
    return null;
  }

  public Collection<Unit> getPupils() {
    return null;
  }

  public Collection<Unit> getTeachers() {
    return null;
  }

  public Collection<Unit> getAttackVictims() {
    return null;
  }

  public Collection<Unit> getAttackAggressors() {
    return null;
  }

  public void refreshRelations() {
    //
  }

  public void refreshRelations(int from) {
    //
  }

  public String toString(boolean withName) {
    return null;
  }

  public boolean addOrder(String order) {
    return false;
  }

  public boolean addOrder(String order, boolean refreshRelations) {
    return false;
  }

  public boolean addOrder(String order, boolean replace, int length) {
    return false;
  }

  public void addOrder(Order newOrder, boolean refreshRelations) {
    //
  }

  public void addOrderAt(int i, Order newOrder) {
    //
  }

  public void addOrderAt(int i, Order newOrder, boolean refreshRelations) {
    //
  }

  public int extractTempUnits(GameData data, int tempSortIndex) {
    return 0;
  }

  public int extractTempUnits(GameData data, int tempSortIndex, Locale locale) {
    return 0;
  }

  public int getAura() {
    return 0;
  }

  public void setAura(int aura) {
    //
  }

  public int getAuraMax() {
    return 0;
  }

  public void setAuraMax(int auraMax) {
    //
  }

  public Map<ID, CombatSpell> getCombatSpells() {
    return null;
  }

  public void setCombatSpells(Map<ID, CombatSpell> combatSpells) {
    //
  }

  public int getCombatStatus() {
    return 0;
  }

  public void setCombatStatus(int combatStatus) {
    //
  }

  public List<String> getComments() {
    return null;
  }

  public void setComments(List<String> comments) {
    //
  }

  public List<String> getEffects() {
    return null;
  }

  public void setEffects(List<String> effects) {
    //
  }

  public ID getFamiliarmageID() {
    return null;
  }

  public void setFamiliarmageID(ID familiarmageID) {
    //
  }

  public Unit getFollows() {
    return null;
  }

  public void setFollows(Unit follows) {
    //
  }

  public int getGuard() {
    return 0;
  }

  public void setGuard(int guard) {
    //
  }

  public String getHealth() {
    return null;
  }

  public void setHealth(String health) {
    //
  }

  public boolean isHideFaction() {
    return false;
  }

  public void setHideFaction(boolean hideFaction) {
    //
  }

  public boolean isHero() {
    return false;
  }

  public void setHero(boolean isHero) {
    //
  }

  public boolean isStarving() {
    return false;
  }

  public void setStarving(boolean isStarving) {
    //
  }

  public boolean isOrdersConfirmed() {
    return false;
  }

  public void setOrdersConfirmed(boolean ordersConfirmed) {
    //
  }

  public String getPrivDesc() {
    return null;
  }

  public void setPrivDesc(String privDesc) {
    //
  }

  public Race getRace() {
    return null;
  }

  public void setRace(Race race) {
    //
  }

  public Race getDisguiseRace() {
    return null;
  }

  public void setRealRace(Race realRace) {
    //
  }

  public Building getSiege() {
    return null;
  }

  public void setSiege(Building siege) {
    //
  }

  public Map<ID, Spell> getSpells() {
    return null;
  }

  public void setSpells(Map<ID, Spell> spells) {
    //
  }

  public int getStealth() {
    return 0;
  }

  public void setStealth(int stealth) {
    //
  }

  public boolean isUnaided() {
    return false;
  }

  public void setUnaided(boolean unaided) {
    //
  }

  public List<Message> getUnitMessages() {
    return null;
  }

  public void setUnitMessages(List<Message> unitMessages) {
    //
  }

  public void setPersons(int persons) {
    //
  }

  public void setSkills(Map<StringID, Skill> skills) {
    //
  }

  public void setWeight(int weight) {
    //
  }

  public CacheableOrderEditor getOrderEditor() {
    return null;
  }

  public void setOrderEditor(CacheableOrderEditor editor) {
    //
  }

  public List<CoordinateID> getPastMovement(GameData data) {
    return null;
  }

  public boolean isPastMovementPassive() {
    return false;
  }

  public boolean isPastMovementPassive(GameSpecificStuff gameSpecificStuff) {
    return false;
  }

  public UnitID getID() {
    return null;
  }

  public Order createOrder(String order) {
    return null;
  }

  public GameData getData() {
    return null;
  }

  public void clearRelations() {
    //
  }

  public void reparseOrders() {
    //
  }

  public CoordinateID getNewRegion() {
    return null;
  }

  public void setNewRegion(CoordinateID destination) {
    //
  }

  public void detach() {
    //
  }

  public boolean isDetailsKnown() {
    return false;
  }

  @Override
  public Object clone() throws CloneNotSupportedException {
    throw new CloneNotSupportedException();
  }

  public void enter(UnitContainer newUC) {
    //
  }
}
