package com.itshaharcha.auth.config;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class AuthPropertiesTest {

    @Test
    void exposesAutoActivateFlag() {
        assertThat(new AuthProperties(true).autoActivate()).isTrue();
        assertThat(new AuthProperties(false).autoActivate()).isFalse();
    }
}
