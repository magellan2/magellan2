// class magellan.client.preferences.AbstractPreferencesAdapter
// created on Jul 29, 2008
//
// Copyright 2003-2008 by magellan project team
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
package magellan.client.preferences;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.LayoutManager;

import javax.swing.BorderFactory;
import javax.swing.JPanel;
import javax.swing.border.TitledBorder;

import magellan.client.swing.layout.GridBagHelper;

/**
 * This class is a helper class for creating PreferencesAdapters or
 * ExtendedPreferencesAdapters. It provides a method for adding groups of
 * options with the same layout.
 * 
 * @author stm
 * @version 1.0, Jul 29, 2008
 */
public abstract class AbstractPreferencesAdapter extends JPanel {

  private GridBagConstraints gridBagConstraints;
  private JPanel currentPanel = null;
  private boolean initialized = false;
  private JPanel content;

  /**
   * @return Returns the last created panel
   */
  public JPanel getCurrentPanel() {
    return currentPanel;
  }

  /**
   * @return Returns the GridBagConstraints used for layouting the panels
   */
  public GridBagConstraints getGridBagConstraints() {
    return gridBagConstraints;
  }

  /**
   * Changes the GridBagConstraints using for layouting panels
   * 
   * @param gridBagConstraints
   */
  public void setGridBagConstraints(GridBagConstraints gridBagConstraints) {
    this.gridBagConstraints = gridBagConstraints;
  }

  /**
   * Called by addPanel if necessary.
   */
  protected void initLayout() {
    this.setLayout(new BorderLayout());
    content = new JPanel(new GridBagLayout());

    gridBagConstraints = new GridBagConstraints();
    gridBagConstraints.insets.top = 10;
    gridBagConstraints.insets.bottom = 10;
    GridBagHelper.setConstraints(gridBagConstraints, 0, 0, 1, 1, 1.0, 0.0,
        GridBagConstraints.FIRST_LINE_START, GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 0),
        0, 0);

    initialized = true;
    this.add(content, BorderLayout.NORTH);
  }

  /**
   * Adds a panel to the PreferencesAdapter. If title is not <code>null</code> it is displayed.
   * 
   * @param title The title of the panel
   * @return The panel that was created
   */
  public JPanel addPanel(String title) {
    if (!initialized)
      initLayout();
    currentPanel = new JPanel();
    initCurrentPanel(title);
    
    return currentPanel;
  }
  
  public JPanel addPanel(String title, LayoutManager layout) {
    if (!initialized)
      initLayout();
    currentPanel = new JPanel(layout);
    initCurrentPanel(title);
    
    return currentPanel;    
  }

  protected void initCurrentPanel(String title){
    if (title != null)
      currentPanel.setBorder(new TitledBorder(BorderFactory.createEtchedBorder(), title));
    else
      currentPanel.setBorder(BorderFactory.createEtchedBorder());
    content.add(currentPanel, gridBagConstraints);
    gridBagConstraints.gridy++;
  }
  
  /**
   * Adds the given component instead of a new JPanel.
   * 
   * @param component
   */
  public void addComponent(Component component) {
    if (!initialized)
      initLayout();

    currentPanel = null;
    content.add(component, gridBagConstraints);
    gridBagConstraints.gridy++;
  }


}