package com.startup.tasteflowbe.controller;

import com.startup.tasteflowbe.model.Order;
import com.startup.tasteflowbe.dto.CheckoutRequest;
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
    public ResponseEntity<Order> checkoutFromCartItems(@RequestBody CheckoutRequest checkoutRequestDto) {
        // Giả sử bạn có phương thức để lấy User từ session/security context
        Long currentUser = 1L;

        // Lấy các giá trị từ DTO
        List<Long> cartItemIds = checkoutRequestDto.getCartItemIds();
        List<Long> voucherIds = checkoutRequestDto.getVoucherIds();
        Long shippingAddressId = checkoutRequestDto.getShippingAddressId();
        Long storeId = checkoutRequestDto.getStoreId();

        // Tạo đơn hàng từ các thông tin trong DTO
        Order createdOrder = orderService.checkoutFromCartItems(currentUser, cartItemIds,
                voucherIds, shippingAddressId, storeId);

        return ResponseEntity.ok(createdOrder);
    }

}
