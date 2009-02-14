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
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Image;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.image.FilteredImageSource;
import java.awt.image.ImageProducer;
import java.awt.image.RGBImageFilter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.swing.GrayFilter;
import javax.swing.JCheckBox;
import javax.swing.JPanel;

import magellan.client.MagellanContext;
import magellan.client.swing.preferences.PreferencesAdapter;
import magellan.library.CoordinateID;
import magellan.library.Ship;
import magellan.library.Unit;
import magellan.library.utils.Direction;
import magellan.library.utils.Regions;
import magellan.library.utils.Resources;
import magellan.library.utils.Umlaut;
import magellan.library.utils.logging.Logger;


/**
 * DOCUMENT-ME
 *
 * @author $Author: $
 * @version $Revision: 389 $
 */
public class PathCellRenderer extends ImageCellRenderer {
	private static final Logger log = Logger.getInstance(PathCellRenderer.class);
	private static final int ACTIVE = 0;
	private static final int PASSIVE = 1;
	private static final int ACTIVEPAST = 2;
	private static final int PASSIVEPAST = 3;
  private static final int ACTIVEFUTURE = 4;
	
	private static final int ALPHALEVEL = 100;
	private boolean drawPassivePath = false;
	private boolean drawPastPath = false;
	protected Map<String,ImageContainer> ownImages = new HashMap<String, ImageContainer>();
	RGBImageFilter passiveFilter;
	RGBImageFilter activePastFilter;
	RGBImageFilter passivePastFilter;
  RGBImageFilter activeFutureFilter;

	/**
	 * Creates a new PathCellRenderer object.
	 *
	 * 
	 * 
	 */
	public PathCellRenderer(CellGeometry geo, MagellanContext context) {
		super(geo, context);
		drawPassivePath = (Boolean.valueOf(settings.getProperty("PathCellRenderer.drawPassivePath",
															"true"))).booleanValue();
		drawPastPath = (Boolean.valueOf(settings.getProperty("PathCellRenderer.drawPastPath", "true"))).booleanValue();

		passiveFilter = new GrayFilter(true, 50);
    activePastFilter = new ChannelFilter(ChannelFilter.ALPHA, PathCellRenderer.ALPHALEVEL);
    passivePastFilter = new ChannelFilter(ChannelFilter.ALPHA, PathCellRenderer.ALPHALEVEL, new GrayFilter(false, 50));
    activeFutureFilter = new ChannelFilter(ChannelFilter.ALPHA, PathCellRenderer.ALPHALEVEL, new ChannelFilter(ChannelFilter.RED, 0));
	}

  /**
   * @see magellan.client.swing.map.HexCellRenderer#render(java.lang.Object, boolean, boolean)
   */
  @Override
	public void render(Object obj, boolean active, boolean selected) {
		try {
			if(obj instanceof Unit) {
				render((Unit) obj);
			} else if(obj instanceof Ship) {
				render(((Ship) obj).getOwnerUnit());
      }
		} catch(Exception e) {
			PathCellRenderer.log.error(e);
		}
	}

	/**
	 * Checks the orders of the specified unit for movement orders and renders arrows indicating
	 * the direction the unit is taking. Note that the movement orders may not be abbreviated for
	 * this to work.
	 *
	 * 
	 */
	private void render(Unit u) {
		if(u == null) {
			return;
		}

		if(drawPastPath) {
			List<CoordinateID> pastMovement = u.getPastMovement(data);

			if(PathCellRenderer.log.isDebugEnabled()) {
				PathCellRenderer.log.debug("render for unit u " + u + " travelled through " + pastMovement);
			}

			renderPath(u, pastMovement, u.isPastMovementPassive() ? PathCellRenderer.PASSIVEPAST : PathCellRenderer.ACTIVEPAST);
		}

		List<CoordinateID> activeMovement = getModifiedMovement(u);

		if(activeMovement.size() > 0) {
			renderPath(u, activeMovement, PathCellRenderer.ACTIVE);
	    List<CoordinateID> additionalMovement = getAdditionalMovement(u);
			if (additionalMovement.size()>0)
			  renderPath(u, additionalMovement, PathCellRenderer.ACTIVEFUTURE);
		} else if(drawPassivePath) {
			// unit does not move itself, check for passive movement
			// Perhaps it is on a ship?
			List<CoordinateID> passiveMovement = null;

			if(u.getModifiedShip() != null) {
				// we are on a ship. try to render movemement from ship owner
				passiveMovement = getModifiedMovement(u.getModifiedShip().getOwnerUnit());
			} else {
				// the unit is not on a ship, search for carriers
				Collection carriers = u.getCarriers();

				if(PathCellRenderer.log.isDebugEnabled()) {
					PathCellRenderer.log.debug("PathCellRenderer.render: " + u + " has " + carriers.size() +
							  " carriers");
				}

				if(carriers.size() == 1) {
					Unit trans = (Unit) carriers.iterator().next();
					passiveMovement = getModifiedMovement(trans);
				}
			}

			renderPath(u, passiveMovement, PathCellRenderer.PASSIVE);
		}
	}


 	private List<CoordinateID> getModifiedMovement(Unit u) {
 	 return getMovement(u, false);
 	}
 	
  private List<CoordinateID> getAdditionalMovement(Unit u) {
    return getMovement(u, true);
  }

  private List<CoordinateID> getMovement(Unit u, boolean isSuffix){
 	  if (u==null)
 	    return Collections.emptyList();
 	  List<CoordinateID> movement = u.getModifiedMovement();
 	  CoordinateID last = movement.size()>0?movement.get(0):null;
 	  if (last!=null){
 	    List<CoordinateID> result = new ArrayList<CoordinateID>(2);
      List<CoordinateID> suffix = new ArrayList<CoordinateID>(0);
 	    for (CoordinateID coord : movement){
 	      if (result.size()==0)
 	        result.add(coord);
 	      else if (coord.equals(result.get(result.size()-1)) || suffix.size()>0){
 	        if (isSuffix)
 	          suffix.add(coord);
 	        else
 	          break;
 	      } else {
 	        result.add(coord);
 	      }
 	    }
 	    if (isSuffix)
 	      return suffix;
 	    else
 	      return result;
 	  }else {
 	    return Collections.emptyList();
 	  }
	}

	private void renderPath(Unit u, List<CoordinateID> coordinates, int imageType) {
		if((coordinates != null) && (coordinates.size() > 0)) {
			renderPath(u, coordinates.get(0),
					   Regions.getDirectionObjectsOfCoordinates(coordinates), imageType);
		}
	}

	private void renderPath(Unit u, CoordinateID start, List directions, int imageType) {
		if(PathCellRenderer.log.isDebugEnabled()) {
			PathCellRenderer.log.debug("renderPath for unit " + u + " from " + start + " with list " + directions +
					  ", imageType " + imageType);
		}

		CoordinateID actCoord = new CoordinateID(start); //  make Coordinate a copy 

		for(Iterator iter = directions.iterator(); iter.hasNext();) {
			Direction dirObj = (Direction) iter.next();
			int dir = dirObj.getDir();

			if(dir != -1) {
				Rectangle rect = cellGeo.getImageRect(actCoord.x, actCoord.y);
				rect.translate(-offset.x, -offset.y);

				Image img = getImage("Pfeil" + dir, imageType);
        
				if(img != null) {
					graphics.drawImage(img, rect.x, rect.y, rect.width, rect.height, null);
				}

				actCoord.translate(Direction.toCoordinate(dir));
			} else {
				break;
			}
		}
	}

	private Image getImage(String name, int imageType) {
		Image img = null;

		if(name != null) {
			String normName = Umlaut.convertUmlauts(name);

			String storeName = imageType + normName;
			if(ownImages.containsKey(storeName)) {
				ImageContainer c = ownImages.get(storeName);

				if(c != null) {
					img = c.scaled;
				}
			} else {
				img = getImage(name);

				switch(imageType) {
				case ACTIVE:
					break;

				case PASSIVE:
					img = createImage(img, passiveFilter);

					break;

				case ACTIVEPAST:
					img = createImage(img, activePastFilter);

					break;

				case PASSIVEPAST:
          img = createImage(img, passivePastFilter);

          break;
				case ACTIVEFUTURE:
					img = createImage(img, new ChannelFilter(ChannelFilter.GREEN, 0, new ChannelFilter(ChannelFilter.ALPHA, PathCellRenderer.ALPHALEVEL)));

					break;
				}

				if(img != null) {
					ownImages.put(storeName, new ImageContainer(img, scale(img)));
				} else {
					// add null to the map so we do not attempt to load the file again
					ownImages.put(storeName, null);
				}
			}
		}

		return img;
	}

	// BEGIN Image processing
	private Image createImage(Image img, RGBImageFilter filter) {
		if(img == null) {
			return null;
		}

		ImageProducer prod = new FilteredImageSource(img.getSource(), filter);

		return Toolkit.getDefaultToolkit().createImage(prod);
	}

	private class ChannelFilter extends RGBImageFilter {
	  public static final int RED = 0x00FF0000;
    public static final int GREEN = 0x0000FF00;
    public static final int BLUE = 0x000000FF;
    public static final int ALPHA = 0xFF000000;
	  
    private int channel;
	  private int level;
		private RGBImageFilter parent;

		/**
		 * Creates a new AlphaFilter object.
		 *
		 * @param channel One of {@link #RED}, {@link #GREEN}, {@link #BLUE}, or {@link #ALPHA}.
		 * @param level The filtering level 0 is "switch of", 255 is "switch on"
		 */
		public ChannelFilter(int channel, int level) {
			this(channel, level, null);
		}

		/**
		 * Creates a new ChannelFilter object.
		 *
		 * 
     * @param channel One of {@link #RED}, {@link #GREEN}, {@link #BLUE}, or {@link #ALPHA}.
     * @param level The filtering level 0 is "switch of", 255 is "switch on"
     * @param parent A filter which is to be applied after this filter
     */
		public ChannelFilter(int channel, int level, RGBImageFilter parent) {
      if (level<0 || level>255)
        throw new IllegalArgumentException("wrong level "+level);
      this.channel = channel;
			this.level = level * (0x01010101 & channel);
			this.parent = parent;

			// canFilterIndexColorModel indicates whether or not it is acceptable
			// to apply the color filtering of the filterRGB method to the color
			// table entries of an IndexColorModel object in lieu of pixel by pixel
			// filtering.
			canFilterIndexColorModel = true;
		}

		/**
		 *  Sets the selected channel of the pixel to the selected level if the alpha value is not 0.
		 * 
		 * @see java.awt.image.RGBImageFilter#filterRGB(int, int, int)
		 */
		@Override
    public int filterRGB(int x, int y, int rgb) {
			if(parent == null) {
				return myFilterRGB(x, y, rgb);
			} else {
				return parent.filterRGB(x, y, myFilterRGB(x, y, rgb));
			}
		}

		private int myFilterRGB(int x, int y, int rgb) {
			// set alpha from opaque to given level
			// if alpha channel IS transparent we dont do anything
			if((rgb & 0xff000000) == 0) {
				return rgb;
			} else {
				// we found a non-transparent pixel, so set given alpha level
				return (rgb & (~channel)) | level;
			}
		}
	}

	// END Image processing

	/**
	 * Scale all images this renderer uses to a certain scale factor.
	 *
	 * @param scaleFactor the factor to scale the images with (a scaleFactor of 1.0 would scale all
	 * 		  images to their original size).
	 */
	@Override
  public void scale(float scaleFactor) {
		super.scale(scaleFactor);

		for(Iterator iter = ownImages.values().iterator(); iter.hasNext();) {
			ImageContainer c = (ImageContainer) iter.next();

			if(c != null) {
				c.scaled = scale(c.unscaled);
			}
		}
	}

	/**
	 * Make the renderer reload all of its cached images.
	 */
	@Override
  public void reloadImages() {
		super.reloadImages();
		ownImages.clear();
	}

	/**
	 * Set the cell geometry this renderer is based on and make it reload all of its cached images.
	 *
	 * 
	 */
	@Override
  public void setCellGeometry(CellGeometry geo) {
		super.setCellGeometry(geo);
		reloadImages();
	}

	/**
	 * DOCUMENT-ME
	 *
	 * 
	 */
	@Override
  public int getPlaneIndex() {
		return Mapper.PLANE_PATH;
	}

	// BEGIN preferences stuff
	private boolean getDrawPassivePath() {
		return drawPassivePath;
	}

	private void setDrawPassivePath(boolean bool) {
		drawPassivePath = bool;
		settings.setProperty("PathCellRenderer.drawPassivePath", String.valueOf(bool));
	}

	private boolean getDrawPastPath() {
		return drawPastPath;
	}

	private void setDrawPastPath(boolean bool) {
		drawPastPath = bool;
		settings.setProperty("PathCellRenderer.drawPastPath", String.valueOf(bool));
	}

	/**
	 * DOCUMENT-ME
	 *
	 * 
	 */
	@Override
  public PreferencesAdapter getPreferencesAdapter() {
		return new Preferences(this);
	}

	protected class Preferences extends JPanel implements PreferencesAdapter {
		// The source component to configure
		protected PathCellRenderer source = null;

		// GUI elements
		private JCheckBox chkDrawPassivePath = null;
		private JCheckBox chkDrawPastPath = null;

		/**
		 * Creates a new Preferences object.
		 *
		 * 
		 */
		public Preferences(PathCellRenderer r) {
			this.source = r;
			init();
		}

		private void init() {
			chkDrawPassivePath = new JCheckBox(Resources.get("map.pathcellrenderer.drawpassivepath"),
											   source.getDrawPassivePath());
			chkDrawPastPath = new JCheckBox(Resources.get("map.pathcellrenderer.drawpastpath"), source.getDrawPastPath());

			this.setLayout(new GridBagLayout());

			GridBagConstraints c = new GridBagConstraints();
			c.anchor = GridBagConstraints.WEST;
			c.gridx = 0;
			c.gridy = 0;
			this.add(chkDrawPassivePath, c);
			c.gridx = 0;
			c.gridy = 1;
			this.add(chkDrawPastPath, c);
		}

        public void initPreferences() {
            // TODO: implement it
        }

		/**
		 * DOCUMENT-ME
		 */
		public void applyPreferences() {
			source.setDrawPassivePath(chkDrawPassivePath.isSelected());
			source.setDrawPastPath(chkDrawPastPath.isSelected());
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

	// END   preferences stuff


  /**
   * @see magellan.client.swing.map.HexCellRenderer#getName()
   */
  @Override
  public String getName() {
    return Resources.get("map.pathcellrenderer.name");
  }

}
