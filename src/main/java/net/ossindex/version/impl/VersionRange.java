/**
 * Copyright (c) 2016 Vör Security Inc.
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * * Redistributions of source code must retain the above copyright
 * notice, this list of conditions and the following disclaimer.
 * * Redistributions in binary form must reproduce the above copyright
 * notice, this list of conditions and the following disclaimer in the
 * documentation and/or other materials provided with the distribution.
 * * Neither the name of the <organization> nor the
 * names of its contributors may be used to endorse or promote products
 * derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL <COPYRIGHT HOLDER> BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package net.ossindex.version.impl;

import java.util.Collection;

import net.ossindex.version.IVersion;
import net.ossindex.version.IVersionRange;

/**
 * A bounded version range is capped at both ends, for example 1.2.5 - 1.2.8
 *
 * @author Ken Duck
 */
public class VersionRange
    extends AbstractCommonRange
{
  private String type;

  /**
   *
   */
  private String operator;

  private SemanticVersion version;

  private boolean hasErrors = false;

  /**
   *
   * @param operator
   * @param version
   */
  public VersionRange(String operator, SemanticVersion version)
  {
    this.version = version;
    this.operator = operator;
  }

  /*
   * (non-Javadoc)
   * @see net.ossindex.version.IVersionRange#contains(net.ossindex.version.IVersion)
   */
  @Override
  public boolean contains(IVersion version)
  {

    // Can the versions be compared?
    if (!(version instanceof SemanticVersion)) {
      return false;
    }

    // We always want the extended version on the left, since it has the
    // more complex code.
    if (this.version instanceof ExtendedSemanticVersion) {
      switch (operator) {
        case ">":
          return this.version.lessThan(version);
        case ">=":
          return this.version.lessThanOrEqualTo(version);
        case "<":
          return this.version.greaterThan(version);
        case "<=":
          return this.version.greaterThanOrEqualTo(version);
        default:
          throw new IllegalArgumentException("Invalid operator: " + operator);
      }
    }
    else {
      switch (operator) {
        case "<":
          return ((SemanticVersion) version).lessThan(this.version);
        case "<=":
          return ((SemanticVersion) version).lessThanOrEqualTo(this.version);
        case ">":
          return ((SemanticVersion) version).greaterThan(this.version);
        case ">=":
          return ((SemanticVersion) version).greaterThanOrEqualTo(this.version);
        default:
          throw new IllegalArgumentException("Invalid operator: " + operator);
      }
    }
  }

  /*
   * (non-Javadoc)
   * @see net.ossindex.version.impl.IVersionRange#isAtomic()
   */
  @Override
  public boolean isDiscrete()
  {
    if (operator == null) {
      return true;
    }
    return false;
  }

  /*
   * (non-Javadoc)
   * @see net.ossindex.version.impl.IVersionRange#isSimple()
   */
  @Override
  public boolean isSimple()
  {
    return true;
  }

  /*
   * (non-Javadoc)
   * @see net.ossindex.version.impl.IVersionRange#getMinimum()
   */
  @Override
  public IVersion getMinimum()
  {
    switch (operator) {
      case "<":
      case "<=":
        return new SemanticVersion(0);
      case ">":
        // FIXME: Not quite correct. We need to increment the version.
        return version;
      case ">=":
        return version;
      default:
        throw new IllegalArgumentException("Invalid operator: " + operator);
    }
  }

  /*
   * (non-Javadoc)
   * @see net.ossindex.version.impl.IVersionRange#getMaximum()
   */
  @Override
  public IVersion getMaximum()
  {
    switch (operator) {
      case "<":
        return version.getPrevVersion();
      case "<=":
        return version;
      case ">":
        return version.getNextVersion();
      case ">=":
        return null;
      default:
        throw new IllegalArgumentException("Invalid operator: " + operator);
    }
  }

  /*
   * (non-Javadoc)
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString()
  {
    return operator + version;
  }

  /*
   * (non-Javadoc)
   * @see net.ossindex.version.impl.IVersionRange#toMavenString()
   */
  @Override
  public String toMavenString()
  {
    String mavenString = "";
    switch (operator) {
      case "<=":
        mavenString = "(,%s]";
        break;
      case "<":
        mavenString = "(,%s)";
        break;
      case "==":
        mavenString = "[%s]";
        break;
      case ">=":
        mavenString = "[%s,)";
        break;
      case ">":
        mavenString = "(%s,)";
        break;
      default:
        toString();
    }
    return String.format(mavenString, version);
  }

  /*
   * (non-Javadoc)
   * @see net.ossindex.version.IVersionRange#intersects(net.ossindex.version.IVersionRange)
   */
  @Override
  public boolean intersects(IVersionRange yourRange)
  {
    // If the supplied set is a version set, let it deal with it
    if (yourRange instanceof VersionSet) {
      return yourRange.intersects(this);
    }
    // If the other range is a simple range, then we can just check the extremities
    // to see if they overlap
    else if (yourRange instanceof VersionRange) {
      IVersion useMyVersion = null;
      switch (operator) {
        case ">":
          useMyVersion = version.getNextVersion();
          break;
        case "<":
          useMyVersion = version.getPrevVersion();
          break;
        default:
          useMyVersion = version;
          break;
      }
      if (yourRange.contains(useMyVersion)) {
        return true;
      }

      IVersion useYourVersion = null;
      switch (((VersionRange) yourRange).operator) {
        case ">":
          useYourVersion = ((VersionRange) yourRange).version.getNextVersion();
          break;
        case "<":
          useYourVersion = ((VersionRange) yourRange).version.getPrevVersion();
          break;
        default:
          useYourVersion = ((VersionRange) yourRange).version;
          break;
      }
      if (this.contains(useYourVersion)) {
        return true;
      }

      // Special case: Versions right next to each other >1.2.6 & <1.2.7
      // There may be micro-versions or snapshot versions between them
      if (this.version.equals(useYourVersion) && ((VersionRange) yourRange).version.equals(useMyVersion)) {
        return true;
      }
    }
    // Logical ranges need to be broken down
    else if (yourRange instanceof AndRange) {
      return yourRange.intersects(this);
    }
    else if (yourRange instanceof OrRange) {
      return yourRange.intersects(this);
    }
    else {
      throw new UnsupportedOperationException();
    }
    return false;
  }

  @Override
  public boolean contains(IVersionRange yourRange) {

    // If your range is a version set, each version should be contained in
    // our range.
    if (yourRange instanceof VersionSet) {
      Collection<IVersion> versions = ((VersionSet) yourRange).getVersions();
      for (IVersion version : versions) {
        if (!this.contains(version)) {
          return false;
        }
      }
      return true;
    }

    // If the other range is a simple range, then we can just check the extremities
    // to see if they are contained
    else if (yourRange instanceof VersionRange) {
      IVersion minimum = yourRange.getMinimum();

      if (!this.contains(minimum)) {
        return false;
      }

      IVersion maximum = yourRange.getMaximum();
      if (maximum != null) {
        if (!this.contains(maximum)) {
          return false;
        }
      }
      else {
        IVersion myMaximum = getMaximum();
        if (myMaximum != null) {
          return false;
        }
      }
      return true;
    }

    // We should contain both end points of an AndRange
    else if (yourRange instanceof AndRange) {
      if (yourRange.isSimple()) {
        IVersion min = yourRange.getMinimum();
        IVersion max = yourRange.getMaximum();
        return contains(min) && contains(max);
      }
    }

    // Unhandled situation
    return false;
  }

  /*
   * (non-Javadoc)
   * @see net.ossindex.version.IVersionRange#getSimplifiedRange()
   */
  @Override
  public IVersionRange getSimplifiedRange() {
    return this;
  }

  public String getType() {
    return type;
  }

  public void setType(String type) {
    this.type = type;
  }

  public void setHasErrors(boolean b) {
    hasErrors = b;
  }

  public boolean hasErrors() {
    return hasErrors;
  }

  /**
   * An unbounded range goes to infinity
   */
  public boolean isUnbounded() {
    switch (operator) {
      case ">":
      case ">=":
        return true;
      default:
        return false;
    }
  }

  /*
   * (non-Javadoc)
   * @see net.ossindex.version.impl.AbstractCommonRange#invert()
   */
  @Override
  public IVersionRange invert() {
    switch (operator) {
      case ">":
        return new VersionRange("<=", version);
      case ">=":
        return new VersionRange("<", version);
      case "<":
        return new VersionRange(">=", version);
      case "<=":
        return new VersionRange(">", version);
      default:
        throw new UnsupportedOperationException();
    }
  }

}
