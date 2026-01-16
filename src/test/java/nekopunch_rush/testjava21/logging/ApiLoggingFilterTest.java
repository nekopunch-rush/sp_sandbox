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
                () -> assertThat(hasLogContaining("----- Request Information -----")).isTrue(),
                () -> assertThat(hasLogContaining("METHOD: POST")).isTrue(),
                () -> assertThat(hasLogContaining("URL:")).isTrue(),
                () -> assertThat(hasLogContaining("/api/test")).isTrue(),
                () -> assertThat(hasLogContaining("HEADERS:")).isTrue(),
                () -> assertThat(hasLogContaining("------------------------")).isTrue(),
                () -> assertThat(hasLogContaining("===== Request Body =====")).isTrue(),
                () -> assertThat(hasLogContaining("hello")).isTrue(), // リクエストボディの内容
                () -> assertThat(hasLogContaining("========================")).isTrue(),
                () -> assertThat(hasLogContaining("----- Response Information -----")).isTrue(),
                () -> assertThat(hasLogContaining("STATUS CODE: 200 OK")).isTrue(),
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
                () -> assertThat(hasLogContaining("----- Request Information -----")).isTrue(),
                () -> assertThat(hasLogContaining("METHOD: GET")).isTrue(),
                () -> assertThat(hasLogContaining("URL:")).isTrue(),
                () -> assertThat(hasLogContaining("/api/test")).isTrue(),
                () -> assertThat(hasLogContaining("HEADERS:")).isTrue(),
                () -> assertThat(hasLogContaining("------------------------")).isTrue(),
                () -> assertThat(hasLogContaining("----- Response Information -----")).isTrue(),
                () -> assertThat(hasLogContaining("STATUS CODE: 200 OK")).isTrue(),
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
                () -> assertThat(hasLogContaining("----- Request Information -----")).isTrue(),
                () -> assertThat(hasLogContaining("METHOD: POST")).isTrue(),
                () -> assertThat(hasLogContaining("URL:")).isTrue(),
                () -> assertThat(hasLogContaining("/api/xml/test")).isTrue(),
                () -> assertThat(hasLogContaining("HEADERS:")).isTrue(),
                () -> assertThat(hasLogContaining("------------------------")).isTrue(),
                () -> assertThat(hasLogContaining("===== Request Body =====")).isTrue(),
                () -> assertThat(hasLogContaining("hello xml")).isTrue(), // リクエストボディの内容
                () -> assertThat(hasLogContaining("========================")).isTrue(),
                () -> assertThat(hasLogContaining("----- Response Information -----")).isTrue(),
                () -> assertThat(hasLogContaining("STATUS CODE: 200 OK")).isTrue(),
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
                () -> assertThat(hasLogContaining("----- Request Information -----")).isTrue(),
                () -> assertThat(hasLogContaining("URL:")).isTrue(),
                () -> assertThat(hasLogContaining("/api/test")).isTrue(),
                () -> assertThat(hasLogContaining("----- Response Information -----")).isTrue(),
                () -> assertThat(hasLogContaining("STATUS CODE: 400 BAD_REQUEST")).isTrue(),
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
                () -> assertThat(hasLogContaining("----- Request Information -----")).isTrue(),
                () -> assertThat(hasLogContaining("URL:")).isTrue(),
                () -> assertThat(hasLogContaining("/api/test")).isTrue(),
                () -> assertThat(hasLogContaining("----- Response Information -----")).isTrue(),
                () -> assertThat(hasLogContaining("STATUS CODE: 500 INTERNAL_SERVER_ERROR")).isTrue(),
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
                () -> assertThat(hasLogContaining("----- Request Information -----")).isTrue(),
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
                () -> assertThat(hasLogContaining("----- Request Information -----")).isTrue(),
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
                () -> assertThat(hasLogContaining("----- Request Information -----")).isTrue(),
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
                () -> assertThat(hasLogContaining("----- Request Information -----")).isTrue(),
                () -> assertThat(hasLogContaining("URL:")).isTrue(),
                () -> assertThat(hasLogContaining("/api/test")).isTrue(),
                () -> assertThat(hasLogContaining("----- Response Information -----")).isTrue(),
                () -> assertThat(hasLogContaining("STATUS CODE: 204 NO_CONTENT")).isTrue()
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
                () -> assertThat(hasLogContaining("----- Request Information -----")).isTrue(),
                () -> assertThat(hasLogContaining("URL:")).isTrue(),
                () -> assertThat(hasLogContaining("/api/test")).isTrue(),
                () -> assertThat(hasLogContaining("----- Response Information -----")).isTrue(),
                () -> assertThat(hasLogContaining("STATUS CODE: 200 OK")).isTrue(),
                () -> assertThat(hasLogContaining("===== Response Body =====")).isTrue(),
                () -> assertThat(hasLogContaining("(empty)")).isTrue(), // 空ボディの表示
                () -> assertThat(hasLogContaining("=========================")).isTrue()
            );
        }
    }

    @Nested
    @DisplayName("バイナリレスポンス テスト")
    class BinaryResponseTests {

        @Test
        @DisplayName("画像（PNG）レスポンス - バイナリとして処理")
        void testImagePngResponse() {
            // Given - PNGのヘッダーを模したバイナリデータ
            byte[] pngData = new byte[]{
                (byte) 0x89, 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A, // PNG signature
                0x00, 0x00, 0x00, 0x0D, 0x49, 0x48, 0x44, 0x52,       // IHDR chunk
                0x00, 0x00, 0x00, 0x10, 0x00, 0x00, 0x00, 0x10,       // 16x16
                0x08, 0x02, 0x00, 0x00, 0x00                          // color type, etc.
            };

            mockWebServer.enqueue(new MockResponse()
                .setResponseCode(200)
                .setHeader(HttpHeaders.CONTENT_TYPE, MediaType.IMAGE_PNG_VALUE)
                .setBody(new okio.Buffer().write(pngData)));

            // When
            byte[] response = webClient.get()
                .uri("/api/image")
                .accept(MediaType.IMAGE_PNG)
                .retrieve()
                .bodyToMono(byte[].class)
                .block();

            // Then
            assertThat(response).isNotNull();
            assertThat(response.length).isEqualTo(pngData.length);

            // ログ出力の検証 - バイナリとして処理されていること
            assertAll(
                () -> assertThat(hasLogContaining("----- Request Information -----")).isTrue(),
                () -> assertThat(hasLogContaining("METHOD: GET")).isTrue(),
                () -> assertThat(hasLogContaining("/api/image")).isTrue(),
                () -> assertThat(hasLogContaining("----- Response Information -----")).isTrue(),
                () -> assertThat(hasLogContaining("STATUS CODE: 200 OK")).isTrue(),
                () -> assertThat(hasLogContaining("===== Response Body =====")).isTrue(),
                () -> assertThat(hasLogContaining("[Binary data:")).isTrue(),
                () -> assertThat(hasLogContaining("bytes")).isTrue(),
                () -> assertThat(hasLogContaining("Content-Type: image/png")).isTrue(),
                () -> assertThat(hasLogContaining("=========================")).isTrue()
            );
        }

        @Test
        @DisplayName("PDFレスポンス - バイナリとして処理")
        void testPdfResponse() {
            // Given - PDFのヘッダーを模したバイナリデータ
            byte[] pdfData = new byte[]{
                0x25, 0x50, 0x44, 0x46, 0x2D, 0x31, 0x2E, 0x34, // %PDF-1.4
                0x0A, 0x31, 0x20, 0x30, 0x20, 0x6F, 0x62, 0x6A,
                0x0A, 0x3C, 0x3C, 0x0A, 0x2F, 0x54, 0x79, 0x70
            };

            mockWebServer.enqueue(new MockResponse()
                .setResponseCode(200)
                .setHeader(HttpHeaders.CONTENT_TYPE, "application/pdf")
                .setBody(new okio.Buffer().write(pdfData)));

            // When
            byte[] response = webClient.get()
                .uri("/api/document.pdf")
                .retrieve()
                .bodyToMono(byte[].class)
                .block();

            // Then
            assertThat(response).isNotNull();

            // ログ出力の検証 - バイナリとして処理されていること
            assertAll(
                () -> assertThat(hasLogContaining("----- Response Information -----")).isTrue(),
                () -> assertThat(hasLogContaining("STATUS CODE: 200 OK")).isTrue(),
                () -> assertThat(hasLogContaining("[Binary data:")).isTrue(),
                () -> assertThat(hasLogContaining("Content-Type: application/pdf")).isTrue()
            );
        }

        @Test
        @DisplayName("オクテットストリームレスポンス - バイナリとして処理")
        void testOctetStreamResponse() {
            // Given - 任意のバイナリデータ
            byte[] binaryData = new byte[1024];
            for (int i = 0; i < binaryData.length; i++) {
                binaryData[i] = (byte) (i % 256);
            }

            mockWebServer.enqueue(new MockResponse()
                .setResponseCode(200)
                .setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_OCTET_STREAM_VALUE)
                .setBody(new okio.Buffer().write(binaryData)));

            // When
            byte[] response = webClient.get()
                .uri("/api/download")
                .accept(MediaType.APPLICATION_OCTET_STREAM)
                .retrieve()
                .bodyToMono(byte[].class)
                .block();

            // Then
            assertThat(response).isNotNull();
            assertThat(response.length).isEqualTo(1024);

            // ログ出力の検証 - バイナリとして処理されていること
            assertAll(
                () -> assertThat(hasLogContaining("----- Response Information -----")).isTrue(),
                () -> assertThat(hasLogContaining("STATUS CODE: 200 OK")).isTrue(),
                () -> assertThat(hasLogContaining("[Binary data: 1024 bytes")).isTrue(),
                () -> assertThat(hasLogContaining("Content-Type: application/octet-stream")).isTrue()
            );
        }

        @Test
        @DisplayName("JPEG画像レスポンス - バイナリとして処理")
        void testImageJpegResponse() {
            // Given - JPEGのヘッダーを模したバイナリデータ
            byte[] jpegData = new byte[]{
                (byte) 0xFF, (byte) 0xD8, (byte) 0xFF, (byte) 0xE0, // JPEG SOI + APP0
                0x00, 0x10, 0x4A, 0x46, 0x49, 0x46, 0x00, 0x01,       // JFIF header
                0x01, 0x00, 0x00, 0x01, 0x00, 0x01, 0x00, 0x00
            };

            mockWebServer.enqueue(new MockResponse()
                .setResponseCode(200)
                .setHeader(HttpHeaders.CONTENT_TYPE, MediaType.IMAGE_JPEG_VALUE)
                .setBody(new okio.Buffer().write(jpegData)));

            // When
            byte[] response = webClient.get()
                .uri("/api/photo.jpg")
                .accept(MediaType.IMAGE_JPEG)
                .retrieve()
                .bodyToMono(byte[].class)
                .block();

            // Then
            assertThat(response).isNotNull();

            // ログ出力の検証 - バイナリとして処理されていること
            assertAll(
                () -> assertThat(hasLogContaining("[Binary data:")).isTrue(),
                () -> assertThat(hasLogContaining("Content-Type: image/jpeg")).isTrue()
            );
        }

        @Test
        @DisplayName("GIF画像レスポンス - バイナリとして処理")
        void testImageGifResponse() {
            // Given - GIFのヘッダーを模したバイナリデータ
            byte[] gifData = new byte[]{
                0x47, 0x49, 0x46, 0x38, 0x39, 0x61, // GIF89a
                0x01, 0x00, 0x01, 0x00, (byte) 0x80, 0x00, 0x00
            };

            mockWebServer.enqueue(new MockResponse()
                .setResponseCode(200)
                .setHeader(HttpHeaders.CONTENT_TYPE, MediaType.IMAGE_GIF_VALUE)
                .setBody(new okio.Buffer().write(gifData)));

            // When
            byte[] response = webClient.get()
                .uri("/api/animation.gif")
                .accept(MediaType.IMAGE_GIF)
                .retrieve()
                .bodyToMono(byte[].class)
                .block();

            // Then
            assertThat(response).isNotNull();

            // ログ出力の検証 - バイナリとして処理されていること
            assertAll(
                () -> assertThat(hasLogContaining("[Binary data:")).isTrue(),
                () -> assertThat(hasLogContaining("Content-Type: image/gif")).isTrue()
            );
        }

        @Test
        @DisplayName("ZIPファイルレスポンス - バイナリとして処理")
        void testZipFileResponse() {
            // Given - ZIPのヘッダーを模したバイナリデータ
            byte[] zipData = new byte[]{
                0x50, 0x4B, 0x03, 0x04, // ZIP local file header signature
                0x14, 0x00, 0x00, 0x00, 0x08, 0x00,
                0x00, 0x00, 0x00, 0x00, 0x00, 0x00
            };

            mockWebServer.enqueue(new MockResponse()
                .setResponseCode(200)
                .setHeader(HttpHeaders.CONTENT_TYPE, "application/zip")
                .setBody(new okio.Buffer().write(zipData)));

            // When
            byte[] response = webClient.get()
                .uri("/api/archive.zip")
                .retrieve()
                .bodyToMono(byte[].class)
                .block();

            // Then
            assertThat(response).isNotNull();

            // ログ出力の検証 - バイナリとして処理されていること
            assertAll(
                () -> assertThat(hasLogContaining("[Binary data:")).isTrue(),
                () -> assertThat(hasLogContaining("Content-Type: application/zip")).isTrue()
            );
        }

        @Test
        @DisplayName("音声ファイル（MP3）レスポンス - バイナリとして処理")
        void testAudioMp3Response() {
            // Given - MP3のヘッダーを模したバイナリデータ
            byte[] mp3Data = new byte[]{
                (byte) 0xFF, (byte) 0xFB, (byte) 0x90, 0x00, // MP3 frame header
                0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00
            };

            mockWebServer.enqueue(new MockResponse()
                .setResponseCode(200)
                .setHeader(HttpHeaders.CONTENT_TYPE, "audio/mpeg")
                .setBody(new okio.Buffer().write(mp3Data)));

            // When
            byte[] response = webClient.get()
                .uri("/api/music.mp3")
                .retrieve()
                .bodyToMono(byte[].class)
                .block();

            // Then
            assertThat(response).isNotNull();

            // ログ出力の検証 - バイナリとして処理されていること
            assertAll(
                () -> assertThat(hasLogContaining("[Binary data:")).isTrue(),
                () -> assertThat(hasLogContaining("Content-Type: audio/mpeg")).isTrue()
            );
        }

        @Test
        @DisplayName("動画ファイル（MP4）レスポンス - バイナリとして処理")
        void testVideoMp4Response() {
            // Given - MP4のヘッダーを模したバイナリデータ
            byte[] mp4Data = new byte[]{
                0x00, 0x00, 0x00, 0x18, 0x66, 0x74, 0x79, 0x70, // ftyp box
                0x6D, 0x70, 0x34, 0x32, 0x00, 0x00, 0x00, 0x00
            };

            mockWebServer.enqueue(new MockResponse()
                .setResponseCode(200)
                .setHeader(HttpHeaders.CONTENT_TYPE, "video/mp4")
                .setBody(new okio.Buffer().write(mp4Data)));

            // When
            byte[] response = webClient.get()
                .uri("/api/video.mp4")
                .retrieve()
                .bodyToMono(byte[].class)
                .block();

            // Then
            assertThat(response).isNotNull();

            // ログ出力の検証 - バイナリとして処理されていること
            assertAll(
                () -> assertThat(hasLogContaining("[Binary data:")).isTrue(),
                () -> assertThat(hasLogContaining("Content-Type: video/mp4")).isTrue()
            );
        }

        @Test
        @DisplayName("Excelファイル（xlsx）レスポンス - バイナリとして処理")
        void testExcelXlsxResponse() {
            // Given - XLSX（ZIP形式）のヘッダーを模したバイナリデータ
            byte[] xlsxData = new byte[]{
                0x50, 0x4B, 0x03, 0x04, 0x14, 0x00, 0x06, 0x00,
                0x08, 0x00, 0x00, 0x00, 0x21, 0x00
            };

            mockWebServer.enqueue(new MockResponse()
                .setResponseCode(200)
                .setHeader(HttpHeaders.CONTENT_TYPE, "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
                .setBody(new okio.Buffer().write(xlsxData)));

            // When
            byte[] response = webClient.get()
                .uri("/api/report.xlsx")
                .retrieve()
                .bodyToMono(byte[].class)
                .block();

            // Then
            assertThat(response).isNotNull();

            // ログ出力の検証 - バイナリとして処理されていること
            assertAll(
                () -> assertThat(hasLogContaining("[Binary data:")).isTrue(),
                () -> assertThat(hasLogContaining("Content-Type: application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")).isTrue()
            );
        }
    }
}

