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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Properties;

import magellan.library.Skill;
import magellan.library.Unit;

/**
 * DOCUMENT-ME
 * 
 * @author $Author: $
 * @version $Revision: 171 $
 */
public class SkillNodeWrapper implements CellObject2, SupportsClipboard {
  private Unit unit;
  private Skill skill;
  private Skill modSkill;
  private boolean showNextLevelPoints = true;
  private boolean showNextLevelLearnTurns = true;
  protected DetailsNodeWrapperDrawPolicy adapter;
  protected String text;
  protected List<String> icon;
  protected List<GraphicsElement> elements;

  /** DOCUMENT-ME */
  public static final int SHOW_NEXTLEVEL = 0;

  /** DOCUMENT-ME */
  public static final int SHOW_NEXTLEVELPOINTS = 1;

  /** DOCUMENT-ME */
  public static final int SHOW_NEXTLEVELTURNS = 2;

  /** DOCUMENT-ME */
  public static final int SHOW_CHANGES = 3;

  /** DOCUMENT-ME */
  public static final int SHOW_CHANGE_STYLED = 4;

  /** DOCUMENT-ME */
  public static final int SHOW_CHANGE_TEXT = 5;
  private static final String SKILL_CHANGE_STYLE_PREFIX = "Talent";

  /**
   * Creates a new SkillNodeWrapper object.
   * 
   * @param u the unit with the specified skills.
   * @param s the base skill. If s is null, it is assumed that the unit aquires that skill only
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
   * DOCUMENT-ME
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

      if (skill.isLevelChanged() && isShowingChanges() && isShowingChangesText()) {
        sb.append('(');

        if (skill.getChangeLevel() >= 0) {
          sb.append('+');
        }

        sb.append(skill.getChangeLevel());
        sb.append(')');
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
   * DOCUMENT-ME
   */
  public boolean isShowingNextLevel() {
    if (adapter != null)
      return adapter.properties[SkillNodeWrapper.SHOW_NEXTLEVEL];

    return true;
  }

  /**
   * DOCUMENT-ME
   */
  public boolean isShowingChanges() {
    if (adapter != null)
      return adapter.properties[SkillNodeWrapper.SHOW_CHANGES];

    return true;
  }

  /**
   * DOCUMENT-ME
   */
  public boolean isShowingChangesStyled() {
    if (adapter != null)
      return adapter.properties[SkillNodeWrapper.SHOW_CHANGE_STYLED];

    return false;
  }

  /**
   * DOCUMENT-ME
   */
  public boolean isShowingChangesText() {
    if (adapter != null)
      return adapter.properties[SkillNodeWrapper.SHOW_CHANGE_TEXT];

    return true;
  }

  /**
   * DOCUMENT-ME
   */
  public boolean emphasized() {
    return false;
  }

  // TODO: possibly make static
  public List<String> getIconNames() {
    if (icon == null) {
      icon = new ArrayList<String>(1);

      if (skill != null) {
        icon.add(skill.getSkillType().getID().toString());
      } else if (modSkill != null) {
        icon.add(modSkill.getSkillType().getID().toString());
      } else {
        icon = null;
      }
    }

    return icon;
  }

  /**
   * DOCUMENT-ME
   */
  public void propertiesChanged() {
    text = null;
    elements = null;
  }

  /**
   * DOCUMENT-ME
   */
  public String getClipboardValue() {
    if (skill != null)
      return skill.getName();
    else
      return toString();
  }

  /**
   * DOCUMENT-ME
   */
  public List<GraphicsElement> getGraphicsElements() {
    if (elements == null) {
      GraphicsElement ge = new GraphicsElement(toString(), null, null, null);
      ge.setType(GraphicsElement.MAIN);

      if (skill != null) {
        ge.setImageName(skill.getSkillType().getID().toString());
      } else if (modSkill != null) {
        ge.setImageName(modSkill.getSkillType().getID().toString());
      }

      boolean isDiff = false;

      if (skill != null) {
        isDiff = skill.isLevelChanged();
      }

      if (isDiff && isShowingChanges() && isShowingChangesStyled()) {
        ge.setStyleset(SkillNodeWrapper.SKILL_CHANGE_STYLE_PREFIX
            + ((skill.getChangeLevel() >= 0) ? ">." : "<.")
            + SkillNodeWrapper.SKILL_CHANGE_STYLE_PREFIX + String.valueOf(skill.getChangeLevel()));
      }

      elements = Collections.singletonList(ge);
    }

    return elements;
  }

  /**
   * DOCUMENT-ME
   */
  public boolean reverseOrder() {
    return false;
  }

  /**
   * DOCUMENT-ME
   */
  public NodeWrapperDrawPolicy init(Properties settings, NodeWrapperDrawPolicy adapter) {
    return init(settings, "SkillNodeWrapper", adapter);
  }

  /**
   * DOCUMENT-ME
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
