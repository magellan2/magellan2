// class magellan.client.utils.SwingUtils
// created on Nov 23, 2010
//
// Copyright 2003-2010 by magellan project team
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
package magellan.client.utils;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.GraphicsConfiguration;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.Window;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.util.Properties;

import javax.swing.KeyStroke;

import magellan.library.utils.PropertiesHelper;

/**
 * A collection of utility functions for Magellan and Swing
 * 
 * @author stm
 * @version 1.0, Nov 23, 2010
 */
public class SwingUtils {

  /**
   * Centers the given component on the current graphics configuration (i.e. on the current screen).
   * 
   * @param component
   * @see GraphicsConfiguration#getBounds()
   */
  public static void center(Component component) {
    GraphicsConfiguration gcc = component.getGraphicsConfiguration();
    Rectangle b = gcc.getBounds();
    // Dimension ss = getToolkit().getScreenSize();

    Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
    // int x = (screen.width - component.getWidth()) / 2;
    // int y = (screen.height - component.getHeight()) / 2;
    int xx = b.x + (b.width - component.getWidth()) / 2;
    int yy = b.y + (b.height - component.getHeight()) / 2;

    // Rectangle virtualBounds = new Rectangle();
    // GraphicsEnvironment ge = GraphicsEnvironment.
    // getLocalGraphicsEnvironment();
    // GraphicsDevice[] gs = ge.getScreenDevices();
    // for (GraphicsDevice gd : gs) {
    // GraphicsConfiguration[] gc = gd.getConfigurations();
    // for (GraphicsConfiguration element : gc) {
    // virtualBounds = virtualBounds.union(element.getBounds());
    // }
    // }

    component.setLocation(xx, yy);
  }

  /**
   * Positions the given component either at the position specified by the two property keys, or, if
   * those are not defined, centers it on the current graphics configuration (i.e. on the current
   * screen). Should the saved location bring the component outside the screen bounds, the position
   * is corrected to at least partly be visible on the screen. This is useful if Magellan is started
   * on different screen resolutions.
   * 
   * @param component This component's location is changed
   * @param settings to load the position
   * @param xKey key for the x value in settings. Should evaluate to an integer.
   * @param yKey key for the x value in settings. Should evaluate to an integer.
   * @see GraphicsConfiguration#getBounds()
   */
  public static void setLocation(Component component, Properties settings, String xKey, String yKey) {
    setLocation(component, settings, xKey, yKey, true);
  }

  /**
   * Positions the given component either at the position specified by the two property keys, or, if
   * those are not defined and <code>center==true</code>, centers it on the current graphics
   * configuration (i.e. on the current screen). Should the saved location bring the component
   * outside the screen bounds, the position is corrected to at least partly be visible on the
   * screen. This is useful if Magellan is started on different screen resolutions.
   * 
   * @param component This component's location is changed
   * @param settings to load the position
   * @param xKey key for the x value in settings. Should evaluate to an integer.
   * @param yKey key for the x value in settings. Should evaluate to an integer.
   * @param center If true, the component is centered if the values cannot be read. Otherwise, the
   *          location isn't changed.
   * @see GraphicsConfiguration#getBounds()
   */
  public static void setLocation(Component component, Properties settings, String xKey,
      String yKey, boolean center) {
    GraphicsConfiguration gc = component.getGraphicsConfiguration();
    Rectangle b = gc.getBounds();

    // String xs = settings.getProperty(xKey);
    // String ys = settings.getProperty(yKey);
    int x = PropertiesHelper.getInteger(settings, xKey, b.x + (b.width - component.getWidth()) / 2);
    int y =
        PropertiesHelper.getInteger(settings, yKey, b.y + (b.height - component.getHeight()) / 2);

    // correct position to be included in the screen
    Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
    x = Math.max(x, -component.getWidth() * 4 / 5 + 10);
    y = Math.max(y, -component.getHeight() * 4 / 5 + 10);
    x = Math.min(x, screen.width - component.getWidth() / 10 + 10);
    y = Math.min(y, screen.height - component.getHeight() / 5 + 10);

    component.setLocation(x, y);
  }

  /**
   * Sets the component's bounds to the value read from settings by rectKey. The settings should
   * contain four keys (rectKey.x, rectKey.y, rectKey.width, rectKey.height) that evaluate to
   * integer. Should the saved location bring the component outside the screen bounds, the position
   * is corrected to at least partly be visible on the screen. This is useful if Magellan is started
   * on different screen resolutions. If the location is not in the settings, the component is
   * maximized on the current device dimensions if <code>maximize==true</code>, then centered.
   * 
   * @param component
   * @param settings
   * @param rectKey
   * @param maximize
   */
  public static void setBounds(Component component, Properties settings, String rectKey,
      boolean maximize) {
    Rectangle bounds = PropertiesHelper.loadRect(settings, null, rectKey);
    if (bounds != null) {
      Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
      // correct position to be included in the screen
      bounds.x = Math.max(bounds.x, -bounds.width * 4 / 5 + 10);
      bounds.y = Math.max(bounds.y, -bounds.height * 4 / 5 + 10);
      bounds.x = Math.min(bounds.x, screen.width - bounds.width / 10 + 10);
      bounds.y = Math.min(bounds.y, screen.height - bounds.height / 5 + 10);

      component.setBounds(bounds);
    } else {
      if (maximize) {
        Rectangle screen = component.getGraphicsConfiguration().getBounds();
        component.setSize(screen.width, screen.height);
      }
      center(component);
    }
  }

  /**
   * Adjust the given rectangle so that it fits the screen size of window.
   * 
   * @param x of old rectangle
   * @param y of old rectangle
   * @param width of old rectangle
   * @param height of old rectangle
   * @param window
   * @return A rectangle that overlaps {@link Window#getToolkit()}.getScreenSize() by at least a few
   *         pixels
   * @see Toolkit#getScreenSize()
   */
  public static Rectangle adjustToScreen(int x, int y, int width, int height, Window window) {
    // make sure dialog is inside screen
    Rectangle r = new Rectangle(x, y, width, height);
    r.width = Math.max(r.width, window.getToolkit().getScreenSize().width / 10 + 1);
    r.width = Math.max(r.width, 20);
    r.height = Math.max(r.height, window.getToolkit().getScreenSize().height / 10 + 1);
    r.height = Math.max(r.height, 20);
    r.width = Math.min(r.width, window.getToolkit().getScreenSize().width);
    r.height = Math.min(r.height, window.getToolkit().getScreenSize().height);
    r.x = Math.min(r.x, window.getToolkit().getScreenSize().width - r.width / 2);
    r.y = Math.min(r.y, window.getToolkit().getScreenSize().height - r.height / 2);
    r.x = Math.max(r.x, -r.width / 2 + 10);
    r.y = Math.max(r.y, -r.height / 2 + 10);
    return r;
  }

  public static String getKeyStroke(KeyStroke stroke) {
    return SwingUtils.getKeyStroke(stroke.getModifiers(), stroke.getKeyCode());
  }

  public static String getKeyStroke(int modifiers, int keyCode) {

    if (keyCode == KeyEvent.VK_UNDEFINED || isModifier(keyCode))
      return InputEvent.getModifiersExText(modifiers);
    else if (modifiers != 0)
      return InputEvent.getModifiersExText(modifiers) + " + "
          + KeyEvent.getKeyText(keyCode);
    else
      return KeyEvent.getKeyText(keyCode);
  }

  public static boolean isModifier(int key) {
    return ((key == KeyEvent.VK_SHIFT) || (key == KeyEvent.VK_CONTROL) || (key == KeyEvent.VK_ALT)
        || (key == KeyEvent.VK_ALT_GRAPH));
  }

}
