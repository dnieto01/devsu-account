package com.ds.devsuaccount.domain.entity;

import com.ds.devsuaccount.infraestructure.database.entity.TransactionDbEntity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@AllArgsConstructor
@Builder
@Data
@NoArgsConstructor
public class ClientReport {
    private String clientId;
    private String beginDate;
    private String endDate;
    private List<TransactionDbEntity> transactions;
}
