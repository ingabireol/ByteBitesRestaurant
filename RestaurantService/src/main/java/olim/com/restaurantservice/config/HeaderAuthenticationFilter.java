package olim.com.restaurantservice.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Header-based Authentication Filter
 * 
 * Extracts user information from headers sent by API Gateway:
 * - X-User-Id: User's unique identifier
 * - X-User-Email: User's email address
 * - X-User-Roles: Comma-separated list of roles
 * 
 * Creates Spring Security authentication context from these headers
 */
@Component
public class HeaderAuthenticationFilter extends OncePerRequestFilter {

    private static final String USER_ID_HEADER = "X-User-Id";
    private static final String USER_EMAIL_HEADER = "X-User-Email";
    private static final String USER_ROLES_HEADER = "X-User-Roles";

    @Override
    protected void doFilterInternal(HttpServletRequest request, 
                                  HttpServletResponse response, 
                                  FilterChain filterChain) throws ServletException, IOException {
        
        // Extract user information from headers
        String userId = request.getHeader(USER_ID_HEADER);
        String userEmail = request.getHeader(USER_EMAIL_HEADER);
        String userRoles = request.getHeader(USER_ROLES_HEADER);
        System.out.println("👌👌👌Headers are: "+userId+" "+userEmail+" "+userRoles);
        // If user headers are present, create authentication
        if (userId != null && userEmail != null && userRoles != null) {
            try {
                // Parse roles and create authorities
                List<SimpleGrantedAuthority> authorities = Arrays.stream(userRoles.split(","))
                        .map(String::trim)
                        .map(SimpleGrantedAuthority::new)
                        .collect(Collectors.toList());

                // Create user principal with email as username
                UserPrincipal userPrincipal = new UserPrincipal(
                    Long.parseLong(userId),
                    userEmail,
                    authorities
                );

                // Create authentication token
                UsernamePasswordAuthenticationToken authentication = 
                    new UsernamePasswordAuthenticationToken(
                        userPrincipal, 
                        null, 
                        authorities
                    );

                // Set authentication in security context
                SecurityContextHolder.getContext().setAuthentication(authentication);

            } catch (Exception e) {
                // Log error but don't fail the request
                logger.warn("Failed to parse user headers: " + e.getMessage());
            }
        }

        // Continue with the filter chain
        filterChain.doFilter(request, response);
    }

    /**
     * Simple user principal class to hold user information
     */
    public static class UserPrincipal {
        private final Long id;
        private final String email;
        private final List<SimpleGrantedAuthority> authorities;

        public UserPrincipal(Long id, String email, List<SimpleGrantedAuthority> authorities) {
            this.id = id;
            this.email = email;
            this.authorities = authorities;
        }

        public Long getId() { return id; }
        public String getEmail() { return email; }
        public List<SimpleGrantedAuthority> getAuthorities() { return authorities; }

        @Override
        public String toString() {
            return "UserPrincipal{" +
                   "id=" + id +
                   ", email='" + email + '\'' +
                   ", authorities=" + authorities +
                   '}';
        }
    }
}