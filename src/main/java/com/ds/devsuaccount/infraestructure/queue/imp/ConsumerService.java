package com.ds.devsuaccount.infraestructure.queue.imp;

import com.ds.devsuaccount.application.TransferService;
import com.ds.devsuaccount.domain.entity.Transfer;
import com.ds.devsuaccount.infraestructure.queue.dto.EventMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class ConsumerService {

    @Autowired
    private TransferService transferService;

    @KafkaListener(topics = "transaction-intent", groupId = "${spring.kafka.consumer.group-id}")
    public void listen(EventMessage<Transfer> event) {
        try {
            log.info("Processing transaction-intent event: {}", event);
            Thread.sleep(1000);
            transferService.process(event.getPayload());

        } catch (Exception e) {
            log.error("Critical error processing event {}. Message will be skipped.", event.getEventId(), e);
        }
    }

    @KafkaListener(topics = "news", groupId = "${spring.kafka.consumer.group-id}")
    public void listenNews(EventMessage<Transfer> event) {
        try {
            log.info("Processing news event: {}", event);
            Thread.sleep(1000);
            transferService.processStatusUpdate(event.getPayload());
        } catch (Exception e) {
            log.error("Critical error processing event {}. Message will be skipped.", event.getEventId(), e);
        }
    }

}