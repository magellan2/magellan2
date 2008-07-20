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

import java.awt.Color;
import java.awt.Font;

/**
 * DOCUMENT-ME
 *
 * @author $Author: $
 * @version $Revision: 171 $
 */
public class GraphicsStyleset {
	/** Holds value of property foreground. */
	private Color foreground;

	/** Holds value of property name. */
	private String name;

	/** Holds value of property background. */
	private Color background;

	/** Holds value of property selectedBackground. */
	private Color selectedBackground;

	/** Holds value of property selectedForeground. */
	private Color selectedForeground;

	/** Holds value of property font. */
	private Font font;

	/** Holds value of property horizontalPos. */
	private int horizontalPos = javax.swing.SwingConstants.RIGHT;

	/** Holds value of property verticalPos. */
	private int verticalPos = javax.swing.SwingConstants.CENTER;

	/** Holds value of property parent. */
	private String parent = null;

	/**
	 * Creates new GraphicsStyleset
	 *
	 * 
	 */
	public GraphicsStyleset(String name) {
		this.name = name;
	}

	/**
	 * Getter for property foreground.
	 *
	 * @return Value of property foreground.
	 */
	public Color getForeground() {
		return foreground;
	}

	/**
	 * Setter for property foreground.
	 *
	 * @param foreground New value of property foreground.
	 */
	public void setForeground(Color foreground) {
		this.foreground = foreground;
	}

	/**
	 * Getter for property name.
	 *
	 * @return Value of property name.
	 */
	public String getName() {
		return name;
	}

	/**
	 * Setter for property name.
	 *
	 * @param name New value of property name.
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * Getter for property background.
	 *
	 * @return Value of property background.
	 */
	public Color getBackground() {
		return background;
	}

	/**
	 * Setter for property background.
	 *
	 * @param background New value of property background.
	 */
	public void setBackground(Color background) {
		this.background = background;
	}

	/**
	 * Getter for property selectedBackground.
	 *
	 * @return Value of property selectedBackground.
	 */
	public Color getSelectedBackground() {
		return selectedBackground;
	}

	/**
	 * Setter for property selectedBackground.
	 *
	 * @param selectedBackground New value of property selectedBackground.
	 */
	public void setSelectedBackground(Color selectedBackground) {
		this.selectedBackground = selectedBackground;
	}

	/**
	 * Getter for property selectedForeground.
	 *
	 * @return Value of property selectedForeground.
	 */
	public Color getSelectedForeground() {
		return selectedForeground;
	}

	/**
	 * Setter for property selectedForeground.
	 *
	 * @param selectedForeground New value of property selectedForeground.
	 */
	public void setSelectedForeground(Color selectedForeground) {
		this.selectedForeground = selectedForeground;
	}

	/**
	 * Getter for property font.
	 *
	 * @return Value of property font.
	 */
	public Font getFont() {
		return font;
	}

	/**
	 * Setter for property font.
	 *
	 * @param font New value of property font.
	 */
	public void setFont(Font font) {
		this.font = font;
	}

	/**
	 * Getter for property horizontalPos.
	 *
	 * @return Value of property horizontalPos.
	 */
	public int getHorizontalPos() {
		return horizontalPos;
	}

	/**
	 * Setter for property horizontalPos.
	 *
	 * @param horizontalPos New value of property horizontalPos.
	 */
	public void setHorizontalPos(int horizontalPos) {
		this.horizontalPos = horizontalPos;
	}

	/**
	 * Getter for property verticalPos.
	 *
	 * @return Value of property verticalPos.
	 */
	public int getVerticalPos() {
		return verticalPos;
	}

	/**
	 * Setter for property verticalPos.
	 *
	 * @param verticalPos New value of property verticalPos.
	 */
	public void setVerticalPos(int verticalPos) {
		this.verticalPos = verticalPos;
	}

	/**
	 * DOCUMENT-ME
	 *
	 * 
	 */
	@Override
  public String toString() {
		return getName();
	}

	/**
	 * Getter for property parent.
	 *
	 * @return Value of property parent.
	 */
	public String getParent() {
		if(parent == null) {
			if(name.indexOf('.') > 0) {
				return name.substring(0, name.lastIndexOf('.'));
			}
		}

		return parent;
	}

	/**
	 * Returns true if the value returned by getParent() is extracted out of the name.
	 *
	 * 
	 */
	public boolean isExtractedParent() {
		return parent == null;
	}

	/**
	 * Setter for property parent.
	 *
	 * @param parent New value of property parent.
	 */
	public void setParent(String parent) {
		this.parent = parent;
	}
}
