package com.startup.tasteflowbe.repository;

import com.startup.tasteflowbe.model.CartItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;

@Repository
public interface CartItemRepository extends JpaRepository<CartItem, Long> {
    List<CartItem> findByUser_UserIdAndCartItemIdIn(Long userUserId, Collection<Long> cartItemIds);
}

