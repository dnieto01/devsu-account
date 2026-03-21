package com.ds.devsuaccount.domain.enums;

import com.ds.devsuaccount.infraestructure.exceptions.ApiException;
import com.ds.devsuaccount.infraestructure.exceptions.ErrorCode;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Slf4j
@Getter
public enum TransferStatusDetails {
    PENDING("pending"),
    APPROVED("approved"),
    REJECTED("rejected"),
    REFUNDED("Refunded"),
    ;

    private static final Map<String, TransferStatusDetails> mapValue;

    private final String name;

    TransferStatusDetails(String name) {
        this.name = name;
    }

    static {
        mapValue = new HashMap<>();
        for (TransferStatusDetails v : TransferStatusDetails.values()) {
            mapValue.put(v.getName(), v);
        }
    }

    public static TransferStatusDetails getName(String name) {
        return Optional.ofNullable(mapValue.get(name.toLowerCase())).orElseThrow(() -> {
                    log.error("Error invalid transfer status detail: {}", name);
                    return new ApiException(ErrorCode.INVALID_PAYMENT_TYPE);
                }
        );

    }
}