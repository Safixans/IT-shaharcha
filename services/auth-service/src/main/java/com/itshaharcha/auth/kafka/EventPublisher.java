package com.itshaharcha.auth.kafka;

public interface EventPublisher {

    void publish(String topic, String key, Object payload);
}
