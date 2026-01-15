package nekopunch_rush.testjava21.logging;

import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.reactivestreams.Publisher;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DefaultDataBufferFactory;
import org.springframework.http.client.reactive.ClientHttpRequestDecorator;
import org.springframework.web.reactive.function.client.ClientRequest;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import org.springframework.http.MediaType;

import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Set;

/**
 * WebClientのリクエスト/レスポンスをログ出力するためのフィルタークラス。
 * <p>
 * 使用例:
 * <pre>
 * WebClient webClient = WebClient.builder()
 *     .filter(ApiLoggingFilter.logRequest())
 *     .filter(ApiLoggingFilter.logResponse())
 *     .build();
 * </pre>
 */
@Slf4j
public class ApiLoggingFilter {

    /** ログ出力するボディの最大サイズ（バイト） */
    private static final int MAX_BODY_SIZE = 10 * 1024; // 10KB

    /** バイナリとして扱うContent-Typeのセット */
    private static final Set<String> BINARY_MEDIA_TYPES = Set.of(
        "application/octet-stream",
        "application/pdf",
        "application/zip",
        "application/gzip",
        "application/x-gzip",
        "application/x-tar",
        "application/x-rar-compressed",
        "application/x-7z-compressed",
        "application/vnd.ms-excel",
        "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
        "application/msword",
        "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
        "application/vnd.ms-powerpoint",
        "application/vnd.openxmlformats-officedocument.presentationml.presentation"
    );

    /** バイナリとして扱うContent-Typeのプレフィックス */
    private static final Set<String> BINARY_MEDIA_TYPE_PREFIXES = Set.of(
        "image/",
        "audio/",
        "video/",
        "font/"
    );

    /**
     * リクエストをログ出力するExchangeFilterFunctionを返す。
     * <p>
     * 出力内容:
     * <ul>
     *   <li>HTTPメソッド</li>
     *   <li>URL</li>
     *   <li>ヘッダー</li>
     *   <li>リクエストボディ（POST、PUTなどの場合）</li>
     * </ul>
     * ボディが{@link #MAX_BODY_SIZE}を超える場合は切り詰めて出力する。
     *
     * @return リクエストログ出力用のExchangeFilterFunction
     */
    public static ExchangeFilterFunction logRequest() {
        return (request, next) -> {
            StringBuilder stringBuilder = new StringBuilder();

            // リクエストメタ情報のログ出力
            loggingRequestWithMeta(stringBuilder, request);

            ClientRequest mutated = ClientRequest.from(request)
                .body((outputMessage, context) -> {
                    ClientHttpRequestDecorator decorator = new ClientHttpRequestDecorator(outputMessage) {
                        final ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();

                        @Override
                        @NonNull
                        public Mono<Void> writeWith(@NonNull Publisher<? extends DataBuffer> body) {
                            // ボディがあるリクエスト（POST、PUTなど）の場合
                            return super.writeWith(Flux.from(body)
                                .doOnNext(dataBuffer -> copy(dataBuffer, byteArrayOutputStream))
                                .doOnComplete(() -> {
                                    loggingBody(stringBuilder, byteArrayOutputStream.toByteArray(), true);
                                    log.info("{}", stringBuilder);
                                }));
                        }

                        @Override
                        @NonNull
                        public Mono<Void> writeAndFlushWith(@NonNull Publisher<? extends Publisher<? extends DataBuffer>> body) {
                            // ストリーミングでボディを送信する場合
                            return super.writeAndFlushWith(Flux.from(body)
                                .map(inner -> Flux.from(inner)
                                    .doOnNext(dataBuffer -> copy(dataBuffer, byteArrayOutputStream)))
                                .doOnComplete(() -> {
                                    loggingBody(stringBuilder, byteArrayOutputStream.toByteArray(), true);
                                    log.info("{}", stringBuilder);
                                }));
                        }

                        @Override
                        @NonNull
                        public Mono<Void> setComplete() {
                            // ボディがないリクエスト（GETなど）の場合
                            log.info("{}", stringBuilder);
                            return super.setComplete();
                        }
                    };
                    return request.body().insert(decorator, context);
                })
                .build();

            return next.exchange(mutated)
                .doOnError(e -> {
                    // 接続エラー・タイムアウト等の場合
                    stringBuilder.append("Error during exchange: ").append(e.getMessage()).append("\n");
                    log.error("[Request Error]\n{}", stringBuilder);
                });
        };
    }

    /**
     * DataBufferの内容をByteArrayOutputStreamにコピーする。
     *
     * @param dataBuffer コピー元のDataBuffer
     * @param byteArrayOutputStream コピー先のByteArrayOutputStream
     */
    private static void copy(DataBuffer dataBuffer, ByteArrayOutputStream byteArrayOutputStream) {
        try (DataBuffer.ByteBufferIterator it = dataBuffer.readableByteBuffers()) {
            while (it.hasNext()) {
                ByteBuffer byteBuffer = it.next();
                byte[] bytes = new byte[byteBuffer.remaining()];
                byteBuffer.get(bytes);
                byteArrayOutputStream.writeBytes(bytes);
            }
        }
    }

    /**
     * リクエストのメタ情報（メソッド、URL、ヘッダー）をStringBuilderに追記する。
     *
     * @param stringBuilder ログ出力用のStringBuilder
     * @param req リクエスト情報
     */
    private static void loggingRequestWithMeta(StringBuilder stringBuilder, ClientRequest req) {
        if (stringBuilder == null) {
            stringBuilder = new StringBuilder();
        }
        stringBuilder.append("\n----- Request Meta -----\n");
        stringBuilder.append("Method: ").append(req.method()).append("\n");
        stringBuilder.append("URL: ").append(req.url()).append("\n");
        stringBuilder.append("HEADERS: ").append("\n");
        StringBuilder finalStringBuilder = stringBuilder;
        req.headers().forEach((key, values) -> {
            finalStringBuilder.append("  ").append(key).append(": ");
            values.forEach(value -> finalStringBuilder.append(value).append(", "));
            finalStringBuilder.append("\n");
        });
        stringBuilder.append("------------------------\n");
    }

    /**
     * リクエストまたはレスポンスのボディをStringBuilderに追記する。
     * <p>
     * ボディが{@link #MAX_BODY_SIZE}を超える場合は切り詰めて出力する。
     * バイナリコンテンツの場合はサイズ情報のみを出力する。
     *
     * @param stringBuilder ログ出力用のStringBuilder
     * @param bodyBytes ボディのバイト配列（nullまたは空の場合は適切なメッセージを出力）
     * @param isRequest trueの場合はリクエスト、falseの場合はレスポンス
     * @param contentType Content-Type（バイナリ判定に使用、nullの場合はテキストとして扱う）
     */
    private static void loggingBody(StringBuilder stringBuilder, byte[] bodyBytes, boolean isRequest, MediaType contentType) {
        if (stringBuilder == null) {
            stringBuilder = new StringBuilder();
        }
        String label = isRequest ? "Request" : "Response";
        stringBuilder.append("===== ").append(label).append(" Body =====\n");
        if (bodyBytes == null) {
            stringBuilder.append(isRequest ? "null" : "(no body)").append("\n");
        } else if (bodyBytes.length == 0) {
            stringBuilder.append("(empty)\n");
        } else if (isBinaryContentType(contentType)) {
            // バイナリデータの場合はサイズ情報のみ出力
            String contentTypeStr = contentType.toString();
            stringBuilder.append("[Binary data: ").append(bodyBytes.length).append(" bytes, Content-Type: ").append(contentTypeStr).append("]\n");
        } else if (bodyBytes.length > MAX_BODY_SIZE) {
            String truncatedBody = new String(bodyBytes, 0, MAX_BODY_SIZE, StandardCharsets.UTF_8);
            stringBuilder.append(truncatedBody);
            stringBuilder.append("\n... (truncated, total size: ").append(bodyBytes.length).append(" bytes)\n");
        } else {
            stringBuilder.append(new String(bodyBytes, StandardCharsets.UTF_8)).append("\n");
        }
        stringBuilder.append(isRequest ? "========================\n" : "=========================\n");
    }

    /**
     * Content-Type引数なしのオーバーロード（後方互換性のため）。
     * テキストとして扱う。
     *
     * @param stringBuilder ログ出力用のStringBuilder
     * @param bodyBytes ボディのバイト配列
     * @param isRequest trueの場合はリクエスト、falseの場合はレスポンス
     */
    private static void loggingBody(StringBuilder stringBuilder, byte[] bodyBytes, boolean isRequest) {
        loggingBody(stringBuilder, bodyBytes, isRequest, null);
    }

    /**
     * 指定されたContent-Typeがバイナリデータかどうかを判定する。
     *
     * @param contentType 判定対象のMediaType（nullの場合はfalseを返す）
     * @return バイナリデータの場合はtrue
     */
    private static boolean isBinaryContentType(MediaType contentType) {
        if (contentType == null) {
            return false;
        }

        String type = contentType.getType();
        String subtype = contentType.getSubtype();
        String fullType = type + "/" + subtype;

        // 完全一致でバイナリ判定
        if (BINARY_MEDIA_TYPES.contains(fullType)) {
            return true;
        }

        // プレフィックス一致でバイナリ判定（image/*, audio/*, video/*, font/*）
        String typePrefix = type + "/";
        return BINARY_MEDIA_TYPE_PREFIXES.contains(typePrefix);
    }

    /**
     * レスポンスボディ取得時のエラー情報をStringBuilderに追記する。
     *
     * @param stringBuilder ログ出力用のStringBuilder
     * @param e 発生した例外
     */
    private static void loggingResponseWithBodyError(StringBuilder stringBuilder, Throwable e) {
        if (stringBuilder == null) {
            stringBuilder = new StringBuilder();
        }
        stringBuilder.append("===== Response Body =====\n");
        stringBuilder.append("Error reading body: ").append(e.getMessage()).append("\n");
        stringBuilder.append("=========================\n");
    }

    /**
     * レスポンスのメタ情報（ステータスコード、ヘッダー）をStringBuilderに追記する。
     *
     * @param stringBuilder ログ出力用のStringBuilder
     * @param response レスポンス情報
     */
    private static void loggingResponseWithMeta(StringBuilder stringBuilder, ClientResponse response) {
        if (stringBuilder == null) {
            stringBuilder = new StringBuilder();
        }
        stringBuilder.append("\n----- Response Meta -----\n");
        stringBuilder.append("Status Code: ").append(response.statusCode().value()).append("\n");
        stringBuilder.append("Headers: ").append(response.headers().asHttpHeaders()).append("\n");
        stringBuilder.append("-------------------------\n");
    }

    /**
     * レスポンスをログ出力するExchangeFilterFunctionを返す。
     * <p>
     * 出力内容:
     * <ul>
     *   <li>ステータスコード</li>
     *   <li>ヘッダー</li>
     *   <li>レスポンスボディ</li>
     * </ul>
     * ボディが{@link #MAX_BODY_SIZE}を超える場合は切り詰めて出力する。
     * <p>
     * 注意: レスポンスボディを一度読み取ってログ出力した後、
     * 新しいClientResponseを再構築して返すため、ストリーミングレスポンスには適さない場合がある。
     *
     * @return レスポンスログ出力用のExchangeFilterFunction
     */
    public static ExchangeFilterFunction logResponse() {
        return (request, next) -> next.exchange(request)
            .flatMap(response -> {
                StringBuilder stringBuilder = new StringBuilder();

                // レスポンスメタ情報のログ出力
                loggingResponseWithMeta(stringBuilder, response);

                // Content-Typeを取得（バイナリ判定用）
                MediaType contentType = response.headers().contentType().orElse(null);

                return response.bodyToMono(byte[].class)
                    .defaultIfEmpty(new byte[0])
                    .map(bytes -> {
                        // ボディが存在する場合（Content-Typeを渡してバイナリ判定）
                        loggingBody(stringBuilder, bytes, false, contentType);
                        log.info("[Response]\n{}", stringBuilder);

                        return ClientResponse.create(response.statusCode(), response.strategies())
                            .headers(headers -> headers.addAll(response.headers().asHttpHeaders()))
                            .cookies(cookie -> cookie.addAll(response.cookies()))
                            .body(Flux.just(new DefaultDataBufferFactory().wrap(bytes)))
                            .build();
                    })
                    .switchIfEmpty(Mono.defer(() -> {
                        // ボディが完全にない場合
                        loggingBody(stringBuilder, null, false, contentType);
                        log.info("[Response]\n{}", stringBuilder);

                        return Mono.just(ClientResponse.create(response.statusCode(), response.strategies())
                            .headers(headers -> headers.addAll(response.headers().asHttpHeaders()))
                            .cookies(cookie -> cookie.addAll(response.cookies()))
                            .build());
                    }))
                    .doOnError(e -> {
                        // ボディ取得エラーの場合
                        loggingResponseWithBodyError(stringBuilder, e);
                        log.error("[Response]\n{}", stringBuilder);
                    });
            });
    }
}
