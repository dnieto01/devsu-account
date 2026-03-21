package com.ds.devsuaccount.domain.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AccountCreateRequest {

    @NotBlank
    private String accountNumber;

    @NotBlank
    private String accountType;

    @NotBlank
    private String clientId;

    @NotNull
    @DecimalMin(value = "0.0", inclusive = true)
    private BigDecimal balance;

    private String status;
}
