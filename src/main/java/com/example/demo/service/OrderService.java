package com.example.demo.service;

import org.apache.coyote.BadRequestException;

import com.example.demo.dto.OrderPageResponseDTO;
import com.example.demo.dto.OrderRequestDTO;
import com.example.demo.dto.OrderRequestUpdateDTO;
import com.example.demo.dto.PagedResponseDTO;
import com.example.demo.exception.ResourceNotFoundException;
import com.example.demo.model.Order;

public interface OrderService {
	public Order createOrder(OrderRequestDTO orderRequestDTO) throws ResourceNotFoundException, Exception;
	public Order getOrderById(String orderId);
	public Order cancelOrder(String orderId) throws BadRequestException;
	public Order confirmOrder(String orderId) throws BadRequestException;
	public Order updateOrder(String orderId, OrderRequestUpdateDTO orderRequestUpdateDTO) throws BadRequestException, ResourceNotFoundException, Exception;
	public PagedResponseDTO<OrderPageResponseDTO> getAllOrders(int page, int limit, String sortField, String sortOrder);
	public PagedResponseDTO<OrderPageResponseDTO> getOrdersByUserId(String userId, int page, int limit, String sortField, String sortOrder);
}
