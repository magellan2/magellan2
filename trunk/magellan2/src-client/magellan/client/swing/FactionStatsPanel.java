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
import java.awt.Container;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
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
import magellan.library.Building;
import magellan.library.CoordinateID;
import magellan.library.EntityID;
import magellan.library.Faction;
import magellan.library.GameData;
import magellan.library.Group;
import magellan.library.ID;
import magellan.library.IntegerID;
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
import magellan.library.rules.BuildingType;
import magellan.library.rules.ItemCategory;
import magellan.library.rules.ItemType;
import magellan.library.rules.Race;
import magellan.library.rules.SkillType;
import magellan.library.rules.UnitContainerType;
import magellan.library.utils.MagellanFactory;
import magellan.library.utils.OrderedHashtable;
import magellan.library.utils.PropertiesHelper;
import magellan.library.utils.Resources;
import magellan.library.utils.SkillStats;
import magellan.library.utils.StringFactory;
import magellan.library.utils.comparator.AllianceFactionComparator;
import magellan.library.utils.comparator.FactionTrustComparator;
import magellan.library.utils.comparator.IDComparator;
import magellan.library.utils.comparator.NameComparator;
import magellan.library.utils.comparator.SkillComparator;
import magellan.library.utils.comparator.SpecifiedSkillTypeSkillComparator;
import magellan.library.utils.comparator.UnitSkillComparator;
import magellan.library.utils.logging.Logger;

/**
 * A panel for showing statistics about factions.
 */
public class FactionStatsPanel extends InternationalizedDataPanel implements SelectionListener, TreeSelectionListener {
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
    this.setLayout(new BorderLayout());
    this.add(getStatPanel(), BorderLayout.CENTER);
    factions = new Hashtable<ID, Faction>(data.factions());
    regions = new Hashtable<CoordinateID, Region>(data.regions());
    dispatcher.addSelectionListener(this);
    unitsTools = (data != null) ? new Units(data.rules) : new Units(null);
    nodeWrapperFactory = new NodeWrapperFactory(settings);
    
    // to get the pref-adapter
    Unit temp = MagellanFactory.createUnit(UnitID.createUnitID(0, data.base));
    nodeWrapperFactory.createUnitNodeWrapper(temp);
    nodeWrapperFactory.createSkillNodeWrapper(temp, new Skill(new SkillType(StringID.create("Test")), 0, 0, 0, false), null);
    nodeWrapperFactory.createItemNodeWrapper(new Item(new ItemType(StringID.create("Test")), 0));
  }

  /**
   * @see magellan.client.swing.InternationalizedDataPanel#gameDataChanged(magellan.library.event.GameDataEvent)
   */
  @Override
  public void gameDataChanged(GameDataEvent e) {
    data = e.getGameData();

    /**
     * Don't clear factions as the SelectionEvent of the updated List in
     * FactionStatsDialog might be processed befor the GameDataEvent
     */

    // factions.clear();
    if (data != null) {
      unitsTools.setRules(data.rules);
      setRegions(data.regions().values());
    }
  }

  /**
   * @see magellan.client.event.SelectionListener#selectionChanged(magellan.client.event.SelectionEvent)
   */
  public void selectionChanged(SelectionEvent e) {
    if (e.getSource() == this) {
      return;
    }

    if ((e.getSelectedObjects() != null) && (e.getSelectionType() == SelectionEvent.ST_REGIONS)) {
      List<Region> regions = new LinkedList<Region>();

      for (Iterator iter = e.getSelectedObjects().iterator(); iter.hasNext();) {
        Object o = iter.next();

        if (o instanceof Region) {
          regions.add((Region) o);
        }
      }

      /**
       * Ulrich Küster: (!) Special care has to be taken. Generelly it can not
       * be differed, if SelectionEvents come from the faction list in
       * FactionStatsDialog or from other components of Magellan (except if a
       * special SelectionType has been defined in SelectionEvent and is used).
       * To keep the faction list in FactionStatsDialog consistent to the
       * displayed data in this FactionStatsPanel object, setFaction() should be
       * _never_ called by this selectionChanged()-method, but directly by the
       * valueChanged()-method of FactionStatsDialog.
       */
      setRegions(regions);
    }
  }

  /**
   * DOCUMENT-ME
   */
  public void setFactions(Collection<Faction> fs) {
    factions.clear();

    if (fs != null) {
      for (Iterator<Faction> iter = fs.iterator(); iter.hasNext();) {
        Faction f = iter.next();
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
      regions = new Hashtable<CoordinateID, Region>(data.regions());
    } else {
      regions.clear();

      for (Iterator<Region> iter = rs.iterator(); iter.hasNext();) {
        Region r = iter.next();
        regions.put((CoordinateID) r.getID(), r);
      }
    }

    updateTree();
  }

  /**
   * @see javax.swing.event.TreeSelectionListener#valueChanged(javax.swing.event.TreeSelectionEvent)
   */
  public void valueChanged(TreeSelectionEvent e) {
    DefaultMutableTreeNode node = (DefaultMutableTreeNode) tree.getLastSelectedPathComponent();

    if (node == null) {
      return;
    }

    Object o = node.getUserObject();

    if (o instanceof UnitNodeWrapper) {
      Unit u = ((UnitNodeWrapper) o).getUnit();
      dispatcher.fire(new SelectionEvent<Unit>(this, null, u));
    } else if (o instanceof UnitContainerNodeWrapper) {
      UnitContainer uc = ((UnitContainerNodeWrapper) o).getUnitContainer();
      dispatcher.fire(new SelectionEvent<UnitContainer>(this, null, uc));
    } else if (o instanceof RegionNodeWrapper){
      Region r = ((RegionNodeWrapper) o).getRegion();
      dispatcher.fire(new SelectionEvent<Region>(this, null, r));
    }
  }

  /*
   * Ilja Pavkovic 2001.10.19: I wanted to see the original values and the
   * predictions of units and persons.
   */

  /**
   * Ulrich Küster: The algorithm wasn't correct as it is wrong to count the
   * number of persons in the temp units to get the number of recruited persons.
   * a) Temp units always have zero persons b) 'normal' units can recruit
   * persons So it is necessary to look at all unit's RecruitmentRelations in
   * the units's cache-object. I changed that. (Of course this doesn't consider,
   * that persons can be given to '0'.)
   */
  private void updateTree() {
    tree.setShowsRootHandles(PropertiesHelper.getBoolean(settings, "EMapOverviewPanel.treeRootHandles", true));

    setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));

    DefaultMutableTreeNode currentNode = null;
    DefaultMutableTreeNode subNode = null;
    Map<ID, Unit> units = new Hashtable<ID, Unit>();
    int personCounter = 0;
    int modifiedUnitsCounter = 0;
    int tempUnitsCounter = 0;
    int modifiedPersonCounter = 0;
    rootNode.removeAllChildren();

    /**
     * Used to collect persons of different race than their faction. Key: String
     * (racename), Value: List containing the units
     */
    Map<String, List<Unit>> specialPersons = new OrderedHashtable<String, List<Unit>>();
    Collection<Unit> heroes = new LinkedList<Unit>();
    int heros_count = 0;

    SkillStats skillStats = new SkillStats();

    for (Iterator<Region> iter = regions.values().iterator(); iter.hasNext();) {
      Region r = iter.next();

      /**
       * poorly it is necessary to refresh all relations, as at this time it is
       * not assured that they are always up to date. Possibly it would be
       * better, to calculate them only, if orders are loaded or changed...
       */
      r.refreshUnitRelations();

      for (Iterator<Unit> it = r.units().iterator(); it.hasNext();) {
        Unit u = it.next();

        if (factions.containsKey(u.getFaction().getID())) {
          units.put(u.getID(), u);
          personCounter += u.getPersons();
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
     * Fiete 20060918: made all nodes with icons, thx to khadar for icons n =
     * new
     * DefaultMutableTreeNode(Resources.get("factionstatspanel.node.units") +
     * (units.size() - tempUnitsCounter) + " (" + modifiedUnitsCounter + ")");
     */
    currentNode = new DefaultMutableTreeNode(nodeWrapperFactory.createSimpleNodeWrapper(Resources.get("factionstatspanel.node.units") + (units.size() - tempUnitsCounter) + " (" + modifiedUnitsCounter + ")", "units"));

    rootNode.add(currentNode);
    /**
     * n = new
     * DefaultMutableTreeNode(Resources.get("factionstatspanel.node.persons") +
     * personCounter + " (" + modifiedPersonCounter + ")");
     */
    currentNode = new DefaultMutableTreeNode(nodeWrapperFactory.createSimpleNodeWrapper(Resources.get("factionstatspanel.node.persons") + personCounter + " (" + modifiedPersonCounter + ")", "persons"));

    rootNode.add(currentNode);

    if (factions.size() == 1) {
      Faction f = null;
      f = factions.values().iterator().next();
      
      /* Race node */
      if (f.getType() != null) {
        // n = new
        // DefaultMutableTreeNode(Resources.get("factionstatspanel.node.race")
        // + f.getType().getName());
        currentNode = new DefaultMutableTreeNode(nodeWrapperFactory.createSimpleNodeWrapper(Resources.get("factionstatspanel.node.race") + f.getType().getName(), "race"));
        rootNode.add(currentNode);
      }

      /* Age node */
      if (f.getAge() > -1) {
        currentNode = new DefaultMutableTreeNode(nodeWrapperFactory.createSimpleNodeWrapper(Resources.get("factionstatspanel.node.age") + ": " + f.getAge(), "age"));
        rootNode.add(currentNode);
      }
      
      /* Locale node */
      if (f.getLocale() != null) {
        currentNode = new DefaultMutableTreeNode(nodeWrapperFactory.createSimpleNodeWrapper(Resources.get("factionstatspanel.node.locale") + ": " + f.getLocale(), "locale"));
        rootNode.add(currentNode);
      }

      /* Magic node */
      if (f.getSpellSchool() != null) {
        // n = new
        // DefaultMutableTreeNode(Resources.get("factionstatspanel.node.magicschool")
        // + f.spellSchool);
        currentNode = new DefaultMutableTreeNode(nodeWrapperFactory.createSimpleNodeWrapper(Resources.get("factionstatspanel.node.magicschool") + f.getSpellSchool(), "magicschool"));
        rootNode.add(currentNode);
      }

      /* Description node */
      String description = f.getDescription();
      if ((description != null) && (description.length() > 0)) {
        // n = new
        // DefaultMutableTreeNode(Resources.get("factionstatspanel.node.banner")
        // + description);
        currentNode = new DefaultMutableTreeNode(nodeWrapperFactory.createSimpleNodeWrapper(Resources.get("factionstatspanel.node.banner") + description, "banner"));
        rootNode.add(currentNode);
      }

      /* Email node */
      if (f.getEmail() != null) {
        // n = new DefaultMutableTreeNode(new
        // SimpleNodeWrapper(Resources.get("factionstatspanel.node.e-mail")
        // + f.email, f.email));
        // added email icon
        currentNode = new DefaultMutableTreeNode(new SimpleNodeWrapper(Resources.get("factionstatspanel.node.e-mail") + f.getEmail(), "email2"));

        rootNode.add(currentNode);
      }

      { /* Trustlevel node */
      String nodeLabel = FactionTrustComparator.getTrustLevelLabel(FactionTrustComparator.getTrustLevel(f.getTrustLevel()));
      nodeLabel += (" (" + f.getTrustLevel() + ")");
      // n = new DefaultMutableTreeNode(nodeLabel);
      currentNode = new DefaultMutableTreeNode(new SimpleNodeWrapper(nodeLabel, "trust"));
      rootNode.add(currentNode);
      }
      
      /* Report owner node */
      if (f.getID().equals(data.getOwnerFaction())){
        currentNode = new DefaultMutableTreeNode(new SimpleNodeWrapper(Resources.get("factionstatspanel.node.reportowner"), "reportowner"));
        rootNode.add(currentNode);
      }
      
      /* Translation node */
      if (f.getID() instanceof EntityID) {
        Map<Integer, CoordinateID> map = data.getCoordinateTranslationMap((EntityID)f.getID());
        if (map!=null){
          currentNode = new DefaultMutableTreeNode(nodeWrapperFactory.createSimpleNodeWrapper(Resources.get("factionstatspanel.node.translations"), "coordinatetranslation"));
          SortedSet<Integer> layers = new TreeSet<Integer>(map.keySet());
          for (Integer i: layers){
            CoordinateID translation = data.getCoordinateTranslation((EntityID) f.getID(), i);
            DefaultMutableTreeNode translationNode = new DefaultMutableTreeNode(nodeWrapperFactory.createSimpleNodeWrapper((new java.text.MessageFormat(Resources.get("factionstatspanel.node.layer"))).format(new Integer [] { i })+" "+translation, "layer"));
            currentNode.add(translationNode);
          }
          rootNode.add(currentNode);
        }
      } else {
        FactionStatsPanel.log.warn("faction ID is not EntityID");
      }
      
      /* score node */
      if (f.getScore() > 0) {
        /**
         * n = new
         * DefaultMutableTreeNode(Resources.get("factionstatspanel.node.score") +
         * f.score + "/" + f.averageScore + " (" + (int) ((100.0 * f.score) /
         * f.averageScore) + "%)");
         */
        String scoreLabel = Resources.get("factionstatspanel.node.score") + f.getScore() + "/" + f.getAverageScore() + " (" + (int) ((100.0 * f.getScore()) / f.getAverageScore()) + "%)";
        currentNode = new DefaultMutableTreeNode(nodeWrapperFactory.createSimpleNodeWrapper(scoreLabel, "score"));

        rootNode.add(currentNode);
      }

      /* prefix node */
      if (f.getRaceNamePrefix() != null) {
        /**
         * n = new
         * DefaultMutableTreeNode(Resources.get("factionstatspanel.node.racenameprefix") +
         * f.getRaceNamePrefix());
         */
        currentNode = new DefaultMutableTreeNode(nodeWrapperFactory.createSimpleNodeWrapper(Resources.get("factionstatspanel.node.racenameprefix") + f.getRaceNamePrefix(), "prefix"));
        rootNode.add(currentNode);
      }

      /* migrants node */
      if (f.getMigrants() > 0) {
        /**
         * n = new
         * DefaultMutableTreeNode(Resources.get("factionstatspanel.node.migrants") +
         * f.migrants + "/" + f.maxMigrants);
         */
        currentNode = new DefaultMutableTreeNode(nodeWrapperFactory.createSimpleNodeWrapper(Resources.get("factionstatspanel.node.migrants") + f.getMigrants() + "/" + f.getMaxMigrants(), "migrants"));
        rootNode.add(currentNode);
      }

      /* alliances node */
      if ((f.getAllies() != null) && (f.getAllies().size() > 0)) {
        currentNode = new DefaultMutableTreeNode(nodeWrapperFactory.createSimpleNodeWrapper(Resources.get("factionstatspanel.node.alliances"), "alliance"));
        rootNode.add(currentNode);
        showAlliances(f.getAllies(), currentNode);
      }

      /* group alliances node */
      if ((f.getGroups() != null) && (f.getGroups().size() > 0)) {
        currentNode = new DefaultMutableTreeNode(nodeWrapperFactory.createSimpleNodeWrapper(Resources.get("factionstatspanel.node.groups"), "groups"));
        rootNode.add(currentNode);

        for (Iterator<Group> iter = f.getGroups().values().iterator(); iter.hasNext();) {
          Group g = iter.next();
          subNode = new DefaultMutableTreeNode(g);
          currentNode.add(subNode);

          if (g.getRaceNamePrefix() != null) {
            subNode.add(new DefaultMutableTreeNode(Resources.get("factionstatspanel.node.racenameprefix") + g.getRaceNamePrefix()));
          }

          showAlliances(g.allies(), subNode);
        }
      }
      
      /* other races node */
      if (specialPersons.size() > 0) {
        // n = new
        // DefaultMutableTreeNode(Resources.get("factionstatspanel.node.otherrace"));
        currentNode = new DefaultMutableTreeNode(nodeWrapperFactory.createSimpleNodeWrapper(Resources.get("factionstatspanel.node.otherrace"), "persons_of_other_race"));
        rootNode.add(currentNode);

        for (Iterator iter = specialPersons.keySet().iterator(); iter.hasNext();) {
          Object obj = iter.next();
          List<Unit> v = specialPersons.get(obj);
          int count = 0;
          String actRealRaceName = "";
          for (Iterator<Unit> iterator = v.iterator(); iterator.hasNext();) {
            Unit actU = iterator.next();
            count += actU.getPersons();
            actRealRaceName = actU.getSimpleRealRaceName();

          }
          String iconPersonName = "person";
          // now we check if a specific race icon exists, if true, we use it
          if (getMagellanContext().getImageFactory().existImageIcon(actRealRaceName)) {
            iconPersonName = actRealRaceName;
          }

          subNode = new DefaultMutableTreeNode(nodeWrapperFactory.createSimpleNodeWrapper(obj + ": " + count, iconPersonName));
          /**
           * String raceNameLang =
           * com.eressea.util.Umlaut.convertUmlauts(obj.toString()); String
           * iconNameEn = getString(raceNameLang); if
           * (iconNameEn.equalsIgnoreCase(raceNameLang)) { m = new
           * DefaultMutableTreeNode(obj + ": " + count); } else { m = new
           * DefaultMutableTreeNode(nodeWrapperFactory.createSimpleNodeWrapper(obj + ": " +
           * count, iconNameEn)); }
           */
          currentNode.add(subNode);

          for (Iterator iterator = v.iterator(); iterator.hasNext();) {
            Unit actUnit = (Unit) iterator.next();
            DefaultMutableTreeNode o = new DefaultMutableTreeNode(nodeWrapperFactory.createUnitNodeWrapper(actUnit, actUnit.getPersons()));
            subNode.add(o);
          }
        }
      }


      /* heroes node */
      if (f.getMaxHeroes() > -1 || heros_count > 0) {
        // n = new
        // DefaultMutableTreeNode(Resources.get("factionstatspanel.node.heroes"));
        double maxHeros = 0;
        long maxHeros2 = 0;
        if (personCounter > 50) {
          maxHeros = (java.lang.Math.log(personCounter / 50) / java.lang.Math.log(10)) * 20;
          maxHeros2 = java.lang.Math.round(java.lang.Math.floor(maxHeros));
        }

        if (f.getMaxHeroes() > -1) {
          maxHeros2 = f.getMaxHeroes();
        }

        String actHeroes = "";
        if (f.getHeroes() != heros_count && f.getHeroes() > -1) {
          actHeroes = Resources.get("factionstatspanel.node.heroes") + " " + f.getHeroes() + "(" + heros_count + ")" + "/" + maxHeros2;
        } else {
          actHeroes = Resources.get("factionstatspanel.node.heroes") + " " + heros_count + "/" + maxHeros2;
        }

        currentNode = new DefaultMutableTreeNode(nodeWrapperFactory.createSimpleNodeWrapper(actHeroes, "heroes"));
        rootNode.add(currentNode);

        for (Iterator iter = heroes.iterator(); iter.hasNext();) {
          Unit u = (Unit) iter.next();

          subNode = new DefaultMutableTreeNode(nodeWrapperFactory.createUnitNodeWrapper(u, u.getPersons()));
          currentNode.add(subNode);
        }

      }
    }
    
    // earned amount of money
    // it is necessary to parse messages to get this information
//  0 = Arbeiten
    // 1 = Unterhaltung
    // 2 = Treiben
    // 3 = Handel
    // 4 = am Handel
    // 5 = Diebstahl
    // 6 = Zauberei
    int earned[] = new int[] { 0, 0, 0, 0, 0, 0, 0 };
    int wanted[] = new int[] { 0, 0, 0, 0, 0, 0, 0 };
    int spentForTrade = 0;
    int totalIncome = 0;
    int totalWanted = 0;
    
    // iconnames for income groups
    String incomeGroupIcon[] = new String[] {"Arbeiten","Unterhaltung","Steuereintreiben","Handeln","Handeln","Tarnung","Magie"};
    
    for (Iterator<Faction> fIter = factions.values().iterator(); fIter.hasNext();) {     
      Faction faction = fIter.next();
      if (faction.getMessages() != null) {
        Iterator<Message> iter = faction.getMessages().iterator();
        while (iter.hasNext()) {
          Message msg = iter.next();
          if (msg.getAttributes() != null) {
            // check whether the message belongs to one of the selected regions
            String value = msg.getAttributes().get("region");
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
              value = msg.getAttributes().get("unit");
              if (value != null) {
                String number = value;
                UnitID id = UnitID.createUnitID(number, 10);
                Unit unit = data.units().get(id);

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
            }

            // region check done
            if ((msg.getMessageType() != null) && (msg.getMessageType().getID() != null)) {
              int msgID = ((IntegerID) msg.getMessageType().getID()).intValue();

              if ((msgID == 771334452) || (msgID == 2097)) {
                // Einnahmen
                String modeValue = msg.getAttributes().get("mode");

                if (modeValue != null) {
                  int i = Integer.parseInt(modeValue);
                  value = msg.getAttributes().get("amount");

                  if (value != null) {
                    earned[i] += Integer.parseInt(value);
                  }

                  value = msg.getAttributes().get("wanted");

                  if (value != null) {
                    wanted[i] += Integer.parseInt(value);
                  }
                }
              } else if (msgID == 170076) {
                // bezahlt für Kauf von Luxusgütern
                value = msg.getAttributes().get("money");

                if (value != null) {
                  spentForTrade += Integer.parseInt(value);
                }
              }
            }
          }
        }
      }

    }
    
    for (int i = 0; i < earned.length; i++) {
      totalIncome += earned[i];
      totalWanted += wanted[i];
    }
    
    if ((totalIncome != 0) || (totalWanted != 0)) {
      Object msgArgs[] = { new Integer(totalIncome), new Integer(totalWanted) };
      // n = new DefaultMutableTreeNode((new
      // java.text.MessageFormat(Resources.get("factionstatspanel.node.income"))).format(msgArgs));
      currentNode = new DefaultMutableTreeNode(nodeWrapperFactory.createSimpleNodeWrapper((new java.text.MessageFormat(Resources.get("factionstatspanel.node.income"))).format(msgArgs), "income"));
      rootNode.add(currentNode);
    }

    for (int i = 0; i < earned.length; i++) {
      if ((earned[i] != 0) || (wanted[i] != 0)) {
        Object msgArgs[] = { new Integer(earned[i]) };
        StringBuffer sb = new StringBuffer();
        sb.append(new java.text.MessageFormat(Resources.get("factionstatspanel.node.income" + i)).format(msgArgs));

        if (earned[i] != wanted[i]) {
          msgArgs = new Object[] { new Integer(wanted[i]) };
          sb.append(" ");
          sb.append((new java.text.MessageFormat(Resources.get("factionstatspanel.node.incomewanted"))).format(msgArgs));
        }

        subNode = new DefaultMutableTreeNode(nodeWrapperFactory.createSimpleNodeWrapper(sb.toString(), incomeGroupIcon[i]));
        currentNode.add(subNode);
      }
    }

    if (spentForTrade != 0) {
      Object msgArgs[] = { new Integer(-spentForTrade) };
      String s = (new java.text.MessageFormat(Resources.get("factionstatspanel.node.spentfortrade")).format(msgArgs));
      subNode = new DefaultMutableTreeNode(nodeWrapperFactory.createSimpleNodeWrapper(s,"Handeln"));
      currentNode.add(subNode);
    }
    
    
    
    // Ausgaben
    // earned amount of money
    // it is necessary to parse messages to get this information
    //  0 = Unterhalt (just count Persons * 10 ?)
    // 1 = Gebäudeunterhalt 
    // 2 = Lernkosten
    // 3 = Almosen
    // 4 = Magiekosten (no chance: Messages contains just the magic item, no cost. No chance to find the spell for sure..)
    // 5 = Handel
    // 6 = Übergaben an andere Parteien
    int spent[] = new int[] { 0, 0, 0, 0, 0, 0, 0};
    
    Map<ID,Integer> buildingUpkeep = new HashMap<ID, Integer>();
    Map<ID,Integer> factionAlms = new HashMap<ID, Integer>();
    
    Map<ID,Map<Region,Integer>> almRegions = new HashMap<ID, Map<Region,Integer>>();
    Map<ID,Integer> factionGivings = new HashMap<ID, Integer>();
    
    // iconnames for income groups
    String spentGroupIcon[] = new String[] {"Persons","Buildingcost","Skills","Alliance","Magie","Handeln","Persons"};
    
    int totalSpent = 0;
    
    for (Iterator<Faction> fIter = factions.values().iterator(); fIter.hasNext();) {

      Faction faction = fIter.next();

      if (faction.getMessages() != null) {
        Iterator<Message> iter = faction.getMessages().iterator();

        while (iter.hasNext()) {
          Message msg = iter.next();

          if (msg.getAttributes() != null) {
            // check whether the message belongs to one of the selected regions
            String value = msg.getAttributes().get("region");

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
              value = msg.getAttributes().get("unit");

              if (value != null) {
                String number = value;
                
                UnitID id = UnitID.createUnitID(number, 10);
                Unit unit = data.units().get(id);

                if (unit != null) {
                  Region r = unit.getRegion();

                  if (r != null) {
                    CoordinateID c = r.getCoordinate();

                    if (!regions.containsKey(c) || !unit.getFaction().equals(faction)) {
                      continue;
                    }
                  }
                }
              }
            }

            // region check done
            if ((msg.getMessageType() != null) && (msg.getMessageType().getID() != null)) {
              int msgID = ((IntegerID) msg.getMessageType().getID()).intValue();

              if (msgID == 107552268) {
                // Gebäudeunterhalt
                String buildingNR = msg.getAttributes().get("building");
                if (buildingNR != null) {
                  int i = Integer.parseInt(buildingNR);
                  EntityID id = EntityID.createEntityID(i,data.base);
                  // get the building
                  Building b = data.getBuilding(id);
                  if (b!=null){
                    // get Building type
                    BuildingType bT = b.getBuildingType();
                    if (bT!=null){
                      // get Maintenance cost Silver
                      ItemType silverType = data.rules.getItemType(EresseaConstants.I_SILVER,false);
                      if (silverType!=null){
                        Item silverItem = bT.getMaintenance(silverType.getID());
                        if (silverItem!=null){
                          spent[1]+=silverItem.getAmount();
                          // entry in building categories
                          Integer actV = buildingUpkeep.get(bT.getID());
                          if (actV==null){
                            actV = new Integer(silverItem.getAmount());
                          } else {
                            actV = new Integer(actV.intValue() + silverItem.getAmount());
                          }
                          buildingUpkeep.put(bT.getID(),actV);
                        }
                      }
                    }
                  }
                }
              } else if (msgID == 170076) {
                // bezahlt für Kauf von Luxusgütern
                value = msg.getAttributes().get("money");

                if (value != null) {
                  spent[5] += Integer.parseInt(value);
                }
              } else if (msgID == 443066738){
                // Lernkosten
                value = msg.getAttributes().get("cost");
                if (value != null) {
                  spent[2] += Integer.parseInt(value);
                }
              } else if (msgID == 1682429624){
                // Almosen
                // müssen in den Regionsmessages überprüft werden
                
              } else if (msgID == 5281483){
                // Übergaben an fremde Nationen
                value = msg.getAttributes().get("resource");
                String value2 = msg.getAttributes().get("amount");
                if (value!=null && value.equalsIgnoreCase("silber") && value2!=null){
                  int menge = Integer.parseInt(value2);
                  value = msg.getAttributes().get("target");
                  UnitID id = UnitID.createUnitID(Integer.parseInt(value), 10);
                  Unit unit = data.units().get(id);
                  if (unit != null) {
                    ID factionID = unit.getFaction().getID();
                    if (factionID!=null){
                      Integer actV = factionGivings.get(factionID);
                      if (actV== null){
                        actV = new Integer(menge);
                      } else {
                        actV = new Integer(menge+actV.intValue());
                      }
                      factionGivings.put(factionID, actV);
                      spent[6]+=menge;
                    }
                  }
                }
                
                
              }
            }
          }
        }
      }
      // search reagionsmessages for Almosen
      if (regions!=null && regions.size()>0){
        for (Iterator<Region> iterR = regions.values().iterator();iterR.hasNext();){
          Region actR = iterR.next();
          if (actR.getMessages()!=null && actR.getMessages().size()>0){
            for (Iterator<Message> iterM = actR.getMessages().iterator();iterM.hasNext();){
              Message actM = iterM.next();
              if ((actM.getMessageType() != null) && (actM.getMessageType().getID() != null)) {
                int msgID = ((IntegerID) actM.getMessageType().getID()).intValue();
                if (msgID == 1682429624){
                  // Almosen
                  // check, ob from unsere Faction ist
                  String value = actM.getAttributes().get("from");
                  EntityID id = EntityID.createEntityID(Integer.parseInt(value), data.base);
                  // Faction beziehen
                  Faction actF = data.getFaction(id);
                  if (actF!=null && actF.equals(faction)){
                    // faction matches
                    value = actM.getAttributes().get("amount");
                    if (value != null) {
                      int cost = Integer.parseInt(value);
                      spent[3] += cost;
                      
                      // collect data for subnodes
                      // alms for whom?
                      value = actM.getAttributes().get("to");
                      id = EntityID.createEntityID(Integer.parseInt(value), data.base);
                      Faction toF = data.getFaction(id);
                      if (toF!=null){
                        Integer actV = factionAlms.get(toF.getID());
                        if (actV==null){
                          actV = new Integer(cost);
                        } else {
                          actV = new Integer(actV.intValue() + cost);
                        }
                        factionAlms.put(toF.getID(), actV);
                        
                        Map<Region,Integer> actRI = almRegions.get(toF.getID());
                        if (actRI==null){
                          actRI = new HashMap<Region, Integer>();
                        }
                        actV = actRI.get(actR);
                        if (actV==null){
                          actV = new Integer(cost);
                        } else {
                          actV = new Integer(cost+actV.intValue());
                        }
                        actRI.put(actR,actV);
                        almRegions.put(toF.getID(),actRI);
                      }
                    }
                  }
                }
              }
            }
          }
        }
      }
  
    } // loop factions
    
    //  Unterhalt
    // we ignore persons of races which do not need support
    spent[0] = personCounter * 10;
    
    for (int i = 0; i < spent.length; i++) {
      totalSpent += spent[i];
    }
    if (totalSpent != 0) {
      currentNode = new DefaultMutableTreeNode(nodeWrapperFactory.createSimpleNodeWrapper(Resources.get("factionstatspanel.node.spent") + ": " + NumberFormat.getNumberInstance().format(totalSpent), "income"));
      rootNode.add(currentNode);
      for (int i = 0; i < spent.length; i++) {
        if (spent[i] != 0) {
          subNode = new DefaultMutableTreeNode(nodeWrapperFactory.createSimpleNodeWrapper(Resources.get("factionstatspanel.node.spent" + i) + ": " + NumberFormat.getNumberInstance().format(spent[i]), spentGroupIcon[i]));
          currentNode.add(subNode);
          // specials...
          if (i==1){
            // buildings after Type
            if (buildingUpkeep.size()>0){
              for (Iterator<ID> iterBT = buildingUpkeep.keySet().iterator();iterBT.hasNext();){
                ID btID = iterBT.next();
                Integer actV = buildingUpkeep.get(btID);
                BuildingType bT = data.rules.getBuildingType(btID);
                if (bT!=null && actV!=null && actV.intValue()>0){
                  DefaultMutableTreeNode subSubNode = new DefaultMutableTreeNode(
                      nodeWrapperFactory.createSimpleNodeWrapper(bT.getName() + ": " + NumberFormat.getNumberInstance().format(actV.intValue()), bT.getName()));
                  subNode.add(subSubNode);
                }
              }
            }
          } else if (i==3){
            // Almosen nach Factions UND Regions
            if (factionAlms.size()>0){
              for (Iterator<ID> iterF = factionAlms.keySet().iterator();iterF.hasNext();){
                ID actFID = iterF.next();
                Integer actV = factionAlms.get(actFID);
                Faction actThisF = data.getFaction(actFID);
                if (actV!=null && actThisF!=null){
                  // node für diese Faction hinzu
                  DefaultMutableTreeNode subSubNode = new DefaultMutableTreeNode(
                      nodeWrapperFactory.createSimpleNodeWrapper(actThisF.getName() + ": " + NumberFormat.getNumberInstance().format(actV.intValue()), "Silber"));
                  subNode.add(subSubNode);
                  // regions für diese Faction dazu
                  Map<Region,Integer> actRM = almRegions.get(actThisF.getID());
                  if (actRM!=null && actRM.size()>0){
                    for (Iterator<Region> iterR = actRM.keySet().iterator();iterR.hasNext();){
                      Region actRR = iterR.next();
                      actV = actRM.get(actRR);
                      if (actV!=null && actV.intValue()>0){
                        DefaultMutableTreeNode subSubSubNode = new DefaultMutableTreeNode(
                            nodeWrapperFactory.createRegionNodeWrapper(actRR, actV.intValue()));
                        subSubNode.add(subSubSubNode);
                      }
                    }
                  }
                }
              }
            }
          } else if (i==6){
            // Übergaben
            if (factionGivings.size()>0){
              for (Iterator<ID> iterFID = factionGivings.keySet().iterator();iterFID.hasNext();){
                ID fID = iterFID.next();
                Integer actV = factionGivings.get(fID);
                Faction actTF = data.getFaction(fID);
                if (actTF!=null && actV!=null && actV.intValue()>0){
                  DefaultMutableTreeNode subSubNode = new DefaultMutableTreeNode(
                      nodeWrapperFactory.createSimpleNodeWrapper(actTF.getName() + ": " + NumberFormat.getNumberInstance().format(actV.intValue()), "Silber"));
                  subNode.add(subSubNode);
                }
              }
            }
          }
        }
      }
    }
    
    // show items and clear categories
    unitsTools.addCategorizedUnitItems(units.values(), rootNode, null, null, true, nodeWrapperFactory);
    
    // add buildings
    // maps BuildingTypes to a List, containing the single buildings
    Map<UnitContainerType, List<Building>> buildingsCounter = new Hashtable<UnitContainerType, List<Building>>();

    // collect the buildings
    for (Iterator iterator = regions.values().iterator(); iterator.hasNext();) {
      Region r = (Region) iterator.next();

      for (Iterator iter = r.buildings().iterator(); iter.hasNext();) {
        Building building = (Building) iter.next();

        if ((building.getOwnerUnit() != null) && factions.containsKey(building.getOwnerUnit().getFaction().getID())) {
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

      currentNode = new DefaultMutableTreeNode(nodeWrapperFactory.createSimpleNodeWrapper(Resources.get("factionstatspanel.node.buildings"), "buildings"));
      rootNode.add(currentNode);
    }

    for (Iterator iter = buildingsCounter.keySet().iterator(); iter.hasNext();) {
      UnitContainerType buildingType = (UnitContainerType) iter.next();
      // Fiete 20060916: changed to display icons instead of folders
      // m = new DefaultMutableTreeNode(buildingType.getName() + ": " +
      // ((List) buildingsCounter.get(buildingType)).size());

      String buildingIconName = StringFactory.getFactory().intern(buildingType.getID().toString());
      subNode = new DefaultMutableTreeNode(nodeWrapperFactory.createSimpleNodeWrapper(buildingType.getName() + ": " + ((List) buildingsCounter.get(buildingType)).size(), buildingIconName));

      currentNode.add(subNode);

      for (Iterator i = ((List) buildingsCounter.get(buildingType)).iterator(); i.hasNext();) {
        UnitContainerNodeWrapper uc = nodeWrapperFactory.createUnitContainerNodeWrapper((Building) i.next());
        subNode.add(new DefaultMutableTreeNode(uc));
      }
    }

    // add ships
    // maps ShipTypes to Lists, containing the single ships
    Map<UnitContainerType, List<Ship>> shipsCounter = new Hashtable<UnitContainerType, List<Ship>>();

    // collect the ships
    for (Iterator iterator = regions.values().iterator(); iterator.hasNext();) {
      Region r = (Region) iterator.next();

      for (Iterator iter = r.ships().iterator(); iter.hasNext();) {
        Ship ship = (Ship) iter.next();

        if ((ship.getOwnerUnit() != null) && factions.containsKey(ship.getOwnerUnit().getFaction().getID())) {
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
      currentNode = new DefaultMutableTreeNode(nodeWrapperFactory.createSimpleNodeWrapper(Resources.get("factionstatspanel.node.ships"), "ships"));
      rootNode.add(currentNode);
    }

    for (Iterator iter = shipsCounter.keySet().iterator(); iter.hasNext();) {
      UnitContainerType shipType = (UnitContainerType) iter.next();
      /**
       * Fiete 20060911 m = new DefaultMutableTreeNode(shipType.getName() + ": " +
       * ((List) shipsCounter.get(shipType)).size());
       */
      // Fiete 20060911: added support for shiptypeicons
      // Fiete 20060915: get rid of english icon names...using stringfactory to
      // get the orginal names
      String shipIconName = StringFactory.getFactory().intern(shipType.getID().toString());
      subNode = new DefaultMutableTreeNode(nodeWrapperFactory.createSimpleNodeWrapper(shipType.getName() + ": " + ((List) shipsCounter.get(shipType)).size(), shipIconName));

      currentNode.add(subNode);

      for (Iterator i = ((List) shipsCounter.get(shipType)).iterator(); i.hasNext();) {
        UnitContainerNodeWrapper uc = nodeWrapperFactory.createUnitContainerNodeWrapper((Ship) i.next());
        subNode.add(new DefaultMutableTreeNode(uc));
      }
    }

    // add skills
    List sortedSkillTypes = skillStats.getKnownSkillTypes();

    if (sortedSkillTypes.size() > 0) {
      // n = new
      // DefaultMutableTreeNode(Resources.get("factionstatspanel.node.skills"));
      currentNode = new DefaultMutableTreeNode(nodeWrapperFactory.createSimpleNodeWrapper(Resources.get("factionstatspanel.node.skills"), "skills"));
      rootNode.add(currentNode);

      for (Iterator iter = sortedSkillTypes.iterator(); iter.hasNext();) {
        SkillType type = (SkillType) iter.next();
        List sortedSkills = skillStats.getKnownSkills(type);

        for (Iterator i = sortedSkills.iterator(); i.hasNext();) {
          Skill skill = (Skill) i.next();
          subNode = new DefaultMutableTreeNode(new SimpleNodeWrapper(type.getName() + " T" + skill.getLevel() + ": " + skillStats.getPersonNumber(skill), type.getName()));
          currentNode.add(subNode);

          List<Unit> unitList = skillStats.getUnits(skill);

          // now sort the units and add them as nodes...
          Comparator<Unique> idCmp = IDComparator.DEFAULT;
          Comparator<Unit> unitCmp = new UnitSkillComparator(new SpecifiedSkillTypeSkillComparator(type, new SkillComparator(), null), idCmp);
          Collections.sort(unitList, unitCmp);

          for (Iterator<Unit> iterator = unitList.iterator(); iterator.hasNext();) {
            Unit u = iterator.next();
            String text = u.toString();

            if (!data.noSkillPoints) {
              int bonus = u.getRace().getSkillBonus(type);
              int currentDays = u.getSkill(type).getPointsPerPerson();
              int nextLevelDays = Skill.getPointsAtLevel(skill.getLevel() - bonus + 1);
              int pointsToLearn = nextLevelDays - currentDays;
              int turnsToLearn = pointsToLearn / 30;

              if ((pointsToLearn % 30) > 0) {
                turnsToLearn++;
              }

              text += (": " + " [" + currentDays + " -> " + nextLevelDays + " {" + turnsToLearn + "}], " + u.getPersons());

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
    Map<ItemCategory, Map<String, ProductionStats>> production = new Hashtable<ItemCategory, Map<String, ProductionStats>>();

    for (Iterator<Faction> Iter = factions.values().iterator(); Iter.hasNext();) {
      Faction faction = Iter.next();

      if (faction.getMessages() != null) {
        for (Iterator<Message> i = faction.getMessages().iterator(); i.hasNext();) {
          Message msg = i.next();
          int msgID = ((IntegerID) msg.getMessageType().getID()).intValue();
          
          // check whether the message belongs to one of the selected regions
          if (msg.getAttributes() != null) {
            String regionCoordinate = msg.getAttributes().get("region");

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
            if ((msg.getMessageType() != null) && (msg.getMessageType().getSection() != null) && msg.getMessageType().getSection().equalsIgnoreCase("production")) {
              String value = msg.getAttributes().get("amount");
              int amount = 0;

              if (value != null) {
                amount = Integer.parseInt(value);
              }

              if (amount != 0) {
                String resource = msg.getAttributes().get("resource");

                // get the resource
                if (resource == null) {
                  // possible a message containing ;herb instead of ;resource
                  resource = msg.getAttributes().get("herb");
                }
                
                if (resource==null){
                  // In Pferdezucht gezüchtete Pferde haben keine ressource
                  // Fiete: 20080521
                  if (msgID==687207561){
                    resource = "Pferd";
                  }
                }
                
                
                if (resource != null) {
                  // find the category
                  ItemType itemType = data.rules.getItemType(StringID.create(resource));
                  ItemCategory itemCategory = null;

                  if (itemType != null) {
                    itemCategory = itemType.getCategory();
                  }

                  if (itemCategory == null) {
                    FactionStatsPanel.log.info("Item without category: "+resource);
                  } else {
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
                    value = msg.getAttributes().get("unit");

                    if (value != null) {
                      int number = Integer.parseInt(value);

                      UnitID id = UnitID.createUnitID(number, data.base);
                      Unit u = data.getUnit(id);

                      if (u != null) {
                        p.units.put(u, new Integer(amount));
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
    DefaultMutableTreeNode prodNode = new DefaultMutableTreeNode(nodeWrapperFactory.createSimpleNodeWrapper(Resources.get("factionstatspanel.node.production"), "production"));

    for (Iterator iter = production.keySet().iterator(); iter.hasNext();) {
      ItemCategory iCategory = (ItemCategory) iter.next();
      
      ArrayList<DefaultMutableTreeNode> subNodeChildList = new ArrayList<DefaultMutableTreeNode>();
      
      String catIconName = magellan.library.utils.Umlaut.convertUmlauts(iCategory.getName());
      String nodeName = Resources.get("factionstatspanel." + catIconName);

      Map<String,ProductionStats> h = production.get(iCategory);
      int totalAmount = 0;

      for (Iterator<String> iterator = h.keySet().iterator(); iterator.hasNext();) {
        String resource = iterator.next();
        ProductionStats stats = h.get(resource);
        totalAmount += stats.totalAmount;
        DefaultMutableTreeNode o = null;
        // o = new DefaultMutableTreeNode(resource + ": " + stats.totalAmount);
        if (catIconName.equalsIgnoreCase("kraeuter")) {
          o = createSimpleNode(data.getTranslation(resource) + ": " + stats.totalAmount, "items/" + "kraeuter");
        } else {
          o = createSimpleNode(data.getTranslation(resource) + ": " + stats.totalAmount, "items/" + resource);
        }

        // subNode.add(o);
        subNodeChildList.add(o);
        int resAmount = 0;
        for (Iterator i = stats.units.keySet().iterator(); i.hasNext();) {
          Unit u = (Unit) i.next();
          int amount = stats.units.get(u).intValue();
          resAmount += amount;
          o.add(new DefaultMutableTreeNode(nodeWrapperFactory.createUnitNodeWrapper(u, amount)));
        }
      }
      if (subNodeChildList!=null && subNodeChildList.size()>0) {
        subNode = createSimpleNode(nodeName + ":" + totalAmount, catIconName);
        for (Iterator<DefaultMutableTreeNode> iterDMTN = subNodeChildList.iterator();iterDMTN.hasNext();){
          subNode.add(iterDMTN.next());
        }
        prodNode.add(subNode);
      }
      
    }

    if (prodNode.getChildCount() > 0) {
      rootNode.add(prodNode);
    }

    // Fiete: Displaying Faction-Items
    // only if one faction is selected
    if (factions.size()==1) {
      Faction f = factions.values().iterator().next();
      if (f.getItems() != null) {
        if (f.getItems().size() > 0) {
          DefaultMutableTreeNode factionPoolNode = new DefaultMutableTreeNode(nodeWrapperFactory.createSimpleNodeWrapper(Resources.get("factionstatspanel.node.factionpool"), "factionpool"));
          for (Iterator iter = f.getItems().iterator(); iter.hasNext();) {
            Item actItem = (Item) iter.next();
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

  private void showAlliances(Map<ID, Alliance> allies, DefaultMutableTreeNode rootNode) {
    if ((allies == null) || (rootNode == null)) {
      return;
    }

    Map<String, List<Alliance>> alliances = new TreeMap<String, List<Alliance>>();
    for (Alliance alliance : allies.values()) {
      String key = alliance.stateToString();
      if (alliances.containsKey(key)) {
        alliances.get(key).add(alliance);
      } else {
        List<Alliance> list = new LinkedList<Alliance>();
        list.add(alliance);
        alliances.put(key,list);
      }
    }
    for(String key : alliances.keySet()) {
      List<Alliance> alliance = alliances.get(key);
      Collections.sort(alliance, new AllianceFactionComparator(new NameComparator(IDComparator.DEFAULT)));
      
      DefaultMutableTreeNode m = new DefaultMutableTreeNode(Resources.get("emapdetailspanel.alliancestate",new Object[]{key}));
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

    JScrollPane treeScrollPane = new JScrollPane(tree, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
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

//  private DefaultMutableTreeNode createSimpleNode(Named obj, String icons) {
//    return new DefaultMutableTreeNode(nodeWrapperFactory.createSimpleNodeWrapper(obj, this.data.getTranslation(obj), (Object) icons));
//  }

  /**
   * A little class used to store information about production statistics for a
   * certain resource
   */
  private class ProductionStats {
    // mapping units who produced the special resource (Unit) to the according
    // amounts (Integer)
    private Map<Unit, Integer> units = new Hashtable<Unit, Integer>();

    // total amount produced by all units
    private int totalAmount;
  }
    
}
