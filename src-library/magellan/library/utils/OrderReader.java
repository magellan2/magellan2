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

package magellan.library.utils;

import java.io.IOException;
import java.io.LineNumberReader;
import java.io.Reader;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Locale;
import java.util.StringTokenizer;

import magellan.library.CompleteData;
import magellan.library.EntityID;
import magellan.library.Faction;
import magellan.library.GameData;
import magellan.library.ID;
import magellan.library.Region;
import magellan.library.TempUnit;
import magellan.library.Unit;
import magellan.library.UnitID;
import magellan.library.gamebinding.EresseaConstants;
import magellan.library.rules.GenericRules;
import magellan.library.utils.logging.Logger;


/**
 * A class for reading a orders file for unit orders.
 */
public class OrderReader {
	private static final Logger log = Logger.getInstance(OrderReader.class);
	private GameData data = null;
	private LineNumberReader stream = null;
	private boolean autoConfirm = false;
	private boolean ignoreSemicolonComments = false;
	private Status status = null;
  private boolean refreshUnitRelations = true;
  private boolean doNotOverwriteConfirmedOrders = false;

	/**
	 * Creates a new OrderReader object adding the read orders to the units it can find in the
	 * specified game data object. This function clears the caches of all units.
	 *
	 * 
	 */
	public OrderReader(GameData g) {
		data = g;

		if(data == null) {
			OrderReader.log.info("OrderReader.OrderReader(): game data is null! Creating empty game data to proceed.");
			data = new CompleteData(new GenericRules());
		}

		// clear the caches in game data
		if(data.units() != null) {
			for(Iterator<Unit> iter = data.units().values().iterator(); iter.hasNext();) {
				Unit u = iter.next();

				u.clearCache();
			}
		}

		if(data.regions() != null) {
			for(Iterator<Region> iter = data.regions().values().iterator(); iter.hasNext();) {
        Region uc = iter.next();

        uc.clearCache();
			}
		}
	}

	/**
	 * Reads the orders from the specified Reader. Orders for multiple factions can be read. Region
	 * lines are ignored. Unit are not created. If there are orders for a unit that cannot be
	 * found in the game data these orders are ignored. Lines containing ECHECK comments are
	 * always ignored. Comments starting with a semicolon and containing the literal 'bestaetigt'
	 * (case and umlaut insensitive) after an arbitrary number of whitespace characters are never
	 * added to a unit's orders, instead they set the order confirmation status of the unit to
	 * true.
	 *
	 * 
	 *
	 * @throws IOException DOCUMENT-ME
	 */
	public void read(Reader in) throws IOException {
		stream = new LineNumberReader(new MergeLineReader(in));

		String line = stream.readLine();

		while(line != null) {
			StringTokenizer tokenizer = new StringTokenizer(line);

			if(tokenizer.hasMoreTokens()) {
				String token = Umlaut.normalize(tokenizer.nextToken());

				if(Resources.getOrderTranslation(EresseaConstants.O_FACTION).startsWith(token) ||
            Resources.getOrderTranslation(EresseaConstants.O_ERESSEA).startsWith(token)) {
					token = tokenizer.nextToken();

					try {
						ID fID = EntityID.createEntityID(token,data.base);
						Faction f = data.getFaction(fID);

						if(f != null) {
							readFaction(fID);
						} else {
							OrderReader.log.info("OrderReader.read(): The faction with id " + fID + " (" +
									 token +
									 ") is not present in the game data, skipping this faction.");
						}
					} catch(NumberFormatException e) {
						OrderReader.log.error("OrderReader.read(): Unable to parse faction id: " +
								  e.toString() + " at line " + stream.getLineNumber(), e);
					}
				}
			}

			line = stream.readLine();
		}
	}

	private void readFaction(ID id) throws IOException {
		Faction faction = data.getFaction(id);

		if(faction == null) {
			data.addFaction(MagellanFactory.createFaction(id, data));
		}

		String line = null; // the line read from the file
		Unit currentUnit = null; // keeps track of the unit which is currently processed
		Locale currentLocale = Locales.getOrderLocale(); // start out with the currently set default order locale

		/* normalized orders that have to be checked often in the loop
		these have to be updated whenever the locale changes */
		String naechsterOrder = Umlaut.normalize(Resources.getOrderTranslation(EresseaConstants.O_NEXT,
																				  currentLocale));
		String localeOrder = Umlaut.normalize(Resources.getOrderTranslation(EresseaConstants.O_LOCALE,
																			   currentLocale));

		if(status == null) {
			status = new Status();
		}

		status.factions++;

		while((line = stream.readLine()) != null) {
			StringTokenizer tokenizer = new StringTokenizer(line, " ;");

			/*
			There was a problem using this StringTokenizer:
			If a unit had an order like " ; Einheit hat Kommando" the tokenizer
			skipped the leading semicolon and tried to parse a new order instead
			of parsing this line as a comment.
			So treat lines, that start with a semicolon special!
			*/
			if(line.trim().startsWith(";")) {
				if(currentUnit != null) {
					// mark orders as confirmed on a ";bestaetigt" comment
					String rest = Umlaut.normalize(line.substring(line.indexOf(';') + 1).trim());

					if(rest.equalsIgnoreCase(OrderWriter.CONFIRMED)) {
						currentUnit.setOrdersConfirmed(true);
					} else if((ignoreSemicolonComments == false) &&
								  (rest.startsWith("ECHECK") == false)) {
						// add all other comments except "; ECHECK ..." to the orders
						currentUnit.addOrders(line,refreshUnitRelations);
					}
				}

				continue;
			}

			if(!tokenizer.hasMoreTokens() || line.trim().equals("")) {
				// empty line
				if(currentUnit != null) {
					currentUnit.addOrders(line,refreshUnitRelations);
				}

				continue;
			}

			String token = Umlaut.normalize(tokenizer.nextToken().trim());

			if(naechsterOrder.startsWith(token)) {
				/* turn orders into 'real' temp units */
				if(currentUnit != null) {
					currentUnit.extractTempUnits(0, currentLocale);
				}

				break;
			} else if(localeOrder.startsWith(token)) {
				if(tokenizer.hasMoreTokens()) {
					token = tokenizer.nextToken().replace('"', ' ').trim();
					currentLocale = new Locale(token, "");

					/* update the locale dependent cached orders */
					naechsterOrder = Umlaut.normalize(Resources.getOrderTranslation(EresseaConstants.O_NEXT,currentLocale));
					localeOrder = Umlaut.normalize(Resources.getOrderTranslation(EresseaConstants.O_LOCALE,currentLocale));
				}
			} else if(Resources.getOrderTranslation(EresseaConstants.O_REGION, currentLocale).startsWith(token)) {
				//ignore
				currentUnit = null;
			} else if(Resources.getOrderTranslation(EresseaConstants.O_UNIT, currentLocale).startsWith(token)) {
				token = tokenizer.nextToken();

				ID unitID = null;

				try {
					unitID = UnitID.createUnitID(token,data.base);
				} catch(NumberFormatException e) {
					OrderReader.log.error("OrderReader.readFaction(): " + e.toString() + " at line " +
							  stream.getLineNumber(), e);
				}

				if(unitID != null) {
					status.units++;

					/* turn orders into 'real' temp units */
					if(currentUnit != null) {
						currentUnit.extractTempUnits(0, currentLocale);
					}

					currentUnit = data.getUnit(unitID);

					if(currentUnit == null) {
						currentUnit = MagellanFactory.createUnit(unitID);
						currentUnit.setFaction(faction);

						data.addUnit(currentUnit);
					} else {
					  if (currentUnit.isOrdersConfirmed() && this.doNotOverwriteConfirmedOrders){
					    // we have a unit with confirmed orders and no OK for 
					    // changing anything
					    // feature request #296, Fiete
					    currentUnit=null;
					    status.confirmedUnitsNotOverwritten++;
					  } else {
  						/* the unit already exists so delete all its
  						   temp units */
  						Collection<ID> victimIDs = new LinkedList<ID>();
  
  						for(Iterator tempIter = currentUnit.tempUnits().iterator();
  								tempIter.hasNext();) {
  							victimIDs.add(((TempUnit) tempIter.next()).getID());
  						}
  
  						for(Iterator<ID> idIter = victimIDs.iterator(); idIter.hasNext();) {
  							currentUnit.deleteTemp(idIter.next(), data);
  						}
					  }
					}
					if (currentUnit!=null){
  					currentUnit.clearOrders();
  					currentUnit.setOrdersConfirmed(autoConfirm);
					}
				} else {
					currentUnit = null;
				}
			} else if(currentUnit != null) {
				currentUnit.addOrders(line,refreshUnitRelations);
			}
		}
	}

	/**
	 * Returns whether all read orders get automatically confirmed.
	 *
	 * 
	 */
	public boolean getAutoConfirm() {
		return this.autoConfirm;
	}

	/**
	 * Sets whether all read orders get automatically confirmed.
	 *
	 * 
	 */
	public void setAutoConfirm(boolean autoConfirm) {
		this.autoConfirm = autoConfirm;
	}

	/**
	 * Returns whether all comments in the orders starting with a semicolon (except confirmation
	 * comments) are ignored.
	 *
	 * 
	 */
	public boolean isIgnoringSemicolonComments() {
		return ignoreSemicolonComments;
	}

	/**
	 * Sets whether all comments in the orders starting with a semicolon (except confirmation
	 * comments) are ignored.
	 *
	 * 
	 */
	public void ignoreSemicolonComments(boolean ignoreSemicolonComments) {
		this.ignoreSemicolonComments = ignoreSemicolonComments;
	}

	/**
	 * Returns the number of factions and units that were read. This method should only be called
	 * after reading the orders has finished.
	 *
	 * 
	 */
	public Status getStatus() {
	  if (status==null)
	    status=new Status();
		return status;
	}

	/**
	 * DOCUMENT-ME
	 *
	 * @author $Author: $
	 * @version $Revision: 242 $
	 */
	public class Status {
		/** DOCUMENT-ME */
		public int units = 0;

		/** DOCUMENT-ME */
		public int factions = 0;
		
		/** 
		 * if doNotOverwriteConfirmedorders=true then this is a counter
		 * of the units which were protected by this setting and left unchanged
		 */
		public int confirmedUnitsNotOverwritten = 0;
	}

  public boolean isRefreshUnitRelations() {
    return refreshUnitRelations;
  }

  public void setRefreshUnitRelations(boolean refreshUnitRelations) {
    this.refreshUnitRelations = refreshUnitRelations;
  }

  /**
   * Returns the value of doNotOverwriteConfirmedOrders.
   * 
   * @return Returns doNotOverwriteConfirmedOrders.
   */
  public boolean DoNotOverwriteConfirmedOrders() {
    return doNotOverwriteConfirmedOrders;
  }

  /**
   * Sets the value of doNotOverwriteConfirmedOrders.
   *
   * @param doNotOverwriteConfirmedOrders The value for doNotOverwriteConfirmedOrders.
   */
  public void setDoNotOverwriteConfirmedOrders(boolean doNotOverwriteConfirmedOrders) {
    this.doNotOverwriteConfirmedOrders = doNotOverwriteConfirmedOrders;
  }
}
