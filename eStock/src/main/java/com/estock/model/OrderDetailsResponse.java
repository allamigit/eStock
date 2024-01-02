package com.estock.model;

import java.math.BigDecimal;
import java.sql.Timestamp;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class OrderDetailsResponse {

	private Long orderId;
	
	private String orderCustomer;

	private Timestamp orderDate;
	
	private Timestamp updateDate;
	
	private Long itemCode;
	
	private String itemName;
	
	private Integer itemSoldQTY;
	
	private BigDecimal itemPrice;

	private BigDecimal orderTotalAmount;
	
	private String orderStatus;

	private String orderComment;

}
