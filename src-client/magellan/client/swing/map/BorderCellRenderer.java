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

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Image;
import java.awt.Point;
import java.util.Collection;

import javax.swing.JPanel;

import magellan.client.MagellanContext;
import magellan.client.swing.preferences.PreferencesAdapter;
import magellan.library.Border;
import magellan.library.CoordinateID;
import magellan.library.Region;
import magellan.library.rules.Date;
import magellan.library.utils.PropertiesHelper;
import magellan.library.utils.Resources;


/**
 * This renderer renders borders, like streets, coasts and the like.
 *
 */
public class BorderCellRenderer extends ImageCellRenderer {
  private static int noRandomImages = -1;
  
  // for border specific region images
  int[] bitMaskArray = {1,2,4,8,16,32,64,128};
  
  // the border type names we can handle in renderOtherBorders
  String[] borderTypes = {"FEUERWAND","IRRLICHTER"};

  
  /**
	 * Creates a new BorderCellRenderer object.
	 *
	 * 
	 * 
	 */
	public BorderCellRenderer(CellGeometry geo, MagellanContext context ) {
		super(geo, context);
	}

	/**
	 * Renders all borders (streets, firewalls, coasts and others.
	 * 
	 * @see magellan.client.swing.map.HexCellRenderer#render(java.lang.Object, boolean, boolean)
	 */
	@Override
  public void render(Object obj, boolean active, boolean selected) {
		if(obj instanceof Region) {
			Region r = (Region) obj;
			Collection<Border> borders = r.borders();

			if(!borders.isEmpty()) {
				// since border objects are rare initialization is
				// done as late as possible
				Point pos = null;
				Dimension size = null;
		    {
		      CoordinateID c = r.getCoordinate();
		      pos = new Point(cellGeo.getImagePosition(c.x, c.y));
		      pos.translate(-offset.x, -offset.y);
		      size = cellGeo.getImageSize();
		    }

				for(Border b : r.borders()) {
					if(magellan.library.utils.Umlaut.normalize(b.getType()).equals("STRASSE") &&
						   (b.getDirection() != magellan.library.utils.Direction.DIR_INVALID)) {
						Image img = (b.getBuildRatio() == 100) ? getImage("Strasse" + b.getDirection())
														  : getImage("Strasse_incomplete" +
																	 b.getDirection());

						if(img == null) {
							img = getImage("Strasse" + b.getDirection());
						}

						if(img != null) {
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
   * Deals with more (classic) borders without percentage in {@link #borderTypes}.
   * 
   * @param b the border to check
   * @param r region we are in
   */
  private void renderOtherBorders(Border b, Region r) {
    Point pos = null;
    Dimension size = null;
    {
      CoordinateID c = r.getCoordinate();
      pos = new Point(cellGeo.getImagePosition(c.x, c.y));
      pos.translate(-offset.x, -offset.y);
      size = cellGeo.getImageSize();
    }
    
    // size of array of type names
    int bMax = this.borderTypes.length;
    // loop through them
    for (int i = 0; i < bMax; i++) {
      String actBorderTypeName = this.borderTypes[i];
      if (magellan.library.utils.Umlaut.normalize(b.getType()).equals(actBorderTypeName)
          && (b.getDirection() != magellan.library.utils.Direction.DIR_INVALID)) {
        Image img = getImage("" + actBorderTypeName.toLowerCase() + b.getDirection());
        if (img != null) {

          graphics.drawImage(img, pos.x, pos.y, size.width, size.height, null);
        }
      }
    }
  }


  /**
   * Adds some coastal things to ocean regions depending on season and direction of non-ocean
   * 
   * @param r
   */
  private void renderCoastals(Region r) {
    if (!r.getRegionType().isOcean()) {
      // no ocean
      return;
    }
    if (r.getCoastBitMap() == null) {
      // no coast
      return;
    }

    // lets look for the directions
    int bitArray = r.getCoastBitMap().intValue();
    for (int i = 0; i < 6; i++) {
      if ((bitArray & bitMaskArray[i]) > 0) {
        // hit
        String imageNameDefault = "ocean_coast_" + i;
        int imageNumber = 0;
        if ((bitArray & bitMaskArray[7]) > 0) {
          imageNumber++;
        }
        if ((bitArray & bitMaskArray[6]) > 0) {
          imageNumber += 2;
        }
        drawMyImage(imageNameDefault, r, imageNumber, false);
      }
    }

    // from bit 7 and 8 we get our random number back
    int imageNumber = 0;
    if ((bitArray & bitMaskArray[7]) > 0) {
      imageNumber++;
    }
    if ((bitArray & bitMaskArray[6]) > 0) {
      imageNumber += 2;
    }
    if (imageNumber!=0){
      String imageNameDefault = "ocean_nocoast";
      drawMyImage(imageNameDefault, r, imageNumber, false);
    }
  }

	private void drawMyImage(String imageNameDefault, Region r, int randomImageNumber, boolean errorIfNotFound){
	  CoordinateID c = null;
    Point pos = null;
    Dimension size = null;
	  String imageName = imageNameDefault;
    Image img = null;
    // first try a season specific icon, if preferences say so!
    // FIXME shouldn't access getData() from here
    if (r.getData().getDate() != null && this.isUseSeasonImages()) {
      switch (r.getData().getDate().getSeason()) {
        case Date.SPRING: imageName+="_spring"; break;
        case Date.SUMMER: imageName+="_summer"; break;
        case Date.AUTUMN: imageName+="_autumn"; break;
        case Date.WINTER: imageName+="_winter"; break;
      }
    }
    String seasonalImageName = imageName;
    // first first we try to add a random number 0..3 to the seasonal
    if (randomImageNumber!=BorderCellRenderer.noRandomImages){
      imageName = seasonalImageName + "_" + randomImageNumber;
      img = getImage(imageName,false);
      if (img==null){
        // fall-back: if no randomized image is found we try just the 
        // seasonal one...
        imageName=seasonalImageName;
      }
    }
    // seasonal always without error msg
    img = getImage(imageName,false);
    
    // if we cannot find it, try a default icon.
    if (img == null) {
      imageName = imageNameDefault; 
      img = getImage(imageName,errorIfNotFound);
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
	 * Returns {@link Mapper#PLANE_BORDER}.
	 * 
	 * @see magellan.client.swing.map.HexCellRenderer#getPlaneIndex()
	 */
	@Override
  public int getPlaneIndex() {
		return Mapper.PLANE_BORDER;
	}

  /**
   * @see magellan.client.swing.map.HexCellRenderer#getName()
   */
  @Override
  public String getName() {
    return Resources.get("map.bordercellrenderer.name");
  }

  
  /**
   * This Preferences dialog does nothing...
   */
  protected class Preferences extends JPanel implements PreferencesAdapter {
    // The source component to configure
    protected BorderCellRenderer source = null;

    // GUI elements

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
    }

    public void initPreferences() {
        
    }

    /**
     * @see magellan.client.swing.preferences.PreferencesAdapter#applyPreferences()
     */
    public void applyPreferences() {
      
    }

    /**
     * @see magellan.client.swing.preferences.PreferencesAdapter#getComponent()
     */
    public Component getComponent() {
      return this;
    }

    /**
     * @see magellan.client.swing.preferences.PreferencesAdapter#getTitle()
     */
    public String getTitle() {
      return source.getName();
    }
  }


  public boolean isUseSeasonImages() {
    // return useSeasonImages;
    return (Boolean.valueOf(settings.getProperty(PropertiesHelper.BORDERCELLRENDERER_USE_SEASON_IMAGES,
        Boolean.TRUE.toString()))).booleanValue();
  }
  
  @Override
  public PreferencesAdapter getPreferencesAdapter() {
    return new Preferences(this);
  }
  
}
