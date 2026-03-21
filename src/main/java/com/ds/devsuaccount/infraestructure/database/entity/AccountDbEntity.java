package com.ds.devsuaccount.infraestructure.database.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * Cuenta bancaria persistida. La clave natural y única es el número de cuenta.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
@Table(name = "client_account")
public class AccountDbEntity {

    @Id
    @Column(name = "account_number", nullable = false, length = 64, updatable = false)
    private String accountNumber;

    @Column(name = "account_type", nullable = false, length = 32)
    private String accountType;

    @Column(name = "balance", nullable = false, precision = 19, scale = 4)
    private BigDecimal balance;

    @Column(name = "status", nullable = false, length = 32)
    private String status;

    @Column(name = "client_id", nullable = false, length = 64)
    private String clientId;
}
