package magellan.library.utils;

import java.util.Iterator;
import java.util.LinkedList;


/**
 * 
 * Object for handling the spell syntax and the tokens within
 * 
 * @author Fiete
 *
 */
public class SpellSyntax {
	
	private final Character syntax_add = new Character("+".charAt(0));
	private final Character syntax_notNeeded = new Character("?".charAt(0));
	
	/**
	 * List of our Tokens
	 */
	private LinkedList<SpellSyntaxToken> tokens = null;
	
	/**
	 * we providing a step through functionality, this is our step
	 */
	private int tokenIndex = 0;
	
	
	/**
	 * creates a new Spellsyntax object from a syntax string
	 * @param text
	 */
	public SpellSyntax(String text){
		parseSyntax(text);
	}
	
	private void parseSyntax(String text){
		if (text==null || text.length() == 0){
			return;
		}
		SpellSyntaxToken t = null;
		char[] chars = text.toCharArray();
		
		for (int i = 0;i<chars.length;i++){
			Character c = new Character(chars[i]);
			if (t!=null && c.equals(this.syntax_add)){
				t.setMultiple(true);
			} else if (t!=null && c.equals(this.syntax_notNeeded)){
				t.setNeeded(false);
			} else {
				t = new SpellSyntaxToken(c);
				addToken(t);
			} 
		}
	}
	
	
	/**
	 * adds a token to this SpellSyntax
	 * @param t - the nw SpellSyntaxToken
	 */
	private void addToken(SpellSyntaxToken t){
		if (this.tokens==null){
			this.tokens = new LinkedList<SpellSyntaxToken>();
		}
		this.tokens.add(t);
	}
	
	public SpellSyntaxToken getNextToken(){
		if (!hasNextToken()){
			return null;
		}
		this.tokenIndex++;
		return (SpellSyntaxToken)this.tokens.get(this.tokenIndex);
	}
	
	public int getTokenSize(){
		if (this.tokens==null){
			return 0;
		} else {
			return this.tokens.size();
		}
	}
	
	public void reset(){
		this.tokenIndex = 0;
	}
	
	public boolean hasNextToken(){
		if (this.tokens != null && this.tokens.size()>0 && this.tokenIndex<(this.tokens.size()-2)){
			return true;
		} else {
			return false;
		}
	}
	
	public SpellSyntaxToken getCurrentToken(){
		if (this.tokens != null && this.tokens.size()>0){
			return (SpellSyntaxToken)this.tokens.get(this.tokenIndex);
		} else {
			return null;
		}
		
	}
	
	/**
	 * returns a string with token information or null
	 */
	public String toString(){
		String retVal = null;
		if (this.tokens==null || this.tokens.size()==0){
			return retVal;
		}
		for (Iterator iter = this.tokens.iterator();iter.hasNext();){
			SpellSyntaxToken token = (SpellSyntaxToken) iter.next();
			String tokenString = token.toString();
			if (tokenString!=null) {
				if (retVal==null){
					retVal = "";
				}
				if (retVal.length()>0){
					retVal += " ";
				}
				
				retVal = retVal + tokenString;
			}
		}
		return retVal;
	}
	
	
	
}