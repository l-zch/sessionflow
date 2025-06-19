package com.sessionflow;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class SessionFlowApplication {

    public static void main(String[] args) {
		// This check is now handled by the run.sh script for better control
		// checkIfAlreadyRunning(); 

		SpringApplication app = new SpringApplication(SessionFlowApplication.class);
		app.addListeners((ApplicationListener<ApplicationReadyEvent>) event -> {
			WebServerApplicationContext context = (WebServerApplicationContext) event.getApplicationContext();
			int port = context.getWebServer().getPort();
			createPidFile(port);
			printWelcomeMessage(port);
		});
		
		String portStr = System.getProperty("server.port");
		int port = findAvailablePort(portStr != null ? Integer.parseInt(portStr) : 8080);
		System.setProperty("server.port", String.valueOf(port));
		
		app.run(args);
		// The started message is now handled by the run.sh script or the app itself
	}

	private static void printWelcomeMessage(int port) {
		String border = "========================================================================";
		String green = "\033[0;32m";
		String yellow = "\033[1;33m";
		String nc = "\033[0m"; // No Color

		System.out.println("\n" + green + border + nc);
		System.out.println(green + "  ✅ SessionFlow is running successfully!" + nc);
		System.out.println(green + border + nc);
		System.out.println("  Access URLs:");
		System.out.println("  " + yellow + "API Docs:      " + nc + "http://localhost:" + port + "/swagger-ui.html");
		System.out.println("  " + yellow + "H2 Console:    " + nc + "http://localhost:" + port + "/h2-console");
		System.out.println("  " + yellow + "WebSocket URL: " + nc + "ws://localhost:" + port + "/ws");
		System.out.println(green + border + nc + "\n");
	}

	private static void createPidFile(int port) {
		try {
			String pid = ManagementFactory.getRuntimeMXBean().getName().split("@")[0];
			String content = pid + ":" + port;
			Files.write(PID_FILE, content.getBytes());
			System.out.println("ℹ️  Created PID file at: " + PID_FILE.toAbsolutePath());

			// Add a shutdown hook to delete the file on exit
			Runtime.getRuntime().addShutdownHook(new Thread(() -> {
				try {
					Files.deleteIfExists(PID_FILE);
					System.out.println("\nℹ️  SessionFlow shutting down. PID file deleted.");
				} catch (IOException e) {
					System.err.println("Error deleting PID file: " + e.getMessage());
				}
			}));
		} catch (IOException e) {
			System.err.println("Could not create PID file: " + e.getMessage());
		}
	}

	private static int findAvailablePort(int startingPort) {
		int port = startingPort;
		int originalPort = port;

		while (!isPortAvailable(port)) {
			System.out.println("⚠️  Port " + port + " is in use. Trying next available port...");
			port++;
			if (port > 65535) {
				throw new IllegalStateException("No available ports found above " + originalPort);
			}
		}

		if (port != originalPort) {
			System.out.println("ℹ️  Requested port " + originalPort + " was occupied. Switched to available port " + port + ".");
		}
		return port;
	}

	private static boolean isPortAvailable(int port) {
		if (port < 1024 || port > 65535) {
			throw new IllegalArgumentException("Invalid start port: " + port);
		}
		try (ServerSocket serverSocket = new ServerSocket(port)) {
			serverSocket.setReuseAddress(true);
			return true;
		} catch (IOException e) {
			return false;
		}
    }
} 