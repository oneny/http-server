package org.example.httpServer.core;

import org.example.http.HttpParser;
import org.example.http.HttpParsingException;
import org.example.http.HttpRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BasicHttpConnectionWorkerThread extends Thread {

  private final static byte CR = '\r';
  private final static byte LF = '\n';
  private final static Logger LOGGER = LoggerFactory.getLogger(BasicHttpConnectionWorkerThread.class);
  private final Socket socket;

  public BasicHttpConnectionWorkerThread(Socket socket) {
    this.socket = socket;
  }

  @Override
  public void run() {
    try (socket;
         InputStream inputStream = socket.getInputStream();
         OutputStream outputStream = socket.getOutputStream()) {

      HttpParser httpParser = new HttpParser();
      HttpRequest httpRequest = httpParser.parseHttpRequest(inputStream);

      int oneInt = -1;
      byte oldByte = (byte) -1;
      StringBuilder sb = new StringBuilder();
      int lineNumber = 0;
      boolean bodyFlag = false;
      String method = null;
      String requestUrl = null;
      String httpVersion = null;
      int contentLength = -1;
      int bodyRead = 0;
      List<Byte> bodyByteList = null;
      Map<String, String> headerMap = new HashMap<>();

      while ((oneInt = inputStream.read()) != -1) {
        byte thisByte = (byte) oneInt;

        if (bodyFlag) { // 메시지 바디 파싱
          bodyRead++;
          bodyByteList.add(thisByte);
          if (bodyRead >= contentLength) { // 메시지 바디까지 읽으면 파싱 종료
            break;
          }
        } else { // RequestLine과 Headers 정보
          if (thisByte == LF && oldByte == CR) {
            String oneLine = sb.substring(0, sb.length() - 1);
            lineNumber++;
            if (lineNumber == 1) {
              // 요청의 첫 행
              // HTTP 메섲, 요청 URL, 버전을 알아낸다.
              int firstBlank = oneLine.indexOf(" ");
              int secondBlank = oneLine.lastIndexOf(" ");
              method = oneLine.substring(0, firstBlank);
              requestUrl = oneLine.substring(firstBlank + 1, secondBlank);
              httpVersion = oneLine.substring(secondBlank + 1);
            } else { // RequestLine이 끝나면
              if (oneLine.length() <= 0) {
                bodyFlag = true; // 헤더 끝

                if (method.equals("GET")) { // GET 방식이면 메시지 바디 없음
                  break;
                }

                String contentLengthValue = headerMap.get("Content-Length");
                if (contentLengthValue != null) {
                  contentLength = Integer.parseInt(contentLengthValue.trim());
                  bodyByteList = new ArrayList<>();
                }
                continue;
              }

              int indexOfColon = oneLine.indexOf(":");
              String headerName = oneLine.substring(0, indexOfColon);
              String headerValue = oneLine.substring(indexOfColon + 1);
              headerMap.put(headerName, headerValue);
            }

            sb.setLength(0);
          } else {
            sb.append((char) thisByte);
          }
        }

        oldByte = (byte) oneInt;
      }

      String html = "<html><head><title>Simple Java HTTP Server</title></head><body><h1>This page was served using my Simple Java HTTP Server</h1></body></html>";

      final String CRLF = "\n\r"; // 13, 10

      String response =
              "HTTP/1.1 200 OK" + CRLF + // Status Line  :  HTTP VERSION RESPONSE_CODE RESPONSE_MESSAGE
                      "Content-Length: " + html.getBytes().length + CRLF + // HEADER
                      CRLF +
                      html +
                      CRLF + CRLF;

      outputStream.write(response.getBytes());

      LOGGER.info(" * Connection Processing Finished.");
    } catch (IOException e) {
      LOGGER.error("Problem with Communication", e);
    } catch (HttpParsingException e) {
      LOGGER.error("Problem with HttpParsing", e);
    }
  }
}
