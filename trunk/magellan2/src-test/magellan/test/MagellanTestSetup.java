package magellan.test;

import java.util.Properties;

import junit.extensions.TestSetup;
import junit.framework.TestSuite;
import magellan.client.MagellanContext;

public class MagellanTestSetup extends TestSetup {
	public MagellanTestSetup(TestSuite test) {
		super(test);
	}

	protected void setUp() {
		MagellanContext context = new MagellanContext(null);
		context.setProperties(new Properties());
		context.init();
	}

	protected void tearDown() {
	}
}
