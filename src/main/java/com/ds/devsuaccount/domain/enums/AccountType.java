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
public enum AccountType {
    AHORROS("ahorros"),
    CORRIENTE("corriente");

    private static final Map<String, AccountType> mapValue;

    private String name;

    AccountType(String name) {
        this.name = name;
    }

    static {
        mapValue = new HashMap<>();
        for (AccountType v : AccountType.values()) {
            mapValue.put(v.getName(), v);
        }
    }

    public static AccountType getName(String name) {
        return Optional.ofNullable(mapValue.get(name.toLowerCase())).orElseThrow(() -> {
                    log.error("Error invalid account type: {}", name);
            return new ApiException(ErrorCode.INVALID_PAYMENT_TYPE);
                }
        );
    }

}
