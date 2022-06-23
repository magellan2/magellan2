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

package magellan.client.actions.file;

import java.awt.event.ActionEvent;
import java.io.File;
import java.io.IOException;
import java.util.Properties;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;

import magellan.client.Client;
import magellan.client.actions.MenuAction;
import magellan.client.swing.EresseaFileFilter;
import magellan.client.utils.SwingUtils;
import magellan.library.event.GameDataEvent;
import magellan.library.event.GameDataListener;
import magellan.library.io.file.FileType;
import magellan.library.io.file.FileTypeFactory;
import magellan.library.utils.Resources;
import magellan.library.utils.logging.Logger;

/**
 * DOCUMENT ME!
 * 
 * @author Andreas
 * @version 1.0
 */
public class FileSaveAsAction extends MenuAction implements GameDataListener {
  private static final Logger log = Logger.getInstance(FileSaveAsAction.class);

  /**
   * Creates a new FileSaveAsAction object.
   * 
   * @param client The client object for this action.
   */
  public FileSaveAsAction(Client client) {
    super(client);
    setEnabled(false);
    client.getDispatcher().addGameDataListener(this);
  }

  /**
   * @see magellan.client.actions.MenuAction#getIconName()
   */
  @Override
  public String getIconName() {
    return "saveas_edit";
  }

  /**
   * Does the saving.
   * 
   * @see magellan.client.actions.MenuAction#menuActionPerformed(java.awt.event.ActionEvent)
   */
  @Override
  public void menuActionPerformed(ActionEvent e) {
    FileType file = getFile();

    if (file != null) {
      doSaveAction(file);
    } else {
      doSaveAsAction();
    }
  }

  public static File getFile(Client client) {
    Properties settings = client.getProperties();
    JFileChooser fc = new JFileChooser();
    fc.setAcceptAllFileFilterUsed(false);

    EresseaFileFilter crFilter = new EresseaFileFilter(EresseaFileFilter.CR_FILTER);
    fc.addChoosableFileFilter(crFilter);

    EresseaFileFilter gzFilter = new EresseaFileFilter(EresseaFileFilter.GZ_FILTER);
    fc.addChoosableFileFilter(gzFilter);

    EresseaFileFilter bz2Filter = new EresseaFileFilter(EresseaFileFilter.BZ2_FILTER);
    fc.addChoosableFileFilter(bz2Filter);

    // EresseaFileFilter zipFilter = new EresseaFileFilter(EresseaFileFilter.ZIP_FILTER);
    // fc.addChoosableFileFilter(zipFilter);

    EresseaFileFilter allCrFilter = new EresseaFileFilter(EresseaFileFilter.ALLCR_FILTER);
    fc.addChoosableFileFilter(allCrFilter);

    File selectedFile = new File(settings.getProperty("Client.lastCRSaved", ""));
    fc.setSelectedFile(selectedFile);

    // select an active file filter
    if (crFilter.accept(selectedFile)) {
      fc.setFileFilter(crFilter);
    } else if (gzFilter.accept(selectedFile)) {
      fc.setFileFilter(gzFilter);
    } else if (bz2Filter.accept(selectedFile)) {
      fc.setFileFilter(bz2Filter);

      // } else if(zipFilter.accept(selectedFile)) {
      // fc.setFileFilter(zipFilter);
    }

    fc.setAccessory(new magellan.client.swing.HistoryAccessory(settings, fc));
    fc.setDialogTitle(Resources.get("actions.filesaveasaction.title"));
    SwingUtils.setPreferredSize(fc, 40, -1, true);

    if (fc.showSaveDialog(client) == JFileChooser.APPROVE_OPTION) {
      boolean bOpenEqualsSave =
          Boolean.valueOf(settings.getProperty("Client.openEqualsSave", "false")).booleanValue();

      if (bOpenEqualsSave) {
        settings.setProperty("Client.lastCROpened", fc.getSelectedFile().getAbsolutePath());
      }

      File dataFile = fc.getSelectedFile();
      EresseaFileFilter actFilter = (EresseaFileFilter) fc.getFileFilter();
      dataFile = actFilter.addExtension(dataFile);

      // if(dataFile.exists()) {
      // FIXME(pavkovic) ask, if file should be overwritten
      // stop execution of saveaction if necessary
      // try {
      // File backup = FileBackup.create(dataFile);
      // log.info("Created backupfile " + backup);
      // } catch(IOException ie) {
      // log.warn("Could not create backupfile for file " + dataFile);
      // }
      // }

      return dataFile;
    } else
      return null;
  }

  protected void doSaveAsAction() {

    File dataFile = FileSaveAsAction.getFile(client);

    if (dataFile != null) {
      doSaveAction(dataFile);
    }
  }

  protected void doSaveAction(File file) {
    try {
      // log.info("debuging: doSaveAction(File) called for file: " + file.toString());
      doSaveAction(FileTypeFactory.singleton().createFileType(file, false));
    } catch (IOException exc) {
      FileSaveAsAction.log.error(exc);
      JOptionPane.showMessageDialog(client, exc.toString(), Resources
          .get("actions.filesaveaction.msg.filesave.error.title"), JOptionPane.ERROR_MESSAGE);
    }
  }

  protected void doSaveAction(FileType filetype) {
    client.saveReport(filetype);
    File f = null;
    try {
      f = filetype.getFile();
    } catch (IOException e) {
      // do nothing
    }
    if (f != null) {
      client.addFileToHistory(f);
    }
  }

  /**
   * this function delivers overwriteable FileType. In FileSaveAsAction it shall deliver null, in
   * FileSaveAction the file type of the gamedata if exists.
   */
  protected FileType getFile() {
    return null;
  }

  public void gameDataChanged(GameDataEvent e) {
    int i = e.getGameData().getRegions().size();
    if (i > 0) {
      setEnabled(true);
    } else {
      setEnabled(false);
    }
  }

  /**
   * @see magellan.client.actions.MenuAction#getAcceleratorTranslated()
   */
  @Override
  protected String getAcceleratorTranslated() {
    return Resources.get("actions.filesaveasaction.accelerator", false);
  }

  /**
   * @see magellan.client.actions.MenuAction#getMnemonicTranslated()
   */
  @Override
  protected String getMnemonicTranslated() {
    return Resources.get("actions.filesaveasaction.mnemonic", false);
  }

  /**
   * @see magellan.client.actions.MenuAction#getNameTranslated()
   */
  @Override
  protected String getNameTranslated() {
    return Resources.get("actions.filesaveasaction.name");
  }

  @Override
  protected String getTooltipTranslated() {
    return Resources.get("actions.filesaveasaction.tooltip", false);
  }
}
