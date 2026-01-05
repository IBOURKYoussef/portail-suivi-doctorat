package com.spring.order_service.repository;

import com.spring.order_service.model.Order;
import org.springframework.data.jpa.repository.JpaRepository;

public interface

OrderRepository extends JpaRepository<Order, Long> {
}
