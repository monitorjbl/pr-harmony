package com.monitorjbl.plugins;

import com.atlassian.stash.hook.HookResponse;
import com.atlassian.stash.hook.PreReceiveHook;
import com.atlassian.stash.repository.RefChange;
import com.atlassian.stash.repository.Repository;
import com.monitorjbl.plugins.config.ConfigDao;

import java.util.Collection;

public class CommitBlockerHook implements PreReceiveHook {
  private final ConfigDao configDao;

  public CommitBlockerHook(ConfigDao configDao) {
    this.configDao = configDao;
  }

  @Override
  public boolean onReceive(Repository repository, Collection<RefChange> collection, HookResponse hookResponse) {
    return false;
  }
}
