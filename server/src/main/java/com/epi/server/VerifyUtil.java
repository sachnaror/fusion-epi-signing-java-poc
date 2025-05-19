
package com.epi.server;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.Signature;
import java.security.interfaces.RSAPublicKey;
import java.util.Base64;
import java.util.Map;

public class VerifyUtil {
    public static boolean verify(Map<String, Object> payload, String signature, RSAPublicKey publicKey) throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        String canonical = mapper.writeValueAsString(payload);
        byte[] hash = MessageDigest.getInstance("SHA-256").digest(canonical.getBytes(StandardCharsets.UTF_8));
        Signature verifier = Signature.getInstance("SHA256withRSA");
        verifier.initVerify(publicKey);
        verifier.update(hash);
        return verifier.verify(Base64.getDecoder().decode(signature));
    }
}
