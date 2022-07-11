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

import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.KeyStroke;
import javax.swing.event.TableModelEvent;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableModel;

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
    if (gcc == null)
      return;
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
      adjustToScreen(bounds);
      component.setBounds(bounds);
    } else {
      if (maximize) {
        Rectangle screen = component.getGraphicsConfiguration().getBounds();
        component.setSize(screen.width, screen.height);
      }
      center(component);
    }
  }

  public static void setPreferredSize(Component component, Properties settings, String rectKey) {
    Rectangle bounds = PropertiesHelper.loadRect(settings, null, rectKey);
    if (bounds != null) {
      adjustToScreen(bounds);
      component.setPreferredSize(new Dimension(bounds.width, bounds.height));
    }
  }

  private static void adjustToScreen(Rectangle bounds) {
    Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
    // correct position to be included in the screen
    bounds.x = Math.max(bounds.x, -bounds.width * 4 / 5 + 10);
    bounds.y = Math.max(bounds.y, -bounds.height * 4 / 5 + 10);
    bounds.x = Math.min(bounds.x, screen.width - bounds.width / 10 + 10);
    bounds.y = Math.min(bounds.y, screen.height - bounds.height / 5 + 10);
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

  /**
   * Returns a dimension set to multiples of the current (JLabel) font size.
   * 
   * @param d This is a multiple of the current font size
   * @param e This is a multiple of the current font size. If this is -1, widthUnits * 5 / 8 is used instead.
   * @param adjustToScreen If this is true, the size is adjustet to the screen size
   */
  public static Dimension getDimension(double d, double e, boolean adjustToScreen) {
    int fontSize = new JLabel().getFont().getSize();
    int width = (int) (fontSize * d);
    int height = (int) (e < 0 ? width / 1.618 : fontSize * e);
    if (adjustToScreen) {
      Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
      if (screen.width < width) {
        width = screen.width;
      }
      if (screen.height < height) {
        height = screen.height;
      }
    }
    return new Dimension(width, height);
  }

  /**
   * Sets the preferred size of the component to multiples of the current (JLabel) font size.
   * 
   * @param c
   * @param widthUnits This is a multiple of the current font size
   * @param heightUnits This is a multiple of the current font size. If this is -1, widthUnits * 5 / 8 is used instead.
   * @param adjustToScreen If this is true, the size is adjustet to the screen size
   */
  public static void setPreferredSize(Component c, double widthUnits, double heightUnits, boolean adjustToScreen) {
    c.setPreferredSize(getDimension(widthUnits, heightUnits, adjustToScreen));
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

  /**
   * A class that helps adjusting row heights of tables, see {@link #prepareTable(JTable)}.
   *
   */
  public interface RenderHelper {

    /**
     * Call this from your overwritten prepareHandler method like this:
     * <code>
     * renderHelper.wrapPrepareHandlerRowHeightAdjusted(JTable.this, row,
     *           super.prepareRenderer(renderer, row, column));
     * </code>
     * 
     * @param jTable
     * @param row
     * @param component
     * @return
     */
    Component wrapPrepareHandlerRowHeightAdjusted(JTable jTable, int row, Component component);

    /**
     * Call this after constructing your table.
     * 
     * @param jTable
     */
    void prepareTable(JTable jTable);

  }

  /**
   * implementation of RenderHelper
   */
  static class RH implements RenderHelper {
    int invalidate[];
    int invalidateMarker;
    private TableModel model;

    void prepareTable(TableModel model) {
      invalidate = new int[0];
      this.model = model;
      model.addTableModelListener((TableModelEvent e) -> {
        synchronized (model) {
          ++invalidateMarker;
        }
      });
    }

    public void prepareTable(JTable jTable) {
      prepareTable(jTable.getModel());
    }

    /**
     * @param jTable
     * @param row
     * @param component
     * @return
     */
    public Component wrapPrepareHandlerRowHeightAdjusted(JTable jTable, int row, Component component) {

      synchronized (model) {
        if (invalidate == null || invalidate.length != jTable.getRowCount()) {
          invalidate = new int[jTable.getRowCount()];
        }
        if (invalidate[row] != invalidateMarker) {
          invalidate[row] = invalidateMarker;
          int height = component.getPreferredSize().height;
          int old = jTable.getRowHeight();
          if (height > old) {
            jTable.setRowHeight(row, height);
          }
        }
        return component;
      }
    }

  }

  /**
   * Use this method to construct a table that adjusts its row heights with the font size of its content.
   * 
   * Use like this:
   * <code>
   * AbstractTableModel tableModel = new MyModel();
   * final RenderHelper renderHelper = SwingUtils.prepareTable(tableModel);
   * JTable table = MyTable(tableModel) {
   *   &#64;Override
   *   public Component prepareRenderer(TableCellRenderer renderer, int row, int column) {
   *     return renderHelper.wrapPrepareHandlerRowHeightAdjusted(this, row,
   *     super.prepareRenderer(renderer, row, column));
   *   }
   * };
   * </code>
   * 
   * @param model Your table model, should extend AbstractTableModel
   * @return A RenderHelper, which you can call in your prepareRenderer method.
   */
  public static RenderHelper prepareTable(AbstractTableModel model) {
    RH rh = new RH();
    rh.prepareTable(model);
    return rh;
  }

  /**
   * Alternative method to construct a table that adjusts its row heights with the font size of its content.
   * 
   * Use like this:
   * <code>
   * RenderHelper renderHelper = SwingUtils.prepareTable();
   * JTable table = MyTable() {
   *   &#64;Override
   *   public Component prepareRenderer(TableCellRenderer renderer, int row, int column) {
   *     return renderHelper.wrapPrepareHandlerRowHeightAdjusted(this, row,
   *     super.prepareRenderer(renderer, row, column));
   *   }
   * };
   * rh.prepareTable(table);
   * </code>
   * 
   * @return A RenderHelper, which you can call in your prepareRenderer method.
   */
  public static RenderHelper prepareTable() {
    RH rh = new RH();
    return rh;
  }

}
