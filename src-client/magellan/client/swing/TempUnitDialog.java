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

package magellan.client.swing;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Container;
import java.awt.FlowLayout;
import java.awt.FocusTraversalPolicy;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Vector;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

import magellan.client.utils.SwingUtils;
import magellan.library.Faction;
import magellan.library.Group;
import magellan.library.gamebinding.EresseaConstants;
import magellan.library.utils.NameFileNameGenerator;
import magellan.library.utils.NameGenerator;
import magellan.library.utils.PropertiesHelper;
import magellan.library.utils.Resources;
import magellan.library.utils.logging.Logger;

/**
 * A dialog for creating a Temp Unit
 */
public class TempUnitDialog extends InternationalizedDialog {
  private static final Logger log = Logger.getInstance(TempUnitDialog.class);

  protected Faction faction = null;
  protected Group parentGroup = null;
  protected int combatStatus = 0;

  protected JTextField id;
  protected JTextField name;
  protected JButton more;
  protected JButton ok;
  protected JButton cancel;
  protected JPanel moreButtonPanel;
  protected JPanel morePanel;
  protected JTextField recruit;
  protected JTextField transfer;
  protected JTextArea descript;
  protected JTextField order;
  protected JComboBox group;
  protected JComboBox combatState;
  protected JCheckBox giveRecruitCost;
  protected JCheckBox giveMaintainCost;
  protected GridBagConstraints con;
  protected GridBagLayout layout;
  protected Component posC;
  protected boolean approved = false;
  protected Properties settings;
  protected JButton nameGen;
  protected Container nameCon;
  private TempUnitDialogListener listener;

  /** settings key for detailed dialog property */
  public static final String SETTINGS_KEY = "TempUnitDialog.ExtendedDialog";
  private static final String BOUNDS_KEY = "TempUnitDialog.bounds";

  /**
   * Creates new TempUnitDialog
   */
  public TempUnitDialog(Frame owner, Component parent, Properties settings) {
    super(owner, true);
    this.settings = settings;

    posC = parent;

    initGUI();

    listener = new TempUnitDialogListener();

    id.addActionListener(listener);
    name.addActionListener(listener);
    nameGen.addActionListener(listener);
    more.addActionListener(listener);
    ok.addActionListener(listener);
    cancel.addActionListener(listener);

    id.addKeyListener(listener);
    name.addKeyListener(listener);
    nameGen.addKeyListener(listener);
    more.addKeyListener(listener);
    ok.addKeyListener(listener);
    cancel.addKeyListener(listener);

    // Focus management/Default opening state
    boolean open = false;

    if (settings.containsKey(TempUnitDialog.SETTINGS_KEY)) {
      String s = settings.getProperty(TempUnitDialog.SETTINGS_KEY);

      if (s.equalsIgnoreCase("true")) {
        open = true;
      }
    } else {
      open = true;
    }

    if (open) {
      changeDialog();
    } else {
      setFocusList(open);
    }

    SwingUtils.setBounds(this, settings, TempUnitDialog.BOUNDS_KEY, false);
    pack();
    // loadBounds();

    addWindowListener(new WindowAdapter() {
      @Override
      public void windowActivated(WindowEvent e) {
        // dont look too close on this method. It recalls itself until this.name is
        // showing on screen and then it calls requestFocusInWindow on it (via
        // reflection api to stay compatible with jdk < 1.4
        SwingUtilities.invokeLater(new Runnable() {
          public void run() {
            if (TempUnitDialog.log.isDebugEnabled()) {
              TempUnitDialog.log.debug("TempUnitDialog.requestFocusInWindows: " + name.isShowing());
            }

            if (name.isShowing()) {
              name.requestFocusInWindow();
            } else {
              SwingUtilities.invokeLater(this);
            }
          }
        });
      }
    });
  }

  private void initGUI() {
    Container main = getContentPane();

    main.setLayout(layout = new GridBagLayout());

    getBasicConstraints();

    // //// text boxes with labels
    con.weightx = 0;
    main.add(new JLabel(Resources.get("completion.tempunitdialog.id.label")), con);
    con.gridy = 1;
    main.add(new JLabel(Resources.get("completion.tempunitdialog.name.label")), con);

    con.gridy = 0;
    con.gridx = 1;
    con.weightx = 1;
    con.fill = GridBagConstraints.HORIZONTAL;
    main.add(id = new JTextField(5), con);
    con.gridy = 1;
    nameCon = new JPanel(new BorderLayout());
    ((JPanel) nameCon).setPreferredSize(id.getPreferredSize());
    nameCon.add(name = new JTextField(20), BorderLayout.CENTER);
    main.add(nameCon, con);

    // ///////// name generator button (unused)
    nameGen = new JButton("...");
    nameGen.setEnabled(false);

    Insets insets = nameGen.getMargin();
    insets.left = 1;
    insets.right = 1;
    nameGen.setMargin(insets);
    nameGen.setFocusPainted(false);

    // ///////// more/less button
    JPanel panel = new JPanel(new FlowLayout(FlowLayout.CENTER));

    panel.add(more = new JButton(Resources.get("completion.tempunitdialog.more.more")));
    panel.setOpaque(false);
    moreButtonPanel = panel;
    main.add(moreButtonPanel, con = getMoreLessConstraints(false));

    // ///////// ok/cancel buttons
    con.gridx = 2;
    con.gridy = 0;
    con.gridwidth = 1;
    con.gridheight = 2;
    panel = new JPanel(new GridLayout(2, 1));
    panel.setOpaque(false);
    panel.add(ok = new JButton(Resources.get("completion.tempunitdialog.ok")));
    ok.setMnemonic(Resources.get("completion.tempunitdialog.ok.mnemonic").charAt(0));
    panel.add(cancel = new JButton(Resources.get("completion.tempunitdialog.cancel")));
    cancel.setMnemonic(Resources.get("completion.tempunitdialog.cancel.mnemonic").charAt(0));

    con.fill = GridBagConstraints.NONE;
    con.weightx = 0;
    main.add(panel, con);
    con.weightx = 1;
    con.fill = GridBagConstraints.HORIZONTAL;

    // /////////// details panel
    morePanel = new JPanel(new GridBagLayout());
    morePanel.setOpaque(false);
    con.gridx = 0;
    con.gridy = 0;
    con.gridwidth = 1;
    con.gridheight = 1;
    morePanel.add(new JLabel(Resources.get("completion.tempunitdialog.recruit.label")), con);
    con.gridy++;
    morePanel.add(new JLabel(Resources.get("completion.tempunitdialog.transfer.label")), con);
    con.gridy += 2;
    morePanel.add(new JLabel(Resources.get("completion.tempunitdialog.order.label")), con);
    con.gridy++;
    morePanel.add(new JLabel(Resources.get("emapdetailspanel.node.combatstatus")), con);
    con.gridy++;
    morePanel.add(new JLabel(Resources.get("completion.tempunitdialog.group.label")), con);
    con.gridy++;
    con.anchor = GridBagConstraints.NORTHWEST;
    morePanel.add(new JLabel(Resources.get("completion.tempunitdialog.descript.label")), con);
    con.anchor = GridBagConstraints.CENTER;
    con.gridy = 2;
    con.gridx = 1;
    con.fill = GridBagConstraints.HORIZONTAL;
    morePanel.add(giveRecruitCost =
        new JCheckBox(Resources.get("completion.tempunitdialog.recruitCost.label")), con);
    con.gridx = 2;
    morePanel.add(giveMaintainCost =
        new JCheckBox(Resources.get("completion.tempunitdialog.maintainCost.label")), con);
    giveRecruitCost.setSelected(settings.getProperty("TempUnitDialog.AddRecruitCost", "false")
        .equals("true"));
    giveMaintainCost.setSelected(settings.getProperty("TempUnitDialog.AddMaintainCost", "false")
        .equals("true"));

    con.gridx = 1;
    con.gridwidth = 2;
    con.gridy = 0;
    morePanel.add(recruit = new JTextField(5), con);
    con.gridy++;
    morePanel.add(transfer = new JTextField(5), con);
    con.gridy += 2;
    morePanel.add(order = new JTextField(10), con);
    con.gridy++;
    morePanel.add(combatState = new JComboBox(), con);
    con.gridy++;
    morePanel.add(group = new JComboBox(), con);
    con.gridy++;
    con.fill = GridBagConstraints.BOTH;
    morePanel.add(new JScrollPane(descript = new TablessTextArea(3, 10)), con);
    descript.setLineWrap(true);
    descript.setWrapStyleWord(true);

    // morePanel isn't added here
  }

  private GridBagConstraints getBasicConstraints() {
    if (con == null) {
      con = new GridBagConstraints();
    }

    con.gridwidth = 1;
    con.gridheight = 1;
    con.weightx = 1;
    con.anchor = GridBagConstraints.WEST;
    con.fill = GridBagConstraints.NONE;

    Insets i = new Insets(1, 2, 1, 2);

    con.insets = i;
    return con;
  }

  // overrides InternationalizedDialog.quit()
  // do not dispose
  @Override
  protected void quit() {
    settings.setProperty("TempUnitDialog.LastOrderEmpty", (order.getText().length() == 0) ? "true"
        : "false");
    approved = false;
    saveBounds();
    setVisible(false);
  }

  /**
   * @see java.awt.Dialog#setVisible(boolean)
   */
  @Override
  public void setVisible(boolean b) {
    super.setVisible(b);

    if (order != null) {
      settings.setProperty("TempUnitDialog.LastOrderEmpty", (order.getText().length() == 0)
          ? "true" : "false");
    }
  }

  protected void saveBounds() {
    Rectangle r = getBounds();
    PropertiesHelper.saveRectangle(settings, r, BOUNDS_KEY);
  }

  protected void setFocusList(boolean extended) {
    Vector<Component> components = new Vector<Component>();
    components.add(id);
    components.add(name);
    if (extended) {
      components.add(recruit);
      components.add(transfer);
      components.add(giveRecruitCost);
      components.add(giveMaintainCost);
      components.add(order);
      components.add(group);
      components.add(descript);
    }
    components.add(more);
    components.add(ok);
    components.add(cancel);

    FocusTraversalPolicy policy = new MagellanFocusTraversalPolicy(components);
    setFocusTraversalPolicy(policy);
  }

  /**
   * Displays the dialog.
   *
   * @param newID preset for id text box
   * @param newName preset for name box
   */
  public void show(String newID, String newName) {

    id.setText(newID);
    recruit.setText(null);
    transfer.setText(null);

    if (settings.getProperty("TempUnitDialog.LastOrderEmpty", "false").equals("true")) {
      order.setText(null);
    } else {
      order.setText(getFaction().getData().getGameSpecificStuff().getOrderChanger().getOrderO(
          getFaction().getLocale(), EresseaConstants.OC_LEARN).getText());
    }

    descript.setText(null);
    show(newName);
  }

  /**
   * Shows the dialog without resetting text boxes except name.
   */
  public void show(String newName) {
    approved = false;
    name.setText(newName);

    // mark whole name
    name.getCaret().setDot(0);
    name.getCaret().moveDot(name.getText().length());

    checkNameGen();
    updateGroupList();
    updateCombatState();
    pack();

    super.setVisible(true);
    saveCostStates();
  }

  protected void saveCostStates() {
    if (giveRecruitCost.isSelected()) {
      settings.setProperty("TempUnitDialog.AddRecruitCost", "true");
    } else {
      settings.remove("TempUnitDialog.AddRecruitCost");
    }

    if (giveMaintainCost.isSelected()) {
      settings.setProperty("TempUnitDialog.AddMaintainCost", "true");
    } else {
      settings.remove("TempUnitDialog.AddMaintainCost");
    }
  }

  protected void checkNameGen() {
    NameGenerator gen = NameFileNameGenerator.getInstance();

    if (gen.isActive()) {
      nameCon.add(nameGen, BorderLayout.EAST);
    } else {
      nameCon.remove(nameGen);
    }

    nameGen.setEnabled(gen.isAvailable());
  }

  /**
   * Returns <code>true</code> if ok button was pressed.
   */
  public boolean isApproved() {
    return approved;
  }

  /**
   * @return true if the "more" (detailed) view was active.
   */
  public boolean wasExtendedDialog() {
    return more.getText().equals(Resources.get("completion.tempunitdialog.more.less"));
  }

  /**
   * @return The value of the id input box
   */
  public String getID() {
    return id.getText();
  }

  /**
   * @return The value of the name input box
   */
  @Override
  public String getName() {
    return name.getText();
  }

  /**
   * @return The value of the recruit input box
   */
  public String getRecruit() {
    return recruit.getText();
  }

  /**
   * @return The value of the transfer input box
   */
  public String getTransfer() {
    return transfer.getText();
  }

  /**
   * @return The value of the order input box
   */
  public String getOrder() {
    return order.getText();
  }

  /**
   * @return The value of the group input box
   */
  public String getGroup() {
    if (group.getSelectedItem() == null)
      return null;
    return group.getSelectedItem().toString();
  }

  /**
   * @return The value of the description input box
   */
  public String getDescript() {
    return descript.getText();
  }

  /**
   * @return The value of the give recruit silver checkbox
   */
  public boolean isGiveRecruitCost() {
    return giveRecruitCost.isSelected();
  }

  /**
   * @return The value of the give maintenance silver checkbox
   */
  public boolean isGiveMaintainCost() {
    return giveMaintainCost.isSelected();
  }

  protected void changeDialog() {
    Container c = getContentPane();

    if (!more.getText().equals(Resources.get("completion.tempunitdialog.more.less"))) { // add
      layout.setConstraints(moreButtonPanel, getMoreLessConstraints(true));
      con.gridx = 0;
      con.gridy = 2;
      con.gridheight = 2;
      con.gridwidth = 2;
      con.fill = GridBagConstraints.BOTH;
      c.add(morePanel, con);
      more.setText(Resources.get("completion.tempunitdialog.more.less"));
      setFocusList(true);
      settings.setProperty(TempUnitDialog.SETTINGS_KEY, "true");
    } else { // remove
      c.remove(morePanel);
      layout.setConstraints(moreButtonPanel, getMoreLessConstraints(false));
      more.setText(Resources.get("completion.tempunitdialog.more.more"));
      setFocusList(false);
      settings.setProperty(TempUnitDialog.SETTINGS_KEY, "false");
    }

    pack();
    // setLocationRelativeTo(posC);
  }

  private GridBagConstraints getMoreLessConstraints(boolean showMore) {
    if (showMore) {
      con.gridx = 0;
      con.gridy = 4;
      con.gridwidth = 3;
      con.gridheight = 1;
      con.fill = GridBagConstraints.HORIZONTAL;

    } else {
      con.gridx = 0;
      con.gridy = 2;
      con.gridwidth = 3;
      con.gridheight = 1;
      con.fill = GridBagConstraints.HORIZONTAL;
    }
    return con;
  }

  /**
   * A text area that doesn't lose focus by tab.
   */
  protected static class TablessTextArea extends JTextArea {
    /**
     * Creates a new TablessTextArea object.
     */
    public TablessTextArea(int rows, int columns) {
      super(rows, columns);
    }

    /**
     * Always returns false.
     *
     * @see javax.swing.JComponent#isManagingFocus()
     */
    @Override
    public boolean isManagingFocus() {
      return false;
    }
  }

  /**
   * Reacts to events.
   *
   * @author stm
   */
  public class TempUnitDialogListener extends KeyAdapter implements ActionListener, KeyListener {

    /**
     * Reacts on buttons and ENTER in text fields.
     *
     * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
     */
    public void actionPerformed(java.awt.event.ActionEvent p1) {
      if (p1.getSource() == more) {
        changeDialog();

        return;
      } else if (p1.getSource() == nameGen) {
        NameGenerator gen = NameFileNameGenerator.getInstance();
        name.setText(gen.getName());
        if (!gen.isAvailable()) {
          JOptionPane.showMessageDialog(new JFrame(), Resources.get("util.namegenerator.nomorenames"));
          nameGen.setEnabled(false);
        }

        return;
      }

      approved = (p1.getSource() instanceof JTextField) || (p1.getSource() == ok);
      saveBounds();
      setVisible(false);
    }

    /**
     * Quits dialog when user pressed ESC.
     *
     * @see java.awt.event.KeyAdapter#keyReleased(java.awt.event.KeyEvent)
     */
    @Override
    public void keyReleased(KeyEvent e) {
      if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
        approved = false;
        saveBounds();
        setVisible(false);
      }
    }

  }

  /**
   * Returns the value of faction.
   *
   * @return Returns faction.
   */
  public Faction getFaction() {
    return faction;
  }

  /**
   * Sets the value of faction.
   *
   * @param faction The value for faction.
   */
  public void setFaction(Faction faction) {
    this.faction = faction;
  }

  /**
   * Returns the value of parentGroup.
   *
   * @return Returns parentGroup.
   */
  public Group getParentGroup() {
    return parentGroup;
  }

  /**
   * Sets the value of parentGroup.
   *
   * @param parentGroup The value for parentGroup.
   */
  public void setParentGroup(Group parentGroup) {
    this.parentGroup = parentGroup;
  }

  /**
   * This method updates the group data inside the TEMP Unit dialog. It adds all available groups.
   * Then it selecteds the Group of the current unit.
   */
  public void updateGroupList() {
    group.removeAllItems();
    group.addItem(""); // default
    if (faction != null && faction.getGroups() != null) {
      for (Group factionGroup : faction.getGroups().values()) {
        group.addItem(factionGroup.getName());
      }
    }

    if (parentGroup != null) {
      group.setSelectedItem(parentGroup.getName());
    }

    group.setEditable(true);
  }

  /**
   * Sets the value of combatStatus.
   *
   * @param combatStatus The value for combatStatus.
   */
  public void setCombatState(int combatStatus) {
    this.combatStatus = combatStatus;
  }

  /**
   * Returns the selected combatState or -1 if it is not set.
   */
  public int getCombatState() {
    return ((CSEntry) combatState.getSelectedItem()).status;
  }

  static final class CSEntry {
    int status;
    String resourceKey;
    String display;

    CSEntry(int status, String resourceKey, String display) {
      this.status = status;
      this.resourceKey = resourceKey;
      this.display = display;
    }

    @Override
    public String toString() {
      return display;
    }
  }

  /**
   * This method updates the combobox with the possible combat states and sets the new possible
   * combat state based on the parent unit combat state.
   */
  public void updateCombatState() {

    combatState.removeAllItems();
    if (faction != null) {
      Map<Integer, String> combatStates =
          faction.getData().getGameSpecificStuff().getCombatStates();
      for (Entry<Integer, String> stateEntry : combatStates.entrySet()) {
        CSEntry item;
        combatState.addItem(item =
            new CSEntry(stateEntry.getKey(), stateEntry.getValue(), Resources.get(stateEntry
                .getValue())));
        if (combatStatus == stateEntry.getKey()) {
          combatState.setSelectedItem(item);
        }
      }
    }
  }
}
