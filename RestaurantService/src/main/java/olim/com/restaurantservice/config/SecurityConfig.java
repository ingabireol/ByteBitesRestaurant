package olim.com.restaurantservice.config;

import lombok.AllArgsConstructor;
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
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

@Configuration
@EnableWebSecurity
@AllArgsConstructor
@EnableMethodSecurity(prePostEnabled = true)
public class SecurityConfig {
    private HeaderAuthenticationFilter headerAuthenticationFilter;

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
                    "/api/restaurants",           // GET all restaurants
                    "/api/restaurants/{id}",      // GET restaurant by ID
                    "/api/restaurants/search",    // Search restaurants
                    "/api/restaurants/cuisine/**", // GET by cuisine
                    "/api/restaurants/*/menu",    // GET restaurant menu
                    "/api/restaurants/*/menu/**", // Menu item endpoints (GET only)
                    "/api/restaurants/health",    // Health check
                    "/actuator/health",           // Actuator health
                    "/h2-console/**",            // H2 console (dev only)
                    "/error"                     // Error pages
                ).permitAll()
                
                // Protected endpoints - require authentication
                // These will be protected by @PreAuthorize annotations
                .requestMatchers(
                    "/api/restaurants/my-restaurants",  // Owner's restaurants
                    "/api/restaurants/stats"            // Owner stats
                ).authenticated()
                
                // All POST, PUT, PATCH, DELETE require authentication
                .requestMatchers("POST", "/api/restaurants/**").authenticated()
                .requestMatchers("PUT", "/api/restaurants/**").authenticated()
                .requestMatchers("PATCH", "/api/restaurants/**").authenticated()
                .requestMatchers("DELETE", "/api/restaurants/**").authenticated()
                
                .anyRequest().authenticated()
            )
                .addFilterBefore(headerAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
            .formLogin(AbstractHttpConfigurer::disable)
            .httpBasic(AbstractHttpConfigurer::disable)
            .headers(headers -> headers
                .frameOptions(HeadersConfigurer.FrameOptionsConfig::sameOrigin)
            );

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        
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
        
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        
        return source;
    }
}