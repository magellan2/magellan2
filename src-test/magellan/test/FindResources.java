// class magellan.test.FindResources
// created on 28.05.2007
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
package magellan.test;

import java.io.File;
import java.io.FileReader;
import java.io.LineNumberReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.Locale;

import magellan.library.utils.Resources;

/**
 * This class finds all resource keys used in this
 * application. They are identified by the String
 * "Resources.get("
 *
 * @author ...
 * @version 1.0, 28.05.2007
 */
public class FindResources {
  private static final String KEY = "Resources.get(";
  
  private static List<String> resourceKeys = new ArrayList<String>();

  /**
   * @param args
   */
  public static void main(String[] args) throws Exception {
    System.out.println("----start----");
    findResources(new File("."));
    Collections.sort(resourceKeys);
    System.out.println("----end----");
    
    Enumeration<String> keys = Resources.getInstance().getKeys(Locale.GERMANY);
    
    // okay, we could do this in ONE loop but I want to sort them by
    // priority...
    System.out.println("----okay----");
    
    while (keys.hasMoreElements()) {
      String key = keys.nextElement();
      boolean found = false;
      for (String v : resourceKeys) {
        if (key.equals(v)) {
          System.out.println("[green]  "+key);
          found = true;
          break;
        }
      }
    }
    
    System.out.println("----maybe----");
    while (keys.hasMoreElements()) {
      String key = keys.nextElement();
      boolean found = false;
      for (String v : resourceKeys) {
        if (key.startsWith(v)) {
          System.out.println("[yellow] "+key + " (vs. "+v+")");
          found = true;
          break;
        }
      }
      if (!found) {
        System.out.println("[red]    "+key);
      }
    }
    
    System.out.println("----unused----");
    while (keys.hasMoreElements()) {
      String key = keys.nextElement();
      boolean found = false;
      for (String v : resourceKeys) {
        if (key.equals(v) || key.startsWith(v)) {
          found = true;
          break;
        }
      }
      if (!found) {
        System.out.println("[red]    "+key);
      }
    }
  }
  
  
  public static void findResources(File root) throws Exception {
    if (root.isDirectory()) {
      File[] files = root.listFiles();
      for (File file : files) {
        findResources(file);
      }
    } else {
      if (root.getName().equals("FindResources.java")) return;
      if (root.getName().endsWith(".java")) {
        scanFile(root);
      }
    }
  }

  public static void scanFile(File file) throws Exception {
    FileReader fr = new FileReader(file);
    LineNumberReader reader = new LineNumberReader(fr);
    String line;
    while ((line = reader.readLine()) != null) {
      if (line.indexOf(KEY)>=0) {
        // Found call... now extract the information...
        extractResourceKey(line);
      }
    }
  }
  
  public static String extractResourceKey(String line) {
    
    int index = line.indexOf(KEY);
    
    if (line.indexOf(KEY,index+KEY.length())>0) {
      extractResourceKey(line.substring(line.indexOf(KEY,index+KEY.length())));
    }
    
    line = line.substring(index+KEY.length());
    
    if (line.indexOf(")")>=0) line = line.substring(0,line.indexOf(")"));
    
    line = line.trim();
    if (line.startsWith("\"")) line = line.substring(1,line.indexOf("\"",2));
    
    if (!resourceKeys.contains(line)) {
      resourceKeys.add(line);
    }
    
    return line;
  }
}
