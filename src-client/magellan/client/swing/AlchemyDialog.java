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

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
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
import java.util.Map;
import java.util.Properties;

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JList;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JTable;
import javax.swing.KeyStroke;
import javax.swing.ListSelectionModel;
import javax.swing.SpringLayout;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableCellRenderer;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import magellan.client.event.EventDispatcher;
import magellan.client.event.SelectionEvent;
import magellan.client.event.SelectionListener;
import magellan.client.swing.AlchemyDialog.PlannerModel.CurrentMaxValue;
import magellan.client.swing.AlchemyDialog.PlannerModel.HerbInfo;
import magellan.client.swing.AlchemyDialog.PlannerModel.IngredientValue;
import magellan.client.swing.AlchemyDialog.PlannerModel.MaxValue;
import magellan.client.swing.AlchemyDialog.PlannerModel.PotionInfo;
import magellan.client.swing.AlchemyDialog.PlannerModel.ProductionValue;
import magellan.client.swing.AlchemyDialog.PlannerModel.RestValue;
import magellan.client.swing.AlchemyDialog.PlannerModel.StockValue;
import magellan.client.swing.AlchemyDialog.PlannerModel.ValueMarker;
import magellan.client.swing.basics.SpringUtilities;
import magellan.client.swing.table.TableSorter;
import magellan.library.CoordinateID;
import magellan.library.EntityID;
import magellan.library.Faction;
import magellan.library.GameData;
import magellan.library.ID;
import magellan.library.Item;
import magellan.library.Potion;
import magellan.library.Region;
import magellan.library.StringID;
import magellan.library.Unit;
import magellan.library.event.GameDataEvent;
import magellan.library.impl.MagellanPotionImpl;
import magellan.library.io.file.FileBackup;
import magellan.library.rules.ItemType;
import magellan.library.utils.Resources;
import magellan.library.utils.filters.CollectionFilters;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/**
 * A dialog for planning production of potions.
 * 
 * @author stm
 */
public class AlchemyDialog extends InternationalizedDataDialog implements SelectionListener {

  private static final magellan.library.utils.logging.Logger log =
      magellan.library.utils.logging.Logger.getInstance(AlchemyDialog.class);

  public static final String FILE_EXTENSION = "axml";
  public static String PROPERTYNAME_LAST_SAVED = "alchemydialog.lastSaved";
  private static String FILE_EXTENSION_DECRIPTION = "XML";

  private List<Region> regions;
  private PlannerModel model;

  private List<Faction> factions;

  /**
   * Creates the dialog and makes it visible.
   * 
   * @param client
   * @param dispatcher
   * @param data
   * @param properties
   * @param values
   */
  public AlchemyDialog(Frame owner, EventDispatcher dispatcher, GameData data, Properties settings,
      Collection<Region> newRegions) {
    super(owner, false, dispatcher, data, settings);

    // register for events
    dispatcher.addGameDataListener(this);
    dispatcher.addSelectionListener(this);

    init();
    setRegions((newRegions == null) ? Collections.EMPTY_SET : CollectionFilters.checkedCast(
        newRegions, Region.class));
  }

  /**
   * Create and initialize GUI.
   */
  private void init() {
    JPanel mainPanel = new JPanel(new GridBagLayout());
    GridBagConstraints con =
        new GridBagConstraints(0, 0, 1, 1, 1, 0, GridBagConstraints.FIRST_LINE_START,
            GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 0), 0, 0);

    JTable planner = createTable();
    // planner.setPreferredSize(new Dimension(400, 300));

    con.gridwidth = 1;

    mainPanel.add(createMenuBar(), con);

    con.weighty = 1;
    con.fill = GridBagConstraints.BOTH;
    con.gridy++;

    mainPanel.add(new JScrollPane(planner), con);

    this.add(mainPanel);
    this.setPreferredSize(new Dimension(800, 500));

    this.pack();
  }

  private JMenuBar createMenuBar() {
    JMenuBar menuBar = new JMenuBar();

    // FIXME (stm) this depends on the font!
    menuBar.setMinimumSize(new Dimension(10, 20));

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
    quitItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_W, KeyEvent.CTRL_MASK));

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

    return menuBar;

  }

  /**
   * Creates the main content of the dialog, the production table.
   */
  private JTable createTable() {
    TableSorter sorter;
    JTable table = new JTable(sorter = new TableSorter(model = new PlannerModel(getData())));
    table.getModel().addTableModelListener(new TableModelListener() {

      public void tableChanged(TableModelEvent e) {
        AlchemyDialog.this.validate();
        AlchemyDialog.this.repaint();
      }
    });
    sorter.setTableHeader(table.getTableHeader());

    table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

    final TableCellRenderer defaultRenderer = table.getDefaultRenderer(Integer.class);

    table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);

    for (int i = 0; i < table.getColumnCount(); ++i) {
      table.getColumnModel().getColumn(i).setPreferredWidth(30);
    }

    table.getColumnModel().getColumn(0).setPreferredWidth(150);
    table.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
    // table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);

    table.setDefaultEditor(ValueMarker.class, table.getDefaultEditor(String.class));

    // render table cells according to their types
    table.setDefaultRenderer(ValueMarker.class, new TableCellRenderer() {

      public Component getTableCellRendererComponent(JTable table, Object value,
          boolean isSelected, boolean hasFocus, int row, int column) {
        Component c =
            defaultRenderer.getTableCellRendererComponent(table, value, isSelected, hasFocus, row,
                column);

        if (value instanceof StockValue) {
          // stock cells (editable) -- with border
          if (c instanceof JComponent)
            ((JComponent) c).setBorder(BorderFactory.createLoweredBevelBorder());

          c.setBackground(Color.WHITE);
          c.setForeground(Color.BLACK);
          c.setFont(c.getFont().deriveFont(Font.BOLD));

          c.setEnabled(true);
          c.setFocusable(true);
        } else if ((value instanceof MaxValue) || (value instanceof CurrentMaxValue)
            || (value instanceof RestValue)) {
          // automatically updated cells
          c.setBackground(Color.LIGHT_GRAY);
          c.setForeground(Color.BLACK);
          c.setFont(c.getFont().deriveFont(Font.BOLD));
          c.setEnabled(true);
          c.setFocusable(false);
        } else if (value instanceof ProductionValue) {
          // production cells (editable) -- with border
          if (c instanceof JComponent)
            ((JComponent) c).setBorder(BorderFactory.createLoweredBevelBorder());

          c.setBackground(Color.WHITE);
          c.setForeground(Color.BLACK);
          c.setFont(c.getFont().deriveFont(Font.BOLD));
          c.setEnabled(true);
          c.setFocusable(true);
        } else if (value instanceof IngredientValue) {
          // ingredients cells -- editable, shaded
          c.setBackground(Color.CYAN);
          c.setForeground(Color.BLACK);
          c.setFont(c.getFont().deriveFont(Font.PLAIN));
          c.setEnabled(true);
          c.setFocusable(true);
        } else {
          // other non-editable cells
          c.setBackground(Color.WHITE);
          c.setForeground(Color.BLACK);
          c.setFont(c.getFont().deriveFont(Font.PLAIN));
          c.setEnabled(true);
          c.setFocusable(true);
        }

        return c;
      }
    });

    return table;
  }

  /**
   * Set selected factions (used for stock updates).
   */
  public void setFactions(List<Faction> newFactions) {
    factions = new ArrayList<Faction>(newFactions);
  }

  /**
   * Set selected regions (used for stock updates).
   */
  public void setRegions(Collection<Region> list) {
    if (list.isEmpty() && (data.regions() != null)) {
      list = data.regions().values();
    }

    regions = new LinkedList<Region>(list);
  }

  /**
   * @see magellan.client.swing.InternationalizedDataDialog#gameDataChanged(magellan.library.event.GameDataEvent)
   */
  @Override
  public void gameDataChanged(GameDataEvent e) {
    super.gameDataChanged(e);
    if (model != null)
      model.setData(data);
    setRegions(Collections.<Region> emptyList());
  }

  /**
   * Set production to 0.
   */
  protected void resetProduction() {
    model.resetProduction();
  }

  /**
   * Updates stocks with herbs in selected regions.
   */
  protected void updateTableFromRegions() {
    model.updateFromRegions(regions);
  }

  /**
   * Update stocks with items of faction's units in selected regions.
   */
  protected void updateTableFromFaction() {
    model.updateFromFactions(getFactions(), regions);
  }

  /**
   * Read dialog data from xml file.
   */
  protected void loadTable() {
    JFileChooser fc = new JFileChooser();
    fc.addChoosableFileFilter(new EresseaFileFilter(FILE_EXTENSION, FILE_EXTENSION_DECRIPTION));
    fc.setSelectedFile(new File(settings.getProperty(PROPERTYNAME_LAST_SAVED, "")));
    fc.setDialogTitle(Resources.get("alchemydialog.loaddialog.title"));

    boolean error = false;
    if (fc.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
      try {
        settings.setProperty(PROPERTYNAME_LAST_SAVED, fc.getSelectedFile().getAbsolutePath());

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
    fc.addChoosableFileFilter(new EresseaFileFilter(FILE_EXTENSION, FILE_EXTENSION_DECRIPTION));
    fc.setSelectedFile(new File(settings.getProperty(PROPERTYNAME_LAST_SAVED, "")));
    fc.setDialogTitle(Resources.get("alchemydialog.savedialog.title"));

    if (fc.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
      XMLStreamWriter writer = null;
      FileOutputStream os = null;
      boolean error = false;
      try {
        settings.setProperty(PROPERTYNAME_LAST_SAVED, fc.getSelectedFile().getAbsolutePath());

        if (fc.getSelectedFile().exists() && fc.getSelectedFile().canWrite()) {
          // create backup file
          try {
            File backup = FileBackup.create(fc.getSelectedFile());
            log.info("Created backupfile " + backup);
          } catch (IOException ie) {
            log.warn("Could not create backupfile for file " + fc.getSelectedFile());
          }
        }
        if (fc.getSelectedFile().exists() && !fc.getSelectedFile().canWrite()) {
          throw new IOException("cannot write " + fc.getSelectedFile());
        } else {

          XMLOutputFactory output = XMLOutputFactory.newInstance();
          writer =
              output.createXMLStreamWriter(os = new FileOutputStream(fc.getSelectedFile()),
                  getData().getEncoding());
          writer.writeStartDocument(getData().getEncoding(), "1.0");
          writer.writeStartElement("alchemydialog");
          writer.writeAttribute("version", "0.1");
          writer.writeCharacters("\n");
// writer.writeNamespace("date", "");
          for (Faction f : getFactions()) {
            writer.writeCharacters("\n");
            writer.writeEmptyElement("faction");
            writer.writeAttribute("id", String.valueOf(((EntityID) f.getID()).intValue()));
          }
          for (Region region : regions) {
            writer.writeCharacters("\n");
            writer.writeEmptyElement("region");
            writer.writeAttribute("coordinate", region.getCoordinate().toString());
          }
          for (HerbInfo info : model.herbs) {
            writer.writeCharacters("\n");
            writer.writeEmptyElement("herb");
            writer.writeAttribute("id", info.item.getID().toString());
            writer.writeAttribute("name", info.item.getName());
            writer.writeAttribute("amount", String.valueOf(info.number));
          }
          for (int pot = 0; pot < model.potions.size(); ++pot) {
            PotionInfo info = model.potions.get(pot);
            writer.writeCharacters("\n");
            writer.writeStartElement("potion");
            writer.writeAttribute("id", info.potion.getID().toString());
            writer.writeAttribute("name", info.potion.getName());
            writer.writeAttribute("planned", String.valueOf(info.production));
            for (ItemType ingredient : info.ingredients.keySet()) {
              writer.writeCharacters("\n");
              writer.writeEmptyElement("ingredient");
              writer.writeAttribute("id", ingredient.getID().toString());
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
        error = true;
      } catch (IOException e) {
        e.printStackTrace();
        error = true;
      } finally {
        try {
          if (os != null)
            os.close();
          if (writer != null)
            writer.close();
        } catch (XMLStreamException e) {
          e.printStackTrace();
          error = true;
        } catch (IOException e) {
          e.printStackTrace();
          error = true;
        }
        if (error) {
          JOptionPane.showMessageDialog(this, Resources.get("alchemydialog.savingerror.message"));
        }
      }
    }
  }

  /**
   * Returns the currently selected factions.
   */
  private List<Faction> getFactions() {
    if (factions == null) {
      Faction f = null;
      if (data.getOwnerFaction() != null) {
        f = data.getFaction(data.getOwnerFaction());
      } else if (data.factions().size() > 0) {
        f = data.factions().values().iterator().next();
      }
      if (f == null)
        return Collections.emptyList();
      else
        return Collections.singletonList(f);
    } else
      return factions;
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
   * A model for the potion planning table.
   */
  public static class PlannerModel extends AbstractTableModel {

    public static class HerbInfo {

      /**
       * Initializes the herb infos.
       * 
       * @param herb The ingredient. Usually, but not necessarily an ItemType from data.rules.
       */
      public HerbInfo(ItemType herb) {
        this.item = herb;
        this.number = 0;
      }

      ItemType item;
      int number;
    }

    public static class PotionInfo {
      public HashMap<ItemType, Integer> ingredients;
      public Potion potion;
      public int production;

      /**
       * Initializes the potions and their ingredients.
       * 
       * @param potion The potion. Usually, but not necessarily a potion from data.
       */
      public PotionInfo(Potion potion) {
        this.potion = potion;
        this.ingredients = new HashMap<ItemType, Integer>();
        this.production = 0;

        for (Item ingredient : potion.ingredients()) {
          this.ingredients.put(ingredient.getItemType(), ingredient.getAmount());
        }
      }

      @Override
      public String toString() {
        return potion.toString();
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
     * @param regions
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
     * 
     * @return
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
      for (Potion potion : data.potions().values()) {
        PotionInfo info = new PotionInfo(potion);
        potions.add(info);
        for (ItemType ingredient : info.ingredients.keySet()) {
          if (!herbMap.containsKey(ingredient)) {
            HerbInfo herbInfo = new HerbInfo(ingredient);
            herbs.add(herbInfo);
            herbMap.put(ingredient, herbInfo);
          }
        }
      }

      Collections.sort(herbs, new Comparator<HerbInfo>() {
        public int compare(HerbInfo o1, HerbInfo o2) {
          return o1.item.getName().compareTo(o2.item.getName());
        }
      });

      Collections.sort(potions, new Comparator<PotionInfo>() {
        public int compare(PotionInfo o1, PotionInfo o2) {
          return o1.potion.getName().compareTo(o2.potion.getName());
        }
      });

      return potions;
    }

    /**
     * Change the set of potions.
     * 
     * @param newPotions
     */
    public void setPotions(ArrayList<PotionInfo> newPotions) {
      potions = new ArrayList<PotionInfo>(newPotions);
      fireTableStructureChanged();
    }

    /**
     * Change the set of herbs.
     */
    public void setHerbs(ArrayList<HerbInfo> newHerbs) {
      herbs = new ArrayList<HerbInfo>(newHerbs);
      herbMap = new HashMap<ItemType, HerbInfo>();
      for (HerbInfo info : newHerbs)
        herbMap.put(info.item, info);
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
      for (int i = 0; i < potions.size(); ++i)
        potions.get(i).production = 0;
    }

    /**
     * Update stocks with items from units of specified factions in selected regions.
     * 
     * @param factions Units are taken from these factions.
     * @param regions Only units from these regions will be considered.
     */
    public void updateFromFactions(List<Faction> factions, List<Region> regions) {
      for (HerbInfo herb : herbs)
        herb.number = 0;
      for (Region region : regions) {
        for (Unit u : region.units()) {
          if (factions.contains(u.getFaction())) {
            for (Item item : u.getItems()) {
              if (herbMap.containsKey(item.getItemType()))
                herbMap.get(item.getItemType()).number += item.getAmount();
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
      for (HerbInfo herb : herbs)
        herb.number = 0;

      for (Region region : regions) {
        HerbInfo info = getInfo(region.getHerb());
        if (info != null)
          info.number += 10;
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
      return potions.size() + fixedCols;
    }

    /**
     * @see javax.swing.table.TableModel#getRowCount()
     */
    public int getRowCount() {
      return herbs.size() + fixedRows;
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
        return potions.get(column - fixedCols).potion.getName();
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
        return nameType;
      case restCol:
        return restType;
      case stockCol:
        return stockType;
      default:
        return ingredientType;
      }
    }

    /**
     * Returns the cell type. One of {@link #nameType}, {@link #noType}, {@link #restType},
     * {@link #noType}, {@link #stockType}, {@link #maxType}, {@link #currentMaxType},
     * {@link #productionType}, {@link #ingredientType}.
     */
    private int getType(int rowIndex, int columnIndex) {
      switch (columnIndex) {
      case nameCol:
        return nameType;
      case restCol:
        if (rowIndex < fixedRows)
          return noType;
        else
          return restType;
      case stockCol:
        if (rowIndex < fixedRows)
          return noType;
        else
          return stockType;
      default:
        switch (rowIndex) {
        case maxRow:
          return maxType;
        case currentMaxRow:
          return currentMaxType;
        case productionRow:
          return productionType;
        default:
          return ingredientType;
        }
      }
    }

    /**
     * A class for helping to identify cell types by
     * {@link TableCellRenderer#getTableCellRendererComponent(JTable, Object, boolean, boolean, int, int)}
     */
    public static class ValueMarker {
      private Integer value;

      public ValueMarker(Integer value) {
        this.value = value;
      }

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

    public static class StockValue extends ValueMarker {
      public StockValue(Integer value) {
        super(value);
      }

      public StockValue(String value) {
        super(value);
      }
    }

    public static class RestValue extends ValueMarker {
      public RestValue(Integer value) {
        super(value);
      }

      public RestValue(String value) {
        super(value);
      }
    }

    public static class PotionValue extends ValueMarker {
      public PotionValue(Integer value) {
        super(value);
      }

      public PotionValue(String value) {
        super(value);
      }
    }

    public static class MaxValue extends PotionValue {
      public MaxValue(Integer value) {
        super(value);
      }

      public MaxValue(String value) {
        super(value);
      }
    }

    public static class CurrentMaxValue extends PotionValue {
      public CurrentMaxValue(Integer value) {
        super(value);
      }

      public CurrentMaxValue(String value) {
        super(value);
      }
    }

    public static class ProductionValue extends PotionValue {
      public ProductionValue(Integer value) {
        super(value);
      }

      public ProductionValue(String value) {
        super(value);
      }
    }

    public static class IngredientValue extends PotionValue {
      public IngredientValue(Integer value) {
        super(value);
      }

      public IngredientValue(String value) {
        super(value);
      }
    }

    /**
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
          return getHerb(rowIndex).item.getName();
        }
      case stockType:
        return new StockValue(getHerb(rowIndex).number);
      case restType:
        return new RestValue(getRest(getHerb(rowIndex)));
      case ingredientType:
        return new IngredientValue(getPotion(columnIndex).ingredients.get(getHerb(rowIndex).item));
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
     * Returns the herb corresponding to the specified table row.
     */
    private HerbInfo getHerb(int rowIndex) {
      return herbs.get(rowIndex - fixedRows);
    }

    /**
     * Returns the potion corresponding to the specified table column.
     */
    private PotionInfo getPotion(int columnIndex) {
      return potions.get(columnIndex - fixedCols);
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
     * Return the amount of the ingredient remaining ofter all projected amounts of potions have
     * been produced.
     */
    private int getRest(HerbInfo herb) {
      int rest = herb.number;
      for (int pot = 0; pot < potions.size(); ++pot) {
        PotionInfo potion = potions.get(pot);
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
        fireTableCellUpdated(maxRow, pot + fixedCols);
        fireTableCellUpdated(currentMaxRow, pot + fixedCols);
      }
      for (int herb = 0; herb < herbs.size(); ++herb) {
        fireTableCellUpdated(herb + fixedRows, restCol);
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
    private Map<ID, ItemType> addedTypes;
    private ArrayList<Faction> newFactions;

    @Override
    public void startDocument() throws SAXException {
      newRegions = new ArrayList<Region>();
      newHerbs = new ArrayList<HerbInfo>();
      newPotions = new ArrayList<PotionInfo>();
      newFactions = new ArrayList<Faction>();
      addedTypes = new HashMap<ID, ItemType>();
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
          log.warn("unknown faction in alchemy file: "+ getEntityID(attributes).toString());
        } else {
          newFactions.add(faction);
        }
      } else if (qName.equals("region")) {
        Region region =
            getData().getRegion(CoordinateID.parse(attributes.getValue("coordinate"), ","));
        if (region == null)
          log.warn("unknown region in alchemy file: " + attributes.getValue("coordinate"));
        else
          newRegions.add(region);
      } else if (qName.equals("herb")) {
        ItemType herb = getData().rules.getItemType(getStringID(attributes));
        if (herb == null) {
          herb = new ItemType(getStringID(attributes));
          herb.setName(attributes.getValue("name"));
          addedTypes.put(getStringID(attributes), herb);
        }
        HerbInfo info;
        newHerbs.add(info = new HerbInfo(herb));
        info.number = Integer.parseInt(attributes.getValue("amount"));
      } else if (qName.equals("potion")) {
        Potion potion = getData().getPotion(getEntityID(attributes));
        if (potion == null) {
          getData().addPotion(potion = new MagellanPotionImpl(getEntityID(attributes)));
          potion.setName(attributes.getValue("name"));
        }
        PotionInfo info = new PotionInfo(potion);
        newPotions.add(info);
        info.production = Integer.parseInt(attributes.getValue("planned"));
        currentPotion = info;
      } else if (qName.equals("ingredient")) {
        currentPotion.ingredients.put(getType(getStringID(attributes)), Integer.parseInt(attributes
            .getValue("amount")));
      }
    }

    private StringID getStringID(Attributes attributes) {
      return StringID.create(attributes.getValue("id"));
    }

    private EntityID getEntityID(Attributes attributes) {
      return EntityID.createEntityID(attributes.getValue("id"), 10, getData().base);
    }

    private ItemType getType(StringID id) {
      ItemType type = getData().rules.getItemType(id);
      if (type == null)
        type = addedTypes.get(id);

      if (type == null)
        throw new RuntimeException("unknown ingredient");

      return type;
    }

    @Override
    public void endElement(String uri, String localName, String qName) throws SAXException {
      if (qName.equals("potion")) {
        currentPotion = null;
      }
    }

    @Override
    public void endDocument() throws SAXException {
      setRegions(newRegions);
      setFactions(newFactions);
      model.setHerbs(newHerbs);
      model.setPotions(newPotions);
    }
  }

  /**
   * Dialog for selecting a faction from all factions in the report.
   */
  public class FactionDialog extends JDialog {
    public FactionDialog() {
      JPanel mainPanel = new JPanel(new SpringLayout());
      DefaultListModel model = new DefaultListModel();
      for (Faction f : getData().factions().values()) {
        model.addElement(f);
      }
      final JList factionList = new JList(model);
      factionList.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
      factionList.clearSelection();
      List<Faction> factions = getFactions();
      for (int row = 0; row < model.size(); ++row) {
        if (factions.contains(model.get(row)))
          factionList.addSelectionInterval(row, row);
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
      this.pack();
    }
  }

}
