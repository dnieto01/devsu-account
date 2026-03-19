package com.ds.devsuaccount.infraestructure.config;

import jakarta.annotation.PostConstruct;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

@Configuration
public class DebugConfig {

    private final Environment env;

    public DebugConfig(Environment env) {
        this.env = env;
    }

    @PostConstruct
    public void printProfiles() {
        System.out.println("ACTIVE PROFILES:");
        for (String p : env.getActiveProfiles()) {
            System.out.println("-> " + p);
        }
    }
}