/**
 * 
 */
package magellan.plugin.extendedcommands.scripts;

import static org.junit.Assert.*;

import java.io.File;
import java.util.*;

import magellan.client.*;
import magellan.client.event.EventDispatcher;
import magellan.library.*;
import magellan.library.utils.Resources;
import magellan.library.utils.logging.Logger;
import magellan.plugin.extendedcommands.*;
import magellan.test.GameDataBuilder;

import org.junit.*;

/**
 * @author steffen
 */
public class E3CommandParserTest {

  private static MagellanContext context;
  private static Properties completionSettings;
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

  /**
   * @throws java.lang.Exception
   */
  @AfterClass
  public static void tearDownAfterClass() throws Exception {
  }

  private static GameData data;
  private static E3CommandParser parser;
  private Unit unit;
  private ExtendedCommandsHelper helper;

  /**
   * @throws java.lang.Exception
   */
  @Before
  public void setUp() throws Exception {
    data = builder.createSimpleGameData(350);
    unit = data.getUnits().iterator().next();
    unit.getFaction().setLocale(Locale.GERMAN);
    client.setData(data);
    parser =
        new E3CommandParser(data, helper =
            ExtendedCommandsProvider.createHelper(client, data, null, null));
  }

  /**
   * @throws java.lang.Exception
   */
  @After
  public void tearDown() throws Exception {
  }

  /**
   * Test method for
   * {@link magellan.plugin.extendedcommands.stm.E3CommandParser#getItemType(java.lang.String)}.
   * 
   * @throws Exception
   */
  @Test
  public final void testGetItemType() throws Exception {
    Assert.assertEquals("Silber", E3CommandParser.getItemType("Silber").getName());
  }

  /**
   * Test method for
   * {@link magellan.plugin.extendedcommands.stm.E3CommandParser#getSkillType(java.lang.String)}.
   */
  @Test
  public final void testGetSkillType() {
    Assert.assertEquals("Hiebwaffen", E3CommandParser.getSkillType("Hiebwaffen").getName());
  }

  /**
   * Test method for
   * {@link magellan.plugin.extendedcommands.stm.E3CommandParser#setConfirm(magellan.library.Unit, boolean)}
   * .
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
   * {@link magellan.plugin.extendedcommands.stm.E3CommandParser#setProperty(magellan.library.Unit, java.lang.String, java.lang.String)}
   * .
   */
  @Test
  public final void testSetProperty() {
    assertEquals(null, unit.getTag("$cript.bla"));
    E3CommandParser.setProperty(unit, "bla", "foo");
    assertEquals("foo", unit.getTag("$cript.bla"));
  }

  /**
   * Test method for
   * {@link magellan.plugin.extendedcommands.stm.E3CommandParser#getProperty(magellan.library.Unit, java.lang.String)}
   * .
   */
  @Test
  public final void testGetProperty() {
    assertEquals("", E3CommandParser.getProperty(unit, "bla"));
    E3CommandParser.setProperty(unit, "bla", "foo");
    assertEquals("foo", E3CommandParser.getProperty(unit, "bla"));
  }

  /**
   * Test method for
   * {@link magellan.plugin.extendedcommands.stm.E3CommandParser#execute(magellan.library.Faction)}.
   */
  @Test
  public final void testExecute() {
    try {
      parser.execute(null);
      fail("argument null not allowed");
    } catch (NullPointerException e) {
      // okay
    }
    parser.execute(unit.getFaction());

  }

  /**
   * Test method for
   * {@link magellan.plugin.extendedcommands.stm.E3CommandParser#addGiveOrder(Unit, Unit, String, int, boolean)}
   * .
   */
  @Test
  public final void testAddGiveOrder() {
    assertFalse(unit.getOrders().contains("GIB 1 5 Silber"));
    unit.clearOrders();
    E3CommandParser.addGiveOrder(unit, unit, "Silber", 5, false);
    assertEquals("GIB 1 5 Silber", unit.getOrders().get(0));

    unit.clearOrders();
    E3CommandParser.addGiveOrder(unit, unit, "Silber", 5, true);
    assertEquals("GIB 1 JE 5 Silber", unit.getOrders().get(0));

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
   * {@link magellan.plugin.extendedcommands.stm.E3CommandParser#addReserveOrder(magellan.library.Unit, java.lang.String, int, boolean)}
   * .
   */
  @Test
  public final void testAddReserveOrder() {
    assertFalse(unit.getOrders().contains("RESERVIEREN 5 Silber"));
    unit.clearOrders();
    E3CommandParser.addReserveOrder(unit, "Silber", 5, false);
    assertEquals("RESERVIEREN 5 Silber", unit.getOrders().get(0));
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

    assertEquals(3, unit.getOrders().size());
    assertEquals("// $cript auto", unit.getOrders().get(1));
    assertEquals("LERNEN Hiebwaffen", unit.getOrders().get(2));
    assertTrue(unit.isOrdersConfirmed());

    unit.clearOrders();
    unit.addOrder("LERNEN Hiebwaffen");
    unit.addOrder("// $cript auto");

    parser.execute(unit.getFaction());

    assertEquals(3, unit.getOrders().size());
    assertEquals("LERNEN Hiebwaffen", unit.getOrders().get(1));
    assertEquals("// $cript auto", unit.getOrders().get(2));
    assertTrue(unit.isOrdersConfirmed());

    // test auto nicht
    unit.clearOrders();
    unit.addOrder("// $cript   auto   nicht");
    unit.addOrder("LERNEN Hiebwaffen");

    parser.execute(unit.getFaction());

    assertEquals(3, unit.getOrders().size());
    assertEquals("// $cript   auto   nicht", unit.getOrders().get(1));
    assertEquals("LERNEN Hiebwaffen", unit.getOrders().get(2));
    assertFalse(unit.isOrdersConfirmed());
  }

  /**
   * Test method for
   * {@link magellan.plugin.extendedcommands.stm.E3CommandParser#commandBenoetige(String...)}.
   */
  @Test
  public final void testCommandBenoetige() {
    // add other unit with Silber
    Unit unit2 = builder.addUnit(data, "v", "Versorger", unit.getFaction(), unit.getRegion());
    builder.addItem(data, unit2, "Silber", 5);

    // test one Benoetige order
    unit.addOrder("// $cript Benoetige 4 Silber");
    parser.execute(unit.getFaction());

    assertEquals("GIB 1 4 Silber", unit2.getOrders().get(1));

    // test one Benoetige order with JE
    unit.setPersons(2);
    unit.addOrder("// $cript Benoetige JE 2 Silber");
    parser.execute(unit.getFaction());

    assertEquals("GIB 1 4 Silber", unit2.getOrders().get(1));

    // test if unit's own items are RESERVED first
    unit.clearOrders();
    unit2.clearOrders();

    builder.addItem(data, unit, "Silber", 2);
    unit.addOrder("// $cript Benoetige 4 Silber");

    parser.execute(unit.getFaction());
    assertEquals("RESERVIEREN 2 Silber", unit.getOrders().get(2));
    assertEquals("GIB 1 2 Silber", unit2.getOrders().get(0));

    // test what happens if supplyer unit already has a RESERVE order (nothing should change)
    unit.clearOrders();
    unit2.clearOrders();

    unit.addOrder("// $cript Benoetige 4 Silber");
    unit2.addOrder("RESERVIEREN 4 Silber");
    unit2.getRegion().refreshUnitRelations(true);

    parser.execute(unit.getFaction());
    assertEquals("RESERVIEREN 2 Silber", unit.getOrders().get(2));
    assertFalse(unit.getOrders().contains("; TODO: needs 1 more Silber"));
    assertEquals("GIB 1 2 Silber", unit2.getOrders().get(1));

    // test what happens if Benoetige unit already has a RESERVE order (nothing should change)
    unit.clearOrders();
    unit2.clearOrders();

    unit.addOrder("// $cript Benoetige 4 Silber");
    unit.addOrder("RESERVIEREN 1 Silber");

    parser.execute(unit.getFaction());
    assertEquals("RESERVIEREN 1 Silber", unit.getOrders().get(2));
    assertEquals("GIB 1 2 Silber", unit2.getOrders().get(0));

    // supplyer now also has Benoetige order and cannot satisfy demand
    unit.clearOrders();
    unit2.clearOrders();

    unit.addOrder("// $cript Benoetige 4 Silber");
    unit2.addOrder("// $cript Benoetige 4 Silber");

    parser.execute(unit.getFaction());
    assertEquals("RESERVIEREN 2 Silber", unit.getOrders().get(2));
    assertEquals("; TODO: needs 1 more Silber", unit.getOrders().get(4));
    assertEquals("RESERVIEREN 4 Silber", unit2.getOrders().get(1));
    assertEquals("GIB 1 1 Silber", unit2.getOrders().get(2));

    // test Benoetige ALLES
    unit.clearOrders();
    unit2.clearOrders();

    unit.addOrder("// $cript Benoetige ALLES Silber");
    unit2.addOrder("// $cript Benoetige 1 2 Silber");

    parser.execute(unit.getFaction());
    assertEquals("RESERVIEREN 2 Silber", unit.getOrders().get(2));
    assertEquals("RESERVIEREN 1 Silber", unit2.getOrders().get(1));
    assertEquals("RESERVIEREN 1 Silber", unit2.getOrders().get(2));
    assertEquals("GIB 1 3 Silber", unit2.getOrders().get(3));
  }

  /**
   * Test method for
   * {@link magellan.plugin.extendedcommands.stm.E3CommandParser#commandGibWenn(String[])}.
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

    assertEquals(4, unit2.getOrders().size());
  }

  /**
   * Test method for
   * {@link magellan.plugin.extendedcommands.stm.E3CommandParser#commandGibWenn(String[])}.
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

    assertEquals(2, unit2.getOrders().size());
    assertEquals("GIB 1 3 Silber", unit2.getOrders().get(1));

    // test receiver unit not there
    unit.clearOrders();
    unit2.clearOrders();

    unit2.addOrder("// $cript GibWenn 2 3 Silber");
    parser.execute(unit.getFaction());

    assertEquals(3, unit2.getOrders().size());
    assertEquals("// $cript GibWenn 2 3 Silber", unit2.getOrders().get(0));
    assertEquals("; TODO: Fehler im Skript in Zeile 1: 2 nicht da.", unit2.getOrders().get(1));//
    assertEquals("GIB 2 3 Silber", unit2.getOrders().get(2));

    // test receiver unit not there (with warning)
    unit.clearOrders();
    unit2.clearOrders();

    unit2.addOrder("// $cript GibWenn 2 3 Silber immer");
    parser.execute(unit.getFaction());

    assertEquals(3, unit2.getOrders().size());
    assertEquals("; TODO: Fehler im Skript in Zeile 1: 2 nicht da.", unit2.getOrders().get(1));
    assertEquals("GIB 2 3 Silber", unit2.getOrders().get(2));

    // test receiver unit not there (with warning)
    unit.clearOrders();
    unit2.clearOrders();

    unit2.addOrder("// $cript GibWenn 2 3 Silber Menge");
    parser.execute(unit.getFaction());

    assertEquals(3, unit2.getOrders().size());
    assertEquals("; 2 nicht da.", unit2.getOrders().get(1));
    assertEquals("GIB 2 3 Silber", unit2.getOrders().get(2));

    // test receiver unit not there (with warning)
    unit.clearOrders();
    unit2.clearOrders();

    unit2.addOrder("// $cript GibWenn 2 3 Silber Einheit");
    parser.execute(unit.getFaction());

    assertEquals(3, unit2.getOrders().size());
    assertEquals("; TODO: Fehler im Skript in Zeile 1: 2 nicht da.", unit2.getOrders().get(1));
    assertEquals("GIB 2 3 Silber", unit2.getOrders().get(2));

    // test receiver unit not there (with warning)
    unit.clearOrders();
    unit2.clearOrders();

    unit2.addOrder("// $cript GibWenn 2 3 Silber nie");
    parser.execute(unit.getFaction());

    assertEquals(3, unit2.getOrders().size());
    assertEquals("; 2 nicht da.", unit2.getOrders().get(1));
    assertEquals("GIB 2 3 Silber", unit2.getOrders().get(2));

    // test receiver unit not there (with nonsense warning)
    unit.clearOrders();
    unit2.clearOrders();

    unit2.addOrder("// $cript GibWenn 2 3 Silber bla");
    parser.execute(unit.getFaction());

    assertEquals(4, unit2.getOrders().size());
    assertEquals(
        "; TODO: Fehler im Skript in Zeile 1: unbekannter Warnungstyp bla; \"immer\", \"Menge\", \"Einheit\" oder \"nie\" erlaubt.",
        unit2.getOrders().get(1));

    assertEquals("; TODO: Fehler im Skript in Zeile 1: 2 nicht da.", unit2.getOrders().get(2));
    assertEquals("GIB 2 3 Silber", unit2.getOrders().get(3));

    // test receiver unit not there (with nonsense warning)
    unit.clearOrders();
    unit2.clearOrders();

    unit2.addOrder("// $cript GibWenn 1 3 Silber bla");
    parser.execute(unit.getFaction());

    assertEquals(3, unit2.getOrders().size());
    assertEquals(
        "; TODO: Fehler im Skript in Zeile 1: unbekannter Warnungstyp bla; \"immer\", \"Menge\", \"Einheit\" oder \"nie\" erlaubt.",
        unit2.getOrders().get(1));
    assertEquals("GIB 1 3 Silber", unit2.getOrders().get(2));

    // test supplyer does not have item
    unit.clearOrders();
    unit2.clearOrders();

    unit2.addOrder("// $cript GibWenn 1 3 Schwert");
    parser.execute(unit.getFaction());

    assertEquals(2, unit2.getOrders().size());
    assertEquals("; TODO: Fehler im Skript in Zeile 1: zu wenig Schwert", unit2.getOrders().get(1));

    // test supplyer does not have item
    unit.clearOrders();
    unit2.clearOrders();

    unit2.addOrder("// $cript GibWenn 1 10 Silber");
    parser.execute(unit.getFaction());

    assertEquals(3, unit2.getOrders().size());
    assertEquals("; TODO: Fehler im Skript in Zeile 1: zu wenig Silber", unit2.getOrders().get(1));
    assertEquals("GIB 1 ALLES Silber", unit2.getOrders().get(2));

    // test supplyer does not have item
    unit.clearOrders();
    unit2.clearOrders();

    unit2.addOrder("// $cript GibWenn 1 10 Silber nie");
    parser.execute(unit.getFaction());

    assertEquals(3, unit2.getOrders().size());
    assertEquals("; zu wenig Silber", unit2.getOrders().get(1));
    assertEquals("GIB 1 ALLES Silber", unit2.getOrders().get(2));

    // test ALL
    unit.clearOrders();
    unit2.clearOrders();

    unit2.addOrder("// $cript GibWenn 1 ALLES Silber");
    parser.execute(unit.getFaction());

    assertEquals(2, unit2.getOrders().size());
    assertEquals("GIB 1 ALLES Silber", unit2.getOrders().get(1));

    // test ALL
    unit.clearOrders();
    unit2.clearOrders();

    unit2.addOrder("// $cript GibWenn 1 JE ALLES Silber");
    parser.execute(unit.getFaction());

    assertEquals(2, unit2.getOrders().size());
    assertEquals("; TODO: Fehler im Skript in Zeile 1: JE ALLES geht nicht", unit2.getOrders().get(
        1));

  }

  /**
   * Test method for
   * {@link magellan.plugin.extendedcommands.stm.E3CommandParser#commandGibWenn(String...)} and
   * {@link magellan.plugin.extendedcommands.stm.E3CommandParser#commandBenoetige(String...)}.
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

    assertEquals(3, unit2.getOrders().size());
    assertEquals("GIB 1 3 Silber", unit2.getOrders().get(1));
    assertEquals("GIB 1 1 Silber", unit2.getOrders().get(2));

    // test with not enough silver
    unit.clearOrders();
    unit2.clearOrders();

    unit2.addOrder("// $cript GibWenn 2 3 Silber nie");
    unit.addOrder("// $cript Benoetige 4 Silber");
    parser.execute(unit.getFaction());

    assertEquals(4, unit2.getOrders().size());
    assertEquals("; TODO: needs 2 more Silber", unit.getOrders().get(3));
    assertEquals("GIB 2 3 Silber", unit2.getOrders().get(2));
    assertEquals("GIB 1 2 Silber", unit2.getOrders().get(3));
  }

  /**
   * Test method for
   * {@link magellan.plugin.extendedcommands.stm.E3CommandParser#commandWarning(java.util.List)}.
   */
  @Test
  public final void testCommandWarning() {
    unit.clearOrders();
    unit.addOrder("// $cript +1 foo");
    parser.execute(unit.getFaction());
    assertFalse(unit.getOrders().contains("// $cript +1 foo"));
    assertFalse(unit.getOrders().contains("// $cript +0 foo"));
    assertEquals("; TODO: foo", unit.getOrders().get(1));
    unit.clearOrders();
    unit.addOrder("// $cript +1");
    parser.execute(unit.getFaction());
    assertEquals("; TODO: ", unit.getOrders().get(1));
    unit.clearOrders();
    unit.addOrder("// $cript +1 ");
    parser.execute(unit.getFaction());
    assertEquals("; TODO: ", unit.getOrders().get(1));
    unit.clearOrders();
    unit.addOrder("// $cript +1 +foo    bar bla blubb");
    parser.execute(unit.getFaction());
    assertEquals("; TODO: +foo    bar bla blubb", unit.getOrders().get(1));
    unit.clearOrders();
    unit.addOrder("// $cript +2 foo");
    parser.execute(unit.getFaction());
    assertFalse(unit.getOrders().contains("; TODO: foo"));
    assertEquals("// $cript +1 foo", unit.getOrders().get(1));
  }

  /**
   * Test method for
   * {@link magellan.plugin.extendedcommands.stm.E3CommandParser#commandSoldier(String...)}.
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

    assertEquals(1, unit.getOrders().size());
    assertEquals(2, unit2.getOrders().size());
    assertEquals("// $cript Soldat", unit2.getOrders().get(0));
    assertEquals("; TODO: Fehler im Skript in Zeile 1: no weapon skill", unit2.getOrders().get(1));

    // normal operation
    builder.addSkill(unit2, "Hiebwaffen", 2);

    unit.clearOrders();
    unit2.clearOrders();
    unit2.addOrder("LERNEN Ausdauer");
    unit2.addOrder("// $cript Soldat");
    parser.execute(unit.getFaction());

    assertEquals(2, unit.getOrders().size());
    assertEquals("GIB s 5 Schwert", unit.getOrders().get(1));
    assertEquals(5, unit2.getOrders().size());
    assertEquals("// $cript Soldat", unit2.getOrders().get(0));
    assertEquals("LERNEN Hiebwaffen", unit2.getOrders().get(1));
    assertEquals("RESERVIEREN 5 Schwert", unit2.getOrders().get(2));
    assertEquals("; needs 10 more Schild", unit2.getOrders().get(3));
    assertEquals("; needs 10 more Kettenhemd", unit2.getOrders().get(4));

    // normal operation
    builder.addSkill(unit2, "Hiebwaffen", 2);

    unit.clearOrders();
    unit2.clearOrders();
    unit2.addOrder("LERNEN Ausdauer");
    unit2.addOrder("// $cript Soldat best");
    parser.execute(unit.getFaction());

    assertEquals(2, unit.getOrders().size());
    assertEquals("GIB s 5 Schwert", unit.getOrders().get(1));
    assertEquals(5, unit2.getOrders().size());
    assertEquals("LERNEN Hiebwaffen", unit2.getOrders().get(1));
    assertEquals("RESERVIEREN 5 Schwert", unit2.getOrders().get(2));
    assertEquals("; needs 10 more Schild", unit2.getOrders().get(3));
    assertEquals("; needs 10 more Kettenhemd", unit2.getOrders().get(4));

    // normal operation
    unit.clearOrders();
    unit2.clearOrders();

    unit2.addOrder("// $cript Soldat Hiebwaffen Schwert Schild null");
    unit2.addOrder("LERNEN Ausdauer");
    parser.execute(unit.getFaction());

    assertEquals(2, unit.getOrders().size());
    assertEquals("GIB s 5 Schwert", unit.getOrders().get(1));
    assertEquals(5, unit2.getOrders().size());
    assertEquals("// $cript Soldat Hiebwaffen Schwert Schild null", unit2.getOrders().get(0));
    assertEquals("LERNEN Hiebwaffen", unit2.getOrders().get(1));
    assertEquals("RESERVIEREN 5 Schwert", unit2.getOrders().get(2));
    assertEquals("; TODO: needs 10 more Schild", unit2.getOrders().get(4));

    // normal operation
    unit.clearOrders();
    unit2.clearOrders();

    unit2.addOrder("// $cript Soldat Hiebwaffen Schwert null best Rüstung");
    parser.execute(unit.getFaction());

    assertEquals(2, unit.getOrders().size());
    assertEquals("GIB s 5 Schwert", unit.getOrders().get(1));
    assertEquals(5, unit2.getOrders().size());
    assertEquals("LERNEN Hiebwaffen", unit2.getOrders().get(1));
    assertEquals("RESERVIEREN 5 Schwert", unit2.getOrders().get(2));
    assertEquals("; TODO: needs 10 more Kettenhemd", unit2.getOrders().get(4));

    // normal operation
    builder.addItem(data, unit2, "Plattenpanzer", 2);

    unit.clearOrders();
    unit2.clearOrders();

    unit2.addOrder("// $cript Soldat Hiebwaffen Schwert null best Rüstung");
    parser.execute(unit.getFaction());

    assertEquals(2, unit.getOrders().size());
    assertEquals("GIB s 5 Schwert", unit.getOrders().get(1));
    assertEquals(6, unit2.getOrders().size());
    assertEquals("LERNEN Hiebwaffen", unit2.getOrders().get(1));
    assertEquals("RESERVIEREN 5 Schwert", unit2.getOrders().get(2));
    assertEquals("RESERVIEREN 2 Plattenpanzer", unit2.getOrders().get(3));
    assertEquals("; TODO: needs 8 more Plattenpanzer", unit2.getOrders().get(5));

  }

  /**
   * Test method for
   * {@link magellan.plugin.extendedcommands.stm.E3CommandParser#isUsable(magellan.library.Item, magellan.library.rules.SkillType)}
   * .
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
   * Test method for
   * {@link magellan.plugin.extendedcommands.stm.E3CommandParser#isWeaponSkill(magellan.library.Skill)}
   * .
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
   * Test method for
   * {@link magellan.plugin.extendedcommands.stm.E3CommandParser#getSilber(magellan.library.Unit)}.
   */
  @Test
  public final void testGetSilber() {
    assertEquals(0, E3CommandParser.getSilber(unit));
    unit.addItem(new Item(data.rules.getItemType("Silber"), 42));
    assertEquals(42, E3CommandParser.getSilber(unit));
  }

  /**
   * Test method for
   * {@link magellan.plugin.extendedcommands.stm.E3CommandParser#addWarning(magellan.library.Unit, java.lang.String)}
   * .
   */
  @Test
  public final void testAddWarning() {
    assertFalse(unit.getOrders().contains("; TODO: foo"));
    unit.clearOrders();
    E3CommandParser.addWarning(unit, "foo");
    assertEquals("; -------------------------------------", unit.getOrders().get(0));
    assertEquals("; TODO: foo", unit.getOrders().get(1));
  }

  /**
   * Test method for
   * {@link magellan.plugin.extendedcommands.stm.E3CommandParser#addWarning(magellan.library.Unit, java.lang.String)}
   * .
   */
  @Test
  public final void testCollectStatsRegion() {
    assertEquals("", unit.getOrders().get(0));
    parser.execute(unit.getFaction());
    // parser.collectStats(unit.getRegion());
    E3CommandParser.notifyMagellan(unit);
    assertEquals("; 0 unit scripts, 0 building scripts, 0 ship scripts, 0 region scripts", unit
        .getOrders().get(0));
  }

  /**
   * Test method for {@link magellan.plugin.extendedcommands.stm.E3CommandParser#satisfyNeeds()}.
   */
  @Test
  public final void testSatisfyNeeds() {
    fail("Not yet implemented");
  }

  /**
   * Test method for
   * {@link magellan.plugin.extendedcommands.stm.E3CommandParser#giveNeed(Need, boolean)} .
   */
  @Test
  public final void testGiveNeed() {

    fail("Not yet implemented");
  }

  /**
   * Test method for
   * {@link magellan.plugin.extendedcommands.stm.E3CommandParser#reserveNeed(Need, boolean)} .
   */
  @Test
  public final void testReserveNeed() {
    fail("Not yet implemented");
  }

  /**
   * Test method for
   * {@link magellan.plugin.extendedcommands.stm.E3CommandParser#detectScriptCommand(java.lang.String)}
   * .
   */
  @Test
  public final void testDetectScriptCommand() {
    fail("Not yet implemented");
  }

  /**
   * Test method for {@link magellan.plugin.extendedcommands.stm.E3CommandParser#parseScripts()}.
   */
  @Test
  public final void testParseScripts() {
    fail("Not yet implemented");
  }

}
