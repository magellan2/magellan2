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
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;
import java.util.StringTokenizer;

import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;
import javax.swing.text.html.HTMLDocument;
import javax.swing.text.html.HTMLEditorKit;

import magellan.client.desktop.DesktopEnvironment;
import magellan.client.desktop.MagellanDesktop;
import magellan.library.utils.MagellanImages;
import magellan.library.utils.Resources;


/**
 * DOCUMENT ME!
 *
 * @author Andreas
 * @version
 */
public class TipOfTheDay extends InternationalizedDialog implements ActionListener {
	// the pane to display the text
	protected JTextPane tipText;
	protected HTMLDocument doc;
	protected HTMLEditorKit kit;
	protected int lastNumber = -1;

	// a checkbox for marking the show state
	protected JCheckBox showTips;

	// settings to store used tips
	protected Properties settings;

	// all tips in language file
	protected List<Tip> allTips;

	// current usable (non-shown) tips
	protected List<Tip> nonShown;
	protected static final String NEXT = "next";

	/** DOCUMENT-ME */
	public static final String HTML_START = "<html><body style=\"margin-left: 40px;margin-top: 15px\"><font color=#4f3f30><b>";

	/** DOCUMENT-ME */
	public static final String HTML_END = "</b></font></body></html>";

	/** DOCUMENT-ME */
	public static final String E_HTML_START = "<html><body style=\"margin-left: 40px;margin-top: 2px\"><font color=#4f3f30><b>";

	/** DOCUMENT-ME */
	public static final String E_HTML_END = "</b></font></body></html>";

	/** DOCUMENT-ME */
	public static final String E_KEY = "ETIP";

	/** DOCUMENT-ME */
	public static boolean active = false;

	/** DOCUMENT-ME */
	public static boolean firstTime = false;

	/**
	 * Creates new TipOfTheDay
	 *
	 * 
	 * 
	 */
	public TipOfTheDay(Frame parent, Properties settings) {
		super(parent, false);

		setTitle(Resources.get("tipoftheday.title"));

		this.settings = settings;

		// init the interface
		initUI();

		// load tips
		initTips();

		pack();

		setLocation(parent);
	}

	protected void setLocation(Component parent) {
		// center if using frame mode
		if(DesktopEnvironment.getMode() == MagellanDesktop.MODE_FRAME) {
			Dimension screen = getToolkit().getScreenSize();
			Dimension size = getSize();
			int x = (screen.width - size.width) / 2;
			int y = (screen.height - size.height) / 2;
			setLocation(x, y);
		} else {
			setLocationRelativeTo(parent);
		}
	}

	/* initializes the dialog
	 *
	 * design is:
	 *
	 * ++++++++++++++++++++++++++++++
	 * + Image + "Did you know?"    +
	 * +       ++++++++++++++++++++++
	 * +       +        Tip         +
	 * ++++++++++++++++++++++++++++++
	 * + Use           +Next/Close  +
	 * ++++++++++++++++++++++++++++++
	 *
	 **/
	protected void initUI() {
		Color foreground = new Color(79, 63, 48);
		Color background = new Color(213, 169, 131);

		JPanel panel = new JPanel(new BorderLayout(2, 2));
		panel.setBorder(BorderFactory.createEmptyBorder(3, 3, 3, 3));
		panel.setBackground(foreground);

		// try to find the image
		Icon icon = null;
		JLabel iconLabel = null;

		try {
			icon = MagellanImages.GUI_TOTD;
		} catch(RuntimeException exc) {
		}

		if(icon != null) {
			iconLabel = new JLabel(icon);
			iconLabel.setBackground(background);
			iconLabel.setOpaque(true);
			panel.add(iconLabel, BorderLayout.WEST);
		} else {
			iconLabel = new JLabel(" Pic here ");
			iconLabel.setBackground(background);
			iconLabel.setOpaque(true);
			panel.add(iconLabel, BorderLayout.WEST);
		}

		JLabel didyouknow = new JLabel("    " + Resources.get("tipoftheday.didyouknow"));
		Font old = didyouknow.getFont();
		didyouknow.setFont(old.deriveFont(old.getStyle() | Font.ITALIC, 18f));
		didyouknow.setForeground(foreground);
		didyouknow.setBackground(background);
		didyouknow.setOpaque(true);

		kit = new HTMLEditorKit();
		tipText = new JTextPane();
		tipText.setEditorKit(kit);
		tipText.setForeground(foreground);
		tipText.setBackground(background);
		tipText.setBorder(null);

		JScrollPane pane = new JScrollPane(tipText);
		pane.setBorder(null);

		JPanel content = new JPanel(new BorderLayout(0, 2));
		content.setBackground(foreground);
		content.add(didyouknow, BorderLayout.NORTH);
		content.add(pane, BorderLayout.CENTER);
		content.setPreferredSize(new Dimension(300, 200));

		panel.add(content, BorderLayout.CENTER);

		showTips = new JCheckBox(Resources.get("tipoftheday.showTips"),
								 settings.getProperty("TipOfTheDay.showTips", "true").equals("true"));
		showTips.setBackground(background);
		showTips.setForeground(foreground);

		JButton close = new JButton(Resources.get("tipoftheday.close"));
		close.addActionListener(this);
		close.setBackground(background);
		close.setForeground(foreground);

		JButton next = new JButton(Resources.get("tipoftheday.nextTip"));
		next.addActionListener(this);
		next.setActionCommand(NEXT);
		next.setBackground(background);
		next.setForeground(foreground);

		JPanel actions = new JPanel(new GridBagLayout());
		actions.setBackground(background);

		GridBagConstraints c = new GridBagConstraints(0, 0, 1, 1, 1, 1, GridBagConstraints.WEST,
													  GridBagConstraints.NONE,
													  new Insets(1, 1, 1, 1), 0, 0);
		actions.add(showTips, c);

		JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT));
		buttons.setBackground(background);
		buttons.add(next);
		buttons.add(close);
		c.gridx = 1;
		c.anchor = GridBagConstraints.EAST;
		actions.add(buttons, c);

		panel.add(actions, BorderLayout.SOUTH);
		setContentPane(panel);

		addWindowListener(new WindowAdapter() {
				public void windowClosing(WindowEvent e) {
					quit();
				}
			});
	}

	protected void initTips() {
		allTips = new LinkedList<Tip>();
		nonShown = new LinkedList<Tip>();

		// load all tips
		int count = 0;

		try {
			count = Integer.parseInt(Resources.get("tipoftheday.numTips"));
		} catch(Exception exc) {
		}

		if(count > 0) {
			for(int i = 0; i < count; i++) {
				try {
					String s = Resources.get("tipoftheday.tip." + String.valueOf(i));

					if(s != null) {
						Tip tip = new Tip();
						tip.number = i;
						tip.text = s;
						allTips.add(tip);
					}
				} catch(Exception exc2) {
				}
			}

			String nonS = settings.getProperty("TipOfTheDay.Tips");

			if((nonS != null) && !nonS.equals("")) {
				StringTokenizer st = new StringTokenizer(nonS, ",");

				while(st.hasMoreTokens()) {
					try {
						int num = Integer.parseInt(st.nextToken());
						Iterator it = allTips.iterator();

						while(it.hasNext()) {
							Tip tip = (Tip) it.next();

							if(tip.number == num) {
								nonShown.add(tip);

								break;
							}
						}
					} catch(Exception exc3) {
					}
				}
			} else {
				reloadTips();
			}
		}

		if(settings.getProperty("TipOfTheDay.firstTime", "true").equals("true")) {
			firstTime = true;
			settings.setProperty("TipOfTheDay.firstTime", "false");
		}
	}

	/**
	 * DOCUMENT-ME
	 *
	 * 
	 */
	public boolean doShow() {
		return firstTime || hasTips();
	}

	/**
	 * DOCUMENT-ME
	 *
	 * 
	 */
	public boolean hasTips() {
		return allTips.size() > 0;
	}

	/**
	 * DOCUMENT-ME
	 */
	public void showNextTip() {
		if(hasTips() || firstTime) {
			// should be
			if(nonShown.size() == 0) {
				reloadTips();
			}

			Tip tip = null;

			if(firstTime) {
				firstTime = false;
				tip = new Tip();
				tip.text = Resources.get("tipoftheday.warningString");
			} else {
				int i = 0;

				do {
					i++;

					int pos = (int) (Math.random() * nonShown.size());
					tip = (Tip) nonShown.get(pos);
				} while((i < 10) && (nonShown.size() != 1) && (tip.number == lastNumber));

				lastNumber = tip.number;
				nonShown.remove(tip);

				if(nonShown.size() == 0) {
					reloadTips();
				}
			}

			try {
				HTMLDocument doc2 = new HTMLDocument();

				if(tip.text.startsWith(E_KEY)) {
					kit.insertHTML(doc2, 0,
								   E_HTML_START + "<font size=+1>" + Resources.get("tipoftheday.tip.eressea") +
								   "</font><br>" + tip.text.substring(E_KEY.length()) + E_HTML_END,
								   0, 0, null);
				} else {
					kit.insertHTML(doc2, 0, HTML_START + tip.text + HTML_END, 0, 0, null);
				}

				tipText.setStyledDocument(doc2);
				doc = doc2;
				repaint();
			} catch(Exception exc) {
				System.out.println(exc);
			}
		}
	}

	protected void reloadTips() {
		nonShown.clear();
		nonShown.addAll(allTips);
	}

	/**
	 * DOCUMENT-ME
	 *
	 * 
	 */
	public void actionPerformed(ActionEvent e) {
		if(NEXT.equals(e.getActionCommand())) {
			showNextTip();

			return;
		}

		quit();
	}

	/**
	 * DOCUMENT-ME
	 */
	public void show() {
		active = true;
		super.show();
	}

	/**
	 * DOCUMENT-ME
	 *
	 * 
	 */
	public void setVisible(boolean flag) {
		super.setVisible(flag);
		active = flag;
	}

	// close the dialog and save the settings
	protected void quit() {
		setVisible(false);

		StringBuffer buf = new StringBuffer();
		Iterator it = nonShown.iterator();

		while(it.hasNext()) {
			Tip tip = (Tip) it.next();
			buf.append(tip.number);

			if(it.hasNext()) {
				buf.append(",");
			}
		}

		settings.setProperty("TipOfTheDay.Tips", buf.toString());

		settings.setProperty("TipOfTheDay.showTips", showTips.isSelected() ? "true" : "false");
	}

	// simple pair of number and html text
	protected class Tip {
		protected int number;
		protected String text;
	}
}
