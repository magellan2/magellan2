/*
 * Copyright (C) 2000-2004 Roger Butenuth, Andreas Gampe, Stefan Goetz, Sebastian Pappert, Klaas
 * Prause, Enno Rehling, Sebastian Tusk, Ulrich Kuester, Ilja Pavkovic This file is part of the
 * Eressea Java Code Base, see the file LICENSING for the licensing information applying to this
 * file.
 */

package magellan.client.actions.file;

import java.awt.event.ActionEvent;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.swing.JOptionPane;
import javax.swing.KeyStroke;

import magellan.client.Client;
import magellan.client.actions.MenuAction;
import magellan.client.desktop.DesktopEnvironment;
import magellan.client.desktop.ShortcutListener;
import magellan.client.swing.OrderWriterDialog;
import magellan.library.event.GameDataEvent;
import magellan.library.event.GameDataListener;
import magellan.library.utils.Resources;

/**
 * With the help of this action it is possible to save the given orders
 * 
 * @author Andreas
 */
public class SaveOrdersAction extends MenuAction implements ShortcutListener, GameDataListener {
  private List<KeyStroke> shortCuts;
  private Mode mode;

  public enum Mode {
    DIALOG, FILE, CLIPBOARD, MAIL
  }

  /**
   * Creates new OpenCRAction
   * 
   * @param client
   */
  public SaveOrdersAction(Client client, Mode mode) {
    super(client);
    this.mode = mode;
    shortCuts = new ArrayList<KeyStroke>(1);
    switch (mode) {
    case DIALOG:
      shortCuts.add(KeyStroke.getKeyStroke(KeyEvent.VK_D, InputEvent.CTRL_MASK
          | InputEvent.SHIFT_MASK));
      break;
    case FILE:
      shortCuts.add(KeyStroke.getKeyStroke(KeyEvent.VK_O, InputEvent.CTRL_MASK
          | InputEvent.SHIFT_MASK));
      break;
    case CLIPBOARD:
      shortCuts.add(KeyStroke.getKeyStroke(KeyEvent.VK_Z, InputEvent.CTRL_MASK
          | InputEvent.SHIFT_MASK));
      break;
    case MAIL:
      shortCuts.add(KeyStroke.getKeyStroke(KeyEvent.VK_M, InputEvent.CTRL_MASK
          | InputEvent.SHIFT_MASK));

      break;
    }
    DesktopEnvironment.registerShortcutListener(this);
    init();

    setEnabled(false);
    client.getDispatcher().addGameDataListener(this);
  }

  private void init() {
    this.setName(getNameTranslated());

    this.setIcon(getIconName());

    if (getMnemonicTranslated() != null && !getMnemonicTranslated().trim().equals("")) {
      this.putValue("mnemonic", new Character(getMnemonicTranslated().charAt(0)));
    }

    if (getAcceleratorTranslated() != null && !getAcceleratorTranslated().trim().equals("")) {
      this.putValue("accelerator", KeyStroke.getKeyStroke(getAcceleratorTranslated()));
    }

    if (getTooltipTranslated() != null && !getTooltipTranslated().trim().equals("")) {
      this.putValue("tooltip", getTooltipTranslated());
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
    }
  }

  private void showDialog() {
    OrderWriterDialog d =
        new OrderWriterDialog(client, true, client.getData(), client.getProperties(), client
            .getSelectedRegions().values());
    d.setVisible(true);
  }

  private void execMail() {
    if (!new OrderWriterDialog(client, true, client.getData(), client.getProperties(), client
        .getSelectedRegions().values()).runMail())
      JOptionPane.showMessageDialog(client, Resources
          .get("actions.saveordersaction.action.error.message"), Resources
          .get("actions.saveordersaction.action.error.title"), JOptionPane.WARNING_MESSAGE);
  }

  private void execSave() {
    if (!new OrderWriterDialog(client, true, client.getData(), client.getProperties(), client
        .getSelectedRegions().values()).runSave())
      JOptionPane.showMessageDialog(client, Resources
          .get("actions.saveordersaction.action.error.message"), Resources
          .get("actions.saveordersaction.action.error.title"), JOptionPane.WARNING_MESSAGE);
  }

  private void execClipboard() {
    if (!new OrderWriterDialog(client, true, client.getData(), client.getProperties(), client
        .getSelectedRegions().values()).runClipboard())
      JOptionPane.showMessageDialog(client, Resources
          .get("actions.saveordersaction.action.error.message"), Resources
          .get("actions.saveordersaction.action.error.title"), JOptionPane.WARNING_MESSAGE);
  }

  /**
   * @see magellan.client.desktop.ShortcutListener#shortCut(javax.swing.KeyStroke)
   */
  public void shortCut(KeyStroke shortcut) {
    if (shortCuts.indexOf(shortcut) < 0)
      return;

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
    }
  }

  /**
   * Should return all short cuts this class want to be informed. The elements should be of type
   * javax.swing.KeyStroke
   */
  public Iterator<KeyStroke> getShortCuts() {
    return shortCuts.iterator();
  }

  /**
   * 
   */
  public String getShortcutDescription(java.lang.Object obj) {
    return Resources.get("actions.saveordersaction.shortcuts.description." + String.valueOf(mode));
  }

  /**
   * 
   */
  public java.lang.String getListenerDescription() {
    return Resources.get("actions.saveordersaction.shortcuts.title");
  }

  /**
   * @see magellan.library.event.GameDataListener#gameDataChanged(magellan.library.event.GameDataEvent)
   */
  public void gameDataChanged(GameDataEvent e) {
    int i = e.getGameData().regions().size();
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
    return Resources.get("actions.saveordersaction.accelerator." + String.valueOf(mode), false);
  }

  /**
   * @see magellan.client.actions.MenuAction#getMnemonicTranslated()
   */
  @Override
  protected String getMnemonicTranslated() {
    return Resources.get("actions.saveordersaction.mnemonic." + String.valueOf(mode), false);
  }

  /**
   * @see magellan.client.actions.MenuAction#getNameTranslated()
   */
  @Override
  protected String getNameTranslated() {
    return Resources.get("actions.saveordersaction.name." + String.valueOf(mode));
  }

  /**
   * @see magellan.client.actions.MenuAction#getTooltipTranslated()
   */
  @Override
  protected String getTooltipTranslated() {
    return Resources.get("actions.saveordersaction.tooltip." + String.valueOf(mode), false);
  }
}
