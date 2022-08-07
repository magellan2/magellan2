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

package magellan.client.swing.tree;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.LayoutManager;
import java.awt.event.ComponentListener;
import java.awt.font.FontRenderContext;
import java.awt.font.LineBreakMeasurer;
import java.awt.font.TextAttribute;
import java.awt.font.TextLayout;
import java.text.AttributedCharacterIterator;
import java.text.AttributedString;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;

import javax.swing.Icon;
import javax.swing.JPanel;
import javax.swing.JTree;
import javax.swing.plaf.TreeUI;
import javax.swing.plaf.basic.BasicTreeUI;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.TreeCellRenderer;
import javax.swing.tree.TreePath;

import magellan.client.Client;
import magellan.library.Message;
import magellan.library.rules.MessageType;
import magellan.library.utils.PropertiesHelper;
import magellan.library.utils.Resources;
import magellan.library.utils.Utils;
import magellan.library.utils.logging.Logger;

/**
 * 
 * 
 * @author Andreas
 * @version 1.0
 */
public class LineWrapCellRenderer extends JPanel implements TreeCellRenderer, ComponentListener {
  private static final Logger log = Logger.getInstance(LineWrapCellRenderer.class);
  protected DefaultTreeCellRenderer defaultRenderer;
  protected Icon icon;
  protected String text;
  protected int lastLength = -1;
  protected Color textColor;
  protected Color textBackground;
  protected Color textFocus;
  protected boolean bold = false;
  protected LinkedList<TextLayout> buf = new LinkedList<TextLayout>();

  /** Holds value of property lineWrap. */
  private boolean lineWrap;

  /**
   * Creates new Template
   */
  public LineWrapCellRenderer(DefaultTreeCellRenderer def) {
    defaultRenderer = def;

    setOpaque(false);
    updateColors();
  }

  /**
   * 
   */
  public void registerTree(JTree tree) {
    tree.addComponentListener(this);
  }

  /**
   * 
   */
  public void unregisterTree(JTree tree) {
    tree.removeComponentListener(this);
  }

  /**
   * 
   */
  protected int getIndent(JTree tree, BasicTreeUI tui, int row) {
    int width = tui.getLeftChildIndent() + tui.getRightChildIndent();
    TreePath path = tree.getPathForRow(row);
    int j = 0;

    if (path != null) {
      j = path.getPathCount() - 1;
    }

    return (width * j);
  }

  /**
   * 
   */
  protected boolean computeSize(JTree tree, BasicTreeUI tui, int row) {
    int treeWidth = tree.getSize().width;

    int labelWidth = 0;

    if (icon != null) {
      labelWidth += (icon.getIconWidth() + 4);
    }

    int indent = 0;

    try {
      indent = getIndent(tree, tui, row);
      // FIXME(stm) workaround for an annoying bug, that caused too low rows
      if (row == -1) {
        indent += tui.getLeftChildIndent() + tui.getRightChildIndent();
      }
    } catch (Exception exc) {
      return false;
    }

    Insets in = getInsets();

    int maxLength = treeWidth - indent - labelWidth - 3 - in.left - in.right;

    lastLength = maxLength;

    AttributedString string = new AttributedString(text);
    if (bold) {
      string.addAttribute(TextAttribute.FONT, getFont().deriveFont(Font.BOLD));
    } else {
      string.addAttribute(TextAttribute.FONT, getFont());
    }

    AttributedCharacterIterator it = string.getIterator();
    FontRenderContext context = ((Graphics2D) tree.getGraphics()).getFontRenderContext();
    LineBreakMeasurer lbm = new LineBreakMeasurer(it, context);

    int height = 0;
    float help = 0;

    while (lbm.getPosition() < it.getEndIndex()) {
      TextLayout tl = lbm.nextLayout(maxLength);
      help += (tl.getAscent() + tl.getDescent());

      if (lbm.getPosition() < it.getEndIndex()) {
        help += tl.getLeading();
      }
    }

    height = (int) Math.ceil(help);

    if (icon != null) {
      height = Math.max(height, icon.getIconHeight());
    }

    setPreferredSize(new Dimension(treeWidth - indent - 1, height + in.top + in.bottom + 2));
    setMinimumSize(getPreferredSize());
    setMaximumSize(getMinimumSize());
    return true;
  }

  protected void computeOneLineSize(FontMetrics fm) {
    int height = fm.getAscent() + fm.getDescent() + 2;
    int width = fm.stringWidth(text) + 2;

    if (icon != null) {
      height = Math.max(height, icon.getIconHeight());
      width += (icon.getIconWidth() + 4);
    }

    setPreferredSize(new Dimension(width, height));
  }

  /**
   * 
   */
  @Override
  public void paintComponent(Graphics g) {
    int x = 0;
    int y = 1;

    Dimension size = getSize();

    if (icon != null) {
      y = Math.max(0, ((size.height - icon.getIconHeight()) / 2));
      icon.paintIcon(this, g, 0, y);
      y = 1;
      x = icon.getIconWidth() + 4;
    }

    x++;

    int height = 0;
    int width = 0;

    if ((lastLength != -1) && (g instanceof Graphics2D) && text.length() > 0) {
      // break text
      Graphics2D g2 = (Graphics2D) g;

      AttributedString string = new AttributedString(text);
      if (bold) {
        string.addAttribute(TextAttribute.FONT, getFont().deriveFont(Font.BOLD));
      } else {
        string.addAttribute(TextAttribute.FONT, getFont());
      }
      string.addAttribute(TextAttribute.FOREGROUND, textColor);

      AttributedCharacterIterator it = string.getIterator();
      FontRenderContext context = g2.getFontRenderContext();
      LineBreakMeasurer lbm = new LineBreakMeasurer(it, context);

      float help = 0;
      float help2 = 0;

      while (lbm.getPosition() < it.getEndIndex()) {
        TextLayout tl = lbm.nextLayout(lastLength);
        buf.add(tl);
        help += tl.getAscent();

        if (tl.getAdvance() > help2) {
          help2 = tl.getAdvance();
        }

        help += tl.getDescent();

        if (lbm.getPosition() < it.getEndIndex()) {
          help += tl.getLeading();
        }
      }

      height = (int) Math.ceil(help);
      width = (int) Math.ceil(help2);

      if (textBackground != null) {
        g.setColor(textBackground);
        g.fillRect(x, y, width, height);
      }

      help = 0;

      Iterator<TextLayout> it2 = buf.iterator();

      while (it2.hasNext()) {
        TextLayout tl = it2.next();
        help += tl.getAscent();
        tl.draw(g2, x, help);
        help += tl.getDescent();

        if (it2.hasNext()) {
          help += tl.getLeading();
        }
      }

      buf.clear();
    } else {
      // draw a singe text line
      FontMetrics fm = g.getFontMetrics(getFont());
      width = fm.stringWidth(text);
      height = fm.getAscent() + fm.getDescent();

      if (textBackground != null) {
        g.setColor(textBackground);
        g.fillRect(x, 0, width, height);
      }

      g.setColor(textColor);
      g.drawString(text, x, fm.getAscent());
    }

    if ((textFocus != null) && (width > 0) && (height > 0)) {
      g.setColor(textFocus);

      x--;
      g.drawRect(x, 0, width, height);
    }
  }

  /**
   * 
   */
  public java.awt.Component getTreeCellRendererComponent(JTree jTree, Object obj, boolean selected,
      boolean expanded, boolean leaf, int row, boolean hasFocus) {
    icon = null;
    textBackground = null;
    textFocus = null;
    lastLength = -1;

    Object userObject = null;

    if (obj instanceof DefaultMutableTreeNode) {
      DefaultMutableTreeNode node = (DefaultMutableTreeNode) obj;
      userObject = node.getUserObject();
    } else {
      userObject = obj;
    }

    String string = "";
    // FF: Default: white
    Color foregroundColor = Color.BLACK;
    bold = false;

    if (userObject instanceof Message) {
      Message message = (Message) userObject;
      string = message.getText();
      if (message.getMessageType() != null) {
        Color c = getColor(message.getMessageType());
        if (c != null) {
          foregroundColor = c;
        }
      }
    } else {
      string = userObject.toString();
      bold = true;
      Color c = getSectionColor(string);
      if (c != null) {
        foregroundColor = c;
      }
    }

    text = jTree.convertValueToText(string, selected, expanded, leaf, row, hasFocus);

    if (defaultRenderer != null) {
      if (selected) {
        textBackground = defaultRenderer.getBackgroundSelectionColor();
        textColor = defaultRenderer.getTextSelectionColor();
        textFocus = defaultRenderer.getBorderSelectionColor();
      } else {
        if (foregroundColor != null) {
          textColor = foregroundColor;
        } else {
          textColor = defaultRenderer.getTextNonSelectionColor();
        }
        textBackground = defaultRenderer.getBackgroundNonSelectionColor();
      }

      if (leaf) {
        icon = defaultRenderer.getLeafIcon();
      } else {
        if (expanded) {
          icon = defaultRenderer.getOpenIcon();
        } else {
          icon = defaultRenderer.getClosedIcon();
        }
      }
    } else {
      if (selected) {
        textBackground = Color.blue.brighter();
        textFocus = Color.blue;
      } else {
        textBackground = Color.white;
      }

      textColor = Color.black;
    }

    TreeUI tui = jTree.getUI();
    boolean init = false;

    if (lineWrap && tui instanceof BasicTreeUI) {
      try {
        init = computeSize(jTree, (BasicTreeUI) tui, row);
      } catch (Exception exc) {
        // false
      }
    }

    if (!init) {
      computeOneLineSize(jTree.getFontMetrics(getFont()));
    }

    return this;
  }

  Map<MessageType, Color> typeColorCache = new HashMap<>();
  Map<String, Color> sectionColorCache = new HashMap<>();

  /**
   * Notifies this renderer that color settings have changed.
   */
  public void updateColors() {
    sectionColorCache.clear();
    typeColorCache.clear();

    for (String section : Client.colorProperties[2]) {
      String caption = Resources.get("messagepanel.section." + section);
      String colorName = "messagetype.section." + section + ".color";
      Color color = getColor(colorName);
      sectionColorCache.put(caption, color);
    }

  }

  private Color getSectionColor(String section) {
    Color color = sectionColorCache.get(section);
    return color;
  }

  private Color getColor(MessageType messageType) {
    Color color = typeColorCache.get(messageType);
    if (color == null) {
      String colorName = PropertiesHelper.MESSAGETYPE_SECTION_UNKNOWN_COLOR;
      if (!messageType.equals(MessageType.NO_TYPE) && messageType.getSection() != null) {
        colorName = "messagetype.section." + messageType.getSection() + ".color";
      }

      color = getColor(colorName);
      typeColorCache.put(messageType, color);
    }
    return color;
  }

  private Color getColor(String colorName) {
    String colorProp = Client.INSTANCE.getProperties().getProperty(colorName);
    if (colorProp == null) {
      LineWrapCellRenderer.log.warnOnce("Property " + colorName + " not found.");
      colorProp = "-";
    }
    return Utils.getColor(colorProp);
  }

  /**
   * 
   */
  protected void updateTree(JTree tree) {
    TreeUI aui = tree.getUI();

    // Since there seems to be a kind of bug with JTree.treeDidChange(),
    // we have to try to let the UI compute the bounds manually.
    //
    // I have only found the way to temporary change indent values in BasicTreeUI
    // to let the cache be cleared.
    // Andreas
    //
    if (aui instanceof BasicTreeUI) {
      BasicTreeUI tui = (BasicTreeUI) aui;
      int i = tui.getLeftChildIndent();
      tui.setLeftChildIndent(i + 1);
      tui.setLeftChildIndent(i);
    }
  }

  /**
   * Informs trees on size changes to recompute their row bounds.
   * 
   * @see java.awt.event.ComponentListener#componentResized(java.awt.event.ComponentEvent)
   */
  public void componentResized(java.awt.event.ComponentEvent componentEvent) {
    Object o = componentEvent.getSource();

    if (o instanceof JTree) {
      updateTree((JTree) o);
    }
  }

  /**
   * @see java.awt.event.ComponentListener#componentMoved(java.awt.event.ComponentEvent)
   */
  public void componentMoved(java.awt.event.ComponentEvent componentEvent) {
    // nop
  }

  /**
   * @see java.awt.event.ComponentListener#componentShown(java.awt.event.ComponentEvent)
   */
  public void componentShown(java.awt.event.ComponentEvent componentEvent) {
    // nop
  }

  /**
   * @see java.awt.event.ComponentListener#componentHidden(java.awt.event.ComponentEvent)
   */
  public void componentHidden(java.awt.event.ComponentEvent componentEvent) {
    // nop
  }

  /**
   * Getter for property lineWrap.
   * 
   * @return Value of property lineWrap.
   */
  public boolean isLineWrap() {
    return lineWrap;
  }

  /**
   * Setter for property lineWrap.
   * 
   * @param lineWrap New value of property lineWrap.
   */
  public void setLineWrap(boolean lineWrap) {
    this.lineWrap = lineWrap;
  }

  // **********************************************************************
  /**
   * @author ...
   * @version 1.0, 15.11.2007
   */
  protected static class SimpleLayout implements LayoutManager {
    protected Dimension inDim = new Dimension(0, 0);
    protected boolean inCompute = false;

    /**
     * 
     */
    public void addLayoutComponent(java.lang.String str, java.awt.Component component) {
      // nop
    }

    /**
     * 
     */
    public void layoutContainer(java.awt.Container container) {
      // always use pref sizes
      Component c = container.getComponent(0);
      Dimension dim = c.getPreferredSize();
      Insets in = container.getInsets();
      c.setBounds(in.left, in.top, dim.width, dim.height);

      c = container.getComponent(1);

      Dimension dim2 = c.getPreferredSize();
      c.setBounds(in.left + dim.width, in.top, dim2.width, dim2.height);
    }

    /**
     * 
     */
    public void removeLayoutComponent(java.awt.Component component) {
      // nop
    }

    /**
     * 
     */
    public java.awt.Dimension minimumLayoutSize(java.awt.Container container) {
      return preferredLayoutSize(container);
    }

    /**
     * 
     */
    public java.awt.Dimension preferredLayoutSize(java.awt.Container container) {
      if (inCompute)
        return inDim;

      inCompute = true;

      Dimension dim = container.getComponent(0).getPreferredSize();
      Dimension dim2 = container.getComponent(1).getPreferredSize();
      Insets in = container.getInsets();
      dim.width += (dim2.width + in.left + in.right);
      dim.height = Math.max(dim.height, dim2.height) + in.top + in.bottom;
      inCompute = false;

      return dim;
    }
  }

}
