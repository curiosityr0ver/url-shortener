package curiosityrover.ishumehta.urlshortener.web;

import java.net.URI;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import curiosityrover.ishumehta.urlshortener.model.ShortUrl;
import curiosityrover.ishumehta.urlshortener.service.ShortUrlService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

@Controller
@Tag(name = "URL Redirection", description = "API for redirecting short URLs to their destination")
public class ShortUrlRedirectController {

	private final ShortUrlService shortUrlService;

	public ShortUrlRedirectController(ShortUrlService shortUrlService) {
		this.shortUrlService = shortUrlService;
	}

	@Operation(
		summary = "Redirect to destination URL",
		description = "Redirects to the destination URL associated with the given slug. This endpoint registers a hit and returns a 308 Permanent Redirect."
	)
	@ApiResponses(value = {
		@ApiResponse(
			responseCode = "308",
			description = "Permanent redirect to destination URL"
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
	@GetMapping("/{slug:[A-Za-z0-9_-]+}")
	public ResponseEntity<Void> redirect(
		@Parameter(description = "The slug identifier of the short URL", required = true, example = "abc12345")
		@PathVariable String slug) {
		ShortUrl shortUrl = shortUrlService.registerHit(slug);
		HttpHeaders headers = new HttpHeaders();
		headers.setLocation(URI.create(shortUrl.getDestinationUrl()));
		return new ResponseEntity<>(headers, HttpStatus.PERMANENT_REDIRECT);
	}
}

