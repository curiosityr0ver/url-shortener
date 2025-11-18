package curiosityrover.ishumehta.urlshortener.web.dto;

import java.time.Instant;

import curiosityrover.ishumehta.urlshortener.model.ShortUrl;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Response containing short URL details")
public record ShortUrlResponse(
	@Schema(description = "Unique identifier of the short URL", example = "1")
	Long id,
	@Schema(description = "The slug identifier", example = "abc12345")
	String slug,
	@Schema(description = "The original destination URL", example = "https://www.example.com/very/long/url/path")
	String destinationUrl,
	@Schema(description = "The complete short URL", example = "http://localhost:8080/abc12345")
	String shortUrl,
	@Schema(description = "Number of times the short URL has been accessed", example = "42")
	long hitCount,
	@Schema(description = "Timestamp when the short URL was created", example = "2024-01-15T10:30:00Z")
	Instant createdAt,
	@Schema(description = "Timestamp of the last access", example = "2024-01-20T14:22:00Z")
	Instant lastAccessedAt,
	@Schema(description = "Expiration timestamp (null if no expiration)", example = "2025-12-31T23:59:59Z")
	Instant expiresAt
) {

	public static ShortUrlResponse from(ShortUrl entity, String publicShortUrl) {
		return new ShortUrlResponse(
			entity.getId(),
			entity.getSlug(),
			entity.getDestinationUrl(),
			publicShortUrl,
			entity.getHitCount(),
			entity.getCreatedAt(),
			entity.getLastAccessedAt(),
			entity.getExpiresAt()
		);
	}
}

