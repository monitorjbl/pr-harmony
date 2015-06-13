package com.monitorjbl.plugins;

import org.junit.Test;

import static com.google.common.collect.Lists.newArrayList;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

public class RegexUtilsTest {
  RegexUtils sut = new RegexUtils();

  @Test
  public void testSinglePattern() throws Exception {
    assertTrue(sut.match(newArrayList("bugfix/*"), "bugfix/iss53"));
    assertFalse(sut.match(newArrayList("test/*"), "bugfix/iss53"));
  }

  @Test
  public void testSinglePattern_specialCharacters() throws Exception {
    assertTrue(sut.match(newArrayList("bug.fix/*"), "bug.fix/iss53"));
    assertFalse(sut.match(newArrayList("te.st/*"), "bug.fix/iss53"));
  }

  @Test
  public void testMultiPattern() throws Exception {
    assertTrue(sut.match(newArrayList("master", "bugfix/*"), "bugfix/iss53"));
    assertFalse(sut.match(newArrayList("master", "test/*"), "bugfix/iss53"));
  }

  @Test
  public void testExactMatch() throws Exception {
    assertTrue(sut.match(newArrayList("bugfix/iss53"), "bugfix/iss53"));
    assertFalse(sut.match(newArrayList("bugfix/iss53"), "bugfix/iss54"));
  }

  @Test
  public void testFormatBranchName() throws Exception {
    assertThat(sut.formatBranchName("refs/heads/master"), equalTo("master"));
  }
}
