package magellan.test.merge;

import junit.framework.Assert;
import magellan.library.GameData;
import magellan.library.GameDataMerger;
import magellan.library.Region;
import magellan.library.Unit;
import magellan.test.GameDataBuilder;
import magellan.test.MagellanTestWithResources;

import org.junit.Test;

public class MergeWithRoads extends MagellanTestWithResources {

  // public MergeWithRoads(String aName) {
  // super(aName);
  // }

  /** bugzilla bug #819 */
  @Test
  public void testLooseOldRoadInformation() throws Exception {
    GameDataBuilder builder = new GameDataBuilder();

    GameData gd1 = builder.createSimpleGameData(350);
    GameData gd2 = builder.createSimpleGameData(351);

    Region region_1_1_gd1 = builder.addRegion(gd1, "1 1", "Region_1_1", "Ebene", 2);
    builder.addUnit(gd1, "Unit_2", region_1_1_gd1);

    Region region_1_0_gd2 = builder.addRegion(gd2, "1 0", "Region_1_0", "Ebene", 2);
    builder.addUnit(gd2, "Unit_2", region_1_0_gd2);
    builder.addRoad(region_1_0_gd2, 1, 1, 100);

    GameData gd4 = GameDataMerger.merge(gd1, gd2);
    // WriteGameData.writeCR(gdMerged, gdMerged.getDate().getDate()+"_gd.cr");

    Region region_1_1_gd4 = gd4.getRegion(region_1_1_gd1.getID());
    Region region_1_0_gd4 = gd4.getRegion(region_1_0_gd2.getID());

    Assert.assertEquals(0, region_1_1_gd4.borders().size());
    Assert.assertEquals(1, region_1_0_gd4.borders().size());
  }

  @Test
  public void testKeepNewRoadInformation() throws Exception {
    GameDataBuilder builder = new GameDataBuilder();

    GameData gd1 = builder.createSimpleGameData(350);
    GameData gd2 = builder.createSimpleGameData(351);

    Region r2 = gd2.regions().values().iterator().next();
    builder.addRoad(r2, 1, 1, 100);

    GameData gd4 = GameDataMerger.merge(gd1, gd2);
    // WriteGameData.writeCR(gdMerged, gdMerged.getDate().getDate()+"_gd.cr");

    Region r4 = gd4.getRegion(r2.getID());

    Assert.assertTrue(r4 != null);
    Assert.assertEquals(1, r4.borders().size());
  }

  /** bugzilla bug #819 */
  @Test
  public void testSameRound() throws Exception {
    GameDataBuilder builder = new GameDataBuilder();

    GameData gd1 = builder.createSimpleGameData(350);
    GameData gd2 = builder.createSimpleGameData(350);

    Region r1 = gd1.regions().values().iterator().next();
    builder.addRoad(r1, 1, 1, 100);

    GameData gd4 = GameDataMerger.merge(gd1, gd2);
    // WriteGameData.writeCR(gdMerged, gdMerged.getDate().getDate()+"_gd.cr");

    Region r4 = gd4.getRegion(r1.getID());
    Assert.assertTrue(r4 != null);
    Assert.assertEquals(1, r4.borders().size());
  }

  /** bugzilla bug #819 */
  @Test
  public void testSameRoundRoadInSecondCR() throws Exception {
    GameDataBuilder builder = new GameDataBuilder();

    GameData gd1 = builder.createSimpleGameData(350);
    GameData gd2 = builder.createSimpleGameData(350);

    Region region_1_0_gd1 = builder.addRegion(gd1, "1 0", "Region_1_0", "Ebene", 2);
    builder.addUnit(gd1, "Unit_2", region_1_0_gd1);

    Region region_1_0_gd2 = builder.addRegion(gd2, "1 0", "Region_1_0", "Ebene", 2);
    builder.addUnit(gd2, "Unit_2", region_1_0_gd2);
    builder.addRoad(region_1_0_gd2, 1, 1, 100);

    GameData gd4 = GameDataMerger.merge(gd1, gd2);

    Region region_1_0_gd4 = gd4.getRegion(region_1_0_gd1.getID());
    Assert.assertEquals(1, region_1_0_gd4.borders().size());
  }

  /** bugzilla bug #819 */
  @Test
  public void testSameRoundUnitInFirstCrAndRoadInSecondCR() throws Exception {
    GameDataBuilder builder = new GameDataBuilder();

    GameData gd1 = builder.createSimpleGameData(350, false);
    GameData gd2 = builder.createSimpleGameData(350, false);

    Region region_1_1_gd1 = builder.addRegion(gd1, "1 1", "Region_1_1", "Ebene", 3);
    builder.addUnit(gd1, "Unit_2", region_1_1_gd1);

    Region region_1_1_gd2 = builder.addRegion(gd2, "1 1", "Region_1_1", "Ebene", 3);
    builder.addRoad(region_1_1_gd2, 1, 1, 100);

    GameData gd4 = GameDataMerger.merge(gd1, gd2);

    Region region_1_1_gd4 = gd4.getRegion(region_1_1_gd1.getID());
    Assert.assertEquals(1, region_1_1_gd4.units().size());
    Assert.assertEquals(1, region_1_1_gd4.borders().size());
  }

  /** bugzilla bug #819 */
  public void testSameRoundUnitInSecondCrAndRoadInFirstCR() throws Exception {
    GameDataBuilder builder = new GameDataBuilder();

    GameData gd1 = builder.createSimpleGameData(350);
    GameData gd2 = builder.createSimpleGameData(350);

    Region region_1_1_gd1 = builder.addRegion(gd1, "1 1", "Region_1_1", "Ebene", 3);
    builder.addRoad(region_1_1_gd1, 1, 1, 100);

    Region region_1_1_gd2 = builder.addRegion(gd2, "1 1", "Region_1_1", "Ebene", 3);
    Unit u2 = builder.addUnit(gd2, "Unit_2", region_1_1_gd2);
    u2.setCombatStatus(1);

    System.out.println(u2.getFaction().isPrivileged());

    GameData gd4 = GameDataMerger.merge(gd1, gd2);

    Region region_1_1_gd4 = gd4.getRegion(region_1_1_gd1.getID());
    Assert.assertEquals(1, region_1_1_gd4.units().size());
    Assert.assertEquals(0, region_1_1_gd4.borders().size());
  }

}
