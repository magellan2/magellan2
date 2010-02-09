package magellan.test;

import java.util.Properties;

import junit.extensions.TestSetup;
import junit.framework.TestSuite;
import magellan.client.MagellanContext;
import magellan.client.event.EventDispatcher;

public abstract class MagellanTestSetup extends TestSetup {

  public MagellanTestSetup() {
    super(new TestSuite());
  }

  public MagellanTestSetup(TestSuite test) {
    super(test);
  }

  @Override
  protected void setUp() {
    MagellanContext context = new MagellanContext(null);
    context.setProperties(new Properties());
    context.setEventDispatcher(new EventDispatcher());
    context.init();
  }

  @Override
  protected void tearDown() {
  }

  public void testNothing() {

  }
}
