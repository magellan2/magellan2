// class magellan.client.preferences.RegionOverviewPreferences
// created on 15.02.2008
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
import java.util.Collections;
import java.util.List;
import java.util.Properties;

import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.TitledBorder;

import magellan.client.swing.preferences.ExtendedPreferencesAdapter;
import magellan.client.swing.preferences.PreferencesAdapter;
import magellan.client.swing.tasks.TaskTablePanel;
import magellan.library.GameData;
import magellan.library.utils.PropertiesHelper;
import magellan.library.utils.Resources;

/**
 * Provides Panel (Extended Preferences Adapter) for Preferences
 * 
 * @author ...
 * @version 1.0, 20.11.2007
 */
public class TaskTablePreferences extends JPanel implements ExtendedPreferencesAdapter {

  private Properties settings;
  private GameData data;
  TaskTablePanel taskPanel;

  // protected ExpandPanel ePanel;
  // protected CollapsePanel cPanel;
  //
  // private JList useList;
  // private JList elementsList;

  private JPanel restrictPanel;

  private JCheckBox chkOwnerParty;

  private JCheckBox chkToDo;
  private JCheckBox chkMovement;
  private JCheckBox chkShip;
  private JCheckBox chkAttack;
  private JCheckBox chkOrderSyntax;

  /**
   * @param parent
   * @param settings
   * @param data
   */
  public TaskTablePreferences(TaskTablePanel parent, Properties settings, GameData data) {
    this.settings = settings;
    taskPanel = parent;

    this.data = data;

    // JPanel pnlTreeStructure = new JPanel();
    // pnlTreeStructure.setLayout(new GridBagLayout());
    //
    // GridBagConstraints c = new GridBagConstraints(0, 0, 1, 1, 1, 1,
    // GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(2,
    // 2, 2, 2), 0, 0);
    // pnlTreeStructure.setBorder(new
    // TitledBorder(BorderFactory.createEtchedBorder(),
    // Resources.get("emapoverviewpanel.prefs.treeStructure")));
    //
    // JPanel elementsPanel = new JPanel();
    // elementsPanel.setLayout(new BorderLayout(0, 0));
    // elementsPanel.setBorder(new
    // TitledBorder(BorderFactory.createEtchedBorder(),
    // Resources.get("emapoverviewpanel.prefs.treeStructure.available")));
    //
    // DefaultListModel elementsListModel = new DefaultListModel();
    // for (Faction f : data.factions().values()){
    // elementsListModel.add(elementsListModel.getSize()-1, f);
    // }
    //    
    // elementsList = new JList(elementsListModel);
    //
    // JScrollPane pane = new JScrollPane(elementsList);
    // elementsPanel.add(pane, BorderLayout.CENTER);
    //
    // JPanel usePanel = new JPanel();
    // usePanel.setLayout(new GridBagLayout());
    // usePanel.setBorder(new TitledBorder(BorderFactory.createEtchedBorder(),
    // Resources.get("emapoverviewpanel.prefs.treeStructure.use")));
    //
    // useList = new JList();
    // useList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    // pane = new JScrollPane(useList);
    // c.gridheight = 4;
    // usePanel.add(pane, c);
    //
    // c.gridheight = 1;
    // c.gridx = 1;
    // c.weightx = 0;
    // usePanel.add(new JPanel(), c);
    //
    // c.gridy++;
    // c.weighty = 0;
    //
    //
    // c.gridy++;
    // c.weighty = 1.0;
    // usePanel.add(new JPanel(), c);
    //
    // c.gridx = 0;
    // c.gridy = 0;
    // c.gridheight = 4;
    // c.weightx = 0.5;
    // c.weighty = 0.5;
    // pnlTreeStructure.add(elementsPanel, c);
    //
    // c.gridx = 2;
    // pnlTreeStructure.add(usePanel, c);
    //
    // c.gridx = 1;
    // c.gridheight = 1;
    // c.weightx = 0;
    // c.weighty = 1.0;
    // pnlTreeStructure.add(new JPanel(), c);
    //
    // c.gridy++;
    // c.weighty = 0;
    //
    // JButton right = new JButton(" --> ");
    // right.addActionListener(new ActionListener() {
    // public void actionPerformed(ActionEvent e) {
    // Object selection[] = elementsList.getSelectedValues();
    // DefaultListModel model = (DefaultListModel) useList.getModel();
    //
    // for (int i = 0; i < selection.length; i++) {
    // if (!model.contains(selection[i])) {
    // model.add(model.getSize(), selection[i]);
    // }
    // }
    // }
    // });
    // pnlTreeStructure.add(right, c);
    //
    // c.gridy++;
    //
    // JButton left = new JButton(" <-- ");
    // left.addActionListener(new ActionListener() {
    // public void actionPerformed(ActionEvent e) {
    // DefaultListModel model = (DefaultListModel) useList.getModel();
    // Object selection[] = useList.getSelectedValues();
    //
    // for (int i = 0; i < selection.length; i++) {
    // model.removeElement(selection[i]);
    // }
    // }
    // });
    // pnlTreeStructure.add(left, c);

    // c.gridy++;
    // c.weighty = 1;
    // pnlTreeStructure.add(new JPanel(), c);
    //
    //
    //
    // this.setLayout(new GridBagLayout());
    // c.anchor = GridBagConstraints.CENTER;
    // c.gridx = 0;
    // c.gridy = 0;
    // c.gridwidth = 1;
    // c.gridheight = 1;
    // c.fill = GridBagConstraints.HORIZONTAL;
    // c.weightx = 1.0;
    // c.weighty = 0.0;
    //
    // c.insets.left = 0;
    // c.anchor = GridBagConstraints.CENTER;
    // c.fill = GridBagConstraints.HORIZONTAL;
    // c.weightx = 1.0;
    // this.add(pnlTreeStructure, c);

    this.setLayout(new BorderLayout());

    restrictPanel = new JPanel();
    restrictPanel.setBorder(new TitledBorder(BorderFactory.createEtchedBorder(), Resources
        .get("tasks.prefs.border.options")));
    restrictPanel.setLayout(new BorderLayout());

    JPanel panel = new JPanel(new GridBagLayout());
    GridBagConstraints c = new GridBagConstraints();
    c.insets = new Insets(2, 2, 2, 2);

    c.gridx = 0;
    c.gridy = 0;
    c.fill = GridBagConstraints.HORIZONTAL;
    c.anchor = GridBagConstraints.WEST;
    c.weightx = 0.0;
    chkOwnerParty = new JCheckBox(Resources.get("tasks.prefs.restricttoowner"), false);
    panel.add(chkOwnerParty, c);

    c.gridy++;
    chkToDo = new JCheckBox(Resources.get("tasks.prefs.inspectors.todo"), true);
    panel.add(chkToDo, c);

    c.gridy++;
    chkMovement = new JCheckBox(Resources.get("tasks.prefs.inspectors.movement"), true);
    panel.add(chkMovement, c);

    c.gridy++;
    chkShip = new JCheckBox(Resources.get("tasks.prefs.inspectors.ship"), true);
    panel.add(chkShip, c);

    c.gridy++;
    chkAttack = new JCheckBox(Resources.get("tasks.prefs.inspectors.attack"), true);
    panel.add(chkAttack, c);

    c.gridy++;
    chkOrderSyntax = new JCheckBox(Resources.get("tasks.prefs.inspectors.ordersyntax"), true);
    panel.add(chkOrderSyntax, c);

    c.gridx = 1;
    c.gridy = 0;
    c.fill = GridBagConstraints.NONE;
    c.anchor = GridBagConstraints.EAST;
    c.weightx = 0.1;
    panel.add(new JLabel(""), c);

    restrictPanel.add(panel, BorderLayout.NORTH);

    add(restrictPanel, BorderLayout.CENTER);

  }

  /**
   * @see magellan.client.swing.preferences.PreferencesAdapter#initPreferences()
   */
  public void initPreferences() {

    DefaultListModel model2 = new DefaultListModel();
    // for (Faction f : data.factions().values()){
    // model2.add(elementsListModel.getSize()-1, f);
    // }

    // useList.setModel(model2);

    /* restrict problems to owner faction */
    chkOwnerParty.setSelected(PropertiesHelper.getBoolean(settings,
        PropertiesHelper.TASKTABLE_RESTRICT_TO_OWNER, false));

    /* use attack inspector */
    chkAttack.setSelected(PropertiesHelper.getBoolean(settings,
        PropertiesHelper.TASKTABLE_INSPECTORS_ATTACK, true));
    /* use movement inspector */
    chkMovement.setSelected(PropertiesHelper.getBoolean(settings,
        PropertiesHelper.TASKTABLE_INSPECTORS_MOVEMENT, true));
    /* use order syntax inspector */
    chkOrderSyntax.setSelected(PropertiesHelper.getBoolean(settings,
        PropertiesHelper.TASKTABLE_INSPECTORS_ORDER_SYNTAX, true));
    /* use ship inspector */
    chkShip.setSelected(PropertiesHelper.getBoolean(settings,
        PropertiesHelper.TASKTABLE_INSPECTORS_SHIP, true));
    /* use to do inspector */
    chkToDo.setSelected(PropertiesHelper.getBoolean(settings,
        PropertiesHelper.TASKTABLE_INSPECTORS_TODO, true));

  }

  /**
   * @see magellan.client.swing.preferences.PreferencesAdapter#applyPreferences()
   */
  public void applyPreferences() {
    settings.setProperty(PropertiesHelper.TASKTABLE_RESTRICT_TO_OWNER, "" + chkOwnerParty.isSelected());
    
    settings.setProperty(PropertiesHelper.TASKTABLE_INSPECTORS_ATTACK, "" + chkAttack.isSelected());
    settings.setProperty(PropertiesHelper.TASKTABLE_INSPECTORS_MOVEMENT, "" + chkMovement.isSelected());
    settings.setProperty(PropertiesHelper.TASKTABLE_INSPECTORS_ORDER_SYNTAX, "" + chkOrderSyntax.isSelected());
    settings.setProperty(PropertiesHelper.TASKTABLE_INSPECTORS_SHIP, "" + chkShip.isSelected());
    settings.setProperty(PropertiesHelper.TASKTABLE_INSPECTORS_TODO, "" + chkToDo.isSelected());

    // DefaultListModel useListModel = (DefaultListModel) useList.getModel();
    // StringBuffer definition = new StringBuffer("");
    //
    // DefaultListModel elementsListModel = (DefaultListModel)
    // elementsList.getModel();
    // for (int i = 0; i < useListModel.getSize(); i++) {
    // String s = (String) useListModel.getElementAt(i);
    //
    // int pos = elementsListModel.indexOf(s);
    // definition.append(pos).append(" ");
    // }
    //
    // settings.setProperty("EMapOverviewPanel.treeStructure",
    // definition.toString());
    //

    taskPanel.refreshProblems();
  }

  /**
   * @see magellan.client.swing.preferences.PreferencesAdapter#getComponent()
   */
  public Component getComponent() {
    return this;
  }

  /**
   * @see magellan.client.swing.preferences.PreferencesAdapter#getTitle()
   */
  public String getTitle() {
    return Resources.get("tasks.prefs.title");
  }

  /**
   * @see magellan.client.swing.preferences.ExtendedPreferencesAdapter#getChildren()
   */
  public List<PreferencesAdapter> getChildren() {
    return Collections.emptyList();
  }

}
