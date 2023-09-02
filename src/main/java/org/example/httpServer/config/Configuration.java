package org.example.httpServer.config;

/**
 * http.json 파일과 매핑하는 클래스
 */
public class Configuration {

  private int port;
  private String webroot;

  public int getPort() {
    return port;
  }

  public void setPort(int port) {
    this.port = port;
  }

  public String getWebroot() {
    return webroot;
  }

  public void setWebroot(String webroot) {
    this.webroot = webroot;
  }
}
