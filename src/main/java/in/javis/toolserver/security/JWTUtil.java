package in.javis.toolserver.security;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Map;

import static in.javis.toolserver.security.SecurityConstants.SECRET_KEY;

/**
 * Utility class for handling JSON Web Tokens (JWT).
 * <p>
 * This class provides methods for validating JWTs and converting hex-encoded secrets into byte arrays.
 * It uses the JJWT library for parsing and verifying JWT tokens.
 * </p>
 */
@Component
public class JWTUtil {

    /**
     * Validates a JWT token and extracts the claims.
     * <p>
     * This method takes a JWT token, validates it using a secret key, and returns the claims as a map.
     * The secret key is derived from a hex-encoded string stored in {@link SecurityConstants#SECRET_KEY}.
     * </p>
     *
     * @param token the JWT token to validate
     * @return a map of claims extracted from the validated token
     */
    public static Map<String, Object> validateToken(String token) {
        SecretKey key = Keys.hmacShaKeyFor(fromHex(SECRET_KEY));

        return Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    /**
     * Converts a hex-encoded string into a byte array.
     * <p>
     * This method converts a string representing hexadecimal values into an array of bytes.
     * This is used to derive the secret key from a hex-encoded format.
     * </p>
     *
     * @param hex the hex-encoded string
     * @return a byte array representing the hex-encoded string
     */
    public static byte[] fromHex(String hex) {
        byte[] bytes = new byte[hex.length() / 2];
        for (int i = 0; i < bytes.length; i++) {
            bytes[i] = (byte) Integer.parseInt(hex.substring(2 * i, 2 * i + 2), 16);
        }
        return bytes;
    }
}
