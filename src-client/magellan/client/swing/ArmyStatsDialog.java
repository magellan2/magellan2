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

/*
 * ArmyStatsDialog.java
 *
 * Created on 5. März 2002, 12:00
 */
package magellan.client.swing;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Dialog;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.LayoutManager;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;
import java.util.StringTokenizer;

import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;

import magellan.client.event.EventDispatcher;
import magellan.library.GameData;
import magellan.library.Region;
import magellan.library.StringID;
import magellan.library.rules.SkillType;
import magellan.library.utils.Resources;
import magellan.library.utils.comparator.IDComparator;
import magellan.library.utils.comparator.NameComparator;
import magellan.library.utils.comparator.SkillTypeRankComparator;

/**
 * DOCUMENT ME!
 * 
 * @author Andreas
 * @version 1.0
 */
public class ArmyStatsDialog extends InternationalizedDataDialog implements ActionListener {
  protected ArmyStatsPanel panel;
  protected JCheckBox categorize;
  protected JButton excludeDialog;
  protected List<SkillType> excludeSkills;
  protected List<String> excludeNames;
  protected List<Integer> excludeCombatStates;

  /**
   * Creates new ArmyStatsDialog
   */
  public ArmyStatsDialog(Frame owner, EventDispatcher ed, GameData data, Properties settings) {
    this(owner, ed, data, settings, null);
  }

  /**
   * Creates a new ArmyStatsDialog object.
   */
  public ArmyStatsDialog(Frame owner, EventDispatcher ed, GameData data, Properties settings,
      Collection<Region> selRegions) {
    super(owner, false, ed, data, settings);

    LayoutManager lm = new FlowLayout(FlowLayout.CENTER);

    JPanel help = new JPanel(lm);
    help.add(categorize =
        new JCheckBox(Resources.get("armystatsdialog.categorized"), settings.getProperty(
            "ArmyStatsDialog.Categorize", "true").equals("true")));
    categorize.addActionListener(this);
    help.add(excludeDialog = new JButton(Resources.get("armystatsdialog.exclude") + "..."));
    excludeDialog.addActionListener(this);

    JPanel inner = new JPanel(new BorderLayout());
    inner.setBorder(BorderFactory.createEtchedBorder());

    loadExclusionData();

    inner.add(panel = new ArmyStatsPanel(ed, data, settings, categorize.isSelected(), selRegions),
        BorderLayout.CENTER);
    panel.setExcludeNames(excludeNames);
    panel.setExcludeSkills(excludeSkills);
    panel.setExcludeCombatStates(excludeCombatStates);
    panel.recreate(data);

    inner.add(help, BorderLayout.SOUTH);

    help = new JPanel(lm);

    JButton button = new JButton(Resources.get("armystatsdialog.ok"));
    button.addActionListener(this);
    help.add(button);

    getContentPane().add(inner, BorderLayout.CENTER);
    getContentPane().add(help, BorderLayout.SOUTH);

    if (settings.containsKey("ArmyStatsDialog.bounds")) {
      StringTokenizer st = new StringTokenizer(settings.getProperty("ArmyStatsDialog.bounds"), ",");

      if (st.countTokens() == 4) {
        try {
          setBounds(Integer.parseInt(st.nextToken()), Integer.parseInt(st.nextToken()), Integer
              .parseInt(st.nextToken()), Integer.parseInt(st.nextToken()));
        } catch (Exception exc) {
          setBounds(100, 100, 600, 600);
        }
      } else {
        setBounds(100, 100, 600, 600);
      }
    } else {
      setBounds(100, 100, 600, 600);
    }
  }

  protected void loadExclusionData() {
    String names = settings.getProperty("ArmyStatsDialog.ExcludedNames");

    if (names != null) {
      StringTokenizer st = new StringTokenizer(names, "~");
      excludeNames = new LinkedList<String>();

      while (st.hasMoreTokens()) {
        excludeNames.add(st.nextToken());
      }

      if (excludeNames.size() == 0) {
        excludeNames = null;
      } else {
        Collections.sort(excludeNames);
      }
    }

    String skills = settings.getProperty("ArmyStatsDialog.ExcludedSkills");

    if ((data != null) && (skills != null)) {
      StringTokenizer st = new StringTokenizer(skills, "~");
      excludeSkills = new ArrayList<SkillType>(st.countTokens());

      while (st.hasMoreTokens()) {
        String skillName = st.nextToken();
        SkillType skill = data.rules.getSkillType(StringID.create(skillName), false);

        if (skill != null) {
          excludeSkills.add(skill);
        }
      }

      if (excludeSkills.size() == 0) {
        excludeSkills = null;
      }
    }

    String states = settings.getProperty("ArmyStatsDialog.ExcludedCombatStates");

    if (states != null) {
      StringTokenizer st = new StringTokenizer(states, "~");
      excludeCombatStates = new LinkedList<Integer>();

      while (st.hasMoreTokens()) {
        try {
          excludeCombatStates.add(new Integer(st.nextToken()));
        } catch (Exception exc) {
        }
      }

      if (excludeCombatStates.size() == 0) {
        excludeCombatStates = null;
      }
    }
  }

  protected void saveExclusionData() {
    StringBuffer buf = new StringBuffer();

    if (excludeNames == null) {
      settings.remove("ArmyStatsDialog.ExcludedNames");
    } else {
      Iterator<String> it = excludeNames.iterator();

      while (it.hasNext()) {
        String s = it.next();
        buf.append(s);

        if (it.hasNext()) {
          buf.append('~');
        }
      }

      settings.setProperty("ArmyStatsDialog.ExcludedNames", buf.toString());
    }

    buf.setLength(0);

    if (excludeSkills == null) {
      settings.remove("ArmyStatsDialog.ExcludedSkills");
    } else {
      Iterator<SkillType> it = excludeSkills.iterator();

      while (it.hasNext()) {
        SkillType st = it.next();
        buf.append(st.getID().toString());

        if (it.hasNext()) {
          buf.append('~');
        }
      }

      settings.setProperty("ArmyStatsDialog.ExcludedSkills", buf.toString());
    }

    buf.setLength(0);

    if (excludeCombatStates == null) {
      settings.remove("ArmyStatsDialog.ExcludedCombatStates");
    } else {
      Iterator<Integer> it = excludeCombatStates.iterator();

      while (it.hasNext()) {
        buf.append((it.next()).intValue());

        if (it.hasNext()) {
          buf.append('~');
        }
      }

      settings.setProperty("ArmyStatsDialog.ExcludedCombatStates", buf.toString());
    }
  }

  /**
   * DOCUMENT-ME
   */
  @Override
  public void setVisible(boolean b) {
    super.setVisible(b);

    if (b) {
      panel.doUpdate();
    } else {
      panel.quit();

      StringBuffer buf = new StringBuffer();
      Rectangle rect = getBounds();
      buf.append(rect.x);
      buf.append(',');
      buf.append(rect.y);
      buf.append(',');
      buf.append(rect.width);
      buf.append(',');
      buf.append(rect.height);
      settings.setProperty("ArmyStatsDialog.bounds", buf.toString());
    }
  }

  /**
   * DOCUMENT-ME
   */
  public void actionPerformed(ActionEvent e) {
    if (e.getSource() == categorize) {
      boolean state = categorize.isSelected();
      panel.setCategorized(state);
      panel.recreate(data);
      settings.setProperty("ArmyStatsDialog.Categorize", state ? "true" : "false");
    } else if (e.getSource() == excludeDialog) {
      if (data != null) {
        ExcludeDialog ed =
            new ExcludeDialog(this, Resources.get("armystatsdialog.exclude.title"), data,
                excludeNames, excludeSkills, excludeCombatStates);
        ed.setVisible(true);
        excludeNames = ed.getExcludedNames();
        excludeSkills = ed.getExcludedSkills();
        excludeCombatStates = ed.getExcludeCombatStates();
        panel.setExcludeNames(excludeNames);
        panel.setExcludeSkills(excludeSkills);
        panel.setExcludeCombatStates(excludeCombatStates);
        panel.recreate(data);
        saveExclusionData();
      }
    } else {
      setVisible(false);
    }
  }

  protected class ExcludeDialog extends JDialog implements ActionListener {
    protected JList names;
    protected JButton close;
    protected JButton add;
    protected JCheckBox skills[];
    protected JCheckBox states[];
    protected final String KEY = "SKILLTYPE";

    /**
     * Creates a new ExcludeDialog object.
     */
    public ExcludeDialog(Dialog owner, String title, GameData data, List<String> eNames,
        List<SkillType> eSkills, List<Integer> eStates) {
      super(owner, title, true);

      javax.swing.border.Border border = BorderFactory.createEtchedBorder();

      // create names list
      DefaultListModel model = new DefaultListModel();

      if (eNames != null) {
        Iterator<String> it = eNames.iterator();

        while (it.hasNext()) {
          model.addElement(it.next());
        }
      }

      names = new JList(model);
      names.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

      JPanel nPanel = new JPanel(new BorderLayout());
      nPanel.add(new JScrollPane(names), BorderLayout.CENTER);

      JPanel inner = new JPanel(new GridBagLayout());
      GridBagConstraints con = new GridBagConstraints();
      con.gridwidth = 1;
      con.gridheight = 1;
      con.fill = GridBagConstraints.HORIZONTAL;
      con.weightx = 1;
      add = new JButton(Resources.get("armystatsdialog.add") + "...");
      add.addActionListener(this);
      inner.add(add, con);
      con.gridy = 1;

      JButton remove = new JButton(Resources.get("armystatsdialog.remove"));
      remove.addActionListener(this);
      inner.add(remove, con);
      nPanel.add(inner, BorderLayout.EAST);
      nPanel.setBorder(new javax.swing.border.TitledBorder(border, Resources
          .get("armystatsdialog.names")));

      // create skill-type check-boxes
      Iterator<SkillType> it1 = data.rules.getSkillTypeIterator();
      List<SkillType> l = new LinkedList<SkillType>();

      while (it1.hasNext()) {
        l.add(it1.next());
      }

      JPanel sPanel = new JPanel(new GridLayout(0, 1));

      if (l.size() > 0) {
        Collections.sort(l, new SkillTypeRankComparator(new NameComparator(IDComparator.DEFAULT),
            settings));
        skills = new JCheckBox[l.size()];
        it1 = l.iterator();

        int i = 0;

        while (it1.hasNext()) {
          SkillType sk = it1.next();
          skills[i] = new JCheckBox(sk.getName());

          if ((eSkills != null) && eSkills.contains(sk)) {
            skills[i].setSelected(true);
          }

          skills[i].putClientProperty(KEY, sk);
          sPanel.add(skills[i]);
          i++;
        }
      }

      JPanel statePanel = new JPanel(new GridLayout(0, 1));
      statePanel.setBorder(new javax.swing.border.TitledBorder(border, Resources
          .get("armystatsdialog.states")));
      states = new JCheckBox[7];

      for (int i = 0; i < 7; i++) {
        states[i] = new JCheckBox(Resources.get("armystatsdialog.state" + i));

        if (eStates != null) {
          Iterator<Integer> it2 = eStates.iterator();

          while (it2.hasNext()) {
            Integer integer = it2.next();

            if (integer.intValue() == (i - 1)) {
              states[i].setSelected(true);
            }
          }
        }

        statePanel.add(states[i]);
      }

      Container cont = getContentPane();
      cont.setLayout(new BorderLayout());

      JPanel help = new JPanel(new GridLayout(1, 0));
      help.add(nPanel);

      JScrollPane scroll = new JScrollPane(sPanel);
      scroll.setBorder(new javax.swing.border.TitledBorder(border, Resources
          .get("armystatsdialog.skills")));
      help.add(scroll);
      help.add(statePanel);
      cont.add(help, BorderLayout.CENTER);

      JPanel button = new JPanel(new FlowLayout(FlowLayout.CENTER));
      close = new JButton(Resources.get("armystatsdialog.close"));
      close.addActionListener(this);
      button.add(close);
      cont.add(button, BorderLayout.SOUTH);

      this.setBounds(200, 200, 10, 10);
      pack();

      if (getWidth() < 600) {
        this.setSize(600, getHeight());
      }

      if (getHeight() > 600) {
        this.setSize(getWidth(), 600);
      }

      setLocationRelativeTo(owner);
    }

    protected void addName() {
      String ret = JOptionPane.showInputDialog(this, Resources.get("armystatsdialog.newname"));

      if ((ret != null) && !ret.equals("")) {
        ((DefaultListModel) names.getModel()).addElement(ret);
      }
    }

    /**
     * DOCUMENT-ME
     */
    public void actionPerformed(java.awt.event.ActionEvent actionEvent) {
      if (actionEvent.getSource() == close) {
        setVisible(false);
      } else if (actionEvent.getSource() == add) {
        addName();

        return;
      }

      if (names.getSelectedIndex() >= 0) {
        ((DefaultListModel) names.getModel()).remove(names.getSelectedIndex());
      }
    }

    /**
     * DOCUMENT-ME
     */
    public List<String> getExcludedNames() {
      List<String> al = new ArrayList<String>(names.getModel().getSize());

      for (int i = 0; i < names.getModel().getSize(); i++) {
        al.add((String) names.getModel().getElementAt(i));
      }

      if (al.size() == 0) {
        al = null;
      }

      return al;
    }

    /**
     * DOCUMENT-ME
     */
    public List<SkillType> getExcludedSkills() {
      List<SkillType> sl = new LinkedList<SkillType>();

      if ((skills != null) && (skills.length > 0)) {
        for (JCheckBox skill : skills) {
          if (skill.isSelected()) {
            sl.add((SkillType) skill.getClientProperty(KEY));
          }
        }
      }

      if (sl.size() == 0) {
        sl = null;
      }

      return sl;
    }

    /**
     * DOCUMENT-ME
     */
    public List<Integer> getExcludeCombatStates() {
      List<Integer> sl = new LinkedList<Integer>();

      for (int i = 0; i < 7; i++) {
        if (states[i].isSelected()) {
          sl.add(new Integer(i - 1));
        }
      }

      if (sl.size() == 0) {
        sl = null;
      }

      return sl;
    }
  }
}
