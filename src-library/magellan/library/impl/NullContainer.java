// class magellan.library.impl.NullContainer
// created on Jan 28, 2020
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
import java.util.Map;

import magellan.library.GameData;
import magellan.library.ID;
import magellan.library.Item;
import magellan.library.Unit;
import magellan.library.UnitContainer;
import magellan.library.relation.UnitRelation;
import magellan.library.rules.UnitContainerType;
import magellan.library.utils.Cache;
import magellan.library.utils.CacheHandler;
import magellan.library.utils.guiwrapper.CacheableOrderEditor;

public class NullContainer implements UnitContainer {

  public void addRelation(UnitRelation rel) {
    //
  }

  public UnitRelation removeRelation(UnitRelation rel) {
    return null;
  }

  public void clearRelations() {
    //
  }

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

  public String getModifiedName() {
    return null;
  }

  public ID getID() {
    return null;
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

  public Item addItem(Item i) {
    return null;
  }

  public Collection<Item> getItems() {
    return null;
  }

  public void setType(UnitContainerType t) {
    //
  }

  public GameData getData() {
    return null;
  }

  public UnitContainerType getType() {
    return null;
  }

  public void setSortIndex(int index) {
    //
  }

  public int getSortIndex() {
    return 0;
  }

  public Collection<Unit> units() {
    return null;
  }

  public Map<? extends ID, Unit> getUnits() {
    return null;
  }

  public Unit getUnit(ID key) {
    return null;
  }

  public void addUnit(Unit u) {
    //
  }

  public Unit removeUnit(ID key) {
    return null;
  }

  public Collection<Unit> modifiedUnits() {
    return null;
  }

  public Unit getModifiedUnit(ID key) {
    return null;
  }

  public Unit getOwner() {
    return null;
  }

  public void setOwner(Unit owner) {
    //
  }

  public void setOwnerUnit(Unit unit) {
    //
  }

  public Unit getOwnerUnit() {
    return null;
  }

  public Unit getModifiedOwnerUnit() {
    return null;
  }

  public void setModifiedOwnerUnit(Unit newOwner) {
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

  public CacheableOrderEditor getOrderEditor() {
    return null;
  }

  public void setOrderEditor(CacheableOrderEditor editor) {
    //
  }

  public boolean leave(Unit unit) {
    return false;
  }

  public void enter(Unit unit) {
    //
  }

  @Override
  public Object clone() throws CloneNotSupportedException {
    throw new CloneNotSupportedException();
  }
}
