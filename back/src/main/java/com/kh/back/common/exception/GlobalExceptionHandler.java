package com.kh.back.common.exception;

import com.kh.back.common.dto.ApiResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.ErrorResponse;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

/**
 * 모든 Controller 에서 던져진 예외를 한곳에서 받아 일관된 실패 봉투(ApiResponse)로 응답한다.
 * 덕분에 Controller 마다 try-catch 를 쓰지 않아도 된다.
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    // Service/비즈니스 로직에서 커스텀하게 던진 예외 (예: 없는 종목코드 → 404)
    @ExceptionHandler(ApiException.class)
    public ResponseEntity<ApiResponse<Void>> handleApi(ApiException e) {
        return ResponseEntity.status(e.getStatus())
                .body(ApiResponse.fail(e.getCode(), e.getMessage()));
    }

    // Spring Framework가 자체적으로 상태 코드를 알고 있는 예외들 (404, 405, 415 등)
    // 500으로 뭉개지지 않고 Spring이 정의한 원래 4xx HTTP 상태 코드를 유지한다.
    @ExceptionHandler({
            NoResourceFoundException.class,                  // 없는 주소 (404)
            HttpRequestMethodNotSupportedException.class,   // 지원하지 않는 HTTP 메서드 (405)
            HttpMediaTypeNotSupportedException.class,        // 지원하지 않는 Content-Type (415)
            ResponseStatusException.class                    // ResponseStatusException (지정 상태)
    })
    public ResponseEntity<ApiResponse<Void>> handleSpring(Exception e) {
        HttpStatus status = HttpStatus.valueOf(((ErrorResponse) e).getStatusCode().value());
        return ResponseEntity.status(status)
                .body(ApiResponse.fail(status.name(), status.getReasonPhrase()));
    }

    // 쿼리 파라미터/경로 변수의 타입이 맞지 않음 (예: GET /api/v1/items?page=abc)
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ApiResponse<Void>> handleTypeMismatch(MethodArgumentTypeMismatchException e) {
        return ResponseEntity.badRequest()
                .body(ApiResponse.fail("BAD_REQUEST", e.getName() + " 값이 올바르지 않습니다."));
    }

    // 요청 본문(JSON)을 읽을 수 없음 (예: 형식이 깨진 JSON, 바디 누락)
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiResponse<Void>> handleNotReadable(HttpMessageNotReadableException e) {
        return ResponseEntity.badRequest()
                .body(ApiResponse.fail("BAD_REQUEST", "요청 본문을 읽을 수 없습니다."));
    }

    // @Valid / @Validated 검증 실패 (예: @NotBlank, @Min 조건 위반)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Void>> handleValidation(MethodArgumentNotValidException e) {
        String errorMessage = e.getBindingResult().getAllErrors().get(0).getDefaultMessage();
        return ResponseEntity.badRequest()
                .body(ApiResponse.fail("BAD_REQUEST", errorMessage));
    }

    // 그 밖의 모든 예외 → 500 Internal Server Error
    // 원인은 서버 로그에만 남기고, 클라이언트에는 내부 상세 에러를 노출하지 않는다.
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleEtc(Exception e) {
        log.error("처리되지 않은 예외", e);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.fail("INTERNAL_ERROR", "서버 오류가 발생했습니다."));
    }
}