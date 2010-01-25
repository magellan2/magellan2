// class magellan.plugin.extendedcommands.ExtendedCommandsDialog
// created on 02.02.2008
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
package magellan.plugin.extendedcommands;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashMap;
import java.util.Map;

import javax.swing.JButton;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import magellan.client.Client;
import magellan.client.desktop.MagellanDesktop;
import magellan.client.event.SelectionEvent;
import magellan.client.event.SelectionListener;
import magellan.client.swing.ui.WrapLayout;
import magellan.library.GameData;
import magellan.library.Region;
import magellan.library.Unit;
import magellan.library.UnitContainer;
import magellan.library.utils.MagellanImages;
import magellan.library.utils.Resources;
import magellan.library.utils.Utils;
import magellan.library.utils.logging.Logger;
import net.infonode.docking.DockingWindow;
import net.infonode.docking.DockingWindowListener;
import net.infonode.docking.OperationAbortedException;
import net.infonode.docking.View;
import net.infonode.gui.ButtonFactory;
import net.infonode.gui.icon.button.CloseIcon;
import net.infonode.tabbedpanel.Tab;
import net.infonode.tabbedpanel.TabDropDownListVisiblePolicy;
import net.infonode.tabbedpanel.TabbedPanel;
import net.infonode.tabbedpanel.titledtab.TitledTab;

/**
 * This is a dialog to edit the script/commands for a given unit. <br />
 * TODO Save dialog positions (size, location, slider position)
 * 
 * @author Thoralf Rickert
 * @version 1.0, 11.09.2007
 */
public class ExtendedCommandsDock extends JPanel implements ActionListener, DockingWindowListener,
    SelectionListener {
  public static final String IDENTIFIER = "ExtendedCommands";
  private static final Logger log = Logger.getInstance(ExtendedCommandsDock.class);
  private ExtendedCommands commands = null;
  private boolean visible = false;
  private TabbedPanel tabs = null;
  private GameData world = null;
  private Map<String, Tab> tabMap = new HashMap<String, Tab>();
  private Map<String, ExtendedCommandsDocument> docMap =
      new HashMap<String, ExtendedCommandsDocument>();
  private SelectionEvent selection;

  public ExtendedCommandsDock(ExtendedCommands commands) {
    this.commands = commands;
    init();
  }

  /**
   * Initializes the GUI of the dock.
   */
  protected void init() {
    setLayout(new BorderLayout());
    
    tabs = new TabbedPanel();
    tabs.getProperties().setTabReorderEnabled(true);
    tabs.getProperties().setTabDropDownListVisiblePolicy(
        TabDropDownListVisiblePolicy.TABS_NOT_VISIBLE);
    tabs.getProperties().setEnsureSelectedTabVisible(true);
    tabs.getProperties().setShadowEnabled(true);

    add(tabs, BorderLayout.CENTER);

    final JPanel north = new JPanel(new WrapLayout());
    
    JButton loadButton =
        new JButton(Resources.get("extended_commands.button.load.caption"), MagellanImages
            .getImageIcon("etc/images/gui/actions/open.gif"));
    loadButton.setToolTipText(Resources.get("extended_commands.button.load.tooltip"));
    loadButton.setRequestFocusEnabled(false);
    loadButton.setActionCommand("button.load");
    loadButton.addActionListener(this);
    loadButton.setAlignmentX(Component.RIGHT_ALIGNMENT);
    north.add(loadButton);

    JButton executeButton =
        new JButton(Resources.get("extended_commands.button.execute.caption"), MagellanImages
            .getImageIcon("etc/images/gui/actions/execute.gif"));
    executeButton.setRequestFocusEnabled(false);
    executeButton.setActionCommand("button.execute");
    executeButton.addActionListener(this);
    executeButton.setAlignmentX(Component.RIGHT_ALIGNMENT);
    north.add(executeButton);

    JButton saveButton =
        new JButton(Resources.get("extended_commands.button.save.caption"), MagellanImages
            .getImageIcon("etc/images/gui/actions/save_edit.gif"));
    saveButton.setRequestFocusEnabled(false);
    saveButton.setVerticalTextPosition(JButton.CENTER);
    saveButton.setHorizontalTextPosition(JButton.LEADING);
    saveButton.setActionCommand("button.save");
    saveButton.addActionListener(this);
    saveButton.setAlignmentX(Component.RIGHT_ALIGNMENT);
    north.add(saveButton);

    JButton saveAllButton =
        new JButton(Resources.get("extended_commands.button.saveall.caption"), MagellanImages
            .getImageIcon("etc/images/gui/actions/saveas_edit.gif"));
    saveAllButton.setRequestFocusEnabled(false);
    saveAllButton.setVerticalTextPosition(JButton.CENTER);
    saveAllButton.setHorizontalTextPosition(JButton.LEADING);
    saveAllButton.setActionCommand("button.saveall");
    saveAllButton.addActionListener(this);
    saveAllButton.setAlignmentX(Component.RIGHT_ALIGNMENT);
    north.add(saveAllButton);

    add(north, BorderLayout.NORTH);
  }

  /**
   * This method is called, if one of the buttons is clicked.
   * 
   * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
   */
  public void actionPerformed(ActionEvent e) {
    log.debug("Action '" + e.getActionCommand() + "' called");
    if (e.getActionCommand().equalsIgnoreCase("button.load")) {
      openCurrent();
    }
    if (e.getActionCommand().equalsIgnoreCase("button.execute")) {
      // let's get the tab and execute it inside the doc.
      TitledTab tab = (TitledTab) tabs.getSelectedTab();
      if (tab == null)
        return; // we don't execute everything here....
      ExtendedCommandsDocument doc = (ExtendedCommandsDocument) tab.getContentComponent();

      log.debug("Execute button selected on tab " + tab.getText());
      doc.actionPerformed(e);
    } else if (e.getActionCommand().equalsIgnoreCase("button.save")) {

      TitledTab tab = (TitledTab) tabs.getSelectedTab();
      if (tab == null)
        return; // we don't save everything here....
      saveTab(tab);
      commands.save();

    } else if (e.getActionCommand().equalsIgnoreCase("button.saveall")) {
      // iterate thru all tabs and save the scripts
      for (int i = 0; i < tabs.getTabCount(); i++) {
        TitledTab tab = (TitledTab) tabs.getTabAt(i);
        saveTab(tab);
      }
      commands.save();
    }
  }

  protected void openCurrent() {
    if (selection == null)
      return;
    
    // warn before loading many scripts
    if (selection.getSelectedObjects().size() > 5) {
      if (JOptionPane.showConfirmDialog(this, Resources.get("extended_commands.loadmany.question",
          selection.getSelectedObjects().size()),
          Resources.get("extended_commands.loadmany.title"), JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION) {
        return;
      }
    }
    
    for (Object sel : selection.getSelectedObjects()) {
      if (sel instanceof Unit) {
        ExtendedCommandsDock.log.debug("Edit Command for Unit " + sel);
        Script script = getScript((Unit) sel);
        setScript((Unit) sel, null, script);
      } else if (sel instanceof UnitContainer) {
        ExtendedCommandsDock.log.debug("Edit Command for Unit " + sel);
        Script script = getScript((UnitContainer) sel);
        setScript(null, (UnitContainer) sel, script);
      }
    }
  }

  /**
   * find the commands for this container or set them to "".
   */
  private Script getScript(UnitContainer container) {
    Script script = commands.getCommands(container);
    if (Utils.isEmpty(script)) {
      script =
          new Script(container.getID().toString(), Script.SCRIPTTYPE_CONTAINER, ContainerType
              .getType(container.getType()), "");
    }
    if (Utils.isEmpty(script.getScript())) {
      // show some examples for beginners...
      String exampleScript = "// example for beginners...\n";
      exampleScript += "//\n";
      exampleScript += "//...";
      script.setScript(exampleScript);
    }
    return script;
  }

  /**
   * find the commands for this unit or set them to "".
   */
  private Script getScript(Unit unit) {
    Script script = commands.getCommands(unit);
    if (Utils.isEmpty(script)) {
      script =
          new Script(unit.getID().toString(), Script.SCRIPTTYPE_UNIT, ContainerType.UNKNOWN, "");
    }
    if (Utils.isEmpty(script) || Utils.isEmpty(script.getScript())) {
      // show some examples for beginners...
      String exampleScript = "// example for beginners...\n";
      exampleScript += "//\n";
      exampleScript += "//if (!unit.isOrdersConfirmed()) {\n";
      exampleScript += "//  unit.setOrdersConfirmed(true);\n";
      exampleScript += "//}\n";
      script.setScript(exampleScript);
    }
    return script;
  }

  /**
   * Saves the content of a tab inside the Extended Commands List. Attention: This is NOT a
   * save-to-disc Operation. It clones the content of the tab and set's it inside the Commands
   * object.
   */
  protected void saveTab(TitledTab tab) {
    if (tab == null)
      return;
    ExtendedCommandsDocument doc = (ExtendedCommandsDocument) tab.getContentComponent();
    log.debug("Save tab '" + tab.getText() + "' contents");

    Script newScript = (Script) doc.getScript().clone();
    newScript.setScript(doc.getScriptingArea().getText());

    if (doc.getUnit() != null) {
      commands.setCommands(doc.getUnit(), newScript);
    } else if (doc.getContainer() != null) {
      commands.setCommands(doc.getContainer(), newScript);
    } else {
      commands.setLibrary(newScript);
    }

    doc.setModified(false);
  }

  /**
   * Setups the dock and opens the script for the given unit or container.
   */
  public void setScript(Unit unit, UnitContainer container, Script script) {

    String key = createKey(unit, container);
    String title = createTitle(unit, container);
    if (tabMap.containsKey(key)) {
      // ok, the entry already exists.
      tabs.setSelectedTab(tabMap.get(key));
    } else {
      // we have to create a tab (normal operation)
      ExtendedCommandsDocument doc = new ExtendedCommandsDocument();
      doc.setWorld(world);
      doc.setCommands(commands);
      doc.setUnit(unit);
      doc.setContainer(container);
      doc.setScript(script);
      TitledTab tab = new TitledTab(title, null, doc, null);
      tab.setHighlightedStateTitleComponent(createCloseTabButton(key, tab));
      doc.setTab(tab);

      tabs.addTab(tab);
      tabs.setSelectedTab(tab);

      tabMap.put(key, tab);
      docMap.put(key, doc);
    }

    // Visibility
    if (!visible) {
      MagellanDesktop.getInstance().setVisible(ExtendedCommandsDock.IDENTIFIER, true);
    }

  }

  protected String createKey(Unit unit, UnitContainer container) {
    if (unit != null)
      return unit.getID().toString();
    if (container != null)
      return container.getID().toString();
    return "---";
  }

  protected String createTitle(Unit unit, UnitContainer container) {
    if (unit != null) {
      String unitName = unit.getName();
      if (unitName == null)
        unitName = "";
      return Resources.get("extended_commands.element.unit", unitName, unit.getID());
    }
    if (container != null) {
      String containerName = container.getName();
      if (containerName == null) {
        if (container instanceof Region)
          containerName = ((Region) container).getRegionType().getName();
      }
      return Resources.get("extended_commands.element.container", containerName, container.getID());
    }
    return Resources.get("extended_commands.element.library");
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
    this.world = world;
    // this means, we have to close all currently open tabs
    if (tabs != null && tabs.getTabCount() > 0) {
      tabMap.clear();
      docMap.clear();

      for (int i = 0; i < tabs.getTabCount(); i++) {
        tabs.removeTab(tabs.getTabAt(i));
        i--;
      }

      // TODO: show library at startup
    }
  }

  /**
   * @see net.infonode.docking.DockingWindowListener#viewFocusChanged(net.infonode.docking.View,
   *      net.infonode.docking.View)
   */
  public void viewFocusChanged(View previouslyFocusedView, View focusedView) {
  }

  /**
   * @see net.infonode.docking.DockingWindowListener#windowAdded(net.infonode.docking.DockingWindow,
   *      net.infonode.docking.DockingWindow)
   */
  public void windowAdded(DockingWindow addedToWindow, DockingWindow addedWindow) {
  }

  /**
   * @see net.infonode.docking.DockingWindowListener#windowClosed(net.infonode.docking.DockingWindow)
   */
  public void windowClosed(DockingWindow window) {
  }

  /**
   * @see net.infonode.docking.DockingWindowListener#windowClosing(net.infonode.docking.DockingWindow)
   */
  public void windowClosing(DockingWindow window) throws OperationAbortedException {
  }

  /**
   * @see net.infonode.docking.DockingWindowListener#windowDocked(net.infonode.docking.DockingWindow)
   */
  public void windowDocked(DockingWindow window) {
  }

  /**
   * @see net.infonode.docking.DockingWindowListener#windowDocking(net.infonode.docking.DockingWindow)
   */
  public void windowDocking(DockingWindow window) throws OperationAbortedException {
  }

  /**
   * @see net.infonode.docking.DockingWindowListener#windowHidden(net.infonode.docking.DockingWindow)
   */
  public void windowHidden(DockingWindow window) {
    this.visible = false;
  }

  /**
   * @see net.infonode.docking.DockingWindowListener#windowMaximized(net.infonode.docking.DockingWindow)
   */
  public void windowMaximized(DockingWindow window) {
  }

  /**
   * @see net.infonode.docking.DockingWindowListener#windowMaximizing(net.infonode.docking.DockingWindow)
   */
  public void windowMaximizing(DockingWindow window) throws OperationAbortedException {
  }

  /**
   * @see net.infonode.docking.DockingWindowListener#windowMinimized(net.infonode.docking.DockingWindow)
   */
  public void windowMinimized(DockingWindow window) {
  }

  /**
   * @see net.infonode.docking.DockingWindowListener#windowMinimizing(net.infonode.docking.DockingWindow)
   */
  public void windowMinimizing(DockingWindow window) throws OperationAbortedException {
  }

  /**
   * @see net.infonode.docking.DockingWindowListener#windowRemoved(net.infonode.docking.DockingWindow,
   *      net.infonode.docking.DockingWindow)
   */
  public void windowRemoved(DockingWindow removedFromWindow, DockingWindow removedWindow) {
  }

  /**
   * @see net.infonode.docking.DockingWindowListener#windowRestored(net.infonode.docking.DockingWindow)
   */
  public void windowRestored(DockingWindow window) {
  }

  /**
   * @see net.infonode.docking.DockingWindowListener#windowRestoring(net.infonode.docking.DockingWindow)
   */
  public void windowRestoring(DockingWindow window) throws OperationAbortedException {
  }

  /**
   * @see net.infonode.docking.DockingWindowListener#windowShown(net.infonode.docking.DockingWindow)
   */
  public void windowShown(DockingWindow window) {
    this.visible = true;
  }

  /**
   * @see net.infonode.docking.DockingWindowListener#windowUndocked(net.infonode.docking.DockingWindow)
   */
  public void windowUndocked(DockingWindow window) {
  }

  /**
   * @see net.infonode.docking.DockingWindowListener#windowUndocking(net.infonode.docking.DockingWindow)
   */
  public void windowUndocking(DockingWindow window) throws OperationAbortedException {
  }

  /**
   * Creates a close tab button that closes the given tab when the button is selected
   * 
   * @param tab the tab what will be closed when the button is pressed
   * @return the close button
   */
  private JButton createCloseTabButton(final String key, final TitledTab tab) {
    return ButtonFactory.createFlatHighlightButton(new CloseIcon(), Resources
        .get("extended_commands.button.close.tooltip"), 0, new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        ExtendedCommandsDocument doc = (ExtendedCommandsDocument) tab.getContentComponent();
        if (doc.isModified()) {
          int answer =
              JOptionPane.showConfirmDialog(Client.INSTANCE, Resources
                  .get("extended_commands.questions.not_saved"), Resources
                  .get("extended_commands.questions.not_saved_title"), JOptionPane.YES_NO_OPTION);
          if (answer == JOptionPane.YES_OPTION) {
            closeTab(key, tab);
          }
        } else {
          closeTab(key, tab);
        }
      }
    });
  }

  public void closeTab(String key, Tab tab) {
    // Closing the tab by removing it from the tabbed panel it is a member of
    Tab t1 = tabMap.remove(key);
    if (t1 != null && !tab.equals(tab)) {
      log.error("Whoops - here is something wrong");
    }
    docMap.remove(key);

    tab.getTabbedPanel().removeTab(tab);
  }

  public void selectionChanged(SelectionEvent e) {
    this.selection = e;
  }
}
