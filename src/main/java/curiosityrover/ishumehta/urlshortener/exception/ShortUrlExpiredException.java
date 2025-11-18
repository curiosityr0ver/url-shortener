package curiosityrover.ishumehta.urlshortener.exception;

public class ShortUrlExpiredException extends RuntimeException {

	public ShortUrlExpiredException(String slug) {
		super("Short URL '" + slug + "' has expired");
	}
}


