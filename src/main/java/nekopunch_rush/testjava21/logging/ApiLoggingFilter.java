package nekopunch_rush.testjava21.logging;

import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.reactivestreams.Publisher;
import org.springframework.core.io.buffer.DataBuffer;
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
                    // Content-Typeを取得（バイナリ判定用）
                    MediaType contentType = request.headers().getContentType();

                    ClientHttpRequestDecorator decorator = new ClientHttpRequestDecorator(outputMessage) {
                        final ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();

                        @Override
                        @NonNull
                        public Mono<Void> writeWith(@NonNull Publisher<? extends DataBuffer> body) {
                            // ボディがあるリクエスト（POST、PUTなど）の場合
                            return super.writeWith(Flux.from(body)
                                .doOnNext(dataBuffer -> copy(dataBuffer, byteArrayOutputStream))
                                .doOnComplete(() -> {
                                    loggingRequestBody(stringBuilder, byteArrayOutputStream.toByteArray(), contentType);
                                    log.info("[Request]\n{}", stringBuilder);
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
                                    loggingRequestBody(stringBuilder, byteArrayOutputStream.toByteArray(), contentType);
                                    log.info("[Request]\n{}", stringBuilder);
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
        stringBuilder.append("\n----- Request Information -----\n");
        stringBuilder.append("METHOD: ").append(req.method()).append("\n");
        stringBuilder.append("URL: ").append(req.url()).append("\n");
        stringBuilder.append("HEADERS: ").append("\n");
        StringBuilder finalStringBuilder = stringBuilder;
        req.headers().forEach((key, values) -> {
            finalStringBuilder.append("    ").append(key).append(": ");
            values.forEach(value -> finalStringBuilder.append(value).append(", "));
            finalStringBuilder.append("\n");
        });
        stringBuilder.append("-------------------------------\n");
    }

    /**
     * リクエストボディをStringBuilderに追記する。
     * <p>
     * ボディが{@link #MAX_BODY_SIZE}を超える場合は切り詰めて出力する。
     * バイナリコンテンツの場合はサイズ情報のみを出力する。
     *
     * @param stringBuilder ログ出力用のStringBuilder
     * @param bodyBytes ボディのバイト配列（nullまたは空の場合は適切なメッセージを出力）
     * @param contentType Content-Type（バイナリ判定に使用、nullの場合はテキストとして扱う）
     */
    private static void loggingRequestBody(StringBuilder stringBuilder, byte[] bodyBytes, MediaType contentType) {
        if (stringBuilder == null) {
            stringBuilder = new StringBuilder();
        }
        stringBuilder.append("===== Request Body =====\n");
        if (bodyBytes == null) {
            stringBuilder.append("null\n");
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
        stringBuilder.append("========================\n");
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
        stringBuilder.append("\n----- Response Information -----\n");
        stringBuilder.append("STATUS CODE: ").append(response.statusCode()).append("\n");
        stringBuilder.append("HEADERS: ").append("\n");
        StringBuilder finalStringBuilder = stringBuilder;
        response.headers().asHttpHeaders().forEach((key, values) -> {
            values.forEach(value -> finalStringBuilder.append("    ").append(key).append(": ").append(value).append("\n"));
        });
        stringBuilder.append("--------------------------------\n");
    }

    /**
     * レスポンスをログ出力するExchangeFilterFunctionを返す。
     * <p>
     * 出力内容:
     * <ul>
     *   <li>ステータスコード</li>
     *   <li>ヘッダー</li>
     *   <li>レスポンスボディ（先頭{@link #MAX_BODY_SIZE}バイトまで）</li>
     * </ul>
     * <p>
     * ボディが{@link #MAX_BODY_SIZE}を超える場合は先頭部分のみログ出力し、
     * 残りのデータはそのままストリーミングで流す。
     * これにより大きなレスポンスでもメモリ消費を抑えつつログ出力が可能。
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

                // ログ用に先頭部分だけをキャプチャするためのバッファ
                ByteArrayOutputStream logBuffer = new ByteArrayOutputStream();
                // キャプチャ済みバイト数を追跡（配列で包んでラムダ内から変更可能にする）
                int[] capturedBytes = {0};
                // ログ出力済みフラグ
                boolean[] logged = {false};

                Flux<DataBuffer> bodyFlux = response.bodyToFlux(DataBuffer.class)
                    .doOnNext(dataBuffer -> {
                        // まだログ用のキャプチャ上限に達していない場合のみコピー
                        if (capturedBytes[0] < MAX_BODY_SIZE) {
                            int remaining = MAX_BODY_SIZE - capturedBytes[0];
                            int readable = dataBuffer.readableByteCount();
                            int toCopy = Math.min(remaining, readable);

                            if (toCopy > 0) {
                                byte[] bytes = new byte[toCopy];
                                // DataBufferの読み取り位置を変更せずにコピー
                                int readPosition = dataBuffer.readPosition();
                                dataBuffer.read(bytes);
                                dataBuffer.readPosition(readPosition); // 読み取り位置を元に戻す
                                logBuffer.writeBytes(bytes);
                                capturedBytes[0] += toCopy;
                            }
                        }
                    })
                    .doOnComplete(() -> {
                        if (!logged[0]) {
                            logged[0] = true;
                            byte[] capturedData = logBuffer.toByteArray();
                            loggingResponseBody(stringBuilder, capturedData, capturedBytes[0], contentType);
                            log.info("[Response]\n{}", stringBuilder);
                        }
                    })
                    .doOnError(e -> {
                        if (!logged[0]) {
                            logged[0] = true;
                            loggingResponseWithBodyError(stringBuilder, e);
                            log.error("[Response]\n{}", stringBuilder);
                        }
                    })
                    .switchIfEmpty(Flux.defer(() -> {
                        // ボディが完全に空の場合
                        if (!logged[0]) {
                            logged[0] = true;
                            loggingResponseBody(stringBuilder, new byte[0], 0, contentType);
                            log.info("[Response]\n{}", stringBuilder);
                        }
                        return Flux.empty();
                    }));

                return Mono.just(ClientResponse.create(response.statusCode(), response.strategies())
                    .headers(headers -> headers.addAll(response.headers().asHttpHeaders()))
                    .cookies(cookie -> cookie.addAll(response.cookies()))
                    .body(bodyFlux)
                    .build());
            });
    }

    /**
     * レスポンスボディをログ出力する。
     * <p>
     * キャプチャしたデータが{@link #MAX_BODY_SIZE}に達している場合は切り詰めメッセージを付与する。
     *
     * @param stringBuilder ログ出力用のStringBuilder
     * @param capturedData キャプチャしたボディデータ
     * @param totalCaptured キャプチャしたバイト数（実際のボディサイズはこれ以上の可能性あり）
     * @param contentType Content-Type
     */
    private static void loggingResponseBody(StringBuilder stringBuilder, byte[] capturedData, int totalCaptured, MediaType contentType) {
        if (stringBuilder == null) {
            stringBuilder = new StringBuilder();
        }
        stringBuilder.append("===== Response Body =====\n");

        if (capturedData == null || capturedData.length == 0) {
            stringBuilder.append("(empty)\n");
        } else if (isBinaryContentType(contentType)) {
            // バイナリデータの場合はサイズ情報のみ出力
            String contentTypeStr = contentType.toString();
            if (totalCaptured >= MAX_BODY_SIZE) {
                stringBuilder.append("[Binary data: ").append(totalCaptured).append("+ bytes (truncated), Content-Type: ").append(contentTypeStr).append("]\n");
            } else {
                stringBuilder.append("[Binary data: ").append(totalCaptured).append(" bytes, Content-Type: ").append(contentTypeStr).append("]\n");
            }
        } else if (totalCaptured >= MAX_BODY_SIZE) {
            // テキストデータで上限に達した場合
            stringBuilder.append(new String(capturedData, StandardCharsets.UTF_8));
            stringBuilder.append("\n... (truncated at ").append(MAX_BODY_SIZE).append(" bytes, actual size may be larger)\n");
        } else {
            stringBuilder.append(new String(capturedData, StandardCharsets.UTF_8)).append("\n");
        }
        stringBuilder.append("=========================\n");
    }
}
