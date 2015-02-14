// class magellan.plugin.extendedcommands.BeanShellEditor
// created on 24.02.2008
//
// Copyright 2003-2008 by magellan project team
//
// Author : $Author: $
// $Id: $
//
// This program is free software; you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation; either version 2 of the License, or
// (at your option) any later version.
//
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU General Public License for more details.
//
// You should have received a copy of the GNU General Public License
// along with this program (see doc/LICENCE.txt); if not, write to the
// Free Software Foundation, Inc.,
// 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
//
package magellan.plugin.extendedcommands;

import java.awt.Dimension;

import javax.swing.JEditorPane;
import javax.swing.JViewport;
import javax.swing.plaf.TextUI;
import javax.swing.text.AbstractDocument;
import javax.swing.text.Document;
import javax.swing.text.Element;
import javax.swing.text.ParagraphView;
import javax.swing.text.StyledEditorKit;
import javax.swing.text.TabSet;
import javax.swing.text.View;
import javax.swing.text.ViewFactory;

/**
 * A TextArea for BeanShell-Skripts. Including Syntax-Highlighting
 *
 * @author Thoralf Rickert
 */
public class BeanShellEditor extends JEditorPane {
  protected BeanShellSyntaxDocument document = null;

  public static final int TAB_WIDTH = 2;

  /**
   * Create the TextArea
   */
  public BeanShellEditor() {
    document = new BeanShellSyntaxDocument();
    int charWidth = getFontMetrics(document.getNormalFont()).stringWidth("        ") / 8;

    TabSet tabs = document.setTabs(charWidth, BeanShellEditor.TAB_WIDTH);

    setDocument(document);

    setEditorKitForContentType("text/beanshell", new TabSizeEditorKit(tabs, charWidth, TAB_WIDTH));
    setContentType("text/beanshell");
  }

  /**
   * Override to get no Line-Wraps
   */
  @Override
  public boolean getScrollableTracksViewportWidth() {
    if (getParent() instanceof JViewport) {
      JViewport port = (JViewport) getParent();
      TextUI ui = getUI();
      int w = port.getWidth();

      // Dimension min = ui.getMinimumSize(this);
      // Dimension max = ui.getMaximumSize(this);
      Dimension pref = ui.getPreferredSize(this);
      if ((w >= pref.width))
        return true;
    }
    return false;
  }

  public static class TabSizeEditorKit extends StyledEditorKit {

    private int charWidth;
    private int tabSize;
    private TabSet defaultTabs;

    public TabSizeEditorKit(TabSet tabs, int charWidth, int tabSize) {
      this.charWidth = charWidth;
      this.tabSize = tabSize;
      defaultTabs = tabs;
    }

    @Override
    public Document createDefaultDocument() {
      BeanShellSyntaxDocument document = new BeanShellSyntaxDocument();
      document.setTabs(charWidth, BeanShellEditor.TAB_WIDTH);
      return document;
    }

    @Override
    public ViewFactory getViewFactory() {
      return new MyViewFactory(super.getViewFactory());
    }

    class MyViewFactory implements ViewFactory {

      private ViewFactory defaultViewFactory;

      public MyViewFactory(ViewFactory viewFactory) {
        defaultViewFactory = viewFactory;
      }

      public View create(Element elem) {
        String kind = elem.getName();
        if (kind != null) {
          if (kind.equals(AbstractDocument.ParagraphElementName))
            return new CustomTabParagraphView(elem);
        }
        return defaultViewFactory.create(elem);
      }
    }

    class CustomTabParagraphView extends ParagraphView {

      public CustomTabParagraphView(Element elem) {
        super(elem);
      }

      @Override
      protected TabSet getTabSet() {
        TabSet tabs = super.getTabSet();
        if (tabs == null)
          return defaultTabs;
        return tabs;
      }

      @Override
      public float nextTabStop(float x, int tabOffset) {
        TabSet tabs = getTabSet();

        if (tabs == null)
          return (((int) x / (charWidth * tabSize) + 1) * charWidth * tabSize);

        return super.nextTabStop(x, tabOffset);
      }

    }
  }
}
