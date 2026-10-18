package com.project_x.core.security;

import com.project_x.core.security.util.KeyGeneratorUtility;
import org.junit.jupiter.api.Test;

import java.security.KeyPair;
import java.util.Base64;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class RSAKeyPropertiesTests {

    @Test
    void loadsConfiguredRsaKeys() {
        KeyPair pair = KeyGeneratorUtility.generateRsaKey();
        RSAKeyProperties properties = new RSAKeyProperties();
        properties.setPublicKeyBase64(
                Base64.getEncoder().encodeToString(
                        pair.getPublic().getEncoded()
                )
        );
        properties.setPrivateKeyBase64(
                Base64.getEncoder().encodeToString(
                        pair.getPrivate().getEncoded()
                )
        );

        properties.initialiseKeys();

        assertThat(properties.getPublicKey().getEncoded())
                .isEqualTo(pair.getPublic().getEncoded());
        assertThat(properties.getPrivateKey().getEncoded())
                .isEqualTo(pair.getPrivate().getEncoded());
    }

    @Test
    void rejectsPartiallyConfiguredKeyPair() {
        RSAKeyProperties properties = new RSAKeyProperties();
        properties.setPublicKeyBase64("configured-without-private-key");

        assertThatThrownBy(properties::initialiseKeys)
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining(
                        "JWT_PUBLIC_KEY_BASE64 and JWT_PRIVATE_KEY_BASE64"
                );
    }
}
