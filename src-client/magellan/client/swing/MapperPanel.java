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
import java.awt.Font;
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
import java.util.ListIterator;
import java.util.Map;

import javax.swing.AbstractAction;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JSlider;
import javax.swing.JViewport;
import javax.swing.KeyStroke;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import magellan.client.MagellanContext;
import magellan.client.desktop.DesktopEnvironment;
import magellan.client.desktop.ExtendedShortcutListener;
import magellan.client.desktop.Initializable;
import magellan.client.desktop.MagellanDesktop;
import magellan.client.desktop.ShortcutListener;
import magellan.client.event.SelectionEvent;
import magellan.client.event.SelectionListener;
import magellan.client.preferences.MapPreferences;
import magellan.client.swing.map.AdvancedRegionShapeCellRenderer;
import magellan.client.swing.map.AdvancedTextCellRenderer;
import magellan.client.swing.map.CellGeometry;
import magellan.client.swing.map.HexCellRenderer;
import magellan.client.swing.map.MapCellRenderer;
import magellan.client.swing.map.Mapper;
import magellan.client.swing.map.Minimapper;
import magellan.client.swing.map.RegionImageCellRenderer;
import magellan.client.swing.map.RegionShapeCellRenderer;
import magellan.client.swing.preferences.PreferencesAdapter;
import magellan.client.swing.preferences.PreferencesFactory;
import magellan.library.Bookmark;
import magellan.library.BookmarkBuilder;
import magellan.library.CoordinateID;
import magellan.library.GameData;
import magellan.library.HasRegion;
import magellan.library.Island;
import magellan.library.Region;
import magellan.library.event.GameDataEvent;
import magellan.library.utils.CollectionFactory;
import magellan.library.utils.MagellanFactory;
import magellan.library.utils.PropertiesHelper;
import magellan.library.utils.Resources;
import magellan.library.utils.Utils;
import magellan.library.utils.logging.Logger;

/**
 * A panel holding all UI components related to an Eressea map. The component contains a Mapper
 * object and additional controls. The policy of this class is not to be concerned with map details
 * like coordinates etc. as much as possible and to provide a general and flexible interface to the
 * mapper.
 */
public class MapperPanel extends InternationalizedDataPanel implements ActionListener,
    SelectionListener, ChangeListener, ExtendedShortcutListener, PreferencesFactory, Initializable {
  private static final Logger log = Logger.getInstance(MapperPanel.class);

  /** for 3 step zoom in and zoom out our 3 scalings */
  private final float level3Scale1 = 0.3f;
  private final float level3Scale2 = 1.3f;
  private final float level3Scale3 = 2.3f;

  /** fixed min and max factors for scaling */
  private final float minScale = 0.1f;
  private final float maxScale = 3.3f;

  /** The map component in this panel. */
  private Mapper mapper;
  private JScrollPane scpMapper;
  private JLabel lblLevel;
  private JLabel lblScaling;
  /** contains JComboBox<Integer> */
  private JComboBox cmbLevel;
  private JSlider sldScaling;
  /** JComboBox<Bookmark> */
  private JComboBox cmbHotSpots;
  private Timer timer;
  private Point dragStart;
  private boolean dragValidated;

  // minimap components
  protected Minimapper minimap;
  protected JScrollPane minimapPane;
  private CellGeometry minimapGeometry;
  protected boolean resizeMinimap;
  protected MinimapScaler minimapScaler;
  protected float lastScale = 1;

  // shortcuts
  private List<KeyStroke> mapperShortcuts;
  private TooltipShortcut tooltipShortcut;

  protected MagellanContext context;

  /**
   * Updates components (like hotspot selection etc).
   *
   * @see magellan.client.swing.InternationalizedDataPanel#gameDataChanged(magellan.library.event.GameDataEvent)
   */
  @Override
  public void gameDataChanged(GameDataEvent e) {
    setGameData(e.getGameData());
    mapper.gameDataChanged(e);
    minimap.gameDataChanged(e);

    // showNavi =
    // context.getProperties().getProperty("MapperPannel.Details.showNavigation", "true").equals(
    // "true");

    lblScaling.setVisible(showNavi);
    sldScaling.setVisible(showNavi);
    List<Integer> levels = mapper.getLevels();
    lblLevel.setVisible((levels.size() > 1) && showNavi);
    cmbLevel.setVisible((levels.size() > 1) && showNavi);
    cmbLevel.removeAllItems();

    for (Integer level : levels) {
      cmbLevel.addItem(level);
    }

    if (cmbLevel.getItemCount() > 0) {
      cmbLevel.setSelectedIndex(0);
    }

    // fill hot spot combo
    fillComboHotSpots();

    cmbHotSpots.setVisible((cmbHotSpots.getItemCount() > 0) && showNavi);

    rescale();
    minimapPane.doLayout();
    minimapPane.repaint();
  }

  private void fillComboHotSpots() {
    cmbHotSpots.removeAllItems();

    if ((getGameData() != null) && (getGameData().getBookmarks() != null)) {
      List<Bookmark> hotSpots = new LinkedList<Bookmark>(getGameData().getBookmarks());
      Collections.sort(hotSpots, new Comparator<Bookmark>() {
        public int compare(Bookmark o1, Bookmark o2) {
          return o1.toString().compareTo(o2.toString());
        }
      });
      cmbHotSpots.setModel(new DefaultComboBoxModel(hotSpots.toArray()));
    }
  }

  /**
   * Selection event handler, updating the map if a new region is selected.
   *
   * @see magellan.client.event.SelectionListener#selectionChanged(magellan.client.event.SelectionEvent)
   */
  public void selectionChanged(SelectionEvent se) {
    if (MapperPanel.log.isDebugEnabled()) {
      MapperPanel.log.debug("MapperPanel.selectionChanged called with " + se.getActiveObject());
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
        Integer level = Integer.valueOf(newCenter.getZ());

        if (level.intValue() != mapper.getLevel()) {
          cmbLevel.setSelectedItem(level);
        }
      }

      /**
       * re-center the map if necessary do this later, mapper probably does not yet know the right
       * active region
       */
      class CenterRunner implements Runnable {
        public CoordinateID center = null;

        /**
         * Creates a new CenterRunner object that centers the given coordinate.
         */
        public CenterRunner(CoordinateID c) {
          center = c;
        }

        /**
         * Centers the map on the region with the given coordinate.
         */
        public void run() {
          if (MapperPanel.log.isDebugEnabled()) {
            MapperPanel.log
                .debug("MapperPanel.selectionChanged: Running CenterRunner on " + center);
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
        Region r1 = island.regions().iterator().next();
        CoordinateID coord = r1.getCoordinate();

        if (cmbLevel.isVisible()) {
          Integer level = Integer.valueOf(coord.getZ());

          if (level.intValue() != mapper.getLevel()) {
            cmbLevel.setSelectedItem(level);
          }
        }

        // then set center rectangle on right pane
        class ParamRunnable implements Runnable {
          Island island1;

          ParamRunnable(Island i) {
            island1 = i;
          }

          /**
           * Tries to center the given island.
           *
           * @see java.lang.Runnable#run()
           */
          @SuppressWarnings("null")
          public void run() {
            Rectangle islandBounds = null;

            for (Region r2 : island1.regions()) {
              CoordinateID coord2 = r2.getCoordinate();

              if (islandBounds == null) {
                islandBounds = mapper.getCellRect(coord2);
              } else {
                islandBounds.add(mapper.getCellRect(coord2));
              }
            }

            Rectangle centerRect = islandBounds;

            if (MapperPanel.log.isDebugEnabled()) {
              MapperPanel.log.debug("MapperPanel.selectionChanged: Running ParamRunnable with "
                  + centerRect);
            }

            if (!scpMapper.getViewport().getViewRect().contains(centerRect)) {
              /* FIXME these numbers should get some bounding */
              centerRect.x -=
                  ((scpMapper.getViewport().getViewRect().getWidth() - centerRect.getWidth()) / 2);
              centerRect.y -=
                  ((scpMapper.getViewport().getViewRect().getHeight() - centerRect.getHeight())
                      / 2);
              scpMapper.getViewport().setViewPosition(centerRect.getLocation());
            }
          }
        }
        SwingUtilities.invokeLater(new ParamRunnable(island));
      }
    }
  }

  Cursor waitCursor = new Cursor(Cursor.WAIT_CURSOR);
  Cursor defaultCursor = new Cursor(Cursor.DEFAULT_CURSOR);

  private boolean showNavi = true;

  /**
   * Action event handler for timer events related to the scaling slider.
   *
   * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
   */
  public void actionPerformed(ActionEvent ae) {
    setCursor(waitCursor);
    mapper.setCursor(waitCursor);

    CoordinateID center = mapper.getCenter(scpMapper.getViewport().getViewRect());
    mapper.setScaleFactor(sldScaling2Scale(sldScaling.getValue()));
    setCenter(center);
    this.repaint();
    setCursor(defaultCursor);
    mapper.setCursor(defaultCursor);
  }

  /**
   * Creates a new <tt>MapperPanel</tt> object.
   */
  public MapperPanel(MagellanContext context, Collection<MapCellRenderer> customRenderers,
      CellGeometry geo) {
    super(context.getEventDispatcher(), context.getGameData(), context.getProperties());
    this.context = context;

    // final MapperPanel thisMapperPanel = this;
    initMinimap();

    context.getEventDispatcher().addSelectionListener(this);

    setLayout(new BorderLayout());
    add(getMainPane(customRenderers, geo), BorderLayout.CENTER);

    // register own mouse listener for letting the user drag the
    // map
    mapper.addMouseListener(new MouseAdapter() {
      @Override
      public void mousePressed(MouseEvent e) {
        dragStart = e.getPoint();
        dragValidated = false;
      }

      @Override
      public void mouseReleased(MouseEvent e) {
        if (((e.getModifiers() & InputEvent.BUTTON3_MASK) != 0) || !dragValidated
            || ((e.getModifiers() & InputEvent.CTRL_DOWN_MASK) != 0))
          return;

        Rectangle bounds = new Rectangle(dragStart.x - 2, dragStart.y - 2, 4, 4);

        if (bounds.contains(e.getPoint()))
          return;

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
      @Override
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

    mapperShortcuts = new ArrayList<KeyStroke>(16);
    // 0: request Focus
    mapperShortcuts.add(KeyStroke.getKeyStroke(KeyEvent.VK_2, InputEvent.CTRL_DOWN_MASK));
    // 1: request Focus
    mapperShortcuts.add(KeyStroke.getKeyStroke(KeyEvent.VK_2, InputEvent.ALT_DOWN_MASK));
    // 2: add Hotspot
    mapperShortcuts.add(KeyStroke.getKeyStroke(KeyEvent.VK_H, InputEvent.CTRL_DOWN_MASK));
    // 3: remove Hotspot
    mapperShortcuts.add(KeyStroke.getKeyStroke(KeyEvent.VK_H, InputEvent.CTRL_DOWN_MASK
        | InputEvent.SHIFT_DOWN_MASK));
    // 4: fog of war
    mapperShortcuts.add(KeyStroke.getKeyStroke(KeyEvent.VK_W, InputEvent.CTRL_DOWN_MASK));
    // 5: tooltip selection
    mapperShortcuts.add(KeyStroke.getKeyStroke(KeyEvent.VK_P, InputEvent.CTRL_DOWN_MASK
        | InputEvent.SHIFT_DOWN_MASK));

    // 6,7: Map Zoom in First is numpad, scnd is normal key
    mapperShortcuts.add(KeyStroke.getKeyStroke(KeyEvent.VK_ADD, InputEvent.CTRL_DOWN_MASK));
    mapperShortcuts.add(KeyStroke.getKeyStroke(KeyEvent.VK_PLUS, InputEvent.CTRL_DOWN_MASK));
    // 8,9: Map Zoom out
    mapperShortcuts.add(KeyStroke.getKeyStroke(KeyEvent.VK_SUBTRACT, InputEvent.CTRL_DOWN_MASK));
    mapperShortcuts.add(KeyStroke.getKeyStroke(KeyEvent.VK_MINUS, InputEvent.CTRL_DOWN_MASK));

    // 3 Level Zoom in
    // 10,11 Fast Zoom In
    mapperShortcuts.add(KeyStroke.getKeyStroke(KeyEvent.VK_ADD, InputEvent.ALT_DOWN_MASK));
    mapperShortcuts.add(KeyStroke.getKeyStroke(KeyEvent.VK_PLUS, InputEvent.ALT_DOWN_MASK));
    // 12,13 Fast Zoom out
    mapperShortcuts.add(KeyStroke.getKeyStroke(KeyEvent.VK_SUBTRACT, InputEvent.ALT_DOWN_MASK));
    mapperShortcuts.add(KeyStroke.getKeyStroke(KeyEvent.VK_MINUS, InputEvent.ALT_DOWN_MASK));

    // 14: ATR selection
    mapperShortcuts.add(KeyStroke.getKeyStroke(KeyEvent.VK_A, InputEvent.CTRL_DOWN_MASK
        | InputEvent.SHIFT_DOWN_MASK));

    // 15: ARR selection
    mapperShortcuts.add(KeyStroke.getKeyStroke(KeyEvent.VK_R, InputEvent.CTRL_DOWN_MASK
        | InputEvent.SHIFT_DOWN_MASK));

    DesktopEnvironment.registerShortcutListener(this);

    float newScale = PropertiesHelper.getFloat(settings, "Map.scaleFactor", 1.0f);
    // bug fix
    if (newScale <= 0) {
      MapperPanel.log.warn("illegal property value Map.scaleFactor: " + newScale);
      newScale = 1f;
    }
    setScaleFactor(newScale);
  }

  /**
   * Creates the Minimap Panel.
   */
  protected void initMinimap() {
    minimap = new Minimapper(context, "MINIMAP");
    minimapGeometry = minimap.getCellGeometry();

    Dimension d = minimapGeometry.getCellSize();
    int size = 10;

    try {
      size = Integer.parseInt(settings.getProperty("Minimap.Scale"));
    } catch (Exception exc) {
      size = 10;
    }

    if (size <= 0) {
      MapperPanel.log.warn("Minimap.Scale <= 0: " + size);
      size = 10;
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
   * Change scale of the minimap.
   *
   * @param scale A value greater than 1. Larger scale means larger regions.
   * @throws IllegalArgumentException if scaleFactor &le; 0.
   */
  public void setMinimapScale(int scale) {
    if (scale <= 0)
      throw new IllegalArgumentException("scale < 0: " + scale);
    Dimension size = minimapGeometry.getCellSize();
    float newScale = (scale * minimapGeometry.getScaleFactor()) / size.width;
    minimap.setScaleFactor(newScale);
    minimapPane.doLayout();
    minimapPane.repaint();
  }

  /**
   * DOCUMENT-ME
   */
  public int getMinimapScale() {
    return minimapGeometry.getCellSize().width;
  }

  /**
   * Sets the autoscale property.
   *
   * @param bool the new value
   */
  public void setAutoScaling(boolean bool) {
    resizeMinimap = bool;
    settings.setProperty("Minimap.AutoScale", bool ? "true" : "false");

    if (bool) {
      rescale();
    }
  }

  /**
   * Returns the value of the auto scaling property.
   */
  public boolean isAutoScaling() {
    return resizeMinimap;
  }

  protected void rescale() {
    minimapScaler
        .componentResized(new ComponentEvent(minimapPane, ComponentEvent.COMPONENT_RESIZED));
  }

  /**
   * Change the minimap's paint mode.
   *
   * @see RegionShapeCellRenderer#setPaintMode(int)
   */
  public void setMinimapMode(int mode) {
    minimap.setPaintMode(mode);
  }

  /**
   * Returns the minimap's paint mode.
   *
   * @see RegionShapeCellRenderer#setPaintMode(int)
   */
  public int getMinimapMode() {
    return minimap.getPaintMode();
  }

  /**
   * Sets a new scaling factor for the map.
   *
   * @param fScale the new scaling factor, values may range from {@link #minScale} to
   *          {@link #maxScale}
   */
  public void setScaleFactor(float fScale) {
    fScale = Math.max(minScale, fScale);
    fScale = Math.min(fScale, maxScale);
    sldScaling.setValue(scale2sldScaling(fScale));
    mapper.setScaleFactor(fScale);
  }

  /**
   * sets new scale factor for map, centers on same position and repaints
   *
   * @param fScale
   */
  public void setNewScaleFactor(float fScale) {
    CoordinateID center = mapper.getCenter(scpMapper.getViewport().getViewRect());
    setScaleFactor(fScale);
    setCenter(center);
    this.repaint();
  }

  /**
   * Returns the current scaling factor applied to the map.
   */
  public float getScaleFactor() {
    return mapper.getScaleFactor();
  }

  /**
   * Set a cell renderer object for its default rendering plane. See com.eressea.swing.map.Mapper
   * for further reference.
   *
   * @param renderer the object responsible for rendering a graphical representation of regions.
   */
  public void setRenderer(HexCellRenderer renderer) {
    mapper.setRenderer(renderer);
  }

  /**
   * Set a cell renderer object for a certain plane of the map. See com.eressea.swing.map.Mapper for
   * further reference.
   *
   * @param renderer the object responsible for rendering a graphical representation of regions.
   * @param plane the plane the renderer will draw to. Lower planes are painted over by higher
   *          planes.
   */
  public void setRenderer(HexCellRenderer renderer, int plane) {
    mapper.setRenderer(renderer, plane);
  }

  /**
   * Get the selected Regions. The returned map can be empty but is never null.
   */
  public Map<CoordinateID, Region> getSelectedRegions() {
    return mapper.getSelectedRegions();
  }

  /**
   * Get the active region.
   */
  public Region getActiveRegion() {
    return mapper.getActiveRegion();
  }

  /**
   * Returns the mapper's current level.
   *
   * @see Mapper#getLevel()
   */
  public int getLevel() {
    return mapper.getLevel();
  }

  /**
   * Changes the mappers' map level
   *
   * @param level the new level
   * @see Mapper#setLevel(int)
   */
  public void setLevel(int level) {
    minimap.setLevel(level);
    mapper.setLevel(level);
    // when there was a change from level 1 to level 0
    // i.e. from Astralraum back to normal map we
    // try to intelligently center the map
    if (mapper.getActiveRegion() != null) {
      CoordinateID c = mapper.getActiveRegion().getCoordinate();
      CoordinateID cNew = context.getGameData().getRelatedCoordinateUI(c, level);
      if (cNew == null) {
        cNew = CoordinateID.create(0, 0, level);
      }
      setCenter(cNew);
    }

    // check, if we have a difference between Level just set and displayed Level in navi
    Integer displayedLevel = (Integer) cmbLevel.getSelectedItem();
    if (level != displayedLevel.intValue()) {
      cmbLevel.setSelectedItem(Integer.valueOf(level));
    }
  }

  /**
   * Centers the map on a certain region.
   *
   * @param center the coordinate of the region to center the map on.
   */
  public void setCenter(CoordinateID center) {
    Point newViewPosition = mapper.getCenteredViewPosition(scpMapper.getSize(), center);

    if (newViewPosition != null) {
      Dimension size = scpMapper.getViewport().getSize();
      newViewPosition.x = Math.max(0, newViewPosition.x);
      newViewPosition.x = Math.min(Math.max(0, mapper.getWidth() - size.width), newViewPosition.x);
      newViewPosition.y = Math.max(0, newViewPosition.y);
      newViewPosition.y =
          Math.min(Math.max(0, mapper.getHeight() - size.height), newViewPosition.y);
      scpMapper.getViewport().setViewPosition(newViewPosition);
    }
  }

  /**
   * Centers the minimap on a certain region.
   *
   * @param center the coordinate of the region to center the map on.
   */
  public void setMinimapCenter(CoordinateID center) {
    Point newViewPosition = minimap.getCenteredViewPosition(minimapPane.getSize(), center);

    if (newViewPosition != null) {
      Dimension size = minimapPane.getViewport().getSize();
      newViewPosition.x = Math.max(0, newViewPosition.x);
      newViewPosition.x = Math.min(Math.max(0, minimap.getWidth() - size.width), newViewPosition.x);
      newViewPosition.y = Math.max(0, newViewPosition.y);
      newViewPosition.y =
          Math.min(Math.max(0, minimap.getHeight() - size.height), newViewPosition.y);
      minimapPane.getViewport().setViewPosition(newViewPosition);
    }
  }

  /**
   * Assign the currently visible part of the map (the region at the center), a hot spot, an id and
   * add it to the list of hot spots.
   *
   * @param name the id to assign to the hot spot.
   */
  public void assignHotSpot(String name) {
    CoordinateID center = mapper.getCenter(scpMapper.getViewport().getViewRect());

    if (center != null) {
      BookmarkBuilder h = MagellanFactory.createBookmark();
      h.setName(name);

      Region r = findRegion(center, 10);
      if (r == null)
        return;

      h.setObject(r);
      getGameData().addBookmark(h.getBookmark());

      fillComboHotSpots();

      if (cmbHotSpots.getItemCount() != 0) {
        cmbHotSpots.setVisible(true);
      }

      if (cmbHotSpots.getFontMetrics(cmbHotSpots.getFont()).stringWidth(name) > cmbHotSpots
          .getMinimumSize().width) {
        cmbHotSpots.setMinimumSize(new Dimension(cmbHotSpots.getFontMetrics(cmbHotSpots.getFont())
            .stringWidth(name), cmbHotSpots.getMinimumSize().height));
      }
    }
  }

  private Region findRegion(CoordinateID center, int maxDist) {
    final GameData data = getGameData();
    return Utils.spiralPattern(center, maxDist, new Utils.SpiralVisitor<Region>() {
      Region result = null;

      public boolean visit(CoordinateID c, int distance) {
        Region r = data.getRegion(CoordinateID.create(c.getX(), c.getY()));
        if (r != null) {
          result = r;
          return true;
        }
        return false;
      }

      public Region getResult() {
        return result;
      }
    });

  }

  /**
   * Center the map on the specified hot spot.
   *
   * @param h the hot spot to move the map to.
   */
  public void showHotSpot(Bookmark h) {
    dispatcher.fire(SelectionEvent.create(this, h.getObject(), SelectionEvent.ST_DEFAULT));

    // // switch planes
    // if ((mapper.getActiveRegion() == null)
    // || (mapper.getActiveRegion().getCoordinate().getZ() != ((h.getCenter()).getZ()))) {
    // if (cmbLevel.isVisible()) {
    // cmbLevel.setSelectedItem(Integer.valueOf((h.getCenter()).getZ()));
    // }
    // }
    //
    // // re-center mapper
    // Point viewPos = mapper.getCenteredViewPosition(scpMapper.getSize(), h.getCenter());
    //
    // if (viewPos != null) {
    // scpMapper.getViewport().setViewPosition(viewPos);
    // mapper.requestFocus();
    // }
  }

  /**
   * Remove the specified hot spot.
   *
   * @param h the hot spot to remove.
   */
  public void removeHotSpot(Bookmark h) {
    getGameData().removeBookmark(h.getObject());
    cmbHotSpots.removeItem(h);

    if (cmbHotSpots.getItemCount() == 0) {
      cmbHotSpots.setVisible(false);
    }
  }

  /**
   * Get the cell geometry from the resources and make all renderers that use images reload the
   * graphics files.
   */
  public void reloadGraphicSet() {
    mapper.reloadGraphicSet();
  }

  /**
   * Stores the region that is at the center of the currently visible area.
   */
  @Override
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
   */
  public Component getView() {
    return mapper;
  }

  // /**
  // * Creates random integer values until one is not already used as a key in the game data's hot
  // * spot map.
  // *
  // * @return an integer the Integer representation of which is not already used as a key in the
  // * current game data's hot spot map.
  // */
  // private IntegerID getNewHotSpotID() {
  // IntegerID i = null;
  //
  // do {
  // i = IntegerID.create(random.nextInt());
  // } while (getGameData().getHotSpot(i) != null);
  //
  // return i;
  // }

  private Container getMainPane(Collection<MapCellRenderer> renderers, CellGeometry geo) {
    mapper = new Mapper(context, renderers, geo, "Mapper");
    scpMapper = new JScrollPane(mapper);

    // ClearLook suggests to remove border
    scpMapper.setBorder(new EmptyBorder(0, 0, 0, 0));

    JPanel mainPanel = new JPanel(new GridBagLayout());
    GridBagConstraints c = new GridBagConstraints();
    mainPanel.setBorder(new EmptyBorder(2, 2, 2, 2));

    lblScaling = new JLabel(Resources.get("mapperpanel.lbl.zoom.caption"));
    sldScaling = new JSlider(SwingConstants.HORIZONTAL);
    sldScaling.setMinimum(scale2sldScaling(minScale));
    sldScaling.setMaximum(scale2sldScaling(maxScale));
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
      @Override
      public void keyPressed(KeyEvent e) {
        if ((e.getModifiersEx() & InputEvent.CTRL_DOWN_MASK) != 0) {
          sldScaling.setSnapToTicks(true);
        }
      }

      @Override
      public void keyReleased(KeyEvent e) {
        if ((e.getModifiersEx() & InputEvent.CTRL_DOWN_MASK) != 0) {
          sldScaling.setSnapToTicks(false);
        }
      }
    });
    lblScaling.setLabelFor(sldScaling);

    lblLevel = new JLabel(Resources.get("mapperpanel.lbl.level.caption"));
    cmbLevel = new JComboBox(mapper.getLevels().toArray(new Integer[] {}));

    if (cmbLevel.getItemCount() > 0) {
      cmbLevel.setSelectedIndex(0);
    }

    cmbLevel.setMinimumSize(new Dimension(50, 25));
    cmbLevel.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent ae) {
        Integer level = (Integer) ((JComboBox) ae.getSource()).getSelectedItem();

        if (level != null) {
          setLevel(level);
        }
      }
    });
    lblLevel.setLabelFor(cmbLevel);
    lblLevel.setVisible(cmbLevel.getItemCount() > 1);
    cmbLevel.setVisible(cmbLevel.getItemCount() > 1);

    cmbHotSpots = new JComboBox();

    fillComboHotSpots();

    cmbHotSpots.setMinimumSize(new Dimension(50, 25));
    cmbHotSpots.setVisible(cmbHotSpots.getItemCount() != 0);
    cmbHotSpots.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent ae) {
        Bookmark h = (Bookmark) ((JComboBox) ae.getSource()).getSelectedItem();

        if (h != null) {
          showHotSpot(h);
        }
      }
    });

    // initial visibility...all other elements are invisible at start up anyway
    showNavi =
        context.getProperties().getProperty("MapperPannel.Details.showNavigation", "true").equals(
            "true");

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

  private int scale2sldScaling(float fScale) {
    return (int) ((fScale - minScale) * 50.0);
  }

  private float sldScaling2Scale(int value) {
    return (float) ((value / 50.0) + minScale);
  }

  /**
   * Called when the viewed rect of the main mapper changes. In further implementations a rect of
   * the visible bounds should be displayed in the minimap.
   *
   * @see javax.swing.event.ChangeListener#stateChanged(javax.swing.event.ChangeEvent)
   */
  public void stateChanged(javax.swing.event.ChangeEvent p1) {
    // not implemented
  }

  /**
   * Returns the minimap's main component.
   */
  public Component getMinimapComponent() {
    return minimapPane;
  }

  /**
   * Should return all short cuts this class want to be informed. The elements should be of type
   * javax.swing.KeyStroke
   */
  public Iterator<KeyStroke> getShortCuts() {
    return mapperShortcuts.iterator();
  }

  /**
   * This method is called when a shortcut from getShortCuts() is recognized.
   *
   * @see magellan.client.desktop.ShortcutListener#shortCut(javax.swing.KeyStroke)
   */
  public void shortCut(javax.swing.KeyStroke shortcut) {
    int index = mapperShortcuts.indexOf(shortcut);

    switch (index) {
    case -1:
      break; // unknown shortcut

    case 0:
    case 1:
      // request FOcus CTRL + 2 or ALT + 2
      magellan.client.desktop.DesktopEnvironment.requestFocus(MagellanDesktop.MAP_IDENTIFIER);
      mapper.requestFocus(); // activate the mapper, not the scrollpane

      break;

    case 2:
      // insert Hotspot CTRL+H
      String input =
          JOptionPane.showInputDialog(Resources.get("mapperpanel.msg.enterhotspotname.text"));

      if ((input != null) && !input.equals("")) {
        assignHotSpot(input); // just CTRL
      }

      break;

    case 3:
      // remove HotSpot CTRL + ALT + H
      Bookmark h = (Bookmark) cmbHotSpots.getSelectedItem();

      if (h != null) {
        removeHotSpot(h); // SHIFT + CTRL
      }

      break;

    case 4:
      // FoW CTRL + W
      Collection<MapCellRenderer> renderers = mapper.getRenderers(Mapper.PLANE_REGION);

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
      // CTRL+Shift+P
      changeTooltip();
      break;

    case 6:
    case 7:
      // Zoom in CTRL + "+"
      if (getScaleFactor() < maxScale) {
        setNewScaleFactor(getNextTickValue());
      }
      break;
    case 8:
    case 9:
      // Zoom out CTRL + "-"
      if (getScaleFactor() > minScale) {
        setNewScaleFactor(getPreviousTickValue());
      }
      break;

    case 10:
    case 11:
      // 3 Step Zoom in ALT + "+"
      if (getScaleFactor() < level3Scale3) {
        float newScale = level3Scale3;
        if (getScaleFactor() < (level3Scale2 - 0.2f)) {
          newScale = level3Scale2;
        }
        setNewScaleFactor(newScale);
      }
      break;
    case 12:
    case 13:
      // 3 Step Zoom out ALT + "-"
      if (getScaleFactor() > level3Scale1) {
        float newScale = level3Scale1;
        if (getScaleFactor() > level3Scale2 + 0.2f) {
          newScale = level3Scale2;
        }
        setNewScaleFactor(newScale);
      }
      break;

    case 14:
      // CTRL+Shift+T
      changeATR();
      break;

    case 15:
      // CTRL+Shift+R
      changeARR();
      break;
    }
  }

  /**
   * Creates a popup menu for changing tooltips
   */
  private void changeTooltip() {
    final List<String> list = mapper.getAllTooltipDefinitions();
    String[] current = mapper.getTooltipDefinition();
    final Map<String, String> defs = CollectionFactory.createOrderedMap(list.size() / 2);
    for (ListIterator<String> it = list.listIterator(); it.hasNext();) {
      defs.put(it.next(), it.next());
    }

    showDialog(new ArrayList<String>(defs.keySet()), Resources.get(
        "mapperpanel.shortcuts.changetooltipmenu.caption"), null,
        new Loader() {
          public void load(String name) {
            mapper.setTooltipDefinition(name, defs.get(name));
            mapper.setShowTooltip(true);
          }
        }, current[0]);
  }
  // if (list == null)
  // return;
  //
  // JPopupMenu menu = new JPopupMenu("tooltips");
  // JMenuItem popupCaption =
  // new JMenuItem(Resources.get("mapperpanel.shortcuts.changetooltipmenu.caption"));
  // popupCaption.setEnabled(false);
  // menu.add(popupCaption);
  //
  // for (int i = 0; i < 10; ++i) {
  // final int number = i;
  // JMenuItem item =
  // new JMenuItem(new AbstractAction(String.valueOf(number) + " "
  // + (2 * i >= list.size() ? "---" : list.get(2 * i))) {
  //
  // public void actionPerformed(ActionEvent e) {
  // if (list.size() > (2 * number)) {
  // mapper.setTooltipDefinition((String) list.get((2 * number) + 1));
  // }
  // }
  // });
  // item.setMnemonic(Character.forDigit(i, 10));
  // menu.add(item);
  // if (list.size() > (2 * number)) {
  // item.setEnabled(true);
  // } else {
  // item.setEnabled(false);
  // }
  // }
  // menu.show(this, getLocation().x, getLocation().y);
  // }

  /**
   * Creates a popup menu for changing tooltips.
   */
  private void changeATR() {
    final AdvancedTextCellRenderer atr = mapper.getATR();
    final List<String> sets = atr.getAllSetNames();
    showDialog(sets, Resources.get("mapperpanel.shortcuts.changeatr.caption"), atr, new Loader() {
      public void load(String name) {
        atr.loadSet(name);
      }
    }, atr.getCurrentSet());
  }

  /**
   * Creates a popup menu for changing tooltips.
   */
  private void changeARR() {
    final AdvancedRegionShapeCellRenderer arr = mapper.getARR();
    final List<String> sets = arr.getAllSetNames();
    showDialog(sets, Resources.get("mapperpanel.shortcuts.changearr.caption"), arr, new Loader() {
      public void load(String name) {
        arr.loadSet(name);
      }
    }, arr.getCurrentSet());
  }

  interface Loader {
    void load(String name);
  }

  private void showDialog(final List<String> sets, String caption,
      final MapCellRenderer newRenderer, final Loader loader, String currentSet) {
    if (sets == null)
      return;

    JPopupMenu menu = new JPopupMenu("tooltips");
    JMenuItem popupCaption = new JMenuItem(caption);
    popupCaption.setEnabled(false);
    menu.add(popupCaption);

    for (int i = 0; i < sets.size(); ++i) {
      final int number = i;
      String name = sets.get(number);
      JMenuItem item = new JMenuItem(new AbstractAction(String.valueOf(number + 1) + " " + name) {

        public void actionPerformed(ActionEvent e) {

          if (sets.size() > number) {
            if (newRenderer != null) {
              mapper.setRenderer(newRenderer);
            }
            loader.load(sets.get(number));

            Mapper.setRenderContextChanged(true);
            DesktopEnvironment.repaintComponent(MagellanDesktop.MAP_IDENTIFIER);
          }
        }
      });
      if (number < 10) {
        item.setMnemonic(Character.forDigit((number + 1) % 10, 10));
      }
      if (sets.get(number).equals(currentSet)) {
        item.setFont(item.getFont().deriveFont(Font.BOLD));
      }
      menu.add(item);
    }
    menu.show(this, getLocation().x, getLocation().y);
  }

  private float getNextTickValue() {
    return sldScaling2Scale(sldScaling.getValue() + 2 * sldScaling.getMajorTickSpacing());
  }

  private float getPreviousTickValue() {
    return sldScaling2Scale(sldScaling.getValue() - 2 * sldScaling.getMajorTickSpacing());
  }

  /**
   * @see magellan.client.swing.preferences.PreferencesFactory#createPreferencesAdapter()
   */
  public PreferencesAdapter createPreferencesAdapter() {
    return new MapPreferences(this);
  }

  /**
   * Returns the current configuration of this mapper panel. The current implementation divides all
   * the information by "_". First the scale factor is stored, then planes(plane index, renderer
   * class name, renderer configuration).
   */
  public java.lang.String getComponentConfiguration() {
    return mapper.getComponentConfiguration() + ":" + minimap.getComponentConfiguration();
  }

  /**
   * @see magellan.client.desktop.Initializable#initComponent(java.lang.String)
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
   * @see magellan.client.desktop.ExtendedShortcutListener#getExtendedShortcutListener(javax.swing.KeyStroke)
   */
  public ShortcutListener getExtendedShortcutListener(KeyStroke stroke) {
    return tooltipShortcut;
  }

  /**
   * @see magellan.client.desktop.ExtendedShortcutListener#isExtendedShortcut(javax.swing.KeyStroke)
   */
  public boolean isExtendedShortcut(KeyStroke stroke) {
    // (stm) extended shortcuts are too obfuscated for the user, so I disabled them
    return false;
    // int index = shortcuts.indexOf(stroke);
    //
    // return (index == 5);
  }

  /**
   * @see magellan.client.desktop.ShortcutListener#getShortcutDescription(KeyStroke)
   */
  public String getShortcutDescription(KeyStroke stroke) {
    int index = mapperShortcuts.indexOf(stroke);

    return Resources.get("mapperpanel.shortcuts.description." + String.valueOf(index));
  }

  /**
   * @see magellan.client.desktop.ShortcutListener#getListenerDescription()
   */
  public String getListenerDescription() {
    return Resources.get("mapperpanel.shortcuts.description");
  }

  protected class MinimapScaler extends ComponentAdapter {
    /**
     * @see java.awt.event.ComponentAdapter#componentResized(java.awt.event.ComponentEvent)
     */
    @Override
    public void componentResized(ComponentEvent e) {
      // FIXME Does not work well with very large maps!
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
            sc = 1.0f;
          }

          try {
            if (newSize.height != prefSize.height) {
              float sc2 = ((float) newSize.height) / ((float) prefSize.height);

              if (sc2 < sc) {
                sc = sc2;
              }
            }
          } catch (Exception exc) {
            // leave value of sc
          }

          try {
            if (sc != 1.0f) {
              if (sc <= 0) {
                sc = 1f;
              }

              minimap.setScaleFactor(minimap.getScaleFactor() * sc);
            }
          } catch (Exception exc) {
            // leave value of sc
          }

          prefSize = minimap.getPreferredSize();
        } while ((loops < 3)
            && ((prefSize.width > newSize.width) || (prefSize.height > newSize.height)));

        minimapPane.doLayout();
        minimapPane.repaint();
        resizeMinimap = true;
      }
    }
  }

  protected class TooltipShortcut implements ShortcutListener {
    protected java.util.List<KeyStroke> shortcuts1;

    /**
     * Creates a new TooltipShortcut object.
     */
    public TooltipShortcut() {
      shortcuts1 = new ArrayList<KeyStroke>(10);

      for (int i = 1; i < 10; i++) {
        shortcuts1.add(KeyStroke.getKeyStroke(Character.forDigit(i, 10)));
      }

      shortcuts1.add(KeyStroke.getKeyStroke(Character.forDigit(0, 10)));
    }

    protected void setTooltip(int index) {
      java.util.List<String> list = mapper.getAllTooltipDefinitions();

      if ((list != null) && (list.size() > (2 * index))) {
        mapper.setTooltipDefinition(list.get(2 * index), list.get((2 * index) + 1));
      }
    }

    /**
     * Sets the x-th tooltip (if defined).
     *
     * @see magellan.client.desktop.ShortcutListener#shortCut(javax.swing.KeyStroke)
     */
    public void shortCut(javax.swing.KeyStroke shortcut) {
      int index = shortcuts1.indexOf(shortcut);

      if ((index >= 0) && (index < 10)) {
        setTooltip(index);
      }
    }

    /**
     * @see magellan.client.desktop.ShortcutListener#getShortCuts()
     */
    public Iterator<KeyStroke> getShortCuts() {
      return shortcuts1.iterator();
    }

    /**
     * @see magellan.client.desktop.ShortcutListener#getShortcutDescription(KeyStroke)
     */
    public String getShortcutDescription(KeyStroke stroke) {
      int index = shortcuts1.indexOf(stroke);

      return Resources.get("mapperpanel.shortcuts.tooltips." + String.valueOf(index));
    }

    /**
     * @see magellan.client.desktop.ShortcutListener#getListenerDescription()
     */
    public String getListenerDescription() {
      return Resources.get("mapperpanel.shortcuts.tooltips");
    }
  }

  /**
   * Returns the value of mapper.
   *
   * @return Returns mapper.
   */
  public Mapper getMapper() {
    return mapper;
  }

  /**
   * Sets the value of mapper.
   *
   * @param mapper The value for mapper.
   */
  public void setMapper(Mapper mapper) {
    this.mapper = mapper;
  }

  /**
   * Returns the value of context.
   *
   * @return Returns context.
   */
  public MagellanContext getContext() {
    return context;
  }

  /**
   * Sets the value of context.
   *
   * @param context The value for context.
   */
  public void setContext(MagellanContext context) {
    this.context = context;
  }

  /**
   * Returns the value of minimap.
   */
  public Minimapper getMinimap() {
    return minimap;
  }

  /**
   * Returns the value of showNavi.
   *
   * @return Returns showNavi.
   */
  public boolean isShowNavigation() {
    return showNavi;
  }

  /**
   * Sets the value of showNavi.
   *
   * @param showNavi The value for showNavi.
   */
  public void setShowNavigation(boolean showNavi) {
    this.showNavi = showNavi;
    PropertiesHelper.setBoolean(context.getProperties(), "MapperPannel.Details.showNavigation",
        showNavi);
    lblScaling.setVisible(showNavi);
    sldScaling.setVisible(showNavi);
    lblLevel.setVisible((cmbLevel.getItemCount() > 1) && showNavi);
    cmbLevel.setVisible((cmbLevel.getItemCount() > 1) && showNavi);
    cmbHotSpots.setVisible((cmbHotSpots.getItemCount() > 0) && showNavi);
  }

  /**
   * @param use
   */
  public void setUseSeasonImages(boolean use) {
    mapper.setUseSeasonImages(use);
    minimap.setUseSeasonImages(use);
  }
}
