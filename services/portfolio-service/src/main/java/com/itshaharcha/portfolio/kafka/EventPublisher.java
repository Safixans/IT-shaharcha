package com.itshaharcha.portfolio.kafka;

public interface EventPublisher {

    void publish(String topic, String key, Object payload);
}
