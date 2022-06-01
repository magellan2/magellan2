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

package magellan.client.utils;

import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.KeyStroke;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import magellan.client.Client;
import magellan.client.desktop.DesktopEnvironment;
import magellan.client.desktop.ShortcutListener;
import magellan.client.event.EventDispatcher;
import magellan.client.event.SelectionEvent;
import magellan.client.event.SelectionListener;
import magellan.client.swing.EresseaFileFilter;
import magellan.library.Bookmark;
import magellan.library.BookmarkBuilder;
import magellan.library.GameData;
import magellan.library.Selectable;
import magellan.library.event.GameDataEvent;
import magellan.library.event.GameDataListener;
import magellan.library.io.file.FileBackup;
import magellan.library.utils.CollectionFactory;
import magellan.library.utils.Encoding;
import magellan.library.utils.MagellanFactory;
import magellan.library.utils.PropertiesHelper;
import magellan.library.utils.Resources;
import magellan.library.utils.Utils;
import magellan.library.utils.logging.Logger;

/**
 * Manages setting an getting of Bookmarks.<br/>
 * - CTRL + F2 : place a bookmark on the current activeObject or removes it if already bookmarked<br/>
 * - F2 : go to next bookmark<br/>
 * - Shift + F2 : go to prior bookmark
 * 
 * @author Ulrich Küster
 */
public class BookmarkManager implements ShortcutListener, SelectionListener, GameDataListener {
  private static Logger log = Logger.getInstance(BookmarkManager.class);

  private static final String BOOKMARK_EXTENSION = ".xml";

  private static final String REGION = "region";
  private EventDispatcher dispatcher;
  private List<KeyStroke> shortCuts = new LinkedList<KeyStroke>();
  private Selectable activeObject = null;

  // the list containing the bookmarked objects
  private Map<Selectable, Bookmark> bookmarks = CollectionFactory.createOrderedMap();

  // the number of the current bookmark
  private int activeBookmark = 0;
  private BookmarkDock dialog;
  private GameData data;

  /**
   * Creates a new BookmarkManager object.
   */
  public BookmarkManager(EventDispatcher dispatcher) {
    this.dispatcher = dispatcher;
    dispatcher.addSelectionListener(this);
    dispatcher.addGameDataListener(this);

    shortCuts.add(KeyStroke.getKeyStroke(KeyEvent.VK_F2, 0));
    shortCuts.add(KeyStroke.getKeyStroke(KeyEvent.VK_F2, InputEvent.SHIFT_DOWN_MASK));
    shortCuts.add(KeyStroke.getKeyStroke(KeyEvent.VK_F2, InputEvent.CTRL_DOWN_MASK));
    DesktopEnvironment.registerShortcutListener(this);

    dialog = BookmarkDock.getInstance();
    dialog.init(this, dispatcher, false);
  }

  /**
   * 
   */
  public Iterator<KeyStroke> getShortCuts() {
    return shortCuts.iterator();
  }

  /**
   * 
   */
  public void shortCut(KeyStroke shortCut) {
    if (shortCut.equals(KeyStroke.getKeyStroke(KeyEvent.VK_F2, 0))) {
      jumpForward();
    } else if (shortCut.equals(KeyStroke.getKeyStroke(KeyEvent.VK_F2, InputEvent.SHIFT_DOWN_MASK))) {
      jumpBackward();
    } else if (shortCut.equals(KeyStroke.getKeyStroke(KeyEvent.VK_F2, InputEvent.CTRL_DOWN_MASK))) {
      toggleBookmark();
    }
  }

  /**
   * Bookmarks the given Object o, if it has not already been bookmarked. In this case o is deleted
   * from the bookmark list.
   */
  public void toggleBookmark(Object o) {
    if (o != null) {
      if (bookmarks.containsKey(o)) {
        data.removeBookmark((Selectable) o);
        bookmarks.remove(o);
        dispatcher.fire(new GameDataEvent(this, data, false));
        if (dialog != null) {
          dialog.updateData();
        }
      } else {
        addBookmark(o);
      }
    }
  }

  /**
   * Equivalent to <code>toggleBookmark(getActiveObject())</code>.
   */
  public void toggleBookmark() {
    toggleBookmark(getActiveObject());
  }

  /**
   * Differs from toggleBookmark in that way that it guarantees, that o is in the bookmark list
   * after the call
   */
  public void addBookmark(Object oo) {
    if (oo instanceof Selectable) {
      Selectable sel = (Selectable) oo;
      if (bookmarks.containsKey(sel)) {
        bookmarks.remove(sel);
      }

      if (activeBookmark < bookmarks.size()) {
        activeBookmark++;
      }

      BookmarkBuilder bb = MagellanFactory.createBookmark();
      bb.setObject(sel);
      bb.setName(sel.getName());
      bookmarks.put(sel, bb.getBookmark());
      data.addBookmark(bb.getBookmark());

      dispatcher.fire(new GameDataEvent(this, data, false));
      if (dialog != null) {
        dialog.updateData();
      }
    }
  }

  /**
   * Removes all bookmarks
   */
  public void clearBookmarks() {
    for (Bookmark bm : bookmarks.values()) {
      data.removeBookmark(bm.getObject());
    }
    bookmarks.clear();
    activeBookmark = 0;

    dispatcher.fire(new GameDataEvent(this, data, false));
    if (dialog != null) {
      dialog.updateData();
    }
  }

  /**
   * Jumps to the next bookmark by firing the corresponding selectionevent.
   */
  public void jumpForward() {
    jump(1);
  }

  /**
   * Jumps to the prior bookmark by firing the corresponding selectionevent.
   */
  public void jumpBackward() {
    jump(-1);
  }

  protected void jump(int direction) {
    if (bookmarks.size() > 0) {
      if ((activeBookmark < 0) || (activeBookmark >= bookmarks.size())) {
        // safety check
        activeBookmark = direction == 1 ? 0 : bookmarks.size() - 1;
      }

      int currentBookmark = 0;
      Bookmark bm = bookmarks.values().iterator().next();
      for (Iterator<Bookmark> it = bookmarks.values().iterator(); it.hasNext()
          && currentBookmark <= activeBookmark; ++currentBookmark) {
        bm = it.next();
      }

      if (bm != null && bm.getObject().equals(activeObject) && (bookmarks.size() > 1)) {
        activeBookmark += direction;
        jump(direction);
      } else if (bm != null && !bm.getObject().equals(activeObject)) {
        activeObject = bm.getObject();
        dispatcher.fire(SelectionEvent.create(this, bm.getObject(), SelectionEvent.ST_DEFAULT));

        if (dialog != null) {
          dialog.setSelectedObject(bm);
        }
      }
    }
  }

  /**
   * Returns the list of all bookmarks
   */
  public List<Bookmark> getBookmarks() {
    return new ArrayList<Bookmark>(bookmarks.values());
  }

  /**
   * Changes the active object.
   * 
   * @see magellan.client.event.SelectionListener#selectionChanged(magellan.client.event.SelectionEvent)
   */
  public void selectionChanged(SelectionEvent se) {
    if (!se.getSource().equals(this) && se.isSingleSelection()) {
      if (se.getActiveObject() instanceof Selectable) {
        activeObject = (Selectable) se.getActiveObject();
      } else {
        activeObject = null;
      }
    }
  }

  /**
   * Resets bookmark list.
   */
  public void gameDataChanged(GameDataEvent ge) {
    // if (ge.getGameData() != data) {
    data = ge.getGameData();
    activeObject = null;
    bookmarks.clear();
    activeBookmark = 0;

    for (Bookmark bm : data.getBookmarks()) {
      bookmarks.put(bm.getObject(), bm);
    }

    if (dialog != null) {
      dialog.updateData();
    }
    // }
  }

  /**
   * @see magellan.client.desktop.ShortcutListener#getShortcutDescription(javax.swing.KeyStroke)
   */
  public String getShortcutDescription(KeyStroke obj) {
    int index = shortCuts.indexOf(obj);

    return Resources.get("util.bookmarkmanager.shortcuts.description." + String.valueOf(index));
  }

  /**
   * @see magellan.client.desktop.ShortcutListener#getListenerDescription()
   */
  public String getListenerDescription() {
    return Resources.get("util.bookmarkmanager.shortcuts.title");
  }

  /**
   * Returns the currently active (i.e. selected elsewhere) object.
   */
  public Selectable getActiveObject() {
    return activeObject;
  }

  /**
   * Returns the value of activeBookmark.
   * 
   * @return Returns activeBookmark.
   */
  public int getActiveBookmark() {
    return activeBookmark;
  }

  /**
   * Sets the value of activeBookmark.
   * 
   * @param activeBookmark The value for activeBookmark.
   */
  public void setActiveBookmark(int activeBookmark) {
    this.activeBookmark = activeBookmark;
  }

  /**
   * Returns the value of dialog.
   * 
   * @return Returns dialog.
   */
  public BookmarkDock getDialog() {
    return dialog;
  }

  /**
   * Sets the value of dialog.
   * 
   * @param dialog The value for dialog.
   */
  public void setDialog(BookmarkDock dialog) {
    this.dialog = dialog;
  }

  /**
   * Sets the value of activeObject.
   * 
   * @param activeObject The value for activeObject.
   */
  public void setActiveObject(Selectable activeObject) {
    this.activeObject = activeObject;
  }

  public void saveBookmarks() {
    JFileChooser fc = new JFileChooser();
    fc.setFileFilter(new EresseaFileFilter(BOOKMARK_EXTENSION, Resources
        .get("util.bookmarkmanager.extensionfilter.name")));
    Client client = dispatcher.getMagellanContext().getClient();
    fc.setSelectedFile(new File(client.getProperties().getProperty(
        PropertiesHelper.BOOKMARKMANAGER_LASTFILE, "")));
    fc.setDialogTitle(Resources.get("util.bookmarkmanager.savedialog.title"));

    if (fc.showSaveDialog(client) == JFileChooser.APPROVE_OPTION) {
      PrintWriter pw = null;
      try {
        client.getProperties().setProperty(PropertiesHelper.BOOKMARKMANAGER_LASTFILE,
            fc.getSelectedFile().getAbsolutePath());

        EresseaFileFilter activeFilter = (EresseaFileFilter) fc.getFileFilter();
        File bookmarkFile = activeFilter.addExtension(fc.getSelectedFile());

        if (bookmarkFile.exists() && bookmarkFile.canWrite()) {
          // create backup file
          try {
            File backup = FileBackup.create(bookmarkFile);
            log.info("Created backupfile " + backup);
          } catch (IOException ie) {
            log.warn("Could not create backupfile for file " + bookmarkFile);
          }
        }
        if (bookmarkFile.exists() && !bookmarkFile.canWrite())
          throw new IOException("cannot write " + bookmarkFile);
        else {
          pw = new PrintWriter(bookmarkFile, Encoding.DEFAULT.toString());

          StringBuilder buffer = new StringBuilder();
          buffer.append("<?xml version='1.0' encoding='" + Encoding.DEFAULT.toString() + "'?>\r\n");
          buffer.append("<bookmarks version='1.0'>\r\n");

          for (Bookmark bm : data.getBookmarks()) {
            buffer.append("<bookmark type=\"");

            buffer.append(bm.getType());
            buffer.append("\" id=\"").append(bm.getObject().getID()).append("\"");
            if (bm.getName() != null) {
              buffer.append(" name=\"").append(bm.getName()).append("\"");
            }
            buffer.append(" />\n");
          }
          buffer.append("</bookmarks>");

          pw.println(buffer.toString());
          pw.close();
        }
      } catch (IOException exc) {
        log.error(exc);
        JOptionPane.showMessageDialog(client, exc.toString(), Resources
            .get("util.bookmarkmanager.msg.filesave.error.title"), JOptionPane.ERROR_MESSAGE);
      } finally {
        if (pw != null) {
          pw.close();
        }
      }
    }
  }

  public void loadBookmarks() {
    JFileChooser fc = new JFileChooser();
    fc.setFileFilter(new EresseaFileFilter(BOOKMARK_EXTENSION, Resources
        .get("util.bookmarkmanager.extensionfilter.name")));
    Client client = dispatcher.getMagellanContext().getClient();
    fc.setSelectedFile(new File(client.getProperties().getProperty(
        PropertiesHelper.BOOKMARKMANAGER_LASTFILE, "")));
    fc.setDialogTitle(Resources.get("util.bookmarkmanager.loaddialog.title"));

    if (fc.showOpenDialog(client) == JFileChooser.APPROVE_OPTION) {
      client.getProperties().setProperty(PropertiesHelper.BOOKMARKMANAGER_LASTFILE,
          fc.getSelectedFile().getAbsolutePath());

      EresseaFileFilter activeFilter = (EresseaFileFilter) fc.getFileFilter();
      File bookmarkFile = activeFilter.addExtension(fc.getSelectedFile());

      if (bookmarkFile.exists() && bookmarkFile.canRead()) {

        File file = bookmarkFile;
        List<Bookmark> bookmarks = new ArrayList<Bookmark>();
        try {
          DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
          Document document = builder.parse(file);
          if (!document.getDocumentElement().getNodeName().equals("bookmarks")) {
            log.fatal("The file " + file
                + " does NOT contain Bookmarks for Magellan. Missing XML root element 'bookmarks'");
            return;
          }

          load(bookmarks, document.getDocumentElement());
          for (Bookmark bm : bookmarks) {
            data.addBookmark(bm);
          }
          dispatcher.fire(new GameDataEvent(this, data, false));
        } catch (Exception exception) {
          log.error(exception);
          ErrorWindow errorWindow = new ErrorWindow("Could not load bookmarks.", exception);
          errorWindow.open();
        }
      }
    }
  }

  private void load(List<Bookmark> bookmarks2, Element root) {
    if (root.getNodeName().equalsIgnoreCase("bookmarks")) {
      List<Element> subnodes = Utils.getChildNodes(root);
      log.info("Found " + subnodes.size() + " bookmarks.");
      for (Element node : subnodes) {
        load(bookmarks2, node);
      }
    } else if (root.getNodeName().equalsIgnoreCase("bookmark")) {
      String type = root.getAttribute("type");
      String id = root.getAttribute("id");
      String name = root.getAttribute("name");
      Bookmark bm =
          MagellanFactory.createBookmark(data, type, id, name.length() == 0 ? null : name);

      if (bm != null) {
        bookmarks2.add(bm);
      } else {
        log.warn("unknown bookmark " + name + "(" + id + ")");
      }
    }
  }
}
