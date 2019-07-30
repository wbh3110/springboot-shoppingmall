package com.shoppingmall.service;

import com.shoppingmall.domain.Cart;
import com.shoppingmall.domain.NormalUser;
import com.shoppingmall.domain.Product;
import com.shoppingmall.domain.ProductOrder;
import com.shoppingmall.domain.enums.OrderStatus;
import com.shoppingmall.dto.ProductOrderRequestDto;
import com.shoppingmall.exception.NotExistCartException;
import com.shoppingmall.exception.NotExistUserException;
import com.shoppingmall.repository.CartRepository;
import com.shoppingmall.repository.NormalUserRepository;
import com.shoppingmall.repository.ProductOrderRepository;
import com.shoppingmall.repository.ProductRepository;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;

@Slf4j
@AllArgsConstructor
@Service
public class ProductOrderService {

    private CartRepository cartRepository;
    private NormalUserRepository normalUserRepository;
    private ProductOrderRepository productOrderRepository;
    private ProductRepository productRepository;

    public void makeOrder(ProductOrderRequestDto productOrderRequestDto) {

        List<Long> cartIdList = productOrderRequestDto.getCartIdList();

        Optional<Cart> cartOpt = cartRepository.findById(cartIdList.get(0));

        if (!cartOpt.isPresent()) {
            throw new NotExistCartException("존재하지 않는 장바구니 입니다.");
        }

        Cart cart = cartOpt.get();
        Long userId = cart.getNormalUser().getId();

        Optional<NormalUser> userOpt = normalUserRepository.findById(userId);

        if (!userOpt.isPresent()) {
            throw new NotExistUserException("존재하지 않는 유저 입니다.");
        }

        ProductOrder productOrder = productOrderRepository.save(ProductOrder.builder()
                .normalUser(userOpt.get())
                .orderNumber(productOrderRequestDto.getOrderNumber())
                .orderName(productOrderRequestDto.getOrderName())
                .amount(productOrderRequestDto.getAmount())
                .deliveryMessage(productOrderRequestDto.getDeliveryMessage())
                .address(productOrderRequestDto.getAddress())
                .orderStatus(OrderStatus.COMPLETE)
                .refundState('N')
                .build());

        List<HashMap<String, Object>> productMapList = new ArrayList<>();

        for (Long cartId : cartIdList) {
            cartOpt = cartRepository.findById(cartId);

            if(cartOpt.isPresent()) {
                // 사용한 장바구니 비활성화
                cart = cartOpt.get();
                cart.setProductOrder(productOrder);
                cart.setUseYn('N');

                HashMap<String, Object> productMap = new HashMap<>();
                productMap.put("product", cart.getProduct());
                productMap.put("productCount", cart.getProductCount());
                productMapList.add(productMap);

                cartRepository.save(cart);
            } else {
                throw new NotExistCartException("존재하지 않는 장바구니 입니다.");
            }
        }

        // 상품의 재고 수정
        for (HashMap<String, Object> productMap : productMapList) {
            Product product = (Product) productMap.get("product");
            Integer productCount = (Integer) productMap.get("productCount");

            product.setPurchaseCount(product.getPurchaseCount() + productCount);
            product.setLimitCount(product.getLimitCount() - productCount);
            product.setTotalCount(product.getTotalCount() - productCount);
            productRepository.save(product);
        }
    }
}
