package com.ds.devsuaccount.infraestructure.queue.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class EventMessage<T> {
    private String eventId;
    private String eventType;
    private String timestamp;
    private String version;
    private T payload;

    public EventMessage(String eventType, T payload) {
        this.eventId = UUID.randomUUID().toString();
        this.eventType = eventType;
        this.timestamp = Instant.now().toString();
        this.version = "1.0";
        this.payload = payload;
    }

}
