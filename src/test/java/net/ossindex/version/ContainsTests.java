package net.ossindex.version;

import net.ossindex.version.impl.VersionSet;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * Test range parsing
 *
 * @author Ken Duck
 */
public class ContainsTests
{

  @Test
  public void testLtRange() throws InvalidRangeException
  {
    IVersionRange range = VersionFactory.getVersionFactory().getRange("<1.2.5");
    assertNotNull(range);
    assertEquals("<1.2.5", range.toString());
    assertEquals("(,1.2.5)", range.toMavenString());
    assertTrue(range.contains(VersionFactory.getVersionFactory().getVersion("1.2.4")));
  }

  @Test
  public void testLeRange() throws InvalidRangeException
  {
    IVersionRange range = VersionFactory.getVersionFactory().getRange("<=1.2.5");
    assertNotNull(range);
    assertEquals("<=1.2.5", range.toString());
    assertEquals("(,1.2.5]", range.toMavenString());
    assertTrue(range.contains(VersionFactory.getVersionFactory().getVersion("1.2.5")));
  }

  @Test
  public void testGtRange() throws InvalidRangeException
  {
    IVersionRange range = VersionFactory.getVersionFactory().getRange(">1.2.5");
    assertNotNull(range);
    assertEquals(">1.2.5", range.toString());
    assertEquals("(1.2.5,)", range.toMavenString());
    assertTrue(range.contains(VersionFactory.getVersionFactory().getVersion("2.2.6")));
  }

  @Test
  public void testGeRange() throws InvalidRangeException
  {
    IVersionRange range = VersionFactory.getVersionFactory().getRange(">=1.2.5");
    assertNotNull(range);
    assertEquals(">=1.2.5", range.toString());
    assertEquals("[1.2.5,)", range.toMavenString());
    assertTrue(range.contains(VersionFactory.getVersionFactory().getVersion("1.2.5")));
  }

  @Test
  public void testExactVersion() throws InvalidRangeException
  {
    IVersionRange range = VersionFactory.getVersionFactory().getRange("1.2.5");
    assertNotNull(range);
    assertTrue(range instanceof VersionSet);
    assertEquals("1.2.5", range.toString());
    assertEquals("[1.2.5]", range.toMavenString());
    assertTrue(range.contains(VersionFactory.getVersionFactory().getVersion("1.2.5")));
  }

  @Test
  public void testVersionSet() throws InvalidRangeException
  {
    IVersionRange range = VersionFactory.getVersionFactory().getRange("1.2.5,1.2.6,1.2.8");
    assertNotNull(range);
    assertTrue(range instanceof VersionSet);
    assertEquals("1.2.5,1.2.6,1.2.8", range.toString());
    assertEquals("[1.2.5],[1.2.6],[1.2.8]", range.toMavenString());
    assertTrue(range.contains(VersionFactory.getVersionFactory().getVersion("1.2.6")));
  }

  @Test
  public void testAndVersion() throws InvalidRangeException
  {
    IVersionRange range = VersionFactory.getVersionFactory().getRange(">1.2.5 & <1.3");
    assertNotNull(range);
    assertEquals(">1.2.5 <1.3.0", range.toString());
    assertEquals("(1.2.5,1.3.0)", range.toMavenString());
    assertTrue(range.contains(VersionFactory.getVersionFactory().getVersion("1.2.6")));
  }

  /**
   * Using a space instead of the and
   */
  @Test
  public void testSpaceAndVersion() throws InvalidRangeException
  {
    IVersionRange range = VersionFactory.getVersionFactory().getRange(">1.2.5 <1.3");
    assertNotNull(range);
    assertEquals(">1.2.5 <1.3.0", range.toString());
    assertEquals("(1.2.5,1.3.0)", range.toMavenString());
    assertTrue(range.contains(VersionFactory.getVersionFactory().getVersion("1.2.6")));
  }

  @Test
  public void testOrVersion() throws InvalidRangeException
  {
    IVersionRange range = VersionFactory.getVersionFactory().getRange("<1.2.5 | >1.3");
    assertNotNull(range);
    assertEquals("<1.2.5 | >1.3.0", range.toString());
    assertEquals("(,1.2.5),(1.3.0,)", range.toMavenString());
    assertFalse(range.contains(VersionFactory.getVersionFactory().getVersion("1.2.5")));
    assertFalse(range.contains(VersionFactory.getVersionFactory().getVersion("1.3.0")));
    assertFalse(range.contains(VersionFactory.getVersionFactory().getVersion("1.2.6")));
    assertTrue(range.contains(VersionFactory.getVersionFactory().getVersion("1.2.4")));
    assertTrue(range.contains(VersionFactory.getVersionFactory().getVersion("1.3.1")));
  }


  @Test
  public void testBracketedRange() throws InvalidRangeException
  {
    IVersionRange range = VersionFactory.getVersionFactory().getRange("(>1.2.5 & <1.3)");
    assertNotNull(range);
    assertEquals(">1.2.5 <1.3.0", range.toString());
    assertEquals("(1.2.5,1.3.0)", range.toMavenString());
    assertTrue(range.contains(VersionFactory.getVersionFactory().getVersion("1.2.6")));
    assertTrue(range.contains(VersionFactory.getVersionFactory().getVersion("1.2.9")));
    assertTrue(range.contains(VersionFactory.getVersionFactory().getVersion("1.2.99")));
    assertFalse(range.contains(VersionFactory.getVersionFactory().getVersion("1.2.5")));
    assertTrue(range.contains(VersionFactory.getVersionFactory().getVersion("1.2.5.99")));
  }

  @Test
  public void testComplexLogic() throws InvalidRangeException
  {
    IVersionRange range = VersionFactory.getVersionFactory().getRange("(>1.2.5 & <1.3) | (>2.2.5 & <2.3)");
    assertNotNull(range);
    assertEquals(">1.2.5 <1.3.0 | >2.2.5 <2.3.0", range.toString());
    assertEquals("(1.2.5,1.3.0),(2.2.5,2.3.0)", range.toMavenString());
    assertTrue(range.contains(VersionFactory.getVersionFactory().getVersion("1.2.6")));
    assertTrue(range.contains(VersionFactory.getVersionFactory().getVersion("2.2.6")));
    assertFalse(range.contains(VersionFactory.getVersionFactory().getVersion("1.2.5")));
    assertFalse(range.contains(VersionFactory.getVersionFactory().getVersion("1.3.1")));
    assertFalse(range.contains(VersionFactory.getVersionFactory().getVersion("2.3.1")));
    assertFalse(range.contains(VersionFactory.getVersionFactory().getVersion("2.4")));
    assertFalse(range.contains(VersionFactory.getVersionFactory().getVersion("bob")));
  }

  @Test
  public void testInvalidRangeWithImplicitAnd() throws InvalidRangeException
  {
    try {
      IVersionRange range = VersionFactory.getVersionFactory().getRange(">=2.10 <=2.2.4");
      fail("Expected AssertionError");
    }
    catch (InvalidRangeException e) {
      assertEquals(e.getMessage(), "Anded ranges do not intersect; this can never happen [>=2.10.0 & <=2.2.4]");
    }
  }

  @Test
  public void testInvalidRangeWithExplicitAnd() throws InvalidRangeException
  {
    try {
      IVersionRange range = VersionFactory.getVersionFactory().getRange(">=2.10 & <=2.2.4");
      fail("Expected AssertionError");
    }
    catch (InvalidRangeException e) {
      assertEquals(e.getMessage(), "Anded ranges do not intersect; this can never happen [>=2.10.0 & <=2.2.4]");
    }
  }

  @Test
  public void testComplexRangeClause() throws InvalidRangeException
  {
    IVersionRange range = VersionFactory.getVersionFactory().getRange(">=3.1.0 <3.1.4 ");
    assertEquals("[3.1.0,3.1.4)", range.toMavenString());
    assertNotNull(range);
    assertTrue(range.contains(VersionFactory.getVersionFactory().getVersion("3.1.3")));
  }

  @Test
  public void testComplexRangeSinglePipeWithBrackets() throws InvalidRangeException
  {
    IVersionRange range = VersionFactory.getVersionFactory().getRange("(>=3.0.0 <3.0.4) | (>=3.1.0 <3.1.4)");
    assertEquals("[3.0.0,3.0.4),[3.1.0,3.1.4)", range.toMavenString());
    assertNotNull(range);
    assertTrue(range.contains(VersionFactory.getVersionFactory().getVersion("3.1.3")));
  }

  @Test
  public void testComplexRangeSinglePipe() throws InvalidRangeException
  {
    IVersionRange range = VersionFactory.getVersionFactory().getRange(">=3.0.0 <3.0.4 | >=3.1.0 <3.1.4");
    assertEquals("[3.0.0,3.0.4),[3.1.0,3.1.4)", range.toMavenString());
    assertNotNull(range);
    assertTrue(range.contains(VersionFactory.getVersionFactory().getVersion("3.1.3")));
  }

  @Test
  public void testComplexRangeDoublePipe() throws InvalidRangeException
  {
    IVersionRange range = VersionFactory.getVersionFactory().getRange(">=3.0.0 <3.0.4 || >=3.1.0 <3.1.4 ");
    assertEquals("[3.0.0,3.0.4),[3.1.0,3.1.4)", range.toMavenString());
    assertNotNull(range);
    assertTrue(range.contains(VersionFactory.getVersionFactory().getVersion("3.1.3")));
  }


  @Test
  public void testComplexRange() throws InvalidRangeException
  {
    IVersionRange range = VersionFactory.getVersionFactory().getRange("<2.8.9 || >=3.0.0 <3.0.4 || >=3.1.0 <3.1.4 ");
    assertNotNull(range);
    assertEquals("(,2.8.9),[3.0.0,3.0.4),[3.1.0,3.1.4)", range.toMavenString());
    assertTrue(range.contains(VersionFactory.getVersionFactory().getVersion("3.0.0")));
    assertTrue(range.contains(VersionFactory.getVersionFactory().getVersion("3.1.0")));
    assertTrue(range.contains(VersionFactory.getVersionFactory().getVersion("3.1.3")));
    assertTrue(range.contains(VersionFactory.getVersionFactory().getVersion("1.1.3")));
    assertTrue(range.contains(VersionFactory.getVersionFactory().getVersion("3.0.3")));
    assertFalse(range.contains(VersionFactory.getVersionFactory().getVersion("3.0.4")));
    assertFalse(range.contains(VersionFactory.getVersionFactory().getVersion("2.8.9")));
    assertFalse(range.contains(VersionFactory.getVersionFactory().getVersion("3.1.4")));
  }

  @Test
  public void testLtRcRange() throws InvalidRangeException
  {
    IVersionRange range = VersionFactory.getVersionFactory().getRange("<1.2.5-rc5");
    assertNotNull(range);
    assertEquals("<1.2.5-rc5", range.toString());
    assertEquals("(,1.2.5-rc5)", range.toMavenString());
    assertTrue(range.contains(VersionFactory.getVersionFactory().getVersion("1.2.4")));
  }


  @Test
  public void testRcLtRcRange() throws InvalidRangeException
  {
    IVersionRange range = VersionFactory.getVersionFactory().getRange("<1.2.5-rc5");
    assertNotNull(range);
    assertEquals("<1.2.5-rc5", range.toString());
    assertEquals("(,1.2.5-rc5)", range.toMavenString());
    assertTrue(range.contains(VersionFactory.getVersionFactory().getVersion("1.2.5-rc4")));
  }

  @Test
  public void testNotLtRcRange() throws InvalidRangeException
  {
    IVersionRange range = VersionFactory.getVersionFactory().getRange("<1.2.5-rc5");
    assertNotNull(range);
    assertEquals("<1.2.5-rc5", range.toString());
    assertEquals("(,1.2.5-rc5)", range.toMavenString());
    assertFalse(range.contains(VersionFactory.getVersionFactory().getVersion("1.2.6")));
  }


  @Test
  public void testNotRcLtRcRange() throws InvalidRangeException
  {
    IVersionRange range = VersionFactory.getVersionFactory().getRange("<1.2.5-rc5");
    assertNotNull(range);
    assertEquals("<1.2.5-rc5", range.toString());
    assertEquals("(,1.2.5-rc5)", range.toMavenString());
    assertFalse(range.contains(VersionFactory.getVersionFactory().getVersion("1.2.5-rc6")));
  }

  @Test
  public void testLt4digitRange1() throws InvalidRangeException
  {
    IVersionRange range = VersionFactory.getVersionFactory().getRange("<1.2.5.6");
    assertNotNull(range);
    assertEquals("<1.2.5.6", range.toString());
    assertEquals("(,1.2.5.6)", range.toMavenString());
    assertTrue(range.contains(VersionFactory.getVersionFactory().getVersion("1.2.5.5")));
  }

  @Test
  public void testLt4digitRange2() throws InvalidRangeException
  {
    IVersionRange range = VersionFactory.getVersionFactory().getRange("<1.2.5.6");
    assertNotNull(range);
    assertEquals("<1.2.5.6", range.toString());
    assertEquals("(,1.2.5.6)", range.toMavenString());
    assertTrue(range.contains(VersionFactory.getVersionFactory().getVersion("1.2.5")));
  }

  @Test
  public void testLt4digitRange3() throws InvalidRangeException
  {
    IVersionRange range = VersionFactory.getVersionFactory().getRange("<1.2.5.6");
    assertNotNull(range);
    assertEquals("<1.2.5.6", range.toString());
    assertEquals("(,1.2.5.6)", range.toMavenString());
    assertTrue(range.contains(VersionFactory.getVersionFactory().getVersion("1.2.4.6")));
  }

  @Test
  public void testLte4digitRange1() throws InvalidRangeException
  {
    IVersionRange range = VersionFactory.getVersionFactory().getRange("<=1.2.5.0");
    assertNotNull(range);
    assertEquals("<=1.2.5.0", range.toString());
    assertEquals("(,1.2.5.0]", range.toMavenString());
    assertTrue(range.contains(VersionFactory.getVersionFactory().getVersion("1.2.5")));
  }

  @Test
  public void testLte4digitRange2() throws InvalidRangeException
  {
    IVersionRange range = VersionFactory.getVersionFactory().getRange("<=1.2.5.6");
    assertNotNull(range);
    assertEquals("<=1.2.5.6", range.toString());
    assertEquals("(,1.2.5.6]", range.toMavenString());
    assertTrue(range.contains(VersionFactory.getVersionFactory().getVersion("1.2.5.5")));
  }

  @Test
  public void testLte4digitRange3() throws InvalidRangeException
  {
    IVersionRange range = VersionFactory.getVersionFactory().getRange("<=1.2.5.6");
    assertNotNull(range);
    assertEquals("<=1.2.5.6", range.toString());
    assertEquals("(,1.2.5.6]", range.toMavenString());
    assertTrue(range.contains(VersionFactory.getVersionFactory().getVersion("1.2.5.6")));
  }

  @Test
  public void testNotLt4digitRange1() throws InvalidRangeException
  {
    IVersionRange range = VersionFactory.getVersionFactory().getRange("<1.2.5.6");
    assertNotNull(range);
    assertEquals("<1.2.5.6", range.toString());
    assertEquals("(,1.2.5.6)", range.toMavenString());
    assertFalse(range.contains(VersionFactory.getVersionFactory().getVersion("1.2.5.7")));
  }

  @Test
  public void testNotLt4digitRange2() throws InvalidRangeException
  {
    IVersionRange range = VersionFactory.getVersionFactory().getRange("<1.2.5.6");
    assertNotNull(range);
    assertEquals("<1.2.5.6", range.toString());
    assertEquals("(,1.2.5.6)", range.toMavenString());
    assertFalse(range.contains(VersionFactory.getVersionFactory().getVersion("1.2.5.6")));
  }

  @Test
  public void testGt4digitRange1() throws InvalidRangeException
  {
    IVersionRange range = VersionFactory.getVersionFactory().getRange(">1.2.5.6");
    assertNotNull(range);
    assertEquals(">1.2.5.6", range.toString());
    assertEquals("(1.2.5.6,)", range.toMavenString());
    assertTrue(range.contains(VersionFactory.getVersionFactory().getVersion("1.2.5.7")));
  }

  @Test
  public void testNotGt4digitRange2() throws InvalidRangeException
  {
    IVersionRange range = VersionFactory.getVersionFactory().getRange(">1.2.5.6");
    assertNotNull(range);
    assertEquals(">1.2.5.6", range.toString());
    assertEquals("(1.2.5.6,)", range.toMavenString());
    assertFalse(range.contains(VersionFactory.getVersionFactory().getVersion("1.2.5")));
  }

  @Test
  public void testNotGt4digitRange3() throws InvalidRangeException
  {
    IVersionRange range = VersionFactory.getVersionFactory().getRange(">1.2.5.6");
    assertNotNull(range);
    assertEquals(">1.2.5.6", range.toString());
    assertEquals("(1.2.5.6,)", range.toMavenString());
    assertFalse(range.contains(VersionFactory.getVersionFactory().getVersion("1.2.4.6")));
  }

  @Test
  public void testGte4digitRange1() throws InvalidRangeException
  {
    IVersionRange range = VersionFactory.getVersionFactory().getRange(">=1.2.5.0");
    assertNotNull(range);
    assertEquals(">=1.2.5.0", range.toString());
    assertEquals("[1.2.5.0,)", range.toMavenString());
    assertTrue(range.contains(VersionFactory.getVersionFactory().getVersion("1.2.5")));
  }

  @Test
  public void testGte4digitRange2() throws InvalidRangeException
  {
    IVersionRange range = VersionFactory.getVersionFactory().getRange(">=1.2.5.6");
    assertNotNull(range);
    assertEquals(">=1.2.5.6", range.toString());
    assertEquals("[1.2.5.6,)", range.toMavenString());
    assertTrue(range.contains(VersionFactory.getVersionFactory().getVersion("1.2.5.7")));
  }

  @Test
  public void testGte4digitRange3() throws InvalidRangeException
  {
    IVersionRange range = VersionFactory.getVersionFactory().getRange(">=1.2.5.6");
    assertNotNull(range);
    assertEquals(">=1.2.5.6", range.toString());
    assertEquals("[1.2.5.6,)", range.toMavenString());
    assertTrue(range.contains(VersionFactory.getVersionFactory().getVersion("1.2.5.6")));
  }

  @Test
  public void testNotGt4digitRange1() throws InvalidRangeException
  {
    IVersionRange range = VersionFactory.getVersionFactory().getRange(">1.2.5.6");
    assertNotNull(range);
    assertEquals(">1.2.5.6", range.toString());
    assertEquals("(1.2.5.6,)", range.toMavenString());
    assertFalse(range.contains(VersionFactory.getVersionFactory().getVersion("1.2.5.5")));
  }

  @Test
  public void testGt4digitRange2() throws InvalidRangeException
  {
    IVersionRange range = VersionFactory.getVersionFactory().getRange(">1.2.5.6");
    assertNotNull(range);
    assertEquals(">1.2.5.6", range.toString());
    assertEquals("(1.2.5.6,)", range.toMavenString());
    assertTrue(range.contains(VersionFactory.getVersionFactory().getVersion("1.2.5.7")));
  }

  @Test
  public void test4digitRangeWith3DigitRange() throws InvalidRangeException
  {
    IVersionRange range = VersionFactory.getVersionFactory().getRange("<1.8.0.9");
    assertNotNull(range);
    assertEquals("<1.8.0.9", range.toString());
    assertEquals("(,1.8.0.9)", range.toMavenString());
    assertFalse(range.contains(VersionFactory.getVersionFactory().getVersion("2.3.2")));
  }


  @Test
  public void testRangeContainsRelease() throws InvalidRangeException
  {
    IVersionRange range = VersionFactory.getVersionFactory()
        .getRange(">=2.5.0 <=2.5.6 || 2.5.6.SEC01 || 2.5.6.SEC02 || 2.5.7 || >=3.0.0 <3.0.3");
    assertNotNull(range);
    assertEquals(">=2.5.0 <=2.5.6 | 2.5.6-SEC01 | 2.5.6-SEC02 | 2.5.7 | >=3.0.0 <3.0.3", range.toString());
    assertEquals("[2.5.0,2.5.6],[2.5.6-SEC01],[2.5.6-SEC02],[2.5.7],[3.0.0,3.0.3)", range.toMavenString());
    assertFalse(range.contains(VersionFactory.getVersionFactory().getVersion("4.3.7.RELEASE")));
  }

  @Test
  public void testContainsWithFinalSuffix() throws InvalidRangeException
  {
    IVersionRange range = VersionFactory.getVersionFactory()
        .getRange(">=4.1.0 <4.2.1 || >=4.3.0 <4.3.2 || >=5.0.0 <5.1.2");
    assertNotNull(range);
    assertEquals(">=4.1.0 <4.2.1 | >=4.3.0 <4.3.2 | >=5.0.0 <5.1.2", range.toString());
    assertEquals("[4.1.0,4.2.1),[4.3.0,4.3.2),[5.0.0,5.1.2)", range.toMavenString());
    assertFalse(range.contains(VersionFactory.getVersionFactory().getVersion("4.3.2")));
    assertFalse(range.contains(VersionFactory.getVersionFactory().getVersion("4.3.2.Final")));
    assertFalse(range.contains(VersionFactory.getVersionFactory().getVersion("4.3.2.RELEASE")));
  }

  @Test
  public void testUnarySetContainsWithFinalSuffix() throws InvalidRangeException
  {
    IVersionRange range = VersionFactory.getVersionFactory().getRange("4.3.2");
    assertNotNull(range);
    assertEquals("4.3.2", range.toString());
    assertEquals("[4.3.2]", range.toMavenString());
    assertTrue(range.contains(VersionFactory.getVersionFactory().getVersion("4.3.2")));
    assertTrue(range.contains(VersionFactory.getVersionFactory().getVersion("4.3.2.Final")));
    assertTrue(range.contains(VersionFactory.getVersionFactory().getVersion("4.3.2.RELEASE")));
  }

  @Test
  public void testUnaryRangeContainsWithFinalSuffix() throws InvalidRangeException
  {
    IVersionRange range = VersionFactory.getVersionFactory().getRange("<=4.3.2");
    assertNotNull(range);
    assertEquals("<=4.3.2", range.toString());
    assertEquals("(,4.3.2]", range.toMavenString());
    assertTrue(range.contains(VersionFactory.getVersionFactory().getVersion("4.3.2")));
    assertTrue(range.contains(VersionFactory.getVersionFactory().getVersion("4.3.2.Final")));
    assertTrue(range.contains(VersionFactory.getVersionFactory().getVersion("4.3.2.RELEASE")));
  }

  @Test
  public void testUnaryFinalSetContainsWithFinalSuffix() throws InvalidRangeException
  {
    IVersionRange range = VersionFactory.getVersionFactory().getRange("4.3.2-Final");
    assertNotNull(range);
    assertEquals("4.3.2", range.toString());
    assertEquals("[4.3.2]", range.toMavenString());
    assertTrue(range.contains(VersionFactory.getVersionFactory().getVersion("4.3.2")));
    assertTrue(range.contains(VersionFactory.getVersionFactory().getVersion("4.3.2.Final")));
    assertTrue(range.contains(VersionFactory.getVersionFactory().getVersion("4.3.2.RELEASE")));
  }

  @Test
  public void testBinaryFinalSetContainsWithFinalSuffix() throws InvalidRangeException
  {
    IVersionRange range = VersionFactory.getVersionFactory().getRange("4.3.2-Final | 4.3.1-GA");
    assertNotNull(range);
    assertEquals("4.3.1 | 4.3.2", range.toString());
    assertEquals("[4.3.1],[4.3.2]", range.toMavenString());
    assertTrue(range.contains(VersionFactory.getVersionFactory().getVersion("4.3.2")));
    assertTrue(range.contains(VersionFactory.getVersionFactory().getVersion("4.3.2.Final")));
    assertTrue(range.contains(VersionFactory.getVersionFactory().getVersion("4.3.2.RELEASE")));
  }

  @Test
  public void testOpenEndedContainsClosed() throws InvalidRangeException
  {
    IVersionRange range1 = VersionFactory.getVersionFactory().getRange("<1.10.10");
    assertNotNull(range1);
    IVersionRange range2 = VersionFactory.getVersionFactory().getRange(">=1.10.1 <1.10.10");
    assertTrue(range1.contains(range2));
  }
}
