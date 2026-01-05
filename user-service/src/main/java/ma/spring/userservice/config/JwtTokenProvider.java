package ma.spring.userservice.config;

import io.jsonwebtoken.Jwts;
<<<<<<< HEAD
import io.jsonwebtoken.security.Keys;
import ma.spring.userservice.model.User;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
=======
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import java.security.Key;
>>>>>>> 6ce757d4999ba41a617273a4b88fa27aebe5c2f5
import java.util.Date;

@Component
public class JwtTokenProvider {

<<<<<<< HEAD
    @Value("${jwt.secret:myVerySecureSecretKeyForJWTTokenGenerationThatIsAtLeast256BitsLong}")
    private String secret;

    @Value("${jwt.expiration:86400000}") // 24 hours
    private long jwtExpirationInMs;

    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    public String generateToken(Authentication authentication) {
        User userPrincipal = (User) authentication.getPrincipal();
=======
    private final Key key = Keys.secretKeyFor(SignatureAlgorithm.HS512); // securely generates 512-bit key
    private final long jwtExpirationInMs = 86400000; // 24 hours

    public String generateToken(Authentication authentication) {
        String username = authentication.getName();
>>>>>>> 6ce757d4999ba41a617273a4b88fa27aebe5c2f5
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + jwtExpirationInMs);

        return Jwts.builder()
<<<<<<< HEAD
                .setSubject(userPrincipal.getUsername())
                .claim("userId", userPrincipal.getId())
                .claim("role", userPrincipal.getRole())
                .claim("email", userPrincipal.getEmail())
                .setIssuedAt(now)
                .setExpiration(expiryDate)
                .signWith(getSigningKey())
                .compact();
    }

    public String generateTokenFromUser(User user) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + jwtExpirationInMs);

        return Jwts.builder()
                .setSubject(user.getUsername())
                .claim("userId", user.getId())
                .claim("role", user.getRole().toString())
                .claim("email", user.getEmail())
                .setIssuedAt(now)
                .setExpiration(expiryDate)
                .signWith(getSigningKey())
=======
                .setSubject(username)
                .setIssuedAt(now)
                .setExpiration(expiryDate)
                .signWith(key) // key is already HS512
>>>>>>> 6ce757d4999ba41a617273a4b88fa27aebe5c2f5
                .compact();
    }
}
