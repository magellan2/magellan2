package magellan.test;

import java.io.File;

import org.junit.Test;

/**
 * 
 */
public class AFirstTest extends MagellanTestWithResources {


  /**
   * Just print the current directory.
   */
  @Test
  public void noTest() {
    System.err.println("Current dir: "+new File(".").getAbsolutePath());
  }
}
