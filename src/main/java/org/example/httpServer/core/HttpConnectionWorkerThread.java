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

      HttpParser httpParser = new HttpParser();
      HttpRequest httpRequest = httpParser.parseHttpRequest(inputStream);

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
