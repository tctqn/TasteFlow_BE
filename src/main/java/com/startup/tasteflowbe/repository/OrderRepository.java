package com.startup.tasteflowbe.repository;

import com.startup.tasteflowbe.model.Order;
import com.startup.tasteflowbe.model.User;
import com.startup.tasteflowbe.model.Voucher;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {
    Optional<Order> findByOrderCode(String orderCode);
    List<Order> findOrdersByUser_UserId(Long userId);
    List<Order> findByStore_StoreId(Long storeId);

        @Query("""
        select distinct o
        from Order o
        left join fetch o.store s
        left join fetch o.orderItems oi
        left join fetch oi.product p
        left join fetch oi.productUnit u
        where o.orderId = :orderId
    """)
        Optional<Order> findByIdWithItemsAndStore(@Param("orderId") Long orderId);

    @Query("SELECT COUNT(o) FROM Order o JOIN o.vouchers v WHERE o.user = :user AND v = :voucher")
    int countByUserAndVoucher(@Param("user") User user, @Param("voucher") Voucher voucher);


}
