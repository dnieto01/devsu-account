package com.ds.devsuaccount.infraestructure.database.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
@Table(name = "transfer_intent")
public class TransactionDbEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;
    @Column(name = "time")
    private LocalDateTime timeStamp;
    @Column(name = "payment_date")
    private LocalDate paymentDate;
    @Column(name = "payment_type")
    private String paymentType;
    @Column(name = "amount")
    private double amount;
    @Column(name = "origin_account")
    private String originAccount;
    @Column(name = "origin_type")
    private String originAccountType;
    @Column(name = "origin_client_id")
    private String originClientId;
    @Column(name = "origin_client_name")
    private String originClientName;
    @Column(name = "destination_account")
    private String destinationAccount;
    @Column(name = "destination_type")
    private String destinationAccountType;
    @Column(name = "destination_client_id")
    private String destinationClientId;
    @Column(name = "destination_client_name")
    private String destinationClientName;
    @Column(name = "status")
    private String status;
    @Column(name = "status_detail")
    private String statusDetail;
    @Column(name = "failed_reason")
    private String failedReason;

}
