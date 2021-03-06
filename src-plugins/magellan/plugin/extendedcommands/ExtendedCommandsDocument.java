// class magellan.plugin.extendedcommands.ExtendedCommandsDocument
// created on 28.07.2008
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
// This program is distributed in the hope that it will be useful,
//
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
// GNU General Public License for more details.
//
// You should have received a copy of the GNU General Public License
// along with this program (see doc/LICENCE.txt); if not, write to the
// Free Software Foundation, Inc.,
// 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
//
package magellan.plugin.extendedcommands;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.border.EtchedBorder;
import javax.swing.event.CaretEvent;
import javax.swing.event.CaretListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import magellan.library.GameData;
import magellan.library.Unit;
import magellan.library.UnitContainer;
import magellan.library.utils.Resources;
import net.infonode.tabbedpanel.titledtab.TitledTab;

/**
 * This is a single panel representing ONE script.
 *
 * @author Thoralf Rickert
 * @version 1.0, 28.07.2008
 */
public class ExtendedCommandsDocument extends JPanel implements ActionListener, CaretListener,
    DocumentListener {
  private BeanShellEditor scriptingArea = null;
  private JComboBox priorityBox = null;
  private GameData world = null;
  private Unit unit = null;
  private UnitContainer container = null;
  private Script script;
  private boolean isModified = false;
  private JTextField findBox = null;
  private ExtendedCommands commands = null;
  private JLabel positionBox = null;
  private TitledTab tab = null;
  private JTextField gotoBox;
  private JButton gotoButton;
  private JButton findButton;
  private Map<String, String> resourceCache;

  /**
   * This constructor creates a single empty document. If you want to load some settings into the
   * document, you have to call setScript().
   */
  public ExtendedCommandsDocument() {
    super();
    initGUI();
  }

  /**
   * Initializes the GUI elements (textare, statusbar, etc.)
   */
  public void initGUI() {
    setLayout(new BorderLayout());

    JPanel editor = new JPanel(new BorderLayout());
    editor.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));

    scriptingArea = new BeanShellEditor();
    scriptingArea.getDocument().addDocumentListener(this);
    scriptingArea.addCaretListener(this);

    editor.add(new JScrollPane(scriptingArea), BorderLayout.CENTER);

    add(editor, BorderLayout.CENTER);

    JPanel south = new JPanel(new BorderLayout());

    // on the left, we present the username:
    JPanel left = new JPanel();
    left.setLayout(new BoxLayout(left, BoxLayout.X_AXIS));
    left.setBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED));
    left.add(Box.createRigidArea(new Dimension(10, 0)));

    JLabel label = new JLabel(Resources.get("extended_commands.priority.caption"));
    left.add(label);

    priorityBox = new JComboBox(Priority.values());
    left.add(priorityBox);
    left.add(Box.createRigidArea(new Dimension(10, 0)));

    JPanel center = new JPanel();
    center.setLayout(new BoxLayout(center, BoxLayout.X_AXIS));
    center.setBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED));
    center.add(Box.createRigidArea(new Dimension(10, 0)));

    findBox = new JTextField(10);
    center.add(findBox);
    center.add(Box.createRigidArea(new Dimension(5, 0)));
    findButton = new JButton(Resources.get("extended_commands.find.caption"));
    findButton.setActionCommand("button.find");
    findButton.addActionListener(this);
    findButton.setRequestFocusEnabled(false);
    center.add(findButton);
    center.add(Box.createRigidArea(new Dimension(10, 0)));

    // right panel contains the cursor position
    JPanel right = new JPanel();
    right.setLayout(new BoxLayout(right, BoxLayout.X_AXIS));
    right.setBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED));

    right.add(Box.createRigidArea(new Dimension(10, 0)));
    label = new JLabel(Resources.get("extended_commands.position.caption"));
    right.add(label);
    right.add(Box.createRigidArea(new Dimension(5, 0)));
    positionBox = new JLabel("0,0");
    right.add(positionBox);
    right.add(Box.createRigidArea(new Dimension(5, 0)));
    gotoButton = new JButton(Resources.get("extended_commands.go.caption"));
    gotoButton.setActionCommand("button.go");
    gotoButton.addActionListener(this);
    gotoButton.setRequestFocusEnabled(false);
    right.add(gotoButton);
    right.add(Box.createRigidArea(new Dimension(10, 0)));

    south.add(left, BorderLayout.WEST);
    south.add(center, BorderLayout.CENTER);
    south.add(right, BorderLayout.EAST);

    add(south, BorderLayout.SOUTH);
  }

  /**
   * This method is called, if one of the buttons is clicked.
   *
   * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
   */
  public void actionPerformed(ActionEvent e) {

    if (e.getActionCommand().equalsIgnoreCase("button.go")) {
      String input = JOptionPane.showInputDialog(Resources.get("extended_commands.go.label"));
      try {
        int line = Integer.parseInt(input);
        scriptingArea.setCaretPosition(getDot(line));
        scriptingArea.requestFocus();
      } catch (NumberFormatException e1) {
        // go nowhere
      }
    } else if (e.getActionCommand().equalsIgnoreCase("button.find")) {
      String search = findBox.getText();
      int pos = -1;
      if (search.length() > 0) {
        int caret = scriptingArea.getCaretPosition();
        String text = scriptingArea.getText();
        Matcher matcher =
            Pattern.compile(Pattern.quote(search), Pattern.CASE_INSENSITIVE).matcher(text);
        if (matcher.find(caret)) {
          pos = matcher.start();
        }
        if (pos < 0 && matcher.find()) {
          pos = matcher.start();
        }
        if (pos >= 0) {
          scriptingArea.setCaretPosition(pos);
          scriptingArea.moveCaretPosition(pos + search.length());
        }
        scriptingArea.requestFocus();
      }
    } else if (e.getActionCommand().equalsIgnoreCase("button.execute")) {
      // execute the command, this means, to temporary store the script
      // and execute it. After that, restore the old script.
      Script newScript = (Script) script.clone();
      newScript.setScript(scriptingArea.getText());

      if (unit != null) {
        commands.setCommands(unit, newScript);
        commands.execute(world, unit);
        commands.setCommands(unit, script); // reset to old script
      } else if (container != null) {
        commands.setCommands(container, newScript);
        commands.execute(world, container);
        commands.setCommands(container, script); // reset to old script
      } else {
        commands.setLibrary(newScript);
        commands.execute(world);
        commands.setLibrary(script); // reset to old script
      }
    }
  }

  private int getDot(int line) {
    String text = scriptingArea.getText();
    int l = 1;
    for (int pos = 0; pos < text.length(); ++pos) {
      char c = text.charAt(pos);
      if (c == '\n') {
        l++;
      }
      if (line == l)
        return pos + 1;
    }
    return text.length();
  }

  /**
   * Returns the value of world.
   *
   * @return Returns world.
   */
  public GameData getWorld() {
    return world;
  }

  /**
   * Sets the value of world.
   *
   * @param world The value for world.
   */
  public void setWorld(GameData world) {
    resourceCache = null;
    this.world = world;
  }

  /**
   * Returns the value of unit.
   *
   * @return Returns unit.
   */
  public Unit getUnit() {
    return unit;
  }

  /**
   * Sets the value of unit.
   *
   * @param unit The value for unit.
   */
  public void setUnit(Unit unit) {
    resourceCache = null;
    this.unit = unit;
  }

  /**
   * Returns the value of container.
   *
   * @return Returns container.
   */
  public UnitContainer getUnitContainer() {
    return container;
  }

  /**
   * Sets the value of container.
   *
   * @param container The value for container.
   */
  public void setContainer(UnitContainer container) {
    resourceCache = null;
    this.container = container;
  }

  /**
   * Returns the value of script.
   *
   * @return Returns script.
   */
  public Script getScript() {
    return script;
  }

  /**
   * Sets the value of script.
   *
   * @param script The value for script.
   */
  public void setScript(Script script) {
    resourceCache = null;
    if (isModified) {
      // ask if it is okay to load the new file
      int result =
          JOptionPane.showConfirmDialog(this,
              Resources.get("extended_commands.questions.not_saved"),
              Resources.get("extended_commands.questions.not_saved_title"),
              JOptionPane.OK_CANCEL_OPTION);
      if (result != JOptionPane.OK_OPTION)
        return;
    }
    this.script = script;
    if (script != null) {
      scriptingArea.setText(script.getScript());
      scriptingArea.setCaretPosition(script.getCursor());
      priorityBox.setSelectedItem(script.getPriority());
    } else {
      scriptingArea.setText("");
      scriptingArea.setCaretPosition(0);
      priorityBox.setSelectedItem(Priority.NORMAL);
    }

    setModified(false);
  }

  /**
   * @see javax.swing.event.CaretListener#caretUpdate(javax.swing.event.CaretEvent)
   */
  public void caretUpdate(CaretEvent e) {
    if (positionBox != null && scriptingArea != null) {
      int pos = e.getDot();
      String text = scriptingArea.getText();
      int row = 1;
      int col = 1;
      for (int i = 0; i < text.length(); i++) {
        if (i == pos) {
          break;
        }
        char c = text.charAt(i);
        if (c == '\n') {
          row++;
          col = 1;
        } else {
          col++;
        }
      }

      positionBox.setText(row + "," + col);
    }
  }

  /**
   * @see javax.swing.event.DocumentListener#changedUpdate(javax.swing.event.DocumentEvent)
   */
  public void changedUpdate(DocumentEvent e) {
    setModified(true);
  }

  /**
   * @see javax.swing.event.DocumentListener#insertUpdate(javax.swing.event.DocumentEvent)
   */
  public void insertUpdate(DocumentEvent e) {
    setModified(true);
  }

  /**
   * @see javax.swing.event.DocumentListener#removeUpdate(javax.swing.event.DocumentEvent)
   */
  public void removeUpdate(DocumentEvent e) {
    setModified(true);
  }

  public BeanShellEditor getScriptingArea() {
    return scriptingArea;
  }

  public void setModified(boolean modified) {
    isModified = modified;

    String changed = "";

    if (modified) {
      changed = " (*)";
    }

    String title = "";

    if (unit != null) {
      title =
          Resources.get("extended_commands.element.unit", unit.getName(), unit.getID()) + changed;
    } else if (container != null) {
      title =
          Resources.get("extended_commands.element.container", container.getName(), container
              .getID())
              + changed;
    } else {
      title = getResource("extended_commands.element.library") + changed;
    }

    if (tab != null) {
      tab.setText(title);
    }

  }

  private String getResource(String key) {
    if (resourceCache == null) {
      resourceCache = new HashMap<String, String>();
    }
    String result = resourceCache.get(key);
    if (result == null) {
      result = Resources.get(key);
      resourceCache.put(key, result);
    }
    return result;
  }

  public boolean isModified() {
    return isModified;
  }

  /**
   * Returns the value of commands.
   *
   * @return Returns commands.
   */
  public ExtendedCommands getCommands() {
    return commands;
  }

  /**
   * Sets the value of commands.
   *
   * @param commands The value for commands.
   */
  public void setCommands(ExtendedCommands commands) {
    resourceCache = null;
    this.commands = commands;
  }

  /**
   * Returns the value of tab.
   *
   * @return Returns tab.
   */
  public TitledTab getTab() {
    return tab;
  }

  /**
   * Sets the value of tab.
   *
   * @param tab The value for tab.
   */
  public void setTab(TitledTab tab) {
    this.tab = tab;
  }

}
