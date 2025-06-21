package com.sessionflow.runner;

import java.io.File;

/**
 * Handles checking for the presence of Web App assets and notifying users if they are missing.
 */
public class WebAppAssetsChecker {

    private static final String WEB_APP_DIR_PATH = "src/main/resources/static/sessionflowapp";

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
     * 
     * @return true if Web App assets exist, false otherwise
     */
    public static boolean checkWebAppAssets() {
        File webAppDir = new File(WEB_APP_DIR_PATH);
        return webAppDir.exists() && 
               webAppDir.isDirectory() && 
               webAppDir.list() != null && 
               webAppDir.list().length > 0;
    }
} 