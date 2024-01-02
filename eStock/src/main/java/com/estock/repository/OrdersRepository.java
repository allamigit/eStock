package com.estock.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.estock.model.Orders;

@Repository
public interface OrdersRepository extends JpaRepository<Orders, Long> {
	
	@Query("SELECT o FROM Orders o WHERE o.orderId = ?1")
	public Orders getOrderDetails(Long orderId);
	
}
