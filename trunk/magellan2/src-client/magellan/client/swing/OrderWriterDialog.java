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
import java.awt.Cursor;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map.Entry;
import java.util.Properties;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.DESedeKeySpec;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;

import magellan.client.swing.layout.GridLayout2;
import magellan.client.utils.FileNameGenerator;
import magellan.client.utils.SwingUtils;
import magellan.library.EntityID;
import magellan.library.Faction;
import magellan.library.GameData;
import magellan.library.Group;
import magellan.library.Region;
import magellan.library.TempUnit;
import magellan.library.Unit;
import magellan.library.io.file.FileBackup;
import magellan.library.utils.Encoding;
import magellan.library.utils.FileNameGeneratorFeed;
import magellan.library.utils.FixedWidthWriter;
import magellan.library.utils.OrderWriter;
import magellan.library.utils.PropertiesHelper;
import magellan.library.utils.Resources;
import magellan.library.utils.UserInterface;
import magellan.library.utils.comparator.NameComparator;
import magellan.library.utils.logging.Logger;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.mail.EmailException;
import org.apache.commons.mail.MultiPartEmail;

/**
 * A GUI for writing orders to a file or copy them to the clipboard. This class can be used as a
 * stand-alone application or can be integrated as dialog into another application.
 */
public class OrderWriterDialog extends InternationalizedDataDialog {
  private static final Logger log = Logger.getInstance(OrderWriterDialog.class);

  private static final int FILE_PANEL = 0;
  private static final int CLIPBOARD_PANEL = 1;
  private static final int EMAIL_PANEL = 2;

  protected static final int DEFAULT_MAILSERVER_PORT = 25;
  protected static final int DEFAULT_FIXED_WIDTH = 76;

  protected static final String DEFAULT_EMAIL = "eressea-server@eressea.de";
  protected static final String DEFAULT_SUBJECT = "Eressea Befehle";

  private boolean standAlone = false;
  private Collection<Region> regions;

  // file
  private JComboBox cmbOutputFile;
  private JTextField txtOutputFileGenerated;

  // email
  private JCheckBox chkUseSettingsFromCR;
  private JCheckBox chkCCToSender;
  private JCheckBox chkUseSSL;
  private JCheckBox chkUseTLS;
  private JTextField txtMailServer;
  private JTextField txtMailServerPort;
  private JTextField txtMailRecipient;
  private JTextField txtMailRecipient2;
  private JTextField txtMailSender;
  private JTextField txtMailSubject;
  private JTextField txtServerUsername;
  private JPasswordField txtServerPassword;
  private JCheckBox chkUseAuth;
  private JCheckBox chkAskPassword;
  private JLabel lblServerUsername;
  private JLabel lblServerPassword;
  private JLabel lblMailRecipient;
  private JLabel lblMailSubject;
  private JLabel lblMailRecipient2;

  // all
  private JCheckBox chkFixedWidth[] = new JCheckBox[3];
  private JTextField txtFixedWidth[] = new JTextField[3];
  private JCheckBox chkECheckComments[] = new JCheckBox[3];
  private JCheckBox chkRemoveSCComments[] = new JCheckBox[3];
  private JCheckBox chkRemoveSSComments[] = new JCheckBox[3];
  private JCheckBox chkConfirmedOnly[] = new JCheckBox[3];
  private JCheckBox chkSelRegionsOnly[] = new JCheckBox[3];
  private JCheckBox chkWriteUnitTagsAsVorlageComment[] = new JCheckBox[3];

  private JComboBox cmbFactions;
  private JComboBox cmbGroups;

  private Container ancestor;

  private JCheckBox chkAutoFileName;

  private Properties localSettings;

  private String algorithm = "DESede";

  private Cipher cipher;

  private SecretKey key;

  /**
   * Create a stand-alone instance of OrderWriterDialog.
   */
  public OrderWriterDialog(GameData data) {
    super(null, false, null, data, new Properties());
    standAlone = true;

    try {
      settings.load(new FileInputStream(new File(System.getProperty("user.home"),
          "OrderWriterDialog.ini")));
    } catch (IOException e) {
      OrderWriterDialog.log.error("OrderWriterDialog.OrderWriterDialog(),", e);
    }

    init();
  }

  /**
   * Create a new OrderWriterDialog object as a dialog with a parent window.
   */
  public OrderWriterDialog(Frame owner, boolean modal, GameData initData, Properties p) {
    super(owner, modal, null, initData, p);
    standAlone = false;
    init();
  }

  /**
   * Create a new OrderWriterDialog object as a dialog with a parent window and a set of selected
   * regions.
   */
  public OrderWriterDialog(Frame owner, boolean modal, GameData initData, Properties p,
      Collection<Region> selectedRegions) {
    super(owner, modal, null, initData, p);
    standAlone = false;
    regions = selectedRegions;
    init();
  }

  private void init() {
    localSettings = new Properties();
    for (Entry<Object, Object> entry : settings.entrySet()) {
      if (((String) entry.getKey()).startsWith(PropertiesHelper.ORDEREDITOR_PREFIX)) {
        localSettings.put(entry.getKey(), entry.getValue());
      }
    }
    List<String> history =
        PropertiesHelper.getList(settings, PropertiesHelper.HISTORY_ACCESSORY_DIRECTORY_HISTORY);
    PropertiesHelper.setList(localSettings, PropertiesHelper.HISTORY_ACCESSORY_DIRECTORY_HISTORY,
        history);

    try {
      String myEncryptionKey = "Magellan2!SuperSecretKey";
      byte[] keyAsBytes = myEncryptionKey.getBytes("UTF8");
      DESedeKeySpec myKeySpec = new DESedeKeySpec(keyAsBytes);
      SecretKeyFactory mySecretKeyFactory = SecretKeyFactory.getInstance(algorithm);
      cipher = Cipher.getInstance(algorithm);
      key = mySecretKeyFactory.generateSecret(myKeySpec);
    } catch (Exception e) {
      log.error("Could not initialize cipher!", e);
      cipher = null;
    }

    Faction faction = null;
    for (Faction f : data.getFactions()) {
      if (f.isPrivileged()) {
        faction = f;
        break;
      }
    }
    // if (faction == null)
    // throw new RuntimeException("no privileged faction in report");

    addKeyListener(new KeyAdapter() {
      @Override
      public void keyPressed(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
          quit(false);
        }
      }
    });
    addWindowListener(new WindowAdapter() {
      @Override
      public void windowClosing(WindowEvent e) {
        quit(false);
      }
    });
    JComponent mainPane = getMainPane();
    ancestor = mainPane.getTopLevelAncestor();

    setContentPane(mainPane);
    setTitle(Resources.get("orderwriterdialog.window.title"));
    pack();
    // setSize(550, 580);

    SwingUtils.setLocation(this, settings, "OrderWriterDialog.x", "OrderWriterDialog.y");
  }

  private JComponent getMainPane() {

    JPanel mainPanel = new JPanel(new GridBagLayout());
    GridBagConstraints c = new GridBagConstraints();

    // insert groups first, so getFactionPanel can call setGroups
    c.anchor = GridBagConstraints.NORTHWEST;
    c.gridx = 0;
    c.gridy = 1;
    c.gridwidth = 1;
    c.gridheight = 1;
    c.fill = GridBagConstraints.HORIZONTAL;
    c.insets = new Insets(14, 4, 4, 4);
    c.weightx = 0.1;
    c.weighty = 0.0;
    mainPanel.add(getGroupPanel(), c);

    c.anchor = GridBagConstraints.NORTHWEST;
    c.gridx = 0;
    c.gridy = 0;
    mainPanel.add(getFactionPanel(), c);

    JPanel buttonPanel = new JPanel(new GridLayout2(0, 1, 0, 6));

    JButton cancelButton = new JButton(Resources.get("orderwriterdialog.btn.cancel.caption"));
    cancelButton.setToolTipText(Resources.get("orderwriterdialog.btn.cancel.tooltip", false));
    cancelButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        quit(false);
      }
    });

    JButton closeButton = new JButton(Resources.get("orderwriterdialog.btn.close.caption"));
    closeButton.setToolTipText(Resources.get("orderwriterdialog.btn.close.tooltip", false));
    closeButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        quit(true);
      }
    });

    buttonPanel.add(cancelButton);
    buttonPanel.add(closeButton);

    c.gridx = 1;
    c.gridy = 0;
    c.gridheight = 2;
    c.weightx = 0.01;
    mainPanel.add(buttonPanel, c);

    c.gridx = 0;
    c.gridy = 2;
    c.gridwidth = 2;
    c.gridheight = 1;

    JTabbedPane tabbedPane = new JTabbedPane(SwingConstants.TOP);

    Component panel = getOptionPanel(FILE_PANEL);
    tabbedPane.addTab(Resources.get("orderwriterdialog.tab.caption." + FILE_PANEL), panel);

    panel = getOptionPanel(EMAIL_PANEL);
    tabbedPane.addTab(Resources.get("orderwriterdialog.tab.caption." + EMAIL_PANEL), panel);

    panel = getOptionPanel(CLIPBOARD_PANEL);
    tabbedPane.addTab(Resources.get("orderwriterdialog.tab.caption." + CLIPBOARD_PANEL), panel);

    mainPanel.add(tabbedPane, c);

    return mainPanel;
  }

  private Container getOptionPanel(int type) {
    JPanel mainPanel = new JPanel(new GridBagLayout());
    GridBagConstraints c = new GridBagConstraints();
    mainPanel.setBorder(new EmptyBorder(4, 4, 4, 4));

    c.gridy = 0;

    if (type == FILE_PANEL) {
      c.anchor = GridBagConstraints.NORTHWEST;
      c.gridx = 0;
      c.gridy++;
      c.gridwidth = 1;
      c.gridheight = 1;
      c.fill = GridBagConstraints.HORIZONTAL;
      c.weightx = 0.1;
      c.weighty = 0.0;
      mainPanel.add(getFilePanel(), c);
    }

    if (type == EMAIL_PANEL) {
      c.anchor = GridBagConstraints.NORTHWEST;
      c.gridx = 0;
      c.gridy++;
      c.gridwidth = 1;
      c.gridheight = 1;
      c.fill = GridBagConstraints.BOTH;
      c.insets = new Insets(0, 0, 0, 0);
      c.weightx = 0.1;
      c.weighty = 0.0;
      mainPanel.add(getMailPanel(), c);
    }
    if (type == CLIPBOARD_PANEL) {
      c.anchor = GridBagConstraints.NORTHWEST;
      c.gridx = 0;
      c.gridy++;
      c.gridwidth = 1;
      c.gridheight = 1;
      c.fill = GridBagConstraints.BOTH;
      c.insets = new Insets(0, 0, 0, 0);
      c.weightx = 0.1;
      c.weighty = 0.0;
      mainPanel.add(new JPanel(), c);
    }

    c.anchor = GridBagConstraints.NORTHWEST;
    c.gridx = 0;
    c.gridy += 2;
    c.gridwidth = 1;
    c.gridheight = 1;
    c.fill = GridBagConstraints.HORIZONTAL;
    c.insets = new Insets(0, 0, 0, 0);
    c.weightx = 0.1;
    c.weighty = 0.0;
    final Container controls;
    mainPanel.add(controls = getControlsPanel(type), c);
    controls.setVisible(false);

    c.gridy--;
    c.weightx = 0;
    c.fill = GridBagConstraints.NONE;
    final JButton detailsButton;
    mainPanel.add(detailsButton = new JButton("Details>>"), c);
    detailsButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent arg0) {
        if (detailsButton.getText().contains(">>")) {
          controls.setVisible(true);
          detailsButton.setText("Details<<");
          pack();
        } else {
          controls.setVisible(false);
          detailsButton.setText("Details>>");
          pack();
        }
      }
    });

    // if (c.gridy < 4) {
    c.gridy += 2;
    c.weighty = 1;
    mainPanel.add(new JPanel(), c);
    // }

    c.anchor = GridBagConstraints.NORTH;
    c.gridwidth = 1;
    c.gridheight = 3;
    c.gridx = 1;
    c.gridy = 0;
    c.fill = GridBagConstraints.NONE;
    c.insets = new Insets(8, 5, 5, 5);
    c.weightx = 0.0;
    c.weighty = 0.0;
    mainPanel.add(getButtonPanel(type), c);

    factionChanged(null, type);

    return mainPanel;
  }

  private Container getButtonPanel(final int type) {
    JPanel buttonPanel = new JPanel(new GridLayout2(0, 1, 0, 6));

    // a hack for writing all faction's orders (useful for testing the eressea server)
    JButton gmButton = new JButton("Save all");
    if (type == FILE_PANEL && PropertiesHelper.getBoolean(localSettings, "GM.enabled", false)) {
      gmButton.setToolTipText(Resources.get("orderwriterdialog.btn.gm.tooltip", false));
      gmButton.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          saveAll();
        }
      });
    }
    if (PropertiesHelper.getBoolean(localSettings, "GM.enabled", false)) {
      buttonPanel.add(gmButton);
    }

    if (type == FILE_PANEL) {
      JButton saveButton = new JButton(Resources.get("orderwriterdialog.btn.save.caption"));
      saveButton.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          saveToFile();
        }
      });

      buttonPanel.add(saveButton);
    }

    if (type == CLIPBOARD_PANEL) {
      JButton clipboardButton =
          new JButton(Resources.get("orderwriterdialog.btn.clipboard.caption"));
      clipboardButton.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          copyToClipboard();
        }
      });
      buttonPanel.add(clipboardButton);
    }

    if (type == EMAIL_PANEL) {
      JButton mailButton = new JButton(Resources.get("orderwriterdialog.btn.mail.caption"));
      mailButton.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent ae) {
          sendMail();
        }
      });
      buttonPanel.add(mailButton);
    }

    // buttonPanel.add(new JSeparator());
    return buttonPanel;
  }

  private Container getControlsPanel(final int type) {
    Faction faction = getFaction(type);
    String suffix = getSuffix(faction, type);

    int fixedWidth =
        PropertiesHelper.getInteger(localSettings, PropertiesHelper.ORDERWRITER_FIXED_WIDTH
            + suffix, DEFAULT_FIXED_WIDTH);
    chkFixedWidth[type] = createCheckBox("wordwrap", null, null, fixedWidth > 0);
    chkFixedWidth[type].addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        txtFixedWidth[type].setEnabled(chkFixedWidth[type].isSelected());
      }
    });

    JLabel lblFixedWidth1 = new JLabel(Resources.get("orderwriterdialog.lbl.wordwrapafter"));
    txtFixedWidth[type] = new JTextField(10);
    txtFixedWidth[type].setText("" + Math.abs(fixedWidth));
    txtFixedWidth[type].setEnabled(chkFixedWidth[type].isSelected());

    JLabel lblFixedWidth2 = new JLabel(Resources.get("orderwriterdialog.lbl.wordwrapchars"));

    JPanel pnlFixedWidth = new JPanel();
    pnlFixedWidth.add(lblFixedWidth1);
    pnlFixedWidth.add(txtFixedWidth[type]);
    pnlFixedWidth.add(lblFixedWidth2);

    chkECheckComments[type] =
        createCheckBox("addecheckcomments", PropertiesHelper.ORDERWRITER_ADD_ECHECK_COMMENTS,
            suffix, true);
    chkRemoveSCComments[type] =
        createCheckBox("removesemicoloncomments", PropertiesHelper.ORDERWRITER_REMOVE_SC_COMMENTS,
            suffix, false);
    chkRemoveSSComments[type] =
        createCheckBox("removedoubleslashcomments",
            PropertiesHelper.ORDERWRITER_REMOVE_SS_COMMENTS, suffix, false);
    chkConfirmedOnly[type] =
        createCheckBox("skipunconfirmedorders", PropertiesHelper.ORDERWRITER_CONFIRMED_ONLY,
            suffix, false);
    chkSelRegionsOnly[type] =
        createCheckBox("selectedregions", PropertiesHelper.ORDERWRITER_SELECTED_REGIONS, suffix,
            false);
    chkSelRegionsOnly[type].setEnabled((regions != null) && (regions.size() > 0));
    chkWriteUnitTagsAsVorlageComment[type] =
        createCheckBox("writeUnitTagsAsVorlageComment",
            PropertiesHelper.ORDERWRITER_WRITE_TAGS_AS_VORLAGE_COMMENT, suffix, false);

    JPanel pnlCmdSave = new JPanel();
    pnlCmdSave.setLayout(new GridBagLayout());
    GridBagConstraints c =
        new GridBagConstraints(0, 0, 1, 1, 1, 0, GridBagConstraints.NORTHEAST,
            GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 0), 1, 1);
    pnlCmdSave.setBorder(new TitledBorder(BorderFactory.createEtchedBorder(), Resources
        .get("orderwriterdialog.border.outputoptions")));
    pnlCmdSave.add(chkFixedWidth[type], c);
    c.gridy++;
    pnlCmdSave.add(pnlFixedWidth, c);
    c.gridy++;
    pnlCmdSave.add(chkECheckComments[type], c);
    c.gridy++;
    pnlCmdSave.add(chkRemoveSCComments[type], c);
    c.gridy++;
    pnlCmdSave.add(chkRemoveSSComments[type], c);
    c.gridy++;
    pnlCmdSave.add(chkConfirmedOnly[type], c);
    c.gridy++;
    pnlCmdSave.add(chkSelRegionsOnly[type], c);
    c.gridy++;
    pnlCmdSave.add(chkWriteUnitTagsAsVorlageComment[type], c);

    return pnlCmdSave;
  }

  private Group getGroup(int type) {
    return (Group) cmbGroups.getSelectedItem();
  }

  private Faction getFaction(int type) {
    return (Faction) cmbFactions.getSelectedItem();
  }

  private Container getFactionPanel() {
    cmbFactions = new JComboBox();

    for (Faction f : data.getFactions()) {
      if (f.isPrivileged()) {
        cmbFactions.addItem(f);
      }
    }

    Faction f =
        data.getFaction(EntityID.createEntityID(localSettings.getProperty(
            PropertiesHelper.ORDERWRITER_FACTION, "-1"), 10, data.base));

    if (f != null) {
      cmbFactions.setSelectedItem(f);
      setGroups(f);
    } else {
      f = (Faction) cmbFactions.getSelectedItem();
      setGroups(f);
    }

    cmbFactions.addItemListener(new ItemListener() {
      public void itemStateChanged(ItemEvent e) {
        // in this situation we need to reinitialize the groups list
        OrderWriterDialog.log.finer("Item event on faction combobox:" + e + " "
            + cmbFactions.getSelectedItem());

        switch (e.getStateChange()) {
        case ItemEvent.SELECTED:
          Faction fSel = (Faction) e.getItem();
          setGroups(fSel);

          for (int type = 0; type < 3; ++type) {
            factionChanged((Faction) e.getItem(), type);
          }
          break;

        case ItemEvent.DESELECTED:
          setGroups(null);
          for (int type = 0; type < 3; ++type) {
            storeSettings(localSettings, (Faction) e.getItem(), type);
          }
          break;
        }

      }
    });

    JPanel pnlCmdSave = new JPanel(new GridLayout(1, 1));
    pnlCmdSave.setBorder(new TitledBorder(BorderFactory.createEtchedBorder(), Resources
        .get("orderwriterdialog.border.faction")));
    pnlCmdSave.add(cmbFactions);

    return pnlCmdSave;
  }

  private void setGroups(Faction f) {
    cmbGroups.removeAllItems();

    if ((f == null) || (f.getGroups() == null) || f.getGroups().isEmpty())
      return;

    cmbGroups.addItem("");

    List<Group> sorted = new ArrayList<Group>(f.getGroups().values());
    Collections.sort(sorted, new NameComparator(null));

    for (Group g : sorted) {
      cmbGroups.addItem(g);
    }

    cmbGroups.setSelectedItem(null);
  }

  private Container getGroupPanel() {
    cmbGroups = new JComboBox();

    JPanel pnlCmdSave = new JPanel(new GridLayout(1, 1));
    pnlCmdSave.setBorder(new TitledBorder(BorderFactory.createEtchedBorder(), Resources
        .get("orderwriterdialog.border.group")));
    pnlCmdSave.add(cmbGroups);

    return pnlCmdSave;
  }

  private Container getFilePanel() {
    Faction faction = getFaction(FILE_PANEL);
    String suffix = getSuffix(faction, FILE_PANEL);

    Object[] list =
        PropertiesHelper.getList(localSettings, PropertiesHelper.ORDERWRITER_OUTPUT_FILE + suffix)
            .toArray();
    cmbOutputFile = new JComboBox(list == null ? new Object[0] : list);
    cmbOutputFile.setEditable(true);

    JButton btnOutputFile = new JButton("...");
    btnOutputFile.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        String outputFile = getFileName((String) cmbOutputFile.getSelectedItem());

        if (outputFile != null) {
          addFileName(outputFile);
        }
      }
    });

    JPanel pnlFile = new JPanel(new BorderLayout());
    pnlFile.setBorder(new TitledBorder(BorderFactory.createEtchedBorder(), Resources
        .get("orderwriterdialog.border.outputfile")));

    pnlFile.add(cmbOutputFile, BorderLayout.CENTER);
    cmbOutputFile.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent arg0) {
        if (arg0.getActionCommand().equals("comboBoxEdited")) {
          log.finer(arg0.getActionCommand());
          updateAutoFileName();
          addFileName((String) cmbOutputFile.getSelectedItem());
        }
      }
    });
    pnlFile.add(btnOutputFile, BorderLayout.EAST);

    JPanel pnl2 = new JPanel(new BorderLayout());
    pnl2.add(chkAutoFileName =
        createCheckBox("autofilename", PropertiesHelper.ORDERWRITER_AUTO_FILENAME, suffix, false),
        BorderLayout.WEST);
    chkAutoFileName.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent arg0) {
        txtOutputFileGenerated.setEnabled(chkAutoFileName.isSelected());
        updateAutoFileName();
      }
    });

    pnl2.add(txtOutputFileGenerated = new JTextField(), BorderLayout.CENTER);
    txtOutputFileGenerated.setEditable(false);
    txtOutputFileGenerated.setEnabled(chkAutoFileName.isSelected());
    updateAutoFileName();

    pnlFile.add(pnl2, BorderLayout.SOUTH);

    // pnlFile.setPreferredSize(new Dimension(150, cmbOutputFile.getPreferredSize().height * 4));
    return pnlFile;
  }

  protected void addFileName(String outputFile) {
    // delete old entry to prevent too many items...
    for (int i = 0; i < cmbOutputFile.getItemCount(); i++) {
      String file = (String) cmbOutputFile.getItemAt(i);
      if (file.equals(outputFile)) {
        cmbOutputFile.removeItemAt(i);
        break;
      }
    }
    cmbOutputFile.insertItemAt(outputFile, 0);
    cmbOutputFile.setSelectedItem(outputFile);
  }

  private Container getMailPanel() {
    Faction faction = getFaction(EMAIL_PANEL);
    String suffix = getSuffix(faction, EMAIL_PANEL);

    JLabel lblMailServer = new JLabel(Resources.get("orderwriterdialog.lbl.smtpserver.name"));
    txtMailServer =
        new JTextField(localSettings.getProperty(PropertiesHelper.ORDERWRITER_MAILSERVER_HOST
            + suffix, "smtp.bar.net"), 20);
    lblMailServer.setLabelFor(txtMailServer);

    JLabel lblMailServerPort = new JLabel(Resources.get("orderwriterdialog.lbl.smtpserver.port"));
    txtMailServerPort =
        new JTextField(""
            + PropertiesHelper.getInteger(localSettings,
                PropertiesHelper.ORDERWRITER_MAILSERVER_PORT + suffix, DEFAULT_MAILSERVER_PORT), 4);
    lblMailServerPort.setLabelFor(txtMailServerPort);

    lblServerUsername = new JLabel(Resources.get("orderwriterdialog.lbl.smtpserver.user"));
    txtServerUsername =
        new JTextField(localSettings.getProperty(PropertiesHelper.ORDERWRITER_MAILSERVER_USERNAME
            + suffix, ""), 20);
    lblServerUsername.setLabelFor(txtServerUsername);

    lblServerPassword = new JLabel(Resources.get("orderwriterdialog.lbl.smtpserver.password"));

    String pw = getPassword(localSettings, suffix);
    txtServerPassword = new JPasswordField(pw, 20);
    lblServerPassword.setLabelFor(txtServerPassword);

    chkAskPassword =
        createCheckBox("askpassword", PropertiesHelper.ORDERWRITER_MAILSERVER_ASKPWD, suffix, true);
    chkAskPassword.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        int answer = 0;
        if (!chkAskPassword.isSelected()) {
          answer =
              JOptionPane.showConfirmDialog(chkAskPassword, Resources
                  .get("orderwriterdialog.msg.passwordwarning"), "", JOptionPane.YES_NO_OPTION);
        }
        if (answer == 0) {
          lblServerPassword.setEnabled(!chkAskPassword.isSelected() || !chkAskPassword.isEnabled());
          txtServerPassword.setEnabled(!chkAskPassword.isSelected() || !chkAskPassword.isEnabled());
        } else {
          chkAskPassword.setSelected(true);
        }
        if (chkAskPassword.isSelected()) {
          txtServerPassword.setText("");
        }
      }
    });

    chkUseAuth =
        createCheckBox("useauth", PropertiesHelper.ORDERWRITER_MAILSERVER_USEAUTH, suffix, false);
    chkUseAuth.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        lblServerUsername.setEnabled(chkUseAuth.isSelected() && chkUseAuth.isEnabled());
        lblServerPassword.setEnabled(chkUseAuth.isSelected() && chkUseAuth.isEnabled()
            && !chkAskPassword.isSelected());
        txtServerUsername.setEnabled(chkUseAuth.isSelected() && chkUseAuth.isEnabled());
        txtServerPassword.setEnabled(chkUseAuth.isSelected() && chkUseAuth.isEnabled()
            && !chkAskPassword.isSelected());
        chkAskPassword.setEnabled(chkUseAuth.isSelected() && chkUseAuth.isEnabled());
      }
    });
    lblServerUsername.setEnabled(chkUseAuth.isSelected());
    lblServerPassword.setEnabled(chkUseAuth.isSelected() && !chkAskPassword.isSelected());
    txtServerUsername.setEnabled(chkUseAuth.isSelected());
    txtServerPassword.setEnabled(chkUseAuth.isSelected() && !chkAskPassword.isSelected());
    chkAskPassword.setEnabled(chkUseAuth.isSelected());

    lblMailRecipient = new JLabel(Resources.get("orderwriterdialog.lbl.recipient"));

    String email =
        localSettings.getProperty(PropertiesHelper.ORDERWRITER_MAILSERVER_RECIPIENT + suffix,
            DEFAULT_EMAIL);

    txtMailRecipient = new JTextField(email, 20);

    lblMailRecipient.setLabelFor(txtMailRecipient);

    JLabel lblMailSender = new JLabel(Resources.get("orderwriterdialog.lbl.sender"));
    txtMailSender =
        new JTextField(localSettings.getProperty(PropertiesHelper.ORDERWRITER_MAILSERVER_SENDER
            + suffix, "myname@example.net"), 20);
    lblMailSender.setLabelFor(txtMailSender);

    lblMailSubject = new JLabel(Resources.get("orderwriterdialog.lbl.subject"));
    txtMailSubject =
        new JTextField(localSettings.getProperty(PropertiesHelper.ORDERWRITER_MAILSERVER_SUBJECT
            + suffix, DEFAULT_SUBJECT), 20);
    lblMailSubject.setLabelFor(txtMailSubject);

    // CC: (Fiete 20090120)
    lblMailRecipient2 = new JLabel("CC:");
    txtMailRecipient2 =
        new JTextField(localSettings.getProperty(PropertiesHelper.ORDERWRITER_MAILSERVER_RECIPIENT2
            + suffix, ""), 20);
    lblMailRecipient2.setLabelFor(txtMailRecipient2);

    JPanel pnlMail = new JPanel(new GridBagLayout());
    GridBagConstraints c = new GridBagConstraints();
    pnlMail.setBorder(new TitledBorder(BorderFactory.createEtchedBorder(), Resources
        .get("orderwriterdialog.border.mailoptions")));

    chkUseSettingsFromCR =
        createCheckBox("usesettingsfromcr",
            PropertiesHelper.ORDERWRITER_MAILSERVER_USE_CR_SETTINGS, suffix, true);
    chkUseSettingsFromCR.setEnabled((data != null) && (data.mailTo != null)
        && (data.mailSubject != null));
    chkUseSettingsFromCR.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        updateRecipient();
      }
    });

    updateRecipient();

    chkCCToSender =
        createCheckBox("cctosender", PropertiesHelper.ORDERWRITER_MAILSERVER_CC2SENDER, suffix,
            true);

    chkUseSSL =
        createCheckBox("usessl", PropertiesHelper.ORDERWRITER_MAILSERVER_SSL, suffix, false);
    chkUseTLS =
        createCheckBox("usetls", PropertiesHelper.ORDERWRITER_MAILSERVER_TLS, suffix, false);

    c.anchor = GridBagConstraints.WEST;
    c.gridx = 0;
    c.gridy = 0;
    c.fill = GridBagConstraints.NONE;
    c.weightx = 0.0;
    c.weighty = 0.0;
    pnlMail.add(lblMailSender, c);

    c.anchor = GridBagConstraints.CENTER;
    c.gridx = 1;
    // c.gridy = 0;
    c.fill = GridBagConstraints.HORIZONTAL;
    c.weightx = 0.2;
    c.weighty = 0.0;
    pnlMail.add(txtMailSender, c);

    c.anchor = GridBagConstraints.CENTER;
    c.gridx = 2;
    // c.gridy = 0;
    c.fill = GridBagConstraints.HORIZONTAL;
    c.weightx = 0.0;
    c.weighty = 0.0;
    pnlMail.add(chkCCToSender, c);

    c.anchor = GridBagConstraints.WEST;
    c.gridx = 0;
    c.gridy++;
    c.fill = GridBagConstraints.NONE;
    c.weightx = 0.0;
    c.weighty = 0.0;
    pnlMail.add(lblMailServer, c);

    c.anchor = GridBagConstraints.CENTER;
    c.gridx = 1;
    // c.gridy = 1;
    c.fill = GridBagConstraints.HORIZONTAL;
    c.weightx = 0.2;
    c.weighty = 0.0;
    pnlMail.add(txtMailServer, c);

    c.anchor = GridBagConstraints.CENTER;
    c.gridx = 2;
    // c.gridy = 0;
    c.fill = GridBagConstraints.HORIZONTAL;
    c.weightx = 0.0;
    c.weighty = 0.0;
    pnlMail.add(chkUseSSL, c);

    c.anchor = GridBagConstraints.WEST;
    c.gridx = 0;
    c.gridy++;
    c.fill = GridBagConstraints.NONE;
    c.weightx = 0.0;
    c.weighty = 0.0;
    pnlMail.add(lblMailServerPort, c);

    c.anchor = GridBagConstraints.CENTER;
    c.gridx = 1;
    // c.gridy = 2;
    c.fill = GridBagConstraints.HORIZONTAL;
    c.weightx = 0.2;
    c.weighty = 0.0;
    pnlMail.add(txtMailServerPort, c);

    c.anchor = GridBagConstraints.CENTER;
    c.gridx = 2;
    // c.gridy = 0;
    c.fill = GridBagConstraints.HORIZONTAL;
    c.weightx = 0.0;
    c.weighty = 0.0;
    pnlMail.add(chkUseTLS, c);

    c.anchor = GridBagConstraints.WEST;
    c.gridx = 0;
    c.gridy++;
    c.fill = GridBagConstraints.NONE;
    c.weightx = 0.0;
    c.weighty = 0.0;
    pnlMail.add(lblServerUsername, c);

    c.anchor = GridBagConstraints.CENTER;
    c.gridx = 1;
    // c.gridy = 3;
    c.fill = GridBagConstraints.HORIZONTAL;
    c.weightx = 0.2;
    c.weighty = 0.0;
    pnlMail.add(txtServerUsername, c);

    c.anchor = GridBagConstraints.CENTER;
    c.gridx = 2;
    // c.gridy = 3;
    c.fill = GridBagConstraints.HORIZONTAL;
    c.weightx = 0.0;
    c.weighty = 0.0;
    pnlMail.add(chkUseAuth, c);

    c.anchor = GridBagConstraints.WEST;
    c.gridx = 0;
    c.gridy++;
    c.fill = GridBagConstraints.NONE;
    c.weightx = 0.0;
    c.weighty = 0.0;
    pnlMail.add(lblServerPassword, c);

    c.anchor = GridBagConstraints.CENTER;
    c.gridx = 2;
    // c.gridy = 3;
    c.fill = GridBagConstraints.HORIZONTAL;
    c.weightx = 0.0;
    c.weighty = 0.0;
    pnlMail.add(chkAskPassword, c);

    c.anchor = GridBagConstraints.CENTER;
    c.gridx = 1;
    // c.gridy = 3;
    c.fill = GridBagConstraints.HORIZONTAL;
    c.weightx = 0.2;
    c.weighty = 0.0;
    pnlMail.add(txtServerPassword, c);

    c.anchor = GridBagConstraints.WEST;
    c.gridx = 0;
    c.gridy++;
    c.fill = GridBagConstraints.NONE;
    c.weightx = 0.0;
    c.weighty = 0.0;
    pnlMail.add(lblMailRecipient, c);

    c.anchor = GridBagConstraints.CENTER;
    c.gridx = 1;
    // c.gridy = 3;
    c.fill = GridBagConstraints.HORIZONTAL;
    c.weightx = 0.2;
    c.weighty = 0.0;
    pnlMail.add(txtMailRecipient, c);

    c.anchor = GridBagConstraints.CENTER;
    c.gridx = 2;
    // c.gridy = 3;
    c.fill = GridBagConstraints.HORIZONTAL;
    c.weightx = 0.0;
    c.weighty = 0.0;
    pnlMail.add(chkUseSettingsFromCR, c);

    c.anchor = GridBagConstraints.WEST;
    c.gridx = 0;
    c.gridy++;
    c.fill = GridBagConstraints.NONE;
    c.weightx = 0.0;
    c.weighty = 0.0;
    pnlMail.add(lblMailSubject, c);

    c.anchor = GridBagConstraints.CENTER;
    c.gridx = 1;
    // c.gridy = 3;
    c.fill = GridBagConstraints.HORIZONTAL;
    c.weightx = 0.2;
    c.weighty = 0.0;
    pnlMail.add(txtMailSubject, c);

    c.anchor = GridBagConstraints.WEST;
    c.gridx = 0;
    c.gridy++;
    c.fill = GridBagConstraints.NONE;
    c.weightx = 0.0;
    c.weighty = 0.0;
    pnlMail.add(lblMailRecipient2, c);

    c.anchor = GridBagConstraints.CENTER;
    c.gridx = 1;
    // c.gridy = 3;
    c.fill = GridBagConstraints.HORIZONTAL;
    c.weightx = 0.2;
    c.weighty = 0.0;
    pnlMail.add(txtMailRecipient2, c);

    return pnlMail;
  }

  private JCheckBox createCheckBox(String name, String resourceKey, String suffix, boolean selected) {
    JCheckBox chkBox =
        new JCheckBox(Resources.get("orderwriterdialog.chk." + name + ".caption"),
            resourceKey == null ? selected : PropertiesHelper.getBoolean(localSettings,
                suffix == null ? resourceKey : (resourceKey + suffix), selected));
    chkBox.setToolTipText(Resources.get("orderwriterdialog.chk." + name + ".tooltip", false));
    return chkBox;
  }

  /**
   * If the faction changed, the configuration for the mailserver and the selected output file may
   * changed...this method checks this.
   * 
   * @param faction
   */
  protected void factionChanged(Faction faction, int type) {
    if (faction == null) {
      faction = getFaction(type);
    }
    String suffix = getSuffix(faction, type);

    if (type == EMAIL_PANEL) {
      // if (localSettings.getProperty(PropertiesHelper.ORDERWRITER_MAILSERVER_USE_CR_SETTINGS +
      // suffix,
      // null) != null) {
      chkUseSettingsFromCR.setSelected(PropertiesHelper.getBoolean(localSettings,
          PropertiesHelper.ORDERWRITER_MAILSERVER_USE_CR_SETTINGS + suffix, true));
      updateRecipient();
      // }

      chkCCToSender.setSelected(PropertiesHelper.getBoolean(localSettings,
          PropertiesHelper.ORDERWRITER_MAILSERVER_CC2SENDER + suffix, true));

      chkUseSSL.setSelected(PropertiesHelper.getBoolean(localSettings,
          PropertiesHelper.ORDERWRITER_MAILSERVER_SSL + suffix, false));

      chkUseTLS.setSelected(PropertiesHelper.getBoolean(localSettings,
          PropertiesHelper.ORDERWRITER_MAILSERVER_TLS + suffix, false));

      txtMailServer.setText(localSettings.getProperty(PropertiesHelper.ORDERWRITER_MAILSERVER_HOST
          + suffix, ""));

      txtMailServerPort.setText(""
          + PropertiesHelper.getInteger(localSettings, PropertiesHelper.ORDERWRITER_MAILSERVER_PORT
              + suffix, DEFAULT_MAILSERVER_PORT));

      chkUseAuth.setSelected(PropertiesHelper.getBoolean(localSettings,
          PropertiesHelper.ORDERWRITER_MAILSERVER_USEAUTH + suffix, false));
      lblServerUsername.setEnabled(chkUseAuth.isSelected() && chkUseAuth.isEnabled());
      lblServerPassword.setEnabled(chkUseAuth.isSelected() && chkUseAuth.isEnabled()
          && !chkAskPassword.isSelected());
      txtServerUsername.setEnabled(chkUseAuth.isSelected() && chkUseAuth.isEnabled());
      txtServerPassword.setEnabled(chkUseAuth.isSelected() && chkUseAuth.isEnabled()
          && !chkAskPassword.isSelected());
      chkAskPassword.setEnabled(chkUseAuth.isSelected() && chkUseAuth.isEnabled());

      txtServerUsername.setText(localSettings
          .getProperty(PropertiesHelper.ORDERWRITER_MAILSERVER_USERNAME + suffix));

      chkAskPassword.setSelected(PropertiesHelper.getBoolean(localSettings,
          PropertiesHelper.ORDERWRITER_MAILSERVER_ASKPWD + suffix, false));
      lblServerPassword.setEnabled(!chkAskPassword.isSelected() || !chkAskPassword.isEnabled());
      txtServerPassword.setEnabled(!chkAskPassword.isSelected() || !chkAskPassword.isEnabled());

      txtServerPassword.setText(getPassword(localSettings, suffix));

      txtMailRecipient.setText(localSettings
          .getProperty(PropertiesHelper.ORDERWRITER_MAILSERVER_RECIPIENT + suffix));

      txtMailSender.setText(localSettings
          .getProperty(PropertiesHelper.ORDERWRITER_MAILSERVER_SENDER + suffix));

      txtMailSubject.setText(localSettings
          .getProperty(PropertiesHelper.ORDERWRITER_MAILSERVER_SUBJECT + suffix));

      txtMailRecipient2.setText(localSettings
          .getProperty(PropertiesHelper.ORDERWRITER_MAILSERVER_RECIPIENT2 + suffix));
    }

    chkSelRegionsOnly[type].setSelected(PropertiesHelper.getBoolean(localSettings,
        PropertiesHelper.ORDERWRITER_SELECTED_REGIONS + suffix, false));

    int fixedWidth =
        PropertiesHelper.getInteger(localSettings, PropertiesHelper.ORDERWRITER_FIXED_WIDTH
            + suffix, DEFAULT_FIXED_WIDTH);
    chkFixedWidth[type].setSelected(fixedWidth > 0);
    txtFixedWidth[type].setText("" + Math.abs(fixedWidth));
    txtFixedWidth[type].setEnabled(chkFixedWidth[type].isSelected());

    chkWriteUnitTagsAsVorlageComment[type].setSelected(PropertiesHelper.getBoolean(localSettings,
        PropertiesHelper.ORDERWRITER_WRITE_TAGS_AS_VORLAGE_COMMENT + suffix, false));

    chkECheckComments[type].setSelected(PropertiesHelper.getBoolean(localSettings,
        PropertiesHelper.ORDERWRITER_ADD_ECHECK_COMMENTS + suffix, false));

    chkRemoveSCComments[type].setSelected(PropertiesHelper.getBoolean(localSettings,
        PropertiesHelper.ORDERWRITER_REMOVE_SC_COMMENTS + suffix, false));

    chkRemoveSSComments[type].setSelected(PropertiesHelper.getBoolean(localSettings,
        PropertiesHelper.ORDERWRITER_REMOVE_SS_COMMENTS + suffix, false));

    chkConfirmedOnly[type].setSelected(PropertiesHelper.getBoolean(localSettings,
        PropertiesHelper.ORDERWRITER_CONFIRMED_ONLY + suffix, false));

    if (type == FILE_PANEL) {
      List<String> files =
          PropertiesHelper
              .getList(localSettings, PropertiesHelper.ORDERWRITER_OUTPUT_FILE + suffix);
      while (cmbOutputFile.getItemCount() > 0) {
        cmbOutputFile.removeItemAt(0);
      }
      if (files != null) {
        for (String file : files) {
          cmbOutputFile.addItem(file);
        }
      }

      if (localSettings.getProperty(PropertiesHelper.ORDERWRITER_AUTO_FILENAME + suffix, null) != null) {
        chkAutoFileName.setSelected(PropertiesHelper.getBoolean(localSettings,
            PropertiesHelper.ORDERWRITER_AUTO_FILENAME + suffix, false));

      } else {
        chkAutoFileName.setSelected(PropertiesHelper.getBoolean(localSettings,
            PropertiesHelper.ORDERWRITER_AUTO_FILENAME, false));
      }
      updateAutoFileName();
    }

    setGroups(faction);
  }

  private String getSuffix(Faction faction, int type) {
    String suffix = "." + (faction == null ? "-" : faction.getID()) + "." + type;
    return suffix;
  }

  protected void updateRecipient() {
    Faction faction = getFaction(EMAIL_PANEL);
    String suffix = getSuffix(faction, EMAIL_PANEL);

    if (!chkUseSettingsFromCR.isEnabled() || !chkUseSettingsFromCR.isSelected()) {
      txtMailRecipient.setText(localSettings.getProperty(
          PropertiesHelper.ORDERWRITER_MAILSERVER_RECIPIENT + suffix, DEFAULT_EMAIL));
      txtMailSubject.setText(localSettings.getProperty(
          PropertiesHelper.ORDERWRITER_MAILSERVER_SUBJECT + suffix, DEFAULT_SUBJECT));
    } else {
      txtMailRecipient.setText(data.mailTo);
      txtMailSubject.setText(data.mailSubject);
    }
    txtMailRecipient.setEnabled(!chkUseSettingsFromCR.isEnabled()
        || !chkUseSettingsFromCR.isSelected());
    txtMailSubject.setEnabled(!chkUseSettingsFromCR.isEnabled()
        || !chkUseSettingsFromCR.isSelected());
    lblMailRecipient.setEnabled(!chkUseSettingsFromCR.isEnabled()
        || !chkUseSettingsFromCR.isSelected());
    lblMailSubject.setEnabled(!chkUseSettingsFromCR.isEnabled()
        || !chkUseSettingsFromCR.isSelected());
  }

  private void storeSettings() {
    for (int i = 0; i < Math.min(cmbFactions.getItemCount(), 6); i++) {
      Faction f = (Faction) cmbFactions.getItemAt(i);
      storeSettings(localSettings, f, EMAIL_PANEL);
      storeSettings(localSettings, f, FILE_PANEL);
      storeSettings(localSettings, f, CLIPBOARD_PANEL);
    }
    settings.putAll(localSettings);
    if (standAlone) {
      try {
        settings.store(new FileOutputStream(new File(System.getProperty("user.home"),
            "OrderWriterDialog.ini")), "");
      } catch (IOException e) {
        OrderWriterDialog.log.error("OrderWriterDialog.storeSettings()", e);
      }
    }
  }

  private void storeSettings(Properties pSettings, Faction faction, int type) {
    // Faction faction = (Faction) cmbFaction[type].getSelectedItem();
    String suffix = getSuffix(faction, type);

    pSettings.setProperty("OrderWriterDialog.x", getX() + "");
    pSettings.setProperty("OrderWriterDialog.y", getY() + "");

    if (getFaction(type) != null) {
      pSettings.setProperty(PropertiesHelper.ORDERWRITER_FACTION, getFaction(type).getID()
          .intValue()
          + "");
    } else {
      pSettings.setProperty(PropertiesHelper.ORDERWRITER_FACTION, "-1");
    }

    if (type == FILE_PANEL) {
      // addFileName((String) cmbOutputFile.getSelectedItem());
      PropertiesHelper.setList(pSettings, PropertiesHelper.ORDERWRITER_OUTPUT_FILE + suffix,
          getNewOutputFiles(cmbOutputFile));
      pSettings.setProperty(PropertiesHelper.ORDERWRITER_AUTO_FILENAME + suffix, String
          .valueOf(chkAutoFileName.isSelected()));
      log.finest(getNewOutputFiles(cmbOutputFile).get(0) + " " + chkAutoFileName.isSelected());
    }

    if (chkFixedWidth[type].isSelected() == true) {
      try {
        pSettings.setProperty(PropertiesHelper.ORDERWRITER_FIXED_WIDTH + suffix, Integer
            .parseInt(txtFixedWidth[type].getText())
            + "");
      } catch (NumberFormatException e) {
        pSettings.setProperty(PropertiesHelper.ORDERWRITER_FIXED_WIDTH + suffix, "0");
      }
    } else {
      try {
        pSettings.setProperty(PropertiesHelper.ORDERWRITER_FIXED_WIDTH + suffix, (-1 * Integer
            .parseInt(txtFixedWidth[type].getText()))
            + "");
      } catch (NumberFormatException e) {
        pSettings.setProperty(PropertiesHelper.ORDERWRITER_FIXED_WIDTH + suffix, "0");
      }
    }

    pSettings.setProperty(PropertiesHelper.ORDERWRITER_ADD_ECHECK_COMMENTS + suffix, String
        .valueOf(chkECheckComments[type].isSelected()));
    pSettings.setProperty(PropertiesHelper.ORDERWRITER_REMOVE_SC_COMMENTS + suffix, String
        .valueOf(chkRemoveSCComments[type].isSelected()));
    pSettings.setProperty(PropertiesHelper.ORDERWRITER_REMOVE_SS_COMMENTS + suffix, String
        .valueOf(chkRemoveSSComments[type].isSelected()));
    pSettings.setProperty(PropertiesHelper.ORDERWRITER_CONFIRMED_ONLY + suffix, String
        .valueOf(chkConfirmedOnly[type].isSelected()));

    if (chkSelRegionsOnly[type].isEnabled()) {
      pSettings.setProperty(PropertiesHelper.ORDERWRITER_SELECTED_REGIONS + suffix, String
          .valueOf(chkSelRegionsOnly[type].isSelected()));
    }

    pSettings.setProperty(PropertiesHelper.ORDERWRITER_WRITE_TAGS_AS_VORLAGE_COMMENT + suffix,
        String.valueOf(chkWriteUnitTagsAsVorlageComment[type].isSelected()));

    if (type == EMAIL_PANEL) {
      if (chkUseSettingsFromCR.isEnabled()) {
        pSettings.setProperty(PropertiesHelper.ORDERWRITER_MAILSERVER_USE_CR_SETTINGS + suffix,
            String.valueOf(chkUseSettingsFromCR.isSelected()));
      }

      pSettings.setProperty(PropertiesHelper.ORDERWRITER_MAILSERVER_CC2SENDER + suffix, String
          .valueOf(chkCCToSender.isSelected()));

      pSettings.setProperty(PropertiesHelper.ORDERWRITER_MAILSERVER_SSL + suffix, String
          .valueOf(chkUseSSL.isSelected()));
      pSettings.setProperty(PropertiesHelper.ORDERWRITER_MAILSERVER_TLS + suffix, String
          .valueOf(chkUseTLS.isSelected()));

      pSettings.setProperty(PropertiesHelper.ORDERWRITER_MAILSERVER_HOST + suffix, txtMailServer
          .getText());
      pSettings.setProperty(PropertiesHelper.ORDERWRITER_MAILSERVER_PORT + suffix,
          txtMailServerPort.getText());

      pSettings.setProperty(PropertiesHelper.ORDERWRITER_MAILSERVER_USEAUTH + suffix, String
          .valueOf(chkUseAuth.isSelected()));
      pSettings.setProperty(PropertiesHelper.ORDERWRITER_MAILSERVER_USERNAME + suffix,
          txtServerUsername.getText());
      pSettings.setProperty(PropertiesHelper.ORDERWRITER_MAILSERVER_ASKPWD + suffix, String
          .valueOf(chkAskPassword.isSelected()));
      // for security reasons only store password if the user explicitly wants it
      pSettings.setProperty(PropertiesHelper.ORDERWRITER_MAILSERVER_PASSWORD + suffix, "");
      if (!chkAskPassword.isSelected() && chkUseAuth.isSelected()) {
        String pw = new String(txtServerPassword.getPassword());
        try {
          String encrypted = encrypt(pw);
          pSettings.setProperty(
              PropertiesHelper.ORDERWRITER_MAILSERVER_PASSWORD_ENCRYPTED + suffix, encrypted);
        } catch (Exception e) {
          log.error("Could not encrypt password", e);
          pSettings.setProperty(PropertiesHelper.ORDERWRITER_MAILSERVER_PASSWORD + suffix, pw);
        }
      } else {
        pSettings.setProperty(PropertiesHelper.ORDERWRITER_MAILSERVER_PASSWORD_ENCRYPTED + suffix,
            "");
      }

      pSettings.setProperty(PropertiesHelper.ORDERWRITER_MAILSERVER_RECIPIENT + suffix,
          txtMailRecipient.getText());
      pSettings.setProperty(PropertiesHelper.ORDERWRITER_MAILSERVER_SENDER + suffix, txtMailSender
          .getText());
      pSettings.setProperty(PropertiesHelper.ORDERWRITER_MAILSERVER_SUBJECT + suffix,
          txtMailSubject.getText());
      pSettings.setProperty(PropertiesHelper.ORDERWRITER_MAILSERVER_RECIPIENT2 + suffix,
          txtMailRecipient2.getText());
    }
  }

  private String getPassword(Properties pSettings, String suffix) {
    String pw = null;
    if (pSettings.getProperty(PropertiesHelper.ORDERWRITER_MAILSERVER_PASSWORD_ENCRYPTED + suffix) != null) {
      try {
        pw =
            decrypt(pSettings
                .getProperty(PropertiesHelper.ORDERWRITER_MAILSERVER_PASSWORD_ENCRYPTED + suffix));
      } catch (Exception e) {
        log.error("Error retrieving password.", e);
      }
    }
    if (pw == null) {
      pw = pSettings.getProperty(PropertiesHelper.ORDERWRITER_MAILSERVER_PASSWORD + suffix, "");
    }
    return pw;
  }

  /**
   * Returns encrypted and base64-encoded password using super secret key.
   */
  private String encrypt(String plaintext) throws InvalidKeyException, BadPaddingException,
      IllegalBlockSizeException {
    cipher.init(Cipher.ENCRYPT_MODE, key);
    byte[] inputBytes = new byte[0];
    try {
      inputBytes = plaintext.getBytes("UTF8");
    } catch (UnsupportedEncodingException e1) {
      // how could this possibly happen?!
      e1.printStackTrace();
    }
    byte[] raw = cipher.doFinal(inputBytes);
    String encoded = new String(Base64.encodeBase64(raw));

    return encoded;
  }

  /** Returns decrypted text for given encrypted, base64-encoded ciphertext using super secret key. */
  private String decrypt(String ciphertext) throws InvalidKeyException, BadPaddingException,
      IllegalBlockSizeException, InvalidAlgorithmParameterException, IOException {
    cipher.init(Cipher.DECRYPT_MODE, key);// , new IvParameterSpec(iv));

    byte[] raw = Base64.decodeBase64(ciphertext.getBytes());
    byte[] recoveredBytes = cipher.doFinal(raw);
    String recovered = new String(recoveredBytes);
    return recovered;
  }

  private List<String> getNewOutputFiles(JComboBox combo) {
    List<String> ret = new ArrayList<String>(combo.getItemCount() + 1);

    if (combo.getSelectedIndex() == -1) {
      ret.add((String) combo.getEditor().getItem());
    }

    String selected = (String) combo.getSelectedItem();
    if (selected != null) {
      ret.add(selected);
    }

    for (int i = 0; i < Math.min(combo.getItemCount(), 6); i++) {
      String item = (String) combo.getItemAt(i);
      if (selected != null && item.equals(selected)) {
        continue; // ignore
      }
      if (ret.contains(item)) {
        continue; // no duplicates
      }
      ret.add(item);
    }

    return ret;
  }

  private void quit(boolean bStoreSettings) {
    if (bStoreSettings) {
      storeSettings();
    }

    if (standAlone == true) {
      System.exit(0);
    } else {
      quit();
    }
  }

  private String getFileName(String defaultFile) {
    String retVal = null;

    JFileChooser fc = new JFileChooser();
    fc.setAccessory(new HistoryAccessory(localSettings, fc));
    fc.addChoosableFileFilter(new EresseaFileFilter(EresseaFileFilter.TXT_FILTER));

    if (defaultFile != null) {
      fc.setSelectedFile(new File(defaultFile));
    }

    if (fc.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
      retVal = fc.getSelectedFile().getPath();
    }

    return retVal;
  }

  private Object[] write(Writer out, boolean forceUnixLineBreaks, Faction faction, int type) {
    return write(out, forceUnixLineBreaks, true, false, faction, type);
  }

  private Object[] write(Writer out, boolean forceUnixLineBreaks, boolean closeStream,
      boolean confirm, Faction faction, int type) {
    if (faction == null)
      return null;
    Object[] result = null;

    setCursor(new Cursor(Cursor.WAIT_CURSOR));

    try {
      Writer stream = null;

      if (chkFixedWidth[type].isSelected()) {
        int fixedWidth = -1;

        try {
          fixedWidth = Integer.parseInt(txtFixedWidth[type].getText());
        } catch (NumberFormatException e) {
          setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
          JOptionPane.showMessageDialog(this, Resources
              .get("orderwriterdialog.msg.invalidfixedwidth.text"), Resources
              .get("orderwriterdialog.msg.invalidfixedwidth.title"), JOptionPane.WARNING_MESSAGE);
          return null;
        }

        stream = new FixedWidthWriter(new BufferedWriter(out), fixedWidth, forceUnixLineBreaks);
      } else {
        stream = out;
      }

      OrderWriter cw = new OrderWriter(data, faction);
      cw.setAddECheckComments(chkECheckComments[type].isSelected());
      cw.setRemoveComments(chkRemoveSCComments[type].isSelected(), chkRemoveSSComments[type]
          .isSelected());
      cw.setConfirmedOnly(chkConfirmedOnly[type].isSelected());
      cw.setWriteUnitTagsAsVorlageComment(chkWriteUnitTagsAsVorlageComment[type].isSelected());

      if (chkSelRegionsOnly[type].isSelected() && (regions != null) && (regions.size() > 0)) {
        cw.setRegions(regions);
      }

      cw.setForceUnixLineBreaks(forceUnixLineBreaks);

      Object group = getGroup(type);

      if (!"".equals(group)) {
        cw.setGroup((Group) group);
      }

      int writtenUnits = cw.write(stream);

      if (closeStream) {
        stream.close();
      }

      int allUnits = 0;

      for (Unit u : data.getUnits()) {
        if (!(u instanceof TempUnit) && u.getFaction().equals(faction)) {
          allUnits++;
        }
      }

      result = new Object[] { writtenUnits, allUnits, faction };
    } catch (IOException ioe) {
      setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
      JOptionPane.showMessageDialog(this, Resources.get("orderwriterdialog.msg.erroronsave.text")
          + ioe.toString(), Resources.get("orderwriterdialog.msg.erroronsave.title"),
          JOptionPane.WARNING_MESSAGE);

      return null;
    }

    setCursor(new Cursor(Cursor.DEFAULT_CURSOR));

    return result;
  }

  private boolean checkPassword(Faction f) {
    if (f == null || f.getPassword() == null) {
      Object msgArgs[] = { f == null ? "null" : f.toString() };
      JOptionPane.showMessageDialog(getRootPane(), (new java.text.MessageFormat(Resources
          .get("orderwriterdialog.msg.nopassword.text"))).format(msgArgs));
      return false;
    }
    return true;
  }

  /**
   * Copies the orders to the clipboard using the settings from the properties.
   */
  public boolean runClipboard() {
    return copyToClipboard();
  }

  protected boolean copyToClipboard() {
    Faction faction = getFaction(CLIPBOARD_PANEL);
    if (faction != null)
      return copyToClipboard(faction);
    else
      return false;
  }

  protected boolean copyToClipboard(Faction faction) {
    if (!checkPassword(faction))
      return false;

    StringWriter sw = new StringWriter();

    Object[] parameters = write(sw, true, faction, CLIPBOARD_PANEL);
    if (parameters != null) {
      // there seems to be a problem with '\r\n'-style linebreaks
      // in the clipboard (you get two linebreaks instead of one)
      // so Unix-style linebreaks have to be enforced
      getToolkit().getSystemClipboard().setContents(
          new java.awt.datatransfer.StringSelection(sw.toString()), null);
      JOptionPane.showMessageDialog(this, (new java.text.MessageFormat(Resources
          .get("orderwriterdialog.msg.writtenunits.text.clipboard"))).format(parameters), Resources
          .get("orderwriterdialog.msg.writtenunits.title"), JOptionPane.INFORMATION_MESSAGE);
      return true;
    } else
      return false;
  }

  private void updateAutoFileName() {
    String pattern = (String) cmbOutputFile.getSelectedItem();

    // this global setting is deprecated, using filename as pattern if appropriate now
    // settings.getProperty("FileNameGenerator.ordersSaveFileNamePattern");

    if (chkAutoFileName.isSelected()) {
      FileNameGeneratorFeed feed = new FileNameGeneratorFeed(super.getData().getDate().getDate());
      Faction f = getFaction(FILE_PANEL);
      if (f != null) {
        feed.setFaction(f.getName());
        feed.setFactionnr(f.getID().toString());
      }
      Object o = getGroup(FILE_PANEL);
      Group g = null;
      if (o != null && !"".equals(o)) {
        g = (Group) o;
      }
      if (g != null) {
        feed.setGroup(g.getName());
      }

      String newFileName = FileNameGenerator.getFileName(pattern, feed);

      txtOutputFileGenerated.setText(newFileName);
      if (pattern == null || !pattern.contains("{")) {
        JOptionPane.showMessageDialog(ancestor, Resources
            .get("orderwriterdialog.msg.nopattern2.text"));
      }
    } else {
      txtOutputFileGenerated.setText(pattern);
    }
  }

  /**
   * Mails the orders using the settings from the properties.
   */
  public boolean runMail() {
    return sendMail();
  }

  protected boolean sendMail() {
    Faction faction = getFaction(EMAIL_PANEL);
    if (faction == null)
      return false;

    if (!checkPassword(faction))
      return false;

    setWaitCursor(true);

    // get mail parameters
    String mailHost = txtMailServer.getText();
    int port = 25;
    String username = null;
    String password = null;
    if (chkUseAuth.isSelected()) {
      username = txtServerUsername.getText();
      if (!chkAskPassword.isSelected()) {
        password = new String(txtServerPassword.getPassword());
      }
    }
    String recipient = txtMailRecipient.getText();
    String sender = txtMailSender.getText();
    String subject = txtMailSubject.getText();
    String recipient2 = txtMailRecipient2.getText();

    if (chkUseSettingsFromCR.isEnabled() && chkUseSettingsFromCR.isSelected()) {
      subject = data.mailSubject;
      recipient = data.mailTo;
    }

    // check mail parameters
    if (sender.equals("")) {
      setWaitCursor(false);
      JOptionPane.showMessageDialog(ancestor, Resources
          .get("orderwriterdialog.msg.invalidfromaddress.text"), Resources
          .get("orderwriterdialog.msg.mailerror.title"), JOptionPane.WARNING_MESSAGE);

      return false;
    }

    if (recipient.equals("")) {
      setWaitCursor(false);
      JOptionPane.showMessageDialog(ancestor, Resources
          .get("orderwriterdialog.msg.invalidrecipient.text"), Resources
          .get("orderwriterdialog.msg.mailerror.title"), JOptionPane.WARNING_MESSAGE);

      return false;
    }

    if (mailHost.equals("")) {
      setWaitCursor(false);
      JOptionPane.showMessageDialog(ancestor, Resources
          .get("orderwriterdialog.msg.invalidsmtpserver.text"), Resources
          .get("orderwriterdialog.msg.mailerror.title"), JOptionPane.WARNING_MESSAGE);

      return false;
    }

    try {
      port = Integer.parseInt(txtMailServerPort.getText());
    } catch (NumberFormatException e) {
      port = -1;
    }
    if (port <= 0) {
      setWaitCursor(false);
      JOptionPane.showMessageDialog(ancestor, Resources
          .get("orderwriterdialog.msg.invalidsmtpserverport.text"), Resources
          .get("orderwriterdialog.msg.mailerror.title"), JOptionPane.WARNING_MESSAGE);

      return false;
    }

    if (username != null) {
      if (password == null) {
        password = showPasswordDialog();
      }
      if (password == null) {
        setWaitCursor(false);
        return false;
      }
    }

    OrderWriterDialog.log.debug("attempting to send mail: " + mailHost + ", " + port + ", "
        + username + ", " + password + ", " + sender + ", " + recipient + ", " + subject);
    sendMailImpl(mailHost, port, username, password, sender, recipient, subject, recipient2,
        faction);

    setWaitCursor(false);

    return true;
  }

  private void setWaitCursor(boolean wait) {
    if (ancestor == null) {
      ancestor = ((JComponent) getContentPane()).getTopLevelAncestor();
    }
    if (wait) {
      ancestor.setCursor(new Cursor(Cursor.WAIT_CURSOR));
    } else {
      ancestor.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
    }
  }

  private void sendMailImpl(final String mailHost, int port, String username, String password,
      String sender, String recipient, String subject, String cc, Faction faction) {

    final MultiPartEmail mailMessage;
    String contentType = "text/plain; charset=" + Encoding.DEFAULT;
    if (PropertiesHelper.getBoolean(localSettings, "TextEncoding.ISOsaveOrders", false)) {
      // new: force our default = ISO
      contentType = "text/plain; charset=" + Encoding.ISO;
    } else if (PropertiesHelper.getBoolean(localSettings, "TextEncoding.UTF8saveOrders", false)) {
      // new: force our default = UTF-8
      contentType = "text/plain; charset=" + Encoding.UTF8;
    } else {
      // old = default = system dependend
      contentType = "text/plain; charset=" + System.getProperty("file.encoding");
    }

    mailMessage = new MultiPartEmail();
    mailMessage.setHostName(mailHost);
    mailMessage.setSubject(subject);

    try {
      mailMessage.setFrom(sender);
      mailMessage.addTo(recipient);

      // specify copy for sender if CC to sender is selected
      if (chkCCToSender.isSelected()) {
        mailMessage.addCc(sender);
      }

      // if users wants extra CC
      if (cc != null && cc.length() > 0) {
        mailMessage.addCc(cc);
      }

    } catch (EmailException e) {
      setWaitCursor(false);

      Object msgArgs[] = { mailHost, e.toString() };
      JOptionPane.showMessageDialog(ancestor, (new java.text.MessageFormat(Resources
          .get("orderwriterdialog.msg.smtpserverunreachable.text"))).format(msgArgs), Resources
          .get("orderwriterdialog.msg.mailerror.title"), JOptionPane.WARNING_MESSAGE);

      if (OrderWriterDialog.log.isDebugEnabled()) {
        OrderWriterDialog.log.debug(e);
      }

      return;
    }

    mailMessage.setSSL(chkUseSSL.isSelected());
    mailMessage.setTLS(chkUseTLS.isSelected());

    StringWriter mailWriter = new StringWriter();
    final Object[] parameters = write(mailWriter, false, false, true, faction, EMAIL_PANEL);

    mailMessage.setContent(mailWriter.toString(), contentType);

    if (username != null && password != null) {
      mailMessage.setAuthentication(username, password);
    }
    mailMessage.setSmtpPort(port);

    // TODO support for POP before SMTP?
    // mailMessage.setPopBeforeSmtp(true, "host", "username", "password");

    OrderWriterDialog.log.info("sending...");
    final UserInterface ui = new ProgressBarUI(this);
    ui.setTitle(Resources.get("orderwriterdialog.btn.mail.caption"));
    ui.setMaximum(0);
    ui.show();
    new Thread(new Runnable() {

      public void run() {
        ui.setProgress(mailHost, 0);
        try {
          mailMessage.send();
          JOptionPane.showMessageDialog(ancestor, (new java.text.MessageFormat(Resources
              .get("orderwriterdialog.msg.writtenunits.text"))).format(parameters), Resources
              .get("orderwriterdialog.msg.writtenunits.title"), JOptionPane.INFORMATION_MESSAGE);

        } catch (EmailException e) {
          OrderWriterDialog.log.info("exception while sending message", e);

          Object msgArgs[] = { e.toString() };
          ui.showDialog(Resources.get("orderwriterdialog.msg.mailerror.title"), Resources.get(
              "orderwriterdialog.msg.transfererror.text", msgArgs), JOptionPane.ERROR_MESSAGE,
              JOptionPane.DEFAULT_OPTION);
        } finally {
          setWaitCursor(false);
          ui.ready();
        }
      }
    }).start();

  }

  /**
   * Shows a dialog asking for a password
   * 
   * @return The user input or <code>null</code> if the user has canceled
   */
  private String showPasswordDialog() {
    String title = Resources.get("orderwriterdialog.lbl.smtpserver.password");
    JPasswordField passwd = new JPasswordField(20) {
      @Override
      public void setVisible(boolean b) {
        super.setVisible(b);
        if (b) {
          requestFocus();
        }
      }
    };
    class MyPanel extends JPanel {
      JPasswordField pwd;

      MyPanel(JPasswordField passwdf) {
        pwd = passwdf;
      }

      @Override
      public void requestFocus() {
        pwd.requestFocus();
      }
    }

    JLabel passwdLabel = new JLabel(title);
    JPanel panel = new MyPanel(passwd);
    panel.add(passwdLabel);
    panel.add(passwd);
    int value =
        JOptionPane.showOptionDialog(ancestor, panel, title, JOptionPane.OK_CANCEL_OPTION,
            JOptionPane.QUESTION_MESSAGE, null, null, null);
    if (value == 0)
      return new String(passwd.getPassword());
    else
      return null;
  }

  /**
   * Saves the orders to a file using the settings from the properties.
   */
  public boolean runSave() {
    return saveToFile();
  }

  protected boolean saveAll() {
    boolean result = false;
    File outputFile = new File(getOutputFileName());
    Writer stream = null;

    try {
      stream = getWriter(outputFile);

      if (stream != null) {
        result = true;
        int[] parameters = new int[4];
        for (Faction f : data.getFactions()) {
          Object[] written = write(stream, false, false, false, f, FILE_PANEL);
          if (written != null) {
            parameters[0] = parameters[0] + (Integer) written[0];
            parameters[1] = parameters[1] + (Integer) written[1];
            ++parameters[2];
          }
          ++parameters[3];
        }
        JOptionPane.showMessageDialog(this, (new java.text.MessageFormat(Resources
            .get("orderwriterdialog.msg.writtenunits.text.file"))).format(new Object[] {
            parameters[0], parameters[1], "" + parameters[2] + "/" + parameters[3], outputFile }),
            Resources.get("orderwriterdialog.msg.writtenunits.title"),
            JOptionPane.INFORMATION_MESSAGE);

        stream.close();
      }
    } catch (IOException ioe) {
      JOptionPane.showMessageDialog(this, Resources.getFormatted(
          "orderwriterdialog.msg.writeerror.text", outputFile.toString(), ioe), Resources
          .get("orderwriterdialog.msg.writeerror.title"), JOptionPane.WARNING_MESSAGE);
    }
    return result;
  }

  private String getOutputFileName() {
    // updateAutoFileName();
    return txtOutputFileGenerated.getText();
  }

  protected boolean saveToFile() {
    Faction faction = getFaction(FILE_PANEL);
    if (faction == null)
      return false;
    if (!checkPassword(faction))
      return false;

    File outputFile = new File(getOutputFileName());
    Writer stream = null;

    try {

      stream = getWriter(outputFile);
      Object[] parameters = write(stream, false, faction, FILE_PANEL);

      if (parameters != null) {
        JOptionPane.showMessageDialog(this, (new java.text.MessageFormat(Resources
            .get("orderwriterdialog.msg.writtenunits.text.file"))).format(new Object[] {
            parameters[0], parameters[1], parameters[2], outputFile }), Resources
            .get("orderwriterdialog.msg.writtenunits.title"), JOptionPane.INFORMATION_MESSAGE);
      }

      return parameters != null;

    } catch (IOException ioe) {
      JOptionPane.showMessageDialog(this, Resources.getFormatted(
          "orderwriterdialog.msg.writeerror.text", outputFile.toString(), ioe), Resources
          .get("orderwriterdialog.msg.writeerror.title"), JOptionPane.WARNING_MESSAGE);
    }

    return false;
  }

  protected Writer getWriter(File outputFile) throws IOException {

    if (outputFile.exists() && outputFile.canWrite()) {
      // create backup file
      try {
        File backup = FileBackup.create(outputFile);
        OrderWriterDialog.log.info("Created backupfile " + backup);
      } catch (IOException ie) {
        OrderWriterDialog.log.warn("Could not create backupfile for file " + outputFile);
      }
    }

    if (outputFile.exists() && !outputFile.canWrite())
      throw new IOException("cannot write " + outputFile);

    // apexo (Fiete) 20061205: if in properties, force ISO encoding
    Writer stream = null;
    if (PropertiesHelper.getBoolean(localSettings, "TextEncoding.ISOsaveOrders", false)) {
      // new: force our default = ISO
      stream = new OutputStreamWriter(new FileOutputStream(outputFile), Encoding.ISO.toString());
    } else if (PropertiesHelper.getBoolean(localSettings, "TextEncoding.UTF8saveOrders", false)) {
      // new: force our default = UTF-8
      stream = new OutputStreamWriter(new FileOutputStream(outputFile), Encoding.UTF8.toString());
    } else {
      // old = default = system dependent
      stream = new FileWriter(outputFile);
    }
    return stream;
  }

}
