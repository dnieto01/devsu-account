package com.ds.devsuaccount.infraestructure.queue.imp;

import com.ds.devsuaccount.infraestructure.exceptions.ApiException;
import com.ds.devsuaccount.infraestructure.exceptions.ErrorCode;
import com.ds.devsuaccount.infraestructure.queue.IQueueService;
import com.ds.devsuaccount.infraestructure.queue.QueueClient;
import com.ds.devsuaccount.infraestructure.queue.QueueUtils;
import com.ds.devsuaccount.infraestructure.queue.dto.EventMessage;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;

@Component
@Slf4j
public class QueueService implements IQueueService {

    @Autowired
    QueueUtils queueUtils;

    @Autowired
    private ObjectMapper objectMapper;

    @Override
    public void publish(Object data, QueueClient client) {
        try {
            String eventType = "USER_EVENT";

            EventMessage<Object> event = new EventMessage<>(eventType, data);
            String jsonMessage = objectMapper.writeValueAsString(event);

            queueUtils.getProducer(client).send(
                    new ProducerRecord<>(client.getResourceName(), jsonMessage),
                    (metadata, exception) -> {
                        if (exception != null) {
                            log.error("Async failure publishing to topic [{}]. Payload: {}. Error: {}",
                                    client.getResourceName(), jsonMessage, exception.getMessage());
                        } else {
                            log.info("Message successfully published. Topic: {} | Partition: {} | Offset: {}",
                                    metadata.topic(), metadata.partition(), metadata.offset());
                        }
                    }
            );

        } catch (Exception e) {
            log.error("Technical error while publishing to queue. Topic: {}", client.getResourceName(), e);
            throw new ApiException(ErrorCode.ERROR_QUEUE_PUBLISH_MESSAGE);
        }
    }
}
