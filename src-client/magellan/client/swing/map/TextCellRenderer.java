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
import java.awt.FontMetrics;
import java.awt.GraphicsEnvironment;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.LinkedList;
import java.util.List;

import javax.swing.JCheckBox;
import javax.swing.JColorChooser;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;

import magellan.client.MagellanContext;
import magellan.client.swing.preferences.PreferencesAdapter;
import magellan.client.utils.Colors;
import magellan.library.CoordinateID;
import magellan.library.Region;
import magellan.library.utils.Resources;
import magellan.library.utils.logging.Logger;

/**
 * DOCUMENT-ME
 * 
 * @author $Author: $
 * @version $Revision: 299 $
 */
public class TextCellRenderer extends AbstractTextCellRenderer {

  // FIXME (stm) overrides fields from super class!
  protected Color fontColor = Color.black;
  protected Font unscaledFont = null;
  protected Font font = null;
  protected FontMetrics fontMetrics = null;
  protected int minimumFontSize = 10;
  protected int fontHeight = 0;
  protected boolean isScalingFont = false;
  protected String regNameAndCoord[] = new String[2];

  /**
   * Creates a new TextCellRenderer object.
   */
  public TextCellRenderer(CellGeometry geo, MagellanContext context) {
    super(geo, context);

    if (settings != null) {
      try {
        setFontColor(Colors.decode(settings.getProperty("TextCellRenderer.textColor", Colors
            .encode(fontColor))));
      } catch (NumberFormatException e) {
        Logger.getInstance(this.getClass()).error("settings error", e);
      }

      setScalingFont((Boolean.valueOf(settings.getProperty("TextCellRenderer.isScalingFont",
          "false"))).booleanValue());
    }

    setFont(new Font(settings.getProperty("TextCellRenderer.fontName", "SansSerif"), Integer
        .parseInt(settings.getProperty("TextCellRenderer.fontStyle", Font.PLAIN + "")), Integer
            .parseInt(settings.getProperty("TextCellRenderer.fontSize", 11 + ""))));
    setMinimumFontSize(Integer.parseInt(settings.getProperty("TextCellRenderer.minimumFontSize",
        "10")));
    setShortenStrings(true);
  }

  @Override
  protected void setFontColor(Color fontColor) {
    super.setFontColor(fontColor);
    settings.setProperty("TextCellRenderer.textColor", Colors.encode(fontColor));
  }

  @Override
  protected void setFont(Font newFont) {
    super.setFont(newFont);
    settings.setProperty("TextCellRenderer.fontName", newFont.getName());
    settings.setProperty("TextCellRenderer.fontStyle", Integer.toString(newFont.getStyle()));
    settings.setProperty("TextCellRenderer.fontSize", Integer.toString(newFont.getSize()));
  }

  @Override
  protected void setScalingFont(boolean bool) {
    if (bool != isScalingFont()) {
      super.setScalingFont(bool);
      settings.setProperty("TextCellRenderer.isScalingFont", String.valueOf(isScalingFont()));
    }
  }

  @Override
  protected void setMinimumFontSize(int size) {
    if (getMinimumFontSize() != size) {
      super.setMinimumFontSize(size);
      settings.setProperty("TextCellRenderer.minimumFontSize", size + "");
    }
  }

  /**
   * @see magellan.client.swing.map.HexCellRenderer#getPreferencesAdapter()
   */
  @Override
  public PreferencesAdapter getPreferencesAdapter() {
    return new Preferences(this);
  }

  /**
   * Returns a pair of (Name, [Coordinate]) if the coordinate's of r are both divisible by 2,
   * <code>null</code> otherwise.
   */
  @Override
  public String[] getText(Region r, Rectangle rect) {
    CoordinateID c = r.getCoordinate();

    if ((r.getName() == null) || ((c.getX() % 2) != 0) || ((c.getY() % 2) != 0))
      return null;

    regNameAndCoord[0] = r.getName();
    regNameAndCoord[1] = "[" + c.getX() + "," + c.getY() + "]";

    return regNameAndCoord;
  }

  /**
   * DOCUMENT-ME
   */
  @Override
  public String getSingleString(Region r, Rectangle rect) {
    if (r.getName() != null)
      return r.getName();

    return null;
  }

  protected class Preferences extends JPanel implements PreferencesAdapter {
    private final Logger log = Logger.getInstance(Preferences.class);
    // The source component to configure
    protected TextCellRenderer source = null;

    // GUI elements
    private JPanel pnlFontColor = null;
    private JComboBox cmbFontName = null;
    private JCheckBox chkFontBold = null;
    private JComboBox cmbFontSize = null;
    private JComboBox cmbMinimumFontSize = null;
    private JCheckBox chkScaleFont = null;

    /**
     * Creates a new Preferences object.
     */
    public Preferences(TextCellRenderer r) {
      source = r;
      init();
    }

    private void init() {
      pnlFontColor = new JPanel();
      pnlFontColor.setSize(50, 50);
      pnlFontColor.setBackground(source.getFontColor());
      pnlFontColor.addMouseListener(new MouseAdapter() {
        @Override
        public void mousePressed(MouseEvent e) {
          Color newColor =
              JColorChooser.showDialog(pnlFontColor.getTopLevelAncestor(), Resources
                  .get("map.textcellrenderer.textcolor"), pnlFontColor.getBackground());

          if (newColor != null) {
            pnlFontColor.setBackground(newColor);
          }
        }
      });

      JLabel lblFontColor = new JLabel(Resources.get("map.textcellrenderer.fontcolor"));
      lblFontColor.setLabelFor(pnlFontColor);

      String fontNames[] = null;

      try {
        fontNames = GraphicsEnvironment.getLocalGraphicsEnvironment().getAvailableFontFamilyNames();
      } catch (NullPointerException e) {
        // FIXME(pavkovic) 2003.03.17: This is bad!
        log.error(
            "Probably your are running jdk1.4.1 on Apple. Perhaps we can keep Magellan running. But don't count on it!");
        fontNames = new String[0];
      }

      cmbFontName = new JComboBox(fontNames);
      cmbFontName.setSelectedItem(source.getFont().getName());

      JLabel lblFontName = new JLabel(Resources.get("map.textcellrenderer.fonttype"));
      lblFontName.setLabelFor(cmbFontName);

      chkFontBold =
          new JCheckBox(Resources.get("map.textcellrenderer.usebold"),
              source.getFont().getStyle() != Font.PLAIN);

      Font font = source.getFont();
      List<String> fontSizes = new LinkedList<String>();
      fontSizes.add("8");
      fontSizes.add("9");
      fontSizes.add("10");
      fontSizes.add("11");
      fontSizes.add("12");
      fontSizes.add("14");
      fontSizes.add("16");
      fontSizes.add("18");
      fontSizes.add("22");
      cmbFontSize = new JComboBox(fontSizes.toArray());
      cmbFontSize.setEditable(true);
      cmbFontSize.setSelectedItem("" + font.getSize());

      JLabel lblFontSize = new JLabel(Resources.get("map.textcellrenderer.fontsize"));
      lblFontSize.setLabelFor(cmbFontSize);

      cmbMinimumFontSize = new JComboBox(fontSizes.toArray());
      cmbMinimumFontSize.setEditable(true);
      cmbMinimumFontSize.setSelectedItem("" + source.getMinimumFontSize());

      JLabel lblMinimumFontSize = new JLabel(Resources.get("map.textcellrenderer.minimumfontsize"));
      lblMinimumFontSize.setLabelFor(cmbMinimumFontSize);

      chkScaleFont =
          new JCheckBox(Resources.get("map.textcellrenderer.scalefontwithmapzoom"), source
              .isScalingFont());

      setLayout(new GridBagLayout());

      GridBagConstraints c =
          new GridBagConstraints(0, 0, 1, 1, 1, 1, GridBagConstraints.NORTHWEST,
              GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0);

      c.gridx = 0;
      c.gridy = 0;
      c.gridwidth = 2;
      c.fill = GridBagConstraints.BOTH;
      this.add(HexCellRenderer.createDescriptionPanel(
          Resources.get("map.textcellrenderer.description." + source.getName().trim().replace(" ", "")),
          this), c);
      c.gridwidth = 1;
      c.gridy++;
      this.add(lblFontColor, c);

      c.gridx++;
      c.fill = GridBagConstraints.VERTICAL;
      pnlFontColor.setMaximumSize(new Dimension(15, 15));
      this.add(pnlFontColor, c);

      c.gridx = 0;
      c.gridy++;
      c.fill = GridBagConstraints.BOTH;
      this.add(lblFontName, c);

      c.gridx++;
      this.add(cmbFontName, c);

      c.gridx = 0;
      c.gridy++;
      c.gridwidth = 2;
      this.add(chkFontBold, c);

      c.gridwidth = 1;
      c.gridy++;
      this.add(lblFontSize, c);

      c.gridx++;
      this.add(cmbFontSize, c);

      c.gridx = 0;
      c.gridy++;
      this.add(lblMinimumFontSize, c);

      c.gridx++;
      this.add(cmbMinimumFontSize, c);

      c.gridx = 0;
      c.gridy++;
      c.gridwidth = 2;
      this.add(chkScaleFont, c);
    }

    public void initPreferences() {
      // TODO: implement it
    }

    /**
     * DOCUMENT-ME
     */
    public void applyPreferences() {
      source.setFontColor(pnlFontColor.getBackground());

      String fontName = (String) cmbFontName.getSelectedItem();
      int fontStyle = chkFontBold.isSelected() ? Font.BOLD : Font.PLAIN;
      int fontSize = Integer.parseInt((String) cmbFontSize.getSelectedItem());
      source.setFont(new Font(fontName, fontStyle, fontSize));
      source.setMinimumFontSize(Integer.parseInt((String) cmbMinimumFontSize.getSelectedItem()));
      source.setScalingFont(chkScaleFont.isSelected());
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
      return source.getName();
    }
  }

  /**
   * @see magellan.client.swing.map.HexCellRenderer#getName()
   */
  @Override
  public String getName() {
    return Resources.get("map.textcellrenderer.name");
  }

}
