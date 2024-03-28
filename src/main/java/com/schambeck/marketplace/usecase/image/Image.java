package com.schambeck.marketplace.usecase.image;

import com.schambeck.marketplace.usecase.product.Product;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

import static jakarta.persistence.CascadeType.MERGE;
import static jakarta.persistence.GenerationType.IDENTITY;

@Builder
@Getter
@Setter
@Entity
@Table(name = "image")
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class Image {
	@Id
	@Column(name = "id")
	@GeneratedValue(strategy = IDENTITY)
	private Integer id;

	@ManyToOne(cascade = MERGE)
	@JoinColumn(name = "product_id", referencedColumnName = "id")
	private Product product;

	@Column(name = "object_id")
	private String objectId;

	@Column(name = "file_name")
	private String fileName;

	@Column(name = "content_type")
	private String contentType;

	@Column(name = "size")
	private Long size;

	@Version
	@Column(name = "version")
	private Integer version;

	@CreatedDate
	@Column(name = "created_at")
	private LocalDateTime createdAt;

	@LastModifiedDate
	@Column(name = "modified_at")
	private LocalDateTime modifiedAt;
}
