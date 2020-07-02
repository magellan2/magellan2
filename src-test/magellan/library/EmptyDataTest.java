package magellan.library;

import static org.junit.Assert.assertEquals;

import java.util.Locale;

import org.junit.Before;
import org.junit.Test;

import magellan.library.rules.GenericRules;
import magellan.library.utils.Locales;
import magellan.test.MagellanTestWithResources;

public class EmptyDataTest extends MagellanTestWithResources {
  private static final Locale SOME_LOCALE = Locale.CANADA_FRENCH;

  private GameData sourceGameData;

  /**
   * @throws Exception
   */
  @Before
  public void setUp() throws Exception {
    Locales.setOrderLocale(SOME_LOCALE);
    sourceGameData = new EmptyData(new GenericRules());
    sourceGameData.setLocale(SOME_LOCALE);
  }

  /**
   * 
   */
  @Test
  public void emptyDataGetsLocaleFromSourceGameData() {
    EmptyData target = new EmptyData(sourceGameData);
    assertEquals(SOME_LOCALE, target.getLocale());
  }
}
