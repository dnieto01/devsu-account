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
    BAD_REQUEST(8, "Bad request", 400),
    INVALID_TRANSFER_STATUS(9, "Invalid transfer status", 400),
    INVALID_TRANSFER_STATUS_DETAIL(9, "Invalid transfer status detail", 400),
    ERROR_PUBLISHING_DATABASE(10, "Error try to publish in database", 400),
    ERROR_PUBLISHING_VALUE_STORAGE(11, "Error try to publish in value storage", 400),


    INVALID_PAYMENT_TYPE(400, "Invalid payment type", 400),
    INVALID_AMOUNT(401, "Invalid amount", 400),
    INVALID_PAYMENT_DATE(402, "Invalid payment date", 400),
    INVALID_CLIENT_ACCOUNT(403, "Invalid client account", 400),
    ERROR_PAYMENT_CONTROLLER_NOT_AUTHORIZED(404, "Money in not authorized", 400),
    INVALID_ACCOUNT_TYPE(405, "Invalid account type", 400),
    ERROR_INVALID_TRANSFER(406, "Invalid transfer information", 400),
    CLIENT_NOT_HAVE_ENOUGH_AMOUNT(407, "Saldo no disponible.", 200),
    //The client does not have sufficient funds in this account


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
