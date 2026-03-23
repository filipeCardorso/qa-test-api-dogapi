package com.qatest.api.config;

import org.yaml.snakeyaml.Yaml;
import java.io.InputStream;
import java.util.Map;

public final class ApiConfig {

    private static ApiConfig instance;
    private final Map<String, Object> config;

    @SuppressWarnings("unchecked")
    private ApiConfig() {
        Yaml yaml = new Yaml();
        try (InputStream input = getClass().getClassLoader().getResourceAsStream("application.yml")) {
            Map<String, Object> root = yaml.load(input);
            this.config = (Map<String, Object>) root.get("api");
        } catch (Exception e) {
            throw new RuntimeException("Failed to load application.yml", e);
        }
    }

    public static synchronized ApiConfig getInstance() {
        if (instance == null) {
            instance = new ApiConfig();
        }
        return instance;
    }

    public String getBaseUrl() {
        return (String) config.get("base-url");
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> getTimeoutConfig() {
        return (Map<String, Object>) config.get("timeout");
    }

    public int getConnectionTimeout() {
        return (int) getTimeoutConfig().get("connection");
    }

    public int getResponseTimeout() {
        return (int) getTimeoutConfig().get("response");
    }
}
