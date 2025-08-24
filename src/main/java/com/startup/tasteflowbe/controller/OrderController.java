package com.startup.tasteflowbe.controller;

import com.startup.tasteflowbe.dto.request.OrderRequestDTO;
import com.startup.tasteflowbe.dto.request.StoreOrderDTO;
import com.startup.tasteflowbe.dto.request.UpdateOrderStatusDTO;
import com.startup.tasteflowbe.dto.response.CreatePaymentResponseDTO;
import com.startup.tasteflowbe.dto.response.OrderResponseDTO;
import com.startup.tasteflowbe.enums.PaymentMethod;
import com.startup.tasteflowbe.mapper.OrderMapper;
import com.startup.tasteflowbe.model.Order;
import com.startup.tasteflowbe.repository.OrderItemRepository;
import com.startup.tasteflowbe.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;
    private final OrderMapper orderMapper;
    private final OrderItemRepository orderItemRepository;

    @GetMapping
    public ResponseEntity<List<Order>> getAllOrders() {
        return ResponseEntity.ok(orderService.getAllOrders());
    }

    @GetMapping("status/{orderCode}")
    public ResponseEntity<OrderResponseDTO> getOrderStatus(@PathVariable Long orderCode) {
        Order order = orderService.findByOrderCode(orderCode);
        return ResponseEntity.ok(orderMapper.toDto(order));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Order> getOrderById(@PathVariable Long id) {
        return orderService.getOrderById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/store/{id}")
    public ResponseEntity<List<OrderResponseDTO>> getAllStoreOrders(@PathVariable Long id) {
        List<OrderResponseDTO> dtoList = orderService.getAllStoreOrders(id)
                .stream()
                .map(orderMapper::toDto)
                .collect(Collectors.toList());
        return ResponseEntity.ok(dtoList);
    }

    @PostMapping
    public ResponseEntity<Order> createOrder(@RequestBody Order order) {
        return ResponseEntity.ok(orderService.createOrder(order));
    }

    @PostMapping("/store")
    public ResponseEntity<?> createStoreOrder(@RequestBody StoreOrderDTO orderRequest) {
        Order order = orderService.createStoreOrder(orderRequest);
        return ResponseEntity.ok(order);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Order> updateOrder(@PathVariable Long id, @RequestBody Order order) {
        return ResponseEntity.ok(orderService.updateOrder(id, order));
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<OrderResponseDTO>> getAllOrdersByUserId(@PathVariable Long userId) {
        List<OrderResponseDTO> orders = orderService.getAllOrdersByUserId(userId);
        return ResponseEntity.ok(orders);
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
        OrderResponseDTO orderResponse = orderService.createOrder(requestDTO);

        if (requestDTO.getPaymentMethod() == PaymentMethod.ONLINE) {
            CreatePaymentResponseDTO paymentResponse = orderService.handleOnlinePayment(orderResponse);
            return ResponseEntity.ok(paymentResponse);
        }

        return ResponseEntity.ok(orderResponse);
    }

    // private StoreOrderResponseDTO convertToDto(Order order) {
    // if (order == null) {
    // return null;
    // }
    //
    // // Chuyển đổi List<OrderItem> -> List<OrderItemResponseDTO>
    // List<OrderItem> orderItems =
    // orderItemRepository.findByOrder_OrderId(order.getOrderId());
    // List<OrderItemResponseDTO> orderItemDtos = orderItems.stream()
    // .map(item -> {
    // OrderItemResponseDTO itemDto = new OrderItemResponseDTO();
    // itemDto.setProductId(item.getProduct().getProductId());
    // itemDto.setProductName(item.getProduct().getName());
    // itemDto.setQuantity(item.getQuantity());
    // itemDto.setPrice(item.getPrice());
    // return itemDto;
    // })
    // .collect(Collectors.toList());
    //
    // // Tạo và trả về DTO chính
    // StoreOrderResponseDTO dto = new StoreOrderResponseDTO();
    // dto.setOrderId(order.getOrderId());
    // dto.setOrderCode(order.getOrderCode());
    // dto.setTotal_price(order.getTotalPrice());
    // dto.setStatus(order.getStatus());
    // dto.setOrder_date(order.getOrderDate());
    // dto.setUser(order.getUser());
    // dto.setOrderItems(orderItemDtos);
    //
    // return dto;
    // }

}
