package com.itshaharcha.user.kafka;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.itshaharcha.user.service.ProfileService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * On {@code user.registered}, provisions an empty profile for the new account.
 * Active only when {@code app.events.kafka-enabled=true}. The payload is parsed
 * leniently (by field) so it tolerates producer-side type headers.
 */
@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "app.events", name = "kafka-enabled", havingValue = "true")
public class UserRegisteredConsumer {

    private final ProfileService profileService;
    private final ObjectMapper objectMapper;

    @KafkaListener(topics = "user.registered", groupId = "user-service")
    public void onUserRegistered(String message) {
        try {
            JsonNode node = objectMapper.readTree(message);
            JsonNode accountIdNode = node.get("accountId");
            if (accountIdNode == null || accountIdNode.isNull()) {
                log.warn("user.registered event missing accountId: {}", message);
                return;
            }
            UUID accountId = UUID.fromString(accountIdNode.asText());
            profileService.getOrCreateProfile(accountId);
        } catch (Exception ex) {
            log.error("Failed to process user.registered event: {}", message, ex);
        }
    }
}
