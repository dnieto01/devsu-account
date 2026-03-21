package com.ds.devsuaccount.infraestructure.controller;

import com.ds.devsuaccount.application.AccountService;
import com.ds.devsuaccount.application.ReportService;
import com.ds.devsuaccount.application.TransferService;
import com.ds.devsuaccount.domain.dto.ResponseDto;
import com.ds.devsuaccount.domain.entity.ClientReport;
import com.ds.devsuaccount.domain.entity.Transfer;
import com.ds.devsuaccount.infraestructure.database.entity.TransactionDbEntity;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class InfraestructureControllersTest {

    @Test
    void reportControllerShouldReturnReportPayload() {
        ReportService reportService = mock(ReportService.class);
        ReportController controller = new ReportController();
        ReflectionTestUtils.setField(controller, "reportService", reportService);

        ClientReport report = ClientReport.builder().clientId("c1").build();
        when(reportService.getClientAccountsReport(any(ClientReport.class))).thenReturn(report);

        ResponseEntity<?> response = controller.get("2025-01-01", "2025-01-10", "c1");

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(report, response.getBody());
        verify(reportService).getClientAccountsReport(any(ClientReport.class));
    }

    @Test
    void transferControllerShouldDelegateAllEndpoints() {
        TransferService transferService = mock(TransferService.class);
        TransferController controller = new TransferController();
        ReflectionTestUtils.setField(controller, "transferService", transferService);

        when(transferService.getMovimientos()).thenReturn(List.of(TransactionDbEntity.builder().id(UUID.randomUUID()).build()));
        when(transferService.getMovimientoById("id1")).thenReturn(TransactionDbEntity.builder().id(UUID.randomUUID()).build());
        when(transferService.getTransfers()).thenReturn(Optional.of(List.of(Transfer.builder().id("t1").build())));
        when(transferService.getTransfer("id2")).thenReturn(Optional.of(Transfer.builder().id("t2").build()));
        when(transferService.postTransfer(any(Transfer.class)))
                .thenReturn(ResponseDto.builder().response("posted").code(201).build());
        when(transferService.putTransfer(any(Transfer.class)))
                .thenReturn(ResponseDto.builder().response("updated").code(200).build());
        when(transferService.validateTransfer(any(Transfer.class)))
                .thenReturn(ResponseDto.builder().response("valid").code(202).build());

        assertEquals(HttpStatus.OK, controller.get().getStatusCode());
        assertEquals(HttpStatus.OK, controller.getById("id1").getStatusCode());
        assertEquals(HttpStatus.OK, controller.getAllTransfer().getStatusCode());
        assertEquals(HttpStatus.OK, controller.getTransfer("id2").getStatusCode());
        assertEquals(HttpStatus.CREATED, controller.postTransfer(new Transfer()).getStatusCode());
        assertEquals(HttpStatus.OK, controller.putTransfer(new Transfer()).getStatusCode());
        assertEquals(HttpStatus.ACCEPTED, controller.validateTransfer(new Transfer()).getStatusCode());
    }

    @Test
    void pingControllerShouldReturnPong() {
        PingController controller = new PingController();

        ResponseEntity<?> response = controller.get();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("pong", response.getBody());
    }

    @Test
    void accountControllerShouldInstantiate() {
        AccountController controller = new AccountController(mock(AccountService.class));
        assertNotNull(controller);
    }
}
