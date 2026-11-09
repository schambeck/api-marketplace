package com.schambeck.marketplace.usecase.order;
import org.springframework.data.jpa.repository.JpaRepository; import java.util.*;
public interface OrderItemRepository extends JpaRepository<OrderItem,Integer> { List<OrderItem> findAllByOrder_IdOrderByIdAsc(Integer orderId); }
