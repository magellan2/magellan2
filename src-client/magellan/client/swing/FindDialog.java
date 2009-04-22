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
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.StreamTokenizer;
import java.io.StringReader;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;
import java.util.StringTokenizer;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.border.TitledBorder;

import magellan.client.Client;
import magellan.client.event.EventDispatcher;
import magellan.client.event.SelectionEvent;
import magellan.client.event.SelectionListener;
import magellan.library.Building;
import magellan.library.Described;
import magellan.library.Faction;
import magellan.library.GameData;
import magellan.library.ID;
import magellan.library.Item;
import magellan.library.Message;
import magellan.library.Named;
import magellan.library.Region;
import magellan.library.Ship;
import magellan.library.Unique;
import magellan.library.Unit;
import magellan.library.UnitContainer;
import magellan.library.event.GameDataEvent;
import magellan.library.utils.Resources;
import magellan.library.utils.comparator.IDComparator;
import magellan.library.utils.comparator.NameComparator;
import magellan.library.utils.logging.Logger;



/**
 * DOCUMENT-ME
 *
 * @author $Author: $
 * @version $Revision: 324 $
 */
public class FindDialog extends InternationalizedDataDialog
	implements javax.swing.event.ListSelectionListener, SelectionListener
{
	private static final Logger log = Logger.getInstance(FindDialog.class);
	private JComboBox txtPattern = null;
	private JCheckBox chkIDs = null;
	private JCheckBox chkNames = null;
	private JCheckBox chkDescs = null;
	private JCheckBox chkCmds = null;
	private JCheckBox chkMessages = null;
	private JCheckBox chkItems = null;
	private JComboBox factionCombo = null;
	private JCheckBox addUnits = null;
	private JCheckBox addRegions = null;
	private JCheckBox addBuildings = null;
	private JCheckBox addShips = null;
	private JCheckBox addTraitors = null;
	private JCheckBox addOnlyUnconfirmedUnits = null;
	private JCheckBox addFactions = null;
	private JList resultList = null;
	private JPanel pnlResults = null;
	private List<String> history;

	// selected regions that are stored as region-objects!
	private List<Region> selectedRegions = new LinkedList<Region>();

	/**
	 * DOCUMENT ME!
	 *
	 * 
	 * 
	 * 
	 * 
	 * 
	 * @param regions A collection of region objects that were selected on the map. Up to the next
	 * 		  SelectionEvent with type ST_REGIONS any search will be limited to these regions.
	 */
	public FindDialog(Frame owner, boolean modal, EventDispatcher dispatcher, GameData d,
					  Properties p, Collection<Region> regions) {
		super(owner, modal, dispatcher, d, p);
		dispatcher.addSelectionListener(this);
		dispatcher.addGameDataListener(this);
		selectedRegions.addAll(regions);
		data = d;
		settings = p;
		this.dispatcher = dispatcher;

		setTitle(Resources.get("finddialog.window.title"));
		setContentPane(getMainPane());
		setSize(420, 500);

		Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
		int x = Integer.parseInt(settings.getProperty("FindDialog.x",
													  ((screen.width - getWidth()) / 2) + ""));
		int y = Integer.parseInt(settings.getProperty("FindDialog.y",
													  ((screen.height - getHeight()) / 2) + ""));
		setLocation(x, y);
	}

	/**
	 * DOCUMENT-ME
	 *
	 * 
	 */
	public void selectionChanged(SelectionEvent s) {
		if((s == null) || (s.getSelectedObjects() == null)) {
			return;
		}

		if(s.getSelectionType() == SelectionEvent.ST_REGIONS) {
			// some regions on the map were selected or deselected
			// it is assumed that selections of this type contain only regions
			selectedRegions.clear();

			for(Iterator iter = s.getSelectedObjects().iterator(); iter.hasNext();) {
				Region r = (Region) iter.next();
				selectedRegions.add(r);
			}
		}
	}

	/**
	 * 
	 */
  @Override
	public void gameDataChanged(GameDataEvent e) {
		this.data = e.getGameData();
		this.selectedRegions.clear();
	}

	/**
	 * DOCUMENT-ME
	 *
	 * 
	 */
	public void valueChanged(javax.swing.event.ListSelectionEvent e) {
		if(e.getValueIsAdjusting()) {
			return;
		}

		Object o = resultList.getSelectedValue();
		Object fireObject = null;

		if(o != null) {
			if(o instanceof RegionWrapper) {
				fireObject = ((RegionWrapper) o).getRegion();
			} else if(o instanceof UnitWrapper) {
				fireObject = ((UnitWrapper) o).getUnit();
			} else {
				fireObject = o;
			}
		}

		if(fireObject != null) {
			dispatcher.fire(new SelectionEvent<Object>(this, null, fireObject));
		}
	}

	private JPanel getMainPane() {
		JPanel main = new JPanel();
		GridBagLayout gridbag = new GridBagLayout();
		GridBagConstraints c = new GridBagConstraints();
		main.setLayout(gridbag);
		main.setBorder(new EmptyBorder(4, 4, 4, 4));

		ActionListener findListener = new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				findAction();
			}
		};

		history = new LinkedList<String>();
		history.add("");

		if(settings.containsKey("FindDialog.History")) {
			StringTokenizer st = new StringTokenizer(settings.getProperty("FindDialog.History"), "~");

			while(st.hasMoreTokens()) {
				history.add(st.nextToken());
			}
		}

		txtPattern = new JComboBox(history.toArray());
		txtPattern.setEditable(true);
		txtPattern.addActionListener(findListener);
		txtPattern.setCursor(new Cursor(Cursor.TEXT_CURSOR));
		txtPattern.setPreferredSize(new Dimension(100, 25));

		JLabel l = new JLabel(Resources.get("finddialog.lbl.pattern.caption") + ": ");
		l.setDisplayedMnemonic(Resources.get("finddialog.lbl.pattern.mnemonic").charAt(0));
		l.setLabelFor(txtPattern);

		JPanel pnlPattern = new JPanel(new BorderLayout());
		pnlPattern.setBorder(new EmptyBorder(4, 4, 4, 4));
		pnlPattern.add(l, BorderLayout.WEST);
		pnlPattern.add(txtPattern, BorderLayout.CENTER);

		chkIDs = new JCheckBox(Resources.get("finddialog.chk.ids.caption"),
							   settings.getProperty("FindDialog.IDs", "true").equals("true"));
		chkIDs.setMnemonic(Resources.get("finddialog.chk.ids.mnemonic").charAt(0));

		chkNames = new JCheckBox(Resources.get("finddialog.chk.names.caption"),
								 settings.getProperty("FindDialog.Names", "true").equals("true"));
		chkNames.setMnemonic(Resources.get("finddialog.chk.names.mnemonic").charAt(0));

		chkDescs = new JCheckBox(Resources.get("finddialog.chk.descriptions.caption"),
								 settings.getProperty("FindDialog.Descriptions", "true").equals("true"));
		chkDescs.setMnemonic(Resources.get("finddialog.chk.descriptions.mnemonic").charAt(0));

		chkCmds = new JCheckBox(Resources.get("finddialog.chk.orders.caption"),
								settings.getProperty("FindDialog.Orders", "true").equals("true"));
		chkCmds.setMnemonic(Resources.get("finddialog.chk.orders.mnemonic").charAt(0));

		chkMessages = new JCheckBox(Resources.get("finddialog.chk.msgsandeffects.caption"),
									settings.getProperty("FindDialog.Msgs", "true").equals("true"));
		chkMessages.setMnemonic(Resources.get("finddialog.chk.msgsandeffects.mnemonic").charAt(0));

		chkItems = new JCheckBox(Resources.get("finddialog.chk.items.caption"),
								 settings.getProperty("FindDialog.Items", "true").equals("true"));
		chkItems.setMnemonic(Resources.get("finddialog.chk.items.mnemonic").charAt(0));

		JPanel pnlAttributeCheckBoxes = new JPanel(new GridBagLayout());

		c.anchor = GridBagConstraints.WEST;
		c.gridx = 0;
		c.gridy = 0;
		c.gridwidth = 1;
		c.gridheight = 1;
		c.fill = GridBagConstraints.NONE;
		c.weightx = 0.0;
		c.weighty = 0.0;
		pnlAttributeCheckBoxes.add(chkIDs, c);

		c.anchor = GridBagConstraints.WEST;
		c.gridx = 1;
		c.gridy = 0;
		c.gridwidth = 1;
		c.gridheight = 1;
		c.fill = GridBagConstraints.NONE;
		c.weightx = 0.0;
		c.weighty = 0.0;
		pnlAttributeCheckBoxes.add(chkNames, c);

		c.anchor = GridBagConstraints.WEST;
		c.gridx = 0;
		c.gridy = 1;
		c.gridwidth = 1;
		c.gridheight = 1;
		c.fill = GridBagConstraints.HORIZONTAL;
		c.weightx = 0.1;
		c.weighty = 0.0;
		pnlAttributeCheckBoxes.add(chkDescs, c);

		c.anchor = GridBagConstraints.WEST;
		c.gridx = 1;
		c.gridy = 1;
		c.gridwidth = 1;
		c.gridheight = 1;
		c.fill = GridBagConstraints.HORIZONTAL;
		c.weightx = 0.1;
		c.weighty = 0.0;
		pnlAttributeCheckBoxes.add(chkCmds, c);

		c.anchor = GridBagConstraints.WEST;
		c.gridx = 0;
		c.gridy = 2;
		c.gridwidth = 1;
		c.gridheight = 1;
		c.fill = GridBagConstraints.HORIZONTAL;
		c.weightx = 0.1;
		c.weighty = 0.0;
		pnlAttributeCheckBoxes.add(chkMessages, c);

		c.anchor = GridBagConstraints.WEST;
		c.gridx = 1;
		c.gridy = 2;
		c.gridwidth = 1;
		c.gridheight = 1;
		c.fill = GridBagConstraints.HORIZONTAL;
		c.weightx = 0.1;
		c.weighty = 0.0;
		pnlAttributeCheckBoxes.add(chkItems, c);

		List<Faction> factions = new LinkedList<Faction>(data.factions().values());
		Collections.sort(factions, new NameComparator(IDComparator.DEFAULT));
		factionCombo = new JComboBox(factions.toArray());
		factionCombo.addItem("");
		factionCombo.setSelectedIndex(factionCombo.getItemCount() - 1);

		JLabel factionLabel = new JLabel(Resources.get("finddialog.lbl.faction.caption") + ": ");
		factionLabel.setDisplayedMnemonic(Resources.get("finddialog.lbl.faction.mnemonic").charAt(0));
		factionLabel.setLabelFor(factionCombo);

		JPanel pnlFaction = new JPanel(new BorderLayout());
		pnlFaction.add(factionLabel, BorderLayout.WEST);
		pnlFaction.add(factionCombo, BorderLayout.CENTER);

		JPanel pnlAttributes = new JPanel(new BorderLayout());
		pnlAttributes.setBorder(new TitledBorder(BorderFactory.createEtchedBorder(),
												 Resources.get("finddialog.frm.attributes")));
		pnlAttributes.add(pnlAttributeCheckBoxes, BorderLayout.CENTER);
		pnlAttributes.add(pnlFaction, BorderLayout.SOUTH);

		JButton findButton = new JButton(Resources.get("finddialog.btn.find"));
		findButton.setDefaultCapable(true);
		findButton.addActionListener(findListener);
		this.getRootPane().setDefaultButton(findButton);

		JButton cancelButton = new JButton(Resources.get("finddialog.btn.close"));
		cancelButton.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					quit();
				}
			});

		JPanel pnlButtons = new JPanel(new GridLayout(2, 1, 3, 3));
		pnlButtons.add(findButton);
		pnlButtons.add(cancelButton);

		addUnits = new JCheckBox(Resources.get("finddialog.chk.units.caption"),
								 settings.getProperty("FindDialog.Units", "true").equals("true"));
		addUnits.setMnemonic(Resources.get("finddialog.chk.units.mnemonic").charAt(0));
		addRegions = new JCheckBox(Resources.get("finddialog.chk.regions.caption"),
								   settings.getProperty("FindDialog.Regions", "false").equals("true"));
		addRegions.setMnemonic(Resources.get("finddialog.chk.regions.mnemonic").charAt(0));
		addBuildings = new JCheckBox(Resources.get("finddialog.chk.buildings.caption"),
									 settings.getProperty("FindDialog.Buildings", "false").equals("true"));
		addBuildings.setMnemonic(Resources.get("finddialog.chk.buildings.mnemonic").charAt(0));
		addShips = new JCheckBox(Resources.get("finddialog.chk.ships.caption"),
								 settings.getProperty("FindDialog.Ships", "false").equals("true"));
		addShips.setMnemonic(Resources.get("finddialog.chk.ships.mnemonic").charAt(0));
		addTraitors = new JCheckBox(Resources.get("finddialog.chk.traitors.caption"),
									settings.getProperty("FindDialog.Traitors", "false").equals("true"));
		addTraitors.setMnemonic(Resources.get("finddialog.chk.traitors.mnemonic").charAt(0));
		addOnlyUnconfirmedUnits = new JCheckBox(Resources.get("finddialog.chk.addonlyunconfirmedunits.caption"),
												settings.getProperty("FindDialog.OnlyUnconfirmedUnits",
																	 "false").equals("true"));
		addOnlyUnconfirmedUnits.setMnemonic(Resources.get("finddialog.chk.addonlyunconfirmedunits.mnemonic")
												.charAt(0));
		addFactions = new JCheckBox(Resources.get("finddialog.chk.factions.caption"),
									settings.getProperty("FindDialog.Factions", "true").equals("true"));
		addFactions.setMnemonic(Resources.get("finddialog.chk.factions.mnemonic").charAt(0));

		JPanel pnlItems = new JPanel(new GridBagLayout());
		GridBagConstraints gbc = new GridBagConstraints(0, 0, 1, 1, 0, 0,
														GridBagConstraints.CENTER,
														GridBagConstraints.HORIZONTAL,
														new Insets(2, 2, 2, 2), 0, 0);
		pnlItems.setBorder(new TitledBorder(BorderFactory.createEtchedBorder(),
											Resources.get("finddialog.frm.objects")));

		pnlItems.add(addRegions, gbc);
		gbc.gridx++;
		pnlItems.add(addFactions, gbc);
		gbc.gridy++;
		gbc.gridx = 0;
		pnlItems.add(addBuildings, gbc);
		gbc.gridx++;
		pnlItems.add(addShips, gbc);
		gbc.gridx = 0;
		gbc.gridy++;
		pnlItems.add(addTraitors, gbc);
		gbc.gridx++;
		pnlItems.add(addUnits, gbc);
		gbc.gridx++;
		gbc.weightx = 1;
		pnlItems.add(addOnlyUnconfirmedUnits, gbc);

		resultList = new JList();
		resultList.addListSelectionListener(this);

		//resultList.setCellRenderer(new IconListCellRenderer());
		JScrollPane scroller = new JScrollPane(resultList, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
											   JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		scroller.setBorder(new LineBorder(Color.black));

		JButton bookmarkResults = new JButton(Resources.get("finddialog.btn.bookmark"));
		bookmarkResults.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					for(int i = 0; i < resultList.getModel().getSize(); i++) {
						Object o = resultList.getModel().getElementAt(i);

						if(o instanceof RegionWrapper) {
							o = ((RegionWrapper) o).getRegion();
						} else if(o instanceof UnitWrapper) {
							o = ((UnitWrapper) o).getUnit();
						}

						((Client) FindDialog.this.getOwner()).getBookmarkManager().addBookmark(o);
					}
				}
			});
		pnlResults = new JPanel(new BorderLayout());
		pnlResults.add(scroller, BorderLayout.CENTER);
		pnlResults.add(bookmarkResults, BorderLayout.SOUTH);
		pnlResults.setBorder(new TitledBorder(BorderFactory.createEtchedBorder(),
											  Resources.get("finddialog.frm.results")));

		c.anchor = GridBagConstraints.CENTER;
		c.gridx = 0;
		c.gridy = 0;
		c.gridwidth = 1;
		c.gridheight = 1;
		c.fill = GridBagConstraints.HORIZONTAL;
		c.weightx = 0.0;
		c.weighty = 0.0;
		main.add(pnlPattern, c);

		c.anchor = GridBagConstraints.NORTH;
		c.gridx = 1;
		c.gridy = 0;
		c.gridwidth = 1;
		c.gridheight = 2;
		c.fill = GridBagConstraints.NONE;
		c.insets = new Insets(2, 2, 2, 2);
		c.weightx = 0.0;
		c.weighty = 0.0;
		main.add(pnlButtons, c);

		c.anchor = GridBagConstraints.CENTER;
		c.gridx = 0;
		c.gridy = 1;
		c.gridwidth = 1;
		c.gridheight = 1;
		c.fill = GridBagConstraints.HORIZONTAL;
		c.insets = new Insets(0, 0, 0, 0);
		c.weightx = 0.0;
		c.weighty = 0.0;
		main.add(pnlAttributes, c);

		c.anchor = GridBagConstraints.CENTER;
		c.gridx = 0;
		c.gridy = 2;
		c.fill = GridBagConstraints.HORIZONTAL;
		c.insets = new Insets(0, 0, 0, 0);
		c.weightx = 0.0;
		c.weighty = 0.0;
		main.add(pnlItems, c);

		c.anchor = GridBagConstraints.CENTER;
		c.gridx = 0;
		c.gridy = 3;
		c.fill = GridBagConstraints.BOTH;
		c.insets = new Insets(0, 0, 0, 0);
		c.weightx = 0.5;
		c.weighty = 0.5;
		main.add(pnlResults, c);

		return main;
	}

	private Collection find() {
		Collection<Object> hits = new HashSet<Object>();
		Collection<Unique> items = new LinkedList<Unique>();
		String pattern = "";

		if(txtPattern.getSelectedItem() != null) {
			pattern = txtPattern.getSelectedItem().toString();

			if(!history.contains(pattern)) {
				if(history.contains("")) {
					history.remove("");
				}

				if(history.size() == 5) {
					history.remove(4);
				}

				history.add(0, pattern);

				List<String> save = new LinkedList<String>(history);
				txtPattern.removeAllItems();
				history = save;

				for(Iterator iter = history.iterator(); iter.hasNext();) {
					txtPattern.addItem(iter.next());
				}

				txtPattern.setSelectedIndex(0);
				storeHistory();
				txtPattern.repaint();
			}
		}

		Collection<String> patterns = new LinkedList<String>();
		StreamTokenizer st = new StreamTokenizer(new StringReader(pattern));
		st.ordinaryChars('0', '9');
		st.ordinaryChar('.');
		st.ordinaryChar('-');
		st.wordChars('0', '9');
		st.wordChars('.', '.');
		st.wordChars('-', '-');
		st.lowerCaseMode(true);

		do {
			try {
				st.nextToken();
			} catch(java.io.IOException e) {
				FindDialog.log.error(e);
			}

			if((st.ttype == StreamTokenizer.TT_WORD) || (st.ttype == '\'') || (st.ttype == '"')) {
				patterns.add(st.sval);

				//				System.out.println(st.sval);
			} else if(st.ttype != StreamTokenizer.TT_EOF) {
				FindDialog.log.warn("Found uncexpected TokenType (" + st.ttype +
						 ") in FindDialog.find() while parsing token: " + st.toString());
			}
		} while(st.ttype != StreamTokenizer.TT_EOF);

		// determine the items to search
		if(addUnits.isSelected() == true) {
			// items.addAll(data.units().values()); TempUnits were forgotten...
			for(Iterator iter = data.units().values().iterator(); iter.hasNext();) {
				Unit u = (Unit) iter.next();

				if((selectedRegions == null) || selectedRegions.isEmpty() ||
					   selectedRegions.contains(u.getRegion())) {
					if(addOnlyUnconfirmedUnits.isSelected()) {
						if(!u.isOrdersConfirmed()) {
							items.add(u);
						}

						for(Iterator iterator = u.tempUnits().iterator(); iterator.hasNext();) {
							Unit tu = (Unit) iterator.next();

							if(!tu.isOrdersConfirmed()) {
								items.add(tu);
							}
						}
					} else {
						items.add(u);
						items.addAll(u.tempUnits());
					}
				}
			}
		}

		if(addRegions.isSelected() == true) {
			if((selectedRegions == null) || selectedRegions.isEmpty()) {
				items.addAll(data.regions().values());
			} else {
				items.addAll(selectedRegions);
			}
		}

		if(addBuildings.isSelected() == true) {
			if((selectedRegions == null) || selectedRegions.isEmpty()) {
				items.addAll(data.buildings().values());
			} else {
				for(Iterator iter = data.buildings().values().iterator(); iter.hasNext();) {
					Building b = (Building) iter.next();

					if(selectedRegions.contains(b.getRegion())) {
						items.add(b);
					}
				}
			}
		}

		if(addShips.isSelected() == true) {
			if((selectedRegions == null) || selectedRegions.isEmpty()) {
				items.addAll(data.ships().values());
			} else {
				for(Iterator iter = data.ships().values().iterator(); iter.hasNext();) {
					Ship s = (Ship) iter.next();

					if(selectedRegions.contains(s.getRegion())) {
						items.add(s);
					}
				}
			}
		}

		if(addTraitors.isSelected() == true) {
			if(!addUnits.isSelected()) {
				Collection<Unit> traitors = getAllTraitors();

				if((selectedRegions == null) || selectedRegions.isEmpty()) {
					items.addAll(traitors);
				} else {
					for(Iterator iter = traitors.iterator(); iter.hasNext();) {
						Unit u = (Unit) iter.next();

						if(selectedRegions.contains(u.getRegion())) {
							items.add(u);
						}
					}
				}
			}
		}

		if(addFactions.isSelected()) {
			items.addAll(data.factions().values());
		}

		// determine the faction to limit the search to
		Faction faction = getFactionFromCombo();

		Iterator i = items.iterator();

		while(i.hasNext() == true) {
			Object item = i.next();

			if(chkItems.isSelected() && (filterItem(item, patterns) == true)) {
				hits.add(item);
			}

			if(chkIDs.isSelected() && (filterId(item, patterns) == true)) {
				hits.add(item);
			}

			if(chkNames.isSelected() && (filterName(item, patterns) == true)) {
				hits.add(item);
			}

			if(chkDescs.isSelected() && (filterDesc(item, patterns) == true)) {
				hits.add(item);
			}

			if(chkCmds.isSelected() && (filterCmd(item, patterns) == true)) {
				hits.add(item);
			}

			if(chkMessages.isSelected() && (filterMessage(item, patterns) == true)) {
				hits.add(item);
			}

			if((faction != null) && (filterFaction(item, faction) == false)) {
				hits.remove(item);
			}
		}

		return wrap(hits);
	}

	private boolean filterId(Object item, Collection patterns) {
		if(patterns.size() == 0) {
			return true;
		}

		boolean retVal = false;
		String id = getID(item);

		for(Iterator iter = patterns.iterator(); iter.hasNext();) {
			if(id.equalsIgnoreCase((String) iter.next())) {
				retVal = true;
				break;
			}
		}

    if (item instanceof Region && !retVal){
      Region r = (Region)item;
      String id1 = Integer.toString((int)r.getUID());
      String id2 = Integer.toString((int)r.getUID(),36);
      for(Iterator iter = patterns.iterator(); iter.hasNext();) {
        String check = (String)iter.next();
        if(id1.equalsIgnoreCase(check) || id2.equalsIgnoreCase(check)) {
          retVal = true;
          break;
        }
      }
    }
    
		return retVal;
	}

	private boolean filterItem(Object item, Collection patterns) {
		if(patterns.size() == 0) {
			return true;
		}

		boolean retVal = false;

		if(item instanceof Unit) {
			Unit u = (Unit) item;

			for(Iterator iterator = u.getItems().iterator(); iterator.hasNext();) {
				String name = getName(((Item) iterator.next()).getItemType());
				
				if(name != null) {
					name = name.toLowerCase();
					
					for(Iterator iter = patterns.iterator(); iter.hasNext();) {
						if(name.indexOf((String) iter.next()) > -1) {
							retVal = true;
							
							break;
						}
					}
				}
			}
		}

		return retVal;
	}

	private String getID(Object item) {
		ID id = ((Unique) item).getID();

		if(id != null) {
			return id.toString();
		} else {
			FindDialog.log.error("Found Unique without id: " + item);

			return "";
		}
	}

	private boolean filterName(Object item, Collection patterns) {
		boolean retVal = false;
		String name = getName(item);

		if(name != null) {
			name = name.toLowerCase();

			for(Iterator iter = patterns.iterator(); iter.hasNext();) {
				if(name.indexOf((String) iter.next()) > -1) {
					retVal = true;

					break;
				}
			}
		}

		return retVal;
	}

	private String getName(Object item) {
		String name = null;

		if(item instanceof Named) {
			name = ((Named) item).getName();
		}

		return name;
	}

	private boolean filterDesc(Object item, Collection patterns) {
		boolean retVal = false;
		String desc = getDesc(item);

		if(desc != null) {
			desc = desc.toLowerCase();

			for(Iterator iter = patterns.iterator(); iter.hasNext();) {
				if(desc.indexOf((String) iter.next()) > -1) {
					retVal = true;

					break;
				}
			}
		}

		return retVal;
	}

	private String getDesc(Object item) {
		String desc = null;

		if(item instanceof Described) {
			desc = ((Described) item).getDescription();
		}

		return desc;
	}

	private boolean filterCmd(Object item, Collection patterns) {
		boolean retVal = false;
		Collection cmds = getCmds(item);

		if(cmds != null) {
			for(Iterator cmdsIter = cmds.iterator(); cmdsIter.hasNext();) {
				String cmd = ((String) cmdsIter.next()).toLowerCase();

				for(Iterator ptrnIter = patterns.iterator(); ptrnIter.hasNext();) {
					if(cmd.indexOf((String) ptrnIter.next()) > -1) {
						retVal = true;

						break;
					}
				}
			}
		}

		return retVal;
	}

	private Collection<Unit> getAllTraitors() {
		Collection<Unit> retVal = new LinkedList<Unit>();

		for(Iterator<Unit> iter = data.units().values().iterator(); iter.hasNext();) {
			Unit unit = iter.next();

			if(unit.isSpy()) {
				retVal.add(unit);
			}
		}

		return retVal;
	}

	private Collection getCmds(Object item) {
		Collection retVal = null;

		if(item instanceof Unit) {
			retVal = ((Unit) item).getOrders();
		}

		return retVal;
	}

	/**
	 * Returns true iff <code>item</code>'s messages contain one of the patterns. 
	 *
	 * @param item
	 * @param patterns
	 * @return <code>true</code> if  <code>item</code>'s messages contain one of the patterns
	 */
	private boolean filterMessage(Object item, Collection patterns) {
		boolean retVal = false;
		Collection msgs = getMessages(item);

		if(msgs != null) {
			for(Iterator msgsIter = msgs.iterator(); msgsIter.hasNext();) {
				Object o = msgsIter.next();
				String msg = "";

				if(o instanceof String) {
					msg = (String) o;
				} else if(o instanceof Message) {
					msg = ((Message) o).getText();
				}

				msg = msg.toLowerCase();

				for(Iterator ptrnIter = patterns.iterator(); ptrnIter.hasNext();) {
					if(msg.indexOf((String) ptrnIter.next()) > -1) {
						retVal = true;

						break;
					}
				}
			}
		}

		return retVal;
	}

	private Collection<Object> getMessages(Object item) {
		Collection<Object> retVal = new LinkedList<Object>();

		if(item instanceof Unit) {
			Unit u = (Unit) item;

			if(u.getUnitMessages() != null) {
				retVal.addAll(u.getUnitMessages());
			}

			if(u.getEffects() != null) {
				retVal.addAll(u.getEffects());
			}
		} else if(item instanceof UnitContainer) {
			UnitContainer c = (UnitContainer) item;

			if(c.getEffects() != null) {
				retVal.addAll(c.getEffects());
			}

			if(c instanceof Region) {
				Region r = (Region) c;

				if(r.getMessages() != null) {
					retVal.addAll(r.getMessages());
				}

				if(r.getEvents() != null) {
					retVal.addAll(r.getEvents());
				}

				if(r.getPlayerMessages() != null) {
					retVal.addAll(r.getPlayerMessages());
				}

				if(r.getTravelThru() != null) {
					retVal.addAll(r.getTravelThru());
				}

				if(r.getTravelThruShips() != null) {
					retVal.addAll(r.getTravelThruShips());
				}
			} else if(c instanceof Faction) {
				Faction f = (Faction) c;

				if(f.getMessages() != null) {
					retVal.addAll(f.getMessages());
				}

				if(f.getErrors() != null) {
					retVal.addAll(f.getErrors());
				}
			}
		}

		return retVal;
	}

	private boolean filterFaction(Object item, Faction pattern) {
		boolean retVal = true;
		Faction faction = getFaction(item);

		if((faction == null) || (faction.equals(pattern) == false)) {
			retVal = false;
		}

		return retVal;
	}

	private Faction getFaction(Object item) {
		Faction faction = null;

		if(item instanceof Unit) {
			faction = ((Unit) item).getFaction();
		} else
		/*if (item instanceof Region) {
		    name = ((Region)item).getName();
		} else*/
		if(item instanceof Building || item instanceof Ship) {
			Unit owner = ((UnitContainer) item).getOwnerUnit();

			if(owner != null) {
				faction = owner.getFaction();
			}
		}

		return faction;
	}

	private Collection<Object> wrap(Collection<Object> items) {
		Collection<Object> wrappers = new LinkedList<Object>();
		Iterator i = items.iterator();

		while(i.hasNext() == true) {
			Object item = i.next();

			if(item instanceof Unit) {
				wrappers.add(new UnitWrapper((Unit) item));
			} else if(item instanceof Region) {
				wrappers.add(new RegionWrapper((Region) item));
			} else {
				wrappers.add(item);
			}

			/*if (item instanceof Building) {
			}
			if (item instanceof Ship) {
			}*/
		}

		return wrappers;
	}

	private Faction getFactionFromCombo() {
		Faction f = null;
		Object item = factionCombo.getSelectedItem();

		if((item != null) && item instanceof Faction) {
			f = (Faction) item;
		}

		return f;
	}

	/**
	 * A class wrapping a Region object, customizing the toString() needs for the tree.
	 */
	private class RegionWrapper {
		private Region region = null;

		/**
		 * Creates a new RegionWrapper object.
		 *
		 * 
		 */
		public RegionWrapper(Region r) {
			region = r;
		}

		/**
		 * DOCUMENT-ME
		 *
		 * 
		 */
		public Region getRegion() {
			return region;
		}

		/**
		 * DOCUMENT-ME
		 *
		 * 
		 */
		public String toString() {
			return region.toString();
		}
	}

	/**
	 * A class wrapping a Unit object, customizing the toString() needs for the tree.
	 */
	private class UnitWrapper {
		private Unit unit = null;

		/**
		 * Creates a new UnitWrapper object.
		 *
		 * 
		 */
		public UnitWrapper(Unit u) {
			unit = u;
		}

		/**
		 * DOCUMENT-ME
		 *
		 * 
		 */
		public Unit getUnit() {
			return unit;
		}

		/**
		 * DOCUMENT-ME
		 *
		 * 
		 */
		public String toString() {
			return unit.toString();
		}
	}

	/**
	 * Closes the dialog and stores settings of the dialog.
	 */
	protected void quit() {
		settings.setProperty("FindDialog.x", getX() + "");
		settings.setProperty("FindDialog.y", getY() + "");
		storeHistory();
		storeSettings();
		super.dispose();
	}

	protected void storeSettings() {
		storeCheckbox(chkIDs, "IDs");
		storeCheckbox(chkNames, "Names");
		storeCheckbox(chkDescs, "Descriptions");
		storeCheckbox(chkCmds, "Orders");
		storeCheckbox(chkMessages, "Messages");
		storeCheckbox(chkItems, "Items");

		storeCheckbox(addUnits, "Units");
		storeCheckbox(addRegions, "Regions");
		storeCheckbox(addBuildings, "Buildings");
		storeCheckbox(addShips, "Ships");
		storeCheckbox(addTraitors, "Traitors");
		storeCheckbox(addFactions, "Factions");
		storeCheckbox(addOnlyUnconfirmedUnits, "OnlyUnconfirmedUnits");
	}

	protected void storeCheckbox(JCheckBox box, String key) {
		settings.setProperty("FindDialog." + key, box.isSelected() ? "true" : "false");
	}

	protected void storeHistory() {
		if((history != null) && (history.size() > 0)) {
			StringBuffer buf = new StringBuffer();
			Iterator it = history.iterator();

			while(it.hasNext()) {
				buf.append(it.next());

				if(it.hasNext()) {
					buf.append('~');
				}
			}

			settings.setProperty("FindDialog.History", buf.toString());
		} else {
			settings.remove("FindDialog.History");
		}
	}

	protected void findAction() {
		Collection results = find();
		resultList.setListData(results.toArray());
		pnlResults.setBorder(new TitledBorder(BorderFactory.createEtchedBorder(),
											  Resources.get("finddialog.frm.results") + " (" + results.size() +
											  ")"));

		if(results.size() > 0) {
			resultList.requestFocus();
		}
	}
}