package com.estock.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class DataFile {

	private Long orderId;
	
	private String orderCustomer;

	private Long itemCode;
	
	private Integer itemSoldQTY;

}
