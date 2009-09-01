/*
 * Copyright (C) 2000-2004 Roger Butenuth, Andreas Gampe, Stefan Goetz, Sebastian Pappert, Klaas
 * Prause, Enno Rehling, Sebastian Tusk, Ulrich Kuester, Ilja Pavkovic This file is part of the
 * Eressea Java Code Base, see the file LICENSING for the licensing information applying to this
 * file.
 */

package magellan.client.actions.file;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.io.File;
import java.util.Collection;
import java.util.Properties;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;

import magellan.client.Client;
import magellan.client.actions.MenuAction;
import magellan.client.swing.EresseaFileFilter;
import magellan.client.swing.HistoryAccessory;
import magellan.library.utils.Resources;

/**
 * This action is called, if the user chooses the menu File > Open. This class loads a new CR into
 * Magellan.
 * 
 * @author Andreas
 * @version 1.0
 */
public class OpenCRAction extends MenuAction {

  /**
   * Creates new OpenCRAction
   * 
   * @param client
   */
  public OpenCRAction(Client client) {
    super(client);
  }

  /**
   * Called when the file->open menu is selected in order to open a certain cr file. Displays a file
   * chooser and loads the selected cr file.
   */
  @Override
  public void menuActionPerformed(ActionEvent e) {
    int response = client.askToSave();
    if (response == JOptionPane.CANCEL_OPTION) {
      return;
    }

    File file = OpenCRAction.getFileFromFileChooser(client);

    if (file != null)
      client.loadCRThread(response == JOptionPane.YES_OPTION, file);
  }

  /**
   * Shows the FileOpen Dialog of Magellan.
   * 
   * @param client The client for storing settings etc., which is also used as parent component.
   */
  public static File getFileFromFileChooser(Client client) {
    return OpenCRAction.getFileFromFileChooser(client, client);
  }

  /**
   * Shows the FileOpen Dialog of Magellan.
   * 
   * @param client The client for storing settings etc.
   * @param parent The parent component for the chooser.
   */
  public static File getFileFromFileChooser(Client client, Component parent) {
    JFileChooser fc = new JFileChooser();
    Properties settings = client.getProperties();
    fc.addChoosableFileFilter(new EresseaFileFilter(EresseaFileFilter.CR_FILTER));
    fc.addChoosableFileFilter(new EresseaFileFilter(EresseaFileFilter.GZ_FILTER));
    fc.addChoosableFileFilter(new EresseaFileFilter(EresseaFileFilter.BZ2_FILTER));
    fc.addChoosableFileFilter(new EresseaFileFilter(EresseaFileFilter.ZIP_FILTER));
    fc.addChoosableFileFilter(new EresseaFileFilter(EresseaFileFilter.ALLCR_FILTER));

    int lastFileFilter =
        Integer.parseInt(settings.getProperty("Client.lastSelectedOpenCRFileFilter", "5"));
    lastFileFilter = Math.min(fc.getChoosableFileFilters().length - 1, lastFileFilter);
    fc.setFileFilter(fc.getChoosableFileFilters()[lastFileFilter]);

    File file = new File(settings.getProperty("Client.lastCROpened", ""));
    fc.setSelectedFile(file);

    if (file.exists() || (file.getParentFile() != null && file.getParentFile().exists())) {
      fc.setCurrentDirectory(file.getParentFile());
    }

    fc.setSelectedFile(file);
    fc.setAccessory(new HistoryAccessory(settings, fc));
    fc.setDialogTitle(Resources.get("actions.opencraction.title"));

    if (fc.showOpenDialog(parent) == JFileChooser.APPROVE_OPTION) {
      // find selected FileFilter
      int i = 0;

      while (!fc.getChoosableFileFilters()[i].equals(fc.getFileFilter())) {
        i++;
      }

      settings.setProperty("Client.lastSelectedOpenCRFileFilter", String.valueOf(i));

      settings.setProperty("Client.lastCROpened", fc.getSelectedFile().getAbsolutePath());
      // TODO (stm) what if file does not exist?
      client.addFileToHistory(fc.getSelectedFile());

      boolean bOpenEqualsSave =
          Boolean.valueOf(settings.getProperty("Client.openEqualsSave", "false")).booleanValue();

      if (bOpenEqualsSave) {
        settings.setProperty("Client.lastCRSaved", fc.getSelectedFile().getAbsolutePath());
      }

      return fc.getSelectedFile();
    }
    return null;
  }

  private static class LoadCR implements Runnable {
    Client client;
    File file;
    Collection selectedObjects;

    /**
     * Creates a new LoadCR object for the given client and file. Reads GameData froma a file and
     * passes it to the specified client.
     * 
     * @param client The client to which the loaded data is passed.
     * @param file The name of the file containing the game data.
     */
    public LoadCR(Client client, File file) {
      this.client = client;
      this.file = file;
      this.selectedObjects = client.getSelectedObjects();
    }

    /*
     * (non-Javadoc)
     * @see java.lang.Runnable#run()
     */
    public void run() {
      client.loadCRThread(file);
    }
  }

  /**
   * @see magellan.client.actions.MenuAction#getAcceleratorTranslated()
   */
  @Override
  protected String getAcceleratorTranslated() {
    return Resources.get("actions.opencraction.accelerator", false);
  }

  /**
   * @see magellan.client.actions.MenuAction#getMnemonicTranslated()
   */
  @Override
  protected String getMnemonicTranslated() {
    return Resources.get("actions.opencraction.mnemonic", false);
  }

  /**
   * @see magellan.client.actions.MenuAction#getNameTranslated()
   */
  @Override
  protected String getNameTranslated() {
    return Resources.get("actions.opencraction.name");
  }

  @Override
  protected String getTooltipTranslated() {
    return Resources.get("actions.opencraction.tooltip", false);
  }
}