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

package magellan.client.swing.desktop;

import java.awt.BorderLayout;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSplitPane;

import magellan.client.swing.ui.InternalFrame;


/**
 * DOCUMENT-ME
 *
 * @author $Author: $
 * @version $Revision: 271 $
 */
public class EmptyPerspective implements Perspective {
	/*
	 * A Perspective holds informations about the desktop view
	 */
	public JPanel getJPanel() {
		int border = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT).getDividerSize();
		JPanel ret = new JPanel();
		ret.setBorder(BorderFactory.createEmptyBorder(border, border, border, border));

		ret.setLayout(new BorderLayout());
		ret.add(new JLabel("This is an empty perspective"), BorderLayout.NORTH);

		InternalFrame ifs = new InternalFrame("Empty perspective");
		ifs.setContent(ret);

		return ifs;
	}
}
