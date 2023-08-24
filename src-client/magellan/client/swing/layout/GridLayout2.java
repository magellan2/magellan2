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

package magellan.client.swing.layout;

import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.Insets;

/**
 * This enhancement enables a GridLayout to emulate a GridBagLayout. Especially this layout allows
 * components of different sizes. This code is extracted from the <a
 * href="http://www.javaworld.com/javaworld/javatips/jw-javatip121.html"> JavaWorld Magazine</a>
 */

// Grid Layout which allows components of different sizes
public class GridLayout2 extends GridLayout {
  /**
   * Generates a GridLayout with 1 row (x-axis) and zero columns (y-axis).
   */
  public GridLayout2() {
    this(1, 0, 0, 0);
  }

  /**
   * Generates a GridLayout with <kbd>rows</kbd> rows and <kbd>cols</kbd> columns.
   */
  public GridLayout2(int rows, int cols) {
    this(rows, cols, 0, 0);
  }

  /**
   * Generates a GridLayout with <kbd>rows</kbd> row, <kbd>cols</kbd> columns, a horizontal gap of
   * <kbd>hgap</kbd> and a vertical gap of <kbd>vgap</kbd> The vertical gap is the distance between two
   * objects in a column (y-axis) The horizontal gap is the distance between two objects in a row
   * (x-axis)
   */
  public GridLayout2(int rows, int cols, int hgap, int vgap) {
    super(rows, cols, hgap, vgap);
  }

  /**
   * from the web page: "As you can see, the code is pretty straightforward. You first ensure that
   * you have the right number of rows and columns to lay out the components. Then you find each
   * component's preferred size. Finally, you compute each row's height as the row components'
   * maximum height. You compute each column width as the maximum width of the row components. The
   * preferred layout size also accounts for the horizontal and vertical gap sizes between
   * components and the parent container insets."
   */
  @Override
  public Dimension preferredLayoutSize(Container parent) {
    // System.err.println("preferredLayoutSize");
    synchronized (parent.getTreeLock()) {
      Insets insets = parent.getInsets();
      int ncomponents = parent.getComponentCount();
      int nrows = getRows();
      int ncols = getColumns();

      if (nrows > 0) {
        ncols = ((ncomponents + nrows) - 1) / nrows;
      } else {
        nrows = ((ncomponents + ncols) - 1) / ncols;
      }

      int w[] = new int[ncols];
      int h[] = new int[nrows];

      for (int i = 0; i < ncomponents; i++) {
        int r = i / ncols;
        int c = i % ncols;
        Component comp = parent.getComponent(i);
        Dimension d = comp.getPreferredSize();

        if (w[c] < d.width) {
          w[c] = d.width;
        }

        if (h[r] < d.height) {
          h[r] = d.height;
        }
      }

      int nw = 0;

      for (int j = 0; j < ncols; j++) {
        nw += w[j];
      }

      int nh = 0;

      for (int i = 0; i < nrows; i++) {
        nh += h[i];
      }

      return new Dimension(insets.left + insets.right + nw + ((ncols - 1) * getHgap()), insets.top
          + insets.bottom + nh + ((nrows - 1) * getVgap()));
    }
  }

  /**
   * From the web page: "The code for minimumLayoutSize() is basically the same as
   * preferredLayoutSize(), except you use the subcomponents' minimum size dimensions."
   */
  @Override
  public Dimension minimumLayoutSize(Container parent) {
    // System.err.println("minimumLayoutSize");
    synchronized (parent.getTreeLock()) {
      Insets insets = parent.getInsets();
      int ncomponents = parent.getComponentCount();
      int nrows = getRows();
      int ncols = getColumns();

      if (nrows > 0) {
        ncols = ((ncomponents + nrows) - 1) / nrows;
      } else {
        nrows = ((ncomponents + ncols) - 1) / ncols;
      }

      int w[] = new int[ncols];
      int h[] = new int[nrows];

      for (int i = 0; i < ncomponents; i++) {
        int r = i / ncols;
        int c = i % ncols;
        Component comp = parent.getComponent(i);
        Dimension d = comp.getMinimumSize();

        if (w[c] < d.width) {
          w[c] = d.width;
        }

        if (h[r] < d.height) {
          h[r] = d.height;
        }
      }

      int nw = 0;

      for (int j = 0; j < ncols; j++) {
        nw += w[j];
      }

      int nh = 0;

      for (int i = 0; i < nrows; i++) {
        nh += h[i];
      }

      return new Dimension(insets.left + insets.right + nw + ((ncols - 1) * getHgap()), insets.top
          + insets.bottom + nh + ((nrows - 1) * getVgap()));
    }
  }

  /**
   * From the web page: "Again, you first ensure you have the right number of rows and columns to
   * lay out the components. Then you compute the scaling factors on x and y coordinates as the
   * ratios between the parent container's current height and width and the height and width of its
   * preferred layout size. You find each component's preferred size and scale it using the scaling
   * factors. You also compute each row's height as the maximum height of the scaled components in
   * the row. You compute each column's width as the maximum width of the scaled components in the
   * row. Finally, you lay out the components, considering the horizontal and vertical gap sizes
   * between components and the parent container insets. You could also scale the horizontal and
   * vertical gap sizes."
   */
  @Override
  public void layoutContainer(Container parent) {
    // System.err.println("layoutContainer");
    synchronized (parent.getTreeLock()) {
      Insets insets = parent.getInsets();
      int ncomponents = parent.getComponentCount();
      int nrows = getRows();
      int ncols = getColumns();

      if (ncomponents == 0)
        return;

      if (nrows > 0) {
        ncols = ((ncomponents + nrows) - 1) / nrows;
      } else {
        nrows = ((ncomponents + ncols) - 1) / ncols;
      }

      int hgap = getHgap();
      int vgap = getVgap();

      // scaling factors
      Dimension pd = preferredLayoutSize(parent);
      double sw = (1.0 * parent.getWidth()) / pd.width;
      double sh = (1.0 * parent.getHeight()) / pd.height;

      // scale
      int w[] = new int[ncols];
      int h[] = new int[nrows];

      for (int i = 0; i < ncomponents; i++) {
        int r = i / ncols;
        int c = i % ncols;
        Component comp = parent.getComponent(i);
        Dimension d = comp.getPreferredSize();
        d.width = (int) (sw * d.width);
        d.height = (int) (sh * d.height);

        if (w[c] < d.width) {
          w[c] = d.width;
        }

        if (h[r] < d.height) {
          h[r] = d.height;
        }
      }

      for (int c = 0, x = insets.left; c < ncols; c++) {
        for (int r = 0, y = insets.top; r < nrows; r++) {
          int i = (r * ncols) + c;

          if (i < ncomponents) {
            parent.getComponent(i).setBounds(x, y, w[c], h[r]);
          }

          y += (h[r] + vgap);
        }

        x += (w[c] + hgap);
      }
    }
  }
}
