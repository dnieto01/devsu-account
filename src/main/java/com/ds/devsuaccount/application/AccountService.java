package com.ds.devsuaccount.application;

import com.ds.devsuaccount.domain.dto.AccountCreateRequest;
import com.ds.devsuaccount.domain.dto.AccountResponse;
import com.ds.devsuaccount.domain.dto.AccountUpdateRequest;
import com.ds.devsuaccount.domain.enums.AccountType;
import com.ds.devsuaccount.infraestructure.database.entity.AccountDbEntity;
import com.ds.devsuaccount.infraestructure.database.repository.AccountRepository;
import com.ds.devsuaccount.infraestructure.exceptions.ApiException;
import com.ds.devsuaccount.infraestructure.exceptions.ErrorCode;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
public class AccountService {

    public static final String STATUS_ACTIVE = "active";
    public static final String STATUS_INACTIVE = "inactive";
    public static final String STATUS_CLOSED = "closed";

    private final AccountRepository accountRepository;

    public AccountService(AccountRepository accountRepository) {
        this.accountRepository = accountRepository;
    }

    @Transactional
    public AccountResponse create(AccountCreateRequest request) {
        if (accountRepository.existsById(request.getAccountNumber())) {
            throw new ApiException(ErrorCode.DUPLICATE_ACCOUNT_NUMBER);
        }
        AccountType.getName(request.getAccountType());
        String status = request.getStatus() != null && !request.getStatus().isBlank()
                ? request.getStatus().trim().toLowerCase()
                : STATUS_ACTIVE;

        AccountDbEntity entity = AccountDbEntity.builder()
                .accountNumber(request.getAccountNumber().trim())
                .accountType(request.getAccountType().trim().toLowerCase())
                .clientId(request.getClientId().trim())
                .balance(request.getBalance())
                .status(status)
                .build();

        return toResponse(accountRepository.save(entity));
    }

    @Transactional(readOnly = true)
    public List<AccountResponse> findAll() {
        return accountRepository.findAll().stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public AccountResponse getByAccountNumber(String accountNumber) {
        return toResponse(requireAccount(accountNumber));
    }

    @Transactional
    public AccountResponse update(String accountNumber, AccountUpdateRequest request) {
        AccountDbEntity entity = requireAccount(accountNumber);
        if (request.getAccountType() != null && !request.getAccountType().isBlank()) {
            AccountType.getName(request.getAccountType());
            entity.setAccountType(request.getAccountType().trim().toLowerCase());
        }
        if (request.getBalance() != null) {
            entity.setBalance(request.getBalance());
        }
        if (request.getStatus() != null && !request.getStatus().isBlank()) {
            entity.setStatus(request.getStatus().trim().toLowerCase());
        }
        return toResponse(accountRepository.save(entity));
    }

    @Transactional
    public void delete(String accountNumber) {
        AccountDbEntity entity = requireAccount(accountNumber);
        entity.setStatus(STATUS_CLOSED);
        accountRepository.save(entity);
    }

    @Transactional(readOnly = true)
    public boolean isValidAccount(String clientId, String accountNumber) {
        return accountRepository.findById(accountNumber)
                .filter(a -> clientId != null && clientId.equals(a.getClientId()))
                .filter(a -> STATUS_ACTIVE.equalsIgnoreCase(a.getStatus()))
                .isPresent();
    }

    @Transactional(readOnly = true)
    public boolean haveEnoughAmount(String clientId, String accountNumber, double amount) {
        return accountRepository.findById(accountNumber)
                .filter(a -> clientId != null && clientId.equals(a.getClientId()))
                .filter(a -> STATUS_ACTIVE.equalsIgnoreCase(a.getStatus()))
                .map(a -> a.getBalance().compareTo(BigDecimal.valueOf(amount)) >= 0)
                .orElse(false);
    }

    @Transactional
    public boolean debitAmount(String clientId, String accountNumber, double amount) {
        AccountDbEntity account = accountRepository.findWithLockByAccountNumber(accountNumber)
                .orElseThrow(() -> new ApiException(ErrorCode.ACCOUNT_NOT_FOUND));
        ensureActiveOwned(clientId, account);
        BigDecimal amt = BigDecimal.valueOf(amount);
        if (account.getBalance().compareTo(amt) < 0) {
            return false;
        }
        account.setBalance(account.getBalance().subtract(amt));
        accountRepository.save(account);
        return true;
    }

    @Transactional
    public boolean creditAmount(String clientId, String accountNumber, double amount) {
        AccountDbEntity account = accountRepository.findWithLockByAccountNumber(accountNumber)
                .orElseThrow(() -> new ApiException(ErrorCode.ACCOUNT_NOT_FOUND));
        ensureActiveOwned(clientId, account);
        BigDecimal amt = BigDecimal.valueOf(amount);
        account.setBalance(account.getBalance().add(amt));
        accountRepository.save(account);
        return true;
    }

    private void ensureActiveOwned(String clientId, AccountDbEntity account) {
        if (clientId == null || !clientId.equals(account.getClientId())) {
            throw new ApiException(ErrorCode.INVALID_CLIENT_ACCOUNT);
        }
        if (!STATUS_ACTIVE.equalsIgnoreCase(account.getStatus())) {
            throw new ApiException(ErrorCode.ACCOUNT_INACTIVE);
        }
    }

    private AccountDbEntity requireAccount(String accountNumber) {
        return accountRepository.findById(accountNumber)
                .orElseThrow(() -> new ApiException(ErrorCode.ACCOUNT_NOT_FOUND));
    }

    private AccountResponse toResponse(AccountDbEntity e) {
        return AccountResponse.builder()
                .accountNumber(e.getAccountNumber())
                .accountType(e.getAccountType())
                .balance(e.getBalance())
                .status(e.getStatus())
                .clientId(e.getClientId())
                .build();
    }
}
