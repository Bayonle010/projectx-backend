package com.project_x.core.security;

import com.project_x.core.security.util.KeyGeneratorUtility;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

@Component
@ConfigurationProperties(prefix = "app.jwt")
public class RSAKeyProperties {

    private static final Logger log =
            LoggerFactory.getLogger(RSAKeyProperties.class);

    private String publicKeyBase64;
    private String privateKeyBase64;

    private RSAPublicKey publicKey;
    private RSAPrivateKey privateKey;

    @PostConstruct
    void initialiseKeys() {
        boolean publicKeyConfigured = hasText(publicKeyBase64);
        boolean privateKeyConfigured = hasText(privateKeyBase64);

        if (publicKeyConfigured != privateKeyConfigured) {
            throw new IllegalStateException(
                    "Both JWT_PUBLIC_KEY_BASE64 and JWT_PRIVATE_KEY_BASE64 must be configured"
            );
        }

        if (!publicKeyConfigured) {
            KeyPair pair = KeyGeneratorUtility.generateRsaKey();
            this.publicKey = (RSAPublicKey) pair.getPublic();
            this.privateKey = (RSAPrivateKey) pair.getPrivate();
            log.warn(
                    "Persistent JWT keys are not configured; tokens will become invalid when the application restarts"
            );
            return;
        }

        try {
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            this.publicKey = (RSAPublicKey) keyFactory.generatePublic(
                    new X509EncodedKeySpec(decode(publicKeyBase64))
            );
            this.privateKey = (RSAPrivateKey) keyFactory.generatePrivate(
                    new PKCS8EncodedKeySpec(decode(privateKeyBase64))
            );
        } catch (Exception exception) {
            throw new IllegalStateException(
                    "Configured JWT RSA keys are invalid",
                    exception
            );
        }
    }

    private byte[] decode(String value) {
        return Base64.getDecoder().decode(value.replaceAll("\\s", ""));
    }

    private boolean hasText(String value) {
        return value != null && !value.isBlank();
    }

    public RSAPublicKey getPublicKey() {
        return publicKey;
    }

    public RSAPrivateKey getPrivateKey() {
        return privateKey;
    }

    public void setPublicKeyBase64(String publicKeyBase64) {
        this.publicKeyBase64 = publicKeyBase64;
    }

    public void setPrivateKeyBase64(String privateKeyBase64) {
        this.privateKeyBase64 = privateKeyBase64;
    }

}
