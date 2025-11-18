package curiosityrover.ishumehta.urlshortener.service;

import java.security.SecureRandom;

import org.springframework.stereotype.Component;

@Component
public class SlugGenerator {

	private static final char[] ALPHABET = "0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ".toCharArray();
	private final SecureRandom random = new SecureRandom();

	public String generateSlug(int length) {
		if (length <= 0) {
			throw new IllegalArgumentException("Slug length must be positive");
		}
		StringBuilder slug = new StringBuilder(length);
		for (int i = 0; i < length; i++) {
			slug.append(ALPHABET[random.nextInt(ALPHABET.length)]);
		}
		return slug.toString();
	}
}

