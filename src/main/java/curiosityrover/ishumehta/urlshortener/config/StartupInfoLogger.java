package curiosityrover.ishumehta.urlshortener.config;

import java.net.URI;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.core.env.Environment;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Component
public class StartupInfoLogger implements ApplicationListener<ApplicationReadyEvent> {

	private static final Logger log = LoggerFactory.getLogger(StartupInfoLogger.class);

	private final Environment environment;
	private final DataSourceProperties dataSourceProperties;

	public StartupInfoLogger(Environment environment, DataSourceProperties dataSourceProperties) {
		this.environment = environment;
		this.dataSourceProperties = dataSourceProperties;
	}

	@Override
	public void onApplicationEvent(@NonNull ApplicationReadyEvent event) {
		if (!log.isInfoEnabled()) {
			return;
		}
		log.info(buildStartupSummary());
	}

	private String buildStartupSummary() {
		String activeProfiles = Optional.ofNullable(environment.getProperty("spring.profiles.active"))
			.filter(StringUtils::hasText)
			.orElse("default");
		String port = environment.getProperty("local.server.port",
			environment.getProperty("server.port", "8080"));
		String configuredBaseUrl = environment.getProperty("app.shortener.base-url");
		String baseUrl = StringUtils.hasText(configuredBaseUrl)
			? trimTrailingSlash(configuredBaseUrl)
			: "http://localhost:" + port;
		String restEndpoint = baseUrl + "/api/urls";
		String swaggerUrl = baseUrl + ensureLeadingSlash(environment.getProperty("springdoc.swagger-ui.path",
			"/swagger-ui.html"));
		String apiDocsUrl = baseUrl + ensureLeadingSlash(environment.getProperty("springdoc.api-docs.path",
			"/v3/api-docs"));
		String slugLength = environment.getProperty("app.shortener.slug-length", "8");

		String jdbcUrl = Optional.ofNullable(dataSourceProperties.getUrl())
			.filter(StringUtils::hasText)
			.orElse(environment.getProperty("spring.datasource.url",
				"jdbc:postgresql://localhost:5432/url_shortener"));
		String dbUser = Optional.ofNullable(dataSourceProperties.getUsername())
			.filter(StringUtils::hasText)
			.orElse(environment.getProperty("spring.datasource.username", "url_shortener_app"));
		DatabaseDescriptor descriptor = describeDatabase(jdbcUrl);

		return """

================================================================================
 URL Shortener is ready [OK]
--------------------------------------------------------------------------------
 Environment : %s
 Port        : %s
 Base URL    : %s

 API Endpoints
   - REST (POST/GET) : %s
   - Swagger UI      : %s
   - OpenAPI JSON    : %s

 Database
   - JDBC URL        : %s
   - Host / Port     : %s:%s
   - Name            : %s
   - User            : %s

 Defaults
   - Slug length     : %s characters

 Tip: To create a link quickly, run:
   curl -X POST %s \\
     -H "Content-Type: application/json" \\
     -d "{\\"destinationUrl\\":\\"https://spring.io/projects\\"}"
================================================================================
""".formatted(
			activeProfiles,
			port,
			baseUrl,
			restEndpoint,
			swaggerUrl,
			apiDocsUrl,
			jdbcUrl,
			descriptor.host(),
			descriptor.port(),
			descriptor.database(),
			dbUser,
			slugLength,
			restEndpoint
		);
	}

	private static String trimTrailingSlash(String value) {
		return value != null && value.endsWith("/")
			? value.substring(0, value.length() - 1)
			: value;
	}

	private static String ensureLeadingSlash(String path) {
		if (!StringUtils.hasText(path)) {
			return "";
		}
		return path.startsWith("/") ? path : "/" + path;
	}

	private DatabaseDescriptor describeDatabase(String jdbcUrl) {
		if (!StringUtils.hasText(jdbcUrl)) {
			return new DatabaseDescriptor("n/a", "n/a", "n/a");
		}
		try {
			String sanitized = jdbcUrl.startsWith("jdbc:") ? jdbcUrl.substring(5) : jdbcUrl;
			URI uri = URI.create(sanitized);
			String host = Optional.ofNullable(uri.getHost()).orElse("localhost");
			String port = uri.getPort() == -1 ? "default" : Integer.toString(uri.getPort());
			String dbName = Optional.ofNullable(uri.getPath())
				.filter(StringUtils::hasText)
				.map(path -> path.startsWith("/") ? path.substring(1) : path)
				.orElse("default");
			return new DatabaseDescriptor(host, port, dbName);
		}
		catch (IllegalArgumentException ex) {
			return new DatabaseDescriptor("unresolved", "?", jdbcUrl);
		}
	}

	private record DatabaseDescriptor(String host, String port, String database) {
	}
}

