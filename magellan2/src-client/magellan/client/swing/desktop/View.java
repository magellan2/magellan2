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

import java.awt.Image;

import javax.swing.JPanel;

/**
 * This object represents a kind of view. Views are a JPanel together with a title and an
 * identifier
 */
public interface View {
	/**
	 * Returns the (localized) name of the View
	 *
	 * @return name of the view
	 */
	public String getName();

	/**
	 * Returns the image associated with the view 
	 * 
	 * @return image of te view
	 */
	public Image getImage();
	
	/**
	 * Returns the unique identifier name of the View
	 *
	 * 
	 */
	//public String getIdentifier();

	/**
	 * returns a Panel of this view
	 *
	 * 
	 */
	public JPanel getPanel();
}
