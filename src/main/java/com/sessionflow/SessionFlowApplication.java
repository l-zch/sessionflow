package com.sessionflow;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.boot.web.context.WebServerApplicationContext;
import org.springframework.context.ApplicationListener;

import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.net.ServerSocket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.springframework.boot.autoconfigure.jmx.JmxAutoConfiguration;
import org.springframework.boot.autoconfigure.mail.MailSenderAutoConfiguration;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.actuate.autoconfigure.metrics.MetricsAutoConfiguration;
import org.springframework.boot.actuate.autoconfigure.metrics.CompositeMeterRegistryAutoConfiguration;
import org.springframework.boot.actuate.autoconfigure.metrics.export.simple.SimpleMetricsExportAutoConfiguration;
import org.springframework.boot.actuate.autoconfigure.metrics.jdbc.DataSourcePoolMetricsAutoConfiguration;
import org.springframework.boot.actuate.autoconfigure.metrics.JvmMetricsAutoConfiguration;
import org.springframework.boot.actuate.autoconfigure.metrics.LogbackMetricsAutoConfiguration;
import org.springframework.boot.actuate.autoconfigure.metrics.SystemMetricsAutoConfiguration;
import org.springframework.boot.actuate.autoconfigure.metrics.web.tomcat.TomcatMetricsAutoConfiguration;
import org.springframework.boot.actuate.autoconfigure.metrics.startup.StartupTimeMetricsListenerAutoConfiguration;
import org.springframework.boot.actuate.autoconfigure.info.InfoEndpointAutoConfiguration;
import org.springframework.boot.actuate.autoconfigure.health.HealthEndpointAutoConfiguration;

@SpringBootApplication(
	exclude = {
		MetricsAutoConfiguration.class,
		CompositeMeterRegistryAutoConfiguration.class,
		SimpleMetricsExportAutoConfiguration.class,
		DataSourcePoolMetricsAutoConfiguration.class,
		JvmMetricsAutoConfiguration.class,
		LogbackMetricsAutoConfiguration.class,
		SystemMetricsAutoConfiguration.class,
		TomcatMetricsAutoConfiguration.class,
		StartupTimeMetricsListenerAutoConfiguration.class,
		InfoEndpointAutoConfiguration.class,
		HealthEndpointAutoConfiguration.class,
		JmxAutoConfiguration.class,
		MailSenderAutoConfiguration.class,
		SecurityAutoConfiguration.class
	}
)
public class SessionFlowApplication {

	private static final Path PID_FILE = Paths.get(".sessionflow.run");

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