package com.monitorjbl.plugins;

import java.util.List;
import java.util.regex.Pattern;

public class RegexUtils {
  public static final String REFS_PREFIX = "refs/heads/";

  public boolean match(List<String> regexes, String value) {
    for (String regex : regexes) {
      regex = regex.replaceAll("\\.","\\\\.").replaceAll("\\*",".*");
      Pattern p = Pattern.compile(regex);
      if (p.matcher(value).matches()) {
        return true;
      }
    }
    return false;
  }

  public String formatBranchName(String refspec) {
    return refspec.replace(REFS_PREFIX, "");
  }
}
