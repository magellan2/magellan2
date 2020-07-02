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

import java.util.Collections;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import magellan.library.Skill;
import magellan.library.Unit;
import magellan.library.rules.SkillType;
import magellan.library.utils.Resources;

/**
 * A node that wraps a skill
 * 
 * @author $Author: $
 * @version $Revision: 171 $
 */
public class SkillNodeWrapper extends DefaultNodeWrapper implements CellObject2, SupportsClipboard {
  private Unit unit;
  private Skill skill;
  private Skill modSkill;
  private boolean showNextLevelPoints = true;
  private boolean showNextLevelLearnTurns = true;
  protected DetailsNodeWrapperDrawPolicy adapter;
  protected String text;
  protected List<String> icon;
  protected List<GraphicsElement> elements;

  /** The index of the show next level property */
  public static final int SHOW_NEXTLEVEL = 0;

  /** The index of the show points to next level property */
  public static final int SHOW_NEXTLEVELPOINTS = 1;

  /** The index of the show change turns until next level property */
  public static final int SHOW_NEXTLEVELTURNS = 2;

  /** The index of the show change property */
  public static final int SHOW_CHANGES = 3;

  /** The index of the show change style property */
  public static final int SHOW_CHANGE_STYLED = 4;

  /** The index of the show change text property */
  public static final int SHOW_CHANGE_TEXT = 5;

  /**
   * Creates a new SkillNodeWrapper object.
   * 
   * @param u the unit with the specified skills.
   * @param s the base skill. If s is null, it is assumed that the unit acquires that skill only
   *          through a person transfer. s and ms may not both be null.
   * @param ms the modified skill. If ms is null, it is assumed that the modification of the skill
   *          cannot be determined. s and ms may not both be null.
   */
  public SkillNodeWrapper(Unit u, Skill s, Skill ms) {
    unit = null;
    skill = null;
    modSkill = null;
    unit = u;

    if (s != null) {
      skill = s;
    } else {
      skill = new Skill(ms.getSkillType(), 0, 0, 0, ms.noSkillPoints());
    }

    modSkill = ms;
  }

  /**
   * Return "Skillname Level|- (+|-change) [points &rarr; nextlevelpoints  {turns to learn }] ..."
   */
  @Override
  public String toString() {
    if (text == null) {
      StringBuffer sb = new StringBuffer();
      sb.append(skill.getName()).append(' ');

      if (!skill.isLostSkill()) {
        sb.append(skill.getLevel());
      } else {
        sb.append('-');
      }

      if (isShowingChanges() && isShowingChangesText()) {
        if (skill.isLevelChanged()) {
          sb.append('(');

          if (skill.getChangeLevel() >= 0) {
            sb.append('+');
          }

          sb.append(skill.getChangeLevel());
          sb.append(')');
        } else if (!unit.isDetailsKnown()) {
          sb.append(' ').append(Resources.get("tree.skillnodewrapper.outdated"));
        }
      }

      // FIX!
      if (!skill.noSkillPoints()) {
        if (modSkill != null) {
          if ((skill.getPoints() == modSkill.getPoints())
              && (unit.getPersons() == unit.getModifiedPersons())) {
            sb.append(" [").append(skill.getPointsPerPerson());

            if (isShowingNextLevelPoints() || isShowingNextLevelLearnTurns()) {
              int nextLevel = Skill.getLevelAtPoints(skill.getPointsPerPerson()) + 1;
              int nextLevelPoints = Skill.getPointsAtLevel(nextLevel);
              int pointsToLearn = nextLevelPoints - skill.getPointsPerPerson();
              int turnsToLearn = pointsToLearn / 30;

              if ((pointsToLearn % 30) > 0) {
                turnsToLearn++;
              }

              if (isShowingNextLevelPoints()) {
                sb.append(" -> ").append(nextLevelPoints);
              }

              if (isShowingNextLevelLearnTurns()) {
                sb.append(" {").append(turnsToLearn).append("}");
              }
            }

            sb.append("]");
          } else {
            sb.append(" [").append(skill.getPointsPerPerson()).append("]");
            sb.append(" (").append(modSkill.getLevel()).append(" [").append(
                modSkill.getPointsPerPerson()).append("])");
          }
        } else {
          sb.append(" [").append(skill.getPointsPerPerson()).append("]");
          sb.append(" (? [?])");
        }
      } else {
        if (modSkill != null) {
          if (skill.getLevel() != modSkill.getLevel()) {
            sb.append(" (").append(modSkill.getLevel()).append(")");
          }
        } else {
          sb.append(" (?)");
        }
      }

      text = sb.toString();
    }

    return text;
  }

  /**
   * Controls whether this wrapper shows the skill points required before the next skill level can
   * be reached.
   */
  public void showNextLevelPoints(boolean bool) {
    adapter = null;
    showNextLevelPoints = bool;
  }

  /**
   * Returns whether this wrapper shows the skill points required before the next skill level can be
   * reached.
   */
  public boolean isShowingNextLevelPoints() {
    if (adapter != null)
      return adapter.properties[SkillNodeWrapper.SHOW_NEXTLEVELPOINTS];

    return showNextLevelPoints;
  }

  /**
   * Controls whether this wrapper shows the number of turns to learn before the next skill level
   * can be reached.
   */
  public void showNextLevelLearnTurns(boolean bool) {
    adapter = null;
    showNextLevelLearnTurns = bool;
  }

  /**
   * Returns whether this wrapper shows the number of turns to learn before the next skill level can
   * be reached.
   */
  public boolean isShowingNextLevelLearnTurns() {
    if (adapter != null)
      return adapter.properties[SkillNodeWrapper.SHOW_NEXTLEVELTURNS];

    return showNextLevelLearnTurns;
  }

  /**
   * @return if this wrapper shows the next level
   */
  public boolean isShowingNextLevel() {
    if (adapter != null)
      return adapter.properties[SkillNodeWrapper.SHOW_NEXTLEVEL];

    return true;
  }

  /**
   * @return if this wrapper shows skill level changes
   */
  public boolean isShowingChanges() {
    if (adapter != null)
      return adapter.properties[SkillNodeWrapper.SHOW_CHANGES];

    return true;
  }

  /**
   * Returns <code>true</code> if the appropriate style is applied to changed skills.
   */
  public boolean isShowingChangesStyled() {
    if (adapter != null)
      return adapter.properties[SkillNodeWrapper.SHOW_CHANGE_STYLED];

    return false;
  }

  /**
   * Returns true if skill change is shown as text.
   */
  public boolean isShowingChangesText() {
    if (adapter != null)
      return adapter.properties[SkillNodeWrapper.SHOW_CHANGE_TEXT];

    return true;
  }

  /**
   * @see magellan.client.swing.tree.CellObject#emphasized()
   */
  @Override
  public boolean emphasized() {
    return false;
  }

  // stm 2010: prevent multiple Lists to be generated for nearly static code
  private static Map<Object, List<String>> iconNamesLists = new Hashtable<Object, List<String>>();

  /**
   * @see magellan.client.swing.tree.CellObject#getIconNames()
   */
  public List<String> getIconNames() {
    SkillType key = null;
    if (skill != null) {
      key = skill.getSkillType();
    } else if (modSkill != null) {
      key = modSkill.getSkillType();
    }

    if (key == null)
      return null;

    List<String> iconNames = iconNamesLists.get(key);

    if (iconNames == null) {
      iconNames = Collections.singletonList(key.getIcon());
      iconNamesLists.put(key, iconNames);
    }

    return iconNames;
  }

  // /**
  // * @see magellan.client.swing.tree.CellObject#getIconNames()
  // */
  // public List<String> getIconNames() {
  // if (icon == null) {
  // icon = new ArrayList<String>(1);
  //
  // if (skill != null) {
  // icon.add(skill.getSkillType().getIcon());
  // } else if (modSkill != null) {
  // icon.add(modSkill.getSkillType().getIcon());
  // } else {
  // icon = null;
  // }
  // }
  //
  // return icon;
  // }

  /**
   * @see magellan.client.swing.tree.CellObject#propertiesChanged()
   */
  public void propertiesChanged() {
    text = null;
    elements = null;
  }

  /**
   * @see magellan.client.swing.tree.SupportsClipboard#getClipboardValue()
   */
  public String getClipboardValue() {
    if (skill != null)
      return skill.getName();
    else
      return toString();
  }

  /**
   * Returns the skill image (possibly styled).
   * 
   * @see magellan.client.swing.tree.CellObject2#getGraphicsElements()
   */
  public List<GraphicsElement> getGraphicsElements() {
    if (elements == null) {
      GraphicsElement ge = new GraphicsElement(toString(), null, null, null);
      ge.setType(GraphicsElement.MAIN);

      if (skill != null) {
        ge.setImageName(skill.getSkillType().getIcon());
      } else if (modSkill != null) {
        ge.setImageName(modSkill.getSkillType().getIcon());
      }

      if (skill != null && isShowingChanges() && isShowingChangesStyled()) {
        if (skill.isLevelChanged()) {
          ge.setStyleset(CellRenderer.SKILL_CHANGE_STYLE_PREFIX
              + ((skill.getChangeLevel() >= 0) ? ">." : "<.")
              + CellRenderer.SKILL_CHANGE_STYLE_PREFIX + String.valueOf(skill.getChangeLevel()));
        } else if (!unit.isDetailsKnown()) {
          ge.setStyleset(CellRenderer.STYLE_NAMES[CellRenderer.TALENT_UNKNOWN_STYLE]);
        }
      }

      elements = Collections.singletonList(ge);
    }

    return elements;
  }

  /**
   * @see magellan.client.swing.tree.CellObject2#getLabelPosition()
   */
  public int getLabelPosition() {
    return elements.size() - 1;
  }

  /**
   * Never reversed.
   * 
   * @see magellan.client.swing.tree.CellObject2#reverseOrder()
   */
  public boolean reverseOrder() {
    return false;
  }

  /**
   * @see magellan.client.swing.tree.CellObject#init(java.util.Properties,
   *      magellan.client.swing.tree.NodeWrapperDrawPolicy)
   */
  public NodeWrapperDrawPolicy init(Properties settings, NodeWrapperDrawPolicy anAdapter) {
    return init(settings, "SkillNodeWrapper", anAdapter);
  }

  /**
   * @see magellan.client.swing.tree.CellObject#init(java.util.Properties, java.lang.String,
   *      magellan.client.swing.tree.NodeWrapperDrawPolicy)
   */
  public NodeWrapperDrawPolicy init(Properties p1, String p2, NodeWrapperDrawPolicy p3) {
    if (p3 == null) {
      p3 = createSkillDrawPolicy(p1, p2);
    }

    p3.addCellObject(this);
    adapter = (DetailsNodeWrapperDrawPolicy) p3;

    return p3;
  }

  private NodeWrapperDrawPolicy createSkillDrawPolicy(Properties settings, String prefix) {
    return new DetailsNodeWrapperDrawPolicy(2, new int[] { 2, 2 }, settings, prefix,
        new String[][] { { ".units.showNextSkillLevel", "true" },
            { ".units.showNextSkillLevelPoints", "true" },
            { ".units.showNextSkillLevelLearnTurns", "true" }, { ".units.showChanges", "true" },
            { ".units.showChangesStyled", "false" }, { ".units.showChangesText", "true" }, },
        new String[] { "prefs.showskill.text", "prefs.points.text", "prefs.turns.text",
            "prefs.changes.text", "prefs.changes.mode0.text", "prefs.changes.mode1.text", }, 0,
        "tree.skillnodewrapper.");
  }

}
