package com.sessionflow;

import com.sessionflow.runner.StartupManager;
import org.springframework.boot.autoconfigure.SpringBootApplication;
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

	public static void main(String[] args) {
		StartupManager.run(SessionFlowApplication.class, args);
	}
} 