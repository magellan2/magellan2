/*
 * Copyright (C) 2000-2004 Roger Butenuth, Andreas Gampe, Stefan Goetz, Sebastian Pappert, Klaas
 * Prause, Enno Rehling, Sebastian Tusk, Ulrich Kuester, Ilja Pavkovic This file is part of the
 * Eressea Java Code Base, see the file LICENSING for the licensing information applying to this
 * file.
 */

package magellan.client.swing;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Vector;

import javax.swing.DefaultListModel;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSlider;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.border.TitledBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableModel;

import magellan.client.event.EventDispatcher;
import magellan.client.event.SelectionEvent;
import magellan.client.event.SelectionListener;
import magellan.client.utils.SwingUtils;
import magellan.client.utils.SwingUtils.RenderHelper;
import magellan.library.Faction;
import magellan.library.GameData;
import magellan.library.Item;
import magellan.library.LuxuryPrice;
import magellan.library.Region;
import magellan.library.StringID;
import magellan.library.Unit;
import magellan.library.event.GameDataEvent;
import magellan.library.gamebinding.EresseaConstants;
import magellan.library.rules.ItemType;
import magellan.library.utils.CollectionFactory;
import magellan.library.utils.PropertiesHelper;
import magellan.library.utils.Resources;
import magellan.library.utils.comparator.FactionTrustComparator;
import magellan.library.utils.comparator.NameComparator;
import magellan.library.utils.filters.CollectionFilters;

/**
 * Dialog for planning trade.
 * 
 * @author Ulrich Küster, stm
 */
public class TradeOrganizer extends InternationalizedDataDialog implements SelectionListener {
  public static final String IDENTIFIER = "TRADES";
  protected List<Region> regions = new LinkedList<Region>();
  protected int minSellMultiplier = 1;
  protected SellTable sell;
  protected BuyTable buy;
  protected StocksTable stocks;
  protected JComboBox luxuries;
  protected JSlider minSellMultiplierSlider;
  protected JLabel totalSellingVolume;
  protected JLabel totalBuyingVolume;
  protected JLabel averagePrice;
  protected JList factionList;

  // Fiete: Keys: German Values:locale (en)
  protected Map<String, String> luxuryTranslations = null;

  /**
   * Creates a new TradeOrganizer object.
   */
  public TradeOrganizer(Frame owner, EventDispatcher dispatcher, GameData data, Properties settings) {
    this(owner, dispatcher, data, settings, null);
  }

  /**
   * Creates a new TradeOrganizer object.
   */
  public TradeOrganizer(Frame owner, EventDispatcher dispatcher, GameData data,
      Properties settings, Collection<Region> newRegions) {
    super(owner, false, dispatcher, data, settings);

    // register for events
    // unnecessary
    // dispatcher.addGameDataListener(this);
    dispatcher.addSelectionListener(this);

    init();
    setRegions((newRegions == null) ? Collections.<Region> emptySet() : newRegions);
  }

  @Override
  public void setVisible(boolean b) {
    super.setVisible(b);
    if (!b) {
      dispatcher.removeGameDataListener(this);
      dispatcher.removeSelectionListener(this);
    }

  }

  protected void init() {
    Dimension dim;
    if (settings.getProperty("TradeOrganizer.width") != null) {
      dim = new Dimension(
          PropertiesHelper.getInteger(settings, "TradeOrganizer.width", 800),
          PropertiesHelper.getInteger(settings, "TradeOrganizer.height", 600));
    } else {
      dim = SwingUtils.getDimension(50, -1, true);
    }

    setSize(dim);

    SwingUtils.setLocation(this, settings, "TradeOrganizer.xPos", "TradeOrganizer.yPos");

    setTitle(Resources.get("tradeorganizer.title"));

    minSellMultiplier =
        Integer.parseInt(settings.getProperty("TradeOrganizer.minSellMultiplier", "1"));

    // build GUI
    JTabbedPane tabPane = new JTabbedPane();

    // build top panel
    JPanel topPanel = new JPanel();
    topPanel.setBorder(new TitledBorder(""));
    topPanel.setLayout(new GridBagLayout());

    GridBagConstraints c =
        new GridBagConstraints(0, 0, 1, 1, 0, 0, GridBagConstraints.WEST, GridBagConstraints.NONE,
            new Insets(5, 5, 5, 5), 0, 0);

    topPanel.add(new JLabel(Resources.get("tradeorganizer.minsellmultiplier")), c);
    c.gridx++;
    minSellMultiplierSlider = new JSlider(1, 31, minSellMultiplier);
    minSellMultiplierSlider.setMinorTickSpacing(1);
    minSellMultiplierSlider.setMajorTickSpacing(1);

    Hashtable<?, ?> sliderLabels = minSellMultiplierSlider.createStandardLabels(5);
    minSellMultiplierSlider.setLabelTable(sliderLabels);
    minSellMultiplierSlider.setSnapToTicks(true);
    minSellMultiplierSlider.setPaintTicks(true);
    minSellMultiplierSlider.setPaintLabels(true);
    dim = SwingUtils.getDimension(20, 3, false);
    dim.height = minSellMultiplierSlider.getPreferredSize().height;
    minSellMultiplierSlider.setMinimumSize(dim);
    dim = SwingUtils.getDimension(30, 3, false);
    dim.height = minSellMultiplierSlider.getPreferredSize().height;
    minSellMultiplierSlider.setPreferredSize(dim);

    minSellMultiplierSlider.addChangeListener(new ChangeListener() {
      public void stateChanged(ChangeEvent ce) {
        minSellMultiplier = Math.max(minSellMultiplierSlider.getValue(), 1);
        setSellTableRegions();
      }
    });
    c.weightx = .1;
    topPanel.add(minSellMultiplierSlider, c);
    c.weightx = 0;
    c.gridx++;

    topPanel.add(new JLabel(Resources.get("tradeorganizer.luxury")), c);
    c.gridx++;
    c.weightx = 1.0;

    luxuries = new JComboBox();
    buildLuxuryTranslations();
    luxuries.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        setSellTableRegions();
        setBuyTableRegions();
        setStocksTableRegions();
      }
    });
    topPanel.add(luxuries, c);

    JPanel help = new JPanel();
    help.setLayout(new GridBagLayout());
    c.insets = new Insets(0, 0, 0, 0);
    c.gridx = 0;
    c.weightx = 0;
    totalBuyingVolume = new JLabel();
    help.add(totalBuyingVolume, c);
    c.gridx++;
    totalSellingVolume = new JLabel();
    help.add(totalSellingVolume, c);
    c.gridx++;
    c.weightx = 1;
    averagePrice = new JLabel();
    help.add(averagePrice, c);

    c.gridx = 0;
    c.gridy = 1;
    c.insets = new Insets(5, 5, 5, 5);
    c.gridwidth = 4;
    topPanel.add(help, c);

    Container cp = getContentPane();
    cp.setLayout(new BorderLayout());
    cp.add(topPanel, BorderLayout.NORTH);
    cp.add(tabPane, BorderLayout.CENTER);

    // build Tables in TabbedPane
    cp = new JPanel();
    cp.setLayout(new BorderLayout());
    tabPane.addTab(Resources.get("tradeorganizer.buy"), cp);
    RenderHelper rhb = SwingUtils.prepareTable();
    buy = new BuyTable() {
      @Override
      public Component prepareRenderer(TableCellRenderer renderer, int row, int column) {
        return rhb.wrapPrepareHandlerRowHeightAdjusted(this, row,
            super.prepareRenderer(renderer, row, column));
      }
    };
    rhb.prepareTable(buy);
    cp.add(new JScrollPane(buy), BorderLayout.CENTER);

    cp = new JPanel();
    cp.setLayout(new BorderLayout());
    tabPane.addTab(Resources.get("tradeorganizer.sell"), cp);
    RenderHelper rhs = SwingUtils.prepareTable();
    sell = new SellTable() {
      @Override
      public Component prepareRenderer(TableCellRenderer renderer, int row, int column) {
        return rhs.wrapPrepareHandlerRowHeightAdjusted(this, row,
            super.prepareRenderer(renderer, row, column));
      }
    };
    rhs.prepareTable(buy);
    cp.add(new JScrollPane(sell), BorderLayout.CENTER);

    tabPane.add(Resources.get("tradeorganizer.stocks"), getStocksPanel());
  }

  private JPanel getStocksPanel() {
    JPanel panel = new JPanel();

    factionList = new JList();
    updateFactions();
    factionList.addListSelectionListener(new ListSelectionListener() {
      public void valueChanged(ListSelectionEvent e) {
        Object[] factionsArray = factionList.getSelectedValues();
        List<Faction> factions = new ArrayList<Faction>();
        for (Object object : factionsArray) {
          factions.add((Faction) object);
        }
        stocks.setFactions(factions);
      }
    });

    JScrollPane factionsScrollPane = new JScrollPane(factionList);
    RenderHelper rh = SwingUtils.prepareTable();
    stocks = new StocksTable() {
      @Override
      public Component prepareRenderer(TableCellRenderer renderer, int row, int column) {
        return rh.wrapPrepareHandlerRowHeightAdjusted(this, row,
            super.prepareRenderer(renderer, row, column));
      }
    };
    rh.prepareTable(stocks);

    JScrollPane stocksTableScrollPane = new JScrollPane(stocks);

    JSplitPane split =
        new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, factionsScrollPane, stocksTableScrollPane);
    split.setDividerLocation(0.4);

    panel.setLayout(new BorderLayout());
    panel.add(split, BorderLayout.CENTER);

    return panel;
  }

  /**
   * Called to update the faction list in the stocks panel
   */
  private void updateFactions() {
    Vector<Faction> factionVector = new Vector<Faction>(data.getFactions());
    Collections.sort(factionVector, FactionTrustComparator.DEFAULT_COMPARATOR);

    DefaultListModel model = new DefaultListModel();

    for (Iterator<Faction> iter = factionVector.listIterator(); iter.hasNext();) {
      model.addElement(iter.next());
    }

    factionList.setModel(model);
    factionList.setSelectedIndex(0);
  }

  /**
   * @see magellan.client.swing.InternationalizedDataDialog#gameDataChanged(magellan.library.event.GameDataEvent)
   */
  @Override
  public void gameDataChanged(GameDataEvent ge) {
    super.gameDataChanged(ge);

    buildLuxuryTranslations();

    setRegions(Collections.<Region> emptySet());

    updateFactions();
  }

  /**
   * Adapt for new regions.
   * 
   * @see magellan.client.event.SelectionListener#selectionChanged(magellan.client.event.SelectionEvent)
   */
  public void selectionChanged(SelectionEvent se) {
    if ((se.getSelectionType() == SelectionEvent.ST_REGIONS) && (se.getSelectedObjects() != null)) {
      setRegions(CollectionFilters.uncheckedCast(se.getSelectedObjects(), Region.class));
    }
  }

  /**
   * Sets this classes region list. Filters out all regions, which trade information is unknown.
   */
  private void setRegions(Collection<? extends Region> newRegions) {
    if (newRegions.isEmpty() && (data.getRegions() != null)) {
      newRegions = data.getRegions();
    }

    regions = new LinkedList<Region>();

    for (Region region : newRegions) {
      if ((region.getPrices() != null) && (region.getPrices().size() > 0)) {
        regions.add(region);
      }
    }

    setSellTableRegions();
    setBuyTableRegions();
    setStocksTableRegions();
  }

  /**
   * Resets the regions, that the buytable shows. Also resets the information about the total buying
   * volume. Assumes, that the prices of all regions contained in this class's region-list are
   * known.
   */
  private void setBuyTableRegions() {
    String curLux = (String) luxuries.getSelectedItem();

    if (curLux == null)
      return;

    curLux = getOriginalLuxuryTranslation(curLux);

    LinkedList<Region> newRegions = new LinkedList<Region>();
    int total = 0;

    for (Region region : regions) {
      LuxuryPrice price = region.getPrices().get(StringID.create(curLux));

      if (price != null && price.getPrice() < 0) {
        newRegions.add(region);
        total += region.maxLuxuries();
      }
    }

    totalBuyingVolume.setText(Resources.get("tradeorganizer.totalBuyingVolume") + total + ", ");
    buy.setRegions(newRegions);
    buy.sort();
  }

  /**
   * Resets the regions, that the sell table shows. Also resets the information about the total
   * selling volume and the average price. Assumes, that the prices of all regions contained in this
   * class's region-list are known.
   */
  private void setSellTableRegions() {
    String curLux = (String) luxuries.getSelectedItem();

    if (curLux == null)
      return;

    curLux = getOriginalLuxuryTranslation(curLux);

    LinkedList<Region> newRegions = new LinkedList<Region>();
    int total = 0;
    int totalPrice = 0;

    for (Region region : regions) {
      LuxuryPrice price = region.getPrices().get(StringID.create(curLux));

      if (price != null && checkPrice(price)) {
        newRegions.add(region);

        int volume = region.maxLuxuries();
        total += volume;
        totalPrice += (volume * price.getPrice());
      }
    }

    sell.setRegions(newRegions);
    totalSellingVolume.setText(Resources.get("tradeorganizer.totalSellingVolume") + total + ", ");

    if (total != 0) {
      averagePrice.setText(Resources.get("tradeorganizer.averagePrice") + (totalPrice / total));
    } else {
      averagePrice.setText(Resources.get("tradeorganizer.averagePrice") + 0);
    }

    sell.sort();
  }

  /**
   * Returns false iff price &lt; 0 or price/itemtype.getResources("Silber") &lt; minSellMultiplier
   */
  private boolean checkPrice(LuxuryPrice price) {
    if (price.getPrice() > 0) {
      Item luxuryPrice = price.getItemType().getResource(EresseaConstants.I_USILVER);

      if ((luxuryPrice != null) && (luxuryPrice.getAmount() > 0)) {
        if ((price.getPrice() / luxuryPrice.getAmount()) < minSellMultiplier)
          return false;
      }

      return true;
    }

    return false;
  }

  /**
   * Just for convenience. Sets the regions included in the stocks table
   */
  private void setStocksTableRegions() {
    stocks.setRegions(regions);
  }

  /**
   * Store settings and quit.
   * 
   * @see magellan.client.swing.InternationalizedDataDialog#quit()
   */
  @Override
  protected void quit() {
    // store settings
    settings.setProperty("TradeOrganizer.width", String.valueOf(getWidth()));
    settings.setProperty("TradeOrganizer.height", String.valueOf(getHeight()));
    settings.setProperty("TradeOrganizer.xPos", String.valueOf(getLocation().x));
    settings.setProperty("TradeOrganizer.yPos", String.valueOf(getLocation().y));
    settings.setProperty("TradeOrganizer.minSellMultiplier", String.valueOf(minSellMultiplier));

    super.quit();
  }

  public class SortableRegionTable extends JTable {

    public SortableRegionTable(final SortableRegionTableModel sortableModel) {
      super(sortableModel);

      getTableHeader().setReorderingAllowed(false);
      setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
      getSelectionModel().addListSelectionListener(new ListSelectionListener() {
        public void valueChanged(ListSelectionEvent e) {
          int i = getSelectedRow();
          if (i >= 0) {
            Region r = sortableModel.getRegion(i);

            if (r != null) {
              dispatcher.fire(SelectionEvent.create(this, r));
            }
          }
        }
      });

      // sorting
      getTableHeader().addMouseListener(new MouseAdapter() {
        @Override
        public void mouseClicked(MouseEvent e) {
          int i = getTableHeader().getColumnModel().getColumnIndexAtX(e.getPoint().x);
          getModel().sort(i);
        }
      });
    }

    @Override
    public SortableRegionTableModel getModel() {
      return (SortableRegionTableModel) super.getModel();
      // return model;
    }

    /**
     * Returns the current set of regions.
     */
    public Collection<Region> getRegions() {
      return getModel().getRegions();
    }

    /**
     * Changes the current set of regions.
     */
    public void setRegions(Collection<Region> regions) {
      getModel().setRegions(regions);
      revalidate();
      validate();
      repaint();
    }

    /**
     * Do the sorting according to current sort mode.
     */
    public void sort() {
      getModel().sort(getModel().curSort);
    }

  }

  /**
   * A table model where rows correspond to regions and sortable by columns.
   */
  public abstract class SortableRegionTableModel extends AbstractTableModel {

    public class ColumnInfo {
      ColumnInfo(String title, Class<?> type, Comparator<? super Region> comparator) {
        this.title = title;
        this.type = type;
        this.comparator = comparator;
      }

      Class<?> type;
      String title;
      Comparator<? super Region> comparator;
    }

    protected int curSort = 1;
    private LinkedList<Region> tableRegions;

    private List<ColumnInfo> colInfos;

    public SortableRegionTableModel() {
      tableRegions = new LinkedList<Region>();
    }

    public void configure(ColumnInfo... columns) {
      colInfos = new ArrayList<ColumnInfo>(columns.length);
      for (ColumnInfo info : columns) {
        colInfos.add(info);
      }
    }

    /**
     * @see javax.swing.table.TableModel#getRowCount()
     */
    public int getRowCount() {
      return tableRegions.size();
    }

    /**
     * @see javax.swing.table.TableModel#getColumnCount()
     */
    public int getColumnCount() {
      return colInfos.size();
    }

    /**
     * @see javax.swing.table.AbstractTableModel#getColumnClass(int)
     */
    @Override
    public Class<? extends Object> getColumnClass(int col) {
      return colInfos.get(col).type;
    }

    @Override
    public String getColumnName(int column) {
      return colInfos.get(column).title;
    }

    /**
     * Returns the current set of regions.
     */
    public Collection<Region> getRegions() {
      return tableRegions;
    }

    /**
     * Changes the current set of regions.
     */
    public void setRegions(Collection<Region> regs) {
      tableRegions.clear();
      tableRegions.addAll(regs);
      sort(curSort);
      fireTableDataChanged();
    }

    /**
     * Returns the region of the indicated row.
     * 
     * @param row The table row
     */
    public Region getRegion(int row) {
      return tableRegions.get(row);
    }

    /**
     * 
     */
    public void sort(int i) {
      if (i < 0 || i >= getColumnCount())
        throw new RuntimeException("invalid column " + i);
      curSort = i;

      Collections.sort(tableRegions, getComparator(i));
    }

    protected Comparator<? super Region> getComparator(int col) {
      return colInfos.get(col).comparator;
    }

  }

  /**
   * A JTable adaption for luxury good selling dialog.
   */
  public class SellTable extends SortableRegionTable {

    /**
     * Creates a new SellTable object.
     */
    public SellTable() {
      super(new SellTableModel());
    }

  }

  /**
   * A model for {@link SellTable}.
   */
  private class SellTableModel extends SortableRegionTableModel {

    public SellTableModel() {
      super();
      super.configure(new ColumnInfo[] {
          this.new ColumnInfo(Resources.get("tradeorganizer.buycolumnname1"), String.class,
              new NameComparator(null)),
          this.new ColumnInfo(Resources.get("tradeorganizer.buycolumnname2"), String.class,
              new RegionPriceComparator()),
          this.new ColumnInfo(Resources.get("tradeorganizer.buycolumnname3"), Integer.class,
              new RegionTradeVolumeComparator()) });
    }

    /**
     * @see javax.swing.table.TableModel#getValueAt(int, int)
     */
    public Object getValueAt(int row, int col) {
      Region region = getRegion(row);

      switch (col) {
      case 0:
        return region.toString();

      case 1: {
        String o = (String) luxuries.getSelectedItem();

        if ((o == null) && (luxuries.getItemCount() > 0)) {
          luxuries.setSelectedIndex(0);
        }

        o = getOriginalLuxuryTranslation(o);

        LuxuryPrice price = region.getPrices().get(StringID.create(o));

        if (price == null)
          return "-?-";
        else
          return price.getPrice();
      }

      case 2:
        return region.getPeasants() / 100;
      }

      return null;
    }

    /**
     * Sorts the model by the specified column (0=name, 1=price, 2=volume).
     * 
     * @param i
     */
    @Override
    public void sort(int i) {
      super.sort(i);

      sell.revalidate();
      sell.repaint();
    }
  }

  /**
   * A JTable adaption for the buying panel.
   */
  public class BuyTable extends SortableRegionTable {
    /**
     * Creates a new BuyTable object.
     */
    public BuyTable() {
      super(new BuyTableModel());
    }

  }

  /**
   * A model for {@link BuyTable}.
   */
  private class BuyTableModel extends SortableRegionTableModel {

    public BuyTableModel() {
      super();
      super.configure(new ColumnInfo[] {
          new ColumnInfo(Resources.get("tradeorganizer.sellcolumnname1"), String.class,
              new NameComparator(null)),
          new ColumnInfo(Resources.get("tradeorganizer.sellcolumnname2"), Integer.class,
              new RegionTradeVolumeComparator()) });
    }

    /**
     * @see javax.swing.table.TableModel#getValueAt(int, int)
     */
    public Object getValueAt(int row, int col) {
      Region region = getRegion(row);

      switch (col) {
      case 0:
        return region.toString();

      case 1:
        return region.getPeasants() / 100;
      }

      return null;
    }

    /**
     * Sorts the model according to the specified column (0=name, 1=volume).
     */
    @Override
    public void sort(int i) {
      super.sort(i);

      buy.revalidate();
      buy.repaint();
    }
  }

  /**
   * A JTable adaption for the stocks panel.
   */
  private class StocksTable extends SortableRegionTable {

    /**
     * Creates a new StocksTable object.
     */
    public StocksTable() {
      super(new StocksTableModel());
    }

    /**
     * Changes the set of managed factions.
     */
    public void setFactions(Collection<Faction> factions) {
      getModel().setFactions(factions);
      revalidate();
      repaint();
    }

    @Override
    public void setModel(TableModel dataModel) {
      super.setModel(dataModel);
    }

    /**
     * @see magellan.client.swing.TradeOrganizer.SortableRegionTable#getModel()
     */
    @Override
    public StocksTableModel getModel() {
      if (super.getModel() instanceof StocksTableModel)
        return (StocksTableModel) super.getModel();
      else
        return new StocksTableModel();
    }
  }

  private class StocksTableModel extends SortableRegionTableModel {
    private LinkedList<Faction> tableFactions = new LinkedList<Faction>();

    // maps Region to Integer (available luxury in stock of selected factions)
    @SuppressWarnings("hiding")
    private Hashtable<Region, Integer> stocks = new Hashtable<Region, Integer>();

    private Collection<Region> allRegions;

    /**
     * @see javax.swing.table.TableModel#getValueAt(int, int)
     */
    public Object getValueAt(int row, int col) {
      Region region = getRegion(row);

      switch (col) {
      case 0:
        return region.toString();

      case 1:
        return stocks.get(region);
      }

      return null;
    }

    public StocksTableModel() {
      super();
      super.configure(new ColumnInfo[] {
          new ColumnInfo(Resources.get("tradeorganizer.sellcolumnname1"), String.class,
              new NameComparator(null)),
          new ColumnInfo(Resources.get("tradeorganizer.sellcolumnname2"), String.class,
              new RegionStockVolumeComparator(stocks)) });
    }

    /**
     * @see magellan.client.swing.TradeOrganizer.SortableRegionTableModel#setRegions(java.util.Collection)
     */
    @Override
    public void setRegions(Collection<Region> regs) {
      super.setRegions(regs);
      allRegions = new ArrayList<Region>(regs);
      updateStocksHashtable();
    }

    Collection<Region> getAllRegions() {
      return allRegions;
    }

    /**
     * Changes the set of factions to compute stocks from.
     */
    public void setFactions(Collection<Faction> factions) {
      tableFactions.clear();
      tableFactions.addAll(factions);
      updateStocksHashtable();
    }

    /**
     * Collects the information about how many units of the selected luxury good is in stock in each
     * region. If none is there, the region is removed out of the region pool.
     */
    private void updateStocksHashtable() {
      stocks.clear();

      if (luxuries.getItemCount() <= 0)
        return;

      if (luxuries.getSelectedIndex() == -1) {
        luxuries.setSelectedIndex(0);
      }

      StringID actSID =
          StringID.create(getOriginalLuxuryTranslation((String) luxuries.getSelectedItem()));
      ItemType luxury = data.getRules().getItemType(actSID);

      if (luxury == null)
        return;

      super.setRegions(getAllRegions());

      for (Iterator<Region> regionIter = getRegions().iterator(); regionIter.hasNext();) {
        Region r = regionIter.next();
        int amount = 0;

        for (Unit u : r.units()) {
          if ((u.getFaction() != null) && tableFactions.contains(u.getFaction())) {
            Item item = u.getItem(luxury);

            if (item != null) {
              amount += item.getAmount();
            }
          }
        }

        if (amount > 0) {
          stocks.put(r, Integer.valueOf(amount));
        } else {
          regionIter.remove();
        }
      }

      sort(curSort);
    }

    @Override
    public void sort(int i) {
      super.sort(i);
    }
  }

  /**
   * Compares two regions using their stock volume of the current luxury. The stock volume is
   * retrieved out of the given Hashtable.
   */
  private class RegionStockVolumeComparator implements Comparator<Region> {
    @SuppressWarnings("hiding")
    private Hashtable<Region, Integer> stocks;

    /**
     * Creates a new RegionStockVolumeComparator object.
     */
    public RegionStockVolumeComparator(Hashtable<Region, Integer> stocks) {
      this.stocks = stocks;
    }

    /**
     * Compares two regions by the value of their <tt>stocks</tt>.
     * 
     * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
     */
    public int compare(Region r1, Region r2) {
      Integer i1 = stocks.get(r1);
      Integer i2 = stocks.get(r2);

      if ((i1 == null) || (i2 == null))
        return 0;
      else
        return i2.intValue() - i1.intValue();
    }
  }

  /**
   * compares two regions using the current luxuries prices
   */
  private class RegionPriceComparator implements Comparator<Region> {
    /**
     * Compares two Regions by the price of the currently selected luxury good there.
     * 
     * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
     */
    public int compare(Region r1, Region r2) {
      LuxuryPrice p1 =
          r1.getPrices().get(
              StringID.create(getOriginalLuxuryTranslation((String) luxuries.getSelectedItem())));
      LuxuryPrice p2 =
          r2.getPrices().get(
              StringID.create(getOriginalLuxuryTranslation((String) luxuries.getSelectedItem())));

      if ((p1 == null) || (p2 == null))
        return 0;
      else
        return p2.getPrice() - p1.getPrice();
    }
  }

  /**
   * Compares two regions using their trade volume.
   */
  private class RegionTradeVolumeComparator implements Comparator<Region> {
    /**
     * Compares two regions by number of peasants.
     * 
     * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
     */
    public int compare(Region r1, Region r2) {
      return r2.getPeasants() - r1.getPeasants();
    }
  }

  private void buildLuxuryTranslations() {

    if (luxuryTranslations == null) {
      luxuryTranslations = CollectionFactory.<String, String> createSyncOrderedMap();
    } else {
      luxuryTranslations.clear();
    }

    if ((data != null) && (data.getRules() != null)) {
      for (ItemType currentType : data.getRules().getItemTypes()) {
        if (currentType.getCategory() != null
            && currentType.getCategory().equals(data.getRules().getItemCategory("luxuries"))) {
          luxuryTranslations.put(data.getTranslation(currentType), currentType.getID().toString());
        }
      }
    }

    luxuries.removeAllItems();
    for (String name : getLuxuryListTranslated()) {
      luxuries.addItem(name);
    }
    if (luxuries.getItemCount() <= 0) {
      luxuries.addItem("-?-");
    }

  }

  private String getOriginalLuxuryTranslation(String value) {
    if (luxuryTranslations.containsKey(value))
      return luxuryTranslations.get(value);
    return value;
  }

  /**
   * Returns the value of luxuryListTranslated.
   * 
   * @return Returns luxuryListTranslated.
   */
  public Collection<String> getLuxuryListTranslated() {
    return luxuryTranslations.keySet();
  }

}
