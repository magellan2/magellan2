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

package magellan.client.swing;

import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.LayoutManager;

/**
 * Simple layout that arranges the first component of the container centered and spans the whole
 * area if the corresponding mode is set.
 *
 * @author Andreas
 * @version 1.0
 */
public class CenterLayout implements LayoutManager {
	private static final Dimension NULL = new Dimension(0, 0);
	private static final int SPAN_X = 1;
	private static final int SPAN_Y = 2;
	protected int mode = 0;

	/** A layout that spans the x axis. */
	public static final CenterLayout SPAN_X_LAYOUT = new CenterLayout(SPAN_X);

	/** A layout that spans the y axis. */
	public static final CenterLayout SPAN_Y_LAYOUT = new CenterLayout(SPAN_Y);

	/** A layout that spans the x and y axis. */
	public static final CenterLayout SPAN_BOTH_LAYOUT = new CenterLayout(SPAN_X | SPAN_Y);

	private CenterLayout(int mode) {
		this.mode = mode;
	}

	/**
	 * DOCUMENT-ME
	 *
	 * 
	 *
	 * 
	 */
	public Dimension minimumLayoutSize(Container container) {
		if(container.getComponentCount() > 0) {
			return container.getComponent(0).getMinimumSize();
		}

		return NULL;
	}

	/**
	 * DOCUMENT-ME
	 *
	 * 
	 * 
	 */
	public void addLayoutComponent(String str, Component component) {
	}

	/**
	 * DOCUMENT-ME
	 *
	 * 
	 */
	public void layoutContainer(Container container) {
		Dimension size;

		if(container.getComponentCount() > 0) {
			size = container.getSize();

			Component first = container.getComponent(0);
			Dimension pSize = first.getPreferredSize();
			int x = (size.width - pSize.width) / 2;

			if((x < 0) || ((mode & SPAN_X) != 0)) {
				x = 0;
			}

			int y = (size.height - pSize.height) / 2;

			if((y < 0) || ((mode & SPAN_Y) != 0)) {
				y = 0;
			}

			int width;

			if((mode & SPAN_X) != 0) {
				width = size.width;
			} else {
				width = Math.min(pSize.width, size.width);
			}

			int height;

			if((mode & SPAN_Y) != 0) {
				height = size.height;
			} else {
				height = Math.min(pSize.height, size.height);
			}

			first.setBounds(x, y, width, height);

			if(container.getComponentCount() > 1) {
				for(int i = 1; i < container.getComponentCount(); i++) {
					container.getComponent(i).setBounds(-1, -1, 0, 0);
				}
			}
		}
	}

	/**
	 * DOCUMENT-ME
	 *
	 * 
	 */
	public void removeLayoutComponent(Component component) {
	}

	/**
	 * DOCUMENT-ME
	 *
	 * 
	 *
	 * 
	 */
	public Dimension preferredLayoutSize(Container container) {
		if(container.getComponentCount() > 0) {
			return container.getComponent(0).getPreferredSize();
		}

		return NULL;
	}
}
