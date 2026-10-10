package com.slokam.av.security;

import com.nimbusds.jose.jwk.source.ImmutableSecret;
import com.slokam.av.entity.User;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.*;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.time.Instant;

import javax.crypto.spec.SecretKeySpec;

@Service
public class JwtService {
    private final JwtEncoder encoder;
    private final JwtDecoder decoder;
    private final long expiration;

    public JwtService(
            @Value("${jwt.secret}") String secret,
            @Value("${jwt.expiration-ms:3600000}") long expiration) {
        byte[] bytes = secret.getBytes(StandardCharsets.UTF_8);
        if (bytes.length < 32 || expiration < 1)
            throw new IllegalArgumentException(
                    "JWT_SECRET needs at least 32 bytes and expiry must be positive");
        var key = new SecretKeySpec(bytes, "HmacSHA256");
        encoder = new NimbusJwtEncoder(new ImmutableSecret<>(key));
        var nimbus = NimbusJwtDecoder.withSecretKey(key).macAlgorithm(MacAlgorithm.HS256).build();
        nimbus.setJwtValidator(
                new org.springframework.security.oauth2.core.DelegatingOAuth2TokenValidator<>(
                        new JwtTimestampValidator(java.time.Duration.ZERO),
                        new JwtIssuerValidator("audience-verdict")));
        decoder = nimbus;
        this.expiration = expiration;
    }

    public String generate(User user) {
        Instant now = Instant.now();
        var claims =
                JwtClaimsSet.builder()
                        .issuer("audience-verdict")
                        .subject(user.id)
                        .claim("userId", user.id)
                        .claim("mobile", user.mobile)
                        .claim("role", user.role.name())
                        .issuedAt(now)
                        .expiresAt(now.plusMillis(expiration))
                        .build();
        return encoder.encode(
                        JwtEncoderParameters.from(
                                JwsHeader.with(MacAlgorithm.HS256).build(), claims))
                .getTokenValue();
    }

    public Jwt validate(String token) {
        return decoder.decode(token);
    }
}
