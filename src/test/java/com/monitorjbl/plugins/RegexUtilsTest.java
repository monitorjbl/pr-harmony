package com.monitorjbl.plugins;

import com.google.common.collect.ImmutableList;
import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class RegexUtilsTest {
  RegexUtils regexUtils = new RegexUtils();

  @Test
  public void testSinglePattern() {
    assertTrue(regexUtils.match(ImmutableList.of("bugfix/*"), "bugfix/iss53"));
    assertFalse(regexUtils.match(ImmutableList.of("test/*"), "bugfix/iss53"));
  }

  @Test
  public void testSinglePattern_specialCharacters() {
    assertTrue(regexUtils.match(ImmutableList.of("bug.fix/*"), "bug.fix/iss53"));
    assertFalse(regexUtils.match(ImmutableList.of("te.st/*"), "bug.fix/iss53"));
  }

  @Test
  public void testMultiPattern() {
    assertTrue(regexUtils.match(ImmutableList.of("master", "bugfix/*"), "bugfix/iss53"));
    assertFalse(regexUtils.match(ImmutableList.of("master", "test/*"), "bugfix/iss53"));
  }

  @Test
  public void testExactMatch() {
    assertTrue(regexUtils.match(ImmutableList.of("bugfix/iss53"), "bugfix/iss53"));
    assertFalse(regexUtils.match(ImmutableList.of("bugfix/iss53"), "bugfix/iss54"));
  }
}
