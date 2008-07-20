package magellan.library.utils;





/**
 * object for one syntax token and properties of the token
 * 
 * @author Fiete
 *
 */
public class SpellSyntaxToken {
	
	/**
	 * token types
	 */
	public final static int SST_undef = 0;
	public final static int SST_String = 1;
	public final static int SST_KeyWord = 2;
	public final static int SST_Number = 3;
	public final static int SST_ShipID = 4;
	public final static int SST_BuildingID = 5;
	public final static int SST_Coordinate = 6;
	public final static int SST_UnitID = 7;
	
	public int getTokenType(){
		int retVal = SpellSyntaxToken.SST_undef;	
		if (this.tokenChar==null){
			return retVal;
		} else if (getTokenString().equals("c")){
			retVal = SpellSyntaxToken.SST_String;
		} else if (getTokenString().equals("k")){
			retVal = SpellSyntaxToken.SST_KeyWord;
		} else if (getTokenString().equals("i")){
			retVal = SpellSyntaxToken.SST_Number;
		} else if (getTokenString().equals("s")){
			retVal = SpellSyntaxToken.SST_ShipID;
		} else if (getTokenString().equals("b")){
			retVal = SpellSyntaxToken.SST_BuildingID;
		} else if (getTokenString().equals("r")){
			retVal = SpellSyntaxToken.SST_Coordinate;
		} else if (getTokenString().equals("u")){
			retVal = SpellSyntaxToken.SST_UnitID;
		}
		return retVal;
	}
	
	
	/**
	Enno 17.02.2007 in e-client
	'c' = Zeichenkette
	'k' = REGION|EINHEIT|STUFE|SCHIFF|GEBAEUDE
	'i' = Zahl
	's' = Schiffsnummer
	'b' = Gebaeudenummer
	'r' = Regionskoordinaten (x, y)
	'u' = Einheit
	'+' = Wiederholung des vorangehenden Parameters
	'?' = vorangegangener Parameter ist nicht zwingend
	*/
	private Character tokenChar = null;
	private boolean needed = true;
	private boolean multiple = false;
	
	/**
	 * constructor
	 *
	 */
	public SpellSyntaxToken(char c){
		this.tokenChar = new Character(c);
	}
	
	/**
	 * constructor
	 *
	 */
	public SpellSyntaxToken(Character c){
		this.tokenChar = c;
	}

	/**
	 * @return the multiple
	 */
	public boolean isMultiple() {
		return multiple;
	}

	/**
	 * @param multiple the multiple to set
	 */
	public void setMultiple(boolean multiple) {
		this.multiple = multiple;
	}

	/**
	 * @return the needed
	 */
	public boolean isNeeded() {
		return needed;
	}

	/**
	 * @param needed the needed to set
	 */
	public void setNeeded(boolean needed) {
		this.needed = needed;
	}

	/**
	 * @return the tokenCharacter
	 */
	public Character getTokenCharacter() {
		return tokenChar;
	}
	
	/**
	 * @return the tokenCharacter as String
	 */
	public String getTokenString(){
		if (this.tokenChar==null){
			return null;
		} else {
			return this.tokenChar.toString();
		}
	}
	
	/**
	 * returns a string representation of the object
	 */
	@Override
  public String toString(){
		String retVal = getTokenString();
		if (retVal==null){
			return retVal;
		}
		String tokenString = "<" + Resources.get("util.spellsyntaxtoken.SpellSyntaxToken." + retVal) + ">";
		retVal = tokenString;
		// if optional...let it show
		if (!isNeeded()){
			retVal = "[" + retVal;
		}
		
		// if multiple entries are possibel...let it show
		if (isMultiple()){
			retVal = retVal + " [" + tokenString + " ...]";
		}
		// if optional...let it show
		if (!isNeeded()){
			retVal = retVal + "]";
		}
		
		return retVal;
	}
	
}