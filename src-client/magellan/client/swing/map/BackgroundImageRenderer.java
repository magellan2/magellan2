// class magellan.client.swing.map.BackgroundRenderer
// created on 17.12.2007
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
package magellan.client.swing.map;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JPanel;

import magellan.client.MagellanContext;
import magellan.client.swing.preferences.PreferencesAdapter;
import magellan.library.utils.Resources;

/**
 * Renders a background image behind the regions.
 *
 * @author darcduck
 * @version 1.0, 17.12.2007
 */
public class BackgroundImageRenderer extends ImageCellRenderer {
  private Image img = null;
  private int imgWidth = -1;
  private int imgHeight = -1;
  private String filename = null;

  /**
   * @param geo
   * @param context
   */
  public BackgroundImageRenderer(CellGeometry geo, MagellanContext context) {
    super(geo, context);
    this.filename =
        context.getProperties().getProperty("map.backgroundimagerenderer.image", "background");
    setImage(loadImage(this.filename));
  }

  /**
   * @see magellan.client.swing.map.HexCellRenderer#getName()
   */
  @Override
  public String getName() {
    return Resources.get("map.backgroundrenderer.name");
  }

  protected Image loadImage(String filename) {
    
    ImageIcon icon = null;
    if ((new File(filename)).exists()){
      icon = this.context.getImageFactory().loadImage(filename);
    }
    if (icon == null) {
      icon = this.context.getImageFactory().loadImage("etc/images/map/"+filename);
    }
    if (icon != null) {
      return icon.getImage();
    }
    return null;
  }
  
  protected void setImage(Image img) {
    this.img = img;
    this.imgWidth = this.img.getWidth(null);
    this.imgHeight = this.img.getHeight(null);
    // TODO repaint!?
  }
  
  /**
   * @see magellan.client.swing.map.HexCellRenderer#getPlaneIndex()
   */
  @Override
  public int getPlaneIndex() {
    return Mapper.PLANE_BACKGROUND;
  }

  /**
   * @see magellan.client.swing.map.HexCellRenderer#render(java.lang.Object, boolean, boolean)
   */
  @Override
  public void render(Object obj, boolean active, boolean selected) {
    if (img != null && imgWidth>0 && imgHeight>0) { 
      int x1 = (int) Math.ceil(offset.x/imgWidth)-1;
      int x2 = (int) Math.ceil((offset.x+offset.width)/imgWidth);   
      int y1 = (int) Math.ceil(offset.y/imgHeight)-1;   
      int y2 = (int) Math.ceil((offset.y+offset.height)/imgHeight);
  
      for (int x=x1; x<=x2; x++) {
        for (int y=y1; y<=y2; y++) {
          graphics.drawImage(img, -offset.x+x*imgWidth, -offset.y+y*imgHeight, null);
        }
      }
    }
  }

  @Override
  public Image scale(Image img) {
    return img;
  }

  @Override
  public void scale(float scaleFactor) {
    // do nothing
  }

  @Override
  public PreferencesAdapter getPreferencesAdapter() {
    return new BackgroundImagePreferences(this);
  }

  /**
   * 
   * This is the inner class used for the preferences
   *
   * @author darcduck
   * @version 1.0, 20.12.2007
   */
  
  protected class BackgroundImagePreferences extends JPanel implements PreferencesAdapter, ActionListener {
    protected BackgroundImageRenderer source = null;
    private Image img = null; 
    private String filename = null;
    private PreferencesImagePanel imgPanel = new PreferencesImagePanel();

    /**
     * Creates a new BackgroundImagePreferences object.
     */
    protected BackgroundImagePreferences(MapCellRenderer source) {
      super(new BorderLayout());
      this.source = (BackgroundImageRenderer)source;
      this.filename = BackgroundImageRenderer.this.filename;
      this.img = BackgroundImageRenderer.this.img;
      this.imgPanel.setImage(this.img);

      JPanel jpbtn = new JPanel();
      JButton btnopen = new JButton(Resources.get("map.backgroundimagerenderer.prefs.open"));
      btnopen.addActionListener(this);
      jpbtn.add(btnopen);
      this.add(jpbtn, BorderLayout.NORTH);
      this.add(this.imgPanel, BorderLayout.CENTER);
    }
    
    public void initPreferences() {
        // TODO: implement it
    }

    /**
     * DOCUMENT-ME
     */
    public void applyPreferences() {
      BackgroundImageRenderer.this.setImage(this.img);
      if (this.filename != null) {
        BackgroundImageRenderer.this.context.getProperties().setProperty("map.backgroundimagerenderer.image", this.filename);  
      }
    }

    /**
     * DOCUMENT-ME
     */
    public Component getComponent() {
      return this;
    }

    /**
     * DOCUMENT-ME
     */
    public String getTitle() {
      return getName();
    }
    
    public void actionPerformed(ActionEvent e) {
      File folder = magellan.client.Client.getMagellanDirectory();
      if (this.filename != null) {
        folder = (new File(this.filename)).getParentFile();
      }
      JFileChooser jfc = new JFileChooser(folder);
      int ret = jfc.showOpenDialog(null);

      if(ret == JFileChooser.APPROVE_OPTION) {
        java.io.File f = jfc.getSelectedFile();
        this.filename = f.getAbsolutePath();
        this.img = BackgroundImageRenderer.this.loadImage(this.filename);
        this.imgPanel.setImage(this.img);
      }  
      validate();
      repaint();
    }

    /**
     * 
     * The JPanel where the image is pained centered.
     *
     * @author darcduck
     * @version 1.0, 20.12.2007
     */
    protected class PreferencesImagePanel extends JPanel {
      private Image img = null;
      
      protected PreferencesImagePanel() {
      }
      
      protected void setImage(Image img) {
        this.img = img;
      }
      
      @Override
      public void paintComponent(Graphics g) {
        if (img != null) {
          Dimension size = getSize();
          int width = img.getWidth(null);
          int height = img.getHeight(null);
          g.setColor(getBackground());
          g.fillRect(0, 0, size.width, size.height);
          g.drawImage(img, (size.width-width)/2, (size.height-height)/2, null);
        }
      }

      /**
       * @see javax.swing.JComponent#getPreferredSize()
       */
      @Override
      public Dimension getPreferredSize() {
        if (img != null) {
          return new Dimension(img.getWidth(null),img.getHeight(null));
        }
        return super.getPreferredSize();
      }

    }
  }
}
