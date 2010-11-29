/**
 * 
 */
package magellan.plugin.extendedcommands.scripts;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.File;
import java.util.Iterator;
import java.util.Locale;
import java.util.Properties;

import magellan.client.Client;
import magellan.client.ClientProvider;
import magellan.client.MagellanContext;
import magellan.client.event.EventDispatcher;
import magellan.library.Faction;
import magellan.library.GameData;
import magellan.library.Item;
import magellan.library.MissingData;
import magellan.library.Skill;
import magellan.library.Unit;
import magellan.library.utils.Resources;
import magellan.library.utils.logging.Logger;
import magellan.plugin.extendedcommands.ExtendedCommandsProvider;
import magellan.test.GameDataBuilder;

import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * @author stm
 */
public class E3CommandParserTest {

  private static MagellanContext context;
  // private static Properties completionSettings;
  private static Properties settings;
  private static Client client;
  private static GameDataBuilder builder;

  /**
   * @throws java.lang.Exception
   */
  @BeforeClass
  public static void setUpBeforeClass() throws Exception {
    Logger.setLevel(Logger.WARN);
    Logger.getInstance(E3CommandParserTest.class).info(new File(".").getAbsolutePath());
    settings = new Properties(); // Client.loadSettings(PARSER_SETTINGS_DIRECTORY,
    // PARSER_SETTINGS_FILE);
    Resources.getInstance().initialize(new File("../Magellan2-current"), "");
    context = new MagellanContext(null);
    context.setProperties(settings);
    context.setEventDispatcher(new EventDispatcher());

    context.init();
    data = new MissingData();
    client = ClientProvider.getClient(data);
    data = new GameDataBuilder().createSimplestGameData();
    client.setData(data);
    builder = new GameDataBuilder();
  }

  private static GameData data;
  private static E3CommandParser parser;
  private Unit unit;

  // private ExtendedCommandsHelper helper;

  /**
   * @throws java.lang.Exception
   */
  @Before
  public void setUp() throws Exception {
    data = builder.createSimpleGameData(350);
    unit = data.getUnits().iterator().next();
    unit.getFaction().setLocale(Locale.GERMAN);
    client.setData(data);
    parser = new E3CommandParser(data, // helper =
        ExtendedCommandsProvider.createHelper(client, data, null, null));
  }

  @SuppressWarnings("deprecation")
  protected boolean containsOrder(Unit unit2, String string) {
    return unit2.getOrders().contains(string);
  }

  /**
   * Test method for {@link E3CommandParser#getItemType(java.lang.String)}.
   * 
   * @throws Exception
   */
  @Test
  public final void testGetItemType() throws Exception {
    Assert.assertEquals("Silber", E3CommandParser.getItemType("Silber").getName());
  }

  /**
   * Test method for {@link E3CommandParser#getSkillType(java.lang.String)}.
   */
  @Test
  public final void testGetSkillType() {
    Assert.assertEquals("Hiebwaffen", E3CommandParser.getSkillType("Hiebwaffen").getName());
  }

  /**
   * Test method for {@link E3CommandParser#setConfirm(magellan.library.Unit, boolean)} .
   */
  @Test
  public final void testSetConfirm() {
    assertEquals(null, unit.getTag("$cript.confirm"));
    E3CommandParser.setConfirm(unit, true);
    assertEquals("1", unit.getTag("$cript.confirm"));
    E3CommandParser.setConfirm(unit, false);
    assertEquals("0", unit.getTag("$cript.confirm"));
    E3CommandParser.setConfirm(unit, true);
    assertEquals("0", unit.getTag("$cript.confirm"));
  }

  /**
   * Test method for
   * {@link E3CommandParser#setProperty(magellan.library.Unit, java.lang.String, java.lang.String)}
   * .
   */
  @Test
  public final void testSetProperty() {
    assertEquals(null, unit.getTag("$cript.bla"));
    E3CommandParser.setProperty(unit, "bla", "foo");
    assertEquals("foo", unit.getTag("$cript.bla"));
  }

  /**
   * Test method for {@link E3CommandParser#getProperty(magellan.library.Unit, java.lang.String)} .
   */
  @Test
  public final void testGetProperty() {
    assertEquals("", E3CommandParser.getProperty(unit, "bla"));
    E3CommandParser.setProperty(unit, "bla", "foo");
    assertEquals("foo", E3CommandParser.getProperty(unit, "bla"));
  }

  /**
   * Test method for {@link E3CommandParser#execute(magellan.library.Faction)}.
   */
  @Test
  public final void testExecute() {
    try {
      parser.execute((Faction) null);
      fail("argument null not allowed");
    } catch (NullPointerException e) {
      // okay
    }
    parser.execute(unit.getFaction());

  }

  /**
   * Test method for {@link E3CommandParser#addGiveOrder(Unit, Unit, String, int, boolean)} .
   */
  @Test
  public final void testAddGiveOrder() {
    assertFalse(containsOrder(unit, "GIB 1 5 Silber"));
    unit.clearOrders();
    E3CommandParser.addGiveOrder(unit, unit, "Silber", 5, false);
    assertEquals("GIB 1 5 Silber", unit.getOrders2().get(0).getText());

    unit.clearOrders();
    E3CommandParser.addGiveOrder(unit, unit, "Silber", 5, true);
    assertEquals("GIB 1 JE 5 Silber", unit.getOrders2().get(0).getText());

    unit.clearOrders();
    try {
      E3CommandParser.addGiveOrder(unit, null, "Silber", 5, false);
      fail("should throw NPE");
    } catch (NullPointerException e) {
      // okay
    }
  }

  /**
   * Test method for
   * {@link E3CommandParser#addReserveOrder(magellan.library.Unit, java.lang.String, int, boolean)}
   * .
   */
  @Test
  public final void testAddReserveOrder() {
    assertFalse(containsOrder(unit, "RESERVIEREN 5 Silber"));
    unit.clearOrders();
    E3CommandParser.addReserveOrder(unit, "Silber", 5, false);
    assertEquals("RESERVIEREN 5 Silber", unit.getOrders2().get(0).getText());
  }

  /**
   * Test method for the auto command.
   */
  @Test
  public final void testCommandAuto() {
    unit.clearOrders();
    unit.addOrder("// $cript auto");
    unit.addOrder("LERNEN Hiebwaffen");

    parser.execute(unit.getFaction());

    assertEquals(3, unit.getOrders2().size());
    assertEquals("// $cript auto", unit.getOrders2().get(1).getText());
    assertEquals("LERNEN Hiebwaffen", unit.getOrders2().get(2).getText());
    assertTrue(unit.isOrdersConfirmed());

    unit.clearOrders();
    unit.addOrder("LERNEN Hiebwaffen");
    unit.addOrder("// $cript auto");

    parser.execute(unit.getFaction());

    assertEquals(3, unit.getOrders2().size());
    assertEquals("LERNEN Hiebwaffen", unit.getOrders2().get(1).getText());
    assertEquals("// $cript auto", unit.getOrders2().get(2).getText());
    assertTrue(unit.isOrdersConfirmed());

    // test auto nicht
    unit.clearOrders();
    unit.addOrder("// $cript   auto   nicht");
    unit.addOrder("LERNEN Hiebwaffen");

    parser.execute(unit.getFaction());

    assertEquals(3, unit.getOrders2().size());
    assertEquals("// $cript   auto   nicht", unit.getOrders2().get(1).getText());
    assertEquals("LERNEN Hiebwaffen", unit.getOrders2().get(2).getText());
    assertFalse(unit.isOrdersConfirmed());
  }

  /**
   * Test method for {@link E3CommandParser#commandBenoetige(String...)}.
   */
  @Test
  public final void testCommandBenoetige() {
    // add other unit with Silber
    Unit unit2 = builder.addUnit(data, "v", "Versorger", unit.getFaction(), unit.getRegion());
    builder.addItem(data, unit2, "Silber", 5);

    // test one Benoetige order
    unit.clearOrders();
    unit.addOrder("// $cript Benoetige 4 Silber");
    parser.execute(unit.getFaction());

    assertEquals(2, unit.getOrders2().size());
    assertEquals("GIB 1 4 Silber", unit2.getOrders2().get(1).getText());

    // test one Benoetige order with JE
    unit.clearOrders();
    unit2.clearOrders();
    unit.setPersons(2);
    unit.addOrder("// $cript Benoetige JE 2 Silber");
    parser.execute(unit.getFaction());

    assertEquals(2, unit.getOrders2().size());
    assertEquals(1, unit2.getOrders2().size());
    assertEquals("GIB 1 4 Silber", unit2.getOrders2().get(0).getText());

    // test if unit's own items are RESERVED first
    unit.clearOrders();
    unit2.clearOrders();

    builder.addItem(data, unit, "Silber", 2);
    unit.addOrder("// $cript Benoetige 4 Silber");

    parser.execute(unit.getFaction());
    assertEquals("RESERVIEREN 2 Silber", unit.getOrders2().get(2).getText());
    assertEquals("GIB 1 2 Silber", unit2.getOrders2().get(0).getText());
    assertEquals(3, unit.getOrders2().size());
    assertEquals(1, unit2.getOrders2().size());
    assertEquals("RESERVIEREN 2 Silber", unit.getOrders2().get(2).getText());
    assertEquals("GIB 1 2 Silber", unit2.getOrders2().get(0).getText());

    // test what happens if supplyer unit already has a RESERVE order (nothing should change)
    unit.clearOrders();
    unit2.clearOrders();

    unit.addOrder("// $cript Benoetige 4 Silber");
    unit2.addOrder("RESERVIEREN 4 Silber");
    unit2.getRegion().refreshUnitRelations(true);

    parser.execute(unit.getFaction());
    assertEquals("RESERVIEREN 2 Silber", unit.getOrders2().get(2).getText());
    assertFalse(containsOrder(unit, "; TODO: needs 1 more Silber"));
    assertEquals("RESERVIEREN 4 Silber", unit2.getOrders2().get(0).getText());
    assertEquals("GIB 1 2 Silber", unit2.getOrders2().get(1).getText());

    // test what happens if Benoetige unit already has a RESERVE order (nothing should change)
    unit.clearOrders();
    unit2.clearOrders();

    unit.addOrder("// $cript Benoetige 4 Silber");
    unit.addOrder("RESERVIEREN 1 Silber");

    parser.execute(unit.getFaction());
    assertEquals("RESERVIEREN 1 Silber", unit.getOrders2().get(2).getText());
    assertEquals("GIB 1 2 Silber", unit2.getOrders2().get(0).getText());

    // supplyer now also has Benoetige order and cannot satisfy demand
    unit.clearOrders();
    unit2.clearOrders();

    unit.addOrder("// $cript Benoetige 4 Silber");
    unit2.addOrder("// $cript Benoetige 4 Silber");

    parser.execute(unit.getFaction());
    assertEquals("RESERVIEREN 2 Silber", unit.getOrders2().get(2).getText());
    assertEquals("; TODO: needs 1 more Silber", unit.getOrders2().get(4).getText());
    assertEquals("RESERVIEREN 4 Silber", unit2.getOrders2().get(1).getText());
    assertEquals("GIB 1 1 Silber", unit2.getOrders2().get(2).getText());

    // test Benoetige ALLES
    unit.clearOrders();
    unit2.clearOrders();

    unit.addOrder("// $cript Benoetige ALLES Silber");
    unit2.addOrder("// $cript Benoetige 1 2 Silber");

    parser.execute(unit.getFaction());
    assertEquals("RESERVIEREN 2 Silber", unit.getOrders2().get(2).getText());
    assertEquals("RESERVIEREN 1 Silber", unit2.getOrders2().get(1).getText());
    assertEquals("RESERVIEREN 1 Silber", unit2.getOrders2().get(2).getText());
    assertEquals("GIB 1 3 Silber", unit2.getOrders2().get(3).getText());
  }

  /**
   * Test method for {@link E3CommandParser#commandBenoetige(String...)}.
   */
  @Test
  public final void testCommandBenoetige2() {
    // test to ensure that a third unit doesn't change anything
    // add other unit with Silber
    Unit unit2 = builder.addUnit(data, "v", "Versorger", unit.getFaction(), unit.getRegion());
    builder.addItem(data, unit2, "Silber", 5);
    Unit unit3 = builder.addUnit(data, "st", "Störer", unit.getFaction(), unit.getRegion());
    builder.addItem(data, unit3, "Silber", 1);

    // test one Benoetige order
    unit.clearOrders();
    unit.addOrder("// $cript Benoetige 4 Silber");
    unit3.addOrder("// $cript Benoetige 1 Silber");
    parser.execute(unit.getFaction());

    assertEquals(2, unit.getOrders2().size());
    assertEquals("GIB 1 4 Silber", unit2.getOrders2().get(1).getText());
  }

  /**
   * Test method for {@link E3CommandParser#commandBenoetige(String...)}.
   */
  @Test
  public final void testCommandBenoetige3() {
    // test to ensure that a third unit doesn't change anything
    Unit unit3 = builder.addUnit(data, "st", "Störer", unit.getFaction(), unit.getRegion());
    builder.addItem(data, unit3, "Silber", 1);
    // add other unit with Silber
    Unit unit2 = builder.addUnit(data, "v", "Versorger", unit.getFaction(), unit.getRegion());
    builder.addItem(data, unit2, "Silber", 5);

    // test one Benoetige order
    unit.clearOrders();
    unit.addOrder("// $cript Benoetige 4 Silber");
    unit3.addOrder("// $cript Benoetige 1 Silber");
    parser.execute(unit.getFaction());

    assertEquals(2, unit.getOrders2().size());
    assertEquals("GIB 1 4 Silber", unit2.getOrders2().get(1).getText());
  }

  /**
   * Test method for {@link E3CommandParser#commandGibWenn(String[])}.
   */
  @Test
  public final void testCommandGibWenn2() {
    // add other unit with Silber
    Unit unit2 = builder.addUnit(data, "v", "Versorger", unit.getFaction(), unit.getRegion());
    builder.addItem(data, unit2, "Silber", 5);

    // test one GibWenn order
    unit.clearOrders();
    unit2.clearOrders();

    unit2.addOrder("// $cript GibWenn qwra 120 Silber Menge");
    parser.execute(unit.getFaction());

    assertEquals(1, unit.getOrders2().size());
    assertEquals(3, unit2.getOrders2().size());
  }

  /**
   * Test method for {@link E3CommandParser#commandGibWenn(String[])}.
   */
  @Test
  public final void testCommandGibWenn3() {
    // add other unit with Kraut
    Unit unit2 = builder.addUnit(data, "v", "Versorger", unit.getFaction(), unit.getRegion());
    builder.addItem(data, unit2, "Würziger Wagemut", 5);
    builder.addItem(data, unit2, "Flachwurz", 5);
    builder.addItem(data, unit2, "Myrrhe", 5);
    builder.addItem(data, unit2, "Seide", 5);

    // test one GibWenn order
    unit.clearOrders();
    unit2.clearOrders();
    unit2.addOrder("// $cript GibWenn 1 KRAUT");
    unit2.addOrder("// $cript GibWenn 1 LUXUS");

    parser.execute(unit.getFaction());
    assertEquals(6, unit2.getOrders2().size());
    assertEquals("GIB 1 ALLES Würziger~Wagemut", unit2.getOrders2().get(1).getText());
    assertEquals("GIB 1 ALLES Flachwurz", unit2.getOrders2().get(2).getText());
    assertEquals("GIB 1 ALLES Myrrhe", unit2.getOrders2().get(4).getText());
    assertEquals("GIB 1 ALLES Seide", unit2.getOrders2().get(5).getText());
  }

  /**
   * Test method for {@link E3CommandParser#commandGibWenn(String[])}.
   */
  @Test
  public final void testCommandGibWenn() {
    // add other unit with Silber
    Unit unit2 = builder.addUnit(data, "v", "Versorger", unit.getFaction(), unit.getRegion());
    builder.addItem(data, unit2, "Silber", 5);

    // test one GibWenn order
    unit.clearOrders();
    unit2.clearOrders();

    unit2.addOrder("// $cript GibWenn 1 3 Silber");
    parser.execute(unit.getFaction());

    assertEquals(2, unit2.getOrders2().size());
    assertEquals("GIB 1 3 Silber", unit2.getOrders2().get(1).getText());

    // test receiver unit not there
    unit.clearOrders();
    unit2.clearOrders();

    unit2.addOrder("// $cript GibWenn 2 3 Silber");
    parser.execute(unit.getFaction());

    assertEquals(2, unit2.getOrders2().size());
    assertEquals("// $cript GibWenn 2 3 Silber", unit2.getOrders2().get(0).getText());
    assertEquals("; TODO: Fehler im Skript in Zeile 1: 2 nicht da", unit2.getOrders2().get(1)
        .getText());//
    // assertEquals("GIB 2 3 Silber", unit2.getOrders2().get(2).getText());

    // test receiver unit not there (with warning)
    unit.clearOrders();
    unit2.clearOrders();

    unit2.addOrder("// $cript GibWenn 2 3 Silber immer");
    parser.execute(unit.getFaction());

    assertEquals(2, unit2.getOrders2().size());
    assertEquals("; TODO: Fehler im Skript in Zeile 1: 2 nicht da", unit2.getOrders2().get(1)
        .getText());
    // assertEquals("GIB 2 3 Silber", unit2.getOrders2().get(2).getText());

    // test receiver unit not there (without warning)
    unit.clearOrders();
    unit2.clearOrders();

    unit2.addOrder("// $cript GibWenn 2 3 Silber Menge");
    parser.execute(unit.getFaction());

    assertEquals(2, unit2.getOrders2().size());
    assertEquals("; 2 nicht da", unit2.getOrders2().get(1).getText());
    // assertEquals("GIB 2 3 Silber", unit2.getOrders2().get(2).getText());

    // test receiver unit not there (with warning)
    unit.clearOrders();
    unit2.clearOrders();

    unit2.addOrder("// $cript GibWenn 2 3 Silber Einheit");
    parser.execute(unit.getFaction());

    assertEquals(2, unit2.getOrders2().size());
    assertEquals("; TODO: Fehler im Skript in Zeile 1: 2 nicht da", unit2.getOrders2().get(1)
        .getText());
    // assertEquals("GIB 2 3 Silber", unit2.getOrders2().get(2).getText());

    // test receiver unit not there (with hidden unit warning)
    unit.clearOrders();
    unit2.clearOrders();

    unit2.addOrder("// $cript GibWenn 2 3 Silber versteckt");
    parser.execute(unit.getFaction());

    assertEquals(3, unit2.getOrders2().size());
    assertEquals("; 2 nicht da", unit2.getOrders2().get(1).getText());
    assertEquals("GIB 2 3 Silber", unit2.getOrders2().get(2).getText());

    // test receiver unit not there (without warning)
    unit.clearOrders();
    unit2.clearOrders();

    unit2.addOrder("// $cript GibWenn 2 3 Silber nie");
    parser.execute(unit.getFaction());

    assertEquals(2, unit2.getOrders2().size());
    assertEquals("; 2 nicht da", unit2.getOrders2().get(1).getText());
    // assertEquals("GIB 2 3 Silber", unit2.getOrders2().get(2).getText());

    // test receiver unit not there (with nonsense warning)
    unit.clearOrders();
    unit2.clearOrders();

    unit2.addOrder("// $cript GibWenn 2 3 Silber bla");
    parser.execute(unit.getFaction());

    assertEquals(3, unit2.getOrders2().size());
    assertEquals("; TODO: Fehler im Skript in Zeile 1: zu viele Parameter", unit2.getOrders2().get(
        1).getText());

    assertEquals("; TODO: Fehler im Skript in Zeile 1: 2 nicht da", unit2.getOrders2().get(2)
        .getText());
    // assertEquals("GIB 2 3 Silber", unit2.getOrders2().get(3).getText());

    // test receiver unit not there (with nonsense warning)
    unit.clearOrders();
    unit2.clearOrders();

    unit2.addOrder("// $cript GibWenn 1 3 Silber bla");
    parser.execute(unit.getFaction());

    assertEquals(3, unit2.getOrders2().size());
    assertEquals("; TODO: Fehler im Skript in Zeile 1: zu viele Parameter", unit2.getOrders2().get(
        1).getText());
    assertEquals("GIB 1 3 Silber", unit2.getOrders2().get(2).getText());

    // test supplyer does not have item
    unit.clearOrders();
    unit2.clearOrders();

    unit2.addOrder("// $cript GibWenn 1 3 Schwert");
    parser.execute(unit.getFaction());

    assertEquals(2, unit2.getOrders2().size());
    assertEquals("; TODO: Fehler im Skript in Zeile 1: zu wenig Schwert", unit2.getOrders2().get(1)
        .getText());

    // test supplyer does not have item
    unit.clearOrders();
    unit2.clearOrders();

    unit2.addOrder("// $cript GibWenn 1 10 Silber");
    parser.execute(unit.getFaction());

    assertEquals(3, unit2.getOrders2().size());
    assertEquals("; TODO: Fehler im Skript in Zeile 1: zu wenig Silber", unit2.getOrders2().get(1)
        .getText());
    assertEquals("GIB 1 ALLES Silber", unit2.getOrders2().get(2).getText());

    // test supplyer does not have item
    unit.clearOrders();
    unit2.clearOrders();

    unit2.addOrder("// $cript GibWenn 1 10 Silber nie");
    parser.execute(unit.getFaction());

    assertEquals(3, unit2.getOrders2().size());
    assertEquals("; zu wenig Silber", unit2.getOrders2().get(1).getText());
    assertEquals("GIB 1 ALLES Silber", unit2.getOrders2().get(2).getText());

    // test ALL
    unit.clearOrders();
    unit2.clearOrders();

    unit2.addOrder("// $cript GibWenn 1 ALLES Silber");
    parser.execute(unit.getFaction());

    assertEquals(2, unit2.getOrders2().size());
    assertEquals("GIB 1 ALLES Silber", unit2.getOrders2().get(1).getText());

    // test ALL
    unit.clearOrders();
    unit2.clearOrders();

    unit2.addOrder("// $cript GibWenn 1 JE ALLES Silber");
    parser.execute(unit.getFaction());

    assertEquals(2, unit2.getOrders2().size());
    assertEquals("; TODO: Fehler im Skript in Zeile 1: JE ALLES geht nicht", unit2.getOrders2()
        .get(1).getText());

  }

  /**
   * Test method for {@link E3CommandParser#commandGibWenn(String...)} and
   * {@link E3CommandParser#commandBenoetige(String...)}.
   */
  @Test
  public final void testCommandGibWennAndBenoetige() {
    // add other unit with Silber
    Unit unit2 = builder.addUnit(data, "v", "Versorger", unit.getFaction(), unit.getRegion());
    builder.addItem(data, unit2, "Silber", 5);

    // test GibWenn and Benoetige
    unit.clearOrders();
    unit2.clearOrders();

    unit2.addOrder("// $cript GibWenn 1 3 Silber");
    unit.addOrder("// $cript Benoetige 4 Silber");
    parser.execute(unit.getFaction());

    assertEquals(2, unit.getOrders2().size());
    assertEquals(3, unit2.getOrders2().size());
    assertEquals("GIB 1 3 Silber", unit2.getOrders2().get(1).getText());
    assertEquals("GIB 1 1 Silber", unit2.getOrders2().get(2).getText());

    // test with not enough silver
    unit.clearOrders();
    unit2.clearOrders();

    unit2.addOrder("// $cript GibWenn 2 3 Silber versteckt");
    unit.addOrder("// $cript Benoetige 4 Silber");
    parser.execute(unit.getFaction());

    assertEquals(4, unit2.getOrders2().size());
    assertEquals("; TODO: needs 2 more Silber", unit.getOrders2().get(3).getText());
    assertEquals("GIB 2 3 Silber", unit2.getOrders2().get(2).getText());
    assertEquals("GIB 1 2 Silber", unit2.getOrders2().get(3).getText());
  }

  /**
   * Test method for {@link E3CommandParser#commandWarning(String[])}.
   */
  @Test
  public final void testCommandWarning() {
    unit.clearOrders();
    unit.addOrder("// $cript +1 foo");
    parser.execute(unit.getFaction());
    assertFalse(containsOrder(unit, "// $cript +1 foo"));
    assertFalse(containsOrder(unit, "// $cript +0 foo"));
    assertEquals("; TODO: foo", unit.getOrders2().get(1).getText());
    unit.clearOrders();
    unit.addOrder("// $cript +1");
    parser.execute(unit.getFaction());
    assertEquals("; TODO: ", unit.getOrders2().get(1).getText());
    unit.clearOrders();
    unit.addOrder("// $cript +1 ");
    parser.execute(unit.getFaction());
    assertEquals("; TODO: ", unit.getOrders2().get(1).getText());
    unit.clearOrders();
    unit.addOrder("// $cript +1 +foo    bar bla blubb");
    parser.execute(unit.getFaction());
    assertEquals("; TODO: +foo    bar bla blubb", unit.getOrders2().get(1).getText());
    unit.clearOrders();
    unit.addOrder("// $cript +2 foo");
    parser.execute(unit.getFaction());
    assertFalse(containsOrder(unit, "; TODO: foo"));
    assertEquals("// $cript +1 foo", unit.getOrders2().get(1).getText());
    unit.clearOrders();
    unit.addOrder("// $cript +1 ; TODO bug armbrustagabe?");
    parser.execute(unit.getFaction());
    assertEquals("; TODO: ; TODO bug armbrustagabe?", unit.getOrders2().get(1).getText());
  }

  /**
   * Test method for {@link E3CommandParser#commandSoldier(String...)}.
   */
  @Test
  public final void testCommandSoldier() {
    Unit unit2 = builder.addUnit(data, "s", "Soldat", unit.getFaction(), unit.getRegion());
    unit2.setPersons(10);
    builder.addItem(data, unit2, "Schwert", 5);
    builder.addItem(data, unit, "Schwert", 10);

    // error: no skill
    unit.clearOrders();
    unit2.clearOrders();
    unit2.addOrder("// $cript Soldat");
    parser.execute(unit.getFaction());

    assertEquals(1, unit.getOrders2().size());
    assertEquals(2, unit2.getOrders2().size());
    assertEquals("// $cript Soldat", unit2.getOrders2().get(0).getText());
    assertEquals("; TODO: Fehler im Skript in Zeile 1: no weapon skill", unit2.getOrders2().get(1)
        .getText());

    // normal operation
    builder.addSkill(unit2, "Hiebwaffen", 2);

    unit.clearOrders();
    unit2.clearOrders();
    unit2.addOrder("LERNEN Ausdauer");
    unit2.addOrder("// $cript Soldat");
    parser.execute(unit.getFaction());

    assertEquals(2, unit.getOrders2().size());
    assertEquals("GIB s 5 Schwert", unit.getOrders2().get(1).getText());
    assertEquals(6, unit2.getOrders2().size());
    assertEquals("// $cript Soldat", unit2.getOrders2().get(0).getText());
    // 1 is debug comment
    assertEquals("LERNEN Hiebwaffen", unit2.getOrders2().get(2).getText());
    assertEquals("RESERVIEREN 5 Schwert", unit2.getOrders2().get(3).getText());
    assertEquals("; needs 10 more Schild", unit2.getOrders2().get(4).getText());
    assertEquals("; needs 10 more Kettenhemd", unit2.getOrders2().get(5).getText());

    // normal operation
    builder.addSkill(unit2, "Hiebwaffen", 2);

    unit.clearOrders();
    unit2.clearOrders();
    unit2.addOrder("LERNEN Ausdauer");
    unit2.addOrder("// $cript Soldat best");
    parser.execute(unit.getFaction());

    assertEquals(2, unit.getOrders2().size());
    assertEquals("GIB s 5 Schwert", unit.getOrders2().get(1).getText());
    assertEquals(6, unit2.getOrders2().size());
    assertEquals("LERNEN Hiebwaffen", unit2.getOrders2().get(2).getText());
    assertEquals("RESERVIEREN 5 Schwert", unit2.getOrders2().get(3).getText());
    assertEquals("; needs 10 more Schild", unit2.getOrders2().get(4).getText());
    assertEquals("; needs 10 more Kettenhemd", unit2.getOrders2().get(5).getText());

    // normal operation
    unit.clearOrders();
    unit2.clearOrders();

    unit2.addOrder("// $cript Soldat Hiebwaffen Schwert Schild null");
    unit2.addOrder("LERNEN Ausdauer");
    parser.execute(unit.getFaction());

    assertEquals(2, unit.getOrders2().size());
    assertEquals("GIB s 5 Schwert", unit.getOrders2().get(1).getText());
    assertEquals(6, unit2.getOrders2().size());
    assertEquals("// $cript Soldat Hiebwaffen Schwert Schild null", unit2.getOrders2().get(0)
        .getText());
    assertEquals("LERNEN Hiebwaffen", unit2.getOrders2().get(2).getText());
    assertEquals("RESERVIEREN 5 Schwert", unit2.getOrders2().get(3).getText());
    assertEquals("; TODO: needs 10 more Schild", unit2.getOrders2().get(5).getText());

    // normal operation
    unit.clearOrders();
    unit2.clearOrders();

    unit2.addOrder("// $cript Soldat Hiebwaffen Schwert null best Rüstung");
    parser.execute(unit.getFaction());

    assertEquals(2, unit.getOrders2().size());
    assertEquals("GIB s 5 Schwert", unit.getOrders2().get(1).getText());
    assertEquals(6, unit2.getOrders2().size());
    assertEquals("LERNEN Hiebwaffen", unit2.getOrders2().get(2).getText());
    assertEquals("RESERVIEREN 5 Schwert", unit2.getOrders2().get(3).getText());
    assertEquals("; TODO: needs 10 more Kettenhemd", unit2.getOrders2().get(5).getText());

    // normal operation
    builder.addItem(data, unit2, "Plattenpanzer", 2);

    unit.clearOrders();
    unit2.clearOrders();

    unit2.addOrder("// $cript Soldat Hiebwaffen Schwert null best Rüstung");
    parser.execute(unit.getFaction());

    assertEquals(2, unit.getOrders2().size());
    assertEquals("GIB s 5 Schwert", unit.getOrders2().get(1).getText());
    assertEquals(7, unit2.getOrders2().size());
    assertEquals("LERNEN Hiebwaffen", unit2.getOrders2().get(2).getText());
    assertEquals("RESERVIEREN 5 Schwert", unit2.getOrders2().get(3).getText());
    assertEquals("RESERVIEREN 2 Plattenpanzer", unit2.getOrders2().get(4).getText());
    assertEquals("; TODO: needs 8 more Plattenpanzer", unit2.getOrders2().get(6).getText());

    // ensure that shields are not armour!
    unit.clearOrders();
    unit2.clearOrders();

    unit2.getItem(data.rules.getItemType("Schwert")).setAmount(0);
    unit2.getItem(data.rules.getItemType("Plattenpanzer")).setAmount(0);
    builder.addItem(data, unit2, "Schild", 10);
    builder.addItem(data, unit, "Schild", 5);

    Unit unit3 = builder.addUnit(data, "s2", "Soldat 2", unit.getFaction(), unit.getRegion());
    unit3.setPersons(10);
    builder.addSkill(unit3, "Hiebwaffen", 2);
    builder.addItem(data, unit3, "Schild", 10);

    unit2.addOrder("// $cript Soldat Talent");
    unit3.clearOrders();
    unit3.addOrder("// $cript Soldat Talent");

    parser.execute(unit.getFaction());

    assertEquals(2, unit.getOrders2().size());
    assertEquals("GIB s 10 Schwert", unit.getOrders2().get(1).getText());
    assertEquals(5, unit2.getOrders2().size());
    assertEquals("LERNEN Hiebwaffen", unit2.getOrders2().get(2).getText());
    assertEquals("RESERVIEREN 10 Schild", unit2.getOrders2().get(3).getText());
    assertEquals("; needs 10 more Plattenpanzer", unit2.getOrders2().get(4).getText());
    assertEquals(6, unit3.getOrders2().size());
    assertEquals("LERNEN Hiebwaffen", unit3.getOrders2().get(2).getText());
    assertEquals("; needs 10 more Schwert", unit3.getOrders2().get(3).getText());
    assertEquals("RESERVIEREN 10 Schild", unit3.getOrders2().get(4).getText());
    assertEquals("; needs 10 more Kettenhemd", unit3.getOrders2().get(5).getText());

  }

  /**
   * Test method for
   * {@link E3CommandParser#isUsable(magellan.library.Item, magellan.library.rules.SkillType)} .
   */
  @Test
  public final void testIsUsable() {
    assertTrue(E3CommandParser.isUsable(data.rules.getItemType("Schwert"), data.rules
        .getSkillType("Hiebwaffen")));
    assertTrue(E3CommandParser.isUsable(data.rules.getItemType("Bihänder"), data.rules
        .getSkillType("Hiebwaffen")));
    assertFalse(E3CommandParser.isUsable(data.rules.getItemType("Schwert"), data.rules
        .getSkillType("Bogenschießen")));
  }

  /**
   * Test method for {@link E3CommandParser#isWeaponSkill(magellan.library.Skill)} .
   */
  @Test
  public final void testIsWeaponSkill() {
    Iterator<Skill> skills = unit.getSkills().iterator();
    assertTrue(E3CommandParser.isWeaponSkill(skills.next()));
    assertFalse(E3CommandParser.isWeaponSkill(skills.next()));
    assertFalse(E3CommandParser.isWeaponSkill(skills.next()));
    assertFalse(E3CommandParser.isWeaponSkill(skills.next()));
  }

  /**
   * Test method for {@link E3CommandParser#getSilber(magellan.library.Unit)}.
   */
  @Test
  public final void testGetSilber() {
    assertEquals(0, E3CommandParser.getSilber(unit));
    unit.addItem(new Item(data.rules.getItemType("Silber"), 42));
    assertEquals(42, E3CommandParser.getSilber(unit));
  }

  /**
   * Test method for {@link E3CommandParser#addWarning(magellan.library.Unit, java.lang.String)} .
   */
  @Test
  public final void testAddWarning() {
    assertFalse(containsOrder(unit, "; TODO: foo"));
    unit.clearOrders();
    E3CommandParser.addWarning(unit, "foo");
    assertEquals("; -------------------------------------", unit.getOrders2().get(0).getText());
    assertEquals("; TODO: foo", unit.getOrders2().get(1).getText());
  }

  /**
   * Test method for {@link E3CommandParser#addWarning(magellan.library.Unit, java.lang.String)} .
   */
  @Test
  public final void testCollectStatsRegion() {
    assertEquals("", unit.getOrders2().get(0).getText());
    parser.execute(unit.getFaction());
    // parser.collectStats(unit.getRegion());
    E3CommandParser.notifyMagellan(unit);
    assertEquals("; 0 unit scripts, 0 building scripts, 0 ship scripts, 0 region scripts", unit
        .getOrders2().get(0).getText());
  }

  /**
   * Test method for {@link E3CommandParser#satisfyNeeds()}.
   */
  @Test
  public final void testSatisfyNeeds() {
    fail("Not yet implemented");
  }

  /**
   * Test method for {@link E3CommandParser#giveNeed(Need, boolean)} .
   */
  @Test
  public final void testGiveNeed() {

    fail("Not yet implemented");
  }

  /**
   * Test method for {@link E3CommandParser#reserveNeed(Need, boolean)} .
   */
  @Test
  public final void testReserveNeed() {
    fail("Not yet implemented");
  }

  /**
   * Test method for {@link E3CommandParser#detectScriptCommand(java.lang.String)} .
   */
  @Test
  public final void testDetectScriptCommand() {
    fail("Not yet implemented");
  }

  /**
   * Test method for {@link E3CommandParser#parseScripts()}.
   */
  @Test
  public final void testParseScripts() {
    fail("Not yet implemented");
  }

}
