package com.estock.model;

import java.math.BigDecimal;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Entity
@Table(name = "stock")
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class Stock {
	
	@Id
	//@SequenceGenerator(name = "stockseq", sequenceName = "stock_seq", initialValue = 102, allocationSize = 2)
	//@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "stockseq")
	@Column(name = "item_code")
	private Long itemCode;
	
	@Column(name = "item_name")
	private String itemName;
	
	@Column(name = "item_stock_qty")
	private Integer itemStockQTY;
	
	@Column(name = "item_price")
	private BigDecimal itemPrice;
	
}