/*
 * Copyright (C) 2000-2004 Roger Butenuth, Andreas Gampe, Stefan Goetz, Sebastian Pappert, Klaas
 * Prause, Enno Rehling, Sebastian Tusk, Ulrich Kuester, Ilja Pavkovic This file is part of the
 * Eressea Java Code Base, see the file LICENSING for the licensing information applying to this
 * file.
 */

package magellan.client.actions.map;

import java.awt.event.ActionEvent;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.LinkedList;
import java.util.List;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;

import magellan.client.Client;
import magellan.client.swing.EresseaFileFilter;
import magellan.library.CoordinateID;
import magellan.library.utils.Resources;
import magellan.library.utils.logging.Logger;

/**
 * Lets the user select a file with a previously saved selection.
 * 
 * @author Ilja Pavkovic
 */
public class OpenSelectionAction extends AbstractSelectionAction {
  private static final Logger log = Logger.getInstance(OpenSelectionAction.class);

  /**
   * Creates a new OpenSelectionAction object.
   */
  public OpenSelectionAction(Client client) {
    super(client);
  }

  /**
   * Clears the selection. Will be overriden in AddSelectionAction.
   */
  protected void preSetCleanSelection() {
    getSelectedRegions().clear();
  }

  /**
   * The name of the "last file opened" property in the Magellan properties file.
   */
  protected String getPropertyName() {
    return "Client.lastSELOpened";
  }

  /**
   * Opens a file dialog, changes selection and fires SelectionEvent.
   * 
   * @see magellan.client.actions.MenuAction#menuActionPerformed(java.awt.event.ActionEvent)
   */
  @Override
  public void menuActionPerformed(ActionEvent e) {
    JFileChooser fc = new JFileChooser();
    fc.setFileFilter(new EresseaFileFilter(SaveSelectionAction.EXTENSION, Resources
        .get("actions.saveselectionaction.selectionfilter.name")));
    fc.setSelectedFile(new File(client.getProperties().getProperty(getPropertyName(), "")));
    fc.setDialogTitle(Resources.get("actions.openselectionaction.title"));

    if (fc.showOpenDialog(client) == JFileChooser.APPROVE_OPTION) {
      client.getProperties().setProperty(getPropertyName(), fc.getSelectedFile().getAbsolutePath());

      List<CoordinateID> coordinates = new LinkedList<CoordinateID>();

      try {
        BufferedReader br = new BufferedReader(new FileReader(fc.getSelectedFile()));

        while (true) {
          String line = br.readLine();

          if (line == null) {
            break;
          }

          if (line.indexOf(SaveSelectionAction.COMMENT) != -1) {
            // remove trailing comment
            line = line.substring(0, line.indexOf(SaveSelectionAction.COMMENT));
          }

          coordinates.add(CoordinateID.parse(line, SaveSelectionAction.DELIMITER));
        }

        br.close();
      } catch (Exception exc) {
        OpenSelectionAction.log.error(exc);
        JOptionPane.showMessageDialog(client, Resources
            .get("actions.openselectionaction.msg.fileordersopen.error.text")
            + e.toString(), Resources
            .get("actions.openselectionaction.msg.fileordersopen.error.title"),
            JOptionPane.ERROR_MESSAGE);
      }

      // load successful, now fill up selection
      preSetCleanSelection();

      for (CoordinateID c : coordinates) {
        if (client.getData().getRegion(c) != null) {
          getSelectedRegions().put(c, client.getData().getRegion(c));
        }
      }

      // fire change event
      updateClientSelection();
    }
  }

}
