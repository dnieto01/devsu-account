package com.ds.devsuaccount.infraestructure.exceptions;

import lombok.Getter;

@Getter
public enum ErrorCode {
    ROUTE_NOT_FOUND(0, "Route not found", 404),
    RESOURCE_ALREADY_LOCKED(2, "Resource already locked", 400),
    ERROR_LOCKING_RESOURCE(3, "Error locking resource", 423),
    MISSING_CONFIG_PROPERTY(4, "Error while attempting to configure the services: a property is missing", 400),
    QUEUE_PRODUCER_CREATION_FAILED(5, "Error creating producer", 400),
    QUEUE_CLIENT_NOT_FOUND(6, "Queue client not found", 400),
    ERROR_QUEUE_PUBLISH_MESSAGE(7, "Error publishing message", 500),


    ;

    private String code;
    private String message;
    private int statusCode;

    ErrorCode(int code, String message, Integer statusCode) {
        this.code = String.format("%s", code);
        this.message = message;
        this.statusCode = statusCode;
    }

    ErrorCode(int code, String message) {
        this(code, message, 500);
    }

}
