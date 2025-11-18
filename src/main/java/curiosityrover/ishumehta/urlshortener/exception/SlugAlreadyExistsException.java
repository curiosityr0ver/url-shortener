package curiosityrover.ishumehta.urlshortener.exception;

public class SlugAlreadyExistsException extends RuntimeException {

	public SlugAlreadyExistsException(String slug) {
		super("Slug already in use: " + slug);
	}
}

