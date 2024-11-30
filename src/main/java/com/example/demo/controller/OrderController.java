 package com.example.demo.controller;

import java.util.LinkedHashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.dto.ApiResponseDTO;
import com.example.demo.dto.OrderPageResponseDTO;
import com.example.demo.dto.OrderRequestDTO;
import com.example.demo.dto.OrderRequestUpdateDTO;
import com.example.demo.dto.PagedResponseDTO;
import com.example.demo.exception.ResourceNotFoundException;
import com.example.demo.model.Order;
import com.example.demo.service.OrderService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/orders")
public class OrderController {
	
	private static final Logger logger = LoggerFactory.getLogger(OrderController.class);
	private OrderService orderService;

	public OrderController(OrderService orderService) {
		this.orderService = orderService;
	}
	
	@PreAuthorize("hasAnyRole('ADMIN', 'USER')")
	@GetMapping("/{orderId}")
	public ResponseEntity<?> getOrderById(@PathVariable String orderId) {
		Order order = orderService.getOrderById(orderId);
		ApiResponseDTO<Order> response = new ApiResponseDTO<>("Order retrieved successfully", HttpStatus.OK.value(),
				order);
		return ResponseEntity.status(HttpStatus.OK).body(response);
	}
	
	@PreAuthorize("hasRole('ADMIN')")
	@GetMapping
	public ResponseEntity<?> getAllOrders(@RequestParam(defaultValue = "0") int page,
			@RequestParam(defaultValue = "0") int limit, @RequestParam(defaultValue = "createdAt") String sortField,
			@RequestParam(defaultValue = "asc") String sortOrder) {
		PagedResponseDTO<OrderPageResponseDTO> orders = orderService.getAllOrders(page, limit, sortField, sortOrder);
		ApiResponseDTO<PagedResponseDTO<OrderPageResponseDTO>> response = new ApiResponseDTO<>(
				"Orders retrieved successfully", HttpStatus.OK.value(), orders);
		return ResponseEntity.status(HttpStatus.OK).body(response);
	}
	
	@PreAuthorize("hasAnyRole('ADMIN', 'USER')")
	@PostMapping
	public ResponseEntity<?> createOrder(@Valid @RequestBody OrderRequestDTO orderRequestDTO, BindingResult bindingResult)
			throws ResourceNotFoundException, Exception {
		if(bindingResult.hasErrors()) {
			Map<String, Object> errors = new LinkedHashMap<String, Object>();
			bindingResult.getFieldErrors().forEach(error -> {
				errors.put(error.getField(), error.getDefaultMessage());
			});
			ApiResponseDTO<Map<String, Object>> response = new ApiResponseDTO<>("Validation failed", HttpStatus.BAD_REQUEST.value(), errors);
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
		}
		Order order = orderService.createOrder(orderRequestDTO);
		ApiResponseDTO<Order> response = new ApiResponseDTO<>("Order created successfully", HttpStatus.CREATED.value(),
				order);
		return ResponseEntity.status(HttpStatus.CREATED).body(response);
	}
	
	@PreAuthorize("hasAnyRole('ADMIN', 'USER')")
	@PostMapping("/{orderId}/cancel")
	public ResponseEntity<?> cancelOrder(@PathVariable String orderId) throws ResourceNotFoundException, Exception {
		Order order = orderService.cancelOrder(orderId);
		ApiResponseDTO<Order> response = new ApiResponseDTO<>("Order canceled successfully", HttpStatus.OK.value(),
				order);
		return ResponseEntity.ok(response);
	}
	
	@PreAuthorize("hasRole('ADMIN')")
	@PostMapping("/{orderId}/confirm")
	public ResponseEntity<?> confirmOrder(@PathVariable String orderId) throws ResourceNotFoundException, Exception {
		Order order = orderService.confirmOrder(orderId);
		ApiResponseDTO<Order> response = new ApiResponseDTO<>("Order confirmed successfully", HttpStatus.OK.value(),
				order);
		return ResponseEntity.ok(response);
	}
	
	@PreAuthorize("hasAnyRole('ADMIN', 'USER')")
	@PostMapping("/{orderId}")
	public ResponseEntity<?> updateOrder(@PathVariable String orderId, @Valid @RequestBody OrderRequestUpdateDTO orderRequestUpdateDTO, BindingResult bindingResult)
			throws ResourceNotFoundException, Exception {
		if(bindingResult.hasErrors()) {
			Map<String, Object> errors = new LinkedHashMap<String, Object>();
			bindingResult.getFieldErrors().forEach(error -> {
				errors.put(error.getField(), error.getDefaultMessage());
			});
			ApiResponseDTO<Map<String, Object>> response = new ApiResponseDTO<>("Validation failed", HttpStatus.BAD_REQUEST.value(), errors);
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
		}
		Order order = orderService.updateOrder(orderId, orderRequestUpdateDTO);
		ApiResponseDTO<Order> response = new ApiResponseDTO<>("Order updated successfully", HttpStatus.OK.value(),
				order);
		return ResponseEntity.ok(response);
	}
}
