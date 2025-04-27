package com.startup.tasteflowbe.controller;

import com.startup.tasteflowbe.model.CartItem;
import com.startup.tasteflowbe.service.CartItemService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/cart-items")
public class CartItemController {

    @Autowired
    private CartItemService cartItemService;

    @PostMapping
    public CartItem addCartItem(@RequestBody CartItem cartItem) {
        return cartItemService.addCartItem(cartItem);
    }

    @GetMapping("/{id}")
    public CartItem getCartItemById(@PathVariable("id") Long id) {
        return cartItemService.getCartItemById(id);
    }

    @GetMapping
    public List<CartItem> getAllCartItems() {
        return cartItemService.getAllCartItems();
    }
}
