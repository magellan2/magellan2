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

import javax.swing.BorderFactory;
import javax.swing.JCheckBox;
import javax.swing.JPanel;

import magellan.client.swing.InternationalizedPanel;
import magellan.client.swing.preferences.PreferencesAdapter;
import magellan.library.utils.Resources;


/**
 * DOCUMENT-ME
 *
 * @author $Author: $
 * @version $Revision: 269 $
 */
public class ResourcePlugInPreferences extends InternationalizedPanel implements PreferencesAdapter {
	protected Properties settings;
	protected JCheckBox chkSearchResources;
	protected JCheckBox chkSearchClassPath;

	/**
	 * Creates a new ExternalModuleSettings object.
	 *
	 * 
	 */
	public ResourcePlugInPreferences(Properties settings) {
		this.settings = settings;
		initComponents();
	}

	private void initComponents() {
		setLayout(new java.awt.GridBagLayout());

		JPanel pnl = new JPanel(new GridBagLayout());
		pnl.setBorder(new javax.swing.border.TitledBorder(BorderFactory.createEtchedBorder(),
														  Resources.get("extern.externalmodulesettings.border.externalmodules")));

		GridBagConstraints c = new GridBagConstraints();

		chkSearchResources = new JCheckBox(Resources.get("extern.externalmodulesettings.chk.searchResources"),
										   Boolean.valueOf(settings.getProperty("ExternalModuleLoader.searchResourcePathClassLoader",
																			"true")).booleanValue());

		c.gridx = 0;
		c.gridy = 0;
		c.anchor = GridBagConstraints.WEST;
		c.fill = GridBagConstraints.NONE;
		c.weightx = 1.0;
		c.weighty = 1.0;
		pnl.add(chkSearchResources, c);

		chkSearchClassPath = new JCheckBox(Resources.get("extern.externalmodulesettings.chk.searchClassPath"),
										   Boolean.valueOf(settings.getProperty("ExternalModuleLoader.searchClassPath",
																			"true")).booleanValue());

		c.gridx = 0;
		c.gridy = 1;
		pnl.add(chkSearchClassPath, c);

		c = new GridBagConstraints(0, 0, 1, 1, 1, 1, GridBagConstraints.NORTH,
								   GridBagConstraints.HORIZONTAL, new Insets(0, 0, 2, 0), 0, 0);
		this.add(pnl, c);
	}

    public void initPreferences() {
        // TODO: implement it
    }

	/**
	 * DOCUMENT-ME
	 */
	public void applyPreferences() {
		settings.setProperty("ExternalModuleLoader.searchResourcePathClassLoader",
							 String.valueOf(chkSearchResources.isSelected()));
		settings.setProperty("ExternalModuleLoader.searchClassPath",
							 String.valueOf(chkSearchClassPath.isSelected()));
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
		return Resources.get("extern.externalmodulesettings.title");
	}
}
