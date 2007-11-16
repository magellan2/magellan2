// class magellan.test.WhoNeedsSwing
// created on 30.04.2007
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

/**
 * This class tries to test the  
 *
 * @author ...
 * @version 1.0, 30.04.2007
 */
public class WhoNeedsSwing {

  /**
   * @param args
   */
  public static void main(String[] args) throws Exception {
    verify(new File("src-backend/"));
    
  }
  
  private static void verify(File root) throws Exception {
    if (root.isDirectory()) {
      File[] files = root.listFiles();
      for (File file : files) {
        if (file.getName().startsWith(".")) continue;
        verify(file);
      }
    } else if (root.getName().endsWith(".java")) {
      LineNumberReader reader = new LineNumberReader(new FileReader(root));
      String line;
      while ((line = reader.readLine()) != null) {
        if (line.trim().startsWith("import ")) {
          if (line.contains("java.awt.") || line.contains("javax.swing")) {
            System.out.println(root.getCanonicalPath());
            break;
          }
        }
      }
      reader.close();
    }
    
  }

}
