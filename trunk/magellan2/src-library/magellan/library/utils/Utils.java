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
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * Contains some useful methods...
 *
 * @author ...
 * @version 1.0, 02.09.2007
 */
public class Utils {

  public static boolean is(Boolean bool) {
    return (bool!=null && bool.booleanValue());
  }
  
  public static int getIntValue(String value) {
    if (value == null) return 0;
    try {
      return Integer.parseInt(value);
    } catch (Exception exception) {
      return 0;
    }
  }
  
  public static boolean getBoolValue(String value, boolean defaultValue) {
    if (value == null) return defaultValue;
    try {
      return Boolean.parseBoolean(value);
    } catch (Exception exception) {
      return defaultValue;
    }
  }
  
  public static boolean isEmpty(Object object) {
    return (object==null);
  }
  
  public static boolean isEmpty(Date date) {
    return (date==null || date.getTime()==0);
  }

  public static boolean isEmpty(String string) {
    return (string==null || string.trim().equals(""));
  }
  
  public static boolean isEmpty(List list) {
    return (list==null || list.size()==0);
  }
  
  public static boolean isEmpty(HashMap hashMap) {
    return (hashMap==null || hashMap.size()==0);
  }
  
  public static boolean isEmpty(NodeList list) {
    return (list==null || list.getLength()==0);
  }
  
  public static boolean isEmpty(Number[] list) {
    return (list==null || list.length==0);
  }
  
  public static boolean isEmpty(String[] list) {
    return (list==null || list.length==0);
  }
    
  /**
   * Diese Methode prüft, ob eine Zahl null oder 0 ist.
   */
  public static boolean isNull(Long number) {
    if(number == null) return true;
    return (number.longValue() == 0l);
  }

  /**
   * Diese Methode prüft, ob eine Zahl null oder 0 ist.
   */
  public static boolean isNull(Date date) {
    if (date == null) return true;
    return (date.getTime() == 0l);
  }
  /**
   * Diese Methode prüft, ob eine Zahl null oder 0 ist.
   */
  public static boolean isNull(Integer number) {
    if (number == null) return true;
    return (number.intValue() == 0);
  }  
  /**
   * Diese Methode prüft, ob eine Zahl null oder 0 ist.
   */
  public static boolean isNull(BigDecimal number) {
    return number == null || number.floatValue() == 0;
  }
  
  public static String notNullString(Object string) {
    return notNullString(string, "");
  }
  
  public static String notNullString(Object object, String nullString) {
    if (object==null) return nullString; else return object.toString();
  }
  
  public static boolean equals(String a, String b) {
    if (a==null && b!=null) return false;
    if (b==null && a!=null) return false;
    if (a==null && b==null) return false;
    return a.equals(b);
  }

  /**
   * Diese Methode ersetzt in einem XML String alle &lt; und &gt; durch
   * &amp;lt; und &amp;gt;.
   */
  public static String escapeXML(Object xmlString) {
    if (xmlString == null) return "";
    if (xmlString instanceof String) return escapeXML((String)xmlString);
    return escapeXML(xmlString.toString());
  }
  /**
   * Diese Methode ersetzt in einem XML String alle &lt; und &gt; durch
   * &amp;lt; und &amp;gt;.
   */
  public static String escapeXML(String xmlString) {
    if (isEmpty(xmlString)) return xmlString;
    xmlString = xmlString.replaceAll("&amp;","&"); // kleiner Hint, um &amp; -> &amp;amp; zu vermeiden.
    xmlString = xmlString.replaceAll("&","&amp;");
    xmlString = xmlString.replaceAll("&lt;","&amp;lt;");
    xmlString = xmlString.replaceAll("&gt;","&amp;gt;");
    xmlString = xmlString.replaceAll("<","&lt;");
    xmlString = xmlString.replaceAll(">","&gt;");
    return xmlString;
  }
  
  public static List<Element> getChildNodes(Element root) {
    NodeList subnodes = root.getChildNodes();
    List<Element> result = new ArrayList<Element>();
    for (int i=0; i<subnodes.getLength(); i++) {
      Node node = subnodes.item(i);
      if (node.getNodeType() != Node.ELEMENT_NODE) continue;
      result.add((Element)node);
    }
    return result;
  }
  public static List<Element> getChildNodes(Element root, String name) {
    NodeList subnodes = root.getChildNodes();
    List<Element> result = new ArrayList<Element>();
    for (int i=0; i<subnodes.getLength(); i++) {
      Node node = subnodes.item(i);
      if (node.getNodeType() != Node.ELEMENT_NODE) continue;
      if (!node.getNodeName().equalsIgnoreCase(name)) continue;
      result.add((Element)node);
    }
    return result;
  }
  public static Element getChildNode(Element root) {
    NodeList subnodes = root.getChildNodes();
    for (int i=0; i<subnodes.getLength(); i++) {
      Node node = subnodes.item(i);
      if (node.getNodeType() != Node.ELEMENT_NODE) continue;
      return (Element)node;
    }
    return null;
  }
  
  /**
   * This method returns a Color object coresponding
   * to the format: #RRGGBB oder RRGGBB
   * 
   * @return A color oder null.
   */
  public static Color getColor(String colorAsString) {
    if (isEmpty(colorAsString)) return null;
    int red   = 0;
    int green = 0;
    int blue  = 0;
    if (colorAsString.startsWith("#") &&  colorAsString.length() == 7) {
      red   = hexadecimalToDecimal(colorAsString.substring(1,3));
      green = hexadecimalToDecimal(colorAsString.substring(3,5));
      blue  = hexadecimalToDecimal(colorAsString.substring(5,7));
    } else if (colorAsString.length() == 6) {
      red   = hexadecimalToDecimal(colorAsString.substring(0,2));
      green = hexadecimalToDecimal(colorAsString.substring(2,4));
      blue  = hexadecimalToDecimal(colorAsString.substring(4,6));
    } else {
      return null;
    }
    
    if (red < 0) red = 0;
    if (red > 255) red = 255;
    
    if (green < 0) green = 0;
    if (green > 255) green = 255;

    if (blue < 0) blue = 0;
    if (blue > 255) blue = 255;
    
    return new Color(red,green,blue);
  }

  public static int hexadecimalToDecimal(String hexadecimal) {
    int power=1, decimal=0, number=0;
    String digit;
    
    
    
    for(int i=hexadecimal.length()-1; i>=0; i--) {
      digit = hexadecimal.toUpperCase().substring(i,i+1);

      if(digit.equals("1"))      number=1;
      else if(digit.equals("2")) number=2;
      else if(digit.equals("3")) number=3;
      else if(digit.equals("4")) number=4;
      else if(digit.equals("5")) number=5;
      else if(digit.equals("6")) number=6;
      else if(digit.equals("7")) number=7;
      else if(digit.equals("8")) number=8;
      else if(digit.equals("9")) number=9;
      else if(digit.equals("A")) number=10;
      else if(digit.equals("B")) number=11;
      else if(digit.equals("C")) number=12;
      else if(digit.equals("D")) number=13;
      else if(digit.equals("E")) number=14;
      else if(digit.equals("F")) number=15;
         
      decimal = decimal+(number*power);
      number = 0;
      power*=16;
    }
    
    return decimal;
  }
}
