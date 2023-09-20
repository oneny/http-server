package org.example.httpServer.core;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.Socket;

public class HttpConnectionWorkerThread extends Thread {

  private final static byte CR = '\r';
  private final static byte LF = '\n';
  private final static Logger LOGGER = LoggerFactory.getLogger(HttpConnectionWorkerThread.class);
  private final Socket socket;

  public HttpConnectionWorkerThread(Socket socket) {
    this.socket = socket;
  }

  @Override
  public void run() {
    try (socket;
         InputStream inputStream = socket.getInputStream();
         OutputStream outputStream = socket.getOutputStream()) {

      int oneInt = -1;
      byte oldByte = (byte) -1;
      StringBuilder sb = new StringBuilder();
      while (-1 != (oneInt = inputStream.read())) {
        byte thisByte = (byte) oneInt;

        if (thisByte == LF && oldByte == CR) {
          // CRLF가 완성되었으면 직전 CRLF부터 여기까기가 한 행이다.
          String oneLine = sb.substring(0, sb.length() - 1); // LF가 버퍼에 들어가기 전이기 때문에 -2가 아닌 -1
          LOGGER.info(oneLine);
          if (oneLine.length() <= 0) {
            // 내용이 없는 행
            // 따라서 메시지 헤더의 마지막일 경우다.
            LOGGER.info("내용이 없는 헤더, 즉 메시지 헤더의 끝");
            break;
          }
          sb.setLength(0);
        } else {
          sb.append((char) thisByte);
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
      LOGGER.error("Problem with communication", e);
    }
  }
}
