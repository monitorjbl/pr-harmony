package com.monitorjbl.plugins;

import com.atlassian.stash.user.StashUser;
import com.atlassian.stash.user.UserService;
import com.atlassian.stash.util.Page;
import com.atlassian.stash.util.PageRequest;
import com.google.common.base.Predicate;
import com.monitorjbl.plugins.config.User;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.List;

import static com.google.common.collect.Lists.newArrayList;
import static com.monitorjbl.plugins.TestUtils.mockStashUser;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.hasItems;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class UserUtilsTest {

  @Mock
  private UserService userService;
  @InjectMocks
  UserUtils sut;

  @Before
  public void setup() throws Exception {
    MockitoAnnotations.initMocks(this);
    StashUser userA = mockStashUser("userA");
    StashUser userB = mockStashUser("userB");
    StashUser user1 = mockStashUser("user1");
    StashUser user2 = mockStashUser("user2");
    StashUser user3 = mockStashUser("user3");
    StashUser user4 = mockStashUser("user4");
    List<StashUser> userList1 = newArrayList(user1, user2);
    List<StashUser> userList2 = newArrayList(user3, user4);

    Page p1 = mock(Page.class);
    Page p2 = mock(Page.class);
    when(p1.getValues()).thenReturn(userList1);
    when(p2.getValues()).thenReturn(userList2);
    when(userService.findUsersByGroup(eq("group1"), any(PageRequest.class))).thenReturn(p1);
    when(userService.findUsersByGroup(eq("group2"), any(PageRequest.class))).thenReturn(p2);
    when(userService.getUserBySlug("userA")).thenReturn(userA);
    when(userService.getUserBySlug("userB")).thenReturn(userB);
    when(userService.getUserBySlug("user1")).thenReturn(user1);
    when(userService.getUserBySlug("user2")).thenReturn(user2);
    when(userService.getUserBySlug("user3")).thenReturn(user3);
    when(userService.getUserBySlug("user4")).thenReturn(user4);
  }

  @Test
  public void testDereferenceGroups_single() throws Exception {
    List<String> result = sut.dereferenceGroups(newArrayList("group1"));
    assertThat(result, is(notNullValue()));
    assertThat(result.size(), equalTo(2));
    assertThat(result, hasItems("user1", "user2"));
  }

  @Test
  public void testDereferenceGroups_multiple() throws Exception {
    List<String> result = sut.dereferenceGroups(newArrayList("group1", "group2"));
    assertThat(result, is(notNullValue()));
    assertThat(result.size(), equalTo(4));
    assertThat(result, hasItems("user1", "user2", "user3", "user4"));
  }
}
