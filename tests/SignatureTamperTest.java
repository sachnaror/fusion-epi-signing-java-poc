
import com.fusion.client.SignUtil;

import java.security.KeyFactory;
import java.security.interfaces.RSAPrivateKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.Base64;
import java.nio.file.Files;
import java.nio.file.Path;

public class SignatureTamperTest {
    public static void main(String[] args) throws Exception {
        String original = "{"amount":"100.00","debtor":"Alice","creditor":"Bob"}";
        String tampered = "{"amount":"999.00","debtor":"Alice","creditor":"Bob"}";

        String key = Files.readString(Path.of("client/resources/private_key.pem"))
                         .replaceAll("-----\w+ PRIVATE KEY-----", "")
                         .replaceAll("\s", "");
        byte[] decoded = Base64.getDecoder().decode(key);
        RSAPrivateKey privateKey = (RSAPrivateKey) KeyFactory.getInstance("RSA").generatePrivate(new PKCS8EncodedKeySpec(decoded));

        String signature = SignUtil.signPayload(original, privateKey);
        System.out.println("Original Signature: " + signature);

        // send 'tampered' with original signature to mock server for failure case test
    }
}
