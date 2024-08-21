package in.javis.toolserver.security;


import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.apache.commons.codec.binary.Base64;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Map;

import static in.javis.toolserver.security.SecurityConstants.SECRET_KEY;

@Component
public class JWTUtil {

    public static Map<String, Object> validateToken(String token) {
        SecretKey key = Keys.hmacShaKeyFor(fromHex(SECRET_KEY));

        return Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public static byte[] fromHex(String hex) {
        byte[] bytes = new byte[hex.length() / 2];
        for (int i = 0; i < bytes.length; i++) {
            bytes[i] = (byte) Integer.parseInt(hex.substring(2 * i, 2 * i + 2), 16);
        }
        return bytes;
    }

}
