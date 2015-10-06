package com.monitorjbl.plugins.config;

import com.atlassian.sal.api.pluginsettings.PluginSettingsFactory;
import com.atlassian.bitbucket.user.UserService;
import com.google.common.base.Predicate;
import com.google.common.collect.Lists;
import org.hamcrest.CoreMatchers;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.List;

import static com.google.common.collect.Lists.newArrayList;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.Matchers.contains;
import static org.junit.Assert.assertThat;

@SuppressWarnings("unchecked")
public class ConfigDaoTest {
  @Mock
  private PluginSettingsFactory pluginSettingsFactory;
  @Mock
  private UserService userService;
  @InjectMocks
  ConfigDao sut;

  Predicate noopPredicate = new Predicate() {
    @Override
    public boolean apply(Object input) {
      return true;
    }
  };

  @Before
  public void setup() throws Exception {
    MockitoAnnotations.initMocks(this);
  }

  @Test
  public void testJoin() throws Exception {
    String val = sut.join(newArrayList("UseR1", "user2", "USER3"), noopPredicate);
    assertThat(val, equalTo("UseR1, user2, USER3"));
  }

  @Test
  public void testSplit() throws Exception {
    List<String> str = sut.split("usER1, user2, USER3");
    assertThat(str, CoreMatchers.<List<String>>equalTo(newArrayList("usER1", "user2", "USER3")));
  }

  @Test
  public void testSplit_blank() throws Exception {
    List<String> str = sut.split("");
    assertThat(str, CoreMatchers.<List<String>>equalTo(Lists.<String>newArrayList()));
  }

  @Test
  public void testOverlayList_topHasValue() throws Exception {
    List<String> str = sut.overlay(newArrayList("bottom"), newArrayList("top"));
    assertThat(str, contains("top"));
  }

  @Test
  public void testOverlayList_topHasNoValue() throws Exception {
    List<String> str = sut.overlay(newArrayList("bottom"), Lists.<String>newArrayList());
    assertThat(str, contains("bottom"));
  }

  @Test
  public void testReverseOverlayList_valuesEqual() throws Exception {
    List<String> str = sut.reverseOverlay(newArrayList("bottom"), newArrayList("bottom"));
    assertThat(str, nullValue());
  }

  @Test
  public void testReverseOverlayList_valuesNotEqual() throws Exception {
    List<String> str = sut.reverseOverlay(newArrayList("bottom"), newArrayList("top"));
    assertThat(str, contains("top"));
  }
}
