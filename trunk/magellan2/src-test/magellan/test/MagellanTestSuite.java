package magellan.test;

import junit.framework.Test;
import junit.framework.TestSuite;
import magellan.test.merge.MergeTestSuite;

public class MagellanTestSuite {

	public static void main(String [] args) {
		if ((args.length > 0) && args [0].toUpperCase ().equals ("TEXT")) {
			junit.textui.TestRunner.run (MagellanTestSuite.suite ());
		} else {
			junit.swingui.TestRunner.run (MagellanTestSuite.class);
		}
	}
	
	public static Test suite() {
		TestSuite suite = new TestSuite ("Magellan Test Suite");

		//suite.addTest (ParserTestSuite.suite ());
		suite.addTest(MergeTestSuite.suite());

		return new MagellanTestSetup(suite);
	}
}
