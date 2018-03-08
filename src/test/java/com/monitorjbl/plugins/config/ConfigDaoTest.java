package com.monitorjbl.plugins.config;

import com.atlassian.bitbucket.user.UserService;
import com.atlassian.sal.api.pluginsettings.PluginSettingsFactory;
import com.google.common.base.Predicates;
import com.google.common.collect.Lists;
import org.hamcrest.CoreMatchers;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.List;

import static com.google.common.collect.Lists.newArrayList;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.Matchers.contains;
import static org.junit.Assert.assertThat;

@RunWith(MockitoJUnitRunner.class)
public class ConfigDaoTest {
  @InjectMocks
  private ConfigDao dao;
  @Mock
  @SuppressWarnings("unused") //Used by @InjectMocks
  private PluginSettingsFactory pluginSettingsFactory;
  @Mock
  @SuppressWarnings("unused") //Used by @InjectMocks
  private UserService userService;

  @Test
  public void testJoin() {
    String val = dao.join(newArrayList("UseR1", "user2", "USER3"), Predicates.alwaysTrue());
    assertThat(val, equalTo("UseR1, user2, USER3"));
  }

  @Test
  public void testSplit() {
    List<String> str = dao.split("usER1, user2, USER3");
    assertThat(str, CoreMatchers.equalTo(newArrayList("usER1", "user2", "USER3")));
  }

  @Test
  public void testSplit_blank() {
    List<String> str = dao.split("");
    assertThat(str, CoreMatchers.equalTo(Lists.<String>newArrayList()));
  }

  @Test
  public void testOverlayList_topHasValue() {
    List<String> str = dao.overlay(newArrayList("bottom"), newArrayList("top"));
    assertThat(str, contains("top"));
  }

  @Test
  public void testOverlayList_topHasNoValue() {
    List<String> str = dao.overlay(newArrayList("bottom"), Lists.newArrayList());
    assertThat(str, contains("bottom"));
  }

  @Test
  public void testReverseOverlayList_valuesEqual() {
    List<String> str = dao.reverseOverlay(newArrayList("bottom"), newArrayList("bottom"));
    assertThat(str, nullValue());
  }

  @Test
  public void testReverseOverlayList_valuesNotEqual() {
    List<String> str = dao.reverseOverlay(newArrayList("bottom"), newArrayList("top"));
    assertThat(str, contains("top"));
  }
}
