package curiosityrover.ishumehta.urlshortener.web.dto;

import java.time.Instant;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record CreateShortUrlRequest(

	@NotBlank(message = "destinationUrl is required")
	@Size(max = 2048, message = "destinationUrl is too long")
	String destinationUrl,

	@Size(min = 3, max = 64, message = "customSlug must be 3-64 characters")
	@Pattern(regexp = "^[A-Za-z0-9_-]+$", message = "customSlug may only contain letters, numbers, '-' or '_'")
	String customSlug,

	@Future(message = "expiresAt must be in the future")
	Instant expiresAt
) {
}

