package org.example.http;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

public class HttpParser {

  private static final Logger LOGGER = LoggerFactory.getLogger(HttpParser.class);
  private static final int SP = 0x20; // 32
  private static final int CR = 0x0D; // 13
  private static final int LF = 0x0A; // 10

  public HttpRequest parseHttpRequest(InputStream inputStream) throws HttpParsingException {
    // UTF-8 유니코드는 아스키 코드와 영문 영역에서는 100% 호환
    InputStreamReader reader = new InputStreamReader(inputStream, StandardCharsets.UTF_8);

    HttpRequest request = new HttpRequest();

    try {
      parseRequestLine(reader, request);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
    parseHeaders(reader, request);
    parseBody(reader, request);

    return request;
  }

  /**
   * 아래 Request-Line을 파싱
   * Method SP Request-Target SP Http-Version CRLF
   * ex) GET /oneny HTTP/1.1
   */
  private void parseRequestLine(InputStreamReader reader, HttpRequest request) throws IOException, HttpParsingException {
    StringBuffer processingDataBuffer = new StringBuffer();
    int _byte;

    boolean methodParsed = false;
    boolean requestTargetParsed = false;

    while ((_byte = reader.read()) >= 0) {
      if (_byte == CR) {
        _byte = reader.read();
        if (_byte == LF) { // RequestLine의 CRLF까지 오면 종
          LOGGER.debug("Request Line VERSION to Process : {}", processingDataBuffer);

          if (!methodParsed || !requestTargetParsed) { // 모두 파싱되지 않았는데 CRLF를 만나면
            throw new HttpParsingException(HttpStatusCode.CLIENT_ERROR_400_BAD_REQUEST);
          }

          try {
            request.setHttpVersion(processingDataBuffer.toString());
          } catch (BadHttpVersionException e) {
            throw new HttpParsingException(HttpStatusCode.CLIENT_ERROR_400_BAD_REQUEST);
          }

          return;
        } else {
          throw new HttpParsingException(HttpStatusCode.CLIENT_ERROR_400_BAD_REQUEST);
        }
      }

      if (_byte == SP) {
        if (!methodParsed) {
          LOGGER.debug("Request Line METHOD to Process : {}", processingDataBuffer);
          request.setMethod(processingDataBuffer.toString());
          methodParsed = true;
        } else if (!requestTargetParsed) {
          LOGGER.debug("Request Line Req Target to Process : {}", processingDataBuffer);
          request.setRequestTarget(processingDataBuffer.toString());
          requestTargetParsed = true;
        } else { // RequestLine의 마지막이 CRLF가 아닌 경우(onlyCRnoLF) or 파싱에서 필요한 것들 외에 추가로 있는 경우(InvalidItems)
          throw new HttpParsingException(HttpStatusCode.CLIENT_ERROR_400_BAD_REQUEST);
        }

        processingDataBuffer.delete(0, processingDataBuffer.length());
        continue;
      }

      processingDataBuffer.append((char) _byte);

      if (!methodParsed && processingDataBuffer.length() > HttpMethod.MAX_LENGTH) {
        throw new HttpParsingException(HttpStatusCode.SERVER_ERROR_501_NOT_IMPLEMENTED);
      }
    }
  }

  private void parseHeaders(InputStreamReader reader, HttpRequest request) {
  }

  private void parseBody(InputStreamReader reader, HttpRequest request) {
  }
}
