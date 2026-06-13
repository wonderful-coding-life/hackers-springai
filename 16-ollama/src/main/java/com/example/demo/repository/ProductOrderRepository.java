package com.example.demo.repository;

import com.example.demo.entity.Member;
import com.example.demo.entity.ProductOrder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProductOrderRepository extends JpaRepository<ProductOrder, Long> {
    List<ProductOrder> findByMemberUsername(String username);
    Optional<ProductOrder> findByOrderNumberAndMemberUsername(String orderNumber, String username);
}
