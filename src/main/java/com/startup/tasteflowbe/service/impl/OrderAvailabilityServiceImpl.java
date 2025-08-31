package com.startup.tasteflowbe.service.impl;

import com.google.zxing.NotFoundException;
import com.startup.tasteflowbe.dto.response.BatchAllocationDTO;
import com.startup.tasteflowbe.dto.response.OrderItemAvailabilityDTO;
import com.startup.tasteflowbe.dto.response.OrderItemResponseDTO;
import com.startup.tasteflowbe.dto.response.OrderResponseDTO;
import com.startup.tasteflowbe.mapper.OrderMapper;
import com.startup.tasteflowbe.model.Inventory;
import com.startup.tasteflowbe.repository.InventoryRepository;
import com.startup.tasteflowbe.repository.OrderRepository;
import com.startup.tasteflowbe.service.OrderAvailabilityService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OrderAvailabilityServiceImpl implements OrderAvailabilityService {

    private final InventoryRepository inventoryRepository;
    private final OrderRepository orderRepository;
    private final OrderMapper orderMapper;

    @Override
    @Transactional
    public OrderResponseDTO getOrderDetailWithAvailability(Long orderId) {
        // 1) Load Order + Store + Items
        var order = orderRepository.findByIdWithItemsAndStore(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found: " + orderId));

        Long storeId = order.getStore().getStoreId();

        // 2) Map sang OrderResponseDTO (hoặc nếu bạn đã có rồi thì bỏ qua bước này)
        OrderResponseDTO dto = orderMapper.toDto(order);

        // 3) Lấy list productId
        List<Long> productIds = dto.getItems().stream()
                .map(OrderItemResponseDTO::getProductId)
                .distinct().toList();

        // 4) Lấy inventories theo FEFO
        List<Inventory> invs = productIds.isEmpty()
                ? List.of()
                : inventoryRepository.findByStoreAndProductIdsOrderByFefo(storeId, productIds);

        // 5) Group theo productId
        Map<Long, List<Inventory>> byProduct = invs.stream()
                .collect(Collectors.groupingBy(i -> i.getProduct().getProductId()));

        // 6) Tính availability cho từng item và gắn vào DTO
        for (OrderItemResponseDTO item : dto.getItems()) {
            int neededBase = defaultZero(item.getQuantityInBase());
            var invList = byProduct.getOrDefault(item.getProductId(), List.of());

            int totalAvailable = invList.stream().mapToInt(Inventory::getQuantity).sum();

            List<BatchAllocationDTO> allocations = new ArrayList<>();
            int remaining = neededBase;

            for (Inventory inv : invList) {
                if (remaining <= 0) break;

                int take = Math.min(inv.getQuantity(), remaining);
                String expiryStr = null;
                if (inv.getBatch() != null && inv.getBatch().getExpirationDate() != null) {
                    expiryStr = inv.getBatch().getExpirationDate().toString();
                }

                allocations.add(BatchAllocationDTO.builder()
                        .batchId(inv.getBatch() != null ? inv.getBatch().getBatchId() : null)
                        .available(inv.getQuantity())
                        .allocate(take)
                        .expiryDate(expiryStr)
                        .build());

                remaining -= take;
            }

            boolean canFulfill = totalAvailable >= neededBase;

            item.setAvailability(OrderItemAvailabilityDTO.builder()
                    .requestedQtyInBase(neededBase)
                    .totalAvailableInBase(totalAvailable)
                    .canFulfill(canFulfill)
                    .allocations(allocations)
                    .build());
        }
        dto.setPointsApplied(order.getPointsApplied() == null ? 0 : order.getPointsApplied());
        dto.setPointsUsed(order.getUser().getPointsUsed()); ;

        return dto;
    }

    private int defaultZero(Integer v) { return v == null ? 0 : v; }
}
