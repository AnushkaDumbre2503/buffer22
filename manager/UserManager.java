package manager;

import model.User;
import repository.UserRepository;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

public class UserManager {
    private UserRepository userRepository;
    
    public UserManager() {
        this.userRepository = new UserRepository();
    }
    
    public User createUser(String username, String email) throws SQLException {
        // Check if user already exists
        if (userRepository.existsByUsername(username)) {
            throw new IllegalArgumentException("Username already exists: " + username);
        }
        
        if (userRepository.existsByEmail(email)) {
            throw new IllegalArgumentException("Email already exists: " + email);
        }
        
        User user = new User(username, email);
        return userRepository.create(user);
    }
    
    public Optional<User> getUserById(int id) throws SQLException {
        return userRepository.findById(id);
    }
    
    public Optional<User> getUserByUsername(String username) throws SQLException {
        return userRepository.findByUsername(username);
    }
    
    public Optional<User> getUserByEmail(String email) throws SQLException {
        return userRepository.findByEmail(email);
    }
    
    public List<User> getAllUsers() throws SQLException {
        return userRepository.findAll();
    }
    
    public boolean updateUser(User user) throws SQLException {
        return userRepository.update(user);
    }
    
    public boolean updateUserLogin(int userId) throws SQLException {
        Optional<User> userOpt = userRepository.findById(userId);
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            user.setLastLogin(java.time.LocalDateTime.now());
            return userRepository.update(user);
        }
        return false;
    }
    
    public boolean deleteUser(int id) throws SQLException {
        return userRepository.delete(id);
    }
    
    public User authenticateUser(String username, String email) throws SQLException {
        // Simple authentication - in real system, this would involve passwords
        Optional<User> userOpt = userRepository.findByUsername(username);
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            if (user.getEmail().equals(email)) {
                // Update last login
                user.setLastLogin(java.time.LocalDateTime.now());
                userRepository.update(user);
                return user;
            }
        }
        return null;
    }
    
    public int getTotalUsers() throws SQLException {
        return userRepository.findAll().size();
    }
    
    public int getActiveUsersInLastHours(int hours) throws SQLException {
        // This would require additional query in UserRepository
        // For now, return total users
        return getTotalUsers();
    }
}
