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

package magellan.library.gamebinding;

import java.io.Reader;
import java.io.StringReader;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

import magellan.library.GameData;
import magellan.library.Spell;
import magellan.library.UnitID;
import magellan.library.completion.OrderParser;
import magellan.library.rules.BuildingType;
import magellan.library.rules.CastleType;
import magellan.library.rules.ItemCategory;
import magellan.library.rules.ItemType;
import magellan.library.utils.Direction;
import magellan.library.utils.IDBaseConverter;
import magellan.library.utils.OrderToken;
import magellan.library.utils.OrderTokenizer;
import magellan.library.utils.Resources;
import magellan.library.utils.logging.Logger;


/**
 * A class for reading Eressea orders and checking their syntactical correctness. A
 * <tt>OrderParser</tt> object can register a <tt>OrderCompleter</tt> object. In such a case the
 * <tt>OrderParser</tt> will call the corresponding methods of the <tt>OrderCompleter</tt> if it
 * encounters an incomplete order.
 */
public class EresseaOrderParser implements OrderParser {
	private static final Logger log = Logger.getInstance(EresseaOrderParser.class);

	// this is not entirely true with dynamic bases but it probably doesn't really hurt
	private static final int MAX_UID = 1679615;

  private static final char[] QUOTES = new char[] { '\'', '"'};
	private String errMsg = null;
	private TokenBucket tokenBucket = null;
	private Iterator<OrderToken> tokens = null;
	private EresseaOrderCompleter completer = null;
	private GameData data = null;

	/**
	 * Creates a new <tt>EresseaOrderParser</tt> object.
	 *
	 * 
	 */
	public EresseaOrderParser(GameData data) {
		this(data, null);
	}

	/**
	 * Creates a new <tt>EresseaOrderParser</tt> object and registers the specified
	 * <tt>OrderCompleter</tt> object. This constructor should be used only by the
	 * <tt>OrderCompleter</tt> class itself.
	 *
	 * 
	 * 
	 */
	public EresseaOrderParser(GameData data, EresseaOrderCompleter cc) {
		tokenBucket = new TokenBucket();
		completer = cc;
		this.data = data;
	}

	/**
	 * Returns the tokens read by the parser.
	 *
	 * @return all <tt>OrderToken</tt> object produced by the underlying <tt>OrderTokenizer</tt> by
	 * 		   reading a order.
	 */
	public List getTokens() {
		return tokenBucket;
	}

	/**
	 * Returns the error messages produced by the last invocation of the <tt>read(Reader in)</tt>
	 * method.
	 *
	 * @return an error message if the last <tt>read</tt> returned <tt>false</tt>, <tt>null</tt>
	 * 		   else.
	 */
	public String getErrorMessage() {
		return errMsg;
	}

	/**
	 * Parses one line of text from the specified stream by tokenizing it and checking the syntax.
	 *
	 * @param in the stream to read the order from.
	 *
	 * @return <tt>true</tt> if the syntax of the order read is valid, <tt>false</tt> else.
	 */
	public boolean read(Reader in) {
		errMsg = null;
		tokenBucket.read(in);
		tokenBucket.mergeTempTokens(data.base);
		tokens = tokenBucket.iterator();

		boolean retVal = true;

		while(tokens.hasNext() && retVal) {
			OrderToken token = tokens.next();

			if(token.ttype != OrderToken.TT_COMMENT) {
				retVal = readOrder(token);
			}
		}

		return retVal;
	}

	protected boolean readOrder(OrderToken t) {
		boolean retVal = false;

		if(t.ttype == OrderToken.TT_PERSIST) {
			retVal = readAt(t);
		} else if(t.equalsToken(Resources.getOrderTranslation(EresseaConstants.O_WORK))) {
			retVal = readWork(t);
		} else if(t.equalsToken(Resources.getOrderTranslation(EresseaConstants.O_ATTACK))) {
			retVal = readAttack(t);
		} else if(t.equalsToken(Resources.getOrderTranslation(EresseaConstants.O_BANNER))) {
			retVal = readBanner(t);
		} else if(t.equalsToken(Resources.getOrderTranslation(EresseaConstants.O_CLAIM))) {
			retVal = readBeanspruche(t);
		} else if(t.equalsToken(Resources.getOrderTranslation(EresseaConstants.O_PROMOTION))) {
			retVal = readBefoerderung(t);
		} else if(t.equalsToken(Resources.getOrderTranslation(EresseaConstants.O_STEAL))) {
			retVal = readBeklaue(t);
		} else if(t.equalsToken(Resources.getOrderTranslation(EresseaConstants.O_SIEGE))) {
			retVal = readBelagere(t);
		} else if(t.equalsToken(Resources.getOrderTranslation(EresseaConstants.O_NAME))) {
			retVal = readBenenne(t);
		} else if(t.equalsToken(Resources.getOrderTranslation(EresseaConstants.O_USE))) {
			retVal = readBenutze(t);
		} else if(t.equalsToken(Resources.getOrderTranslation(EresseaConstants.O_DESCRIBE))) {
			retVal = readBeschreibe(t);
		} else if(t.equalsToken(Resources.getOrderTranslation(EresseaConstants.O_ENTER))) {
			retVal = readBetrete(t);
		} else if(t.equalsToken(Resources.getOrderTranslation(EresseaConstants.O_GUARD))) {
			retVal = readBewache(t);
		} else if(t.equalsToken(Resources.getOrderTranslation(EresseaConstants.O_MESSAGE))) {
			retVal = readBotschaft(t);
		} else if(t.equalsToken(Resources.getOrderTranslation(EresseaConstants.O_DEFAULT))) {
			retVal = readDefault(t);
		} else if(t.equalsToken(Resources.getOrderTranslation(EresseaConstants.O_EMAIL))) {
			retVal = readEmail(t);
		} else if(t.equalsToken(Resources.getOrderTranslation(EresseaConstants.O_END))) {
			retVal = readEnde(t);
		} else if(t.equalsToken(Resources.getOrderTranslation(EresseaConstants.O_RIDE))) {
			retVal = readFahre(t);
		} else if(t.equalsToken(Resources.getOrderTranslation(EresseaConstants.O_FOLLOW))) {
			retVal = readFolge(t);
		} else if(t.equalsToken(Resources.getOrderTranslation(EresseaConstants.O_RESEARCH))) {
			retVal = readForsche(t);
		} else if(t.equalsToken(Resources.getOrderTranslation(EresseaConstants.O_GIVE))) {
			retVal = readGib(t);
		} else if(t.equalsToken(Resources.getOrderTranslation(EresseaConstants.O_GROUP))) {
			retVal = readGruppe(t);
		} else if(t.equalsToken(Resources.getOrderTranslation(EresseaConstants.O_HELP))) {
			retVal = readHelfe(t);
		} else if(t.equalsToken(Resources.getOrderTranslation(EresseaConstants.O_COMBAT))) {
			retVal = readKaempfe(t);
		} else if(t.equalsToken(Resources.getOrderTranslation(EresseaConstants.O_COMBATSPELL))) {
			retVal = readKampfzauber(t);
		} else if(t.equalsToken(Resources.getOrderTranslation(EresseaConstants.O_BUY))) {
			retVal = readKaufe(t);
		} else if(t.equalsToken(Resources.getOrderTranslation(EresseaConstants.O_CONTACT))) {
			retVal = readKontaktiere(t);
		} else if(t.equalsToken(Resources.getOrderTranslation(EresseaConstants.O_TEACH))) {
			retVal = readLehre(t);
		} else if(t.equalsToken(Resources.getOrderTranslation(EresseaConstants.O_LEARN))) {
			retVal = readLerne(t);
		} else if(t.equalsToken(Resources.getOrderTranslation(EresseaConstants.O_SUPPLY))) {
			retVal = readGib(t);
		} else if(t.equalsToken(Resources.getOrderTranslation(EresseaConstants.O_LOCALE))) {
			retVal = readLocale(t);
		} else if(t.equalsToken(Resources.getOrderTranslation(EresseaConstants.O_MAKE))) {
			retVal = readMache(t);
		} else if(t.equalsToken(Resources.getOrderTranslation(EresseaConstants.O_MOVE))) {
			retVal = readNach(t);
		} else if(t.equalsToken(Resources.getOrderTranslation(EresseaConstants.O_NEXT))) {
			retVal = readFinalKeyword(t);
		} else if(t.equalsToken(Resources.getOrderTranslation(EresseaConstants.O_NUMBER))) {
			retVal = readNummer(t);
		} else if(t.equalsToken(Resources.getOrderTranslation(EresseaConstants.O_OPTION))) {
			retVal = readOption(t);
		} else if(t.equalsToken(Resources.getOrderTranslation(EresseaConstants.O_FACTION))) {
			retVal = readPartei(t);
		} else if(t.equalsToken(Resources.getOrderTranslation(EresseaConstants.O_PASSWORD))) {
			retVal = readPasswort(t);
		} else if(t.equalsToken(Resources.getOrderTranslation(EresseaConstants.O_PLANT))) {
			retVal = readPflanzen(t);
		} else if(t.equalsToken(Resources.getOrderTranslation(EresseaConstants.O_PIRACY))) {
			retVal = readPiraterie(t);
		} else if(t.equalsToken(Resources.getOrderTranslation(EresseaConstants.O_PREFIX))) {
			retVal = readPraefix(t);
		} else if(t.equalsToken(Resources.getOrderTranslation(EresseaConstants.O_REGION))) {
			retVal = readRegion(t);
		} else if(t.equalsToken(Resources.getOrderTranslation(EresseaConstants.O_RECRUIT))) {
			retVal = readRekrutiere(t);
		} else if(t.equalsToken(Resources.getOrderTranslation(EresseaConstants.O_RESERVE))) {
			retVal = readReserviere(t);
		} else if(t.equalsToken(Resources.getOrderTranslation(EresseaConstants.O_ROUTE))) {
			retVal = readRoute(t);
		} else if(t.equalsToken(Resources.getOrderTranslation(EresseaConstants.O_SORT))) {
			retVal = readSortiere(t);
		} else if(t.equalsToken(Resources.getOrderTranslation(EresseaConstants.O_SPY))) {
			retVal = readSpioniere(t);
		} else if(t.equalsToken(Resources.getOrderTranslation(EresseaConstants.O_QUIT))) {
			retVal = readStirb(t);
		} else if(t.equalsToken(Resources.getOrderTranslation(EresseaConstants.O_HIDE))) {
			retVal = readTarne(t);
		} else if(t.equalsToken(Resources.getOrderTranslation(EresseaConstants.O_CARRY))) {
			retVal = readTransportiere(t);
		} else if(t.equalsToken(Resources.getOrderTranslation(EresseaConstants.O_TAX))) {
			retVal = readTreibe(t);
		} else if(t.equalsToken(Resources.getOrderTranslation(EresseaConstants.O_ENTERTAIN))) {
			retVal = readUnterhalte(t);
		} else if(t.equalsToken(Resources.getOrderTranslation(EresseaConstants.O_ORIGIN))) {
			retVal = readUrsprung(t);
		} else if(t.equalsToken(Resources.getOrderTranslation(EresseaConstants.O_FORGET))) {
			retVal = readVergesse(t);
		} else if(t.equalsToken(Resources.getOrderTranslation(EresseaConstants.O_SELL))) {
			retVal = readVerkaufe(t);
		} else if(t.equalsToken(Resources.getOrderTranslation(EresseaConstants.O_LEAVE))) {
			retVal = readFinalKeyword(t);
		} else if(t.equalsToken(Resources.getOrderTranslation(EresseaConstants.O_CAST))) {
			retVal = readZaubere(t);
		} else if(t.equalsToken(Resources.getOrderTranslation(EresseaConstants.O_SHOW))) {
			retVal = readZeige(t);
		} else if(t.equalsToken(Resources.getOrderTranslation(EresseaConstants.O_DESTROY))) {
			retVal = readZerstoere(t);
		} else if(t.equalsToken(Resources.getOrderTranslation(EresseaConstants.O_GROW))) {
			retVal = readZuechte(t);
		} else {
			retVal = checkFinal(t);
		}
    if(completer != null && !t.followedBySpace() && t.ttype != OrderToken.TT_PERSIST) {
      completer.cmplt();
    }

		return retVal;
	}

	//************* AT
	private boolean readAt(OrderToken token) {
		OrderToken t = tokens.next();

		return readOrder(t);
	}
  
	//************* WORK (ARBEITE)
	private boolean readWork(OrderToken token) {
		token.ttype = OrderToken.TT_KEYWORD;

		return checkNextFinal();
	}

	//************* ATTACK (ATTACKIERE)
	private boolean readAttack(OrderToken token) {
		boolean retVal = false;
		token.ttype = OrderToken.TT_KEYWORD;

		OrderToken t = tokens.next();

		if(isID(t.getText()) == true) {
			retVal = readAttackUID(t);
		} else {
			unexpected(t);
		}

    if(completer != null && !t.followedBySpace()) {
      completer.cmpltAttack();
    }

		return retVal;
	}

	private boolean readAttackUID(OrderToken token) {
		token.ttype = OrderToken.TT_ID;
		return checkNextFinal();
	}

	//************* BANNER
	private boolean readBanner(OrderToken token) {
		boolean retVal = false;
		token.ttype = OrderToken.TT_KEYWORD;

		OrderToken t = tokens.next();

		if(isQuoted(t.getText()) == true) {
			retVal = readFinalString(t);
		} else {
			unexpected(t);
		}

		return retVal;
	}

	//************* BEFÖRDERUNG
	private boolean readBefoerderung(OrderToken token) {
		token.ttype = OrderToken.TT_KEYWORD;
		
		return checkNextFinal();
	}

	//************* BEKLAUE
	private boolean readBeklaue(OrderToken token) {
		boolean retVal = false;
		token.ttype = OrderToken.TT_KEYWORD;

		OrderToken t = tokens.next();

		if(isID(t.getText()) == true) {
			retVal = readBeklaueUID(t);
		} else {
			unexpected(t);
		}

    if(completer != null && !t.followedBySpace()) {
      completer.cmpltBeklaue();
    }

		return retVal;
	}

	private boolean readBeklaueUID(OrderToken token) {
		token.ttype = OrderToken.TT_ID;

		return checkNextFinal();
	}

	//************* BELAGERE
	private boolean readBelagere(OrderToken token) {
		boolean retVal = false;
		token.ttype = OrderToken.TT_KEYWORD;

		OrderToken t = tokens.next();

		if(isID(t.getText()) == true) {
			retVal = readBelagereBID(t);
		} else {
			unexpected(t);
		}

    if(completer != null && !t.followedBySpace()) {
      completer.cmpltBelagere();
    }

		return retVal;
	}

	private boolean readBelagereBID(OrderToken token) {
		token.ttype = OrderToken.TT_ID;

		return checkNextFinal();
	}

	//************* BENENNE
	protected boolean readBenenne(OrderToken token) {
		boolean retVal = false;
		token.ttype = OrderToken.TT_KEYWORD;

		OrderToken t = tokens.next();

		if(t.equalsToken(Resources.getOrderTranslation(EresseaConstants.O_CASTLE))) {
			retVal = readBenenneBeschreibeTarget(t);
		} else if(t.equalsToken(Resources.getOrderTranslation(EresseaConstants.O_UNIT))) {
			retVal = readBenenneBeschreibeTarget(t);
		} else if(t.equalsToken(Resources.getOrderTranslation(EresseaConstants.O_FACTION))) {
			retVal = readBenenneBeschreibeTarget(t);
		} else if(t.equalsToken(Resources.getOrderTranslation(EresseaConstants.O_REGION))) {
			retVal = readBenenneBeschreibeTarget(t);
		} else if(t.equalsToken(Resources.getOrderTranslation(EresseaConstants.O_SHIP))) {
			retVal = readBenenneBeschreibeTarget(t);
		} else if(t.equalsToken(Resources.getOrderTranslation(EresseaConstants.O_FOREIGN))) {
			retVal = readBenenneFremdes(t);
		} else {
			unexpected(t);
		}

    if(completer != null && !t.followedBySpace()) {
      completer.cmpltBenenne();
    }

		return retVal;
	}

	protected boolean readBenenneBeschreibeTarget(OrderToken token) {
		boolean retVal = false;
		token.ttype = OrderToken.TT_KEYWORD;

		OrderToken t = tokens.next();

		if(isString(t.getText()) == true) {
			retVal = readFinalString(t);
		} else {
			unexpected(t);
		}

		return retVal;
	}

	protected boolean readBenenneFremdes(OrderToken token) {
		boolean retVal = false;
		token.ttype = OrderToken.TT_KEYWORD;

		OrderToken t = tokens.next();

		if(t.equalsToken(Resources.getOrderTranslation(EresseaConstants.O_UNIT))) {
			retVal = readBenenneFremdeEinheit(t);
		} else if(t.equalsToken(Resources.getOrderTranslation(EresseaConstants.O_CASTLE))) {
			retVal = readBenenneFremdesGebaeude(t);
		} else if(t.equalsToken(Resources.getOrderTranslation(EresseaConstants.O_FACTION))) {
			retVal = readBenenneFremdePartei(t);
		} else if(t.equalsToken(Resources.getOrderTranslation(EresseaConstants.O_SHIP))) {
			retVal = readBenenneFremdesSchiff(t);
		} else {
			unexpected(t);
		}

    if(completer != null && !t.followedBySpace()) {
      completer.cmpltBenenneFremdes();
    }

		return retVal;
	}

	private boolean readBenenneFremdeEinheit(OrderToken token) {
		boolean retVal = false;
		token.ttype = OrderToken.TT_KEYWORD;

		OrderToken t = tokens.next();

		if(isID(t.getText())) {
			retVal = readBenenneFremdesTargetID(t);
		} else {
			unexpected(t);
		}

    if(completer != null && !t.followedBySpace()) {
      completer.cmpltBenenneFremdeEinheit();
    }

		return retVal;
	}

	private boolean readBenenneFremdesGebaeude(OrderToken token) {
		boolean retVal = false;
		token.ttype = OrderToken.TT_KEYWORD;

		OrderToken t = tokens.next();

		if(isID(t.getText())) {
			retVal = readBenenneFremdesTargetID(t);
		} else {
			unexpected(t);
		}
    if(completer != null && !t.followedBySpace()) {
      completer.cmpltBenenneFremdesGebaeude();
    }

		return retVal;
	}

	private boolean readBenenneFremdePartei(OrderToken token) {
		boolean retVal = false;
		token.ttype = OrderToken.TT_KEYWORD;

		OrderToken t = tokens.next();

		if(isID(t.getText())) {
			retVal = readBenenneFremdesTargetID(t);
		} else {
			unexpected(t);
		}

    if(completer != null && !t.followedBySpace()) {
      completer.cmpltBenenneFremdePartei();
    }

		return retVal;
	}

	private boolean readBenenneFremdesSchiff(OrderToken token) {
		boolean retVal = false;
		token.ttype = OrderToken.TT_KEYWORD;

		OrderToken t = tokens.next();

		if(isID(t.getText())) {
			retVal = readBenenneFremdesTargetID(t);
		} else {
			unexpected(t);
		}

		if(completer!=null && !t.followedBySpace()){
				completer.cmpltBenenneFremdesSchiff(); 
		}
		return retVal;
	}

	private boolean readBenenneFremdesTargetID(OrderToken token) {
		boolean retVal = false;
		token.ttype = OrderToken.TT_ID;

		OrderToken t = tokens.next();

		if(isString(t.getText())) {
			retVal = readFinalString(t);
		} else {
			unexpected(t);
		}

		if(completer!=null && !t.followedBySpace()){
				completer.cmpltBenenneFremdesTargetID(); 
		}
		return retVal;
	}

	//************* BENUTZE
	private boolean readBenutze(OrderToken token) {
		boolean retVal = false;
		token.ttype = OrderToken.TT_KEYWORD;

		OrderToken t = tokens.next();

		if(isString(t.getText()) == true) {
			retVal = readFinalString(t);
		} else if (isNumeric(t.getText())){
      retVal = readBenutzeAmount(t); 
    }    
     else {
			unexpected(t);
		}

		if(completer!=null && !t.followedBySpace()){
				completer.cmpltBenutze(0); 
		}
		return retVal;
	}

  
  private boolean readBenutzeAmount(OrderToken token) {
    boolean retVal = false;
    token.ttype = OrderToken.TT_NUMBER;
    
    // anzahl feststellen?
    int minAmount = 0;
    try {
      minAmount = Integer.parseInt(token.getText());
    } catch (NumberFormatException e){
      // not parsable Number !?
    }
    
    OrderToken t = tokens.next();

    if(isString(t.getText()) == true) {
      retVal = readFinalString(t);
    } else {
      unexpected(t);
    }

    if(completer!=null && !t.followedBySpace()){
        completer.cmpltBenutze(minAmount); 
    }
    return retVal;
  }
  
  
	//************* BESCHREIBE
	private boolean readBeschreibe(OrderToken token) {
		boolean retVal = false;
		token.ttype = OrderToken.TT_KEYWORD;

		OrderToken t = tokens.next();

		if(t.equalsToken(Resources.getOrderTranslation(EresseaConstants.O_CASTLE))) {
			retVal = readBenenneBeschreibeTarget(t);
		} else if(t.equalsToken(Resources.getOrderTranslation(EresseaConstants.O_UNIT))) {
			retVal = readBenenneBeschreibeTarget(t);
		} else if(t.equalsToken(Resources.getOrderTranslation(EresseaConstants.O_PRIVATE))) {
			retVal = readBenenneBeschreibeTarget(t);
		} else if(t.equalsToken(Resources.getOrderTranslation(EresseaConstants.O_REGION))) {
			retVal = readBenenneBeschreibeTarget(t);
		} else if(t.equalsToken(Resources.getOrderTranslation(EresseaConstants.O_SHIP))) {
			retVal = readBenenneBeschreibeTarget(t);
		} else {
			unexpected(t);
		}

		if(completer!=null && !t.followedBySpace()){
				completer.cmpltBeschreibe(); 
		}
		return retVal;
	}

	//************* BETRETE
	protected boolean readBetrete(OrderToken token) {
		boolean retVal = false;
		token.ttype = OrderToken.TT_KEYWORD;

		OrderToken t = tokens.next();

		if(t.equalsToken(Resources.getOrderTranslation(EresseaConstants.O_CASTLE))) {
			retVal = readBetreteBurg(t);
		} else if(t.equalsToken(Resources.getOrderTranslation(EresseaConstants.O_SHIP))) {
			retVal = readBetreteSchiff(t);
		} else {
			unexpected(t);
		}

		if(completer!=null && !t.followedBySpace()){
				completer.cmpltBetrete(); 
		}
		return retVal;
	}

	protected boolean readBetreteBurg(OrderToken token) {
		boolean retVal = false;
		token.ttype = OrderToken.TT_KEYWORD;

		OrderToken t = tokens.next();

		if(isID(t.getText()) == true) {
			retVal = readBetreteBurgBID(t);
		} else {
			unexpected(t);
		}

		if(completer!=null && !t.followedBySpace()){
				completer.cmpltBetreteBurg(); 
		}
		return retVal;
	}

	private boolean readBetreteBurgBID(OrderToken token) {
		token.ttype = OrderToken.TT_ID;

		return checkNextFinal();
	}

	protected boolean readBetreteSchiff(OrderToken token) {
		boolean retVal = false;
		token.ttype = OrderToken.TT_KEYWORD;

		OrderToken t = tokens.next();

		if(isID(t.getText()) == true) {
			retVal = readBetreteSchiffSID(t);
		} else {
			unexpected(t);
		}

		if(completer!=null && !t.followedBySpace()){
				completer.cmpltBetreteSchiff(); 
		}
		return retVal;
	}

	private boolean readBetreteSchiffSID(OrderToken token) {
		token.ttype = OrderToken.TT_ID;

		return checkNextFinal();
	}

	//************* BEWACHE
	private boolean readBewache(OrderToken token) {
		boolean retVal = false;
		token.ttype = OrderToken.TT_KEYWORD;

		OrderToken t = tokens.next();

		if(t.equalsToken(Resources.getOrderTranslation(EresseaConstants.O_NOT))) {
			retVal = readBewacheNicht(t);
		} else {
			retVal = checkFinal(t);
		}

		return retVal;
	}

	private boolean readBewacheNicht(OrderToken token) {
		token.ttype = OrderToken.TT_KEYWORD;

		return checkNextFinal();
	}

	//************* BOTSCHAFT
	private boolean readBotschaft(OrderToken token) {
		boolean retVal = false;
		token.ttype = OrderToken.TT_KEYWORD;

		OrderToken t = tokens.next();

		// FIX
		if(t.equalsToken("AN")) {
			retVal = readBotschaft(t);
		} else if(t.equalsToken(Resources.getOrderTranslation(EresseaConstants.O_UNIT))) {
			retVal = readBotschaftEinheit(t);
		} else if(t.equalsToken(Resources.getOrderTranslation(EresseaConstants.O_FACTION))) {
			retVal = readBotschaftPartei(t);
		} else if(t.equalsToken(Resources.getOrderTranslation(EresseaConstants.O_REGION))) {
			retVal = readBotschaftRegion(t);
		} else if(t.equalsToken(Resources.getOrderTranslation(EresseaConstants.O_CASTLE))) {
			retVal = readBotschaftGebaeude(t);
		} else if(t.equalsToken(Resources.getOrderTranslation(EresseaConstants.O_SHIP))) {
			retVal = readBotschaftSchiff(t);
		} else {
			unexpected(t);
		}

		if(completer!=null && !t.followedBySpace()){
				completer.cmpltBotschaft(); 
		}
		return retVal;
	}

	private boolean readBotschaftEinheit(OrderToken token) {
		boolean retVal = false;
		token.ttype = OrderToken.TT_KEYWORD;

		OrderToken t = tokens.next();

		if(isID(t.getText()) == true) {
			retVal = readBotschaftEinheitUID(t);
		} else {
			unexpected(t);
		}

		if(completer!=null && !t.followedBySpace()){
				completer.cmpltBotschaftEinheit(); 
		}
		return retVal;
	}

	private boolean readBotschaftEinheitUID(OrderToken token) {
		boolean retVal = false;
		token.ttype = OrderToken.TT_ID;

		OrderToken t = tokens.next();

		if(isQuoted(t.getText()) == true) {
			retVal = readFinalString(t);
		} else {
			unexpected(t);
		}

		return retVal;
	}

	private boolean readBotschaftPartei(OrderToken token) {
		boolean retVal = false;
		token.ttype = OrderToken.TT_KEYWORD;

		OrderToken t = tokens.next();

		if(isID(t.getText()) == true) {
			retVal = readBotschaftParteiFID(t);
		} else {
			unexpected(t);
		}

		if(completer!=null && !t.followedBySpace()){
				completer.cmpltBotschaftPartei(); 
		}
		return retVal;
	}

	private boolean readBotschaftParteiFID(OrderToken token) {
		boolean retVal = false;
		token.ttype = OrderToken.TT_ID;

		OrderToken t = tokens.next();

		if(isQuoted(t.getText()) == true) {
			retVal = readFinalString(t);
		} else {
			unexpected(t);
		}

		return retVal;
	}

	private boolean readBotschaftRegion(OrderToken token) {
		boolean retVal = false;
		token.ttype = OrderToken.TT_KEYWORD;

		OrderToken t = tokens.next();

		if(isString(t.getText()) == true) {
			retVal = readFinalString(t);
		} else {
			unexpected(t);
		}

		return retVal;
	}

	private boolean readBotschaftGebaeude(OrderToken token) {
		boolean retVal = false;
		token.ttype = OrderToken.TT_KEYWORD;

		OrderToken t = tokens.next();

		if(isID(t.getText()) == true) {
			retVal = readBotschaftGebaeudeID(t);
		} else {
			unexpected(t);
		}

		if(completer!=null && !t.followedBySpace()){
				completer.cmpltBotschaftGebaeude(); 
		}
		return retVal;
	}

	private boolean readBotschaftGebaeudeID(OrderToken token) {
		boolean retVal = false;
		token.ttype = OrderToken.TT_ID;

		OrderToken t = tokens.next();

		if(isQuoted(t.getText()) == true) {
			retVal = readFinalString(t);
		} else {
			unexpected(t);
		}

		if(completer!=null && !t.followedBySpace()){
				completer.cmpltBotschaftGebaeudeID(); 
		}
		return retVal;
	}

	private boolean readBotschaftSchiff(OrderToken token) {
		boolean retVal = false;
		token.ttype = OrderToken.TT_KEYWORD;

		OrderToken t = tokens.next();

		if(isID(t.getText()) == true) {
			retVal = readBotschaftSchiffID(t);
		} else {
			unexpected(t);
		}

		if(completer!=null && !t.followedBySpace()){
				completer.cmpltBotschaftSchiff(); 
		}
		return retVal;
	}

	private boolean readBotschaftSchiffID(OrderToken token) {
		boolean retVal = false;
		token.ttype = OrderToken.TT_ID;

		OrderToken t = tokens.next();

		if(isQuoted(t.getText()) == true) {
			retVal = readFinalString(t);
		} else {
			unexpected(t);
		}

		if(completer!=null && !t.followedBySpace()){
				completer.cmpltBotschaftSchiffID(); 
		}
		return retVal;
	}

  private static enum Type { EMPTY, OPENING, CLOSING };

	//************* DEFAULT
	private boolean readDefault(OrderToken token) {
		token.ttype = OrderToken.TT_KEYWORD;

		OrderToken t = tokens.next();
		
		// the following can be string literal, a string literal within quotes or with an opening quote
		// find out which type we have
		Type tokenType=Type.EMPTY;
		String innerText="";

		char quote = 0;
		
    if(t.ttype == OrderToken.TT_EOC || t.getText().length()<1) {
      // empty
      innerText = "";
      tokenType = Type.EMPTY;
    } else {
      if (t.getText().charAt(0)=='\''){
        // opening single quote
        quote='\'';
      } else if (t.getText().charAt(0)=='"'){
        // opening double quote
        quote='"';
      } else {
        // no quote
        innerText = t.getText();
        tokenType = Type.EMPTY;
      }
		} 
    if (quote!=0){
      // text starts with quote
      if (t.getText().length()>=2 && t.getText().charAt(t.getText().length() - 1)==quote)  {
        innerText = t.getText().substring(1, t.getText().length()-1);
        tokenType = Type.CLOSING;
      }else{
        innerText = t.getText().substring(1, t.getText().length());
        tokenType = Type.OPENING;
      }
		}

    // parse the string inside the quote(s)
		boolean retVal = read(new StringReader(innerText)) && innerText.length()!=0;
		
		
		if (tokenType==Type.CLOSING){
		  // return true iff the innerText is an nonempty order
		  if (completer!=null) {
        completer.clear();
      }
		  return retVal && innerText.length()!=0;
		}
		
		if (completer!=null){
		  if (tokenType==Type.EMPTY){
		    // nothing
        completer.cmplOpeningQuote(null, quote==0?'\'':quote);
		  } else if (tokenType==Type.OPENING){
		    // quote with following text:
		    OrderTokenizer tokenizer = new OrderTokenizer(new StringReader(innerText));
        OrderToken firstToken =tokenizer.getNextToken(), lastToken=firstToken;
        int tokenCount = 0;
        for (OrderToken currentToken = firstToken; currentToken.ttype!=OrderToken.TT_EOC;tokenCount++){
          lastToken=currentToken;
          currentToken=tokenizer.getNextToken();
        }
        // add opening and closing quotes to value as fit, but not to name
        // this way, the completion list is filtered correctly, later
        if (retVal) {
          completer.cmplFinalQuote(lastToken,quote);
        }
        if (tokenCount==1 && ! lastToken.followedBySpace()){
          completer.cmplOpeningQuote(null, quote);
        }

		  }
		}
		return false;
	}

	//************* EMAIL
	private boolean readEmail(OrderToken token) {
		boolean retVal = false;
		token.ttype = OrderToken.TT_KEYWORD;

		OrderToken t = tokens.next();

		if(isEmailAddress(t.getText()) == true) {
			retVal = readEmailAddress(t);
		} else {
			unexpected(t);
		}

		return retVal;
	}

	private boolean readEmailAddress(OrderToken token) {
		token.ttype = OrderToken.TT_STRING;

		return checkNextFinal();
	}

	//************* ENDE
	private boolean readEnde(OrderToken token) {
		token.ttype = OrderToken.TT_KEYWORD;

		return checkNextFinal();
	}

	//************* FAHRE
	private boolean readFahre(OrderToken token) {
		boolean retVal = false;
		token.ttype = OrderToken.TT_KEYWORD;

		OrderToken t = tokens.next();

		if(isID(t.getText()) == true) {
			retVal = readFahreUID(t);
		} else {
			unexpected(t);
		}

		if(completer!=null && !t.followedBySpace()){
				completer.cmpltFahre(); 
		}
		return retVal;
	}

	private boolean readFahreUID(OrderToken token) {
		token.ttype = OrderToken.TT_ID;

		return checkNextFinal();
	}

	//************* FOLGE
	private boolean readFolge(OrderToken token) {
		boolean retVal = false;
		token.ttype = OrderToken.TT_KEYWORD;

		OrderToken t = tokens.next();

		if(t.equalsToken(Resources.getOrderTranslation(EresseaConstants.O_UNIT)) == true) {
			retVal = readFolgeEinheit(t);
		} else if(t.equalsToken(Resources.getOrderTranslation(EresseaConstants.O_SHIP)) == true) {
			retVal = readFolgeSchiff(t);
		} else {
			unexpected(t);
		}

		if(completer!=null && !t.followedBySpace()){
				completer.cmpltFolge(); 
		}
		return retVal;
	}

	private boolean readFolgeEinheit(OrderToken token) {
		boolean retVal = false;
		token.ttype = OrderToken.TT_KEYWORD;

		OrderToken t = tokens.next();

		if(isID(t.getText()) == true) {
			retVal = readFinalID(t);
		} else {
			unexpected(t);
		}

		if(completer!=null && !t.followedBySpace()){
				completer.cmpltFolgeEinheit(); 
		}
		return retVal;
	}

	private boolean readFolgeSchiff(OrderToken token) {
		boolean retVal = false;
		token.ttype = OrderToken.TT_KEYWORD;

		OrderToken t = tokens.next();

		if(isID(t.getText()) == true) {
			retVal = readFinalID(t);
		} else {
			unexpected(t);
		}

		if(completer!=null && !t.followedBySpace()){
				completer.cmpltFolgeSchiff(); 
		}
		return retVal;
	}

	//************* BEANSPRUCHE (Fiete)
	private boolean readBeanspruche(OrderToken token){
		boolean retVal = false;
		token.ttype = OrderToken.TT_KEYWORD;

		OrderToken t = tokens.next();

		if (isNumeric(t.getText())){
			retVal = readBeansprucheAmount(t);
		} else if (isString(t.getText())) {
			retVal = readFinalString(t);
		} else {
			unexpected(t);
		}

		if(completer!=null && !t.followedBySpace()){
				completer.cmpltBeanspruche(); 
		}
		return retVal;
	}
	
	private boolean readBeansprucheAmount(OrderToken token){
		boolean retVal = false;
		token.ttype = OrderToken.TT_NUMBER;

		OrderToken t = tokens.next();

		if (isString(t.getText())){
			retVal = readFinalString(t);
		} else {
			unexpected(t);
		}

		if(completer!=null && !t.followedBySpace()){
				completer.cmpltBeanspruche(); 
		}
		return retVal;
	}
	
	
	//************* FORSCHE
	private boolean readForsche(OrderToken token) {
		boolean retVal = false;
		token.ttype = OrderToken.TT_KEYWORD;

		OrderToken t = tokens.next();

		if(t.equalsToken(Resources.getOrderTranslation(EresseaConstants.O_HERBS))) {
			retVal = readFinalKeyword(t);
		} else {
			unexpected(t);
		}

		if(completer!=null && !t.followedBySpace()){
				completer.cmpltForsche(); 
		}
		return retVal;
	}

	//************* GIB
	private boolean readGib(OrderToken token) {
		boolean retVal = false;
		token.ttype = OrderToken.TT_KEYWORD;

		OrderToken t = tokens.next();

		if(isID(t.getText()) == true) {
			retVal = readGibUID(t);
		} else {
			unexpected(t);
		}

    if(completer != null && !t.followedBySpace()) {
      completer.cmpltGib();
    }

    return retVal;
	}

	private boolean readGibUID(OrderToken token) {
		boolean retVal = false;
		token.ttype = OrderToken.TT_ID;

		UnitID uid = UnitID.createUnitID(token.getText(),data.base);
		OrderToken t = tokens.next();

		if(isNumeric(t.getText()) == true) {
			retVal = readGibUIDAmount(t, uid, Integer.parseInt(t.getText()), true);
		} else if (t.equalsToken(Resources.getOrderTranslation(EresseaConstants.O_EACH))){
			retVal = readGibJe(t, uid);
		}else if(t.equalsToken(Resources.getOrderTranslation(EresseaConstants.O_ALL))) {
			retVal = readGibUIDAlles(t);
		} else if(t.equalsToken(Resources.getOrderTranslation(EresseaConstants.O_UNIT)) ||
					  t.equalsToken(Resources.getOrderTranslation(EresseaConstants.O_CONTROL)) ||
					  t.equalsToken(Resources.getOrderTranslation(EresseaConstants.O_HERBS))) {
			retVal = readFinalKeyword(t);
//		} else if(isString(t.getText()) == true) {
// this is not allowed
//			retVal = readFinalString(t);
		} else {
			unexpected(t);
		}

		if(completer!=null && !t.followedBySpace()){
				completer.cmpltGibUID(); 
		}
		return retVal;
	}

	private boolean readGibJe(OrderToken token, UnitID uid) {
		boolean retVal = false;
		token.ttype = OrderToken.TT_KEYWORD;

		OrderToken t = tokens.next();

		if (isNumeric(t.getText()) == true) {
			retVal = readGibUIDAmount(t, uid, Integer.parseInt(t.getText()), false); // GIB JE PERSONS is illegal
		} else
		// // GIVE bla JE ALL ... does not make sense
		// if(t.equalsToken(Resources.getOrderTranslation(EresseaConstants.O_ALL))) {
		// retVal = readGibUIDAlles(t);
		// } else
		if (isString(t.getText()) == true) {
			retVal = readFinalString(t);
		} else {
			unexpected(t);
		}

		if(completer!=null && !t.followedBySpace()){
				completer.cmpltGibJe(); 
		}
		return retVal;
	}
	
	/**
	 * For multiple-line-completion like the creation of give-orders for the resources of an item
	 * in OrderCompleter.cmpltGibUIDAmount it is necessary to save the unit's id and the amount to
	 * be given. This is done by this method.
	 *
	 * 
	 * @param uid the unit's id
	 * @param i the amount
	 *
	 * 
	 */
	private boolean readGibUIDAmount(OrderToken token, UnitID uid, int i, boolean persons) {
		boolean retVal = false;
		token.ttype = OrderToken.TT_NUMBER;

		OrderToken t = tokens.next();

		if(isString(t.getText()) == true) {
			retVal = readFinalString(t);
		} else {
			unexpected(t);
		}

		if(completer!=null && !t.followedBySpace()){
				completer.cmpltGibUIDAmount(uid, i, persons); 
		}
		return retVal;
	}

	private boolean readGibUIDAlles(OrderToken token) {
		boolean retVal = false;
		token.ttype = OrderToken.TT_KEYWORD;

		OrderToken t = tokens.next();

		if(isString(t.getText()) == true) {
			retVal = readFinalString(t);
		} else {
			retVal = checkFinal(t);
		}

		if(completer!=null && !t.followedBySpace()){
				completer.cmpltGibUIDAlles(); 
		}
		return retVal;
	}

	//************* GRUPPE
	private boolean readGruppe(OrderToken token) {
		boolean retVal = false;
		token.ttype = OrderToken.TT_KEYWORD;

		OrderToken t = tokens.next();

		if(isString(t.getText()) == true) {
			retVal = readFinalString(t);
		} else {
			if(completer != null) {
				completer.cmpltGruppe();
			}

			// just "GRUPPE" without explicit group is valid 
			retVal=checkFinal(t);
//			unexpected(t);
		}

		return retVal;
	}

	//************* HELFE
	private boolean readHelfe(OrderToken token) {
		boolean retVal = false;
		token.ttype = OrderToken.TT_KEYWORD;

		OrderToken t = tokens.next();

		if(isID(t.getText()) == true) {
			retVal = readHelfeFID(t);
		} else {
			unexpected(t);
		}

		if(completer!=null && !t.followedBySpace()){
				completer.cmpltHelfe(); 
		}
		return retVal;
	}

	private boolean readHelfeFID(OrderToken token) {
		boolean retVal = false;
		token.ttype = OrderToken.TT_ID;

		OrderToken t = tokens.next();

		if(t.equalsToken(Resources.getOrderTranslation(EresseaConstants.O_ALL)) ||
			   t.equalsToken(Resources.getOrderTranslation(EresseaConstants.O_HELP_GUARD)) ||
			   t.equalsToken(Resources.getOrderTranslation(EresseaConstants.O_HELP_GIVE)) ||
			   t.equalsToken(Resources.getOrderTranslation(EresseaConstants.O_HELP_COMBAT)) ||
			   t.equalsToken(Resources.getOrderTranslation(EresseaConstants.O_HELP_SILVER)) ||
			   t.equalsToken(Resources.getOrderTranslation(EresseaConstants.O_HELP_FACTIONSTEALTH))) {
			retVal = readHelfeFIDModifier(t);
		} else {
			unexpected(t);
		}

		if(completer!=null && !t.followedBySpace()){
				completer.cmpltHelfeFID(); 
		}
		return retVal;
	}

	private boolean readHelfeFIDModifier(OrderToken token) {
		boolean retVal = false;
		token.ttype = OrderToken.TT_KEYWORD;

		OrderToken t = tokens.next();

		if(t.equalsToken(Resources.getOrderTranslation(EresseaConstants.O_NOT))) {
			retVal = readHelfeFIDModifierNicht(t, token.getText());
		} else {
			retVal = checkFinal(t);
		}

		if(completer!=null && !t.followedBySpace()){
				completer.cmpltHelfeFIDModifier(); 
		}
		return retVal;
	}

	private boolean readHelfeFIDModifierNicht(OrderToken token, String modifier) {
		token.ttype = OrderToken.TT_KEYWORD;

		return checkNextFinal();
	}

	//************* KAEMPFE
	private boolean readKaempfe(OrderToken token) {
		boolean retVal = false;
		token.ttype = OrderToken.TT_KEYWORD;

		OrderToken t = tokens.next();

		if(t.equalsToken(Resources.getOrderTranslation(EresseaConstants.O_COMBAT_AGGRESSIVE))) {
			retVal = readFinalKeyword(t);
		} else if(t.equalsToken(Resources.getOrderTranslation(EresseaConstants.O_COMBAT_REAR))) {
			retVal = readFinalKeyword(t);
		} else if(t.equalsToken(Resources.getOrderTranslation(EresseaConstants.O_COMBAT_DEFENSIVE))) {
			retVal = readFinalKeyword(t);
		} else if(t.equalsToken(Resources.getOrderTranslation(EresseaConstants.O_COMBAT_NOT))) {
			retVal = readFinalKeyword(t);
		} else if(t.equalsToken(Resources.getOrderTranslation(EresseaConstants.O_COMBAT_FLEE))) {
			retVal = readFinalKeyword(t);
		} else if(t.equalsToken(Resources.getOrderTranslation(EresseaConstants.O_COMBAT_HELP))) {
			retVal = readKaempfeHelfe(t);
		} else {
			retVal = checkFinal(t);
		}

		if(completer!=null && !t.followedBySpace()){
				completer.cmpltKaempfe(); 
		}
		return retVal;
	}

	private boolean readKaempfeHelfe(OrderToken token) {
		boolean retVal = false;
		token.ttype = OrderToken.TT_KEYWORD;

		OrderToken t = tokens.next();

		if(t.equalsToken(Resources.getOrderTranslation(EresseaConstants.O_NOT))) {
			retVal = readFinalKeyword(t);
		} else {
			retVal = checkFinal(t);
		}

		if(completer!=null && !t.followedBySpace()){
				completer.cmpltKaempfeHelfe(); 
		}
		return retVal;
	}

	//************* KAMPFZAUBER
	private boolean readKampfzauber(OrderToken token) {
		boolean retVal = false;
		token.ttype = OrderToken.TT_KEYWORD;

		OrderToken t = tokens.next();

		if(t.equalsToken(Resources.getOrderTranslation(EresseaConstants.O_LEVEL))) {
			retVal = readKampfzauberStufe(t);
		} else if(t.equalsToken(Resources.getOrderTranslation(EresseaConstants.O_NOT))) {
			retVal = readFinalKeyword(t);
		} else if(isString(t.getText())) {
			retVal = readKampfzauberSpell(t);
		} else {
			unexpected(t);
		}

		if(completer!=null && !t.followedBySpace()){
				completer.cmpltKampfzauber(); 
		}
		return retVal;
	}

	private boolean readKampfzauberStufe(OrderToken token) {
		boolean retVal = false;
		token.ttype = OrderToken.TT_KEYWORD;

		OrderToken t = tokens.next();

		if(isNumeric(t.getText())) {
			t.ttype = OrderToken.TT_NUMBER;
			t = tokens.next();

			if(isString(t.getText())) {
				retVal = readFinalString(t);
			} else {
				unexpected(t);
			}

		if(completer!=null && !t.followedBySpace()){
					completer.cmpltKampfzauberStufe(); 
		}
		}

		return retVal;
	}

	private boolean readKampfzauberSpell(OrderToken token) {
		boolean retVal = false;
		token.ttype = OrderToken.TT_STRING;

		OrderToken t = tokens.next();

		if(t.equalsToken(Resources.getOrderTranslation(EresseaConstants.O_NOT))) {
			retVal = readFinalKeyword(t);
		} else {
			retVal = checkFinal(t);
		}

		if(completer!=null && !t.followedBySpace()){
				completer.cmpltKampfzauberSpell(); 
		}
		return retVal;
	}

	//************* KAUFE
	private boolean readKaufe(OrderToken token) {
		boolean retVal = false;
		token.ttype = OrderToken.TT_KEYWORD;

		OrderToken t = tokens.next();

		if(isNumeric(t.getText()) == true) {
			retVal = readKaufeAmount(t);
		} else {
			unexpected(t);
		}

		if(completer!=null && !t.followedBySpace()){
				completer.cmpltKaufe(); 
		}
		return retVal;
	}

	private boolean readKaufeAmount(OrderToken token) {
		boolean retVal = false;
		ItemType type = null;
		ItemCategory luxuryCategory = (data != null)
									  ? data.rules.getItemCategory(EresseaConstants.C_LUXURIES) : null;
		token.ttype = OrderToken.TT_NUMBER;

		OrderToken t = tokens.next();

		// 
		if((data.rules != null) && ((type = data.rules.getItemType(t.getText())) != null) &&
			   (luxuryCategory != null) && luxuryCategory.equals(type.getCategory())) {
			retVal = readFinalString(t);
		} else {
			unexpected(t);
		}

		if(completer!=null && !t.followedBySpace()){
				completer.cmpltKaufeAmount(); 
		}
		return retVal;
	}

	//************* KONTAKTIERE
	private boolean readKontaktiere(OrderToken token) {
		boolean retVal = false;
		token.ttype = OrderToken.TT_KEYWORD;

		OrderToken t = tokens.next();

		if(isID(t.getText()) == true) {
			retVal = readKontaktiereUID(t);
		} else {
			unexpected(t);
		}

		if(completer!=null && !t.followedBySpace()){
				completer.cmpltKontaktiere(); 
		}
		return retVal;
	}

	private boolean readKontaktiereUID(OrderToken token) {
		token.ttype = OrderToken.TT_ID;

		return checkNextFinal();
	}

	//************* LEHRE
	private boolean readLehre(OrderToken token) {
		boolean retVal = false;
		token.ttype = OrderToken.TT_KEYWORD;

		OrderToken t = tokens.next();

		if(isID(t.getText()) == true) {
			retVal = readLehreUID(t);
		} else {
			unexpected(t);
		}

		if(completer!=null && !t.followedBySpace()){
				completer.cmpltLehre(); 
		}
		return retVal;
	}

	private boolean readLehreUID(OrderToken token) {
		boolean retVal = false;
		token.ttype = OrderToken.TT_ID;

		OrderToken t = tokens.next();

		if(isID(t.getText()) == true) {
			retVal = readLehreUID(t);
		} else {
			retVal = checkFinal(t);
		}

		if(completer!=null && !t.followedBySpace()){
				completer.cmpltLehre(); 
		}
		return retVal;
	}

	//************* LERNE
	private boolean readLerne(OrderToken token) {
		boolean retVal = false;
		token.ttype = OrderToken.TT_KEYWORD;

		OrderToken t = tokens.next();

		// detect quoted strings
		if((data.rules != null) && (data.rules.getSkillType(t.getStrippedText(EresseaOrderParser.QUOTES)) != null)) {
			t.ttype = OrderToken.TT_STRING;
			t = tokens.next();

			if(isNumeric(t.getText()) == true) {
				retVal = readFinalNumber(t);
			} else {
				retVal = checkFinal(t);
			}
		} else {
			unexpected(t);
		}

		if(completer!=null && !t.followedBySpace()){
				completer.cmpltLerne(); 
		}
		return retVal;
	}

	//************* LOCALE
	private boolean readLocale(OrderToken token) {
		boolean retVal = false;
		token.ttype = OrderToken.TT_KEYWORD;

		OrderToken t = tokens.next();

		if(isString(t.getText())) {
			retVal = readFinalString(t);
		} else {
			unexpected(t);
		}

		if(completer!=null && !t.followedBySpace()){
				completer.cmpltLocale(); 
		}
		return retVal;
	}

	//************* MACHE
	private boolean readMache(OrderToken token) {
		boolean retVal = false;
		BuildingType type = null;
		token.ttype = OrderToken.TT_KEYWORD;

		OrderToken t = tokens.next();

		if(isNumeric(t.getText()) == true) {
			retVal = readMacheAmount(t);
		} else if(t.equalsToken(Resources.getOrderTranslation(EresseaConstants.O_TEMP))) {
			retVal = readMacheTemp(t);
		} else if(isTempID(t.getText()) == true) {
			retVal = readMacheTempID(t);
		} else if(t.equalsToken(Resources.getOrderTranslation(EresseaConstants.O_CASTLE))) {
			retVal = readMacheBurg(t);
		} else if((data.rules != null) && ((type = data.rules.getBuildingType(t.getText())) != null) &&
					  (!(type instanceof CastleType) ||
					  t.equalsToken(Resources.getOrderTranslation(EresseaConstants.O_CASTLE)))) {
			retVal = readMacheBuilding(t);
		} else if((data.rules != null) && (data.rules.getShipType(t.getText()) != null)) {
			retVal = readMacheShip(t);
		} else if(t.equalsToken(Resources.getOrderTranslation(EresseaConstants.O_SHIP))) {
			retVal = readMacheSchiff(t);
		} else if(t.equalsToken(Resources.getOrderTranslation(EresseaConstants.O_ROAD))) {
			retVal = readMacheStrasse(t);
		} else if(t.equalsToken(Resources.getOrderTranslation(EresseaConstants.O_SEED))) {
			retVal = readFinalKeyword(t);
		} else if(t.ttype == OrderToken.TT_EOC) {
			retVal = false;
		} else {
      retVal = readMacheAnything(t);
    }

		if(completer!=null && !t.followedBySpace()){
				completer.cmpltMache(); 
		}
 

		return retVal;
	}

	private boolean readMacheAmount(OrderToken token) {
		boolean retVal = false;
		BuildingType type = null;
		token.ttype = OrderToken.TT_NUMBER;

		OrderToken t = tokens.next();

		if(t.equalsToken(Resources.getOrderTranslation(EresseaConstants.O_CASTLE))) {
			retVal = readMacheBurg(t);
		} else if((data.rules != null) && ((type = data.rules.getBuildingType(t.getText())) != null) &&
					  !(type instanceof CastleType)) {
			retVal = readMacheBuilding(t);
		} else if((data.rules != null) && (data.rules.getShipType(t.getText()) != null)) {
			retVal = readMacheShip(t);
		} else if(t.equalsToken(Resources.getOrderTranslation(EresseaConstants.O_SHIP))) {
			retVal = readMacheSchiff(t);
		} else {
			retVal = readMacheAnything(t);
		}

		if(completer!=null && !t.followedBySpace()){
				completer.cmpltMacheAmount(); 
		}
		return retVal;
	}

	private boolean readMacheTemp(OrderToken token) {
		token.ttype = OrderToken.TT_ID;

		OrderToken t = tokens.next();

		if(completer != null) {
			completer.cmpltMacheTemp();
		}

		unexpected(t);

		return false; // there can't follow an id, else it would have been merged with TEMP
	}

	private boolean readMacheTempID(OrderToken token) {
		boolean retVal = false;
		token.ttype = OrderToken.TT_ID;

		OrderToken t = tokens.next();

		if(isString(t.getText())) {
			retVal = readFinalString(t);
		} else {
			retVal = checkFinal(t);
		}

		if(completer!=null && !t.followedBySpace()){
				completer.cmpltMacheTempID(); 
		}
		return retVal;
	}

	private boolean readMacheBurg(OrderToken token) {
		boolean retVal = false;
		token.ttype = OrderToken.TT_KEYWORD;

		OrderToken t = tokens.next();

		if(isID(t.getText())) {
			retVal = readFinalID(t);
		} else {
			retVal = checkFinal(t);
		}

		if(completer!=null && !t.followedBySpace()){
				completer.cmpltMacheBurg(); 
		}
		return retVal;
	}

	private boolean readMacheBuilding(OrderToken token) {
		boolean retVal = false;
		token.ttype = OrderToken.TT_STRING;

		OrderToken t = tokens.next();

		if(isID(t.getText()) == true) {
			retVal = readFinalID(t);
		} else {
			retVal = checkFinal(t);
		}

		if(completer!=null && !t.followedBySpace()){
				completer.cmpltMacheBuilding(token.getText()); 
		}
		return retVal;
	}

	private boolean readMacheShip(OrderToken token) {
		token.ttype = OrderToken.TT_STRING;

		return checkNextFinal();
	}

	private boolean readMacheSchiff(OrderToken token) {
		boolean retVal = false;
		token.ttype = OrderToken.TT_STRING;

		OrderToken t = tokens.next();

		if(isID(t.getText()) == true) {
			retVal = readFinalID(t);
		} else {
			retVal = checkFinal(t);
		}

		if(completer!=null && !t.followedBySpace()){
				completer.cmpltMacheSchiff(); 
		}
		return retVal;
	}

	private boolean readMacheStrasse(OrderToken token) {
		boolean retVal = false;
		token.ttype = OrderToken.TT_STRING;

		OrderToken t = tokens.next();

		if(Direction.toInt(t.getText()) != Direction.DIR_INVALID) {
			retVal = readFinalString(t);
		} else {
			unexpected(t);
		}

		if(completer!=null && !t.followedBySpace()){
				completer.cmpltMacheStrasse(); 
		}
		return retVal;
	}

	private boolean readMacheAnything(OrderToken token) {
		boolean retVal = true;

		if((token.ttype != OrderToken.TT_EOC) && (token.ttype != OrderToken.TT_COMMENT)) {
			token.ttype = OrderToken.TT_STRING;
			retVal = checkNextFinal();
		}

		return retVal;
	}

	//************* NACH
	private boolean readNach(OrderToken token) {
		boolean retVal = false;
		token.ttype = OrderToken.TT_KEYWORD;

		OrderToken t = tokens.next();

		if(Direction.toInt(t.getText()) != Direction.DIR_INVALID) {
			retVal = readNachDirection(t);
		} else {
			unexpected(t);
		}

		if(completer!=null && !t.followedBySpace()){
				completer.cmpltNach(); 
		}
		return retVal;
	}

	private boolean readNachDirection(OrderToken token) {
		boolean retVal = false;
		token.ttype = OrderToken.TT_KEYWORD;

		OrderToken t = tokens.next();

		if(Direction.toInt(t.getText()) != Direction.DIR_INVALID) {
			retVal = readNachDirection(t);
		} else {
			retVal = checkFinal(t);
		}

		if(completer!=null && !t.followedBySpace()){
				completer.cmpltNach(); 
		}
		return retVal;
	}

	//************* NUMMER
	private boolean readNummer(OrderToken token) {
		boolean retVal = false;
		token.ttype = OrderToken.TT_KEYWORD;

		OrderToken t = tokens.next();

		if(t.equalsToken(Resources.getOrderTranslation(EresseaConstants.O_UNIT)) == true) {
			retVal = readNummerEinheit(t);
		} else if(t.equalsToken(Resources.getOrderTranslation(EresseaConstants.O_SHIP)) == true) {
			retVal = readNummerSchiff(t);
		} else if(t.equalsToken(Resources.getOrderTranslation(EresseaConstants.O_FACTION)) == true) {
			retVal = readNummerPartei(t);
		} else if(t.equalsToken(Resources.getOrderTranslation(EresseaConstants.O_CASTLE)) == true) {
			retVal = readNummerBurg(t);
		} else {
			unexpected(t);
		}

		if(completer!=null && !t.followedBySpace()){
				completer.cmpltNummer(); 
		}
		return retVal;
	}

	private boolean readNummerEinheit(OrderToken token) {
		boolean retVal = false;
		token.ttype = OrderToken.TT_KEYWORD;

		OrderToken t = tokens.next();

		if(isID(t.getText()) == true) {
			retVal = readFinalID(t);
		} else if(t.ttype == OrderToken.TT_EOC) {
			retVal = true;
		} else {
			unexpected(t);
		}

		return retVal;
	}

	private boolean readNummerPartei(OrderToken token) {
		boolean retVal = false;
		token.ttype = OrderToken.TT_KEYWORD;

		OrderToken t = tokens.next();

		if(isID(t.getText()) == true) {
			retVal = readFinalID(t);
		} else if(t.ttype == OrderToken.TT_EOC) {
			retVal = true;
		} else {
			unexpected(t);
		}

		return retVal;
	}

	private boolean readNummerSchiff(OrderToken token) {
		boolean retVal = false;
		token.ttype = OrderToken.TT_KEYWORD;

		OrderToken t = tokens.next();

		if(isID(t.getText()) == true) {
			retVal = readFinalID(t);
		} else if(t.ttype == OrderToken.TT_EOC) {
			retVal = true;
		} else {
			unexpected(t);
		}

		return retVal;
	}

	private boolean readNummerBurg(OrderToken token) {
		boolean retVal = false;
		token.ttype = OrderToken.TT_KEYWORD;

		OrderToken t = tokens.next();

		if(isID(t.getText()) == true) {
			retVal = readFinalID(t);
		} else if(t.ttype == OrderToken.TT_EOC) {
			retVal = true;
		} else {
			unexpected(t);
		}

		return retVal;
	}

	//************* OPTION
	private boolean readOption(OrderToken token) {
		boolean retVal = false;
		token.ttype = OrderToken.TT_KEYWORD;

		OrderToken t = tokens.next();

		if(t.equalsToken(Resources.getOrderTranslation(EresseaConstants.O_ADDRESSES)) ||
			   t.equalsToken(Resources.getOrderTranslation(EresseaConstants.O_REPORT)) ||
			   t.equalsToken(Resources.getOrderTranslation(EresseaConstants.O_BZIP2)) ||
			   t.equalsToken(Resources.getOrderTranslation(EresseaConstants.O_COMPUTER)) ||
			   t.equalsToken(Resources.getOrderTranslation(EresseaConstants.O_ITEMPOOL)) ||
			   t.equalsToken(Resources.getOrderTranslation(EresseaConstants.O_SILVERPOOL)) ||
			   t.equalsToken(Resources.getOrderTranslation(EresseaConstants.O_STATISTICS)) ||
			   t.equalsToken(Resources.getOrderTranslation(EresseaConstants.O_ZIPPED)) ||
         t.equalsToken(Resources.getOrderTranslation(EresseaConstants.O_SCORE)) ||
			   t.equalsToken(Resources.getOrderTranslation(EresseaConstants.O_TEMPLATE))) {
			retVal = readOptionOption(t);
		} else {
			unexpected(t);
		}

		if(completer!=null && !t.followedBySpace()){
				completer.cmpltOption(); 
		}
		return retVal;
	}

	private boolean readOptionOption(OrderToken token) {
		boolean retVal = false;
		token.ttype = OrderToken.TT_KEYWORD;

		OrderToken t = tokens.next();

		if(t.equalsToken(Resources.getOrderTranslation(EresseaConstants.O_NOT))) {
			retVal = readFinalKeyword(t);
		} else {
			retVal = checkFinal(t);
		}

		if(completer!=null && !t.followedBySpace()){
				completer.cmpltOptionOption(); 
		}
		return retVal;
	}

	//************* PARTEI
	private boolean readPartei(OrderToken token) {
		boolean retVal = false;
		token.ttype = OrderToken.TT_KEYWORD;

		OrderToken t = tokens.next();

		if(isID(t.getText()) == true) {
			retVal = readParteiFID(t);
		} else {
			unexpected(t);
		}

		return retVal;
	}

	private boolean readParteiFID(OrderToken token) {
		boolean retVal = false;
		token.ttype = OrderToken.TT_ID;

		OrderToken t = tokens.next();

		if(isQuoted(t.getText()) == true) {
			retVal = readFinalString(t);
		} else {
			unexpected(t);
		}

		return retVal;
	}

	//************* PASSWORT
	private boolean readPasswort(OrderToken token) {
		boolean retVal = false;
		token.ttype = OrderToken.TT_KEYWORD;

		OrderToken t = tokens.next();

		if(t.ttype == OrderToken.TT_EOC) {
			retVal = true;
		} else if(isQuoted(t.getText()) == true) {
			retVal = readFinalString(t);
		} else {
			unexpected(t);
		}

		return retVal;
	}

	//************* PFLANZEN
	private boolean readPflanzen(OrderToken token) {
		token.ttype = OrderToken.TT_KEYWORD;

		return checkFinal(tokens.next());
	}

	//************* PIRATERIE
	private boolean readPiraterie(OrderToken token) {
		boolean retVal = false;
		token.ttype = OrderToken.TT_KEYWORD;

		OrderToken t = tokens.next();

		if(isID(t.getText()) == true) {
			retVal = readPiraterieFID(t);
		} else {
		  retVal = checkFinal(t);
		}

		if(completer!=null && !t.followedBySpace()){
				completer.cmpltPiraterie(); 
		}
		return retVal;
	}

	private boolean readPiraterieFID(OrderToken token) {
		boolean retVal = false;
		token.ttype = OrderToken.TT_ID;

		OrderToken t = tokens.next();

		if(isID(t.getText()) == true) {
			retVal = readPiraterieFID(t);
		} else {
			retVal = checkFinal(t);
		}

		if(completer!=null && !t.followedBySpace()){
				completer.cmpltPiraterieFID(); 
		}
		return retVal;
	}

	//************* PRAEFIX
	private boolean readPraefix(OrderToken token) {
		boolean retVal = false;
		token.ttype = OrderToken.TT_KEYWORD;

		OrderToken t = tokens.next();

		if(isQuoted(t.getText()) == true) {
			retVal = readFinalString(t);
		} else {
			retVal = checkFinal(t);
		}

		if(completer!=null && !t.followedBySpace()){
				completer.cmpltPraefix(); 
		}
		return retVal;
	}

	//************* REGION
	private boolean readRegion(OrderToken token) {
		boolean retVal = false;
		token.ttype = OrderToken.TT_KEYWORD;

		OrderToken t = tokens.next();

		if(isRID(t.getText()) == true) {
			retVal = readFinalID(t);
		} else {
			unexpected(t);
		}

		return retVal;
	}

	//************* REKRUTIERE
	private boolean readRekrutiere(OrderToken token) {
		boolean retVal = false;
		token.ttype = OrderToken.TT_KEYWORD;

		OrderToken t = tokens.next();

		if(isNumeric(t.getText()) == true) {
			retVal = readFinalNumber(t);
		} else {
			unexpected(t);
		}

		return retVal;
	}

	//************* RESERVIERE
	private boolean readReserviere(OrderToken token) {
		boolean retVal = false;
		token.ttype = OrderToken.TT_KEYWORD;

		OrderToken t = tokens.next();
		if(isNumeric(t.getText()) == true) {
			retVal = readReserviereAmount(t);
		} else if (t.equalsToken(Resources.getOrderTranslation(EresseaConstants.O_EACH))){
			retVal = readReserviereJe(t);
		} else {
			unexpected(t);
		}

		if(completer!=null && !t.followedBySpace()){
				completer.cmpltReserviere(); 
		}
		return retVal;
	}
	
	private boolean readReserviereJe(OrderToken token){
		boolean retVal = false;
		token.ttype = OrderToken.TT_KEYWORD;
		
		OrderToken t = tokens.next();
		
		if(isNumeric(t.getText()) == true) {
			retVal = readReserviereAmount(t);
		} else {
			retVal = checkFinal(t);
		}

		if(completer!=null && !t.followedBySpace()){
				completer.cmpltReserviereJe(); 
		}
		return retVal;

	}

	private boolean readReserviereAmount(OrderToken token) {
		boolean retVal = false;
		token.ttype = OrderToken.TT_NUMBER;

		OrderToken t = tokens.next();

		if(isString(t.getText()) == true) {
			retVal = readFinalString(t);
		} else {
			unexpected(t);
		}

		if(completer!=null && !t.followedBySpace()){
				completer.cmpltReserviereAmount(); 
		}
		return retVal;
	}

	//************* ROUTE
	private boolean readRoute(OrderToken token) {
		boolean retVal = false;
		token.ttype = OrderToken.TT_KEYWORD;

		OrderToken t = tokens.next();

		if(Direction.toInt(t.getText()) != Direction.DIR_INVALID) {
			retVal = readRouteDirection(t);
		} else if(t.equalsToken(Resources.getOrderTranslation(EresseaConstants.O_PAUSE))) {
			retVal = readRouteDirection(t);
		} else {
			unexpected(t);
		}

		if(completer!=null && !t.followedBySpace()){
				completer.cmpltRoute(); 
		}
		return retVal;
	}

	private boolean readRouteDirection(OrderToken token) {
		boolean retVal = false;
		token.ttype = OrderToken.TT_KEYWORD;

		OrderToken t = tokens.next();

		if(Direction.toInt(t.getText()) != Direction.DIR_INVALID) {
			retVal = readRouteDirection(t);
		} else if(t.equalsToken(Resources.getOrderTranslation(EresseaConstants.O_PAUSE))) {
			retVal = readRouteDirection(t);
		} else {
			retVal = checkFinal(t);
		}

		if(completer!=null && !t.followedBySpace()){
				completer.cmpltRoute(); 
		}
		return retVal;
	}

	//************* SORTIERE
	private boolean readSortiere(OrderToken token) {
		boolean retVal = false;
		token.ttype = OrderToken.TT_KEYWORD;

		OrderToken t = tokens.next();

		// FIX
		if(t.equalsToken(Resources.getOrderTranslation(EresseaConstants.O_BEFORE))) {
			retVal = readSortiereVor(t);
		} else if(t.equalsToken(Resources.getOrderTranslation(EresseaConstants.O_AFTER))) {
			retVal = readSortiereHinter(t);
		} else {
			unexpected(t);
		}

		if(completer!=null && !t.followedBySpace()){
				completer.cmpltSortiere(); 
		}
		return retVal;
	}

	private boolean readSortiereVor(OrderToken token) {
		boolean retVal = false;
		token.ttype = OrderToken.TT_KEYWORD;

		OrderToken t = tokens.next();

		if(isID(t.getText())) {
			retVal = readFinalID(t);
		} else {
			unexpected(t);
		}

		if(completer!=null && !t.followedBySpace()){
				completer.cmpltSortiereVor(); 
		}
		return retVal;
	}

	private boolean readSortiereHinter(OrderToken token) {
		boolean retVal = false;
		token.ttype = OrderToken.TT_KEYWORD;

		OrderToken t = tokens.next();

		if(isID(t.getText())) {
			retVal = readFinalID(t);
		} else {
			unexpected(t);
		}

		if(completer!=null && !t.followedBySpace()){
				completer.cmpltSortiereHinter(); 
		}
		return retVal;
	}

	//************* SPIONIERE
	private boolean readSpioniere(OrderToken token) {
		boolean retVal = false;
		token.ttype = OrderToken.TT_KEYWORD;

		OrderToken t = tokens.next();

		if(isID(t.getText()) == true) {
			retVal = readFinalID(t);
		} else {
			unexpected(t);
		}

		if(completer!=null && !t.followedBySpace()){
				completer.cmpltSpioniere(); 
		}
		return retVal;
	}

	//************* STIRB
	private boolean readStirb(OrderToken token) {
		boolean retVal = false;
		token.ttype = OrderToken.TT_KEYWORD;

		OrderToken t = tokens.next();

		if(isQuoted(t.getText()) == true) {
			retVal = readFinalString(t);
		} else {
			unexpected(t);
		}

		if(completer!=null && !t.followedBySpace()){
				completer.cmpltStirb(); 
		}
		return retVal;
	}

	//************* TARNE
	private boolean readTarne(OrderToken token) {
		boolean retVal = false;
		token.ttype = OrderToken.TT_KEYWORD;

		OrderToken t = tokens.next();

		if(isNumeric(t.getText()) == true) {
			retVal = readFinalNumber(t);
		} else if(t.equalsToken(Resources.getOrderTranslation(EresseaConstants.O_FACTION))) {
			retVal = readTarnePartei(t);
		} else if((data.rules != null) && (data.rules.getRace(t.getText()) != null)) {
			retVal = readFinalString(t);
		} else {
			retVal = checkFinal(t);
		}

		if(completer!=null && !t.followedBySpace()){
				completer.cmpltTarne(); 
		}
		return retVal;
	}

	private boolean readTarnePartei(OrderToken token) {
		boolean retVal = false;
		token.ttype = OrderToken.TT_KEYWORD;

		OrderToken t = tokens.next();

		if(t.equalsToken(Resources.getOrderTranslation(EresseaConstants.O_NOT))) {
			retVal = readFinalKeyword(t);
		} else if(t.equalsToken(Resources.getOrderTranslation(EresseaConstants.O_NUMBER))) {
			retVal = readTarneParteiNummer(t);
		} else {
			retVal = checkFinal(t);
		}

		if(completer!=null && !t.followedBySpace()){
				completer.cmpltTarnePartei(); 
		}
		return retVal;
	}

	private boolean readTarneParteiNummer(OrderToken token) {
		boolean retVal = false;
		token.ttype = OrderToken.TT_KEYWORD;

		OrderToken t = tokens.next();

		if(isID(t.getText())) {
			retVal = readFinalID(t);
		} else {
			unexpected(t);
		}

		if(completer!=null && !t.followedBySpace()){
				completer.cmpltTarneParteiNummer(); 
		}
		return retVal;
	}

	//************* TRANSPORTIERE
	private boolean readTransportiere(OrderToken token) {
		boolean retVal = false;
		token.ttype = OrderToken.TT_KEYWORD;

		OrderToken t = tokens.next();

		if(isID(t.getText()) == true) {
			retVal = readFinalID(t);
		} else {
			unexpected(t);
		}

		if(completer!=null && !t.followedBySpace()){
				completer.cmpltTransportiere(); 
		}
		return retVal;
	}

	//************* TREIBE
	private boolean readTreibe(OrderToken token) {
		boolean retVal = false;
		token.ttype = OrderToken.TT_KEYWORD;

		OrderToken t = tokens.next();

		if(isNumeric(t.getText()) == true) {
			retVal = readFinalNumber(t);
		} else {
			retVal = checkFinal(t);
		}

		return retVal;
	}

	//************* UNTERHALTE
	private boolean readUnterhalte(OrderToken token) {
		boolean retVal = false;
		token.ttype = OrderToken.TT_KEYWORD;

		OrderToken t = tokens.next();

		if(isNumeric(t.getText()) == true) {
			retVal = readFinalNumber(t);
		} else {
			retVal = checkFinal(t);
		}

		return retVal;
	}

	//************* URSPRUNG
	private boolean readUrsprung(OrderToken token) {
		boolean retVal = false;
		token.ttype = OrderToken.TT_KEYWORD;

		OrderToken t = tokens.next();

		if(isNumeric(t.getText(), 10, Integer.MIN_VALUE, Integer.MAX_VALUE) == true) {
			retVal = readUrsprungX(t);
		} else {
			unexpected(t);
		}

		return retVal;
	}

	private boolean readUrsprungX(OrderToken token) {
		boolean retVal = false;
		token.ttype = OrderToken.TT_ID;

		OrderToken t = tokens.next();

		if(isNumeric(t.getText(), 10, Integer.MIN_VALUE, Integer.MAX_VALUE) == true) {
			retVal = readFinalID(t);
		} else {
			unexpected(t);
		}

		return retVal;
	}

	//************* VERGESSE
	private boolean readVergesse(OrderToken token) {
		boolean retVal = false;
		token.ttype = OrderToken.TT_KEYWORD;

		OrderToken t = tokens.next();

		if((data.rules != null) && (data.rules.getSkillType(t.getText()) != null)) {
			retVal = readFinalString(t);
		} else {
			unexpected(t);
		}

		if(completer!=null && !t.followedBySpace()){
				completer.cmpltVergesse(); 
		}
		return retVal;
	}

	//************* VERKAUFE
	private boolean readVerkaufe(OrderToken token) {
		boolean retVal = false;
		token.ttype = OrderToken.TT_KEYWORD;

		OrderToken t = tokens.next();

		if(isNumeric(t.getText()) == true) {
			retVal = readVerkaufeAmount(t);
		} else if(t.equalsToken(Resources.getOrderTranslation(EresseaConstants.O_ALL))) {
			retVal = readVerkaufeAlles(t);
		} else {
			unexpected(t);
		}

		if(completer!=null && !t.followedBySpace()){
				completer.cmpltVerkaufe(); 
		}
		return retVal;
	}

	private boolean readVerkaufeAmount(OrderToken token) {
		boolean retVal = false;
		ItemType type = null;
		ItemCategory luxuryCategory = (data.rules != null)
									  ? data.rules.getItemCategory(EresseaConstants.C_LUXURIES) : null;
		token.ttype = OrderToken.TT_NUMBER;

		OrderToken t = tokens.next();

		if((data.rules != null) && ((type = data.rules.getItemType(t.getText())) != null) &&
			   (luxuryCategory != null) && type.getCategory().equals(luxuryCategory)) {
			retVal = readFinalString(t);
		} else {
			unexpected(t);
		}

		if(completer!=null && !t.followedBySpace()){
				completer.cmpltVerkaufeAmount(); 
		}
		return retVal;
	}

	private boolean readVerkaufeAlles(OrderToken token) {
		boolean retVal = false;
		ItemType type = null;
		ItemCategory luxuryCategory = (data.rules != null)
									  ? data.rules.getItemCategory(EresseaConstants.C_LUXURIES) : null;
		token.ttype = OrderToken.TT_KEYWORD;

		OrderToken t = tokens.next();

		if((data.rules != null) && ((type = data.rules.getItemType(t.getText())) != null) && (type != null) &&
			   (luxuryCategory != null) && luxuryCategory.equals(type.getCategory())) {
			retVal = readFinalString(t);
		} else {
			unexpected(t);
		}

		if(completer!=null && !t.followedBySpace()){
				completer.cmpltVerkaufeAlles(); 
		}
		return retVal;
	}

	//************* ZAUBERE
	private boolean readZaubere(OrderToken token) {
		boolean retVal = false;
		token.ttype = OrderToken.TT_KEYWORD;

		OrderToken t = tokens.next();

		if(t.equalsToken(Resources.getOrderTranslation(EresseaConstants.O_REGION))) {
			retVal = readZaubereRegion(t);
		} else if(t.equalsToken(Resources.getOrderTranslation(EresseaConstants.O_LEVEL))) {
			retVal = readZaubereStufe(t);
		} else if(isString(t.getText())) {
//      Spell s = data.getSpell(t.getText());
//      if(s != null) {
//        retVal = readZaubereSpruch(t, s);
//      } else {
        retVal = readZaubereSpruch(t);
//      }
		} else {
			unexpected(t);
		}

		if(completer!=null && !t.followedBySpace()){
				completer.cmpltZaubere(); 
		}
		return retVal;
	}

	private boolean readZaubereRegion(OrderToken token) {
		boolean retVal = false;
		token.ttype = OrderToken.TT_KEYWORD;

		OrderToken t = tokens.next();

		if(isNumeric(t.getText(), 10, Integer.MIN_VALUE, Integer.MAX_VALUE)) {
			retVal = readZaubereRegionCoor(t);
		} else {
			unexpected(t);
		}

		if(completer!=null && !t.followedBySpace()){
				completer.cmpltZaubereRegion(); 
		}
		return retVal;
	}

	private boolean readZaubereRegionCoor(OrderToken token) {
		boolean retVal = false;

		// x-coordinate
		token.ttype = OrderToken.TT_NUMBER;

		OrderToken t = tokens.next();

		if(isNumeric(t.getText(), 10, Integer.MIN_VALUE, Integer.MAX_VALUE)) {
			// y-coordinate
			t.ttype = OrderToken.TT_NUMBER;
			t = tokens.next();

			if(t.equalsToken(Resources.getOrderTranslation(EresseaConstants.O_LEVEL))) {
				retVal = readZaubereRegionStufe(t);
			} else if(isString(t.getText())) {
				retVal = readZaubereSpruch(t);
			} else {
				unexpected(t);
			}

  		if(completer!=null && !t.followedBySpace()){
  					completer.cmpltZaubereRegionCoor(); 
  		}
		}

		return retVal;
	}

	private boolean readZaubereStufe(OrderToken token) {
		boolean retVal = false;
		token.ttype = OrderToken.TT_KEYWORD;

		OrderToken t = tokens.next();

		if(isNumeric(t.getText())) {
			t.ttype = OrderToken.TT_NUMBER;
			t = tokens.next();

			if(isString(t.getText())) {
				retVal = readZaubereSpruch(t);
			} else {
				unexpected(t);
			}

  		if(completer!=null && !t.followedBySpace()){
  					completer.cmpltZaubereStufe(); 
  		}
		}

		return retVal;
	}

	private boolean readZaubereRegionStufe(OrderToken token) {
		boolean retVal = false;
		token.ttype = OrderToken.TT_KEYWORD;

		OrderToken t = tokens.next();

		if(isNumeric(t.getText())) {
			t.ttype = OrderToken.TT_NUMBER;
			t = tokens.next();

			if(isString(t.getText())) {
				retVal = readZaubereSpruch(t);
			} else {
				unexpected(t);
			}

  		if(completer!=null && !t.followedBySpace()){
  					completer.cmpltZaubereRegionStufe(); 
  		}
		}

		return retVal;
	}

  private boolean readZaubereSpruch(OrderToken token){
    boolean retVal = false;
    token.ttype = OrderToken.TT_STRING;
    // checken, ob der Zauberspruch bekannt ist
    // Problem: keine Referenz auf die Unit, wir können nicht die spells der unit durchgehen
    // wir müssen spells der GameData durchgehen
    if (data.spells()==null || data.spells().size()==0){return false;}
    for (Spell s:data.spells().values()){
      String test= token.getText().replaceAll("\"","").replaceAll("~"," ").replaceAll("\'","");
      if (test.equalsIgnoreCase(s.getName())){
        // here we return just true
        // toDo: get Spell Syntax, check, if more tokens expected and
        // do next checks
        skipRestOfOrder();
        return true;
      }
    }
    return retVal;
  }
  
  /**
   * skipps rest of Line
   *
   */
  private void skipRestOfOrder(){
    if (!tokens.hasNext()) return;
    OrderToken t = tokens.next();
    while ((t.ttype != OrderToken.TT_EOC) && (t.ttype != OrderToken.TT_COMMENT) && tokens.hasNext()){
      t = tokens.next();
    }
  }
  
  
//  private boolean readZaubereSpruch(OrderToken token, Spell s) {
//    boolean retVal = false;
//    token.ttype = OrderToken.TT_STRING;
//
//    OrderToken t = tokens.next();
//    
//    SpellSyntax ss = s.getSpellSyntax();
//    ss.reset();
//    SpellSyntaxToken sst = ss.getNextToken();
//   
//    retVal = readZaubereSyntax(t, sst);
//
//    return retVal;
//  }

//  private boolean readZaubereSyntax(OrderToken token, SpellSyntaxToken sst) {
//    switch (sst.getTokenType()) {
//      case SpellSyntaxToken.SST_KeyWord: {
//        if(token.equalsToken(Resources.getOrderTranslation(EresseaConstants.O_CASTLE))) {
//          token.ttype = OrderToken.TT_KEYWORD;
//          token = tokens.next();
//          token.ttype = OrderToken.TT_ID;
//          
//        } else if(token.equalsToken(Resources.getOrderTranslation(EresseaConstants.O_UNIT))) {
//          
//        } else if(token.equalsToken(Resources.getOrderTranslation(EresseaConstants.O_FACTION))) {
//          
//        } else if(token.equalsToken(Resources.getOrderTranslation(EresseaConstants.O_REGION))) {
//          
//        } else if(token.equalsToken(Resources.getOrderTranslation(EresseaConstants.O_SHIP))) {
//          
//        }
//      }
//    }
//    return false;
//  }
  
  
  //************* ZEIGE
	private boolean readZeige(OrderToken token) {
		boolean retVal = false;
		token.ttype = OrderToken.TT_KEYWORD;

		OrderToken t = tokens.next();

		if(t.equalsToken(Resources.getOrderTranslation(EresseaConstants.O_ALL))) {
			retVal = readZeigeAlle(t);
		} else if(isString(t.getText()) == true) {
			retVal = readFinalString(t);
		} else {
			unexpected(t);
		}

		if(completer!=null && !t.followedBySpace()){
				completer.cmpltZeige(); 
		}
		return retVal;
	}

	private boolean readZeigeAlle(OrderToken token) {
		boolean retVal = false;
		token.ttype = OrderToken.TT_KEYWORD;

		OrderToken t = tokens.next();

		if(t.equalsToken(Resources.getOrderTranslation(EresseaConstants.O_POTIONS)) ||
			   t.equalsToken(Resources.getOrderTranslation(EresseaConstants.O_SPELLS))) {
			retVal = readFinalKeyword(t);
		} else {
			unexpected(t);
		}

		if(completer!=null && !t.followedBySpace()){
				completer.cmpltZeigeAlle(); 
		}
		return retVal;
	}

	//************* ZERSTOERE
	private boolean readZerstoere(OrderToken token) {
		boolean retVal = false;
		token.ttype = OrderToken.TT_KEYWORD;

		OrderToken t = tokens.next();

		if(isNumeric(t.getText())) {
			retVal = readZerstoereAmount(t);
		} else if(t.equalsToken(Resources.getOrderTranslation(EresseaConstants.O_ROAD))) {
			retVal = readZerstoereStrasse(t);
		} else {
			retVal = checkFinal(t);
		}

		if(completer!=null && !t.followedBySpace()){
				completer.cmpltZerstoere(); 
		}
		return retVal;
	}

	private boolean readZerstoereAmount(OrderToken token) {
		boolean retVal = false;
		token.ttype = OrderToken.TT_NUMBER;

		OrderToken t = tokens.next();

		if(t.equalsToken(Resources.getOrderTranslation(EresseaConstants.O_ROAD))) {
			retVal = readZerstoereStrasse(t);
		} else {
			retVal = checkFinal(t);
		}

		if(completer!=null && !t.followedBySpace()){
				completer.cmpltZerstoere(); 
		}
		return retVal;
	}

	private boolean readZerstoereStrasse(OrderToken token) {
		boolean retVal = false;
		token.ttype = OrderToken.TT_STRING;

		OrderToken t = tokens.next();

		if(Direction.toInt(t.getText()) != Direction.DIR_INVALID) {
			retVal = readFinalString(t);
		} else {
			unexpected(t);
		}

		if(completer!=null && !t.followedBySpace()){
				completer.cmpltZerstoereStrasse(); 
		}
		return retVal;
	}

	//************* ZUECHTE
	private boolean readZuechte(OrderToken token) {
		boolean retVal = false;
		token.ttype = OrderToken.TT_KEYWORD;

		OrderToken t = tokens.next();

		if(t.equalsToken(Resources.getOrderTranslation(EresseaConstants.O_HERBS)) ||
			   t.equalsToken(Resources.getOrderTranslation(EresseaConstants.O_HORSES))) {
			retVal = readFinalKeyword(t);
		} else {
			unexpected(t);
		}

		if(completer!=null && !t.followedBySpace()){
				completer.cmpltZuechte(); 
		}
		return retVal;
	}

	private boolean readFinalKeyword(OrderToken token) {
		token.ttype = OrderToken.TT_KEYWORD;

		return checkNextFinal();
	}

	private boolean readFinalString(OrderToken token) {
		token.ttype = OrderToken.TT_STRING;

		return checkNextFinal();
	}

	private boolean readFinalID(OrderToken token) {
		token.ttype = OrderToken.TT_ID;

		return checkNextFinal();
	}

	private boolean readFinalNumber(OrderToken token) {
		token.ttype = OrderToken.TT_NUMBER;

		return checkNextFinal();
	}

	/**
	 * Checks whether the next token is the end of line or a comment, i.e. the indicating a valid
	 * end of the order. Reports an unexpected token if that is not the case.
	 */
	protected boolean checkNextFinal() {
		if(tokens.hasNext()) {
			OrderToken t = tokens.next();

			return checkFinal(t);
		} else {
			errMsg = "Missing token";

			return false;
		}
	}

	/**
	 * Checks whether the token t is the end of line or a comment, i.e. the indicating a valid end
	 * of the order. Reports an unexpected token if that is not the case.
	 *
	 */
	private boolean checkFinal(OrderToken t) {
		boolean retVal = ((t.ttype == OrderToken.TT_EOC) || (t.ttype == OrderToken.TT_COMMENT));

		if(retVal == false) {
			unexpected(t);
		}

		return retVal;
	}

	protected void unexpected(OrderToken t) {
		errMsg = "Unexpected token " + t.toString();
	}

	private boolean isNumeric(String txt, int radix, int min, int max) {
		boolean retVal = false;

		try {
			int i = Integer.parseInt(txt, radix);
			retVal = ((i >= min) && (i <= max));
		} catch(NumberFormatException e) {
		}

		return retVal;
	}

	private boolean isNumeric(String txt) {
		return isNumeric(txt, 10, 0, Integer.MAX_VALUE);
	}

	protected boolean isID(String txt) {
		boolean retVal = isNumeric(txt, data.base, 0, EresseaOrderParser.MAX_UID);

		if(retVal == false) {
			retVal = isTempID(txt);
		}

		return retVal;
	}

	private boolean isTempID(String txt) {
		boolean retVal = false;
		int blankPos = txt.indexOf(" ");

		if(blankPos == -1) {
			blankPos = txt.indexOf("\t");
		}

		if(blankPos > -1) {
			String temp = txt.substring(0, blankPos);
			String nr = txt.substring(blankPos + 1);
			retVal = (temp.equalsIgnoreCase("TEMP"));
			retVal = retVal && isNumeric(nr, data.base, 0, EresseaOrderParser.MAX_UID);
		}

		return retVal;
	}

	private boolean isRID(String txt) {
		boolean retVal = false;
		int firstCommaPos = txt.indexOf(",");
		int secondCommaPos = txt.lastIndexOf(",");

		if(firstCommaPos > -1) {
			if(secondCommaPos > firstCommaPos) {
				try {
					Integer.parseInt(txt.substring(0, firstCommaPos));
					Integer.parseInt(txt.substring(firstCommaPos + 1, secondCommaPos));
					Integer.parseInt(txt.substring(secondCommaPos + 1, txt.length()));
					retVal = true;
				} catch(NumberFormatException e) {
					EresseaOrderParser.log.warn("OrderEditor.getColor()", e);
				}
			} else {
				try {
					Integer.parseInt(txt.substring(0, firstCommaPos));
					Integer.parseInt(txt.substring(firstCommaPos + 1, txt.length()));
					retVal = true;
				} catch(NumberFormatException e) {
					EresseaOrderParser.log.warn("OrderEditor.getColor()", e);
				}
			}
		}

		return retVal;
	}

	private boolean isQuoted(String txt) {
		return (txt.startsWith("\"") && txt.endsWith("\""));
	}
  private boolean isSingleQuoted(String txt) {
    return (txt.startsWith("\'") && txt.endsWith("\'"));
  }

	private boolean isString(String txt) {
		boolean retVal = isQuoted(txt);
		
    if (retVal==false){
      retVal = isSingleQuoted(txt);
    }
    // we only allow numbers within text
    // otherwise 1234 would also match to isString
    boolean isNumeric = isNumeric(txt);
		if((retVal == false) && (txt.length() > 0)) {
			retVal = true;
			for(int i = 0; i < txt.length(); i++) {
				char c = txt.charAt(i);
				// we allow numbers if txt is not numeric
				if(!(((c >= 'A') && (c <= 'Z')) || ((c >= 'a') && (c <= 'z')) || (c == 'Ä') ||
					   (c == 'Ö') || (c == 'Ü') || (c == 'ä') || (c == 'ö') || (c == 'ü') ||
					   (c == '~') || (c == 'ß') || (c==',') || (c=='.') || (c=='_') || (c==':') || 
             ((!isNumeric) && (c>='0') && (c<='9')))) {
					retVal = false;
					break;
				}
			}
		}

		return retVal;
	}

	private boolean isEmailAddress(String txt) {
		boolean retVal = true;
		int atIndex = txt.indexOf("@");

		if((atIndex > -1) && (atIndex == txt.lastIndexOf("@"))) {
			for(int i = 0; i < txt.length(); i++) {
				char c = txt.charAt(i);

				if(!(((c >= '0') && (c <= '9')) || ((c >= 'A') && (c <= 'Z')) ||
					   ((c >= 'a') && (c <= 'z')) || (c == '-') || (c == '_') || (c == '.') ||
					   (c == '@'))) {
					retVal = false;

					break;
				}
			}
		} else {
			retVal = false;
		}

		return retVal;
	}

  /**
   * Returns the value of tokenBucket.
   * 
   * @return Returns tokenBucket.
   */
  public Iterator<OrderToken> getTokensIterator() {
    return tokens;
  }

  /**
   * Returns the value of completer.
   * 
   * @return Returns completer.
   */
  public EresseaOrderCompleter getCompleter() {
    return completer;
  }

  /**
   * Returns the value of data.
   * 
   * @return Returns data.
   */
  public GameData getData() {
    return data;
  }
	
	
	
	
}


/**
 * A class for collecting and preprocessing order tokens
 */
class TokenBucket extends Vector<OrderToken> {
	private static final int MAX_TEMP_NR = 1679615; // = (36 ^ 4) - 1;

	/**
	 * Creates a new TokenBucket object.
	 */
	public TokenBucket() {
	}

	/**
	 * Creates a new TokenBucket object.
	 *
	 * 
	 */
	public TokenBucket(Reader in) {
		read(in);
	}

	/**
	 * @see java.util.Vector#add(java.lang.Object)
	 */
	@Override
	public boolean add(OrderToken o) {
		super.add(o);

		return true;
	}

	/**
	 * DOCUMENT-ME
	 *
	 * 
	 * @param in
	 * @return
	 */
	public int read(Reader in) {
		OrderTokenizer tokenizer = new OrderTokenizer(in);
		OrderToken token = null;
		clear();

		do {
			token = tokenizer.getNextToken();
			add(token);
		} while(token.ttype != OrderToken.TT_EOC);

		return size();
	}

	/**
	 * Merges two tokens if the first one contains the string TEMP the second one contains an id.
	 *
	 * @return the number of remaining tokens.
	 */
	public int mergeTempTokens(int base) {
		if(size() > 1) {
			for(int i = 0; i < (size() - 1); i++) {
				OrderToken tempToken = tokenAt(i);
				String tempText = tempToken.getText();

				if(tempText.equalsIgnoreCase("TEMP")) {
					try {
						OrderToken nrToken = tokenAt(i + 1);
						String nrText = nrToken.getText();
						int nr = IDBaseConverter.parse(nrText,base);

						if((nr >= 0) && (nr <= TokenBucket.MAX_TEMP_NR)) {
							tempToken.setText("TEMP " + nrText);

							if((tempToken.getEnd() > -1) && (nrToken.getEnd() > -1)) {
								tempToken.setEnd(nrToken.getEnd());
							}

							remove(i + 1);
						}
					} catch(NumberFormatException e) {
					}
				}
			}
		}

		return size();
	}

	/**
	 * DOCUMENT-ME
	 *
	 * 
	 *
	 * 
	 */
	public OrderToken tokenAt(int index) {
		OrderToken retVal = null;

		if(index < size()) {
			retVal = elementAt(index);
		}

		return retVal;
	}
	
	
}
