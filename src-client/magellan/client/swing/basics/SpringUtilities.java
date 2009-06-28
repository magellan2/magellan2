package magellan.client.swing.basics;

import java.awt.Component;
import java.awt.Container;

import javax.swing.JPanel;
import javax.swing.Spring;
import javax.swing.SpringLayout;

/**
 * A 1.4 file that provides utility methods for creating form- or grid-style layouts with
 * SpringLayout. These utilities are used by several programs, such as SpringBox and
 * SpringCompactGrid.
 */
public class SpringUtilities {
  /**
   * A debugging utility that prints to stdout the component's minimum, preferred, and maximum
   * sizes.
   */
  public static void printSizes(Component c) {
    System.out.println("minimumSize = " + c.getMinimumSize());
    System.out.println("preferredSize = " + c.getPreferredSize());
    System.out.println("maximumSize = " + c.getMaximumSize());
  }

  /**
   * Aligns the first <code>rows</code> * <code>cols</code> components of <code>parent</code> in a
   * grid. Each component is as big as the maximum preferred width and height of the components. The
   * parent is made just big enough to fit them all.
   * 
   * @param rows number of rows
   * @param cols number of columns
   * @param initialX x location to start the grid at
   * @param initialY y location to start the grid at
   * @param xPad x padding between cells
   * @param yPad y padding between cells
   */
  public static void makeGrid(Container parent, int rows, int cols, int initialX, int initialY,
      int xPad, int yPad) {
    if (rows < 0 || cols < 0 || rows + cols == 0)
      throw new IllegalArgumentException();

    if (parent.getComponentCount()==0)
      return;

    if (rows == 0)
      rows = getOtherGridValue(parent.getComponentCount(), cols);
    if (cols == 0)
      cols = getOtherGridValue(parent.getComponentCount(), cols);

    SpringLayout layout;
    try {
      layout = (SpringLayout) parent.getLayout();
    } catch (ClassCastException exc) {
      System.err.println("The first argument to makeGrid must use SpringLayout.");
      return;
    }

    int max = rows * cols;
    for (int i=parent.getComponentCount(); i<max; ++i){
      parent.add(new JPanel());
    }

    Spring xPadSpring = Spring.constant(xPad);
    Spring yPadSpring = Spring.constant(yPad);
    Spring initialXSpring = Spring.constant(initialX);
    Spring initialYSpring = Spring.constant(initialY);
    
    // Calculate Springs that are the max of the width/height so that all
    // cells have the same size.
    Spring maxWidthSpring = layout.getConstraints(parent.getComponent(0)).getWidth();
    Spring maxHeightSpring = layout.getConstraints(parent.getComponent(0)).getWidth();
    for (int i = 1; i < max; i++) {
      SpringLayout.Constraints cons = layout.getConstraints(parent.getComponent(i));

      maxWidthSpring = Spring.max(maxWidthSpring, cons.getWidth());
      maxHeightSpring = Spring.max(maxHeightSpring, cons.getHeight());
    }

    // Apply the new width/height Spring. This forces all the
    // components to have the same size.
    for (int i = 0; i < max; i++) {
      SpringLayout.Constraints cons = layout.getConstraints(parent.getComponent(i));

      cons.setWidth(maxWidthSpring);
      cons.setHeight(maxHeightSpring);
    }

    // Then adjust the x/y constraints of all the cells so that they
    // are aligned in a grid.
    SpringLayout.Constraints lastCons = null;
    SpringLayout.Constraints lastRowCons = null;
    for (int i = 0; i < max; i++) {
      SpringLayout.Constraints cons = layout.getConstraints(parent.getComponent(i));
      if (i % cols == 0 || lastCons == null) { // start of new row
        lastRowCons = lastCons;
        cons.setX(initialXSpring);
      } else { // x position depends on previous component
        cons.setX(Spring.sum(lastCons.getConstraint(SpringLayout.EAST), xPadSpring));
      }

      if (i / cols == 0 || lastRowCons == null) { // first row
        cons.setY(initialYSpring);
      } else { // y position depends on previous row
        cons.setY(Spring.sum(lastRowCons.getConstraint(SpringLayout.SOUTH), yPadSpring));
      }
      lastCons = cons;
    }

    if (lastCons != null) {
      // Set the parent's size.
      SpringLayout.Constraints pCons = layout.getConstraints(parent);
      pCons.setConstraint(SpringLayout.SOUTH, Spring.sum(Spring.constant(yPad), lastCons
          .getConstraint(SpringLayout.SOUTH)));
      pCons.setConstraint(SpringLayout.EAST, Spring.sum(Spring.constant(xPad), lastCons
          .getConstraint(SpringLayout.EAST)));
    }
  }

  private static int getOtherGridValue(int componentCount, int cols) {
    if (componentCount % cols == 0)
      return componentCount / cols;
    else
      return componentCount / cols + 1;
  }

  /* Used by makeCompactGrid. */
  private static SpringLayout.Constraints getConstraintsForCell(int row, int col, Container parent,
      int cols) {
    SpringLayout layout = (SpringLayout) parent.getLayout();
    Component c = parent.getComponent(row * cols + col);
    return layout.getConstraints(c);
  }

  /**
   * Aligns the first <code>rows</code> * <code>cols</code> components of <code>parent</code> in a
   * grid. Each component in a column is as wide as the maximum preferred width of the components in
   * that column; height is similarly determined for each row. The parent is made just big enough to
   * fit them all.
   * 
   * @param rows number of rows
   * @param cols number of columns
   * @param initialX x location to start the grid at
   * @param initialY y location to start the grid at
   * @param xPad x padding between cells
   * @param yPad y padding between cells
   */
  public static void makeCompactGrid(Container parent, int rows, int cols, int initialX,
      int initialY, int xPad, int yPad) {
    if (rows < 0 || cols < 0 || rows + cols == 0)
      throw new IllegalArgumentException();

    if (parent.getComponentCount()==0)
      return;
    
    if (rows == 0)
      rows = getOtherGridValue(parent.getComponentCount(), cols);
    if (cols == 0)
      cols = getOtherGridValue(parent.getComponentCount(), cols);

    SpringLayout layout;
    try {
      layout = (SpringLayout) parent.getLayout();
    } catch (ClassCastException exc) {
      System.err.println("The first argument to makeCompactGrid must use SpringLayout.");
      return;
    }

    int max = rows * cols;
    for (int i=parent.getComponentCount(); i<max; ++i){
      parent.add(new JPanel());
    }

    // Align all cells in each column and make them the same width.
    Spring x = Spring.constant(initialX);
    for (int c = 0; c < cols; c++) {
      Spring width = Spring.constant(0);
      for (int r = 0; r < rows; r++) {
        width =
            Spring.max(width, SpringUtilities.getConstraintsForCell(r, c, parent, cols).getWidth());
      }
      for (int r = 0; r < rows; r++) {
        SpringLayout.Constraints constraints =
            SpringUtilities.getConstraintsForCell(r, c, parent, cols);
        constraints.setX(x);
        constraints.setWidth(width);
      }
      x = Spring.sum(x, Spring.sum(width, Spring.constant(xPad)));
    }

    // Align all cells in each row and make them the same height.
    Spring y = Spring.constant(initialY);
    for (int r = 0; r < rows; r++) {
      Spring height = Spring.constant(0);
      for (int c = 0; c < cols; c++) {
        height =
            Spring.max(height, SpringUtilities.getConstraintsForCell(r, c, parent, cols)
                .getHeight());
      }
      for (int c = 0; c < cols; c++) {
        SpringLayout.Constraints constraints =
            SpringUtilities.getConstraintsForCell(r, c, parent, cols);
        constraints.setY(y);
        constraints.setHeight(height);
      }
      y = Spring.sum(y, Spring.sum(height, Spring.constant(yPad)));
    }

    // Set the parent's size.
    SpringLayout.Constraints pCons = layout.getConstraints(parent);
    pCons.setConstraint(SpringLayout.SOUTH, y);
    pCons.setConstraint(SpringLayout.EAST, x);
  }
}
