package com.ds.devsuaccount.application;

import com.ds.devsuaccount.domain.dto.ResponseDto;
import com.ds.devsuaccount.domain.entity.Account;
import com.ds.devsuaccount.domain.entity.Client;
import com.ds.devsuaccount.domain.entity.Destination;
import com.ds.devsuaccount.domain.entity.Origin;
import com.ds.devsuaccount.domain.entity.Transfer;
import com.ds.devsuaccount.domain.enums.TransferStatus;
import com.ds.devsuaccount.domain.enums.TransferStatusDetails;
import com.ds.devsuaccount.domain.mapper.TransactionMapper;
import com.ds.devsuaccount.infraestructure.database.entity.TransactionDbEntity;
import com.ds.devsuaccount.infraestructure.database.repository.TransactionRepository;
import com.ds.devsuaccount.infraestructure.lock.ILockService;
import com.ds.devsuaccount.infraestructure.queue.IQueueService;
import com.ds.devsuaccount.infraestructure.valuestorage.IValueStorageService;
import com.ds.devsuaccount.infraestructure.valuestorage.entity.Idempotency;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyDouble;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class TransferServiceBusinessLogicTest {

    @Test
    void postTransferShouldReturnDomainStatusCodeAndKeepRejectedStatus() {
        TransactionMapper mapper = mock(TransactionMapper.class);
        AccountService accountService = mock(AccountService.class);
        IQueueService queueService = mock(IQueueService.class);
        ILockService lockService = mock(ILockService.class);
        TransactionRepository transactionRepository = mock(TransactionRepository.class);
        IValueStorageService valueStorageService = mock(IValueStorageService.class);

        TransferService service = new TransferService(mapper);
        ReflectionTestUtils.setField(service, "accountService", accountService);
        ReflectionTestUtils.setField(service, "queueService", queueService);
        ReflectionTestUtils.setField(service, "lockService", lockService);
        ReflectionTestUtils.setField(service, "transactionRepository", transactionRepository);
        ReflectionTestUtils.setField(service, "valueStorageService", valueStorageService);

        Transfer transfer = buildTransfer("money_out", LocalDate.now().toString());
        TransactionDbEntity persisted = TransactionDbEntity.builder().id(UUID.randomUUID()).build();
        when(mapper.dtoToEntity(any(Transfer.class))).thenReturn(new TransactionDbEntity());
        when(transactionRepository.save(any(TransactionDbEntity.class))).thenReturn(persisted);
        when(accountService.isValidAccount(any(), any())).thenReturn(true);
        when(accountService.haveEnoughAmount(any(), any(), anyDouble())).thenReturn(false);

        ResponseDto<?> response = service.postTransfer(transfer);

        assertEquals(200, response.getCode());
        Transfer published = (Transfer) response.getResponse();
        assertEquals(TransferStatus.REJECTED.getName(), published.getStatus());
        assertEquals(TransferStatusDetails.REJECTED.getName(), published.getStatusDetail());
    }

    @Test
    void processMiShouldUseOriginIdForIdempotencyAndRejectInvalidTransfer() {
        TransactionMapper mapper = mock(TransactionMapper.class);
        AccountService accountService = mock(AccountService.class);
        IQueueService queueService = mock(IQueueService.class);
        ILockService lockService = mock(ILockService.class);
        TransactionRepository transactionRepository = mock(TransactionRepository.class);
        IValueStorageService valueStorageService = mock(IValueStorageService.class);

        TransferService service = new TransferService(mapper);
        ReflectionTestUtils.setField(service, "accountService", accountService);
        ReflectionTestUtils.setField(service, "queueService", queueService);
        ReflectionTestUtils.setField(service, "lockService", lockService);
        ReflectionTestUtils.setField(service, "transactionRepository", transactionRepository);
        ReflectionTestUtils.setField(service, "valueStorageService", valueStorageService);

        Transfer transfer = buildTransfer("money_in", "1999-01-01");
        transfer.setOriginId("ORIGIN-1");
        when(lockService.acquireLock("ORIGIN-1")).thenReturn(true);
        when(valueStorageService.getIdempotency("ORIGIN-1")).thenReturn(Optional.empty());
        when(accountService.isValidAccount(any(), any())).thenReturn(true);

        service.processMi(transfer);

        assertEquals(TransferStatus.REJECTED.getName(), transfer.getStatus());
        assertEquals(TransferStatusDetails.REJECTED.getName(), transfer.getStatusDetail());
        verify(accountService, never()).creditAmount(any(), any(), any(Double.class));
        verify(valueStorageService).saveIdempotency(eq("ORIGIN-1"), eq(transfer));
        verify(lockService).releaseLock("ORIGIN-1");
    }

    @Test
    void processMoShouldCheckIdempotencyByOriginId() {
        TransactionMapper mapper = mock(TransactionMapper.class);
        AccountService accountService = mock(AccountService.class);
        IQueueService queueService = mock(IQueueService.class);
        ILockService lockService = mock(ILockService.class);
        TransactionRepository transactionRepository = mock(TransactionRepository.class);
        IValueStorageService valueStorageService = mock(IValueStorageService.class);

        TransferService service = new TransferService(mapper);
        ReflectionTestUtils.setField(service, "accountService", accountService);
        ReflectionTestUtils.setField(service, "queueService", queueService);
        ReflectionTestUtils.setField(service, "lockService", lockService);
        ReflectionTestUtils.setField(service, "transactionRepository", transactionRepository);
        ReflectionTestUtils.setField(service, "valueStorageService", valueStorageService);

        Transfer transfer = buildTransfer("money_out", LocalDate.now().toString());
        transfer.setOriginId("ORIGIN-2");
        transfer.setId("MO-123");
        when(lockService.acquireLock("ORIGIN-2")).thenReturn(true);
        when(valueStorageService.getIdempotency("ORIGIN-2"))
                .thenReturn(Optional.of(Idempotency.builder().id("ORIGIN-2").build()));

        service.processMo(transfer);

        verify(valueStorageService).getIdempotency("ORIGIN-2");
        verify(lockService).releaseLock("ORIGIN-2");
    }

    private Transfer buildTransfer(String type, String paymentDate) {
        Account originAccount = Account.builder().id("A1").type("ahorros").build();
        Account destinationAccount = Account.builder().id("A2").type("corriente").build();
        Client originClient = Client.builder().id("C1").name("Alice").build();
        Client destinationClient = Client.builder().id("C2").name("Bob").build();
        Origin origin = Origin.builder().account(originAccount).client(originClient).build();
        Destination destination = Destination.builder().account(destinationAccount).client(destinationClient).build();

        HashMap<Object, Object> additional = new HashMap<>();
        additional.put("admin", true);

        return Transfer.builder()
                .type(type)
                .paymentDate(paymentDate)
                .amount(10.0)
                .origin(origin)
                .destination(destination)
                .additionalInfo(additional)
                .build();
    }
}
