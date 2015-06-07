package com.monitorjbl.plugins;

import com.atlassian.stash.pull.PullRequestParticipant;
import com.atlassian.stash.user.StashUser;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class TestUtils {

  public static PullRequestParticipant mockParticipant(String name, boolean approved) {
    PullRequestParticipant p = mock(PullRequestParticipant.class);
    StashUser user = mock(StashUser.class);
    when(p.getUser()).thenReturn(user);
    when(user.getSlug()).thenReturn(name);
    when(p.isApproved()).thenReturn(approved);
    return p;
  }
}
