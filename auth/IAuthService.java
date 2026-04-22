package auth;

import java.sql.SQLException;

public interface IAuthService {
    AuthContext login(String username, String password) throws SQLException;
    boolean signup(String username, String email, String password) throws SQLException;
    boolean validateSession(String sessionToken) throws SQLException;
}
