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

package magellan.client.preferences;

import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.Properties;

import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import magellan.client.swing.preferences.PreferencesAdapter;
import magellan.library.utils.PropertiesHelper;
import magellan.library.utils.Resources;

/**
 * This preferences dialog configures the textfile encoding properties of Magellan.
 * 
 * @author Fiete
 * @version 1.0
 */
public class ClientTextEncodingPreferences extends AbstractPreferencesAdapter implements
    PreferencesAdapter {

  static Properties settings;
  protected JCheckBox saveISOOrders;
  protected JCheckBox openISOOrders;
  protected JCheckBox openUTFOrders;
  protected JCheckBox runISOEcheck;
  protected JCheckBox runJVorlage;
  protected JCheckBox saveUTFOrders;
  protected JCheckBox runUTFEcheck;

  public ClientTextEncodingPreferences(Properties _settings) {
    ClientTextEncodingPreferences.settings = _settings;
    initGUI();
  }

  /**
   * @see PreferencesAdapter#initPreferences()
   */
  public void initPreferences() {
    // nothing to do...

  }

  private void initGUI() {
    getTextEncodingPrefrencesPanel(addPanel(Resources
        .get("util.textencodingpreferences.prefs.title")));
  }

  /**
   * 
   */
  private Component getTextEncodingPrefrencesPanel(JPanel textEncodingPrefrencesPanel) {
    textEncodingPrefrencesPanel.setLayout(new GridBagLayout());

    GridBagConstraints c =
        new GridBagConstraints(0, 0, 2, 1, 1, 0, GridBagConstraints.WEST,
            GridBagConstraints.HORIZONTAL, new Insets(2, 10, 1, 10), 0, 0);

    JLabel textEncodingInfoLabel =
        new JLabel(Resources.get("util.textencodingpreferences.prefs.info1"));
    textEncodingPrefrencesPanel.add(textEncodingInfoLabel, c);

    c.gridy++;
    textEncodingInfoLabel = new JLabel(Resources.get("util.textencodingpreferences.prefs.info2"));
    textEncodingPrefrencesPanel.add(textEncodingInfoLabel, c);

    c.gridy++;
    openISOOrders =
        new JCheckBox(Resources
            .get("util.textencodingpreferences.checkbox.textEncodingISOopenOrders.label"),
            PropertiesHelper.getBoolean(ClientTextEncodingPreferences.settings,
                "TextEncoding.ISOopenOrders", false));
    textEncodingPrefrencesPanel.add(openISOOrders, c);

    c.gridy++;
    openUTFOrders =
        new JCheckBox(Resources
            .get("util.textencodingpreferences.checkbox.textEncodingUTFopenOrders.label"),
            PropertiesHelper.getBoolean(ClientTextEncodingPreferences.settings,
                "TextEncoding.UTFopenOrders", true));
    textEncodingPrefrencesPanel.add(openUTFOrders, c);

    c.gridy++;
    saveISOOrders =
        new JCheckBox(Resources
            .get("util.textencodingpreferences.checkbox.textEncodingISOsaveOrders.label"),
            PropertiesHelper.getBoolean(ClientTextEncodingPreferences.settings,
                "TextEncoding.ISOsaveOrders", false));
    textEncodingPrefrencesPanel.add(saveISOOrders, c);

    // TextEncoding.UTF8saveOrders
    c.gridy++;
    saveUTFOrders =
        new JCheckBox(Resources
            .get("util.textencodingpreferences.checkbox.textEncodingUTFsaveOrders.label"),
            PropertiesHelper.getBoolean(ClientTextEncodingPreferences.settings,
                "TextEncoding.UTF8saveOrders", true));
    textEncodingPrefrencesPanel.add(saveUTFOrders, c);

    c.gridy++;
    runISOEcheck =
        new JCheckBox(Resources
            .get("util.textencodingpreferences.checkbox.textEncodingISOECheck.label"),
            PropertiesHelper.getBoolean(ClientTextEncodingPreferences.settings,
                "TextEncoding.ISOrunEcheck", false));
    textEncodingPrefrencesPanel.add(runISOEcheck, c);

    c.gridy++;
    runUTFEcheck =
        new JCheckBox(Resources
            .get("util.textencodingpreferences.checkbox.textEncodingUTFECheck.label"),
            PropertiesHelper.getBoolean(ClientTextEncodingPreferences.settings,
                "TextEncoding.UTF8runEcheck", true));
    textEncodingPrefrencesPanel.add(runUTFEcheck, c);

    c.gridy++;
    runJVorlage =
        new JCheckBox(Resources
            .get("util.textencodingpreferences.checkbox.textEncodingISOJVorlage.label"),
            PropertiesHelper.getBoolean(ClientTextEncodingPreferences.settings,
                "TextEncoding.ISOrunJVorlage", false));
    runJVorlage.setEnabled(false);
    textEncodingPrefrencesPanel.add(runJVorlage, c);

    if (saveISOOrders.isSelected()) {
      saveUTFOrders.setEnabled(false);
      saveUTFOrders.setSelected(false);
    } else if (saveUTFOrders.isSelected()) {
      saveISOOrders.setEnabled(false);
      saveISOOrders.setSelected(false);
    }

    if (openISOOrders.isSelected()) {
      openUTFOrders.setEnabled(false);
      openUTFOrders.setSelected(false);
    } else if (openUTFOrders.isSelected()) {
      openISOOrders.setEnabled(false);
      openISOOrders.setSelected(false);
    }

    if (runISOEcheck.isSelected()) {
      runUTFEcheck.setEnabled(false);
      runUTFEcheck.setSelected(false);
    } else if (runUTFEcheck.isSelected()) {
      runISOEcheck.setEnabled(false);
      runISOEcheck.setSelected(false);
    }

    saveISOOrders.addChangeListener(new ChangeListener() {
      public void stateChanged(ChangeEvent e) {
        if (saveISOOrders.isSelected()) {
          saveUTFOrders.setEnabled(false);
          saveUTFOrders.setSelected(false);
        } else {
          saveUTFOrders.setEnabled(true);
        }
      }
    });

    saveUTFOrders.addChangeListener(new ChangeListener() {
      public void stateChanged(ChangeEvent e) {
        if (saveUTFOrders.isSelected()) {
          saveISOOrders.setEnabled(false);
          saveISOOrders.setSelected(false);
        } else {
          saveISOOrders.setEnabled(true);
        }
      }
    });

    openISOOrders.addChangeListener(new ChangeListener() {
      public void stateChanged(ChangeEvent e) {
        if (openISOOrders.isSelected()) {
          openUTFOrders.setEnabled(false);
          openUTFOrders.setSelected(false);
        } else {
          openUTFOrders.setEnabled(true);
        }
      }
    });

    openUTFOrders.addChangeListener(new ChangeListener() {
      public void stateChanged(ChangeEvent e) {
        if (openUTFOrders.isSelected()) {
          openISOOrders.setEnabled(false);
          openISOOrders.setSelected(false);
        } else {
          openISOOrders.setEnabled(true);
        }
      }
    });

    runISOEcheck.addChangeListener(new ChangeListener() {
      public void stateChanged(ChangeEvent e) {
        if (runISOEcheck.isSelected()) {
          runUTFEcheck.setEnabled(false);
          runUTFEcheck.setSelected(false);
        } else {
          runUTFEcheck.setEnabled(true);
        }
      }
    });

    runUTFEcheck.addChangeListener(new ChangeListener() {
      public void stateChanged(ChangeEvent e) {
        if (runUTFEcheck.isSelected()) {
          runISOEcheck.setEnabled(false);
          runISOEcheck.setSelected(false);
        } else {
          runISOEcheck.setEnabled(true);
        }
      }
    });

    return textEncodingPrefrencesPanel;
  }

  /**
   * save settings
   */
  public void applyPreferences() {
    ClientTextEncodingPreferences.settings.setProperty("TextEncoding.ISOsaveOrders", (saveISOOrders
        .isSelected() ? "true" : "false"));
    ClientTextEncodingPreferences.settings.setProperty("TextEncoding.ISOopenOrders", (openISOOrders
        .isSelected() ? "true" : "false"));
    ClientTextEncodingPreferences.settings.setProperty("TextEncoding.ISOrunEcheck", (runISOEcheck
        .isSelected() ? "true" : "false"));
    ClientTextEncodingPreferences.settings.setProperty("TextEncoding.ISOrunJVorlage", (runJVorlage
        .isSelected() ? "true" : "false"));
    ClientTextEncodingPreferences.settings.setProperty("TextEncoding.UTF8saveOrders",
        (saveUTFOrders.isSelected() ? "true" : "false"));
    ClientTextEncodingPreferences.settings.setProperty("TextEncoding.UTFopenOrders", (openUTFOrders
        .isSelected() ? "true" : "false"));
    ClientTextEncodingPreferences.settings.setProperty("TextEncoding.UTF8runEcheck", (runUTFEcheck
        .isSelected() ? "true" : "false"));
  }

  /**
   * Returns the component for showing in preferences dialog
   * 
   * @return The Component
   */
  public Component getComponent() {
    return this;
  }

  /**
   * @see magellan.client.swing.preferences.PreferencesAdapter#getTitle()
   */
  public String getTitle() {
    return Resources.get("util.textencodingpreferences.prefs.title");
  }

}
