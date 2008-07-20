// class magellan.client.desktop.MagellanPopupMenuFactory
// created on 20.06.2007
//
// Copyright 2003-2007 by magellan project team
//
// Author : $Author: $
// $Id: $
//
// This program is free software; you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation; either version 2 of the License, or
// (at your option) any later version.
// 
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU General Public License for more details.
// 
// You should have received a copy of the GNU General Public License
// along with this program (see doc/LICENCE.txt); if not, write to the
// Free Software Foundation, Inc., 
// 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
// 
package magellan.client.desktop;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeListener;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.Icon;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;

import magellan.library.utils.Resources;
import net.infonode.docking.AbstractTabWindow;
import net.infonode.docking.DockingWindow;
import net.infonode.docking.RootWindow;
import net.infonode.docking.SplitWindow;
import net.infonode.docking.TabWindow;
import net.infonode.docking.View;
import net.infonode.docking.WindowBar;
import net.infonode.docking.WindowPopupMenuFactory;
import net.infonode.docking.action.CloseOthersWindowAction;
import net.infonode.docking.action.CloseWithAbortWindowAction;
import net.infonode.docking.action.DockWithAbortWindowAction;
import net.infonode.docking.action.MaximizeWithAbortWindowAction;
import net.infonode.docking.action.MinimizeWithAbortWindowAction;
import net.infonode.docking.action.RestoreWithAbortWindowAction;
import net.infonode.docking.action.UndockWithAbortWindowAction;
import net.infonode.docking.internalutil.InternalDockingUtil;
import net.infonode.docking.util.DockingUtil;
import net.infonode.docking.util.ViewFactory;
import net.infonode.docking.util.ViewFactoryManager;
import net.infonode.gui.icon.button.ArrowIcon;
import net.infonode.gui.menu.MenuUtil;
import net.infonode.tabbedpanel.TabbedPanelProperties;
import net.infonode.tabbedpanel.titledtab.TitledTabProperties;
import net.infonode.util.Direction;

/**
 * This is a factory for popup creation in a infonode docking environment.
 * It is actually a copy of the WindowMenuUtil class and contains localization
 * strings for the menu items...(this is currently now supported in the
 * infonode framework).
 *
 * @author Thoralf Rickert, 
 * @version 1.0, 20.06.2007
 */
public class MagellanPopupMenuFactory implements WindowPopupMenuFactory {
  private static final Icon[] ARROW_ICONS = new Icon[4];
  private ViewFactoryManager viewFactoryManager;

  static {
    final Direction[] directions = Direction.getDirections();

    for (int i = 0; i < directions.length; i++) {
      MagellanPopupMenuFactory.ARROW_ICONS[i] = new ArrowIcon(InternalDockingUtil.DEFAULT_BUTTON_ICON_SIZE + 1, directions[i]);
    }
  }
  
  public MagellanPopupMenuFactory(ViewFactoryManager viewFactoryManager) {
    this.viewFactoryManager = viewFactoryManager;
  }

  /**
   * @see net.infonode.docking.WindowPopupMenuFactory#createPopupMenu(net.infonode.docking.DockingWindow)
   */
  public JPopupMenu createPopupMenu(DockingWindow window) {
    JPopupMenu menu = new JPopupMenu(window.getTitle());

    if (!(window instanceof RootWindow)) {
      if (!(window instanceof WindowBar)) {
        MagellanPopupMenuFactory.addWindowMenuItems(menu, window);
        menu.addSeparator();
      }

      //if (addTabItems) {
        MagellanPopupMenuFactory.addTabOrientationMenuItems(menu, window);
        MagellanPopupMenuFactory.addTabDirectionMenuItems(menu, window);
        menu.addSeparator();
     // }

     // if (addSplitWindowItems) {
        MagellanPopupMenuFactory.addSplitWindowMenuItems(menu, window);
        menu.addSeparator();
     // }
    }

    MagellanPopupMenuFactory.addNewViewMenuItems(menu, window, viewFactoryManager);
    MenuUtil.optimizeSeparators(menu);
    MenuUtil.align(menu);
    return menu;
  }

  /**
   * 
   */  
  private static void addWindowMenuItems(JPopupMenu menu, DockingWindow window) {
    menu.add(new MagellanPopupAction(UndockWithAbortWindowAction.INSTANCE.getAction(window).toSwingAction(),Resources.get("dock.popup.undock")));
    menu.add(new MagellanPopupAction(DockWithAbortWindowAction.INSTANCE.getAction(window).toSwingAction(),Resources.get("dock.popup.dock")));
    menu.add(new MagellanPopupAction(RestoreWithAbortWindowAction.INSTANCE.getAction(window).toSwingAction(),Resources.get("dock.popup.restore")));
    menu.add(new MagellanPopupAction(MinimizeWithAbortWindowAction.INSTANCE.getAction(window).toSwingAction(),Resources.get("dock.popup.minimize")));

    if (window instanceof TabWindow) {
      menu.add(new MagellanPopupAction(MaximizeWithAbortWindowAction.INSTANCE.getAction(window).toSwingAction(),Resources.get("dock.popup.maximize")));
    }

    menu.add(new MagellanPopupAction(CloseWithAbortWindowAction.INSTANCE.getAction(window).toSwingAction(),Resources.get("dock.popup.close")));

    if (window.getWindowParent() instanceof AbstractTabWindow) {
      menu.add(new MagellanPopupAction(CloseOthersWindowAction.INSTANCE.getAction(window).toSwingAction(),Resources.get("dock.popup.close_others")));
    }

    JMenu moveToMenu = MagellanPopupMenuFactory.getMoveToMenuItems(window);

    if (moveToMenu.getItemCount() > 0) {
      menu.add(moveToMenu);
    }
  }

  /**
   * 
   */
  private static void addTabOrientationMenuItems(JPopupMenu menu, DockingWindow window) {
    final AbstractTabWindow tabWindow = MagellanPopupMenuFactory.getTabWindowFor(window);

    if (tabWindow == null || tabWindow instanceof WindowBar) {
      return;
    }

    JMenu orientationMenu = new JMenu(Resources.get("dock.popup.tab_orientation"));
    TabbedPanelProperties properties = tabWindow.getTabWindowProperties().getTabbedPanelProperties();
    final Direction[] directions = Direction.getDirections();

    for (int i = 0; i < directions.length; i++) {
      final Direction dir = directions[i];
      JMenuItem item = orientationMenu.add(new JMenuItem(Resources.get("dock.direction."+dir.getName()), MagellanPopupMenuFactory.ARROW_ICONS[i]));
      item.setEnabled(dir != properties.getTabAreaOrientation());
      item.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          tabWindow.getTabWindowProperties().getTabbedPanelProperties().setTabAreaOrientation(dir);
        }
      });
    }

    menu.add(orientationMenu);
  }

  private static void addTabDirectionMenuItems(JPopupMenu menu, DockingWindow window) {
    final AbstractTabWindow tabWindow = MagellanPopupMenuFactory.getTabWindowFor(window);

    if (tabWindow == null) {
      return;
    }

    JMenu directionMenu = new JMenu(Resources.get("dock.popup.tab_direction"));
    TitledTabProperties properties = TitledTabProperties.getDefaultProperties();
    properties.addSuperObject(tabWindow.getTabWindowProperties().getTabProperties().getTitledTabProperties());
    final Direction[] directions = Direction.getDirections();

    for (int i = 0; i < directions.length; i++) {
      final Direction dir = directions[i];

      if (dir != Direction.LEFT) {
        JMenuItem item = directionMenu.add(new JMenuItem(Resources.get("dock.direction."+dir.getName()), MagellanPopupMenuFactory.ARROW_ICONS[i]));
        item.setEnabled(dir != properties.getNormalProperties().getDirection());
        item.addActionListener(new ActionListener() {
          public void actionPerformed(ActionEvent e) {
            tabWindow.getTabWindowProperties().getTabProperties().getTitledTabProperties().getNormalProperties()
                .setDirection(dir);
          }
        });
      }
    }

    menu.add(directionMenu);
  }

  private static void addSplitWindowMenuItems(JPopupMenu menu, final DockingWindow window) {
    if (window instanceof SplitWindow) {
      JMenu splitMenu = new JMenu(Resources.get("dock.popup.split_window"));

      splitMenu.add("25%").addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          ((SplitWindow) window).setDividerLocation(0.25f);
        }
      });

      splitMenu.add("50%").addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          ((SplitWindow) window).setDividerLocation(0.5f);
        }
      });

      splitMenu.add("75%").addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          ((SplitWindow) window).setDividerLocation(0.75f);
        }
      });

      splitMenu.addSeparator();

      splitMenu.add(Resources.get("dock.popup.flip_orientation")).addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          ((SplitWindow) window).setHorizontal(!((SplitWindow) window).isHorizontal());
        }
      });

      splitMenu.add(Resources.get("dock.popup.mirror")).addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          SplitWindow sw = (SplitWindow) window;
          sw.setWindows(window.getChildWindow(1), window.getChildWindow(0));
          sw.setDividerLocation(1 - sw.getDividerLocation());
        }
      });

      menu.add(splitMenu);
    }
  }
  

  private static void addNewViewMenuItems(JPopupMenu menu, final DockingWindow window, ViewFactoryManager viewManager) {
    ViewFactory[] viewFactories = viewManager.getViewFactories();

    if (viewFactories.length == 0) {
      return;
    }

    JMenu viewsPopup = new JMenu(Resources.get("dock.popup.show_view"));

    for (int i = 0; i < viewFactories.length; i++) {
      final ViewFactory vf = viewFactories[i];

      viewsPopup.add(new JMenuItem(vf.getTitle(), vf.getIcon())).addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          View view = vf.createView();

          if (view.getRootWindow() == window.getRootWindow()) {
            return;
          }

          view.restore();

          if (view.getRootWindow() == window.getRootWindow()) {
            return;
          }

          if (window instanceof RootWindow) {
            ((RootWindow) window).setWindow(view);
          } else {
            AbstractTabWindow tabWindow = MagellanPopupMenuFactory.getTabWindowFor(window);

            if (tabWindow != null) {
              tabWindow.addTab(view);
            }
          }
        }
      });
    }

    menu.add(viewsPopup);
  }

  private static AbstractTabWindow getTabWindowFor(DockingWindow window) {
    return (AbstractTabWindow)
        (window instanceof AbstractTabWindow ? window :
         window.getWindowParent() != null && window.getWindowParent() instanceof AbstractTabWindow ?
         window.getWindowParent() :
         null);
  }

  private static JMenu getMoveToMenuItems(final DockingWindow window) {
    JMenu moveToMenu = new JMenu(Resources.get("dock.popup.move_to_windowbar"));

    if (window.isMinimizable()) {
      final RootWindow root = window.getRootWindow();
      final Direction[] directions = Direction.getDirections();

      for (int i = 0; i < 4; i++) {
        final Direction dir = directions[i];

        if (!DockingUtil.isAncestor(root.getWindowBar(dir), window) && root.getWindowBar(dir).isEnabled()) {
          moveToMenu.add(new JMenuItem(Resources.get("dock.direction."+dir.getName()), MagellanPopupMenuFactory.ARROW_ICONS[i])).addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
              root.getWindowBar(dir).addTab(window);
            }
          });
        }
      }
    }

    return moveToMenu;
  }

}

class MagellanPopupAction extends AbstractAction {
  private AbstractAction superAction = null;
  
  public MagellanPopupAction(Action superAction, String name) {
    this.superAction = (AbstractAction)superAction;
    putValue(Action.NAME, name);
  }

  /**
   * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
   */
  public void actionPerformed(ActionEvent e) {
    superAction.actionPerformed(e);
  }

  /**
   * @see javax.swing.AbstractAction#addPropertyChangeListener(java.beans.PropertyChangeListener)
   */
  @Override
  public synchronized void addPropertyChangeListener(PropertyChangeListener listener) {
    superAction.addPropertyChangeListener(listener);
  }

  /**
   * @see javax.swing.AbstractAction#getKeys()
   */
  @Override
  public Object[] getKeys() {
    return superAction.getKeys();
  }

  /**
   * @see javax.swing.AbstractAction#getPropertyChangeListeners()
   */
  @Override
  public synchronized PropertyChangeListener[] getPropertyChangeListeners() {
    return superAction.getPropertyChangeListeners();
  }

  /**
   * @see javax.swing.AbstractAction#getValue(java.lang.String)
   */
  @Override
  public Object getValue(String key) {
    return superAction.getValue(key);
  }

  /**
   * @see javax.swing.AbstractAction#isEnabled()
   */
  @Override
  public boolean isEnabled() {
    return superAction.isEnabled();
  }

  /**
   * @see javax.swing.AbstractAction#putValue(java.lang.String, java.lang.Object)
   */
  @Override
  public void putValue(String key, Object newValue) {
    superAction.putValue(key, newValue);
  }

  /**
   * @see javax.swing.AbstractAction#removePropertyChangeListener(java.beans.PropertyChangeListener)
   */
  @Override
  public synchronized void removePropertyChangeListener(PropertyChangeListener listener) {
    superAction.removePropertyChangeListener(listener);
  }

  /**
   * @see javax.swing.AbstractAction#setEnabled(boolean)
   */
  @Override
  public void setEnabled(boolean newValue) {
    superAction.setEnabled(newValue);
  }
  
  
}
