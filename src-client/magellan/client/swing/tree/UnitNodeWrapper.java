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

package magellan.client.swing.tree;

import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;

import javax.swing.Icon;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JMenu;
import javax.swing.JMenuItem;

import magellan.client.swing.context.ContextChangeable;
import magellan.client.swing.context.ContextObserver;
import magellan.library.Faction;
import magellan.library.Item;
import magellan.library.Rules;
import magellan.library.Skill;
import magellan.library.Unit;
import magellan.library.rules.Category;
import magellan.library.utils.Resources;
import magellan.library.utils.comparator.ItemCategoryComparator;
import magellan.library.utils.comparator.SkillComparator;
import magellan.library.utils.comparator.SkillRankComparator;
import magellan.library.utils.logging.Logger;

/**
 * A UnitNodeWrapper serves as an abstraction layer between a tree cell renderer and the unit to
 * render. It manages a list of icons and text(s) that are to be displayed. It maintains a
 * {@link UnitNodeWrapper.UnitNodeWrapperDrawPolicy} adapter which governs many of the object's
 * properties.
 *
 * @author $Author: $
 * @version $Revision: 288 $
 */
public class UnitNodeWrapper extends DefaultNodeWrapper implements CellObject2, SupportsClipboard {
  private static final Comparator<Skill> skillComparator = new SkillComparator();
  private static Comparator<Skill> rankComparator = null;

  private Unit unit = null;
  private long amount = -1;
  private long modified = -1;
  private String prfx = null;
  private String text = null;
  private boolean iconNamesCreated = false;
  private List<GraphicsElement> iconNames = null;
  private Boolean reverse;
  // private String additionalIcon = null;
  private List<String> additionalIcons;
  private UnitNodeWrapperDrawPolicy adapter;

  /**
   * Creates a new UnitNodeWrapper object. The text for the unit is generated from the unit's name
   * and ID and from the arguments.
   *
   * @param u The unit
   * @param prfx A text that is displayed in front of the name
   * @param num The number of persons
   * @param mod The modified number of persons
   */
  public UnitNodeWrapper(Unit u, String prfx, long num, long mod) {
    unit = u;
    amount = num;
    modified = mod;
    this.prfx = prfx;
    // this.text = getText(u, prfx, num, mod);
  }

  /**
   * Creates a new UnitNodeWrapper with specified text.
   *
   * @param u The unit
   * @param text The text that is displayed for the unit
   */
  public UnitNodeWrapper(Unit u, String text) {
    unit = u;
    this.text = text;
  }

  /**
   * Returns the unit represented by the wrapper.
   */
  public Unit getUnit() {
    return unit;
  }

  /**
   * Returns a string representation of the object, which is either pre-set by the constructor or
   * generated from the unit name, id, amount and modified amount (if applicable).
   */
  @Override
  public String toString() {
    return text != null ? text : UnitNodeWrapper.getText(unit, prfx, amount, modified);
  }

  /**
   * Specifies an additional icon that is displayed in front of the unit text.
   */
  public void addAdditionalIcon(String icon) {
    if (additionalIcons == null) {
      additionalIcons = new LinkedList<String>();
    }
    // added in reverse order, for more efficiency in getGraphicsElements()
    additionalIcons.add(0, icon);
  }

  /**
   * Returns <code>null</code>.
   *
   * @see magellan.client.swing.tree.CellObject#getIconNames()
   */
  public List<String> getIconNames() {
    return null;
  }

  /**
   * Returns <code>true</code> if the unit belongs to a priveleged faction and the unit's orders or
   * one of the subordinate element's orders are unconfirmed.
   *
   * @see magellan.client.swing.tree.CellObject#emphasized()
   */
  @Override
  public boolean emphasized() {
    Faction f = unit.getFaction();

    if ((f != null) && f.isPrivileged()) {
      if (!unit.isOrdersConfirmed())
        return true;

      for (SupportsEmphasizing se : getSubordinatedElements()) {
        if (se.emphasized())
          return true;
      }
    }

    return false;
  }

  /**
   * Returns the according option of the draw policy.
   */
  public boolean isShowingAdditional() {
    return adapter.properties[UnitNodeWrapperDrawPolicy.SHOW_ADDITIONAL];
  }

  /**
   * Returns the accordant option of the draw policy.
   */
  public boolean isShowingContainerIcons() {
    return isShowingAdditional() && adapter.properties[UnitNodeWrapperDrawPolicy.SHOW_CONTAINER];
  }

  /**
   * Returns the accordant option of the draw policy.
   */
  public boolean isShowingSkillIcons() {
    return isShowingAdditional() && adapter.properties[UnitNodeWrapperDrawPolicy.SHOW_SKILL];
  }

  /**
   * Returns the accordant option of the draw policy.
   */
  public boolean isShowingOtherIcons() {
    return isShowingAdditional() && adapter.properties[UnitNodeWrapperDrawPolicy.SHOW_OTHER];
  }

  /**
   * Returns the accordant option of the draw policy.
   */
  public boolean isShowingIconText() {
    return isShowingAdditional() && adapter.properties[UnitNodeWrapperDrawPolicy.SHOW_TEXT];
  }

  /**
   * Return <code>true</code> iff skills with level less than one should be shown.
   *
   * @return <code>true</code> iff skills with level less than one should be shown
   */
  public boolean isShowingSkillsLessThanOne() {
    return isShowingAdditional()
        && adapter.properties[UnitNodeWrapperDrawPolicy.SHOW_SKILL_LESS_ONE];
  }

  /**
   * Return <code>true</code> iff skills with level less than two should be shown.
   *
   * @return <code>true</code> iff skills with level less than two should be shown
   */
  public boolean isShowingSkillsLessThanTwo() {
    return isShowingAdditional()
        && adapter.properties[UnitNodeWrapperDrawPolicy.SHOW_SKILL_LESS_TWO];
  }

  /**
   * Returns the maximum number of skill icons that should be shown.
   *
   * @return The maximum number of skill icons that should be shown.
   */
  public int numberOfShownSkills() {
    if (!adapter.properties[UnitNodeWrapperDrawPolicy.NUMBER_OF_SHOWN_SKILLS])
      return Integer.MAX_VALUE;

    for (int i = 1; i <= 5; ++i) {
      if (!adapter.properties[UnitNodeWrapperDrawPolicy.NUMBER_OF_SHOWN_SKILLS + i])
        return i - 1;
    }
    return 5;
  }

  /**
   * Returns the accordant option of the draw policy.
   */
  public boolean isShowingExpectedOnly() {
    return isShowingAdditional()
        && adapter.properties[UnitNodeWrapperDrawPolicy.SHOW_EXPECTED_ONLY];
  }

  /**
   * Returns the accordant option of the draw policy.
   */
  public boolean isShowingChanges() {
    return adapter.properties[UnitNodeWrapperDrawPolicy.SHOW_CHANGES];
  }

  /**
   * Returns the accordant option of the draw policy.
   */
  public boolean isShowingChangesStyled() {
    return isShowingChanges() && adapter.properties[UnitNodeWrapperDrawPolicy.SHOW_CHANGE_STYLED];
  }

  /**
   * Returns the accordant option of the draw policy.
   */
  public boolean isShowingChangesText() {
    return isShowingChanges() && adapter.properties[UnitNodeWrapperDrawPolicy.SHOW_CHANGE_TEXT];
  }

  /**
   * Returns the accordant option of the draw policy.
   */
  public boolean isShowingCategorized() {
    return adapter.properties[UnitNodeWrapperDrawPolicy.SHOW_CATEGORIZED];
  }

  /**
   * Returns the accordant option of the draw policy.
   */
  public boolean isShowingCategorized(int type) {
    if (type < 0)
      return false;
    return adapter.properties[UnitNodeWrapperDrawPolicy.CATEGORIZE_START + type];
  }

  /**
   * Generates a text which is composed of the prefix, the units name and id, and amount1 and
   * amount2.
   *
   * @param u
   * @param prefix
   * @param amount1
   * @param amount2
   * @return
   */
  private static String getText(Unit u, String prefix, long amount1, long amount2) {
    StringBuffer sb = new StringBuffer();

    if (prefix != null) {
      sb.append(prefix);
    }

    sb.append(u.toString());

    if (amount1 > -1) {
      sb.append(": ").append(amount1);

      if ((amount2 > -1) && (amount2 != amount1)) {
        sb.append(" (").append(amount2).append(")");
      }
    }

    return sb.toString();
  }

  private List<GraphicsElement> createGraphicsElements(Unit u) {
    List<GraphicsElement> names = new LinkedList<GraphicsElement>();
    List<Skill> skills = new LinkedList<Skill>();

    if (isShowingSkillIcons() && (u.getSkills() != null)) {
      boolean bool = isShowingSkillsLessThanOne();
      boolean bool2 = isShowingSkillsLessThanTwo();
      for (Skill s : u.getSkills()) {
        boolean addSkill = true;
        // maybe show skills if above user-defined threshold; also respect big changes
        if (s.getLevel() < 1 && s.getChangeLevel() >= -1 && !bool) {
          addSkill = false;
        }
        if (s.getLevel() < 2 && s.getChangeLevel() >= -2 && !bool2) {
          addSkill = false;
        }
        if (addSkill) {
          skills.add(s);
        }
      }

      if (adapter.getSettings().getProperty("EMapOverviewPanel.useBestSkill", "true")
          .equalsIgnoreCase("true")) {
        // use best skill to sort icons
        Collections.sort(skills, UnitNodeWrapper.skillComparator);
      } else {
        // use skilltyperanking to sort icons
        Collections.sort(skills, UnitNodeWrapper.rankComparator);
      }
    }

    List<Item> others = null;

    if (isShowingOtherIcons()) {
      others = new LinkedList<Item>(u.getModifiedItems());
      // sort items by category
      Collections.sort(others, ItemCategoryComparator.getInstance());
    }

    // main
    Tag2Element.start(u);

    GraphicsElement start = new UnitGraphicsElement(toString());
    start.setType(GraphicsElement.MAIN);

    Tag2Element.apply(start);

    GraphicsElement ge = null;

    // Fiete Test: temps, die gefaellt werden..haben geburtstag
    // Fiete removed 20060911 (creator wishes to use the icon for the item only
    /**
     * if (u.getPersons()==0 && u.getModifiedPersons()&gt;0){ names.add(new GraphicsElement(null, null,
     * "geburtstag")); }
     */

    if (isShowingContainerIcons()) {
      if (unit.getBuilding() != null && unit.getBuilding().getType() != null) {
        ge = new GraphicsElement(null, null, unit.getBuilding().getType().getIcon());
        ge.setTooltip(unit.getBuilding().getName());
        ge.setType(GraphicsElement.ADDITIONAL);
        names.add(ge);
      }

      if (unit.getShip() != null) {
        if (unit.getShip().getType() == null) {
          ge = new GraphicsElement(null, null, "ERROR");
        } else {
          ge = new GraphicsElement(null, null, unit.getShip().getType().getIcon());
          ge.setTooltip(unit.getShip().getName());
          ge.setType(GraphicsElement.ADDITIONAL);
        }
        names.add(ge);
      }
    }

    // Heldenanzeige
    if (u.isHero()) {
      names.add(new GraphicsElement(null, null, "hero"));
    }

    // skills
    int skillCounter = 0;
    for (Iterator<Skill> iter = skills.iterator(); iter.hasNext()
        && skillCounter < numberOfShownSkills();) {
      Skill s = iter.next();
      skillCounter++;
      ge = null;

      if (isShowingIconText()) {
        ge =
            new GraphicsElement(Integer.valueOf(s.getLevel()), null, null, s.getSkillType()
                .getIcon());

        if (s.isLostSkill()) {
          ge.setObject("-");
        }
      } else {
        ge = new GraphicsElement(null, null, s.getSkillType().getIcon());
      }

      ge.setTooltip(s.getSkillType().getName());
      ge.setType(GraphicsElement.ADDITIONAL);

      if (isShowingChanges() && (s.isLevelChanged() || !u.isDetailsKnown())) {
        if (isShowingChangesStyled()) {
          if (s.isLevelChanged()) {
            ge.setStyleset(CellRenderer.SKILL_CHANGE_STYLE_PREFIX
                + ((s.getChangeLevel() >= 0) ? ">." : "<.")
                + CellRenderer.SKILL_CHANGE_STYLE_PREFIX + String.valueOf(s.getChangeLevel()));
          } else {
            ge.setStyleset(CellRenderer.STYLE_NAMES[CellRenderer.TALENT_UNKNOWN_STYLE]);
          }
        }

        if (isShowingChangesText() && isShowingIconText()) {
          ge.setObject(ge.getObject().toString() + "(" + ((s.getChangeLevel() >= 0) ? "+" : "")
              + String.valueOf(s.getChangeLevel()) + ")");
        }
      } else {
        Tag2Element.apply(ge);
      }

      names.add(ge);
    }

    // items
    if (others != null) {
      if (isShowingCategorized()) {
        adapter.sortCategories(u.getData().getRules());
      }

      int currentCategory = -1, count = 0;
      StringBuilder buffer = new StringBuilder();
      String iconName = null;

      for (Item s : others) {
        if (isShowingExpectedOnly()) {
          if (s.getAmount() <= 0) {
            // skip "empty" items
            continue;
          }
        }

        ge = null;

        int newCategory = getCategory(s);
        if (isShowingCategorized()) {
          if (currentCategory >= 0 && currentCategory != newCategory) {
            if (iconName != null) {
              // new category starts, add old category node
              names.add(createGE(count, iconName, buffer));
            }
          }
          if (newCategory != currentCategory)
            if (isShowingCategorized(newCategory)) {
              // init new category node
              buffer.setLength(0);
              count = 0;
              iconName = "items/" + s.getItemType().getIcon();
            } else {
              iconName = null;
            }

          if (isShowingCategorized(newCategory)) {
            // append to current node
            if (buffer.length() > 0) {
              buffer.append(',');
            }
            buffer.append(s.getAmount());
            buffer.append(' ');
            buffer.append(s.getName());

            if (count > 0) {
              iconName =
                  magellan.library.utils.Umlaut.convertUmlauts(adapter.categories[newCategory]);
              if (iconName == null) {
                Logger.getInstance(this.getClass()).warn(
                    "category without icon: " + adapter.categories[newCategory]);
                iconName = "items/" + s.getItemType().getIcon();
              }
            }
            count += s.getAmount();
          }
        }
        currentCategory = newCategory;
        if (!isShowingCategorized() || !isShowingCategorized(currentCategory)) {
          // add uncategorized item
          if (isShowingIconText()) {
            ge = new GraphicsElement(null, null, null, "items/" + s.getItemType().getIcon());

            Item oldItem = u.getItem(s.getItemType());
            long oldAmount = 0;

            if (oldItem != null) {
              oldAmount = oldItem.getAmount();
            }

            if (s.isChanged()) {
              if (isShowingExpectedOnly()) {
                // only show expected future value
                ge.setObject(String.valueOf(s.getAmount()));
              } else {
                ge.setObject(String.valueOf(oldAmount) + "(" + String.valueOf(s.getAmount()) + ")");
              }
            } else {
              if (oldAmount == 0) {
                continue;
              }

              ge.setObject(Long.valueOf(oldAmount));
            }
          } else {
            ge = new GraphicsElement(null, null, "items/" + s.getItemType().getIcon());
          }

          ge.setTooltip(s.getName());
          ge.setType(GraphicsElement.ADDITIONAL);
          Tag2Element.apply(ge);
          names.add(ge);
        }
      }
      if (iconName != null) {
        // add last category node
        names.add(createGE(count, iconName, buffer));
      }
    }

    if (reverseOrder()) {
      names.add(0, start);
    } else {
      names.add(start);
    }

    if (additionalIcons != null) {
      for (String addIcon : additionalIcons) {
        names.add(0, new GraphicsElement(null, null, addIcon));
      }
    }

    // ease garbage collection
    skills.clear();

    // others.clear();
    return names;
  }

  private int getCategory(Item s) {
    if (s.getItemType().getCategory() == null)
      return -1;

    // Integer val = itemCatMap.get(s.getItemType());
    // if (val != null)
    // return val;

    Category cat = s.getItemType().getCategory();
    while (cat != null) {
      for (int i = 0; i < adapter.categories.length; ++i) {
        if (adapter.categories[i].equals(cat.getID().toString()))
          // itemCatMap.put(s.getItemType(), i);
          return i;
      }
      cat = cat.getParent();
    }
    // itemCatMap.put(s.getItemType(), -1);
    return -1;
  }

  private GraphicsElement createGE(long count, String iconName, StringBuilder buffer) {
    GraphicsElement ge;
    if (isShowingIconText()) {
      ge = new GraphicsElement(Long.valueOf(count), null, null, iconName);
    } else {
      ge = new GraphicsElement(null, null, iconName);
    }
    ge.setTooltip(buffer.toString());
    ge.setType(GraphicsElement.ADDITIONAL);
    Tag2Element.apply(ge);
    return ge;
  }

  /**
   * Specifies if the icons should be displayed in reverse order. This overrides the settings if the
   * {@link UnitNodeWrapper.UnitNodeWrapperDrawPolicy}.
   *
   * @param bool
   */
  public void setReverseOrder(boolean bool) {
    reverse = bool ? Boolean.TRUE : Boolean.FALSE;
  }

  /**
   * Returns <code>true</code> if the icons should be displayed in reverse order.
   */
  public boolean reverseOrder() {
    if (reverse != null)
      return reverse.booleanValue();

    return adapter.properties[UnitNodeWrapperDrawPolicy.SHOW_NAMEFIRST];
  }

  /**
   * Clears the collection of icons and indicates that it should be re-created.
   */
  public void clearBuffer() {
    if (iconNames != null) {
      iconNames.clear();
      iconNames = null;
    }

    iconNamesCreated = false;
  }

  /**
   * Indicates that the list icons should be re-created
   *
   * @see magellan.client.swing.tree.CellObject#propertiesChanged()
   */
  public void propertiesChanged() {
    clearBuffer();
  }

  /**
   * @see magellan.client.swing.tree.CellObject#init(java.util.Properties,
   *      magellan.client.swing.tree.NodeWrapperDrawPolicy)
   */
  public NodeWrapperDrawPolicy init(Properties settings, NodeWrapperDrawPolicy newAdapter) {
    return init(settings, "UnitNodeWrapper", newAdapter);
  }

  /**
   * @see magellan.client.swing.tree.CellObject#init(java.util.Properties, java.lang.String,
   *      magellan.client.swing.tree.NodeWrapperDrawPolicy)
   */
  public NodeWrapperDrawPolicy init(Properties settings, String prefix,
      NodeWrapperDrawPolicy newAdapter) {
    // return the adapter
    if (newAdapter == null) {
      newAdapter = new UnitNodeWrapperDrawPolicy(settings, prefix);
    }

    newAdapter.addCellObject(this);
    adapter = (UnitNodeWrapperDrawPolicy) newAdapter;

    if (UnitNodeWrapper.rankComparator == null) {
      UnitNodeWrapper.rankComparator = new SkillRankComparator(null, settings);
    }

    return newAdapter;
  }

  /**
   * Returns the list of of {@link GraphicsElement}s that should be displayed.
   *
   * @see magellan.client.swing.tree.CellObject2#getGraphicsElements()
   */
  public List<GraphicsElement> getGraphicsElements() {
    if (!iconNamesCreated) {
      iconNames = createGraphicsElements(unit);
      iconNamesCreated = true;
    }

    return iconNames;
  }

  /**
   * @see magellan.client.swing.tree.CellObject2#getLabelPosition()
   */
  public int getLabelPosition() {
    return additionalIcons == null ? reverseOrder() ? 0 : (iconNames.size() - 1) : reverseOrder()
        ? (additionalIcons.size()) : (iconNames.size() + additionalIcons.size() - 1);
  }

  private static class UnitNodeWrapperDrawPolicy extends DetailsNodeWrapperDrawPolicy implements
  ContextChangeable, ActionListener {
    /** Show additional icons */
    public static final int SHOW_ADDITIONAL = 0;

    /** Show unit's container */
    public static final int SHOW_CONTAINER = 1;

    /** Show skills */
    public static final int SHOW_SKILL = 2;

    /** Show skills less than one */
    public static final int SHOW_SKILL_LESS_ONE = 3;

    /** Show skills less than two */
    public static final int SHOW_SKILL_LESS_TWO = 4;

    /** DOCUMENT-ME */
    public static final int SHOW_OTHER = 5;

    /** Show additional text */
    public static final int SHOW_TEXT = 6;

    /** Show name before icons */
    public static final int SHOW_NAMEFIRST = 7;

    /** Show only expected, not current stuff */
    public static final int SHOW_EXPECTED_ONLY = 8;

    /** option for showing only first x skills */
    public static final int NUMBER_OF_SHOWN_SKILLS = 9;

    /** positions for show one/two/three skills */
    public static final int NUMBER_OF_SHOWN_SKILLS_START = 10;

    /** positions for show one/two/three skills */
    public static final int NUMBER_OF_SHOWN_SKILLS_END = 14;

    /** show (item/skill) changes */
    public static final int SHOW_CHANGES = 15;

    /** apply styles to changes */
    public static final int SHOW_CHANGE_STYLED = 16;

    /** show changes as (text) */
    public static final int SHOW_CHANGE_TEXT = 17;

    /** show items categorized */
    public static final int SHOW_CATEGORIZED = 18;

    /** start index for various categories */
    public static final int CATEGORIZE_START = 19;

    /** Number of said categories */
    public static final int NUMBER_OF_CATEGORIES = 7;

    protected String categories[] = { "weapons", "armour", "resources", "luxuries", "herbs",
        "potions", "misc" };

    // for menu use
    protected ContextObserver obs;
    protected JMenu contextMenu;
    protected JCheckBoxMenuItem itemItem;
    protected JCheckBoxMenuItem skillItem;

    /**
     * Creates a new UnitNodeWrapperDrawPolicy object.
     */
    public UnitNodeWrapperDrawPolicy(Properties settings, String prefix) {
      super(4, new int[] { 8, 5, 2, 7 }, settings, prefix, new String[][] {
          { "showAdditional", "true" }, { "showContainerIcons", "true" },
          { "showSkillIcons", "true" }, { "showSkillLessThanOneIcons", "false" },
          { "showSkillLessThanTwoIcons", "true" }, { "showOtherIcons", "true" },
          { "showIconText", "true" }, { "showNamesFirst", "false" },
          { "showExpectedOnly", "false" },

          { "showHighest", "false" }, { "showHighest.1", "true" }, { "showHighest.2", "true" },
          { "showHighest.3", "false" }, { "showHighest.4", "false" }, { "showHighest.5", "false" },

          { "showChanges", "true" }, { "showChangesStyled", "true" },
          { "showChangesText", "false" },

          { "showCategorized", "false" }, { "showCategorized.0", "false" },
          { "showCategorized.1", "false" }, { "showCategorized.2", "false" },
          { "showCategorized.3", "false" }, { "showCategorized.4", "false" },
          { "showCategorized.5", "false" }, { "showCategorized.6", "false" },

          // { "showWarnings", "false" }
      }, new String[] { "prefs.additional.text", "prefs.container.text", "prefs.skill.text",
          "prefs.skilllessthanone.text", "prefs.skilllessthantwo.text", "prefs.other.text",
          "prefs.icontext.text", "prefs.nfirst.text", "prefs.showExpectedOnly",

          "prefs.showhighest.text", "prefs.showhighest.1", "prefs.showhighest.2",
          "prefs.showhighest.3", "prefs.showhighest.4", "prefs.showhighest.5",

          "prefs.changes.text", "prefs.changes.mode0.text", "prefs.changes.mode1.text",

          "prefs.categorized.text", "prefs.categorized.0", "prefs.categorized.1",
          "prefs.categorized.2", "prefs.categorized.3", "prefs.categorized.4",
          "prefs.categorized.5", "prefs.categorized.6",

      "prefs.showWarnings" }, 4, "tree.unitnodewrapper.");

      // context menu
      contextMenu = new JMenu(Resources.get("tree.unitnodewrapper.prefs.title"));
      itemItem =
          new JCheckBoxMenuItem(Resources.get("tree.unitnodewrapper.prefs.other.text"),
              properties[SHOW_OTHER]);
      itemItem.addActionListener(this);
      contextMenu.add(itemItem);
      skillItem =
          new JCheckBoxMenuItem(Resources.get("tree.unitnodewrapper.prefs.skill.text"),
              properties[SHOW_SKILL]);
      skillItem.addActionListener(this);
      contextMenu.add(skillItem);
    }

    /**
     * Sort item categories by priority specified in rules.
     */
    public void sortCategories(final Rules rules) {
      Arrays.sort(categories, new Comparator<String>() {
        public int compare(String o1, String o2) {
          int p1 =
              rules.getItemCategory(o1) == null ? Integer.MAX_VALUE : rules.getItemCategory(o1)
                  .getSortIndex();
          int p2 =
              rules.getItemCategory(o2) == null ? Integer.MAX_VALUE : rules.getItemCategory(o2)
                  .getSortIndex();
          if (p1 == p2)
            return o1.compareTo(o2);
          return p1 - p2;
        }
      });
    }

    /**
     * @see magellan.client.swing.tree.AbstractNodeWrapperDrawPolicy#applyPreferences()
     */
    @Override
    public void applyPreferences() {
      // check warner
      // uWarning.getAdapter(null, null).applyPreferences();
      // update all wrappers
      super.applyPreferences();

      skillItem.setSelected(properties[SHOW_SKILL]);
      itemItem.setSelected(properties[SHOW_OTHER]);
    }

    /**
     * @see magellan.client.swing.context.ContextChangeable#getContextAdapter()
     */
    public JMenuItem getContextAdapter() {
      return contextMenu;
    }

    /**
     * @see magellan.client.swing.context.ContextChangeable#setContextObserver(magellan.client.swing.context.ContextObserver)
     */
    public void setContextObserver(ContextObserver co) {
      obs = co;
    }

    /**
     * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
     */
    public void actionPerformed(ActionEvent e) {
      boolean changed = false;

      if (properties[SHOW_SKILL] != skillItem.isSelected()) {
        properties[SHOW_SKILL] = skillItem.isSelected();

        if ((properties[SHOW_SKILL] && sK[SHOW_SKILL][1].equals("true"))
            || (!properties[SHOW_SKILL] && sK[SHOW_SKILL][1].equals("false"))) {
          settings.remove(prefix + "." + sK[SHOW_SKILL][0]);
        } else {
          settings.setProperty(prefix + "." + sK[SHOW_SKILL][0], properties[SHOW_OTHER] ? "true"
              : "false");
        }

        changed = true;
      }

      if (properties[SHOW_OTHER] != itemItem.isSelected()) {
        properties[SHOW_OTHER] = skillItem.isSelected();

        if ((properties[SHOW_OTHER] && sK[SHOW_OTHER][1].equals("true"))
            || (!properties[SHOW_OTHER] && sK[SHOW_OTHER][1].equals("false"))) {
          settings.remove(prefix + "." + sK[SHOW_OTHER][0]);
        } else {
          settings.setProperty(prefix + "." + sK[SHOW_OTHER][0], properties[SHOW_OTHER] ? "true"
              : "false");
        }

        changed = true;
      }

      if (changed) {
        // update all wrappers
        applyPreferences();

        if (obs != null) {
          obs.contextDataChanged();
        }
      }
    }

    /**
     * Returns the settings.
     */
    public Properties getSettings() {
      return settings;
    }
  }

  protected class UnitGraphicsElement extends GraphicsElement {
    /**
     * Creates a new UnitGraphicsElement object.
     */
    public UnitGraphicsElement(Object o, Icon i, Image im, String s) {
      super(o, i, im, s);
    }

    /**
     * Creates a new UnitGraphicsElement object.
     */
    public UnitGraphicsElement(Icon i, Image im, String s) {
      super(i, im, s);
    }

    /**
     * Creates a new UnitGraphicsElement object.
     */
    public UnitGraphicsElement(Object o) {
      super(o);
    }

    /**
     *
     */
    @Override
    public boolean isEmphasized() {
      return emphasized();
    }
  }

  /**
   * @see magellan.client.swing.tree.SupportsClipboard#getClipboardValue()
   */
  public String getClipboardValue() {
    if (unit != null)
      return unit.toString();
    else
      return toString();
  }

  public void setText(String text) {
    this.text = text;
  }
}
