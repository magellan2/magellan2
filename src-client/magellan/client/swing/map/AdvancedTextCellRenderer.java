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

/*
 * AdvancedTextCellRenderer.java
 *
 * Created on 12. Juli 2001, 19:19
 */
package magellan.client.swing.map;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.StringTokenizer;

import javax.swing.AbstractButton;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import magellan.client.MagellanContext;
import magellan.client.desktop.DesktopEnvironment;
import magellan.client.desktop.ExtendedShortcutListener;
import magellan.client.desktop.ShortcutListener;
import magellan.client.swing.context.ContextChangeable;
import magellan.client.swing.context.ContextObserver;
import magellan.client.swing.preferences.PreferencesAdapter;
import magellan.library.GameData;
import magellan.library.Region;
import magellan.library.event.GameDataEvent;
import magellan.library.event.GameDataListener;
import magellan.library.utils.Resources;
import magellan.library.utils.replacers.ReplacerHelp;
import magellan.library.utils.replacers.ReplacerSystem;


/**
 * A slightly improved text cell renderer using the replacer engine of Magellan to create region
 * dependent output. <i>Caches a lot to improve performance, so let the interpreter allocate more
 * memory!</i>
 *
 * @author Andreas
 * @version 1.1
 */
public class AdvancedTextCellRenderer extends TextCellRenderer implements ExtendedShortcutListener,
																		  GameDataListener,
																		  ContextChangeable,
																		  ActionListener
{
	protected static final String BLANK = "";
	protected boolean breakLines; // Line break style
	protected static List<String> buffer; // breaking buffer		
	protected float lastScale = -1; // last scale factor, for broken lines cache
	protected ATRSet set;
	protected ATRPreferences adapter;
	protected ShortcutListener deflistener; // Shortcutlistener at depth 1 (after STRG-A)
	protected JMenu contextMenu;
	protected ContextObserver obs;

	/**
	 * Creates new AdvancedTextCellRenderer
	 *
	 * 
	 * 
	 */
	public AdvancedTextCellRenderer(CellGeometry geo, MagellanContext context) {
		super(geo, context);
		context.getEventDispatcher().addGameDataListener(this);

		if(buffer == null) {
			buffer = new LinkedList<String>();
		}

		loadSet(settings.getProperty("ATR.CurrentSet", "Standard"));

		// load style settings
		try {
			breakLines = settings.getProperty("ATR.breakLines", "false").equals("true");
		} catch(Exception exc2) {
			breakLines = false;
		}

		try {
			setHAlign(Integer.parseInt(settings.getProperty("ATR.horizontalAlign", "0")));
		} catch(Exception exc) {
			setHAlign(CENTER);
		}

		// create shortcut structure
		DesktopEnvironment.registerShortcutListener(KeyStroke.getKeyStroke(KeyEvent.VK_T,
																		   KeyEvent.CTRL_MASK |
																		   KeyEvent.ALT_MASK), this);
		deflistener = new DefListener();

		// create the context menu as needed
	}

	/**
	 * DOCUMENT-ME
	 *
	 * 
	 */
	public void loadSet(String name) {
		loadSet(name, settings);

		if(!exists(name)) {
			saveSet();
		}
	}

	/**
	 * DOCUMENT-ME
	 *
	 * 
	 * 
	 */
	public void loadSet(String name, Properties settings) {
		ATRSet s = new ATRSet(name);
		s.load(settings);
		this.set = s;
		settings.setProperty("ATR.CurrentSet", name);
	}

	/**
	 * DOCUMENT-ME
	 */
	public void saveSet() {
		saveSet(settings);
	}

	/**
	 * DOCUMENT-ME
	 *
	 * 
	 */
	public void saveSet(Properties settings) {
		set.save(settings);

		if(!exists(set.getName(), settings)) {
			settings.setProperty("ATR.Sets",
								 settings.getProperty("ATR.Sets", "") + ";" + set.getName());
		}
	}

	/**
	 * DOCUMENT-ME
	 *
	 * 
	 *
	 * 
	 */
	public boolean exists(String name) {
		return exists(name, settings);
	}

	/**
	 * DOCUMENT-ME
	 *
	 * 
	 * 
	 *
	 * 
	 */
	public boolean exists(String name, Properties settings) {
		String allSets = settings.getProperty("ATR.Sets", "");
		int sindex = 0;
		int i = -1;

		do {
			i = allSets.indexOf(name, sindex);

			boolean check = (i >= 0);

			try {
				char first = allSets.charAt(i - 1);

				if(first != ';') {
					check = false;
				}
			} catch(IndexOutOfBoundsException ie) {
			}

			try {
				char first = allSets.charAt(i + name.length());

				if(first != ';') {
					check = false;
				}
			} catch(IndexOutOfBoundsException ie2) {
			}

			if(check) {
				return true;
			}

			sindex++;
		} while(i >= 0);

		return false;
	}

	// a collection of set names
	public Collection<String> getAllSets() {
		Collection<String> c = new LinkedList<String>();
		StringTokenizer st = new StringTokenizer(settings.getProperty("ATR.Sets", "Standard"), ";");

		while(st.hasMoreTokens()) {
			c.add(st.nextToken());
		}

		return c;
	}

	/**
	 * DOCUMENT-ME
	 *
	 * 
	 */
	public void removeSet(String name) {
		String key = "ATR." + name + ".";
		settings.remove(key + "Def");
		settings.remove(key + "Unknown");

		StringTokenizer st = new StringTokenizer(settings.getProperty("ATR.Sets", "Standard"), ";");
		StringBuffer b = new StringBuffer();

		while(st.hasMoreTokens()) {
			String s = st.nextToken();

			if(!s.equals(name)) {
				if(b.length() > 0) {
					b.append(';');
				}

				b.append(s);
			}
		}

		settings.setProperty("ATR.Sets", b.toString());
	}

	/**
	 * DOCUMENT-ME
	 *
	 * 
	 */
	public void setHAlign(int h) {
		super.setHAlign(h);
		settings.setProperty("ATR.horizontalAlign", String.valueOf(h));
	}

	/**
	 * DOCUMENT-ME
	 *
	 * 
	 * 
	 * 
	 */
	public void init(GameData data, Graphics g, Point offset) {
		if(cellGeo.getScaleFactor() != lastScale) {
			set.clearCache();
			lastScale = cellGeo.getScaleFactor();
		}

		if(this.data != data) {
			set.clearCache();
			set.reprocessReplacer();
		}

		super.init(data, g, offset);
	}

	// never have single strings
	public String getSingleString(Region r, Rectangle rect) {
		return null;
	}

	/**
	 * DOCUMENT-ME
	 *
	 * 
	 * 
	 *
	 * 
	 */
	public String[] getText(Region r, Rectangle rect) {
		return set.getReplacement(r, getCellGeometry(), getFontMetrics());
	}

	/**
	 * DOCUMENT-ME
	 *
	 * 
	 */
	public String getName() {
		return "ATR";
	}

	/**
	 * DOCUMENT-ME
	 *
	 * 
	 */
	public void setBreakLines(boolean b) {
		if(breakLines != b) {
			set.clearCache();
			breakLines = b;
			settings.setProperty("ATR.breakLines", b ? "true" : "false");
			Mapper.setRenderContextChanged(true);
			DesktopEnvironment.repaintComponent("MAP");
		}
	}

	/**
	 * DOCUMENT-ME
	 *
	 * 
	 */
	public PreferencesAdapter getPreferencesAdapter() {
		return new ATRPreferences(this);
	}

	/**
	 * DOCUMENT-ME
	 *
	 * 
	 */
	public void gameDataChanged(GameDataEvent e) {
		set.clearCache();
		set.reprocessReplacer();
	}

	/**
	 * Should return all short cuts this class want to be informed. The elements should be of type
	 * javax.swing.KeyStroke
	 *
	 * 
	 */
	public Iterator<KeyStroke> getShortCuts() {
		return null;
	}

	/**
	 * This method is called when a shortcut from getShortCuts() is recognized.
	 *
	 * 
	 */
	public void shortCut(javax.swing.KeyStroke shortcut) {
	}

	/**
	 * DOCUMENT-ME
	 *
	 * 
	 *
	 * 
	 */
	public boolean isExtendedShortcut(KeyStroke stroke) {
		return true;
	}

	/**
	 * DOCUMENT-ME
	 *
	 * 
	 *
	 * 
	 */
	public ShortcutListener getExtendedShortcutListener(KeyStroke stroke) {
		return deflistener;
	}

	/**
	 * DOCUMENT-ME
	 *
	 * 
	 *
	 * 
	 */
	public String getShortcutDescription(Object stroke) {
		return Resources.get("magellan.map.advancedtextcellrenderer.shortcuts.description");
	}

	/**
	 * DOCUMENT-ME
	 *
	 * 
	 */
	public String getListenerDescription() {
		return Resources.get("magellan.map.advancedtextcellrenderer.shortcuts.title");
	}



	//////////////////
	// Context menu //
	//////////////////
	public JMenuItem getContextAdapter() {
		if(contextMenu == null) {
			contextMenu = new JMenu(Resources.get("magellan.map.advancedtextcellrenderer.context.title"));
			fillDefItems();
		}

		return contextMenu;
	}

	protected void fillDefItems() {
		contextMenu.removeAll();

		Collection c = getAllSets();
		Iterator it = c.iterator();

		while(it.hasNext()) {
			JMenuItem item = new JMenuItem((String) it.next());
			item.addActionListener(this);
			contextMenu.add(item);
		}
	}

	protected String getShortText(String text, int maxLength) {
		if(text == null) {
			return null;
		}

		if(text.length() <= maxLength) {
			return text;
		}

		return (text.substring(0, 7) + "...");
	}

	// don't need to call anyone, we repaint ourself
	public void setContextObserver(ContextObserver co) {
		obs = co;
	}

	/**
	 * DOCUMENT-ME
	 *
	 * 
	 */
	public void actionPerformed(java.awt.event.ActionEvent actionEvent) {
		loadSet(actionEvent.getActionCommand());

		if(obs != null) {
			obs.contextDataChanged();
		}
	}

	////////////////
	// SET & REPLACER
	///////////////
	protected String createString(ReplacerSystem rep, Region r) {
		if(rep != null) { // create replacement

			Object o = rep.getReplacement(r);

			if(o != null) {
				return o.toString();
			}
		}

		return BLANK;
	}

	protected String[] breakString(String s, CellGeometry geo, FontMetrics fm) {
		int maxWidth = (int) (geo.getCellSize().width * 0.9);
		buffer.clear();

		// create "defined" line breaks
		StringTokenizer st = new StringTokenizer(s, "\n");

		while(st.hasMoreTokens()) {
			buffer.add(st.nextToken());
		}

		boolean changed = false;

		do {
			changed = false;

			Iterator it = buffer.iterator();
			int index = 0;

			while(it.hasNext() && !changed) {
				String part = (String) it.next();

				if(fm.stringWidth(part) > maxWidth) {
					breakStringImpl(part, buffer, fm, maxWidth, index);
					changed = true;
				}

				index++;
			}
		} while(changed);

		String strbuf[] = new String[buffer.size()];

		for(int i = 0; i < strbuf.length; i++) {
			strbuf[i] = (String) buffer.get(i);
		}

		return strbuf;
	}

	private void breakStringImpl(String part, List<String> buffer, FontMetrics fm, int mw, int index) {
		char chr[] = part.toCharArray();
		int len = chr.length;

		while(fm.charsWidth(chr, 0, len) > mw) {
			len--;
		}

		if(breakLines) {
			String first = new String(chr, 0, len);
			String rest = new String(chr, len, chr.length - len);
			buffer.set(index, rest);
			buffer.add(index, first);
		} else {
			try {
				chr[len - 1] = '.';
				chr[len - 2] = '.';
				chr[len - 3] = '.';
			} catch(Exception exc) {
			}

			part = new String(chr, 0, len);
			buffer.set(index, part);
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
			defaultTranslations = new Hashtable<String,String>();
			defaultTranslations.put("context.title", "ATR");
			defaultTranslations.put("shortcuts.description", "Choose set(0-9)");
			defaultTranslations.put("shortcuts.title", "ATR");
			defaultTranslations.put("prefs.breakline", "Automatic line break");
			defaultTranslations.put("prefs.replace", "Unknown replace");
			defaultTranslations.put("prefs.align0", "Left");
			defaultTranslations.put("prefs.align1", "Centered");
			defaultTranslations.put("prefs.align2", "Right");
			defaultTranslations.put("prefs.aligntext", "Text alignment");
			defaultTranslations.put("boolReplace.false", "False");
			defaultTranslations.put("boolReplace.true", "True");
			defaultTranslations.put("prefs.export", "Export...");
			defaultTranslations.put("prefs.import", "Import...");
			defaultTranslations.put("prefs.remove", "Remove");
			defaultTranslations.put("prefs.nameexits2", "This name already exists: ");
			defaultTranslations.put("prefs.nameexists", "This name already exists.");
			defaultTranslations.put("prefs.rename.text", "Please enter a new name");
			defaultTranslations.put("prefs.add.text", "Please enter a name");
			defaultTranslations.put("prefs.def", "Def.");
			defaultTranslations.put("prefs.rename", "Rename...");
			defaultTranslations.put("prefs.add", "Add...");
			defaultTranslations.put("prefs.fonthelp",
									"Please change font properties at the text renderer");
		}

		return defaultTranslations;
	}

	/**
	 * Encapsulates one setting containing a name, a definition string and a cache object.
	 */
	protected class ATRSet {
		protected String name;
		protected String def;
		protected String unknown = "-?-";
		protected ReplacerSystem replacer = null;
		protected Map<Region,String[]> cache;

		/**
		 * Creates a new ATRSet object.
		 *
		 * 
		 */
		public ATRSet(String name) {
			this.name = name;
			cache = new HashMap<Region, String[]>();
		}

		/**
		 * DOCUMENT-ME
		 */
		public void clearCache() {
			cache.clear();
		}

		/**
		 * DOCUMENT-ME
		 */
		public void reprocessReplacer() {
			replacer = ReplacerHelp.createReplacer(def, unknown);
		}

		/**
		 * DOCUMENT-ME
		 *
		 * 
		 * 
		 * 
		 *
		 * 
		 */
		public String[] getReplacement(Region r, CellGeometry geo, FontMetrics fm) {
			if(cache.containsKey(r)) {
				return cache.get(r);
			}

			String buf[] = breakString(createString(replacer, r), geo, fm);
			cache.put(r, buf);

			return buf;
		}

		/**
		 * DOCUMENT-ME
		 *
		 * 
		 */
		public void setDef(String def) {
			clearCache();
			this.def = def;
			reprocessReplacer();
		}

		/**
		 * DOCUMENT-ME
		 *
		 * 
		 */
		public String getDef() {
			return def;
		}

		/**
		 * DOCUMENT-ME
		 *
		 * 
		 */
		public String getName() {
			return name;
		}

		/**
		 * DOCUMENT-ME
		 *
		 * 
		 */
		public void setName(String name) {
			this.name = name;
		}

		/**
		 * DOCUMENT-ME
		 *
		 * 
		 */
		public String getUnknown() {
			return unknown;
		}

		/**
		 * DOCUMENT-ME
		 *
		 * 
		 */
		public void setUnknown(String unknown) {
			clearCache();
			this.unknown = unknown;

			if(unknown == null) {
				this.unknown = "";
			}

			reprocessReplacer();
		}

		/**
		 * DOCUMENT-ME
		 *
		 * 
		 */
		public void load(Properties settings) {
			clearCache();

			String key = "ATR." + name + ".";
			def = settings.getProperty(key + "Def", "rname");
			setUnknown(settings.getProperty(key + "Unknown", "-?-"));
		}

		/**
		 * DOCUMENT-ME
		 *
		 * 
		 */
		public void save(Properties settings) {
			String key = "ATR." + name + ".";
			settings.setProperty(key + "Def", def);

			if(unknown.equals("-?-")) {
				settings.remove(key + "Unknown");
			} else {
				settings.setProperty(key + "Unknown", unknown);
			}
		}
	}

	class DefListener implements ShortcutListener {
		protected List<KeyStroke> keys;

		/**
		 * Creates a new DefListener object.
		 */
		public DefListener() {
			keys = new ArrayList<KeyStroke>(10);

			for(int i = 1; i < 10; i++) {
				keys.add(KeyStroke.getKeyStroke(Character.forDigit(i, 10)));
			}

			keys.add(KeyStroke.getKeyStroke('0'));
		}

		/**
		 * DOCUMENT-ME
		 *
		 * 
		 */
		public Iterator<KeyStroke> getShortCuts() {
			return keys.iterator();
		}

		/**
		 * DOCUMENT-ME
		 *
		 * 
		 */
		public void shortCut(javax.swing.KeyStroke shortcut) {
			int i = keys.indexOf(shortcut);

			if(i >= 0) {
				Collection c = getAllSets();

				if(c.size() > i) {
					Iterator it = c.iterator();
					int j = 0;

					while(it.hasNext()) {
						String s = (String) it.next();

						if(i == j) {
							loadSet(s);
							DesktopEnvironment.repaintComponent("MAP");

							break;
						}

						j++;
					}
				}
			}
		}

		/**
		 * DOCUMENT-ME
		 *
		 * 
		 *
		 * 
		 */
		public String getShortcutDescription(Object stroke) {
			return null;
		}

		/**
		 * DOCUMENT-ME
		 *
		 * 
		 */
		public String getListenerDescription() {
			return null;
		}
	}

	protected class ATRPreferences extends JPanel implements ActionListener, PreferencesAdapter,
															 ListSelectionListener
	{
		protected AdvancedTextCellRenderer s;

		// global properties
		protected JCheckBox linebreak;
		protected JRadioButton align[];

		// set-local properties
		protected JTextField replace;
		protected JTextArea def;

		// ui things
		protected JList list;
		protected DefaultListModel listModel;
		protected AbstractButton add;
		protected AbstractButton remove;
		protected AbstractButton rename;
		protected AbstractButton importB;
		protected AbstractButton export;

		/**
		 * Creates a new ATRPreferences object.
		 *
		 * 
		 */
		public ATRPreferences(AdvancedTextCellRenderer s) {
			this.s = s;
			this.setLayout(new GridBagLayout());

			GridBagConstraints c = new GridBagConstraints(0, 0, 2, 1, 1.0, 1.0,
														  GridBagConstraints.CENTER,
														  GridBagConstraints.HORIZONTAL,
														  new Insets(1, 1, 1, 1), 0, 0);

			JTextArea fontHelp = new JTextArea(Resources.get("magellan.map.advancedtextcellrenderer.prefs.fonthelp"));
			fontHelp.setEditable(false);
			fontHelp.setBorder(null);
			fontHelp.setBackground(this.getBackground());
			fontHelp.setLineWrap(true);
			fontHelp.setWrapStyleWord(true);

			this.add(fontHelp, c);

			c.gridy++;
			this.add(new JSeparator(JSeparator.HORIZONTAL), c);

			c.gridwidth = 1;
			c.fill = GridBagConstraints.NONE;
			c.gridy++;

			linebreak = new JCheckBox(Resources.get("magellan.map.advancedtextcellrenderer.prefs.breakline"), s.breakLines);
			this.add(linebreak, c);

			c.gridx = 1;
			this.add(createAlignPanel(), c);

			c.gridx = 0;
			c.gridy++;
			c.gridwidth = 2;
			c.fill = GridBagConstraints.HORIZONTAL;
			this.add(new JSeparator(JSeparator.HORIZONTAL), c);

			c.gridwidth = 2;
			c.gridy++;
			c.fill = GridBagConstraints.BOTH;
			this.add(createFullSetPanel(), c);
		}

		protected Component createFullSetPanel() {
			JPanel help = new JPanel(new BorderLayout());

			help.add(createSetPanel(), BorderLayout.CENTER);
			help.add(createNavPanel(), BorderLayout.WEST);

			return help;
		}

		protected Component createNavPanel() {
			add = new JButton(Resources.get("magellan.map.advancedtextcellrenderer.prefs.add"));
			add.addActionListener(this);
			remove = new JButton(Resources.get("magellan.map.advancedtextcellrenderer.prefs.remove"));
			remove.addActionListener(this);
			rename = new JButton(Resources.get("magellan.map.advancedtextcellrenderer.prefs.rename"));
			rename.addActionListener(this);
			importB = new JButton(Resources.get("magellan.map.advancedtextcellrenderer.prefs.import"));
			importB.addActionListener(this);
			export = new JButton(Resources.get("magellan.map.advancedtextcellrenderer.prefs.export"));
			export.addActionListener(this);

			listModel = new DefaultListModel();
			list = new JList(listModel);
			list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
			fillList(set.getName());

			JPanel p = new JPanel(new GridBagLayout());
			GridBagConstraints c = new GridBagConstraints(0, 0, 1, 1, 1.0, 1.0,
														  GridBagConstraints.WEST,
														  GridBagConstraints.HORIZONTAL,
														  new Insets(0, 1, 1, 1), 0, 0);

			p.add(add, c);
			c.gridy++;
			p.add(rename, c);
			c.gridy++;
			p.add(remove, c);
			c.gridy++;
			p.add(new JSeparator(JSeparator.HORIZONTAL), c);
			c.gridy++;
			p.add(importB, c);
			c.gridy++;
			p.add(export, c);
			c.gridy++;
			c.fill = GridBagConstraints.BOTH;
			p.add(new JScrollPane(list), c);

			list.addListSelectionListener(this);

			return p;
		}

		protected Component createSetPanel() {
			JPanel sPanel = new JPanel(new BorderLayout());

			JPanel help = new JPanel(new FlowLayout(FlowLayout.CENTER));
			help.add(new JLabel(Resources.get("magellan.map.advancedtextcellrenderer.prefs.replace")));
			help.add(replace = new JTextField(set.getUnknown(), 5));
			sPanel.add(help, BorderLayout.NORTH);

			help = new JPanel();
			help.add(new JLabel(Resources.get("magellan.map.advancedtextcellrenderer.prefs.def"))); //Resources.get("magellan.map.advancedtextcellrenderer.prefs.planes")),c);
			help.add(new JScrollPane(def = new JTextArea(set.getDef(), 15, 30)));
			sPanel.add(help, BorderLayout.CENTER);

			return sPanel;
		}

		protected Component createAlignPanel() {
			Box box = new Box(BoxLayout.X_AXIS);
			box.add(new JLabel(Resources.get("magellan.map.advancedtextcellrenderer.prefs.aligntext")));
			box.add(Box.createHorizontalStrut(5));

			ButtonGroup group = new ButtonGroup();
			align = new JRadioButton[3];

			for(int i = 0; i < 3; i++) {
				align[i] = new JRadioButton(Resources.get("magellan.map.advancedtextcellrenderer.prefs.align" + String.valueOf(i)),
											i == s.getHAlign());
				group.add(align[i]);
				box.add(align[i]);
			}

			return box;
		}

		protected void fillList() {
			Collection c = s.getAllSets();

			if(c.size() > 0) {
				fillList(c.iterator().next());
			} else {
				fillList(null);
			}
		}

		protected void fillList(Object select) {
			Collection c = s.getAllSets();
			listModel.removeAllElements();

			Iterator it = c.iterator();

			while(it.hasNext()) {
				listModel.addElement(it.next());
			}

			if(select != null) {
				list.setSelectedValue(select, true);
			} else {
				list.setSelectedIndex(-1);
			}
		}

		/**
		 * DOCUMENT-ME
		 *
		 * 
		 */
		public void valueChanged(ListSelectionEvent e) {
			if(list.getSelectedValue() != null) {
				if((set == null) || (list.getSelectedValue() != set.getName())) {
					if(set != null) {
						saveSettings();
					}

					loadSet((String) list.getSelectedValue());
					updateSet();
				}
			}
		}

		protected void updateSet() {
			replace.setText(set.getUnknown());
			def.setText(set.getDef());
		}

        public void initPreferences() {
            // TODO: implement it
        }

		/**
		 * DOCUMENT-ME
		 */
		public void applyPreferences() {
			saveSettings();
			s.setBreakLines(linebreak.isSelected());

			for(int i = 0; i < 3; i++) {
				if(align[i].isSelected()) {
					s.setHAlign(i);

					break;
				}
			}
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
			return s.getName();
		}

		/**
		 * DOCUMENT-ME
		 *
		 * 
		 */
		public void actionPerformed(ActionEvent e) {
			if(e.getSource() == add) {
				addSet();
			} else if(e.getSource() == rename) {
				renameSet();
			} else if(e.getSource() == remove) {
				removeSet();
			} else if(e.getSource() == importB) {
				importSet();
			} else if(e.getSource() == export) {
				exportSet();
			}
		}

		protected void saveSettings() {
			set.setDef(def.getText());
			set.setUnknown(replace.getText());
			saveSet();
		}

		protected void addSet() {
			String name = JOptionPane.showInputDialog(this, Resources.get("magellan.map.advancedtextcellrenderer.prefs.add.text"));

			if((name != null) && !name.trim().equals("")) {
				if(exists(name)) {
					JOptionPane.showMessageDialog(this, Resources.get("magellan.map.advancedtextcellrenderer.prefs.nameexists"));
				} else {
					saveSettings();
					loadSet(name);
					fillList(name);
				}
			}
		}

		protected void renameSet() {
			String newName = JOptionPane.showInputDialog(this, Resources.get("magellan.map.advancedtextcellrenderer.prefs.rename.text"));

			if((newName != null) && !newName.trim().equals("")) {
				if(exists(newName)) {
					JOptionPane.showMessageDialog(this, Resources.get("magellan.map.advancedtextcellrenderer.prefs.nameexists"));
				} else {
					s.removeSet(set.getName());
					set.setName(newName);
					saveSettings();
					fillList(newName);
				}
			}
		}

		protected void removeSet() {
			String name = (String) list.getSelectedValue();

			if(name != null) {
				s.removeSet(name);

				ATRSet old = set;
				set = null;
				fillList();

				if(set == null) {
					set = old;
				}
			}
		}

		protected void importSet() {
			JFileChooser jfc = new JFileChooser(magellan.client.Client.getMagellanDirectory());
			int ret = jfc.showOpenDialog(this);

			if(ret == JFileChooser.APPROVE_OPTION) {
				java.io.File f = jfc.getSelectedFile();
				String lastName = set.getName();

				try {
					Properties prop = new Properties();
					prop.load(new java.io.FileInputStream(f));

					String sets = prop.getProperty("ATR.Sets", "");
					StringTokenizer st = new StringTokenizer(sets, ";");

					while(st.hasMoreTokens()) {
						String retS = importImpl(st.nextToken(), prop);

						if(retS != null) {
							lastName = retS;
						}
					}
				} catch(Exception exc) {
				}

				fillList(lastName);
			}
		}

		protected String importImpl(String name, Properties prop) {
			String oldName = name;

			while((name != null) && exists(name)) {
				name = JOptionPane.showInputDialog(this, Resources.get("magellan.map.advancedtextcellrenderer.prefs.nameexists2") + name);
			}

			if(name == null) {
				return null;
			}

			saveSettings();
			s.loadSet(oldName, prop);
			set.setName(name);
			updateSet();
			saveSettings();

			return name;
		}

		protected void exportSet() {
			JFileChooser jfc = new JFileChooser(magellan.client.Client.getMagellanDirectory());
			int ret = jfc.showSaveDialog(this);

			if(ret == JFileChooser.APPROVE_OPTION) {
				try {
					java.io.File f = jfc.getSelectedFile();
					FileOutputStream out = new FileOutputStream(f);
					Properties prop = new Properties();
					saveSet(prop);
					prop.store(out, "ATR exported settings");
					out.close();
				} catch(IOException exc) {
				}
			}
		}
	}
}
