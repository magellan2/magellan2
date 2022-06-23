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

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.GraphicsEnvironment;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Rectangle;
import java.awt.Stroke;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Collection;
import java.util.Enumeration;
import java.util.StringTokenizer;

import javax.swing.AbstractButton;
import javax.swing.ButtonGroup;
import javax.swing.JCheckBox;
import javax.swing.JColorChooser;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;

import magellan.client.MagellanContext;
import magellan.client.swing.preferences.PreferencesAdapter;
import magellan.client.utils.Colors;
import magellan.client.utils.SwingUtils;
import magellan.library.CoordinateID;
import magellan.library.GameData;
import magellan.library.Region;
import magellan.library.Sign;
import magellan.library.utils.Resources;
import magellan.library.utils.logging.Logger;

/**
 * Basic class to draw Signs: a string within a box a first try
 *
 * @author Fiete
 * @version 1.0
 */
public class SignTextCellRenderer extends HexCellRenderer {
  protected Color fontColor = Color.black;
  protected Color backColor = Color.white;
  protected Color brighterColor = Color.black.brighter();
  protected Font unscaledFont;
  protected Font font;
  // protected FontMetrics fontMetrics ;
  protected int minimumFontSize = 10;
  // protected int fontHeight = 0;
  protected boolean isScalingFont = false;
  protected boolean drawlines = false;
  protected int hAlign = SignTextCellRenderer.CENTER;
  protected String singleString[] = new String[1];
  String doubleString[] = new String[2];

  private boolean drawFrame;

  /**
   * Identifier in Region-Tags for MapLines
   */
  public static final String LINE_TAG = "mapline";

  /** align text left */
  public static final int LEFT = 0;

  /** align text centered */
  public static final int CENTER = 1;

  /** align text right */
  public static final int RIGHT = 2;

  private boolean LinesOnly = false;

  /**
   * Creates new SignTextCellRenderer
   */
  protected SignTextCellRenderer(CellGeometry geo, MagellanContext context) {
    super(geo, context);

    if (settings != null) {
      setFontColor(Colors.decode(settings.getProperty("SignTextCellRenderer.textColor", Colors
          .encode(fontColor))));
      setBackColor(Colors.decode(settings.getProperty("SignTextCellRenderer.backColor", Colors
          .encode(backColor))));

      setScalingFont((Boolean.valueOf(settings.getProperty("SignTextCellRenderer.isScalingFont",
          "false"))).booleanValue());
      setDrawingLines((Boolean.valueOf(settings.getProperty("SignTextCellRenderer.DrawLines",
          "false"))).booleanValue());
      setDrawingFrame((Boolean.valueOf(settings.getProperty("SignTextCellRenderer.DrawFrame",
          "true"))).booleanValue());
      switch (settings.getProperty("SignTextCellRenderer.halign", "CENTER")) {
      case "LEFT":
        setHAlign(LEFT);
        break;
      case "RIGHT":
        setHAlign(RIGHT);
        break;
      case "CENTER":
      default:
        setHAlign(CENTER);
      }
    }

    setFont(new Font(settings.getProperty("SignTextCellRenderer.fontName", "SansSerif"), Integer
        .parseInt(settings.getProperty("SignTextCellRenderer.fontStyle", Font.PLAIN + "")), Integer
            .parseInt(settings.getProperty("SignTextCellRenderer.fontSize", 11 + ""))));
    setMinimumFontSize(Integer.parseInt(settings.getProperty(
        "SignTextCellRenderer.minimumFontSize", "10")));
  }

  protected Color getFontColor() {
    return fontColor;
  }

  protected void setFontColor(Color col) {
    fontColor = col;
    settings.setProperty("SignTextCellRenderer.textColor", Colors.encode(fontColor));
  }

  protected Font getFont() {
    return unscaledFont;
  }

  protected void setFont(Font f) {
    unscaledFont = f;
    setFont(1f);
    settings.setProperty("SignTextCellRenderer.fontName", f.getName());
    settings.setProperty("SignTextCellRenderer.fontStyle", Integer.toString(f.getStyle()));
    settings.setProperty("SignTextCellRenderer.fontSize", Integer.toString(f.getSize()));
  }

  protected void setFont(float scaleFactor) {
    font = unscaledFont.deriveFont(Math.max(unscaledFont.getSize() * scaleFactor, minimumFontSize));

    // using deprecated getFontMetrics() to avoid Java2D methods
    // fontMetrics = Client.getDefaultFontMetrics(this.font);
    // fontHeight = this.fontMetrics.getHeight();
  }

  protected boolean isScalingFont() {
    return isScalingFont;
  }

  protected void setScalingFont(boolean b) {
    if (b != isScalingFont()) {
      isScalingFont = b;
      settings.setProperty("SignTextCellRenderer.isScalingFont", String.valueOf(isScalingFont()));
    }
  }

  protected boolean isDrawingLines() {
    return drawlines;
  }

  protected void setDrawingLines(boolean b) {
    if (b != drawlines) {
      drawlines = b;
      settings.setProperty("SignTextCellRenderer.DrawLines", String.valueOf(isDrawingLines()));
    }
  }

  protected int getMinimumFontSize() {
    return minimumFontSize;
  }

  protected void setMinimumFontSize(int size) {
    if (getMinimumFontSize() != size) {
      minimumFontSize = size;
      settings.setProperty("SignTextCellRenderer.minimumFontSize", size + "");
    }
  }

  protected int getHAlign() {
    return hAlign;
  }

  protected void setHAlign(int i) {
    hAlign = i;
    switch (hAlign) {
    case LEFT:
      settings.setProperty("SignTextCellRenderer.halign", "LEFT");
      break;
    case RIGHT:
      settings.setProperty("SignTextCellRenderer.halign", "RIGHT");
      break;
    case CENTER:
    default:
      settings.setProperty("SignTextCellRenderer.halign", "CENTER");
    }

  }

  /**
   * @see magellan.client.swing.map.HexCellRenderer#getPlaneIndex()
   */
  @Override
  public int getPlaneIndex() {
    return Mapper.PLANE_SIGNS;
  }

  /**
   * Returns the text lines which should be on the sign; max 2 lines allowed
   *
   * @param r the region
   * @param rect a rectangle of region-hex - not needed here
   * @return Collection of Strings to put on the sign
   */
  public Collection<Sign> getText(Region r, Rectangle rect) {
    return r.getSigns();
  }

  /**
   * @see magellan.client.swing.map.HexCellRenderer#init(magellan.library.GameData, java.awt.Graphics,
   *      java.awt.Rectangle)
   */
  @Override
  public void init(GameData aData, Graphics g, Rectangle aOffset) {
    super.init(aData, g, aOffset);

    if (isScalingFont) {
      setFont(cellGeo.getScaleFactor());
    } else {
      setFont(1.0f);
    }

  }

  private void create_Maplines(Region r) {
    // Linien...Fiete 20170312
    // Syntax der Tags:
    // TargetX,TargetY,Color_R,Color_G,Color_B,Line_width,[..(tool-specific-values)..]

    String key = SignTextCellRenderer.LINE_TAG;
    if (r.containsTag(key)) {
      CoordinateID c = r.getCoordinate();

      Rectangle rect = cellGeo.getImageRect(c.getX(), c.getY());
      rect.translate(-offset.x, -offset.y);
      StringTokenizer st = new StringTokenizer(r.getTag(key), " ");

      while (st.hasMoreTokens()) {
        String token = st.nextToken();
        // token besteht aus X,Y
        String[] coords = token.split(",");
        int TargetX = Integer.parseInt(coords[0]);
        int TargetY = Integer.parseInt(coords[1]);
        Color col = Color.red;
        if (coords.length > 2) {
          // die farbe ist als RGB uebergeben worden
          col =
              new Color(Integer.parseInt(coords[2]), Integer.parseInt(coords[3]), Integer
                  .parseInt(coords[4]));
        }
        int thickness = 5;
        if (coords.length > 5) {
          thickness = Integer.parseInt(coords[5]);
        }
        thickness = (int) Math.floor(thickness * cellGeo.getScaleFactor());
        if (thickness < 1) {
          thickness = 1;
        }
        Rectangle targetRect = cellGeo.getImageRect(TargetX, TargetY);
        targetRect.translate(-offset.x, -offset.y);
        Stroke savestroke = graphics.getStroke();
        graphics.setPaint(col);
        graphics.setStroke(new BasicStroke(thickness));
        graphics.drawLine(rect.x + (rect.width / 2), rect.y + (rect.height / 2), targetRect.x
            + (targetRect.width / 2),
            targetRect.y + (targetRect.height / 2));
        graphics.setStroke(savestroke);
      }
    }
  }

  /**
   * @see magellan.client.swing.map.HexCellRenderer#render(java.lang.Object, boolean, boolean)
   */
  @Override
  public void render(Object obj, boolean active, boolean selected) {
    if (obj instanceof Region) {
      Region r = (Region) obj;

      if (LinesOnly && isDrawingLines() && offset != null) {
        create_Maplines(r);
        return;
      }
      if (graphics == null || font == null)
        return;
      CoordinateID c = r.getCoordinate();
      Rectangle rect = cellGeo.getCellRect(c.getX(), c.getY());

      Collection<Sign> signs = getText(r, rect);
      if (signs == null)
        return;

      String[] display = new String[signs.size()];
      int signCount = 0;
      for (Sign s : signs) {
        display[signCount++] = s.getText();
      }

      if (display.length == 0)
        return;

      graphics.setFont(font);
      graphics.setColor(fontColor);

      int height = getHeight(display);
      int middleX = (rect.x + (rect.width / 2)) - offset.x;
      int middleY = (rect.y + (rect.height / 2) + 1) - offset.y;
      int upperY = middleY;

      if (display.length == 1) {
        upperY += (height / 4);
      } else {
        upperY -= (((display.length - 1) * height) / 4);
      }

      // put the text above the cell...
      upperY -= (rect.height / 2);
      int i = 0;

      // height/X is not good...but near.
      // to have the border a bit away from text
      int lmax = (getMaxWidth(display) + (height / 4));
      fillRectangle(middleX - (lmax / 2), upperY - height, lmax, ((display.length) * height)
          + (height / 4));

      int leftX = middleX - (getMaxWidth(display) / 2);
      int rightX = middleX + (getMaxWidth(display) / 2);

      i = 0;
      for (String s : display) {
        switch (hAlign) {
        case LEFT:
          drawString(graphics, s, leftX, upperY + (i * height));
          break;
        case CENTER:
          int l = getWidth(s);
          drawString(graphics, s, middleX - (l / 2), upperY + (i * height));
          break;
        case RIGHT:
          drawString(graphics, s, rightX - getWidth(s), upperY + (i * height));
          break;
        }
        i++;
      }
    }
  }

  protected int getWidth(String string) {
    return (int) font.getStringBounds(string, graphics.getFontRenderContext()).getWidth();
  }

  protected int getWidth(char[] chars, int begin, int limit) {
    return (int) font.getStringBounds(chars, begin, limit, graphics.getFontRenderContext())
        .getWidth();
  }

  private int getMaxWidth(String[] display) {
    int maxWidth = -1;
    for (String s : display) {
      if (getWidth(s) > maxWidth) {
        maxWidth = getWidth(s);
      }
    }

    return maxWidth;
  }

  private int getHeight(String[] display) {
    float height = 1;
    for (String text : display) {
      height =
          Math.max(height, font.getLineMetrics(text, graphics.getFontRenderContext()).getHeight());
    }
    return (int) (height * 0.9);
  }

  private void drawString(Graphics graphic, String text, int X, int Y) {
    graphics.setColor(fontColor);
    graphics.drawString(text, X, Y);
  }

  private void fillRectangle(int X, int Y, int width, int height) {
    if (isDrawingFrame()) {
      graphics.setColor(fontColor);
      graphics.drawRect(X - 1, Y - 1, width + 1, height + 1);
      graphics.fillRect(X + (width / 2), Y + height, 2, height / 2);
    }
    graphics.setColor(backColor);
    graphics.fillRect(X, Y, width, height);
  }

  @Override
  public PreferencesAdapter getPreferencesAdapter() {
    return new Preferences(this);
  }

  protected class Preferences extends JPanel implements PreferencesAdapter {
    // The source component to configure
    protected SignTextCellRenderer source;
    private final Logger log = Logger.getInstance(Preferences.class);

    // GUI elements
    private JPanel pnlFontColor;
    private JPanel pnlBackColor;
    private JComboBox<String> cmbFontName;
    private JCheckBox chkFontBold;
    private JComboBox<Integer> cmbFontSize;
    private JComboBox<Integer> cmbMinimumFontSize;
    private ButtonGroup alignment;
    private JCheckBox chkFrame;
    private JCheckBox chkScaleFont;
    private JCheckBox chkDrawLines;

    /**
     * Creates a new Preferences object.
     */
    public Preferences(SignTextCellRenderer r) {
      source = r;
      init();
    }

    private void init() {
      pnlFontColor = new JPanel();
      Dimension prefDim = SwingUtils.getDimension(1.5, 1.5, false);
      pnlFontColor.setPreferredSize(prefDim);
      pnlFontColor.setBackground(source.getFontColor());
      pnlFontColor.addMouseListener(new MouseAdapter() {
        @Override
        public void mousePressed(MouseEvent e) {
          Color newColor =
              JColorChooser.showDialog(pnlFontColor.getTopLevelAncestor(), Resources
                  .get("map.signtextcellrenderer.textcolor"), pnlFontColor.getBackground());

          if (newColor != null) {
            pnlFontColor.setBackground(newColor);
          }
        }
      });

      JLabel lblFontColor = new JLabel(Resources.get("map.signtextcellrenderer.fontcolor"));
      lblFontColor.setLabelFor(pnlFontColor);

      pnlBackColor = new JPanel();
      pnlBackColor.setPreferredSize(prefDim);
      pnlBackColor.setBackground(source.getBackColor());
      pnlBackColor.addMouseListener(new MouseAdapter() {
        @Override
        public void mousePressed(MouseEvent e) {
          Color newColor =
              JColorChooser.showDialog(pnlBackColor.getTopLevelAncestor(), Resources
                  .get("map.signtextcellrenderer.backcolor"), pnlBackColor.getBackground());

          if (newColor != null) {
            pnlBackColor.setBackground(newColor);
          }
        }
      });

      JLabel lblBackColor = new JLabel(Resources.get("map.signtextcellrenderer.backcolor"));
      lblFontColor.setLabelFor(pnlBackColor);

      chkFrame = new JCheckBox(Resources.get("map.signtextcellrenderer.drawframe"), source
          .isDrawingFrame());

      alignment = new ButtonGroup();

      JRadioButton rbutton;
      alignment.add(rbutton = new JRadioButton(Resources.get("map.signtextcellrenderer.alignleft")));
      rbutton.setActionCommand("LEFT");
      rbutton.setSelected(source.getHAlign() == LEFT);
      alignment.add(rbutton = new JRadioButton(Resources.get("map.signtextcellrenderer.aligncenter")));
      rbutton.setActionCommand("CENTER");
      rbutton.setSelected(source.getHAlign() != RIGHT && source.getHAlign() != LEFT);
      alignment.add(rbutton = new JRadioButton(Resources.get("map.signtextcellrenderer.alignright")));
      rbutton.setActionCommand("RIGHT");
      rbutton.setSelected(source.getHAlign() == RIGHT);

      String fontNames[] = null;

      try {
        fontNames = GraphicsEnvironment.getLocalGraphicsEnvironment().getAvailableFontFamilyNames();
      } catch (NullPointerException e) {
        // FIXME(pavkovic) 2003.03.17: This is bad!
        log.error(
            "Probably your are running jdk1.4.1 on Apple. Perhaps we can keep Magellan running. But don't count on it!");
        fontNames = new String[0];
      }

      cmbFontName = new JComboBox<String>(fontNames);
      cmbFontName.setSelectedItem(source.getFont().getName());

      JLabel lblFontName = new JLabel(Resources.get("map.signtextcellrenderer.fonttype"));
      lblFontName.setLabelFor(cmbFontName);

      chkFontBold =
          new JCheckBox(Resources.get("map.signtextcellrenderer.usebold"), source.getFont()
              .getStyle() != Font.PLAIN);

      Font font = source.getFont();
      Integer[] fontSizes = new Integer[] {
          8, 9, 10, 11, 12, 14, 16, 18, 22
      };
      cmbFontSize = new JComboBox<Integer>(fontSizes);
      cmbFontSize.setEditable(true);
      cmbFontSize.setSelectedItem(font.getSize());

      JLabel lblFontSize = new JLabel(Resources.get("map.signtextcellrenderer.fontsize"));
      lblFontSize.setLabelFor(cmbFontSize);

      cmbMinimumFontSize = new JComboBox<Integer>(fontSizes);
      cmbMinimumFontSize.setEditable(true);
      cmbMinimumFontSize.setSelectedItem(source.getMinimumFontSize());

      JLabel lblMinimumFontSize =
          new JLabel(Resources.get("map.signtextcellrenderer.minimumfontsize"));
      lblMinimumFontSize.setLabelFor(cmbMinimumFontSize);

      chkScaleFont =
          new JCheckBox(Resources.get("map.signtextcellrenderer.scalefontwithmapzoom"), source
              .isScalingFont());

      chkDrawLines =
          new JCheckBox(Resources.get("map.signtextcellrenderer.drawinglines"), source
              .isDrawingLines());

      setLayout(new GridBagLayout());

      GridBagConstraints c = new GridBagConstraints();
      c.anchor = GridBagConstraints.WEST;
      c.gridx = 0;
      c.gridy = 0;
      this.add(lblFontColor, c);
      c.gridx = 1;
      this.add(pnlFontColor, c);

      c.gridx = 0;
      c.gridy++;
      this.add(lblBackColor, c);
      c.gridx = 1;
      this.add(pnlBackColor, c);

      c.gridx = 0;
      c.gridy++;
      this.add(chkFrame, c);

      c.gridx = 0;
      c.gridy++;
      c.gridwidth = 2;
      this.add(makeButtonGroup(alignment), c);
      c.gridwidth = 1;

      c.gridx = 0;
      c.gridy++;
      this.add(lblFontName, c);
      c.gridx = 1;
      this.add(cmbFontName, c);

      c.gridx = 0;
      c.gridy++;
      c.gridwidth = 2;
      this.add(chkFontBold, c);
      c.gridwidth = 1;

      c.gridx = 0;
      c.gridy++;
      this.add(lblFontSize, c);
      c.gridx = 1;
      this.add(cmbFontSize, c);

      c.gridx = 0;
      c.gridy++;
      this.add(lblMinimumFontSize, c);
      c.gridx = 1;
      this.add(cmbMinimumFontSize, c);

      c.gridx = 0;
      c.gridy++;
      c.gridwidth = 2;
      this.add(chkScaleFont, c);

      c.gridx = 0;
      c.gridy++;
      c.gridwidth = 2;
      this.add(chkDrawLines, c);
    }

    private Component makeButtonGroup(ButtonGroup group) {
      JPanel panel = new JPanel(new GridLayout(1, 0));
      Enumeration<AbstractButton> buttons = group.getElements();
      while (buttons.hasMoreElements()) {
        panel.add(buttons.nextElement());
      }
      return panel;
    }

    public void initPreferences() {
      // TODO: implement it
    }

    /**
     * @see magellan.client.swing.preferences.PreferencesAdapter#applyPreferences()
     */
    public void applyPreferences() {
      source.setFontColor(pnlFontColor.getBackground());
      source.setBackColor(pnlBackColor.getBackground());

      source.setDrawingFrame(chkFrame.isSelected());
      switch (alignment.getSelection().getActionCommand()) {
      case "LEFT":
        source.setHAlign(LEFT);
        break;
      case "RIGHT":
        source.setHAlign(RIGHT);
        break;
      case "CENTER":
        source.setHAlign(CENTER);
        break;
      }

      String fontName = cmbFontName.getItemAt(cmbFontName.getSelectedIndex());
      int fontStyle = chkFontBold.isSelected() ? Font.BOLD : Font.PLAIN;
      Integer fontSize = 10, min = 8;
      fontSize = cmbFontSize.getItemAt(cmbFontSize.getSelectedIndex());
      if (fontSize == null) {
        fontSize = 10;
      }
      min = cmbMinimumFontSize.getItemAt(cmbMinimumFontSize.getSelectedIndex());
      if (min == null) {
        min = 8;
      }
      fontSize = Math.min(999, Math.max(0, fontSize));
      min = Math.min(999, Math.max(0, min));
      source.setFont(new Font(fontName, fontStyle, fontSize));
      source.setMinimumFontSize(min);
      source.setScalingFont(chkScaleFont.isSelected());
      source.setDrawingLines(chkDrawLines.isSelected());
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

  /**
   * @return the backColor
   */
  public Color getBackColor() {
    return backColor;
  }

  /**
   * Should the sign frame be drawn.
   */
  public boolean isDrawingFrame() {
    return drawFrame;
  }

  protected void setDrawingFrame(boolean b) {
    if (b != drawFrame) {
      drawFrame = b;
      settings.setProperty("SignTextCellRenderer.DrawFrame", String.valueOf(drawFrame));
    }
  }

  /**
   * @param backColor the backColor to set
   */
  public void setBackColor(Color backColor) {
    this.backColor = backColor;
    settings.setProperty("SignTextCellRenderer.backColor", Colors.encode(backColor));
  }

  /**
   * @see magellan.client.swing.map.HexCellRenderer#getName()
   */
  @Override
  public String getName() {
    return Resources.get("map.signtextcellrenderer.name");
  }

  public void setLinesOnly(boolean linesOnly) {
    LinesOnly = linesOnly;
  }

}
