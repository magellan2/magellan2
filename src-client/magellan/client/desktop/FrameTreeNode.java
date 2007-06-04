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

package magellan.client.desktop;

import java.beans.PropertyChangeListener;
import java.io.PrintWriter;
import java.math.BigDecimal;

import javax.swing.JSplitPane;

/**
 * DOCUMENT ME!
 *
 * @author Andreas
 * @version
 */
public class FrameTreeNode implements PropertyChangeListener {
	/** Holds value of property leaf. */
	private boolean leaf;

	/** Holds value of property name. */
	private String name;

	/** Holds value of property child. */
	private FrameTreeNode child[];

	/** Holds value of property percentage. */
	private double percentage;

	/** Holds value of property orientation. */
	private int orientation;

	/** Holds value of property absolute. */
	private boolean absolute;
	private JSplitPane splitPane;

	/** Holds value of property configuration. */
	private String configuration;

	/**
	 * Creates new FrameTreeNode
	 */
	public FrameTreeNode() {
		setLeaf(false);
	}

	/**
	 * Getter for property leaf.
	 *
	 * @return Value of property leaf.
	 */
	public boolean isLeaf() {
		return leaf;
	}

	/**
	 * Setter for property leaf.
	 *
	 * @param leaf New value of property leaf.
	 */
	public void setLeaf(boolean leaf) {
		this.leaf = leaf;

		if(!leaf) {
			child = new FrameTreeNode[2];
		}
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
	 * Indexed getter for property child.
	 *
	 * @param index Index of the property.
	 *
	 * @return Value of the property at <CODE>index</CODE>.
	 */
	public FrameTreeNode getChild(int index) {
		return child[index];
	}

	/**
	 * Indexed setter for property child.
	 *
	 * @param index Index of the property.
	 * @param child New value of the property at <CODE>index</CODE>.
	 */
	public void setChild(int index, FrameTreeNode child) {
		this.child[index] = child;
	}

	/**
	 * Getter for property percentage.
	 *
	 * @return Value of property percentage.
	 */
	public double getPercentage() {
		return percentage;
	}

	/**
	 * Setter for property percentage.
	 *
	 * @param percentage New value of property percentage.
	 */
	public void setPercentage(double percentage) {
		this.percentage = percentage;
	}

	/**
	 * Getter for property orientation.
	 *
	 * @return Value of property orientation.
	 */
	public int getOrientation() {
		return orientation;
	}

	/**
	 * Setter for property orientation.
	 *
	 * @param orientation New value of property orientation.
	 */
	public void setOrientation(int orientation) {
		this.orientation = orientation;
	}

	/**
	 * Getter for property absolute.
	 *
	 * @return Value of property absolute.
	 */
	public boolean isAbsolute() {
		return absolute;
	}

	/**
	 * Setter for property absolute.
	 *
	 * @param absolute New value of property absolute.
	 */
	public void setAbsolute(boolean absolute) {
		this.absolute = absolute;
	}

	/**
	 * DOCUMENT-ME
	 *
	 * 
	 */
	public void write(PrintWriter out) {
		if(isLeaf()) {
			if(getConfiguration() != null) {
				out.println("COMPONENT " + getName() + " " + getConfiguration());
			} else {
				out.println("COMPONENT " + getName());
			}
		} else {
			out.println("SPLIT " + getPercentage() + ' ' +
						((getOrientation() == javax.swing.JSplitPane.HORIZONTAL_SPLIT) ? 'H' : 'V'));

			if(getChild(0) != null) {
				getChild(0).write(out);
			}

			if(getChild(1) != null) {
				getChild(1).write(out);
			}

			out.println("/SPLIT");
		}
	}

	/**
	 * DOCUMENT-ME
	 *
	 * 
	 */
	public void connectToSplitPane(JSplitPane pane) {
		if(splitPane != null) {
			splitPane.removePropertyChangeListener(JSplitPane.DIVIDER_LOCATION_PROPERTY, this);
		}

		splitPane = pane;

		if(splitPane != null) {
			splitPane.addPropertyChangeListener(JSplitPane.DIVIDER_LOCATION_PROPERTY, this);
		}
	}

	/**
	 * DOCUMENT-ME
	 */
	public void refreshPercentage() {
		if(isLeaf() || (splitPane == null)) {
			return;
		}

		// pavkovic 2003.06.04: translate absolute values to relative values
		double fullsize = (double) ((splitPane.getOrientation() == JSplitPane.VERTICAL_SPLIT)
									? splitPane.getHeight()
									: (splitPane.getWidth() - splitPane.getDividerSize()));

		if(isAbsolute()) {
			setAbsolute(false);
		}

		double location = (double) splitPane.getDividerLocation();

		// round half up (sign(value)*floor(|value|+0.5f)), 2 digits after . (/10^2) 
		setPercentage(new BigDecimal(location).divide(new BigDecimal(fullsize), 2,
													  BigDecimal.ROUND_HALF_UP).doubleValue());

		/*if (!isLeaf())
		            for(int i=0;i<2;i++)
		                getChild(i).refreshPercentage();*/
	}

	/**
	 * DOCUMENT-ME
	 *
	 * 
	 */
	public void propertyChange(java.beans.PropertyChangeEvent p1) {
		refreshPercentage();
	}

	/**
	 * Getter for property configuration. null means no configurable object/no configurations.
	 *
	 * @return Value of property configuration.
	 */
	public String getConfiguration() {
		return configuration;
	}

	/**
	 * Setter for property configuration.
	 *
	 * @param configuration New value of property configuration.
	 */
	public void setConfiguration(String configuration) {
		this.configuration = configuration;
	}
}
