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

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Frame;
import java.util.Collection;
import java.util.Properties;

import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

import magellan.client.event.EventDispatcher;
import magellan.client.utils.SwingUtils;
import magellan.library.GameData;
import magellan.library.Region;
import magellan.library.utils.Resources;

/**
 * A dialog wrapper for the ECheck panel.
 */
public class ECheckDialog extends InternationalizedDataDialog {
  private ECheckPanel pnlECheck = null;

  /**
   * Create a new ECheckDialog object as a dialog with a parent window.
   */
  public ECheckDialog(Frame owner, boolean modal, EventDispatcher ed, GameData initData,
      Properties p) {
    this(owner, modal, ed, initData, p, null);
  }

  /**
   * Create a new ECheckDialog object as a dialog with a parent window.
   */
  public ECheckDialog(Frame owner, boolean modal, EventDispatcher ed, GameData initData,
      Properties p, Collection<Region> regions) {
    super(owner, modal, ed, initData, p);
    init(regions);
  }

  private void init(Collection<Region> regions) {
    if (regions == null) {
      pnlECheck = new ECheckPanel(dispatcher, data, settings);
    } else {
      pnlECheck = new ECheckPanel(dispatcher, data, settings, regions);
    }

    setContentPane(getMainPane());
    setTitle(Resources.get("echeckdialog.window.title"));

    int width = Integer.parseInt(settings.getProperty("ECheckDialog.width", "500"));
    int height = Integer.parseInt(settings.getProperty("ECheckDialog.height", "300"));
    this.setSize(width, height);

    SwingUtils.setLocation(this, settings, "ECheckDialog.x", "ECheckDialog.y");

    pnlECheck.setSelRegionsOnly(Boolean.valueOf(
        settings.getProperty("ECheckDialog.includeSelRegionsOnly", "false")).booleanValue());
    pnlECheck.setConfirmedOnly(Boolean.valueOf(
        settings.getProperty("ECheckDialog.confirmedOnly", "false")).booleanValue());
  }

  private Container getMainPane() {
    JPanel mainPanel = new JPanel(new BorderLayout(6, 0));
    mainPanel.setBorder(new EmptyBorder(4, 4, 4, 4));
    mainPanel.add(pnlECheck, BorderLayout.CENTER);

    return mainPanel;
  }

  private void storeSettings() {
    settings.setProperty("ECheckDialog.x", String.valueOf(getX()));
    settings.setProperty("ECheckDialog.y", String.valueOf(getY()));
    settings.setProperty("ECheckDialog.width", String.valueOf(getWidth()));
    settings.setProperty("ECheckDialog.height", String.valueOf(getHeight()));
    settings.setProperty("ECheckDialog.includeSelRegionsOnly", String.valueOf(pnlECheck
        .getSelRegionsOnly()));
    settings
        .setProperty("ECheckDialog.confirmedOnly", String.valueOf(pnlECheck.getConfirmedOnly()));
  }

  @Override
  protected void quit() {
    storeSettings();
    pnlECheck.quit();
    super.quit();
  }

  /**
   * DOCUMENT-ME
   */
  public void exec() {
    pnlECheck.runECheck();
  }
}
