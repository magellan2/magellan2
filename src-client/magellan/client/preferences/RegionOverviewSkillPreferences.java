// class magellan.client.preferences.RegionOverviewSkillPreferences
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
import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;

import javax.swing.BorderFactory;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JButton;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListCellRenderer;
import javax.swing.ListModel;
import javax.swing.ListSelectionModel;
import javax.swing.border.TitledBorder;

import magellan.client.Client;
import magellan.client.event.EventDispatcher;
import magellan.client.swing.preferences.PreferencesAdapter;
import magellan.client.utils.ImageFactory;
import magellan.library.GameData;
import magellan.library.rules.SkillType;
import magellan.library.utils.Resources;
import magellan.library.utils.Umlaut;
import magellan.library.utils.comparator.IDComparator;
import magellan.library.utils.comparator.NameComparator;
import magellan.library.utils.comparator.SkillTypeRankComparator;

/**
 * Panel for maintainig the SkillTypeList for sorting after skillType
 *
 * @author ...
 * @version 1.0, 20.11.2007
 */
public class RegionOverviewSkillPreferences extends JPanel implements PreferencesAdapter {

  private JList<SkillType> skillList = null;
  private JButton upButton = null;
  private JButton downButton = null;
  private JButton refreshListButton = null;

  private SkillTypeComparator skillTypeComparator = null;

  private Properties settings;

  /**
   * Creates a new SkillPreferences object.
   */
  public RegionOverviewSkillPreferences(EventDispatcher dispatcher, ImageFactory imageFactory,
      Properties settings, GameData data) {
    setLayout(new BorderLayout());
    setBorder(new TitledBorder(BorderFactory.createEtchedBorder(), Resources
        .get("emapoverviewpanel.prefs.skillorder")));
    this.settings = settings;

    skillList = new JList<SkillType>();
    skillList.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
    skillList.setCellRenderer(new MyCellRenderer(data, imageFactory));
    // entries for List are updated in initPreferences

    this.add(new JScrollPane(skillList), BorderLayout.CENTER);

    JPanel buttons = new JPanel(new GridBagLayout());
    GridBagConstraints c =
        new GridBagConstraints(0, 0, 1, 1, 1, 0, GridBagConstraints.WEST,
            GridBagConstraints.HORIZONTAL, new Insets(0, 1, 2, 1), 0, 0);

    upButton = new JButton(Resources.get("emapoverviewpanel.prefs.upbutton.caption"));
    upButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        if ((skillList.getModel() == null) || (skillList.getModel().getSize() == 0))
          return;

        int selIndices[] = skillList.getSelectedIndices();

        if (selIndices.length == 0)
          return;

        List<SkillType> newData = new LinkedList<SkillType>();
        ListModel<SkillType> oldData = skillList.getModel();
        List<Integer> newSelectedIndices = new LinkedList<Integer>();

        for (int i = 0; i < oldData.getSize(); i++) {
          SkillType o = oldData.getElementAt(i);

          if (skillList.isSelectedIndex(i)) {
            int newPos;

            if ((i > 0) && !newSelectedIndices.contains(i - 1)) {
              newPos = i - 1;
            } else {
              newPos = i;
            }

            newData.add(newPos, o);
            newSelectedIndices.add(newPos);
          } else {
            newData.add(o);
          }
        }

        skillList.setListData(newData.toArray(new SkillType[0]));

        int selection[] = new int[newSelectedIndices.size()];
        int i = 0;

        for (Iterator<Integer> iter = newSelectedIndices.iterator(); iter.hasNext(); i++) {
          selection[i] = (iter.next()).intValue();
        }

        skillList.setSelectedIndices(selection);
        skillList.ensureIndexIsVisible(selection[0]);
      }
    });
    buttons.add(upButton, c);

    c.gridy++;

    downButton = new JButton(Resources.get("emapoverviewpanel.prefs.downbutton.caption"));
    downButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        if ((skillList.getModel() == null) || (skillList.getModel().getSize() == 0))
          return;

        int selIndices[] = skillList.getSelectedIndices();

        if (selIndices.length == 0)
          return;

        List<SkillType> newData = new LinkedList<SkillType>();
        ListModel<SkillType> oldData = skillList.getModel();
        List<Integer> newSelectedIndices = new LinkedList<Integer>();

        for (int i = oldData.getSize() - 1; i >= 0; i--) {
          SkillType o = oldData.getElementAt(i);

          if (skillList.isSelectedIndex(i)) {
            int newPos;

            if ((i < (oldData.getSize() - 1))
                && !newSelectedIndices.contains(Integer.valueOf(i + 1))) {
              newPos = i + 1;
              newData.add(1, o);
            } else {
              newPos = i;
              newData.add(0, o);
            }

            newSelectedIndices.add(newPos);
          } else {
            newData.add(0, o);
          }
        }

        skillList.setListData(newData.toArray(new SkillType[0]));

        int selection[] = new int[newSelectedIndices.size()];
        int i = 0;

        for (Iterator<Integer> iter = newSelectedIndices.iterator(); iter.hasNext(); i++) {
          selection[i] = (iter.next()).intValue();
        }

        skillList.setSelectedIndices(selection);
        skillList.ensureIndexIsVisible(selection[0]);
      }
    });
    buttons.add(downButton, c);

    // add a filler
    c.gridy++;
    c.fill = GridBagConstraints.BOTH;
    c.weighty = 1;
    buttons.add(new JPanel(), c);

    c.anchor = GridBagConstraints.SOUTHWEST;
    c.gridy++;
    c.fill = GridBagConstraints.HORIZONTAL;
    c.weighty = 0;
    c.insets.bottom = 0;

    final GameData fData = data;

    refreshListButton =
        new JButton(Resources.get("emapoverviewpanel.prefs.refreshlistbutton.caption"));
    refreshListButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        if ((skillList.getModel() == null) || (skillList.getModel().getSize() == 0))
          return;

        ListModel<SkillType> listData = skillList.getModel();
        List<SkillType> v = new LinkedList<SkillType>();

        for (int index = 0; index < listData.getSize(); index++) {
          v.add(listData.getElementAt(index));
        }

        if (skillTypeComparator == null) {
          skillTypeComparator = new SkillTypeComparator(fData);
        }

        Collections.sort(v, skillTypeComparator);
        skillList.setListData(v.toArray(new SkillType[0]));
      }
    });
    buttons.add(refreshListButton, c);

    this.add(buttons, BorderLayout.EAST);

    initPreferences();
  }

  /**
   * Make this editable.
   */
  @Override
  public void setEnabled(boolean enable) {
    super.setEnabled(enable);
    skillList.setEnabled(enable);
    upButton.setEnabled(enable);
    downButton.setEnabled(enable);
    refreshListButton.setEnabled(enable);
  }

  /**
   * fills the values
   * 
   * @see magellan.client.swing.preferences.PreferencesAdapter#initPreferences()
   */
  public void initPreferences() {
    GameData data = Client.INSTANCE.getData();
    if (data != null) {
      List<SkillType> v = new LinkedList<SkillType>();

      for (Iterator<SkillType> iter = data.getRules().getSkillTypeIterator(); iter.hasNext();) {
        SkillType type = iter.next();
        v.add(type);
      }

      Collections.sort(v, new SkillTypeRankComparator(new NameComparator(IDComparator.DEFAULT),
          settings));
      skillList.setListData(v.toArray(new SkillType[0]));

      if (v.size() > 0) {
        setEnabled(true);
      } else {
        setEnabled(false);
      }
    } else {
      setEnabled(false);
    }
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
    return Resources.get("emapoverviewpanel.prefs.skillorder");
  }

  /**
   * @see magellan.client.swing.preferences.PreferencesAdapter#applyPreferences()
   */
  public void applyPreferences() {
    ListModel<SkillType> listData = skillList.getModel();

    for (int index = 0; index < listData.getSize(); index++) {
      SkillType s = listData.getElementAt(index);
      settings.setProperty("ClientPreferences.compareValue." + s.getID(), String.valueOf(index));

    }
  }

  /**
   * An extra cell renderer to display the skills in the list
   * 
   * @author ...
   * @version 1.0, 20.11.2007
   */
  private static class MyCellRenderer extends DefaultListCellRenderer implements
      ListCellRenderer<Object> {
    // we need a reference to the translations
    private GameData data = null;
    // we need a reference to the ImageFactory
    private ImageFactory imageFactory = null;

    /**
     * Constructs a new extra cell renderer for our skill list
     * 
     * @param _data
     * @param _imageFactory
     */

    public MyCellRenderer(GameData _data, ImageFactory _imageFactory) {
      data = _data;
      imageFactory = _imageFactory;
    }

    /**
     * returns the JLabel to display in our skill list
     *
     * @param value value to display
     * @param index cell index
     * @param isSelected is the cell selected
     * @param cellHasFocus the list and the cell have the focus
     * @see javax.swing.ListCellRenderer#getListCellRendererComponent(javax.swing.JList,
     *      java.lang.Object, int, boolean, boolean)
     */
    @Override
    public Component getListCellRendererComponent(JList<?> list, Object value,
        int index, boolean isSelected, boolean cellHasFocus) {
      String s = value.toString();
      String normalizedIconName = Umlaut.convertUmlauts(s).toLowerCase();
      s = data.getTranslation(s);
      setText(s);
      setIcon(imageFactory.loadImageIcon(normalizedIconName));
      if (isSelected) {
        setBackground(list.getSelectionBackground());
        setForeground(list.getSelectionForeground());
      } else {
        setBackground(list.getBackground());
        setForeground(list.getForeground());
      }
      setEnabled(list.isEnabled());
      setFont(list.getFont());
      setOpaque(true);
      return this;
    }
  }

  /**
   * a small comparator to compare translated skillNames
   * 
   * @author ...
   * @version 1.0, 20.11.2007
   */
  private static class SkillTypeComparator implements Comparator<SkillType> {

    // Reference to Translations
    private GameData data = null;

    /**
     * constructs new Comparator
     * 
     * @param _data
     */
    public SkillTypeComparator(GameData _data) {
      data = _data;
    }

    public int compare(SkillType o1, SkillType o2) {
      String s1 = data.getTranslation(o1.getName());
      String s2 = data.getTranslation(o2.getName());
      return s1.compareToIgnoreCase(s2);
    }
  }

}
