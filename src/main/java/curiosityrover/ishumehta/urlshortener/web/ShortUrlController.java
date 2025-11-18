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
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/urls")
@Validated
public class ShortUrlController {

	private final ShortUrlService shortUrlService;

	public ShortUrlController(ShortUrlService shortUrlService) {
		this.shortUrlService = shortUrlService;
	}

	@PostMapping
	public ResponseEntity<ShortUrlResponse> create(@Valid @RequestBody CreateShortUrlRequest request) {
		ShortUrl created = shortUrlService.createShortUrl(request.destinationUrl(), request.customSlug());
		return ResponseEntity.status(HttpStatus.CREATED)
			.body(ShortUrlResponse.from(created, shortUrlService.buildPublicShortUrl(created.getSlug())));
	}

	@GetMapping("/{slug}")
	public ShortUrlResponse get(@PathVariable String slug) {
		ShortUrl shortUrl = shortUrlService.getShortUrl(slug);
		return ShortUrlResponse.from(shortUrl, shortUrlService.buildPublicShortUrl(shortUrl.getSlug()));
	}
}

