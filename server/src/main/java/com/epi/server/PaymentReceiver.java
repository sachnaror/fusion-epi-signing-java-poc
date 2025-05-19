
package com.epi.server;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.nio.file.Files;
import java.nio.file.Path;
import java.security.KeyFactory;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import java.util.Map;

@RestController
public class PaymentReceiver {
    @PostMapping("/payments")
    public ResponseEntity<?> receivePayment(@RequestBody Map<String, Object> payload,
                                            @RequestHeader(value = "X-Signature", required = false) String signature) throws Exception {
        if (signature == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Missing signature");
        }

        String pubKeyPEM = Files.readString(Path.of("server/resources/public_key.pem"));
        pubKeyPEM = pubKeyPEM.replaceAll("-----\w+ PUBLIC KEY-----", "").replaceAll("\s", "");
        byte[] decoded = Base64.getDecoder().decode(pubKeyPEM);
        RSAPublicKey publicKey = (RSAPublicKey) KeyFactory.getInstance("RSA").generatePublic(new X509EncodedKeySpec(decoded));

        boolean isValid = VerifyUtil.verify(payload, signature, publicKey);
        if (!isValid) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid signature");
        }
        return ResponseEntity.ok("Payment accepted");
    }
}
