package com.itshaharcha.analytics.kafka;

public interface EventPublisher {

    void publish(String topic, String key, Object payload);
}
