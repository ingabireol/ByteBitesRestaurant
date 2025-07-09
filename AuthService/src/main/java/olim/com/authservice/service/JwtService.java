package olim.com.authservice.service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import olim.com.authservice.entity.User;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

/**
 * JWT Service for token generation and validation
 * 
 * This service handles:
 * - JWT token creation with user information
 * - Token validation and parsing
 * - Extracting user details from tokens
 * - Token expiration management
 */
@Service
public class JwtService {

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.expiration}")
    private long jwtExpiration;

    /**
     * Generate JWT token for user
     * 
     * @param user authenticated user
     * @return JWT token string
     */
    public String generateToken(User user) {
        Map<String, Object> extraClaims = new HashMap<>();
        extraClaims.put("userId", user.getId().toString());
        extraClaims.put("firstName", user.getFirstName());
        extraClaims.put("lastName", user.getLastName());
        extraClaims.put("roles", user.getRole().name());
        extraClaims.put("provider", user.getProvider() != null ? user.getProvider() : "local");
        
        return generateToken(extraClaims, user);
    }

    /**
     * Generate JWT token with extra claims
     * 
     * @param extraClaims additional claims to include in token
     * @param userDetails user details
     * @return JWT token string
     */
    public String generateToken(Map<String, Object> extraClaims, User userDetails) {
        return buildToken(extraClaims, userDetails, jwtExpiration);
    }

    /**
     * Generate refresh token (longer expiration)
     * 
     * @param user authenticated user
     * @return refresh token string
     */
    public String generateRefreshToken(User user) {
        Map<String, Object> extraClaims = new HashMap<>();
        extraClaims.put("userId", user.getId().toString());
        extraClaims.put("type", "refresh");
        
        // Refresh token expires in 7 days
        return buildToken(extraClaims, user, 604800000L);
    }

    /**
     * Build JWT token with specified claims and expiration
     */
    private String buildToken(Map<String, Object> extraClaims, User userDetails, long expiration) {
        Date now = new Date();
        Date expirationDate = new Date(now.getTime() + expiration);

        return Jwts.builder()
                .setClaims(extraClaims)
                .setSubject(userDetails.getUsername())
                .setIssuedAt(now)
                .setExpiration(expirationDate)
                .setIssuer("bytebites-auth-service")
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    /**
     * Extract username from JWT token
     * 
     * @param token JWT token
     * @return username (email)
     */
    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    /**
     * Extract user ID from JWT token
     * 
     * @param token JWT token
     * @return user ID
     */
    public String extractUserId(String token) {
        return extractClaim(token, claims -> claims.get("userId", String.class));
    }

    /**
     * Extract user roles from JWT token
     * 
     * @param token JWT token
     * @return user roles
     */
    public String extractRoles(String token) {
        return extractClaim(token, claims -> claims.get("roles", String.class));
    }

    /**
     * Extract expiration date from JWT token
     * 
     * @param token JWT token
     * @return expiration date
     */
    public Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    /**
     * Extract specific claim from JWT token
     * 
     * @param token JWT token
     * @param claimsResolver function to extract specific claim
     * @return extracted claim
     */
    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    /**
     * Extract all claims from JWT token
     * 
     * @param token JWT token
     * @return all claims
     */
    private Claims extractAllClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    /**
     * Check if token is expired
     * 
     * @param token JWT token
     * @return true if expired
     */
    public boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    /**
     * Validate JWT token against user details
     * 
     * @param token JWT token
     * @param userDetails user details
     * @return true if valid
     */
    public boolean isTokenValid(String token, User userDetails) {
        final String username = extractUsername(token);
        return (username.equals(userDetails.getUsername())) && !isTokenExpired(token);
    }

    /**
     * Get signing key from secret
     */
    private SecretKey getSigningKey() {
        byte[] keyBytes = Decoders.BASE64.decode(secret);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    /**
     * Get token expiration time in milliseconds
     * 
     * @return expiration time
     */
    public long getJwtExpiration() {
        return jwtExpiration;
    }
}