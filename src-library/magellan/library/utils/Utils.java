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
}
