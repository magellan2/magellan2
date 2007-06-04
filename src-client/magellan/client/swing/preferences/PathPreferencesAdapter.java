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

package magellan.client.swing.preferences;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import magellan.library.utils.Resources;


/**
 * DOCUMENT ME!
 *
 * @author Andreas
 * @version
 */
public class PathPreferencesAdapter extends JPanel implements PreferencesAdapter {
	protected List<JTextField> textFields;
	protected List<String> keys;
	protected Properties settings;
	protected GridBagConstraints con;
	protected JFileChooser fchooser;
	protected File file;

	/**
	 * Creates new PathPreferencesAdapter
	 *
	 * 
	 */
	public PathPreferencesAdapter(Properties set) {
		settings = set;

		// UI
		setLayout(new GridBagLayout());
		con = new GridBagConstraints();
		con.anchor = GridBagConstraints.CENTER;
		con.fill = GridBagConstraints.HORIZONTAL;
		con.weighty = 0;
		con.gridy = 0;

		// list
		textFields = new LinkedList<JTextField>();
		keys = new LinkedList<String>();

		// files
		fchooser = new JFileChooser();

		try {
			file = new File(".");

			if(!file.isDirectory()) {
				file = file.getParentFile();
			}

			if(file == null) {
				file = new File(".");
			}
		} catch(Exception exc) {
		}
	}

	/**
	 * DOCUMENT-ME
	 *
	 * 
	 * 
	 */
	public void addPath(String label, String key) {
		con.gridx = 0;
		con.weightx = 0.25;
		add(new JLabel(label), con);

		JTextField tf = new JTextField(settings.getProperty(key));
		con.gridx = 1;
		con.weightx = 0.5;
		add(tf, con);
		textFields.add(tf);
		keys.add(key);

		con.gridx = 2;
		con.weightx = 0.25;
		add(new DirButton(tf), con);

		con.gridy++;

		doLayout();
	}

	/**
	 * DOCUMENT-ME
	 *
	 * 
	 */
	public void setVisible(boolean b) {
		if(b && (textFields.size() > 0)) {
			for(int i = 0, max = textFields.size(); i < max; i++) {
				((JTextField) textFields.get(i)).setText(settings.getProperty((String) keys.get(i)));
			}
		}

		super.setVisible(b);
	}

    public void initPreferences() {
        // TODO: implement it
    }

	/**
	 * DOCUMENT-ME
	 */
	public void applyPreferences() {
		if(textFields.size() > 0) {
			for(int i = 0; i < textFields.size(); i++) {
				settings.setProperty((String) keys.get(i),
									 ((JTextField) textFields.get(i)).getText());
			}
		}
	}

	/**
	 * DOCUMENT-ME
	 *
	 * 
	 */
	public java.awt.Component getComponent() {
		return this;
	}

	/**
	 * DOCUMENT-ME
	 *
	 * 
	 */
	public String getTitle() {
		return Resources.get("magellan.preferences.pathpreferencesadapter.prefs.title");
	}

	protected class DirButton extends JButton implements ActionListener {
		protected JTextField text;

		/**
		 * Creates a new DirButton object.
		 *
		 * 
		 */
		public DirButton(JTextField jtf) {
			super("...");
			text = jtf;
			addActionListener(this);
		}

		/**
		 * DOCUMENT-ME
		 *
		 * 
		 */
		public void actionPerformed(ActionEvent e) {
			try {
				fchooser.setSelectedFile(new File(text.getText()));
			} catch(Exception exc) {
				fchooser.setCurrentDirectory(file);
			}

			if(fchooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
				text.setText(fchooser.getSelectedFile().toString());
			}
		}
	}

	// pavkovic 2003.01.28: this is a Map of the default Translations mapped to this class
	// it is called by reflection (we could force the implementation of an interface,
	// this way it is more flexible.)
	// Pls use this mechanism, so the translation files can be created automagically
	// by inspecting all classes.
	private static Map<String,String> defaultTranslations;

	/**
	 * DOCUMENT-ME
	 *
	 * 
	 */
	public static synchronized Map<String,String> getDefaultTranslations() {
		if(defaultTranslations == null) {
			defaultTranslations = new Hashtable<String, String>();
			defaultTranslations.put("prefs.title", "Paths");
		}

		return defaultTranslations;
	}
}
