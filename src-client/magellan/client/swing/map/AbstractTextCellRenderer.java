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
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Rectangle;

import magellan.client.MagellanContext;
import magellan.library.CoordinateID;
import magellan.library.GameData;
import magellan.library.Region;

/**
 * Abstract base class for text renderers. Several possibilities to change the output. New feature:
 * Alignment.
 * 
 * @author Andreas
 * @version 1.0
 */
public abstract class AbstractTextCellRenderer extends HexCellRenderer {
  protected Color fontColor = Color.black;
  protected Color brighterColor = Color.black.brighter();
  protected Font unscaledFont = null;
  protected Font font = null;
  // protected FontMetrics fontMetrics = null;
  protected int minimumFontSize = 10;
  // protected int fontHeight = 0;
  protected boolean isScalingFont = false;
  protected int hAlign = AbstractTextCellRenderer.CENTER;
  protected boolean shortenStrings = false;
  protected String singleString[] = new String[1];

  /** DOCUMENT-ME */
  public static final int LEFT = 0;

  /** DOCUMENT-ME */
  public static final int CENTER = 1;

  /** DOCUMENT-ME */
  public static final int RIGHT = 2;

  /**
   * Creates new AbstractTextCellRenderer
   */
  protected AbstractTextCellRenderer(CellGeometry geo, MagellanContext context) {
    super(geo, context);
  }

  protected Color getFontColor() {
    return fontColor;
  }

  protected void setFontColor(Color col) {
    fontColor = col;
    brighterColor = fontColor.brighter();
  }

  protected Font getFont() {
    return unscaledFont;
  }

  protected void setFont(Font f) {
    unscaledFont = f;
    setFont(1f);
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
    isScalingFont = b;
  }

  protected int getMinimumFontSize() {
    return minimumFontSize;
  }

  protected void setMinimumFontSize(int m) {
    minimumFontSize = m;
  }

  protected int getHAlign() {
    return hAlign;
  }

  protected void setHAlign(int i) {
    hAlign = i;
  }

  // protected FontMetrics getFontMetrics() {
  // if(fontMetrics == null) {
  // fontMetrics = Client.getDefaultFontMetrics(new Font("TimesRoman",Font.PLAIN, 10));
  // }
  //
  // return fontMetrics;
  // }

  protected boolean isShortenStrings() {
    return shortenStrings;
  }

  protected void setShortenStrings(boolean b) {
    shortenStrings = b;
  }

  /**
   * DOCUMENT-ME
   */
  @Override
  public int getPlaneIndex() {
    return Mapper.PLANE_TEXT;
  }

  /**
   * DOCUMENT-ME
   */
  public abstract String getSingleString(Region r, Rectangle rect);

  /**
   * DOCUMENT-ME
   */
  public abstract String[] getText(Region r, Rectangle rect);

  /**
   * @see magellan.client.swing.map.HexCellRenderer#init(magellan.library.GameData,
   *      java.awt.Graphics, java.awt.Rectangle)
   */
  @Override
  public void init(GameData data, Graphics g, Rectangle offset) {
    super.init(data, g, offset);

    if (isScalingFont) {
      setFont(cellGeo.getScaleFactor());
    } else {
      setFont(1.0f);
    }
  }

  /**
   * @see magellan.client.swing.map.HexCellRenderer#render(java.lang.Object, boolean, boolean)
   */
  @Override
  public void render(Object obj, boolean active, boolean selected) {
    if (obj instanceof Region) {
      Region r = (Region) obj;
      CoordinateID c = r.getCoordinate();
      Rectangle rect = cellGeo.getCellRect(c.getX(), c.getY());

      String display[] = getText(r, rect);

      if ((display == null) || (display.length == 0)) {
        singleString[0] = getSingleString(r, rect);

        if ((singleString[0] == null) || singleString[0].equals(""))
          return;

        display = singleString;
      }

      graphics.setFont(font);
      graphics.setColor(fontColor);

      shortenStrings(display, rect.width);

      int height = getMaxHeight(display);
      int middleX = (rect.x + (rect.width / 2)) - offset.x;
      int middleY = (rect.y + (rect.height / 2) + 1) - offset.y;
      int upperY = middleY;

      if (display.length == 1) {
        upperY += (height / 4);
      } else {
        upperY -= (((display.length - 1) * height) / 2) - height / 4;
      }

      switch (hAlign) {
      // left
      case LEFT:

        int leftX = middleX - (getMaxWidth(display) / 2);

        for (int i = 0; i < display.length; i++) {
          drawString(graphics, display[i], leftX, upperY + (i * height));
        }

        break;

      // center
      case CENTER:

        for (int i = 0; i < display.length; i++) {
          int l = getWidth(display[i]);
          drawString(graphics, display[i], middleX - (l / 2), upperY + (i * height));
        }

        break;

      // right
      case RIGHT:

        int rightX = middleX + (getMaxWidth(display) / 2);

        for (int i = 0; i < display.length; i++) {
          drawString(graphics, display[i], rightX - getWidth(display[i]), upperY + (i * height));
        }

        break;
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

  protected int getHeight(String[] display) {
    double h = 0;
    for (String text : display) {
      h += font.getStringBounds(text, graphics.getFontRenderContext()).getWidth();
    }
    return (int) h;
  }

  protected int getMaxHeight(String[] display) {
    float height = 1;
    for (String text : display) {
      height =
          Math.max(height, font.getLineMetrics(text, graphics.getFontRenderContext()).getHeight());
    }
    return (int) (height * 0.9);
  }

  private int getMaxWidth(String display[]) {
    int maxWidth = -1;

    for (String element : display) {
      if (getWidth(element) > maxWidth) {
        maxWidth = getWidth(element);
      }
    }

    return maxWidth;
  }

  private void drawString(Graphics graphic, String text, int X, int Y) {
    // graphics.setColor(brighterColor);
    // graphics.drawString(text, X+1, Y+1);
    graphics.setColor(fontColor);
    graphics.drawString(text, X, Y);
  }

  protected synchronized void shortenStrings(String str[], int maxWidth) {
    if (shortenStrings && (str != null) && (str.length > 0)) {
      for (int i = 0; i < str.length; i++) {
        if (str[i] != null) {
          if (getWidth(str[i]) > maxWidth) {
            char name[] = str[i].toCharArray();
            int nameLen = name.length;
            int nameWidth = 0;

            for (; nameLen > 0; nameLen--) {
              nameWidth = getWidth(name, 0, nameLen);

              if (nameWidth < (maxWidth * 0.9)) {
                if (nameLen < name.length) {
                  name[nameLen - 1] = '.';
                  name[nameLen] = '.';
                  nameLen++;
                }

                break;
              }
            }

            str[i] = new String(name, 0, nameLen);
          }
        }
      }
    }
  }
}
