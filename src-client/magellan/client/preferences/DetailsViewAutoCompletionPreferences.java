// class magellan.client.preferences.DetailsViewAutoCompletionPreferences
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
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.border.BevelBorder;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import magellan.client.completion.AutoCompletion;
import magellan.client.swing.InternationalizedDialog;
import magellan.client.swing.MagellanFocusTraversalPolicy;
import magellan.client.swing.completion.CompletionGUI;
import magellan.client.swing.preferences.PreferencesAdapter;
import magellan.client.utils.KeyTextField;
import magellan.client.utils.SwingUtils;
import magellan.library.utils.CollectionFactory;
import magellan.library.utils.PropertiesHelper;
import magellan.library.utils.Resources;
import magellan.library.utils.SelfCleaningProperties;

/**
 * @author ...
 * @version 1.0, 15.02.2008
 */
public class DetailsViewAutoCompletionPreferences extends JPanel implements PreferencesAdapter {
  protected AutoCompletion source;
  protected JCheckBox cEnable;
  protected JCheckBox cPopup;
  protected JCheckBox cHotKey;
  protected JCheckBox cLimitMakeCompletion;
  protected JComboBox<CompletionGUI> cForGUIs;
  protected JTextField tTime;
  protected KeyTextField keyFields[];

  // a copy of AutoCompletion.selfDefinedCompletions
  private Map<String, String> selfDefinedCompletions;

  /**
   * Creates a new DetailAutoCompletionPreferencesAdapter object.
   */
  public DetailsViewAutoCompletionPreferences(AutoCompletion s) {
    source = s;
    setLayout(new BorderLayout());
    JPanel panel = new JPanel(new GridBagLayout());

    GridBagConstraints c =
        new GridBagConstraints(0, 0, 2, 1, 0.1, 1.0, GridBagConstraints.WEST,
            GridBagConstraints.HORIZONTAL, new Insets(2, 2, 2, 2), 0, 0);

    panel.add(new JPanel(), c);

    c.gridwidth = 1;
    c.weighty = 0;
    c.gridy++;
    cEnable =
        new JCheckBox(Resources.get("completion.autocompletion.prefs.autocompletion"), source
            .isEnableAutoCompletion());
    panel.add(cEnable, c);

    c.gridy++;
    cLimitMakeCompletion =
        new JCheckBox(Resources.get("completion.autocompletion.prefs.limitmakecompletion"), source
            .getLimitMakeCompletion());
    cLimitMakeCompletion.setToolTipText(Resources
        .get("completion.autocompletion.prefs.limitmakecompletion.tooltip"));
    panel.add(cLimitMakeCompletion, c);

    cPopup =
        new JCheckBox(Resources.get("completion.autocompletion.prefs.stubmode"), source
            .getEmptyStubMode());
    c.gridy++;
    panel.add(cPopup, c);

    cHotKey =
        new JCheckBox(Resources.get("completion.autocompletion.prefs.hotkeymode"), source
            .getHotKeyMode());
    c.gridy++;
    panel.add(cHotKey, c);

    c.gridy++;

    JPanel inner = new JPanel(new GridBagLayout());
    GridBagConstraints con2 =
        new GridBagConstraints(0, 0, 1, 1, 0, 0, GridBagConstraints.WEST,
            GridBagConstraints.HORIZONTAL, c.insets, 0, 0);

    inner.add(new JLabel(Resources.get("completion.autocompletion.prefs.time")), con2);
    con2.gridy++;
    inner.add(new JLabel(Resources.get("completion.autocompletion.prefs.gui") + ":"), con2);

    con2.gridx = 1;
    con2.gridy = 0;
    con2.weightx = 1;
    tTime = new JTextField(String.valueOf(source.getActivationTime()), 5);
    inner.add(tTime, con2);
    con2.gridy++;

    cForGUIs = new JComboBox<CompletionGUI>(source.getCompletionGUIs());
    cForGUIs.setEditable(false);

    if (source.getCurrentGUI() != null) {
      cForGUIs.setSelectedItem(source.getCurrentGUI());
    }

    inner.add(cForGUIs, con2);

    panel.add(inner, c);

    Component keys = createKeyComponents(s);
    c.gridx = 1;
    c.gridy = 1;
    c.weightx = 1;
    c.gridheight = 4;
    panel.add(keys, c);

    /**
     * Code for self defined completions.
     */

    // make a copy of the self defined completions that will be
    // written back in applyPreferences()
    selfDefinedCompletions =
        CollectionFactory.<String, String> createSyncOrderedMap(source
            .getSelfDefinedCompletionsMap());

    c.gridx = 0;
    c.gridy = 6;
    c.gridheight = 1;
    c.gridwidth = 2;
    c.weighty = 0;
    c.insets = new Insets(10, 0, 10, 0);
    panel.add(new JSeparator(SwingConstants.HORIZONTAL), c);

    JPanel sDCPanel = getSelfDefinedCompletionPanel();
    c.gridy++;
    c.weighty = 0.2;
    c.fill = GridBagConstraints.BOTH;
    c.insets = new Insets(0, 0, 0, 0);
    panel.add(sDCPanel, c);

    c.weighty = 1;
    c.gridy++;
    panel.add(new JPanel(), c);

    add(panel, BorderLayout.NORTH);
  }

  private JPanel getSelfDefinedCompletionPanel() {
    JPanel sDCPanel = new JPanel();
    sDCPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
    sDCPanel.setLayout(new GridBagLayout());

    GridBagConstraints c =
        new GridBagConstraints(0, 0, 1, 1, 0.5, 0, GridBagConstraints.CENTER,
            GridBagConstraints.BOTH, new Insets(2, 2, 2, 2), 0, 0);
    sDCPanel.add(new JLabel(Resources
        .get("completion.autocompletion.prefs.SelfDefinedCompletions.title")
        + ":"), c);

    final JLabel completionValue = new JLabel();
    JScrollPane temp = new JScrollPane(completionValue);
    temp.setBorder(new CompoundBorder(new TitledBorder(BorderFactory.createEtchedBorder(),
        Resources
            .get("completion.autocompletion.prefs.SelfDefinedCompletions.completionValue.title")),
        new EmptyBorder(5, 5, 5, 5)));
    c.gridx = 1;
    c.gridy = 1;
    c.weighty = 0.5;
    sDCPanel.add(temp, c);

    List<String> l = new LinkedList<String>(selfDefinedCompletions.keySet());
    Collections.sort(l);

    DefaultListModel<String> listModel = new DefaultListModel<String>();

    for (String string : l) {
      listModel.addElement(string);
    }

    final JList<String> completionNames = new JList<String>(listModel);
    completionNames.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    completionNames.addListSelectionListener(new ListSelectionListener() {
      public void valueChanged(ListSelectionEvent e) {
        if (!e.getValueIsAdjusting()) {
          String str = completionNames.getSelectedValue();
          StringBuilder display = new StringBuilder();

          if (str != null) {
            str = selfDefinedCompletions.get(str);
            display.append("<html><b><p>");

            for (int i = 0; i < str.length(); i++) {
              if (str.charAt(i) == '\n') {
                display.append("</p><p>");
              } else {
                display.append(str.charAt(i));
              }
            }

            display.append("</p></b></html>");
          }

          completionValue.setText(display.toString());
        }
      }
    });

    if (completionNames.getModel().getSize() > 0) {
      completionNames.setSelectedIndex(0);
    }

    temp = new JScrollPane(completionNames);
    temp.setBorder(new TitledBorder(BorderFactory.createBevelBorder(BevelBorder.LOWERED), Resources
        .get("completion.autocompletion.prefs.SelfDefinedCompletions.completionNames.title")));
    c.gridx = 0;
    c.gridheight = 3;
    sDCPanel.add(temp, c);

    final JButton delete =
        new JButton(Resources
            .get("completion.autocompletion.prefs.SelfDefinedCompletions.deleteButton.caption"));
    delete.setMnemonic(Resources.get(
        "completion.autocompletion.prefs.SelfDefinedCompletions.deleteButton.mnemonic").charAt(0));

    if (completionNames.getModel().getSize() == 0) {
      delete.setEnabled(false);
    }

    delete.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        int index = completionNames.getSelectedIndex();
        String name = completionNames.getSelectedValue();
        ((DefaultListModel<String>) completionNames.getModel()).remove(index);

        if (completionNames.getModel().getSize() == 0) {
          delete.setEnabled(false);
          completionValue.setText("");
        } else if (completionNames.getModel().getSize() > index) {
          completionNames.setSelectedIndex(index);
        } else {
          completionNames.setSelectedIndex(index - 1);
        }

        completionNames.repaint();
        selfDefinedCompletions.remove(name);
      }
    });

    final JButton newCompletion =
        new JButton(
            Resources
                .get("completion.autocompletion.prefs.SelfDefinedCompletions.newCompletionButton.caption"));
    newCompletion.setMnemonic(Resources.get(
        "completion.autocompletion.prefs.SelfDefinedCompletions.newCompletionButton.mnemonic")
        .charAt(0));
    newCompletion.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        String nameAndValue[] =
            (new DefineCompletionDialog(JOptionPane.getFrameForComponent(newCompletion)))
                .getNewCompletionNameAndValue();
        String name = nameAndValue[0];
        String value = nameAndValue[1];

        if (!name.equals("") && !value.equals("")) {
          selfDefinedCompletions.put(name, value);

          DefaultListModel<String> lstModel = (DefaultListModel<String>) completionNames.getModel();

          if (!lstModel.contains(name)) {
            lstModel.addElement(name);
          }

          completionNames.clearSelection();
          completionNames.setSelectedIndex(lstModel.indexOf(name));
          completionNames.repaint();
          delete.setEnabled(true);
        }
      }
    });

    c.gridx = 1;
    c.gridy = 2;
    c.gridheight = 1;
    c.fill = GridBagConstraints.HORIZONTAL;
    c.weighty = 0;
    sDCPanel.add(newCompletion, c);

    c.gridy = 3;
    sDCPanel.add(delete, c);

    return sDCPanel;
  }

  protected Component createKeyComponents(AutoCompletion s) {
    JPanel p = new JPanel(new java.awt.GridBagLayout());
    java.awt.GridBagConstraints c =
        new java.awt.GridBagConstraints(0, 0, 1, 1, 0, 0, GridBagConstraints.WEST,
            GridBagConstraints.HORIZONTAL, new Insets(2, 2, 2, 2), 0, 0);

    for (int i = 0; i < AutoCompletion.numKeys; i++) {
      c.gridy = i;
      p.add(new JLabel(Resources.get("completion.autocompletion.prefs.keys." + String.valueOf(i))),
          c);
    }

    c.gridx = 1;
    keyFields = new KeyTextField[AutoCompletion.numKeys];

    KeyStroke[] ck = s.getCompleterKeys();

    for (int i = 0; i < AutoCompletion.numKeys; i++) {
      c.gridy = i;
      keyFields[i] = new KeyTextField(20);
      keyFields[i].init(ck[i]);
      p.add(keyFields[i], c);
    }

    return p;
  }

  /**
   * Adds a GUI mode
   */
  public void addCompletionGUI(CompletionGUI cGUI) {
    cForGUIs.addItem(cGUI);
    revalidate();
    doLayout();
  }

  /**
   * Selects a GUI mode.
   */
  public void setCurrentGUI(CompletionGUI cGUI) {
    cForGUIs.setSelectedItem(cGUI);
  }

  /**
   * Returns this.
   * 
   * @see magellan.client.swing.preferences.PreferencesAdapter#getComponent()
   */
  public Component getComponent() {
    return this;
  }

  /**
   * @see magellan.client.swing.preferences.PreferencesAdapter#getTitle()
   */
  public String getTitle() {
    return Resources.get("completion.autocompletion.prefs.title");
  }

  /**
   * @see magellan.client.swing.preferences.PreferencesAdapter#initPreferences()
   */
  public void initPreferences() {
    // TODO: implement it
  }

  /**
   * Apply settings to AutoCompletion
   * 
   * @see magellan.client.swing.preferences.PreferencesAdapter#applyPreferences()
   */
  public void applyPreferences() {
    source.setEnableAutoCompletion(cEnable.isSelected());
    source.setCurrentGUI((CompletionGUI) cForGUIs.getSelectedItem());
    source.setLimitMakeCompletion(cLimitMakeCompletion.isSelected());
    source.setEmptyStubMode(cPopup.isSelected());
    source.setHotKeyMode(cHotKey.isSelected());

    int t = 150;

    try {
      t = Integer.parseInt(tTime.getText());
    } catch (Exception exc) {
      tTime.setText("150");
    }

    source.setActivationTime(t);

    // maybe we could use the given array from AutoCompletion directly?
    KeyStroke[] ck = new KeyStroke[AutoCompletion.numKeys];

    for (int i = 0; i < AutoCompletion.numKeys; i++) {
      ck[i] = keyFields[i].getKeyStroke();
    }

    source.setCompleterKeys(ck);

    /** Apply preferences for self defined completions */

    // delete old entries out of settings file
    String s =
        (String) source.getSettings().get(
            PropertiesHelper.AUTOCOMPLETION_SELF_DEFINED_COMPLETIONS_COUNT);
    int completionCount = 0;

    if (s != null) {
      completionCount = Integer.parseInt(s);
    }

    for (int i = 0; i < completionCount; i++) {
      source.getSettings().remove("AutoCompletion.SelfDefinedCompletions.name" + i);
      source.getSettings().remove("AutoCompletion.SelfDefinedCompletions.value" + i);
    }

    // insert new values
    completionCount = 0;

    for (String name : selfDefinedCompletions.keySet()) {
      String value = selfDefinedCompletions.get(name);
      source.getSettings().setProperty(
          "AutoCompletion.SelfDefinedCompletions.name" + completionCount, name);
      source.getSettings().setProperty(
          "AutoCompletion.SelfDefinedCompletions.value" + completionCount, value);
      completionCount++;
    }

    source.getSettings().setProperty(
        PropertiesHelper.AUTOCOMPLETION_SELF_DEFINED_COMPLETIONS_COUNT,
        String.valueOf(completionCount));

    // update selfDefinedCompletion table of AutoCompletion
    source.getSelfDefinedCompletionsMap().clear();
    source.getSelfDefinedCompletionsMap().putAll(selfDefinedCompletions);
  }

  public static void applyDefault(SelfCleaningProperties settings) {
    settings.setProperty(
        PropertiesHelper.AUTOCOMPLETION_SELF_DEFINED_COMPLETIONS_COUNT,
        "1");
    settings.setProperty(
        "AutoCompletion.SelfDefinedCompletions.name0", "TODO");
    settings.setProperty(
        "AutoCompletion.SelfDefinedCompletions.value0", "; TODO");
  }

  private static class DefineCompletionDialog extends InternationalizedDialog {
    private JTextField name;
    private JTextArea value;
    private JButton ok;
    private JButton cancel;

    private DefineCompletionDialog(Frame frame) {
      super(frame, true);
      setTitle(Resources.get("completion.autocompletion.DefineCompletionDialog.title"));

      this.setSize(SwingUtils.getDimension(30, 15, true));

      JPanel cp = new JPanel();
      getContentPane().add(cp);
      cp.setBorder(new CompoundBorder(BorderFactory.createEtchedBorder(), new EmptyBorder(5, 5, 5,
          5)));
      cp.setLayout(new GridBagLayout());

      GridBagConstraints c =
          new GridBagConstraints(0, 0, 1, 1, 1.0, 0.0, GridBagConstraints.CENTER,
              GridBagConstraints.HORIZONTAL, new Insets(2, 2, 2, 2), 0, 0);

      name = new JTextField();
      name.setBorder(new TitledBorder(BorderFactory.createEtchedBorder(), Resources
          .get("completion.autocompletion.DefineCompletionDialog.nameField.title")));
      cp.add(name, c);

      value = new JTextArea();
      value.setMargin(new Insets(4, 4, 4, 4));

      JScrollPane temp = new JScrollPane(value);
      temp.setBorder(new TitledBorder(BorderFactory.createEtchedBorder(), Resources
          .get("completion.autocompletion.DefineCompletionDialog.valueField.title")));
      c.gridy = 1;
      c.fill = GridBagConstraints.BOTH;
      c.weighty = 1.0;
      cp.add(temp, c);

      ok =
          new JButton(Resources
              .get("completion.autocompletion.DefineCompletionDialog.okButton.caption"));
      ok.setMnemonic(Resources.get(
          "completion.autocompletion.DefineCompletionDialog.okButton.mnemonic").charAt(0));
      c.gridy = 0;
      c.gridx = 1;
      c.weighty = 0.0;
      c.weightx = 0.0;
      c.fill = GridBagConstraints.HORIZONTAL;
      cp.add(ok, c);

      cancel =
          new JButton(Resources
              .get("completion.autocompletion.DefineCompletionDialog.cancelButton.caption"));
      cancel.setMnemonic(Resources.get(
          "completion.autocompletion.DefineCompletionDialog.cancelButton.mnemonic").charAt(0));
      cancel.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          DefineCompletionDialog.this.quit();
        }
      });
      c.gridy = 1;
      c.anchor = GridBagConstraints.NORTH;
      cp.add(cancel, c);

      Vector<Component> components = new Vector<Component>();
      components.add(name);
      components.add(value);
      components.add(ok);
      components.add(cancel);

      setFocusTraversalPolicy(new MagellanFocusTraversalPolicy(components));
      //
      // name.setNextFocusableComponent(value);
      // value.setNextFocusableComponent(ok);
      // ok.setNextFocusableComponent(cancel);
      // cancel.setNextFocusableComponent(name);
    }

    private String[] getNewCompletionNameAndValue() {
      final String retVal[] = { "", "" };
      ok.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          retVal[0] = name.getText();
          retVal[1] = value.getText();
          quit();
        }
      });
      setLocationRelativeTo(getOwner());
      setVisible(true);

      return retVal;
    }
  }

}
