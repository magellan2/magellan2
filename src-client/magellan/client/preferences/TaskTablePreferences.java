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
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
// GNU General Public License for more details.
//
// You should have received a copy of the GNU General Public License
// along with this program (see doc/LICENCE.txt); if not, write to the
// Free Software Foundation, Inc.,
// 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
//
package magellan.client.preferences;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.border.TitledBorder;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;

import magellan.client.swing.layout.WrappableLabel;
import magellan.client.swing.preferences.ExtendedPreferencesAdapter;
import magellan.client.swing.preferences.PreferencesAdapter;
import magellan.client.swing.tasks.TaskTablePanel;
import magellan.library.GameData;
import magellan.library.tasks.ProblemType;
import magellan.library.utils.Resources;

/**
 * Provides Panel (Extended Preferences Adapter) for Preferences
 * 
 * @author stm
 * @version 1.0, 20.11.2007
 */
public class TaskTablePreferences extends JPanel implements ExtendedPreferencesAdapter {

  /**
   * A tree that displays problem types
   */
  public class TypeTree extends JTree {

    DefaultMutableTreeNode root;
    DefaultTreeModel model;
    Map<String, DefaultMutableTreeNode> catNodes;
    Set<ProblemType> problems;

    public TypeTree(boolean fill) {
      root = new DefaultMutableTreeNode();
      model = new DefaultTreeModel(root);
      setModel(model);
      setRootVisible(false);
      catNodes = new HashMap<String, DefaultMutableTreeNode>();
      problems = new HashSet<ProblemType>();
      if (fill) {
        fill();
      }
    }

    protected void fill() {
      root.removeAllChildren();
      catNodes.clear();
      problems.clear();
      for (ProblemType p : taskPanel.getAllProblemTypes()) {
        addProblem(p);
      }
      model.nodeStructureChanged(root);
    }

    public Collection<ProblemType> getProblems() {
      return Collections.unmodifiableCollection(problems);
    }

    public Collection<ProblemType> getSelectedProblems() {
      List<ProblemType> result = new LinkedList<ProblemType>();
      TreePath selection[] = getSelectionPaths();
      if (selection == null)
        return Collections.emptySet();

      for (TreePath element : selection) {
        DefaultMutableTreeNode node = (DefaultMutableTreeNode) element.getLastPathComponent();
        if (node.getUserObject() instanceof ProblemType) {
          result.add((ProblemType) node.getUserObject());
        } else {
          for (Enumeration<?> children = node.children(); children.hasMoreElements();) {
            result.add((ProblemType) ((DefaultMutableTreeNode) children.nextElement())
                .getUserObject());
          }
        }
      }
      return result;
    }

    public void addProblem(ProblemType p) {
      if (problems.contains(p))
        return;
      String cat = p.getGroup();
      if (cat == null) {
        root.add(new DefaultMutableTreeNode(p));
        problems.add(p);
      } else {
        if (!catNodes.containsKey(cat)) {
          DefaultMutableTreeNode catNode = new DefaultMutableTreeNode(cat);
          root.add(catNode);
          catNodes.put(cat, catNode);
        }
        catNodes.get(cat).add(new DefaultMutableTreeNode(p));
        problems.add(p);
      }
      model.nodeStructureChanged(root);
    }

    public void removeProblem(ProblemType p) {
      if (!problems.contains(p))
        return;
      String cat = p.getGroup();

      if (cat == null) {
        remove(root, p);
        problems.remove(p);
      } else {
        DefaultMutableTreeNode parent = catNodes.get(cat);
        remove(parent, p);
        problems.remove(p);
        if (parent.getChildCount() == 0) {
          catNodes.remove(cat);
          model.removeNodeFromParent(parent);
        }
      }
      // model.nodeStructureChanged(root);
    }

    private void remove(DefaultMutableTreeNode parent, ProblemType p) {
      for (int i = 0; i < parent.getChildCount(); ++i) {
        DefaultMutableTreeNode child = (DefaultMutableTreeNode) parent.getChildAt(i);
        if (child.getUserObject().equals(p)) {
          model.removeNodeFromParent(child);
        }
      }
    }
  }

  protected Properties settings;
  protected TaskTablePanel taskPanel;

  private JPanel restrictPanel;

  /** Nur Probleme der Besitzerpartei anzeigen lassen */
  private JCheckBox chkOwnerParty;
  /** Nur Probleme von Parteien anzeigen lassen, bei denen wir etwas ändern können (Passwort haben) */
  private JCheckBox chkPasswordParties;
  /** Nur Probleme der selektierten Regionen anzeigen */
  private JCheckBox chkRestrictToSelection;
  /** Nur Probleme der aktuellen Region anzeigen */
  private JCheckBox chkRestrictToActiveRegion;

  private TypeTree inspectorsList;
  private TypeTree ignoreList;

  // private TypeTree useList;

  /**
   * @param parent
   * @param settings
   * @param data
   */
  public TaskTablePreferences(TaskTablePanel parent, Properties settings, GameData data) {
    this.settings = settings;
    taskPanel = parent;

    setLayout(new BorderLayout());

    restrictPanel = new JPanel();
    restrictPanel.setBorder(new TitledBorder(BorderFactory.createEtchedBorder(), Resources
        .get("tasks.prefs.border.options")));
    restrictPanel.setLayout(new BorderLayout());

    JPanel panel = new JPanel(new GridBagLayout());
    GridBagConstraints c = new GridBagConstraints();
    c.insets = new Insets(2, 2, 2, 2);

    c.gridx = 0;
    c.gridy = 0;
    c.fill = GridBagConstraints.BOTH;
    c.anchor = GridBagConstraints.WEST;
    c.weightx = 0.0;
    chkOwnerParty = new JCheckBox(Resources.get("tasks.prefs.restricttoowner"), true);
    panel.add(chkOwnerParty, c);

    c.gridy++;
    chkPasswordParties = new JCheckBox(Resources.get("tasks.prefs.restricttopassword"), true);
    panel.add(chkPasswordParties, c);

    c.gridy++;
    chkRestrictToActiveRegion =
        new JCheckBox(Resources.get("tasks.prefs.restricttoactiveregion"), true);
    panel.add(chkRestrictToActiveRegion, c);

    c.gridy++;
    chkRestrictToSelection = new JCheckBox(Resources.get("tasks.prefs.restricttoselection"), true);
    panel.add(chkRestrictToSelection, c);

    c.gridy++;
    c.fill = GridBagConstraints.BOTH;
    c.weightx = 1;
    c.weighty = 1;
    panel.add(getInspectorPanel(), c);

    restrictPanel.add(panel, BorderLayout.CENTER);

    add(restrictPanel, BorderLayout.CENTER);

  }

  private JPanel getInspectorPanel() {
    JPanel pnlSelection = new JPanel();
    pnlSelection.setBorder(new TitledBorder(BorderFactory.createEtchedBorder(), Resources
        .get("tasks.prefs.inspectors.selection")));

    JPanel inspectorsPanel = new JPanel();
    inspectorsPanel.setLayout(new BorderLayout(0, 0));
    inspectorsPanel.setBorder(new TitledBorder(BorderFactory.createEtchedBorder(), Resources
        .get("tasks.prefs.inspectors.available")));

    inspectorsList = new TypeTree(true);

    inspectorsList.setVisibleRowCount(10);
    JScrollPane pane = new JScrollPane(inspectorsList);
    inspectorsPanel.add(pane, BorderLayout.CENTER);

    WrappableLabel description = WrappableLabel.getLabel(Resources.get("tasks.prefs.inspectors.help"));
    JPanel descPanel = new JPanel(new BorderLayout());
    descPanel.setBorder(new TitledBorder(BorderFactory.createEtchedBorder(), Resources
        .get("tasks.prefs.inspectors.description")));

    inspectorsList.addTreeSelectionListener(new TreeSelectionListener() {
      public void valueChanged(TreeSelectionEvent e) {
        DefaultMutableTreeNode node = (DefaultMutableTreeNode) e.getPath().getLastPathComponent();
        Object o = node.getUserObject();
        if (o != null && o instanceof ProblemType) {
          description.setText(((ProblemType) o).getDescription());
        } else {
          description.setText(Resources.get("tasks.prefs.inspectors.help"));
        }
      }
    });

    JPanel ignorePanel = new JPanel();
    ignorePanel.setLayout(new GridBagLayout());
    ignorePanel.setBorder(new TitledBorder(BorderFactory.createEtchedBorder(), Resources
        .get("tasks.prefs.inspectors.ignore")));

    ignoreList = new TypeTree(false);

    ignoreList.addTreeSelectionListener(new TreeSelectionListener() {
      public void valueChanged(TreeSelectionEvent e) {
        DefaultMutableTreeNode node = (DefaultMutableTreeNode) e.getPath().getLastPathComponent();
        Object o = node.getUserObject();
        if (o != null && o instanceof ProblemType) {
          description.setText(((ProblemType) o).getDescription());
        } else {
          description.setText(Resources.get("tasks.prefs.inspectors.help"));
        }
      }
    });

    JButton right = new JButton("  -->  ");
    right.addActionListener(new ActionListener() {
      /** add all selected nodes on the left to the useList */
      public void actionPerformed(ActionEvent e) {
        for (ProblemType p : inspectorsList.getSelectedProblems()) {
          ignoreList.addProblem(p);
        }
      }
    });

    JButton left = new JButton("  <--  ");
    left.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        int first = -1, candidate = -1, previous = -1, last = -1, count = 0;
        for (int i : ignoreList.getSelectionRows()) {
          if (first < 0) {
            first = i;
          }
          if (candidate < 0 && previous >= 0 && i > previous + 1) {
            candidate = previous + 1 - count;
          }
          last = i;
          previous = i;
          count++;
        }
        if (candidate < 0 && last < ignoreList.getRowCount() - 1) {
          candidate = last + 1 - count;
        }
        if (candidate < 0 && first > 0) {
          candidate = first - 1;
        }

        for (ProblemType p : ignoreList.getSelectedProblems()) {
          ignoreList.removeProblem(p);
        }
        ignoreList.setSelectionInterval(candidate, candidate);
      }
    });

    ignoreList.setVisibleRowCount(10);
    pane = new JScrollPane(ignoreList);

    pnlSelection.setLayout(new GridBagLayout());

    GridBagConstraints c =
        new GridBagConstraints(0, 0, 1, 1, 1, 1, GridBagConstraints.CENTER,
            GridBagConstraints.BOTH, new Insets(2, 2, 2, 2), 0, 0);

    c.gridheight = 4;
    ignorePanel.add(pane, c);

    c.gridheight = 1;
    c.gridx = 1;
    c.weightx = 0;
    ignorePanel.add(new JPanel(), c);

    c.gridx = 0;
    c.gridy = 0;
    c.gridheight = 4;
    c.weightx = 0.5;
    c.weighty = 0.5;
    pnlSelection.add(inspectorsPanel, c);

    c.gridx = 2;
    pnlSelection.add(ignorePanel, c);

    c.gridx = 1;
    c.gridheight = 1;
    c.weightx = 0;
    c.weighty = 1.0;
    pnlSelection.add(new JPanel(), c);

    c.gridy++;
    c.weighty = 0;

    pnlSelection.add(right, c);

    c.gridy++;

    pnlSelection.add(left, c);

    c.gridy++;
    c.weighty = 1.0;
    pnlSelection.add(new JPanel(), c);

    c.gridy = 5;
    c.gridx = 0;
    c.gridwidth = 3;
    c.weightx = 0.5;
    c.weighty = 1;
    pnlSelection.add(description, c);

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

    for (ProblemType t : taskPanel.getIgnoredProblems()) {
      ignoreList.addProblem(t);
    }

    inspectorsList.fill();
  }

  /**
   * @see magellan.client.swing.preferences.PreferencesAdapter#applyPreferences()
   */
  public void applyPreferences() {

    Set<ProblemType> result = new HashSet<ProblemType>();
    for (ProblemType p : ignoreList.getProblems()) {
      result.add(p);
    }
    // taskPanel.setActiveProblems(result);
    taskPanel.setIgnoredProblems(result);

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
