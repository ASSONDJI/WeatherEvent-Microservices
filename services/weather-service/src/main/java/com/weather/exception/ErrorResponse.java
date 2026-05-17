package com.weather.exception;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Builder
public class ErrorResponse {
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime timestamp;
    private int status;
    private String error;
    private String errorCode;
    private String message;
    private String path;

    public static ErrorResponse of(int status, String error, String errorCode,
                                    String message, String path) {
        return ErrorResponse.builder()
            .timestamp(LocalDateTime.now())
            .status(status).error(error)
            .errorCode(errorCode).message(message).path(path)
            .build();
    }
}
