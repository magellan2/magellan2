/*
 * Copyright (C) 2000-2004 Roger Butenuth, Andreas Gampe, Stefan Goetz, Sebastian Pappert, Klaas
 * Prause, Enno Rehling, Sebastian Tusk, Ulrich Kuester, Ilja Pavkovic This file is part of the
 * Eressea Java Code Base, see the file LICENSING for the licensing information applying to this
 * file.
 */

package magellan.library.gamebinding.e3a;

import magellan.library.EntityID;
import magellan.library.Faction;
import magellan.library.GameData;
import magellan.library.gamebinding.EresseaConstants;
import magellan.library.gamebinding.EresseaOrderParser;
import magellan.library.rules.BuildingType;
import magellan.library.rules.Race;
import magellan.library.utils.OrderToken;
import magellan.library.utils.Resources;

/**
 */
public class E3AOrderParser extends EresseaOrderParser {

  private E3AOrderCompleter completer;

  /**
   * Creates a new <tt>EresseaOrderParser</tt> object.
   */
  public E3AOrderParser(GameData data) {
    super(data);
  }

  /**
   * Creates a new <tt>EresseaOrderParser</tt> object and registers the specified
   * <tt>OrderCompleter</tt> object. This constructor should be used only by the
   * <tt>OrderCompleter</tt> class itself.
   */
  public E3AOrderParser(GameData data, E3AOrderCompleter cc) {
    super(data, cc);
    this.completer = cc;
  }

  protected void initCommands() {
    super.initCommands();

    // TODO
    removeCommand(Resources.getOrderTranslation(EresseaConstants.O_RESEARCH));
// removeCommand(Resources.getOrderTranslation(EresseaConstants.O_FACTION));
// removeCommand(Resources.getOrderTranslation(EresseaConstants.O_REGION));
    // removeCommand(Resources.getOrderTranslation(EresseaConstants.O_GROW));

    removeCommand(Resources.getOrderTranslation(EresseaConstants.O_SPY));
    removeCommand(Resources.getOrderTranslation(EresseaConstants.O_STEAL));
    removeCommand(Resources.getOrderTranslation(EresseaConstants.O_HIDE));

    removeCommand(Resources.getOrderTranslation(EresseaConstants.O_STEAL));

    // removeCommand(Resources.getOrderTranslation(EresseaConstants.O_WORK));
    removeCommand(Resources.getOrderTranslation(EresseaConstants.O_TAX));
    removeCommand(Resources.getOrderTranslation(EresseaConstants.O_ENTERTAIN));

    removeCommand(Resources.getOrderTranslation(EresseaConstants.O_BUY)); // ?
    removeCommand(Resources.getOrderTranslation(EresseaConstants.O_SELL));

    removeCommand(Resources.getOrderTranslation(EresseaConstants.O_TEACH));

    removeCommand(Resources.getOrderTranslation(EresseaConstants.O_SUPPLY));

    removeCommand(Resources.getOrderTranslation(EresseaConstants.O_SABOTAGE));

// removeCommand(Resources.getOrderTranslation(EresseaConstants.O_SIEGE));

    // TODO?
    addCommand(Resources.getOrderTranslation(E3AConstants.O_ALLIANCE), new AllianzReader());
    // addCommand(Resources.getOrderTranslation(E3AConstants.O_GIVE), new GibReader());
    addCommand(Resources.getOrderTranslation(E3AConstants.O_MAKE), new E3MacheReader());
    addCommand(Resources.getOrderTranslation(E3AConstants.O_RECRUIT), new RekrutiereReader());
    // addCommand(Resources.getOrderTranslation(E3AConstants.O_LEARNMAGIC), new XYZReader());
  }

  public E3AOrderCompleter getCompleter() {
    return this.completer;
  }

  // ************* ALLIANZ
  protected class AllianzReader extends OrderHandler {
    public boolean read(OrderToken token) {
      boolean retVal = false;
      token.ttype = OrderToken.TT_KEYWORD;

      OrderToken t = getNextToken();

      if (t.ttype == OrderToken.TT_EOC) {
        unexpected(t);
      } else if (t.equalsToken(Resources.getOrderTranslation(E3AConstants.O_ALLIANCE_KICK))) {
        retVal = readAllianzFactionID(t);
      } else if (t.equalsToken(Resources.getOrderTranslation(E3AConstants.O_ALLIANCE_LEAVE))) {
        t.ttype = OrderToken.TT_KEYWORD;
        retVal = checkNextFinal();
      } else if (t.equalsToken(Resources.getOrderTranslation(E3AConstants.O_ALLIANCE_COMMAND))) {
        retVal = readAllianzFactionID(t);
      } else if (t.equalsToken(Resources.getOrderTranslation(E3AConstants.O_ALLIANCE_NEW))) {
        t.ttype = OrderToken.TT_KEYWORD;
        retVal = checkNextFinal();
      } else if (t.equalsToken(Resources.getOrderTranslation(E3AConstants.O_ALLIANCE_INVITE))) {
        retVal = readAllianzFactionID(t);
      } else if (t.equalsToken(Resources.getOrderTranslation(E3AConstants.O_ALLIANCE_JOIN))) {
        retVal = readAllianzBeitreten(t);
      } else {
        unexpected(t);
      }

      if (getCompleter() != null && !t.followedBySpace()) {
        getCompleter().cmpltAllianz();
      }

      return retVal;
    }

    protected boolean readAllianzBeitreten(OrderToken token) {
      token.ttype = OrderToken.TT_KEYWORD;

      OrderToken t = getNextToken();

      if (isID(t.getText(), false))
        return readFinalID(t);
      else {
        unexpected(t);
      }

      return false;
    }

    protected boolean readAllianzFactionID(OrderToken token) {
      boolean retVal = false;
      token.ttype = OrderToken.TT_KEYWORD;

      OrderToken t = getNextToken();

      if (isID(t.getText(), false)) {
        t.ttype = OrderToken.TT_ID;
        Faction faction = getData().getFaction(EntityID.createEntityID(t.getText(), getData().base));
        retVal = faction != null && checkNextFinal();
      } else {
        unexpected(t);
      }

      if (getCompleter() != null && !t.followedBySpace()) {
        getCompleter().cmpltFactions("");
      }

      return retVal;
    }
  }

//************* BENENNE
  protected class BenenneReader extends EresseaOrderParser.BenenneReader {
    public boolean read(OrderToken token) {
      boolean retVal = false;
      token.ttype = OrderToken.TT_KEYWORD;

      OrderToken t = getNextToken();
      t.ttype = OrderToken.TT_KEYWORD;
      if (t.equalsToken(Resources.getOrderTranslation(E3AConstants.O_ALLIANCE))) {
        retVal = readDescription(false);
      } else if (t.equalsToken(Resources.getOrderTranslation(EresseaConstants.O_CASTLE))) {
        retVal = readDescription(false);
      } else if (t.equalsToken(Resources.getOrderTranslation(EresseaConstants.O_UNIT))) {
        retVal = readDescription(false);
      } else if (t.equalsToken(Resources.getOrderTranslation(EresseaConstants.O_FACTION))) {
        retVal = readDescription(false);
      } else if (t.equalsToken(Resources.getOrderTranslation(EresseaConstants.O_REGION))) {
        retVal = readDescription(false);
      } else if (t.equalsToken(Resources.getOrderTranslation(EresseaConstants.O_SHIP))) {
        retVal = readDescription(false);
      } else if (t.equalsToken(Resources.getOrderTranslation(EresseaConstants.O_FOREIGNBUILDING))) {
        retVal = readBenenneFremdes(t);
      } else if (t.equalsToken(Resources.getOrderTranslation(EresseaConstants.O_FOREIGNFACTION))) {
        retVal = readBenenneFremdes(t);
      } else if (t.equalsToken(Resources.getOrderTranslation(EresseaConstants.O_FOREIGNSHIP))) {
        retVal = readBenenneFremdes(t);
      } else if (t.equalsToken(Resources.getOrderTranslation(EresseaConstants.O_FOREIGNUNIT))) {
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

  // ************* MACHE
  protected class E3MacheReader extends MacheReader {
    @Override
    protected BuildingType isCastle(OrderToken t) {
      if (t.equalsToken(Resources.getOrderTranslation(E3AConstants.O_WATCH)))
        if (getData().rules != null)
          return getData().rules.getCastleType(E3AConstants.B_GUARDTOWER);
      return null;
    }
  }

  
  
  // ************* REKRUTIERE
  protected class RekrutiereReader extends OrderHandler {
    public boolean read(OrderToken token) {
      boolean retVal = false;
      token.ttype = OrderToken.TT_KEYWORD;

      OrderToken t = getNextToken();

      if (isNumeric(t.getText()) == true) {
        retVal = readRekrutiereAmount(t);
      } else {
        unexpected(t);
      }

      if (getCompleter() != null && !t.followedBySpace()) {
        getCompleter().cmpltRekrutiere();
      }

      return retVal;
    }

    protected boolean readRekrutiereAmount(OrderToken token) {
      boolean retVal = false;
      token.ttype = OrderToken.TT_NUMBER;

      OrderToken t = getNextToken();

      if (isString(t)) {
        retVal = new StringChecker(false, false, false, false) {
          @Override
          protected boolean checkInner() {
            if (innerToken == null || getData() == null || getData().rules == null)
              return true;
            return getRace(content) != null;
          }

        }.read(t);
      } else {
        retVal = checkFinal(t);
      }

      if (getCompleter() != null && !t.followedBySpace()) {
        getCompleter().cmpltRekrutiereAmount();
      }

      return retVal;
    }
  }

  private Race getRace(String content) {
    for (Race r : getData().rules.getRaces()) {
      if (r.getRecruitmentName() != null
          && content.equalsIgnoreCase(Resources.getOrderTranslation("race."
              + r.getRecruitmentName())))
        return r;
    }
    return null;
  }
  
}
