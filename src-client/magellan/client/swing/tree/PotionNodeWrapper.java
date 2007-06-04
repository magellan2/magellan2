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

import java.util.List;
import java.util.Properties;

import magellan.library.Potion;


/**
 * DOCUMENT-ME
 *
 * @author $Author: $
 * @version $Revision: 259 $
 */
public class PotionNodeWrapper implements CellObject, SupportsClipboard {
	private Potion potion = null;
    private String name = null;
    private String postfix = null;

	/**
	 * Creates a new PotionNodeWrapper object.
	 *
	 * 
	 */
	public PotionNodeWrapper(Potion p) {
		this(p, null);
	}

	/**
	 * Creates a new PotionNodeWrapper object.
	 *
	 * 
	 * 
	 */
	public PotionNodeWrapper(Potion p, String postfix) {
        this(p, p.getName(),postfix);
    }
    public PotionNodeWrapper(Potion p, String name, String postfix) {
        this.potion = p;
        this.name = name;
        this.postfix = postfix;
	}

	/**
	 * DOCUMENT-ME
	 *
	 * 
	 */
	public Potion getPotion() {
		return potion;
	}

	/**
	 * DOCUMENT-ME
	 *
	 * 
	 */
	public String toString() {
	    return postfix == null ? name : (name+postfix);	}

	/**
	 * DOCUMENT-ME
	 *
	 * 
	 */
	public List getIconNames() {
		return null;
	}

	/**
	 * DOCUMENT-ME
	 *
	 * 
	 */
	public boolean emphasized() {
		return false;
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
	public String getClipboardValue() {
		if(potion != null) {
			return potion.getName();
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
