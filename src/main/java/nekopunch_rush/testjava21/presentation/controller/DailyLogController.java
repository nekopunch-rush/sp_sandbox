package nekopunch_rush.testjava21.presentation.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import nekopunch_rush.testjava21.application.DailyLogService;
import nekopunch_rush.testjava21.domain.lifelog.DailyLog;
import nekopunch_rush.testjava21.presentation.assembler.DailyLogAssembler;
import nekopunch_rush.testjava21.presentation.resoource.lifelog.ApiResponse;
import nekopunch_rush.testjava21.presentation.resoource.lifelog.DailyLogRequest;
import nekopunch_rush.testjava21.presentation.resoource.lifelog.DailyLogResponse;
import nekopunch_rush.testjava21.presentation.resoource.lifelog.DailyLogSummaryResponse;
import org.apache.ibatis.javassist.NotFoundException;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;

@RestController
@RequestMapping("/daily-logs")
@RequiredArgsConstructor
public class DailyLogController {

    private final DailyLogService dailyLogService;

    // 登録
    @PostMapping
    public ResponseEntity<ApiResponse<?>> register(@RequestBody @Valid DailyLogRequest request) {

        DailyLog log = DailyLogAssembler.toDomain(request);
        dailyLogService.register(log);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.builder()
                        .success(true)
                        .message("登録が完了しました。")
                        .data(null)
                        .build());
    }

    // 更新
//    @PutMapping("/{userId}/{logDate}")
//    public ResponseEntity<ApiResponse<?>> update(
//            @PathVariable Long userId,
//            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate logDate,
//            @RequestBody @Valid DailyLogRequest request) {
//
//        DailyLog log = DailyLogAssembler.toDomain(request);
//        //dailyLogService.update(userId, logDate, log);
//        return ResponseEntity.ok(
//                ApiResponse.builder()
//                        .success(true)
//                        .message("更新が完了しました。")
//                        .data(null)
//                        .build());
//    }

    // 削除
    @DeleteMapping("/{userId}/{logDate}")
    public ResponseEntity<ApiResponse<?>> delete(
            @PathVariable Long userId,
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate logDate)
            throws NotFoundException {
        dailyLogService.delete(userId, logDate);
        return ResponseEntity.ok(
                ApiResponse.builder()
                .success(true)
                .message("削除が完了しました。")
                .data(null)
                .build());
    }

    // 詳細取得
    @GetMapping("/{userId}/{logDate}")
    public ResponseEntity<ApiResponse<?>> getByUserIdAndDate(
            @PathVariable Long userId,
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate logDate)
            throws NotFoundException {

        DailyLog log = dailyLogService.findByUserIdAndDate(userId, logDate);
        DailyLogResponse response = DailyLogAssembler.toResponse(log);
        return ResponseEntity.ok(
                ApiResponse.builder()
                        .success(true)
                        .message("取得成功")
                        .data(response)
                        .build());
    }

    // 一覧取得（月単位）
    @GetMapping("/{userId}/{yearMonth}")
    public ResponseEntity<ApiResponse<?>> getMonthly(
            @PathVariable Long userId,
            @PathVariable @DateTimeFormat(pattern = "yyyy-MM") YearMonth yearMonth) {

        List<DailyLog> logs = dailyLogService.findMonthlyByUserId(userId, yearMonth);
        List<DailyLogSummaryResponse> responseList = logs.stream()
                .map(log -> DailyLogSummaryResponse.builder()
                        .logDate(log.getLogDate())
                        .sleepHours(log.getSleepHours())
                        .moodLevel(log.getMoodLevel())
                        .build())
                .toList();

        return ResponseEntity.ok(
                ApiResponse.builder()
                        .success(true)
                        .message("取得成功")
                        .data(responseList)
                        .build());
    }
}

