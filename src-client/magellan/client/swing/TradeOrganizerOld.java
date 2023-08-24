/*
 * Copyright (C) 2000-2004 Roger Butenuth, Andreas Gampe, Stefan Goetz, Sebastian Pappert, Klaas
 * Prause, Enno Rehling, Sebastian Tusk, Ulrich Kuester, Ilja Pavkovic This file is part of the
 * Eressea Java Code Base, see the file LICENSING for the licensing information applying to this
 * file.
 */

package magellan.client.swing;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Toolkit;
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

import magellan.client.event.EventDispatcher;
import magellan.client.event.SelectionEvent;
import magellan.client.event.SelectionListener;
import magellan.library.Faction;
import magellan.library.GameData;
import magellan.library.Item;
import magellan.library.LuxuryPrice;
import magellan.library.Region;
import magellan.library.StringID;
import magellan.library.Unit;
import magellan.library.event.GameDataEvent;
import magellan.library.rules.ItemType;
import magellan.library.utils.CollectionFactory;
import magellan.library.utils.Resources;
import magellan.library.utils.comparator.FactionTrustComparator;
import magellan.library.utils.comparator.NameComparator;
import magellan.library.utils.filters.CollectionFilters;

/**
 * DOCUMENT ME!
 * 
 * @author Ulrich Küster
 */
public class TradeOrganizerOld extends InternationalizedDataDialog implements SelectionListener {
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

  // protected LinkedList<String> luxuryListTranslated = null;

  /**
   * Creates a new TradeOrganizer object.
   */
  public TradeOrganizerOld(Frame owner, EventDispatcher dispatcher, GameData data,
      Properties settings) {
    this(owner, dispatcher, data, settings, null);
  }

  /**
   * Creates a new TradeOrganizer object.
   */
  public TradeOrganizerOld(Frame owner, EventDispatcher dispatcher, GameData data,
      Properties settings, Collection<Region> newRegions) {
    super(owner, false, dispatcher, data, settings);

    // register for events
    dispatcher.addGameDataListener(this);
    dispatcher.addSelectionListener(this);

    init();
    setRegions((newRegions == null) ? Collections.<Region> emptySet() : newRegions);
    setVisible(true);
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
    int width = Integer.parseInt(settings.getProperty("TradeOrganizer.width", "800"));
    int height = Integer.parseInt(settings.getProperty("TradeOrganizer.height", "600"));
    int xPos = Integer.parseInt(settings.getProperty("TradeOrganizer.xPos", "-1"));
    int yPos = Integer.parseInt(settings.getProperty("TradeOrganizer.yPos", "-1"));

    minSellMultiplier =
        Integer.parseInt(settings.getProperty("TradeOrganizer.minSellMultiplier", "1"));

    if (xPos == -1) {
      xPos = ((int) Toolkit.getDefaultToolkit().getScreenSize().getWidth() - width) / 2;
    }

    if (yPos == -1) {
      yPos = ((int) Toolkit.getDefaultToolkit().getScreenSize().getHeight() - height) / 2;
    }

    setSize(width, height);
    setLocation(xPos, yPos);
    setTitle(Resources.get("tradeorganizer.title"));

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
    minSellMultiplierSlider = new JSlider(1, 30, minSellMultiplier);
    minSellMultiplierSlider.setMinorTickSpacing(1);
    minSellMultiplierSlider.setMajorTickSpacing(1);

    Hashtable<?, ?> sliderLabels = minSellMultiplierSlider.createStandardLabels(5);
    minSellMultiplierSlider.setLabelTable(sliderLabels);
    minSellMultiplierSlider.setSnapToTicks(true);
    minSellMultiplierSlider.setPaintTicks(true);
    minSellMultiplierSlider.setPaintLabels(true);

    minSellMultiplierSlider.addChangeListener(new ChangeListener() {
      public void stateChanged(ChangeEvent ce) {
        minSellMultiplier = Math.max(minSellMultiplierSlider.getValue(), 1);
        setSellTableRegions();
      }
    });
    topPanel.add(minSellMultiplierSlider, c);
    c.gridx++;

    topPanel.add(new JLabel(Resources.get("tradeorganizer.luxury")), c);
    c.gridx++;
    c.weightx = 1.0;

    buildLuxuryTranslations();
    // this was in random order...Fiete 20061219
    // luxuries = new JComboBox(this.luxuryTranslations.values().toArray());
    // this is better:
    luxuries = new JComboBox(getLuxuryListTranslated().toArray());

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
    c.gridwidth = 2;
    topPanel.add(help, c);

    Container cp = getContentPane();
    cp.setLayout(new BorderLayout());
    cp.add(topPanel, BorderLayout.NORTH);
    cp.add(tabPane, BorderLayout.CENTER);

    // build Tables in TabbedPane
    cp = new JPanel();
    cp.setLayout(new BorderLayout());
    tabPane.addTab(Resources.get("tradeorganizer.buy"), cp);
    buy = new BuyTable();
    cp.add(new JScrollPane(buy), BorderLayout.CENTER);

    cp = new JPanel();
    cp.setLayout(new BorderLayout());
    tabPane.addTab(Resources.get("tradeorganizer.sell"), cp);
    sell = new SellTable();
    cp.add(new JScrollPane(sell), BorderLayout.CENTER);

    tabPane.add(Resources.get("tradeorganizer.stocks"), getStocksPanel());
  }

  private JPanel getStocksPanel() {
    JPanel panel = new JPanel();

    // panel.setLayout(new GridBagLayout());
    // GridBagConstraints c = new GridBagConstraints(0, 0, 1, 1, 0, 1, GridBagConstraints.CENTER,
    // GridBagConstraints.BOTH, new Insets(3, 3, 3, 3), 0, 0);
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
    stocks = new StocksTable();

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
      setRegions(CollectionFilters.filter(se.getSelectedObjects(), Region.class));
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

      if (price.getPrice() < 0) {
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

      if (checkPrice(price)) {
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
      Item luxuryPrice = price.getItemType().getResource(StringID.create("Silber"));

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

  /**
   * DOCUMENT-ME
   * 
   * @author $Author: $
   * @version $Revision: 384 $
   */
  public class SellTable extends JTable {
    private SellTableModel model;

    /**
     * Creates a new SellTable object.
     */
    public SellTable() {
      super();
      model = new SellTableModel();
      setModel(model);
      getTableHeader().setReorderingAllowed(false);
      setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
      getSelectionModel().addListSelectionListener(new ListSelectionListener() {
        public void valueChanged(ListSelectionEvent e) {
          int i = SellTable.this.getSelectedRow();
          Region r = model.getRegion(i);

          if (r != null) {
            dispatcher.fire(SelectionEvent.create(this, r));
          }
        }
      });

      // sorting
      getTableHeader().addMouseListener(new MouseAdapter() {
        @Override
        public void mouseClicked(MouseEvent e) {
          int i = getTableHeader().getColumnModel().getColumnIndexAtX(e.getPoint().x);
          model.sort(i);
        }
      });
    }

    /**
     * DOCUMENT-ME
     */
    public Collection<Region> getRegions() {
      return model.getRegions();
    }

    /**
     * DOCUMENT-ME
     */
    public void setRegions(Collection<Region> regions) {
      model.setRegions(regions);
    }

    /**
     * DOCUMENT-ME
     */
    public void sort() {
      model.sort(model.curSort);
    }
  }

  private class SellTableModel extends AbstractTableModel {
    private int curSort = 1;
    private LinkedList<Region> tableRegions = new LinkedList<Region>();

    /**
     * DOCUMENT-ME
     */
    public int getRowCount() {
      return tableRegions.size();
    }

    /**
     * DOCUMENT-ME
     */
    public int getColumnCount() {
      return 3;
    }

    /**
     * DOCUMENT-ME
     */
    public Object getValueAt(int row, int col) {
      Region region = tableRegions.get(row);

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
          return Integer.valueOf(price.getPrice());
      }

      case 2:
        return Integer.valueOf(region.getPeasants() / 100);
      }

      return null;
    }

    /**
     * DOCUMENT-ME
     */
    @Override
    public String getColumnName(int col) {
      switch (col) {
      case 0:
        return Resources.get("tradeorganizer.buycolumnname1");

      case 1:
        return Resources.get("tradeorganizer.buycolumnname2");

      case 2:
        return Resources.get("tradeorganizer.buycolumnname3");
      }

      return "";
    }

    /**
     * DOCUMENT-ME
     */
    @Override
    public Class<? extends Object> getColumnClass(int col) {
      return getValueAt(0, col).getClass();
    }

    /**
     * DOCUMENT-ME
     */
    public Collection<Region> getRegions() {
      return tableRegions;
    }

    /**
     * DOCUMENT-ME
     */
    public void setRegions(Collection<Region> regs) {
      tableRegions.clear();
      tableRegions.addAll(regs);
      sort(curSort);
    }

    /**
     * DOCUMENT-ME
     */
    public Region getRegion(int row) {
      return tableRegions.get(row);
    }

    /**
     * DOCUMENT-ME
     */
    public void sort(int i) {
      curSort = i;

      switch (i) {
      case 0:
        Collections.sort(tableRegions, new NameComparator(null));

        break;

      case 1:
        Collections.sort(tableRegions, new RegionPriceComparator());

        break;

      case 2:
        Collections.sort(tableRegions, new RegionTradeVolumeComparator());
      }

      sell.revalidate();
      sell.repaint();
    }
  }

  /**
   * DOCUMENT-ME
   * 
   * @author $Author: $
   * @version $Revision: 384 $
   */
  public class BuyTable extends JTable {
    private BuyTableModel model;

    /**
     * Creates a new BuyTable object.
     */
    public BuyTable() {
      model = new BuyTableModel();
      setModel(model);
      getTableHeader().setReorderingAllowed(false);
      setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
      getSelectionModel().addListSelectionListener(new ListSelectionListener() {
        public void valueChanged(ListSelectionEvent e) {
          int i = BuyTable.this.getSelectedRow();
          Region r = model.getRegion(i);

          if (r != null) {
            dispatcher.fire(SelectionEvent.create(this, r));
          }
        }
      });

      // sorting
      getTableHeader().addMouseListener(new MouseAdapter() {
        @Override
        public void mouseClicked(MouseEvent e) {
          int i = getTableHeader().getColumnModel().getColumnIndexAtX(e.getPoint().x);
          model.sort(i);
        }
      });
    }

    /**
     * DOCUMENT-ME
     */
    public Collection<Region> getRegions() {
      return model.getRegions();
    }

    /**
     * DOCUMENT-ME
     */
    public void setRegions(Collection<Region> regions) {
      model.setRegions(regions);
    }

    /**
     * DOCUMENT-ME
     */
    public void sort() {
      model.sort(model.curSort);
    }
  }

  private class BuyTableModel extends AbstractTableModel {
    private int curSort = 1;
    private LinkedList<Region> tableRegions = new LinkedList<Region>();

    /**
     * DOCUMENT-ME
     */
    public int getRowCount() {
      return tableRegions.size();
    }

    /**
     * DOCUMENT-ME
     */
    public int getColumnCount() {
      return 2;
    }

    /**
     * DOCUMENT-ME
     */
    public Object getValueAt(int row, int col) {
      Region region = tableRegions.get(row);

      switch (col) {
      case 0:
        return region.toString();

      case 1:
        return Integer.valueOf(region.getPeasants() / 100);
      }

      return null;
    }

    /**
     * DOCUMENT-ME
     */
    @Override
    public String getColumnName(int col) {
      switch (col) {
      case 0:
        return Resources.get("tradeorganizer.sellcolumnname1");

      case 1:
        return Resources.get("tradeorganizer.sellcolumnname2");
      }

      return "";
    }

    /**
     * DOCUMENT-ME
     */
    @Override
    public Class<? extends Object> getColumnClass(int col) {
      return getValueAt(0, col).getClass();
    }

    /**
     * DOCUMENT-ME
     */
    public Collection<Region> getRegions() {
      return tableRegions;
    }

    /**
     * DOCUMENT-ME
     */
    public void setRegions(Collection<Region> regs) {
      tableRegions.clear();
      tableRegions.addAll(regs);
      sort(curSort);
    }

    /**
     * DOCUMENT-ME
     */
    public Region getRegion(int row) {
      return tableRegions.get(row);
    }

    /**
     * DOCUMENT-ME
     */
    public void sort(int i) {
      curSort = i;

      switch (i) {
      case 0:
        Collections.sort(tableRegions, new NameComparator(null));

        break;

      case 1:
        Collections.sort(tableRegions, new RegionTradeVolumeComparator());
      }

      buy.revalidate();
      buy.repaint();
    }
  }

  private class StocksTable extends JTable {
    private StocksTableModel model;

    /**
     * Creates a new StocksTable object.
     */
    public StocksTable() {
      model = new StocksTableModel();
      setModel(model);
      getTableHeader().setReorderingAllowed(false);
      setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
      getSelectionModel().addListSelectionListener(new ListSelectionListener() {
        public void valueChanged(ListSelectionEvent e) {
          int i = StocksTable.this.getSelectedRow();
          if (i >= 0) {
            Region r = model.getRegion(i);

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
          model.sort(i);
        }
      });
    }

    /**
     * DOCUMENT-ME
     */
    public void setRegions(Collection<Region> regions) {
      model.setRegions(regions);
    }

    /**
     * DOCUMENT-ME
     */
    public void setFactions(Collection<Faction> factions) {
      model.setFactions(factions);
    }

    /**
     * DOCUMENT-ME
     */
    public void sort() {
      model.sort(model.curSort);
    }
  }

  private class StocksTableModel extends AbstractTableModel {
    private int curSort = 1;
    private LinkedList<Region> tableRegions = new LinkedList<Region>();
    private LinkedList<Faction> tableFactions = new LinkedList<Faction>();

    // maps Region to Integer (available luxury in stock of selected factions)
    private Hashtable<Region, Integer> stocks = new Hashtable<Region, Integer>();

    /**
     * DOCUMENT-ME
     */
    public int getRowCount() {
      return tableRegions.size();
    }

    /**
     * DOCUMENT-ME
     */
    public int getColumnCount() {
      return 2;
    }

    /**
     * DOCUMENT-ME
     */
    public Object getValueAt(int row, int col) {
      Region region = tableRegions.get(row);

      switch (col) {
      case 0:
        return region.toString();

      case 1:
        return stocks.get(region);
      }

      return null;
    }

    /**
     * DOCUMENT-ME
     */
    @Override
    public String getColumnName(int col) {
      switch (col) {
      case 0:
        return Resources.get("tradeorganizer.sellcolumnname1");

      case 1:
        return Resources.get("tradeorganizer.sellcolumnname2");
      }

      return "";
    }

    /**
     * DOCUMENT-ME
     */
    @Override
    public Class<? extends Object> getColumnClass(int col) {
      return getValueAt(0, col).getClass();
    }

    /**
     * DOCUMENT-ME
     */
    public void setRegions(Collection<Region> regs) {
      tableRegions.clear();
      tableRegions.addAll(regs);
      updateStocksHashtable();
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
      tableRegions.clear();
      tableRegions.addAll(regions);

      if (luxuries.getSelectedIndex() == -1) {
        luxuries.setSelectedIndex(0);
      }

      StringID actSID =
          StringID.create(getOriginalLuxuryTranslation((String) luxuries.getSelectedItem()));
      ItemType luxury = data.getRules().getItemType(actSID);

      for (Iterator<Region> regionIter = tableRegions.iterator(); regionIter.hasNext();) {
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

    /**
     * DOCUMENT-ME
     */
    public Region getRegion(int row) {
      if (row >= 0)
        return tableRegions.get(row);
      return null;
    }

    /**
     * DOCUMENT-ME
     */
    public void sort(int i) {
      curSort = i;

      switch (i) {
      case 0:
        Collections.sort(tableRegions, new NameComparator(null));

        break;

      case 1:
        Collections.sort(tableRegions, new RegionStockVolumeComparator(stocks));
      }

      TradeOrganizerOld.this.stocks.revalidate();
      TradeOrganizerOld.this.stocks.repaint();
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
     * Compares two regions by the value of their <kbd>stocks</kbd>.
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
   * compares two regions unsing their trade volume
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
      // String help[] =
      // new String[] { "Balsam", "Gewürz", "Juwel", "Myrrhe", "Öl", "Seide", "Weihrauch" };
      for (ItemType currentType : data.getRules().getItemTypes()) {
        if (currentType.getCategory() != null
            && currentType.getCategory().equals(data.getRules().getItemCategory("luxuries"))) {
          luxuryTranslations.put(currentType.toString(), data.getTranslation(currentType));

          // this.luxuryListTranslated.add(data.getTranslation(currentType));
        }
      }
    }
  }

  private String getOriginalLuxuryTranslation(String value) {
    String erg = value;
    if (luxuryTranslations == null)
      return erg;
    for (String actKey : luxuryTranslations.keySet()) {
      String actValue = luxuryTranslations.get(actKey);
      if (actValue.equalsIgnoreCase(value))
        return actKey;
    }

    return value;
  }

  /**
   * Returns the value of luxuryListTranslated.
   * 
   * @return Returns luxuryListTranslated.
   */
  public Collection<String> getLuxuryListTranslated() {
    return luxuryTranslations.values();
  }

}
