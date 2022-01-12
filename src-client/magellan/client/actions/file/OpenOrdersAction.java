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
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.Properties;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;

import magellan.client.Client;
import magellan.client.actions.MenuAction;
import magellan.client.desktop.MagellanDesktop;
import magellan.client.swing.EresseaFileFilter;
import magellan.client.swing.OpenOrdersAccessory;
import magellan.client.swing.ProgressBarUI;
import magellan.library.event.GameDataEvent;
import magellan.library.event.GameDataListener;
import magellan.library.gamebinding.GameSpecificOrderReader;
import magellan.library.gamebinding.GameSpecificOrderReader.Status;
import magellan.library.io.BOMReader;
import magellan.library.io.file.FileType;
import magellan.library.utils.Encoding;
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
    Properties settings = client.getProperties();
    JFileChooser fc = new JFileChooser();
    fc.setFileFilter(new EresseaFileFilter(EresseaFileFilter.TXT_ORDERS_FILTER));
    fc.setSelectedFile(new File(settings.getProperty("Client.lastOrdersOpened", "")));

    OpenOrdersAccessory acc = new OpenOrdersAccessory(settings, fc);
    fc.setAccessory(acc);

    if (fc.showOpenDialog(client) == JFileChooser.APPROVE_OPTION) {
      loadAsynchronously(acc, fc);
    }

    // repaint since command confirmation status may have changed
    client.getDesktop().repaint(MagellanDesktop.OVERVIEW_IDENTIFIER);
  }

  protected void loadAsynchronously(final OpenOrdersAccessory acc, final JFileChooser fc) {
    final ProgressBarUI ui = new ProgressBarUI(client);

    ui.show();
    ui.setMaximum(-1);

    new Thread(new Runnable() {

      public void run() {
        Properties settings = client.getProperties();
        settings.setProperty("Client.lastOrdersOpened", fc.getSelectedFile().getAbsolutePath());

        try {
          GameSpecificOrderReader r =
              client.getData().getRules().getGameSpecificStuff().getOrderReader(client.getData());
          StringBuilder messageS;
          Status status = new Status();
          if (r == null) {
            log.warn("OrderReader not available");
            messageS =
                new StringBuilder(Resources
                    .get("actions.openordersaction.msg.fileordersopen.status.noreader"));
          } else {
            r.setAutoConfirm(acc.getAutoConfirm());
            r.setIgnoreSemicolonComments(acc.getIgnoreSemicolonComments());
            r.setDoNotOverwriteConfirmedOrders(acc.getDoNotOverwriteConfirmedOrders());

            // apexo (Fiete) 20061205: if in properties, force ISO encoding
            if (PropertiesHelper.getBoolean(settings, "TextEncoding.ISOopenOrders", false)) {
              // new: force our default = ISO
              Reader stream =
                  new InputStreamReader(
                      new FileInputStream(fc.getSelectedFile().getAbsolutePath()),
                      FileType.DEFAULT_ENCODING.toString());
              r.read(stream);
            } else if (PropertiesHelper.getBoolean(settings, "TextEncoding.UTFopenOrders", true)) {
              // new: force UTF
              Reader stream =
                  new InputStreamReader(
                      new FileInputStream(fc.getSelectedFile().getAbsolutePath()), Encoding.UTF8
                          .toString());
              r.read(stream);
            } else {
              // old = default = system dependent
              r.read(new BOMReader(new FileInputStream(fc.getSelectedFile()), null));
              // r.read(new FileReader(fc.getSelectedFile().getAbsolutePath()));
            }

            status = r.getStatus();
            messageS =
                new StringBuilder(Resources.get(
                    "actions.openordersaction.msg.fileordersopen.status.text", status.factions,
                    status.units));
            if (status.unknownUnits > 0) {
              messageS.append("\n").append(
                  Resources.get("actions.openordersaction.msg.fileordersopen.status.text3", Integer
                      .valueOf(status.unknownUnits)));
            }
            if (status.confirmedUnitsNotOverwritten > 0) {
              messageS.append("\n").append(status.confirmedUnitsNotOverwritten).append(" ").append(
                  Resources.get("actions.openordersaction.msg.fileordersopen.status.text2"));
            }
            if (status.errors > 0) {
              messageS.append("\n").append(
                  Resources.get("actions.openordersaction.msg.fileordersopen.status.errors",
                      status.errors));
            }
          }
          JOptionPane.showMessageDialog(client, messageS.toString(), Resources
              .get("actions.openordersaction.msg.fileordersopen.status.title"),
              (status.factions > 0 && status.units > 0) ? JOptionPane.PLAIN_MESSAGE
                  : JOptionPane.WARNING_MESSAGE);
          // in order to refresh relations, force a complete new init of the game data, using
          // data.clone, using for that client.setOrigin...(Fiete)
          // client.setOrigin(CoordinateID.ZERO);

          // setOrigin and set Data already fire a GameDataEvent
          // client.getDispatcher().fire(new GameDataEvent(this, client.getData()));

          // (stm) this should be sufficient, as long as unit relations are (forcedly) refreshed via
          // postProcess in OrderReader
          client.setData(client.getData());
          client.setReportChanged(true);
        } catch (Throwable exc) {
          OpenOrdersAction.log.error(exc);
          JOptionPane.showMessageDialog(client, Resources
              .get("actions.openordersaction.msg.fileordersopen.error.text")
              + exc.toString(), Resources
                  .get("actions.openordersaction.msg.fileordersopen.error.title"),
              JOptionPane.ERROR_MESSAGE);
        } finally {
          ui.ready();
        }
      }
    }).start();
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.eressea.event.GameDataListener#gameDataChanged(com.eressea.event.GameDataEvent)
   */
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
    return Resources.get("actions.openordersaction.accelerator", false);
  }

  /**
   * @see magellan.client.actions.MenuAction#getMnemonicTranslated()
   */
  @Override
  protected String getMnemonicTranslated() {
    return Resources.get("actions.openordersaction.mnemonic", false);
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
    return Resources.get("actions.openordersaction.tooltip", false);
  }

}
