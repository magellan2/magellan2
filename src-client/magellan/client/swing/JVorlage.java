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
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.LineNumberReader;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;
import java.util.StringTokenizer;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import javax.swing.filechooser.FileFilter;

import magellan.client.Client;
import magellan.client.swing.layout.WrappableLabel;
import magellan.client.utils.SwingUtils;
import magellan.library.utils.Resources;
import magellan.library.utils.logging.Logger;

/**
 * A GUI for Vorlage by Steffen Schuemann (Gulrak). See <A HREF="http://www.gulrak.de/">Gulrak's
 * Homepage</A> for details on Vorlage. This class can be used as a stand-alone application or can
 * be integrated as dialog into a different application.
 */
public class JVorlage extends InternationalizedDialog {
  private static final Logger log = Logger.getInstance(JVorlage.class);
  private Properties settings = null;
  private boolean standAlone = false;
  private JComboBox comboInputFile = null;
  private JComboBox comboOutputFile = null;
  private JComboBox comboScriptFile = null;
  private JComboBox comboScriptDirectory = null;
  private JTextField txtVorlageFile = null;
  private JCheckBox chkOptionCR = null;
  private JTextField txtOptions = null;
  private JTextArea txtOutput = null;
  private Container txtInfo;

  /**
   * Create a stand-alone instance of JVorlage.
   */
  public JVorlage() {
    super((Frame) null, false);
    standAlone = true;

    try {
      settings.load(new FileInputStream(new File(System.getProperty("user.home"), "JVorlage.ini")));
    } catch (IOException e) {
      JVorlage.log.error("JVorlage.JVorlage()", e);
    }

    init();
  }

  /**
   * Create a new JVorlage object as a dialog with a parent window.
   */
  public JVorlage(Frame owner, boolean modal, Properties p) {
    super(owner, modal);
    settings = p;
    init();
  }

  private void init() {
    setContentPane(getMainPane());
    setTitle(Resources.get("jvorlage.window.title"));

    pack();
    if (settings.getProperty("JVorlage.width") != null) {
      int width = Integer.parseInt(settings.getProperty("JVorlage.width", "450"));
      int height = Integer.parseInt(settings.getProperty("JVorlage.height", "310"));
      setSize(width, height);
    }

    SwingUtils.setLocation(this, settings, "JVorlage.x", "JVorlage.y");
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
    c.fill = GridBagConstraints.BOTH;
    c.weightx = 0.01;
    c.weighty = 0.1;
    c.insets = new Insets(5, 5, 5, 5);
    mainPanel.add(getInfoPanel(), c);

    c.anchor = GridBagConstraints.NORTH;
    c.gridx = 1;
    c.gridy = 0;
    c.gridwidth = 1;
    c.gridheight = 2;
    c.fill = GridBagConstraints.NONE;
    c.weightx = 0.0;
    c.weighty = 0.0;
    c.insets = new Insets(0, 0, 0, 0);
    mainPanel.add(getButtonPanel(), c);

    c.anchor = GridBagConstraints.CENTER;
    c.gridx = 0;
    c.gridy = 1;
    c.gridwidth = 1;
    c.gridheight = 1;
    c.fill = GridBagConstraints.HORIZONTAL;
    c.weightx = 0.1;
    c.weighty = 0.0;
    c.insets = new Insets(0, 0, 0, 5);
    mainPanel.add(getFilePanel(), c);

    c.anchor = GridBagConstraints.CENTER;
    c.gridx = 0;
    c.gridy = 2;
    c.gridwidth = 2;
    c.gridheight = 1;
    c.fill = GridBagConstraints.HORIZONTAL;
    c.weightx = 0.1;
    c.weighty = 0.0;
    c.insets = new Insets(0, 0, 0, 0);
    mainPanel.add(getOptionPanel(), c);

    c.anchor = GridBagConstraints.CENTER;
    c.gridx = 0;
    c.gridy = 3;
    c.gridwidth = 2;
    c.gridheight = 1;
    c.fill = GridBagConstraints.BOTH;
    c.weightx = 0.2;
    c.weighty = 0.2;
    c.insets = new Insets(0, 0, 0, 0);
    mainPanel.add(getOutputPanel(), c);

    return mainPanel;
  }

  private Container getInfoPanel() {
    txtInfo = WrappableLabel.getLabel(Resources.get("jvorlage.txt.info.text")).getComponent();

    return txtInfo;
  }

  private Container getButtonPanel() {
    JButton okButton = new JButton(Resources.get("jvorlage.btn.ok.caption"));
    okButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        execUsersVorlage();

      }
    });

    JButton cancelButton = new JButton(Resources.get("jvorlage.btn.close.caption"));
    cancelButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        quit();
      }
    });

    JPanel buttonPanel = new JPanel(new GridLayout(2, 1, 5, 5));
    buttonPanel.add(okButton);
    buttonPanel.add(cancelButton);

    return buttonPanel;
  }

  private Container getFilePanel() {
    comboInputFile =
        new JComboBox(getList(settings.getProperty("JVorlage.inputFile", "")).toArray());
    comboInputFile.setEditable(true);
    comboInputFile.setToolTipText(Resources.get("jvorlage.combo.inputfile.tooltip"));

    JLabel lblInputFile = new JLabel(Resources.get("jvorlage.lbl.inputfile.caption"));
    lblInputFile.setLabelFor(comboInputFile);
    lblInputFile.setToolTipText(comboInputFile.getToolTipText());

    JButton btnInputFile = new JButton("...");
    btnInputFile.setToolTipText(Resources.get("jvorlage.btn.inputfile.tooltip"));
    btnInputFile.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        String inputFile =
            getFileName((String) comboInputFile.getSelectedItem(), true, new EresseaFileFilter(
                EresseaFileFilter.CR_FILTER), false);

        if (inputFile != null) {
          comboInputFile.addItem(inputFile);
          comboInputFile.setSelectedItem(inputFile);
        }
      }
    });

    comboOutputFile =
        new JComboBox(getList(settings.getProperty("JVorlage.outputFile", "")).toArray());
    comboOutputFile.setEditable(true);
    comboOutputFile.setToolTipText(Resources.get("jvorlage.combo.outputfile.tooltip"));

    JLabel lblOutputFile = new JLabel(Resources.get("jvorlage.lbl.outputfile.caption"));
    lblOutputFile.setLabelFor(comboOutputFile);
    lblOutputFile.setToolTipText(comboOutputFile.getToolTipText());

    JButton btnOutputFile = new JButton("...");
    btnOutputFile.setToolTipText(Resources.get("jvorlage.btn.outputfile.tooltip"));
    btnOutputFile.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        String outputFile =
            getFileName((String) comboOutputFile.getSelectedItem(), false, new EresseaFileFilter(
                EresseaFileFilter.CR_FILTER), false);

        if (outputFile != null) {
          comboOutputFile.addItem(outputFile);
          comboOutputFile.setSelectedItem(outputFile);
        }
      }
    });

    comboScriptFile =
        new JComboBox(getList(settings.getProperty("JVorlage.scriptFile", "")).toArray());
    comboScriptFile.setEditable(true);
    comboScriptFile.setToolTipText(Resources.get("jvorlage.combo.scriptfile.tooltip"));

    JLabel lblScriptFile = new JLabel(Resources.get("jvorlage.lbl.scriptfile.caption"));
    lblScriptFile.setLabelFor(comboScriptFile);
    lblScriptFile.setToolTipText(comboScriptFile.getToolTipText());

    JButton btnScriptFile = new JButton("...");
    btnScriptFile.setToolTipText(Resources.get("jvorlage.btn.scriptfile.tooltip"));
    btnScriptFile.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        List<String> extensions = new LinkedList<String>();
        extensions.add(".txt");
        extensions.add(".vms");
        String scriptFile =
            getFileName((String) comboScriptFile.getSelectedItem(), false, new EresseaFileFilter(
                extensions, Resources.get("JVorlage.filefilter.script.description")), false);

        if (scriptFile != null && new File(scriptFile).isFile()) {
          comboScriptFile.addItem(scriptFile);
          comboScriptFile.setSelectedItem(scriptFile);
        }
      }
    });

    comboScriptDirectory =
        new JComboBox(getList(settings.getProperty("JVorlage.scriptdirectory", "")).toArray());
    comboScriptDirectory.setEditable(true);
    comboScriptDirectory.setToolTipText(Resources.get("jvorlage.combo.scriptdirectory.tooltip"));

    JLabel lblScriptDirectory = new JLabel(Resources.get("jvorlage.lbl.scriptdirectory.caption"));
    lblScriptDirectory.setLabelFor(comboScriptDirectory);
    lblScriptDirectory.setToolTipText(comboScriptDirectory.getToolTipText());

    JButton btnScriptDirectory = new JButton("...");
    btnScriptDirectory.setToolTipText(Resources.get("jvorlage.btn.scriptdirectory.tooltip"));
    btnScriptDirectory.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        String scriptDirectory =
            getFileName((String) comboScriptDirectory.getSelectedItem(), false, null, true);

        if (scriptDirectory != null && new File(scriptDirectory).isDirectory()) {
          comboScriptDirectory.addItem(scriptDirectory);
          comboScriptDirectory.setSelectedItem(scriptDirectory);
        }
      }
    });

    txtVorlageFile = new JTextField(settings.getProperty("JVorlage.vorlageFile", ""));
    txtVorlageFile.setEditable(false);
    txtVorlageFile.setToolTipText(Resources.get("jvorlage.txt.vorlagefile.tooltip"));

    JLabel lblVorlageFile = new JLabel(Resources.get("jvorlage.lbl.vorlagefile.caption"));
    lblVorlageFile.setLabelFor(txtVorlageFile);
    lblVorlageFile.setToolTipText(txtVorlageFile.getToolTipText());
    //
    // JButton btnVorlageFile = new JButton("...");
    // btnVorlageFile.setToolTipText(Resources.get("jvorlage.btn.vorlagefile.tooltip"));
    // btnVorlageFile.addActionListener(new ActionListener() {
    // public void actionPerformed(ActionEvent e) {
    // String vorlageFile = getFileName(txtVorlageFile.getText(), false,
    // new EresseaFileFilter("exe",
    // Resources.get("jvorlage.filter.executable.description")));
    //
    // if(vorlageFile != null) {
    // txtVorlageFile.setText(vorlageFile);
    // }
    // }
    // });

    JPanel pnlFiles = new JPanel(new GridBagLayout());
    pnlFiles.setBorder(new TitledBorder(BorderFactory.createEtchedBorder(), Resources
        .get("jvorlage.border.files")));

    GridBagConstraints c = new GridBagConstraints();

    // inputFile
    c.anchor = GridBagConstraints.WEST;
    c.gridx = 0;
    c.gridy = 0;
    c.gridwidth = 1;
    c.gridheight = 1;
    c.fill = GridBagConstraints.NONE;
    c.weightx = 0.0;
    c.weighty = 0.0;
    pnlFiles.add(lblInputFile, c);
    c.anchor = GridBagConstraints.CENTER;
    c.gridx = 1;
    c.gridy = 0;
    c.gridwidth = 1;
    c.gridheight = 1;
    c.fill = GridBagConstraints.HORIZONTAL;
    c.weightx = 0.1;
    c.weighty = 0.0;
    pnlFiles.add(comboInputFile, c);
    c.anchor = GridBagConstraints.CENTER;
    c.gridx = 2;
    c.gridy = 0;
    c.gridwidth = 1;
    c.gridheight = 1;
    c.fill = GridBagConstraints.NONE;
    c.weightx = 0.0;
    c.weighty = 0.0;
    pnlFiles.add(btnInputFile, c);

    // outputFile
    c.anchor = GridBagConstraints.WEST;
    c.gridx = 0;
    c.gridy++;
    c.gridwidth = 1;
    c.gridheight = 1;
    c.fill = GridBagConstraints.NONE;
    c.weightx = 0.0;
    c.weighty = 0.0;
    pnlFiles.add(lblOutputFile, c);
    c.anchor = GridBagConstraints.CENTER;
    c.gridx = 1;
    // c.gridy = 1;
    c.gridwidth = 1;
    c.gridheight = 1;
    c.fill = GridBagConstraints.HORIZONTAL;
    c.weightx = 0.1;
    c.weighty = 0.0;
    pnlFiles.add(comboOutputFile, c);
    c.anchor = GridBagConstraints.CENTER;
    c.gridx = 2;
    // c.gridy = 1;
    c.gridwidth = 1;
    c.gridheight = 1;
    c.fill = GridBagConstraints.NONE;
    c.weightx = 0.0;
    c.weighty = 0.0;
    pnlFiles.add(btnOutputFile, c);

    // scriptFile
    c.anchor = GridBagConstraints.WEST;
    c.gridx = 0;
    c.gridy++;
    c.gridwidth = 1;
    c.gridheight = 1;
    c.fill = GridBagConstraints.NONE;
    c.weightx = 0.0;
    c.weighty = 0.0;
    pnlFiles.add(lblScriptFile, c);
    c.anchor = GridBagConstraints.CENTER;
    c.gridx = 1;
    // c.gridy = 2;
    c.gridwidth = 1;
    c.gridheight = 1;
    c.fill = GridBagConstraints.HORIZONTAL;
    c.weightx = 0.1;
    c.weighty = 0.0;
    pnlFiles.add(comboScriptFile, c);
    c.anchor = GridBagConstraints.CENTER;
    c.gridx = 2;
    // c.gridy = 2;
    c.gridwidth = 1;
    c.gridheight = 1;
    c.fill = GridBagConstraints.NONE;
    c.weightx = 0.0;
    c.weighty = 0.0;
    pnlFiles.add(btnScriptFile, c);

    // scriptDirectory
    c.anchor = GridBagConstraints.WEST;
    c.gridx = 0;
    c.gridy++;
    c.gridwidth = 1;
    c.gridheight = 1;
    c.fill = GridBagConstraints.NONE;
    c.weightx = 0.0;
    c.weighty = 0.0;
    pnlFiles.add(lblScriptDirectory, c);
    c.anchor = GridBagConstraints.CENTER;
    c.gridx = 1;
    // c.gridy = 2;
    c.gridwidth = 1;
    c.gridheight = 1;
    c.fill = GridBagConstraints.HORIZONTAL;
    c.weightx = 0.1;
    c.weighty = 0.0;
    pnlFiles.add(comboScriptDirectory, c);
    c.anchor = GridBagConstraints.CENTER;
    c.gridx = 2;
    // c.gridy = 2;
    c.gridwidth = 1;
    c.gridheight = 1;
    c.fill = GridBagConstraints.NONE;
    c.weightx = 0.0;
    c.weighty = 0.0;
    pnlFiles.add(btnScriptDirectory, c);

    // vorlageFile
    c.anchor = GridBagConstraints.WEST;
    c.gridx = 0;
    c.gridy++;
    c.gridwidth = 1;
    c.gridheight = 1;
    c.fill = GridBagConstraints.NONE;
    c.weightx = 0.0;
    c.weighty = 0.0;
    pnlFiles.add(lblVorlageFile, c);
    c.anchor = GridBagConstraints.CENTER;
    c.gridx = 1;
    // c.gridy = 3;
    c.gridwidth = 1;
    c.gridheight = 1;
    c.fill = GridBagConstraints.HORIZONTAL;
    c.weightx = 0.1;
    c.weighty = 0.0;
    pnlFiles.add(txtVorlageFile, c);

    return pnlFiles;
  }

  private Container getOptionPanel() {
    chkOptionCR =
        new JCheckBox(Resources.get("jvorlage.chk.outputcr.caption"), (Boolean.valueOf(settings
            .getProperty("JVorlage.optionCR", "false"))).booleanValue());
    chkOptionCR.setToolTipText(Resources.get("jvorlage.chk.outputcr.tooltip"));

    txtOptions = new JTextField(settings.getProperty("JVorlage.options", ""));
    txtOptions.setToolTipText(Resources.get("jvorlage.txt.options.tooltip"));

    JLabel lblOptions = new JLabel(Resources.get("jvorlage.lbl.options.caption"));
    lblOptions.setToolTipText(txtOptions.getToolTipText());
    lblOptions.setLabelFor(txtOptions);

    JPanel pnlOptions = new JPanel(new GridBagLayout());
    pnlOptions.setBorder(new TitledBorder(BorderFactory.createEtchedBorder(), Resources
        .get("jvorlage.border.options")));

    GridBagConstraints c = new GridBagConstraints();
    c.anchor = GridBagConstraints.WEST;
    c.gridx = 0;
    c.gridy = 0;
    c.gridwidth = 2;
    c.gridheight = 1;
    c.fill = GridBagConstraints.NONE;
    c.weightx = 0.0;
    c.weighty = 0.0;
    pnlOptions.add(chkOptionCR, c);
    c.anchor = GridBagConstraints.WEST;
    c.gridx = 0;
    c.gridy = 1;
    c.gridwidth = 1;
    c.gridheight = 1;
    c.fill = GridBagConstraints.NONE;
    c.weightx = 0.0;
    c.weighty = 0.0;
    pnlOptions.add(lblOptions, c);
    c.anchor = GridBagConstraints.CENTER;
    c.gridx = 1;
    c.gridy = 1;
    c.gridwidth = 1;
    c.gridheight = 1;
    c.fill = GridBagConstraints.HORIZONTAL;
    c.weightx = 0.1;
    c.weighty = 0.0;
    pnlOptions.add(txtOptions, c);

    return pnlOptions;
  }

  private Container getOutputPanel() {
    /*
     * JLabel lblVersion = new JLabel("Version: unbekannt"); JButton btnShowArgs = new
     * JButton("Hilfe anzeigen"); btnShowArgs.addActionListener(new ActionListener() { public void
     * actionPerformed(ActionEvent e) { StringBuffer sb = execVorlage("-?"); if (sb != null) {
     * txtOutput.setText(sb.toString()); } } });
     */
    txtOutput = new JTextArea("\n\n");
    txtOutput.setBorder(BorderFactory.createEtchedBorder());

    JPanel pnlOutput = new JPanel(new GridBagLayout());
    pnlOutput.setBorder(new TitledBorder(BorderFactory.createEtchedBorder(), Resources
        .get("jvorlage.border.output")));

    GridBagConstraints c = new GridBagConstraints();

    c.anchor = GridBagConstraints.CENTER;
    c.gridx = 0;
    c.gridy = 0;
    c.gridwidth = 2;
    c.gridheight = 1;
    c.fill = GridBagConstraints.BOTH;
    c.weightx = 0.1;
    c.weighty = 0.1;
    c.insets = new Insets(1, 1, 1, 1);
    pnlOutput.add(new JScrollPane(txtOutput), c);
    pnlOutput.setPreferredSize(new Dimension(200, 200));

    return pnlOutput;
  }

  /**
   * Stores all properties of JVorlage that should be preserved to the global Properties object. In
   * standalone mode this also saves this object to the file 'JVorlage.ini' in the user's home
   * directory.
   */
  private void storeSettings() {
    settings.setProperty("JVorlage.width", getWidth() + "");
    settings.setProperty("JVorlage.height", getHeight() + "");
    settings.setProperty("JVorlage.x", getX() + "");
    settings.setProperty("JVorlage.y", getY() + "");

    settings.setProperty("JVorlage.inputFile", getString(comboInputFile));
    settings.setProperty("JVorlage.outputFile", getString(comboOutputFile));
    settings.setProperty("JVorlage.scriptFile", getString(comboScriptFile));
    settings.setProperty("JVorlage.scriptDirectory", getString(comboScriptDirectory));
    // settings.setProperty("JVorlage.vorlageFile", txtVorlageFile.getText());

    settings.setProperty("JVorlage.optionCR", String.valueOf(chkOptionCR.isSelected()));
    settings.setProperty("JVorlage.options", txtOptions.getText());

    if (standAlone == true) {
      try {
        settings.store(new FileOutputStream(new File(System.getProperty("user.home"),
            "JVorlage.ini")), "");
      } catch (IOException e) {
        JVorlage.log.error("JVorlage.storeSettings()", e);
      }
    }
  }

  /**
   * Executes Vorlage with the specified options and returns the output Vorlage produced.
   */
  private StringBuffer execVorlage(String options) {
    String commandLine = null;
    StringBuffer sb = null;
    FileReader reader = null;
    File tempFile = null;
    File vorlage = new File(settings.getProperty("JVorlage.vorlageFile", "vorlage.exe"));

    if ((vorlage.exists() == false) || (vorlage.canRead() == false)) {
      JOptionPane.showMessageDialog(this, Resources.get("jvorlage.msg.invalidvorlage.text"),
          Resources.get("jvorlage.msg.invalidvorlage.title"), JOptionPane.ERROR_MESSAGE);

      return sb;
    }

    commandLine = vorlage.getAbsolutePath();

    try {
      tempFile = File.createTempFile("JVorlage", null);
    } catch (Exception e) {
      JVorlage.log.error(
          "JVorlage.execVorlage(): unable to create temporary file for Vorlage output", e);
      JOptionPane.showMessageDialog(this, Resources.get("jvorlage.msg.tempfileerror.text"),
          Resources.get("jvorlage.msg.tempfileerror.title"), JOptionPane.ERROR_MESSAGE);
    }

    if (tempFile != null) {
      commandLine +=
          (" -e " + tempFile.getAbsolutePath() + " -do " + tempFile.getAbsolutePath() + " -to " + tempFile
              .getAbsolutePath());
    }

    {

      commandLine += " ";
      commandLine += options;

      Process p = null;
      long start = System.currentTimeMillis();

      try {
        setCursor(new Cursor(Cursor.WAIT_CURSOR));
        File dir = null;
        if (comboScriptDirectory.getSelectedItem() != null) {
          dir = new File((String) comboScriptDirectory.getSelectedItem());
        }
        if (dir != null && dir.isDirectory()) {
          p = Runtime.getRuntime().exec(commandLine, null, dir);
        } else {
          p = Runtime.getRuntime().exec(commandLine);
        }

      } catch (Exception e) {
        JVorlage.log.error("JVorlage.execVorlage()", e);
      }

      if (p != null) {
        while (true) {
          try {
            Thread.sleep(300);
          } catch (InterruptedException e) {
            // do nothing
          }

          if ((System.currentTimeMillis() - start) > 10000) {
            if (JOptionPane.showConfirmDialog(this, Resources.get("jvorlage.msg.stopvorlage.text"),
                Resources.get("jvorlage.msg.stopvorlage.title"), JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
              p.destroy();

              break;
            }
            start = System.currentTimeMillis();
          }

          try {
            if (p.exitValue() != 0) {
              Object msgArgs[] = { Integer.valueOf(p.exitValue()) };
              JOptionPane.showMessageDialog(this, (new java.text.MessageFormat(Resources
                  .get("jvorlage.msg.execerror.text"))).format(msgArgs), Resources
                      .get("jvorlage.msg.execerror.title"), JOptionPane.WARNING_MESSAGE);
            }

            break;
          } catch (IllegalThreadStateException e) {
          }
        }
      }
    }
    setCursor(new Cursor(Cursor.DEFAULT_CURSOR));

    if (tempFile != null) {
      try {
        reader = new FileReader(tempFile);
      } catch (FileNotFoundException e) {
        JVorlage.log.error(
            "JVorlage.execVorlage(): cannot create a file reader on the temp output file", e);
      }
    }

    if (reader != null) {
      try {
        sb = new StringBuffer();

        String line = null;
        LineNumberReader lnr = new LineNumberReader(reader);

        while ((line = lnr.readLine()) != null) {
          sb.append(line).append("\n");
        }

        lnr.close();
      } catch (IOException e) {
        JVorlage.log.error("JVorlage.execVorlage(): unable to read the temporary output file: ", e);
        sb = null;
      }

      try {
        reader.close();
      } catch (IOException e) {
        log.error("error closing temp file", e);
        // let's hope for the best...
      }
    }

    if (tempFile != null) {
      tempFile.delete();
    }

    return sb;
  }

  /**
   * Assembles a set of options for Vorlage from the user's input in the GUI elements and executes
   * Vorlage, writing its output to the output text area.
   */
  private void execUsersVorlage() {
    String commandLine = "";

    if (!"".equals(txtOptions.getText())) {
      commandLine += (" " + txtOptions.getText());
    }

    if (chkOptionCR.isSelected() == true) {
      commandLine += " -cr";
    }

    if ((comboScriptFile.getSelectedItem() != null)
        && (comboScriptFile.getSelectedItem().equals("") == false)) {
      commandLine += (" -i " + (String) comboScriptFile.getSelectedItem());
    }

    commandLine += (" -f -o " + (String) comboOutputFile.getSelectedItem());
    commandLine += (" " + (String) comboInputFile.getSelectedItem());

    StringBuffer sb = execVorlage(commandLine);

    if (sb != null) {
      txtOutput.setText(sb.toString());
    }
  }

  /**
   * Converts the elements in the specified combo box to one String seperating the string
   * representation of the items by '|' characters.
   */
  private String getString(JComboBox combo) {
    StringBuffer sb = new StringBuffer();

    if (combo.getSelectedIndex() == -1) {
      sb.append((String) combo.getEditor().getItem()).append("|");
    }

    for (int i = 0; i < Math.min(combo.getItemCount(), 6); i++) {
      if (i > 0) {
        sb.append("|");
      }

      sb.append((String) combo.getItemAt(i));
    }

    return sb.toString();
  }

  /**
   * Takes a String containing substrings separated by '|' characters and assembles a list from
   * that.
   */
  private List<String> getList(String str) {
    List<String> retVal = new LinkedList<String>();
    StringTokenizer t = new StringTokenizer(str, "|");

    while (t.hasMoreTokens() == true) {
      retVal.add(t.nextToken());
    }

    return retVal;
  }

  @Override
  protected void quit() {
    storeSettings();

    if (standAlone == true) {
      System.exit(0);
    } else {
      dispose();
    }
  }

  /**
   * Returns a a file name by showing the user a file selection dialog. If the user's selection is
   * empty, null is returned.
   * 
   * @param directories
   */
  private String getFileName(String defaultFile, boolean multSel, FileFilter ff, boolean directories) {
    StringBuffer sb = new StringBuffer();
    File files[] = null;

    JFileChooser fc = new JFileChooser();
    if (ff != null) {
      fc.addChoosableFileFilter(ff);
      fc.setFileFilter(ff);
    }
    fc.setMultiSelectionEnabled(multSel);
    if (directories) {
      fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
    }

    if (defaultFile != null) {
      fc.setSelectedFile(new File(defaultFile));
    }

    if (fc.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
      if (multSel) {
        files = fc.getSelectedFiles();
      } else {
        files = new File[1];
        files[0] = fc.getSelectedFile();
      }
    }

    if (files != null) {
      for (int i = 0; i < files.length; i++) {
        if (files[i].getAbsolutePath().indexOf(' ') > -1) {
          sb.append("\"").append(files[i].getAbsolutePath()).append("\"");
        } else {
          sb.append(files[i].getAbsolutePath());
        }

        if (i < (files.length - 1)) {
          sb.append(" ");
        }
      }
    }

    return sb.toString();
  }

  /**
   * Main method for standalone app.
   * 
   * @throws IOException If an I/O error occurred.
   */
  public static void main(String args[]) throws IOException {
    Properties settings = new Properties();
    settings.load(new FileInputStream(Client.SETTINGS_FILENAME));
    Resources.getInstance();

    JVorlage v = new JVorlage();
    v.setVisible(true);
  }
}
