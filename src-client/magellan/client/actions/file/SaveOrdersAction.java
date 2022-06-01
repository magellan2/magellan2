/*
 * Copyright (C) 2000-2004 Roger Butenuth, Andreas Gampe, Stefan Goetz, Sebastian Pappert, Klaas
 * Prause, Enno Rehling, Sebastian Tusk, Ulrich Kuester, Ilja Pavkovic This file is part of the
 * Eressea Java Code Base, see the file LICENSING for the licensing information applying to this
 * file.
 */

package magellan.client.actions.file;

import java.awt.Dialog.ModalityType;
import java.awt.event.ActionEvent;

import javax.swing.JOptionPane;
import javax.swing.KeyStroke;

import magellan.client.Client;
import magellan.client.actions.MenuAction;
import magellan.client.swing.OrderWriterDialog;
import magellan.library.event.GameDataEvent;
import magellan.library.event.GameDataListener;
import magellan.library.utils.Resources;

/**
 * With the help of this action it is possible to save the given orders
 *
 * @author Andreas
 */
public class SaveOrdersAction extends MenuAction implements GameDataListener {
  private Mode mode;

  public enum Mode {
    /** show a dialog */
    DIALOG,
    /** save to given file */
    FILE,
    /** save to clipboard */
    CLIPBOARD,
    /** send a mail */
    MAIL,
    /** send to Eressea Server */
    PUT_ON_SERVER
  }

  /**
   * Creates new OpenCRAction
   *
   * @param client
   */
  public SaveOrdersAction(Client client, Mode mode) {
    super(client);
    this.mode = mode;

    init();
    setEnabled(false);
    client.getDispatcher().addGameDataListener(this);
  }

  /**
   * Simulates {@link MenuAction#MenuAction(Client)}.
   */
  private void init() {
    setName(getNameTranslated());

    setIcon(getIconName());

    if (getMnemonicTranslated() != null && !getMnemonicTranslated().trim().equals("")) {
      putValue("mnemonic", Character.valueOf(getMnemonicTranslated().charAt(0)));
    }

    if (getAcceleratorTranslated() != null && !getAcceleratorTranslated().trim().equals("")) {
      putValue("accelerator", KeyStroke.getKeyStroke(getAcceleratorTranslated()));
    }

    if (getTooltipTranslated() != null && !getTooltipTranslated().trim().equals("")) {
      putValue("tooltip", getTooltipTranslated());
    }
  }

  /**
   *
   */
  @Override
  public void menuActionPerformed(ActionEvent e) {
    switch (mode) {
    case DIALOG:
      showDialog();
      break;
    case FILE:
      execSave();
      break;
    case CLIPBOARD:
      execClipboard();
      break;
    case MAIL:
      execMail();
      break;
    case PUT_ON_SERVER:
      execPutOnServer();
      break;
    }
  }

  private void showDialog() {
    if (!OrderWriterDialog.canShow(client.getData())) {
      JOptionPane.showMessageDialog(client, Resources.get("actions.saveordersaction.dialog.nofaction.message"));
    } else {
      OrderWriterDialog d = new OrderWriterDialog(client, false, client.getData(), client.getProperties(), client
          .getSelectedRegions().values());
      d.setModalityType(ModalityType.DOCUMENT_MODAL);
      d.setVisible(true);
    }
  }

  private void execMail() {
    if (!OrderWriterDialog.canShow(client.getData())) {
      JOptionPane.showMessageDialog(client, Resources.get("actions.saveordersaction.dialog.nofaction.message"));
    } else if (!new OrderWriterDialog(client, true, client.getData(), client.getProperties(), client
        .getSelectedRegions().values()).runMail()) {
      JOptionPane.showMessageDialog(client, Resources.get("actions.saveordersaction.action.error.message"), Resources
          .get("actions.saveordersaction.action.error.title"), JOptionPane.WARNING_MESSAGE);
    }
  }

  private void execSave() {
    if (!OrderWriterDialog.canShow(client.getData())) {
      JOptionPane.showMessageDialog(client, Resources.get("actions.saveordersaction.dialog.nofaction.message"));
    } else if (!new OrderWriterDialog(client, true, client.getData(), client.getProperties(), client
        .getSelectedRegions().values()).runSave()) {
      JOptionPane.showMessageDialog(client, Resources.get("actions.saveordersaction.action.error.message"), Resources
          .get("actions.saveordersaction.action.error.title"), JOptionPane.WARNING_MESSAGE);
    }
  }

  private void execPutOnServer() {
    if (!OrderWriterDialog.canShow(client.getData())) {
      JOptionPane.showMessageDialog(client, Resources.get("actions.saveordersaction.dialog.nofaction.message"));
    } else if (!new OrderWriterDialog(client, true, client.getData(), client.getProperties(), client
        .getSelectedRegions().values()).runPutOnServer()) {
      JOptionPane.showMessageDialog(client, Resources.get("actions.saveordersaction.action.error.message"), Resources
          .get("actions.saveordersaction.action.error.title"), JOptionPane.WARNING_MESSAGE);
    }
  }

  private void execClipboard() {
    if (!OrderWriterDialog.canShow(client.getData())) {
      JOptionPane.showMessageDialog(client, Resources.get("actions.saveordersaction.dialog.nofaction.message"));
    } else {
      if (!new OrderWriterDialog(client, true, client.getData(), client.getProperties(), client.getSelectedRegions()
          .values()).runClipboard()) {
        JOptionPane.showMessageDialog(client, Resources.get("actions.saveordersaction.action.error.message"), Resources
            .get("actions.saveordersaction.action.error.title"),
            JOptionPane.WARNING_MESSAGE);
      }
    }
  }

  /**
   * @see magellan.library.event.GameDataListener#gameDataChanged(magellan.library.event.GameDataEvent)
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
    if (mode == null)
      return "default"; // not yet initialized
    return Resources.get("actions.saveordersaction.accelerator." + String.valueOf(mode), false);
  }

  /**
   * @see magellan.client.actions.MenuAction#getMnemonicTranslated()
   */
  @Override
  protected String getMnemonicTranslated() {
    if (mode == null)
      return "default"; // not yet initialized
    return Resources.get("actions.saveordersaction.mnemonic." + String.valueOf(mode), false);
  }

  /**
   * @see magellan.client.actions.MenuAction#getNameTranslated()
   */
  @Override
  protected String getNameTranslated() {
    if (mode == null)
      return "default"; // not yet initialized
    return Resources.get("actions.saveordersaction.name." + String.valueOf(mode));
  }

  /**
   * @see magellan.client.actions.MenuAction#getTooltipTranslated()
   */
  @Override
  protected String getTooltipTranslated() {
    if (mode == null)
      return "default"; // not yet initialized
    return Resources.get("actions.saveordersaction.tooltip." + String.valueOf(mode), false);
  }
}
