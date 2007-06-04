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
import java.awt.Dimension;
import java.awt.Rectangle;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.swing.JComponent;
import javax.swing.JSplitPane;


/**
 * DOCUMENT ME!
 *
 * @author Andreas
 * @version
 */
public class SplitBuilder extends Object {
	private List<Component> componentsUsed;

	/** Holds value of property screen. */
	private Rectangle screen;
	private static Dimension minSize;

	/**
	 * Creates new SplitBuilder
	 *
	 * 
	 */
	public SplitBuilder(Rectangle s) {
		componentsUsed = new LinkedList<Component>();
		screen = s;

		if(minSize == null) {
			minSize = new Dimension(100, 10);
		}
	}

	/**
	 * DOCUMENT-ME
	 *
	 * 
	 * 
	 *
	 * 
	 */
	public JComponent buildDesktop(FrameTreeNode root, Map<String,Component> components) {
		componentsUsed.clear();
		root = checkTree(root, components);

		if(root == null) {
			return null;
		}

		if(root.isLeaf()) {
			componentsUsed.add(components.get(root.getName()));

			return (JComponent) components.get(root.getName());
		}

		return createSplit(root, components, screen);
	}

	protected FrameTreeNode checkTree(FrameTreeNode node, Map comp) {
		if(node == null) {
			return null;
		}

		if(node.isLeaf()) {
			if(comp.containsKey(node.getName())) {
				return node;
			} else {
				return null;
			}
		}

		FrameTreeNode left = checkTree(node.getChild(0), comp);
		FrameTreeNode right = checkTree(node.getChild(1), comp);
		node.setChild(0, left);
		node.setChild(1, right);

		if(left == null) {
			return right;
		}

		if(right == null) {
			return left;
		}

		return node;
	}

	protected JComponent createSplit(FrameTreeNode current, Map components, Rectangle sourceRect) {
		int orient = current.getOrientation();
		JSplitPane jsp = magellan.client.swing.ui.UIFactory.createBorderlessJSplitPane(orient);
		Rectangle left = new Rectangle();
		Rectangle right = new Rectangle();
		left.x = sourceRect.x;
		left.y = sourceRect.y;

		if(current.isAbsolute()) {
			int divider = (int) Math.round(current.getPercentage());
			divider = checkDividerInRectangle(divider, orient, sourceRect);
			jsp.setDividerLocation(divider);
			createRects(orient, divider, sourceRect, left, right);
		} else {
			int divider = createRects(orient, current.getPercentage(), sourceRect, left, right);
			jsp.setDividerLocation(divider);
		}

		// pavkovic 2004.04.02: remove one touch expander
		jsp.setOneTouchExpandable(false);

		// connect the split pane and the node
		current.connectToSplitPane(jsp);

		if(current.getChild(0).isLeaf()) {
			JComponent jc = (JComponent) components.get(current.getChild(0).getName());

            {
                Object name = current.getChild(0).getName();
                String configuration = current.getChild(0).getConfiguration();
                // special meaning of overview
                if("OVERVIEW".equals(name))  {
                    name = "OVERVIEW&HISTORY";
                } 
                if(components.get(name) instanceof Initializable && configuration != null) {
                    ((Initializable) components.get(name)).initComponent(configuration);
                }
            }

			jc.setMinimumSize(minSize);

			if(current.getChild(0).getName() == null) {
				jsp.setTopComponent(jc);
			} else {
				jsp.setTopComponent(new magellan.client.swing.ui.InternalFrame(current.getChild(0)
																				  .getName(), jc));
			}

			if(!componentsUsed.contains(jc)) {
				componentsUsed.add(jc);
			}
		} else {
			JComponent jc = createSplit(current.getChild(0), components, left);

			if(current.getChild(0).getName() == null) {
				jsp.setTopComponent(jc);
			} else {
				jsp.setTopComponent(new magellan.client.swing.ui.InternalFrame(current.getChild(0)
																				  .getName(), jc));
			}
		}

		if(current.getChild(1).isLeaf()) {
			JComponent jc = (JComponent) components.get(current.getChild(1).getName());

			if((jc instanceof Initializable) && (current.getChild(1).getConfiguration() != null)) {
				((Initializable) jc).initComponent(current.getChild(1).getConfiguration());
			}

			jc.setMinimumSize(minSize);

			if(current.getChild(1).getName() == null) {
				jsp.setBottomComponent(jc);
			} else {
				jsp.setBottomComponent(new magellan.client.swing.ui.InternalFrame(current.getChild(1)
																					 .getName(), jc));
			}

			if(!componentsUsed.contains(jc)) {
				componentsUsed.add(jc);
			}
		} else {
			// jsp.setBottomComponent(createSplit(current.getChild(1), components, right));
			JComponent jc = createSplit(current.getChild(1), components, right);

			if(current.getChild(1).getName() == null) {
				jsp.setBottomComponent(jc);
			} else {
				jsp.setBottomComponent(new magellan.client.swing.ui.InternalFrame(current.getChild(1)
																					 .getName(), jc));
			}
		}

		return jsp;
	}

	private void createRects(int orient, int divider, Rectangle source, Rectangle left,
							 Rectangle right) {
		if(orient == JSplitPane.HORIZONTAL_SPLIT) {
			left.width = divider - left.x;
			left.height = source.height;
			right.x = divider;
			right.y = left.y;
			right.width = source.width - left.width;
			right.height = left.height;
		} else {
			left.width = source.width;
			left.height = divider - source.y;
			right.x = source.x;
			right.y = divider;
			right.width = source.width;
			right.height = source.height - left.height;
		}
	}

	private int createRects(int orient, double div, Rectangle source, Rectangle left,
							Rectangle right) {
		int divider;

		if(orient == JSplitPane.HORIZONTAL_SPLIT) {
			divider = source.x + (int) (div * source.width);
			left.width = divider - left.x;
			left.height = source.height;
			right.x = divider;
			right.y = left.y;
			right.width = source.width - left.width;
			right.height = left.height;
			divider = left.width;
		} else {
			divider = source.y + (int) (div * source.height);
			left.width = source.width;
			left.height = divider - source.y;
			right.x = source.x;
			right.y = divider;
			right.width = source.width;
			right.height = source.height - left.height;
			divider = left.height;
		}

		return divider;
	}

	private int checkDividerInRectangle(int divider, int orient, Rectangle bounds) {
		if(orient == JSplitPane.HORIZONTAL_SPLIT) {
			if(divider < 0) {
				return 1;
			}

			if(divider > bounds.width) {
				return bounds.width - 1;
			}
		} else {
			if(divider < 0) {
				return 1;
			}

			if(divider > bounds.height) {
				return bounds.height - 1;
			}
		}

		return divider;
	}

	/**
	 * DOCUMENT-ME
	 *
	 * 
	 */
	public List getComponentsUsed() {
		return componentsUsed;
	}

	/**
	 * Getter for property screen.
	 *
	 * @return Value of property screen.
	 */
	public Rectangle getScreen() {
		return screen;
	}

	/**
	 * Setter for property screen.
	 *
	 * @param screen New value of property screen.
	 */
	public void setScreen(Rectangle screen) {
		this.screen = screen;
	}
}
