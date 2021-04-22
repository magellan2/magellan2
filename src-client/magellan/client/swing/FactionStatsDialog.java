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
import java.awt.Frame;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
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
import magellan.client.utils.SwingUtils;
import magellan.library.CoordinateID;
import magellan.library.EntityID;
import magellan.library.Faction;
import magellan.library.GameData;
import magellan.library.Group;
import magellan.library.ID;
import magellan.library.TrustLevel;
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

  private static FactionTrustComparator factionTrustComparator =
      FactionTrustComparator.DEFAULT_COMPARATOR;
  private static NameComparator nameComparator = new NameComparator(IDComparator.DEFAULT);

  /**
   * Create a new FactionStatsDialog object as a dialog with a parent window.
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
   */
  public FactionStatsDialog(Frame owner, boolean modal, EventDispatcher ed, GameData initData,
      Properties p, Faction f) {
    this(owner, modal, ed, initData, p);
    lstFaction.setSelectedValue(f, true);
  }

  /**
   *
   */
  private void init() {
    setContentPane(getMainPane());
    setTitle(Resources.get("factionstatsdialog.window.title"));

    int width = Integer.parseInt(settings.getProperty("FactionStatsDialog.width", "800"));
    int height = Integer.parseInt(settings.getProperty("FactionStatsDialog.height", "540"));
    setSize(width, height);

    SwingUtils.setLocation(this, settings, "FactionStatsDialog.x", "FactionStatsDialog.y");

    splFaction.setDividerLocation(Integer.parseInt(settings.getProperty("FactionStatsDialog.split",
        "340")));

    ID selFacID =
        EntityID.createEntityID(settings.getProperty("FactionStatsDialog.selFacID", "-1"), 10,
            data.base);
    Faction selFac = data.getFaction(selFacID);

    if (selFac != null) {
      lstFaction.setSelectedValue(selFac, true);
    } else {
      lstFaction.setSelectedIndex(0);
    }
  }

  /**
   * 
   */
  private Container getMainPane() {
    JPanel mainPanel = new JPanel(new BorderLayout(0, 5));
    mainPanel.setBorder(new EmptyBorder(4, 4, 4, 4));

    // optionPanel = new EresseaOptionPanel();
    tabPane = new JTabbedPane();
    tabPane.addTab(Resources.get("factionstatsdialog.tab.stats.caption"), null, pnlStats, null);

    JPanel skillChartPanel = getSkillChartPanel();

    if (skillChartPanel != null) {
      tabPane.addTab(Resources.get("factionstatsdialog.tab.skillchart.caption"), skillChartPanel);
    }

    // pavkovic 2003.11.19: deactivated, because EresseaOptionPanel is currently broken
    // tabPane.addTab(Resources.get("factionstatsdialog.tab.options.caption"), null, optionPanel,
    // null);
    splFaction = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, getFactionPanel(), tabPane);
    mainPanel.add(splFaction, BorderLayout.CENTER);
    mainPanel.add(getButtonPanel(), BorderLayout.SOUTH);

    return mainPanel;
  }

  /**
   * 
   */
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

  /**
   * 
   */
  private Container getFactionPanel() {
    factions = new LinkedList<Faction>(data.getFactions());

    String sortByTrustLevel = settings.getProperty("FactionStatsDialog.SortByTrustLevel", "true");

    // sort factions
    if (sortByTrustLevel.equals("true")) {
      Collections.sort(factions, FactionStatsDialog.factionTrustComparator);
    } else if (sortByTrustLevel.equals("detailed")) {
      Collections.sort(factions, FactionTrustComparator.DETAILED_COMPARATOR);
    } else {
      Collections.sort(factions, FactionStatsDialog.nameComparator);
    }

    final FactionStatsDialog d = this;
    lstFaction = new JList(factions.toArray());

    lstFaction.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
    lstFaction.addListSelectionListener(new ListSelectionListener() {
      public void valueChanged(ListSelectionEvent e) {
        if (e.getValueIsAdjusting())
          return;
        lstFaction.ensureIndexIsVisible(lstFaction.getLeadSelectionIndex());

        SelectionEvent se = null;
        JList list = (JList) e.getSource();

        /**
         * Ulrich Küster: (!) Special care has to be taken for the FactionStatsDialog. It can not be
         * differed, if SelectionEvents come from the faction list in FactionStatsDialog or from
         * other components of Magellan. To keep the faction list in this object consistent to the
         * displayed data in the FactionStatsPanel object, FactionStatsPanel.setFaction() should be
         * _never_ called by FactionStatsPanel.selectionChanged(), but always directly by this
         * method.
         */
        for (int i = 0; i < tabPane.getTabCount(); i++) {
          Component c = tabPane.getComponentAt(i);

          if (c instanceof FactionStatsPanel) {
            ((FactionStatsPanel) c).setFactions(magellan.library.utils.filters.CollectionFilters
                .filter(list.getSelectedValues(), Faction.class));
          } else if (c instanceof SelectionListener) {
            if ((list.getModel().getSize() > 0) && !list.isSelectionEmpty()) {
              List<List<Object>> contexts = new ArrayList<List<Object>>();
              for (Object o : list.getSelectedValues()) {
                contexts.add(Collections.singletonList(o));
              }
              se = SelectionEvent.create(d, contexts);
            } else {
              se = SelectionEvent.create(d);
            }
            ((SelectionListener) c).selectionChanged(se);
          }
        }
      }
    });

    String s;

    if (sortByTrustLevel.equalsIgnoreCase("true")) {
      s = Resources.get("factionstatsdialog.btn.sort.detailed.caption");
    } else if (sortByTrustLevel.equalsIgnoreCase("detailed")) {
      s = Resources.get("factionstatsdialog.btn.sort.name.caption");
    } else {
      s = Resources.get("factionstatsdialog.btn.sort.trustlevel.caption");
    }

    final JButton sort = new JButton(s);
    sort.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        String sortByTrust = settings.getProperty("FactionStatsDialog.SortByTrustLevel", "true");
        if (sortByTrust.equalsIgnoreCase("true")) {
          sortByTrust = "detailed";
          sort.setText(Resources.get("factionstatsdialog.btn.sort.name.caption"));
          Collections.sort(factions, FactionTrustComparator.DETAILED_COMPARATOR);
        } else if (sortByTrust.equalsIgnoreCase("detailed")) {
          sortByTrust = "false";
          sort.setText(Resources.get("factionstatsdialog.btn.sort.trustlevel.caption"));
          Collections.sort(factions, FactionStatsDialog.nameComparator);
        } else {
          sortByTrust = "true";
          sort.setText(Resources.get("factionstatsdialog.btn.sort.detailed.caption"));
          Collections.sort(factions, FactionStatsDialog.factionTrustComparator);
        }
        settings.setProperty("FactionStatsDialog.SortByTrustLevel", String.valueOf(sortByTrust));

        Object o = lstFaction.getSelectedValue();
        lstFaction.setListData(factions.toArray());
        lstFaction.setSelectedValue(o, true);
        lstFaction.repaint();
      }
    });

    JButton btnDeleteFaction =
        new JButton(Resources.get("factionstatsdialog.btn.deletefaction.caption"));
    btnDeleteFaction.setMnemonic(Resources.get("factionstatsdialog.btn.deletefaction.mnemonic")
        .charAt(0));
    btnDeleteFaction.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        Object[] values = lstFaction.getSelectedValues();
        List<Faction> victims = new LinkedList<Faction>();
        for (Object value : values) {
          victims.add((Faction) value);
        }

        for (Iterator<Faction> iter = victims.iterator(); iter.hasNext();) {
          Faction f = iter.next();
          boolean veto = false;

          // we don't delete factions with units. This units are visible and it would
          // be fatal to delete their faction "container".
          if (f.units().size() > 0) {
            Object msgArgs[] = { f };
            JOptionPane.showMessageDialog(d, (new java.text.MessageFormat(Resources
                .get("factionstatsdialog.msg.factioncontainsunits.text"))).format(msgArgs));
            veto = true;
          }

          if (!veto) {
            for (Faction dummy : data.getFactions()) {
              // let's check, if one faction outside the selection has an alliance with this
              // faction. If so, we should NOT delete the faction.
              Object msgArgs[] = null;
              if (dummy.units().size() > 0 && !victims.contains(dummy)) {
                if ((dummy.getAllies() != null && dummy.getAllies().containsKey(f.getID()))
                    || (dummy.getAlliance() != null && dummy.getAlliance().getFactions().contains(
                        f.getID()))) {
                  msgArgs = new Object[] { f, dummy };
                } else if (dummy.getGroups() != null) {
                  for (Group group : dummy.getGroups().values()) {
                    if (group.allies() != null && group.allies().containsKey(f.getID())) {
                      msgArgs = new Object[] { f, dummy };
                      break;
                    }
                  }
                }
                if (msgArgs != null) {
                  JOptionPane.showMessageDialog(d, (new java.text.MessageFormat(Resources
                      .get("factionstatsdialog.msg.factionisallied.text"))).format(msgArgs));
                  veto = true;
                  break;
                }
              }
            }
          }

          if (!veto) {
            Object msgArgs[] = { f, };

            if (JOptionPane.showConfirmDialog(d, (new java.text.MessageFormat(Resources
                .get("factionstatsdialog.msg.confirmdeletefaction.text"))).format(msgArgs),
                Resources.get("factionstatsdialog.msg.confirmdeletefaction.title"),
                JOptionPane.YES_NO_OPTION) != JOptionPane.OK_OPTION) {
              veto = true;
            }
          }

          if (veto) {
            iter.remove();
          }
        }

        if (victims.size() > 0) {
          for (Faction f : victims) {
            data.removeFaction(f.getID());
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

    final JButton btnPassword = new JButton(Resources.get("factionstatsdialog.btn.setpwd.caption"));
    btnPassword.setMnemonic(Resources.get("factionstatsdialog.btn.setpwd.menmonic").charAt(0));
    btnPassword.setToolTipText(Resources.get("factionstatsdialog.btn.setpwd.tooltip"));
    btnPassword.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        if ((lstFaction.getModel().getSize() <= 0) || lstFaction.isSelectionEmpty())
          return;

        Faction f = (Faction) lstFaction.getSelectedValue();

        FactionPropertiesDialog dialog =
            new FactionPropertiesDialog(null, true, dispatcher, data, getSettings(), f);

        dialog.setVisible(true);

        if (dialog.approved()) {
          String pwd = dialog.getPassword();

          if (pwd != null) { // if user did not hit the cancel button

            if (!pwd.equals("")) { // if this password is valid
              f.setPassword(pwd);
            } else {
              f.setPassword(null);
            }

            // store the password to the settings even if it is invalid
            settings.setProperty("Faction.password." + (f.getID()).intValue(),
                (f.getPassword() != null) ? f.getPassword() : "");

            // if the pw is valid increase this faction's trust level
            if (f.getPassword() != null) {
              f.setTrustLevel(TrustLevel.TL_PRIVILEGED);
            } else {
              // default is okay here, combat ally trust levels are restored
              // in the next loop anyway
              f.setTrustLevel(TrustLevel.TL_DEFAULT);
            }

            TrustLevels.recalculateTrustLevels(data);
          }

          if (dialog.isOwner()) {
            data.setOwnerFaction(f.getID());
          } else if (data.getOwnerFaction() != null && data.getOwnerFaction().equals(f.getID())) {
            data.setOwnerFaction(null);
          }

          Collection<CoordinateID> translations = dialog.getTranslations();
          if (translations != null) {
            data.clearTranslations(f.getID());
            for (CoordinateID translation : translations) {
              data.setCoordinateTranslation(f.getID(), translation);
            }
          }
          // notify game data listeners
          dispatcher.fire(new GameDataEvent(this, data));
        }
      }
    });

    // enable password button only if exactly one faction is selected.
    lstFaction.addListSelectionListener(new ListSelectionListener() {
      public void valueChanged(ListSelectionEvent e) {
        btnPassword.setEnabled(lstFaction.getSelectedIndices().length == 1);
      }
    });

    JButton btnTrustlevel = new JButton(Resources.get("factionstatsdialog.btn.trustlevel.caption"));
    btnTrustlevel
        .setMnemonic(Resources.get("factionstatsdialog.btn.trustlevel.mnemonic").charAt(0));
    btnTrustlevel.setToolTipText(Resources.get("factionstatsdialog.btn.trustlevel.tooltip"));
    btnTrustlevel.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        if ((lstFaction.getModel().getSize() <= 0) || lstFaction.isSelectionEmpty())
          return;

        Object selectedFactions[] = lstFaction.getSelectedValues();
        boolean validInput = false;

        while (!validInput) {
          // ask for Trustlevel
          validInput = true;

          String oldTrustLevel;

          if (selectedFactions.length == 1) {
            Faction faction = (Faction) selectedFactions[0];

            if (faction.isTrustLevelSetByUser()) {
              oldTrustLevel = String.valueOf(faction.getTrustLevel());
            } else {
              oldTrustLevel = ""; // indicates default
            }
          } else {
            oldTrustLevel = ""; // more than one faction selected
          }

          String stringValue =
              (String) JOptionPane.showInputDialog(FactionStatsDialog.this, Resources
                  .get("factionstatsdialog.msg.trustlevelinput.text"), Resources
                      .get("factionstatsdialog.msg.trustlevelinput.title"),
                  JOptionPane.QUESTION_MESSAGE, null, null, oldTrustLevel);

          if (stringValue != null) {
            for (Object selectedFaction : selectedFactions) {
              Faction faction = (Faction) selectedFaction;

              // this indicates, that further on Magellan shall
              // calculate the trustlevel for this faction on its own
              if (stringValue.length() == 0) {
                faction.setTrustLevelSetByUser(false);
                faction.setTrustLevel(TrustLevel.TL_DEFAULT);
              } else {
                try {
                  int intValue = Integer.parseInt(stringValue);
                  faction.setTrustLevel(intValue);
                  faction.setTrustLevelSetByUser(true);
                } catch (NumberFormatException exc) {
                  // ask again for input
                  validInput = false;
                }
              }
            }

            if (validInput) {
              TrustLevels.recalculateTrustLevels(data);

              // GameData did probably change
              dispatcher.fire(new GameDataEvent(this, data));
              Collections.sort(factions, FactionStatsDialog.factionTrustComparator);
            } else {
              JOptionPane.showMessageDialog(FactionStatsDialog.this, Resources
                  .get("factionstatsdialog.msg.trustlevelinputinvalid"));
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
   * sourcecode has become an integral part of the magellan code base. Thus it has no longer to be
   * instatiated via reflections.
   */
  private JPanel getSkillChartPanel() {
    /*
     * // try to load the skillchart classes ResourcePathClassLoader loader = new
     * ResourcePathClassLoader(settings); Class SkillChartPanel = null; try { SkillChartPanel =
     * loader.loadClass("magellan.client.skillchart.SkillChartPanel"); }
     * catch(java.lang.ClassNotFoundException cnf) { return null; } // get it's constructor
     * java.lang.reflect.Constructor constructor = null; try { constructor =
     * SkillChartPanel.getConstructor(new Class[] {
     * Class.forName("magellan.client.event.EventDispatcher"),
     * Class.forName("magellan.library.GameData"), Class.forName("java.util.Properties") }); }
     * catch(java.lang.NoSuchMethodException e) { log.error(e); return null; }
     * catch(java.lang.ClassNotFoundException e) { log.error(e); return null; }
     * catch(java.lang.NoClassDefFoundError e) { log.error(e); return null; } // create an instance
     * of this class Object skillChartPanel = null; try { skillChartPanel =
     * constructor.newInstance(new Object[] { dispatcher, data, settings }); }
     * catch(java.lang.reflect.InvocationTargetException e) { log.error(e); return null; }
     * catch(java.lang.IllegalAccessException e) { log.error(e); return null; }
     * catch(java.lang.InstantiationException e) { log.error(e); return null; } // return casted
     * Panel return (JPanel) skillChartPanel;
     */
    try {
      JPanel skillChartPanel = new SkillChartPanel(dispatcher, data, settings);

      return skillChartPanel;
    } catch (Throwable t) {
      FactionStatsDialog.log.warn(t + ": " + t.getLocalizedMessage());
      t.printStackTrace();
      FactionStatsDialog.log
          .warn("FactionStatsDialog.getSkillChartPanel(): Couldn't create skillChartPanel! Delivering null.");
    }

    return null;
  }

  private void storeSettings() {
    settings.setProperty("FactionStatsDialog.x", getX() + "");
    settings.setProperty("FactionStatsDialog.y", getY() + "");
    settings.setProperty("FactionStatsDialog.width", getWidth() + "");
    settings.setProperty("FactionStatsDialog.height", getHeight() + "");
    settings.setProperty("FactionStatsDialog.split", splFaction.getDividerLocation() + "");

    if ((lstFaction.getModel().getSize() > 0) && (lstFaction.getSelectedValue() != null)) {
      settings.setProperty("FactionStatsDialog.selFacID",
          (((Faction) lstFaction.getSelectedValue()).getID()).intValue() + "");
    }
  }

  @Override
  protected void quit() {
    storeSettings();
    super.quit();
  }

  /**
   * Rebuilds the faction list.
   * 
   * @see magellan.client.swing.InternationalizedDataDialog#gameDataChanged(magellan.library.event.GameDataEvent)
   */
  @Override
  public void gameDataChanged(GameDataEvent e) {
    Object oldSelection = lstFaction.getSelectedValue();
    data = e.getGameData();
    factions = new LinkedList<Faction>(data.getFactions());

    // sort factions
    Collections.sort(factions, FactionStatsDialog.factionTrustComparator);
    lstFaction.setListData(factions.toArray());

    ID selFacID =
        EntityID.createEntityID(settings.getProperty("FactionStatsDialog.selFacID", "-1"), 10,
            data.base);
    Faction selFac = data.getFaction(selFacID);

    if (selFac != null) {
      lstFaction.setSelectedValue(selFac, true);
    } else {
      lstFaction.setSelectedIndex(0);
    }
    // select old selection if possible
    if (oldSelection != null) {
      lstFaction.setSelectedValue(oldSelection, true);
    }

    lstFaction.repaint();
  }

}
