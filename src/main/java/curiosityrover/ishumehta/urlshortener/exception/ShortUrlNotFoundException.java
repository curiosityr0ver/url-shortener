package curiosityrover.ishumehta.urlshortener.exception;

public class ShortUrlNotFoundException extends RuntimeException {

	public ShortUrlNotFoundException(String slug) {
		super("No short URL found for slug: " + slug);
	}
}

