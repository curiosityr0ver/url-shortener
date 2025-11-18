package curiosityrover.ishumehta.urlshortener.web;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import curiosityrover.ishumehta.urlshortener.model.ShortUrl;
import curiosityrover.ishumehta.urlshortener.service.ShortUrlService;
import curiosityrover.ishumehta.urlshortener.web.dto.CreateShortUrlRequest;
import curiosityrover.ishumehta.urlshortener.web.dto.ShortUrlResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/urls")
@Validated
@Tag(name = "Short URLs", description = "API for creating and managing short URLs")
public class ShortUrlController {

	private final ShortUrlService shortUrlService;

	public ShortUrlController(ShortUrlService shortUrlService) {
		this.shortUrlService = shortUrlService;
	}

	@Operation(
		summary = "Create a short URL",
		description = "Creates a new short URL from a destination URL. Optionally accepts a custom slug and expiration date."
	)
	@ApiResponses(value = {
		@ApiResponse(
			responseCode = "201",
			description = "Short URL created successfully",
			content = @Content(schema = @Schema(implementation = ShortUrlResponse.class))
		),
		@ApiResponse(
			responseCode = "400",
			description = "Invalid request data",
			content = @Content
		),
		@ApiResponse(
			responseCode = "409",
			description = "Custom slug already exists",
			content = @Content
		)
	})
	@PostMapping
	public ResponseEntity<ShortUrlResponse> create(@Valid @RequestBody CreateShortUrlRequest request) {
		ShortUrl created = shortUrlService.createShortUrl(request.destinationUrl(), request.customSlug(), request.expiresAt());
		return ResponseEntity.status(HttpStatus.CREATED)
			.body(ShortUrlResponse.from(created, shortUrlService.buildPublicShortUrl(created.getSlug())));
	}

	@Operation(
		summary = "Get short URL details",
		description = "Retrieves details of a short URL by its slug, including hit count and expiration information."
	)
	@ApiResponses(value = {
		@ApiResponse(
			responseCode = "200",
			description = "Short URL found",
			content = @Content(schema = @Schema(implementation = ShortUrlResponse.class))
		),
		@ApiResponse(
			responseCode = "404",
			description = "Short URL not found",
			content = @Content
		),
		@ApiResponse(
			responseCode = "410",
			description = "Short URL has expired",
			content = @Content
		)
	})
	@GetMapping("/{slug}")
	public ShortUrlResponse get(
		@Parameter(description = "The slug identifier of the short URL", required = true, example = "abc12345")
		@PathVariable String slug) {
		ShortUrl shortUrl = shortUrlService.getShortUrl(slug);
		return ShortUrlResponse.from(shortUrl, shortUrlService.buildPublicShortUrl(shortUrl.getSlug()));
	}
}

