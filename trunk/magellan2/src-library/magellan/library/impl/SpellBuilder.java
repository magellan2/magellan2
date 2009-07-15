// class magellan.library.impl.SpellBuilder
// created on Jul 16, 2009
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
package magellan.library.impl;

import java.util.Locale;
import java.util.Map;

import magellan.library.GameData;
import magellan.library.IntegerID;
import magellan.library.Spell;
import magellan.library.StringID;

/**
 * Spells are identified by their name in the CR, not by their integer ID. This class is for
 * building a string before its name is known. After a name is set, the real {@link Spell} object
 * can be constructed using the {@link #construct()} method.
 * 
 * @author stm
 */
public class SpellBuilder {

  MagellanSpellImpl spell;
  private IntegerID id;
  private GameData data;
  private String name;

  public SpellBuilder(IntegerID id, GameData data) {
    spell = new MagellanSpellImpl(StringID.create("spell not yet constructed"), data);
    this.id = id;
    this.data = data;
  }

  /**
   * Returns the set name or <code>null</code>. If <code>null</code> is returned,
   * {@link #construct()} cannot be called!
   */
  public String getName() {
    return name;
  }

  /**
   * Creates a Spell object after it has been completely constructed
   * 
   * @return A new spell object with {@link #getName()} as id.
   * @throws RuntimeException if no name has been set
   */
  public Spell construct() {
    if (name == null)
      throw new RuntimeException("spell construction incomplete");

    MagellanSpellImpl result = new MagellanSpellImpl(StringID.create(name), data);
    if (spell.getAttributeSize() > 0)
      for (String key : spell.getAttributeKeys()) {
        result.addAttribute(key, spell.getAttribute(key));
      }

    result.setBlockID(id.intValue());
    result.setComponents(spell.getComponents());
    result.setDescription(spell.getDescription());
    result.setIsFamiliar(spell.getIsFamiliar());
    result.setIsFar(spell.getIsFar());
    result.setLevel(spell.getLevel());
    result.setLocale(spell.getLocale());
    result.setName(name);
    result.setOnOcean(spell.getOnOcean());
    result.setOnShip(spell.getOnShip());
    result.setRank(spell.getRank());
    result.setSyntax(spell.getSyntax());
    result.setType(spell.getType());

    return result;
  }

  /**
   * @param key
   * @param value
   * @see magellan.library.impl.MagellanSpellImpl#addAttribute(java.lang.String, java.lang.String)
   */
  public void addAttribute(String key, String value) {
    spell.addAttribute(key, value);
  }

  /**
   * @param id
   * @see magellan.library.impl.MagellanSpellImpl#setBlockID(int)
   */
  public void setBlockID(int id) {
    spell.setBlockID(id);
  }

  /**
   * @param components
   * @see magellan.library.impl.MagellanSpellImpl#setComponents(java.util.Map)
   */
  public void setComponents(Map<String, String> components) {
    spell.setComponents(components);
  }

  /**
   * @param description
   * @see magellan.library.impl.MagellanDescribedImpl#setDescription(java.lang.String)
   */
  public void setDescription(String description) {
    spell.setDescription(description);
  }

  /**
   * @param isFamiliar
   * @see magellan.library.impl.MagellanSpellImpl#setIsFamiliar(boolean)
   */
  public void setIsFamiliar(boolean isFamiliar) {
    spell.setIsFamiliar(isFamiliar);
  }

  /**
   * @param far
   * @see magellan.library.impl.MagellanSpellImpl#setIsFar(boolean)
   */
  public void setIsFar(boolean far) {
    spell.setIsFar(far);
  }

  /**
   * @param level
   * @see magellan.library.impl.MagellanSpellImpl#setLevel(int)
   */
  public void setLevel(int level) {
    spell.setLevel(level);
  }

  /**
   * @param locale
   * @see magellan.library.impl.MagellanSpellImpl#setLocale(java.util.Locale)
   */
  public void setLocale(Locale locale) {
    spell.setLocale(locale);
  }

  /**
   * @param name
   * @see magellan.library.impl.MagellanNamedImpl#setName(java.lang.String)
   */
  public void setName(String name) {
    this.name = name;
    spell.setName(name);
  }

  /**
   * @param onOcean
   * @see magellan.library.impl.MagellanSpellImpl#setOnOcean(boolean)
   */
  public void setOnOcean(boolean onOcean) {
    spell.setOnOcean(onOcean);
  }

  /**
   * @param onShip
   * @see magellan.library.impl.MagellanSpellImpl#setOnShip(boolean)
   */
  public void setOnShip(boolean onShip) {
    spell.setOnShip(onShip);
  }

  /**
   * @param rank
   * @see magellan.library.impl.MagellanSpellImpl#setRank(int)
   */
  public void setRank(int rank) {
    spell.setRank(rank);
  }

  /**
   * @param syntax
   * @see magellan.library.impl.MagellanSpellImpl#setSyntax(java.lang.String)
   */
  public void setSyntax(String syntax) {
    spell.setSyntax(syntax);
  }

  /**
   * @param type
   * @see magellan.library.impl.MagellanSpellImpl#setType(java.lang.String)
   */
  public void setType(String type) {
    spell.setType(type);
  }

}
