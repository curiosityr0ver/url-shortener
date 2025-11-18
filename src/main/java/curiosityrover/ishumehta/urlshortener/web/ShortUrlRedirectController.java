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

@Controller
public class ShortUrlRedirectController {

	private final ShortUrlService shortUrlService;

	public ShortUrlRedirectController(ShortUrlService shortUrlService) {
		this.shortUrlService = shortUrlService;
	}

	@GetMapping("/{slug:[A-Za-z0-9_-]+}")
	public ResponseEntity<Void> redirect(@PathVariable String slug) {
		ShortUrl shortUrl = shortUrlService.registerHit(slug);
		HttpHeaders headers = new HttpHeaders();
		headers.setLocation(URI.create(shortUrl.getDestinationUrl()));
		return new ResponseEntity<>(headers, HttpStatus.PERMANENT_REDIRECT);
	}
}

