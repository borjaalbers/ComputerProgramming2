package com.gymflow.config;

import java.util.Properties;

/**
 * Central place to load database credentials.
 */
public final class DatabaseConfig {
    private DatabaseConfig() {}

    public static Properties load() {
        Properties props = new Properties();
        props.setProperty("url", System.getenv().getOrDefault("GYMFLOW_DB_URL", "jdbc:h2:mem:gymflow"));
        props.setProperty("username", System.getenv().getOrDefault("GYMFLOW_DB_USER", "sa"));
        props.setProperty("password", System.getenv().getOrDefault("GYMFLOW_DB_PASSWORD", ""));
        return props;
    }
}
