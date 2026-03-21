package com.ds.devsuaccount.infraestructure.misc;

import com.ds.devsuaccount.infraestructure.database.entity.TransactionDbEntity;
import com.ds.devsuaccount.infraestructure.exceptions.ApiErrorResponse;
import com.ds.devsuaccount.infraestructure.exceptions.ApiException;
import com.ds.devsuaccount.infraestructure.exceptions.ErrorCode;
import com.ds.devsuaccount.infraestructure.queue.QueueClient;
import com.ds.devsuaccount.infraestructure.queue.dto.EventMessage;
import com.ds.devsuaccount.infraestructure.utils.DateUtils;
import com.ds.devsuaccount.infraestructure.utils.ScopeUtils;
import com.ds.devsuaccount.infraestructure.valuestorage.entity.Idempotency;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class InfraestructureMiscTest {

    @Test
    void dateUtilsShouldParseAndDetectToday() {
        LocalDate today = LocalDate.now();
        assertEquals(today, DateUtils.parseShortDate(today.toString()));
        assertTrue(DateUtils.isTodayString(today.toString()));
        assertFalse(DateUtils.isTodayString(today.minusDays(1).toString()));
    }

    @Test
    void scopeUtilsShouldCalculateAndValidateScope() {
        ScopeUtils.calculateScopeSuffix();
        assertNotNull(System.getProperty(ScopeUtils.SCOPE_SUFFIX));
        assertNotNull(ScopeUtils.getScopeValue());
        assertTrue(ScopeUtils.isLocalScope() || ScopeUtils.isTestScope());
    }

    @Test
    void exceptionsAndErrorResponseShouldMapValues() {
        ApiException ex = new ApiException(ErrorCode.BAD_REQUEST);
        ApiErrorResponse response = new ApiErrorResponse(ex);

        assertEquals(ErrorCode.BAD_REQUEST.getCode(), ex.getCode());
        assertEquals(ex.getDescription(), response.getDescription());
        assertEquals(ex.getStatusCode(), response.getStatusCode());
    }

    @Test
    void errorCodeAndQueueClientEnumsShouldExposeValues() {
        assertEquals("transaction-intent", QueueClient.TRANSFER_INTENT_QUEUE.getResourceName());
        assertEquals("news", QueueClient.NEWS.getResourceName());
        assertEquals("404", ErrorCode.ERROR_PAYMENT_CONTROLLER_NOT_AUTHORIZED.getCode());
    }

    @Test
    void eventMessageShouldSetDefaultMetadata() {
        EventMessage<String> message = new EventMessage<>("USER_EVENT", "payload");
        assertNotNull(message.getEventId());
        assertEquals("USER_EVENT", message.getEventType());
        assertEquals("1.0", message.getVersion());
        assertEquals("payload", message.getPayload());
    }

    @Test
    void idempotencyAndTransactionEntityShouldHoldData() {
        Idempotency idempotency = Idempotency.builder().id("id1").value("v1").build();
        assertEquals("id1", idempotency.getId());

        UUID id = UUID.randomUUID();
        TransactionDbEntity entity = TransactionDbEntity.builder()
                .id(id)
                .timeStamp(LocalDateTime.now())
                .paymentDate(LocalDate.now())
                .paymentType("transfer")
                .amount(15.2)
                .originAccount("001")
                .status("ok")
                .build();

        assertEquals(id, entity.getId());
        assertEquals("transfer", entity.getPaymentType());
    }
}
