package com.patria.test.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.patria.test.entity.Order;

@Repository
public interface OrderRepository extends JpaRepository<Order, String>, JpaSpecificationExecutor<Order> {

    @Query(value = "SELECT * FROM ORDERS o ORDER BY CAST(SUBSTRING(o.order_no, 2) AS INTEGER) DESC LIMIT 1", nativeQuery = true)
    Optional<Order> findTopByOrderByOrderNoDesc();

}
