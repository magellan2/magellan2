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

/*
 * ContextChangeable.java
 *
 * Created on 1. März 2002, 15:32
 */
package magellan.client.swing.context;

import javax.swing.JMenuItem;

/**
 * DOCUMENT ME!
 *
 * @author Andreas
 * @version 1.0
 */
public interface ContextChangeable {
	/**
	 * DOCUMENT-ME
	 *
	 * 
	 */
	public JMenuItem getContextAdapter();

	/**
	 * DOCUMENT-ME
	 *
	 * 
	 */
	public void setContextObserver(ContextObserver co);
}
