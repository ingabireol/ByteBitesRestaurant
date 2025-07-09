package olim.com.authservice.service;

import olim.com.authservice.entity.User;
import olim.com.authservice.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class UserService implements UserDetailsService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    /**
     * Load user by username (email) for Spring Security
     * 
     * @param username user's email
     * @return UserDetails for authentication
     * @throws UsernameNotFoundException if user not found
     */
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return userRepository.findByEmail(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + username));
    }

    public User registerUser(String email, String firstName, String lastName, String password, User.Role role) {
        // Check if user already exists
        if (userRepository.existsByEmail(email)) {
            throw new RuntimeException("User already exists with email: " + email);
        }

        // Create new user
        User user = new User();
        user.setEmail(email);
        user.setFirstName(firstName);
        user.setLastName(lastName);
        user.setPassword(passwordEncoder.encode(password)); // Hash password
        user.setRole(role);
        user.setProvider("local"); // Local registration

        return userRepository.save(user);
    }


    public User registerCustomer(String email, String firstName, String lastName, String password) {
        return registerUser(email, firstName, lastName, password, User.Role.ROLE_CUSTOMER);
    }
    public User registerRestaurantOwner(String email, String firstName, String lastName, String password) {
        return registerUser(email, firstName, lastName, password, User.Role.ROLE_RESTAURANT_OWNER);
    }

    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    public Optional<User> findById(Long id) {
        return userRepository.findById(id);
    }

    public User authenticateUser(String email, String password) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Invalid email or password"));

        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new RuntimeException("Invalid email or password");
        }

        if (!user.isEnabled()) {
            throw new RuntimeException("Account is disabled");
        }

        return user;
    }

    public User createOrUpdateOAuth2User(String email, String firstName, String lastName, 
                                       String provider, String providerId) {
        // Check if user exists by provider ID
        Optional<User> existingUser = userRepository.findByProviderAndProviderId(provider, providerId);
        
        if (existingUser.isPresent()) {
            // Update existing OAuth2 user
            User user = existingUser.get();
            user.setEmail(email);
            user.setFirstName(firstName);
            user.setLastName(lastName);
            return userRepository.save(user);
        }
        
        // Check if user exists by email (might be a local user wanting to link OAuth2)
        Optional<User> userByEmail = userRepository.findByEmail(email);
        if (userByEmail.isPresent()) {
            // Link OAuth2 to existing local account
            User user = userByEmail.get();
            user.setProvider(provider);
            user.setProviderId(providerId);
            return userRepository.save(user);
        }
        
        // Create new OAuth2 user
        User newUser = new User();
        newUser.setEmail(email);
        newUser.setFirstName(firstName);
        newUser.setLastName(lastName);
        newUser.setPassword(passwordEncoder.encode("oauth2-no-password")); // Placeholder password
        newUser.setRole(User.Role.ROLE_CUSTOMER); // Default role for OAuth2 users
        newUser.setProvider(provider);
        newUser.setProviderId(providerId);
        
        return userRepository.save(newUser);
    }

    public void updatePassword(Long userId, String oldPassword, String newPassword) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
            throw new RuntimeException("Current password is incorrect");
        }

        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
    }

    public void updateUserRole(Long userId, User.Role newRole) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        user.setRole(newRole);
        userRepository.save(user);
    }

    public void setUserEnabled(Long userId, boolean enabled) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        user.setEnabled(enabled);
        userRepository.save(user);
    }

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }


    public List<User> getUsersByRole(User.Role role) {
        return userRepository.findByRole(role);
    }

    public List<User> searchUsersByEmail(String email) {
        return userRepository.findByEmailContainingIgnoreCase(email);
    }

    public List<User> searchUsersByName(String name) {
        return userRepository.findByNameContainingIgnoreCase(name);
    }


    public UserStats getUserStats() {
        long totalUsers = userRepository.count();
        long customers = userRepository.countByRole(User.Role.ROLE_CUSTOMER);
        long restaurantOwners = userRepository.countByRole(User.Role.ROLE_RESTAURANT_OWNER);
        long admins = userRepository.countByRole(User.Role.ROLE_ADMIN);
        long oauth2Users = userRepository.findAllOAuth2Users().size();
        long localUsers = userRepository.findAllLocalUsers().size();
        
        return new UserStats(totalUsers, customers, restaurantOwners, admins, oauth2Users, localUsers);
    }


    public void deleteUser(Long userId) {
        if (!userRepository.existsById(userId)) {
            throw new RuntimeException("User not found");
        }
        userRepository.deleteById(userId);
    }

    public boolean emailExists(String email) {
        return userRepository.existsByEmail(email);
    }

    /**
     * User statistics data class
     */
    public static class UserStats {
        private final long totalUsers;
        private final long customers;
        private final long restaurantOwners;
        private final long admins;
        private final long oauth2Users;
        private final long localUsers;

        public UserStats(long totalUsers, long customers, long restaurantOwners,
                        long admins, long oauth2Users, long localUsers) {
            this.totalUsers = totalUsers;
            this.customers = customers;
            this.restaurantOwners = restaurantOwners;
            this.admins = admins;
            this.oauth2Users = oauth2Users;
            this.localUsers = localUsers;
        }

        // Getters
        public long getTotalUsers() { return totalUsers; }
        public long getCustomers() { return customers; }
        public long getRestaurantOwners() { return restaurantOwners; }
        public long getAdmins() { return admins; }
        public long getOauth2Users() { return oauth2Users; }
        public long getLocalUsers() { return localUsers; }
    }
}