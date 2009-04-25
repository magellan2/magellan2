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
  TaskTablePanel taskPanel;

  // protected ExpandPanel ePanel;
  // protected CollapsePanel cPanel;
  //
  // private JList useList;
  // private JList elementsList;

  private JPanel restrictPanel;

  /** Nur Probleme der Besitzerpartei anzeigen lassen */
  private JCheckBox chkOwnerParty;
  /** Nur Probleme von Parteien anzeigen lassen, bei denen wir etwas ändern können (Passwort haben) */
  private JCheckBox chkPasswordParties;
  /** Nur Probleme der selektierten Regionen anzeigen */
  private JCheckBox chkRestrictToSelection;
  /** Nur Probleme der aktuellen Region anzeigen */
  private JCheckBox chkRestrictToActiveRegion;

  private JCheckBox chkToDo;
  private JCheckBox chkMovement;
  private JCheckBox chkShip;
  private JCheckBox chkSkill;
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
    chkOwnerParty = new JCheckBox(Resources.get("tasks.prefs.restricttoowner"), true);
    panel.add(chkOwnerParty, c);

    c.gridy++;
    chkPasswordParties = new JCheckBox(Resources.get("tasks.prefs.restricttopassword"), true);
    panel.add(chkPasswordParties, c);
    
    c.gridy++;
    chkRestrictToActiveRegion = new JCheckBox(Resources.get("tasks.prefs.restricttoactiveregion"), true);
    panel.add(chkRestrictToActiveRegion, c);
    
    c.gridy++;
    chkRestrictToSelection = new JCheckBox(Resources.get("tasks.prefs.restricttoselection"), true);
    panel.add(chkRestrictToSelection, c);
    
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
    chkSkill = new JCheckBox(Resources.get("tasks.prefs.inspectors.skill"), true);
    panel.add(chkSkill, c);

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

    /* restrict problems to owner faction */
    chkOwnerParty.setSelected(taskPanel.restrictToOwner());
    
    /* restrict problems to factions with passwords */
    chkPasswordParties.setSelected(taskPanel.restrictToPassword());

    chkRestrictToActiveRegion.setSelected(taskPanel.restrictToActiveRegion());
    
    chkRestrictToSelection.setSelected(taskPanel.restrictToSelection());

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
    /* use skill inspector */
    chkSkill.setSelected(PropertiesHelper.getBoolean(settings,
        PropertiesHelper.TASKTABLE_INSPECTORS_SKILL, true));
    /* use to do inspector */
    chkToDo.setSelected(PropertiesHelper.getBoolean(settings,
        PropertiesHelper.TASKTABLE_INSPECTORS_TODO, true));

  }

  /**
   * @see magellan.client.swing.preferences.PreferencesAdapter#applyPreferences()
   */
  public void applyPreferences() {
    settings.setProperty(PropertiesHelper.TASKTABLE_INSPECTORS_ATTACK, "" + chkAttack.isSelected());
    settings.setProperty(PropertiesHelper.TASKTABLE_INSPECTORS_MOVEMENT, "" + chkMovement.isSelected());
    settings.setProperty(PropertiesHelper.TASKTABLE_INSPECTORS_ORDER_SYNTAX, "" + chkOrderSyntax.isSelected());
    settings.setProperty(PropertiesHelper.TASKTABLE_INSPECTORS_SHIP, "" + chkShip.isSelected());
    settings.setProperty(PropertiesHelper.TASKTABLE_INSPECTORS_SKILL, "" + chkSkill.isSelected());
    settings.setProperty(PropertiesHelper.TASKTABLE_INSPECTORS_TODO, "" + chkToDo.isSelected());

    taskPanel.setRestrictToOwner(chkOwnerParty.isSelected());
    taskPanel.setRestrictToPassword(chkPasswordParties.isSelected());
    taskPanel.setRestrictToSelection(chkRestrictToSelection.isSelected());
    taskPanel.setRestrictToActiveRegion(chkRestrictToActiveRegion.isSelected());
    
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
