package com.example.commercepaymentsystems.orders.repository;

import com.example.commercepaymentsystems.orders.entity.Order;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface OrderRepository extends JpaRepository<Order, Long> {

    // 생성일 기준 최신 주문부터 조회
    Page<Order> findByCustomer_IdOrderByCreatedAtDesc(Long customerId, Pageable pageable);

    // 주문 상세 조회
    Optional<Order> findByIdAndCustomer_Id(Long orderId, Long customerId);
}
