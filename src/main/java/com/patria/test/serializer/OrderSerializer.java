package com.patria.test.serializer;

import org.springframework.stereotype.Service;

import com.patria.test.dto.response.OrderResponse;
import com.patria.test.entity.Order;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class OrderSerializer {

    private final ItemSerializer itemSerializer;

    public OrderResponse serialize(Order data, Integer stock) {
        return OrderResponse.builder()
                .orderNo(data.getOrderNo())
                .item(itemSerializer.serialize(data.getItem(), stock))
                .qty(data.getQty())
                .price(data.getPrice())
                .build();
    }
}
