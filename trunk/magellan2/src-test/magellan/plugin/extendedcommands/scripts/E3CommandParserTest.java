/**
 * 
 */
package magellan.plugin.extendedcommands.scripts;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertSame;
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

    E3CommandParser.DEFAULT_SUPPLY_PRIORITY = 1;
  }

  @SuppressWarnings("deprecation")
  protected boolean containsOrder(Unit unit2, String string) {
    return unit2.getOrders().contains(string);
  }

  protected void assertComment(Unit u, int number, boolean shortComment, boolean longComment) {
    assertTrue("expected comment, but not enough orders", u.getOrders2().size() > number);
    String actual = u.getOrders2().get(number).getText();
    assertTrue("no long comment: " + actual, !longComment || actual.startsWith("// "));
    assertTrue("no short comment: " + actual, !shortComment || actual.startsWith(";"));
  }

  protected void assertOrder(String expected, Unit u, int number) {
    assertTrue("expected " + expected + ", but not enough orders", u.getOrders2().size() > number);
    String actual = u.getOrders2().get(number).getText();
    if (!(actual.startsWith(expected) && (actual.length() == expected.length() || actual.substring(
        expected.length()).trim().startsWith(";")))) {
      assertEquals(expected, actual);
    }
  }

  protected void assertError(String expected, Unit u, int number) {
    assertTrue("expected " + expected + ", but not enough orders", u.getOrders2().size() > number);
    String actual = u.getOrders2().get(number).getText();
    if (!(actual.contains(expected) && actual.startsWith("; TODO"))) {
      assertEquals("; TODO: " + expected, actual);
    }
  }

  protected void assertMessage(String expected, Unit u, int number) {
    assertTrue("expected " + expected + ", but not enough orders", u.getOrders2().size() > number);
    String actual = u.getOrders2().get(number).getText();
    if (!(actual.contains(expected) && actual.startsWith("; "))) {
      assertEquals("; " + expected, actual);
    }
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
    assertOrder("GIB 1 5 Silber", unit, 0);

    unit.clearOrders();
    E3CommandParser.addGiveOrder(unit, unit, "Silber", 5, true);
    assertOrder("GIB 1 JE 5 Silber", unit, 0);

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
    assertOrder("RESERVIEREN 5 Silber", unit, 0);
  }

  /**
   * Test method for the repeat command.
   */
  @Test
  public final void testCommandRepeat() {
    unit.clearOrders();
    unit.deleteAllTags();
    unit.addOrder("// $cript 2 LERNEN Hiebwaffen");

    parser.execute(unit.getFaction());
    assertEquals(2, unit.getOrders2().size());
    assertOrder("// $cript 1 LERNEN Hiebwaffen", unit, 1);

    unit.clearOrders();
    unit.deleteAllTags();
    unit.addOrder("// $cript 1 LERNEN Hiebwaffen");

    parser.execute(unit.getFaction());
    assertEquals(2, unit.getOrders2().size());
    assertOrder("LERNEN Hiebwaffen", unit, 1);

    unit.clearOrders();
    unit.deleteAllTags();
    unit.addOrder("// $cript 2 5 // $cript GibWenn abc 100 Silber");

    parser.execute(unit.getFaction());
    assertEquals(2, unit.getOrders2().size());
    assertOrder("// $cript 1 5 // $cript GibWenn abc 100 Silber", unit, 1);

    unit.clearOrders();
    unit.deleteAllTags();
    unit.addOrder("// $cript 1 5 // $cript GibWenn abc 100 Silber");

    parser.execute(unit.getFaction());
    assertEquals(4, unit.getOrders2().size());
    assertOrder("// $cript 5 5 // $cript GibWenn abc 100 Silber", unit, 1);
    assertOrder("// $cript GibWenn abc 100 Silber", unit, 2);
    assertError("abc nicht da", unit, 3);

    unit.clearOrders();
    unit.deleteAllTags();
    unit.addOrder("// $cript 2 5 // $cript GibWenn abc 100 Silber");

    parser.execute(unit.getFaction());
    assertEquals(2, unit.getOrders2().size());
    assertOrder("// $cript 1 5 // $cript GibWenn abc 100 Silber", unit, 1);

    unit.clearOrders();
    unit.deleteAllTags();
    unit.addOrder("// $cript 2 5 1 LERNEN Hiebwaffen");

    parser.execute(unit.getFaction());
    assertEquals(2, unit.getOrders2().size());
    assertOrder("// $cript 1 5 0 LERNEN Hiebwaffen", unit, 1);

    unit.clearOrders();
    unit.deleteAllTags();
    unit.addOrder("// $cript 1 5 0 LERNEN Hiebwaffen");

    parser.execute(unit.getFaction());
    assertEquals(2, unit.getOrders2().size());
    assertOrder("LERNEN Hiebwaffen", unit, 1);

    unit.clearOrders();
    unit.deleteAllTags();
    unit.addOrder("// $cript 2 5 0 LERNEN Hiebwaffen");

    parser.execute(unit.getFaction());
    assertEquals(2, unit.getOrders2().size());
    assertOrder("", unit, 1);
  }

  /**
   * Test method for the auto command.
   */
  @Test
  public final void testCommandAuto() {
    unit.clearOrders();
    unit.deleteAllTags();
    unit.addOrder("// $cript auto");
    unit.addOrder("LERNEN Hiebwaffen");

    parser.execute(unit.getFaction());

    assertEquals(3, unit.getOrders2().size());
    assertOrder("// $cript auto", unit, 1);
    assertOrder("LERNEN Hiebwaffen", unit, 2);
    assertTrue(E3CommandParser.getProperty(unit, "confirm").equals("1"));

    unit.clearOrders();
    unit.deleteAllTags();
    unit.addOrder("LERNEN Hiebwaffen");
    unit.addOrder("// $cript auto");

    parser.execute(unit.getFaction());

    assertEquals(3, unit.getOrders2().size());
    assertOrder("LERNEN Hiebwaffen", unit, 1);
    assertOrder("// $cript auto", unit, 2);
    assertTrue(E3CommandParser.getProperty(unit, "confirm").equals("1"));

    // test auto nicht
    unit.clearOrders();
    unit.deleteAllTags();
    unit.addOrder("// $cript   auto   NICHT");
    unit.addOrder("LERNEN Hiebwaffen");

    parser.execute(unit.getFaction());

    assertEquals(3, unit.getOrders2().size());
    assertOrder("// $cript   auto   NICHT", unit, 1);
    assertOrder("LERNEN Hiebwaffen", unit, 2);
    assertTrue(E3CommandParser.getProperty(unit, "confirm").equals("0"));

    // test nicht precedemce
    unit.clearOrders();
    unit.deleteAllTags();
    unit.addOrder("// $cript   auto   NICHT");
    unit.addOrder("// $cript   auto");
    unit.addOrder("LERNEN Hiebwaffen");

    parser.execute(unit.getFaction());

    assertEquals(4, unit.getOrders2().size());
    assertOrder("// $cript   auto   NICHT", unit, 1);
    assertOrder("// $cript   auto", unit, 2);
    assertOrder("LERNEN Hiebwaffen", unit, 3);
    assertTrue(E3CommandParser.getProperty(unit, "confirm").equals("0"));

    // test auto period
    unit.clearOrders();
    unit.deleteAllTags();
    unit.addOrder("// $cript auto 0");
    parser.execute(unit.getFaction());
    assertEquals(2, unit.getOrders2().size());
    assertOrder("// $cript auto -1", unit, 1);
    assertTrue(E3CommandParser.getProperty(unit, "confirm").equals("0"));

    // test auto period
    unit.clearOrders();
    unit.deleteAllTags();
    unit.addOrder("// $cript auto 1");
    parser.execute(unit.getFaction());
    assertEquals(2, unit.getOrders2().size());
    assertOrder("// $cript auto 0", unit, 1);
    assertTrue(E3CommandParser.getProperty(unit, "confirm").equals("1"));

    // test auto period
    unit.clearOrders();
    unit.deleteAllTags();
    unit.addOrder("// $cript auto 2 5");
    parser.execute(unit.getFaction());
    assertEquals(2, unit.getOrders2().size());
    assertOrder("// $cript auto 1 5", unit, 1);
    assertTrue(E3CommandParser.getProperty(unit, "confirm").equals("1"));

    // test auto period
    unit.clearOrders();
    unit.deleteAllTags();
    unit.addOrder("// $cript auto 0 5");
    parser.execute(unit.getFaction());
    assertEquals(2, unit.getOrders2().size());
    assertOrder("// $cript auto 4 5", unit, 1);
    assertTrue(E3CommandParser.getProperty(unit, "confirm").equals("0"));
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
    assertOrder("GIB 1 4 Silber", unit2, 1);

    // test one Benoetige order with JE
    unit.clearOrders();
    unit2.clearOrders();
    unit.setPersons(2);
    unit.addOrder("// $cript Benoetige JE 2 Silber");
    parser.execute(unit.getFaction());

    assertEquals(2, unit.getOrders2().size());
    assertEquals(1, unit2.getOrders2().size());
    assertOrder("GIB 1 4 Silber", unit2, 0);

    // test if unit's own items are RESERVED first
    unit.clearOrders();
    unit2.clearOrders();

    builder.addItem(data, unit, "Silber", 2);
    unit.addOrder("// $cript Benoetige 4 Silber");

    parser.execute(unit.getFaction());
    assertOrder("RESERVIEREN JE 1 Silber", unit, 2);
    assertOrder("GIB 1 2 Silber", unit2, 0);
    assertEquals(3, unit.getOrders2().size());
    assertEquals(1, unit2.getOrders2().size());
    assertOrder("GIB 1 2 Silber", unit2, 0);

    // test what happens if supplyer unit already has a RESERVE order (nothing should change)
    unit.clearOrders();
    unit2.clearOrders();

    unit.addOrder("// $cript Benoetige 4 Silber");
    unit2.addOrder("RESERVIEREN 4 Silber");
    unit2.getRegion().refreshUnitRelations(true);

    parser.execute(unit.getFaction());
    assertOrder("RESERVIEREN JE 1 Silber", unit, 2);
    assertFalse(containsOrder(unit, "; TODO: braucht 1 mehr Silber"));
    assertOrder("RESERVIEREN 4 Silber", unit2, 0);
    assertOrder("GIB 1 2 Silber", unit2, 1);

    // test what happens if Benoetige unit already has a RESERVE order (nothing should change)
    unit.clearOrders();
    unit2.clearOrders();

    unit.addOrder("// $cript Benoetige 4 Silber");
    unit.addOrder("RESERVIEREN 1 Silber");

    parser.execute(unit.getFaction());
    assertOrder("RESERVIEREN 1 Silber", unit, 2);
    assertOrder("GIB 1 2 Silber", unit2, 0);

    // supplyer now also has Benoetige order and cannot satisfy demand
    unit.clearOrders();
    unit2.clearOrders();

    unit.addOrder("// $cript Benoetige 4 Silber");
    unit2.addOrder("// $cript Benoetige 4 Silber");

    parser.execute(unit.getFaction());
    assertOrder("RESERVIEREN JE 1 Silber", unit, 2);
    assertError("braucht 1 mehr Silber", unit, 3);
    assertOrder("RESERVIEREN 4 Silber", unit2, 1);
    assertOrder("GIB 1 1 Silber", unit2, 2);

    // test Benoetige ALLES
    unit.clearOrders();
    unit2.clearOrders();

    unit.addOrder("// $cript Benoetige ALLES Silber");
    unit2.addOrder("// $cript Benoetige 1 2 Silber");

    parser.execute(unit.getFaction());
    // assertOrder("RESERVIEREN 2 Silber", unit, 2);
    assertOrder("RESERVIEREN JE 1 Silber", unit2, 1);
    assertOrder("GIB 1 3 Silber", unit2, 2);
    assertSame(3, unit2.getOrders2().size());

    // // test Benoetige ALLES
    // unit.clearOrders();
    // unit2.clearOrders();
    //
    // unit.addOrder("// $cript Benoetige ALLES Silber");
    // unit2.addOrder("// $cript Benoetige 1 2 Silber");
    // unit2.addOrder("// $cript Versorge 1");
    //
    // parser.execute(unit.getFaction());
    // // assertOrder("RESERVIEREN 2 Silber", unit, 2);
    // assertOrder("RESERVIEREN 1 Silber", unit2, 2);
    // assertOrder("GIB 1 3 Silber", unit2, 3);
    // assertSame(2, unit.getOrders2().size());
    // assertSame(4, unit2.getOrders2().size());
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
    assertOrder("GIB 1 4 Silber", unit2, 1);
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
    assertOrder("GIB 1 4 Silber", unit2, 1);
  }

  /**
   * Test method for {@link E3CommandParser#commandGibWenn(String[])}.
   */
  @Test
  public final void testCommandGibWenn2() {
    E3CommandParser.ADD_NOT_THERE_INFO = true;
    // add other unit with Silber
    Unit unit2 = builder.addUnit(data, "v", "Versorger", unit.getFaction(), unit.getRegion());
    builder.addItem(data, unit2, "Silber", 5);

    // test one GibWenn order
    unit.clearOrders();
    unit2.clearOrders();

    unit2.addOrder("// $cript GibWenn qwra 120 Silber Menge");
    parser.execute(unit.getFaction());

    assertEquals(1, unit.getOrders2().size());
    assertEquals(2, unit2.getOrders2().size());

    // test one GibWenn order
    unit.clearOrders();
    unit2.clearOrders();

    unit2.addOrder("// $cript GibWenn qwra 120 Silber versteckt");
    parser.execute(unit.getFaction());

    assertEquals(1, unit.getOrders2().size());
    assertEquals(4, unit2.getOrders2().size());
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
    assertOrder("GIB 1 ALLES Würziger~Wagemut", unit2, 1);
    assertOrder("GIB 1 ALLES Flachwurz", unit2, 2);
    assertOrder("GIB 1 ALLES Myrrhe", unit2, 4);
    assertOrder("GIB 1 ALLES Seide", unit2, 5);
  }

  /**
   * Test method for {@link E3CommandParser#commandGibWenn(String[])}.
   */
  @Test
  public final void testCommandGibWenn4() {
    // add other unit with Kraut
    Unit unit2 = builder.addUnit(data, "v", "Versorger", unit.getFaction(), unit.getRegion());
    // should not crash for unknown items
    builder.addItem(data, unit2, "Geheimnisvolles Teil", 2);
    builder.addItem(data, unit2, "Flachwurz", 5);
    builder.addItem(data, unit2, "Myrrhe", 5);
    builder.addItem(data, unit2, "Seide", 5);

    // test one GibWenn order
    unit.clearOrders();
    unit2.clearOrders();
    unit2.addOrder("// $cript GibWenn 1 KRAUT");
    unit2.addOrder("// $cript GibWenn 1 LUXUS");

    parser.execute(unit.getFaction());
    assertEquals(5, unit2.getOrders2().size());
    assertOrder("GIB 1 ALLES Flachwurz", unit2, 1);
    assertOrder("GIB 1 ALLES Myrrhe", unit2, 3);
    assertOrder("GIB 1 ALLES Seide", unit2, 4);
  }

  /**
   * Test method for {@link E3CommandParser#commandGibWenn(String[])}.
   */
  @Test
  public final void testCommandGibWenn() {
    E3CommandParser.ADD_NOT_THERE_INFO = true;
    // add other unit with Silber
    Unit unit2 = builder.addUnit(data, "v", "Versorger", unit.getFaction(), unit.getRegion());
    builder.addItem(data, unit2, "Silber", 5);

    // test one GibWenn order
    unit.clearOrders();
    unit2.clearOrders();

    unit2.addOrder("// $cript GibWenn 1 3 Silber");
    parser.execute(unit.getFaction());

    assertEquals(2, unit2.getOrders2().size());
    assertOrder("GIB 1 3 Silber", unit2, 1);

    // test receiver unit not there
    unit.clearOrders();
    unit2.clearOrders();

    unit2.addOrder("// $cript GibWenn 2 3 Silber");
    parser.execute(unit.getFaction());

    assertEquals(2, unit2.getOrders2().size());
    assertOrder("// $cript GibWenn 2 3 Silber", unit2, 0);
    assertError("nicht da", unit2, 1);//
    // assertOrder("GIB 2 3 Silber", unit2, 2);

    // test receiver unit not there (with warning)
    unit.clearOrders();
    unit2.clearOrders();

    unit2.addOrder("// $cript GibWenn 2 3 Silber immer");
    parser.execute(unit.getFaction());

    assertEquals(2, unit2.getOrders2().size());
    assertError("nicht da", unit2, 1);
    // assertOrder("GIB 2 3 Silber", unit2, 2);

    // test receiver unit not there (without warning)
    unit.clearOrders();
    unit2.clearOrders();

    unit2.addOrder("// $cript GibWenn 2 3 Silber Menge");
    parser.execute(unit.getFaction());

    assertEquals(2, unit2.getOrders2().size());
    assertOrder("; 2 nicht da", unit2, 1);
    // assertOrder("GIB 2 3 Silber", unit2, 2);

    // test receiver unit not there (with warning)
    unit.clearOrders();
    unit2.clearOrders();

    unit2.addOrder("// $cript GibWenn 2 3 Silber Einheit");
    parser.execute(unit.getFaction());

    assertEquals(2, unit2.getOrders2().size());
    assertError("2 nicht da", unit2, 1);
    // assertOrder("GIB 2 3 Silber", unit2, 2);

    // test receiver unit not there (with hidden unit warning)
    unit.clearOrders();
    unit2.clearOrders();

    unit2.addOrder("// $cript GibWenn 2 3 Silber versteckt");
    parser.execute(unit.getFaction());

    assertEquals(3, unit2.getOrders2().size());
    assertOrder("; 2 nicht da", unit2, 1);
    assertOrder("GIB 2 3 Silber", unit2, 2);

    // test receiver unit not there (without warning)
    unit.clearOrders();
    unit2.clearOrders();

    unit2.addOrder("// $cript GibWenn 2 3 Silber nie");
    parser.execute(unit.getFaction());

    assertEquals(2, unit2.getOrders2().size());
    assertOrder("; 2 nicht da", unit2, 1);
    // assertOrder("GIB 2 3 Silber", unit2, 2);

    // test receiver unit not there (with nonsense warning)
    unit.clearOrders();
    unit2.clearOrders();

    unit2.addOrder("// $cript GibWenn 2 3 Silber bla");
    parser.execute(unit.getFaction());

    assertEquals(2, unit2.getOrders2().size());
    assertError("zu viele Parameter", unit2, 1);
    // assertError("2 nicht da", unit2, 2);
    // assertOrder("GIB 2 3 Silber", unit2, 3);

    // test receiver unit not there (with nonsense warning)
    unit.clearOrders();
    unit2.clearOrders();

    unit2.addOrder("// $cript GibWenn 1 3 Silber bla");
    parser.execute(unit.getFaction());

    assertEquals(2, unit2.getOrders2().size());
    assertError("zu viele Parameter", unit2, 1);
    // assertOrder("GIB 1 3 Silber", unit2, 2);

    // test supplyer does not have item
    unit.clearOrders();
    unit2.clearOrders();

    unit2.addOrder("// $cript GibWenn 1 3 Schwert");
    parser.execute(unit.getFaction());

    assertEquals(2, unit2.getOrders2().size());
    assertError("zu wenig Schwert", unit2, 1);

    // test supplyer does not have item
    unit.clearOrders();
    unit2.clearOrders();

    unit2.addOrder("// $cript GibWenn 1 10 Silber");
    parser.execute(unit.getFaction());

    assertEquals(3, unit2.getOrders2().size());
    assertError("zu wenig Silber", unit2, 1);
    assertOrder("GIB 1 ALLES Silber", unit2, 2);

    // test supplyer does not have item
    unit.clearOrders();
    unit2.clearOrders();

    unit2.addOrder("// $cript GibWenn 1 10 Silber nie");
    parser.execute(unit.getFaction());

    assertEquals(3, unit2.getOrders2().size());
    assertMessage("zu wenig Silber", unit2, 1);
    assertOrder("GIB 1 ALLES Silber", unit2, 2);

    // test ALL
    unit.clearOrders();
    unit2.clearOrders();

    unit2.addOrder("// $cript GibWenn 1 ALLES Silber");
    parser.execute(unit.getFaction());

    assertEquals(2, unit2.getOrders2().size());
    assertOrder("GIB 1 ALLES Silber", unit2, 1);

    // test ALL
    unit.clearOrders();
    unit2.clearOrders();

    unit2.addOrder("// $cript GibWenn 1 JE ALLES Silber");
    parser.execute(unit.getFaction());

    assertEquals(2, unit2.getOrders2().size());
    assertError("JE ALLES geht nicht", unit2, 1);

  }

  /**
   * Test method for {@link E3CommandParser#commandGibWenn(String[])}.
   */
  @Test
  public final void testCommandGibWenn5() {
    E3CommandParser.ADD_NOT_THERE_INFO = true;
    // add other unit with Silber
    Unit unit2 = builder.addUnit(data, "v", "Versorger", unit.getFaction(), unit.getRegion());
    builder.addItem(data, unit2, "Silber", 5);

    // test GibWenn with error
    unit.clearOrders();
    unit2.clearOrders();

    unit2.addOrder("// $cript GibWenn ALLES Silber");
    parser.execute(unit.getFaction());

    assertEquals(1, unit.getOrders2().size());
    assertEquals(2, unit2.getOrders2().size());
    assertOrder("// $cript GibWenn ALLES Silber", unit2, 0);
    assertError("zu viele Parameter", unit2, 1);
  }

  /**
   * Test method for {@link E3CommandParser#commandGibWenn(String...)} and
   * {@link E3CommandParser#commandBenoetige(String...)}.
   */
  @Test
  public final void testCommandGibWennAndBenoetige() {
    E3CommandParser.ADD_NOT_THERE_INFO = true;
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
    assertOrder("GIB 1 3 Silber", unit2, 1);
    assertOrder("GIB 1 1 Silber", unit2, 2);

    // test with not enough silver
    unit.clearOrders();
    unit2.clearOrders();

    unit2.addOrder("// $cript GibWenn 2 3 Silber versteckt");
    unit.addOrder("// $cript Benoetige 4 Silber");
    parser.execute(unit.getFaction());

    assertEquals(4, unit2.getOrders2().size());
    assertError("braucht 2 mehr Silber", unit, 2);
    assertOrder("GIB 2 3 Silber", unit2, 2);
    assertOrder("GIB 1 2 Silber", unit2, 3);

    // test with depot
    unit.clearOrders();
    unit2.clearOrders();

    builder.addItem(data, unit2, "Würziger Wagemut", 5);
    builder.addItem(data, unit, "Silber", 20); // maintenance silver
    unit2.addOrder("// $cript GibWenn 2 Kraut versteckt");
    unit.addOrder("// $cript BerufDepotVerwalter");
    parser.execute(unit.getFaction());

    assertEquals(4, unit2.getOrders2().size());
    assertMessage("2 nicht da", unit2, 1);
    assertOrder("GIB 2 ALLES Würziger~Wagemut", unit2, 2);
    assertOrder("GIB 1 5 Silber", unit2, 3);
  }

  /**
   * Test method for {@link E3CommandParser#commandBenoetige(String...)}.
   */
  @Test
  public final void testCommandVersorge() {

    E3CommandParser.DEFAULT_SUPPLY_PRIORITY = 0;

    // test to ensure that a third unit doesn't change anything
    // add other unit with Silber
    Unit unit2 = builder.addUnit(data, "v", "Versorger", unit.getFaction(), unit.getRegion());
    builder.addItem(data, unit2, "Silber", 5);
    Unit unit3 = builder.addUnit(data, "st", "Störer", unit.getFaction(), unit.getRegion());
    builder.addItem(data, unit3, "Silber", 5);

    // no Versorge
    unit.clearOrders();
    unit2.clearOrders();
    unit3.clearOrders();
    unit.addOrder("// $cript Benoetige 4 Silber");
    parser.execute(unit.getFaction());

    assertEquals(2, unit.getOrders2().size());
    assertEquals(1, unit2.getOrders2().size());
    assertEquals(0, unit3.getOrders2().size());
    assertOrder("GIB 1 4 Silber", unit2, 0);

    // min/max
    unit.clearOrders();
    unit2.clearOrders();
    unit3.clearOrders();

    unit.addOrder("// $cript Benoetige 0 4 Silber");
    parser.execute(unit.getFaction());

    assertEquals(3, unit.getOrders2().size());
    assertEquals(0, unit2.getOrders2().size());
    assertEquals(0, unit3.getOrders2().size());

    // explicit Versorge
    unit.clearOrders();
    unit2.clearOrders();
    unit3.clearOrders();

    unit.addOrder("// $cript Benoetige 0 4 Silber");
    unit2.addOrder("// $cript Versorge 1");
    parser.execute(unit.getFaction());

    assertEquals(2, unit.getOrders2().size());
    assertEquals(2, unit2.getOrders2().size());
    assertEquals(0, unit3.getOrders2().size());
    assertOrder("GIB 1 4 Silber", unit2, 1);

    // later unit with higher priority
    unit.clearOrders();
    unit2.clearOrders();
    unit3.clearOrders();

    unit.addOrder("// $cript Benoetige 0 4 Silber");
    unit3.addOrder("// $cript Versorge 1");
    parser.execute(unit.getFaction());

    assertEquals(2, unit.getOrders2().size());
    assertEquals(0, unit2.getOrders2().size());
    assertEquals(2, unit3.getOrders2().size());
    assertOrder("GIB 1 4 Silber", unit3, 1);

    // later unit with higher priority
    unit.clearOrders();
    unit2.clearOrders();
    unit3.clearOrders();

    unit.addOrder("// $cript Benoetige 0 4 Silber");
    unit2.addOrder("// $cript Versorge 1");
    unit3.addOrder("// $cript Versorge 2");
    parser.execute(unit.getFaction());

    assertEquals(2, unit.getOrders2().size());
    assertEquals(1, unit2.getOrders2().size());
    assertEquals(2, unit3.getOrders2().size());
    assertOrder("GIB 1 4 Silber", unit3, 1);
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
    assertError("foo", unit, 1);
    assertEquals(2, unit.getOrders2().size());

    unit.clearOrders();
    unit.addOrder("// $cript +1");
    parser.execute(unit.getFaction());
    assertError("", unit, 1);

    unit.clearOrders();
    unit.addOrder("// $cript +1 ");
    parser.execute(unit.getFaction());
    assertError("", unit, 1);

    unit.clearOrders();
    unit.addOrder("// $cript +1 +foo    bar bla blubb");
    parser.execute(unit.getFaction());
    assertError("+foo    bar bla blubb", unit, 1);

    unit.clearOrders();
    unit.addOrder("// $cript +2 foo");
    parser.execute(unit.getFaction());
    assertOrder("// $cript +1 foo", unit, 1);
    assertEquals(2, unit.getOrders2().size());

    unit.clearOrders();
    unit.addOrder("// $cript +1 ; TODO bug armbrustagabe?"); // actually, this ;-comment would be
                                                             // cut away by the server
    parser.execute(unit.getFaction());
    assertError("; TODO bug armbrustagabe?", unit, 1);
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
    assertOrder("// $cript Soldat", unit2, 0);
    assertError("kein Kampftalent", unit2, 1);

    // normal operation
    builder.addSkill(unit2, "Hiebwaffen", 2);

    unit.clearOrders();
    unit2.clearOrders();
    unit2.addOrder("LERNEN Ausdauer");
    unit2.addOrder("// $cript Soldat");
    parser.execute(unit.getFaction());

    assertEquals(2, unit.getOrders2().size());
    assertOrder("GIB s 5 Schwert", unit, 1);
    assertEquals(6, unit2.getOrders2().size());
    assertOrder("// $cript Soldat", unit2, 0);
    // 1 is debug comment
    assertOrder("LERNEN Hiebwaffen", unit2, 2);
    assertOrder("RESERVIEREN 5 Schwert", unit2, 3);
    assertOrder("; braucht 10 mehr Schild", unit2, 4);
    assertOrder("; braucht 10 mehr Kettenhemd", unit2, 5);

    // normal operation
    builder.addSkill(unit2, "Hiebwaffen", 2);

    unit.clearOrders();
    unit2.clearOrders();
    unit2.addOrder("LERNEN Ausdauer");
    unit2.addOrder("// $cript Soldat best");
    parser.execute(unit.getFaction());

    assertEquals(2, unit.getOrders2().size());
    assertOrder("GIB s 5 Schwert", unit, 1);
    assertEquals(6, unit2.getOrders2().size());
    assertOrder("LERNEN Hiebwaffen", unit2, 2);
    assertOrder("RESERVIEREN 5 Schwert", unit2, 3);
    assertOrder("; braucht 10 mehr Schild", unit2, 4);
    assertOrder("; braucht 10 mehr Kettenhemd", unit2, 5);

    // normal operation
    unit.clearOrders();
    unit2.clearOrders();

    unit2.addOrder("// $cript Soldat Hiebwaffen Schwert Schild null");
    unit2.addOrder("LERNEN Ausdauer");
    parser.execute(unit.getFaction());

    assertEquals(2, unit.getOrders2().size());
    assertOrder("GIB s 5 Schwert", unit, 1);
    assertEquals(5, unit2.getOrders2().size());
    assertEquals("// $cript Soldat Hiebwaffen Schwert Schild null", unit2.getOrders2().get(0)
        .getText());
    assertOrder("LERNEN Hiebwaffen", unit2, 2);
    assertOrder("RESERVIEREN 5 Schwert", unit2, 3);
    assertError("braucht 10 mehr Schild", unit2, 4);

    // normal operation
    unit.clearOrders();
    unit2.clearOrders();

    unit2.addOrder("// $cript Soldat Hiebwaffen Schwert null best Rüstung");
    parser.execute(unit.getFaction());

    assertEquals(2, unit.getOrders2().size());
    assertOrder("GIB s 5 Schwert", unit, 1);
    assertEquals(5, unit2.getOrders2().size());
    assertOrder("LERNEN Hiebwaffen", unit2, 2);
    assertOrder("RESERVIEREN 5 Schwert", unit2, 3);
    assertError("braucht 10 mehr Kettenhemd", unit2, 4);

    // normal operation
    builder.addItem(data, unit2, "Plattenpanzer", 2);

    unit.clearOrders();
    unit2.clearOrders();

    unit2.addOrder("// $cript Soldat Hiebwaffen Schwert null best Rüstung");
    parser.execute(unit.getFaction());

    assertEquals(2, unit.getOrders2().size());
    assertOrder("GIB s 5 Schwert", unit, 1);
    assertEquals(6, unit2.getOrders2().size());
    assertOrder("LERNEN Hiebwaffen", unit2, 2);
    assertOrder("RESERVIEREN 5 Schwert", unit2, 3);
    assertOrder("RESERVIEREN 2 Plattenpanzer", unit2, 4);
    assertError("braucht 8 mehr Plattenpanzer", unit2, 5);

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

    unit2.clearOrders();
    unit2.addOrder("// $cript Soldat Talent");
    unit3.clearOrders();
    unit3.addOrder("// $cript Soldat Talent");

    parser.execute(unit.getFaction());

    assertEquals(2, unit.getOrders2().size());
    assertOrder("GIB s 10 Schwert", unit, 1);
    assertEquals(5, unit2.getOrders2().size());
    assertOrder("LERNEN Hiebwaffen", unit2, 2);
    assertOrder("RESERVIEREN JE 1 Schild", unit2, 3);
    assertOrder("; braucht 10 mehr Plattenpanzer", unit2, 4);
    assertEquals(6, unit3.getOrders2().size());
    assertOrder("LERNEN Hiebwaffen", unit3, 2);
    assertOrder("; braucht 10 mehr Schwert", unit3, 3);
    assertOrder("RESERVIEREN JE 1 Schild", unit3, 4);
    assertOrder("; braucht 10 mehr Kettenhemd", unit3, 5);

  }

  /**
   * Test method for {@link E3CommandParser#commandKontrolle(String...)}.
   */
  @Test
  public final void testCommandKontrolle() {
    unit.clearOrders();
    unit.addOrder("// $cript KrautKontrolle NO NO PAUSE SO SO PAUSE");
    parser.execute(unit.getFaction());
    assertOrder("// $cript KrautKontrolle SO SO PAUSE NO NO PAUSE", unit, 1);
    assertOrder("ROUTE NO NO PAUSE PAUSE", unit, 2);
    assertEquals(3, unit.getOrders2().size());

    unit.clearOrders();
    unit.addOrder("// $cript KrautKontrolle NO NO PAUSE SO SO PAUSE");
    unit.addOrder("FORSCHEN KRÄUTER");
    parser.execute(unit.getFaction());
    assertOrder("// $cript KrautKontrolle SO SO PAUSE NO NO PAUSE", unit, 1);
    assertOrder("ROUTE NO NO PAUSE PAUSE", unit, 2);
    assertEquals(3, unit.getOrders2().size());

    unit.clearOrders();
    unit.addOrder("// $cript KrautKontrolle NO NO PAUSE SO SO PAUSE");
    unit.addOrder("ROUTE NO NO PAUSE PAUSE");
    parser.execute(unit.getFaction());
    assertOrder("// $cript KrautKontrolle NO NO PAUSE SO SO PAUSE", unit, 1);
    assertOrder("ROUTE NO NO PAUSE PAUSE", unit, 2);
    assertEquals(3, unit.getOrders2().size());

    unit.clearOrders();
    unit.addOrder("// $cript KrautKontrolle NO NO PAUSE SO SO PAUSE");
    unit.addOrder("ROUTE PAUSE NO NO PAUSE");
    parser.execute(unit.getFaction());
    assertOrder("// $cript KrautKontrolle NO NO PAUSE SO SO PAUSE", unit, 1);
    assertOrder("FORSCHEN KRÄUTER", unit, 2);
    assertEquals(3, unit.getOrders2().size());
  }

  /**
   * Test method for {@link E3CommandParser#commandTrade(String[])}.
   */
  @Test
  public final void testCommandTrade() {
    unit.clearOrders();
    unit.addOrder("// $cript Handel 100 ALLES Talent");
    parser.execute(unit.getFaction());
    assertOrder("// $cript Handel 100 ALLES Talent", unit, 1);
    assertError("kein Handelstalent", unit, 2);

    builder.addSkill(unit, "Handeln", 10);
    builder.setPrices(unit.getRegion(), "Balsam");
    builder.addItem(data, unit, "Öl", 2);
    builder.addItem(data, unit, "Myrrhe", 200);
    unit.getRegion().setPeasants(1000);
    unit.clearOrders();
    unit.addOrder("// $cript Handel 100 ALLES Talent");
    parser.execute(unit.getFaction());
    assertOrder("// $cript Handel 100 ALLES Talent", unit, 1);
    assertOrder("KAUFEN 100 Balsam", unit, 2);
    assertError("Einheit hat zu wenig Handelstalent", unit, 3);
    assertError("braucht 3300 mehr Silber", unit, 4);
    assertEquals(5, unit.getOrders2().size());

    builder.addItem(data, unit, "Silber", 5000);
    unit.clearOrders();
    unit.addOrder("// $cript Handel 20 ALLES Talent");
    parser.execute(unit.getFaction());
    assertOrder("// $cript Handel 20 ALLES Talent", unit, 1);
    assertOrder("KAUFEN 20 Balsam", unit, 2);
    assertOrder("VERKAUFEN ALLES Myrrhe", unit, 3);
    assertOrder("VERKAUFEN 2 Öl", unit, 4);
    assertOrder("RESERVIEREN 180 Silber", unit, 5);
    assertEquals(6, unit.getOrders2().size());
  }

  /**
   * Test method for {@link E3CommandParser#commandClear(String[])}.
   */
  @Test
  public final void testCommandClear() {
    unit.clearOrders();
    unit.addOrder("// $cript Loeschen");
    unit.addOrder("Foobar");
    parser.execute(unit.getFaction());
    assertEquals(2, unit.getOrders2().size());
    assertComment(unit, 0, true, false);
    assertOrder("// $cript Loeschen", unit, 1);

    unit.clearOrders();
    unit.addOrder("Foobar");
    unit.addOrder("// $cript Loeschen");
    parser.execute(unit.getFaction());
    assertEquals(3, unit.getOrders2().size());
    assertComment(unit, 0, true, false);
    assertOrder("Foobar", unit, 1);
    assertOrder("// $cript Loeschen", unit, 2);

    unit.clearOrders();
    unit.addOrder("// $cript Loeschen lang");
    unit.addOrder("Foobar");
    unit.addOrder("LERNEN Reiten");
    unit.addOrder("; bla");
    parser.execute(unit.getFaction());
    assertEquals(4, unit.getOrders2().size());
    assertComment(unit, 0, true, false);
    assertOrder("// $cript Loeschen lang", unit, 1);
    assertOrder("Foobar", unit, 2);
    assertOrder("; bla", unit, 3);
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
    // assertOrder("; -------------------------------------", unit, 0);
    assertOrder("; TODO: foo", unit, 0);
  }

  /**
   * Test method for {@link E3CommandParser#addWarning(magellan.library.Unit, java.lang.String)} .
   */
  @Test
  public final void testCollectStatsRegion() {
    assertOrder("", unit, 0);
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
    // fail("Not yet implemented");
  }

  /**
   * Test method for {@link E3CommandParser#giveNeed(Need, boolean)} .
   */
  @Test
  public final void testGiveNeed() {

    // fail("Not yet implemented");
  }

  /**
   * Test method for {@link E3CommandParser#reserveNeed(Need, boolean)} .
   */
  @Test
  public final void testReserveNeed() {
    // fail("Not yet implemented");
  }

  /**
   * Test method for {@link E3CommandParser#detectScriptCommand(java.lang.String)} .
   */
  @Test
  public final void testDetectScriptCommand() {
    // fail("Not yet implemented");
  }

  /**
   * Test method for {@link E3CommandParser#parseScripts()}.
   */
  @Test
  public final void testParseScripts() {
    // fail("Not yet implemented");
  }

  /**
   * Test method for {@link E3CommandParser#cleanShortOrders(Faction, magellan.library.Region)}.
   */
  @Test
  public final void testCleanShort() {
    unit.clearOrders();
    unit.addOrder("// $cript Loeschen");
    unit.addOrder("// bla");
    unit.addOrder("LERNEN Segeln");
    unit.addOrder("@RESERVIEREN 10 Silber");
    unit.addOrder("; @RESERVIEREN 10 Silber");
    unit.addOrder("Foobar");
    parser.cleanShortOrders(unit.getFaction(), null);
    assertEquals(5, unit.getOrders2().size());
    assertEquals("// $cript Loeschen", unit.getOrders2().get(0).toString());
    assertEquals("// bla", unit.getOrders2().get(1).toString());
    assertEquals("LERNEN Segeln", unit.getOrders2().get(2).toString());
    assertEquals("@RESERVIEREN 10 Silber", unit.getOrders2().get(3).toString());
    // assertEquals(";; @RESERVIEREN 10 Silber", unit.getOrders2().get(4).toString());
    assertEquals(";Foobar", unit.getOrders2().get(4).toString());
  }

  /**
   * Test method for {@link E3CommandParser#markTRound(int)}.
   */
  @Test
  public final void testMarkTRoad() {
    unit.clearOrders();
    unit.addOrder("// $cript Loeschen");
    unit.addOrder("// $$L716 bla");
    unit.addOrder("// $$L717 bla");
    unit.addOrder("// $$718 bla");
    unit.addOrder("// bla");
    unit.addOrder("LERNEN Segeln");
    unit.addOrder("@RESERVIEREN 10 Silber");
    unit.addOrder("; @RESERVIEREN 10 Silber");
    unit.addOrder("Foobar");
    parser.markTRound(350);
    assertEquals(8, unit.getOrders2().size());
    assertEquals("// $cript Loeschen", unit.getOrders2().get(0).toString());
    assertEquals("// $$718 bla", unit.getOrders2().get(1).toString());
    assertEquals("// bla", unit.getOrders2().get(2).toString());
    assertEquals("LERNEN Segeln", unit.getOrders2().get(3).toString());
    assertEquals("@RESERVIEREN 10 Silber", unit.getOrders2().get(4).toString());
    assertEquals("; @RESERVIEREN 10 Silber", unit.getOrders2().get(5).toString());
    assertEquals("Foobar", unit.getOrders2().get(6).toString());
    assertEquals("// $$L351", unit.getOrders2().get(7).toString());
  }

}
