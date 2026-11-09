package com.schambeck.marketplace.usecase.order;
import org.springframework.data.jpa.repository.JpaRepository; import java.util.*;
public interface OrderStatusHistoryRepository extends JpaRepository<OrderStatusHistory,Long> { Optional<OrderStatusHistory> findByOrder_IdAndIdempotencyKey(Integer id,String key); List<OrderStatusHistory> findAllByOrder_IdOrderByCreatedAtAsc(Integer id); }
