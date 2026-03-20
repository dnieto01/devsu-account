package com.ds.devsuaccount.application;

import com.ds.devsuaccount.domain.entity.ClientReport;
import com.ds.devsuaccount.domain.enums.TransferType;
import com.ds.devsuaccount.infraestructure.database.entity.TransactionDbEntity;
import com.ds.devsuaccount.infraestructure.database.repository.TransactionRepository;
import com.ds.devsuaccount.infraestructure.utils.DateUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
public class ReportService {

    @Autowired
    private TransactionRepository transactionRepository;

    public ClientReport getClientAccountsReport(ClientReport report) {
        List<TransactionDbEntity> transactions = new ArrayList<>();
        transactions.addAll(transactionRepository.findByOriginClientIdEqualsAndPaymentDateBetweenAndPaymentType(report.getClientId(),
                DateUtils.parseShortDate(report.getBeginDate()), DateUtils.parseShortDate(report.getEndDate()), TransferType.MO.getName()));
        transactions.addAll(transactionRepository.findByDestinationClientIdEqualsAndPaymentDateBetweenAndPaymentType(report.getClientId(),
                DateUtils.parseShortDate(report.getBeginDate()), DateUtils.parseShortDate(report.getEndDate()), TransferType.MI.getName()));
        report.setTransactions(transactions);
        return report;
    }
}
