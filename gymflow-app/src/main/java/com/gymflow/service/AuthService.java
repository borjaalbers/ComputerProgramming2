package com.gymflow.service;

import java.util.Optional;

/**
 * Defines authentication operations for the system.
 */
public interface AuthService {
    Optional<String> authenticate(String username, String password);
}
