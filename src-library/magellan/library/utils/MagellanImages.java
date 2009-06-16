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

public class MagellanImages {
  public static ImageIcon GUI_TOTD = null;
  public static  ImageIcon GUI_CREATETEMPUNIT = null;
  public static  ImageIcon GUI_DELETETEMPUNIT = null;
  
  public static  ImageIcon ABOUNT_APPLICATION_ICON = null;
  public static  ImageIcon ABOUT_MAGELLAN = null;
  public static  ImageIcon BULLETS_LEAF = null;
  public static  ImageIcon BULLETS_OPEN = null;
  public static  ImageIcon BULLETS_CLOSED = null;
   
  private static File magellanDirectory = null;
  
  public static URL getResource(String path) {
    try {
      File resFile = new File(path);
      if (!resFile.exists()){
        // try to use MagDir
        resFile = new File(MagellanImages.magellanDirectory,path);
        if (!resFile.exists()){
          // OK give up here
          return null;
        }
      }
      return resFile.toURI().toURL();
    } catch (MalformedURLException exception) {
      return null;
    }
  }
  
  public static ImageIcon getImageIcon(String path) {
    // log.info("getImageIcon: " + path);
    File file = new File(path);
    if (!file.exists()){
      file = new File(MagellanImages.magellanDirectory,path);
      // log.info("trying also: " + file.toString());
    }
    if (!file.exists()) {
      return null;
    }
    
    ImageIcon res = new ImageIcon(file.toString());
    return res;
  }

  /**
   * Sets the value of magellanDirectory.
   *
   * @param magellanDirectory The value for magellanDirectory.
   */
  public static void setMagellanDirectory(File magellanDirectory) {
    MagellanImages.magellanDirectory = magellanDirectory;
    MagellanImages.GUI_TOTD = MagellanImages.getImageIcon("etc/images/gui/totd.gif");
    MagellanImages.GUI_CREATETEMPUNIT = MagellanImages.getImageIcon("etc/images/gui/createtempunit.gif");
    MagellanImages.GUI_DELETETEMPUNIT = MagellanImages.getImageIcon("etc/images/gui/deletetempunit.gif");
    
    MagellanImages.ABOUNT_APPLICATION_ICON = MagellanImages.getImageIcon("etc/images/about/appicon.gif");
    MagellanImages.ABOUT_MAGELLAN = MagellanImages.getImageIcon("etc/images/about/magellan.jpg");
    MagellanImages.BULLETS_LEAF = MagellanImages.getImageIcon("etc/images/bullets/leaf.gif");
    MagellanImages.BULLETS_OPEN = MagellanImages.getImageIcon("etc/images/bullets/open.gif");
    MagellanImages.BULLETS_CLOSED = MagellanImages.getImageIcon("etc/images/bullets/closed.gif");    
    
  }
}