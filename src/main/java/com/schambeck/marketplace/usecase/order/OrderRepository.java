package com.schambeck.marketplace.usecase.order;
import org.springframework.data.domain.*; import org.springframework.data.jpa.repository.*; import org.springframework.data.repository.query.Param; import jakarta.persistence.LockModeType; import java.util.*;
public interface OrderRepository extends JpaRepository<Order,Integer> {
 Optional<Order> findByBuyer_UsernameAndCheckoutKey(String username,String checkoutKey);
 Optional<Order> findByPublicId(UUID publicId);
 Page<Order> findAllByBuyer_UsernameOrderByCreatedAtDesc(String username,Pageable pageable);
 @Lock(LockModeType.PESSIMISTIC_WRITE) @Query("select o from Order o where o.publicId=:id") Optional<Order> findByPublicIdForUpdate(@Param("id") UUID id);
}
