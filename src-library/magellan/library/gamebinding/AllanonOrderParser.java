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
import magellan.library.completion.OrderParser;
import magellan.library.utils.OrderToken;

/**
 * @author Thoralf Rickert
 * @version 1.0, 17.04.2008
 */
public class AllanonOrderParser extends EresseaOrderParser {
  /**
   * Creates a new <kbd>EresseaOrderParser</kbd> object.
   */
  public AllanonOrderParser(GameData data) {
    super(data, null);
  }

  /**
   * Creates a new <kbd>EresseaOrderParser</kbd> object and registers the specified
   * <kbd>OrderCompleter</kbd> object. This constructor should be used only by the
   * <kbd>OrderCompleter</kbd> class itself.
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
    addCheckedCommand(AllanonConstants.OC_ANWERBEN, new AnwerbenReader(this));
    addCheckedCommand(AllanonConstants.OC_ERKUNDEN, new ErkundeReader(this));
    addCheckedCommand(AllanonConstants.OC_MEUCHELN, new MeuchelnReader(this));
  }

  // ************* ANWERBEN
  protected class AnwerbenReader extends OrderHandler {
    AnwerbenReader(OrderParser parser) {
      super(parser);
    }

    @Override
    protected boolean readIt(OrderToken token) {
      token.ttype = OrderToken.TT_KEYWORD;

      getOrder().setLong(true);
      return checkNextFinal();
    }
  }

  // ************* ERKUNDE
  protected class ErkundeReader extends OrderHandler {
    ErkundeReader(OrderParser parser) {
      super(parser);
    }

    @Override
    protected boolean readIt(OrderToken token) {
      token.ttype = OrderToken.TT_KEYWORD;

      getOrder().setLong(true);
      return checkNextFinal();
    }
  }

  // ************* MEUCHELN
  protected class MeuchelnReader extends OrderHandler {
    MeuchelnReader(OrderParser parser) {
      super(parser);
    }

    @Override
    protected boolean readIt(OrderToken token) {
      boolean retVal = false;
      token.ttype = OrderToken.TT_KEYWORD;

      getOrder().setLong(true);
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
    public BetreteReader(OrderParser parser) {
      super(parser);
    }

    @Override
    protected boolean readIt(OrderToken token) {
      boolean retVal = false;
      token.ttype = OrderToken.TT_KEYWORD;

      OrderToken t = getNextToken();

      if (t.equalsToken(getOrderTranslation(EresseaConstants.OC_CASTLE))) {
        retVal = readBetreteBurg(t);
      } else if (t.equalsToken(getOrderTranslation(AllanonConstants.OC_KARAWANE))) {
        retVal = readBetreteKarawane(t);
      } else if (t.equalsToken(getOrderTranslation(EresseaConstants.OC_SHIP))) {
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
    public BenenneReader(OrderParser parser) {
      super(parser);
    }

    @Override
    protected boolean readIt(OrderToken token) {
      boolean retVal = false;
      token.ttype = OrderToken.TT_KEYWORD;

      OrderToken t = getNextToken();
      t.ttype = OrderToken.TT_KEYWORD;

      if (t.equalsToken(getOrderTranslation(EresseaConstants.OC_CASTLE))) {
        retVal = readDescription(false) != null;
      } else if (t.equalsToken(getOrderTranslation(EresseaConstants.OC_UNIT))) {
        retVal = readDescription(false) != null;
      } else if (t.equalsToken(getOrderTranslation(EresseaConstants.OC_PARAMETER_FACTION))) {
        retVal = readDescription(false) != null;
      } else if (t.equalsToken(getOrderTranslation(EresseaConstants.OC_REGION))) {
        retVal = readDescription(false) != null;
      } else if (t.equalsToken(getOrderTranslation(EresseaConstants.OC_SHIP))) {
        retVal = readDescription(false) != null;
      } else if (t.equalsToken(getOrderTranslation(AllanonConstants.OC_KARAWANE))) {
        retVal = readDescription(false) != null;
      } else if (t.equalsToken(getOrderTranslation(EresseaConstants.OC_FOREIGN))) {
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
