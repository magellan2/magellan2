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
  private JTextField txtMailRecipient = null;
  private JTextField txtMailSender = null;
  private JTextField txtMailSubject = null;
  private JButton sendButton;
  
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
    setSize(550, 580);

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

    return pnlFile;
  }

  private Container getMailPanel() {
    JLabel lblMailServer = new JLabel(Resources.get("orderwriterdialog.lbl.smtpserver"));
    txtMailServer = new JTextField(settings.getProperty("OrderWriter.mailServer", "smtp.bar.net"),20);
    lblMailServer.setLabelFor(txtMailServer);

    JLabel lblMailRecipient = new JLabel(Resources.get("orderwriterdialog.lbl.recipient"));

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

    lblMailRecipient.setLabelFor(txtMailServer);

    JLabel lblMailSender = new JLabel(Resources.get("orderwriterdialog.lbl.sender"));
    txtMailSender = new JTextField(settings.getProperty("OrderWriter.mailSender", "foo@bar.net"),20);
    lblMailSender.setLabelFor(txtMailServer);

    JLabel lblMailSubject = new JLabel(Resources.get("orderwriterdialog.lbl.subject"));
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
    c.gridy = 0;
    c.fill = GridBagConstraints.HORIZONTAL;
    c.weightx = 0.2;
    c.weighty = 0.0;
    pnlMail.add(txtMailSender, c);

    c.anchor = GridBagConstraints.CENTER;
    c.gridx = 2;
    c.gridy = 0;
    c.fill = GridBagConstraints.HORIZONTAL;
    c.weightx = 0.0;
    c.weighty = 0.0;
    pnlMail.add(chkCCToSender, c);

    c.anchor = GridBagConstraints.WEST;
    c.gridx = 0;
    c.gridy = 1;
    c.fill = GridBagConstraints.NONE;
    c.weightx = 0.0;
    c.weighty = 0.0;
    pnlMail.add(lblMailServer, c);

    c.anchor = GridBagConstraints.CENTER;
    c.gridx = 1;
    c.gridy = 1;
    c.fill = GridBagConstraints.HORIZONTAL;
    c.weightx = 0.2;
    c.weighty = 0.0;
    pnlMail.add(txtMailServer, c);

    c.anchor = GridBagConstraints.WEST;
    c.gridx = 0;
    c.gridy = 2;
    c.fill = GridBagConstraints.NONE;
    c.weightx = 0.0;
    c.weighty = 0.0;
    pnlMail.add(lblMailRecipient, c);

    c.anchor = GridBagConstraints.CENTER;
    c.gridx = 1;
    c.gridy = 2;
    c.fill = GridBagConstraints.HORIZONTAL;
    c.weightx = 0.2;
    c.weighty = 0.0;
    pnlMail.add(txtMailRecipient, c);

    c.anchor = GridBagConstraints.CENTER;
    c.gridx = 2;
    c.gridy = 2;
    c.fill = GridBagConstraints.HORIZONTAL;
    c.weightx = 0.0;
    c.weighty = 0.0;
    pnlMail.add(chkUseSettingsFromCR, c);

    c.anchor = GridBagConstraints.WEST;
    c.gridx = 0;
    c.gridy = 3;
    c.fill = GridBagConstraints.NONE;
    c.weightx = 0.0;
    c.weighty = 0.0;
    pnlMail.add(lblMailSubject, c);

    c.anchor = GridBagConstraints.CENTER;
    c.gridx = 1;
    c.gridy = 3;
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

    Writer mailWriter = null;

    JButton ae = sendButton;
    ae.getTopLevelAncestor().setCursor(new Cursor(Cursor.WAIT_CURSOR));

    // get mail parameters
    String mailHost = txtMailServer.getText();
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

    MailMessage mailMessage;

    try {
      mailMessage = new MailMessage(mailHost);
      mailMessage.from(sender);
      mailMessage.to(recipient);
      // added by Fiete 2006-08-28
      // support for header field: date
      // takes the new date(), formats it and appends the header
      SimpleDateFormat f = new SimpleDateFormat("EEE, d MMM yyyy HH:mm:ss Z (z)",Locale.US);
      mailMessage.setHeader("Date", f.format(new Date()));
      if (PropertiesHelper.getboolean(settings, "TextEncoding.ISOsaveOrders", false)) {
        // new: force our default = ISO
        mailMessage.setHeader("Content-Type", "text/plain; charset=" + Encoding.ISO);
      } else if (PropertiesHelper.getboolean(settings, "TextEncoding.UTF8saveOrders", false)) {
        // new: force our default = UTF-8
        mailMessage.setHeader("Content-Type", "text/plain; charset=" + Encoding.UTF8);
      } else {
        // old = default = system dependend
        mailWriter = new OutputStreamWriter(mailMessage.getPrintStream());
        mailMessage.setHeader("Content-Type", "text/plain; charset=" + System.getProperty("file.encoding"));
      }

      mailMessage.setSubject(subject);

      // specify copy for sender if CC to sender is selected
      if(chkCCToSender.isSelected()) {
        mailMessage.cc(sender);
      }
    } catch(IOException e) {
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

    try {
      if (PropertiesHelper.getboolean(settings, "TextEncoding.ISOsaveOrders", false)) {
        // new: force our default = ISO
        mailWriter = new OutputStreamWriter(mailMessage.getPrintStream(), Encoding.ISO.toString());
      } else if (PropertiesHelper.getboolean(settings, "TextEncoding.UTF8saveOrders", false)) {
        // new: force our default = UTF-8
        mailWriter = new OutputStreamWriter(mailMessage.getPrintStream(), Encoding.UTF8.toString());
      } else {
        // old = default = system dependend
        mailWriter = new OutputStreamWriter(mailMessage.getPrintStream());
      }
      write(mailWriter, false, false);
      mailMessage.sendAndClose();
      mailWriter.close();
    } catch(IOException e) {
      ae.getTopLevelAncestor().setCursor(new Cursor(Cursor.DEFAULT_CURSOR));

      Object msgArgs[] = { e.toString() };
      JOptionPane.showMessageDialog(ae,
                      (new java.text.MessageFormat(Resources.get("orderwriterdialog.msg.transfererror.text"))).format(msgArgs),
                      Resources.get("orderwriterdialog.msg.mailerror.title"),
                      JOptionPane.WARNING_MESSAGE);

      if(log.isDebugEnabled()) {
        log.debug(e);
      }

      return;
    }

    ae.getTopLevelAncestor().setCursor(new Cursor(Cursor.DEFAULT_CURSOR));

    storeSettings();
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
