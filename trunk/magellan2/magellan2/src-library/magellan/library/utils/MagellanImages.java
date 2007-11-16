// class magellan.library.utils.MagellanImages
// created on 29.04.2007
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

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;

import javax.swing.ImageIcon;

import magellan.library.utils.logging.Logger;

public class MagellanImages {
  private static final Logger log = Logger.getInstance(MagellanImages.class);
  
  public static final ImageIcon GUI_TOTD = new ImageIcon("etc/images/gui/totd.gif");
  public static final ImageIcon GUI_CREATETEMPUNIT = new ImageIcon("etc/images/gui/createtempunit.gif");
  public static final ImageIcon GUI_DELETETEMPUNIT = new ImageIcon("etc/images/gui/deletetempunit.gif");
  
  public static final ImageIcon ABOUNT_APPLICATION_ICON = new ImageIcon("etc/images/about/appicon.gif");
  public static final ImageIcon ABOUT_MAGELLAN = new ImageIcon("etc/images/about/magellan.gif");
  public static final ImageIcon BULLETS_LEAF = new ImageIcon("etc/images/bullets/leaf.gif");
  public static final ImageIcon BULLETS_OPEN = new ImageIcon("etc/images/bullets/open.gif");
  public static final ImageIcon BULLETS_CLOSED = new ImageIcon("etc/images/bullets/closed.gif");
   
  
  public static URL getResource(String path) {
    try {
      return new File(path).toURI().toURL();
    } catch (MalformedURLException exception) {
      return null;
    }
  }
  
  public static ImageIcon getImageIcon(String path) {
    File file = new File(path);
    if (!file.exists()) return null;
    return new ImageIcon(path);
  }
}
