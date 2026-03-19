package com.ds.devsuaccount.infraestructure.exceptions;

import lombok.Getter;

@Getter
public class ApiErrorResponse {
    private final String code;
    private final String description;
    private final Integer statusCode;

    public ApiErrorResponse(ApiException ex) {
        this.code = ex.getCode();
        this.description = ex.getDescription();
        this.statusCode = ex.getStatusCode();
    }
}