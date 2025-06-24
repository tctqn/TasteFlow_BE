package com.startup.tasteflowbe.controller;

import com.startup.tasteflowbe.dto.request.OrderRequestDTO;
import com.startup.tasteflowbe.dto.response.CreatePaymentResponseDTO;
import com.startup.tasteflowbe.dto.response.OrderResponseDTO;
import com.startup.tasteflowbe.enums.PaymentMethod;
import com.startup.tasteflowbe.mapper.OrderMapper;
import com.startup.tasteflowbe.model.Order;
import com.startup.tasteflowbe.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;
    private final OrderMapper orderMapper;

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

    @PostMapping
    public ResponseEntity<Order> createOrder(@RequestBody Order order) {
        return ResponseEntity.ok(orderService.createOrder(order));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Order> updateOrder(@PathVariable Long id, @RequestBody Order order) {
        return ResponseEntity.ok(orderService.updateOrder(id, order));
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

}
