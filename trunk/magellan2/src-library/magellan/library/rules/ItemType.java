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

import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;

import magellan.library.ID;
import magellan.library.Item;
import magellan.library.Skill;
import magellan.library.utils.Umlaut;


/**
 * DOCUMENT-ME
 *
 * @author $Author: $
 * @version $Revision: 326 $
 */
public class ItemType extends ObjectType implements Comparable {
	private float weight = 0;
	private String iconName = null;
	private Skill makeSkill = null;
	private Skill useSkill = null;
	private ItemCategory category = null;
	private Map<ID,Item> resources = null;

	/**
	 * Creates a new ItemType object.
	 *
	 * 
	 */
	public ItemType(ID id) {
		super(id);
	}

	/**
	 * DOCUMENT-ME
	 *
	 * 
	 */
	public void setWeight(float w) {
		weight = w;
	}

	/**
	 * DOCUMENT-ME
	 *
	 * 
	 */
	public float getWeight() {
		return weight;
	}

	/**
	 * DOCUMENT-ME
	 *
	 * 
	 */
	public void setMakeSkill(Skill s) {
		makeSkill = s;
	}

	/**
	 * DOCUMENT-ME
	 *
	 * 
	 */
	public Skill getMakeSkill() {
		return makeSkill;
	}

	/**
	 * DOCUMENT-ME
	 *
	 * 
	 */
	public void setUseSkill(Skill s) {
		useSkill = s;
	}

	/**
	 * DOCUMENT-ME
	 *
	 * 
	 */
	public Skill getUseSkill() {
		return useSkill;
	}

	/**
	 * DOCUMENT-ME
	 *
	 * 
	 */
	public void setCategory(ItemCategory c) {
		this.category = c;

		if(c != null) {
			c.addInstance(this);
		}
	}

	/**
	 * DOCUMENT-ME
	 *
	 * 
	 */
	public ItemCategory getCategory() {
		return this.category;
	}

	/**
	 * DOCUMENT-ME
	 *
	 * 
	 */
	public void addResource(Item i) {
		if(resources == null) {
			resources = new Hashtable<ID, Item>();
		}

		resources.put(i.getItemType().getID(), i);
	}

	/**
	 * DOCUMENT-ME
	 *
	 * 
	 */
	public Iterator<Item> getResources() {
    if (resources == null) return null;
    return resources.values().iterator();
	}

	/**
	 * DOCUMENT-ME
	 *
	 * 
	 *
	 * 
	 */
	public Item getResource(ID id) {
		if(resources != null) {
			return (Item) resources.get(id);
		} else {
			return null;
		}
	}

	/**
	 * Returns the file name of the icon to use for this item.
	 *
	 * 
	 */
	public String getIconName() {
		if(!iconNameEvaluated && (iconName == null)) {
			if(category != null) {
				iconName = category.getIconName();
			}

			if(iconName == null) {
				iconName = getID().toString();
			}

			iconName = Umlaut.convertUmlauts(iconName.toLowerCase());
			iconNameEvaluated = true;
		}

		return iconName;
	}

	private boolean iconNameEvaluated = false;

	/**
	 * Sets the file name of the icon to use for this item.
	 *
	 * 
	 */
	public void setIconName(String iName) {
		iconName = iName;
		iconNameEvaluated = false;
	}
	public int compareTo(Object o){
		ItemType cmpItemType = (ItemType)o;
		return this.getName().compareTo(cmpItemType.getName());
	}
}
