package com.itshaharcha.auth.kafka;

import org.junit.jupiter.api.Test;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;

import java.util.concurrent.CompletableFuture;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class EventPublisherTest {

    @Test
    void loggingPublisher_publishesWithoutError() {
        EventPublisher publisher = new LoggingEventPublisher().eventPublisher();
        assertThatCode(() -> publisher.publish("user.registered", "k", "payload"))
                .doesNotThrowAnyException();
    }

    @Test
    @SuppressWarnings("unchecked")
    void kafkaPublisher_sendsToTemplate_onSuccess() {
        KafkaTemplate<String, Object> template = mock(KafkaTemplate.class);
        CompletableFuture<SendResult<String, Object>> future =
                CompletableFuture.completedFuture(null);
        when(template.send(eq("topic"), eq("key"), eq("payload"))).thenReturn(future);

        new KafkaEventPublisher(template).publish("topic", "key", "payload");

        verify(template).send("topic", "key", "payload");
    }

    @Test
    @SuppressWarnings("unchecked")
    void kafkaPublisher_handlesSendFailure_withoutThrowing() {
        KafkaTemplate<String, Object> template = mock(KafkaTemplate.class);
        CompletableFuture<SendResult<String, Object>> future =
                CompletableFuture.failedFuture(new RuntimeException("broker down"));
        when(template.send(eq("topic"), eq("key"), eq("payload"))).thenReturn(future);

        assertThatCode(() -> new KafkaEventPublisher(template).publish("topic", "key", "payload"))
                .doesNotThrowAnyException();
    }
}
