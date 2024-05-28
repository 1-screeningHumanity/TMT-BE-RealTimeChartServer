package tmt.realtimechartservice.global.common.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.springframework.http.HttpStatus;

public record BaseResponse<T>(
        @JsonProperty("httpStatus") HttpStatus httpStatus,
        @JsonProperty("isSuccess") Boolean isSuccess,
        @JsonProperty("message") String message,
        @JsonProperty("code") int code,
        @JsonProperty("data") T result
) {


    // 요청에 성공한 경우 -> return 객체가 필요한 경우
    public BaseResponse(T result) {
        this(
                BaseResponseCode.SUCCESS.getHttpStatus(),
                BaseResponseCode.SUCCESS.isSuccess(),
                BaseResponseCode.SUCCESS.getMessage(),
                BaseResponseCode.SUCCESS.getCode(),
                result
        );
    }

    // 요청에 성공한 경우 -> return 객체가 필요 없는 경우
    public BaseResponse() {
        this(
                BaseResponseCode.SUCCESS.getHttpStatus(),
                BaseResponseCode.SUCCESS.isSuccess(),
                BaseResponseCode.SUCCESS.getMessage(),
                BaseResponseCode.SUCCESS.getCode(),
                null
        );
    }

    // 요청 실패한 경우
    public BaseResponse(BaseResponseCode status) {
        this(
                status.getHttpStatus(),
                status.isSuccess(),
                status.getMessage(),
                status.getCode(),
                null
        );
    }
}
