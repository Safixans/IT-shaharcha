package com.itshaharcha.learning.kafka;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/** Fallback when Kafka is disabled: events are logged, not published. */
@Slf4j
@Configuration
public class LoggingEventPublisher {

    @Bean
    @ConditionalOnMissingBean(EventPublisher.class)
    public EventPublisher eventPublisher() {
        return (topic, key, payload) ->
                log.info("[event:{}] key={} payload={}", topic, key, payload);
    }
}
