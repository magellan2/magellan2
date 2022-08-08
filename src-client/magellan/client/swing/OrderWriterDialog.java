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
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.Charset;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.StringTokenizer;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.DESedeKeySpec;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.InputVerifier;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import javax.swing.text.JTextComponent;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.httpclient.methods.multipart.FilePart;
import org.apache.commons.httpclient.methods.multipart.Part;
import org.apache.commons.mail.EmailException;
import org.apache.commons.mail.MultiPartEmail;

import magellan.client.Help;
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
import magellan.library.gamebinding.GameSpecificOrderWriter;
import magellan.library.io.file.FileBackup;
import magellan.library.utils.Encoding;
import magellan.library.utils.FileNameGeneratorFeed;
import magellan.library.utils.FixedWidthWriter;
import magellan.library.utils.GetNetworkAddress;
import magellan.library.utils.HTTPClient;
import magellan.library.utils.HTTPResult;
import magellan.library.utils.PropertiesHelper;
import magellan.library.utils.Resources;
import magellan.library.utils.TrustLevels;
import magellan.library.utils.UserInterface;
import magellan.library.utils.comparator.NameComparator;
import magellan.library.utils.logging.Logger;

/**
 * A GUI for writing orders to a file or copy them to the clipboard. This class can be used as a stand-alone application
 * or can be integrated as dialog into another application.
 */
public class OrderWriterDialog extends InternationalizedDataDialog {
  public static class EmailVerifier extends InputVerifier {

    private Color bg;
    private boolean allowEmpty;
    private boolean allowList;
    private String separator;

    /**
     * Verifies one or a list of email addresses (separated by the given regular expression).
     */
    public EmailVerifier(boolean allowEmpty, boolean allowList, String separatorRegex) {
      this.allowEmpty = allowEmpty;
      this.allowList = allowList;
      separator = separatorRegex;
    }

    /**
     * Verifies a single email address.
     */
    public EmailVerifier() {
      this(false, false, null);
    }

    @Override
    public boolean verify(JComponent input) {
      try {
        String text = ((JTextComponent) input).getText();
        if (text.trim().isEmpty())
          return allowEmpty;

        if (!allowList)
          return !new InternetAddress(text, true).equals(null);
        else {
          for (String part : text.split(separator)) {
            @SuppressWarnings("unused")
            InternetAddress internetAddress = new InternetAddress(part, true);
          }
          return true;
        }

      } catch (AddressException e) {
        return false;
      }
    }

    @Override
    public boolean shouldYieldFocus(JComponent source, JComponent target) {
      if (bg == null) {
        bg = source.getBackground();
      }
      if (!verify(source)) {
        source.setBackground(errorColor);
      } else {
        source.setBackground(bg);
      }
      return verifyTarget(target);
    }

  }

  private static final Logger log = Logger.getInstance(OrderWriterDialog.class);

  private static final int FILE_PANEL = 0;
  private static final int CLIPBOARD_PANEL = 1;
  private static final int EMAIL_PANEL = 2;
  private static final int SERVER_PANEL = 3;
  private static final int NUM_PANELS = 4;

  protected static final int DEFAULT_MAILSERVER_PORT = 25;
  protected static final int DEFAULT_FIXED_WIDTH = 76;

  protected static final String DEFAULT_EMAIL = "eressea-server@eressea.kn-bremen.de";
  protected static final String DEFAULT_SUBJECT = "Eressea Befehle";

  protected static final String DEFAULT_SERVER_ADDRESS =
      "https://www.eressea.kn-bremen.de/eressea/orders-php/upload.php";

  protected static Color errorColor = new Color(255, 125, 125);

  private boolean standAlone = false;
  private Collection<Region> regions;

  // upload to server
  private JComboBox<String> cmbServerURLs;

  // file
  private JComboBox<String> cmbOutputFiles;
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
  private JCheckBox chkFixedWidth[] = new JCheckBox[4];
  private JTextField txtFixedWidth[] = new JTextField[4];
  private JCheckBox chkECheckComments[] = new JCheckBox[4];
  private JCheckBox chkRemoveSCComments[] = new JCheckBox[4];
  private JCheckBox chkRemoveSSComments[] = new JCheckBox[4];
  private JCheckBox chkConfirmedOnly[] = new JCheckBox[4];
  private JCheckBox chkSelRegionsOnly[] = new JCheckBox[4];
  private JCheckBox chkWriteUnitTagsAsVorlageComment[] = new JCheckBox[4];

  private JComboBox<Faction> cmbFactions;
  private JComboBox<Object> cmbGroups;

  private Container ancestor;

  private JCheckBox chkAutoFileName;

  private Properties localSettings;

  private String algorithm = "DESede";

  private Cipher cipher;

  private SecretKey key;

  private List<Provider> providers;

  private boolean focusServer;

  private boolean focusPassword;

  private JComboBox<Provider> cmbProvider;

  private List<Integer> panels;

  static final class MailSettings {

    String sender;
    String server;
    int port;
    boolean useSSL;
    boolean useTLS;
    boolean useAuth;
    String userName;

    MailSettings(String sender, String server, int port, String userName, boolean ssl, boolean tls, boolean auth) {
      this.sender = sender;
      this.server = server;
      this.port = port;
      this.userName = userName;
      useSSL = ssl;
      useTLS = tls;
      useAuth = auth;
    }
  }

  interface Provider {

    MailSettings getMailSettings(String email);

    boolean accept(String sender);

    String getName();
  }

  /**
   * Create a stand-alone instance of OrderWriterDialog.
   */
  public OrderWriterDialog(GameData data) {
    super(null, false, null, data, new Properties());
    standAlone = true;

    localSettings = settings;
    try {
      settings.load(new FileInputStream(new File(System.getProperty("user.home"), "OrderWriterDialog.ini")));
    } catch (IOException e) {
      OrderWriterDialog.log.error("OrderWriterDialog.OrderWriterDialog(),", e);
    }

    // init();
  }

  /**
   * Create a new OrderWriterDialog object as a dialog with a parent window.
   */
  public OrderWriterDialog(Frame owner, boolean modal, GameData initData, Properties p) {
    super(owner, modal, null, initData, p);
    standAlone = false;
    localizeSettings();

    // init();
  }

  /**
   * Create a new OrderWriterDialog object as a dialog with a parent window and a set of selected regions.
   */
  public OrderWriterDialog(Frame owner, boolean modal, GameData initData, Properties p,
      Collection<Region> selectedRegions) {
    super(owner, modal, null, initData, p);
    standAlone = false;
    regions = selectedRegions;
    localizeSettings();
    // init();
  }

  private void localizeSettings() {
    localSettings = new Properties();
    for (Entry<Object, Object> entry : settings.entrySet()) {
      localSettings.put(entry.getKey(), entry.getValue());
    }
    List<String> history = PropertiesHelper.getList(settings, PropertiesHelper.HISTORY_ACCESSORY_DIRECTORY_HISTORY);
    PropertiesHelper.setList(localSettings, PropertiesHelper.HISTORY_ACCESSORY_DIRECTORY_HISTORY, history);
  }

  @Override
  public void setVisible(boolean b) {
    if (b) {
      init();
      if (focusServer) {
        txtServerUsername.requestFocusInWindow();
        focusServer = false;
      }
      if (focusPassword) {
        txtServerPassword.requestFocusInWindow();
        focusPassword = false;
      }
    }
    super.setVisible(b);
  }

  private void init() {
    init(NUM_PANELS);
  }

  private void init(int type) {
    if (!canShow(data))
      throw new IllegalArgumentException("no faction with password");

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
    JComponent mainPane = getMainPane(type);
    ancestor = mainPane.getTopLevelAncestor();

    setContentPane(new JScrollPane(mainPane));
    setTitle(Resources.get("orderwriterdialog.window.title"));
    pack();

    SwingUtils.setLocation(this, localSettings, "OrderWriterDialog.x", "OrderWriterDialog.y");
  }

  private void initProviders() {
    providers = new ArrayList<Provider>(4);

    providers.add(createProvider("GMX", new String[] {
        "gmx.de", "gmx.net", "mein.gmx", "gmx.at", "gmx.ch", "mail.gmx", "email.gmx", "gmx.eu", "gmx.org",
        "gmx.info",
        "gmx.biz", "gmx.com", "fantasymail.de", "herr-der-mails.de", "fettabernett.de", "quantentunnel.de",
        "sonnnenkinder.org", "abwesend.de"
    },
        ".gmx", "mail.gmx.net", 587));

    providers.add(createProvider("WEB.DE", new String[] {}, "web.de", "smtp.web.de", 587));
    providers.add(createProvider("Posteo", new String[] { "posteo.de", "posteo.ch", "posteo.at", "posteo.eu",
        "posteo.me", "posteo.org", "posteo.net", "posteo.us" },
        null, "posteo.de", 587));
    providers.add(createProvider("T-Online", new String[] { "t-online.de", "magenta.de" },
        null, "securesmtp.t-online.de", 587));
    providers.add(createProvider("Gmail", new String[] { "googlemail.com" },
        "gmail.com", "smtp.googlemail.com", 587));
    providers.add(createProvider("Outlook", new String[] { "outlook.de", "outlook.com", "hotmail.com" },
        null, "smtp-mail.outlook.com", 587));
    providers.add(createProvider("Freenet",
        new String[] { "freenet.de", "fn.de", "freenetmail.de", "bossmail.de", "justmail.de" },
        null, "mx.freenet.de", 587));
    providers.add(createProvider("mailbox.org", new String[] {}, "mailbox.org", "smtp.mailbox.org", 465));
  }

  private Provider createProvider(String name, String[] extensions, String domain, String server, int port) {
    return createProvider(name, extensions, domain, server, port, true, true, true);
  }

  private Provider createProvider(String name, String[] extensions, String domain, String server, int port, boolean ssl,
      boolean tls, boolean auth) {
    return new Provider() {

      private Collection<String> gmxEtensions = new HashSet<String>(Arrays.asList(extensions));

      public MailSettings getMailSettings(String sender) {
        return new MailSettings(sender, server, port, sender, ssl, tls, auth);
      }

      public boolean accept(String sender) {
        if (sender == null)
          return false;
        String extension = sender.replaceFirst(".*@", "");
        if (extension != null
            && (gmxEtensions.contains(extension) ||
                (domain != null && extension.endsWith(domain))))
          return true;
        return false;
      }

      public String getName() {
        return name;
      }

      @Override
      public String toString() {
        return getName();
      }
    };
  }

  private JComponent getMainPane(int type) {

    JPanel mainPanel = new JPanel(new GridBagLayout());
    GridBagConstraints c = new GridBagConstraints();

    // insert groups first, so getFactionPanel can call setGroups
    c.anchor = GridBagConstraints.NORTHWEST;
    c.gridx = 0;
    c.gridy = 1;
    c.gridwidth = 1;
    c.gridheight = 1;
    c.fill = GridBagConstraints.HORIZONTAL;
    c.insets = new Insets(4, 4, 4, 4);
    c.weightx = 0.1;
    c.weighty = 0.0;
    mainPanel.add(getGroupPanel(), c);

    c.anchor = GridBagConstraints.NORTHWEST;
    c.gridx = 0;
    c.gridy = 0;
    mainPanel.add(getFactionPanel(), c);

    JPanel buttonPanel = new JPanel(new GridLayout2(0, 1, 0, 3));

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

    JButton helpButton = new JButton(Resources.get("orderwriterdialog.btn.help.caption"));
    String helpId = "menu_file_saveorders";
    try {
      Help help = Help.getInstance(settings);
      helpButton.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent ae) {
          try {
            help.showTopic(helpId);
          } catch (Exception ex) {
            log.error(ex);
          }
        }
      });
    } catch (Exception ee) {
      log.error("trouble with visiting help id", ee);
    }

    buttonPanel.add(cancelButton);
    buttonPanel.add(closeButton);

    buttonPanel.add(Box.createVerticalStrut(18));
    buttonPanel.add(helpButton);

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
    panels = new LinkedList<Integer>();

    if (type == NUM_PANELS || type == EMAIL_PANEL) {
      initProviders();
      initKey();
      Component panel = getOptionPanel(EMAIL_PANEL);
      tabbedPane.addTab(Resources.get("orderwriterdialog.tab.caption." + EMAIL_PANEL), panel);
      panels.add(EMAIL_PANEL);
    }

    if (type == NUM_PANELS || type == FILE_PANEL) {
      Component panel = getOptionPanel(FILE_PANEL);
      tabbedPane.addTab(Resources.get("orderwriterdialog.tab.caption." + FILE_PANEL), panel);
      panels.add(FILE_PANEL);
    }

    if (type == NUM_PANELS || type == CLIPBOARD_PANEL) {
      Component panel = getOptionPanel(CLIPBOARD_PANEL);
      tabbedPane.addTab(Resources.get("orderwriterdialog.tab.caption." + CLIPBOARD_PANEL), panel);
      panels.add(CLIPBOARD_PANEL);
    }

    if (type == NUM_PANELS || type == SERVER_PANEL) {
      Component panel = getOptionPanel(SERVER_PANEL);
      tabbedPane.addTab(Resources.get("orderwriterdialog.tab.caption." + SERVER_PANEL), panel);
      panels.add(SERVER_PANEL);
    }

    mainPanel.add(tabbedPane, c);

    return mainPanel;
  }

  private void initKey() {
    try {
      String myEncryptionKey = "Magellan2!SuperSecretKey";
      try {
        myEncryptionKey +=
            System.getProperty("os.name") + System.getProperty("os.arch") + System.getProperty("os.version");
      } catch (Throwable t) {
        // not a big deal
      }
      try {
        myEncryptionKey += GetNetworkAddress.getAddress(GetNetworkAddress.Type.MAC);
      } catch (Throwable t) {
        // not a big deal
      }
      byte[] keyAsBytes = myEncryptionKey.getBytes("UTF8");
      DESedeKeySpec myKeySpec = new DESedeKeySpec(keyAsBytes);
      SecretKeyFactory mySecretKeyFactory = SecretKeyFactory.getInstance(algorithm);
      cipher = Cipher.getInstance(algorithm);
      key = mySecretKeyFactory.generateSecret(myKeySpec);
    } catch (Exception e) {
      log.error("Could not initialize cipher!", e);
      cipher = null;
    }
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
    } else if (type == EMAIL_PANEL) {
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

    } else if (type == CLIPBOARD_PANEL) {
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
    } else if (type == SERVER_PANEL) {
      c.anchor = GridBagConstraints.NORTHWEST;
      c.gridx = 0;
      c.gridy++;
      c.gridwidth = 1;
      c.gridheight = 1;
      c.fill = GridBagConstraints.BOTH;
      c.insets = new Insets(0, 0, 0, 0);
      c.weightx = 0.1;
      c.weighty = 0.0;
      mainPanel.add(getServerPanel(), c);
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
        try {
          if (detailsButton.getText().contains(">>")) {
            controls.setVisible(true);
            detailsButton.setText("Details<<");
            pack();
          } else {
            controls.setVisible(false);
            detailsButton.setText("Details>>");
            pack();
          }
        } catch (Throwable t) {
          log.error(t);
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

    if (isGmEnabled()) {
      // a hack for writing all faction's orders (useful for testing the eressea server)
      JButton gmButton = new JButton("Save all");
      if (type == FILE_PANEL) {
        gmButton.setToolTipText(Resources.get("orderwriterdialog.btn.gm.tooltip", false));
        gmButton.addActionListener(new ActionListener() {
          public void actionPerformed(ActionEvent e) {
            saveAll();
          }
        });
      }

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
    } else if (type == CLIPBOARD_PANEL) {
      JButton clipboardButton = new JButton(Resources.get("orderwriterdialog.btn.clipboard.caption"));
      clipboardButton.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          copyToClipboard();
        }
      });
      buttonPanel.add(clipboardButton);
    } else if (type == EMAIL_PANEL) {
      JButton mailButton = new JButton(Resources.get("orderwriterdialog.btn.mail.caption"));
      mailButton.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent ae) {
          sendMail();
        }
      });
      JButton fillButton = new JButton(Resources.get("orderwriterdialog.btn.autofill.caption"));
      fillButton.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent ae) {
          if (getFaction() != null) {
            if (txtMailSender.getText().trim().isEmpty()) {
              txtMailSender.setText(getFaction().getEmail());
            }
            if (cmbProvider.getSelectedIndex() >= 0) {
              fillServer(cmbProvider.getItemAt(cmbProvider.getSelectedIndex()), txtMailSender.getText());
            }
          }
        }
      });

      buttonPanel.add(mailButton);
      buttonPanel.add(new JSeparator());
      buttonPanel.add(cmbProvider);
      buttonPanel.add(fillButton);
    } else if (type == SERVER_PANEL) {
      JButton sendButton = new JButton(Resources.get("orderwriterdialog.btn.server.caption"));
      sendButton.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent ae) {
          saveToServer();
        }
      });
      buttonPanel.add(sendButton);
    }

    return buttonPanel;
  }

  private Container getControlsPanel(final int type) {
    Faction faction = getFaction();
    String suffix = getSuffix(faction, type);

    // selection defaults are overwritten by factionChanged...
    int fixedWidth = PropertiesHelper.getInteger(localSettings, PropertiesHelper.ORDERWRITER_FIXED_WIDTH + suffix,
        DEFAULT_FIXED_WIDTH);
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

    chkECheckComments[type] = createCheckBox("addecheckcomments", PropertiesHelper.ORDERWRITER_ADD_ECHECK_COMMENTS,
        suffix, true);
    chkRemoveSCComments[type] = createCheckBox("removesemicoloncomments",
        PropertiesHelper.ORDERWRITER_REMOVE_SC_COMMENTS, suffix, type != FILE_PANEL);
    chkRemoveSSComments[type] = createCheckBox("removedoubleslashcomments",
        PropertiesHelper.ORDERWRITER_REMOVE_SS_COMMENTS, suffix, false);
    chkConfirmedOnly[type] = createCheckBox("skipunconfirmedorders", PropertiesHelper.ORDERWRITER_CONFIRMED_ONLY,
        suffix, false);
    chkSelRegionsOnly[type] = createCheckBox("selectedregions", PropertiesHelper.ORDERWRITER_SELECTED_REGIONS, suffix,
        false);
    chkSelRegionsOnly[type].setEnabled((regions != null) && (regions.size() > 0));
    chkWriteUnitTagsAsVorlageComment[type] = createCheckBox("writeUnitTagsAsVorlageComment",
        PropertiesHelper.ORDERWRITER_WRITE_TAGS_AS_VORLAGE_COMMENT, suffix, false);

    JPanel pnlCmdSave = new JPanel();
    pnlCmdSave.setLayout(new GridBagLayout());
    GridBagConstraints c = new GridBagConstraints(0, 0, 1, 1, 1, 0, GridBagConstraints.NORTHEAST,
        GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 0), 1, 1);
    pnlCmdSave.setBorder(new TitledBorder(BorderFactory.createEtchedBorder(), Resources.get(
        "orderwriterdialog.border.outputoptions")));
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

  private Group getGroup() {
    Object o = cmbGroups.getSelectedItem();
    if (o instanceof Group)
      return (Group) o;
    return null;
  }

  private Faction getFaction() {
    return (Faction) cmbFactions.getSelectedItem();
  }

  private Container getFactionPanel() {
    cmbFactions = new JComboBox<Faction>();

    for (Faction f : data.getFactions()) {
      if (TrustLevels.isPrivileged(f)) {
        cmbFactions.addItem(f);
      }
    }

    Faction f = data.getFaction(EntityID.createEntityID(localSettings.getProperty(PropertiesHelper.ORDERWRITER_FACTION,
        "-1"), 10, data.base));

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
        // OrderWriterDialog.log.finer("Item event on faction combobox:" + e + " "
        // + cmbFactions.getSelectedItem());

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
    pnlCmdSave.setBorder(new TitledBorder(BorderFactory.createEtchedBorder(), Resources.get(
        "orderwriterdialog.border.faction")));
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
    cmbGroups = new JComboBox<Object>();

    JPanel pnlCmdSave = new JPanel(new GridLayout(1, 1));
    pnlCmdSave.setBorder(new TitledBorder(BorderFactory.createEtchedBorder(), Resources.get(
        "orderwriterdialog.border.group")));
    pnlCmdSave.add(cmbGroups);

    return pnlCmdSave;
  }

  private Container getServerPanel() {
    String[] list = PropertiesHelper.getList(localSettings, PropertiesHelper.ORDERWRITER_SERVER_URL).toArray(
        new String[0]);
    cmbServerURLs = new JComboBox<String>(list == null ? new String[0] : list);
    cmbServerURLs.setEditable(true);

    JPanel pnlFile = new JPanel(new BorderLayout());
    pnlFile.setBorder(new TitledBorder(BorderFactory.createEtchedBorder(), Resources.get(
        "orderwriterdialog.border.output2server")));

    pnlFile.add(cmbServerURLs, BorderLayout.CENTER);
    addServerURL(DEFAULT_SERVER_ADDRESS);
    cmbServerURLs.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent arg0) {
        if ("comboBoxEdited".equals(arg0.getActionCommand())) {
          // todo syntax check of edited URL can be done via new URL()
          addServerURL((String) cmbServerURLs.getSelectedItem());
        }
      }
    });

    return pnlFile;
  }

  protected void addServerURL(String url) {
    // delete old entry to prevent too many items...
    for (int i = 0; i < cmbServerURLs.getItemCount(); i++) {
      String file = cmbServerURLs.getItemAt(i);
      if (file.equals(url)) {
        cmbServerURLs.removeItemAt(i);
        break;
      }
    }
    cmbServerURLs.insertItemAt(url, 0);
    cmbServerURLs.setSelectedItem(url);
  }

  private Container getFilePanel() {
    Faction faction = getFaction();
    String suffix = getSuffix(faction, FILE_PANEL);

    String[] list = PropertiesHelper.getList(localSettings, PropertiesHelper.ORDERWRITER_OUTPUT_FILE + suffix)
        .toArray(new String[0]);
    cmbOutputFiles = new JComboBox<String>(list);
    cmbOutputFiles.setEditable(true);

    JButton btnOutputFile = new JButton("...");
    btnOutputFile.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        String outputFile = getFileName((String) cmbOutputFiles.getSelectedItem());

        if (outputFile != null) {
          addFileName(outputFile);
        }
      }
    });

    JPanel pnlFile = new JPanel(new BorderLayout());
    pnlFile.setBorder(new TitledBorder(BorderFactory.createEtchedBorder(), Resources.get(
        "orderwriterdialog.border.outputfile")));

    pnlFile.add(cmbOutputFiles, BorderLayout.CENTER);
    cmbOutputFiles.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent arg0) {
        // log.finer(arg0.getActionCommand());
        if ("comboBoxEdited".equals(arg0.getActionCommand())) {
          addFileName((String) cmbOutputFiles.getSelectedItem());
        }
        updateAutoFileName();
      }
    });
    pnlFile.add(btnOutputFile, BorderLayout.EAST);

    JPanel pnl2 = new JPanel(new BorderLayout());
    pnl2.add(chkAutoFileName = createCheckBox("autofilename", PropertiesHelper.ORDERWRITER_AUTO_FILENAME, suffix,
        false), BorderLayout.WEST);
    chkAutoFileName.addItemListener(new ItemListener() {
      public void itemStateChanged(ItemEvent e) {
        txtOutputFileGenerated.setEnabled(chkAutoFileName.isSelected());
        updateAutoFileName();
      }
    });

    pnl2.add(txtOutputFileGenerated = new JTextField(), BorderLayout.CENTER);
    txtOutputFileGenerated.setEditable(false);
    txtOutputFileGenerated.setEnabled(chkAutoFileName.isSelected());
    updateAutoFileName();

    pnl2.add(new JLabel(Resources.get("util.filenamegenerator.field.ordersSaveFileNameInfo.label")),
        BorderLayout.SOUTH);

    pnlFile.add(pnl2, BorderLayout.SOUTH);

    // pnlFile.setPreferredSize(new Dimension(150, cmbOutputFile.getPreferredSize().height * 4));
    return pnlFile;
  }

  protected void addFileName(String outputFile) {
    // delete old entry to prevent too many items...
    for (int i = 0; i < cmbOutputFiles.getItemCount(); i++) {
      String file = cmbOutputFiles.getItemAt(i);
      if (file.equals(outputFile)) {
        cmbOutputFiles.removeItemAt(i);
        break;
      }
    }
    cmbOutputFiles.insertItemAt(outputFile, 0);
    cmbOutputFiles.setSelectedItem(outputFile);
  }

  private Container getMailPanel() {
    Faction faction = getFaction();
    String suffix = getSuffix(faction, EMAIL_PANEL);

    JLabel lblMailServer = new JLabel(Resources.get("orderwriterdialog.lbl.smtpserver.name"));
    txtMailServer = new JTextField(localSettings.getProperty(PropertiesHelper.ORDERWRITER_MAILSERVER_HOST + suffix,
        "smtp.bar.net"), 20);
    lblMailServer.setLabelFor(txtMailServer);

    JLabel lblMailServerPort = new JLabel(Resources.get("orderwriterdialog.lbl.smtpserver.port"));
    txtMailServerPort = new JTextField("" + PropertiesHelper.getInteger(localSettings,
        PropertiesHelper.ORDERWRITER_MAILSERVER_PORT + suffix, DEFAULT_MAILSERVER_PORT), 4);
    lblMailServerPort.setLabelFor(txtMailServerPort);

    lblServerUsername = new JLabel(Resources.get("orderwriterdialog.lbl.smtpserver.user"));
    txtServerUsername = new JTextField(localSettings.getProperty(PropertiesHelper.ORDERWRITER_MAILSERVER_USERNAME
        + suffix, ""), 20);
    lblServerUsername.setLabelFor(txtServerUsername);

    lblServerPassword = new JLabel(Resources.get("orderwriterdialog.lbl.smtpserver.password"));

    String pw = getPassword(localSettings, suffix);
    txtServerPassword = new JPasswordField(pw, 20);
    lblServerPassword.setLabelFor(txtServerPassword);

    chkAskPassword = createCheckBox("askpassword", PropertiesHelper.ORDERWRITER_MAILSERVER_ASKPWD, suffix, true);
    chkAskPassword.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        setAskPassword();
      }
    });

    chkUseAuth = createCheckBox("useauth", PropertiesHelper.ORDERWRITER_MAILSERVER_USEAUTH, suffix, true);
    chkUseAuth.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        setAskPassword();
      }
    });

    lblServerUsername.setEnabled(chkUseAuth.isSelected());
    lblServerPassword.setEnabled(chkUseAuth.isSelected() && !chkAskPassword.isSelected());
    txtServerUsername.setEnabled(chkUseAuth.isSelected());
    txtServerPassword.setEnabled(chkUseAuth.isSelected() && !chkAskPassword.isSelected());
    chkAskPassword.setEnabled(chkUseAuth.isSelected());

    lblMailRecipient = new JLabel(Resources.get("orderwriterdialog.lbl.recipient"));

    String email = localSettings.getProperty(PropertiesHelper.ORDERWRITER_MAILSERVER_RECIPIENT + suffix, DEFAULT_EMAIL);

    txtMailRecipient = new JTextField(email, 20);
    txtMailRecipient.setInputVerifier(new EmailVerifier());

    lblMailRecipient.setLabelFor(txtMailRecipient);

    Provider[] providerNames = new Provider[providers.size()];
    int i = 0;
    for (Provider p : providers) {
      providerNames[i++] = p;
    }
    cmbProvider = new JComboBox<Provider>(providerNames);

    JLabel lblMailSender = new JLabel(Resources.get("orderwriterdialog.lbl.sender"));
    txtMailSender = new JTextField(localSettings.getProperty(PropertiesHelper.ORDERWRITER_MAILSERVER_SENDER + suffix),
        20);

    txtMailSender.setInputVerifier(new EmailVerifier() {
      @Override
      public boolean shouldYieldFocus(JComponent source, JComponent target) {
        boolean r = super.shouldYieldFocus(source, target);
        if (verify(source)) {
          Provider p = getMailSettings(txtMailSender.getText());
          cmbProvider.setSelectedItem(p);
        }
        return r;
      }
    });
    lblMailSender.setLabelFor(txtMailSender);

    lblMailSubject = new JLabel(Resources.get("orderwriterdialog.lbl.subject"));
    lblMailSubject.setLabelFor(txtMailSubject);
    txtMailSubject = new JTextField(localSettings.getProperty(PropertiesHelper.ORDERWRITER_MAILSERVER_SUBJECT + suffix,
        DEFAULT_SUBJECT), 20);
    lblMailSubject.setLabelFor(txtMailSubject);

    // CC: (Fiete 20090120)
    lblMailRecipient2 = new JLabel("CC:");
    txtMailRecipient2 = new JTextField(localSettings.getProperty(PropertiesHelper.ORDERWRITER_MAILSERVER_RECIPIENT2
        + suffix, ""), 20);
    txtMailRecipient2.setInputVerifier(new EmailVerifier(true, true, "[,;]"));
    lblMailRecipient2.setLabelFor(txtMailRecipient2);

    JPanel pnlMail = new JPanel(new GridBagLayout());
    GridBagConstraints c = new GridBagConstraints();
    pnlMail.setBorder(new TitledBorder(BorderFactory.createEtchedBorder(), Resources.get(
        "orderwriterdialog.border.mailoptions")));

    chkUseSettingsFromCR = createCheckBox("usesettingsfromcr", PropertiesHelper.ORDERWRITER_MAILSERVER_USE_CR_SETTINGS,
        suffix, true);
    chkUseSettingsFromCR.setEnabled((data != null) && (data.mailTo != null) && (data.mailSubject != null));
    chkUseSettingsFromCR.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        updateRecipient();
      }
    });

    chkCCToSender = createCheckBox("cctosender", PropertiesHelper.ORDERWRITER_MAILSERVER_CC2SENDER, suffix, true);

    chkUseSSL = createCheckBox("usessl", PropertiesHelper.ORDERWRITER_MAILSERVER_SSL, suffix, false);
    chkUseTLS = createCheckBox("usetls", PropertiesHelper.ORDERWRITER_MAILSERVER_TLS, suffix, false);

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

  protected void setAskPassword() {
    boolean auth = chkUseAuth.isSelected() && chkUseAuth.isEnabled();
    lblServerUsername.setEnabled(auth);
    txtServerUsername.setEnabled(auth);
    chkAskPassword.setEnabled(auth);

    int answer = JOptionPane.NO_OPTION;
    if (!chkAskPassword.isSelected() && chkAskPassword.isEnabled()) {
      answer = JOptionPane.showConfirmDialog(chkAskPassword, Resources.get("orderwriterdialog.msg.passwordwarning"),
          "", JOptionPane.YES_NO_OPTION);
    }
    if (answer != JOptionPane.YES_OPTION) {
      chkAskPassword.setSelected(true);
    }

    boolean ask = !chkAskPassword.isSelected() || !chkAskPassword.isEnabled();
    lblServerPassword.setEnabled(auth && ask);
    txtServerPassword.setEnabled(auth && ask);
    if (auth && txtServerUsername.getText().length() == 0) {
      focusServer = true;
      txtServerUsername.requestFocusInWindow();
    } else if (ask && txtServerPassword.getPassword().length == 0) {
      focusPassword = true;
      txtServerPassword.requestFocusInWindow();
    }

    if (chkAskPassword.isSelected()) {
      txtServerPassword.setText("");
    }
  }

  private JCheckBox createCheckBox(String name, String resourceKey, String suffix, boolean selected) {
    JCheckBox chkBox = new JCheckBox(Resources.get("orderwriterdialog.chk." + name + ".caption"), resourceKey == null
        ? selected : PropertiesHelper.getBoolean(localSettings, suffix == null
            ? resourceKey : (resourceKey + suffix), selected));
    chkBox.setToolTipText(Resources.get("orderwriterdialog.chk." + name + ".tooltip", false));
    return chkBox;
  }

  /**
   * If the faction changed, the configuration for the mailserver and the selected output file may changed...this method
   * checks this.
   *
   * @param faction
   */
  protected void factionChanged(Faction faction, int type) {
    if (faction == null) {
      faction = getFaction();
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

      chkUseSSL.setSelected(PropertiesHelper.getBoolean(localSettings, PropertiesHelper.ORDERWRITER_MAILSERVER_SSL
          + suffix, false));

      chkUseTLS.setSelected(PropertiesHelper.getBoolean(localSettings, PropertiesHelper.ORDERWRITER_MAILSERVER_TLS
          + suffix, false));

      txtMailServer.setText(localSettings.getProperty(PropertiesHelper.ORDERWRITER_MAILSERVER_HOST + suffix, ""));

      txtMailServerPort.setText("" + PropertiesHelper.getInteger(localSettings,
          PropertiesHelper.ORDERWRITER_MAILSERVER_PORT + suffix, DEFAULT_MAILSERVER_PORT));

      chkUseAuth.setSelected(PropertiesHelper.getBoolean(localSettings, PropertiesHelper.ORDERWRITER_MAILSERVER_USEAUTH
          + suffix, false));
      lblServerUsername.setEnabled(chkUseAuth.isSelected() && chkUseAuth.isEnabled());
      lblServerPassword.setEnabled(chkUseAuth.isSelected() && chkUseAuth.isEnabled() && !chkAskPassword.isSelected());
      txtServerUsername.setEnabled(chkUseAuth.isSelected() && chkUseAuth.isEnabled());
      txtServerPassword.setEnabled(chkUseAuth.isSelected() && chkUseAuth.isEnabled() && !chkAskPassword.isSelected());
      chkAskPassword.setEnabled(chkUseAuth.isSelected() && chkUseAuth.isEnabled());

      txtServerUsername.setText(localSettings.getProperty(PropertiesHelper.ORDERWRITER_MAILSERVER_USERNAME + suffix));

      chkAskPassword.setSelected(PropertiesHelper.getBoolean(localSettings,
          PropertiesHelper.ORDERWRITER_MAILSERVER_ASKPWD + suffix, false));
      lblServerPassword.setEnabled(!chkAskPassword.isSelected() || !chkAskPassword.isEnabled());
      txtServerPassword.setEnabled(!chkAskPassword.isSelected() || !chkAskPassword.isEnabled());

      txtServerPassword.setText(getPassword(localSettings, suffix));

      txtMailSender.setText(localSettings.getProperty(PropertiesHelper.ORDERWRITER_MAILSERVER_SENDER + suffix));

      txtMailRecipient2.setText(localSettings.getProperty(PropertiesHelper.ORDERWRITER_MAILSERVER_RECIPIENT2 + suffix));

      if (txtMailSender.getText().equals("") && faction != null) {
        txtMailSender.setText(faction.getEmail());
        autoFillServer(txtMailSender.getText());
      }
    }

    chkSelRegionsOnly[type].setSelected(PropertiesHelper.getBoolean(localSettings,
        PropertiesHelper.ORDERWRITER_SELECTED_REGIONS + suffix, false));

    int fixedWidth = PropertiesHelper.getInteger(localSettings, PropertiesHelper.ORDERWRITER_FIXED_WIDTH + suffix,
        DEFAULT_FIXED_WIDTH);
    chkFixedWidth[type].setSelected(fixedWidth > 0);
    txtFixedWidth[type].setText("" + Math.abs(fixedWidth));
    txtFixedWidth[type].setEnabled(chkFixedWidth[type].isSelected());

    chkWriteUnitTagsAsVorlageComment[type].setSelected(PropertiesHelper.getBoolean(localSettings,
        PropertiesHelper.ORDERWRITER_WRITE_TAGS_AS_VORLAGE_COMMENT + suffix, false));

    chkECheckComments[type].setSelected(PropertiesHelper.getBoolean(localSettings,
        PropertiesHelper.ORDERWRITER_ADD_ECHECK_COMMENTS + suffix, true));

    chkRemoveSCComments[type].setSelected(PropertiesHelper.getBoolean(localSettings,
        PropertiesHelper.ORDERWRITER_REMOVE_SC_COMMENTS + suffix, type != FILE_PANEL));

    chkRemoveSSComments[type].setSelected(PropertiesHelper.getBoolean(localSettings,
        PropertiesHelper.ORDERWRITER_REMOVE_SS_COMMENTS + suffix, false));

    chkConfirmedOnly[type].setSelected(PropertiesHelper.getBoolean(localSettings,
        PropertiesHelper.ORDERWRITER_CONFIRMED_ONLY + suffix, false));

    if (type == FILE_PANEL) {
      List<String> files = PropertiesHelper.getList(localSettings, PropertiesHelper.ORDERWRITER_OUTPUT_FILE + suffix);
      while (cmbOutputFiles.getItemCount() > 0) {
        cmbOutputFiles.removeItemAt(0);
      }
      if (files != null) {
        for (String file : files) {
          if (!file.isEmpty()) {
            cmbOutputFiles.addItem(file);
          }
        }
      }
      if (cmbOutputFiles.getItemCount() == 0) {
        cmbOutputFiles.addItem("orders.txt");
        cmbOutputFiles.addItem("orders-{factionnr}-{round}.txt");
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

    if (type == SERVER_PANEL) {
      List<String> files = PropertiesHelper.getList(localSettings, PropertiesHelper.ORDERWRITER_SERVER_URL + suffix);
      while (cmbServerURLs.getItemCount() > 0) {
        cmbServerURLs.removeItemAt(0);
      }
      boolean hasDefault = false;
      if (files != null) {
        for (String file : files) {
          if (!file.isEmpty()) {
            hasDefault |= file.equals(DEFAULT_SERVER_ADDRESS);
            cmbServerURLs.addItem(file);
          }
        }
      }
      if (!hasDefault) {
        addServerURL(DEFAULT_SERVER_ADDRESS);
      }
    }

    setGroups(faction);
  }

  private Provider autoFillServer(String email) {
    Provider provider = getMailSettings(email);
    if (JOptionPane.showConfirmDialog(this, Resources.get("orderwriterdialog.msg.autofill.provider", email),
        "", JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION)
      return provider;
    if (provider == null) {
      String[] providerNames = new String[providers.size()];
      int i = 0;
      for (Provider p : providers) {
        providerNames[i++] = p.getName();
      }
      Object answer = JOptionPane.showInputDialog(this,
          Resources.get("orderwriterdialog.msg.autofill.select", email), "",
          JOptionPane.QUESTION_MESSAGE, null, providerNames, 0);
      if (answer != null) {
        for (Provider p : providers)
          if (p.getName().equals(answer)) {
            provider = p;
            break;
          }
      }
    }
    if (provider != null) {
      fillServer(provider, email);
    }
    return provider;
  }

  private void fillServer(Provider provider, String email) {
    MailSettings mailSettings = provider.getMailSettings(email);

    txtMailSender.setText(mailSettings.sender);
    txtMailServer.setText(mailSettings.server);
    txtMailServerPort.setText(String.valueOf(mailSettings.port));
    txtServerUsername.setText(mailSettings.userName);
    chkUseSSL.setSelected(mailSettings.useSSL);
    chkUseTLS.setSelected(mailSettings.useTLS);
    chkUseAuth.setSelected(mailSettings.useAuth);
    setAskPassword();
  }

  private Provider getMailSettings(String sender) {
    for (Provider provider : providers) {
      if (provider.accept(sender))
        return provider;
    }
    return null;
  }

  private String getSuffix(Faction faction, int type) {
    String suffix = "." + (faction == null ? "-" : faction.getID()) + "." + type;
    return suffix;
  }

  protected void updateRecipient() {
    Faction faction = getFaction();
    String suffix = getSuffix(faction, EMAIL_PANEL);

    if (!chkUseSettingsFromCR.isEnabled() || !chkUseSettingsFromCR.isSelected()) {
      txtMailRecipient.setText(localSettings.getProperty(PropertiesHelper.ORDERWRITER_MAILSERVER_RECIPIENT + suffix,
          DEFAULT_EMAIL));
      txtMailSubject.setText(localSettings.getProperty(PropertiesHelper.ORDERWRITER_MAILSERVER_SUBJECT + suffix,
          DEFAULT_SUBJECT));
    } else {
      txtMailRecipient.setText(data.mailTo);
      txtMailSubject.setText(data.mailSubject);
    }
    txtMailRecipient.setEnabled(!chkUseSettingsFromCR.isEnabled() || !chkUseSettingsFromCR.isSelected());
    txtMailSubject.setEnabled(!chkUseSettingsFromCR.isEnabled() || !chkUseSettingsFromCR.isSelected());
    lblMailRecipient.setEnabled(!chkUseSettingsFromCR.isEnabled() || !chkUseSettingsFromCR.isSelected());
    lblMailSubject.setEnabled(!chkUseSettingsFromCR.isEnabled() || !chkUseSettingsFromCR.isSelected());
  }

  private void storeSettings() {
    for (int type = 0; type < NUM_PANELS; ++type) {
      if (panels.contains(type)) {
        Faction f = getFaction();
        if (f != null) {
          storeSettings(localSettings, f, type);
        }
      }
    }
    settings.putAll(localSettings);
    if (standAlone) {
      try {
        settings.store(new FileOutputStream(new File(System.getProperty("user.home"), "OrderWriterDialog.ini")), "");
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

    if (getFaction() != null) {
      pSettings.setProperty(PropertiesHelper.ORDERWRITER_FACTION, getFaction().getID().intValue() + "");
    } else {
      pSettings.setProperty(PropertiesHelper.ORDERWRITER_FACTION, "-1");
    }

    if (type == FILE_PANEL) {
      PropertiesHelper.setList(pSettings, PropertiesHelper.ORDERWRITER_OUTPUT_FILE + suffix, getNewOutputFiles(
          cmbOutputFiles));
      pSettings.setProperty(PropertiesHelper.ORDERWRITER_AUTO_FILENAME + suffix, String.valueOf(chkAutoFileName
          .isSelected()));
    }

    if (chkFixedWidth[type].isSelected() == true) {
      try {
        pSettings.setProperty(PropertiesHelper.ORDERWRITER_FIXED_WIDTH + suffix, Integer.parseInt(txtFixedWidth[type]
            .getText()) + "");
      } catch (NumberFormatException e) {
        pSettings.setProperty(PropertiesHelper.ORDERWRITER_FIXED_WIDTH + suffix, "0");
      }
    } else {
      try {
        pSettings.setProperty(PropertiesHelper.ORDERWRITER_FIXED_WIDTH + suffix, (-1 * Integer.parseInt(
            txtFixedWidth[type].getText())) + "");
      } catch (NumberFormatException e) {
        pSettings.setProperty(PropertiesHelper.ORDERWRITER_FIXED_WIDTH + suffix, "0");
      }
    }

    pSettings.setProperty(PropertiesHelper.ORDERWRITER_ADD_ECHECK_COMMENTS + suffix, String.valueOf(
        chkECheckComments[type].isSelected()));
    pSettings.setProperty(PropertiesHelper.ORDERWRITER_REMOVE_SC_COMMENTS + suffix, String.valueOf(
        chkRemoveSCComments[type].isSelected()));
    pSettings.setProperty(PropertiesHelper.ORDERWRITER_REMOVE_SS_COMMENTS + suffix, String.valueOf(
        chkRemoveSSComments[type].isSelected()));
    pSettings.setProperty(PropertiesHelper.ORDERWRITER_CONFIRMED_ONLY + suffix, String.valueOf(chkConfirmedOnly[type]
        .isSelected()));

    if (chkSelRegionsOnly[type].isEnabled()) {
      pSettings.setProperty(PropertiesHelper.ORDERWRITER_SELECTED_REGIONS + suffix, String.valueOf(
          chkSelRegionsOnly[type].isSelected()));
    }

    pSettings.setProperty(PropertiesHelper.ORDERWRITER_WRITE_TAGS_AS_VORLAGE_COMMENT + suffix, String.valueOf(
        chkWriteUnitTagsAsVorlageComment[type].isSelected()));

    if (type == EMAIL_PANEL) {
      if (chkUseSettingsFromCR.isEnabled()) {
        pSettings.setProperty(PropertiesHelper.ORDERWRITER_MAILSERVER_USE_CR_SETTINGS + suffix, String.valueOf(
            chkUseSettingsFromCR.isSelected()));
      }

      pSettings.setProperty(PropertiesHelper.ORDERWRITER_MAILSERVER_CC2SENDER + suffix, String.valueOf(chkCCToSender
          .isSelected()));

      pSettings.setProperty(PropertiesHelper.ORDERWRITER_MAILSERVER_SSL + suffix, String.valueOf(chkUseSSL
          .isSelected()));
      pSettings.setProperty(PropertiesHelper.ORDERWRITER_MAILSERVER_TLS + suffix, String.valueOf(chkUseTLS
          .isSelected()));

      pSettings.setProperty(PropertiesHelper.ORDERWRITER_MAILSERVER_HOST + suffix, txtMailServer.getText());
      pSettings.setProperty(PropertiesHelper.ORDERWRITER_MAILSERVER_PORT + suffix, txtMailServerPort.getText());

      pSettings.setProperty(PropertiesHelper.ORDERWRITER_MAILSERVER_USEAUTH + suffix, String.valueOf(chkUseAuth
          .isSelected()));
      pSettings.setProperty(PropertiesHelper.ORDERWRITER_MAILSERVER_USERNAME + suffix, txtServerUsername.getText());
      pSettings.setProperty(PropertiesHelper.ORDERWRITER_MAILSERVER_ASKPWD + suffix, String.valueOf(chkAskPassword
          .isSelected()));
      // for security reasons only store password if the user explicitly wants it
      pSettings.setProperty(PropertiesHelper.ORDERWRITER_MAILSERVER_PASSWORD + suffix, "");
      if (!chkAskPassword.isSelected() && chkUseAuth.isSelected()) {
        String pw = new String(txtServerPassword.getPassword());
        try {
          String encrypted = encrypt(pw);
          pSettings.setProperty(PropertiesHelper.ORDERWRITER_MAILSERVER_PASSWORD_ENCRYPTED + suffix, encrypted);
        } catch (Exception e) {
          log.error("Could not encrypt password", e);
          pSettings.setProperty(PropertiesHelper.ORDERWRITER_MAILSERVER_PASSWORD + suffix, pw);
        }
      } else {
        pSettings.setProperty(PropertiesHelper.ORDERWRITER_MAILSERVER_PASSWORD_ENCRYPTED + suffix, "");
      }

      pSettings.setProperty(PropertiesHelper.ORDERWRITER_MAILSERVER_RECIPIENT + suffix, txtMailRecipient.getText());
      pSettings.setProperty(PropertiesHelper.ORDERWRITER_MAILSERVER_SENDER + suffix, txtMailSender.getText());
      pSettings.setProperty(PropertiesHelper.ORDERWRITER_MAILSERVER_SUBJECT + suffix, txtMailSubject.getText());
      pSettings.setProperty(PropertiesHelper.ORDERWRITER_MAILSERVER_RECIPIENT2 + suffix, txtMailRecipient2.getText());
    }

    if (type == SERVER_PANEL) {
      PropertiesHelper.setList(pSettings, PropertiesHelper.ORDERWRITER_SERVER_URL + suffix, getNewOutputFiles(
          cmbServerURLs));
    }
  }

  private String getPassword(Properties pSettings, String suffix) {
    String pw = null;
    if (pSettings.getProperty(PropertiesHelper.ORDERWRITER_MAILSERVER_PASSWORD_ENCRYPTED + suffix) != null) {
      try {
        pw = decrypt(pSettings.getProperty(PropertiesHelper.ORDERWRITER_MAILSERVER_PASSWORD_ENCRYPTED + suffix));
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
  private String encrypt(String plaintext) throws InvalidKeyException, BadPaddingException, IllegalBlockSizeException {
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
  private String decrypt(String ciphertext) throws InvalidKeyException, BadPaddingException, IllegalBlockSizeException,
      InvalidAlgorithmParameterException, IOException {
    cipher.init(Cipher.DECRYPT_MODE, key);// , new IvParameterSpec(iv));

    byte[] raw = Base64.decodeBase64(ciphertext.getBytes());
    byte[] recoveredBytes = cipher.doFinal(raw);
    String recovered = new String(recoveredBytes);
    return recovered;
  }

  private List<String> getNewOutputFiles(JComboBox<String> combo) {
    List<String> ret = new ArrayList<String>(combo.getItemCount() + 1);

    if (combo.getSelectedIndex() == -1) {
      ret.add((String) combo.getEditor().getItem());
    }

    String selected = (String) combo.getSelectedItem();
    if (selected != null) {
      ret.add(selected);
    }

    for (int i = 0; i < Math.min(combo.getItemCount(), 6); i++) {
      String item = combo.getItemAt(i);
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
    fc.setFileFilter(new EresseaFileFilter(EresseaFileFilter.TXT_ORDERS_FILTER));

    if (defaultFile != null) {
      fc.setSelectedFile(new File(defaultFile));
    }
    SwingUtils.setPreferredSize(fc, 50, -1, true);
    SwingUtils.setPreferredSize(fc, localSettings, PropertiesHelper.FILE_CHOOSER_BOUNDS);

    if (fc.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
      retVal = fc.getSelectedFile().getPath();
    }
    PropertiesHelper.saveRectangle(localSettings, fc.getBounds(), PropertiesHelper.FILE_CHOOSER_BOUNDS);

    return retVal;
  }

  private Object[] write(Writer out, boolean forceUnixLineBreaks, Faction faction, int type) {
    return write(out, forceUnixLineBreaks, true, false, faction, type);
  }

  private Object[] write(Writer out, boolean forceUnixLineBreaks, boolean closeStream, boolean confirm, Faction faction,
      int type) {
    if (faction == null)
      return null;
    Object[] result = null;

    setCursor(new Cursor(Cursor.WAIT_CURSOR));

    Writer stream = null;
    try {
      if (chkFixedWidth[type].isSelected()) {
        int fixedWidth = -1;

        try {
          fixedWidth = Integer.parseInt(txtFixedWidth[type].getText());
        } catch (NumberFormatException e) {
          setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
          JOptionPane.showMessageDialog(this, Resources.get("orderwriterdialog.msg.invalidfixedwidth.text"), Resources
              .get("orderwriterdialog.msg.invalidfixedwidth.title"),
              JOptionPane.WARNING_MESSAGE);
          return null;
        }

        stream = new FixedWidthWriter(new BufferedWriter(out), fixedWidth, forceUnixLineBreaks);
      } else {
        stream = out;
      }

      GameSpecificOrderWriter cw = data.getGameSpecificStuff().getOrderWriter();
      cw.setGameData(data);
      cw.setFaction(faction);

      cw.setAddECheckComments(chkECheckComments[type].isSelected());
      cw.setRemoveComments(chkRemoveSCComments[type].isSelected(), chkRemoveSSComments[type].isSelected());
      cw.setConfirmedOnly(chkConfirmedOnly[type].isSelected());
      cw.setWriteUnitTagsAsVorlageComment(chkWriteUnitTagsAsVorlageComment[type].isSelected());

      if (chkSelRegionsOnly[type].isSelected() && (regions != null) && (regions.size() > 0)) {
        cw.setRegions(regions);
      }

      cw.setForceUnixLineBreaks(forceUnixLineBreaks);

      Group group = getGroup();

      if (group != null) {
        cw.setGroup(group);
      }

      int writtenUnits = cw.write(stream);

      int allUnits = 0;

      for (Unit u : data.getUnits()) {
        if (!(u instanceof TempUnit) && faction.equals(u.getFaction())) {
          allUnits++;
        }
      }

      result = new Object[] { writtenUnits, allUnits, faction, cw.getErrors() };
      if (cw.getErrors().size() > 0) {
        StringBuilder errMsg = new StringBuilder(Resources.get("orderwriterdialog.msg.erroronsave.text"));
        int i = 0;
        for (String error : cw.getErrors()) {
          errMsg.append("\n").append(error);
          if (++i > 3) {
            errMsg.append("\n...");
            break;
          }
        }
        JOptionPane.showMessageDialog(this, errMsg, Resources.get("orderwriterdialog.msg.erroronsave.title"),
            JOptionPane.WARNING_MESSAGE);

      }
    } catch (IOException ioe) {
      setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
      JOptionPane.showMessageDialog(this, Resources.get("orderwriterdialog.msg.erroronsave.text") + ioe.toString(),
          Resources.get("orderwriterdialog.msg.erroronsave.title"),
          JOptionPane.WARNING_MESSAGE);

      return null;
    } finally {
      if (closeStream && stream != null) {
        try {
          stream.close();
        } catch (IOException e) {
          // ignore (safe)
        }
      }
      setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
    }
    return result;
  }

  private boolean checkPassword(Faction f) {
    if (f == null || f.getPassword() == null) {
      Object msgArgs[] = { f == null ? "null" : f.toString() };
      JOptionPane.showMessageDialog(getRootPane(), (new java.text.MessageFormat(Resources.get(
          "orderwriterdialog.msg.nopassword.text"))).format(msgArgs));
      return false;
    }
    return true;
  }

  /**
   * Copies the orders to the clipboard using the settings from the properties.
   */
  public boolean runClipboard() {
    init(CLIPBOARD_PANEL);
    return copyToClipboard();
  }

  protected boolean copyToClipboard() {
    Faction faction = getFaction();
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
      getToolkit().getSystemClipboard().setContents(new java.awt.datatransfer.StringSelection(sw.toString()), null);
      JOptionPane.showMessageDialog(this, (new java.text.MessageFormat(Resources.get(
          "orderwriterdialog.msg.writtenunits.text.clipboard"))).format(parameters), Resources.get(
              "orderwriterdialog.msg.writtenunits.title"), JOptionPane.INFORMATION_MESSAGE);
      return true;
    } else
      return false;
  }

  private void updateAutoFileName() {
    String pattern = (String) cmbOutputFiles.getSelectedItem();

    // this global setting is deprecated, using filename as pattern if appropriate now
    // localSettings.getProperty("FileNameGenerator.ordersSaveFileNamePattern");

    if (chkAutoFileName.isSelected()) {
      FileNameGeneratorFeed feed = new FileNameGeneratorFeed(super.getData().getDate().getDate());
      Faction f = getFaction();
      if (f != null) {
        feed.setFaction(f.getName());
        feed.setFactionnr(f.getID().toString());
      }
      Group g = getGroup();
      if (g != null) {
        feed.setGroup(g.getName());
      }

      String newFileName = FileNameGenerator.getFileName(pattern, feed);

      txtOutputFileGenerated.setText(newFileName);
    } else {
      txtOutputFileGenerated.setText(pattern);
    }
  }

  /**
   * Sends the orders using the settings from the properties.
   */
  public boolean runPutOnServer() {
    init(SERVER_PANEL);
    return saveToServer();
  }

  protected boolean saveToServer() {
    Faction faction = getFaction();
    if (faction == null)
      return false;

    if (!checkPassword(faction))
      return false;

    setWaitCursor(true);

    String url = (String) cmbServerURLs.getSelectedItem();

    // check url parameters
    if ("".equals(url)) {
      setWaitCursor(false);
      JOptionPane.showMessageDialog(ancestor, Resources.get("orderwriterdialog.msg.invalidserverurl.text"), Resources
          .get("orderwriterdialog.msg.invalidserverurl.title"), JOptionPane.WARNING_MESSAGE);

      return false;
    }

    URI uri;
    try {
      uri = new URI(url);
    } catch (URISyntaxException e) {
      JOptionPane.showMessageDialog(ancestor, Resources.get("orderwriterdialog.msg.wrongurl.text"), Resources.get(
          "orderwriterdialog.msg.invalidserverurl.title"), JOptionPane.WARNING_MESSAGE);
      return false;
    }

    OrderWriterDialog.log.debug("attempting to send orders to server: " + url + ".");
    sendToServerImpl(uri, faction);

    setWaitCursor(false);

    return true;

  }

  private void sendToServerImpl(URI uri, Faction faction) {
    ProgressBarUI progress = new ProgressBarUI(this);
    progress.setTitle(Resources.get("orderwriterdialog.btn.server.caption"));
    progress.show();

    new Thread(() -> {
      try {
        progress.setProgress("creating orders", -1);
        File orderFile = File.createTempFile("orders", null);

        FileWriter cmds = new FileWriter(orderFile, Charset.forName("UTF-8"));

        final Object[] parameters = write(cmds, false, false, true, faction, SERVER_PANEL);

        progress.setProgress("Sending ...", -1);
        HTTPClient client = new HTTPClient(localSettings);
        OrderWriterDialog.log.info("sending...");
        client.setAuthentication(faction.getID().toString(), faction.getPassword(), uri.getHost(), -1, null, null);

        Part[] parts = {
            new FilePart("input", orderFile)
        };
        HTTPResult result = client.post(uri, parts);

        int status = result == null ? -1 : result.getStatus();

        if (status == 401) {
          log.warn("Authentication failed");
          progress.showMessageDialog(Resources.get("orderwriterdialog.msg.passworderror.text"));
        } else if (status < 200 || status >= 300 || result == null) {
          if (status < 0 || result == null) {
            log.warn("No response from server");
          } else {
            log.warn("Response from server: " + status);
            log.warn("Response from server: " + result.getResultAsString());
          }
          progress.showMessageDialog(Resources.get("orderwriterdialog.msg.servererror.text", status));
        } else {
          String answer = result.getResultAsString();
          log.info(Resources.get("orderwriterdialog.msg.writtenunits.text.server",
              parameters[0], parameters[1], parameters[2], answer));
          progress.showMessageDialog(Resources.get("orderwriterdialog.msg.writtenunits.text.server",
              parameters[0], parameters[1], parameters[2], answer));
        }

      } catch (Exception e) {
        log.warn(e);
        JOptionPane.showMessageDialog(ancestor,
            Resources.get("orderwriterdialog.msg.servererror.text", -2),
            Resources.get("orderwriterdialog.msg.servererror.title"), JOptionPane.ERROR_MESSAGE);
      } finally {
        progress.ready();
      }
    }).start();

  }

  /**
   * Mails the orders using the settings from the properties.
   */
  public boolean runMail() {
    init(EMAIL_PANEL);
    return sendMail();
  }

  protected boolean sendMail() {
    Faction faction = getFaction();
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
      JOptionPane.showMessageDialog(ancestor, Resources.get("orderwriterdialog.msg.invalidfromaddress.text"), Resources
          .get("orderwriterdialog.msg.mailerror.title"), JOptionPane.WARNING_MESSAGE);

      return false;
    }

    if (recipient.equals("")) {
      setWaitCursor(false);
      JOptionPane.showMessageDialog(ancestor, Resources.get("orderwriterdialog.msg.invalidrecipient.text"), Resources
          .get("orderwriterdialog.msg.mailerror.title"), JOptionPane.WARNING_MESSAGE);

      return false;
    }

    if (mailHost.equals("")) {
      setWaitCursor(false);
      JOptionPane.showMessageDialog(ancestor, Resources.get("orderwriterdialog.msg.invalidsmtpserver.text"), Resources
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
      JOptionPane.showMessageDialog(ancestor, Resources.get("orderwriterdialog.msg.invalidsmtpserverport.text"),
          Resources.get("orderwriterdialog.msg.mailerror.title"), JOptionPane.WARNING_MESSAGE);

      return false;
    }

    if (username != null) {
      if (password == null || password.trim().isEmpty()) {
        password = showPasswordDialog();
      }
      if (password == null) {
        setWaitCursor(false);
        return false;
      }
    }

    OrderWriterDialog.log.debug("attempting to send mail: " + mailHost + ", " + port + ", " + username + ", " + password
        + ", " + sender + ", " + recipient + ", " + subject);
    sendMailImpl(mailHost, port, username, password, sender, recipient, subject, recipient2, faction);

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

  private void sendMailImpl(final String mailHost, int port, String username, String password, String sender,
      String recipient, String subject, String cc, Faction faction) {

    final MultiPartEmail mailMessage;
    String contentType = "text/plain; charset=" + Encoding.DEFAULT;
    if (PropertiesHelper.getBoolean(localSettings, "TextEncoding.ISOsaveOrders", false)) {
      // new: force our default = ISO
      contentType = "text/plain; charset=" + Encoding.ISO;
    } else if (PropertiesHelper.getBoolean(localSettings, "TextEncoding.UTF8saveOrders", true)) {
      // new: force our default = UTF-8
      contentType = "text/plain; charset=" + Encoding.UTF8;
    } else {
      // old = default = system dependent
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
        StringTokenizer tokenizer = new StringTokenizer(cc, ",;");
        while (tokenizer.hasMoreTokens()) {
          String aCC = tokenizer.nextToken();
          if (aCC.length() > 0) {
            mailMessage.addCc(aCC);
          }
        }
      }

    } catch (EmailException e) {
      setWaitCursor(false);

      Object msgArgs[] = { mailHost, e.toString() };
      JOptionPane.showMessageDialog(ancestor, (new java.text.MessageFormat(Resources.get(
          "orderwriterdialog.msg.smtpserverunreachable.text"))).format(msgArgs), Resources.get(
              "orderwriterdialog.msg.mailerror.title"), JOptionPane.WARNING_MESSAGE);

      if (OrderWriterDialog.log.isDebugEnabled()) {
        OrderWriterDialog.log.debug(e);
      }

      return;
    }

    mailMessage.setSSLOnConnect(chkUseSSL.isSelected());
    mailMessage.setStartTLSEnabled(chkUseTLS.isSelected());

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
          JOptionPane.showMessageDialog(ancestor, (new java.text.MessageFormat(Resources.get(
              "orderwriterdialog.msg.writtenunits.text"))).format(parameters), Resources.get(
                  "orderwriterdialog.msg.writtenunits.title"), JOptionPane.INFORMATION_MESSAGE);

        } catch (EmailException e) {
          OrderWriterDialog.log.info("exception while sending message", e);

          Object msgArgs[] = { e.toString(), e.getCause() == null ? "" : e.getCause().getMessage() };
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
    JLabel passwdLabel = new JLabel(title);
    final JPasswordField passwd = new JPasswordField(30);
    boolean[] gainedFocusBefore = new boolean[] { false };
    JPanel panel = new JPanel();
    panel.add(passwdLabel);
    panel.add(passwd);

    JOptionPane op = new JOptionPane(panel, JOptionPane.QUESTION_MESSAGE, JOptionPane.OK_CANCEL_OPTION);

    JDialog dlg = op.createDialog(ancestor, title);

    // Wire up FocusListener to ensure JPasswordField is able to request focus when the dialog is first shown.
    dlg.addWindowFocusListener(new WindowAdapter() {
      @Override
      public void windowGainedFocus(WindowEvent e) {
        if (!gainedFocusBefore[0]) {
          gainedFocusBefore[0] = true;
          passwd.requestFocusInWindow();
        }
      }
    });

    dlg.setVisible(true);

    if (op.getValue() != null && op.getValue().equals(JOptionPane.OK_OPTION))
      return new String(passwd.getPassword());
    else
      return null;
  }

  /**
   * Saves the orders to a file using the settings from the properties.
   */
  public boolean runSave() {
    init(FILE_PANEL);
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
        JOptionPane.showMessageDialog(this, (new java.text.MessageFormat(Resources.get(
            "orderwriterdialog.msg.writtenunits.text.file"))).format(new Object[] { parameters[0], parameters[1], ""
                + parameters[2] + "/" + parameters[3], outputFile }), Resources.get(
                    "orderwriterdialog.msg.writtenunits.title"), JOptionPane.INFORMATION_MESSAGE);

        stream.close();
      }
    } catch (IOException ioe) {
      JOptionPane.showMessageDialog(this, Resources.getFormatted("orderwriterdialog.msg.writeerror.text", outputFile
          .toString(), ioe), Resources.get("orderwriterdialog.msg.writeerror.title"),
          JOptionPane.WARNING_MESSAGE);
    }
    return result;
  }

  private String getOutputFileName() {
    // updateAutoFileName();
    return txtOutputFileGenerated.getText();
  }

  protected boolean saveToFile() {
    Faction faction = getFaction();
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
        JOptionPane.showMessageDialog(this, (new java.text.MessageFormat(Resources.get(
            "orderwriterdialog.msg.writtenunits.text.file"))).format(new Object[] { parameters[0], parameters[1],
                parameters[2], outputFile }), Resources.get("orderwriterdialog.msg.writtenunits.title"),
            JOptionPane.INFORMATION_MESSAGE);
      }

      return parameters != null;

    } catch (IOException ioe) {
      JOptionPane.showMessageDialog(this, Resources.getFormatted("orderwriterdialog.msg.writeerror.text", outputFile
          .toString(), ioe), Resources.get("orderwriterdialog.msg.writeerror.title"),
          JOptionPane.WARNING_MESSAGE);
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
    } else if (PropertiesHelper.getBoolean(localSettings, "TextEncoding.UTF8saveOrders", true)) {
      // new: force our default = UTF-8 TextEncoding.UTF8saveOrders
      stream = new OutputStreamWriter(new FileOutputStream(outputFile), Encoding.UTF8.toString());
    } else {
      // old = default = system dependent
      stream = new FileWriter(outputFile);
    }
    return stream;
  }

  /**
   * Returns true if the dialog can be show for the given data, i.e., if it has privileged factions.
   */
  public static boolean canShow(GameData data) {
    Faction faction = null;
    for (Faction f : data.getFactions()) {
      if (TrustLevels.isPrivileged(f)) {
        faction = f;
        break;
      }
    }
    return faction != null;
  }

  private boolean isGmEnabled() {
    return PropertiesHelper.getBoolean(localSettings, "GM.enabled", false);
  }

}
