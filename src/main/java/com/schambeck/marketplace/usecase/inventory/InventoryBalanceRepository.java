package com.schambeck.marketplace.usecase.inventory;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface InventoryBalanceRepository extends JpaRepository<InventoryBalance, Integer> {
    boolean existsByProductId(Integer productId);
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select balance from InventoryBalance balance join fetch balance.product where balance.product.id = :productId")
    Optional<InventoryBalance> findByProductIdForUpdate(@Param("productId") Integer productId);

    Optional<InventoryBalance> findByProduct_Id(Integer productId);
}
