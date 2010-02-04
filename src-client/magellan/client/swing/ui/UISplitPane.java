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

package magellan.client.swing.ui;

import java.awt.Color;

import javax.swing.BorderFactory;
import javax.swing.JSplitPane;
import javax.swing.border.Border;
import javax.swing.plaf.basic.BasicSplitPaneUI;

/**
 * DOCUMENT-ME
 * 
 * @author $Author: $
 * @version $Revision: 271 $
 */
public class UISplitPane extends JSplitPane {
  private boolean dividerIsLine;

  /**
   * Creates a new UISplitPane object.
   */
  public UISplitPane(int orientation) {
    this(orientation, false);
  }

  /**
   * Creates a new UISplitPane object.
   */
  public UISplitPane(int orientation, boolean dividerIsLine) {
    /* This class tries to remove the divider border of a JSplitPane */
    super(orientation);
    this.dividerIsLine = dividerIsLine;
    setBorder(BorderFactory.createEmptyBorder());
    changeDividerBorder();

    if (dividerIsLine) {
      setDividerSize(1);
    }
  }

  /**
   * DOCUMENT-ME
   */
  @Override
  public void updateUI() {
    /* take care of UI changes */
    super.updateUI();
    changeDividerBorder();
  }

  private void changeDividerBorder() {
    if (getUI() instanceof BasicSplitPaneUI) {
      ((BasicSplitPaneUI) getUI()).getDivider().setBorder(getChangedBorder());
    }
  }

  private Border getChangedBorder() {
    if (dividerIsLine) {
      if (getOrientation() == JSplitPane.HORIZONTAL_SPLIT)
        return BorderFactory.createMatteBorder(0, 0, 0, 1, Color.black); // EAST
      else
        return BorderFactory.createMatteBorder(1, 0, 0, 0, Color.black); // NORTH
    } else
      return BorderFactory.createEmptyBorder();
  }

  // private static class SingleLineBorder {
  // public final static int EAST = 0;
  // private int orientation;
  // public SingleLineBorder(int o) {
  // orientation = o;
  // }
  // }
}
