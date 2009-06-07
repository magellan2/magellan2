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
import java.awt.Font;
import java.awt.Graphics;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Rectangle;
import java.util.Iterator;
import java.util.Properties;

import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.LookAndFeel;
import javax.swing.plaf.basic.BasicHTML;
import javax.swing.text.View;

import magellan.client.event.EventDispatcher;
import magellan.client.event.SelectionEvent;
import magellan.client.event.SelectionListener;
import magellan.client.swing.layout.GridBagHelper;
import magellan.client.swing.preferences.PreferencesAdapter;
import magellan.library.HasRegion;
import magellan.library.Region;
import magellan.library.event.GameDataEvent;
import magellan.library.utils.Resources;
import magellan.library.utils.logging.Logger;
import magellan.library.utils.replacers.ReplacerHelp;
import magellan.library.utils.replacers.ReplacerSystem;


/**
 * A GUI component displaying very basic data about regions.
 */
public class BasicRegionPanel extends InternationalizedDataPanel implements SelectionListener {
	private static final Logger log = Logger.getInstance(BasicRegionPanel.class);
	private ReplacerSystem replacer;
	private HTMLLabel html;

	//private JLabel html;
	private String def;
	private Region lastRegion;

	/**
	 * Creates a new BasicRegionPanel object.
	 *
	 * 
	 * 
	 */
	public BasicRegionPanel(EventDispatcher d, Properties p) {
		super(d, p);
		dispatcher.addSelectionListener(this);
		init();
	}

	/**
	 * DOCUMENT-ME
	 *
	 * 
	 */
	@Override
  public void gameDataChanged(GameDataEvent e) {
		lastRegion = null;
		parseDefinition(def);
	}

	/**
	 * DOCUMENT-ME
	 *
	 * 
	 */
	public void selectionChanged(SelectionEvent e) {
		Object o = e.getActiveObject();

		if(o instanceof Region) {
			show((Region) o);
		} else if(o instanceof HasRegion) {
			show(((HasRegion) o).getRegion());
		}
	}

	private void show(Region r) {
		lastRegion = r;

		Object rep = replacer.getReplacement(r);

		// we dont take care if there are unreplaceable stuff
		html.setText(rep.toString());
	}

	private void init() {
		def = settings.getProperty("BasicRegionPanel.Def");

		if(def == null) {
			def = Resources.get("basicregionpanel.default");
		}

		if(!BasicHTML.isHTMLString(def)) {
			// preparse the definition if not html format
			def = BasicRegionPanel.makeHTMLFromString(def, false);
			settings.setProperty("BasicRegionPanel.Def", def);
		}

		parseDefinition(def);
		this.setLayout(new GridBagLayout());

		GridBagConstraints c = new GridBagConstraints();

		GridBagHelper.setConstraints(c, 0, 0, GridBagConstraints.REMAINDER, 1, 1.0, 1.0, /* different weighty!*/
									 GridBagConstraints.NORTHWEST, GridBagConstraints.HORIZONTAL,
									 c.insets, 0, 0);

		//html = new JLabel();

		//html.setContentType("text/html");
		//html.setFont(new Font("Arial", Font.PLAIN, html.getFont().getSize()));
		//html.setEditable(false);
		add(html = new HTMLLabel(), c);

		// parse the def for ourself
		show(null);
	}

	// one separator on x labels, or no separators (x<=0)
	private static final int separatorDist = 2;

	/**
	 * this is a helper function to migrate the old stuff to the new html layout
	 *
	 * 
	 * 
	 *
	 * 
	 */
	public static String makeHTMLFromString(String def, boolean filter) {
		StringBuffer sb = new StringBuffer();
		sb.append("<html>\n<body>\n<table  cellpadding=0 width=100%>\n");

		if(BasicRegionPanel.log.isDebugEnabled()) {
			BasicRegionPanel.log.debug("BasicRegionPanel.makeHTMLFromString: string (of length " + def.length() +
					  "):\n" + def);
		}

		for(Iterator iterRow = new BasicStringTokenizer(def, "\\\\"); iterRow.hasNext();) {
			// create new rows
			String row = (String) iterRow.next();

			if(BasicRegionPanel.log.isDebugEnabled()) {
				BasicRegionPanel.log.debug("BasicRegionPanel.makeHTMLFromString: working on row " + row);
			}

			sb.append("<tr>\n");

			int i = 0;

			for(Iterator iter = new BasicStringTokenizer(row, "&&"); iter.hasNext();) {
				String str = (String) iter.next();
				sb.append("<td>");

				if(!filter || (str.indexOf('?') == -1)) {
					// filter unreplaced strings?
					sb.append(str);
				}

				sb.append("</td>\n");
				i++;

				if(((i % BasicRegionPanel.separatorDist) == 0) && iter.hasNext()) {
					// mod seperatorDist: include new separator
					sb.append("<td></td>\n");
				}
			}

			sb.append("</tr>\n");
		}

		sb.append("</table>\n</body>\n</html>");

		String htmlText = sb.toString();

		if(BasicRegionPanel.log.isDebugEnabled()) {
			BasicRegionPanel.log.debug("BasicRegionPanel.makeHTMLFromString: transforming string \n" +
					  def.replace('?', '#') + "\" to " + htmlText.replace('?', '#'));
		}

		return htmlText;
	}

	protected Dimension getDimension(String def) {
		int cols = 1;
		int rows = 1;
		int index = 0;
		int curcols = 1;

		while((def.indexOf("&&", index) >= 0) || (def.indexOf("\\\\", index) >= 0)) {
			int index1 = def.indexOf("&&", index);

			if(index1 == -1) {
				index1 = Integer.MAX_VALUE;
			}

			int index2 = def.indexOf("\\\\", index);

			if(index2 == -1) {
				index2 = Integer.MAX_VALUE;
			}

			if(index1 < index2) {
				curcols++;
				index = index1 + 2;
			} else {
				if(curcols > cols) {
					cols = curcols;
				}

				curcols = 1;
				rows++;
				index = index2 + 2;
			}

			if(curcols > cols) {
				cols = curcols;
			}
		}

		return new Dimension(cols, rows);
	}

	protected void parseDefinition(String def) {
		replacer = ReplacerHelp.createReplacer(def);
	}

	/**
	 * DOCUMENT-ME
	 */
	public PreferencesAdapter getPreferredAdapter() {
		return new BRPPreferences();
	}

	/**
	 * DOCUMENT-ME
	 */
	public String getDefinition() {
		return def;
	}

	/**
	 * DOCUMENT-ME
	 */
	public void setDefinition(String d) {
		settings.setProperty("BasicRegionPanel.Def", d);
		def = d;
		parseDefinition(def);
		show(lastRegion);
	}

	protected class BRPPreferences extends JPanel implements PreferencesAdapter {
		protected JTextArea defText;

		/**
		 * Creates a new BRPPreferences object.
		 */
		public BRPPreferences() {
			this.setLayout(new BorderLayout());
			this.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), Resources.get("basicregionpanel.prefs.title")));
      JPanel panel = new JPanel(new BorderLayout());

			//text pane
			defText = new JTextArea(getDefinition());
      
      panel.setPreferredSize(new Dimension(300,300));
      panel.add(new JScrollPane(defText), BorderLayout.CENTER);
      
			this.add(panel, BorderLayout.CENTER);
		}

    public void initPreferences() {
          // TODO: implement it
    }

		/**
		 * DOCUMENT-ME
		 */
		public void applyPreferences() {
			String t = defText.getText();

			if(!t.equals(getDefinition())) {
				setDefinition(t);
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
			return Resources.get("basicregionpanel.prefs.title");
		}
	}

	/**
	 * This class emulates the behaviour of StringTokenizer with a string as delimiter.
	 */
	public static class BasicStringTokenizer implements Iterator {
		int newPosition = -1;
		int currentPosition = 0;
		int maxPosition = 0;
		String str;
		String delim;

		BasicStringTokenizer(String str, String delim) {
			this.str = delim + str + delim;
			this.delim = delim;
			maxPosition = str.length();
		}

		/**
		 * DOCUMENT-ME
		 *
		 * 
		 */
		public boolean hasNext() {
			newPosition = skipDelims(currentPosition);

			return newPosition < maxPosition;
		}

		/**
		 * Skips ahead from startPos and returns the index of the next delimiter character
		 * encountered, or maxPosition if no such delimiter is found.
		 *
		 * 
		 *
		 * 
		 */
		private int scanToken(int startPos) {
			int position = str.indexOf(delim, startPos);

			if(position == -1) {
				position = maxPosition;
			}

			return position;
		}

		/**
		 * Skips delimiters starting from the specified position. Returns the index of the first
		 * non-delimiter character at or after startPos.
		 *
		 * 
		 *
		 * 
		 */
		private int skipDelims(int startPos) {
			int position = str.indexOf(delim, startPos);
			int ret = startPos;

			if(position == startPos) {
				ret += delim.length();
			}

			return ret;
		}

		/**
		 * DOCUMENT-ME
		 *
		 * 
		 *
		 * @throws java.util.NoSuchElementException DOCUMENT-ME
		 */
		public Object next() {
			currentPosition = (newPosition > 0) ? newPosition : skipDelims(currentPosition);

			if(currentPosition >= maxPosition) {
				throw new java.util.NoSuchElementException();
			}

			// reset newPosition
			newPosition = -1;

			int start = currentPosition;
			currentPosition = scanToken(currentPosition);

			String ret = str.substring(start, currentPosition);

			return ret;
		}

		/**
		 * DOCUMENT-ME
		 *
		 * @throws UnsupportedOperationException DOCUMENT-ME
		 */
		public void remove() {
			throw new UnsupportedOperationException();
		}
	}

	/**
	 * DOCUMENT-ME
	 *
	 * @author $Author: $
	 * @version $Revision: 352 $
	 */
	public static class HTMLLabel extends JComponent {
    private static final Logger log = Logger.getInstance(HTMLLabel.class);

		private String text;
		private transient View view;

		/**
		 * requires: 'text' is HTML string.
		 *
		 * 
		 */
		public HTMLLabel(String text) {
			// we need to install the LookAndFeel Fonts from the beginning
			LookAndFeel.installColorsAndFont(this, "Label.background", "Label.foreground",
											 "Label.font");
			setText(text);
		}

		/**
		 * Creates a new HTMLLabel object.
		 */
		public HTMLLabel() {
			this("<html></html>");
		}

		/**
		 * DOCUMENT-ME
		 */
		@Override
    public void updateUI() {
			super.updateUI();
			LookAndFeel.installColorsAndFont(this, "Label.background", "Label.foreground",
											 "Label.font");
		}

		/**
		 * requires: 's' is HTML string.
		 *
		 * 
		 *
		 * @throws IllegalArgumentException DOCUMENT-ME
		 */
		public void setText(String s) {
			if(HTMLLabel.equal(s, text)) {
				return;
			}

			if(!BasicHTML.isHTMLString(s)) {
				throw new IllegalArgumentException();
			}

			text = s;

			revalidate();
			repaint();

			view = null;
		}

		/**
		 * DOCUMENT-ME
		 *
		 * 
		 */
		public String getText() {
			return text;
		}

		/**
		 * DOCUMENT-ME
		 *
		 * 
		 */
		@Override
    public Dimension getMinimumSize() {
			if(isMinimumSizeSet()) {
				return super.getMinimumSize();
			}

			Insets i = getInsets();

			Dimension d = new Dimension(i.left + i.right, i.top + i.bottom);

			d.width += view().getMinimumSpan(View.X_AXIS);
			d.height += view().getMinimumSpan(View.Y_AXIS);

			return d;
		}

		/**
		 * DOCUMENT-ME
		 *
		 * 
		 */
		@Override
    public Dimension getPreferredSize() {
			if(isPreferredSizeSet()) {
				return super.getPreferredSize();
			}

			Insets i = getInsets();

			Dimension d = new Dimension(i.left + i.right, i.top + i.bottom);

			d.width += view().getPreferredSpan(View.X_AXIS);
			d.height += view().getPreferredSpan(View.Y_AXIS);

			return d;
		}

		/**
		 * DOCUMENT-ME
		 *
		 * 
		 */
		@Override
    public Dimension getMaximumSize() {
			if(isMaximumSizeSet()) {
				return super.getMaximumSize();
			}

			Insets i = getInsets();

			Dimension d = new Dimension(i.left + i.right, i.top + i.bottom);

			d.width += view().getMaximumSpan(View.X_AXIS);
			d.height += view().getMaximumSpan(View.Y_AXIS);

			return d;
		}

		protected View view() {
			if(view == null) {
				view = BasicHTML.createHTMLView(this, text);
			}

			return view;
		}

		/**
		 * DOCUMENT-ME
		 *
		 * 
		 */
		@Override
    public void setForeground(Color c) {
			if(!HTMLLabel.equal(c, getForeground())) {
				view = null;
			}

			super.setForeground(c);
		}

		/**
		 * DOCUMENT-ME
		 *
		 * 
		 */
		@Override
    public void setFont(Font f) {
			if(!HTMLLabel.equal(f, getFont())) {
				view = null;
			}

			if(HTMLLabel.log.isDebugEnabled()) {
				HTMLLabel.log.debug("HTMLLabel.setFont(" + f + " called");
			}

			super.setFont(f);
		}

		private static boolean equal(Object a, Object b) {
			return (a == null) ? (b == null) : a.equals(b);
		}

		@Override
    protected void paintComponent(Graphics g) {
			if(isOpaque()) // incorrect, but done as everywhere
			 {
				g.setColor(getBackground());
				g.fillRect(0, 0, getWidth(), getHeight());
			}

			view().paint(g, HTMLLabel.calculateInnerArea(this, null));
		}

		/**
		 * DOCUMENT-ME
		 *
		 * 
		 * 
		 *
		 * 
		 */
		public static Rectangle calculateInnerArea(JComponent c, Rectangle r) {
			if(c == null) {
				return null;
			}

			Rectangle rect = r;
			Insets insets = c.getInsets();

			if(rect == null) {
				rect = new Rectangle();
			}

			rect.x = insets.left;
			rect.y = insets.top;
			rect.width = c.getWidth() - insets.left - insets.right;
			rect.height = c.getHeight() - insets.top - insets.bottom;

			return rect;
		}
	}
}
