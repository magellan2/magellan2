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
import java.awt.Container;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.Toolkit;
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
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
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
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;

import magellan.client.utils.FileNameGenerator;
import magellan.library.EntityID;
import magellan.library.Faction;
import magellan.library.GameData;
import magellan.library.Group;
import magellan.library.TempUnit;
import magellan.library.Unit;
import magellan.library.io.file.FileBackup;
import magellan.library.utils.Encoding;
import magellan.library.utils.FileNameGeneratorFeed;
import magellan.library.utils.FixedWidthWriter;
import magellan.library.utils.OrderWriter;
import magellan.library.utils.PropertiesHelper;
import magellan.library.utils.Resources;
import magellan.library.utils.comparator.NameComparator;
import magellan.library.utils.logging.Logger;

import org.apache.commons.mail.EmailException;
import org.apache.commons.mail.MultiPartEmail;
import org.apache.tools.mail.MailMessage;

/**
 * A GUI for writing orders to a file or copy them to the clipboard. This class can be used as a
 * stand-alone application or can be integrated as dialog into another application.
 */
public class OrderWriterDialog extends InternationalizedDataDialog {
  private static final Logger log = Logger.getInstance(OrderWriterDialog.class);
  private boolean standAlone = false;
  private Collection regions = null;
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
  private JComboBox cmbFaction = null;
  private JComboBox cmbGroup = null;
  private JTextField txtMailServer = null;
  private JTextField txtMailServerPort = null;
  private JTextField txtMailRecipient = null;
  private JTextField txtMailSender = null;
  private JTextField txtMailSubject = null;
  private JTextField txtServerUsername = null;
  private JTextField txtServerPassword = null;
  private JCheckBox chkUseAuth = null;
  private JCheckBox chkAskPassword = null;
 
  private JButton sendButton;
  private JLabel lblServerUsername;
  private JLabel lblServerPassword;
  private JLabel lblMailRecipient;
  private JLabel lblMailSubject;
  
  /**
   * Create a stand-alone instance of OrderWriterDialog.
   */
  public OrderWriterDialog(GameData data) {
    super(null, false, null, data, new Properties());
    standAlone = true;
    
    try {
      settings.load(new FileInputStream(new File(System.getProperty("user.home"),"OrderWriterDialog.ini")));
    } catch(IOException e) {
      log.error("OrderWriterDialog.OrderWriterDialog(),", e);
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
  public OrderWriterDialog(Frame owner, boolean modal, GameData initData, Properties p, Collection selectedRegions) {
    super(owner, modal, null, initData, p);
    standAlone = false;
    this.regions = selectedRegions;
    init();
  }

  private void init() {
    addKeyListener(new KeyAdapter() {
        public void keyPressed(KeyEvent e) {
          if(e.getKeyCode() == KeyEvent.VK_ESCAPE) {
            quit(false);
          }
        }
      });
    addWindowListener(new WindowAdapter() {
        public void windowClosing(WindowEvent e) {
          quit(false);
        }
      });
    setContentPane(getMainPane());
    setTitle(Resources.get("orderwriterdialog.window.title"));
    pack();
//    setSize(550, 580);

    Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
    int x = Integer.parseInt(settings.getProperty("OrderWriterDialog.x",((screen.width - getWidth()) / 2) + ""));
    int y = Integer.parseInt(settings.getProperty("OrderWriterDialog.y",((screen.height - getHeight()) / 2) + ""));
    setLocation(x, y);
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
    
    return mainPanel;
  }

  private Container getButtonPanel() {
    JButton saveButton = new JButton(Resources.get("orderwriterdialog.btn.save.caption"));
    saveButton.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          saveToFile();
        }
      }
    );

    JButton clipboardButton = new JButton(Resources.get("orderwriterdialog.btn.clipboard.caption"));
    clipboardButton.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          copyToClipboard();
        }
      }
    );

    JButton mailButton = new JButton(Resources.get("orderwriterdialog.btn.mail.caption"));
    mailButton.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent ae) {
          sendMail();
        }
      }
    );
    sendButton = mailButton;

    JButton autoFileNameButton = new JButton(Resources.get("orderwriterdialog.btn.autofilename.caption"));
    autoFileNameButton.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent ae) {
          autoFileName();
        }
      }
    );
    

    
    JButton cancelButton = new JButton(Resources.get("orderwriterdialog.btn.close.caption"));
    cancelButton.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          quit(false);
        }
      }
    );

    JPanel buttonPanel = new JPanel(new GridLayout(6, 1, 0, 6));
    buttonPanel.add(saveButton);
    buttonPanel.add(clipboardButton);
    buttonPanel.add(mailButton);
    buttonPanel.add(autoFileNameButton);
    buttonPanel.add(cancelButton);
    
    return buttonPanel;
  }

  private Container getControlsPanel() {
    int fixedWidth = Integer.parseInt(settings.getProperty("OrderWriter.fixedWidth", "76"));
    chkFixedWidth = new JCheckBox(Resources.get("orderwriterdialog.chk.wordwrap.caption"), fixedWidth > 0);
    chkFixedWidth.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          txtFixedWidth.setEnabled(chkFixedWidth.isSelected());
        }
      }
    );

    JLabel lblFixedWidth1 = new JLabel(Resources.get("orderwriterdialog.lbl.wordwrapafter"));
    txtFixedWidth = new JTextField(10);
    txtFixedWidth.setText("" + Math.abs(fixedWidth));
    txtFixedWidth.setEnabled(chkFixedWidth.isSelected());

    JLabel lblFixedWidth2 = new JLabel(Resources.get("orderwriterdialog.lbl.wordwrapchars"));

    JPanel pnlFixedWidth = new JPanel();
    pnlFixedWidth.add(lblFixedWidth1);
    pnlFixedWidth.add(txtFixedWidth);
    pnlFixedWidth.add(lblFixedWidth2);

    chkECheckComments = new JCheckBox(Resources.get("orderwriterdialog.chk.addecheckcomments.caption"),(Boolean.valueOf(settings.getProperty("OrderWriter.addECheckComments","true"))).booleanValue());
    chkRemoveSCComments = new JCheckBox(Resources.get("orderwriterdialog.chk.removesemicoloncomments.caption"),(Boolean.valueOf(settings.getProperty("OrderWriter.removeSCComments","false"))).booleanValue());
    chkRemoveSSComments = new JCheckBox(Resources.get("orderwriterdialog.chk.removedoubleslashcomments.caption"),(Boolean.valueOf(settings.getProperty("OrderWriter.removeSSComments","false"))).booleanValue());
    chkConfirmedOnly = new JCheckBox(Resources.get("orderwriterdialog.chk.skipunconfirmedorders.caption"),(Boolean.valueOf(settings.getProperty("OrderWriter.confirmedOnly","false"))).booleanValue());
    chkSelRegionsOnly = new JCheckBox(Resources.get("orderwriterdialog.chk.selectedregions.caption"),(Boolean.valueOf(settings.getProperty("OrderWriter.includeSelRegionsOnly","false"))).booleanValue());
    chkSelRegionsOnly.setEnabled((regions != null) && (regions.size() > 0));
    chkWriteUnitTagsAsVorlageComment = new JCheckBox(Resources.get("orderwriterdialog.chk.writeUnitTagsAsVorlageComment.caption"),(Boolean.valueOf(settings.getProperty("OrderWriter.writeUnitTagsAsVorlageComment","false"))).booleanValue());

    JPanel pnlCmdSave = new JPanel();
    pnlCmdSave.setLayout(new BoxLayout(pnlCmdSave, BoxLayout.Y_AXIS));
    pnlCmdSave.setBorder(new TitledBorder(BorderFactory.createEtchedBorder(),Resources.get("orderwriterdialog.border.outputoptions")));
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
    cmbFaction.addItemListener(new ItemListener() {
        public void itemStateChanged(ItemEvent e) {
          if(
            // in this situation we need to reinitialize the groups list
            log.isDebugEnabled()) {
            // in this situation we need to reinitialize the groups list
            log.debug("Item even on faction combobox:" + e);
          }

          switch(e.getStateChange()) {
          case ItemEvent.SELECTED:

            Faction f = (Faction) e.getItem();
            setGroups(f);

            break;

          case ItemEvent.DESELECTED:
            setGroups(null);

            break;
          }
        }
      });

    for(Iterator iter = data.factions().values().iterator(); iter.hasNext();) {
      Faction f = (Faction) iter.next();

      if(f.isPrivileged()) {
        cmbFaction.addItem(f);
      }
    }

    Faction f = data.getFaction(EntityID.createEntityID(settings.getProperty("OrderWriter.faction","-1"), 10));

    if(f != null) {
      cmbFaction.setSelectedItem(f);
    }

    JPanel pnlCmdSave = new JPanel(new GridLayout(1, 1));
    pnlCmdSave.setBorder(new TitledBorder(BorderFactory.createEtchedBorder(),Resources.get("orderwriterdialog.border.faction")));
    pnlCmdSave.add(cmbFaction);

    return pnlCmdSave;
  }

  private void setGroups(Faction f) {
    cmbGroup.removeAllItems();

    if((f == null) || (f.getGroups() == null) || f.getGroups().isEmpty()) {
      return;
    }

    cmbGroup.addItem("");

    List<Group> sorted = new ArrayList<Group>(f.getGroups().values());
    Collections.sort(sorted, new NameComparator<Group>(null));

    for(Iterator<Group> iter = sorted.iterator(); iter.hasNext();) {
      Group g = iter.next();
      cmbGroup.addItem(g);
    }

    cmbGroup.setSelectedItem(null);
  }

  private Container getGroupPanel() {
    cmbGroup = new JComboBox();

    JPanel pnlCmdSave = new JPanel(new GridLayout(1, 1));
    pnlCmdSave.setBorder(new TitledBorder(BorderFactory.createEtchedBorder(),Resources.get("orderwriterdialog.border.group")));
    pnlCmdSave.add(cmbGroup);

    return pnlCmdSave;
  }

  private Container getFilePanel() {
    cmbOutputFile = new JComboBox(PropertiesHelper.getList(settings, "OrderWriter.outputFile").toArray());
    cmbOutputFile.setEditable(true);

    JButton btnOutputFile = new JButton("...");
    btnOutputFile.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          String outputFile = getFileName((String) cmbOutputFile.getSelectedItem());

          if(outputFile != null) {
            cmbOutputFile.insertItemAt(outputFile, 0);
            cmbOutputFile.setSelectedItem(outputFile);
          }
        }
      });

    JPanel pnlFile = new JPanel(new BorderLayout());
    pnlFile.setBorder(new TitledBorder(BorderFactory.createEtchedBorder(),Resources.get("orderwriterdialog.border.outputfile")));
    pnlFile.add(cmbOutputFile, BorderLayout.CENTER);
    pnlFile.add(btnOutputFile, BorderLayout.EAST);
    pnlFile.setPreferredSize(new Dimension(150,cmbOutputFile.getPreferredSize().height*2));
    return pnlFile;
  }

  private Container getMailPanel() {
    JLabel lblMailServer = new JLabel(Resources.get("orderwriterdialog.lbl.smtpserver.name"));
    txtMailServer = new JTextField(settings.getProperty("OrderWriter.mailServer", "smtp.bar.net"),20);
    lblMailServer.setLabelFor(txtMailServer);

    JLabel lblMailServerPort = new JLabel(Resources.get("orderwriterdialog.lbl.smtpserver.port"));
    txtMailServerPort = new JTextField(settings.getProperty("OrderWriter.mailServerPort", "25"),4);
    lblMailServerPort.setLabelFor(txtMailServerPort);

    lblServerUsername = new JLabel(Resources.get("orderwriterdialog.lbl.smtpserver.user"));
    txtServerUsername = new JTextField(settings.getProperty("OrderWriter.serverUsername", ""),20);
    lblServerUsername.setLabelFor(txtServerUsername);

    lblServerPassword = new JLabel(Resources.get("orderwriterdialog.lbl.smtpserver.password"));
    txtServerPassword = new JTextField(settings.getProperty("OrderWriter.serverPassword", ""),20);
    lblServerPassword.setLabelFor(txtServerPassword);
    

    chkAskPassword = new JCheckBox(Resources.get("orderwriterdialog.chk.askpassword.caption"),(Boolean.valueOf(settings.getProperty("OrderWriter.askPassword","true"))).booleanValue());
    chkAskPassword.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        int answer=0;
        if (!chkAskPassword.isSelected()){
          answer=JOptionPane.showConfirmDialog(chkAskPassword, Resources.get("orderwirterdialog.msg.passwordwarning"),"",JOptionPane.YES_NO_OPTION);
        }
        if (answer==0){
          lblServerPassword.setEnabled(!chkAskPassword.isSelected() || !chkAskPassword.isEnabled());
          txtServerPassword.setEnabled(!chkAskPassword.isSelected() || !chkAskPassword.isEnabled());
        }else{
          chkAskPassword.setSelected(true);
        }
        if (chkAskPassword.isSelected())
          txtServerPassword.setText("");
      }
    });

    chkUseAuth = new JCheckBox(Resources.get("orderwriterdialog.chk.useauth.caption"),(Boolean.valueOf(settings.getProperty("OrderWriter.useAuth","false"))).booleanValue());
    chkUseAuth.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        lblServerUsername.setEnabled(chkUseAuth.isSelected() && chkUseAuth.isEnabled());
        lblServerPassword.setEnabled(chkUseAuth.isSelected() && chkUseAuth.isEnabled() && !chkAskPassword.isSelected());
        txtServerUsername.setEnabled(chkUseAuth.isSelected() && chkUseAuth.isEnabled());
        txtServerPassword.setEnabled(chkUseAuth.isSelected() && chkUseAuth.isEnabled() && !chkAskPassword.isSelected());
        chkAskPassword.setEnabled(chkUseAuth.isSelected() && chkUseAuth.isEnabled());
      }
    });
    lblServerUsername.setEnabled(chkUseAuth.isSelected());
    lblServerPassword.setEnabled(chkUseAuth.isSelected()  && !chkAskPassword.isSelected());
    txtServerUsername.setEnabled(chkUseAuth.isSelected());
    txtServerPassword.setEnabled(chkUseAuth.isSelected()  && !chkAskPassword.isSelected());
    chkAskPassword.setEnabled(chkUseAuth.isSelected() );
    

    lblMailRecipient = new JLabel(Resources.get("orderwriterdialog.lbl.recipient"));

    // pavkovic 2002.01.23: enno wanted this change...
    String email = settings.getProperty("OrderWriter.mailRecipient","eressea-server@eressea.upb.de");

    if(email.toLowerCase().equals("eressea@kn-bremen.de")) {
      email = "eressea-server@eressea.upb.de";
    }

    if(email.toLowerCase().equals("eressea@eressea.kn-bremen.de")) {
      email = "eressea-server@eressea.upb.de";
    }

    if(email.toLowerCase().equals("eressea@eressea.amber.kn-bremen.de")) {
      email = "eressea-server@eressea.upb.de";
    }

    txtMailRecipient = new JTextField(email, 20);

    lblMailRecipient.setLabelFor(txtMailRecipient);

    JLabel lblMailSender = new JLabel(Resources.get("orderwriterdialog.lbl.sender"));
    txtMailSender = new JTextField(settings.getProperty("OrderWriter.mailSender", "foo@bar.net"),20);
    lblMailSender.setLabelFor(txtMailSender);

    lblMailSubject = new JLabel(Resources.get("orderwriterdialog.lbl.subject"));
    txtMailSubject = new JTextField(settings.getProperty("OrderWriter.mailSubject","Eressea Befehle"), 20);
    lblMailSubject.setLabelFor(txtMailSubject);

    JPanel pnlMail = new JPanel(new GridBagLayout());
    GridBagConstraints c = new GridBagConstraints();
    pnlMail.setBorder(new TitledBorder(BorderFactory.createEtchedBorder(),Resources.get("orderwriterdialog.border.mailoptions")));

    chkUseSettingsFromCR = new JCheckBox(Resources.get("orderwriterdialog.chk.usesettingsfromcr.caption"),(Boolean.valueOf(settings.getProperty("OrderWriter.useSettingsFromCr","true"))).booleanValue());
    chkUseSettingsFromCR.setEnabled((data != null) && (data.mailTo != null) && (data.mailSubject != null));
    chkUseSettingsFromCR.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          txtMailRecipient.setEnabled(!chkUseSettingsFromCR.isEnabled() || !chkUseSettingsFromCR.isSelected());
          txtMailSubject.setEnabled(!chkUseSettingsFromCR.isEnabled() || !chkUseSettingsFromCR.isSelected());
          lblMailRecipient.setEnabled(!chkUseSettingsFromCR.isEnabled() || !chkUseSettingsFromCR.isSelected());
          lblMailSubject.setEnabled(!chkUseSettingsFromCR.isEnabled() || !chkUseSettingsFromCR.isSelected());
        }
      }
    );
    txtMailRecipient.setEnabled(!chkUseSettingsFromCR.isEnabled() || !chkUseSettingsFromCR.isSelected());
    txtMailSubject.setEnabled(!chkUseSettingsFromCR.isEnabled() || !chkUseSettingsFromCR.isSelected());

    chkCCToSender = new JCheckBox(Resources.get("orderwriterdialog.chk.cctosender.caption"),(Boolean.valueOf(settings.getProperty("OrderWriter.CCToSender","true"))).booleanValue());

    c.anchor = GridBagConstraints.WEST;
    c.gridx = 0;
    c.gridy = 0;
    c.fill = GridBagConstraints.NONE;
    c.weightx = 0.0;
    c.weighty = 0.0;
    pnlMail.add(lblMailSender, c);

    c.anchor = GridBagConstraints.CENTER;
    c.gridx = 1;
//    c.gridy = 0;
    c.fill = GridBagConstraints.HORIZONTAL;
    c.weightx = 0.2;
    c.weighty = 0.0;
    pnlMail.add(txtMailSender, c);

    c.anchor = GridBagConstraints.CENTER;
    c.gridx = 2;
//    c.gridy = 0;
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
//  c.gridy = 1;
    c.fill = GridBagConstraints.HORIZONTAL;
    c.weightx = 0.2;
    c.weighty = 0.0;
    pnlMail.add(txtMailServer, c);

    c.anchor = GridBagConstraints.WEST;
    c.gridx = 0;
    c.gridy++;
    c.fill = GridBagConstraints.NONE;
    c.weightx = 0.0;
    c.weighty = 0.0;
    pnlMail.add(lblMailServerPort, c);

    c.anchor = GridBagConstraints.CENTER;
    c.gridx = 1;
//  c.gridy = 2;
    c.fill = GridBagConstraints.HORIZONTAL;
    c.weightx = 0.2;
    c.weighty = 0.0;
    pnlMail.add(txtMailServerPort, c);

    c.anchor = GridBagConstraints.WEST;
    c.gridx = 0;
    c.gridy++;
    c.fill = GridBagConstraints.NONE;
    c.weightx = 0.0;
    c.weighty = 0.0;
    pnlMail.add(lblServerUsername, c);

    c.anchor = GridBagConstraints.CENTER;
    c.gridx = 1;
//  c.gridy = 3;
    c.fill = GridBagConstraints.HORIZONTAL;
    c.weightx = 0.2;
    c.weighty = 0.0;
    pnlMail.add(txtServerUsername, c);

    c.anchor = GridBagConstraints.CENTER;
    c.gridx = 2;
//    c.gridy = 3;
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
//    c.gridy = 3;
    c.fill = GridBagConstraints.HORIZONTAL;
    c.weightx = 0.0;
    c.weighty = 0.0;
    pnlMail.add(chkAskPassword, c);

    c.anchor = GridBagConstraints.CENTER;
    c.gridx = 1;
//  c.gridy = 3;
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
//  c.gridy = 3;
    c.fill = GridBagConstraints.HORIZONTAL;
    c.weightx = 0.2;
    c.weighty = 0.0;
    pnlMail.add(txtMailRecipient, c);

    c.anchor = GridBagConstraints.CENTER;
    c.gridx = 2;
//    c.gridy = 3;
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
//    c.gridy = 3;
    c.fill = GridBagConstraints.HORIZONTAL;
    c.weightx = 0.2;
    c.weighty = 0.0;
    pnlMail.add(txtMailSubject, c);

    return pnlMail;
  }

  private void storeSettings() {
    settings.setProperty("OrderWriterDialog.x", getX() + "");
    settings.setProperty("OrderWriterDialog.y", getY() + "");
    PropertiesHelper.setList(settings, "OrderWriter.outputFile",getNewOutputFiles(cmbOutputFile));

    if(chkFixedWidth.isSelected() == true) {
      try {
        settings.setProperty("OrderWriter.fixedWidth",Integer.parseInt(txtFixedWidth.getText()) + "");
      } catch(NumberFormatException e) {
        settings.setProperty("OrderWriter.fixedWidth", "0");
      }
    } else {
      try {
        settings.setProperty("OrderWriter.fixedWidth",(-1 * Integer.parseInt(txtFixedWidth.getText())) + "");
      } catch(NumberFormatException e) {
        settings.setProperty("OrderWriter.fixedWidth", "0");
      }
    }

    settings.setProperty("OrderWriter.addECheckComments",String.valueOf(chkECheckComments.isSelected()));
    settings.setProperty("OrderWriter.removeSCComments",String.valueOf(chkRemoveSCComments.isSelected()));
    settings.setProperty("OrderWriter.removeSSComments",String.valueOf(chkRemoveSSComments.isSelected()));
    settings.setProperty("OrderWriter.confirmedOnly",String.valueOf(chkConfirmedOnly.isSelected()));

    if(chkUseSettingsFromCR.isEnabled()) {
      settings.setProperty("OrderWriter.useSettingsFromCr",String.valueOf(chkUseSettingsFromCR.isSelected()));
    }

    settings.setProperty("OrderWriter.CCToSender",String.valueOf(chkCCToSender.isSelected()));

    if(chkSelRegionsOnly.isEnabled()) {
      settings.setProperty("OrderWriter.includeSelRegionsOnly",String.valueOf(chkSelRegionsOnly.isSelected()));
    }

    settings.setProperty("OrderWriter.writeUnitTagsAsVorlageComment",String.valueOf(chkWriteUnitTagsAsVorlageComment.isSelected()));

    settings.setProperty("OrderWriter.faction",((EntityID) ((Faction) cmbFaction.getSelectedItem()).getID()).intValue() + "");

    settings.setProperty("OrderWriter.mailServer", txtMailServer.getText());
    settings.setProperty("OrderWriter.mailServerPort", txtMailServerPort.getText());

    settings.setProperty("OrderWriter.useAuth", String.valueOf(chkUseAuth.isSelected()));
    settings.setProperty("OrderWriter.serverUsername", txtServerUsername.getText());
    settings.setProperty("OrderWriter.askPassword", String.valueOf(chkAskPassword.isSelected()));
    // for security reasons only store password if the user explicitly wants it
    if (!chkAskPassword.isSelected() && chkUseAuth.isSelected())
      settings.setProperty("OrderWriter.serverPassword", txtServerPassword.getText());
    else
      settings.setProperty("OrderWriter.serverPassword", "");
      
    
    settings.setProperty("OrderWriter.mailRecipient", txtMailRecipient.getText());
    settings.setProperty("OrderWriter.mailSender", txtMailSender.getText());
    settings.setProperty("OrderWriter.mailSubject", txtMailSubject.getText());

    if(standAlone) {
      try {
        settings.store(new FileOutputStream(new File(System.getProperty("user.home"),"OrderWriterDialog.ini")), "");
      } catch(IOException e) {
        log.error("OrderWriterDialog.storeSettings()", e);
      }
    }
  }

  private List<String> getNewOutputFiles(JComboBox combo) {
    List<String> ret = new ArrayList<String>(combo.getItemCount() + 1);

    if(combo.getSelectedIndex() == -1) {
      ret.add((String)combo.getEditor().getItem());
    }

    for(int i = 0; i < Math.min(combo.getItemCount(), 6); i++) {
      ret.add((String)combo.getItemAt(i));
    }

    return ret;
  }

  private void quit(boolean bStoreSettings) {
    if(bStoreSettings) {
      storeSettings();
    }

    if(standAlone == true) {
      System.exit(0);
    } else {
      quit();
    }
  }

  private String getFileName(String defaultFile) {
    String retVal = null;

    JFileChooser fc = new JFileChooser();
    fc.setAccessory(new HistoryAccessory(settings, fc));

    if(defaultFile != null) {
      fc.setSelectedFile(new File(defaultFile));
    }

    if(fc.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
      retVal = fc.getSelectedFile().getPath();
    }

    return retVal;
  }

  private boolean write(Writer out, boolean forceUnixLineBreaks) {
    return write(out, forceUnixLineBreaks, true);
  }

  private boolean write(Writer out, boolean forceUnixLineBreaks, boolean closeStream) {
    setCursor(new Cursor(Cursor.WAIT_CURSOR));

    Faction faction = (Faction) cmbFaction.getSelectedItem();

    try {
      Writer stream = null;

      if(chkFixedWidth.isSelected()) {
        int fixedWidth = -1;

        try {
          fixedWidth = Integer.parseInt(txtFixedWidth.getText());
        } catch(NumberFormatException e) {
          setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
          JOptionPane.showMessageDialog(this, Resources.get("orderwriterdialog.msg.invalidfixedwidth.text"),Resources.get("orderwriterdialog.msg.invalidfixedwidth.title"),JOptionPane.WARNING_MESSAGE);
          return false;
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

      if(chkSelRegionsOnly.isSelected() && (regions != null) && (regions.size() > 0)) {
        cw.setRegions(regions);
      }

      cw.setForceUnixLineBreaks(forceUnixLineBreaks);

      Object group = cmbGroup.getSelectedItem();

      if(!"".equals(group)) {
        cw.setGroup((Group) group);
      }

      int writtenUnits = cw.write(stream);

      if(closeStream) {
        stream.close();
      }

      int allUnits = 0;

      for(Iterator<Unit> iter = data.units().values().iterator(); iter.hasNext();) {
        Unit u = iter.next();

        if(!(u instanceof TempUnit) && u.getFaction().equals(faction)) {
          allUnits++;
        }
      }

      JOptionPane.showMessageDialog(this,
                      (new java.text.MessageFormat(Resources.get("orderwriterdialog.msg.writtenunits.text"))).format(new Object[] {
                                                             String.valueOf(writtenUnits),
                                                             String.valueOf(allUnits),
                                                             faction
                                                           }),
                      Resources.get("orderwriterdialog.msg.writtenunits.title"),
                      JOptionPane.INFORMATION_MESSAGE);
    } catch(IOException ioe) {
      setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
      JOptionPane.showMessageDialog(this, Resources.get("orderwriterdialog.msg.erroronsave.text") + ioe.toString(),
                      Resources.get("orderwriterdialog.msg.erroronsave.title"),JOptionPane.WARNING_MESSAGE);

      return false;
    }

    setCursor(new Cursor(Cursor.DEFAULT_CURSOR));

    return true;
  }

  private boolean checkPassword() {
    Faction f = (Faction) cmbFaction.getSelectedItem();

    if(f==null || f.getPassword() == null) {
      Object msgArgs[] = { f.toString() };
      JOptionPane.showMessageDialog(getRootPane(),(new java.text.MessageFormat(Resources.get("orderwriterdialog.msg.nopassword.text"))).format(msgArgs));
      return false;
    } else {
      return true;
    }
  }

  /**
   * DOCUMENT-ME
   */
  public void runClipboard() {
    copyToClipboard();
  }

  protected void copyToClipboard() {
    if(!checkPassword()) {
      return;
    }

    StringWriter sw = new StringWriter();

    if(write(sw, true)) {
      // there seems to be a problem with '\r\n'-style linebreaks
      // in the clipboard (you get two linebreaks instead of one)
      // so Unix-style linebreaks have to be enforced
      getToolkit().getSystemClipboard().setContents(new java.awt.datatransfer.StringSelection(sw.toString()),null);
      storeSettings();
    }
  }

  /**
   * @author Fiete
   * 
   * Generates a new Filename according to the predefined (and stored) pattern
   *
   */
  protected void autoFileName(){
    FileNameGeneratorFeed feed = new FileNameGeneratorFeed(super.getData().getDate().getDate());
    Faction f = (Faction) cmbFaction.getSelectedItem();
    if (f!=null) {
      feed.setFaction(f.getName());
      feed.setFactionnr(f.getID().toString());
    }
    Object o = cmbGroup.getSelectedItem();
    Group g = null;
    if(o != null && !"".equals(o)) {
      g = (Group) o;
    }
    if (g!=null){
      feed.setGroup(g.getName());
    }
    
    String pattern = settings.getProperty("FileNameGenerator.ordersSaveFileNamePattern");
    
    String newFileName = FileNameGenerator.getFileName(pattern, feed);
    
    cmbOutputFile.insertItemAt(newFileName, 0);
    cmbOutputFile.setSelectedItem(newFileName);
    
  }
  
  /**
   * DOCUMENT-ME
   */
  public void runMail() {
    sendMail();
  }
  
  protected void sendMail() {
    if(!checkPassword()) {
      return;
    }

    JButton ae = sendButton;
    ae.getTopLevelAncestor().setCursor(new Cursor(Cursor.WAIT_CURSOR));

    // get mail parameters
    String mailHost = txtMailServer.getText();
    int port = 25;
    String username = null;
    String password = null;
    if (chkUseAuth.isSelected()){
      username = txtServerUsername.getText();
      if (!chkAskPassword.isSelected())
        password = txtServerPassword.getText();
    }
    String recipient = txtMailRecipient.getText();
    String sender = txtMailSender.getText();
    String subject = txtMailSubject.getText();

    if(chkUseSettingsFromCR.isEnabled() && chkUseSettingsFromCR.isSelected()) {
      subject = data.mailSubject;
      recipient = data.mailTo;
    }

    // check mail parameters
    if(sender.equals("")) {
      ae.getTopLevelAncestor().setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
      JOptionPane.showMessageDialog(ae, Resources.get("orderwriterdialog.msg.invalidfromaddress.text"),
                      Resources.get("orderwriterdialog.msg.mailerror.title"),
                      JOptionPane.WARNING_MESSAGE);

      return;
    }

    if(recipient.equals("")) {
      ae.getTopLevelAncestor().setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
      JOptionPane.showMessageDialog(ae, Resources.get("orderwriterdialog.msg.invalidrecipient.text"),
                      Resources.get("orderwriterdialog.msg.mailerror.title"),
                      JOptionPane.WARNING_MESSAGE);

      return;
    }

    if(mailHost.equals("")) {
      ae.getTopLevelAncestor().setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
      JOptionPane.showMessageDialog(ae, Resources.get("orderwriterdialog.msg.invalidsmtpserver.text"),
                      Resources.get("orderwriterdialog.msg.mailerror.title"),
                      JOptionPane.WARNING_MESSAGE);

      return;
    }
    
    try {
      port = Integer.parseInt(txtMailServerPort.getText());
    } catch (NumberFormatException e){
      port = -1;
    }
    if (port <= 0){
      ae.getTopLevelAncestor().setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
      // TODO update resource
      JOptionPane.showMessageDialog(ae, Resources.get("orderwriterdialog.msg.invalidsmtpserverport.text"),
                      Resources.get("orderwriterdialog.msg.mailerror.title"),
                      JOptionPane.WARNING_MESSAGE);

      return;
    }

    if (username!=null){
      if (password==null){
        password = showPasswordDialog(ae);
      }
      if (password==null){
        ae.getTopLevelAncestor().setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
        return;
      }
    }
    
    log.debug("attempting to send mail: "+mailHost + ", " + port + ", " +username + ", " +password + ", " +sender + ", " +recipient + ", " +subject);
    sendMailImpl(mailHost, port, username, password, sender, recipient, subject, ae);


    ae.getTopLevelAncestor().setCursor(new Cursor(Cursor.DEFAULT_CURSOR));

    storeSettings();
  }

  private void sendMailImpl(String mailHost, int port,  String username, String password, String sender, String recipient, String subject, JButton ae) {

    MultiPartEmail mailMessage;
    String contentType = "text/plain; charset=" + Encoding.DEFAULT;
    if (PropertiesHelper.getboolean(settings, "TextEncoding.ISOsaveOrders", false)) {
      // new: force our default = ISO
      contentType = "text/plain; charset=" + Encoding.ISO;
    } else if (PropertiesHelper.getboolean(settings, "TextEncoding.UTF8saveOrders", false)) {
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
      if(chkCCToSender.isSelected()) {
        mailMessage.addCc(sender);
      }
    } catch(EmailException e) {
      ae.getTopLevelAncestor().setCursor(new Cursor(Cursor.DEFAULT_CURSOR));

      Object msgArgs[] = { mailHost, e.toString() };
      JOptionPane.showMessageDialog(ae,
          (new java.text.MessageFormat(Resources.get("orderwriterdialog.msg.smtpserverunreachable.text"))).format(msgArgs),
          Resources.get("orderwriterdialog.msg.mailerror.title"),
          JOptionPane.WARNING_MESSAGE);

      if(log.isDebugEnabled()) {
        log.debug(e);
      }

      return;
    }

    StringWriter mailWriter = new StringWriter();
    write(mailWriter, false, false);

    mailMessage.setContent(mailWriter.toString(), contentType);
//  mailMessage.setCharset("ISO-8859-1");
//  mailMessage.setMsg(mailWriter.toString());
//  mailMessage.addPart(mailWriter.toString(), "text/plain; charset=ISO-8859-1");

    if (username!=null && password!=null){
      mailMessage.setAuthentication(username, password);
    }
    mailMessage.setSmtpPort(port);

    // TODO support for POP befor SMTP?
//  mailMessage.setPopBeforeSmtp(true, "host", "username", "password");

    // stm: this is a hack described at http://www.jguru.com/faq/view.jsp?EID=237257
    // shouldn't be necessary!
//  add handlers for main MIME types
//  MailcapCommandMap mc = (MailcapCommandMap)CommandMap.getDefaultCommandMap();
//  mc.addMailcap("text/html;; x-java-content-handler=com.sun.mail.handlers.text_html");
//  mc.addMailcap("text/xml;; x-java-content-handler=com.sun.mail.handlers.text_xml");
//  mc.addMailcap("text/plain;; x-java-content-handler=com.sun.mail.handlers.text_plain");
//  mc.addMailcap("multipart/*;; x-java-content-handler=com.sun.mail.handlers.multipart_mixed");
//  mc.addMailcap("message/rfc822;; x-java-content-handler=com.sun.mail.handlers.message_rfc822");
//  CommandMap.setDefaultCommandMap(mc);

    try {
      log.info("sending...");
      mailMessage.send();
    } catch(EmailException e) {
      ae.getTopLevelAncestor().setCursor(new Cursor(Cursor.DEFAULT_CURSOR));

      Object msgArgs[] = { e.toString() };
      JOptionPane.showMessageDialog(ae,
                      (new java.text.MessageFormat(Resources.get("orderwriterdialog.msg.transfererror.text"))).format(msgArgs),
                      Resources.get("orderwriterdialog.msg.mailerror.title"),
                      JOptionPane.WARNING_MESSAGE);
      log.info(e+((!e.getCause().equals(e))?(" "+e.getCause()):""));
      if(log.isDebugEnabled()) {
        log.debug(e);
      }

      return;
    }

  }
  
  
  /**
   * Shows a dialog asking for a password
   * 
   * @param ae 
   * @return The user input or <code>null</code> if the user has canceled
   */
  private String showPasswordDialog(JButton ae) {
    String title = Resources.get("orderwriterdialog.lbl.smtpserver.password");
    JPasswordField passwd = new JPasswordField(20){ public void setVisible(boolean b){ super.setVisible(b); if (b) requestFocus();}  };
    class MyPanel extends JPanel {
      JPasswordField pwd; 
      MyPanel(JPasswordField passwd) {
        pwd = passwd;
      }
      public void requestFocus(){ 
        pwd.requestFocus(); 
      } };
    JLabel passwdLabel = new JLabel(title);
    JPanel panel = new MyPanel(passwd);
    panel.add(passwdLabel);
    panel.add(passwd);
    int value = JOptionPane.showOptionDialog(ae, panel, title, JOptionPane.OK_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE, null, null, null);
//    JDialog dialog = pane.createDialog();
//    passwd.requestFocus();
//    dialog.setVisible(true);
//    dialog.dispose();
//
//    Object value = pane.getInputValue();
    if (value==0)
      return new String(passwd.getPassword());
    else
      return null;
  }


  public class SecurePrompt extends javax.swing.JDialog {
    public SecurePrompt(Frame parent, String title, String label) {
      super(parent, true);

      //{{INIT_CONTROLS
      setTitle(title);
      getContentPane().setLayout(null);
      setSize(403, 129);
      setVisible(false);
//      JLabel1.setText("User ID:");
//      getContentPane().add(JLabel1);
//      JLabel1.setBounds(12, 12, 48, 24);
      JLabel2.setText(label);
      getContentPane().add(JLabel2);
      JLabel2.setBounds(12, 48, 72, 24);
      _ok.setText("OK");
      getContentPane().add(_ok);
      _ok.setBounds(60, 84, 84, 24);
      getContentPane().add(_pwd);
      _pwd.setBounds(72, 48, 324, 24);
      _cancel.setText("Cancel");
      getContentPane().add(_cancel);
      _cancel.setBounds(264, 84, 84, 24);
      //}}

      //{{REGISTER_LISTENERS
      SymAction lSymAction = new SymAction();
      _ok.addActionListener(lSymAction);
      _cancel.addActionListener(lSymAction);
      //}}
    }

    public void setVisible(boolean b) {
      if (b)
        setLocation(50, 50);
      super.setVisible(b);
    }

    public void addNotify() {
      // Record the size of the window prior to calling parents addNotify.
      Dimension size = getSize();

      super.addNotify();

      if (frameSizeAdjusted)
        return;
      frameSizeAdjusted = true;

      // Adjust size of frame according to the insets
      Insets insets = getInsets();
      setSize(insets.left + insets.right + size.width, insets.top
          + insets.bottom + size.height);
    }

    // Used by addNotify
    boolean frameSizeAdjusted = false;

    //{{DECLARE_CONTROLS
    javax.swing.JLabel JLabel1 = new javax.swing.JLabel();

    javax.swing.JLabel JLabel2 = new javax.swing.JLabel();

    /**
     * The user ID entered.
     */
    javax.swing.JTextField _uid = new javax.swing.JTextField();

    /**
     */
    javax.swing.JButton _ok = new javax.swing.JButton();

    /**
     * The password is entered.
     */
    javax.swing.JPasswordField _pwd = new javax.swing.JPasswordField();

    javax.swing.JButton _cancel = new javax.swing.JButton();

    //}}

    class SymAction implements java.awt.event.ActionListener {
      public void actionPerformed(java.awt.event.ActionEvent event) {
        Object object = event.getSource();
        if (object == _ok)
          Ok_actionPerformed(event);
        else if (object == _cancel)
          Cancel_actionPerformed(event);
      }
    }

    /**
     * Called when ok is clicked.
     * 
     * @param event
     */
    void Ok_actionPerformed(java.awt.event.ActionEvent event) {
      setVisible(false);
    }

    /**
     * Called when cancel is clicked.
     * 
     * @param event
     */
    void Cancel_actionPerformed(java.awt.event.ActionEvent event) {
      _uid.setText("");
      _pwd.setText("");
      setVisible(false);
    }
  }
 
  
  /**
   * DOCUMENT-ME
   */
  public void runSave() {
    saveToFile();
  }

  protected void saveToFile() {
    if(!checkPassword()) {
      return;
    }

    File outputFile = new File((String) cmbOutputFile.getSelectedItem());

    if(outputFile.exists()) {
      // create backup file
      try {
        File backup = FileBackup.create(outputFile);
        log.info("Created backupfile " + backup);
      } catch(IOException ie) {
        log.warn("Could not create backupfile for file " + outputFile);
      }
    } 
    
    try {
      // apexo (Fiete) 20061205: if in properties, force ISO encoding
      Writer stream = null;
      if (PropertiesHelper.getboolean(settings, "TextEncoding.ISOsaveOrders", false)) {
        // new: force our default = ISO
        stream = new OutputStreamWriter(new FileOutputStream(outputFile), Encoding.ISO.toString());
      } else if (PropertiesHelper.getboolean(settings, "TextEncoding.UTF8saveOrders", false)) {
        // new: force our default = UTF-8
        stream = new OutputStreamWriter(new FileOutputStream(outputFile), Encoding.UTF8.toString());
      } else {
        // old = default = system dependend
        stream = new FileWriter(outputFile);
      }
      if(write(stream, false)) {
        quit(true);
      }
    } catch(IOException ioe) {
      Object msgArgs[] = { outputFile.toString() };
      JOptionPane.showMessageDialog(this,
                      (new java.text.MessageFormat(Resources.get("orderwriterdialog.msg.writeerror.text"))).format(msgArgs),
                      Resources.get("orderwriterdialog.msg.writeerror.title"),
                      JOptionPane.WARNING_MESSAGE);
    }
  }
}
