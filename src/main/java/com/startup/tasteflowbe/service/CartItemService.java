package com.startup.tasteflowbe.service;

import com.startup.tasteflowbe.model.CartItem;

import java.util.List;

public interface CartItemService {
    CartItem addCartItem(CartItem cartItem);
    CartItem getCartItemById(Long cartItemId);
    List<CartItem> getAllCartItems();
}
