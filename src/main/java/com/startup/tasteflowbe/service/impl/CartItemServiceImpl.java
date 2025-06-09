package com.startup.tasteflowbe.service.impl;

import com.startup.tasteflowbe.model.CartItem;
import com.startup.tasteflowbe.repository.CartItemRepository;
import com.startup.tasteflowbe.service.CartItemService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class CartItemServiceImpl implements CartItemService {

    @Autowired
    private CartItemRepository cartItemRepository;

    @Override
    public CartItem addCartItem(CartItem cartItem) {
        return cartItemRepository.save(cartItem);
    }

    @Override
    public CartItem getCartItemById(Long cartItemId) {
        Optional<CartItem> cartItem = cartItemRepository.findById(cartItemId);
        return cartItem.orElse(null);
    }

    @Override
    public List<CartItem> getAllCartItems() {
        return cartItemRepository.findAll();
    }
}
