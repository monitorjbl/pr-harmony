package com.monitorjbl.plugins;

import java.util.List;
import java.util.regex.Pattern;

public class RegexUtils {
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
}
