package org.example.http;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class HttpVersionTest {

  @Test
  void getBestCompatibleVersion_ExactMatch() throws BadHttpVersionException {
    HttpVersion version = HttpVersion.getBestCompatibleVersion("HTTP/1.1");

    assertThat(version).isEqualTo(HttpVersion.HTTP_1_1);
  }

  @Test
  void getBestCompatibleVersion_BadFormat() {
    assertThatThrownBy(() -> HttpVersion.getBestCompatibleVersion("http/1.1"))
            .isInstanceOf(BadHttpVersionException.class);
  }

  @Test
  void getBestCompatibleVersion_HigherVersion() throws BadHttpVersionException {
    HttpVersion version = HttpVersion.getBestCompatibleVersion("HTTP/1.2");

    assertThat(version).isEqualTo(HttpVersion.HTTP_1_1);
  }
}
