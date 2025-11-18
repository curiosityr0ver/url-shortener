package curiosityrover.ishumehta.urlshortener.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import curiosityrover.ishumehta.urlshortener.model.ShortUrl;

public interface ShortUrlRepository extends JpaRepository<ShortUrl, Long> {

	Optional<ShortUrl> findBySlug(String slug);

	boolean existsBySlug(String slug);
}

