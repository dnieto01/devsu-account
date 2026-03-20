package com.ds.devsuaccount.infraestructure.controller;

import com.ds.devsuaccount.application.ReportService;
import com.ds.devsuaccount.domain.entity.ClientReport;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("reportes")
@Slf4j
public class ReportController {

    @Autowired
    private ReportService reportService;

    @GetMapping("")
    public ResponseEntity<?> get(
            @RequestParam("begin_date") String beginDate,
            @RequestParam("end_date") String endDate,
            @RequestParam("client") String clientId) {

        ClientReport response = reportService.getClientAccountsReport(ClientReport.builder().beginDate(beginDate).endDate(endDate).clientId(clientId).build());

        return ResponseEntity.ok(response);
    }

}
