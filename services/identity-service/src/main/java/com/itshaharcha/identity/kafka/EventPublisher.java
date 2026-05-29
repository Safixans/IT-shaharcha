package com.itshaharcha.identity.kafka;

public interface EventPublisher {

    void publish(String topic, String key, Object payload);
}
