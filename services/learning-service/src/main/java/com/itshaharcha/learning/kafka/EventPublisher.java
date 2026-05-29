package com.itshaharcha.learning.kafka;

public interface EventPublisher {

    void publish(String topic, String key, Object payload);
}
