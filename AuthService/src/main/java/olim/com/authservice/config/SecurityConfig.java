package olim.com.authservice.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Security Configuration for Auth Service
 * 
 * Configures security settings for the authentication service:
 * - Password encoding
 * - HTTP security rules
 * - Session management
 * - CORS configuration
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    /**
     * Password encoder bean
     * 
     * Uses BCrypt for secure password hashing
     * BCrypt is a adaptive hash function based on the Blowfish cipher
     * 
     * @return BCryptPasswordEncoder instance
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(12); // Strength of 12 (good balance of security and performance)
    }

    /**
     * Security filter chain configuration
     * 
     * Configures HTTP security for the auth service
     * 
     * @param http HttpSecurity object to configure
     * @return SecurityFilterChain
     */
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            // Disable CSRF for REST APIs
            .csrf(csrf -> csrf.disable())
            
            // Configure CORS
            .cors(cors -> cors.configure(http))
            
            // Session management - stateless for JWT
            .sessionManagement(session -> session
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            
            // Configure authorization
            .authorizeHttpRequests(auth -> auth
                // Public endpoints - no authentication required
                .requestMatchers(
                    "/api/auth/register",
                    "/api/auth/login", 
                    "/api/auth/refresh",
                    "/api/auth/health",
                    "/api/auth/validate",
                    "/actuator/health",
                    "/h2-console/**",
                    "/error"
                ).permitAll()
                
                // OAuth2 endpoints (if implementing OAuth2)
                .requestMatchers("/oauth2/**", "/login/oauth2/**").permitAll()
                
                // All other endpoints require authentication
                .anyRequest().authenticated()
            )
            
            // Disable form login (we're using JWT)
            .formLogin(AbstractHttpConfigurer::disable)
            
            // Disable HTTP Basic authentication
            .httpBasic(AbstractHttpConfigurer::disable)
            
            // Configure headers for H2 console (development only)
            .headers(headers -> headers
                .frameOptions(HeadersConfigurer.FrameOptionsConfig::sameOrigin)// Allow H2 console frames
            );

        return http.build();
    }

    /**
     * CORS configuration bean
     * 
     * Allows cross-origin requests from web applications
     * In production, you should specify exact origins instead of "*"
     */
    @Bean
    public org.springframework.web.cors.CorsConfigurationSource corsConfigurationSource() {
        org.springframework.web.cors.CorsConfiguration configuration = new org.springframework.web.cors.CorsConfiguration();
        
        // Allow specific origins (in production, replace "*" with actual origins)
        configuration.addAllowedOriginPattern("*");
        
        // Allow specific methods
        configuration.addAllowedMethod("GET");
        configuration.addAllowedMethod("POST");
        configuration.addAllowedMethod("PUT");
        configuration.addAllowedMethod("DELETE");
        configuration.addAllowedMethod("OPTIONS");
        
        // Allow specific headers
        configuration.addAllowedHeader("*");
        
        // Allow credentials
        configuration.setAllowCredentials(true);
        
        // Cache preflight response for 1 hour
        configuration.setMaxAge(3600L);
        
        org.springframework.web.cors.UrlBasedCorsConfigurationSource source = 
            new org.springframework.web.cors.UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        
        return source;
    }
}