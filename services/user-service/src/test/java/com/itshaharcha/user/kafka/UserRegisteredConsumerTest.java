package com.itshaharcha.user.kafka;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.itshaharcha.user.service.ProfileService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.UUID;

import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class UserRegisteredConsumerTest {

    @Mock private ProfileService profileService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    private UserRegisteredConsumer consumer() {
        return new UserRegisteredConsumer(profileService, objectMapper);
    }

    @Test
    void provisionsProfile_forValidEvent() {
        UUID accountId = UUID.randomUUID();
        String message = "{\"accountId\":\"" + accountId + "\",\"email\":\"a@b.com\"}";

        consumer().onUserRegistered(message);

        verify(profileService).getOrCreateProfile(accountId);
    }

    @Test
    void ignoresEvent_missingAccountId() {
        consumer().onUserRegistered("{\"email\":\"a@b.com\"}");
        verify(profileService, never()).getOrCreateProfile(org.mockito.ArgumentMatchers.any());
    }

    @Test
    void swallowsMalformedJson() {
        consumer().onUserRegistered("not-json");
        verify(profileService, never()).getOrCreateProfile(org.mockito.ArgumentMatchers.any());
    }

    @Test
    void swallowsInvalidAccountId() {
        consumer().onUserRegistered("{\"accountId\":\"not-a-uuid\"}");
        verify(profileService, never()).getOrCreateProfile(org.mockito.ArgumentMatchers.any());
    }
}
