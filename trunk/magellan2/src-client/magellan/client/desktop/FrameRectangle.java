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

import java.awt.Component;
import java.awt.Frame;
import java.awt.Rectangle;
import java.awt.event.ComponentListener;
import java.awt.event.WindowListener;

/**
 * DOCUMENT ME!
 *
 * @author Andreas
 * @version
 */
public class FrameRectangle extends java.awt.Rectangle implements ComponentListener, WindowListener {
	protected Frame myFrame;
	protected String frameTitle;
	protected String frameComponent;
	protected int state = Frame.NORMAL;
	protected boolean visible = true;

	/** Holds value of property configuration. */
	private String configuration;

	/**
	 * Creates new FrameRectangle
	 *
	 * 
	 * 
	 */
	public FrameRectangle(String ft, String fc) {
		frameTitle = ft;
		frameComponent = fc;
	}

	/**
	 * DOCUMENT-ME
	 *
	 * 
	 */
	public void connectToFrame(Frame f) {
		if(myFrame != null) {
			myFrame.removeComponentListener(this);
			myFrame.removeWindowListener(this);
		}

		myFrame = f;

		if(myFrame != null) {
			myFrame.addComponentListener(this);
			myFrame.addWindowListener(this);
		}
	}

	/**
	 * DOCUMENT-ME
	 *
	 * 
	 */
	public void componentShown(java.awt.event.ComponentEvent p1) {
	}

	/**
	 * DOCUMENT-ME
	 *
	 * 
	 */
	public void componentResized(java.awt.event.ComponentEvent p1) {
		Rectangle rect = ((Component) p1.getSource()).getBounds();
		x = rect.x;
		y = rect.y;
		width = rect.width;
		height = rect.height;
	}

	/**
	 * DOCUMENT-ME
	 *
	 * 
	 */
	public void componentHidden(java.awt.event.ComponentEvent p1) {
	}

	/**
	 * DOCUMENT-ME
	 *
	 * 
	 */
	public void componentMoved(java.awt.event.ComponentEvent p1) {
		Rectangle rect = ((Component) p1.getSource()).getBounds();
		x = rect.x;
		y = rect.y;
		width = rect.width;
		height = rect.height;
	}

	/**
	 * DOCUMENT-ME
	 *
	 * 
	 */
	public void setFrameTitle(String s) {
		frameTitle = s;
	}

	/**
	 * DOCUMENT-ME
	 *
	 * 
	 */
	public void setFrameComponent(String s) {
		frameComponent = s;
	}

	/**
	 * DOCUMENT-ME
	 *
	 * 
	 */
	public String getFrameTitle() {
		return frameTitle;
	}

	/**
	 * DOCUMENT-ME
	 *
	 * 
	 */
	public String getFrameComponent() {
		return frameComponent;
	}

	/**
	 * DOCUMENT-ME
	 *
	 * 
	 */
	public Frame getConnectedFrame() {
		return myFrame;
	}

	/**
	 * DOCUMENT-ME
	 *
	 * 
	 */
	public void setState(int s) {
		state = s;
	}

	/**
	 * DOCUMENT-ME
	 *
	 * 
	 */
	public int getState() {
		return state;
	}

	/**
	 * DOCUMENT-ME
	 *
	 * 
	 */
	public void setVisible(boolean b) {
		visible = b;
	}

	/**
	 * DOCUMENT-ME
	 *
	 * 
	 */
	public boolean isVisible() {
		return visible;
	}

	/**
	 * DOCUMENT-ME
	 *
	 * 
	 */
	public void windowDeactivated(java.awt.event.WindowEvent p1) {
	}

	/**
	 * DOCUMENT-ME
	 *
	 * 
	 */
	public void windowClosed(java.awt.event.WindowEvent p1) {
	}

	/**
	 * DOCUMENT-ME
	 *
	 * 
	 */
	public void windowDeiconified(java.awt.event.WindowEvent p1) {
		state = Frame.NORMAL;
	}

	/**
	 * DOCUMENT-ME
	 *
	 * 
	 */
	public void windowOpened(java.awt.event.WindowEvent p1) {
		visible = true;
	}

	/**
	 * DOCUMENT-ME
	 *
	 * 
	 */
	public void windowIconified(java.awt.event.WindowEvent p1) {
		state = Frame.ICONIFIED;
	}

	/**
	 * DOCUMENT-ME
	 *
	 * 
	 */
	public void windowClosing(java.awt.event.WindowEvent p1) {
		visible = false;
	}

	/**
	 * DOCUMENT-ME
	 *
	 * 
	 */
	public void windowActivated(java.awt.event.WindowEvent p1) {
	}

	/**
	 * Getter for property configuration.
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
