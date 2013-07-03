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
package magellan.library.gamebinding;

import magellan.library.GameData;
import magellan.library.completion.CompleterSettingsProvider;
import magellan.library.completion.Completion;

public class AtlantisOrderCompleter extends AbstractOrderCompleter {

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
    // completions.add(new Completion(getOrderTranslation(EresseaConstants.O_WORK)));
    // completions.add(new Completion(getOrderTranslation(EresseaConstants.O_ATTACK), " "));
    // completions.add(new Completion(getOrderTranslation(EresseaConstants.O_BANNER), spaceQuotes,
    // Completion.DEFAULT_PRIORITY, 1));
    // if (unit.getFaction() != null && unit.getFaction().getItems().size() > 0) {
    // completions.add(new Completion(getOrderTranslation(EresseaConstants.O_CLAIM), " "));
    // }

    completions.add(new Completion(getOrderTranslation(AtlantisConstants.O_FORM)));
    completions.add(new Completion(getOrderTranslation(AtlantisConstants.O_ACCEPT)));
    completions.add(new Completion(getOrderTranslation(AtlantisConstants.O_ADDRESS)));
    completions.add(new Completion(getOrderTranslation(AtlantisConstants.O_ADMIT)));
    completions.add(new Completion(getOrderTranslation(AtlantisConstants.O_ALLY)));
    completions.add(new Completion(getOrderTranslation(AtlantisConstants.O_BEHIND)));
    completions.add(new Completion(getOrderTranslation(AtlantisConstants.O_COMBAT)));
    completions.add(new Completion(getOrderTranslation(AtlantisConstants.O_DISPLAY)));
    completions.add(new Completion(getOrderTranslation(AtlantisConstants.O_GUARD)));
    completions.add(new Completion(getOrderTranslation(AtlantisConstants.O_NAME)));
    completions.add(new Completion(getOrderTranslation(AtlantisConstants.O_PASSWORD)));
    completions.add(new Completion(getOrderTranslation(AtlantisConstants.O_RESHOW)));
    completions.add(new Completion(getOrderTranslation(AtlantisConstants.O_FIND)));
    completions.add(new Completion(getOrderTranslation(AtlantisConstants.O_BOARD)));
    completions.add(new Completion(getOrderTranslation(AtlantisConstants.O_ENTER)));
    completions.add(new Completion(getOrderTranslation(AtlantisConstants.O_LEAVE)));
    completions.add(new Completion(getOrderTranslation(AtlantisConstants.O_PROMOTE)));
    completions.add(new Completion(getOrderTranslation(AtlantisConstants.O_ATTACK)));
    completions.add(new Completion(getOrderTranslation(AtlantisConstants.O_DEMOLISH)));
    completions.add(new Completion(getOrderTranslation(AtlantisConstants.O_GIVE)));
    completions.add(new Completion(getOrderTranslation(AtlantisConstants.O_PAY)));
    completions.add(new Completion(getOrderTranslation(AtlantisConstants.O_SINK)));
    completions.add(new Completion(getOrderTranslation(AtlantisConstants.O_TRANSFER)));
    completions.add(new Completion(getOrderTranslation(AtlantisConstants.O_TAX)));
    completions.add(new Completion(getOrderTranslation(AtlantisConstants.O_RECRUIT)));
    completions.add(new Completion(getOrderTranslation(AtlantisConstants.O_QUIT)));
    completions.add(new Completion(getOrderTranslation(AtlantisConstants.O_MOVE)));
    completions.add(new Completion(getOrderTranslation(AtlantisConstants.O_SAIL)));
    completions.add(new Completion(getOrderTranslation(AtlantisConstants.O_BUILD)));
    completions.add(new Completion(getOrderTranslation(AtlantisConstants.O_ENTERTAIN)));
    completions.add(new Completion(getOrderTranslation(AtlantisConstants.O_PRODUCE)));
    completions.add(new Completion(getOrderTranslation(AtlantisConstants.O_RESEARCH)));
    completions.add(new Completion(getOrderTranslation(AtlantisConstants.O_STUDY)));
    completions.add(new Completion(getOrderTranslation(AtlantisConstants.O_TEACH)));
    completions.add(new Completion(getOrderTranslation(AtlantisConstants.O_WORK)));
    completions.add(new Completion(getOrderTranslation(AtlantisConstants.O_CAST)));
  }

  public void cmpltId() {
    completions.add(new Completion("<id>", "", ""));
  }

  public void cmpltFlag() {
    completions.add(new Completion("0"));
    completions.add(new Completion("1"));
  }

}
