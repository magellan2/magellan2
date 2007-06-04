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
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.LayoutManager;
import java.awt.event.ComponentListener;
import java.awt.font.FontRenderContext;
import java.awt.font.LineBreakMeasurer;
import java.awt.font.TextAttribute;
import java.awt.font.TextLayout;
import java.text.AttributedCharacterIterator;
import java.text.AttributedString;
import java.util.Iterator;
import java.util.LinkedList;

import javax.swing.Icon;
import javax.swing.JPanel;
import javax.swing.JTree;
import javax.swing.plaf.TreeUI;
import javax.swing.plaf.basic.BasicTreeUI;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.TreeCellRenderer;
import javax.swing.tree.TreePath;

/**
 * DOCUMENT ME!
 *
 * @author Andreas
 * @version
 */
public class LineWrapCellRenderer extends JPanel implements TreeCellRenderer, ComponentListener {
	protected DefaultTreeCellRenderer defaultRenderer;
	protected Icon icon;
	protected String text;
	protected int lastLength = -1;
	protected Color textColor;
	protected Color textBackground;
	protected Color textFocus;
	protected LinkedList<TextLayout> buf = new LinkedList<TextLayout>();

	/** Holds value of property lineWrap. */
	private boolean lineWrap;

	/**
	 * Creates new Template
	 *
	 * 
	 */
	public LineWrapCellRenderer(DefaultTreeCellRenderer def) {
		defaultRenderer = def;

		setOpaque(false);
	}

	/**
	 * DOCUMENT-ME
	 *
	 * 
	 */
	public void registerTree(JTree tree) {
		tree.addComponentListener(this);
	}

	/**
	 * DOCUMENT-ME
	 *
	 * 
	 */
	public void unregisterTree(JTree tree) {
		tree.removeComponentListener(this);
	}

	protected int getIndent(JTree tree, BasicTreeUI tui, int row) {
		int width = tui.getLeftChildIndent() + tui.getRightChildIndent();
		TreePath path = tree.getPathForRow(row);
		int j = 0;

		if(path != null) {
			j = path.getPathCount() - 1;
		}

		return (width * j);
	}

	protected boolean computeSize(JTree tree, BasicTreeUI ui, int row) {
		int treeWidth = tree.getSize().width;

		int labelWidth = 0;

		if(icon != null) {
			labelWidth += (icon.getIconWidth() + 4);
		}

		int indent = 0;

		try {
			indent = getIndent(tree, ui, row);
		} catch(Exception exc) {
			return false;
		}

		Insets in = getInsets();

		int maxLength = treeWidth - indent - labelWidth - 3 - in.left - in.right;
		lastLength = maxLength;

		AttributedString string = new AttributedString(text);
		string.addAttribute(TextAttribute.FONT, getFont());

		AttributedCharacterIterator it = string.getIterator();
		FontRenderContext context = ((Graphics2D) tree.getGraphics()).getFontRenderContext();
		LineBreakMeasurer lbm = new LineBreakMeasurer(it, context);

		int height = 0;
		float help = 0;

		while(lbm.getPosition() < it.getEndIndex()) {
			TextLayout tl = lbm.nextLayout(maxLength);
			help += (tl.getAscent() + tl.getDescent());

			if(lbm.getPosition() < it.getEndIndex()) {
				help += tl.getLeading();
			}
		}

		height = (int) Math.ceil(help);

		if(icon != null) {
			height = Math.max(height, icon.getIconHeight());
		}

		setPreferredSize(new Dimension(treeWidth - indent - 1, height + in.top + in.bottom + 2));

		return true;
	}

	protected void computeOneLineSize(FontMetrics fm) {
		int height = fm.getAscent() + fm.getDescent() + 2;
		int width = fm.stringWidth(text) + 2;

		if(icon != null) {
			height = Math.max(height, icon.getIconHeight());
			width += (icon.getIconWidth() + 4);
		}

		setPreferredSize(new Dimension(width, height));
	}

	/**
	 * DOCUMENT-ME
	 *
	 * 
	 */
	public void paintComponent(Graphics g) {
		int x = 0;
		int y = 1;

		Dimension size = getSize();

		if(icon != null) {
			y = Math.max(0, ((size.height - icon.getIconHeight()) / 2));
			icon.paintIcon(this, g, 0, y);
			y = 1;
			x = icon.getIconWidth() + 4;
		}

		x++;

		int height = 0;
		int width = 0;

		if((lastLength != -1) && (g instanceof Graphics2D)) {
			// break text
			Graphics2D g2 = (Graphics2D) g;

			AttributedString string = new AttributedString(text);
			string.addAttribute(TextAttribute.FONT, getFont());
			string.addAttribute(TextAttribute.FOREGROUND, textColor);

			AttributedCharacterIterator it = string.getIterator();
			FontRenderContext context = g2.getFontRenderContext();
			LineBreakMeasurer lbm = new LineBreakMeasurer(it, context);

			float help = 0;
			float help2 = 0;

			while(lbm.getPosition() < it.getEndIndex()) {
				TextLayout tl = lbm.nextLayout(lastLength);
				buf.add(tl);
				help += tl.getAscent();

				if(tl.getAdvance() > help2) {
					help2 = tl.getAdvance();
				}

				help += tl.getDescent();

				if(lbm.getPosition() < it.getEndIndex()) {
					help += tl.getLeading();
				}
			}

			height = (int) Math.ceil(help);
			width = (int) Math.ceil(help2);

			if(textBackground != null) {
				g.setColor(textBackground);
				g.fillRect(x, y, width, height);
			}

			help = 0;

			Iterator it2 = buf.iterator();

			while(it2.hasNext()) {
				TextLayout tl = (TextLayout) it2.next();
				help += tl.getAscent();
				tl.draw(g2, x, help);
				help += tl.getDescent();

				if(it2.hasNext()) {
					help += tl.getLeading();
				}
			}

			buf.clear();
		} else {
			// draw a singe text line
			FontMetrics fm = g.getFontMetrics(getFont());
			width = fm.stringWidth(text);
			height = fm.getAscent() + fm.getDescent();

			if(textBackground != null) {
				g.setColor(textBackground);
				g.fillRect(x, 0, width, height);
			}

			g.setColor(textColor);
			g.drawString(text, x, fm.getAscent());
		}

		if((textFocus != null) && (width > 0) && (height > 0)) {
			g.setColor(textFocus);

			x--;
			g.drawRect(x, 0, width, height);
		}
	}

	/**
	 * DOCUMENT-ME
	 *
	 * 
	 * 
	 * 
	 * 
	 * 
	 * 
	 * 
	 *
	 * 
	 */
	public java.awt.Component getTreeCellRendererComponent(javax.swing.JTree jTree,
														   java.lang.Object obj, boolean param,
														   boolean param3, boolean param4,
														   int param5, boolean param6) {
		icon = null;
		textBackground = null;
		textFocus = null;
		lastLength = -1;

		text = jTree.convertValueToText(obj, param, param3, param4, param5, param6);

		if(defaultRenderer != null) {
			if(param) {
				textBackground = defaultRenderer.getBackgroundSelectionColor();
				textColor = defaultRenderer.getTextSelectionColor();
				textFocus = defaultRenderer.getBorderSelectionColor();
			} else {
				textBackground = defaultRenderer.getBackgroundNonSelectionColor();
				textColor = defaultRenderer.getTextNonSelectionColor();
			}

			if(param4) {
				icon = defaultRenderer.getLeafIcon();
			} else {
				if(param3) {
					icon = defaultRenderer.getOpenIcon();
				} else {
					icon = defaultRenderer.getClosedIcon();
				}
			}
		} else {
			if(param) {
				textBackground = Color.blue.brighter();
				textFocus = Color.blue;
			} else {
				textBackground = Color.white;
			}

			textColor = Color.black;
		}

		TreeUI ui = jTree.getUI();
		boolean init = false;

		if(lineWrap && ui instanceof BasicTreeUI) {
			try {
				init = computeSize(jTree, (BasicTreeUI) ui, param5);
			} catch(Exception exc) {
			}
		}

		if(!init) {
			computeOneLineSize(jTree.getFontMetrics(getFont()));
		}

		return this;
	}

	protected void updateTree(JTree tree) {
		TreeUI ui = tree.getUI();

		/* Since there seems to be a kind of bug with JTree.treeDidChange(),
		 * we have to try to let the UI compute the bounds manually.
		 *
		 * I have only found the way to temporary change indent values in BasicTreeUI
		 * to let the cache be cleared.
		 *                                  Andreas
		 */
		if(ui instanceof BasicTreeUI) {
			BasicTreeUI tui = (BasicTreeUI) ui;
			int i = tui.getLeftChildIndent();
			tui.setLeftChildIndent(i + 1);
			tui.setLeftChildIndent(i);
		}
	}

	/**
	 * Informs trees on size changes to recompute their row bounds.
	 *
	 * 
	 */
	public void componentResized(java.awt.event.ComponentEvent componentEvent) {
		Object o = componentEvent.getSource();

		if(o instanceof JTree) {
			updateTree((JTree) o);
		}
	}

	/**
	 * DOCUMENT-ME
	 *
	 * 
	 */
	public void componentMoved(java.awt.event.ComponentEvent componentEvent) {
	}

	/**
	 * DOCUMENT-ME
	 *
	 * 
	 */
	public void componentShown(java.awt.event.ComponentEvent componentEvent) {
	}

	/**
	 * DOCUMENT-ME
	 *
	 * 
	 */
	public void componentHidden(java.awt.event.ComponentEvent componentEvent) {
	}

	/**
	 * Getter for property lineWrap.
	 *
	 * @return Value of property lineWrap.
	 */
	public boolean isLineWrap() {
		return lineWrap;
	}

	/**
	 * Setter for property lineWrap.
	 *
	 * @param lineWrap New value of property lineWrap.
	 */
	public void setLineWrap(boolean lineWrap) {
		this.lineWrap = lineWrap;
	}

	protected class SimpleLayout implements LayoutManager {
		protected Dimension inDim = new Dimension(0, 0);
		protected boolean inCompute = false;

		/**
		 * DOCUMENT-ME
		 *
		 * 
		 *
		 * 
		 */
		public java.awt.Dimension minimumLayoutSize(java.awt.Container container) {
			if(inCompute) {
				return inDim;
			}

			inCompute = true;

			Dimension dim = container.getComponent(0).getMinimumSize();
			Dimension dim2 = container.getComponent(1).getMinimumSize();
			Insets in = container.getInsets();
			dim.width += (dim2.width + in.left + in.right);
			dim.height = Math.max(dim.height, dim2.height) + in.top + in.bottom;
			inCompute = false;

			return dim;
		}

		/**
		 * DOCUMENT-ME
		 *
		 * 
		 * 
		 */
		public void addLayoutComponent(java.lang.String str, java.awt.Component component) {
		}

		/**
		 * DOCUMENT-ME
		 *
		 * 
		 */
		public void layoutContainer(java.awt.Container container) {
			// always use pref sizes
			Component c = container.getComponent(0);
			Dimension dim = c.getPreferredSize();
			Insets in = container.getInsets();
			c.setBounds(in.left, in.top, dim.width, dim.height);

			c = container.getComponent(1);

			Dimension dim2 = c.getPreferredSize();
			c.setBounds(in.left + dim.width, in.top, dim2.width, dim2.height);
		}

		/**
		 * DOCUMENT-ME
		 *
		 * 
		 */
		public void removeLayoutComponent(java.awt.Component component) {
		}

		/**
		 * DOCUMENT-ME
		 *
		 * 
		 *
		 * 
		 */
		public java.awt.Dimension preferredLayoutSize(java.awt.Container container) {
			if(inCompute) {
				return inDim;
			}

			inCompute = true;

			Dimension dim = container.getComponent(0).getPreferredSize();
			Dimension dim2 = container.getComponent(1).getPreferredSize();
			Insets in = container.getInsets();
			dim.width += (dim2.width + in.left + in.right);
			dim.height = Math.max(dim.height, dim2.height) + in.top + in.bottom;
			inCompute = false;

			return dim;
		}
	}
}
