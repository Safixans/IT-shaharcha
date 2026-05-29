package com.itshaharcha.common.entity;

import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class BaseEntityTest {

    private static final class Sample extends BaseEntity {
    }

    @Test
    void accessors_roundTrip_andDefaultNotDeleted() {
        Sample e = new Sample();
        assertThat(e.isDeleted()).isFalse();

        UUID id = UUID.randomUUID();
        Instant now = Instant.now();
        e.setId(id);
        e.setCreatedAt(now);
        e.setUpdatedAt(now);
        e.setCreatedBy("alice");
        e.setUpdatedBy("bob");
        e.setDeleted(true);
        e.setVersion(3L);

        assertThat(e.getId()).isEqualTo(id);
        assertThat(e.getCreatedAt()).isEqualTo(now);
        assertThat(e.getUpdatedAt()).isEqualTo(now);
        assertThat(e.getCreatedBy()).isEqualTo("alice");
        assertThat(e.getUpdatedBy()).isEqualTo("bob");
        assertThat(e.isDeleted()).isTrue();
        assertThat(e.getVersion()).isEqualTo(3L);
    }
}
