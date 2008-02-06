// class magellan.client.swing.map.BuildingCellRendererPreferences
// created on 06.02.2008
//
// Copyright 2003-2008 by magellan project team
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
package magellan.client.swing.map;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;

import javax.swing.JPanel;
import javax.swing.JTabbedPane;

import magellan.client.Client;
import magellan.client.swing.InternationalizedPanel;
import magellan.client.swing.preferences.PreferencesAdapter;
import magellan.client.utils.ErrorWindow;
import magellan.library.utils.Resources;

public class BuildingCellRendererPreferences extends InternationalizedPanel implements PreferencesAdapter
{
  private BuildingCellRenderer source=null;
  private JPanel testPanel = null;
  
  public BuildingCellRendererPreferences(BuildingCellRenderer m){
    this.source = m;
    this.init();
  }
  
  
  private void init(){
    // the tab
    JTabbedPane tabbed = new JTabbedPane();
    
    // set - management
    JPanel manage = new JPanel();
    
    
    // edit the actual set
    JPanel edit = new JPanel();
    
    // test the actual set
    JPanel test = new JPanel(new BorderLayout());
    testPanel = new singleRegionDisplay();
    test.add(testPanel,BorderLayout.WEST);
    test.revalidate();
    
    // together
    tabbed.addTab("manage",manage);
    tabbed.addTab("edit",edit);
    tabbed.addTab("test",test);

    this.add(tabbed);
    
  }
  
  public Component getComponent() {
    return this;
  }
  
  public void initPreferences() {
    
  }
  
  public void applyPreferences() {
    
  }
  
  public String getTitle() {
    return Resources.get("map.buildingpreferences.title");
  }
  
  /**
   * 
   * TODO This class must be commented
   *
   * @author ...
   * @version 1.0, 06.02.2008
   */
  protected class singleRegionDisplay extends JPanel{
    public singleRegionDisplay(){
      super();
      init();
    }
    private void init(){
      this.setSize(240,240);
    }
    
    protected void paintComponent(Graphics g) {
      try {
        paintMapperComponent(g);
      } catch (Throwable t) {
        ErrorWindow errorWindow = new ErrorWindow(Client.INSTANCE, ErrorWindow.UNKNOWN_ERROR_MESSAGE, "", t);
        errorWindow.setShutdownOnCancel(true);
        errorWindow.setVisible(true);
      }
    }
    
    
    protected void paintMapperComponent(Graphics g) {
      
      g.setColor(Color.BLACK);
      g.fillRect(0, 0, 240, 240);
    }
  }
  
}
