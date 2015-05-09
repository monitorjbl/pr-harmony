package com.monitorjbl.plugins;

import com.monitorjbl.plugins.config.ConfigDao;

public class MergeBlocker {
  private final ConfigDao configDao;

  public MergeBlocker(ConfigDao configDao) {
    this.configDao = configDao;
  }
}
