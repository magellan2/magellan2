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
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.StringTokenizer;

import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;
import javax.swing.border.TitledBorder;

import magellan.client.swing.preferences.ExtendedPreferencesAdapter;
import magellan.client.swing.preferences.PreferencesAdapter;
import magellan.client.swing.tasks.TaskTablePanel;
import magellan.library.GameData;
import magellan.library.tasks.Inspector;
import magellan.library.tasks.ProblemType;
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
  private JList inspectorsList;
  private JList useList;

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
    panel.add(getInspectorPanel(), c);
    
    c.gridx = 1;
    c.gridy = 0;
    c.fill = GridBagConstraints.NONE;
    c.anchor = GridBagConstraints.EAST;
    c.weightx = 0.1;
    panel.add(new JLabel(""), c);

    restrictPanel.add(panel, BorderLayout.NORTH);

    add(restrictPanel, BorderLayout.CENTER);

  }

  private JPanel getInspectorPanel() {
    JPanel pnlSelection = new JPanel();
    pnlSelection.setLayout(new GridBagLayout());

    GridBagConstraints c = new GridBagConstraints(0, 0, 1, 1, 1, 1, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(2, 2, 2, 2), 0, 0);
    pnlSelection.setBorder(new TitledBorder(BorderFactory.createEtchedBorder(), Resources.get("tasks.prefs.inspectors.selection")));

    JPanel inspectorsPanel = new JPanel();
    inspectorsPanel.setLayout(new BorderLayout(0, 0));
    inspectorsPanel.setBorder(new TitledBorder(BorderFactory.createEtchedBorder(), Resources.get("tasks.prefs.inspectors.available")));

    DefaultListModel inspectorsListModel = new DefaultListModel();
    for (Inspector i : taskPanel.getInspectors()){
      for (ProblemType p : i.getTypes()){
        inspectorsListModel.addElement(p);
      }
    }

    inspectorsList = new JList(inspectorsListModel);

    JScrollPane pane = new JScrollPane(inspectorsList);
    inspectorsPanel.add(pane, BorderLayout.CENTER);

    JPanel usePanel = new JPanel();
    usePanel.setLayout(new GridBagLayout());
    usePanel.setBorder(new TitledBorder(BorderFactory.createEtchedBorder(), Resources.get("tasks.prefs.inspectors.use")));

    useList = new JList();
    useList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    pane = new JScrollPane(useList);
    c.gridheight = 4;
    usePanel.add(pane, c);

    c.gridheight = 1;
    c.gridx = 1;
    c.weightx = 0;
    usePanel.add(new JPanel(), c);

    c.gridx = 0;
    c.gridy = 0;
    c.gridheight = 4;
    c.weightx = 0.5;
    c.weighty = 0.5;
    pnlSelection.add(inspectorsPanel, c);

    c.gridx = 2;
    pnlSelection.add(usePanel, c);

    c.gridx = 1;
    c.gridheight = 1;
    c.weightx = 0;
    c.weighty = 1.0;
    pnlSelection.add(new JPanel(), c);

    c.gridy++;
    c.weighty = 0;

    JButton right = new JButton("  -->  ");
    right.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        Object selection[] = inspectorsList.getSelectedValues();
        DefaultListModel model = (DefaultListModel) useList.getModel();

        for (int i = 0; i < selection.length; i++) {
          if (!model.contains(selection[i])) {
            model.add(model.getSize(), selection[i]);
          }
        }
      }
    });
    pnlSelection.add(right, c);

    c.gridy++;

    JButton left = new JButton("  <--  ");
    left.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        DefaultListModel model = (DefaultListModel) useList.getModel();
        Object selection[] = useList.getSelectedValues();

        for (int i = 0; i < selection.length; i++) {
          model.removeElement(selection[i]);
        }
      }
    });
    pnlSelection.add(left, c);

    c.gridy++;
    c.weighty = 1;
    pnlSelection.add(new JPanel(), c);
    
    return pnlSelection;
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


    String criteria = settings.getProperty(PropertiesHelper.TASKTABLE_INSPECTORS_LIST);
    if (criteria == null){
      StringBuffer sb = new StringBuffer();
      for (Inspector i : taskPanel.getInspectors()){
        for (ProblemType p : i.getTypes()){
          if (sb.length()>0)
            sb.append(" ");
          sb.append(p);
        }
      }
      criteria = sb.toString();
    }
    
    DefaultListModel model2 = new DefaultListModel();

    for (StringTokenizer tokenizer = new StringTokenizer(criteria); tokenizer.hasMoreTokens();) {
      String s = tokenizer.nextToken();
      try {
        try {
          model2.add(model2.size(), s);
        } catch (ArrayIndexOutOfBoundsException e) {
          model2.add(model2.size(), "unknown");
        }
      } catch (NumberFormatException e) {
      }
    }
    useList.setModel(model2);
  }

  /**
   * @see magellan.client.swing.preferences.PreferencesAdapter#applyPreferences()
   */
  public void applyPreferences() {
    DefaultListModel useListModel = (DefaultListModel) useList.getModel();
    StringBuffer definition = new StringBuffer("");

    Set<Object> result = new HashSet<Object>();
    for (int i = 0; i < useListModel.getSize(); i++) {
      Object s = useListModel.getElementAt(i);
      result.add(useListModel.getElementAt(i));
      if (definition.length()>0)
        definition.append(" ");
      definition.append(s);
    }
    taskPanel.setActiveProblems(result);

    settings.setProperty(PropertiesHelper.TASKTABLE_INSPECTORS_LIST, definition.toString());

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
