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
import java.awt.Dimension;
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
import java.io.Writer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Properties;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JSeparator;
import javax.swing.JTextField;
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

import org.apache.commons.mail.EmailException;
import org.apache.commons.mail.MultiPartEmail;

/**
 * A GUI for writing orders to a file or copy them to the clipboard. This class can be used as a
 * stand-alone application or can be integrated as dialog into another application.
 */
public class OrderWriterDialog extends InternationalizedDataDialog {
  private static final Logger log = Logger.getInstance(OrderWriterDialog.class);

  final String defaultEmail = "eressea-server@eressea.kn-bremen.de";
  final String defaultSubject = "Eressea Befehle";

  private boolean standAlone = false;
  private Collection<Region> regions = null;
  private JComboBox cmbOutputFile = null;
  private JCheckBox chkUseSettingsFromCR = null;
  private JCheckBox chkFixedWidth = null;
  private JTextField txtFixedWidth = null;
  private JCheckBox chkECheckComments = null;
  private JCheckBox chkRemoveSCComments = null;
  private JCheckBox chkRemoveSSComments = null;
  private JCheckBox chkConfirmedOnly = null;
  private JCheckBox chkSelRegionsOnly = null;
  private JCheckBox chkWriteUnitTagsAsVorlageComment = null;
  private JCheckBox chkCCToSender = null;
  private JCheckBox chkUseSSL = null;
  private JCheckBox chkUseTLS = null;
  private JComboBox cmbFaction = null;
  private JComboBox cmbGroup = null;
  private JTextField txtMailServer = null;
  private JTextField txtMailServerPort = null;
  private JTextField txtMailRecipient = null;
  private JTextField txtMailRecipient2 = null;
  private JTextField txtMailSender = null;
  private JTextField txtMailSubject = null;
  private JTextField txtServerUsername = null;
  private JPasswordField txtServerPassword = null;
  private JCheckBox chkUseAuth = null;
  private JCheckBox chkAskPassword = null;

  private JButton sendButton;
  private JLabel lblServerUsername;
  private JLabel lblServerPassword;
  private JLabel lblMailRecipient;
  private JLabel lblMailSubject;
  private JLabel lblMailRecipient2;

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
    setContentPane(getMainPane());
    setTitle(Resources.get("orderwriterdialog.window.title"));
    pack();
    // setSize(550, 580);

    SwingUtils.setLocation(this, settings, "OrderWriterDialog.x", "OrderWriterDialog.y");
  }

  private Container getMainPane() {
    JPanel mainPanel = new JPanel(new GridBagLayout());
    GridBagConstraints c = new GridBagConstraints();
    mainPanel.setBorder(new EmptyBorder(4, 4, 4, 4));

    c.anchor = GridBagConstraints.CENTER;
    c.gridx = 0;
    c.gridy = 0;
    c.gridwidth = 1;
    c.gridheight = 1;
    c.fill = GridBagConstraints.HORIZONTAL;
    c.weightx = 0.1;
    c.weighty = 0.0;
    mainPanel.add(getFilePanel(), c);

    c.anchor = GridBagConstraints.NORTH;
    c.gridx = 1;
    c.gridy = 0;
    c.gridwidth = 1;
    c.gridheight = 4;
    c.fill = GridBagConstraints.NONE;
    c.insets = new Insets(8, 5, 0, 0);
    c.weightx = 0.0;
    c.weighty = 0.0;
    mainPanel.add(getButtonPanel(), c);

    c.anchor = GridBagConstraints.CENTER;
    c.gridx = 0;
    c.gridy = 2;
    c.gridwidth = 1;
    c.gridheight = 1;
    c.fill = GridBagConstraints.HORIZONTAL;
    c.insets = new Insets(0, 0, 0, 0);
    c.weightx = 0.0;
    c.weighty = 0.0;
    mainPanel.add(getGroupPanel(), c);

    c.anchor = GridBagConstraints.CENTER;
    c.gridx = 0;
    c.gridy = 1;
    c.gridwidth = 1;
    c.gridheight = 1;
    c.fill = GridBagConstraints.HORIZONTAL;
    c.insets = new Insets(0, 0, 0, 0);
    c.weightx = 0.0;
    c.weighty = 0.0;
    mainPanel.add(getFactionPanel(), c);

    c.anchor = GridBagConstraints.CENTER;
    c.gridx = 0;
    c.gridy = 3;
    c.gridwidth = 1;
    c.gridheight = 1;
    c.fill = GridBagConstraints.HORIZONTAL;
    c.insets = new Insets(0, 0, 0, 0);
    c.weightx = 0.0;
    c.weighty = 0.0;
    mainPanel.add(getControlsPanel(), c);

    c.anchor = GridBagConstraints.CENTER;
    c.gridx = 0;
    c.gridy = 4;
    c.gridwidth = 1;
    c.gridheight = 1;
    c.fill = GridBagConstraints.BOTH;
    c.insets = new Insets(0, 0, 0, 0);
    c.weightx = 0.0;
    c.weighty = 0.0;
    mainPanel.add(getMailPanel(), c);

    factionChanged();

    return mainPanel;
  }

  private Container getButtonPanel() {
    JButton saveButton = new JButton(Resources.get("orderwriterdialog.btn.save.caption"));
    saveButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        saveToFile();
      }
    });

    JButton clipboardButton = new JButton(Resources.get("orderwriterdialog.btn.clipboard.caption"));
    clipboardButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        copyToClipboard();
      }
    });

    JButton mailButton = new JButton(Resources.get("orderwriterdialog.btn.mail.caption"));
    mailButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent ae) {
        sendMail();
      }
    });
    sendButton = mailButton;

    JButton autoFileNameButton =
        new JButton(Resources.get("orderwriterdialog.btn.autofilename.caption"));
    autoFileNameButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent ae) {
        autoFileName();
      }
    });

    JButton closeButton = new JButton(Resources.get("orderwriterdialog.btn.close.caption"));
    closeButton.setToolTipText(Resources.get("orderwriterdialog.btn.close.tooltip", false));
    closeButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        quit(true);
      }
    });

    JButton cancelButton = new JButton(Resources.get("orderwriterdialog.btn.cancel.caption"));
    cancelButton.setToolTipText(Resources.get("orderwriterdialog.btn.cancel.tooltip", false));
    cancelButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        quit(false);
      }
    });

    JButton gmButton = new JButton("Save all");
    if (PropertiesHelper.getBoolean(settings, "GM.enabled", false)) {
      gmButton.setToolTipText(Resources.get("orderwriterdialog.btn.gm.tooltip", false));
      gmButton.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          saveAll();
        }
      });
    }

    JPanel buttonPanel = new JPanel(new GridLayout2(0, 1, 0, 6));
    buttonPanel.add(saveButton);
    if (PropertiesHelper.getBoolean(settings, "GM.enabled", false)) {
      buttonPanel.add(gmButton);
    }
    buttonPanel.add(clipboardButton);
    buttonPanel.add(mailButton);
    buttonPanel.add(autoFileNameButton);
    buttonPanel.add(new JSeparator());
    buttonPanel.add(cancelButton);
    buttonPanel.add(closeButton);

    return buttonPanel;
  }

  private Container getControlsPanel() {
    int fixedWidth =
        Integer.parseInt(settings.getProperty(PropertiesHelper.ORDERWRITER_FIXED_WIDTH, "76"));
    chkFixedWidth =
        new JCheckBox(Resources.get("orderwriterdialog.chk.wordwrap.caption"), fixedWidth > 0);
    chkFixedWidth.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        txtFixedWidth.setEnabled(chkFixedWidth.isSelected());
      }
    });

    JLabel lblFixedWidth1 = new JLabel(Resources.get("orderwriterdialog.lbl.wordwrapafter"));
    txtFixedWidth = new JTextField(10);
    txtFixedWidth.setText("" + Math.abs(fixedWidth));
    txtFixedWidth.setEnabled(chkFixedWidth.isSelected());

    JLabel lblFixedWidth2 = new JLabel(Resources.get("orderwriterdialog.lbl.wordwrapchars"));

    JPanel pnlFixedWidth = new JPanel();
    pnlFixedWidth.add(lblFixedWidth1);
    pnlFixedWidth.add(txtFixedWidth);
    pnlFixedWidth.add(lblFixedWidth2);

    chkECheckComments =
        new JCheckBox(Resources.get("orderwriterdialog.chk.addecheckcomments.caption"),
            (Boolean.valueOf(settings.getProperty(PropertiesHelper.ORDERWRITER_ADD_ECHECK_COMMENTS,
                "true"))).booleanValue());
    chkRemoveSCComments =
        new JCheckBox(Resources.get("orderwriterdialog.chk.removesemicoloncomments.caption"),
            (Boolean.valueOf(settings.getProperty(PropertiesHelper.ORDERWRITER_REMOVE_SC_COMMENTS,
                "false"))).booleanValue());
    chkRemoveSSComments =
        new JCheckBox(Resources.get("orderwriterdialog.chk.removedoubleslashcomments.caption"),
            (Boolean.valueOf(settings.getProperty(PropertiesHelper.ORDERWRITER_REMOVE_SS_COMMENTS,
                "false"))).booleanValue());
    chkConfirmedOnly =
        new JCheckBox(Resources.get("orderwriterdialog.chk.skipunconfirmedorders.caption"),
            (Boolean.valueOf(settings.getProperty(PropertiesHelper.ORDERWRITER_CONFIRMED_ONLY,
                "false"))).booleanValue());
    chkSelRegionsOnly =
        new JCheckBox(Resources.get("orderwriterdialog.chk.selectedregions.caption"), (Boolean
            .valueOf(settings.getProperty(PropertiesHelper.ORDERWRITER_SELECTED_REGIONS, "false")))
            .booleanValue());
    chkSelRegionsOnly.setEnabled((regions != null) && (regions.size() > 0));
    chkWriteUnitTagsAsVorlageComment =
        new JCheckBox(Resources.get("orderwriterdialog.chk.writeUnitTagsAsVorlageComment.caption"),
            (Boolean.valueOf(settings.getProperty(
                PropertiesHelper.ORDERWRITER_WRITE_TAGS_AS_VORLAGE_COMMENT, "false")))
                .booleanValue());

    JPanel pnlCmdSave = new JPanel();
    pnlCmdSave.setLayout(new BoxLayout(pnlCmdSave, BoxLayout.Y_AXIS));
    pnlCmdSave.setBorder(new TitledBorder(BorderFactory.createEtchedBorder(), Resources
        .get("orderwriterdialog.border.outputoptions")));
    pnlCmdSave.add(chkFixedWidth);
    pnlCmdSave.add(pnlFixedWidth);

    pnlCmdSave.add(chkECheckComments);
    pnlCmdSave.add(chkRemoveSCComments);
    pnlCmdSave.add(chkRemoveSSComments);
    pnlCmdSave.add(chkConfirmedOnly);
    pnlCmdSave.add(chkSelRegionsOnly);
    pnlCmdSave.add(chkWriteUnitTagsAsVorlageComment);

    return pnlCmdSave;
  }

  private Container getFactionPanel() {
    cmbFaction = new JComboBox();

    for (Faction f : data.getFactions()) {
      if (f.isPrivileged()) {
        cmbFaction.addItem(f);
      }
    }

    Faction f =
        data.getFaction(EntityID.createEntityID(settings.getProperty(
            PropertiesHelper.ORDERWRITER_FACTION, "-1"), 10, data.base));

    if (f != null) {
      cmbFaction.setSelectedItem(f);
      setGroups(f);
    } else {
      f = (Faction) cmbFaction.getSelectedItem();
      setGroups(f);
    }

    cmbFaction.addItemListener(new ItemListener() {
      public void itemStateChanged(ItemEvent e) {
        // in this situation we need to reinitialize the groups list
        OrderWriterDialog.log.debug("Item even on faction combobox:" + e);

        switch (e.getStateChange()) {
        case ItemEvent.SELECTED:

          Faction fSel = (Faction) e.getItem();
          setGroups(fSel);

          break;

        case ItemEvent.DESELECTED:
          setGroups(null);

          break;
        }

        factionChanged();
      }
    });

    JPanel pnlCmdSave = new JPanel(new GridLayout(1, 1));
    pnlCmdSave.setBorder(new TitledBorder(BorderFactory.createEtchedBorder(), Resources
        .get("orderwriterdialog.border.faction")));
    pnlCmdSave.add(cmbFaction);

    return pnlCmdSave;
  }

  private void setGroups(Faction f) {
    cmbGroup.removeAllItems();

    if ((f == null) || (f.getGroups() == null) || f.getGroups().isEmpty())
      return;

    cmbGroup.addItem("");

    List<Group> sorted = new ArrayList<Group>(f.getGroups().values());
    Collections.sort(sorted, new NameComparator(null));

    for (Group g : sorted) {
      cmbGroup.addItem(g);
    }

    cmbGroup.setSelectedItem(null);
  }

  private Container getGroupPanel() {
    cmbGroup = new JComboBox();

    JPanel pnlCmdSave = new JPanel(new GridLayout(1, 1));
    pnlCmdSave.setBorder(new TitledBorder(BorderFactory.createEtchedBorder(), Resources
        .get("orderwriterdialog.border.group")));
    pnlCmdSave.add(cmbGroup);

    return pnlCmdSave;
  }

  private Container getFilePanel() {
    cmbOutputFile =
        new JComboBox(PropertiesHelper.getList(settings, PropertiesHelper.ORDERWRITER_OUTPUT_FILE)
            .toArray());
    cmbOutputFile.setEditable(true);

    JButton btnOutputFile = new JButton("...");
    btnOutputFile.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        String outputFile = getFileName((String) cmbOutputFile.getSelectedItem());

        if (outputFile != null) {
          // delete old entry to prevent to many items...
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
      }
    });

    JPanel pnlFile = new JPanel(new BorderLayout());
    pnlFile.setBorder(new TitledBorder(BorderFactory.createEtchedBorder(), Resources
        .get("orderwriterdialog.border.outputfile")));
    pnlFile.add(cmbOutputFile, BorderLayout.CENTER);
    pnlFile.add(btnOutputFile, BorderLayout.EAST);
    pnlFile.setPreferredSize(new Dimension(150, cmbOutputFile.getPreferredSize().height * 2));
    return pnlFile;
  }

  private Container getMailPanel() {
    JLabel lblMailServer = new JLabel(Resources.get("orderwriterdialog.lbl.smtpserver.name"));
    txtMailServer =
        new JTextField(settings.getProperty(PropertiesHelper.ORDERWRITER_MAILSERVER_HOST,
            "smtp.bar.net"), 20);
    lblMailServer.setLabelFor(txtMailServer);

    JLabel lblMailServerPort = new JLabel(Resources.get("orderwriterdialog.lbl.smtpserver.port"));
    txtMailServerPort =
        new JTextField(settings.getProperty(PropertiesHelper.ORDERWRITER_MAILSERVER_PORT, "25"), 4);
    lblMailServerPort.setLabelFor(txtMailServerPort);

    lblServerUsername = new JLabel(Resources.get("orderwriterdialog.lbl.smtpserver.user"));
    txtServerUsername =
        new JTextField(settings.getProperty(PropertiesHelper.ORDERWRITER_MAILSERVER_USERNAME, ""),
            20);
    lblServerUsername.setLabelFor(txtServerUsername);

    lblServerPassword = new JLabel(Resources.get("orderwriterdialog.lbl.smtpserver.password"));
    txtServerPassword =
        new JPasswordField(settings.getProperty(PropertiesHelper.ORDERWRITER_MAILSERVER_PASSWORD,
            ""), 20);
    lblServerPassword.setLabelFor(txtServerPassword);

    chkAskPassword =
        new JCheckBox(Resources.get("orderwriterdialog.chk.askpassword.caption"), (Boolean
            .valueOf(settings.getProperty(PropertiesHelper.ORDERWRITER_MAILSERVER_ASKPWD, "true")))
            .booleanValue());
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
        new JCheckBox(Resources.get("orderwriterdialog.chk.useauth.caption"),
            (Boolean.valueOf(settings.getProperty(PropertiesHelper.ORDERWRITER_MAILSERVER_USEAUTH,
                "false"))).booleanValue());
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
        settings.getProperty(PropertiesHelper.ORDERWRITER_MAILSERVER_RECIPIENT, defaultEmail);

    // stm 2008.03.02: enno wanted this change undone ;)
    // // pavkovic 2002.01.23: enno wanted this change...
    // if(email.toLowerCase().equals("eressea@kn-bremen.de")) {
    // email = defaultEmail;
    // }
    //
    // if(email.toLowerCase().equals("eressea@eressea.kn-bremen.de")) {
    // email = defaultEmail;
    // }
    //
    // if(email.toLowerCase().equals("eressea@eressea.amber.kn-bremen.de")) {
    // email = defaultEmail;
    // }

    txtMailRecipient = new JTextField(email, 20);

    lblMailRecipient.setLabelFor(txtMailRecipient);

    JLabel lblMailSender = new JLabel(Resources.get("orderwriterdialog.lbl.sender"));
    txtMailSender =
        new JTextField(settings.getProperty(PropertiesHelper.ORDERWRITER_MAILSERVER_SENDER,
            "foo@bar.net"), 20);
    lblMailSender.setLabelFor(txtMailSender);

    lblMailSubject = new JLabel(Resources.get("orderwriterdialog.lbl.subject"));
    txtMailSubject =
        new JTextField(settings.getProperty(PropertiesHelper.ORDERWRITER_MAILSERVER_SUBJECT,
            defaultSubject), 20);
    lblMailSubject.setLabelFor(txtMailSubject);

    // CC: (Fiete 20090120)
    lblMailRecipient2 = new JLabel("CC:");
    txtMailRecipient2 =
        new JTextField(
            settings.getProperty(PropertiesHelper.ORDERWRITER_MAILSERVER_RECIPIENT2, ""), 20);
    lblMailRecipient2.setLabelFor(txtMailRecipient2);

    JPanel pnlMail = new JPanel(new GridBagLayout());
    GridBagConstraints c = new GridBagConstraints();
    pnlMail.setBorder(new TitledBorder(BorderFactory.createEtchedBorder(), Resources
        .get("orderwriterdialog.border.mailoptions")));

    chkUseSettingsFromCR =
        new JCheckBox(Resources.get("orderwriterdialog.chk.usesettingsfromcr.caption"), (Boolean
            .valueOf(settings.getProperty(PropertiesHelper.ORDERWRITER_MAILSERVER_USE_CR_SETTINGS,
                "true"))).booleanValue());
    chkUseSettingsFromCR.setEnabled((data != null) && (data.mailTo != null)
        && (data.mailSubject != null));
    chkUseSettingsFromCR.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        updateRecipient();
      }
    });

    updateRecipient();

    chkCCToSender =
        new JCheckBox(Resources.get("orderwriterdialog.chk.cctosender.caption"), (Boolean
            .valueOf(settings
                .getProperty(PropertiesHelper.ORDERWRITER_MAILSERVER_CC2SENDER, "true")))
            .booleanValue());
    chkUseSSL =
        new JCheckBox(Resources.get("orderwriterdialog.chk.usessl.caption"), (Boolean
            .valueOf(settings.getProperty(PropertiesHelper.ORDERWRITER_MAILSERVER_SSL, "false")))
            .booleanValue());
    chkUseTLS =
        new JCheckBox(Resources.get("orderwriterdialog.chk.usetls.caption"), (Boolean
            .valueOf(settings.getProperty(PropertiesHelper.ORDERWRITER_MAILSERVER_TLS, "false")))
            .booleanValue());

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

  /**
   * If the faction changed, the configuration for the mailserver and the selected output file may
   * changed...this method checks this.
   */
  protected void factionChanged() {
    Faction faction = (Faction) cmbFaction.getSelectedItem();
    String suffix = faction != null ? "." + faction.getID() : "";

    if (settings
        .getProperty(PropertiesHelper.ORDERWRITER_MAILSERVER_USE_CR_SETTINGS + suffix, null) != null) {
      chkUseSettingsFromCR.setSelected(PropertiesHelper.getBoolean(settings,
          PropertiesHelper.ORDERWRITER_MAILSERVER_USE_CR_SETTINGS + suffix, true));

      updateRecipient();
    }

    if (settings.getProperty(PropertiesHelper.ORDERWRITER_MAILSERVER_CC2SENDER + suffix, null) != null) {
      chkCCToSender.setSelected(PropertiesHelper.getBoolean(settings,
          PropertiesHelper.ORDERWRITER_MAILSERVER_CC2SENDER + suffix, true));
    }

    // PropertiesHelper.ORDERWRITER_MAILSERVER_SSL+suffix
    // PropertiesHelper.ORDERWRITER_MAILSERVER_TLS+suffix
    if (settings.getProperty(PropertiesHelper.ORDERWRITER_MAILSERVER_SSL + suffix, null) != null) {
      chkUseSSL.setSelected(PropertiesHelper.getBoolean(settings,
          PropertiesHelper.ORDERWRITER_MAILSERVER_SSL + suffix, false));
    }

    if (settings.getProperty(PropertiesHelper.ORDERWRITER_MAILSERVER_TLS + suffix, null) != null) {
      chkUseTLS.setSelected(PropertiesHelper.getBoolean(settings,
          PropertiesHelper.ORDERWRITER_MAILSERVER_TLS + suffix, false));
    }

    if (settings.getProperty(PropertiesHelper.ORDERWRITER_SELECTED_REGIONS + suffix, null) != null) {
      chkSelRegionsOnly.setSelected(PropertiesHelper.getBoolean(settings,
          PropertiesHelper.ORDERWRITER_SELECTED_REGIONS + suffix, false));
    }

    if (settings.getProperty(PropertiesHelper.ORDERWRITER_FIXED_WIDTH + suffix, null) != null) {
      int fixedWidth =
          Integer.parseInt(settings.getProperty(PropertiesHelper.ORDERWRITER_FIXED_WIDTH + suffix,
              "76"));
      chkFixedWidth.setSelected(fixedWidth > 0);
      txtFixedWidth.setText("" + Math.abs(fixedWidth));
      txtFixedWidth.setEnabled(chkFixedWidth.isSelected());
    }

    if (settings.getProperty(PropertiesHelper.ORDERWRITER_WRITE_TAGS_AS_VORLAGE_COMMENT + suffix,
        null) != null) {
      chkWriteUnitTagsAsVorlageComment.setSelected(PropertiesHelper.getBoolean(settings,
          PropertiesHelper.ORDERWRITER_WRITE_TAGS_AS_VORLAGE_COMMENT + suffix, false));
    }

    if (settings.getProperty(PropertiesHelper.ORDERWRITER_MAILSERVER_HOST + suffix, null) != null) {
      txtMailServer.setText(settings.getProperty(PropertiesHelper.ORDERWRITER_MAILSERVER_HOST
          + suffix));
    }

    if (settings.getProperty(PropertiesHelper.ORDERWRITER_MAILSERVER_PORT + suffix, null) != null) {
      txtMailServerPort.setText(settings.getProperty(PropertiesHelper.ORDERWRITER_MAILSERVER_PORT
          + suffix));
    }

    if (settings.getProperty(PropertiesHelper.ORDERWRITER_MAILSERVER_USEAUTH + suffix, null) != null) {
      chkUseAuth.setSelected(PropertiesHelper.getBoolean(settings,
          PropertiesHelper.ORDERWRITER_MAILSERVER_USEAUTH + suffix, false));
      lblServerUsername.setEnabled(chkUseAuth.isSelected() && chkUseAuth.isEnabled());
      lblServerPassword.setEnabled(chkUseAuth.isSelected() && chkUseAuth.isEnabled()
          && !chkAskPassword.isSelected());
      txtServerUsername.setEnabled(chkUseAuth.isSelected() && chkUseAuth.isEnabled());
      txtServerPassword.setEnabled(chkUseAuth.isSelected() && chkUseAuth.isEnabled()
          && !chkAskPassword.isSelected());
      chkAskPassword.setEnabled(chkUseAuth.isSelected() && chkUseAuth.isEnabled());
    }

    if (settings.getProperty(PropertiesHelper.ORDERWRITER_MAILSERVER_USERNAME + suffix, null) != null) {
      txtServerUsername.setText(settings
          .getProperty(PropertiesHelper.ORDERWRITER_MAILSERVER_USERNAME + suffix));
    }

    if (settings.getProperty(PropertiesHelper.ORDERWRITER_MAILSERVER_ASKPWD + suffix, null) != null) {
      chkAskPassword.setSelected(PropertiesHelper.getBoolean(settings,
          PropertiesHelper.ORDERWRITER_MAILSERVER_ASKPWD + suffix, false));
      lblServerPassword.setEnabled(!chkAskPassword.isSelected() || !chkAskPassword.isEnabled());
      txtServerPassword.setEnabled(!chkAskPassword.isSelected() || !chkAskPassword.isEnabled());
    }

    if (settings.getProperty(PropertiesHelper.ORDERWRITER_MAILSERVER_PASSWORD + suffix, null) != null) {
      txtServerPassword.setText(settings
          .getProperty(PropertiesHelper.ORDERWRITER_MAILSERVER_PASSWORD + suffix));
    }

    if (settings.getProperty(PropertiesHelper.ORDERWRITER_MAILSERVER_RECIPIENT + suffix, null) != null) {
      txtMailRecipient.setText(settings
          .getProperty(PropertiesHelper.ORDERWRITER_MAILSERVER_RECIPIENT + suffix));
    }

    if (settings.getProperty(PropertiesHelper.ORDERWRITER_MAILSERVER_SENDER + suffix, null) != null) {
      txtMailSender.setText(settings.getProperty(PropertiesHelper.ORDERWRITER_MAILSERVER_SENDER
          + suffix));
    }

    if (settings.getProperty(PropertiesHelper.ORDERWRITER_MAILSERVER_SUBJECT + suffix, null) != null) {
      txtMailSubject.setText(settings.getProperty(PropertiesHelper.ORDERWRITER_MAILSERVER_SUBJECT
          + suffix));
    }

    if (settings.getProperty(PropertiesHelper.ORDERWRITER_ADD_ECHECK_COMMENTS + suffix, null) != null) {
      chkECheckComments.setSelected(PropertiesHelper.getBoolean(settings,
          PropertiesHelper.ORDERWRITER_ADD_ECHECK_COMMENTS + suffix, false));
    }

    if (settings.getProperty(PropertiesHelper.ORDERWRITER_REMOVE_SC_COMMENTS + suffix, null) != null) {
      chkRemoveSCComments.setSelected(PropertiesHelper.getBoolean(settings,
          PropertiesHelper.ORDERWRITER_REMOVE_SC_COMMENTS + suffix, false));
    }

    if (settings.getProperty(PropertiesHelper.ORDERWRITER_REMOVE_SS_COMMENTS + suffix, null) != null) {
      chkRemoveSSComments.setSelected(PropertiesHelper.getBoolean(settings,
          PropertiesHelper.ORDERWRITER_REMOVE_SS_COMMENTS + suffix, false));
    }

    if (settings.getProperty(PropertiesHelper.ORDERWRITER_CONFIRMED_ONLY + suffix, null) != null) {
      chkConfirmedOnly.setSelected(PropertiesHelper.getBoolean(settings,
          PropertiesHelper.ORDERWRITER_CONFIRMED_ONLY + suffix, false));
    }

    if (PropertiesHelper.getList(settings, PropertiesHelper.ORDERWRITER_OUTPUT_FILE + suffix)
        .size() > 0) {
      while (cmbOutputFile.getItemCount() > 0) {
        cmbOutputFile.removeItemAt(0);
      }
      List<String> files =
          PropertiesHelper.getList(settings, PropertiesHelper.ORDERWRITER_OUTPUT_FILE + suffix);
      for (String file : files) {
        cmbOutputFile.addItem(file);
      }
    }

    if (settings.getProperty(PropertiesHelper.ORDERWRITER_MAILSERVER_RECIPIENT2 + suffix, null) != null) {
      txtMailRecipient2.setText(settings
          .getProperty(PropertiesHelper.ORDERWRITER_MAILSERVER_RECIPIENT2 + suffix));
    }

    setGroups(faction);
  }

  protected void updateRecipient() {
    Faction faction = (Faction) cmbFaction.getSelectedItem();
    String suffix = faction != null ? "." + faction.getID() : "";

    if (!chkUseSettingsFromCR.isEnabled() || !chkUseSettingsFromCR.isSelected()) {
      txtMailRecipient.setText(settings.getProperty(
          PropertiesHelper.ORDERWRITER_MAILSERVER_RECIPIENT + suffix, defaultEmail));
      txtMailSubject.setText(settings.getProperty(PropertiesHelper.ORDERWRITER_MAILSERVER_SUBJECT
          + suffix, defaultSubject));
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
    Faction faction = (Faction) cmbFaction.getSelectedItem();
    String suffix = "." + (faction == null ? "-" : faction.getID());

    settings.setProperty("OrderWriterDialog.x", getX() + "");
    settings.setProperty("OrderWriterDialog.y", getY() + "");
    PropertiesHelper.setList(settings, PropertiesHelper.ORDERWRITER_OUTPUT_FILE,
        getNewOutputFiles(cmbOutputFile));
    PropertiesHelper.setList(settings, PropertiesHelper.ORDERWRITER_OUTPUT_FILE + suffix,
        getNewOutputFiles(cmbOutputFile));

    if (chkFixedWidth.isSelected() == true) {
      try {
        settings.setProperty(PropertiesHelper.ORDERWRITER_FIXED_WIDTH, Integer
            .parseInt(txtFixedWidth.getText())
            + "");
        settings.setProperty(PropertiesHelper.ORDERWRITER_FIXED_WIDTH + suffix, Integer
            .parseInt(txtFixedWidth.getText())
            + "");
      } catch (NumberFormatException e) {
        settings.setProperty(PropertiesHelper.ORDERWRITER_FIXED_WIDTH, "0");
        settings.setProperty(PropertiesHelper.ORDERWRITER_FIXED_WIDTH + suffix, "0");
      }
    } else {
      try {
        settings.setProperty(PropertiesHelper.ORDERWRITER_FIXED_WIDTH, (-1 * Integer
            .parseInt(txtFixedWidth.getText()))
            + "");
        settings.setProperty(PropertiesHelper.ORDERWRITER_FIXED_WIDTH + suffix, (-1 * Integer
            .parseInt(txtFixedWidth.getText()))
            + "");
      } catch (NumberFormatException e) {
        settings.setProperty(PropertiesHelper.ORDERWRITER_FIXED_WIDTH, "0");
        settings.setProperty(PropertiesHelper.ORDERWRITER_FIXED_WIDTH + suffix, "0");
      }
    }

    settings.setProperty(PropertiesHelper.ORDERWRITER_ADD_ECHECK_COMMENTS, String
        .valueOf(chkECheckComments.isSelected()));
    settings.setProperty(PropertiesHelper.ORDERWRITER_ADD_ECHECK_COMMENTS + suffix, String
        .valueOf(chkECheckComments.isSelected()));
    settings.setProperty(PropertiesHelper.ORDERWRITER_REMOVE_SC_COMMENTS, String
        .valueOf(chkRemoveSCComments.isSelected()));
    settings.setProperty(PropertiesHelper.ORDERWRITER_REMOVE_SC_COMMENTS + suffix, String
        .valueOf(chkRemoveSCComments.isSelected()));
    settings.setProperty(PropertiesHelper.ORDERWRITER_REMOVE_SS_COMMENTS, String
        .valueOf(chkRemoveSSComments.isSelected()));
    settings.setProperty(PropertiesHelper.ORDERWRITER_REMOVE_SS_COMMENTS + suffix, String
        .valueOf(chkRemoveSSComments.isSelected()));
    settings.setProperty(PropertiesHelper.ORDERWRITER_CONFIRMED_ONLY, String
        .valueOf(chkConfirmedOnly.isSelected()));
    settings.setProperty(PropertiesHelper.ORDERWRITER_CONFIRMED_ONLY + suffix, String
        .valueOf(chkConfirmedOnly.isSelected()));

    if (chkUseSettingsFromCR.isEnabled()) {
      settings.setProperty(PropertiesHelper.ORDERWRITER_MAILSERVER_USE_CR_SETTINGS, String
          .valueOf(chkUseSettingsFromCR.isSelected()));
      settings.setProperty(PropertiesHelper.ORDERWRITER_MAILSERVER_USE_CR_SETTINGS + suffix, String
          .valueOf(chkUseSettingsFromCR.isSelected()));
    }

    settings.setProperty(PropertiesHelper.ORDERWRITER_MAILSERVER_CC2SENDER, String
        .valueOf(chkCCToSender.isSelected()));
    settings.setProperty(PropertiesHelper.ORDERWRITER_MAILSERVER_CC2SENDER + suffix, String
        .valueOf(chkCCToSender.isSelected()));

    settings.setProperty(PropertiesHelper.ORDERWRITER_MAILSERVER_SSL, String.valueOf(chkUseSSL
        .isSelected()));
    settings.setProperty(PropertiesHelper.ORDERWRITER_MAILSERVER_SSL + suffix, String
        .valueOf(chkUseSSL.isSelected()));
    settings.setProperty(PropertiesHelper.ORDERWRITER_MAILSERVER_TLS, String.valueOf(chkUseTLS
        .isSelected()));
    settings.setProperty(PropertiesHelper.ORDERWRITER_MAILSERVER_TLS + suffix, String
        .valueOf(chkUseTLS.isSelected()));

    if (chkSelRegionsOnly.isEnabled()) {
      settings.setProperty(PropertiesHelper.ORDERWRITER_SELECTED_REGIONS, String
          .valueOf(chkSelRegionsOnly.isSelected()));
      settings.setProperty(PropertiesHelper.ORDERWRITER_SELECTED_REGIONS + suffix, String
          .valueOf(chkSelRegionsOnly.isSelected()));
    }

    settings.setProperty(PropertiesHelper.ORDERWRITER_WRITE_TAGS_AS_VORLAGE_COMMENT, String
        .valueOf(chkWriteUnitTagsAsVorlageComment.isSelected()));
    settings.setProperty(PropertiesHelper.ORDERWRITER_WRITE_TAGS_AS_VORLAGE_COMMENT + suffix,
        String.valueOf(chkWriteUnitTagsAsVorlageComment.isSelected()));

    if (cmbFaction.getSelectedItem() != null) {
      settings.setProperty(PropertiesHelper.ORDERWRITER_FACTION, (((Faction) cmbFaction
          .getSelectedItem()).getID()).intValue()
          + "");
    } else {
      settings.setProperty(PropertiesHelper.ORDERWRITER_FACTION, "-1");
    }

    settings.setProperty(PropertiesHelper.ORDERWRITER_MAILSERVER_HOST, txtMailServer.getText());
    settings.setProperty(PropertiesHelper.ORDERWRITER_MAILSERVER_HOST + suffix, txtMailServer
        .getText());
    settings.setProperty(PropertiesHelper.ORDERWRITER_MAILSERVER_PORT, txtMailServerPort.getText());
    settings.setProperty(PropertiesHelper.ORDERWRITER_MAILSERVER_PORT + suffix, txtMailServerPort
        .getText());

    settings.setProperty(PropertiesHelper.ORDERWRITER_MAILSERVER_USEAUTH, String.valueOf(chkUseAuth
        .isSelected()));
    settings.setProperty(PropertiesHelper.ORDERWRITER_MAILSERVER_USEAUTH + suffix, String
        .valueOf(chkUseAuth.isSelected()));
    settings.setProperty(PropertiesHelper.ORDERWRITER_MAILSERVER_USERNAME, txtServerUsername
        .getText());
    settings.setProperty(PropertiesHelper.ORDERWRITER_MAILSERVER_USERNAME + suffix,
        txtServerUsername.getText());
    settings.setProperty(PropertiesHelper.ORDERWRITER_MAILSERVER_ASKPWD, String
        .valueOf(chkAskPassword.isSelected()));
    settings.setProperty(PropertiesHelper.ORDERWRITER_MAILSERVER_ASKPWD + suffix, String
        .valueOf(chkAskPassword.isSelected()));
    // for security reasons only store password if the user explicitly wants it
    if (!chkAskPassword.isSelected() && chkUseAuth.isSelected()) {
      settings.setProperty(PropertiesHelper.ORDERWRITER_MAILSERVER_PASSWORD, new String(
          txtServerPassword.getPassword()));
      settings.setProperty(PropertiesHelper.ORDERWRITER_MAILSERVER_PASSWORD + suffix, new String(
          txtServerPassword.getPassword()));
    } else {
      settings.setProperty(PropertiesHelper.ORDERWRITER_MAILSERVER_PASSWORD, "");
      settings.setProperty(PropertiesHelper.ORDERWRITER_MAILSERVER_PASSWORD + suffix, "");
    }

    settings.setProperty(PropertiesHelper.ORDERWRITER_MAILSERVER_RECIPIENT, txtMailRecipient
        .getText());
    settings.setProperty(PropertiesHelper.ORDERWRITER_MAILSERVER_RECIPIENT + suffix,
        txtMailRecipient.getText());
    settings.setProperty(PropertiesHelper.ORDERWRITER_MAILSERVER_SENDER, txtMailSender.getText());
    settings.setProperty(PropertiesHelper.ORDERWRITER_MAILSERVER_SENDER + suffix, txtMailSender
        .getText());
    settings.setProperty(PropertiesHelper.ORDERWRITER_MAILSERVER_SUBJECT, txtMailSubject.getText());
    settings.setProperty(PropertiesHelper.ORDERWRITER_MAILSERVER_SUBJECT + suffix, txtMailSubject
        .getText());
    settings.setProperty(PropertiesHelper.ORDERWRITER_MAILSERVER_RECIPIENT2, txtMailRecipient2
        .getText());
    settings.setProperty(PropertiesHelper.ORDERWRITER_MAILSERVER_RECIPIENT2 + suffix,
        txtMailRecipient2.getText());

    if (standAlone) {
      try {
        settings.store(new FileOutputStream(new File(System.getProperty("user.home"),
            "OrderWriterDialog.ini")), "");
      } catch (IOException e) {
        OrderWriterDialog.log.error("OrderWriterDialog.storeSettings()", e);
      }
    }
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
    fc.setAccessory(new HistoryAccessory(settings, fc));
    fc.addChoosableFileFilter(new EresseaFileFilter(EresseaFileFilter.TXT_FILTER));

    if (defaultFile != null) {
      fc.setSelectedFile(new File(defaultFile));
    }

    if (fc.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
      retVal = fc.getSelectedFile().getPath();
    }

    return retVal;
  }

  private Object[] write(Writer out, boolean forceUnixLineBreaks, Faction faction) {
    return write(out, forceUnixLineBreaks, true, false, faction);
  }

  private Object[] write(Writer out, boolean forceUnixLineBreaks, boolean closeStream,
      boolean confirm, Faction faction) {
    if (faction == null)
      return null;
    Object[] result = null;

    setCursor(new Cursor(Cursor.WAIT_CURSOR));

    try {
      Writer stream = null;

      if (chkFixedWidth.isSelected()) {
        int fixedWidth = -1;

        try {
          fixedWidth = Integer.parseInt(txtFixedWidth.getText());
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
      cw.setAddECheckComments(chkECheckComments.isSelected());
      cw.setRemoveComments(chkRemoveSCComments.isSelected(), chkRemoveSSComments.isSelected());
      cw.setConfirmedOnly(chkConfirmedOnly.isSelected());
      cw.setWriteUnitTagsAsVorlageComment(chkWriteUnitTagsAsVorlageComment.isSelected());

      if (chkSelRegionsOnly.isSelected() && (regions != null) && (regions.size() > 0)) {
        cw.setRegions(regions);
      }

      cw.setForceUnixLineBreaks(forceUnixLineBreaks);

      Object group = cmbGroup.getSelectedItem();

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
    Faction faction = (Faction) cmbFaction.getSelectedItem();
    if (faction != null)
      return copyToClipboard(faction);
    else
      return false;
  }

  protected boolean copyToClipboard(Faction faction) {
    if (!checkPassword(faction))
      return false;

    StringWriter sw = new StringWriter();

    Object[] parameters = write(sw, true, faction);
    if (parameters != null) {
      // there seems to be a problem with '\r\n'-style linebreaks
      // in the clipboard (you get two linebreaks instead of one)
      // so Unix-style linebreaks have to be enforced
      getToolkit().getSystemClipboard().setContents(
          new java.awt.datatransfer.StringSelection(sw.toString()), null);
      JOptionPane.showMessageDialog(this, (new java.text.MessageFormat(Resources
          .get("orderwriterdialog.msg.writtenunits.text"))).format(parameters), Resources
          .get("orderwriterdialog.msg.writtenunits.title"), JOptionPane.INFORMATION_MESSAGE);
      // storeSettings();
      return true;
    } else
      return false;
  }

  /**
   * Generates a new Filename according to the predefined (and stored) pattern
   */
  protected void autoFileName() {
    FileNameGeneratorFeed feed = new FileNameGeneratorFeed(super.getData().getDate().getDate());
    Faction f = (Faction) cmbFaction.getSelectedItem();
    if (f != null) {
      feed.setFaction(f.getName());
      feed.setFactionnr(f.getID().toString());
    }
    Object o = cmbGroup.getSelectedItem();
    Group g = null;
    if (o != null && !"".equals(o)) {
      g = (Group) o;
    }
    if (g != null) {
      feed.setGroup(g.getName());
    }

    String pattern = settings.getProperty("FileNameGenerator.ordersSaveFileNamePattern");

    if (pattern != null) {
      String newFileName = FileNameGenerator.getFileName(pattern, feed);

      cmbOutputFile.insertItemAt(newFileName, 0);
      cmbOutputFile.setSelectedItem(newFileName);
    }
  }

  /**
   * Mails the orders using the settings from the properties.
   */
  public boolean runMail() {
    return sendMail();
  }

  protected boolean sendMail() {
    Faction faction = (Faction) cmbFaction.getSelectedItem();
    if (faction == null)
      return false;

    if (!checkPassword(faction))
      return false;

    JButton ae = sendButton;
    ae.getTopLevelAncestor().setCursor(new Cursor(Cursor.WAIT_CURSOR));

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
      ae.getTopLevelAncestor().setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
      JOptionPane.showMessageDialog(ae, Resources
          .get("orderwriterdialog.msg.invalidfromaddress.text"), Resources
          .get("orderwriterdialog.msg.mailerror.title"), JOptionPane.WARNING_MESSAGE);

      return false;
    }

    if (recipient.equals("")) {
      ae.getTopLevelAncestor().setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
      JOptionPane.showMessageDialog(ae, Resources
          .get("orderwriterdialog.msg.invalidrecipient.text"), Resources
          .get("orderwriterdialog.msg.mailerror.title"), JOptionPane.WARNING_MESSAGE);

      return false;
    }

    if (mailHost.equals("")) {
      ae.getTopLevelAncestor().setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
      JOptionPane.showMessageDialog(ae, Resources
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
      ae.getTopLevelAncestor().setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
      // TODO update resource
      JOptionPane.showMessageDialog(ae, Resources
          .get("orderwriterdialog.msg.invalidsmtpserverport.text"), Resources
          .get("orderwriterdialog.msg.mailerror.title"), JOptionPane.WARNING_MESSAGE);

      return false;
    }

    if (username != null) {
      if (password == null) {
        password = showPasswordDialog(ae);
      }
      if (password == null) {
        ae.getTopLevelAncestor().setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
        return false;
      }
    }

    OrderWriterDialog.log.debug("attempting to send mail: " + mailHost + ", " + port + ", "
        + username + ", " + password + ", " + sender + ", " + recipient + ", " + subject);
    sendMailImpl(mailHost, port, username, password, sender, recipient, subject, ae, recipient2,
        faction);

    ae.getTopLevelAncestor().setCursor(new Cursor(Cursor.DEFAULT_CURSOR));

    // storeSettings();
    return true;
  }

  private void sendMailImpl(final String mailHost, int port, String username, String password,
      String sender, String recipient, String subject, final JButton ae, String cc, Faction faction) {

    final MultiPartEmail mailMessage;
    String contentType = "text/plain; charset=" + Encoding.DEFAULT;
    if (PropertiesHelper.getBoolean(settings, "TextEncoding.ISOsaveOrders", false)) {
      // new: force our default = ISO
      contentType = "text/plain; charset=" + Encoding.ISO;
    } else if (PropertiesHelper.getBoolean(settings, "TextEncoding.UTF8saveOrders", false)) {
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
      ae.getTopLevelAncestor().setCursor(new Cursor(Cursor.DEFAULT_CURSOR));

      Object msgArgs[] = { mailHost, e.toString() };
      JOptionPane.showMessageDialog(ae, (new java.text.MessageFormat(Resources
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
    final Object[] parameters = write(mailWriter, false, false, true, faction);

    mailMessage.setContent(mailWriter.toString(), contentType);

    if (username != null && password != null) {
      mailMessage.setAuthentication(username, password);
    }
    mailMessage.setSmtpPort(port);

    // TODO support for POP befor SMTP?
    // mailMessage.setPopBeforeSmtp(true, "host", "username", "password");

    OrderWriterDialog.log.info("sending...");
    final Component parent = this;
    final UserInterface ui = new ProgressBarUI(this);
    ui.setTitle(Resources.get("orderwriterdialog.btn.mail.caption"));
    ui.setMaximum(0);
    ui.show();
    new Thread(new Runnable() {

      public void run() {
        ui.setProgress(mailHost, 0);
        try {
          mailMessage.send();
          JOptionPane.showMessageDialog(parent, (new java.text.MessageFormat(Resources
              .get("orderwriterdialog.msg.writtenunits.text"))).format(parameters), Resources
              .get("orderwriterdialog.msg.writtenunits.title"), JOptionPane.INFORMATION_MESSAGE);

        } catch (EmailException e) {
          OrderWriterDialog.log.info("exception while sending message", e);

          Object msgArgs[] = { e.toString() };
          ui.showDialog((new JOptionPane(Resources.get("orderwriterdialog.msg.transfererror.text",
              msgArgs), JOptionPane.OK_OPTION)).createDialog(OrderWriterDialog.this, Resources
              .get("orderwriterdialog.msg.mailerror.title")));
        } finally {
          ae.getTopLevelAncestor().setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
          ui.ready();
        }
      }
    }).start();

  }

  /**
   * Shows a dialog asking for a password
   * 
   * @param ae
   * @return The user input or <code>null</code> if the user has canceled
   */
  private String showPasswordDialog(JButton ae) {
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
        JOptionPane.showOptionDialog(ae, panel, title, JOptionPane.OK_CANCEL_OPTION,
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
    File outputFile = new File((String) cmbOutputFile.getSelectedItem());
    Writer stream = null;

    try {
      stream = getWriter(outputFile);

      if (stream != null) {
        result = true;
        int[] parameters = new int[4];
        for (Faction f : data.getFactions()) {
          Object[] written = write(stream, false, false, false, f);
          if (written != null) {
            parameters[0] = parameters[0] + (Integer) written[0];
            parameters[1] = parameters[1] + (Integer) written[1];
            ++parameters[2];
          }
          ++parameters[3];
        }
        JOptionPane.showMessageDialog(this, (new java.text.MessageFormat(Resources
            .get("orderwriterdialog.msg.writtenunits.text"))).format(new Object[] { parameters[0],
            parameters[1], "" + parameters[2] + "/" + parameters[3] }), Resources
            .get("orderwriterdialog.msg.writtenunits.title"), JOptionPane.INFORMATION_MESSAGE);

        stream.close();
      }
    } catch (IOException ioe) {
      JOptionPane.showMessageDialog(this, Resources.getFormatted(
          "orderwriterdialog.msg.writeerror.text", outputFile.toString(), ioe), Resources
          .get("orderwriterdialog.msg.writeerror.title"), JOptionPane.WARNING_MESSAGE);
    }
    return result;
  }

  protected boolean saveToFile() {
    Faction faction = (Faction) cmbFaction.getSelectedItem();
    if (faction == null)
      return false;
    if (!checkPassword(faction))
      return false;

    File outputFile = new File((String) cmbOutputFile.getSelectedItem());
    Writer stream = null;

    try {

      stream = getWriter(outputFile);
      Object[] parameters = write(stream, false, faction);

      if (parameters != null) {
        JOptionPane.showMessageDialog(this, (new java.text.MessageFormat(Resources
            .get("orderwriterdialog.msg.writtenunits.text"))).format(parameters), Resources
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
    if (PropertiesHelper.getBoolean(settings, "TextEncoding.ISOsaveOrders", false)) {
      // new: force our default = ISO
      stream = new OutputStreamWriter(new FileOutputStream(outputFile), Encoding.ISO.toString());
    } else if (PropertiesHelper.getBoolean(settings, "TextEncoding.UTF8saveOrders", false)) {
      // new: force our default = UTF-8
      stream = new OutputStreamWriter(new FileOutputStream(outputFile), Encoding.UTF8.toString());
    } else {
      // old = default = system dependent
      stream = new FileWriter(outputFile);
    }
    return stream;
  }
}
