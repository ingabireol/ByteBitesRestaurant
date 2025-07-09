package olim.com.authservice.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;
import olim.com.authservice.entity.User;

public class AuthDto {

    /**
     * Login request DTO
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class LoginRequest {
        @NotBlank(message = "Email is required")
        @Email(message = "Email should be valid")
        private String email;
        @NotBlank(message = "Password is required")
        private String password;
    }

    /**
     * Registration request DTO
     */
    @Getter
    @Setter
    @AllArgsConstructor
    @NoArgsConstructor
    public static class RegisterRequest {
        @NotBlank(message = "Email is required")
        @Email(message = "Email should be valid")
        private String email;

        @NotBlank(message = "First name is required")
        @Size(min = 2, max = 50, message = "First name must be between 2 and 50 characters")
        private String firstName;

        @NotBlank(message = "Last name is required")
        @Size(min = 2, max = 50, message = "Last name must be between 2 and 50 characters")
        private String lastName;

        @NotBlank(message = "Password is required")
        @Size(min = 6, message = "Password must be at least 6 characters")
        private String password;

        @NotBlank(message = "Confirm password is required")
        private String confirmPassword;

        private User.Role role = User.Role.ROLE_CUSTOMER; // Default role

        public RegisterRequest(String email, String firstName, String lastName, String password, String confirmPassword) {
            this.email = email;
            this.firstName = firstName;
            this.lastName = lastName;
            this.password = password;
            this.confirmPassword = confirmPassword;
        }
        // Validation method
        public boolean isPasswordMatching() {
            return password != null && password.equals(confirmPassword);
        }
    }

    /**
     * Authentication response DTO
     */
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AuthResponse {
        private String token;
        private String refreshToken;
        private String type = "Bearer";
        private UserInfo user;

        public AuthResponse(String token, String refreshToken, UserInfo user) {
            this.token = token;
            this.refreshToken = refreshToken;
            this.user = user;
        }
    }

    /**
     * User information DTO (for responses)
     */
    @Getter
    @Setter
    @AllArgsConstructor
    @NoArgsConstructor
    public static class UserInfo {
        private Long id;
        private String email;
        private String firstName;
        private String lastName;
        private User.Role role;
        private String provider;
        private boolean enabled;
        public UserInfo(User user) {
            this.id = user.getId();
            this.email = user.getEmail();
            this.firstName = user.getFirstName();
            this.lastName = user.getLastName();
            this.role = user.getRole();
            this.provider = user.getProvider();
            this.enabled = user.isEnabled();
        }

        // Utility methods
        public String getFullName() {
            return firstName + " " + lastName;
        }

        public boolean isOAuth2User() {
            return provider != null && !provider.equals("local");
        }
    }

   @Getter
   @Setter
   @NoArgsConstructor
   @AllArgsConstructor
    public static class PasswordChangeRequest {
        @NotBlank(message = "Current password is required")
        private String currentPassword;

        @NotBlank(message = "New password is required")
        @Size(min = 6, message = "New password must be at least 6 characters")
        private String newPassword;

        @NotBlank(message = "Confirm new password is required")
        private String confirmNewPassword;

        // Validation method
        public boolean isNewPasswordMatching() {
            return newPassword != null && newPassword.equals(confirmNewPassword);
        }
    }

    /**
     * Token refresh request DTO
     */
    @Getter
    @Setter
    @AllArgsConstructor
    @NoArgsConstructor
    public static class TokenRefreshRequest {
        @NotBlank(message = "Refresh token is required")
        private String refreshToken;

    }

    /**
     * Token refresh response DTO
     */
    @Getter
    @Setter
    @RequiredArgsConstructor
    @AllArgsConstructor
    public static class TokenRefreshResponse {
        private String token;
        private String refreshToken;
        private String type = "Bearer";
        public TokenRefreshResponse(String token, String refreshToken) {
            this.token = token;
            this.refreshToken = refreshToken;
        }


    }

    /**
     * Generic API response DTO
     */
    @Getter
    @Setter
    @RequiredArgsConstructor
    @AllArgsConstructor
    public static class ApiResponse<T> {
        private boolean success;
        private String message;
        private T data;

        public ApiResponse(boolean success, String message) {
            this.success = success;
            this.message = message;
        }
        // Static factory methods
        public static <T> ApiResponse<T> success(String message, T data) {
            return new ApiResponse<>(true, message, data);
        }

        public static <T> ApiResponse<T> success(String message) {
            return new ApiResponse<>(true, message);
        }

        public static <T> ApiResponse<T> error(String message) {
            return new ApiResponse<>(false, message);
        }

    }
}