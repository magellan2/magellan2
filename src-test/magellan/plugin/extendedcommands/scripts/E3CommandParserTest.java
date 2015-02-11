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
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Locale;

import magellan.client.Client;
import magellan.client.ClientProvider;
import magellan.library.Faction;
import magellan.library.GameData;
import magellan.library.Item;
import magellan.library.MissingData;
import magellan.library.Rules;
import magellan.library.Skill;
import magellan.library.Unit;
import magellan.library.gamebinding.EresseaConstants;
import magellan.library.utils.Locales;
import magellan.library.utils.logging.Logger;
import magellan.plugin.extendedcommands.ExtendedCommandsProvider;
import magellan.test.GameDataBuilder;
import magellan.test.MagellanTestWithResources;

import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * @author stm
 */
public class E3CommandParserTest extends MagellanTestWithResources {

  private static Client client;
  private static GameDataBuilder builder;

  /**
   * @throws java.lang.Exception
   */
  @BeforeClass
  public static void setUpBeforeClass() throws Exception {
    setLocale(DE_LOCALE);
    initResources();

    Logger.setLevel(Logger.WARN);
    Logger.getInstance(E3CommandParserTest.class).info(new File(".").getAbsolutePath());

    data = new MissingData();
    client = ClientProvider.getClient(data, new File("."));
    settings.setProperty("locales.orders", DE_LOCALE.getLanguage());
    Locales.setGUILocale(DE_LOCALE);
    Locales.setOrderLocale(DE_LOCALE);

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

  protected void assertComment(Unit u, int lineNr, boolean shortComment, boolean longComment) {
    assertTrue("expected comment, but not enough orders", u.getOrders2().size() > lineNr);
    String actual = u.getOrders2().get(lineNr).getText();
    assertTrue("no long comment: " + actual, !longComment || actual.startsWith("// "));
    assertTrue("no short comment: " + actual, !shortComment || actual.startsWith(";"));
  }

  protected void assertOrder(String expected, Unit u, int lineNr) {
    assertTrue("expected " + expected + ", but not enough orders", u.getOrders2().size() > lineNr);
    String actual = u.getOrders2().get(lineNr).getText();
    if (!(actual.startsWith(expected) && (actual.length() == expected.length() || actual.substring(
        expected.length()).trim().startsWith(";")))) {
      assertEquals(expected, actual);
    }
  }

  protected void assertError(String expected, Unit u, int lineNr) {
    assertError(expected, u, lineNr, "; TODO", "(Fehler");
  }

  protected void assertWarning(String expected, Unit u, int lineNr) {
    assertError(expected, u, lineNr, "; TODO", "");
  }

  protected void assertError(String expected, Unit u, int lineNr, String prefix, String warning) {
    int min, max;
    String found = null;
    if (lineNr > 0) {
      assertTrue("expected order \"" + expected + "\", but not enough orders", u.getOrders2()
          .size() > lineNr);
      min = lineNr;
      max = lineNr;
    } else {
      assertTrue("expected order \"" + expected + "\" but unit has no orders", u.getOrders2()
          .size() > 0);
      min = 0;
      max = u.getOrders2().size() - 1;
    }

    for (int currentLine = min; currentLine <= max && found == null; ++currentLine) {
      String actual = u.getOrders2().get(currentLine).getText();
      if (actual.contains(expected) && actual.startsWith(prefix) && actual.contains(warning)) {
        found = actual;
      }
    }
    if (found == null) {
      assertEquals("; TODO: " + expected + " " + warning, found);
    }
  }

  protected void assertMessage(String expected, Unit u, int lineNr) {
    assertError(expected, u, lineNr, "; ", "");
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
    assertFalse(containsOrder(unit, "RESERVIERE 5 Silber"));
    unit.clearOrders();
    E3CommandParser.addReserveOrder(unit, "Silber", 5, false);
    assertOrder("RESERVIERE 5 Silber", unit, 0);
  }

  /**
   * Test for the Warning helper class
   *
   * @throws Exception
   */
  @Test
  public void testWarning() throws Exception {

    Warning w = new Warning(false);
    for (int individual : new int[] { E3CommandParser.C_AMOUNT, E3CommandParser.C_ARMOR,
        E3CommandParser.C_FOREIGN, E3CommandParser.C_SHIELD, E3CommandParser.C_SKILL,
        E3CommandParser.C_UNIT, E3CommandParser.C_WEAPON }) {
      assertFalse(individual + " set", w.contains(individual));
    }
    assertFalse(w.contains(E3CommandParser.W_HIDDEN));

    w = new Warning(true);
    for (int individual : new int[] { E3CommandParser.C_AMOUNT, E3CommandParser.C_ARMOR,
        E3CommandParser.C_FOREIGN, E3CommandParser.C_SHIELD, E3CommandParser.C_SKILL,
        E3CommandParser.C_UNIT, E3CommandParser.C_WEAPON }) {
      assertTrue(individual + " set", w.contains(individual));
    }
    assertFalse(w.contains(E3CommandParser.W_HIDDEN));

    w = new Warning(false);
    assertEquals(false, w.contains(E3CommandParser.C_AMOUNT));
    assertEquals(false, w.contains(E3CommandParser.C_SKILL));
    assertEquals(false, w.contains(E3CommandParser.C_HIDDEN));
    // assertEquals(E3CommandParser.C_ALWAYS, w.asInt());
    w = new Warning(false);
    String[] tokens = w.parse(new String[] {});
    assertEquals(0, tokens.length);
    assertEquals(true, w.contains(E3CommandParser.C_AMOUNT));
    assertEquals(true, w.contains(E3CommandParser.C_SKILL));
    assertEquals(false, w.contains(E3CommandParser.C_HIDDEN));
    // assertEquals(E3CommandParser.C_ALWAYS, w.asInt());
    w = new Warning(false);
    tokens = w.parse(new String[] { E3CommandParser.W_AMOUNT });
    assertEquals(true, w.contains(E3CommandParser.C_AMOUNT));
    assertEquals(false, w.contains(E3CommandParser.C_SKILL));
    assertEquals(false, w.contains(E3CommandParser.C_HIDDEN));
    // assertEquals(E3CommandParser.C_AMOUNT, w.asInt());
    w = new Warning(false);

    tokens =
        w.parse(new String[] { "Hello", "1", "2", E3CommandParser.W_SKILL, E3CommandParser.W_AMOUNT });
    assertEquals(3, tokens.length);
    assertEquals("2", tokens[2]);
    assertEquals(true, w.contains(E3CommandParser.C_AMOUNT));
    assertEquals(true, w.contains(E3CommandParser.C_SKILL));
    assertEquals(false, w.contains(E3CommandParser.C_HIDDEN));
    // assertEquals(E3CommandParser.C_AMOUNT | E3CommandParser.C_SKILL, w.asInt());
    w = new Warning(false);
    tokens = w.parse(new String[] { E3CommandParser.W_NEVER });
    assertEquals(0, tokens.length);
    assertEquals(false, w.contains(E3CommandParser.C_AMOUNT));
    assertEquals(false, w.contains(E3CommandParser.C_SKILL));
    assertEquals(false, w.contains(E3CommandParser.C_HIDDEN));
    // assertEquals(E3CommandParser.C_NEVER, w.asInt());

    w = new Warning(false);
    tokens = w.parse(new String[] { E3CommandParser.W_HIDDEN });
    assertEquals(0, tokens.length);
    assertEquals(true, w.contains(E3CommandParser.C_AMOUNT));
    assertEquals(true, w.contains(E3CommandParser.C_SKILL));
    assertEquals(true, w.contains(E3CommandParser.C_HIDDEN));
    // assertEquals(E3CommandParser.C_NEVER, w.asInt());
  }

  /**
   * Test method for the repeat command.
   */
  @Test
  public final void testCommandRepeat() {
    unit.clearOrders();
    unit.deleteAllTags();
    unit.addOrder("// $cript 2 LERNE Hiebwaffen");

    parser.execute(unit.getFaction());
    assertEquals(2, unit.getOrders2().size());
    assertOrder("// $cript 1 LERNE Hiebwaffen", unit, 1);

    unit.clearOrders();
    unit.deleteAllTags();
    unit.addOrder("// $cript 1 LERNE Hiebwaffen");

    parser.execute(unit.getFaction());
    assertEquals(2, unit.getOrders2().size());
    assertOrder("LERNE Hiebwaffen", unit, 1);

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
    assertWarning("abc nicht da", unit, 3);

    unit.clearOrders();
    unit.deleteAllTags();
    unit.addOrder("// $cript 2 5 $cript GibWenn abc 100 Silber");

    parser.execute(unit.getFaction());
    assertEquals(2, unit.getOrders2().size());
    assertOrder("// $cript 1 5 $cript GibWenn abc 100 Silber", unit, 1);

    unit.clearOrders();
    unit.deleteAllTags();
    unit.addOrder("// $cript 1 5 $cript GibWenn abc 100 Silber");

    parser.execute(unit.getFaction());
    assertEquals(4, unit.getOrders2().size());
    assertOrder("// $cript 5 5 $cript GibWenn abc 100 Silber", unit, 1);
    assertOrder("; $cript GibWenn abc 100 Silber", unit, 2);
    assertWarning("abc nicht da", unit, 3);

    unit.clearOrders();
    unit.deleteAllTags();
    unit.addOrder("// $cript 2 5 1 LERNE Hiebwaffen");

    parser.execute(unit.getFaction());
    assertEquals(2, unit.getOrders2().size());
    assertOrder("// $cript 1 5 0 LERNE Hiebwaffen", unit, 1);

    unit.clearOrders();
    unit.deleteAllTags();
    unit.addOrder("// $cript 1 5 0 LERNE Hiebwaffen");

    parser.execute(unit.getFaction());
    assertEquals(2, unit.getOrders2().size());
    assertOrder("LERNE Hiebwaffen", unit, 1);

    unit.clearOrders();
    unit.deleteAllTags();
    unit.addOrder("// $cript 2 5 0 LERNE Hiebwaffen");

    parser.execute(unit.getFaction());
    assertEquals(2, unit.getOrders2().size());
    assertOrder("", unit, 1);

    unit.clearOrders();
    unit.deleteAllTags();
    unit.addOrder("// $cript 1 1 // $cript +1 bla");

    parser.execute(unit.getFaction());
    assertEquals(3, unit.getOrders2().size());
    assertOrder("// $cript 1 1 // $cript +1 bla", unit, 1);
    assertWarning("bla", unit, 2);
  }

  /**
   * Test method for the repeat command.
   */
  @Test
  public final void testCommandRepeat2() {
    unit.clearOrders();
    unit.deleteAllTags();
    unit.addOrder("// $cript 1 1 $cript +1 Message");

    parser.execute(unit.getFaction());
    assertEquals(3, unit.getOrders2().size());
    assertWarning("Message", unit, 2);
  }

  /**
   * Test method for the repeat command.
   */
  @Test
  public final void testCommandRepeatNull() {
    unit.clearOrders();
    unit.deleteAllTags();
    unit.addOrder("// $cript 1");

    parser.execute(unit.getFaction());
    assertEquals(2, unit.getOrders2().size());
    assertOrder("", unit, 1);

    unit.clearOrders();
    unit.deleteAllTags();
    unit.addOrder("// $cript 1 1");

    parser.execute(unit.getFaction());
    assertEquals(3, unit.getOrders2().size());
    assertOrder("// $cript 1 1", unit, 1);
    assertOrder("", unit, 2);
  }

  /**
   * Test method for the auto command.
   */
  @Test
  public final void testCommandAuto() {
    unit.clearOrders();
    unit.deleteAllTags();
    unit.addOrder("// $cript auto");
    unit.addOrder("LERNE Hiebwaffen");

    parser.execute(unit.getFaction());

    assertEquals(3, unit.getOrders2().size());
    assertOrder("// $cript auto", unit, 1);
    assertOrder("LERNE Hiebwaffen", unit, 2);
    assertTrue(E3CommandParser.getProperty(unit, "confirm").equals("1"));

    unit.clearOrders();
    unit.deleteAllTags();
    unit.addOrder("LERNE Hiebwaffen");
    unit.addOrder("// $cript auto");

    parser.execute(unit.getFaction());

    assertEquals(3, unit.getOrders2().size());
    assertOrder("LERNE Hiebwaffen", unit, 1);
    assertOrder("// $cript auto", unit, 2);
    assertTrue(E3CommandParser.getProperty(unit, "confirm").equals("1"));

    // test auto nicht
    unit.clearOrders();
    unit.deleteAllTags();
    unit.addOrder("// $cript   auto   NICHT");
    unit.addOrder("LERNE Hiebwaffen");

    parser.execute(unit.getFaction());

    assertEquals(3, unit.getOrders2().size());
    assertOrder("// $cript   auto   NICHT", unit, 1);
    assertOrder("LERNE Hiebwaffen", unit, 2);
    assertTrue(E3CommandParser.getProperty(unit, "confirm").equals("0"));

    // test nicht precedemce
    unit.clearOrders();
    unit.deleteAllTags();
    unit.addOrder("// $cript   auto   NICHT");
    unit.addOrder("// $cript   auto");
    unit.addOrder("LERNE Hiebwaffen");

    parser.execute(unit.getFaction());

    assertEquals(4, unit.getOrders2().size());
    assertOrder("// $cript   auto   NICHT", unit, 1);
    assertOrder("// $cript   auto", unit, 2);
    assertOrder("LERNE Hiebwaffen", unit, 3);
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
   * Test method for {@link E3CommandParser#commandNeed(String...)}.
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
    assertOrder("RESERVIERE JE 1 Silber", unit, 2);
    assertOrder("GIB 1 2 Silber", unit2, 0);
    assertEquals(3, unit.getOrders2().size());
    assertEquals(1, unit2.getOrders2().size());
    assertOrder("GIB 1 2 Silber", unit2, 0);

    // test what happens if supplier unit already has a RESERVE order (nothing should change)
    unit.clearOrders();
    unit2.clearOrders();

    unit.addOrder("// $cript Benoetige 4 Silber");
    unit2.addOrder("RESERVIERE 4 Silber");
    unit2.getRegion().refreshUnitRelations(true);

    parser.execute(unit.getFaction());
    assertOrder("RESERVIERE JE 1 Silber", unit, 2);
    assertFalse(containsOrder(unit, "; TODO: braucht 1 mehr Silber"));
    assertOrder("RESERVIERE 4 Silber", unit2, 0);
    assertOrder("GIB 1 2 Silber", unit2, 1);

    // test what happens if Benoetige unit already has a RESERVE order (nothing should change)
    unit.clearOrders();
    unit2.clearOrders();

    unit.addOrder("// $cript Benoetige 4 Silber");
    unit.addOrder("RESERVIERE 1 Silber");

    parser.execute(unit.getFaction());
    assertOrder("RESERVIERE 1 Silber", unit, 2);
    assertOrder("GIB 1 2 Silber", unit2, 0);

    // supplyer now also has Benoetige order and cannot satisfy demand
    unit.clearOrders();
    unit2.clearOrders();

    unit.addOrder("// $cript Benoetige 4 Silber");
    unit2.addOrder("// $cript Benoetige 4 Silber");

    parser.execute(unit.getFaction());
    assertWarning("braucht 1 mehr Silber", unit, 2);
    assertOrder("RESERVIERE JE 1 Silber", unit, 3);
    assertOrder("GIB 1 1 Silber", unit2, 1);
    assertOrder("RESERVIERE 4 Silber", unit2, 2);

    // test Benoetige ALLES
    unit.clearOrders();
    unit2.clearOrders();

    unit.addOrder("// $cript Benoetige ALLES Silber");
    unit2.addOrder("// $cript Benoetige 1 2 Silber");

    parser.execute(unit.getFaction());
    // assertOrder("RESERVIERE 2 Silber", unit, 2);
    assertOrder("GIB 1 3 Silber", unit2, 1);
    assertOrder("RESERVIERE JE 1 Silber", unit2, 2);
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
    // // assertOrder("RESERVIERE 2 Silber", unit, 2);
    // assertOrder("RESERVIERE 1 Silber", unit2, 2);
    // assertOrder("GIB 1 3 Silber", unit2, 3);
    // assertSame(2, unit.getOrders2().size());
    // assertSame(4, unit2.getOrders2().size());

    // test one Benoetige order with fractional JE
    unit.clearOrders();
    unit2.clearOrders();
    unit.setPersons(20);
    unit.addOrder("// $cript Benoetige JE 0.33 Silber");
    parser.execute(unit.getFaction());

    assertEquals(3, unit.getOrders2().size()); // unit has 2 silver at this point
    assertEquals(1, unit2.getOrders2().size());
    assertOrder("GIB 1 5 Silber", unit2, 0);

  }

  /**
   * Test method for {@link E3CommandParser#commandNeed(String...)}.
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
   * Test method for {@link E3CommandParser#commandNeed(String...)}.
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
   * Test method for {@link E3CommandParser#commandNeed(String...)}.
   */
  @Test
  public final void testCommandBenoetigePrio() {
    // add other unit with Silber
    Unit unit2 = builder.addUnit(data, "v", "Versorger", unit.getFaction(), unit.getRegion());
    builder.addItem(data, unit2, "Silber", 6);

    // test conflicting
    unit.clearOrders();
    unit.addOrder("// $cript Benoetige 4 Silber");
    unit2.clearOrders();
    unit2.addOrder("// $cript Benoetige 4 Silber 101");
    parser.execute(unit.getFaction());

    assertEquals(3, unit.getOrders2().size());
    assertEquals(3, unit2.getOrders2().size());
    assertWarning("braucht 2 mehr Silber", unit, 2);
    assertOrder("GIB 1 2 Silber", unit2, 1);
    assertOrder("RESERVIERE 4 Silber", unit2, 2);

    // test conflicting
    unit.clearOrders();
    unit.addOrder("// $cript Benoetige 4 Silber");
    unit2.clearOrders();
    unit2.addOrder("// $cript Benoetige 4 Silber 99");
    parser.execute(unit.getFaction());

    assertEquals(2, unit.getOrders2().size());
    assertEquals(4, unit2.getOrders2().size());
    assertOrder("GIB 1 4 Silber", unit2, 1);
    assertWarning("braucht 2 mehr Silber", unit2, 2);
    assertOrder("RESERVIERE 2 Silber", unit2, 3);
  }

  /**
   * Test method for {@link E3CommandParser#commandNeed(String...)}.
   */
  @Test
  public final void testCommandBenoetigeWarning1() {
    // add other unit with Silber
    Unit unit2 = builder.addUnit(data, "v", "Versorger", unit.getFaction(), unit.getRegion());
    builder.addItem(data, unit2, "Silber", 6);

    // Warning should make no difference
    // test conflicting
    unit.clearOrders();
    unit.addOrder("// $cript Benoetige 4 Silber");
    unit2.clearOrders();
    unit2.addOrder("// $cript Benoetige 4 Silber 101");
    parser.execute(unit.getFaction());

    assertEquals(3, unit.getOrders2().size());
    assertEquals(3, unit2.getOrders2().size());
    assertWarning("braucht 2 mehr Silber", unit, 2);
    assertOrder("GIB 1 2 Silber", unit2, 1);
    assertOrder("RESERVIERE 4 Silber", unit2, 2);

    // test conflicting
    unit.clearOrders();
    unit.addOrder("// $cript Benoetige 4 Silber");
    unit2.clearOrders();
    unit2.addOrder("// $cript Benoetige 4 Silber 99");
    parser.execute(unit.getFaction());

    assertEquals(2, unit.getOrders2().size());
    assertEquals(4, unit2.getOrders2().size());
    assertOrder("GIB 1 4 Silber", unit2, 1);
    assertWarning("braucht 2 mehr Silber", unit2, 2);
    assertOrder("RESERVIERE 2 Silber", unit2, 3);
  }

  /**
   * Test method for {@link E3CommandParser#commandNeed(String...)}.
   */
  @Test
  public final void testCommandBenoetigeFremd() {
    // add other unit with Silber
    Unit unit2 = builder.addUnit(data, "v", "Versorger", unit.getFaction(), unit.getRegion());
    builder.addItem(data, unit2, "Silber", 6);

    // Warning in Benoetige should not parse
    // test conflicting
    unit.clearOrders();
    unit.addOrder("// $cript Benoetige 4 Silber nie");
    unit.addOrder("// $cript BenoetigeFremd v 4 Silber 101 nie");
    unit2.clearOrders();
    parser.execute(unit.getFaction());

    assertEquals(4, unit.getOrders2().size());
    assertEquals(1, unit2.getOrders2().size());
    assertMessage("Ungültige Zahl in Benoetige", unit, 2);

    // test conflicting
    unit.clearOrders();
    unit.addOrder("// $cript Benoetige 4 Silber");
    unit.addOrder("// $cript BenoetigeFremd v 4 Silber 99");
    unit2.clearOrders();
    parser.execute(unit.getFaction());

    assertEquals(3, unit.getOrders2().size());
    assertEquals(3, unit2.getOrders2().size());
    assertOrder("GIB 1 4 Silber", unit2, 0);
    assertMessage("braucht 2 mehr Silber", unit2, 1);
    assertOrder("RESERVIERE 2 Silber", unit2, 2);

    // test conflicting
    unit.clearOrders();
    unit.addOrder("// $cript Benoetige 4 Silber");
    unit.addOrder("// $cript BenoetigeFremd v 4 Silber 99 nie");
    unit2.clearOrders();
    parser.execute(unit.getFaction());

    assertEquals(3, unit.getOrders2().size());
    assertEquals(2, unit2.getOrders2().size());
    assertOrder("GIB 1 4 Silber", unit2, 0);
    assertOrder("RESERVIERE 2 Silber", unit2, 1);

    unit.clearOrders();
    unit.addOrder("// $cript BenoetigeFremd nnnn 4 Silber 99");
    unit2.clearOrders();
    parser.execute(unit.getFaction());

    assertEquals(3, unit.getOrders2().size());
    assertWarning("nnnn nicht da", unit, 2);
  }

  /**
   * Test method for {@link E3CommandParser#commandNeed(String...)}.
   */
  @Test
  public final void testCommandBenoetigeFremdOtherFaction() {
    // add other unit with Silber
    Faction faction2 = builder.addFaction(data, "otto", "Others", "Menschen", 0);
    Unit unit2 = builder.addUnit(data, "v", "Versorger", faction2, unit.getRegion());
    builder.addItem(data, unit, "Silber", 6);

    // Warning should make no difference
    // test conflicting
    unit.clearOrders();
    unit.addOrder("// $cript Benoetige 4 Silber");
    unit.addOrder("// $cript BenoetigeFremd v 4 Silber 101");
    unit2.clearOrders();

    parser.execute(unit.getFaction());
    assertEquals(7, unit.getOrders2().size());
    assertEquals(0, unit2.getOrders2().size());
    assertWarning("Einheit v gehört nicht zu uns", unit, 3);
    assertOrder("GIB v 4 Silber", unit, 4);
    assertMessage("braucht 2 mehr Silber", unit, 5);
    assertOrder("RESERVIERE 2 Silber", unit, 6);

    // Warning should make no difference
    // test conflicting
    unit.clearOrders();
    unit.addOrder("// $cript Benoetige 4 Silber");
    unit.addOrder("// $cript BenoetigeFremd v 4 Silber 101 Menge");
    unit2.clearOrders();

    parser.execute(unit.getFaction());
    assertEquals(6, unit.getOrders2().size());
    assertEquals(0, unit2.getOrders2().size());
    assertOrder("GIB v 4 Silber", unit, 3);
    assertMessage("braucht 2 mehr Silber", unit, 4);
    assertOrder("RESERVIERE 2 Silber", unit, 5);

    unit.clearOrders();
    unit.addOrder("// $cript Benoetige 4 Silber");
    unit.addOrder("// $cript BenoetigeFremd v 4 Silber 101");
    unit2.clearOrders();

    ArrayList<Faction> factions = new ArrayList<Faction>();
    factions.add(unit.getFaction());
    factions.add(unit2.getFaction());
    parser.execute(factions);
    assertEquals(6, unit.getOrders2().size());
    assertEquals(0, unit2.getOrders2().size());
    assertOrder("GIB v 4 Silber", unit, 3);
    assertMessage("braucht 2 mehr Silber", unit, 4);
    assertOrder("RESERVIERE 2 Silber", unit, 5);

  }

  /**
   * Test method for {@link E3CommandParser#commandNeed(String...)}.
   */
  @Test
  public final void testCommandBenoetigeFremdLUXUS() {
    Unit unit2 = builder.addUnit(data, "v", "Versorger", unit.getFaction(), unit.getRegion());
    Unit unit3 = builder.addUnit(data, "lag", "Lager", unit.getFaction(), unit.getRegion());
    builder.addItem(data, unit, "Balsam", 6);
    builder.addItem(data, unit, "Elfenlieb", 7);
    builder.addItem(data, unit, "Silber", 9);

    unit.clearOrders();
    unit2.clearOrders();
    unit3.clearOrders();
    unit.addOrder("// $cript BenoetigeFremd v KRAUT 91");
    parser.execute(unit.getFaction());

    assertEquals(0, unit2.getOrders2().size());
    assertEquals(0, unit3.getOrders2().size());
    assertEquals(3, unit.getOrders2().size());
    assertOrder("GIB v 7 Elfenlieb", unit, 2);

    builder.addItem(data, unit3, "Elfenlieb", 3);
    builder.addItem(data, unit3, "Schneekristall", 4);
    unit.clearOrders();
    unit2.clearOrders();
    unit3.clearOrders();
    unit.addOrder("// $cript BenoetigeFremd v KRAUT 91");
    parser.execute(unit.getFaction());

    assertEquals(0, unit2.getOrders2().size());
    assertEquals(2, unit3.getOrders2().size());
    assertEquals(3, unit.getOrders2().size());
    assertOrder("GIB v 7 Elfenlieb", unit, 2);
    assertOrder("GIB v 3 Elfenlieb", unit3, 0);
    assertOrder("GIB v 4 Schneekristall", unit3, 1);

    builder.addItem(data, unit3, "Balsam", 3);
    builder.addItem(data, unit, "Balsam", 4);
    unit.clearOrders();
    unit2.clearOrders();
    unit3.clearOrders();
    unit.addOrder("// $cript Benoetige LUXUS 90");
    unit.addOrder("// $cript BenoetigeFremd v LUXUS 91");
    parser.execute(unit.getFaction());

    assertEquals(0, unit2.getOrders2().size());
    assertEquals(4, unit.getOrders2().size());
    assertOrder("GIB v 4 Balsam", unit, 3);
    assertEquals(1, unit3.getOrders2().size());
    assertOrder("GIB v 3 Balsam", unit3, 0);

  }

  /**
   * Test method for {@link E3CommandParser#commandNeed(String...)}.
   */
  @Test
  public final void testCommandBenoetigeSimplify() {
    builder.addItem(data, unit, "Silber", 100);

    unit.clearOrders();
    unit.addOrder("// $cript Benoetige 4 Silber");
    unit.addOrder("// $cript Benoetige 4 Silber 99");
    parser.execute(unit.getFaction());

    assertEquals(4, unit.getOrders2().size());
    assertOrder("RESERVIERE 8 Silber", unit, 3);
  }

  /**
   * Test method for {@link E3CommandParser#commandEmbassador(String ...)}.
   */
  @Test
  public final void testCommandEmbassador() {
    // no skill, no money --> work
    unit.clearOrders();
    unit.addOrder("// $cript BerufBotschafter Segeln");
    unit.addOrder("LERNE Segeln");
    parser.execute(unit.getFaction());

    assertEquals(4, unit.getOrders2().size());
    assertOrder("// $cript BerufBotschafter Segeln", unit, 1);
    assertOrder("ARBEITE", unit, 2);
    assertOrder("; LERNE Segeln", unit, 3);

    // with entertain skill
    unit.clearOrders();
    unit.addOrder("// $cript BerufBotschafter Segeln");
    builder.addItem(data, unit, "Silber", 90);
    builder.addSkill(unit, "Unterhaltung", 1);
    parser.execute(unit.getFaction());

    assertEquals(3, unit.getOrders2().size());
    assertOrder("// $cript BerufBotschafter Segeln", unit, 1);
    assertOrder("UNTERHALTE", unit, 2);

    // other command
    unit.clearOrders();
    unit.addOrder("// $cript BerufBotschafter MACHE Holz");
    unit.addOrder("MACHE Stein");
    builder.addItem(data, unit, "Silber", 90);
    builder.addSkill(unit, "Unterhaltung", 1);
    parser.execute(unit.getFaction());

    assertEquals(4, unit.getOrders2().size());
    assertOrder("// $cript BerufBotschafter MACHE Holz", unit, 1);
    assertOrder("UNTERHALTE", unit, 2);
    assertOrder("; MACHE Stein", unit, 3);

    // with money --> learn default
    unit.clearOrders();
    unit.addOrder("// $cript BerufBotschafter");
    builder.addItem(data, unit, "Silber", 110);
    parser.execute(unit.getFaction());

    assertEquals(4, unit.getOrders2().size());
    assertOrder("// $cript BerufBotschafter", unit, 1);
    assertOrder("LERNE Wahrnehmung", unit, 3);

    // with money --> learn
    unit.clearOrders();
    unit.addOrder("// $cript BerufBotschafter Segeln");
    builder.addItem(data, unit, "Silber", 110);
    parser.execute(unit.getFaction());

    assertEquals(4, unit.getOrders2().size());
    assertOrder("// $cript BerufBotschafter Segeln", unit, 1);
    assertOrder("LERNE Segeln", unit, 3);

    // with money, other command
    unit.clearOrders();
    unit.addOrder("// $cript BerufBotschafter MACHE 5 Holz");
    unit.addOrder("UNTERHALTE");
    builder.addItem(data, unit, "Silber", 110);
    parser.execute(unit.getFaction());

    assertEquals(4, unit.getOrders2().size());
    assertOrder("// $cript BerufBotschafter MACHE 5 Holz", unit, 1);
    assertOrder("MACHE 5 Holz", unit, 2);
    assertOrder("; UNTERHALTE", unit, 3);
  }

  /**
   * Test method for {@link E3CommandParser#commandGiveIf(String[])}.
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
   * Test method for {@link E3CommandParser#commandGiveIf(String[])}.
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
   * Test method for {@link E3CommandParser#commandGiveIf(String[])}.
   */
  @Test
  public final void testCommandGibWenn3b() {
    // add other unit with Kraut
    Unit unit2 = builder.addUnit(data, "v", "Versorger", unit.getFaction(), unit.getRegion());
    builder.addItem(data, unit2, "Würziger Wagemut", 5);
    builder.addItem(data, unit2, "Myrrhe", 5);
    builder.addItem(data, unit2, "Wasser des Lebens", 5);

    // test one GibWenn order
    unit.clearOrders();
    unit2.clearOrders();
    unit2.addOrder("// $cript GibWenn 1 TRANK nie");

    parser.execute(unit.getFaction());
    assertEquals(2, unit2.getOrders2().size());
    assertOrder("GIB 1 ALLES Wasser~des~Lebens", unit2, 1);
  }

  /**
   * Test method for {@link E3CommandParser#commandGiveIf(String[])}.
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
   * Test method for {@link E3CommandParser#commandGiveIf(String[])}.
   */
  @Test
  public final void testCommandGibWennStupid() {
    E3CommandParser.ADD_NOT_THERE_INFO = true;
    // add other unit with Silber
    Unit unit2 = builder.addUnit(data, "v", "Versorger", unit.getFaction(), unit.getRegion());
    builder.addItem(data, unit2, "Silber", 5);

    // test one GibWenn order
    unit.clearOrders();
    unit2.clearOrders();

    unit2.addOrder("// $cript GibWenn");
    parser.execute(unit.getFaction());

    assertEquals(2, unit2.getOrders2().size());
    assertError("falsche Anzahl Argumente", unit2, 1);

  }

  /**
   * Test method for {@link E3CommandParser#commandGiveIf(String[])}.
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
    assertWarning("nicht da", unit2, 1);//
    // assertOrder("GIB 2 3 Silber", unit2, 2);

    // test receiver unit not there (with warning)
    unit.clearOrders();
    unit2.clearOrders();

    unit2.addOrder("// $cript GibWenn 2 3 Silber immer");
    parser.execute(unit.getFaction());

    assertEquals(2, unit2.getOrders2().size());
    assertWarning("nicht da", unit2, 1);
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
    assertWarning("2 nicht da", unit2, 1);
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
    assertWarning("zu wenig Schwert", unit2, 1);

    // test supplyer does not have item
    unit.clearOrders();
    unit2.clearOrders();

    unit2.addOrder("// $cript GibWenn 1 10 Silber");
    parser.execute(unit.getFaction());

    assertEquals(3, unit2.getOrders2().size());
    assertWarning("zu wenig Silber", unit2, 1);
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
   * Test method for {@link E3CommandParser#commandGiveIf(String[])}.
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
   * Test method for {@link E3CommandParser#commandGiveIf(String...)} and
   * {@link E3CommandParser#commandNeed(String...)}.
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
    assertWarning("braucht 2 mehr Silber", unit, 2);
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
   * Test method for {@link E3CommandParser#commandDepot(String[])}.
   */
  @Test
  public final void testCommandDepot() {
    E3CommandParser.DEFAULT_SUPPLY_PRIORITY = 0;

    Unit unit2 = builder.addUnit(data, "u2", "Unit 2", unit.getFaction(), unit.getRegion());
    builder.addItem(data, unit2, "Silber", 500);
    builder.addItem(data, unit, "Holz", 10);

    // test GibWenn and Benoetige
    unit.clearOrders();
    unit2.clearOrders();

    unit.addOrder("// $cript BerufDepotVerwalter 100 200");
    unit2.addOrder("// $cript Benoetige 280 Silber");
    unit2.addOrder("// $cript Benoetige 0 1 Holz");
    unit2.addOrder("// $cript Versorge 1");
    parser.execute(unit.getFaction());

    assertEquals(3, unit.getOrders2().size());
    assertOrder("GIB u2 1 Holz", unit, 2);
    assertEquals(6, unit2.getOrders2().size());
    assertOrder("GIB 1 120 Silber", unit2, 3);
    assertOrder("GIB 1 100 Silber", unit2, 4);
    assertOrder("RESERVIERE 280 Silber", unit2, 5);

  }

  /**
   * Test method for {@link E3CommandParser#commandNeed(String...)}.
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
    assertWarning("foo", unit, 1);
    assertEquals(2, unit.getOrders2().size());

    unit.clearOrders();
    unit.addOrder("// $cript +1");
    parser.execute(unit.getFaction());
    assertWarning("", unit, 1);

    unit.clearOrders();
    unit.addOrder("// $cript +1 ");
    parser.execute(unit.getFaction());
    assertWarning("", unit, 1);

    unit.clearOrders();
    unit.addOrder("// $cript +1 +foo    bar bla blubb");
    parser.execute(unit.getFaction());
    assertWarning("+foo    bar bla blubb", unit, 1);

    unit.clearOrders();
    unit.addOrder("// $cript +2 foo");
    parser.execute(unit.getFaction());
    assertOrder("// $cript +1 foo", unit, 1);
    assertEquals(2, unit.getOrders2().size());

    unit.clearOrders();
    unit.addOrder("// $cript +1 ; TODO bug armbrustagabe?"); // actually, this ;-comment would be
    // cut away by the server
    parser.execute(unit.getFaction());
    assertWarning("; TODO bug armbrustagabe?", unit, 1);
  }

  /**
   * Test method for {@link E3CommandParser#commandComment(String[])}.
   */
  @Test
  public final void testCommandComment() {
    unit.clearOrders();
    unit.addOrder("// $cript Kommentar  foo  bar");

    parser.execute(unit.getFaction());
    assertOrder(";  foo  bar", unit, 2);
  }

  /**
   * Test method for {@link E3CommandParser#commandLearn(String[])}.
   */
  @Test
  public final void testCommandLearn() {
    Unit unit2 = builder.addUnit(data, "s", "Soldat", unit.getFaction(), unit.getRegion());
    unit2.setPersons(10);

    // error: no skill
    unit.clearOrders();
    unit2.clearOrders();
    unit2.addOrder("// $cript Lerne");
    parser.execute(unit.getFaction());

    assertEquals(1, unit.getOrders2().size());
    assertEquals(2, unit2.getOrders2().size());
    assertOrder("// $cript Lerne", unit2, 0);
    assertError("falsche Anzahl Argumente", unit2, 1);

    // normal operation
    builder.addSkill(unit2, "Hiebwaffen", 2);

    // unit.clearOrders();
    // unit2.clearOrders();
    // unit2.addOrder("LERNE Ausdauer");
    // unit2.addOrder("// $cript Lerne");
    // parser.execute(unit.getFaction());
    //
    // assertEquals(3, unit2.getOrders2().size());
    // assertOrder("// $cript Lerne", unit2, 0);
    // // 1 is debug comment
    // assertOrder("LERNE Hiebwaffen", unit2, 2);

    // normal operation
    unit.clearOrders();
    unit2.clearOrders();
    unit2.addOrder("LERNE Unterhaltung");
    unit2.addOrder("// $cript Lerne Ausdauer 1");
    parser.execute(unit.getFaction());

    assertEquals(3, unit2.getOrders2().size());
    assertOrder("// $cript Lerne Ausdauer 1", unit2, 0);
    // 1 is debug comment
    assertOrder("LERNE Ausdauer", unit2, 2);

    // normal operation
    unit.clearOrders();
    unit2.clearOrders();
    unit2.addOrder("LERNE Unterhaltung");
    unit2.addOrder("// $cript Lerne Hiebwaffen 1");
    parser.execute(unit.getFaction());

    assertEquals(3, unit2.getOrders2().size());
    assertOrder("// $cript Lerne Hiebwaffen 1", unit2, 0);
    // 1 is debug comment
    assertOrder("LERNE Hiebwaffen", unit2, 2);

    // normal operation
    unit.clearOrders();
    unit2.clearOrders();
    unit2.addOrder("LERNE Hiebwaffen");
    unit2.addOrder("// $cript Lerne Ausdauer 2 Hiebwaffen 2");
    parser.execute(unit.getFaction());

    assertEquals(3, unit2.getOrders2().size());
    assertOrder("// $cript Lerne Ausdauer 2 Hiebwaffen 2", unit2, 0);
    // 1 is debug comment
    assertOrder("LERNE Ausdauer", unit2, 2);
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
    assertWarning("kein Kampftalent", unit2, 1);

    // normal operation
    builder.addSkill(unit2, "Hiebwaffen", 2);

    unit.clearOrders();
    unit2.clearOrders();
    unit2.addOrder("LERNE Ausdauer");
    unit2.addOrder("// $cript Soldat");
    parser.execute(unit.getFaction());

    assertEquals(2, unit.getOrders2().size());
    assertOrder("GIB s 5 Schwert", unit, 1);
    assertEquals(6, unit2.getOrders2().size());
    assertOrder("// $cript Soldat", unit2, 0);
    // 1 is debug comment
    assertOrder("LERNE Hiebwaffen", unit2, 2);
    assertOrder("RESERVIERE 5 Schwert", unit2, 3);
    assertOrder("; braucht 10 mehr Schild", unit2, 4);
    assertOrder("; braucht 10 mehr Kettenhemd", unit2, 5);

    // normal operation
    builder.addSkill(unit2, "Hiebwaffen", 2);

    unit.clearOrders();
    unit2.clearOrders();
    unit2.addOrder("LERNE Ausdauer");
    unit2.addOrder("// $cript Soldat best");
    parser.execute(unit.getFaction());

    assertEquals(2, unit.getOrders2().size());
    assertOrder("GIB s 5 Schwert", unit, 1);
    assertEquals(6, unit2.getOrders2().size());
    assertOrder("LERNE Hiebwaffen", unit2, 2);
    assertOrder("RESERVIERE 5 Schwert", unit2, 3);
    assertOrder("; braucht 10 mehr Schild", unit2, 4);
    assertOrder("; braucht 10 mehr Kettenhemd", unit2, 5);

    // normal operation
    unit.clearOrders();
    unit2.clearOrders();

    unit2.addOrder("// $cript Soldat Hiebwaffen Schwert Schild null");
    unit2.addOrder("LERNE Ausdauer");
    parser.execute(unit.getFaction());

    assertEquals(2, unit.getOrders2().size());
    assertOrder("GIB s 5 Schwert", unit, 1);
    assertEquals(5, unit2.getOrders2().size());
    assertEquals("// $cript Soldat Hiebwaffen Schwert Schild null", unit2.getOrders2().get(0)
        .getText());
    assertOrder("LERNE Hiebwaffen", unit2, 2);
    assertOrder("RESERVIERE 5 Schwert", unit2, 3);
    assertWarning("braucht 10 mehr Schild", unit2, 4);

    // normal operation
    unit.clearOrders();
    unit2.clearOrders();

    unit2.addOrder("// $cript Soldat Hiebwaffen Schwert null best Rüstung");
    parser.execute(unit.getFaction());

    assertEquals(2, unit.getOrders2().size());
    assertOrder("GIB s 5 Schwert", unit, 1);
    assertEquals(5, unit2.getOrders2().size());
    assertOrder("LERNE Hiebwaffen", unit2, 2);
    assertOrder("RESERVIERE 5 Schwert", unit2, 3);
    assertWarning("braucht 10 mehr Kettenhemd", unit2, 4);

    // normal operation
    builder.addItem(data, unit2, "Plattenpanzer", 2);

    unit.clearOrders();
    unit2.clearOrders();

    unit2.addOrder("// $cript Soldat Hiebwaffen Schwert null best Rüstung");
    parser.execute(unit.getFaction());

    assertEquals(2, unit.getOrders2().size());
    assertOrder("GIB s 5 Schwert", unit, 1);
    assertEquals(6, unit2.getOrders2().size());
    assertOrder("LERNE Hiebwaffen", unit2, 2);
    assertOrder("RESERVIERE 5 Schwert", unit2, 3);
    assertWarning("braucht 8 mehr Plattenpanzer", unit2, 4);
    assertOrder("RESERVIERE 2 Plattenpanzer", unit2, 5);

    // ensure that shields are not armour!
    unit.clearOrders();
    unit2.clearOrders();

    unit2.getItem(data.getRules().getItemType("Schwert")).setAmount(0);
    unit2.getItem(data.getRules().getItemType("Plattenpanzer")).setAmount(0);
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
    assertOrder("LERNE Hiebwaffen", unit2, 2);
    assertOrder("RESERVIERE JE 1 Schild", unit2, 3);
    assertOrder("; braucht 10 mehr Plattenpanzer", unit2, 4);
    assertEquals(6, unit3.getOrders2().size());
    assertOrder("LERNE Hiebwaffen", unit3, 2);
    assertOrder("; braucht 10 mehr Schwert", unit3, 3);
    assertOrder("RESERVIERE JE 1 Schild", unit3, 4);
    assertOrder("; braucht 10 mehr Kettenhemd", unit3, 5);

  }

  /**
   * Another Test method for {@link E3CommandParser#commandSoldier(String...)}.
   */
  @Test
  public final void testCommandSoldier2() {
    unit.clearOrders();
    unit.addOrder("// $cript Soldat best Hellebarde null Plattenpanzer");
    builder.addSkill(unit, "Hiebwaffen", 2);
    builder.addItem(data, unit, "Hellebarde", 1);
    builder.addItem(data, unit, "Plattenpanzer", 1);
    parser.execute(unit.getFaction());

    assertEquals(6, unit.getOrders2().size());
    // 0 is debug comment
    assertOrder("// $cript Soldat best Hellebarde null Plattenpanzer", unit, 1);
    // 2 is debug comment
    assertOrder("LERNE Hiebwaffen", unit, 3);
    assertOrder("RESERVIERE JE 1 Hellebarde", unit, 4);
    assertOrder("RESERVIERE JE 1 Plattenpanzer", unit, 5);
  }

  /**
   * Test method for {@link E3CommandParser#commandControl(String...)}.
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
    unit.addOrder("FORSCHE KRÄUTER");
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
    assertOrder("FORSCHE KRÄUTER", unit, 2);
    assertEquals(3, unit.getOrders2().size());
  }

  /**
   * Test method for {@link E3CommandParser#commandTrade(String[])}.
   */
  @Test
  public final void testCommandTrade() {
    // test missing skill
    unit.clearOrders();
    unit.addOrder("// $cript Handel 100 ALLES Talent");
    parser.execute(unit.getFaction());
    assertOrder("// $cript Handel 100 ALLES Talent", unit, 1);
    assertError("kein Handelstalent", unit, 2);

    builder.addSkill(unit, "Handeln", 10);
    builder.setPrices(unit.getRegion(), "Balsam");
    builder.addItem(data, unit, "Öl", 2);
    builder.addItem(data, unit, "Myrrhe", 200);

    // no trade because volume is 0
    unit.clearOrders();
    unit.addOrder("// $cript Handel 100 ALLES Talent");
    parser.execute(unit.getFaction());
    assertOrder("// $cript Handel 100 ALLES Talent", unit, 1);
    assertError("Kein Handel möglich", unit, 2);
    assertEquals(3, unit.getOrders2().size());

    // test missing skill and silver
    unit.getRegion().setPeasants(1000); // volume 10

    unit.clearOrders();
    unit.addOrder("// $cript Handel 100 ALLES Talent");
    parser.execute(unit.getFaction());
    assertOrder("// $cript Handel 100 ALLES Talent", unit, 1);
    assertOrder("KAUFE 88 Balsam", unit, 2);
    assertOrder("VERKAUFE ALLES Myrrhe", unit, 3);
    assertOrder("VERKAUFE 2 Öl", unit, 4);
    assertError("Einheit hat zu wenig Handelstalent", unit, 5);
    assertMessage("braucht 10 mehr Juwel", unit, 6);
    assertMessage("braucht 10 mehr Weihrauch", unit, 7);
    assertMessage("braucht 10 mehr Gewürz", unit, 8);
    assertOrder("RESERVIERE 10 Myrrhe", unit, 9);
    assertMessage("braucht 8 mehr Öl", unit, 10);
    assertOrder("RESERVIERE 2 Öl", unit, 11);
    assertMessage("braucht 10 mehr Seide", unit, 12);
    assertWarning("braucht 2592 mehr Silber", unit, 13);
    assertEquals(14, unit.getOrders2().size());

    unit.clearOrders();
    unit.addOrder("// $cript Handel 100 ALLES Menge");
    parser.execute(unit.getFaction());
    assertOrder("// $cript Handel 100 ALLES Menge", unit, 1);
    assertOrder("KAUFE 88 Balsam", unit, 2);
    // one less warning than above
    assertWarning("braucht 2592 mehr Silber", unit, 12);
    assertEquals(13, unit.getOrders2().size());

    // test normal operation
    builder.addItem(data, unit, "Silber", 5000);
    unit.clearOrders();
    unit.addOrder("// $cript Handel 20 ALLES Talent");
    parser.execute(unit.getFaction());
    assertOrder("// $cript Handel 20 ALLES Talent", unit, 1);
    assertOrder("KAUFE 20 Balsam", unit, 2);
    assertOrder("VERKAUFE ALLES Myrrhe", unit, 3);
    assertOrder("VERKAUFE 2 Öl", unit, 4);
    // 7 resources
    assertOrder("RESERVIERE 180 Silber", unit, 12);
    assertEquals(13, unit.getOrders2().size());

    // test buy amount 0
    unit.clearOrders();
    unit.addOrder("// $cript Handel 0 ALLES Talent");
    parser.execute(unit.getFaction());
    assertOrder("// $cript Handel 0 ALLES Talent", unit, 1);
    assertOrder("VERKAUFE ALLES Myrrhe", unit, 2);
    assertOrder("VERKAUFE 2 Öl", unit, 3);
    assertEquals(11, unit.getOrders2().size());

    // test normal operation
    builder.addItem(data, unit, "Silber", 20000);
    unit.clearOrders();
    unit.addOrder("// $cript Handel 45 ALLES nie");
    parser.execute(unit.getFaction());
    assertOrder("// $cript Handel 45 ALLES nie", unit, 1);
    assertOrder("KAUFE 45 Balsam", unit, 2);
    assertOrder("VERKAUFE ALLES Myrrhe", unit, 3);
    assertOrder("VERKAUFE 2 Öl", unit, 4);
    assertMessage("braucht 10 mehr Juwel", unit, 5);
    assertMessage("braucht 10 mehr Weihrauch", unit, 6);
    assertMessage("braucht 10 mehr Gewürz", unit, 7);
    assertOrder("RESERVIERE 10 Myrrhe", unit, 8);
    assertMessage("braucht 8 mehr Öl", unit, 9);
    assertOrder("RESERVIERE 2 Öl", unit, 10);
    assertMessage("braucht 10 mehr Seide", unit, 11);
    assertOrder("RESERVIERE 750 Silber", unit, 12);
    assertEquals(13, unit.getOrders2().size());

    // test explicit resources given
    builder.addItem(data, unit, "Silber", 20000);
    unit.clearOrders();
    unit.addOrder("// $cript Handel 45 Myrrhe Öl Weihrauch");
    parser.execute(unit.getFaction());
    assertOrder("// $cript Handel 45 Myrrhe Öl Weihrauch", unit, 1);
    assertOrder("KAUFE 45 Balsam", unit, 2);
    assertOrder("VERKAUFE ALLES Myrrhe", unit, 3);
    assertOrder("VERKAUFE 2 Öl", unit, 4);
    // assertOrder("VERKAUFE ALLES Weihrauch", unit, 5);
    assertOrder("RESERVIERE 10 Myrrhe", unit, 5);
    assertWarning("braucht 8 mehr Öl", unit, 6);
    assertOrder("RESERVIERE 2 Öl", unit, 7);
    assertWarning("braucht 10 mehr Weihrauch", unit, 8);
    assertOrder("RESERVIERE 750 Silber", unit, 9);
    assertEquals(10, unit.getOrders2().size());

    // test explicit resources given
    builder.addItem(data, unit, "Silber", 20000);
    unit.clearOrders();
    unit.addOrder("// $cript Handel 45 Myrrhe Öl Weihrauch Talent");
    parser.execute(unit.getFaction());
    assertOrder("// $cript Handel 45 Myrrhe Öl Weihrauch Talent", unit, 1);
    assertOrder("KAUFE 45 Balsam", unit, 2);
    assertOrder("VERKAUFE ALLES Myrrhe", unit, 3);
    assertOrder("VERKAUFE 2 Öl", unit, 4);
    // assertOrder("VERKAUFE ALLES Weihrauch", unit, 5);
    assertOrder("RESERVIERE 10 Myrrhe", unit, 5);
    assertOrder("RESERVIERE 2 Öl", unit, 6);
    assertOrder("RESERVIERE 750 Silber", unit, 7);
    assertEquals(8, unit.getOrders2().size());

  }

  /**
   * Test method for {@link E3CommandParser#commandTrade(String[])}.
   */
  @Test
  public final void testVerkaufeALLES0() {
    unit.clearOrders();
    unit.addOrder("// $cript Handel x2 Weihrauch Öl Talent");
    builder.addSkill(unit, "Handeln", 10);
    unit.getRegion().setPeasants(1000); // volume 10
    builder.setPrices(unit.getRegion(), "Balsam");
    builder.addItem(data, unit, "Weihrauch", 200);
    builder.addItem(data, unit, "Silber", 5000);

    parser.execute(unit.getFaction());
    assertOrder("KAUFE 20 Balsam", unit, 2);
    assertOrder("VERKAUFE ALLES Weihrauch", unit, 3);
    assertOrder("RESERVIERE 10 Weihrauch", unit, 4);
    assertOrder("RESERVIERE 180 Silber", unit, 5);
    assertEquals(6, unit.getOrders2().size());

  }

  /**
   * Test method for {@link E3CommandParser#commandTrade(String[])}.
   */
  @Test
  public final void testCommandTradeSellFirst() {
    // skill is low: prefer selling
    builder.addSkill(unit, "Handeln", 2);
    unit.getRegion().setPeasants(1900); // volume 19
    builder.setPrices(unit.getRegion(), "Balsam");

    builder.addItem(data, unit, "Silber", 5000);
    builder.addItem(data, unit, "Myrrhe", 500);
    unit.clearOrders();

    unit.addOrder("// $cript Handel 20 ALLES Talent");
    parser.execute(unit.getFaction());
    assertOrder("// $cript Handel 20 ALLES Talent", unit, 1);
    assertOrder("KAUFE 1 Balsam", unit, 2);
    assertOrder("VERKAUFE ALLES Myrrhe", unit, 3);
    assertError("Einheit hat zu wenig Handelstalent", unit, 4);
    assertMessage("braucht 19 mehr Juwel", unit, 5);
    assertMessage("braucht 19 mehr Weihrauch", unit, 6);
    assertMessage("braucht 19 mehr Gewürz", unit, 7);
    assertOrder("RESERVIERE 19 Myrrhe", unit, 8);
    assertMessage("braucht 19 mehr Öl", unit, 9);
    assertMessage("braucht 19 mehr Seide", unit, 10);
    assertOrder("RESERVIERE 6 Silber", unit, 11);
    assertEquals(12, unit.getOrders2().size());
  }

  /**
   * Test method for {@link E3CommandParser#commandTrade(String[])}.
   */
  @Test
  public final void testCommandTradeX() {
    // test missing skill

    builder.addSkill(unit, "Handeln", 10);
    builder.setPrices(unit.getRegion(), "Balsam");
    builder.addItem(data, unit, "Öl", 2);
    builder.addItem(data, unit, "Myrrhe", 200);
    builder.addItem(data, unit, "Silber", 20000);

    unit.getRegion().setPeasants(1000);

    unit.clearOrders();
    unit.addOrder("// $cript Handel x3 ALLES");
    parser.execute(unit.getFaction());

    assertOrder("// $cript Handel x3 ALLES", unit, 1);
    assertOrder("KAUFE 30 Balsam", unit, 2);
    assertOrder("VERKAUFE ALLES Myrrhe", unit, 3);
    assertOrder("VERKAUFE 2 Öl", unit, 4);
    assertMessage("braucht 10 mehr Juwel", unit, 5);
    assertMessage("braucht 10 mehr Weihrauch", unit, 6);
    assertMessage("braucht 10 mehr Gewürz", unit, 7);
    assertOrder("RESERVIERE 10 Myrrhe", unit, 8);
    assertMessage("braucht 8 mehr Öl", unit, 9);
    assertOrder("RESERVIERE 2 Öl", unit, 10);
    assertMessage("braucht 10 mehr Seide", unit, 11);
    assertOrder("RESERVIERE 360 Silber", unit, 12);
    assertEquals(13, unit.getOrders2().size());
  }

  /**
   * Test method for {@link E3CommandParser#commandClear(String[])}.
   */
  @Test
  public final void testCommandClear() {
    unit.clearOrders();
    unit.addOrder("// $cript Loeschen $kurz");
    unit.addOrder("Foobar");
    parser.execute(unit.getFaction());
    assertEquals(3, unit.getOrders2().size());
    assertComment(unit, 0, true, false);
    assertOrder("// $cript Loeschen $kurz", unit, 1);
    assertOrder("; Foobar", unit, 2);

    unit.clearOrders();
    unit.addOrder("Foobar");
    unit.addOrder("// $cript Loeschen $kurz");
    unit.addOrder("Foobar");
    parser.execute(unit.getFaction());
    assertEquals(4, unit.getOrders2().size());
    assertComment(unit, 0, true, false);
    assertOrder("; Foobar", unit, 1);
    assertOrder("// $cript Loeschen $kurz", unit, 2);
    assertOrder("; Foobar", unit, 3);

    unit.clearOrders();
    unit.addOrder("// $cript Loeschen");
    unit.addOrder("Foobar");
    unit.addOrder("@GIB 1 2 Silber");
    unit.addOrder("@ARBEITE");
    unit.addOrder("LERNE Reiten");
    unit.addOrder("; bla");
    parser.execute(unit.getFaction());
    assertEquals(7, unit.getOrders2().size());
    assertComment(unit, 0, true, false);
    assertOrder("// $cript Loeschen", unit, 1);
    assertOrder("Foobar", unit, 2);
    assertOrder("@GIB 1 2 Silber", unit, 3);
    assertOrder("; @ARBEITE", unit, 4);
    assertOrder("; LERNE Reiten", unit, 5);
    assertOrder("; bla", unit, 6);

    unit.clearOrders();
    unit.addOrder("Foobar");
    unit.addOrder("LERNE Reiten");
    unit.addOrder("; bla");
    unit.addOrder("// $cript Loeschen");

    parser.execute(unit.getFaction());
    assertEquals(5, unit.getOrders2().size());
    assertComment(unit, 0, true, false);
    assertOrder("Foobar", unit, 1);
    assertOrder("; LERNE Reiten", unit, 2);
    assertOrder("; bla", unit, 3);
    assertOrder("// $cript Loeschen", unit, 4);

    unit.clearOrders();
    unit.addOrder("// $cript 1 LERNE Reiten");
    unit.addOrder("// $cript Loeschen");
    unit.addOrder("// $cript 1 LERNE Reiten");

    parser.execute(unit.getFaction());
    assertEquals(4, unit.getOrders2().size());
    assertComment(unit, 0, true, false);
    assertOrder("; LERNE Reiten", unit, 1);
    assertOrder("// $cript Loeschen", unit, 2);
    assertOrder("LERNE Reiten", unit, 3);
  }

  /**
   * Test method for {@link E3CommandParser#commandClear(String[])}.
   */
  @Test
  public final void testCommandClearPrefix() {
    unit.clearOrders();
    unit.addOrder("// $cript Loeschen LERNE");
    unit.addOrder("Foobar");
    unit.addOrder("foo bars");
    unit.addOrder("LERNE Hiebwaffen");
    parser.execute(unit.getFaction());
    assertEquals(5, unit.getOrders2().size());
    assertComment(unit, 0, true, false);
    assertOrder("// $cript Loeschen LERNE", unit, 1);
    assertOrder("Foobar", unit, 2);
    assertOrder("foo bars", unit, 3);
    assertOrder("; LERNE Hiebwaffen", unit, 4);

    unit.clearOrders();
    unit.addOrder("// $cript Loeschen $kurz foo bar");
    unit.addOrder("Foobar");
    unit.addOrder("foo bars");
    parser.execute(unit.getFaction());
    assertEquals(4, unit.getOrders2().size());
    assertComment(unit, 0, true, false);
    assertOrder("// $cript Loeschen $kurz foo bar", unit, 1);
    assertOrder("Foobar", unit, 2);
    assertOrder("; foo bars", unit, 3);
  }

  /**
   * Test method for
   * {@link E3CommandParser#isUsable(magellan.library.Item, magellan.library.rules.SkillType)} .
   */
  @Test
  public final void testIsUsable() {
    assertTrue(E3CommandParser.isUsable(data.getRules().getItemType("Schwert"), data.getRules()
        .getSkillType("Hiebwaffen")));
    assertTrue(E3CommandParser.isUsable(data.getRules().getItemType("Bihänder"), data.getRules()
        .getSkillType("Hiebwaffen")));
    assertFalse(E3CommandParser.isUsable(data.getRules().getItemType("Schwert"), data.getRules()
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
  }

  /**
   * Test method for {@link E3CommandParser#getSilber(magellan.library.Unit)}.
   */
  @Test
  public final void testGetSilber() {
    assertEquals(0, E3CommandParser.getSilber(unit));
    unit.addItem(new Item(data.getRules().getItemType("Silber"), 42));
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
    unit.addOrder("// $cript Loeschen $kurz");
    unit.addOrder("// bla");
    unit.addOrder("LERNE Segeln");
    unit.addOrder("@RESERVIERE 10 Silber");
    unit.addOrder("; @RESERVIERE 10 Silber");
    unit.addOrder("Foobar");
    parser.cleanShortOrders(unit.getFaction(), null);
    assertEquals(5, unit.getOrders2().size());
    assertEquals("// $cript Loeschen $kurz", unit.getOrders2().get(0).toString());
    assertEquals("// bla", unit.getOrders2().get(1).toString());
    assertEquals("LERNE Segeln", unit.getOrders2().get(2).toString());
    assertEquals("@RESERVIERE 10 Silber", unit.getOrders2().get(3).toString());
    // assertEquals(";; @RESERVIERE 10 Silber", unit.getOrders2().get(4).toString());
    assertEquals(";Foobar", unit.getOrders2().get(4).toString());
  }

  /**
   * Test method for {@link E3CommandParser#cleanShortOrders(Faction, magellan.library.Region)}.
   */
  @Test
  public final void testCleanTwoLong() {
    unit.clearOrders();
    unit.addOrder("// $cript Loeschen $kurz");
    unit.addOrder("LERNE Segeln");
    unit.addOrder("NACH no");
    unit.addOrder("; NACH no");
    unit.addOrder("@RESERVIERE 10 Silber");
    parser.fixTwoLongOrders(unit.getFaction(), null);
    assertEquals(5, unit.getOrders2().size());
    assertOrder("// $cript Loeschen $kurz", unit, 0);
    assertOrder("LERNE Segeln", unit, 1);
    assertOrder("; NACH no", unit, 2);
    assertOrder("; NACH no", unit, 3);
    assertOrder("@RESERVIERE 10 Silber", unit, 4);
  }

  /**
   * Test method for {@link E3CommandParser#markTRound(int)}.
   */
  @Test
  public final void testMarkTRoad() {
    unit.clearOrders();
    unit.addOrder("// $cript Loeschen $kurz");
    unit.addOrder("// $$L716 bla");
    unit.addOrder("// $$L717 bla");
    unit.addOrder("// $$718 bla");
    unit.addOrder("// bla");
    unit.addOrder("LERNE Segeln");
    unit.addOrder("@RESERVIERE 10 Silber");
    unit.addOrder("; @RESERVIERE 10 Silber");
    unit.addOrder("Foobar");
    parser.markTRound(350);
    assertEquals(8, unit.getOrders2().size());
    assertEquals("// $cript Loeschen $kurz", unit.getOrders2().get(0).toString());
    assertEquals("// $$718 bla", unit.getOrders2().get(1).toString());
    assertEquals("// bla", unit.getOrders2().get(2).toString());
    assertEquals("LERNE Segeln", unit.getOrders2().get(3).toString());
    assertEquals("@RESERVIERE 10 Silber", unit.getOrders2().get(4).toString());
    assertEquals("; @RESERVIERE 10 Silber", unit.getOrders2().get(5).toString());
    assertEquals("Foobar", unit.getOrders2().get(6).toString());
    assertEquals("// $$L351", unit.getOrders2().get(7).toString());
  }

  /**
   * Test method for {@link E3CommandParser#commandRecruit(String [])}.
   */
  @Test
  public final void testCommandRecruit() {
    unit.clearOrders();
    unit.addOrder("// $cript RekrutiereMax");
    parser.execute(unit.getFaction());
    assertOrder("// $cript RekrutiereMax", unit, 1);
    assertError("Nicht genug Rekruten", unit, 2);

    unit.getRegion().setPeasants(1000); // 100 peasants = 25 recruits

    unit.clearOrders();
    unit.addOrder("// $cript RekrutiereMax");
    parser.execute(unit.getFaction());
    assertOrder("// $cript RekrutiereMax", unit, 1);
    assertOrder("REKRUTIERE 25", unit, 2);
    assertWarning("braucht 2000 mehr Silber", unit, 3);

    builder.addItem(data, unit, "Silber", 2000);

    unit.clearOrders();
    unit.addOrder("// $cript RekrutiereMax");
    parser.execute(unit.getFaction());
    assertOrder("REKRUTIERE 25", unit, 2);

    unit.clearOrders();
    unit.addOrder("// $cript RekrutiereMax 0 10");
    parser.execute(unit.getFaction());
    assertWarning("Rekrutierung fertig", unit, 2);
    assertOrder("REKRUTIERE 9", unit, 3);

    unit.clearOrders();
    unit.addOrder("// $cript RekrutiereMax 0 1");
    parser.execute(unit.getFaction());
    assertWarning("Rekrutierung fertig", unit, 2);

    // Unit unit2 = builder.addUnit(data, "zwei", unit.getRegion());
    // builder.addItem(data, unit2, "Silber", 5000);
    // unit2.addOrder("// $cript Versorge 1");

    unit.clearOrders();
    unit.addOrder("// $cript RekrutiereMax 10 100");
    parser.execute(unit.getFaction());
    assertOrder("REKRUTIERE 25", unit, 2);
    assertMessage("braucht 5920 mehr Silber", unit, 3);

  }

  /**
   * Test method for {@link E3CommandParser#commandRecruit(String [])}.
   */
  @Test
  public final void testCommandRecruitOrks() {
    unit.getRegion().setPeasants(1000); // 100 peasants = 25 recruits

    unit.clearOrders();
    unit.setPersons(344);
    unit.setRace(getRules().getRace(EresseaConstants.R_ORKS));
    unit.setRealRace(getRules().getRace(EresseaConstants.R_ORKS));
    unit.addOrder("// $cript RekrutiereMax 1 400");
    parser.execute(unit.getFaction());
    assertOrder("REKRUTIERE 50", unit, 2);
  }

  /**
   * Test method for {@link E3CommandParser#commandCollector(String[])}.
   */
  @Test
  public final void testCommandCollector() {
    data.getDate().setDate(200);

    unit.clearOrders();
    unit.addOrder("// $cript Sammler 5");
    parser.execute(unit.getFaction());
    assertOrder("FORSCHE KRÄUTER", unit, 2);

    unit.clearOrders();
    unit.addOrder("// $cript Sammler 5");
    parser.execute(unit.getFaction());
    assertOrder("FORSCHE KRÄUTER", unit, 2);

    data.getDate().setDate(201);
    unit.getRegion().setHerbAmount("viele");
    unit.clearOrders();
    unit.addOrder("// $cript Sammler 5");
    parser.execute(unit.getFaction());
    assertOrder("MACHE KRÄUTER", unit, 2);

    unit.clearOrders();
    unit.getRegion().setType(getRules().getRegionType(EresseaConstants.RT_OCEAN));
    unit.addOrder("// $cript Sammler 5");

    parser.execute(unit.getFaction());
    assertError("Sammeln nicht möglich!", unit, 2);

  }

  private Rules getRules() {
    return data.getRules();
  }

  /**
   * Test method for {@link E3CommandParser#commandEarn(String [])}.
   */
  @Test
  public final void testCommandEarn() {
    unit.getRegion().setPeasants(1000);

    // no skill
    unit.clearOrders();
    unit.addOrder("// $cript Ernaehre");
    unit.setPersons(100);

    parser.execute(unit.getFaction());
    assertEquals(3, unit.getOrders2().size());
    assertOrder("ARBEITE ", unit, 2);

    // no money in region
    unit.clearOrders();
    unit.addOrder("// $cript Ernaehre");
    builder.addSkill(unit, "Unterhaltung", 2);

    parser.execute(unit.getFaction());
    assertEquals(3, unit.getOrders2().size());
    assertOrder("ARBEITE ", unit, 2);

    // normal entertain
    unit.clearOrders();
    unit.addOrder("// $cript Ernaehre");
    unit.getRegion().setSilver(50000);

    parser.execute(unit.getFaction());
    assertEquals(3, unit.getOrders2().size());
    assertOrder("UNTERHALTE ", unit, 2);

    // tax too high
    unit.clearOrders();
    unit.addOrder("// $cript Ernaehre");
    unit.getRegion().setSilver(9999);
    builder.addSkill(unit, "Steuereintreiben", 9);
    builder.addItem(data, unit, "Schwert", 100);

    parser.execute(unit.getFaction());
    assertEquals(4, unit.getOrders2().size());
    assertOrder("TREIBE ", unit, 2);
    assertWarning("Bauern verhungern", unit, 3);

    // skill too high
    unit.clearOrders();
    unit.addOrder("// $cript Ernaehre");
    builder.addSkill(unit, "Steuereintreiben", 0);
    builder.addSkill(unit, "Unterhaltung", 20);

    parser.execute(unit.getFaction());
    assertEquals(4, unit.getOrders2().size());
    assertOrder("UNTERHALTE ", unit, 2);
    assertWarning("unterbeschäftigt", unit, 3);
  }

  /**
   * Test method for {@link E3CommandParser#commandMonitor(String[])},
   * {@link E3CommandParser#commandAllow(String[])}
   */
  @Test
  public final void testCommandUeberwache() {
    Faction faction2 = builder.addFaction(data, "otto", "Others", "Menschen", 0);
    builder.addUnit(data, "v", "Versorger", faction2, unit.getRegion());

    // unkown unit: warn
    unit.clearOrders();
    unit.addOrder("// $cript Ueberwache");

    parser.execute(unit.getFaction());
    assertWarning("unerlaubte Einheiten", unit, -1);
    assertEquals(3, unit.getOrders2().size());

    // allow unit v: no warning
    unit.clearOrders();
    unit.addOrder("// $cript Erlaube otto v");
    unit.addOrder("// $cript Ueberwache");

    parser.execute(unit.getFaction());
    assertEquals(3, unit.getOrders2().size());

    // require v: no warning
    unit.clearOrders();
    unit.addOrder("// $cript Verlange otto v");
    unit.addOrder("// $cript Ueberwache");

    parser.execute(unit.getFaction());
    assertEquals(3, unit.getOrders2().size());

    // require w: warn
    unit.clearOrders();
    unit.addOrder("// $cript Erlaube otto v");
    unit.addOrder("// $cript Verlange otto w");
    unit.addOrder("// $cript Erlaube otto x");
    unit.addOrder("// $cript Ueberwache");

    parser.execute(unit.getFaction());
    assertWarning("nicht mehr da", unit, -1);
    assertEquals(6, unit.getOrders2().size());

    // allow all: no warning
    unit.clearOrders();
    unit.addOrder("// $cript Erlaube otto ALLES");
    unit.addOrder("// $cript Ueberwache");

    parser.execute(unit.getFaction());
    assertEquals(3, unit.getOrders2().size());

  }

  /**
   *
   */
  @Test
  public void testInfinitive() {
  }
}
