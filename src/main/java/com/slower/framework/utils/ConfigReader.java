package com.slower.framework.utils;

import com.slower.framework.exceptions.FrameworkException;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Locale;
import java.util.Properties;

public final class ConfigReader {
    private static final String CONFIG_FILE = "config.properties";
    private static final String DEFAULT_BROWSER = "chrome";
    private static final Properties PROPERTIES = new Properties();

    static {
        try (InputStream is = ConfigReader.class.getClassLoader().getResourceAsStream(CONFIG_FILE)) {
            if (is == null) {
                throw new FrameworkException("Missing config file on classpath: " + CONFIG_FILE);
            }
            PROPERTIES.load(is);
        } catch (IOException e) {
            throw new FrameworkException("Failed to load: " + CONFIG_FILE, e);
        }
    }

    private ConfigReader() {
    }

    public static String get(String key) {
        String sys = System.getProperty(key);
        if (sys != null && !sys.isBlank()) {
            String trimmed = sys.trim();
            if (!isPlaceholder(trimmed)) {
                return trimmed;
            }
        }
        String env = System.getenv(toEnvKey(key));
        if (env != null && !env.isBlank()) {
            String trimmed = env.trim();
            if (!isPlaceholder(trimmed)) {
                return trimmed;
            }
        }
        String val = PROPERTIES.getProperty(key);
        if (val == null) {
            return null;
        }
        return val.trim();
    }

    public static String getOrDefault(String key, String defaultValue) {
        String val = get(key);
        return (val == null || val.isBlank()) ? defaultValue : val;
    }

    public static String getEnvironment() {
        return getOrDefault("env", "QA").trim().toUpperCase(Locale.ROOT);
    }

    public static String getEnvProperty(String propertyKey) {
        String env = getEnvironment();
        String envKey = env + "." + propertyKey;

        String envValue = get(envKey);
        if (envValue != null && !envValue.isBlank()) {
            return envValue.trim();
        }

        return get(propertyKey);
    }

    public static String getBrowser() {
        String browser = getOrDefault("browser", DEFAULT_BROWSER).trim().toLowerCase(Locale.ROOT);
        if ("chrome".equals(browser) || "edge".equals(browser)) {
            return browser;
        }
        return DEFAULT_BROWSER;
    }

    public static boolean isHeadless() {
        return Boolean.parseBoolean(getOrDefault("headless", "true"));
    }

    public static int getTimeoutSeconds() {
        return Integer.parseInt(getOrDefault("timeout.seconds", "15"));
    }

    public static int getPageLoadTimeoutSeconds() {
        return Integer.parseInt(getOrDefault("pageload.timeout.seconds", "30"));
    }

    public static int getRetryCount() {
        return Integer.parseInt(getOrDefault("retry.count", "1"));
    }

    public static boolean isFullPageScreenshotOnFailure() {
        return Boolean.parseBoolean(getOrDefault("screenshot.fullpage.onFailure", "false"));
    }

    public static boolean keepBrowserOpen() {
        return Boolean.parseBoolean(getOrDefault("keep.browser.open", "false"));
    }

    public static long getSlowMoMs() {
        return Long.parseLong(getOrDefault("slowmo.ms", "0"));
    }

    public static boolean isCi() {
        return Boolean.parseBoolean(getOrDefault("ci", "false"));
    }

    public static boolean isVideoRecordingEnabled() {
        String configured = get("video.recording.enabled");
        if (configured != null && !configured.isBlank()) {
            return Boolean.parseBoolean(configured);
        }
        return !isHeadless() && !isCi();
    }

    public static String getBrowserBinaryPath() {
        return get("browser.binary.path");
    }

    public static String getBaseUrl() {
        String url = getEnvProperty("url");
        if (url == null || url.isBlank()) {
            throw new FrameworkException("Missing base URL. Set env URL via '" + getEnvironment() + ".url' in config.properties");
        }
        return resolveUrl(url.trim());
    }

    public static String resolveUrl(String rawUrl) {
        if (rawUrl.startsWith("classpath:")) {
            String resourcePath = rawUrl.substring("classpath:".length());
            if (resourcePath.startsWith("/")) {
                resourcePath = resourcePath.substring(1);
            }
            URL resource = ConfigReader.class.getClassLoader().getResource(resourcePath);
            if (resource == null) {
                throw new FrameworkException("Classpath resource not found: " + resourcePath);
            }
            return resource.toExternalForm();
        }
        return rawUrl;
    }

    private static boolean isPlaceholder(String value) {
        if (value == null) {
            return false;
        }
        String v = value.trim();
        if (v.isEmpty()) {
            return false;
        }
        if (v.startsWith("$")) {
            return true;
        }
        // Common Maven/CI placeholders
        return v.startsWith("${") && v.endsWith("}");
    }

    private static String toEnvKey(String key) {
        return key
                .replace('.', '_')
                .replace('-', '_')
                .toUpperCase(Locale.ROOT);
    }
}
