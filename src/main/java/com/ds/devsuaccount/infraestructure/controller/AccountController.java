package com.ds.devsuaccount.infraestructure.controller;

import com.ds.devsuaccount.application.AccountService;
import com.ds.devsuaccount.domain.dto.AccountCreateRequest;
import com.ds.devsuaccount.domain.dto.AccountResponse;
import com.ds.devsuaccount.domain.dto.AccountUpdateRequest;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("cuentas")
@Slf4j
public class AccountController {

    private final AccountService accountService;

    public AccountController(AccountService accountService) {
        this.accountService = accountService;
    }

    @PostMapping
    public ResponseEntity<AccountResponse> create(@Valid @RequestBody AccountCreateRequest request) {
        AccountResponse created = accountService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @GetMapping
    public ResponseEntity<List<AccountResponse>> list() {
        return ResponseEntity.ok(accountService.findAll());
    }

    @GetMapping("/{accountNumber}")
    public ResponseEntity<AccountResponse> get(@PathVariable String accountNumber) {
        return ResponseEntity.ok(accountService.getByAccountNumber(accountNumber));
    }

    @PutMapping("/{accountNumber}")
    public ResponseEntity<AccountResponse> update(
            @PathVariable String accountNumber,
            @RequestBody AccountUpdateRequest request) {
        return ResponseEntity.ok(accountService.update(accountNumber, request));
    }

    @DeleteMapping("/{accountNumber}")
    public ResponseEntity<Void> delete(@PathVariable String accountNumber) {
        accountService.delete(accountNumber);
        return ResponseEntity.noContent().build();
    }
}
