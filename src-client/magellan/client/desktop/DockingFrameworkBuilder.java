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
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.swing.JCheckBoxMenuItem;
import javax.swing.JComponent;
import javax.swing.JMenu;
import javax.swing.JSplitPane;

import magellan.client.Client;
import magellan.client.utils.ErrorWindow;
import magellan.client.utils.MagellanObjectInputStream;
import magellan.client.utils.MagellanObjectOutputStream;
import magellan.library.utils.Resources;
import magellan.library.utils.logging.Logger;
import net.infonode.docking.DockingWindow;
import net.infonode.docking.RootWindow;
import net.infonode.docking.SplitWindow;
import net.infonode.docking.TabWindow;
import net.infonode.docking.View;
import net.infonode.docking.theme.DockingWindowsTheme;
import net.infonode.docking.theme.ShapedGradientDockingTheme;
import net.infonode.docking.util.DockingUtil;
import net.infonode.docking.util.StringViewMap;
import net.infonode.util.Direction;


/**
 *
 * @author Andreas
 * @version
 */
public class DockingFrameworkBuilder  {
  private static final Logger log = Logger.getInstance(DockingFrameworkBuilder.class);
	private List<Component> componentsUsed;
  private StringViewMap viewMap = null;

	/** Holds value of property screen. */
	private Rectangle screen;
	private static Dimension minSize;

	/**
	 * Creates new DockingFrameworkBuilder
	 *
	 * 
	 */
	public DockingFrameworkBuilder(Rectangle s) {
		componentsUsed = new LinkedList<Component>();
		screen = s;

		if(minSize == null) {
			minSize = new Dimension(100, 10);
		}
	}

	/**
	 * 
	 */
	public JComponent buildDesktop(FrameTreeNode root, Map<String,Component> components, File serializedView) {
		componentsUsed.clear();
		root = checkTree(root, components);

		if(root == null) {
			return null;
		}

		if(root.isLeaf()) {
			componentsUsed.add(components.get(root.getName()));

			return (JComponent) components.get(root.getName());
		}
    
    // okay, this is a try for InfoNode docking window framework
    // we have a tree of settings and a list of components. Let's build a root window here...
    
    return createRootWindow(components,screen,serializedView);

		//return createSplit(root, components, screen);
	}
  
  /**
   * This method tries to setup the infonode docking framework.
   */
  protected JComponent createRootWindow(Map<String,Component> components, Rectangle size, File serializedViewData) {
    Map<String,View> views = new HashMap<String,View>();
    viewMap = new StringViewMap();

    for (String key : components.keySet()) {
      if (key.equals("COMMANDS")) continue; // deprecated
      if (key.equals("NAME")) continue; // deprecated
      if (key.equals("DESCRIPTION")) continue; // deprecated
      if (key.equals("OVERVIEW&HISTORY")) continue; // deprecated
      
      Component component = components.get(key);
      
      View view = new View(Resources.get("dock."+key+".title"),null,component);
      view.setName(key);
      view.setToolTipText(Resources.get("dock."+key+".tooltip"));
      viewMap.addView(key,view);
      views.put(key,view);
    }
    
    RootWindow window = null;
    try {
      if (serializedViewData != null && serializedViewData.exists()) {
        window = read(viewMap,serializedViewData);
      } else {
        window = createDefault(viewMap,views);
      }
    } catch (NullPointerException npe) {
      // okay, sometimes this happens without a reason (setToolTipText())...
      log.error("NPE",npe);
    } catch (Throwable t) {
      log.fatal(t.getMessage(),t);
      ErrorWindow errorWindow = new ErrorWindow(Client.INSTANCE,t.getMessage(),"",t);
      errorWindow.setVisible(true);
    }
    
    DockingWindowsTheme theme = new ShapedGradientDockingTheme();
    window.getWindowBar(Direction.DOWN).setEnabled(true);
    window.getRootWindowProperties().addSuperObject(theme.getRootWindowProperties());
    window.setPopupMenuFactory(new MagellanPopupMenuFactory(viewMap));
    
    window.getRootWindowProperties().getWindowAreaProperties().setBackgroundColor(null).setBorder(null);
    window.getRootWindowProperties().getWindowAreaShapedPanelProperties().setComponentPainter(null);
    window.getRootWindowProperties().getComponentProperties().setBackgroundColor(null);
    window.getRootWindowProperties().getShapedPanelProperties().setComponentPainter(null);

    return window;
  }
  
  public StringViewMap getViewMap() {
    return viewMap;
  }
  
  public void write(File serializedViewData, RootWindow window) throws IOException {
    FileOutputStream fos = new FileOutputStream(serializedViewData);
    ObjectOutputStream oos = new MagellanObjectOutputStream(fos);
    window.write(oos, true);
    oos.close();
    fos.close();
  }
  
  public RootWindow read(StringViewMap viewMap, File serializedViewData) throws IOException {
    RootWindow window = DockingUtil.createRootWindow(viewMap, true);
    
    FileInputStream fis = new FileInputStream(serializedViewData);
    ObjectInputStream ois = new MagellanObjectInputStream(fis);
    window.read(ois, true);
    ois.close();
    fis.close();
    return window;
  }
  
  
  protected RootWindow createDefault(StringViewMap viewMap, Map<String,View> views) {
    RootWindow window = DockingUtil.createRootWindow(viewMap, true);
    
    View overview = views.get("OVERVIEW");
    View history = views.get("HISTORY");
    View minimap = views.get("MINIMAP");
    View map = views.get("MAP");
    View messages = views.get("MESSAGES");
    View details = views.get("DETAILS");
    View orders = views.get("ORDERS");
    View name = views.get("NAME&DESCRIPTION");
    View echeck = views.get("ECHECK");
    
    TabWindow bottomLeft = new TabWindow(new DockingWindow[]{minimap,history});
    bottomLeft.setSelectedTab(0);
    SplitWindow left = new SplitWindow(false,overview,bottomLeft);
    left.setDividerLocation(0.6f);
    
    TabWindow bottomCenter = new TabWindow(new DockingWindow[]{messages,echeck});
    bottomCenter.setSelectedTab(0);
    SplitWindow middle = new SplitWindow(false,map,bottomCenter);
    middle.setDividerLocation(0.6f);
    
    SplitWindow topRight = new SplitWindow(false,name,details);
    SplitWindow right = new SplitWindow(false,topRight,orders);
    
    SplitWindow splitWindow = new SplitWindow(true,left,new SplitWindow(true,middle,right));
    splitWindow.setDividerLocation(0.3f);
    
    window.setWindow(splitWindow);
    
    return window;
  }
  
  public static JMenu createDesktopMenu(Map<String,Component> components, ActionListener listener) {
    JMenu desktopMenu = new JMenu(Resources.get("desktop.magellandesktop.menu.desktop.caption"));
    desktopMenu.setMnemonic(Resources.get("desktop.magellandesktop.menu.desktop.mnemonic").charAt(0));
    
    if(components.size() > 0) {
      for (String key : components.keySet()) {
        if (key.equals("COMMANDS")) continue; // deprecated
        if (key.equals("NAME")) continue; // deprecated
        if (key.equals("DESCRIPTION")) continue; // deprecated
        if (key.equals("OVERVIEW&HISTORY")) continue; // deprecated
        
        Component component = components.get(key);
        JCheckBoxMenuItem item = new JCheckBoxMenuItem(Resources.get("dock."+key+".title"), false);
        item.setActionCommand("menu."+key);
        desktopMenu.add(item);
        item.addActionListener(listener);
      }
    }
    
    return desktopMenu;
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
