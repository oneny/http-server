package org.example.http;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public enum HttpVersion {
  HTTP_1_1("HTTP/1.1", 1, 1);

  private static final Pattern httpVersionRegexPattern = Pattern.compile("^HTTP/(?<major>\\d+).(?<minor>\\d+)");
  private final String LITERAL;
  private final int MAJOR;
  private final int MINOR;

  HttpVersion(String LITERAL, int MAJOR, int MINOR) {
    this.LITERAL = LITERAL;
    this.MAJOR = MAJOR;
    this.MINOR = MINOR;
  }

  public static HttpVersion getBestCompatibleVersion(String literalVersion) throws BadHttpVersionException {
    Matcher matcher = httpVersionRegexPattern.matcher(literalVersion);

    if (!matcher.find() || matcher.groupCount() != 2) {
      throw new BadHttpVersionException();
    }

    int major = Integer.parseInt(matcher.group("major"));
    int minor = Integer.parseInt(matcher.group("minor"));

    HttpVersion tempBestCompatible = null;

    for (HttpVersion version : HttpVersion.values()) {
      if (version.LITERAL.equals(literalVersion)) {
        return version;
      } else {
        if (version.MAJOR == major && version.MINOR < minor) {
          tempBestCompatible = version;
        }
      }
    }

    return tempBestCompatible;
  }

}
