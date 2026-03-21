package com.ds.devsuaccount.application;

import com.ds.devsuaccount.domain.dto.ResponseDto;
import com.ds.devsuaccount.domain.dto.TransferValidateDto;
import com.ds.devsuaccount.domain.entity.Transfer;
import com.ds.devsuaccount.domain.enums.AccountType;
import com.ds.devsuaccount.domain.enums.TransferStatus;
import com.ds.devsuaccount.domain.enums.TransferStatusDetails;
import com.ds.devsuaccount.domain.enums.TransferType;
import com.ds.devsuaccount.domain.mapper.TransactionMapper;
import com.ds.devsuaccount.infraestructure.database.entity.TransactionDbEntity;
import com.ds.devsuaccount.infraestructure.database.repository.TransactionRepository;
import com.ds.devsuaccount.infraestructure.exceptions.ApiException;
import com.ds.devsuaccount.infraestructure.exceptions.ErrorCode;
import com.ds.devsuaccount.infraestructure.lock.ILockService;
import com.ds.devsuaccount.infraestructure.queue.IQueueService;
import com.ds.devsuaccount.infraestructure.queue.QueueClient;
import com.ds.devsuaccount.infraestructure.utils.DateUtils;
import com.ds.devsuaccount.infraestructure.valuestorage.IValueStorageService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@Slf4j
public class TransferService {

    @Autowired
    private AccountService accountService;

    @Autowired
    private IQueueService queueService;

    @Autowired
    private ILockService lockService;

    @Autowired
    private TransactionRepository transactionRepository;

    @Autowired
    private IValueStorageService valueStorageService;

    private final TransactionMapper mapper;

    public TransferService(TransactionMapper mapper) {
        this.mapper = mapper;
    }

    public ResponseDto postTransfer(Transfer transfer) {
        try {
            if (transfer.getAdditionalInfo() == null || transfer.getAdditionalInfo().isEmpty()) {
                Map<Object, Object> additionalInfo = new HashMap<>();
                additionalInfo.put("admin", false);
                transfer.setAdditionalInfo(additionalInfo);
            }

            if (!((TransferValidateDto) validateTransfer(transfer).getResponse()).getIsValid()) {
                log.error("Invalid transfer intent");
                transfer.setFailedReason("Error in validations");
                throw new ApiException(ErrorCode.ERROR_INVALID_TRANSFER);
            }

            if (!accountService.haveEnoughAmount(
                    transfer.getOrigin().getClient().getId(),
                    transfer.getOrigin().getAccount().getId(),
                    transfer.getAmount())) {
                log.error("Insufficient funds");
                transfer.setFailedReason("Insufficient funds");
                throw new ApiException(ErrorCode.CLIENT_NOT_HAVE_ENOUGH_AMOUNT);
            }
        } catch (ApiException e) {
            transfer.setStatus(TransferStatus.REJECTED.getName());
            transfer.setStatusDetail(TransferStatusDetails.REJECTED.getName());
            return ResponseDto.builder().response(publishMo(transfer)).code(e.getStatusCode()).build();
        }

        return ResponseDto.builder().response(publishMo(transfer)).code(201).build();
    }

    private Transfer publishMo(Transfer transfer) {
        if (transfer.getStatus() == null) {
            transfer.setStatus(TransferStatus.CREATED.getName());
        }
        if (transfer.getStatusDetail() == null) {
            transfer.setStatusDetail(TransferStatusDetails.PENDING.getName());
        }
        TransactionDbEntity db = null;
        try {
            db = transactionRepository.save(mapper.dtoToEntity(transfer));
        } catch (Exception e) {
            log.error("Error publishing in DB{}", e.getMessage(), e);
            throw new ApiException(ErrorCode.ERROR_PUBLISHING_DATABASE);
        }
        transfer.setId(String.format("MO-%s", db.getId()));
        transfer.setOriginId(db.getId().toString());
        queueService.publish(transfer, QueueClient.TRANSFER_INTENT_QUEUE);

        return transfer;
    }

    public ResponseDto putTransfer(Transfer transfer) {

        return ResponseDto.builder().response(transfer).code(200).build();
    }

    public ResponseDto process(Transfer transfer) {

        TransferType type = TransferType.getName(transfer.getType());
        switch (type) {
            case MI -> {
                processMi(transfer);
                return ResponseDto.builder().response(transfer).code(200).build();
            }
            case MO -> {
                processMo(transfer);
                return ResponseDto.builder().response(transfer).code(200).build();
            }
        }

        log.error("Payment Type not found");
        throw new ApiException(ErrorCode.INVALID_PAYMENT_TYPE);
    }

    public ResponseDto validateTransfer(Transfer transfer) {

        boolean isValid = false;

        try {

            TransferType type = TransferType.getName(transfer.getType());

            if (transfer.getAmount() <= 0) {
                log.error("Error invalid amount");
                throw new ApiException(ErrorCode.INVALID_AMOUNT);
            }

            if (!DateUtils.isTodayString(transfer.getPaymentDate())) {
                log.error("Error invalid payment date");
                throw new ApiException(ErrorCode.INVALID_PAYMENT_DATE);
            }

            AccountType.getName(transfer.getOrigin().getAccount().getType());
            validateAccount(transfer.getOrigin().getClient().getId(), transfer.getOrigin().getAccount().getId());

            AccountType.getName(transfer.getDestination().getAccount().getType());
            validateAccount(transfer.getDestination().getClient().getId(), transfer.getDestination().getAccount().getId());

            if (type.equals(TransferType.MI))
                if (transfer.getAdditionalInfo() == null || transfer.getAdditionalInfo().isEmpty() || !Boolean.TRUE.equals(transfer.getAdditionalInfo().get("admin"))) {
                    log.error("Error payment not authorized");
                    throw new ApiException(ErrorCode.ERROR_PAYMENT_CONTROLLER_NOT_AUTHORIZED);
                }
            isValid = true;
        } catch (Exception e) {
            log.error("Validation failed {}", e.getMessage(), e);
        }

        TransferValidateDto response = TransferValidateDto.builder()
                .transfer(transfer)
                .isValid(isValid)
                .build();

        return ResponseDto.builder().response(response).code(isValid ? 200 : 400).build();
    }

    private void validateAccount(String clientId, String account) {
        if (!accountService.isValidAccount(clientId, account)) {
            log.error("Error invalid destination account");
            throw new ApiException(ErrorCode.INVALID_CLIENT_ACCOUNT);
        }
    }

    public void processMo(Transfer transfer) {
        String key = transfer.getOriginId();
        boolean lock = false;
        try {
            lock = lockService.acquireLock(key);
            if (valueStorageService.getIdempotency(key).isEmpty()) {
                TransferValidateDto isValid = (TransferValidateDto) validateTransfer(transfer).getResponse();
                if (!isValid.getIsValid()) {
                    transfer.setStatus(TransferStatus.REJECTED.getName());
                    transfer.setStatusDetail(TransferStatusDetails.REJECTED.getName());
                    transfer.setFailedReason("Error validating");
                } else if (!accountService.debitAmount(transfer.getOrigin().getClient().getId(), transfer.getOrigin().getAccount().getId(), transfer.getAmount())) {
                    transfer.setStatus(TransferStatus.REJECTED.getName());
                    transfer.setStatusDetail(TransferStatusDetails.REJECTED.getName());
                    transfer.setFailedReason("Not is possible debit the amount");
                } else {
                    transfer.setStatus(TransferStatus.APPROVED.getName());
                    transfer.setStatusDetail(TransferStatusDetails.APPROVED.getName());
                }
                valueStorageService.saveTransfer(transfer);
                queueService.publish(transfer, QueueClient.NEWS);
                valueStorageService.saveIdempotency(key, transfer);
                if (TransferStatus.APPROVED.getName().equals(transfer.getStatus()))
                    publishMi(transfer);
            } else {
                log.info("Transfer has been processed before");
            }
        } finally {
            if (lock)
                lockService.releaseLock(key);
        }
    }

    public void processMi(Transfer transfer) {
        String key = transfer.getOriginId();
        boolean lock = false;
        try {
            lock = lockService.acquireLock(key);
            if (valueStorageService.getIdempotency(key).isEmpty()) {
                TransferValidateDto isValid = (TransferValidateDto) validateTransfer(transfer).getResponse();

                if (!isValid.getIsValid()) {
                    transfer.setStatus(TransferStatus.REJECTED.getName());
                    transfer.setStatusDetail(TransferStatusDetails.REJECTED.getName());
                    transfer.setFailedReason("Error validating");
                } else if (accountService.creditAmount(transfer.getDestination().getClient().getId(), transfer.getDestination().getAccount().getId(), transfer.getAmount())) {
                    transfer.setStatus(TransferStatus.APPROVED.getName());
                    transfer.setStatusDetail(TransferStatusDetails.APPROVED.getName());
                } else {
                    transfer.setStatus(TransferStatus.REJECTED.getName());
                    transfer.setStatusDetail(TransferStatusDetails.REJECTED.getName());
                    transfer.setFailedReason("Not is possible credit the amount");
                }

                valueStorageService.saveTransfer(transfer);
                queueService.publish(transfer, QueueClient.NEWS);
                valueStorageService.saveIdempotency(key, transfer);

            } else {
                log.info("Transfer has been processed before");
            }
        } finally {
            if (lock)
                lockService.releaseLock(key);
        }
    }

    public void publishMi(Transfer transfer) {
        transfer.setOriginId(transfer.getId());
        transfer.setType(TransferType.MI.getName());
        transfer.setStatus(TransferStatus.CREATED.getName());
        transfer.setStatusDetail(TransferStatusDetails.PENDING.getName());
        transfer.setFailedReason(null);
        transfer.getAdditionalInfo().put("admin", true);
        String tempId = transfer.getId();

        TransactionDbEntity db = null;
        try {
            transfer.setId(null);
            db = transactionRepository.save(mapper.dtoToEntity(transfer));
        } catch (Exception e) {
            log.warn(tempId);
            log.error("Error publishing in DB{}", e.getMessage(), e);
            throw new ApiException(ErrorCode.ERROR_PUBLISHING_DATABASE);
        }
        transfer.setId(String.format("MI-%s", db.getId()));
        queueService.publish(transfer, QueueClient.TRANSFER_INTENT_QUEUE);
    }


    public void processStatusUpdate(Transfer transfer) {
        String key = transfer.getOriginId();
        boolean lock = false;
        try {
            lock = lockService.acquireLock(key);
            updateStatusTransaction(transfer);
        } finally {
            if (lock)
                lockService.releaseLock(key);
        }
    }

    private void updateStatusTransaction(Transfer transfer) {
        TransferType type = TransferType.getName(transfer.getType());
        TransactionDbEntity transaction = null;

        switch (type) {
            case MI -> {
                transaction = transactionRepository.findById(UUID.fromString(transfer.getId().replace("MI-", ""))).get();
            }
            case MO -> {
                transaction = transactionRepository.findById(UUID.fromString(transfer.getOriginId())).get();
            }
        }
        transaction.setStatus(transfer.getStatus());
        transaction.setStatusDetail(transfer.getStatusDetail());
        transactionRepository.save(transaction);

    }


    public TransactionDbEntity getMovimientoById(String id) {
        return transactionRepository.getById(UUID.fromString(id));
    }

    public List<TransactionDbEntity> getMovimientos() {
        return transactionRepository.findAll();
    }

    public Optional<Transfer> getTransfer(String id) {
        return valueStorageService.findById(id);
    }

    public Optional<List<Transfer>> getTransfers() {
        return Optional.of(valueStorageService.findAll());
    }
}
