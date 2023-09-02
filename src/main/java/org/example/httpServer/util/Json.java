package org.example.httpServer.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.*;

public class Json {

  private static ObjectMapper myObjectMapper = defaultObjectMapper();

  private static ObjectMapper defaultObjectMapper() {
    ObjectMapper om = new ObjectMapper();

    // JSON 데이터에 포함된 속성 중에서 Java 클래스에 매핑되지 않는 속성이 있을 때 예외를 발생시키는데
    // 아래 설정을 통해 예외를 발생시키지 않도록 설정 변경
    om.configure(DeserializationFeature.FAIL_ON_IGNORED_PROPERTIES, false);
    return om;
  }

  /**
   * JSON 데이터를 파라미터로 받아 파싱하여 JsonNode로 반환
   */
  public static JsonNode parse(String jsonSrc) throws JsonProcessingException {
    // readTree() 메서드는 JSON 데이터를 파싱하여 JSON 트리를 생성한다.
    return myObjectMapper.readTree(jsonSrc);
  }

  public static <T> T fromJson(JsonNode node, Class<T> clazz) throws JsonProcessingException {
    // JsonNode를 통해 넘겨받은 클래스의 타입 즉, 해당 타입으로 변환하여 반환
    return myObjectMapper.treeToValue(node, clazz);
  }

  public static JsonNode toJson(Object obj) {
    return myObjectMapper.valueToTree(obj);
  }

  public static String stringify(JsonNode node) throws JsonProcessingException {
    return generateJson(node, false);
  }

  public static String stringifyPretty(JsonNode node) throws JsonProcessingException {
    return generateJson(node, true);
  }

  /**
   * ObjectWriter는 JSON 데이터를 쓰기 위한 기능을 제공
   * writeValuesAsString() 메서드를 통해서 넘겨받은 객체를 JSON 형식의 문자열로 직렬화하여 반환한다.
   */
  private static String generateJson(Object o, boolean pretty) throws JsonProcessingException {
    ObjectWriter writer = myObjectMapper.writer();

    if (pretty) {
      // pretty-printing으로 포맷팅하여 JSON 데이터가 읽기 쉽게 들여쓰기와 줄 바꿈을 가지고 있는 형태로 표시된다.
      writer = writer.with(SerializationFeature.INDENT_OUTPUT);
    }

    return writer.writeValueAsString(o);
  }
}
