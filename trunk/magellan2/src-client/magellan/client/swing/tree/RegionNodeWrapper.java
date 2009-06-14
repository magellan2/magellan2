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

import java.awt.Image;
import java.util.Collections;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.swing.Icon;

import magellan.library.Region;
import magellan.library.utils.StringFactory;


/**
 * DOCUMENT-ME
 *
 * @author $Author: $
 * @version $Revision: 171 $
 */
public class RegionNodeWrapper extends EmphasizingImpl implements CellObject2, SupportsClipboard, SupportsEmphasizing {
	private Region region = null;
	private List<GraphicsElement> GEs = null;
	private int amount = Integer.MIN_VALUE;

	/**
	 * Creates a new RegionNodeWrapper object.
	 *
	 * 
	 */
	public RegionNodeWrapper(Region r) {
		this(r, Integer.MIN_VALUE);
	}

	/**
	 * Creates a new RegionNodeWrapper object.
	 *
	 * 
	 * 
	 */
	public RegionNodeWrapper(Region r, int amount) {
		this.region = r;
		this.amount = amount;
	}

	/**
	 * DOCUMENT-ME
	 *
	 * 
	 */
	public Region getRegion() {
		return region;
	}

	/**
	 * DOCUMENT-ME
	 *
	 * 
	 */
	public void setAmount(int amount) {
		this.amount = amount;
	}

	/**
	 * DOCUMENT-ME
	 *
	 * 
	 */
	public int getAmount() {
		return this.amount;
	}

	/**
	 * DOCUMENT-ME
	 *
	 * 
	 */
	@Override
  public String toString() {
		return (amount > Integer.MIN_VALUE) ? (region.toString() + ": " + amount) : region.toString();
	}

	// pavkovic 2003.10.01: prevent multiple Lists to be generated for nearly static code
	private static Map<Object,List<String>> iconNamesLists = new Hashtable<Object, List<String>>();

	/**
	 * DOCUMENT-ME
	 *
	 * 
	 */
	public List<String> getIconNames() {
		Object key = region.getType().getID();
		List<String> iconNames = RegionNodeWrapper.iconNamesLists.get(key);

		if(iconNames == null) {
			// in this situation init the region
			region.refreshUnitRelations();

			iconNames = Collections.singletonList(StringFactory.getFactory().intern(key.toString()));
			RegionNodeWrapper.iconNamesLists.put(key, iconNames);
		}

		return iconNames;
	}

	/**
	 * DOCUMENT-ME
	 */
	public void propertiesChanged() {
	}

	/**
	 * DOCUMENT-ME
	 *
	 * 
	 */
	public List<GraphicsElement> getGraphicsElements() {
		if(GEs == null) {
			// in this situation init the region
			region.refreshUnitRelations();

			GraphicsElement ge = new RegionGraphicsElement(toString(), null, null,
														   region.getType().getID().toString());
			ge.setTooltip(region.getType().getName());
			ge.setType(GraphicsElement.MAIN);

			GEs = Collections.singletonList(ge);
		}

		return GEs;
	}

	/**
	 * DOCUMENT-ME
	 *
	 * 
	 */
	public boolean reverseOrder() {
		return false;
	}

	protected class RegionGraphicsElement extends GraphicsElement {
		/**
		 * Creates a new RegionGraphicsElement object.
		 *
		 * 
		 * 
		 * 
		 * 
		 */
		public RegionGraphicsElement(Object o, Icon i, Image im, String s) {
			super(o, i, im, s);
			setType(GraphicsElement.MAIN);
		}

		/**
		 * DOCUMENT-ME
		 *
		 * 
		 */
		@Override
    public boolean isEmphasized() {
			return emphasized();
		}
	}

	/**
	 * DOCUMENT-ME
	 *
	 * 
	 */
	public String getClipboardValue() {
		if(region != null) {
			return region.toString();
		} else {
			return toString();
		}
	}

	/**
	 * DOCUMENT-ME
	 *
	 * 
	 * 
	 *
	 * 
	 */
	public NodeWrapperDrawPolicy init(Properties settings, NodeWrapperDrawPolicy adapter) {
		return null;
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
		return null;
	}
}
