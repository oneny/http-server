package org.example.httpServer.core;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.Socket;

public class HttpConnectionWorkerThread extends Thread {

  private final static Logger LOGGER = LoggerFactory.getLogger(HttpConnectionWorkerThread.class);
  private final Socket socket;

  public HttpConnectionWorkerThread(Socket socket) {
    this.socket = socket;
  }

  @Override
  public void run() {
    try (socket;
         BufferedReader br = new BufferedReader(new InputStreamReader(socket.getInputStream()));
         OutputStream outputStream = socket.getOutputStream();) {

      String line;

      while ((line = br.readLine()) != null && !line.isEmpty()) {
        System.out.println(line);
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
