package com.example.demo.repository;

import com.example.demo.entity.ProductOrder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProductOrderRepository extends JpaRepository<ProductOrder, Long> {
    List<ProductOrder> findByMemberId(String memberId);
    Optional<ProductOrder> findByOrderNumber(String orderNumber);
}
