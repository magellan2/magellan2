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
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Properties;

import javax.swing.ButtonGroup;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.border.TitledBorder;

import magellan.client.Client;
import magellan.client.swing.preferences.ExtendedPreferencesAdapter;
import magellan.client.swing.preferences.PreferencesAdapter;
import magellan.client.utils.FileNameGenerator;
import magellan.library.utils.IDBaseConverter;
import magellan.library.utils.Locales;
import magellan.library.utils.PropertiesHelper;
import magellan.library.utils.Resources;
import magellan.library.utils.VersionInfo;
import magellan.library.utils.logging.Logger;



/**
 * This is the panel for the client settings.
 *
 * @author $Author: $
 * @version $Revision: 350 $
 */
public class ClientPreferences extends AbstractPreferencesAdapter implements ExtendedPreferencesAdapter, ActionListener {
	private static final Logger log = Logger.getInstance(ClientPreferences.class);
	Properties settings = null;
	Client source = null;
	private javax.swing.JComboBox cmbGUILocale = null;
	private javax.swing.JComboBox cmbOrderLocale = null;

	// The initial value for GameData.curTempID

	/**
	 * @see magellan.library.GameData#curTempID
	 */
	private JTextField tempIDsInitialValue;

	// number will allways be increased by one after a TempUnit has been created
	private JRadioButton countBase36;

	// tries to count decimal:
	// for example: A005 -> A006 -> A007 -> A008 -> A009 -> A010 (and not A00A)
	private JRadioButton countDecimal;
	private JRadioButton ascendingOrder;
	private JRadioButton descendingOrder;
	private JCheckBox showTempUnitDialog;
	private JCheckBox showProgress;
	private JCheckBox createVoidRegions;
  private JCheckBox checkForUpdates;
  private JCheckBox checkForNightlyUpdates;
  private JCheckBox loadlastreport;
	protected List<PreferencesAdapter> subAdapters;

	/**
	 * Creates a new ClientPreferences object.
	 *
	 * 
	 * 
	 */
	public ClientPreferences(Properties p, Client source) {
		settings = p;
		this.source = source;

		initGUI();
	}

  /**
   * @see magellan.client.swing.preferences.ExtendedPreferencesAdapter#getChildren()
   */
	public List<PreferencesAdapter> getChildren() {
		return subAdapters;
	}

	private void initGUI() {
		subAdapters = new ArrayList<PreferencesAdapter>(3);

		// add font and l&f only as sub adapter
		subAdapters.add(new ClientLookAndFeelPreferences(source,settings));

		// add the history only as sub adapter
		subAdapters.add(new ClientFileHistoryPreferences(source,settings));

		// add the file name generator only as sub adapter
		FileNameGenerator.init(settings);
		subAdapters.add(new ClientFileNameGeneratorPreferences(settings));
		
		// add the name generator only as sub adapter
		subAdapters.add(new ClientNameGeneratorPreferences(source,settings));
		
		// add the TextEncodingPreferences only as sub adapter
		subAdapters.add(new ClientTextEncodingPreferences(settings));
    
    subAdapters.add(new ClientMessagePreferences(source.getMessagePanel()));

		// locales
		getLocalesPanel();
		getTempUnitPanel();
		getMiscPanel();
		
	}
	

  private Component getMiscPanel() {
    JPanel panel = addPanel(Resources.get("clientpreferences.misc.border"), new GridBagLayout());

    int line = 0;
    GridBagConstraints c = new GridBagConstraints();
    c.insets = new Insets(2, 2, 2, 2);
    
    // check for updates
    c.gridx = 0;
    c.gridy = line;
    c.fill = GridBagConstraints.HORIZONTAL;
    c.anchor = GridBagConstraints.WEST;
    c.weightx = 0.0;
    checkForUpdates = new JCheckBox( Resources.get("clientpreferences.misc.checkforupdates.caption"), PropertiesHelper.getBoolean(settings, VersionInfo.PROPERTY_KEY_UPDATECHECK_CHECK, true));
    checkForUpdates.setHorizontalAlignment(SwingConstants.LEFT);
    panel.add(checkForUpdates,c);
    c.gridx = 1;
    c.gridy = line;
    c.fill = GridBagConstraints.NONE;
    c.anchor = GridBagConstraints.EAST;
    c.weightx = 0.1;
    panel.add(new JLabel(""), c);
    line++;

    // check for nightly updates
    c.gridx = 0;
    c.gridy = line;
    c.fill = GridBagConstraints.HORIZONTAL;
    c.anchor = GridBagConstraints.WEST;
    c.weightx = 0.0;
    checkForNightlyUpdates = new JCheckBox( Resources.get("clientpreferences.misc.checkfornightlyupdates.caption"), PropertiesHelper.getBoolean(settings, VersionInfo.PROPERTY_KEY_UPDATECHECK_NIGHTLY_CHECK, false));
    checkForNightlyUpdates.setHorizontalAlignment(SwingConstants.LEFT);
    checkForNightlyUpdates.setEnabled(checkForUpdates.isSelected());
    panel.add(checkForNightlyUpdates,c);
    c.gridx = 1;
    c.gridy = line;
    c.fill = GridBagConstraints.NONE;
    c.anchor = GridBagConstraints.EAST;
    c.weightx = 0.1;
    panel.add(new JLabel(""), c);
    line++;
    
    checkForUpdates.setActionCommand(VersionInfo.PROPERTY_KEY_UPDATECHECK_CHECK);
    checkForUpdates.addActionListener(this);

    // load last report on startup
    c.gridx = 0;
    c.gridy = line;
    c.fill = GridBagConstraints.HORIZONTAL;
    c.anchor = GridBagConstraints.WEST;
    c.weightx = 0.0;
    loadlastreport = new JCheckBox( Resources.get("clientpreferences.misc.loadlastreport.caption"), PropertiesHelper.getBoolean(settings, PropertiesHelper.CLIENTPREFERENCES_LOAD_LAST_REPORT, true));
    loadlastreport.setHorizontalAlignment(SwingConstants.LEFT);
    panel.add(loadlastreport,c);
    c.gridx = 1;
    c.gridy = line;
    c.fill = GridBagConstraints.NONE;
    c.anchor = GridBagConstraints.EAST;
    c.weightx = 0.1;
    panel.add(new JLabel(""), c);
    line++;
    
    // create void regions
    c.gridx = 0;
    c.gridy = line;
    c.fill = GridBagConstraints.HORIZONTAL;
    c.anchor = GridBagConstraints.WEST;
    c.weightx = 0.0;
    createVoidRegions = new JCheckBox( Resources.get("clientpreferences.create.void.regions.caption"), PropertiesHelper.getBoolean(settings, "map.creating.void", false));
    createVoidRegions.setHorizontalAlignment(SwingConstants.LEFT);
    panel.add(createVoidRegions,c);
    c.gridx = 1;
    c.gridy = line;
    c.fill = GridBagConstraints.NONE;
    c.anchor = GridBagConstraints.EAST;
    c.weightx = 0.1;
    panel.add(new JLabel(""), c);
    line++;

    // progress in titlebar
    c.gridx = 0;
    c.gridy = line;
    c.fill = GridBagConstraints.HORIZONTAL;
    c.anchor = GridBagConstraints.WEST;
    c.weightx = 0.0;
    showProgress = new JCheckBox( Resources.get("clientpreferences.progress.caption"), source.isShowingStatus());
    showProgress.setHorizontalAlignment(SwingConstants.LEFT);
    panel.add(showProgress,c);
    c.gridx = 1;
    c.gridy = line;
    c.fill = GridBagConstraints.NONE;
    c.anchor = GridBagConstraints.EAST;
    c.weightx = 0.1;
    panel.add(new JLabel(""), c);
    line++;

    return panel;
  }

	private Component getTempUnitPanel() {
		// tempUnitIDs
		JPanel tempIDs = addPanel(Resources.get("clientpreferences.border.temps"), new GridBagLayout());
		GridBagConstraints c2 = new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0,
													   GridBagConstraints.WEST,
													   GridBagConstraints.VERTICAL,
													   new Insets(4, 4, 4, 4), 0, 0);

		JLabel label = new JLabel( Resources.get("clientpreferences.tempids"));
		tempIDs.add(label, c2);
		c2.gridy = 1;

		String s = settings.getProperty("ClientPreferences.TempIDsInitialValue", "");
		tempIDsInitialValue = new JTextField(s);
		tempIDsInitialValue.setPreferredSize(new Dimension(100, 40));
		tempIDsInitialValue.setBorder(new TitledBorder( Resources.get("clientpreferences.tempidsinitialvalue.caption")));
		tempIDsInitialValue.setMargin(new Insets(2, 2, 2, 2));
		tempIDsInitialValue.setHorizontalAlignment(SwingConstants.CENTER);
		tempIDs.add(tempIDsInitialValue, c2);

		Boolean b = Boolean.valueOf(settings.getProperty("ClientPreferences.countDecimal", "true"));
		countDecimal = new JRadioButton( Resources.get("clientpreferences.tempids.countdecimal.caption"), b.booleanValue());
		countBase36 = new JRadioButton( Resources.get("clientpreferences.tempids.countbase36.caption"), !b.booleanValue());

		ButtonGroup buttonGroup = new ButtonGroup();
		buttonGroup.add(countDecimal);
		buttonGroup.add(countBase36);
		c2.gridx = 1;
		tempIDs.add(countDecimal, c2);
		c2.gridx = 2;
		tempIDs.add(countBase36, c2);
		c2.weightx = 1.0;
		c2.gridx = 3;
		tempIDs.add(new JPanel(), c2);

		//pavkovic
		Boolean ascending = Boolean.valueOf(settings.getProperty("ClientPreferences.ascendingOrder",
																 "true"));
		ascendingOrder = new JRadioButton( Resources.get("clientpreferences.tempids.ascendingorder.caption"),
										  ascending.booleanValue());
		descendingOrder = new JRadioButton( Resources.get("clientpreferences.tempids.descendingorder.caption"),
										   !ascending.booleanValue());

		ButtonGroup buttonGroup2 = new ButtonGroup();
		buttonGroup2.add(ascendingOrder);
		buttonGroup2.add(descendingOrder);
		c2.gridy = 2;
		c2.gridx = 1;
		tempIDs.add(ascendingOrder, c2);
		c2.gridx = 2;
		tempIDs.add(descendingOrder, c2);
		c2.weightx = 1.0;

		c2.gridy = 3;
		c2.gridx = 0;
		c2.gridwidth = 3;
		showTempUnitDialog = new JCheckBox( Resources.get("clientpreferences.showtempunitdialog"),
										   settings.getProperty("MultiEditorOrderEditorList.ButtonPanel.ShowTempUnitDialog",
																"true").equalsIgnoreCase("true"));
		tempIDs.add(showTempUnitDialog, c2);

		// tooltips
		tempIDsInitialValue.setToolTipText( Resources.get("clientpreferences.tempidsinitialvalue.tooltip"));
		countDecimal.setToolTipText( Resources.get("clientpreferences.tempids.countdecimal.tooltip"));
		countBase36.setToolTipText( Resources.get("clientpreferences.tempids.countbase36.tooltip"));

		return tempIDs;
	}

	/**
	 * Creates a panel containing the GUI elements for setting the locales.
	 *
	 * 
	 */
	private Component getLocalesPanel() {
		Object availLocales[] = { new LocaleWrapper(Locale.GERMAN), new LocaleWrapper(Locale.ENGLISH) };

		JPanel pnlLocales = addPanel(Resources.get("clientpreferences.border.locales"), new GridBagLayout());

		cmbGUILocale = new JComboBox(availLocales);
		cmbGUILocale.setSelectedItem(new LocaleWrapper(Locales.getGUILocale()));

		JLabel lblGUILocale = new JLabel( Resources.get("clientpreferences.lbl.guilocale.caption"));
		lblGUILocale.setDisplayedMnemonic( Resources.get("clientpreferences.lbl.guilocale.mnemonic").charAt(0));
		lblGUILocale.setLabelFor(cmbGUILocale);

		cmbOrderLocale = new JComboBox(availLocales);
		cmbOrderLocale.setSelectedItem(new LocaleWrapper(Locales.getOrderLocale()));

		JLabel lblOrderLocale = new JLabel( Resources.get("clientpreferences.lbl.orderlocale.caption"));
		lblOrderLocale.setDisplayedMnemonic( Resources.get("clientpreferences.lbl.orderlocale.mnemonic").charAt(0));
		lblOrderLocale.setLabelFor(cmbOrderLocale);

		GridBagConstraints c = new GridBagConstraints();
		c.insets = new Insets(2, 2, 2, 2);
		c.gridx = 0;
		c.gridy = 0;
		c.fill = GridBagConstraints.HORIZONTAL;
		c.anchor = GridBagConstraints.CENTER;
		c.weightx = 0.0;
		pnlLocales.add(lblGUILocale, c);
		c.gridx = 1;
		c.gridy = 0;
		c.fill = GridBagConstraints.NONE;
		c.anchor = GridBagConstraints.EAST;
		c.weightx = 0.1;
		pnlLocales.add(cmbGUILocale, c);

		c.gridx = 0;
		c.gridy = 1;
		c.fill = GridBagConstraints.HORIZONTAL;
		c.anchor = GridBagConstraints.CENTER;
		c.weightx = 0.0;
		pnlLocales.add(lblOrderLocale, c);
		c.gridx = 1;
		c.gridy = 1;
		c.fill = GridBagConstraints.NONE;
		c.anchor = GridBagConstraints.EAST;
		c.weightx = 0.1;
		pnlLocales.add(cmbOrderLocale, c);

		return pnlLocales;
	}

    /**
     * TODO: implement it
     * @deprecated not implemented!
     * 
     * @see magellan.client.swing.preferences.PreferencesAdapter#initPreferences()
     */
    @Deprecated
    public void initPreferences() {
        // TODO: implement it
    }

	/**
	 * DOCUMENT-ME
	 */
	public void applyPreferences() {
		Locale guiLocale = ((LocaleWrapper) cmbGUILocale.getSelectedItem()).getLocale();
		Locale orderLocale = ((LocaleWrapper) cmbOrderLocale.getSelectedItem()).getLocale();
		settings.setProperty("locales.gui", guiLocale.getLanguage());
		settings.setProperty("locales.orders", orderLocale.getLanguage());
		Locales.setGUILocale(guiLocale);
		Locales.setOrderLocale(orderLocale);

		if(source.getData() != null) {
			source.getData().setCurTempID(-1);
		}

		String s = tempIDsInitialValue.getText();

		if((s == null) || s.equalsIgnoreCase("")) {
			settings.setProperty("ClientPreferences.TempIDsInitialValue", "");
		} else {
			int tempID = 0;

            int base = 10;
            if(source.getData() != null) {
                base = source.getData().base;
            }
            
			try {
				tempID = IDBaseConverter.parse(tempIDsInitialValue.getText(),base);
			} catch(java.lang.NumberFormatException nfe) {
				ClientPreferences.log.warn("ClientPreferences.applyPreferences: Error when parsing the initial value of the temp ids: " +
						 tempIDsInitialValue.getText());
			}

			if(tempID > IDBaseConverter.getMaxId(base)) {
				ClientPreferences.log.warn("ClientPreferences.applyPreferences: Found tempID out of valid values: " +
						 tempID);
				tempIDsInitialValue.setText("");
				settings.setProperty("ClientPreferences.TempIDsInitialValue", "");
				tempID = 0;
			}

			if(tempID != 0) {
				settings.setProperty("ClientPreferences.TempIDsInitialValue",
									 IDBaseConverter.toString(tempID,base));
			}
		}

		settings.setProperty("ClientPreferences.countDecimal", String.valueOf(countDecimal.isSelected()));
		settings.setProperty("ClientPreferences.ascendingOrder", String.valueOf(ascendingOrder.isSelected()));
		settings.setProperty("MultiEditorOrderEditorList.ButtonPanel.ShowTempUnitDialog", String.valueOf(showTempUnitDialog.isSelected()));

		settings.setProperty("map.creating.void",String.valueOf(createVoidRegions.isSelected()));
    
    settings.setProperty(VersionInfo.PROPERTY_KEY_UPDATECHECK_CHECK,String.valueOf(checkForUpdates.isSelected()));
    settings.setProperty(VersionInfo.PROPERTY_KEY_UPDATECHECK_NIGHTLY_CHECK,String.valueOf(checkForNightlyUpdates.isSelected()));
    settings.setProperty(PropertiesHelper.CLIENTPREFERENCES_LOAD_LAST_REPORT,String.valueOf(loadlastreport.isSelected()));
		
		source.setShowStatus(showProgress.isSelected());
	}

	/**
	 * DOCUMENT-ME
	 *
	 * 
	 */
	public Component getComponent() {
		return this;
	}

	/**
	 * DOCUMENT-ME
	 *
	 * 
	 */
	public String getTitle() {
		return  Resources.get("clientpreferences.title");
	}

	/**
	 * A wrapper for storing Locale objects in a combo box while displaying a full name instead of
	 * only a language code.
	 */
	private static class LocaleWrapper {
		private Locale locale = null;

		/**
		 * Creates a new LocaleWrapper object.
		 *
		 * 
		 */
		public LocaleWrapper(Locale l) {
			this.locale = l;
		}

		/**
		 * DOCUMENT-ME
		 *
		 * 
		 */
		public Locale getLocale() {
			return this.locale;
		}

		/**
		 * DOCUMENT-ME
		 *
		 * 
		 */
		@Override
    public String toString() {
			return this.locale.getDisplayName();
		}

		/**
		 * DOCUMENT-ME
		 *
		 * 
		 *
		 * 
		 */
		@Override
    public boolean equals(Object o) {
			try {
				return this.locale.equals(((LocaleWrapper) o).getLocale());
			} catch(ClassCastException e) {
				return false;
			}
		}
	}

  public void actionPerformed(ActionEvent e) {
    if (e.getActionCommand().equals(VersionInfo.PROPERTY_KEY_UPDATECHECK_CHECK)) {
      checkForNightlyUpdates.setEnabled(checkForUpdates.isSelected());
      if (!checkForUpdates.isSelected()) checkForNightlyUpdates.setSelected(false);
    }
    
  }

}
