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
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.SpringLayout;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;

import magellan.client.Client;
import magellan.client.swing.basics.SpringUtilities;
import magellan.client.swing.layout.GridLayout2;
import magellan.library.Alliance;
import magellan.library.Battle;
import magellan.library.Building;
import magellan.library.CoordinateID;
import magellan.library.Faction;
import magellan.library.GameData;
import magellan.library.Group;
import magellan.library.ID;
import magellan.library.Item;
import magellan.library.Message;
import magellan.library.Potion;
import magellan.library.Region;
import magellan.library.RegionResource;
import magellan.library.Ship;
import magellan.library.Skill;
import magellan.library.Spell;
import magellan.library.Unit;
import magellan.library.UnitID;
import magellan.library.io.cr.CRWriter;
import magellan.library.io.file.FileBackup;
import magellan.library.io.file.FileTypeFactory;
import magellan.library.utils.MemoryManagment;
import magellan.library.utils.PropertiesHelper;
import magellan.library.utils.Resources;
import magellan.library.utils.Translations;
import magellan.library.utils.logging.Logger;

/**
 * A GUI for writing a CR to a file or copy it to the clipboard. This class can
 * be used as a stand-alone application or can be integrated as dialog into a
 * different application.
 */
public class CRWriterDialog extends InternationalizedDataDialog {
  private static final Logger log = Logger.getInstance(CRWriterDialog.class);
  private boolean standAlone = false;
  private Collection<Region> regions = null;
  private JComboBox comboOutputFile = null;
  private JCheckBox chkServerConformance = null;
  private JCheckBox chkIslands = null;
  private JCheckBox chkRegions = null;
  private JCheckBox chkRegionDetails = null;
  private JCheckBox chkBuildings = null;
  private JCheckBox chkShips = null;
  private JCheckBox chkUnits = null;
  private JCheckBox chkUnitDetails = null;
  private JCheckBox chkSkills = null;
  private JCheckBox chkOrders = null;
  private JCheckBox chkItems = null;
  private JCheckBox chkMessages = null;
  private JCheckBox chkSpellsAndPotions = null;
  private JCheckBox chkSelRegionsOnly = null;
  private JCheckBox chkDelStats = null;
  private JCheckBox chkDelTrans = null;
  private JCheckBox chkDelEmptyFactions = null;
  private JCheckBox chkExportHotspots = null;

  private GameData data = null;

  /**
   * Create a stand-alone instance of CRWriterDialog.
   */
  public CRWriterDialog(GameData data) {
    super(null, false, null, data, new Properties());
    standAlone = true;
    this.data = data;
    try {
      settings.load(new FileInputStream(new File(System.getProperty("user.home"),
          "CRWriterDialog.ini")));
    } catch (IOException e) {
      CRWriterDialog.log.error("CRWriterDialog.CRWriterDialog()", e);
    }

    init();
  }

  /**
   * Create a new CRWriterDialog object as a dialog with a parent window.
   */
  public CRWriterDialog(Frame owner, boolean modal, GameData initData, Properties p) {
    super(owner, modal, null, initData, p);
    this.data = initData;
    init();
  }

  /**
   * Create a new CRWriterDialog object as a dialog with a parent window and a
   * set of selected regions.
   */
  public CRWriterDialog(Frame owner, boolean modal, GameData initData, Properties p,
      Collection<Region> selectedRegions) {
    super(owner, modal, null, initData, p);
    this.regions = selectedRegions;
    this.data = initData;
    init();
  }

  private void init() {
    setContentPane(getMainPane());
    setTitle(Resources.get("crwriterdialog.window.title"));
    pack();

    Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
    int x =
        Integer.parseInt(settings.getProperty("CRWriterDialog.x", ((screen.width - getWidth()) / 2)
            + ""));
    int y =
        Integer.parseInt(settings.getProperty("CRWriterDialog.y",
            ((screen.height - getHeight()) / 2) + ""));
    setLocation(x, y);
  }

  private Container getMainPane() {
    JPanel mainPanel = new JPanel(new GridBagLayout());
    GridBagConstraints c = new GridBagConstraints();
    mainPanel.setBorder(new EmptyBorder(4, 4, 4, 4));

    c.anchor = GridBagConstraints.CENTER;
    c.gridx = 0;
    c.gridy = 0;
    c.gridwidth = 2;
    c.gridheight = 1;
    c.fill = GridBagConstraints.HORIZONTAL;
    c.weightx = 0.1;
    c.weighty = 0.0;
    Container fp = getFilePanel(); 
    mainPanel.add(fp, c);

    c.anchor = GridBagConstraints.NORTH;
    c.gridx = 0;
    c.gridy = 1;
    c.gridwidth = 1;
    c.gridheight = 1;
    c.fill = GridBagConstraints.HORIZONTAL;
    c.weightx = 0.1;
    c.weighty = 0.0;
    Container op = getOptionPanel(); 
    mainPanel.add(op, c);

    c.anchor = GridBagConstraints.NORTH;
    c.gridx = 1;
    c.gridy = 1;
    c.gridwidth = 1;
    c.gridheight = 1;
    c.fill = GridBagConstraints.NONE;
    c.weightx = 0.0;
    c.weighty = 0.0;
    Container bp = getButtonPanel();
    mainPanel.add(bp, c);
    // don't let dialog size be dominated by file panel size
    fp.setPreferredSize(new Dimension(op.getPreferredSize().width+bp.getPreferredSize().width, fp.getPreferredSize().height));

    return mainPanel;
  }

  private Container getButtonPanel() {
    JButton saveButton = new JButton(Resources.get("crwriterdialog.btn.save.caption"));
    saveButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        File outputFile = new File((String) comboOutputFile.getSelectedItem());

        try {
          write(FileTypeFactory.singleton().createFileType(outputFile, false).createWriter(
              data.getEncoding(),
              Integer.parseInt(settings.getProperty("Client.CRBackups.count",
                  FileBackup.DEFAULT_BACKUP_LEVEL + ""))));
        } catch (IOException ioe) {
          CRWriterDialog.log.error(ioe);
          JOptionPane.showMessageDialog((JButton) e.getSource(), Resources.getFormatted(
              "crwriterdialog.msg.writeerror.text", outputFile.getName(), ioe.toString()),
              Resources.get("crwriterdialog.msg.exporterror.title"), JOptionPane.WARNING_MESSAGE);
        }
      }
    });
    saveButton.setToolTipText(Resources.get("crwriterdialog.btn.save.tooltip", false));
    
    JButton clipboardButton = new JButton(Resources.get("crwriterdialog.btn.clipboard.caption"));
    clipboardButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        StringWriter sw = new StringWriter();
        write(sw);
        getToolkit().getSystemClipboard().setContents(
            new java.awt.datatransfer.StringSelection(sw.toString()), null);
      }
    });
    clipboardButton.setToolTipText(Resources.get("crwriterdialog.btn.clipboard.tooltip", false));

    JButton cancelButton = new JButton(Resources.get("crwriterdialog.btn.cancel.caption"));
    cancelButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        quit(false);
      }
    });
    cancelButton.setToolTipText(Resources.get("crwriterdialog.btn.cancel.tooltip", false));

    JButton exitButton = new JButton(Resources.get("crwriterdialog.btn.exit.caption"));
    exitButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        quit(true);
      }
    });
    exitButton.setToolTipText(Resources.get("crwriterdialog.btn.exit.tooltip", false));

    JPanel buttonPanel = new JPanel(new SpringLayout());

    buttonPanel.add(saveButton);
    buttonPanel.add(clipboardButton);
    buttonPanel.add(new JSeparator());
    buttonPanel.add(cancelButton);
    buttonPanel.add(exitButton);

    SpringUtilities.makeCompactGrid(buttonPanel, 5, 1, 5, 10, 5, 5);

    return buttonPanel;
  }

  private Container getFilePanel() {
    comboOutputFile =
        new JComboBox(PropertiesHelper.getList(settings, "CRWriterDialog.outputFile").toArray());
    comboOutputFile.setEditable(true);

    // JLabel lblOutputFile = new
    // JLabel(Resources.get("crwriterdialog.lbl.targetfile"));
    // lblOutputFile.setLabelFor(comboOutputFile);

    JButton btnOutputFile = new JButton("...");
    btnOutputFile.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        String outputFile = getFileName((String) comboOutputFile.getSelectedItem());

        if (outputFile != null) {
          // comboOutputFile.addItem(outputFile);
          // bug Fiete 20061217
          comboOutputFile.insertItemAt(outputFile, 0);
          comboOutputFile.setSelectedItem(outputFile);
        }
      }
    });

    JPanel pnlFiles = new JPanel(new GridBagLayout());
    pnlFiles.setBorder(new TitledBorder(BorderFactory.createEtchedBorder(), Resources
        .get("crwriterdialog.border.files")));

    GridBagConstraints c = new GridBagConstraints();

    // // outputFile
    // c.anchor = GridBagConstraints.WEST;
    // c.gridx = 0;
    // c.gridy = 1;
    // c.gridwidth = 1;
    // c.gridheight = 1;
    // c.fill = GridBagConstraints.NONE;
    // c.weightx = 0.0;
    // c.weighty = 0.0;
    // pnlFiles.add(lblOutputFile, c);
    c.anchor = GridBagConstraints.CENTER;
    c.gridx = 1;
    c.gridy = 1;
    c.gridwidth = 1;
    c.gridheight = 1;
    c.fill = GridBagConstraints.HORIZONTAL;
    c.weightx = 0.1;
    c.weighty = 0.0;
    pnlFiles.add(comboOutputFile, c);
    c.anchor = GridBagConstraints.CENTER;
    c.gridx = 2;
    c.gridy = 1;
    c.gridwidth = 1;
    c.gridheight = 1;
    c.fill = GridBagConstraints.NONE;
    c.weightx = 0.0;
    c.weighty = 0.0;
    pnlFiles.add(btnOutputFile, c);

    return pnlFiles;
  }

  private Container getOptionPanel() {
    JButton btnPreAll = new JButton(Resources.get("crwriterdialog.btn.preall.caption"));
    JButton btnPreMap = new JButton(Resources.get("crwriterdialog.btn.premap.caption"));
    JButton btnPreSkills = new JButton(Resources.get("crwriterdialog.btn.preskills.caption"));
    JButton btnPreNoTrust = new JButton(Resources.get("crwriterdialog.btn.prenotrust.caption"));
    JButton btnPreLast = new JButton(Resources.get("crwriterdialog.btn.prelast.caption"));

    btnPreAll.setToolTipText(Resources.get("crwriterdialog.btn.preall.tooltip", false));
    btnPreMap.setToolTipText(Resources.get("crwriterdialog.btn.premap.tooltip", false));
    btnPreSkills.setToolTipText(Resources.get("crwriterdialog.btn.preskills.tooltip", false));
    btnPreNoTrust.setToolTipText(Resources.get("crwriterdialog.btn.prenotrust.tooltip", false));
    btnPreLast.setToolTipText(Resources.get("crwriterdialog.btn.prelast.tooltip", false));

    btnPreAll.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent arg0) {
        setPreAll();
      }
    });
    btnPreMap.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent arg0) {
        setPreMap();
      }
    });
    btnPreSkills.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent arg0) {
        setPreSkills();
      }
    });
    btnPreNoTrust.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent arg0) {
        setPreNoTrust();
      }
    });
    btnPreLast.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent arg0) {
        setPreLast();
      }
    });

    chkServerConformance =
        new JCheckBox(Resources.get("crwriterdialog.chk.servercompatibility.caption"));
    chkIslands = new JCheckBox(Resources.get("crwriterdialog.chk.islands.caption"));
    chkRegions = new JCheckBox(Resources.get("crwriterdialog.chk.regions.caption"));
    chkRegionDetails = new JCheckBox(Resources.get("crwriterdialog.chk.regiondetails.caption"));
    chkBuildings = new JCheckBox(Resources.get("crwriterdialog.chk.buildings.caption"));
    chkShips = new JCheckBox(Resources.get("crwriterdialog.chk.ships.caption"));
    chkUnits = new JCheckBox(Resources.get("crwriterdialog.chk.units.caption"));
    chkUnitDetails = new JCheckBox(Resources.get("crwriterdialog.chk.unitdetails.caption"));
    chkSkills = new JCheckBox(Resources.get("crwriterdialog.chk.skills.caption"));
    chkOrders = new JCheckBox(Resources.get("crwriterdialog.chk.orders.caption"));
    chkItems = new JCheckBox(Resources.get("crwriterdialog.chk.items.caption"));
    chkMessages = new JCheckBox(Resources.get("crwriterdialog.chk.messages.caption"));
    chkSpellsAndPotions =
        new JCheckBox(Resources.get("crwriterdialog.chk.spellsandpotions.caption"));
    chkSelRegionsOnly = new JCheckBox(Resources.get("crwriterdialog.chk.selectedregions.caption"));
    chkSelRegionsOnly.setEnabled((regions != null) && (regions.size() > 0));
    chkDelStats = new JCheckBox(Resources.get("crwriterdialog.chk.delstats.caption"));
    chkDelTrans = new JCheckBox(Resources.get("crwriterdialog.chk.deltrans.caption"));
    chkDelEmptyFactions =
        new JCheckBox(Resources.get("crwriterdialog.chk.delemptyfactions.caption"));
    chkExportHotspots = new JCheckBox(Resources.get("crwriterdialog.chk.exporthotspots.caption"));

    setPreLast();
    chkSelRegionsOnly.setEnabled((regions != null) && (regions.size() > 0));

    // Tooltips
    chkServerConformance.setToolTipText(Resources
        .get("crwriterdialog.chk.servercompatibility.tooltip"));
    chkIslands.setToolTipText(Resources.get("crwriterdialog.chk.islands.tooltip"));
    chkRegions.setToolTipText(Resources.get("crwriterdialog.chk.regions.tooltip"));
    chkRegionDetails.setToolTipText(Resources.get("crwriterdialog.chk.regiondetails.tooltip"));
    chkBuildings.setToolTipText(Resources.get("crwriterdialog.chk.buildings.tooltip"));
    chkShips.setToolTipText(Resources.get("crwriterdialog.chk.ships.tooltip"));
    chkUnits.setToolTipText(Resources.get("crwriterdialog.chk.units.tooltip"));
    chkUnitDetails.setToolTipText(Resources.get("crwriterdialog.chk.unitdetails.tooltip"));
    chkSkills.setToolTipText(Resources.get("crwriterdialog.chk.skills.tooltip"));
    chkOrders.setToolTipText(Resources.get("crwriterdialog.chk.orders.tooltip"));
    chkItems.setToolTipText(Resources.get("crwriterdialog.chk.items.tooltip"));
    chkMessages.setToolTipText(Resources.get("crwriterdialog.chk.messages.tooltip"));
    chkSpellsAndPotions
        .setToolTipText(Resources.get("crwriterdialog.chk.spellsandpotions.tooltip"));
    chkSelRegionsOnly.setToolTipText(Resources.get("crwriterdialog.chk.selectedregions.tooltip"));
    chkDelStats.setToolTipText(Resources.get("crwriterdialog.chk.delstats.tooltip"));
    chkDelTrans.setToolTipText(Resources.get("crwriterdialog.chk.deltrans.tooltip"));
    chkDelEmptyFactions
        .setToolTipText(Resources.get("crwriterdialog.chk.delemptyfactions.tooltip"));
    chkExportHotspots.setToolTipText(Resources.get("crwriterdialog.chk.exporthotspots.tooltip"));

    chkUnits.addItemListener(new ItemListener() {
      public void itemStateChanged(ItemEvent e) {
        if (chkUnits.isSelected()) {
          chkUnitDetails.setEnabled(true);
          chkSkills.setEnabled(true);
          chkOrders.setEnabled(true);
          chkItems.setEnabled(true);
        } else {
          chkUnitDetails.setEnabled(false);
          chkSkills.setEnabled(false);
          chkOrders.setEnabled(false);
          chkItems.setEnabled(false);
        }
      }
    });

    chkUnits.setSelected(!chkUnits.isSelected());
    chkUnits.setSelected(!chkUnits.isSelected());

    // extra ActionListener
    chkServerConformance.addItemListener(new ItemListener() {
      public void itemStateChanged(ItemEvent e) {
        if (chkServerConformance.isSelected()) {
          chkIslands.setEnabled(false);
          chkExportHotspots.setEnabled(false);

        } else {
          chkIslands.setEnabled(true);
          if (data != null && data.hotSpots() != null && data.hotSpots().size() > 0) {
            chkExportHotspots.setEnabled(true);
          } else {
            chkExportHotspots.setEnabled(false);
          }
        }
      }
    });

    chkServerConformance.setSelected(!chkServerConformance.isSelected());
    chkServerConformance.setSelected(!chkServerConformance.isSelected());

    JPanel pnlButtons = new JPanel(new GridLayout(0, 5));
    pnlButtons.setBorder(new TitledBorder(BorderFactory.createEtchedBorder(), Resources
        .get("crwriterdialog.border.prebuttons")));
    pnlButtons.add(btnPreAll);
    pnlButtons.add(btnPreMap);
    pnlButtons.add(btnPreSkills);
    pnlButtons.add(btnPreNoTrust);
    pnlButtons.add(btnPreLast);

    // layout: two columns, as many rows as needed
    JPanel pnlOptions = new JPanel(new GridLayout2(0, 2, 7, 1));

    pnlOptions.add(chkServerConformance);
    pnlOptions.add(chkIslands);
    pnlOptions.add(chkRegions);
    pnlOptions.add(chkRegionDetails);
    pnlOptions.add(chkBuildings);
    pnlOptions.add(chkShips);
    pnlOptions.add(chkUnits);
    pnlOptions.add(chkUnitDetails);
    pnlOptions.add(chkSkills);
    pnlOptions.add(chkItems);
    pnlOptions.add(chkOrders);
    pnlOptions.add(chkMessages);
    pnlOptions.add(chkSpellsAndPotions);
    pnlOptions.add(chkSelRegionsOnly);
    pnlOptions.add(chkDelStats);
    pnlOptions.add(chkDelTrans);
    pnlOptions.add(chkDelEmptyFactions);
    pnlOptions.add(chkExportHotspots);

    JPanel optionPanel = new JPanel(new SpringLayout());
    optionPanel.setBorder(new TitledBorder(BorderFactory.createEtchedBorder(), Resources
        .get("crwriterdialog.border.options")));
    optionPanel.add(pnlButtons);
    optionPanel.add(pnlOptions);
    SpringUtilities.makeCompactGrid(optionPanel, 2, 1, 0, 0, 1, 1);

    return optionPanel;
  }

  protected void setPreLast() {

    chkServerConformance.setSelected(Boolean.valueOf(settings.getProperty(
        "CRWriterDialog.serverConformance", "true")));
    chkIslands.setSelected(Boolean.valueOf(settings.getProperty("CRWriterDialog.includeIslands",
        "true")));
    chkRegions.setSelected(Boolean.valueOf(settings.getProperty("CRWriterDialog.includeRegions",
        "true")));
    chkRegionDetails.setSelected(Boolean.valueOf(settings.getProperty(
        "CRWriterDialog.includeRegionDetails", "true")));
    chkBuildings.setSelected(Boolean.valueOf(settings.getProperty(
        "CRWriterDialog.includeBuildings", "true")));
    chkShips.setSelected(Boolean.valueOf(settings
        .getProperty("CRWriterDialog.includeShips", "true")));
    chkUnitDetails.setSelected(Boolean.valueOf(settings.getProperty(
        "CRWriterDialog.includeUnitDetails", "true")));
    chkSkills.setSelected(Boolean.valueOf(settings.getProperty("CRWriterDialog.includeSkills",
        "true")));
    chkOrders.setSelected(Boolean.valueOf(settings.getProperty("CRWriterDialog.includeOrders",
        "true")));
    chkItems.setSelected(Boolean.valueOf(settings
        .getProperty("CRWriterDialog.includeItems", "true")));
    chkUnits.setSelected(Boolean.valueOf(settings
        .getProperty("CRWriterDialog.includeUnits", "true")));
    chkMessages.setSelected(Boolean.valueOf(settings.getProperty("CRWriterDialog.includeMessages",
        "true")));
    chkSpellsAndPotions.setSelected(Boolean.valueOf(settings.getProperty(
        "CRWriterDialog.includeSpellsAndPotions", "true")));
    chkSelRegionsOnly.setSelected(Boolean.valueOf(settings.getProperty(
        "CRWriterDialog.includeSelRegionsOnly", "false")));
    chkDelStats.setSelected(Boolean.valueOf(settings
        .getProperty("CRWriterDialog.delStats", "false")));
    chkDelTrans.setSelected(Boolean.valueOf(settings
        .getProperty("CRWriterDialog.delTrans", "false")));
    chkDelEmptyFactions.setSelected(Boolean.valueOf(settings.getProperty(
        "CRWriterDialog.delEmptyFactions", "false")));
    chkExportHotspots.setSelected(Boolean.valueOf(settings.getProperty(
        "CRWriterDialog.exportHotspots", "true")));

  }

  protected void setPreNoTrust() {
    chkIslands.setSelected(true);
    chkServerConformance.setSelected(false);
    chkRegions.setSelected(true);
    chkRegionDetails.setSelected(false);
    chkBuildings.setSelected(true);
    chkShips.setSelected(true);
    chkUnitDetails.setSelected(false);
    chkSkills.setSelected(false);
    chkOrders.setSelected(false);
    chkItems.setSelected(false);
    chkUnits.setSelected(true);
    chkMessages.setSelected(false);
    chkSpellsAndPotions.setSelected(false);
    // chkSelRegionsOnly.setSelected();
    chkDelStats.setSelected(true);
    chkDelTrans.setSelected(false);
    chkDelEmptyFactions.setSelected(true);
    chkExportHotspots.setSelected(false);
  }

  protected void setPreSkills() {
    chkIslands.setSelected(true);
    chkServerConformance.setSelected(false);
    chkRegions.setSelected(true);
    chkRegionDetails.setSelected(false);
    chkBuildings.setSelected(false);
    chkShips.setSelected(false);
    chkUnitDetails.setSelected(false);
    chkSkills.setSelected(true);
    chkOrders.setSelected(true);
    chkItems.setSelected(false);
    chkUnits.setSelected(true);
    chkMessages.setSelected(false);
    chkSpellsAndPotions.setSelected(false);
    // chkSelRegionsOnly.setSelected();
    chkDelStats.setSelected(true);
    chkDelTrans.setSelected(false);
    chkDelEmptyFactions.setSelected(true);
    chkExportHotspots.setSelected(false);
  }

  protected void setPreMap() {
    chkIslands.setSelected(true);
    chkServerConformance.setSelected(false);
    chkRegions.setSelected(true);
    chkRegionDetails.setSelected(true);
    chkBuildings.setSelected(true);
    chkShips.setSelected(false);
    chkUnitDetails.setSelected(false);
    chkSkills.setSelected(false);
    chkOrders.setSelected(false);
    chkItems.setSelected(false);
    chkUnits.setSelected(false);
    chkMessages.setSelected(false);
    chkSpellsAndPotions.setSelected(false);
    chkSelRegionsOnly.setSelected(true);
    chkDelStats.setSelected(false);
    chkDelTrans.setSelected(false);
    chkDelEmptyFactions.setSelected(true);
    chkExportHotspots.setSelected(false);
  }

  protected void setPreAll() {
    chkIslands.setSelected(true);
    chkServerConformance.setSelected(false);
    chkRegions.setSelected(true);
    chkRegionDetails.setSelected(true);
    chkBuildings.setSelected(true);
    chkShips.setSelected(true);
    chkUnitDetails.setSelected(true);
    chkSkills.setSelected(true);
    chkOrders.setSelected(true);
    chkItems.setSelected(true);
    chkUnits.setSelected(true);
    chkMessages.setSelected(true);
    chkSpellsAndPotions.setSelected(true);
    chkSelRegionsOnly.setSelected(false);
    chkDelStats.setSelected(false);
    chkDelTrans.setSelected(false);
    chkDelEmptyFactions.setSelected(false);
    chkExportHotspots.setSelected(true);
  }

  private void storeSettings() {
    settings.setProperty("CRWriterDialog.x", getX() + "");
    settings.setProperty("CRWriterDialog.y", getY() + "");

    PropertiesHelper.setList(settings, "CRWriterDialog.outputFile",
        getNewOutputFiles(comboOutputFile));
    settings.setProperty("CRWriterDialog.serverConformance", String.valueOf(chkServerConformance
        .isSelected()));
    settings.setProperty("CRWriterDialog.includeIslands", String.valueOf(chkIslands.isSelected()));
    settings.setProperty("CRWriterDialog.includeRegions", String.valueOf(chkRegions.isSelected()));
    settings.setProperty("CRWriterDialog.includeRegionDetails", String.valueOf(chkRegionDetails
        .isSelected()));
    settings.setProperty("CRWriterDialog.includeBuildings", String.valueOf(chkBuildings
        .isSelected()));
    settings.setProperty("CRWriterDialog.includeShips", String.valueOf(chkShips.isSelected()));
    settings.setProperty("CRWriterDialog.includeUnits", String.valueOf(chkUnits.isSelected()));
    settings.setProperty("CRWriterDialog.includeUnitDetails", String.valueOf(chkUnitDetails
        .isSelected()));
    settings.setProperty("CRWriterDialog.includeSkills", String.valueOf(chkSkills.isSelected()));
    settings.setProperty("CRWriterDialog.includeOrders", String.valueOf(chkOrders.isSelected()));
    settings.setProperty("CRWriterDialog.includeItems", String.valueOf(chkItems.isSelected()));
    settings
        .setProperty("CRWriterDialog.includeMessages", String.valueOf(chkMessages.isSelected()));
    settings.setProperty("CRWriterDialog.includeSpellsAndPotions", String
        .valueOf(chkSpellsAndPotions.isSelected()));
    settings.setProperty("CRWriterDialog.delStats", String.valueOf(chkDelStats.isSelected()));
    settings.setProperty("CRWriterDialog.delTrans", String.valueOf(chkDelTrans.isSelected()));
    settings.setProperty("CRWriterDialog.delEmptyFactions", String.valueOf(chkDelEmptyFactions
        .isSelected()));
    settings.setProperty("CRWriterDialog.exportHotspots", String.valueOf(chkExportHotspots
        .isSelected()));

    if (chkSelRegionsOnly.isEnabled()) {
      settings.setProperty("CRWriterDialog.includeSelRegionsOnly", String.valueOf(chkSelRegionsOnly
          .isSelected()));
    }

    if (standAlone == true) {
      try {
        settings.store(new FileOutputStream(new File(System.getProperty("user.home"),
            "CRWriterDialog.ini")), "");
      } catch (IOException e) {
        CRWriterDialog.log.error("CRWriterDialog.storeSettings():", e);
      }
    }
  }

  private List<Object> getNewOutputFiles(JComboBox combo) {
    List<Object> ret = new ArrayList<Object>(combo.getItemCount() + 1);

    if (combo.getSelectedIndex() == -1) {
      ret.add(combo.getEditor().getItem());
    }

    for (int i = 0; i < Math.min(combo.getItemCount(), 6); i++) {
      ret.add(combo.getItemAt(i));
    }

    return ret;
  }

  /**
   * Quit without saving settings.
   * 
   * @see magellan.client.swing.InternationalizedDataDialog#quit()
   */
  protected void quit(){
    quit(false);
  }
  
  /**
   * If <code>save==true</code>, save settings. Then quit.
   * 
   * @param save
   */
  protected void quit(boolean save) {
    if (save)
      storeSettings();

    if (standAlone == true) {
      System.exit(0);
    } else {
      super.quit();
    }
  }

  private String getFileName(String filename) {
    File defaultFile = new File(filename);
    String retVal = null;

    JFileChooser fc = new JFileChooser();
    EresseaFileFilter crFilter = new EresseaFileFilter(EresseaFileFilter.CR_FILTER);
    fc.addChoosableFileFilter(crFilter);

    EresseaFileFilter gzFilter = new EresseaFileFilter(EresseaFileFilter.GZ_FILTER);
    fc.addChoosableFileFilter(gzFilter);

    EresseaFileFilter bz2Filter = new EresseaFileFilter(EresseaFileFilter.BZ2_FILTER);
    fc.addChoosableFileFilter(bz2Filter);

    // EresseaFileFilter zipFilter = new
    // EresseaFileFilter(EresseaFileFilter.ZIP_FILTER);
    // fc.addChoosableFileFilter(zipFilter);

    // select an active file filter
    if (crFilter.accept(defaultFile)) {
      fc.setFileFilter(crFilter);
    } else if (gzFilter.accept(defaultFile)) {
      fc.setFileFilter(gzFilter);
    } else if (bz2Filter.accept(defaultFile)) {
      fc.setFileFilter(bz2Filter);
      // } else if(zipFilter.accept(defaultFile)) {
      // fc.setFileFilter(zipFilter);
    }

    if (defaultFile != null) {
      fc.setSelectedFile(defaultFile);
    }

    if (fc.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
      retVal = fc.getSelectedFile().getPath();
    }

    return retVal;
  }

  /**
   */
  private synchronized void write(Writer out) {
    setCursor(new Cursor(Cursor.WAIT_CURSOR));

    try {
      CRWriter crw = new CRWriter(new ProgressBarUI(Client.INSTANCE), out);
      crw.setServerConformance(chkServerConformance.isSelected());
      crw.setIncludeIslands(chkIslands.isSelected());
      crw.setIncludeRegions(chkRegions.isSelected());
      crw.setIncludeRegionDetails(chkRegionDetails.isSelected());
      crw.setIncludeBuildings(chkBuildings.isSelected());
      crw.setIncludeShips(chkShips.isSelected());
      crw.setIncludeUnits(chkUnits.isSelected());
      crw.setIncludeUnitDetails(chkUnitDetails.isSelected());
      crw.setIncludeOrders(chkOrders.isSelected());
      crw.setIncludeSkills(chkSkills.isSelected());
      crw.setIncludeItems(chkItems.isSelected());
      crw.setIncludeMessages(chkMessages.isSelected());
      crw.setIncludeSpellsAndPotions(chkSpellsAndPotions.isSelected());
      crw.setExportHotspots(chkExportHotspots.isSelected());

      GameData newData = data;

      // if delemptyfactions is selected we need to have the newData cloned too
      if (chkDelEmptyFactions.isSelected() || chkDelStats.isSelected()
          || chkSelRegionsOnly.isSelected()) {
        // make the clone here already.
        try {
          newData = (GameData) data.clone();
          if (newData==null){
            throw new NullPointerException();
          }
          if (newData.outOfMemory) {
            JOptionPane.showMessageDialog(this, Resources.get("client.msg.outofmemory.text"),
                Resources.get("client.msg.outofmemory.title"), JOptionPane.ERROR_MESSAGE);
            CRWriterDialog.log.error(Resources.get("client.msg.outofmemory.text"));
          }
        } catch (CloneNotSupportedException e) {
          CRWriterDialog.log.error(
              "CRWriterDialog: trying to clone gamedata failed, fallback to merge method.", e);
          newData = GameData.merge(data, data);
        }
        if (!MemoryManagment.isFreeMemory(newData.estimateSize())) {
          JOptionPane.showMessageDialog(this, Resources.get("client.msg.lowmem.text"), Resources
              .get("client.msg.lowmem.title"), JOptionPane.WARNING_MESSAGE);
        }
      }

      if (chkDelStats.isSelected()) {
        // delete points, person counts, spell school, alliances, messages
        // of privileged factions
        // Fiete: why only from privileged factions?
        if (newData.factions() != null) {
          Iterator<Faction> it1 = newData.factions().values().iterator();
          boolean excludeBRegions =
              (crw.getIncludeMessages() && chkSelRegionsOnly.isSelected() && (regions != null) && (regions
                  .size() > 0));

          while (it1.hasNext()) {
            Faction f = it1.next();
            boolean found = true;

            if (excludeBRegions) {
              Iterator<Region> it2 = regions.iterator();
              found = false;

              while (!found && it2.hasNext()) {
                Region reg = it2.next();
                Iterator<Unit> it3 = reg.units().iterator();

                while (!found && it3.hasNext()) {
                  Unit unit = it3.next();
                  found = f.equals(unit.getFaction());
                }
              }
            }

            // here we del the stats !
            f.setAverageScore(-1);
            f.setScore(-1);
            f.setPersons(-1);
            f.setMigrants(-1);
            f.setMaxMigrants(-1);
            f.setSpellSchool(null);
            f.setAllies(null);
            f.setHeroes(-1);
            f.setMaxHeroes(-1);

            if (found && f.isPrivileged()) {
              /**
               * Fiete: removed here and called for every faction
               * f.setAverageScore(-1); f.setScore(-1); f.setPersons(-1);
               * f.setMigrants(-1); f.setMaxMigrants(-1);
               * f.setSpellSchool(null); f.setAllies(null); // FIXED: heroes?
               * (Fiete) f.setHeroes(-1); f.setMaxHeroes(-1);
               **/
              if (excludeBRegions && (f.getMessages() != null)) {
                Iterator<Message> it2 = f.getMessages().iterator();

                // ArrayList of Messages to be removed
                ArrayList<Message> messageRemoveList = null;

                while (it2.hasNext()) {
                  Message mes = it2.next();
                  found = false;

                  Iterator<Region> it3 = regions.iterator();

                  while (it3.hasNext() && !found) {
                    Region reg = it3.next();

                    if (reg.getMessages() != null) {
                      Iterator<Message> it4 = reg.getMessages().iterator();

                      while (!found && it4.hasNext()) {
                        found = mes.equals(it4.next());
                      }
                    }
                  }

                  if (!found) {
                    // removed the remove from used iterator
                    // it2.remove();
                    // adding this message to our removeList
                    if (messageRemoveList == null) {
                      messageRemoveList = new ArrayList<Message>();
                    }
                    if (!messageRemoveList.contains(mes)) {
                      messageRemoveList.add(mes);
                    }
                  }
                }

                // check if some messages should be removed
                if (messageRemoveList != null && messageRemoveList.size() > 0) {
                  for (Iterator iter = messageRemoveList.iterator(); iter.hasNext();) {
                    Message removeM = (Message) iter.next();
                    f.getMessages().remove(removeM);
                  }
                }
              }
            }
          }
        }
      }

      if (chkDelTrans.isSelected()) {
        // clean translation table
        List<String> trans = new LinkedList<String>(newData.translations().getKeyTreeSet());
        // List<String> trans = new
        // LinkedList<String>(newData.translations().keySet());

        // some static data that is not connected but needed
        trans.remove("Einheit");
        trans.remove("Person");
        trans.remove("verwundet");
        trans.remove("schwer verwundet");
        trans.remove("erschöpft");

        Collection<Region> lookup = data.regions().values();

        if (chkSelRegionsOnly.isSelected() && (regions != null) && (regions.size() > 0)) {
          lookup = regions;
        }

        Iterator<Region> regionIterator = lookup.iterator();
        Iterator<RegionResource> resourceIterator = null;
        Iterator<Ship> shipIterator = null;
        Iterator<Building> buildingIterator = null;
        Iterator<Unit> unitIterator = null;
        Iterator<Item> itemIterator = null;
        Iterator<Potion> potionIterator = null;
        Iterator<Spell> spellIterator = null;
        Iterator<Skill> skillIterator = null;
        Iterator<String> stringIterator = null;
        boolean checkShips = chkShips.isSelected();
        boolean checkUnits = chkUnits.isSelected();
//        boolean checkUnitDetails = chkUnitDetails.isSelected();
//        boolean checkSkills = chkSkills.isSelected();
//        boolean checkOrders = chkOrders.isSelected();
//        boolean checkItems = chkItems.isSelected();
        boolean checkBuildings = chkBuildings.isSelected();
        boolean checkSpells = chkSpellsAndPotions.isSelected();
        boolean checkRegDetails = chkRegionDetails.isSelected();

        while (regionIterator.hasNext()) {
          Region r = regionIterator.next();
          trans.remove(r.getType().getID().toString());

          if (checkRegDetails) {
            resourceIterator = r.resources().iterator();

            while (resourceIterator.hasNext()) {
              magellan.library.RegionResource res = resourceIterator.next();
              trans.remove(res.getID().toString());
              trans.remove(res.getType().getID().toString());
            }
          }

          if (checkShips) {
            shipIterator = r.ships().iterator();

            while (shipIterator.hasNext()) {
              trans.remove((shipIterator.next()).getType().getID().toString());
            }
          }

          if (checkBuildings) {
            buildingIterator = r.buildings().iterator();

            while (buildingIterator.hasNext()) {
              trans.remove((buildingIterator.next()).getType().getID().toString());
            }
          }

          if (checkUnits) {
            unitIterator = r.units().iterator();

            while (unitIterator.hasNext()) {
              Unit u = unitIterator.next();
              trans.remove(u.getRace().getID().toString());

              if (u.getRaceNamePrefix() != null) {
                trans.remove(u.getRaceNamePrefix());
              } else {
                if ((u.getFaction() != null) && (u.getFaction().getRaceNamePrefix() != null)) {
                  trans.remove(u.getFaction().getRaceNamePrefix());
                }
              }

              itemIterator = u.getItems().iterator();

              while (itemIterator.hasNext()) {
                trans.remove((itemIterator.next()).getItemType().getID().toString());
              }

              skillIterator = u.getSkills().iterator();

              while (skillIterator.hasNext()) {
                trans.remove((skillIterator.next()).getSkillType().getID().toString());
              }
            }
          }
        }

        if (checkSpells) {
          spellIterator = data.spells().values().iterator();

          while (spellIterator.hasNext()) {
            Spell sp = spellIterator.next();
            trans.remove(sp.getID().toString());
            trans.remove(sp.getName());

            stringIterator = sp.getComponents().keySet().iterator();
            while (stringIterator.hasNext()) {
              trans.remove(stringIterator.next());
            }
          }

          potionIterator = data.potions().values().iterator();

          while (potionIterator.hasNext()) {
            Potion sp = potionIterator.next();
            trans.remove(sp.getID().toString());
            itemIterator = sp.ingredients().iterator();

            while (itemIterator.hasNext()) {
              trans.remove(itemIterator.next().getItemType().getID().toString());
            }
          }
        }

        if (trans.size() > 0) {
          CRWriterDialog.log.debug("Following translations will be removed:");
          stringIterator = trans.iterator();

          // java.util.Map<String,String> newTrans = newData.translations();
          Translations newTrans = newData.translations();

          while (stringIterator.hasNext()) {
            Object o = stringIterator.next();
            newTrans.remove((String) o);

            if (CRWriterDialog.log.isDebugEnabled()) {
              CRWriterDialog.log.debug("Removing: " + o);
            }
          }
        }
      }

      // Deleting empty Factions
      if (this.chkDelEmptyFactions.isSelected()) {
        Collection<Region> lookup = data.regions().values();
        if (chkSelRegionsOnly.isSelected() && (regions != null) && (regions.size() > 0)) {
          lookup = regions;
        }
        // ArrayList of Factions to be removed
        ArrayList<Faction> factionRemoveList = null;
        // Looping through the factions
        if (newData.factions() != null) {
          for (Iterator<Faction> it1 = newData.factions().values().iterator(); it1.hasNext();) {
            Faction actF = it1.next();
            boolean found = false;
            // Looping through exportet regions or all regions, lookup is set
            // already
            if (lookup != null && lookup.size() > 0) {
              Iterator<Region> it2 = lookup.iterator();
              it2.hasNext();
              while (!found && it2.hasNext()) {
                Region reg = it2.next();
                Iterator<Unit> it3 = reg.units().iterator();
                while (!found && it3.hasNext()) {
                  Unit unit = it3.next();
                  if (actF.equals(unit.getFaction())) {
                    // int i22=0;
                  }
                  found = actF.equals(unit.getFaction());
                }
              }

              if (!found) {
                // "remove" removed
                // it1.remove(); // ???? TR: why removing it from the
                // iterator...
                if (factionRemoveList == null) {
                  factionRemoveList = new ArrayList<Faction>();
                }
                if (!factionRemoveList.contains(actF)) {
                  factionRemoveList.add(actF);
                }
              }
            }
          }
        }

        // remove code
        // check if factions should be removed
        if (factionRemoveList != null && factionRemoveList.size() > 0) {
          for (Iterator iter = factionRemoveList.iterator(); iter.hasNext();) {
            Faction removeF = (Faction) iter.next();
            // Removing the faction from newData
            newData.factions().remove(removeF.getID());
            // alliances...if one of the partners is our delete Faction->delete
            this.cleanAllianzes(newData, removeF);
          }
        }
      }

      // Messages: remove all messages concerning regions not in selection
      if (chkSelRegionsOnly.isSelected() && (regions != null) && (regions.size() > 0)) {
        this.cleanMessages(newData, regions);
        this.cleanBattles(newData, regions);
      }

      if (chkSelRegionsOnly.isSelected() && (regions != null) && (regions.size() > 0)) {
        crw.setRegions(regions);
      }

      crw.write(newData);
      crw.close();
    } catch (Exception exc) {
      CRWriterDialog.log.error(exc);
      JOptionPane.showMessageDialog(this, Resources.get("crwriterdialog.msg.exporterror.text")
          + exc.toString(), Resources.get("crwriterdialog.msg.exporterror.title"),
          JOptionPane.WARNING_MESSAGE);
    }

    setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
  }

  /**
   * removes all alliances from all factions to the specific faction
   * 
   * @param data
   * @param factionToDel
   */
  private void cleanAllianzes(GameData data, Faction factionToDel) {
    Iterator<Faction> i1 = data.factions().values().iterator();
    while (i1.hasNext()) {
      Faction actF = i1.next();
      if (!actF.equals(factionToDel)) {
        this.cleanAllies(actF.getAllies(), factionToDel);
        // groups too
        if (actF.getGroups() != null) {
          Iterator<Group> i2 = actF.getGroups().values().iterator();
          while (i2.hasNext()) {
            Group actG = i2.next();
            this.cleanAllies(actG.allies(), factionToDel);
          }
        }
      }
    }
  }

  /**
   * Removes alliances to specific factions
   * 
   * @param allies
   *          may be <code>null</code>
   * @param factionToDel
   */
  private void cleanAllies(Map<ID, Alliance> allies, Faction factionToDel) {
    ArrayList<ID> allianceRemoveList = null;
    if (allies != null && allies.keySet() != null) {
      Iterator<ID> i2 = allies.keySet().iterator();
      while (i2.hasNext()) {
        ID allianceID = i2.next();
        Alliance actAlliance = allies.get(allianceID);
        if (actAlliance.getFaction().equals(factionToDel)) {
          // jip! delete it!
          if (allianceRemoveList == null) {
            allianceRemoveList = new ArrayList<ID>();
          }
          allianceRemoveList.add(allianceID);
        }
      }
      // something to delete?
      if (allianceRemoveList != null && allianceRemoveList.size() > 0) {
        Iterator<ID> i3 = allianceRemoveList.iterator();
        while (i3.hasNext()) {
          allies.remove(i3.next());
        }
      }
    }
  }

  /**
   * Removes all Messages linking to regions not in regionList
   * 
   * @param data
   * @param regionList
   */
  private void cleanMessages(GameData data, Collection<Region> regionList) {
    // Messages of factions
    if (data.factions() != null) {
      Iterator<Faction> i1 = data.factions().values().iterator();
      while (i1.hasNext()) {
        this.cleanMessages(data, i1.next(), regionList);
      }
    }
    // Messages of regions
    if (data.regions() != null) {
      Iterator<Region> i1 = data.regions().values().iterator();
      while (i1.hasNext()) {
        this.cleanMessages(data, i1.next(), regionList);
      }
    }
  }

  /**
   * Removes all Messages of the faction linking to regions not in regionList
   * 
   * @param data
   * @param regionList
   */
  private void cleanMessages(GameData data, Faction f, Collection<Region> regionList) {
    if (f == null || f.getMessages() == null || f.getMessages().size() == 0) {
      return;
    }
    this.cleanMessages(data, f.getMessages(), regionList);
  }

  /**
   * Removes all Messages of the faction linking to regions not in regionList
   * 
   * @param data
   * @param regionList
   */
  private void cleanMessages(GameData data, Region r, Collection<Region> regionList) {
    if (r == null || r.getMessages() == null || r.getMessages().size() == 0) {
      return;
    }
    this.cleanMessages(data, r.getMessages(), regionList);
  }

  /**
   * Removes all Messages of the faction linking to regions not in regionList
   * checkimg msg-tags: region, target, unit, student, teacher
   * 
   * @param data
   * @param regionList
   */
  private void cleanMessages(GameData data, List<Message> msgList, Collection<Region> regionList) {
    if (msgList == null || msgList.size() == 0 || regionList == null || regionList.size() == 0) {
      return;
    }

    ArrayList<Message> keepList = null;
    Iterator<Message> i1 = msgList.iterator();
    while (i1.hasNext()) {
      Message msg = i1.next();
      if (msg.getAttributes() != null) {
        // check whether the message belongs to one of the selected regions
        // region related messages:
        if (this.msgRegionAttributeNotInRegionList(data, msg, "region", regionList)
            || this.msgRegionAttributeNotInRegionList(data, msg, "from", regionList)
            || this.msgRegionAttributeNotInRegionList(data, msg, "to", regionList)) {
          continue;
        }
        // unit related messages
        if (this.msgUnitAttributeNotInRegionList(data, msg, "unit", regionList)
            || this.msgUnitAttributeNotInRegionList(data, msg, "teacher", regionList)
            || this.msgUnitAttributeNotInRegionList(data, msg, "student", regionList)
            || this.msgUnitAttributeNotInRegionList(data, msg, "target", regionList)) {
          continue;
        }
      }
      // checks done, what left here should be kept
      if (keepList == null) {
        keepList = new ArrayList<Message>();
      }
      keepList.add(msg);
    }

    // ready...now delete all messages and add the keep list
    msgList.clear();
    if (keepList != null && keepList.size() > 0) {
      msgList.addAll(keepList);
    }
  }

  /**
   * true, if the Attribute tagName of the Message msg links not to a region in
   * regionLists
   * 
   * @param data
   * @param tagName
   * @param regionList
   * @return
   */
  private boolean msgUnitAttributeNotInRegionList(GameData data, Message msg, String tagName,
      Collection<Region> regionList) {
    boolean erg = false;
    String value = msg.getAttributes().get(tagName);
    if (value != null && value.indexOf(" ") < 0) {
      erg = true;
      String number = value;
      UnitID id = UnitID.createUnitID(number, 10);
      Unit unit = data.units().get(id);
      if (unit != null) {
        Region r = unit.getRegion();
        if (r != null) {
          if (regionList.contains(r)) {
            return false;
          }
        }
      }
    }
    return erg;
  }

  /**
   * true, if the Attribute tagName of the Message msg links not to a region in
   * regionLists
   * 
   * @param data
   * @param tagName
   * @param regionList
   * @return
   */
  private boolean msgRegionAttributeNotInRegionList(GameData data, Message msg, String tagName,
      Collection<Region> regionList) {
    boolean erg = false;
    String value = msg.getAttributes().get(tagName);
    if (value != null && value.indexOf(" ") > 0) {
      erg = true;
      String regionCoordinate = value;
      CoordinateID coordinate = CoordinateID.parse(regionCoordinate, ",");
      if (coordinate == null) {
        coordinate = CoordinateID.parse(regionCoordinate, " ");
      }
      if (coordinate != null) {
        Region mR = data.getRegion(coordinate);
        if (mR != null) {
          if (regionList.contains(mR)) {
            return false;
          }
        }
      }
    }
    return erg;
  }

  /**
   * clean up the battles outsinde regionList
   * 
   * @param data
   * @param regionList
   */
  private void cleanBattles(GameData data, Collection<Region> regionList) {
    if (data.factions() != null) {
      Iterator<Faction> i1 = data.factions().values().iterator();
      while (i1.hasNext()) {
        Faction actF = i1.next();
        if (actF.getBattles() != null && actF.getBattles().size() > 0) {
          this.cleanBattles(data, actF, regionList);
        }
      }
    }

  }

  /**
   * removes all battles from the faction which took place outside the regions
   * in regionList
   * 
   * @param data
   * @param f
   * @param regionList
   */
  private void cleanBattles(GameData data, Faction f, Collection<Region> regionList) {
    if (f.getBattles() == null || f.getBattles().size() == 0) {
      return;
    }
    ArrayList<Battle> battleRemoveList = null;
    Iterator<Battle> it1 = f.getBattles().iterator();
    while (it1.hasNext()) {
      Battle actBattle = it1.next();
      Region actR = data.getRegion((CoordinateID) actBattle.getID());
      if (actR == null || !regionList.contains(actR)) {
        // we have to remove the battle
        if (battleRemoveList == null) {
          battleRemoveList = new ArrayList<Battle>();
        }
        battleRemoveList.add(actBattle);
      }
    }
    if (battleRemoveList != null && battleRemoveList.size() > 0) {
      f.getBattles().removeAll(battleRemoveList);
    }
  }

}
