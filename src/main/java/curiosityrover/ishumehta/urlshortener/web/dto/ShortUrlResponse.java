package curiosityrover.ishumehta.urlshortener.web.dto;

import java.time.Instant;

import curiosityrover.ishumehta.urlshortener.model.ShortUrl;

public record ShortUrlResponse(
	Long id,
	String slug,
	String destinationUrl,
	String shortUrl,
	long hitCount,
	Instant createdAt,
	Instant lastAccessedAt
) {

	public static ShortUrlResponse from(ShortUrl entity, String publicShortUrl) {
		return new ShortUrlResponse(
			entity.getId(),
			entity.getSlug(),
			entity.getDestinationUrl(),
			publicShortUrl,
			entity.getHitCount(),
			entity.getCreatedAt(),
			entity.getLastAccessedAt()
		);
	}
}

