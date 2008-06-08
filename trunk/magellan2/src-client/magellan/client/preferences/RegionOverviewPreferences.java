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
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.StringTokenizer;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.border.AbstractBorder;
import javax.swing.border.TitledBorder;

import magellan.client.EMapOverviewPanel;
import magellan.client.swing.preferences.ExtendedPreferencesAdapter;
import magellan.client.swing.preferences.PreferencesAdapter;
import magellan.client.swing.tree.TreeHelper;
import magellan.client.utils.TreeBuilder;
import magellan.library.GameData;
import magellan.library.io.cr.CRParser;
import magellan.library.utils.PropertiesHelper;
import magellan.library.utils.Resources;

/**
 * 
 * Provides Panel (Extended Preferences Adapter) for Preferences
 *
 * @author ...
 * @version 1.0, 20.11.2007
 */
public class RegionOverviewPreferences extends JPanel implements ExtendedPreferencesAdapter {
  

  private EMapOverviewPanel overviewPanel = null;
  private Properties settings;

  /** DOCUMENT-ME */
  public JCheckBox chkSortRegions = null;

  /**
   * TODO DOCUMENT ME! Comment for <code>chkSortShipUnderUnitParent</code>.
   */
  public JCheckBox chkSortShipUnderUnitParent = null;

  /** DOCUMENT-ME */
  public JRadioButton rdbSortRegionsCoordinates = null;

  /** DOCUMENT-ME */
  public JRadioButton rdbSortRegionsIslands = null;

  /** DOCUMENT-ME */
  public JCheckBox chkDisplayIslands = null;

  /** DOCUMENT-ME */
  public JRadioButton rdbSortUnitsUnsorted = null;

  /** DOCUMENT-ME */
  public JRadioButton rdbSortUnitsSkills = null;

  // use the best skill of the unit to sort it

  /** DOCUMENT-ME */
  public JRadioButton useBestSkill = null;

  /**
   * if true, regiontree will contain regions without own units but with
   * buildings known in it
   */
  public JCheckBox chkRegionTreeBuilder_withBuildings = null;

  /**
   * if true, regiontree will contain regions without own units but with Ships
   * known in it
   */
  public JCheckBox chkRegionTreeBuilder_withShips = null;

  /**
   * if true, regiontree will contain regions without own units but with
   * Comments known in it
   */
  public JCheckBox chkRegionTreeBuilder_withComments = null;
  
  // use the topmost skill in (selfdefined) skilltype-list to sort it

  /** DOCUMENT-ME */
  public JRadioButton useTopmostSkill = null;

  /** DOCUMENT-ME */
  public JRadioButton rdbSortUnitsNames = null;
  protected ExpandPanel ePanel;
  protected CollapsePanel cPanel;
  private RegionOverviewSkillPreferences skillSort;
  private List<PreferencesAdapter> subAdapters;
  private JList useList;
  private JList elementsList;

  /**
   * Creates a new EMapOverviewPreferences object.
   * 
   * @param settings
   *          DOCUMENT-ME
   */
  public RegionOverviewPreferences(EMapOverviewPanel parent, Properties settings, GameData data) {
    this.settings = settings;
    overviewPanel = parent;
    chkSortRegions = new JCheckBox(Resources.get("emapoverviewpanel.prefs.sortregions"));

    chkSortShipUnderUnitParent = new JCheckBox(Resources.get("emapoverviewpanel.prefs.sortShipUnderUnitParent"));

    rdbSortRegionsCoordinates = new JRadioButton(Resources.get("emapoverviewpanel.prefs.sortbycoordinates"));

    rdbSortRegionsIslands = new JRadioButton(Resources.get("emapoverviewpanel.prefs.sortbyislands"));

    ButtonGroup regionSortButtons = new ButtonGroup();
    regionSortButtons.add(rdbSortRegionsCoordinates);
    regionSortButtons.add(rdbSortRegionsIslands);

    JPanel pnlRegionSortButtons = new JPanel();
    pnlRegionSortButtons.setLayout(new BoxLayout(pnlRegionSortButtons, BoxLayout.Y_AXIS));
    pnlRegionSortButtons.setBorder(new TitledBorder(BorderFactory.createEtchedBorder(), Resources.get("emapoverviewpanel.prefs.regionsorting")));
    pnlRegionSortButtons.add(chkSortRegions);
    pnlRegionSortButtons.add(rdbSortRegionsCoordinates);
    pnlRegionSortButtons.add(rdbSortRegionsIslands);

    chkDisplayIslands = new JCheckBox(Resources.get("emapoverviewpanel.prefs.showislands"));

    chkRegionTreeBuilder_withBuildings = new JCheckBox(Resources.get("emapoverviewpanel.prefs.treebuildings"));
    chkRegionTreeBuilder_withShips = new JCheckBox(Resources.get("emapoverviewpanel.prefs.treeships"));
    chkRegionTreeBuilder_withComments = new JCheckBox(Resources.get("emapoverviewpanel.prefs.treecomments"));

    JPanel pnlTreeStructure = new JPanel();
    pnlTreeStructure.setLayout(new GridBagLayout());

    GridBagConstraints c = new GridBagConstraints(0, 0, 1, 1, 1, 1, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(2, 2, 2, 2), 0, 0);
    pnlTreeStructure.setBorder(new TitledBorder(BorderFactory.createEtchedBorder(), Resources.get("emapoverviewpanel.prefs.treeStructure")));

    JPanel elementsPanel = new JPanel();
    elementsPanel.setLayout(new BorderLayout(0, 0));
    elementsPanel.setBorder(new TitledBorder(BorderFactory.createEtchedBorder(), Resources.get("emapoverviewpanel.prefs.treeStructure.available")));

    DefaultListModel elementsListModel = new DefaultListModel();
    elementsListModel.add(TreeHelper.FACTION, Resources.get("emapoverviewpanel.prefs.treeStructure.element.faction"));
    elementsListModel.add(TreeHelper.GUISE_FACTION, Resources.get("emapoverviewpanel.prefs.treeStructure.element.guisefaction"));
    elementsListModel.add(TreeHelper.GROUP, Resources.get("emapoverviewpanel.prefs.treeStructure.element.group"));
    elementsListModel.add(TreeHelper.COMBAT_STATUS, Resources.get("emapoverviewpanel.prefs.treeStructure.element.combat"));
    elementsListModel.add(TreeHelper.HEALTH, Resources.get("emapoverviewpanel.prefs.treeStructure.element.health"));
    elementsListModel.add(TreeHelper.FACTION_DISGUISE_STATUS, Resources.get("emapoverviewpanel.prefs.treeStructure.element.factiondisguise"));
    elementsListModel.add(TreeHelper.TRUSTLEVEL, Resources.get("emapoverviewpanel.prefs.treeStructure.element.trustlevel"));
    elementsListModel.add(TreeHelper.TAGGABLE, Resources.get("emapoverviewpanel.prefs.treeStructure.element.taggable", new Object[] { CRParser.TAGGABLE_STRING }));
    elementsListModel.add(TreeHelper.TAGGABLE2, Resources.get("emapoverviewpanel.prefs.treeStructure.element.taggable", new Object[] { CRParser.TAGGABLE_STRING2 }));
    elementsListModel.add(TreeHelper.TAGGABLE3, Resources.get("emapoverviewpanel.prefs.treeStructure.element.taggable", new Object[] { CRParser.TAGGABLE_STRING3 }));
    elementsListModel.add(TreeHelper.TAGGABLE4, Resources.get("emapoverviewpanel.prefs.treeStructure.element.taggable", new Object[] { CRParser.TAGGABLE_STRING4 }));
    elementsListModel.add(TreeHelper.TAGGABLE5, Resources.get("emapoverviewpanel.prefs.treeStructure.element.taggable", new Object[] { CRParser.TAGGABLE_STRING5 }));

    elementsList = new JList(elementsListModel);

    JScrollPane pane = new JScrollPane(elementsList);
    elementsPanel.add(pane, BorderLayout.CENTER);

    JPanel usePanel = new JPanel();
    usePanel.setLayout(new GridBagLayout());
    usePanel.setBorder(new TitledBorder(BorderFactory.createEtchedBorder(), Resources.get("emapoverviewpanel.prefs.treeStructure.use")));

    useList = new JList();
    useList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    pane = new JScrollPane(useList);
    c.gridheight = 4;
    usePanel.add(pane, c);

    c.gridheight = 1;
    c.gridx = 1;
    c.weightx = 0;
    usePanel.add(new JPanel(), c);

    c.gridy++;
    c.weighty = 0;

    JButton up = new JButton(Resources.get("emapoverviewpanel.prefs.treeStructure.up"));
    up.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        int pos = useList.getSelectedIndex();
        DefaultListModel model = (DefaultListModel) useList.getModel();

        if (pos == 0) {
          return;
        }

        Object o = model.elementAt(pos);
        model.remove(pos);
        model.insertElementAt(o, pos - 1);
        useList.setSelectedIndex(pos - 1);
      }
    });
    usePanel.add(up, c);

    c.gridy++;

    JButton down = new JButton(Resources.get("emapoverviewpanel.prefs.treeStructure.down"));
    down.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        int pos = useList.getSelectedIndex();
        DefaultListModel model = (DefaultListModel) useList.getModel();

        if (pos == (model.getSize() - 1)) {
          return;
        }

        Object o = model.elementAt(pos);
        model.remove(pos);
        model.insertElementAt(o, pos + 1);
        useList.setSelectedIndex(pos + 1);
      }
    });
    usePanel.add(down, c);

    c.gridy++;
    c.weighty = 1.0;
    usePanel.add(new JPanel(), c);

    c.gridx = 0;
    c.gridy = 0;
    c.gridheight = 4;
    c.weightx = 0.5;
    c.weighty = 0.5;
    pnlTreeStructure.add(elementsPanel, c);

    c.gridx = 2;
    pnlTreeStructure.add(usePanel, c);

    c.gridx = 1;
    c.gridheight = 1;
    c.weightx = 0;
    c.weighty = 1.0;
    pnlTreeStructure.add(new JPanel(), c);

    c.gridy++;
    c.weighty = 0;

    JButton right = new JButton("  -->  ");
    right.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        Object selection[] = elementsList.getSelectedValues();
        DefaultListModel model = (DefaultListModel) useList.getModel();

        for (int i = 0; i < selection.length; i++) {
          if (!model.contains(selection[i])) {
            model.add(model.getSize(), selection[i]);
          }
        }
      }
    });
    pnlTreeStructure.add(right, c);

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
    pnlTreeStructure.add(left, c);

    c.gridy++;
    c.weighty = 1;
    pnlTreeStructure.add(new JPanel(), c);

    // Unit sorting
    rdbSortUnitsUnsorted = new JRadioButton(Resources.get("emapoverviewpanel.prefs.reportorder"));
    rdbSortUnitsUnsorted.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        skillSort.setEnabled(false);
        useBestSkill.setEnabled(false);
        useTopmostSkill.setEnabled(false);
      }
    });

    rdbSortUnitsSkills = new JRadioButton(Resources.get("emapoverviewpanel.prefs.sortbyskills"));
    rdbSortUnitsSkills.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        skillSort.setEnabled(true);
        useBestSkill.setEnabled(true);
        useTopmostSkill.setEnabled(true);
      }
    });

    useBestSkill = new JRadioButton(Resources.get("emapoverviewpanel.prefs.usebestskill"));
    useTopmostSkill = new JRadioButton(Resources.get("emapoverviewpanel.prefs.usetopmostskill"));

    ButtonGroup whichSkillToUse = new ButtonGroup();
    whichSkillToUse.add(useBestSkill);
    whichSkillToUse.add(useTopmostSkill);
    rdbSortUnitsNames = new JRadioButton(Resources.get("emapoverviewpanel.prefs.sortbynames"));
    rdbSortUnitsNames.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        skillSort.setEnabled(false);
        useBestSkill.setEnabled(false);
        useTopmostSkill.setEnabled(false);
      }
    });

    ButtonGroup unitsSortButtons = new ButtonGroup();
    unitsSortButtons.add(rdbSortUnitsUnsorted);
    unitsSortButtons.add(rdbSortUnitsSkills);
    unitsSortButtons.add(rdbSortUnitsNames);

    JPanel pnlUnitSort = new JPanel();
    pnlUnitSort.setLayout(new GridBagLayout());
    c = new GridBagConstraints(0, 0, 1, 1, 0.1, 0.1, GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0);
    pnlUnitSort.setBorder(new TitledBorder(BorderFactory.createEtchedBorder(), Resources.get("emapoverviewpanel.prefs.unitsorting")));
    pnlUnitSort.add(rdbSortUnitsUnsorted, c);
    c.gridy = 1;
    pnlUnitSort.add(rdbSortUnitsSkills, c);
    c.gridy = 2;
    c.insets = new Insets(0, 30, 0, 0);
    pnlUnitSort.add(useBestSkill, c);
    c.gridy = 3;
    pnlUnitSort.add(useTopmostSkill, c);
    c.gridy = 4;
    c.ipadx = 0;
    c.insets = new Insets(0, 0, 0, 0);
    pnlUnitSort.add(rdbSortUnitsNames, c);

    this.setLayout(new GridBagLayout());
    c.anchor = GridBagConstraints.CENTER;
    c.gridx = 0;
    c.gridy = 0;
    c.gridwidth = 1;
    c.gridheight = 1;
    c.fill = GridBagConstraints.HORIZONTAL;
    c.weightx = 1.0;
    c.weighty = 0.0;
    this.add(pnlRegionSortButtons, c);

    c.anchor = GridBagConstraints.WEST;
    c.gridy++;
    c.insets.left = 10;
    c.fill = GridBagConstraints.NONE;
    c.weightx = 0.0;
    this.add(chkDisplayIslands, c);

    c.anchor = GridBagConstraints.WEST;
    c.gridy++;
    c.insets.left = 10;
    c.fill = GridBagConstraints.NONE;
    c.weightx = 0.0;
    this.add(chkSortShipUnderUnitParent, c);

    c.anchor = GridBagConstraints.WEST;
    c.gridy++;
    c.insets.left = 10;
    c.fill = GridBagConstraints.NONE;
    c.weightx = 0.0;
    this.add(chkRegionTreeBuilder_withBuildings, c);

    c.anchor = GridBagConstraints.WEST;
    c.gridy++;
    c.insets.left = 10;
    c.fill = GridBagConstraints.NONE;
    c.weightx = 0.0;
    this.add(chkRegionTreeBuilder_withShips, c);

    c.anchor = GridBagConstraints.WEST;
    c.gridy++;
    c.insets.left = 10;
    c.fill = GridBagConstraints.NONE;
    c.weightx = 0.0;
    this.add(chkRegionTreeBuilder_withComments, c);

    c.insets.left = 0;
    c.anchor = GridBagConstraints.CENTER;
    c.gridy++;
    c.fill = GridBagConstraints.HORIZONTAL;
    c.weightx = 1.0;
    this.add(pnlTreeStructure, c);

    c.gridy++;
    this.add(pnlUnitSort, c);

    JPanel help = new JPanel(new GridLayout(1, 2));
    help.setBorder(new TitledBorder(BorderFactory.createEtchedBorder(), Resources.get("emapoverviewpanel.prefs.expand.title")));
    help.add(ePanel = new ExpandPanel()); // , BorderLayout.WEST);
    help.add(cPanel = new CollapsePanel()); // , BorderLayout.EAST);
    c.gridy++;
    this.add(help, c);

    subAdapters = new ArrayList<PreferencesAdapter>(1);
    subAdapters.add(skillSort =
        new RegionOverviewSkillPreferences(parent.getEventDispatcher(), parent.getEventDispatcher()
            .getMagellanContext().getImageFactory(), settings, data));
  }

  
  
  
  /*
   * (non-Javadoc)
   * 
   * @see com.eressea.swing.preferences.PreferencesAdapter#initPreferences()
   */
  public void initPreferences() {
    chkSortRegions.setSelected(PropertiesHelper.getBoolean(settings, "EMapOverviewPanel.sortRegions", true));
    chkSortShipUnderUnitParent.setSelected(PropertiesHelper.getBoolean(settings, "EMapOverviewPanel.sortShipUnderUnitParent", true));

    chkRegionTreeBuilder_withBuildings.setSelected(PropertiesHelper.getBoolean(settings, "EMapOverviewPanel.treeBuilderWithBuildings", true));
    chkRegionTreeBuilder_withShips.setSelected(PropertiesHelper.getBoolean(settings, "EMapOverviewPanel.treeBuilderWithShips", true));
    chkRegionTreeBuilder_withComments.setSelected(PropertiesHelper.getBoolean(settings, "EMapOverviewPanel.treeBuilderWithComments", true));

    rdbSortRegionsCoordinates.setSelected(settings.getProperty("EMapOverviewPanel.sortRegionsCriteria", "coordinates").equals("coordinates"));
    rdbSortRegionsIslands.setSelected(settings.getProperty("EMapOverviewPanel.sortRegionsCriteria", "coordinates").equals("islands"));
    chkDisplayIslands.setSelected(PropertiesHelper.getBoolean(settings, "EMapOverviewPanel.displayIslands", true));

    String criteria = settings.getProperty("EMapOverviewPanel.treeStructure", " " + TreeHelper.FACTION + " " + TreeHelper.GROUP);

    DefaultListModel model2 = new DefaultListModel();

    for (StringTokenizer tokenizer = new StringTokenizer(criteria); tokenizer.hasMoreTokens();) {
      String s = tokenizer.nextToken();
      try {
        int i = Integer.parseInt(s);

        try {
          model2.add(model2.size(), elementsList.getModel().getElementAt(i));
        } catch (ArrayIndexOutOfBoundsException e) {
          model2.add(model2.size(), "unknown");
        }
      } catch (NumberFormatException e) {
      }
    }
    useList.setModel(model2);

    rdbSortUnitsUnsorted.setSelected(settings.getProperty("EMapOverviewPanel.sortUnitsCriteria", "skills").equals("unsorted"));
    rdbSortUnitsSkills.setSelected(settings.getProperty("EMapOverviewPanel.sortUnitsCriteria", "skills").equals("skills"));
    useBestSkill.setSelected(PropertiesHelper.getBoolean(settings, "EMapOverviewPanel.useBestSkill", true));
    useTopmostSkill.setSelected(!useBestSkill.isSelected());

    rdbSortUnitsNames.setSelected(settings.getProperty("EMapOverviewPanel.sortUnitsCriteria", "skills").equals("names"));

    // FIXME (stm) this is strictly not necessary
    skillSort.initPreferences();
  }

  /**
   * DOCUMENT-ME
   */
  public void applyPreferences() {
    settings.setProperty("EMapOverviewPanel.sortRegions", String.valueOf(chkSortRegions.isSelected()));

    settings.setProperty("EMapOverviewPanel.sortShipUnderUnitParent", String.valueOf(chkSortShipUnderUnitParent.isSelected()));

    settings.setProperty("EMapOverviewPanel.treeBuilderWithBuildings", String.valueOf(chkRegionTreeBuilder_withBuildings.isSelected()));

    settings.setProperty("EMapOverviewPanel.treeBuilderWithShips", String.valueOf(chkRegionTreeBuilder_withShips.isSelected()));

    settings.setProperty("EMapOverviewPanel.treeBuilderWithComments", String.valueOf(chkRegionTreeBuilder_withComments.isSelected()));

    // workaround to support EMapOverviewPanel.filters
    int newFilter = TreeBuilder.UNITS;
    if (chkRegionTreeBuilder_withBuildings.isSelected())
      newFilter = newFilter | TreeBuilder.BUILDINGS;
    if (chkRegionTreeBuilder_withShips.isSelected())
      newFilter = newFilter | TreeBuilder.SHIPS;
    if (chkRegionTreeBuilder_withComments.isSelected())
      newFilter = newFilter | TreeBuilder.COMMENTS;

    settings.setProperty("EMapOverviewPanel.filters", String.valueOf(newFilter));

    if (rdbSortRegionsCoordinates.isSelected()) {
      settings.setProperty("EMapOverviewPanel.sortRegionsCriteria", "coordinates");
    } else if (rdbSortRegionsIslands.isSelected()) {
      settings.setProperty("EMapOverviewPanel.sortRegionsCriteria", "islands");
    }

    settings.setProperty("EMapOverviewPanel.displayIslands", String.valueOf(chkDisplayIslands.isSelected()));

    if (rdbSortUnitsUnsorted.isSelected()) {
      settings.setProperty("EMapOverviewPanel.sortUnitsCriteria", "unsorted");
    } else if (rdbSortUnitsSkills.isSelected()) {
      settings.setProperty("EMapOverviewPanel.sortUnitsCriteria", "skills");
    } else if (rdbSortUnitsNames.isSelected()) {
      settings.setProperty("EMapOverviewPanel.sortUnitsCriteria", "names");
    }

    settings.setProperty("EMapOverviewPanel.useBestSkill", String.valueOf(useBestSkill.isSelected()));

    DefaultListModel useListModel = (DefaultListModel) useList.getModel();
    StringBuffer definition = new StringBuffer("");

    DefaultListModel elementsListModel = (DefaultListModel) elementsList.getModel();
    for (int i = 0; i < useListModel.getSize(); i++) {
      String s = (String) useListModel.getElementAt(i);

      int pos = elementsListModel.indexOf(s);
      definition.append(pos).append(" ");
    }

    settings.setProperty("EMapOverviewPanel.treeStructure", definition.toString());

    ePanel.apply();
    cPanel.apply();

    // We have to assure, that SkillPreferences.applyPreferences is called
    // before we rebuild the tree, i.e. before we call gameDataChanged().
    skillSort.applyPreferences();

    overviewPanel.rebuildTree();
  }
  
  /**
   * Saves the settings about the collapsing
   */
  protected void saveCollapseProperty() {
    settings.setProperty("EMapOverviewPanel.CollapseMode", String.valueOf(overviewPanel.getCollapseMode()));
  }
  
  /**
   * Saves the settings about the expanding
   */
  protected void saveExpandProperties() {
    settings.setProperty("EMapOverviewPanel.ExpandMode", String.valueOf(overviewPanel.getExpandMode()));
    settings.setProperty("EMapOverviewPanel.ExpandTrustlevel", String.valueOf(overviewPanel.getExpandTrustLevel()));
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
    return Resources.get("emapoverviewpanel.prefs.title");
  }

  /**
   * @see magellan.client.swing.preferences.ExtendedPreferencesAdapter#getChildren()
   */
  public List<PreferencesAdapter> getChildren() {
    return subAdapters;
  }

  /**
   *
   */
  protected class ExpandPanel extends JPanel implements ActionListener {
    protected JRadioButton radioButtons[];
    protected JCheckBox checkBox;
    protected JTextField trustlevel;

    /**
     * Creates a new ExpandPanel object.
     */
    public ExpandPanel() {
      super(new GridBagLayout());

      GridBagConstraints con = new GridBagConstraints(0, 0, 1, 1, 1, 0, GridBagConstraints.NORTHWEST, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0);
      radioButtons = new JRadioButton[3];

      ButtonGroup group = new ButtonGroup();

      boolean expanded = (overviewPanel.getExpandMode() & EMapOverviewPanel.EXPAND_FLAG) != 0;

      radioButtons[0] = new JRadioButton(Resources.get("emapoverviewpanel.prefs.expand.none"), !expanded);
      group.add(radioButtons[0]);
      this.add(radioButtons[0], con);

      con.gridy++;
      con.fill = GridBagConstraints.HORIZONTAL;
      this.add(new JSeparator(JSeparator.HORIZONTAL), con);
      con.fill = GridBagConstraints.NONE;

      radioButtons[1] = new JRadioButton(Resources.get("emapoverviewpanel.prefs.expand.faction"), (expanded && ((overviewPanel.getExpandMode() >> 2) == 0)));
      group.add(radioButtons[1]);
      con.gridy++;
      this.add(radioButtons[1], con);

      radioButtons[2] = new JRadioButton(Resources.get("emapoverviewpanel.prefs.expand.full"), (expanded && ((overviewPanel.getExpandMode() >> 2) == 3)));
      group.add(radioButtons[2]);
      con.gridy++;
      this.add(radioButtons[2], con);

      trustlevel = new JTextField(String.valueOf(overviewPanel.getExpandTrustLevel()), 3);
      trustlevel.setEnabled(radioButtons[2].isSelected());

      JPanel help = new JPanel();
      help.add(Box.createRigidArea(new Dimension(20, 5)));

      String s = Resources.get("emapoverviewpanel.prefs.expand.trustlevel");
      int index = s.indexOf("#T");

      if (index == 0) {
        help.add(trustlevel);
        help.add(new JLabel(s.substring(2)));
      } else if ((index == -1) || (index == (s.length() - 2))) {
        if (index != -1) {
          s = s.substring(0, index);
        }

        help.add(new JLabel(s));
        help.add(trustlevel);
      } else {
        help.add(new JLabel(s.substring(0, index)));
        help.add(trustlevel);
        help.add(new JLabel(s.substring(index + 2)));
      }

      con.gridy++;
      this.add(help, con);

      con.gridy++;
      checkBox =
          new JCheckBox(Resources.get("emapoverviewpanel.prefs.expand.ifinside"), (overviewPanel
              .getExpandMode() & EMapOverviewPanel.EXPAND_IFINSIDE_FLAG) != 0);
      checkBox.setEnabled(expanded);
      this.add(checkBox, con);

      registerListener();
    }

    protected void registerListener() {
      for (int i = 0; i < radioButtons.length; i++) {
        radioButtons[i].addActionListener(this);
      }
    }

    /**
     * DOCUMENT-ME
     */
    public void apply() {
      if (radioButtons[0].isSelected()) {
        overviewPanel.setExpandMode(overviewPanel.getExpandMode() & (0xFFFFFFFF ^ EMapOverviewPanel.EXPAND_FLAG));
      } else {
        overviewPanel.setExpandMode(overviewPanel.getExpandMode() | EMapOverviewPanel.EXPAND_FLAG);
      }

      if (checkBox.isSelected()) {
        overviewPanel.setExpandMode(overviewPanel.getExpandMode() | EMapOverviewPanel.EXPAND_IFINSIDE_FLAG);
      } else {
        overviewPanel.setExpandMode(overviewPanel.getExpandMode() & (0xFFFFFFFF ^ EMapOverviewPanel.EXPAND_IFINSIDE_FLAG));
      }

      int i = overviewPanel.getExpandMode() >> 2;

      if (radioButtons[1].isSelected()) {
        i = 0;
      } else if (radioButtons[2].isSelected()) {
        i = 3;
      }

      overviewPanel.setExpandMode((overviewPanel.getExpandMode() & (EMapOverviewPanel.EXPAND_FLAG | EMapOverviewPanel.EXPAND_IFINSIDE_FLAG)) | (i << 2));

      try {
        overviewPanel.setExpandTrustLevel(Integer.parseInt(trustlevel.getText()));
      } catch (NumberFormatException nfe) {
      }

      saveExpandProperties();
    }

    /**
     * DOCUMENT-ME
     * 
     * @param actionEvent
     *          DOCUMENT-ME
     */
    public void actionPerformed(java.awt.event.ActionEvent actionEvent) {
      checkBox.setEnabled(actionEvent.getSource() != radioButtons[0]);
      trustlevel.setEnabled(actionEvent.getSource() == radioButtons[2]);
    }
  }

  protected class CollapsePanel extends JPanel implements ActionListener {
    protected JRadioButton radioButtons[];
    protected JCheckBox checkBox;

    /**
     * Creates a new CollapsePanel object.
     */
    public CollapsePanel() {
      super(new GridBagLayout());

      this.setBorder(new LeftBorder());

      GridBagConstraints con = new GridBagConstraints(0, 0, 1, 1, 1, 0, GridBagConstraints.NORTHWEST, GridBagConstraints.NONE, new Insets(0, 3, 0, 0), 0, 0);
      radioButtons = new JRadioButton[3];

      ButtonGroup group = new ButtonGroup();

      boolean collapse = (overviewPanel.getCollapseMode() & EMapOverviewPanel.COLLAPSE_FLAG) != 0;

      radioButtons[0] = new JRadioButton(Resources.get("emapoverviewpanel.prefs.collapse.none"), !collapse);
      group.add(radioButtons[0]);
      this.add(radioButtons[0], con);

      con.gridy++;
      con.fill = GridBagConstraints.HORIZONTAL;
      con.insets.left = 0;
      this.add(new JSeparator(JSeparator.HORIZONTAL), con);
      con.insets.left = 3;
      con.fill = GridBagConstraints.NONE;

      radioButtons[1] = new JRadioButton(Resources.get("emapoverviewpanel.prefs.collapse.faction"), (collapse && ((overviewPanel.getCollapseMode() >> 2) == 0)));
      group.add(radioButtons[1]);
      con.gridy++;
      this.add(radioButtons[1], con);

      radioButtons[2] = new JRadioButton(Resources.get("emapoverviewpanel.prefs.collapse.full"), (collapse && ((overviewPanel.getCollapseMode() >> 2) == 3)));
      group.add(radioButtons[2]);
      con.gridy++;
      this.add(radioButtons[2], con);

      con.gridy++;
      checkBox = new JCheckBox(Resources.get("emapoverviewpanel.prefs.collapse.onlyautoexpanded"), (overviewPanel.getCollapseMode() & EMapOverviewPanel.COLLAPSE_ONLY_EXPANDED) != 0);
      this.add(checkBox, con);

      // to make it equally high to ePanel
      con.gridy++;
      this.add(Box.createVerticalStrut(checkBox.getPreferredSize().height + 5), con);

      /*
       * con.gridx = 0; con.gridheight = con.gridy + 1; con.gridy = 0;
       * con.fill = GridBagConstraints.VERTICAL; JComponent c = new
       * JSeparator(JSeparator.VERTICAL); c.setMaximumSize(new Dimension(3,
       * 1000)); this.add(c, con);
       */
      registerListener();
    }

    protected void registerListener() {
      for (int i = 0; i < radioButtons.length; i++) {
        radioButtons[i].addActionListener(this);
      }
    }

    /**
     * DOCUMENT-ME
     * 
     * @param actionEvent
     *          DOCUMENT-ME
     */
    public void actionPerformed(java.awt.event.ActionEvent actionEvent) {
      checkBox.setEnabled(actionEvent.getSource() != radioButtons[0]);
    }

    /**
     * DOCUMENT-ME
     */
    public void apply() {
      if (radioButtons[0].isSelected()) {
        overviewPanel.setCollapseMode(overviewPanel.getCollapseMode() & (0xFFFFFFFF ^ EMapOverviewPanel.COLLAPSE_FLAG));
      } else {
        overviewPanel.setCollapseMode(overviewPanel.getCollapseMode() | EMapOverviewPanel.COLLAPSE_FLAG);
      }

      if (checkBox.isSelected()) {
        overviewPanel.setCollapseMode(overviewPanel.getCollapseMode() | EMapOverviewPanel.COLLAPSE_ONLY_EXPANDED);
      } else {
        overviewPanel.setCollapseMode(overviewPanel.getCollapseMode() & (0xFFFFFFFF ^ EMapOverviewPanel.COLLAPSE_ONLY_EXPANDED));
      }

      int i = overviewPanel.getCollapseMode() >> 2;

      if (radioButtons[1].isSelected()) {
        i = 0;
      } else if (radioButtons[2].isSelected()) {
        i = 3;
      }

      overviewPanel.setCollapseMode((overviewPanel.getCollapseMode() & (EMapOverviewPanel.COLLAPSE_FLAG | EMapOverviewPanel.COLLAPSE_ONLY_EXPANDED)) | (i << 2));

      saveCollapseProperty();
    }

    protected class LeftBorder extends AbstractBorder {
      protected JSeparator sep;

      /**
       * Creates a new LeftBorder object.
       */
      public LeftBorder() {
        sep = new JSeparator(JSeparator.VERTICAL);
      }

      /**
       * DOCUMENT-ME
       */
      public Insets getBorderInsets(Component c) {
        return getBorderInsets(c, null);
      }

      /**
       * DOCUMENT-ME
       */
      public Insets getBorderInsets(Component c, Insets in) {
        if (in == null) {
          in = new Insets(0, 0, 0, 0);
        }

        in.top = 0;
        in.bottom = 0;
        in.right = 0;
        in.left = sep.getPreferredSize().width;

        return in;
      }

      /**
       * DOCUMENT-ME
       */
      public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
        sep.setBounds(x, y, width, height);
        SwingUtilities.paintComponent(g, sep, new JPanel(), x, y, width, height);
      }
    }
  }
}
