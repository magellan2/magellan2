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

import java.awt.Rectangle;
import java.awt.event.ActionListener;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;

import javax.swing.JMenu;
import javax.swing.JMenuItem;

import magellan.client.MagellanContext;
import magellan.client.swing.context.ContextChangeable;
import magellan.client.swing.context.ContextObserver;
import magellan.library.ID;
import magellan.library.LuxuryPrice;
import magellan.library.Region;
import magellan.library.StringID;
import magellan.library.event.GameDataEvent;
import magellan.library.event.GameDataListener;
import magellan.library.rules.ItemCategory;
import magellan.library.rules.ItemType;
import magellan.library.utils.Resources;


/**
 * Simple extension of the default text cell renderer that has two modes: Sell-Mode: Shows the
 * luxury sold in the region(first char) and the trade value Price-Mode: Shows the price of a
 * luxury.
 *
 * @author Andreas
 * @version 1.0
 */
public class TradeTextCellRenderer extends TextCellRenderer implements GameDataListener,
																	   ActionListener,
																	   ContextChangeable
{
	protected ItemType item;
	protected String itemName;
	protected boolean sellMode = false;
	protected Collection<ItemType> allLuxuries;
	protected JMenu context;
	protected static final String KEY = "LUXURY";
	protected String stringArray[] = new String[2];
	protected ContextObserver obs;

	/**
	 * Creates new TradeTextCellRenderer
	 *
	 * 
	 * 
	 */
	public TradeTextCellRenderer(CellGeometry geo, MagellanContext context) {
		super(geo, context);
		context.getEventDispatcher().addGameDataListener(this);
		itemName = settings.getProperty("TradeTextCellRenderer.Item");
		sellMode = settings.getProperty("TradeTextCellRenderer.SellMode", "false").equals("true");
		findLuxuries();
		createContextMenu();
	}

	/**
	 * DOCUMENT-ME
	 *
	 * 
	 * 
	 *
	 * 
	 */
	public String getSingleString(Region r, Rectangle rect) {
		if(sellMode) {
			if(r.getPrices() == null) {
				return null;
			}

			Iterator<ID> it = r.getPrices().keySet().iterator();

			while(it.hasNext()) {
				ID id = it.next();
				LuxuryPrice lp = r.getPrices().get(id);

				if(lp.getPrice() < 0) {
					ItemType type = data.rules.getItemType(id, false);

					if(type != null) {
						return type.getName().substring(0, 1) + r.maxLuxuries();
					}
				}
			}
		}

		return null;
	}

	/**
	 * DOCUMENT-ME
	 *
	 * 
	 * 
	 *
	 * 
	 */
	public String[] getText(Region r, Rectangle rect) {
		if(sellMode) {
			return null;
		} else {
			if((item == null) || (r.getPrices() == null)) {
				return null;
			}

			LuxuryPrice lp = r.getPrices().get(item.getID());

			if(lp != null) {
				stringArray[0] = item.getName();
				stringArray[1] = String.valueOf(lp.getPrice());

				return stringArray;
			}

			return null;
		}
	}

	/**
	 * DOCUMENT-ME
	 *
	 * 
	 */
	public void gameDataChanged(GameDataEvent e) {
		this.data = e.getGameData();
		findLuxuries();
		reprocessContextMenu();
	}

	protected void findLuxuries() {
		if(allLuxuries == null) {
			allLuxuries = new LinkedList<ItemType>();
		} else {
			allLuxuries.clear();
		}

		item = null;

		if(data == null) {
			return;
		}

		ItemCategory cat = data.rules.getItemCategory(StringID.create("luxuries"), false);

		if(cat == null) {
			return;
		}

		for(Iterator iter = data.rules.getItemTypeIterator(); iter.hasNext();) {
			ItemType type = (ItemType) iter.next();

			if((type.getCategory() != null) && cat.equals(type.getCategory())) {
				allLuxuries.add(type);

				if(type.getID().toString().equals(itemName)) {
					item = type;
				}
			}
		}
	}

	protected void createContextMenu() {
		context = new JMenu(getName());
	}

	protected void reprocessContextMenu() {
		context.removeAll();

		Iterator it = allLuxuries.iterator();
		boolean added = false;

		while(it.hasNext()) {
			ItemType type = (ItemType) it.next();
			JMenuItem i = new JMenuItem(type.getName());
			i.addActionListener(this);
			i.putClientProperty(KEY, type);
			context.add(i);
			added = true;
		}

		if(added) {
			context.addSeparator();
		}

		JMenuItem i = new JMenuItem(Resources.get("magellan.map.tradetextcellrenderer.sellMode"));
		i.addActionListener(this);
		context.add(i);
	}

	/**
	 * DOCUMENT-ME
	 *
	 * 
	 */
	public void actionPerformed(java.awt.event.ActionEvent actionEvent) {
		JMenuItem src = (JMenuItem) actionEvent.getSource();
		item = (ItemType) src.getClientProperty(KEY);

		if(item == null) {
			sellMode = true;
		} else {
			itemName = item.getID().toString();
			settings.setProperty("TradeTextCellRenderer.Item", itemName);
			sellMode = false;
		}

		if(obs != null) {
			obs.contextDataChanged();
		}
	}

	/**
	 * DOCUMENT-ME
	 *
	 * 
	 */
	public JMenuItem getContextAdapter() {
		return context;
	}

	/**
	 * DOCUMENT-ME
	 *
	 * 
	 */
	public void setContextObserver(ContextObserver co) {
		obs = co;
	}

  /**
   * @see magellan.client.swing.map.TextCellRenderer#getName()
   */
  @Override
  public String getName() {
    return Resources.get("magellan.map.tradetextcellrenderer.name");
  }
  
  
  
}
