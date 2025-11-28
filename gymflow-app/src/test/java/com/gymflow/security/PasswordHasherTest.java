package com.gymflow.security;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class PasswordHasherTest {

    @Test
    void hashesPasswordDeterministically() {
        String first = PasswordHasher.sha256("secret");
        String second = PasswordHasher.sha256("secret");
        assertEquals(first, second);
        assertEquals(64, first.length());
    }
}
