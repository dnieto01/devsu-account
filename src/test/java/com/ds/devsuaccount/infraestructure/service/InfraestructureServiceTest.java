package com.ds.devsuaccount.infraestructure.service;

import com.ds.devsuaccount.application.TransferService;
import com.ds.devsuaccount.domain.entity.Transfer;
import com.ds.devsuaccount.infraestructure.cache.CacheService;
import com.ds.devsuaccount.infraestructure.exceptions.ApiException;
import com.ds.devsuaccount.infraestructure.lock.LockService;
import com.ds.devsuaccount.infraestructure.lock.LockServiceLocal;
import com.ds.devsuaccount.infraestructure.queue.QueueClient;
import com.ds.devsuaccount.infraestructure.queue.QueueUtils;
import com.ds.devsuaccount.infraestructure.queue.imp.ConsumerService;
import com.ds.devsuaccount.infraestructure.queue.imp.QueueService;
import com.ds.devsuaccount.infraestructure.queue.dto.EventMessage;
import com.ds.devsuaccount.infraestructure.valuestorage.ValueStorageService;
import com.ds.devsuaccount.infraestructure.valuestorage.entity.Idempotency;
import com.ds.devsuaccount.infraestructure.valuestorage.repository.IdempotencyRepository;
import com.ds.devsuaccount.infraestructure.valuestorage.repository.TransferVSRepository;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.junit.jupiter.api.Test;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.test.util.ReflectionTestUtils;
import redis.clients.jedis.Jedis;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Future;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class InfraestructureServiceTest {

    @Test
    void valueStorageServiceShouldDelegateAndHandleIdempotency() {
        TransferVSRepository transferRepository = mock(TransferVSRepository.class);
        IdempotencyRepository idempotencyRepository = mock(IdempotencyRepository.class);
        CacheService cacheService = mock(CacheService.class);

        ValueStorageService service = new ValueStorageService();
        ReflectionTestUtils.setField(service, "repositoryValueStorage", transferRepository);
        ReflectionTestUtils.setField(service, "idempotencyRepository", idempotencyRepository);
        ReflectionTestUtils.setField(service, "cacheService", cacheService);

        Transfer transfer = Transfer.builder().id("id1").originId("origin").build();
        when(transferRepository.findById("id1")).thenReturn(Optional.of(transfer));
        when(transferRepository.findByOriginId("origin")).thenReturn(transfer);
        when(transferRepository.save(transfer)).thenReturn(transfer);
        when(cacheService.getFromIdempotencyCache("key")).thenReturn(Idempotency.builder().id("key").build());
        when(idempotencyRepository.findById("key")).thenReturn(Optional.of(Idempotency.builder().id("key").build()));

        assertTrue(service.findById("id1").isPresent());
        assertNotNull(service.findByOriginId("origin"));
        service.findAll();
        service.saveTransfer(transfer);
        service.saveIdempotency("key", "value");
        assertTrue(service.getIdempotency("key").isPresent());

        when(cacheService.getFromIdempotencyCache("missing")).thenReturn(null);
        assertTrue(service.getIdempotency("missing").isEmpty());
    }

    @Test
    void cacheServiceShouldReadWriteFromCacheManager() {
        CacheManager manager = mock(CacheManager.class);
        Cache cache = mock(Cache.class);
        CacheService service = new CacheService();
        ReflectionTestUtils.setField(service, "cacheManager", manager);
        when(manager.getCache("idempotencyCache")).thenReturn(cache);

        Idempotency value = Idempotency.builder().id("k").build();
        service.saveIdempotencyCache("k", value);
        when(cache.get("k", Idempotency.class)).thenReturn(value);
        assertEquals("k", service.getFromIdempotencyCache("k").getId());
    }

    @Test
    void queueServiceShouldPublishAndHandleSerializationError() throws Exception {
        QueueUtils queueUtils = mock(QueueUtils.class);
        tools.jackson.databind.ObjectMapper mapper = mock(tools.jackson.databind.ObjectMapper.class);
        @SuppressWarnings("unchecked")
        Producer<String, String> producer = mock(Producer.class);

        QueueService service = new QueueService();
        ReflectionTestUtils.setField(service, "queueUtils", queueUtils);
        ReflectionTestUtils.setField(service, "objectMapper", mapper);

        when(mapper.writeValueAsString(any())).thenReturn("{\"ok\":true}");
        when(queueUtils.getProducer(QueueClient.NEWS)).thenReturn(producer);
        when(producer.send(any(), any())).thenReturn(mock(Future.class));

        service.publish(Map.of("x", "y"), QueueClient.NEWS);
        verify(producer).send(any(), any());

        doThrow(new RuntimeException("error")).when(mapper).writeValueAsString(any());
        assertThrows(ApiException.class, () -> service.publish(Map.of("x", "y"), QueueClient.NEWS));
    }

    @Test
    void consumerServiceShouldProcessEventsAndSwallowProcessingErrors() {
        TransferService transferService = mock(TransferService.class);
        ConsumerService service = new ConsumerService();
        ReflectionTestUtils.setField(service, "transferService", transferService);

        EventMessage<Transfer> event = new EventMessage<>("USER_EVENT", Transfer.builder().id("1").build());
        service.listen(event);
        service.listenNews(event);
        verify(transferService).process(any(Transfer.class));
        verify(transferService).processStatusUpdate(any(Transfer.class));

        doThrow(new RuntimeException("fail")).when(transferService).process(any(Transfer.class));
        doThrow(new RuntimeException("fail")).when(transferService).processStatusUpdate(any(Transfer.class));
        service.listen(event);
        service.listenNews(event);
    }

    @Test
    void queueUtilsShouldReturnExistingProducer() {
        QueueUtils utils = new QueueUtils();
        @SuppressWarnings("unchecked")
        Producer<String, String> producer = mock(Producer.class);
        Map<QueueClient, Producer<String, String>> map = new ConcurrentHashMap<>();
        map.put(QueueClient.TRANSFER_INTENT_QUEUE, producer);
        ReflectionTestUtils.setField(utils, "queueClients", map);

        Producer<String, String> result = utils.getProducer(QueueClient.TRANSFER_INTENT_QUEUE);
        assertEquals(producer, result);
    }

    @Test
    void lockServiceLocalShouldAcquireReleaseAndFailWhenLocked() {
        LockServiceLocal service = new LockServiceLocal();
        assertTrue(service.acquireLock("k1"));
        assertThrows(ApiException.class, () -> service.isLocked("k1"));
        service.releaseLock("k1");
        service.isLocked("k1");
    }

    @Test
    void lockServiceShouldAcquireReleaseAndThrowWhenAlreadyLocked() {
        Jedis jedis = mock(Jedis.class);
        LockService service = new LockService(jedis, 3000L);

        when(jedis.exists("k2")).thenReturn(false);
        when(jedis.set(eq("k2"), any(), any())).thenReturn("OK");
        assertTrue(service.acquireLock("k2"));

        when(jedis.eval(any(), eq(1), eq("k2"), any())).thenReturn(1L);
        service.releaseLock("k2");

        when(jedis.exists("k3")).thenReturn(true);
        assertThrows(ApiException.class, () -> service.isLocked("k3"));
    }
}
