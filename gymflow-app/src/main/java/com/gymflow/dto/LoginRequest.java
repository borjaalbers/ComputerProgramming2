package com.gymflow.dto;

/**
 * Simple DTO representing login credentials coming from the UI layer.
 */
public record LoginRequest(String username, String password) { }
