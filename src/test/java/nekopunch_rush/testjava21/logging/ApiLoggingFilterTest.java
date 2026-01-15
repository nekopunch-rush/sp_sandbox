package nekopunch_rush.testjava21.logging;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlRootElement;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.SocketPolicy;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.http.codec.json.Jackson2JsonDecoder;
import org.springframework.http.codec.json.Jackson2JsonEncoder;
import org.springframework.http.codec.xml.Jaxb2XmlDecoder;
import org.springframework.http.codec.xml.Jaxb2XmlEncoder;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.netty.http.client.HttpClient;

import java.io.IOException;
import java.time.Duration;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

class ApiLoggingFilterTest {

    private MockWebServer mockWebServer;
    private WebClient webClient;
    private ObjectMapper objectMapper;
    private ListAppender<ILoggingEvent> logAppender;
    private Logger apiLoggingFilterLogger;

    @BeforeEach
    void setUp() throws IOException {
        mockWebServer = new MockWebServer();
        mockWebServer.start();

        // カスタマイズされたObjectMapper
        objectMapper = new ObjectMapper()
            .findAndRegisterModules()
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
            .disable(DeserializationFeature.ADJUST_DATES_TO_CONTEXT_TIME_ZONE)
            .enable(SerializationFeature.INDENT_OUTPUT);

        // タイムアウト設定付きのHttpClient
        HttpClient httpClient = HttpClient.create()
            .responseTimeout(Duration.ofSeconds(3));

        // WebClient構築（ObjectMapperがデフォルト、XMLコーデックがカスタム）
        webClient = WebClient.builder()
            .baseUrl(mockWebServer.url("/").toString())
            .clientConnector(new ReactorClientHttpConnector(httpClient))
            .codecs(configurer -> {
                // デフォルトのコーデックを設定
                configurer.defaultCodecs().jackson2JsonEncoder(new Jackson2JsonEncoder(objectMapper, MediaType.APPLICATION_JSON));
                configurer.defaultCodecs().jackson2JsonDecoder(new Jackson2JsonDecoder(objectMapper, MediaType.APPLICATION_JSON));
                // XMLコーデック追加
                configurer.defaultCodecs().jaxb2Encoder(new Jaxb2XmlEncoder());
                configurer.defaultCodecs().jaxb2Decoder(new Jaxb2XmlDecoder());
            })
            .filter(ApiLoggingFilter.logRequest())
            .filter(ApiLoggingFilter.logResponse())
            .build();

        // ログキャプチャの設定
        apiLoggingFilterLogger = (Logger) LoggerFactory.getLogger(ApiLoggingFilter.class);
        logAppender = new ListAppender<>();
        logAppender.start();
        apiLoggingFilterLogger.addAppender(logAppender);
    }

    @AfterEach
    void tearDown() throws IOException {
        apiLoggingFilterLogger.detachAppender(logAppender);
        try {
            mockWebServer.shutdown();
        } catch (IOException e) {
            // タイムアウトテストなどでシャットダウンに時間がかかる場合は無視
        }
    }

    /**
     * キャプチャしたログメッセージを取得
     */
    private List<ILoggingEvent> getCapturedLogs() {
        return logAppender.list;
    }

    /**
     * 指定した文字列を含むログが存在するか確認
     */
    private boolean hasLogContaining(String text) {
        return getCapturedLogs().stream()
            .anyMatch(event -> event.getFormattedMessage().contains(text));
    }

    // テスト用DTOクラス
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @XmlRootElement(name = "TestRequest")
    @XmlAccessorType(XmlAccessType.FIELD)
    public static class TestRequest {
        private String message;
        private int value;
        private RequestDetail detail;
        private LocalDate requestDate;
        private OffsetDateTime requestDateTime;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @XmlAccessorType(XmlAccessType.FIELD)
    public static class RequestDetail {
        private String description;
        private String category;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @XmlRootElement(name = "TestResponse")
    @XmlAccessorType(XmlAccessType.FIELD)
    public static class TestResponse {
        private String result;
        private boolean success;
        private ResponseDetail detail;
        private List<ResponseItem> items;
        private LocalDate responseDate;
        private OffsetDateTime responseDateTime;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @XmlAccessorType(XmlAccessType.FIELD)
    public static class ResponseDetail {
        private String code;
        private String message;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @XmlAccessorType(XmlAccessType.FIELD)
    public static class ResponseItem {
        private int id;
        private String name;
    }

    @Nested
    @DisplayName("JSON リクエスト/レスポンス テスト")
    class JsonTests {

        @Test
        @DisplayName("POST - JSON リクエスト/レスポンス 正常系")
        void testJsonPostSuccess() throws Exception {
            // Given
            TestRequest request = TestRequest.builder()
                .message("hello")
                .value(123)
                .detail(RequestDetail.builder()
                    .description("test description")
                    .category("category1")
                    .build())
                .requestDate(LocalDate.of(2026, 1, 16))
                .requestDateTime(OffsetDateTime.of(2026, 1, 16, 10, 30, 0, 0, ZoneOffset.of("+09:00")))
                .build();

            TestResponse expectedResponse = TestResponse.builder()
                .result("ok")
                .success(true)
                .detail(ResponseDetail.builder()
                    .code("200")
                    .message("Success")
                    .build())
                .items(List.of(
                    ResponseItem.builder().id(1).name("item1").build(),
                    ResponseItem.builder().id(2).name("item2").build()
                ))
                .responseDate(LocalDate.of(2026, 1, 16))
                .responseDateTime(OffsetDateTime.of(2026, 1, 16, 10, 30, 5, 0, ZoneOffset.of("+09:00")))
                .build();
            String responseBody = objectMapper.writeValueAsString(expectedResponse);

            mockWebServer.enqueue(new MockResponse()
                .setResponseCode(200)
                .setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .setBody(responseBody));

            // When
            TestResponse response = webClient.post()
                .uri("/api/test")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .retrieve()
                .bodyToMono(TestResponse.class)
                .block();

            // Then
            assertThat(response).isNotNull();
            assertThat(response.getResult()).isEqualTo("ok");
            assertThat(response.isSuccess()).isTrue();
            assertThat(response.getDetail()).isNotNull();
            assertThat(response.getDetail().getCode()).isEqualTo("200");
            assertThat(response.getItems()).hasSize(2);
            assertThat(response.getItems().getFirst().getName()).isEqualTo("item1");

            // リクエストの検証
            var recordedRequest = mockWebServer.takeRequest();
            assertThat(recordedRequest.getMethod()).isEqualTo("POST");
            assertThat(recordedRequest.getPath()).isEqualTo("/api/test");
            assertThat(recordedRequest.getHeader(HttpHeaders.CONTENT_TYPE)).contains(MediaType.APPLICATION_JSON_VALUE);

            // ログ出力の検証
            assertAll(
                () -> assertThat(hasLogContaining("----- Request Meta -----")).isTrue(),
                () -> assertThat(hasLogContaining("Method: POST")).isTrue(),
                () -> assertThat(hasLogContaining("URL:")).isTrue(),
                () -> assertThat(hasLogContaining("/api/test")).isTrue(),
                () -> assertThat(hasLogContaining("HEADERS:")).isTrue(),
                () -> assertThat(hasLogContaining("------------------------")).isTrue(),
                () -> assertThat(hasLogContaining("===== Request Body =====")).isTrue(),
                () -> assertThat(hasLogContaining("hello")).isTrue(), // リクエストボディの内容
                () -> assertThat(hasLogContaining("========================")).isTrue(),
                () -> assertThat(hasLogContaining("----- Response Meta -----")).isTrue(),
                () -> assertThat(hasLogContaining("Status Code: 200")).isTrue(),
                () -> assertThat(hasLogContaining("-------------------------")).isTrue(),
                () -> assertThat(hasLogContaining("===== Response Body =====")).isTrue(),
                () -> assertThat(hasLogContaining("ok")).isTrue(), // レスポンスボディの内容
                () -> assertThat(hasLogContaining("=========================")).isTrue()
            );
        }

        @Test
        @DisplayName("GET - JSON レスポンス 正常系")
        void testJsonGetSuccess() throws Exception {
            // Given
            TestResponse expectedResponse = TestResponse.builder()
                .result("get result")
                .success(true)
                .responseDate(LocalDate.of(2026, 1, 16))
                .responseDateTime(OffsetDateTime.of(2026, 1, 16, 10, 30, 0, 0, ZoneOffset.of("+09:00")))
                .build();
            String responseBody = objectMapper.writeValueAsString(expectedResponse);

            mockWebServer.enqueue(new MockResponse()
                .setResponseCode(200)
                .setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .setBody(responseBody));

            // When
            TestResponse response = webClient.get()
                .uri("/api/test")
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .bodyToMono(TestResponse.class)
                .block();

            // Then
            assertThat(response).isNotNull();
            assertThat(response.getResult()).isEqualTo("get result");
            assertThat(response.isSuccess()).isTrue();

            // リクエストの検証
            var recordedRequest = mockWebServer.takeRequest();
            assertThat(recordedRequest.getMethod()).isEqualTo("GET");
            assertThat(recordedRequest.getPath()).isEqualTo("/api/test");

            // ログ出力の検証
            assertAll(
                () -> assertThat(hasLogContaining("----- Request Meta -----")).isTrue(),
                () -> assertThat(hasLogContaining("Method: GET")).isTrue(),
                () -> assertThat(hasLogContaining("URL:")).isTrue(),
                () -> assertThat(hasLogContaining("/api/test")).isTrue(),
                () -> assertThat(hasLogContaining("HEADERS:")).isTrue(),
                () -> assertThat(hasLogContaining("------------------------")).isTrue(),
                () -> assertThat(hasLogContaining("----- Response Meta -----")).isTrue(),
                () -> assertThat(hasLogContaining("Status Code: 200")).isTrue(),
                () -> assertThat(hasLogContaining("-------------------------")).isTrue(),
                () -> assertThat(hasLogContaining("===== Response Body =====")).isTrue(),
                () -> assertThat(hasLogContaining("get result")).isTrue(), // レスポンスボディの内容
                () -> assertThat(hasLogContaining("=========================")).isTrue()
            );
        }
    }

    @Nested
    @DisplayName("XML リクエスト/レスポンス テスト")
    class XmlTests {

        @Test
        @DisplayName("POST - XML リクエスト/レスポンス 正常系")
        void testXmlPostSuccess() throws Exception {
            // Given
            TestRequest request = TestRequest.builder()
                .message("hello xml")
                .value(456)
                .requestDate(LocalDate.of(2026, 1, 16))
                .requestDateTime(OffsetDateTime.of(2026, 1, 16, 10, 30, 0, 0, ZoneOffset.of("+09:00")))
                .build();

            String responseXml = """
                <?xml version="1.0" encoding="UTF-8"?>
                <TestResponse>
                    <result>xml ok</result>
                    <success>true</success>
                </TestResponse>
                """;

            mockWebServer.enqueue(new MockResponse()
                .setResponseCode(200)
                .setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_XML_VALUE)
                .setBody(responseXml));

            // When（JAXBオブジェクトで送受信）
            TestResponse response = webClient.post()
                .uri("/api/xml/test")
                .contentType(MediaType.APPLICATION_XML)
                .bodyValue(request)
                .retrieve()
                .bodyToMono(TestResponse.class)
                .block();

            // Then
            assertThat(response).isNotNull();
            assertThat(response.getResult()).isEqualTo("xml ok");
            assertThat(response.isSuccess()).isTrue();

            // リクエストの検証
            var recordedRequest = mockWebServer.takeRequest();
            assertThat(recordedRequest.getMethod()).isEqualTo("POST");
            assertThat(recordedRequest.getPath()).isEqualTo("/api/xml/test");
            assertThat(recordedRequest.getHeader(HttpHeaders.CONTENT_TYPE)).contains(MediaType.APPLICATION_XML_VALUE);

            // ログ出力の検証
            assertAll(
                () -> assertThat(hasLogContaining("----- Request Meta -----")).isTrue(),
                () -> assertThat(hasLogContaining("Method: POST")).isTrue(),
                () -> assertThat(hasLogContaining("URL:")).isTrue(),
                () -> assertThat(hasLogContaining("/api/xml/test")).isTrue(),
                () -> assertThat(hasLogContaining("HEADERS:")).isTrue(),
                () -> assertThat(hasLogContaining("------------------------")).isTrue(),
                () -> assertThat(hasLogContaining("===== Request Body =====")).isTrue(),
                () -> assertThat(hasLogContaining("hello xml")).isTrue(), // リクエストボディの内容
                () -> assertThat(hasLogContaining("========================")).isTrue(),
                () -> assertThat(hasLogContaining("----- Response Meta -----")).isTrue(),
                () -> assertThat(hasLogContaining("Status Code: 200")).isTrue(),
                () -> assertThat(hasLogContaining("-------------------------")).isTrue(),
                () -> assertThat(hasLogContaining("===== Response Body =====")).isTrue(),
                () -> assertThat(hasLogContaining("xml ok")).isTrue(), // レスポンスボディの内容
                () -> assertThat(hasLogContaining("=========================")).isTrue()
            );
        }
    }

    @Nested
    @DisplayName("エラーレスポンス テスト")
    class ErrorTests {

        @Test
        @DisplayName("400 Bad Request エラー")
        void testBadRequestError() {
            // Given
            TestRequest request = TestRequest.builder()
                .message("bad request")
                .value(0)
                .build();
            String errorResponse = "{\"error\": \"Bad Request\", \"message\": \"Invalid input\"}";

            mockWebServer.enqueue(new MockResponse()
                .setResponseCode(400)
                .setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .setBody(errorResponse));

            // When & Then
            try {
                webClient.post()
                    .uri("/api/test")
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(request)
                    .retrieve()
                    .bodyToMono(TestResponse.class)
                    .block();
            } catch (WebClientResponseException.BadRequest ex) {
                assertThat(ex.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
                assertThat(ex.getResponseBodyAsString()).contains("Invalid input");
            }

            // ログ出力の検証
            assertAll(
                () -> assertThat(hasLogContaining("----- Request Meta -----")).isTrue(),
                () -> assertThat(hasLogContaining("URL:")).isTrue(),
                () -> assertThat(hasLogContaining("/api/test")).isTrue(),
                () -> assertThat(hasLogContaining("----- Response Meta -----")).isTrue(),
                () -> assertThat(hasLogContaining("Status Code: 400")).isTrue(),
                () -> assertThat(hasLogContaining("===== Response Body =====")).isTrue(),
                () -> assertThat(hasLogContaining("Invalid input")).isTrue() // エラーレスポンスボディの内容
            );
        }

        @Test
        @DisplayName("500 Internal Server Error エラー")
        void testInternalServerError() {
            // Given
            TestRequest request = TestRequest.builder()
                .message("server error")
                .value(999)
                .build();
            String errorResponse = "{\"error\": \"Internal Server Error\", \"message\": \"Something went wrong\"}";

            mockWebServer.enqueue(new MockResponse()
                .setResponseCode(500)
                .setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .setBody(errorResponse));

            // When & Then
            try {
                webClient.post()
                    .uri("/api/test")
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(request)
                    .retrieve()
                    .bodyToMono(TestResponse.class)
                    .block();
            } catch (WebClientResponseException.InternalServerError ex) {
                assertThat(ex.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
                assertThat(ex.getResponseBodyAsString()).contains("Something went wrong");
            }

            // ログ出力の検証
            assertAll(
                () -> assertThat(hasLogContaining("----- Request Meta -----")).isTrue(),
                () -> assertThat(hasLogContaining("URL:")).isTrue(),
                () -> assertThat(hasLogContaining("/api/test")).isTrue(),
                () -> assertThat(hasLogContaining("----- Response Meta -----")).isTrue(),
                () -> assertThat(hasLogContaining("Status Code: 500")).isTrue(),
                () -> assertThat(hasLogContaining("===== Response Body =====")).isTrue(),
                () -> assertThat(hasLogContaining("Something went wrong")).isTrue() // エラーレスポンスボディの内容
            );
        }
    }

    @Nested
    @DisplayName("ネットワークエラー テスト")
    class NetworkErrorTests {

        @Test
        @DisplayName("タイムアウトエラー")
        void testTimeoutError() {
            // Given
            TestRequest request = TestRequest.builder()
                .message("timeout test")
                .value(1)
                .build();

            mockWebServer.enqueue(new MockResponse()
                .setResponseCode(200)
                .setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .setBody("{\"result\": \"ok\", \"success\": true}")
                .setBodyDelay(10, TimeUnit.SECONDS)); // 10秒遅延（タイムアウト3秒より長い）

            // When & Then
            try {
                webClient.post()
                    .uri("/api/test")
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(request)
                    .retrieve()
                    .bodyToMono(TestResponse.class)
                    .block();
            } catch (Exception ex) {
                // タイムアウトによるエラーが発生することを確認
                assertThat(ex).isNotNull();
            }

            // ログ出力の検証（リクエストログは出力される）
            assertAll(
                () -> assertThat(hasLogContaining("----- Request Meta -----")).isTrue(),
                () -> assertThat(hasLogContaining("URL:")).isTrue(),
                () -> assertThat(hasLogContaining("/api/test")).isTrue(),
                // エラーログが出力されることを確認（Request ErrorまたはResponse Error）
                () -> assertThat(hasLogContaining("Error")).isTrue()
            );
        }

        @Test
        @DisplayName("接続エラー（サーバー切断）")
        void testConnectionError() {
            // Given
            TestRequest request = TestRequest.builder()
                .message("connection error test")
                .value(1)
                .build();

            mockWebServer.enqueue(new MockResponse()
                .setSocketPolicy(SocketPolicy.DISCONNECT_AFTER_REQUEST));

            // When & Then
            try {
                webClient.post()
                    .uri("/api/test")
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(request)
                    .retrieve()
                    .bodyToMono(TestResponse.class)
                    .block();
            } catch (Exception ex) {
                // 接続エラーが発生することを確認
                assertThat(ex).isNotNull();
            }

            // ログ出力の検証（リクエストログは出力される）
            assertAll(
                () -> assertThat(hasLogContaining("----- Request Meta -----")).isTrue(),
                () -> assertThat(hasLogContaining("URL:")).isTrue(),
                () -> assertThat(hasLogContaining("/api/test")).isTrue()
            );
        }

        @Test
        @DisplayName("接続エラー（即座に切断）")
        void testImmediateDisconnect() {
            // Given
            TestRequest request = TestRequest.builder()
                .message("immediate disconnect test")
                .value(1)
                .build();

            mockWebServer.enqueue(new MockResponse()
                .setSocketPolicy(SocketPolicy.DISCONNECT_AT_START));

            // When & Then
            try {
                webClient.post()
                    .uri("/api/test")
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(request)
                    .retrieve()
                    .bodyToMono(TestResponse.class)
                    .block();
            } catch (Exception ex) {
                // 接続エラーが発生することを確認
                assertThat(ex).isNotNull();
            }

            // ログ出力の検証（リクエストログは出力される）
            assertAll(
                () -> assertThat(hasLogContaining("----- Request Meta -----")).isTrue(),
                () -> assertThat(hasLogContaining("URL:")).isTrue(),
                () -> assertThat(hasLogContaining("/api/test")).isTrue(),
                () -> assertThat(hasLogContaining("Error during exchange")).isTrue() // エラーログ
            );
        }
    }

    @Nested
    @DisplayName("空レスポンス テスト")
    class EmptyResponseTests {

        @Test
        @DisplayName("204 No Content レスポンス")
        void testNoContentResponse() {
            // Given
            TestRequest request = TestRequest.builder()
                .message("no content test")
                .value(1)
                .build();

            mockWebServer.enqueue(new MockResponse()
                .setResponseCode(204));

            // When
            webClient.post()
                .uri("/api/test")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .retrieve()
                .bodyToMono(Void.class)
                .block();

            // Then - ログ出力の検証
            assertAll(
                () -> assertThat(hasLogContaining("----- Request Meta -----")).isTrue(),
                () -> assertThat(hasLogContaining("URL:")).isTrue(),
                () -> assertThat(hasLogContaining("/api/test")).isTrue(),
                () -> assertThat(hasLogContaining("----- Response Meta -----")).isTrue(),
                () -> assertThat(hasLogContaining("Status Code: 204")).isTrue()
            );
        }

        @Test
        @DisplayName("200 OK 空ボディ レスポンス")
        void testEmptyBodyResponse() {
            // Given
            TestRequest request = TestRequest.builder()
                .message("empty body test")
                .value(1)
                .build();

            mockWebServer.enqueue(new MockResponse()
                .setResponseCode(200)
                .setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .setBody(""));

            // When
            String body = webClient.post()
                .uri("/api/test")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .retrieve()
                .bodyToMono(String.class)
                .block();

            // Then
            assertThat(body).isEmpty();

            // ログ出力の検証
            assertAll(
                () -> assertThat(hasLogContaining("----- Request Meta -----")).isTrue(),
                () -> assertThat(hasLogContaining("URL:")).isTrue(),
                () -> assertThat(hasLogContaining("/api/test")).isTrue(),
                () -> assertThat(hasLogContaining("----- Response Meta -----")).isTrue(),
                () -> assertThat(hasLogContaining("Status Code: 200")).isTrue(),
                () -> assertThat(hasLogContaining("===== Response Body =====")).isTrue(),
                () -> assertThat(hasLogContaining("(empty)")).isTrue(), // 空ボディの表示
                () -> assertThat(hasLogContaining("=========================")).isTrue()
            );
        }
    }
}

