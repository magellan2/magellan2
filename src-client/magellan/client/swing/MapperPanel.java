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

package magellan.client.swing;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Container;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.InputEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSlider;
import javax.swing.JViewport;
import javax.swing.KeyStroke;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import magellan.client.MagellanContext;
import magellan.client.desktop.DesktopEnvironment;
import magellan.client.desktop.ExtendedShortcutListener;
import magellan.client.desktop.Initializable;
import magellan.client.desktop.ShortcutListener;
import magellan.client.event.SelectionEvent;
import magellan.client.event.SelectionListener;
import magellan.client.swing.map.CellGeometry;
import magellan.client.swing.map.HexCellRenderer;
import magellan.client.swing.map.MapCellRenderer;
import magellan.client.swing.map.Mapper;
import magellan.client.swing.map.Minimapper;
import magellan.client.swing.map.RegionImageCellRenderer;
import magellan.client.swing.preferences.ExtendedPreferencesAdapter;
import magellan.client.swing.preferences.PreferencesAdapter;
import magellan.client.swing.preferences.PreferencesFactory;
import magellan.library.CoordinateID;
import magellan.library.HasRegion;
import magellan.library.HotSpot;
import magellan.library.ID;
import magellan.library.IntegerID;
import magellan.library.Island;
import magellan.library.Region;
import magellan.library.event.GameDataEvent;
import magellan.library.utils.MagellanFactory;
import magellan.library.utils.Resources;
import magellan.library.utils.logging.Logger;

/**
 * A panel holding all UI components related to an Eressea map. The component
 * contains a Mapper object and additional controls. The policy of this class is
 * not to be concerned with map details like coordinates etc. as much as
 * possible and to provide a general and flexible interface to the mapper.
 */
public class MapperPanel extends InternationalizedDataPanel implements ActionListener, SelectionListener, ChangeListener, ExtendedShortcutListener, PreferencesFactory, Initializable {
  private static final Logger log = Logger.getInstance(MapperPanel.class);

  /** fpr 3 step zoom in and zoom out our 3 scalings */
  private final float level3Scale1 = 0.4f;
  private final float level3Scale2 = 1.3f;
  private final float level3Scale3 = 2.2f;
  
  
  /** The map component in this panel. */
  private Mapper mapper = null;
  private JScrollPane scpMapper = null;
  private JLabel lblLevel = null;
  private JLabel lblScaling = null;
  private JComboBox cmbLevel = null;
  private JSlider sldScaling = null;
  private JComboBox cmbHotSpots = null;
  private Timer timer = null;
  private Point dragStart = null;
  private boolean dragValidated = false;

  // minimap components
  protected Minimapper minimap;
  protected JScrollPane minimapPane;
  private CellGeometry minimapGeometry;
  protected boolean resizeMinimap;
  protected MinimapScaler minimapScaler;
  protected float lastScale = 1;

  // shortcuts
  private List<KeyStroke> shortcuts;
  private TooltipShortcut tooltipShortcut;

  protected MagellanContext context;

  /**
   * GameData event handler.
   * 
   * @param e
   *          DOCUMENT-ME
   */
  public void gameDataChanged(GameDataEvent e) {
    data = e.getGameData();
    mapper.gameDataChanged(e);
    minimap.gameDataChanged(e);
    
    boolean showNavi = context.getProperties().getProperty("MapperPannel.Details.showNavigation", "true").equals("true");
    
    lblScaling.setVisible(showNavi);
    sldScaling.setVisible(showNavi);
    List levels = mapper.getLevels();
    lblLevel.setVisible((levels.size() > 1) && showNavi);
    cmbLevel.setVisible((levels.size() > 1) && showNavi);
    cmbLevel.removeAllItems();

    for (int i = 0; i < levels.size(); i++) {
      cmbLevel.addItem(levels.get(i));
    }

    if (cmbLevel.getItemCount() > 0) {
      cmbLevel.setSelectedIndex(0);
    }

    // fill hot spot combo
    cmbHotSpots.removeAllItems();

    if ((data != null) && (data.hotSpots() != null)) {
      List<HotSpot> hotSpots = new LinkedList<HotSpot>(data.hotSpots().values());
      Collections.sort(hotSpots, new Comparator<HotSpot>() {
        public int compare(HotSpot o1, HotSpot o2) {
          return o1.getName().compareTo(o2.getName());
        }
      });
      cmbHotSpots.setModel(new DefaultComboBoxModel(hotSpots.toArray()));
    }

    cmbHotSpots.setVisible((cmbHotSpots.getItemCount() > 0) && showNavi);

    rescale();
    minimapPane.doLayout();
    minimapPane.repaint();
  }

  /**
   * Selection event handler, updating the map if a new region is selected.
   * 
   * @param se
   *          DOCUMENT-ME
   */
  public void selectionChanged(SelectionEvent se) {
    if (log.isDebugEnabled()) {
      log.debug("MapperPanel.selectionChanged called with " + se.getActiveObject());
    }

    // update the currently selected item in the level combo box
    Object o = se.getActiveObject();
    CoordinateID newCenter = null;

    if (o != null) {
      Region newCenterRegion = null;

      if (o instanceof Region) {
        newCenterRegion = (Region) o;
      } else if (o instanceof HasRegion) {
        newCenterRegion = ((HasRegion) o).getRegion();
      }

      if (newCenterRegion != null) {
        newCenter = newCenterRegion.getCoordinate();
      }
    }

    if (newCenter != null) {
      if (cmbLevel.isVisible()) {
        Integer level = new Integer(newCenter.z);

        if (level.intValue() != mapper.getLevel()) {
          cmbLevel.setSelectedItem(level);
        }
      }

      // re-center the map if necessary
      // do this later, mapper probably does not
      // yet know the right active region
      class CenterRunner implements Runnable {
        /** DOCUMENT-ME */
        public CoordinateID center = null;

        /**
         * Creates a new CenterRunner object.
         * 
         * @param c
         *          DOCUMENT-ME
         */
        public CenterRunner(CoordinateID c) {
          center = c;
        }

        /**
         * DOCUMENT-ME
         */
        public void run() {
          if (log.isDebugEnabled()) {
            log.debug("MapperPanel.selectionChanged: Running CenterRunner on " + center);
          }

          Rectangle cellRect = mapper.getCellRect(center);

          if (cellRect != null) {
            if (!scpMapper.getViewport().getViewRect().contains(cellRect)) {
              setCenter(center);
            }
          }

          cellRect = minimap.getCellRect(center);

          if (cellRect != null) {
            if (!minimapPane.getViewport().getViewRect().contains(cellRect)) {
              setMinimapCenter(center);
            }
          }
        }
      }
      SwingUtilities.invokeLater(new CenterRunner(newCenter));
    } else if ((o != null) && o instanceof Island) {
      // center to island
      Island island = (Island) o;

      if (!island.regions().isEmpty()) {
        // first set right level
        Region r = (Region) island.regions().iterator().next();
        CoordinateID coord = r.getCoordinate();

        if (cmbLevel.isVisible()) {
          Integer level = new Integer(coord.z);

          if (level.intValue() != mapper.getLevel()) {
            cmbLevel.setSelectedItem(level);
          }
        }

        // then set center rectangle on right pane
        class ParamRunnable implements Runnable {
          Island island;

          ParamRunnable(Island i) {
            island = i;
          }

          /**
           * DOCUMENT-ME
           */
          public void run() {
            Rectangle islandBounds = null;

            for (Iterator iter = island.regions().iterator(); iter.hasNext();) {
              Region r = (Region) iter.next();
              CoordinateID coord = r.getCoordinate();

              if (islandBounds == null) {
                islandBounds = mapper.getCellRect(coord);
              } else {
                islandBounds.add(mapper.getCellRect(coord));
              }
            }

            Rectangle centerRect = islandBounds;

            if (log.isDebugEnabled()) {
              log.debug("MapperPanel.selectionChanged: Running ParamRunnable with " + centerRect);
            }

            if (!scpMapper.getViewport().getViewRect().contains(centerRect)) {
              /* FIX these numbers should get some bounding */
              centerRect.x -= ((scpMapper.getViewport().getViewRect().getWidth() - centerRect.getWidth()) / 2);
              centerRect.y -= ((scpMapper.getViewport().getViewRect().getHeight() - centerRect.getHeight()) / 2);
              scpMapper.getViewport().setViewPosition(centerRect.getLocation());
            }
          }
        }
        SwingUtilities.invokeLater(new ParamRunnable(island));
      }
    }
  }

  /** Action event handler for timer events related to the scaling slider. */
  Cursor waitCursor = new Cursor(Cursor.WAIT_CURSOR);
  Cursor defaultCursor = new Cursor(Cursor.DEFAULT_CURSOR);

  /**
   * DOCUMENT-ME
   * 
   * @param ae
   *          DOCUMENT-ME
   */
  public void actionPerformed(ActionEvent ae) {
    setCursor(waitCursor);
    mapper.setCursor(waitCursor);

    CoordinateID center = mapper.getCenter(scpMapper.getViewport().getViewRect());
    mapper.setScaleFactor((float) ((sldScaling.getValue() / 50.0) + 0.3));
    setCenter(center);
    this.repaint();
    setCursor(defaultCursor);
    mapper.setCursor(defaultCursor);
  }

  /**
   * Creates a new <tt>MapperPanel</tt> object.
   */
  public MapperPanel(MagellanContext context, Collection<MapCellRenderer> customRenderers, CellGeometry geo) {
    super(context.getEventDispatcher(), context.getProperties());
    this.context = context;

    // final MapperPanel thisMapperPanel = this;
    initMinimap();

    context.getEventDispatcher().addSelectionListener(this);

    setLayout(new BorderLayout());
    add(getMainPane(customRenderers, geo), BorderLayout.CENTER);

    // register own mouse listener for letting the user drag the
    // map
    mapper.addMouseListener(new MouseAdapter() {
      public void mousePressed(MouseEvent e) {
        dragStart = e.getPoint();
        dragValidated = false;
      }

      public void mouseReleased(MouseEvent e) {
        if (((e.getModifiers() & InputEvent.BUTTON3_MASK) != 0) || !dragValidated || ((e.getModifiers() & InputEvent.CTRL_MASK) != 0)) {
          return;
        }

        Rectangle bounds = new Rectangle(dragStart.x - 2, dragStart.y - 2, 4, 4);

        if (bounds.contains(e.getPoint())) {
          return;
        }

        JViewport viewport = scpMapper.getViewport();
        Point viewPos = viewport.getViewPosition();
        dragStart.translate(-e.getPoint().x, -e.getPoint().y);
        dragStart.translate(viewPos.x, viewPos.y);

        if (dragStart.x < 0) {
          dragStart.x = 0;
        } else {
          int maxX = mapper.getWidth() - viewport.getWidth();

          if (dragStart.x > maxX) {
            dragStart.x = maxX;
          }
        }

        if (dragStart.y < 0) {
          dragStart.y = 0;
        } else {
          int maxY = mapper.getHeight() - viewport.getHeight();

          if (dragStart.y > maxY) {
            dragStart.y = maxY;
          }
        }

        viewport.setViewPosition(dragStart);
      }
    });

    // use a mouse motion listener to confirm that the user actually moved the
    // mouse while dragging
    // to avoid unintended drag effects
    mapper.addMouseMotionListener(new MouseMotionAdapter() {
      public void mouseDragged(MouseEvent e) {
        if (!dragValidated) {
          Rectangle bounds = new Rectangle(dragStart.x - 2, dragStart.y - 2, 4, 4);

          if (!bounds.contains(e.getPoint())) {
            dragValidated = true;
          }
        }
      }
    });

    // initialize Shortcuts
    tooltipShortcut = new TooltipShortcut();

    shortcuts = new ArrayList<KeyStroke>(10);
    // 0: request Focus
    shortcuts.add(KeyStroke.getKeyStroke(KeyEvent.VK_2, KeyEvent.CTRL_MASK));
    // 1: request Focus
    shortcuts.add(KeyStroke.getKeyStroke(KeyEvent.VK_2, KeyEvent.ALT_MASK));
    // 2: add Hotspot
    shortcuts.add(KeyStroke.getKeyStroke(KeyEvent.VK_H, KeyEvent.CTRL_MASK));
    // 3: remove Hotspot
    shortcuts.add(KeyStroke.getKeyStroke(KeyEvent.VK_H, KeyEvent.CTRL_MASK | KeyEvent.SHIFT_MASK));
    // 4: fog of war
    shortcuts.add(KeyStroke.getKeyStroke(KeyEvent.VK_W, KeyEvent.CTRL_MASK));
    // 5: tooltips ? open open problems
    shortcuts.add(KeyStroke.getKeyStroke(KeyEvent.VK_P, KeyEvent.CTRL_MASK));
    
    // 6,7: Map Zoom in  First is numpad, scnd is normal key
    shortcuts.add(KeyStroke.getKeyStroke(KeyEvent.VK_ADD  , KeyEvent.CTRL_MASK));
    shortcuts.add(KeyStroke.getKeyStroke(KeyEvent.VK_PLUS  , KeyEvent.CTRL_MASK));
    //  8,9: Map Zoom out  
    shortcuts.add(KeyStroke.getKeyStroke(KeyEvent.VK_SUBTRACT , KeyEvent.CTRL_MASK));
    shortcuts.add(KeyStroke.getKeyStroke(KeyEvent.VK_MINUS , KeyEvent.CTRL_MASK));
    
    // 3 Level Zoom in
    // 10 Fast Zoom In
    shortcuts.add(KeyStroke.getKeyStroke(KeyEvent.VK_PAGE_DOWN , KeyEvent.CTRL_MASK));
    // 11 Fast Zoom out
    shortcuts.add(KeyStroke.getKeyStroke(KeyEvent.VK_PAGE_UP , KeyEvent.CTRL_MASK));

    DesktopEnvironment.registerShortcutListener(this);
  }

  /**
   * Creates the Minimap Panel.
   */
  protected void initMinimap() {
    minimap = new Minimapper(context);
    minimapGeometry = minimap.getCellGeometry();

    Dimension d = minimapGeometry.getCellSize();
    int size = 10;

    try {
      size = Integer.parseInt(settings.getProperty("Minimap.Scale"));
    } catch (Exception exc) {
    }

    lastScale = size / (float) d.width;
    minimap.setScaleFactor(lastScale);
    minimapPane = new JScrollPane(minimap);

    // ClearLook suggests to remove border
    minimapPane.setBorder(new EmptyBorder(0, 0, 0, 0));

    resizeMinimap = settings.getProperty("Minimap.AutoScale", "false").equals("true");
    minimapScaler = new MinimapScaler();
    minimapPane.addComponentListener(minimapScaler);
    
    String minimapRenderClassName = settings.getProperty("Minimap.Renderer");
    if (minimapRenderClassName != null && minimap.getAvailableRenderers() != null) {
      for (MapCellRenderer renderer : minimap.getAvailableRenderers()) {
        if (renderer.getClass().getName().equals(minimapRenderClassName)) {
          minimap.setRenderer(renderer);
        }
      }
    }
  }

  /**
   * DOCUMENT-ME
   * 
   * @param scale
   *          DOCUMENT-ME
   */
  public void setMinimapScale(int scale) {
    Dimension size = minimapGeometry.getCellSize();
    minimap.setScaleFactor(((float) scale * minimapGeometry.getScaleFactor()) / (float) size.width);
    minimapPane.doLayout();
    minimapPane.repaint();
  }

  /**
   * DOCUMENT-ME
   * 
   * 
   */
  public int getMinimapScale() {
    return minimapGeometry.getCellSize().width;
  }

  /**
   * DOCUMENT-ME
   * 
   * @param bool
   *          DOCUMENT-ME
   */
  public void setAutoScaling(boolean bool) {
    resizeMinimap = bool;
    settings.setProperty("Minimap.AutoScale", bool ? "true" : "false");

    if (bool) {
      rescale();
    }
  }

  /**
   * DOCUMENT-ME
   * 
   * 
   */
  public boolean isAutoScaling() {
    return resizeMinimap;
  }

  protected void rescale() {
    minimapScaler.componentResized(new ComponentEvent(minimapPane, ComponentEvent.COMPONENT_RESIZED));
  }

  /**
   * DOCUMENT-ME
   */
  public void synchronizeMinimap() {
    minimap.synchronizeColors();
  }

  /**
   * DOCUMENT-ME
   * 
   * @param mode
   *          DOCUMENT-ME
   */
  public void setMinimapMode(int mode) {
    minimap.setPaintMode(mode);
  }

  /**
   * DOCUMENT-ME
   * 
   * 
   */
  public int getMinimapMode() {
    return minimap.getPaintMode();
  }

  /**
   * Sets a new scaling factor for the map.
   * 
   * @param fScale
   *          the new scaling factor, values may range from 0.3 to 2.3.
   */
  public void setScaleFactor(float fScale) {
    fScale = Math.max(0.3f, fScale);
    fScale = Math.min(fScale, (100.0f / 50.0f) + 0.3f);
    sldScaling.setValue((int) ((fScale - 0.3) * 50.0));
    mapper.setScaleFactor(fScale);
  }

  /**
   * sets new scale factor for map, centers on same position and repaints
   * @param fScale
   */
  public void setNewScaleFactor(float fScale) {
    CoordinateID center = mapper.getCenter(scpMapper.getViewport().getViewRect());
    this.setScaleFactor(fScale);
    setCenter(center);
    this.repaint(); 
  }
  
  /**
   * Returns the current scaling factor applied to the map.
   * 
   * 
   */
  public float getScaleFactor() {
    return mapper.getScaleFactor();
  }

  /**
   * Set a cell renderer object for its default rendering plane. See
   * com.eressea.swing.map.Mapper for further reference.
   * 
   * @param renderer
   *          the object responsible for rendering a graphical representation of
   *          regions.
   */
  public void setRenderer(HexCellRenderer renderer) {
    mapper.setRenderer(renderer);
  }

  /**
   * Set a cell renderer object for a certain plane of the map. See
   * com.eressea.swing.map.Mapper for further reference.
   * 
   * @param renderer
   *          the object responsible for rendering a graphical representation of
   *          regions.
   * @param plane
   *          the plane the renderer will draw to. Lower planes are painted over
   *          by higher planes.
   */
  public void setRenderer(HexCellRenderer renderer, int plane) {
    mapper.setRenderer(renderer, plane);
  }

  /**
   * Get the selected Regions. The returned map can be empty but is never null.
   * 
   * 
   */
  public Map<CoordinateID,Region> getSelectedRegions() {
    return mapper.getSelectedRegions();
  }

  /**
   * Get the active region.
   * 
   * 
   */
  public Region getActiveRegion() {
    return mapper.getActiveRegion();
  }

  /**
   * DOCUMENT-ME
   * 
   * 
   */
  public int getLevel() {
    return mapper.getLevel();
  }

  /**
   * DOCUMENT-ME
   * 
   * @param level
   *          DOCUMENT-ME
   */
  public void setLevel(int level) {
    minimap.setLevel(level);
    mapper.setLevel(level);

    // when there was a change from level 1 to level 0
    // i.e. from Astralraum back to normal map we
    // try to intelligently center the map
    if (mapper.getActiveRegion() != null) {
      CoordinateID c = mapper.getActiveRegion().getCoordinate();

      if (c.z == level) {
        setCenter(c);
      } else if ((c.z == 1) && (level == 0)) {
        CoordinateID newCoordinate = new CoordinateID(c.x * 4, c.y * 4, 0);
        setCenter(newCoordinate);
      }
    }
  }

  /**
   * Centers the map on a certain region.
   * 
   * @param center
   *          the coordinate of the region to center the map on.
   */
  public void setCenter(CoordinateID center) {
    Point newViewPosition = mapper.getCenteredViewPosition(scpMapper.getSize(), center);

    if (newViewPosition != null) {
      Dimension size = scpMapper.getViewport().getSize();
      newViewPosition.x = Math.max(0, newViewPosition.x);
      newViewPosition.x = Math.min(Math.max(0, mapper.getWidth() - size.width), newViewPosition.x);
      newViewPosition.y = Math.max(0, newViewPosition.y);
      newViewPosition.y = Math.min(Math.max(0, mapper.getHeight() - size.height), newViewPosition.y);
      scpMapper.getViewport().setViewPosition(newViewPosition);
    }
  }

  /**
   * Centers the minimap on a certain region.
   * 
   * @param center
   *          the coordinate of the region to center the map on.
   */
  public void setMinimapCenter(CoordinateID center) {
    Point newViewPosition = minimap.getCenteredViewPosition(minimapPane.getSize(), center);

    if (newViewPosition != null) {
      Dimension size = minimapPane.getViewport().getSize();
      newViewPosition.x = Math.max(0, newViewPosition.x);
      newViewPosition.x = Math.min(Math.max(0, minimap.getWidth() - size.width), newViewPosition.x);
      newViewPosition.y = Math.max(0, newViewPosition.y);
      newViewPosition.y = Math.min(Math.max(0, minimap.getHeight() - size.height), newViewPosition.y);
      minimapPane.getViewport().setViewPosition(newViewPosition);
    }
  }

  /**
   * Assign the currently visible part of the map (the region at the center), a
   * hot spot, an id and add it to the list of hot spots.
   * 
   * @param name
   *          the id to assign to the hot spot.
   */
  public void assignHotSpot(String name) {
    CoordinateID center = mapper.getCenter(scpMapper.getViewport().getViewRect());

    if (center != null) {
      ID id = getNewHotSpotID();

      if (id == null) {
        log.warn("MapperPanel.assignHotSpot(): unable to determine free id for new hot spot!");

        return;
      }

      HotSpot h = MagellanFactory.createHotSpot(id);
      h.setName(name);
      h.setCenter(center);
      data.setHotSpot(h);

      List<HotSpot> hotSpots = new LinkedList<HotSpot>(data.hotSpots().values());
      Collections.sort(hotSpots, new Comparator<HotSpot>() {
        public int compare(HotSpot o1, HotSpot o2) {
          return o1.getName().compareTo(o2.getName());
        }
      });
      cmbHotSpots.setModel(new DefaultComboBoxModel(hotSpots.toArray()));

      if (cmbHotSpots.getItemCount() != 0) {
        cmbHotSpots.setVisible(true);
      }

      if (cmbHotSpots.getFontMetrics(cmbHotSpots.getFont()).stringWidth(name) > cmbHotSpots.getMinimumSize().width) {
        cmbHotSpots.setMinimumSize(new Dimension(cmbHotSpots.getFontMetrics(cmbHotSpots.getFont()).stringWidth(name), cmbHotSpots.getMinimumSize().height));
      }
    }
  }

  /**
   * Center the map on the specified hot spot.
   * 
   * @param h
   *          the hot spot to move the map to.
   */
  public void showHotSpot(HotSpot h) {
    // switch planes
    if ((mapper.getActiveRegion() == null) || (mapper.getActiveRegion().getCoordinate().z != (((CoordinateID) h.getCenter()).z))) {
      if (cmbLevel.isVisible()) {
        cmbLevel.setSelectedItem(new Integer(((CoordinateID) h.getCenter()).z));
      }
    }

    // re-center mapper
    Point viewPos = mapper.getCenteredViewPosition(scpMapper.getSize(), (CoordinateID) h.getCenter());

    if (viewPos != null) {
      scpMapper.getViewport().setViewPosition(viewPos);
      mapper.requestFocus();
    }
  }

  /**
   * Remove the specified hot spot.
   * 
   * @param h
   *          the hot spot to remove.
   */
  public void removeHotSpot(HotSpot h) {
    data.removeHotSpot(h.getID());
    cmbHotSpots.removeItem(h);

    if (cmbHotSpots.getItemCount() == 0) {
      cmbHotSpots.setVisible(false);
    }
  }

  /**
   * Get the cell geometry from the resources and make all renderers that use
   * images reload the graphics files.
   */
  public void reloadGraphicSet() {
    mapper.reloadGraphicSet();
  }

  /**
   * Stores the region that is at the center of the currently visible area.
   */
  public void quit() {
    settings.setProperty("Map.scaleFactor", Float.toString(getScaleFactor()));

    CoordinateID center = mapper.getCenter(scpMapper.getViewport().getViewRect());

    if (center != null) {
      settings.setProperty("Map.lastCenterRegion", center.toString());
    }

    settings.setProperty("Minimap.Scale", String.valueOf(getMinimapScale()));
  }

  /**
   * Returns the component that draws the map.
   * 
   * 
   */
  public Component getView() {
    return mapper;
  }

  /**
   * Creates random integer values until one is not already used as a key in the
   * game data's hot spot map.
   * 
   * @return an integer the Integer representation of which is not already used
   *         as a key in the current game data's hot spot map.
   */
  private ID getNewHotSpotID() {
    ID i = null;

    do {
      i = IntegerID.create((int) (Math.random() * Integer.MAX_VALUE));
    } while (data.getHotSpot(i) != null);

    return i;
  }

  private Container getMainPane(Collection<MapCellRenderer> renderers, CellGeometry geo) {
    mapper = new Mapper(context, renderers, geo);
    scpMapper = new JScrollPane(mapper);

    // ClearLook suggests to remove border
    scpMapper.setBorder(new EmptyBorder(0, 0, 0, 0));

    JPanel mainPanel = new JPanel(new GridBagLayout());
    GridBagConstraints c = new GridBagConstraints();
    mainPanel.setBorder(new EmptyBorder(2, 2, 2, 2));

    
    
    lblScaling = new JLabel(Resources.get("mapperpanel.lbl.zoom.caption"));
    sldScaling = new JSlider(SwingConstants.HORIZONTAL);
    sldScaling.setMajorTickSpacing(5);
    sldScaling.setPaintTicks(true);
    sldScaling.addChangeListener(new ChangeListener() {
      public void stateChanged(ChangeEvent ce) {
        /*
         * Timer support routine for events related to the scaling slider.
         */
        if (timer == null) {
          timer = new Timer(200, MapperPanel.this);
          timer.setRepeats(false);
        }

        // always restart to prevent refreshing while moving around
        timer.restart();
      }
    });

    sldScaling.addKeyListener(new KeyAdapter() {
      public void keyPressed(KeyEvent e) {
        if ((e.getModifiers() & InputEvent.CTRL_MASK) != 0) {
          sldScaling.setSnapToTicks(true);
        }
      }

      public void keyReleased(KeyEvent e) {
        if ((e.getModifiers() & InputEvent.CTRL_MASK) != 0) {
          sldScaling.setSnapToTicks(false);
        }
      }
    });
    lblScaling.setLabelFor(sldScaling);

    lblLevel = new JLabel(Resources.get("mapperpanel.lbl.level.caption"));
    cmbLevel = new JComboBox(mapper.getLevels().toArray());

    if (cmbLevel.getItemCount() > 0) {
      cmbLevel.setSelectedIndex(0);
    }

    cmbLevel.setMinimumSize(new Dimension(50, 25));
    cmbLevel.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent ae) {
        Integer level = (Integer) ((JComboBox) ae.getSource()).getSelectedItem();

        if (level != null) {
          setLevel(level.intValue());
        }
      }
    });
    lblLevel.setLabelFor(cmbLevel);
    lblLevel.setVisible(cmbLevel.getItemCount() > 1);
    cmbLevel.setVisible(cmbLevel.getItemCount() > 1);

    cmbHotSpots = new JComboBox();

    if ((data != null) && (data.hotSpots() != null)) {
      for (Iterator iter = data.hotSpots().values().iterator(); iter.hasNext();) {
        HotSpot h = (HotSpot) iter.next();
        cmbHotSpots.addItem(h);
      }
    }

    cmbHotSpots.setMinimumSize(new Dimension(50, 25));
    cmbHotSpots.setVisible(cmbHotSpots.getItemCount() != 0);
    cmbHotSpots.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent ae) {
        HotSpot h = (HotSpot) ((JComboBox) ae.getSource()).getSelectedItem();

        if (h != null) {
          showHotSpot(h);
        }
      }
    });

    // initial visibility...all other elements are invisisble at start up anyway
    boolean showNavi = context.getProperties().getProperty("MapperPannel.Details.showNavigation", "true").equals("true");
    sldScaling.setVisible(showNavi);
    lblScaling.setVisible(showNavi);
    
    
    c.anchor = GridBagConstraints.CENTER;
    c.gridx = 0;
    c.gridy = 0;
    c.gridwidth = 1;
    c.gridheight = 1;
    c.fill = GridBagConstraints.NONE;
    c.weightx = 0.0;
    c.weighty = 0.0;
    mainPanel.add(lblScaling, c);

    c.anchor = GridBagConstraints.CENTER;
    c.gridx = 1;
    c.gridy = 0;
    c.gridwidth = 1;
    c.gridheight = 1;
    c.fill = GridBagConstraints.HORIZONTAL;
    c.insets = new Insets(5, 0, 5, 0);
    c.weightx = 0.1;
    c.weighty = 0.0;
    mainPanel.add(sldScaling, c);

    c.anchor = GridBagConstraints.CENTER;
    c.gridx = 2;
    c.gridy = 0;
    c.gridwidth = 1;
    c.gridheight = 1;
    c.fill = GridBagConstraints.NONE;
    c.insets = new Insets(0, 3, 0, 0);
    c.weightx = 0.0;
    c.weighty = 0.0;
    mainPanel.add(lblLevel, c);

    c.anchor = GridBagConstraints.CENTER;
    c.gridx = 3;
    c.gridy = 0;
    c.gridwidth = 1;
    c.gridheight = 1;
    c.fill = GridBagConstraints.NONE;
    c.insets = new Insets(0, 0, 3, 0);
    c.weightx = 0.0;
    c.weighty = 0.0;
    mainPanel.add(cmbLevel, c);

    c.anchor = GridBagConstraints.CENTER;
    c.gridx = 4;
    c.gridy = 0;
    c.gridwidth = 1;
    c.gridheight = 1;
    c.fill = GridBagConstraints.HORIZONTAL;
    c.insets = new Insets(0, 5, 3, 0);
    c.weightx = 0.0;
    c.weighty = 0.0;
    mainPanel.add(cmbHotSpots, c);
  
    c.anchor = GridBagConstraints.CENTER;
    c.gridx = 0;
    c.gridy = 1;
    c.gridwidth = 5;
    c.gridheight = 1;
    c.fill = GridBagConstraints.BOTH;
    c.insets = new Insets(0, 0, 0, 0);
    c.weightx = 0.2;
    c.weighty = 0.2;
    
    mainPanel.add(scpMapper, c);
    

    return mainPanel;
  }

  /**
   * Called when the viewed rect of the main mapper changes. In further
   * implementations a rect of the visible bounds should be displayed in the
   * minimap.
   * 
   * @param p1
   *          DOCUMENT-ME
   */
  public void stateChanged(javax.swing.event.ChangeEvent p1) {
  }

  /**
   * DOCUMENT-ME
   * 
   * 
   */
  public Component getMinimap() {
    return minimapPane;
  }

  /**
   * Should return all short cuts this class want to be informed. The elements
   * should be of type javax.swing.KeyStroke
   * 
   * 
   */
  public Iterator<KeyStroke> getShortCuts() {
    return shortcuts.iterator();
  }

  /**
   * This method is called when a shortcut from getShortCuts() is recognized.
   * 
   * @param shortcut
   *          DOCUMENT-ME
   */
  public void shortCut(javax.swing.KeyStroke shortcut) {
    int index = shortcuts.indexOf(shortcut);

    switch (index) {
    case -1:
      break; // unknown shortcut

    case 0:
    case 1:
      // request FOcus CTRL + 2 or ALT + 2
      magellan.client.desktop.DesktopEnvironment.requestFocus("MAP");
      mapper.requestFocus(); // activate the mapper, not the scrollpane

      break;

    case 2:
      // insert Hotspot CTRL+H
      String input = JOptionPane.showInputDialog(Resources.get("mapperpanel.msg.enterhotspotname.text"));

      if ((input != null) && !input.equals("")) {
        assignHotSpot(input); // just CTRL
      }

      break;

    case 3:
      // remove HotSpot CTRL + ALT + H
      HotSpot h = (HotSpot) cmbHotSpots.getSelectedItem();

      if (h != null) {
        removeHotSpot(h); // SHIFT + CTRL
      }

      break;

    case 4:
      // FoW CTRL + W
      Collection renderers = mapper.getRenderers(Mapper.PLANE_REGION);

      if ((renderers != null) && (renderers.size() > 0)) {
        Object o = renderers.iterator().next();

        if (o instanceof RegionImageCellRenderer) {
          RegionImageCellRenderer r = (RegionImageCellRenderer) o;
          r.setFogOfWar(!r.getFogOfWar());
          Mapper.setRenderContextChanged(true);
          mapper.repaint();
        }
      }

      break;

    case 5:
      // tooltips? open problems are opened CTRL+P
      break;
    
    case 6:
    case 7:
      // Zoom in CTRL + "+"
      float currentSF = this.getScaleFactor();      
      this.setNewScaleFactor(currentSF * 1.33f);
      break;
    case 8:
    case 9:  
      // Zoom out CTRL + "-"
      float currentSF2 = this.getScaleFactor();
      this.setNewScaleFactor(currentSF2 * 0.66f);
      break;
      
    case 10:
      // 3 Step Zoom in CTRL+ PgDown
      if (this.getScaleFactor()<this.level3Scale3){
        float newScale = this.level3Scale3;
        if (this.getScaleFactor()<this.level3Scale2){
          newScale = this.level3Scale2;
        }
        this.setNewScaleFactor(newScale);
      }
      break;
    case 11:
      // 3 Step Zoom out CTRL + PgUP
      if (this.getScaleFactor()>this.level3Scale1){
        float newScale = this.level3Scale1;
        if (this.getScaleFactor()>this.level3Scale2){
          newScale = this.level3Scale2;
        }
        this.setNewScaleFactor(newScale);
      }
      break;
    
    }  
    
    
    
  }

  /**
   * DOCUMENT-ME
   * 
   * 
   */
  public PreferencesAdapter createPreferencesAdapter() {
    return new MapperPanelPreferences(this);
  }

  /**
   * Returns the current configuration of this mapper panel. The current
   * implementation divides all the information by "_". First the scale factor
   * is stored, then planes(plane index, renderer class name, renderer
   * configuration).
   * 
   * 
   */
  public java.lang.String getComponentConfiguration() {
    return mapper.getComponentConfiguration() + ":" + minimap.getComponentConfiguration();
  }

  /**
   * DOCUMENT-ME
   * 
   * @param p1
   *          DOCUMENT-ME
   */
  public void initComponent(java.lang.String p1) {
    if (p1.indexOf(':') >= 0) {
      mapper.initComponent(p1.substring(0, p1.indexOf(':')));
      minimap.initComponent(p1.substring(p1.indexOf(':') + 1));
    } else {
      mapper.initComponent(p1);
    }
  }

  /**
   * Returns the listener responsible for the sub-short-cuts
   * 
   * @param stroke
   *          DOCUMENT-ME
   * 
   */
  public ShortcutListener getExtendedShortcutListener(KeyStroke stroke) {
    return tooltipShortcut;
  }

  /**
   * Returns wether the given stroke is for an extended short-cut.
   * 
   * @param stroke
   *          DOCUMENT-ME
   * 
   */
  public boolean isExtendedShortcut(KeyStroke stroke) {
    int index = shortcuts.indexOf(stroke);

    return (index == 5);
  }

  /**
   * DOCUMENT-ME
   * 
   * @param stroke
   *          DOCUMENT-ME
   * 
   */
  public String getShortcutDescription(Object stroke) {
    int index = shortcuts.indexOf(stroke);

    return Resources.get("mapperpanel.shortcuts.description." + String.valueOf(index));
  }

  /**
   * DOCUMENT-ME
   * 
   * 
   */
  public String getListenerDescription() {
    return Resources.get("mapperpanel.shortcuts.description");
  }

  private class MapperPanelPreferences extends JPanel implements ExtendedPreferencesAdapter {
    protected class MapperPanelDetailPreferences extends JPanel implements PreferencesAdapter {
      private JCheckBox showNavigation;
      
      
      public MapperPanelDetailPreferences() {
        // Anzeige der oberen Leiste?
        showNavigation = new JCheckBox(Resources.get("mapperpanel.prefs.details.chk.shownavigation"), context.getProperties().getProperty("MapperPannel.Details.showNavigation", "true").equals("true"));
        
        this.add(showNavigation);
        
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
        return Resources.get("mapperpanel.prefs.details.title");
      }

      public void initPreferences() {
        // TODO: implement it
      }

      /**
       * DOCUMENT-ME
       */
      public void applyPreferences() {
        if (showNavigation.isSelected()!= context.getProperties().getProperty("MapperPannel.Details.showNavigation", "true").equals("true")){
          // we have a change here
          context.getProperties().setProperty("MapperPannel.Details.showNavigation", showNavigation.isSelected() ? "true" : "false");
          context.getEventDispatcher().fire(new GameDataEvent(this,data));
        }
      }
    }
    protected class MinimapPreferences extends JPanel implements PreferencesAdapter {
      private JSlider sldZoom;
      private JComboBox cmbDisplayMode;
      private JCheckBox autoScale;
      private PreferencesAdapter renderers;

      /**
       * Creates a new MinimapPreferences object.
       */
      public MinimapPreferences() {
        renderers = minimap.getPreferencesAdapter();

        // display mode combo box
        String items[] = new String[5];
        items[0] = Resources.get("mapperpanel.prefs.minimapitems.terrain");
        items[1] = Resources.get("mapperpanel.prefs.minimapitems.politics");
        items[2] = Resources.get("mapperpanel.prefs.minimapitems.allfactions");
        items[3] = Resources.get("mapperpanel.prefs.minimapitems.trustlevel");
        items[4] = Resources.get("mapperpanel.prefs.minimapitems.trustlevelguard");
        cmbDisplayMode = new JComboBox(items);
        cmbDisplayMode.setSelectedIndex(source.getMinimapMode());

        JLabel lblDisplayMode = new JLabel(Resources.get("mapperpanel.prefs.lbl.minimapoptions"));
        lblDisplayMode.setLabelFor(cmbDisplayMode);
        lblDisplayMode.setHorizontalTextPosition(JLabel.CENTER);

        // color synching button
        JButton btnSyncColors = new JButton(Resources.get("mapperpanel.prefs.lbl.synccolors.caption"));
        btnSyncColors.addActionListener(new ActionListener() {
          public void actionPerformed(ActionEvent e) {
            MapperPanel.this.synchronizeMinimap();
          }
        });

        // zoom slider
        sldZoom = new JSlider(1, 25, 10);
        sldZoom.setLabelTable(sldZoom.createStandardLabels(5));
        sldZoom.setMajorTickSpacing(10);
        sldZoom.setMinorTickSpacing(5);
        sldZoom.setPaintLabels(true);
        sldZoom.setPaintTicks(true);
        sldZoom.setValue(getMinimapScale());

        JLabel lblZoom = new JLabel(Resources.get("mapperpanel.prefs.lbl.zoom"));
        lblZoom.setLabelFor(sldZoom);
        lblZoom.setHorizontalTextPosition(JLabel.CENTER);

        // auto scale checkbox
        autoScale = new JCheckBox(Resources.get("mapperpanel.prefs.lbl.minimapautoscale"), source.isAutoScaling());

        // panel grouping minimap stuff
        this.setBorder(new TitledBorder(BorderFactory.createEtchedBorder(), Resources.get("mapperpanel.prefs.border.minimap")));

        this.setLayout(new GridBagLayout());

        GridBagConstraints c = new GridBagConstraints();
        c.anchor = GridBagConstraints.CENTER;
        c.gridx = 0;
        c.gridy = 0;
        c.gridwidth = 4;
        c.gridheight = 1;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.weightx = 0.1;
        c.weighty = 1;
        this.add(renderers.getComponent(), c);

        /*
         * this.add(lblDisplayMode, c); c.anchor = GridBagConstraints.CENTER;
         * c.gridx = 1; c.gridy = 0; c.gridwidth = 2; c.gridheight = 1; c.fill =
         * GridBagConstraints.HORIZONTAL; c.weightx = 1; c.weighty = 0;
         * this.add(cmbDisplayMode, c); c.anchor = GridBagConstraints.CENTER;
         * c.gridx = 3; c.gridy = 0; c.gridwidth = 1; c.gridheight = 1; c.fill =
         * GridBagConstraints.NONE; c.weightx = 0; c.weighty = 0;
         * this.add(btnSyncColors, c);
         */
        c.anchor = GridBagConstraints.CENTER;
        c.gridx = 0;
        c.gridy = 1;
        c.gridwidth = 1;
        c.gridheight = 1;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.weightx = 0.1;
        c.weighty = 0;
        this.add(lblZoom, c);

        c.anchor = GridBagConstraints.CENTER;
        c.gridx = 1;
        c.gridy = 1;
        c.gridwidth = 2;
        c.gridheight = 1;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.weightx = 1;
        c.weighty = 0;
        this.add(sldZoom, c);

        c.anchor = GridBagConstraints.CENTER;
        c.gridx = 3;
        c.gridy = 1;
        c.gridwidth = 1;
        c.gridheight = 1;
        c.fill = GridBagConstraints.NONE;
        c.weightx = 1;
        c.weighty = 0;
        this.add(autoScale, c);
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
        return Resources.get("mapperpanel.prefs.border.minimap");
      }

      public void initPreferences() {
        // TODO: implement it
      }

      /**
       * DOCUMENT-ME
       */
      public void applyPreferences() {
        renderers.applyPreferences();

        // setMinimapMode(cmbDisplayMode.getSelectedIndex());
        if (autoScale.isSelected()) {
          setAutoScaling(true);
        } else {
          setAutoScaling(false);
          setMinimapScale(sldZoom.getValue());
        }

        minimapPane.doLayout();
        minimapPane.repaint(100);
      }
    }

    // The source component to configure
    private MapperPanel source = null;

    // GUI elements
    private PreferencesAdapter prefMapper = null;
    private List<PreferencesAdapter> subAdapter;

    /**
     * Creates a new MapperPanelPreferences object.
     * 
     * @param m
     *          DOCUMENT-ME
     */
    public MapperPanelPreferences(MapperPanel m) {
      this.source = m;
      prefMapper = mapper.getPreferencesAdapter();

      subAdapter = new ArrayList<PreferencesAdapter>(2);
      subAdapter.add(new MapperPanelDetailPreferences());
      subAdapter.add(new MinimapPreferences());
    }

    /**
     * DOCUMENT-ME
     * 
     * 
     */
    public java.util.List getChildren() {
      return subAdapter;
    }

    /**
     * DOCUMENT-ME
     * 
     * 
     */
    public Component getComponent() {
      return prefMapper.getComponent();
    }

    public void initPreferences() {
      // TODO: implement it
    }

    /**
     * DOCUMENT-ME
     */
    public void applyPreferences() {
      prefMapper.applyPreferences();

      mapper.repaint(100);
    }

    /**
     * DOCUMENT-ME
     * 
     * 
     */
    public String getTitle() {
      return Resources.get("mapperpanel.prefs.title");
    }
  }

  protected class MinimapScaler extends ComponentAdapter {
    /**
     * DOCUMENT-ME
     * 
     * @param e
     *          DOCUMENT-ME
     */
    public void componentResized(ComponentEvent e) {
      if (resizeMinimap && (e.getSource() == minimapPane)) {
        resizeMinimap = false;

        Dimension newSize = minimapPane.getSize();

        if ((newSize.width <= 0) || (newSize.height <= 0)) {
          resizeMinimap = true;

          return;
        }

        Dimension prefSize;
        int loops = 0;

        do {
          loops++;

          // make it a little bit smaller
          newSize.width -= 2;
          newSize.height -= 2;
          prefSize = minimap.getPreferredSize();

          float sc = 1.0f;

          try {
            if (newSize.width != prefSize.width) {
              sc = ((float) newSize.width) / ((float) prefSize.width);
            }
          } catch (Exception exc) {
          }

          try {
            if (newSize.height != prefSize.height) {
              float sc2 = ((float) newSize.height) / ((float) prefSize.height);

              if (sc2 < sc) {
                sc = sc2;
              }
            }
          } catch (Exception exc) {
          }

          try {
            if (sc != 1.0f) {
              if (sc < 0) {
                sc = 0;
              }

              minimap.setScaleFactor(minimap.getScaleFactor() * sc);
            }
          } catch (Exception exc) {
          }

          prefSize = minimap.getPreferredSize();
        } while ((loops < 3) && ((prefSize.width > newSize.width) || (prefSize.height > newSize.height)));

        minimapPane.doLayout();
        minimapPane.repaint();
        resizeMinimap = true;
      }
    }
  }

  protected class TooltipShortcut implements ShortcutListener {
    protected java.util.List<KeyStroke> shortcuts;

    /**
     * Creates a new TooltipShortcut object.
     */
    public TooltipShortcut() {
      shortcuts = new ArrayList<KeyStroke>(10);

      for (int i = 1; i < 10; i++) {
        shortcuts.add(KeyStroke.getKeyStroke(Character.forDigit(i, 10)));
      }

      shortcuts.add(KeyStroke.getKeyStroke(Character.forDigit(0, 10)));
    }

    protected void setTooltip(int index) {
      java.util.List list = mapper.getAllTooltipDefinitions();

      if ((list != null) && (list.size() > (2 * index))) {
        mapper.setTooltipDefinition((String) list.get((2 * index) + 1));
      }
    }

    /**
     * This method is called when a shortcut from getShortCuts() is recognized.
     * 
     * @param shortcut
     *          DOCUMENT-ME
     */
    public void shortCut(javax.swing.KeyStroke shortcut) {
      int index = shortcuts.indexOf(shortcut);

      if ((index >= 0) && (index < 10)) {
        setTooltip(index);
      }
    }

    /**
     * Should return all short cuts this class want to be informed. The elements
     * should be of type javax.swing.KeyStroke
     * 
     * 
     */
    public Iterator<KeyStroke> getShortCuts() {
      return shortcuts.iterator();
    }

    /**
     * DOCUMENT-ME
     * 
     * @param stroke
     *          DOCUMENT-ME
     * 
     */
    public String getShortcutDescription(Object stroke) {
      int index = shortcuts.indexOf(stroke);

      return Resources.get("mapperpanel.shortcuts.tooltips." + String.valueOf(index));
    }

    /**
     * DOCUMENT-ME
     * 
     * 
     */
    public String getListenerDescription() {
      return Resources.get("mapperpanel.shortcuts.tooltips");
    }
  }
}
