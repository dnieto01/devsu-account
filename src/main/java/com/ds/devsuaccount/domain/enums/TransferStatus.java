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
public enum TransferStatus {
    CREATED("created"),
    APPROVED("approved"),
    REJECTED("rejected"),
    REFUND("refund"),
    ;

    private static final Map<String, TransferStatus> mapValue;

    private String name;

    TransferStatus(String name) {
        this.name = name;
    }

    static {
        mapValue = new HashMap<>();
        for (TransferStatus v : TransferStatus.values()) {
            mapValue.put(v.getName(), v);
        }
    }

    public static TransferStatus getName(String name) {
        return Optional.ofNullable(mapValue.get(name.toLowerCase())).orElseThrow(() -> {
                    log.error("Error invalid transfer status: {}", name);
                    return new ApiException(ErrorCode.INVALID_PAYMENT_TYPE);
                }
        );

    }
}
