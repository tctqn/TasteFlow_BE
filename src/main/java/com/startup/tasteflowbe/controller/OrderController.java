package com.startup.tasteflowbe.controller;

import com.startup.tasteflowbe.dto.request.OrderRequestDTO;
import com.startup.tasteflowbe.dto.request.UpdateOrderStatusDTO;
import com.startup.tasteflowbe.dto.response.CreatePaymentResponseDTO;
import com.startup.tasteflowbe.dto.response.InventoriesResponseDTO;
import com.startup.tasteflowbe.dto.response.OrderItemResponseDTO;
import com.startup.tasteflowbe.dto.response.OrderResponseDTO;
import com.startup.tasteflowbe.dto.response.ProductBatchResponseDTO;
import com.startup.tasteflowbe.dto.response.ProductResponseDTO;
import com.startup.tasteflowbe.dto.response.StoreOrderResponseDTO;
import com.startup.tasteflowbe.enums.OrderStatus;
import com.startup.tasteflowbe.enums.PaymentMethod;
import com.startup.tasteflowbe.model.Order;
import com.startup.tasteflowbe.model.OrderItem;
import com.startup.tasteflowbe.model.Product;
import com.startup.tasteflowbe.model.ProductBatch;
import com.startup.tasteflowbe.repository.OrderItemRepository;
import com.startup.tasteflowbe.dto.CheckoutRequest;
import com.startup.tasteflowbe.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;
    private final OrderItemRepository orderItemRepository;

    @GetMapping
    public ResponseEntity<List<Order>> getAllOrders() {
        return ResponseEntity.ok(orderService.getAllOrders());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Order> getOrderById(@PathVariable Long id) {
        return orderService.getOrderById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/store/{id}")
    public ResponseEntity<List<StoreOrderResponseDTO>> getAllStoreOrders(@PathVariable Long id) {
        List<StoreOrderResponseDTO> dtoList = orderService.getAllStoreOrders(id)
                .stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
        return ResponseEntity.ok(dtoList);
    }

    @PostMapping
    public ResponseEntity<Order> createOrder(@RequestBody Order order) {
        return ResponseEntity.ok(orderService.createOrder(order));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Order> updateOrder(@PathVariable Long id, @RequestBody Order order) {
        return ResponseEntity.ok(orderService.updateOrder(id, order));
    }

    @PutMapping("/update/{id}")
    public ResponseEntity<OrderResponseDTO> updateOrderStatus(@PathVariable Long id,
            @RequestBody UpdateOrderStatusDTO request) {
        return ResponseEntity.ok(orderService.updateOrderStatus(id, request.getStatus(), request.getNotes()));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteOrder(@PathVariable Long id) {
        orderService.deleteOrder(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/checkout")
    public ResponseEntity<?> checkout(@RequestBody OrderRequestDTO requestDTO) {
        System.out.println("Received checkout request: " + requestDTO);
        OrderResponseDTO orderResponse = orderService.createOrder(requestDTO);

        if (requestDTO.getPaymentMethod() == PaymentMethod.ONLINE) {
            CreatePaymentResponseDTO paymentResponse = orderService.handleOnlinePayment(orderResponse);
            return ResponseEntity.ok(paymentResponse);
        }

        return ResponseEntity.ok(orderResponse);
    }

    private StoreOrderResponseDTO convertToDto(Order order) {
        if (order == null) {
            return null;
        }

        // Chuyển đổi List<OrderItem> -> List<OrderItemResponseDTO>
        List<OrderItem> orderItems = orderItemRepository.findByOrder_OrderId(order.getOrderId());
        List<OrderItemResponseDTO> orderItemDtos = orderItems.stream()
                .map(item -> {
                    OrderItemResponseDTO itemDto = new OrderItemResponseDTO();
                    itemDto.setProductId(item.getProduct().getProductId());
                    itemDto.setProductName(item.getProduct().getName());
                    itemDto.setQuantity(item.getQuantity());
                    itemDto.setPrice(item.getPrice());
                    return itemDto;
                })
                .collect(Collectors.toList());

        // Tạo và trả về DTO chính
        StoreOrderResponseDTO dto = new StoreOrderResponseDTO();
        dto.setOrderId(order.getOrderId());
        dto.setOrderCode(order.getOrderCode());
        dto.setTotal_price(order.getTotalPrice());
        dto.setStatus(order.getStatus());
        dto.setOrder_date(order.getOrderDate());
        dto.setUser(order.getUser());
        dto.setOrderItems(orderItemDtos);

        return dto;
    }

}
