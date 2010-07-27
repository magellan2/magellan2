package magellan.library.utils;

import java.util.LinkedList;

/**
 * Object for handling the spell syntax and the tokens within
 * 
 * @author Fiete
 */
public class SpellSyntax {

  private final char syntax_add = '+'; // new Character("+".charAt(0));
  private final char syntax_notNeeded = '?'; // new Character("?".charAt(0));

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
   * 
   * @param text
   */
  public SpellSyntax(String text) {
    parseSyntax(text);
  }

  private void parseSyntax(String text) {
    if (text == null || text.length() == 0)
      return;
    SpellSyntaxToken t = null;
    char[] chars = text.toCharArray();

    for (char d : chars) {
      if (t != null && d == syntax_add) {
        t.setMultiple(true);
      } else if (t != null && d == syntax_notNeeded) {
        t.setNeeded(false);
      } else {
        t = new SpellSyntaxToken(d);
        addToken(t);
      }
    }
  }

  /**
   * adds a token to this SpellSyntax
   * 
   * @param t - the nw SpellSyntaxToken
   */
  private void addToken(SpellSyntaxToken t) {
    if (tokens == null) {
      tokens = new LinkedList<SpellSyntaxToken>();
    }
    tokens.add(t);
  }

  public SpellSyntaxToken getNextToken() {
    if (!hasNextToken())
      return null;
    tokenIndex++;
    return tokens.get(tokenIndex);
  }

  public int getTokenSize() {
    if (tokens == null)
      return 0;
    else
      return tokens.size();
  }

  public void reset() {
    tokenIndex = 0;
  }

  public boolean hasNextToken() {
    if (tokens != null && tokens.size() > 0 && tokenIndex < (tokens.size() - 2))
      return true;
    else
      return false;
  }

  public SpellSyntaxToken getCurrentToken() {
    if (tokens != null && tokens.size() > 0)
      return tokens.get(tokenIndex);
    else
      return null;

  }

  /**
   * returns a string with token information or null
   */
  @Override
  public String toString() {
    String retVal = null;
    if (tokens == null || tokens.size() == 0)
      // FIXME returning null is a bad idea
      return retVal;
    for (SpellSyntaxToken token : tokens) {
      String tokenString = token.toString();
      if (tokenString != null) {
        if (retVal == null) {
          retVal = "";
        }
        if (retVal.length() > 0) {
          retVal += " ";
        }

        retVal = retVal + tokenString;
      }
    }
    return retVal;
  }

}