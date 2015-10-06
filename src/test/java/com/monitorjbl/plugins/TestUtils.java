package com.monitorjbl.plugins;

import com.atlassian.bitbucket.pull.PullRequestParticipant;
import com.atlassian.bitbucket.user.ApplicationUser;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class TestUtils {

  public static PullRequestParticipant mockParticipant(String name, boolean approved) {
    PullRequestParticipant p = mock(PullRequestParticipant.class);
    ApplicationUser user = mockApplicationUser(name);
    when(p.getUser()).thenReturn(user);
    when(p.isApproved()).thenReturn(approved);
    return p;
  }

  public static ApplicationUser mockApplicationUser(String name) {
    ApplicationUser user = mock(ApplicationUser.class);
    when(user.getSlug()).thenReturn(name);
    return user;
  }
}
