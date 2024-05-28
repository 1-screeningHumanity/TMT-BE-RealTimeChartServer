package tmt.realtimechartservice.global.common.exception;

import lombok.Getter;
import tmt.realtimechartservice.global.common.response.BaseResponseCode;

@Getter
public class CustomException extends RuntimeException {

    private final BaseResponseCode status;

    public CustomException(BaseResponseCode status) {
        this.status = status;
    }
}
