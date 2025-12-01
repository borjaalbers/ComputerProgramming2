package com.gymflow.config;

import java.util.Properties;

/**
 * Central place to load database credentials.
 */
public final class DatabaseConfig {
    private DatabaseConfig() {}

    public static Properties load() {
        Properties props = new Properties();
        // Default to H2 in-memory database with DB_CLOSE_DELAY=-1 to keep it in memory
        // even when all connections are closed
        String defaultUrl = "jdbc:h2:mem:gymflow;DB_CLOSE_DELAY=-1;MODE=MySQL";
        props.setProperty("url", System.getenv().getOrDefault("GYMFLOW_DB_URL", defaultUrl));
        props.setProperty("username", System.getenv().getOrDefault("GYMFLOW_DB_USER", "sa"));
        props.setProperty("password", System.getenv().getOrDefault("GYMFLOW_DB_PASSWORD", ""));
        return props;
    }
}
