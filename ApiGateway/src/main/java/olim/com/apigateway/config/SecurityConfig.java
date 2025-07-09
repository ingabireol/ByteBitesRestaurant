package olim.com.apigateway.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;


@Configuration
@EnableWebFluxSecurity
public class SecurityConfig {

    /**
     * Configure security for API Gateway
     * 
     * We disable most of Spring Security's default behavior because:
     * 1. JWT validation is handled by our custom AuthenticationFilter
     * 2. CORS is configured in application.yml
     * 3. We want to allow all traffic to pass through (filtering happens in custom filter)
     */
    @Bean
    public SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity http) {
        return http
                // Disable CSRF for APIs
                .csrf(csrf -> csrf.disable())
                
                // Disable form login (we're using JWT)
                .formLogin(form -> form.disable())
                
                // Disable HTTP Basic authentication
                .httpBasic(basic -> basic.disable())
                
                // Configure authorization
                .authorizeExchange(exchanges -> exchanges
                        // Allow actuator endpoints for health checks
                        .pathMatchers("/actuator/**").permitAll()
                        
                        // Allow Eureka endpoints (for monitoring)
                        .pathMatchers("/eureka/**").permitAll()
                        
                        // Allow all other requests (our custom filter will handle auth)
                        .anyExchange().permitAll()
                )
                .build();
    }
}