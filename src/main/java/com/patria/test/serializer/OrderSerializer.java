package com.patria.test.serializer;

import org.springframework.stereotype.Service;

import com.patria.test.dto.response.OrderResponse;
import com.patria.test.entity.Order;
import com.patria.test.service.ItemService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class OrderSerializer {

    private final ItemSerializer itemSerializer;
    private final ItemService itemService;

    public OrderResponse serialize(Order data) throws Exception{
        return OrderResponse.builder()
                .orderNo(data.getOrderNo())
                .item(itemSerializer.serialize(data.getItem(), itemService.getStockByItem(data.getItem())))
                .qty(data.getQty())
                .price(data.getPrice())
                .build();
    }
}
