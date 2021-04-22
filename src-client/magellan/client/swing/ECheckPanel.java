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
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Cursor;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.LineNumberReader;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.ListCellRenderer;
import javax.swing.UIDefaults;
import javax.swing.UIManager;
import javax.swing.border.EmptyBorder;
import javax.swing.border.MatteBorder;
import javax.swing.border.TitledBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import magellan.client.desktop.DesktopEnvironment;
import magellan.client.desktop.MagellanDesktop;
import magellan.client.desktop.ShortcutListener;
import magellan.client.event.EventDispatcher;
import magellan.client.event.SelectionEvent;
import magellan.client.event.SelectionListener;
import magellan.library.Faction;
import magellan.library.GameData;
import magellan.library.Region;
import magellan.library.event.GameDataEvent;
import magellan.library.gamebinding.EresseaConstants;
import magellan.library.gamebinding.GameSpecificOrderWriter;
import magellan.library.utils.Encoding;
import magellan.library.utils.JECheck;
import magellan.library.utils.JECheck.ECheckMessage;
import magellan.library.utils.JVMUtilities;
import magellan.library.utils.PropertiesHelper;
import magellan.library.utils.Resources;
import magellan.library.utils.TrustLevels;
import magellan.library.utils.logging.Logger;

/**
 * A panel for showing statistics about factions.
 */
public class ECheckPanel extends InternationalizedDataPanel implements SelectionListener,
    ShortcutListener {
  private static final Logger log = Logger.getInstance(ECheckPanel.class);

  /** @deprecated Use {@link MagellanDesktop#ECHECK_IDENTIFIER} instead */
  @Deprecated
  public static final String IDENTIFIER = MagellanDesktop.ECHECK_IDENTIFIER;

  private JList<ECheckMessage> lstMessages;
  private JComboBox<Faction> cmbFactions;
  private JTextArea txtOutput;
  private File orderFile;
  private JComboBox<String> usedOptions = new JComboBox<String>();
  private JTextField txtOptions = (JTextField) usedOptions.getEditor().getEditorComponent();
  private JCheckBox chkConfirmedOnly;
  private JCheckBox chkSelRegionsOnly;
  private Collection<Region> regions;

  // shortcuts
  private List<KeyStroke> shortcuts;

  private Container pnlEcheckOutput;

  private JPanel pnlOutput;

  private JButton btnHide;

  /**
   * Creates a new ECheckPanel object.
   */
  public ECheckPanel(EventDispatcher dispatcher, GameData initData, Properties p,
      Collection<Region> regions) {
    super(dispatcher, initData, p);
    this.regions = new ArrayList<Region>(regions);
    init(dispatcher);
  }

  /**
   * Creates a new ECheckPanel object.
   */
  public ECheckPanel(EventDispatcher dispatcher, GameData initData, Properties p) {
    super(dispatcher, initData, p);
    init(dispatcher);
  }

  private void init(EventDispatcher d) {
    d.addSelectionListener(this);
    JPanel echeckPanel = new JPanel();
    echeckPanel.setLayout(new BorderLayout());

    JPanel northPanel = new JPanel(new BorderLayout(1, 3));
    northPanel.add(getControlsPanel(), BorderLayout.CENTER);
    northPanel.add(getButtonPanel(), BorderLayout.EAST);

    echeckPanel.add(northPanel, BorderLayout.NORTH);

    pnlOutput = new JPanel(new GridLayout(0, 1));
    pnlOutput.add(getMessagesPanel());
    pnlEcheckOutput = getOutputPanel();
    pnlEcheckOutput.setVisible(false);
    // don't add here, it is added later
    // pnlOutput.add(pnlEcheckOutput);

    echeckPanel.add(pnlOutput, BorderLayout.CENTER);

    JPanel mainPanel = new JPanel(new BorderLayout(6, 0));
    mainPanel.setBorder(new EmptyBorder(4, 4, 4, 4));
    mainPanel.add(echeckPanel, BorderLayout.CENTER);

    setLayout(new BorderLayout());
    this.add(mainPanel, BorderLayout.CENTER);

    // initialize shortcuts
    shortcuts = new ArrayList<KeyStroke>(1);

    // 0: Focus
    shortcuts.add(KeyStroke.getKeyStroke(KeyEvent.VK_E, InputEvent.CTRL_DOWN_MASK));

    // register for shortcuts
    DesktopEnvironment.registerShortcutListener(this);
  }

  private Container getButtonPanel() {
    JButton btnRun = new JButton(Resources.get("echeckdialog.btn.run.caption"));
    btnRun.setMnemonic(Resources.get("echeckdialog.btn.run.mnemonic").charAt(0));
    btnRun.setDefaultCapable(true);
    btnRun.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        runECheck();
      }
    });

    /*
     * JButton btnClose = new JButton(Resources.get("echeckdialog.btn.close.caption"));
     * btnClose.setMnemonic(Resources.get("echeckdialog.btn.close.mnemonic").charAt(0));
     * btnClose.addActionListener(new ActionListener() { public void actionPerformed(ActionEvent e)
     * { quit(); } }); btnClose.setEnabled(false);
     */

    JButton btnHelp = new JButton(Resources.get("echeckdialog.btn.help.caption"));
    btnHelp.setMnemonic(Resources.get("echeckdialog.btn.help.mnemonic").charAt(0));
    btnHelp.setDefaultCapable(false);
    btnHelp.addActionListener(new ActionListener() {

      public void actionPerformed(ActionEvent e) {
        showHelp();
      }

    });

    btnHide = new JButton(Resources.get("echeckdialog.btn.details.caption.on"));
    btnHide.setMnemonic(Resources.get("echeckdialog.btn.details.mnemonic").charAt(0));
    btnHide.setDefaultCapable(false);
    btnHide.addActionListener(new ActionListener() {

      public void actionPerformed(ActionEvent e) {
        toggleECheckOutput();
      }

    });

    JPanel buttonPanel = new JPanel(new GridBagLayout());
    GridBagConstraints c =
        new GridBagConstraints(0, 0, 1, 1, 0, 0, GridBagConstraints.NORTH,
            GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 0), 0, 0);
    buttonPanel.add(btnRun, c);
    // buttonPanel.add(btnClose);
    c.gridy++;
    buttonPanel.add(btnHelp, c);
    c.gridy++;
    buttonPanel.add(btnHide, c);
    c.gridy++;
    c.fill = GridBagConstraints.BOTH;
    c.weighty = 1.0;
    JPanel filler = new JPanel();
    // filler.setPreferredSize(new Dimension(1, 1000));
    buttonPanel.add(filler, c);
    return buttonPanel;
  }

  private void toggleECheckOutput() {
    GridLayout l = (GridLayout) pnlOutput.getLayout();
    if (pnlEcheckOutput.isVisible()) {
      pnlEcheckOutput.setVisible(false);
      pnlOutput.remove(pnlEcheckOutput);
      l.setColumns(1);
      btnHide.setText(Resources.get("echeckdialog.btn.details.caption.on"));
    } else {
      pnlEcheckOutput.setVisible(true);
      l.setColumns(2);
      pnlOutput.add(pnlEcheckOutput);
      btnHide.setText(Resources.get("echeckdialog.btn.details.caption.off"));
    }
    // invalidate();
    validate();
    repaint();
  }

  /**
   * @see SelectionListener#selectionChanged(SelectionEvent)
   */
  public void selectionChanged(SelectionEvent e) {
    if (e.getSelectionType() != SelectionEvent.ST_REGIONS)
      return;
    if (e.getSelectedObjects() != null) {
      if (regions == null) {
        regions = new HashSet<Region>();
      } else {
        regions.clear();
      }

      for (Object o : e.getSelectedObjects()) {
        if (o instanceof Region) {
          regions.add((Region) o);
        }
      }

      chkSelRegionsOnly.setEnabled((regions != null) && (regions.size() > 0));
    }
  }

  /**
   * 
   */
  @Override
  public void gameDataChanged(GameDataEvent e) {
    setGameData(e.getGameData());

    if (cmbFactions != null) {
      cmbFactions.removeAllItems();
      for (Faction f : getGameData().getFactions()) {
        if (TrustLevels.isPrivileged(f)) {
          cmbFactions.addItem(f);
        }
      }
      if (cmbFactions.getItemCount() > 0) {
        cmbFactions.setSelectedIndex(0);
      }
    }
  }

  private void showHelp() {
    // check the given executable
    File exeFile = new File(settings.getProperty("JECheckPanel.echeckEXE", "echeck.exe"));

    if (!exeFile.exists()) {
      JOptionPane.showMessageDialog(getRootPane(), Resources
          .get("echeckpanel.msg.echeckmissing.text"), Resources
              .get("echeckpanel.msg.echeckmissing.title"), JOptionPane.ERROR_MESSAGE);

      return;
    }

    try {
      ECheckMessage result = JECheck.getHelp(exeFile, settings);
      txtOutput.setText("");
      lstMessages.setListData(new ECheckMessage[] { result });
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  /**
   * This method starts ECheck and tries to find any problems in the orders.
   */
  public void runECheck() {
    // check the selected faction
    Faction selectedFaction = (Faction) cmbFactions.getSelectedItem();

    if (selectedFaction == null) {
      JOptionPane.showMessageDialog(getRootPane(), Resources.get("echeckpanel.msg.nofaction.text"),
          Resources.get("echeckpanel.msg.nofaction.title"), JOptionPane.ERROR_MESSAGE);

      return;
    }

    String options = txtOptions.getText();

    /**
     * If user changed the options save them
     */
    if (!options.equalsIgnoreCase(getDefaultOptions(selectedFaction))) {
      usedOptions.removeItem(options);
      usedOptions.insertItemAt(options, 0);
      usedOptions.setSelectedIndex(0);

      if (usedOptions.getItemCount() > 5) {
        usedOptions.removeItemAt(5);
      }

      String s = "";

      for (int i = 0; i < usedOptions.getItemCount(); i++) {
        s += (usedOptions.getItemAt(i) + ";");
      }

      settings.setProperty("ECheckPanel.options." + selectedFaction.getID().toString(), s);
    }

    // check the given executable
    File exeFile = new File(settings.getProperty("JECheckPanel.echeckEXE", "echeck.exe"));

    if (!exeFile.exists()) {
      JOptionPane.showMessageDialog(getRootPane(), Resources
          .get("echeckpanel.msg.echeckmissing.text"), Resources
              .get("echeckpanel.msg.echeckmissing.title"), JOptionPane.ERROR_MESSAGE);

      return;
    }

    setCursor(new Cursor(Cursor.WAIT_CURSOR));

    /* check version */
    try {
      if (!JECheck.checkVersion(exeFile, settings)) {
        setCursor(new Cursor(Cursor.DEFAULT_CURSOR));

        Object msgArgs[] = { JECheck.getRequiredVersion() };
        JOptionPane.showMessageDialog(getRootPane(), java.text.MessageFormat.format(Resources
            .get("echeckpanel.msg.wrongversion.text"), msgArgs), Resources
                .get("echeckpanel.msg.wrongversion.title"), JOptionPane.ERROR_MESSAGE);

        return;
      }
    } catch (IOException e) {
      setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
      JOptionPane.showMessageDialog(getRootPane(), Resources
          .get("echeckpanel.msg.versionretrievalerror.text"), Resources
              .get("echeckpanel.msg.versionretrievalerror.title"), JOptionPane.ERROR_MESSAGE);

      return;
    }

    // save orders
    if (orderFile != null) {
      orderFile.delete();
    }

    Writer stream = null;
    try {
      orderFile = File.createTempFile("orders", null);

      // apexo (Fiete) 20061205: if in properties, force ISO encoding
      if (PropertiesHelper.getBoolean(settings, "TextEncoding.ISOrunEcheck", false)) {
        // new: force our default = ISO
        stream = new OutputStreamWriter(new FileOutputStream(orderFile), Encoding.ISO.toString());
      } else if (PropertiesHelper.getBoolean(settings, "TextEncoding.UTF8runEcheck", false)) {
        // new: force our default = UTF8
        stream = new OutputStreamWriter(new FileOutputStream(orderFile), Encoding.UTF8.toString());
      } else {
        // old = default = system dependent
        stream = new FileWriter(orderFile);
      }
      GameSpecificOrderWriter cmdWriter = getGameData().getGameSpecificStuff().getOrderWriter();
      cmdWriter.setGameData(getGameData());
      cmdWriter.setFaction(selectedFaction);
      cmdWriter.setECheckOptions(options);

      if (chkSelRegionsOnly.isSelected() && (regions != null) && (regions.size() > 0)) {
        cmdWriter.setRegions(regions);
      }

      cmdWriter.setConfirmedOnly(chkConfirmedOnly.isSelected());
      cmdWriter.write(stream);
      stream.close();
    } catch (IOException e) {
      setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
      JOptionPane.showMessageDialog(getRootPane(), Resources
          .get("echeckpanel.msg.ordersaveerror.text")
          + e, Resources.get("echeckpanel.msg.ordersaveerror.title"), JOptionPane.ERROR_MESSAGE);

      if (orderFile != null) {
        orderFile.delete();
        orderFile = null;
      }

      return;
    } finally {
      if (stream != null) {
        try {
          stream.close();
        } catch (IOException e) {
          log.error(e);
        }
      }
    }

    // run ECheck and display the ECheck output
    try {
      LineNumberReader r = new LineNumberReader(new JECheck(exeFile, orderFile, options, settings));
      StringBuffer sb = new StringBuffer();

      while (r.ready()) {
        sb.append(r.readLine()).append("\n");
      }

      r.close();
      txtOutput.setText(sb.toString());
    } catch (IOException e) {
      ECheckPanel.log.error("ECheckPanel.runECheck(): error while reading ECheck output", e);
    }

    // run echeck to get the messages
    try {
      List<ECheckMessage> messages =
          new LinkedList<ECheckMessage>(JECheck.getMessages(exeFile, orderFile, options, settings));

      if (messages.size() > 0) {
        JECheck.determineAffectedObjects(getGameData(), orderFile, messages);
      } else {
        JOptionPane.showMessageDialog(getRootPane(), Resources
            .get("echeckpanel.msg.noecheckmessages.text"), Resources
                .get("echeckpanel.msg.noecheckmessages.title"), JOptionPane.INFORMATION_MESSAGE);
      }
      lstMessages.setListData(messages.toArray(new ECheckMessage[] {}));
    } catch (IOException e) {
      setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
      JOptionPane.showMessageDialog(getRootPane(), Resources
          .get("echeckpanel.msg.echeckerror.text")
          + e, Resources.get("echeckpanel.msg.echeckerror.title"), JOptionPane.ERROR_MESSAGE);

      if (orderFile != null) {
        orderFile.delete();
        orderFile = null;
      }

      setCursor(new Cursor(Cursor.DEFAULT_CURSOR));

      return;
    } catch (java.text.ParseException e) {
      setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
      JOptionPane.showMessageDialog(getRootPane(), Resources.get("echeckpanel.msg.parseerror.text")
          + e, Resources.get("echeckpanel.msg.parseerror.title"), JOptionPane.ERROR_MESSAGE);

      if (orderFile != null) {
        orderFile.delete();
        orderFile = null;
      }
    }

    if (orderFile != null) {
      orderFile.delete();
      orderFile = null;
    }
    setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
  }

  /**
   * 
   */
  @Override
  public void quit() {
    if (dispatcher != null) {
      dispatcher.removeSelectionListener(this);
    }

    super.quit();

    if (orderFile != null) {
      orderFile.delete();
    }
  }

  private Container getControlsPanel() {
    usedOptions.setEditable(true);

    JLabel lblFactions = new JLabel(Resources.get("echeckpanel.lbl.faction.caption"));
    lblFactions.setDisplayedMnemonic(Resources.get("echeckpanel.lbl.faction.mnemonic").charAt(0));
    cmbFactions = new JComboBox<Faction>();
    lblFactions.setLabelFor(cmbFactions);

    for (Faction f : getGameData().getFactions()) {
      if (TrustLevels.isPrivileged(f)) {
        cmbFactions.addItem(f);
      }
    }

    cmbFactions.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        Faction f = (Faction) cmbFactions.getSelectedItem();

        if (f != null) {
          // set the saved options sets of the new faction
          usedOptions.removeAllItems();

          String s = settings.getProperty("ECheckPanel.options." + f.getID().toString());

          if ((s != null) && (s.length() > 0)) {
            String temp = "";

            for (int i = 0; i < s.length(); i++) {
              if (s.charAt(i) != ';') {
                temp += s.charAt(i);
              } else {
                usedOptions.addItem(temp);
                temp = "";
              }
            }

            if (usedOptions.getItemCount() > 0) {
              usedOptions.setSelectedIndex(0);
            } else {
              txtOptions.setText(getDefaultOptions(f));
            }
          } else {
            txtOptions.setText(getDefaultOptions(f));
          }
        }
      }
    });

    chkConfirmedOnly =
        new JCheckBox(Resources.get("echeckpanel.chk.skipunconfirmedorders.caption"));

    chkSelRegionsOnly = new JCheckBox(Resources.get("echeckpanel.chk.selectedregions.caption"));
    chkSelRegionsOnly.setEnabled((regions != null) && (regions.size() > 0));

    JLabel lblOptions = new JLabel(Resources.get("echeckpanel.lbl.options.caption"));
    lblOptions.setDisplayedMnemonic(Resources.get("echeckpanel.lbl.options.mnemonic").charAt(0));
    lblOptions.setLabelFor(usedOptions);

    /* trigger option creation */
    if (cmbFactions.getItemCount() > 0) {
      cmbFactions.setSelectedIndex(0);
    }

    JPanel controls = new JPanel(new GridBagLayout());

    GridBagConstraints c = new GridBagConstraints();
    c.anchor = GridBagConstraints.WEST;
    c.insets = new Insets(2, 1, 2, 1);
    c.gridy = 0;

    // c.gridx = 0;
    // c.gridy = 0;
    // c.fill = GridBagConstraints.NONE;
    // c.weightx = 0.0;
    // c.weighty = 0.0;
    // controls.add(lblECheckEXE, c);
    //
    // c.gridx = 1;
    // c.gridy = 0;
    // c.fill = GridBagConstraints.HORIZONTAL;
    // c.weightx = 1.0;
    // c.weighty = 0.0;
    // controls.add(txtECheckEXE, c);
    //
    // c.gridx = 2;
    // c.gridy = 0;
    // c.fill = GridBagConstraints.NONE;
    // c.weightx = 0.0;
    // c.weighty = 0.0;
    // controls.add(btnBrowse, c);

    c.gridx = 0;
    c.gridy++;
    c.fill = GridBagConstraints.NONE;
    c.weightx = 0.0;
    c.weighty = 0.0;
    controls.add(lblFactions, c);

    c.gridx = 1;
    // c.gridy = 1;
    c.fill = GridBagConstraints.HORIZONTAL;
    c.weightx = 1.0;
    c.weighty = 0.0;
    c.gridwidth = 2;
    controls.add(cmbFactions, c);

    c.gridx = 0;
    c.gridy++;
    c.fill = GridBagConstraints.NONE;
    c.weightx = 0.0;
    c.weighty = 0.0;
    c.gridwidth = 1;
    controls.add(lblOptions, c);

    c.gridx = 1;
    // c.gridy = 2;
    c.fill = GridBagConstraints.HORIZONTAL;
    c.weightx = 1.0;
    c.weighty = 0.0;
    c.gridwidth = 2;
    controls.add(usedOptions, c);

    c.gridx = 0;
    c.gridy++;
    c.fill = GridBagConstraints.HORIZONTAL;
    c.weightx = 0.0;
    c.weighty = 0.0;
    c.gridwidth = 2;
    controls.add(chkConfirmedOnly, c);

    c.gridx = 0;
    c.gridy++;
    c.fill = GridBagConstraints.HORIZONTAL;
    c.weightx = 0.0;
    c.weighty = 0.0;
    c.gridwidth = 2;
    controls.add(chkSelRegionsOnly, c);

    return controls;
  }

  private Container getMessagesPanel() {
    lstMessages = new JList<ECheckMessage>();
    lstMessages.setCellRenderer(new ECheckMessageRenderer());
    lstMessages.addListSelectionListener(new ListSelectionListener() {
      public void valueChanged(ListSelectionEvent e) {
        if (!e.getValueIsAdjusting()) {
          JECheck.ECheckMessage msg = lstMessages.getSelectedValue();

          if (msg == null || msg.getType() == ECheckMessage.MESSAGE)
            return;

          if (msg.getAffectedObject() != null) {
            dispatcher.fire(SelectionEvent.create(ECheckPanel.this, msg.getAffectedObject(),
                SelectionEvent.ST_DEFAULT));
          } else {
            JOptionPane.showMessageDialog(ECheckPanel.this.getRootPane(), Resources
                .get("echeckpanel.msg.messagetargetnotfound.text"), Resources
                    .get("echeckpanel.msg.messagetargetnotfound.title"),
                JOptionPane.INFORMATION_MESSAGE);
          }
        }
      }
    });

    JPanel messages = new JPanel(new BorderLayout());
    messages.setBorder(new TitledBorder(BorderFactory.createEtchedBorder(), Resources
        .get("echeckpanel.border.echeckmessages")));
    messages.add(new JScrollPane(lstMessages), BorderLayout.CENTER);

    return messages;
  }

  private Container getOutputPanel() {
    txtOutput = new JTextArea();
    txtOutput.setEditable(false);

    JPanel orders = new JPanel(new BorderLayout());
    orders.setBorder(new TitledBorder(BorderFactory.createEtchedBorder(), Resources
        .get("echeckpanel.border.orders")));
    orders.add(new JScrollPane(txtOutput), BorderLayout.CENTER);
    return orders;
  }

  /**
   * Returns a file name by showing the user a file selection dialog. If the user's selection is
   * empty, null is returned.
   */
  // private String getFileName(String defaultFile) {
  // String retVal = null;
  //
  // javax.swing.filechooser.FileFilter ff = new javax.swing.filechooser.FileFilter() {
  // @Override
  // public boolean accept(File f) {
  // String fileName = f.getName().toLowerCase();
  //
  // return f.isDirectory() || (fileName.endsWith(".exe") || fileName.equals("echeck"));
  // }
  //
  // @Override
  // public String getDescription() {
  // return Resources.get("echeckpanel.filter.echeckexe.desc");
  // }
  // };
  //
  // JFileChooser fc = new JFileChooser();
  // fc.addChoosableFileFilter(ff);
  // fc.setFileFilter(ff);
  // fc.setMultiSelectionEnabled(false);
  //
  // if(defaultFile != null) {
  // fc.setSelectedFile(new File(defaultFile));
  // }
  //
  // if(fc.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
  // retVal = fc.getSelectedFile().getAbsolutePath();
  // }
  //
  // return retVal;
  // }

  /**
   * Default echeck options for given faction.
   */
  private String getDefaultOptions(Faction f) {
    String options = "";

    if ((f.getType() != null) && (f.getRace().getRecruitmentCosts() > 0)) {
      options += (" -r" + f.getRace().getRecruitmentCosts());
    }

    if ((f.getOptions() != null) && f.getOptions().isActive(EresseaConstants.OC_SILVERPOOL)) {
      options += " -l";
    }

    if (f.getLocale() != null) {
      options += (" -L" + f.getLocale().getLanguage());
    }

    // Warning Level
    options += " -w2";

    return options;
  }

  private static final class ECheckMessageRenderer extends JPanel implements ListCellRenderer<ECheckMessage> {
    private static javax.swing.border.Border focusedBorder = null;
    private static javax.swing.border.Border selectedBorder = null;
    private static javax.swing.border.Border plainBorder = null;
    private static Color textForeground = null;
    private static Color textBackground = null;
    private static Color selectedForeground = null;
    private static Color selectedBackground = null;
    private JLabel lblCaption = null;
    private JTextArea txtMessage = null;

    /**
     * Creates a new ECheckMessageRenderer object.
     */
    public ECheckMessageRenderer() {
      GridBagConstraints c = new GridBagConstraints();

      lblCaption = new JLabel();
      lblCaption.setOpaque(false);

      txtMessage = new JTextArea();
      txtMessage.setOpaque(false);

      setLayout(new GridBagLayout());

      c.anchor = GridBagConstraints.CENTER;
      c.gridx = 0;
      c.gridy = 0;
      c.gridwidth = 1;
      c.gridheight = 1;
      c.fill = GridBagConstraints.HORIZONTAL;
      c.weightx = 0.5;
      c.weighty = 0.0;
      c.insets = new Insets(0, 0, 0, 0);
      this.add(lblCaption, c);

      c.anchor = GridBagConstraints.CENTER;
      c.gridx = 0;
      c.gridy = 1;
      c.gridwidth = 1;
      c.gridheight = 1;
      c.fill = GridBagConstraints.HORIZONTAL;
      c.weightx = 0.0;
      c.weighty = 0.0;
      c.insets = new Insets(0, 8, 0, 0);
      this.add(txtMessage, c);

      applyUIDefaults();
    }

    /**
     * 
     * @see javax.swing.ListCellRenderer#getListCellRendererComponent(javax.swing.JList,
     *      java.lang.Object, int, boolean, boolean)
     */
    public Component getListCellRendererComponent(JList<? extends ECheckMessage> list, ECheckMessage m, int index,
        boolean isSelected, boolean cellHasFocus) {
      if (isSelected) {
        setBackground(ECheckMessageRenderer.selectedBackground);
        txtMessage.setBackground(ECheckMessageRenderer.selectedBackground);
        lblCaption.setForeground(ECheckMessageRenderer.selectedForeground);
        txtMessage.setForeground(ECheckMessageRenderer.selectedForeground);
      } else {
        setBackground(ECheckMessageRenderer.textBackground);
        txtMessage.setBackground(ECheckMessageRenderer.textBackground);
        lblCaption.setForeground(ECheckMessageRenderer.textForeground);
        txtMessage.setForeground(ECheckMessageRenderer.textForeground);
      }

      if (cellHasFocus) {
        setBorder(ECheckMessageRenderer.focusedBorder);
      } else {
        if (isSelected) {
          setBorder(ECheckMessageRenderer.selectedBorder);
        } else {
          setBorder(ECheckMessageRenderer.plainBorder);
        }
      }

      if (m == null) {
        lblCaption.setText("");
        txtMessage.setText("");
      } else {

        if (m.getType() == JECheck.ECheckMessage.WARNING) {
          lblCaption.setText(Resources.get("echeckpanel.celllbl.warning", new Object[] { m
              .getLineNr() }));
        } else if (m.getType() == JECheck.ECheckMessage.ERROR) {
          lblCaption.setText(Resources.get("echeckpanel.celllbl.error", new Object[] { m
              .getLineNr() }));
        } else {
          lblCaption.setText("");
        }

        txtMessage.setText(m.getMessage());
      }

      return this;
    }

    private void applyUIDefaults() {
      UIDefaults defaults = UIManager.getDefaults();
      ECheckMessageRenderer.textForeground = Color.BLACK;
      ECheckMessageRenderer.textForeground = (Color) defaults.get("Tree.textForeground");
      ECheckMessageRenderer.textBackground = Color.WHITE;
      ECheckMessageRenderer.textBackground = (Color) defaults.get("Tree.textBackground");
      ECheckMessageRenderer.selectedForeground = Color.BLACK;
      ECheckMessageRenderer.selectedForeground = (Color) defaults.get("Tree.selectionForeground");
      ECheckMessageRenderer.selectedBackground = Color.WHITE;
      ECheckMessageRenderer.selectedBackground = (Color) defaults.get("Tree.selectionBackground");

      // pavkovic 2003.10.17: prevent jvm 1.4.2_01 bug
      ECheckMessageRenderer.focusedBorder =
          new MatteBorder(1, 1, 1, 1, JVMUtilities.getTreeSelectionBorderColor());
      ECheckMessageRenderer.selectedBorder =
          new MatteBorder(1, 1, 1, 1, ECheckMessageRenderer.selectedBackground);
      ECheckMessageRenderer.plainBorder = new EmptyBorder(1, 1, 1, 1);

      setOpaque(true);
      setBackground(ECheckMessageRenderer.textBackground);
      lblCaption.setForeground(ECheckMessageRenderer.textBackground);
      txtMessage.setForeground(ECheckMessageRenderer.textBackground);
    }
  }

  /**
   * Returns the true if only units of selected regions should be checked.
   */
  public boolean getSelRegionsOnly() {
    return chkSelRegionsOnly.isSelected();
  }

  /**
   * Set to true if only unitsof selected regions should be checked.
   */
  public void setSelRegionsOnly(boolean b) {
    chkSelRegionsOnly.setSelected(b);
  }

  /**
   * Returns true if only confirmed units should be checked.
   */
  public boolean getConfirmedOnly() {
    return chkConfirmedOnly.isSelected();
  }

  /**
   * Set to true if only confirmed units should be checked.
   */
  public void setConfirmedOnly(boolean b) {
    chkConfirmedOnly.setSelected(b);
  }

  /**
   * Should return all short cuts this class want to be informed. The elements should be of type
   * javax.swing.KeyStroke
   */
  public Iterator<KeyStroke> getShortCuts() {
    return shortcuts.iterator();
  }

  /**
   * This method is called when a shortcut from getShortCuts() is recognized.
   * 
   * @param shortcut
   * @see magellan.client.desktop.ShortcutListener#shortCut(javax.swing.KeyStroke)
   */
  public void shortCut(javax.swing.KeyStroke shortcut) {
    int index = shortcuts.indexOf(shortcut);

    switch (index) {
    case -1:
      break; // unknown shortcut

    case 0:
      DesktopEnvironment.requestFocus(MagellanDesktop.ECHECK_IDENTIFIER);
      break;
    }
  }

  /**
   * @see magellan.client.desktop.ShortcutListener#getShortcutDescription(KeyStroke)
   */
  public String getShortcutDescription(KeyStroke stroke) {
    int index = shortcuts.indexOf(stroke);

    return Resources.get("echeckpanel.shortcut.description." + String.valueOf(index));
  }

  /**
   * @see magellan.client.desktop.ShortcutListener#getListenerDescription()
   */
  public String getListenerDescription() {
    return Resources.get("echeckpanel.shortcut.title");
  }

}
