// class magellan.library.utils.Utils
// created on 02.09.2007
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
package magellan.library.utils;

import java.awt.Color;
import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import magellan.library.CoordinateID;

/**
 * Contains some useful methods...
 * 
 * @author ...
 * @version 1.0, 02.09.2007
 */
public class Utils {

  /**
   * Checks, if a Boolean is true (and not null)
   */
  public static boolean is(Boolean bool) {
    return (bool != null && bool.booleanValue());
  }

  /**
   * Parses a string into a int, if possible otherwise it returns 0.
   */
  public static int getIntValue(String value) {
    if (value == null)
      return 0;
    try {
      return Integer.parseInt(value);
    } catch (Exception exception) {
      return 0;
    }
  }

  /**
   * Parses a string into a long, if possible otherwise it returns 0d.
   */
  public static long getLongValue(String value) {
    if (value == null)
      return 0;
    try {
      return Long.parseLong(value);
    } catch (Exception exception) {
      return 0;
    }
  }

  /**
   * Returns the value of an Integer, or a default value if it is <code>null</code>.
   * 
   * @param integer
   * @param defaultValue
   * @return <code>defaultValue</code> if <code>integer==0</code>, otherwise
   *         <code>integer.intValue()</code>
   */
  public static int getIntValue(Integer integer, int defaultValue) {
    if (integer != null)
      return integer;
    return defaultValue;
  }

  /**
   * Returns the boolean value of a string.
   */
  public static boolean getBoolValue(String value, boolean defaultValue) {
    if (Utils.isEmpty(value))
      return defaultValue;
    try {
      return Boolean.parseBoolean(value);
    } catch (Exception exception) {
      return defaultValue;
    }
  }

  /**
   * @param object
   * @return <code>true</code> if object is <code>null</code>
   */
  public static boolean isEmpty(Object object) {
    return (object == null);
  }

  /**
   * @param date
   * @return <code>true</code> if date is <code>null</code> or time is 0
   */
  public static boolean isEmpty(Date date) {
    return (date == null || date.getTime() == 0);
  }

  /**
   * @param string
   * @return <code>true</code> if string is <code>null</code> or equals ""
   */
  public static boolean isEmpty(String string) {
    return (string == null || string.trim().equals(""));
  }

  /**
   * @param list
   * @return <code>true</code> if list is <code>null</code> or empty
   */
  public static boolean isEmpty(List<Element> list) {
    return (list == null || list.size() == 0);
  }

  /**
   * @param map
   * @return <code>true</code> if map is <code>null</code> or empty
   */
  public static boolean isEmpty(HashMap<?, ?> map) {
    return (map == null || map.size() == 0);
  }

  /**
   * @param list
   * @return <code>true</code> if list is <code>null</code> or empty
   */
  public static boolean isEmpty(NodeList list) {
    return (list == null || list.getLength() == 0);
  }

  /**
   * @param list
   * @return <code>true</code> if list is <code>null</code> or empty
   */
  public static boolean isEmpty(Number[] list) {
    return (list == null || list.length == 0);
  }

  /**
   * @param list
   * @return <code>true</code> if list is <code>null</code> or empty
   */
  public static boolean isEmpty(String[] list) {
    return (list == null || list.length == 0);
  }

  /**
   * Diese Methode prüft, ob eine Zahl null oder 0 ist.
   */
  public static boolean isNull(Long number) {
    if (number == null)
      return true;
    return (number.longValue() == 0l);
  }

  /**
   * Diese Methode prüft, ob eine Zahl null oder 0 ist.
   */
  public static boolean isNull(Date date) {
    if (date == null)
      return true;
    return (date.getTime() == 0l);
  }

  /**
   * Diese Methode prüft, ob eine Zahl null oder 0 ist.
   */
  public static boolean isNull(Integer number) {
    if (number == null)
      return true;
    return (number.intValue() == 0);
  }

  /**
   * Diese Methode prüft, ob eine Zahl null oder 0 ist.
   */
  public static boolean isNull(BigDecimal number) {
    return number == null || number.floatValue() == 0;
  }

  /**
   * Returns a never null string.
   */
  public static String notNullString(Object string) {
    return Utils.notNullString(string, "");
  }

  /**
   * Returns a never null string.
   */
  public static String notNullString(Object object, String nullString) {
    if (object == null)
      return nullString;
    else
      return object.toString();
  }

  /**
   * Diese Methode ersetzt in einem XML String alle &lt; und &gt; durch &amp;lt; und &amp;gt;.
   */
  public static String escapeXML(Object xmlString) {
    if (xmlString == null)
      return "";
    if (xmlString instanceof String)
      return Utils.escapeXML((String) xmlString);
    return Utils.escapeXML(xmlString.toString());
  }

  /**
   * Diese Methode ersetzt in einem XML String alle &lt; und &gt; durch &amp;lt; und &amp;gt;.
   */
  public static String escapeXML(String xmlString) {
    if (Utils.isEmpty(xmlString))
      return xmlString;
    xmlString = xmlString.replaceAll("&amp;", "&"); // kleiner Hint, um &amp; -> &amp;amp; zu
    // vermeiden.
    xmlString = xmlString.replaceAll("&", "&amp;");
    xmlString = xmlString.replaceAll("&lt;", "&amp;lt;");
    xmlString = xmlString.replaceAll("&gt;", "&amp;gt;");
    xmlString = xmlString.replaceAll("<", "&lt;");
    xmlString = xmlString.replaceAll(">", "&gt;");
    return xmlString;
  }

  /**
   * Returns all XML child nodes of a root node.
   */
  public static List<Element> getChildNodes(Element root) {
    NodeList subnodes = root.getChildNodes();
    List<Element> result = new ArrayList<Element>();
    for (int i = 0; i < subnodes.getLength(); i++) {
      Node node = subnodes.item(i);
      if (node.getNodeType() != Node.ELEMENT_NODE) {
        continue;
      }
      result.add((Element) node);
    }
    return result;
  }

  /**
   * Returns all XML child nodes of a root node with the given name
   */
  public static List<Element> getChildNodes(Element root, String name) {
    NodeList subnodes = root.getChildNodes();
    List<Element> result = new ArrayList<Element>();
    for (int i = 0; i < subnodes.getLength(); i++) {
      Node node = subnodes.item(i);
      if (node.getNodeType() != Node.ELEMENT_NODE) {
        continue;
      }
      if (!node.getNodeName().equalsIgnoreCase(name)) {
        continue;
      }
      result.add((Element) node);
    }
    return result;
  }

  /**
   * Returns the first child node of a root node with the given name
   */
  public static Element getChildNode(Element root, String name) {
    List<Element> children = Utils.getChildNodes(root, name);
    if (Utils.isEmpty(children))
      return null;
    return children.get(0);
  }

  /**
   * Returns the first child node of a root node
   */
  public static Element getChildNode(Element root) {
    NodeList subnodes = root.getChildNodes();
    for (int i = 0; i < subnodes.getLength(); i++) {
      Node node = subnodes.item(i);
      if (node.getNodeType() != Node.ELEMENT_NODE) {
        continue;
      }
      return (Element) node;
    }
    return null;
  }

  /**
   * Returns al character data inside a root element
   */
  public static String getCData(Element root) {
    if (root == null)
      return "";
    if (root.getFirstChild() == null)
      return "";
    return root.getFirstChild().getNodeValue();
  }

  /**
   * This method returns a Color object coresponding to the format: #RRGGBB oder RRGGBB
   * 
   * @return A color oder null.
   */
  public static Color getColor(String colorAsString) {
    if (Utils.isEmpty(colorAsString))
      return null;
    int red = 0;
    int green = 0;
    int blue = 0;
    if (colorAsString.startsWith("#") && colorAsString.length() == 7) {
      red = Utils.hexadecimalToDecimal(colorAsString.substring(1, 3));
      green = Utils.hexadecimalToDecimal(colorAsString.substring(3, 5));
      blue = Utils.hexadecimalToDecimal(colorAsString.substring(5, 7));
    } else if (colorAsString.length() == 6) {
      red = Utils.hexadecimalToDecimal(colorAsString.substring(0, 2));
      green = Utils.hexadecimalToDecimal(colorAsString.substring(2, 4));
      blue = Utils.hexadecimalToDecimal(colorAsString.substring(4, 6));
    } else
      return null;

    if (red < 0) {
      red = 0;
    }
    if (red > 255) {
      red = 255;
    }

    if (green < 0) {
      green = 0;
    }
    if (green > 255) {
      green = 255;
    }

    if (blue < 0) {
      blue = 0;
    }
    if (blue > 255) {
      blue = 255;
    }

    return new Color(red, green, blue);
  }

  /**
   * This method returns a String object coresponding to the format: #RRGGBB oder RRGGBB
   * 
   * @return A color oder null.
   */
  public static String getColor(Color color) {
    if (color == null)
      return null;
    int red = color.getRed();
    int green = color.getGreen();
    int blue = color.getBlue();

    if (red < 0) {
      red = 0;
    }
    if (red > 255) {
      red = 255;
    }

    if (green < 0) {
      green = 0;
    }
    if (green > 255) {
      green = 255;
    }

    if (blue < 0) {
      blue = 0;
    }
    if (blue > 255) {
      blue = 255;
    }

    String r = Utils.decimalToHexadecimal(red);
    String g = Utils.decimalToHexadecimal(green);
    String b = Utils.decimalToHexadecimal(blue);

    if (r.length() < 2) {
      r = "0" + r;
    }
    if (g.length() < 2) {
      g = "0" + g;
    }
    if (b.length() < 2) {
      b = "0" + b;
    }

    return "#" + r + g + b;
  }

  /**
   * Returns the decimal value of a hex number
   */
  public static int hexadecimalToDecimal(String hexadecimal) {
    int power = 1, decimal = 0, number = 0;
    String digit;

    for (int i = hexadecimal.length() - 1; i >= 0; i--) {
      digit = hexadecimal.toUpperCase().substring(i, i + 1);

      if (digit.equals("1")) {
        number = 1;
      } else if (digit.equals("2")) {
        number = 2;
      } else if (digit.equals("3")) {
        number = 3;
      } else if (digit.equals("4")) {
        number = 4;
      } else if (digit.equals("5")) {
        number = 5;
      } else if (digit.equals("6")) {
        number = 6;
      } else if (digit.equals("7")) {
        number = 7;
      } else if (digit.equals("8")) {
        number = 8;
      } else if (digit.equals("9")) {
        number = 9;
      } else if (digit.equals("A")) {
        number = 10;
      } else if (digit.equals("B")) {
        number = 11;
      } else if (digit.equals("C")) {
        number = 12;
      } else if (digit.equals("D")) {
        number = 13;
      } else if (digit.equals("E")) {
        number = 14;
      } else if (digit.equals("F")) {
        number = 15;
      }

      decimal = decimal + (number * power);
      number = 0;
      power *= 16;
    }

    return decimal;
  }

  private static final String hex[] = { "0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "A", "B",
      "C", "D", "E", "F" };

  /**
   * Returns the hex value of a decimal number
   */
  public static String decimalToHexadecimal(long decimal) {
    long rest;

    StringBuffer hexadecimal = new StringBuffer(10);

    long result = decimal;
    do {
      rest = result % 16;
      result = result / 16;

      hexadecimal.append(Utils.hex[(int) rest]);
    } while (result != 0);

    return hexadecimal.reverse().toString();
  }

  /**
   * Returns the date in the form "DD.MM.YYYY HH:MM:ss"
   */
  public static String toDayAndTime(Date date) {
    if (date == null)
      return "-";
    SimpleDateFormat format = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss.SSS");
    return format.format(date);
  }

  /**
   * Returns the date in the form "yyyy-MM-dd HH:mm:ss"
   */
  public static Date toDate(String date) {
    if (!Utils.isEmpty(date)) {
      try {
        SimpleDateFormat formater = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        return formater.parse(date);
      } catch (ParseException exception) {
        // do nothing
      }
    }
    return null;
  }

  /**
   * Helper class for spiralPattern
   */
  public static interface SpiralVisitor<T> {
    /**
     * @param c The current coordinate
     * @param distance The distance between center and c
     * @return true if the search should terminate
     */
    public boolean visit(CoordinateID c, int distance);

    /**
     * @return result that is returned by spiralPattern()
     */
    public T getResult();
  }

  private static int[] dxs = new int[] { -1, -1, 0, 1, 1, 0 };
  private static int[] dys = new int[] { 1, 0, -1, -1, 0, 1 };

  /**
   * Create hexagonal coordinates in a spiral pattern around center. Call visitor.visit for each
   * coordinate. Terminate when distance &gt; maxDist or visitor.visit() returns true.
   * 
   * @param center
   * @param maxDist
   * @param visitor
   */
  public static <T> T spiralPattern(CoordinateID center, int maxDist, SpiralVisitor<T> visitor) {
    if (visitor.visit(center, 0))
      return visitor.getResult();
    int z = center.getZ();

    int cx = center.getX(), cy = center.getY();
    for (int dist = 1; dist <= (maxDist > 0 ? maxDist : Integer.MAX_VALUE); ++dist) {
      int dx = dist, dy = 0;
      for (int delta = 0; delta < 6; ++delta) {
        for (int step = 0; step < dist; ++step) {
          if (visitor.visit(CoordinateID.create(cx + dx, cy + dy, z), dist))
            return visitor.getResult();
          dx += dxs[delta];
          dy += dys[delta];
        }
      }
    }
    return visitor.getResult();
  }

  /**
   * Cut a string to at most maxLength characters.
   * 
   * @param string
   * @param maxLength
   */
  public static String cutString(String string, int maxLength) {
    if (string.length() <= maxLength)
      return string;
    StringBuilder builder = new StringBuilder(maxLength);
    builder.append(string.substring(0, 80)).append("...").append(string.substring(string.length() - 17));
    return builder.toString();
  }

}
