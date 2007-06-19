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

package magellan.client.utils;

import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.Properties;

import javax.swing.BorderFactory;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;

import magellan.client.swing.layout.GridBagHelper;
import magellan.client.swing.preferences.PreferencesAdapter;
import magellan.library.utils.PropertiesHelper;
import magellan.library.utils.Resources;


/**
 * DOCUMENT ME!
 *
 * @author Fiete
 * @version
 */
public class TextEncodingPreferences extends JPanel implements PreferencesAdapter {

	static Properties settings;
	protected JCheckBox saveOrders;
	protected JCheckBox openOrders;
	protected JCheckBox runEcheck;
	protected JCheckBox runJVorlage;

	public TextEncodingPreferences(Properties _settings){
		settings = _settings;
		this.initGUI();
	}
	
	
	/* (non-Javadoc)
	 * @see com.eressea.swing.preferences.PreferencesAdapter#initPreferences()
	 */
	public void initPreferences() {
		// nothing to do...
		
	}
	
	private void initGUI() {
		/*
		*/

		// layout this container
		setLayout(new GridBagLayout());

		GridBagConstraints c = new GridBagConstraints();

		c.insets.top = 10;
		c.insets.bottom = 10;
		GridBagHelper.setConstraints(c, 0, 0, GridBagConstraints.REMAINDER, 1, 1.0, 1.0,
									 GridBagConstraints.NORTHWEST,
									 GridBagConstraints.HORIZONTAL, c.insets, 0, 0);

		this.add(TextEncodingPrefrencesPanel(), c);

	}

	private Component TextEncodingPrefrencesPanel() {
		JPanel textEncodingPrefrencesPanel = new JPanel();
		textEncodingPrefrencesPanel.setLayout(new GridBagLayout());
		textEncodingPrefrencesPanel.setBorder(new TitledBorder(new CompoundBorder(BorderFactory.createEtchedBorder(),
														   new EmptyBorder(0, 3, 3, 3)),
										Resources.get("util.textencodingpreferences.prefs.title")));

		GridBagConstraints c = new GridBagConstraints(0, 0, 2, 1, 1, 0,
													  GridBagConstraints.WEST,
													  GridBagConstraints.HORIZONTAL,
													  new Insets(2, 10, 1, 10), 0, 0);

		JLabel textEncodingInfoLabel = new JLabel(Resources.get("util.textencodingpreferences.prefs.info1"));
		textEncodingPrefrencesPanel.add(textEncodingInfoLabel,c);
		
		c.gridy++;
		textEncodingInfoLabel = new JLabel(Resources.get("util.textencodingpreferences.prefs.info2"));
		textEncodingPrefrencesPanel.add(textEncodingInfoLabel,c);
		
		c.gridy++;
		saveOrders = new JCheckBox(Resources.get("util.textencodingpreferences.checkbox.textEncodingISOsaveOrders.label"),PropertiesHelper.getboolean(settings, "TextEncoding.ISOsaveOrders", false));
		textEncodingPrefrencesPanel.add(saveOrders, c);
		
		c.gridy++;
		openOrders = new JCheckBox(Resources.get("util.textencodingpreferences.checkbox.textEncodingISOopenOrders.label"),PropertiesHelper.getboolean(settings, "TextEncoding.ISOopenOrders", false));
		textEncodingPrefrencesPanel.add(openOrders, c);
		
		c.gridy++;
		runEcheck = new JCheckBox(Resources.get("util.textencodingpreferences.checkbox.textEncodingISOECheck.label"),PropertiesHelper.getboolean(settings, "TextEncoding.ISOrunEcheck", false));
		textEncodingPrefrencesPanel.add(runEcheck, c);
		
		c.gridy++;
		runJVorlage = new JCheckBox(Resources.get("util.textencodingpreferences.checkbox.textEncodingISOJVorlage.label"),PropertiesHelper.getboolean(settings, "TextEncoding.ISOrunJVorlage", false));
		runJVorlage.setEnabled(false);
		textEncodingPrefrencesPanel.add(runJVorlage, c);
		
		return textEncodingPrefrencesPanel;
	}

   	/**
	 * save settings 
	 * 
	 */
	public void applyPreferences() {
		settings.setProperty("TextEncoding.ISOsaveOrders", (saveOrders.isSelected() ? "true" : "false"));
		settings.setProperty("TextEncoding.ISOopenOrders", (openOrders.isSelected() ? "true" : "false"));
		settings.setProperty("TextEncoding.ISOrunEcheck", (runEcheck.isSelected() ? "true" : "false"));
		settings.setProperty("TextEncoding.ISOrunJVorlage", (runJVorlage.isSelected() ? "true" : "false"));
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
	 * DOCUMENT-ME
	 *
	 * 
	 */
	public String getTitle() {
		return Resources.get("util.textencodingpreferences.prefs.title");
	}

}
