package com.ds.devsuaccount.application;

import com.ds.devsuaccount.domain.dto.AccountCreateRequest;
import com.ds.devsuaccount.domain.dto.AccountResponse;
import com.ds.devsuaccount.domain.dto.AccountUpdateRequest;
import com.ds.devsuaccount.infraestructure.database.entity.AccountDbEntity;
import com.ds.devsuaccount.infraestructure.database.repository.AccountRepository;
import com.ds.devsuaccount.infraestructure.exceptions.ApiException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AccountServiceTest {

    @Mock
    private AccountRepository accountRepository;

    @InjectMocks
    private AccountService accountService;

    @Test
    void createShouldPersistAndReturnResponse() {
        when(accountRepository.existsById("001")).thenReturn(false);
        when(accountRepository.save(any(AccountDbEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));

        AccountResponse response = accountService.create(AccountCreateRequest.builder()
                .accountNumber("001")
                .accountType("ahorros")
                .clientId("C1")
                .balance(new BigDecimal("100.00"))
                .build());

        assertEquals("001", response.getAccountNumber());
        assertEquals("ahorros", response.getAccountType());
        assertEquals(AccountService.STATUS_ACTIVE, response.getStatus());
        assertEquals(0, new BigDecimal("100.00").compareTo(response.getBalance()));
    }

    @Test
    void createShouldRejectDuplicateAccountNumber() {
        when(accountRepository.existsById("001")).thenReturn(true);

        assertThrows(ApiException.class, () -> accountService.create(AccountCreateRequest.builder()
                .accountNumber("001")
                .accountType("ahorros")
                .clientId("C1")
                .balance(BigDecimal.ZERO)
                .build()));
    }

    @Test
    void debitAndCreditShouldUpdateBalanceWithLock() {
        AccountDbEntity entity = AccountDbEntity.builder()
                .accountNumber("A1")
                .accountType("ahorros")
                .clientId("C1")
                .balance(new BigDecimal("50"))
                .status(AccountService.STATUS_ACTIVE)
                .build();
        when(accountRepository.findWithLockByAccountNumber("A1")).thenReturn(Optional.of(entity));
        when(accountRepository.save(any(AccountDbEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));

        assertTrue(accountService.debitAmount("C1", "A1", 20));
        assertEquals(0, new BigDecimal("30").compareTo(entity.getBalance()));

        assertTrue(accountService.creditAmount("C1", "A1", 10));
        assertEquals(0, new BigDecimal("40").compareTo(entity.getBalance()));
    }

    @Test
    void debitShouldReturnFalseWhenInsufficientFunds() {
        AccountDbEntity entity = AccountDbEntity.builder()
                .accountNumber("A1")
                .accountType("ahorros")
                .clientId("C1")
                .balance(new BigDecimal("5"))
                .status(AccountService.STATUS_ACTIVE)
                .build();
        when(accountRepository.findWithLockByAccountNumber("A1")).thenReturn(Optional.of(entity));

        assertFalse(accountService.debitAmount("C1", "A1", 20));
    }

    @Test
    void deleteShouldSoftCloseAccount() {
        AccountDbEntity entity = AccountDbEntity.builder()
                .accountNumber("A1")
                .accountType("ahorros")
                .clientId("C1")
                .balance(BigDecimal.TEN)
                .status(AccountService.STATUS_ACTIVE)
                .build();
        when(accountRepository.findById("A1")).thenReturn(Optional.of(entity));
        when(accountRepository.save(any(AccountDbEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));

        accountService.delete("A1");

        assertEquals(AccountService.STATUS_CLOSED, entity.getStatus());
    }

    @Test
    void updateShouldApplyPartialFields() {
        AccountDbEntity entity = AccountDbEntity.builder()
                .accountNumber("A1")
                .accountType("ahorros")
                .clientId("C1")
                .balance(BigDecimal.TEN)
                .status(AccountService.STATUS_ACTIVE)
                .build();
        when(accountRepository.findById("A1")).thenReturn(Optional.of(entity));
        when(accountRepository.save(any(AccountDbEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));

        accountService.update("A1", AccountUpdateRequest.builder()
                .status(AccountService.STATUS_INACTIVE)
                .build());

        assertEquals(AccountService.STATUS_INACTIVE, entity.getStatus());
    }

    @Test
    void findAllShouldMapEntities() {
        when(accountRepository.findAll()).thenReturn(List.of(
                AccountDbEntity.builder()
                        .accountNumber("1")
                        .accountType("corriente")
                        .clientId("C")
                        .balance(BigDecimal.ONE)
                        .status(AccountService.STATUS_ACTIVE)
                        .build()
        ));

        List<AccountResponse> all = accountService.findAll();
        assertEquals(1, all.size());
        assertEquals("1", all.get(0).getAccountNumber());
    }
}
