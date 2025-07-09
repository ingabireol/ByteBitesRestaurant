package olim.com.apigateway.filter;

import olim.com.apigateway.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Component
public class AuthenticationFilter extends AbstractGatewayFilterFactory<AuthenticationFilter.Config> {

    @Autowired
    private JwtUtil jwtUtil;

    public AuthenticationFilter() {
        super(Config.class);
    }

    /**
     * Main filter logic
     */
    @Override
    public GatewayFilter apply(Config config) {
        return ((exchange, chain) -> {
            ServerHttpRequest request = exchange.getRequest();

            // Check if Authorization header exists
            if (!request.getHeaders().containsKey(HttpHeaders.AUTHORIZATION)) {
                return this.onError(exchange, "No Authorization header", HttpStatus.UNAUTHORIZED);
            }

            // Extract token from Authorization header
            String authHeader = request.getHeaders().get(HttpHeaders.AUTHORIZATION).get(0);
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                return this.onError(exchange, "Invalid Authorization header", HttpStatus.UNAUTHORIZED);
            }

            String token = authHeader.substring(7); // Remove "Bearer " prefix

            try {
                // Validate token
                if (!jwtUtil.validateToken(token)) {
                    return this.onError(exchange, "Invalid or expired token", HttpStatus.UNAUTHORIZED);
                }

                // Extract user information from token
                String username = jwtUtil.extractUsername(token);
                String userId = jwtUtil.extractUserId(token);
                String roles = jwtUtil.extractRoles(token);

                // Add user info to request headers for downstream services
                ServerHttpRequest modifiedRequest = request.mutate()
                        .header("X-User-Id", userId)
                        .header("X-User-Email", username)
                        .header("X-User-Roles", roles)
                        .build();

                // Continue with modified request
                return chain.filter(exchange.mutate().request(modifiedRequest).build());

            } catch (Exception e) {
                return this.onError(exchange, "Token validation failed: " + e.getMessage(), HttpStatus.UNAUTHORIZED);
            }
        });
    }

    /**
     * Handle authentication errors
     */
    private Mono<Void> onError(ServerWebExchange exchange, String err, HttpStatus httpStatus) {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(httpStatus);
        response.getHeaders().add("Content-Type", "application/json");
        
        String body = "{\"error\":\"" + err + "\",\"status\":" + httpStatus.value() + "}";
        
        return response.writeWith(Mono.just(response.bufferFactory().wrap(body.getBytes())));
    }

    /**
     * Configuration class for the filter
     */
    public static class Config {
        // Configuration properties can be added here if needed
    }
}