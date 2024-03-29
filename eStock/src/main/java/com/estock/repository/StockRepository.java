package com.estock.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.estock.model.Stock;

@Repository
public interface StockRepository extends JpaRepository<Stock, Long> {

	@Query(value = "SELECT s.itemName FROM Stock s WHERE s.itemCode = ?1")
	public String getItemName(Long itemCode);

	@Query(value = "SELECT s FROM Stock s WHERE s.itemCode = ?1")
	public Stock getItemDetails(Long itemCode);

}
