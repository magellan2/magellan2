// class magellan.library.gamebinding.AtlantisOrderCompleter
// created on Apr 16, 2013
//
// Copyright 2003-2013 by magellan project team
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
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU General Public License for more details.
//
// You should have received a copy of the GNU General Public License
// along with this program (see doc/LICENCE.txt); if not, write to the
// Free Software Foundation, Inc.,
// 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
//
package magellan.library.gamebinding.atlantis;

import java.util.Iterator;

import magellan.library.GameData;
import magellan.library.StringID;
import magellan.library.completion.CompleterSettingsProvider;
import magellan.library.completion.Completion;
import magellan.library.gamebinding.AbstractOrderCompleter;
import magellan.library.gamebinding.EresseaConstants;
import magellan.library.rules.ItemType;
import magellan.library.rules.SkillType;

/**
 * Order completer for Atlantis game
 */
public class AtlantisOrderCompleter extends AbstractOrderCompleter {

  /**
   *
   */
  public AtlantisOrderCompleter(GameData gd, CompleterSettingsProvider ac) {
    super(gd, ac);
  }

  @Override
  protected void initParser() {
    setParser(new AtlantisOrderParser(getData(), this));
  }

  // begin of completion methods invoked by OrderParser
  @Override
  protected void cmplt() {
    // add completions, that were defined by the user in the option pane
    // and can be accessed by CompleterSettingsProvider.getSelfDefinedCompletions()
    completions.addAll(completerSettingsProvider.getSelfDefinedCompletions());
    // completions.add(new Completion(getOrderTranslation(EresseaConstants.OC_WORK)));
    // completions.add(new Completion(getOrderTranslation(EresseaConstants.OC_ATTACK), " "));
    // completions.add(new Completion(getOrderTranslation(EresseaConstants.OC_BANNER), spaceQuotes,
    // Completion.DEFAULT_PRIORITY, 1));
    // if (unit.getFaction() != null && unit.getFaction().getItems().size() > 0) {
    // completions.add(new Completion(getOrderTranslation(EresseaConstants.OC_CLAIM), " "));
    // }

    completions.add(new Completion(getOrderTranslation(AtlantisConstants.OC_FORM), " "));
    completions.add(new Completion(getOrderTranslation(AtlantisConstants.OC_ACCEPT), " "));
    completions.add(new Completion(getOrderTranslation(AtlantisConstants.OC_ADDRESS), " "));
    completions.add(new Completion(getOrderTranslation(AtlantisConstants.OC_ADMIT), " "));
    completions.add(new Completion(getOrderTranslation(AtlantisConstants.OC_ALLY), " "));
    completions.add(new Completion(getOrderTranslation(AtlantisConstants.OC_BEHIND), " "));
    completions.add(new Completion(getOrderTranslation(AtlantisConstants.OC_COMBAT), " "));
    completions.add(new Completion(getOrderTranslation(AtlantisConstants.OC_DISPLAY), " "));
    completions.add(new Completion(getOrderTranslation(AtlantisConstants.OC_GUARD), " "));
    completions.add(new Completion(getOrderTranslation(AtlantisConstants.OC_NAME), " "));
    completions.add(new Completion(getOrderTranslation(AtlantisConstants.OC_PASSWORD), " "));
    completions.add(new Completion(getOrderTranslation(AtlantisConstants.OC_RESHOW), " "));
    completions.add(new Completion(getOrderTranslation(AtlantisConstants.OC_FIND), " "));
    completions.add(new Completion(getOrderTranslation(AtlantisConstants.OC_BOARD), " "));
    completions.add(new Completion(getOrderTranslation(AtlantisConstants.OC_ENTER), " "));
    completions.add(new Completion(getOrderTranslation(AtlantisConstants.OC_LEAVE), " "));
    completions.add(new Completion(getOrderTranslation(AtlantisConstants.OC_PROMOTE), " "));
    completions.add(new Completion(getOrderTranslation(AtlantisConstants.OC_ATTACK), " "));
    completions.add(new Completion(getOrderTranslation(AtlantisConstants.OC_DEMOLISH), " "));
    completions.add(new Completion(getOrderTranslation(AtlantisConstants.OC_GIVE), " "));
    completions.add(new Completion(getOrderTranslation(AtlantisConstants.OC_PAY), " "));
    completions.add(new Completion(getOrderTranslation(AtlantisConstants.OC_SINK), " "));
    completions.add(new Completion(getOrderTranslation(AtlantisConstants.OC_TRANSFER), " "));
    completions.add(new Completion(getOrderTranslation(AtlantisConstants.OC_TAX), " "));
    completions.add(new Completion(getOrderTranslation(AtlantisConstants.OC_RECRUIT), " "));
    completions.add(new Completion(getOrderTranslation(AtlantisConstants.OC_QUIT), " "));
    completions.add(new Completion(getOrderTranslation(AtlantisConstants.OC_MOVE), " "));
    completions.add(new Completion(getOrderTranslation(AtlantisConstants.OC_SAIL), " "));
    completions.add(new Completion(getOrderTranslation(AtlantisConstants.OC_BUILD), " "));
    completions.add(new Completion(getOrderTranslation(AtlantisConstants.OC_ENTERTAIN), " "));
    completions.add(new Completion(getOrderTranslation(AtlantisConstants.OC_PRODUCE), " "));
    completions.add(new Completion(getOrderTranslation(AtlantisConstants.OC_RESEARCH), " "));
    completions.add(new Completion(getOrderTranslation(AtlantisConstants.OC_STUDY), " "));
    completions.add(new Completion(getOrderTranslation(AtlantisConstants.OC_TEACH), " "));
    completions.add(new Completion(getOrderTranslation(AtlantisConstants.OC_WORK), " "));
    completions.add(new Completion(getOrderTranslation(AtlantisConstants.OC_CAST), " "));
  }

  /**
   * Add completion for an ID.
   */
  public void cmpltId() {
    completions.add(new Completion("<id>", "", ""));
  }

  /**
   * Add completions for a number between min and max (inclusively).
   */
  public void cmpltNumber(int min, int max) {
    if (max < min + 6) {
      for (int i = min; i <= max; ++i) {
        completions.add(new Completion(String.valueOf(i)));
      }
    } else {
      completions.add(new Completion("<num>", String.valueOf(min == Integer.MIN_VALUE
          ? (max == Integer.MAX_VALUE ? 1 : max) : min), " "));
    }
  }

  @Override
  protected String getTemp() {
    return "NEW";
  }

  public void cmpltAttack() {
    cmpltId();
    completions.add(new Completion(getOrderTranslation(AtlantisConstants.OC_PEASANTS)));
  }

  public void cmpltItem(StringID... exclude) {
    this.addUnitItems(0, "", exclude);

  }

  public void cmpltMache() {
    // items
    for (final Iterator<ItemType> iter = data.getRules().getItemTypeIterator(); iter.hasNext();) {
      final ItemType itemType = iter.next();
      boolean canMake = true;

      if (itemType.getMakeSkill() == null) {
        // some items can not be made like dragonblood or magic artefacts
        canMake = false;
      } else if (!hasSkill(unit, itemType.getMakeSkill().getSkillType().getID(), itemType
          .getMakeSkill().getLevel())) {
        canMake = false;
      } else if (completerSettingsProvider.getLimitMakeCompletion()
          && !checkForMaterials(itemType.getResources())) {
        canMake = false;
      } else if (itemType.equals(data.getRules().getItemType(EresseaConstants.I_UIRON))
          && (region.getIron() <= 0)) {
        canMake = false;
      } else if (itemType.equals(data.getRules().getItemType(EresseaConstants.I_ULAEN))
          && (region.getLaen() <= 0)) {
        canMake = false;
      } else if (itemType.equals(data.getRules().getItemType(EresseaConstants.I_WOOD)) &&
      // bugzilla enhancement 599: also allow completion on sprouts
      // also take care of mallorn flag
          (((region.getTrees() <= 0) && (region.getSprouts() <= 0)) || region.isMallorn())) {
        canMake = false;
      } else if (itemType.equals(data.getRules().getItemType(EresseaConstants.I_UMALLORN)) &&
      // bugzilla enhancement 599: also allow completion on sprouts
          (((region.getTrees() <= 0) && (region.getSprouts() <= 0)) || !region.isMallorn())) {
        canMake = false;
      } else if (itemType.equals(data.getRules().getItemType(EresseaConstants.I_UHORSE))
          && (region.getHorses() <= 0)) {
        canMake = false;
      } else if (itemType.equals(data.getRules().getItemType(EresseaConstants.I_USTONE))
          && (region.getStones() <= 0)) {
        canMake = false;
      }

      if (canMake) {
        addItem(itemType, "");
      }
    }

  }

  public void cmpltKeyword(StringID... keys) {
    for (StringID key : keys) {
      completions.add(new Completion(getOrderTranslation(key), " "));
    }
  }

  public void cmpltDirection() {
    addDirections(" ");
    addSurroundingRegions(1, " ");
  }

  /**
   * Cost is not appended in Atlantis.
   *
   * @see magellan.library.gamebinding.AbstractOrderCompleter#addSkills()
   */
  @Override
  public void addSkills() {
    if ((data != null) && (data.getRules() != null)) {
      for (SkillType t : data.getRules().getSkillTypes()) {
        // add quotes if needed
        String name = getOrderTranslation(t);
        name = name.replace(' ', '_');

        completions.add(new Completion(name));
      }
    }
  }

}
