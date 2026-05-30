package com.itshaharcha.assessment.kafka;

public interface EventPublisher {

    void publish(String topic, String key, Object payload);
}
