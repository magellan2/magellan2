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
 * TODO This class must be commented
 *
 * @author ...
 * @version 1.0, 28.11.2007
 */
public class EresseaMessageRenderer extends MessageRenderer {
  private GameData gd = null;
  // we have to add the . since i found $unit.dative($unit) as a function
  private Pattern literalsPattern;
  
  public EresseaMessageRenderer(GameData gd) {
    this.gd = gd; 
    try {
      this.literalsPattern = Pattern.compile("[a-z.]+\\(?");
    } catch (Exception e) {
      e.printStackTrace();
    }
  }
  
  /**
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
  
  private String renderAttribute(String name, Map<String,String> attributes) throws ParseException {
    return attributes.get(name);
/*
    // String value?
    if (attrval.charAt(0)=='"') {
      return attrval.substring(1, attrval.length()-1);
    // Integer or Integer triple?
    } else {
      final Pattern numbersPattern = Pattern.compile("-?[0-9]+( -?[0-9]+)*");
      // parse constant integers
      Matcher m=numbersPattern.matcher(attrval);
      if (m.find() && m.start() == 0) {
        StringTokenizer st = new StringTokenizer(m.group());
        int[] ar = new int[st.countTokens()];
        int i = 0;
        while (st.hasMoreTokens()) {
          ar[i] = Integer.parseInt(st.nextToken());
          i++;
        }
        return ar;
      } else {
        throw new ParseException("ERROR: Attribute interpretion failed "+name+" "+attrval, 0);
      }
    }
*/
  }
  
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
