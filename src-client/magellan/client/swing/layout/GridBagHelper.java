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

import java.awt.GridBagConstraints;
import java.awt.Insets;

/**
 * DOCUMENT-ME
 * 
 * @author $Author: $
 * @version $Revision: 171 $
 */
public class GridBagHelper {
  /**
   * Sets the constraints for the given constraints object. Helper function to be able to reuse a
   * constraints object.
   * 
   * @param constraints the constraints object to initialize.
   * @param gridx the initial gridx value.
   * @param gridy the initial gridy value.
   * @param gridwidth the initial gridwidth value.
   * @param gridheight the initial gridheight value.
   * @param weightx the initial weightx value.
   * @param weighty the initial weighty value.
   * @param anchor the initial anchor value.
   * @param fill the initial fill value.
   * @param insets the initial insets value.
   * @param ipadx the initial ipadx value.
   * @param ipady the initial ipady value.
   * @return the initialized constraints object.
   */
  public static GridBagConstraints setConstraints(GridBagConstraints constraints, int gridx,
      int gridy, int gridwidth, int gridheight, double weightx, double weighty, int anchor,
      int fill, Insets insets, int ipadx, int ipady) {
    constraints.gridx = gridx;
    constraints.gridy = gridy;
    constraints.gridwidth = gridwidth;
    constraints.gridheight = gridheight;
    constraints.weightx = weightx;
    constraints.weighty = weighty;
    constraints.anchor = anchor;
    constraints.fill = fill;
    constraints.insets.top = insets.top;
    constraints.insets.bottom = insets.bottom;
    constraints.insets.left = insets.left;
    constraints.insets.right = insets.right;
    constraints.ipadx = ipadx;
    constraints.ipady = ipady;

    return constraints;
  }
}
