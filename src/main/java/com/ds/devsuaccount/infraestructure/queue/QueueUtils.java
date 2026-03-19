package com.ds.devsuaccount.infraestructure.queue;

import com.ds.devsuuser.infraestructure.exceptions.ApiException;
import com.ds.devsuuser.infraestructure.exceptions.ErrorCode;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Objects;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@Slf4j
@Component
public class QueueUtils {
    @Value("${spring.kafka.bootstrap-servers}")
    private String bootstrapServers;

    private final ConcurrentMap<QueueClient, Producer<String, String>> queueClients = new ConcurrentHashMap<>();

    public Producer<String, String> getProducer(QueueClient client) {

        Producer<String, String> producer = queueClients.get(client);

        if (Objects.isNull(producer)) {
            return registerProducer(client);
        }

        return producer;
    }

    public Producer<String, String> registerProducer(QueueClient client) {
        return queueClients.computeIfAbsent(client, k -> {
            try {
                Properties properties = new Properties();
                properties.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);

                properties.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
                properties.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());

                properties.put(ProducerConfig.ACKS_CONFIG, "all");
                properties.put(ProducerConfig.RETRIES_CONFIG, 3);
                properties.put(ProducerConfig.BATCH_SIZE_CONFIG, 16384);
                properties.put(ProducerConfig.LINGER_MS_CONFIG, 1);
                return new KafkaProducer<>(properties);
            } catch (Exception e) {
                log.error("Error creating Kafka producer for client: {}", client, e);
                throw new ApiException(ErrorCode.QUEUE_PRODUCER_CREATION_FAILED);
            }
        });
    }
}
