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

package magellan.client.swing.ui;

import java.awt.Component;

import javax.swing.BorderFactory;
import javax.swing.JSplitPane;

/**
 * DOCUMENT-ME
 *
 * @author $Author: $
 * @version $Revision: 171 $
 */
public class UIFactory {
	/*
	 * This class is a helper class to consistently create different gui elements like JScrollPane, JSplitPane etc.
	 */
	public static JSplitPane createBorderlessJSplitPane(int orientation) {
		JSplitPane ret = new UISplitPane(orientation);

		// JSplitPane ret = new JSplitPane(orientation);
		ret.setBorder(BorderFactory.createEmptyBorder());

		return ret;
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
	public static JSplitPane createBorderlessJSplitPane(int orientation, Component first,
														Component second) {
		JSplitPane ret = UIFactory.createBorderlessJSplitPane(orientation);
		ret.setTopComponent(first);
		ret.setBottomComponent(second);

		return ret;
	}
}
