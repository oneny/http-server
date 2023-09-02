# HTTP Server

> Socket을 이용해 간단한 HTTP Server 만들기

### 관련 블로그

- [소켓을 활용한 Echo Server 만들기](https://oneny.tistory.com/62)

### HTTP Server 개요

1. Configuration 파일 읽기
    - 프로그램의 설정 정보를 저장하는 파일을 읽어와서 프로그램이 동작할 환경 설정을 구성한다. 이 설정은 포트 번호, 파일 경로, 데이터베이스 연결 정보 등을 포함할 수 있다.
2. 특정 포트 번호에서 소켓 열기
    - 네트워크 연결을 수신하기 위해 특정 포트 번호에서 소켓을 연다. 이것은 클라이언트 요청을 수신하고 처리할 준비를 하는 단계이다.
3. 요청 메시지 읽기
    - 클라이언트로부터의 요청 메시지를 읽어와서 해당 요청에 대한 처리를 시작한다. 요청 메시지는 클라이언트가 원하는 작업에 대한 정보를 담은 데이터이다.
4. 파일 시스템에서 파일 열고 읽기
    - 필요한 경우 파일 시스템에서 파일을 열고 읽어온다. 이것은 클라이언트 요청에 대한 데이터나 리소스를 제공하기 위해 사용될 수 있다.
5. 응답 메시지 쓰기
    - 클라이언트에 대한 응답 메시지를 생성하고 전송한다. 이 메시지는 클라이언트의 요청에 따라 생성되며, 처리 결과를 포함할 수 있다.

### Configuration, ConfigurationManager 생성

HTTP 서버의 설정 관리를 담당하는 클래스를 생성한다. ConfigurationManager는 싱글톤 패턴을 사용하여 여러 곳에서 공유되어 설정을 일관되게 관리하도록 구현했다.    
ConfigurationManager의 loadConfigurationFile() 메서드는 지정된 파일 경로에 대한 설정 파일(json)을 로드하여 Configuration 객체로 파싱 및 변환하는 역할을 한다.

## 멀티스레드를 통해 클라이언트에 대한 응답 속도 향상

### ServerListenerThread의 문제

```java
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

        try (socket;
             InputStream inputStream = socket.getInputStream();
             OutputStream outputStream = socket.getOutputStream()) {

          String html = "<html><head><title>Simple Java HTTP Server</title></head><body><h1>This page was served using my Simple Java HTTP Server</h1></body></html>";

          final String CRLF = "\n\r"; // 13, 10

          String response =
                  "HTTP/1.1 200 OK" + CRLF + // Status Line  :  HTTP VERSION RESPONSE_CODE RESPONSE_MESSAGE
                          "Content-Length: " + html.getBytes().length + CRLF + // HEADER
                          CRLF +
                          html +
                          CRLF + CRLF;

          outputStream.write(response.getBytes());

          try {
            sleep(5000);
          } catch (InterruptedException e) {
            throw new RuntimeException(e);
          }

          LOGGER.info(" * Connection Processing Finished.");
        } catch (IOException e) {
          LOGGER.error("Problem with communication", e);
        }
      }
    } catch (IOException e) {
      LOGGER.error("Problem with setting socket", e);
    }
  }

  public Socket acceptSocket() throws IOException {
    Socket socket = serverSocket.accept();
    LOGGER.info(" * Connection accepted: " + socket.getInetAddress());
    return socket;
  }
}
```

위 클래스는 하나의 스레드에서 클라이언트의 요청이 들어오면 accept() 메서드를 통해 소켓이 연결되고, OutputStream을 통해 응답을 하는 것을 확인할 수 있다.    
하지만 여기서 간단한 작업이 아닌 무거운 작업인 경우에 어떻게 될까? sleep(5000);를 통해 5초간 메서드가 잠시 일시중단되도록 만들었다. 그리고 그 결과를 확인해보자.

![image](https://github.com/oneny/http-server/assets/97153666/721c6baa-0978-41c9-b61f-d949c69b9a17)

위 출력된 시간을 보면, 5초 뒤에 해당 소켓에 대해 연결이 종료되고, 다음 ServerSocket의 대기 중인 큐에서 꺼내져 다음 소켓이 연결되는 것을 확인할 수 있다.    
그래서 7개의 소켓이 연결되고, 작업한 뒤 끊기기까지 대략 35초가 소요된 것을 확인할 수 있다. 이를 해결하기 위해서는 어떻게 해야할까?

### 작업 스레드 생성

```java
public class HttpConnectionWorkerThread extends Thread {

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

      String html = "<html><head><title>Simple Java HTTP Server</title></head><body><h1>This page was served using my Simple Java HTTP Server</h1></body></html>";

      final String CRLF = "\n\r"; // 13, 10

      String response =
              "HTTP/1.1 200 OK" + CRLF + // Status Line  :  HTTP VERSION RESPONSE_CODE RESPONSE_MESSAGE
                      "Content-Length: " + html.getBytes().length + CRLF + // HEADER
                      CRLF +
                      html +
                      CRLF + CRLF;

      outputStream.write(response.getBytes());

      try {
        sleep(5000);
      } catch (InterruptedException e) {
        throw new RuntimeException(e);
      }

      LOGGER.info(" * Connection Processing Finished.");
    } catch (IOException e) {
      LOGGER.error("Problem with communication", e);
    }
  }
}
```
```java
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

  public Socket acceptSocket() throws IOException {
    Socket socket = serverSocket.accept();
    LOGGER.info(" * Connection accepted: " + socket.getInetAddress());
    return socket;
  }
}
```

위의 작업 스레드를 따로 분리해뒀다. 그리고 기존의 ServerListenerThread에서 연결된 소켓을 넘겨받아 해당 연결된 소켓을 통해 클라이언트와 통신하는 것을 확인할 수 있다.    
그러면 ServerListenerThread에서는 클라이언트와의 소켓 연결되는 작업만 하고, 소켓을 넘겨주면서 작업을 위한 별도의 스레드를 두고 종료가 된다.    
별도의 스레드는 클라이언트 각각의 요청에 대해서 생성되어 병렬로 처리할 수 있어 응답 속도 향상된다.

![image](https://github.com/oneny/http-server/assets/97153666/50ae447b-3400-4c4f-bfed-7d55fe4a8497)

위 결과를 보면 알 수 있듯이 각 요청에 대해 작업 스레드가 별도로 생성됨으로써 해당 클라이언트의 요청에 대해 5초 뒤에 연결이 끊기는 것을 확인할 수 있다.

### HttpConnectionWorkerThread의 역할 및 장점

1. 병렬 처리
    - ServerListenerThread는 클라이언트의 연결 요청을 수신하고 각 연결을 HttpConnectionWorkerThread 객체로 위임한다.
    - 이렇게 여러 클라이언트의 요청을 동시에 처리할 수 있어, 서버가 병렬로 작동하여 효율적으로 동작할 수 있다.
2. 요청 처리 분리
    - HttpConnectionWorkerThread는 개별 클라이언트 연결을 처리하는 역할을 담당한다.
    - 이로써 서버의 핵심 로직과 클라이언트와의 통신 로직을 분리할 수 있어 유지 보수를 용이하게 할 수 있다.
3. 응답 시간 향상
    - HttpConnectionWorkerThread는 하나의 클라이언트 연결을 담당하며, 해당 클라이언트에게 응답을 보내는 역할을 한다.
    - 위에서 봤듯이 하나의 스레드에서 처리한다면 클라이언트의 연결 처리가 지연될 수 있지만 HttpConnectionWorkerThread를 사용하여 클라이언트는 별도의 스레드에서 처리되므로, 응답 시간을 향상시킬 수 있다.
    - 따라서 더 많은 클라이언트의 요청을 동시에 처리할 수 있어 더 많은 클라이언트를 지원하고, 서버의 성능을 향상시킬 수 있다.
