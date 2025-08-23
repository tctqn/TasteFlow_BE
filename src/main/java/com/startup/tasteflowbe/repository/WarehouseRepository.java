package com.startup.tasteflowbe.repository;

import com.startup.tasteflowbe.model.Warehouse;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import org.springframework.data.repository.query.Param;
import org.springframework.data.jpa.repository.Query;

@Repository
public interface WarehouseRepository extends JpaRepository<Warehouse, Long> {
        Optional<Warehouse> findByManager_UserId(Long userId);

        Warehouse findByWarehouseId(Long warehouseId);

        @Query(value = "select name from warehouses where warehouse_id = :warehouseId", nativeQuery = true)
        String findWarehouseNameById(@Param("warehouseId") Long warehouseId);

        // On-hand của một sản phẩm tại kho
        @Query(value = "select coalesce(sum(i.quantity),0) " +
                        "from inventories i " +
                        "where i.warehouse_id = :warehouseId and i.product_id = :productId", nativeQuery = true)
        Double onHandOfProduct(@Param("warehouseId") Long warehouseId,
                        @Param("productId") Long productId);

        // Reorder level (nếu lưu ở inventories)
        @Query(value = "select coalesce(max(i.reorder_level),0) " +
                        "from inventories i " +
                        "where i.warehouse_id = :warehouseId and i.product_id = :productId", nativeQuery = true)
        Double reorderLevelOfProduct(@Param("warehouseId") Long warehouseId,
                        @Param("productId") Long productId);

        // Inbound trung bình (M tháng gần nhất) từ product_batches
        @Query(value = "select coalesce(sum(pb.quantity),0) / CAST(:months AS numeric) " +
                        "from product_batches pb " +
                        "where pb.warehouse_id = :warehouseId " +
                        "  and pb.product_id = :productId " +
                        "  and pb.received_date >= now() - (CAST(:months AS integer) * interval '1 month')", nativeQuery = true)
        Double avgMonthlyInboundOfProduct(@Param("warehouseId") Long warehouseId,
                        @Param("productId") Long productId,
                        @Param("months") int months);

        // Outbound → store trung bình (M tháng)
        @Query(value = "select coalesce(sum(sm.quantity),0) / CAST(:months AS numeric) " +
                        "from stock_movements sm " +
                        "where sm.warehouse_id = :warehouseId " +
                        "  and sm.product_id = :productId " +
                        "  and sm.movement_type = 'TRANSFER_TO_STORE' " +
                        "  and sm.movement_date >= now() - (CAST(:months AS integer) * interval '1 month')", nativeQuery = true)
        Double avgMonthlyOutboundToStoreOfProduct(@Param("warehouseId") Long warehouseId,
                        @Param("productId") Long productId,
                        @Param("months") int months);

        // Near-expiry on-hand trong N ngày tới
        @Query(value = "select coalesce(sum(i.quantity),0) " +
                        "from inventories i " +
                        "join product_batches pb on pb.batch_id = i.batch_id " +
                        "where i.warehouse_id = :warehouseId " +
                        "  and i.product_id = :productId " +
                        "  and pb.expiration_date between current_date and current_date + (CAST(:days AS integer) * interval '1 day')", nativeQuery = true)
        Double nearExpiryOnHandOfProduct(@Param("warehouseId") Long warehouseId,
                        @Param("productId") Long productId,
                        @Param("days") int days);

        // Min DTE trên tồn hiện có
        @Query(value = "select min((pb.expiration_date::date - current_date))::int " +
                        "from inventories i " +
                        "join product_batches pb on pb.batch_id = i.batch_id " +
                        "where i.warehouse_id = :warehouseId and i.product_id = :productId and i.quantity > 0", nativeQuery = true)
        Integer minDaysToExpiryOfProduct(@Param("warehouseId") Long warehouseId,
                        @Param("productId") Long productId);

        // Ước tính expired trung bình (M tháng) theo từng lô
        @Query(value = "with per_batch as ( " +
                        "  select pb.batch_id, pb.quantity as qty_received, pb.expiration_date " +
                        "  from product_batches pb " +
                        "  where pb.warehouse_id = :warehouseId " +
                        "    and pb.product_id = :productId " +
                        "    and pb.expiration_date <= now() " +
                        "    and pb.expiration_date >= now() - (CAST(:months AS integer) * interval '1 month') " +
                        "), consumed as ( " +
                        "  select sm.batch_id, sum(sm.quantity) as qty_out_before_exp " +
                        "  from stock_movements sm " +
                        "  join product_batches p2 on p2.batch_id = sm.batch_id " +
                        "  where sm.batch_id in (select batch_id from per_batch) " +
                        "    and sm.movement_date < p2.expiration_date " +
                        "    and sm.movement_type in ('TRANSFER_TO_STORE','TRANSFER_TO_WAREHOUSE','ADJUSTMENT_OUT','DAMAGE','WRITE_OFF') "
                        +
                        "  group by sm.batch_id " +
                        "), expired_est as ( " +
                        "  select greatest(0, p.qty_received - coalesce(c.qty_out_before_exp,0)) as expired_qty " +
                        "  from per_batch p left join consumed c on c.batch_id = p.batch_id " +
                        ") " +
                        "select coalesce(sum(expired_qty),0) / CAST(:months AS numeric) from expired_est", nativeQuery = true)
        Double avgMonthlyExpiredEstimateOfProduct(@Param("warehouseId") Long warehouseId,
                        @Param("productId") Long productId,
                        @Param("months") int months);

}
