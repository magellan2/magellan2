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
 * ArmyStatsPanel.java
 *
 * Created on 4. März 2002, 12:19
 */
package magellan.client.swing;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTree;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;

import magellan.client.event.EventDispatcher;
import magellan.client.event.SelectionEvent;
import magellan.client.event.SelectionListener;
import magellan.client.swing.tree.CellRenderer;
import magellan.client.swing.tree.CopyTree;
import magellan.client.swing.tree.NodeWrapperFactory;
import magellan.client.swing.tree.SimpleNodeWrapper;
import magellan.client.swing.tree.UnitNodeWrapper;
import magellan.library.Alliance;
import magellan.library.Faction;
import magellan.library.GameData;
import magellan.library.Island;
import magellan.library.Item;
import magellan.library.Named;
import magellan.library.Region;
import magellan.library.Rules;
import magellan.library.Skill;
import magellan.library.StringID;
import magellan.library.Unit;
import magellan.library.event.GameDataEvent;
import magellan.library.gamebinding.EresseaConstants;
import magellan.library.rules.ItemCategory;
import magellan.library.rules.ItemType;
import magellan.library.rules.SkillType;
import magellan.library.utils.Resources;
import magellan.library.utils.TrustLevels;
import magellan.library.utils.filters.CollectionFilters;
import magellan.library.utils.logging.Logger;

/**
 * DOCUMENT ME!
 * 
 * @author Andreas
 * @version 1.0
 */
public class ArmyStatsPanel extends InternationalizedDataPanel implements TreeSelectionListener,
    SelectionListener {
  public static final String IDENTIFIER = "ARMYSTATS";

  private static final Logger log = Logger.getInstance(ArmyStatsPanel.class);
  protected NodeWrapperFactory factory;
  protected CopyTree tree;
  protected CopyTree tree2;
  protected DefaultMutableTreeNode treeRoot;
  protected DefaultMutableTreeNode tree2Root;
  protected JSplitPane content;
  protected int dividerPos = Integer.MIN_VALUE;
  protected List<Armies> armies;
  protected Map<Skill, List<ItemType>> weapons;
  protected ItemCategory weapon;
  protected ItemCategory front;
  protected ItemCategory back;
  protected ItemCategory armourType;
  protected ItemCategory shieldType;
  protected boolean categorize = true;
  protected List<SkillType> excludeSkills;
  protected List<String> excludeNames;
  protected List<Integer> excludeCombatStates;
  protected Collection<Region> lastSelected;

  /**
   * Creates new ArmyStatsPanel
   */
  public ArmyStatsPanel(EventDispatcher ed, GameData data, Properties settings, boolean doCategorize) {
    this(ed, data, settings, doCategorize, null);
  }

  /**
   * Creates a new ArmyStatsPanel object.
   */
  public ArmyStatsPanel(EventDispatcher ed, GameData data, Properties settings,
      boolean doCategorize, Collection<Region> selRegions) {
    super(ed, data, settings);

    lastSelected = selRegions;

    // unnecessary
    // dispatcher.addGameDataListener(this);
    ed.addSelectionListener(this);

    factory = new NodeWrapperFactory(settings, "EMapOverviewPanel", "Dummy-Factory");

    String dPos = settings.getProperty("ArmyStatsPanel.DividerLoc");

    if ((dPos != null) && !dPos.equals("")) {
      try {
        dividerPos = Integer.parseInt(dPos);
      } catch (Exception exc) {
        // no valid number
      }
    }

    categorize = doCategorize;

    initTrees(ed);

    createArmies(data, selRegions);
    createTrees();

    setLayout(new java.awt.BorderLayout());
    content.setDividerLocation(dividerPos);
    add(content, java.awt.BorderLayout.CENTER);
  }

  /**
   * @see magellan.client.swing.InternationalizedDataPanel#gameDataChanged(magellan.library.event.GameDataEvent)
   */
  @Override
  public void gameDataChanged(GameDataEvent e) {
    super.gameDataChanged(e);

    GameData data = e.getGameData();
    createArmies(data);
    createTrees();
  }

  /**
   * 
   */
  public void setCategorized(boolean b) {
    categorize = b;
  }

  /**
   * DOCUMENT-ME
   */
  public void setExcludeSkills(List<SkillType> l) {
    excludeSkills = l;
  }

  /**
   * DOCUMENT-ME
   */
  public void setExcludeNames(List<String> l) {
    excludeNames = l;
  }

  /**
   * DOCUMENT-ME
   */
  public void setExcludeCombatStates(List<Integer> l) {
    excludeCombatStates = l;
  }

  protected void updateData(GameData data, Collection<Region> regions) {
    armies.clear();
    createArmies(data, regions, categorize); // jump over rule base
    createTrees();
    doUpdate();
    repaint();
  }

  /**
   * DOCUMENT-ME
   */
  public void doUpdate() {
    /** Try to fix the swing bug of a a too small tree */
    javax.swing.plaf.TreeUI ui = tree.getUI();

    if (ui instanceof javax.swing.plaf.basic.BasicTreeUI) {
      javax.swing.plaf.basic.BasicTreeUI ui2 = (javax.swing.plaf.basic.BasicTreeUI) ui;
      int i = ui2.getLeftChildIndent();
      ui2.setLeftChildIndent(100);
      ui2.setLeftChildIndent(i);
    }

    ui = tree2.getUI();

    if (ui instanceof javax.swing.plaf.basic.BasicTreeUI) {
      javax.swing.plaf.basic.BasicTreeUI ui2 = (javax.swing.plaf.basic.BasicTreeUI) ui;
      int i = ui2.getLeftChildIndent();
      ui2.setLeftChildIndent(100);
      ui2.setLeftChildIndent(i);
    }

    if (dividerPos != Integer.MIN_VALUE) {
      content.setDividerLocation(dividerPos);
    } else {
      content.setDividerLocation(0.5);
      dividerPos = content.getDividerLocation();
    }
  }

  /**
   * DOCUMENT-ME
   */
  public void recreate(GameData data) {
    dividerPos = content.getDividerLocation();

    setGameData(data);
    createArmies(data, data.equals(getGameData()) ? lastSelected : null);
    createTrees();
    content.setDividerLocation(dividerPos);
  }

  /**
   * DOCUMENT-ME
   */
  @Override
  public void quit() {
    settings.setProperty("ArmyStatsPanel.DividerLoc", String.valueOf(content.getDividerLocation()));
  }

  protected void initTrees(EventDispatcher ed) {
    CellRenderer renderer = new CellRenderer(getMagellanContext());

    treeRoot = new DefaultMutableTreeNode();

    DefaultTreeModel dtm = new DefaultTreeModel(treeRoot);
    tree = new CopyTree(dtm);
    tree.setRootVisible(false);
    tree.setCellRenderer(renderer);
    tree.addTreeSelectionListener(this);

    tree2Root = new DefaultMutableTreeNode();
    dtm = new DefaultTreeModel(tree2Root);
    tree2 = new CopyTree(dtm);
    tree2.setRootVisible(false);
    tree2.setCellRenderer(renderer);
    tree2.addTreeSelectionListener(this);

    JSplitPane pane =
        new JSplitPane(JSplitPane.VERTICAL_SPLIT, new JScrollPane(tree), new JScrollPane(tree2));
    pane.setDividerLocation(.5);
    content = pane;
  }

  // //////////////////////////
  // T R E E FILLING Code //
  // //////////////////////////
  protected void createTrees() {
    treeRoot.removeAllChildren();
    addList(treeRoot, armies, false);
    ((DefaultTreeModel) tree.getModel()).reload();

    tree2Root.removeAllChildren();
    addList(tree2Root, armies, true);
    ((DefaultTreeModel) tree2.getModel()).reload();
  }

  protected void addList(DefaultMutableTreeNode root, Collection<Armies> list, boolean mode) {
    Iterator<Armies> it = list.iterator();

    while (it.hasNext()) {
      Object obj = it.next();

      if (!mode && obj instanceof IslandArmies) {
        addIslandArmies(root, (IslandArmies) obj);
      } else if (!mode && obj instanceof RegionArmies) {
        addRegionArmies(root, (RegionArmies) obj);
      } else if (mode && obj instanceof WholeArmy) {
        addWholeArmy(root, (WholeArmy) obj);
      }
    }
  }

  protected DefaultMutableTreeNode addIslandArmies(DefaultMutableTreeNode root, IslandArmies armies) {
    DefaultMutableTreeNode islRoot =
        new DefaultMutableTreeNode(new SimpleNodeWrapper(armies, "insel"));
    root.add(islRoot);

    Iterator<IslandArmy> it1 = armies.iarmies.iterator();

    while (it1.hasNext()) {
      IslandArmy iArmy = it1.next();
      DefaultMutableTreeNode iRoot =
          new DefaultMutableTreeNode(new SimpleNodeWrapper(iArmy, getArmyIcon(iArmy.owner)));
      islRoot.add(iRoot);
    }

    Iterator<RegionArmies> it2 = armies.rarmies.iterator();

    while (it2.hasNext()) {
      addRegionArmies(islRoot, it2.next());
    }

    return islRoot;
  }

  protected DefaultMutableTreeNode addRegionArmies(DefaultMutableTreeNode root, RegionArmies armies) {
    DefaultMutableTreeNode regRoot =
        new DefaultMutableTreeNode(new SimpleNodeWrapper(armies, armies.region.getType().getIcon()
            .toString()
            + "-detail"));
    root.add(regRoot);

    Iterator<Army> it = armies.armies.iterator();

    while (it.hasNext()) {
      addArmy(regRoot, it.next());
    }

    return regRoot;
  }

  protected String getArmyIcon(Faction fac) {
    String icon = "kampfstatus";

    if (TrustLevels.isPrivileged(fac)) {
      icon = "alliancestate_basisfaction";
    } else if (getGameData() != null) {
      int minTrust = 255;

      for (Faction f : getGameData().getFactions()) {
        if (TrustLevels.isPrivileged(f)) {
          if ((f.getAllies() != null) && f.getAllies().containsKey(fac.getID())) {
            Alliance a = f.getAllies().get(fac.getID());
            minTrust &= a.getState();
          } else if (f.getAlliance() != null && f.getAlliance().getFactions().contains(fac.getID())) {
            // alliance implies HELP combat (in E3!)
            minTrust &= EresseaConstants.A_COMBAT;
          } else {
            minTrust = 0;
          }
        }
      }

      icon = "alliancestate_" + String.valueOf(minTrust);
    }

    return icon;
  }

  protected DefaultMutableTreeNode addWholeArmy(DefaultMutableTreeNode root, WholeArmy army) {
    DefaultMutableTreeNode armRoot =
        new DefaultMutableTreeNode(new SimpleNodeWrapper(army, getArmyIcon(army.owner)));
    root.add(armRoot);

    Iterator<Army> it = army.armies.iterator();

    while (it.hasNext()) {
      Object obj = it.next();

      if (obj instanceof Army) {
        addArmy(armRoot, (Army) obj);
      } else if (obj instanceof IslandArmy) {
        IslandArmy ia = (IslandArmy) obj;
        DefaultMutableTreeNode iRoot =
            new DefaultMutableTreeNode(new SimpleNodeWrapper(ia, "insel"));
        armRoot.add(iRoot);

        Iterator<Army> it2 = ia.armies.iterator();

        while (it2.hasNext()) {
          addArmy(iRoot, it2.next());
        }
      }
    }

    return armRoot;
  }

  protected void addArmy(DefaultMutableTreeNode root, Army army) {
    DefaultMutableTreeNode armyRoot =
        new DefaultMutableTreeNode(new SimpleNodeWrapper(army, army.shortString ? (army.region
            .getType().getIcon() + "-detail") : getArmyIcon(army.owner)));
    root.add(armyRoot);

    for (int i = 0; i < 2; i++) {
      if (army.warLines[i] != null) {
        WarLine wl = army.warLines[i];
        DefaultMutableTreeNode lineRoot = new DefaultMutableTreeNode(wl);
        armyRoot.add(lineRoot);

        if (wl.categorized) {
          for (WeaponGroup wg : wl.groups) {
            if (wg.units.size() == 1) {
              PartUnit u = wg.units.iterator().next();
              lineRoot.add(createNodeWrapper(u));
            } else {
              String icon = null;

              if (wg.weapon != null) {
                icon = "items/" + wg.weapon.getIcon();
              } else {
                icon = "warnung";
              }

              DefaultMutableTreeNode groupRoot =
                  new DefaultMutableTreeNode(new SimpleNodeWrapper(wg, icon));
              lineRoot.add(groupRoot);

              Iterator<PartUnit> it2 = wg.units.iterator();

              while (it2.hasNext()) {
                PartUnit u = it2.next();
                groupRoot.add(createNodeWrapper(u));
              }
            }
          }
        } else {
          addUnits(lineRoot, wl.units);
        }
      }
    }
  }

  protected void addUnits(DefaultMutableTreeNode root, Collection<Unit> units) {
    if (units != null) {
      Iterator<Unit> it = units.iterator();

      while (it.hasNext()) {
        Unit u = it.next();
        root.add(new DefaultMutableTreeNode(factory.createUnitNodeWrapper(u, u.getPersons())));
      }
    }
  }

  protected Collection<String> getIconObject(PartUnit unit) {
    int i = 0;

    Collection<String> col = new LinkedList<String>();

    if (unit.weapon != null) {
      i++;
    }

    if (unit.armour != null) {
      i++;
    }

    if (unit.shield != null) {
      i++;
    }

    if (i == 0) {
      col.add("warnung");
    }

    if (unit.weapon != null) {
      col.add("items/" + unit.weapon.getIcon());
    }

    if (unit.armour != null) {
      col.add("items/" + unit.armour.getIcon());
    }

    if (unit.shield != null) {
      col.add("items/" + unit.shield.getIcon());
    }

    return col;
  }

  protected DefaultMutableTreeNode createNodeWrapper(PartUnit partUnit) {
    DefaultMutableTreeNode node =
        new DefaultMutableTreeNode(new SimpleNodeWrapper(partUnit, getIconObject(partUnit)));
    node.add(new DefaultMutableTreeNode(factory.createUnitNodeWrapper(partUnit.parent,
        partUnit.parent.getPersons())));

    return node;
  }

  // ////////////////////////////
  // A R M Y CREATION CODE //
  // ////////////////////////////
  protected void createArmies(GameData data) {
    createArmies(data, null);
  }

  protected void createArmies(GameData data, Collection<Region> regions) {
    createRuleBase(data);

    if (armies == null) {
      armies = new LinkedList<Armies>();
    } else {
      armies.clear();
    }

    setGameData(data);
    createArmies(data, regions, categorize);
  }

  protected void createArmies(GameData data, Collection<Region> regions, boolean cat) {
    Comparator<Named> nameComp = new magellan.library.utils.comparator.NameComparator(null);
    Comparator<Faction> compare =
        magellan.library.utils.comparator.FactionTrustComparator.DEFAULT_COMPARATOR;

    List<Region> allRegions = null;

    if ((regions != null) && (regions.size() > 0)) {
      allRegions = new ArrayList<Region>(regions);
    } else {
      allRegions = new ArrayList<Region>(data.getRegions());
    }

    List<Island> islList = new ArrayList<Island>(data.getIslands());
    Collections.sort(islList, nameComp);

    Iterator<Island> islIt = islList.iterator();
    Map<Faction, IslandArmy> armyMap = new HashMap<Faction, IslandArmy>();
    Map<Faction, Collection<Object>> facMap = new HashMap<Faction, Collection<Object>>();

    if (islIt != null) {
      while (islIt.hasNext()) {
        armyMap.clear();

        Island isl = islIt.next();
        List<Region> iregions = new ArrayList<Region>(isl.regions());
        Collections.sort(iregions, nameComp);

        Iterator<Region> regIt = iregions.iterator();
        IslandArmies islArmies = new IslandArmies(isl);

        while (regIt.hasNext()) {
          Region reg = regIt.next();
          if (reg.units().size() == 0) {
            continue;
          }
          allRegions.remove(reg);

          Collection<Army> col = createRegionArmies(reg, cat);
          Iterator<Army> colIt1 = col.iterator();
          RegionArmies regArmies = new RegionArmies(reg);
          Map<Faction, Army> map = new HashMap<Faction, Army>();

          while (colIt1.hasNext()) {
            Army army = colIt1.next();
            map.put(army.owner, army);
          }

          List<Faction> list = new ArrayList<Faction>(map.keySet());
          Collections.sort(list, compare);
          Iterator<Faction> colIt2 = list.iterator();

          while (colIt2.hasNext()) {
            Army army = map.get(colIt2.next());
            regArmies.addArmy(army);

            Faction fac = army.owner;

            if (!armyMap.containsKey(fac)) {
              armyMap.put(fac, new IslandArmy(fac, isl));
            }

            IslandArmy islArmy = armyMap.get(fac);
            islArmy.addArmy(army);
          }

          if (regArmies.men > 0) {
            islArmies.rarmies.add(regArmies);
          }
        }

        if (armyMap.size() > 0) {
          List<Faction> list = new ArrayList<Faction>(armyMap.keySet());
          Collections.sort(list, compare);

          Iterator<Faction> iarmies = list.iterator();

          while (iarmies.hasNext()) {
            IslandArmy ia = armyMap.get(iarmies.next());
            islArmies.addArmy(ia);

            if (!facMap.containsKey(ia.owner)) {
              facMap.put(ia.owner, new ArrayList<Object>());
            }

            facMap.get(ia.owner).add(ia);
          }

          armies.add(islArmies);
        }
      }
    }

    ArmyComparator ac = new ArmyComparator();
    Collections.sort(armies, ac);

    // rest
    Collections.sort(allRegions, nameComp);

    Iterator<Region> regIt = allRegions.iterator();
    List<RegionArmies> allRegArmies = new ArrayList<RegionArmies>();

    while (regIt.hasNext()) {
      Region reg = regIt.next();
      if (reg.units().size() == 0) {
        continue;
      }
      Collection<Army> col = createRegionArmies(reg, cat);

      if (col.size() > 0) {
        RegionArmies regArmies = new RegionArmies(reg);
        Iterator<Army> colIt1 = col.iterator();
        Map<Faction, Army> map = new HashMap<Faction, Army>();

        while (colIt1.hasNext()) {
          Army army = colIt1.next();
          map.put(army.owner, army);
        }

        List<Faction> list = new ArrayList<Faction>(map.keySet());
        Collections.sort(list, compare);
        Iterator<Faction> colIt2 = list.iterator();

        while (colIt2.hasNext()) {
          Army army = map.get(colIt2.next());
          regArmies.addArmy(army);

          if (!facMap.containsKey(army.owner)) {
            facMap.put(army.owner, new ArrayList<Object>());
          }

          facMap.get(army.owner).add(army);
        }

        allRegArmies.add(regArmies);
      }
    }

    Collections.sort(allRegArmies, ac);
    armies.addAll(allRegArmies);

    // now create the WholeArmy objects
    if (facMap.size() > 0) {
      List<Faction> wholeList = new ArrayList<Faction>(facMap.keySet());
      Collections.sort(wholeList, compare);

      Iterator<Faction> facIt = wholeList.iterator();

      while (facIt.hasNext()) {
        Faction fac = facIt.next();
        WholeArmy wa = new WholeArmy(fac);
        Iterator<Object> facIt2 = facMap.get(fac).iterator();

        while (facIt2.hasNext()) {
          Object o = facIt2.next();

          if (o instanceof IslandArmy) {
            IslandArmy ia = (IslandArmy) o;
            IslandArmy ia2 = new IslandArmy(ia.owner, ia.island);
            Iterator<Army> facIt3 = ia.armies.iterator();

            while (facIt3.hasNext()) {
              Army army = facIt3.next();
              army = army.copy();
              army.shortString = true;
              ia2.addArmy(army);
            }

            ia2.shortString = false;
            wa.addIslandArmy(ia2);
          } else if (o instanceof Army) {
            Army army = (Army) o;
            army = army.copy();
            army.shortString = true;
            wa.addArmy(army);
          }
        }

        armies.add(wa);
      }
    }
  }

  protected void createRuleBase(GameData data) {
    if (weapons == null) {
      weapons = new HashMap<Skill, List<ItemType>>();
    } else {
      weapons.clear();
    }

    Rules r = data.getRules();
    weapon = r.getItemCategory(StringID.create("weapons"));

    if (weapon == null)
      return;

    front = r.getItemCategory(StringID.create("front weapons"));
    back = r.getItemCategory(StringID.create("distance weapons"));
    armourType = r.getItemCategory(StringID.create("armour"));
    shieldType = r.getItemCategory(StringID.create("shield"));

    for (Iterator<ItemType> iter = r.getItemTypeIterator(); iter.hasNext();) {
      ItemType it = iter.next();

      if ((it.getCategory() != null) && it.getCategory().isDescendant(weapon)) {
        Skill sk = it.getUseSkill();

        if (!weapons.containsKey(sk)) {
          weapons.put(sk, new LinkedList<ItemType>());
        }

        Collection<ItemType> col = weapons.get(sk);
        col.add(it);
      }
    }
  }

  protected Collection<Army> createRegionArmies(Region r, boolean cat) {
    Collection<Army> col = new LinkedList<Army>();

    if (r.units().size() == 0)
      return col;

    Iterator<Unit> unitIt = r.units().iterator();
    Map<Faction, Army> facMap = new HashMap<Faction, Army>();
    Map<Skill, Collection<Item>> unitMap = null;
    Collection<Skill> unitSkills = null;
    Collection<Item> armour = null;
    Collection<Item> nonSkillWeapons = null;

    while (unitIt.hasNext()) {
      Unit unit = unitIt.next();

      if (excludeSkills != null) {
        boolean doContinue = true;
        Iterator<SkillType> it = excludeSkills.iterator();

        while (doContinue && it.hasNext()) {
          SkillType sk = it.next();
          Skill skill = unit.getSkill(sk);

          if (skill != null /* && skill.getLevel()>0) */) {
            doContinue = false;
          }
        }

        if (!doContinue) {
          continue;
        }
      }

      if ((unit.getName() != null) && (excludeNames != null)) {
        boolean doContinue = true;
        Iterator<String> it = excludeNames.iterator();
        String name = unit.getName();

        while (doContinue && it.hasNext()) {
          String st = it.next();

          if (name.indexOf(st) != -1) {
            doContinue = false;
          }
        }

        if (!doContinue) {
          continue;
        }
      }

      if (TrustLevels.isPrivileged(unit.getFaction()) && (excludeCombatStates != null)) {
        boolean doContinue = true;
        Iterator<Integer> it = excludeCombatStates.iterator();

        while (doContinue && it.hasNext()) {
          Integer i = it.next();
          doContinue = i.intValue() != unit.getCombatStatus();
        }

        if (!doContinue) {
          continue;
        }
      }

      boolean inFront = unit.getCombatStatus() < 2;

      unitMap = getSkillsWithWeapons(unit, unitSkills = getWeaponSkills(unit, unitSkills), unitMap);
      nonSkillWeapons = getNonSkillWeapons(unit, unitMap.keySet(), nonSkillWeapons);
      armour = getArmour(unit, armour);

      if (!TrustLevels.isPrivileged(unit.getFaction())) {
        if ((unitMap.size() == 0) && (nonSkillWeapons.size() <= 1) && (armour.size() == 0)) {
          continue;
        }

        if (unitMap.size() == 0) {
          inFront = guessCombatState(nonSkillWeapons) == 0;
        }
      }

      if (!facMap.containsKey(unit.getFaction())) {
        facMap.put(unit.getFaction(), new Army(unit.getFaction(), r));
      }

      Army army = facMap.get(unit.getFaction());

      if (cat) {
        Collection<Skill> col2 = unitMap.keySet();
        int persons = unit.getPersons();
        int line = inFront ? 0 : 1;
        int maxSkillLevel = 0;
        Skill maxSkill = getHighestSkill(unit, col2);

        if (maxSkill != null) {
          maxSkillLevel = maxSkill.getLevel();
        }

        while (persons > 0) {
          if (col2.size() > 0) {
            maxSkill = getHighestSkill(unit, col2);

            Collection<Item> col3 = unitMap.get(maxSkill);
            col2.remove(maxSkill);

            Iterator<Item> itemIt = col3.iterator();

            while ((persons > 0) && itemIt.hasNext()) {
              Item item = itemIt.next();
              int amount = Math.min(persons, item.getAmount());
              persons -= amount;
              addArmoured(unit, amount, maxSkill.getLevel(), army, line, item.getItemType(), null,
                  armour, false, true);
            }
          } else {
            Iterator<Item> itemIt = nonSkillWeapons.iterator();

            while ((persons > 0) && itemIt.hasNext()) {
              Item item = itemIt.next();
              int amount = 0;

              if (item != null) {
                amount = Math.min(persons, item.getAmount());
              } else {
                amount = persons;
              }

              persons -= amount;
              addArmoured(unit, amount, maxSkillLevel - 2, army, line, (item != null) ? item
                  .getItemType() : null, null, armour, false, false);
            }
          }
        }
      } else { // uncategorized

        int persons = unit.getPersons();
        int unarmed = persons;
        Iterator<Collection<Item>> weaponIt1 = unitMap.values().iterator();

        while ((unarmed > 0) && weaponIt1.hasNext()) {
          Collection<Item> col2 = weaponIt1.next();
          Iterator<Item> colIt = col2.iterator();

          while ((unarmed > 0) && colIt.hasNext()) {
            Item item = colIt.next();
            unarmed -= item.getAmount();

            if (unarmed < 0) {
              unarmed = 0;
            }
          }
        }

        if ((unarmed > 0) && (nonSkillWeapons.size() > 1)) {
          Iterator<Item> weaponIt2 = nonSkillWeapons.iterator();

          while ((unarmed > 0) && weaponIt2.hasNext()) {
            Item item = weaponIt2.next();

            if (item != null) {
              unarmed -= item.getAmount();

              if (unarmed < 0) {
                unarmed = 0;
              }
            }
          }
        }

        addUnit(unit, persons, unarmed, army, inFront ? 0 : 1);
      }
    }

    if (facMap.size() > 0) {
      col.addAll(facMap.values());
    }

    facMap = null;

    return col;
  }

  protected int guessCombatState(Collection<Item> weapons) {
    int guess = 0; // guess front

    // now search for distance weapons
    if ((weapons != null) && (back != null)) {
      Iterator<Item> it = weapons.iterator();

      while (it.hasNext()) {
        Item item = it.next();

        if (item != null) {
          if ((item.getItemType().getCategory() != null)
              && item.getItemType().getCategory().isDescendant(back)) {
            guess = 2; // now guess back
          }
        }
      }
    }

    return guess;
  }

  protected WarLine getWarLine(Army army, int line, boolean cat) {
    if (army.warLines[line] == null) {
      army.setWarLine(new WarLine(line));
      army.warLines[line].categorized = cat;
    }

    return army.warLines[line];
  }

  protected boolean addArmoured(Unit unit, int amount, int skill, Army army, int line,
      ItemType weapon, ItemType armour, Collection<Item> armourCol, boolean shield, boolean hasSkill) {
    Iterator<Item> aIt = armourCol.iterator();

    boolean ret = false; // something deleted within, need a new iterator

    while ((amount > 0) && aIt.hasNext()) {
      Item aItem = aIt.next();

      if (shield) {
        if (aItem.getItemType().getCategory().isDescendant(shieldType)) {
          int aAmount = aItem.getAmount();

          if (aAmount > amount) {
            aItem.setAmount(aAmount - amount);
            addPartUnit(unit, amount, skill, army, line, weapon, armour, aItem.getItemType(),
                hasSkill, true);
            amount = 0;
          } else if (aAmount == amount) {
            aIt.remove();
            addPartUnit(unit, amount, skill, army, line, weapon, armour, aItem.getItemType(),
                hasSkill, true);
            amount = 0;
            ret = true;
          } else {
            addPartUnit(unit, aAmount, skill, army, line, weapon, armour, aItem.getItemType(),
                hasSkill, true);
            aIt.remove();
            amount -= aAmount;
            ret = true;
          }
        }
      } else {
        if (!aItem.getItemType().getCategory().isDescendant(shieldType)) {
          int aAmount = aItem.getAmount();

          if (aAmount > amount) {
            aItem.setAmount(aAmount - amount);

            if (addArmoured(unit, amount, skill, army, line, weapon, aItem.getItemType(),
                armourCol, true, hasSkill)) {
              aIt = armourCol.iterator();
            }

            amount = 0;
          } else if (aAmount == amount) {
            aIt.remove();

            if (addArmoured(unit, amount, skill, army, line, weapon, aItem.getItemType(),
                armourCol, true, hasSkill)) {
              aIt = armourCol.iterator();
            }

            amount = 0;
          } else {
            aIt.remove();
            amount -= aAmount;

            if (addArmoured(unit, aAmount, skill, army, line, weapon, aItem.getItemType(),
                armourCol, true, hasSkill)) {
              aIt = armourCol.iterator();
            }
          }
        }
      }
    }

    if (amount > 0) {
      if (shield) {
        addPartUnit(unit, amount, skill, army, line, weapon, armour, null, hasSkill, true);
      } else {
        addArmoured(unit, amount, skill, army, line, weapon, null, armourCol, true, hasSkill);
      }
    }

    return ret;
  }

  protected void addPartUnit(Unit full, int persons, int skill, Army army, int line,
      ItemType weapon, ItemType armour, ItemType shield, boolean cat) {
    addPartUnit(full, persons, skill, army, line, weapon, armour, shield, true, cat);
  }

  protected void addPartUnit(Unit full, int persons, int skill, Army army, int line,
      ItemType weapon, ItemType armour, ItemType shield, boolean hasSkill, boolean cat) {
    WarLine wl = getWarLine(army, line, cat);
    WeaponGroup wg = getWeaponGroup(wl, weapon);
    wg.addUnit(new PartUnit(full, weapon, persons, skill, armour, shield, hasSkill));
  }

  protected void addUnit(Unit unit, int persons, int unarmed, Army army, int line) {
    WarLine wl = getWarLine(army, line, false);
    wl.addUnit(unit, persons, unarmed);
  }

  protected Collection<Item> getNonSkillWeapons(Unit unit, Collection<Skill> used,
      Collection<Item> col) {
    if (col == null) {
      col = new LinkedList<Item>();
    } else {
      col.clear();
    }

    Iterator<Item> it1 = unit.getItems().iterator();

    while (it1.hasNext()) {
      Item item = it1.next();

      if ((item.getItemType().getCategory() != null)
          && item.getItemType().getCategory().isDescendant(weapon)) {
        col.add(item);
      }
    }

    if (used != null) {
      Iterator<Skill> it2 = used.iterator();

      while ((col.size() > 0) && it2.hasNext()) {
        SkillType st = it2.next().getSkillType();
        Iterator<Item> it3 = col.iterator();

        while (it3.hasNext()) {
          Item item = it3.next();

          if ((item.getItemType().getUseSkill() != null)
              && st.equals(item.getItemType().getUseSkill().getSkillType())) {
            it3.remove();
          }
        }
      }
    }

    col.add(null);

    return col;
  }

  protected Collection<Item> getArmour(Unit unit, Collection<Item> col) {
    if (col == null) {
      col = new LinkedList<Item>();
    } else {
      col.clear();
    }

    if (armourType == null)
      return col;

    Iterator<Item> it = unit.getItems().iterator();

    while (it.hasNext()) {
      Item item = it.next();

      if ((item.getItemType().getCategory() != null)
          && item.getItemType().getCategory().isDescendant(armourType)) {
        col.add(new Item(item.getItemType(), item.getAmount()));
      }
    }

    return col;
  }

  protected WeaponGroup getWeaponGroup(WarLine wl, ItemType weapon) {
    for (WeaponGroup wg : wl.groups) {
      if (wg.weapon == weapon)
        return wg;
    }

    WeaponGroup wg = new WeaponGroup(weapon);
    wl.addGroup(wg);

    return wg;
  }

  protected Collection<Skill> getWeaponSkills(Unit unit, Collection<Skill> col) {
    if (col == null) {
      col = new LinkedList<Skill>();
    } else {
      col.clear();
    }

    Iterator<Skill> it = weapons.keySet().iterator();

    while (it.hasNext()) {
      Skill sk = it.next();

      if (sk != null) {
        Skill usk = unit.getSkill(sk.getSkillType());

        if ((usk != null) && (usk.getLevel() >= sk.getLevel())) {
          col.add(usk);
        }
      }
    }

    return col;
  }

  protected Collection<Item> getWeaponsForSkill(Unit unit, Skill skill, Collection<Item> col) {
    if (col == null) {
      col = new LinkedList<Item>();
    } else {
      col.clear();
    }

    Collection<ItemType> col2 = null;
    Iterator<Skill> it1 = weapons.keySet().iterator();

    while (it1.hasNext()) {
      Skill sk = it1.next();
      // sk==null means weapon is not in rules
      if (sk == null
          || (sk.getSkillType().equals(skill.getSkillType()) && (skill.getLevel() >= sk.getLevel()))) {
        if (col2 == null) {
          col2 = weapons.get(sk);
        } else if (weapons.containsValue(col2)) {
          Collection<ItemType> col3 = new LinkedList<ItemType>(col2);
          col2 = col3;
          col2.addAll(weapons.get(sk));
        } else {
          col2.addAll(weapons.get(sk));
        }
      }
    }

    if (col2 == null) {
      ArmyStatsPanel.log.error("No weapons for skill " + skill);

      return col;
    }

    Iterator<ItemType> it2 = col2.iterator();

    while (it2.hasNext()) {
      ItemType type = it2.next();

      if (unit.getItem(type) != null) {
        col.add(unit.getItem(type));
      }
    }

    return col;
  }

  protected Map<Skill, Collection<Item>> getSkillsWithWeapons(Unit unit, Collection<Skill> skills,
      Map<Skill, Collection<Item>> map) {
    Collection<Item> col = null;
    Iterator<Skill> it = skills.iterator();

    if (map == null) {
      map = new HashMap<Skill, Collection<Item>>();
    } else {
      map.clear();
    }

    while (it.hasNext()) {
      Skill sk = it.next();
      col = getWeaponsForSkill(unit, sk, null);

      if (col.size() > 0) {
        map.put(sk, col);
      }
    }

    return map;
  }

  protected Skill getHighestSkill(Unit unit, Collection<Skill> col) {
    Skill maxSkill = null;
    int maxValue = -1;
    Iterator<Skill> it = col.iterator();

    while (it.hasNext()) {
      Skill sk = it.next();

      if (sk.getLevel() > maxValue) {
        maxValue = sk.getLevel();
        maxSkill = sk;
      }
    }

    return maxSkill;
  }

  /**
   * DOCUMENT-ME
   */
  public void valueChanged(javax.swing.event.TreeSelectionEvent treeSelectionEvent) {
    Object o = ((JTree) treeSelectionEvent.getSource()).getLastSelectedPathComponent();

    if ((o != null) && (o instanceof DefaultMutableTreeNode)) {
      Object o2 = ((DefaultMutableTreeNode) o).getUserObject();

      if (o2 instanceof UnitNodeWrapper) {
        dispatcher.fire(SelectionEvent.create(this, ((UnitNodeWrapper) o2).getUnit()));
      } else if (o2 instanceof SimpleNodeWrapper) {
        Object o3 = ((SimpleNodeWrapper) o2).getObject();

        if (o3 instanceof PartUnit) {
          dispatcher.fire(SelectionEvent.create(this, ((PartUnit) o3).parent));
        } else if (o3 instanceof WeaponGroup) {
          fireWeaponGroupSelection((WeaponGroup) o3);
        } else if (o3 instanceof WarLine) {
          fireWareLineSelection((WarLine) o3);
        } else if (o3 instanceof Army) {
          fireArmySelection((Army) o3);
        } else if (o3 instanceof RegionArmies) {
          dispatcher.fire(SelectionEvent.create(this, ((RegionArmies) o3).region));
        } else if (o3 instanceof IslandArmy) {
          dispatcher.fire(SelectionEvent.create(this, ((IslandArmy) o3).island,
              SelectionEvent.ST_DEFAULT));
        } else if (o3 instanceof IslandArmies) {
          dispatcher.fire(SelectionEvent.create(this, ((IslandArmies) o3).island,
              SelectionEvent.ST_DEFAULT));
        }
      } else if (o2 instanceof WarLine) {
        fireWareLineSelection((WarLine) o2);
      }
    }
  }

  protected Set<Unit> getWeaponGroupUnits(WeaponGroup g, Set<Unit> set) {
    if (set == null) {
      set = new HashSet<Unit>();
    }

    Iterator<PartUnit> it = g.units.iterator();

    while (it.hasNext()) {
      set.add(it.next().parent);
    }

    return set;
  }

  protected void fireWeaponGroupSelection(WeaponGroup w) {
    Set<Unit> s = getWeaponGroupUnits(w, null);
    if (s.size() > 0) {
      s.iterator().next();
    }

    dispatcher.fire(SelectionEvent.create(this, (Unit) null, s));
  }

  protected Set<Unit> getWarLineUnits(WarLine w, Set<Unit> set) {
    if (set == null) {
      set = new HashSet<Unit>();
    }

    if (w.categorized) {
      for (WeaponGroup wg : w.groups) {
        getWeaponGroupUnits(wg, set);
      }
    } else {
      set.addAll(w.units);
    }

    return set;
  }

  protected void fireWareLineSelection(WarLine w) {
    Set<Unit> s = getWarLineUnits(w, null);
    if (s.size() > 0) {
      s.iterator().next();
    }

    dispatcher.fire(SelectionEvent.create(this, null, s));
  }

  protected void fireArmySelection(Army a) {
    Set<Unit> set = new HashSet<Unit>();

    for (int i = 0; i < 2; i++) {
      if (a.warLines[i] != null) {
        set = getWarLineUnits(a.warLines[i], set);
      }
    }

    if (a.region != null) {
      dispatcher.fire(SelectionEvent.create(this, a.region));
    }
    if (a instanceof IslandArmy) {
      dispatcher.fire(SelectionEvent.create(this, ((IslandArmy) a).island,
          SelectionEvent.ST_DEFAULT));
    }
  }

  /**
   * Invoked when different objects are activated or selected.
   */
  public void selectionChanged(SelectionEvent e) {
    if ((e.getSource() != this) && (e.getSelectionType() == SelectionEvent.ST_REGIONS)) {
      lastSelected = CollectionFilters.filter(e.getSelectedObjects(), Region.class);
      updateData(getGameData(), lastSelected);
    }
  }

  protected class PartUnit {
    protected ItemType weapon;
    protected ItemType armour;
    protected ItemType shield;
    protected Unit parent;
    protected int persons = 0;
    protected int skill = 0;
    protected boolean hasSkill = true;

    /**
     * Creates a new PartUnit object.
     */
    public PartUnit(Unit p, ItemType w, int persons, int s, ItemType a, ItemType sh, boolean has) {
      parent = p;
      weapon = w;
      shield = sh;
      this.persons = persons;
      skill = s;
      hasSkill = has;
      armour = a;
    }

    /**
     * DOCUMENT-ME
     */
    @Override
    public String toString() {
      StringBuffer buf = new StringBuffer();
      buf.append(persons);
      buf.append(' ');
      buf.append(Resources.get("armystatspanel.soldiersofunit"));
      buf.append(' ');
      buf.append(parent.getName());

      if (weapon != null) {
        buf.append(' ');
        buf.append(Resources.get("armystatspanel.with"));
        buf.append(' ');
        buf.append(weapon.getName());
      } else {
        buf.append(", ");
        buf.append(Resources.get("armystatspanel.withoutweapon"));
      }

      if ((armour != null) || (shield != null)) {
        buf.append(", ");

        if (armour != null) {
          buf.append(armour.getName());

          if (shield != null) {
            buf.append(' ');
            buf.append(Resources.get("armystatspanel.and"));
            buf.append(' ');
            buf.append(shield.getName());
          }
        } else {
          buf.append(shield.getName());
        }
      }

      if (parent.getSkillMap() != null) {
        buf.append(", ");
        buf.append(Resources.get("armystatspanel.skill"));
        buf.append(' ');

        if (skill > 0) {
          buf.append(skill);
        } else {
          buf.append(Resources.get("armystatspanel.missing"));
        }

        if (!hasSkill) {
          buf.append('(');
          buf.append(Resources.get("armystatspanel.wrongskill"));
          buf.append(')');
        }
      }

      return buf.toString();
    }
  }

  protected class WeaponGroup {
    protected ItemType weapon;
    protected List<PartUnit> units;

    /**
     * Creates a new WeaponGroup object.
     */
    public WeaponGroup(ItemType w) {
      weapon = w;
      units = new LinkedList<PartUnit>();
    }

    /**
     * DOCUMENT-ME
     */
    public void addUnit(PartUnit p) {
      units.add(p);
    }

    /**
     * DOCUMENT-ME
     */
    public int getMen() {
      int men = 0;
      Iterator<PartUnit> it = units.iterator();

      while (it.hasNext()) {
        PartUnit pu = it.next();
        men += pu.persons;
      }

      return men;
    }

    /**
     * DOCUMENT-ME
     */
    @Override
    public String toString() {
      StringBuffer buf = new StringBuffer();
      buf.append(getMen());
      buf.append(' ');
      buf.append(Resources.get("armystatspanel.soldiers"));

      if (weapon != null) {
        buf.append(' ');
        buf.append(Resources.get("armystatspanel.with"));
        buf.append(' ');
        buf.append(weapon.getName());
      } else {
        buf.append(", ");
        buf.append(Resources.get("armystatspanel.withoutweapon"));
      }

      return buf.toString();
    }
  }

  protected class WarLine {
    protected int lineType;
    protected List<WeaponGroup> groups;
    protected List<Unit> units;
    protected boolean categorized = true;
    protected int men = 0;
    protected int unarmed = 0;

    /**
     * Creates a new WarLine object.
     */
    public WarLine(int lt) {
      groups = new LinkedList<WeaponGroup>();
      units = new LinkedList<Unit>();
      lineType = lt;
    }

    /**
     * DOCUMENT-ME
     */
    public void addGroup(WeaponGroup w) {
      groups.add(w);
    }

    /**
     * DOCUMENT-ME
     * 
     * @deprecated
     */
    @Deprecated
    public void addUnit(Unit u, int persons, int unarmed) {
      units.add(u);
      men += persons;
      this.unarmed += unarmed;
    }

    /**
     * DOCUMENT-ME
     */
    public int getMen() {
      if (categorized) {
        int men = 0;
        for (WeaponGroup wg : groups) {
          men += wg.getMen();
        }

        return men;
      } else
        return men;
    }

    /**
     * DOCUMENT-ME
     */
    public int getUnarmed() {
      if (categorized) {
        int unarmed = 0;
        for (WeaponGroup wg : groups) {
          if (wg.weapon == null) {
            unarmed += wg.getMen();
          }
        }

        return unarmed;
      } else
        return unarmed;
    }

    /**
     * DOCUMENT-ME
     */
    @Override
    public String toString() {
      StringBuffer buf = new StringBuffer();

      if (lineType == 0) {
        buf.append(Resources.get("armystatspanel.frontline"));
      } else {
        buf.append(Resources.get("armystatspanel.backline"));
      }

      buf.append(", ");
      buf.append(getMen());
      buf.append(' ');
      buf.append(Resources.get("armystatspanel.soldiers"));

      int unarmed = getUnarmed();

      if (unarmed > 0) {
        buf.append('(');
        buf.append(unarmed);
        buf.append(' ');
        buf.append(Resources.get("armystatspanel.withoutweapon"));
        buf.append(')');
      }

      return buf.toString();
    }
  }

  protected class Army {
    protected WarLine warLines[];
    protected Faction owner;
    protected Region region;
    protected boolean shortString = false;

    /**
     * Creates a new Army object.
     */
    public Army(Faction o, Region r) {
      owner = o;
      region = r;
      warLines = new WarLine[2];
    }

    /**
     * DOCUMENT-ME
     */
    public void setWarLine(WarLine w) {
      warLines[w.lineType] = w;
    }

    /**
     * DOCUMENT-ME
     */
    public int getMen() {
      int men = 0;

      if (warLines[0] != null) {
        men += warLines[0].getMen();
      }

      if (warLines[1] != null) {
        men += warLines[1].getMen();
      }

      return men;
    }

    /**
     * DOCUMENT-ME
     */
    public int getUnarmed() {
      int unarmed = 0;

      if (warLines[0] != null) {
        unarmed += warLines[0].getUnarmed();
      }

      if (warLines[1] != null) {
        unarmed += warLines[1].getUnarmed();
      }

      return unarmed;
    }

    protected Army copy() {
      Army a = new Army(owner, region);
      a.warLines = warLines;

      return a;
    }

    /**
     * DOCUMENT-ME
     */
    @Override
    public String toString() {
      StringBuffer buf = new StringBuffer();

      if (!shortString) {
        buf.append(owner.toString());
      } else {
        buf.append(region.toString());
      }

      buf.append(": ");
      buf.append(getMen());
      buf.append(' ');
      buf.append(Resources.get("armystatspanel.soldiers"));

      int unarmed = getUnarmed();

      if (unarmed > 0) {
        buf.append('(');
        buf.append(unarmed);
        buf.append(' ');
        buf.append(Resources.get("armystatspanel.withoutweapon"));
        buf.append(')');
      }

      return buf.toString();
    }
  }

  protected interface Armies {
    public String getName();
  }

  protected class RegionArmies implements Armies {
    protected Region region;
    protected List<Army> armies;
    protected int men = 0;
    protected int unarmed = 0;

    /**
     * Creates a new RegionArmies object.
     */
    public RegionArmies(Region r) {
      region = r;
      armies = new LinkedList<Army>();
    }

    /**
     * DOCUMENT-ME
     */
    public void addArmy(Army a) {
      armies.add(a);
      men += a.getMen();
      unarmed += a.getUnarmed();
    }

    public String getName() {
      return region.getName();
    }

    /**
     * DOCUMENT-ME
     */
    @Override
    public String toString() {
      StringBuffer buf = new StringBuffer(region.toString());
      buf.append('(');
      buf.append(men);
      buf.append(' ');
      buf.append(Resources.get("armystatspanel.soldiers"));

      if (unarmed > 0) {
        buf.append(", ");
        buf.append(unarmed);
        buf.append(' ');
        buf.append(Resources.get("armystatspanel.withoutweapon"));
      }

      buf.append(')');

      return buf.toString();
    }
  }

  protected class IslandArmy extends Army {
    protected Faction owner;
    protected Island island;
    protected List<Army> armies;
    protected boolean shortString = true;
    protected int men = 0;
    protected int unarmed = 0;

    /**
     * Creates a new IslandArmy object.
     */
    public IslandArmy(Faction o, Island i) {
      super(o, null);
      owner = o;
      island = i;
      armies = new LinkedList<Army>();
    }

    /**
     * DOCUMENT-ME
     */
    public void addArmy(Army a) {
      armies.add(a);
      men += a.getMen();
      unarmed += a.getUnarmed();
    }

    @Override
    protected IslandArmy copy() {
      IslandArmy o = new IslandArmy(owner, island);
      o.armies = armies;
      o.men = men;
      o.unarmed = unarmed;

      return o;
    }

    /**
     * DOCUMENT-ME
     */
    @Override
    public String toString() {
      StringBuffer buf = new StringBuffer();

      if (!shortString) {
        buf.append(island.getName());
      } else {
        buf.append(owner.toString());
      }

      buf.append(": ");
      buf.append(men);
      buf.append(' ');
      buf.append(Resources.get("armystatspanel.soldiers"));

      if (unarmed > 0) {
        buf.append('(');
        buf.append(unarmed);
        buf.append(' ');
        buf.append(Resources.get("armystatspanel.withoutweapon"));
        buf.append(')');
      }

      return buf.toString();
    }
  }

  protected class IslandArmies implements Armies {
    protected Island island;
    protected List<IslandArmy> iarmies;
    protected List<RegionArmies> rarmies;
    protected int men = 0;
    protected int unarmed = 0;

    /**
     * Creates a new IslandArmies object.
     */
    public IslandArmies(Island i) {
      island = i;
      iarmies = new LinkedList<IslandArmy>();
      rarmies = new LinkedList<RegionArmies>();
    }

    /**
     * DOCUMENT-ME
     */
    public void addArmy(IslandArmy army) {
      iarmies.add(army);
      men += army.men;
      unarmed += army.unarmed;
    }

    public String getName() {
      return island.getName();
    }

    /**
     * DOCUMENT-ME
     */
    @Override
    public String toString() {
      StringBuffer buf = new StringBuffer(island.getName());
      buf.append(" (");
      buf.append(men);
      buf.append(Resources.get("armystatspanel.soldiers"));

      if (unarmed > 0) {
        buf.append(", ");
        buf.append(unarmed);
        buf.append(' ');
        buf.append(Resources.get("armystatspanel.withoutweapon"));
      }

      buf.append(')');

      return buf.toString();
    }
  }

  protected class WholeArmy implements Armies {
    protected Faction owner;
    protected List<Army> armies;
    protected int men = 0;
    protected int unarmed = 0;

    /**
     * Creates a new WholeArmy object.
     */
    public WholeArmy(Faction o) {
      owner = o;
      armies = new LinkedList<Army>();
    }

    /**
     * DOCUMENT-ME
     */
    public void addArmy(Army a) {
      armies.add(a);
      men += a.getMen();
      unarmed += a.getUnarmed();
    }

    /**
     * DOCUMENT-ME
     */
    public void addIslandArmy(IslandArmy ia) {
      armies.add(ia);
      men += ia.men;
      unarmed += ia.unarmed;
    }

    public String getName() {
      return owner.getName();
    }

    /**
     * DOCUMENT-ME
     */
    @Override
    public String toString() {
      StringBuffer buf = new StringBuffer(Resources.get("armystatspanel.armeeof"));
      buf.append(' ');
      buf.append(owner.toString());
      buf.append(" (");
      buf.append(men);
      buf.append(' ');
      buf.append(Resources.get("armystatspanel.soldiers"));

      if (unarmed > 0) {
        buf.append(", ");
        buf.append(unarmed);
        buf.append(' ');
        buf.append(Resources.get("armystatspanel.withoutweapon"));
      }

      buf.append(')');

      return buf.toString();
    }
  }

  protected class ArmyComparator implements Comparator<Armies> {
    /**
     * DOCUMENT-ME
     */
    public int compare(Armies o1, Armies o2) {
      String s1 = null;
      String s2 = null;

      if (o1 != null) {
        s1 = o1.getName();
      }
      if (o2 != null) {
        s2 = o2.getName();
      }

      if (s1 == null) {
        if (s2 == null)
          return 0;
        return -1;
      } else {
        if (s2 == null)
          return 1;
        return s1.compareTo(s2);
      }
    }

  }

}
