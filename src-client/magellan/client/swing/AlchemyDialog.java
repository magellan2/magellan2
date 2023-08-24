// class magellan.client.swing.AlchemyDialog
// created on Sep 21, 2009
//
// Copyright 2003-2009 by magellan project team
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
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
// GNU General Public License for more details.
//
// You should have received a copy of the GNU General Public License
// along with this program (see doc/LICENCE.txt); if not, write to the
// Free Software Foundation, Inc.,
// 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
//
package magellan.client.swing;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.KeyStroke;
import javax.swing.ListSelectionModel;
import javax.swing.SpringLayout;
import javax.swing.UIManager;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableModel;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import magellan.client.event.EventDispatcher;
import magellan.client.event.SelectionEvent;
import magellan.client.event.SelectionListener;
import magellan.client.swing.AlchemyDialog.PlannerModel.CurrentMaxValue;
import magellan.client.swing.AlchemyDialog.PlannerModel.HerbInfo;
import magellan.client.swing.AlchemyDialog.PlannerModel.IngredientValue;
import magellan.client.swing.AlchemyDialog.PlannerModel.MaxValue;
import magellan.client.swing.AlchemyDialog.PlannerModel.PotionInfo;
import magellan.client.swing.AlchemyDialog.PlannerModel.PotionValue;
import magellan.client.swing.AlchemyDialog.PlannerModel.ProductionValue;
import magellan.client.swing.AlchemyDialog.PlannerModel.RestValue;
import magellan.client.swing.AlchemyDialog.PlannerModel.StockValue;
import magellan.client.swing.AlchemyDialog.PlannerModel.ValueMarker;
import magellan.client.swing.basics.SpringUtilities;
import magellan.client.swing.table.TableSorter;
import magellan.client.swing.tree.ContextManager;
import magellan.client.utils.SwingUtils;
import magellan.client.utils.SwingUtils.RenderHelper;
import magellan.library.CoordinateID;
import magellan.library.EntityID;
import magellan.library.Faction;
import magellan.library.GameData;
import magellan.library.IntegerID;
import magellan.library.Item;
import magellan.library.LuxuryPrice;
import magellan.library.Potion;
import magellan.library.Region;
import magellan.library.Spell;
import magellan.library.StringID;
import magellan.library.Unit;
import magellan.library.event.GameDataEvent;
import magellan.library.impl.MagellanPotionImpl;
import magellan.library.io.file.FileBackup;
import magellan.library.rules.ItemType;
import magellan.library.utils.PropertiesHelper;
import magellan.library.utils.Resources;
import magellan.library.utils.comparator.FactionTrustComparator;
import magellan.library.utils.comparator.IDComparator;
import magellan.library.utils.comparator.NameComparator;
import magellan.library.utils.filters.CollectionFilters;

/**
 * A dialog for planning production of potions.
 * 
 * @author stm
 */
public class AlchemyDialog extends InternationalizedDataDialog implements SelectionListener {

  private static final magellan.library.utils.logging.Logger log =
      magellan.library.utils.logging.Logger.getInstance(AlchemyDialog.class);

  /**
   * The file extension used for writing files
   */
  public static final String FILE_EXTENSION = "axml";
  /**
   * the last saved property
   */
  public static String PROPERTYNAME_LAST_SAVED = "alchemydialog.lastSaved";

  private static String FILE_EXTENSION_DECRIPTION = "XML";

  private List<Region> regions;
  private PlannerModel tableModel;
  private TableSorter sorter;

  private List<Faction> activeFactions;

  private JTable planner;

  /**
   * Creates the dialog and makes it visible.
   * 
   * @param owner
   * @param dispatcher
   * @param data
   * @param settings
   * @param newRegions
   * @see InternationalizedDataDialog#InternationalizedDataDialog(Frame, boolean, EventDispatcher,
   *      GameData, Properties)
   */
  public AlchemyDialog(Frame owner, EventDispatcher dispatcher, GameData data, Properties settings,
      Collection<Region> newRegions) {
    super(owner, false, dispatcher, data, settings);

    // register for events
    // unnecessary
    // dispatcher.addGameDataListener(this);
    dispatcher.addSelectionListener(this);

    init();
    setRegions((newRegions == null) ? Collections.<Region> emptySet() : CollectionFilters
        .checkedCast(newRegions, Region.class));
  }

  /**
   * Create and initialize GUI.
   */
  private void init() {
    JPanel mainPanel = new JPanel(new BorderLayout());

    planner = createTable();

    mainPanel.add(createMenuBar(), BorderLayout.NORTH);
    mainPanel.add(new JScrollPane(planner), BorderLayout.CENTER);
    this.add(mainPanel);
    SwingUtils.setPreferredSize(mainPanel, 60, -1, true);

    pack();
  }

  @Override
  public void setVisible(boolean b) {
    super.setVisible(b);
    if (b) {
      SwingUtils.setBounds(this, settings, PropertiesHelper.ALCHEMY_DIALOG_BOUNDS, false);
    } else {
      PropertiesHelper.saveRectangle(settings, getBounds(), PropertiesHelper.ALCHEMY_DIALOG_BOUNDS);
    }
  }

  private JMenuBar createMenuBar() {
    JMenuBar menuBar = new JMenuBar();

    JMenu menu;
    menuBar.add(menu = new JMenu(Resources.get("alchemydialog.menu.file.title")));

    menu.add(new AbstractAction(Resources.get("alchemydialog.menu.load.title")) {
      public void actionPerformed(ActionEvent e) {
        loadTable();
      }
    });
    menu.add(new AbstractAction(Resources.get("alchemydialog.menu.save.title")) {
      public void actionPerformed(ActionEvent e) {
        saveTable();
      }
    });
    menu.add(new JSeparator());
    AbstractAction quitAction = new AbstractAction(Resources.get("alchemydialog.menu.quit.title")) {
      public void actionPerformed(ActionEvent e) {
        setVisible(false);
      }

    };
    JMenuItem quitItem;
    menu.add(quitItem = new JMenuItem(quitAction));
    quitItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_W, InputEvent.CTRL_DOWN_MASK));

    menuBar.add(menu = new JMenu(Resources.get("alchemydialog.menu.update.title")));

    menu.add(new AbstractAction(Resources.get("alchemydialog.menu.regionupdate.title")) {
      public void actionPerformed(ActionEvent e) {
        updateTableFromRegions();
      }
    });
    menu.add(new AbstractAction(Resources.get("alchemydialog.menu.factionupdate.title")) {
      public void actionPerformed(ActionEvent e) {
        updateTableFromFaction();
      }
    });
    menu.add(new AbstractAction(Resources.get("alchemydialog.menu.factionselection.title")) {
      public void actionPerformed(ActionEvent e) {
        selectFactions();
      }
    });
    menu.add(new JSeparator());
    menu.add(new AbstractAction(Resources.get("alchemydialog.menu.resetproduction.title")) {
      public void actionPerformed(ActionEvent e) {
        resetProduction();
      }
    });

    menuBar.add(menu = new JMenu(Resources.get("alchemydialog.menu.data.title")));
    menu.add(new AbstractAction(Resources.get("alchemydialog.menu.addherb.title")) {
      public void actionPerformed(ActionEvent e) {
        addHerb();
      }
    });
    menu.add(new AbstractAction(Resources.get("alchemydialog.menu.addpotion.title")) {
      public void actionPerformed(ActionEvent e) {
        addPotion();
      }
    });
    menu.add(new AbstractAction(Resources.get("alchemydialog.menu.removeherb.title")) {
      public void actionPerformed(ActionEvent e) {
        removeHerb();
      }
    });
    menu.add(new AbstractAction(Resources.get("alchemydialog.menu.removepotion.title")) {
      public void actionPerformed(ActionEvent e) {
        removePotion();
      }
    });
    menu.add(new AbstractAction(Resources.get("alchemydialog.menu.reload.title")) {
      public void actionPerformed(ActionEvent e) {
        gameDataChanged(new GameDataEvent(this, data));
      }
    });

    return menuBar;

  }

  protected void removePotion() {
    if (planner.getSelectedColumn() > 0) {
      tableModel.removePotion(view2modelColumn((planner.getSelectedColumn())));
    }
  }

  protected void removeHerb() {
    if (planner.getSelectedRow() > 0) {
      tableModel.removeHerb(view2modelRow((planner.getSelectedRow())));
    }
  }

  protected void addPotion() {
    String answer =
        JOptionPane.showInputDialog(this, Resources.get("alchemydialog.addpotion.name.label"));
    if (answer == null)
      return;

    for (Potion p : data.getPotions()) {
      if (p.getName().equals(answer)) {
        tableModel.addPotion(new PotionInfo(p));
        return;
      }
    }
    for (Spell s : data.getSpells()) {
      if (s.getName().equals(answer)) {
        tableModel.addPotion(new PotionInfo(s));
        break;
      }
    }

    tableModel.addPotion(new PotionInfo(answer));
  }

  protected void addHerb() {
    String answer =
        JOptionPane.showInputDialog(this, Resources.get("alchemydialog.addherb.name.label"));
    if (answer == null)
      return;

    ItemType ingredient = data.getRules().getItemType(answer);
    if (ingredient == null) {
      // warning: this is an item type outside the rules!
      ingredient = new ItemType(StringID.create(answer));
      ingredient.setName(answer);
    }
    tableModel.addHerb(new HerbInfo(ingredient));
  }

  /**
   * Creates the main content of the dialog, the production table.
   */
  private JTable createTable() {
    tableModel = new PlannerModel(getData());
    RenderHelper renderHelper = SwingUtils.prepareTable(tableModel);

    final JTable table =
        new AlchemyTable(sorter = new TableSorter(tableModel)) {
          @Override
          public Component prepareRenderer(TableCellRenderer renderer, int row, int column) {
            return renderHelper.wrapPrepareHandlerRowHeightAdjusted(this, row,
                super.prepareRenderer(renderer, row, column));
          }
        };

    table.getModel().addTableModelListener(new TableModelListener() {

      public void tableChanged(TableModelEvent e) {
        AlchemyDialog.this.validate();
        AlchemyDialog.this.repaint();
      }
    });
    // // the java 1.6 way:
    // final JTable table = new AlchemyTable(tableModel = new PlannerModel(getData()));
    // table.setAutoCreateRowSorter(true);
    // TableRowSorter<PlannerModel> sorter = new TableRowSorter<PlannerModel>(tableModel);
    // table.setRowSorter(sorter);
    // for (int i = 2; i < tableModel.getColumnCount() - 1; ++i)
    // sorter.setComparator(i, getComp1(false));
    sorter.setTableHeader(table.getTableHeader());
    sorter.setColumnComparator(PotionValue.class, getComp1(false));
    sorter.setColumnComparator(String.class, getComp2(true));
    sorter.setColumnComparator(StockValue.class, getComp1(true));
    sorter.setColumnComparator(RestValue.class, getComp1(true));

    table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

    table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);

    table.getColumnModel().getColumn(0).setPreferredWidth(150);
    for (int i = 1; i < table.getColumnCount(); ++i) {
      table.getColumnModel().getColumn(i).setPreferredWidth(30);
    }

    table.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
    // table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);

    table.setDefaultEditor(ValueMarker.class, table.getDefaultEditor(String.class));

    // render table cells according to their types
    table.setDefaultRenderer(ValueMarker.class, getValueRenderer(table));

    // add context menu
    table.addMouseListener(getContextMenu());

    return table;
  }

  /**
   * Returns a comparator for sorting the first column
   * 
   * @param nullUp if <code>true</code>, <code>null</code> values will be sorted to the top.
   */
  private Comparator<Object> getComp2(final boolean nullUp) {
    return new Comparator<Object>() {
      /**
       * String (max possible) | null | null | MaxValue | ... String (remaining) | null | null |
       * CurrentMaxValue | ... String (planned) | null | null | ProductionValue | ... HerbInfo
       * (Herb1) | StockValue | RestValue | IngredientValue/null | ... HerbInfo (Herb2) | StockValue
       * | RestValue | IngredientValue/null | ... ...
       */
      public int compare(Object o1, Object o2) {
        if (o1 instanceof String) {
          if (o2 instanceof String)
            return ((String) o1).compareTo((String) o2);
          else
            return -1;
        }

        if (o2 instanceof String)
          return 1;

        ItemType val1 = null;
        if (o1 instanceof ItemType) {
          val1 = (ItemType) o1;
        }
        ItemType val2 = null;
        if (o2 instanceof ItemType) {
          val2 = (ItemType) o2;

        }
        if (val1 == null || val2 == null) {
          if (val2 != null)
            return nullUp ? -1 : 1;
          else if (val1 != null)
            return nullUp ? 1 : -1;
          return 0;
        }

        return val1.getName().compareTo(val2.getName());
      }
    };
  }

  /**
   * Returns a comparator for sorting the {@link ValueMarker} columns.
   * 
   * @param nullUp if <code>true</code>, <code>null</code> values will be sorted to the top.
   */
  private Comparator<Object> getComp1(final boolean nullUp) {

    return new Comparator<Object>() {
      /**
       * String (max possible) | null | null | MaxValue | ... String (remaining) | null | null |
       * CurrentMaxValue | ... String (planned) | null | null | ProductionValue | ... HerbInfo
       * (Herb1) | StockValue | RestValue | IngredientValue/null | ... HerbInfo (Herb2) | StockValue
       * | RestValue | IngredientValue/null | ... ...
       */
      public int compare(Object o1, Object o2) {
        ValueMarker val1 = null;
        if (o1 instanceof ValueMarker) {
          val1 = (ValueMarker) o1;
        }
        ValueMarker val2 = null;
        if (o2 instanceof ValueMarker) {
          val2 = (ValueMarker) o2;

        }
        if (val1 == null || val2 == null) {
          if (val2 != null)
            return nullUp ? -1 : 1;
          else if (val1 != null)
            return nullUp ? 1 : -1;
          return 0;
        }

        if (val1 instanceof MaxValue) {
          if (!(val2 instanceof MaxValue))
            return -1;
        }
        if (val2 instanceof MaxValue)
          return 1;

        if (val1 instanceof CurrentMaxValue) {
          if (!(val2 instanceof CurrentMaxValue))
            return -1;
        }
        if (val2 instanceof CurrentMaxValue)
          return 1;

        if (val1 instanceof ProductionValue) {
          if (!(val2 instanceof ProductionValue))
            return -1;
        }
        if (val2 instanceof ProductionValue)
          return 1;

        return val2.value - val1.value;
      }

    };
  }

  /**
   * Creates and returns the renderer that renders table cells depending on their
   * {@link ValueMarker} type.
   */
  private TableCellRenderer getValueRenderer(JTable table) {
    final TableCellRenderer defaultRenderer = table.getDefaultRenderer(Integer.class);
    return new TableCellRenderer() {

      private Font[] boldCache = new Font[2];
      private Font[] plainCache = new Font[2];
      private int miss = 0;

      public Component getTableCellRendererComponent(JTable pTable, Object value,
          boolean isSelected, boolean hasFocus, int row, int column) {
        Component c =
            defaultRenderer.getTableCellRendererComponent(pTable, value, isSelected, hasFocus, row,
                column);

        if (value instanceof StockValue) {
          // stock cells (editable) -- with border
          if (c instanceof JComponent) {
            ((JComponent) c).setBorder(BorderFactory.createLoweredBevelBorder());
          }

          c.setBackground(Color.WHITE);
          c.setForeground(Color.BLACK);
          c.setFont(getBold(c.getFont()));

          c.setEnabled(true);
          c.setFocusable(true);
        } else if ((value instanceof MaxValue) || (value instanceof CurrentMaxValue)
            || (value instanceof RestValue)) {
          // automatically updated cells
          c.setBackground(Color.LIGHT_GRAY);
          c.setForeground(Color.BLACK);
          c.setFont(getBold(c.getFont()));
          c.setEnabled(true);
          c.setFocusable(false);
        } else if (value instanceof ProductionValue) {
          // production cells (editable) -- with border
          if (c instanceof JComponent) {
            ((JComponent) c).setBorder(BorderFactory.createLoweredBevelBorder());
          }

          c.setBackground(Color.WHITE);
          c.setForeground(Color.BLACK);
          c.setFont(getBold(c.getFont()));
          c.setEnabled(true);
          c.setFocusable(true);
        } else if (value instanceof IngredientValue) {
          // ingredients cells -- editable, shaded
          c.setBackground(Color.CYAN);
          c.setForeground(Color.BLACK);
          c.setFont(getPlain(c.getFont()));
          c.setEnabled(true);
          c.setFocusable(true);
        } else {
          // other non-editable cells
          c.setBackground(Color.WHITE);
          c.setForeground(Color.BLACK);
          c.setFont(getPlain(c.getFont()));
          c.setEnabled(true);
          c.setFocusable(true);
        }
        return c;
      }

      private Font getPlain(Font font) {
        if (plainCache[0] != font) {
          ++miss;
          Font plain = font.deriveFont(Font.PLAIN);
          plainCache[0] = font;
          plainCache[1] = plain;
        }
        if (miss == 10 || miss == 100) {
          log.fine("cache miss " + miss);
        }
        return plainCache[1];
      }

      private Font getBold(Font font) {
        if (boldCache[0] != font) {
          ++miss;
          Font bold = font.deriveFont(Font.BOLD);
          boldCache[0] = font;
          boldCache[1] = bold;
        }
        if (miss == 10 || miss == 100) {
          log.fine("cache miss " + miss);
        }
        return boldCache[1];
      }
    };
  }

  /**
   * Returns the mouse listener that shows the context menu.
   */
  private MouseListener getContextMenu() {
    return new MouseAdapter() {
      @Override
      public void mouseClicked(MouseEvent e) {
        maybeShowPopup(e);
      }

      @Override
      public void mousePressed(MouseEvent e) {
        maybeShowPopup(e);
      }

      @Override
      public void mouseReleased(MouseEvent e) {
        maybeShowPopup(e);
      }

      private void maybeShowPopup(MouseEvent e) {
        if (e.isPopupTrigger()) {
          JPopupMenu menu = new JPopupMenu();
          final int col = view2modelColumn(planner.columnAtPoint(e.getPoint()));
          final int row = view2modelRow(planner.rowAtPoint(e.getPoint()));
          Object val = tableModel.getValueAt(row, col);
          StringBuilder text = new StringBuilder();
          text.append(tableModel.getColumnName(col)).append("/").append(
              tableModel.getValueAt(row, 0)).append(": ").append(val == null ? 0 : val);
          JMenuItem item = new JMenuItem(text.toString());
          item.setEnabled(false);
          menu.add(item);
          item = new JMenuItem(Resources.get("alchemydialog.contextmenu.removerow.title"));
          item.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e2) {
              tableModel.removeHerb(row);
            }
          });
          menu.add(item);
          item = new JMenuItem(Resources.get("alchemydialog.contextmenu.removecol.title"));
          item.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e2) {
              tableModel.removePotion(col);
            }
          });
          menu.add(item);
          ContextManager.showMenu(menu, AlchemyDialog.this, e.getX(), e.getY());

        }
      }
    };
  }

  /**
   * Set selected factions (used for stock updates).
   */
  public void setFactions(List<Faction> newFactions) {
    activeFactions = new ArrayList<Faction>(newFactions);
  }

  /**
   * Set selected regions (used for stock updates).
   */
  public void setRegions(Collection<Region> list) {
    if (list.isEmpty() && (data.getRegions() != null)) {
      regions = new ArrayList<Region>(data.getRegions());
    } else {
      regions = new ArrayList<Region>(list);
    }
  }

  /**
   * @see magellan.client.swing.InternationalizedDataDialog#gameDataChanged(magellan.library.event.GameDataEvent)
   */
  @Override
  public void gameDataChanged(GameDataEvent e) {
    super.gameDataChanged(e);
    if (tableModel != null) {
      tableModel.setData(data);
    }
    setRegions(Collections.<Region> emptyList());
  }

  /**
   * Set production to 0.
   */
  protected void resetProduction() {
    tableModel.resetProduction();
  }

  /**
   * Updates stocks with herbs in selected regions.
   */
  protected void updateTableFromRegions() {
    tableModel.updateFromRegions(regions);
  }

  /**
   * Update stocks with items of faction's units in selected regions.
   */
  protected void updateTableFromFaction() {
    tableModel.updateFromFactions(getFactions(), regions);
  }

  /**
   * Read dialog data from xml file.
   */
  protected void loadTable() {
    JFileChooser fc = new JFileChooser();
    EresseaFileFilter filter;
    fc.setFileFilter(new EresseaFileFilter(AlchemyDialog.FILE_EXTENSION,
        AlchemyDialog.FILE_EXTENSION_DECRIPTION));
    fc.setSelectedFile(new File(settings.getProperty(AlchemyDialog.PROPERTYNAME_LAST_SAVED, "")));
    fc.setDialogTitle(Resources.get("alchemydialog.loaddialog.title"));

    boolean error = false;
    if (fc.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
      try {
        settings.setProperty(AlchemyDialog.PROPERTYNAME_LAST_SAVED, fc.getSelectedFile()
            .getAbsolutePath());

        SAXParserFactory factory = SAXParserFactory.newInstance();
        SAXParser parser;
        parser = factory.newSAXParser();
        parser.parse(fc.getSelectedFile(), new AlchemyFileHandler());
      } catch (ParserConfigurationException e) {
        e.printStackTrace();
        error = true;
      } catch (SAXException e) {
        e.printStackTrace();
        error = true;
      } catch (IOException e) {
        e.printStackTrace();
        error = true;
      }
    }
    if (error) {
      JOptionPane.showMessageDialog(this, Resources.get("alchemydialog.parsingerror.message"));
    }
  }

  /**
   * Save dialog data to xml file.
   */
  protected void saveTable() {
    JFileChooser fc = new JFileChooser();
    fc.setFileFilter(new EresseaFileFilter(AlchemyDialog.FILE_EXTENSION,
        AlchemyDialog.FILE_EXTENSION_DECRIPTION));
    fc.setSelectedFile(new File(settings.getProperty(AlchemyDialog.PROPERTYNAME_LAST_SAVED, "")));
    fc.setDialogTitle(Resources.get("alchemydialog.savedialog.title"));

    if (fc.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
      XMLStreamWriter writer = null;
      FileOutputStream os = null;
      String error = null;
      try {
        settings.setProperty(AlchemyDialog.PROPERTYNAME_LAST_SAVED, fc.getSelectedFile()
            .getAbsolutePath());
        File name = null;
        name = fc.getSelectedFile();
        if (!fc.getFileFilter().equals(fc.getAcceptAllFileFilter())
            && !name.getName().endsWith("." + AlchemyDialog.FILE_EXTENSION)) {
          name = new File(name.getPath() + "." + AlchemyDialog.FILE_EXTENSION);
        }

        if (name.exists()) {
          // create backup file
          try {
            File backup = FileBackup.create(name);
            AlchemyDialog.log.info("Created backupfile " + backup);
          } catch (IOException ie) {
            AlchemyDialog.log.warn("Could not create backupfile for file " + name);
          }
        }

        {
          XMLOutputFactory output = XMLOutputFactory.newInstance();
          writer =
              output
                  .createXMLStreamWriter(os = new FileOutputStream(name), getData().getEncoding());
          writer.writeStartDocument(getData().getEncoding(), "1.0");
          writer.writeStartElement("alchemydialog");
          writer.writeAttribute("version", "0.1");
          writer.writeCharacters("\n");
          // writer.writeNamespace("date", "");
          for (Faction f : getFactions()) {
            writer.writeCharacters("\n");
            writer.writeEmptyElement("faction");
            writer.writeAttribute("id", String.valueOf((f.getID()).intValue()));
          }
          for (Region region : regions) {
            writer.writeCharacters("\n");
            writer.writeEmptyElement("region");
            writer.writeAttribute("coordinate", region.getCoordinate().toString());
          }
          for (HerbInfo info : tableModel.herbs) {
            writer.writeCharacters("\n");
            writer.writeEmptyElement("herb");
            // writer.writeAttribute("id", info.item.getID().toString());
            writer.writeAttribute("name", info.item.getName());
            writer.writeAttribute("amount", String.valueOf(info.number));
          }
          for (PotionInfo info : tableModel.getPotions()) {
            writer.writeCharacters("\n");
            writer.writeStartElement("potion");
            // writer.writeAttribute("id", info.getID().toString());
            writer.writeAttribute("name", info.getName());
            writer.writeAttribute("planned", String.valueOf(info.production));
            for (ItemType ingredient : info.ingredients.keySet()) {
              writer.writeCharacters("\n");
              writer.writeEmptyElement("ingredient");
              // writer.writeAttribute("id", ingredient.getID().toString());
              writer.writeAttribute("name", ingredient.getName());
              writer.writeAttribute("amount", info.ingredients.get(ingredient).toString());
            }
            writer.writeCharacters("\n");
            writer.writeEndElement();
          }
          writer.writeCharacters("\n");
          writer.writeEndDocument();
          writer.flush();
          os.flush();
        }
      } catch (XMLStreamException e) {
        e.printStackTrace();
        error = e.getLocalizedMessage();
      } catch (IOException e) {
        e.printStackTrace();
        error = e.getLocalizedMessage();
      } finally {
        try {
          if (os != null) {
            os.close();
          }
          if (writer != null) {
            writer.close();
          }
        } catch (XMLStreamException e) {
          e.printStackTrace();
          error = e.getLocalizedMessage();
        } catch (IOException e) {
          e.printStackTrace();
          error = e.getLocalizedMessage();
        }
        if (error != null) {
          JOptionPane.showMessageDialog(this, Resources.get("alchemydialog.savingerror.message",
              error));
        }
      }
    }
  }

  /**
   * Returns the currently selected factions.
   */
  private List<Faction> getFactions() {
    if (activeFactions == null) {
      Faction f = null;
      if (data.getOwnerFaction() != null) {
        f = data.getFaction(data.getOwnerFaction());
      } else if (data.getFactions().size() > 0) {
        f = data.getFactions().iterator().next();
      }
      if (f == null)
        return Collections.emptyList();
      else
        return Collections.singletonList(f);
    } else
      return activeFactions;
  }

  /**
   * Shows the faction selection dialog.
   */
  private void selectFactions() {
    new FactionDialog().setVisible(true);
  }

  /**
   * Update selected regions if necessary.
   * 
   * @see magellan.client.event.SelectionListener#selectionChanged(magellan.client.event.SelectionEvent)
   */
  public void selectionChanged(SelectionEvent se) {
    if ((se.getSelectionType() == SelectionEvent.ST_REGIONS) && (se.getSelectedObjects() != null)) {
      setRegions(CollectionFilters.uncheckedCast(se.getSelectedObjects(), Region.class));
    }
  }

  /**
   * Convert a row from the view (the JTable) to a row in the model ({@link #planner}). This takes
   * into account sorting by {@link #sorter} and by the JTable ({@link #planner}).
   * 
   * @param row row number in the view
   * @return row number in the model
   */
  protected int view2modelRow(int row) {
    return planner.convertRowIndexToModel(sorter.modelIndex(row));
  }

  /**
   * Convert a row from the view (the JTable) to a row in the model ({@link #planner}). This takes
   * into account sorting by the JTable ({@link #planner}).
   * 
   * @param col column number in the view
   * @return column number in the model
   */
  protected int view2modelColumn(int col) {
    return planner.convertColumnIndexToModel(col);
  }

  /**
   * A model for the potion planning table.
   */
  public static class PlannerModel extends AbstractTableModel {

    /**
     * 
     */
    public static class HerbInfo {

      /**
       * Initializes the herb infos.
       * 
       * @param herb The ingredient. Usually, but not necessarily an ItemType from data.rules.
       */
      public HerbInfo(ItemType herb) {
        item = herb;
        number = 0;
      }

      ItemType item;
      int number;
    }

    /**
     */
    public static class PotionInfo {
      /** */
      public HashMap<ItemType, Integer> ingredients;
      private Potion _potion;
      private Spell _spell;
      private String _name;
      /** */
      public int production;

      /**
       * Initializes the potions and their ingredients.
       * 
       * @param potion The potion. Usually, but not necessarily a potion from data.
       */
      public PotionInfo(Potion potion) {
        _potion = potion;
        ingredients = new HashMap<ItemType, Integer>();
        production = 0;

        for (Item ingredient : potion.ingredients()) {
          ingredients.put(ingredient.getItemType(), ingredient.getAmount());
        }
      }

      /** */
      public PotionInfo(Spell spell) {
        _spell = spell;
        ingredients = new HashMap<ItemType, Integer>();
        production = 0;

        for (Spell.Component ingredient : spell.getParsedComponents()) {
          if (ingredient.getItem() != null) {
            ingredients.put(ingredient.getItem(), ingredient.getAmount());
          }
        }
      }

      /** */
      public PotionInfo(String answer) {
        _name = answer;
        ingredients = new HashMap<ItemType, Integer>();
        production = 0;
      }

      @Override
      public String toString() {
        if (_potion != null)
          return _potion.toString();
        else if (_spell != null)
          return _spell.toString();
        else
          return _name;
      }

      /** */
      public String getName() {
        return toString();
      }
    }

    /** The column number of the name column */
    private static final int nameCol = 0;

    /** The column number of the stock column */
    private static final int stockCol = 1;

    /** The column number of the rest column */
    private static final int restCol = 2;

    /** The number of non-potion columns */
    private static final int fixedCols = 3;

    /** The column number of the maximum potions row */
    private static final int maxRow = 0;

    /** The column number of the available potions row */
    private static final int currentMaxRow = 1;

    /** The column number of the planned number of potions row */
    private static final int productionRow = 2;

    /** The column number of the maximum potions row */
    private static final int fixedRows = 3;

    /** A marker for the cells containing names */
    private static final int nameType = 0;

    /** A marker for the cells containing ingredient stocks */
    private static final int stockType = 1;

    /** A marker for the cells containing nothing */
    private static final int noType = 20;

    /** A marker for the cells containing remaining ingredients after reduction */
    private static final int restType = 2;

    /** A marker for the cells containing maximum possible production */
    private static final int maxType = 10;

    /** A marker for the cells containing still possible production */
    private static final int currentMaxType = 11;

    /** A marker for the cells containing production amount */
    private static final int productionType = 12;

    /** A marker for the cells containing ingredient value */
    private static final int ingredientType = 13;

    /** The available potions */
    protected List<PotionInfo> potions;

    /** The available ingredients */
    List<HerbInfo> herbs;

    /** A map to lookup HerbInfos quickly */
    private HashMap<ItemType, HerbInfo> herbMap;

    private GameData data;

    /**
     * @param data
     */
    public PlannerModel(GameData data) {
      this.data = data;
      potions = getPotions();
      herbs = getHerbs();
    }

    /**
     * Return the list of known ingredients.
     */
    private List<HerbInfo> getHerbs() {
      if (herbs != null)
        return herbs;

      getPotions();
      return herbs;
    }

    /**
     * Return the list of known potions;
     */
    public List<PotionInfo> getPotions() {
      if (potions != null)
        return potions;

      if (herbs == null) {
        herbs = new ArrayList<HerbInfo>();
        herbMap = new HashMap<ItemType, HerbInfo>();
      }

      potions = new ArrayList<PotionInfo>();
      // iterate through all potions and add ingredients
      for (Potion potion : data.getPotions()) {
        PotionInfo info = new PotionInfo(potion);
        potions.add(info);
        for (ItemType ingredient : info.ingredients.keySet()) {
          addHerb(ingredient);
        }
      }

      for (Region r : data.getRegions()) {
        if (r.getHerb() != null) {
          addHerb(r.getHerb());
        }

        if (r.getPrices() != null) {
          for (LuxuryPrice price : r.getPrices().values()) {
            addHerb(price.getItemType());
          }
        }
      }

      for (Spell spell : data.getSpells()) {
        PotionInfo info = new PotionInfo(spell);
        if (info.ingredients.size() > 0) {
          potions.add(info);
          for (ItemType ingredient : info.ingredients.keySet()) {
            addHerb(ingredient);
          }
        }
      }

      sortHerbs();

      sortPotions();

      return potions;
    }

    private void sortPotions() {
      Collections.sort(potions, new Comparator<PotionInfo>() {
        public int compare(PotionInfo o1, PotionInfo o2) {
          return o1.getName().compareTo(o2.getName());
        }
      });
    }

    private void sortHerbs() {
      Collections.sort(herbs, new Comparator<HerbInfo>() {
        public int compare(HerbInfo o1, HerbInfo o2) {
          return o1.item.getName().compareTo(o2.item.getName());
        }
      });
    }

    /**
     * For internal use only.
     * 
     * @param ingredient
     */
    private void addHerb(ItemType ingredient) {
      if (!herbMap.containsKey(ingredient)) {
        HerbInfo herbInfo = new HerbInfo(ingredient);
        herbs.add(herbInfo);
        herbMap.put(ingredient, herbInfo);
      }
    }

    /**
     * Add an ingredient. External use.
     * 
     * @param herbInfo
     */
    public void addHerb(HerbInfo herbInfo) {
      if (!herbMap.containsKey(herbInfo.item)) {
        herbs.add(herbInfo);
        herbMap.put(herbInfo.item, herbInfo);
      }
      sortHerbs();
      fireTableStructureChanged();
    }

    /**
     * Adds a potion. External use.
     * 
     * @param potionInfo
     * @return false if the potion was already in the model.
     */
    public boolean addPotion(PotionInfo potionInfo) {
      if (potions.contains(potionInfo))
        return false;

      potions.add(potionInfo);
      sortPotions();
      setPotions(potions);
      return true;
    }

    /**
     * Removes the herb of the specified row from the model.
     */
    public boolean removeHerb(int row) {
      HerbInfo herbInfo = getHerb(row);
      if (herbInfo == null)
        return false;
      if (herbMap.containsKey(herbInfo.item)) {
        herbs.remove(herbInfo);
        herbMap.remove(herbInfo.item);
        sortHerbs();
        fireTableStructureChanged();
        return true;
      }
      return false;
    }

    /**
     * Removes the potion of the specified column from the model.
     */
    public boolean removePotion(int col) {
      PotionInfo potionInfo = getPotion(col);
      if (potionInfo == null)
        return false;
      if (!potions.contains(potionInfo))
        return false;

      potions.remove(potionInfo);
      sortPotions();
      setPotions(potions);
      return true;
    }

    /**
     * Change the set of potions.
     * 
     * @param newPotions
     */
    public void setPotions(List<PotionInfo> newPotions) {
      potions = new ArrayList<PotionInfo>(newPotions);
      fireTableStructureChanged();
    }

    /**
     * Change the set of herbs.
     */
    public void setHerbs(List<HerbInfo> newHerbs) {
      herbs = new ArrayList<HerbInfo>(newHerbs);
      herbMap = new HashMap<ItemType, HerbInfo>();
      for (HerbInfo info : newHerbs) {
        herbMap.put(info.item, info);
      }
      fireTableStructureChanged();
    }

    /**
     * Change the underlying data. Rests everything.
     * 
     * @param data
     */
    public void setData(GameData data) {
      this.data = data;
      potions = null;
      herbs = null;
      potions = getPotions();
      herbs = getHerbs();
      fireTableStructureChanged();
    }

    /**
     * Set production to 0.
     */
    public void resetProduction() {
      for (PotionInfo potion : potions) {
        potion.production = 0;
      }
    }

    /**
     * Update stocks with items from units of specified factions in selected regions.
     * 
     * @param factions Units are taken from these factions.
     * @param regions Only units from these regions will be considered.
     */
    public void updateFromFactions(List<Faction> factions, List<Region> regions) {
      for (HerbInfo herb : herbs) {
        herb.number = 0;
      }
      for (Region region : regions) {
        for (Unit u : region.units()) {
          if (factions.contains(u.getFaction())) {
            for (Item item : u.getItems()) {
              if (herbMap.containsKey(item.getItemType())) {
                herbMap.get(item.getItemType()).number += item.getAmount();
              }
            }
          }
        }
      }
      fireTableDataChanged();
    }

    /**
     * Update stocks with region herbs in selected regions.
     */
    public void updateFromRegions(List<Region> regions) {
      for (HerbInfo herb : herbs) {
        herb.number = 0;
      }

      for (Region region : regions) {
        HerbInfo info = getInfo(region.getHerb());
        if (info != null) {
          info.number += 10;
        }

        if (region.getPrices() != null) {
          for (LuxuryPrice price : region.getPrices().values()) {
            info = getInfo(price.getItemType());
            if (info != null) {
              info.number += 10;
            }
          }
        }
      }
      fireTableDataChanged();
    }

    /**
     * Returns the HerbInfo corresponding to an item.
     */
    private HerbInfo getInfo(ItemType herb) {
      return herbMap.get(herb);
    }

    /**
     * @see javax.swing.table.TableModel#getColumnCount()
     */
    public int getColumnCount() {
      return potions.size() + PlannerModel.fixedCols;
    }

    /**
     * @see javax.swing.table.TableModel#getRowCount()
     */
    public int getRowCount() {
      return herbs.size() + PlannerModel.fixedRows;
    }

    /**
     * @see javax.swing.table.AbstractTableModel#getColumnName(int)
     */
    @Override
    public String getColumnName(int column) {
      switch (column) {
      case nameCol:
        return Resources.get("alchemydialog.colname.name.title");
      case stockCol:
        return Resources.get("alchemydialog.colname.number.title");
      case restCol:
        return Resources.get("alchemydialog.colname.rest.title");

      default:
        return potions.get(column - PlannerModel.fixedCols).getName();
      }
    }

    /**
     * @see javax.swing.table.AbstractTableModel#getColumnClass(int)
     */
    @Override
    public Class<?> getColumnClass(int columnIndex) {
      switch (getType(columnIndex)) {
      case nameType:
        return String.class;
      case stockType:
        return StockValue.class;
      case noType:
      case restType:
        return RestValue.class;
      case maxType:
      case currentMaxType:
      case productionType:
      case ingredientType:
        return PotionValue.class;
      default:
        return super.getColumnClass(columnIndex);
      }
    }

    /**
     * Returns the type of cell in this column.
     */
    private int getType(int columnIndex) {
      switch (columnIndex) {
      case nameCol:
        return PlannerModel.nameType;
      case restCol:
        return PlannerModel.restType;
      case stockCol:
        return PlannerModel.stockType;
      default:
        return PlannerModel.ingredientType;
      }
    }

    /**
     * Returns the cell type. One of {@link #nameType}, {@link #noType}, {@link #restType},
     * {@link #noType}, {@link #stockType}, {@link #maxType}, {@link #currentMaxType},
     * {@link #productionType}, {@link #ingredientType}. <code>
     * nameType (max possible) | noType | noType | maxType | ...
     * nameType (remaining) | noType | noType | currentMaxType | ...
     * nameType (planned) | noType | noType | productionType | ...
     * nameType (Herb1) | stockType | restType | ingredientType | ...
     * nameType (Herb2) | stockType | restType | ingredientType | ...
     * ...
     * <code>
     */
    private int getType(int rowIndex, int columnIndex) {
      switch (columnIndex) {
      case nameCol:
        return PlannerModel.nameType;
      case restCol:
        if (rowIndex < PlannerModel.fixedRows)
          return PlannerModel.noType;
        else
          return PlannerModel.restType;
      case stockCol:
        if (rowIndex < PlannerModel.fixedRows)
          return PlannerModel.noType;
        else
          return PlannerModel.stockType;
      default:
        switch (rowIndex) {
        case maxRow:
          return PlannerModel.maxType;
        case currentMaxRow:
          return PlannerModel.currentMaxType;
        case productionRow:
          return PlannerModel.productionType;
        default:
          return PlannerModel.ingredientType;
        }
      }
    }

    /**
     * A class for helping to identify cell types by
     * {@link TableCellRenderer#getTableCellRendererComponent(JTable, Object, boolean, boolean, int, int)}
     */
    public static class ValueMarker {
      private Integer value;

      /**       */
      public ValueMarker(Integer value) {
        this.value = value;
      }

      /**       */
      public ValueMarker(String value) {
        try {
          this.value = Integer.parseInt(value);
        } catch (NumberFormatException e) {
          this.value = 0;
        }
      }

      @Override
      public String toString() {
        if (value == null)
          return "";
        else
          return String.valueOf(value);
      }
    }

    /** Value for current stock */
    public static class StockValue extends ValueMarker {
      /**       */
      public StockValue(Integer value) {
        super(value);
      }

      /**       */
      public StockValue(String value) {
        super(value);
      }
    }

    /** Value for remaining herb */
    public static class RestValue extends ValueMarker {
      /**       */
      public RestValue(Integer value) {
        super(value);
      }

      /**       */
      public RestValue(String value) {
        super(value);
      }
    }

    /** Value for potion name */
    public static class PotionValue extends ValueMarker {
      /**       */
      public PotionValue(Integer value) {
        super(value);
      }

      /**       */
      public PotionValue(String value) {
        super(value);
      }
    }

    /** Value for maximum potion value */
    public static class MaxValue extends PotionValue {
      /**       */
      public MaxValue(Integer value) {
        super(value);
      }

      /**       */
      public MaxValue(String value) {
        super(value);
      }
    }

    /** Value for remaining max potion value */
    public static class CurrentMaxValue extends PotionValue {
      /**       */
      public CurrentMaxValue(Integer value) {
        super(value);
      }

      /**       */
      public CurrentMaxValue(String value) {
        super(value);
      }
    }

    /** Value for current production */
    public static class ProductionValue extends PotionValue {
      /**       */
      public ProductionValue(Integer value) {
        super(value);
      }

      /**       */
      public ProductionValue(String value) {
        super(value);
      }
    }

    /** Value for ingredient */
    public static class IngredientValue extends PotionValue {
      /**       */
      public IngredientValue(Integer value) {
        super(value);
      }

      /**       */
      public IngredientValue(String value) {
        super(value);
      }
    }

    /**
     * Returns the value at the specified cell this is either a String or a subclass of
     * {@link ValueMarker}. The table looks as follows: <code>
     * String (max possible) | null | null | MaxValue | ...
     * String (remaining) | null | null | CurrentMaxValue | ...
     * String (planned) | null | null | ProductionValue | ...
     * HerbInfo (Herb1) | StockValue | RestValue | IngredientValue/null | ...
     * HerbInfo (Herb2) | StockValue | RestValue | IngredientValue/null | ...
     * ...
     * </code>
     * 
     * @see javax.swing.table.TableModel#getValueAt(int, int)
     */
    public Object getValueAt(int rowIndex, int columnIndex) {
      switch (getType(rowIndex, columnIndex)) {
      case nameType:
        switch (rowIndex) {
        case maxRow:
          return Resources.get("alchemydialog.rowname.maxproduction.title");
        case currentMaxRow:
          return Resources.get("alchemydialog.rowname.rest.title");
        case productionRow:
          return Resources.get("alchemydialog.rowname.production.title");
        default:
          return getHerb(rowIndex).item;
        }
      case stockType:
        return new StockValue(getHerb(rowIndex).number);
      case restType:
        return new RestValue(getRest(getHerb(rowIndex)));
      case ingredientType:
        Integer val = getPotion(columnIndex).ingredients.get(getHerb(rowIndex).item);
        return (val == null || val == 0) ? null : new IngredientValue(val);
      case maxType:
        return new MaxValue(getMax(getPotion(columnIndex)));
      case currentMaxType:
        return new CurrentMaxValue(getRest(getPotion(columnIndex)));
      case productionType:
        return new ProductionValue(getPotion(columnIndex).production);
      default:
        return null;
      }

    }

    /**
     * Returns the herb corresponding to the specified table row or <code>null</code> on an invalid
     * row.
     */
    private HerbInfo getHerb(int rowIndex) {
      if (rowIndex < PlannerModel.fixedRows)
        return null;
      return herbs.get(rowIndex - PlannerModel.fixedRows);
    }

    /**
     * Returns the potion corresponding to the specified table column or <code>null</code> on an
     * invalid column.
     */
    private PotionInfo getPotion(int columnIndex) {
      if (columnIndex < PlannerModel.fixedRows)
        return null;
      return potions.get(columnIndex - PlannerModel.fixedCols);
    }

    /**
     * Return the maximal producible number of the potion, i.e., look ingredient with the lowest
     * stock is exhausted.
     */
    private int getMax(PotionInfo potionInfo) {
      int max = Integer.MAX_VALUE;
      for (HerbInfo herb : herbs) {
        if (potionInfo.ingredients.containsKey(herb.item)
            && potionInfo.ingredients.get(herb.item) > 0) {
          max = Math.min(max, herb.number / potionInfo.ingredients.get(herb.item));
        }
      }
      return max;
    }

    /**
     * Return the maximal producible number of the potion
     * <em>after the projected amount for each potion has been produced</em>.
     */
    private int getRest(PotionInfo potionInfo) {
      int max = Integer.MAX_VALUE;
      for (HerbInfo herb : herbs) {
        if (potionInfo.ingredients.containsKey(herb.item)
            && potionInfo.ingredients.get(herb.item) > 0) {
          max = Math.min(max, getRest(herb) / potionInfo.ingredients.get(herb.item));
        }
      }
      return max;
    }

    /**
     * Return the amount of the ingredient remaining after all projected amounts of potions have
     * been produced.
     */
    private int getRest(HerbInfo herb) {
      int rest = herb.number;
      for (PotionInfo potion : potions) {
        if (potion.ingredients.containsKey(herb.item)) {
          rest -= potion.production * potion.ingredients.get(herb.item);
        }
      }
      return rest;
    }

    /**
     * Only stock cells, production cells and ingredient cells are editable.
     * 
     * @see javax.swing.table.AbstractTableModel#isCellEditable(int, int)
     */
    @Override
    public boolean isCellEditable(int rowIndex, int columnIndex) {
      switch (getType(rowIndex, columnIndex)) {
      case stockType:
      case productionType:
      case ingredientType:
        return true;
      default:
        return false;
      }
    }

    /**
     * @see javax.swing.table.AbstractTableModel#setValueAt(java.lang.Object, int, int)
     */
    @Override
    public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
      super.setValueAt(aValue, rowIndex, columnIndex);

      Integer iValue = 0;
      if (aValue instanceof String) {
        try {
          iValue = Integer.parseInt((String) aValue);
        } catch (NumberFormatException e) {
          // ignore invalid value
        }
      } else if (aValue instanceof Integer) {
        iValue = (Integer) aValue;
      } else if (aValue instanceof ValueMarker) {
        iValue = ((ValueMarker) aValue).value;
      }

      switch (getType(rowIndex, columnIndex)) {
      case stockType:
        getHerb(rowIndex).number = iValue;
        break;
      case productionType:
        getPotion(columnIndex).production = iValue;
        break;
      case ingredientType:
        getPotion(columnIndex).ingredients.put(getHerb(rowIndex).item, iValue);
        break;
      default:
      }

      for (int pot = 0; pot < potions.size(); ++pot) {
        fireTableCellUpdated(PlannerModel.maxRow, pot + PlannerModel.fixedCols);
        fireTableCellUpdated(PlannerModel.currentMaxRow, pot + PlannerModel.fixedCols);
      }
      for (int herb = 0; herb < herbs.size(); ++herb) {
        fireTableCellUpdated(herb + PlannerModel.fixedRows, PlannerModel.restCol);
      }
    }

  }

  /**
   * Handler for parsing alchemy settings files.
   */
  public class AlchemyFileHandler extends DefaultHandler {

    private ArrayList<Region> newRegions;
    private ArrayList<HerbInfo> newHerbs;
    private ArrayList<PotionInfo> newPotions;
    private PotionInfo currentPotion;
    // private Map<ID, ItemType> addedTypes;
    private ArrayList<Faction> newFactions;
    private int unknownFactions;
    private int unknownRegions;

    @Override
    public void startDocument() throws SAXException {
      newRegions = new ArrayList<Region>();
      newHerbs = new ArrayList<HerbInfo>();
      newPotions = new ArrayList<PotionInfo>();
      newFactions = new ArrayList<Faction>();
      // addedTypes = new HashMap<ID, ItemType>();
      unknownFactions = 0;
      unknownRegions = 0;
    }

    /**
     * @param uri The Namespace URI, or the empty string if the element has no Namespace URI or if
     *          Namespace processing is not being performed.
     * @param localName The local name (without prefix), or the empty string if Namespace processing
     *          is not being performed.
     * @param qName The qualified name (with prefix), or the empty string if qualified names are not
     *          available.
     * @param attributes The attributes attached to the element. If there are no attributes, it
     *          shall be an empty Attributes object.
     * @exception org.xml.sax.SAXException Any SAX exception, possibly wrapping another exception.
     * @see org.xml.sax.helpers.DefaultHandler#startElement(java.lang.String, java.lang.String,
     *      java.lang.String, org.xml.sax.Attributes)
     */
    @Override
    public void startElement(String uri, String localName, String qName, Attributes attributes)
        throws SAXException {
      if (qName.equals("faction")) {
        Faction faction = getData().getFaction(getEntityID(attributes));
        if (faction == null) {
          unknownFactions++;
          // AlchemyDialog.log.warn("unknown faction in alchemy file: "
          // + getEntityID(attributes).toString());
        } else {
          newFactions.add(faction);
        }
      } else if (qName.equals("region")) {
        Region region =
            getData().getRegion(CoordinateID.parse(attributes.getValue("coordinate"), ","));
        if (region == null) {
          unknownRegions++;
          // AlchemyDialog.log.warn("unknown region in alchemy file: "
          // + attributes.getValue("coordinate"));
        } else {
          newRegions.add(region);
        }
      } else if (qName.equals("herb")) {
        ItemType herb = getHerb(attributes);
        HerbInfo info;
        newHerbs.add(info = new HerbInfo(herb));
        info.number = Integer.parseInt(attributes.getValue("amount"));
      } else if (qName.equals("potion")) {
        Potion potion = getPotion((attributes));
        PotionInfo info = new PotionInfo(potion);
        newPotions.add(info);
        info.production = Integer.parseInt(attributes.getValue("planned"));
        currentPotion = info;
      } else if (qName.equals("ingredient")) {
        currentPotion.ingredients.put(getHerb(attributes), Integer.parseInt(attributes
            .getValue("amount")));
      }
    }

    private ItemType getHerb(Attributes attributes) {
      for (ItemType type : getData().getRules().getItemTypes()) {
        if (type.getName().equals(attributes.getValue("name")))
          return type;
      }
      ItemType herb = new ItemType(StringID.create("-1"));
      herb.setName(attributes.getValue("name"));
      // addedTypes.put(getStringID(attributes), herb);
      return herb;
    }

    private Potion getPotion(Attributes attributes) {
      for (Potion p : getData().getPotions()) {
        if (p.getName().equals(attributes.getValue("name")))
          return p;
      }
      Potion potion = new MagellanPotionImpl(IntegerID.create(-1));
      potion.setName(attributes.getValue("name"));
      // addedPotion.put()
      return potion;
    }

    // private StringID getStringID(Attributes attributes) {
    // return StringID.create(attributes.getValue("id"));
    // }
    //
    private EntityID getEntityID(Attributes attributes) {
      return EntityID.createEntityID(attributes.getValue("id"), 10, getData().base);
    }

    // private ItemType getType(StringID id) {
    // ItemType type = getData().rules.getItemType(id);
    // if (type == null)
    // type = addedTypes.get(id);
    //
    // if (type == null)
    // throw new RuntimeException("unknown ingredient");
    //
    // return type;
    // }

    @Override
    public void endElement(String uri, String localName, String qName) throws SAXException {
      if (qName.equals("potion")) {
        currentPotion = null;
      }
    }

    @Override
    public void endDocument() throws SAXException {
      log.info("alchemy file read with " + newRegions.size() + " regions and " + unknownFactions
          + " unknown factions and " + unknownRegions + " unknown regions.");
      setRegions(newRegions);
      dispatcher.fire(SelectionEvent.create(AlchemyDialog.this, newRegions));
      setFactions(newFactions);
      tableModel.setHerbs(newHerbs);
      tableModel.setPotions(newPotions);
    }
  }

  private static FactionTrustComparator factionTrustComparator =
      FactionTrustComparator.DEFAULT_COMPARATOR;

  private static NameComparator nameComparator = new NameComparator(IDComparator.DEFAULT);

  /**
   * Dialog for selecting a faction from all factions in the report.
   */
  public class FactionDialog extends JDialog {
    /**
     * 
     */
    public FactionDialog() {
      JPanel mainPanel = new JPanel(new SpringLayout());
      DefaultListModel factionListModel = new DefaultListModel();

      LinkedList<Faction> sorted = new LinkedList<Faction>(getData().getFactions());
      String sortByTrustLevel = settings.getProperty("FactionStatsDialog.SortByTrustLevel", "true");
      // sort factions
      if (sortByTrustLevel.equals("true")) {
        Collections.sort(sorted, factionTrustComparator);
      } else if (sortByTrustLevel.equals("detailed")) {
        Collections.sort(sorted, FactionTrustComparator.DETAILED_COMPARATOR);
      } else {
        Collections.sort(sorted, nameComparator);
      }

      for (Faction f : sorted) {
        factionListModel.addElement(f);
      }
      final JList factionList = new JList(factionListModel);
      factionList.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
      factionList.clearSelection();
      List<Faction> factions = getFactions();
      for (int row = 0; row < factionListModel.size(); ++row) {
        if (factions.contains(factionListModel.get(row))) {
          factionList.addSelectionInterval(row, row);
        }
      }
      factionList.setVisibleRowCount(10);
      mainPanel.add(new JScrollPane(factionList));
      JButton okButton = new JButton(Resources.get("button.ok"));
      okButton.addActionListener(new ActionListener() {

        public void actionPerformed(ActionEvent e) {
          ArrayList<Faction> newFactions = new ArrayList<Faction>();
          for (Object o : factionList.getSelectedValues()) {
            newFactions.add((Faction) o);
          }
          setFactions(newFactions);
          setVisible(false);
        }
      });

      mainPanel.add(okButton);
      SpringUtilities.makeCompactGrid(mainPanel, 2, 1, 2, 2, 2, 2);

      this.add(mainPanel);
      pack();
    }
  }

  /**
   * Renders cells with linewrap.
   */
  public static class TextAreaRenderer extends JTextArea implements TableCellRenderer {

    /**
     * Creates a new renderer
     * 
     * @param table
     */
    public TextAreaRenderer(JTable table, int rows) {
      setLineWrap(true);
      setEditable(false);
      setWrapStyleWord(false);
      setOpaque(false);
      setBorder(UIManager.getBorder("TableHeader.cellBorder"));
      setRows(rows);
      // JTableHeader header = table.getTableHeader();
      // setForeground(header.getForeground());
      // setBackground(table.getTableHeader().getBackground());
      // setFont(header.getFont());
      // setPreferredSize(new Dimension(100, 40));
    }

    /**
     * @see javax.swing.table.TableCellRenderer#getTableCellRendererComponent(javax.swing.JTable,
     *      java.lang.Object, boolean, boolean, int, int)
     */
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
        boolean hasFocus, int row, int column) {

      setText((value == null) ? "" : value.toString());
      return this;
    }

  }

  /**
   * Rendering table cells with JLabels (does not work well).
   */
  public static class JLabelRenderer extends JLabel implements TableCellRenderer {

    /**
     */
    public JLabelRenderer(JTable table) {
      // setPreferredSize(new Dimension(50, 50));
    }

    /**
     * @see javax.swing.table.TableCellRenderer#getTableCellRendererComponent(javax.swing.JTable,
     *      java.lang.Object, boolean, boolean, int, int)
     */
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
        boolean hasFocus, int row, int column) {

      setText((value == null) ? "" : "<html>" + value.toString().replaceAll(" ", "<br>")
          + "</html>");

      return this;
    }

  }

  /**
   * A table with custom header.
   */
  public class AlchemyTable extends JTable {
    /**
     * @param model
     */
    public AlchemyTable(TableModel model) {
      super(model);
    }

    /**
     * Creates multiline headers with tooltips.
     * 
     * @see javax.swing.JTable#createDefaultTableHeader()
     */
    @Override
    protected JTableHeader createDefaultTableHeader() {
      return new JTableHeader(columnModel) {
        @Override
        public String getToolTipText(MouseEvent e) {
          java.awt.Point p = e.getPoint();
          int index = columnModel.getColumnIndexAtX(p.x);
          int realIndex = columnModel.getColumn(index).getModelIndex();

          return getModel().getColumnName(realIndex);
        }

        @Override
        public TableCellRenderer getDefaultRenderer() {
          TextAreaRenderer ta = new TextAreaRenderer(AlchemyTable.this, 3);
          return ta;
          // return new JLabelRenderer(null);
        }
      };
    }

  }

}
