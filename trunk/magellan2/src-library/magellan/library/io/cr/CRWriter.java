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

package magellan.library.io.cr;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

import magellan.library.Alliance;
import magellan.library.Battle;
import magellan.library.Border;
import magellan.library.Building;
import magellan.library.CombatSpell;
import magellan.library.CoordinateID;
import magellan.library.EntityID;
import magellan.library.Faction;
import magellan.library.GameData;
import magellan.library.Group;
import magellan.library.HotSpot;
import magellan.library.ID;
import magellan.library.IntegerID;
import magellan.library.Island;
import magellan.library.Item;
import magellan.library.LuxuryPrice;
import magellan.library.Message;
import magellan.library.Potion;
import magellan.library.Region;
import magellan.library.RegionResource;
import magellan.library.Scheme;
import magellan.library.Ship;
import magellan.library.Sign;
import magellan.library.Skill;
import magellan.library.Spell;
import magellan.library.TempUnit;
import magellan.library.Unit;
import magellan.library.UnitID;
import magellan.library.io.file.FileType;
import magellan.library.rules.EresseaDate;
import magellan.library.rules.MessageType;
import magellan.library.rules.OptionCategory;
import magellan.library.rules.Options;
import magellan.library.rules.Race;
import magellan.library.rules.UnitContainerType;
import magellan.library.utils.NullUserInterface;
import magellan.library.utils.Resources;
import magellan.library.utils.TranslationType;
import magellan.library.utils.Translations;
import magellan.library.utils.Umlaut;
import magellan.library.utils.UserInterface;
import magellan.library.utils.comparator.IDComparator;
import magellan.library.utils.comparator.SortIndexComparator;
import magellan.library.utils.logging.Logger;


/**
 * A class for writing game data, or parts of it, to a stream in the computer report format.
 * 
 * <p>
 * The generated output has platform dependent line breaks.
 * </p>
 * 
 * <p>
 * Implementation notes:
 * </p>
 * 
 * <p>
 * The basic mechanism of this class is to overload the <tt>write()</tt> method for most of the
 * Eressea base classes. Since many blocks and tags come in bunches there are helper functions for
 * each such sequence handling the collections in which the data objects are stored in.
 * </p>
 */
public class CRWriter extends BufferedWriter {
	private static final Logger log = Logger.getInstance(CRWriter.class);
	private boolean useTildesForQuotes = false;

	// incremented whenever a unit is written, can then be compared
	// to the total number of units in the game data
	private int unitsWritten = 0;
	
	// fiete: see no other choice to find the familiarmage - unit
	private GameData data = null;
  
  private String encoding = FileType.DEFAULT_ENCODING.toString();
  
  private UserInterface ui = null;
  private boolean savingInProgress = false;


  /**
   * Creates a CR writer with a default-sized ouput buffer.
   *
   * @param out the stream to write output to.
   */
  public CRWriter(UserInterface ui, Writer out) {
    super(out);
    this.ui = ui;
    if (this.ui == null) {
      ui = new NullUserInterface();
    }
  }

  /**
   * Creates a CR writer with a ouput buffer of the specified size.
   *
   * @param fileType the filetype to write to
   *
   * @throws IOException DOCUMENT-ME
   */
  public CRWriter(UserInterface ui, FileType fileType, String encoding) throws IOException {
    super(fileType.createWriter(encoding));
    this.ui = ui;
    this.encoding = encoding;
    if (this.ui == null) {
      ui = new NullUserInterface();
    }
  }

  /**
   * Creates a CR writer with a ouput buffer of the specified size.
   *
   * @param fileType the filetype to write to
   *
   * @throws IOException DOCUMENT-ME
   */
  public CRWriter(UserInterface ui, FileType fileType, String encoding,int numberOfBackups) throws IOException {
    super(fileType.createWriter(encoding,numberOfBackups));
    this.ui = ui;
    this.encoding = encoding;
    if (this.ui == null) {
      ui = new NullUserInterface();
    }
  }
  
  
	/**
	 * Escape quotation marks in <tt>text</tt> with a backslash.
	 *
	 * @param text the string to be modified.
	 *
	 * @return the resulting string with escaped quotation marks.
	 */
	private String escapeQuotes(String text) {
		if(text == null) {
			CRWriter.log.warn("CRWriter.escapeQuotes(): argument 'text' is null");

			return null;
		}

		StringBuffer sb = new StringBuffer(text.length() + 2);

		for(int i = 0; i < text.length(); i++) {
			char c = text.charAt(i);

			if(c == '"') {
				sb.append('\\');
			}

			sb.append(c);
		}

		return sb.toString();
	}

	/**
	 * Remove quotes from text and replace blanks whithin the quoted section with ~ characters. ("a
	 * \"b c\"" -> "a b~c")
	 *
	 * @param text the string to be modified.
	 *
	 * @return the resulting string.
	 */
	private String tildeQuotes(String text) {
		if(text == null) {
			CRWriter.log.warn("CRWriter.tildeQuotes(): argument 'text' is null");

			return null;
		}

		StringBuffer sb = new StringBuffer(text.length() + 2);
		boolean replace = false;

		for(int i = 0; i < text.length(); i++) {
			char c = text.charAt(i);

			if(c == '"') {
				replace = !replace;
			} else if((c == ' ') && replace) {
				sb.append('~');
			} else {
				sb.append(c);
			}
		}

		return sb.toString();
	}

	/**
	 * Write the String <tt>str</tt> quoted to the underlying stream. If a part of <tt>str</tt> is
	 * quoted, its quotes are escaped according to the current quote escape setting.
	 *
	 * 
	 *
	 * @throws IOException DOCUMENT-ME
	 */
	private void writeQuotedString(String str) throws IOException {
		if(str == null) {
			CRWriter.log.warn("CRWriter.writeQuotedString(): argument str is null");

			return;
		}

		boolean repairString = false;

		if(str.indexOf('\n') != -1) {
			repairString = true;
			CRWriter.log.warn("CRWriter.writeQuotedString(): argument str contains \'\\n\'. Splitting line.");
		}

		if(str.indexOf('\r') != -1) {
			repairString = true;
			CRWriter.log.warn("CRWriter.writeQuotedString(): argument str contains \'\\r\'. Splitting line.");
		}

		if(repairString) {
			// 2002.04.05 pavkovic: It seems that where exist a string with "\r\n" inside
			// These will be written linewise
			StringTokenizer st = new StringTokenizer(str, "\n\r");

			while(st.hasMoreTokens()) {
				writeQuotedString(st.nextToken());
			}

			return;
		}

		if(useTildesForQuotes) {
			write("\"" + tildeQuotes(str) + "\"");
		} else {
			write("\"" + escapeQuotes(str) + "\"");
		}

		newLine();
	}

	/**
	 * Write the String <tt>str</tt> quoted along with the specified tag to the underlying stream.
	 * If a part of <tt>str</tt> is quoted, its quotes are escaped according to the current quote
	 * escape setting. writeQuotedTag("a b", "tag") results in writing "\"a b\";tag\n" to the
	 *
	 * @param str the string that is to be put in quotes and written to the
	 * @param tag the tag to be written to the stream, separated from <tt>str</tt> by a semicolon.
	 *
	 * @throws IOException DOCUMENT-ME
	 */
	private void writeQuotedTag(String str, String tag) throws IOException {
		if(str == null) {
			CRWriter.log.warn("CRWriter.writeQuotedTag(): argument str is null");

			return;
		}

		if(tag == null) {
			CRWriter.log.warn("CRWriter.writeQuotedTag(): argument tag is null");

			return;
		}

		if(useTildesForQuotes) {
			write("\"" + tildeQuotes(str) + "\";" + tag);
		} else {
			write("\"" + escapeQuotes(str) + "\";" + tag);
		}

		newLine();
	}

  private void writeCoordinateTranslations(GameData world) throws IOException {
    for (ID f: world.factions().keySet()){
      EntityID fID = (EntityID) f;
      Map<Integer, CoordinateID> map = world.getCoordinateTranslationMap(fID);
      if (map!=null && !map.isEmpty()){
        write("COORDTRANS " + (fID).intValue());
        newLine();
        for (CoordinateID t : map.values()){
          write(t.toString(" ")+";translation");
          newLine();
        }
      }
    }
  }

	/**
	 * Write a sequence of message blocks to the underlying stream.
	 *
	 * @param list a list containing the <tt>Message</tt> objects to be written.
	 *
	 * @throws IOException DOCUMENT-ME
	 */
	public void writeMessages(List list) throws IOException {
		if(list == null) {
			return;
		}

		for(Iterator iter = list.iterator(); iter.hasNext();) {
			writeMessage((Message) iter.next());
		}
	}

	/**
	 * Write the cr representation of a <tt>Message</tt> object to the underlying stream.
	 *
	 * 
	 *
	 * @throws IOException DOCUMENT-ME
	 */
	public void writeMessage(Message msg) throws IOException {
		if(msg == null) {
			return;
		}

		write("MESSAGE " + msg.getID());
		newLine();

		if(msg.getMessageType() != null) {
			write(msg.getMessageType().getID() + ";type");
			newLine();
		}

		if(msg.getText() != null) {
			writeQuotedTag(msg.getText(), "rendered");
		}

		if(msg.getAttributes() != null) {
			for(Iterator<String> iter = msg.getAttributes().keySet().iterator(); iter.hasNext();) {
				String key = iter.next();
				String value =  msg.getAttributes().get(key);

				try {
					Integer.parseInt(value);
					write(value + ";" + key);
					newLine();
				} catch(NumberFormatException e) {
					CoordinateID c = CoordinateID.parse(value, " ");

					if(c != null) {
						write(value + ";" + key);
						newLine();
					} else {
						writeQuotedTag(value, key);
					}
				}
			}
		}
	}

	/**
	 * Write the data as one block named <tt>blockName</tt> to the underlying stream. The data is
	 * written as simple cr strings. The block name is only written if there is data to follow.
	 *
	 * @param blockName the name of the block to be written (can not be a block with an id).
	 * @param data a collection containing <tt>Message</tt> objects.
	 *
	 * @throws IOException DOCUMENT-ME
	 */
	public void writeMessageBlock(String blockName, Collection data) throws IOException {
		if((data == null) || data.isEmpty()) {
			return;
		}

		write(blockName);
		newLine();
		writeMessageSequence(data);
	}

	/**
	 * Write the data as one sequence of simple cr strings.
	 *
	 * @param data a collection containing <tt>Message</tt> objects.
	 *
	 * @throws IOException DOCUMENT-ME
	 */
	public void writeMessageSequence(Collection data) throws IOException {
		if((data == null) || data.isEmpty()) {
			return;
		}

		for(Iterator iter = data.iterator(); iter.hasNext();) {
			Message msg = (Message) iter.next();
			writeQuotedString(msg.getText());
		}
	}

	/**
	 * Write a the data as one sequence of simple cr strings.
	 *
	 * @param data a collection containing <tt>String</tt> objects.
	 *
	 * @throws IOException DOCUMENT-ME
	 */
	public void writeStringSequence(Collection data) throws IOException {
		if((data == null) || data.isEmpty()) {
			return;
		}

		for(Iterator iter = data.iterator(); iter.hasNext();) {
			String str = (String) iter.next();
			writeQuotedString(str);
		}
	}

	/**
	 * Write the data as one block named <tt>blockName</tt> to the underlying stream. The data is
	 * written as simple cr strings. The block name is only written if there is data to follow.
	 *
	 * @param blockName the name of the block to be written (can not be a block with an id).
	 * @param data a colleciton containing <tt>String</tt> objects.
	 *
	 * @throws IOException DOCUMENT-ME
	 */
	public void writeStringBlock(String blockName, Collection data) throws IOException {
		if((data == null) || data.isEmpty()) {
			return;
		}

		write(blockName);
		newLine();
		writeStringSequence(data);
	}

	/**
	 * Write the VERSION block for the specified game data to the underyling
	 *
	 * 
	 *
	 * @throws IOException DOCUMENT-ME
	 */
	public void writeVersion(GameData world) throws IOException {
		write("VERSION 64");
		newLine();
		
		// The Echecker of German Atlantis has problems with the locale line
		// so we check the game name
		if (!world.getGameName().startsWith("GAV")){
      writeQuotedTag(encoding,"charset");
			if(world.getLocale() != null) {
				writeQuotedTag(world.getLocale().toString(), "locale");
			}
		}
		if(world.noSkillPoints) {
			write("1;noskillpoints");
			newLine();
		}

		write((System.currentTimeMillis() / 1000) + ";date");
		newLine();

		// keep the game type, when writing a CR.
		writeQuotedTag(world.getGameName(), "Spiel");

		if(serverConformance) {
			writeQuotedTag("Standard", "Konfiguration");
		} else {
			writeQuotedTag("Java-Tools", "Konfiguration");
		}

		writeQuotedTag("Hex", "Koordinaten");
		
		// Tracking a bug
		String actGameName = world.getGameName().toLowerCase();
		if ((actGameName.indexOf("eressea")>-1 || actGameName.indexOf("vinyambar")>-1) && (world.base!=36)){
			// this should not happen
			CRWriter.log.warn("BASE ERROR !! report to write could have not base36 !! Changed to base36. (Was " + world.base + ")");
			world.base = 36;
		}
		write(world.base + ";Basis");
		newLine();
		write("1;Umlaute");
		newLine();

		if(!serverConformance && (world.getCurTempID() != -1)) {
			write(world.getCurTempID() + ";curTempID");

			/**
			 * @see com.eressea.GameData#curTempID
			 */
			newLine();
		}

		if(world.getDate() != null) {
			write(world.getDate().getDate() + ";Runde");
			newLine();
			write(((EresseaDate) world.getDate()).getEpoch() + ";Zeitalter");
			newLine();
		}

		if(world.mailTo != null) {
			writeQuotedTag(world.mailTo, "mailto");
		}

		if(world.mailSubject != null) {
			writeQuotedTag(world.mailSubject, "mailcmd");
		}
		
		if (!serverConformance && world.getOwnerFaction()!=null){
		  write(world.getOwnerFaction().intValue()+ ";reportowner");
		  newLine();
		}
	}

	/**
	 * Write a spells (ZAUBER) block to the underlying stream.
	 *
	 * @param map a map containing the spells to write. The keys are expected to be
	 * 		  <tt>Integer</tt> objects containing the ids of the spells. The values are expected
	 * 		  to be instances of class <tt>Spell</tt>.
	 *
	 * @throws IOException DOCUMENT-ME
	 */
	public void writeSpells(Map map) throws IOException {
		if(map == null) {
			return;
		}

		for(Iterator iter = map.values().iterator(); iter.hasNext();) {
			write((Spell) iter.next());
		}
	}

	/**
	 * Write the cr representation of a <tt>Spell</tt> object to the underlying stream.
	 *
	 * 
	 *
	 * @throws IOException DOCUMENT-ME
	 */
	public void write(Spell spell) throws IOException {
		if(spell.getBlockID() == -1) {
			return;
		}

		write("ZAUBER " + spell.getBlockID());
		newLine();

		if(spell.getName() != null) {
			writeQuotedTag(spell.getName(), "name");
		}

		write(spell.getLevel() + ";level");
		newLine();
		write(spell.getRank() + ";rank");
		newLine();

		if(spell.getDescription() != null) {
			writeQuotedTag(spell.getDescription(), "info");
		}

		if(spell.getType() != null) {
			writeQuotedTag(spell.getType(), "class");
		}

		if(spell.getOnOcean()) {
			write("1;ocean");
			newLine();
		}

		if(spell.getOnShip()) {
			write("1;ship");
			newLine();
		}

		if(spell.getIsFar()) {
			write("1;far");
			newLine();
		}

		if(spell.getIsFamiliar()) {
			write("1;familiar");
			newLine();
		}

		if (spell.getSyntax()!=null){
			writeQuotedTag(spell.getSyntax(),"syntax");
		}
		
		
		writeSpellComponents(spell.getComponents());
	}

	/**
	 * Write a sequence of potion (TRANK) blocks to the underlying stream.
	 *
	 * @param map a map containing the potions to write. The keys are expected to be
	 * 		  <tt>Integer</tt> objects containing the ids of the potions. The values are expected
	 * 		  to be instances of class <tt>Potion</tt>.
	 *
	 * @throws IOException DOCUMENT-ME
	 */
	public void writePotions(Map map) throws IOException {
		if(map == null) {
			return;
		}

		for(Iterator iter = map.values().iterator(); iter.hasNext();) {
			write((Potion) iter.next());
		}
	}

	/**
	 * Write the cr representation of a <tt>Potion</tt> object to the underlying stream.
	 *
	 * 
	 *
	 * @throws IOException DOCUMENT-ME
	 */
	public void write(Potion potion) throws IOException {
		write("TRANK " + potion.getID().toString());
		newLine();

		if(potion.getName() != null) {
			writeQuotedTag(potion.getName(), "Name");
		}

		write(potion.getLevel() + ";Stufe");
		newLine();
		writeQuotedTag(potion.getDescription(), "Beschr");
		writePotionIngredients(potion.ingredients());
	}

	/**
	 * Writes the ingredients of a potion as a ZUTATEN block to the underlying stream.
	 *
	 * @param ingredients a collection containing Item objects.
	 *
	 * @throws IOException DOCUMENT-ME
	 */
	public void writePotionIngredients(Collection ingredients) throws IOException {
		if(!ingredients.isEmpty()) {
			write("ZUTATEN");
			newLine();

			for(Iterator iter = ingredients.iterator(); iter.hasNext();) {
				Item i = (Item) iter.next();
				writeQuotedString(i.getItemType().getID().toString());
			}
		}
	}

	/**
	 * Write a spell components (KOMPONENTEN) block to the underyling. The block name is only
	 * written, if there are components in <tt>comps</tt>.
	 *
	 * @param comps a map containing the components to be written. The map is expected to contain
	 * 		  the names of the components as keys and the component data as values (both
	 * 		  as<tt>String</tt> objects). Such a map can be found in the <tt>Spell</tt> class.
	 *
	 * @throws IOException DOCUMENT-ME
	 *
	 * @see magellan.library.Spell
	 */
	public void writeSpellComponents(Map comps) throws IOException {
		if(comps == null) {
			return;
		}

		Iterator iter = comps.keySet().iterator();

		if(iter.hasNext()) {
			write("KOMPONENTEN");
			newLine();
		}

		while(iter.hasNext()) {
			String key = (String) iter.next();
			String value = (String) comps.get(key);

			try {
				Integer.parseInt(value);
				write(value + ";" + key);
				newLine();
			} catch(NumberFormatException e) {
				writeQuotedTag(value, key);
			}
		}
	}

	/**
	 * Write the cr representation of a <tt>Option</tt> object to the underlying stream.
	 *
	 * 
	 *
	 * @throws IOException DOCUMENT-ME
	 */
	public void write(Options options) throws IOException {
		write("OPTIONEN");
		newLine();

		for(Iterator iter = options.options().iterator(); iter.hasNext();) {
			OptionCategory o = (OptionCategory) iter.next();
			write((o.isActive() ? "1" : "0") + ";" + o.getID().toString());
			newLine();
		}
	}

	/**
	 * Write a sequence of group (GRUPPE) blocks to the underlying stream.
	 *
	 * @param map a map containing the groups to write. The keys are expected to be
	 * 		  <tt>Integer</tt> objects containing the ids of the alliances. The values are
	 * 		  expected to be instances of class <tt>Group</tt>.
	 *
	 * @throws IOException DOCUMENT-ME
	 */
	public void writeGroups(Map map) throws IOException {
		if(map == null) {
			return;
		}

		for(Iterator i = map.values().iterator(); i.hasNext();) {
			writeGroup((Group) i.next());
		}
	}

	/**
	 * Write the cr representation of a <tt>Group</tt> object to the underlying stream.
	 *
	 * 
	 *
	 * @throws IOException DOCUMENT-ME
	 */
	public void writeGroup(Group group) throws IOException {
		if(group == null) {
			return;
		}

		write("GRUPPE " + group.getID());
		newLine();

		if(group.getName() != null) {
			writeQuotedTag(group.getName(), "name");
		}

		if(group.getRaceNamePrefix() != null) {
			writeQuotedTag(group.getRaceNamePrefix(), "typprefix");
		}

		writeAlliances(group.allies());
	}

	/**
	 * Write a sequence of alliance (ALLIANZ) blocks to the underlying stream.
	 *
	 * @param map a map containing the alliances to write. The keys are expected to be
	 * 		  <tt>Integer</tt> objects containing the ids of the alliances. The values are
	 * 		  expected to be instances of class <tt>Alliance</tt>.
	 *
	 * @throws IOException DOCUMENT-ME
	 */
	public void writeAlliances(Map map) throws IOException {
		if(map == null) {
			return;
		}

		for(Iterator iter = map.values().iterator(); iter.hasNext();) {
			write((Alliance) iter.next());
		}
	}

	/**
	 * Write the cr representation of an <tt>Alliance</tt> object to the underlying stream.
	 *
	 * 
	 *
	 * @throws IOException DOCUMENT-ME
	 */
	public void write(Alliance alliance) throws IOException {
		if(alliance == null) {
			return;
		}

		Faction f = alliance.getFaction();
		write("ALLIANZ " + ((EntityID) f.getID()).intValue());
		newLine();

		if(f.getName() != null) {
			writeQuotedTag(f.getName(), "Parteiname");
		}

		write(alliance.getState() + ";Status");
		newLine();
	}

	/**
	 * Write a sequence of battle (BATTLE) blocks to the underlying stream.
	 *
	 * @param list a list containing the <tt>Battle</tt> objects to be written
	 *
	 * @throws IOException DOCUMENT-ME
	 */
	public void writeBattles(List list) throws IOException {
		if(list == null) {
			return;
		}

		for(Iterator iter = list.iterator(); iter.hasNext();) {
			write((Battle) iter.next());
		}
	}

	/**
	 * Write the cr representation of a <tt>Battle</tt> object to the underlying stream.
	 *
	 * 
	 *
	 * @throws IOException DOCUMENT-ME
	 */
	public void write(Battle battle) throws IOException {
		if(battle == null) {
			return;
		}

		if(!battle.isBattleSpec()) {
			write("BATTLE " + battle.getID().toString(" "));
		} else {
			write("BATTLESPEC " + battle.getID().toString(" "));
		}

		newLine();
		writeMessages(battle.messages());
	}

	/**
	 * Write a sequence of faction (PARTEI) blocks to the underlying stream.
	 *
	 * @param map a map containing the factions to write. The keys are expected to be
	 * 		  <tt>Integer</tt> objects containing the ids of the factions. The values are expected
	 * 		  to be instances of class <tt>Faction</tt>.
	 *
	 * @throws IOException DOCUMENT-ME
	 */
	public void writeFactions(Map<ID, Faction> map) throws IOException {
    if (map == null) {
      return;
    }

    // write owner first
    Faction ownerFaction = null;
    if (map.values().size() > 0) {
      ownerFaction = map.values().iterator().next();
    }
    if (ownerFaction != null) {
      writeFaction(ownerFaction);
    }
    List<Faction> sorted = new ArrayList<Faction>(map.values());
    Comparator<Faction> sortIndexComparator = new SortIndexComparator<Faction>(IDComparator.DEFAULT);
    Collections.sort(sorted, sortIndexComparator);

    // write other factions
    for (Faction f : sorted) {
      if (ownerFaction == null || !f.equals(ownerFaction)) {
        writeFaction(f);
      }
    }
	}

	/**
	 * Write the cr representation of a <tt>Faction</tt> object to the underlying stream.
	 *
	 * 
	 *
	 * @throws IOException DOCUMENT-ME
	 */
	public void writeFaction(Faction faction) throws IOException {
		if(((EntityID) faction.getID()).intValue() == -1) {
			return;
		}

		write("PARTEI " + ((EntityID) faction.getID()).intValue());
		newLine();

		//if (faction.password != null) {
		//	writeQuotedTag(faction.password, "Passwort");
		//}
		if(faction.getLocale() != null) {
			writeQuotedTag(faction.getLocale().toString(), "locale");
		}

		if(faction.getOptions() != null) {
			write(faction.getOptions().getBitMap() + ";Optionen");
			newLine();
		}

		if(faction.getScore() != -1) {
			write(faction.getScore() + ";Punkte");
			newLine();
		}

		if(faction.getAverageScore() != -1) {
			write(faction.getAverageScore() + ";Punktedurchschnitt");
			newLine();
		}

		Race race = faction.getRace();

		if(race != null) {
			if(race.toString() != null) {
				writeQuotedTag(race.getID().toString(), "Typ");
			}

			if(race.getRecruitmentCosts() != -1) {
				write(race.getRecruitmentCosts() + ";Rekrutierungskosten");
				newLine();
			}
		}

		if(faction.getPersons() != -1) {
			write(faction.getPersons() + ";Anzahl Personen");
			newLine();
		}

		if(faction.getMigrants() != -1) {
			write(faction.getMigrants() + ";Anzahl Immigranten");
			newLine();
		}
		
		if(faction.getHeroes() != -1) {
			write(faction.getHeroes() + ";heroes");
			newLine();
		}
		
		if(faction.getMaxHeroes() != -1) {
			write(faction.getMaxHeroes() + ";max_Heroes");
			newLine();
		}
		
		if(faction.getAge() != -1) {
			write(faction.getAge() + ";age");
			newLine();
		}
		
		if(faction.getMaxMigrants() != -1) {
			write(faction.getMaxMigrants() + ";Max. Immigranten");
			newLine();
		}

		if(faction.getSpellSchool() != null) {
			writeQuotedTag(faction.getSpellSchool(), "Magiegebiet");
		}

		if(faction.getName() != null) {
			writeQuotedTag(faction.getName(), "Parteiname");
		}

		if(faction.getEmail() != null) {
			writeQuotedTag(faction.getEmail(), "email");
		}

		if(faction.getDescription() != null) {
			writeQuotedTag(faction.getDescription(), "banner");
		}

    if(faction.getRaceNamePrefix() != null) {
      writeQuotedTag(faction.getRaceNamePrefix(), "typprefix");
    }

    if(faction.getTreasury() != 0) {
      write(faction.getTreasury()+";Schatz");
      newLine();
    }

		if(!serverConformance && faction.isTrustLevelSetByUser()) {
			write(faction.getTrustLevel() + ";trustlevel");
			newLine();
		}
		
		writeItems(faction.getItems().iterator());

		if(faction.getOptions() != null) {
			write(faction.getOptions());
		}

		writeAlliances(faction.getAllies());
		writeGroups(faction.getGroups());

		if(includeMessages) {
			writeStringBlock("FEHLER", faction.getErrors());
			writeMessages(faction.getMessages());
			writeBattles(faction.getBattles());

			if(!serverConformance) {
				writeStringBlock("COMMENTS", faction.getComments());
			}
		}
	}

	/**
	 * Write a sequence of ship (SCHIFF) blocks to the underlying stream.
	 *
	 * @param ships an iterator containing the<tt>Ship</tt> objects to be written.
	 *
	 * @throws IOException DOCUMENT-ME
	 */
	public void writeShips(Collection<Ship> ships) throws IOException {
		if(ships == null) {
			return;
		}

		List<Ship> sorted = new ArrayList<Ship>(ships);
		Comparator<Ship> sortIndexComparator = new SortIndexComparator<Ship>(IDComparator.DEFAULT);
		Collections.sort(sorted, sortIndexComparator);

		for(Iterator<Ship> iter = sorted.iterator(); iter.hasNext();) {
			writeShip(iter.next());
		}
	}

	/**
	 * Write the cr representation of a <tt>Ship</tt> object to the underlying stream.
	 *
	 * 
	 *
	 * @throws IOException DOCUMENT-ME
	 */
	public void writeShip(Ship ship) throws IOException {
		write("SCHIFF " + ((EntityID) ship.getID()).intValue());
		newLine();

		if(ship.getName() != null) {
			writeQuotedTag(ship.getName(), "Name");
		}

		if(ship.getDescription() != null) {
			writeQuotedTag(ship.getDescription(), "Beschr");
		}

		UnitContainerType type = ship.getType();

		if(type != null) {
			writeQuotedTag(type.getID().toString(), "Typ");
		}

		if(ship.getDamageRatio() > 0) {
			write(ship.getDamageRatio() + ";Schaden");
			newLine();
		}

		if(ship.getSize() != -1) {
			write(ship.getSize() + ";Groesse");
			newLine();
		}

		if(ship.getShoreId() != -1) {
			write(ship.getShoreId() + ";Kueste");
			newLine();
		}

		if(shallExportUnit(ship.getOwnerUnit())) {
			write(((UnitID) ship.getOwnerUnit().getID()).intValue() + ";Kapitaen");
			newLine();

			if(ship.getOwnerUnit().getFaction() != null) {
				write(((EntityID) ship.getOwnerUnit().getFaction().getID()).intValue() + ";Partei");
				newLine();
			}
		}

		if(ship.getCargo() != -1) {
			write(ship.getCargo() + ";cargo");
			newLine();
		}

		if(ship.getCapacity() != -1) {
			write(ship.getCapacity() + ";capacity");
			newLine();
		}
		
		if(ship.getDeprecatedLoad() != -1) {
			write(ship.getDeprecatedLoad() + ";Ladung");
			newLine();
		}

		if(ship.getDeprecatedCapacity() != -1) {
			write(ship.getDeprecatedCapacity() + ";MaxLadung");
			newLine();
		}

		if(includeMessages) {
			writeStringBlock("EFFECTS", ship.getEffects());

			if(!serverConformance) {
				writeStringBlock("COMMENTS", ship.getComments());
			}
		}
	}

	/**
	 * Write a sequence of building (BURG) blocks to the underlying stream.
	 *
	 * @param buildings an iterator containing the<tt>Building</tt> objects to be written.
	 *
	 * @throws IOException
	 */
	public void writeBuildings(Collection<Building> buildings) throws IOException {
		if(buildings == null) {
			return;
		}

		List<Building> sorted = new ArrayList<Building>(buildings);
		Comparator<Building> sortIndexComparator = new SortIndexComparator<Building>(IDComparator.DEFAULT);
		Collections.sort(sorted, sortIndexComparator);

		for(Iterator<Building> iter = sorted.iterator(); iter.hasNext();) {
			writeBuilding(iter.next());
		}
	}

	/**
	 * Write the cr representation of a <tt>Building</tt> object to the underlying stream.
	 *
	 * 
	 *
	 * @throws IOException DOCUMENT-ME
	 */
	public void writeBuilding(Building building) throws IOException {
		if(building == null) {
			return;
		}

		UnitContainerType type = building.getType();
		write("BURG " + ((EntityID) building.getID()).intValue());
		newLine();

		if(type != null) {
			writeQuotedTag(type.getID().toString(), "Typ");
		}

		if(building.getName() != null) {
			writeQuotedTag(building.getName(), "Name");
		}

		if(building.getDescription() != null) {
			writeQuotedTag(building.getDescription(), "Beschr");
		}

		if(building.getSize() > 0) {
			write(building.getSize() + ";Groesse");
			newLine();
		}

		if(shallExportUnit(building.getOwnerUnit())) {
			write(((UnitID) building.getOwnerUnit().getID()).intValue() + ";Besitzer");
			newLine();

			if(building.getOwnerUnit().getFaction() != null) {
				write(((EntityID) building.getOwnerUnit().getFaction().getID()).intValue() +
					  ";Partei");
				newLine();
			}
		}

		if(building.getCost() > 0) {
			write(building.getCost() + ";Unterhalt");
			newLine();
		}

		if(includeMessages) {
			writeStringBlock("EFFECTS", building.getEffects());

			if(!serverConformance) {
				writeStringBlock("COMMENTS", building.getComments());
			}
		}
	}

	/**
	 * Write a skills (TALENTE) block to the underlying stream. The block is only written, if
	 * <tt>skills</tt> contains at least one <tt>Skill</tt> object.
	 *
	 * @param skills an iterator over the <tt>Skill</tt> objects to write.
	 * @param persons the number of persons in the unit this skill belongs to.
	 *
	 * @throws IOException DOCUMENT-ME
	 */
	public void writeSkills(Iterator<Skill> skills, int persons) throws IOException {
		if(skills.hasNext()) {
			write("TALENTE");
			newLine();
		}

		while(skills.hasNext()) {
			writeSkill(skills.next(), persons);
		}
	}

	/**
	 * Write the cr representation of a <tt>Skill</tt> object to the underlying stream.
	 *
	 * 
	 * 
	 *
	 * @throws IOException DOCUMENT-ME
	 */
	public void writeSkill(Skill skill, int persons) throws IOException {
		write(skill.getPoints() + " " + skill.getRealLevel());

		if(!getServerConformance() && skill.isLevelChanged()) {
			write(" " + skill.getChangeLevel());
		}

		write(";" + skill.getSkillType().getID());

		newLine();
	}

	/**
	 * Write a COMMANDS block to the underlying stream. The block is only written, if <tt>list</tt>
	 * contains at least one <tt>String</tt> object representing an order.
	 *
	 * @param list a list with the <tt>String</tt> objects to be written.
	 *
	 * @throws IOException DOCUMENT-ME
	 */
	public void writeOrders(List list) throws IOException {
		if((list == null) || list.isEmpty()) {
			return;
		}

		write("COMMANDS");
		newLine();

		for(Iterator iter = list.iterator(); iter.hasNext();) {
			writeQuotedString((String) iter.next());
		}
	}

	/**
	 * Write a unit's spell (SPRUECHE) block to the underlying stream. The block is only written,
	 * if <tt>list</tt> contains at least one <tt>Spell</tt> object.
	 *
	 * @param spells a list with the<tt>Spell</tt> object names to be written.
	 *
	 * @throws IOException DOCUMENT-ME
	 */
	public void writeUnitSpells(Map spells) throws IOException {
		if(spells == null) {
			return;
		}

		Iterator i = spells.values().iterator();

		if(i.hasNext()) {
			write("SPRUECHE");
			newLine();
		}

		while(i.hasNext()) {
			Spell s = (Spell) i.next();
			writeQuotedString(s.getName());
		}
	}

	/**
	 * Write a unit's combat spell (KAMPFZAUBER) blocks to the underlying stream.
	 *
	 * @param map a Map with the <tt>CombatSpell</tt> objects to be written.
	 *
	 * @throws IOException DOCUMENT-ME
	 */
	public void writeUnitCombatSpells(Map map) throws IOException {
		if(map == null) {
			return;
		}

		Iterator i = map.values().iterator();

		while(i.hasNext()) {
			CombatSpell cs = (CombatSpell) i.next();
			write(cs);
		}
	}

	/**
	 * Write the cr representation of a <tt>CombatSpell</tt> object to the underlying stream.
	 *
	 * 
	 *
	 * @throws IOException DOCUMENT-ME
	 */
	public void write(CombatSpell cs) throws IOException {
		if(cs != null) {
			if(cs.getID() != null) {
				write("KAMPFZAUBER " + cs.getID().toString());
				newLine();

				if(cs.getSpell() != null) {
					if(cs.getSpell().getName() != null) {
						writeQuotedTag(cs.getSpell().getName(), "name");
					} else {
						CRWriter.log.warn("CRWriter.write(CombatSpell): warning: spell name is null!");
					}
				} else {
					CRWriter.log.warn("CRWriter.write(CombatSpell): warning: spell is null!");
				}

				write(cs.getCastingLevel() + ";level");
				newLine();
			} else {
				CRWriter.log.warn("CRWriter.write(CombatSpell): warning: combat spell ID is null!");
			}
		} else {
			CRWriter.log.warn("CRWriter.write(CombatSpell): warning: combat spell is null!");
		}
	}

	/**
	 * Write a unit's items (GEGENSTAENDE) block to the underlying stream. The block is only
	 * written, if <tt>items</tt> contains at least one <tt>Item</tt> object.
	 *
	 * @param items an iterator over the <tt>Item</tt> objects to be written.
	 *
	 * @throws IOException DOCUMENT-ME
	 */
	public void writeItems(Iterator items) throws IOException {
		if(items.hasNext()) {
			write("GEGENSTAENDE");
			newLine();
		}

		while(items.hasNext()) {
			Item item = (Item) items.next();
			write(item);
		}
	}

	/**
	 * Write the cr representation of a <tt>Item</tt> object to the underlying stream.
	 *
	 * 
	 *
	 * @throws IOException DOCUMENT-ME
	 */
	public void write(Item item) throws IOException {
		write(item.getAmount() + ";" + item.getItemType().getID());
		newLine();
	}

	/**
	 * Write a sequence of unit (EINHEIT) blocks to the underlying stream.
	 *
	 * @param units an iterator for the<tt>Unit</tt> objects to be written.
	 *
	 * @throws IOException DOCUMENT-ME
	 */
	public void writeUnits(Collection<Unit> units) throws IOException {
		if(units == null) {
			return;
		}

		List<Unit> sorted = new ArrayList<Unit>(units);
		Comparator<Unit> sortIndexComparator = new SortIndexComparator<Unit>(IDComparator.DEFAULT);
		Collections.sort(sorted, sortIndexComparator);

		for(Iterator<Unit> iter = sorted.iterator(); iter.hasNext();) {
			Unit u = iter.next();
			writeUnit(u);
		}
	}

	/**
	 * 
	 * @param u the unit to export
	 * @return true iff units == null or empty or units contains u
	 */
	private boolean shallExportUnit(Unit u) {
		return u != null && 
			(units == null || units.isEmpty() || units.contains(u));
	}

	/**
	 * Write the cr representation of a <tt>Unit</tt> object to the underyling
	 *
	 * 
	 *
	 * @throws IOException DOCUMENT-ME
	 */
	public void writeUnit(Unit unit) throws IOException {
		if(unit instanceof TempUnit || !shallExportUnit(unit)) {
			return;
		}

		unitsWritten++;
		write("EINHEIT " + ((UnitID) unit.getID()).intValue());
		newLine();

		if(unit.getName() != null) {
			writeQuotedTag(unit.getName(), "Name");
		}

		if(unit.getDescription() != null) {
			writeQuotedTag(unit.getDescription(), "Beschr");
		}

		if(unit.getPrivDesc() != null) {
			writeQuotedTag(unit.getPrivDesc(), "privat");
		}

		if(unit.getFaction() != null) {
			int id = ((EntityID) unit.getFaction().getID()).intValue();

			if(id != -1) {
				write(id + ";Partei");
				newLine();
			}
		}

		write(unit.getPersons() + ";Anzahl");
		newLine();

		if(unit.getRace() != null) {
			write("\"" + unit.getRace().getID().toString() + "\";Typ");
			newLine();
		}

		if(unit.getTempID() != null) {
			write(unit.getTempID().intValue() + ";temp");
			newLine();
		}

		if(unit.getAlias() != null) {
			write( unit.getAlias().intValue() + ";alias");
			newLine();
		}

		if(unit.getCombatStatus() != -1) {
			write(unit.getCombatStatus() + ";Kampfstatus");
			newLine();
		}

		if(unit.isUnaided()) {
			write("1;unaided");
			newLine();
		}

		if(unit.getStealth() != -1) {
			write(unit.getStealth() + ";Tarnung");
			newLine();
		}

		if(unit.getRealRace() != null) {
			write("\"" + unit.getRealRace() + "\";wahrerTyp");
			newLine();
		}

		if(unit.getShip() != null && includeShips) {
			write(((EntityID) unit.getShip().getID()).intValue() + ";Schiff");
			newLine();
		}

		if(unit.getBuilding() != null && includeBuildings) {
			write(((EntityID) unit.getBuilding().getID()).intValue() + ";Burg");
			newLine();
		}

		// since CR Version 51 Silber is an normal item

		/*int silver = unit.getSilver();
		 if (silver > 0) {
		 write(silver + ";Silber");
		 newLine();
		 }*/
		if(unit.isHideFaction()) {
			write("1;Parteitarnung");
			newLine();
		}

		if(shallExportUnit(unit.getFollows())) {
			write(((UnitID) unit.getFollows().getID()).intValue() + ";folgt");
			newLine();
		}

		
		
		
		if(unit.getGuard() != 0) {
			write(unit.getGuard() + ";bewacht");
			newLine();
		}

		if(unit.getAura() != -1) {
			write(unit.getAura() + ";Aura");
			newLine();
		}

		if(unit.getAuraMax() != -1) {
			write(unit.getAuraMax() + ";Auramax");
			newLine();
		}

		if(unit.getHealth() != null) {
			writeQuotedTag(unit.getHealth(), "hp");
		}

		if(unit.isHero()) {
			write("1;hero");
			newLine();
		}

		if(unit.isStarving()) {
			write("1;hunger");
			newLine();
		}

		if(!serverConformance && unit.isOrdersConfirmed()) {
			write("1;ejcOrdersConfirmed");
			newLine();
		}

		if(unit.getGroup() != null) {
			write(unit.getGroup().getID() + ";gruppe");
			newLine();
		}

		if(unit.isSpy()) {
			write("1;Verraeter");
			newLine();
		}

		if(unit.getGuiseFaction() != null) {
			// write(((IntegerID) unit.getGuiseFaction().getID()).intValue() + ";Verkleidung");
			// Anderepartei
			write(((IntegerID) unit.getGuiseFaction().getID()).intValue() + ";Anderepartei");
			newLine();
		}

		if(unit.isWeightWellKnown()) {
			write(unit.getWeight()+";weight");
			newLine();
		}
		
		//  fiete: familiarmage
		if (unit.getFamiliarmageID()!=null) {
			IntegerID iID = (IntegerID) unit.getFamiliarmageID();
			write(iID.intValue() + ";familiarmage");
			newLine();
		}
		
		
		if(unit.getRaceNamePrefix() != null) {
			writeQuotedTag(unit.getRaceNamePrefix(), "typprefix");
		}

		if(unit.hasTags()) {
			java.util.Map map = unit.getTagMap();
			java.util.Iterator it = map.keySet().iterator();

			while(it.hasNext()) {
				Object key = it.next();
				Object value = map.get(key);

				try {
					Integer.parseInt(value.toString());
					write(value + ";" + key);
					newLine();
				} catch(NumberFormatException e) {
					writeQuotedTag(value.toString(), key.toString());
				}
			}
		}

		if(includeMessages) {
			writeStringBlock("EFFECTS", unit.getEffects());
			writeMessageBlock("EINHEITSBOTSCHAFTEN", unit.getUnitMessages());
			if(!serverConformance) {
				writeStringBlock("COMMENTS", unit.getComments());
			}
		}
		

		//
		//writeOrders(unit.orders);
		//writeStringSequence(unit.getTempOrders());
		writeOrders(unit.getCompleteOrders());
		writeSkills(unit.getSkills().iterator(), unit.getPersons());
		writeUnitSpells(unit.getSpells());
		writeUnitCombatSpells(unit.getCombatSpells());
		writeItems(unit.getItems().iterator());
	}

	/**
	 * Write a region prices (PREISE) block to the underlying stream.
	 *
	 * @param map list containing the<tt>LuxuryPrice</tt> objects to be written.
	 *
	 * @throws IOException DOCUMENT-ME
	 */
	public void writePrices(Map map) throws IOException {
		if(map == null) {
			return;
		}

		Iterator i = map.values().iterator();

		if(i.hasNext()) {
			write("PREISE");
			newLine();
		}

		while(i.hasNext()) {
			write((LuxuryPrice) i.next());
		}
	}

	/**
	 * Write region block containing the luxury prices of the last turn (LETZTEPREISE) to the
	 * underlying stream.
	 *
	 * @param map a map containing the <tt>LuxuryPrice</tt> objects to be written.
	 *
	 * @throws IOException DOCUMENT-ME
	 */
	public void writeOldPrices(Map map) throws IOException {
		if(map == null) {
			return;
		}

		Iterator i = map.values().iterator();

		if(i.hasNext()) {
			write("LETZTEPREISE");
			newLine();
		}

		while(i.hasNext()) {
			write((LuxuryPrice) i.next());
		}
	}

	/**
	 * Write the cr representation of a <tt>LuxuryPrice</tt> object to the underlying stream.
	 *
	 * 
	 *
	 * @throws IOException DOCUMENT-ME
	 */
	public void write(LuxuryPrice price) throws IOException {
		write(price.getPrice() + ";" + price.getItemType().getID().toString());
		newLine();
	}

	/**
	 * Write a sequence of region border (GRENZE) blocks to the underlying stream.
	 *
	 * @param c collection containing the <tt>Border</tt> objects to be written.
	 *
	 * @throws IOException DOCUMENT-ME
	 */
	public void writeBorders(Collection c) throws IOException {
		if(c == null) {
			return;
		}

		Iterator i = c.iterator();

		while(i.hasNext()) {
			writeBorder((Border) i.next());
		}
	}

	/**
	 * Write the cr representation of a <tt>Border</tt> object to the underlying stream.
	 *
	 * 
	 *
	 * @throws IOException DOCUMENT-ME
	 */
	public void writeBorder(Border border) throws IOException {
		write("GRENZE " + border.getID());
		newLine();
		writeQuotedTag(border.getType(), "typ");
		write(border.getDirection() + ";richtung");
		newLine();
		write(border.getBuildRatio() + ";prozent");
		newLine();
	}

	/**
	 * Write a sequence of region blocks to the underlying stream.
	 *
	 * @param map a map containing the region to write. The keys are expected to be
	 * 		  <tt>Integer</tt> objects containing the ids of the regions. The values are expected
	 * 		  to be instances of class <tt>Region</tt>.
	 */
	public void writeRegions(Map<CoordinateID,Region> map) throws IOException {
		if(map == null) {
			return;
		}

		writeRegions(map.values());
	}

	/**
	 * Write a sequence of region (REGION) blocks to the underlying stream.
	 *
	 * @param regions a collection containing the regions to write.
	 */
	public void writeRegions(Collection<Region> regions) throws IOException {
		if(regions == null) {
			return;
		}
    
    if (ui != null) {
      ui.setMaximum(regions.size());
    }
    int counter = 0;

		for(Iterator<Region> iter = regions.iterator(); iter.hasNext();) {
      Region region = iter.next();
      
      if (ui != null) {
        if (region.getName() != null) {
          ui.setProgress(Resources.get("crwriterdialog.progress.07a",new Object[]{region.getName()}), counter++);
        } else {
          ui.setProgress(Resources.get("crwriterdialog.progress.07"), counter++);
        }
      }
      
			writeRegion(region);
		}
    if (ui != null) {
      ui.setMaximum(11);
    }
    if (ui != null) {
      ui.setProgress(Resources.get("crwriterdialog.progress.07"), counter++);
    }
	}

	/**
	 * Write the cr representation of a <tt>Region</tt> object to the underlying stream.
	 */
	public void writeRegion(Region region) throws IOException {
		
		// Fiete 20070117
		// Exception: Magellan-added Regions to show TheVoid
		// these regions should not be written
		if (region.getRegionType().equals(data.rules.getRegionType("Leere"))){
			return;
		}
		
		write("REGION " + region.getID().toString(" "));
		newLine();
		
		// Fiete: starting in round 570 we can have region.UID within
		// eressea, coming from the server.
		// if UID is known, write it now
		// UID=0 reserved for no UID.
		if (region.getUID()!=0){
		   // first example was quoted
		   // writeQuotedTag(region.getUID() + "", "id");
		   // finally we use not quoted IDs
		  write(region.getUID()+ ";id");
		  newLine();
		}
		
		UnitContainerType type = region.getType();

		if((region.getName() != null) && !region.getName().equals("")) {
			// write name only if it differs from type
			if(type != null) {
				String strRegion = Umlaut.normalize(region.getName());
				String strType = Umlaut.normalize(type.toString());

				if(!strRegion.equalsIgnoreCase(strType)) {
					writeQuotedTag(region.getName(), "Name");
				}
			}
		}

		if(type != null) {
			writeQuotedTag(type.getID().toString(), "Terrain");
		}

		if(region.getDescription() != null) {
			writeQuotedTag(region.getDescription(), "Beschr");
		}

		if(includeIslands && !serverConformance && (region.getIsland() != null)) {
			writeQuotedTag(region.getIsland().getID().toString(), "Insel");
		}

		if(!serverConformance && region.getData().getSelectedRegionCoordinates().containsKey(region.getID())) {
			write("1;ejcIsSelected");
			newLine();
		}

		if(region.hasTags()) {
			java.util.Map map = region.getTagMap();
			java.util.Iterator it = map.keySet().iterator();

			while(it.hasNext()) {
				Object key = it.next();
				Object value = map.get(key);

				try {
					Integer.parseInt(value.toString());
					write(value + ";" + key);
					newLine();
				} catch(NumberFormatException e) {
					writeQuotedTag(value.toString(), key.toString());
				}
			}
		}

		if(includeRegionDetails) {
			if(region.getTrees() > 0) {
				write(region.getTrees() + ";Baeume");
				newLine();
			}

			if(region.isMallorn()) {
				write("1;Mallorn");
				newLine();
			}

			if(!serverConformance && (region.getOldTrees() > -1)) {
				write(region.getOldTrees() + ";letztebaeume");
				newLine();
			}

			if(region.getSprouts() > 0) {
				write(region.getSprouts() + ";Schoesslinge");
				newLine();
			}

			if(!serverConformance && (region.getOldSprouts() > -1)) {
				write(region.getOldSprouts() + ";letzteSchoesslinge");
				newLine();
			}

			if(region.getPeasants() > 0) {
				write(region.getPeasants() + ";Bauern");
				newLine();
			}

			if(!serverConformance && (region.getOldPeasants() > -1)) {
				write(region.getOldPeasants() + ";letztebauern");
				newLine();
			}

			if(region.getHorses() > 0) {
				write(region.getHorses() + ";Pferde");
				newLine();
			}

			if(!serverConformance && (region.getOldHorses() > -1)) {
				write(region.getOldHorses() + ";letztepferde");
				newLine();
			}

			if(region.getSilver() > 0) {
				write(region.getSilver() + ";Silber");
				newLine();
			}

			if(!serverConformance && (region.getOldSilver() > -1)) {
				write(region.getOldSilver() + ";letztessilber");
				newLine();
			}

			if(region.maxEntertain() > 0) {
				write(region.maxEntertain() + ";Unterh");
				newLine();
			}

			if(region.maxRecruit() > 0) {
				write(region.maxRecruit() + ";Rekruten");
				newLine();
			}

			// pavkovic 2002.05.10: recruits (and old recruits are used from cr)
			if(!serverConformance && (region.maxOldRecruit() > -1)) {
				write(region.maxOldRecruit() + ";letzterekruten");
				newLine();
			}

			if(region.getWage() > 0) {
				if(includeBuildings) {
					write(region.getWage() + ";Lohn");
				} else {
					write("10;Lohn");
				}

				newLine();
			}

			if(includeBuildings && !serverConformance && (region.getOldWage() > -1)) {
				write(region.getOldWage() + ";letzterlohn");
				newLine();
			}

			if(region.getIron() > 0) {
				write(region.getIron() + ";Eisen");
				newLine();
			}

			if(!serverConformance && (region.getOldIron() > -1)) {
				write(region.getOldIron() + ";letzteseisen");
				newLine();
			}

			if(region.getLaen() > 0) {
				write(region.getLaen() + ";Laen");
				newLine();
			}

			if(!serverConformance && (region.getOldLaen() > -1)) {
				write(region.getOldLaen() + ";letzteslaen");
				newLine();
			}

			if(region.getStones() > 0) {
				write(region.getStones() + ";Steine");
				newLine();
			}

			if(!serverConformance && (region.getOldStones() > -1)) {
				write(region.getOldStones() + ";letztesteine");
				newLine();
			}

			if((region.getHerb() != null) && !serverConformance) {
				writeQuotedTag(region.getHerb().getID().toString(), "herb");
			}

			if((region.getHerbAmount() != null) && !serverConformance) {
				writeQuotedTag(region.getHerbAmount(), "herbamount");
			}

			if(region.isOrcInfested()) {
				write("1;Verorkt");
				newLine();
			}
      
      if(!serverConformance && region.isActive()) {
        write("1;aktiveRegion");
        newLine();
      }

			if(region.getVisibility() != null) {
				writeQuotedTag(region.getVisibility(), "visibility");
			}

			writeRegionResources(region.resources());
			writePrices(region.getPrices());

			if(!serverConformance && (region.getOldPrices() != null)) {
				writeOldPrices(region.getOldPrices());
			}
			
			if(!serverConformance && (region.getSigns()!=null)) {
				writeSigns(region.getSigns());
			}

			writeBorders(region.borders());

			if(includeMessages) {
				writeStringBlock("EFFECTS", region.getEffects());

				if(!serverConformance) {
					writeStringBlock("COMMENTS", region.getComments());
				}

				writeMessageBlock("REGIONSEREIGNISSE", region.getEvents());

				//writeMessageBlock("REGIONSKOMMENTAR", region.comments);
				writeMessageBlock("REGIONSBOTSCHAFTEN", region.getPlayerMessages());
				writeMessageBlock("UMGEBUNG", region.getSurroundings());
				writeMessageBlock("DURCHREISE", region.getTravelThru());
				writeMessageBlock("DURCHSCHIFFUNG", region.getTravelThruShips());
				writeMessages(region.getMessages());
			}
		}

		writeSchemes(region.schemes());

		if(includeBuildings) {
			writeBuildings(region.buildings());
		}

		if(includeShips) {
			writeShips(region.ships());
		}

		if(includeUnits) {
			writeUnits(region.units());
		}
	}

	/**
	 * Write a collection of signs to the underlying stream
	 *
	 * @param signs Collection of signs
	 *
	 * @throws IOException passes a IOException from streamwriter
	 */
	private void writeSigns(Collection signs) throws IOException {
		if (signs == null || signs.isEmpty()){
			return;
		}
		int counter = 1;
		for (Iterator iter = signs.iterator();iter.hasNext();){
			writeSign((Sign)iter.next(),counter);
			counter++;
		}
	}
	
	/**
	 * Write a presentation of a sign to the underlying stream
	 *
	 * @param sign the sign
	 * @param int counter  just a counter for IDing the sign
	 *
	 * @throws IOException passes a IOException from streamwriter
	 */
	private void writeSign(Sign s, int counter) throws IOException {
		write("SIGN " + counter);
		newLine();
		writeQuotedTag(s.getText(), "text");
	}
	
	/**
	 * Write a collection of schemes to the underlying stream
	 *
	 * 
	 *
	 * @throws IOException DOCUMENT-ME
	 */
	public void writeSchemes(Collection schemes) throws IOException {
		if((schemes == null) || schemes.isEmpty()) {
			return;
		}

		for(Iterator iter = schemes.iterator(); iter.hasNext();) {
			writeScheme((Scheme) iter.next());
		}
	}

	/**
	 * Writes the cr representation of a Scheme object to the underlying stream.
	 *
	 * 
	 *
	 * @throws IOException DOCUMENT-ME
	 */
	public void writeScheme(Scheme scheme) throws IOException {
		write("SCHEMEN " + scheme.getID().toString(" "));
		newLine();

		if(scheme.getName() != null) {
			writeQuotedTag(scheme.getName(), "Name");
		}
	}

	/**
	 * Write a collection of region resources to the underlying stream
	 *
	 * 
	 *
	 * @throws IOException DOCUMENT-ME
	 */
	public void writeRegionResources(Collection resources) throws IOException {
		if((resources == null) || resources.isEmpty()) {
			return;
		}

		for(Iterator iter = resources.iterator(); iter.hasNext();) {
			writeRegionResource((RegionResource) iter.next());
		}
	}

	/**
	 * Writes the cr representation of a region resource object to the underlying stream.
	 *
	 * 
	 *
	 * @throws IOException DOCUMENT-ME
	 */
	public void writeRegionResource(RegionResource res) throws IOException {
		write("RESOURCE " + res.getID().toString());
		newLine();
		writeQuotedTag(res.getType().getID().toString(), "type");

		if(res.getAmount() > -1) {
			write(res.getAmount() + ";number");
			newLine();
		}

		if(res.getSkillLevel() > -1) {
			write(res.getSkillLevel() + ";skill");
			newLine();
		}
    
    if (res.getDate()!=null && res.getDate().getDate()>-1 && !serverConformance){
      write(res.getDate().getDate() + ";Runde");
      newLine();
    }
    
	}

	/**
	 * Write message type blocks to the underlying stream.
	 *
	 * @param map a map containing the <tt>MessageType</tt> objects to be written as values.
	 *
	 * @throws IOException DOCUMENT-ME
	 */
	public void writeMsgTypes(Map map) throws IOException {
		if(map == null) {
			return;
		}

		for(Iterator iter = map.values().iterator(); iter.hasNext();) {
			writeMessageType((MessageType) iter.next());
		}
	}

	/**
	 * Write the cr representation of a <tt>MessageType</tt> object to the underlying stream.
	 *
	 * 
	 *
	 * @throws IOException DOCUMENT-ME
	 */
	public void writeMessageType(MessageType msgType) throws IOException {
		if(msgType == null) {
			CRWriter.log.warn("CRWriter.writeMessageType(): argument msgType is null");

			return;
		}

		if((msgType.getID() == null) || (((IntegerID) msgType.getID()).intValue() < 0)) {
			CRWriter.log.warn("CRWriter.writeMessageType(): invalid ID");

			return;
		}

		if(msgType.getPattern() == null) {
			CRWriter.log.warn("CRWriter.writeMessageType(): pattern of message type " + msgType.getID() +
					 " is null");

			return;
		}

		write("MESSAGETYPE " + msgType.getID().toString());
		newLine();
		writeQuotedTag(msgType.getPattern(), "text");

		if(msgType.getSection() != null) {
			writeQuotedTag(msgType.getSection(), "section");
		}
	}

	/**
	 * Write the complete game data from <tt>world</tt> in the cr format.
	 *
	 * @param world the game data to write.
	 */
	public Thread write(final GameData world) throws IOException, NullPointerException {
		if(world == null) {
			throw new NullPointerException("CRWriter.write(GameData): argument world is null");
		}
    this.data = world;
    savingInProgress = true;
    
    Thread t = null;
    
    if (ui != null && !(ui instanceof NullUserInterface)) {
      t = new Thread(new Runnable() {
        public void run() {
          try {
            // Bug #117: make sure that savingInProgress is true, until the
            // writer is closed or writer could remain open in multi-threaded
            // execution.
            writeThread(world);
            close(true);
            savingInProgress=false;
          } catch (Exception exception) {
            CRWriter.log.error(exception);
            ui.showException(Resources.get("crwriterdialog.exception"), null, exception);
          }
        }
      });
      t.start();
    } else {
      writeThread(world);
      close(true);
      savingInProgress=false;
    }
    
    return t;
  }
  
  /**
   * Close, even if saving in progress.
   * 
   * @param b
   * @throws IOException 
   */
  private void close(boolean b) throws IOException {
    super.close();
  }

  /**
   * @see java.io.BufferedWriter#close()
   */
  @Override
  public void close() throws IOException {
    if (savingInProgress) {
      return;
    }
    super.close();
  }
  
  /**
   * Write the complete game data from <tt>world</tt> in the cr format.
   * This method is called by the public method write(GameData). This
   * method can be run in a thread.
   *
   * @param world the game data to write.
   */
  protected void writeThread(GameData world) throws IOException, NullPointerException {
    
    CRWriter.log.info("Saving report. Encoding: " + encoding);
    if (!encoding.equalsIgnoreCase(world.getEncoding())){
      CRWriter.log.warn("Encodings differ while writing CR: writer users " + encoding + ", gamadata is set to " + world.getEncoding() + ", setting charset to:" + world.getEncoding());
      this.encoding = world.getEncoding();
    }
    if (ui != null) {
      ui.setMaximum(11);
    }
    if (ui != null) {
      ui.setTitle(Resources.get("crwriterdialog.progress.title"));
    }
    if (ui != null) {
      ui.show();
    }
    
    if (ui != null) {
      ui.setProgress(Resources.get("crwriterdialog.progress.01"), 1);
    }
		writeVersion(world);

    if(!serverConformance) {
      writeCoordinateTranslations(world);
    }

		if(!serverConformance && exportHotspots) {
      if (ui != null) {
        ui.setProgress(Resources.get("crwriterdialog.progress.02"), 2);
      }
			writeHotSpots(world.hotSpots());
		}

    // this assumes that if somebody doesn't write units
    // also factions aren't necessary; maybe this needs further
    // specification
    if(includeUnits) {
      if (ui != null) {
        ui.setProgress(Resources.get("crwriterdialog.progress.03"), 3);
      }
      writeFactions(world.factions());
    }
    
		if(includeSpellsAndPotions) {
      if (ui != null) {
        ui.setProgress(Resources.get("crwriterdialog.progress.04"), 4);
      }
			writeSpells(world.spells());
      
      if (ui != null) {
        ui.setProgress(Resources.get("crwriterdialog.progress.05"), 5);
      }
			writePotions(world.potions());
		}

		if(!serverConformance && includeIslands) {
      if (ui != null) {
        ui.setProgress(Resources.get("crwriterdialog.progress.06"), 6);
      }
			writeIslands(world.islands());
		}

		if(includeRegions) {
      if (ui != null) {
        ui.setProgress(Resources.get("crwriterdialog.progress.07"), 7);
      }
			if((regions != null) && (regions.size() > 0)) {
				writeRegions(regions);
			} else {
				writeRegions(world.regions());
			}
		}

		if(includeMessages) {
      if (ui != null) {
        ui.setProgress(Resources.get("crwriterdialog.progress.08"), 8);
      }
			writeMsgTypes(world.msgTypes());
		}

    if (ui != null) {
      ui.setProgress(Resources.get("crwriterdialog.progress.09"), 9);
    }
		writeTranslations(world.translations());

		if(includeRegions && includeUnits && ((regions == null) || (regions.size() == 0))) {
      if (ui != null) {
        ui.setProgress(Resources.get("crwriterdialog.progress.10"), 10);
      }
			if(world.units() != null) {
				if(world.units().size() != unitsWritten) {
					int homelessUnitsCounter = 0;

					for(Iterator iter = world.units().values().iterator(); iter.hasNext();) {
						Unit u = (Unit) iter.next();

						if(u.getRegion() == null) {
							homelessUnitsCounter++;
						}
					}

					if((world.units().size() - homelessUnitsCounter) != unitsWritten) {
						throw new IOException("Although there are " +
											  (world.units().size() - homelessUnitsCounter) +
											  " units, only " + unitsWritten + " were written!");
					}
				}
			}
		}

    if (ui != null) {
      ui.setProgress(Resources.get("crwriterdialog.progress.11"), 11);
    }
    
    if (ui != null) {
      ui.ready();
    }
    CRWriter.log.info("Done saving report");
	}
  
  public boolean savingInProgress() {
    return savingInProgress;
  }

	/**
	 * Change the quote escape behaviour of this CRWriter. Tilde escapes look like: 'a "b c"' -> 
     * 'a b~c', whereas a backslash escape works like this: 'a "b c"' -> 'a \"b c\"'
	 *
	 * @param bool if <tt>true</tt>, escape quoted parts of any string written to the underlying
	 * 		  stream with tildes. If <tt>false</tt>, use backslash character to escape the
	 * 		  quotation marks themselves.
	 */
	public void setTildeEscapes(boolean bool) {
		useTildesForQuotes = true;
	}

	private boolean includeRegions = true;

	/**
	 * Returns whether <tt>write(GameData data)</tt> writes information about the regions in data
	 * to the underlying stream.
	 *
	 * 
	 */
	public boolean getIncludeRegions() {
		return this.includeRegions;
	}

	/**
	 * Toggles whether <tt>write(GameData data)</tt> writes information about the regions in data
	 * to the underlying stream.
	 *
	 * 
	 */
	public void setIncludeRegions(boolean includeRegions) {
		this.includeRegions = includeRegions;
	}

	private boolean includeBuildings = true;

	/**
	 * Returns whether <tt>write(GameData data)</tt> writes information about the buildings in data
	 * to the underlying stream.
	 *
	 * 
	 */
	public boolean getIncludeBuildings() {
		return this.includeBuildings;
	}

	/**
	 * Toggles whether <tt>write(GameData data)</tt> writes information about the buildings in data
	 * to the underlying stream.
	 *
	 * 
	 */
	public void setIncludeBuildings(boolean includeBuildings) {
		this.includeBuildings = includeBuildings;
	}

	private boolean includeShips = true;

	/**
	 * Returns whether <tt>write(GameData data)</tt> writes information about the ships in data to
	 * the underlying stream.
	 *
	 * 
	 */
	public boolean getIncludeShips() {
		return this.includeShips;
	}

	/**
	 * Toggles whether <tt>write(GameData data)</tt> writes information about the ships in data to
	 * the underlying stream.
	 *
	 * 
	 */
	public void setIncludeShips(boolean includeShips) {
		this.includeShips = includeShips;
	}

	private boolean includeUnits = true;

	/**
	 * Returns whether <tt>write(GameData data)</tt> writes information about the units in data to
	 * the underlying stream.
	 *
	 * 
	 */
	public boolean getIncludeUnits() {
		return this.includeUnits;
	}

	/**
	 * Toggles whether <tt>write(GameData data)</tt> writes information about the units in data to
	 * the underlying stream.
	 *
	 * 
	 */
	public void setIncludeUnits(boolean includeUnits) {
		this.includeUnits = includeUnits;
	}

	private boolean includeRegionDetails = true;

	/**
	 * Returns whether <tt>write(GameData data)</tt> writes detailed information about the regions
	 * in data to the underlying stream.
	 *
	 * 
	 */
	public boolean getIncludeRegionDetails() {
		return this.includeRegionDetails;
	}

	/**
	 * Toggles whether <tt>write(GameData data)</tt> writes detailed information about the regions
	 * in data to the underlying stream.
	 *
	 * 
	 */
	public void setIncludeRegionDetails(boolean includeRegionDetails) {
		this.includeRegionDetails = includeRegionDetails;
	}

	private boolean includeIslands = true;

	/**
	 * Returns whether <tt>write(GameData data)</tt> writes information about islands to the
	 * underlying stream.
	 *
	 * 
	 */
	public boolean getIncludeIslands() {
		return this.includeIslands;
	}

	/**
	 * Toggles whether <tt>write(GameData data)</tt> writes information about islands to the
	 * underlying stream.
	 *
	 * 
	 */
	public void setIncludeIslands(boolean includeIslands) {
		this.includeIslands = includeIslands;
	}

	private boolean includeMessages = true;

	/**
	 * Returns whether <tt>write(GameData data)</tt> writes messages contained in the game data to
	 * the underlying stream.
	 *
	 * 
	 */
	public boolean getIncludeMessages() {
		return this.includeMessages;
	}

	/**
	 * Toggles whether <tt>write(GameData data)</tt> writes messages contained in the game data to
	 * the underlying stream.
	 *
	 * 
	 */
	public void setIncludeMessages(boolean includeMessages) {
		this.includeMessages = includeMessages;
	}

  
  private boolean exportHotspots = true;
  
  /**
   * Returns whether <tt>write(GameData data)</tt> writes Hotspots contained in the game data to
   * the underlying stream.
   *
   * 
   */
  public boolean getExportHotspots() {
    return this.exportHotspots;
  }

  /**
   * Toggles whether <tt>write(GameData data)</tt> writes Hotspots contained in the game data to
   * the underlying stream.
   *
   * 
   */
  public void setExportHotspots(boolean exportHotspots) {
    this.exportHotspots = exportHotspots;
  }
  
  
  
	private boolean includeSpellsAndPotions = true;

	/**
	 * Returns whether <tt>write(GameData data)</tt> writes messages contained in the game data to
	 * the underlying stream.
	 *
	 * 
	 */
	public boolean getIncludeSpellsAndPotions() {
		return this.includeSpellsAndPotions;
	}

	/**
	 * Toggles whether <tt>write(GameData data)</tt> writes messages contained in the game data to
	 * the underlying stream.
	 *
	 * 
	 */
	public void setIncludeSpellsAndPotions(boolean includeSpellsAndPotions) {
		this.includeSpellsAndPotions = includeSpellsAndPotions;
	}

	private boolean serverConformance = false;

	/**
	 * Returns whether <tt>write(GameData data)</tt> writes a cr that is compatible with cr's
	 * generated by the Eressea server, i.e. not including JavaClient specific data.
	 *
	 * 
	 */
	public boolean getServerConformance() {
		return this.serverConformance;
	}

	/**
	 * Toggles whether <tt>write(GameData data)</tt> writes a cr that is compatible with cr's
	 * generated by the Eressea server, i.e. not including JavaClient specific data.
	 *
	 * 
	 */
	public void setServerConformance(boolean serverConformance) {
		this.serverConformance = serverConformance;
	}

	/**
	 * Write a sequence of island blocks to the underlying stream.
	 *
	 * @param map a map containing the islands to write. The keys are expected to be
	 * 		  <tt>Integer</tt> objects containing the ids of the islands. The values are expected
	 * 		  to be instances of class <tt>Island</tt>.
	 *
	 * @throws IOException DOCUMENT-ME
	 */
	public void writeIslands(Map map) throws IOException {
		if(map == null) {
			return;
		}

		for(Iterator iter = map.values().iterator(); iter.hasNext();) {
			write((Island) iter.next());
		}
	}

	/**
	 * Write the cr representation of an <tt>Island</tt> object to the underlying stream.
	 *
	 * 
	 *
	 * @throws IOException DOCUMENT-ME
	 */
	public void write(Island island) throws IOException {
		if(island == null) {
			return;
		}

		write("ISLAND " + island.getID());
		newLine();

		if(island.getName() != null) {
			writeQuotedTag(island.getName(), "name");
		}

		if(island.getDescription() != null) {
			writeQuotedTag(island.getDescription(), "Beschr");
		}
	}

	/**
	 * Write a sequence of hot spot blocks to the underlying stream.
	 *
	 * 
	 *
	 * @throws IOException DOCUMENT-ME
	 */
	public void writeHotSpots(Map hotSpots) throws IOException {
		if(hotSpots == null) {
			return;
		}

		for(Iterator iter = hotSpots.values().iterator(); iter.hasNext();) {
			write((HotSpot) iter.next());
		}
	}

	/**
	 * Write the cr representation of a hot spot to the underlying stream.
	 *
	 * 
	 *
	 * @throws IOException DOCUMENT-ME
	 */
	public void write(HotSpot h) throws IOException {
		if(h == null) {
			return;
		}

		write("HOTSPOT " + h.getID().toString(" "));
		newLine();
		writeQuotedTag(h.getName(), "name");
		writeQuotedTag(h.getCenter().toString(" "), "coord");
	}

	private Collection<Region> regions = null;

	/**
	 * Returns the regions this object writes to the underlying stream.
	 *
	 * 
	 */
	public Collection<Region> getRegions() {
		return this.regions;
	}

	/**
	 * Supply the writer with a collection of regions it should write to the underlying stream
	 * instead of all regions contained in the game data. If regions is null or if there is no
	 * element in the supplied collection, the writer returns to writing all regions defined in
	 * the game data.
	 *
	 * 
	 */
	public void setRegions(Collection<Region> regions) {
		this.regions = regions;
	}


	private Collection<Unit> units = null;

	/**
	 * Returns the units this object writes to the underlying stream.
	 *
	 * 
	 */
	public Collection<Unit> getUnits() {
		return this.units;
	}

	/**
	 * Supply the writer with a collection of units it should write to the underlying stream
	 * instead of all units contained in the game data. If units is null or if there is no
	 * element in the supplied collection, the writer returns to writing all units defined in
	 * the game data.
	 *
	 * 
	 */
	public void setUnits(Collection<Unit> units) {
		this.units = units;
	}

	/**
	 * Write the translation table to underlying stream.
	 *
	 * 
	 *
	 * @throws IOException DOCUMENT-ME
	 */
	public void writeTranslations(Translations m) throws IOException {
		if((m == null) || (m.size() == 0)) {
			return;
		}

		write("TRANSLATION");
		newLine();
		
		for (Iterator<String> iter = m.getKeyTreeSet().iterator();iter.hasNext();){
		  String key = iter.next();
		  String value = m.getTranslation(key, TranslationType.sourceCR);
		  if (value!=null){
		    writeQuotedTag(value, key);
		  }
		}
		
		
		/*
		List<String> sorted = new ArrayList<String>(m.keySet());
		Collections.sort(sorted);

		for(Iterator<String> iter = sorted.iterator(); iter.hasNext();) {
			String key = iter.next();
			String value = m.get(key);
			writeQuotedTag(value, key);
		}
		*/
	}
}
