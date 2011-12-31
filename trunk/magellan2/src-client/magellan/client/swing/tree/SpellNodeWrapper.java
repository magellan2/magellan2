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
import java.util.List;
import java.util.Properties;

import magellan.library.CombatSpell;
import magellan.library.Spell;

/**
 * Displays a spell.
 * 
 * @author $Author: stm$
 */
public class SpellNodeWrapper extends DefaultNodeWrapper implements CellObject2, SupportsClipboard {
  private Spell spell;

  private CombatSpell cSpell;

  private List<GraphicsElement> graphicElements;

  /**
   * Creates a new spellNodeWrapper object.
   */
  public SpellNodeWrapper(Spell s) {
    spell = s;
  }

  /**
   * Creates a node wrapper from a combat spell.
   */
  public SpellNodeWrapper(CombatSpell spell2) {
    cSpell = spell2;
    spell = spell2.getSpell();
  }

  /**
   * @return spell name + postfix
   */
  @Override
  public String toString() {
    if (cSpell == null)
      return spell.toString();
    else
      return cSpell.toString();
  }

  /**
   * @see magellan.client.swing.tree.CellObject#getIconNames()
   */
  public List<String> getIconNames() {
    return Collections.singletonList("spell");
  }

  /**
   * @see magellan.client.swing.tree.CellObject#emphasized()
   */
  @Override
  public boolean emphasized() {
    return false;
  }

  /**
   * @see magellan.client.swing.tree.CellObject#propertiesChanged()
   */
  public void propertiesChanged() {
    // no changeable properties
  }

  /**
   * @see magellan.client.swing.tree.SupportsClipboard#getClipboardValue()
   */
  public String getClipboardValue() {
    return toString();
  }

  /**
   * @see magellan.client.swing.tree.CellObject#init(java.util.Properties,
   *      magellan.client.swing.tree.NodeWrapperDrawPolicy)
   */
  public NodeWrapperDrawPolicy init(Properties settings, NodeWrapperDrawPolicy adapter) {
    return null;
  }

  /**
   * @see magellan.client.swing.tree.CellObject#init(java.util.Properties, java.lang.String,
   *      magellan.client.swing.tree.NodeWrapperDrawPolicy)
   */
  public NodeWrapperDrawPolicy init(Properties settings, String prefix,
      NodeWrapperDrawPolicy adapter) {
    return null;
  }

  /**
   * @see magellan.client.swing.tree.CellObject2#getGraphicsElements()
   */
  public List<GraphicsElement> getGraphicsElements() {
    if (graphicElements == null) {
      GraphicsElement ge = new GraphicsElement(toString(), null, null, "spell");
      StringBuilder tip = new StringBuilder();
      for (Spell.Component ingredient : spell.getParsedComponents()) {
        if (tip.length() > 0) {
          tip.append(", ");
        }
        tip.append(ingredient.toString());
      }
      tip.append(";\n");
      tip.append(spell.getSyntaxString());
      ge.setTooltip(tip.toString());

      graphicElements = Collections.singletonList(ge);
    }

    return graphicElements;
  }

  public boolean reverseOrder() {
    return false;
  }

  public int getLabelPosition() {
    return graphicElements.size() - 1;
  }

  public Spell getSpell() {
    return spell;
  }
}
