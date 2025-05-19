
package com.fusion.client;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;

import java.security.interfaces.RSAPrivateKey;
import java.time.Instant;
import java.util.Date;
import java.util.Map;
import java.util.UUID;

public class JWTGenerator {
    public static String generateJWT(String issuer, String subject, String actSub, String audience, RSAPrivateKey privateKey) {
        Instant now = Instant.now();
        Algorithm algorithm = Algorithm.RSA256(null, privateKey);
        return JWT.create()
                .withJWTId(UUID.randomUUID().toString())
                .withIssuer(issuer)
                .withSubject(subject)
                .withAudience(audience)
                .withIssuedAt(Date.from(now))
                .withExpiresAt(Date.from(now.plusSeconds(60)))
                .withClaim("act", Map.of("sub", actSub))
                .sign(algorithm);
    }
}
