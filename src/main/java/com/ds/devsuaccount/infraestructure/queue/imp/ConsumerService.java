package com.ds.devsuaccount.infraestructure.queue.imp;

import com.ds.devsuuser.infraestructure.queue.dto.EventMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class ConsumerService {

    @KafkaListener(topics = "transaction-intent", groupId = "${spring.kafka.consumer.group-id}")
    public void listen(EventMessage<Object> event) {
        try {
            log.info("Processing event: {}", event);

            log.info("Event {} processed and offset committed.", event.getEventId());
        } catch (Exception e) {
            log.error("Critical error processing event {}. Message will be skipped.", event.getEventId(), e);
        }
    }

}