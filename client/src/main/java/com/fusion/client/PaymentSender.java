package com.fusion.client;

import java.io.FileInputStream;
import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.KeyFactory;
import java.security.interfaces.RSAPrivateKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.Base64;
import java.util.Properties;

public class PaymentSender {
    public static void main(String[] args) throws Exception {
        // Load properties
        Properties props = new Properties();
        try (InputStream input = new FileInputStream("client/resources/config.properties")) {
            props.load(input);
        }

        String issuer = props.getProperty("issuer");
        String subject = props.getProperty("subject");
        String actSub = props.getProperty("act.sub");
        String audience = props.getProperty("audience");
        String privateKeyPath = props.getProperty("private.key.path");

        // Sample payload
        String payload = "{\"amount\":\"100.00\",\"debtor\":\"Alice\",\"creditor\":\"Bob\"}";

        // Load and parse private key
        String keyPEM = Files.readString(Paths.get(privateKeyPath))
                             .replaceAll("-----\\w+ PRIVATE KEY-----", "")
                             .replaceAll("\\s", "");
        byte[] decoded = Base64.getDecoder().decode(keyPEM);
        RSAPrivateKey privateKey = (RSAPrivateKey) KeyFactory.getInstance("RSA")
                .generatePrivate(new PKCS8EncodedKeySpec(decoded));

        // Generate JWT and Signature
        String jwt = JWTGenerator.generateJWT(issuer, subject, actSub, audience, privateKey);
        String signature = SignUtil.signPayload(payload, privateKey);

        // Send HTTP POST
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/payments"))
                .header("Authorization", "Bearer " + jwt)
                .header("Content-Type", "application/json")
                .header("X-Signature", signature)
                .POST(HttpRequest.BodyPublishers.ofString(payload))
                .build();

        HttpClient client = HttpClient.newHttpClient();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        System.out.println("Status: " + response.statusCode());
        System.out.println("Response: " + response.body());
    }
}
