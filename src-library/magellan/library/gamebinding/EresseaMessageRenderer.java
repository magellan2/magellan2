// class magellan.library.gamebinding.EresseaMessageRenderer
// created on 28.11.2007
//
// Copyright 2003-2007 by magellan project team
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

import magellan.library.Message;
import magellan.library.GameData;
import magellan.library.EntityID;
import magellan.library.CoordinateID;
import magellan.library.Unit;
import magellan.library.Region;
import magellan.library.Faction;
import magellan.library.Ship;
import magellan.library.Building;
import magellan.library.utils.Resources;

import java.util.Map;
import java.util.regex.*;
import java.text.ParseException;
import java.util.StringTokenizer;
/**
 * A Renderer for Eressea Messages
 * 
 * Messages in Eressea look like this:
 * MESSAGE 350568592
 * 5281483;type
 * "Whio (whio) übergibt 10 Silber an Darin Jerekop (djer).";rendered
 * 1515696;unit
 * 10;amount
 * "Silber";resource
 * 631683;target
 * 
 * The rendered tag can be rendered by this Renderer using the Message, 
 * the Messagetype and Translations
 * 
 * Messagetypes look like this:
 * 
 * MESSAGETYPE 5281483
 * "\"$unit($unit) übergibt $int($amount) $resource($resource,$amount) an $unit($target).\"";text
 * "economy";section
 *
 * @author ...
 * @version 1.0, 28.11.2007
 */
public class EresseaMessageRenderer implements MessageRenderer {
  /**
   * The GameData as we need a lot of "background" information from it. 
   */
  private GameData gd = null;
  
  /**
   * A regexp pattern that will be initialzed in the constructor to find 
   * attributes and functions
   */
  private Pattern literalsPattern;
  
  /**
   * The Constructor 
   * @param gd - The GameData - required for a lot of functions
   */
  public EresseaMessageRenderer(GameData gd) {
    this.gd = gd; 
    try {
      this.literalsPattern = Pattern.compile("[a-z.]+\\(?");
    } catch (Exception e) {
      e.printStackTrace();
    }
  }
  
  /**
   * Renders the Message
   * 
   * @see magellan.library.gamebinding.MessageRenderer#renderMessage(magellan.library.Message)
   */
  public String renderMessage(Message msg) {
    String pat = msg.getMessageType().getPattern();
    try {
      return renderString(new StringBuffer(pat), msg.getAttributes());
    } catch (ParseException e) {
      return pat+" "+e.getMessage()+" Last parse position "+(pat.length()-e.getErrorOffset());
    }
  }
  
  /**
   * Renders a String part of the message.
   * 
   * @param unparsed - the rightmost part that is not parsed up to now - the method manipulates this!
   * @param attributes - the mapping of atributes to values of the message
   * @return a String containing the fully rendered String part
   * @throws ParseException if any problem occured parsing the msg
   */
  private String renderString(StringBuffer unparsed, Map<String,String> attributes) throws ParseException {
    // remove trailing spaces
    while (unparsed.charAt(0)==' ') {
      unparsed.deleteCharAt(0);
    }
    if (unparsed.charAt(0)=='"') {
      StringBuffer parsed = new StringBuffer(); 
      unparsed.deleteCharAt(0);
      // search first $ sign
      int posd = unparsed.indexOf("$");
      // search first " sign
      int posq = unparsed.indexOf("\"");
      // $ should be before " and both should be >=0
      while ((0 <= posd) && (posd < posq)) {
        // append all before $ to the return string
        parsed.append(unparsed.substring(0, posd));
        // delete from beginning to $ sign
        unparsed.delete(0, posd+1);
        // append the returned string of the evaluated $ to the return string  
        parsed.append(renderDollar(unparsed, attributes));
        // find next $ and " sign
        posd = unparsed.indexOf("$");
        posq = unparsed.indexOf("\"");
      }
      // append remaining unparsed string up to " 
      if (posq >= 0) {
        parsed.append(unparsed.substring(0, posq));
        unparsed.delete(0, posq+1);
        return parsed.toString();
      } else {
        throw new ParseException("ERROR: no matching quote \"", unparsed.length());
      }
    } else if (unparsed.charAt(0)=='$') {
      unparsed.deleteCharAt(0);
      try {
        return (String)renderDollar(unparsed, attributes);
      } catch (ClassCastException e) {
        throw new ParseException("ERROR: String expected", unparsed.length());
      }
    } else {
      throw new ParseException("ERROR: No string found", unparsed.length());
    }
  }
  
  /**
   * mainly decides if we have a attribute or function following the $ sign
   * 
   * @param unparsed - the rightmost part that is not parsed up to now - the method manipulates this!
   * @param attributes - the mapping of atributes to values of the message
   * @return a String or Array of int could also be NULL
   * @throws ParseException if any problem occured parsing the msg
   */
  private Object renderDollar(StringBuffer unparsed, Map<String,String> attributes) throws ParseException {
    if (unparsed.charAt(0)=='{') {
      int pos = unparsed.indexOf("}");
      if (pos > 0) {
        String name=unparsed.substring(1, pos);
        unparsed.delete(0, pos+1);
        return renderAttribute(name, attributes);
      } else {
        // ERROR: no matching }
        throw new ParseException("ERROR: no matching bracket '}'", unparsed.length());
      }
    } else {
      Matcher m=literalsPattern.matcher(unparsed);
      if (m.find()&&m.start()==0) {
        String name = m.group();
        if (unparsed.charAt(m.end()-1)=='(') {
          // a function
          // delete function name and opening bracket (
          unparsed.delete(0, m.end());
          // note that we give the name including the opening bracket (
          return renderFunction(name, unparsed, attributes);
        } else {
          // an attribute
          unparsed.delete(0, m.end());
          return renderAttribute(name, attributes);
        }
      } else {
        throw new ParseException("ERROR: $ without literals", unparsed.length());
      }
    }
  }
  
  /**
   * Returns the attribute value for a given attribute name.
   * 
   * @param name - the name of the attribute
   * @param attributes - the mapping of atributes to values of the message
   * @return The attribute value as String
   */
  private String renderAttribute(String name, Map<String,String> attributes) {
    return attributes.get(name);
  }
  
  /**
   * Interpretes the functions. This is the most "complex" part of the message rendering. 
   * If a unknown function arises it has to be implemented here.
   * 
   * @param name - the name of the function without dollar "$" but with the opening bracket "("
   * 
   * @param unparsed - the rightmost part that is not parsed up to now - the method manipulates this!
   * @param attributes - the mapping of atributes to values of the message
   * @return a String or Array of int could also be NULL
   * @throws ParseException if any problem occured parsing the msg
   */
  private Object renderFunction(String name, StringBuffer unparsed, Map<String,String> attributes) throws ParseException {
    Object value;
    // $int()
    if (name.equals("int(")) {
      value = Integer.toString(renderInteger(unparsed, attributes)[0]);
    // $unit() 
    // $unit.dative()  
    } else if (name.equals("unit(")||name.equals("unit.dative(")) {
      EntityID uid = EntityID.createEntityID(renderInteger(unparsed, attributes)[0], gd.base);
      Unit unit = gd.getUnit(uid);
      if (unit != null) {
        value = unit.toString();
      } else {
        value = Resources.get("unit.unit")+" ("+uid.toString()+")";
      }        
    // $region()
    } else if (name.equals("region(")) {
      int[] i_ar = renderInteger(unparsed, attributes);
      if ((i_ar != null)&&(i_ar.length>=2)) {
        CoordinateID rid;
        if (i_ar.length == 2) {
          rid = new CoordinateID(i_ar[0], i_ar[1]);
        } else {
          rid = new CoordinateID(i_ar[0], i_ar[1], i_ar[2]);
        }
        Region r = gd.getRegion(rid);
        if (r != null) {
          value = r.toString();
        } else {
          value = "??? ("+rid.toString()+")";
        }
      } else {
        if (i_ar == null) {
          value = "NULL (???)";
        } else {
          throw new ParseException("ERROR: wrong arguments for region()", unparsed.length());
        }
      }
    // $faction()
    } else if (name.equals("faction(")) {
      EntityID fid = EntityID.createEntityID(renderInteger(unparsed, attributes)[0], gd.base);
      Faction faction = gd.getFaction(fid);
      if (faction != null) {
        value = faction.toString();
      } else {
        value = Resources.get("faction.faction")+" ("+fid.toString()+")";
      }        
    // $ship()
    } else if (name.equals("ship(")) {
      EntityID sid = EntityID.createEntityID(renderInteger(unparsed, attributes)[0], gd.base);
      Ship ship = gd.getShip(sid);
      if (ship != null) {
        value = ship.toString();
      } else {
        value = Resources.get("ship.ship")+" ("+sid.toString()+")";
      }        
    // $building()
    } else if (name.equals("building(")) {
      EntityID bid = EntityID.createEntityID(renderInteger(unparsed, attributes)[0], gd.base);
      Building building = gd.getBuilding(bid);
      if (building != null) {
        value = building.toString();
      } else {
        value = Resources.get("building.building")+" ("+bid.toString()+")";
      }        
    // $eq()
    } else if (name.equals("eq(")) {
      int a = renderInteger(unparsed, attributes)[0];
      int pos = unparsed.indexOf(",");
      if (pos>=0) {
        unparsed.delete(0, pos+1);
        int b = renderInteger(unparsed, attributes)[0];
        int[] i = new int[1];
        if (a == b) {
          i[0] = 1;
        } else {
          i[0] = 0;
        }
        value = i;
      } else {
        throw new ParseException("ERROR: wrong arguments for eq()", unparsed.length());
      }
    // $add()
    } else if (name.equals("add(")) {
      int a = renderInteger(unparsed, attributes)[0];
      int pos = unparsed.indexOf(",");
      if (pos>=0) {
        unparsed.delete(0, pos+1);
        int b = renderInteger(unparsed, attributes)[0];
        int[] i = new int[1];
        if (a == b) {
          i[0] = 1;
        } else {
          i[0] = 0;
        }
        value = i;
      } else {
        throw new ParseException("ERROR: wrong arguments for eq()", unparsed.length());
      }
    // $if()
    } else if (name.equals("if(")) {
      int cond = renderInteger(unparsed, attributes)[0];
      int pos = unparsed.indexOf(",");
      if (pos>=0) {
        unparsed.delete(0, pos+1);
        String do_true = renderString(unparsed, attributes);
        pos = unparsed.indexOf(",");
        if (pos>=0) {
          unparsed.delete(0, pos+1);
          String do_false = renderString(unparsed, attributes);
          if (cond>0) {
            value = do_true;
          } else {
            value = do_false;
          }
        } else {
          throw new ParseException("ERROR: wrong arguments for if()", unparsed.length());
        }
      } else {
        throw new ParseException("ERROR: wrong arguments for if()", unparsed.length());
      }
    // $strlen()
    } else if (name.equals("strlen(")) {
      String content = renderString(unparsed, attributes);
      int[] i = new int[1];
      i[0] = content.length();
      value = i;
    // $order()
    } else if (name.equals("order(")) {
      value = renderString(unparsed, attributes);
    // $isnull()
    } else if (name.equals("isnull(")) {
      Object obj = renderNull(unparsed, attributes);
      int[] i = new int[1];
      if (obj == null) {
        i[0] = 1;
      } else {
        i[1] = 0;
      }
      value = i;  
    // $resource()
    } else if (name.equals("resource(")) {
      String item = renderString(unparsed, attributes);
      int pos = unparsed.indexOf(",");
      if (pos>=0) {
        unparsed.delete(0, pos+1);
        int amount = renderInteger(unparsed, attributes)[0];
        // TODO use amount if possible
        value = gd.getTranslation(item);
      } else {
        throw new ParseException("ERROR: wrong arguments for resource()", unparsed.length());
      }
    // $localize()
    // $skill()
    // $spell()
    // $race()
    } else if (name.equals("localize(")||name.equals("skill(")||name.equals("spell(")||name.equals("race(")) {
      String str = renderString(unparsed, attributes);
      value = gd.getTranslation(str);
    // $weight()
    } else if (name.equals("weight(")) {
      int weight = renderInteger(unparsed, attributes)[0];
      value = (weight/100)+" GE"; 
    // $resources()
    } else if (name.equals("resources(")) {
      value = renderString(unparsed, attributes);
      
      
    // TODO more functions to implement:

    // $direction()
      
    } else {
      throw new ParseException("ERROR: unknown function "+name+")", unparsed.length());     
    }

    // delete closing bracket before returning the value
    int close = unparsed.indexOf(")");
    if (close >=0) {
      unparsed.delete(0, close+1);
      return value;
    } else {
      throw new ParseException("ERROR: no matching bracket ')'", unparsed.length());
    }
  }
  
  /**
   * Tries to render an integer. This could of course also be a return value 
   * of a function or an attribute value. The return of attributes and functions 
   * are treated like constants. 
   * 
   * @param unparsed - the rightmost part that is not parsed up to now - the method manipulates this!
   * @param attributes - the mapping of atributes to values of the message
   * @return an Array of int
   * @throws ParseException if any problem occured parsing the msg
   */
  private int[] renderInteger(StringBuffer unparsed, Map<String,String> attributes) throws ParseException {
    // remove trailing spaces
    while (unparsed.charAt(0)==' ') {
      unparsed.deleteCharAt(0);
    }
    if (unparsed.charAt(0)=='$') {
      unparsed.deleteCharAt(0);
      Object obj = renderDollar(unparsed, attributes);  
      try {
        return (int[])obj;
      } catch (ClassCastException eint) {
        try {
          unparsed.insert(0, ((String)obj).replaceAll(",", " "));
        } catch (ClassCastException estr) {
          throw new ParseException("ERROR: Integer[] expected", unparsed.length());
        }
      }
    } 
    final Pattern numbersPattern = Pattern.compile("-?[0-9]+( +-?[0-9]+)*");
    // parse constant integers
    Matcher m=numbersPattern.matcher(unparsed);
    if (m.find() && m.start() == 0) {
      StringTokenizer st = new StringTokenizer(m.group()," ,");
      int[] ar = new int[st.countTokens()];
      int i = 0;
      while (st.hasMoreTokens()) {
        ar[i] = Integer.parseInt(st.nextToken());
        i++;
      }
      unparsed.delete(0, m.end());
      return ar;
    } else {
      throw new ParseException("ERROR: No Integer constant found", unparsed.length());
    }
  }
  
  /**
   * Tries to render a constant value NULL or returns the value of an attribute or function. 
   * 
   * @param unparsed - the rightmost part that is not parsed up to now - the method manipulates this!
   * @param attributes - the mapping of atributes to values of the message
   * @return something that may be NULL
   * @throws ParseException if any problem occured parsing the msg, i.e. a constant STRING or integer is found
   */

  private Object renderNull(StringBuffer unparsed, Map<String,String> attributes) throws ParseException {
    // remove trailing spaces
    while (unparsed.charAt(0)==' ') {
      unparsed.deleteCharAt(0);
    }
    if (unparsed.charAt(0)=='$') {
      unparsed.deleteCharAt(0);
      return renderDollar(unparsed, attributes);  
    } else if (unparsed.indexOf("NULL")==0) {
      unparsed.delete(0, 4);
      return null;
    } else {
      throw new ParseException("ERROR: Attribute expected for isnull()", unparsed.length());
    }
  }
}
