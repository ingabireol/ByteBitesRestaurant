package olim.com.authservice.repository;

import olim.com.authservice.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);
    boolean existsByEmail(String email);

    /**
     * Find user by OAuth2 provider and provider ID
     * Used for OAuth2 login (Google, GitHub)
     * 
     * @param provider OAuth2 provider name (google, github)
     * @param providerId unique ID from the provider
     * @return Optional containing user if found
     */
    Optional<User> findByProviderAndProviderId(String provider, String providerId);

    List<User> findByRole(User.Role role);

    /**
     * Find users by role and enabled status
     * 
     * @param role user role
     * @param enabled whether the account is enabled
     * @return list of users matching criteria
     */
    List<User> findByRoleAndEnabled(User.Role role, boolean enabled);


    long countByRole(User.Role role);

    /**
     * Find users whose email contains the search term (case-insensitive)
     * Useful for admin user search functionality
     * 
     * @param email search term
     * @return list of users whose email contains the search term
     */
    @Query("SELECT u FROM User u WHERE LOWER(u.email) LIKE LOWER(CONCAT('%', :email, '%'))")
    List<User> findByEmailContainingIgnoreCase(@Param("email") String email);

    @Query("SELECT u FROM User u WHERE LOWER(u.firstName) LIKE LOWER(CONCAT('%', :name, '%')) " +
           "OR LOWER(u.lastName) LIKE LOWER(CONCAT('%', :name, '%'))")
    List<User> findByNameContainingIgnoreCase(@Param("name") String name);

    /**
     * Find all OAuth2 users (users who signed up via social login)
     * 
     * @return list of OAuth2 users
     */
    @Query("SELECT u FROM User u WHERE u.provider IS NOT NULL AND u.provider != 'local'")
    List<User> findAllOAuth2Users();

    /**
     * Find all local users (users who signed up with email/password)
     * 
     * @return list of local users
     */
    @Query("SELECT u FROM User u WHERE u.provider IS NULL OR u.provider = 'local'")
    List<User> findAllLocalUsers();
}