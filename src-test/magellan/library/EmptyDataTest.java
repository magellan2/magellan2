package magellan.library;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.util.Locale;

import magellan.library.rules.GenericRules;
import magellan.library.utils.Locales;

import org.junit.Before;
import org.junit.Test;

public class EmptyDataTest {
  private static final Locale SOME_LOCALE = Locale.CANADA_FRENCH;

  private GameData sourceGameData;

  @Before
  public void setUp() throws Exception {
    Locales.setOrderLocale(SOME_LOCALE);
    sourceGameData = new EmptyData(new GenericRules());
    sourceGameData.setLocale(SOME_LOCALE);
  }

  @Test
  public void emptyDataGetsLocaleFromSourceGameData() {
    EmptyData target = new EmptyData(sourceGameData);
    assertThat(target.getLocale(), is(SOME_LOCALE));
  }
}
