package curiosityrover.ishumehta.urlshortener.web;

import java.util.HashMap;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import curiosityrover.ishumehta.urlshortener.exception.ShortUrlExpiredException;
import curiosityrover.ishumehta.urlshortener.exception.ShortUrlNotFoundException;
import curiosityrover.ishumehta.urlshortener.exception.SlugAlreadyExistsException;

@RestControllerAdvice
public class RestExceptionHandler {

	@ExceptionHandler(ShortUrlNotFoundException.class)
	public ProblemDetail handleNotFound(ShortUrlNotFoundException exception) {
		ProblemDetail detail = ProblemDetail.forStatus(HttpStatus.NOT_FOUND);
		detail.setTitle("Short URL not found");
		detail.setDetail(exception.getMessage());
		return detail;
	}

	@ExceptionHandler(SlugAlreadyExistsException.class)
	public ProblemDetail handleConflict(SlugAlreadyExistsException exception) {
		ProblemDetail detail = ProblemDetail.forStatus(HttpStatus.CONFLICT);
		detail.setTitle("Slug already exists");
		detail.setDetail(exception.getMessage());
		return detail;
	}

	@ExceptionHandler(ShortUrlExpiredException.class)
	public ProblemDetail handleExpired(ShortUrlExpiredException exception) {
		ProblemDetail detail = ProblemDetail.forStatus(HttpStatus.GONE);
		detail.setTitle("Short URL expired");
		detail.setDetail(exception.getMessage());
		return detail;
	}

	@ExceptionHandler(MethodArgumentNotValidException.class)
	public ProblemDetail handleValidation(MethodArgumentNotValidException exception) {
		ProblemDetail detail = ProblemDetail.forStatus(HttpStatus.BAD_REQUEST);
		detail.setTitle("Validation failed");

		Map<String, String> errors = new HashMap<>();
		for (FieldError fieldError : exception.getBindingResult().getFieldErrors()) {
			errors.put(fieldError.getField(), fieldError.getDefaultMessage());
		}
		detail.setProperty("errors", errors);
		return detail;
	}

	@ExceptionHandler(IllegalArgumentException.class)
	public ProblemDetail handleIllegalArgument(IllegalArgumentException exception) {
		ProblemDetail detail = ProblemDetail.forStatus(HttpStatus.BAD_REQUEST);
		detail.setTitle("Invalid request");
		detail.setDetail(exception.getMessage());
		return detail;
	}
}

