package org.example.http;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;

class HttpParserTest {

  private HttpParser httpParser;

  @BeforeEach
  void setUp() {
    httpParser = new HttpParser();
  }

  @Test
  void parseHttpRequest() throws HttpParsingException {
    HttpRequest request = httpParser.parseHttpRequest(generateValidGETTestCase());

    assertThat(request.getMethod()).isEqualTo(HttpMethod.GET);
  }

  @Test
  void parseBadHttpRequest() {
    assertThatThrownBy(() -> httpParser.parseHttpRequest(generateValidBADTestCase()))
            .isInstanceOf(HttpParsingException.class)
            .hasMessage("Not Implemented");
  }

  @Test
  void parseBad2HttpRequest() {
    assertThatThrownBy(() -> httpParser.parseHttpRequest(generateValidBADTestCase2()))
            .isInstanceOf(HttpParsingException.class)
            .hasMessage("Not Implemented");
  }

  @Test
  void parseHttpRequestInvalidItems() {
    assertThatThrownBy(() -> httpParser.parseHttpRequest(generateBadTestCaseRequestLineInvalidItems()))
            .isInstanceOf(HttpParsingException.class)
            .hasMessage("Bad Request");
  }
  @Test
  void parseHttpRequestLine_EmptyRequestLine() {
    assertThatThrownBy(() -> httpParser.parseHttpRequest(generateBadTestCaseEmptyRequestLine()))
            .isInstanceOf(HttpParsingException.class)
            .hasMessage("Bad Request");
  }

  @Test
  void parseHttpRequestLine_onlyCRnoLF() {
    assertThatThrownBy(() -> httpParser.parseHttpRequest(generateBadTestCaseRequestLineOnlyCRnoLF()))
            .isInstanceOf(HttpParsingException.class)
            .hasMessage("Bad Request");
  }

  @Test
  void parseHttpRequestLine_BadHttpVersion() {
    assertThatThrownBy(() -> httpParser.parseHttpRequest(generateBadHttpVersionTestCase()))
            .isInstanceOf(HttpParsingException.class)
            .hasMessage("Bad Request");
  }

  @Test
  void parseHttpRequestLine_UnsupportedHttpVersion() {
    assertThatThrownBy(() -> httpParser.parseHttpRequest(generateUnSuportedHttpVersionTestCase()))
            .isInstanceOf(HttpParsingException.class)
            .hasMessage("Http Version Not Supported");
  }

  @Test
  void parseHttpRequestLine_supportedHttpVersion() throws HttpParsingException {
    HttpRequest httpRequest = httpParser.parseHttpRequest(generateSuportedHttpVersionTestCase());

    assertAll(
            () -> assertThat(httpRequest.getMethod()).isEqualTo(HttpMethod.GET),
            () -> assertThat(httpRequest.getRequestTarget()).isEqualTo("/"),
            () -> assertThat(httpRequest.getBestCompatibleHttpVersion()).isEqualTo(HttpVersion.HTTP_1_1)
    );
  }

  private InputStream generateValidGETTestCase() {
    String rawData = "GET / HTTP/1.1\r\n" +
            "Host: localhost:8080\r\n" +
            "Connection: keep-alive\r\n" +
            "sec-ch-ua: \"Chromium\";v=\"116\", \"Not)A;Brand\";v=\"24\", \"Google Chrome\";v=\"116\"\r\n" +
            "sec-ch-ua-mobile: ?0\r\n" +
            "sec-ch-ua-platform: \"macOS\"\r\n" +
            "Upgrade-Insecure-Requests: 1\r\n" +
            "User-Agent: Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/116.0.0.0 Safari/537.36\r\n" +
            "Accept: text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.7\r\n" +
            "Sec-Fetch-Site: none\r\n" +
            "Sec-Fetch-Mode: navigate\r\n" +
            "Sec-Fetch-User: ?1\r\n" +
            "Sec-Fetch-Dest: document\r\n" +
            "Accept-Encoding: gzip, deflate, br\r\n" +
            "Accept-Language: ko-KR,ko;q=0.9,en-US;q=0.8,en;q=0.7\r\n" +
            "\r\n";

    return new ByteArrayInputStream(rawData.getBytes(StandardCharsets.UTF_8));
  }

  private InputStream generateValidBADTestCase() {
    String rawData = "BAD / HTTP/1.1\r\n" +
            "Host: localhost:8080\r\n" +
            "\r\n";

    return new ByteArrayInputStream(rawData.getBytes(StandardCharsets.UTF_8));
  }

  private InputStream generateValidBADTestCase2() {
    String rawData = "GETTTTTT / HTTP/1.1\r\n" +
            "Host: localhost:8080\r\n" +
            "\r\n";

    return new ByteArrayInputStream(rawData.getBytes(StandardCharsets.UTF_8));
  }

  private InputStream generateBadTestCaseRequestLineInvalidItems() {
    String rawData = "GET / AAAAAA HTTP/1.1\r\n" +
            "Host: localhost:8080\r\n" +
            "\r\n";

    return new ByteArrayInputStream(rawData.getBytes(StandardCharsets.UTF_8));
  }

  private InputStream generateBadTestCaseEmptyRequestLine() {
    String rawData = "\r\n" +
            "Host: localhost:8080\r\n" +
            "\r\n";

    return new ByteArrayInputStream(rawData.getBytes(StandardCharsets.UTF_8));
  }

  private InputStream generateBadTestCaseRequestLineOnlyCRnoLF() {
    String rawData = "GET / HTTP/1.1\r" +
            "Host: localhost:8080\r\n" +
            "\r\n";

    return new ByteArrayInputStream(rawData.getBytes(StandardCharsets.UTF_8));
  }

  private InputStream generateBadHttpVersionTestCase() {
    String rawData = "GET / HTP/1.1\r\n" +
            "Host: localhost:8080\r\n" +
            "\r\n";

    return new ByteArrayInputStream(rawData.getBytes(StandardCharsets.UTF_8));
  }

  private InputStream generateUnSuportedHttpVersionTestCase() {
    String rawData = "GET / HTTP/2.1\r\n" +
            "Host: localhost:8080\r\n" +
            "\r\n";

    return new ByteArrayInputStream(rawData.getBytes(StandardCharsets.UTF_8));
  }

  private InputStream generateSuportedHttpVersionTestCase() {
    String rawData = "GET / HTTP/1.2\r\n" +
            "Host: localhost:8080\r\n" +
            "\r\n";

    return new ByteArrayInputStream(rawData.getBytes(StandardCharsets.UTF_8));
  }
}
