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

import java.awt.Container;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;

import magellan.client.Client;
import magellan.library.Building;
import magellan.library.Faction;
import magellan.library.GameData;
import magellan.library.Item;
import magellan.library.Message;
import magellan.library.Potion;
import magellan.library.Region;
import magellan.library.RegionResource;
import magellan.library.Ship;
import magellan.library.Skill;
import magellan.library.Spell;
import magellan.library.Unit;
import magellan.library.io.cr.CRWriter;
import magellan.library.io.file.FileTypeFactory;
import magellan.library.utils.PropertiesHelper;
import magellan.library.utils.Resources;
import magellan.library.utils.logging.Logger;


/**
 * A GUI for writing a CR to a file or copy it to the clipboard. This class can be used as a
 * stand-alone application or can be integrated as dialog into a different application.
 */
public class CRWriterDialog extends InternationalizedDataDialog {
	private static final Logger log = Logger.getInstance(CRWriterDialog.class);
	private boolean standAlone = false;
	private Collection<Region> regions = null;
	private JComboBox comboOutputFile = null;
	private JCheckBox chkServerConformance = null;
	private JCheckBox chkIslands = null;
	private JCheckBox chkRegions = null;
	private JCheckBox chkRegionDetails = null;
	private JCheckBox chkBuildings = null;
	private JCheckBox chkShips = null;
	private JCheckBox chkUnits = null;
	private JCheckBox chkMessages = null;
	private JCheckBox chkSpellsAndPotions = null;
	private JCheckBox chkSelRegionsOnly = null;
	private JCheckBox chkDelStats = null;
	private JCheckBox chkDelTrans = null;

	/**
	 * Create a stand-alone instance of CRWriterDialog.
	 *
	 * 
	 */
	public CRWriterDialog(GameData data) {
		super(null, false, null, data, new Properties());
		standAlone = true;

		try {
			settings.load(new FileInputStream(new File(System.getProperty("user.home"),
													   "CRWriterDialog.ini")));
		} catch(IOException e) {
			log.error("CRWriterDialog.CRWriterDialog()", e);
		}

		init();
	}

	/**
	 * Create a new CRWriterDialog object as a dialog with a parent window.
	 *
	 * 
	 * 
	 * 
	 * 
	 */
	public CRWriterDialog(Frame owner, boolean modal, GameData initData, Properties p) {
		super(owner, modal, null, initData, p);
		init();
	}

	/**
	 * Create a new CRWriterDialog object as a dialog with a parent window and a set of selected
	 * regions.
	 */
	public CRWriterDialog(Frame owner, boolean modal, GameData initData, Properties p, Collection<Region> selectedRegions) {
		super(owner, modal, null, initData, p);
		this.regions = selectedRegions;
		init();
	}

	private void init() {
		setContentPane(getMainPane());
		setTitle(Resources.get("crwriterdialog.window.title"));
		setSize(450, 250);

		Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
		int x = Integer.parseInt(settings.getProperty("CRWriterDialog.x",
													  ((screen.width - getWidth()) / 2) + ""));
		int y = Integer.parseInt(settings.getProperty("CRWriterDialog.y",
													  ((screen.height - getHeight()) / 2) + ""));
		setLocation(x, y);
	}

	private Container getMainPane() {
		JPanel mainPanel = new JPanel(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();
		mainPanel.setBorder(new EmptyBorder(4, 4, 4, 4));

		c.anchor = GridBagConstraints.NORTH;
		c.gridx = 0;
		c.gridy = 0;
		c.gridwidth = 1;
		c.gridheight = 1;
		c.fill = GridBagConstraints.HORIZONTAL;
		c.weightx = 0.1;
		c.weighty = 0.0;
		mainPanel.add(getOptionPanel(), c);

		c.anchor = GridBagConstraints.NORTH;
		c.gridx = 1;
		c.gridy = 0;
		c.gridwidth = 1;
		c.gridheight = 1;
		c.fill = GridBagConstraints.NONE;
		c.weightx = 0.0;
		c.weighty = 0.0;
		mainPanel.add(getButtonPanel(), c);

		c.anchor = GridBagConstraints.CENTER;
		c.gridx = 0;
		c.gridy = 1;
		c.gridwidth = 2;
		c.gridheight = 1;
		c.fill = GridBagConstraints.HORIZONTAL;
		c.weightx = 0.1;
		c.weighty = 0.0;
		mainPanel.add(getFilePanel(), c);

		return mainPanel;
	}

	private Container getButtonPanel() {
		JButton saveButton = new JButton(Resources.get("crwriterdialog.btn.save.caption"));
		saveButton.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					File outputFile = new File((String) comboOutputFile.getSelectedItem());

					try {
						write(FileTypeFactory.singleton().createFileType(outputFile, false).createWriter(data.getEncoding()));
						quit();
					} catch(IOException ioe) {
						log.error(ioe);

						String msgArgs[] = { outputFile.getPath() };
						JOptionPane.showMessageDialog((JButton) e.getSource(),
													  (new java.text.MessageFormat(Resources.get("crwriterdialog.msg.writeerror.text"))).format(msgArgs),
													  Resources.get("crwriterdialog.msg.exporterror.title"),
													  JOptionPane.WARNING_MESSAGE);
					}
				}
			});

		JButton clipboardButton = new JButton(Resources.get("crwriterdialog.btn.clipboard.caption"));
		clipboardButton.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					StringWriter sw = new StringWriter();
					write(sw);
					getToolkit().getSystemClipboard().setContents(new java.awt.datatransfer.StringSelection(sw.toString()),
																  null);
					quit();
				}
			});

		JButton cancelButton = new JButton(Resources.get("crwriterdialog.btn.close.caption"));
		cancelButton.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					quit();
				}
			});

		JPanel buttonPanel = new JPanel(new GridLayout(3, 1, 0, 4));
		buttonPanel.add(saveButton);
		buttonPanel.add(clipboardButton);
		buttonPanel.add(cancelButton);

		return buttonPanel;
	}

	private Container getFilePanel() {
		comboOutputFile = new JComboBox(PropertiesHelper.getList(settings,
																 "CRWriterDialog.outputFile")
														.toArray());
		comboOutputFile.setEditable(true);

		JLabel lblOutputFile = new JLabel(Resources.get("crwriterdialog.lbl.targetfile"));
		lblOutputFile.setLabelFor(comboOutputFile);

		JButton btnOutputFile = new JButton("...");
		btnOutputFile.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					String outputFile = getFileName((String) comboOutputFile.getSelectedItem());

					if(outputFile != null) {
						// comboOutputFile.addItem(outputFile);
						// bug Fiete 20061217
						comboOutputFile.insertItemAt(outputFile,0);
						comboOutputFile.setSelectedItem(outputFile);
					}
				}
			});

		JPanel pnlFiles = new JPanel(new GridBagLayout());
		pnlFiles.setBorder(new TitledBorder(BorderFactory.createEtchedBorder(),
											Resources.get("crwriterdialog.border.files")));

		GridBagConstraints c = new GridBagConstraints();

		// outputFile
		c.anchor = GridBagConstraints.WEST;
		c.gridx = 0;
		c.gridy = 1;
		c.gridwidth = 1;
		c.gridheight = 1;
		c.fill = GridBagConstraints.NONE;
		c.weightx = 0.0;
		c.weighty = 0.0;
		pnlFiles.add(lblOutputFile, c);
		c.anchor = GridBagConstraints.CENTER;
		c.gridx = 1;
		c.gridy = 1;
		c.gridwidth = 1;
		c.gridheight = 1;
		c.fill = GridBagConstraints.HORIZONTAL;
		c.weightx = 0.1;
		c.weighty = 0.0;
		pnlFiles.add(comboOutputFile, c);
		c.anchor = GridBagConstraints.CENTER;
		c.gridx = 2;
		c.gridy = 1;
		c.gridwidth = 1;
		c.gridheight = 1;
		c.fill = GridBagConstraints.NONE;
		c.weightx = 0.0;
		c.weighty = 0.0;
		pnlFiles.add(btnOutputFile, c);

		return pnlFiles;
	}

	private Container getOptionPanel() {
		// TODO: add tooltips
		chkServerConformance = new JCheckBox(Resources.get("crwriterdialog.chk.servercompatibility.caption"),
											 (Boolean.valueOf(settings.getProperty("CRWriterDialog.serverConformance",
																			   "true"))).booleanValue());
		chkIslands = new JCheckBox(Resources.get("crwriterdialog.chk.islands.caption"),
								   (Boolean.valueOf(settings.getProperty("CRWriterDialog.includeIslands",
																	 "true"))).booleanValue());
		chkRegions = new JCheckBox(Resources.get("crwriterdialog.chk.regions.caption"),
								   (Boolean.valueOf(settings.getProperty("CRWriterDialog.includeRegions",
																	 "true"))).booleanValue());
		chkRegionDetails = new JCheckBox(Resources.get("crwriterdialog.chk.regiondetails.caption"),
										 (Boolean.valueOf(settings.getProperty("CRWriterDialog.includeRegionDetails",
																		   "true"))).booleanValue());
		chkBuildings = new JCheckBox(Resources.get("crwriterdialog.chk.buildings.caption"),
									 (Boolean.valueOf(settings.getProperty("CRWriterDialog.includeBuildings",
																	   "true"))).booleanValue());
		chkShips = new JCheckBox(Resources.get("crwriterdialog.chk.ships.caption"),
								 (Boolean.valueOf(settings.getProperty("CRWriterDialog.includeShips",
																   "true"))).booleanValue());
		chkUnits = new JCheckBox(Resources.get("crwriterdialog.chk.units.caption"),
								 (Boolean.valueOf(settings.getProperty("CRWriterDialog.includeUnits",
																   "true"))).booleanValue());
		chkMessages = new JCheckBox(Resources.get("crwriterdialog.chk.messages.caption"),
									(Boolean.valueOf(settings.getProperty("CRWriterDialog.includeMessages",
																	  "true"))).booleanValue());
		chkSpellsAndPotions = new JCheckBox(Resources.get("crwriterdialog.chk.spellsandpotions.caption"),
											(Boolean.valueOf(settings.getProperty("CRWriterDialog.includeSpellsAndPotions",
																			  "true"))).booleanValue());
		chkSelRegionsOnly = new JCheckBox(Resources.get("crwriterdialog.chk.selectedregions.caption"),
										  (Boolean.valueOf(settings.getProperty("CRWriterDialog.includeSelRegionsOnly",
																			"false"))).booleanValue());
		chkSelRegionsOnly.setEnabled((regions != null) && (regions.size() > 0));
		chkDelStats = new JCheckBox(Resources.get("crwriterdialog.chk.delstats.caption"),
									(Boolean.valueOf(settings.getProperty("CRWriterDialog.delStats",
																	  "false"))).booleanValue());
		chkDelTrans = new JCheckBox(Resources.get("crwriterdialog.chk.deltrans.caption"),
									(Boolean.valueOf(settings.getProperty("CRWriterDialog.delTrans",
																	  "false"))).booleanValue());

		JPanel pnlOptions = new JPanel(new GridLayout(6, 2));
		pnlOptions.setBorder(new TitledBorder(BorderFactory.createEtchedBorder(),
											  Resources.get("crwriterdialog.border.options")));
		pnlOptions.add(chkServerConformance);
		pnlOptions.add(chkIslands);
		pnlOptions.add(chkRegions);
		pnlOptions.add(chkRegionDetails);
		pnlOptions.add(chkBuildings);
		pnlOptions.add(chkShips);
		pnlOptions.add(chkUnits);
		pnlOptions.add(chkMessages);
		pnlOptions.add(chkSpellsAndPotions);
		pnlOptions.add(chkSelRegionsOnly);
		pnlOptions.add(chkDelStats);
		pnlOptions.add(chkDelTrans);

		return pnlOptions;
	}

	private void storeSettings() {
		settings.setProperty("CRWriterDialog.x", getX() + "");
		settings.setProperty("CRWriterDialog.y", getY() + "");

		PropertiesHelper.setList(settings, "CRWriterDialog.outputFile",
								 getNewOutputFiles(comboOutputFile));
		settings.setProperty("CRWriterDialog.serverConformance",
							 String.valueOf(chkServerConformance.isSelected()));
		settings.setProperty("CRWriterDialog.includeIslands",
							 String.valueOf(chkIslands.isSelected()));
		settings.setProperty("CRWriterDialog.includeRegions",
							 String.valueOf(chkRegions.isSelected()));
		settings.setProperty("CRWriterDialog.includeRegionDetails",
							 String.valueOf(chkRegionDetails.isSelected()));
		settings.setProperty("CRWriterDialog.includeBuildings",
							 String.valueOf(chkBuildings.isSelected()));
		settings.setProperty("CRWriterDialog.includeShips",
							 String.valueOf(chkShips.isSelected()));
		settings.setProperty("CRWriterDialog.includeUnits",
							 String.valueOf(chkUnits.isSelected()));
		settings.setProperty("CRWriterDialog.includeMessages",
							 String.valueOf(chkMessages.isSelected()));
		settings.setProperty("CRWriterDialog.includeSpellsAndPotions",
							 String.valueOf(chkSpellsAndPotions.isSelected()));
		settings.setProperty("CRWriterDialog.delStats",
							 String.valueOf(chkDelStats.isSelected()));
		settings.setProperty("CRWriterDialog.delTrans",
							 String.valueOf(chkDelTrans.isSelected()));

		if(chkSelRegionsOnly.isEnabled()) {
			settings.setProperty("CRWriterDialog.includeSelRegionsOnly",
								 String.valueOf(chkSelRegionsOnly.isSelected()));
		}

		if(standAlone == true) {
			try {
				settings.store(new FileOutputStream(new File(System.getProperty("user.home"),
															 "CRWriterDialog.ini")), "");
			} catch(IOException e) {
				log.error("CRWriterDialog.storeSettings():", e);
			}
		}
	}

	private List<Object> getNewOutputFiles(JComboBox combo) {
		List<Object> ret = new ArrayList<Object>(combo.getItemCount() + 1);

		if(combo.getSelectedIndex() == -1) {
			ret.add(combo.getEditor().getItem());
		}

		for(int i = 0; i < Math.min(combo.getItemCount(), 6); i++) {
			ret.add(combo.getItemAt(i));
		}

		return ret;
	}

	protected void quit() {
		storeSettings();

		if(standAlone == true) {
			System.exit(0);
		} else {
			super.quit();
		}
	}

	private String getFileName(String defaultFile) {
		String retVal = null;

		JFileChooser fc = new JFileChooser();

		if(defaultFile != null) {
			fc.setSelectedFile(new File(defaultFile));
		}

		if(fc.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
			retVal = fc.getSelectedFile().getPath();
		}

		return retVal;
	}

  /**
   */
	private synchronized void write(Writer out) {
		setCursor(new Cursor(Cursor.WAIT_CURSOR));

		try {
			CRWriter crw = new CRWriter(new ProgressBarUI(Client.INSTANCE),out);
			crw.setServerConformance(chkServerConformance.isSelected());
			crw.setIncludeIslands(chkIslands.isSelected());
			crw.setIncludeRegions(chkRegions.isSelected());
			crw.setIncludeRegionDetails(chkRegionDetails.isSelected());
			crw.setIncludeBuildings(chkBuildings.isSelected());
			crw.setIncludeShips(chkShips.isSelected());
			crw.setIncludeUnits(chkUnits.isSelected());
			crw.setIncludeMessages(chkMessages.isSelected());
			crw.setIncludeSpellsAndPotions(chkSpellsAndPotions.isSelected());

			GameData newData = data;

			if(chkDelStats.isSelected()) {
				try {
					newData = (GameData) data.clone();
				} catch(CloneNotSupportedException e) {
					log.error("CRWriterDialog: trying to clone gamedata failed, fallback to merge method.",e);
					newData = GameData.merge(data, data);
				}

				// delete points, person counts, spell school, alliances, messages
				// of privileged factions
			  //
				if(newData.factions() != null) {
					Iterator<Faction> it1 = newData.factions().values().iterator();
					boolean excludeBRegions = (crw.getIncludeMessages() && chkSelRegionsOnly.isSelected() && (regions != null) && (regions.size() > 0));

					while(it1.hasNext()) {
						Faction f = it1.next();
						boolean found = true;

						if(excludeBRegions) {
							Iterator<Region> it2 = regions.iterator();
							found = false;

							while(!found && it2.hasNext()) {
								Region reg = it2.next();
								Iterator<Unit> it3 = reg.units().iterator();

								while(!found && it3.hasNext()) {
									Unit unit = it3.next();
									found = f.equals(unit.getFaction());
								}
							}

							if(!found) {
                it1.remove(); // ???? TR: why removing it from the iterator...
							}
						}

						if(found && f.isPrivileged()) {
							f.setAverageScore(-1);
							f.setScore(-1);
							f.setPersons(-1);
							f.setMigrants(-1);
							f.setMaxMigrants(-1);
							f.setSpellSchool(null);
							f.setAllies(null);
							// TODO: heroes?

							if(excludeBRegions && (f.getMessages() != null)) {
								Iterator<Message> it2 = f.getMessages().iterator();

								while(it2.hasNext()) {
									Message mes = it2.next();
									found = false;

									Iterator<Region> it3 = regions.iterator();

									while(it3.hasNext() && !found) {
										Region reg = it3.next();

										if(reg.getMessages() != null) {
											Iterator<Message> it4 = reg.getMessages().iterator();

											while(!found && it4.hasNext()) {
												found = mes.equals(it4.next());
											}
										}
									}

									if(!found) {
										it2.remove();
									}
								}
							}
						}
					}
				}
			}

			if(chkDelTrans.isSelected()) {
				// clean translation table
				List<String> trans = new LinkedList<String>(newData.translations().keySet());

				// some static data that is not connected but needed
				trans.remove("Einheit");
				trans.remove("Person");
				trans.remove("verwundet");
				trans.remove("schwer verwundet");
				trans.remove("erschöpft");

				Collection<Region> lookup = data.regions().values();

				if(chkSelRegionsOnly.isSelected() && (regions != null) && (regions.size() > 0)) {
					lookup = regions;
				}

				Iterator<Region> regionIterator = lookup.iterator();
				Iterator<RegionResource> resourceIterator = null;
        Iterator<Ship> shipIterator = null;
				Iterator<Building> buildingIterator = null;
        Iterator<Unit> unitIterator = null;
        Iterator<Item> itemIterator = null;
        Iterator<Potion> potionIterator = null;
        Iterator<Spell> spellIterator = null;
        Iterator<Skill> skillIterator = null;
        Iterator<String> stringIterator = null;
				boolean checkShips = chkShips.isSelected();
				boolean checkUnits = chkUnits.isSelected();
				boolean checkBuildings = chkBuildings.isSelected();
				boolean checkSpells = chkSpellsAndPotions.isSelected();
				boolean checkRegDetails = chkRegionDetails.isSelected();

				while(regionIterator.hasNext()) {
					Region r = regionIterator.next();
					trans.remove(r.getType().getID().toString());

					if(checkRegDetails) {
            resourceIterator = r.resources().iterator();

						while(resourceIterator.hasNext()) {
							magellan.library.RegionResource res = resourceIterator.next();
							trans.remove(res.getID().toString());
							trans.remove(res.getType().getID().toString());
						}
					}

					if(checkShips) {
            shipIterator = r.ships().iterator();

						while(shipIterator.hasNext()) {
							trans.remove((shipIterator.next()).getType().getID().toString());
						}
					}

					if(checkBuildings) {
						buildingIterator = r.buildings().iterator();

						while(buildingIterator.hasNext()) {
							trans.remove((buildingIterator.next()).getType().getID().toString());
						}
					}

					if(checkUnits) {
						unitIterator = r.units().iterator();

						while(unitIterator.hasNext()) {
							Unit u = unitIterator.next();
							trans.remove(u.getRace().getID().toString());

							if(u.getRaceNamePrefix() != null) {
								trans.remove(u.getRaceNamePrefix());
							} else {
								if((u.getFaction() != null) && (u.getFaction().getRaceNamePrefix() != null)) {
									trans.remove(u.getFaction().getRaceNamePrefix());
								}
							}

							itemIterator = u.getItems().iterator();

							while(itemIterator.hasNext()) {
								trans.remove((itemIterator.next()).getItemType().getID().toString());
							}

							skillIterator = u.getSkills().iterator();

							while(skillIterator.hasNext()) {
								trans.remove((skillIterator.next()).getSkillType().getID().toString());
							}
						}
					}
				}

				if(checkSpells) {
					spellIterator = data.spells().values().iterator();

					while(spellIterator.hasNext()) {
						Spell sp = spellIterator.next();
						trans.remove(sp.getID().toString());
            trans.remove(sp.getName());
            
            stringIterator = sp.getComponents().keySet().iterator();
            while(stringIterator.hasNext()) {
            	trans.remove(stringIterator.next());
            }
					}

					potionIterator = data.potions().values().iterator();

					while(potionIterator.hasNext()) {
						Potion sp = potionIterator.next();
						trans.remove(sp.getID().toString());
						itemIterator = sp.ingredients().iterator();

						while(itemIterator.hasNext()) {
							trans.remove(itemIterator.next().getItemType().getID()
									.toString());
						}
					}
				}

				if(trans.size() > 0) {
					log.debug("Following translations will be removed:");
					stringIterator = trans.iterator();

					java.util.Map<String,String> newTrans = newData.translations();

					while(stringIterator.hasNext()) {
						Object o = stringIterator.next();
						newTrans.remove(o);

						if(log.isDebugEnabled()) {
							log.debug("Removing: " + o);
						}
					}
				}
			}

			if(chkSelRegionsOnly.isSelected() && (regions != null) && (regions.size() > 0)) {
				crw.setRegions(regions);
			}

			crw.write(newData);
			crw.close();
		} catch(Exception exc) {
			log.error(exc);
			JOptionPane.showMessageDialog(this, Resources.get("crwriterdialog.msg.exporterror.text") + exc.toString(),
          Resources.get("crwriterdialog.msg.exporterror.title"),
										  JOptionPane.WARNING_MESSAGE);
		}

		setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
	}
}
