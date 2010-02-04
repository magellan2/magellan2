// class magellan.test.ExtendedTests
// created on Jan 10, 2010
//
// Copyright 2003-2010 by magellan project team
//
// Author : $Author: $
// $Id: $
//
// This program is free software; you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation; either version 2 of the License, or
// (at your option) any later version.
// 
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
// GNU General Public License for more details.
// 
// You should have received a copy of the GNU General Public License
// along with this program (see doc/LICENCE.txt); if not, write to the
// Free Software Foundation, Inc.,
// 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
// 
package magellan.test;

import java.util.ArrayList;
import java.util.Collection;
import java.util.StringTokenizer;
import java.util.regex.Pattern;

import magellan.client.Client;
import magellan.client.event.EventDispatcher;
import magellan.library.Faction;
import magellan.library.GameData;
import magellan.library.Item;
import magellan.library.Rules;
import magellan.library.Skill;
import magellan.library.StringID;
import magellan.library.Unit;
import magellan.library.UnitContainer;
import magellan.library.gamebinding.EresseaConstants;
import magellan.library.rules.ItemCategory;
import magellan.library.rules.ItemType;
import magellan.library.rules.SkillType;
import magellan.library.utils.replacers.ReplacerHelp;
import magellan.plugin.extendedcommands.ExtendedCommandsHelper;

/**
 * This class is here to test my ExtendedCommands scripts. Note that scripts do not support
 * generics...
 * 
 * @author stm
 */
@SuppressWarnings("unchecked")
public class ExtendedTests {

  ExtendedCommandsHelper helper;
  GameData world;
  private UnitContainer container;
  private Unit unit;

  public ExtendedTests() {
  }

  public void test(Client client, Unit u) {
    world = u.getRegion().getData();
    container = null;
    unit = u;
    helper = new ExtendedCommandsHelper(client, world);
  }

  public void test(Client client, UnitContainer uc) {
    world = uc.getData();
    container = uc;
    unit = null;
    helper = new ExtendedCommandsHelper(client, world);
  }

  // ////////////////////////////////////////////////////////////////////////////////////////////
  // / copy from here ///////////////////////////////////////////////////////////////////////////

  private static final int EINHEITENLIMIT = 200;

  public void notifyMagellan(Unit u) {
    // helper.updateUnit(u);
  }

  public ItemType getItemType(String name) {
    return world.rules.getItemType(name);
  }

  public SkillType getSkillType(String name) {
    return world.rules.getSkillType(name);
  }

  /**
   * If confirm is false, mark unit as not confirmable. If confirm is true, mark it as confirmable
   * if it has not been marked as unconfirmable before (unconfirm always overrides confirm).
   */
  public void setConfirm(Unit u, boolean confirm) {
    String tag = getProperty(u, "confirm");
    if (confirm) {
      if (tag.length() == 0) {
        setProperty(u, "confirm", "1");
      }
    } else if (!confirm) {
      setProperty(u, "confirm", "0");
    }
  }

  /**
   * Add a property (in form of a tag) to the unit.
   */
  public void setProperty(Unit u, String tagName, String value) {
    u.putTag("$cript." + tagName, value);
    // u.addOrder("; $cript " + tagName + ":" + value, false, 0);
  }

  /**
   * Returns a property value (from a tag) from the unit. Returns the empty string if the tagName is
   * undefined.
   */
  public String getProperty(Unit u, String tagName) {
    String tag = u.getTag("$cript." + tagName);
    return tag == null ? "" : tag;
  }

  /**
   * Parses scripts and confirms units according to the "confirm" tag. Call this for the faction
   * container to wrapup all units.
   */
  public void wrapup(UnitContainer container) {
    parseScripts(container);

    if (container instanceof Faction) {
      if (((Faction) container).units().size() > ExtendedTests.EINHEITENLIMIT - 10) {
        addWarning(container.units().iterator().next(), "Einheitenlimit fast erreicht!");
      }
    }

    for (Unit u : container.units()) {
      if ("1".equals(getProperty(u, "confirm"))) {
        u.setOrdersConfirmed(true);
      } else if ("0".equals(getProperty(u, "confirm"))) {
        u.setOrdersConfirmed(false);
      }
      notifyMagellan(u);
    }
  }

  /**
   * If order is a script command ("// $cript ..."), returns a StringTokenizer pointing to the first
   * token after the "// $cript". Otherwise returns null.
   */
  private StringTokenizer detectScriptCommand(String order) {
    StringTokenizer tokenizer = new StringTokenizer(order, " ");
    if (tokenizer.hasMoreTokens()) {
      String part = tokenizer.nextToken();
      if (part.equals("//")) {
        if (tokenizer.hasMoreTokens()) {
          part = tokenizer.nextToken();
          if (part.equals("$cript"))
            return tokenizer;
        }
      }
    }
    return null;
  }

  /**
   * Parses the orders of the units in container for commands of the form "// $cript ..." and tries
   * to execute them. Known commands:<br />
   * +X text -- If X<=1 then a warning containing text is added to the unit's orders. Otherwise X is
   * decreased by one.
   */
  public void parseScripts(UnitContainer container) {
    for (Unit u : container.units()) {
      Collection oldOrders = new ArrayList(u.getOrders());
      Collection newOrders = new ArrayList();
      boolean changedOrders = false;
      int error = -1;
      String errMsg = null;
      int line = 0;
      for (Object o : oldOrders) {
        ++line;
        String order = (String) o;
        StringTokenizer tokenizer = detectScriptCommand(order);
        if (tokenizer == null) {
          newOrders.add(order);
          continue;
        }

        if (tokenizer.hasMoreTokens()) {
          String command = tokenizer.nextToken();
          if (command.startsWith("+")) {
            int delay = -1;
            try {
              delay = Integer.parseInt(command.substring(1));
            } catch (NumberFormatException e) {
              error = line;
              errMsg = "Zahl erwartet";
              break;
            }
            if (delay <= 1) {
              StringBuilder warning = new StringBuilder("; TODO: ");
              while (tokenizer.hasMoreTokens()) {
                warning.append(tokenizer.nextToken());
              }
              newOrders.add(warning.toString());
              changedOrders = true;
            } else {
              StringBuilder newCommand = new StringBuilder("// $cript +");
              newCommand.append(delay - 1).append(" ");
              while (tokenizer.hasMoreTokens()) {
                newCommand.append(tokenizer.nextToken());
              }
              newOrders.add(newCommand.toString());
              changedOrders = true;
            }
          } else if (command.equals("auto")) {
            setConfirm(u, true);
            newOrders.add(order);
            newOrders.add("; autoconfirmed");
            changedOrders = true;
          } else {
            error = line;
            errMsg = "unbekannter Befehl";
            break;
          }
        }
      }
      if (error != -1) {
        u.addOrder("; TODO: Fehler im Skript in Zeile " + error + ": " + errMsg, false, 0);
        notifyMagellan(u);
      } else if (changedOrders) {
        u.setOrders(newOrders);
      }
      notifyMagellan(u);
    }
  }

  /**
   * Returns true if the item is usable with weaponSkill, false otherwise.
   */
  private boolean isUsable(Item item, SkillType weaponSkill) {
    return (item.getItemType().getUseSkill() != null && item.getItemType().getUseSkill()
        .getSkillType().equals(weaponSkill));
  }

  /**
   * Returns true if skill is a weapon skill.
   */
  private boolean isWeaponSkill(Skill skill) {
    return skill.getSkillType().getID().equals(EresseaConstants.S_HIEBWAFFEN)
        || skill.getSkillType().getID().equals(EresseaConstants.S_STANGENWAFFEN)
        || skill.getSkillType().getID().equals(EresseaConstants.S_BOGENSCHIESSEN)
        || skill.getSkillType().getID().equals(EresseaConstants.S_ARMBRUSTSCHIESSEN)
        || skill.getSkillType().getID().equals(EresseaConstants.S_KATAPULTBEDIENUNG);
  }

  /**
   * Convenience method for calling <code>soldier(u, null, null, null, null, false)</code>.
   * 
   * @param u
   */
  public void soldier(Unit u) {
    soldier(u, null, null, null, null, false);
  }

  /**
   * Marks the unit as soldier. Learns its best weapon skill. Reserves suitable weapon, armor, and
   * shield if available. Confirms unit if there's no problem.
   * 
   * @param u The unit in question
   * @param sWeaponSkill The desired skill. If <code>null</code>, the unit's best weapon skill is
   *          used. If the unit knows no weapon skill, a warning is issued.
   * @param sWeapon The desired weapon that is reserved. If this is <code>null</code>, a weapon that
   *          matches the weaponSkill is reserved.
   * @param sArmor The desired armor. If <code>null</code>, a suitable armor is reserved. If the
   *          unit has no armor at all, <em>no</em> warning is issued.
   * @param sShield The desired shield. If <code>null</code>, a suitable shield is reserved. If the
   *          unit has no shield at all, <em>no</em> warning is issued.
   * @param warnEquipment Warnings for missing equipment are only issued if this is
   *          <code>true</code>.
   */
  public void soldier(Unit u, String sWeaponSkill, String sWeapon, String sArmor, String sShield,
      boolean warnEquipment) {

    Rules rules = world.rules;

    SkillType weaponSkill =
        sWeaponSkill == null ? null : rules.getSkillType(StringID.create(sWeaponSkill));
    ItemType weapon = sWeapon == null ? null : rules.getItemType(StringID.create(sWeapon));
    ItemType armor = sArmor == null ? null : rules.getItemType(StringID.create(sArmor));
    ItemType shield = sShield == null ? null : rules.getItemType(StringID.create(sShield));

    if (weaponSkill == null) {

      int max = 0;
      for (Skill skill : u.getSkills()) {
        if (isWeaponSkill(skill) && skill.getLevel() > max) {
          max = skill.getLevel();
          weaponSkill = skill.getSkillType();
        }
      }
      if (weaponSkill == null) {
        addWarning(u, "no weapon skill");
        return;
      }
    }
    if (weapon == null) {
      for (Item item : u.getItems()) {
        if (isUsable(item, weaponSkill)) {
          weapon = item.getItemType();
        }
      }
    }

    if (warnEquipment
        && (weapon == null || u.getItem(weapon) == null || u.getItem(weapon).getAmount() != u
            .getModifiedPersons())) {
      addWarning(u, "no suitable weapon");
    }

    ItemCategory armorType = rules.getItemCategory(StringID.create("armour"), false);
    if (armor == null) {
      for (Item item : u.getItems()) {
        if (item.getItemType().getCategory().isDescendant(armorType)) {
          armor = item.getItemType();
        }
      }
    }
    if (warnEquipment
        && (armor != null && (u.getItem(armor) == null || u.getItem(armor).getAmount() != u
            .getModifiedPersons()))) {
      addWarning(u, "no suitable armor");
      setConfirm(u, false);
    }

    ItemCategory shieldType = rules.getItemCategory(StringID.create("shield"), false);
    if (armor == null) {
      for (Item item : u.getItems()) {
        if (item.getItemType().getCategory().isDescendant(shieldType)) {
          shield = item.getItemType();
        }
      }
    }
    if (warnEquipment
        && (shield != null && (u.getItem(armor) == null || u.getItem(shield).getAmount() != u
            .getModifiedPersons()))) {
      addWarning(u, "no suitable shield");
      setConfirm(u, false);
    }

    u.addOrder("LERNEN " + weaponSkill.getName(), true, 1);
    if (weapon != null) {
      u.addOrder("RESERVIERE JE 1 " + weapon.getOrderName(), true, 4);
    }
    if (armor != null) {
      u.addOrder("RESERVIERE JE 1 " + armor.getOrderName(), true, 4);
    }
    if (shield != null) {
      u.addOrder("RESERVIERE JE 1 " + shield.getOrderName(), true, 4);
    }
    setConfirm(u, true);
    // u.setOrdersConfirmed(confirm);
    notifyMagellan(u);
  }

  void parseShipLoaderTag(magellan.library.Region r) {
    for (Unit u : r.units()) {
      String name = null;
      for (String line : u.getOrders()) {
        java.util.regex.Pattern p = Pattern.compile(".*[$]([^$]*)[$]sl.*");
        java.util.regex.Matcher m = p.matcher(line);
        if (m.matches()) {
          name = m.group(1);
        }
      }
      if (name != null) {
        u.putTag("ejcTaggableComparator5", name);
      } else {
        u.removeTag("ejcTaggableComparator5");
      }
    }
  }

  void parseShipLoaderTag2(magellan.library.Region r) {
    for (Unit u : r.units()) {
      String name = null;
      for (String line : u.getOrders()) {
        java.util.regex.Pattern p = Pattern.compile(".*[$]([^$]*)[$]verlassen.*");
        java.util.regex.Matcher m = p.matcher(line);
        if (m.matches()) {
          name = m.group(1);
        }
      }
      if (name != null && name.equals("633")) {
        u.putTag("ejcTaggableComparator5", name);
      } else {
        u.removeTag("ejcTaggableComparator5");
      }
    }
  }

  // ///////////////////////////////////////////////////////
  // HELPER functions
  // ///////////////////////////////////////////////////////

  /**
   * return the amount of silver of the unit
   */
  int getSilber(Unit unit) {
    return Math.max(helper.getItemCount(unit, "Silver"), helper.getItemCount(unit, "Silber"));
  }

  void addWarning(Unit unit, String text) {
    helper.addOrder(unit, "; -------------------------------------");
    helper.addOrder(unit, "; TODO: " + text);
    setConfirm(unit, false);
  }

  void setNeed(Unit unit, String item, int amount) {
    // if (needs == null)
    // needs = new HashMap();
    // Map unitNeed = (Map) needs.get(unit);
    // if (unitNeed == null)
    // unitNeed = new HashMap(1);
    // if (unitNeed.get(item) == null)
    // unitNeed.put(item, amount);
    // else
    // unitNeed.put(item, amount + ((Integer) unitNeed.get(item)));
    //
    // needs.put(unit, unitNeed);
    // helper.addOrder(unit, "; unit needs " + ((Map) needs.get(unit)).get(item) + " of " + item);
  }

  // ///////////////////////////////////////////////////////
  // STEUERMANN
  // ///////////////////////////////////////////////////////

  void steuermann(Unit unit, int silberMin, int silberMax) {
    int silber = getSilber(unit);
    if (silber < silberMin) {
      addWarning(unit, "Steuermann hat zu wenig Silber!");
    }

    setNeed(unit, "Silber", silberMax);
  }

  // ///////////////////////////////////////////////////////
  // VORLAGE functions
  // ///////////////////////////////////////////////////////

  StringTokenizer parseOrder(String order) {
    if (!order.startsWith("// #call"))
      return null;

    return new StringTokenizer(order.substring("// #call".length()), " ");
  }

  boolean checkSyntax(Unit unit, String command, StringTokenizer arguments, int expected) {
    if (arguments.countTokens() != expected) {
      addWarning(unit, "#call " + command + ": wrong number of arguments, expected " + expected);
      return false;
    } else
      return true;
  }

  void parseVorlage(Unit unit) {
    Collection oldOrders = new ArrayList<Object>(unit.getOrders());
    for (Object o : oldOrders) {
      String order = (String) o;
      StringTokenizer tokenizer = parseOrder(order);
      if (tokenizer == null) {
        continue;
      }

      if (tokenizer.hasMoreTokens()) {
        String command = tokenizer.nextToken();
        if (command.equals("Steuermann")) {

          if (checkSyntax(unit, command, tokenizer, 2)) {
            steuermann(unit, Integer.parseInt(tokenizer.nextToken()), Integer.parseInt(tokenizer
                .nextToken()));
          }
        } else if (command.equals("AutoBestaetigen")) {
          if (checkSyntax(unit, command, tokenizer, 1)) {
            setConfirm(unit, tokenizer.nextToken().equalsIgnoreCase("an"));
          }
        } else {
          addWarning(unit, "unsupported Vorlage command: " + command);
        }
      }
    }
  }

  void testReplacer() {
    new ReplacerHelp(new EventDispatcher(), world);
    ReplacerHelp.createReplacer("§priv§50§item§Silber§priv§clear§")
        .getReplacement(unit.getRegion());
  }

  static class A {

    public static String b() {
      return "A.b";
    }
  }

  class B {

    public String b() {
      return "B.b";
    }

  }

  public void testA(Unit u) {
    helper.addOrder(u, A.b());
    helper.addOrder(u, (new B()).b());
  }

}
