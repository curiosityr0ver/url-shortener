package curiosityrover.ishumehta.urlshortener.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import curiosityrover.ishumehta.urlshortener.exception.ShortUrlExpiredException;
import curiosityrover.ishumehta.urlshortener.model.ShortUrl;
import curiosityrover.ishumehta.urlshortener.repository.ShortUrlRepository;

class ShortUrlServiceTest {

	@Mock
	private ShortUrlRepository repository;

	@Mock
	private SlugGenerator slugGenerator;

	@InjectMocks
	private ShortUrlService service;

	@BeforeEach
	void setUp() {
		MockitoAnnotations.openMocks(this);
		when(slugGenerator.generateSlug(anyInt())).thenReturn("abc123");
	}

	@SuppressWarnings("null")
	@Test
	void createShortUrl_setsExpiresAt() {
		when(repository.existsBySlug("abc123")).thenReturn(false);
		when(repository.save(any(ShortUrl.class))).thenAnswer(invocation -> invocation.getArgument(0, ShortUrl.class));

		Instant expiresAt = Instant.now().plusSeconds(3600);

		ShortUrl created = service.createShortUrl("https://example.com", null, expiresAt);

		assertThat(created.getExpiresAt()).isEqualTo(expiresAt);
	}

	@Test
	void registerHit_throwsWhenExpired() {
		ShortUrl expired = ShortUrl.builder()
			.slug("expired")
			.destinationUrl("https://example.com")
			.expiresAt(Instant.now().minusSeconds(60))
			.build();

		when(repository.findBySlug("expired")).thenReturn(Optional.of(expired));

		assertThatThrownBy(() -> service.registerHit("expired"))
			.isInstanceOf(ShortUrlExpiredException.class)
			.hasMessageContaining("expired");
	}
}


