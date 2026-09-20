package com.schambeck.marketplace.usecase.offer;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OfferStatusHistoryRepository extends JpaRepository<OfferStatusHistory, Integer> {
    List<OfferStatusHistory> findAllByOfferIdOrderByIdAsc(Integer offerId);
}
