package com.zyna.dev.ecommerce.cart.service.impl;

import com.zyna.dev.ecommerce.cart.CartMapper;
import com.zyna.dev.ecommerce.cart.dto.request.AddToCartRequest;
import com.zyna.dev.ecommerce.cart.dto.request.UpdateCartItemRequest;
import com.zyna.dev.ecommerce.cart.dto.response.CartItemResponse;
import com.zyna.dev.ecommerce.cart.models.CartItem;
import com.zyna.dev.ecommerce.cart.repository.CartItemRepository;
import com.zyna.dev.ecommerce.cart.service.interfaces.CartService;
import com.zyna.dev.ecommerce.common.exceptions.ApplicationException;
import com.zyna.dev.ecommerce.products.models.Product;
import com.zyna.dev.ecommerce.products.repository.ProductRepository;
import com.zyna.dev.ecommerce.users.User;
import com.zyna.dev.ecommerce.users.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CartServiceImpl implements CartService {

    private final CartItemRepository cartItemRepository;
    private final UserRepository userRepository;
    private final ProductRepository productRepository;
    private final CartMapper cartMapper;

    @Override
    @Transactional(readOnly = true)
    public List<CartItemResponse> getMyCart(Long userId) {
        User user = getUser(userId);
        List<CartItem> items = cartItemRepository.findByUser(user);
        return cartMapper.toCartItemResponses(items);
    }

    @Override
    @Transactional
    public CartItemResponse addToCart(Long userId, AddToCartRequest request) {
        User user = getUser(userId);
        Product product = productRepository.findById(request.getProductId())
                .orElseThrow(() -> new ApplicationException(
                        HttpStatus.NOT_FOUND, "Product not found"
                ));

        CartItem cartItem = cartItemRepository.findByUserAndProduct(user, product)
                .orElse(CartItem.builder()
                        .user(user)
                        .product(product)
                        .quantity(0)
                        .build());

        int newQty = cartItem.getQuantity() + request.getQuantity();
        if (newQty <= 0) {
            throw new ApplicationException(HttpStatus.BAD_REQUEST, "Quantity must be > 0");
        }

        // OPTIONAL: check không cho vượt stock hiện tại
        if (product.getStock() != null && newQty > product.getStock()) {
            throw new ApplicationException(
                    HttpStatus.BAD_REQUEST,
                    "Quantity exceeds available stock"
            );
        }

        cartItem.setQuantity(newQty);
        CartItem saved = cartItemRepository.save(cartItem);

        return cartMapper.toCartItemResponse(saved);
    }

    @Override
    @Transactional
    public CartItemResponse updateCartItem(Long userId, Long cartItemId, UpdateCartItemRequest request) {
        User user = getUser(userId);
        CartItem item = cartItemRepository.findById(cartItemId)
                .orElseThrow(() -> new ApplicationException(HttpStatus.NOT_FOUND, "Cart item not found"));

        if (!item.getUser().getId().equals(user.getId())) {
            throw new ApplicationException(HttpStatus.FORBIDDEN, "Not your cart item");
        }

        // Nếu quantity = 0 thì xoá item luôn
        if (request.getQuantity() == 0) {
            cartItemRepository.delete(item);
            return null; // FE có thể hiểu là "đã xoá"
        }

        if (item.getProduct().getStock() != null
                && request.getQuantity() > item.getProduct().getStock()) {
            throw new ApplicationException(
                    HttpStatus.BAD_REQUEST,
                    "Quantity exceeds available stock"
            );
        }

        item.setQuantity(request.getQuantity());
        CartItem saved = cartItemRepository.save(item);

        return cartMapper.toCartItemResponse(saved);
    }

    @Override
    @Transactional
    public void removeCartItem(Long userId, Long cartItemId) {
        User user = getUser(userId);
        CartItem item = cartItemRepository.findById(cartItemId)
                .orElseThrow(() -> new ApplicationException(HttpStatus.NOT_FOUND, "Cart item not found"));

        if (!item.getUser().getId().equals(user.getId())) {
            throw new ApplicationException(HttpStatus.FORBIDDEN, "Not your cart item");
        }

        cartItemRepository.delete(item);
    }

    @Override
    @Transactional
    public void clearCart(Long userId) {
        User user = getUser(userId);
        cartItemRepository.deleteByUser(user);
    }

    // ============== PRIVATE HELPER ==============

    private User getUser(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new ApplicationException(HttpStatus.NOT_FOUND, "User not found"));
    }
}
