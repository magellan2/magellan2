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
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;

import magellan.client.swing.layout.GridBagHelper;
import magellan.client.swing.preferences.PreferencesAdapter;
import magellan.client.swing.preferences.PreferencesFactory;
import magellan.library.utils.Resources;


/**
 * DOCUMENT ME!
 *
 * @author Andreas
 * @version
 */
public class NameGenerator implements PreferencesFactory {
	boolean available = false;
	List<String> names;
	static Properties settings;
	static NameGenerator gen;

	/**
	 * DOCUMENT-ME
	 *
	 * 
	 */
	public static void init(Properties set) {
		settings = set;
		new NameGenerator(settings);
	}

	/**
	 * DOCUMENT-ME
	 */
	public static void quit() {
		if(gen != null) {
			gen.close();
		}
	}

	/**
	 * DOCUMENT-ME
	 *
	 * 
	 */
	public static NameGenerator getInstance() {
		if(gen == null) {
			new NameGenerator(settings);
		}

		return gen;
	}

	private NameGenerator(Properties settings) {
		load(settings.getProperty("NameGenerator.Source"));
		available = settings.getProperty("NameGenerator.active", "false").equals("true");

		gen = this;
	}

	protected void load(String file) {
		if(names == null) {
			names = new LinkedList<String>();
		} else {
			names.clear();
		}

		if((file != null) && !file.trim().equals("")) {
			try {
				BufferedReader in = new BufferedReader(new FileReader(file));
				String s = null;

				while((s = in.readLine()) != null) {
					names.add(s.trim());
				}

				in.close();
			} catch(IOException ioe) {
				System.out.println(ioe);
			}
		}

		if(names.size() == 0) {
			names = null;
		}
	}

	protected void close() {
		String file = settings.getProperty("NameGenerator.Source");

		if(file != null) {
			try {
				File f = new File(file);

				if(f.exists()) {
					f.delete();
				}

				if(names != null) {
					PrintWriter out = new PrintWriter(new FileWriter(file));
					Iterator it = names.iterator();

					while(it.hasNext()) {
						out.println(it.next());
					}

					out.close();
				}
			} catch(IOException exc) {
			}
		}
	}

	/**
	 * DOCUMENT-ME
	 *
	 * 
	 */
	public boolean isActive() {
		return available;
	}

	/**
	 * DOCUMENT-ME
	 *
	 * 
	 */
	public boolean isAvailable() {
		return (available && (names != null));
	}

	/**
	 * DOCUMENT-ME
	 *
	 * 
	 */
	public String getName() {
		if(names != null) {
			String name = (String) names.remove(0);

			if(names.size() == 0) {
				names = null;
				showMessage();
			}

			return name;
		}

		return null;
	}

	protected void showMessage() {
		JOptionPane.showMessageDialog(new JFrame(), Resources.get("magellan.util.namegenerator.nomorenames"));
	}

	/**
	 * DOCUMENT-ME
	 *
	 * 
	 */
	public PreferencesAdapter createPreferencesAdapter() {
		return new NameGenPrefAdapter();
	}

	protected class NameGenPrefAdapter extends JPanel implements PreferencesAdapter, ActionListener {
		protected JCheckBox active;
		protected JTextField fileField;

		/**
		 * Creates a new NameGenPrefAdapter object.
		 */
		public NameGenPrefAdapter() {
			initGUI();
		}

		private void initGUI() {
			/*
			*/

			// set up the panel for the maximum file history size
			// layout this container
			setLayout(new GridBagLayout());

			GridBagConstraints c = new GridBagConstraints();

			c.insets.top = 10;
			c.insets.bottom = 10;
			GridBagHelper.setConstraints(c, 0, 0, GridBagConstraints.REMAINDER, 1, 1.0, 1.0,
										 GridBagConstraints.NORTHWEST,
										 GridBagConstraints.HORIZONTAL, c.insets, 0, 0);

			// help panel
			this.add(getNameGeneratorPanel(), c);
			c.insets.top = 0;
		}

		private Component getNameGeneratorPanel() {
			JPanel help = new JPanel();
			help.setLayout(new GridBagLayout());
			help.setBorder(new TitledBorder(new CompoundBorder(BorderFactory.createEtchedBorder(),
															   new EmptyBorder(0, 3, 3, 3)),
											Resources.get("magellan.util.namegenerator.prefs.title")));

			GridBagConstraints c = new GridBagConstraints(0, 0, 2, 1, 1, 0,
														  GridBagConstraints.WEST,
														  GridBagConstraints.HORIZONTAL,
														  new Insets(2, 10, 1, 10), 0, 0);

			active = new JCheckBox(Resources.get("magellan.util.namegenerator.prefs.active"), isActive());
			help.add(active, c);

			c.gridy++;
			c.gridwidth = 1;
			c.insets.right = 1;
			fileField = new JTextField(settings.getProperty("NameGenerator.Source"), 20);
			help.add(fileField, c);
			c.gridx++;
			c.insets.right = 10;
			c.fill = GridBagConstraints.NONE;

			JButton b = new JButton("...");
			b.addActionListener(this);
			help.add(b, c);

			return help;
		}

        public void initPreferences() {
            // TODO: implement it
        }

		/**
		 * DOCUMENT-ME
		 */
		public void applyPreferences() {
			available = active.isSelected();
			settings.setProperty("NameGenerator.Source", fileField.getText());

			if(available) {
				settings.setProperty("NameGenerator.active", "true");
			} else {
				settings.remove("NameGenerator.active");
			}

			load(fileField.getText());
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
			return Resources.get("magellan.util.namegenerator.prefs.title");
		}

		/**
		 * DOCUMENT-ME
		 *
		 * 
		 */
		public void actionPerformed(java.awt.event.ActionEvent actionEvent) {
			String s = settings.getProperty("NameGenerator.Source");

			if(s == null) {
				s = ".";
			} else {
				int i = s.lastIndexOf(File.separatorChar);

				if(i > 0) {
					s = s.substring(0, i);
				} else {
					s = ".";
				}
			}

			JFileChooser f = new JFileChooser(s);
			int ret = f.showOpenDialog(this);

			if(ret == JFileChooser.APPROVE_OPTION) {
				fileField.setText(f.getSelectedFile().toString());
			}
		}
	}
}
