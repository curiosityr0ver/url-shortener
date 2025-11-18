package curiosityrover.ishumehta.urlshortener.service;

import java.net.URI;
import java.net.URISyntaxException;
import java.time.Instant;
import java.util.Objects;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import curiosityrover.ishumehta.urlshortener.exception.ShortUrlExpiredException;
import curiosityrover.ishumehta.urlshortener.exception.ShortUrlNotFoundException;
import curiosityrover.ishumehta.urlshortener.exception.SlugAlreadyExistsException;
import curiosityrover.ishumehta.urlshortener.model.ShortUrl;
import curiosityrover.ishumehta.urlshortener.repository.ShortUrlRepository;

@Service
public class ShortUrlService {

	private static final Logger log = LoggerFactory.getLogger(ShortUrlService.class);

	private final ShortUrlRepository repository;
	private final SlugGenerator slugGenerator;

	@Value("${app.shortener.base-url:http://localhost:8080}")
	private String baseUrl;

	@Value("${app.shortener.slug-length:8}")
	private int slugLength;

	public ShortUrlService(ShortUrlRepository repository, SlugGenerator slugGenerator) {
		this.repository = repository;
		this.slugGenerator = slugGenerator;
	}

	@SuppressWarnings("null")
	@Transactional
	public ShortUrl createShortUrl(String destinationUrl, String customSlug, Instant expiresAt) {
		String normalizedUrl = normalizeDestinationUrl(destinationUrl);
		String slug = determineSlug(customSlug);
		Instant normalizedExpiry = normalizeExpiry(expiresAt);

		if (repository.existsBySlug(slug)) {
			throw new SlugAlreadyExistsException(slug);
		}

		ShortUrl shortUrl = ShortUrl.builder()
			.slug(slug)
			.destinationUrl(normalizedUrl)
			.expiresAt(normalizedExpiry)
			.build();

		try {
			ShortUrl persisted = repository.save(shortUrl);
			log.info(
				"Created short URL slug='{}' shortUrl='{}' destination='{}' expiresAt={}",
				persisted.getSlug(),
				buildPublicShortUrl(persisted.getSlug()),
				persisted.getDestinationUrl(),
				normalizedExpiry != null ? normalizedExpiry : "never"
			);
			return Objects.requireNonNull(persisted, "Short URL could not be persisted");
		}
		catch (DataIntegrityViolationException e) {
			throw new SlugAlreadyExistsException(slug);
		}
	}

	@Transactional(readOnly = true)
	public ShortUrl getShortUrl(String slug) {
		return repository.findBySlug(slug)
			.orElseThrow(() -> new ShortUrlNotFoundException(slug));
	}

	@Transactional
	public ShortUrl registerHit(String slug) {
		ShortUrl shortUrl = getShortUrl(slug);
		ensureNotExpired(shortUrl);
		shortUrl.setHitCount(shortUrl.getHitCount() + 1);
		shortUrl.setLastAccessedAt(Instant.now());
		ShortUrl updated = repository.save(shortUrl);
		log.info(
			"Hit short URL slug='{}' destination='{}' totalHits={}",
			updated.getSlug(),
			updated.getDestinationUrl(),
			updated.getHitCount()
		);
		return updated;
	}

	public String buildPublicShortUrl(String slug) {
		if (!StringUtils.hasText(baseUrl)) {
			return slug;
		}
		String trimmedBase = baseUrl.endsWith("/") ? baseUrl.substring(0, baseUrl.length() - 1) : baseUrl;
		return trimmedBase + "/" + slug;
	}

	private String determineSlug(String requestedSlug) {
		if (StringUtils.hasText(requestedSlug)) {
			String sanitized = sanitizeSlug(requestedSlug);
			if (sanitized.length() < 3) {
				throw new IllegalArgumentException("Custom slug must be at least 3 characters");
			}
			return sanitized;
		}

		int effectiveLength = slugLength > 0 ? slugLength : 8;
		String generatedSlug;
		do {
			generatedSlug = slugGenerator.generateSlug(effectiveLength);
		}
		while (repository.existsBySlug(generatedSlug));
		return generatedSlug;
	}

	private String sanitizeSlug(String slug) {
		String trimmed = slug.trim();
		if (!trimmed.matches("^[A-Za-z0-9_-]+$")) {
			throw new IllegalArgumentException("Slug may only contain letters, numbers, '-' or '_'");
		}
		return trimmed;
	}

	private String normalizeDestinationUrl(String destinationUrl) {
		if (!StringUtils.hasText(destinationUrl)) {
			throw new IllegalArgumentException("Destination URL is required");
		}

		String trimmed = destinationUrl.trim();
		try {
			URI uri = new URI(trimmed);
			String scheme = uri.getScheme();
			if (scheme == null || (!scheme.equalsIgnoreCase("http") && !scheme.equalsIgnoreCase("https"))) {
				throw new IllegalArgumentException("Destination URL must start with http or https");
			}
			if (!StringUtils.hasText(uri.getHost())) {
				throw new IllegalArgumentException("Destination URL must include a valid host");
			}
			return uri.toString();
		}
		catch (URISyntaxException e) {
			throw new IllegalArgumentException("Destination URL is invalid");
		}
	}

	private Instant normalizeExpiry(Instant expiresAt) {
		if (expiresAt == null) {
			return null;
		}
		Instant now = Instant.now();
		if (!expiresAt.isAfter(now)) {
			throw new IllegalArgumentException("expiresAt must be a future instant");
		}
		return expiresAt;
	}

	private void ensureNotExpired(ShortUrl shortUrl) {
		if (shortUrl.isExpired()) {
			throw new ShortUrlExpiredException(shortUrl.getSlug());
		}
	}
}

