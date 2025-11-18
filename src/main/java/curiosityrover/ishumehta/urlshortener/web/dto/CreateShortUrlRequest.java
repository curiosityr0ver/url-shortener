package curiosityrover.ishumehta.urlshortener.web.dto;

import java.time.Instant;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

@Schema(description = "Request to create a new short URL")
public record CreateShortUrlRequest(

	@Schema(description = "The destination URL to shorten", example = "https://www.example.com/very/long/url/path", required = true)
	@NotBlank(message = "destinationUrl is required")
	@Size(max = 2048, message = "destinationUrl is too long")
	String destinationUrl,

	@Schema(description = "Optional custom slug (3-64 characters, alphanumeric, dash, or underscore)", example = "my-custom-slug")
	@Size(min = 3, max = 64, message = "customSlug must be 3-64 characters")
	@Pattern(regexp = "^[A-Za-z0-9_-]+$", message = "customSlug may only contain letters, numbers, '-' or '_'")
	String customSlug,

	@Schema(description = "Optional expiration date/time (must be in the future)", example = "2025-12-31T23:59:59Z")
	@Future(message = "expiresAt must be in the future")
	Instant expiresAt
) {
}

