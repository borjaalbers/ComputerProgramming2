package com.gymflow.dao;

import com.gymflow.model.User;

import java.util.Optional;

/**
 * Data access contract for user entities.
 */
public interface UserDao {
    Optional<User> findByUsername(String username);
}
