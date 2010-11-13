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
import magellan.library.gamebinding.RenameOrder;
import magellan.library.rules.BuildingType;
import magellan.library.rules.Race;
import magellan.library.utils.OrderToken;

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
    completer = cc;
  }

  @Override
  protected void initCommands() {
    super.initCommands();

    // TODO
    removeCommand(EresseaConstants.O_RESEARCH);
    // removeCommand(EresseaConstants.O_FACTION);
    // removeCommand(EresseaConstants.O_REGION);
    // removeCommand(EresseaConstants.O_GROW);

    removeCommand(EresseaConstants.O_SPY);
    removeCommand(EresseaConstants.O_STEAL);
    removeCommand(EresseaConstants.O_HIDE);

    removeCommand(EresseaConstants.O_STEAL);

    // removeCommand(EresseaConstants.O_WORK);
    removeCommand(EresseaConstants.O_TAX);
    removeCommand(EresseaConstants.O_ENTERTAIN);

    removeCommand(EresseaConstants.O_BUY); // ?
    removeCommand(EresseaConstants.O_SELL);

    removeCommand(EresseaConstants.O_TEACH);

    removeCommand(EresseaConstants.O_SUPPLY);

    removeCommand(EresseaConstants.O_SABOTAGE);

    // removeCommand(EresseaConstants.O_SIEGE);

    // TODO?
    addCommand(E3AConstants.O_ALLIANCE, new AllianzReader());
    addCommand(E3AConstants.O_PAY, new BezahleReader());
    // addCommand(E3AConstants.O_GIVE, new GibReader());
    addCommand(EresseaConstants.O_MAKE, new E3MacheReader());
    addCommand(EresseaConstants.O_RECRUIT, new RekrutiereReader());
    // addCommand(E3AConstants.O_LEARNMAGIC, new XYZReader());
  }

  @Override
  public E3AOrderCompleter getCompleter() {
    return completer;
  }

  // ************* ALLIANZ
  protected class AllianzReader extends OrderHandler {
    @Override
    protected boolean readIt(OrderToken token) {
      boolean retVal = false;
      token.ttype = OrderToken.TT_KEYWORD;

      OrderToken t = getNextToken();

      if (isEoC(t)) {
        unexpected(t);
      } else if (t.equalsToken(getOrderTranslation(E3AConstants.O_ALLIANCE_KICK))) {
        retVal = readAllianzFactionID(t);
      } else if (t.equalsToken(getOrderTranslation(E3AConstants.O_ALLIANCE_LEAVE))) {
        t.ttype = OrderToken.TT_KEYWORD;
        retVal = checkNextFinal();
      } else if (t.equalsToken(getOrderTranslation(E3AConstants.O_ALLIANCE_COMMAND))) {
        retVal = readAllianzFactionID(t);
      } else if (t.equalsToken(getOrderTranslation(E3AConstants.O_ALLIANCE_NEW))) {
        t.ttype = OrderToken.TT_KEYWORD;
        retVal = checkNextFinal();
      } else if (t.equalsToken(getOrderTranslation(E3AConstants.O_ALLIANCE_INVITE))) {
        retVal = readAllianzFactionID(t);
      } else if (t.equalsToken(getOrderTranslation(E3AConstants.O_ALLIANCE_JOIN))) {
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
        Faction faction =
            getData().getFaction(EntityID.createEntityID(t.getText(), getData().base));
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

  // ************* BENENNE
  protected class BenenneReader extends EresseaOrderParser.BenenneReader {

    @Override
    protected boolean readIt(OrderToken token) {
      boolean retVal = false;
      token.ttype = OrderToken.TT_KEYWORD;

      OrderToken t = getNextToken();
      t.ttype = OrderToken.TT_KEYWORD;
      if (t.equalsToken(getOrderTranslation(E3AConstants.O_ALLIANCE))) {
        // FIXME getOrder().type = RenameOrder.T_ALLIANCE;
        getOrder().name = readDescription(false);
        retVal = readDescription(false) != null;
      } else if (t.equalsToken(getOrderTranslation(EresseaConstants.O_CASTLE))) {
        getOrder().type = RenameOrder.T_BUILDING;
        getOrder().name = readDescription(false);
        retVal = readDescription(false) != null;
      } else if (t.equalsToken(getOrderTranslation(EresseaConstants.O_UNIT))) {
        getOrder().type = RenameOrder.T_UNIT;
        getOrder().name = readDescription(false);
        retVal = readDescription(false) != null;
      } else if (t.equalsToken(getOrderTranslation(EresseaConstants.O_FACTION))) {
        getOrder().type = RenameOrder.T_FACTION;
        getOrder().name = readDescription(false);
        retVal = readDescription(false) != null;
      } else if (t.equalsToken(getOrderTranslation(EresseaConstants.O_REGION))) {
        getOrder().type = RenameOrder.T_REGION;
        getOrder().name = readDescription(false);
        retVal = readDescription(false) != null;
      } else if (t.equalsToken(getOrderTranslation(EresseaConstants.O_SHIP))) {
        getOrder().type = RenameOrder.T_SHIP;
        getOrder().name = readDescription(false);
        retVal = readDescription(false) != null;
      } else if (t.equalsToken(getOrderTranslation(EresseaConstants.O_FOREIGNBUILDING))) {
        retVal = readBenenneFremdes(t);
      } else if (t.equalsToken(getOrderTranslation(EresseaConstants.O_FOREIGNFACTION))) {
        retVal = readBenenneFremdes(t);
      } else if (t.equalsToken(getOrderTranslation(EresseaConstants.O_FOREIGNSHIP))) {
        retVal = readBenenneFremdes(t);
      } else if (t.equalsToken(getOrderTranslation(EresseaConstants.O_FOREIGNUNIT))) {
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

  // ************* BEZAHLE
  protected class BezahleReader extends OrderHandler {
    @Override
    protected boolean readIt(OrderToken token) {
      boolean retVal = false;
      token.ttype = OrderToken.TT_KEYWORD;

      OrderToken t = getNextToken();

      if (t.equalsToken(getOrderTranslation(EresseaConstants.O_NOT))) {
        retVal = readBewacheNicht(t);
      } else {
        retVal = false;

      }

      if (getCompleter() != null && !t.followedBySpace()) {
        getCompleter().cmpltBezahle();
      }
      return retVal;
    }

    protected boolean readBewacheNicht(OrderToken token) {
      token.ttype = OrderToken.TT_KEYWORD;

      return checkNextFinal();
    }
  }

  // ************* MACHE
  protected class E3MacheReader extends MacheReader {
    @Override
    protected BuildingType isCastle(OrderToken t) {
      if (t.equalsToken(getOrderTranslation(E3AConstants.O_WATCH)))
        if (getRules() != null)
          return getRules().getCastleType(E3AConstants.B_GUARDTOWER);
      return null;
    }
  }

  // ************* REKRUTIERE
  protected class RekrutiereReader extends EresseaOrderParser.RekrutiereReader {
    @Override
    protected boolean readIt(OrderToken token) {
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
      int val = getNumber(token.getText());
      getOrder().setAmount(val);

      OrderToken t = getNextToken();

      if (isString(t)) {
        retVal = new StringChecker(false, false, false, false) {
          @Override
          protected boolean checkInner() {
            if (innerToken == null || getData() == null || getRules() == null)
              return true;
            Race race = getRace(content);
            getOrder().setRace(race);
            return race != null;
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
    for (Race r : getRules().getRaces())
      if (r.getRecruitmentName() != null
          && content.equalsIgnoreCase(getOrderTranslation("race." + r.getRecruitmentName())))
        return r;
    return null;
  }

}
