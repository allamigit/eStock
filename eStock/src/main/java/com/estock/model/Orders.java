package com.estock.model;

import java.math.BigDecimal;
import java.sql.Timestamp;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Entity
@Table(name = "orders")
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class Orders {
	
	@Id
	@Column(name = "order_id")
	private Long orderId;
	
	@Column(name = "order_customer")
	private String orderCustomer;
	
	@Column(name = "order_date")
	private Timestamp orderDate;
	
	@Column(name = "update_date")
	private Timestamp updateDate;
	
	@Column(name = "item_code")
	private Long itemCode;
	
	@Column(name = "item_sold_qty")
	private Integer itemSoldQTY;
	
	@Column(name = "item_price")
	private BigDecimal itemPrice;

	@Column(name = "order_total_amount")
	private BigDecimal orderTotalAmount;
	
	@Column(name = "order_status")
	private String orderStatus;
	
	@Column(name = "order_comment")
	private String orderComment;

}
