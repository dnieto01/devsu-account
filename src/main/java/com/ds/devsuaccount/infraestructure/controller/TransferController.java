package com.ds.devsuaccount.infraestructure.controller;

import com.ds.devsuaccount.application.TransferService;
import com.ds.devsuaccount.domain.dto.ResponseDto;
import com.ds.devsuaccount.domain.entity.Transfer;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("movimientos")
@Slf4j
public class TransferController {

    @Autowired
    private TransferService transferService;

    @GetMapping("")
    public ResponseEntity<?> get() {
        return new ResponseEntity<>(transferService.getMovimientos(), HttpStatus.OK);
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getById(@PathVariable String id) {
        return new ResponseEntity<>(transferService.getMovimientoById(id), HttpStatus.OK);
    }

    @GetMapping("/transfer")
    public ResponseEntity<?> getAllTransfer() {
        return new ResponseEntity<>(transferService.getTransfers(), HttpStatus.OK);
    }

    @GetMapping("/transfer/{id}")
    public ResponseEntity<?> getTransfer(@PathVariable String id) {
        return new ResponseEntity<>(transferService.getTransfer(id), HttpStatus.OK);
    }

    @PostMapping()
    public ResponseEntity<?> postTransfer(@Valid @RequestBody Transfer transfer) {
        ResponseDto response = transferService.postTransfer(transfer);
        return new ResponseEntity<>(response.getResponse(), HttpStatus.resolve(response.getCode()));
    }

    @PutMapping()
    public ResponseEntity<?> putTransfer(@Valid @RequestBody Transfer transfer) {
        ResponseDto response = transferService.putTransfer(transfer);
        return new ResponseEntity<>(response.getResponse(), HttpStatus.resolve(response.getCode()));
    }

    @PostMapping("validate")
    public ResponseEntity<?> validateTransfer(@Valid @RequestBody Transfer transfer) {
        ResponseDto response = transferService.validateTransfer(transfer);
        return new ResponseEntity<>(response.getResponse(), HttpStatus.resolve(response.getCode()));
    }

}
