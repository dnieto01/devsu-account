package com.ds.devsuaccount.infraestructure.queue;

public interface IQueueService {

    void publish(Object data, QueueClient client);

}
