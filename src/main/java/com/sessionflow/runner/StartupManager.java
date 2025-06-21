package com.sessionflow.runner;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.boot.web.context.WebServerApplicationContext;
import org.springframework.context.ApplicationListener;

import java.io.IOException;

/**
 * Main startup coordinator for the SessionFlow application.
 * Orchestrates the startup process by delegating to specialized components.
 */
public class StartupManager {

    public static void run(Class<?> primarySource, String[] args) {
        try {
            // 1. Process command-line arguments
            CommandLineProcessor.processArguments(args);

            // 2. Check for web app assets
            boolean webAppAssetsExist = WebAppAssetsChecker.checkAndNotify();

            // 3. Acquire application lock
            ApplicationLockManager lockManager = new ApplicationLockManager();
            if (!lockManager.acquireLock()) {
                System.exit(1);
                return;
            }

            // 4. Start Spring Boot application
            startSpringBootApplication(primarySource, args, webAppAssetsExist);

        } catch (IOException e) {
            System.err.println("❌ Could not initialize SessionFlow application.");
            e.printStackTrace();
            System.exit(1);
        }
    }

    /**
     * Starts the Spring Boot application with appropriate listeners.
     */
    private static void startSpringBootApplication(Class<?> primarySource, String[] args, boolean webAppAssetsExist) {
        SpringApplication app = new SpringApplication(primarySource);
        app.addListeners((ApplicationListener<ApplicationReadyEvent>) event -> {
            WebServerApplicationContext context = (WebServerApplicationContext) event.getApplicationContext();
            ConsoleMessagePrinter.printWelcomeMessage(context.getWebServer().getPort(), webAppAssetsExist);
        });

        app.run(args);
    }
} 