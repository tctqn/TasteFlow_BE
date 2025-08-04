package com.startup.tasteflowbe.service;

import com.startup.tasteflowbe.dto.request.OrderRequestDTO;
import com.startup.tasteflowbe.dto.request.StoreOrderDTO;
import com.startup.tasteflowbe.dto.response.CreatePaymentResponseDTO;
import com.startup.tasteflowbe.dto.response.OrderResponseDTO;
import com.startup.tasteflowbe.model.Order;

import java.util.List;
import java.util.Optional;

public interface OrderService {
    List<Order> getAllOrders();

    Order findByOrderCode(Long orderCode);

    Optional<Order> getOrderById(Long id);

    Order createOrder(Order order);

    Order updateOrder(Long id, Order order);

    void deleteOrder(Long id);

    // Order checkoutFromCartItems(Long userId, List<Long> cartItemIds, List<Long>
    // voucherIds, Long shippingAddressId, Long storeId);
    OrderResponseDTO createOrder(OrderRequestDTO dto);

    CreatePaymentResponseDTO handleOnlinePayment(OrderResponseDTO order);

    void markOrderAsPaid(Long orderCode);

    List<Order> getAllStoreOrders(Long storeId);

    OrderResponseDTO updateOrderStatus(Long id, String status, String notes);

    List<OrderResponseDTO> getAllOrdersByUserId(Long userId);

    public Order createStoreOrder(StoreOrderDTO storeOrderDTO);
}
