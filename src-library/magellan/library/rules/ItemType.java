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

package magellan.library.rules;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

import magellan.library.Item;
import magellan.library.Skill;
import magellan.library.StringID;
import magellan.library.utils.Umlaut;

/**
 * Stores rule relevant info about types of items, like "Holz".
 */
public class ItemType extends ObjectType {
  private float weight = 0;
  private String iconName = null;
  private Skill makeSkill = null;
  private Skill useSkill = null;
  private ItemCategory category = null;
  private Map<StringID, Item> resources = null;
  private boolean storableInBonw = false;
  private String icon;

  /**
   * Creates a new ItemType object.
   */
  public ItemType(StringID id) {
    super(id);
  }

  /**
   * Sets the items weight in GE.
   */
  public void setWeight(float w) {
    weight = w;
  }

  /**
   * Returns the items weight in GE.
   */
  public float getWeight() {
    return weight;
  }

  /**
   * Sets the skill needed to MAKE this item.
   */
  public void setMakeSkill(Skill s) {
    makeSkill = s;
  }

  /**
   * Returns the skill needed to MAKE this item.
   */
  public Skill getMakeSkill() {
    return makeSkill;
  }

  /**
   * Sets the skill needed to use this item (e.g. a weapon skill).
   */
  public void setUseSkill(Skill s) {
    useSkill = s;
  }

  /**
   * Returns the skill needed to use this item (e.g. a weapon skill).
   */
  public Skill getUseSkill() {
    return useSkill;
  }

  /**
   * Sets the items category.
   */
  public void setCategory(ItemCategory c) {
    category = c;

    if (c != null) {
      c.addInstance(this);
    }
  }

  /**
   * Returns the items category. May return <code>null</code>.
   */
  public ItemCategory getCategory() {
    return category;
  }

  /**
   * Adds an item to the set of resources needed to make this ItemType.
   */
  public void addResource(Item i) {
    if (resources == null) {
      resources = new LinkedHashMap<StringID, Item>();
    }

    resources.put(i.getItemType().getID(), i);
  }

  /**
   * Returns the set of resources needed to MAKE this ItemType or <code>null</code> if there are
   * none.
   */
  public Iterator<Item> getResources() {
    if (resources == null)
      return null;
    return resources.values().iterator();
  }

  /**
   * Returns the resource of this type belonging to the id or <code>null</code> if the id doesn't
   * belong to the resources.
   */
  public Item getResource(StringID id) {
    if (resources != null)
      return resources.get(id);
    else
      return null;
  }

  /**
   * Returns the file name of the icon to use for this item.
   */
  @Override
  public String getIcon() {
    if (!iconNameEvaluated && (iconName == null)) {
      if (category != null) {
        iconName = category.getIconName();
      }

      if (iconName == null) {
        iconName = getID().toString();
      }

      iconName = Umlaut.convertUmlauts(iconName.toLowerCase());
      iconNameEvaluated = true;
    }

    return iconName;
  }

  private boolean iconNameEvaluated = false;

  private short isHorse = -1;

  /**
   * Sets the file name of the icon to use for this item.
   */
  @Override
  public void setIcon(String iName) {
    super.setIcon(iName);
    iconName = iName;
    iconNameEvaluated = false;
  }

  /**
   * @return name quoted if required
   */

  public String getOrderName() {
    if ((getName().indexOf(" ") > -1))
      return getName().replace(' ', '~');
    else
      return getName();
  }

  /**
   * store the attribute if the item can be stored in a bag of negative weight
   */
  public void setStoreableInBonw(int bonw) {
    storableInBonw = bonw > 0;
  }

  /**
   * @return the status if an item can be stored in a bag of negative weight
   */
  public boolean isStoreableInBonw() {
    return storableInBonw;
  }

  /**
   * Compares this type to another type by name.
   * 
   * @see magellan.library.impl.MagellanIdentifiableImpl#compareTo(java.lang.Object)
   */
  @Override
  public int compareTo(Object o) {
    ItemType cmpItemType = (ItemType) o;
    return getName().compareTo(cmpItemType.getName());
  }

  /**
   * Returns the id uniquely identifying this object.
   */
  @Override
  public StringID getID() {
    return (StringID) id;
  }

  /**
   * Returns true if this type is a horse.
   */
  public boolean isHorse() {
    return isHorse > 0;
  }

  /**
   * Sets the horse property.
   */
  public void setHorse(short horse) {
    isHorse = horse;
  }
}
