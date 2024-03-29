package com.estock.batch;

import java.math.BigDecimal;
import java.sql.Timestamp;

import org.springframework.batch.item.ItemProcessor;
import org.springframework.beans.factory.annotation.Autowired;

import com.estock.model.DataFile;
import com.estock.model.Orders;
import com.estock.model.Stock;
import com.estock.repository.OrdersRepository;
import com.estock.repository.StockRepository;

public class OrdersProcessor implements ItemProcessor<DataFile, Orders> {

	@Autowired
	private StockRepository stockRepository;

	@Autowired
	private OrdersRepository ordersRepository;

	@Override
    public Orders process(DataFile item) throws Exception, NumberFormatException {
        // Transform DataFile to Orders fields
		Long orderId = item.getOrderId();
		String orderCustomer = item.getOrderCustomer();
		Long itemCode = item.getItemCode();
		Integer itemSoldQTY = item.getItemSoldQTY();
		String orderStatus, orderComment = null;

        // Perform processing on DataFile
		Orders order = ordersRepository.getOrderDetails(orderId);
		Stock itemDetails = stockRepository.getItemDetails(itemCode);
		Orders updatedOrder = null;
		  
		if(order == null && itemSoldQTY <= itemDetails.getItemStockQTY()) {
		  //Add new order
		  orderStatus = "NEW_COMPLETE";
		  updatedOrder = createOrder(orderId, orderCustomer, itemSoldQTY, itemDetails, orderStatus, orderComment);
		} 
		  
		if(itemSoldQTY > itemDetails.getItemStockQTY()) {
		  //Reject add/update order
		  orderStatus = (order==null?"NEW_FAIL":"CHANGE_FAIL");
		  orderComment = "Unsufficient stock quantity (" + itemSoldQTY + ")";
		  if(orderStatus.equals("NEW_FAIL")) { 
			  itemSoldQTY = 0;
		  }
		  updatedOrder = createOrder(orderId, orderCustomer, itemSoldQTY, itemDetails, orderStatus, orderComment);
		}
		  
		if(order != null && order.getItemSoldQTY() + itemSoldQTY < 0) {
		  //Reject update order
		  orderStatus = "RETURN_FAIL";
		  orderComment = "Invalid return quantity (" + itemSoldQTY + ")";
		  updatedOrder = createOrder(orderId, orderCustomer, order.getItemSoldQTY(), itemDetails, orderStatus, orderComment); 
		  }
		  
		if(order != null && order.getItemSoldQTY() + itemSoldQTY >= 0) {
		  //Update order
		  orderStatus = (order.getItemSoldQTY() + itemSoldQTY>0?"CHANGE_COMPLETE":"RETURN_COMPLETE");
		  Integer newSoldQTY = order.getItemSoldQTY() + itemSoldQTY;
		  updatedOrder = createOrder(orderId, orderCustomer, newSoldQTY, itemDetails, orderStatus, orderComment);
		}

		// Return Orders object with processed values
		return updatedOrder;
    }
    
	
	private Orders createOrder(
			Long orderId, 
			String orderCustomer, 
			Integer itemSoldQTY, 
			Stock itemDetails,
			String orderStatus,
			String orderComment) {
		
		Timestamp timestamp = new Timestamp(System.currentTimeMillis());
		BigDecimal orderTotalAmount = itemDetails.getItemPrice().multiply(BigDecimal.valueOf(itemSoldQTY));
		Timestamp orderDate, updateDate = null;
		if(orderStatus.contains("NEW")) {
			orderDate = timestamp;
			updateDate = null;
		} else {
			orderDate = ordersRepository.getOrderDetails(orderId).getOrderDate();
			updateDate = timestamp;			
		}
		
		Orders order = Orders.builder()
					.orderId(orderId)
					.orderCustomer(orderCustomer)
					.orderDate(orderDate)
					.updateDate(updateDate)
					.itemCode(itemDetails.getItemCode())
					.itemSoldQTY(itemSoldQTY)
					.itemPrice(itemDetails.getItemPrice())
					.orderTotalAmount(orderTotalAmount)
					.orderStatus(orderStatus)
					.orderComment(orderComment)
					.build();
		
		return order;
	}
	
}
