package com.schambeck.marketplace.usecase.seller;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface SellerApplicationRepository extends JpaRepository<SellerApplication, Integer> {
    Optional<SellerApplication> findFirstByUser_ExternalIdOrderBySubmittedAtDesc(String externalId);
    boolean existsByUser_ExternalIdAndStatus(String externalId, SellerApplicationStatus status);
    Page<SellerApplication> findAllByStatusOrderBySubmittedAtAsc(SellerApplicationStatus status, Pageable pageable);
}
