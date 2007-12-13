/*
 *  Copyright (C) 2000-2004 Roger Butenuth, Andreas Gampe,
 *                          Stefan Goetz, Sebastian Pappert,
 *                          Klaas Prause, Enno Rehling,
 *                          Sebastian Tusk, Ulrich Kuester,
 *                          Ilja Pavkovic
 *
 * This file is part of the Eressea Java Code Base, see the
 * file LICENSING for the licensing information applying to
 * this file.
 *
 */

package magellan.client.swing.map;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GraphicsEnvironment;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Image;
import java.awt.Point;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Collection;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.swing.JCheckBox;
import javax.swing.JColorChooser;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;

import magellan.client.MagellanContext;
import magellan.client.swing.map.SignTextCellRenderer.Preferences;
import magellan.client.swing.preferences.PreferencesAdapter;
import magellan.library.Border;
import magellan.library.CoordinateID;
import magellan.library.Region;
import magellan.library.rules.Date;
import magellan.library.utils.Resources;
import magellan.library.utils.logging.Logger;


/**
 * DOCUMENT-ME
 *
 * @author $Author: $
 * @version $Revision: 299 $
 */
public class BorderCellRenderer extends ImageCellRenderer {
  private static final Logger log = Logger.getInstance(BorderCellRenderer.class);
  
  
  // for border specific region images
  int[] bitMaskArray = {1,2,4,8,16,32,64,128};
  
  // the border type names we can handle in renderOtherBorders
  String[] borderTypes = {"FEUERWAND","IRRLICHTER"};
  
  
  private boolean useSeasonImages = true;
  
  /**
	 * Creates a new BorderCellRenderer object.
	 *
	 * 
	 * 
	 */
	public BorderCellRenderer(CellGeometry geo, MagellanContext context ) {
		super(geo, context);
    setUseSeasonImages((Boolean.valueOf(settings.getProperty("BoderCellRenderer.useSeasonImages",
     "true"))).booleanValue());
    
	}

	/**
	 * DOCUMENT-ME
	 *
	 * 
	 * 
	 * 
	 */
	public void render(Object obj, boolean active, boolean selected) {
		if(obj instanceof Region) {
			Region r = (Region) obj;
			Collection borders = r.borders();

			if(borders.isEmpty() == false) {
				// since border objects are rare initialization is
				// done as late as possible
				CoordinateID c = null;
				Point pos = null;
				Dimension size = null;

				for(Iterator iter = r.borders().iterator(); iter.hasNext();) {
					Border b = (Border) iter.next();

					if(magellan.library.utils.Umlaut.normalize(b.getType()).equals("STRASSE") &&
						   (b.getDirection() != magellan.library.utils.Direction.DIR_INVALID)) {
						Image img = (b.getBuildRatio() == 100) ? getImage("Strasse" + b.getDirection())
														  : getImage("Strasse_incomplete" +
																	 b.getDirection());

						if(img == null) {
							img = getImage("Strasse" + b.getDirection());
						}

						if(img != null) {
							if(c == null) {
								c = r.getCoordinate();
								pos = new Point(cellGeo.getImagePosition(c.x, c.y));
								pos.translate(-offset.x, -offset.y);
								size = cellGeo.getImageSize();
							}

							graphics.drawImage(img, pos.x, pos.y, size.width, size.height, null);
						}
					} else {
					  renderOtherBorders(b,r);
          }
          
				}
			}
			renderCoastals(r);
		}
	}

  
  /**
   * deals with more (classic) borders: firewalls and "Irrlichter"
   * no prozent
   * @param b the border to check
   * @param r region we are in
   */
	private void renderOtherBorders(Border b, Region r){
    CoordinateID c = null;
    Point pos = null;
    Dimension size = null;
	  // size of array of type names
    int bMax = this.borderTypes.length;
    // loop throuh them
    for (int i=0;i<bMax;i++){
      String actBorderTypeName = this.borderTypes[i];
      if(magellan.library.utils.Umlaut.normalize(b.getType()).equals(actBorderTypeName) &&
           (b.getDirection() != magellan.library.utils.Direction.DIR_INVALID)) {
        Image img = getImage("" + actBorderTypeName.toLowerCase() + b.getDirection());
        if(img != null) {
          if(c == null) {
            c = r.getCoordinate();
            pos = new Point(cellGeo.getImagePosition(c.x, c.y));
            pos.translate(-offset.x, -offset.y);
            size = cellGeo.getImageSize();
          }
          graphics.drawImage(img, pos.x, pos.y, size.width, size.height, null);
        }
      }
    }
  }
  
  
  
	/**
	 * Adds some coastal things to ocean regions
	 * depending on season and direction of non-ocean
	 * @param r
	 */
	private void renderCoastals(Region r){
	  if (!r.getRegionType().isOcean()){
	    // no ocean
	    return;
	  }
	  if (r.getCoastBitMap()==null){
	    // no coast
	    return;
	  }
	  
	  // lets look for the directions
	  int bitArray = r.getCoastBitMap().intValue();
	  boolean borderAdded = false;
	  for (int i = 0; i<6;i++){
	    if ((bitArray & bitMaskArray[i])>0){
	      // hit
	      borderAdded=true;
	      String imageNameDefault = "ocean_coast_" + i;
	      drawMyImage(imageNameDefault, r);
	    }
	  }
	  if (!borderAdded){
	    // Integervalue != null and no border added->
	    // we have an nocoast-region with pattern
	    // from bit 7 and 8 we get our random number back
	    int erg=0;
	    if ((bitArray & bitMaskArray[7])>0){
	      erg++;
	    }
	    if ((bitArray & bitMaskArray[6])>0){
        erg+=2;
      }
	    String imageNameDefault = "ocean_nocoast_" + erg;
	    drawMyImage(imageNameDefault, r);
      
	  }
	}
	
  
  
  
  
  
	private void drawMyImage(String imageNameDefault, Region r){
	  CoordinateID c = null;
    Point pos = null;
    Dimension size = null;
	  String imageName = imageNameDefault;
    Image img = null;
    // first try a season specific icon, if preferences say so!
    if (r.getData().getDate() != null && this.useSeasonImages) {
      switch (r.getData().getDate().getSeason()) {
        case Date.SPRING: imageName+="_spring"; break;
        case Date.SUMMER: imageName+="_summer"; break;
        case Date.AUTUMN: imageName+="_autumn"; break;
        case Date.WINTER: imageName+="_winter"; break;
      }
    }
    img = getImage(imageName);
    
    // if we cannot find it, try a default icon.
    if (img == null) {
      imageName = imageNameDefault; 
      img = getImage(imageName);
    }
    if(img != null) {
      c = r.getCoordinate();
      pos = new Point(cellGeo.getImagePosition(c.x, c.y));
      pos.translate(-offset.x, -offset.y);
      size = cellGeo.getImageSize();
      graphics.drawImage(img, pos.x, pos.y, size.width, size.height, null);
    } else {
      // log.warn("RegionImageCellRenderer.render(): image is null (" + imageName + ")");
    }
	}
	
	/**
	 * DOCUMENT-ME
	 *
	 * 
	 */
	public int getPlaneIndex() {
		return Mapper.PLANE_BORDER;
	}

	// pavkovic 2003.01.28: this is a Map of the default Translations mapped to this class
	// it is called by reflection (we could force the implementation of an interface,
	// this way it is more flexible.)
	// Pls use this mechanism, so the translation files can be created automagically
	// by inspecting all classes.
	private static Map<String,String> defaultTranslations;

	/**
	 * DOCUMENT-ME
	 *
	 * 
	 */
	public static synchronized Map<String,String> getDefaultTranslations() {
		if(defaultTranslations == null) {
			defaultTranslations = new Hashtable<String, String>();
			defaultTranslations.put("name", "Road renderer");
		}

		return defaultTranslations;
	}



  /**
   * @see magellan.client.swing.map.HexCellRenderer#getName()
   */
  @Override
  public String getName() {
    return Resources.get("map.bordercellrenderer.name");
  }

  
  protected class Preferences extends JPanel implements PreferencesAdapter {
    // The source component to configure
    protected BorderCellRenderer source = null;
    private final Logger log = Logger.getInstance(Preferences.class);

    // GUI elements

    private JCheckBox chkUseSeasonImages = null;
    /**
     * Creates a new Preferences object.
     *
     * 
     */
    public Preferences(BorderCellRenderer r) {
      this.source = r;
      init();
    }

    private void init() {
      
      chkUseSeasonImages = new JCheckBox(Resources.get("map.bordercellrenderer.useseasonimages"), source.isUseSeasonImages());

      this.setLayout(new GridBagLayout());

      GridBagConstraints c = new GridBagConstraints();
      c.anchor = GridBagConstraints.WEST;
      c.gridx = 0;
      c.gridy = 0;
      this.add(chkUseSeasonImages, c);
      
    }

    public void initPreferences() {
        // TODO: implement it
    }

    /**
     * DOCUMENT-ME
     */
    public void applyPreferences() {
      source.setUseSeasonImages(chkUseSeasonImages.isSelected());
    }

    /**
     * DOCUMENT-ME
     *
     * 
     */
    public Component getComponent() {
      return this;
    }

    /**
     * DOCUMENT-ME
     *
     * 
     */
    public String getTitle() {
      return source.getName();
    }
  }


  public boolean isUseSeasonImages() {
    return useSeasonImages;
  }

  public void setUseSeasonImages(boolean useSeasonImages) {
    this.useSeasonImages = useSeasonImages;
    settings.setProperty("BoderCellRenderer.useSeasonImages",
         String.valueOf(useSeasonImages));
  }
  
  public PreferencesAdapter getPreferencesAdapter() {
    return new Preferences(this);
  }
  
}
