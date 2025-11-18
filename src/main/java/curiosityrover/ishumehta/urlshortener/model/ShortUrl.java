package curiosityrover.ishumehta.urlshortener.model;

import java.time.Instant;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "short_urls", indexes = @Index(name = "idx_short_urls_slug", columnList = "slug", unique = true))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ShortUrl {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(nullable = false, unique = true, length = 64)
	private String slug;

	@Column(name = "destination_url", nullable = false, length = 2048)
	private String destinationUrl;

	@Column(name = "created_at", nullable = false, updatable = false)
	private Instant createdAt;

	@Column(name = "last_accessed_at")
	private Instant lastAccessedAt;

	@Column(name = "hit_count", nullable = false)
	private long hitCount;

	@PrePersist
	@SuppressWarnings("unused")
	void onCreate() {
		createdAt = Instant.now();
		hitCount = 0L;
	}
}

