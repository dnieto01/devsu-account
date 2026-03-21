package com.ds.devsuaccount.infraestructure.queue;

public enum QueueClient {

    TRANSFER_INTENT_QUEUE("transaction-intent"),
    NEWS("news"),
    ;

    private final String topic;

    QueueClient(String resourceName) {
        this.topic = resourceName;
    }

    public String getResourceName() {
        return this.topic;
    }
}
