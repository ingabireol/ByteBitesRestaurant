package olim.com.orderservice.config;

import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * Security Configuration for Order Service
 * 
 * Similar to Restaurant Service - trusts API Gateway for authentication
 * Uses method-level security with @PreAuthorize annotations
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true) // Enable @PreAuthorize annotations
@AllArgsConstructor
public class SecurityConfig {

    private HeaderAuthenticationFilter headerAuthenticationFilter;
    /**
     * Security filter chain configuration
     * 
     * Orders are sensitive data, so most endpoints require authentication
     * The API Gateway handles JWT validation and forwards user info via headers
     */
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            // Disable CSRF for REST APIs
            .csrf(AbstractHttpConfigurer::disable)

            // Configure CORS
            .cors(cors -> cors.configure(http))
            
            // Session management - stateless (no sessions)
            .sessionManagement(session -> session
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            
            // Configure authorization
            .authorizeHttpRequests(auth -> auth
                // Public endpoints - no authentication required
                .requestMatchers(
                    "/api/orders/health",       // Health check
                    "/actuator/**",         // Actuator health
                    "/h2-console/**",          // H2 console (dev only)
                    "/error"                   // Error pages
                ).permitAll()
                
                // All other order endpoints require authentication
                // Specific authorization handled by @PreAuthorize annotations
                .anyRequest().authenticated()
            )
            
            // Disable form login (we're using JWT via gateway)
            .formLogin(AbstractHttpConfigurer::disable)
            
            // Disable HTTP Basic authentication
            .httpBasic(AbstractHttpConfigurer::disable)
                .addFilterBefore(headerAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
            // Configure headers for H2 console (development only)
            .headers(headers -> headers
                .frameOptions(HeadersConfigurer.FrameOptionsConfig::sameOrigin)
            );

        return http.build();
    }

    /**
     * CORS configuration
     */
    @Bean
    public org.springframework.web.cors.CorsConfigurationSource corsConfigurationSource() {
        org.springframework.web.cors.CorsConfiguration configuration = 
            new org.springframework.web.cors.CorsConfiguration();
        
        // Allow all origins for development (restrict in production)
        configuration.addAllowedOriginPattern("*");
        
        // Allow all HTTP methods
        configuration.addAllowedMethod("*");
        
        // Allow all headers
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