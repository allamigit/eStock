package com.estock.service;

import java.io.FileWriter;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.estock.model.OrderDetailsResponse;
import com.estock.model.Orders;
import com.estock.model.Stock;
import com.estock.repository.OrdersRepository;
import com.estock.repository.StockRepository;

@Service
public class OrdersService {

	@Autowired
	private StockRepository stockRepository;

	@Autowired
	private OrdersRepository ordersRepository;

	@Value("${localFilePath}")
	private String localFilePath;
	
	public OrdersService(StockRepository stockRepository, OrdersRepository ordersRepository) {
		super();
		this.stockRepository = stockRepository;
		this.ordersRepository = ordersRepository;
	}

	public OrderDetailsResponse getOrderDetails(Long orderId) {
		OrderDetailsResponse orderDetailsResponse = null;
		Orders order = ordersRepository.getOrderDetails(orderId);
		if(order != null) {
			orderDetailsResponse = OrderDetailsResponse.builder()
					.orderId(orderId)
					.orderCustomer(order.getOrderCustomer())
					.orderDate(order.getOrderDate())
					.updateDate(order.getUpdateDate())
					.itemCode(order.getItemCode())
					.itemName(stockRepository.getItemName(order.getItemCode()))
					.itemSoldQTY(order.getItemSoldQTY())
					.itemPrice(order.getItemPrice())
					.orderTotalAmount(order.getOrderTotalAmount())
					.orderStatus(order.getOrderStatus())
					.orderComment(order.getOrderComment())
					.build();
		}
		
		return orderDetailsResponse;
	}
	
	public List<String> readOrdersDataFile(MultipartFile fileName) {		
		Path filePath = Paths.get(this.localFilePath, fileName.getOriginalFilename());
		List<String> fileLines = null;
		try (Stream<String> lines = Files.lines(filePath)) {
			fileLines = lines.collect(Collectors.toList());
		} 
		catch (IOException e) {
		  e.printStackTrace();
		}
		
		return fileLines;
	}
	
	public List<Orders> processOrdersDataFile(List<String> fileLines) {	
		List<Orders> ordersList = new ArrayList<>();
	    for(int a=1; a<fileLines.size(); a++) {
	      String[] eachLine = fileLines.get(a).split(", ");
		  String[] lineData = eachLine[0].split(",");
		  Long orderId = Long.parseLong(lineData[0]);
		  String orderCustomer = lineData[1];
		  Long itemCode = Long.parseLong(lineData[2]);
		  Integer itemSoldQTY = Integer.parseInt(lineData[3]);
		  
		  Orders order = ordersRepository.getOrderDetails(orderId);
		  Stock itemDetails = stockRepository.getItemDetails(itemCode);
		  String orderStatus, orderComment = null;
		  Orders updatedOrder = null;
		  
		  if(order == null && itemSoldQTY <= itemDetails.getItemStockQTY()) {
			//Add new order
			orderStatus = "NEW_COMPLETE";
			updatedOrder = addNewOrder(orderId, orderCustomer, itemSoldQTY, orderStatus, orderComment, itemDetails);
		  } 
		  
		  if(itemSoldQTY > itemDetails.getItemStockQTY()) {
		    //Reject add/update order
			orderStatus = (order==null?"NEW_FAIL":"CHANGE_FAIL");
			orderComment = "Unsufficient stock quantity (" + itemSoldQTY + ")";
			if(orderStatus.equals("NEW_FAIL")) {
				updatedOrder = addNewOrder(orderId, orderCustomer, 0, orderStatus, orderComment, itemDetails);
			} else {
				updatedOrder = updateOrder(orderId, itemSoldQTY, orderStatus, orderComment, itemDetails);
			}
		  }
		  
		  if(order != null && order.getItemSoldQTY() + itemSoldQTY < 0) {
			//Reject update order
			orderStatus = "RETURN_FAIL";
			orderComment = "Invalid return quantity (" + itemSoldQTY + ")";
			updatedOrder = updateOrder(orderId, itemSoldQTY, orderStatus, orderComment, itemDetails);
		  }
		  
		  if(order != null && order.getItemSoldQTY() + itemSoldQTY >= 0) {
			//Update order
			orderStatus = (order.getItemSoldQTY() + itemSoldQTY>0?"CHANGE_COMPLETE":"RETURN_COMPLETE");
			updatedOrder = updateOrder(orderId, itemSoldQTY, orderStatus, orderComment, itemDetails);
		  }
		  
		  ordersList.add(updatedOrder);
	    }
	    
		return ordersList;
	}
	
	public void generateOrdersDataFile(List<Orders> ordersList) {
		List<Orders> ordersCompleteList = ordersList.stream()
				.filter(o -> o.getOrderStatus().endsWith("COMPLETE"))
				.collect(Collectors.toList());
		
		if(ordersCompleteList != null) {
			try {
				Path filePath = Paths.get(this.localFilePath, "eStock_Orders_Complete.txt");
				FileWriter writeLine = new FileWriter(filePath.toString());
				writeLine.write("orderId,orderCustomer,itemCode,itemSoldQTY,orderStatus\n");
				for(int i=0; i<ordersCompleteList.size(); i++) {
					String strLine = ordersCompleteList.get(i).getOrderId().toString() + "," +
							ordersCompleteList.get(i).getOrderCustomer() + "," +
							ordersCompleteList.get(i).getItemCode().toString() + "," +
							ordersCompleteList.get(i).getItemSoldQTY().toString() + "," +
							ordersCompleteList.get(i).getOrderStatus();
					writeLine.write(strLine);
					if(i < ordersCompleteList.size()-1) writeLine.write("\n");
				}
				writeLine.close();
		    } catch (IOException e) {
		      e.printStackTrace();
		    }
		}
		
		List<Orders> ordersFailList = ordersList.stream()
				.filter(o -> o.getOrderStatus().endsWith("FAIL"))
				.collect(Collectors.toList());

		if(ordersFailList != null) {
			try {
				Path filePath = Paths.get(this.localFilePath, "eStock_Orders_Fail.txt");
				FileWriter writeLine = new FileWriter(filePath.toString());
				writeLine.write("orderId,orderCustomer,itemCode,itemSoldQTY,orderStatus,orderComment\n");
				for(int i=0; i<ordersFailList.size(); i++) {
					String strLine = ordersFailList.get(i).getOrderId().toString() + "," +
							ordersFailList.get(i).getOrderCustomer() + "," +
							ordersFailList.get(i).getItemCode().toString() + "," +
							ordersFailList.get(i).getItemSoldQTY().toString() + "," +
							ordersFailList.get(i).getOrderStatus() + "," +
							ordersFailList.get(i).getOrderComment();
					writeLine.write(strLine);
					if(i < ordersFailList.size()-1) writeLine.write("\n");
				}
				writeLine.close();
		    } catch (IOException e) {
		      e.printStackTrace();
		    }
		}
		
	}

	private Orders addNewOrder(Long orderId, String orderCustomer, Integer itemSoldQTY, String orderStatus, String orderComment, Stock itemDetails) {
		Timestamp timestamp = new Timestamp(System.currentTimeMillis());
		BigDecimal orderTotalAmount = itemDetails.getItemPrice().multiply(BigDecimal.valueOf(itemSoldQTY));
		Orders order = Orders.builder()
				.orderId(orderId)
				.orderCustomer(orderCustomer)
				.orderDate(timestamp)
				.itemCode(itemDetails.getItemCode())
				.itemSoldQTY(itemSoldQTY)
				.itemPrice(itemDetails.getItemPrice())
				.orderTotalAmount(orderTotalAmount)
				.orderStatus(orderStatus)
				.orderComment(orderComment)
				.build();
		
		ordersRepository.save(order);
		
		if(!orderStatus.contains("FAIL")) {
			itemDetails.setItemStockQTY(itemDetails.getItemStockQTY() - itemSoldQTY);
			stockRepository.save(itemDetails);
		}
		
		return order;
	}

	private Orders updateOrder(Long orderId, Integer itemSoldQTY, String orderStatus, String orderComment, Stock itemDetails) {
		Timestamp timestamp = new Timestamp(System.currentTimeMillis());
		Orders order = ordersRepository.getOrderDetails(orderId);
		Integer newSoldQTY = order.getItemSoldQTY() + itemSoldQTY;
		BigDecimal orderTotalAmount = itemDetails.getItemPrice().multiply(BigDecimal.valueOf(newSoldQTY));
		if(orderStatus.contains("FAIL")) {
			newSoldQTY = order.getItemSoldQTY();
			orderTotalAmount = order.getOrderTotalAmount();
		}
		order.setUpdateDate(timestamp);
		order.setItemSoldQTY(newSoldQTY);
		order.setOrderTotalAmount(orderTotalAmount);
		order.setOrderStatus(orderStatus);
		order.setOrderComment(orderComment);
		ordersRepository.save(order);
		
		if(!orderStatus.contains("FAIL")) {
			itemDetails.setItemStockQTY(itemDetails.getItemStockQTY() - itemSoldQTY);
			stockRepository.save(itemDetails);
		}
		
		return order;
	}

}
