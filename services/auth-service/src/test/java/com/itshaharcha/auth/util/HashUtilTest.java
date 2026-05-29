package com.itshaharcha.auth.util;

import org.junit.jupiter.api.Test;

import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;

import static org.assertj.core.api.Assertions.assertThat;

class HashUtilTest {

    @Test
    void hasPrivateConstructor_andIsInstantiableViaReflection() throws Exception {
        Constructor<HashUtil> ctor = HashUtil.class.getDeclaredConstructor();
        assertThat(Modifier.isPrivate(ctor.getModifiers())).isTrue();
        ctor.setAccessible(true);
        assertThat(ctor.newInstance()).isNotNull();
    }

    @Test
    void sha256_isDeterministicAndHex64() {
        String a = HashUtil.sha256("hello");
        String b = HashUtil.sha256("hello");
        assertThat(a).isEqualTo(b);
        assertThat(a).hasSize(64).matches("[0-9a-f]{64}");
    }

    @Test
    void sha256_matchesKnownVector() {
        // SHA-256("") is well-known.
        assertThat(HashUtil.sha256(""))
                .isEqualTo("e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855");
    }

    @Test
    void sha256_differsForDifferentInput() {
        assertThat(HashUtil.sha256("a")).isNotEqualTo(HashUtil.sha256("b"));
    }

    @Test
    void generateOtp_isSixDigits() {
        for (int i = 0; i < 200; i++) {
            assertThat(HashUtil.generateOtp()).matches("\\d{6}");
        }
    }
}
