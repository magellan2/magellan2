package magellan.test.merge;

import junit.framework.TestSuite;

public class MergeTestSuite {

  public static TestSuite suite() {
    TestSuite suite = new TestSuite("Merge Test Suite");

    suite.addTest(new TestSuite(MergeWithUnitMessages.class));
    suite.addTest(new TestSuite(WriteGameData.class));
    // suite.addTest (new TestSuite (MergeSimplestGameData.class));
    suite.addTest(new TestSuite(MergeSimpleGameData.class));
    suite.addTest(new TestSuite(MergeWithRoads.class));
    return suite;
  }
}
