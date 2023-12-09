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
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

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

import magellan.client.swing.basics.SpringUtilities;
import magellan.client.swing.layout.GridLayout2;
import magellan.client.utils.SwingUtils;
import magellan.library.Alliance;
import magellan.library.AllianceGroup;
import magellan.library.Battle;
import magellan.library.Building;
import magellan.library.CoordinateID;
import magellan.library.EntityID;
import magellan.library.Faction;
import magellan.library.GameData;
import magellan.library.GameDataMerger;
import magellan.library.Group;
import magellan.library.ID;
import magellan.library.Item;
import magellan.library.Message;
import magellan.library.Potion;
import magellan.library.Region;
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
import magellan.library.utils.TrustLevels;
import magellan.library.utils.UserInterface;
import magellan.library.utils.logging.Logger;
import magellan.library.utils.transformation.IdentityTransformer;

/**
 * A GUI for writing a CR to a file or copy it to the clipboard. This class can be used as a
 * stand-alone application or can be integrated as dialog into a different application.
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

  /**
   * Create a stand-alone instance of CRWriterDialog.
   */
  public CRWriterDialog(GameData data) {
    super(null, false, null, data, new Properties());
    standAlone = true;
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
    init();
  }

  /**
   * Create a new CRWriterDialog object as a dialog with a parent window and a set of selected
   * regions.
   */
  public CRWriterDialog(Frame owner, boolean modal, GameData initData, Properties p,
      Collection<Region> selectedRegions) {
    super(owner, modal, null, initData, p);
    regions = selectedRegions;
    init();
  }

  private void init() {
    setContentPane(getMainPane());
    setTitle(Resources.get("crwriterdialog.window.title"));
    pack();

    SwingUtils.setLocation(this, settings, "CRWriterDialog.x", "CRWriterDialog.y");
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
    fp.setPreferredSize(new Dimension(op.getPreferredSize().width + bp.getPreferredSize().width, fp
        .getPreferredSize().height));

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
          if (data != null && data.getBookmarks().size() > 0) {
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
  @Override
  protected void quit() {
    quit(false);
  }

  /**
   * If <code>save==true</code>, save settings. Then quit.
   * 
   * @param save
   */
  protected void quit(boolean save) {
    if (save) {
      storeSettings();
    }

    if (standAlone == true) {
      System.exit(0);
    } else {
      super.quit();
    }
  }

  private String getFileName(String filename) {
    File defaultFile = filename == null ? null : new File(filename);
    String retVal = null;

    JFileChooser fc = new JFileChooser();
    EresseaFileFilter crFilter = new EresseaFileFilter(EresseaFileFilter.CR_FILTER);
    fc.addChoosableFileFilter(crFilter);

    EresseaFileFilter gzFilter = new EresseaFileFilter(EresseaFileFilter.GZ_FILTER);
    fc.addChoosableFileFilter(gzFilter);

    EresseaFileFilter bz2Filter = new EresseaFileFilter(EresseaFileFilter.BZ2_FILTER);
    fc.addChoosableFileFilter(bz2Filter);

    EresseaFileFilter allCrFilter = new EresseaFileFilter(EresseaFileFilter.ALLCR_FILTER);
    fc.addChoosableFileFilter(allCrFilter);

    // we don't currently support zip files for cr export

    fc.setFileFilter(allCrFilter);

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
  private synchronized void write(final Writer out) {
    setCursor(new Cursor(Cursor.WAIT_CURSOR));

    final UserInterface ui = new ProgressBarUI(this);
    ui.show();
    new Thread(new Runnable() {

      public void run() {
        try {
          doWrite(ui, out);
        } finally {
          ui.ready();
        }
      }
    }).start();
    setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
  }

  private void doWrite(UserInterface ui, Writer out) {
    try {
      int maxProgress =
          2 + 2 * (chkSelRegionsOnly.isEnabled() && chkSelRegionsOnly.isSelected() ? regions.size()
              : data.getRegions().size());
      ui.setMaximum(maxProgress);
      ui.setTitle(Resources.get("crwriterdialog.progress.title"));
      ui.setProgress(Resources.get("crwriterdialog.progress.start"), 2);

      GameData newData = data;

      if (chkDelEmptyFactions.isSelected() || chkDelStats.isSelected()
          || chkSelRegionsOnly.isSelected()) {
        ui.setProgress(Resources.get("crwriterdialog.progress.cloning"), Math.max(2,
            maxProgress / 20));
        newData = cloneData(data);
      }

      if (chkDelStats.isSelected()) {
        // delete points, person counts, spell school, alliances, messages
        // of privileged factions
        // Fiete: why only from privileged factions?
        ui.setProgress(Resources.get("crwriterdialog.progress.cleanfactions"),
            maxProgress / 2 / 5 * 1);
        delStats(newData);
      }

      if (chkDelTrans.isSelected()) {
        // clean translation table
        ui.setProgress(Resources.get("crwriterdialog.progress.cleantranslations"),
            maxProgress / 2 / 5 * 2);

        cleanTranslations(newData);
      }

      if (chkDelEmptyFactions.isSelected()) {
        ui.setProgress(Resources.get("crwriterdialog.progress.cleanemptyfactions"),
            maxProgress / 2 / 5 * 3);
        // Deleting empty Factions
        deleteEmptyFactions(newData);
      }

      ui.setProgress(Resources.get("crwriterdialog.progress.cleanmessages"),
          maxProgress / 2 / 5 * 4);
      // Messages: remove all messages concerning regions not in selection
      if (chkSelRegionsOnly.isSelected() && (regions != null) && (regions.size() > 0)) {
        cleanMessages(newData, regions);
        cleanBattles(newData, regions);
      }

      CRWriter crw = new CRWriter(newData, ui, out);
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

      if (chkSelRegionsOnly.isSelected() && (regions != null) && (regions.size() > 0)) {
        crw.setRegions(regions);
      }

      ui.setProgress(Resources.get("crwriterdialog.progress.writing"), maxProgress / 2);
      try {
        crw.writeSynchronously();
      } finally {
        crw.close();
      }
    } catch (Exception exc) {
      CRWriterDialog.log.error(exc);
      JOptionPane.showMessageDialog(this, Resources.get("crwriterdialog.msg.exporterror.text")
          + exc.toString(), Resources.get("crwriterdialog.msg.exporterror.title"),
          JOptionPane.WARNING_MESSAGE);
    }
  }

  protected GameData cloneData(GameData data2) {
    // if delemptyfactions is selected we need to have the newData cloned too
    GameData newData;
    // make the clone here already.
    try {
      newData = data.clone();
      if (newData == null)
        throw new NullPointerException();
      if (newData.isOutOfMemory()) {
        JOptionPane.showMessageDialog(this, Resources.get("client.msg.outofmemory.text"), Resources
            .get("client.msg.outofmemory.title"), JOptionPane.ERROR_MESSAGE);
        CRWriterDialog.log.error(Resources.get("client.msg.outofmemory.text"));
      }
    } catch (CloneNotSupportedException e) {
      CRWriterDialog.log.error(
          "CRWriterDialog: trying to clone gamedata failed, fallback to merge method.", e);
      newData = GameDataMerger.merge(data, new IdentityTransformer());
    }
    if (!MemoryManagment.isFreeMemory(newData.estimateSize())) {
      JOptionPane.showMessageDialog(this, Resources.get("client.msg.lowmem.text"), Resources
          .get("client.msg.lowmem.title"), JOptionPane.WARNING_MESSAGE);
    }
    return newData;
  }

  protected void delStats(GameData newData) {
    if (newData.getFactions() != null) {
      boolean excludeBRegions =
          (chkMessages.isSelected() && chkSelRegionsOnly.isSelected() && (regions != null) && (regions
              .size() > 0));

      for (Faction f : newData.getFactions()) {
        boolean found = true;

        if (excludeBRegions) {
          found = false;
          for (Region reg : regions) {
            if (found) {
              break;
            }
            for (Unit unit : reg.units()) {
              if (found) {
                break;
              }
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

        if (found && TrustLevels.isPrivileged(f)) {
          /**
           * Fiete: removed here and called for every faction f.setAverageScore(-1); f.setScore(-1);
           * f.setPersons(-1); f.setMigrants(-1); f.setMaxMigrants(-1); f.setSpellSchool(null);
           * f.setAllies(null); // FIXED: heroes? (Fiete) f.setHeroes(-1); f.setMaxHeroes(-1);
           **/
          if (excludeBRegions && (f.getMessages() != null)) {

            // ArrayList of Messages to be removed
            ArrayList<Message> messageRemoveList = null;

            for (Message mes : f.getMessages()) {
              found = false;

              for (Region reg : regions) {
                if (found) {
                  break;
                }

                if (reg.getMessages() != null) {
                  for (Message message : reg.getMessages()) {
                    if (found) {
                      break;
                    }
                    found = mes.equals(message);
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
              for (Message removeM : messageRemoveList) {
                f.getMessages().remove(removeM);
              }
            }
          }
        }
      }
    }
  }

  protected void cleanTranslations(GameData newData) {
    List<String> trans = new LinkedList<String>(newData.translations().getKeyTreeSet());
    // List<String> trans = new
    // LinkedList<String>(newData.translations().keySet());

    // some static data that is not connected but needed
    trans.remove("Einheit");
    trans.remove("Person");
    trans.remove("verwundet");
    trans.remove("schwer verwundet");
    trans.remove("erschöpft");

    Collection<? extends Region> lookup = data.getRegions();

    if (chkSelRegionsOnly.isSelected() && (regions != null) && (regions.size() > 0)) {
      lookup = regions;
    }

    boolean checkShips = chkShips.isSelected();
    boolean checkUnits = chkUnits.isSelected();
    // boolean checkUnitDetails = chkUnitDetails.isSelected();
    // boolean checkSkills = chkSkills.isSelected();
    // boolean checkOrders = chkOrders.isSelected();
    // boolean checkItems = chkItems.isSelected();
    boolean checkBuildings = chkBuildings.isSelected();
    boolean checkSpells = chkSpellsAndPotions.isSelected();
    boolean checkRegDetails = chkRegionDetails.isSelected();

    for (Region r : lookup) {
      trans.remove(r.getType().getID().toString());

      if (checkRegDetails) {
        for (magellan.library.RegionResource res : r.resources()) {
          trans.remove(res.getID().toString());
          trans.remove(res.getType().getID().toString());
        }
      }

      if (checkShips) {
        for (Ship ship : r.ships()) {
          trans.remove(ship.getType().getID().toString());
        }
      }

      if (checkBuildings) {
        for (Building b : r.buildings()) {
          trans.remove(b.getType().getID().toString());
        }
      }

      if (checkUnits) {
        for (Unit u : r.units()) {
          trans.remove(u.getRace().getID().toString());

          if (u.getRaceNamePrefix() != null) {
            trans.remove(u.getRaceNamePrefix());
          } else {
            if ((u.getFaction() != null) && (u.getFaction().getRaceNamePrefix() != null)) {
              trans.remove(u.getFaction().getRaceNamePrefix());
            }
          }

          for (Item item : u.getItems()) {
            trans.remove(item.getItemType().getID().toString());
          }

          for (Skill skill : u.getSkills()) {
            trans.remove(skill.getSkillType().getID().toString());
          }
        }
      }
    }

    if (checkSpells) {
      for (Spell sp : data.getSpells()) {
        trans.remove(sp.getID().toString());
        trans.remove(sp.getName());

        for (String comp : sp.getComponents().keySet()) {
          trans.remove(comp);
        }
      }

      for (Potion potion : data.getPotions()) {
        trans.remove(potion.getID().toString());
        for (Item item : potion.ingredients()) {
          trans.remove(item.getItemType().getID().toString());
        }
      }
    }

    if (trans.size() > 0) {
      CRWriterDialog.log.debug("Following translations will be removed:");

      Translations newTrans = newData.translations();

      for (String translation : trans) {
        newTrans.remove(translation);

        if (CRWriterDialog.log.isDebugEnabled()) {
          CRWriterDialog.log.debug("Removing: " + translation);
        }
      }
    }
  }

  protected void deleteEmptyFactions(GameData newData) {
    Collection<? extends Region> lookup = data.getRegions();
    if (chkSelRegionsOnly.isSelected() && (regions != null) && (regions.size() > 0)) {
      lookup = regions;
    }
    // ArrayList of Factions to be removed
    Set<Faction> factionRemoveList = new HashSet<Faction>();
    // Looping through the factions
    if (newData.getFactions() != null) {
      for (Faction actF : newData.getFactions()) {
        boolean found = false;
        // Looping through exported regions or all regions to see if the faction has a unit
        // lookup is set already
        if (lookup != null && lookup.size() > 0) {
          for (Region reg : lookup) {
            if (found) {
              break;
            }
            for (Unit unit : reg.units()) {
              if (found) {
                break;
              }
              found = actF.equals(unit.getFaction());
            }
          }

          if (!found) {
            factionRemoveList.add(actF);
          }
        }
      }
    }

    // remove code
    // check if factions should be removed
    if (factionRemoveList.size() > 0) {
      for (Faction removeF : factionRemoveList) {
        // Removing the faction from newData
        newData.removeFaction(removeF.getID());

        // alliances...if one of the partners is our delete Faction->delete
        cleanAllianzes(newData, removeF);
      }
      for (Iterator<AllianceGroup> iterator = newData.getAllianceGroups().iterator(); iterator
          .hasNext();) {
        AllianceGroup alliance = iterator.next();
        if (alliance.getFactions().size() == 0) {
          iterator.remove();
        }
      }
    }
  }

  /**
   * removes all alliances from all factions to the specific faction
   * 
   * @param data
   * @param factionToDel
   */
  private void cleanAllianzes(GameData data, Faction factionToDel) {
    for (Faction actF : data.getFactions()) {
      if (!actF.equals(factionToDel)) {
        cleanAllies(actF.getAllies(), factionToDel);
        // groups too
        if (actF.getGroups() != null) {
          for (Group actG : actF.getGroups().values()) {
            cleanAllies(actG.allies(), factionToDel);
          }
        }
      }
      if (actF.getAlliance() != null) {
        for (Iterator<ID> iterator = actF.getAlliance().getFactions().iterator(); iterator
            .hasNext();) {
          ID id = iterator.next();
          if (id.equals(factionToDel.getID())) {
            iterator.remove();
          }
        }
      }
    }

  }

  /**
   * Removes alliances to specific factions
   * 
   * @param allies may be <code>null</code>
   * @param factionToDel
   */
  private void cleanAllies(Map<EntityID, Alliance> allies, Faction factionToDel) {
    ArrayList<EntityID> allianceRemoveList = null;
    if (allies != null && allies.keySet() != null) {
      for (EntityID allianceID : allies.keySet()) {
        Alliance actAlliance = allies.get(allianceID);
        if (actAlliance.getFaction().equals(factionToDel)) {
          // jip! delete it!
          if (allianceRemoveList == null) {
            allianceRemoveList = new ArrayList<EntityID>();
          }
          allianceRemoveList.add(allianceID);
        }
      }
      // something to delete?
      if (allianceRemoveList != null && allianceRemoveList.size() > 0) {
        for (EntityID id : allianceRemoveList) {
          allies.remove(id);
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
    if (data.getFactions() != null) {
      for (Faction f : data.getFactions()) {
        this.cleanMessages(data, f, regionList);
      }
    }
    // Messages of regions
    if (data.getRegions() != null) {
      for (Region r : data.getRegions()) {
        this.cleanMessages(data, r, regionList);
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
    if (f == null || f.getMessages() == null || f.getMessages().size() == 0)
      return;
    this.cleanMessages(data, f.getMessages(), regionList);
  }

  /**
   * Removes all Messages of the faction linking to regions not in regionList
   * 
   * @param data
   * @param regionList
   */
  private void cleanMessages(GameData data, Region r, Collection<Region> regionList) {
    if (r == null || r.getMessages() == null || r.getMessages().size() == 0)
      return;
    this.cleanMessages(data, r.getMessages(), regionList);
  }

  /**
   * Removes all Messages of the faction linking to regions not in regionList checkimg msg-tags:
   * region, target, unit, student, teacher
   * 
   * @param data
   * @param regionList
   */
  private void cleanMessages(GameData data, List<Message> msgList, Collection<Region> regionList) {
    if (msgList == null || msgList.size() == 0 || regionList == null || regionList.size() == 0)
      return;

    ArrayList<Message> keepList = null;
    for (Message msg : msgList) {
      // check whether the message belongs to one of the selected regions
      // region related messages:
      if (msgRegionAttributeNotInRegionList(data, msg, "region", regionList)
          || msgRegionAttributeNotInRegionList(data, msg, "from", regionList)
          || msgRegionAttributeNotInRegionList(data, msg, "to", regionList)) {
        continue;
      }
      // unit related messages
      if (msgUnitAttributeNotInRegionList(data, msg, "unit", regionList)
          || msgUnitAttributeNotInRegionList(data, msg, "teacher", regionList)
          || msgUnitAttributeNotInRegionList(data, msg, "student", regionList)
          || msgUnitAttributeNotInRegionList(data, msg, "target", regionList)) {
        continue;
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
   * true, if the Attribute tagName of the Message msg links not to a region in regionLists
   * 
   * @param data
   * @param tagName
   * @param regionList
   */
  private boolean msgUnitAttributeNotInRegionList(GameData data, Message msg, String tagName,
      Collection<Region> regionList) {
    boolean erg = false;
    String value = msg.getAttribute(tagName);
    if (value != null && value.indexOf(" ") < 0) {
      erg = true;
      String number = value;
      UnitID id = UnitID.createUnitID(number, 10, data.base);
      Unit unit = data.getUnit(id);
      if (unit != null) {
        Region r = unit.getRegion();
        if (r != null) {
          if (regionList.contains(r))
            return false;
        }
      }
    }
    return erg;
  }

  /**
   * true, if the Attribute tagName of the Message msg links not to a region in regionLists
   * 
   * @param data
   * @param tagName
   * @param regionList
   */
  private boolean msgRegionAttributeNotInRegionList(GameData data, Message msg, String tagName,
      Collection<Region> regionList) {
    boolean erg = false;
    String value = msg.getAttribute(tagName);
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
          if (regionList.contains(mR))
            return false;
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
    if (data.getFactions() != null) {
      for (Faction actF : data.getFactions()) {
        if (actF.getBattles() != null && actF.getBattles().size() > 0) {
          this.cleanBattles(data, actF, regionList);
        }
      }
    }

  }

  /**
   * removes all battles from the faction which took place outside the regions in regionList
   * 
   * @param data
   * @param f
   * @param regionList
   */
  private void cleanBattles(GameData data, Faction f, Collection<Region> regionList) {
    if (f.getBattles() == null || f.getBattles().size() == 0)
      return;
    ArrayList<Battle> battleRemoveList = null;
    for (Battle actBattle : f.getBattles()) {
      Region actR = data.getRegion(actBattle.getID());
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
