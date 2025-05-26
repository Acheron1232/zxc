package com.laba.ilaba.security;

import jakarta.enterprise.context.ApplicationScoped;
import org.mindrot.jbcrypt.BCrypt;

@ApplicationScoped
public class PasswordService {

    public String hashPassword(String password) {
        String salt = BCrypt.gensalt();
        return BCrypt.hashpw(password, salt);
    }

    public boolean checkPassword(String password, String storedHash) {
        return BCrypt.checkpw(password, storedHash);
    }
}
