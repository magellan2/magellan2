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

package magellan.client.actions.extras;

import java.awt.event.ActionEvent;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import magellan.client.Client;
import magellan.client.actions.MenuAction;
import magellan.client.swing.preferences.PreferencesDialog;
import magellan.client.swing.preferences.PreferencesFactory;
import magellan.library.utils.Resources;
import magellan.library.utils.logging.Logger;

/**
 * DOCUMENT ME!
 * 
 * @author Andreas Gampe
 * @author Ilja Pavkovic
 * @version $Revision: 305 $
 */
public class OptionAction extends MenuAction {
  private static final Logger log = Logger.getInstance(OptionAction.class);

  private List<PreferencesFactory> adapters;

  /**
   * This timer object is used to rebuild the PreferencesDialog in background. If the
   * actionPerformed method is called the dialog will be discarded and recreated
   */
  private Timer t;

  /**
   * Creates a new OptionAction object.
   * 
   * @param client
   * @param adapters Either PreferencesAdapter or PreferencesFactory (???)
   */
  public OptionAction(Client client, List<PreferencesFactory> adapters) {
    super(client);
    this.adapters = adapters;
    initTimer();
  }

  PreferencesDialog dialog = null;

  /**
   * DOCUMENT-ME
   */
  @Override
  public void menuActionPerformed(ActionEvent e) {
    if (dialog == null) {
      buildDialog();
    }

    dialog.setVisible(true);
    dialog = null;
  }

  private void initTimer() {
    t = new Timer("OptionScheduler", true);
    t.scheduleAtFixedRate(new TimerTask() {
      @Override
      public void run() {
        buildDialog();
      }
    }, 1000, 1000);
  }

  private void buildDialog() {
    if (dialog != null)
      return;

    PreferencesDialog pd = new PreferencesDialog(client, true, client.getProperties(), adapters);

    if (dialog == null) {
      dialog = pd;
    }
  }

  /**
   * DOCUMENT-ME
   */
  public void updateLaF() {
    if (dialog != null) {
      dialog.updateLaF();
    }
  }

  /**
   * @see magellan.client.actions.MenuAction#getAcceleratorTranslated()
   */
  @Override
  protected String getAcceleratorTranslated() {
    return Resources.get("actions.optionaction.accelerator", false);
  }

  /**
   * @see magellan.client.actions.MenuAction#getMnemonicTranslated()
   */
  @Override
  protected String getMnemonicTranslated() {
    return Resources.get("actions.optionaction.mnemonic", false);
  }

  /**
   * @see magellan.client.actions.MenuAction#getNameTranslated()
   */
  @Override
  protected String getNameTranslated() {
    return Resources.get("actions.optionaction.name");
  }

  @Override
  protected String getTooltipTranslated() {
    return Resources.get("actions.optionaction.tooltip", false);
  }
}
