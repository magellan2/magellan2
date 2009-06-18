/*
 * Copyright (C) 2000-2004 Roger Butenuth, Andreas Gampe, Stefan Goetz, Sebastian Pappert, Klaas
 * Prause, Enno Rehling, Sebastian Tusk, Ulrich Kuester, Ilja Pavkovic This file is part of the
 * Eressea Java Code Base, see the file LICENSING for the licensing information applying to this
 * file.
 */

package magellan.library.gamebinding.e3a;

import magellan.library.GameData;
import magellan.library.gamebinding.EresseaConstants;
import magellan.library.gamebinding.EresseaOrderParser;
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
// removeCommand(Resources.getOrderTranslation(EresseaConstants.O_RESEARCH));
// removeCommand(Resources.getOrderTranslation(EresseaConstants.O_CONTACT));
// removeCommand(Resources.getOrderTranslation(EresseaConstants.O_FACTION));
// removeCommand(Resources.getOrderTranslation(EresseaConstants.O_REGION));
// removeCommand(Resources.getOrderTranslation(EresseaConstants.O_RECRUIT));
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

    removeCommand(Resources.getOrderTranslation(EresseaConstants.O_SIEGE));

    // TODO?
    // addCommand(Resources.getOrderTranslation(E3AConstants.O_ALLIANZ), new AllianzReader());
    // addCommand(Resources.getOrderTranslation(E3AConstants.O_GIVE), new GibReader());
    addCommand(Resources.getOrderTranslation(E3AConstants.O_RECRUIT), new RekrutiereReader());
    // addCommand(Resources.getOrderTranslation(E3AConstants.O_LEARNMAGIC), new XYZReader());
  }

  public E3AOrderCompleter getCompleter() {
    return this.completer;
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
      
      if (isNewString(t)){
        retVal = new StringChecker(false, false, false) {
          protected boolean check(OrderToken[] tokens) {
            if (tokens[0]!=null || tokens[2]!=null)
              return false;
            if (tokens[1]==null || getData()==null || getData().rules==null)
              return true;
            return getData().rules.getRace(tokens[1].getText())!=null;
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

}
