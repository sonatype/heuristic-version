package net.ossindex.version;

import org.junit.Ignore;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * Test inverting ranges.
 *
 * @author Ken Duck
 */
public class InverseRangeTests
{

  @Test
  public void simpleVersionRangeInversionTest() throws InvalidRangeException {
    IVersionRange range = VersionFactory.getVersionFactory().getRange(">= 1.9.3");
    assertNotNull(range);
    assertEquals(">=1.9.3", range.toString());

    IVersionRange irange = range.invert();
    assertNotNull(irange);
    assertEquals("<1.9.3", irange.toString());


    range = VersionFactory.getVersionFactory().getRange("> 1.9.3");
    assertNotNull(range);
    assertEquals(">1.9.3", range.toString());

    irange = range.invert();
    assertNotNull(irange);
    assertEquals("<=1.9.3", irange.toString());


    range = VersionFactory.getVersionFactory().getRange("<= 1.9.3");
    assertNotNull(range);
    assertEquals("<=1.9.3", range.toString());

    irange = range.invert();
    assertNotNull(irange);
    assertEquals(">1.9.3", irange.toString());


    range = VersionFactory.getVersionFactory().getRange("< 1.9.3");
    assertNotNull(range);
    assertEquals("<1.9.3", range.toString());

    irange = range.invert();
    assertNotNull(irange);
    assertEquals(">=1.9.3", irange.toString());
  }

  @Test
  public void andRangeInversionTest() throws InvalidRangeException {
    IVersionRange range = VersionFactory.getVersionFactory().getRange("~> 1.9.3.484");
    assertNotNull(range);
    assertEquals(">=1.9.3.484 <1.9.4.0", range.toString());

    IVersionRange irange = range.invert();
    assertNotNull(irange);
    assertEquals("<1.9.3.484 | >=1.9.4.0", irange.toString());
  }

  @Test
  public void andRangeOverlapTest() throws InvalidRangeException {
    IVersionRange range = VersionFactory.getVersionFactory().getRange("~> 4.2.5, >= 4.2.5.1");
    assertNotNull(range);
    assertEquals(">=4.2.5.1 <4.3.0", range.toString());
  }


  @Test
  public void orRangeInversionTest() throws InvalidRangeException {
    IVersionRange range = VersionFactory.getVersionFactory().getRange("<1.0.0 | >2.0.0");
    assertNotNull(range);
    assertEquals("<1.0.0 | >2.0.0", range.toString());

    IVersionRange irange = range.invert();
    assertNotNull(irange);
    assertEquals(">=1.0.0 <=2.0.0", irange.toString());
  }

  @Test
  public void mergeOredRanges() throws InvalidRangeException {
    IVersionRange range1 = VersionFactory.getVersionFactory().getRange(">1.0.0");
    IVersionRange range2 = VersionFactory.getVersionFactory().getRange("<2.0.0 | >3.0.0");
    IVersionRange range3 = VersionFactory.getVersionFactory().getRange("<4.0.0 | >5.0.0");
    IVersionRange range4 = VersionFactory.getVersionFactory().getRange("<6.0.0");

    IVersionRange arange = VersionFactory.getVersionFactory().merge(range1, range2);
    assertEquals(">1.0.0 <2.0.0 | >3.0.0", arange.toString());

    arange = VersionFactory.getVersionFactory().merge(range2, range3);
    assertEquals("<2.0.0 | >3.0.0 <4.0.0 | >5.0.0", arange.toString());

    arange = VersionFactory.getVersionFactory().merge(range3, range4);
    assertEquals("<4.0.0 | >5.0.0 <6.0.0", arange.toString());

    arange = VersionFactory.getVersionFactory().merge(range1, range2, range3, range4);
    assertEquals(">1.0.0 <2.0.0 | >3.0.0 <4.0.0 | >5.0.0 <6.0.0", arange.toString());
  }

  @Test
  public void complexInversion() throws InvalidRangeException {
    IVersionRange range1 = VersionFactory.getVersionFactory().getRange("~> 1.9.3.484");
    IVersionRange range2 = VersionFactory.getVersionFactory().getRange("~> 2.0.0.353");
    IVersionRange range3 = VersionFactory.getVersionFactory().getRange(">= 2.1.0.preview.2");

    IVersionRange irange1 = range1.invert();
    IVersionRange irange2 = range2.invert();
    IVersionRange irange3 = range3.invert();

    IVersionRange merge = VersionFactory.getVersionFactory().merge(irange1, irange2, irange3);
    assertEquals("<1.9.3.484 | >=1.9.4.0 <2.0.0.353 | >=2.0.1.0 <2.1.0-preview.2", merge.toString());
  }

  /**
   * FIXME: This test should be enabled and fixed
   */
  @Test
  @Ignore
  public void betaInversion() throws InvalidRangeException {
    IVersionRange range1 = VersionFactory.getVersionFactory().getRange("~> 4.1.7");
    IVersionRange range2 = VersionFactory.getVersionFactory().getRange(">=4.2.0-beta3");
    IVersionRange irange1 = range1.invert();
    IVersionRange irange2 = range2.invert();
    IVersionRange merge = VersionFactory.getVersionFactory().merge(irange1, irange2);
    assertEquals("<4.1.7 | >4.1.99999999 <4.2.0-beta3", merge.toString());
  }
}
