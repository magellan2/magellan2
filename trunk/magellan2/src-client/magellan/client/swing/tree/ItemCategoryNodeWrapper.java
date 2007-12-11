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

package magellan.client.swing.tree;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import magellan.library.rules.ItemCategory;


/**
 * DOCUMENT ME!
 *
 * @author Ulrich Küster
 */
public class ItemCategoryNodeWrapper implements CellObject{
	private int amount = -1;
	private ItemCategory cat = null;
	private String setCatName = null;
	protected List<String> icons;
	protected List<String> returnIcons;
	
	protected DetailsNodeWrapperDrawPolicy adapter;
	
	/**
	 * Creates a new ItemCategoryNodeWrapper object.
	 *
	 * 
	 * 
	 */
	public ItemCategoryNodeWrapper(ItemCategory category, int amount) {
		this.amount = amount;
		this.cat = category;
	}
	public ItemCategoryNodeWrapper(ItemCategory category, int amount, String _catName) {
		this.amount = amount;
		this.cat = category;
		this.setCatName = _catName;
	}
	

	/**
	 * DOCUMENT-ME
	 *
	 * 
	 */
	public void setAmount(int i) {
		amount = i;
	}

	/**
	 * DOCUMENT-ME
	 *
	 * 
	 */
	public ItemCategory getItemCategory() {
		return cat;
	}

	/**
	 * DOCUMENT-ME
	 *
	 * 
	 */
	public String toString() {
		if(amount == -1) {
			if (this.setCatName==null) {
				return cat.toString();
			} else {
				return this.setCatName;
			}
		} else {
			if (this.setCatName==null) {
				return cat.toString() + ": " + amount;
			} else {
				return this.setCatName + ": " + amount;
			}
		}
	}
	public NodeWrapperDrawPolicy init(Properties settings, NodeWrapperDrawPolicy adapter) {
		return init(settings, "SimpleNodeWrapper", adapter);
	}

	/**
	 * DOCUMENT-ME
	 *
	 * 
	 * 
	 * 
	 *
	 * 
	 */
	public NodeWrapperDrawPolicy init(Properties settings, String prefix,
									  NodeWrapperDrawPolicy adapter) {
		if(adapter == null) {
			adapter = createSimpleDrawPolicy(settings, prefix);
		}

		adapter.addCellObject(this);
		this.adapter = (DetailsNodeWrapperDrawPolicy) adapter;

		return adapter;
	}
	protected NodeWrapperDrawPolicy createSimpleDrawPolicy(Properties settings, String prefix) {
		return new DetailsNodeWrapperDrawPolicy(1, null, settings, prefix, new String[][] {
													{ "simple.showIcon", "true" }}, new String[] { "icons.text" }, 0, "tree.itemcategorynodewrapper.");
	}
	
	public void setIcons(Object icons) {
		this.icons = null;
        if(icons != null) {
            if(icons instanceof Collection) {
                this.icons = new ArrayList<String>((Collection<String>) icons);
            } else if(icons instanceof Map) {
                Map m = (Map) icons;

                this.icons = new ArrayList<String>(m.size());

                for(Iterator iter = m.values().iterator(); iter.hasNext();) {
                    this.icons.add(iter.next().toString());
                }
            } else {
                this.icons = Collections.singletonList(icons.toString());
            }
        }
	}
	public boolean emphasized() {
		return false;
	}
	public void propertiesChanged() {
		returnIcons = null;
	}
	public List getIconNames() {
		if(returnIcons == null) {
			
				returnIcons = icons;
			
		}

		return returnIcons;
	}
	
}
