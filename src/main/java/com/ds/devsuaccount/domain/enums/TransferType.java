package com.ds.devsuaccount.domain.enums;

import com.ds.devsuaccount.infraestructure.exceptions.ApiException;
import com.ds.devsuaccount.infraestructure.exceptions.ErrorCode;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Getter
@Slf4j
public enum TransferType {
    MO("money_out"),
    MI("money_in"),
    ;

    private static final Map<String, TransferType> mapValue;

    private final String name;

    TransferType(String name) {
        this.name = name;
    }

    static {
        mapValue = new HashMap<>();
        for (TransferType v : TransferType.values()) {
            mapValue.put(v.getName(), v);
        }
    }

    public static TransferType getName(String name) {
        return Optional.ofNullable(mapValue.get(name.toLowerCase())).orElseThrow(() -> {
                    log.error("Error invalid payment type: {}", name);
                    return new ApiException(ErrorCode.INVALID_PAYMENT_TYPE);
                }
        );

    }

}

