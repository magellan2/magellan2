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

import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.MediaTracker;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.InputEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.StringTokenizer;

import javax.swing.Scrollable;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.ToolTipManager;

import magellan.client.Client;
import magellan.client.MagellanContext;
import magellan.client.desktop.Initializable;
import magellan.client.event.SelectionEvent;
import magellan.client.event.SelectionListener;
import magellan.client.event.UnitOrdersEvent;
import magellan.client.swing.InternationalizedDataPanel;
import magellan.client.swing.context.MapContextMenu;
import magellan.client.swing.preferences.PreferencesAdapter;
import magellan.client.utils.ErrorWindow;
import magellan.library.Building;
import magellan.library.CoordinateID;
import magellan.library.GameData;
import magellan.library.Region;
import magellan.library.Scheme;
import magellan.library.Ship;
import magellan.library.Unit;
import magellan.library.event.GameDataEvent;
import magellan.library.event.UnitChangeEvent;
import magellan.library.event.UnitChangeListener;
import magellan.library.rules.ItemType;
import magellan.library.utils.PropertiesHelper;
import magellan.library.utils.Resources;
import magellan.library.utils.Sorted;
import magellan.library.utils.logging.Logger;
import magellan.library.utils.replacers.ReplacerFactory;
import magellan.library.utils.replacers.ReplacerHelp;
import magellan.library.utils.replacers.ReplacerSystem;

/**
 * A component displaying a map based on a <tt>GameData</tt> object. The appearance of the map is
 * made configurable by using combinations of classes implementing the <tt>CellRenderers</tt>
 * interface.
 * <p>
 * <b>Note:</b>
 * </p>
 * <p>
 * This class avoids Java2D methods so it can be used with JDK version earlier than 1.2.
 * </p>
 */
public class Mapper extends InternationalizedDataPanel implements SelectionListener, Scrollable,
    UnitChangeListener, Initializable {
  private static final Logger log = Logger.getInstance(Mapper.class);

  /**
   * a mapping for int positions of planes to logical names. Will be used for magellan_desktop.ini
   */
  public static final String PLANE_STRINGS[] = { "BACKGROUND", "BEHIND", "REGION", "BORDER",
      "BUILDING", "SHIP", "TEXT", "PATH", "HIGHLIGHT", "MARKINGS", "SCHEMES", "SIGNS" };

  /** Plane for general background renderer, like background image renderer */
  public static final int PLANE_BACKGROUND = 0;

  /**
   * Plane for something to be rendered behind the regions, makes only sense when regions have
   * transparent parts, used for schemes of the real world shining through the dust of the astral
   * space
   */
  public static final int PLANE_BEHIND = 1;

  /** Plane for the region map */
  public static final int PLANE_REGION = 2;

  /** Plane for borders */
  public static final int PLANE_BORDER = 3;

  /** Plane for buildings */
  public static final int PLANE_BUILDING = 4;

  /** Plane for ships */
  public static final int PLANE_SHIP = 5;

  /** Plane for region text */
  public static final int PLANE_TEXT = 6;

  /** Plane for path informations */
  public static final int PLANE_PATH = 7;

  /** Plane for highlights */
  public static final int PLANE_HIGHLIGHT = 8;

  /** Plane for markers */
  public static final int PLANE_MARKINGS = 9;

  /** Plane for schemes */
  public static final int PLANE_SCHEMES = 10;

  /** Plane for signs */
  public static final int PLANE_SIGNS = 11;

  /** Number of available planes */
  private static final int PLANES = 12;

  /**
   * Default tooltip
   *
   * @deprecated use {@link #DEFAULT_TOOLTIP}
   */
  @Deprecated
  public static final String DEFAULT_TOOLTIP_DEFINITION = "<html><font=-1>§rname§</font></html>";
  /**
   * Default tooltip
   */
  public static final String DEFAULT_TOOLTIP = "Standard~<html><font=-1>§rname§</font></html>";

  private static final Cursor WAIT_CURSOR = new Cursor(Cursor.WAIT_CURSOR);
  private static final Cursor DEFAULT_CURSOR = new Cursor(Cursor.DEFAULT_CURSOR);

  private RenderingPlane planes[] = null;
  private Collection<MapCellRenderer> availableRenderers = null;
  private MediaTracker tracker = null;
  private Region prevDragRegion = null;
  private boolean doDraggingSelect = false;
  private Object activeObject = null;
  private Region activeRegion = null;
  private CoordinateID activeCoordinate = null;
  private Map<CoordinateID, Region> selectedRegions = new Hashtable<CoordinateID, Region>();
  private List<CoordinateID> pathRegions = new LinkedList<CoordinateID>();
  private boolean pathPersistence = false;
  private Rectangle mapToScreenBounds = null;
  private int showLevel = 0;
  private float scaleFactor = 1.0f;
  private Rectangle currentBounds = null;
  private Image buffer = null;

  // The cell geometry used by the renderes, see setRenderer()
  private CellGeometry cellGeometry = null;

  /** Holds value of property renderContextChanged. */
  private boolean renderContextChanged;

  private static Collection<Mapper> instances = new ArrayList<Mapper>();

  // protected StringBuffer tooltipBuffer=new StringBuffer();
  protected boolean showTooltip = false;
  protected ItemType silverItemType = null;
  protected ReplacerSystem tooltipDefinition;
  protected String[] tooltipDefinitionStrings = new String[2];
  protected static ReplacerFactory tooltipReplacers;

  // region sublist for rendering
  protected List<Sorted> objectList = null;
  protected int lastRegionRenderingType = -1;
  protected int inPaint = 0;

  // context
  protected MapContextMenu conMenu;

  protected MagellanContext context;

  private AdvancedTextCellRenderer atr;

  private boolean defer = true;

  private boolean deferredPainting = true;

  private boolean useSeasonImages = true;

  private AdvancedRegionShapeCellRenderer arr;

  private String id;

  /**
   * Creates a new Mapper object.
   *
   * @param id
   */
  public Mapper(MagellanContext context, Collection<MapCellRenderer> customRenderers,
      CellGeometry geom, String id) {
    super(context.getEventDispatcher(), context.getGameData(), context.getProperties());
    this.id = id;

    instances.add(this);

    this.context = context;

    conMenu =
        new MapContextMenu(context.getClient(), context.getEventDispatcher(), context
            .getProperties());

    reprocessTooltipDefinition();

    setShowTooltip(PropertiesHelper.getBoolean(settings, "Mapper.showTooltips", false));

    setUseSeasonImages(PropertiesHelper.getBoolean(settings,
        PropertiesHelper.BORDERCELLRENDERER_USE_SEASON_IMAGES, true));

    setDoubleBuffered(false); // we mainly use our own buffer

    final Mapper mapper = this;

    // set the tracker used to repaint when loading and scaling images takes a
    // while
    // removed this decision from options. This is a developer decision!!!
    // if ((Boolean.valueOf(settings.getProperty("Mapper.deferPainting", "true"))).booleanValue()) {
    // tracker = new MediaTracker(this);
    // ImageCellRenderer.setTracker(tracker);
    // }

    // load the cell geometry to be used for painting cells
    cellGeometry = geom;

    // initialize renderers and planes (mark the order!)
    availableRenderers = initAvailableRenderers(cellGeometry, settings, customRenderers);
    planes = initRenderingPlanes();

    // determine the size of the map in component coordinates
    mapToScreenBounds = getMapToScreenBounds();

    if (mapToScreenBounds != null) {
      setPreferredSize(mapToScreenBounds.getSize());
    }

    addMouseListener(new MouseAdapter() {
      @Override
      public void mousePressed(MouseEvent me) {
        requestFocus();

        if (!pathPersistence) {
          pathRegions.clear();
        }

        if ((cellGeometry == null) || (mapToScreenBounds == null))
          return;

        CoordinateID c =
            cellGeometry.getCoordinate(me.getPoint().x + mapToScreenBounds.x, me.getPoint().y
                + mapToScreenBounds.y, showLevel);
        Region region = getGameData().getRegion(c);
        Region voidRegion = getGameData().voids().get(c);
        Region wrapper = null;

        // might be a wrapping region: find original
        if (region == null) {
          wrapper = getGameData().wrappers().get(c);
          if (wrapper != null) {
            region = getGameData().getOriginal(wrapper);
          }
        }

        if (region == null && PropertiesHelper.getBoolean(settings, "map.creating.void", false)) {
          voidRegion = getGameData().voids().get(c);
        }

        // FIXME on Mac platforms CTRL+Button1 is the popup trigger, which conflicts with this
        // behaviour
        if ((me.getModifiers() & InputEvent.BUTTON1_MASK) != 0) {
          if ((me.getModifiers() & InputEvent.CTRL_MASK) != 0) {
            if (region != null) {
              // add region to selection -- do not add wrappers or voids
              if (selectedRegions.containsKey(region.getID()) == false) {
                doDraggingSelect = true;
                selectedRegions.put(region.getID(), region);
              } else {
                doDraggingSelect = false;
                selectedRegions.remove(region.getID());
              }

              getGameData().setSelectedRegionCoordinates(selectedRegions);
              dispatcher.fire(SelectionEvent.create(mapper, selectedRegions.values()));
              repaint();
              prevDragRegion = region;
            }
          } else {
            // move cursor to region
            if (region != null || voidRegion != null) {
              activeRegion = region != null ? region : voidRegion;
              activeCoordinate = c;
              activeObject = activeRegion;
              dispatcher.fire(SelectionEvent.create(mapper, activeRegion));
              repaint();
            }
          }
        } else if ((me.getModifiers() & InputEvent.BUTTON3_MASK) != 0) {
          if (region != null) {
            // show context menu for real region
            conMenu.init(region, selectedRegions.values());
            conMenu.show(Mapper.this, me.getX(), me.getY());
          } else {
            // show context menu for no region
            conMenu.clear(c);
            conMenu.show(Mapper.this, me.getX(), me.getY());
          }
        }
      }

      @Override
      public void mouseReleased(MouseEvent me) {
        if ((me.getModifiers() & InputEvent.BUTTON1_MASK) != 0) {
          prevDragRegion = null;
        }
      }
    });

    addMouseMotionListener(new MouseMotionAdapter() {
      @Override
      public void mouseDragged(MouseEvent me) {
        if (((me.getModifiers() & InputEvent.BUTTON1_MASK) != 0)
            && ((me.getModifiers() & InputEvent.CTRL_MASK) != 0)) {
          if (!pathPersistence) {
            pathRegions.clear();
          }

          if (cellGeometry == null)
            return;

          CoordinateID c =
              cellGeometry.getCoordinate(me.getPoint().x + mapToScreenBounds.x, me.getPoint().y
                  + mapToScreenBounds.y, showLevel);
          Region r = getGameData().getRegion(c);

          if ((r != null) && ((prevDragRegion == null) || !prevDragRegion.equals(r))) {
            boolean regionAlreadySelected = selectedRegions.containsKey(c);
            boolean doFire = false;

            if (!regionAlreadySelected) {
              if (doDraggingSelect) {
                selectedRegions.put(c, r);
                doFire = true;
              }
            } else {
              if (!doDraggingSelect) {
                selectedRegions.remove(c);
                doFire = true;
              }
            }

            if (doFire) {
              getGameData().setSelectedRegionCoordinates(selectedRegions);
              dispatcher.fire(SelectionEvent.create(mapper, selectedRegions.values()));
            }

            repaint();
          }

          prevDragRegion = r;
        }
      }
    });

    addKeyListener(new KeyAdapter() {
      @Override
      public void keyPressed(KeyEvent e) {
        if (activeCoordinate == null)
          return;

        CoordinateID translationCoord = null;
        final int xsgn =
            cellGeometry.getUnscaledCellPositionY(1, 0)
                - cellGeometry.getUnscaledCellPositionY(0, 0) > 0 ? -1 : 1;
        final int ysgn =
            cellGeometry.getUnscaledCellPositionY(0, 1)
                - cellGeometry.getUnscaledCellPositionY(0, 0) > 0 ? -1 : 1;

        switch (e.getKeyCode()) {
        case KeyEvent.VK_UP:
        case KeyEvent.VK_NUMPAD9:
          translationCoord = CoordinateID.create(0, ysgn);

          break;

        case KeyEvent.VK_RIGHT:
        case KeyEvent.VK_NUMPAD6:
          translationCoord = CoordinateID.create(xsgn, 0);
          break;

        case KeyEvent.VK_DOWN:
        case KeyEvent.VK_NUMPAD1:
          translationCoord = CoordinateID.create(0, -ysgn);

          break;

        case KeyEvent.VK_LEFT:
        case KeyEvent.VK_NUMPAD4:
          translationCoord = CoordinateID.create(-xsgn, 0);

          break;

        case KeyEvent.VK_NUMPAD3:
          translationCoord = CoordinateID.create(xsgn, -ysgn);

          break;

        case KeyEvent.VK_NUMPAD7:
          translationCoord = CoordinateID.create(-xsgn, ysgn);

          break;

        case KeyEvent.VK_NUMPAD2:
          translationCoord = CoordinateID.create(xsgn, -2 * ysgn);

          break;

        case KeyEvent.VK_NUMPAD8:
          translationCoord = CoordinateID.create(-xsgn, 2 * ysgn);

          break;

        default:
          break;
        }

        if (translationCoord != null) {
          activeCoordinate = activeCoordinate.translate(translationCoord);
          activeRegion = getGameData().getRegion(activeCoordinate);
          getGameData().setSelectedRegionCoordinates(null);
          dispatcher.fire(SelectionEvent.create(mapper, activeRegion));
          repaint();
        }
      }
    });

    dispatcher.addSelectionListener(this);
    // we are now a UnitChangeListener
    // dispatcher.addUnitOrdersListener(this);

    conMenu.updateRenderers(this);
    conMenu.updateTooltips(this);

    final int defaultDismissTimeout = ToolTipManager.sharedInstance().getDismissDelay();
    addMouseListener(new MouseAdapter() {
      @Override
      public void mouseEntered(MouseEvent me) {
        ToolTipManager.sharedInstance().setDismissDelay(20000);
      }

      @Override
      public void mouseExited(MouseEvent me) {
        ToolTipManager.sharedInstance().setDismissDelay(defaultDismissTimeout);
      }
    });
  }

  /**
   *
   */
  protected void reprocessTooltipDefinition() {
    // standard def is Name~Definition
    String[] tip = settings.getProperty("Mapper.ToolTip.Definition", Mapper.DEFAULT_TOOLTIP).split(
        "~");
    if (tip.length < 2) {
      tip = new String[] { "", tip[0] };
    }
    setTooltipDefinition(tip[0], tip[1]);
  }

  /**
   * @see javax.swing.JComponent#getToolTipText(java.awt.event.MouseEvent)
   */
  @Override
  public String getToolTipText(MouseEvent e) {
    if (tooltipDefinition != null) {
      try {
        CoordinateID c =
            cellGeometry.getCoordinate(e.getPoint().x + mapToScreenBounds.x, e.getPoint().y
                + mapToScreenBounds.y, showLevel);
        Region r = getGameData().getRegion(c);
        if (r == null) {
          r = getGameData().wrappers().get(c);
        }
        if (r == null && PropertiesHelper.getBoolean(settings, "map.creating.void", false)) {
          r = getGameData().voids().get(c);
        }

        if (r != null) {
          Object ret = tooltipDefinition.getReplacement(r);

          if (ret != null)
            return ret.toString();
          else
            return "-?-";
        }
      } catch (Exception exc) {
        // not critical
      }
    }

    return null;
  }

  /**
   * @see magellan.client.event.UnitOrdersListener#unitOrdersChanged(magellan.client.event.UnitOrdersEvent)
   */
  public void unitOrdersChanged(UnitOrdersEvent e) {
    // TODO: do we really need to repaint this?
    repaint();
  }

  /**
   * Add a cell renderer object to the mapper. Each cell renderer has a rendering plane associated
   * with it, so if there is already a renderer in the rendering plane of the added renderer the old
   * renderer is removed.
   *
   * @param renderer the object responsible for rendering a graphical representation of regions.
   */
  public void setRenderer(MapCellRenderer renderer) {
    if (renderer != null) {
      setRenderer(renderer, renderer.getPlaneIndex());
    } else {
      Mapper.log
          .warn(
              "Mapper.setRenderer(): null renderer set has been set for unknown rendering plane!");
    }
  }

  /**
   * Set a cell renderer object for a certain plane of the map. This function can be used to
   * override the renderes default rendering plane.
   *
   * @param renderer the object responsible for rendering a graphical representation of regions.
   * @param plane the plane the renderer will draw to. Lower planes are painted over by higher
   *          planes. See the constants in com.eressea.swing.map.Mapper for possible values or
   *          choose a value between 0 and getRenderPlainCount() - 1.
   */
  public void setRenderer(MapCellRenderer renderer, int plane) {
    if ((plane >= 0) && (plane < planes.length)) {
      if (planes[plane] == null) {
        planes[plane] = new RenderingPlane(plane, "Zusatzplane");
      }

      planes[plane].setRenderer(renderer);

      String className = "none";

      if (renderer != null) {
        className = renderer.getClass().getName();
      }
      // log.info("Mapper.setRenderer("+className+")");
      // log.info("Mapper.getPropertyName("+plane+")="+getPropertyName(plane));
      settings.setProperty(getPropertyName(plane), className);
      conMenu.updateRenderers(this);
    } else {
      Mapper.log.warn("Mapper.setRenderer(): invalid argument: plane out of bounds");
    }
  }

  protected String getPropertyName(int plane) {
    return "Mapper.Planes." + Mapper.PLANE_STRINGS[plane];
  }

  /**
   * Get the cell renderer objects that are available for a certain rendering plane. It is suggested
   * that these objects are used for calling one of the setRenderer() methods.
   *
   * @param plane the plane the renderer will draw to. Lower planes are painted over by higher
   *          planes. See the constants in com.eressea.swing.map.Mapper for possible values.
   * @return the renderer object associated with the specified rendering plane or null if no such
   *         association exists.
   */
  public Collection<MapCellRenderer> getRenderers(int plane) {
    Collection<MapCellRenderer> renderers = null;

    if ((plane >= 0) && (plane < planes.length)) {
      renderers = new LinkedList<MapCellRenderer>();

      for (MapCellRenderer r : getAvailableRenderers()) {
        if (r.getPlaneIndex() == plane) {
          renderers.add(r);
        }
      }
    } else {
      Mapper.log.warn("Mapper.getRenderers(): invalid argument: plane out of bounds");
    }

    return renderers;
  }

  /**
   * Returns a list of object containing the rendering planes existing in this Mapper object. The
   * planes are sorted with ascending plane indices.
   */
  public List<RenderingPlane> getPlanes() {
    return Arrays.asList(planes);
  }

  /**
   * Set a path - a list of consecutive regions - to be rendered by the renderer registered to the
   * path rendering plane.
   *
   * @param path a list of <tt>Region</tt> objects to be rendered as a path on the map.
   * @param isPersistent if <tt>true</tt>, always render the path, else render the path only until a
   *          different region is selected.
   */
  public void setPath(List<CoordinateID> path, boolean isPersistent) {
    pathRegions.clear();
    pathRegions.addAll(path);
    pathPersistence = isPersistent;
  }

  /**
   * @see magellan.client.swing.InternationalizedDataPanel#gameDataChanged(magellan.library.event.GameDataEvent)
   */
  @Override
  public void gameDataChanged(GameDataEvent e) {
    setGameData(e.getGameData());

    // FIXME (stm) shouldn't we make contextMenu a GameDataListener, too?
    conMenu.setGameData(getGameData());

    mapToScreenBounds = getMapToScreenBounds();

    if (mapToScreenBounds != null) {
      setSize(mapToScreenBounds.getSize());
    }

    setPreferredSize(getSize());
    activeRegion = getGameData().getActiveRegion();
    if (activeRegion != null) {
      activeCoordinate = activeRegion.getCoordinate();
    }
    selectedRegions.clear();

    pathRegions.clear();

    reprocessTooltipDefinition();
  }

  @Override
  public void setGameData(GameData data) {
    getGameData().removeUnitChangeListener(this);
    super.setGameData(data);
    data.addUnitChangeListener(this);

    if (getGameData() != null) {
      cellGeometry.setCoordMapper(getGameData().getGameSpecificStuff().getCoordMapper());
    }
  }

  /**
   * @see magellan.client.event.SelectionListener#selectionChanged(magellan.client.event.SelectionEvent)
   */
  public void selectionChanged(SelectionEvent se) {
    if (se.getSource() == this)
      return;

    activeObject = se.getActiveObject();

    if (activeObject != null) {
      Region newRegion = null;

      if (activeObject instanceof Region) {
        newRegion = (Region) activeObject;
      } else if (activeObject instanceof Building) {
        newRegion = ((Building) activeObject).getRegion();
      } else if (activeObject instanceof Ship) {
        newRegion = ((Ship) activeObject).getRegion();
      } else if (activeObject instanceof Unit) {
        newRegion = ((Unit) activeObject).getRegion();
      }

      if (newRegion != null) {
        activeRegion = newRegion;
        activeCoordinate = activeRegion.getCoordinate();

        if (activeCoordinate.getZ() != showLevel) {
          setLevel(activeCoordinate.getZ());
        }
      }
    }

    if (Mapper.log.isDebugEnabled()) {
      Mapper.log.debug("Mapper.selectionChanged on region " + activeRegion);
    }

    if ((se.getSelectedObjects() != null) && (se.getSelectionType() == SelectionEvent.ST_REGIONS)) {
      selectedRegions.clear();

      for (Object o : se.getSelectedObjects()) {
        if (o instanceof Region) {
          Region r = (Region) o;
          selectedRegions.put(r.getID(), r);
        }
      }
    }

    if ((activeObject != null)
        || ((se.getSelectedObjects() != null) && (se
            .getSelectionType() == SelectionEvent.ST_REGIONS))) {
      repaint();
    }
  }

  /**
   * @see java.awt.Component#isFocusTraversable()
   */
  @Override
  public boolean isFocusTraversable() {
    return true;
  }

  /**
   * @see javax.swing.JComponent#isRequestFocusEnabled()
   */
  @Override
  public boolean isRequestFocusEnabled() {
    return true;
  }

  /**
   * @see javax.swing.Scrollable#getPreferredScrollableViewportSize()
   */
  public Dimension getPreferredScrollableViewportSize() {
    return getPreferredSize();
  }

  /**
   * @see javax.swing.Scrollable#getScrollableBlockIncrement(java.awt.Rectangle, int, int)
   */
  public int getScrollableBlockIncrement(Rectangle visibleRect, int orientation, int direction) {
    if (orientation == SwingConstants.HORIZONTAL)
      return visibleRect.width - cellGeometry.getCellSize().width;
    else
      return visibleRect.height - cellGeometry.getCellSize().height;
  }

  /**
   * @see javax.swing.Scrollable#getScrollableTracksViewportHeight()
   */
  public boolean getScrollableTracksViewportHeight() {
    return false;
  }

  /**
   * @see javax.swing.Scrollable#getScrollableTracksViewportWidth()
   */
  public boolean getScrollableTracksViewportWidth() {
    return false;
  }

  /**
   * Returns a list of available rendereres in this map.
   */
  public Collection<MapCellRenderer> getAvailableRenderers() {
    return availableRenderers;
  }

  /**
   * @see javax.swing.Scrollable#getScrollableUnitIncrement(java.awt.Rectangle, int, int)
   */
  public int getScrollableUnitIncrement(Rectangle visibleRect, int orientation, int direction) {
    if (orientation == SwingConstants.HORIZONTAL)
      return cellGeometry.getCellSize().width;
    else
      return cellGeometry.getCellSize().height;
  }

  /**
   * Creates a sublist of regions to render according to the state of the given int. Values are
   * interpreted as those of {@link RenderingPlane}.
   */
  protected List<Sorted> createSubList(int condition, CoordinateID upperLeft,
      CoordinateID lowerRight, List<Sorted> regionList, int duration, int paintNumber) {
    // okay, the result could contain Regions or Units or whatever...
    List<Sorted> main = null;

    if ((inPaint < 2) || (paintNumber == 0) || (duration > 0)) {
      main = regionList;
    }

    if (main == null) {
      main = new LinkedList<Sorted>();
    } else {
      main.clear();
    }

    if ((condition & RenderingPlane.ACTIVE_OBJECT) != 0) {
      // simply add the first region found
      if (activeObject != null) {
        if (activeObject instanceof Sorted) {
          main.add((Sorted) activeObject);
        }
      }
      return main;
    }

    // just use visible regions as base
    if ((condition & RenderingPlane.VISIBLE_REGIONS) != 0) {
      int xstart = upperLeft.getX();
      int xend = lowerRight.getX();
      int ystart = upperLeft.getY();
      int yend = (lowerRight.getY());
      if (xstart > xend) {
        int dummy = xend;
        xend = xstart + 1;
        xstart = dummy - 2;
      } else {
        xstart -= 2;
        xend += 1;
      }
      if (ystart < yend) {
        int dummy = yend;
        yend = ystart - 1;
        ystart = dummy + 1;
      } else {
        ystart += 1;
        yend -= 1;
      }

      int yCounter = 0;

      for (int y = ystart; y >= yend; y--) {
        if ((++yCounter % 2) == 0) {
          xstart += 1;
        }

        for (int x = xstart; x < xend; x++) {
          CoordinateID c = CoordinateID.create(x, y, upperLeft.getZ());
          Region r = getGameData().getRegion(c);
          if (r == null) {
            r = getGameData().wrappers().get(c);
          }
          if (r == null && PropertiesHelper.getBoolean(settings, "map.creating.void", false)) {
            r = getGameData().voids().get(c);
          }

          if (r != null) {
            main.add(r);
          }
        }
      }
    }
    /*
     * get all regions as base Note: This may be a little bit too simple since I don't know if
     * regions are sorted by coordinates in GameData. If not the painting sequence may be bad...
     * FIX: Have to look at the level...
     */
    else {
      for (Region r : getGameData().getRegions()) {
        if (r.getCoordinate().getZ() == upperLeft.getZ()) {
          main.add(r);
        }
      }
      for (Region r : getGameData().wrappers().values()) {
        if (r.getCoordinate().getZ() == upperLeft.getZ()) {
          main.add(r);
        }
      }
      for (Region r : getGameData().voids().values()) {
        if (r.getCoordinate().getZ() == upperLeft.getZ()) {
          main.add(r);
        }
      }
    }

    // sort out according to other states, use AND
    if ((condition & (RenderingPlane.SELECTED_REGIONS | RenderingPlane.ACTIVE_OR_SELECTED
        | RenderingPlane.ACTIVE_REGION | RenderingPlane.TAGGED_REGIONS)) != 0) {
      Iterator<Sorted> it = main.iterator();

      /*
       * Note: On some computers this occasionally throws Concurrent Mod Exceptions. In this case
       * stop out-sorting and return.
       */
      try {
        while (it.hasNext()) {
          Sorted sorted = it.next();
          if (sorted instanceof Region) {
            Region r = (Region) sorted;

            if (((condition & RenderingPlane.SELECTED_REGIONS) != 0)
                && !selectedRegions.containsKey(r.getID())) {
              it.remove();
            } else if (((condition & RenderingPlane.ACTIVE_REGION) != 0)
                && !(r.equals(activeRegion))) {
              it.remove();
            } else if (((condition & RenderingPlane.ACTIVE_OR_SELECTED) != 0)
                && !(r.equals(activeRegion) || selectedRegions.containsKey(r.getID()))) {
              it.remove();
            } else if (((condition & RenderingPlane.TAGGED_REGIONS) != 0) && !(r.hasTags())) {
              it.remove();
            }
          }
        }
      } catch (Exception exc) {
        log.info(exc);
        // just return with incomplete list
      }
    }

    return main;
  }

  protected void setLastRegionRenderingType(int l) {
    lastRegionRenderingType = l;
  }

  protected int getLastRegionRenderingType() {
    return lastRegionRenderingType;
  }

  /**
   * @see javax.swing.JComponent#paintComponent(java.awt.Graphics)
   */
  @Override
  protected void paintComponent(Graphics g) {
    try {
      paintMapperComponent(g);
    } catch (Throwable t) {
      Mapper.log.error("", t);
      ErrorWindow errorWindow =
          new ErrorWindow(Client.INSTANCE, ErrorWindow.UNKNOWN_ERROR_MESSAGE, "", t);
      errorWindow.setShutdownOnCancel(true);
      errorWindow.setVisible(true);
    }
  }

  /**
   * Paints the map
   */
  protected void paintMapperComponent(Graphics g) {
    if (mapToScreenBounds == null)
      return;

    if (cellGeometry == null) {
      Mapper.log.warn("Mapper.paint(): Unable to determine drawing area!");

      return;
    }

    Rectangle clipBounds = g.getClipBounds();

    if ((clipBounds.width <= 0) || (clipBounds.height <= 0))
      return;

    setCursor(Mapper.WAIT_CURSOR);

    int paintNumber = inPaint;
    inPaint++;

    int duration = 0;
    List<Sorted> regList = objectList;

    Rectangle offset =
        new Rectangle(mapToScreenBounds.x + clipBounds.x, mapToScreenBounds.y + clipBounds.y,
            clipBounds.width, clipBounds.height);
    CoordinateID upperLeftCorner = cellGeometry.getCoordinate(offset.x, offset.y, showLevel);
    CoordinateID lowerRightCorner =
        cellGeometry.getCoordinate(offset.x + clipBounds.width, offset.y + clipBounds.height,
            showLevel);

    // synchronized (trackLock) {
    if (isDeferringPainting()) {
      defer = defer || (tracker != null && !tracker.checkAll());
    } else {
      defer = false;
      try {
        if (tracker != null) {
          tracker.waitForAll();
        }
      } catch (InterruptedException e1) {
        log.warn("painting interrupted", e1);
        return;
      }
    }

    // if nothing about the drawing area changed we can simply
    // paint our buffer
    if (!clipBounds.equals(currentBounds) || isRenderContextChanged()) {
      renderContextChanged = false;

      if ((buffer == null) || !clipBounds.equals(currentBounds)) {
        setLastRegionRenderingType(-1); // full redraw

        if (buffer != null) {
          buffer.flush();
          buffer = null;
        }

        buffer =
            new BufferedImage(clipBounds.width, clipBounds.height, BufferedImage.TYPE_INT_ARGB);
      }

      Graphics bg = buffer.getGraphics();
      bg.setColor(getBackground());
      bg.fillRect(0, 0, clipBounds.width, clipBounds.height);

      for (int planeIndex = 0; (planeIndex < Mapper.PLANE_PATH)
          && (planeIndex < planes.length); planeIndex++) {
        if (planes[planeIndex] == null) {
          continue;
        }

        MapCellRenderer renderer = planes[planeIndex].getRenderer();

        if (renderer == null) {
          continue;
        }

        if (planes[planeIndex].getRegionTypes() == RenderingPlane.ALWAYS_ONCE) {
          renderer.init(getGameData(), bg, offset);
          renderer.render(null, false, false);
        } else {

          // maybe another region set
          if (planes[planeIndex].getRegionTypes() != getLastRegionRenderingType()) {
            setLastRegionRenderingType(planes[planeIndex].getRegionTypes());
            regList =
                createSubList(getLastRegionRenderingType(), upperLeftCorner, lowerRightCorner,
                    regList, duration, paintNumber);
            duration++;
          }

          if ((regList == null) || (regList.size() == 0)) {
            continue;
          }

          renderer.init(getGameData(), bg, offset);

          for (Sorted obj : regList) {
            boolean selected = false;
            boolean active = false;

            if (obj instanceof Region) {
              Region r = (Region) obj;

              selected = selectedRegions.containsKey(r.getID());

              if (activeRegion != null) {
                active = activeRegion.equals(r);
              }
            }

            renderer.render(obj, active, selected);
          }
        }
      }

      bg.dispose();
      bg = null;
    }

    g.drawImage(buffer, clipBounds.x, clipBounds.y, this);
    // ThreadMXBean bean = java.lang.management.ManagementFactory.getThreadMXBean();
    // if (bean instanceof com.sun.management.ThreadMXBean) {
    // Thread[] list = new Thread[Thread.currentThread().getThreadGroup().activeCount() * 2];
    // Thread.currentThread().getThreadGroup().enumerate(list);
    // for (Thread t : list)
    // if (t != null) {
    // log.fine("Thread " + t.getName() + " "
    // + ((com.sun.management.ThreadMXBean) bean).getThreadAllocatedBytes(t.getId()));
    // }
    // }

    offset.x = mapToScreenBounds.x;
    offset.y = mapToScreenBounds.y;

    // there are some every time repaint things
    if (planes.length > Mapper.PLANE_PATH) {
      boolean clipChanged = !clipBounds.equals(currentBounds);

      for (int planeIndex = Mapper.PLANE_PATH; planeIndex < Mapper.PLANE_SCHEMES; planeIndex++) {
        if (planes[planeIndex] == null) {
          continue;
        }

        MapCellRenderer renderer = planes[planeIndex].getRenderer();

        if (renderer == null) {
          continue;
        }

        if ((planes[planeIndex].getRegionTypes() != getLastRegionRenderingType()) || clipChanged) {
          setLastRegionRenderingType(planes[planeIndex].getRegionTypes());
          regList =
              createSubList(getLastRegionRenderingType(), upperLeftCorner, lowerRightCorner,
                  regList, duration, paintNumber);
          duration++;
        }

        if ((regList == null) || (regList.size() == 0)) {
          continue;
        }

        renderer.init(getGameData(), g, offset);

        for (Sorted obj : regList) {
          boolean selected = false;
          boolean active = false;

          if (obj instanceof Region) {
            Region r = (Region) obj;

            selected = selectedRegions.containsKey(r.getID());

            if (activeRegion != null) {
              active = activeRegion.equals(r);
            }
          }

          renderer.render(obj, active, selected);
        }
      }

      /**
       * Paint the schemes-plane
       */

      // Is there any need to mark schemes?
      if ((activeRegion != null) && (getLevel() == 0)) {
        if (activeRegion.getCoordinate().getZ() == 1) {
          // "Astralraum"-region is active
          // contains all schemes of the active region
          Collection<Region> regionSchemeList = new LinkedList<Region>();

          // collect schemes
          if ((activeRegion.schemes() != null) && !activeRegion.schemes().isEmpty()) {
            for (Scheme scheme : activeRegion.schemes()) {
              Region r = getGameData().getRegion(scheme.getID());

              if (r != null) {
                regionSchemeList.add(r);
              }
            }

            // now render the regions with the SchemeCellRenderer
            MapCellRenderer renderer = planes[Mapper.PLANE_SCHEMES].getRenderer();

            if (renderer != null) {
              renderer.init(getGameData(), g, offset);

              for (Region region : regionSchemeList) {
                renderer.render(region, true, true);
              }
            }
          }
        }
      }

      /**
       * End of paint scheme-markings
       */

      /**
       * signs paint
       */

      clipChanged = !clipBounds.equals(currentBounds);
      int planeIndex = Mapper.PLANE_SIGNS;

      MapCellRenderer renderer = planes[planeIndex].getRenderer();

      if ((planes[planeIndex].getRegionTypes() != getLastRegionRenderingType()) || clipChanged) {
        setLastRegionRenderingType(planes[planeIndex].getRegionTypes());
        regList =
            createSubList(lastRegionRenderingType, upperLeftCorner, lowerRightCorner, regList,
                duration, paintNumber);
        duration++;
      }

      if ((renderer != null) && (regList != null) && (regList.size() > 0)) {

        renderer.init(getGameData(), g, offset);

        for (Object obj : regList) {
          boolean selected = false;
          boolean active = false;

          if (obj instanceof Region) {
            Region r = (Region) obj;

            selected = selectedRegions.containsKey(r.getID());

            if (activeRegion != null) {
              active = activeRegion.equals(r);
            }
          }

          renderer.render(obj, active, selected);
        }
      }

      /**
       * just a test - allways draw all signs - for lines! Fiete 2017-03-13
       */
      if (renderer != null) {
        // testen - alle rendern - alle...weil unklar ist, von wo nach wo lines gehen können
        if (renderer instanceof SignTextCellRenderer) {
          SignTextCellRenderer STCR = (SignTextCellRenderer) renderer;
          if (STCR.isDrawingLines()) {
            STCR.setLinesOnly(true);
            for (Region r : getGameData().getRegions()) {
              if (r.getID().getZ() == showLevel) {
                STCR.render(r, false, false);
              }
            }
            STCR.setLinesOnly(false);
          }
        }
      }

      /**
       * end paint signs
       */
    }

    currentBounds = clipBounds;

    if (defer) {
      (new Thread() {

        @Override
        public void run() {
          try {
            if (defer) {
              tracker.waitForAll();
              // Thread.sleep(500);
              defer = false;

              Object[] errors = tracker.getErrorsAny();
              if (errors != null) {
                log.warnOnce("Image tracker errors: " + errors.length + " (" + tracker + ")");
              }
              currentBounds.x = -1;
              SwingUtilities.invokeLater(new Runnable() {

                public void run() {
                  repaint();
                }
              });
            }
          } catch (InterruptedException e) {
            // return
          }
        }
      }).start();
    }

    inPaint--;
    objectList = regList;

    setCursor(Mapper.DEFAULT_CURSOR);
  }

  /**
   * Returns the current scale or zoom factor. This value is a real factor, i.e. 1.0 means that the
   * components are painted according to the values supplied by the underlying CellGeometry object.
   */
  public float getScaleFactor() {
    return scaleFactor;
  }

  /**
   * Sets the scale or zoom factor. This value is a real factor, i.e. 1.0 means that the components
   * are painted according to the values supplied by the underlying CellGeometry object.
   *
   * @param scaleFactor The new factor. Must be &gt; 0.
   * @throws IllegalArgumentException if scaleFactor &le; 0.
   */
  public void setScaleFactor(float scaleFactor) {
    if (scaleFactor <= 0 || Float.isInfinite(scaleFactor))
      throw new IllegalArgumentException("factor < 0: " + scaleFactor);
    // reset tracker to avoid memory leak
    deferPainting(!isDeferringPainting());
    deferPainting(!isDeferringPainting());

    this.scaleFactor = scaleFactor;
    cellGeometry.setValidScaleFactor(scaleFactor);
    this.scaleFactor = cellGeometry.getScaleFactor();

    mapToScreenBounds = getMapToScreenBounds();

    if (mapToScreenBounds != null) {
      setSize(mapToScreenBounds.getSize());
      setPreferredSize(mapToScreenBounds.getSize());
    }

    if (currentBounds != null) {
      currentBounds.setSize(-1, -1);
    }

    for (RenderingPlane plane : planes) {
      if (plane == null) {
        continue;
      }

      MapCellRenderer renderer = plane.getRenderer();

      if (renderer != null) {
        renderer.scale(this.scaleFactor);
      }
    }
  }

  /**
   * Returns a list containing all the different levels ('Eressea-Ebenen') this Mapper knows of. The
   * list contains Integer objects stating the level number.
   */
  public List<Integer> getLevels() {
    List<Integer> levels = new LinkedList<Integer>();

    if (getGameData() != null) {
      for (Region r : getGameData().getRegions()) {
        Integer i = Integer.valueOf(r.getCoordinate().getZ());

        if (levels.contains(i) == false) {
          levels.add(i);
        }
      }
    }

    return levels;
  }

  /**
   * Sets the level ('Eressea-Ebene') this Mapper knows of. The list contains Integer objects
   * stating the level number.
   */
  public void setLevel(int level) {
    showLevel = level;
    mapToScreenBounds = getMapToScreenBounds();
    setSize(mapToScreenBounds.getSize());
    setPreferredSize(getSize());

    // activeRegion = null;
    if (currentBounds != null) {
      currentBounds.setSize(-1, -1);
    }

    invalidate();
    repaint();
  }

  /**
   * Returns the level ('Eressea-Ebene') this Mapper actually displays.
   */
  public int getLevel() {
    return showLevel;
  }

  /**
   * Get the selected Regions. The returned map can be empty but is never null.
   */
  public Map<CoordinateID, Region> getSelectedRegions() {
    return selectedRegions;
  }

  /**
   * Get the active region.
   */
  public Region getActiveRegion() {
    return activeRegion;
  }

  /**
   * Returns the bounds of the specified region on this component.
   *
   * @param cell the coordinate of the region to be evaluated.
   * @return the bounds (the upper left corner and the size) of the region cell in component
   *         coordinates.
   */
  public Rectangle getCellRect(CoordinateID cell) {
    Rectangle bounds = null;

    if (cellGeometry != null) {
      bounds = cellGeometry.getCellRect(cell.getX(), cell.getY());
      bounds.translate(-mapToScreenBounds.x, -mapToScreenBounds.y);
    } else {
      Mapper.log.warn("Mapper.getCellRect(): Unable to determine cell bounds!");
    }

    return bounds;
  }

  /**
   * Returns the coordinate of the region that is at the center of the currently displayed area.
   *
   * @param clipBounds the bounds indicating which part of the mappers drawing area is actually
   *          visible.
   */
  public CoordinateID getCenter(Rectangle clipBounds) {
    CoordinateID center = null;

    if (mapToScreenBounds != null) {
      Point centerScreen =
          new Point(mapToScreenBounds.x + clipBounds.x + (clipBounds.width / 2),
              mapToScreenBounds.y + clipBounds.y + (clipBounds.height / 2));

      if (cellGeometry != null) {
        center = cellGeometry.getCoordinate(centerScreen.x, centerScreen.y, showLevel);
      } else {
        Mapper.log.warn("Mapper.getCenter(): Unable to determine drawing area!");
      }
    }

    return center;
  }

  /**
   * Returns the location (upper left corner) of the drawing area so that a certain region is at the
   * center of the view port.
   *
   * @param viewSize the size of the mappers viewport, i.e. the size of the part of the mappers
   *          drawing area that is actually visible.
   * @param center the coordinate to center on.
   * @return a Point with x and y so that a view port of size viewSize is centered over the
   *         specified region center.
   */
  public Point getCenteredViewPosition(Dimension viewSize, CoordinateID center) {
    Point viewPos = null;

    if ((cellGeometry != null) && (viewSize != null) && (center != null)) {
      // get the cell position as relative screen coordinates
      Rectangle cellPos = cellGeometry.getCellRect(center.getX(), center.getY());

      // transform cell position into absolute screen coordinates on this
      // component
      cellPos.translate(-mapToScreenBounds.x, -mapToScreenBounds.y);

      // shift the cell position by half a cell size to get to its center
      cellPos
          .translate(cellGeometry.getCellSize().width / 2, cellGeometry.getCellSize().height / 2);

      // now get the view port
      viewPos = new Point(cellPos.x - (viewSize.width / 2), cellPos.y - (viewSize.height / 2));
    } else {
      Mapper.log.warn("Mapper.getCenteredViewPosition(): Unable to determine drawing area!");
    }

    return viewPos;
  }

  /**
   * Get the cell geometry from the resources and make all renderers that use images reload the
   * graphics files.
   */
  public void reloadGraphicSet() {
    cellGeometry = new CellGeometry("cellgeometry.txt");
    if (getGameData() != null) {
      cellGeometry.setCoordMapper(getGameData().getGameSpecificStuff().getCoordMapper());
    }

    for (RenderingPlane plane : planes) {
      if ((plane != null) && (plane.getRenderer() != null)) {
        plane.getRenderer().setCellGeometry(cellGeometry);
      }
    }

    setScaleFactor(getScaleFactor());
    repaint();
  }

  /**
   * Creates a preferences panel allowing to configure this component.
   */
  public PreferencesAdapter getPreferencesAdapter() {
    return new MapperPreferences(this, true);
  }

  /**
   * Returns whether deferred painting after loading and scaling images is used or not. Together
   * with the ImageCellRenderer class this option tells the mapper/renderer whether to scale images
   * synchronously and introduce delays on painting or to scale images asynchronously and trigger a
   * redraw after a short amount of time.
   */
  public boolean isDeferringPainting() {
    return deferredPainting;
  }

  /**
   * Activates or de-activates deferred painting after loading and scaling images. Together with the
   * ImageCellRenderer class this option tells the mapper/renderer whether to scale images
   * synchronously and introduce delays on painting or to scale images asynchronously and trigger a
   * redraw after a short amount of time.
   */
  public void deferPainting(boolean bool) {
    if (bool != isDeferringPainting()) {
      deferredPainting = bool;
      tracker = new MediaTracker(this);
      defer = true;
      ImageCellRenderer.setTracker(tracker);
      // settings.setProperty("Mapper.deferPainting", String.valueOf(bool));
    }
  }

  /**
   * Returns a rectangle that indicates the offset and the size of the whole map that is formed by
   * data.regions(). The values returned are given in pixels as returned by CellGeometry, i.e. if
   * there is only region 0, 0 then the returned rectangle would be : x=0, y=0, width=cellwidth,
   * height=cellheight.
   */
  private Rectangle getMapToScreenBounds() {
    if ((getGameData() == null) || (cellGeometry == null))
      return null;

    if (getGameData().getRegions().size() == 0)
      return new Rectangle(0, 0, 1, 1);

    Point upperLeft = new Point(Integer.MAX_VALUE, Integer.MAX_VALUE);
    Point lowerRight = new Point(Integer.MIN_VALUE, Integer.MIN_VALUE);

    for (Region r : getGameData().getRegions()) {
      CoordinateID c = r.getCoordinate();
      if (c.getZ() == showLevel) {
        int x = cellGeometry.getCellPositionX(c.getX(), c.getY());
        int y = cellGeometry.getCellPositionY(c.getX(), c.getY());
        upperLeft.x = Math.min(x, upperLeft.x);
        upperLeft.y = Math.min(y, upperLeft.y);
        lowerRight.x = Math.max(x, lowerRight.x);
        lowerRight.y = Math.max(y, lowerRight.y);
      }
    }
    if (lowerRight.x == Integer.MIN_VALUE || lowerRight.y == Integer.MIN_VALUE
        || upperLeft.x == Integer.MAX_VALUE || upperLeft.y == Integer.MAX_VALUE) {
      lowerRight = new Point(-1, 1);
      upperLeft = new Point(-1, 1);
    }

    // provide a small border...
    int bordersize = 10;
    upperLeft.translate(cellGeometry.getCellSize().width * bordersize * -1, cellGeometry
        .getCellSize().height
        * bordersize * -1);
    lowerRight.translate(cellGeometry.getCellSize().width * bordersize,
        cellGeometry.getCellSize().height * bordersize);

    lowerRight.x += (cellGeometry.getCellSize().width + 1);
    lowerRight.y += (cellGeometry.getCellSize().height + 1);

    return new Rectangle(upperLeft, new Dimension(lowerRight.x - upperLeft.x, lowerRight.y
        - upperLeft.y));
  }

  protected RenderingPlane[] initRenderingPlanes() {
    RenderingPlane p[] = new RenderingPlane[Mapper.PLANES];
    p[Mapper.PLANE_BACKGROUND] =
        new RenderingPlane(Mapper.PLANE_BACKGROUND, Resources
            .get("map.mapper.plane.background.name"), RenderingPlane.ALWAYS_ONCE);
    p[Mapper.PLANE_BACKGROUND].setRenderer(getRenderer(settings.getProperty("Mapper.Planes."
        + Mapper.PLANE_STRINGS[Mapper.PLANE_BACKGROUND], BackgroundImageRenderer.class.getName())));

    p[Mapper.PLANE_BEHIND] =
        new RenderingPlane(Mapper.PLANE_BEHIND, Resources.get("map.mapper.plane.behind.name"));
    p[Mapper.PLANE_BEHIND].setRenderer(getRenderer(settings.getProperty("Mapper.Planes."
        + Mapper.PLANE_STRINGS[Mapper.PLANE_BEHIND], EresseaSchemesCellRenderer.class.getName())));

    p[Mapper.PLANE_REGION] =
        new RenderingPlane(Mapper.PLANE_REGION, Resources.get("map.mapper.plane.region.name"));
    p[Mapper.PLANE_REGION].setRenderer(getRenderer(settings.getProperty("Mapper.Planes."
        + Mapper.PLANE_STRINGS[Mapper.PLANE_REGION], RegionImageCellRenderer.class.getName())));

    p[Mapper.PLANE_BORDER] =
        new RenderingPlane(Mapper.PLANE_BORDER, Resources.get("map.mapper.plane.border.name"));
    p[Mapper.PLANE_BORDER].setRenderer(getRenderer(settings.getProperty("Mapper.Planes."
        + Mapper.PLANE_STRINGS[Mapper.PLANE_BORDER], BorderCellRenderer.class.getName())));

    p[Mapper.PLANE_BUILDING] =
        new RenderingPlane(Mapper.PLANE_BUILDING, Resources.get("map.mapper.plane.building.name"));
    p[Mapper.PLANE_BUILDING].setRenderer(getRenderer(settings.getProperty("Mapper.Planes."
        + Mapper.PLANE_STRINGS[Mapper.PLANE_BUILDING], BuildingCellRenderer.class.getName())));

    p[Mapper.PLANE_SHIP] =
        new RenderingPlane(Mapper.PLANE_SHIP, Resources.get("map.mapper.plane.ship.name"));
    p[Mapper.PLANE_SHIP].setRenderer(getRenderer(settings.getProperty("Mapper.Planes."
        + Mapper.PLANE_STRINGS[Mapper.PLANE_SHIP], ShipCellRenderer.class.getName())));

    p[Mapper.PLANE_TEXT] =
        new RenderingPlane(Mapper.PLANE_TEXT, Resources.get("map.mapper.plane.text.name"));
    p[Mapper.PLANE_TEXT].setRenderer(getRenderer(settings.getProperty("Mapper.Planes."
        + Mapper.PLANE_STRINGS[Mapper.PLANE_TEXT], TextCellRenderer.class.getName())));

    p[Mapper.PLANE_PATH] =
        new RenderingPlane(Mapper.PLANE_PATH, Resources.get("map.mapper.plane.path.name"),
            RenderingPlane.ACTIVE_OBJECT);
    p[Mapper.PLANE_PATH].setRenderer(getRenderer(settings.getProperty("Mapper.Planes."
        + Mapper.PLANE_STRINGS[Mapper.PLANE_PATH], PathCellRenderer.class.getName())));

    p[Mapper.PLANE_HIGHLIGHT] =
        new RenderingPlane(Mapper.PLANE_HIGHLIGHT,
            Resources.get("map.mapper.plane.highlight.name"), RenderingPlane.VISIBLE_REGIONS
                | RenderingPlane.ACTIVE_OR_SELECTED);
    p[Mapper.PLANE_HIGHLIGHT]
        .setRenderer(getRenderer(settings.getProperty("Mapper.Planes."
            + Mapper.PLANE_STRINGS[Mapper.PLANE_HIGHLIGHT], HighlightImageCellRenderer.class
                .getName())));

    p[Mapper.PLANE_MARKINGS] =
        new RenderingPlane(Mapper.PLANE_MARKINGS, Resources.get("map.mapper.plane.markings.name"),
            RenderingPlane.VISIBLE_REGIONS | RenderingPlane.TAGGED_REGIONS);
    p[Mapper.PLANE_MARKINGS].setRenderer(getRenderer(settings.getProperty("Mapper.Planes."
        + Mapper.PLANE_STRINGS[Mapper.PLANE_MARKINGS], MarkingsImageCellRenderer.class.getName())));

    p[Mapper.PLANE_SCHEMES] =
        new RenderingPlane(Mapper.PLANE_SCHEMES, Resources.get("map.mapper.plane.schemes.name"),
            RenderingPlane.VISIBLE_REGIONS);
    p[Mapper.PLANE_SCHEMES].setRenderer(getRenderer(SchemeCellRenderer.class.getName()));

    p[Mapper.PLANE_SIGNS] =
        new RenderingPlane(Mapper.PLANE_SIGNS, Resources.get("map.mapper.plane.signs.name"));
    p[Mapper.PLANE_SIGNS].setRenderer(getRenderer(SignTextCellRenderer.class.getName()));

    return p;
  }

  private Collection<MapCellRenderer> initAvailableRenderers(CellGeometry geo,
      Properties aSettings, Collection<MapCellRenderer> cRenderers) {
    Collection<MapCellRenderer> renderers = new LinkedList<MapCellRenderer>();
    renderers.add(new RegionImageCellRenderer(geo, context));
    renderers.add(new RegionShapeCellRenderer(geo, context));
    renderers.add(arr = new AdvancedRegionShapeCellRenderer(geo, context));
    renderers.add(new BorderCellRenderer(geo, context));
    renderers.add(new BuildingCellRenderer(geo, context));
    renderers.add(new ShipCellRenderer(geo, context));
    renderers.add(new TextCellRenderer(geo, context));
    renderers.add(new TradeTextCellRenderer(geo, context));
    renderers.add(atr = new AdvancedTextCellRenderer(geo, context));
    renderers.add(new PathCellRenderer(geo, context));
    renderers.add(new HighlightImageCellRenderer(geo, context));
    renderers.add(new HighlightShapeCellRenderer(geo, context));
    renderers.add(new MarkingsImageCellRenderer(geo, context));
    renderers.add(new SchemeCellRenderer(geo, context));
    renderers.add(new SignTextCellRenderer(geo, context));
    renderers.add(new EresseaSchemesCellRenderer(geo, context));
    renderers.add(new BackgroundImageRenderer(geo, context));

    if (cRenderers != null) {
      for (MapCellRenderer map : cRenderers) {
        if (map instanceof HexCellRenderer) {
          ((HexCellRenderer) map).settings = aSettings;
        }

        map.setCellGeometry(geo);
        renderers.add(map);
      }
    }

    // look for Mapper-aware renderers. Add Mapper if Interface MapperAware is
    // implemented
    for (MapCellRenderer mapCellRenderer : renderers) {
      Object o = mapCellRenderer;

      if (o instanceof MapperAware) {
        ((MapperAware) o).setMapper(this);
      }
    }

    return renderers;
  }

  private MapCellRenderer getRenderer(String className) {
    MapCellRenderer renderer = null;

    if (!className.equals("none")) {
      for (MapCellRenderer r : getAvailableRenderers()) {
        if (r.getClass().getName().equals(className)) {
          renderer = r;

          break;
        }
      }
    }

    return renderer;
  }

  /**
   * Getter for property cellGeometry.
   *
   * @return Value of property cellGeometry.
   */
  public CellGeometry getCellGeometry() {
    return cellGeometry;
  }

  /**
   * Getter for property renderContextChanged.
   *
   * @return Value of property renderContextChanged.
   */
  public boolean isRenderContextChanged() {
    return renderContextChanged;
  }

  /**
   * Setter for property renderContextChanged.
   *
   * @param r New value of property renderContextChanged.
   */
  public static void setRenderContextChanged(boolean r) {
    for (Mapper mapper : instances) {
      mapper.renderContextChanged = r;
    }
  }

  /**
   * Sets if the tooltip should be available
   */
  public void setShowTooltip(boolean b) {
    showTooltip = b;

    if (b) {
      ToolTipManager.sharedInstance().registerComponent(this);
      settings.setProperty("Mapper.showTooltips", "true");
    } else {
      ToolTipManager.sharedInstance().unregisterComponent(this);
      settings.setProperty("Mapper.showTooltips", "false");
    }
  }

  /**
   * True if tooltips are enabled
   */
  public boolean isShowingTooltip() {
    return showTooltip;
  }

  /**
   * Returns the tooltip definition
   */
  public String[] getTooltipDefinition() {
    return tooltipDefinitionStrings;
  }

  /**
   * Sets the tooltip definition
   */
  public void setTooltipDefinition(String name, String tdef) {
    tooltipDefinitionStrings[0] = name;
    tooltipDefinitionStrings[1] = tdef;
    tooltipDefinition = ReplacerHelp.createReplacer(tdef);
    settings.setProperty("Mapper.ToolTip.Definition", name + "~" + tdef);
  }

  /**
   * Returns a list of available tooltip definitions
   */
  public List<String> getAllTooltipDefinitions() {
    String s = settings.getProperty("Mapper.ToolTip.Definitions", Mapper.DEFAULT_TOOLTIP);
    StringTokenizer st = new StringTokenizer(s, "~");
    int j = st.countTokens();

    if ((j % 2) != 0) {
      j--;
    }

    List<String> al = new ArrayList<String>(j);

    if (st.countTokens() > 1) {
      for (int i = 0; i < (j / 2); i++) {
        al.add(st.nextToken());
        al.add(st.nextToken());
      }
    }

    return al;
  }

  /**
   * Sets a list of available tooltip definitions
   */
  public void setAllTooltipDefinitions(List<?> l) {
    StringBuffer buf = new StringBuffer();

    if (l.size() > 1) {
      Iterator<?> it = l.iterator();

      for (int i = 0; i < (l.size() / 2); i++) {
        buf.append(it.next());
        buf.append('~');
        buf.append(it.next());

        if (i < (l.size() - 1)) {
          buf.append('~');
        }
      }
    } else {
      buf.append(Mapper.DEFAULT_TOOLTIP);
    }

    settings.setProperty("Mapper.ToolTip.Definitions", buf.toString());

    conMenu.updateTooltips(this);
  }

  /**
   * Adds a new tooltip defintion
   */
  public void addTooltipDefinition(String name, String def) {
    settings.setProperty("Mapper.ToolTip.Definitions", settings.getProperty(
        "Mapper.ToolTip.Definitions", Mapper.DEFAULT_TOOLTIP)
        + "~" + name + "~" + def);

    conMenu.updateTooltips(this);
  }

  /*
   * desktop init methods NOTE: Since the mapper panel is registered as the MAP component, these
   * methods are not called by the desktop manager for the normal map. They have to be delegated to
   * this. But the MINIMAP component uses this feature itself since it is kind of a stand-alone
   * although it is managed by the mapper panel.
   */

  /**
   * Returns the current configuration of this mapper panel. The current implementation divides all
   * the information by "_". First the scale factor is stored, then planes(plane index, renderer
   * class name, renderer configuration).
   */
  public String getComponentConfiguration() {
    Iterator<RenderingPlane> it = getPlanes().iterator();
    StringBuffer buf = new StringBuffer();
    buf.append(getScaleFactor());
    buf.append('_');

    while (it.hasNext()) {
      RenderingPlane rp = it.next();

      if ((rp == null) || (rp.getRenderer() == null)) {
        continue;
      }

      buf.append(rp.getIndex());
      buf.append('_');
      buf.append(((Object) rp.getRenderer()).getClass().getName());
      buf.append('_');

      if (rp.getRenderer() instanceof Initializable) {
        String config = ((Initializable) rp.getRenderer()).getComponentConfiguration();

        if ((config == null) || (config.length() < 1)) {
          buf.append("NI");
        } else {
          buf.append(config);
        }
      } else {
        buf.append("NI");
      }

      if (it.hasNext()) {
        buf.append('_');
      }
    }

    return buf.toString();
  }

  /**
   * Implemented for interface {@link Initializable} to set configuration data to this component.
   *
   * @param p1 the configuration string from magellan_desktop.ini
   */
  public void initComponent(String p1) {
    if ((p1 == null) || (p1.length() == 0))
      return;

    StringTokenizer st = new StringTokenizer(p1, "_");

    float newFactor = 1;
    try {
      newFactor = Float.parseFloat(st.nextToken());
    } catch (Exception exc) {
      newFactor = 1f;
    }
    if (newFactor <= 0) {
      Mapper.log.warn("scale factor <= 0: " + newFactor);
      newFactor = 1f;
    }

    setScaleFactor(newFactor);

    while (st.hasMoreTokens()) {
      try {
        String index = st.nextToken();
        int iindex = Integer.parseInt(index);
        String className = st.nextToken();
        String config = st.nextToken();
        Collection<MapCellRenderer> col = getRenderers(iindex);

        if (col != null) {
          Iterator<MapCellRenderer> it = col.iterator();

          while (it.hasNext()) {
            MapCellRenderer mcp = it.next();

            if (className.equals(((Object) mcp).getClass().getName())) {
              setRenderer(mcp, iindex);

              if ((config != null) && !config.equals("NI") && (mcp instanceof Initializable)) {
                ((Initializable) mcp).initComponent(config);
              }

              break;
            }
          }
        }
      } catch (Exception exc) {
        // just try to continue
        log.info(exc);
      }
    }
  }

  /**
   * Returns the current ATR.
   */
  public AdvancedTextCellRenderer getATR() {
    return atr;
  }

  /**
   * Returns the current ARR.
   */
  public AdvancedRegionShapeCellRenderer getARR() {
    return arr;
  }

  /**
   * @see magellan.library.event.UnitChangeListener#unitChanged(magellan.library.event.UnitChangeEvent)
   */
  public void unitChanged(UnitChangeEvent event) {
    // we need this for paths
    if (activeObject == event.getUnit()) {
      repaint();
    }
  }

  /**
   * @param use
   */
  public void setUseSeasonImages(boolean use) {
    useSeasonImages = use;
    // we use a property of border cell renderer for historical reasons...
    PropertiesHelper.setBoolean(context.getProperties(),
        PropertiesHelper.BORDERCELLRENDERER_USE_SEASON_IMAGES, use);
  }

  /**
   * Returns the value of useSeasonImages.
   *
   * @return Returns useSeasonImages.
   */
  public boolean isUseSeasonImages() {
    return useSeasonImages;
  }

  public void setID(String id) {
    this.id = id;
  }

  public String getID() {
    return id;
  }

}
