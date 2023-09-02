package org.example.http;

import java.util.Arrays;

public enum HttpMethod {
  GET, HEAD;
  public static final int MAX_LENGTH;

  static  {
    MAX_LENGTH = Arrays.stream(HttpMethod.values())
            .map(HttpMethod::name)
            .mapToInt(String::length)
            .max()
            .orElse(-1);
  }
}
