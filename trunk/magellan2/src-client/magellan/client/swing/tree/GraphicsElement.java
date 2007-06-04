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

import javax.swing.Icon;

/**
 * DOCUMENT-ME
 *
 * @author $Author: $
 * @version $Revision: 171 $
 */
public class GraphicsElement {
	// Icon related things

	/** Holds an icon if provided. Will be asked for at first from renderer. */
	private Icon icon = null;

	/** Stores an image to create an icon from. Is asked secondly if no icon is provided. */
	private Image image = null;

	/**
	 * A name for the image that could be used for an Image icon. Will be used by the renderer if
	 * neither icon nor image are provided.
	 */
	private String imageName = null;

	// Text display things

	/** The object that should be displayed. If not null, the text of toString() will be used. */
	private Object object = null;

	/** A styleset to use for display. */
	private String styleset = null;

	/** States if this element should be displayed emphasized. */
	private boolean emphasized = false;

	/** A tooltip to display. */
	private String tooltip;

	/** Type of this element. See type constants. Default is SIMPLE. */
	private int type = SIMPLE;

	/** DOCUMENT-ME */
	public static final int SIMPLE = 0;

	/** DOCUMENT-ME */
	public static final int MAIN = 1;

	/** DOCUMENT-ME */
	public static final int ADDITIONAL = 2;

	// Single use: Icon(/Image/ImageName) / Object
	public GraphicsElement(Icon icon, Image image, String imageName) {
		this.icon = icon;
		this.image = image;
		this.imageName = imageName;
	}

	/**
	 * Creates a new GraphicsElement object.
	 *
	 * 
	 */
	public GraphicsElement(Object object) {
		this.object = object;
	}

	// Combined use: Icon + Object
	public GraphicsElement(Object object, Icon icon, Image image, String imageName) {
		this.object = object;
		this.icon = icon;
		this.image = image;
		this.imageName = imageName;
	}

	/**
	 * Getter for property icon.
	 *
	 * @return Value of property icon.
	 */
	public Icon getIcon() {
		return icon;
	}

	/**
	 * Setter for property icon.
	 *
	 * @param icon New value of property icon.
	 */
	public void setIcon(Icon icon) {
		this.icon = icon;
	}

	/**
	 * Getter for property image.
	 *
	 * @return Value of property image.
	 */
	public Image getImage() {
		return image;
	}

	/**
	 * Setter for property image.
	 *
	 * @param image New value of property image.
	 */
	public void setImage(Image image) {
		this.image = image;
	}

	/**
	 * Getter for property imageName.
	 *
	 * @return Value of property imageName.
	 */
	public String getImageName() {
		return imageName;
	}

	/**
	 * Setter for property imageName.
	 *
	 * @param imageName New value of property imageName.
	 */
	public void setImageName(String imageName) {
		this.imageName = imageName;
	}

	/**
	 * Getter for property object.
	 *
	 * @return Value of property object.
	 */
	public Object getObject() {
		return object;
	}

	/**
	 * Setter for property object.
	 *
	 * @param object New value of property object.
	 */
	public void setObject(Object object) {
		this.object = object;
	}

	/**
	 * Getter for property styleset.
	 *
	 * @return Value of property styleset.
	 */
	public String getStyleset() {
		return styleset;
	}

	/**
	 * Setter for property styleset.
	 *
	 * @param styleset New value of property styleset.
	 */
	public void setStyleset(String styleset) {
		this.styleset = styleset;
	}

	/**
	 * DOCUMENT-ME
	 *
	 * 
	 */
	public boolean hasStyleset() {
		return styleset != null;
	}

	/**
	 * Getter for property emphasized.
	 *
	 * @return Value of property emphasized.
	 */
	public boolean isEmphasized() {
		return emphasized;
	}

	/**
	 * Setter for property emphasized.
	 *
	 * @param emphasized New value of property emphasized.
	 */
	public void setEmphasized(boolean emphasized) {
		this.emphasized = emphasized;
	}

	/**
	 * Getter for property tooltip.
	 *
	 * @return Value of property tooltip.
	 */
	public String getTooltip() {
		return tooltip;
	}

	/**
	 * Setter for property tooltip.
	 *
	 * @param tooltip New value of property tooltip.
	 */
	public void setTooltip(String tooltip) {
		this.tooltip = tooltip;
	}

	/**
	 * Returns the type of this element.
	 *
	 * 
	 */
	public int getType() {
		return type;
	}

	/**
	 * Sets the type of this element. Default is SIMPLE.
	 *
	 * 
	 */
	public void setType(int type) {
		this.type = type;
	}
}
