package com.monitorjbl.plugins.config;

import com.atlassian.sal.api.pluginsettings.PluginSettingsFactory;
import com.google.common.base.Joiner;

import java.util.List;

public class ConfigDao {
  private final PluginSettingsFactory pluginSettingsFactory;

  public ConfigDao(PluginSettingsFactory pluginSettingsFactory) {
    this.pluginSettingsFactory = pluginSettingsFactory;
  }

  public List<String> getBlockedBranches() {
    return null;
  }

  public void setBlockedBranches(List<String> blockedBranches) {
    Joiner.on(',').join(blockedBranches);
  }

}
