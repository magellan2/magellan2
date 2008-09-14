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
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.Properties;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;

import magellan.client.Client;
import magellan.client.EMapOverviewPanel;
import magellan.client.actions.MenuAction;
import magellan.client.swing.EresseaFileFilter;
import magellan.client.swing.OpenOrdersAccessory;
import magellan.library.CoordinateID;
import magellan.library.event.GameDataEvent;
import magellan.library.event.GameDataListener;
import magellan.library.io.file.FileType;
import magellan.library.utils.OrderReader;
import magellan.library.utils.PropertiesHelper;
import magellan.library.utils.Resources;
import magellan.library.utils.logging.Logger;


/**
 * DOCUMENT ME!
 *
 * @author Andreas
 * @version 1.0
 */
public class OpenOrdersAction extends MenuAction implements GameDataListener {
	private static final Logger log = Logger.getInstance(OpenOrdersAction.class);

	/**
	 * Creates a new OpenOrdersAction object.
	 *
	 * @param client
	 */
	public OpenOrdersAction(Client client) {
        super(client);
        setEnabled(false);
        client.getDispatcher().addGameDataListener(this);
	}

	/**
   * Asks for a txt file to be opened and tries to add the orders from it.
   */
  @Override
  public void menuActionPerformed(ActionEvent e) {
    JFileChooser fc = new JFileChooser();
    Properties settings = client.getProperties();
    fc.addChoosableFileFilter(new EresseaFileFilter(EresseaFileFilter.TXT_FILTER));
    fc.setSelectedFile(new File(settings.getProperty("Client.lastOrdersOpened", "")));

    OpenOrdersAccessory acc = new OpenOrdersAccessory(settings, fc);
    fc.setAccessory(acc);

    if (fc.showOpenDialog(client) == JFileChooser.APPROVE_OPTION) {
      settings.setProperty("Client.lastOrdersOpened", fc.getSelectedFile().getAbsolutePath());

      OrderReader r = new OrderReader(client.getData());
      r.setAutoConfirm(acc.getAutoConfirm());
      r.ignoreSemicolonComments(acc.getIgnoreSemicolonComments());
      r.setDoNotOverwriteConfirmedOrders(acc.getDoNotOverwriteConfirmedOrders());

      // we clone later the hole gamedata, we do not need to
      // refresh the UnitRelations now
      r.setRefreshUnitRelations(false);

      try {
        // apexo (Fiete) 20061205: if in properties, force ISO encoding
        if (!PropertiesHelper.getBoolean(settings, "TextEncoding.ISOopenOrders", false)) {
          // old = default = system dependent
          r.read(new FileReader(fc.getSelectedFile().getAbsolutePath()));
        } else {
          // new: force our default = ISO
          Reader stream =
              new InputStreamReader(new FileInputStream(fc.getSelectedFile().getAbsolutePath()),
                  FileType.DEFAULT_ENCODING.toString());
          r.read(stream);
        }

        /**
         * we do not need the refresh anymore... we clone the hole gamedata
         * later... // OrderReaderPatch010207 stm (manually by Fiete): if
         * (client.getData().regions()!=null){ // added by fiete to be failsafe
         * for (Iterator it =
         * client.getData().regions().values().iterator();it.hasNext();){ Region
         * region = (Region)it.next(); region.refreshUnitRelations(true); } } //
         * OrderReaderPatch end
         */

        OrderReader.Status status = r.getStatus();
        Object msgArgs[] = { new Integer(status.factions), new Integer(status.units) };
        String messageS = Resources.get("actions.openordersaction.msg.fileordersopen.status.text");
        if (status.confirmedUnitsNotOverwritten > 0) {
          messageS +=
              "\n" + status.confirmedUnitsNotOverwritten + " "
                  + Resources.get("actions.openordersaction.msg.fileordersopen.status.text2");
        }
        JOptionPane.showMessageDialog(client, (new java.text.MessageFormat(messageS))
            .format(msgArgs), Resources
            .get("actions.openordersaction.msg.fileordersopen.status.title"),
            (status.factions > 0 && status.units > 0) ? JOptionPane.PLAIN_MESSAGE
                : JOptionPane.WARNING_MESSAGE);
      } catch (Exception exc) {
        OpenOrdersAction.log.error(exc);
        JOptionPane.showMessageDialog(client, Resources
            .get("actions.openordersaction.msg.fileordersopen.error.text")
            + exc.toString(), Resources
            .get("actions.openordersaction.msg.fileordersopen.error.title"),
            JOptionPane.ERROR_MESSAGE);
      }

      // client.getDispatcher().fire(new GameDataEvent(client,
      // client.getData()));
      // force a complete new init of the game data, using data.clone
      // using for that client.setOrigin...(Fiete)
      this.client.setOrigin(new CoordinateID(0, 0));
    }

		// repaint since command confirmation status may have changed
		client.getDesktop().repaint(EMapOverviewPanel.IDENTIFIER);
	}

	/* (non-Javadoc)
	 * @see com.eressea.event.GameDataListener#gameDataChanged(com.eressea.event.GameDataEvent)
	 */
	public void gameDataChanged(GameDataEvent e) {
		int i = e.getGameData().regions().size();
		if (i>0) {
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
    return Resources.get("actions.openordersaction.accelerator",false);
  }

  /**
   * @see magellan.client.actions.MenuAction#getMnemonicTranslated()
   */
  @Override
  protected String getMnemonicTranslated() {
    return Resources.get("actions.openordersaction.mnemonic",false);
  }

  /**
   * @see magellan.client.actions.MenuAction#getNameTranslated()
   */
  @Override
  protected String getNameTranslated() {
    return Resources.get("actions.openordersaction.name");
  }

  @Override
  protected String getTooltipTranslated() {
    return Resources.get("actions.openordersaction.tooltip",false);
  }
  
}
