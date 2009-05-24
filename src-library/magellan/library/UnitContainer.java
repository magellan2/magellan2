/*
 *  Copyright (C) 2000-2004 Roger Butenuth, Andreas Gampe,
 *                          Stefan Goetz, Sebastian Pappert,
 *                          Klaas Prause, Enno Rehling,
 *                          Sebastian Tusk, Ulrich Kuester,
 *                          Ilja Pavkovic
 *
 * This file is part of the Eressea Java Code Base, see the
 * file LICENSING for the licensing information applying to
 * this file.
 *
 */

package magellan.library;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import magellan.library.relation.UnitRelation;
import magellan.library.rules.UnitContainerType;
import magellan.library.utils.Sorted;
import magellan.library.utils.Taggable;
import magellan.library.utils.guiwrapper.CacheableOrderEditor;

/**
 * DOCUMENT-ME
 * 
 * @author $Author: $
 * @version $Revision: 389 $
 */
public interface UnitContainer extends Related, Sorted, Taggable, HasCache {

  /**
   * Adds an item to the unitcontainer. If the unitcontainer already has an item
   * of the same type, the item is overwritten with the specified item object.
   * 
   * @return the specified item i.
   */
  public Item addItem(Item i);

  /**
   * Returns all the items this unit possesses.
   * 
   * @return a collection of Item objects.
   */
  public Collection<Item> getItems();

  /**
   * DOCUMENT-ME
   * 
   * @throws IllegalArgumentException
   *           DOCUMENT-ME
   */
  public void setType(UnitContainerType t);

  /**
   * Returns the associated GameData
   */
  public GameData getData();

  /**
   * returns the type of the UnitContainer
   */
  public UnitContainerType getType();

  /**
   * Sets an index indicating how instances of class are sorted in the report.
   */
  public void setSortIndex(int index);

  /**
   * Returns an index indicating how instances of class are sorted in the
   * report.
   */
  public int getSortIndex();

  /**
   * Returns an unmodifiable collection of all the units in this container.
   */
  public Collection<Unit> units();

  /**
   * Retrieve a unit in this container by id.
   */
  public Unit getUnit(ID key);

  /**
   * Adds a unit to this container. This method should only be invoked by
   * Unit.setXXX() methods.
   */
  void addUnit(Unit u);

  /**
   * Removes a unit from this container. This method should only be invoked by
   * Unit.setXXX() methods.
   */
  Unit removeUnit(ID key);

  /**
   * DOCUMENT-ME
   */
  public Collection<Unit> modifiedUnits();

  /**
   * DOCUMENT-ME
   */
  public Unit getModifiedUnit(ID key);

  /**
   * DOCUMENT-ME
   */
  public String toString();
  
  /**
   * Returns value of the owner tag of the UnitContainer or <code>null</code>.
   * Most callers should use {@link #getOwnerUnit()} instead!
   * 
   * @return The current owner of the UnitContainer or <code>null</code>.
   */
  public Unit getOwner();

  
  /**
   * Set the value of the owner tag. 
   * 
   * @param owner
   */
  public void setOwner(Unit owner);

  /**
   * Sets the unit owning this UnitContainer. You should probably use {@link #setOwner(Unit)} instead!
   *
   * @param unit
   */
  public void setOwnerUnit(Unit unit);

  /**
   * Returns the unit owning this UnitContainer. If this UnitContainer is an
   * instance of class Ship or Building the normal owning unit is returned (or
   * null, if there is none). In case of a Region, the OwnerUnit of the largest
   * castle is returned. In case of a Faction, null is returned.
   */
  public Unit getOwnerUnit();

  /**
   * DOCUMENT-ME
   */
  public void addRelation(UnitRelation rel);

  /**
   * @see magellan.library.Named#getModifiedName()
   */
  public String getModifiedName();
  
  /**
   * DOCUMENT-ME
   */
  public UnitRelation removeRelation(UnitRelation rel);
  
  /**
   * EXTERNAL TAG METHODS DOCUMENT-ME
   * 
   * @see magellan.library.utils.Taggable#deleteAllTags()
   */
  public void deleteAllTags();

  /**
   * DOCUMENT-ME
   */
  public String putTag(String tag, String value);
  /**
   * DOCUMENT-ME
   */
  public String getTag(String tag);

  /**
   * DOCUMENT-ME
   */
  public String removeTag(String tag);

  /**
   * DOCUMENT-ME
   */
  public boolean containsTag(String tag);
  /**
   * DOCUMENT-ME
   */
  public Map<String,String> getTagMap();
  /**
   * DOCUMENT-ME
   */
  public boolean hasTags();

  /**
   * Returns the value of comments.
   * 
   * @return Returns comments.
   */
  public List<String> getComments();

  /**
   * Sets the value of comments.
   *
   * @param comments The value for comments.
   */
  public void setComments(List<String> comments) ;

  /**
   * Returns the value of effects.
   * 
   * @return Returns effects.
   */
  public List<String> getEffects();

  /**
   * Sets the value of effects.
   *
   * @param effects The value for effects.
   */
  public void setEffects(List<String> effects);

  
  /**
   * Always returns <code>null</code>. UnitContainers do not have order editors.
   * 
   * @return
   */
  public CacheableOrderEditor getOrderEditor();

  public void setOrderEditor(CacheableOrderEditor editor);

}