package com.sessionflow.runner;

import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;

import java.io.IOException;

/**
 * Handles checking for the presence of Web App assets and notifying users if they are missing.
 */
public class WebAppAssetsChecker {

    private static final String WEB_APP_RESOURCE_PATTERN = "classpath*:static/sessionflowapp/*";

    /**
     * Checks if Web App assets exist and prints a message if they are missing.
     * 
     * @return true if Web App assets exist, false otherwise
     */
    public static boolean checkAndNotify() {
        boolean assetsExist = checkWebAppAssets();
        
        if (!assetsExist) {
            ConsoleMessagePrinter.printWebAppAssetsMissingMessage();
        }
        
        return assetsExist;
    }

    /**
     * Checks if Web App assets exist without printing any messages.
     * <p>
     * This method is designed to work both when running from an IDE and from a packaged JAR file.
     * It checks for the presence of any files within the {@code static/sessionflowapp} directory on the classpath.
     * 
     * @return true if Web App assets exist, false otherwise
     */
    public static boolean checkWebAppAssets() {
        try {
            ResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
            Resource[] resources = resolver.getResources(WEB_APP_RESOURCE_PATTERN);
            return resources.length > 0;
        } catch (IOException e) {
            // If an IOException occurs (e.g., directory not found), assume assets are missing.
            return false;
        }
    }
} 