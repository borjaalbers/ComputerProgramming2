package com.gymflow.config;

import java.io.File;
import java.util.Properties;

/**
 * Central place to load database credentials.
 */
public final class DatabaseConfig {
    private DatabaseConfig() {}

    public static Properties load() {
        Properties props = new Properties();
        
        // Use file-based H2 database for persistence across application restarts
        // Database file will be stored in ./data/gymflow.mv.db
        String dbPath = "./data/gymflow";
        
        // Ensure data directory exists
        File dataDir = new File("./data");
        if (!dataDir.exists()) {
            dataDir.mkdirs();
            System.out.println("Created data directory: " + dataDir.getAbsolutePath());
        }
        
        // File-based H2 database URL - data persists on disk
        String defaultUrl = "jdbc:h2:file:" + dbPath + ";AUTO_SERVER=TRUE;MODE=MySQL";
        props.setProperty("url", System.getenv().getOrDefault("GYMFLOW_DB_URL", defaultUrl));
        props.setProperty("username", System.getenv().getOrDefault("GYMFLOW_DB_USER", "sa"));
        props.setProperty("password", System.getenv().getOrDefault("GYMFLOW_DB_PASSWORD", ""));
        
        System.out.println("Database location: " + new File(dbPath + ".mv.db").getAbsolutePath());
        return props;
    }
}
