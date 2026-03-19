package com.ds.devsuaccount.infraestructure.exceptions;

import lombok.Getter;

@Getter
public class ApiException extends RuntimeException {
    private final String code;
    private final String description;
    private final Integer statusCode;

    public ApiException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.code = errorCode.getCode();
        this.description = errorCode.getMessage();
        this.statusCode = errorCode.getStatusCode();
    }

    public ApiException(String code, String description, Integer statusCode) {
        super(description);
        this.code = code;
        this.description = description;
        this.statusCode = statusCode;
    }
}
