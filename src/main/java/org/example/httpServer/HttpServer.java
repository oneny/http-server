package org.example.httpServer;

import org.example.httpServer.config.Configuration;
import org.example.httpServer.config.ConfigurationManager;
import org.example.httpServer.core.ServerListenerThread;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

/**
 * Driver Class for tht Http Server
 */
public class HttpServer {

  private final static Logger LOGGER = LoggerFactory.getLogger(HttpServer.class);

  public static void main(String[] args) {

    LOGGER.info("Server starting...");
    ConfigurationManager.getInstance().loadConfigurationFile("src/main/resources/http.json");
    Configuration conf = ConfigurationManager.getInstance().getCurrentConfiguration();

    LOGGER.info("Using Port: " + conf.getPort());
    LOGGER.info("Using WebRoot: " + conf.getWebroot());

    try {
      ServerListenerThread serverListenerThread = new ServerListenerThread(conf.getPort(), conf.getWebroot());
      serverListenerThread.start();
    } catch (IOException e) {
      e.printStackTrace();
      // TODO handle later.
    }

  }
}
