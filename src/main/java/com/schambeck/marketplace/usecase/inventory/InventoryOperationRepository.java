package com.schambeck.marketplace.usecase.inventory;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface InventoryOperationRepository extends JpaRepository<InventoryOperation, Long> {
    Optional<InventoryOperation> findByOperationTypeAndOperationKey(String operationType, String operationKey);
}
