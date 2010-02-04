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

import java.text.ParseException;
import java.util.Hashtable;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import magellan.library.Building;
import magellan.library.CoordinateID;
import magellan.library.EntityID;
import magellan.library.Faction;
import magellan.library.GameData;
import magellan.library.Message;
import magellan.library.Region;
import magellan.library.Ship;
import magellan.library.Unit;
import magellan.library.rules.MessageType;
import magellan.library.utils.Resources;
import magellan.library.utils.logging.Logger;

/**
 * A Renderer for Eressea Messages Messages in Eressea look like this: MESSAGE 350568592
 * 5281483;type "Whio (whio) übergibt 10 Silber an Darin Jerekop (djer).";rendered 1515696;unit
 * 10;amount "Silber";resource 631683;target The rendered tag can be rendered by this Renderer using
 * the Message, the Messagetype and Translations Messagetypes look like this: MESSAGETYPE 5281483
 * "\"$unit($unit) übergibt $int($amount) $resource($resource,$amount) an $unit($target).\"";text
 * "economy";section
 * 
 * @author ...
 * @version 1.0, 28.11.2007
 */
public class EresseaMessageRenderer implements MessageRenderer {
  private static final Logger log = Logger.getInstance(EresseaMessageRenderer.class);
  private static final Map<MessageType, MessageType> loggedTypes =
      new Hashtable<MessageType, MessageType>();
  /**
   * The GameData as we need a lot of "background" information from it.
   */
  private GameData gd = null;

  /**
   * A regexp pattern that will be initialzed in the constructor to find attributes and functions
   */
  private Pattern literalsPattern;

  /**
   * A unit with id 1 is an unknown unit
   */
  private EntityID unknownUnit;

  /**
   * The Constructor
   * 
   * @param gd - The GameData - required for a lot of functions
   */
  public EresseaMessageRenderer(GameData gd) {
    this.gd = gd;
    try {
      literalsPattern = Pattern.compile("[a-z.]+\\(?");
      unknownUnit = EntityID.createEntityID(1, gd.base);
    } catch (Exception e) {
      EresseaMessageRenderer.log.error(e.getMessage(), e);
    }
  }

  /**
   * Renders the Message
   * 
   * @see magellan.library.gamebinding.MessageRenderer#renderMessage(magellan.library.Message)
   */
  public String renderMessage(Message msg) {
    String pat = msg.getMessageType().getPattern();
    if (pat == null)
      return null;
    try {
      String rendered = renderString(new StringBuffer(pat), msg.getAttributes());
      if ((rendered == null) || (rendered.equals("")))
        // fix for empty strings, that cause trouble in the message panel
        return " ";
      else
        return rendered;
    } catch (ParseException e) {
      if (!EresseaMessageRenderer.loggedTypes.containsKey(msg.getMessageType())) {
        EresseaMessageRenderer.loggedTypes.put(msg.getMessageType(), msg.getMessageType());
        if (msg.getAttributes() == null) {
          EresseaMessageRenderer.log.warn("Message Rendering Error: " + pat + " null "
              + e.getMessage() + " Last parse position " + (pat.length() - e.getErrorOffset()), e);
        } else {
          EresseaMessageRenderer.log.warn("Message Rendering Error: " + pat + " "
              + msg.getAttributes().toString() + " " + e.getMessage() + " Last parse position "
              + (pat.length() - e.getErrorOffset()), e);
        }
      }
      return null;
    } catch (Exception e) {
      if (!EresseaMessageRenderer.loggedTypes.containsKey(msg.getMessageType())) {
        EresseaMessageRenderer.loggedTypes.put(msg.getMessageType(), msg.getMessageType());
        if (msg.getAttributes() == null) {
          EresseaMessageRenderer.log.warn("Message Rendering Error: " + pat + " null "
              + e.getMessage(), e);
        } else {
          EresseaMessageRenderer.log.error("Message Rendering Error: " + pat + " "
              + msg.getAttributes().toString() + " " + e.getMessage(), e);
        }
      }
      return null;
    }
  }

  /**
   * Renders a String part of the message.
   * 
   * @param unparsed - the rightmost part that is not parsed up to now - the method manipulates
   *          this!
   * @param attributes - the mapping of atributes to values of the message
   * @return a String containing the fully rendered String part
   * @throws ParseException if any problem occured parsing the msg
   */
  private String renderString(StringBuffer unparsed, Map<String, String> attributes)
      throws ParseException {
    // remove trailing spaces
    while (unparsed.charAt(0) == ' ') {
      unparsed.deleteCharAt(0);
    }
    if (unparsed.charAt(0) == '"') {
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
        unparsed.delete(0, posd + 1);
        // append the returned string of the evaluated $ to the return string
        parsed.append(renderDollar(unparsed, attributes));
        // find next $ and " sign
        posd = unparsed.indexOf("$");
        posq = unparsed.indexOf("\"");
      }
      // append remaining unparsed string up to "
      if (posq >= 0) {
        parsed.append(unparsed.substring(0, posq));
        unparsed.delete(0, posq + 1);
        return parsed.toString();
      } else
        throw new ParseException("no matching quote \"", unparsed.length());
    } else if (unparsed.charAt(0) == '$') {
      unparsed.deleteCharAt(0);
      try {
        return (String) renderDollar(unparsed, attributes);
      } catch (ClassCastException e) {
        throw new ParseException("String expected", unparsed.length());
      }
    } else
      throw new ParseException("No string found", unparsed.length());
  }

  /**
   * mainly decides if we have a attribute or function following the $ sign
   * 
   * @param unparsed - the rightmost part that is not parsed up to now - the method manipulates
   *          this!
   * @param attributes - the mapping of atributes to values of the message
   * @return a String or Array of int could also be NULL
   * @throws ParseException if any problem occured parsing the msg
   */
  private Object renderDollar(StringBuffer unparsed, Map<String, String> attributes)
      throws ParseException {
    if (unparsed.charAt(0) == '{') {
      int pos = unparsed.indexOf("}");
      if (pos > 0) {
        String name = unparsed.substring(1, pos);
        unparsed.delete(0, pos + 1);
        return renderAttribute(name, attributes);
      } else
        // ERROR: no matching }
        throw new ParseException("no matching bracket '}'", unparsed.length());
    } else {
      Matcher m = literalsPattern.matcher(unparsed);
      if (m.find() && m.start() == 0) {
        String name = m.group();
        if (unparsed.charAt(m.end() - 1) == '(') {
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
      } else
        throw new ParseException("$ without literals", unparsed.length());
    }
  }

  /**
   * Returns the attribute value for a given attribute name.
   * 
   * @param name - the name of the attribute
   * @param attributes - the mapping of atributes to values of the message
   * @return The attribute value as String
   */
  private String renderAttribute(String name, Map<String, String> attributes) {
    String attr = attributes.get(name);
    if ((attr == null) || (attr.equals("")))
      // fix for empty attributes ->->--^
      return null;
    else
      return attr;
  }

  /**
   * Interpretes the functions. This is the most "complex" part of the message rendering. If a
   * unknown function arises it has to be implemented here.
   * 
   * @param name - the name of the function without dollar "$" but with the opening bracket "("
   * @param unparsed - the rightmost part that is not parsed up to now - the method manipulates
   *          this!
   * @param attributes - the mapping of atributes to values of the message
   * @return a String or Array of int could also be NULL
   * @throws ParseException if any problem occured parsing the msg
   */
  private Object renderFunction(String name, StringBuffer unparsed, Map<String, String> attributes)
      throws ParseException {
    Object value;
    // $int(<int>)
    if (name.equals("int(")) {
      int[] ar = renderInteger(unparsed, attributes);
      if (ar != null) {
        value = Integer.toString(ar[0]);
      } else
        throw new ParseException("argument of int() returns NULL", unparsed.length());
    } else if (name.equals("unit(") || name.equals("unit.dative(")) {
      EntityID uid = renderEntityID(unparsed, attributes);
      if ((uid == null) || (uid.equals(unknownUnit))) {
        value = Resources.get("eresseamsgrenderer.func.unit.unknown");
      } else {
        Unit unit = gd.getUnit(uid);
        if (unit != null) {
          value = unit.toString();
        } else {
          value = Resources.get("eresseamsgrenderer.func.unit.unit") + " (" + uid.toString() + ")";
        }
      }
      // $region(<int int int>)
    } else if (name.equals("region(")) {
      int[] i_ar = renderInteger(unparsed, attributes);
      if ((i_ar != null) && (i_ar.length >= 2)) {
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
          value =
              Resources.get("eresseamsgrenderer.func.region.unknown") + " (" + rid.toString() + ")";
        }
      } else {
        if (i_ar == null) {
          value = Resources.get("eresseamsgrenderer.func.region.unknown");
        } else
          throw new ParseException("wrong arguments for region()", unparsed.length());
      }
      // $trail(<string>)
    } else if (name.equals("trail(")) {
      String trailparam = renderString(unparsed, attributes);
      StringBuffer trail = new StringBuffer();
      if (trailparam != null) {
        String[] regions = trailparam.split(",");
        for (int i = 0; i < regions.length; i++) {
          String[] coords = regions[i].trim().split(" ");
          if ((coords != null) && (coords.length >= 2)) {
            CoordinateID rid;
            if (coords.length == 2) {
              rid = new CoordinateID(Integer.parseInt(coords[0]), Integer.parseInt(coords[1]));
            } else {
              rid =
                  new CoordinateID(Integer.parseInt(coords[0]), Integer.parseInt(coords[1]),
                      Integer.parseInt(coords[2]));
            }
            Region r = gd.getRegion(rid);
            if (r != null) {
              // TODO include "the plains of" in the return String
              // this is hard as we need to know the correct german artikel
              // die Ebene, die Wüste (, die Berge)
              // der Gletscher, der Sumpf, der Vulkan, der Aktive Vulkan, der Wald
              // das Hochland, das Bergland
              trail.append(r.toString());
            } else {
              trail.append(Resources.get("eresseamsgrenderer.func.region.unknown") + " ("
                  + rid.toString() + ")");
            }
            switch (regions.length - i) {
            case 1:
              break;
            case 2:
              trail.append(" " + Resources.get("eresseamsgrenderer.func.trail.and") + " ");
              break;
            default:
              trail.append(", ");
            }
          } else {
            // we add nothing - leave string empty
          }
        }
        value = trail.toString();
      } else {
        value = "";
      }
      // $faction(<ID>)
    } else if (name.equals("faction(")) {
      EntityID fid = renderEntityID(unparsed, attributes);
      if (fid == null) {
        value = Resources.get("eresseamsgrenderer.func.faction.unknown");
      } else {
        Faction faction = gd.getFaction(fid);
        if (faction != null) {
          value = faction.toString();
        } else {
          value =
              Resources.get("eresseamsgrenderer.func.faction.faction") + " (" + fid.toString()
                  + ")";
        }
      }
      // $ship(<ID>)
    } else if (name.equals("ship(")) {
      EntityID sid = renderEntityID(unparsed, attributes);
      if (sid == null) {
        value = Resources.get("eresseamsgrenderer.func.ship.unknown");
      } else {
        Ship ship = gd.getShip(sid);
        if (ship != null) {
          value = ship.toString(false);
        } else {
          value = Resources.get("eresseamsgrenderer.func.ship.ship") + " (" + sid.toString() + ")";
        }
      }
      // $building(<ID>)
    } else if (name.equals("building(")) {
      EntityID bid = renderEntityID(unparsed, attributes);
      if (bid == null) {
        value = Resources.get("eresseamsgrenderer.func.building.unknown");
      } else {
        Building building = gd.getBuilding(bid);
        if (building != null) {
          value = building.toString();
        } else {
          value =
              Resources.get("eresseamsgrenderer.func.building.building") + " (" + bid.toString()
                  + ")";
        }
      }
      // $eq(<int>,<int>)
    } else if (name.equals("eq(")) {
      int[] ar = renderInteger(unparsed, attributes);
      int pos = unparsed.indexOf(",");
      if (pos >= 0) {
        unparsed.delete(0, pos + 1);
        int[] i = new int[1];
        if (ar == null) {
          i[0] = 0;
        } else {
          int a = ar[0];
          ar = renderInteger(unparsed, attributes);
          if (ar == null) {
            i[0] = 0;
          } else {
            if (a == ar[0]) {
              i[0] = 1;
            } else {
              i[0] = 0;
            }
          }
        }
        value = i;
      } else
        throw new ParseException("wrong arguments for eq()", unparsed.length());
    } else if (name.equals("add(")) {
      int[] ar = renderInteger(unparsed, attributes);
      int pos = unparsed.indexOf(",");
      if ((pos >= 0) && (ar != null)) {
        unparsed.delete(0, pos + 1);
        int a = ar[0];
        ar = renderInteger(unparsed, attributes);
        if (ar != null) {
          int[] i = new int[1];
          i[0] = a + ar[0];
          value = i;
        } else
          throw new ParseException("second argument of add() returns NULL", unparsed.length());
      } else
        throw new ParseException("wrong arguments for add()", unparsed.length());
    } else if (name.equals("if(")) {
      int cond = renderInteger(unparsed, attributes)[0];
      int pos = unparsed.indexOf(",");
      if (pos >= 0) {
        unparsed.delete(0, pos + 1);
        String do_true = renderString(unparsed, attributes);
        pos = unparsed.indexOf(",");
        if (pos >= 0) {
          unparsed.delete(0, pos + 1);
          String do_false = renderString(unparsed, attributes);
          if (cond > 0) {
            value = do_true;
          } else {
            value = do_false;
          }
        } else
          throw new ParseException("wrong arguments for if()", unparsed.length());
      } else
        throw new ParseException("wrong arguments for if()", unparsed.length());
    } else if (name.equals("strlen(")) {
      String content = renderString(unparsed, attributes);
      int[] i = new int[1];
      if (content == null) {
        i[0] = 0;
      } else {
        i[0] = content.length();
      }
      value = i;
      // $order(<string>)
    } else if (name.equals("order(")) {
      value = renderString(unparsed, attributes);
      // $isnull(<any>)
    } else if (name.equals("isnull(")) {
      Object obj = renderNull(unparsed, attributes);
      int[] i = new int[1];
      if (obj == null) {
        i[0] = 1;
      } else {
        i[0] = 0;
      }
      value = i;
      // $resource(<string>,<int>)
    } else if (name.equals("resource(")) {
      String item = renderString(unparsed, attributes);
      int pos = unparsed.indexOf(",");
      if (pos >= 0) {
        unparsed.delete(0, pos + 1);
        renderInteger(unparsed, attributes);
        // TODO use amount(=ar[0]) if possible to spell the item names correctly
        value = gd.getTranslation(item);
      } else
        throw new ParseException("wrong arguments for resource()", unparsed.length());
    } else if (name.equals("resources(")) {
      // the string has the following style: "<amount1> <item1>, <amount2> <item2>, ..."
      // beware <item> could include spaces!
      String resparam = renderString(unparsed, attributes);
      StringBuffer res = new StringBuffer();
      if (resparam != null) {
        String[] resources = resparam.split(",");
        for (int i = 0; i < resources.length; i++) {
          String[] parts = resources[i].trim().split(" ", 2);
          if (i > 0) {
            res.append(", ");
          }
          res.append(parts[0]);
          if (parts.length == 2) {
            res.append(" ").append(gd.getTranslation(parts[1]));
          }
        }
        value = res.toString();
      } else {
        value = "";
      }
      // $localize(<string>)
      // $skill(<string>)
      // $spell(<string>)
      // $race(<string>)
      // $terrain(<string>)
    } else if (name.equals("localize(") || name.equals("skill(") || name.equals("spell(")
        || name.equals("race(") || name.equals("terrain(")) {
      String str = renderString(unparsed, attributes);
      value = gd.getTranslation(str);
      // $weight(<int>)
    } else if (name.equals("weight(")) {
      int[] ar = renderInteger(unparsed, attributes);
      if (ar != null) {
        value = (ar[0] / 100) + " GE";
      } else
        throw new ParseException(name + ") requires an int parameter != NULL", unparsed.length());
    } else if (name.equals("direction(")) {
      int[] ar = renderInteger(unparsed, attributes);
      if ((ar != null) && (ar.length > 0)) {
        if ((ar[0] >= 0) && (ar[0] <= 5)) {
          value = Resources.get("eresseamsgrenderer.func.direction." + ar[0]);
        } else
          throw new ParseException("direction() requires an int parameter between 0 and 5",
              unparsed.length());
      } else
        throw new ParseException("direction() requires an int parameter != NULL", unparsed.length());
    } else {
      value = "unknown:" + name + renderString(unparsed, attributes) + ")";
      // throw new ParseException("unknown token: "+name, unparsed.length());
    }

    // delete closing bracket before returning the value
    int close = unparsed.indexOf(")");
    if (close >= 0) {
      unparsed.delete(0, close + 1);
      return value;
    } else
      throw new ParseException("no matching bracket ')'", unparsed.length());
  }

  /**
   * Tries to render an id of a unit, faction, .... This could of course also be a return value of a
   * function or an attribute value. Calls renderInteger to obtain the value. If a null array is
   * returned or an array of zero values this method returns null.
   * 
   * @param unparsed - the rightmost part that is not parsed up to now - the method manipulates
   *          this!
   * @param attributes - the mapping of atributes to values of the message
   * @return an EntityID
   * @throws ParseException if any problem occured parsing the msg
   */

  private EntityID renderEntityID(StringBuffer unparsed, Map<String, String> attributes)
      throws ParseException {
    int[] i_ar = renderInteger(unparsed, attributes);
    if ((i_ar != null) && (i_ar.length >= 1))
      return EntityID.createEntityID(i_ar[0], gd.base);
    else
      return null;
  }

  /**
   * Tries to render an integer. This could of course also be a return value of a function or an
   * attribute value. The return of attributes and functions are treated like constants.
   * 
   * @param unparsed - the rightmost part that is not parsed up to now - the method manipulates
   *          this!
   * @param attributes - the mapping of atributes to values of the message
   * @return an Array of int
   * @throws ParseException if any problem occured parsing the msg
   */
  private int[] renderInteger(StringBuffer unparsed, Map<String, String> attributes)
      throws ParseException {
    // remove trailing spaces
    while (unparsed.charAt(0) == ' ') {
      unparsed.deleteCharAt(0);
    }
    if (unparsed.charAt(0) == '$') {
      unparsed.deleteCharAt(0);
      Object obj = renderDollar(unparsed, attributes);
      try {
        return (int[]) obj;
      } catch (ClassCastException eint) {
        try {
          unparsed.insert(0, ((String) obj).replaceAll(",", " "));
        } catch (ClassCastException estr) {
          throw new ParseException("Integer[] expected", unparsed.length());
        }
      }
    }
    final Pattern numbersPattern = Pattern.compile("-?[0-9]+( +-?[0-9]+)*");
    // parse constant integers
    Matcher m = numbersPattern.matcher(unparsed);
    if (m.find() && m.start() == 0) {
      StringTokenizer st = new StringTokenizer(m.group(), " ,");
      int[] ar = new int[st.countTokens()];
      int i = 0;
      while (st.hasMoreTokens()) {
        ar[i] = Integer.parseInt(st.nextToken());
        i++;
      }
      unparsed.delete(0, m.end());
      return ar;
    } else
      throw new ParseException("No Integer constant found", unparsed.length());
  }

  /**
   * Tries to render a constant value NULL or returns the value of an attribute or function.
   * 
   * @param unparsed - the rightmost part that is not parsed up to now - the method manipulates
   *          this!
   * @param attributes - the mapping of atributes to values of the message
   * @return something that may be NULL
   * @throws ParseException if any problem occured parsing the msg, i.e. a constant STRING or
   *           integer is found
   */

  private Object renderNull(StringBuffer unparsed, Map<String, String> attributes)
      throws ParseException {
    // remove trailing spaces
    while (unparsed.charAt(0) == ' ') {
      unparsed.deleteCharAt(0);
    }
    if (unparsed.charAt(0) == '$') {
      unparsed.deleteCharAt(0);
      return renderDollar(unparsed, attributes);
    } else if (unparsed.indexOf("NULL") == 0) {
      unparsed.delete(0, 4);
      return null;
    } else
      throw new ParseException("Attribute expected for isnull()", unparsed.length());
  }
}
