// class magellan.library.gamebinding.AllanonOrderParser
// created on 17.04.2008
//
// Copyright 2003-2008 by magellan project team
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
package magellan.library.gamebinding;

import magellan.library.GameData;
import magellan.library.utils.OrderToken;
import magellan.library.utils.Resources;

/**
 * @author Thoralf Rickert
 * @version 1.0, 17.04.2008
 */
public class AllanonOrderParser extends EresseaOrderParser {
  /**
   * Creates a new <tt>EresseaOrderParser</tt> object.
   */
  public AllanonOrderParser(GameData data) {
    super(data, null);
  }

  /**
   * Creates a new <tt>EresseaOrderParser</tt> object and registers the specified
   * <tt>OrderCompleter</tt> object. This constructor should be used only by the
   * <tt>OrderCompleter</tt> class itself.
   */
  public AllanonOrderParser(GameData data, AllanonOrderCompleter cc) {
    super(data, cc);
  }

  /**
   * @see magellan.library.gamebinding.EresseaOrderParser#initCommands()
   */
  @Override
  protected void initCommands() {
    super.initCommands();
    addCommand(Resources.getOrderTranslation(AllanonConstants.O_ANWERBEN), new AnwerbenReader());
    addCommand(Resources.getOrderTranslation(AllanonConstants.O_MEUCHELN), new MeuchelnReader());
  }

  // ************* ANWERBEN
  protected class AnwerbenReader extends OrderHandler {
    @Override
    public boolean read(OrderToken token) {
      token.ttype = OrderToken.TT_KEYWORD;

      return checkNextFinal();
    }
  }

  // ************* MEUCHELN
  protected class MeuchelnReader extends OrderHandler {
    @Override
    public boolean read(OrderToken token) {
      boolean retVal = false;
      token.ttype = OrderToken.TT_KEYWORD;

      OrderToken t = getNextToken();

      if (isID(t.getText()) == true) {
        retVal = readMeuchelnUID(t);
      } else {
        unexpected(t);
      }

      if (getCompleter() != null && !t.followedBySpace()) {
        // this is not optimal because the EresseaOrderCompleter does add an COMBAT state to the
        // unit command
        // but that isn't necessary for Allanon - but I don't want to change the whole API at the
        // moment.
        getCompleter().cmpltAttack();
      }

      return retVal;
    }

    public boolean readMeuchelnUID(OrderToken token) {
      token.ttype = OrderToken.TT_ID;
      return checkNextFinal();
    }
  }

  // ************* BETRETE
  protected class BetreteReader extends EresseaOrderParser.BetreteReader {
    @Override
    public boolean read(OrderToken token) {
      boolean retVal = false;
      token.ttype = OrderToken.TT_KEYWORD;

      OrderToken t = getNextToken();

      if (t.equalsToken(Resources.getOrderTranslation(EresseaConstants.O_CASTLE))) {
        retVal = readBetreteBurg(t);
      } else if (t.equalsToken(Resources.getOrderTranslation(AllanonConstants.O_KARAWANE))) {
        retVal = readBetreteKarawane(t);
      } else if (t.equalsToken(Resources.getOrderTranslation(EresseaConstants.O_SHIP))) {
        retVal = readBetreteSchiff(t);
      } else {
        unexpected(t);
      }

      if (getCompleter() != null && !t.followedBySpace()) {
        getCompleter().cmpltBetrete();
      }
      return retVal;
    }

    protected boolean readBetreteKarawane(OrderToken token) {
      boolean retVal = false;
      token.ttype = OrderToken.TT_KEYWORD;

      OrderToken t = getNextToken();

      if (isID(t.getText()) == true) {
        retVal = readBetreteKarawaneID(t);
      } else {
        unexpected(t);
      }

      if (getCompleter() != null && !t.followedBySpace()) {
        getCompleter().cmpltBetreteSchiff();
      }
      return retVal;
    }

    protected boolean readBetreteKarawaneID(OrderToken token) {
      token.ttype = OrderToken.TT_ID;

      return checkNextFinal();
    }
  }

  // ************* BENENNE
  protected class BenenneReader extends EresseaOrderParser.BenenneReader {
    @Override
    public boolean read(OrderToken token) {
      boolean retVal = false;
      token.ttype = OrderToken.TT_KEYWORD;

      OrderToken t = getNextToken();
      t.ttype = OrderToken.TT_KEYWORD;

      if (t.equalsToken(Resources.getOrderTranslation(EresseaConstants.O_CASTLE))) {
        retVal = readDescription(false);
      } else if (t.equalsToken(Resources.getOrderTranslation(EresseaConstants.O_UNIT))) {
        retVal = readDescription(false);
      } else if (t.equalsToken(Resources.getOrderTranslation(EresseaConstants.O_FACTION))) {
        retVal = readDescription(false);
      } else if (t.equalsToken(Resources.getOrderTranslation(EresseaConstants.O_REGION))) {
        retVal = readDescription(false);
      } else if (t.equalsToken(Resources.getOrderTranslation(EresseaConstants.O_SHIP))) {
        retVal = readDescription(false);
      } else if (t.equalsToken(Resources.getOrderTranslation(AllanonConstants.O_KARAWANE))) {
        retVal = readDescription(false);
      } else if (t.equalsToken(Resources.getOrderTranslation(EresseaConstants.O_FOREIGN))) {
        retVal = readBenenneFremdes(t);
      } else {
        t.ttype = OrderToken.TT_UNDEF;
        unexpected(t);
      }

      if (getCompleter() != null && !t.followedBySpace()) {
        getCompleter().cmpltBenenne();
      }

      return retVal;
    }
  }

}
