package org.example.httpServer.core;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * we are going to have the ServerListenerThread have a constructor
 * that takes in the port and the webroot
 * remember we could have the configuration manager inside the ServerListenerThread
 * and get those values
 */
public class ServerListenerThread extends Thread {

  private final static Logger LOGGER = LoggerFactory.getLogger(ServerListenerThread.class);
  private final ServerSocket serverSocket;
  private int port;
  private String webroot;

  public ServerListenerThread(int port, String webroot) throws IOException {
    this.port = port;
    this.webroot = webroot;
    // 이미 포트 번호가 사용되거나 포트를 사용할 권한 없는 등의 이유로 IOException이 발생할 수 있음
    serverSocket = new ServerSocket(this.port);
  }

  @Override
  public void run() {

    try (serverSocket) {
      while (serverSocket.isBound() && !serverSocket.isClosed()) {
        Socket socket = serverSocket.accept();
        LOGGER.info(" * Connection accepted: " + socket.getInetAddress());

        HttpConnectionWorkerThread workerThread = new HttpConnectionWorkerThread(socket);
        workerThread.start();
      }
    } catch (IOException e) {
      LOGGER.error("Problem with setting socket", e);
    }
  }
}
