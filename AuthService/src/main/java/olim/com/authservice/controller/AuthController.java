package olim.com.authservice.controller;

import jakarta.validation.Valid;
import olim.com.authservice.dto.AuthDto.*;
import olim.com.authservice.dto.ProfileUpdateRequest;
import olim.com.authservice.entity.User;
import olim.com.authservice.service.JwtService;
import olim.com.authservice.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Authentication Controller
 *
 * Handles all authentication-related endpoints:
 * - User registration
 * - User login
 * - Token refresh
 * - Password management
 * - User profile operations
 */
@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*", maxAge = 3600)
public class AuthController {

    @Autowired
    private UserService userService;

    @Autowired
    private JwtService jwtService;

    /**
     * User registration endpoint
     * 
     * @param request registration details
     * @return authentication response with JWT token
     */
    @PostMapping("/register")
    public ResponseEntity<ApiResponse<AuthResponse>> register(@Valid @RequestBody RegisterRequest request) {
        try {
            // Validate password confirmation
            if (!request.isPasswordMatching()) {
                return ResponseEntity.badRequest()
                        .body(ApiResponse.error("Passwords do not match"));
            }

            // Check if user already exists
            if (userService.emailExists(request.getEmail())) {
                return ResponseEntity.badRequest()
                        .body(ApiResponse.error("Email is already registered"));
            }

            // Register user
            User user = userService.registerUser(
                    request.getEmail(),
                    request.getFirstName(),
                    request.getLastName(),
                    request.getPassword(),
                    request.getRole()
            );
            UserInfo userInfo = new UserInfo(user.getId(),user.getEmail(), user.getFirstName(), user.getFirstName(), user.getRole(), user.getProvider(), user.isEnabled());

            // Generate tokens
            String token = jwtService.generateToken(user);
            String refreshToken = jwtService.generateRefreshToken(user);

            // Create response
            AuthResponse authResponse = new AuthResponse(token, refreshToken, userInfo);

            return ResponseEntity.ok(ApiResponse.success("User registered successfully", authResponse));

        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Registration failed: " + e.getMessage()));
        }
    }

    /**
     * User login endpoint
     * 
     * @param request login credentials
     * @return authentication response with JWT token
     */
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AuthResponse>> login(@Valid @RequestBody LoginRequest request) {
        try {
            // Authenticate user
            User user = userService.authenticateUser(request.getEmail(), request.getPassword());

            // Generate tokens
            String token = jwtService.generateToken(user);
            String refreshToken = jwtService.generateRefreshToken(user);

            // Create response
            AuthResponse authResponse = new AuthResponse(token, refreshToken, new UserInfo(user));

            return ResponseEntity.ok(ApiResponse.success("Login successful", authResponse));

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error("Authentication failed: " + e.getMessage()));
        }
    }

    /**
     * Token refresh endpoint
     * 
     * @param request refresh token
     * @return new JWT token
     */
    @PostMapping("/refresh")
    public ResponseEntity<ApiResponse<TokenRefreshResponse>> refreshToken(@Valid @RequestBody TokenRefreshRequest request) {
        try {
            String refreshToken = request.getRefreshToken();

            // Validate refresh token
            if (jwtService.isTokenExpired(refreshToken)) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(ApiResponse.error("Refresh token is expired"));
            }

            // Extract user from refresh token
            String userEmail = jwtService.extractUsername(refreshToken);
            User user = userService.findByEmail(userEmail)
                    .orElseThrow(() -> new RuntimeException("User not found"));

            // Generate new tokens
            String newToken = jwtService.generateToken(user);
            String newRefreshToken = jwtService.generateRefreshToken(user);

            TokenRefreshResponse response = new TokenRefreshResponse(newToken, newRefreshToken);

            return ResponseEntity.ok(ApiResponse.success("Token refreshed successfully", response));

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error("Token refresh failed: " + e.getMessage()));
        }
    }

    /**
     * Get current user profile
     * Requires valid JWT token in Authorization header
     * 
     * @param userEmail extracted from JWT by gateway
     * @return user profile information
     */
    @GetMapping("/profile")
    public ResponseEntity<ApiResponse<UserInfo>> getProfile(@RequestHeader("X-User-Email") String userEmail) {
        try {
            User user = userService.findByEmail(userEmail)
                    .orElseThrow(() -> new RuntimeException("User not found"));

            UserInfo userInfo = new UserInfo(user);

            return ResponseEntity.ok(ApiResponse.success("Profile retrieved successfully", userInfo));

        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Failed to retrieve profile: " + e.getMessage()));
        }
    }

    /**
     * Update user profile
     * 
     * @param userEmail extracted from JWT by gateway
     * @param updateRequest updated profile information
     * @return updated user profile
     */
    @PutMapping("/profile")
    public ResponseEntity<ApiResponse<UserInfo>> updateProfile(
            @RequestHeader("X-User-Email") String userEmail,
            @Valid @RequestBody ProfileUpdateRequest updateRequest) {
        try {
            User user = userService.findByEmail(userEmail)
                    .orElseThrow(() -> new RuntimeException("User not found"));

            // Update user information
            user.setFirstName(updateRequest.getFirstName());
            user.setLastName(updateRequest.getLastName());
            
            // Note: Email changes might require additional verification in production
            if (!updateRequest.getEmail().equals(user.getEmail())) {
                if (userService.emailExists(updateRequest.getEmail())) {
                    return ResponseEntity.badRequest()
                            .body(ApiResponse.error("Email is already taken"));
                }
                user.setEmail(updateRequest.getEmail());
            }

            UserInfo userInfo = new UserInfo(user);

            return ResponseEntity.ok(ApiResponse.success("Profile updated successfully", userInfo));

        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Failed to update profile: " + e.getMessage()));
        }
    }

    /**
     * Change password
     * 
     * @param userEmail extracted from JWT by gateway
     * @param request password change details
     * @return success response
     */
    @PutMapping("/change-password")
    public ResponseEntity<ApiResponse<String>> changePassword(
            @RequestHeader("X-User-Email") String userEmail,
            @Valid @RequestBody PasswordChangeRequest request) {
        try {
            // Validate new password confirmation
            if (!request.isNewPasswordMatching()) {
                return ResponseEntity.badRequest()
                        .body(ApiResponse.error("New passwords do not match"));
            }

            User user = userService.findByEmail(userEmail)
                    .orElseThrow(() -> new RuntimeException("User not found"));

            // Update password
            userService.updatePassword(user.getId(), request.getCurrentPassword(), request.getNewPassword());

            return ResponseEntity.ok(ApiResponse.success("Password changed successfully"));

        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Failed to change password: " + e.getMessage()));
        }
    }

    /**
     * Validate token (used by other services)
     * 
     * @param token JWT token to validate
     * @return validation result
     */
    @PostMapping("/validate")
    public ResponseEntity<ApiResponse<UserInfo>> validateToken(@RequestParam String token) {
        try {
            if (jwtService.isTokenExpired(token)) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(ApiResponse.error("Token is expired"));
            }

            String userEmail = jwtService.extractUsername(token);
            User user = userService.findByEmail(userEmail)
                    .orElseThrow(() -> new RuntimeException("User not found"));

            if (!jwtService.isTokenValid(token, user)) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(ApiResponse.error("Token is invalid"));
            }

            UserInfo userInfo = new UserInfo(user);

            return ResponseEntity.ok(ApiResponse.success("Token is valid", userInfo));

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error("Token validation failed: " + e.getMessage()));
        }
    }

    /**
     * Health check endpoint
     * 
     * @return service status
     */
    @GetMapping("/health")
    public ResponseEntity<ApiResponse<String>> health() {
        return ResponseEntity.ok(ApiResponse.success("Auth service is running"));
    }

}