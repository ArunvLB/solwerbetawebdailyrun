package com.slower.framework.utils;

public record FormData(
        String fullName,
        String email,
        String phone,
        String company,
        String message,
        String preferredDemoDateTime
) {
    public static FormData fromConfig() {
        return new FormData(
                ConfigReader.getOrDefault("testdata.fullName", "Test User"),
                ConfigReader.getOrDefault("testdata.email", "test.user@example.com"),
                ConfigReader.getOrDefault("testdata.phone", "9999999999"),
                ConfigReader.getOrDefault("testdata.company", "Solwer QA"),
                ConfigReader.getOrDefault("testdata.message", "Automated test submission"),
                ConfigReader.getOrDefault("testdata.preferredDemoDateTime", DateTimeDefaults.defaultPreferredDemoDateTime())
        );
    }
}

