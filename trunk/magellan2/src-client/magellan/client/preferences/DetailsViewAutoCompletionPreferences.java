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
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.Collections;
import java.util.Iterator;
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
import magellan.library.utils.OrderedHashtable;
import magellan.library.utils.PropertiesHelper;
import magellan.library.utils.Resources;

/**
 * 
 *
 * @author ...
 * @version 1.0, 15.02.2008
 */
public class DetailsViewAutoCompletionPreferences extends JPanel implements PreferencesAdapter {
  protected AutoCompletion source;
  protected JCheckBox cEnable;
  protected JCheckBox iPopup;
  protected JCheckBox limitMakeCompletion;
  protected JComboBox forGUIs;
  protected JTextField tTime;
  protected KeyTextField keyFields[];

  // a copy of AutoCompletion.selfDefinedCompletions
  private Map<String, String> selfDefinedCompletions;

  /**
   * Creates a new DetailAutoCompletionPreferencesAdapter object.
   */
  public DetailsViewAutoCompletionPreferences(AutoCompletion s) {
    source = s;
    this.setLayout(new BorderLayout());
    JPanel panel = new JPanel(new GridBagLayout());

    GridBagConstraints c = new GridBagConstraints(0, 0, 2, 1, 0.1, 1.0, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(2, 2, 2, 2), 0, 0);

    panel.add(new JPanel(), c);

    c.gridwidth = 1;
    c.weighty = 0;
    c.gridy++;
    cEnable = new JCheckBox(Resources.get("completion.autocompletion.prefs.autocompletion"), source.isEnableAutoCompletion());
    panel.add(cEnable, c);

    c.gridy++;
    limitMakeCompletion = new JCheckBox(Resources.get("completion.autocompletion.prefs.limitmakecompletion"), source.getLimitMakeCompletion());
    limitMakeCompletion.setToolTipText(Resources.get("completion.autocompletion.prefs.limitmakecompletion.tooltip"));
    panel.add(limitMakeCompletion, c);

    iPopup = new JCheckBox(Resources.get("completion.autocompletion.prefs.stubmode"), source.getEmptyStubMode());
    c.gridy++;
    panel.add(iPopup, c);

    c.gridy++;

    JPanel inner = new JPanel(new GridBagLayout());
    GridBagConstraints con2 = new GridBagConstraints(0, 0, 1, 1, 0, 0, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, c.insets, 0, 0);

    inner.add(new JLabel(Resources.get("completion.autocompletion.prefs.time")), con2);
    con2.gridy++;
    inner.add(new JLabel(Resources.get("completion.autocompletion.prefs.gui") + ":"), con2);

    con2.gridx = 1;
    con2.gridy = 0;
    con2.weightx = 1;
    tTime = new JTextField(String.valueOf(source.getActivationTime()), 5);
    inner.add(tTime, con2);
    con2.gridy++;

    forGUIs = new JComboBox(source.getCompletionGUIs());
    forGUIs.setEditable(false);

    if (source.getCurrentGUI() != null) {
      forGUIs.setSelectedItem(source.getCurrentGUI());
    }

    inner.add(forGUIs, con2);

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
    this.selfDefinedCompletions = new OrderedHashtable<String, String>(source.getSelfDefinedCompletionsMap());

    c.gridx = 0;
    c.gridy = 5;
    c.gridheight = 1;
    c.gridwidth = 2;
    c.weighty = 0;
    c.insets = new Insets(10, 0, 10, 0);
    panel.add(new JSeparator(SwingConstants.HORIZONTAL), c);

    JPanel sDCPanel = getSelfDefinedCompletionPanel();
    c.gridy = 6;
    c.weighty = 0.2;
    c.fill = GridBagConstraints.BOTH;
    c.insets = new Insets(0, 0, 0, 0);
    panel.add(sDCPanel, c);

    c.weighty = 1;
    c.gridy++;
    panel.add(new JPanel(), c);
    
    add(panel,BorderLayout.NORTH);
  }

  private JPanel getSelfDefinedCompletionPanel() {
    JPanel sDCPanel = new JPanel();
    sDCPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
    sDCPanel.setLayout(new GridBagLayout());

    GridBagConstraints c = new GridBagConstraints(0, 0, 1, 1, 0.5, 0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(2, 2, 2, 2), 0, 0);
    sDCPanel.add(new JLabel(Resources.get("completion.autocompletion.prefs.SelfDefinedCompletions.title") + ":"), c);

    final JLabel completionValue = new JLabel();
    JScrollPane temp = new JScrollPane(completionValue);
    temp.setBorder(new CompoundBorder(new TitledBorder(BorderFactory.createEtchedBorder(), Resources.get("completion.autocompletion.prefs.SelfDefinedCompletions.completionValue.title")), new EmptyBorder(5, 5, 5, 5)));
    c.gridx = 1;
    c.gridy = 1;
    c.weighty = 0.5;
    sDCPanel.add(temp, c);

    List<String> l = new LinkedList<String>(this.selfDefinedCompletions.keySet());
    Collections.sort(l);

    DefaultListModel listModel = new DefaultListModel();

    for (Iterator iter = l.iterator(); iter.hasNext();) {
      listModel.addElement(iter.next());
    }

    final JList completionNames = new JList(listModel);
    completionNames.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    completionNames.addListSelectionListener(new ListSelectionListener() {
      public void valueChanged(ListSelectionEvent e) {
        if (!e.getValueIsAdjusting()) {
          String str = (String) completionNames.getSelectedValue();
          String display = "";

          if (str != null) {
            str = (String) selfDefinedCompletions.get(str);
            display = "<html><b><p>";

            for (int i = 0; i < str.length(); i++) {
              if (str.charAt(i) == '\n') {
                display += "</p><p>";
              } else {
                display += str.charAt(i);
              }
            }

            display += "</p></b></html>";
          }

          completionValue.setText(display);
        }
      }
    });

    if (completionNames.getModel().getSize() > 0) {
      completionNames.setSelectedIndex(0);
    }

    temp = new JScrollPane(completionNames);
    temp.setBorder(new TitledBorder(BorderFactory.createBevelBorder(BevelBorder.LOWERED), Resources.get("completion.autocompletion.prefs.SelfDefinedCompletions.completionNames.title")));
    c.gridx = 0;
    c.gridheight = 3;
    sDCPanel.add(temp, c);

    final JButton delete = new JButton(Resources.get("completion.autocompletion.prefs.SelfDefinedCompletions.deleteButton.caption"));
    delete.setMnemonic(Resources.get("completion.autocompletion.prefs.SelfDefinedCompletions.deleteButton.mnemonic").charAt(0));

    if (completionNames.getModel().getSize() == 0) {
      delete.setEnabled(false);
    }

    delete.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        int index = completionNames.getSelectedIndex();
        String name = (String) completionNames.getSelectedValue();
        ((DefaultListModel) completionNames.getModel()).remove(index);

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

    final JButton newCompletion = new JButton(Resources.get("completion.autocompletion.prefs.SelfDefinedCompletions.newCompletionButton.caption"));
    newCompletion.setMnemonic(Resources.get("completion.autocompletion.prefs.SelfDefinedCompletions.newCompletionButton.mnemonic").charAt(0));
    newCompletion.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        String nameAndValue[] = (new DefineCompletionDialog(JOptionPane.getFrameForComponent(newCompletion))).getNewCompletionNameAndValue();
        String name = nameAndValue[0];
        String value = nameAndValue[1];

        if (!name.equals("") && !value.equals("")) {
          selfDefinedCompletions.put(name, value);

          DefaultListModel lstModel = (DefaultListModel) completionNames.getModel();

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
    java.awt.GridBagConstraints c = new java.awt.GridBagConstraints(0, 0, 1, 1, 0, 0, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(2, 2, 2, 2), 0, 0);

    for (int i = 0; i < 4; i++) {
      c.gridy = i;
      p.add(new JLabel(Resources.get("completion.autocompletion.prefs.keys." + String.valueOf(i))), c);
    }

    c.gridx = 1;
    keyFields = new KeyTextField[4];

    int ck[][] = s.getCompleterKeys();

    for (int i = 0; i < 4; i++) {
      c.gridy = i;
      keyFields[i] = new KeyTextField();
      keyFields[i].init(ck[i][0], ck[i][1]);
      p.add(keyFields[i], c);
    }

    return p;
  }

  /**
   * DOCUMENT-ME
   * 
   * @param cGUI
   *          DOCUMENT-ME
   */
  public void addCompletionGUI(CompletionGUI cGUI) {
    forGUIs.addItem(cGUI);
    this.revalidate();
    this.doLayout();
  }

  /**
   * DOCUMENT-ME
   * 
   * @param cGUI
   *          DOCUMENT-ME
   */
  public void setCurrentGUI(CompletionGUI cGUI) {
    forGUIs.setSelectedItem(cGUI);
  }

  /**
   * DOCUMENT-ME
   * 
   * 
   */
  public Component getComponent() {
    return this;
  }

  /**
   * DOCUMENT-ME
   * 
   * 
   */
  public String getTitle() {
    return Resources.get("completion.autocompletion.prefs.title");
  }

  /**
   * DOCUMENT-ME
   * 
   * @see magellan.client.swing.preferences.PreferencesAdapter#initPreferences()
   */
  public void initPreferences() {
    // TODO: implement it
  }

  /**
   * DOCUMENT-ME
   */
  public void applyPreferences() {
    source.setEnableAutoCompletion(cEnable.isSelected());
    source.setCurrentGUI((CompletionGUI) forGUIs.getSelectedItem());
    source.setLimitMakeCompletion(limitMakeCompletion.isSelected());
    source.setEmptyStubMode(iPopup.isSelected());

    int t = 150;

    try {
      t = Integer.parseInt(tTime.getText());
    } catch (Exception exc) {
      tTime.setText("150");
    }

    source.setActivationTime(t);

    // maybe we could use the given array from AutoCompletion directly?
    int ck[][] = new int[4][2];

    for (int i = 0; i < 4; i++) {
      ck[i][0] = keyFields[i].getModifiers();
      ck[i][1] = keyFields[i].getKeyCode();
    }

    source.setCompleterKeys(ck);

    /** Apply preferences for self defined completions */

    // delete old entries out of settings file
    String s = (String) source.getSettings().get(PropertiesHelper.AUTOCOMPLETION_SELF_DEFINED_COMPLETIONS_COUNT);
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

    for (Iterator iter = selfDefinedCompletions.keySet().iterator(); iter.hasNext();) {
      String name = (String) iter.next();
      String value = (String) selfDefinedCompletions.get(name);
      source.getSettings().setProperty("AutoCompletion.SelfDefinedCompletions.name" + completionCount, name);
      source.getSettings().setProperty("AutoCompletion.SelfDefinedCompletions.value" + completionCount, value);
      completionCount++;
    }

    source.getSettings().setProperty(PropertiesHelper.AUTOCOMPLETION_SELF_DEFINED_COMPLETIONS_COUNT, String.valueOf(completionCount));

    // update selfDefinedCompletion table of AutoCompletion
    source.getSelfDefinedCompletionsMap().clear();
    source.getSelfDefinedCompletionsMap().putAll(selfDefinedCompletions);
  }

  private class KeyTextField extends JTextField implements KeyListener {
    protected int modifiers = 0;
    protected int key = 0;

    /**
     * Creates a new KeyTextField object.
     */
    public KeyTextField() {
      super(20);
      this.addKeyListener(this);
    }

    /**
     * DOCUMENT-ME
     * 
     * @param modifiers
     *          DOCUMENT-ME
     * @param key
     *          DOCUMENT-ME
     */
    public void init(int modifiers, int key) {
      this.key = key;
      this.modifiers = modifiers;

      String s = KeyEvent.getKeyModifiersText(modifiers);

      if ((s != null) && (s.length() > 0)) {
        s += ('+' + KeyEvent.getKeyText(key));
      } else {
        s = KeyEvent.getKeyText(key);
      }

      setText(s);
    }

    /**
     * DOCUMENT-ME
     * 
     * @param p1
     *          DOCUMENT-ME
     */
    public void keyReleased(java.awt.event.KeyEvent p1) {
      // maybe should delete any input if there's no "stable"(non-modifying)
      // key
    }

    /**
     * DOCUMENT-ME
     * 
     * @param p1
     *          DOCUMENT-ME
     */
    public void keyPressed(java.awt.event.KeyEvent p1) {
      modifiers = p1.getModifiers();
      key = p1.getKeyCode();

      // avoid double string
      if ((key == KeyEvent.VK_SHIFT) || (key == KeyEvent.VK_CONTROL) || (key == KeyEvent.VK_ALT) || (key == KeyEvent.VK_ALT_GRAPH)) {
        int xored = 0;

        switch (key) {
        case KeyEvent.VK_SHIFT:
          xored = KeyEvent.SHIFT_MASK;

          break;

        case KeyEvent.VK_CONTROL:
          xored = KeyEvent.CTRL_MASK;

          break;

        case KeyEvent.VK_ALT:
          xored = KeyEvent.ALT_MASK;

          break;

        case KeyEvent.VK_ALT_GRAPH:
          xored = KeyEvent.ALT_GRAPH_MASK;

          break;
        }

        modifiers ^= xored;
      }

      String s = KeyEvent.getKeyModifiersText(modifiers);

      if ((s != null) && (s.length() > 0)) {
        s += ('+' + KeyEvent.getKeyText(key));
      } else {
        s = KeyEvent.getKeyText(key);
      }

      setText(s);
      p1.consume();
    }

    /**
     * DOCUMENT-ME
     * 
     * @param p1
     *          DOCUMENT-ME
     */
    public void keyTyped(java.awt.event.KeyEvent p1) {
    }

    /**
     * to allow "tab" as a key
     * 
     * @see javax.swing.JComponent#isManagingFocus()
     */
    public boolean isManagingFocus() {
      return true;
    }

    /**
     * DOCUMENT-ME
     * 
     * 
     */
    public int getKeyCode() {
      return key;
    }

    /**
     * DOCUMENT-ME
     * 
     * 
     */
    public int getModifiers() {
      return modifiers;
    }
  }

  private class DefineCompletionDialog extends InternationalizedDialog {
    private JTextField name;
    private JTextArea value;
    private JButton ok;
    private JButton cancel;
       

    private DefineCompletionDialog(Frame frame) {
      super(frame, true);
      this.setTitle(Resources.get("completion.autocompletion.DefineCompletionDialog.title"));
      this.setSize(500, 200);

      JPanel cp = new JPanel();
      this.getContentPane().add(cp);
      cp.setBorder(new CompoundBorder(BorderFactory.createEtchedBorder(), new EmptyBorder(5, 5, 5, 5)));
      cp.setLayout(new GridBagLayout());

      GridBagConstraints c = new GridBagConstraints(0, 0, 1, 1, 1.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(2, 2, 2, 2), 0, 0);

      name = new JTextField();
      name.setBorder(new TitledBorder(BorderFactory.createEtchedBorder(), Resources.get("completion.autocompletion.DefineCompletionDialog.nameField.title")));
      cp.add(name, c);

      value = new JTextArea();
      value.setMargin(new Insets(4, 4, 4, 4));

      JScrollPane temp = new JScrollPane(value);
      temp.setBorder(new TitledBorder(BorderFactory.createEtchedBorder(), Resources.get("completion.autocompletion.DefineCompletionDialog.valueField.title")));
      c.gridy = 1;
      c.fill = GridBagConstraints.BOTH;
      c.weighty = 1.0;
      cp.add(temp, c);

      ok = new JButton(Resources.get("completion.autocompletion.DefineCompletionDialog.okButton.caption"));
      ok.setMnemonic(Resources.get("completion.autocompletion.DefineCompletionDialog.okButton.mnemonic").charAt(0));
      c.gridy = 0;
      c.gridx = 1;
      c.weighty = 0.0;
      c.weightx = 0.0;
      c.fill = GridBagConstraints.HORIZONTAL;
      cp.add(ok, c);

      cancel = new JButton(Resources.get("completion.autocompletion.DefineCompletionDialog.cancelButton.caption"));
      cancel.setMnemonic(Resources.get("completion.autocompletion.DefineCompletionDialog.cancelButton.mnemonic").charAt(0));
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
//      name.setNextFocusableComponent(value);
//      value.setNextFocusableComponent(ok);
//      ok.setNextFocusableComponent(cancel);
//      cancel.setNextFocusableComponent(name);
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
      this.setLocationRelativeTo(getOwner());
      this.setVisible(true);

      return retVal;
    }
  }
}
