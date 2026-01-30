package com.zyna.dev.ecommerce.carts.service.impl;

import com.zyna.dev.ecommerce.carts.CartMapper;
import com.zyna.dev.ecommerce.carts.dto.request.AddToCartRequest;
import com.zyna.dev.ecommerce.carts.dto.request.UpdateCartItemRequest;
import com.zyna.dev.ecommerce.carts.dto.response.CartItemResponse;
import com.zyna.dev.ecommerce.carts.models.CartItem;
import com.zyna.dev.ecommerce.carts.repository.CartItemRepository;
import com.zyna.dev.ecommerce.carts.service.interfaces.CartService;
import com.zyna.dev.ecommerce.common.exceptions.ApplicationException;
import com.zyna.dev.ecommerce.products.models.Product;
import com.zyna.dev.ecommerce.products.repository.ProductRepository;
import com.zyna.dev.ecommerce.users.models.User;
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
    private final com.zyna.dev.ecommerce.products.repository.SizeRepository sizeRepository;
    private final com.zyna.dev.ecommerce.products.repository.ProductSizeRepository productSizeRepository;
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

        com.zyna.dev.ecommerce.products.models.Size size = sizeRepository.findById(request.getSizeId())
                .orElseThrow(() -> new ApplicationException(HttpStatus.NOT_FOUND, "Size not found linked to id: " + request.getSizeId()));

        CartItem cartItem = cartItemRepository.findByUserAndProductAndSize(user, product, size)
                .orElse(CartItem.builder()
                        .user(user)
                        .product(product)
                        .size(size)
                        .quantity(0)
                        .build());

        int newQty = cartItem.getQuantity() + request.getQuantity();
        if (newQty <= 0) {
            throw new ApplicationException(HttpStatus.BAD_REQUEST, "Quantity must be > 0");
        }

        // Check Stock using ProductSize
        com.zyna.dev.ecommerce.products.models.ProductSize productSize = productSizeRepository.findByProductAndSize(product, size)
                .orElseThrow(() -> new ApplicationException(HttpStatus.BAD_REQUEST, "Product variant (Size) invalid"));

        if (newQty > productSize.getQuantity()) {
            throw new ApplicationException(
                    HttpStatus.BAD_REQUEST,
                    "Quantity exceeds available stock for this size"
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

        com.zyna.dev.ecommerce.products.models.ProductSize productSize = productSizeRepository.findByProductAndSize(item.getProduct(), item.getSize())
                .orElseThrow(() -> new ApplicationException(HttpStatus.BAD_REQUEST, "Product variant definition missing"));

        if (request.getQuantity() > productSize.getQuantity()) {
            throw new ApplicationException(
                    HttpStatus.BAD_REQUEST,
                    "Quantity exceeds available stock for this size"
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
