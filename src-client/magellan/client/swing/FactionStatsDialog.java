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
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.GridLayout;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Properties;

import javax.swing.JButton;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.ListSelectionModel;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import magellan.client.event.EventDispatcher;
import magellan.client.event.SelectionEvent;
import magellan.client.event.SelectionListener;
import magellan.client.skillchart.SkillChartPanel;
import magellan.library.EntityID;
import magellan.library.Faction;
import magellan.library.GameData;
import magellan.library.ID;
import magellan.library.Named;
import magellan.library.Unique;
import magellan.library.event.GameDataEvent;
import magellan.library.utils.Resources;
import magellan.library.utils.TrustLevels;
import magellan.library.utils.comparator.FactionTrustComparator;
import magellan.library.utils.comparator.IDComparator;
import magellan.library.utils.comparator.NameComparator;
import magellan.library.utils.logging.Logger;


/**
 * A dialog wrapper for the faction statistics display.
 */
public class FactionStatsDialog extends InternationalizedDataDialog {
	private static final Logger log = Logger.getInstance(FactionStatsDialog.class);
	private List<Faction> factions = null;
	private FactionStatsPanel pnlStats = null;
	private JList lstFaction = null;
	private JSplitPane splFaction = null;
	private JTabbedPane tabPane = null;

	//private EresseaOptionPanel optionPanel = null;
	private static FactionTrustComparator<Named> factionTrustComparator = FactionTrustComparator.DEFAULT_COMPARATOR;
	private static NameComparator<Unique> nameComparator = new NameComparator<Unique>(IDComparator.DEFAULT);

	/**
	 * Create a new FactionStatsDialog object as a dialog with a parent window.
	 *
	 * 
	 * 
	 * 
	 * 
	 * 
	 */
	public FactionStatsDialog(Frame owner, boolean modal, EventDispatcher ed, GameData initData,
							  Properties p) {
		super(owner, modal, ed, initData, p);
		pnlStats = new FactionStatsPanel(dispatcher, data, p);
		init();
	}

	/**
	 * Create a new FactionStatsDialog object as a dialog with a parent window and with the given
	 * faction selected.
	 *
	 * 
	 * 
	 * 
	 * 
	 * 
	 * 
	 */
	public FactionStatsDialog(Frame owner, boolean modal, EventDispatcher ed, GameData initData,
							  Properties p, Faction f) {
		this(owner, modal, ed, initData, p);
		lstFaction.setSelectedValue(f, true);
	}

	private void init() {
		setContentPane(getMainPane());
		setTitle(Resources.get("factionstatsdialog.window.title"));

		int width = Integer.parseInt(settings.getProperty("FactionStatsDialog.width", "500"));
		int height = Integer.parseInt(settings.getProperty("FactionStatsDialog.height", "300"));
		setSize(width, height);

		Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
		int x = Integer.parseInt(settings.getProperty("FactionStatsDialog.x",
													  ((screen.width - getWidth()) / 2) + ""));
		int y = Integer.parseInt(settings.getProperty("FactionStatsDialog.y",
													  ((screen.height - getHeight()) / 2) + ""));
		setLocation(x, y);
		splFaction.setDividerLocation(Integer.parseInt(settings.getProperty("FactionStatsDialog.split",
																			(width / 2) + "")));

		ID selFacID = EntityID.createEntityID(settings.getProperty("FactionStatsDialog.selFacID",
																   "-1"), 10);
		Faction selFac = data.getFaction(selFacID);

		if(selFac != null) {
			lstFaction.setSelectedValue(selFac, true);
		} else {
			lstFaction.setSelectedIndex(0);
		}
	}

	private Container getMainPane() {
		JPanel mainPanel = new JPanel(new BorderLayout(0, 5));
		mainPanel.setBorder(new EmptyBorder(4, 4, 4, 4));

		//optionPanel = new EresseaOptionPanel();
		tabPane = new JTabbedPane();
		tabPane.addTab(Resources.get("factionstatsdialog.tab.stats.caption"), null, pnlStats, null);

		JPanel skillChartPanel = getSkillChartPanel();

		if(skillChartPanel != null) {
			tabPane.addTab(Resources.get("factionstatsdialog.tab.skillchart.caption"), skillChartPanel);
		}

		// pavkovic 2003.11.19: deactivated, because EresseaOptionPanel is currently broken
		// tabPane.addTab(Resources.get("factionstatsdialog.tab.options.caption"), null, optionPanel, null);
		splFaction = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, getFactionPanel(), tabPane);
		mainPanel.add(splFaction, BorderLayout.CENTER);
		mainPanel.add(getButtonPanel(), BorderLayout.SOUTH);

		return mainPanel;
	}

	private Container getButtonPanel() {
		JButton btnClose = new JButton(Resources.get("factionstatsdialog.btn.close.caption"));
		btnClose.setMnemonic(Resources.get("factionstatsdialog.btn.close.menmonic").charAt(0));
		btnClose.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					quit();
				}
			});

		JPanel buttonPanel = new JPanel();
		buttonPanel.add(btnClose);

		return buttonPanel;
	}

	private Container getFactionPanel() {
		factions = new LinkedList<Faction>(data.factions().values());

		String sortByTrustLevel = settings.getProperty("FactionStatsDialog.SortByTrustLevel",
				"true");

		// sort factions
		if(sortByTrustLevel.equals("true")) {
			Collections.sort(factions, factionTrustComparator);
		} else if (sortByTrustLevel.equals("detailed")){
			Collections.sort(factions, FactionTrustComparator.DETAILED_COMPARATOR);
		} else {
			Collections.sort(factions, nameComparator);
		}

		final FactionStatsDialog d = this;
		lstFaction = new JList(factions.toArray());
		
		// (stm): The L&F should do this automatically, see j2sdk-1.4.2-doc/api/javax/swing/doc-files/Key-Index.html#JList
//		// to jump to first faction which name starts with the typed key
//		lstFaction.addKeyListener(new KeyAdapter() {
//				public void keyPressed(KeyEvent e) {
//					Faction dummy = new Faction(IntegerID.create(-2), null);
//					char c = e.getKeyChar();
//
//					if(!Character.isLetter(c)) {
//						return;
//					}
//
//					dummy.setName(String.valueOf(c));
//
//					int index = Collections.binarySearch(factions, dummy, nameComparator);
//
//					if(index < 0) {
//						index = -index - 1;
//					}
//
//					if(index == lstFaction.getModel().getSize()) {
//						index--;
//					}
//
//					Object o = lstFaction.getModel().getElementAt(index);
//					lstFaction.setSelectedValue(o, true);
//				}
//			});
		lstFaction.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
		lstFaction.addListSelectionListener(new ListSelectionListener() {
				public void valueChanged(ListSelectionEvent e) {
					if(e.getValueIsAdjusting()) {
						return;
					}
					lstFaction.ensureIndexIsVisible(lstFaction.getLeadSelectionIndex());
					
					SelectionEvent se = null;
					JList list = (JList) e.getSource();

					if((list.getModel().getSize() > 0) && !list.isSelectionEmpty()) {
						se = new SelectionEvent(d, Arrays.asList(list.getSelectedValues()),
												list.getSelectedValue());
					} else {
						se = new SelectionEvent(d, new LinkedList(), null);
					}

					// notify all components in the tabbed pane

					/**
					 * Ulrich Küster: (!) Special care has to be taken for the FactionStatsDialog.
					 * It can not be differed, if SelectionEvents come from the faction list in
					 * FactionStatsDialog or from other components of Magellan. To keep the
					 * faction list in this object consistent to the displayed data in the
					 * FactionStatsPanel object, FactionStatsPanel.setFaction() should be _never_
					 * called by FactionStatsPanel.selectionChanged(), but always directly by this
					 * method.
					 */
					for(int i = 0; i < tabPane.getTabCount(); i++) {
						Component c = tabPane.getComponentAt(i);

						if(c instanceof FactionStatsPanel) {
							((FactionStatsPanel) c).setFactions(se.getSelectedObjects());
						} else if(c instanceof SelectionListener) {
							((SelectionListener) c).selectionChanged(se);
						}
					}
				}
			});

		String s;

		if(sortByTrustLevel.equalsIgnoreCase("true")) {
			s = Resources.get("factionstatsdialog.btn.sort.detailed.caption");
		} else if (sortByTrustLevel.equalsIgnoreCase("detailed")){
			s = Resources.get("factionstatsdialog.btn.sort.name.caption");
		}else{
			s = Resources.get("factionstatsdialog.btn.sort.trustlevel.caption");
		}

		final JButton sort = new JButton(s);
		sort.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					String sortByTrust = settings.getProperty("FactionStatsDialog.SortByTrustLevel", "true");
					if(sortByTrust.equalsIgnoreCase("true")) {
						sortByTrust="detailed";
						sort.setText(Resources.get("factionstatsdialog.btn.sort.name.caption"));
						Collections.sort(factions, FactionTrustComparator.DETAILED_COMPARATOR);
					} else if (sortByTrust.equalsIgnoreCase("detailed")){
						sortByTrust="false";
						sort.setText(Resources.get("factionstatsdialog.btn.sort.trustlevel.caption"));
						Collections.sort(factions, nameComparator);
					}else{
						sortByTrust="true";
						sort.setText(Resources.get("factionstatsdialog.btn.sort.detailed.caption"));
						Collections.sort(factions, factionTrustComparator);
					}
					settings.setProperty("FactionStatsDialog.SortByTrustLevel",
										 String.valueOf(sortByTrust));

					Object o = lstFaction.getSelectedValue();
					lstFaction.setListData(factions.toArray());
					lstFaction.setSelectedValue(o, true);
					lstFaction.repaint();
				}
			});

		JButton btnDeleteFaction = new JButton(Resources.get("factionstatsdialog.btn.deletefaction.caption"));
		btnDeleteFaction.setMnemonic(Resources.get("factionstatsdialog.btn.deletefaction.mnemonic").charAt(0));
		btnDeleteFaction.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
          Object[] values = lstFaction.getSelectedValues();
					List<Faction> victims = new LinkedList<Faction>();
          for (Object value : values) victims.add((Faction)value);

					for(ListIterator<Faction> iter = (ListIterator<Faction>) victims.iterator(); iter.hasNext();) {
						Faction f = iter.next();
						boolean veto = false;

						if(f.units().size() > 0) {
							Object msgArgs[] = { f };
							JOptionPane.showMessageDialog(d,
														  (new java.text.MessageFormat(Resources.get("factionstatsdialog.msg.factioncontainsunits.text"))).format(msgArgs));
							veto = true;
						}

						if(!veto) {
							for(Iterator allFactions = data.factions().values().iterator();
									allFactions.hasNext();) {
								Faction dummy = (Faction) allFactions.next();

								if((dummy.units().size() > 0) && (dummy.getAllies() != null) &&
									   dummy.getAllies().containsKey(f.getID())) {
									Object msgArgs[] = { f, dummy.getAllies().get(f.getID()) };
									JOptionPane.showMessageDialog(d,
																  (new java.text.MessageFormat(Resources.get("factionstatsdialog.msg.factionisallied.text"))).format(msgArgs));
									veto = true;

									break;
								}
							}
						}

						if(!veto) {
							Object msgArgs[] = { f, };

							if(JOptionPane.showConfirmDialog(d,
																 (new java.text.MessageFormat(Resources.get("factionstatsdialog.msg.confirmdeletefaction.text"))).format(msgArgs),
																 Resources.get("factionstatsdialog.msg.confirmdeletefaction.title"),
																 JOptionPane.YES_NO_OPTION) != JOptionPane.OK_OPTION) {
								veto = true;
							}
						}

						if(veto) {
							iter.remove();
						}
					}

					if(victims.size() > 0) {
						for(Iterator iter = victims.iterator(); iter.hasNext();) {
							Faction f = (Faction) iter.next();
							data.factions().remove(f.getID());
						}

						// should notify game data listeners here
						factions.removeAll(victims);
						lstFaction.setListData(factions.toArray());
						lstFaction.repaint();
						pnlStats.setFactions(new LinkedList<Faction>());
					}
					// TODO: delete properties belonging to this faction (like password) also?
				}
			});

		JButton btnPassword = new JButton(Resources.get("factionstatsdialog.btn.setpwd.caption"));
		btnPassword.setMnemonic(Resources.get("factionstatsdialog.btn.setpwd.menmonic").charAt(0));
		btnPassword.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					if((lstFaction.getModel().getSize() <= 0) || lstFaction.isSelectionEmpty()) {
						return;
					}

					Faction f = (Faction) lstFaction.getSelectedValue();

					String pwd = "";

					if(f.getPassword() != null) {
						pwd = f.getPassword();
					}

					// ask user for password
					Object msgArgs[] = { f };
					pwd = (String) JOptionPane.showInputDialog(getRootPane(),
															   (new java.text.MessageFormat(Resources.get("factionstatsdialog.msg.passwdinput.text"))).format(msgArgs),
															   Resources.get("factionstatsdialog.msg.passwdinput.title"),
															   JOptionPane.QUESTION_MESSAGE, null,
															   null, pwd);

					if(pwd != null) { // if user did not hit the cancel button

						if(!pwd.equals("")) { // if this password is valid
							f.setPassword(pwd);
						} else {
							f.setPassword(null);
						}

						// store the password to the settings even if it is invalid
						settings.setProperty("Faction.password." +
											 ((EntityID) f.getID()).intValue(),
											 (f.getPassword() != null) ? f.getPassword() : "");

						// if the pw is valid increase this faction's trust level
						if(f.getPassword() != null) {
							f.setTrustLevel(Faction.TL_PRIVILEGED);
						} else {
							// default is okay here, combat ally trust levels are restored
							// in the next loop anyway
							f.setTrustLevel(Faction.TL_DEFAULT);
						}

						TrustLevels.recalculateTrustLevels(data);
					}

					// notify game data listeners
					dispatcher.fire(new GameDataEvent(this, data));
				}
			});

		JButton btnTrustlevel = new JButton(Resources.get("factionstatsdialog.btn.trustlevel.caption"));
		btnTrustlevel.setMnemonic(Resources.get("factionstatsdialog.btn.trustlevel.mnemonic").charAt(0));
		btnTrustlevel.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					if((lstFaction.getModel().getSize() <= 0) || lstFaction.isSelectionEmpty()) {
						return;
					}

					Object selectedFactions[] = lstFaction.getSelectedValues();
					boolean validInput = false;

					while(!validInput) {
						// ask for Trustlevel
						validInput = true;

						String oldTrustLevel;

						if(selectedFactions.length == 1) {
							Faction faction = (Faction) selectedFactions[0];

							if(faction.isTrustLevelSetByUser()) {
								oldTrustLevel = String.valueOf(faction.getTrustLevel());
							} else {
								oldTrustLevel = ""; // indicates default
							}
						} else {
							oldTrustLevel = ""; // more than one faction selected
						}

						String stringValue = (String) JOptionPane.showInputDialog(FactionStatsDialog.this,
																				  Resources.get("factionstatsdialog.msg.trustlevelinput.text"),
																				  Resources.get("factionstatsdialog.msg.trustlevelinput.title"),
																				  JOptionPane.OK_CANCEL_OPTION,
																				  null, null,
																				  oldTrustLevel);

						if(stringValue != null) {
							for(int index = 0; index < selectedFactions.length; index++) {
								Faction faction = (Faction) selectedFactions[index];
								
								// this indicates, that further on Magellan shall
								// calculate the trustlevel for this faction on its own
								if(stringValue.length() == 0) {
									faction.setTrustLevelSetByUser(false);
									faction.setTrustLevel(Faction.TL_DEFAULT);
								} else {
									try {
										int intValue = Integer.parseInt(stringValue);
										faction.setTrustLevel(intValue);
										faction.setTrustLevelSetByUser(true);
									} catch(NumberFormatException exc) {
										// ask again for input
										validInput = false;
									}
								}
							}

							if(validInput) {
								TrustLevels.recalculateTrustLevels(data);

								// GameData did probably change
								dispatcher.fire(new GameDataEvent(this, data));
								Collections.sort(factions, factionTrustComparator);
							} else {
								JOptionPane.showMessageDialog(FactionStatsDialog.this,
															  Resources.get("factionstatsdialog.msg.trustlevelinputinvalid"));
							}
						}
					}
				}
			});

		JPanel pnlButtons = new JPanel(new GridLayout(4, 1, 0, 3));
		pnlButtons.add(sort);
		pnlButtons.add(btnPassword);
		pnlButtons.add(btnTrustlevel);
		pnlButtons.add(btnDeleteFaction);

		JPanel pnlFactions = new JPanel(new BorderLayout(0, 5));
		pnlFactions.add(new JScrollPane(lstFaction), BorderLayout.CENTER);
		pnlFactions.add(pnlButtons, BorderLayout.SOUTH);

		return pnlFactions;
	}

	/**
	 * Returns the skillchart statistics panel. The old method is no longer of use, since the
	 * sourcecode has become an integral part of the magellan code base. Thus it has no longer to
	 * be instatiated via reflections.
	 *
	 * 
	 */
	private JPanel getSkillChartPanel() {
		/*    // try to load the skillchart classes
		    ResourcePathClassLoader loader = new ResourcePathClassLoader(settings);
		    Class SkillChartPanel = null;

		    try {
		        SkillChartPanel = loader.loadClass("magellan.client.skillchart.SkillChartPanel");
		    } catch(java.lang.ClassNotFoundException cnf) {
		        return null;
		    }

		    // get it's constructor
		    java.lang.reflect.Constructor constructor = null;

		    try {
		        constructor = SkillChartPanel.getConstructor(new Class[] {
		                                                         Class.forName("magellan.client.event.EventDispatcher"),
		                                                         Class.forName("magellan.library.GameData"),
		                                                         Class.forName("java.util.Properties")
		                                                     });
		    } catch(java.lang.NoSuchMethodException e) {
		        log.error(e);

		        return null;
		    } catch(java.lang.ClassNotFoundException e) {
		        log.error(e);

		        return null;
		    } catch(java.lang.NoClassDefFoundError e) {
		        log.error(e);

		        return null;
		    }

		    // create an instance of this class
		    Object skillChartPanel = null;

		    try {
		        skillChartPanel = constructor.newInstance(new Object[] { dispatcher, data, settings });
		    } catch(java.lang.reflect.InvocationTargetException e) {
		        log.error(e);

		        return null;
		    } catch(java.lang.IllegalAccessException e) {
		        log.error(e);

		        return null;
		    } catch(java.lang.InstantiationException e) {
		        log.error(e);

		        return null;
		    }

		    // return casted Panel
		    return (JPanel) skillChartPanel;
		    */
		try {
			JPanel skillChartPanel = new SkillChartPanel(dispatcher, data,settings);

			return skillChartPanel;
		} catch(Throwable t) {
			log.warn(t+": "+t.getLocalizedMessage());
			t.printStackTrace();
			log.warn("FactionStatsDialog.getSkillChartPanel(): Couldn't create skillChartPanel! Delivering null.");
		}

		return null;
	}

	private void storeSettings() {
		settings.setProperty("FactionStatsDialog.x", getX() + "");
		settings.setProperty("FactionStatsDialog.y", getY() + "");
		settings.setProperty("FactionStatsDialog.width", getWidth() + "");
		settings.setProperty("FactionStatsDialog.height", getHeight() + "");
		settings.setProperty("FactionStatsDialog.split", splFaction.getDividerLocation() + "");

		if((lstFaction.getModel().getSize() > 0) && (lstFaction.getSelectedValue() != null)) {
			settings.setProperty("FactionStatsDialog.selFacID",
								 ((EntityID) ((Faction) lstFaction.getSelectedValue()).getID()).intValue() +
								 "");
		}
	}

	protected void quit() {
		storeSettings();
		super.quit();
	}

	/**
	 * DOCUMENT-ME
	 *
	 * 
	 */
	public void gameDataChanged(GameDataEvent e) {
		data = e.getGameData();
		factions = new LinkedList<Faction>(data.factions().values());

		// sort factions
		Collections.sort(factions, factionTrustComparator);
		lstFaction.setListData(factions.toArray());

		ID selFacID = EntityID.createEntityID(settings.getProperty("FactionStatsDialog.selFacID",
																   "-1"), 10);
		Faction selFac = data.getFaction(selFacID);

		if(selFac != null) {
			lstFaction.setSelectedValue(selFac, true);
		} else {
			lstFaction.setSelectedIndex(0);
		}

		lstFaction.repaint();
	}

	//public void showEresseaOptions() {
	//	tabPane.setSelectedComponent(optionPanel);
	//}

}
