/*
 * Copyright (C) 2000-2004 Roger Butenuth, Andreas Gampe, Stefan Goetz, Sebastian Pappert, Klaas
 * Prause, Enno Rehling, Sebastian Tusk, Ulrich Kuester, Ilja Pavkovic This file is part of the
 * Eressea Java Code Base, see the file LICENSING for the licensing information applying to this
 * file.
 */

package magellan.client.swing;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ScrollPaneConstants;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;

import magellan.client.event.EventDispatcher;
import magellan.client.event.SelectionEvent;
import magellan.client.event.SelectionListener;
import magellan.client.swing.tree.CellRenderer;
import magellan.client.swing.tree.CopyTree;
import magellan.client.swing.tree.FactionNodeWrapper;
import magellan.client.swing.tree.ItemNodeWrapper;
import magellan.client.swing.tree.NodeWrapperFactory;
import magellan.client.swing.tree.RegionNodeWrapper;
import magellan.client.swing.tree.SimpleNodeWrapper;
import magellan.client.swing.tree.UnitContainerNodeWrapper;
import magellan.client.swing.tree.UnitNodeWrapper;
import magellan.client.utils.Units;
import magellan.library.Alliance;
import magellan.library.AllianceGroup;
import magellan.library.Building;
import magellan.library.CoordinateID;
import magellan.library.EntityID;
import magellan.library.Faction;
import magellan.library.GameData;
import magellan.library.Group;
import magellan.library.ID;
import magellan.library.Item;
import magellan.library.Message;
import magellan.library.Region;
import magellan.library.Ship;
import magellan.library.Skill;
import magellan.library.StringID;
import magellan.library.TempUnit;
import magellan.library.Unique;
import magellan.library.Unit;
import magellan.library.UnitContainer;
import magellan.library.UnitID;
import magellan.library.event.GameDataEvent;
import magellan.library.gamebinding.EresseaConstants;
import magellan.library.rules.ConstructibleType;
import magellan.library.rules.ItemCategory;
import magellan.library.rules.ItemType;
import magellan.library.rules.Race;
import magellan.library.rules.SkillType;
import magellan.library.rules.UnitContainerType;
import magellan.library.utils.CollectionFactory;
import magellan.library.utils.PropertiesHelper;
import magellan.library.utils.Resources;
import magellan.library.utils.SkillStats;
import magellan.library.utils.StringFactory;
import magellan.library.utils.TrustLevels;
import magellan.library.utils.comparator.AllianceFactionComparator;
import magellan.library.utils.comparator.IDComparator;
import magellan.library.utils.comparator.NameComparator;
import magellan.library.utils.comparator.SkillComparator;
import magellan.library.utils.comparator.SpecifiedSkillTypeSkillComparator;
import magellan.library.utils.comparator.UnitSkillComparator;
import magellan.library.utils.logging.Logger;

/**
 * A panel for showing statistics about factions.
 */
public class FactionStatsPanel extends InternationalizedDataPanel implements SelectionListener,
    TreeSelectionListener {
  private static final Logger log = Logger.getInstance(FactionStatsPanel.class);
  private Map<ID, Faction> factions = null;
  private Map<CoordinateID, Region> regions = null;
  private DefaultTreeModel treeModel = null;
  private DefaultMutableTreeNode rootNode = null;
  private CopyTree tree = null;
  private NodeWrapperFactory nodeWrapperFactory;
  private Units unitsTools = null;

  /**
   * Creates a new FactionStatsPanel object.
   */
  public FactionStatsPanel(EventDispatcher d, GameData initData, Properties p) {
    super(d, initData, p);
    setLayout(new BorderLayout());
    this.add(getStatPanel(), BorderLayout.CENTER);
    factions = new Hashtable<ID, Faction>(getGameData().factions());
    regions = new Hashtable<CoordinateID, Region>(getGameData().regions());
    dispatcher.addSelectionListener(this);
    unitsTools = (getGameData() != null) ? new Units(getGameData().getRules()) : new Units(null);
    nodeWrapperFactory = new NodeWrapperFactory(settings);

    // to get the pref-adapter FIXME remove
    nodeWrapperFactory.createUnitNodeWrapper(null);
    nodeWrapperFactory.createSkillNodeWrapper(null, new Skill(
        new SkillType(StringID.create("Test")), 0, 0, 0, false), null);
    nodeWrapperFactory.createItemNodeWrapper(new Item(new ItemType(StringID.create("Test")), 0));
  }

  /**
   * @see magellan.client.swing.InternationalizedDataPanel#gameDataChanged(magellan.library.event.GameDataEvent)
   */
  @Override
  public void gameDataChanged(GameDataEvent e) {
    setGameData(e.getGameData());

    /**
     * Don't clear factions as the SelectionEvent of the updated List in FactionStatsDialog might be
     * processed before the GameDataEvent
     */

    // factions.clear();
    if (getGameData() != null) {
      unitsTools.setRules(getGameData().getRules());
      // FIXME need to clear, or updateTree may be called on invalid faction list...
      // factions.clear();
      // regions.clear();
      setRegions(getGameData().getRegions());
    }
  }

  /**
   * Updates the panel if a new region has been selected.
   *
   * @see magellan.client.event.SelectionListener#selectionChanged(magellan.client.event.SelectionEvent)
   */
  public void selectionChanged(SelectionEvent e) {
    if (e.getSource() == this)
      return;

    if ((e.getSelectedObjects() != null) && (e.getSelectionType() == SelectionEvent.ST_REGIONS)) {
      List<Region> regions = new LinkedList<Region>();

      for (Object o : e.getSelectedObjects()) {
        if (o instanceof Region) {
          regions.add((Region) o);
        }
      }

      /**
       * Ulrich Küster: (!) Special care has to be taken. Generally it can not be differed, if
       * SelectionEvents come from the faction list in FactionStatsDialog or from other components
       * of Magellan (except if a special SelectionType has been defined in SelectionEvent and is
       * used). To keep the faction list in FactionStatsDialog consistent to the displayed data in
       * this FactionStatsPanel object, setFaction() should be _never_ called by this
       * selectionChanged()-method, but directly by the valueChanged()-method of FactionStatsDialog.
       */
      setRegions(regions);
    }
  }

  /**
   * Sets the set of factions to display and updates the panel contents.
   */
  public void setFactions(Collection<Faction> fs) {
    factions.clear();

    if (fs != null) {
      for (Faction f : fs) {
        factions.put(f.getID(), f);
      }
    }

    updateTree();
  }

  /**
   * DOCUMENT-ME
   */
  public void setRegions(Collection<Region> rs) {
    if ((rs == null) || rs.isEmpty()) {
      regions = new Hashtable<CoordinateID, Region>(getGameData().regions());
    } else {
      regions.clear();

      for (Region r : rs) {
        regions.put(r.getID(), r);
      }
    }

    updateTree();
  }

  /**
   * @see javax.swing.event.TreeSelectionListener#valueChanged(javax.swing.event.TreeSelectionEvent)
   */
  public void valueChanged(TreeSelectionEvent e) {
    DefaultMutableTreeNode node = (DefaultMutableTreeNode) tree.getLastSelectedPathComponent();

    if (node == null)
      return;

    Object o = node.getUserObject();

    if (o instanceof UnitNodeWrapper) {
      Unit u = ((UnitNodeWrapper) o).getUnit();
      dispatcher.fire(SelectionEvent.create(this, u));
    } else if (o instanceof UnitContainerNodeWrapper) {
      UnitContainer uc = ((UnitContainerNodeWrapper) o).getUnitContainer();
      dispatcher.fire(SelectionEvent.create(this, uc, SelectionEvent.ST_DEFAULT));
    } else if (o instanceof RegionNodeWrapper) {
      Region r = ((RegionNodeWrapper) o).getRegion();
      dispatcher.fire(SelectionEvent.create(this, r));
    }
  }

  /*
   * Ilja Pavkovic 2001.10.19: I wanted to see the original values and the predictions of units and
   * persons.
   */

  /**
   * Ulrich Küster: The algorithm wasn't correct as it is wrong to count the number of persons in
   * the temp units to get the number of recruited persons. a) Temp units always have zero persons
   * b) 'normal' units can recruit persons So it is necessary to look at all unit's
   * RecruitmentRelations in the units's cache-object. I changed that. (Of course this doesn't
   * consider, that persons can be given to '0'.)
   */
  private void updateTree() {
    tree.setShowsRootHandles(PropertiesHelper.getBoolean(settings,
        "EMapOverviewPanel.treeRootHandles", true));

    setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));

    DefaultMutableTreeNode currentNode = null;
    DefaultMutableTreeNode subNode = null;
    Map<ID, Unit> units = new Hashtable<ID, Unit>();
    long personCounter = 0;
    long maintenance = 0;
    long modifiedUnitsCounter = 0;
    long tempUnitsCounter = 0;
    long modifiedPersonCounter = 0;
    rootNode.removeAllChildren();

    currentNode =
        new DefaultMutableTreeNode(nodeWrapperFactory.createSimpleNodeWrapper(Resources.get(
            "factionstatspanel.node.regions", regions.size()), "regions"));

    rootNode.add(currentNode);

    /**
     * Used to collect persons of different race than their faction. Key: String (racename), Value:
     * List containing the units
     */
    Map<String, List<Unit>> specialPersons =
        CollectionFactory.<String, List<Unit>> createSyncOrderedMap();
    Collection<Unit> heroes = new LinkedList<Unit>();
    long heros_count = 0;

    SkillStats skillStats = new SkillStats();

    for (Region r : regions.values()) {
      /**
       * poorly it is necessary to refresh all relations, as at this time it is not assured that
       * they are always up to date. Possibly it would be better, to calculate them only, if orders
       * are loaded or changed...
       */
      r.refreshUnitRelations();

      for (Unit u : r.units()) {
        if (factions.containsKey(u.getFaction().getID())) {
          units.put(u.getID(), u);
          personCounter += u.getPersons();
          maintenance +=
              u.getPersons()
                  * (u.getRace().getMaintenance() >= 0 ? u.getRace().getMaintenance() : 10);
          skillStats.addUnit(u);

          Race race = u.getRace();

          if ((u.getFaction().getRace() != null) && !race.equals(u.getFaction().getRace())) {
            List<Unit> v = specialPersons.get(race.getName());

            if (v == null) {
              v = new LinkedList<Unit>();
              specialPersons.put(race.getName(), v);
            }

            v.add(u);
          }
          if (u.isHero()) {
            heroes.add(u);
            heros_count += u.getPersons();
          }

          if (u.getModifiedPersons() > 0) {
            modifiedUnitsCounter++;
            modifiedPersonCounter += u.getModifiedPersons();
          }

          if (u instanceof TempUnit) {
            tempUnitsCounter++;
          }
        }
      }
    }
    /**
     * Fiete 20060918: made all nodes with icons, thx to khadar for icons n = new
     * DefaultMutableTreeNode(Resources.get("factionstatspanel.node.units") + (units.size() -
     * tempUnitsCounter) + " (" + modifiedUnitsCounter + ")");
     */
    currentNode =
        new DefaultMutableTreeNode(nodeWrapperFactory.createSimpleNodeWrapper(Resources.get(
            "factionstatspanel.node.units", (units.size() - tempUnitsCounter),
            modifiedUnitsCounter, getGameData().getMaxUnits() > 0 ? getGameData().getMaxUnits()
                : "???"), "units"));

    rootNode.add(currentNode);
    /**
     * n = new DefaultMutableTreeNode(Resources.get("factionstatspanel.node.persons") +
     * personCounter + " (" + modifiedPersonCounter + ")");
     */
    currentNode =
        new DefaultMutableTreeNode(nodeWrapperFactory.createSimpleNodeWrapper(Resources
            .get("factionstatspanel.node.persons")
            + personCounter + " (" + modifiedPersonCounter + ")", "persons"));

    rootNode.add(currentNode);

    if (factions.size() == 1) {
      Faction f = null;
      f = factions.values().iterator().next();

      /* Race node */
      if (f.getType() != null) {
        // n = new
        // DefaultMutableTreeNode(Resources.get("factionstatspanel.node.race")
        // + f.getType().getName());
        currentNode =
            new DefaultMutableTreeNode(nodeWrapperFactory.createSimpleNodeWrapper(Resources
                .get("factionstatspanel.node.race")
                + f.getType().getName(), "race"));

        // Fiete 20090224: realRace for demons
        // not visisable in CR-Block for faction "PARTEI"
        // after merging the "Demon" is not visisble anymore
        // trick: check realrace irgendeiner Unit
        Race orgRace = null;
        for (Region r : regions.values()) {
          for (Unit u : r.units()) {
            if (u.getFaction().equals(f)) {
              if (u.getDisguiseRace() != null && u.getDisguiseRace() != u.getRace()) {
                // Bingo. Units DisguisedRace is different then the "Race"
                // last Check - OutputNames different?
                if (!f.getType().getName().equalsIgnoreCase(u.getRace().getName())) {
                  orgRace = u.getRace();
                  break;
                }
              }
            }
          }
          if (orgRace != null) {
            break;
          }
        }
        if (orgRace != null) {
          currentNode =
              new DefaultMutableTreeNode(nodeWrapperFactory.createSimpleNodeWrapper(Resources
                  .get("factionstatspanel.node.race")
                  + f.getType().getName() + " (" + orgRace.getName() + ")", "race"));
        }

        rootNode.add(currentNode);
      }

      /* Age node */
      if (f.getAge() > -1) {
        currentNode =
            new DefaultMutableTreeNode(nodeWrapperFactory.createSimpleNodeWrapper(Resources
                .get("factionstatspanel.node.age")
                + ": " + f.getAge(), "age"));
        rootNode.add(currentNode);
      }

      /* Locale node */
      if (f.getLocale() != null) {
        currentNode =
            new DefaultMutableTreeNode(nodeWrapperFactory.createSimpleNodeWrapper(Resources
                .get("factionstatspanel.node.locale")
                + ": " + f.getLocale(), "locale"));
        rootNode.add(currentNode);
      }

      /* Magic node */
      if (f.getSpellSchool() != null) {
        // n = new
        // DefaultMutableTreeNode(Resources.get("factionstatspanel.node.magicschool")
        // + f.spellSchool);
        currentNode =
            new DefaultMutableTreeNode(nodeWrapperFactory.createSimpleNodeWrapper(Resources
                .get("factionstatspanel.node.magicschool")
                + f.getSpellSchool(), "magicschool"));
        rootNode.add(currentNode);
      }

      /* Description node */
      String description = f.getDescription();
      if ((description != null) && (description.length() > 0)) {
        // n = new
        // DefaultMutableTreeNode(Resources.get("factionstatspanel.node.banner")
        // + description);
        currentNode =
            new DefaultMutableTreeNode(nodeWrapperFactory.createSimpleNodeWrapper(Resources
                .get("factionstatspanel.node.banner")
                + description, "banner"));
        rootNode.add(currentNode);
      }

      /* Email node */
      if (f.getEmail() != null) {
        // n = new DefaultMutableTreeNode(new
        // SimpleNodeWrapper(Resources.get("factionstatspanel.node.e-mail")
        // + f.email, f.email));
        // added email icon
        currentNode =
            new DefaultMutableTreeNode(new SimpleNodeWrapper(Resources
                .get("factionstatspanel.node.e-mail")
                + f.getEmail(), "email2"));

        rootNode.add(currentNode);
      }

      { /* Trustlevel node */
        String nodeLabel = TrustLevels.getTrustLevelLabel(f.getTrustLevel());
        nodeLabel += (" (" + f.getTrustLevel() + ")");
        // n = new DefaultMutableTreeNode(nodeLabel);
        currentNode = new DefaultMutableTreeNode(new SimpleNodeWrapper(nodeLabel, "trust"));
        rootNode.add(currentNode);
      }

      /* Report owner node */
      if (f.getID().equals(getGameData().getOwnerFaction())) {
        currentNode =
            new DefaultMutableTreeNode(new SimpleNodeWrapper(Resources
                .get("factionstatspanel.node.reportowner"), "reportowner"));
        rootNode.add(currentNode);
      }

      /* Translation node */
      Map<Integer, CoordinateID> map = getGameData().getCoordinateTranslationMap(f.getID());
      if (map != null) {
        currentNode =
            new DefaultMutableTreeNode(nodeWrapperFactory.createSimpleNodeWrapper(Resources
                .get("factionstatspanel.node.translations"), "coordinatetranslation"));
        SortedSet<Integer> layers = new TreeSet<Integer>(map.keySet());
        for (Integer i : layers) {
          CoordinateID translation = getGameData().getCoordinateTranslation(f.getID(), i);
          DefaultMutableTreeNode translationNode =
              new DefaultMutableTreeNode(nodeWrapperFactory.createSimpleNodeWrapper(
                  (new java.text.MessageFormat(Resources.get("factionstatspanel.node.layer")))
                      .format(new Integer[] { i })
                      + " " + translation, "layer"));
          currentNode.add(translationNode);
        }
        rootNode.add(currentNode);
      }

      /* score node */
      if (f.getScore() > 0) {
        String scoreLabel =
            Resources.get("factionstatspanel.node.score") + f.getScore() + "/"
                + f.getAverageScore() + " (" + (int) ((100.0 * f.getScore()) / f.getAverageScore())
                + "%)";
        currentNode =
            new DefaultMutableTreeNode(nodeWrapperFactory.createSimpleNodeWrapper(scoreLabel,
                "score"));

        rootNode.add(currentNode);
      }

      /* prefix node */
      if (f.getRaceNamePrefix() != null) {
        /**
         * n = new DefaultMutableTreeNode(Resources.get( "factionstatspanel.node.racenameprefix") +
         * f.getRaceNamePrefix());
         */
        currentNode =
            new DefaultMutableTreeNode(nodeWrapperFactory.createSimpleNodeWrapper(Resources
                .get("factionstatspanel.node.racenameprefix")
                + f.getRaceNamePrefix(), "prefix"));
        rootNode.add(currentNode);
      }

      /* migrants node */
      if (f.getMigrants() > 0) {
        /**
         * n = new DefaultMutableTreeNode(Resources.get( "factionstatspanel.node.migrants") +
         * f.migrants + "/" + f.maxMigrants);
         */
        currentNode =
            new DefaultMutableTreeNode(nodeWrapperFactory.createSimpleNodeWrapper(Resources
                .get("factionstatspanel.node.migrants")
                + f.getMigrants() + "/" + f.getMaxMigrants(), "migrants"));
        rootNode.add(currentNode);
      }

      /* alliances node */
      if ((f.getAllies() != null && f.getAllies().size() > 0) || f.getAlliance() != null) {
        currentNode =
            new DefaultMutableTreeNode(nodeWrapperFactory.createSimpleNodeWrapper(Resources
                .get("factionstatspanel.node.alliances"), "alliance"));
        rootNode.add(currentNode);
        FactionStatsPanel.showAlliances(getGameData(), f, f.getAllies(), f.getAlliance(),
            currentNode);
      }

      /* group alliances node */
      if ((f.getGroups() != null) && (f.getGroups().size() > 0)) {
        currentNode =
            new DefaultMutableTreeNode(nodeWrapperFactory.createSimpleNodeWrapper(Resources
                .get("factionstatspanel.node.groups"), "groups"));
        rootNode.add(currentNode);

        for (Group g : f.getGroups().values()) {
          subNode = new DefaultMutableTreeNode(g);
          currentNode.add(subNode);

          if (g.getRaceNamePrefix() != null) {
            subNode.add(new DefaultMutableTreeNode(Resources
                .get("factionstatspanel.node.racenameprefix")
                + g.getRaceNamePrefix()));
          }

          FactionStatsPanel.showAlliances(getGameData(), f, g.allies(), f.getAlliance(), subNode);
        }
      }

      /* other races node */
      if (specialPersons.size() > 0) {
        // n = new
        // DefaultMutableTreeNode(Resources.get("factionstatspanel.node.otherrace"));
        currentNode =
            new DefaultMutableTreeNode(nodeWrapperFactory.createSimpleNodeWrapper(Resources
                .get("factionstatspanel.node.otherrace"), "persons_of_other_race"));
        rootNode.add(currentNode);

        for (String raceName : specialPersons.keySet()) {
          List<Unit> v = specialPersons.get(raceName);
          long count = 0;
          String actRealRaceName = v.size() > 0 ? v.get(0).getRace().getIcon() : "";
          for (Unit actU : v) {
            count += actU.getPersons();
          }
          String iconPersonName = "person";
          // now we check if a specific race icon exists, if true, we use it
          if (getMagellanContext().getImageFactory().existImageIcon(actRealRaceName)) {
            iconPersonName = actRealRaceName;
          }

          subNode =
              new DefaultMutableTreeNode(nodeWrapperFactory.createSimpleNodeWrapper(raceName + ": "
                  + count, iconPersonName));
          /**
           * String raceNameLang = com.eressea.util.Umlaut.convertUmlauts(obj.toString()); String
           * iconNameEn = getString(raceNameLang); if (iconNameEn.equalsIgnoreCase(raceNameLang)) {
           * m = new DefaultMutableTreeNode(obj + ": " + count); } else { m = new
           * DefaultMutableTreeNode (nodeWrapperFactory.createSimpleNodeWrapper(obj + ": " + count,
           * iconNameEn)); }
           */
          currentNode.add(subNode);

          for (Unit actUnit : v) {
            DefaultMutableTreeNode o =
                new DefaultMutableTreeNode(nodeWrapperFactory.createUnitNodeWrapper(actUnit,
                    actUnit.getPersons()));
            subNode.add(o);
          }
        }
      }

      /* heroes node */
      if (f.getMaxHeroes() > -1 || heros_count > 0) {
        String maxHeros2 = "0 (!)";
        if (f.getMaxHeroes() > -1) {
          maxHeros2 = String.valueOf(f.getMaxHeroes());
        } else if (personCounter > 50) {
          // hack, but it should be in the report anyway
          // from week 1255 in E2, the first hero is at 550 people or so
          int offset = getGameData().getDate().getDate() > 1255 ? 500 : 0;
          long nsize = personCounter - offset;
          double maxHeros = 0;
          if (nsize > 0) {
            maxHeros = (java.lang.Math.log(nsize / 50.0) / java.lang.Math.log(10)) * 20;
            if (maxHeros < 0) {
              maxHeros = 0;
            }
          }
          maxHeros2 = java.lang.Math.round(java.lang.Math.floor(maxHeros)) + " (!)";
        }

        String actHeroes = "";
        if (f.getHeroes() != heros_count && f.getHeroes() > -1) {
          actHeroes =
              Resources.get("factionstatspanel.node.heroes") + " " + f.getHeroes() + "("
                  + heros_count + ")" + "/" + maxHeros2;
        } else {
          actHeroes =
              Resources.get("factionstatspanel.node.heroes") + " " + heros_count + "/" + maxHeros2;
        }

        currentNode =
            new DefaultMutableTreeNode(nodeWrapperFactory.createSimpleNodeWrapper(actHeroes,
                "heroes"));
        rootNode.add(currentNode);

        for (Unit u : heroes) {
          subNode =
              new DefaultMutableTreeNode(nodeWrapperFactory
                  .createUnitNodeWrapper(u, u.getPersons()));
          currentNode.add(subNode);
        }

      }
    }

    // Einnahmen
    // it is necessary to parse messages to get this information
    // 0 = Arbeiten
    // 1 = Unterhaltung
    // 2 = Treiben
    // 3 = Handel
    // 4 = am Handel
    // 5 = Diebstahl
    // 6 = Zauberei
    // final int E_WORK = 0;
    // final int E_ENTERTAIN = 1;
    final int E_TAX = 2;
    // final int E_TRADE = 3;
    final int E_TRADETAX = 4;
    // final int E_THEFT = 5;
    // final int E_MAGIC = 6;
    final int E_MAX = 7;

    long earned[] = new long[E_MAX];
    long wanted[] = new long[E_MAX];
    Arrays.fill(earned, 0);
    Arrays.fill(wanted, 0);

    long totalIncome = 0;
    long totalWanted = 0;

    final int EE_ALMS = 0;
    final int EE_TRANSFERS = 1;
    long extraEarned[] = new long[] { 0, 0 };
    // 7 = Almosen
    // 8 = Übergaben

    // Übergaben an andere Parteien
    Map<ID, Long> factionGettings = new HashMap<ID, Long>();

    Map<ID, Long> factionAlmsGotten = new HashMap<ID, Long>();
    Map<ID, Map<Region, Long>> almGottenRegions = new HashMap<ID, Map<Region, Long>>();

    // Ausgaben
    // it is necessary to parse messages to get this information
    // 0 = Unterhalt (just count Persons * 10 ?)
    // 1 = Gebäudeunterhalt
    // 2 = Lernkosten
    // 3 = Magiekosten (no chance: Messages contains just the magic item, no
    // cost. No chance to find the spell for sure..)
    // 4 = Handel
    // 5 = Diebstahl
    // 6 = Almosen
    // 7 = Übergaben an andere Parteien
    final int S_SUPPORT = 0;
    final int S_UPKEEP = 1;
    final int S_LEARN = 2;
    // final int S_MAGIC = 3;
    final int S_TRADE = 4;
    final int S_THEFT = 5;
    final int S_ALMS = 6;
    final int S_TRANSFERS = 7;
    long spent[] = new long[8];
    Arrays.fill(spent, 0);

    Map<StringID, Long> buildingUpkeep = new HashMap<StringID, Long>();

    Map<ID, Long> factionGivings = new HashMap<ID, Long>();

    Map<ID, Long> factionAlmsGiven = new HashMap<ID, Long>();
    Map<ID, Map<Region, Long>> almGivenRegions = new HashMap<ID, Map<Region, Long>>();

    long totalSpent = 0;

    for (Faction faction : factions.values()) {
      if (faction.getMessages() == null) {
        continue;
      }

      for (Message msg : faction.getMessages()) {
        // check whether the message belongs to one of the selected regions
        String value = msg.getAttribute("region");
        if (value != null) {
          String regionCoordinate = value;
          ID coordinate = CoordinateID.parse(regionCoordinate, ",");
          if (coordinate == null) {
            coordinate = CoordinateID.parse(regionCoordinate, " ");
          }
          if (!regions.containsKey(coordinate)) {
            continue;
          }
        } else {
          // some messages, for instance of type 170076 don't have
          // a region tag but a unit tag. Therefore get the region
          // via the unit of that message!
          value = msg.getAttribute("unit");
          if (value != null) {
            String number = value;
            UnitID id = UnitID.createUnitID(number, 10, getGameData().base);
            Unit unit = getGameData().getUnit(id);

            if (unit != null) {
              Region r = unit.getRegion();

              if (r != null) {
                CoordinateID c = r.getCoordinate();

                if (!regions.containsKey(c)) {
                  continue;
                }
              }
            }
          }
        } // region check done

        if (msg.getMessageType() == null || msg.getMessageType().getID() == null) {
          continue;
        }

        int msgID = (msg.getMessageType().getID()).intValue();
        try {
          if ((msgID == 771334452) || (msgID == 2097)) {
            // xyz verdient ... [durch Unterhaltung | ]
            String modeValue = msg.getAttribute("mode");

            if (modeValue != null) {
              int i = Integer.parseInt(modeValue);
              if (i >= E_MAX) {
                log.warnOnce("unknwon earn mode " + i);
                i = E_MAX;
              }
              value = msg.getAttribute("amount");

              if (value != null) {
                earned[i] += Integer.parseInt(value);
              }

              value = msg.getAttribute("wanted");

              if (value != null) {
                wanted[i] += Integer.parseInt(value);
              }
            }
          } else if (msgID == 1264208711) {
            // E3 Steuern
            value = msg.getAttribute("amount");

            if (value != null) {
              earned[E_TAX] += Integer.parseInt(value);
              wanted[E_TAX] += Integer.parseInt(value);
            }
          } else if (msgID == 107552268) {
            // Gebäudeunterhalt
            String buildingNR = msg.getAttribute("building");
            if (buildingNR != null) {
              int i = Integer.parseInt(buildingNR);
              EntityID id = EntityID.createEntityID(i, getGameData().base);
              // get the building
              Building b = getGameData().getBuilding(id);
              if (b != null) {
                // get Building type
                ConstructibleType bT = b.getBuildingType();
                if (bT != null) {
                  // get Maintenance cost Silver
                  ItemType silverType =
                      getGameData().getRules().getItemType(EresseaConstants.I_USILVER);
                  if (silverType != null) {
                    Item silverItem = bT.getMaintenance(silverType.getID());
                    if (silverItem != null) {
                      spent[S_UPKEEP] += silverItem.getAmount();
                      // entry in building categories
                      increase(buildingUpkeep, bT.getID(), silverItem.getAmount());
                    }
                  }
                }
              }
            }
          } else if (msgID == 170076) {
            // bezahlt für Kauf von Luxusgütern
            value = msg.getAttribute("money");

            if (value != null) {
              spent[S_TRADE] += Integer.parseInt(value);
            }
          } else if (msgID == 443066738) {
            // Lernkosten
            value = msg.getAttribute("cost");
            if (value != null) {
              spent[S_LEARN] += Integer.parseInt(value);
            }
          } else if (msgID == 1543395091) {
            // Diebstahl
            try {
              Unit unit =
                  getGameData().getUnit(
                      UnitID.createUnitID(Integer.parseInt(msg.getAttribute("unit")),
                          getGameData().base));
              int amount = Integer.parseInt(msg.getAttribute("amount"));
              if (factions.containsKey(unit.getFaction().getID())) {
                spent[S_THEFT] += amount;
              }
            } catch (Exception e) {

            }
          } else if (msgID == 1682429624) {
            // Almosen
            // müssen in den Regionsmessages überprüft werden

          } else if (msgID == 5281483) {
            // Übergaben
            value = msg.getAttribute("resource");
            String value2 = msg.getAttribute("amount");
            if (value != null && value.equalsIgnoreCase(EresseaConstants.I_USILVER.toString())
                && value2 != null) {
              long amount = Integer.parseInt(value2);
              value = msg.getAttribute("unit");
              UnitID id = UnitID.createUnitID(value, 10, getGameData().base);
              Unit giver = getGameData().getUnit(id);
              ID giverID =
                  (giver == null || giver.getFaction() == null) ? EntityID.createEntityID(-1,
                      getGameData().base) : giver.getFaction().getID();
              value = msg.getAttribute("target");
              id = UnitID.createUnitID(value, 10, getGameData().base);
              Unit target = getGameData().getUnit(id);
              ID targetID =
                  (target == null || target.getFaction() == null) ? EntityID.createEntityID(-1,
                      getGameData().base) : target.getFaction().getID();
              // von factions an andere Parteien
              if (giver != null && factions.containsKey(giverID)) {
                increase(factionGivings, targetID, amount);
                spent[S_TRANSFERS] += amount;
              }
              // von anderen Parteien an factions
              if (target != null && factions.containsKey(target.getFaction().getID())) {
                increase(factionGettings, giverID, amount);
                extraEarned[EE_TRANSFERS] += amount;
              }
            }
          }
        } catch (NumberFormatException e) {
          FactionStatsPanel.log.error("unexpected message format: " + msg + "\n" + e);
        }

      } // loop messages

      // search region messages for Almosen
      if (regions != null && regions.size() > 0) {
        for (Region currentRegion : regions.values()) {
          if (currentRegion.getMessages() != null && currentRegion.getMessages().size() > 0) {
            for (Message currentMessage : currentRegion.getMessages()) {
              if ((currentMessage.getMessageType() != null)
                  && (currentMessage.getMessageType().getID() != null)) {
                int msgID = (currentMessage.getMessageType().getID()).intValue();
                if (msgID == 1682429624) {
                  try {
                    // Almosen
                    // check, ob from unsere Faction ist
                    String from = currentMessage.getAttribute("from");
                    EntityID fromID =
                        EntityID.createEntityID(Integer.parseInt(from), getGameData().base);
                    // Faction beziehen
                    ID fromFactionID =
                        getGameData().getFaction(fromID) == null ? EntityID.createEntityID(-1,
                            getGameData().base) : getGameData().getFaction(fromID).getID();
                    String to = currentMessage.getAttribute("to");
                    EntityID toID = EntityID.createEntityID(Integer.parseInt(to), getGameData().base);
                    ID toFactionID =
                        getGameData().getFaction(toID) == null ? EntityID.createEntityID(-1,
                            getGameData().base) : getGameData().getFaction(toID).getID();
                    long amount = Integer.parseInt(currentMessage.getAttribute("amount"));
                    if (fromFactionID.equals(faction.getID())) {
                      // alms from us
                      spent[S_ALMS] += amount;
                      // collect data for subnodes
                      increase(factionAlmsGiven, toFactionID, amount);

                      Map<Region, Long> factionAlms = almGivenRegions.get(toFactionID);
                      if (factionAlms == null) {
                        factionAlms = new HashMap<Region, Long>();
                      }
                      increase(factionAlms, currentRegion, amount);
                      almGivenRegions.put(toFactionID, factionAlms);
                    } else if (toFactionID.equals(faction.getID())) {
                      // alms to us
                      extraEarned[EE_ALMS] += amount;
                      // collect data for subnodes
                      increase(factionAlmsGotten, fromFactionID, amount);

                      Map<Region, Long> factionAlms = almGottenRegions.get(fromFactionID);
                      if (factionAlms == null) {
                        factionAlms = new HashMap<Region, Long>();
                      }
                      increase(factionAlms, currentRegion, amount);
                      almGottenRegions.put(fromFactionID, factionAlms);
                    }
                  } catch (Exception e) {
                    FactionStatsPanel.log.error("unexpected message format: " + currentMessage + "\n" + e);
                  }
                }
              }
            }
          }
        }
      }

    } // loop factions

    // Unterhalt
    // we ignore persons of races which do not need support
    spent[S_SUPPORT] = maintenance;

    for (long element : spent) {
      totalSpent += element;
    }

    for (int i = 0; i < earned.length; i++) {
      totalIncome += earned[i];
      totalWanted += wanted[i];
    }
    for (long element : extraEarned) {
      totalIncome += element;
      totalWanted += element;
    }

    // iconnames for income groups
    // 0 = Arbeiten
    // 1 = Unterhaltung
    // 2 = Treiben
    // 3 = Handel
    // 4 = am Handel
    // 5 = Diebstahl
    // 6 = Zauberei
    String incomeGroupIcon[] =
        new String[] { "Arbeiten", "Unterhaltung", "Steuereintreiben", "Handeln", "Handeln",
            "Tarnung", "Magie", "Sonstiges" };
    String incomeGroupIcon2[] = { "Alliance", "Persons" };

    if ((totalIncome != 0) || (totalWanted != 0)) {
      Object msgArgs[] = { Long.valueOf(totalIncome), Long.valueOf(totalWanted) };
      // n = new DefaultMutableTreeNode((new
      // java.text.MessageFormat(Resources.get("factionstatspanel.node.income"))).format(msgArgs));
      currentNode =
          new DefaultMutableTreeNode(nodeWrapperFactory.createSimpleNodeWrapper(
              (new java.text.MessageFormat(Resources.get("factionstatspanel.node.income")))
                  .format(msgArgs), "income"));
      rootNode.add(currentNode);
    }

    for (int i = 0; i < earned.length; i++) {
      // income by work, entertain, tax, theft, trade
      if ((earned[i] != 0) || (wanted[i] != 0)) {
        Object msgArgs[] = { Long.valueOf(earned[i]) };
        StringBuffer sb = new StringBuffer();
        sb.append(new java.text.MessageFormat(Resources.get("factionstatspanel.node.income" + i))
            .format(msgArgs));

        if (earned[i] != wanted[i]) {
          msgArgs = new Object[] { Long.valueOf(wanted[i]) };
          sb.append(" ");
          sb.append((new java.text.MessageFormat(Resources
              .get("factionstatspanel.node.incomewanted"))).format(msgArgs));
        }

        subNode =
            new DefaultMutableTreeNode(nodeWrapperFactory.createSimpleNodeWrapper(sb.toString(),
                incomeGroupIcon[i]));
        currentNode.add(subNode);
      }
      if (i == E_TRADETAX && spent[S_TRADE] != 0) {
        // already included in "spent", below
        // // insert extra node for trade expenses
        // Object msgArgs[] = { Integer.valueOf(-spent[S_TRADE]) };
        // String s =
        // (new java.text.MessageFormat(Resources.get("factionstatspanel.node.spentfortrade"))
        // .format(msgArgs));
        // subNode =
        // new DefaultMutableTreeNode(nodeWrapperFactory.createSimpleNodeWrapper(s, "Handeln"));
        // currentNode.add(subNode);
      }

    }
    for (int i = 0; i < extraEarned.length; i++) {
      // income by alms, transfers
      if (extraEarned[i] != 0) {
        Object msgArgs[] = { Long.valueOf(extraEarned[i]) };
        StringBuffer sb = new StringBuffer();
        sb.append(new java.text.MessageFormat(Resources.get("factionstatspanel.node.income"
            + (earned.length + i))).format(msgArgs));

        subNode =
            new DefaultMutableTreeNode(nodeWrapperFactory.createSimpleNodeWrapper(sb.toString(),
                incomeGroupIcon2[i]));
        currentNode.add(subNode);
        if (i == EE_ALMS) {
          // Almosen nach Factions UND Regions
          if (factionAlmsGotten.size() > 0) {
            for (ID actFID : factionAlmsGotten.keySet()) {
              Long alms = factionAlmsGotten.get(actFID);
              Faction curFaction = getGameData().getFaction(actFID);
              if (alms != null) {
                // node für diese Faction hinzu
                DefaultMutableTreeNode subSubNode =
                    new DefaultMutableTreeNode(nodeWrapperFactory.createSimpleNodeWrapper(
                        curFaction == null ? Resources.get("emapdetailspanel.node.unknownfaction")
                            : curFaction.getName() + ": "
                                + NumberFormat.getNumberInstance().format(alms.longValue()),
                        "Silber"));
                subNode.add(subSubNode);
                // regions für diese Faction dazu
                if (curFaction != null) {
                  Map<Region, Long> factionAlms = almGottenRegions.get(curFaction.getID());
                  if (factionAlms != null && factionAlms.size() > 0) {
                    for (Region curRegion : factionAlms.keySet()) {
                      alms = factionAlms.get(curRegion);
                      if (alms != null && alms.longValue() > 0) {
                        DefaultMutableTreeNode subSubSubNode =
                            new DefaultMutableTreeNode(nodeWrapperFactory.createRegionNodeWrapper(
                                curRegion, alms.longValue()));
                        subSubNode.add(subSubSubNode);
                      }
                    }
                  }
                }
              }
            }
          }

        }

        if (i == EE_TRANSFERS) {
          // Übergaben
          if (factionGettings.size() > 0) {
            for (ID fID : factionGettings.keySet()) {
              Long actV = factionGettings.get(fID);
              Faction actTF = getGameData().getFaction(fID);
              if (actV != null && actV.longValue() > 0) {
                DefaultMutableTreeNode subSubNode =
                    new DefaultMutableTreeNode(nodeWrapperFactory.createSimpleNodeWrapper(
                        actTF == null ? Resources.get("emapdetailspanel.node.unknownfaction")
                            : actTF.getName() + ": "
                                + NumberFormat.getNumberInstance().format(actV.longValue()),
                        "Silber"));
                subNode.add(subSubNode);
              }
            }
          }
        }
      }
    }

    // iconnames for expense groups
    // 0 = Unterhalt (persons * maintenance, if known)
    // 1 = Gebäudeunterhalt
    // 2 = Lernkosten
    // 3 = Magiekosten (no chance: Messages contains just the magic item, no
    // cost. No chance to find the spell for sure..)
    // 4 = Handel
    // 5 = Diebstahl
    // 6 = Almosen
    // 7 = Übergaben an andere Parteien
    String spentGroupIcon[] =
        new String[] { "Persons", "Buildingcost", "Skills", "Magie", "Handeln", "Tarnung",
            "Alliance", "Persons" };

    if (totalSpent != 0) {
      currentNode =
          new DefaultMutableTreeNode(nodeWrapperFactory.createSimpleNodeWrapper(Resources
              .get("factionstatspanel.node.spent")
              + ": " + NumberFormat.getNumberInstance().format(totalSpent), "income"));
      rootNode.add(currentNode);
      for (int i = 0; i < spent.length; i++) {
        if (spent[i] != 0) {
          subNode =
              new DefaultMutableTreeNode(nodeWrapperFactory.createSimpleNodeWrapper(Resources
                  .get("factionstatspanel.node.spent" + i)
                  + ": " + NumberFormat.getNumberInstance().format(spent[i]), spentGroupIcon[i]));
          currentNode.add(subNode);
          // specials...
          if (i == S_UPKEEP) {
            // buildings after Type
            if (buildingUpkeep.size() > 0) {
              for (StringID btID : buildingUpkeep.keySet()) {
                Long upkeep = buildingUpkeep.get(btID);
                ConstructibleType bT = getGameData().getRules().getBuildingType(btID);
                if (bT != null && upkeep != null && upkeep.longValue() > 0) {
                  DefaultMutableTreeNode subSubNode =
                      new DefaultMutableTreeNode(nodeWrapperFactory.createSimpleNodeWrapper(bT
                          .getName()
                          + ": " + NumberFormat.getNumberInstance().format(upkeep.longValue()), bT
                              .getName()));
                  subNode.add(subSubNode);
                }
              }
            }
          } else if (i == S_ALMS) {
            // Almosen nach Factions UND Regions
            if (factionAlmsGiven.size() > 0) {
              for (ID actFID : factionAlmsGiven.keySet()) {
                Long alms = factionAlmsGiven.get(actFID);
                Faction curFaction = getGameData().getFaction(actFID);
                if (alms != null) {
                  // node für diese Faction hinzu
                  DefaultMutableTreeNode subSubNode =
                      new DefaultMutableTreeNode(nodeWrapperFactory.createSimpleNodeWrapper(
                          curFaction == null ? Resources
                              .get("emapdetailspanel.node.unknownfaction") : curFaction.getName()
                                  + ": " + NumberFormat.getNumberInstance().format(alms.longValue()),
                          "Silber"));
                  subNode.add(subSubNode);
                  // regions für diese Faction dazu
                  if (curFaction != null) {
                    Map<Region, Long> actRM = almGivenRegions.get(curFaction.getID());
                    if (actRM != null && actRM.size() > 0) {
                      for (Region actRR : actRM.keySet()) {
                        alms = actRM.get(actRR);
                        if (alms != null && alms.longValue() > 0) {
                          DefaultMutableTreeNode subSubSubNode =
                              new DefaultMutableTreeNode(nodeWrapperFactory
                                  .createRegionNodeWrapper(actRR, alms.longValue()));
                          subSubNode.add(subSubSubNode);
                        }
                      }
                    }
                  }
                }
              }
            }
          } else if (i == S_TRANSFERS) {
            // Übergaben
            if (factionGivings.size() > 0) {
              for (ID fID : factionGivings.keySet()) {
                Long givings = factionGivings.get(fID);
                Faction actTF = getGameData().getFaction(fID);
                if (actTF != null && givings != null && givings.longValue() > 0) {
                  DefaultMutableTreeNode subSubNode =
                      new DefaultMutableTreeNode(nodeWrapperFactory.createSimpleNodeWrapper(actTF
                          .getName()
                          + ": " + NumberFormat.getNumberInstance().format(givings.longValue()),
                          "Silber"));
                  subNode.add(subSubNode);
                }
              }
            }
          }
        }
      }
    }

    // show items and clear categories
    unitsTools.addCategorizedUnitItems(units.values(), rootNode, null, null, true,
        nodeWrapperFactory, null);

    // add buildings
    // maps BuildingTypes to a List, containing the single buildings
    Map<UnitContainerType, List<Building>> buildingsCounter =
        new Hashtable<UnitContainerType, List<Building>>();

    // collect the buildings
    for (Region r : regions.values()) {
      for (Building building : r.buildings()) {
        if (isOwned(building, r, factions)) {
          if (!buildingsCounter.containsKey(building.getType())) {
            buildingsCounter.put(building.getType(), new LinkedList<Building>());
          }

          buildingsCounter.get(building.getType()).add(building);
        }
      }
    }

    // add the buildings to the tree
    if (buildingsCounter.keySet().size() > 0) {
      // n = new
      // DefaultMutableTreeNode(Resources.get("factionstatspanel.node.buildings"));

      currentNode =
          new DefaultMutableTreeNode(nodeWrapperFactory.createSimpleNodeWrapper(Resources
              .get("factionstatspanel.node.buildings"), "buildings"));
      rootNode.add(currentNode);
    }

    for (UnitContainerType buildingType : buildingsCounter.keySet()) {
      String buildingIconName = StringFactory.getFactory().intern(buildingType.getID().toString());
      subNode =
          new DefaultMutableTreeNode(nodeWrapperFactory.createSimpleNodeWrapper(buildingType
              .getName()
              + ": " + (buildingsCounter.get(buildingType)).size(), buildingIconName));

      currentNode.add(subNode);

      for (Building building : (buildingsCounter.get(buildingType))) {
        UnitContainerNodeWrapper uc = nodeWrapperFactory.createUnitContainerNodeWrapper(building);
        subNode.add(new DefaultMutableTreeNode(uc));
      }
    }

    // add ships
    // maps ShipTypes to Lists, containing the single ships
    Map<UnitContainerType, List<Ship>> shipsCounter =
        new Hashtable<UnitContainerType, List<Ship>>();

    // collect the ships
    for (Region r : regions.values()) {
      for (Ship ship : r.ships()) {
        if (isOwned(ship, r, factions)) {
          if (!shipsCounter.containsKey(ship.getType())) {
            shipsCounter.put(ship.getType(), new LinkedList<Ship>());
          }

          shipsCounter.get(ship.getType()).add(ship);
        }
      }
    }

    // add the ships to the tree
    if (shipsCounter.keySet().size() > 0) {
      // n = new
      // DefaultMutableTreeNode(Resources.get("factionstatspanel.node.ships"));
      currentNode =
          new DefaultMutableTreeNode(nodeWrapperFactory.createSimpleNodeWrapper(Resources
              .get("factionstatspanel.node.ships"), "ships"));
      rootNode.add(currentNode);
    }

    for (UnitContainerType shipType : shipsCounter.keySet()) {
      /**
       * Fiete 20060911 m = new DefaultMutableTreeNode(shipType.getName() + ": " + ((List)
       * shipsCounter.get(shipType)).size());
       */
      // Fiete 20060911: added support for shiptypeicons
      // Fiete 20060915: get rid of english icon names...using stringfactory to
      // get the orginal names
      String shipIconName = StringFactory.getFactory().intern(shipType.getID().toString());
      int shipCount = 0, convoyCount = 0;
      for (Ship ship : shipsCounter.get(shipType)) {
        shipCount += ship.getAmount();
        if (ship.getAmount() > 1) {
          convoyCount++;
        }
      }
      if (convoyCount > 0 && shipsCounter.get(shipType).size() > 1) {
        subNode =
            new DefaultMutableTreeNode(nodeWrapperFactory.createSimpleNodeWrapper(shipType.getName()
                + ": " + Resources.get("factionstatspanel.node.shipcount",
                    shipCount, shipsCounter.get(shipType).size(), convoyCount, shipsCounter.get(shipType).size()
                        - convoyCount), shipIconName));
      } else {
        subNode = new DefaultMutableTreeNode(nodeWrapperFactory.createSimpleNodeWrapper(shipType.getName()
            + ": " + shipCount, shipIconName));
      }

      currentNode.add(subNode);

      for (Ship ship : (shipsCounter.get(shipType))) {
        UnitContainerNodeWrapper uc = nodeWrapperFactory.createUnitContainerNodeWrapper(ship);
        subNode.add(new DefaultMutableTreeNode(uc));
      }
    }

    // add skills
    List<SkillType> sortedSkillTypes = skillStats.getKnownSkillTypes();

    if (sortedSkillTypes.size() > 0) {
      // n = new
      // DefaultMutableTreeNode(Resources.get("factionstatspanel.node.skills"));
      currentNode =
          new DefaultMutableTreeNode(nodeWrapperFactory.createSimpleNodeWrapper(Resources
              .get("factionstatspanel.node.skills"), "skills"));
      rootNode.add(currentNode);

      for (SkillType type : sortedSkillTypes) {
        List<Skill> sortedSkills = skillStats.getKnownSkills(type);

        for (Skill skill : sortedSkills) {
          subNode =
              new DefaultMutableTreeNode(new SimpleNodeWrapper(type.getName() + " T"
                  + skill.getLevel() + ": " + skillStats.getPersonNumber(skill), type.getName()));
          currentNode.add(subNode);

          List<Unit> unitList = skillStats.getUnits(skill);

          // now sort the units and add them as nodes...
          Comparator<Unique> idCmp = IDComparator.DEFAULT;
          // TODO(stm) using SpecifiedSkillTypeSkillComp here seems pretty pointless, as all units
          // have the same value in the specified skill
          Comparator<Unit> unitCmp =
              new UnitSkillComparator(new SpecifiedSkillTypeSkillComparator(type,
                  new SkillComparator(), null), idCmp);
          Collections.sort(unitList, unitCmp);

          for (Unit u : unitList) {
            String text = u.toString();

            if (!getGameData().noSkillPoints) {
              int bonus = u.getRace().getSkillBonus(type);
              int currentDays = u.getSkill(type).getPointsPerPerson();
              int nextLevelDays = Skill.getPointsAtLevel(skill.getLevel() - bonus + 1);
              int pointsToLearn = nextLevelDays - currentDays;
              int turnsToLearn = pointsToLearn / 30;

              if ((pointsToLearn % 30) > 0) {
                turnsToLearn++;
              }

              text +=
                  (": " + " [" + currentDays + " -> " + nextLevelDays + " {" + turnsToLearn
                      + "}], " + u.getPersons());

            } else {
              text += ": " + u.getPersons();
            }

            UnitNodeWrapper w = nodeWrapperFactory.createUnitNodeWrapper(u, text);
            subNode.add(new DefaultMutableTreeNode(w));
          }
        }
      }
    }

    // Add production
    // Mapping ItemCategory to Hashtable that maps resource (String) to
    // ProductionStats-Objects
    Map<ItemCategory, Map<String, ProductionStats>> production =
        new Hashtable<ItemCategory, Map<String, ProductionStats>>();

    for (Faction faction : factions.values()) {
      if (faction.getMessages() != null) {
        for (Message msg : faction.getMessages()) {
          int msgID = (msg.getMessageType().getID()).intValue();

          // check whether the message belongs to one of the selected regions
          {
            String regionCoordinate = msg.getAttribute("region");

            if (regionCoordinate != null) {
              ID coordinate = CoordinateID.parse(regionCoordinate, ",");

              if (coordinate == null) {
                coordinate = CoordinateID.parse(regionCoordinate, " ");
              }

              if (!regions.containsKey(coordinate)) {
                continue;
              }
            }

            // find a valid amount
            if ((msg.getMessageType() != null) && (msg.getMessageType().getSection() != null)
                && msg.getMessageType().getSection().equalsIgnoreCase("production")) {
              String value = msg.getAttribute("amount");
              long amount = 0;

              if (value != null) {
                amount = Integer.parseInt(value);
              }

              if (amount != 0) {
                String resource = msg.getAttribute("resource");

                // get the resource
                if (resource == null) {
                  // possible a message containing ;herb instead of ;resource
                  resource = msg.getAttribute("herb");
                }

                if (resource == null) {
                  // In Pferdezucht gezüchtete Pferde haben keine ressource
                  // Fiete: 20080521
                  if (msgID == 687207561) {
                    resource = "Pferd";
                  }
                }

                if (resource != null) {
                  // find the category
                  ItemType itemType =
                      getGameData().getRules().getItemType(StringID.create(resource));
                  ItemCategory itemCategory = null;

                  if (itemType != null) {
                    itemCategory = itemType.getCategory();
                  }

                  if (itemCategory == null) {
                    FactionStatsPanel.log.info("Item without category: " + resource);
                    itemCategory = getGameData().getRules().getItemCategory("misc");
                  }
                  if (itemCategory != null) {
                    // add the data
                    Map<String, ProductionStats> h = production.get(itemCategory);

                    if (h == null) {
                      h = new Hashtable<String, ProductionStats>();
                      production.put(itemCategory, h);
                    }

                    ProductionStats p = h.get(resource);

                    if (p == null) {
                      p = new ProductionStats();
                      h.put(resource, p);
                    }

                    p.totalAmount += amount;

                    // try to get the unit that did the production
                    value = msg.getAttribute("unit");

                    if (value != null) {
                      int number = Integer.parseInt(value);

                      UnitID id = UnitID.createUnitID(number, getGameData().base);
                      Unit u = getGameData().getUnit(id);

                      if (u != null) {
                        p.units.put(u, amount);
                      }
                    }
                  }
                }
              }
            }
          }
        }
      }
    }

    // remember: production maps ItemCategory to Map that maps resource (String)
    // to ProductionStats-Objects
    // DefaultMutableTreeNode prodNode = new
    // DefaultMutableTreeNode(Resources.get("factionstatspanel.node.production"));
    DefaultMutableTreeNode prodNode =
        new DefaultMutableTreeNode(nodeWrapperFactory.createSimpleNodeWrapper(Resources
            .get("factionstatspanel.node.production"), "production"));

    for (ItemCategory iCategory : production.keySet()) {
      ArrayList<DefaultMutableTreeNode> subNodeChildList = new ArrayList<DefaultMutableTreeNode>();

      String catIconName = magellan.library.utils.Umlaut.convertUmlauts(iCategory.getName());
      String nodeName = Resources.get("factionstatspanel." + catIconName);

      Map<String, ProductionStats> h = production.get(iCategory);
      long totalAmount = 0;

      for (String resource : h.keySet()) {
        ProductionStats stats = h.get(resource);
        totalAmount += stats.totalAmount;
        DefaultMutableTreeNode o = null;
        // o = new DefaultMutableTreeNode(resource + ": " + stats.totalAmount);
        if (catIconName.equalsIgnoreCase("kraeuter")) {
          o =
              createSimpleNode(getGameData().getTranslation(resource) + ": " + stats.totalAmount,
                  "items/" + "kraeuter");
        } else {
          o =
              createSimpleNode(getGameData().getTranslation(resource) + ": " + stats.totalAmount,
                  "items/" + resource);
        }

        // subNode.add(o);
        subNodeChildList.add(o);
        for (Unit u : stats.units.keySet()) {
          long amount = stats.units.get(u).longValue();
          o.add(new DefaultMutableTreeNode(nodeWrapperFactory.createUnitNodeWrapper(u, amount)));
        }
      }
      if (subNodeChildList.size() > 0) {
        subNode = createSimpleNode(nodeName + ":" + totalAmount, catIconName);
        for (DefaultMutableTreeNode defaultMutableTreeNode : subNodeChildList) {
          subNode.add(defaultMutableTreeNode);
        }
        prodNode.add(subNode);
      }

    }

    if (prodNode.getChildCount() > 0) {
      rootNode.add(prodNode);
    }

    // Fiete: Displaying Faction-Items
    // only if one faction is selected
    if (factions.size() == 1) {
      Faction f = factions.values().iterator().next();
      if (f.getItems() != null) {
        if (f.getItems().size() > 0) {
          DefaultMutableTreeNode factionPoolNode =
              new DefaultMutableTreeNode(nodeWrapperFactory.createSimpleNodeWrapper(Resources
                  .get("factionstatspanel.node.factionpool"), "factionpool"));
          for (Item actItem : f.getItems()) {
            ItemNodeWrapper itemNodeWrapper = nodeWrapperFactory.createItemNodeWrapper(actItem);
            DefaultMutableTreeNode itemNode = new DefaultMutableTreeNode(itemNodeWrapper);
            factionPoolNode.add(itemNode);
          }
          rootNode.add(factionPoolNode);
        }
      }
    }

    treeModel.reload();
    setCursor(Cursor.getDefaultCursor());
  }

  private boolean isOwned(UnitContainer container, Region region, Map<magellan.library.ID, Faction> friendlies) {
    Unit owner = container.getOwnerUnit() != null ? container.getOwnerUnit() : region.getOwnerUnit();
    if (owner == null) {
      for (Unit u : region.units()) {
        if (u.getFaction() != null && friendlies.containsKey(u.getFaction().getID())) {
          owner = u;
          break;
        }
      }
    }
    return owner != null && friendlies.containsKey(owner.getFaction().getID());
  }

  private <ID> void increase(Map<ID, Long> intMap, ID id, long delta) {
    Long value = intMap.get(id);
    if (value == null) {
      intMap.put(id, delta);
    } else {
      intMap.put(id, value + delta);
    }

  }

  public static void showAlliances(GameData data, Faction owner, Map<EntityID, Alliance> allies,
      AllianceGroup allianceGroup, DefaultMutableTreeNode rootNode) {
    if (rootNode == null)
      return;

    if (allianceGroup != null) {
      DefaultMutableTreeNode m = new DefaultMutableTreeNode(allianceGroup.toString());
      rootNode.add(m);

      Faction leader = data.getFaction(allianceGroup.getLeader());
      if (leader == null) {
        // probably just a stray selection event...
        FactionStatsPanel.log.warn("alliance without leader: " + allianceGroup);
      } else {
        FactionNodeWrapper wrapper =
            new FactionNodeWrapper(leader, null, Collections.<EntityID, Alliance> emptyMap());
        DefaultMutableTreeNode factionNode = new DefaultMutableTreeNode(wrapper);
        m.add(factionNode);
      }
      for (ID id : allianceGroup.getFactions()) {
        Faction faction = data.getFaction(id);
        if (faction == null) {
          FactionStatsPanel.log.warn("unknown faction in alliance: " + id);
        } else if (faction != leader) {
          FactionNodeWrapper wrapper =
              new FactionNodeWrapper(faction, null, Collections.<EntityID, Alliance> emptyMap());
          DefaultMutableTreeNode factionNode = new DefaultMutableTreeNode(wrapper);
          m.add(factionNode);
        }
      }
    }

    if (allies == null)
      return;

    Map<String, List<Alliance>> alliances = new TreeMap<String, List<Alliance>>();
    for (Alliance alliance : allies.values()) {
      String key = alliance.stateToString();
      if (alliances.containsKey(key)) {
        alliances.get(key).add(alliance);
      } else {
        List<Alliance> list = new LinkedList<Alliance>();
        list.add(alliance);
        alliances.put(key, list);
      }
    }
    for (String key : alliances.keySet()) {
      List<Alliance> alliance = alliances.get(key);
      Collections.sort(alliance, new AllianceFactionComparator(new NameComparator(
          IDComparator.DEFAULT)));

      DefaultMutableTreeNode m =
          new DefaultMutableTreeNode(Resources.get("emapdetailspanel.alliancestate",
              new Object[] { key }));
      for (Alliance a : alliance) {
        FactionNodeWrapper f = new FactionNodeWrapper(a.getFaction(), null, allies);
        DefaultMutableTreeNode o = new DefaultMutableTreeNode(f);
        m.add(o);
      }

      rootNode.add(m);
    }
  }

  private Container getStatPanel() {
    rootNode = new DefaultMutableTreeNode(Resources.get("factionstatspanel.node.rootName"));
    treeModel = new DefaultTreeModel(rootNode);
    tree = new CopyTree(treeModel);
    // tree.setRootVisible(true);
    tree.setRootVisible(false);

    tree.addTreeSelectionListener(this);
    tree.setShowsRootHandles(true);
    CellRenderer tr = new CellRenderer(getMagellanContext());
    tree.setCellRenderer(tr);

    JScrollPane treeScrollPane =
        new JScrollPane(tree, ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS,
            ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
    treeScrollPane.setMinimumSize(new Dimension(100, 50));

    JPanel stats = new JPanel(new GridLayout(1, 1));
    stats.add(treeScrollPane);

    return stats;
  }

  /**
   * Returns a simple node
   */
  private DefaultMutableTreeNode createSimpleNode(Object obj, String icons) {
    return new DefaultMutableTreeNode(nodeWrapperFactory.createSimpleNodeWrapper(obj, icons));
  }

  /**
   * A little class used to store information about production statistics for a certain resource
   */
  private static class ProductionStats {
    // mapping units who produced the special resource (Unit) to the according
    // amounts (Integer)
    private Map<Unit, Long> units = new Hashtable<Unit, Long>();

    // total amount produced by all units
    private long totalAmount;
  }

}
